import asyncio
from typing import Dict, Any, List, Optional
from .utils import (
    parse_email,
    extract_authentication_results,
    extract_urls,
    get_body_text,
    extract_received_ips,
    likely_origin_ip,
    reverse_dns,
    is_private_ip,
    extract_from_domain,
    extract_return_path_domain,
)
import dkim  # type: ignore
import spf  # type: ignore
import dns.asyncresolver  # type: ignore
from .reputation import evaluate_urls


def _score_from_results(
    auth: Dict[str, Any],
    origin_ip: Optional[str],
    urls_eval: Dict[str, Dict],
    from_domain: Optional[str],
    env_domain: Optional[str],
) -> Dict[str, Any]:
    score = 0
    reasons: List[str] = []

    spf_res = (auth.get("spf") or {}).get("result")
    dkim_res = (auth.get("dkim") or {}).get("result")
    dmarc_pol = (auth.get("dmarc") or {}).get("policy")

    if spf_res == "pass":
        score += 2
    elif spf_res in {"fail", "softfail"}:
        reasons.append("SPF fail")

    if dkim_res == "pass":
        score += 2
    elif dkim_res in {"fail", "permerror"}:
        reasons.append("DKIM fail")

    if dmarc_pol is not None:
        score += 1

    # From vs return-path mismatch heuristic
    if from_domain and env_domain and from_domain != env_domain:
        score -= 2
        reasons.append("From-domain ≠ Envelope-from")

    if origin_ip and is_private_ip(origin_ip):
        score -= 2
        reasons.append("Origin IP is private range")

    # Suspicious domains from URLs evaluation
    for url, data in urls_eval.items():
        local = data.get("local", {})
        if local.get("score", 0) < 0:
            score -= 2
            reasons.append(f"Suspicious domain: {data.get('domain')}")
        vt = data.get("virustotal")
        if vt and (vt.get("malicious", 0) > 0 or vt.get("suspicious", 0) > 0):
            score -= 2
            reasons.append(f"VirusTotal flags: {data.get('domain')}")

    if score <= -1:
        verdict = "likely_phishing"
    elif score <= 1:
        verdict = "suspicious"
    else:
        verdict = "likely_legitimate"

    return {"score": score, "verdict": verdict, "reasons": reasons}


async def analyze_bytes(raw_bytes: bytes) -> Dict[str, Any]:
    msg = parse_email(raw_bytes)
    auth = extract_authentication_results(msg)
    body = get_body_text(msg)
    urls = extract_urls(body)
    ips = extract_received_ips(msg)
    origin = likely_origin_ip(ips)
    rdns = await reverse_dns(origin) if origin else None
    urls_eval = await evaluate_urls(urls)

    # Parse domains
    from_domain = extract_from_domain(msg)
    env_domain = extract_return_path_domain(msg)

    # Fallback DKIM verification if header lacks result
    if not (auth.get("dkim") or {}).get("result"):
        try:
            loop = asyncio.get_running_loop()
            verified = await loop.run_in_executor(None, lambda: dkim.DKIM(raw_bytes).verify())
            auth["dkim"] = {"result": "pass" if verified else "fail"}
        except Exception:
            pass

    # Fallback SPF check if header lacks result and we have inputs
    if not (auth.get("spf") or {}).get("result") and origin and (env_domain or from_domain):
        try:
            sdomain = env_domain or from_domain or ""
            helo = sdomain
            loop = asyncio.get_running_loop()
            def do_spf():
                res, code, txt = spf.check2(i=origin, s=f"postmaster@{sdomain}", h=helo)
                return res
            spf_res = await loop.run_in_executor(None, do_spf)
            auth["spf"] = {"result": spf_res}
        except Exception:
            pass

    # DMARC record existence
    if from_domain:
        try:
            name = f"_dmarc.{from_domain}"
            ans = await dns.asyncresolver.resolve(name, "TXT")
            # Any TXT mentioning v=DMARC1 counts as record exists
            exists = any(any(b"v=DMARC1" in part for part in rdata.strings) for rdata in ans)
            auth.setdefault("dmarc", {})["policy"] = "exists" if exists else None
        except Exception:
            pass

    scoring = _score_from_results(auth, origin, urls_eval, from_domain, env_domain)

    return {
        "spf": auth.get("spf"),
        "dkim": auth.get("dkim"),
        "dmarc": auth.get("dmarc"),
        "urls": urls,
        "received_ips": ips,
        "likely_origin_ip": origin,
        "reverse_dns": rdns,
        "verdict": scoring["verdict"],
        "score": scoring["score"],
        "reasons": scoring["reasons"],
    }
