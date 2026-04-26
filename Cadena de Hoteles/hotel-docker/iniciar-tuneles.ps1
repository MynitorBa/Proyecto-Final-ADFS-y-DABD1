# =============================================================================
# iniciar-tuneles.ps1
# Levanta el hotel DESDE CERO con Quick Tunnels de Cloudflare.
# Cada ejecucion borra contenedores, volumenes e imagenes anteriores.
# Uso: .\iniciar-tuneles.ps1 -EnvFile .env.hotel1
# =============================================================================

param(
    [string]$EnvFile = ".env.hotel1"
)

# ── Leer variables del .env ───────────────────────────────────────────────────
$envVars = @{}
Get-Content $EnvFile | Where-Object { $_ -match '^[^#]' } | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        $envVars[$Matches[1].Trim()] = $Matches[2].Trim()
    }
}

$BACKEND_PORT  = $envVars["BACKEND_PORT"]
$FRONTEND_PORT = $envVars["FRONTEND_PORT"]
$AGENCY_PORT   = $envVars["AGENCY_PORT"]
$AIRLINE_PORT  = $envVars["AIRLINE_PORT"]

Write-Host ""
Write-Host "=============================================" -ForegroundColor DarkCyan
Write-Host "  Miku Inn - Cloudflare Quick Tunnel" -ForegroundColor Cyan
Write-Host "  Env: $EnvFile" -ForegroundColor Gray
Write-Host "  Backend:  localhost:$BACKEND_PORT" -ForegroundColor Gray
Write-Host "  Frontend: localhost:$FRONTEND_PORT" -ForegroundColor Gray
Write-Host "=============================================" -ForegroundColor DarkCyan

# ── Funcion: esperar a que cloudflared imprima la URL ─────────────────────────
function Wait-ForTunnelUrl {
    param([string]$LogFile, [int]$TimeoutSec = 90)

    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    Write-Host "   Esperando URL" -NoNewline -ForegroundColor Yellow

    while ((Get-Date) -lt $deadline) {
        Write-Host "." -NoNewline -ForegroundColor Yellow
        Start-Sleep -Milliseconds 800

        if (Test-Path $LogFile) {
            $content = Get-Content $LogFile -Raw -ErrorAction SilentlyContinue
            if ($content -match 'https://[a-z0-9\-]+\.trycloudflare\.com') {
                Write-Host " OK" -ForegroundColor Green
                return $Matches[0]
            }
        }
    }

    Write-Host " TIMEOUT" -ForegroundColor Red
    return $null
}

