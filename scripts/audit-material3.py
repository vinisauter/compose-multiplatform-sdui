"""Audit named public visual composables against the *resolved* M3 sources, without downloading.

Usage: python scripts/audit-material3.py <path-to-material3-1.8.2-sources.jar>
This checks named component families, not overload/parameter parity or runtime behavior.
"""
import argparse
import re
import zipfile
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source_jar", type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    definitions = root / "humming-contract/src/commonMain/kotlin/me/developes/humming/sdui/layout/Material3Definitions.kt"
    source = definitions.read_text(encoding="utf-8").split("object LegacyMaterial3Definitions")[0]
    wires = [name for match in re.finditer(r'(?:variants|definition)\("([^"]+)"', source)
             for name in match[1].split("|")]
    aliases = {"materialOutlinedButton": "OutlinedButton", "materialTextButton": "TextButton",
               "materialIconButton": "IconButton"}
    implemented = {aliases.get(name, name[0].upper() + name[1:]) for name in wires}
    implemented.update({"Text", "Icon", "Button", "TextField", "TopAppBar", "Scaffold", "Divider"})
    # Host/style configuration, not separate visual nodes. See docs/material3.md.
    configuration = {"MaterialTheme", "ProvideTextStyle"}
    public = set()
    with zipfile.ZipFile(args.source_jar) as jar:
        for entry in jar.namelist():
            if not entry.startswith("commonMain/") or not entry.endswith(".kt") or "/internal/" in entry or "/tokens/" in entry:
                continue
            code = jar.read(entry).decode("utf-8")
            # Top-level, non-internal named composables. Defaults members and factories
            # are deliberately excluded; receiver member ExposedDropdownMenu is audited manually.
            for match in re.finditer(r'^(?:public )?(?:expect )?fun (?:[\w<>]+\.)?([A-Z]\w*)\(', code, re.M):
                if "@Composable" in code[max(0, match.start() - 150):match.start()]:
                    public.add(match[1])
    missing = sorted(public - implemented - configuration)
    print(f"Public named top-level composables: {len(public)}; new wire adapters: {len(wires)}")
    print("Configuration only:", ", ".join(sorted(public & configuration)))
    print("Missing visual families:", ", ".join(missing) if missing else "none")
    return 1 if missing else 0


if __name__ == "__main__":
    raise SystemExit(main())
