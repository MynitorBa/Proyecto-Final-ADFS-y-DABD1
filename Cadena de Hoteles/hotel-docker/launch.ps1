param(
    [string]$Action = "up",
    [int]$N = 3
)

$ORACLE_HOST        = "localhost"
$ORACLE_PORT        = "1521"
$ORACLE_SERVICE     = "XEPDB1"
$ORACLE_SYSDBA_USER = "system"
$ORACLE_SYSDBA_PASS = "meme1234"
$ORACLE_BASE_SCHEMA = "system"
$AGENCY_PORT        = "5173"
$AIRLINE_PORT       = "5001"

function Write-Info   { param($m) Write-Host "[INFO]  $m" -ForegroundColor Cyan }
function Write-Ok     { param($m) Write-Host "[OK]    $m" -ForegroundColor Green }
function Write-Skip   { param($m) Write-Host "[SKIP]  $m" -ForegroundColor DarkGray }
function Write-Warn   { param($m) Write-Host "[WARN]  $m" -ForegroundColor Yellow }
function Write-Err    { param($m) Write-Host "[ERROR] $m" -ForegroundColor Red }
function Write-Header { param($m) Write-Host "`n=== $m ===" -ForegroundColor Cyan }

function Get-EnvValue {
    param([int]$n, [string]$key)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { return $null }
    $line = Get-Content $f | Where-Object { $_ -match "^$key=" } | Select-Object -First 1
    if ($line) { return ($line -split "=", 2)[1].Trim() }
    return $null
}

function Test-Deps {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Err "Docker no esta instalado"
        exit 1
    }
    $script:HasSqlplus = [bool](Get-Command sqlplus -ErrorAction SilentlyContinue)
    if (-not $script:HasSqlplus) {
        Write-Warn "sqlplus no encontrado - se omitira Oracle"
    }
}

function Test-DockerRunning {
    param([int]$n)
    $r = docker ps --format "{{.Names}}" 2>&1 |
         Where-Object { $_ -match "hotel-${n}[-_]" -or $_ -eq "hotel-$n" }
    return ($null -ne $r -and "$r" -ne "")
}

function Test-DockerImageExists {
    param([int]$n)
    $imgs = docker images --format "{{.Repository}}" 2>&1 |
            Where-Object { $_ -match "hotel-$n" }
    return ($null -ne $imgs -and "$imgs" -ne "")
}

function Test-OracleUser {
    param([int]$n)
    if (-not $script:HasSqlplus) { return $false }
    $user = "hotel$n"
    $conn = "${ORACLE_SYSDBA_USER}/${ORACLE_SYSDBA_PASS}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SERVICE}"
    $tmp  = "$env:TEMP\chk_$n.sql"
    "SELECT COUNT(*) FROM dba_users WHERE username=UPPER('$user');`nEXIT;" |
        Set-Content -Path $tmp -Encoding ASCII
    $result = & sqlplus -S $conn "@$tmp" 2>&1
    Remove-Item $tmp -ErrorAction SilentlyContinue
    $cnt = $result | Where-Object { $_ -match "^\s*\d+\s*$" } | Select-Object -First 1
    if ($cnt) { return ($cnt.Trim() -ne "0") }
    return $false
}

function Ensure-EnvFile {
    param([int]$n)
    $f = ".env.hotel$n"
    if (Test-Path $f) { Write-Skip ".env.hotel$n ya existe"; return }
    @(
        "COMPOSE_PROJECT_NAME=hotel-$n",
        "",
        "DB_SERVICE=$ORACLE_SERVICE",
        "DB_USER=hotel$n",
        "DB_PASS=pass_hotel$n",
        "",
        "BACKEND_PORT=700$n",
        "FRONTEND_PORT=400$n",
        "",
        "AGENCY_PORT=$AGENCY_PORT",
        "AIRLINE_PORT=$AIRLINE_PORT",
        "",
        "JWT_SECRET=clave_jwt_supersecreta_hotel_instancia_${n}_larga_para_256bits",
        "COOKIE_NAME=auth_token_hotel$n"
    ) | Set-Content -Path $f -Encoding UTF8
    Write-Ok "Creado .env.hotel$n  (backend:700$n  frontend:400$n)"
}

