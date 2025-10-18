from pydantic import BaseModel
import os
from dotenv import load_dotenv

load_dotenv()

class Settings(BaseModel):
    ingest_key: str = os.getenv("INGEST_KEY", "change_this_token")
    virustotal_api_key: str | None = os.getenv("VIRUSTOTAL_API_KEY")
    siem_webhook_url: str | None = os.getenv("SIEM_WEBHOOK_URL")
    cache_ttl: int = int(os.getenv("CACHE_TTL", "300"))

settings = Settings()
