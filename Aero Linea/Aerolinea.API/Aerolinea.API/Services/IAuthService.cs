using Aerolinea.API.DTOs;

namespace Aerolinea.API.Services
{
    public interface IAuthService
    {
        Task<LoginResponseDto?> Login(LoginRequestDto request, string? ip, string? userAgent);
        Task Logout(int? usuarioId, string? username, string? ip, string? userAgent);
    }
}