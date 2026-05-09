#!/bin/bash
# Install dre-integrate skill for Claude Code and Codex.
# Usage:
#   gh api -H "Accept: application/vnd.github.raw" "repos/dantech0xff/dre-kt/contents/install-skill.sh?ref=master" | bash
#   ./install-skill.sh
#   ./install-skill.sh --claude
#   ./install-skill.sh --codex
#   ./install-skill.sh --all

set -euo pipefail

REPO="dantech0xff/dre-kt"
BRANCH="master"
SKILL_NAME="dre-integrate"
SOURCE_SKILL_DIR=".claude/skills/$SKILL_NAME"
CLAUDE_HOME="${CLAUDE_HOME:-.claude}"
CODEX_HOME="${CODEX_HOME:-${HOME:-.}/.codex}"

FILES=(
    "SKILL.md"
    "references/api-reference.md"
    "references/setup-guide.md"
    "references/feature-scaffold.md"
    "references/migration-guide.md"
    "references/examples.md"
)

usage() {
    cat <<EOF
Install dre-integrate skill.

Usage:
  install-skill.sh [--claude|--codex|--all]

Options:
  --claude   Install to ${CLAUDE_HOME:-.claude}/skills/dre-integrate
  --codex    Install to ${CODEX_HOME}/skills/dre-integrate
  --all      Install to both Claude and Codex skill directories (default)
  --help     Show this help

Requirements:
  gh         Authenticated GitHub CLI available on PATH

Environment:
  CLAUDE_HOME  Override Claude home directory for install/test
  CODEX_HOME   Override Codex home directory for install/test
EOF
}

TARGET="${1:-}"
if [ -z "$TARGET" ]; then
    TARGET="--all"
fi

INSTALL_CLAUDE=false
INSTALL_CODEX=false

case "$TARGET" in
    --claude|claude)
        INSTALL_CLAUDE=true
        ;;
    --codex|codex)
        INSTALL_CODEX=true
        ;;
    --all|all)
        INSTALL_CLAUDE=true
        INSTALL_CODEX=true
        ;;
    -h|--help|help)
        usage
        exit 0
        ;;
    *)
        echo "Unknown option: $TARGET" >&2
        usage >&2
        exit 1
        ;;
esac

require_gh() {
    if ! command -v gh >/dev/null 2>&1; then
        echo "GitHub CLI is required. Install gh, then run this script again." >&2
        exit 1
    fi

    if ! gh auth status >/dev/null 2>&1; then
        echo "GitHub CLI authentication is required. Run gh auth login or set GH_TOKEN, then run this script again." >&2
        exit 1
    fi
}

add_codex_frontmatter() {
    local skill_file="$1"
    local next_file="$skill_file.next"

    if [ "$(head -n 1 "$skill_file")" = "---" ]; then
        return
    fi

    {
        printf '%s\n' "---"
        printf '%s\n' "name: $SKILL_NAME"
        printf '%s\n' 'description: "Integrate dre-kt (Dispatch -> Reduce -> Effects) into an Android/Kotlin project."'
        printf '%s\n' "---"
        printf '\n'
        sed -n '1,$p' "$skill_file"
    } > "$next_file"

    mv "$next_file" "$skill_file"
}

download_skill() {
    local skill_dir="$1"
    local target_kind="$2"
    local parent_dir
    local tmp_dir

    echo "Installing $SKILL_NAME skill to $skill_dir"
    parent_dir="$(dirname "$skill_dir")"
    mkdir -p "$parent_dir"
    tmp_dir="$(mktemp -d "$parent_dir/.${SKILL_NAME}.tmp.XXXXXX")"
    trap 'rm -rf "$tmp_dir"' RETURN

    for file in "${FILES[@]}"; do
        echo "  Downloading $file"
        mkdir -p "$(dirname "$tmp_dir/$file")"
        gh api \
            -H "Accept: application/vnd.github.raw" \
            "repos/$REPO/contents/$SOURCE_SKILL_DIR/$file?ref=$BRANCH" \
            > "$tmp_dir/$file"
    done

    if [ "$target_kind" = "codex" ]; then
        add_codex_frontmatter "$tmp_dir/SKILL.md"
        write_codex_metadata "$tmp_dir"
    fi

    rm -rf "$skill_dir"
    mv "$tmp_dir" "$skill_dir"
    trap - RETURN
}

write_codex_metadata() {
    local skill_dir="$1"

    mkdir -p "$skill_dir/agents"
    cat > "$skill_dir/agents/openai.yaml" <<'EOF'
interface:
  display_name: "Dre Integrate"
  short_description: "Integrate dre-kt (Dispatch -> Reduce -> Effects) into an Android/Kotlin project."
  default_prompt: "Use $dre-integrate to integrate dre-kt into an Android/Kotlin project."
policy:
  allow_implicit_invocation: true
EOF
}

require_gh

if [ "$INSTALL_CLAUDE" = true ]; then
    download_skill "$CLAUDE_HOME/skills/$SKILL_NAME" "claude"
fi

if [ "$INSTALL_CODEX" = true ]; then
    download_skill "$CODEX_HOME/skills/$SKILL_NAME" "codex"
fi

echo ""
echo "Installed $SKILL_NAME"
echo ""
echo "Usage:"
echo "  /dre-integrate setup     - Add dre-kt dependency"
echo "  /dre-integrate feature   - Scaffold a new feature"
echo "  /dre-integrate migrate   - Migrate from MVI/MVVM"
echo '  $dre-integrate           - Use the Codex skill when installed for Codex'
