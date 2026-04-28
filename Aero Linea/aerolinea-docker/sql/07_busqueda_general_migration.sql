-- ─────────────────────────────────────────────────────────────────────────────
-- Migración 07: Busqueda General desde encabezado
-- Permite registrar búsquedas de texto libre (sin ruta específica)
-- haciendo nullable la columna RutaID y agregando TipoBusqueda 4='General'.
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. Hacer RutaID nullable (solo si aún es NOT NULL)
IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Busqueda' AND COLUMN_NAME = 'RutaID' AND IS_NULLABLE = 'NO'
)
BEGIN
    -- Eliminar la FK existente sobre RutaID
    DECLARE @fkName NVARCHAR(256);
    SELECT @fkName = fk.name
    FROM   sys.foreign_keys fk
    INNER JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
    INNER JOIN sys.columns c ON c.object_id = fkc.parent_object_id AND c.column_id = fkc.parent_column_id
    WHERE  fk.parent_object_id = OBJECT_ID('Busqueda') AND c.name = 'RutaID';

    IF @fkName IS NOT NULL
        EXEC ('ALTER TABLE Busqueda DROP CONSTRAINT ' + @fkName);

    -- Ahora sí alterar la columna a nullable
    ALTER TABLE Busqueda ALTER COLUMN RutaID INT NULL;

    -- Volver a agregar la FK como nullable
    ALTER TABLE Busqueda ADD FOREIGN KEY (RutaID) REFERENCES Ruta(ID);

    PRINT 'RutaID en Busqueda ahora es nullable.';
END
ELSE
BEGIN
    PRINT 'RutaID ya es nullable, no se requiere cambio.';
END

-- 2. Agregar TipoBusqueda 4 = 'General' (si no existe)
IF NOT EXISTS (SELECT 1 FROM TipoBusqueda WHERE ID = 3)
    INSERT INTO TipoBusqueda (ID, Tipo) VALUES (3, N'Web');

IF NOT EXISTS (SELECT 1 FROM TipoBusqueda WHERE ID = 4)
    INSERT INTO TipoBusqueda (ID, Tipo) VALUES (4, N'General');

PRINT 'Migración 07 completada.';
