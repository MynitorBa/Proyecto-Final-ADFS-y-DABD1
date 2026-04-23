using Xunit;
using Moq;
using FluentAssertions;
using Aerolinea.API.Services;
using Aerolinea.API.Repositories;
using Aerolinea.API.Models;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;

namespace Aerolinea.API.Tests.Services;

/// <summary>
/// Tests unitarios para AuthService — login con IUsuarioRepository mockeado.
/// </summary>
public class AuthServiceTest
{
    private readonly Mock<IUsuarioRepository> _mockRepo;
    private readonly AuthService _service;

    public AuthServiceTest()
    {
        _mockRepo = new Mock<IUsuarioRepository>();
        _service = new AuthService(_mockRepo.Object);
    }

    // ── Login exitoso ────────────────────────────────────────────────
    [Fact]
    public async Task Login_UsuarioExisteYContrasenaCorrecta_RetornaLoginResponseDto()
    {
        // Arrange
        var hash = PasswordHasher.Hash("pass123");
        var usuario = new Usuario { Id = 5, Nombre = "Ana", Correo = "ana@broom.com", RolID = 1, ContrasenaHash = hash };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("ana@broom.com")).ReturnsAsync(usuario);
        _mockRepo.Setup(r => r.ObtenerNombreRol(1)).ReturnsAsync("Cliente");

        var dto = new LoginRequestDto { CorreoOUsername = "ana@broom.com", Contrasena = "pass123" };

        // Act
        var result = await _service.Login(dto);

        // Assert
        result.Should().NotBeNull();
        result!.UsuarioId.Should().Be(5);
        result.Nombre.Should().Be("Ana");
        result.Correo.Should().Be("ana@broom.com");
        result.RolNombre.Should().Be("Cliente");
    }

    [Fact]
    public async Task Login_UsuarioExisteYContrasenaCorrecta_LlamaRepoUnaVez()
    {
        var hash = PasswordHasher.Hash("clave");
        var usuario = new Usuario { Id = 1, Nombre = "Luis", Correo = "luis@x.com", RolID = 2, ContrasenaHash = hash };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("luis@x.com")).ReturnsAsync(usuario);
        _mockRepo.Setup(r => r.ObtenerNombreRol(2)).ReturnsAsync("Admin");

        await _service.Login(new LoginRequestDto { CorreoOUsername = "luis@x.com", Contrasena = "clave" });

        _mockRepo.Verify(r => r.ObtenerPorCorreoOUsername("luis@x.com"), Times.Once);
    }

    [Fact]
    public async Task Login_UsuarioExisteYContrasenaCorrecta_ConsultaRolDelUsuario()
    {
        var hash = PasswordHasher.Hash("abc");
        var usuario = new Usuario { Id = 2, Nombre = "Pedro", Correo = "p@p.com", RolID = 3, ContrasenaHash = hash };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("p@p.com")).ReturnsAsync(usuario);
        _mockRepo.Setup(r => r.ObtenerNombreRol(3)).ReturnsAsync("Webservice");

        var result = await _service.Login(new LoginRequestDto { CorreoOUsername = "p@p.com", Contrasena = "abc" });

        _mockRepo.Verify(r => r.ObtenerNombreRol(3), Times.Once);
        result!.RolId.Should().Be(3);
    }

    // ── Contraseña incorrecta ─────────────────────────────────────────
    [Fact]
    public async Task Login_ContrasenaIncorrecta_RetornaNull()
    {
        var hash = PasswordHasher.Hash("correcta");
        var usuario = new Usuario { Id = 1, ContrasenaHash = hash, Correo = "x@x.com", Nombre = "X", RolID = 1 };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("x@x.com")).ReturnsAsync(usuario);

        var result = await _service.Login(new LoginRequestDto { CorreoOUsername = "x@x.com", Contrasena = "incorrecta" });

        result.Should().BeNull();
    }

    [Fact]
    public async Task Login_ContrasenaIncorrecta_NoConsultaRol()
    {
        var hash = PasswordHasher.Hash("verdadera");
        var usuario = new Usuario { Id = 1, ContrasenaHash = hash, Correo = "a@a.com", Nombre = "A", RolID = 1 };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("a@a.com")).ReturnsAsync(usuario);

        await _service.Login(new LoginRequestDto { CorreoOUsername = "a@a.com", Contrasena = "falsa" });

        _mockRepo.Verify(r => r.ObtenerNombreRol(It.IsAny<int>()), Times.Never);
    }

    // ── Usuario no encontrado ─────────────────────────────────────────
    [Fact]
    public async Task Login_UsuarioNoExiste_RetornaNull()
    {
        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("noexi@x.com")).ReturnsAsync((Usuario?)null);

        var result = await _service.Login(new LoginRequestDto { CorreoOUsername = "noexi@x.com", Contrasena = "algo" });

        result.Should().BeNull();
    }

    [Fact]
    public async Task Login_UsuarioNoExiste_NoConsultaRol()
    {
        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername(It.IsAny<string>())).ReturnsAsync((Usuario?)null);

        await _service.Login(new LoginRequestDto { CorreoOUsername = "fake@x.com", Contrasena = "x" });

        _mockRepo.Verify(r => r.ObtenerNombreRol(It.IsAny<int>()), Times.Never);
    }

    // ── Rol nulo — fallback ───────────────────────────────────────────
    [Fact]
    public async Task Login_RolNuloEnDB_UsaFallback()
    {
        var hash = PasswordHasher.Hash("pwd");
        var usuario = new Usuario { Id = 9, ContrasenaHash = hash, Correo = "z@z.com", Nombre = "Z", RolID = 99 };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("z@z.com")).ReturnsAsync(usuario);
        _mockRepo.Setup(r => r.ObtenerNombreRol(99)).ReturnsAsync((string?)null);

        var result = await _service.Login(new LoginRequestDto { CorreoOUsername = "z@z.com", Contrasena = "pwd" });

        result!.RolNombre.Should().Be("Usuario Registrado");
    }

    // ── Username (no correo) ──────────────────────────────────────────
    [Fact]
    public async Task Login_UsandoUsername_RetornaLoginResponseDto()
    {
        var hash = PasswordHasher.Hash("pass");
        var usuario = new Usuario { Id = 7, ContrasenaHash = hash, Correo = "m@m.com", Nombre = "M", Username = "muser", RolID = 1 };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("muser")).ReturnsAsync(usuario);
        _mockRepo.Setup(r => r.ObtenerNombreRol(1)).ReturnsAsync("Cliente");

        var result = await _service.Login(new LoginRequestDto { CorreoOUsername = "muser", Contrasena = "pass" });

        result.Should().NotBeNull();
        result!.UsuarioId.Should().Be(7);
    }

    // ── RolId retornado correctamente ─────────────────────────────────
    [Fact]
    public async Task Login_CredencialesCorrectas_RetornaRolIdCorrecto()
    {
        var hash = PasswordHasher.Hash("p");
        var usuario = new Usuario { Id = 3, ContrasenaHash = hash, Correo = "c@c.com", Nombre = "C", RolID = 2 };

        _mockRepo.Setup(r => r.ObtenerPorCorreoOUsername("c@c.com")).ReturnsAsync(usuario);
        _mockRepo.Setup(r => r.ObtenerNombreRol(2)).ReturnsAsync("Administrador");

        var result = await _service.Login(new LoginRequestDto { CorreoOUsername = "c@c.com", Contrasena = "p" });

        result!.RolId.Should().Be(2);
        result.RolNombre.Should().Be("Administrador");
    }
}
