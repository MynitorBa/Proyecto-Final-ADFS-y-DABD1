<script>
  /**
   * @file Destinos.svelte
   * @description Pagina de exploracion de destinos disponibles. Permite al usuario
   * buscar habitaciones (individuales, combinaciones exactas o aproximadas) en todos
   * los destinos del catalogo, filtrando por cantidad de personas y fechas. Al seleccionar
   * un resultado, navega al detalle del hotel correspondiente.
   */

  // @ts-nocheck
  export let navigateTo;
  import '../styles/destinos.css';

  /** URL base del backend. @type {string} */
      import { API } from '../lib/api.js';


  /** Indica si hay una busqueda en progreso. @type {boolean} */
  let loading = false;

  /** Mensaje de error si la busqueda falla. @type {string} */
  let error = '';

  /** Numero de personas confirmado para la busqueda activa. @type {number} */
  let personas = 1;

  /** Valor del input de personas (no reactivo hasta que se ejecuta la busqueda). @type {number} */
  let personasInput = 1;

  /** True cuando ya se realizo al menos una busqueda. @type {boolean} */
  let buscado = false;

  /** Lista de items a mostrar (habitaciones directas, combinaciones o aproximaciones). @type {any[]} */
  let resultados = [];

  /** Limite maximo de resultados para no saturar la UI. @type {number} */
  const MAX_RESULTS = 15;

  /** Fecha de check-in seleccionada por el usuario. @type {string} */
  let checkIn = getFutureDate(1);

  /** Fecha de check-out seleccionada por el usuario. @type {string} */
  let checkOut = getFutureDate(2);

  /** Fecha minima seleccionable (hoy). @type {string} */
  const minDate = new Date().toISOString().split('T')[0];

  /** Fecha maxima seleccionable (100 anos en el futuro). @type {string} */
  const maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 100)).toISOString().split('T')[0];

  /**
   * Calcula una fecha futura en formato YYYY-MM-DD.
   * @param {number} daysFromNow - Dias a sumar desde hoy.
   * @returns {string}
   */
  function getFutureDate(daysFromNow) {
    const d = new Date();
    d.setDate(d.getDate() + daysFromNow);
    return d.toISOString().split('T')[0];
  }

  /**
   * Ejecuta la busqueda de habitaciones disponibles en todos los destinos.
   * Sigue estos pasos:
   *  1. Obtiene la lista de destinos del API para extraer ciudades unicas.
   *  2. Lanza busquedas en paralelo por cada ciudad.
   *  3. Construye items de tipo directa, combinacion y aproximada.
   *  4. Deduplica y limita los resultados a MAX_RESULTS.
   * @async
   * @returns {Promise<void>}
   */
  async function buscar() {
    personas = Number(personasInput) || 1;
    loading = true;
    error   = '';
    buscado = true;

    try {
      // 1. Traer lista de destinos para saber ciudades
      let hotelesBasicos = [];
      let res = await fetch(`${API}/destinos`, { credentials: 'include' });
      if (res.ok) {
        hotelesBasicos = await res.json();
      } else {
        throw new Error('No se pudieron cargar los destinos');
      }

      if (hotelesBasicos.length === 0) {
        resultados = [];
        return;
      }

      // 2. Ciudades unicas
      const ciudades = new Map();
      for (const h of hotelesBasicos) {
        const key = `${h.ciudad}|||${h.pais}`;
        if (!ciudades.has(key)) {
          ciudades.set(key, { ciudad: h.ciudad, pais: h.pais });
        }
      }

      // 3. POST /busqueda por cada ciudad.
      //    Registrar UNA sola entrada para trazar la accion del usuario (primera ciudad),
      //    el resto se consulta con ?registrar=false para no inflar las metricas.
      const ciudadesArr = Array.from(ciudades.values());
      const promesas = ciudadesArr.map(async ({ ciudad, pais }, idx) => {
        const registrar = idx === 0; // solo la primera ciudad registra la busqueda
        try {
          const url = registrar ? `${API}/busqueda` : `${API}/busqueda?registrar=false`;
          const r = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
              pais,
              ciudad,
              fechaCheckIn: checkIn,
              fechaCheckOut: checkOut,
              cantidadPersonas: personas
            })
          });
          if (r.ok) return await r.json();
          return [];
        } catch (_) { return []; }
      });

      const busquedas = await Promise.all(promesas);
      const todosHoteles = busquedas.flat();

      // 4. Construir items: habitaciones directas + combinaciones
      const items = [];

      for (const hotel of todosHoteles) {
        // A) Habitaciones directas (capacidad >= personas)
        if (hotel.tiposHabitacion && hotel.tiposHabitacion.length > 0) {
          for (const hab of hotel.tiposHabitacion) {
            items.push({
              tipo: 'directa',
              hotel,
              habitacion: hab,
              label: hab.tipoHabitacion,
              descripcion: hab.descripcion,
              tipoCama: hab.tipoCama,
              metros: hab.metrosCuadrados,
              capacidad: hab.capacidadMaxima,
              precioPorNoche: hab.precioPorNoche,
              precioPorPersona: hab.precioPorPersona,
              imagenesIds: hab.imagenesIds || [],
            });
          }
        }

        // B) Combinaciones del backend
        const combNums = hotel.combinacionesNumericas || [];
        if (combNums.length > 0 && hotel.tiposHabitacionPorCapacidad) {
          for (const combo of combNums) {
            if (combo.length <= 1) continue; // solo combos de 2+ habs

            const habsCombo = [];
            const usados = {};
            let valido = true;

            for (const cap of combo) {
              const key = String(cap);
              const rooms = hotel.tiposHabitacionPorCapacidad[key];
              if (!rooms || rooms.length === 0) { valido = false; break; }
              const idx = usados[key] ?? 0;
              if (idx >= rooms.length) { valido = false; break; }
              habsCombo.push(rooms[idx]);
              usados[key] = idx + 1;
            }

            if (!valido) continue;

            const totalNoche   = habsCombo.reduce((s, h) => s + h.precioPorNoche, 0);
            const totalPersona = habsCombo.reduce((s, h) => s + h.precioPorPersona, 0);
            const capTotal     = combo.reduce((s, c) => s + c, 0);

            items.push({
              tipo: 'combinacion',
              hotel,
              habitaciones: habsCombo,
              combo,
              label: `Combinación: ${habsCombo.map(h => h.tipoHabitacion).join(' + ')}`,
              descripcion: `${habsCombo.length} habitaciones combinadas para ${capTotal} personas`,
              capacidad: capTotal,
              precioPorNoche: totalNoche,
              precioPorPersona: totalPersona,
            });
          }
        }

        // C) Combo aproximado (si no hay directa ni combo exacto)
        const tieneDirecta = hotel.tiposHabitacion && hotel.tiposHabitacion.length > 0;
        const tieneCombo   = combNums.length > 0;
        if (!tieneDirecta && !tieneCombo && hotel.tiposHabitacionPorCapacidad) {
          const porCap    = hotel.tiposHabitacionPorCapacidad;
          const todasHabs = [];
          for (const [capStr, rooms] of Object.entries(porCap)) {
            const cap = Number(capStr);
            for (const room of rooms) {
              todasHabs.push({ ...room, cap });
            }
          }
          todasHabs.sort((a, b) => b.cap - a.cap);

          let sumCap = 0;
          const selec = [];
          for (const hab of todasHabs) {
            if (sumCap >= personas) break;
            selec.push(hab);
            sumCap += hab.cap;
          }

          if (sumCap >= personas && sumCap <= personas + 2 && selec.length > 1) {
            const totalNoche   = selec.reduce((s, h) => s + h.precioPorNoche, 0);
            const totalPersona = selec.reduce((s, h) => s + h.precioPorPersona, 0);

            items.push({
              tipo: 'aproximada',
              hotel,
              habitaciones: selec,
              combo: selec.map(h => h.cap),
              label: `Opción cercana: ${selec.map(h => h.tipoHabitacion).join(' + ')}`,
              descripcion: `${selec.length} habitaciones · capacidad ${sumCap} personas (aprox.)`,
              capacidad: sumCap,
              precioPorNoche: totalNoche,
              precioPorPersona: totalPersona,
            });
          }
        }
      }

      // 5. Deduplicar y limitar
      const seen = new Set();
      resultados = items.filter(item => {
        let key;
        if (item.tipo === 'directa') {
          key = `d-${item.habitacion.tipoHabitacionId}`;
        } else {
          key = `c-${item.hotel.id}-${(item.habitaciones || []).map(h => h.tipoHabitacionId).join(',')}`;
        }
        if (seen.has(key)) return false;
        seen.add(key);
        return true;
      }).slice(0, MAX_RESULTS);

    } catch (e) {
      error = e.message || 'Error al buscar';
    } finally {
      loading = false;
    }
  }

  // Busqueda automatica al montar el componente con 1 persona por defecto
  buscar();

  /**
   * Navega al detalle del hotel seleccionado pasando las fechas y personas actuales.
   * @param {any} hotel - Objeto del hotel a visitar.
   */
  function goToHotel(hotel) {
    navigateTo('hotel-detail', {
      hotel,
      cantidadPersonas: personas,
      fechaCheckIn: checkIn,
      fechaCheckOut: checkOut
    });
  }

  /**
   * Formatea un numero como moneda USD sin decimales.
   * @param {number} p - Valor a formatear.
   * @returns {string}
   */
  const fmt = p => new Intl.NumberFormat('es-GT', {
    style: 'currency', currency: 'USD', minimumFractionDigits: 0
  }).format(p);

  /**
   * Devuelve la URL de la imagen principal de un item de resultado.
   * Soporta habitaciones directas y combinaciones.
   * @param {any} item - Item de resultado.
   * @returns {string|null}
   */
  function roomImage(item) {
    if (item.tipo === 'directa' && item.imagenesIds && item.imagenesIds.length > 0) {
      return `${API}/imagenes/habitacion/${item.imagenesIds[0]}`;
    }
    if (item.tipo !== 'directa' && item.habitaciones && item.habitaciones.length > 0) {
      const first = item.habitaciones[0];
      if (first.imagenesIds && first.imagenesIds.length > 0) {
        return `${API}/imagenes/habitacion/${first.imagenesIds[0]}`;
      }
    }
    return null;
  }

  /**
   * Oculta una imagen rota en lugar de mostrar el icono de imagen rota del navegador.
   * @param {Event} e - Evento de error del elemento img.
   */
  function handleImgError(e) {
    e.target.style.display = 'none';
  }

  /**
   * Previene el comportamiento por defecto del form y ejecuta la busqueda.
   * @param {Event} e - Evento submit del formulario.
   */
  function handleSubmit(e) {
    e.preventDefault();
    buscar();
  }
