using Xunit;
using FluentAssertions;
using Aerolinea.API.Helpers;

namespace Aerolinea.API.Tests.Helpers;

public class TarjetaHelperTest
{
    // ── ValidarFormato ─────────────────────────────────────────────────
    [Fact]
    public void ValidarFormato_DatosValidos_NoLanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", fechaFutura, "123");
        act.Should().NotThrow();
    }

    [Fact]
    public void ValidarFormato_NumeroTarjetaVacio_LanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("", "Juan Perez", fechaFutura, "123");
        act.Should().Throw<Exception>().WithMessage("*número de tarjeta*");
    }

    [Fact]
    public void ValidarFormato_NumeroTarjetaMenos16Digitos_LanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("411111111111", "Juan Perez", fechaFutura, "123");
        act.Should().Throw<Exception>().WithMessage("*16 dígitos*");
    }

    [Fact]
    public void ValidarFormato_NombreVacio_LanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "", fechaFutura, "123");
        act.Should().Throw<Exception>().WithMessage("*titular*");
    }

    [Fact]
    public void ValidarFormato_NombreConNumeros_LanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan123", fechaFutura, "123");
        act.Should().Throw<Exception>().WithMessage("*titular*");
    }

    [Fact]
    public void ValidarFormato_FechaVacia_LanzaExcepcion()
    {
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", "", "123");
        act.Should().Throw<Exception>().WithMessage("*expiración*");
    }

    [Fact]
    public void ValidarFormato_FechaFormatoIncorrecto_LanzaExcepcion()
    {
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", "2026/12", "123");
        act.Should().Throw<Exception>().WithMessage("*MM/YY*");
    }

    [Fact]
    public void ValidarFormato_TarjetaVencida_LanzaExcepcion()
    {
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", "01/20", "123");
        act.Should().Throw<Exception>().WithMessage("*vencida*");
    }

    [Fact]
    public void ValidarFormato_CvvVacio_LanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", fechaFutura, "");
        act.Should().Throw<Exception>().WithMessage("*CVV*");
    }

    [Fact]
    public void ValidarFormato_CvvMenos3Digitos_LanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", fechaFutura, "12");
        act.Should().Throw<Exception>().WithMessage("*CVV*");
    }

    [Fact]
    public void ValidarFormato_CvvCuatroDigitosAmex_NoLanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111111111111111", "Juan Perez", fechaFutura, "1234");
        act.Should().NotThrow();
    }

    [Fact]
    public void ValidarFormato_NumeroConGuiones_NoLanzaExcepcion()
    {
        var fechaFutura = $"12/{DateTime.Now.AddYears(2).Year % 100:D2}";
        var act = () => TarjetaHelper.ValidarFormato("4111-1111-1111-1111", "Juan Perez", fechaFutura, "123");
        act.Should().NotThrow();
    }

    // ── DetectarTipo ───────────────────────────────────────────────────
    [Fact]
    public void DetectarTipo_NumeroVisa_RetornaVisa()
    {
        TarjetaHelper.DetectarTipo("4111111111111111").Should().Be("Visa");
    }

    [Fact]
    public void DetectarTipo_NumeroMastercard_RetornaMastercard()
    {
        TarjetaHelper.DetectarTipo("5111111111111111").Should().Be("Mastercard");
    }

    [Fact]
    public void DetectarTipo_NumeroAmex_RetornaAmericanExpress()
    {
        TarjetaHelper.DetectarTipo("371111111111111").Should().Be("American Express");
    }

    [Fact]
    public void DetectarTipo_NumeroDiscover_RetornaDiscover()
    {
        TarjetaHelper.DetectarTipo("6011111111111111").Should().Be("Discover");
    }

    [Fact]
    public void DetectarTipo_NumeroDesconocido_RetornaDesconocido()
    {
        TarjetaHelper.DetectarTipo("9111111111111111").Should().Be("Desconocido");
    }
}
