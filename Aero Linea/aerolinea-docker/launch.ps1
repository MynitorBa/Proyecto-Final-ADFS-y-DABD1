param(
    [string]$Action = "up",
    [int]$N = 3
)

# --- CONFIGURACION ------------------------------------------------
$DB_PASSWORD   = "Aerolinea123"
$DB_NAME       = "AerolineaDB"
$AGENCY_PORT   = "5173"
$HOTEL_PORT    = "5174"
$HOTEL_BACKEND = "7000"
$EMAIL_PASS    = "axvv hnkv gylv gupb"

function Write-Info   { param($m) Write-Host "[INFO]  $m" -ForegroundColor Cyan }
function Write-Ok     { param($m) Write-Host "[OK]    $m" -ForegroundColor Green }
function Write-Skip   { param($m) Write-Host "[SKIP]  $m" -ForegroundColor DarkGray }
function Write-Warn   { param($m) Write-Host "[WARN]  $m" -ForegroundColor Yellow }
function Write-Err    { param($m) Write-Host "[ERROR] $m" -ForegroundColor Red }
function Write-Header { param($m) Write-Host "`n=== $m ===" -ForegroundColor Cyan }

function Test-Deps {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Err "Docker no esta instalado o no esta en el PATH"
        exit 1
    }
}

# Verifica si el backend de la instancia N esta corriendo
function Test-BackendRunning {
    param([int]$n)
    $r = docker ps --format "{{.Names}}" 2>&1 |
         Where-Object { $_ -match "aerolinea-${n}[-_]backend" -or $_ -eq "aerolinea-$n-backend" }
    return ($null -ne $r -and "$r" -ne "")
}

# Verifica si la imagen del backend/frontend existe
function Test-ImageExists {
    param([int]$n, [string]$svc)
    $imgs = docker images --format "{{.Repository}}" 2>&1 |
            Where-Object { $_ -match "aerolinea-$n-$svc" }
    return ($null -ne $imgs -and "$imgs" -ne "")
}

# --- GENERAR .env ------------------------------------------------
function Ensure-EnvFile {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (Test-Path $f) { Write-Skip "$f ya existe"; return }

    $dbPort       = 1432 + $n
    $backendPort  = 5000 + $n
    $frontendPort = 3000 + $n

    @(
        "COMPOSE_PROJECT_NAME=aerolinea-$n",
        "DB_NAME=$DB_NAME",
        "DB_PASSWORD=$DB_PASSWORD",
        "DB_PORT=$dbPort",
        "BACKEND_PORT=$backendPort",
        "FRONTEND_PORT=$frontendPort",
        "AGENCY_PORT=$AGENCY_PORT",
        "HOTEL_PORT=$HOTEL_PORT",
        "HOTEL_BACKEND_PORT=$HOTEL_BACKEND",
        "",
        "# Credencial SMTP para envio de correos",
        "EMAIL_PASSWORD=$EMAIL_PASS"
    ) | Set-Content -Path $f -Encoding UTF8

    Write-Ok "Creado $f  (backend:$backendPort  frontend:$frontendPort  db:$dbPort)"
}

# --- LEVANTAR -----------------------------------------------------
function Ensure-Up {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Err "No existe $f"; return }
    if (Test-BackendRunning $n) { Write-Skip "aerolinea-$n ya esta corriendo"; return }

    Write-Info "Levantando aerolinea-$n..."
    $needsBuild = (-not (Test-ImageExists $n "backend")) -or (-not (Test-ImageExists $n "frontend"))
    if ($needsBuild) {
        docker-compose --env-file $f -p "aerolinea-$n" up -d --build 2>&1 | Select-Object -Last 8
    } else {
        docker-compose --env-file $f -p "aerolinea-$n" up -d 2>&1 | Select-Object -Last 8
    }

    # Polling: esperar hasta 90s a que el backend este corriendo
    Write-Info "Esperando que aerolinea-$n este lista..."
    $timeout = 90
    $elapsed = 0
    $interval = 5
    while ($elapsed -lt $timeout) {
        Start-Sleep -Seconds $interval
        $elapsed += $interval
        if (Test-BackendRunning $n) { break }
        Write-Info "  ... ${elapsed}s / ${timeout}s"
    }

    if (Test-BackendRunning $n) {
        Write-Ok "aerolinea-$n activo  ->  http://localhost:$( 3000 + $n )"
    } else {
        Write-Err "aerolinea-$n no pudo levantarse en ${timeout}s - revisa: .\launch.ps1 logs-backend $n"
    }
}

