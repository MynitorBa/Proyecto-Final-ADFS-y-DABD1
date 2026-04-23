using Xunit;
using FluentAssertions;
using Aerolinea.API.Helpers;

namespace Aerolinea.API.Tests.Helpers;

public class PasswordHasherTest
{
    [Fact]
    public void Hash_ConContrasenaValida_RetornaHashNoVacio()
    {
        var hash = PasswordHasher.Hash("miContrasena123");
        hash.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public void Hash_ConContrasenaValida_RetornaHashDiferenteAlOriginal()
    {
        var contrasena = "miContrasena123";
        var hash = PasswordHasher.Hash(contrasena);
        hash.Should().NotBe(contrasena);
    }

    [Fact]
    public void Hash_MismaContrasena_RetornaHashesDiferentes()
    {
        var hash1 = PasswordHasher.Hash("contrasena");
        var hash2 = PasswordHasher.Hash("contrasena");
        hash1.Should().NotBe(hash2);
    }

    [Fact]
    public void Hash_ContrasenaVacia_RetornaHash()
    {
        var hash = PasswordHasher.Hash("");
        hash.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public void Verify_ContrasenaCorrecta_RetornaTrue()
    {
        var contrasena = "miContrasena123";
        var hash = PasswordHasher.Hash(contrasena);
        PasswordHasher.Verify(contrasena, hash).Should().BeTrue();
    }

    [Fact]
    public void Verify_ContrasenaIncorrecta_RetornaFalse()
    {
        var hash = PasswordHasher.Hash("correcta");
        PasswordHasher.Verify("incorrecta", hash).Should().BeFalse();
    }

    [Fact]
    public void Verify_ContrasenaVaciaConHashVacio_RetornaFalse()
    {
        var hash = PasswordHasher.Hash("unaContrasena");
        PasswordHasher.Verify("", hash).Should().BeFalse();
    }

    [Fact]
    public void Verify_ContrasenaConEspacios_RetornaTrue()
    {
        var contrasena = "con espacios 123";
        var hash = PasswordHasher.Hash(contrasena);
        PasswordHasher.Verify(contrasena, hash).Should().BeTrue();
    }

    [Fact]
    public void Verify_ContrasenaConCaracteresEspeciales_RetornaTrue()
    {
        var contrasena = "P@$$w0rd!#%";
        var hash = PasswordHasher.Hash(contrasena);
        PasswordHasher.Verify(contrasena, hash).Should().BeTrue();
    }

    [Fact]
    public void Hash_RetornaStringConPrefijoBcrypt()
    {
        var hash = PasswordHasher.Hash("test123");
        hash.Should().StartWith("$2");
    }
}
