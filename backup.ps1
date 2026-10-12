# backup.ps1 - respaldo de la BD (dump .sql) + fotos de cocteles (uploads/)
# Uso:  .\backup.ps1            (guarda en .\backup\<fecha_hora>\)
#       .\backup.ps1 -Out D:\bu (otra carpeta)
# Requiere: Docker Desktop abierto y el stack levantado.
param([string]$Out = (Join-Path $PSScriptRoot 'backup'))

$ErrorActionPreference = 'Stop'
$stamp = Get-Date -Format 'yyyy-MM-dd_HHmm'
$dest  = Join-Path $Out $stamp
New-Item -ItemType Directory -Force -Path $dest | Out-Null

# Docker corriendo?
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker no esta corriendo: abre Docker Desktop y vuelve a intentar.' }

# Contenedores del stack presentes?
$running = docker compose ps --status running --services
if ($running -notcontains 'mysql' -or $running -notcontains 'core') {
    throw 'Faltan contenedores (mysql/core). Levanta el stack:  docker compose up -d'
}

Write-Host "== Dump de MySQL (auth_db, core_db, notif_db) =="
docker compose exec -T mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --routines --triggers --databases auth_db core_db notif_db --result-file=/tmp/respaldo.sql'
if ($LASTEXITCODE -ne 0) { throw 'Fallo el mysqldump.' }
docker compose cp "mysql:/tmp/respaldo.sql" (Join-Path $dest 'respaldo.sql')
docker compose exec -T mysql sh -c 'rm -f /tmp/respaldo.sql'

Write-Host "== Fotos (volumen uploads_data) =="
$uploads = Join-Path $dest 'uploads'
docker compose cp "core:/data/uploads" $uploads
if ($LASTEXITCODE -ne 0) { throw 'Fallo copiar uploads/.' }

Write-Host ''
Write-Host "Backup listo: $dest" -ForegroundColor Green
Get-ChildItem $dest -Recurse -File | Measure-Object -Property Length -Sum |
    ForEach-Object { "  Archivos: $($_.Count)   Total: {0:N1} MB" -f ($_.Sum / 1MB) }
Write-Host ''
Write-Host 'Para restaurar en otra maquina:'
Write-Host '  1) docker compose up -d mysql   (deja correr las migraciones)'
Write-Host '  2) docker compose cp backup\<fecha>\respaldo.sql mysql:/tmp/r.sql'
Write-Host '  3) docker compose exec -T mysql sh -c ''mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /tmp/r.sql'''
Write-Host '  4) copia la carpeta uploads/ al volumen:  docker compose cp backup\<fecha>\uploads core:/data/uploads'
