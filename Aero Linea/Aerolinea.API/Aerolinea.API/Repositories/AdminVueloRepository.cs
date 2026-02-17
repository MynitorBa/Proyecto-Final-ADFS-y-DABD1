using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Aerolinea.API.Models.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AdminVueloRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public AdminVueloRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<int> CrearVuelo(CrearVueloAdminDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar o crear la ruta
                int rutaId = await ObtenerOCrearRuta(dto.AeropuertoOrigenId, dto.AeropuertoDestinoId, connection, transaction);

                // 2. Obtener capacidad del avión
                var capacidadAvion = await ObtenerCapacidadAvion(dto.AvionId, connection, transaction);

                // 3. Crear el vuelo
                var insertVueloQuery = @"
                    INSERT INTO Vuelo (NumeroVuelo, Fecha, HoraSalida, HoraLlegada, EstadoID, AvionID, RutaID, BoletosDisponibles)
                    OUTPUT INSERTED.ID
                    VALUES (@NumeroVuelo, @Fecha, @HoraSalida, @HoraLlegada, @EstadoId, @AvionId, @RutaId, @BoletosDisponibles)";

                using var commandVuelo = new SqlCommand(insertVueloQuery, connection, transaction);
                commandVuelo.Parameters.AddWithValue("@NumeroVuelo", dto.NumeroVuelo);
                commandVuelo.Parameters.AddWithValue("@Fecha", dto.Fecha.Date);
                commandVuelo.Parameters.AddWithValue("@HoraSalida", TimeSpan.Parse(dto.HoraSalida));
                commandVuelo.Parameters.AddWithValue("@HoraLlegada", TimeSpan.Parse(dto.HoraLlegada));
                commandVuelo.Parameters.AddWithValue("@EstadoId", 1); // 1 = Activo
                commandVuelo.Parameters.AddWithValue("@AvionId", dto.AvionId);
                commandVuelo.Parameters.AddWithValue("@RutaId", rutaId);
                commandVuelo.Parameters.AddWithValue("@BoletosDisponibles", capacidadAvion);

                var vueloId = (int)await commandVuelo.ExecuteScalarAsync();

                // 4. Crear boletos para el vuelo
                await CrearBoletos(vueloId, capacidadAvion, dto.PrecioTurista, dto.PrecioEjecutiva, connection, transaction);

                transaction.Commit();
                return vueloId;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        private async Task<int> ObtenerOCrearRuta(int origenId, int destinoId, SqlConnection connection, SqlTransaction transaction)
        {
            // Primero intentar obtener la ruta existente
            var queryBuscar = @"
                SELECT ID FROM Ruta 
                WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId";

            using var commandBuscar = new SqlCommand(queryBuscar, connection, transaction);
            commandBuscar.Parameters.AddWithValue("@OrigenId", origenId);
            commandBuscar.Parameters.AddWithValue("@DestinoId", destinoId);

            var resultado = await commandBuscar.ExecuteScalarAsync();

            if (resultado != null)
            {
                return (int)resultado;
            }

            // Si no existe, crear la ruta con duración estimada por defecto
            var queryCrear = @"
                INSERT INTO Ruta (OrigenID, DestinoID, DuracionEstimada)
                OUTPUT INSERTED.ID
                VALUES (@OrigenId, @DestinoId, @DuracionEstimada)";

            using var commandCrear = new SqlCommand(queryCrear, connection, transaction);
            commandCrear.Parameters.AddWithValue("@OrigenId", origenId);
            commandCrear.Parameters.AddWithValue("@DestinoId", destinoId);
            // Duración estimada por defecto de 120 minutos (2 horas)
            // El administrador puede actualizar esto después si es necesario
            commandCrear.Parameters.AddWithValue("@DuracionEstimada", 120);

            return (int)await commandCrear.ExecuteScalarAsync();
        }

        private async Task<int> ObtenerCapacidadAvion(int avionId, SqlConnection connection, SqlTransaction transaction)
        {
            var query = "SELECT CapacidadPasajeros FROM Avion WHERE ID = @AvionId";
            using var command = new SqlCommand(query, connection, transaction);
            command.Parameters.AddWithValue("@AvionId", avionId);

            var resultado = await command.ExecuteScalarAsync();
            return resultado != null ? (int)resultado : 0;
        }

        private async Task CrearBoletos(int vueloId, int capacidadAvion, decimal precioTurista, decimal precioEjecutiva, SqlConnection connection, SqlTransaction transaction)
        {
            // Calcular distribución de asientos: 25% Ejecutiva, 75% Turista
            int asientosEjecutiva = (int)(capacidadAvion * 0.25);
            int asientosTurista = capacidadAvion - asientosEjecutiva;

            var query = @"
                INSERT INTO Boleto (NoBoleto, NoAsiento, Precio, VueloID, ClaseID, EstadoBoletoID, ReservacionID, DatosPasajeroID)
                VALUES (@NoBoleto, @NoAsiento, @Precio, @VueloId, @ClaseId, @EstadoBoletoId, NULL, NULL)";

            // Crear boletos de Clase Ejecutiva
            for (int i = 1; i <= asientosEjecutiva; i++)
            {
                using var command = new SqlCommand(query, connection, transaction);
                command.Parameters.AddWithValue("@NoBoleto", $"BOL-{vueloId}-{i:D4}");
                command.Parameters.AddWithValue("@NoAsiento", $"{i}{(char)('A' + (i - 1) % 6)}"); // 1A, 2B, 3C, etc.
                command.Parameters.AddWithValue("@Precio", precioEjecutiva);
                command.Parameters.AddWithValue("@VueloId", vueloId);
                command.Parameters.AddWithValue("@ClaseId", 2); // Ejecutiva
                command.Parameters.AddWithValue("@EstadoBoletoId", 1); // Disponible
                await command.ExecuteNonQueryAsync();
            }

            // Crear boletos de Clase Turista
            for (int i = asientosEjecutiva + 1; i <= capacidadAvion; i++)
            {
                using var command = new SqlCommand(query, connection, transaction);
                command.Parameters.AddWithValue("@NoBoleto", $"BOL-{vueloId}-{i:D4}");
                command.Parameters.AddWithValue("@NoAsiento", $"{i}{(char)('A' + (i - 1) % 6)}");
                command.Parameters.AddWithValue("@Precio", precioTurista);
                command.Parameters.AddWithValue("@VueloId", vueloId);
                command.Parameters.AddWithValue("@ClaseId", 1); // Turista
                command.Parameters.AddWithValue("@EstadoBoletoId", 1); // Disponible
                await command.ExecuteNonQueryAsync();
            }
        }

        public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT 
                    v.ID,
                    v.NumeroVuelo,
                    aorigen.Codigo + ' - ' + corigen.Nombre AS CiudadOrigen,
                    adestino.Codigo + ' - ' + cdestino.Nombre AS CiudadDestino,
                    v.Fecha,
                    v.HoraSalida,
                    v.HoraLlegada,
                    v.EstadoID,
                    e.Estatus,
                    av.CapacidadPasajeros,
                    v.BoletosDisponibles,
                    (av.CapacidadPasajeros - v.BoletosDisponibles) AS AsientosVendidos
                FROM Vuelo v
                INNER JOIN Ruta r ON v.RutaID = r.ID
                INNER JOIN Aeropuerto aorigen ON r.OrigenID = aorigen.ID
                INNER JOIN Aeropuerto adestino ON r.DestinoID = adestino.ID
                INNER JOIN Ciudad corigen ON aorigen.CiudadID = corigen.ID
                INNER JOIN Ciudad cdestino ON adestino.CiudadID = cdestino.ID
                INNER JOIN Estado e ON v.EstadoID = e.ID
                INNER JOIN Avion av ON v.AvionID = av.ID
                ORDER BY v.Fecha DESC, v.HoraSalida DESC";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var vuelos = new List<VueloHistorialDTO>();

            while (await reader.ReadAsync())
            {
                var fecha = reader.GetDateTime(4);
                var estadoId = reader.GetInt32(7);
                var estadoRaw = reader.GetString(8).ToLower();
                string estado;

                // Determinar el estado basándose en EstadoID y fecha
                if (estadoId == 3 || estadoRaw.Contains("cancel"))
                {
                    // Vuelo cancelado
                    estado = "cancelado";
                }
                else if (estadoId == 2 || estadoRaw.Contains("complet"))
                {
                    // Vuelo completado
                    estado = "completado";
                }
                else if (estadoId == 1 || estadoRaw.Contains("activ") || estadoRaw.Contains("program") || estadoRaw.Contains("tiempo"))
                {
                    // Verificar si el vuelo ya pasó (comparar fecha + hora)
                    var horaSalida = reader.GetTimeSpan(5);
                    var fechaHoraSalida = fecha.Date.Add(horaSalida);

                    if (fechaHoraSalida < DateTime.Now)
                    {
                        estado = "completado";
                    }
                    else
                    {
                        estado = "activo";
                    }
                }
                else
                {
                    // Por defecto, verificar por fecha
                    if (fecha.Date < DateTime.Now.Date)
                    {
                        estado = "completado";
                    }
                    else
                    {
                        estado = "activo";
                    }
                }

                vuelos.Add(new VueloHistorialDTO
                {
                    Id = reader.GetInt32(0),
                    NumeroVuelo = reader.GetString(1),
                    Ruta = $"{reader.GetString(2)} → {reader.GetString(3)}",
                    Fecha = fecha.ToString("yyyy-MM-dd"),
                    HoraSalida = reader.GetTimeSpan(5).ToString(@"hh\:mm"),
                    HoraLlegada = reader.GetTimeSpan(6).ToString(@"hh\:mm"),
                    Estado = estado,
                    AsientosTotales = reader.GetInt32(9),
                    AsientosVendidos = reader.GetInt32(11)
                });
            }

            return vuelos;
        }

        public async Task<bool> CancelarVuelo(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var transaction = connection.BeginTransaction();

            try
            {
                // 1️⃣ Cancelar vuelo
                var queryVuelo = @"
            UPDATE Vuelo 
            SET EstadoID = 3
            WHERE ID = @VueloId AND EstadoID != 3";

                using var commandVuelo = new SqlCommand(queryVuelo, connection, transaction);
                commandVuelo.Parameters.AddWithValue("@VueloId", vueloId);

                var filasVuelo = await commandVuelo.ExecuteNonQueryAsync();


                // 2️⃣ Cancelar boletos del vuelo
                var queryBoletos = @"
            UPDATE Boleto
            SET EstadoBoletoID = 4
            WHERE VueloID = @VueloId";

                using var commandBoletos = new SqlCommand(queryBoletos, connection, transaction);
                commandBoletos.Parameters.AddWithValue("@VueloId", vueloId);

                await commandBoletos.ExecuteNonQueryAsync();


                // 3️⃣ Cancelar reservaciones asociadas a esos boletos
                var queryReservaciones = @"
            UPDATE r
            SET r.EstadoReservaID = 3
            FROM Reservacion r
            INNER JOIN Boleto b ON b.ReservacionID = r.ID
            WHERE b.VueloID = @VueloId";

                using var commandReservas = new SqlCommand(queryReservaciones, connection, transaction);
                commandReservas.Parameters.AddWithValue("@VueloId", vueloId);

                await commandReservas.ExecuteNonQueryAsync();


                transaction.Commit();
                return filasVuelo > 0;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

    }
}