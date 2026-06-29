#!/usr/bin/env python3
"""Fill only missing string keys per locale — much faster than full regeneration."""

from __future__ import annotations

import re
import sys
import time
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

try:
    from deep_translator import GoogleTranslator
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "deep-translator", "-q"])
    from deep_translator import GoogleTranslator

RES = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"

LOCALES = {
    "hi": ("values-hi", "hi"), "te": ("values-te", "te"), "ta": ("values-ta", "ta"),
    "kn": ("values-kn", "kn"), "ml": ("values-ml", "ml"), "mr": ("values-mr", "mr"),
    "bn": ("values-bn", "bn"), "gu": ("values-gu", "gu"), "pa": ("values-pa", "pa"),
    "es": ("values-es", "es"), "fr": ("values-fr", "fr"), "de": ("values-de", "de"),
    "pt": ("values-pt", "pt"), "it": ("values-it", "it"), "ru": ("values-ru", "ru"),
    "ar": ("values-ar", "ar"), "in": ("values-in", "id"), "ja": ("values-ja", "ja"),
    "ko": ("values-ko", "ko"), "zh": ("values-zh", "zh-CN"), "zh-TW": ("values-zh-rTW", "zh-TW"),
}

KEEP_ENGLISH = {"app_name", "home_title", "theme_amoled", "theme_neon", "theme_cyber"}
PH_RE = re.compile(r"(%(\d+\$)?[sd]|%[sd]|ParentDashboard|XP|AMOLED|Neon|Cyber)")


def parse(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    return {e.get("name"): e.text for e in ET.parse(path).getroot()
            if e.tag == "string" and e.get("name") and e.text}


def esc(t: str) -> str:
    return (
        t.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def mask(text: str) -> tuple[str, list[str]]:
    tok: list[str] = []
    def r(m: re.Match[str]) -> str:
        tok.append(m.group(0))
        return f"__P{len(tok)-1}__"
    return PH_RE.sub(r, text), tok


def unmask(text: str, tok: list[str]) -> str:
    for i, t in enumerate(tok):
        text = text.replace(f"__P{i}__", t)
    return text


def translate_batch(values: list[str], target: str) -> list[str]:
    if not values:
        return []
    tr = GoogleTranslator(source="en", target=target)
    pairs = [mask(v) for v in values]
    masked = [p[0] for p in pairs]
    try:
        out = tr.translate_batch(masked)
    except Exception:
        out = [tr.translate(t) for t in masked]
    return [unmask(o or pairs[i][0], pairs[i][1]) for i, o in enumerate(out)]


def fill_locale(code: str, folder: str, target: str, base: dict[str, str], order: list[str]) -> str:
    path = RES / folder / "strings.xml"
    existing = parse(path)
    missing = [k for k in order if k not in existing]
    if not missing:
        print(f"{code}: already complete ({len(existing)} keys)", flush=True)
        return code

    to_keys, to_vals = [], []
    for k in missing:
        if k in KEEP_ENGLISH:
            existing[k] = base[k]
        else:
            to_keys.append(k)
            to_vals.append(base[k])

    if to_vals:
        translated = translate_batch(to_vals, target)
        for k, v in zip(to_keys, translated):
            existing[k] = v
        time.sleep(0.1)

    merged = {k: existing[k] for k in order}
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for k, v in merged.items():
        lines.append(f'    <string name="{k}">{esc(v)}</string>')
    lines.append("</resources>")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{code}: added {len(missing)} keys -> {len(merged)} total", flush=True)
    return code


def main() -> None:
    base = parse(RES / "values" / "strings.xml")
    order = list(base.keys())
    incomplete = [(c, LOCALES[c][0], LOCALES[c][1]) for c in LOCALES
                  if len(parse(RES / LOCALES[c][0] / "strings.xml")) < len(order)]
    print(f"Base: {len(order)} keys, incomplete locales: {len(incomplete)}", flush=True)
    if not incomplete:
        return

    with ThreadPoolExecutor(max_workers=4) as pool:
        futs = [pool.submit(fill_locale, c, f, t, base, order) for c, f, t in incomplete]
        for f in as_completed(futs):
            f.result()


if __name__ == "__main__":
    main()