# --- BAJAR --------------------------------------------------------
function Stop-Instance {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    if (-not (Test-BackendRunning $n)) { Write-Skip "aerolinea-$n ya estaba detenida"; return }
    Write-Info "Bajando aerolinea-$n..."
    docker-compose --env-file $f -p "aerolinea-$n" down 2>&1 | Select-Object -Last 3
    Write-Ok "aerolinea-$n detenida"
}

# --- REBUILDS -----------------------------------------------------
function Rebuild-Frontend {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild FRONTEND aerolinea-$n..."
    docker-compose --env-file $f -p "aerolinea-$n" down 2>&1 | Out-Null
    docker rmi "aerolinea-$n-frontend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "aerolinea-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Frontend aerolinea-$n reconstruido"
}

function Rebuild-Backend {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild BACKEND aerolinea-$n..."
    docker-compose --env-file $f -p "aerolinea-$n" down 2>&1 | Out-Null
    docker rmi "aerolinea-$n-backend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "aerolinea-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Backend aerolinea-$n reconstruido"
}

function Rebuild-All {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild COMPLETO aerolinea-$n..."
    docker-compose --env-file $f -p "aerolinea-$n" down 2>&1 | Out-Null
    docker rmi "aerolinea-$n-frontend" "aerolinea-$n-backend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "aerolinea-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Rebuild completo aerolinea-$n listo"
}

function Rebuild-Compose {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Recreando aerolinea-$n (cambio en compose/.env)..."
    docker-compose --env-file $f -p "aerolinea-$n" down 2>&1 | Out-Null
    docker-compose --env-file $f -p "aerolinea-$n" up -d --force-recreate 2>&1 | Select-Object -Last 4
    Write-Ok "aerolinea-$n recreada"
}

function Rebuild-NoCache {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild SIN CACHE aerolinea-$n..."
    docker-compose --env-file $f -p "aerolinea-$n" down 2>&1 | Out-Null
    docker-compose --env-file $f -p "aerolinea-$n" build --no-cache 2>&1 | Select-Object -Last 5
    docker-compose --env-file $f -p "aerolinea-$n" up -d 2>&1 | Select-Object -Last 4
    Write-Ok "Rebuild sin cache aerolinea-$n listo"
}

# --- NUKE ---------------------------------------------------------
function Nuke-Instance {
    param([int]$n)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Warn "Eliminando contenedores, imagenes Y VOLUMEN DB de aerolinea-$n..."
    docker-compose --env-file $f -p "aerolinea-$n" down --rmi all --volumes 2>&1 | Select-Object -Last 5
    # BUGFIX: docker-compose down --volumes NO borra volumenes con nombre explicito (name:).
    # Hay que eliminarlo directamente por el nombre calculado.
    $volName = "aerolinea-$n-db-data"
    $volExists = docker volume ls --format "{{.Name}}" 2>&1 | Where-Object { $_ -eq $volName }
    if ($volExists) {
        Write-Warn "Eliminando volumen nombrado: $volName"
        docker volume rm $volName -f 2>&1 | Out-Null
        Write-Ok "Volumen $volName eliminado"
    }
    Write-Ok "aerolinea-$n eliminada completamente (incluye datos de DB)"
}

