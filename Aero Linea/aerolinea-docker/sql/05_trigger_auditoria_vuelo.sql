USE [AerolineaDB]
GO

-- =============================================
-- TABLA 1: TipoAuditoriaVuelo (catálogo)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'TipoAuditoriaVuelo')
CREATE TABLE dbo.TipoAuditoriaVuelo (
    ID        INT IDENTITY(1,1) PRIMARY KEY,
    Operacion VARCHAR(50) NOT NULL
);
GO

-- Datos iniciales del catálogo
IF NOT EXISTS (SELECT 1 FROM dbo.TipoAuditoriaVuelo WHERE Operacion = 'CREACION')
    INSERT INTO dbo.TipoAuditoriaVuelo (Operacion) VALUES ('CREACION');

IF NOT EXISTS (SELECT 1 FROM dbo.TipoAuditoriaVuelo WHERE Operacion = 'CANCELACION')
    INSERT INTO dbo.TipoAuditoriaVuelo (Operacion) VALUES ('CANCELACION');
GO

-- =============================================
-- TABLA 2: AuditoriaVuelo (registro por evento)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'AuditoriaVuelo')
CREATE TABLE dbo.AuditoriaVuelo (
    ID                   INT IDENTITY(1,1) PRIMARY KEY,
    VueloID              INT          NOT NULL,
    NumeroVuelo          VARCHAR(20)  NOT NULL,
    TipoAuditoriaVueloID INT          NOT NULL,
    EstadoAnteriorID     INT          NULL,
    EstadoNuevoID        INT          NOT NULL,
    Fecha                DATETIME     NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_AuditoriaVuelo_Tipo
        FOREIGN KEY (TipoAuditoriaVueloID)
        REFERENCES dbo.TipoAuditoriaVuelo(ID)
);
GO

-- =============================================
-- TRIGGER: trg_Vuelo_Auditoria
-- =============================================
-- Dispara en AFTER INSERT (creación) y
-- AFTER UPDATE cuando EstadoID cambia a 4
-- (cancelación). Registra cada evento en
-- AuditoriaVuelo con estado anterior y nuevo.
-- =============================================
CREATE OR ALTER TRIGGER dbo.trg_Vuelo_Auditoria
ON dbo.Vuelo
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- CREACION: vuelo recién insertado (no existe en deleted)
    INSERT INTO dbo.AuditoriaVuelo
        (VueloID, NumeroVuelo, TipoAuditoriaVueloID, EstadoAnteriorID, EstadoNuevoID)
    SELECT
        i.ID,
        i.NumeroVuelo,
        1,          -- CREACION
        NULL,       -- sin estado anterior
        i.EstadoID
    FROM inserted i
    WHERE NOT EXISTS (SELECT 1 FROM deleted d WHERE d.ID = i.ID);

    -- CANCELACION: EstadoID cambió a 4 en un UPDATE
    INSERT INTO dbo.AuditoriaVuelo
        (VueloID, NumeroVuelo, TipoAuditoriaVueloID, EstadoAnteriorID, EstadoNuevoID)
    SELECT
        i.ID,
        i.NumeroVuelo,
        2,          -- CANCELACION
        d.EstadoID, -- estado que tenía antes
        i.EstadoID  -- 4 = Cancelado
    FROM inserted i
    INNER JOIN deleted d ON d.ID = i.ID
    WHERE i.EstadoID = 4
      AND d.EstadoID <> 4;

END
GO
