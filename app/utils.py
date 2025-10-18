import asyncio
import email
from email import policy
from email.parser import BytesParser
from email.utils import parseaddr
from typing import List, Optional, Dict, Any
import re
import socket

PRIVATE_IP_PATTERNS = [
    re.compile(r"^10\.") ,
    re.compile(r"^192\.168\.") ,
    re.compile(r"^172\.(1[6-9]|2[0-9]|3[0-1])\.") ,
    re.compile(r"^127\.") ,
]

URL_REGEX = re.compile(r"https?://[\w.-]+(?:/[\w\-./?%&=+#]*)?", re.IGNORECASE)
IP_REGEX = re.compile(r"(?:\d{1,3}\.){3}\d{1,3}")


def parse_email(raw_bytes: bytes) -> email.message.EmailMessage:
    return BytesParser(policy=policy.default).parsebytes(raw_bytes)


def extract_authentication_results(msg: email.message.EmailMessage) -> Dict[str, Any]:
    headers = {k.lower(): v for (k, v) in msg.items()}
    result = {
        "spf": {},
        "dkim": {},
        "dmarc": {},
        "from": headers.get("from"),
        "return-path": headers.get("return-path"),
        "authentication-results": headers.get("authentication-results"),
    }

    auth = headers.get("authentication-results") or ""
    # Parse simple tokens like spf=pass dkim=pass dmarc=pass
    for mech in ("spf", "dkim", "dmarc"):
        m = re.search(rf"{mech}\s*=\s*(pass|fail|none|neutral|softfail|temperror|permerror)", auth, re.IGNORECASE)
        if m:
            (res,) = m.groups()
            result[mech] = {"result": res.lower()}

    # DMARC record existence heuristic from header
    dmarc_policy = None
    ar_dmarc = re.search(r"dmarc=([a-z]+)\s*\(([^)]*)\)", auth, re.IGNORECASE)
    if ar_dmarc:
        dmarc_policy = ar_dmarc.group(2)
    result["dmarc"]["policy"] = dmarc_policy

    return result


def get_body_text(msg: email.message.EmailMessage) -> str:
    parts: List[str] = []
    if msg.is_multipart():
        for part in msg.walk():
            ctype = part.get_content_type()
            if ctype in ("text/plain", "text/html"):
                try:
                    parts.append(part.get_content())
                except Exception:
                    try:
                        parts.append(part.get_payload(decode=True).decode(part.get_content_charset() or "utf-8", errors="ignore"))
                    except Exception:
                        continue
    else:
        try:
            parts.append(msg.get_content())
        except Exception:
            try:
                parts.append(msg.get_payload(decode=True).decode(msg.get_content_charset() or "utf-8", errors="ignore"))
            except Exception:
                pass
    return "\n".join(p for p in parts if p)


def extract_urls(text: str) -> List[str]:
    return list({m.group(0) for m in URL_REGEX.finditer(text or "")})


def extract_received_ips(msg: email.message.EmailMessage) -> List[str]:
    ips: List[str] = []
    for h, v in msg.items():
        if h.lower() == "received":
            ips += IP_REGEX.findall(v)
    # Normalize and filter invalid octets
    cleaned: List[str] = []
    for ip in ips:
        octets = ip.split(".")
        if len(octets) == 4 and all(0 <= int(o) <= 255 for o in octets):
            cleaned.append(ip)
    return list(dict.fromkeys(cleaned))


def likely_origin_ip(ips: List[str]) -> Optional[str]:
    if not ips:
        return None
    # Heuristic: last public IP seen in received chain is often source
    public_ips = [ip for ip in ips if not any(p.match(ip) for p in PRIVATE_IP_PATTERNS)]
    if public_ips:
        return public_ips[-1]
    return ips[-1]


async def reverse_dns(ip: str, timeout: float = 3.0) -> Optional[str]:
    """Return PTR hostname for an IP if available.

    Tries async getnameinfo first, then falls back to blocking gethostbyaddr
    executed in a thread pool.
    """
    loop = asyncio.get_running_loop()
    try:
        host, _svc = await asyncio.wait_for(loop.getnameinfo((ip, 0), 0), timeout=timeout)
        return host
    except Exception:
        pass
    try:
        # Fallback: run reverse DNS in a thread to avoid blocking
        host, _aliases, _ips = await asyncio.wait_for(
            loop.run_in_executor(None, socket.gethostbyaddr, ip), timeout=timeout
        )
        return host
    except Exception:
        return None


def is_private_ip(ip: str) -> bool:
    return any(p.match(ip) for p in PRIVATE_IP_PATTERNS)


def extract_from_domain(msg: email.message.EmailMessage) -> Optional[str]:
    """Extract domain from From header, if present."""
    addr = parseaddr(msg.get("from", ""))[1]
    if not addr or "@" not in addr:
        return None
    return addr.split("@")[-1].strip().lower()


def extract_return_path_domain(msg: email.message.EmailMessage) -> Optional[str]:
    """Extract domain from Return-Path header, if present."""
    rp = (msg.get("return-path") or "").strip("<>")
    if not rp or "@" not in rp:
        return None
    return rp.split("@")[-1].strip().lower()
