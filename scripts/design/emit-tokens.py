#!/usr/bin/env python3
"""Emit framework token files from the canonical DTCG tokens.json.
Regenerates docs/design/identity/{tokens.css, tailwind.tokens.js, mantine.theme.ts}.
Run: python3 scripts/design/emit-tokens.py"""
import json, pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
ID = ROOT / "docs/design/identity"
tok = json.loads((ID / "tokens.json").read_text())

def leaves(group):
    return {k: v["$value"] for k, v in group.items() if isinstance(v, dict) and "$value" in v}

light = leaves(tok["color"]["light"])
dark = leaves(tok["color"]["dark"])
sizes = leaves(tok["font"]["size"])
space = leaves(tok["space"])
radius = leaves(tok["radius"])

def css_block(d, prefix):
    return "\n".join(f"  --{prefix}-{k}: {v};" for k, v in d.items())

css = f"""/* GENERATED from tokens.json by scripts/design/emit-tokens.py — do not hand-edit. */
:root, [data-theme="light"] {{
{css_block(light, 'color')}
{css_block(sizes, 'font-size')}
{css_block(space, 'space')}
{css_block(radius, 'radius')}
  --font-sans: {", ".join(tok['font']['family']['sans']['$value'])};
  --shadow-sm: 0 1px 2px 0 rgba(15,23,42,0.08);
  --shadow-md: 0 4px 12px 0 rgba(15,23,42,0.10);
}}
[data-theme="dark"] {{
{css_block(dark, 'color')}
}}
@media (prefers-color-scheme: dark) {{
  :root:not([data-theme="light"]) {{
{css_block(dark, 'color')}
  }}
}}
"""
(ID / "tokens.css").write_text(css)

tw = "// GENERATED from tokens.json — do not hand-edit.\n"
tw += "module.exports = {\n  theme: {\n    extend: {\n      colors: {\n"
for k in light:
    tw += f"        '{k}': 'var(--color-{k})',\n"
tw += "      },\n      borderRadius: {\n"
for k, v in radius.items():
    tw += f"        '{k}': '{v}',\n"
tw += "      },\n    },\n  },\n}\n"
(ID / "tailwind.tokens.js").write_text(tw)

mt = "// GENERATED from tokens.json — do not hand-edit.\n"
mt += "import type { MantineThemeOverride } from '@mantine/core';\n\n"
mt += "export const taskmapleTheme: MantineThemeOverride = {\n"
mt += "  fontFamily: \"" + ", ".join(tok['font']['family']['sans']['$value']) + "\",\n"
mt += "  primaryColor: 'blue',\n  defaultRadius: 'md',\n"
mt += "  other: {\n    light: " + json.dumps(light) + ",\n    dark: " + json.dumps(dark) + ",\n  },\n};\n"
(ID / "mantine.theme.ts").write_text(mt)

print("emitted: tokens.css, tailwind.tokens.js, mantine.theme.ts")
print("light keys:", len(light), "dark keys:", len(dark))
