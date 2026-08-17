"""
Kinetic AI Trainer — Python Backend Server
Uses raw HTTP to Groq API with retry, caching, and rate limiting.

Setup:
  pip install fastapi uvicorn requests pydantic cachetools

Run:
  uvicorn server:app --host 0.0.0.0 --port 8000

Environment variables:
  PROVIDER=groq|ollama
  GROQ_API_KEY=gsk_...
  MODEL=qwen/qwen3.6-27b
  API_KEY=your-secret-api-key (for client auth)
  MAX_MESSAGE_LENGTH=4000
  CACHE_TTL_SECONDS=3600
  CACHE_MAX_SIZE=256
"""

import os
import time
import hashlib
import logging
from functools import lru_cache
from typing import List, Optional

import requests
from cachetools import TTLCache
from fastapi import FastAPI, Depends, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, field_validator

# =============================================
# CONFIG
# =============================================
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
MODEL = os.getenv("MODEL", "qwen/qwen3.6-27b")
OLLAMA_HOST = os.getenv("OLLAMA_HOST", "http://localhost:11434")
PROVIDER = os.getenv("PROVIDER", "groq")
API_KEY = os.getenv("API_KEY", "")
MAX_MESSAGE_LENGTH = int(os.getenv("MAX_MESSAGE_LENGTH", "4000"))
MAX_HISTORY_MESSAGES = 20
GROQ_TIMEOUT = 15
OLLAMA_TIMEOUT = 30
MAX_RETRIES = 3
CACHE_TTL_SECONDS = int(os.getenv("CACHE_TTL_SECONDS", "3600"))
CACHE_MAX_SIZE = int(os.getenv("CACHE_MAX_SIZE", "256"))

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("kinetic-ai")

# =============================================
# APP
# =============================================
app = FastAPI(title="Kinetic AI Trainer", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "https://kinetic-backend-3ff6.onrender.com",
        "http://localhost:8000",
    ],
    allow_methods=["POST", "GET"],
    allow_headers=["Content-Type", "Authorization", "X-API-Key"],
)

# =============================================
# CACHING
# =============================================
response_cache = TTLCache(maxsize=CACHE_MAX_SIZE, ttl=CACHE_TTL_SECONDS)

# =============================================
# CIRCUIT BREAKER
# =============================================
class CircuitBreaker:
    def __init__(self, failure_threshold=5, reset_timeout=60):
        self.failure_threshold = failure_threshold
        self.reset_timeout = reset_timeout
        self.failure_count = 0
        self.last_failure_time = 0
        self.state = "closed"  # closed = normal, open = failing

    def record_failure(self):
        self.failure_count += 1
        self.last_failure_time = time.time()
        if self.failure_count >= self.failure_threshold:
            self.state = "open"
            logger.warning(f"Circuit breaker OPEN after {self.failure_count} failures")

    def record_success(self):
        self.failure_count = 0
        self.state = "closed"

    def can_execute(self) -> bool:
        if self.state == "closed":
            return True
        if time.time() - self.last_failure_time > self.reset_timeout:
            self.state = "half-open"
            return True
        return False

circuit_breaker = CircuitBreaker()

# =============================================
# MODELS
# =============================================
class ChatMessage(BaseModel):
    role: str = Field(..., min_length=1, max_length=20)
    content: str = Field(..., min_length=1, max_length=MAX_MESSAGE_LENGTH)

class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=MAX_MESSAGE_LENGTH)
    system_prompt: str = Field("", max_length=MAX_MESSAGE_LENGTH)
    history: List[ChatMessage] = []

    @field_validator("message")
    @classmethod
    def strip_message(cls, v: str) -> str:
        return v.strip()

    @field_validator("system_prompt")
    @classmethod
    def strip_system_prompt(cls, v: str) -> str:
        return v.strip()

class ChatResponse(BaseModel):
    reply: str
    cached: bool = False
    provider: str = ""

class HealthResponse(BaseModel):
    status: str
    provider: str
    model: str
    cache_size: int
    circuit_state: str

# =============================================
# AUTH DEPENDENCY
# =============================================
async def verify_api_key(request: Request):
    if not API_KEY:
        return

    api_key = request.headers.get("X-API-Key", "")
    auth_header = request.headers.get("Authorization", "")

    if api_key == API_KEY:
        return

    if auth_header.startswith("Bearer "):
        token = auth_header.split("Bearer ")[1]
        if token == API_KEY:
            return

    raise HTTPException(
        status_code=401,
        detail="Invalid or missing API key"
    )

# =============================================
# RETRY LOGIC
# =============================================
def request_with_retry(method: str, url: str, max_retries: int = MAX_RETRIES, **kwargs) -> requests.Response:
    last_exception = None
    for attempt in range(max_retries):
        try:
            resp = requests.request(method, url, **kwargs)
            if resp.status_code == 429:
                retry_after = int(resp.headers.get("Retry-After", 2 ** attempt))
                logger.warning(f"Rate limited, retrying after {retry_after}s")
                time.sleep(retry_after)
                continue
            return resp
        except requests.exceptions.Timeout as e:
            last_exception = e
            wait = 2 ** attempt
            logger.warning(f"Timeout on attempt {attempt + 1}/{max_retries}, retrying in {wait}s")
            time.sleep(wait)
        except requests.exceptions.ConnectionError as e:
            last_exception = e
            wait = 2 ** attempt
            logger.warning(f"Connection error on attempt {attempt + 1}/{max_retries}, retrying in {wait}s")
            time.sleep(wait)

    if last_exception:
        raise last_exception
    raise Exception("Max retries exceeded")

