using Xunit;
using FluentAssertions;
using Microsoft.AspNetCore.Http;
using System.Security.Claims;
using Aerolinea.API.Helpers;

namespace Aerolinea.API.Tests.Helpers;

public class SessionHelperTest
{
    private HttpContext CrearContextoConClaims(params Claim[] claims)
    {
        var identity = new ClaimsIdentity(claims, "TestAuth");
        var principal = new ClaimsPrincipal(identity);
        var context = new DefaultHttpContext();
        context.User = principal;
        return context;
    }

    private HttpContext CrearContextoSinAutenticar()
    {
        var context = new DefaultHttpContext();
        context.User = new ClaimsPrincipal(new ClaimsIdentity());
        return context;
    }

    // ── Constantes ─────────────────────────────────────────────────────
    [Fact]
    public void ClaimUsuarioId_ValorCorrecto()
    {
        SessionHelper.ClaimUsuarioId.Should().Be("UsuarioId");
    }

    [Fact]
    public void ClaimRolId_ValorCorrecto()
    {
        SessionHelper.ClaimRolId.Should().Be("RolId");
    }

    // ── GetUsuarioId ───────────────────────────────────────────────────
    [Fact]
    public void GetUsuarioId_ClaimPresente_RetornaId()
    {
        var context = CrearContextoConClaims(new Claim(SessionHelper.ClaimUsuarioId, "42"));
        SessionHelper.GetUsuarioId(context).Should().Be(42);
    }

    [Fact]
    public void GetUsuarioId_SinClaim_RetornaNull()
    {
        var context = CrearContextoSinAutenticar();
        SessionHelper.GetUsuarioId(context).Should().BeNull();
    }

    [Fact]
    public void GetUsuarioId_ClaimNoNumerico_RetornaNull()
    {
        var context = CrearContextoConClaims(new Claim(SessionHelper.ClaimUsuarioId, "abc"));
        SessionHelper.GetUsuarioId(context).Should().BeNull();
    }

    // ── GetRolId ───────────────────────────────────────────────────────
    [Fact]
    public void GetRolId_ClaimPresente_RetornaId()
    {
        var context = CrearContextoConClaims(new Claim(SessionHelper.ClaimRolId, "2"));
        SessionHelper.GetRolId(context).Should().Be(2);
    }

    [Fact]
    public void GetRolId_SinClaim_RetornaNull()
    {
        var context = CrearContextoSinAutenticar();
        SessionHelper.GetRolId(context).Should().BeNull();
    }

    // ── GetNombre / GetCorreo ──────────────────────────────────────────
    [Fact]
    public void GetNombre_ClaimPresente_RetornaNombre()
    {
        var context = CrearContextoConClaims(new Claim(ClaimTypes.Name, "Ricardo"));
        SessionHelper.GetNombre(context).Should().Be("Ricardo");
    }

    [Fact]
    public void GetCorreo_ClaimPresente_RetornaCorreo()
    {
        var context = CrearContextoConClaims(new Claim(ClaimTypes.Email, "test@broom.com"));
        SessionHelper.GetCorreo(context).Should().Be("test@broom.com");
    }

    // ── EstaAutenticado ────────────────────────────────────────────────
    [Fact]
    public void EstaAutenticado_UsuarioAutenticado_RetornaTrue()
    {
        var context = CrearContextoConClaims(new Claim(SessionHelper.ClaimUsuarioId, "1"));
        SessionHelper.EstaAutenticado(context).Should().BeTrue();
    }

    [Fact]
    public void EstaAutenticado_SinAutenticar_RetornaFalse()
    {
        var context = CrearContextoSinAutenticar();
        SessionHelper.EstaAutenticado(context).Should().BeFalse();
    }

    // ── TieneRol ───────────────────────────────────────────────────────
    [Fact]
    public void TieneRol_RolCorrecto_RetornaTrue()
    {
        var context = CrearContextoConClaims(new Claim(ClaimTypes.Role, "Administrador"));
        SessionHelper.TieneRol(context, "Administrador").Should().BeTrue();
    }

    [Fact]
    public void TieneRol_RolIncorrecto_RetornaFalse()
    {
        var context = CrearContextoConClaims(new Claim(ClaimTypes.Role, "Cliente"));
        SessionHelper.TieneRol(context, "Administrador").Should().BeFalse();
    }
}
