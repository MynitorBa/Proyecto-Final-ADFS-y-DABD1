using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class VueloRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public VueloRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        //  BÚSQUEDA DIRECTA  (sin cambios)
        public async Task<List<VueloDetalleDTO>> BuscarVuelos(
            int origenId, int destinoId, DateTime fecha,
            int cantidadPasajeros, int? claseId = null)
        {
            var vuelos = new List<VueloDetalleDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string filtroClase = claseId == 1
                ? "AND v.BoletosTurista   >= @cantidadPasajeros"
                : claseId == 2
                    ? "AND v.BoletosEjecutivo >= @cantidadPasajeros"
                    : "AND (v.BoletosTurista >= @cantidadPasajeros OR v.BoletosEjecutivo >= @cantidadPasajeros)";

            string query = $@"
                SELECT 
                    v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                    e.ID AS EstadoId, e.Estatus,
                    a.ID AS AvionId, a.Modelo, a.Marca, a.CapacidadPasajeros,
                    ao.ID, ao.Nombre, ao.Codigo, co.Nombre, po.Nombre,
                    ad.ID, ad.Nombre, ad.Codigo, cd.Nombre, pd.Nombre,
                    r.ID AS RutaId, r.DuracionEstimada,
                    v.PrecioTurista, v.PrecioEjecutivo,
                    v.BoletosTurista, v.BoletosEjecutivo,
                    v.FechaLlegada
                FROM Vuelo v
                INNER JOIN Estado     e  ON v.EstadoID  = e.ID
                INNER JOIN Avion      a  ON v.AvionID   = a.ID
                INNER JOIN Ruta       r  ON v.RutaID    = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID  = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
                INNER JOIN Pais       po ON co.PaisID   = po.ID
                INNER JOIN Pais       pd ON cd.PaisID   = pd.ID
                WHERE r.OrigenID  = @origenId
                  AND r.DestinoID = @destinoId
                  AND v.Fecha     = @fecha
                  AND e.Estatus   = 'A tiempo'
                  {filtroClase}
                ORDER BY v.HoraSalida";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@origenId", origenId);
            cmd.Parameters.AddWithValue("@destinoId", destinoId);
            cmd.Parameters.AddWithValue("@fecha", fecha.Date);
            cmd.Parameters.AddWithValue("@cantidadPasajeros", cantidadPasajeros);

            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
                vuelos.Add(MapearVuelo(reader));

            reader.Close();

            foreach (var vuelo in vuelos)
                vuelo.Tripulantes = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);

            return vuelos;
        }

        //  BÚSQUEDA CON 1 ESCALA
        public async Task<List<VueloConEscalaDTO>> BuscarVuelosConEscala(
            int origenId, int destinoId, DateTime fecha,
            int cantidadPasajeros, int? claseId = null)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // ── 1. Duración estimada del vuelo directo (si existe) ────────
            int? duracionDirectaMinutos = null;
            string queryDirecta = @"
                SELECT TOP 1 DuracionEstimada FROM Ruta
                WHERE OrigenID = @origenId AND DestinoID = @destinoId";

            using (var cmd = new SqlCommand(queryDirecta, connection))
            {
                cmd.Parameters.AddWithValue("@origenId", origenId);
                cmd.Parameters.AddWithValue("@destinoId", destinoId);
                var result = await cmd.ExecuteScalarAsync();
                if (result != null && result != DBNull.Value)
                    duracionDirectaMinutos = (int)result;
            }

            // Límite de duración total: 1.5× la directa, o 12h si no hay ruta directa
            int limiteMinutos = duracionDirectaMinutos.HasValue
                ? (int)(duracionDirectaMinutos.Value * 1.5)
                : 12 * 60;

            // ── 2. Traer tramos 1: origen → aeropuerto intermedio (día pedido) ──
            var tramos1 = await BuscarTramos(
                connection, origenId, destinoId,
                fecha, cantidadPasajeros, claseId, esOrigen: true);

            // ── 3. Traer tramos 2: aeropuerto intermedio → destino ────────
            var tramos2DiaMismo = await BuscarTramos(
                connection, origenId, destinoId,
                fecha, cantidadPasajeros, claseId, esOrigen: false);

            var tramos2DiaSiguiente = await BuscarTramos(
                connection, origenId, destinoId,
                fecha.AddDays(1), cantidadPasajeros, claseId, esOrigen: false);

            var tramos2 = tramos2DiaMismo.Concat(tramos2DiaSiguiente).ToList();

            // ── 4. Cruzar y filtrar ───────────────────────────────────────
            var resultados = new List<VueloConEscalaDTO>();
            var combinacionesVistas = new HashSet<string>();

            foreach (var t1 in tramos1)
            {
                // El aeropuerto de escala es el destino del tramo 1
                foreach (var t2 in tramos2.Where(t => t.OrigenId == t1.DestinoId))
                {
                    // Cálculo limpio con FechaLlegada — sin lógica de medianoche
                    var llegadaT1 = t1.FechaLlegada + t1.HoraLlegada;
                    var salidaT2 = t2.Fecha.Date + t2.HoraSalida;

                    int minutosEscala = (int)(salidaT2 - llegadaT1).TotalMinutes;

                    // Ventana de escala: 1h a 12h
                    if (minutosEscala < 60 || minutosEscala > 720)
                        continue;

                    int duracionTotal = t1.DuracionMinutos + minutosEscala + t2.DuracionMinutos;

                    // Filtro de 1.5× duración directa
                    if (duracionTotal > limiteMinutos)
                        continue;

                    // Evitar duplicados (misma combinación de vuelos)
                    string key = $"{t1.Id}-{t2.Id}";
                    if (!combinacionesVistas.Add(key))
                        continue;

                    // Disponibilidad = mínimo entre ambos tramos por clase
                    int? dispTurista = t1.BoletosDisponiblesTurista.HasValue && t2.BoletosDisponiblesTurista.HasValue
                        ? Math.Min(t1.BoletosDisponiblesTurista.Value, t2.BoletosDisponiblesTurista.Value)
                        : null;

                    int? dispEjecutiva = t1.BoletosDisponiblesEjecutiva.HasValue && t2.BoletosDisponiblesEjecutiva.HasValue
                        ? Math.Min(t1.BoletosDisponiblesEjecutiva.Value, t2.BoletosDisponiblesEjecutiva.Value)
                        : null;

                    // Filtrar por disponibilidad de la clase pedida
                    if (claseId == 1 && (dispTurista == null || dispTurista < cantidadPasajeros)) continue;
                    if (claseId == 2 && (dispEjecutiva == null || dispEjecutiva < cantidadPasajeros)) continue;
                    if (claseId == null &&
                        (dispTurista == null || dispTurista < cantidadPasajeros) &&
                        (dispEjecutiva == null || dispEjecutiva < cantidadPasajeros))
                        continue;

                    // Precio total = suma de ambos tramos
                    decimal? precioTuristaTotal = t1.PrecioTurista.HasValue && t2.PrecioTurista.HasValue
                        ? t1.PrecioTurista + t2.PrecioTurista : null;
                    decimal? precioEjecutivaTotal = t1.PrecioEjecutiva.HasValue && t2.PrecioEjecutiva.HasValue
                        ? t1.PrecioEjecutiva + t2.PrecioEjecutiva : null;

                    resultados.Add(new VueloConEscalaDTO
                    {
                        NumeroEscalas = 1,
                        DuracionTotalMinutos = duracionTotal,
                        TiempoEscalaMinutos = minutosEscala,
                        PrecioTuristaTotal = precioTuristaTotal,
                        PrecioEjecutivaTotal = precioEjecutivaTotal,
                        BoletosDisponiblesTurista = dispTurista,
                        BoletosDisponiblesEjecutiva = dispEjecutiva,
                        Tramos = new List<VueloDetalleDTO> { t1, t2 }
                    });
                }
            }

            // Cargar tripulantes para todos los tramos únicos
            var vuelosUnicos = resultados
                .SelectMany(r => r.Tramos)
                .GroupBy(v => v.Id)
                .Select(g => g.First())
                .ToList();

            var tripulantesPorVuelo = new Dictionary<int, List<TripulanteDTO>>();
            foreach (var vuelo in vuelosUnicos)
                tripulantesPorVuelo[vuelo.Id] = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);

            foreach (var resultado in resultados)
                foreach (var tramo in resultado.Tramos)
                    tramo.Tripulantes = tripulantesPorVuelo[tramo.Id];

            // Ordenar por duración total
            return resultados.OrderBy(r => r.DuracionTotalMinutos).ToList();
        }

        //  TRAER TRAMOS (origen fijo o destino fijo)
        private async Task<List<VueloDetalleDTO>> BuscarTramos(
            SqlConnection connection,
            int origenId, int destinoId,
            DateTime fecha, int cantidadPasajeros, int? claseId,
            bool esOrigen)
        {
            // esOrigen=true  → buscamos vuelos que SALEN del origenId (tramo 1)
            //                   pero NO llegan al destino final (eso sería directo)
            // esOrigen=false → buscamos vuelos que LLEGAN al destinoId (tramo 2)
            //                   pero NO salen del origen original (eso sería directo)

            string filtroClase = claseId == 1
                ? "AND v.BoletosTurista   >= @cantidadPasajeros"
                : claseId == 2
                    ? "AND v.BoletosEjecutivo >= @cantidadPasajeros"
                    : "AND (v.BoletosTurista >= @cantidadPasajeros OR v.BoletosEjecutivo >= @cantidadPasajeros)";

            string filtroPosicion = esOrigen
                ? "AND r.OrigenID  = @fijoId AND r.DestinoID != @excluirId"
                : "AND r.DestinoID = @fijoId AND r.OrigenID  != @excluirId";

            string query = $@"
                SELECT 
                    v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                    e.ID AS EstadoId, e.Estatus,
                    a.ID AS AvionId, a.Modelo, a.Marca, a.CapacidadPasajeros,
                    ao.ID, ao.Nombre, ao.Codigo, co.Nombre, po.Nombre,
                    ad.ID, ad.Nombre, ad.Codigo, cd.Nombre, pd.Nombre,
                    r.ID AS RutaId, r.DuracionEstimada,
                    v.PrecioTurista, v.PrecioEjecutivo,
                    v.BoletosTurista, v.BoletosEjecutivo,
                    v.FechaLlegada
                FROM Vuelo v
                INNER JOIN Estado     e  ON v.EstadoID  = e.ID
                INNER JOIN Avion      a  ON v.AvionID   = a.ID
                INNER JOIN Ruta       r  ON v.RutaID    = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID  = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
                INNER JOIN Pais       po ON co.PaisID   = po.ID
                INNER JOIN Pais       pd ON cd.PaisID   = pd.ID
                WHERE v.Fecha   = @fecha
                  AND e.Estatus = 'A tiempo'
                  {filtroPosicion}
                  {filtroClase}
                ORDER BY v.HoraSalida";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@fecha", fecha.Date);
            cmd.Parameters.AddWithValue("@cantidadPasajeros", cantidadPasajeros);
            cmd.Parameters.AddWithValue("@fijoId", esOrigen ? origenId : destinoId);
            cmd.Parameters.AddWithValue("@excluirId", esOrigen ? destinoId : origenId);

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<VueloDetalleDTO>();
            while (await reader.ReadAsync())
                lista.Add(MapearVuelo(reader));

            return lista;
        }

        //  GUARDAR BÚSQUEDA
        public async Task GuardarBusqueda(
            int origenId, int destinoId, DateTime fechaSalida,
            int cantidadPersonas, int? usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            int? rutaId = null;
            using (var cmd = new SqlCommand(
                "SELECT TOP 1 ID FROM Ruta WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId", connection))
            {
                cmd.Parameters.AddWithValue("@OrigenId", origenId);
                cmd.Parameters.AddWithValue("@DestinoId", destinoId);
                var result = await cmd.ExecuteScalarAsync();
                if (result != null) rutaId = (int)result;
            }

            if (!rutaId.HasValue) return;

            using var cmdInsert = new SqlCommand(@"
                INSERT INTO Busqueda (RutaID, FechaSalida, CantidadPersonas, UsuarioID, TipoBusquedaID, Fecha)
                VALUES (@RutaId, @FechaSalida, @CantidadPersonas, @UsuarioId, 1, @FechaHoy)", connection);

            cmdInsert.Parameters.AddWithValue("@RutaId", rutaId.Value);
            cmdInsert.Parameters.AddWithValue("@FechaSalida", fechaSalida.Date);
            cmdInsert.Parameters.AddWithValue("@CantidadPersonas", cantidadPersonas);
            cmdInsert.Parameters.AddWithValue("@UsuarioId", usuarioId.HasValue ? (object)usuarioId.Value : DBNull.Value);
            cmdInsert.Parameters.AddWithValue("@FechaHoy", DateTime.Now);

            await cmdInsert.ExecuteNonQueryAsync();
        }

        

        private VueloDetalleDTO MapearVuelo(SqlDataReader reader)
        {
            return new VueloDetalleDTO
            {
                Id = reader.GetInt32(0),
                NumeroVuelo = reader.GetString(1),
                Fecha = reader.GetDateTime(2),
                HoraSalida = reader.IsDBNull(3) ? TimeSpan.Zero : reader.GetTimeSpan(3),
                HoraLlegada = reader.IsDBNull(4) ? TimeSpan.Zero : reader.GetTimeSpan(4),
                EstadoId = reader.GetInt32(5),
                Estado = reader.GetString(6),
                AvionId = reader.GetInt32(7),
                AvionModelo = reader.GetString(8),
                AvionMarca = reader.GetString(9),
                CapacidadPasajeros = reader.GetInt32(10),
                OrigenId = reader.GetInt32(11),
                OrigenNombre = reader.GetString(12),
                OrigenCodigo = reader.GetString(13),
                OrigenCiudad = reader.GetString(14),
                OrigenPais = reader.GetString(15),
                DestinoId = reader.GetInt32(16),
                DestinoNombre = reader.GetString(17),
                DestinoCodigo = reader.GetString(18),
                DestinoCiudad = reader.GetString(19),
                DestinoPais = reader.GetString(20),
                RutaId = reader.GetInt32(21),
                DuracionMinutos = reader.IsDBNull(22) ? 0 : reader.GetInt32(22),
                PrecioTurista = reader.IsDBNull(23) ? null : reader.GetDecimal(23),
                PrecioEjecutiva = reader.IsDBNull(24) ? null : reader.GetDecimal(24),
                BoletosDisponiblesTurista = reader.IsDBNull(25) ? null : reader.GetInt32(25),
                BoletosDisponiblesEjecutiva = reader.IsDBNull(26) ? null : reader.GetInt32(26),
                FechaLlegada = reader.GetDateTime(27),
                Tripulantes = new List<TripulanteDTO>()
            };
        }

        private async Task<List<TripulanteDTO>> ObtenerTripulantesPorVuelo(SqlConnection connection, int vueloId)
        {
            var tripulantes = new List<TripulanteDTO>();

            string query = @"
                SELECT mt.ID, mt.Nombre, mt.Apellido, mt.RolID, rt.Cargo
                FROM EquipoPivote ep
                INNER JOIN MiembroTripulacion mt ON ep.MiembroTripulacionID = mt.ID
                INNER JOIN RolTripulacion     rt ON mt.RolID = rt.ID
                WHERE ep.VueloID = @vueloId
                ORDER BY rt.Cargo, mt.Nombre";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@vueloId", vueloId);
            using var reader = await cmd.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                tripulantes.Add(new TripulanteDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    NombreRol = reader.GetString(4),
                    NombreCompleto = $"{reader.GetString(1)} {reader.GetString(2)}"
                });
            }

            return tripulantes;
        }
    }
}