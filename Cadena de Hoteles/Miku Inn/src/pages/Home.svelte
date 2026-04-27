<script>
  import { onMount } from 'svelte';
  import '../styles/home.css';
  import { validarFechas } from '../utils/validarFechas.js';

  export let navigateTo = (page, data = null) => {};
  export let destinationSuggestion = null;
  export let alianzaToken = null;
  export let isLoggedIn = false;
  export let alianzaAutocompletarData = null;
  export let onAlianzaValidada = null;
  export let onAlianzaAutocompletarConsumida = null;

  let porcentajeDescuento = null;
  import { API } from '../lib/api.js';

  let checkIn = '';
  let checkOut = '';
  let cantidadPersonas = 1;
  let isSearching = false;
  let searchError = '';

  let paisQuery = '';
  let paisesSugeridos = [];
  let paisSeleccionado = null;
  let paisLoading = false;
  let paisTimer = null;

  let ciudadQuery = '';
  let ciudadesSugeridas = [];
  let ciudadSeleccionada = false;
  let ciudadLoading = false;
  let todasLasCiudades = [];

  function toLocalDateStr(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  const today = toLocalDateStr(new Date());

  $: minCheckOut = (() => {
    if (!checkIn) return today;
    const d = new Date(checkIn);
    d.setDate(d.getDate() + 1);
    return toLocalDateStr(d);
  })();

  // ── Destinos & disponibilidad ─────────────────────────────────────────────
  let destinos = [];
  let destinosLoading = true;
  let mostrarTodosDestinos = false;
  let hotelDisponibleIds = new Set();
  let availabilityLoading = true;

  let userPaisOrigen = null;
  let userCiudadOrigen = null;
  let heroRef = null;
  let buscandoDirecto = false;
  let buscandoHotelId = null;

  $: destinosOrdenados = [...destinos].sort((a, b) => b.rating - a.rating);
  $: destinosMostrados = mostrarTodosDestinos
    ? destinosOrdenados.slice(0, 20)
    : destinosOrdenados.slice(0, 4);
  $: topHotelesDisponibles = destinosOrdenados
    .filter(h => h.rating > 0 && hotelDisponibleIds.has(h.id))
    .slice(0, 6);

  onMount(async () => {
    const todayDate = new Date();
    const tomorrow  = new Date(todayDate);
    tomorrow.setDate(tomorrow.getDate() + 1);
    checkIn  = toLocalDateStr(todayDate);
    checkOut = toLocalDateStr(tomorrow);

    if (alianzaAutocompletarData?.fechaIda)    checkIn  = alianzaAutocompletarData.fechaIda;
    if (alianzaAutocompletarData?.fechaVuelta) checkOut = alianzaAutocompletarData.fechaVuelta;

    if (destinationSuggestion) ciudadQuery = destinationSuggestion;

    if (alianzaToken) {
      if (!isLoggedIn) {
        sessionStorage.setItem('pendingAlianzaToken', alianzaToken);
        navigateTo('login');
        return;
      }
      await validarAlianzaToken();
    }

    // Cargar destinos
    try {
      const res = await fetch(`${API}/destinos`);
      if (res.ok) destinos = await res.json();
    } catch (_) {}
    destinosLoading = false;

    // Cargar perfil del usuario
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

    // Verificar disponibilidad para la sección "favoritos" (no bloqueante)
    if (destinos.length > 0) {
      const todayStr = toLocalDateStr(new Date());
      const manaDate = new Date(); manaDate.setDate(manaDate.getDate() + 1);
      const manaStr  = toLocalDateStr(manaDate);

      const ciudadesMap = new Map();
      destinos.forEach(h => {
        const key = `${h.ciudad}|||${h.pais}`;
        if (!ciudadesMap.has(key)) ciudadesMap.set(key, { ciudad: h.ciudad, pais: h.pais });
      });

      const checks = [...ciudadesMap.values()].map(({ ciudad, pais }) =>
        fetch(`${API}/busqueda?registrar=false`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ ciudad, pais, fechaCheckIn: todayStr, fechaCheckOut: manaStr, cantidadPersonas: 1 })
        }).then(r => r.ok ? r.json() : []).catch(() => [])
      );

      Promise.allSettled(checks).then(resultados => {
        const ids = new Set();
        resultados.forEach(r => {
          if (r.status === 'fulfilled' && Array.isArray(r.value))
            r.value.forEach(h => { if (h.id) ids.add(h.id); });
        });
        hotelDisponibleIds = ids;
        availabilityLoading = false;
      });
    } else {
      availabilityLoading = false;
    }
  });

  $: if (destinationSuggestion) ciudadQuery = destinationSuggestion;

  $: if (alianzaAutocompletarData) {
    const _pais        = alianzaAutocompletarData.pais;
    const _ciudad      = alianzaAutocompletarData.ciudad;
    const _pct         = alianzaAutocompletarData.porcentajeDescuento ?? null;
    const _fechaIda    = alianzaAutocompletarData.fechaIda;
    const _fechaVuelta = alianzaAutocompletarData.fechaVuelta;
    const _token       = alianzaAutocompletarData.token;

    paisSeleccionado    = { country: _pais };
    paisQuery           = _pais;
    ciudadQuery         = _ciudad;
    ciudadSeleccionada  = true;
    porcentajeDescuento = _pct;

    if (_fechaIda)    checkIn  = _fechaIda;
    if (_fechaVuelta) checkOut = _fechaVuelta;

    if (_pct) {
      sessionStorage.setItem('alianzaDescuento', String(_pct));
      sessionStorage.setItem('alianzaPct', String(_pct));
    }
    if (_token) {
      sessionStorage.setItem('alianzaTokenActivo', _token);
      sessionStorage.setItem('alianzaCiudad', _ciudad);
      sessionStorage.setItem('alianzaPais',   _pais);
    }
    if (onAlianzaValidada) onAlianzaValidada(_pct);
  }

  async function validarAlianzaToken() {
    try {
      const res = await fetch(`${API}/alianza/validar?token=${alianzaToken}`, {
        method: 'GET', credentials: 'include'
      });
      if (res.ok) {
        const data = await res.json();
        paisSeleccionado    = { country: data.pais };
        paisQuery           = data.pais;
        ciudadQuery         = data.ciudad;
        ciudadSeleccionada  = true;
        porcentajeDescuento = data.porcentajeDescuento ?? null;
        if (data.fechaIda)    checkIn  = data.fechaIda;
        if (data.fechaVuelta) checkOut = data.fechaVuelta;
        if (porcentajeDescuento) sessionStorage.setItem('alianzaDescuento', String(porcentajeDescuento));
        if (porcentajeDescuento) sessionStorage.setItem('alianzaPct', String(porcentajeDescuento));
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

  // ── Autocomplete de país ──────────────────────────────────────────────────
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

  function blurPais() {
    setTimeout(() => {
      if (paisQuery && !paisSeleccionado) { paisQuery = ''; paisesSugeridos = []; }
      else { paisesSugeridos = []; }
    }, 200);
  }

  function onCiudadInput() {
    ciudadSeleccionada = false;
    const q = ciudadQuery.toLowerCase().trim();
    ciudadesSugeridas = q.length < 2
      ? []
      : todasLasCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
  }

  function seleccionarCiudad(c) {
    ciudadQuery = c; ciudadSeleccionada = true;
    ciudadesSugeridas = [];
  }

  function blurCiudad() {
    setTimeout(() => {
      if (ciudadQuery && !ciudadSeleccionada) { ciudadQuery = ''; ciudadesSugeridas = []; }
      else { ciudadesSugeridas = []; }
    }, 200);
  }

  async function handleSearch(e) {
    e.preventDefault();
    searchError = '';

    if (!paisSeleccionado)  { searchError = 'Por favor selecciona un país de la lista.'; return; }
    if (!ciudadSeleccionada){ searchError = 'Por favor selecciona una ciudad de la lista.'; return; }

    const validacion = validarFechas(checkIn, checkOut, today);
    if (!validacion.valido) { searchError = validacion.error; return; }

    isSearching = true;
    try {
      const alianzaCiudad = sessionStorage.getItem('alianzaCiudad');
      const alianzaPais   = sessionStorage.getItem('alianzaPais');
      let descuentoParaEstasBusqueda = porcentajeDescuento;

      if (alianzaCiudad && alianzaPais) {
        const mismaCiudad = ciudadQuery.trim().toLowerCase() === alianzaCiudad.toLowerCase();
        const mismoPais   = paisQuery.trim().toLowerCase()   === alianzaPais.toLowerCase();
        if (!mismaCiudad || !mismoPais) {
          descuentoParaEstasBusqueda = null;
          sessionStorage.removeItem('alianzaDescuento');
          if (onAlianzaValidada) onAlianzaValidada(null);
        } else {
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
          pais: paisQuery.trim(), ciudad: ciudadQuery.trim(),
          fechaCheckIn: checkIn, fechaCheckOut: checkOut,
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

  async function irABuscar(hotel) {
    if (buscandoDirecto) return;
    buscandoDirecto = true;
    buscandoHotelId = hotel.id;

    const todayStr   = toLocalDateStr(new Date());
    const mananaDate = new Date(); mananaDate.setDate(mananaDate.getDate() + 1);
    const mananaStr  = toLocalDateStr(mananaDate);

    try {
      const res = await fetch(`${API}/busqueda`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          pais: hotel.pais, ciudad: hotel.ciudad,
          fechaCheckIn: todayStr, fechaCheckOut: mananaStr, cantidadPersonas: 1
        })
      });
      const hotels = res.ok ? await res.json() : [];
      navigateTo('search-results', {
        pais: hotel.pais, ciudad: hotel.ciudad,
        fechaCheckIn: todayStr, fechaCheckOut: mananaStr,
        cantidadPersonas: 1, hotels, porcentajeDescuento: null
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

  function iniciales(nombre) {
    return nombre.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }

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

  const features = [
    {
      icon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
      title: 'Hoteles de Lujo',
      description: 'Experimenta comodidad y elegancia en cada una de nuestras propiedades premium.',
      stat: '20+', statLabel: 'Propiedades', gradient: 'linear-gradient(135deg,#1e3a5f,#2563eb)', glow: 'rgba(37,99,235,0.35)'
    },
    {
      icon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`,
      title: 'Destinos Exclusivos',
      description: 'Presencia en los destinos más exclusivos y demandados del mundo.',
      stat: '15+', statLabel: 'Destinos', gradient: 'linear-gradient(135deg,#1e3351,#7c3aed)', glow: 'rgba(124,58,237,0.35)'
    },
    {
      icon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
      title: 'Servicio 24/7',
      description: 'Atención personalizada a toda hora con personal altamente capacitado.',
      stat: '24/7', statLabel: 'Disponible', gradient: 'linear-gradient(135deg,#1e3a2e,#059669)', glow: 'rgba(5,150,105,0.35)'
    },
    {
      icon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
      title: 'Mejor Precio',
      description: 'Las mejores tarifas directamente con nosotros, sin intermediarios ni cargos ocultos.',
      stat: '100%', statLabel: 'Garantía', gradient: 'linear-gradient(135deg,#3b2e0a,#d97706)', glow: 'rgba(217,119,6,0.35)'
    }
  ];
</script>

<div class="home-page">

  <!-- ── Hero ──────────────────────────────────────────────────────────────── -->
  <section class="home__hero-section" bind:this={heroRef}>
    <div class="home__hero-overlay"></div>
    <div class="home__hero-content">
      <div class="hero-text">
        <h1 class="home__hero-title">Bienvenido a Miku Inn</h1>
        <p class="hero-subtitle">Descubre experiencias únicas en nuestros hoteles alrededor del mundo</p>
      </div>

      <div class="search-card">
        <h2 class="home__search-title">Encuentra tu hotel ideal</h2>

        {#if searchError}
          <div class="home__search-error">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {searchError}
          </div>
        {/if}

        <form class="search-form" on:submit={handleSearch}>
          <div class="home__form-grid">

            <div class="home__form-group">
              <label for="h-pais" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"/></svg>
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

            <div class="home__form-group">
              <label for="h-ciudad" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                Ciudad
                {#if ciudadLoading}<span style="font-weight:400;font-size:0.75rem;opacity:0.6;">Cargando...</span>{/if}
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

            <div class="home__form-group">
              <label for="h-checkin" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                Check-in
              </label>
              <input type="date" id="h-checkin" class="home__form-input" bind:value={checkIn} min={today} required />
            </div>

            <div class="home__form-group">
              <label for="h-checkout" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                Check-out
              </label>
              <input type="date" id="h-checkout" class="home__form-input" bind:value={checkOut} min={minCheckOut} required />
            </div>

            <div class="home__form-group">
              <label for="h-personas" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                Huéspedes
              </label>
              <select id="h-personas" class="home__form-input" bind:value={cantidadPersonas}>
                {#each Array(10) as _, i}
                  <option value={i + 1}>{i + 1} {i === 0 ? 'Huésped' : 'Huéspedes'}</option>
                {/each}
              </select>
            </div>
          </div>

          <button type="submit" class="search-button" disabled={isSearching}>
            {#if isSearching}
              <svg class="home__spin" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              Buscando...
            {:else}
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              Buscar Hoteles
            {/if}
          </button>
        </form>
      </div>
    </div>
  </section>

  <!-- ── Sección 1: Tu reserva en 5 pasos ─────────────────────────────────── -->
  <section class="steps-section">
    <div class="steps-section__bg-glow"></div>
    <div class="home__container">
      <div class="home__section-header">
        <div class="steps-section__pill">Proceso simple y rápido</div>
        <h2 class="home__section-title steps-section__title">Tu reserva en 5 pasos</h2>
        <p class="home__section-description steps-section__desc">Desde la búsqueda hasta el check-out, todo en un solo lugar</p>
      </div>

      <div class="steps-track">
        <div class="steps-connector" aria-hidden="true"></div>

        <div class="step-card">
          <div class="step-num">1</div>
          <div class="step-icon-wrap step-icon-wrap--search">
            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
          </div>
          <h3 class="step-title">Busca tu destino</h3>
          <p class="step-desc">Elige país, ciudad, fechas y número de huéspedes en nuestro buscador inteligente</p>
          <div class="step-badge">Buscador</div>
        </div>

        <div class="step-card">
          <div class="step-num">2</div>
          <div class="step-icon-wrap step-icon-wrap--hotel">
            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </div>
          <h3 class="step-title">Elige tu hotel</h3>
          <p class="step-desc">Compara hoteles, fotos, amenidades y tipo de habitación — doble, suite y más</p>
          <div class="step-badge">Resultados</div>
        </div>

        <div class="step-card">
          <div class="step-num">3</div>
          <div class="step-icon-wrap step-icon-wrap--reserva">
            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
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

        <div class="step-card">
          <div class="step-num">4</div>
          <div class="step-icon-wrap step-icon-wrap--pago">
            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </div>
          <h3 class="step-title">Realiza el pago</h3>
          <p class="step-desc">Pago seguro con tarjeta. Descuentos automáticos si vienes de una aerolínea aliada</p>
          <div class="step-badge">Pago seguro</div>
        </div>

        <div class="step-card">
          <div class="step-num">5</div>
          <div class="step-icon-wrap step-icon-wrap--enjoy">
            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
              <line x1="9" y1="9" x2="9.01" y2="9"/>
              <line x1="15" y1="9" x2="15.01" y2="9"/>
            </svg>
          </div>
          <h3 class="step-title">¡A disfrutar!</h3>
          <p class="step-desc">Recibe tu confirmación por correo y vive la experiencia Miku Inn</p>
          <div class="step-badge step-badge--gold">¡Listo!</div>
        </div>
      </div>
    </div>
  </section>

  <!-- ── Sección 2: Destinos populares ────────────────────────────────────── -->
  <section class="destinos-section">
    <div class="home__container">
      <div class="home__section-header">
        <div class="steps-section__pill" style="background:rgba(212,175,55,0.1);border-color:rgba(212,175,55,0.3);color:#D4AF37;">Más reservados</div>
        <h2 class="home__section-title destinos-section__title">Destinos más populares</h2>
        <p class="home__section-description destinos-section__desc">
          Los hoteles donde más viajeros eligen quedarse
          {#if userPaisOrigen}
            <span class="destinos-origen-inline">
              · Desde <strong>{userCiudadOrigen ? userCiudadOrigen + ', ' : ''}{userPaisOrigen}</strong>
            </span>
          {/if}
        </p>
      </div>

      {#if destinosLoading}
        <div class="destinos-flat-grid">
          {#each Array(4) as _}
            <div class="destino-card-skeleton"></div>
          {/each}
        </div>
      {:else if destinos.length === 0}
        <p class="destinos-empty">No hay destinos disponibles por el momento.</p>
      {:else}
        <div class="destinos-flat-grid">
          {#each destinosMostrados as hotel, idx}
            <div class="destino-card">
              <div
                class="destino-card-img"
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
                  <span class="destinos-hotel-iniciales">{iniciales(hotel.nombre)}</span>
                {/if}

                {#if hotel.rating > 0}
                  <div class="destino-card-rating">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="#D4AF37">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                    {hotel.rating.toFixed(1)}
                  </div>
                {/if}

                <div class="destino-card-overlay">
                  <div class="destino-card-location-overlay">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                    {hotel.ciudad}, {hotel.pais}
                  </div>
                </div>
              </div>

              <div class="destino-card-body">
                <h4 class="destino-card-nombre">{hotel.nombre}</h4>
                {#if hotel.descripcion}
                  <p class="destino-card-desc">
                    {hotel.descripcion.length > 75 ? hotel.descripcion.slice(0, 75) + '…' : hotel.descripcion}
                  </p>
                {/if}
                <button
                  type="button"
                  class="destino-card-btn"
                  class:destino-card-btn--loading={buscandoHotelId === hotel.id}
                  disabled={buscandoDirecto}
                  on:click={() => irABuscar(hotel)}
                >
                  {#if buscandoHotelId === hotel.id}
                    <svg class="home__spin" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                    </svg>
                    Buscando...
                  {:else}
                    Ver disponibilidad
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3">
                      <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
                    </svg>
                  {/if}
                </button>
              </div>
            </div>
          {/each}
        </div>

        {#if !mostrarTodosDestinos && destinosOrdenados.length > 4}
          <div class="destinos-ver-mas-wrap">
            <button type="button" class="destinos-ver-mas-btn" on:click={() => mostrarTodosDestinos = true}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              Ver más destinos · {Math.min(destinosOrdenados.length, 20)} hoteles
            </button>
          </div>
        {/if}
      {/if}
    </div>
  </section>

  <!-- ── Sección 3: Los favoritos de nuestros huéspedes ───────────────────── -->
  {#if !destinosLoading}
  <section class="top-section">
    <div class="home__container">
      <div class="home__section-header">
        <div class="steps-section__pill" style="background:rgba(102,126,234,0.1);border-color:rgba(102,126,234,0.3);color:#818cf8;">Disponibles hoy</div>
        <h2 class="home__section-title destinos-section__title">Los favoritos de nuestros huéspedes</h2>
        <p class="home__section-description destinos-section__desc">Hoteles mejor valorados con disponibilidad para hoy</p>
      </div>

      {#if availabilityLoading}
        <div class="top-hotel-grid">
          {#each Array(6) as _}
            <div class="top-card-skeleton"></div>
          {/each}
        </div>
      {:else if topHotelesDisponibles.length === 0}
        <div class="top-empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#475569" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><path d="M8 15s1.5-2 4-2 4 2 4 2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/>
          </svg>
          <p>No hay hoteles disponibles para hoy. <button type="button" class="top-empty-link" on:click={() => window.scrollTo({top:0, behavior:'smooth'})}>Usa el buscador</button> para otras fechas.</p>
        </div>
      {:else}
        <div class="top-hotel-grid">
          {#each topHotelesDisponibles as hotel, idx}
            <button
              type="button"
              class="top-hotel-card"
              class:top-hotel-card--loading={buscandoHotelId === hotel.id}
              disabled={buscandoDirecto}
              on:click={() => irABuscar(hotel)}
              aria-label="Ver disponibilidad en {hotel.nombre}"
            >
              <div
                class="top-hotel-img"
                style="background:{hotel.imagenesIds && hotel.imagenesIds.length > 0 ? 'none' : placeholderColor(idx)}"
              >
                {#if hotel.imagenesIds && hotel.imagenesIds.length > 0}
                  <img src="{API}/imagenes/{hotel.imagenesIds[0]}" alt={hotel.nombre} loading="lazy"
                    on:error={(e) => { e.target.style.display='none'; }} />
                {:else}
                  <span class="destinos-hotel-iniciales" style="font-size:2rem;">{iniciales(hotel.nombre)}</span>
                {/if}

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

                <div class="top-hotel-pos">#{idx + 1}</div>

                <div class="destinos-rating-badge" style="bottom:8px;top:auto;left:8px;right:auto;">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  {hotel.rating.toFixed(1)}
                </div>

                <div class="top-disponible-badge">Disponible</div>
              </div>

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
      {/if}
    </div>
  </section>
  {/if}

  <!-- ── Sección 4: ¿Por qué elegir Miku Inn? ─────────────────────────────── -->
  <section class="features-section">
    <div class="features-section__glow"></div>
    <div class="home__container">
      <div class="home__section-header">
        <div class="steps-section__pill" style="background:rgba(212,175,55,0.1);border-color:rgba(212,175,55,0.25);color:#D4AF37;">Nuestras ventajas</div>
        <h2 class="home__section-title features-section__title">¿Por qué elegir Miku Inn?</h2>
        <p class="home__section-description features-section__desc">Experiencias excepcionales que superan todas las expectativas</p>
      </div>

      <div class="features-grid">
        {#each features as feature, i}
          <div class="feature-card">
            <div class="feature-stat-row">
              <span class="feature-stat">{feature.stat}</span>
              <span class="feature-stat-label">{feature.statLabel}</span>
            </div>
            <div class="feature-icon-wrap" style="background:{feature.gradient};box-shadow:0 8px 28px {feature.glow}">
              {@html feature.icon}
            </div>
            <h3 class="feature-title">{feature.title}</h3>
            <p class="feature-description">{feature.description}</p>
          </div>
        {/each}
      </div>
    </div>
  </section>

  <!-- ── Sección 5: CTA ────────────────────────────────────────────────────── -->
  <section class="home__cta-section">
    <div class="home__cta-bg"></div>
    <div class="home__container">
      <div class="home__cta-content">
        <div class="home__cta-badge-row">
          <span class="home__cta-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            +1,000 viajeros satisfechos
          </span>
          <span class="home__cta-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            Pago 100% seguro
          </span>
        </div>

        <h2 class="home__cta-title">¿Listo para tu próxima aventura?</h2>
        <p class="home__cta-description">Reserva hoy en Miku Inn y vive una experiencia de lujo única — con el mejor precio garantizado</p>

        <div class="cta-buttons">
          <button type="button" class="home__cta-button primary"
            on:click={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            Buscar mi hotel
          </button>
          {#if !isLoggedIn}
            <button type="button" class="home__cta-button secondary" on:click={() => navigateTo('register')}>
              Crear cuenta gratis
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            </button>
          {:else}
            <button type="button" class="home__cta-button secondary" on:click={() => navigateTo('reservations')}>
              Ver mis reservas
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            </button>
          {/if}
        </div>
      </div>
    </div>
  </section>

</div>
