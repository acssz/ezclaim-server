#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
HOOKS_DIR="$ROOT_DIR/.git/hooks"

if [[ ! -d "$HOOKS_DIR" ]]; then
  echo "[install-git-hooks] .git/hooks not found. Run after 'git init' or in a cloned repo."
  exit 1
fi

if command -v pre-commit >/dev/null 2>&1; then
  echo "[install-git-hooks] Installing pre-commit hooks via pre-commit..."
  (cd "$ROOT_DIR" && pre-commit install --hook-type pre-commit --hook-type pre-push)
  echo "[install-git-hooks] pre-commit hooks installed."
else
  echo "[install-git-hooks] 'pre-commit' not found. Falling back to legacy shell hook."
  install_hook() {
    local name="$1"; shift
    local src="$ROOT_DIR/scripts/$name.sh"
    local dst="$HOOKS_DIR/$name"
    if [[ ! -f "$src" ]]; then
      echo "[install-git-hooks] Missing script: $src"
      exit 1
    fi
    cp "$src" "$dst"
    chmod +x "$dst"
    echo "[install-git-hooks] Installed $name hook."
  }
  install_hook pre-commit
  echo "[install-git-hooks] Legacy shell pre-commit installed. To enable full hooks, install pre-commit: pipx install pre-commit (or pip install pre-commit)."
fi