# --- DESTROY ALL --------------------------------------------------
# Elimina ABSOLUTAMENTE TODO lo relacionado con aerolineas:
# contenedores, imagenes, volumenes de DB y archivos .env.
# Escanea instancias 1..20 para no dejar nada huerfano.
function Destroy-All {
    Write-Host ""
    Write-Host "╔══════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "║              ⚠  DESTROY ALL - AEROLINEAS  ⚠              ║" -ForegroundColor Red
    Write-Host "║                                                          ║" -ForegroundColor Red
    Write-Host "║  Esto eliminara SIN RECUPERACION:                        ║" -ForegroundColor Red
    Write-Host "║   • Todos los contenedores  (db, backend, frontend)      ║" -ForegroundColor Red
    Write-Host "║   • Todas las imagenes Docker de aerolinea               ║" -ForegroundColor Red
    Write-Host "║   • Todos los volumenes de SQL Server (datos de BD)      ║" -ForegroundColor Red
    Write-Host "║   • Todos los archivos .env.aerolineaN                   ║" -ForegroundColor Red
    Write-Host "╚══════════════════════════════════════════════════════════╝" -ForegroundColor Red
    Write-Host ""
    $confirm = Read-Host "Escribi DESTROY para confirmar"
    if ($confirm -ne "DESTROY") { Write-Info "Cancelado. No se elimino nada."; return }

    Write-Header "FASE 1 — Bajar y eliminar stacks con .env conocidos (1..20)"
    for ($i = 1; $i -le 20; $i++) {
        $f = ".env.aerolinea$i"
        if (Test-Path $f) {
            Write-Info "Eliminando stack aerolinea-$i via $f..."
            docker-compose --env-file $f -p "aerolinea-$i" down --rmi all --volumes 2>&1 | Select-Object -Last 3
            Write-Ok "Stack aerolinea-$i eliminado"
        }
    }

    Write-Header "FASE 2 — Forzar eliminacion de contenedores huerfanos"
    $containers = docker ps -a --format "{{.Names}}" 2>&1 |
                  Where-Object { $_ -match "^aerolinea-\d" }
    if ($containers) {
        $containers | ForEach-Object {
            Write-Warn "Eliminando contenedor huerfano: $_"
            docker rm -f $_ 2>&1 | Out-Null
        }
        Write-Ok "Contenedores huerfanos eliminados"
    } else {
        Write-Skip "No quedan contenedores aerolinea huerfanos"
    }

    Write-Header "FASE 3 — Eliminar imagenes Docker de aerolinea"
    $images = docker images --format "{{.Repository}}:{{.Tag}}" 2>&1 |
              Where-Object { $_ -match "aerolinea" }
    if ($images) {
        $images | ForEach-Object {
            Write-Warn "Eliminando imagen: $_"
            docker rmi -f $_ 2>&1 | Out-Null
        }
        Write-Ok "Imagenes eliminadas"
    } else {
        Write-Skip "No quedan imagenes aerolinea"
    }

    Write-Header "FASE 4 — Eliminar volumenes Docker de aerolinea"
    # Primero: forzar eliminacion por nombre calculado (fix: compose no borra volumenes con name: explicito)
    for ($i = 1; $i -le 20; $i++) {
        $volName = "aerolinea-$i-db-data"
        $volExists = docker volume ls --format "{{.Name}}" 2>&1 | Where-Object { $_ -eq $volName }
        if ($volExists) {
            Write-Warn "Eliminando volumen nombrado: $volName"
            docker volume rm $volName -f 2>&1 | Out-Null
        }
    }
    # Segundo: barrer cualquier volumen aerolinea restante
    $volumes = docker volume ls --format "{{.Name}}" 2>&1 |
               Where-Object { $_ -match "aerolinea" }
    if ($volumes) {
        $volumes | ForEach-Object {
            Write-Warn "Eliminando volumen: $_"
            docker volume rm -f $_ 2>&1 | Out-Null
        }
        Write-Ok "Volumenes eliminados"
    } else {
        Write-Skip "No quedan volumenes aerolinea"
    }

    Write-Header "FASE 5 — Eliminar archivos .env.aerolineaN"
    $envFiles = Get-ChildItem -Path "." -Filter ".env.aerolinea*" -File -ErrorAction SilentlyContinue
    if ($envFiles) {
        $envFiles | ForEach-Object {
            Write-Warn "Eliminando $_"
            Remove-Item $_.FullName -Force
        }
        Write-Ok "Archivos .env eliminados"
    } else {
        Write-Skip "No se encontraron archivos .env.aerolineaN"
    }

    Write-Host ""
    Write-Host "✅ DESTROY ALL completado." -ForegroundColor Green
    Write-Host "   No queda ningun rastro de aerolineas en Docker ni en disco." -ForegroundColor Green
    Write-Host "   Para volver a levantar: .\launch.ps1 up [N]" -ForegroundColor Cyan
    Write-Host ""
}

# --- LOGS ---------------------------------------------------------
function Show-Logs {
    param([int]$n, [string]$svc = "backend", [int]$lines = 40)
    $f = ".env.aerolinea$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Header "Logs $svc aerolinea-$n (ultimas $lines lineas)"
    docker-compose --env-file $f -p "aerolinea-$n" logs $svc --tail $lines 2>&1
}

# --- ENV CHECK ----------------------------------------------------
function Show-EnvCheck {
    param([int]$n)
    $cname = "aerolinea-$n-backend"
    Write-Header "Variables de entorno en $cname"
    docker exec $cname printenv 2>&1 |
        Where-Object { $_ -match "(PORT|DB_|CONNECTION|COOKIE|CORS|SERVER|EMAIL|HOTEL|VITE)" }
}

