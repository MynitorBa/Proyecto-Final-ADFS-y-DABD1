param(
    [string]$Action = "up",
    [int]$N = 3
)

# --- CONFIGURACION ------------------------------------------------
$MYSQL_HOST     = "localhost"
$MYSQL_PORT     = "3306"
$MYSQL_ROOT     = "root"
$SQL_FILE       = "agencia_viajes.sql"

$SMTP_HOST      = "smtp.gmail.com"
$SMTP_PORT      = "587"
$SMTP_USER      = "distribuidorapine@gmail.com"
$SMTP_PASS      = "axvv hnkv gylv gupb"
$SMTP_FROM      = "MOVENT <distribuidorapine@gmail.com>"
$EMAIL_PASS     = "axvv hnkv gylv gupb"
$EMAIL_SENDER   = "distribuidorapine@gmail.com"
$RECAPTCHA_KEY  = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe"
$AIRLINE_BACKEND= "http://localhost:5001"
$HOTEL_BACKEND  = "http://localhost:7001"

function Write-Info   { param($m) Write-Host "[INFO]  $m" -ForegroundColor Cyan }
function Write-Ok     { param($m) Write-Host "[OK]    $m" -ForegroundColor Green }
function Write-Skip   { param($m) Write-Host "[SKIP]  $m" -ForegroundColor DarkGray }
function Write-Warn   { param($m) Write-Host "[WARN]  $m" -ForegroundColor Yellow }
function Write-Err    { param($m) Write-Host "[ERROR] $m" -ForegroundColor Red }
function Write-Header { param($m) Write-Host "`n=== $m ===" -ForegroundColor Cyan }

function Test-Deps {
    # Agregar XAMPP al PATH automaticamente si mysql no esta disponible
    if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
        $xamppPath = "C:\xampp\mysql\bin"
        if (Test-Path $xamppPath) {
            $env:PATH += ";$xamppPath"
            Write-Info "XAMPP MySQL agregado al PATH automaticamente"
        }
    }

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Err "Docker no esta instalado"; exit 1
    }
    $script:HasMysql = [bool](Get-Command mysql -ErrorAction SilentlyContinue)
    if (-not $script:HasMysql) {
        Write-Warn "mysql no encontrado - configura MySQL manualmente"
    }
}

# Conexion sin contrasena (XAMPP default)
function Get-MysqlArgs {
    return @("-u$MYSQL_ROOT", "-h", $MYSQL_HOST, "-P", $MYSQL_PORT, "--batch", "--silent")
}

function Test-DockerRunning {
    param([int]$n)
    $r = docker ps --format "{{.Names}}" 2>&1 |
         Where-Object { $_ -match "agencia-${n}[-_]" -or $_ -eq "agencia-$n" }
    return ($null -ne $r -and "$r" -ne "")
}

function Test-DockerImageExists {
    param([int]$n)
    $imgs = docker images --format "{{.Repository}}" 2>&1 |
            Where-Object { $_ -match "agencia-$n" }
    return ($null -ne $imgs -and "$imgs" -ne "")
}

function Test-MySQLDatabase {
    param([int]$n)
    if (-not $script:HasMysql) { return $false }
    $dbName = "agencia_viajes_$n"
    $args   = Get-MysqlArgs
    $result = $null
    try {
        $result = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$dbName';" |
                  & mysql @args 2>&1
    } catch { return $false }
    return ("$result" -match $dbName)
}

