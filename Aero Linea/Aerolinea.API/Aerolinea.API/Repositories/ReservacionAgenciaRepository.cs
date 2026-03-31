using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
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

        public async Task<ReservacionCreadaDTO> CrearReservacion(List<SeleccionVueloDTO> vuelos, decimal descuento)
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

                string noReservacion = "RES" + DateTime.Now.ToString("yyyyMMddHHmmss") + new Random().Next(1000, 9999);
                DateTime fechaExpiracion = DateTime.Now.AddMinutes(15);
                int reservacionId;

                string insertReservacion = @"
                    INSERT INTO Reservacion (NoReservacion, UsuarioID, FechaReservacion, FechaExpiracion, Total, EstadoReservaID)
                    VALUES (@noReservacion, NULL, GETDATE(), @fechaExpiracion, @total, 1);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                using (var cmd = new SqlCommand(insertReservacion, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@noReservacion", noReservacion);
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
                        precioClase = Math.Round(Convert.ToDecimal(result) * (1 - descuento / 100), 2);
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
                    MinutosRestantes = 15
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
    }
}