function Ensure-OracleHotel {
    param([int]$n)
    if (-not $script:HasSqlplus) { Write-Skip "Oracle hotel$n - sin sqlplus"; return }
    $user = "hotel$n"
    $pass = Get-EnvValue $n "DB_PASS"
    if (-not $pass) { $pass = "pass_hotel$n" }
    if (Test-OracleUser $n) { Write-Skip "Usuario Oracle '$user' ya existe"; return }
    Write-Info "Configurando Oracle para '$user'..."
    $sysConn  = "${ORACLE_SYSDBA_USER}/${ORACLE_SYSDBA_PASS}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SERVICE}"
    $userConn = "${user}/${pass}@${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_SERVICE}"
    $tmp1 = "$env:TEMP\hotel${n}_s1.sql"
    $tmp2 = "$env:TEMP\hotel${n}_s2.sql"
    $tmp3 = "$env:TEMP\hotel${n}_s3.sql"
    $log  = "$env:TEMP\hotel${n}_oracle.log"

    @"
CREATE USER $user IDENTIFIED BY $pass;
GRANT CONNECT, RESOURCE TO $user;
GRANT UNLIMITED TABLESPACE TO $user;
EXIT;
"@ | Set-Content -Path $tmp1 -Encoding ASCII
    Write-Info "  Paso 1: Creando usuario..."
    $o1 = & sqlplus -S $sysConn "@$tmp1" 2>&1
    $o1 | Add-Content $log
    $o1 | Where-Object { $_ -match "(ORA-|granted|created)" } | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }

    @"
