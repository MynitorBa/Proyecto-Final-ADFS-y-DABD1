<script>
/**
 * @file DetalleVuelo.svelte
 * @description Modal component that shows detailed information for a single flight or a
 * multi-leg (escala) itinerary. Displays the full route hero section, individual tramo
 * breakdowns for connecting flights, crew information, available seat classes with pricing
 * and amenities, and a threaded comment/review system for the route. Handles upvoting and
 * downvoting of comments, inline reply forms, and tree-based comment nesting via ComentarioNodo.
 * Appears as an overlay triggered from the Vuelos page when the user clicks "Ver Detalles".
 */
  // @ts-nocheck
  import '../styles/detallesv.css';
  import { onMount, onDestroy } from 'svelte';
  import { sesion } from '../stores/sesion.js';
  import ComentarioNodo from './ComentarioNodo.svelte';

  /** The flight or escala object to display. Direct flights have flat fields; escalas have a tramos array. @type {object} */
  export let flight;

  /** Callback function to close this modal from the parent component. @type {function} */
  export let onClose;

  import { API } from '../lib/api.js';

  $: esEscala  = Array.isArray(flight?.tramos) && flight.tramos.length > 0; /* True cuando el vuelo tiene tramos, indicando itinerario con escala */

  $: tramos    = esEscala ? flight.tramos : (flight ? [flight] : []); /* Lista normalizada de tramos: un elemento para vuelo directo, todos los tramos para escala */

  $: primer    = tramos[0]  ?? {}; /* Primer tramo del itinerario, usado para leer el aeropuerto de origen y hora de salida */

  $: ultimo    = tramos[tramos.length - 1] ?? {}; /* Ultimo tramo del itinerario, usado para leer el aeropuerto de destino y hora de llegada */

  $: rutaId    = primer?.rutaId ?? null; /* ID de ruta del primer tramo, usado para cargar los comentarios de esta ruta */

  $: duracionTotal = esEscala
    ? (flight.duracionTotalMinutos ?? tramos.reduce((a, t) => a + (t.duracionMinutos ?? 0), 0))
    : (flight?.duracionMinutos ?? 0); /* Duracion total en minutos: usa el agregado de escala o suma duraciones individuales */

  $: precioTurista   = esEscala ? (flight.precioTuristaTotal   ?? 0) : (flight?.precioTurista   ?? 0); /* Precio turista: total de escala o precio por asiento del vuelo directo */

  $: precioEjecutiva = esEscala ? (flight.precioEjecutivaTotal ?? 0) : (flight?.precioEjecutiva ?? 0); /* Precio ejecutiva: total de escala o precio por asiento del vuelo directo */

  $: asientosTurista   = flight?.boletosDisponiblesTurista   ?? 0; /* Asientos disponibles en turista calculados desde el objeto flight */

  $: asientosEjecutiva = flight?.boletosDisponiblesEjecutiva ?? 0; /* Asientos disponibles en ejecutiva calculados desde el objeto flight */

  $: totalAsientos = asientosTurista + asientosEjecutiva; /* Suma de asientos de ambas clases, mostrado en el panel de especificaciones */

  /** Currently selected class tab, either 'economico' or 'ejecutivo'. @type {string} */
  let selectedClass = 'economico';

  $: precioMostrado    = selectedClass === 'economico' ? precioTurista    : precioEjecutiva; /* Precio mostrado en el sidebar segun la clase seleccionada */

  $: asientosMostrados = selectedClass === 'economico' ? asientosTurista  : asientosEjecutiva; /* Cantidad de asientos mostrada en el sidebar segun la clase seleccionada */

  $: turistaDisponible   = precioTurista   > 0 && asientosTurista   > 0; /* True cuando la clase turista tiene precio positivo y al menos un asiento disponible */

  $: ejecutivaDisponible = precioEjecutiva > 0 && asientosEjecutiva > 0; /* True cuando la clase ejecutiva tiene precio positivo y al menos un asiento disponible */

  $: tripulantes = (() => { /* Lista deduplicada de tripulantes de todos los tramos, identificados por id */
    const vistos = new Set(), lista = [];
    for (const t of tramos)
      for (const trip of (t.tripulantes ?? []))
        if (!vistos.has(trip.id)) { vistos.add(trip.id); lista.push(trip); }
    return lista;
  })();

  /** Static amenity lists for each cabin class shown in the class selection cards. @type {object} */
  const amenidades = {
    economico: ['Equipaje de mano incluido (8kg)','Asiento estandar','Comida y bebida incluida','Entretenimiento a bordo','USB en asiento'],
    ejecutivo: ['Equipaje de mano incluido (12kg)','Equipaje facturado incluido (32kg x2)','Asiento cama totalmente reclinable','Menu gourmet y bar completo','Entretenimiento premium','Kit de amenidades de lujo','Acceso a sala VIP','Embarque prioritario']
  };

  /** Flat array of all comment objects fetched from the API for the current route. @type {Array<object>} */
  let comentariosPlanos = [];

  /** True while the comments are being fetched from the API. @type {boolean} */
  let loadingComentarios = true;

  /** Map of per-comment UI state keyed by comment id, tracking expanded/form/reply/vote state. @type {object} */
  let estadoNodos = {};

  $: haySession = !!$sesion; /* True cuando el usuario tiene sesion activa, controla si se muestran controles de respuesta y voto */

  $: raices    = comentariosPlanos.filter(c => !c.comentarioPadreId); /* Comentarios raiz sin padre, usados como puntos de entrada del arbol de comentarios */

  $: hijosMap  = (() => { /* Mapa de id de comentario padre a sus hijos directos, reconstruido cuando cambia comentariosPlanos */
    const map = {};
    comentariosPlanos.forEach(c => { if (c.comentarioPadreId) { if (!map[c.comentarioPadreId]) map[c.comentarioPadreId] = []; map[c.comentarioPadreId].push(c); } });
    return map;
  })();

  /**
   * Returns the direct child comments of a given parent comment id.
   * @param {number} id - The parent comment id to look up.
   * @returns {Array<object>} Array of child comment objects, or empty array if none exist.
   */
  function getHijos(id) { return hijosMap[id] ?? []; }

  onMount(async () => {
    document.body.classList.add('modal-open');
    if (rutaId) await cargarComentarios();
    else loadingComentarios = false;
  });

  onDestroy(() => { document.body.classList.remove('modal-open'); });

  /**
   * Loads comments for the current route from the API. When the user has a session,
   * fetches the extended endpoint that includes the user's existing vote on each comment.
   * Initializes estadoNodos with default UI state for each comment returned.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarComentarios() {
    loadingComentarios = true;
    try {
      const s = $sesion;
      const url = s
        ? `${API}/api/comentarios/ruta/${rutaId}/con-voto`
        : `${API}/api/comentarios/ruta/${rutaId}`;
      const res = await fetch(url, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        comentariosPlanos = data;
        const ns = {};
        data.forEach(c => { ns[c.id] = { expandido: false, mostrandoForm: false, textoRespuesta: '', enviando: false, votoActual: c.votoUsuario ?? null }; });
        estadoNodos = ns;
      }
    } catch(e) { console.error(e); }
    finally { loadingComentarios = false; }
  }

  /**
   * Toggles an upvote or downvote on a comment. If the user already voted with the same
   * value, the vote is removed via DELETE /api/votos/:id. Otherwise a new vote is submitted
   * via POST /api/votos. Updates estadoNodos and comentariosPlanos with the new vote state
   * and updated downvote count from the server response.
   * @async
   * @param {number} comentarioId - ID of the comment to vote on.
   * @param {number} valor - Vote value (1 for upvote, -1 for downvote).
   * @returns {Promise<void>}
   */
  async function votar(comentarioId, valor) {
    if (!$sesion) return;
    const estado = estadoNodos[comentarioId]; if (!estado) return;
    try {
      if (estado.votoActual === valor) {
        const r = await fetch(`${API}/api/votos/${comentarioId}`, { method: 'DELETE', credentials: 'include' });
        if (r.ok) { const d = await r.json(); _actualizarVoto(comentarioId, null, d.nuevosDowns); }
      } else {
        const r = await fetch(`${API}/api/votos`, { method:'POST', credentials:'include', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ comentarioId, valor }) });
        if (r.ok) { const d = await r.json(); if (!d.message) _actualizarVoto(comentarioId, valor, d.nuevosDowns); }
      }
    } catch(e) { console.error(e); }
  }

  /**
   * Updates the local vote state for a comment without re-fetching from the API.
   * Replaces the votoActual in estadoNodos and updates the downs count in comentariosPlanos.
   * Forces Svelte reactivity by reassigning both objects.
   * @param {number} id - Comment id to update.
   * @param {number|null} nuevoVoto - The new vote value, or null if the vote was removed.
   * @param {number} downs - The updated downvote count from the server.
   */
  function _actualizarVoto(id, nuevoVoto, downs) {
    estadoNodos[id] = { ...estadoNodos[id], votoActual: nuevoVoto };
    comentariosPlanos = comentariosPlanos.map(c => c.id === id ? { ...c, downs } : c);
    estadoNodos = { ...estadoNodos };
  }

  /**
   * Toggles the reply form visibility for a comment and clears any draft text.
   * @param {number} id - Comment id whose reply form should be toggled.
   */
  function toggleForm(id)     { estadoNodos[id] = { ...estadoNodos[id], mostrandoForm: !estadoNodos[id].mostrandoForm, textoRespuesta: '' }; estadoNodos = { ...estadoNodos }; }

  /**
   * Toggles the expanded/collapsed state of child replies for a comment.
   * @param {number} id - Comment id whose children should be shown or hidden.
   */
  function toggleExpandido(id){ estadoNodos[id] = { ...estadoNodos[id], expandido: !estadoNodos[id].expandido }; estadoNodos = { ...estadoNodos }; }

  /**
   * Updates the draft reply text in estadoNodos for a specific comment node.
   * @param {number} id - Comment id whose reply draft is being updated.
   * @param {string} v - The new text value from the textarea.
   */
  function onTextoChange(id, v){ estadoNodos[id] = { ...estadoNodos[id], textoRespuesta: v }; estadoNodos = { ...estadoNodos }; }

  /**
   * Submits a reply to a parent comment via POST /api/comentarios/respuesta. Validates that
   * the reply text is non-empty before sending. On success, appends the new comment to
   * comentariosPlanos, initializes its estadoNodo, collapses the reply form on the parent
   * and auto-expands the parent's children to show the new reply.
   * @async
   * @param {number} padreId - ID of the parent comment being replied to.
   * @returns {Promise<void>}
   */
  async function enviarRespuesta(padreId) {
    const estado = estadoNodos[padreId]; if (!estado?.textoRespuesta?.trim()) return;
    estadoNodos[padreId] = { ...estado, enviando: true }; estadoNodos = { ...estadoNodos };
    try {
      const r = await fetch(`${API}/api/comentarios/respuesta`, { method:'POST', credentials:'include', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ comentarioPadreId: padreId, contenido: estado.textoRespuesta.trim() }) });
      if (r.ok) {
        const nuevo = await r.json();
        comentariosPlanos = [...comentariosPlanos, nuevo];
        estadoNodos[nuevo.id] = { expandido: false, mostrandoForm: false, textoRespuesta: '', enviando: false, votoActual: null };
        estadoNodos[padreId] = { ...estadoNodos[padreId], mostrandoForm: false, textoRespuesta: '', enviando: false, expandido: true };
        estadoNodos = { ...estadoNodos };
      }
    } catch(e) { estadoNodos[padreId] = { ...estadoNodos[padreId], enviando: false }; estadoNodos = { ...estadoNodos }; }
  }

  /**
   * Trims a time string to its HH:MM portion for display in the UI.
   * @param {string} h - Time string in HH:MM or HH:MM:SS format.
   * @returns {string} First five characters of the string, or '--:--' if falsy.
   */
  function formatHora(h)    { return h ? h.substring(0,5) : '--:--'; }

  /**
   * Converts a duration in minutes to a human-readable Xh Ym string.
   * @param {number} m - Duration in minutes.
   * @returns {string} Formatted duration or 'N/A' if the value is falsy.
   */
  function formatDur(m)     { if (!m) return 'N/A'; return `${Math.floor(m/60)}h ${m%60}m`; }

  /**
   * Formats a numeric price as a USD string with two decimal places using en-US locale.
   * @param {number} p - Price value to format.
   * @returns {string} Formatted price string, or 'No disponible' if falsy.
   */
  function formatPrecio(p)  { if (!p) return 'No disponible'; return p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }

  /**
   * Converts a star count to a boolean array of length 5 for rendering filled/empty stars.
   * @param {number} n - Number of filled stars (0-5).
   * @returns {Array<boolean>} Array where true means the star at that index is filled.
   */
  function getEstrellas(n)  { return Array.from({ length: 5 }, (_, i) => i < (n ?? 0)); }

  /**
   * Formats an ISO date string into a short localized date using es-ES locale.
   * @param {string} f - ISO date string.
   * @returns {string} Localized date string such as '3 abr 2026', or empty string if falsy.
   */
  function formatFecha(f)   { if (!f) return ''; return new Date(f).toLocaleDateString('es-ES', { year:'numeric', month:'short', day:'numeric' }); }

  /**
   * Handles clicks on the modal backdrop and closes the modal when the click
   * target is the backdrop itself rather than a child element.
   * @param {MouseEvent} e - The DOM click event.
   */
  function handleBackdrop(e) { if (e.target === e.currentTarget) onClose(); }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<!-- Modal backdrop que cierra el modal al hacer clic fuera del contenido -->
