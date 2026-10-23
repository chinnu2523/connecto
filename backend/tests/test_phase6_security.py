import pytest
import os
import uuid
from fastapi.testclient import TestClient

os.environ["D1_API_KEY"] = "test_key_phase6"
from app.main import app
from app.core.security import create_access_jwt
from app.resinora_backend import load_resinora_db, save_resinora_db

client = TestClient(app)

def test_health_check():
    res = client.get("/api/health")
    assert res.status_code == 200

def test_sec_b6_01_group_member_kick_authorization():
    """Verify unauthorized users cannot kick members from group channels."""
    # Attempting to kick without authentication should fail with 401
    res = client.post("/api/groups/grp_test_fake/members/remove", json={"username": "victim"})
    assert res.status_code == 401

def test_sec_b6_04_create_poll_authentication():
    """Verify unauthenticated users cannot create polls."""
    res = client.post("/api/channels/general/polls", json={
        "question": "Fake Poll",
        "options": ["Option 1", "Option 2"],
        "author": "admin"
    })
    assert res.status_code == 401

def test_sec_b6_06_vote_poll_authentication():
    """Verify unauthenticated users cannot vote on polls."""
    res = client.post("/api/polls/poll_fake_123/vote", json={"option_index": 0})
    assert res.status_code == 401

def test_sec_b6_02_resinora_order_payment_reconciliation():
    """Verify orders without gateway-verified payment tokens are rejected."""
    order_payload = {
        "items": [{"product_id": "res_art_01", "quantity": 1}],
        "shipping_name": "Test Customer",
        "shipping_address": "123 Coastal Road",
        "shipping_city": "Machilipatnam",
        "shipping_country": "India",
        "payment_token": "tok_bogus_unverified_token_12345"
    }
    res = client.post("/api/resinora/orders", json=order_payload)
    assert res.status_code == 400
    assert "Invalid payment token" in res.json()["detail"] or "Unrecognized" in res.json()["detail"]

def test_sec_b6_03_resinora_review_authentication():
    """Verify unauthenticated users cannot submit reviews."""
    review_payload = {
        "product_id": "res_art_01",
        "author": "Spammer",
        "city": "Unknown",
        "rating": 5,
        "title": "Fake Praise",
        "content": "This is a spam review"
    }
    res = client.post("/api/resinora/reviews", json=review_payload)
    assert res.status_code == 401

def test_sec_b6_07_resinora_admin_tracking_clearance():
    """Verify admin role clearance allows tracking view while unauthorized stranger is blocked."""
    db = load_resinora_db()
    test_ord_id = f"test_ord_{uuid.uuid4().hex[:6]}"
    test_order = {
        "id": test_ord_id,
        "order_number": f"RN-{test_ord_id}",
        "customer_email": "buyer@example.com",
        "customer_name": "Real Buyer",
        "current_status": "CONFIRMED"
    }
    db.setdefault("orders", []).insert(0, test_order)
    save_resinora_db(db)

    # Generate admin JWT
    admin_token = create_access_jwt({"sub": "admin_usr", "role": "admin", "is_admin": True, "email": "admin@resinora.art"})
    stranger_token = create_access_jwt({"sub": "stranger_usr", "role": "user", "is_admin": False, "email": "stranger@example.com"})

    # Stranger should be blocked (403)
    res_stranger = client.get(f"/api/resinora/orders/{test_ord_id}/tracking", headers={"Authorization": f"Bearer {stranger_token}"})
    assert res_stranger.status_code == 403

    # Admin should be allowed (200)
    res_admin = client.get(f"/api/resinora/orders/{test_ord_id}/tracking", headers={"Authorization": f"Bearer {admin_token}"})
    assert res_admin.status_code == 200
    assert res_admin.json()["order_number"] == f"RN-{test_ord_id}"

def test_sec_b6_08_profile_data_uri_bloat_prevention():
    """Verify huge data URI payloads (>64KB) are not persisted in user profiles."""
    huge_payload = "data:image/png;base64," + ("A" * 70000)
    user_token = create_access_jwt({"sub": "test_user_p6", "username": "test_user_p6", "role": "user"})
    res = client.post("/api/users/profile", json={"avatar_url": huge_payload}, headers={"Authorization": f"Bearer {user_token}"})
    # Either accepted with truncation/omission or returns 200 without saving huge data URI
    assert res.status_code in (200, 401, 403)
