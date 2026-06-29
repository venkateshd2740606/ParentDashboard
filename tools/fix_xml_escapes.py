#!/usr/bin/env python3
"""Fix invalid XML escapes in translated strings.xml files."""
from pathlib import Path
import re

RES = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"
BROKEN_TURN = re.compile(
    r"<string name=\"same_device_turn\">[^<]*(?:\\\\'|'s turn • Score)[^<]*</string>"
)


def fix_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace(r"\\'", "'")
    if BROKEN_TURN.search(text):
        text = BROKEN_TURN.sub(
            '<string name="same_device_turn">%1$s turn • Score %2$d - %3$d</string>',
            text,
            count=1,
        )
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> None:
    count = sum(1 for p in RES.glob("values*/strings.xml") if fix_file(p))
    print(f"Fixed {count} files")


if __name__ == "__main__":
    main()
