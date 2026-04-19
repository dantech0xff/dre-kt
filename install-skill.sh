#!/bin/bash
# Install dre-integrate skill for Claude Code
# Usage: curl -fsSL https://raw.githubusercontent.com/dantech0xff/dre-kt/master/install-skill.sh | bash

set -e

REPO="dantech0xff/dre-kt"
BRANCH="master"
SKILL_DIR=".claude/skills/dre-integrate"
BASE_URL="https://raw.githubusercontent.com/$REPO/$BRANCH/$SKILL_DIR"

FILES=(
    "SKILL.md"
    "references/api-reference.md"
    "references/setup-guide.md"
    "references/feature-scaffold.md"
    "references/migration-guide.md"
    "references/examples.md"
)

echo "Installing dre-integrate skill..."

mkdir -p "$SKILL_DIR/references"

for file in "${FILES[@]}"; do
    echo "  Downloading $file"
    curl -fsSL "$BASE_URL/$file" -o "$SKILL_DIR/$file"
done

echo ""
echo "✓ Installed to $SKILL_DIR"
echo ""
echo "Usage:"
echo "  /dre-integrate setup     — Add dre-kt dependency"
echo "  /dre-integrate feature   — Scaffold a new feature"
echo "  /dre-integrate migrate   — Migrate from MVI/MVVM"
