<script>
  /**
   * @file SearchResults.svelte
   * @description Pagina de resultados de busqueda de hoteles. Permite refinar
   * la busqueda (pais, ciudad, fechas, huespedes), filtrar los resultados por
   * precio, tipo de habitacion y amenidades, y ordenarlos por precio o calificacion.
   * Tambien calcula y muestra distintas opciones de precio (directa, combinada,
   * aproximada y con persona extra) para el numero de huespedes indicado.
   */

  import { onMount } from 'svelte';

  /**
   * Parametros de la busqueda recibidos del componente padre.
   * @type {{ pais?:string, ciudad?:string, fechaCheckIn?:string, fechaCheckOut?:string, cantidadPersonas?:number, hotels?:any[], porcentajeDescuento?:number|null } | null}
   */
  export let searchParams = null;

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo;

  /**
   * Porcentaje de descuento de alianza pasado directamente desde App.svelte.
   * Es la fuente de verdad principal. searchParams es fallback secundario.
   * @type {number|null}
   */
  export let alianzaDescuento = null;

  import '../styles/searchresults.css';

  /** URL base de la API del backend. @type {string} */
      import { API } from '../lib/api.js';


  /** Indica si hay una carga en proceso (skeleton). @type {boolean} */
  let isLoading    = false;

  /** Indica si se esta realizando una nueva busqueda. @type {boolean} */
  let isSearching  = false;

  /** Modo de vista de la lista de hoteles: 'list' o 'grid'. @type {string} */
  let viewMode     = 'list';

  /** Mensaje de error de validacion en el formulario de busqueda. @type {string} */
  let searchError  = '';

  /** Indica si ya se realizo al menos una busqueda y hay resultados que mostrar. @type {boolean} */
  let searchDone = !!(searchParams && Array.isArray(searchParams.hotels));

  /** Fecha de check-in seleccionada. @type {string} */
  let fechaCheckIn     = (searchParams && searchParams.fechaCheckIn)     ? searchParams.fechaCheckIn     : '';

  /** Fecha de check-out seleccionada. @type {string} */
  let fechaCheckOut    = (searchParams && searchParams.fechaCheckOut)    ? searchParams.fechaCheckOut    : '';

  /** Cantidad de personas para la busqueda. @type {number} */
  let cantidadPersonas = (searchParams && searchParams.cantidadPersonas) ? searchParams.cantidadPersonas : 1;

  /** Hoteles crudos devueltos por la API antes de aplicar filtros. @type {any[]} */
  let hotelsRaw        = (searchParams && Array.isArray(searchParams.hotels)) ? searchParams.hotels      : [];

  /**
   * Porcentaje de descuento de alianza recibido desde el flujo de token.
   * Fuente 1: alianzaDescuento prop (App.svelte, mas confiable).
   * Fuente 2: searchParams (Home a navigateTo).
   * Fuente 3: sessionStorage (fallback definitivo).
   * @type {number|null}
   */
  let porcentajeDescuento = null;

  // Reactivo: se recalcula cada vez que cambia alianzaDescuento prop o searchParams
  $: porcentajeDescuento = (() => {
    if (alianzaDescuento) return alianzaDescuento;
    if (searchParams && searchParams.porcentajeDescuento) return searchParams.porcentajeDescuento;
    const stored = sessionStorage.getItem('alianzaDescuento');
    return stored ? Number(stored) : null;
  })();

  /**
   * Convierte un objeto Date a cadena YYYY-MM-DD en la zona horaria local.
   * @param {Date} date - Fecha a convertir.
   * @returns {string}
   */
  function toLocalDateStr(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  /** Fecha minima permitida para check-in (hoy). @type {string} */
  const today = toLocalDateStr(new Date());

  // Fecha minima para check-out (al menos un dia despues del check-in)
  $: minCheckOut = (() => {
    if (!fechaCheckIn) return today;
    const d = new Date(fechaCheckIn);
    d.setDate(d.getDate() + 1);
    return toLocalDateStr(d);
  })();

  /** Texto del buscador de pais. @type {string} */
  let paisQuery = (searchParams && searchParams.pais) ? searchParams.pais : '';

  /** Sugerencias filtradas para el autocomplete de pais. @type {any[]} */
  let paisesSugeridos = [];

  /** Objeto del pais seleccionado del autocomplete. @type {any} */
  let paisSeleccionado = (searchParams && searchParams.pais) ? { country: searchParams.pais } : null;

  /** Indica si se esta cargando la lista de paises. @type {boolean} */
  let paisLoading = false;

  /** Temporizador de debounce para la busqueda de paises. @type {any} */
  let paisTimer = null;

  /** Texto del buscador de ciudad. @type {string} */
  let ciudadQuery = (searchParams && searchParams.ciudad) ? searchParams.ciudad : '';

  /** Sugerencias filtradas para el autocomplete de ciudad. @type {string[]} */
  let ciudadesSugeridas = [];

  /** Indica si una ciudad fue seleccionada formalmente del autocomplete. @type {boolean} */
  let ciudadSeleccionada = !!(searchParams && searchParams.ciudad);

  /** Indica si se estan cargando las ciudades del pais seleccionado. @type {boolean} */
  let ciudadLoading = false;

  /** Lista completa de ciudades disponibles para el pais seleccionado. @type {string[]} */
  let todasLasCiudades = [];

  /**
   * Si venimos con un pais preseleccionado, cargamos sus ciudades al montar.
   */
  onMount(async () => {
    if (paisSeleccionado) {
      ciudadLoading = true;
      try {
        const res = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ country: paisSeleccionado.country })
        });
        const data = await res.json();
        todasLasCiudades = data.data || [];
      } catch { todasLasCiudades = []; }
      ciudadLoading = false;
    }
  });

  /**
   * Maneja la escritura en el campo de pais. Resetea la ciudad y lanza
   * una busqueda con debounce de 300ms.
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
   * Confirma la seleccion de un pais y carga las ciudades correspondientes.
   * @async
   * @param {any} p - Pais seleccionado.
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
      const res = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
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
   * Limpia el campo de pais al perder el foco si el usuario no selecciono ninguno.
   */
  function blurPais() {
    setTimeout(() => {
      if (paisQuery && !paisSeleccionado) { paisQuery = ''; paisesSugeridos = []; }
      else { paisesSugeridos = []; }
    }, 200);
  }

  /**
   * Filtra las ciudades del pais seleccionado segun lo que escribe el usuario.
   */
  function onCiudadInput() {
    ciudadSeleccionada = false;
    const q = ciudadQuery.toLowerCase().trim();
    ciudadesSugeridas = q.length < 2
      ? []
      : todasLasCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
  }

  /**
   * Confirma la seleccion de una ciudad del autocomplete.
   * @param {string} c - Nombre de la ciudad seleccionada.
   */
  function seleccionarCiudad(c) {
    ciudadQuery = c; ciudadSeleccionada = true;
    ciudadesSugeridas = [];
  }

  /**
   * Limpia el campo de ciudad al perder el foco si el usuario no selecciono ninguna.
   */
  function blurCiudad() {
    setTimeout(() => {
      if (ciudadQuery && !ciudadSeleccionada) { ciudadQuery = ''; ciudadesSugeridas = []; }
      else { ciudadesSugeridas = []; }
    }, 200);
  }

  /**
   * Estado de los filtros aplicados a los resultados.
   * @type {{ priceMin: number, priceMax: number, tiposHab: string[], amenidades: string[], sortBy: string }}
   */
  let filters = {
    priceMin:    0,
    priceMax:    0,
    tiposHab:    [],
    amenidades:  [],
    sortBy:      'recommended'
  };

  // Numero de noches calculado de forma reactiva
  $: nights = (fechaCheckIn && fechaCheckOut)
    ? Math.max(1, Math.ceil((Number(new Date(fechaCheckOut)) - Number(new Date(fechaCheckIn))) / 86400000))
    : 1;

  // Lista de amenidades unicas de todos los hoteles para el panel de filtros
  $: allAmenidades = (() => {
    const set = new Set();
    hotelsRaw.forEach(h => h.amenidades?.forEach(a => set.add(a.nombre)));
    return [...set].sort();
  })();

  // Lista de tipos de habitacion unicos para el panel de filtros
  $: allTiposHab = (() => {
    const set = new Set();
    hotelsRaw.forEach(h => h.tiposHabitacion?.forEach(r => set.add(r.tipoHabitacion)));
    return [...set].sort();
  })();

  // Hoteles filtrados y ordenados segun los filtros activos
  $: filteredHotels = filterAndSort(hotelsRaw, filters);

  /**
   * Obtiene el precio minimo por noche entre todos los tipos de habitacion del hotel
   * que pueden acomodar exactamente a los huespedes sin extras.
   * @param {any} hotel - Objeto hotel.
   * @returns {number|null}
   */
  function getMinPrice(hotel) {
    if (!hotel.tiposHabitacion || hotel.tiposHabitacion.length === 0) return null;
    return Math.min(...hotel.tiposHabitacion.map(r => r.precioPorNoche));
  }

  /**
   * Obtiene la combinacion exacta de habitaciones sugerida por el backend
   * para cubrir al numero de huespedes con multiples cuartos.
   * @param {any} hotel - Objeto hotel.
   * @returns {any[]|null}
   */
  function getComboHabs(hotel) {
    if (!hotel.combinacionesNumericas?.length) return null;
    const combo = hotel.combinacionesNumericas[0];
    if (combo.length <= 1) return null;
    const usados = {};
    const result = [];
    for (const cap of combo) {
      const key   = String(cap);
      const rooms = hotel.tiposHabitacionPorCapacidad?.[key];
      if (!rooms?.length) return null;
      const idx = usados[key] ?? 0;
      if (idx >= rooms.length) return null;
      result.push({ tipo: rooms[idx].tipoHabitacion, precio: rooms[idx].precioPorNoche, cap });
      usados[key] = idx + 1;
    }
    return result;
  }

  /**
   * Cuando no hay combinacion exacta ni habitacion directa, calcula una opcion
   * aproximada usando las habitaciones de mayor capacidad disponibles.
   * @param {any} hotel - Objeto hotel.
   * @param {number} personas - Numero de huespedes.
   * @returns {{ habs: any[], capacidadTotal: number, esAproximado: boolean }|null}
   */
  function getComboAproximado(hotel, personas) {
    const tieneDirecta = hotel.tiposHabitacion && hotel.tiposHabitacion.length > 0;
    const tieneCombo   = !!getComboHabs(hotel);
    if (tieneDirecta || tieneCombo) return null;

    const porCapacidad = hotel.tiposHabitacionPorCapacidad;
    if (!porCapacidad || Object.keys(porCapacidad).length === 0) return null;

    const todasHabs = [];
    for (const [capStr, rooms] of Object.entries(porCapacidad)) {
      const cap = Number(capStr);
      for (const room of rooms) {
        todasHabs.push({ tipo: room.tipoHabitacion, precio: room.precioPorNoche, cap });
      }
    }

    todasHabs.sort((a, b) => b.cap - a.cap);

    let sumCap   = 0;
    const selec  = [];
    const limite = personas + 2;

    for (const hab of todasHabs) {
      if (sumCap >= personas) break;
      selec.push(hab);
      sumCap += hab.cap;
    }

    if (sumCap < personas || sumCap > limite) return null;
    if (selec.length <= 1) return null;

    return { habs: selec, capacidadTotal: sumCap, esAproximado: true };
  }

  /**
   * Calcula la opcion de habitacion para (personas - 1) mas una persona extra,
   * cuando no cabe exactamente el grupo en ninguna habitacion disponible.
   * @param {any} hotel - Objeto hotel.
   * @param {number} personas - Numero de huespedes.
   * @returns {{ tipo: string, precioPorNoche: number, precioPorPersona: number, cap: number, total: number }|null}
   */
  function getPersonaExtraMin(hotel, personas) {
    if (personas <= 1) return null;
    const porCap = hotel.tiposHabitacionPorCapacidad;
    if (!porCap) return null;
    const target = String(personas - 1);
    const rooms = porCap[target];
    if (!rooms?.length) return null;
    const best = rooms.reduce((min, r) =>
      (r.precioPorNoche + r.precioPorPersona) < (min.precioPorNoche + min.precioPorPersona) ? r : min
    , rooms[0]);
    return { tipo: best.tipoHabitacion, precioPorNoche: best.precioPorNoche, precioPorPersona: best.precioPorPersona, cap: personas - 1, total: best.precioPorNoche + best.precioPorPersona };
  }

  /**
   * Suma los precios por noche de un array de habitaciones de combinacion.
   * @param {any[]} habs - Array de habitaciones con campo `precio`.
   * @returns {number}
   */
  function sumPrecios(habs) {
    return habs.reduce((s, h) => s + h.precio, 0);
  }

  /**
   * Obtiene el precio minimo "desde" del hotel considerando todas las opciones
   * disponibles (directa, combinada, aproximada, con extra).
   * @param {any} hotel - Objeto hotel.
   * @returns {number|null}
   */
  function getDesde(hotel) {
    const directo = getMinPrice(hotel);
    const combo   = getComboHabs(hotel);
    const aprox   = getComboAproximado(hotel, cantidadPersonas);
    const extra   = getPersonaExtraMin(hotel, cantidadPersonas);

    const precios = [];
    if (directo !== null)  precios.push(directo);
    if (combo   !== null)  precios.push(sumPrecios(combo));
    if (aprox   !== null)  precios.push(sumPrecios(aprox.habs));
    if (extra   !== null)  precios.push(extra.total);

    return precios.length ? Math.min(...precios) : null;
  }

  /**
   * Filtra y ordena el array de hoteles segun los filtros activos.
   * Excluye hoteles sin ninguna opcion disponible para el grupo.
   * @param {any[]} hotels - Hoteles crudos.
   * @param {typeof filters} f - Objeto de filtros activo.
   * @returns {any[]}
   */
  function filterAndSort(hotels, f) {
    return hotels
      .filter(h => {
        const tieneDirecta = h.tiposHabitacion && h.tiposHabitacion.length > 0;
        const tieneCombo   = !!getComboHabs(h);
        const tieneAprox   = !!getComboAproximado(h, cantidadPersonas);
        const tieneExtra   = !!getPersonaExtraMin(h, cantidadPersonas);
        if (!tieneDirecta && !tieneCombo && !tieneAprox && !tieneExtra) return false;

        const desde  = getDesde(h);
        const priceOk = desde === null || (desde >= f.priceMin && (f.priceMax === 0 || desde <= f.priceMax));

        const tipoOk = f.tiposHab.length === 0 ||
          h.tiposHabitacion?.some(r => f.tiposHab.includes(r.tipoHabitacion));

        const amenOk = f.amenidades.length === 0 ||
          f.amenidades.every(a => h.amenidades?.some(am => am.nombre === a));

        return priceOk && tipoOk && amenOk;
      })
      .sort((a, b) => {
        if (f.sortBy === 'price-low')  return (getDesde(a) ?? 0) - (getDesde(b) ?? 0);
        if (f.sortBy === 'price-high') return (getDesde(b) ?? 0) - (getDesde(a) ?? 0);
        if (f.sortBy === 'rating')     return (b.rating ?? 0) - (a.rating ?? 0);
        return (b.rating ?? 0) - (a.rating ?? 0);
      });
  }

  /**
   * Agrega o quita un valor de un array de filtros y fuerza reactividad.
   * @param {string[]} arr - Array de filtros a modificar.
   * @param {string} val - Valor a agregar o quitar.
   */
  function toggleArr(arr, val) {
    const i = arr.indexOf(val);
    i > -1 ? arr.splice(i, 1) : arr.push(val);
    filters = { ...filters };
  }

  /**
   * Reinicia todos los filtros a sus valores por defecto.
   */
  function resetFilters() {
    filters = { priceMin: 0, priceMax: 0, tiposHab: [], amenidades: [], sortBy: 'recommended' };
  }

  /**
   * Maneja el reenvio del formulario de busqueda con los nuevos parametros.
   * Valida los campos, llama al endpoint y actualiza los resultados.
   * @async
   * @param {Event} e - Evento de submit del formulario.
   * @returns {Promise<void>}
   */
  async function handleReSearch(e) {
    e.preventDefault();
    searchError = '';

    if (!paisSeleccionado) { searchError = 'Por favor selecciona un país de la lista.'; return; }
    if (!ciudadSeleccionada) { searchError = 'Por favor selecciona una ciudad de la lista.'; return; }
    if (!fechaCheckIn) { searchError = 'Selecciona la fecha de check-in.'; return; }
    if (!fechaCheckOut) { searchError = 'Selecciona la fecha de check-out.'; return; }
    if (new Date(fechaCheckOut) <= new Date(fechaCheckIn)) { searchError = 'El check-out debe ser al menos un día después del check-in.'; return; }
    if (fechaCheckIn < today) { searchError = 'El check-in no puede ser una fecha pasada.'; return; }

    isSearching = true;
    try {
      const res = await fetch(`${API}/busqueda`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          pais:             paisQuery.trim(),
          ciudad:           ciudadQuery.trim(),
          fechaCheckIn,
          fechaCheckOut,
          cantidadPersonas: Number(cantidadPersonas)
        })
      });
      if (!res.ok) { searchError = 'Error al buscar.'; return; }
      hotelsRaw  = await res.json();
      searchDone = true;
      resetFilters();
    } catch(err) {
      searchError = 'Error de conexión: ' + err.message;
    } finally {
      isSearching = false;
    }
  }

  /**
   * Formatea un numero como moneda USD.
   * @param {number} p - Valor a formatear.
   * @returns {string}
   */
  const fmt = p => new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD', minimumFractionDigits: 0 }).format(p);

  /**
   * Aplica el porcentaje de descuento de alianza a un precio dado.
   * Devuelve null si no hay descuento activo.
   * @param {number} p - Precio original.
   * @returns {number|null}
   */
  function precioD(p) {
    if (!porcentajeDescuento) return null;
    return Math.round(p * (1 - porcentajeDescuento / 100) * 100) / 100;
  }

  /**
   * Opciones disponibles para el selector de ordenamiento.
   * @type {{ id: string, label: string }[]}
   */
  const SORTS = [
    { id: 'recommended', label: 'Recomendado' },
    { id: 'price-low',   label: 'Precio: Menor' },
    { id: 'price-high',  label: 'Precio: Mayor' },
    { id: 'rating',      label: 'Mejor Valorado' }
  ];

  /**
   * Devuelve el path SVG del icono correspondiente a una amenidad por nombre.
   * @param {string} nombre - Nombre de la amenidad.
   * @returns {string} Path SVG.
   */
  function amenidadIcon(nombre) {
    const n = nombre.toLowerCase();
    if (n.includes('wifi'))           return 'M5 12.55a11 11 0 0 1 14.08 0M1.42 9a16 16 0 0 1 21.16 0M8.53 16.11a6 6 0 0 1 6.95 0M12 20h.01';
    if (n.includes('piscina'))        return 'M2 12h20M2 17h20M2 7h20';
    if (n.includes('gimnasio'))       return 'M6.5 6.5h11M18 12H6M6.5 17.5h11';
    if (n.includes('estacionamiento'))return 'M5 17H3a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2h-3';
    if (n.includes('restaurante'))    return 'M3 11l19-9-9 19-2-8-8-2z';
    if (n.includes('spa'))            return 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z';
    if (n.includes('bar'))            return 'M8 22h8M7 10h10l-1 7H8L7 10zM5 10l2-7h10l2 7';
    if (n.includes('desayuno'))       return 'M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z';
    return 'M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 21 12 17.77 5.82 21 7 14.14 2 9.27l6.91-1.01L12 2z';
  }
