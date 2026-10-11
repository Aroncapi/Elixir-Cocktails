#!/usr/bin/env bash
# Deploy en el servidor: trae los ultimos cambios y reconstruye solo
# lo que cambio (cache de Docker). Uso: ./deploy.sh  (o bash deploy.sh)
set -euo pipefail
cd "$(dirname "$0")"

echo "== git pull =="
git pull

echo "== docker compose up -d --build =="
docker compose up -d --build

echo "== estado =="
docker compose ps
echo "Listo. Frontend: http://$(grep -E '^PUBLIC_URL=' .env 2>/dev/null | cut -d= -f2- || echo 'localhost')"
