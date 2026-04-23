using Aerolinea.API.DTOs;

namespace Aerolinea.API.Services
{
    public interface IAuthService
    {
        Task<LoginResponseDto?> Login(LoginRequestDto request);
    }
}
