use std::{
    collections::HashSet,
    env,
    net::SocketAddr,
    sync::{
        atomic::{AtomicU64, Ordering},
        Arc,
    },
    time::Instant,
};

use axum::{
    extract::{
        ws::{Message, WebSocket, WebSocketUpgrade},
        Query, State,
    },
    http::HeaderMap,
    http::StatusCode,
    response::{IntoResponse, Json},
    routing::{get, post},
    Router,
};
use dashmap::DashMap;
use futures_util::{sink::SinkExt, stream::StreamExt};
use serde::{Deserialize, Serialize};
use tokio::sync::mpsc;
use axum::http::HeaderValue;
use tower_http::cors::CorsLayer;
use tracing::info;

static NEXT_CONN_ID: AtomicU64 = AtomicU64::new(1);
static TOTAL_MESSAGES_RELAYED: AtomicU64 = AtomicU64::new(0);
static TOTAL_BYTES_RELAYED: AtomicU64 = AtomicU64::new(0);

#[derive(Clone)]
struct AppState {
    start_time: Instant,
    connections: Arc<DashMap<u64, ClientInfo>>,
    channels: Arc<DashMap<String, HashSet<u64>>>,
    voice_rooms: Arc<DashMap<String, HashSet<u64>>>,
    user_to_conns: Arc<DashMap<String, HashSet<u64>>>,
    internal_secret: String,
}

struct ClientInfo {
    user_id: String,
    sender: mpsc::Sender<Message>,
}

#[derive(Debug, Deserialize)]
struct WsQuery {
    user_id: Option<String>,
    token: Option<String>,
    channel_id: Option<String>,
    room_id: Option<String>,
}

#[derive(Debug, Deserialize)]
#[allow(dead_code)]
struct ClientAction {
    action: Option<String>,
    #[serde(rename = "type")]
    msg_type: Option<String>,
    channel_id: Option<String>,
    room_id: Option<String>,
    data: Option<serde_json::Value>,
}

#[derive(Debug, Deserialize)]
struct BroadcastRequest {
    channel_id: Option<String>,
    room_id: Option<String>,
    user_ids: Option<Vec<String>>,
    exclude_conn_id: Option<u64>,
    exclude_user_id: Option<String>,
    payload: serde_json::Value,
}

#[derive(Debug, Serialize)]
struct HealthResponse {
    status: &'static str,
    service: &'static str,
    version: &'static str,
    active_connections: usize,
    active_channels: usize,
    active_voice_rooms: usize,
    total_messages_relayed: u64,
    total_bytes_relayed: u64,
    uptime_secs: u64,
}

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt()
        .with_env_filter(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "connecto_relay=info,tower_http=info".into()),
        )
        .init();

    let internal_secret = env::var("RELAY_INTERNAL_SECRET")
        .unwrap_or_else(|_| {
            eprintln!("WARNING: RELAY_INTERNAL_SECRET not set, using insecure default!");
            "change_me_in_production".to_string()
        });

    let state = AppState {
        start_time: Instant::now(),
        connections: Arc::new(DashMap::new()),
        channels: Arc::new(DashMap::new()),
        voice_rooms: Arc::new(DashMap::new()),
        user_to_conns: Arc::new(DashMap::new()),
        internal_secret,
    };

    // Restrict CORS to connecto.fun origins only (security hardening)
    let cors = CorsLayer::new()
        .allow_origin([
            "https://connecto.fun".parse::<HeaderValue>().unwrap(),
            "https://news.connecto.fun".parse::<HeaderValue>().unwrap(),
            "https://n8n.connecto.fun".parse::<HeaderValue>().unwrap(),
        ])
        .allow_methods([
            axum::http::Method::GET,
            axum::http::Method::POST,
            axum::http::Method::OPTIONS,
        ])
        .allow_headers(tower_http::cors::Any);

    let app = Router::new()
        .route("/health", get(health_handler))
        .route("/ws/relay", get(ws_handler))
        .route("/ws/voice", get(ws_voice_handler))
        .route("/api/broadcast", post(broadcast_handler))
        .layer(cors)
        .with_state(state);

    let bind_addr: SocketAddr = "127.0.0.1:8082".parse().unwrap();
    info!("Starting connecto-relay on http://{}", bind_addr);

    let listener = tokio::net::TcpListener::bind(bind_addr)
        .await
        .expect("Failed to bind TcpListener on 127.0.0.1:8082");

    axum::serve(
        listener,
        app.into_make_service_with_connect_info::<SocketAddr>(),
    )
    .await
    .expect("Server failed");
}

