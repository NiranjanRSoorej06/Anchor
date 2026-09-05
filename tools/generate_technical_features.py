from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "app/src/main/java/com/anchor"
DOCUMENT = ROOT / "docs/technical-features.md"

SECTIONS = {
    "CORE_HAPTICS": SOURCE_ROOT / "core/haptics",
    "CORE_AUDIO": SOURCE_ROOT / "core/audio",
    "CORE_VISION": SOURCE_ROOT / "core/vision",
    "DEVTOOLS": SOURCE_ROOT / "devtools",
}

DECLARATION_RE = re.compile(
    r"(?m)^[ \t]*(?P<prefix>(?:(?:public|private|protected|internal|sealed|data|value|inner|enum|annotation|fun)\s+)*)"
    r"(?P<kind>class|interface|object|typealias|fun)\s+"
    r"(?P<name>[A-Za-z_][A-Za-z0-9_]*)"
)
KDOC_RE = re.compile(r"/\*\*(.*?)\*/", re.DOTALL)


def clean_kdoc(text: str) -> str:
    lines = []
    for line in text.splitlines():
        line = re.sub(r"^\s*\*\s?", "", line).strip()
        if line and not line.startswith("@"):
            lines.append(line)
    return " ".join(lines)


def summary_for(kdoc: str | None, kind: str, name: str) -> str:
    if kdoc:
        summary = clean_kdoc(kdoc)
        if summary:
            summary = summary.split("##", 1)[0].strip()
            summary = re.sub(r"\s+", " ", summary)
            return summary

    labels = {
        "class": "Implementation class",
        "interface": "Interface boundary",
        "object": "Feature catalog or utility object",
        "typealias": "Type alias",
        "fun": "Top-level function",
    }
    return f"{labels.get(kind, 'Kotlin declaration')}: `{name}`."


def nearest_kdoc(text: str, declaration_start: int) -> str | None:
    prefix = text[:declaration_start]
    match = list(KDOC_RE.finditer(prefix))
    if not match:
        return None

    candidate = match[-1]
    between = prefix[candidate.end():]
    if between.strip():
        return None
    return candidate.group(1)


def declarations(path: Path) -> list[tuple[str, str, str]]:
    text = path.read_text(encoding="utf-8")
    found = []

    for match in DECLARATION_RE.finditer(text):
        kind = match.group("kind")
        name = match.group("name")

        # Methods inside a class/object are implementation details for this
        # inventory. Keep only true top-level functions, while retaining
        # structural declarations such as nested data classes if present.
        if kind == "fun":
            line_start = text.rfind("\n", 0, match.start()) + 1
            indentation = text[line_start:match.start("prefix")]
            if indentation or "private" in match.group("prefix").split():
                continue

        prefix = match.group("prefix")
        if kind == "class" and "enum" in prefix.split():
            kind = "enum class"

        kdoc = nearest_kdoc(text, match.start())
        found.append((kind, name, summary_for(kdoc, kind, name)))

    return found


def render(directory: Path) -> str:
    rows = []
    for path in sorted(directory.rglob("*.kt")):
        relative = path.relative_to(ROOT).as_posix()
        for kind, name, summary in declarations(path):
            rows.append(f"- `{name}` ({kind}) in `{relative}`: {summary}")

    return "\n".join(rows) if rows else "_No Kotlin declarations found._"


def replace_section(document: str, section: str, content: str) -> str:
    start = f"<!-- AUTO:{section}:START -->"
    end = f"<!-- AUTO:{section}:END -->"
    pattern = re.compile(rf"(?s){re.escape(start)}.*?{re.escape(end)}")
    matches = list(pattern.finditer(document))

    if len(matches) != 1:
        raise RuntimeError(
            f"Expected exactly one marker pair for {section}; found {len(matches)}"
        )

    replacement = f"{start}\n{content}\n{end}"
    return pattern.sub(replacement, document, count=1)


def main() -> None:
    document = DOCUMENT.read_text(encoding="utf-8")

    for section, directory in SECTIONS.items():
        if not directory.is_dir():
            raise FileNotFoundError(f"Source directory does not exist: {directory}")
        document = replace_section(document, section, render(directory))

    DOCUMENT.write_text(document, encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main()
