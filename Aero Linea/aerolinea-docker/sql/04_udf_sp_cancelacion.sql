USE [AerolineaDB]
GO

-- =============================================
-- FUNCIÓN ESCALAR: dbo.ufn_HorasHastaVuelo
-- =============================================
-- Recibe el ID de una reservación y devuelve
-- las horas mínimas hasta el primer vuelo activo
-- (EstadoBoletoID IN (2,3)) de esa reservación.
-- Combina Vuelo.Fecha (date) + HoraSalida (time)
-- en un DATETIME comparable contra GETDATE().
-- Retorna -1 si no hay boletos activos.
-- Utilizada por: usp_CancelarReservacion,
--               PuedeCancelar (C#).
-- =============================================
CREATE OR ALTER FUNCTION dbo.ufn_HorasHastaVuelo
(
    @ReservacionID INT
)
RETURNS INT
AS
BEGIN
    DECLARE @HorasMinimas INT;

    SELECT @HorasMinimas = MIN(
        DATEDIFF(
            HOUR,
            GETDATE(),
            DATEADD(MINUTE,
                DATEPART(MINUTE, v.HoraSalida),
                DATEADD(HOUR,
                    DATEPART(HOUR, v.HoraSalida),
                    CAST(v.Fecha AS DATETIME)
                )
            )
        )
    )
    FROM Boleto b
    INNER JOIN Vuelo v ON v.ID = b.VueloID
    WHERE b.ReservacionID  = @ReservacionID
      AND b.EstadoBoletoID IN (2, 3);

    RETURN ISNULL(@HorasMinimas, -1);
END
GO

-- =============================================
-- PROCEDIMIENTO: dbo.usp_CancelarReservacion
-- =============================================
-- Cancela una reservación en estado Pendiente (1)
-- o Confirmada (2). Para confirmadas valida la
-- regla de 24 horas antes del vuelo llamando
-- internamente a dbo.ufn_HorasHastaVuelo.
--
-- Parámetros de entrada:
--   @ReservacionID  INT           -- ID de la reservación
--   @UsuarioID      INT           -- ID del usuario que cancela
--   @Motivo         VARCHAR(255)  -- Motivo de cancelación
--   @EsAdmin        BIT = 0       -- 1 = omite validación de propiedad
--
-- Parámetros de salida:
--   @Resultado      INT           -- 0=OK | 1=NoExiste/Acceso
--                                 -- 2=EstadoInvalido | 3=Menos24h
--                                 -- -1=ErrorInterno
--   @Mensaje        VARCHAR(500)  -- Descripción del resultado
--   @NoReservacion  VARCHAR(50)   -- Para correo de cancelación
--   @NombreUsuario  VARCHAR(200)  -- Para correo de cancelación
--   @EmailUsuario   VARCHAR(150)  -- Para correo de cancelación
-- =============================================
CREATE OR ALTER PROCEDURE dbo.usp_CancelarReservacion
    @ReservacionID   INT,
    @UsuarioID       INT,
    @Motivo          VARCHAR(255),
    @EsAdmin         BIT          = 0,
    @Resultado       INT          OUTPUT,
    @Mensaje         VARCHAR(500) OUTPUT,
    @NoReservacion   VARCHAR(50)  OUTPUT,
    @NombreUsuario   VARCHAR(200) OUTPUT,
    @EmailUsuario    VARCHAR(150) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT @Resultado = -1, @Mensaje = '',
           @NoReservacion = '', @NombreUsuario = '', @EmailUsuario = '';

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @EstadoActual    INT;
        DECLARE @HorasHastaVuelo INT;

        -- 1. Verificar existencia, propiedad y obtener datos para el correo.
        --    UPDLOCK + ROWLOCK evitan doble cancelación concurrente.
        SELECT
            @EstadoActual  = r.EstadoReservaID,
            @NoReservacion = r.NoReservacion,
            @NombreUsuario = u.Nombre + ' ' + u.Apellido,
            @EmailUsuario  = u.Correo
        FROM Reservacion r WITH (UPDLOCK, ROWLOCK)
        INNER JOIN Usuario u ON u.ID = r.UsuarioID
        WHERE r.ID = @ReservacionID
          AND (@EsAdmin = 1 OR r.UsuarioID = @UsuarioID);

        IF @EstadoActual IS NULL
        BEGIN
            SET @Resultado = 1;
            SET @Mensaje   = 'Reservación no encontrada o no tienes acceso a ella.';
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- 2. Solo Pendiente (1) o Confirmada (2) son cancelables.
        IF @EstadoActual NOT IN (1, 2)
        BEGIN
            SET @Resultado = 2;
            SET @Mensaje   = 'Solo puedes cancelar reservaciones pendientes o confirmadas.';
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- 3. Regla de 24 horas: aplica únicamente a reservaciones Confirmadas.
        IF @EstadoActual = 2
        BEGIN
            SET @HorasHastaVuelo = dbo.ufn_HorasHastaVuelo(@ReservacionID);

            IF @HorasHastaVuelo = -1
            BEGIN
                SET @Resultado = 1;
                SET @Mensaje   = 'No se encontraron vuelos activos en esta reservación.';
                ROLLBACK TRANSACTION;
                RETURN;
            END

            IF @HorasHastaVuelo < 24
            BEGIN
                SET @Resultado = 3;
                SET @Mensaje   = 'No puedes cancelar. Faltan menos de 24 horas para tu vuelo (quedan '
                    + CAST(@HorasHastaVuelo AS VARCHAR(10)) + ' horas).';
                ROLLBACK TRANSACTION;
                RETURN;
            END
        END

        -- 4. Restaurar disponibilidad en Vuelo.
        --    Un solo UPDATE con subquery reemplaza el loop por clase del C#.
        UPDATE v
        SET
            v.BoletosTurista   = v.BoletosTurista   + ISNULL(cnt.CantTurista,   0),
            v.BoletosEjecutivo = v.BoletosEjecutivo + ISNULL(cnt.CantEjecutivo, 0)
        FROM Vuelo v
        INNER JOIN (
            SELECT
                b.VueloID,
                SUM(CASE WHEN b.ClaseID = 1 THEN 1 ELSE 0 END) AS CantTurista,
                SUM(CASE WHEN b.ClaseID = 2 THEN 1 ELSE 0 END) AS CantEjecutivo
            FROM Boleto b
            WHERE b.ReservacionID  = @ReservacionID
              AND b.EstadoBoletoID IN (2, 3)
            GROUP BY b.VueloID
        ) cnt ON cnt.VueloID = v.ID;

        -- 5. Cancelar boletos activos.
        UPDATE Boleto
        SET EstadoBoletoID = 4
        WHERE ReservacionID  = @ReservacionID
          AND EstadoBoletoID IN (2, 3);

        -- 6. Cancelar la reservación.
        UPDATE Reservacion
        SET EstadoReservaID   = 3,
            FechaExpiracion   = NULL,
            FechaCancelacion  = GETDATE(),
            MotivoCancelacion = @Motivo
        WHERE ID = @ReservacionID;

        COMMIT TRANSACTION;

        SET @Resultado = 0;
        SET @Mensaje   = 'Reservación cancelada exitosamente.';

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        SET @Resultado = -1;
        SET @Mensaje   = 'Error interno: ' + ERROR_MESSAGE();
    END CATCH
END
GO
