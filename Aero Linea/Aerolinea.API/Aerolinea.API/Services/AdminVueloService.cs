using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de administracion de vuelos. Contiene la logica de negocio para crear,
    /// cancelar y consultar vuelos, asi como verificar disponibilidad de aviones y tripulantes.
    /// </summary>
    public class AdminVueloService
    {
        private readonly AdminVueloRepository _adminVueloRepository;
        private readonly RutaRepository _rutaRepository;

        /// <summary>
        /// Inicializa el servicio con los repositorios necesarios para la gestion de vuelos y rutas.
        /// </summary>
        public AdminVueloService(AdminVueloRepository adminVueloRepository, RutaRepository rutaRepository)
        {
            _adminVueloRepository = adminVueloRepository;
            _rutaRepository = rutaRepository;
        }

        /// <summary>
        /// Crea un nuevo vuelo aplicando validaciones sobre numero de vuelo, aeropuertos, avion,
        /// fecha, horario, cantidad de boletos, precios y existencia de ruta entre los aeropuertos.
        /// Retorna el ID del vuelo creado.
        /// </summary>
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

        /// <summary>
        /// Retorna el historial completo de vuelos registrados en el sistema,
        /// incluyendo vuelos pasados, activos y cancelados.
        /// </summary>
        public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
        {
            return await _adminVueloRepository.ObtenerHistorialVuelos();
        }

        /// <summary>
        /// Cancela un vuelo existente dado su identificador. Valida que el ID sea mayor a cero
        /// antes de proceder con la cancelacion en el repositorio.
        /// </summary>
        public async Task<bool> CancelarVuelo(int vueloId)
        {
            if (vueloId <= 0)
                throw new ArgumentException("ID de vuelo inválido");

            return await _adminVueloRepository.CancelarVuelo(vueloId);
        }

        /// <summary>
        /// Retorna el conjunto de IDs de aviones que ya tienen un vuelo programado
        /// para la fecha, hora de salida y aeropuerto de origen indicados.
        /// Permite filtrar aviones no disponibles al momento de crear un vuelo nuevo.
        /// </summary>
        public async Task<HashSet<int>> ObtenerAvionesOcupados(
            DateTime fecha, TimeSpan horaSalida, int aeropuertoOrigenId)
            => await _adminVueloRepository.ObtenerAvionesOcupados(fecha, horaSalida, aeropuertoOrigenId);

        /// <summary>
        /// Retorna el conjunto de IDs de tripulantes que ya estan asignados a algun vuelo
        /// en la fecha y hora de salida indicadas. Permite evitar conflictos de asignacion.
        /// </summary>
        public async Task<HashSet<int>> ObtenerTripulantesOcupados(DateTime fecha, TimeSpan horaSalida)
            => await _adminVueloRepository.ObtenerTripulantesOcupados(fecha, horaSalida);

    }
}