SET SERVEROUTPUT ON SIZE UNLIMITED;
DECLARE
    v_ddl   CLOB;
    v_nuevo CLOB;
    PROCEDURE copiar_schema(p_destino VARCHAR2) IS
    BEGIN
        FOR t IN (SELECT table_name FROM user_tables WHERE table_name NOT LIKE 'BIN`$%' ORDER BY table_name) LOOP
            BEGIN
                EXECUTE IMMEDIATE 'CREATE TABLE '||p_destino||'.'||t.table_name||' AS SELECT * FROM $ORACLE_BASE_SCHEMA.'||t.table_name;
                DBMS_OUTPUT.PUT_LINE('  OK tabla: '||t.table_name);
            EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR tabla '||t.table_name||': '||SQLERRM); END;
        END LOOP;
        FOR s IN (SELECT sequence_name,min_value,max_value,increment_by,cycle_flag,order_flag,cache_size,last_number FROM user_sequences) LOOP
            BEGIN
                EXECUTE IMMEDIATE 'CREATE SEQUENCE '||p_destino||'.'||s.sequence_name||' START WITH '||s.last_number||' INCREMENT BY '||s.increment_by||' MINVALUE '||s.min_value||' MAXVALUE '||s.max_value||CASE WHEN s.cycle_flag='Y' THEN ' CYCLE' ELSE ' NOCYCLE' END||CASE WHEN s.order_flag='Y' THEN ' ORDER' ELSE ' NOORDER' END||' CACHE '||s.cache_size;
                DBMS_OUTPUT.PUT_LINE('  OK seq: '||s.sequence_name);
            EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR seq '||s.sequence_name||': '||SQLERRM); END;
        END LOOP;
        FOR c IN (SELECT c.constraint_name,c.table_name,LISTAGG(cc.column_name,', ')WITHIN GROUP(ORDER BY cc.position) AS cols FROM user_constraints c JOIN user_cons_columns cc ON c.constraint_name=cc.constraint_name WHERE c.constraint_type='P' AND c.table_name NOT LIKE 'BIN`$%' GROUP BY c.constraint_name,c.table_name) LOOP
            BEGIN
                EXECUTE IMMEDIATE 'ALTER TABLE '||p_destino||'.'||c.table_name||' ADD CONSTRAINT '||c.constraint_name||' PRIMARY KEY ('||c.cols||')';
                DBMS_OUTPUT.PUT_LINE('  OK PK: '||c.constraint_name);
            EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR PK '||c.constraint_name||': '||SQLERRM); END;
        END LOOP;
        FOR c IN (SELECT c.constraint_name,c.table_name,LISTAGG(cc.column_name,', ')WITHIN GROUP(ORDER BY cc.position) AS cols FROM user_constraints c JOIN user_cons_columns cc ON c.constraint_name=cc.constraint_name WHERE c.constraint_type='U' AND c.table_name NOT LIKE 'BIN`$%' GROUP BY c.constraint_name,c.table_name) LOOP
            BEGIN
                EXECUTE IMMEDIATE 'ALTER TABLE '||p_destino||'.'||c.table_name||' ADD CONSTRAINT '||c.constraint_name||' UNIQUE ('||c.cols||')';
                DBMS_OUTPUT.PUT_LINE('  OK UK: '||c.constraint_name);
            EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR UK '||c.constraint_name||': '||SQLERRM); END;
        END LOOP;
        FOR c IN (SELECT c.constraint_name,c.table_name,LISTAGG(cc.column_name,', ')WITHIN GROUP(ORDER BY cc.position) AS sc,r.table_name AS rt,LISTAGG(rc.column_name,', ')WITHIN GROUP(ORDER BY rc.position) AS rc2,c.delete_rule AS dr FROM user_constraints c JOIN user_cons_columns cc ON c.constraint_name=cc.constraint_name JOIN user_constraints r ON c.r_constraint_name=r.constraint_name JOIN user_cons_columns rc ON r.constraint_name=rc.constraint_name WHERE c.constraint_type='R' AND c.table_name NOT LIKE 'BIN`$%' GROUP BY c.constraint_name,c.table_name,r.table_name,c.delete_rule) LOOP
            BEGIN
                EXECUTE IMMEDIATE 'ALTER TABLE '||p_destino||'.'||c.table_name||' ADD CONSTRAINT '||c.constraint_name||' FOREIGN KEY ('||c.sc||') REFERENCES '||p_destino||'.'||c.rt||' ('||c.rc2||')'||CASE c.dr WHEN 'CASCADE' THEN ' ON DELETE CASCADE' WHEN 'SET NULL' THEN ' ON DELETE SET NULL' ELSE '' END;
                DBMS_OUTPUT.PUT_LINE('  OK FK: '||c.constraint_name);
            EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR FK '||c.constraint_name||': '||SQLERRM); END;
        END LOOP;
        FOR tr IN (SELECT trigger_name FROM user_triggers WHERE table_name NOT LIKE 'BIN`$%') LOOP
            BEGIN
                v_ddl := DBMS_METADATA.GET_DDL('TRIGGER',tr.trigger_name,'SYSTEM');
                v_nuevo := REPLACE(v_ddl,'"SYSTEM".','"'||UPPER(p_destino)||'".');
                EXECUTE IMMEDIATE v_nuevo;
                DBMS_OUTPUT.PUT_LINE('  OK trigger: '||tr.trigger_name);
            EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR trigger '||tr.trigger_name||': '||SQLERRM); END;
        END LOOP;
        DBMS_OUTPUT.PUT_LINE('Listo: '||p_destino);
    END copiar_schema;
BEGIN copiar_schema('$user'); END;
/
EXIT;
"@ | Set-Content -Path $tmp2 -Encoding ASCII
    Write-Info "  Paso 2: Copiando schema..."
    $o2 = & sqlplus -S $sysConn "@$tmp2" 2>&1
    $o2 | Add-Content $log
    $o2 | Where-Object { $_ -match "(OK|ERR|Listo)" } | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }

    @"
