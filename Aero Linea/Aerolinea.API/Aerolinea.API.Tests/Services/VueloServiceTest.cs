using Xunit;
using Moq;
using FluentAssertions;
using Aerolinea.API.Services;
using Aerolinea.API.Repositories;
using Aerolinea.API.DTOs;

namespace Aerolinea.API.Tests.Services;

/// <summary>
/// Tests unitarios para VueloService — búsqueda general, búsqueda con filtros
/// de precio y delegación correcta al IVueloRepository mockeado.
/// </summary>
public class VueloServiceTest
{
    private readonly Mock<IVueloRepository> _mockRepo;
    private readonly VueloService _service;

    public VueloServiceTest()
    {
        _mockRepo = new Mock<IVueloRepository>();
        _service = new VueloService(_mockRepo.Object);
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private static VueloDetalleDTO MakeVuelo(int id, decimal? turista = null, decimal? ejecutiva = null) =>
        new()
        {
            Id = id,
            NumeroVuelo = $"BR{id:000}",
            Fecha = DateTime.Today,
            FechaLlegada = DateTime.Today,
            Estado = "Programado",
            AvionModelo = "737",
            AvionMarca = "Boeing",
            OrigenNombre = "Guatemala",
            OrigenCodigo = "GUA",
            OrigenCiudad = "Guatemala",
            OrigenPais = "Guatemala",
            DestinoNombre = "Miami",
            DestinoCodigo = "MIA",
            DestinoCiudad = "Miami",
            DestinoPais = "USA",
            PrecioTurista = turista,
            PrecioEjecutiva = ejecutiva
        };

    private static VueloConEscalaDTO MakeEscala(decimal? turistaTotal = null, decimal? ejecutivaTotal = null) =>
        new()
        {
            NumeroEscalas = 1,
            DuracionTotalMinutos = 300,
            TiempoEscalaMinutos = 60,
            PrecioTuristaTotal = turistaTotal,
            PrecioEjecutivaTotal = ejecutivaTotal,
            Tramos = new List<VueloDetalleDTO>()
        };

    private static BuscarVueloDTO MakeDto(int origen = 1, int destino = 2,
        decimal? min = null, decimal? max = null, int? clase = null) =>
        new()
        {
            OrigenId = origen,
            DestinoId = destino,
            Fecha = DateTime.Today,
            CantidadPasajeros = 1,
            PrecioMinimo = min,
            PrecioMaximo = max,
            ClaseId = clase
        };

    // ── BusquedaGeneral ───────────────────────────────────────────────────
    [Fact]
    public async Task BusquedaGeneral_DelegaAlRepositorio()
    {
        var lista = new List<VueloDetalleDTO> { MakeVuelo(1) };
        _mockRepo.Setup(r => r.BusquedaGeneral("BR001")).ReturnsAsync(lista);

        var result = await _service.BusquedaGeneral("BR001");

        result.Should().HaveCount(1);
        _mockRepo.Verify(r => r.BusquedaGeneral("BR001"), Times.Once);
    }

    [Fact]
    public async Task BusquedaGeneral_QueryVacia_RetornaListaVacia()
    {
        _mockRepo.Setup(r => r.BusquedaGeneral("")).ReturnsAsync(new List<VueloDetalleDTO>());

        var result = await _service.BusquedaGeneral("");

        result.Should().BeEmpty();
    }

    [Fact]
    public async Task BusquedaGeneral_RetornaResultadosDelRepo()
    {
        var lista = new List<VueloDetalleDTO> { MakeVuelo(1), MakeVuelo(2) };
        _mockRepo.Setup(r => r.BusquedaGeneral("GUA")).ReturnsAsync(lista);

        var result = await _service.BusquedaGeneral("GUA");

        result.Should().HaveCount(2);
        result[0].Id.Should().Be(1);
        result[1].Id.Should().Be(2);
    }

    // ── BuscarVuelos — guardado de búsqueda ──────────────────────────────
    [Fact]
    public async Task BuscarVuelos_GuardaBusquedaEnRepo()
    {
        var dto = MakeDto();
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        await _service.BuscarVuelos(dto, usuarioId: null);

        _mockRepo.Verify(r => r.GuardarBusqueda(dto.OrigenId, dto.DestinoId, dto.Fecha, dto.CantidadPasajeros, null), Times.Once);
    }

    [Fact]
    public async Task BuscarVuelos_PasaUsuarioIdAlGuardar()
    {
        var dto = MakeDto();
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        await _service.BuscarVuelos(dto, usuarioId: 42);

        _mockRepo.Verify(r => r.GuardarBusqueda(dto.OrigenId, dto.DestinoId, dto.Fecha, dto.CantidadPasajeros, 42), Times.Once);
    }

    // ── BuscarVuelos — sin filtros de precio ─────────────────────────────
    [Fact]
    public async Task BuscarVuelos_SinFiltros_RetornaTodosLosDirectos()
    {
        var directos = new List<VueloDetalleDTO> { MakeVuelo(1, 100m, 200m), MakeVuelo(2, 150m, 300m) };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(1, 2, It.IsAny<DateTime>(), 1, null)).ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(1, 2), null);

