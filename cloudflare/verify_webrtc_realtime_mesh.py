import asyncio
import json
import time
import urllib.request
import websockets

BASE_WS_URL = "wss://connecto.fun/ws"
BASE_HTTP_URL = "https://connecto.fun"

async def test_mesh():
    print("=" * 70)
    print("  CONNECTO CLOUDFLARE DURABLE OBJECT WEBRTC & REALTIME SYNC TEST")
    print("=" * 70)

    # 1. Connect two simulated users (Madara & Elena) to the Edge WebSocket mesh
    user1 = "madara_legend"
    user2 = "elena_vance"

    ws1_url = f"{BASE_WS_URL}/general/{user1}"
    ws2_url = f"{BASE_WS_URL}/general/{user2}"

    print(f"\n[STEP 1] Connecting {user1} to {ws1_url}...")
    ws1 = await websockets.connect(ws1_url)
    resp1 = json.loads(await ws1.recv())
    print(f"  ✓ {user1} connected: {resp1.get('engine', 'Edge')} (mode={resp1.get('mode')})")

    print(f"[STEP 2] Connecting {user2} to {ws2_url}...")
    ws2 = await websockets.connect(ws2_url)
    resp2 = json.loads(await ws2.recv())
    print(f"  ✓ {user2} connected: {resp2.get('engine', 'Edge')} (mode={resp2.get('mode')})")

    # Madara should have received presence_update for Elena
    presence_msg = json.loads(await ws1.recv())
    print(f"  ✓ {user1} received presence update: {presence_msg}")
    assert presence_msg.get("type") == "presence_update" and presence_msg.get("username") == user2

    # 3. Test Ping/Pong Latency
    print("\n[STEP 3] Measuring WebSocket Round-Trip Latency...")
    pings = []
    for i in range(3):
        t0 = time.time()
        await ws1.send(json.dumps({"action": "ping"}))
        pong = json.loads(await ws1.recv())
        latency_ms = (time.time() - t0) * 1000
        pings.append(latency_ms)
        print(f"  Ping {i+1}: {latency_ms:.2f} ms (response: {pong})")
    avg_latency = sum(pings) / len(pings)
    print(f"  ✓ Average WebSocket round-trip latency: {avg_latency:.2f} ms")

    # 4. Test Channel Broadcast (Elena -> General Channel -> Madara)
    print("\n[STEP 4] Testing Channel Real-Time Broadcast...")
    test_chat = f"Shinobi live synchronization verified at {time.strftime('%H:%M:%S')}!"
    t_chat_start = time.time()
    await ws2.send(json.dumps({
        "type": "message",
        "channel_id": "general",
        "content": test_chat
    }))

    # Madara receives Elena's message
    msg_received = json.loads(await ws1.recv())
    chat_latency_ms = (time.time() - t_chat_start) * 1000
    print(f"  ✓ {user1} received broadcast from {user2} in {chat_latency_ms:.2f} ms!")
    print(f"    Message payload: {msg_received.get('message', {}).get('content')}")
    assert msg_received.get("type") == "new_message"

    # Also drain Elena's own broadcast echo if subscribed
    try:
        ws2_echo = await asyncio.wait_for(ws2.recv(), timeout=0.5)
    except asyncio.TimeoutError:
        pass

    # 5. WebRTC Signaling: Call Initiate -> Ringing & Incoming
    print("\n[STEP 5] Testing WebRTC 1:1 Voice Call Initiation...")
    call_id = f"call_{int(time.time())}"
    t_call_start = time.time()
    await ws2.send(json.dumps({
        "action": "call:initiate",
        "target": user1,
        "call_id": call_id,
        "avatar": "🔥"
    }))

    # Elena receives ringing acknowledgment
    ringing = json.loads(await ws2.recv())
    print(f"  ✓ Caller ({user2}) received ringing state: {ringing.get('type')} (peer_online={ringing.get('peer_online')})")
    assert ringing.get("type") == "call:ringing" and ringing.get("peer_online") is True

    # Madara receives incoming call
    incoming = json.loads(await ws1.recv())
    webrtc_signaling_latency = (time.time() - t_call_start) * 1000
    print(f"  ✓ Callee ({user1}) received incoming call in {webrtc_signaling_latency:.2f} ms!")
    print(f"    Caller: {incoming.get('caller')} | Call ID: {incoming.get('call_id')}")
    assert incoming.get("type") == "call:incoming" and incoming.get("caller") == user2

    # 6. WebRTC Signaling: Call Accept -> Accepted
    print("\n[STEP 6] Testing WebRTC Call Accept...")
    await ws1.send(json.dumps({
        "action": "call:accept",
        "target": user2,
        "call_id": call_id
    }))

    accepted = json.loads(await ws2.recv())
    print(f"  ✓ Caller ({user2}) received call:accepted from {accepted.get('callee')}")
    assert accepted.get("type") == "call:accepted"

    # 7. WebRTC Signaling: Offer & Answer (SDP exchange)
    print("\n[STEP 7] Testing WebRTC SDP Offer / Answer Handshake...")
    dummy_offer = "v=0\r\no=Elena 12345 2 IN IP4 0.0.0.0\r\ns=Connecto Opus Voice\r\nt=0 0\r\nm=audio 5004 RTP/SAVPF 111"
    await ws2.send(json.dumps({
        "action": "call:offer",
        "target": user1,
        "call_id": call_id,
        "sdp": dummy_offer
    }))

    offer_recv = json.loads(await ws1.recv())
    print(f"  ✓ Callee ({user1}) received call:offer SDP from {offer_recv.get('caller')}")
    assert offer_recv.get("type") == "call:offer" and "Opus" in str(offer_recv.get("sdp"))

    dummy_answer = "v=0\r\no=Madara 67890 2 IN IP4 0.0.0.0\r\ns=Connecto Opus Voice\r\nt=0 0\r\nm=audio 5006 RTP/SAVPF 111"
    await ws1.send(json.dumps({
        "action": "call:answer",
        "target": user2,
        "call_id": call_id,
        "sdp": dummy_answer
    }))

    answer_recv = json.loads(await ws2.recv())
    print(f"  ✓ Caller ({user2}) received call:answer SDP from {answer_recv.get('callee')}")
    assert answer_recv.get("type") == "call:answer"

    # 8. WebRTC Signaling: ICE Candidate Relay
    print("\n[STEP 8] Testing WebRTC ICE Candidate Exchange...")
    candidate_obj = {
        "candidate": "candidate:1 1 UDP 2130706431 192.168.1.100 5004 typ host",
        "sdpMid": "0",
        "sdpMLineIndex": 0
    }
    await ws2.send(json.dumps({
        "action": "call:ice-candidate",
        "target": user1,
        "call_id": call_id,
        "candidate": candidate_obj
    }))

    ice_recv = json.loads(await ws1.recv())
    print(f"  ✓ Callee ({user1}) received ICE candidate: {ice_recv.get('candidate')}")
    assert ice_recv.get("type") == "call:ice-candidate"

    # 9. Voice Speaking Indicator (Audio stage activity halo)
    print("\n[STEP 9] Testing Voice Speaking State...")
    await ws1.send(json.dumps({
        "action": "voice:speaking",
        "room_id": "vr_lounge",
        "speaking": True
    }))
    speaking_recv = json.loads(await ws2.recv())
    print(f"  ✓ Peer ({user2}) received voice:speaking halo: user={speaking_recv.get('user')}, speaking={speaking_recv.get('speaking')}")
    assert speaking_recv.get("type") == "voice:speaking" and speaking_recv.get("speaking") is True

    # 10. Call Teardown: Call End
    print("\n[STEP 10] Testing WebRTC Call Hangup (call:end)...")
    await ws2.send(json.dumps({
        "action": "call:end",
        "target": user1,
        "call_id": call_id
    }))

    end_recv = json.loads(await ws1.recv())
    print(f"  ✓ Callee ({user1}) received call:ended: {end_recv}")
    assert end_recv.get("type") == "call:ended"

    # 11. REST API to WebSocket Bridge Broadcast Test
    print("\n[STEP 11] Testing HTTP REST API -> Real-Time WebSocket Push...")
    req = urllib.request.Request(
        f"{BASE_HTTP_URL}/api/channels/general/messages",
        data=json.dumps({"content": "Message posted via HTTP REST API endpoint"}).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "X-User-Username": "connecto_admin",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
    )
    with urllib.request.urlopen(req) as resp:
        rest_data = json.loads(resp.read().decode("utf-8"))
        print(f"  ✓ REST API message created in D1: id={rest_data.get('id')}")

    # Both Madara and Elena should immediately receive this REST-posted message over their WebSockets
    rest_ws_recv = json.loads(await asyncio.wait_for(ws1.recv(), timeout=2.0))
    print(f"  ✓ Madara WebSocket immediately received REST broadcast: {rest_ws_recv.get('message', {}).get('content')}")
    assert rest_ws_recv.get("type") == "new_message"

    await ws1.close()
    await ws2.close()

    print("\n" + "=" * 70)
    print("  ALL 11 REAL-TIME WEBRTC & CLOUDFLARE MESH TESTS PASSED PERFECTLY!")
    print("=" * 70)

if __name__ == "__main__":
    asyncio.run(test_mesh())
