using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AdminVueloService
    {
        private readonly AdminVueloRepository _adminVueloRepository;
        private readonly RutaRepository _rutaRepository;

        public AdminVueloService(AdminVueloRepository adminVueloRepository, RutaRepository rutaRepository)
        {
            _adminVueloRepository = adminVueloRepository;
            _rutaRepository = rutaRepository;
        }

        public async Task<int> CrearVuelo(CrearVueloAdminDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.NumeroVuelo))
                throw new ArgumentException("El número de vuelo es obligatorio");

            if (dto.AeropuertoOrigenId <= 0)
                throw new ArgumentException("Debe seleccionar un aeropuerto de origen");

            if (dto.AeropuertoDestinoId <= 0)
                throw new ArgumentException("Debe seleccionar un aeropuerto de destino");

            if (dto.AeropuertoOrigenId == dto.AeropuertoDestinoId)
                throw new ArgumentException("El aeropuerto de origen y destino no pueden ser iguales");

            if (dto.AvionId <= 0)
                throw new ArgumentException("Debe seleccionar un avión");

            if (dto.Fecha < DateTime.Now.Date)
                throw new ArgumentException("La fecha del vuelo no puede ser en el pasado");

            if (!TimeSpan.TryParse(dto.HoraSalida, out _))
                throw new ArgumentException("El formato de hora de salida es inválido");

            // NOTA: HoraLlegada y FechaLlegada ya NO se validan aquí.
            // Son calculadas automáticamente en el repositorio usando:
            //   DuracionEstimada de la Ruta + ZonaHoraria de los aeropuertos.

            if (dto.BoletosTurista < 0)
                throw new ArgumentException("Los boletos de clase turista no pueden ser negativos");

            if (dto.BoletosEjecutivo < 0)
                throw new ArgumentException("Los boletos de clase ejecutiva no pueden ser negativos");

            if (dto.BoletosTurista == 0 && dto.BoletosEjecutivo == 0)
                throw new ArgumentException("Debe asignar al menos un boleto turista o ejecutivo");

            if (dto.PrecioTurista <= 0)
                throw new ArgumentException("El precio de clase turista debe ser mayor a 0");

            if (dto.PrecioEjecutiva <= 0)
                throw new ArgumentException("El precio de clase ejecutiva debe ser mayor a 0");

            // Validar que exista una ruta entre los aeropuertos seleccionados
            bool rutaExiste = await _rutaRepository.ExisteRuta(
                dto.AeropuertoOrigenId, dto.AeropuertoDestinoId);

            if (!rutaExiste)
                throw new InvalidOperationException(
                    "No existe una ruta entre los aeropuertos seleccionados. " +
                    "Ve a 'Gestionar Rutas' y crea la ruta antes de crear el vuelo.");

            return await _adminVueloRepository.CrearVuelo(dto);
        }

        public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
        {
            return await _adminVueloRepository.ObtenerHistorialVuelos();
        }

        public async Task<bool> CancelarVuelo(int vueloId)
        {
            if (vueloId <= 0)
                throw new ArgumentException("ID de vuelo inválido");

            return await _adminVueloRepository.CancelarVuelo(vueloId);
        }
        // ── Disponibilidad ───────────────────────────────────────────────
        public async Task<HashSet<int>> ObtenerAvionesOcupados(
            DateTime fecha, TimeSpan horaSalida, int aeropuertoOrigenId)
            => await _adminVueloRepository.ObtenerAvionesOcupados(fecha, horaSalida, aeropuertoOrigenId);

        public async Task<HashSet<int>> ObtenerTripulantesOcupados(DateTime fecha, TimeSpan horaSalida)
            => await _adminVueloRepository.ObtenerTripulantesOcupados(fecha, horaSalida);

    }
}