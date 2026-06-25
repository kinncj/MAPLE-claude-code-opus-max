#!/usr/bin/env python3
"""Extract ```gherkin blocks from docs/stories/*.md into tests/features/*.feature.

Idempotent. Skips the story template. Replaces the broken inline-Python Makefile target.
"""
import pathlib
import re
import sys

STORIES = pathlib.Path("docs/stories")
OUT = pathlib.Path("tests/features")
SKIP = {"_template"}


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    count = 0
    scenarios = 0
    for story in sorted(STORIES.glob("*.md")):
        if story.stem in SKIP:
            continue
        blocks = re.findall(r"```gherkin\n(.*?)```", story.read_text(), re.DOTALL)
        if not blocks:
            continue
        feature = "\n\n".join(b.rstrip() + "\n" for b in blocks)
        (OUT / f"{story.stem}.feature").write_text(feature)
        count += 1
        scenarios += sum(b.count("Scenario:") for b in blocks)
    print(f"Synced {count} feature file(s), {scenarios} scenario(s) to {OUT}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
