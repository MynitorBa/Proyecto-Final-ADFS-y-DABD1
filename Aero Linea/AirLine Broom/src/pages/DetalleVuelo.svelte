<script>
/**
 * @file DetalleVuelo.svelte
 * @description Componente modal que muestra informacion detallada de un vuelo individual o un
 * itinerario de multiples tramos (escala). Muestra la seccion hero de ruta completa, desglose
 * individual de tramos para vuelos de conexion, informacion de tripulacion, clases de asiento
 * disponibles con precios y amenidades, y un sistema de comentarios/resenas en hilo para la ruta.
 * Maneja votos positivos y negativos en comentarios, formularios de respuesta en linea y anidacion
 * de comentarios basada en arbol mediante ComentarioNodo. Aparece como superposicion disparada
 * desde la pagina Vuelos cuando el usuario hace clic en "Ver Detalles".
 */
  // @ts-nocheck
  import '../styles/detallesv.css';
  import { onMount, onDestroy } from 'svelte';
  import { sesion } from '../stores/sesion.js';
  import ComentarioNodo from './ComentarioNodo.svelte';

  /** El objeto de vuelo o escala a mostrar. Los vuelos directos tienen campos planos; las escalas tienen un arreglo tramos. @type {object} */
  export let flight;

  /** Funcion de callback para cerrar este modal desde el componente padre. @type {function} */
  export let onClose;

  import { API } from '../lib/api.js';

  $: esEscala  = Array.isArray(flight?.tramos) && flight.tramos.length > 0; /* Verdadero cuando el vuelo tiene tramos, indicando itinerario con escala */

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

  /** Pestana de clase seleccionada actualmente, ya sea 'economico' o 'ejecutivo'. @type {string} */
  let selectedClass = 'economico';

  $: precioMostrado    = selectedClass === 'economico' ? precioTurista    : precioEjecutiva; /* Precio mostrado en el sidebar segun la clase seleccionada */

  $: asientosMostrados = selectedClass === 'economico' ? asientosTurista  : asientosEjecutiva; /* Cantidad de asientos mostrada en el sidebar segun la clase seleccionada */

  $: turistaDisponible   = precioTurista   > 0 && asientosTurista   > 0; /* Verdadero cuando la clase turista tiene precio positivo y al menos un asiento disponible */

  $: ejecutivaDisponible = precioEjecutiva > 0 && asientosEjecutiva > 0; /* Verdadero cuando la clase ejecutiva tiene precio positivo y al menos un asiento disponible */

  $: tripulantes = (() => { /* Lista deduplicada de tripulantes de todos los tramos, identificados por id */
    const vistos = new Set(), lista = [];
    for (const t of tramos)
      for (const trip of (t.tripulantes ?? []))
        if (!vistos.has(trip.id)) { vistos.add(trip.id); lista.push(trip); }
    return lista;
  })();

  /** Listas estaticas de amenidades para cada clase de cabina mostradas en las tarjetas de seleccion de clase. @type {object} */
  const amenidades = {
    economico: ['Equipaje de mano incluido (8kg)','Asiento estandar','Comida y bebida incluida','Entretenimiento a bordo','USB en asiento'],
    ejecutivo: ['Equipaje de mano incluido (12kg)','Equipaje facturado incluido (32kg x2)','Asiento cama totalmente reclinable','Menu gourmet y bar completo','Entretenimiento premium','Kit de amenidades de lujo','Acceso a sala VIP','Embarque prioritario']
  };

  /** Arreglo plano de todos los objetos de comentario obtenidos de la API para la ruta actual. @type {Array<object>} */
  let comentariosPlanos = [];

  /** Verdadero mientras se obtienen los comentarios de la API. @type {boolean} */
  let loadingComentarios = true;

  /** Mapa de estado de UI por comentario indexado por id de comentario, rastreando estado expandido/formulario/respuesta/voto. @type {object} */
  let estadoNodos = {};

  $: haySession = !!$sesion; /* Verdadero cuando el usuario tiene sesion activa, controla si se muestran controles de respuesta y voto */

  $: raices    = comentariosPlanos.filter(c => !c.comentarioPadreId); /* Comentarios raiz sin padre, usados como puntos de entrada del arbol de comentarios */

  $: hijosMap  = (() => { /* Mapa de id de comentario padre a sus hijos directos, reconstruido cuando cambia comentariosPlanos */
    const map = {};
    comentariosPlanos.forEach(c => { if (c.comentarioPadreId) { if (!map[c.comentarioPadreId]) map[c.comentarioPadreId] = []; map[c.comentarioPadreId].push(c); } });
    return map;
  })();

  /**
   * Retorna los comentarios hijos directos de un id de comentario padre dado.
   * @param {number} id - El id de comentario padre a buscar.
   * @returns {Array<object>} Arreglo de objetos de comentario hijo, o arreglo vacio si no existen.
   */
  function getHijos(id) { return hijosMap[id] ?? []; }

  onMount(async () => {
    document.body.classList.add('modal-open');
    if (rutaId) await cargarComentarios();
    else loadingComentarios = false;
  });

  onDestroy(() => { document.body.classList.remove('modal-open'); });

  /**
   * Carga los comentarios de la ruta actual desde la API. Cuando el usuario tiene sesion,
   * obtiene el endpoint extendido que incluye el voto existente del usuario en cada comentario.
   * Inicializa estadoNodos con el estado de UI por defecto para cada comentario retornado.
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
   * Alterna un voto positivo o negativo en un comentario. Si el usuario ya voto con el mismo
   * valor, el voto se elimina mediante DELETE /api/votos/:id. De lo contrario, se envia un nuevo
   * voto mediante POST /api/votos. Actualiza estadoNodos y comentariosPlanos con el nuevo estado
   * de voto y la cantidad de votos negativos actualizada desde la respuesta del servidor.
   * @async
   * @param {number} comentarioId - ID del comentario en el que votar.
   * @param {number} valor - Valor del voto (1 para positivo, -1 para negativo).
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
   * Actualiza el estado de voto local de un comentario sin volver a obtenerlo desde la API.
   * Reemplaza el votoActual en estadoNodos y actualiza la cantidad de votos negativos en comentariosPlanos.
   * Fuerza la reactividad de Svelte reasignando ambos objetos.
   * @param {number} id - Id del comentario a actualizar.
   * @param {number|null} nuevoVoto - El nuevo valor de voto, o null si el voto fue eliminado.
   * @param {number} downs - La cantidad de votos negativos actualizada desde el servidor.
   */
  function _actualizarVoto(id, nuevoVoto, downs) {
    estadoNodos[id] = { ...estadoNodos[id], votoActual: nuevoVoto };
    comentariosPlanos = comentariosPlanos.map(c => c.id === id ? { ...c, downs } : c);
    estadoNodos = { ...estadoNodos };
  }

  /**
   * Alterna la visibilidad del formulario de respuesta de un comentario y limpia el texto borrador.
   * @param {number} id - Id del comentario cuyo formulario de respuesta debe alternarse.
   */
  function toggleForm(id)     { estadoNodos[id] = { ...estadoNodos[id], mostrandoForm: !estadoNodos[id].mostrandoForm, textoRespuesta: '' }; estadoNodos = { ...estadoNodos }; }

  /**
   * Alterna el estado expandido/colapsado de las respuestas hijas de un comentario.
   * @param {number} id - Id del comentario cuyos hijos deben mostrarse u ocultarse.
   */
  function toggleExpandido(id){ estadoNodos[id] = { ...estadoNodos[id], expandido: !estadoNodos[id].expandido }; estadoNodos = { ...estadoNodos }; }

  /**
   * Actualiza el texto borrador de respuesta en estadoNodos para un nodo de comentario especifico.
   * @param {number} id - Id del comentario cuyo borrador de respuesta se esta actualizando.
   * @param {string} v - El nuevo valor de texto del textarea.
   */
  function onTextoChange(id, v){ estadoNodos[id] = { ...estadoNodos[id], textoRespuesta: v }; estadoNodos = { ...estadoNodos }; }

  /**
   * Envia una respuesta a un comentario padre mediante POST /api/comentarios/respuesta. Valida que
   * el texto de respuesta no este vacio antes de enviar. En caso de exito, agrega el nuevo comentario
   * a comentariosPlanos, inicializa su estadoNodo, colapsa el formulario de respuesta en el padre
   * y expande automaticamente los hijos del padre para mostrar la nueva respuesta.
   * @async
   * @param {number} padreId - ID del comentario padre al que se responde.
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
   * Recorta una cadena de hora a su porcion HH:MM para mostrar en la UI.
   * @param {string} h - Cadena de hora en formato HH:MM o HH:MM:SS.
   * @returns {string} Los primeros cinco caracteres de la cadena, o '--:--' si es falsy.
   */
  function formatHora(h)    { return h ? h.substring(0,5) : '--:--'; }

  /**
   * Convierte una duracion en minutos a una cadena legible Xh Ym.
   * @param {number} m - Duracion en minutos.
   * @returns {string} Duracion formateada o 'N/A' si el valor es falsy.
   */
  function formatDur(m)     { if (!m) return 'N/A'; return `${Math.floor(m/60)}h ${m%60}m`; }

  /**
   * Formatea un precio numerico como cadena USD con dos decimales usando el locale en-US.
   * @param {number} p - Valor de precio a formatear.
   * @returns {string} Cadena de precio formateada, o 'No disponible' si es falsy.
   */
  function formatPrecio(p)  { if (!p) return 'No disponible'; return p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }

  /**
   * Convierte una cantidad de estrellas a un arreglo booleano de longitud 5 para renderizar estrellas llenas/vacias.
   * @param {number} n - Cantidad de estrellas llenas (0-5).
   * @returns {Array<boolean>} Arreglo donde verdadero significa que la estrella en ese indice esta llena.
   */
  function getEstrellas(n)  { return Array.from({ length: 5 }, (_, i) => i < (n ?? 0)); }

  /**
   * Formatea una cadena ISO de fecha en una fecha corta localizada usando el locale es-ES.
   * @param {string} f - Cadena ISO de fecha.
   * @returns {string} Cadena de fecha localizada como '3 abr 2026', o cadena vacia si es falsy.
   */
  function formatFecha(f)   { if (!f) return ''; return new Date(f).toLocaleDateString('es-ES', { year:'numeric', month:'short', day:'numeric' }); }

  /**
   * Maneja los clics en el fondo del modal y lo cierra cuando el objetivo del clic
   * es el propio fondo en lugar de un elemento hijo.
   * @param {MouseEvent} e - El evento clic del DOM.
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
