USE [AerolineaDB]
GO

-- =============================================
-- VISTA: dbo.vw_BoletoDetalle
-- =============================================
-- Basada en la query CargarBoletos del backend.
-- Fusiona Boleto + Clase + EstadoBoleto + Vuelo
-- + Ruta + Aeropuerto (x2) + Ciudad (x2)
-- + Pais (x2) + Avion + DatosPasajero + Ciudad
-- + Pais del pasajero en una sola consulta
-- (13 JOINs).
-- Elimina la necesidad de llamar a
-- ObtenerDatosPasajero por separado en C#.
-- Uso: SELECT ... FROM vw_BoletoDetalle
--      WHERE ReservacionID = @id
-- =============================================
CREATE OR ALTER VIEW dbo.vw_BoletoDetalle
AS
SELECT
    b.ID                AS BoletoID,
    b.NoBoleto,
    b.NoAsiento,
    b.Precio,
    b.ReservacionID,
    b.EstadoBoletoID,
    b.DatosPasajeroID,
    c.TipoDeClase       AS Clase,
    eb.Estado           AS EstadoBoleto,
    v.ID                AS VueloID,
    v.NumeroVuelo,
    v.Fecha             AS FechaVuelo,
    v.HoraSalida,
    v.HoraLlegada,
    v.FechaLlegada,
    ru.ID               AS RutaID,
    ru.DuracionEstimada,
    ao.Codigo           AS OrigenCodigo,
    ao.Nombre           AS OrigenNombre,
    co.Nombre           AS OrigenCiudad,
    po.Nombre           AS OrigenPais,
    ad.Codigo           AS DestinoCodigo,
    ad.Nombre           AS DestinoNombre,
    cd.Nombre           AS DestinoCiudad,
    pd.Nombre           AS DestinoPais,
    a.ID                AS AvionID,
    a.Modelo            AS AvionModelo,
    a.Marca             AS AvionMarca,
    a.CapacidadPasajeros,
    dp.Nombre           AS PasajeroNombre,
    dp.Apellido         AS PasajeroApellido,
    dp.Pasaporte        AS PasajeroPasaporte,
    dp.Telefono         AS PasajeroTelefono,
    cpas.Nombre         AS PasajeroCiudad,
    ppas.Nombre         AS PasajeroPais
FROM Boleto b
INNER JOIN Clase         c    ON c.ID    = b.ClaseID
INNER JOIN EstadoBoleto  eb   ON eb.ID   = b.EstadoBoletoID
INNER JOIN Vuelo         v    ON v.ID    = b.VueloID
INNER JOIN Ruta          ru   ON ru.ID   = v.RutaID
INNER JOIN Aeropuerto    ao   ON ao.ID   = ru.OrigenID
INNER JOIN Aeropuerto    ad   ON ad.ID   = ru.DestinoID
INNER JOIN Ciudad        co   ON co.ID   = ao.CiudadID
INNER JOIN Ciudad        cd   ON cd.ID   = ad.CiudadID
INNER JOIN Pais          po   ON po.ID   = co.PaisID
INNER JOIN Pais          pd   ON pd.ID   = cd.PaisID
INNER JOIN Avion         a    ON a.ID    = v.AvionID
LEFT  JOIN DatosPasajero dp   ON dp.ID   = b.DatosPasajeroID
LEFT  JOIN Ciudad        cpas ON cpas.ID = dp.CiudadID
LEFT  JOIN Pais          ppas ON ppas.ID = cpas.PaisID
GO
