import pytest
from fastapi.testclient import TestClient
import sys
import uuid
import base64
import json

sys.path.insert(0, "/home/viki/connecto-app")
from app.main import app

client = TestClient(app)

def test_health_endpoint():
    res = client.get("/api/health")
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "ok"
    assert data["version"] == "2.6.0"

def test_user_registration_and_login():
    test_uname = f"ninja_{uuid.uuid4().hex[:6]}"
    res_reg = client.post("/api/register", json={
        "username": test_uname,
        "password": "strong_password",
        "avatar": "⚡",
        "bio": "Elite Shinobi"
    })
    assert res_reg.status_code == 200
    reg_data = res_reg.json()
    assert reg_data["user"]["username"] == test_uname
    assert "token" in reg_data

    # Test login with the newly registered user
    res_login = client.post("/api/login", json={
        "username": test_uname,
        "password": "strong_password"
    })
    assert res_login.status_code == 200
    login_data = res_login.json()
    assert login_data["user"]["username"] == test_uname

    # Test login with incorrect password
    res_bad_pwd = client.post("/api/login", json={
        "username": test_uname,
        "password": "wrong_password"
    })
    assert res_bad_pwd.status_code == 401

    # Test login with non-existent user
    res_no_user = client.post("/api/login", json={
        "username": f"nonexistent_{uuid.uuid4().hex[:6]}",
        "password": "password"
    })
    assert res_no_user.status_code == 404

    # Test duplicate registration
    res_dup = client.post("/api/register", json={
        "username": test_uname,
        "password": "strong_password"
    })
    assert res_dup.status_code == 400

def test_signup_all_basic_details_and_login_match():
    unique_id = uuid.uuid4().hex[:6]
    test_uname = f"uchiha_{unique_id}"
    res_reg = client.post("/api/register", json={
        "username": test_uname,
        "nickname": f"Phantom Ren {unique_id}",
        "email": f"sasuke_{unique_id}@clan.leaf",
        "password": "chidori_lightning_99",
        "date_of_birth": "1999-07-23",
        "gender": "male",
        "bio": "Avenger of the Uchiha clan.",
        "avatar": "⚡"
    })
    assert res_reg.status_code == 200
    reg_data = res_reg.json()
    assert reg_data["user"]["username"] == test_uname
    assert reg_data["user"]["nickname"] == f"Phantom Ren {unique_id}"
    assert reg_data["user"]["email"] == f"sasuke_{unique_id}@clan.leaf"
    assert reg_data["user"]["date_of_birth"] == "1999-07-23"
    assert reg_data["user"]["gender"] == "male"
    assert reg_data["user"]["avatar"] == "⚡"
    assert reg_data["redirect_to"] == "login"

    # Now verify login against the saved database credentials
    res_login = client.post("/api/login", json={
        "username": test_uname,
        "password": "chidori_lightning_99"
    })
    assert res_login.status_code == 200
    login_data = res_login.json()
    assert login_data["user"]["username"] == test_uname
    assert login_data["user"]["nickname"] == f"Phantom Ren {unique_id}"
    assert login_data["user"]["avatar"] == "⚡"
    assert "token" in login_data

    # Verify invalid password rejected
    res_bad = client.post("/api/login", json={
        "username": test_uname,
        "password": "wrong_password_here"
    })
    assert res_bad.status_code == 401
    assert "Incorrect password" in res_bad.json()["detail"]

def test_stats_endpoint():
    res = client.get("/api/stats")
    assert res.status_code == 200
    data = res.json()
    assert isinstance(data["active_shinobi"], int)
    assert data["active_shinobi"] >= 1

def test_learning_tracks_endpoints():
    res_tracks = client.get("/api/learning/tracks?username=vance")
    assert res_tracks.status_code == 200
    tracks = res_tracks.json()
    assert len(tracks) >= 3

    res_genin = client.get("/api/learning/tracks/genin/lessons?username=vance")
    assert res_genin.status_code == 200

    res_lesson = client.get("/api/learning/lessons/genin-1?username=vance")
    assert res_lesson.status_code == 200

def test_unread_and_mark_read():
    res_dm = client.post("/api/dms/vance/messages", json={
        "sender": "phoenix",
        "recipient": "vance",
        "content": "Unread test message from Phoenix!",
        "avatar": "🍃"
    })
    assert res_dm.status_code == 200

    res_unreads = client.get("/api/dm/unread-counts?username=vance")
    assert res_unreads.status_code == 200
    assert "phoenix" in res_unreads.json()

    res_read = client.post("/api/dm/phoenix/mark-read?username=vance")
    assert res_read.status_code == 200