# --- STATUS -------------------------------------------------------
function Show-Status {
    param([int]$total = 3)
    Write-Header "Estado de instancias - Aerolineas"
    Write-Host ("{0,-15} {1,-8} {2,-12} {3,-10} {4,-10} {5}" -f "INSTANCIA","ENV","DOCKER","BACKEND","FRONTEND","URL") -ForegroundColor Cyan
    Write-Host ("-" * 80) -ForegroundColor DarkGray
    for ($i = 1; $i -le $total; $i++) {
        $e  = if (Test-Path ".env.aerolinea$i") { "SI" } else { "NO" }
        $d  = if (Test-BackendRunning $i) { "CORRIENDO" } else { "DETENIDO" }
        $bp = 5000 + $i
        $fp = 3000 + $i
        $u  = if ($d -eq "CORRIENDO") { "http://localhost:$fp" } else { "-" }
        $c  = if ($d -eq "CORRIENDO") { "Green" } else { "Gray" }
        Write-Host ("{0,-15} {1,-8} {2,-12} {3,-10} {4,-10} {5}" -f "aerolinea-$i",$e,$d,":$bp",":$fp",$u) -ForegroundColor $c
    }
    Write-Host ""
}

# --- HELP ---------------------------------------------------------
function Show-Help {
    Write-Host "`n=== GUIA DE COMANDOS - AEROLINEAS DOCKER ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "GESTION BASICA:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 up [N]                  Levanta N aerolineas"
    Write-Host "  .\launch.ps1 down [N]                Baja N aerolineas (conserva DB)"
    Write-Host "  .\launch.ps1 status [N]              Tabla de estado"
    Write-Host ""
    Write-Host "REBUILD:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 rebuild-frontend [N]    Rebuild solo frontend"
    Write-Host "  .\launch.ps1 rebuild-backend [N]     Rebuild solo backend"
    Write-Host "  .\launch.ps1 rebuild-all [N]         Rebuild frontend + backend"
    Write-Host "  .\launch.ps1 rebuild-compose [N]     Recrea contenedores (cambio en .env/compose)"
    Write-Host "  .\launch.ps1 rebuild-nocache [N]     Rebuild sin cache"
    Write-Host ""
    Write-Host "LOGS Y DIAGNOSTICO:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 logs-backend [N]        Logs del backend"
    Write-Host "  .\launch.ps1 logs-frontend [N]       Logs del frontend"
    Write-Host "  .\launch.ps1 logs-db [N]             Logs de SQL Server"
    Write-Host "  .\launch.ps1 logs-db-init [N]        Logs del init de BD (schema+seed)"
    Write-Host "  .\launch.ps1 envcheck [N]            Variables de entorno en backend"
    Write-Host ""
    Write-Host "PELIGROSO:" -ForegroundColor Red
    Write-Host "  .\launch.ps1 nuke [N]                Elimina TODO incluyendo datos de DB"
    Write-Host "  .\launch.ps1 destroy-all             Elimina ABSOLUTAMENTE TODO (todas las instancias)"
    Write-Host "  .\launch.ps1 clean-images            Elimina imagenes huerfanas"
    Write-Host ""
    Write-Host "EJEMPLOS:" -ForegroundColor Green
    Write-Host "  .\launch.ps1 up 3                    Levanta 3 aerolineas"
    Write-Host "  .\launch.ps1 rebuild-backend 2       Rebuild backend aerolineas 1 y 2"
    Write-Host "  .\launch.ps1 logs-db-init 1          Ver si el schema se aplico bien"
    Write-Host "  .\launch.ps1 status 3                Ver estado de 3 instancias"
    Write-Host ""
    Write-Host "PUERTOS POR INSTANCIA:" -ForegroundColor Yellow
    Write-Host "  Aerolinea N  ->  Frontend: 300N  Backend: 500N  DB: 1432+N"
    Write-Host ""
}

