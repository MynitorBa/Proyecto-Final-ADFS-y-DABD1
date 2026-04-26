<script>
// @ts-nocheck
/**
 * @file AdminGestionTripulacion.svelte
 * @description Gestión de la tripulación asignada a cada vuelo.
 * Muestra los vuelos activos (hoy y futuro) y permite ver, modificar,
 * auto-rellenar o limpiar la tripulación de cada uno.
 */
  import { onMount } from 'svelte';

  export let API;
  export let mostrarToast;
  export let mostrarConfirm;

  // ── Vuelos ──────────────────────────────────────────────────────────────
  let vuelos          = [];
  let loadingVuelos   = false;
  let filtroBusqueda  = '';

  $: vuelosFiltrados = vuelos.filter(v => {
    if (!filtroBusqueda.trim()) return true;
    const q = filtroBusqueda.trim().toLowerCase();
    return v.numeroVuelo?.toLowerCase().includes(q)
        || v.origen?.toLowerCase().includes(q)
        || v.destino?.toLowerCase().includes(q);
  });

  async function cargarVuelos() {
    loadingVuelos = true;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/historial`, { credentials: 'include' });
      if (r.ok) {
        const todos  = await r.json();
        const hoy    = new Date().toISOString().split('T')[0];
        vuelos = todos
          .filter(v => (v.estado === 'Activo' || v.estado === 'En curso') && v.fecha >= hoy)
          .sort((a, b) => a.fecha.localeCompare(b.fecha) || a.horaSalida.localeCompare(b.horaSalida));
      } else {
        mostrarToast('error', 'Error al cargar vuelos');
      }
    } catch { mostrarToast('error', 'Error de conexión'); }
    finally { loadingVuelos = false; }
  }

  // ── Tripulantes ──────────────────────────────────────────────────────────
  let todosTripulantes = [];
  let rolesTripulacion = [];

  $: rolIdPiloto   = rolesTripulacion.find(r => r.nombre.toLowerCase().includes('piloto') && !r.nombre.toLowerCase().includes('co'))?.id ?? -1;
  $: rolIdCopiloto = rolesTripulacion.find(r => r.nombre.toLowerCase().includes('copiloto') || r.nombre.toLowerCase().includes('co-piloto'))?.id ?? -2;

  async function cargarTripulantes() {
    try {
      const [rT, rR] = await Promise.all([
        fetch(`${API}/api/tripulacion?incluirInactivos=false`, { credentials: 'include' }),
        fetch(`${API}/api/tripulacion/roles`,                  { credentials: 'include' })
      ]);
      if (rT.ok) todosTripulantes = await rT.json();
      if (rR.ok) rolesTripulacion = (await rR.json()).map(r => ({ id: r.id, nombre: r.cargo }));
    } catch { console.error('Error al cargar tripulantes'); }
  }

  onMount(() => Promise.all([cargarVuelos(), cargarTripulantes()]));

  // ── Modal de gestión ─────────────────────────────────────────────────────
  let vueloGestionar       = null;
  let mostrarModal         = false;
  let cargandoEquipo       = false;
  let guardando            = false;
  let autoRellenadoListo   = false;

  /** IDs seleccionados para el vuelo actual */
  let seleccionados = [];

  /** Equipos actuales cargados por vuelo (cache) */
  let equipoCache = {};

  $: pilotos    = seleccionados.filter(id => todosTripulantes.find(t => t.id === id)?.rolID === rolIdPiloto).length;
  $: copilotos  = seleccionados.filter(id => todosTripulantes.find(t => t.id === id)?.rolID === rolIdCopiloto).length;
  $: auxiliares = seleccionados.filter(id => {
      const t = todosTripulantes.find(t => t.id === id);
      return t && t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto;
    }).length;
  $: composicionValida = pilotos >= 1 && copilotos >= 1 && auxiliares >= 3;

  async function abrirModal(vuelo) {
    vueloGestionar     = vuelo;
    seleccionados      = [];
    autoRellenadoListo = false;
    mostrarModal       = true;
    cargandoEquipo     = true;
    try {
      const r = await fetch(`${API}/api/tripulacion/vuelo/${vuelo.id}/equipo`, { credentials: 'include' });
      if (r.ok) {
        const equipo      = await r.json();
        equipoCache[vuelo.id] = equipo;
        seleccionados     = equipo.map(t => t.id);
      }
    } catch { mostrarToast('error', 'Error al cargar equipo del vuelo'); }
    finally { cargandoEquipo = false; }
  }

  function cerrarModal() {
    mostrarModal   = false;
    vueloGestionar = null;
    seleccionados  = [];
  }

  function toggleTripulante(id) {
    if (seleccionados.includes(id)) {
      seleccionados = seleccionados.filter(x => x !== id);
    } else {
      const t = todosTripulantes.find(x => x.id === id);
      if (!t) return;
      if (t.rolID === rolIdPiloto && pilotos >= 1) {
        mostrarToast('error', 'Solo se puede asignar 1 piloto por vuelo');
        return;
      }
      if (t.rolID === rolIdCopiloto && copilotos >= 1) {
        mostrarToast('error', 'Solo se puede asignar 1 copiloto por vuelo');
        return;
      }
      if (t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto && auxiliares >= 3) {
        mostrarToast('error', 'Solo se pueden asignar 3 auxiliares por vuelo');
        return;
      }
      if (seleccionados.length >= 5) {
        mostrarToast('error', 'El equipo ya tiene 5 tripulantes (máximo)');
        return;
      }
      seleccionados = [...seleccionados, id];
    }
    autoRellenadoListo = false;
  }

  async function limpiarTripulacion() {
    const ok = await mostrarConfirm('¿Quitar toda la tripulación de este vuelo?', '', 'danger');
    if (!ok) return;
    seleccionados      = [];
    autoRellenadoListo = false;
  }

  function autoRellenar() {
    const rand = arr => arr[Math.floor(Math.random() * arr.length)];
    const selec = [];
    const usados = new Set();

    const pilotsDisp  = todosTripulantes.filter(t => t.activo !== false && t.rolID === rolIdPiloto);
    const copilosDisp = todosTripulantes.filter(t => t.activo !== false && t.rolID === rolIdCopiloto);
    const auxDisp     = todosTripulantes.filter(t => t.activo !== false && t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto);

    function elegir(pool) {
      const cands = pool.filter(t => !usados.has(t.id));
      if (!cands.length) return;
      const e = rand(cands);
      selec.push(e.id);
      usados.add(e.id);
    }

    elegir(pilotsDisp);
    elegir(copilosDisp);
    for (let i = 0; i < 3; i++) elegir(auxDisp);

    seleccionados      = selec;
    autoRellenadoListo = true;
  }

  async function guardarTripulacion() {
    guardando = true;
    try {
      const r = await fetch(`${API}/api/tripulacion/vuelo/${vueloGestionar.id}/equipo`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(seleccionados)
      });
      if (r.ok) {
        mostrarToast('success', 'Tripulación actualizada correctamente');
        equipoCache[vueloGestionar.id] = todosTripulantes.filter(t => seleccionados.includes(t.id));
        cerrarModal();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar');
      }
    } catch { mostrarToast('error', 'Error de conexión'); }
    finally { guardando = false; }
  }

  /** Devuelve el equipo cacheado de un vuelo, o array vacío. */
  function equipoDeVuelo(vueloId) {
    return equipoCache[vueloId] ?? [];
  }

  /** ¿El vuelo tiene composición mínima cacheada? */
  function vueloOk(vueloId) {
    const eq   = equipoDeVuelo(vueloId);
    const p    = eq.filter(t => t.rolID === rolIdPiloto).length;
    const c    = eq.filter(t => t.rolID === rolIdCopiloto).length;
    const a    = eq.filter(t => t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto).length;
    return eq.length > 0 && p >= 1 && c >= 1 && a >= 3;
  }

  /** Horas que faltan para la salida del vuelo (puede ser negativo si ya pasó). */
  function horasHastaVuelo(v) {
    const salidaStr = `${v.fecha}T${v.horaSalida}:00`;
    const salida = new Date(salidaStr);
    return (salida - new Date()) / (1000 * 60 * 60);
  }
</script>

<section class="admin-section">
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Tripulación</h2>
      <p class="admin-section__subtitle">Asigna o modifica la tripulación de vuelos activos</p>
    </div>
    <button class="btn-add" on:click={() => Promise.all([cargarVuelos(), cargarTripulantes()])}
      style="background:#4b5563">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
        stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px">
        <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>Actualizar
    </button>
  </div>

  <!-- Buscador -->
  <div class="gt-search-wrap" style="margin-bottom:1rem">
    <svg class="gt-search-icon" viewBox="0 0 20 20" fill="currentColor">
      <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
    </svg>
    <input type="text" class="gt-search" bind:value={filtroBusqueda}
      placeholder="Buscar por número, origen o destino..." />
  </div>

  {#if loadingVuelos}
    <p class="loading-text">Cargando vuelos...</p>

  {:else if vuelos.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay vuelos activos o próximos.</p>
    </div>

  {:else if vuelosFiltrados.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">Ningún vuelo coincide con la búsqueda.</p>
    </div>

  {:else}
    <div class="gt-vuelos-grid">
      {#each vuelosFiltrados as v}
        {@const eq = equipoDeVuelo(v.id)}
        {@const ok = vueloOk(v.id)}
        {@const horas = horasHastaVuelo(v)}
        {@const bloqueado = horas < 1}
        <div class="gt-card" class:gt-card--ok={ok && eq.length > 0} class:gt-card--warn={!ok && eq.length > 0} class:gt-card--empty={eq.length === 0}>

          <!-- Header del vuelo -->
          <div class="gt-card__header">
            <span class="gt-card__num">{v.numeroVuelo}</span>
            <span class="gt-card__ruta">{v.origen} → {v.destino}</span>
            {#if ok && eq.length > 0}
              <span class="gt-badge gt-badge--ok">Completa</span>
            {:else if eq.length > 0}
              <span class="gt-badge gt-badge--warn">Incompleta</span>
            {:else}
              <span class="gt-badge gt-badge--empty">Sin tripulación</span>
            {/if}
          </div>

          <!-- Info del vuelo -->
          <div class="gt-card__meta">
            <span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>{v.fecha}
            </span>
            <span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>{v.horaSalida}
            </span>
            {#if v.avionNombre || v.avion}
            <span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>{v.avionNombre ?? v.avion}
            </span>
            {/if}
            {#if bloqueado}
              <span class="gt-badge-blocked">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:2px"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>Bloqueado (&lt;1h)
              </span>
            {/if}
          </div>

          <!-- Equipo cargado (solo si ya fue abierto) -->
          {#if eq.length > 0}
            <div class="gt-card__equipo">
              {#each eq as t}
                <span class="gt-pill"
                  class:gt-pill--piloto={t.rolID === rolIdPiloto}
                  class:gt-pill--copiloto={t.rolID === rolIdCopiloto}
                  class:gt-pill--auxiliar={t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto}>
                  {t.nombre} {t.apellido}
                </span>
              {/each}
            </div>
          {/if}

          <button class="gt-card__btn" class:gt-card__btn--blocked={bloqueado}
            on:click={() => !bloqueado && abrirModal(v)} disabled={bloqueado}>
            {#if bloqueado}
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>Sin cambios (&lt;1h)
            {:else}
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>Gestionar tripulación
            {/if}
          </button>
        </div>
      {/each}
    </div>
  {/if}
</section>

<!-- Modal de gestión de tripulación -->
{#if mostrarModal && vueloGestionar}
  <div class="modal-overlay" role="dialog" aria-modal="true">
    <div class="modal modal--gt" on:click|stopPropagation>

      <!-- Overlay guardando -->
      {#if guardando}
        <div class="gt-saving-overlay">
          <div class="autofill-overlay__box">
            <div class="autofill-spinner"></div>
            <p class="autofill-overlay__title">Guardando tripulación…</p>
          </div>
        </div>
      {/if}

      <div class="modal__header">
        <div style="display:flex;flex-direction:column;gap:2px">
          <span style="font-size:1rem;font-weight:700;color:#1C1A18">{vueloGestionar.numeroVuelo}</span>
          <span style="font-size:0.85rem;color:#374151">{vueloGestionar.origen} → {vueloGestionar.destino}</span>
          <span style="font-size:0.75rem;color:#6b7280">{vueloGestionar.fecha} · {vueloGestionar.horaSalida}</span>
        </div>
        <button class="modal__close" on:click={cerrarModal} disabled={guardando}>×</button>
      </div>

      <div class="modal__body">
        {#if cargandoEquipo}
          <p class="modal-loading">Cargando tripulación actual…</p>

        {:else}
          <!-- Contador de composición -->
          <div class="gt-composicion">
            <span class:comp-ok={pilotos >= 1} class:comp-err={pilotos < 1}>
              Pilotos: {pilotos}/1
            </span>
            <span class:comp-ok={copilotos >= 1} class:comp-err={copilotos < 1}>
              Copilotos: {copilotos}/1
            </span>
            <span class:comp-ok={auxiliares >= 3} class:comp-err={auxiliares < 3}>
              Auxiliares: {auxiliares}/3
            </span>
            {#if composicionValida}
              <span class="gt-comp-ok">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:2px"><polyline points="20 6 9 17 4 12"/></svg>Composición válida
              </span>
            {:else}
              <span class="gt-comp-err">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:2px"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>Faltan roles
              </span>
            {/if}
          </div>

          <!-- Tripulantes seleccionados actualmente -->
          {#if seleccionados.length > 0}
            <div class="gt-seleccionados">
              {#each seleccionados as id}
                {@const t = todosTripulantes.find(x => x.id === id)}
                {#if t}
                  <span class="gt-sel-pill"
                    class:gt-sel-pill--piloto={t.rolID === rolIdPiloto}
                    class:gt-sel-pill--copiloto={t.rolID === rolIdCopiloto}>
                    {t.nombre} {t.apellido}
                    <button class="gt-sel-rm" on:click={() => toggleTripulante(id)}>×</button>
                  </span>
                {/if}
              {/each}
            </div>
          {:else}
            <p class="gt-sin-selec">Ningún tripulante seleccionado.</p>
          {/if}

          <!-- Acciones rápidas -->
          <div class="gt-acciones-rapidas">
            <button class="btn-autorellenar"
              class:btn-autorellenar--listo={autoRellenadoListo}
              on:click={autoRellenar}
              disabled={guardando || autoRellenadoListo}>
              {#if autoRellenadoListo}
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><polyline points="20 6 9 17 4 12"/></svg>Auto-rellenado
              {:else}
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><path d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z"/><path d="M12 8v4l3 3"/></svg>Auto-rellenar
              {/if}
            </button>
            <button class="gt-btn-limpiar" on:click={limpiarTripulacion} disabled={guardando || seleccionados.length === 0}>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>Limpiar todo
            </button>
          </div>

          <!-- Selector por rol -->
          {#each rolesTripulacion as rol}
            {@const disp = todosTripulantes.filter(t => t.rolID === rol.id)}
            {#if disp.length > 0}
              <p class="gt-rol-label">{rol.nombre}s</p>
              <div class="vrc-tripulantes-grid">
                {#each disp as t}
                  {@const sel = seleccionados.includes(t.id)}
                  <button type="button"
                    class="vrc-trip-btn"
                    class:vrc-trip-btn--sel={sel}
                    on:click={() => toggleTripulante(t.id)}>
                    <span class="gt-trip-id">#{t.id}</span>
                    <span class="vrc-trip-nombre">{t.nombre} {t.apellido}</span>
                    <span class="vrc-trip-rol">{t.nombreRol}</span>
                  </button>
                {/each}
              </div>
            {/if}
          {/each}

        {/if}
      </div>

      <div class="modal__actions" style="padding:1rem 1.5rem;border-top:1px solid #e5e7eb">
        <button class="btn-primary"
          class:btn-danger--destacado={composicionValida}
          on:click={guardarTripulacion}
          disabled={guardando}>
          {guardando ? 'Guardando…' : 'Guardar tripulación'}
        </button>
        <button class="btn-secondary" on:click={cerrarModal} disabled={guardando}>Cancelar</button>
      </div>
    </div>
  </div>
{/if}

<style>
  /* Grid de vuelos */
  .gt-vuelos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1rem; }

  /* Tarjeta de vuelo */
  .gt-card { border: 1.5px solid #e5e7eb; border-radius: 12px; padding: 1rem; background: #fafafa; display: flex; flex-direction: column; gap: 0.6rem; }
  .gt-card--ok    { border-color: #86efac; background: #f0fdf4; }
  .gt-card--warn  { border-color: #fcd34d; background: #fffbeb; }
  .gt-card--empty { border-color: #e5e7eb; background: #f9fafb; }

  .gt-card__header { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
  .gt-card__num  { font-weight: 700; font-size: 0.9rem; color: #1C1A18; }
  .gt-card__ruta { font-size: 0.82rem; color: #374151; }
  .gt-badge { font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.45rem; border-radius: 4px; margin-left: auto; }
  .gt-badge--ok    { background: #dcfce7; color: #166534; }
  .gt-badge--warn  { background: #fef3c7; color: #92400e; }
  .gt-badge--empty { background: #f3f4f6; color: #6b7280; }

  .gt-card__meta { display: flex; gap: 0.75rem; font-size: 0.78rem; color: #6b7280; flex-wrap: wrap; }

  .gt-card__equipo { display: flex; flex-wrap: wrap; gap: 0.3rem; }
  .gt-pill { font-size: 0.72rem; font-weight: 600; padding: 0.15rem 0.5rem; border-radius: 999px; }
  .gt-pill--piloto   { background: #dbeafe; color: #1e40af; }
  .gt-pill--copiloto { background: #ede9fe; color: #5b21b6; }
  .gt-pill--auxiliar { background: #e0f2fe; color: #0369a1; }

  .gt-card__btn { align-self: flex-start; padding: 0.4rem 0.9rem; background: #1C1A18; color: #D4AF37; border: none; border-radius: 7px; font-size: 0.82rem; font-weight: 600; cursor: pointer; transition: background 0.15s; margin-top: 0.25rem; }
  .gt-card__btn:hover:not(:disabled) { background: #2d2b28; }
  .gt-card__btn--blocked { background: #9ca3af !important; color: #f3f4f6 !important; cursor: not-allowed; }
  .gt-card__btn:disabled { opacity: 0.85; cursor: not-allowed; }
  .gt-badge-blocked { background: #fef2f2; color: #b91c1c; font-size: 0.7rem; font-weight: 700; padding: 0.1rem 0.4rem; border-radius: 4px; border: 1px solid #fca5a5; }

  /* Buscador */
  .gt-search-wrap { position: relative; }
  .gt-search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 16px; height: 16px; color: #9ca3af; pointer-events: none; }
  .gt-search { width: 100%; padding: 9px 12px 9px 36px; font-size: 0.875rem; border: 1.5px solid #d1d5db; border-radius: 8px; background: #fff; box-sizing: border-box; }
  .gt-search:focus { outline: none; border-color: #6366f1; }

  /* Modal */
  .modal--gt { max-width: 680px; width: 96%; max-height: 90vh; display: flex; flex-direction: column; }
  .modal__body { overflow-y: auto; padding: 1.25rem 1.5rem; flex: 1; }

  /* Composición */
  .gt-composicion { display: flex; gap: 1rem; font-size: 0.82rem; font-weight: 600; flex-wrap: wrap; margin-bottom: 0.85rem; padding: 0.6rem 0.85rem; background: #f9fafb; border-radius: 8px; border: 1px solid #e5e7eb; align-items: center; }
  .comp-ok  { color: #166534; }
  .comp-err { color: #dc2626; }
  .gt-comp-ok  { margin-left: auto; color: #166534; font-size: 0.8rem; }
  .gt-comp-err { margin-left: auto; color: #dc2626; font-size: 0.8rem; }

  /* Seleccionados */
  .gt-seleccionados { display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.75rem; }
  .gt-sel-pill { display: inline-flex; align-items: center; gap: 0.3rem; padding: 0.2rem 0.5rem 0.2rem 0.65rem; border-radius: 999px; font-size: 0.78rem; font-weight: 600; background: #1C1A18; color: #F2EFEA; }
  .gt-sel-pill--piloto   { background: #1e40af; }
  .gt-sel-pill--copiloto { background: #5b21b6; }
  .gt-sel-rm { background: none; border: none; color: inherit; cursor: pointer; font-size: 0.9rem; opacity: 0.75; padding: 0; line-height: 1; }
  .gt-sel-rm:hover { opacity: 1; }
  .gt-sin-selec { font-size: 0.82rem; color: #9ca3af; font-style: italic; margin-bottom: 0.75rem; }

  /* Acciones rápidas */
  .gt-acciones-rapidas { display: flex; gap: 0.6rem; margin-bottom: 1rem; flex-wrap: wrap; }
  .btn-autorellenar { padding: 0.5rem 1rem; background: #D4AF37; color: #1C1A18; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 0.85rem; transition: all 0.2s; }
  .btn-autorellenar:hover:not(:disabled) { background: #b8962e; }
  .btn-autorellenar:disabled { opacity: 0.6; cursor: not-allowed; }
  .btn-autorellenar--listo { background: #d1fae5; color: #065f46; border: 1.5px solid #6ee7b7; opacity: 1 !important; }
  .gt-btn-limpiar { padding: 0.5rem 1rem; background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 0.85rem; transition: all 0.2s; }
  .gt-btn-limpiar:hover:not(:disabled) { background: #fca5a5; }
  .gt-btn-limpiar:disabled { opacity: 0.5; cursor: not-allowed; }

  /* Rol label */
  .gt-rol-label { font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #6b7280; margin: 0.75rem 0 0.4rem; }

  /* Grid de tripulantes (reutiliza estilos del componente AdminTripulantes) */
  .vrc-tripulantes-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 0.4rem; max-height: 200px; overflow-y: auto; margin-bottom: 0.5rem; }
  .vrc-trip-btn { display: flex; flex-direction: column; align-items: flex-start; gap: 0.1rem; padding: 0.45rem 0.65rem; border: 1.5px solid #e5e7eb; border-radius: 7px; background: #fff; cursor: pointer; font-size: 0.8rem; text-align: left; transition: all 0.15s; }
  .vrc-trip-btn:hover { border-color: #B89A7A; background: #fdf8f3; }
  .vrc-trip-btn--sel { border-color: #1C1A18; background: #1C1A18; color: #F2EFEA; }
  .vrc-trip-nombre { font-weight: 600; line-height: 1.2; }
  .vrc-trip-rol    { font-size: 0.7rem; opacity: 0.75; }
  .gt-trip-id      { font-size: 0.68rem; color: #9ca3af; font-family: monospace; }
  .vrc-trip-btn--sel .gt-trip-id { color: #D4AF37; }

  /* Overlay guardando */
  .gt-saving-overlay { position: absolute; inset: 0; z-index: 10; background: rgba(28,26,24,0.7); border-radius: inherit; display: flex; align-items: center; justify-content: center; }
  .autofill-overlay__box { display: flex; flex-direction: column; align-items: center; gap: 1rem; background: #fff; border-radius: 12px; padding: 2rem 2.5rem; max-width: 280px; width: 90%; box-shadow: 0 8px 32px rgba(0,0,0,0.25); text-align: center; }
  .autofill-spinner { width: 40px; height: 40px; border: 4px solid #e5e7eb; border-top-color: #D4AF37; border-radius: 50%; animation: spin 0.75s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg); } }
  .autofill-overlay__title { font-size: 0.95rem; font-weight: 700; color: #1C1A18; margin: 0; }
  .modal-loading { color: #6b7280; font-style: italic; }

  /* Botón guardar destacado */
  .btn-danger--destacado { background: #1C1A18 !important; box-shadow: 0 0 0 3px rgba(212,175,55,0.35); animation: pulsar 1.4s ease-in-out infinite; }
  @keyframes pulsar { 0%,100% { box-shadow: 0 0 0 3px rgba(212,175,55,0.25); } 50% { box-shadow: 0 0 0 6px rgba(212,175,55,0.08); } }
</style>
