using Xunit;
using FluentAssertions;
using Aerolinea.API.Helpers;

namespace Aerolinea.API.Tests.Helpers;

public class TokenHelperTest
{
    [Fact]
    public void GenerarTokenHash_RetornaStringNoVacio()
    {
        var token = TokenHelper.GenerarTokenHash();
        token.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public void GenerarTokenHash_RetornaStringDe64Caracteres()
    {
        var token = TokenHelper.GenerarTokenHash();
        token.Should().HaveLength(64);
    }

    [Fact]
    public void GenerarTokenHash_RetornaSoloHexadecimal()
    {
        var token = TokenHelper.GenerarTokenHash();
        token.Should().MatchRegex("^[0-9a-f]+$");
    }

    [Fact]
    public void GenerarTokenHash_LlamadasConsecutivas_RetornaTokensDiferentes()
    {
        var token1 = TokenHelper.GenerarTokenHash();
        var token2 = TokenHelper.GenerarTokenHash();
        token1.Should().NotBe(token2);
    }

    [Fact]
    public void GenerarTokenHash_RetornaMayusculasOMinusculas_Solo()
    {
        var token = TokenHelper.GenerarTokenHash();
        token.Should().Be(token.ToLower());
    }
}