SET SERVEROUTPUT ON SIZE UNLIMITED;
BEGIN
    FOR t IN (SELECT DISTINCT c.table_name FROM user_tab_columns c JOIN user_tables u ON c.table_name=u.table_name WHERE c.column_name='ID' AND u.table_name NOT LIKE 'BIN`$%' ORDER BY c.table_name) LOOP
        DECLARE v_seq VARCHAR2(30); v_trg VARCHAR2(30); v_ex NUMBER; BEGIN
            v_seq:='SEQ_'||SUBSTR(t.table_name,1,26); v_trg:='TRG_'||SUBSTR(t.table_name,1,26);
            SELECT COUNT(*) INTO v_ex FROM user_sequences WHERE sequence_name=v_seq;
            IF v_ex=0 THEN DECLARE v_max NUMBER:=1; BEGIN
                EXECUTE IMMEDIATE 'SELECT NVL(MAX(ID),0)+1 FROM '||t.table_name INTO v_max;
                EXECUTE IMMEDIATE 'CREATE SEQUENCE '||v_seq||' START WITH '||v_max||' INCREMENT BY 1 NOCACHE NOCYCLE';
                DBMS_OUTPUT.PUT_LINE('  OK seq: '||v_seq||' start='||v_max);
            END; ELSE DBMS_OUTPUT.PUT_LINE('  YA EXISTE: '||v_seq); END IF;
            EXECUTE IMMEDIATE 'CREATE OR REPLACE TRIGGER '||v_trg||' BEFORE INSERT ON '||t.table_name||' FOR EACH ROW WHEN (NEW.ID IS NULL) BEGIN SELECT '||v_seq||'.NEXTVAL INTO :NEW.ID FROM dual; END;';
            DBMS_OUTPUT.PUT_LINE('  OK trigger: '||v_trg||' -> '||t.table_name);
        EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('  ERR '||t.table_name||': '||SQLERRM); END;
    END LOOP;
END;
/
EXIT;
"@ | Set-Content -Path $tmp3 -Encoding ASCII
    Write-Info "  Paso 3: Creando SEQ_ y TRG_..."
    $o3 = & sqlplus -S $userConn "@$tmp3" 2>&1
    $o3 | Add-Content $log
    $o3 | Where-Object { $_ -match "(OK|ERR|YA EXISTE)" } | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
    Remove-Item $tmp1,$tmp2,$tmp3 -ErrorAction SilentlyContinue
    if (Test-OracleUser $n) { Write-Ok "Oracle listo para $user" } else { Write-Err "Fallo Oracle para $user - log: $log" }
}

function Ensure-DockerUp {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Err "No existe $f"; return }
    if (Test-DockerRunning $n) { Write-Skip "hotel-$n ya esta corriendo"; return }
    Write-Info "Levantando hotel-$n..."
    if (-not (Test-DockerImageExists $n)) {
        docker-compose --env-file $f -p "hotel-$n" up -d --build 2>&1 | Select-Object -Last 4
    } else {
        docker-compose --env-file $f -p "hotel-$n" up -d 2>&1 | Select-Object -Last 4
    }
    if (Test-DockerRunning $n) { Write-Ok "hotel-$n activo -> http://localhost:400$n" } else { Write-Err "hotel-$n no pudo levantarse" }
}

function Stop-Hotel {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    if (-not (Test-DockerRunning $n)) { Write-Skip "hotel-$n ya estaba detenido"; return }
    Write-Info "Bajando hotel-$n..."
    docker-compose --env-file $f -p "hotel-$n" down 2>&1 | Select-Object -Last 3
    Write-Ok "hotel-$n detenido"
}

function Rebuild-Frontend {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Info "Rebuild FRONTEND hotel-$n..."
    docker-compose --env-file $f -p "hotel-$n" down 2>&1 | Out-Null
    docker rmi "hotel-$n-frontend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "hotel-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Frontend hotel-$n reconstruido"
}

function Rebuild-Backend {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Info "Rebuild BACKEND hotel-$n..."
    docker-compose --env-file $f -p "hotel-$n" down 2>&1 | Out-Null
    docker rmi "hotel-$n-backend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "hotel-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Backend hotel-$n reconstruido"
}

function Rebuild-All {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Info "Rebuild COMPLETO hotel-$n..."
    docker-compose --env-file $f -p "hotel-$n" down 2>&1 | Out-Null
    docker rmi "hotel-$n-frontend" "hotel-$n-backend" -f 2>&1 | Out-Null
    docker-compose --env-file $f -p "hotel-$n" up -d --build 2>&1 | Select-Object -Last 4
    Write-Ok "Rebuild completo hotel-$n listo"
}

