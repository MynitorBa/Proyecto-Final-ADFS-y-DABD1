<script>
  import '../styles/home.css';
  import logoHero from '../assets/BroomHero1.png';
  import { onMount } from 'svelte';
  
  export let navigateTo;
  export let suggestedDestination = null;

  let tripType = 'roundtrip'; 
  let departureDate = '';
  let returnDate = '';
  let passengers = 1;
  let aeropuertos = [];
  let loadingAeropuertos = true;
  let fromQuery = '';
  let fromSugeridos = [];
  let fromSeleccionado = null;
  let toQuery = '';
  let toSugeridos = [];
  let toSeleccionado = null;
  
  // SEPARAR fechas disponibles para IDA y VUELTA
  let fechasDisponiblesIda = [];
  let fechasDisponiblesVuelta = [];
  
  let loadingFechas = false;
  let mostrarCalendarios = false;
  let mesIda = new Date();
  let mesVuelta = new Date();
  let searchError = '';
  let guardandoBusqueda = false;

  onMount(async () => {
    try {
      const res = await fetch('https://localhost:7107/api/aeropuertos');
      aeropuertos = await res.json();
      console.log('✅ Aeropuertos cargados:', aeropuertos.length);
    } catch (err) { 
      console.error('❌ Error cargando aeropuertos:', err); 
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
    fechasDisponiblesIda = [];
    fechasDisponiblesVuelta = [];
    departureDate = '';
    returnDate = '';
    mostrarCalendarios = false;
  }

  async function cargarFechasDisponibles() {
    if (!fromSeleccionado || !toSeleccionado) return;
    loadingFechas = true;
    resetCalendarios();
    
    try {
      // CARGAR FECHAS DE IDA (origen → destino)
      console.log('📅 Cargando fechas de IDA...');
      const resIda = await fetch(
        `https://localhost:7107/api/aeropuertos/fechas-disponibles?origenId=${fromSeleccionado.id}&destinoId=${toSeleccionado.id}`
      );
      const dataIda = await resIda.json();
      fechasDisponiblesIda = dataIda.map(f => f.split('T')[0]);
      console.log('✅ Fechas IDA:', fechasDisponiblesIda);

      // CARGAR FECHAS DE VUELTA (destino → origen)
      console.log('📅 Cargando fechas de VUELTA...');
      const resVuelta = await fetch(
        `https://localhost:7107/api/aeropuertos/fechas-disponibles?origenId=${toSeleccionado.id}&destinoId=${fromSeleccionado.id}`
      );
      const dataVuelta = await resVuelta.json();
      fechasDisponiblesVuelta = dataVuelta.map(f => f.split('T')[0]);
      console.log('✅ Fechas VUELTA:', fechasDisponiblesVuelta);

      // Configurar meses del calendario
      if (fechasDisponiblesIda.length > 0) {
        const primeraIda = new Date(fechasDisponiblesIda[0] + 'T00:00:00');
        mesIda = new Date(primeraIda.getFullYear(), primeraIda.getMonth(), 1);
      } else {
        const hoy = new Date();
        mesIda = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
      }

      // Calendario de vuelta debe iniciar en el primer vuelo de vuelta disponible
      if (fechasDisponiblesVuelta.length > 0) {
        const primeraVuelta = new Date(fechasDisponiblesVuelta[0] + 'T00:00:00');
        mesVuelta = new Date(primeraVuelta.getFullYear(), primeraVuelta.getMonth(), 1);
      } else {
        // Si no hay fechas de vuelta, usar mes siguiente a IDA como fallback
        mesVuelta = new Date(mesIda.getFullYear(), mesIda.getMonth() + 1, 1);
      }
      
      mostrarCalendarios = true;
      
    } catch (err) { 
      console.error('❌ Error cargando fechas:', err); 
    } finally { 
      loadingFechas = false; 
    }
  }

  // Validar si fecha de IDA está disponible
  function esFechaDisponibleIda(f) { 
    return fechasDisponiblesIda.includes(f); 
  }

  // Validar si fecha de VUELTA está disponible
  function esFechaDisponibleVuelta(f) { 
    return fechasDisponiblesVuelta.includes(f); 
  }

  const diasSemana = ['LU','MA','MI','JU','VI','SA','DO'];
  const mesesNombre = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

  function getDias(fecha) {
    const y = fecha.getFullYear(), m = fecha.getMonth();
    let ini = new Date(y, m, 1).getDay() - 1;
    if (ini < 0) ini = 6;
    const dias = [];
    for (let i = 0; i < ini; i++) dias.push(null);
    const total = new Date(y, m + 1, 0).getDate();
    for (let d = 1; d <= total; d++) {
      dias.push({
        dia: d,
        fecha: `${y}-${String(m+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`
      });
    }
    return dias;
  }

  $: diasIda = getDias(mesIda);
  $: diasVuelta = getDias(mesVuelta);
  $: titIda = `${mesesNombre[mesIda.getMonth()]} ${mesIda.getFullYear()}`;
  $: titVuelta = `${mesesNombre[mesVuelta.getMonth()]} ${mesVuelta.getFullYear()}`;

  function prevIda() { mesIda = new Date(mesIda.getFullYear(), mesIda.getMonth() - 1, 1); }
  function nextIda() { mesIda = new Date(mesIda.getFullYear(), mesIda.getMonth() + 1, 1); }
  function prevVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() - 1, 1); }
  function nextVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() + 1, 1); }

  function pickIda(f) {
    if (!esFechaDisponibleIda(f)) return;
    departureDate = f;
    searchError = '';
  }

  function pickVuelta(f) {
    // Validar que sea después de la fecha de ida Y que haya vuelos disponibles
    if (departureDate && f < departureDate) return;
    if (!esFechaDisponibleVuelta(f)) return;
    returnDate = f;
    searchError = '';
  }

  async function handleSearchFlight() {
    searchError = '';
    
    // Validaciones
    if (!fromSeleccionado) { 
      searchError = 'Selecciona el aeropuerto de origen.'; 
      return; 
    }
    if (!toSeleccionado) { 
      searchError = 'Selecciona el aeropuerto de destino.'; 
      return; 
    }
    if (!departureDate) { 
      searchError = 'Selecciona la fecha de ida.'; 
      return; 
    }
    if (tripType === 'roundtrip' && !returnDate) {
      searchError = 'Selecciona la fecha de regreso.'; 
      return;
    }

    guardandoBusqueda = true;
    
    try {
      const busquedaData = {
        origenId: fromSeleccionado.id,
        destinoId: toSeleccionado.id,
        origenNombre: fromSeleccionado.ciudad,
        destinoNombre: toSeleccionado.ciudad,
        origenCodigo: fromSeleccionado.codigo,
        destinoCodigo: toSeleccionado.codigo,
        fechaIda: departureDate,
        fechaVuelta: returnDate || '',
        pasajeros: passengers,
        tripType: tripType
      };

      console.log('📤 Guardando búsqueda en backend:', busquedaData);

      const response = await fetch('https://localhost:7107/api/busquedas/guardar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(busquedaData)
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Error del servidor:', response.status, errorText);
        throw new Error('Error al guardar la búsqueda');
      }

      const resultado = await response.json();
      console.log('✅ Búsqueda guardada con ID:', resultado.id);

      // Navegar a vuelos pasando solo el ID
      navigateTo('vuelos', { busquedaId: resultado.id });

    } catch (error) {
      console.error('❌ Error guardando búsqueda:', error);
      searchError = 'Error al procesar la búsqueda. Intenta nuevamente.';
    } finally {
      guardandoBusqueda = false;
    }
  }