async fn health_handler(State(state): State<AppState>) -> Json<HealthResponse> {
    Json(HealthResponse {
        status: "ok",
        service: "connecto-relay",
        version: "0.1.0",
        active_connections: state.connections.len(),
        active_channels: state.channels.len(),
        active_voice_rooms: state.voice_rooms.len(),
        total_messages_relayed: TOTAL_MESSAGES_RELAYED.load(Ordering::Relaxed),
        total_bytes_relayed: TOTAL_BYTES_RELAYED.load(Ordering::Relaxed),
        uptime_secs: state.start_time.elapsed().as_secs(),
    })
}

fn constant_time_compare(a: &[u8], b: &[u8]) -> bool {
    if a.len() != b.len() {
        return false;
    }
    let mut diff = 0u8;
    for (x, y) in a.iter().zip(b.iter()) {
        diff |= x ^ y;
    }
    diff == 0
}

async fn broadcast_handler(
    State(state): State<AppState>,
    headers: HeaderMap,
    Json(req): Json<BroadcastRequest>,
) -> impl IntoResponse {
    // Security: Validate internal secret header using constant-time comparison
    let provided_secret = headers
        .get("X-Internal-Secret")
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    if !constant_time_compare(provided_secret.as_bytes(), state.internal_secret.as_bytes()) {
        return (
            StatusCode::UNAUTHORIZED,
            Json(serde_json::json!({ "error": "Unauthorized: invalid or missing X-Internal-Secret" })),
        );
    }

    let payload_str = match serde_json::to_string(&req.payload) {
        Ok(s) => s,
        Err(e) => {
            return (
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({ "error": e.to_string() })),
            );
        }
    };

    let payload_len = payload_str.len() as u64;
    let ws_msg = Message::Text(payload_str.into());

    let mut target_conns = HashSet::new();

    if let Some(ch) = &req.channel_id {
        if let Some(conns) = state.channels.get(ch) {
            target_conns.extend(conns.iter().copied());
        }
    }

    if let Some(r) = &req.room_id {
        if let Some(conns) = state.voice_rooms.get(r) {
            target_conns.extend(conns.iter().copied());
        }
    }

    if let Some(users) = &req.user_ids {
        for u in users {
            if let Some(conns) = state.user_to_conns.get(u) {
                target_conns.extend(conns.iter().copied());
            }
        }
    }

    if let Some(exclude_id) = req.exclude_conn_id {
        target_conns.remove(&exclude_id);
    }
    if let Some(exclude_user) = &req.exclude_user_id {
        if let Some(conns) = state.user_to_conns.get(exclude_user) {
            for c in conns.iter() {
                target_conns.remove(c);
            }
        }
    }

    let mut delivered = 0usize;
    for conn_id in target_conns {
        if let Some(client) = state.connections.get(&conn_id) {
            if client.sender.try_send(ws_msg.clone()).is_ok() {
                delivered += 1;
            }
        }
    }

    TOTAL_MESSAGES_RELAYED.fetch_add(delivered as u64, Ordering::Relaxed);
    TOTAL_BYTES_RELAYED.fetch_add((delivered as u64) * payload_len, Ordering::Relaxed);

    (
        StatusCode::OK,
        Json(serde_json::json!({
            "status": "broadcast_complete",
            "delivered_count": delivered
        })),
    )
}

async fn ws_handler(
    ws: WebSocketUpgrade,
    Query(query): Query<WsQuery>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, query, state, false))
}

async fn ws_voice_handler(
    ws: WebSocketUpgrade,
    Query(query): Query<WsQuery>,
    State(state): State<AppState>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, query, state, true))
}