# --- GENERAR .env SIN CONTRASENA ---------------------------------
function Ensure-EnvFile {
    param([int]$n)
    $f = ".env.agencia$n"
    if (Test-Path $f) { Write-Skip "$f ya existe"; return }

    $backendPort  = 8000 + $n
    $frontendPort = 6000 + $n
    $dbName       = "agencia_viajes_$n"

    @(
        "COMPOSE_PROJECT_NAME=agencia-$n",
        "",
        "# BASE DE DATOS (MySQL en el Host - sin contrasena)",
        "DB_HOST=host.docker.internal",
        "DB_PORT=$MYSQL_PORT",
        "DB_NAME=$dbName",
        "DB_USER=root",
        "DB_PASSWORD=",
        "",
        "# PUERTOS",
        "BACKEND_PORT=$backendPort",
        "FRONTEND_PORT=$frontendPort",
        "",
        "# SERVIDOR",
        "SERVER_PORT=8080",
        "SERVER_URL=http://localhost:$backendPort",
        "",
        "# SEGURIDAD Y SESION",
        "JWT_SECRET=MoventAgenciaViajes2026_SecretKey_Instancia${n}_x7k9p2mnbvcxzqwerty",
        "COOKIE_NAME=agencia_session_$n",
        "",
        "# CORS",
        "ALLOWED_ORIGIN=http://localhost:$frontendPort",
        "",
        "# URLS DE SISTEMAS EXTERNOS",
        "AIRLINE_BACKEND_URL=$AIRLINE_BACKEND",
        "HOTEL_BACKEND_URL=$HOTEL_BACKEND",
        "",
        "# CONFIGURACION DE CORREO (SMTP)",
        "SMTP_HOST=$SMTP_HOST",
        "SMTP_PORT=$SMTP_PORT",
        "SMTP_USER=$SMTP_USER",
        "SMTP_PASS=$SMTP_PASS",
        "SMTP_FROM=$SMTP_FROM",
        "EMAIL_PASSWORD=$EMAIL_PASS",
        "EMAIL_SENDER=$EMAIL_SENDER",
        "",
        "# RECAPTCHA",
        "RECAPTCHA_SECRET_KEY=$RECAPTCHA_KEY",
        "",
        "ENV=development"
    ) | Set-Content -Path $f -Encoding UTF8

    Write-Ok "Creado $f  (backend:$backendPort  frontend:$frontendPort  db:$dbName)"
}

# --- MYSQL: crear BD y aplicar schema ----------------------------
function Ensure-MySQLDatabase {
    param([int]$n)
    if (-not $script:HasMysql) {
        Write-Skip "MySQL: sin cliente disponible"
        return
    }

    $dbName = "agencia_viajes_$n"
    $args   = Get-MysqlArgs

    if (Test-MySQLDatabase $n) {
        Write-Skip "BD MySQL '$dbName' ya existe"
        return
    }

    # PASO 1: crear base de datos (simple, sin usuario extra, usa root)
    Write-Info "Creando BD '$dbName'..."
    $out1 = "CREATE DATABASE IF NOT EXISTS ``$dbName`` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" |
            & mysql @args 2>&1
    if ("$out1" -match "ERROR") {
        Write-Err "Error creando BD: $out1"
        return
    }
    Write-Ok "BD '$dbName' creada"

    # PASO 2: aplicar schema
    if (Test-Path $SQL_FILE) {
        Write-Info "Aplicando schema desde $SQL_FILE..."
        $schemaArgs = $args + @($dbName)
        $out2 = Get-Content $SQL_FILE -Raw | & mysql @schemaArgs 2>&1
        if ("$out2" -match "ERROR") {
            Write-Err "Error en schema: $out2"
        } else {
            Write-Ok "Schema aplicado en '$dbName'"
        }
    } else {
        Write-Warn "No se encontro $SQL_FILE - aplica el schema manualmente"
    }
}

# --- DOCKER -------------------------------------------------------
function Ensure-Up {
    param([int]$n)
    $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Err "No existe $f"; return }
    if (Test-DockerRunning $n) { Write-Skip "agencia-$n ya esta corriendo"; return }
    Write-Info "Levantando agencia-$n..."
    if (-not (Test-DockerImageExists $n)) {
        docker-compose --env-file $f -p "agencia-$n" up -d --build 2>&1 | Select-Object -Last 5
    } else {
        docker-compose --env-file $f -p "agencia-$n" up -d 2>&1 | Select-Object -Last 5
    }
    Start-Sleep -Seconds 3
    if (Test-DockerRunning $n) {
        Write-Ok "agencia-$n activa  ->  http://localhost:$( 6000 + $n )"
    } else {
        Write-Err "agencia-$n no pudo levantarse - revisa: .\launch.ps1 logs-backend $n"
    }
}

function Stop-Instance {
    param([int]$n)
    $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    if (-not (Test-DockerRunning $n)) { Write-Skip "agencia-$n ya estaba detenida"; return }
    Write-Info "Bajando agencia-$n..."
    docker-compose --env-file $f -p "agencia-$n" down 2>&1 | Select-Object -Last 3
    Write-Ok "agencia-$n detenida"
}

function Rebuild-Frontend {
    param([int]$n); $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild FRONTEND agencia-$n..."
    docker-compose --env-file $f -p "agencia-$n" down 2>&1 | Out-Null
    docker rmi "agencia-$n-frontend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "agencia-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Frontend agencia-$n listo"
}

function Rebuild-Backend {
    param([int]$n); $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild BACKEND agencia-$n..."
    docker-compose --env-file $f -p "agencia-$n" down 2>&1 | Out-Null
    docker rmi "agencia-$n-backend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "agencia-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Backend agencia-$n listo"
}