</script>

<div class="broom-home">
  <section class="broom-home__hero">
    <img src={logoHero} alt="Broom AirLine Hero">
    <div class="broom-home__hero-overlay">
      <h1 class="broom-home__hero-title">Vuela a donde tus sueños te lleven</h1>
      <p class="broom-home__hero-subtitle">Descubre el mundo con Broom AirLine</p>
    </div>
  </section>

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
          <div class="broom-home__form-group" style="position:relative">
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
                      <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                      <span class="home-autocomplete__nombre">{a.nombre}</span>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group" style="position:relative">
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
                      <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                      <span class="home-autocomplete__nombre">{a.nombre}</span>
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
            <button type="submit" class="broom-home__search-btn" disabled={guardandoBusqueda}>
              <svg class="broom-home__search-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              {guardandoBusqueda ? 'Procesando...' : 'Buscar vuelo'}
            </button>
          </div>
        </div>

        {#if loadingFechas}
          <div class="cal-loading">Cargando disponibilidad de vuelos...</div>
        {:else if mostrarCalendarios}
          <div class="cal-wrapper">
            <div class="cal-header-info">
              {#if fechasDisponiblesIda.length > 0 || fechasDisponiblesVuelta.length > 0}
                <span class="cal-info-text">✈ Días con vuelo están marcados — selecciona tu fecha</span>
              {:else}
                <span class="cal-info-text cal-info-text--empty">No hay vuelos disponibles en esta ruta</span>
              {/if}
            </div>

            <div class="cal-dual" class:cal-dual--single={tripType === 'oneway'}>
              <!-- CALENDARIO DE IDA -->
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
                      {@const sel = departureDate === item.fecha}
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

              <!-- CALENDARIO DE VUELTA -->
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
                        <!-- Fecha posterior a ida Y con vuelos disponibles -->
                        {@const bloqPorIda = departureDate && item.fecha < departureDate}
                        {@const bloqPorNoDisponible = !esFechaDisponibleVuelta(item.fecha)}
                        {@const bloq = bloqPorIda || bloqPorNoDisponible}
                        {@const sel = returnDate === item.fecha}
                        
                        <button type="button" class="cal-day"
                          class:cal-day--disponible-vuelta={!bloq && !sel}
                          class:cal-day--seleccionado-vuelta={sel}
                          class:cal-day--bloqueado={bloq}
                          on:click={() => pickVuelta(item.fecha)}
                          disabled={bloq}
                          title={bloqPorIda ? 'Fecha anterior a la ida' : bloqPorNoDisponible ? 'Sin vuelos de regreso' : 'Vuelo disponible'}>
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

  <section class="broom-home__destinations">
    <div class="broom-home__destinations-container">
      <h2 class="broom-home__destinations-title">Destinos destacados</h2>
      <div class="broom-home__destinations-grid">
        <article class="broom-home__destination-card">
          <div class="broom-home__destination-image">
            <img src="https://th.bing.com/th/id/R.ef1269d03e8cd5b5d625bda0dca6f222?rik=67LTJJMuW8HnfQ&riu=http%3a%2f%2f1.bp.blogspot.com%2f-XCDP_qtL724%2fUQgMmYegCuI%2fAAAAAAAAjdE%2fhmq_X36t-Hs%2fs1600%2f202_1la_tour_eiffel___paris__france.jpg&ehk=V0GlPlEL31Rm9Kb0i%2fAKEYy92nYb%2bE1GUJsa3Lallnc%3d&risl=&pid=ImgRaw&r=0" alt="" class="broom-home__destination-image-visual">
          </div>
          <div class="broom-home__destination-content">
            <h3 class="broom-home__destination-name">París, Francia</h3>
            <p class="broom-home__destination-description">Descubre la ciudad del amor y sus icónicos monumentos.</p>
          </div>
        </article>
        <article class="broom-home__destination-card">
          <div class="broom-home__destination-image">
            <img src="https://hldak.mmtcdn.com/prod-s3-hld-hpcmsadmin/holidays/images/cities/1921/Tokyo-Tower.jpg" alt="" class="broom-home__destination-image-visual">
          </div>
          <div class="broom-home__destination-content">
            <h3 class="broom-home__destination-name">Tokio, Japón</h3>
            <p class="broom-home__destination-description">Experimenta la perfecta fusión entre tradición y modernidad.</p>
          </div>
        </article>
      </div>
      <div class="broom-home__destinations-actions">
        <button type="button" class="broom-home__destinations-btn" on:click={() => navigateTo('destinos-destacados')}>Ver más destinos</button>
      </div>
    </div>
  </section>

  <section class="broom-home__manage-booking">
    <div class="broom-home__manage-booking-container">
      <div class="broom-home__manage-booking-content">
        <div class="broom-home__manage-booking-image">
          <img src="https://i.pinimg.com/originals/a5/0d/05/a50d05dd4ca9116119320a244c438c19.jpg" alt="">
        </div>
        <div class="broom-home__manage-booking-info">
          <h2 class="broom-home__manage-booking-title">¿Ya tienes una reserva?</h2>
          <p class="broom-home__manage-booking-description">Administra tus vuelos, selecciona asientos, añade equipaje y mucho más.</p>
          <button type="button" class="broom-home__manage-booking-btn" on:click={() => navigateTo('reservas')}>Administrar reservas</button>
        </div>
      </div>
    </div>
  </section>
</div>