def test_guest_learning_progress():
    res = client.get("/api/learning/progress?username=guest")
    assert res.status_code == 200
    data = res.json()
    assert data["username"] == "guest"
    assert data["xp"] == 0
    assert data["rank"] == "Genin"

def test_google_auth_endpoint():
    g_email = f"shinobi_{uuid.uuid4().hex[:6]}@gmail.com"
    g_name = "Google Shinobi"
    res = client.post("/api/auth/google", json={
        "email": g_email,
        "name": g_name,
        "avatar": "⚡"
    })
    assert res.status_code == 200
    data = res.json()
    assert "token" in data
    assert "user" in data
    assert data["user"]["email"] == g_email
    assert data["user"]["nickname"] == g_name
    assert data["user"]["onboarding_completed"] is True
    assert data["user"]["auth_provider"] == "google"

def test_google_auth_code_flow_and_database_persistence():
    g_code = f"4/0AY0e-g_{uuid.uuid4().hex[:12]}"
    g_email = f"ninja_code_{uuid.uuid4().hex[:6]}@gmail.com"
    res = client.post("/api/auth/google", json={
        "code": g_code,
        "email": g_email,
        "name": "Auth Code Shinobi"
    })
    assert res.status_code == 200
    data = res.json()
    assert "token" in data
    assert data["user"]["google_auth_code"] == g_code
    assert data["user"]["email"] == g_email
    
    # Verify user exists in database
    uname = data["user"]["username"]
    res_profile = client.get(f"/api/user/profile?username={uname}")
    assert res_profile.status_code == 200
    assert res_profile.json()["user"]["username"] == uname
    assert res_profile.json()["user"]["email"] == g_email

def test_google_auth_credential_jwt_flow():
    sub_id = f"1092837465_{uuid.uuid4().hex[:8]}"
    email = f"jwt_shinobi_{uuid.uuid4().hex[:6]}@gmail.com"
    header = base64.urlsafe_b64encode(b'{"alg":"RS256","typ":"JWT"}').decode().rstrip("=")
    payload_dict = {
        "iss": "https://accounts.google.com",
        "sub": sub_id,
        "email": email,
        "name": "JWT Shinobi",
        "picture": "https://lh3.googleusercontent.com/a/sample-photo"
    }
    payload = base64.urlsafe_b64encode(json.dumps(payload_dict).encode()).decode().rstrip("=")
    mock_jwt = f"{header}.{payload}.mocksignature"

    res = client.post("/api/auth/google", json={
        "credential": mock_jwt
    })
    assert res.status_code == 200
    data = res.json()
    assert data["user"]["email"] == email
    assert data["user"]["google_id"] == sub_id
    assert data["user"]["picture"] == "https://lh3.googleusercontent.com/a/sample-photo"

def test_security_headers_present():
    res = client.get("/api/health")
    assert res.status_code == 200
    assert res.headers.get("X-Content-Type-Options") == "nosniff"
    assert res.headers.get("X-Frame-Options") == "SAMEORIGIN"
    assert res.headers.get("X-XSS-Protection") == "1; mode=block"
    assert res.headers.get("Referrer-Policy") == "strict-origin-when-cross-origin"

def test_file_upload_security_protections():
    # Attempting to upload dangerous executable should fail with 400
    res = client.post(
        "/api/upload",
        files={"file": ("malicious_payload.exe", b"MZ\x90\x00\x03\x00\x00\x00", "application/x-msdownload")}
    )
    assert res.status_code == 400
    assert "forbidden" in res.json()["detail"]

