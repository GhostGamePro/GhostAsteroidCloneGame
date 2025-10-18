import asyncio
import re
from typing import Dict, Optional, List
from cachetools import TTLCache
import httpx
from .config import settings

SUSPICIOUS_TLDS = {"xyz", "top", "icu", "click", "work", "gq", "cf", "ga", "ml", "tk"}

_cache: TTLCache[str, Dict] = TTLCache(maxsize=1024, ttl=settings.cache_ttl)


def _domain_from_url(url: str) -> Optional[str]:
    try:
        host = re.sub(r"^https?://", "", url, flags=re.IGNORECASE)
        host = host.split("/")[0]
        return host.lower()
    except Exception:
        return None


def local_reputation(domain: str) -> Dict:
    reasons: List[str] = []
    score = 0

    # Short domains (<=5) sometimes suspicious
    if len(domain.split(".")[0]) <= 5:
        score -= 1
        reasons.append(f"Short domain: {domain}")

    # Many hyphens
    hyphens = domain.count("-")
    if hyphens >= 2:
        score -= 1
        reasons.append(f"Many hyphens: {domain}")

    # Rare or suspicious TLDs
    tld = domain.split(".")[-1]
    if tld in SUSPICIOUS_TLDS:
        score -= 2
        reasons.append(f"Suspicious TLD: .{tld}")

    return {"domain": domain, "score": score, "reasons": reasons}


async def virustotal_domain(domain: str) -> Optional[Dict]:
    api_key = settings.virustotal_api_key
    if not api_key:
        return None
    cached = _cache.get(f"vt:{domain}")
    if cached is not None:
        return cached
    url = f"https://www.virustotal.com/api/v3/domains/{domain}"
    headers = {"x-apikey": api_key}
    async with httpx.AsyncClient(timeout=6.0) as client:
        try:
            resp = await client.get(url, headers=headers)
            if resp.status_code == 200:
                data = resp.json()
                stats = data.get("data", {}).get("attributes", {}).get("last_analysis_stats", {})
                malicious = stats.get("malicious", 0)
                suspicious = stats.get("suspicious", 0)
                result = {"stats": stats, "malicious": malicious, "suspicious": suspicious}
                _cache[f"vt:{domain}"] = result
                return result
        except Exception:
            return None
    return None


async def evaluate_urls(urls: List[str]) -> Dict[str, Dict]:
    results: Dict[str, Dict] = {}
    tasks: List[asyncio.Future] = []
    urls_with_domain: List[str] = []

    for url in urls:
        domain = _domain_from_url(url)
        if not domain:
            continue
        urls_with_domain.append(url)
        results[url] = {"domain": domain, "local": local_reputation(domain)}
        if settings.virustotal_api_key:
            tasks.append(virustotal_domain(domain))
        else:
            tasks.append(asyncio.sleep(0))

    vt_results = await asyncio.gather(*tasks, return_exceptions=True)
    for url, vt in zip(urls_with_domain, vt_results):
        domain = results.get(url, {}).get("domain")
        if domain is None:
            continue
        if isinstance(vt, dict):
            results[url]["virustotal"] = vt
    return results
