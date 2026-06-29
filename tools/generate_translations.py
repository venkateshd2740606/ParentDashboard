#!/usr/bin/env python3
"""Generate fully translated ParentDashboard strings for every supported locale."""

from __future__ import annotations

import argparse
import re
import sys
import time
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

try:
    from deep_translator import GoogleTranslator
except ImportError:
    print("Installing deep-translator...", file=sys.stderr, flush=True)
    import subprocess

    subprocess.check_call([sys.executable, "-m", "pip", "install", "deep-translator", "-q"])
    from deep_translator import GoogleTranslator

COLORSORT_RES = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"

LOCALES = {
    "hi": ("values-hi", "hi"),
    "te": ("values-te", "te"),
    "ta": ("values-ta", "ta"),
    "kn": ("values-kn", "kn"),
    "ml": ("values-ml", "ml"),
    "mr": ("values-mr", "mr"),
    "bn": ("values-bn", "bn"),
    "gu": ("values-gu", "gu"),
    "pa": ("values-pa", "pa"),
    "es": ("values-es", "es"),
    "fr": ("values-fr", "fr"),
    "de": ("values-de", "de"),
    "pt": ("values-pt", "pt"),
    "it": ("values-it", "it"),
    "ru": ("values-ru", "ru"),
    "ar": ("values-ar", "ar"),
    "in": ("values-in", "id"),
    "ja": ("values-ja", "ja"),
    "ko": ("values-ko", "ko"),
    "zh": ("values-zh", "zh-CN"),
    "zh-TW": ("values-zh-rTW", "zh-TW"),
}

KEEP_ENGLISH_KEYS = {
    "app_name",
    "home_title",
    "theme_amoled",
    "theme_neon",
    "theme_cyber",
}

PLACEHOLDER_RE = re.compile(
    r"(%(\d+\$)?[sd]|%[sd]|ParentDashboard|XP|AMOLED|Neon|Cyber)"
)


def parse_strings(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    tree = ET.parse(path)
    return {
        elem.get("name"): elem.text
        for elem in tree.getroot()
        if elem.tag == "string" and elem.get("name") and elem.text is not None
    }


def escape_xml(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "\\'")
    )


def mask_placeholders(text: str) -> tuple[str, list[str]]:
    tokens: list[str] = []

    def repl(match: re.Match[str]) -> str:
        tokens.append(match.group(0))
        return f"__PH{len(tokens) - 1}__"

    return PLACEHOLDER_RE.sub(repl, text), tokens


def unmask_placeholders(text: str, tokens: list[str]) -> str:
    for index, token in enumerate(tokens):
        text = text.replace(f"__PH{index}__", token)
    return text


def translate_values(values: list[str], target: str) -> list[str]:
    if not values:
        return []
    translator = GoogleTranslator(source="en", target=target)
    masked_pairs = [mask_placeholders(value) for value in values]
    masked_texts = [pair[0] for pair in masked_pairs]
    translated: list[str] = []
    chunk_size = 50
    for start in range(0, len(masked_texts), chunk_size):
        chunk = masked_texts[start : start + chunk_size]
        try:
            chunk_result = translator.translate_batch(chunk)
        except Exception:
            chunk_result = [translator.translate(text) for text in chunk]
        translated.extend(chunk_result)
        time.sleep(0.15)
    result: list[str] = []
    for (masked, tokens), translated_text in zip(masked_pairs, translated):
        result.append(unmask_placeholders(translated_text or masked, tokens))
    return result


def write_strings(folder: str, strings: dict[str, str]) -> None:
    out_dir = COLORSORT_RES / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for key, value in strings.items():
        lines.append(f'    <string name="{key}">{escape_xml(value)}</string>')
    lines.append("</resources>")
    (out_dir / "strings.xml").write_text("\n".join(lines) + "\n", encoding="utf-8")


def translate_locale(
    code: str,
    folder: str,
    google_target: str,
    base: dict[str, str],
    keys: list[str],
    force: bool,
) -> tuple[str, int]:
    out_path = COLORSORT_RES / folder / "strings.xml"
    existing = parse_strings(out_path)
    if not force and len(existing) >= len(keys):
        print(f"Skipping {code} ({len(existing)} keys already complete)", flush=True)
        return code, len(existing)

    print(f"Translating {code} ({google_target})...", flush=True)
    translated_map: dict[str, str] = {}
    to_translate_keys: list[str] = []
    to_translate_values: list[str] = []

    for key in keys:
        if key in KEEP_ENGLISH_KEYS:
            translated_map[key] = base[key]
            continue
        to_translate_keys.append(key)
        to_translate_values.append(base[key])

    translated_values = translate_values(to_translate_values, google_target)
    for key, value in zip(to_translate_keys, translated_values):
        translated_map[key] = value

    ordered = {key: translated_map[key] for key in keys}
    write_strings(folder, ordered)
    print(f"  Wrote {folder}/strings.xml ({len(keys)} keys)", flush=True)
    return code, len(keys)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--force", action="store_true", help="Regenerate even if complete")
    parser.add_argument("--locale", action="append", help="Only these locale codes (repeatable)")
    parser.add_argument("--workers", type=int, default=2, help="Parallel locale workers")
    args = parser.parse_args()

    base = parse_strings(COLORSORT_RES / "values" / "strings.xml")
    keys = list(base.keys())
    selected = LOCALES.items()
    if args.locale:
        selected = [(code, LOCALES[code]) for code in args.locale if code in LOCALES]

    jobs = [
        (code, folder, google_target)
        for code, (folder, google_target) in selected
    ]

    if args.workers <= 1 or len(jobs) == 1:
        for code, folder, google_target in jobs:
            translate_locale(code, folder, google_target, base, keys, args.force)
        return

    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        futures = [
            pool.submit(translate_locale, code, folder, google_target, base, keys, args.force)
            for code, folder, google_target in jobs
        ]
        for future in as_completed(futures):
            code, count = future.result()
            print(f"Finished {code}: {count} keys", flush=True)


if __name__ == "__main__":
    main()
