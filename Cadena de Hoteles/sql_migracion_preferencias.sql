-- Migración: agrega columna de preferencias de ofertas al usuario
-- Ejecutar una sola vez en Oracle antes de levantar el backend

ALTER TABLE Usuario ADD (Preferencias_Oferta VARCHAR2(2000) DEFAULT NULL);
