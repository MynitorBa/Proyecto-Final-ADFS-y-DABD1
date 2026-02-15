<script>
// @ts-nocheck
  import '../styles/vuelos.css';
  import DetalleVueloModal from './DetalleVuelo.svelte';
  import { onMount } from 'svelte';

  export let navigateTo;
  export let searchParams = null;

  // ⚡ FIX: Datos de búsqueda - inicialización con defaults
  let searchData = {
    origenNombre: '',
    destinoNombre: '',
    origenCodigo: '',
    destinoCodigo: '',
    fechaIda: '',
    fechaVuelta: '',
    pasajeros: 1,
    tripType: 'roundtrip',
    origenId: null,
    destinoId: null
  };

  // ⚡ FIX: Actualizar cuando lleguen searchParams
  $: if (searchParams) {
    searchData = { ...searchData, ...searchParams };
    console.log('✅ Vuelos.svelte recibió searchParams:', searchData);
  }

  let currentView = 'outbound';
  let selectedOutbound = { flight: null, clase: null };
  let selectedReturn = { flight: null, clase: null };

  let showDetailModal = false;
  let detailFlight = null;

  // Vuelos del backend
  let vuelosIda = [];
  let vuelosVuelta = [];
  let loadingIda = true;
  let loadingVuelta = false;
  let errorIda = '';
  let errorVuelta = '';

  // Clases disponibles del backend
  let clases = [];
  let loadingClases = true;

  // Filtros
  let precioMin = '';
  let precioMax = '';

  onMount(async () => {
    console.log('🔄 Vuelos.svelte montado. searchParams:', searchParams);
    await Promise.all([cargarClases(), buscarVuelosIda()]);
  });

  async function cargarClases() {
    try {
      const res = await fetch('http://localhost:5190/api/clases');
      if (res.ok) {
        clases = await res.json();
        console.log('✅ Clases cargadas:', clases);
      }
    } catch (err) {
      console.error('❌ Error cargando clases:', err);
      // Si no existe el endpoint, usamos clases por defecto
      clases = [
        { id: 1, tipoDeClase: 'Turista' },
        { id: 2, tipoDeClase: 'Ejecutiva' }
      ];
    } finally {
      loadingClases = false;
    }
  }

  async function buscarVuelosIda() {
    console.log('🔍 Intentando buscar vuelos de ida...');
    console.log('   origenId:', searchData.origenId);
    console.log('   destinoId:', searchData.destinoId);
    console.log('   fechaIda:', searchData.fechaIda);
    console.log('   pasajeros:', searchData.pasajeros);

    if (!searchData.origenId || !searchData.destinoId || !searchData.fechaIda) {
      errorIda = 'Parámetros de búsqueda incompletos. Vuelve al inicio y busca un vuelo.';
      loadingIda = false;
      console.error('❌ Parámetros incompletos:', searchData);
      return;
    }
    
    loadingIda = true;
    errorIda = '';
    
    try {
      const requestBody = {
        origenId: searchData.origenId,
        destinoId: searchData.destinoId,
        fecha: searchData.fechaIda,
        cantidadPasajeros: searchData.pasajeros
      };
      
      console.log('📤 Enviando búsqueda de vuelos:', requestBody);
      
      const res = await fetch('http://localhost:5190/api/vuelos/buscar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });
      
      if (!res.ok) {
        const errorText = await res.text();
        console.error('❌ Error en respuesta:', res.status, errorText);
        throw new Error(`Error ${res.status}`);
      }
      
      vuelosIda = await res.json();
      console.log('✅ Vuelos de ida recibidos:', vuelosIda.length, 'vuelos');
      console.log('   Primer vuelo:', vuelosIda[0]);
      
    } catch (err) {
      console.error('❌ Error buscando vuelos:', err);
      errorIda = 'No se pudieron cargar los vuelos de ida.';
    } finally {
      loadingIda = false;
    }
  }

  async function buscarVuelosVuelta() {
    if (searchData.tripType !== 'roundtrip' || !searchData.fechaVuelta) {
      console.log('⏭️ Saltando búsqueda de vuelta (no es ida y vuelta)');
      return;
    }
    
    loadingVuelta = true;
    errorVuelta = '';
    
    try {
      const requestBody = {
        origenId: searchData.destinoId,
        destinoId: searchData.origenId,
        fecha: searchData.fechaVuelta,
        cantidadPasajeros: searchData.pasajeros
      };
      
      console.log('📤 Enviando búsqueda de vuelos de vuelta:', requestBody);
      
      const res = await fetch('http://localhost:5190/api/vuelos/buscar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });
      
      if (!res.ok) throw new Error();
      
      vuelosVuelta = await res.json();
      console.log('✅ Vuelos de vuelta recibidos:', vuelosVuelta.length);
      
    } catch (err) {
      console.error('❌ Error buscando vuelos de vuelta:', err);
      errorVuelta = 'No se pudieron cargar los vuelos de vuelta.';
    } finally {
      loadingVuelta = false;
    }
  }

  // Filtrado reactivo por precio
  $: vuelosFiltradosIda = aplicarFiltros(vuelosIda);
  $: vuelosFiltradosVuelta = aplicarFiltros(vuelosVuelta);
  $: currentFlights = currentView === 'outbound' ? vuelosFiltradosIda : vuelosFiltradosVuelta;
  $: loading = currentView === 'outbound' ? loadingIda : loadingVuelta;
  $: errorActual = currentView === 'outbound' ? errorIda : errorVuelta;
  $: selectedFlight = currentView === 'outbound' ? selectedOutbound : selectedReturn;
  $: canProceed = selectedFlight.flight !== null && selectedFlight.clase !== null;

  function aplicarFiltros(lista) {
    return lista.filter(v => {
      if (precioMin !== '' && (v.precio ?? 0) < parseFloat(precioMin)) return false;
      if (precioMax !== '' && (v.precio ?? 0) > parseFloat(precioMax)) return false;
      return true;
    });
  }

  function limpiarFiltros() {
    precioMin = '';
    precioMax = '';
  }

  function selectFlight(vuelo, clase) {
    console.log('✅ Vuelo seleccionado:', vuelo.numeroVuelo, 'Clase:', clase.tipoDeClase);
    if (currentView === 'outbound') {
      selectedOutbound = { flight: vuelo, clase };
    } else {
      selectedReturn = { flight: vuelo, clase };
    }
  }

  function isSelected(vuelo, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.flight?.id === vuelo.id && s.clase?.id === clase.id;
  }

  function viewDetails(vuelo) {
    detailFlight = vuelo;
    showDetailModal = true;
  }

  function closeModal() {
    showDetailModal = false;
    detailFlight = null;
  }

  async function nextStep() {
    if (currentView === 'outbound' && selectedOutbound.flight) {
      if (searchData.tripType === 'roundtrip') {
        currentView = 'return';
        await buscarVuelosVuelta();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        console.log('➡️ Navegando a datos-pasajeros con:', { outbound: selectedOutbound, searchData });
        navigateTo('datos-pasajeros', { outbound: selectedOutbound, searchData });
      }
    } else if (currentView === 'return' && selectedReturn.flight) {
      console.log('➡️ Navegando a datos-pasajeros con:', { outbound: selectedOutbound, return: selectedReturn, searchData });
      navigateTo('datos-pasajeros', { outbound: selectedOutbound, return: selectedReturn, searchData });
    }
  }

  function goBack() {
    if (currentView === 'return') {
      currentView = 'outbound';
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      navigateTo('home');
    }
  }

  function formatDuracion(min) {
    if (!min) return '';
    return `${Math.floor(min / 60)}h ${min % 60}m`;
  }

  function formatHora(h) {
    return h ? h.substring(0, 5) : '';
  }
</script>

{#if showDetailModal && detailFlight}
  <DetalleVueloModal flight={detailFlight} onClose={closeModal} />
{/if}

<div class="vuelos-page">
  <div class="vuelos-page__container">
    
    <!-- Header con resumen de búsqueda -->
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

      <!-- Indicador de paso actual -->
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

    <!-- Contenido principal -->
    <div class="vuelos-page__content">
      
      <!-- Filtros laterales -->
      <aside class="vuelos-page__filters">
        <div class="filters-panel">
          <div class="filters-panel__header">
            <h3 class="filters-panel__title">Filtros</h3>
            <button class="filters-panel__clear" on:click={limpiarFiltros}>Limpiar</button>
          </div>

          <div class="filter-group">
            <label class="filter-group__label" for="precioMin">Rango de Precio</label>
            <div class="filter-group__price-range">
              <input id="precioMin" type="number" class="filter-group__input" placeholder="Min" bind:value={precioMin} />
              <span>-</span>
              <input type="number" class="filter-group__input" placeholder="Max" bind:value={precioMax} />
            </div>
          </div>

          <div class="filter-group">
            <label class="filter-group__label" for="filtroClase">Clase</label>
            <select id="filtroClase" class="filter-group__select">
              <option value="">Todas</option>
              {#each clases as c}
                <option value={c.id}>{c.tipoDeClase}</option>
              {/each}
            </select>
          </div>
        </div>
      </aside>

      <!-- Lista de vuelos -->
      <div class="vuelos-page__main">
        <div class="flights-header">
          <h2 class="flights-header__title">
            {currentView === 'outbound' ? 'Vuelos de Ida' : 'Vuelos de Regreso'}
          </h2>
          <p class="flights-header__subtitle">
            {loading ? 'Buscando vuelos...' : `${currentFlights.length} vuelos disponibles`}
          </p>
        </div>

        <!-- Estados de carga / error / vacío -->
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
                <div class="flight-card__content">
                  <div class="flight-card__header">
                    <div class="flight-card__code-info">
                      <span class="flight-card__code">{vuelo.numeroVuelo}</span>
                      <span class="flight-card__airline">{vuelo.avionMarca} {vuelo.avionModelo}</span>
                    </div>
                    <div class="flight-card__rating">
                      <span class="flight-card__rating-value">{vuelo.boletosDisponibles}</span>
                      <span class="flight-card__airline">asientos</span>
                    </div>
                  </div>

                  <div class="flight-card__schedule">
                    <div class="schedule-point">
                      <span class="schedule-point__time">{formatHora(vuelo.horaSalida)}</span>
                      <span class="schedule-point__code">{vuelo.origenCodigo}</span>
                    </div>
                    <div class="schedule-duration">
                      <div class="schedule-duration__line"></div>
                      <span class="schedule-duration__time">{formatDuracion(vuelo.duracionMinutos)}</span>
                      <span class="schedule-duration__type">Directo</span>
                    </div>
                    <div class="schedule-point">
                      <span class="schedule-point__time">{formatHora(vuelo.horaLlegada)}</span>
                      <span class="schedule-point__code">{vuelo.destinoCodigo}</span>
                    </div>
                  </div>

                  <!-- Selección de Clase -->
                  <div class="flight-card__class-selection">
                    {#each clases as clase}
                      <button
                        class="class-option"
                        class:class-option--selected={isSelected(vuelo, clase)}
                        on:click={() => selectFlight(vuelo, clase)}
                      >
                        <span class="class-option__name">{clase.tipoDeClase}</span>
                        {#if estaSeleccionado && isSelected(vuelo, clase)}
                          <span class="class-option__label">Seleccionado ✓</span>
                        {:else}
                          <span class="class-option__label">por persona</span>
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
          {currentView === 'outbound' && searchData.tripType === 'roundtrip'
            ? 'Seleccionar Vuelo de Vuelta'
            : 'Siguiente Paso'}
        </button>
      </div>
    {/if}
  </div>
</div>