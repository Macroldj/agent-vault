#!/usr/bin/env bash
set -euo pipefail
REGISTRY="${1:-}"
USERNAME="${2:-}"
PASSWORD="${3:-}"
if [[ -z "$REGISTRY" || -z "$USERNAME" || -z "$PASSWORD" ]]; then
  echo "usage: $0 <registry> <username> <password>" >&2
  exit 1
fi
echo "$PASSWORD" | docker login "$REGISTRY" -u "$USERNAME" --password-stdin