function Rebuild-Compose {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Info "Recreando hotel-$n (cambio en compose/.env)..."
    docker-compose --env-file $f -p "hotel-$n" down 2>&1 | Out-Null
    docker-compose --env-file $f -p "hotel-$n" up -d --force-recreate 2>&1 | Select-Object -Last 4
    Write-Ok "hotel-$n recreado"
}

function Rebuild-NoCache {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Info "Rebuild SIN CACHE hotel-$n..."
    docker-compose --env-file $f -p "hotel-$n" down 2>&1 | Out-Null
    docker-compose --env-file $f -p "hotel-$n" build --no-cache 2>&1 | Select-Object -Last 5
    docker-compose --env-file $f -p "hotel-$n" up -d 2>&1 | Select-Object -Last 4
    Write-Ok "Rebuild sin cache hotel-$n listo"
}

function Nuke-Hotel {
    param([int]$n)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Warn "ELIMINANDO todo de hotel-$n (contenedores + imagenes)..."
    docker-compose --env-file $f -p "hotel-$n" down --rmi all 2>&1 | Select-Object -Last 5
    Write-Ok "hotel-$n eliminado completamente"
}

function Show-Logs {
    param([int]$n, [string]$svc = "backend", [int]$lines = 30)
    $f = ".env.hotel$n"
    if (-not (Test-Path $f)) { Write-Warn "No existe .env.hotel$n"; return }
    Write-Header "Logs $svc hotel-$n (ultimas $lines lineas)"
    docker-compose --env-file $f -p "hotel-$n" logs $svc --tail $lines 2>&1
}

function Show-EnvCheck {
    param([int]$n)
    $cname = "hotel-$n-backend"
    Write-Header "Variables de entorno en $cname"
    docker exec $cname printenv 2>&1 | Where-Object { $_ -match "(PORT|DB_|JWT|COOKIE|CORS)" }
}

function Show-Status {
    param([int]$total = 5)
    Write-Header "Estado de instancias"
    Write-Host ("{0,-10} {1,-8} {2,-10} {3,-12} {4}" -f "HOTEL","ENV","ORACLE","DOCKER","URL") -ForegroundColor Cyan
    Write-Host ("-" * 65) -ForegroundColor DarkGray
    for ($i = 1; $i -le $total; $i++) {
        $e = if (Test-Path ".env.hotel$i") { "SI" } else { "NO" }
        $o = if (Test-OracleUser $i)       { "SI" } else { "NO" }
        $d = if (Test-DockerRunning $i)    { "CORRIENDO" } else { "DETENIDO" }
        $u = if ($d -eq "CORRIENDO")       { "http://localhost:400$i" } else { "-" }
        $c = if ($d -eq "CORRIENDO")       { "Green" } else { "Gray" }
        Write-Host ("{0,-10} {1,-8} {2,-10} {3,-12} {4}" -f "hotel-$i",$e,$o,$d,$u) -ForegroundColor $c
    }
    Write-Host ""
}

function Show-Help {
    Write-Host "`n=== GUIA DE COMANDOS - HOTELES DOCKER ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "GESTION BASICA:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 up [N]                 Levanta N hoteles (crea todo lo necesario)"
    Write-Host "  .\launch.ps1 down [N]               Baja N hoteles"
    Write-Host "  .\launch.ps1 status [N]             Tabla de estado de N instancias"
    Write-Host ""
    Write-Host "REBUILD POR CAMBIOS DE CODIGO:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 rebuild-frontend [N]   Reconstruye solo el frontend de N hoteles"
    Write-Host "  .\launch.ps1 rebuild-backend [N]    Reconstruye solo el backend de N hoteles"
    Write-Host "  .\launch.ps1 rebuild-all [N]        Reconstruye frontend+backend de N hoteles"
    Write-Host "  .\launch.ps1 rebuild-compose [N]    Recrea contenedores (cambio en .env/compose)"
    Write-Host "  .\launch.ps1 rebuild-nocache [N]    Rebuild sin cache (cuando algo raro pasa)"
    Write-Host ""
    Write-Host "LOGS Y DIAGNOSTICO:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 logs-backend [N]       Logs del backend de N hoteles"
    Write-Host "  .\launch.ps1 logs-frontend [N]      Logs del frontend de N hoteles"
    Write-Host "  .\launch.ps1 envcheck [N]           Variables de entorno en contenedor backend"
    Write-Host ""
    Write-Host "ORACLE:" -ForegroundColor Yellow
    Write-Host "  .\launch.ps1 oracle-only [N]        Solo configura Oracle para N hoteles"
    Write-Host ""
    Write-Host "PELIGROSO:" -ForegroundColor Red
    Write-Host "  .\launch.ps1 nuke [N]               Elimina contenedores e imagenes de N hoteles"
    Write-Host "  .\launch.ps1 clean-images           Elimina imagenes huerfanas de Docker"
    Write-Host ""
    Write-Host "EJEMPLOS:" -ForegroundColor Green
    Write-Host "  .\launch.ps1 up 10                  Levanta 10 hoteles"
    Write-Host "  .\launch.ps1 rebuild-frontend 3     Rebuild frontend de hoteles 1,2,3"
    Write-Host "  .\launch.ps1 logs-backend 1         Ver logs backend hotel-1"
    Write-Host "  .\launch.ps1 status 5               Ver estado de 5 instancias"
    Write-Host ""
}

