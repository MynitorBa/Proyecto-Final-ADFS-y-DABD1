<script>
  // @ts-nocheck
  import '../styles/vuelos.css';
  import DetalleVueloModal from './DetalleVuelo.svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;
  export let searchParams = null;

  import { API } from '../lib/api.js';

  // ── flag búsqueda global ──
  let isGlobalSearch = false;
  let globalSearchQuery = '';
  let newGlobalQuery = '';

  let searchData = {
    origenId: null, destinoId: null,
    origenNombre: '', destinoNombre: '',
    origenCodigo: '', destinoCodigo: '',
    fechaIda: '', fechaVuelta: '',
    pasajeros: 1, tripType: 'roundtrip',
    flightMode: 'todos'
  };

  let currentView = 'outbound';

  // { type: 'directo'|'escala', flight, escala, clase }
  let selectedOutbound = { type: null, flight: null, escala: null, clase: null };
  let selectedReturn   = { type: null, flight: null, escala: null, clase: null };

  let showDetailModal = false;
  let detailFlight    = null;

  let vuelosIda    = { directos: [], conEscala: [] };
  let vuelosVuelta = { directos: [], conEscala: [] };
  let loadingIda   = false;
  let loadingVuelta = false;
  let errorIda     = '';
  let errorVuelta  = '';
  let creandoReserva = false;
  let errorReserva   = '';

  // Tab activo según flightMode inicial; se puede cambiar en vuelos
  let tabIda    = 'directos';
  let tabVuelta = 'directos';

  const clases = [
    { id: 1, tipoDeClase: 'Turista' },
    { id: 2, tipoDeClase: 'Ejecutiva' }
  ];
  let precioMin = '';
  let precioMax = '';
  let claseSeleccionada = '';

  /* ── Init ── */
  if (searchParams?.fromGlobalSearch) {
    // NUEVO: Búsqueda global desde Header
    isGlobalSearch = true;
    globalSearchQuery = searchParams.globalSearchQuery || '';
    newGlobalQuery = globalSearchQuery;
    const vuelos = searchParams.globalSearchResults || [];
    vuelosIda = { directos: vuelos, conEscala: [] };
    tabIda = 'directos';
    searchData.tripType = 'oneway';
  } else if (searchParams?.searchData) {
    searchData   = searchParams.searchData;
    vuelosIda    = searchParams.vuelosIda   ?? { directos: [], conEscala: [] };
    vuelosVuelta = searchParams.vuelosVuelta ?? { directos: [], conEscala: [] };

    // Preseleccionar tab según flightMode que viene del Home
    const fm = searchData.flightMode ?? 'todos';
    if (fm === 'escalas') {
      tabIda = 'escalas'; tabVuelta = 'escalas';
    } else if (fm === 'directo') {
      tabIda = 'directos'; tabVuelta = 'directos';
    } else {
      // 'todos': tab al primero que tenga resultados
      tabIda    = (vuelosIda.directos?.length    ?? 0) > 0 ? 'directos' : 'escalas';
      tabVuelta = (vuelosVuelta.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
    }
  } else {
    errorIda = 'No se encontró información de búsqueda. Realiza una nueva búsqueda.';
  }

  /* ── Reactivos ── */
  $: currentVuelos    = currentView === 'outbound' ? vuelosIda : vuelosVuelta;
  $: currentTab       = currentView === 'outbound' ? tabIda    : tabVuelta;
  $: loading          = currentView === 'outbound' ? loadingIda    : loadingVuelta;
  $: errorActual      = currentView === 'outbound' ? errorIda      : errorVuelta;
  $: listaDirectos    = currentVuelos.directos  ?? [];
  $: listaEscalas     = currentVuelos.conEscala ?? [];
  $: listaActiva      = currentTab === 'directos' ? listaDirectos : listaEscalas;
  $: totalResultados  = listaDirectos.length + listaEscalas.length;
  $: canProceed       = currentView === 'outbound'
      ? selectedOutbound.type !== null
      : selectedReturn.type   !== null;

  // Filtrar según flightMode del searchData
  $: listaDirectosFiltrada = filtrarSegunMode(listaDirectos);
  $: listaEscalasFiltrada  = filtrarSegunMode(listaEscalas);

  function filtrarSegunMode(lista) { return lista; } // La API devuelve lo correcto; el modo filtra TABS

  // Tabs visibles según flightMode
  $: mostrarTabDirectos = (searchData.flightMode === 'todos' || searchData.flightMode === 'directo') && listaDirectos.length > 0;
  $: mostrarTabEscalas  = (searchData.flightMode === 'todos' || searchData.flightMode === 'escalas')  && listaEscalas.length  > 0;

  /* ── Tab control ── */
  function setTab(tab) {
    if (currentView === 'outbound') tabIda = tab;
    else tabVuelta = tab;
  }

  /* ── Re-búsqueda global ── */
  async function reBuscarGlobal() {
    const q = newGlobalQuery.trim();
    if (q.length < 2) return;
    loadingIda = true; errorIda = '';
    try {
      const res = await fetch(`${API}/api/vuelos/busqueda-general?query=${encodeURIComponent(q)}`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error();
      const vuelos = await res.json();
      vuelosIda = { directos: vuelos, conEscala: [] };
      globalSearchQuery = q;
      tabIda = 'directos';
      selectedOutbound = { type: null, flight: null, escala: null, clase: null };
    } catch {
      errorIda = 'Error al buscar vuelos.';
    } finally {
      loadingIda = false;
    }
  }

  /* ── Filtros ── */
  async function buscarVuelos(esIda) {
    if (isGlobalSearch) return;
    const origen  = esIda ? searchData.origenId  : searchData.destinoId;
    const destino = esIda ? searchData.destinoId : searchData.origenId;
    const fecha   = esIda ? searchData.fechaIda  : searchData.fechaVuelta;
    if (!origen || !destino || !fecha) return;

    if (esIda) { loadingIda = true;    errorIda = ''; }
    else       { loadingVuelta = true; errorVuelta = ''; }

    try {
      const body = { origenId: origen, destinoId: destino, fecha, cantidadPasajeros: searchData.pasajeros };
      if (precioMin !== '') body.precioMinimo = parseFloat(precioMin);
      if (precioMax !== '') body.precioMaximo = parseFloat(precioMax);
      if (claseSeleccionada !== '') body.claseId = parseInt(claseSeleccionada);

      const res = await fetch(`${API}/api/vuelos/buscar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error();
      const data = await res.json();

      if (esIda) {
        vuelosIda = data;
        tabIda = (data.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
      } else {
        vuelosVuelta = data;
        tabVuelta = (data.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
      }
    } catch {
      if (esIda) errorIda = 'No se pudieron cargar los vuelos de ida.';
      else errorVuelta = 'No se pudieron cargar los vuelos de vuelta.';
    } finally {
      if (esIda) loadingIda = false;
      else loadingVuelta = false;
    }
  }

  async function aplicarFiltros() {
    if (isGlobalSearch) { reBuscarGlobal(); return; }
    await buscarVuelos(currentView === 'outbound');
  }
  function limpiarFiltros() {
    precioMin = ''; precioMax = ''; claseSeleccionada = '';
    aplicarFiltros();
  }

  /* ── Selección ── */
  function selectDirecto(vuelo, clase) {
    if (currentView === 'outbound') selectedOutbound = { type: 'directo', flight: vuelo, escala: null, clase };
    else selectedReturn = { type: 'directo', flight: vuelo, escala: null, clase };
  }
  function isSelectedDirecto(vuelo, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.type === 'directo' && s.flight?.id === vuelo.id && s.clase?.id === clase.id;
  }

  function selectEscala(escala, clase) {
    if (currentView === 'outbound') selectedOutbound = { type: 'escala', flight: null, escala, clase };
    else selectedReturn = { type: 'escala', flight: null, escala, clase };
  }
  function isSelectedEscala(escala, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.type === 'escala' &&
      s.escala?.tramos?.[0]?.id === escala?.tramos?.[0]?.id &&
      s.clase?.id === clase.id;
  }

  function viewDetails(vuelo) { detailFlight = vuelo; showDetailModal = true; }
  function closeModal()       { showDetailModal = false; detailFlight = null; }

  /* ── Siguiente paso / reserva ── */
  async function nextStep() {
    errorReserva = '';
    if (currentView === 'outbound' && selectedOutbound.type) {
      if (!isGlobalSearch && searchData.tripType === 'roundtrip') {
        currentView = 'return';
        if ((vuelosVuelta.directos?.length ?? 0) === 0 && (vuelosVuelta.conEscala?.length ?? 0) === 0)
          await buscarVuelos(false);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        await crearReserva();
      }
    } else if (currentView === 'return' && selectedReturn.type) {
      await crearReserva();
    }
  }

  async function crearReserva() {
    const sesionActual = $sesion;
    if (!sesionActual) { navigateTo('login'); return; }

    creandoReserva = true;
    errorReserva   = '';
    const pasajeros = Number(searchData.pasajeros) || 1;
    const vuelos = [];
    _agregarVuelos(vuelos, selectedOutbound, pasajeros);
    if (selectedReturn.type) _agregarVuelos(vuelos, selectedReturn, pasajeros);

    console.log('Body reserva:', JSON.stringify({ vuelos }, null, 2));
    try {
      const res = await fetch(`${API}/api/reservaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ vuelos })
      });
      const data = await res.json();
      if (!res.ok) { errorReserva = data.message ?? 'Error al crear la reservación.'; return; }
      navigateTo('datos-pasajeros', { reserva: data, searchData });
    } catch {
      errorReserva = 'Error de conexión al crear la reservación.';
    } finally {
      creandoReserva = false;
    }
  }

  function _agregarVuelos(arr, sel, pasajeros) {
    if (sel.type === 'directo') {
      arr.push({ vueloId: Number(sel.flight.id), claseId: Number(sel.clase.id), cantidadPasajeros: pasajeros });
    } else if (sel.type === 'escala') {
      for (const tramo of sel.escala.tramos)
        arr.push({ vueloId: Number(tramo.id), claseId: Number(sel.clase.id), cantidadPasajeros: pasajeros });
    }
  }

  function goBack() {
    if (currentView === 'return') { currentView = 'outbound'; window.scrollTo({ top: 0, behavior: 'smooth' }); }
    else navigateTo('home');
  }

  /* ── Helpers ── */
  function formatDuracion(min) {
    if (!min) return '';
    return `${Math.floor(min / 60)}h ${min % 60}m`;
  }
  function formatHora(h) { return h ? h.substring(0, 5) : ''; }
  function formatFecha(f) {
    if (!f) return '';
    try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }); }
    catch { return f; }
  }

  function getPrecioDirecto(vuelo, claseId) {
    return claseId === 1 ? vuelo.precioTurista : claseId === 2 ? vuelo.precioEjecutiva : null;
  }
  function getBoletosDirecto(vuelo, claseId) {
    return claseId === 1 ? (vuelo.boletosDisponiblesTurista ?? 0) : claseId === 2 ? (vuelo.boletosDisponiblesEjecutiva ?? 0) : 0;
  }
  function getPrecioEscala(escala, claseId) {
    return claseId === 1 ? escala.precioTuristaTotal : claseId === 2 ? escala.precioEjecutivaTotal : null;
  }
  function getBoletosEscala(escala, claseId) {
    return claseId === 1 ? (escala.boletosDisponiblesTurista ?? 0) : claseId === 2 ? (escala.boletosDisponiblesEjecutiva ?? 0) : 0;
  }
  function formatPrecio(p) {
    if (!p) return 'No disponible';
    return `$ ${p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }
  function labelEscala(n) { return n === 1 ? '1 escala' : `${n} escalas`; }
</script>

{#if showDetailModal && detailFlight}
  <DetalleVueloModal flight={detailFlight} onClose={closeModal} />
{/if}

<div class="vuelos-page">
  <div class="vuelos-page__container">

    <!-- HEADER -->
    <div class="vuelos-page__header">
      <button class="vuelos-page__back" on:click={goBack}>Volver</button>

      {#if isGlobalSearch}
        <!-- ══ NUEVO: Header de búsqueda global ══ -->
        <div class="search-summary">
          <div class="search-summary__route">
            <span class="search-summary__origin">Resultados para</span>
            <span class="search-summary__destination">"{globalSearchQuery}"</span>
          </div>
          <div class="search-summary__details">
            <div class="search-summary__item">
              <span class="search-summary__label">Vuelos encontrados</span>
              <span class="search-summary__value">{listaDirectos.length}</span>
            </div>
          </div>
          <!-- Mini buscador para re-buscar -->
          <div style="display:flex; gap:0.5rem; margin-top:1rem;">
            <input type="text" bind:value={newGlobalQuery}
              on:keydown={e => e.key === 'Enter' && reBuscarGlobal()}
              placeholder="Buscar otra vez..."
              style="flex:1; padding:0.6rem 1rem; border:1px solid #B89A7A; border-radius:20px; font-size:0.85rem; font-family:inherit; outline:none;"
            >
            <button on:click={reBuscarGlobal}
              style="padding:0.6rem 1.2rem; background:#1C1A18; color:#fff; border:none; border-radius:20px; cursor:pointer; font-family:inherit; font-size:0.8rem; font-weight:600;">
              Buscar
            </button>
          </div>
        </div>
      {:else}
        <!-- ══ Header de búsqueda normal (sin cambios) ══ -->
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
            {#if searchData.flightMode && searchData.flightMode !== 'todos'}
              <div class="search-summary__item">
                <span class="search-summary__label">Tipo</span>
                <span class="search-summary__value search-summary__value--mode">
                  {searchData.flightMode === 'directo' ? '✈ Directo' : '⇌ Con escalas'}
                </span>
              </div>
            {/if}
          </div>
        </div>

        <div class="step-indicator">
          <div class="step-indicator__item"
            class:step-indicator__item--active={currentView === 'outbound'}
            class:step-indicator__item--completed={selectedOutbound.type}>
            <span class="step-indicator__number">1</span>
            <span class="step-indicator__label">Vuelo de Ida</span>
          </div>
          {#if searchData.tripType === 'roundtrip'}
            <div class="step-indicator__line"></div>
            <div class="step-indicator__item"
              class:step-indicator__item--active={currentView === 'return'}
              class:step-indicator__item--completed={selectedReturn.type}>
              <span class="step-indicator__number">2</span>
              <span class="step-indicator__label">Vuelo de Vuelta</span>
            </div>
          {/if}
        </div>
      {/if}
    </div>

    <!-- CONTENT -->
    <div class="vuelos-page__content" style={isGlobalSearch ? 'grid-template-columns: 1fr;' : ''}>

      <!-- FILTROS (solo en búsqueda normal) -->
      {#if !isGlobalSearch}
        <aside class="vuelos-page__filters">
          <div class="filters-panel">
            <div class="filters-panel__header">
              <h3 class="filters-panel__title">Filtros</h3>
              <button class="filters-panel__clear" on:click={limpiarFiltros}>Limpiar</button>
            </div>

            <!-- Filtro tipo de vuelo -->
            <div class="filter-group">
              <span class="filter-group__label">Tipo de vuelo</span>
              <div class="filter-flight-mode">
                <button
                  class="filter-mode-btn"
                  class:filter-mode-btn--active={currentTab === 'directos'}
                  on:click={() => setTab('directos')}
                  disabled={listaDirectos.length === 0}
                >
                  <span class="filter-mode-btn__icon">✈</span>
                  <span class="filter-mode-btn__text">Directo</span>
                  {#if listaDirectos.length > 0}
                    <span class="filter-mode-btn__count">{listaDirectos.length}</span>
                  {/if}
                </button>
                <button
                  class="filter-mode-btn filter-mode-btn--escala"
                  class:filter-mode-btn--active={currentTab === 'escalas'}
                  on:click={() => setTab('escalas')}
                  disabled={listaEscalas.length === 0}
                >
                  <span class="filter-mode-btn__icon">⇌</span>
                  <span class="filter-mode-btn__text">Con escalas</span>
                  {#if listaEscalas.length > 0}
                    <span class="filter-mode-btn__count">{listaEscalas.length}</span>
                  {/if}
                </button>
              </div>
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
      {/if}

      <!-- LISTA -->
      <div class="vuelos-page__main">
        <div class="flights-header">
          <h2 class="flights-header__title">
            {#if isGlobalSearch}
              Vuelos disponibles
            {:else}
              {currentView === 'outbound' ? 'Vuelos de Ida' : 'Vuelos de Regreso'}
            {/if}
          </h2>
          <p class="flights-header__subtitle">
            {#if loading}
              Buscando vuelos...
            {:else if isGlobalSearch}
              {totalResultados} vuelos encontrados para "{globalSearchQuery}"
            {:else}
              {totalResultados} opciones — mostrando {currentTab === 'directos' ? 'vuelos directos' : 'vuelos con escalas'}
            {/if}
          </p>
        </div>

        {#if loading}
          <div class="vuelos-estado">Buscando vuelos...</div>

        {:else if errorActual}
          <div class="vuelos-estado vuelos-estado--info">{errorActual}</div>

        {:else if totalResultados === 0}
          <div class="vuelos-estado">
            {#if isGlobalSearch}
              No se encontraron vuelos para "{globalSearchQuery}". Intenta con otra búsqueda.
            {:else}
              No hay vuelos disponibles para esta ruta y fecha.
            {/if}
          </div>

        {:else if currentTab === 'directos'}

          {#if listaDirectos.length === 0}
            <div class="vuelos-estado">No hay vuelos directos. Cambia a "Con escalas" en el panel de filtros.</div>
          {:else}
            <div class="flights-list">
              {#each listaDirectos as vuelo}
                {@const selObj = currentView === 'outbound' ? selectedOutbound : selectedReturn}
                {@const estaSeleccionado = selObj.type === 'directo' && selObj.flight?.id === vuelo.id}

                <div class="flight-card" class:flight-card--selected={estaSeleccionado}>
                  <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <circle cx="190" cy="15" r="90" stroke="#c9a96e" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="62" stroke="#c9a96e" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="36" stroke="#c9a96e" stroke-width="0.6"/>
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
                      {#if isGlobalSearch && vuelo.fecha}
                        <span class="flight-card__badge flight-card__badge--directo">{formatFecha(vuelo.fecha)}</span>
                      {:else}
                        <span class="flight-card__badge flight-card__badge--directo">Directo</span>
                      {/if}
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
                        {#if isGlobalSearch}
                          <span class="schedule-duration__type">{vuelo.origenCiudad || ''} → {vuelo.destinoCiudad || ''}</span>
                        {:else}
                          <span class="schedule-duration__type">Directo</span>
                        {/if}
                      </div>
                      <div class="schedule-point schedule-point--right">
                        <span class="schedule-point__time">{formatHora(vuelo.horaLlegada)}</span>
                        <span class="schedule-point__code">{vuelo.destinoCodigo || ''}</span>
                      </div>
                    </div>

                    <div class="flight-card__class-selection">
                      {#each clases as clase}
                        {@const precio     = getPrecioDirecto(vuelo, clase.id)}
                        {@const boletos    = getBoletosDirecto(vuelo, clase.id)}
                        {@const disponible = precio !== null && precio > 0 && boletos >= (isGlobalSearch ? 1 : searchData.pasajeros)}
                        <button
                          class="class-option"
                          class:class-option--selected={isSelectedDirecto(vuelo, clase)}
                          class:class-option--disabled={!disponible}
                          disabled={!disponible}
                          on:click={() => disponible && selectDirecto(vuelo, clase)}
                        >
                          <span class="class-option__name">{clase.tipoDeClase}</span>
                          {#if disponible}
                            <span class="class-option__price">{formatPrecio(precio)}</span>
                            <span class="class-option__label">{isSelectedDirecto(vuelo, clase) ? 'Seleccionado ✓' : 'por persona'}</span>
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

        {:else}
          <!-- ESCALAS -->
          {#if listaEscalas.length === 0}
            <div class="vuelos-estado">No hay vuelos con escalas.</div>
          {:else}
            <div class="flights-list">
              {#each listaEscalas as escala}
                {@const selObj = currentView === 'outbound' ? selectedOutbound : selectedReturn}
                {@const estaSeleccionado = selObj.type === 'escala' && selObj.escala?.tramos?.[0]?.id === escala?.tramos?.[0]?.id}

                <div class="flight-card flight-card--escala" class:flight-card--selected={estaSeleccionado}>
                  <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <circle cx="190" cy="15" r="90" stroke="#3b4f6b" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="62" stroke="#3b4f6b" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="36" stroke="#3b4f6b" stroke-width="0.6"/>
                    <path d="M95 72 L148 48 L160 53 L142 66 L156 62 L160 68 L128 79 Z" fill="#3b4f6b" opacity="0.18"/>
                    <line x1="60" y1="125" x2="200" y2="125" stroke="#3b4f6b" stroke-width="0.5" stroke-dasharray="4 7"/>
                  </svg>

                  <div class="flight-card__content">
                    <div class="flight-card__header">
                      <div class="flight-card__code-info">
                        <span class="flight-card__code">
                          {escala.tramos[0]?.origenCodigo} → {escala.tramos[escala.tramos.length - 1]?.destinoCodigo}
                        </span>
                        <span class="flight-card__airline">
                          {escala.tramos.map(t => t.numeroVuelo).join(' · ')}
                        </span>
                      </div>
                      <span class="flight-card__badge flight-card__badge--escala">
                        {labelEscala(escala.numeroEscalas)}
                      </span>
                    </div>

                    <div class="escala-timeline">
                      {#each escala.tramos as tramo, ti}
                        <div class="escala-tramo">
                          <div class="escala-tramo__airports">
                            <div class="escala-tramo__point">
                              <span class="escala-tramo__time">{formatHora(tramo.horaSalida)}</span>
                              <span class="escala-tramo__code">{tramo.origenCodigo}</span>
                              <span class="escala-tramo__city">{tramo.origenCiudad}</span>
                            </div>
                            <div class="escala-tramo__middle">
                              <div class="escala-tramo__track">
                                <div class="escala-tramo__dot"></div>
                                <div class="escala-tramo__line"></div>
                                <span class="escala-tramo__plane">✈</span>
                                <div class="escala-tramo__line"></div>
                                <div class="escala-tramo__dot"></div>
                              </div>
                              <span class="escala-tramo__dur">{formatDuracion(tramo.duracionMinutos)}</span>
                              <span class="escala-tramo__num">{tramo.numeroVuelo}</span>
                            </div>
                            <div class="escala-tramo__point escala-tramo__point--right">
                              <span class="escala-tramo__time">{formatHora(tramo.horaLlegada)}</span>
                              <span class="escala-tramo__code">{tramo.destinoCodigo}</span>
                              <span class="escala-tramo__city">{tramo.destinoCiudad}</span>
                            </div>
                          </div>
                        </div>

                        {#if ti < escala.tramos.length - 1}
                            {@const llegada = new Date(`1970-01-01T${escala.tramos[ti].horaLlegada}`)}
                            {@const salida  = new Date(`1970-01-01T${escala.tramos[ti + 1].horaSalida}`)}
                            {@const minutos = ((salida - llegada) / 60000 + 1440) % 1440}
                            <div class="escala-conexion">
                              <div class="escala-conexion__line"></div>
                              <div class="escala-conexion__badge">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                  <circle cx="12" cy="12" r="4"/>
                                </svg>
                                Escala · {formatDuracion(minutos)} en {escala.tramos[ti].destinoCiudad}
                              </div>
                              <div class="escala-conexion__line"></div>
                            </div>
                          {/if}
                      {/each}
                    </div>

                    <div class="escala-resumen">
                      <div class="escala-resumen__item">
                        <span class="escala-resumen__label">Duración total</span>
                        <span class="escala-resumen__value">{formatDuracion(escala.duracionTotalMinutos)}</span>
                      </div>
                      <div class="escala-resumen__item">
                        <span class="escala-resumen__label">Escalas</span>
                        <span class="escala-resumen__value">{escala.numeroEscalas}</span>
                      </div>
                    </div>

                    <div class="flight-card__class-selection">
                      {#each clases as clase}
                        {@const precio     = getPrecioEscala(escala, clase.id)}
                        {@const boletos    = getBoletosEscala(escala, clase.id)}
                        {@const disponible = precio !== null && precio > 0 && boletos >= searchData.pasajeros}
                        <button
                          class="class-option"
                          class:class-option--selected={isSelectedEscala(escala, clase)}
                          class:class-option--disabled={!disponible}
                          disabled={!disponible}
                          on:click={() => disponible && selectEscala(escala, clase)}
                        >
                          <span class="class-option__name">{clase.tipoDeClase}</span>
                          {#if disponible}
                            <span class="class-option__price">{formatPrecio(precio)}</span>
                            <span class="class-option__label">{isSelectedEscala(escala, clase) ? 'Seleccionado ✓' : 'precio total'}</span>
                          {:else}
                            <span class="class-option__label class-option__label--unavailable">No disponible</span>
                          {/if}
                        </button>
                      {/each}
                    </div>

                    <button class="flight-card__details-btn" on:click={() => viewDetails(escala)}>
                      Ver Detalles de la Ruta
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          {/if}
        {/if}
      </div>
    </div>

    {#if errorReserva}
      <div class="vuelos-error-reserva">{errorReserva}</div>
    {/if}

    {#if canProceed}
      <div class="vuelos-page__next-step">
        <button class="next-step-btn" on:click={nextStep} disabled={creandoReserva}>
          {#if creandoReserva}
            Creando reservación...
          {:else if !isGlobalSearch && currentView === 'outbound' && searchData.tripType === 'roundtrip'}
            Seleccionar Vuelo de Vuelta
          {:else}
            Siguiente Paso
          {/if}
        </button>
      </div>
    {/if}

  </div>
</div>