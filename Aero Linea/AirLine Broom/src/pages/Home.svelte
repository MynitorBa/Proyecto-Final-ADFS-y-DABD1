<script>
  import '../styles/home.css';
  import logoHero from '../assets/BroomHero1.png';
  import { onMount } from 'svelte';

  export let navigateTo;
  export let suggestedDestination = null;

  const API = 'https://localhost:7107';

  let tripType      = 'roundtrip';
  let departureDate = '';
  let returnDate    = '';
  let passengers    = 1;

  let aeropuertos        = [];
  let loadingAeropuertos = true;
  let fromQuery          = '';
  let fromSugeridos      = [];
  let fromSeleccionado   = null;
  let toQuery            = '';
  let toSugeridos        = [];
  let toSeleccionado     = null;

  let fechasDisponiblesIda    = [];
  let fechasDisponiblesVuelta = [];
  let loadingFechas      = false;
  let mostrarCalendarios = false;
  let mesIda    = new Date();
  let mesVuelta = new Date();
  let searchError = '';
  let buscando    = false;

  const diasSemana  = ['LU','MA','MI','JU','VI','SA','DO'];
  const mesesNombre = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

  // Destinos: aeropuertos con imagen para mostrar en la sección
  // Todos los aeropuertos que tengan imagen subida desde el admin
  $: destinosConImagen = aeropuertos.filter(a => a.imagenBase64);

  onMount(async () => {
    try {
      const res = await fetch(`${API}/api/aeropuertos`);
      aeropuertos = await res.json();
    } catch (err) {
      console.error('Error cargando aeropuertos:', err);
    } finally {
      loadingAeropuertos = false;
    }
  });

  $: if (suggestedDestination) {
    toQuery = suggestedDestination;
    const f = document.getElementById('toCity');
    if (f) {
      f.classList.add('broom-home__form-input--highlighted');
      setTimeout(() => f.classList.remove('broom-home__form-input--highlighted'), 2000);
    }
  }

  $: if (passengers && fromSeleccionado && toSeleccionado) cargarFechasDisponibles();

  function onFromInput() {
    const q = fromQuery.toLowerCase();
    fromSugeridos = q.length < 1 ? [] : aeropuertos.filter(a =>
      a.ciudad.toLowerCase().includes(q) ||
      a.nombre.toLowerCase().includes(q) ||
      a.codigo.toLowerCase().includes(q)
    ).slice(0, 5);
    if (fromSeleccionado && fromQuery !== `${fromSeleccionado.ciudad} (${fromSeleccionado.codigo})`) {
      fromSeleccionado = null; resetCalendarios();
    }
  }
  function seleccionarOrigen(a) {
    fromSeleccionado = a;
    fromQuery = `${a.ciudad} (${a.codigo})`;
    fromSugeridos = [];
    cargarFechasDisponibles();
  }

  function onToInput() {
    const q = toQuery.toLowerCase();
    toSugeridos = q.length < 1 ? [] : aeropuertos.filter(a =>
      (a.ciudad.toLowerCase().includes(q) ||
       a.nombre.toLowerCase().includes(q) ||
       a.codigo.toLowerCase().includes(q)) &&
      a.id !== fromSeleccionado?.id
    ).slice(0, 5);
    if (toSeleccionado && toQuery !== `${toSeleccionado.ciudad} (${toSeleccionado.codigo})`) {
      toSeleccionado = null; resetCalendarios();
    }
  }
  function seleccionarDestino(a) {
    toSeleccionado = a;
    toQuery = `${a.ciudad} (${a.codigo})`;
    toSugeridos = [];
    cargarFechasDisponibles();
  }

  function resetCalendarios() {
    fechasDisponiblesIda = []; fechasDisponiblesVuelta = [];
    departureDate = ''; returnDate = '';
    mostrarCalendarios = false;
  }

  async function cargarFechasDisponibles() {
    if (!fromSeleccionado || !toSeleccionado) return;
    loadingFechas = true;
    resetCalendarios();
    try {
      const [resIda, resVuelta] = await Promise.all([
        fetch(`${API}/api/aeropuertos/fechas-disponibles?origenId=${fromSeleccionado.id}&destinoId=${toSeleccionado.id}&cantidadPersonas=${passengers}`),
        fetch(`${API}/api/aeropuertos/fechas-disponibles?origenId=${toSeleccionado.id}&destinoId=${fromSeleccionado.id}&cantidadPersonas=${passengers}`)
      ]);
      const dataIda    = await resIda.json();
      const dataVuelta = await resVuelta.json();
      fechasDisponiblesIda    = dataIda.map(f => f.split('T')[0]);
      fechasDisponiblesVuelta = dataVuelta.map(f => f.split('T')[0]);

      const priIda = fechasDisponiblesIda[0];
      mesIda = priIda
        ? (() => { const d = new Date(priIda + 'T00:00:00'); return new Date(d.getFullYear(), d.getMonth(), 1); })()
        : new Date(new Date().getFullYear(), new Date().getMonth(), 1);

      const priVuelta = fechasDisponiblesVuelta[0];
      mesVuelta = priVuelta
        ? (() => { const d = new Date(priVuelta + 'T00:00:00'); return new Date(d.getFullYear(), d.getMonth(), 1); })()
        : new Date(mesIda.getFullYear(), mesIda.getMonth() + 1, 1);

      mostrarCalendarios = true;
    } catch (err) {
      console.error('Error cargando fechas:', err);
    } finally {
      loadingFechas = false;
    }
  }

  function esFechaDisponibleIda(f)    { return fechasDisponiblesIda.includes(f); }
  function esFechaDisponibleVuelta(f) { return fechasDisponiblesVuelta.includes(f); }

  function getDias(fecha) {
    const y = fecha.getFullYear(), m = fecha.getMonth();
    let ini = new Date(y, m, 1).getDay() - 1;
    if (ini < 0) ini = 6;
    const dias = [];
    for (let i = 0; i < ini; i++) dias.push(null);
    const total = new Date(y, m + 1, 0).getDate();
    for (let d = 1; d <= total; d++)
      dias.push({ dia: d, fecha: `${y}-${String(m+1).padStart(2,'0')}-${String(d).padStart(2,'0')}` });
    return dias;
  }

  $: diasIda    = getDias(mesIda);
  $: diasVuelta = getDias(mesVuelta);
  $: titIda     = `${mesesNombre[mesIda.getMonth()]} ${mesIda.getFullYear()}`;
  $: titVuelta  = `${mesesNombre[mesVuelta.getMonth()]} ${mesVuelta.getFullYear()}`;

  function prevIda()    { mesIda    = new Date(mesIda.getFullYear(),    mesIda.getMonth()    - 1, 1); }
  function nextIda()    { mesIda    = new Date(mesIda.getFullYear(),    mesIda.getMonth()    + 1, 1); }
  function prevVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() - 1, 1); }
  function nextVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() + 1, 1); }

  function pickIda(f) {
    if (!esFechaDisponibleIda(f)) return;
    departureDate = f; searchError = '';
  }
  function pickVuelta(f) {
    if (departureDate && f < departureDate) return;
    if (!esFechaDisponibleVuelta(f)) return;
    returnDate = f; searchError = '';
  }

  async function handleSearchFlight() {
    searchError = '';
    if (!fromSeleccionado) { searchError = 'Selecciona el aeropuerto de origen.';  return; }
    if (!toSeleccionado)   { searchError = 'Selecciona el aeropuerto de destino.'; return; }
    if (!departureDate)    { searchError = 'Selecciona la fecha de ida.';           return; }
    if (tripType === 'roundtrip' && !returnDate) { searchError = 'Selecciona la fecha de regreso.'; return; }

    buscando = true;
    try {
      const resIda = await fetch(`${API}/api/vuelos/buscar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          origenId: fromSeleccionado.id, destinoId: toSeleccionado.id,
          fecha: departureDate, cantidadPasajeros: passengers
        })
      });
      if (!resIda.ok) throw new Error();
      const vuelosIda = await resIda.json();

      let vuelosVuelta = { directos: [], conEscala: [] };
      if (tripType === 'roundtrip' && returnDate) {
        const resVuelta = await fetch(`${API}/api/vuelos/buscar`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            origenId: toSeleccionado.id, destinoId: fromSeleccionado.id,
            fecha: returnDate, cantidadPasajeros: passengers
          })
        });
        if (resVuelta.ok) vuelosVuelta = await resVuelta.json();
      }

      navigateTo('vuelos', {
        vuelosIda, vuelosVuelta,
        searchData: {
          origenId:      fromSeleccionado.id,
          destinoId:     toSeleccionado.id,
          origenNombre:  fromSeleccionado.ciudad,
          destinoNombre: toSeleccionado.ciudad,
          origenCodigo:  fromSeleccionado.codigo,
          destinoCodigo: toSeleccionado.codigo,
          fechaIda:      departureDate,
          fechaVuelta:   returnDate || '',
          pasajeros:     passengers,
          tripType
        }
      });
    } catch (err) {
      console.error('Error en búsqueda:', err);
      searchError = 'Error al buscar vuelos. Intenta nuevamente.';
    } finally {
      buscando = false;
    }
  }
</script>

<div class="broom-home">

  <!-- ── Hero ── -->
  <section class="broom-home__hero">
    <img src={logoHero} alt="Broom AirLine Hero">
    <div class="broom-home__hero-overlay">
      <h1 class="broom-home__hero-title">Vuela a donde tus sueños te lleven</h1>
      <p class="broom-home__hero-subtitle">Descubre el mundo con Broom AirLine</p>
    </div>
  </section>

  <!-- ── Búsqueda ── -->
  <section class="broom-home__search-section">
    <div class="broom-home__search-container">
      <h2 class="broom-home__search-title">Encuentra tu vuelo</h2>

      <form class="broom-home__search-form" on:submit|preventDefault={handleSearchFlight}>

        <div class="broom-home__trip-type">
          <label class="broom-home__radio-label">
            <input type="radio" name="tripType" value="roundtrip" bind:group={tripType} class="broom-home__radio-input">
            <span class="broom-home__radio-text">Ida y vuelta</span>
          </label>
          <label class="broom-home__radio-label">
            <input type="radio" name="tripType" value="oneway" bind:group={tripType} class="broom-home__radio-input">
            <span class="broom-home__radio-text">Solo ida</span>
          </label>
        </div>

        <div class="broom-home__form-grid">

          <div class="broom-home__form-group broom-home__form-group--relative">
            <label for="fromCity" class="broom-home__form-label">Desde</label>
            <input type="text" id="fromCity" bind:value={fromQuery} on:input={onFromInput}
              placeholder={loadingAeropuertos ? 'Cargando...' : 'Ciudad de origen'}
              class="broom-home__form-input" autocomplete="off" />
            {#if fromSugeridos.length > 0}
              <ul class="home-autocomplete__list">
                {#each fromSugeridos as a}
                  <li class="home-autocomplete__item">
                    <button type="button" class="home-autocomplete__btn" on:click={() => seleccionarOrigen(a)}>
                      <span class="home-autocomplete__code">{a.codigo}</span>
                      <div class="home-autocomplete__info">
                        <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                        <span class="home-autocomplete__nombre">{a.nombre} · {a.pais}</span>
                      </div>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group broom-home__form-group--relative">
            <label for="toCity" class="broom-home__form-label">Hacia</label>
            <input type="text" id="toCity" bind:value={toQuery} on:input={onToInput}
              placeholder="Ciudad de destino"
              class="broom-home__form-input" autocomplete="off" />
            {#if toSugeridos.length > 0}
              <ul class="home-autocomplete__list">
                {#each toSugeridos as a}
                  <li class="home-autocomplete__item">
                    <button type="button" class="home-autocomplete__btn" on:click={() => seleccionarDestino(a)}>
                      <span class="home-autocomplete__code">{a.codigo}</span>
                      <div class="home-autocomplete__info">
                        <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                        <span class="home-autocomplete__nombre">{a.nombre} · {a.pais}</span>
                      </div>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group">
            <label for="passengers" class="broom-home__form-label">Pasajeros</label>
            <select id="passengers" bind:value={passengers} class="broom-home__form-input broom-home__form-select">
              {#each Array(9) as _, i}
                <option value={i + 1}>{i + 1} {i === 0 ? 'Pasajero' : 'Pasajeros'}</option>
              {/each}
            </select>
          </div>

          <div class="broom-home__form-group broom-home__form-group--btn">
            <div class="broom-home__form-label broom-home__form-label--hidden" aria-hidden="true">·</div>
            <button type="submit" class="broom-home__search-btn" disabled={buscando}>
              <svg class="broom-home__search-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              {buscando ? 'Buscando...' : 'Buscar vuelo'}
            </button>
          </div>

        </div>

        <!-- Calendarios -->
        {#if loadingFechas}
          <div class="cal-loading">Cargando disponibilidad de vuelos...</div>
        {:else if mostrarCalendarios}
          <div class="cal-wrapper">
            <div class="cal-header-info">
              {#if fechasDisponiblesIda.length > 0 || fechasDisponiblesVuelta.length > 0}
                <span class="cal-info-text">✈ Días con vuelo están marcados — selecciona tu fecha</span>
              {:else}
                <span class="cal-info-text cal-info-text--empty">
                  No hay vuelos disponibles en esta ruta para {passengers} {passengers === 1 ? 'pasajero' : 'pasajeros'}
                </span>
              {/if}
            </div>

            <div class="cal-dual" class:cal-dual--single={tripType === 'oneway'}>

              <div class="cal-container">
                <div class="cal-label">✈ Fecha de ida</div>
                <div class="cal-nav">
                  <button type="button" class="cal-nav__btn" on:click={prevIda}>‹</button>
                  <span class="cal-nav__title">{titIda}</span>
                  <button type="button" class="cal-nav__btn" on:click={nextIda}>›</button>
                </div>
                <div class="cal-grid cal-grid--header">
                  {#each diasSemana as d}<span class="cal-day-name">{d}</span>{/each}
                </div>
                <div class="cal-grid">
                  {#each diasIda as item}
                    {#if item === null}
                      <span class="cal-day cal-day--empty"></span>
                    {:else}
                      {@const disp = esFechaDisponibleIda(item.fecha)}
                      {@const sel  = departureDate === item.fecha}
                      <button type="button" class="cal-day"
                        class:cal-day--disponible={disp && !sel}
                        class:cal-day--seleccionado-ida={sel}
                        class:cal-day--bloqueado={!disp}
                        on:click={() => pickIda(item.fecha)}
                        disabled={!disp}
                        title={disp ? 'Vuelo disponible' : 'Sin vuelos'}>
                        {item.dia}
                      </button>
                    {/if}
                  {/each}
                </div>
                {#if departureDate}
                  <div class="cal-selected-info">Ida: <strong>{departureDate}</strong></div>
                {/if}
              </div>

              {#if tripType === 'roundtrip'}
                <div class="cal-container">
                  <div class="cal-label">↩ Fecha de regreso</div>
                  <div class="cal-nav">
                    <button type="button" class="cal-nav__btn" on:click={prevVuelta}>‹</button>
                    <span class="cal-nav__title">{titVuelta}</span>
                    <button type="button" class="cal-nav__btn" on:click={nextVuelta}>›</button>
                  </div>
                  <div class="cal-grid cal-grid--header">
                    {#each diasSemana as d}<span class="cal-day-name">{d}</span>{/each}
                  </div>
                  <div class="cal-grid">
                    {#each diasVuelta as item}
                      {#if item === null}
                        <span class="cal-day cal-day--empty"></span>
                      {:else}
                        {@const bloqIda  = departureDate && item.fecha < departureDate}
                        {@const bloqDisp = !esFechaDisponibleVuelta(item.fecha)}
                        {@const bloq = bloqIda || bloqDisp}
                        {@const sel  = returnDate === item.fecha}
                        <button type="button" class="cal-day"
                          class:cal-day--disponible-vuelta={!bloq && !sel}
                          class:cal-day--seleccionado-vuelta={sel}
                          class:cal-day--bloqueado={bloq}
                          on:click={() => pickVuelta(item.fecha)}
                          disabled={bloq}
                          title={bloqIda ? 'Fecha anterior a la ida' : bloqDisp ? 'Sin vuelos de regreso' : 'Vuelo disponible'}>
                          {item.dia}
                        </button>
                      {/if}
                    {/each}
                  </div>
                  {#if returnDate}
                    <div class="cal-selected-info cal-selected-info--vuelta">Regreso: <strong>{returnDate}</strong></div>
                  {/if}
                </div>
              {/if}

            </div>
          </div>
        {/if}

        {#if searchError}
          <p class="broom-home__search-error">{searchError}</p>
        {/if}

      </form>
    </div>
  </section>

  <!-- ── Destinos desde la BD (aeropuertos con imagen) ── -->
  {#if destinosConImagen.length > 0}
  <section class="broom-home__destinations">
    <div class="broom-home__destinations-container">
      <h2 class="broom-home__destinations-title">Destinos destacados</h2>
      <div class="broom-home__destinations-grid">
        {#each destinosConImagen as aeropuerto}
          <article class="broom-home__destination-card"
            on:click={() => { toQuery = `${aeropuerto.ciudad} (${aeropuerto.codigo})`; toSeleccionado = aeropuerto; }}
            role="button" tabindex="0"
            on:keydown={e => e.key === 'Enter' && navigateTo('vuelos')}>
            <div class="broom-home__destination-image">
              <img
                src={aeropuerto.imagenBase64.startsWith("data:") ? aeropuerto.imagenBase64 : `data:image/jpeg;base64,${aeropuerto.imagenBase64}`}
                alt="{aeropuerto.ciudad}, {aeropuerto.pais}"
                class="broom-home__destination-image-visual"
              />
              <div class="broom-home__destination-badge">{aeropuerto.codigo}</div>
            </div>
            <div class="broom-home__destination-content">
              <h3 class="broom-home__destination-name">{aeropuerto.ciudad}</h3>
              <p class="broom-home__destination-meta">{aeropuerto.pais}</p>
              <p class="broom-home__destination-description">{aeropuerto.nombre}</p>
            </div>
          </article>
        {/each}
      </div>
      <div class="broom-home__destinations-actions">
        <button type="button" class="broom-home__destinations-btn" on:click={() => navigateTo('destinos-destacados')}>
          Ver más destinos
        </button>
      </div>
    </div>
  </section>
  {/if}

</div>