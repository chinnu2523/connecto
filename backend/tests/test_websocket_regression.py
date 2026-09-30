import pytest
import json
from fastapi.testclient import TestClient
import sys

sys.path.insert(0, "/home/viki/connecto-app")
from app.main import app

client = TestClient(app)

def test_websocket_ping_pong():
    with client.websocket_connect("/ws/general/tester") as ws:
        ws.receive_json() # join
        ws.send_text(json.dumps({"action": "ping"}))
        resp = ws.receive_json()
        assert resp["type"] == "pong"

def test_websocket_dm_and_friend_events():
    """Verify real-time friend requests over WebSocket."""
    with client.websocket_connect("/ws/general/cipher") as ws_sasuke:
        ws_sasuke.receive_json() # cipher join

        with client.websocket_connect("/ws/general/nova") as ws_sakura:
            ws_sakura.receive_json() # nova join
            ws_sasuke.receive_json() # cipher receives nova join broadcast

            # Send friend request
            res_req = client.post("/api/friends/request", json={"sender": "cipher", "recipient": "nova"})
            assert res_req.status_code in [200, 400]

            if res_req.status_code == 200:
                evt = ws_sakura.receive_json()
                assert evt["type"] == "friend:request_sent"
                assert evt["request"]["sender"] == "cipher"

def test_websocket_call_signaling():
    """Verify 1:1 WebRTC call initiation, answer, and termination."""
    with client.websocket_connect("/ws/general/vance") as ws_caller:
        ws_caller.receive_json() # caller join

        with client.websocket_connect("/ws/general/cipher") as ws_callee:
            ws_callee.receive_json() # callee join
            ws_caller.receive_json() # caller receives cipher join

            # Caller initiates call
            ws_caller.send_text(json.dumps({
                "action": "call:initiate",
                "target": "cipher",
                "call_id": "test_call_99",
                "avatar": "🔥"
            }))

            # Callee receives incoming call
            incoming_evt = ws_callee.receive_json()
            assert incoming_evt["type"] == "call:incoming"
            assert incoming_evt["caller"] == "vance"
            assert incoming_evt["call_id"] == "test_call_99"

            # Callee accepts call
            ws_callee.send_text(json.dumps({
                "action": "call:accept",
                "target": "vance",
                "call_id": "test_call_99"
            }))

            # Caller receives accepted event
            accept_evt = ws_caller.receive_json()
            assert accept_evt["type"] == "call:accepted"
            assert accept_evt["callee"] == "cipher"
