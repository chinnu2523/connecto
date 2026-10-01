import urllib.request
import urllib.error
import json

def test_endpoint(name, url, headers=None, method="GET", body=None):
    hdrs = {"User-Agent": "Mozilla/5.0 (Connecto-Verifier/1.0)"}
    if headers:
        hdrs.update(headers)
    data = json.dumps(body).encode("utf-8") if body else None
    if body:
        hdrs["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=hdrs, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10.0) as resp:
            status = resp.status
            mode = resp.headers.get("X-Connecto-Mode", "cloud_server (edge)")
            content = resp.read().decode("utf-8")
            try:
                parsed = json.loads(content)
            except:
                parsed = content[:100]
            print(f"[{name}] Status: {status} | Mode: {mode}")
            return parsed
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8", errors="replace")[:200]
        print(f"[{name}] HTTP Error {e.code}: {err_msg}")
        return None
    except Exception as e:
        print(f"[{name}] Error: {e}")
        return None

print("=================================================================")
print("CONNECTO VERIFICATION: LOCAL SERVER VS CLOUD SERVER")
print("STRICT TEST ACCOUNT VERIFICATION: test_user")
print("=================================================================\n")

# 1. Login with test_user in Local Server Mode
local_login = test_endpoint(
    "LOCAL SERVER LOGIN (test_user)",
    "http://localhost:8081/api/auth/login",
    method="POST",
    body={"login": "test_user", "username": "test_user", "password": "TestPass123!"}
)
if local_login:
    tok = local_login.get("access_token") or local_login.get("token") or "none"
    u = local_login.get("user", {})
    uname = u.get("username") or local_login.get("username", "test_user")
    dname = u.get("display_name") or local_login.get("display_name", "Test User")
    print(f"  -> Local User: {uname} ({dname}) | Token prefix: {tok[:15]}...\n")

# 2. Login with test_user in Cloud Server Mode
cloud_login = test_endpoint(
    "CLOUD SERVER LOGIN (test_user)",
    "https://connecto.fun/api/auth/login",
    headers={"X-Force-Cloud": "1"},
    method="POST",
    body={"login": "test_user", "username": "test_user", "password": "TestPass123!"}
)
if cloud_login:
    tok = cloud_login.get("access_token") or cloud_login.get("token") or "none"
    u = cloud_login.get("user", {})
    uname = u.get("username", "unknown")
    dname = u.get("display_name", "unknown")
    print(f"  -> Cloud User: {uname} ({dname}) | Token prefix: {tok[:15]}...\n")

# 3. Channel Messages Comparison
channels = ["general", "announcements", "gaming", "dev-chat"]
for ch in channels:
    print(f"--- Channel #{ch} ---")
    local_res = test_endpoint(f"LOCAL #{ch}", f"http://localhost:8081/api/channels/{ch}/messages")
    local_msgs = local_res.get("messages", []) if isinstance(local_res, dict) else (local_res if isinstance(local_res, list) else [])
    print(f"  -> Local Message Count: {len(local_msgs)}")
    
    cloud_res = test_endpoint(f"CLOUD #{ch}", f"https://connecto.fun/api/channels/{ch}/messages", headers={"X-Force-Cloud": "1"})
    cloud_msgs = cloud_res.get("messages", []) if isinstance(cloud_res, dict) else (cloud_res if isinstance(cloud_res, list) else [])
    print(f"  -> Cloud Message Count: {len(cloud_msgs)}")

    if cloud_msgs:
        first = cloud_msgs[0]
        c_text = first.get("content") or first.get("text") or ""
        author = first.get("sender_username") or first.get("author_name") or first.get("user") or "unknown"
        print(f"  -> Sample Cloud Msg: \"{c_text[:40]}...\" by {author}")
    print()

# 4. Voice Rooms
print("--- Voice Rooms ---")
v_local = test_endpoint("LOCAL VOICE ROOMS", "http://localhost:8081/api/voice/rooms")
v_cloud = test_endpoint("CLOUD VOICE ROOMS", "https://connecto.fun/api/voice/rooms", headers={"X-Force-Cloud": "1"})
l_rooms = v_local.get("rooms", []) if isinstance(v_local, dict) else (v_local if isinstance(v_local, list) else [])
c_rooms = v_cloud.get("rooms", []) if isinstance(v_cloud, dict) else (v_cloud if isinstance(v_cloud, list) else [])
print(f"  -> Local Rooms: {len(l_rooms)} | Cloud Rooms: {len(c_rooms)}\n")

# 5. Stats
print("--- Platform Stats ---")
s_local = test_endpoint("LOCAL STATS", "http://localhost:8081/api/stats")
s_cloud = test_endpoint("CLOUD STATS", "https://connecto.fun/api/stats", headers={"X-Force-Cloud": "1"})
print(f"  -> Local Stats: {s_local}")
print(f"  -> Cloud Stats: {s_cloud}")