</script>

<div class="sr-page">
  <div class="sr-container">

    <!-- Barra de modificacion de busqueda con autocomplete de pais y ciudad -->
    <div class="sr-modify-bar">
      <div class="sr-modify-content">
        <form class="sr-modify-form" on:submit={handleReSearch}>
          <div class="sr-form-fields">

            <!-- Autocomplete de pais con debounce -->
            <div class="sr-field-group sr-field-group--ac">
              <label for="sr-pais">Pais</label>
              <div class="sr-ac-wrap">
                <input id="sr-pais" type="text"
                  bind:value={paisQuery}
                  on:input={onPaisInput}
                  on:blur={blurPais}
                  placeholder="Escribe un país..."
                  autocomplete="off" />
                {#if paisLoading}
                  <div class="sr-ac-loading">Buscando...</div>
                {:else if paisesSugeridos.length > 0}
                  <ul class="sr-ac-list">
                    {#each paisesSugeridos as p}
                      <li><button type="button" class="sr-ac-btn" on:mousedown|preventDefault={() => seleccionarPais(p)}>{p.country}</button></li>
                    {/each}
                  </ul>
                {/if}
              </div>
            </div>

            <!-- Autocomplete de ciudad (dependiente del pais seleccionado) -->
            <div class="sr-field-group sr-field-group--ac">
              <label for="sr-ciudad">
                Ciudad
                {#if ciudadLoading}
                  <span class="sr-ac-hint">Cargando...</span>
                {/if}
              </label>
              <div class="sr-ac-wrap">
                <input id="sr-ciudad" type="text"
                  bind:value={ciudadQuery}
                  on:input={onCiudadInput}
                  on:blur={blurCiudad}
                  placeholder={!paisSeleccionado ? 'Primero selecciona un país' : ciudadLoading ? 'Cargando ciudades...' : 'Escribe una ciudad...'}
                  disabled={!paisSeleccionado || ciudadLoading}
                  autocomplete="off" />
                {#if ciudadesSugeridas.length > 0}
                  <ul class="sr-ac-list">
                    {#each ciudadesSugeridas as c}
                      <li><button type="button" class="sr-ac-btn" on:mousedown|preventDefault={() => seleccionarCiudad(c)}>{c}</button></li>
                    {/each}
                  </ul>
                {/if}
              </div>
            </div>

            <div class="sr-field-group">
              <label for="sr-checkin">Check-in</label>
              <input
                id="sr-checkin"
                type="date"
                bind:value={fechaCheckIn}
                min={today}
                required />
            </div>

            <div class="sr-field-group">
              <label for="sr-checkout">Check-out</label>
              <input
                id="sr-checkout"
                type="date"
                bind:value={fechaCheckOut}
                min={minCheckOut}
                required />
            </div>

            <div class="sr-field-group">
              <label for="sr-personas">Huéspedes</label>
              <input id="sr-personas" type="number" bind:value={cantidadPersonas} min="1" placeholder="Nº huéspedes" />
            </div>
          </div>

          {#if searchError}
            <p class="sr-error">{searchError}</p>
          {/if}

          <button type="submit" class="btn-modify" disabled={isSearching}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            {isSearching ? 'Buscando...' : 'Buscar'}
          </button>
        </form>
      </div>
    </div>

    <!-- Banner de descuento de alianza -->
    {#if porcentajeDescuento}
      <div style="background:linear-gradient(135deg,#064e3b,#059669 55%,#34d399);border-radius:16px;padding:1.25rem 2rem;margin:1.5rem 0;display:flex;align-items:center;gap:1.5rem;position:relative;overflow:hidden;box-shadow:0 8px 28px rgba(16,185,129,.38);color:white;">
        <!-- Burbujas decorativas de fondo -->
        <div style="position:absolute;right:-28px;top:-28px;width:140px;height:140px;border-radius:50%;background:rgba(255,255,255,.07);pointer-events:none;"></div>
        <div style="position:absolute;right:60px;bottom:-38px;width:100px;height:100px;border-radius:50%;background:rgba(255,255,255,.05);pointer-events:none;"></div>
        <!-- Semicirculos que simulan el corte de un ticket -->
        <div style="position:absolute;left:86px;top:-15px;width:30px;height:30px;border-radius:50%;background:#f8fafc;pointer-events:none;"></div>
        <div style="position:absolute;left:86px;bottom:-15px;width:30px;height:30px;border-radius:50%;background:#f8fafc;pointer-events:none;"></div>
        <!-- Icono de etiqueta / tag con label "Alianza" -->
        <div style="display:flex;flex-direction:column;align-items:center;gap:.3rem;min-width:70px;padding-right:1.25rem;border-right:2px dashed rgba(255,255,255,.35);flex-shrink:0;position:relative;z-index:1;">
          <svg width="38" height="38" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.5" aria-hidden="true">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <circle cx="7" cy="7" r="1.3" fill="white" stroke="none"/>
          </svg>
          <span style="font-size:.6rem;font-weight:800;text-transform:uppercase;letter-spacing:.6px;opacity:.9;">Alianza</span>
        </div>
        <!-- Porcentaje y descripcion -->
        <div style="flex:1;min-width:0;position:relative;z-index:1;">
          <div style="display:flex;align-items:baseline;gap:.5rem;flex-wrap:wrap;line-height:1.1;">
            <span style="font-size:2.75rem;font-weight:900;">{porcentajeDescuento}%</span>
            <span style="font-size:1.15rem;font-weight:700;opacity:.95;">de descuento especial</span>
          </div>
          <p style="margin:.3rem 0 0;font-size:.83rem;opacity:.85;">Precio preferencial por alianza · Se aplica automáticamente en tu reservación</p>
        </div>
        <!-- Checkmark decorativo a la derecha -->
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none" style="opacity:.18;flex-shrink:0;" aria-hidden="true">
          <circle cx="32" cy="32" r="29" stroke="white" stroke-width="2.5" stroke-dasharray="7 4"/>
          <path d="M19 32l9 10 17-19" stroke="white" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    {/if}

    <!-- Encabezado y resultados: solo se muestran si ya hubo una busqueda -->
    {#if searchDone}
      <div class="sr-header">
        <div>
          <h1>{ciudadQuery}{paisQuery ? ', ' + paisQuery : ''}: {filteredHotels.length} hotel{filteredHotels.length !== 1 ? 'es' : ''} encontrado{filteredHotels.length !== 1 ? 's' : ''}</h1>
          <p class="sr-subtitle">{nights} {nights === 1 ? 'noche' : 'noches'} · {cantidadPersonas} {cantidadPersonas === 1 ? 'persona' : 'personas'}</p>
        </div>
        <!-- Toggle entre vista de lista y grilla -->
        <div class="sr-actions">
          <div class="view-toggle">
            <button class="vbtn" class:active={viewMode === 'list'} on:click={() => viewMode = 'list'} title="Lista">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
            </button>
            <button class="vbtn" class:active={viewMode === 'grid'} on:click={() => viewMode = 'grid'} title="Cuadricula">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
            </button>
          </div>
        </div>
      </div>

      <div class="sr-layout">

        <!-- Panel lateral de filtros -->
        <aside class="sr-filters">
          <div class="sr-filters-hdr">
            <h2>Filtrar por:</h2>
            <button class="btn-reset" on:click={resetFilters}>Limpiar</button>
          </div>

          <!-- Filtro de rango de precio por noche -->
          <div class="filter-group">
            <h3 class="filter-title">Precio por noche</h3>
            <div class="price-row">
              <label for="pmin">Min
                <div class="price-inp-wrap">
                  <span>$</span>
                  <input id="pmin" type="number" bind:value={filters.priceMin} min="0" />
                </div>
              </label>
              <span>—</span>
              <label for="pmax">Max
                <div class="price-inp-wrap">
                  <span>$</span>
                  <input id="pmax" type="number" bind:value={filters.priceMax} min="0" />
                </div>
              </label>
            </div>
            <div class="price-display">${filters.priceMin || '0'} — {filters.priceMax ? '$' + filters.priceMax : 'Sin límite'} / noche</div>
          </div>

          <!-- Filtro por tipo de habitacion -->
          {#if allTiposHab.length > 0}
            <div class="filter-group">
              <h3 class="filter-title">Tipo de habitacion</h3>
              {#each allTiposHab as tipo}
                <label class="chk-label">
                  <input type="checkbox" checked={filters.tiposHab.includes(tipo)} on:change={() => toggleArr(filters.tiposHab, tipo)} />
                  <span>{tipo}</span>
                </label>
              {/each}
            </div>
          {/if}

          <!-- Filtro por amenidades disponibles -->
          {#if allAmenidades.length > 0}
            <div class="filter-group">
              <h3 class="filter-title">Servicios y amenidades</h3>
              {#each allAmenidades as amen}
                <label class="chk-label">
                  <input type="checkbox" checked={filters.amenidades.includes(amen)} on:change={() => toggleArr(filters.amenidades, amen)} />
                  <span>{amen}</span>
                </label>
              {/each}
            </div>
          {/if}
        </aside>

        <main class="sr-main">

          <!-- Barra de ordenamiento -->
          <div class="sort-bar">
            <span class="sort-lbl">Ordenar:</span>
            {#each SORTS as s}
              <button class="sort-btn" class:active={filters.sortBy === s.id} on:click={() => filters.sortBy = s.id}>{s.label}</button>
            {/each}
          </div>

          <!-- Skeleton de carga mientras se busca -->
          {#if isLoading || isSearching}
            {#each Array(3) as _}
              <div class="hotel-card skeleton">
                <div class="sk-img"></div>
                <div class="sk-body">
                  <div class="sk-line"></div>
                  <div class="sk-line short"></div>
                  <div class="sk-line medium"></div>
                </div>
              </div>
            {/each}

          {:else if hotelsRaw.length === 0}
            <!-- Estado vacio: ningun hotel encontrado para la busqueda -->
            <div class="no-results">
              <div class="no-results-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              </div>
              <h2>No encontramos hoteles</h2>
              <p>Intenta con otro país, ciudad o fechas diferentes.</p>
              <button class="btn-primary" on:click={() => navigateTo('home')}>Nueva búsqueda</button>
            </div>

          {:else if filteredHotels.length === 0}
            <!-- Estado vacio: filtros demasiado restrictivos -->
            <div class="no-results">
              <div class="no-results-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              </div>
              <h2>No hay hoteles con esos filtros</h2>
              <p>Intenta ajustar los filtros de la izquierda</p>
              <button class="btn-primary" on:click={resetFilters}>Limpiar filtros</button>
            </div>

          {:else}
            <!-- Lista/grilla de tarjetas de hotel con todas sus opciones de precio -->
            <div class="hotels-grid" class:list-view={viewMode === 'list'} class:grid-view={viewMode === 'grid'}>
              {#each filteredHotels as hotel (hotel.id)}
                {@const minPrice  = getMinPrice(hotel)}
                {@const comboHabs = getComboHabs(hotel)}
                {@const comboAprox = getComboAproximado(hotel, cantidadPersonas)}
                {@const comboTotal = comboHabs ? sumPrecios(comboHabs) : null}
                {@const aproxTotal = comboAprox ? sumPrecios(comboAprox.habs) : null}
                {@const extraInfo  = getPersonaExtraMin(hotel, cantidadPersonas)}
                {@const desde     = getDesde(hotel)}

                <!-- Tarjeta clickeable: navega a hotel-detail pasando porcentajeDescuento -->
                <div class="hotel-card"
                  role="button"
                  tabindex="0"
                  on:click={() => navigateTo('hotel-detail', { hotel, cantidadPersonas, fechaCheckIn, fechaCheckOut, porcentajeDescuento })}
                  on:keydown={e => e.key === 'Enter' && navigateTo('hotel-detail', { hotel, cantidadPersonas, fechaCheckIn, fechaCheckOut, porcentajeDescuento })}>

                  <!-- Galeria / imagen principal del hotel -->
                  <div class="hotel-gallery">
                    {#if hotel.imagenesIds && hotel.imagenesIds.length > 0}
                      <img
                        src="{API}/imagenes/hotel/{hotel.imagenesIds[0]}"
                        alt={hotel.nombre}
                        class="hotel-img-real"
                        on:error={e => {
                          e.currentTarget.style.display = 'none';
                          e.currentTarget.nextElementSibling.style.display = 'flex';
                        }}
                      />
                      <div class="hotel-img-placeholder" style="display:none">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                        <p class="img-count">Sin imagenes aun</p>
                      </div>
                    {:else}
                      <div class="hotel-img-placeholder">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                        <p class="img-count">Sin imagenes aun</p>
                      </div>
                    {/if}
                    {#if hotel.estado === 'Activo'}
                      <div class="hotel-estado-badge hotel-estado-badge--activo">Disponible</div>
                    {/if}
                    {#if comboAprox}
                      <div class="hotel-estado-badge hotel-estado-badge--aprox" style="left:auto;right:0.75rem;">
                        Opción cercana · {comboAprox.capacidadTotal} pers.
                      </div>
                    {/if}
                  </div>

                  <!-- Contenido textual de la tarjeta -->
                  <div class="hotel-content">
                    <div class="hotel-hdr">
                      <div class="hotel-title-wrap">
                        <h2 class="hotel-name">{hotel.nombre}</h2>
                        <div class="hotel-loc">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                          {hotel.direccion}
                        </div>
                      </div>
                      {#if hotel.rating}
                        <div class="rating-box">
                          <div class="rating-score">{hotel.rating.toFixed(1)}</div>
                          <div class="rating-text">{hotel.rating >= 4.8 ? 'Extraordinario' : hotel.rating >= 4.5 ? 'Fabuloso' : hotel.rating >= 4 ? 'Muy bueno' : 'Bueno'}</div>
                        </div>
                      {/if}
                    </div>

                    <p class="hotel-desc">{hotel.descripcion}</p>

                    <!-- Amenidades como pills con icono -->
                    {#if hotel.amenidades && hotel.amenidades.length > 0}
                      <div class="amenities-row">
                        {#each hotel.amenidades.slice(0, 5) as am}
                          <span class="amenity-pill">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d={amenidadIcon(am.nombre)}/></svg>
                            {am.nombre}
                          </span>
                        {/each}
                        {#if hotel.amenidades.length > 5}
                          <span class="amenity-more">+{hotel.amenidades.length - 5} mas</span>
                        {/if}
                      </div>
                    {/if}

                    <!-- Preview de tipos de habitacion disponibles -->
                    {#if hotel.tiposHabitacion && hotel.tiposHabitacion.length > 0}
                      <div class="habitaciones-preview">
                        <p class="habitaciones-label">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M2 4v16M22 4v16M2 8h20M2 16h20M6 8v8M10 8v8M14 8v8M18 8v8"/></svg>
                          {hotel.tiposHabitacion.length} tipo{hotel.tiposHabitacion.length !== 1 ? 's' : ''} de habitacion disponible{hotel.tiposHabitacion.length !== 1 ? 's' : ''}
                        </p>
                        <div class="hab-chips">
                          {#each hotel.tiposHabitacion.slice(0, 3) as hab}
                            <span class="hab-chip">
                              {hab.tipoHabitacion}
                              <span class="hab-chip-price">{fmt(hab.precioPorNoche)}/noche</span>
                            </span>
                          {/each}
                          {#if hotel.tiposHabitacion.length > 3}
                            <span class="hab-chip hab-chip--more">+{hotel.tiposHabitacion.length - 3} mas</span>
                          {/if}
                        </div>
                      </div>
                    {/if}

                    <!-- Footer con opciones de precio y boton de detalle -->
                    <div class="hotel-footer">
                      <div class="pricing">

                        {#if desde !== null}
                          <div class="desde-badge">
                            <span class="desde-lbl">Desde</span>
                            {#if precioD(desde) !== null}
                              <span class="desde-precio" style="text-decoration:line-through;opacity:.45;font-size:1rem;">{fmt(desde)}</span>
                              <span class="desde-precio" style="color:#059669;">{fmt(precioD(desde))}</span>
                            {:else}
                              <span class="desde-precio">{fmt(desde)}</span>
                            {/if}
                            <span class="desde-sub">/ noche · {fmt((precioD(desde) ?? desde) * nights)} por {nights} noche{nights !== 1 ? 's' : ''}</span>
                          </div>
                        {/if}

                        <div class="price-boxes">

                          <!-- Opcion de habitacion directa (exactamente la capacidad solicitada) -->
                          {#if minPrice !== null}
                            <div class="price-box">
                              <div class="price-box-label">Habitación directa</div>
                              <div class="curr-price">
                                {#if precioD(minPrice) !== null}
                                  <span class="price-amount" style="text-decoration:line-through;opacity:.4;font-size:1.1rem;">{fmt(minPrice)}</span>
                                  <span class="price-amount" style="color:#059669;">{fmt(precioD(minPrice))}</span>
                                {:else}
                                  <span class="price-amount">{fmt(minPrice)}</span>
                                {/if}
                                <span class="price-lbl">/ noche</span>
                              </div>
                              <div class="per-night">{fmt((precioD(minPrice) ?? minPrice) * nights)} por {nights} noche{nights !== 1 ? 's' : ''}</div>
                            </div>
                          {/if}

                          {#if minPrice !== null && comboHabs}
                            <div class="price-box-divider">ó</div>
                          {/if}

                          <!-- Opcion de combinacion exacta de habitaciones -->
                          {#if comboHabs}
                            <div class="price-box price-box--combo">
                              <div class="price-box-label">Combinación de habitaciones</div>
                              {#each comboHabs as hab, i}
                                <div class="combo-hab-row">
                                  <span class="combo-hab-name">Hab.{i + 1} · {hab.tipo} <span class="combo-cap">({hab.cap} pers.)</span></span>
                                  <span class="combo-hab-price">{fmt(hab.precio)}<span class="price-lbl">/noche</span></span>
                                </div>
                              {/each}
                              <div class="combo-total">
                                {#if precioD(comboTotal) !== null}
                                  <span style="text-decoration:line-through;opacity:.45;">{fmt(comboTotal)}/noche</span>
                                  → <strong style="color:#059669;">{fmt(precioD(comboTotal))}/noche</strong>
                                  · {fmt(precioD(comboTotal) * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                                {:else}
                                  Total: <strong>{fmt(comboTotal)}/noche</strong>
                                  · {fmt(comboTotal * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                                {/if}
                              </div>
                            </div>
                          {/if}

                          <!-- Opcion aproximada cuando no hay combinacion exacta -->
                          {#if comboAprox}
                            <div class="price-box price-box--aprox">
                              <div class="price-box-label">
                                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="vertical-align:-1px;margin-right:3px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                                Opción cercana — {comboAprox.capacidadTotal} de {cantidadPersonas} pers.
                              </div>
                              {#each comboAprox.habs as hab, i}
                                <div class="combo-hab-row">
                                  <span class="combo-hab-name">Hab.{i + 1} · {hab.tipo} <span class="combo-cap">({hab.cap} pers.)</span></span>
                                  <span class="combo-hab-price">{fmt(hab.precio)}<span class="price-lbl">/noche</span></span>
                                </div>
                              {/each}
                              <div class="combo-total">
                                {#if precioD(aproxTotal) !== null}
                                  <span style="text-decoration:line-through;opacity:.45;">{fmt(aproxTotal)}/noche</span>
                                  → <strong style="color:#059669;">{fmt(precioD(aproxTotal))}/noche</strong>
                                  · {fmt(precioD(aproxTotal) * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                                {:else}
                                  Total: <strong>{fmt(aproxTotal)}/noche</strong>
                                  · {fmt(aproxTotal * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                                {/if}
                              </div>
                            </div>
                          {/if}

                          <!-- Opcion de habitacion mas persona extra -->
                          {#if extraInfo}
                            {#if (minPrice !== null || comboHabs || comboAprox)}
                              <div class="price-box-divider">ó</div>
                            {/if}
                            <div class="price-box price-box--combo">
                              <div class="price-box-label">Habitación + 1 persona extra</div>
                              <div class="combo-hab-row">
                                <span class="combo-hab-name">{extraInfo.tipo} <span class="combo-cap">(cap. {extraInfo.cap} +1 extra)</span></span>
                                <span class="combo-hab-price">{fmt(extraInfo.precioPorNoche)}<span class="price-lbl">/noche</span></span>
                              </div>
                              <div class="combo-hab-row">
                                <span class="combo-hab-name" style="color: var(--primary);">+1 persona extra</span>
                                <span class="combo-hab-price" style="color: var(--primary);">+{fmt(extraInfo.precioPorPersona)}<span class="price-lbl">/noche</span></span>
                              </div>
                              <div class="combo-total">
                                {#if precioD(extraInfo.total) !== null}
                                  <span style="text-decoration:line-through;opacity:.45;">{fmt(extraInfo.total)}/noche</span>
                                  → <strong style="color:#059669;">{fmt(precioD(extraInfo.total))}/noche</strong>
                                  · {fmt(precioD(extraInfo.total) * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                                {:else}
                                  Total: <strong>{fmt(extraInfo.total)}/noche</strong>
                                  · {fmt(extraInfo.total * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                                {/if}
                              </div>
                            </div>
                          {/if}

                          {#if minPrice === null && !comboHabs && !comboAprox && !extraInfo}
                            <div class="price-box">
                              <div class="price-box-label">Precio a consultar</div>
                            </div>
                          {/if}

                        </div>

                        <!-- Boton "Ver disponibilidad": tambien pasa porcentajeDescuento -->
                        <button class="btn-view" on:click|stopPropagation={() => navigateTo('hotel-detail', { hotel, cantidadPersonas, fechaCheckIn, fechaCheckOut, porcentajeDescuento })}>
                          Ver disponibilidad
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                        </button>
                      </div>
                    </div>

                  </div>
                </div>
              {/each}
            </div>
          {/if}

        </main>
      </div>
    {/if}

  </div>
</div>