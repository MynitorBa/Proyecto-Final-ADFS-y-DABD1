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

  /**
   * Token de alianza proveniente del query param ?token= de la URL.
   * Si existe y el usuario esta logueado se valida contra el backend
   * para autocompletar pais y ciudad en el buscador.
   * @type {string|null}
   */
  export let alianzaToken = null;

  /**
   * Indica si el usuario tiene sesion activa. Se usa para decidir si
   * validar el token o redirigir al login.
   * @type {boolean}
   */
  export let isLoggedIn = false;

  /**
   * Datos de pais y ciudad ya validados por App tras login con token pendiente.
   * Cuando llegan, los campos se setean reactivamente sin llamada adicional.
   * @type {{ pais: string, ciudad: string }|null}
   */
  export let alianzaAutocompletarData = null;

  /**
   * Callback inyectado por App. Se llama cuando el token de alianza es validado
   * exitosamente para que App actualice alianzaDescuento y lo pase a SearchResults.
   * @type {Function|null}
   */
  export let onAlianzaValidada = null;

  /**
   * Callback para avisar a App que los datos de autocompletado ya fueron consumidos.
   * App limpiara alianzaAutocompletarData para que no vuelva a autocomplete al remontar.
   * @type {function|null}
   */
  export let onAlianzaAutocompletarConsumida = null;

  /**
   * Porcentaje de descuento de alianza obtenido al validar el token.
   * Se pasa a search-results en el navigateTo del handleSearch.
   * @type {number|null}
   */
  let porcentajeDescuento = null;
      import { API } from '../lib/api.js';


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

  onMount(async () => {
    const todayDate = new Date();
    const tomorrow  = new Date(todayDate);
    tomorrow.setDate(tomorrow.getDate() + 1);
    checkIn  = toLocalDateStr(todayDate);
    checkOut = toLocalDateStr(tomorrow);

    // Si llega una sugerencia de destino desde otra pagina, la pre-rellena
    if (destinationSuggestion) ciudadQuery = destinationSuggestion;

    // Logica de token de alianza
    if (alianzaToken) {
      if (!isLoggedIn) {
        sessionStorage.setItem('pendingAlianzaToken', alianzaToken);
        navigateTo('login');
        return;
      }
      await validarAlianzaToken();
    }

    // Cargar destinos disponibles para la sección explorar
    try {
      const res = await fetch(`${API}/destinos`);
      if (res.ok) {
        destinos = await res.json();
        destinosPorPais = agruparPorPais(destinos);
        paisesDestino   = Object.keys(destinosPorPais).sort();
        if (paisesDestino.length > 0) paisDestinoSeleccionado = paisesDestino[0];
      }
    } catch (_) {}
    destinosLoading = false;

    // Cargar origen del usuario si tiene sesión
    if (isLoggedIn) {
      try {
        const res = await fetch(`${API}/usuarios/perfil`, { credentials: 'include' });
        if (res.ok) {
          const perfil = await res.json();
          userPaisOrigen   = perfil.pais   || null;
          userCiudadOrigen = perfil.ciudad || null;
        }
      } catch (_) {}
    }
  });

  // Sincroniza el campo de ciudad si llega una nueva sugerencia en tiempo real
  $: if (destinationSuggestion) ciudadQuery = destinationSuggestion;

  // Cuando App ya valido el token (caso post-login), setea los campos directamente
  $: if (alianzaAutocompletarData) {
    paisSeleccionado   = { country: alianzaAutocompletarData.pais };
    paisQuery          = alianzaAutocompletarData.pais;
    ciudadQuery        = alianzaAutocompletarData.ciudad;
    ciudadSeleccionada = true;
    porcentajeDescuento = alianzaAutocompletarData.porcentajeDescuento ?? null;
    if (porcentajeDescuento) {
      sessionStorage.setItem('alianzaDescuento', String(porcentajeDescuento));
      sessionStorage.setItem('alianzaPct', String(porcentajeDescuento));
    }
    if (alianzaAutocompletarData.token) {
      sessionStorage.setItem('alianzaTokenActivo', alianzaAutocompletarData.token);
      sessionStorage.setItem('alianzaCiudad', alianzaAutocompletarData.ciudad);
      sessionStorage.setItem('alianzaPais',   alianzaAutocompletarData.pais);
    }
    if (onAlianzaValidada) onAlianzaValidada(porcentajeDescuento);
    // Avisar a App que los datos fueron consumidos para que no vuelvan a
    // autocomplete la proxima vez que Home remonte.
    if (onAlianzaAutocompletarConsumida) onAlianzaAutocompletarConsumida();
  }

  /**
   * Valida el token de alianza contra el backend. Si es valido autocompleta
   * los campos de pais y ciudad directamente en el estado sin pasar por la
   * API externa de countriesnow, evitando resets intermedios en los campos.
   * @async
   * @returns {Promise<void>}
   */
  async function validarAlianzaToken() {
    try {
      const res = await fetch(`${API}/alianza/validar?token=${alianzaToken}`, {
        method: 'GET',
        credentials: 'include'
      });
      if (res.ok) {
        const data = await res.json();
        console.log('[alianza] respuesta backend:', data);
        paisSeleccionado  = { country: data.pais };
        paisQuery         = data.pais;
        ciudadQuery       = data.ciudad;
        ciudadSeleccionada = true;
        porcentajeDescuento = data.porcentajeDescuento ?? null;
        if (porcentajeDescuento) sessionStorage.setItem('alianzaDescuento', String(porcentajeDescuento));
        // alianzaPct guarda el porcentaje original del token y nunca se borra
        // mientras el token sigue activo — permite restaurar el descuento
        // si el usuario vuelve a buscar en la ciudad correcta.
        if (porcentajeDescuento) sessionStorage.setItem('alianzaPct', String(porcentajeDescuento));
        // Guardar ciudad/país del token para validar que la búsqueda sea en esa misma ubicación
        sessionStorage.setItem('alianzaCiudad', data.ciudad);
        sessionStorage.setItem('alianzaPais',   data.pais);
        sessionStorage.setItem('alianzaTokenActivo', alianzaToken);
        if (onAlianzaValidada) onAlianzaValidada(porcentajeDescuento);
      } else {
        const err = await res.json().catch(() => ({}));
        searchError = err.mensaje || 'Token de alianza inválido o expirado.';
      }
    } catch (_) {
      searchError = 'Error al validar el token de alianza.';
    }
  }

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
      // Verificar si la busqueda es en la misma ciudad del token de alianza.
      // Si es diferente: suprimir el descuento visualmente pero CONSERVAR el token
      // para que si el usuario regresa a la ciudad correcta, el descuento vuelva.
      const alianzaCiudad = sessionStorage.getItem('alianzaCiudad');
      const alianzaPais   = sessionStorage.getItem('alianzaPais');
      let descuentoParaEstasBusqueda = porcentajeDescuento;

      if (alianzaCiudad && alianzaPais) {
        const mismaCiudad = ciudadQuery.trim().toLowerCase() === alianzaCiudad.toLowerCase();
        const mismoPais   = paisQuery.trim().toLowerCase() === alianzaPais.toLowerCase();

        if (!mismaCiudad || !mismoPais) {
          // Ciudad diferente: suprimir descuento en esta busqueda.
          // Se borra alianzaDescuento del sessionStorage para que SearchResults
          // no lo recupere como fallback, pero el token y su ciudad/pais se conservan
          // para que si el usuario regresa a la ciudad correcta, el descuento vuelva.
          descuentoParaEstasBusqueda = null;
          sessionStorage.removeItem('alianzaDescuento');
          if (onAlianzaValidada) onAlianzaValidada(null);

        } else {
          // Ciudad correcta: restaurar el descuento si habia sido suprimido.
          // alianzaPct nunca se borra mientras el token siga activo.
          const pct = porcentajeDescuento
            ?? (sessionStorage.getItem('alianzaPct') ? Number(sessionStorage.getItem('alianzaPct')) : null);
          if (pct) {
            porcentajeDescuento = pct;
            descuentoParaEstasBusqueda = pct;
            sessionStorage.setItem('alianzaDescuento', String(pct));
            if (onAlianzaValidada) onAlianzaValidada(pct);
          }
        }
      }
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
        cantidadPersonas: Number(cantidadPersonas), hotels,
        porcentajeDescuento: descuentoParaEstasBusqueda ?? null
      });
    } catch (err) {
      searchError = 'Error de conexión: ' + err.message;
    } finally {
      isSearching = false;
    }
  }

  // ── Sección de exploración de destinos ──────────────────────────────────────

  /** Lista completa de hoteles activos del sistema. @type {any[]} */
  let destinos = [];

  /** Hoteles agrupados por país destino. @type {Record<string, any[]>} */
  let destinosPorPais = {};

  /** Lista de países únicos disponibles como destino. @type {string[]} */
  let paisesDestino = [];

  /** País destino actualmente expandido en el panel. @type {string|null} */
  let paisDestinoSeleccionado = null;

  /** Hotel con detalle visible (móvil). @type {any|null} */
  let hotelDetalle = null;

  /** País de origen del usuario (de su perfil). @type {string|null} */
  let userPaisOrigen = null;

  /** Ciudad de origen del usuario (de su perfil). @type {string|null} */
  let userCiudadOrigen = null;

  /** True mientras se cargan los destinos. @type {boolean} */
  let destinosLoading = true;

  /** Referencia al elemento hero para el scroll. @type {Element|null} */
  let heroRef = null;

  /**
   * Agrupa un array de hoteles por su campo `pais`.
   * @param {any[]} lista
   * @returns {Record<string, any[]>}
   */
  function agruparPorPais(lista) {
    return lista.reduce((acc, h) => {
      (acc[h.pais] = acc[h.pais] || []).push(h);
      return acc;
    }, {});
  }

  /**
   * Alterna la expansión del panel de un país destino.
   * @param {string} pais
   */
  function seleccionarPaisDestino(pais) {
    paisDestinoSeleccionado = paisDestinoSeleccionado === pais ? null : pais;
    hotelDetalle = null;
  }

  /** True mientras se ejecuta una búsqueda directa desde un panel. @type {boolean} */
  let buscandoDirecto = false;

  /** ID del hotel cuya búsqueda directa está en curso (para mostrar spinner). @type {number|null} */
  let buscandoHotelId = null;

  /**
   * Busca directamente en el backend usando el país y ciudad del hotel
   * y navega a search-results como si el usuario hubiera llenado el formulario.
   * Usa fechas de hoy/mañana y 1 huésped como valores por defecto.
   * @param {any} hotel
   */
  async function irABuscar(hotel) {
    if (buscandoDirecto) return;
    buscandoDirecto = true;
    buscandoHotelId = hotel.id;

    const todayStr    = toLocalDateStr(new Date());
    const mananaDate  = new Date(); mananaDate.setDate(mananaDate.getDate() + 1);
    const mananaStr   = toLocalDateStr(mananaDate);

    try {
      const res = await fetch(`${API}/busqueda`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          pais:             hotel.pais,
          ciudad:           hotel.ciudad,
          fechaCheckIn:     todayStr,
          fechaCheckOut:    mananaStr,
          cantidadPersonas: 1
        })
      });

      const hotels = res.ok ? await res.json() : [];
      navigateTo('search-results', {
        pais:             hotel.pais,
        ciudad:           hotel.ciudad,
        fechaCheckIn:     todayStr,
        fechaCheckOut:    mananaStr,
        cantidadPersonas: 1,
        hotels,
        porcentajeDescuento: null
      });
    } catch (_) {
      navigateTo('search-results', {
        pais: hotel.pais, ciudad: hotel.ciudad,
        fechaCheckIn: todayStr, fechaCheckOut: mananaStr,
        cantidadPersonas: 1, hotels: [], porcentajeDescuento: null
      });
    } finally {
      buscandoDirecto = false;
      buscandoHotelId = null;
    }
  }

  /**
   * Genera las iniciales del nombre del hotel para usar como placeholder de imagen.
   * @param {string} nombre
   * @returns {string}
   */
  function iniciales(nombre) {
    return nombre.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }

  /**
   * Retorna el color de fondo del placeholder según el índice del hotel.
   * @param {number} i
   * @returns {string}
   */
  function placeholderColor(i) {
    const colores = [
      'linear-gradient(135deg,#1e3a5f,#2563eb)',
      'linear-gradient(135deg,#1e3351,#7c3aed)',
      'linear-gradient(135deg,#1e3a2e,#059669)',
      'linear-gradient(135deg,#3b2e0a,#d97706)',
      'linear-gradient(135deg,#3b1f1f,#dc2626)',
      'linear-gradient(135deg,#1e2e3a,#0891b2)',
    ];
    return colores[i % colores.length];
  }

  /**
   * Retorna el color de acento del chip de país según índice.
   * @param {number} i
   * @returns {string}
   */
  function chipAccent(i) {
    const accents = ['#2563eb','#7c3aed','#059669','#d97706','#dc2626','#0891b2'];
    return accents[i % accents.length];
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
  <section class="home__hero-section" bind:this={heroRef}>
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
                  placeholder="Escribe un país..." autocomplete="off" translate="no" />
                {#if paisLoading}
                  <div class="home__autocomplete-loading">Buscando...</div>
                {:else if paisesSugeridos.length > 0}
                  <ul class="home__autocomplete-list" translate="no">
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
                  autocomplete="off" translate="no" />
                {#if ciudadesSugeridas.length > 0}
                  <ul class="home__autocomplete-list" translate="no">
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

  <!-- Seccion de los 5 pasos del flujo de compra -->
  <section class="steps-section">
    <div class="home__container">
      <div class="home__section-header">
        <h2 class="home__section-title">Tu reserva en 5 pasos</h2>
        <p class="home__section-description">Desde la búsqueda hasta el check-out, todo en un solo lugar</p>
      </div>

      <div class="steps-track">
        <!-- Línea conectora entre pasos -->
        <div class="steps-connector" aria-hidden="true"></div>

        <!-- Paso 1: Buscar -->
        <div class="step-card">
          <div class="step-num">1</div>
          <div class="step-icon-wrap step-icon-wrap--search">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
          </div>
          <h3 class="step-title">Busca tu destino</h3>
          <p class="step-desc">Elige país, ciudad, fechas y número de huéspedes en nuestro buscador</p>
          <div class="step-badge">Buscador</div>
        </div>

        <!-- Paso 2: Seleccionar hotel -->
        <div class="step-card">
          <div class="step-num">2</div>
          <div class="step-icon-wrap step-icon-wrap--hotel">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </div>
          <h3 class="step-title">Elige tu hotel</h3>
          <p class="step-desc">Compara hoteles, fotos, amenidades y tipo de habitación — doble, suite y más</p>
          <div class="step-badge">Resultados</div>
        </div>

        <!-- Paso 3: Reservar -->
        <div class="step-card">
          <div class="step-num">3</div>
          <div class="step-icon-wrap step-icon-wrap--reserva">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
              <path d="M8 14h.01M12 14h.01M16 14h.01"/>
            </svg>
          </div>
          <h3 class="step-title">Confirma tu reserva</h3>
          <p class="step-desc">Revisa el resumen de tu estancia y confirma los detalles de la reservación</p>
          <div class="step-badge">Reserva</div>
        </div>

        <!-- Paso 4: Pagar -->
        <div class="step-card">
          <div class="step-num">4</div>
          <div class="step-icon-wrap step-icon-wrap--pago">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </div>
          <h3 class="step-title">Realiza el pago</h3>
          <p class="step-desc">Pago seguro con tarjeta. Descuentos automáticos si vienes de una aerolínea aliada</p>
          <div class="step-badge">Pago</div>
        </div>

        <!-- Paso 5: Disfrutar -->
        <div class="step-card">
          <div class="step-num">5</div>
          <div class="step-icon-wrap step-icon-wrap--enjoy">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
              <line x1="9" y1="9" x2="9.01" y2="9"/>
              <line x1="15" y1="9" x2="15.01" y2="9"/>
            </svg>
          </div>
          <h3 class="step-title">¡A disfrutar!</h3>
          <p class="step-desc">Recibe tu confirmación por correo con todos los detalles y vive la experiencia Miku Inn</p>
          <div class="step-badge step-badge--gold">¡Listo!</div>
        </div>
      </div>
    </div>
  </section>

  <!-- Seccion Explora Destinos: DESDE → HACIA con panel expandible por país -->
  <section class="destinos-section">
    <div class="home__container">

      <!-- Encabezado con indicador de origen si el usuario tiene sesión -->
      <div class="destinos-header">
        <div class="destinos-title-wrap">
          <h2 class="home__section-title" style="margin-bottom:0.4rem;">Explora nuestros destinos</h2>
          <p class="home__section-description" style="margin:0;">Selecciona un país y descubre los hoteles disponibles</p>
        </div>
        {#if userPaisOrigen}
          <div class="destinos-origen-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
              <circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/>
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"/>
            </svg>
            Desde: <strong>{userCiudadOrigen ? userCiudadOrigen + ', ' : ''}{userPaisOrigen}</strong>
          </div>
        {/if}
      </div>

      {#if destinosLoading}
        <!-- Skeleton de carga -->
        <div class="destinos-skeleton-row">
          {#each Array(5) as _}
            <div class="destinos-chip-skeleton"></div>
          {/each}
        </div>
      {:else if paisesDestino.length === 0}
        <p class="destinos-empty">No hay destinos disponibles por el momento.</p>
      {:else}

        <!-- Fila de chips de países destino -->
        <div class="destinos-chips-row" role="list">
          {#each paisesDestino as pais, i}
            <button
              type="button"
              class="destinos-chip"
              class:destinos-chip--active={paisDestinoSeleccionado === pais}
              style="--chip-accent:{chipAccent(i)}"
              on:click={() => seleccionarPaisDestino(pais)}
              role="listitem"
              aria-pressed={paisDestinoSeleccionado === pais}
            >
              <!-- Avion icono (hacia) -->
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
              </svg>
              {pais}
              <span class="destinos-chip-count">{destinosPorPais[pais].length}</span>
            </button>
          {/each}
        </div>

        <!-- Panel expandible del país seleccionado -->
        {#if paisDestinoSeleccionado && destinosPorPais[paisDestinoSeleccionado]}
          <div class="destinos-panel">

            <!-- Título del panel con flecha DESDE→HACIA -->
            <div class="destinos-panel-title">
              {#if userPaisOrigen}
                <span class="destinos-desde">{userPaisOrigen}</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
                </svg>
              {:else}
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
                </svg>
              {/if}
              <span class="destinos-hacia">{paisDestinoSeleccionado}</span>
              <span class="destinos-panel-count">
                {destinosPorPais[paisDestinoSeleccionado].length}
                {destinosPorPais[paisDestinoSeleccionado].length === 1 ? 'hotel' : 'hoteles'}
              </span>
            </div>

            <!-- Grid de tarjetas de hotel -->
            <div class="destinos-hotel-grid">
              {#each destinosPorPais[paisDestinoSeleccionado] as hotel, idx}
                <div class="destinos-hotel-card">

                  <!-- Imagen o placeholder con iniciales -->
                  <div
                    class="destinos-hotel-img"
                    style="background:{hotel.imagenesIds && hotel.imagenesIds.length > 0
                      ? 'none'
                      : placeholderColor(idx)}"
                  >
                    {#if hotel.imagenesIds && hotel.imagenesIds.length > 0}
                      <img
                        src="{API}/imagenes/{hotel.imagenesIds[0]}"
                        alt={hotel.nombre}
                        loading="lazy"
                        on:error={(e) => { e.target.style.display='none'; }}
                      />
                    {:else}
                      <span class="destinos-hotel-iniciales">{iniciales(hotel.nombre)}</span>
                    {/if}

                    <!-- Badge de rating -->
                    {#if hotel.rating > 0}
                      <div class="destinos-rating-badge">
                        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                        </svg>
                        {hotel.rating.toFixed(1)}
                      </div>
                    {/if}
                  </div>

                  <!-- Contenido de la tarjeta -->
                  <div class="destinos-hotel-body">
                    <h4 class="destinos-hotel-nombre">{hotel.nombre}</h4>
                    <div class="destinos-hotel-ciudad">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                        <circle cx="12" cy="10" r="3"/>
                      </svg>
                      {hotel.ciudad}
                    </div>
                    {#if hotel.descripcion}
                      <p class="destinos-hotel-desc">
                        {hotel.descripcion.length > 80
                          ? hotel.descripcion.slice(0, 80) + '…'
                          : hotel.descripcion}
                      </p>
                    {/if}
                    <button
                      type="button"
                      class="destinos-buscar-btn"
                      class:destinos-buscar-btn--loading={buscandoHotelId === hotel.id}
                      disabled={buscandoDirecto}
                      on:click={() => irABuscar(hotel)}
                    >
                      {#if buscandoHotelId === hotel.id}
                        <svg class="home__spin" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                          <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                        </svg>
                        Buscando...
                      {:else}
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3">
                          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                        </svg>
                        Ver disponibilidad
                      {/if}
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          </div>
        {/if}
      {/if}
    </div>
  </section>

  <!-- Seccion Hoteles mejor valorados: top hoteles por rating, búsqueda directa -->
  {#if !destinosLoading && destinos.length > 0}
  {@const topHoteles = [...destinos].filter(h => h.rating > 0).sort((a,b) => b.rating - a.rating).slice(0, 6)}
  {#if topHoteles.length > 0}
  <section class="top-section">
    <div class="home__container">
      <div class="home__section-header">
        <h2 class="home__section-title">Hoteles mejor valorados</h2>
        <p class="home__section-description">Los favoritos de nuestros huéspedes — click para ver disponibilidad</p>
      </div>

      <div class="top-hotel-grid">
        {#each topHoteles as hotel, idx}
          <button
            type="button"
            class="top-hotel-card"
            class:top-hotel-card--loading={buscandoHotelId === hotel.id}
            disabled={buscandoDirecto}
            on:click={() => irABuscar(hotel)}
            aria-label="Ver disponibilidad en {hotel.nombre}"
          >
            <!-- Imagen / placeholder -->
            <div
              class="top-hotel-img"
              style="background:{hotel.imagenesIds && hotel.imagenesIds.length > 0 ? 'none' : placeholderColor(idx)}"
            >
              {#if hotel.imagenesIds && hotel.imagenesIds.length > 0}
                <img
                  src="{API}/imagenes/{hotel.imagenesIds[0]}"
                  alt={hotel.nombre}
                  loading="lazy"
                  on:error={(e) => { e.target.style.display='none'; }}
                />
              {:else}
                <span class="destinos-hotel-iniciales" style="font-size:2rem;">{iniciales(hotel.nombre)}</span>
              {/if}

              <!-- Overlay oscuro al hover -->
              <div class="top-hotel-overlay">
                {#if buscandoHotelId === hotel.id}
                  <svg class="home__spin" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.2">
                    <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                  </svg>
                {:else}
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.2">
                    <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                  </svg>
                  <span>Ver disponibilidad</span>
                {/if}
              </div>

              <!-- Badge de posición -->
              <div class="top-hotel-pos">#{idx + 1}</div>

              <!-- Badge de rating -->
              <div class="destinos-rating-badge" style="bottom:8px;top:auto;left:8px;right:auto;">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                </svg>
                {hotel.rating.toFixed(1)}
              </div>
            </div>

            <!-- Info del hotel -->
            <div class="top-hotel-body">
              <h4 class="destinos-hotel-nombre">{hotel.nombre}</h4>
              <div class="destinos-hotel-ciudad">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                {hotel.ciudad}, {hotel.pais}
              </div>
            </div>
          </button>
        {/each}
      </div>
    </div>
  </section>
  {/if}
  {/if}

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