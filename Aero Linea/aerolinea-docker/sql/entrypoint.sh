#!/bin/bash

echo "Esperando SQL Server..."
sleep 20

echo "[1/7] Creando schema..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/01_schema.sql

echo "[2/7] Cargando seed data..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/02_seed.sql

echo "[3/7] Aplicando migracion v2 (soft-delete + renombre FKs)..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/03_soft_delete_migration.sql

echo "[4/7] Creando UDF y SP de cancelacion..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/04_udf_sp_cancelacion.sql

echo "[5/7] Creando triggers de auditoria..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/05_trigger_auditoria_vuelo.sql

echo "[6/7] Creando vistas..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/06_vista_boleto_detalle.sql

echo "[7/7] Cargando datos de Aerolinea (18 MB, ~5000 registros)..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /scripts/07_aerolinea_data.sql

echo "✅ Base de datos lista con todos los datos"