def test_file_upload_valid_file():
    res = client.post(
        "/api/upload",
        files={"file": ("sample_scroll.png", b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR", "image/png")}
    )
    assert res.status_code == 200
    data = res.json()
    assert "url" in data
    assert data["url"].startswith("/uploads/")

def test_cv_screening_qualified_candidate():
    sample_cv = b"""
    Senior Frontend Engineer with 6+ years of professional experience leading web application architecture.
    Expert in modern JavaScript (ES6+), TypeScript, and hands-on React development.
    Built scalable applications with Redux Toolkit, Zustand, and React Query for state management.
    Proficient in Tailwind CSS, Styled Components, and consuming RESTful and GraphQL APIs.
    Wrote comprehensive unit and integration tests using Jest and React Testing Library.
    Expertise in Git, GitFlow, Next.js, Node.js, and CI/CD pipelines via GitHub Actions.
    Designed responsive UI/UX workflows in Figma and optimized Core Web Vitals via Lighthouse.
    """
    res = client.post(
        "/api/cv/screen",
        data={
            "name": "Jane Doe",
            "email": "jane.doe@frontend.dev",
            "role": "Senior Frontend Developer"
        },
        files={"file": ("jane_cv.txt", sample_cv, "text/plain")}
    )
    assert res.status_code == 200
    data = res.json()
    assert "candidate" in data
    cand = data["candidate"]
    assert cand["name"] == "Jane Doe"
    assert cand["qualificationRate"] >= 0.75
    assert cand["status"] == "Qualified"
    assert "explanation" in cand

def test_cv_screening_underqualified_candidate():
    sample_cv = b"Junior web designer with 1 year experience in HTML, WordPress, and basic CSS styling."
    res = client.post(
        "/api/cv/screen",
        data={
            "name": "Bob Rookie",
            "email": "bob.rookie@example.com",
            "role": "Senior Frontend Developer"
        },
        files={"file": ("bob_cv.txt", sample_cv, "text/plain")}
    )
    assert res.status_code == 200
    data = res.json()
    cand = data["candidate"]
    assert cand["qualificationRate"] <= 0.60
    assert cand["status"] in ["Borderline", "Unqualified"]

def test_cv_candidates_roster():
    res = client.get("/api/cv/candidates")
    assert res.status_code == 200
    data = res.json()
    assert "candidates" in data
    assert isinstance(data["candidates"], list)
    assert len(data["candidates"]) >= 2

def test_cv_job_roles_endpoint():
    res = client.get("/api/cv/roles")
    assert res.status_code == 200
    data = res.json()
    assert "roles" in data
    assert len(data["roles"]) >= 8
    role_titles = [r["title"] for r in data["roles"]]
    assert any("Frontend" in t for t in role_titles)
    assert any("Backend" in t for t in role_titles)
    assert any("Security" in t for t in role_titles)
    assert any("AI & Machine Learning" in t for t in role_titles)

def test_cv_workflow_json_endpoint():
    res = client.get("/api/cv/workflow.json")
    assert res.status_code == 200
    data = res.json()
    assert "nodes" in data
    assert "connections" in data

def test_cv_screening_backend_engineer_role():
    sample_cv = b"""
    Senior Backend Architect with 6+ years building high-concurrency microservices in Python (asyncio) and FastAPI.
    Designed relational database schemas in PostgreSQL and cached hot queries in Redis with strict ACID compliance.
    Built real-time WebSocket and RESTful APIs handling 50k requests/sec.
    Deployed event-driven streaming with Kafka and containerized clusters using Docker and Kubernetes.
    Comprehensive test coverage using Pytest and CI/CD automation via GitHub Actions.
    """
    res = client.post(
        "/api/cv/screen",
        data={
            "name": "David Sterling",
            "email": "david.sterling@backend.io",
            "role": "Senior Backend Engineer"
        },
        files={"file": ("david_cv.txt", sample_cv, "text/plain")}
    )
    assert res.status_code == 200
    cand = res.json()["candidate"]
    assert cand["name"] == "David Sterling"
    assert cand["role"] == "Senior Backend Engineer"
    assert cand["qualificationRate"] >= 0.75
    assert cand["status"] == "Qualified"

def test_cv_screening_security_engineer_role():
    sample_cv = b"""
    Application Security Engineer with 4 years experience in Python, FastAPI, and React.
    Implemented OAuth 2.0, JWT RBAC, TLS 1.3 encryption, and OWASP Top 10 vulnerability mitigation.
    Conducted SAST/DAST scanning with Snyk, penetration testing, and rate limiting against DDoS attacks.
    Maintained secure Git CI/CD pipelines and published security incident response procedures.
    """
    res = client.post(
        "/api/cv/screen",
        data={
            "name": "Sarah Connor",
            "email": "sarah.connor@appsec.net",
            "role": "Fullstack Security Engineer"
        },
        files={"file": ("sarah_cv.txt", sample_cv, "text/plain")}
    )
    assert res.status_code == 200
    cand = res.json()["candidate"]
    assert cand["qualificationRate"] >= 0.75
    assert cand["status"] == "Qualified"

def test_cv_screening_ai_ml_engineer_role():
    sample_cv = b"""
    AI/ML Engineer with 4+ years developing agentic LLM pipelines using Python, PyTorch, and Gemini/GPT APIs.
    Engineered high-throughput RAG systems with Pinecone vector databases and Mistral OCR for PDF document extraction.
    Built autonomous multi-agent tool-calling workflows using n8n and LangChain.
    Deployed low-latency inference APIs via FastAPI and optimized evaluation benchmarks.
    """
    res = client.post(
        "/api/cv/screen",
        data={
            "name": "Ada Lovelace",
            "email": "ada.lovelace@ai-agents.com",
            "role": "AI & Machine Learning Engineer"
        },
        files={"file": ("ada_cv.txt", sample_cv, "text/plain")}
    )
    assert res.status_code == 200
    cand = res.json()["candidate"]
    assert cand["qualificationRate"] >= 0.75
    assert cand["status"] == "Qualified"

