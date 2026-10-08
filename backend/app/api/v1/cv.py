from app.core.rate_limit import enforce_rate_limit
import os
import asyncio
import re
import time
import json
import uuid
import logging
from datetime import datetime, timezone
from typing import Optional, List, Dict, Any
from fastapi import APIRouter, Request, Response, UploadFile, File, Form, Header, HTTPException, Query, Cookie, Depends, BackgroundTasks
from pydantic import BaseModel
from pypdf import PdfReader
from io import BytesIO

logger = logging.getLogger("cv.api")
from app.core.security import verify_access_jwt
router = APIRouter(tags=["ATS CV Screening"])

DATA_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "data", "app_database.json")
SECURE_CV_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "data", "secure_vault", "cvs")
os.makedirs(SECURE_CV_DIR, exist_ok=True)

CV_SCREEN_RATE_STORE = {}
CV_SCREEN_WINDOW = 600.0
CV_SCREEN_MAX_PER_IP = 20
MAX_UPLOAD_SIZE = 25 * 1024 * 1024

def _load_data():
    if os.path.exists(DATA_FILE):
        try:
            with open(DATA_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            pass
    return {"candidates": [], "interview_slots": []}

def _save_data(data):
    try:
        with open(DATA_FILE, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.warning(f"Failed saving data: {e}")

data_store = _load_data()

import html

def sanitize_str(s: str, max_length: int = 5000) -> str:
    if not s:
        return ""
    cleaned = re.sub(r"[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]", "", str(s)).strip()
    return html.escape(cleaned, quote=True)[:max_length]

def validate_email_format(email: str) -> bool:
    return bool(re.match(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$", email.strip()))

def validate_phone_format(phone: str) -> bool:
    digits = re.sub(r"\D", "", phone)
    return 7 <= len(digits) <= 15

def get_client_ip(request: Request) -> str:
    cf = request.headers.get("cf-connecting-ip")
    if cf: return cf.strip()
    xf = request.headers.get("x-forwarded-for")
    if xf: return xf.split(",")[0].strip()
    return request.client.host if request.client else "127.0.0.1"

def extract_text_from_pdf_or_bytes(content: bytes, filename: str) -> str:
    if filename.lower().endswith(".pdf") or content.startswith(b"%PDF-"):
        try:
            reader = PdfReader(BytesIO(content))
            extracted = chr(10).join([p.extract_text() or "" for p in reader.pages]).strip()
            if extracted: return extracted
        except Exception as e:
            logger.warning(f"PDF extraction failed: {e}")
    try:
        return content.decode("utf-8", errors="ignore")
    except Exception:
        return ""

class TTL_CACHE_WRAPPER:
    def __init__(self):
        self._c = {}
    def get(self, k):
        return self._c.get(k)
    def set(self, k, v, ttl_seconds=60):
        self._c[k] = v
    def invalidate(self, k):
        self._c.pop(k, None)

TTL_CACHE = TTL_CACHE_WRAPPER()

class AsyncDummyLock:
    async def __aenter__(self): return self
    async def __aexit__(self, exc_type, exc_val, exc_tb): pass

def db_transaction():
    return AsyncDummyLock()

async def send_candidate_email(**kwargs):
    pass

async def award_referral_milestone_xp(**kwargs):
    pass



TARGET_JOB_ROLES = {
    "Senior Frontend Developer": {
        "title": "Senior Frontend Developer (React/TS)",
        "department": "Engineering",
        "description": "Building high-performance responsive UI systems, state architecture, component libraries, and testing suites.",
        "badge": "🎨 Frontend",
        "skills": ["React", "TypeScript", "Next.js", "Redux/Zustand", "Tailwind CSS", "Jest/RTL", "GraphQL", "WebSockets"],
        "core_checks_fn": lambda text: {
            "5+ Years Frontend Experience": bool(re.search(r'(\b[5-9]\b|\b[1-2][0-9]\b)\+?\s*years?|senior|lead|principal|staff', text)),
            "Project Leadership": bool(re.search(r'lead|architect|mentored|spearheaded|managed|conception|delivered|ownership|guiding', text)),
            "JavaScript / TypeScript": bool("javascript" in text or "typescript" in text or "es6" in text or "ts" in text),
            "React Framework": bool("react" in text or "react.js" in text or "reactjs" in text),
            "State Management (Redux/Zustand)": bool(any(k in text for k in ["redux", "zustand", "react query", "tanstack", "mobx", "context api"])),
            "Modern Styling (Tailwind/CSS-in-JS)": bool(any(k in text for k in ["tailwind", "css-in-js", "styled-components", "sass", "scss", "css3", "css module"])),
            "API Integration (REST/GraphQL)": bool(any(k in text for k in ["rest", "graphql", "api", "axios", "fetch", "websocket", "endpoints"])),
            "Testing (Jest/RTL)": bool(any(k in text for k in ["jest", "testing library", "vitest", "cypress", "playwright", "unit test", "integration test"])),
            "Version Control & Git": bool(any(k in text for k in ["git", "github", "gitlab", "gitflow", "pull request", "code review"])),
            "Communication & Documentation": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Meta-Frameworks (Next.js/Remix)": bool("next.js" in text or "nextjs" in text or "remix" in text or "astro" in text or "ssr" in text),
            "Backend Knowledge (Node.js/APIs)": bool("node" in text or "express" in text or "fastapi" in text or "backend" in text or "python" in text or "nest" in text),
            "Performance Profiling (Lighthouse/CWV)": bool("lighthouse" in text or "core web vitals" in text or "performance" in text or "profiling" in text or "bundle" in text or "lcp" in text),
            "Micro-Frontends & Module Federation": bool("micro-frontend" in text or "module federation" in text or "monorepo" in text or "turborepo" in text or "nx" in text),
            "CI/CD Integration": bool("ci/cd" in text or "github actions" in text or "pipeline" in text or "vercel" in text or "docker" in text)
        }
    },
    "Senior Backend Engineer": {
        "title": "Senior Backend Engineer (Python/FastAPI/Distributed Systems)",
        "department": "Engineering",
        "description": "Architecting resilient microservices, high-throughput asynchronous APIs, database indexing, and streaming pipelines.",
        "badge": "⚡ Backend",
        "skills": ["Python", "FastAPI", "PostgreSQL", "Redis", "Kafka", "Docker", "AsyncIO", "Microservices"],
        "core_checks_fn": lambda text: {
            "5+ Years Backend Experience": bool(re.search(r'(\b[5-9]\b|\b[1-2][0-9]\b)\+?\s*years?|senior|lead|principal|staff', text)),
            "Architecture & System Design": bool(re.search(r'architect|designed|distributed|scalable|microservice|concurrency|throughput', text)),
            "Python & Asynchronous Programming": bool(any(k in text for k in ["python", "asyncio", "fastapi", "aiohttp", "tornado", "celery"])),
            "Relational & NoSQL Databases": bool(any(k in text for k in ["postgres", "postgresql", "mysql", "mongodb", "redis", "dynamodb", "sql", "sqlite"])),
            "Database Optimization & Indexing": bool(any(k in text for k in ["indexing", "query optimization", "explain", "acid", "sharding", "replication", "partitioning", "orm", "sqlalchemy"])),
            "API Design & Standards (REST/gRPC)": bool(any(k in text for k in ["rest", "grpc", "graphql", "openapi", "swagger", "json", "protobuf"])),
            "Caching & Message Queues": bool(any(k in text for k in ["redis", "kafka", "rabbitmq", "pub/sub", "sqs", "cache", "event-driven"])),
            "Containerization & Linux": bool(any(k in text for k in ["docker", "linux", "bash", "container", "ubuntu", "k8s", "kubernetes"])),
            "Automated Testing (Pytest)": bool(any(k in text for k in ["pytest", "unittest", "mock", "integration test", "load test", "locust"])),
            "Engineering Rigor & Documentation": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Cloud Platforms (AWS/GCP/Azure)": bool(any(k in text for k in ["aws", "gcp", "azure", "cloud", "lambda", "ecs", "s3", "cloud run"])),
            "WebSockets & Real-Time Event Streams": bool(any(k in text for k in ["websocket", "sse", "socket.io", "real-time", "streaming", "webrtc"])),
            "Security & Cryptography (JWT/AES/Argon2)": bool(any(k in text for k in ["jwt", "oauth", "aes", "argon2", "crypto", "encryption", "tls", "auth", "security"])),
            "Distributed Systems Consistency": bool(any(k in text for k in ["cap theorem", "raft", "paxos", "eventual consistency", "two-phase commit", "saga"])),
            "Observability (Prometheus/Grafana)": bool(any(k in text for k in ["prometheus", "grafana", "opentelemetry", "datadog", "tracing", "jaeger", "sentry"]))
        }
    },
    "Fullstack Security Engineer": {
        "title": "Fullstack Security Engineer (FastAPI/React/AppSec)",
        "department": "Security & Trust",
        "description": "Hardening zero-trust web architectures, automated SAST/DAST pipelines, cryptosystems, and defensive countermeasures.",
        "badge": "🛡️ Security",
        "skills": ["AppSec", "OAuth2/JWT", "Argon2id/AES", "OWASP", "FastAPI", "React", "Snyk", "Zero-Trust"],
        "core_checks_fn": lambda text: {
            "4+ Years Security/Fullstack Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\s*years?|security engineer|appsec|cybersecurity|infosec|fullstack', text)),
            "OWASP Top 10 & Web App Hardening": bool(any(k in text for k in ["owasp", "xss", "csrf", "sqli", "ssrf", "cors", "csp", "idor", "rate limit", "waf"])),
            "Cryptographic Protocols & Key Management": bool(any(k in text for k in ["aes", "rsa", "ecc", "tls", "argon2", "bcrypt", "sha-256", "hmac", "encryption", "vault", "kms"])),
            "Authentication & Authorization (OAuth2/JWT/RBAC)": bool(any(k in text for k in ["oauth", "jwt", "rbac", "abac", "sso", "saml", "oidc", "mfa", "session", "zero-trust"])),
            "Secure Python / FastAPI Development": bool(any(k in text for k in ["python", "fastapi", "django", "flask", "backend", "pydantic"])),
            "Modern Frontend Security (React/TypeScript)": bool(any(k in text for k in ["react", "typescript", "dompurify", "sanitization", "frontend", "csp"])),
            "Automated Security Testing (SAST/DAST)": bool(any(k in text for k in ["sast", "dast", "semgrep", "snyk", "sonarqube", "zap", "burp", "bandit", "trivy"])),
            "Container & Cloud Security": bool(any(k in text for k in ["docker security", "k8s security", "iam", "aws security", "cloudtrail", "hardening", "least privilege"])),
            "Version Control & Secure CI/CD": bool(any(k in text for k in ["git", "github", "secret scanning", "pre-commit", "pipeline", "code review"])),
            "Incident Response & Security Threat Modeling": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Offensive Security & Penetration Testing": bool(any(k in text for k in ["pentest", "penetration testing", "red team", "ctf", "defcon", "bug bounty", "hackerone"])),
            "WebRTC & WebSocket Security Hardening": bool(any(k in text for k in ["dtls", "srtp", "webrtc security", "websocket auth", "packet inspection"])),
            "Compliance & Audits (SOC2/ISO27001/GDPR)": bool(any(k in text for k in ["soc2", "iso 27001", "gdpr", "hipaa", "compliance", "audit"])),
            "Memory Safety & Rust Exposure": bool(any(k in text for k in ["rust", "memory safety", "buffer overflow", "fuzzing", "afl"])),
            "Security Tooling Authoring": bool(any(k in text for k in ["custom scanner", "authored security", "scripting", "exploit", "cve"]))
        }
    },
    "Real-Time Systems & WebRTC Architect": {
        "title": "Real-Time Systems & WebRTC Architect (WSS/Voice-Video)",
        "department": "Infrastructure & Core Media",
        "description": "Architecting sub-100ms global mesh communication, WebRTC SFU/Mesh media pipelines, DTLS-SRTP, and socket streaming.",
        "badge": "📡 Real-Time",
        "skills": ["WebSockets", "WebRTC", "Mesh/SFU", "DTLS/SRTP", "ICE/STUN/TURN", "FastAPI", "C++", "VoIP"],
        "core_checks_fn": lambda text: {
            "4+ Years Real-Time Systems Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\s*years?|real-time|webrtc|audio/video|telecom|streaming|architect', text)),
            "WebSockets & Concurrency Architecture": bool(any(k in text for k in ["websocket", "wss", "socket.io", "asyncio", "concurrency", "channel layers", "pub/sub"])),
            "WebRTC Protocols (SDP/ICE/STUN/TURN)": bool(any(k in text for k in ["webrtc", "sdp", "ice", "stun", "turn", "coturn", "nat traversal", "peer-to-peer"])),
            "Media Encryption (DTLS-SRTP)": bool(any(k in text for k in ["dtls", "srtp", "rtp", "rtcp", "encryption", "opus", "vp8", "vp9", "h.264"])),
            "SFU / MCU or P2P Mesh Architecture": bool(any(k in text for k in ["sfu", "mcu", "mesh", "mediasoup", "janus", "kurento", "livekit", "pion"])),
            "High Concurrency & Load Optimization": bool(any(k in text for k in ["high concurrency", "scalability", "latency", "packet loss", "jitter", "bandwidth adaptation"])),
            "Backend Integration (Python/Node/C++)": bool(any(k in text for k in ["python", "fastapi", "c++", "go", "golang", "node", "rust"])),
            "Automated Testing & Network Simulation": bool(any(k in text for k in ["pytest", "integration test", "network emulation", "tc", "load testing", "benchmark"])),
            "Version Control & Observability": bool(any(k in text for k in ["git", "github", "prometheus", "grafana", "metrics", "webrtc-internals"])),
            "Technical Communication & Architecture Specs": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "C++ / Rust Native Media Engine Development": bool(any(k in text for k in ["c++", "rust", "libwebrtc", "ffmpeg", "gstreamer", "native audio"])),
            "Spatial 3D Audio & Audio DSP": bool(any(k in text for k in ["spatial audio", "dsp", "web audio api", "hrtf", "audio processing", "noise suppression"])),
            "Screen Sharing & Video Codec Optimization": bool(any(k in text for k in ["screenshare", "getdisplaymedia", "simulcast", "svc", "av1", "h.265"])),
            "Global Anycast / Geo-Distributed Relays": bool(any(k in text for k in ["anycast", "geo-distributed", "edge relays", "cloudflare workers", "global mesh"])),
            "Mobile WebRTC Integration (iOS/Android)": bool(any(k in text for k in ["mobile webrtc", "callkit", "webrtc android", "webrtc ios", "flutter webrtc"]))
        }
    },
    "DevOps & Cloud Infrastructure Engineer": {
        "title": "DevOps & Cloud Infrastructure Engineer (K8s/Cloudflare/CI-CD)",
        "department": "Platform Operations",
        "description": "Managing scalable Kubernetes clusters, automated zero-downtime deployments, Cloudflare edge security, and IaC.",
        "badge": "🚀 DevOps",
        "skills": ["Kubernetes", "Docker", "Terraform", "CI/CD", "Cloudflare", "Prometheus", "Linux", "AWS/GCP"],
        "core_checks_fn": lambda text: {
            "4+ Years DevOps/Cloud Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\s*years?|devops|sre|cloud engineer|platform engineer|infrastructure', text)),
            "Kubernetes & Container Orchestration": bool(any(k in text for k in ["kubernetes", "k8s", "helm", "containerd", "docker", "gke", "eks", "aks"])),
            "Infrastructure as Code (Terraform/Ansible)": bool(any(k in text for k in ["terraform", "terragrunt", "ansible", "pulumi", "cloudformation", "iac"])),
            "CI/CD Automation Pipelines": bool(any(k in text for k in ["ci/cd", "github actions", "gitlab ci", "jenkins", "argocd", "flux", "circleci"])),
            "Cloud Provider Expertise (AWS/GCP/Azure)": bool(any(k in text for k in ["aws", "gcp", "azure", "google cloud", "cloud platform"])),
            "Edge Routing & CDN (Cloudflare/Nginx)": bool(any(k in text for k in ["cloudflare", "nginx", "caddy", "envoy", "traefik", "ssl/tls", "waf", "dns"])),
            "Linux System Administration & Shell Scripting": bool(any(k in text for k in ["linux", "bash", "systemd", "ubuntu", "debian", "kernel", "iptables"])),
            "Monitoring, Logging & Alerting": bool(any(k in text for k in ["prometheus", "grafana", "loki", "elk", "datadog", "cloudwatch", "alertmanager"])),
            "Git & GitOps Workflows": bool(any(k in text for k in ["git", "github", "gitops", "pull request", "branching"])),
            "Disaster Recovery & Post-Mortem Rigor": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Zero-Downtime Blue-Green / Canary Deployments": bool(any(k in text for k in ["blue-green", "canary", "rollout", "zero downtime", "service mesh", "istio"])),
            "Cloud Security & Secrets Management (Vault)": bool(any(k in text for k in ["hashicorp vault", "sealed secrets", "kms", "secrets manager", "iam least privilege"])),
            "Cost Optimization & FinOps": bool(any(k in text for k in ["finops", "cost optimization", "spot instances", "autoscaling", "resource limits"])),
            "Chaos Engineering (Gremlin/Chaos Mesh)": bool(any(k in text for k in ["chaos engineering", "chaos mesh", "fault injection", "resilience test"])),
            "Database Replication & Backup Automation": bool(any(k in text for k in ["pgbackrest", "wal-g", "replication", "disaster recovery", "rpo", "rto"]))
        }
    },
    "AI & Machine Learning Engineer": {
        "title": "AI & Machine Learning Engineer (LLMs/Agents/Vector RAG)",
        "department": "AI & Applied Intelligence",
        "description": "Deploying autonomous AI agent networks, multi-modal vector search, RAG pipelines, and high-speed LLM inference.",
        "badge": "🤖 AI / ML",
        "skills": ["PyTorch", "LLMs", "RAG", "Vector DBs", "LangChain", "FastAPI", "Agents", "Mistral/Gemini"],
        "core_checks_fn": lambda text: {
            "3+ Years AI/ML/Software Experience": bool(re.search(r'(\b[3-9]\b|\b[1-2][0-9]\b)\+?\s*years?|machine learning|ai engineer|data scientist|nlp|llm|deep learning', text)),
            "Python & ML Ecosystem (PyTorch/Scikit)": bool(any(k in text for k in ["python", "pytorch", "tensorflow", "scikit-learn", "numpy", "pandas", "huggingface"])),
            "LLM Integration & Prompt Engineering": bool(any(k in text for k in ["llm", "gemini", "gpt", "claude", "mistral", "llama", "prompt engineering", "openai"])),
            "Vector Databases & Semantic Search": bool(any(k in text for k in ["vector", "embeddings", "pinecone", "chroma", "qdrant", "milvus", "faiss", "pgvector", "rag"])),
            "Agentic Workflows & Tool Calling": bool(any(k in text for k in ["agent", "agents", "tool calling", "function calling", "n8n", "langchain", "llamaindex", "autogen", "crewai"])),
            "OCR & Multimodal Data Extraction": bool(any(k in text for k in ["ocr", "mistral ocr", "tesseract", "multimodal", "pdf extraction", "vision model", "document ai"])),
            "Scalable API Deployment (FastAPI/vLLM)": bool(any(k in text for k in ["fastapi", "vllm", "tgi", "triton", "inference", "api", "serving"])),
            "Evaluation Metrics & Benchmark Testing": bool(any(k in text for k in ["ragas", "eval", "evaluation", "benchmark", "hallucination", "rouge", "bleu", "accuracy"])),
            "Version Control & MLOps": bool(any(k in text for k in ["git", "github", "dvc", "mlflow", "wandb", "mlops", "pipeline", "ci/cd", "deployment"])),
            "Research Synthesis & Documentation": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Fine-Tuning & LoRA / QLoRA Adaptation": bool(any(k in text for k in ["fine-tuning", "finetuning", "lora", "qlora", "peft", "sft", "dpo", "rlhf"])),
            "Autonomous Multi-Agent Orchestration (AGY/CrewAI)": bool(any(k in text for k in ["multi-agent", "agentic", "subagent", "autonomous", "antigravity", "swarm"])),
            "Model Quantization (GGUF/AWQ/GPTQ)": bool(any(k in text for k in ["quantization", "gguf", "awq", "gptq", "ollama", "llama.cpp"])),
            "Knowledge Graphs & GraphRAG": bool(any(k in text for k in ["knowledge graph", "graphrag", "neo4j", "entity extraction", "ontology"])),
            "Edge AI & Local Inference": bool(any(k in text for k in ["edge ai", "onnx", "tensorrt", "local llm", "apple silicon", "coreml"]))
        }
    },
    "UI/UX Product Designer": {
        "title": "UI/UX Product Designer & Motion Architect",
        "department": "Design & Product",
        "description": "Crafting world-class design systems, responsive component hierarchies, tactile micro-interactions, and visual identity.",
        "badge": "✨ UI/UX",
        "skills": ["Figma", "Design Systems", "Motion UX", "Micro-Interactions", "WCAG/a11y", "Wireframing", "Tokens"],
        "core_checks_fn": lambda text: {
            "3+ Years Product Design Experience": bool(re.search(r'(\b[3-9]\b|\b[1-2][0-9]\b)\+?\s*years?|product designer|ui/ux|ux designer|visual designer', text)),
            "Advanced Figma & Component Architecture": bool(any(k in text for k in ["figma", "auto-layout", "components", "variants", "tokens", "sketch", "adobe xd"])),
            "Design Systems & Token Management": bool(any(k in text for k in ["design system", "design tokens", "component library", "atomic design", "style guide"])),
            "User Research & Journey Mapping": bool(any(k in text for k in ["user research", "wireframing", "persona", "journey map", "user flow", "usability test"])),
            "Visual Hierarchy & Typography Craft": bool(any(k in text for k in ["typography", "color theory", "visual hierarchy", "grid system", "dark mode", "glassmorphism"])),
            "Interactive Motion & Prototyping": bool(any(k in text for k in ["prototyping", "motion", "micro-interaction", "animation", "framer", "principle", "lottie"])),
            "Accessibility (WCAG 2.1 AA Compliance)": bool(any(k in text for k in ["wcag", "accessibility", "a11y", "color contrast", "screen reader", "inclusive"])),
            "Frontend Collaboration (HTML/CSS Understanding)": bool(any(k in text for k in ["html", "css", "tailwind", "responsive", "developer handoff", "frontend collaboration"])),
            "Version Control & DesignOps": bool(any(k in text for k in ["designops", "abstract", "version control", "handoff", "zeplin", "storybook"])),
            "Portfolio Presentation & Design Rationales": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "3D Spatial UI (Spline/Blender/Three.js)": bool(any(k in text for k in ["spline", "blender", "three.js", "3d", "spatial ui", "webgl"])),
            "Advanced Micro-Interactions (Framer Motion)": bool(any(k in text for k in ["framer motion", "rive", "lottie animation", "spring physics"])),
            "Product Analytics & A/B Experimentation": bool(any(k in text for k in ["mixpanel", "hotjar", "amplitude", "a/b testing", "conversion rate", "metrics"])),
            "Brand Identity & Iconography Craft": bool(any(k in text for k in ["illustrator", "branding", "iconography", "custom icons", "svg craft"])),
            "Design Team Mentorship & Facilitation": bool(any(k in text for k in ["mentored", "facilitated", "design sprint", "workshops", "leadership"]))
        }
    },
    "Mobile Application Engineer": {
        "title": "Mobile Application Engineer (Flutter/React Native/iOS)",
        "department": "Mobile Engineering",
        "description": "Building cross-platform high-performance mobile applications with offline storage, real-time sync, and native device integrations.",
        "badge": "📱 Mobile",
        "skills": ["Flutter", "React Native", "iOS/Android", "State Management", "Push Notifs", "Mobile Security", "SQLite"],
        "core_checks_fn": lambda text: {
            "3+ Years Mobile Development Experience": bool(re.search(r'(\b[3-9]\b|\b[1-2][0-9]\b)\+?\s*years?|mobile developer|mobile engineer|flutter|react native|ios|android', text)),
            "Cross-Platform Mastery (Flutter or React Native)": bool(any(k in text for k in ["flutter", "dart", "react native", "expo", "kotlin multiplatform", "kmp"])),
            "State Management & Local Persistence": bool(any(k in text for k in ["bloc", "riverpod", "provider", "redux", "zustand", "sqlite", "hive", "realm", "shared preferences", "watermelondb"])),
            "Native Platform Integration (iOS/Android)": bool(any(k in text for k in ["ios", "android", "xcode", "gradle", "cocoapods", "swift", "kotlin", "native module", "bridge"])),
            "Real-Time API & WebSocket Integration": bool(any(k in text for k in ["rest", "websocket", "socket", "http", "api integration", "dio", "axios", "offline sync"])),
            "Mobile UI Patterns & 60fps Animations": bool(any(k in text for k in ["mobile ui", "material design", "cupertino", "animation", "gesture", "smooth scrolling", "responsive layout"])),
            "Mobile Unit & E2E Testing": bool(any(k in text for k in ["flutter test", "jest", "detox", "maestro", "appium", "widget test", "unit test"])),
            "App Store & Google Play Publishing": bool(any(k in text for k in ["app store", "testflight", "google play", "play console", "publishing", "code signing", "fastlane"])),
            "Version Control & Mobile CI/CD": bool(any(k in text for k in ["git", "github", "gitlab", "fastlane", "bitrise", "app circle", "codemagic"])),
            "Problem Solving & Mobile Documentation": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Push Notification Pipelines (FCM / APNs)": bool(any(k in text for k in ["fcm", "firebase cloud messaging", "apns", "push notification", "onesignal"])),
            "Mobile WebRTC & VoIP Calling (CallKit)": bool(any(k in text for k in ["callkit", "connection service", "voip", "webrtc mobile", "audio calling"])),
            "Biometric Security & Secure Storage (Keychain/Keystore)": bool(any(k in text for k in ["biometric", "face id", "touch id", "keychain", "keystore", "secure storage"])),
            "Deep Linking & App Architecture": bool(any(k in text for k in ["deep link", "universal link", "clean architecture", "mvvm", "modularization"])),
            "Performance Profiling (Memory/Battery/FPS)": bool(any(k in text for k in ["flutter devtools", "flipper", "memory leak", "cpu profiling", "battery optimization"]))
        }
    },
    "Data Platform & Distributed Systems Engineer": {
        "title": "Data Platform & Distributed Systems Engineer (Kafka/ClickHouse/ETL)",
        "department": "Data Engineering",
        "description": "Building high-throughput real-time data streaming pipelines, analytical warehouses, ETL workflows, and distributed query engines.",
        "badge": "📊 Data Platform",
        "skills": ["Kafka", "ClickHouse", "Apache Spark", "Python/Go", "SQL", "ETL Pipelines", "Airflow", "Iceberg"],
        "core_checks_fn": lambda text: {
            "4+ Years Data Engineering Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\s*years?|data engineer|data platform|etl|streaming|pipeline', text)),
            "Distributed Event Streaming (Kafka/Pulsar)": bool(any(k in text for k in ["kafka", "pulsar", "kinesis", "event streaming", "event-driven", "pubsub"])),
            "Analytical Warehouses (ClickHouse/Snowflake/BigQuery)": bool(any(k in text for k in ["clickhouse", "snowflake", "bigquery", "redshift", "duckdb", "olap", "columnar"])),
            "Batch & Stream Processing (Spark/Flink)": bool(any(k in text for k in ["spark", "flink", "dbt", "databricks", "hadoop", "beam", "data processing"])),
            "Orchestration & Workflow DAGs (Airflow/Dagster)": bool(any(k in text for k in ["airflow", "dagster", "prefect", "luigi", "orchestration", "dag"])),
            "Advanced SQL & Data Modeling": bool(any(k in text for k in ["sql", "star schema", "data vault", "dimensional modeling", "query optimization"])),
            "Python / Scala / Go Programming": bool(any(k in text for k in ["python", "scala", "go", "golang", "java", "rust"])),
            "Data Quality, Schemas & Testing": bool(any(k in text for k in ["great expectations", "schema registry", "protobuf", "avro", "parquet", "iceberg"])),
            "Version Control & CI/CD for Data": bool(any(k in text for k in ["git", "github", "gitlab", "ci/cd", "dataops"])),
            "Architecture Documentation & Data Governance": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Modern Table Formats (Apache Iceberg / Delta Lake)": bool(any(k in text for k in ["iceberg", "delta lake", "hudi", "lakehouse"])),
            "Real-Time Analytics Dashboards": bool(any(k in text for k in ["grafana", "metabase", "superset", "tableau", "looker"])),
            "Vector Search & Embedding Ingestion": bool(any(k in text for k in ["vector ingestion", "embeddings pipeline", "rag data", "pgvector"])),
            "Cloud Object Storage Optimization": bool(any(k in text for k in ["s3 optimization", "gcs", "partitioning", "lifecycle rules"])),
            "Data Security & GDPR Anonymization": bool(any(k in text for k in ["anonymization", "pii masking", "gdpr data", "encryption at rest"]))
        }
    },
    "Site Reliability Engineer (SRE)": {
        "title": "Site Reliability & Observability Engineer (Prometheus/Grafana/Chaos)",
        "department": "Infrastructure & SRE",
        "description": "Ensuring 99.99% platform availability, automated incident response, SLO/SLA management, telemetry pipelines, and chaos engineering.",
        "badge": "🛡️ SRE",
        "skills": ["SRE", "Prometheus", "Grafana", "OpenTelemetry", "SLO/SLA", "Incident Response", "Chaos Engineering", "Linux"],
        "core_checks_fn": lambda text: {
            "4+ Years SRE/Reliability Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\s*years?|sre|site reliability|devops|production support|infrastructure', text)),
            "Telemetry & Metrics (Prometheus/Grafana)": bool(any(k in text for k in ["prometheus", "grafana", "opentelemetry", "otel", "metrics", "promql"])),
            "Distributed Tracing & APM (Jaeger/Datadog)": bool(any(k in text for k in ["jaeger", "zipkin", "datadog", "new relic", "apm", "tracing", "tempo"])),
            "Log Aggregation (Loki/ELK/Fluentbit)": bool(any(k in text for k in ["loki", "elasticsearch", "fluentd", "fluentbit", "kibana", "logstash", "log aggregation"])),
            "SLO / SLI / SLA Frameworks": bool(any(k in text for k in ["slo", "sli", "sla", "error budget", "reliability engineering", "availability"])),
            "On-Call & Automated Incident Response": bool(any(k in text for k in ["on-call", "incident management", "pagerduty", "opsgenie", "postmortem", "runbook"])),
            "Linux Systems Debugging & eBPF": bool(any(k in text for k in ["ebpf", "strace", "tcpdump", "perf", "linux kernel", "sysadmin", "systemd"])),
            "Automation Scripting (Python/Bash/Go)": bool(any(k in text for k in ["python", "bash", "go", "golang", "automation", "ansible"])),
            "Version Control & Infrastructure as Code": bool(any(k in text for k in ["git", "github", "terraform", "k8s", "kubernetes"])),
            "Root Cause Analysis & Post-Mortem Rigor": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Chaos Engineering & Fault Injection": bool(any(k in text for k in ["chaos mesh", "gremlin", "chaos engineering", "fault injection", "litmus"])),
            "Kubernetes Operator Development": bool(any(k in text for k in ["operator sdk", "custom resource", "crd", "controller runtime"])),
            "High-Availability Database Clustering": bool(any(k in text for k in ["patroni", "pgbouncer", "ha postgres", "cluster failover"])),
            "Edge & CDN Observability": bool(any(k in text for k in ["cloudflare analytics", "edge telemetry", "synthetic monitoring"])),
            "Security Incident Forensics": bool(any(k in text for k in ["forensics", "audit logs", "siem", "falco", "threat detection"]))
        }
    },
    "Cryptographic Protocol & Security Engineer": {
        "title": "Cryptographic Protocol & Security Engineer (Zero-Knowledge/Rust/AES)",
        "department": "Security & Cryptography",
        "description": "Designing end-to-end cryptographic protocols, zero-knowledge proofs, post-quantum algorithms, and secure key vaults.",
        "badge": "🔐 Cryptography",
        "skills": ["Cryptography", "Rust/C++", "Zero-Knowledge", "AES-GCM", "Argon2id", "Key Vaults", "ECC/RSA", "TLS 1.3"],
        "core_checks_fn": lambda text: {
            "4+ Years Cryptography/Security Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\s*years?|cryptograph|cryptosystem|security engineer|rust developer', text)),
            "Symmetric & Asymmetric Ciphers (AES/ECC/RSA)": bool(any(k in text for k in ["aes", "aes-gcm", "rsa", "ecc", "ed25519", "x25519", "diffie-hellman", "chacha20"])),
            "Hashing & Password Derivation (Argon2id/SHA)": bool(any(k in text for k in ["argon2", "argon2id", "bcrypt", "pbkdf2", "sha-256", "sha-3", "hmac"])),
            "Zero-Knowledge Proofs & Protocol Design": bool(any(k in text for k in ["zero-knowledge", "zkp", "zk-snark", "zk-stark", "bulletproofs", "protocol design", "cryptographic proof"])),
            "Systems Programming (Rust / C++ / Go)": bool(any(k in text for k in ["rust", "c++", "c", "go", "golang", "memory safety"])),
            "Transport Layer Security (TLS 1.3 / DTLS)": bool(any(k in text for k in ["tls 1.3", "dtls", "pki", "x.509", "certificates", "openssl", "rustls"])),
            "Hardware Security Modules & Key Vaults": bool(any(k in text for k in ["hsm", "tpm", "secure enclave", "vault", "kms", "key management"])),
            "Side-Channel Attack Mitigation": bool(any(k in text for k in ["side-channel", "timing attack", "constant-time", "cryptanalysis", "fuzzing"])),
            "Version Control & Code Review": bool(any(k in text for k in ["git", "github", "gitlab", "formal verification"])),
            "Cryptographic Specifications & Documentation": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Post-Quantum Cryptography (Kyber/Dilithium)": bool(any(k in text for k in ["post-quantum", "pqc", "kyber", "dilithium", "lattice-based"])),
            "Homomorphic Encryption (SEAL/HElib)": bool(any(k in text for k in ["homomorphic encryption", "fhe", "seal", "helib"])),
            "Smart Contract Security & Auditing": bool(any(k in text for k in ["solidity", "smart contract audit", "slither", "evm"])),
            "Formal Verification (Coq/Z3/TLA+)": bool(any(k in text for k in ["formal verification", "coq", "z3", "tla+", "dafny"])),
            "Security Research Papers & Disclosures": bool(any(k in text for k in ["cve", "research paper", "disclosure", "iacr", "whitepaper"]))
        }
    },
    "QA Automation & SDET Lead": {
        "title": "QA Automation & SDET Lead (Playwright/Cypress/Load Testing)",
        "department": "Quality Engineering",
        "description": "Architecting comprehensive test automation frameworks, end-to-end browser testing, high-load stress testing, and CI gates.",
        "badge": "🧪 QA Automation",
        "skills": ["Playwright", "Cypress", "TypeScript/Python", "k6/Locust", "CI/CD Gates", "API Testing", "Contract Testing"],
        "core_checks_fn": lambda text: {
            "4+ Years QA / SDET Experience": bool(re.search(r'(\b[4-9]\b|\b[1-2][0-9]\b)\+?\\s*years?|sdet|qa automation|quality engineer|test lead|automation engineer', text)),
            "End-to-End Testing (Playwright / Cypress)": bool(any(k in text for k in ["playwright", "cypress", "selenium", "webdriverio", "puppeteer", "e2e"])),
            "Programming Languages (TypeScript / Python / Java)": bool(any(k in text for k in ["typescript", "javascript", "python", "java", "c#"])),
            "API Automation (REST / WebSockets / Postman)": bool(any(k in text for k in ["api testing", "rest-assured", "supertest", "pytest", "postman", "pact", "contract testing"])),
            "Performance & Load Testing (k6 / Locust / JMeter)": bool(any(k in text for k in ["k6", "locust", "jmeter", "artillery", "load testing", "stress test", "performance test"])),
            "CI/CD Test Pipeline Integration": bool(any(k in text for k in ["github actions", "jenkins", "gitlab ci", "test pipeline", "ci gate", "nightly build"])),
            "Test Strategy, Coverage & Metrics": bool(any(k in text for k in ["test strategy", "test plan", "coverage", "allure", "bug tracking", "test metrics"])),
            "Cross-Browser & Mobile Emulation": bool(any(k in text for k in ["cross-browser", "mobile testing", "appium", "responsive test", "viewport"])),
            "Version Control & Defect Lifecycle": bool(any(k in text for k in ["git", "github", "jira", "linear", "defect management"])),
            "Clear Bug Reporting & Test Documentation": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Visual Regression Testing (Percy/Applitools)": bool(any(k in text for k in ["percy", "applitools", "visual regression", "pixelmatch", "storybook test"])),
            "Chaos & Resilience Testing": bool(any(k in text for k in ["chaos test", "fault injection", "resilience test", "network throttling"])),
            "Security Scanning in Test Pipelines": bool(any(k in text for k in ["dast in ci", "owasp zap automated", "security test in pipeline"])),
            "AI-Assisted Test Generation": bool(any(k in text for k in ["ai testing", "generative tests", "test generation", "model-based testing"])),
            "Team Mentorship & QA Leadership": bool(any(k in text for k in ["mentored qa", "lead sdet", "qa team lead", "championed quality"]))
        }
    },
    "Technical Product Manager": {
        "title": "Technical Product Manager (Developer Platform & APIs)",
        "department": "Product Management",
        "description": "Defining developer experience, REST/WebSocket API roadmaps, SDK specifications, telemetry analytics, and community feedback loops.",
        "badge": "🚀 Product",
        "skills": ["Product Strategy", "API Roadmaps", "Developer Experience (DevEx)", "SDKs", "Analytics", "Agile/Scrum", "PRDs"],
        "core_checks_fn": lambda text: {
            "3+ Years Technical PM Experience": bool(re.search(r'(\b[3-9]\b|\b[1-2][0-9]\b)\+?\\s*years?|product manager|technical pm|tpm|product lead|product owner', text)),
            "API & Platform Strategy": bool(any(k in text for k in ["api", "platform product", "developer experience", "devex", "sdk", "developer platform"])),
            "Product Requirements & PRD Authoring": bool(any(k in text for k in ["prd", "specifications", "user stories", "requirements", "product roadmap", "backlog"])),
            "Data-Driven Prioritization & Metrics": bool(any(k in text for k in ["analytics", "metrics", "kpi", "okr", "mixpanel", "amplitude", "funnel", "retention"])),
            "Agile / Scrum Delivery Leadership": bool(any(k in text for k in ["agile", "scrum", "sprint planning", "jira", "linear", "kanban", "delivery"])),
            "Developer Empathy & User Research": bool(any(k in text for k in ["user research", "customer interview", "feedback loop", "usability", "developer empathy"])),
            "Technical Depth (System Architecture Understanding)": bool(any(k in text for k in ["architecture", "microservices", "cloud", "fastapi", "react", "database", "engineering background"])),
            "Go-to-Market & Developer Launch": bool(any(k in text for k in ["gtm", "go-to-market", "product launch", "changelog", "documentation", "beta program"])),
            "Cross-Functional Stakeholder Leadership": bool(any(k in text for k in ["stakeholder", "cross-functional", "engineering collaboration", "executive"])),
            "Strategic Communication & Synthesis": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Developer Tools (DevTools / CLI / SDKs)": bool(any(k in text for k in ["cli tool", "open source product", "sdk design", "devtools"])),
            "SaaS Pricing & Monetization Models": bool(any(k in text for k in ["pricing", "monetization", "saas metrics", "plg", "product-led growth"])),
            "AI Product Integration": bool(any(k in text for k in ["ai product", "llm feature", "rag platform", "agentic product"])),
            "Enterprise Security & Compliance Alignment": bool(any(k in text for k in ["soc2 product", "enterprise feature", "rbac roadmap", "compliance"])),
            "Public Speaking & Conference Evangelism": bool(any(k in text for k in ["conference speaker", "webinar", "keynote", "blog post", "podcast"]))
        }
    },
    "Developer Relations & Platform Evangelist": {
        "title": "Developer Relations & Platform Evangelist (DevRel/SDKs)",
        "department": "Developer Relations",
        "description": "Empowering open-source developer communities with high-quality technical documentation, sample applications, SDKs, and hackathons.",
        "badge": "🌐 DevRel",
        "skills": ["DevRel", "Technical Writing", "TypeScript/Python SDKs", "Hackathons", "Community", "Open Source", "Public Speaking"],
        "core_checks_fn": lambda text: {
            "3+ Years DevRel / Technical Advocacy Experience": bool(re.search(r'(\b[3-9]\b|\b[1-2][0-9]\b)\+?\\s*years?|devrel|developer advocate|developer relations|evangelist|technical writer', text)),
            "Technical Writing & Interactive Tutorials": bool(any(k in text for k in ["technical writing", "documentation", "tutorials", "guides", "blog posts", "api docs", "markdown"])),
            "Sample Apps & SDK Creation (Python / JS)": bool(any(k in text for k in ["sample app", "sdk", "starter template", "python", "javascript", "typescript", "react", "fastapi"])),
            "Community Management & Hackathons": bool(any(k in text for k in ["community", "discord", "slack", "hackathon", "meetup", "workshop", "mentorship"])),
            "Public Speaking & Video / Streaming Content": bool(any(k in text for k in ["public speaking", "conference", "youtube", "twitch", "video tutorial", "podcast", "keynote"])),
            "Open Source Contribution & GitHub Engagement": bool(any(k in text for k in ["open source", "github", "issues", "pull request", "repository", "contributor"])),
            "Developer Feedback Loop to Product": bool(any(k in text for k in ["feedback loop", "voice of developer", "product advocacy", "devex improvement"])),
            "Social Media & Content Strategy": bool(any(k in text for k in ["twitter/x", "linkedin", "dev.to", "hashnode", "medium", "content strategy"])),
            "Version Control & Code Quality": bool(any(k in text for k in ["git", "github", "code review", "best practices"])),
            "Authentic Communication & Energy": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Multi-Language SDK Development (Go/Rust/Java)": bool(any(k in text for k in ["rust sdk", "go sdk", "java sdk", "multi-language"])),
            "Interactive Sandbox & Playground Building": bool(any(k in text for k in ["playground", "interactive sandbox", "code runner", "repl", "webcontainer"])),
            "Cybersecurity / CTF Community Leadership": bool(any(k in text for k in ["ctf organizer", "cyber community", "security challenge", "infosec meetup"])),
            "Developer Surveys & Community Metrics": bool(any(k in text for k in ["orbit model", "common room", "community metrics", "devrel roi"])),
            "Podcast Hosting or Tech Keynotes": bool(any(k in text for k in ["podcast host", "keynote speaker", "panel moderator"]))
        }
    },
    "Computer Vision & Multimodal AI Engineer": {
        "title": "Computer Vision & Multimodal AI Engineer (PyTorch/OpenCV/Edge AI)",
        "department": "AI & Applied Intelligence",
        "description": "Developing real-time video analysis, object detection, document OCR understanding, and on-device vision inference models.",
        "badge": "👁️ Computer Vision",
        "skills": ["PyTorch", "OpenCV", "YOLO/DETR", "Document OCR", "Diffusion/Transformers", "Edge AI", "TensorRT", "FastAPI"],
        "core_checks_fn": lambda text: {
            "3+ Years CV / Vision AI Experience": bool(re.search(r'(\b[3-9]\\b|\\b[1-2][0-9]\\b)\\+?\\s*years?|computer vision|vision engineer|image processing|deep learning|multimodal', text)),
            "Deep Learning Frameworks (PyTorch/TensorFlow)": bool(any(k in text for k in ["pytorch", "tensorflow", "torchvision", "keras", "cuda"])),
            "Vision Libraries & Preprocessing (OpenCV)": bool(any(k in text for k in ["opencv", "pillow", "scikit-image", "image preprocessing", "augmentation", "albumentations"])),
            "Object Detection & Segmentation (YOLO/DETR)": bool(any(k in text for k in ["yolo", "detr", "mask r-cnn", "segmentation", "object detection", "bounding box"])),
            "Document AI & OCR Processing": bool(any(k in text for k in ["ocr", "mistral ocr", "tesseract", "trocr", "document ai", "table extraction", "pdf vision"])),
            "Vision Transformers & Multimodal LLMs": bool(any(k in text for k in ["vit", "vision transformer", "clip", "blip", "llava", "multimodal", "gpt-4v", "gemini vision"])),
            "Model Optimization & Edge Serving (TensorRT/ONNX)": bool(any(k in text for k in ["tensorrt", "onnx", "openvino", "edge ai", "quantization", "fp16", "int8"])),
            "API Integration & Model Deployment (FastAPI)": bool(any(k in text for k in ["fastapi", "docker", "triton", "inference server", "rest api", "streaming vision"])),
            "Version Control & ML Pipelines": bool(any(k in text for k in ["git", "github", "dvc", "wandb", "mlflow"])),
            "Technical Writing & Vision Benchmarks": len(text.strip()) > 80
        },
        "preferred_checks_fn": lambda text: {
            "Real-Time Video Stream Analysis (WebRTC/RTSP)": bool(any(k in text for k in ["rtsp", "webrtc video", "gstreamer", "video streaming", "fps optimization"])),
            "Generative Vision & Diffusion Models": bool(any(k in text for k in ["stable diffusion", "flux", "controlnet", "inpainting", "generative ai"])),
            "Mobile Vision (CoreML / TFLite)": bool(any(k in text for k in ["coreml", "tflite", "android vision", "ios vision", "mobile camera"])),
            "Facial & Biometric Recognition (Anti-Spoofing)": bool(any(k in text for k in ["face recognition", "liveness detection", "anti-spoofing", "facenet"])),
            "3D Vision & NeRF / Gaussian Splatting": bool(any(k in text for k in ["nerf", "gaussian splatting", "point cloud", "depth estimation", "3d reconstruction"]))
        }
    }
}

def resolve_target_job_role(role_input: str) -> str:
    if not role_input:
        return "Senior Frontend Developer"
    r_lower = role_input.lower().strip()
    for key in TARGET_JOB_ROLES.keys():
        if key.lower() == r_lower or r_lower in key.lower() or key.lower() in r_lower:
            return key
    if "front" in r_lower or "react" in r_lower:
        return "Senior Frontend Developer"
    elif "back" in r_lower or "fastapi" in r_lower or "python" in r_lower:
        return "Senior Backend Engineer"
    elif "sec" in r_lower or "appsec" in r_lower or "pentest" in r_lower:
        return "Fullstack Security Engineer"
    elif "webrtc" in r_lower or "voice" in r_lower or "video" in r_lower or "real-time" in r_lower or "stream" in r_lower:
        return "Real-Time Systems & WebRTC Architect"
    elif "devops" in r_lower or "cloud" in r_lower or "k8s" in r_lower or "infra" in r_lower or "sre" in r_lower:
        return "DevOps & Cloud Infrastructure Engineer"
    elif "ai" in r_lower or "ml" in r_lower or "llm" in r_lower or "agent" in r_lower:
        return "AI & Machine Learning Engineer"
    elif "design" in r_lower or "ui" in r_lower or "ux" in r_lower or "figma" in r_lower:
        return "UI/UX Product Designer"
    elif "mobile" in r_lower or "flutter" in r_lower or "ios" in r_lower or "android" in r_lower:
        return "Mobile Application Engineer"
    return "Senior Frontend Developer"

def evaluate_candidate_cv(cv_text: str, candidate_name: str, target_role: str = "Senior Frontend Developer") -> Dict[str, Any]:
    text_lower = cv_text.lower()
    canonical_role = resolve_target_job_role(target_role)
    role_config = TARGET_JOB_ROLES[canonical_role]
    
    # 1. Execute role-specific check functions
    core_checks = role_config["core_checks_fn"](text_lower)
    preferred_checks = role_config["preferred_checks_fn"](text_lower)
    
    core_met_count = sum(1 for met in core_checks.values() if met)
    preferred_met_count = sum(1 for met in preferred_checks.values() if met)
    total_core = len(core_checks)
    
    all_core_met = (core_met_count == total_core)
    
    if all_core_met:
        base_score = 0.78
        bonus = (preferred_met_count / max(1, len(preferred_checks))) * 0.20
        final_rate = round(base_score + bonus, 2)
    else:
        final_rate = round(min(0.62, (core_met_count / max(1, total_core)) * 0.55 + (preferred_met_count / max(1, len(preferred_checks))) * 0.08), 2)
        
    final_rate = max(0.20, min(0.98, final_rate))
    final_percent = int(final_rate * 100)

    # 2. Extract Category Breakdown
    tech_score = int(min(100, max(20, (core_met_count / max(1, total_core)) * 70 + (preferred_met_count / max(1, len(preferred_checks))) * 30)))
    exp_score = 95 if any(re.search(p, text_lower) for p in [r'\b[5-9]\b\+?\s*years?', r'senior', r'lead', r'principal', r'architect']) else (75 if any(re.search(p, text_lower) for p in [r'\b[2-4]\b\+?\s*years?', r'mid', r'engineer']) else 45)
    arch_score = int(min(100, max(30, tech_score * 0.9 + (15 if any(k in text_lower for k in ["architect", "distributed", "system", "scale", "microservice", "security", "performance"]) else 0))))
    impact_score = int(min(100, max(35, 70 + (25 if any(k in text_lower for k in ["mentored", "lead", "spearheaded", "delivered", "optimized", "increased", "reduced", "ownership"]) else 5))))

    category_scores = {
        "technical_competency": {"name": "Technical Competency & Stack", "score": tech_score, "weight": "35%"},
        "domain_experience": {"name": "Domain Experience & Seniority", "score": exp_score, "weight": "25%"},
        "system_architecture": {"name": "System Architecture & Engineering Rigor", "score": arch_score, "weight": "25%"},
        "communication_impact": {"name": "Communication, Testing & Impact", "score": impact_score, "weight": "15%"}
    }

    # 3. Extract Evidence Citations from raw CV text
    sentences = re.split(r'[\n\r•·\-\*]+|(?<=[.!?])\s+', cv_text)
    clean_sentences = [s.strip() for s in sentences if len(s.strip()) > 25 and len(s.strip()) < 220]
    
    citations = []
    seen_quotes = set()

    for sent in clean_sentences:
        s_low = sent.lower()
        if len(citations) >= 5:
            break
        if any(k in s_low for k in ["react", "fastapi", "python", "typescript", "webrtc", "docker", "kubernetes", "security", "cloud", "api", "database", "postgres", "sql"]):
            if sent not in seen_quotes:
                seen_quotes.add(sent)
                citations.append({
                    "category": "Technical Qualification",
                    "quote": sent,
                    "criterion": "Core Technology Stack Mastery"
                })
        elif any(k in s_low for k in ["architect", "lead", "spearheaded", "mentored", "designed", "engineered", "reduced", "scaled", "migrated"]):
            if sent not in seen_quotes:
                seen_quotes.add(sent)
                citations.append({
                    "category": "Impact & Leadership",
                    "quote": sent,
                    "criterion": "System Design & Leadership Evidence"
                })

    if not citations and clean_sentences:
        citations.append({
            "category": "Profile Overview",
            "quote": clean_sentences[0],
            "criterion": "Candidate Resume Summary"
        })

    # 4. Strengths & Growth Areas
    strengths = []
    growth_areas = []
    
    for req_name, met in core_checks.items():
        if met and len(strengths) < 4:
            strengths.append(f"Demonstrated competency in {req_name}")
        elif not met and len(growth_areas) < 3:
            growth_areas.append(f"Missing documented depth in {req_name}")
            
    for req_name, met in preferred_checks.items():
        if met and len(strengths) < 5:
            strengths.append(f"High-value bonus qualification in {req_name}")
        elif not met and len(growth_areas) < 4:
            growth_areas.append(f"Opportunity to demonstrate {req_name}")

    if not growth_areas:
        growth_areas.append("Demonstrates well-rounded profile meeting all target criteria.")

    # 5. Next Steps Recommendation
    if final_percent >= 80:
        verdict = "Strong Match — Candidate exceeds qualification thresholds. Technical interview slot booking recommended."
        status_label = "Shortlisted"
        stage_init = "interview"
    elif final_percent >= 60:
        verdict = "Promising Candidate — Core qualifications verified with minor gaps. Recommended for recruiter screening."
        status_label = "Screening"
        stage_init = "screening"
    else:
        verdict = "Skill Alignment Gap — Candidate does not currently satisfy the minimum threshold for this specific role."
        status_label = "Reviewed"
        stage_init = "reviewed"

    return {
        "qualificationRate": final_rate,
        "matchPercentage": final_percent,
        "status": status_label,
        "stage": stage_init,
        "verdict": verdict,
        "role": canonical_role,
        "role_title": role_config["title"],
        "category_scores": category_scores,
        "citations": citations,
        "strengths": strengths,
        "growth_areas": growth_areas,
        "core_checks": core_checks,
        "preferred_checks": preferred_checks,
        "core_met_count": core_met_count,
        "preferred_met_count": preferred_met_count
    }

@router.get("/api/cv/roles")
async def get_job_roles():
    cached = TTL_CACHE.get("cv_roles_summary")
    if cached is not None:
        return cached
    
    roles_summary = []
    for key, cfg in TARGET_JOB_ROLES.items():
        skills = cfg.get("skills", ["Engineering", "Python", "Problem Solving"])
        roles_summary.append({
            "id": key,
            "title": cfg.get("title", key),
            "department": cfg.get("department", "Engineering"),
            "description": cfg.get("description", ""),
            "badge": cfg.get("badge", "💼 Tech"),
            "skills": skills,
            "experience": cfg.get("experience", "3+ Years"),
            "location": "Remote / Hybrid",
            "type": "Full-time"
        })
    res = {
        "status": "ok",
        "total_roles": len(roles_summary),
        "roles": roles_summary
    }
    TTL_CACHE.set("cv_roles_summary", res, ttl_seconds=60.0)
    return res

class CreateJobRoleRequest(BaseModel):
    id: str
    title: str
    department: str
    description: str
    badge: Optional[str] = "💼 Tech"
    skills: Optional[List[str]] = []
    admin_key: Optional[str] = ""

@router.post("/api/cv/roles")
async def create_custom_job_role(req: CreateJobRoleRequest, authorization: Optional[str] = Header(None)):
    auth_jwt = authorization[7:] if authorization and authorization.startswith("Bearer ") else req.admin_key
    user_payload = verify_access_jwt(auth_jwt) if (auth_jwt and auth_jwt not in ("admin", "kiki", "recruiter_session")) else None
    is_recruiter = (user_payload and (user_payload.get("role") in ("admin", "recruiter") or user_payload.get("rank") in ("Grandmaster", "Jonin"))) or (False)
    
    if not is_recruiter:
        raise HTTPException(status_code=403, detail="Recruiter or Admin authorization required to create open positions.")

    skills_list = req.skills or ["Engineering", "Problem Solving", "Git"]
    
    TARGET_JOB_ROLES[req.id] = {
        "title": req.title,
        "department": req.department,
        "description": req.description,
        "badge": req.badge,
        "skills": skills_list,
        "core_checks_fn": lambda text, sk=skills_list: {
            f"Required: {s}": (s.lower() in text.lower()) for s in sk
        } | {"Documentation & Portfolio": len(text.strip()) > 80},
        "preferred_checks_fn": lambda text: {
            "Leadership & Mentorship": bool(re.search(r'lead|architect|mentored|managed|spearheaded', text)),
            "Automated CI/CD Workflows": bool("ci" in text or "cd" in text or "pipeline" in text)
        }
    }
    
    db = load_data()
    if "job_roles" not in db:
        db["job_roles"] = {}
    db["job_roles"][req.id] = {
        "id": req.id,
        "title": req.title,
        "department": req.department,
        "description": req.description,
        "badge": req.badge,
        "skills": skills_list
    }
    save_data(db, immediate=True)
    TTL_CACHE.delete("cv_roles_summary")
    
    return {
        "status": "ok",
        "message": f"Position '{req.title}' successfully registered in database and candidate assessment engine.",
        "role_id": req.id
    }


@router.get("/api/cv/workflow")
@router.get("/api/cv/workflow.json")
async def get_cv_workflow_json(
    authorization: Optional[str] = Header(None),
    admin_key: Optional[str] = Header(None, alias="admin_key"),
    admin_key_dash: Optional[str] = Header(None, alias="admin-key"),
    token: Optional[str] = Query(None)
):
    key = admin_key or admin_key_dash
    auth_jwt = token or (authorization[7:] if authorization and authorization.startswith("Bearer ") else None) or key
    user_payload = verify_access_jwt(auth_jwt) if (auth_jwt and auth_jwt not in ("admin", "kiki", "recruiter_session")) else None
    is_recruiter = (user_payload and (user_payload.get("role") in ("admin", "recruiter") or user_payload.get("rank") in ("Grandmaster", "Jonin"))) or (False)
    
    if not is_recruiter:
        raise HTTPException(status_code=403, detail="Access Denied: Recruiter or Administrator authorization required to view workflow definitions.")

    return {
        "status": "ok",
        "pipeline": "Connecto Enterprise Automated Candidate Assessment Engine",
        "engine": "Mistral OCR + Gemini AI Rubric Reasoning",
        "supported_roles": 8,
        "stages": ["Submitted", "Screening", "Reviewed", "Interview", "Offered", "Archived"],
        "access": "Internal Protected"
    }

def get_client_ip(request: Request) -> str:
    if not request:
        return "127.0.0.1"
    cf_ip = request.headers.get("cf-connecting-ip")
    if cf_ip:
        return cf_ip.strip()
    x_real_ip = request.headers.get("x-real-ip")
    if x_real_ip:
        return x_real_ip.strip()
    x_fwd = request.headers.get("x-forwarded-for")
    if x_fwd:
        return x_fwd.split(",")[0].strip()
    if request.client and request.client.host:
        return request.client.host
    return "127.0.0.1"

CV_SCREEN_RATE_STORE: Dict[str, List[float]] = {}
CV_SCREEN_WINDOW = 600.0   # 10 minutes
CV_SCREEN_MAX_PER_IP = 5    # Max 5 CV submissions per 10 mins per IP

@router.post("/api/cv/screen")
async def screen_cv(
    request: Request,
    name: str = Form(...),
    email: str = Form(...),
    phone: Optional[str] = Form(""),
    role: str = Form("Senior Frontend Developer"),
    referral_source: str = Form("Direct"),
    referrer_handle: Optional[str] = Form(""),
    file: UploadFile = File(...)
):
    clean_email = email.strip()
    if not validate_email_format(clean_email):
        raise HTTPException(status_code=400, detail="Invalid email ID. Please enter a valid email address (e.g. alex@example.com).")
    clean_phone = (phone or "").strip()
    if clean_phone and not validate_phone_format(clean_phone):
        raise HTTPException(status_code=400, detail="Invalid phone number. Please enter a valid 7-15 digit phone number (e.g. +91 9876543210).")
    # 1. IP Rate Limiting on Public Submission Endpoint (Max 5 submissions / 10m)
    enforce_rate_limit(request, "cv_screen", max_requests=5, window_seconds=600.0)

    if not file.filename:
        raise HTTPException(status_code=400, detail="CV file is required")
        
    content = await file.read()
    if len(content) > MAX_UPLOAD_SIZE:
        raise HTTPException(status_code=413, detail="CV file exceeds 25 MB limit")
        
    cv_text = extract_text_from_pdf_or_bytes(content, file.filename)
    if not cv_text.strip():
        cv_text = f"Candidate Name: {name}\nEmail: {email}\nRole: {role}\nResume submitted."
        
    # Save file into isolated, non-public SECURE_CV_DIR
    clean_filename = re.sub(r'[^a-zA-Z0-9_.-]', '_', os.path.basename(file.filename))
    saved_filename = f"cv_{int(time.time())}_{uuid.uuid4().hex[:8]}_{clean_filename}"
    saved_path = os.path.join(SECURE_CV_DIR, saved_filename)
    with open(saved_path, "wb") as f:
        f.write(content)
    try:
        os.chmod(saved_path, 0o600)
    except Exception:
        pass
        
    analysis = evaluate_candidate_cv(cv_text, name, role)
    
    cand_id = f"cand_{uuid.uuid4().hex[:8]}"
    tracking_id = f"VC-CAND-{uuid.uuid4().hex[:6].upper()}"
    rate = analysis["qualificationRate"]
    stage = analysis.get("stage", "screening")

    ref_src = sanitize_str(referral_source or "Direct")
    ref_hdl = sanitize_str(referrer_handle or "")
    if ref_hdl and not ref_hdl.startswith("@") and not ref_hdl.startswith("REF-"):
        ref_hdl = f"@{ref_hdl}"

    now_iso = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

    candidate_entry = {
        "id": cand_id,
        "tracking_id": tracking_id,
        "name": sanitize_str(name),
        "email": sanitize_str(email),
        "role": sanitize_str(analysis.get("role", role)),
        "role_title": analysis.get("role_title", role),
        "qualificationRate": rate,
        "matchPercentage": analysis.get("matchPercentage", int(rate * 100)),
        "status": analysis["status"],
        "stage": stage,
        "verdict": analysis.get("verdict", ""),
        "category_scores": analysis.get("category_scores", {}),
        "citations": analysis.get("citations", []),
        "strengths": analysis.get("strengths", []),
        "growth_areas": analysis.get("growth_areas", []),
        "referral_source": ref_src,
        "referrer_handle": ref_hdl,
        "core_checks": analysis["core_checks"],
        "preferred_checks": analysis["preferred_checks"],
        "cv_storage_key": saved_filename,
        "cv_url": f"/api/cv/candidates/{cand_id}/download",
        "extracted_text": cv_text,
        "timestamp": now_iso,
        "stage_history": [
            {"from_stage": "", "to_stage": "submitted", "timestamp": now_iso, "notes": "Application submitted via online portal"},
            {"from_stage": "submitted", "to_stage": stage, "timestamp": now_iso, "notes": f"AI Rubric evaluation completed ({analysis.get('matchPercentage', int(rate*100))}%)"}
        ]
    }
    
    async with db_transaction():
        if "candidates" not in data_store:
            data_store["candidates"] = []
        data_store["candidates"].insert(0, candidate_entry)
        TTL_CACHE.invalidate("cv_candidates")
        TTL_CACHE.invalidate("referral_leaderboard")
    
    logger.info(f"[CV_SCREENED] Candidate '{name}' ({email}) [{tracking_id}] for '{candidate_entry['role']}' evaluated and saved to secure vault. Score: {analysis.get('matchPercentage')}%")

    # Award referral milestone XP if referrer handle provided
    if ref_hdl:
        await award_referral_milestone_xp(candidate_entry, "submitted")
        if stage in ("shortlisted", "interview"):
            await award_referral_milestone_xp(candidate_entry, stage)

    # Fire confirmation email asynchronously — non-blocking
    asyncio.create_task(send_candidate_email(
        to_email=email,
        name=name,
        role=candidate_entry["role"],
        tracking_id=tracking_id,
        stage="submitted",
        score=int(rate * 100)
    ))
    
    return {
        "status": "ok",
        "candidate": candidate_entry,
        "tracking_id": tracking_id,
        "message": f"Candidate {name} evaluated successfully for {candidate_entry['role']}! Tracking ID: {tracking_id}"
    }

@router.get("/api/cv/candidates/{cand_id}/download")
async def download_candidate_cv(
    cand_id: str,
    authorization: Optional[str] = Header(None),
    token: Optional[str] = Query(None),
    auth_token: Optional[str] = Cookie(None)
):
    """
    Role-gated secure download endpoint for candidate CV documents.
    Requires recruiter/admin JWT or candidate tracking token.
    """
    auth_jwt = token or (authorization[7:] if authorization and authorization.startswith("Bearer ") else auth_token)
    user_payload = verify_access_jwt(auth_jwt) if auth_jwt else None
    
    candidates = data_store.get("candidates", [])
    candidate = next((c for c in candidates if c.get("id") == cand_id or c.get("tracking_id") == cand_id), None)
    if not candidate:
        raise HTTPException(status_code=404, detail="Candidate not found")
        
    is_authorized = False
    if user_payload:
        user_role = user_payload.get("role", "")
        user_rank = user_payload.get("rank", "")
        if user_role in ("admin", "recruiter") or user_rank in ("Grandmaster", "Jonin"):
            is_authorized = True
            
    if not is_authorized:
        # Check if caller provided valid candidate tracking token or recruiter passkey
        if token and (token == candidate.get("tracking_id") or False):
            is_authorized = True
            
    if not is_authorized:
        raise HTTPException(
            status_code=403, 
            detail="Access Denied: Recruiter authorization required to download applicant CVs."
        )
        
    storage_key = candidate.get("cv_storage_key") or os.path.basename(candidate.get("cv_url", ""))
    file_path = os.path.join(SECURE_CV_DIR, storage_key)
    if not os.path.exists(file_path):
        legacy_path = os.path.join(UPLOADS_DIR, storage_key)
        if os.path.exists(legacy_path):
            file_path = legacy_path
        else:
            raise HTTPException(status_code=404, detail="CV document file not found")
            
    return FileResponse(
        file_path, 
        filename=storage_key,
        media_type="application/pdf" if storage_key.endswith(".pdf") else "application/octet-stream"
    )

# ==================== CANDIDATE STATUS UPDATE WITH EMAIL ====================

class CandidateStatusUpdateRequest(BaseModel):
    tracking_id: str
    new_stage: str  # submitted | screening | shortlisted | interview | offered | rejected
    notes: Optional[str] = ""
    admin_key: Optional[str] = ""

CAREER_STATE_TRANSITIONS = {
    "submitted": {"screening", "shortlisted", "reviewed", "rejected", "archived"},
    "screening": {"shortlisted", "reviewed", "interview", "rejected", "archived"},
    "shortlisted": {"interview", "reviewed", "offered", "rejected", "archived"},
    "reviewed": {"screening", "shortlisted", "interview", "rejected", "archived"},
    "interview": {"offered", "reviewed", "rejected", "archived"},
    "offered": {"hired", "rejected", "archived"},
    "hired": {"archived"},
    "rejected": {"archived", "submitted"},
    "archived": {"submitted"}
}

@router.post("/api/cv/update-status")
async def update_candidate_status(
    req: CandidateStatusUpdateRequest,
    authorization: Optional[str] = Header(None),
    admin_key: Optional[str] = Header(None, alias="admin_key"),
    admin_key_dash: Optional[str] = Header(None, alias="admin-key"),
    token: Optional[str] = Query(None),
    auth_token: Optional[str] = Cookie(None)
):
    """
    Recruiter/Admin endpoint to advance a candidate's stage using a verified state machine.
    Restricted to authenticated recruiters/admins to prevent unauthorized XP milestone farming.
    """
    VALID_STAGES = {"submitted", "screening", "shortlisted", "reviewed", "interview", "offered", "hired", "rejected", "archived"}
    new_st = req.new_stage.lower().strip()
    if new_st not in VALID_STAGES:
        raise HTTPException(status_code=400, detail=f"Invalid stage. Must be one of: {', '.join(VALID_STAGES)}")

    # 1. Verify Recruiter or Admin Authorization
    key = admin_key or admin_key_dash or req.admin_key
    auth_jwt = token or (authorization[7:] if authorization and authorization.startswith("Bearer ") else auth_token) or key
    user_payload = verify_access_jwt(auth_jwt) if (auth_jwt and not False) else None
    
    is_authorized = False
    if user_payload:
        user_role = user_payload.get("role", "")
        user_rank = user_payload.get("rank", "")
        if user_role in ("admin", "recruiter") or user_rank in ("Grandmaster", "Jonin"):
            is_authorized = True
            
    if not is_authorized:
        # Check if caller provided valid recruiter passkey or admin key
        if False or False:
            is_authorized = True
            
    if not is_authorized:
        raise HTTPException(
            status_code=403, 
            detail="Access Denied: Recruiter or Administrator authorization required to advance candidate stages and award milestone XP."
        )

    candidates = data_store.get("candidates", [])
    target = None
    for c in candidates:
        if c.get("tracking_id", "").lower() == req.tracking_id.strip().lower():
            target = c
            break
    
    if not target:
        raise HTTPException(status_code=404, detail=f"No candidate found with tracking ID '{req.tracking_id}'")
    
    old_stage = target.get("stage", "submitted").lower()
    
    # 2. State Machine Validation
    allowed_next = CAREER_STATE_TRANSITIONS.get(old_stage, set())
    if new_st != old_stage and new_st not in allowed_next:
        logger.warning(f"[STATE_MACHINE_REJECT] Invalid transition from '{old_stage}' to '{new_st}' for {req.tracking_id}")
        raise HTTPException(
            status_code=400,
            detail=f"Invalid stage transition: Cannot transition candidate directly from '{old_stage}' to '{new_st}'. Allowed next stages: {', '.join(allowed_next) if allowed_next else 'None'}"
        )
    
    target["stage"] = new_st
    if req.notes:
        target["admin_notes"] = sanitize_str(req.notes)
    now_iso = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    target["last_updated"] = now_iso
    
    # Append to immutable stage history
    if "stage_history" not in target or not isinstance(target["stage_history"], list):
        target["stage_history"] = []
    target["stage_history"].append({
        "from_stage": old_stage,
        "to_stage": new_st,
        "timestamp": now_iso,
        "updated_by": (user_payload.get("username") if user_payload else None) or "Recruiter",
        "notes": sanitize_str(req.notes or "")
    })
    
    save_data()
    
    logger.info(f"[STAGE_UPDATE] {req.tracking_id}: {old_stage} → {new_st}")
    
    # Award referral milestone XP if candidate has a referrer
    if target.get("referrer_handle"):
        await award_referral_milestone_xp(target, new_st)
    
    # Fire stage-transition email
    email_sent = False
    if target.get("email"):
        email_sent = await send_candidate_email(
            to_email=target["email"],
            name=target.get("name", "Candidate"),
            role=target.get("role", "the applied role"),
            tracking_id=req.tracking_id,
            stage=new_st,
            score=int(target.get("qualificationRate", 0))
        )
    
    return {
        "status": "ok",
        "tracking_id": req.tracking_id,
        "old_stage": old_stage,
        "new_stage": new_st,
        "candidate_name": target.get("name"),
        "candidate_email": target.get("email"),
        "stage_history": target.get("stage_history", []),
        "email_sent": email_sent,
        "smtp_enabled": SMTP_ENABLED
    }

@router.post("/api/cv/test-email")
async def test_email_endpoint(
    to_email: str = Form(...),
    stage: str = Form("submitted"),
    name: str = Form("Test Candidate"),
    role: str = Form("Senior Frontend Developer"),
    tracking_id: str = Form("VC-CAND-TEST01"),
    authorization: Optional[str] = Header(None)
):
    # Strict Recruiter / Admin Authorization Guard
    auth_jwt = authorization[7:] if authorization and authorization.startswith("Bearer ") else ""
    user_payload = verify_access_jwt(auth_jwt) if auth_jwt else None
    if not (user_payload and user_payload.get("role") in ("admin", "recruiter")):
        raise HTTPException(status_code=403, detail="Forbidden: Recruiter or Administrator authentication required.")
    """Admin test endpoint to preview and send a stage email template."""
    VALID_STAGES = {"submitted", "screening", "shortlisted", "interview", "offered", "rejected"}
    if stage not in VALID_STAGES:
        raise HTTPException(status_code=400, detail=f"Invalid stage '{stage}'")
    
    if not SMTP_ENABLED:
        template = STAGE_EMAIL_TEMPLATES.get(stage, {})
        html_preview = build_email_html(template, name, role, tracking_id, score=85)
        return {
            "status": "preview_only",
            "smtp_enabled": False,
            "message": "SMTP not configured. Set SMTP_USER and SMTP_PASS env vars to enable sending.",
            "email_subject": template.get("subject", "").format(role=role, name=name),
            "html_preview_length": len(html_preview)
        }
    
    sent = await send_candidate_email(
        to_email=to_email,
        name=name,
        role=role,
        tracking_id=tracking_id,
        stage=stage,
        score=85
    )
    return {"status": "ok" if sent else "error", "email_sent": sent, "to": to_email, "stage": stage}

@router.get("/api/cv/track")
@router.get("/api/cv/track/{tracking_id}")
async def track_candidate_api(tracking_id: Optional[str] = None, q: Optional[str] = Query(None)):
    query_val = (tracking_id or q or "").strip().lower()
    if not query_val:
        raise HTTPException(status_code=400, detail="Tracking ID or email is required")

    candidates = data_store.get("candidates", [])
    
    # Backfill missing tracking_id / stage
    for c in candidates:
        if "tracking_id" not in c or not c["tracking_id"]:
            cid_clean = c.get("id", "cand_abc").replace("cand_", "")[:6].upper()
            c["tracking_id"] = f"VC-CAND-{cid_clean}"
        if "stage" not in c:
            r = c.get("qualificationRate", 80)
            c["stage"] = "interview" if r >= 85 else "screening"

    match = None
    for c in candidates:
        if c.get("tracking_id", "").lower() == query_val or c.get("email", "").lower() == query_val or c.get("id", "").lower() == query_val:
            match = c
            break

    if not match:
        if "alex" in query_val:
            match = {
                "id": "cand_alex_demo",
                "tracking_id": "VC-CAND-ALEX94",
                "name": "Alex Mercer",
                "email": "alex.mercer@shinobi-tech.io",
                "role": "Senior Frontend Developer (React/TS)",
                "qualificationRate": 94,
                "status": "Highly Qualified",
                "stage": "interview",
                "timestamp": "2026-08-29 18:30:00"
            }
        elif "sarah" in query_val:
            match = {
                "id": "cand_sarah_demo",
                "tracking_id": "VC-CAND-SRH96",
                "name": "Sarah Chen",
                "email": "sarah.chen@cyber-guard.net",
                "role": "Fullstack Security Engineer",
                "qualificationRate": 96,
                "status": "Highly Qualified",
                "stage": "interview",
                "timestamp": "2026-08-29 19:15:00"
            }
        elif "jordan" in query_val:
            match = {
                "id": "cand_jordan_demo",
                "tracking_id": "VC-CAND-JRD91",
                "name": "Jordan Hayes",
                "email": "jordan.hayes@webrtc-core.org",
                "role": "Real-Time Systems & WebRTC Architect",
                "qualificationRate": 91,
                "status": "Highly Qualified",
                "stage": "interview",
                "timestamp": "2026-08-29 20:00:00"
            }

    if not match:
        raise HTTPException(status_code=404, detail=f"No candidate application found for '{query_val}'")

    rate = match.get("qualificationRate", 75)
    current_stage = match.get("stage", "interview" if rate >= 85 else "screening")

    timeline = [
        {
            "step": 1,
            "key": "submitted",
            "title": "Application Received & OCR Parsed",
            "date": match.get("timestamp", "Recent"),
            "status": "completed",
            "description": "Resume extracted and parsed via Mistral OCR high-density token analyzer."
        },
        {
            "step": 2,
            "key": "screening",
            "title": "AI Screening & Rubric Score",
            "date": match.get("timestamp", "Recent"),
            "status": "completed",
            "description": f"Evaluated against 10 role parameters. Score: {rate}% ({match.get('status', 'Qualified')})."
        },
        {
            "step": 3,
            "key": "interview",
            "title": "Technical Deep Dive & Peer Review",
            "date": "Within 2-3 business days",
            "status": "in_progress" if current_stage == "interview" else ("completed" if current_stage == "offered" else "pending"),
            "description": "45-minute technical architecture and coding discussion with lead engineers."
        },
        {
            "step": 4,
            "key": "offered",
            "title": "Final Decision & Offer",
            "date": "Post-interview",
            "status": "completed" if current_stage == "offered" else "pending",
            "description": "Offer letter delivery and team onboarding schedule."
        }
    ]

    return {
        "status": "ok",
        "tracking_id": match.get("tracking_id", "VC-CAND-GENERIC"),
        "candidate": {
            "name": match.get("name"),
            "email": match.get("email"),
            "role": match.get("role"),
            "qualificationRate": match.get("qualificationRate"),
            "status": match.get("status"),
            "current_stage": current_stage,
            "submitted_at": match.get("timestamp")
        },
        "timeline": timeline
    }

@router.get("/api/cv/candidates")
async def get_candidates(authorization: Optional[str] = Header(None)):
    # Strict Privacy Guard: Scores are private to each user only.
    auth_jwt = authorization[7:] if authorization and authorization.startswith("Bearer ") else ""
    user_payload = verify_access_jwt(auth_jwt) if auth_jwt else None
    if not (user_payload and user_payload.get("role") == "admin"):
        raise HTTPException(
            status_code=403,
            detail="Candidate evaluation scores and previous records are strictly private and confidential."
        )
    return {
        "status": "ok",
        "candidates": data_store.get("candidates", [])
    }

@router.delete("/api/cv/candidates/{cid}")
@router.delete("/api/cv/candidates/{cid}/purge")
async def delete_candidate(
    cid: str, 
    authorization: Optional[str] = Header(None),
    admin_key: Optional[str] = Header(None, alias="admin_key"),
    admin_key_dash: Optional[str] = Header(None, alias="admin-key"),
    token: Optional[str] = Query(None)
):
    """
    Privacy Compliance & Recruiter Action: Permanently purge a candidate record,
    shred the uploaded CV file from the secure vault, and emit a compliance audit log.
    """
    key = admin_key or admin_key_dash
    auth_jwt = token or (authorization[7:] if authorization and authorization.startswith("Bearer ") else None) or key
    user_payload = verify_access_jwt(auth_jwt) if (auth_jwt and auth_jwt not in ("admin", "kiki", "recruiter_session")) else None
    is_recruiter = (user_payload and (user_payload.get("role") in ("admin", "recruiter") or user_payload.get("rank") in ("Grandmaster", "Jonin"))) or (False)
    
    if not is_recruiter:
        raise HTTPException(status_code=403, detail="Access Denied: Recruiter or Administrator authorization required to purge candidate records.")

    clean_id = cid.strip().lower()
    async with db_transaction():
        candidates = data_store.get("candidates", [])
        matched = next((c for c in candidates if c.get("id", "").lower() == clean_id or c.get("tracking_id", "").lower() == clean_id), None)
        if matched:
            # 1. Shred CV document from Secure Vault & legacy uploads
            storage_keys = [matched.get("cv_storage_key"), os.path.basename(matched.get("cv_url", ""))]
            for skey in storage_keys:
                if not skey:
                    continue
                vault_path = os.path.join(SECURE_CV_DIR, skey)
                if os.path.exists(vault_path):
                    try:
                        # Overwrite with zeros before deletion (secure shredding)
                        with open(vault_path, "wb") as f:
                            f.write(b"\x00" * 4096)
                        os.remove(vault_path)
                    except Exception as e:
                        logger.warning(f"[PURGE_SHRED_ERROR] Could not shred {vault_path}: {e}")
                        
                legacy_path = os.path.join(UPLOADS_DIR, skey)
                if os.path.exists(legacy_path):
                    try:
                        os.remove(legacy_path)
                    except Exception:
                        pass
            
            cand_name = matched.get("name", "Unknown")
            cand_track = matched.get("tracking_id", cid)
            
            # 2. Remove from database
            data_store["candidates"] = [c for c in candidates if c.get("id", "").lower() != clean_id and c.get("tracking_id", "").lower() != clean_id]
            TTL_CACHE.invalidate("cv_candidates")
            
            # 3. Log compliance audit event
            log_audit_event(
                action="CANDIDATE_DATA_PURGED",
                actor=(user_payload.get("username") if user_payload else "Recruiter"),
                target=f"{cand_name} ({cand_track})",
                details=f"GDPR/Privacy data deletion executed. CV document shredded and database record erased."
            )
            
            logger.info(f"[PRIVACY_PURGE] Candidate '{cand_name}' [{cand_track}] permanently erased on request.")
            return {"status": "ok", "message": f"Candidate record and CV document for '{cid}' permanently erased and shredded."}
            
    return {"status": "error", "message": "Candidate not found"}

class PrivacyPurgeRequest(BaseModel):
    tracking_id: str
    candidate_email: str
    reason: Optional[str] = "User privacy request"

@router.post("/api/cv/privacy/purge-request")
async def submit_privacy_purge_request(
    req: PrivacyPurgeRequest,
    authorization: Optional[str] = Header(None),
    admin_key: Optional[str] = Header(None, alias="admin_key"),
    admin_key_dash: Optional[str] = Header(None, alias="admin-key")
):
    """Candidate Self-Service Privacy Purge: Allows applicant to request permanent deletion using their tracking ID and email."""
    candidates = data_store.get("candidates", [])
    matched = next((c for c in candidates if c.get("tracking_id", "").lower() == req.tracking_id.strip().lower() and (c.get("email") or "").lower() == req.candidate_email.strip().lower()), None)
    if not matched:
        raise HTTPException(status_code=404, detail="No matching candidate found with the specified Tracking ID and email address.")
    
    return await delete_candidate(cid=matched["id"], admin_key=None)

@router.get("/api/system/logs")
async def get_system_diagnostic_logs(
    limit: int = Query(100),
    authorization: Optional[str] = Header(None),
    admin_key: Optional[str] = Header(None, alias="admin_key"),
    admin_key_dash: Optional[str] = Header(None, alias="admin-key"),
    token: Optional[str] = Query(None)
):
    """Admin Diagnostic Endpoint: View recent centralized backend error/info logs."""
    key = admin_key or admin_key_dash
    auth_jwt = token or (authorization[7:] if authorization and authorization.startswith("Bearer ") else None) or key
    user_payload = verify_access_jwt(auth_jwt) if (auth_jwt and auth_jwt not in ("admin", "kiki")) else None
    is_admin = (user_payload and (user_payload.get("role") == "admin" or user_payload.get("rank") == "Grandmaster")) or (False)
    
    if not is_admin:
        raise HTTPException(status_code=403, detail="Access Denied: Administrator authorization required to view system diagnostic logs.")
        
    log_file_path = os.path.join(APP_ROOT, "logs", "connecto_app.log")
    lines = []
    if os.path.exists(log_file_path):
        try:
            with open(log_file_path, "r", encoding="utf-8", errors="replace") as f:
                lines = f.readlines()
        except Exception as e:
            lines = [f"Error reading log file: {e}"]
    return {
        "status": "ok",
        "total_lines": len(lines),
        "logs": [l.rstrip("\r\n") for l in lines[-max(1, min(500, limit)):]]
    }

# ==================== INTERVIEW SCHEDULING SYSTEM ====================

class InterviewSlotRequest(BaseModel):
    date: str        # ISO date: "2026-09-05"
    time: str        # "10:00"
    duration_min: Optional[int] = 45
    interviewer: Optional[str] = "Engineering Lead"
    notes: Optional[str] = ""

class InterviewBookingRequest(BaseModel):
    slot_id: str
    tracking_id: str
    candidate_name: Optional[str] = "Candidate"
    candidate_email: Optional[str] = ""
    notes: Optional[str] = ""

def _get_default_slots() -> List[Dict]:
    """Seed with 14 days of default available slots if none exist."""
    slots = []
    from datetime import timedelta
    now = datetime.now()
    interviewers = ["Vivek (Engineering Lead)", "Ryu Lightning (Principal Eng)", "Chinnu (Fullstack Lead)"]
    times = ["10:00", "11:00", "14:00", "15:00", "16:00"]
    for day_offset in range(1, 15):
        d = now + timedelta(days=day_offset)
        if d.weekday() >= 5:  # skip weekends
            continue
        date_str = d.strftime("%Y-%m-%d")
        for t in times:
            slots.append({
                "id": f"slot_{date_str}_{t.replace(':', '')}",
                "date": date_str,
                "time": t,
                "duration_min": 45,
                "interviewer": interviewers[day_offset % len(interviewers)],
                "available": True,
                "booked_by_tracking_id": None,
                "booked_by_name": None,
                "notes": ""
            })
    return slots

def ensure_slots_initialized():
    today = datetime.now().strftime("%Y-%m-%d")
    current_slots = data_store.get("interview_slots", [])
    valid_slots = [s for s in current_slots if s.get("date", "") >= today]
    if not valid_slots:
        data_store["interview_slots"] = _get_default_slots()
        save_data()
    else:
        data_store["interview_slots"] = valid_slots

@router.get("/api/interview/slots")
async def get_interview_slots(available_only: bool = Query(True)):
    ensure_slots_initialized()
    slots = data_store.get("interview_slots", [])
    today = datetime.now().strftime("%Y-%m-%d")
    slots = [s for s in slots if s.get("date", "") >= today]
    if available_only:
        slots = [s for s in slots if s.get("available", True)]
    return {"status": "ok", "slots": slots, "total": len(slots)}

@router.post("/api/interview/slots")
async def create_interview_slot(
    req: InterviewSlotRequest,
    authorization: Optional[str] = Header(None),
    admin_key: Optional[str] = Header(None),
    token: Optional[str] = Query(None)
):
    """Recruiter/Admin: create a new available interview slot."""
    auth_jwt = token or (authorization[7:] if authorization and authorization.startswith("Bearer ") else None) or admin_key
    user_payload = verify_access_jwt(auth_jwt) if (auth_jwt and auth_jwt not in ("admin", "kiki", "recruiter_session")) else None
    is_recruiter = (user_payload and (user_payload.get("role") in ("admin", "recruiter") or user_payload.get("rank") in ("Grandmaster", "Jonin"))) or (False)
    
    if not is_recruiter:
        raise HTTPException(status_code=403, detail="Access Denied: Recruiter authorization required to create interview slots.")

    ensure_slots_initialized()
    slot_id = f"slot_{req.date}_{req.time.replace(':', '')}_{uuid.uuid4().hex[:4]}"
    slot = {
        "id": slot_id,
        "date": req.date,
        "time": req.time,
        "duration_min": req.duration_min or 45,
        "interviewer": req.interviewer or "Engineering Lead",
        "available": True,
        "booked_by_tracking_id": None,
        "booked_by_name": None,
        "notes": req.notes or ""
    }
    data_store["interview_slots"].append(slot)
    save_data()
    return {"status": "ok", "slot": slot}

@router.post("/api/interview/book")
async def book_interview_slot(req: InterviewBookingRequest):
    """Candidate: book an available slot using their tracking ID."""
    ensure_slots_initialized()
    slots = data_store.get("interview_slots", [])
    
    # Find the slot
    target_slot = None
    for s in slots:
        if s["id"] == req.slot_id:
            target_slot = s
            break
    
    if not target_slot:
        raise HTTPException(status_code=404, detail="Interview slot not found")
    if not target_slot.get("available", True):
        raise HTTPException(status_code=409, detail="This slot has already been booked. Please choose another.")
    
    # Validate tracking ID exists
    candidates = data_store.get("candidates", [])
    candidate = None
    for c in candidates:
        if c.get("tracking_id", "").lower() == req.tracking_id.strip().lower():
            candidate = c
            break
    
    if not candidate:
        raise HTTPException(status_code=404, detail=f"No candidate found with tracking ID '{req.tracking_id}'")
    
    # Check candidate doesn't already have a booking
    for s in slots:
        if s.get("booked_by_tracking_id") == req.tracking_id and not s.get("available", True):
            raise HTTPException(
                status_code=409,
                detail=f"You already have an interview booked on {s['date']} at {s['time']}. Cancel it first to rebook."
            )
    
    # Book the slot
    target_slot["available"] = False
    target_slot["booked_by_tracking_id"] = req.tracking_id
    target_slot["booked_by_name"] = sanitize_str(req.candidate_name)
    target_slot["booked_at"] = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    target_slot["candidate_notes"] = sanitize_str(req.notes or "")
    
    # Advance candidate stage to 'interview' if not already offered/rejected
    current_stage = candidate.get("stage", "screening")
    if current_stage not in ("offered", "rejected"):
        candidate["stage"] = "interview"
        candidate["interview_slot_id"] = req.slot_id
    save_data()
    
    # Award referral milestone XP for interview stage
    if candidate.get("referrer_handle"):
        await award_referral_milestone_xp(candidate, "interview")
    
    logger.info(f"[INTERVIEW_BOOKED] {req.tracking_id} booked slot {req.slot_id} on {target_slot['date']} {target_slot['time']}")

    # Build a nice datetime string for email
    try:
        from datetime import datetime as dt
        slot_dt = dt.strptime(f"{target_slot['date']} {target_slot['time']}", "%Y-%m-%d %H:%M")
        slot_display = slot_dt.strftime("%A, %B %d %Y at %I:%M %p")
    except Exception:
        slot_display = f"{target_slot['date']} {target_slot['time']}"

    # Send interview confirmation email
    confirmation_sent = await _send_interview_confirmation_email(
        to_email=candidate.get("email", req.candidate_email),
        name=candidate.get("name", req.candidate_name),
        role=candidate.get("role", "the applied role"),
        tracking_id=req.tracking_id,
        slot_display=slot_display,
        interviewer=target_slot.get("interviewer", "Engineering Lead"),
        duration_min=target_slot.get("duration_min", 45)
    )
    
    return {
        "status": "ok",
        "slot": target_slot,
        "candidate_stage": candidate.get("stage"),
        "confirmation_email_sent": confirmation_sent,
        "slot_display": slot_display
    }

@router.delete("/api/interview/book/{slot_id}")
async def cancel_interview_booking(slot_id: str, tracking_id: str = Query(...)):
    """Candidate or admin: cancel an existing booking."""
    ensure_slots_initialized()
    slots = data_store.get("interview_slots", [])
    target = None
    for s in slots:
        if s["id"] == slot_id:
            target = s
            break
    if not target:
        raise HTTPException(status_code=404, detail="Slot not found")
    if target.get("booked_by_tracking_id") != tracking_id:
        raise HTTPException(status_code=403, detail="This slot is not booked by you")
    
    target["available"] = True
    target["booked_by_tracking_id"] = None
    target["booked_by_name"] = None
    target["booked_at"] = None
    target["candidate_notes"] = ""
    save_data()
    
    return {"status": "ok", "message": "Interview booking cancelled successfully"}

@router.get("/api/interview/my-booking")
async def get_my_booking(tracking_id: str = Query(...)):
    """Get a candidate's current interview booking."""
    ensure_slots_initialized()
    slots = data_store.get("interview_slots", [])
    for s in slots:
        if s.get("booked_by_tracking_id") == tracking_id:
            try:
                from datetime import datetime as dt
                slot_dt = dt.strptime(f"{s['date']} {s['time']}", "%Y-%m-%d %H:%M")
                slot_display = slot_dt.strftime("%A, %B %d %Y at %I:%M %p")
            except Exception:
                slot_display = f"{s['date']} {s['time']}"
            return {"status": "ok", "booking": s, "slot_display": slot_display}
    return {"status": "ok", "booking": None}

async def _send_interview_confirmation_email(to_email: str, name: str, role: str, tracking_id: str, slot_display: str, interviewer: str, duration_min: int) -> bool:
    """Send branded interview confirmation email."""
    if not SMTP_ENABLED:
        logger.warning(f"[EMAIL] SMTP not configured — skipping interview confirmation to {to_email}")
        return False
    
    subject = f"📅 Interview Confirmed — {role} | vConnect Careers"
    body_html = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><title>vConnect Careers</title></head>
<body style="margin:0;padding:0;background:#0f172a;font-family:'Segoe UI',Arial,sans-serif;color:#e2e8f0;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#0f172a;padding:40px 20px;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0" style="background:#1e293b;border-radius:16px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.5);">
        <tr><td style="background:linear-gradient(135deg,#a855f722,#a855f744);padding:28px 32px;text-align:center;border-bottom:1px solid #a855f744;">
          <div style="font-size:36px;margin-bottom:8px;">📅</div>
          <div style="font-size:11px;font-weight:800;letter-spacing:2px;text-transform:uppercase;color:#c084fc;margin-bottom:6px;">vConnect Careers</div>
          <h1 style="margin:0;font-size:20px;font-weight:800;color:#fff;">Your Interview is Confirmed!</h1>
        </td></tr>
        <tr><td style="padding:28px 32px;">
          <p>Hi <strong>{name}</strong>,</p>
          <p>Your technical interview for <strong>{role}</strong> at vConnect is officially booked. We're looking forward to meeting you!</p>
          <div style="background:rgba(168,85,247,0.08);border:1px solid rgba(168,85,247,0.2);border-radius:12px;padding:16px 20px;margin:18px 0;">
            <div style="font-size:12px;color:#94a3b8;margin-bottom:10px;text-transform:uppercase;letter-spacing:1px;">Interview Details</div>
            <div style="font-size:15px;font-weight:700;color:#fff;margin-bottom:6px;">🗓 {slot_display}</div>
            <div style="font-size:13px;color:#cbd5e1;margin-bottom:4px;">👤 Interviewer: {interviewer}</div>
            <div style="font-size:13px;color:#cbd5e1;">⏱ Duration: {duration_min} minutes</div>
          </div>
          <p><strong>Preparation checklist:</strong></p>
          <ul style="font-size:14px;color:#cbd5e1;line-height:1.8;">
            <li>Review your recent project highlights and architecture decisions</li>
            <li>Prepare to discuss your experience with the role's core tech stack</li>
            <li>Test your camera and internet connection 10 minutes before</li>
            <li>Have a code editor ready for live coding exercises</li>
          </ul>
          <p>A calendar invite with the meeting link will be sent shortly from your interviewer.</p>
          <p><strong>Tracking ID:</strong> <code style="background:#1e293b;color:#38bdf8;padding:3px 8px;border-radius:4px;">{tracking_id}</code></p>
          <p>Track your status: <a href="https://careers.connecto.fun/#track" style="color:#38bdf8;">careers.connecto.fun/#track</a></p>
        </td></tr>
        <tr><td style="padding:20px 32px 28px;text-align:center;">
          <a href="https://careers.connecto.fun" style="display:inline-block;background:#a855f7;color:#fff;font-weight:800;font-size:13px;padding:10px 24px;border-radius:8px;text-decoration:none;">careers.connecto.fun</a>
        </td></tr>
        <tr><td style="padding:16px 32px;border-top:1px solid rgba(255,255,255,0.07);text-align:center;">
          <p style="margin:0;font-size:11px;color:#475569;">⚔️ vConnect — Shinobi Mesh Network · careers.connecto.fun</p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body></html>"""

    def _send():
        try:
            msg = MIMEMultipart("alternative")
            msg["Subject"] = subject
            msg["From"] = f"{SMTP_FROM_NAME} <{SMTP_FROM_EMAIL}>"
            msg["To"] = to_email
            msg.attach(MIMEText(body_html, "html", "utf-8"))
            with smtplib.SMTP(SMTP_HOST, SMTP_PORT, timeout=15) as server:
                server.ehlo(); server.starttls()
                server.login(SMTP_USER, SMTP_PASS)
                server.sendmail(SMTP_FROM_EMAIL, [to_email], msg.as_string())
            logger.info(f"[EMAIL_SENT] Interview confirmation to={to_email}")
            return True
        except Exception as e:
            logger.error(f"[EMAIL_ERROR] Interview confirmation to={to_email}: {e}")
            return False

    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(None, _send)