function Rebuild-All {
    param([int]$n); $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild COMPLETO agencia-$n..."
    docker-compose --env-file $f -p "agencia-$n" down 2>&1 | Out-Null
    docker rmi "agencia-$n-frontend" "agencia-$n-backend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "agencia-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Rebuild completo agencia-$n listo"
}

function Rebuild-Compose {
    param([int]$n); $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Recreando agencia-$n..."
    docker-compose --env-file $f -p "agencia-$n" down 2>&1 | Out-Null
    docker-compose --env-file $f -p "agencia-$n" up -d --force-recreate 2>&1 | Select-Object -Last 4
    Write-Ok "agencia-$n recreada"
}

function Rebuild-NoCache {
    param([int]$n); $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Info "Rebuild SIN CACHE agencia-$n..."
    docker-compose --env-file $f -p "agencia-$n" down 2>&1 | Out-Null
    docker-compose --env-file $f -p "agencia-$n" build --no-cache 2>&1 | Select-Object -Last 5
    docker-compose --env-file $f -p "agencia-$n" up -d 2>&1 | Select-Object -Last 4
    Write-Ok "agencia-$n sin cache lista"
}

function Nuke-Instance {
    param([int]$n); $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Warn "Eliminando Docker de agencia-$n (MySQL no se toca)..."
    docker-compose --env-file $f -p "agencia-$n" down --rmi all 2>&1 | Select-Object -Last 4
    Write-Ok "agencia-$n eliminada de Docker"
}

function Show-Logs {
    param([int]$n, [string]$svc = "backend", [int]$lines = 40)
    $f = ".env.agencia$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe $f"; return }
    Write-Header "Logs $svc agencia-$n"
    docker-compose --env-file $f -p "agencia-$n" logs $svc --tail $lines 2>&1
}

function Show-EnvCheck {
    param([int]$n)
    Write-Header "Env agencia-$n-backend"
    docker exec "agencia-$n-backend" printenv 2>&1 |
        Where-Object { $_ -match "(PORT|DB_|JWT|COOKIE|ORIGIN|AIRLINE|HOTEL|SMTP|EMAIL|SERVER)" }
}

function Show-Status {
    param([int]$total = 3)
    Write-Header "Estado - Agencias"
    Write-Host ("{0,-14} {1,-8} {2,-10} {3,-12} {4}" -f "INSTANCIA","ENV","MYSQL","DOCKER","URL") -ForegroundColor Cyan
    Write-Host ("-" * 65) -ForegroundColor DarkGray
    for ($i = 1; $i -le $total; $i++) {
        $e  = if (Test-Path ".env.agencia$i") { "SI" } else { "NO" }
        $db = if (Test-MySQLDatabase $i)      { "SI" } else { "NO" }
        $d  = if (Test-DockerRunning $i)      { "CORRIENDO" } else { "DETENIDO" }
        $u  = if ($d -eq "CORRIENDO")         { "http://localhost:$( 6000 + $i )" } else { "-" }
        $c  = if ($d -eq "CORRIENDO")         { "Green" } else { "Gray" }
        Write-Host ("{0,-14} {1,-8} {2,-10} {3,-12} {4}" -f "agencia-$i",$e,$db,$d,$u) -ForegroundColor $c
    }
    Write-Host ""
}

function Show-Help {
    Write-Host "`n=== COMANDOS - AGENCIAS DOCKER ===" -ForegroundColor Cyan
    Write-Host "  .\launch.ps1 up [N]                  Levanta N agencias"
    Write-Host "  .\launch.ps1 down [N]                Baja N agencias"
    Write-Host "  .\launch.ps1 status [N]              Tabla de estado"
    Write-Host "  .\launch.ps1 mysql-only [N]          Solo configura MySQL"
    Write-Host "  .\launch.ps1 rebuild-frontend [N]    Rebuild frontend"
    Write-Host "  .\launch.ps1 rebuild-backend [N]     Rebuild backend"
    Write-Host "  .\launch.ps1 rebuild-all [N]         Rebuild completo"
    Write-Host "  .\launch.ps1 rebuild-compose [N]     Recrea contenedores"
    Write-Host "  .\launch.ps1 rebuild-nocache [N]     Rebuild sin cache"
    Write-Host "  .\launch.ps1 logs-backend [N]        Logs backend"
    Write-Host "  .\launch.ps1 logs-frontend [N]       Logs frontend"
    Write-Host "  .\launch.ps1 envcheck [N]            Variables de entorno"
    Write-Host "  .\launch.ps1 nuke [N]                Elimina Docker (MySQL intacto)"
    Write-Host "  .\launch.ps1 destroy-all [N]         DESTRUYE TODO: Docker + MySQL + .env"
    Write-Host "  .\launch.ps1 clean-images            Imagenes huerfanas"
    Write-Host ""
    Write-Host "PUERTOS: Frontend 600N | Backend 800N | MySQL 3306 (compartido)" -ForegroundColor Yellow
    Write-Host "DB: usa root sin contrasena (XAMPP default)" -ForegroundColor Yellow
    Write-Host ""
}