</script>

<!-- Pagina principal de destinos disponibles -->
<div class="destinos-page">

  <!-- Seccion hero con buscador de personas y fechas -->
  <div class="destinos-hero">
    <div class="destinos-hero__overlay"></div>
    <div class="destinos-hero__content">
      <h1 class="destinos-hero__title">Destinos Disponibles</h1>
      <p class="destinos-hero__subtitle">Encuentra habitaciones disponibles en todos nuestros destinos</p>

      <!-- Formulario de busqueda por numero de personas y fechas -->
      <form class="destinos-search-form" on:submit={handleSubmit}>

        <!-- Campo de personas -->
        <div class="destinos-search-field">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          <input
            type="number"
            bind:value={personasInput}
            min="1"
            max="20"
            class="destinos-search-input"
          />
          <span class="destinos-search-label">{personasInput == 1 ? 'persona' : 'personas'}</span>
        </div>

        <!-- Campo de fecha de entrada -->
        <div class="destinos-search-field">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          <input
            type="date"
            bind:value={checkIn}
            min={minDate}
            max={maxDate}
            class="destinos-search-input"
          />
          <span class="destinos-search-label">entrada</span>
        </div>

        <!-- Campo de fecha de salida -->
        <div class="destinos-search-field">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          <input
            type="date"
            bind:value={checkOut}
            min={minDate}
            max={maxDate}
            class="destinos-search-input"
          />
          <span class="destinos-search-label">salida</span>
        </div>

        <button type="submit" class="destinos-search-btn" disabled={loading}>
          {#if loading}
            <div class="destinos-search-spinner"></div>
            Buscando...
          {:else}
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            Buscar disponibles
          {/if}
        </button>
      </form>
    </div>
  </div>

  <!-- Area de resultados de la busqueda -->
  <div class="destinos-container">

    <!-- Estado de carga mientras se buscan habitaciones -->
    {#if loading}
      <div class="destinos-loading">
        <div class="destinos-spinner"></div>
        <p>Buscando habitaciones disponibles para {personasInput} {personasInput == 1 ? 'persona' : 'personas'}...</p>
      </div>

    <!-- Mensaje de error si la busqueda fallo -->
    {:else if error}
      <div class="destinos-empty">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#e74c3c" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <h2>Error al cargar</h2>
        <p>{error}</p>
        <button class="destinos-btn" on:click={buscar}>Reintentar</button>
      </div>

    <!-- Sin resultados para el criterio de busqueda -->
    {:else if buscado && resultados.length === 0}
      <div class="destinos-empty">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="1.5">
          <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
        </svg>
        <h2>No hay habitaciones disponibles</h2>
        <p>No se encontraron opciones para {personas} {personas === 1 ? 'persona' : 'personas'}. Intenta con otro número o fechas.</p>
      </div>

    <!-- Lista de resultados encontrados -->
    {:else if buscado}
      <div class="destinos-results-header">
        <p class="destinos-results-info">
          <strong>{resultados.length}</strong> opción{resultados.length !== 1 ? 'es' : ''}
          para <strong>{personas} {personas === 1 ? 'persona' : 'personas'}</strong>
          · {checkIn} → {checkOut}
        </p>
      </div>

      <div class="dh-rooms-list">
        {#each resultados as item, i}
          {@const hotel = item.hotel}
          {@const totalNoche = item.precioPorNoche + item.precioPorPersona * personas}

          <!-- Tarjeta de resultado individual con imagen, info y precio -->
          <div class="dh-room" style="animation-delay: {Math.min(i * 0.05, 0.6)}s">

            <!-- Imagen de la habitacion con badges de capacidad y tipo de combo -->
            <div class="dh-room__image">
              {#if roomImage(item)}
                <img src={roomImage(item)} alt={item.label} on:error={handleImgError} />
              {:else}
                <div class="dh-room__no-img">
                  <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                </div>
              {/if}

              <div class="dh-room__capacity-badge">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                {#if item.tipo === 'directa'}
                  Máx. {item.capacidad}
                {:else}
                  {item.capacidad} pers.
                {/if}
              </div>

              {#if item.tipo === 'combinacion'}
                <div class="dh-room__combo-badge">Combinación</div>
              {:else if item.tipo === 'aproximada'}
                <div class="dh-room__combo-badge dh-room__combo-badge--aprox">Aprox.</div>
              {/if}
            </div>

            <!-- Informacion del hotel y detalles de la habitacion -->
            <div class="dh-room__info">
              <div class="dh-room__hotel-tag">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                {hotel.nombre} · {hotel.ciudad}, {hotel.pais}
                {#if hotel.rating}
                  <span class="dh-room__hotel-rating">★ {hotel.rating.toFixed(1)}</span>
                {/if}
              </div>

              <h3 class="dh-room__name">{item.label}</h3>
              <p class="dh-room__desc">{item.descripcion}</p>

              <!-- Especificaciones para habitacion directa -->
              {#if item.tipo === 'directa'}
                <div class="dh-room__specs">
                  <span class="dh-room__spec">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                    {item.tipoCama}
                  </span>
                  <span class="dh-room__spec">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
                    {item.metros} m²
                  </span>
                </div>
              {:else}
                <!-- Detalle de habitaciones del combo -->
                <div class="dh-room__combo-detail">
                  {#each item.habitaciones as hab, idx}
                    <span class="dh-room__combo-chip">
                      Hab.{idx+1}: {hab.tipoHabitacion}
                      <span class="dh-room__combo-chip-cap">({item.combo[idx]} pers.)</span>
                      <span class="dh-room__combo-chip-price">{fmt(hab.precioPorNoche)}/n</span>
                    </span>
                  {/each}
                </div>
              {/if}
            </div>

            <!-- Columna de precios y boton de reserva -->
            <div class="dh-room__pricing">
              <div class="dh-room__price-main">
                <span class="dh-room__price-amount">{fmt(item.precioPorNoche)}</span>
                <span class="dh-room__price-unit">/ noche</span>
              </div>
              <div class="dh-room__price-person">
                + {fmt(item.precioPorPersona)} / persona
              </div>
              <div class="dh-room__price-total">
                Total: {fmt(totalNoche)}
              </div>
              <div class="dh-room__price-detail">
                1 noche · {personas} pers.
              </div>
              <button class="dh-room__btn" on:click={() => goToHotel(hotel)}>
                Reservar
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
              </button>
            </div>

          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>