async fn handle_socket(
    socket: WebSocket,
    query: WsQuery,
    state: AppState,
    _is_voice_preferred: bool,
) {
    let conn_id = NEXT_CONN_ID.fetch_add(1, Ordering::Relaxed);
    // Security: Only allow explicit user_id if token is present and non-empty,
    // or if the identifier is explicitly anonymous/guest. Otherwise enforce anon-<conn_id>
    let user_id = match (&query.user_id, &query.token) {
        (Some(uid), Some(tok)) if !tok.trim().is_empty() => uid.clone(),
        (Some(uid), _) if uid.starts_with("anon-") || uid.starts_with("guest_") => uid.clone(),
        _ => format!("anon-{}", conn_id),
    };

    let (mut ws_tx, mut ws_rx) = socket.split();
    let (tx, mut rx) = mpsc::channel::<Message>(512);

    state.connections.insert(
        conn_id,
        ClientInfo {
            user_id: user_id.clone(),
            sender: tx.clone(),
        },
    );

    state
        .user_to_conns
        .entry(user_id.clone())
        .or_default()
        .insert(conn_id);

    if let Some(ch) = query.channel_id {
        state.channels.entry(ch).or_default().insert(conn_id);
    }

    if let Some(r) = query.room_id {
        state.voice_rooms.entry(r).or_default().insert(conn_id);
    }

    let send_task = tokio::spawn(async move {
        while let Some(msg) = rx.recv().await {
            if ws_tx.send(msg).await.is_err() {
                break;
            }
        }
    });

    let state_clone = state.clone();

    let recv_task = tokio::spawn(async move {
        while let Some(Ok(msg)) = ws_rx.next().await {
            match msg {
                Message::Text(text) => {
                    if let Ok(action) = serde_json::from_str::<ClientAction>(&text) {
                        let act = action.action.as_deref().or(action.msg_type.as_deref());
                        match act {
                            Some("subscribe") | Some("channel:join") => {
                                if let Some(ch) = action.channel_id {
                                    state_clone
                                        .channels
                                        .entry(ch)
                                        .or_default()
                                        .insert(conn_id);
                                }
                            }
                            Some("unsubscribe") | Some("channel:leave") => {
                                if let Some(ch) = action.channel_id {
                                    if let Some(mut conns) = state_clone.channels.get_mut(&ch) {
                                        conns.remove(&conn_id);
                                    }
                                }
                            }
                            Some("join_voice") | Some("voice:join") => {
                                if let Some(r) = action.room_id {
                                    state_clone
                                        .voice_rooms
                                        .entry(r)
                                        .or_default()
                                        .insert(conn_id);
                                }
                            }
                            Some("leave_voice") | Some("voice:leave") => {
                                if let Some(r) = action.room_id {
                                    if let Some(mut conns) = state_clone.voice_rooms.get_mut(&r) {
                                        conns.remove(&conn_id);
                                    }
                                }
                            }
                            Some("voice_chunk") | Some("audio_chunk") | Some("matrix_live") => {
                                if let Some(r) = action.room_id {
                                    if let Some(conns) = state_clone.voice_rooms.get(&r) {
                                        let relay_msg = Message::Text(text.clone());
                                        let len = text.len() as u64;
                                        let mut count = 0u64;
                                        for peer_id in conns.iter() {
                                            if *peer_id != conn_id {
                                                if let Some(client) = state_clone.connections.get(peer_id) {
                                                    if client.sender.try_send(relay_msg.clone()).is_ok() {
                                                        count += 1;
                                                    }
                                                }
                                            }
                                        }
                                        TOTAL_MESSAGES_RELAYED.fetch_add(count, Ordering::Relaxed);
                                        TOTAL_BYTES_RELAYED.fetch_add(count * len, Ordering::Relaxed);
                                    }
                                }
                            }
                            _ => {}
                        }
                    }
                }
                Message::Binary(bin) => {
                    for item in state_clone.voice_rooms.iter() {
                        let room_id = item.key();
                        let conns = item.value();
                        if conns.contains(&conn_id) {
                            let relay_msg = Message::Binary(bin.clone());
                            let len = bin.len() as u64;
                            let mut count = 0u64;
                            for peer_id in conns.iter() {
                                if *peer_id != conn_id {
                                    if let Some(client) = state_clone.connections.get(peer_id) {
                                        if client.sender.try_send(relay_msg.clone()).is_ok() {
                                            count += 1;
                                        }
                                    }
                                }
                            }
                            TOTAL_MESSAGES_RELAYED.fetch_add(count, Ordering::Relaxed);
                            TOTAL_BYTES_RELAYED.fetch_add(count * len, Ordering::Relaxed);
                            break;
                        }
                    }
                }
                Message::Close(_) => break,
                Message::Ping(_) => {}
                Message::Pong(_) => {}
            }
        }
    });

    tokio::select! {
        _ = send_task => {},
        _ = recv_task => {},
    };

    state.connections.remove(&conn_id);
    if let Some(mut conns) = state.user_to_conns.get_mut(&user_id) {
        conns.remove(&conn_id);
    }
    for mut item in state.channels.iter_mut() {
        item.value_mut().remove(&conn_id);
    }
    for mut item in state.voice_rooms.iter_mut() {
        item.value_mut().remove(&conn_id);
    }
}