function Destroy-All {
    param([int]$total)
    Write-Host "`n=== DESTRUCCION TOTAL - AGENCIAS ===" -ForegroundColor Red
    Write-Host ""
    Write-Warn "Esta operacion es IRREVERSIBLE y eliminara:"
    Write-Warn "  - Contenedores Docker (backend y frontend) de $total agencias"
    Write-Warn "  - Imagenes Docker de las $total agencias"
    Write-Warn "  - Bases de datos MySQL: agencia_viajes_1 .. agencia_viajes_$total"
    Write-Warn "  - Archivos de entorno: .env.agencia1 .. .env.agencia$total"
    Write-Host ""
    $confirm = Read-Host "Escribi DESTRUIR para confirmar"
    if ($confirm -ne "DESTRUIR") { Write-Info "Cancelado. No se realizo ninguna accion."; return }

    # PASO 1 - Docker: contenedores e imagenes
    Write-Header "PASO 1 / 3 - Eliminando Docker (contenedores + imagenes)"
    for ($i = 1; $i -le $total; $i++) {
        $f = ".env.agencia$i"
        Write-Info "Docker agencia-$i..."
        if (Test-Path $f) {
            docker-compose --env-file $f -p "agencia-$i" down --rmi all --volumes --remove-orphans 2>&1 | Out-Null
        } else {
            # Sin .env intentamos bajar igual usando el project name
            docker-compose -p "agencia-$i" down --rmi all --volumes --remove-orphans 2>&1 | Out-Null
        }
        # Por si quedaron imagenes con nombre explicito
        docker rmi "agencia-$i-backend"  -f 2>&1 | Out-Null
        docker rmi "agencia-$i-frontend" -f 2>&1 | Out-Null
        Write-Ok "Docker agencia-$i eliminado"
    }

    # PASO 2 - MySQL: bases de datos
    Write-Header "PASO 2 / 3 - Eliminando bases de datos MySQL"
    if ($script:HasMysql) {
        $mysqlArgs = Get-MysqlArgs
        for ($i = 1; $i -le $total; $i++) {
            $dbName = "agencia_viajes_$i"
            Write-Info "Eliminando BD '$dbName'..."
            $dropSql = "DROP DATABASE IF EXISTS ``$dbName``; FLUSH PRIVILEGES;"
            $out = $dropSql | & mysql @mysqlArgs 2>&1
            if ("$out" -match "ERROR") {
                Write-Err "Error al eliminar '$dbName': $out"
            } else {
                Write-Ok "BD '$dbName' eliminada"
            }
        }
    } else {
        Write-Warn "Cliente mysql no disponible - elimina las BDs manualmente en XAMPP/phpMyAdmin"
        for ($i = 1; $i -le $total; $i++) {
            Write-Warn "  -> DROP DATABASE IF EXISTS ``agencia_viajes_$i``;"
        }
    }

    # PASO 3 - Archivos .env
    Write-Header "PASO 3 / 3 - Eliminando archivos .env"
    for ($i = 1; $i -le $total; $i++) {
        $f = ".env.agencia$i"
        if (Test-Path $f) {
            Remove-Item $f -Force
            Write-Ok "Eliminado $f"
        } else {
            Write-Skip "$f no existia"
        }
    }

    Write-Host ""
    Write-Host "=== DESTRUCCION COMPLETA ===" -ForegroundColor Red
    Write-Ok "Se eliminaron $total agencias: Docker, MySQL y .env borrados por completo."
    Write-Host ""
}

