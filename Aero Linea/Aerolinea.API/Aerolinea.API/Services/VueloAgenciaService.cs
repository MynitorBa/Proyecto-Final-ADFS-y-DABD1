using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de busqueda de vuelos para agencias. Resuelve la ciudad y aeropuerto
    /// a partir del nombre de pais y ciudad provistos por la agencia, aplica el descuento
    /// configurado para la agencia y retorna vuelos directos y con escala con precios ajustados.
    /// </summary>
    public class VueloAgenciaService
    {
        private readonly VueloRepository _vueloRepository;
        private readonly PaisRepository _paisRepository;
        private readonly CiudadRepository _ciudadRepository;
        private readonly AgenciaRepository _agenciaRepository;
        private readonly AeropuertoRepository _aeropuertoRepository;

        /// <summary>
        /// Inicializa el servicio con los repositorios necesarios para resolver ubicaciones,
        /// obtener el descuento de la agencia y consultar vuelos disponibles.
        /// </summary>
        public VueloAgenciaService(
            VueloRepository vueloRepository,
            PaisRepository paisRepository,
            CiudadRepository ciudadRepository,
            AgenciaRepository agenciaRepository,
            AeropuertoRepository aeropuertoRepository)
        {
            _vueloRepository = vueloRepository;
            _paisRepository = paisRepository;
            _ciudadRepository = ciudadRepository;
            _agenciaRepository = agenciaRepository;
            _aeropuertoRepository = aeropuertoRepository;
        }

        /// <summary>
        /// Busca vuelos disponibles para una agencia resolviendo primero los aeropuertos de origen
        /// y destino a partir de los nombres de pais y ciudad. Obtiene el descuento de la agencia,
        /// busca vuelos directos y con escala, y aplica el factor de descuento a todos los precios
        /// antes de retornar los resultados.
        /// </summary>
        public async Task<ResultadoBusquedaDTO> BuscarVuelos(BuscarVueloAgenciaDTO dto, int agenciaId)
        {
            // 1. Obtener descuento de la agencia (solo para registrar, NO aplicar en búsqueda)
            decimal descuento = await _agenciaRepository.ObtenerDescuento(agenciaId);
            // NO aplicar descuento: devolver precios originales
            // El descuento es responsabilidad de Movent (nivel lógico/negocio)

            // 2. Resolver IDs
            using var connection = _agenciaRepository.CrearConexion();
            await connection.OpenAsync();

            int paisOrigenId = await _paisRepository.ObtenerOCrearId(dto.OrigenPais, connection);
            int paisDestinoId = await _paisRepository.ObtenerOCrearId(dto.DestinoPais, connection);

            // CORRECCIÓN 3: ¡ESTAS LÍNEAS SON LAS QUE FALTABAN!
            // Primero obtienes el ID de la Ciudad...
            int ciudadOrigenId = await _ciudadRepository.ObtenerOCrearId(dto.Origen, paisOrigenId, connection);
            int ciudadDestinoId = await _ciudadRepository.ObtenerOCrearId(dto.Destino, paisDestinoId, connection);

            // ...y luego usas ese ID de ciudad para buscar el Aeropuerto
            int origenId = await _aeropuertoRepository.ObtenerIdPorCiudad(ciudadOrigenId, connection);
            int destinoId = await _aeropuertoRepository.ObtenerIdPorCiudad(ciudadDestinoId, connection);

            // 3. Registrar búsqueda como tipo Agencia (ID=2)
            await _vueloRepository.GuardarBusqueda(
                origenId: origenId,
                destinoId: destinoId,
                fechaSalida: dto.Fecha,
                cantidadPersonas: dto.CantidadPasajeros,
                usuarioId: null,
                tipoBusquedaId: 2
            );

            // 4. Buscar vuelos
            var interno = new BuscarVueloDTO
            {
                OrigenId = origenId,
                DestinoId = destinoId,
                Fecha = dto.Fecha,
                CantidadPasajeros = dto.CantidadPasajeros,
                ClaseId = dto.ClaseId,
                PrecioMinimo = dto.PrecioMinimo,
                PrecioMaximo = dto.PrecioMaximo
            };

            var resultado = await _vueloRepository.BuscarVuelos(
                interno.OrigenId, interno.DestinoId,
                interno.Fecha, interno.CantidadPasajeros, interno.ClaseId);

            var conEscala = await _vueloRepository.BuscarVuelosConEscalas(
                interno.OrigenId, interno.DestinoId,
                interno.Fecha, interno.CantidadPasajeros, interno.ClaseId);

            // NO aplicar descuento: devolver precios originales SIN modificar
            // El descuento es responsabilidad de Movent (nivel lógico/negocio)
            // Los vuelos se retornan con sus precios originales

            return new ResultadoBusquedaDTO
            {
                Directos = resultado,
                ConEscala = conEscala
            };
        }

    }
}
