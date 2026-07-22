$composeFile = Join-Path $PSScriptRoot "..\docker-compose.infra.yml"
docker compose -f $composeFile up -d