# =============================================
# CACHE KEY GENERATION
# =============================================
def make_cache_key(message: str, system_prompt: str, history: List[ChatMessage]) -> str:
    content = f"{system_prompt}||{message}"
    for msg in history[-5:]:
        content += f"||{msg.role}:{msg.content}"
    return hashlib.sha256(content.encode()).hexdigest()[:32]

# =============================================
# PROVIDER: GROQ
# =============================================
def chat_groq(message: str, system_prompt: str, history: List[ChatMessage]) -> str:
    if not GROQ_API_KEY:
        return "Error: GROQ_API_KEY not configured"

    messages = []
    if system_prompt:
        messages.append({"role": "system", "content": system_prompt})

    recent = history[-MAX_HISTORY_MESSAGES:]
    for msg in recent:
        role = "assistant" if msg.role in ("ai", "assistant", "model") else "user"
        messages.append({"role": role, "content": msg.content})
    messages.append({"role": "user", "content": message})

    payload = {
        "model": MODEL,
        "messages": messages,
        "max_tokens": 512,
        "temperature": 0.7,
    }
    # Qwen 3.6 27B has reasoning enabled by default (returns <think> blocks).
    # Disable it so users only see the clean final answer.
    if "qwen" in MODEL.lower():
        payload["reasoning_effort"] = "none"

    resp = request_with_retry(
        "POST",
        "https://api.groq.com/openai/v1/chat/completions",
        max_retries=MAX_RETRIES,
        headers={
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type": "application/json",
        },
        json=payload,
        timeout=GROQ_TIMEOUT,
    )
    resp.raise_for_status()
    return resp.json()["choices"][0]["message"]["content"]

# =============================================
# PROVIDER: OLLAMA
# =============================================
def chat_ollama(message: str, system_prompt: str, history: List[ChatMessage]) -> str:
    messages = []
    if system_prompt:
        messages.append({"role": "system", "content": system_prompt})

    recent = history[-MAX_HISTORY_MESSAGES:]
    for msg in recent:
        messages.append({"role": msg.role, "content": msg.content})
    messages.append({"role": "user", "content": message})

    resp = request_with_retry(
        "POST",
        f"{OLLAMA_HOST}/api/chat",
        max_retries=MAX_RETRIES,
        json={"model": MODEL, "messages": messages, "stream": False},
        timeout=OLLAMA_TIMEOUT,
    )
    resp.raise_for_status()
    return resp.json()["message"]["content"]

# =============================================
# ROUTES
# =============================================
@app.get("/health", response_model=HealthResponse)
def health():
    return HealthResponse(
        status="ok",
        provider=PROVIDER,
        model=MODEL,
        cache_size=len(response_cache),
        circuit_state=circuit_breaker.state,
    )

@app.post("/chat", response_model=ChatResponse)
async def chat(req: ChatRequest, _auth: Optional[str] = Depends(verify_api_key)):
    cache_key = make_cache_key(req.message, req.system_prompt, req.history)

    if cache_key in response_cache:
        logger.info(f"Cache HIT for key={cache_key[:8]}...")
        return ChatResponse(
            reply=response_cache[cache_key],
            cached=True,
            provider=PROVIDER,
        )

    if not circuit_breaker.can_execute():
        raise HTTPException(
            status_code=503,
            detail="Service temporarily unavailable. Circuit breaker is open."
        )

    try:
        if PROVIDER == "groq":
            reply = chat_groq(req.message, req.system_prompt, req.history)
        else:
            reply = chat_ollama(req.message, req.system_prompt, req.history)

        circuit_breaker.record_success()
        response_cache[cache_key] = reply
        logger.info(f"Chat OK provider={PROVIDER} len={len(reply)}")

        return ChatResponse(reply=reply, cached=False, provider=PROVIDER)

    except requests.exceptions.Timeout:
        circuit_breaker.record_failure()
        logger.error("Provider timeout")
        raise HTTPException(status_code=504, detail="AI provider timed out. Please try again.")

    except requests.exceptions.ConnectionError:
        circuit_breaker.record_failure()
        logger.error("Provider connection failed")
        raise HTTPException(status_code=502, detail="Cannot connect to AI provider.")

    except requests.exceptions.HTTPError as e:
        circuit_breaker.record_failure()
        status = e.response.status_code if e.response is not None else 0
        if status == 429:
            raise HTTPException(status_code=429, detail="AI provider rate limited. Please wait.")
        logger.error(f"Provider HTTP error: {status}")
        raise HTTPException(status_code=502, detail=f"AI provider error (HTTP {status}).")

    except Exception as e:
        circuit_breaker.record_failure()
        logger.error(f"Unexpected error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error.")