<div class="dv-backdrop" on:click={handleBackdrop}>
  <div class="dv-modal">

    <!-- Barra superior con identificador del vuelo y boton de cierre -->
    <div class="dv-header">
      <div class="dv-header-left">
        {#if esEscala}
          <span class="dv-code">{primer.origenCodigo ?? '---'} → {ultimo.destinoCodigo ?? '---'}</span>
          <span class="dv-aircraft">{flight.numeroEscalas} {flight.numeroEscalas === 1 ? 'escala' : 'escalas'} · {tramos.map(t => t.numeroVuelo).join(' · ')}</span>
        {:else}
          <span class="dv-code">{flight?.numeroVuelo ?? 'N/A'}</span>
          <span class="dv-aircraft">{flight?.avionMarca ?? ''} {flight?.avionModelo ?? ''}</span>
        {/if}
      </div>
      <button class="dv-close" on:click={onClose}>✕ Cerrar</button>
    </div>

    <!-- Hero con ruta completa, duracion total y tipo de vuelo (directo o con escalas) -->
    <div class="dv-hero">
      <div class="dv-hero-content">
        <div class="dv-hero-point">
          <div class="dv-hero-time">{formatHora(primer.horaSalida)}</div>
          <div class="dv-hero-code">{primer.origenCodigo ?? '---'}</div>
          <div class="dv-hero-label">Salida</div>
        </div>
        <div class="dv-hero-mid">
          <div class="dv-hero-line">
            <div class="dv-hero-dot"></div>
            <div class="dv-hero-track"></div>
            <span class="dv-hero-plane">✈</span>
            <div class="dv-hero-track"></div>
            <div class="dv-hero-dot"></div>
          </div>
          <div class="dv-hero-dur">{formatDur(duracionTotal)}</div>
          <div class="dv-hero-tipo">{esEscala ? `${flight.numeroEscalas} escala${flight.numeroEscalas > 1 ? 's' : ''}` : 'Directo'}</div>
        </div>
        <div class="dv-hero-point dv-hero-point--right">
          <div class="dv-hero-time">{formatHora(ultimo.horaLlegada)}</div>
          <div class="dv-hero-code">{ultimo.destinoCodigo ?? '---'}</div>
          <div class="dv-hero-label">Llegada</div>
        </div>
      </div>
    </div>

    <div class="dv-body">
      <div class="dv-grid">

        <div class="dv-main">

          <!-- Detalle de tramos para vuelos con escala o informacion del vuelo directo -->
          {#if esEscala}
            <div class="dv-section-title">Detalle de tramos</div>

            {#each tramos as tramo, ti}
              <div class="dv-tramo-card">
                <div class="dv-tramo-header">
                  <span class="dv-tramo-badge">Tramo {ti + 1}</span>
                  <span class="dv-tramo-vuelo">{tramo.numeroVuelo}</span>
                  <span class="dv-tramo-aeronave">{tramo.avionMarca} {tramo.avionModelo}</span>
                </div>

                <div class="dv-tramo-hero">
                  <div class="dv-tramo-point">
                    <div class="dv-tramo-time">{formatHora(tramo.horaSalida)}</div>
                    <div class="dv-tramo-code">{tramo.origenCodigo}</div>
                    <div class="dv-tramo-city">{tramo.origenCiudad}</div>
                    <div class="dv-tramo-pais">{tramo.origenPais}</div>
                  </div>
                  <div class="dv-tramo-mid">
                    <div class="dv-tramo-track-wrap">
                      <div class="dv-tramo-dot"></div>
                      <div class="dv-tramo-line"></div>
                      <span class="dv-tramo-plane">✈</span>
                      <div class="dv-tramo-line"></div>
                      <div class="dv-tramo-dot"></div>
                    </div>
                    <div class="dv-tramo-dur">{formatDur(tramo.duracionMinutos)}</div>
                    <div class="dv-tramo-tipo">Directo</div>
                  </div>
                  <div class="dv-tramo-point dv-tramo-point--right">
                    <div class="dv-tramo-time">{formatHora(tramo.horaLlegada)}</div>
                    <div class="dv-tramo-code">{tramo.destinoCodigo}</div>
                    <div class="dv-tramo-city">{tramo.destinoCiudad}</div>
                    <div class="dv-tramo-pais">{tramo.destinoPais}</div>
                  </div>
                </div>

                <div class="dv-specs">
                  <div class="dv-spec"><div class="dv-spec-label">Vuelo</div><div class="dv-spec-value">{tramo.numeroVuelo}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Fecha</div><div class="dv-spec-value">{tramo.fecha ? tramo.fecha.substring(0,10) : 'N/A'}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Aeronave</div><div class="dv-spec-value">{tramo.avionMarca} {tramo.avionModelo}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Duracion</div><div class="dv-spec-value">{formatDur(tramo.duracionMinutos)}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Estado</div><div class="dv-spec-value">{tramo.estado ?? 'N/A'}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Capacidad</div><div class="dv-spec-value dv-spec-value--gold">{tramo.capacidadPasajeros ?? '—'}</div></div>
                </div>

                {#if (tramo.tripulantes ?? []).length > 0}
                  <div class="dv-section-title dv-section-title--sm dv-section-title--mt">Tripulacion del tramo</div>
                  <div class="dv-tripulantes">
                    {#each tramo.tripulantes as trip}
                      <div class="dv-tripulante">
                        <div class="dv-tripulante-avatar">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                        </div>
                        <div class="dv-tripulante-info">
                          <div class="dv-tripulante-nombre">{trip.nombreCompleto}</div>
                          <div class="dv-tripulante-rol">{trip.nombreRol}</div>
                        </div>
                      </div>
                    {/each}
                  </div>
                {/if}
              </div>

              {#if ti < tramos.length - 1}
                <div class="dv-escala-sep">
                  <div class="dv-escala-sep__line"></div>
                  <div class="dv-escala-sep__badge">
                    ⇌ Escala en {tramo.destinoCiudad} · {formatDur(flight.tiempoEscalaMinutos)}
                  </div>
                  <div class="dv-escala-sep__line"></div>
                </div>
              {/if}
            {/each}

          {:else}
            <div class="dv-section-title">Informacion del vuelo</div>
            <div class="dv-specs">
              <div class="dv-spec"><div class="dv-spec-label">Numero de vuelo</div><div class="dv-spec-value">{flight?.numeroVuelo ?? 'N/A'}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Fecha</div><div class="dv-spec-value">{flight?.fecha ? flight.fecha.substring(0,10) : 'N/A'}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Aeronave</div><div class="dv-spec-value">{flight?.avionMarca} {flight?.avionModelo}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Duracion</div><div class="dv-spec-value">{formatDur(flight?.duracionMinutos)}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Asientos disponibles</div><div class="dv-spec-value dv-spec-value--gold">{totalAsientos}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Tipo</div><div class="dv-spec-value">Directo</div></div>
            </div>

            {#if tripulantes.length > 0}
              <div class="dv-section-title dv-section-title--mt">Tripulacion</div>
              <div class="dv-tripulantes">
                {#each tripulantes as trip}
                  <div class="dv-tripulante">
                    <div class="dv-tripulante-avatar">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    </div>
                    <div class="dv-tripulante-info">
                      <div class="dv-tripulante-nombre">{trip.nombreCompleto}</div>
                      <div class="dv-tripulante-rol">{trip.nombreRol}</div>
                    </div>
                  </div>
                {/each}
              </div>
            {/if}
          {/if}

          <!-- Selector de clase de cabina con precios y lista de amenidades -->
          <div class="dv-section-title dv-section-title--mt">Clases disponibles</div>
          <div class="dv-clases">
            <button
              class="dv-clase"
              class:dv-clase--selected={selectedClass === 'economico'}
              class:dv-clase--disabled={!turistaDisponible}
              disabled={!turistaDisponible}
              on:click={() => turistaDisponible && (selectedClass = 'economico')}
            >
              {#if selectedClass === 'economico' && turistaDisponible}<div class="dv-clase-badge">✓ Seleccionada</div>{/if}
              <div class="dv-clase-name">Clase Turista</div>
              {#if turistaDisponible}
                <div class="dv-clase-avail">{asientosTurista} asientos disponibles</div>
                <div class="dv-clase-price">
                  <span class="dv-clase-desde">Desde</span>
                  <span class="dv-clase-amount">$ {formatPrecio(precioTurista)}</span>
                  <span class="dv-clase-pp">/ persona</span>
                </div>
              {:else}
                <div class="dv-clase-unavailable">No disponible</div>
              {/if}
              <ul class="dv-amenities">{#each amenidades.economico as a}<li>{a}</li>{/each}</ul>
            </button>

            <button
              class="dv-clase dv-clase--premium"
              class:dv-clase--selected={selectedClass === 'ejecutivo'}
              class:dv-clase--disabled={!ejecutivaDisponible}
              disabled={!ejecutivaDisponible}
              on:click={() => ejecutivaDisponible && (selectedClass = 'ejecutivo')}
            >
              <div class="dv-clase-ribbon">Premium</div>
              {#if selectedClass === 'ejecutivo' && ejecutivaDisponible}<div class="dv-clase-badge">✓ Seleccionada</div>{/if}
              <div class="dv-clase-name">Clase Ejecutiva</div>
              {#if ejecutivaDisponible}
                <div class="dv-clase-avail">{asientosEjecutiva} asientos disponibles</div>
                <div class="dv-clase-price">
                  <span class="dv-clase-desde">Desde</span>
                  <span class="dv-clase-amount">$ {formatPrecio(precioEjecutiva)}</span>
                  <span class="dv-clase-pp">/ persona</span>
                </div>
              {:else}
                <div class="dv-clase-unavailable">No disponible</div>
              {/if}
              <ul class="dv-amenities">{#each amenidades.ejecutivo as a}<li>{a}</li>{/each}</ul>
            </button>
          </div>

          <!-- Seccion de resenas con arbol de comentarios jerarquico para la ruta -->
          <div class="dv-section-title dv-section-title--mt">
            Resenas · {primer.origenCodigo ?? '---'} → {ultimo.destinoCodigo ?? '---'}
          </div>
          {#if loadingComentarios}
            <div class="dv-loading">Cargando resenas...</div>
          {:else if raices.length === 0}
            <div class="dv-no-comentarios">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <p>Aun no hay resenas para esta ruta</p>
            </div>
          {:else}
            <div class="dv-comentarios-raiz">
              {#each raices as comentario (comentario.id)}
                <ComentarioNodo
                  {comentario} {getHijos} {estadoNodos} {haySession}
                  {formatFecha} {getEstrellas} {votar}
                  {toggleForm} {toggleExpandido} {enviarRespuesta} {onTextoChange}
                  profundidad={0}
                />
              {/each}
            </div>
          {/if}

        </div>

        <!-- Sidebar con resumen de vuelo, clase seleccionada y precio referencial -->
        <aside class="dv-sidebar">
          <div class="dv-summary">
            <div class="dv-summary-title">Resumen</div>

            {#if esEscala}
              {#each tramos as tramo, ti}
                <div class="dv-summary-tramo-label">Tramo {ti + 1} — {tramo.numeroVuelo}</div>
                <div class="dv-summary-row"><span>Salida</span><span>{tramo.origenCodigo} · {formatHora(tramo.horaSalida)}</span></div>
                <div class="dv-summary-row"><span>Llegada</span><span>{tramo.destinoCodigo} · {formatHora(tramo.horaLlegada)}</span></div>
                <div class="dv-summary-row"><span>Duracion</span><span>{formatDur(tramo.duracionMinutos)}</span></div>
                {#if ti < tramos.length - 1}
                  <div class="dv-summary-escala-sep">⇌ Escala {formatDur(flight.tiempoEscalaMinutos)}</div>
                {/if}
              {/each}
              <div class="dv-summary-divider"></div>
              <div class="dv-summary-row"><span>Duracion total</span><span class="dv-summary-strong">{formatDur(duracionTotal)}</span></div>
            {:else}
              <div class="dv-summary-row"><span>Vuelo</span><span>{flight?.numeroVuelo ?? 'N/A'}</span></div>
              <div class="dv-summary-row"><span>Fecha</span><span>{flight?.fecha ? flight.fecha.substring(0,10) : 'N/A'}</span></div>
              <div class="dv-summary-row"><span>Salida</span><span>{flight?.origenCodigo ?? '---'} · {formatHora(flight?.horaSalida)}</span></div>
              <div class="dv-summary-row"><span>Llegada</span><span>{flight?.destinoCodigo ?? '---'} · {formatHora(flight?.horaLlegada)}</span></div>
              <div class="dv-summary-row"><span>Duracion</span><span>{formatDur(flight?.duracionMinutos)}</span></div>
              <div class="dv-summary-row"><span>Aeronave</span><span>{flight?.avionMarca} {flight?.avionModelo}</span></div>
            {/if}

            <div class="dv-summary-divider"></div>
            <div class="dv-summary-row dv-summary-row--highlight">
              <span>Clase</span>
              <span>{selectedClass === 'economico' ? 'Turista' : 'Ejecutiva'}</span>
            </div>
            <div class="dv-summary-divider"></div>
            {#if precioMostrado > 0}
              <div class="dv-summary-row">
                <span>Precio {esEscala ? 'total ruta' : '/ persona'}</span>
                <span class="dv-summary-price">$ {formatPrecio(precioMostrado)}</span>
              </div>
              <div class="dv-summary-row">
                <span>Asientos disponibles</span>
                <span class="dv-summary-seats">{asientosMostrados}</span>
              </div>
            {:else}
              <span class="dv-summary-unavailable">Clase no disponible</span>
            {/if}
            <div class="dv-summary-note">Precio referencial · sujeto a cambios</div>
            <div class="dv-summary-disclaimer">Vista informativa. Para reservar, selecciona el vuelo en la lista principal.</div>
          </div>
        </aside>

      </div>
    </div>

  </div>
</div>