# -------------------------------------------------------------------
switch ($Action) {
    "up" {
        Write-Header "Levantando $N agencias"
        Test-Deps
        $started = 0; $skipped = 0
        for ($i = 1; $i -le $N; $i++) {
            Write-Host ""; Write-Info "--- Agencia $i / $N ---"
            Ensure-EnvFile $i
            Ensure-MySQLDatabase $i
            $wasUp = Test-DockerRunning $i
            Ensure-Up $i
            if ($wasUp) { $skipped++ } else { $started++ }
        }
        Write-Host ""; Show-Status $N
        Write-Ok "$N agencias: $started iniciadas, $skipped ya corriendo"
    }
    "down" {
        Write-Header "Bajando $N agencias"; Test-Deps
        for ($i = 1; $i -le $N; $i++) { Stop-Instance $i }
        Show-Status $N
    }
    "status"           { Test-Deps; Show-Status $N }
    "mysql-only"       {
        Write-Header "Configurando MySQL para $N agencias"; Test-Deps
        for ($i = 1; $i -le $N; $i++) { Ensure-EnvFile $i; Ensure-MySQLDatabase $i }
        Write-Ok "MySQL listo para $N agencias"
    }
    "rebuild-frontend" { Write-Header "Rebuild FRONTEND"; Test-Deps; for ($i=1;$i-le $N;$i++){Rebuild-Frontend $i}; Show-Status $N }
    "rebuild-backend"  { Write-Header "Rebuild BACKEND";  Test-Deps; for ($i=1;$i-le $N;$i++){Rebuild-Backend $i};  Show-Status $N }
    "rebuild-all"      { Write-Header "Rebuild COMPLETO"; Test-Deps; for ($i=1;$i-le $N;$i++){Rebuild-All $i};      Show-Status $N }
    "rebuild-compose"  { Write-Header "Recrear contenedores"; Test-Deps; for ($i=1;$i-le $N;$i++){Rebuild-Compose $i}; Show-Status $N }
    "rebuild-nocache"  { Write-Header "Rebuild SIN CACHE"; Test-Deps; for ($i=1;$i-le $N;$i++){Rebuild-NoCache $i}; Show-Status $N }
    "logs-backend"     { Test-Deps; for ($i=1;$i-le $N;$i++){Show-Logs $i "backend" 40} }
    "logs-frontend"    { Test-Deps; for ($i=1;$i-le $N;$i++){Show-Logs $i "frontend" 30} }
    "envcheck"         { Test-Deps; for ($i=1;$i-le $N;$i++){Show-EnvCheck $i} }
    "nuke" {
        Write-Warn "Elimina Docker de $N agencias. MySQL no se toca."
        $confirm = Read-Host "Escribi SI para confirmar"
        if ($confirm -eq "SI") { Test-Deps; for ($i=1;$i-le $N;$i++){Nuke-Instance $i} }
        else { Write-Info "Cancelado." }
    }
    "up-new" {
        Write-Header "REINICIO COMPLETO de $N agencias (Docker + MySQL + .env)"
        Write-Warn "Esto elimina contenedores, imagenes, BASES DE DATOS MySQL y archivos .env de $N agencias."
        Write-Warn "Se recreara todo desde cero. Los datos se PERDERAN."
        $confirm = Read-Host "Escribi REINICIAR para confirmar"
        if ($confirm -ne "REINICIAR") { Write-Info "Cancelado."; break }
        Test-Deps
        Write-Header "PASO 1: Bajando y eliminando Docker"
        for ($i = 1; $i -le $N; $i++) { Nuke-Instance $i }
        Write-Header "PASO 2: Eliminando bases de datos MySQL"
        $args = Get-MysqlArgs
        for ($i = 1; $i -le $N; $i++) {
            $dbName = "agencia_viajes_$i"
            $dbUser = "agencia_user_$i"
            Write-Info "Eliminando BD '$dbName' y usuario '$dbUser'..."
            $dropSql = "DROP DATABASE IF EXISTS ``$dbName``; DROP USER IF EXISTS '${dbUser}'@'%'; FLUSH PRIVILEGES;"
            $dropSql | & mysql @args 2>&1 | Out-Null
            Write-Ok "BD '$dbName' eliminada"
        }
        Write-Header "PASO 3: Eliminando archivos .env"
        for ($i = 1; $i -le $N; $i++) {
            $f = ".env.agencia$i"
            if (Test-Path $f) { Remove-Item $f -Force; Write-Ok "Eliminado $f" }
            else { Write-Skip "$f no existia" }
        }
        Write-Header "PASO 4: Recreando todo desde cero"
        $started = 0
        for ($i = 1; $i -le $N; $i++) {
            Write-Host ""; Write-Info "--- Agencia $i / $N ---"
            Ensure-EnvFile $i
            Ensure-MySQLDatabase $i
            Ensure-Up $i
            $started++
        }
        Write-Host ""; Show-Status $N
        Write-Ok "Reinicio completo: $started agencias recreadas desde cero"
    }
    "destroy-all" {
        Test-Deps
        Destroy-All $N
    }
    "clean-images" {
        docker image prune -f
        docker images 2>&1 | Select-String "agencia"
    }
    default { Show-Help }
}