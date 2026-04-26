-- ============================================================
-- MIGRACIÓN: Soft-delete para Avion y MiembroTripulacion
-- Agrega columna Activo BIT (1=activo, 0=inactivo)
-- Ejecutar una sola vez contra la base de datos de producción
-- ============================================================

-- Agregar columna Activo a Avion (si no existe)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.Avion') AND name = 'Activo'
)
BEGIN
    ALTER TABLE [dbo].[Avion] ADD [Activo] BIT NOT NULL DEFAULT 1;
    UPDATE [dbo].[Avion] SET [Activo] = 1;
    PRINT 'Columna Activo agregada a Avion';
END
ELSE
    PRINT 'Columna Activo ya existe en Avion - sin cambios';

-- Agregar columna Activo a MiembroTripulacion (si no existe)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.MiembroTripulacion') AND name = 'Activo'
)
BEGIN
    ALTER TABLE [dbo].[MiembroTripulacion] ADD [Activo] BIT NOT NULL DEFAULT 1;
    UPDATE [dbo].[MiembroTripulacion] SET [Activo] = 1;
    PRINT 'Columna Activo agregada a MiembroTripulacion';
END
ELSE
    PRINT 'Columna Activo ya existe en MiembroTripulacion - sin cambios';
