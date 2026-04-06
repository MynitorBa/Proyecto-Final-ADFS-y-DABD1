<script>
  /**
   * @file Home.svelte
   * @description Pagina principal de Miku Inn. Contiene el hero con el buscador
   * de hoteles por pais, ciudad, fechas y numero de huespedes. Tambien muestra
   * las caracteristicas de la cadena y una seccion CTA para invitar al usuario
   * a explorar mas destinos.
   */

  import { onMount } from 'svelte';
  import '../styles/home.css';

  import { validarFechas } from '../utils/validarFechas.js';

  /**
   * Funcion de navegacion inyectada por el router.
   * @type {Function}
   */
  export let navigateTo = (page, data = null) => {};

  /**
   * Sugerencia de destino recibida desde otra pagina para pre-rellenar el campo de ciudad.
   * @type {string|null}
   */
  export let destinationSuggestion = null;

  /** URL base del backend. @type {string} */
  const API = 'http://localhost:7000';

  /** Fecha de check-in seleccionada en el formulario. @type {string} */
  let checkIn = '';

  /** Fecha de check-out seleccionada en el formulario. @type {string} */
  let checkOut = '';

  /** Numero de huespedes seleccionado. @type {number} */
  let cantidadPersonas = 1;

  /** Indica si la busqueda de hoteles esta en progreso. @type {boolean} */
  let isSearching = false;

  /** Mensaje de error de validacion o de respuesta del servidor. @type {string} */
  let searchError = '';

  // --- Autocomplete de pais ---

  /** Texto escrito en el campo de pais. @type {string} */
  let paisQuery = '';

  /** Lista de paises sugeridos segun el texto ingresado. @type {any[]} */
  let paisesSugeridos = [];

  /** Objeto del pais seleccionado de la lista de sugerencias. @type {any|null} */
  let paisSeleccionado = null;

  /** True mientras se cargan las sugerencias de pais. @type {boolean} */
  let paisLoading = false;

  /** Timer para el debounce del autocomplete de pais. @type {any} */
  let paisTimer = null;

  // --- Autocomplete de ciudad ---

  /** Texto escrito en el campo de ciudad. @type {string} */
  let ciudadQuery = '';

  /** Lista de ciudades sugeridas filtradas por el texto escrito. @type {string[]} */
  let ciudadesSugeridas = [];

  /** True cuando el usuario selecciono una ciudad valida de la lista. @type {boolean} */
  let ciudadSeleccionada = false;

  /** True mientras se cargan las ciudades del pais seleccionado. @type {boolean} */
  let ciudadLoading = false;

  /** Lista completa de ciudades del pais seleccionado. @type {string[]} */
  let todasLasCiudades = [];

  /**
   * Convierte un objeto Date a string en formato YYYY-MM-DD usando la zona local.
   * @param {Date} date - Fecha a convertir.
   * @returns {string}
   */
  function toLocalDateStr(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  /** Fecha de hoy en formato YYYY-MM-DD, usada como minimo en el date picker. @type {string} */
  const today = toLocalDateStr(new Date());

  // Minimo de fecha permitido para el check-out (siempre al menos un dia despues del check-in).
  $: minCheckOut = (() => {
    if (!checkIn) return today;
    const d = new Date(checkIn);
    d.setDate(d.getDate() + 1);
    return toLocalDateStr(d);
  })();

  onMount(() => {
    const todayDate = new Date();
    const tomorrow  = new Date(todayDate);
    tomorrow.setDate(tomorrow.getDate() + 1);
    checkIn  = toLocalDateStr(todayDate);
    checkOut = toLocalDateStr(tomorrow);

    // Si llega una sugerencia de destino desde otra pagina, la pre-rellena
    if (destinationSuggestion) ciudadQuery = destinationSuggestion;
  });

  // Sincroniza el campo de ciudad si llega una nueva sugerencia en tiempo real
  $: if (destinationSuggestion) ciudadQuery = destinationSuggestion;

  // --- Logica del autocomplete de pais ---

  /**
   * Se ejecuta al escribir en el campo de pais. Resetea la ciudad y lanza
   * una busqueda con debounce de 300ms contra la API de countriesnow.
   */
  function onPaisInput() {
    paisSeleccionado = null;
    ciudadQuery = ''; ciudadSeleccionada = false;
    ciudadesSugeridas = []; todasLasCiudades = [];
    const q = paisQuery.trim();
    if (q.length < 2) { paisesSugeridos = []; return; }
    clearTimeout(paisTimer);
    paisTimer = setTimeout(async () => {
      paisLoading = true;
      try {
        const res  = await fetch('https://countriesnow.space/api/v0.1/countries');
        const data = await res.json();
        paisesSugeridos = (data.data || [])
          .filter(p => p.country.toLowerCase().includes(q.toLowerCase()))
          .slice(0, 6);
      } catch { paisesSugeridos = []; }
      paisLoading = false;
    }, 300);
  }

  /**
   * Confirma la seleccion de un pais y carga sus ciudades desde la API externa.
   * @async
   * @param {any} p - Objeto de pais seleccionado.
   * @returns {Promise<void>}
   */
  async function seleccionarPais(p) {
    paisSeleccionado = p;
    paisQuery = p.country;
    paisesSugeridos = [];
    ciudadQuery = ''; ciudadSeleccionada = false;
    ciudadesSugeridas = []; todasLasCiudades = [];
    ciudadLoading = true;
    try {
      const res  = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ country: p.country })
      });
      const data = await res.json();
      todasLasCiudades = data.data || [];
    } catch { todasLasCiudades = []; }
    ciudadLoading = false;
  }

  /**
   * Oculta las sugerencias de pais cuando el campo pierde el foco.
   * Si el usuario no confirmo un pais, limpia el campo.
   */
  function blurPais() {
    setTimeout(() => {
      if (paisQuery && !paisSeleccionado) { paisQuery = ''; paisesSugeridos = []; }
      else { paisesSugeridos = []; }
    }, 200);
  }

  // --- Logica del autocomplete de ciudad ---

  /**
   * Filtra la lista de ciudades del pais seleccionado segun el texto escrito.
   */
  function onCiudadInput() {
    ciudadSeleccionada = false;
    const q = ciudadQuery.toLowerCase().trim();
    ciudadesSugeridas = q.length < 2
      ? []
      : todasLasCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
  }

  /**
   * Confirma la seleccion de una ciudad de la lista de sugerencias.
   * @param {string} c - Nombre de la ciudad seleccionada.
   */
  function seleccionarCiudad(c) {
    ciudadQuery = c; ciudadSeleccionada = true;
    ciudadesSugeridas = [];
  }

  /**
   * Oculta las sugerencias de ciudad cuando el campo pierde el foco.
   * Si el usuario no confirmo una ciudad, limpia el campo.
   */
  function blurCiudad() {
    setTimeout(() => {
      if (ciudadQuery && !ciudadSeleccionada) { ciudadQuery = ''; ciudadesSugeridas = []; }
      else { ciudadesSugeridas = []; }
    }, 200);
  }

  /**
   * Maneja el submit del formulario de busqueda. Valida los campos y luego
   * hace un POST al endpoint /busqueda del backend. Navega a la pagina de
   * resultados si la busqueda es exitosa.
   * @async
   * @param {Event} e - Evento submit del formulario.
   * @returns {Promise<void>}
   */
  async function handleSearch(e) {
    e.preventDefault();
    searchError = '';

    if (!paisSeleccionado) { searchError = 'Por favor selecciona un país de la lista.'; return; }
    if (!ciudadSeleccionada) { searchError = 'Por favor selecciona una ciudad de la lista.'; return; }
    
    const validacion = validarFechas(checkIn, checkOut, today);
    if (!validacion.valido) { searchError = validacion.error; return; }

    isSearching = true;
    try {
      const res = await fetch(`${API}/busqueda`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          pais:             paisQuery.trim(),
          ciudad:           ciudadQuery.trim(),
          fechaCheckIn:     checkIn,
          fechaCheckOut:    checkOut,
          cantidadPersonas: Number(cantidadPersonas)
        })
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        searchError = err.mensaje || 'Error al buscar hoteles.';
        return;
      }

      const hotels = await res.json();
      navigateTo('search-results', {
        pais: paisQuery.trim(), ciudad: ciudadQuery.trim(),
        fechaCheckIn: checkIn, fechaCheckOut: checkOut,
        cantidadPersonas: Number(cantidadPersonas), hotels
      });
    } catch (err) {
      searchError = 'Error de conexión: ' + err.message;
    } finally {
      isSearching = false;
    }
  }

  /**
   * Caracteristicas destacadas de la cadena para la seccion de beneficios.
   * Cada item tiene un icono SVG inline, un titulo y una descripcion.
   * @type {{ icon: string, title: string, description: string }[]}
   */
  const features = [
    { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>`, title: 'Hoteles de Lujo', description: 'Experimenta comodidad y elegancia en cada una de nuestras propiedades premium' },
    { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>`, title: 'Ubicaciones Premium', description: 'Presencia en los destinos más exclusivos y demandados del mundo' },
    { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>`, title: 'Servicio Excepcional', description: 'Atención personalizada las 24 horas con personal altamente capacitado' },
    { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>`, title: 'Mejor Precio Garantizado', description: 'Las mejores tarifas directamente con nosotros, sin intermediarios' }
  ];
