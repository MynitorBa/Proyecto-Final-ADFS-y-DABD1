using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AdminVueloService
    {
        private readonly AdminVueloRepository _adminVueloRepository;

        public AdminVueloService(AdminVueloRepository adminVueloRepository)
        {
            _adminVueloRepository = adminVueloRepository;
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

            if (!TimeSpan.TryParse(dto.HoraLlegada, out _))
                throw new ArgumentException("El formato de hora de llegada es inválido");

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
    }
}