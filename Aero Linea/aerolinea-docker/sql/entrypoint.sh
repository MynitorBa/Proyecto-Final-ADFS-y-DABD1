#!/bin/bash

echo "SQL Server..."
sleep 20

echo "schema..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /docker-entrypoint-initdb/01-schema.sql

echo "seed data..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /docker-entrypoint-initdb/02-seed.sql

echo "UDF y SP de cancelacion..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /docker-entrypoint-initdb/04_udf_sp_cancelacion.sql

echo "Trigger auditoria vuelos..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /docker-entrypoint-initdb/05_trigger_auditoria_vuelo.sql

echo "Vista vw_BoletoDetalle..."
/opt/mssql-tools18/bin/sqlcmd \
  -S localhost \
  -U sa \
  -P "$SA_PASSWORD" \
  -No \
  -i /docker-entrypoint-initdb/06_vista_boleto_detalle.sql

echo "Base de datos lista"