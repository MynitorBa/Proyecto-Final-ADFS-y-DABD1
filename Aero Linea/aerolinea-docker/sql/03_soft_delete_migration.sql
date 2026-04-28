-- ============================================================
-- MIGRACIÓN v2: Soft-delete para Avion, MiembroTripulacion y Ruta
-- Agrega columna Activo BIT (1=activo, 0=inactivo)
-- Ejecutar una sola vez contra bases de datos existentes.
-- En instalaciones frescas via Docker el schema ya las incluye.
-- ============================================================

USE AerolineaDB;
GO

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
GO

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
GO

-- NUEVO v2: Agregar columna Activo a Ruta (si no existe)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.Ruta') AND name = 'Activo'
)
BEGIN
    ALTER TABLE [dbo].[Ruta] ADD [Activo] BIT NOT NULL DEFAULT 1;
    UPDATE [dbo].[Ruta] SET [Activo] = 1;
    PRINT 'Columna Activo agregada a Ruta';
END
ELSE
    PRINT 'Columna Activo ya existe en Ruta - sin cambios';
GO

-- NUEVO v2: Renombrar FK_LogReservacion_Tipo -> FK_LogRes_Tipo si el nombre viejo existe
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_LogReservacion_Tipo')
BEGIN
    ALTER TABLE [dbo].[LogReservacion] DROP CONSTRAINT FK_LogReservacion_Tipo;
    ALTER TABLE [dbo].[LogReservacion] ADD CONSTRAINT FK_LogRes_Tipo
        FOREIGN KEY ([TipoEventoID]) REFERENCES [dbo].[TipoEventoReservacion] ([ID]);
    PRINT 'FK_LogReservacion_Tipo renombrada a FK_LogRes_Tipo';
END
ELSE
    PRINT 'FK_LogRes_Tipo ya tiene nombre correcto - sin cambios';
GO

-- NUEVO v2: Renombrar FK_LogReservacion_Usuario -> FK_LogRes_Usuario si el nombre viejo existe
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_LogReservacion_Usuario')
BEGIN
    ALTER TABLE [dbo].[LogReservacion] DROP CONSTRAINT FK_LogReservacion_Usuario;
    ALTER TABLE [dbo].[LogReservacion] ADD CONSTRAINT FK_LogRes_Usuario
        FOREIGN KEY ([UsuarioID]) REFERENCES [dbo].[Usuario] ([Id]);
    PRINT 'FK_LogReservacion_Usuario renombrada a FK_LogRes_Usuario';
END
ELSE
    PRINT 'FK_LogRes_Usuario ya tiene nombre correcto - sin cambios';
GO

-- NUEVO v2: Renombrar FK_UsuarioNacionalidad_Usuario -> FK_UN_Usuario si el nombre viejo existe
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_UsuarioNacionalidad_Usuario')
BEGIN
    ALTER TABLE [dbo].[UsuarioNacionalidad] DROP CONSTRAINT FK_UsuarioNacionalidad_Usuario;
    ALTER TABLE [dbo].[UsuarioNacionalidad] ADD CONSTRAINT FK_UN_Usuario
        FOREIGN KEY ([UsuarioId]) REFERENCES [dbo].[Usuario] ([Id]);
    PRINT 'FK_UsuarioNacionalidad_Usuario renombrada a FK_UN_Usuario';
END
ELSE
    PRINT 'FK_UN_Usuario ya tiene nombre correcto - sin cambios';
GO

PRINT '✅ Migracion v2 completada';
GO