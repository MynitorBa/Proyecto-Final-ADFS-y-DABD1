<script>
  // @ts-nocheck
  import '../styles/vuelos.css';
  import DetalleVueloModal from './DetalleVuelo.svelte';
  import { onMount } from 'svelte';

  export let navigateTo;
  export let searchParams = null;

  let searchData = {
    origenNombre: '', destinoNombre: '',
    origenCodigo: '', destinoCodigo: '',
    fechaIda: '', fechaVuelta: '',
    pasajeros: 1, tripType: 'roundtrip',
    origenId: null, destinoId: null
  };

  let currentView = 'outbound';
  let selectedOutbound = { flight: null, clase: null };
  let selectedReturn   = { flight: null, clase: null };
  let showDetailModal  = false;
  let detailFlight     = null;
  let vuelosIda        = [];
  let vuelosVuelta     = [];
  let loadingIda       = true;
  let loadingVuelta    = false;
  let errorIda         = '';
  let errorVuelta      = '';
  let clases = [
    { id: 1, tipoDeClase: 'Turista' },
    { id: 2, tipoDeClase: 'Ejecutiva' }
  ];
  let precioMin = '';
  let precioMax = '';
  let claseSeleccionada = '';

  onMount(async () => {
    if (searchParams?.busquedaId) {
      await cargarBusquedaDesdeBackend(searchParams.busquedaId);
    } else {
      errorIda = 'No se encontró información de búsqueda. Por favor, realiza una nueva búsqueda desde el inicio.';
      loadingIda = false;
    }
  });

  async function cargarBusquedaDesdeBackend(busquedaId) {
    try {
      const response = await fetch(`https://localhost:7107/api/busquedas/${busquedaId}`);
      if (!response.ok) throw new Error('Búsqueda no encontrada o expirada');
      const busqueda = await response.json();
      searchData = {
        origenNombre: busqueda.origenNombre || '', destinoNombre: busqueda.destinoNombre || '',
        origenCodigo: busqueda.origenCodigo || '', destinoCodigo: busqueda.destinoCodigo || '',
        fechaIda: busqueda.fechaIda || '', fechaVuelta: busqueda.fechaVuelta || '',
        pasajeros: busqueda.pasajeros || 1, tripType: busqueda.tripType || 'roundtrip',
        origenId: busqueda.origenId || null, destinoId: busqueda.destinoId || null
      };
      await buscarVuelosIda();
    } catch (error) {
      errorIda = 'La búsqueda ha expirado o no existe. Por favor, realiza una nueva búsqueda desde el inicio.';
      loadingIda = false;
    }
  }

  async function buscarVuelosIda() {
    if (!searchData.origenId || !searchData.destinoId || !searchData.fechaIda) {
      errorIda = 'Parámetros de búsqueda incompletos. Vuelve al inicio y busca un vuelo.';
      loadingIda = false;
      return;
    }
    loadingIda = true; errorIda = '';
    try {
      const body = {
        origenId: searchData.origenId,
        destinoId: searchData.destinoId,
        fecha: searchData.fechaIda,
        cantidadPasajeros: searchData.pasajeros
      };

      // Agregar filtros opcionales
      if (precioMin !== '') body.precioMinimo = parseFloat(precioMin);
      if (precioMax !== '') body.precioMaximo = parseFloat(precioMax);
      if (claseSeleccionada !== '') body.claseId = parseInt(claseSeleccionada);

      const res = await fetch('https://localhost:7107/api/vuelos/buscar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      vuelosIda = await res.json();
    } catch (err) {
      errorIda = 'No se pudieron cargar los vuelos de ida.';
    } finally { loadingIda = false; }
  }

  async function buscarVuelosVuelta() {
    if (searchData.tripType !== 'roundtrip' || !searchData.fechaVuelta) return;
    loadingVuelta = true; errorVuelta = '';
    try {
      const body = {
        origenId: searchData.destinoId,
        destinoId: searchData.origenId,
        fecha: searchData.fechaVuelta,
        cantidadPasajeros: searchData.pasajeros
      };

      // Agregar filtros opcionales
      if (precioMin !== '') body.precioMinimo = parseFloat(precioMin);
      if (precioMax !== '') body.precioMaximo = parseFloat(precioMax);
      if (claseSeleccionada !== '') body.claseId = parseInt(claseSeleccionada);

      const res = await fetch('https://localhost:7107/api/vuelos/buscar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error('Error buscando vuelos de vuelta');
      vuelosVuelta = await res.json();
    } catch (err) {
      errorVuelta = 'No se pudieron cargar los vuelos de vuelta.';
    } finally { loadingVuelta = false; }
  }

  async function aplicarFiltros() {
    if (currentView === 'outbound') {
      await buscarVuelosIda();
    } else {
      await buscarVuelosVuelta();
    }
  }

  $: currentFlights  = currentView === 'outbound' ? vuelosIda : vuelosVuelta;
  $: loading         = currentView === 'outbound' ? loadingIda    : loadingVuelta;
  $: errorActual     = currentView === 'outbound' ? errorIda      : errorVuelta;
  $: selectedFlight  = currentView === 'outbound' ? selectedOutbound : selectedReturn;
  $: canProceed      = selectedFlight.flight !== null && selectedFlight.clase !== null;

  function limpiarFiltros() {
    precioMin = '';
    precioMax = '';
    claseSeleccionada = '';
    aplicarFiltros();
  }

  function selectFlight(vuelo, clase) {
    if (currentView === 'outbound') selectedOutbound = { flight: vuelo, clase };
    else selectedReturn = { flight: vuelo, clase };
  }

  function isSelected(vuelo, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.flight?.id === vuelo.id && s.clase?.id === clase.id;
  }

  function viewDetails(vuelo) { detailFlight = vuelo; showDetailModal = true; }
  function closeModal()       { showDetailModal = false; detailFlight = null; }

  async function nextStep() {
    if (currentView === 'outbound' && selectedOutbound.flight) {
      if (searchData.tripType === 'roundtrip') {
        currentView = 'return';
        await buscarVuelosVuelta();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        // Solo ida - intentar crear reserva si está logueado
        await crearReserva();
      }
    } else if (currentView === 'return' && selectedReturn.flight) {
      // Ida y vuelta - intentar crear reserva si está logueado
      await crearReserva();
    }
  }

  async function crearReserva() {
    const usuarioId = sessionStorage.getItem('usuarioId');
    
    // Si no está logueado, ir directo a datos-pasajeros
    if (!usuarioId) {
      if (searchData.tripType === 'roundtrip') {
        navigateTo('datos-pasajeros', { outbound: selectedOutbound, return: selectedReturn, searchData });
      } else {
        navigateTo('datos-pasajeros', { outbound: selectedOutbound, searchData });
      }
      return;
    }

    // Construir el array de vuelos
    const vuelos = [];
    
    // Agregar vuelo de ida
    vuelos.push({
      vueloId: selectedOutbound.flight.id,
      claseId: selectedOutbound.clase.id,
      cantidadPasajeros: searchData.pasajeros
    });
    
    // Agregar vuelo de vuelta si existe
    if (selectedReturn.flight) {
      vuelos.push({
        vueloId: selectedReturn.flight.id,
        claseId: selectedReturn.clase.id,
        cantidadPasajeros: searchData.pasajeros
      });
    }

    // Hacer POST a /api/reservaciones
    try {
      const response = await fetch('https://localhost:7107/api/reservaciones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          usuarioId: parseInt(usuarioId),
          vuelos: vuelos
        })
      });

      if (response.ok) {
        const reservaData = await response.json();
        // Si la reserva fue exitosa, navegar a datos-pasajeros con la info de la reserva
        if (searchData.tripType === 'roundtrip') {
          navigateTo('datos-pasajeros', { 
            outbound: selectedOutbound, 
            return: selectedReturn, 
            searchData,
            reserva: reservaData 
          });
        } else {
          navigateTo('datos-pasajeros', { 
            outbound: selectedOutbound, 
            searchData,
            reserva: reservaData 
          });
        }
      }
      // Si falla, no hacer nada (sin alert)
    } catch (error) {
      // Si hay error, no hacer nada (sin alert)
      console.error('Error al crear reserva:', error);
    }
  }

  function goBack() {
    if (currentView === 'return') { currentView = 'outbound'; window.scrollTo({ top: 0, behavior: 'smooth' }); }
    else navigateTo('home');
  }

  function formatDuracion(min) {
    if (!min) return '';
    return `${Math.floor(min/60)}h ${min%60}m`;
  }

  function formatHora(h) { return h ? h.substring(0,5) : ''; }

  function getPrecioClase(vuelo, claseId) {
    if (claseId === 1) return vuelo.precioTurista;
    if (claseId === 2) return vuelo.precioEjecutiva;
    return null;
  }

  function getBoletosDisponiblesClase(vuelo, claseId) {
    if (claseId === 1) return vuelo.boletosDisponiblesTurista || 0;
    if (claseId === 2) return vuelo.boletosDisponiblesEjecutiva || 0;
    return 0;
  }

  function formatPrecio(precio) {
    if (!precio) return 'No disponible';
    return `$ ${precio.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }
</script>

{#if showDetailModal && detailFlight}
  <DetalleVueloModal flight={detailFlight} onClose={closeModal} />
{/if}

<div class="vuelos-page">
  <div class="vuelos-page__container">

    <div class="vuelos-page__header">
      <button class="vuelos-page__back" on:click={goBack}>Volver</button>
      <div class="search-summary">
        <div class="search-summary__route">
          <span class="search-summary__origin">{searchData.origenNombre || 'Origen'}</span>
          <span class="search-summary__arrow">→</span>
          <span class="search-summary__destination">{searchData.destinoNombre || 'Destino'}</span>
        </div>
        <div class="search-summary__details">
          <div class="search-summary__item">
            <span class="search-summary__label">Salida</span>
            <span class="search-summary__value">{searchData.fechaIda || '—'}</span>
          </div>
          {#if searchData.tripType === 'roundtrip'}
            <div class="search-summary__item">
              <span class="search-summary__label">Regreso</span>
              <span class="search-summary__value">{searchData.fechaVuelta || '—'}</span>
            </div>
          {/if}
          <div class="search-summary__item">
            <span class="search-summary__label">Pasajeros</span>
            <span class="search-summary__value">{searchData.pasajeros}</span>
          </div>
        </div>
      </div>
      <div class="step-indicator">
        <div class="step-indicator__item"
          class:step-indicator__item--active={currentView === 'outbound'}
          class:step-indicator__item--completed={selectedOutbound.flight}>
          <span class="step-indicator__number">1</span>
          <span class="step-indicator__label">Vuelo de Ida</span>
        </div>
        {#if searchData.tripType === 'roundtrip'}
          <div class="step-indicator__line"></div>
          <div class="step-indicator__item"
            class:step-indicator__item--active={currentView === 'return'}
            class:step-indicator__item--completed={selectedReturn.flight}>
            <span class="step-indicator__number">2</span>
            <span class="step-indicator__label">Vuelo de Vuelta</span>
          </div>
        {/if}
      </div>
    </div>

    <div class="vuelos-page__content">
      <aside class="vuelos-page__filters">
        <div class="filters-panel">
          <div class="filters-panel__header">
            <h3 class="filters-panel__title">Filtros</h3>
            <button class="filters-panel__clear" on:click={limpiarFiltros}>Limpiar</button>
          </div>
          <div class="filter-group">
            <label class="filter-group__label" for="precioMin">Rango de Precio (USD)</label>
            <div class="filter-group__price-range">
              <input id="precioMin" type="number" class="filter-group__input" placeholder="Min" bind:value={precioMin} />
              <span>-</span>
              <input type="number" class="filter-group__input" placeholder="Max" bind:value={precioMax} />
            </div>
          </div>
          <div class="filter-group">
            <label class="filter-group__label" for="filtroClase">Clase</label>
            <div class="filter-group__select">
              <select id="filtroClase" class="filter-group__select-element" bind:value={claseSeleccionada}>
                <option value="">Todas</option>
                {#each clases as c}
                  <option value={c.id}>{c.tipoDeClase}</option>
                {/each}
              </select>
            </div>
          </div>
          <button class="filters-panel__apply" on:click={aplicarFiltros}>Aplicar Filtros</button>
        </div>
      </aside>

      <div class="vuelos-page__main">
        <div class="flights-header">
          <h2 class="flights-header__title">{currentView === 'outbound' ? 'Vuelos de Ida' : 'Vuelos de Regreso'}</h2>
          <p class="flights-header__subtitle">{loading ? 'Buscando vuelos...' : `${currentFlights.length} vuelos disponibles`}</p>
        </div>

        {#if loading}
          <div class="vuelos-estado">Buscando vuelos...</div>
        {:else if errorActual}
          <div class="vuelos-estado vuelos-estado--info">{errorActual}</div>
        {:else if currentFlights.length === 0}
          <div class="vuelos-estado">No hay vuelos disponibles para esta ruta y fecha.</div>
        {:else}
          <div class="flights-list">
            {#each currentFlights as vuelo}
              {@const estaSeleccionado = (currentView === 'outbound' ? selectedOutbound : selectedReturn).flight?.id === vuelo.id}

              <div class="flight-card" class:flight-card--selected={estaSeleccionado}>

                <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <circle cx="190" cy="15"  r="90" stroke="#c9a96e" stroke-width="0.6"/>
                  <circle cx="190" cy="15"  r="62" stroke="#c9a96e" stroke-width="0.6"/>
                  <circle cx="190" cy="15"  r="36" stroke="#c9a96e" stroke-width="0.6"/>
                  <path d="M95 72 L148 48 L160 53 L142 66 L156 62 L160 68 L128 79 Z" fill="#c9a96e" opacity="0.18"/>
                  <line x1="60" y1="125" x2="200" y2="125" stroke="#c9a96e" stroke-width="0.5" stroke-dasharray="4 7"/>
                  <line x1="80" y1="135" x2="200" y2="135" stroke="#c9a96e" stroke-width="0.4" stroke-dasharray="3 8"/>
                </svg>

                <div class="flight-card__content">
                  <div class="flight-card__header">
                    <div class="flight-card__code-info">
                      <span class="flight-card__code">{vuelo.numeroVuelo || 'N/A'}</span>
                      <span class="flight-card__airline">{vuelo.avionMarca || ''} {vuelo.avionModelo || ''}</span>
                    </div>
                    <div class="flight-card__rating">
                      <span class="flight-card__rating-value">{vuelo.boletosDisponibles || 0}</span>
                      <span class="flight-card__rating-label">asientos</span>
                    </div>
                  </div>

                  <div class="flight-card__schedule">
                    <div class="schedule-point">
                      <span class="schedule-point__time">{formatHora(vuelo.horaSalida)}</span>
                      <span class="schedule-point__code">{vuelo.origenCodigo || ''}</span>
                    </div>
                    <div class="schedule-duration">
                      <div class="schedule-duration__track">
                        <div class="schedule-duration__dot"></div>
                        <div class="schedule-duration__line"></div>
                        <span class="schedule-duration__plane">✈</span>
                        <div class="schedule-duration__line"></div>
                        <div class="schedule-duration__dot"></div>
                      </div>
                      <span class="schedule-duration__time">{formatDuracion(vuelo.duracionMinutos)}</span>
                      <span class="schedule-duration__type">Directo</span>
                    </div>
                    <div class="schedule-point">
                      <span class="schedule-point__time">{formatHora(vuelo.horaLlegada)}</span>
                      <span class="schedule-point__code">{vuelo.destinoCodigo || ''}</span>
                    </div>
                  </div>

                  <div class="flight-card__class-selection">
                    {#each clases as clase}
                      {@const precio = getPrecioClase(vuelo, clase.id)}
                      {@const boletosDisp = getBoletosDisponiblesClase(vuelo, clase.id)}
                      {@const disponible = precio !== null && precio > 0 && boletosDisp >= searchData.pasajeros}

                      <button
                        class="class-option"
                        class:class-option--selected={isSelected(vuelo, clase)}
                        class:class-option--disabled={!disponible}
                        disabled={!disponible}
                        on:click={() => disponible && selectFlight(vuelo, clase)}
                      >
                        <span class="class-option__name">{clase.tipoDeClase}</span>
                        {#if disponible}
                          <span class="class-option__price">{formatPrecio(precio)}</span>
                          <span class="class-option__label">
                            {estaSeleccionado && isSelected(vuelo, clase) ? 'Seleccionado ✓' : 'por persona'}
                          </span>
                        {:else}
                          <span class="class-option__label class-option__label--unavailable">No disponible</span>
                        {/if}
                      </button>
                    {/each}
                  </div>

                  <button class="flight-card__details-btn" on:click={() => viewDetails(vuelo)}>
                    Ver Detalles
                  </button>
                </div>
              </div>
            {/each}
          </div>
        {/if}
      </div>
    </div>

    {#if canProceed}
      <div class="vuelos-page__next-step">
        <button class="next-step-btn" on:click={nextStep}>
          {currentView === 'outbound' && searchData.tripType === 'roundtrip' ? 'Seleccionar Vuelo de Vuelta' : 'Siguiente Paso'}
        </button>
      </div>
    {/if}
  </div>
</div>

<style>
  .filters-panel__apply {
    width: 100%;
    padding: 0.75rem;
    background-color: #c9a96e;
    color: white;
    border: none;
    border-radius: 0.5rem;
    font-weight: 600;
    cursor: pointer;
    margin-top: 1rem;
    transition: background-color 0.2s;
  }

  .filters-panel__apply:hover {
    background-color: #b89860;
  }

  .class-option__price {
    font-size: 1.25rem;
    font-weight: 700;
    color: #c9a96e;
    margin: 0.25rem 0;
  }

  .class-option--disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .class-option__label--unavailable {
    color: #dc2626;
    font-weight: 600;
  }
</style>