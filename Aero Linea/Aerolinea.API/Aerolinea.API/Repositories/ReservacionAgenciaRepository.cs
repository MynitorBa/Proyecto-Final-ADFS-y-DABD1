using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de reservaciones para agencias. Gestiona la creacion de reservaciones
    /// con descuento aplicado, la expiracion manual de reservaciones pendientes y la
    /// asignacion de pasajeros a boletos de una reservacion existente.
    /// </summary>
    public class ReservacionAgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        private readonly PaisRepository _paisRepository;
        private readonly CiudadRepository _ciudadRepository;

        public ReservacionAgenciaRepository(
            DbConnectionFactory connectionFactory,
            PaisRepository paisRepository,
            CiudadRepository ciudadRepository)
        {
            _connectionFactory = connectionFactory;
            _paisRepository = paisRepository;
            _ciudadRepository = ciudadRepository;
        }

        /// <summary>
        /// Crea una reservacion para la agencia indicada aplicando el descuento configurado.
        /// Verifica disponibilidad de boletos con UPDLOCK/ROWLOCK, descuenta asientos del vuelo,
        /// asigna asientos secuenciales y retorna el DTO con todos los boletos reservados.
        /// La transaccion usa nivel Serializable para evitar condiciones de carrera.
        /// </summary>
        public async Task<ReservacionCreadaDTO> CrearReservacion(List<SeleccionVueloDTO> vuelos, decimal descuento, int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction(System.Data.IsolationLevel.Serializable);

            try
            {
                decimal factor = 1 - (descuento / 100);
                decimal total = 0;
                var boletosReservados = new List<BoletoReservadoDTO>();

                foreach (var vuelo in vuelos)
                {
                    string campoDisponible = vuelo.ClaseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string campoPrecio = vuelo.ClaseId == 1 ? "PrecioTurista" : "PrecioEjecutivo";

                    string queryVerificar = $"SELECT {campoDisponible}, {campoPrecio} FROM Vuelo WITH (UPDLOCK, ROWLOCK) WHERE ID = @vueloId";

                    int disponibles = 0;
                    decimal precio = 0;

                    using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        if (!await reader.ReadAsync())
                            throw new Exception($"El vuelo {vuelo.VueloId} no existe.");
                        disponibles = reader.IsDBNull(0) ? 0 : reader.GetInt32(0);
                        precio = reader.IsDBNull(1) ? 0 : reader.GetDecimal(1);
                    }

                    if (disponibles < vuelo.CantidadPasajeros)
                    {
                        string clase = vuelo.ClaseId == 1 ? "Turista" : "Ejecutivo";
                        throw new Exception(
                            $"No hay suficientes boletos de clase {clase} en el vuelo {vuelo.VueloId}. " +
                            $"Disponibles: {disponibles}, solicitados: {vuelo.CantidadPasajeros}.");
                    }

                    decimal precioConDescuento = Math.Round(precio * factor, 2);
                    total += precioConDescuento * vuelo.CantidadPasajeros;
                }

                int usuarioWebId;
                string queryUsuario = "SELECT UsuarioWebID FROM Agencia WHERE ID = @agenciaId";
                using (var cmd = new SqlCommand(queryUsuario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
                    usuarioWebId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                string noReservacion = "RES" + DateTime.Now.ToString("yyyyMMddHHmmss") + new Random().Next(1000, 9999);
                DateTime fechaExpiracion = DateTime.Now.AddMinutes(10);
                int reservacionId;

                string insertReservacion = @"
                    INSERT INTO Reservacion (NoReservacion, UsuarioID, FechaReservacion, FechaExpiracion, Total, EstadoReservaID)
                    VALUES (@noReservacion, @usuarioId, GETDATE(), @fechaExpiracion, @total, 1);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                using (var cmd = new SqlCommand(insertReservacion, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@noReservacion", noReservacion);
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioWebId);
                    cmd.Parameters.AddWithValue("@fechaExpiracion", fechaExpiracion);
                    cmd.Parameters.AddWithValue("@total", total);
                    reservacionId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                foreach (var vuelo in vuelos)
                {
                    string campoDisponible = vuelo.ClaseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string campoPrecio = vuelo.ClaseId == 1 ? "PrecioTurista" : "PrecioEjecutivo";
                    string nombreClase = vuelo.ClaseId == 1 ? "Turista" : "Ejecutivo";

                    string updateVuelo = $"UPDATE Vuelo SET {campoDisponible} = {campoDisponible} - @cantidad WHERE ID = @vueloId";
                    using (var cmd = new SqlCommand(updateVuelo, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@cantidad", vuelo.CantidadPasajeros);
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    decimal precioClase = 0;
                    string queryPrecio = $"SELECT {campoPrecio} FROM Vuelo WHERE ID = @vueloId";
                    using (var cmd = new SqlCommand(queryPrecio, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        var result = await cmd.ExecuteScalarAsync();
                        precioClase = Math.Round(Convert.ToDecimal(result) * factor, 2);
                    }

                    var asientosOcupados = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
                    string queryAsientos = @"
                        SELECT NoAsiento FROM Boleto
                        WHERE VueloID = @vueloId AND ClaseID = @claseId AND EstadoBoletoID IN (2, 3)";

                    using (var cmd = new SqlCommand(queryAsientos, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        cmd.Parameters.AddWithValue("@claseId", vuelo.ClaseId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        while (await reader.ReadAsync())
                            asientosOcupados.Add(reader.GetString(0));
                    }

                    string asientoActual = null;

                    for (int i = 0; i < vuelo.CantidadPasajeros; i++)
                    {
                        do
                        {
                            asientoActual = SiguienteAsiento(asientoActual, vuelo.ClaseId);
                        }
                        while (asientosOcupados.Contains(asientoActual));

                        asientosOcupados.Add(asientoActual);

                        string noBoleto = $"BOL{reservacionId}{vuelo.VueloId}{i}{Guid.NewGuid().ToString("N")[..8].ToUpper()}";

                        string insertBoleto = @"
                            INSERT INTO Boleto (NoBoleto, NoAsiento, Precio, VueloID, ClaseID, EstadoBoletoID, ReservacionID)
                            VALUES (@noBoleto, @noAsiento, @precio, @vueloId, @claseId, 2, @reservacionId);
                            SELECT CAST(SCOPE_IDENTITY() AS INT);";

                        int boletoId;
                        using (var cmd = new SqlCommand(insertBoleto, connection, transaction))
                        {
                            cmd.Parameters.AddWithValue("@noBoleto", noBoleto);
                            cmd.Parameters.AddWithValue("@noAsiento", asientoActual);
                            cmd.Parameters.AddWithValue("@precio", precioClase);
                            cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                            cmd.Parameters.AddWithValue("@claseId", vuelo.ClaseId);
                            cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                            boletoId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                        }

                        boletosReservados.Add(new BoletoReservadoDTO
                        {
                            BoletoId = boletoId,
                            NoBoleto = noBoleto,
                            NoAsiento = asientoActual,
                            Precio = precioClase,
                            NumeroVuelo = vuelo.VueloId.ToString(),
                            Clase = nombreClase
                        });
                    }
                }

                transaction.Commit();

                return new ReservacionCreadaDTO
                {
                    ReservacionId = reservacionId,
                    NoReservacion = noReservacion,
                    FechaExpiracion = fechaExpiracion,
                    Total = total,
                    Boletos = boletosReservados,
                    MinutosRestantes = 10
                };
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        private string SiguienteAsiento(string ultimoAsiento, int claseId)
        {
            string prefijo = claseId == 2 ? "E-" : "";
            int columnas = 6;

            if (ultimoAsiento == null)
                return $"{prefijo}A1";

            string raw = (prefijo != "" && ultimoAsiento.StartsWith(prefijo))
                ? ultimoAsiento.Substring(prefijo.Length)
                : ultimoAsiento;

            int splitIdx = 0;
            while (splitIdx < raw.Length && char.IsLetter(raw[splitIdx])) splitIdx++;

            string filaLetras = raw.Substring(0, splitIdx);
            int columna = int.Parse(raw.Substring(splitIdx));

            if (columna < columnas)
                return $"{prefijo}{filaLetras}{columna + 1}";
            else
                return $"{prefijo}{SiguienteLetraFila(filaLetras)}1";
        }

        private string SiguienteLetraFila(string fila)
        {
            char[] letras = fila.ToCharArray();
            int i = letras.Length - 1;

            while (i >= 0)
            {
                if (letras[i] < 'Z') { letras[i]++; return new string(letras); }
                letras[i] = 'A';
                i--;
            }

            return "A" + new string(letras);
        }

        /// <summary>
        /// Marca como expirada una reservacion pendiente de la agencia. Libera los boletos
        /// reservados devolviendo disponibilidad al vuelo correspondiente y cambia el estado
        /// de la reservacion a expirado (4). Lanza excepcion si la reservacion no esta pendiente.
        /// </summary>
        public async Task ExpirarReservacion(int reservacionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                string queryVerificar = "SELECT EstadoReservaID FROM Reservacion WHERE ID = @id";
                int estado = 0;
                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@id", reservacionId);
                    var result = await cmd.ExecuteScalarAsync();
                    if (result == null)
                        throw new Exception("Reservación no encontrada.");
                    estado = Convert.ToInt32(result);
                }

                if (estado != 1)
                    throw new Exception("La reservación no está en estado pendiente.");

                // Liberar boletos
                string queryGrupos = @"
            SELECT VueloID, ClaseID, COUNT(*) AS Cantidad
            FROM Boleto
            WHERE ReservacionID = @id AND EstadoBoletoID = 2
            GROUP BY VueloID, ClaseID";

                var grupos = new List<(int VueloId, int ClaseId, int Cantidad)>();
                using (var cmd = new SqlCommand(queryGrupos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@id", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        grupos.Add((reader.GetInt32(0), reader.GetInt32(1), reader.GetInt32(2)));
                }

                string cancelarBoletos = @"
            UPDATE Boleto SET EstadoBoletoID = 4
            WHERE ReservacionID = @id AND EstadoBoletoID = 2";
                using (var cmd = new SqlCommand(cancelarBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@id", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                foreach (var (vueloId, claseId, cantidad) in grupos)
                {
                    string campo = claseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string devolver = $"UPDATE Vuelo SET {campo} = {campo} + @cantidad WHERE ID = @vueloId";
                    using var cmd = new SqlCommand(devolver, connection, transaction);
                    cmd.Parameters.AddWithValue("@cantidad", cantidad);
                    cmd.Parameters.AddWithValue("@vueloId", vueloId);
                    await cmd.ExecuteNonQueryAsync();
                }

                string expirar = "UPDATE Reservacion SET EstadoReservaID = 4 WHERE ID = @id";
                using (var cmd = new SqlCommand(expirar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@id", reservacionId);
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

        /// <summary>
        /// Verifica si una reservacion pertenece a la agencia indicada y se encuentra
        /// en estado pendiente (1). Retorna true si se cumplen ambas condiciones.
        /// </summary>
        public async Task<bool> PerteneceAAgenciaYEstaPendiente(int reservacionId, int agenciaId)
        {
                    using var connection = _connectionFactory.CreateConnection();
                    await connection.OpenAsync();

                    string sql = @"
                SELECT COUNT(*) FROM Reservacion r
                JOIN Agencia a ON r.UsuarioID = a.UsuarioWebID
                WHERE r.ID = @reservacionId
                AND a.ID = @agenciaId
                AND r.EstadoReservaID = 1";

            using var cmd = new SqlCommand(sql, connection);
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@agenciaId", agenciaId);

                    int count = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                    return count > 0;
        }

        /// <summary>
        /// Asigna o actualiza los datos de pasajero para cada boleto de una reservacion de agencia.
        /// Verifica que la reservacion pertenezca a la agencia, este pendiente y no haya expirado.
        /// Crea o actualiza registros en DatosPasajero y los vincula al boleto correspondiente.
        /// </summary>
        public async Task AgregarPasajerosAReservacion(int reservacionId, List<DatosPasajeroDTO> pasajeros, int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // Verificar que la reservación existe, pertenece a la agencia y está pendiente
                string queryVerificar = @"
            SELECT r.EstadoReservaID, r.FechaExpiracion
            FROM Reservacion r
            JOIN Agencia a ON r.UsuarioID = a.UsuarioWebID
            WHERE r.ID = @reservacionId AND a.ID = @agenciaId";

                int estadoReserva = 0;
                DateTime? fechaExpiracion = null;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("La reservación no existe o no pertenece a esta agencia.");
                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1)) fechaExpiracion = reader.GetDateTime(1);
                }

                if (estadoReserva != 1)
                    throw new Exception("La reservación no está en estado pendiente.");
                if (fechaExpiracion.HasValue && fechaExpiracion.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado.");

                foreach (var pasajero in pasajeros)
                {
                    string queryVerificarBoleto = "SELECT ReservacionID, DatosPasajeroID FROM Boleto WHERE ID = @boletoId";

                    int? reservacionDelBoleto = null;
                    int? pasajeroExistente = null;

                    using (var cmd = new SqlCommand(queryVerificarBoleto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@boletoId", pasajero.BoletoId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        if (!await reader.ReadAsync())
                            throw new Exception($"El boleto {pasajero.BoletoId} no existe.");
                        if (!reader.IsDBNull(0)) reservacionDelBoleto = reader.GetInt32(0);
                        if (!reader.IsDBNull(1)) pasajeroExistente = reader.GetInt32(1);
                    }

                    if (reservacionDelBoleto != reservacionId)
                        throw new Exception($"El boleto {pasajero.BoletoId} no pertenece a esta reservación.");

                    int paisId = await _paisRepository.ObtenerOCrearId(pasajero.Pais, connection, transaction);
                    int ciudadId = await _ciudadRepository.ObtenerOCrearId(pasajero.Ciudad, paisId, connection, transaction);

                    int datosPasajeroId;

                    if (pasajeroExistente.HasValue)
                    {
                        string updatePasajero = @"
                    UPDATE DatosPasajero
                    SET Nombre = @nombre, Apellido = @apellido, Pasaporte = @pasaporte,
                        Telefono = @telefono, CiudadID = @ciudadId
                    WHERE ID = @id";

                        using var cmd = new SqlCommand(updatePasajero, connection, transaction);
                        cmd.Parameters.AddWithValue("@id", pasajeroExistente.Value);
                        cmd.Parameters.AddWithValue("@nombre", pasajero.Nombre);
                        cmd.Parameters.AddWithValue("@apellido", pasajero.Apellido);
                        cmd.Parameters.AddWithValue("@pasaporte", pasajero.Pasaporte);
                        cmd.Parameters.AddWithValue("@telefono", pasajero.Telefono);
                        cmd.Parameters.AddWithValue("@ciudadId", ciudadId);
                        await cmd.ExecuteNonQueryAsync();
                        datosPasajeroId = pasajeroExistente.Value;
                    }
                    else
                    {
                        string insertPasajero = @"
                    INSERT INTO DatosPasajero (Nombre, Apellido, Pasaporte, Telefono, CiudadID)
                    VALUES (@nombre, @apellido, @pasaporte, @telefono, @ciudadId);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                        using var cmd = new SqlCommand(insertPasajero, connection, transaction);
                        cmd.Parameters.AddWithValue("@nombre", pasajero.Nombre);
                        cmd.Parameters.AddWithValue("@apellido", pasajero.Apellido);
                        cmd.Parameters.AddWithValue("@pasaporte", pasajero.Pasaporte);
                        cmd.Parameters.AddWithValue("@telefono", pasajero.Telefono);
                        cmd.Parameters.AddWithValue("@ciudadId", ciudadId);
                        datosPasajeroId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                    }

                    string updateBoleto = "UPDATE Boleto SET DatosPasajeroID = @datosPasajeroId WHERE ID = @boletoId";
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
    }
}
