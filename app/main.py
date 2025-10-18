import os
from fastapi import FastAPI, UploadFile, File, Header, HTTPException
from fastapi.responses import JSONResponse
import httpx
from .config import settings
from .analyzer import analyze_bytes

app = FastAPI(title="Phish Analyzer", version="0.1.0")


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.post("/ingest")
async def ingest(email_file: UploadFile = File(...), ingest_key: str | None = Header(default=None, convert_underscores=False)):
    if ingest_key != settings.ingest_key:
        raise HTTPException(status_code=401, detail="Invalid ingest key")
    raw = await email_file.read()
    result = await analyze_bytes(raw)
    await _maybe_send_to_siem(result)
    return JSONResponse(result)


@app.post("/analyze_raw")
async def analyze_raw(email_raw: UploadFile = File(...)):
    raw = await email_raw.read()
    result = await analyze_bytes(raw)
    await _maybe_send_to_siem(result)
    return JSONResponse(result)


async def _maybe_send_to_siem(payload: dict):
    if not settings.siem_webhook_url:
        return
    async with httpx.AsyncClient(timeout=5.0) as client:
        try:
            await client.post(settings.siem_webhook_url, json=payload)
        except Exception:
            pass