# -------------------------------------------------------------------
# MAIN
# -------------------------------------------------------------------
switch ($Action) {

    "up" {
        Write-Header "Levantando $N instancias de aerolinea"
        Test-Deps
        $started = 0; $skipped = 0
        for ($i = 1; $i -le $N; $i++) {
            Write-Host ""
            Write-Info "--- Aerolinea $i / $N ---"
            Ensure-EnvFile $i
            $wasUp = Test-BackendRunning $i
            Ensure-Up $i
            if ($wasUp) { $skipped++ } else { $started++ }
        }
        Write-Host ""
        Show-Status $N
        Write-Ok "$N aerolineas: $started iniciadas, $skipped ya corriendo"
        if ($started -gt 0) {
            Write-Info "Nota: el db-init puede tardar ~30s en aplicar schema+seed la primera vez."
            Write-Info "Si el backend da error de DB, espera un momento y revisa: .\launch.ps1 logs-db-init 1"
        }
    }

    "down" {
        Write-Header "Bajando $N instancias"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Stop-Instance $i }
        Show-Status $N
    }

    "status" {
        Test-Deps; Show-Status $N
    }

    "rebuild-frontend" {
        Write-Header "Rebuild FRONTEND - $N aerolineas"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-Frontend $i }
        Show-Status $N
    }

    "rebuild-backend" {
        Write-Header "Rebuild BACKEND - $N aerolineas"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-Backend $i }
        Show-Status $N
    }

    "rebuild-all" {
        Write-Header "Rebuild COMPLETO - $N aerolineas"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-All $i }
        Show-Status $N
    }

    "rebuild-compose" {
        Write-Header "Recrear contenedores - $N aerolineas"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-Compose $i }
        Show-Status $N
    }

    "rebuild-nocache" {
        Write-Header "Rebuild SIN CACHE - $N aerolineas"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-NoCache $i }
        Show-Status $N
    }

    "logs-backend" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-Logs $i "backend" 40 }
    }

    "logs-frontend" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-Logs $i "frontend" 30 }
    }

    "logs-db" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-Logs $i "db" 30 }
    }

    "logs-db-init" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-Logs $i "db-init" 50 }
    }

    "envcheck" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-EnvCheck $i }
    }

    "nuke" {
        Write-Warn "ADVERTENCIA: Esto elimina contenedores, imagenes Y DATOS DE BD de $N aerolineas."
        $confirm = Read-Host "Escribi SI para confirmar"
        if ($confirm -eq "SI") {
            for ($i = 1; $i -le $N; $i++) { Nuke-Instance $i }
            Write-Ok "Nuke completado para $N aerolineas"
        } else { Write-Info "Cancelado." }
    }

    "up-new" {
        Write-Header "REINICIO COMPLETO de $N aerolineas (Docker + BD + .env)"
        Write-Warn "Esto elimina contenedores, imagenes, DATOS DE SQL SERVER y archivos .env de $N aerolineas."
        Write-Warn "Se recreara todo desde cero incluyendo schema y seed. Los datos se PERDERAN."
        $confirm = Read-Host "Escribi REINICIAR para confirmar"
        if ($confirm -ne "REINICIAR") { Write-Info "Cancelado."; break }
        Test-Deps
        Write-Header "PASO 1: Bajando y eliminando Docker + BD"
        for ($i = 1; $i -le $N; $i++) {
            Write-Info "Eliminando aerolinea-$i (contenedores + imagenes + volumen DB)..."
            $f = ".env.aerolinea$i"
            if (Test-Path $f) {
                docker-compose --env-file $f -p "aerolinea-$i" down --rmi all --volumes 2>&1 | Select-Object -Last 3
            } else {
                docker-compose -p "aerolinea-$i" down --rmi all --volumes 2>&1 | Out-Null
            }
            # BUGFIX: eliminar volumen nombrado explicito que compose no borra solo
            $volName = "aerolinea-$i-db-data"
            $volExists = docker volume ls --format "{{.Name}}" 2>&1 | Where-Object { $_ -eq $volName }
            if ($volExists) {
                Write-Warn "Forzando eliminacion de volumen: $volName"
                docker volume rm $volName -f 2>&1 | Out-Null
            }
            Write-Ok "aerolinea-$i eliminada completamente"
        }
        Write-Header "PASO 2: Eliminando archivos .env"
        for ($i = 1; $i -le $N; $i++) {
            $f = ".env.aerolinea$i"
            if (Test-Path $f) { Remove-Item $f -Force; Write-Ok "Eliminado $f" }
            else { Write-Skip "$f no existia" }
        }
        Write-Header "PASO 3: Recreando todo desde cero"
        $started = 0
        for ($i = 1; $i -le $N; $i++) {
            Write-Host ""
            Write-Info "--- Aerolinea $i / $N ---"
            Ensure-EnvFile $i
            Ensure-Up $i
            $started++
        }
        Write-Host ""
        Show-Status $N
        Write-Ok "Reinicio completo: $started aerolineas recreadas desde cero"
        Write-Info "Nota: el db-init aplica schema+seed automaticamente al arrancar."
        Write-Info "Si el backend da error al inicio, espera 30s y revisa: .\launch.ps1 logs-db-init 1"
    }

    "destroy-all" {
        Test-Deps
        Destroy-All
    }

    "clean-images" {
        Write-Info "Eliminando imagenes huerfanas..."
        docker image prune -f
        Write-Host ""
        Write-Info "Imagenes de aerolinea actuales:"
        docker images 2>&1 | Select-String "aerolinea"
    }

    "help"    { Show-Help }
    default   { Show-Help }
}