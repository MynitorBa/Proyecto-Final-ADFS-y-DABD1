using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class ReservacionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public ReservacionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<ReservacionCreadaDTO> CrearReservacion(int usuarioId, List<SeleccionVueloDTO> vuelos)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Generar número de reservación único
                string noReservacion = GenerarNoReservacion();

                // 2. Calcular fecha de expiración (15 minutos desde ahora)
                DateTime fechaExpiracion = DateTime.Now.AddMinutes(15);

                // 3. Calcular total y obtener boletos
                decimal total = 0;
                var boletosReservados = new List<BoletoReservadoDTO>();
                int reservacionId = 0;

                // 4. Crear la reservación (primero sin total)
                string insertReservacion = @"
                    INSERT INTO Reservacion (NoReservacion, UsuarioID, FechaCreacion, FechaExpiracion, Total, EstadoReservaID)
                    VALUES (@noReservacion, @usuarioId, GETDATE(), @fechaExpiracion, 0, 1);
                    SELECT SCOPE_IDENTITY();";

                using (var cmd = new SqlCommand(insertReservacion, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@noReservacion", noReservacion);
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@fechaExpiracion", fechaExpiracion);
                    reservacionId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                // 5. Para cada vuelo, reservar los primeros boletos disponibles
                foreach (var vuelo in vuelos)
                {
                    // Obtener los primeros N boletos disponibles de esa clase
                    string queryBoletos = @"
                        SELECT TOP (@cantidad) 
                            b.ID, b.NoBoleto, b.NoAsiento, b.Precio,
                            v.NumeroVuelo,
                            c.TipoDeClase
                        FROM Boleto b
                        INNER JOIN Vuelo v ON b.VueloID = v.ID
                        INNER JOIN Clase c ON b.ClaseID = c.ID
                        INNER JOIN EstadoBoleto eb ON b.EstadoBoletoID = eb.ID
                        WHERE b.VueloID = @vueloId
                          AND b.ClaseID = @claseId
                          AND eb.Estado = 'Disponible'
                        ORDER BY b.NoAsiento";

                    using var cmdBoletos = new SqlCommand(queryBoletos, connection, transaction);
                    cmdBoletos.Parameters.AddWithValue("@cantidad", vuelo.CantidadPasajeros);
                    cmdBoletos.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                    cmdBoletos.Parameters.AddWithValue("@claseId", vuelo.ClaseId);

                    using var reader = await cmdBoletos.ExecuteReaderAsync();
                    var boletosVuelo = new List<int>();

                    while (await reader.ReadAsync())
                    {
                        int boletoId = reader.GetInt32(0);
                        boletosVuelo.Add(boletoId);

                        boletosReservados.Add(new BoletoReservadoDTO
                        {
                            BoletoId = boletoId,
                            NoBoleto = reader.GetString(1),
                            NoAsiento = reader.GetString(2),
                            Precio = reader.GetDecimal(3),
                            NumeroVuelo = reader.GetString(4),
                            Clase = reader.GetString(5)
                        });

                        total += reader.GetDecimal(3);
                    }
                    reader.Close();

                    // Verificar que obtuvimos suficientes boletos
                    if (boletosVuelo.Count < vuelo.CantidadPasajeros)
                    {
                        throw new Exception($"No hay suficientes boletos disponibles en el vuelo {vuelo.VueloId}");
                    }

                    // Actualizar los boletos: estado a Reservado (2) y asignar reservación
                    string updateBoletos = @"
                        UPDATE Boleto 
                        SET EstadoBoletoID = 2, 
                            ReservacionID = @reservacionId
                        WHERE ID IN (" + string.Join(",", boletosVuelo) + ")";

                    using var cmdUpdate = new SqlCommand(updateBoletos, connection, transaction);
                    cmdUpdate.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmdUpdate.ExecuteNonQueryAsync();

                    // Actualizar BoletosDisponibles del vuelo
                    string updateVuelo = @"
                        UPDATE Vuelo 
                        SET BoletosDisponibles = BoletosDisponibles - @cantidad
                        WHERE ID = @vueloId";

                    using var cmdUpdateVuelo = new SqlCommand(updateVuelo, connection, transaction);
                    cmdUpdateVuelo.Parameters.AddWithValue("@cantidad", vuelo.CantidadPasajeros);
                    cmdUpdateVuelo.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                    await cmdUpdateVuelo.ExecuteNonQueryAsync();
                }

                // 6. Actualizar el total de la reservación
                string updateTotal = @"
                    UPDATE Reservacion 
                    SET Total = @total
                    WHERE ID = @reservacionId";

                using (var cmd = new SqlCommand(updateTotal, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@total", total);
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 7. Commit de la transacción
                transaction.Commit();

                return new ReservacionCreadaDTO
                {
                    ReservacionId = reservacionId,
                    NoReservacion = noReservacion,
                    FechaExpiracion = fechaExpiracion,
                    Total = total,
                    Boletos = boletosReservados
                };
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        private string GenerarNoReservacion()
        {
            // Formato: RES + timestamp + random
            return "RES" + DateTime.Now.ToString("yyyyMMddHHmmss") + new Random().Next(1000, 9999);
        }

        public async Task<int> LiberarReservasExpiradas()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Buscar todas las reservas pendientes expiradas
                string queryReservasExpiradas = @"
                    SELECT ID
                    FROM Reservacion
                    WHERE EstadoReservaID = 1 
                      AND FechaExpiracion IS NOT NULL
                      AND FechaExpiracion < GETDATE()";

                var reservasExpiradas = new List<int>();

                using (var cmd = new SqlCommand(queryReservasExpiradas, connection, transaction))
                {
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                    {
                        reservasExpiradas.Add(reader.GetInt32(0));
                    }
                }

                if (reservasExpiradas.Count == 0)
                {
                    transaction.Commit();
                    return 0;
                }

                // 2. Para cada reserva expirada
                foreach (var reservaId in reservasExpiradas)
                {
                    // 2.1 Obtener los vuelos y cantidad de boletos a liberar
                    string queryBoletosVuelo = @"
                        SELECT VueloID, COUNT(*) as Cantidad
                        FROM Boleto
                        WHERE ReservacionID = @reservaId
                        GROUP BY VueloID";

                    var vuelosAfectados = new Dictionary<int, int>();

                    using (var cmd = new SqlCommand(queryBoletosVuelo, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservaId", reservaId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        while (await reader.ReadAsync())
                        {
                            vuelosAfectados[reader.GetInt32(0)] = reader.GetInt32(1);
                        }
                    }

                    // 2.2 Liberar los boletos (cambiar estado a Disponible y quitar reservación)
                    string updateBoletos = @"
                        UPDATE Boleto
                        SET EstadoBoletoID = 1,
                            ReservacionID = NULL,
                            DatosPasajeroID = NULL
                        WHERE ReservacionID = @reservaId";

                    using (var cmd = new SqlCommand(updateBoletos, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservaId", reservaId);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    // 2.3 Restaurar BoletosDisponibles en cada vuelo
                    foreach (var vuelo in vuelosAfectados)
                    {
                        string updateVuelo = @"
                            UPDATE Vuelo
                            SET BoletosDisponibles = BoletosDisponibles + @cantidad
                            WHERE ID = @vueloId";

                        using var cmd = new SqlCommand(updateVuelo, connection, transaction);
                        cmd.Parameters.AddWithValue("@cantidad", vuelo.Value);
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.Key);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    // 2.4 Actualizar estado de la reservación a Expirada (ID 4)
                    string updateReservacion = @"
                        UPDATE Reservacion
                        SET EstadoReservaID = 4
                        WHERE ID = @reservaId";

                    using (var cmd = new SqlCommand(updateReservacion, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservaId", reservaId);
                        await cmd.ExecuteNonQueryAsync();
                    }
                }

                transaction.Commit();
                return reservasExpiradas.Count;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        public async Task AgregarPasajerosAReservacion(int reservacionId, List<DatosPasajeroDTO> pasajeros)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que la reservación existe y está vigente
                string queryVerificar = @"
                    SELECT EstadoReservaID, FechaExpiracion
                    FROM Reservacion
                    WHERE ID = @reservacionId";

                int estadoReserva = 0;
                DateTime? fechaExpiracion = null;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();

                    if (!await reader.ReadAsync())
                    {
                        throw new Exception("La reservación no existe.");
                    }

                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1))
                    {
                        fechaExpiracion = reader.GetDateTime(1);
                    }
                }

                // Verificar que la reservación está Pendiente (1)
                if (estadoReserva != 1)
                {
                    throw new Exception("La reservación no está en estado pendiente.");
                }

                // Verificar que no ha expirado
                if (fechaExpiracion.HasValue && fechaExpiracion.Value < DateTime.Now)
                {
                    throw new Exception("La reservación ha expirado.");
                }

                // 2. Para cada pasajero
                foreach (var pasajero in pasajeros)
                {
                    // 2.1 Verificar que el boleto pertenece a esta reservación
                    string queryVerificarBoleto = @"
                        SELECT ReservacionID, DatosPasajeroID
                        FROM Boleto
                        WHERE ID = @boletoId";

                    int? reservacionDelBoleto = null;
                    int? pasajeroExistente = null;

                    using (var cmd = new SqlCommand(queryVerificarBoleto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@boletoId", pasajero.BoletoId);
                        using var reader = await cmd.ExecuteReaderAsync();

                        if (!await reader.ReadAsync())
                        {
                            throw new Exception($"El boleto {pasajero.BoletoId} no existe.");
                        }

                        if (!reader.IsDBNull(0))
                        {
                            reservacionDelBoleto = reader.GetInt32(0);
                        }

                        if (!reader.IsDBNull(1))
                        {
                            pasajeroExistente = reader.GetInt32(1);
                        }
                    }

                    if (reservacionDelBoleto != reservacionId)
                    {
                        throw new Exception($"El boleto {pasajero.BoletoId} no pertenece a esta reservación.");
                    }

                    // 2.2 Crear o actualizar datos del pasajero
                    int datosPasajeroId;

                    if (pasajeroExistente.HasValue)
                    {
                        // Actualizar datos existentes
                        string updatePasajero = @"
                            UPDATE DatosPasajero
                            SET Nombre = @nombre,
                                Apellido = @apellido,
                                Pasaporte = @pasaporte,
                                Telefono = @telefono,
                                PaisID = @paisId,
                                CiudadID = @ciudadId
                            WHERE ID = @id";

                        using var cmd = new SqlCommand(updatePasajero, connection, transaction);
                        cmd.Parameters.AddWithValue("@id", pasajeroExistente.Value);
                        cmd.Parameters.AddWithValue("@nombre", pasajero.Nombre);
                        cmd.Parameters.AddWithValue("@apellido", pasajero.Apellido);
                        cmd.Parameters.AddWithValue("@pasaporte", pasajero.Pasaporte);
                        cmd.Parameters.AddWithValue("@telefono", pasajero.Telefono);
                        cmd.Parameters.AddWithValue("@paisId", pasajero.PaisId);
                        cmd.Parameters.AddWithValue("@ciudadId", pasajero.CiudadId);
                        await cmd.ExecuteNonQueryAsync();

                        datosPasajeroId = pasajeroExistente.Value;
                    }
                    else
                    {
                        // Crear nuevos datos de pasajero
                        string insertPasajero = @"
                            INSERT INTO DatosPasajero (Nombre, Apellido, Pasaporte, Telefono, PaisID, CiudadID)
                            VALUES (@nombre, @apellido, @pasaporte, @telefono, @paisId, @ciudadId);
                            SELECT SCOPE_IDENTITY();";

                        using var cmd = new SqlCommand(insertPasajero, connection, transaction);
                        cmd.Parameters.AddWithValue("@nombre", pasajero.Nombre);
                        cmd.Parameters.AddWithValue("@apellido", pasajero.Apellido);
                        cmd.Parameters.AddWithValue("@pasaporte", pasajero.Pasaporte);
                        cmd.Parameters.AddWithValue("@telefono", pasajero.Telefono);
                        cmd.Parameters.AddWithValue("@paisId", pasajero.PaisId);
                        cmd.Parameters.AddWithValue("@ciudadId", pasajero.CiudadId);
                        datosPasajeroId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                    }

                    // 2.3 Asociar el pasajero al boleto
                    string updateBoleto = @"
                        UPDATE Boleto
                        SET DatosPasajeroID = @datosPasajeroId
                        WHERE ID = @boletoId";

                    using (var cmd = new SqlCommand(updateBoleto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@datosPasajeroId", datosPasajeroId);
                        cmd.Parameters.AddWithValue("@boletoId", pasajero.BoletoId);
                        await cmd.ExecuteNonQueryAsync();
                    }
                }

                transaction.Commit();
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        public async Task ConfirmarReservacion(int reservacionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que la reservación existe y está vigente
                string queryVerificar = @"
                    SELECT EstadoReservaID, FechaExpiracion
                    FROM Reservacion
                    WHERE ID = @reservacionId";

                int estadoReserva = 0;
                DateTime? fechaExpiracion = null;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();

                    if (!await reader.ReadAsync())
                    {
                        throw new Exception("La reservación no existe.");
                    }

                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1))
                    {
                        fechaExpiracion = reader.GetDateTime(1);
                    }
                }

                // Verificar que la reservación está Pendiente (1)
                if (estadoReserva != 1)
                {
                    throw new Exception("La reservación no está en estado pendiente.");
                }

                // Verificar que no ha expirado
                if (fechaExpiracion.HasValue && fechaExpiracion.Value < DateTime.Now)
                {
                    throw new Exception("La reservación ha expirado.");
                }

                // 2. Verificar que todos los boletos tienen pasajeros asignados
                string queryVerificarPasajeros = @"
                    SELECT COUNT(*)
                    FROM Boleto
                    WHERE ReservacionID = @reservacionId
                      AND DatosPasajeroID IS NULL";

                using (var cmd = new SqlCommand(queryVerificarPasajeros, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    int boletosSinPasajero = (int)await cmd.ExecuteScalarAsync();

                    if (boletosSinPasajero > 0)
                    {
                        throw new Exception("Hay boletos sin pasajeros asignados. Por favor complete todos los datos.");
                    }
                }

                // 3. Cambiar estado de los boletos a Vendido (3)
                string updateBoletos = @"
                    UPDATE Boleto
                    SET EstadoBoletoID = 3
                    WHERE ReservacionID = @reservacionId";

                using (var cmd = new SqlCommand(updateBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 4. Cambiar estado de la reservación a Confirmada (2)
                string updateReservacion = @"
                    UPDATE Reservacion
                    SET EstadoReservaID = 2,
                        FechaExpiracion = NULL
                    WHERE ID = @reservacionId";

                using (var cmd = new SqlCommand(updateReservacion, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                transaction.Commit();
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
    }
}