# -- MAIN ----------------------------------------------------------
switch ($Action) {

    "up" {
        Write-Header "Asegurando $N instancias de hotel"
        Test-Deps
        $started = 0; $skipped = 0
        for ($i = 1; $i -le $N; $i++) {
            Write-Host ""; Write-Info "--- Hotel $i / $N ---"
            Ensure-EnvFile $i
            Ensure-OracleHotel $i
            $wasUp = Test-DockerRunning $i
            Ensure-DockerUp $i
            if ($wasUp) { $skipped++ } else { $started++ }
        }
        Write-Host ""; Show-Status $N
        Write-Ok "$N hoteles: $started iniciados, $skipped ya corriendo"
    }

    "down" {
        Write-Header "Bajando $N instancias"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Stop-Hotel $i }
        Show-Status $N
    }

    "status" {
        Test-Deps; Show-Status $N
    }

    "rebuild-frontend" {
        Write-Header "Rebuild FRONTEND - $N hoteles"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-Frontend $i }
        Show-Status $N
    }

    "rebuild-backend" {
        Write-Header "Rebuild BACKEND - $N hoteles"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-Backend $i }
        Show-Status $N
    }

    "rebuild-all" {
        Write-Header "Rebuild COMPLETO - $N hoteles"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-All $i }
        Show-Status $N
    }

    "rebuild-compose" {
        Write-Header "Recrear contenedores - $N hoteles"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-Compose $i }
        Show-Status $N
    }

    "rebuild-nocache" {
        Write-Header "Rebuild SIN CACHE - $N hoteles"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Rebuild-NoCache $i }
        Show-Status $N
    }

    "logs-backend" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-Logs $i "backend" 30 }
    }

    "logs-frontend" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-Logs $i "frontend" 30 }
    }

    "envcheck" {
        Test-Deps
        for ($i = 1; $i -le $N; $i++) { Show-EnvCheck $i }
    }

    "oracle-only" {
        Write-Header "Configurando Oracle para $N hoteles"
        Test-Deps
        for ($i = 1; $i -le $N; $i++) {
            Write-Info "--- Oracle hotel $i / $N ---"
            Ensure-OracleHotel $i
        }
        Write-Ok "Oracle verificado para $N hoteles"
    }

    "nuke" {
        Write-Warn "ADVERTENCIA: Esto elimina contenedores e imagenes de $N hoteles."
        Write-Warn "Los datos de Oracle NO se borran. Solo Docker."
        $confirm = Read-Host "Escribi SI para confirmar"
        if ($confirm -eq "SI") {
            for ($i = 1; $i -le $N; $i++) { Nuke-Hotel $i }
            Write-Ok "Nuke completado para $N hoteles"
        } else { Write-Info "Cancelado." }
    }

    "clean-images" {
        Write-Info "Eliminando imagenes huerfanas..."
        docker image prune -f
        Write-Host ""
        Write-Info "Imagenes de hotel actuales:"
        docker images | Select-String "hotel"
    }

    "help" { Show-Help }

    default { Show-Help }
}