        result.Directos.Should().HaveCount(2);
    }

    [Fact]
    public async Task BuscarVuelos_SinFiltros_RetornaTodosLosConEscala()
    {
        var escalas = new List<VueloConEscalaDTO> { MakeEscala(200m, 400m) };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(escalas);

        var result = await _service.BuscarVuelos(MakeDto(), null);

        result.ConEscala.Should().HaveCount(1);
    }

    // ── Filtro de precio directos — ClaseId == 1 (Turista) ───────────────
    [Fact]
    public async Task BuscarVuelos_FiltroPrecioMaxTurista_ExcluyeVueloCaro()
    {
        var directos = new List<VueloDetalleDTO>
        {
            MakeVuelo(1, turista: 100m),
            MakeVuelo(2, turista: 300m)
        };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), 1))
                 .ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(clase: 1, max: 200m), null);

        result.Directos.Should().HaveCount(1);
        result.Directos[0].Id.Should().Be(1);
    }

    [Fact]
    public async Task BuscarVuelos_FiltroPrecioMinTurista_ExcluyeVueloBarato()
    {
        var directos = new List<VueloDetalleDTO>
        {
            MakeVuelo(1, turista: 50m),
            MakeVuelo(2, turista: 200m)
        };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), 1))
                 .ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(clase: 1, min: 100m), null);

        result.Directos.Should().HaveCount(1);
        result.Directos[0].Id.Should().Be(2);
    }

    // ── Filtro de precio directos — ClaseId == 2 (Ejecutiva) ─────────────
    [Fact]
    public async Task BuscarVuelos_FiltroPrecioEjecutiva_FiltroCorrectamente()
    {
        var directos = new List<VueloDetalleDTO>
        {
            MakeVuelo(1, ejecutiva: 400m),
            MakeVuelo(2, ejecutiva: 600m)
        };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), 2))
                 .ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(clase: 2, max: 500m), null);

        result.Directos.Should().HaveCount(1);
        result.Directos[0].Id.Should().Be(1);
    }

    // ── Filtro de precio directos — sin clase (usa el menor precio) ───────
    [Fact]
    public async Task BuscarVuelos_SinClase_UsaMenorPrecio_PasaFiltro()
    {
        // Turista=100, Ejecutiva=500 → menor=100, entra en max=200
        var directos = new List<VueloDetalleDTO> { MakeVuelo(1, turista: 100m, ejecutiva: 500m) };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), null))
                 .ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(max: 200m), null);

        result.Directos.Should().HaveCount(1);
    }

    [Fact]
    public async Task BuscarVuelos_SinClase_UsaMenorPrecio_FallaFiltro()
    {
        // Turista=300, Ejecutiva=500 → menor=300, no entra en max=200
        var directos = new List<VueloDetalleDTO> { MakeVuelo(1, turista: 300m, ejecutiva: 500m) };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), null))
                 .ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(max: 200m), null);

        result.Directos.Should().BeEmpty();
    }

    [Fact]
    public async Task BuscarVuelos_PrecioNulo_ExcluyeVuelo()
    {
        // Sin precio asignado → excluido aunque no haya filtro de mínimo
        var directos = new List<VueloDetalleDTO> { MakeVuelo(1, turista: null, ejecutiva: null) };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), null))
                 .ReturnsAsync(directos);
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        var result = await _service.BuscarVuelos(MakeDto(max: 999m), null);

        result.Directos.Should().BeEmpty();
    }

    // ── Filtro de precio escalas ──────────────────────────────────────────
    [Fact]
    public async Task BuscarVuelos_FiltroPrecioEscalaTurista_ExcluyeEscalaCara()
    {
        var escalas = new List<VueloConEscalaDTO>
        {
            MakeEscala(turistaTotal: 150m),
            MakeEscala(turistaTotal: 450m)
        };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), 1))
                 .ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(escalas);

        var result = await _service.BuscarVuelos(MakeDto(clase: 1, max: 300m), null);

        result.ConEscala.Should().HaveCount(1);
        result.ConEscala[0].PrecioTuristaTotal.Should().Be(150m);
    }

    [Fact]
    public async Task BuscarVuelos_FiltroPrecioEscalaEjecutiva_FiltroCorrectamente()
    {
        var escalas = new List<VueloConEscalaDTO>
        {
            MakeEscala(ejecutivaTotal: 300m),
            MakeEscala(ejecutivaTotal: 800m)
        };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), 2))
                 .ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(escalas);

        var result = await _service.BuscarVuelos(MakeDto(clase: 2, max: 500m), null);

        result.ConEscala.Should().HaveCount(1);
        result.ConEscala[0].PrecioEjecutivaTotal.Should().Be(300m);
    }

    [Fact]
    public async Task BuscarVuelos_FiltroPrecioEscalaSinClase_UsaMenorPrecio()
    {
        // Turista=100, Ejecutiva=200 → menor=100, entra en max=150
        var escalas = new List<VueloConEscalaDTO> { MakeEscala(turistaTotal: 100m, ejecutivaTotal: 200m) };
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), null))
                 .ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(escalas);

        var result = await _service.BuscarVuelos(MakeDto(max: 150m), null);

        result.ConEscala.Should().HaveCount(1);
    }

    // ── Resultado completo ────────────────────────────────────────────────
    [Fact]
    public async Task BuscarVuelos_RetornaResultadoBusquedaDTOConAmbasListas()
    {
        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .ReturnsAsync(new List<VueloDetalleDTO> { MakeVuelo(1) });
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO> { MakeEscala(200m) });

        var result = await _service.BuscarVuelos(MakeDto(), null);

        result.Should().NotBeNull();
        result.Directos.Should().HaveCount(1);
        result.ConEscala.Should().HaveCount(1);
    }

    [Fact]
    public async Task BuscarVuelos_LlamaRepoBuscarVuelosConParametrosCorrectos()
    {
        var fecha = new DateTime(2026, 6, 15);
        var dto = new BuscarVueloDTO { OrigenId = 5, DestinoId = 10, Fecha = fecha, CantidadPasajeros = 2, ClaseId = 1 };

        _mockRepo.Setup(r => r.GuardarBusqueda(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>()))
                 .Returns(Task.CompletedTask);
        _mockRepo.Setup(r => r.BuscarVuelos(5, 10, fecha, 2, 1)).ReturnsAsync(new List<VueloDetalleDTO>());
        _mockRepo.Setup(r => r.BuscarVuelosConEscalas(It.IsAny<int>(), It.IsAny<int>(), It.IsAny<DateTime>(), It.IsAny<int>(), It.IsAny<int?>(), It.IsAny<int>()))
                 .ReturnsAsync(new List<VueloConEscalaDTO>());

        await _service.BuscarVuelos(dto, null);

        _mockRepo.Verify(r => r.BuscarVuelos(5, 10, fecha, 2, 1), Times.Once);
    }
}
