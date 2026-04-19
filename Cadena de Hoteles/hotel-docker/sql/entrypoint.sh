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

echo "Base de datos lista"