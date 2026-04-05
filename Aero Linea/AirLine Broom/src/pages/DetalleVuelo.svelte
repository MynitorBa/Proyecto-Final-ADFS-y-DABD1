<script>
  // @ts-nocheck
  import '../styles/detallesv.css';
  import { onMount, onDestroy } from 'svelte';
  import { sesion } from '../stores/sesion.js';
  import ComentarioNodo from './ComentarioNodo.svelte';

  export let flight;   // vuelo directo O objeto escala con tramos[]
  export let onClose;

  import { API } from '../lib/api.js';

  // ── Normalizar: escala tiene tramos[], directo no ──
  $: esEscala  = Array.isArray(flight?.tramos) && flight.tramos.length > 0;
  $: tramos    = esEscala ? flight.tramos : (flight ? [flight] : []);
  $: primer    = tramos[0]  ?? {};
  $: ultimo    = tramos[tramos.length - 1] ?? {};
  $: rutaId    = primer?.rutaId ?? null;

  // Duración total
  $: duracionTotal = esEscala
    ? (flight.duracionTotalMinutos ?? tramos.reduce((a, t) => a + (t.duracionMinutos ?? 0), 0))
    : (flight?.duracionMinutos ?? 0);

  // Precios
  $: precioTurista   = esEscala ? (flight.precioTuristaTotal   ?? 0) : (flight?.precioTurista   ?? 0);
  $: precioEjecutiva = esEscala ? (flight.precioEjecutivaTotal ?? 0) : (flight?.precioEjecutiva ?? 0);
  $: asientosTurista   = flight?.boletosDisponiblesTurista   ?? 0;
  $: asientosEjecutiva = flight?.boletosDisponiblesEjecutiva ?? 0;
  $: totalAsientos = asientosTurista + asientosEjecutiva;

  let selectedClass = 'economico';
  $: precioMostrado    = selectedClass === 'economico' ? precioTurista    : precioEjecutiva;
  $: asientosMostrados = selectedClass === 'economico' ? asientosTurista  : asientosEjecutiva;
  $: turistaDisponible   = precioTurista   > 0 && asientosTurista   > 0;
  $: ejecutivaDisponible = precioEjecutiva > 0 && asientosEjecutiva > 0;

  // Tripulantes únicos de todos los tramos
  $: tripulantes = (() => {
    const vistos = new Set(), lista = [];
    for (const t of tramos)
      for (const trip of (t.tripulantes ?? []))
        if (!vistos.has(trip.id)) { vistos.add(trip.id); lista.push(trip); }
    return lista;
  })();

  const amenidades = {
    economico: ['Equipaje de mano incluido (8kg)','Asiento estándar','Comida y bebida incluida','Entretenimiento a bordo','USB en asiento'],
    ejecutivo: ['Equipaje de mano incluido (12kg)','Equipaje facturado incluido (32kg x2)','Asiento cama totalmente reclinable','Menú gourmet y bar completo','Entretenimiento premium','Kit de amenidades de lujo','Acceso a sala VIP','Embarque prioritario']
  };

  let comentariosPlanos = [];
  let loadingComentarios = true;
  let estadoNodos = {};
  $: haySession = !!$sesion;
  $: raices    = comentariosPlanos.filter(c => !c.comentarioPadreId);
  $: hijosMap  = (() => {
    const map = {};
    comentariosPlanos.forEach(c => { if (c.comentarioPadreId) { if (!map[c.comentarioPadreId]) map[c.comentarioPadreId] = []; map[c.comentarioPadreId].push(c); } });
    return map;
  })();
  function getHijos(id) { return hijosMap[id] ?? []; }

  // ── Bloquear scroll del body ──
  onMount(async () => {
    document.body.classList.add('modal-open');
    if (rutaId) await cargarComentarios();
    else loadingComentarios = false;
  });
  onDestroy(() => { document.body.classList.remove('modal-open'); });

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
  function _actualizarVoto(id, nuevoVoto, downs) {
    estadoNodos[id] = { ...estadoNodos[id], votoActual: nuevoVoto };
    comentariosPlanos = comentariosPlanos.map(c => c.id === id ? { ...c, downs } : c);
    estadoNodos = { ...estadoNodos };
  }

  function toggleForm(id)     { estadoNodos[id] = { ...estadoNodos[id], mostrandoForm: !estadoNodos[id].mostrandoForm, textoRespuesta: '' }; estadoNodos = { ...estadoNodos }; }
  function toggleExpandido(id){ estadoNodos[id] = { ...estadoNodos[id], expandido: !estadoNodos[id].expandido }; estadoNodos = { ...estadoNodos }; }
  function onTextoChange(id, v){ estadoNodos[id] = { ...estadoNodos[id], textoRespuesta: v }; estadoNodos = { ...estadoNodos }; }

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

  function formatHora(h)    { return h ? h.substring(0,5) : '--:--'; }
  function formatDur(m)     { if (!m) return 'N/A'; return `${Math.floor(m/60)}h ${m%60}m`; }
  function formatPrecio(p)  { if (!p) return 'No disponible'; return p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
  function getEstrellas(n)  { return Array.from({ length: 5 }, (_, i) => i < (n ?? 0)); }
  function formatFecha(f)   { if (!f) return ''; return new Date(f).toLocaleDateString('es-ES', { year:'numeric', month:'short', day:'numeric' }); }

  function handleBackdrop(e) { if (e.target === e.currentTarget) onClose(); }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="dv-backdrop" on:click={handleBackdrop}>
  <div class="dv-modal">

    <!-- STICKY HEADER -->
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

    <!-- HERO RUTA COMPLETA -->
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

    <!-- SCROLLABLE BODY -->
    <div class="dv-body">
      <div class="dv-grid">

        <!-- ── MAIN ── -->
        <div class="dv-main">

          {#if esEscala}
            <!-- ══ TRAMOS ══ -->
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
                  <div class="dv-spec"><div class="dv-spec-label">Duración</div><div class="dv-spec-value">{formatDur(tramo.duracionMinutos)}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Estado</div><div class="dv-spec-value">{tramo.estado ?? 'N/A'}</div></div>
                  <div class="dv-spec"><div class="dv-spec-label">Capacidad</div><div class="dv-spec-value dv-spec-value--gold">{tramo.capacidadPasajeros ?? '—'}</div></div>
                </div>

                {#if (tramo.tripulantes ?? []).length > 0}
                  <div class="dv-section-title dv-section-title--sm dv-section-title--mt">Tripulación del tramo</div>
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

              <!-- Conexión entre tramos -->
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
            <!-- ══ VUELO DIRECTO ══ -->
            <div class="dv-section-title">Información del vuelo</div>
            <div class="dv-specs">
              <div class="dv-spec"><div class="dv-spec-label">Número de vuelo</div><div class="dv-spec-value">{flight?.numeroVuelo ?? 'N/A'}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Fecha</div><div class="dv-spec-value">{flight?.fecha ? flight.fecha.substring(0,10) : 'N/A'}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Aeronave</div><div class="dv-spec-value">{flight?.avionMarca} {flight?.avionModelo}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Duración</div><div class="dv-spec-value">{formatDur(flight?.duracionMinutos)}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Asientos disponibles</div><div class="dv-spec-value dv-spec-value--gold">{totalAsientos}</div></div>
              <div class="dv-spec"><div class="dv-spec-label">Tipo</div><div class="dv-spec-value">Directo</div></div>
            </div>

            {#if tripulantes.length > 0}
              <div class="dv-section-title dv-section-title--mt">Tripulación</div>
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

          <!-- ══ CLASES ══ -->
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

          <!-- ══ RESEÑAS ══ -->
          <div class="dv-section-title dv-section-title--mt">
            Reseñas · {primer.origenCodigo ?? '---'} → {ultimo.destinoCodigo ?? '---'}
          </div>
          {#if loadingComentarios}
            <div class="dv-loading">Cargando reseñas...</div>
          {:else if raices.length === 0}
            <div class="dv-no-comentarios">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <p>Aún no hay reseñas para esta ruta</p>
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

        <!-- ── SIDEBAR ── -->
        <aside class="dv-sidebar">
          <div class="dv-summary">
            <div class="dv-summary-title">Resumen</div>

            {#if esEscala}
              {#each tramos as tramo, ti}
                <div class="dv-summary-tramo-label">Tramo {ti + 1} — {tramo.numeroVuelo}</div>
                <div class="dv-summary-row"><span>Salida</span><span>{tramo.origenCodigo} · {formatHora(tramo.horaSalida)}</span></div>
                <div class="dv-summary-row"><span>Llegada</span><span>{tramo.destinoCodigo} · {formatHora(tramo.horaLlegada)}</span></div>
                <div class="dv-summary-row"><span>Duración</span><span>{formatDur(tramo.duracionMinutos)}</span></div>
                {#if ti < tramos.length - 1}
                  <div class="dv-summary-escala-sep">⇌ Escala {formatDur(flight.tiempoEscalaMinutos)}</div>
                {/if}
              {/each}
              <div class="dv-summary-divider"></div>
              <div class="dv-summary-row"><span>Duración total</span><span class="dv-summary-strong">{formatDur(duracionTotal)}</span></div>
            {:else}
              <div class="dv-summary-row"><span>Vuelo</span><span>{flight?.numeroVuelo ?? 'N/A'}</span></div>
              <div class="dv-summary-row"><span>Fecha</span><span>{flight?.fecha ? flight.fecha.substring(0,10) : 'N/A'}</span></div>
              <div class="dv-summary-row"><span>Salida</span><span>{flight?.origenCodigo ?? '---'} · {formatHora(flight?.horaSalida)}</span></div>
              <div class="dv-summary-row"><span>Llegada</span><span>{flight?.destinoCodigo ?? '---'} · {formatHora(flight?.horaLlegada)}</span></div>
              <div class="dv-summary-row"><span>Duración</span><span>{formatDur(flight?.duracionMinutos)}</span></div>
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