# ── Funcion: lanzar cloudflared en segundo plano ──────────────────────────────
function Start-Tunnel {
    param([int]$Port, [string]$Label)

    $logFile = "$env:TEMP\cf-$Label-$Port.log"
    Remove-Item $logFile -ErrorAction SilentlyContinue

    $proc = Start-Process cloudflared `
        -ArgumentList "tunnel --url http://localhost:$Port --no-autoupdate" `
        -RedirectStandardError $logFile `
        -PassThru -WindowStyle Hidden

    return @{ Process = $proc; Log = $logFile }
}

# ── PASO 1: Limpiar TODO lo anterior ─────────────────────────────────────────
Write-Host ""
Write-Host "[1/6] Limpiando contenedores, volumenes e imagenes anteriores..." -ForegroundColor Cyan

docker compose --env-file $EnvFile down --volumes --remove-orphans

$projectName = $envVars["COMPOSE_PROJECT_NAME"]
docker images --format "{{.Repository}}:{{.Tag}}" | Where-Object { $_ -like "$projectName*" } | ForEach-Object {
    docker rmi $_ --force 2>$null
}

Remove-Item "docker-compose.override.yml" -ErrorAction SilentlyContinue

Write-Host "   Limpieza completa." -ForegroundColor Green

# ── PASO 2: Levantar el backend desde cero ────────────────────────────────────
Write-Host ""
Write-Host "[2/6] Construyendo y levantando backend desde cero..." -ForegroundColor Cyan
docker compose --env-file $EnvFile up -d --build backend
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo el build del backend." -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 5

# ── PASO 3: Tunel del backend ─────────────────────────────────────────────────
Write-Host ""
Write-Host "[3/6] Abriendo tunel del backend (puerto $BACKEND_PORT)..." -ForegroundColor Cyan
$backendTunnel = Start-Tunnel -Port $BACKEND_PORT -Label "backend"
$backendUrl    = Wait-ForTunnelUrl $backendTunnel.Log

if (-not $backendUrl) {
    Write-Host "ERROR: No se pudo obtener la URL del backend." -ForegroundColor Red
    Write-Host "       Verifica que cloudflared este instalado: cloudflared --version" -ForegroundColor Red
    exit 1
}
Write-Host "   $backendUrl" -ForegroundColor Green

# ── PASO 4: Reconstruir frontend con la URL publica del backend ───────────────
Write-Host ""
Write-Host "[4/6] Construyendo frontend con URL del backend..." -ForegroundColor Cyan
Write-Host "      (puede tardar 1-2 minutos)" -ForegroundColor Gray

@"
services:
  frontend:
    build:
      args:
        - VITE_API_URL=$backendUrl
"@ | Set-Content "docker-compose.override.yml"

docker compose --env-file $EnvFile up -d --build frontend
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo el build del frontend." -ForegroundColor Red
    exit 1
}
Start-Sleep -Seconds 3

# ── PASO 5: Tunel del frontend ────────────────────────────────────────────────
Write-Host ""
Write-Host "[5/6] Abriendo tunel del frontend (puerto $FRONTEND_PORT)..." -ForegroundColor Cyan
$frontendTunnel = Start-Tunnel -Port $FRONTEND_PORT -Label "frontend"
$frontendUrl    = Wait-ForTunnelUrl $frontendTunnel.Log

if (-not $frontendUrl) {
    Write-Host "ERROR: No se pudo obtener la URL del frontend." -ForegroundColor Red
    exit 1
}
Write-Host "   $frontendUrl" -ForegroundColor Green

# ── PASO 6: Actualizar CORS del backend con la URL publica del frontend ───────
Write-Host ""
Write-Host "[6/6] Actualizando CORS y reiniciando backend..." -ForegroundColor Cyan

@"
services:
  frontend:
    build:
      args:
        - VITE_API_URL=$backendUrl
  backend:
    environment:
      - CORS_EXTRA_ORIGINS=http://localhost:$AGENCY_PORT,http://localhost:$AIRLINE_PORT,$frontendUrl
"@ | Set-Content "docker-compose.override.yml"

docker compose --env-file $EnvFile up -d backend

# ── Resultado ─────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "=============================================" -ForegroundColor DarkGreen
Write-Host "  Todo listo! (construido desde cero)" -ForegroundColor Green
Write-Host ""
Write-Host "  Comparte este enlace:" -ForegroundColor White
Write-Host "  $frontendUrl" -ForegroundColor Cyan
Write-Host ""
Write-Host "  (La URL cambia si reinicias este script)" -ForegroundColor Gray
Write-Host "=============================================" -ForegroundColor DarkGreen
Write-Host ""
Write-Host "  Presiona Ctrl+C para cerrar los tuneles" -ForegroundColor Gray
Write-Host ""

# ── Mantener el script vivo; limpiar al salir ─────────────────────────────────
try {
    while ($true) { Start-Sleep -Seconds 5 }
}
finally {
    Write-Host ""
    Write-Host "Cerrando tuneles y limpiando..." -ForegroundColor Yellow
    $backendTunnel.Process  | Stop-Process -ErrorAction SilentlyContinue
    $frontendTunnel.Process | Stop-Process -ErrorAction SilentlyContinue
    docker compose --env-file $EnvFile down --volumes --remove-orphans
    Remove-Item "docker-compose.override.yml" -ErrorAction SilentlyContinue
    Write-Host "Listo. Hasta luego." -ForegroundColor Gray
}