</script>

<!-- Pagina principal de Miku Inn -->
<div class="home-page">

  <!-- Seccion hero con imagen de fondo y el formulario de busqueda -->
  <section class="home__hero-section">
    <div class="home__hero-overlay"></div>
    <div class="home__hero-content">
      <div class="hero-text">
        <h1 class="home__hero-title">Bienvenido a Miku Inn</h1>
        <p class="hero-subtitle">Descubre experiencias únicas en nuestros hoteles alrededor del mundo</p>
      </div>

      <!-- Tarjeta del buscador de hoteles -->
      <div class="search-card">
        <h2 class="home__search-title">Encuentra tu hotel ideal</h2>

        <!-- Alerta de error de validacion o de respuesta del servidor -->
        {#if searchError}
          <div class="home__search-error">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {searchError}
          </div>
        {/if}

        <form class="search-form" on:submit={handleSearch}>
          <div class="home__form-grid">

            <!-- Campo de pais con autocomplete -->
            <div class="home__form-group">
              <label for="h-pais" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"></path></svg>
                País
              </label>
              <div class="home__autocomplete-wrap">
                <input type="text" id="h-pais" class="home__form-input"
                  bind:value={paisQuery} on:input={onPaisInput} on:blur={blurPais}
                  placeholder="Escribe un país..." autocomplete="off" />
                {#if paisLoading}
                  <div class="home__autocomplete-loading">Buscando...</div>
                {:else if paisesSugeridos.length > 0}
                  <ul class="home__autocomplete-list">
                    {#each paisesSugeridos as p}
                      <li><button type="button" class="home__autocomplete-btn" on:click={() => seleccionarPais(p)}>{p.country}</button></li>
                    {/each}
                  </ul>
                {/if}
              </div>
            </div>

            <!-- Campo de ciudad con autocomplete (depende del pais elegido) -->
            <div class="home__form-group">
              <label for="h-ciudad" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                Ciudad
                {#if ciudadLoading}
                  <span style="font-weight:400;font-size:0.75rem;opacity:0.6;">Cargando...</span>
                {/if}
              </label>
              <div class="home__autocomplete-wrap">
                <input type="text" id="h-ciudad" class="home__form-input"
                  bind:value={ciudadQuery} on:input={onCiudadInput} on:blur={blurCiudad}
                  placeholder={!paisSeleccionado ? 'Primero selecciona un país' : ciudadLoading ? 'Cargando ciudades...' : 'Escribe una ciudad...'}
                  disabled={!paisSeleccionado || ciudadLoading}
                  autocomplete="off" />
                {#if ciudadesSugeridas.length > 0}
                  <ul class="home__autocomplete-list">
                    {#each ciudadesSugeridas as c}
                      <li><button type="button" class="home__autocomplete-btn" on:click={() => seleccionarCiudad(c)}>{c}</button></li>
                    {/each}
                  </ul>
                {/if}
              </div>
            </div>

            <!-- Selector de fecha de check-in -->
            <div class="home__form-group">
              <label for="h-checkin" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                Check-in
              </label>
              <input type="date" id="h-checkin" class="home__form-input"
                bind:value={checkIn}
                min={today}
                required />
            </div>

            <!-- Selector de fecha de check-out con minimo dinamico -->
            <div class="home__form-group">
              <label for="h-checkout" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                Check-out
              </label>
              <input type="date" id="h-checkout" class="home__form-input"
                bind:value={checkOut}
                min={minCheckOut}
                required />
            </div>

            <!-- Selector de numero de huespedes -->
            <div class="home__form-group">
              <label for="h-personas" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                Huéspedes
              </label>
              <select id="h-personas" class="home__form-input" bind:value={cantidadPersonas}>
                {#each Array(10) as _, i}
                  <option value={i + 1}>{i + 1} {i === 0 ? 'Huésped' : 'Huéspedes'}</option>
                {/each}
              </select>
            </div>

          </div>

          <!-- Boton de busqueda con estado de carga -->
          <button type="submit" class="search-button" disabled={isSearching}>
            {#if isSearching}
              <svg class="home__spin" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              Buscando...
            {:else}
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
              Buscar Hoteles
            {/if}
          </button>
        </form>
      </div>
    </div>
  </section>

  <!-- Seccion de caracteristicas y beneficios de Miku Inn -->
  <section class="features-section">
    <div class="home__container">
      <div class="home__section-header">
        <h2 class="home__section-title">¿Por qué elegir Miku Inn?</h2>
        <p class="home__section-description">Experiencias excepcionales que superan expectativas</p>
      </div>
      <!-- Grid de tarjetas de caracteristicas -->
      <div class="features-grid">
        {#each features as feature}
          <div class="feature-card">
            <div class="feature-icon">{@html feature.icon}</div>
            <h3 class="feature-title">{feature.title}</h3>
            <p class="feature-description">{feature.description}</p>
          </div>
        {/each}
      </div>
    </div>
  </section>

  <!-- Seccion CTA para animar al usuario a reservar -->
  <section class="home__cta-section">
    <div class="home__container">
      <div class="home__cta-content">
        <h2 class="home__cta-title">¿Listo para tu próxima aventura?</h2>
        <p class="home__cta-description">Únete a miles de viajeros que confían en Miku Inn para sus experiencias de lujo</p>
        <div class="cta-buttons">
          <button type="button" class="home__cta-button primary" on:click={() => navigateTo('search-results', null)}>
            Explorar Hoteles
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </button>
          <a href="http://localhost:5173/destinations" class="home__cta-button secondary">
            Ver Destinos
          </a>
        </div>
      </div>
    </div>
  </section>

</div>
