<script>
// @ts-nocheck
/**
 * @file AdminGestionAvionesVuelo.svelte
 * @description Panel para cambiar el avión asignado a cada vuelo activo.
 * Muestra tarjetas de vuelo con el avión actual y permite reemplazarlo
 * por uno disponible que tenga capacidad suficiente para los boletos ya vendidos.
 * El cambio requiere al menos 48h de anticipación.
 */
  import { onMount } from 'svelte';

  export let API;
  export let mostrarToast;
  export let mostrarConfirm;

  // ── Vuelos ──────────────────────────────────────────────────────────────
  let vuelos         = [];
  let loadingVuelos  = false;
  let filtroBusqueda = '';

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
        const todos = await r.json();
        const hoy   = new Date().toISOString().split('T')[0];
        vuelos = todos
          .filter(v => (v.estado === 'Activo' || v.estado === 'En curso') && v.fecha >= hoy)
          .sort((a, b) => a.fecha.localeCompare(b.fecha) || a.horaSalida.localeCompare(b.horaSalida));
      } else {
        mostrarToast('error', 'Error al cargar vuelos');
      }
    } catch { mostrarToast('error', 'Error de conexión'); }
    finally { loadingVuelos = false; }
  }

  // ── Aviones ──────────────────────────────────────────────────────────────
  let todosAviones = [];

  async function cargarAviones() {
    try {
      const r = await fetch(`${API}/api/aviones`, { credentials: 'include' });
      if (r.ok) todosAviones = (await r.json()).filter(a => a.activo !== false);
    } catch { console.error('Error cargando aviones'); }
  }

  onMount(() => Promise.all([cargarVuelos(), cargarAviones()]));

  // ── Utilidad: horas hasta el vuelo ──────────────────────────────────────
  function horasHastaVuelo(v) {
    const salidaStr = `${v.fecha}T${v.horaSalida}:00`;
    return (new Date(salidaStr) - new Date()) / (1000 * 60 * 60);
  }

  // ── Modal ────────────────────────────────────────────────────────────────
  let vueloGestionar        = null;
  let mostrarModal          = false;
  let guardando             = false;
  let avionSeleccionadoId   = null;
  let avionesOcupados       = new Set();
  let cargandoDisponibilidad = false;
  let busquedaAvion         = '';

  $: avionActual = todosAviones.find(a => a.id === vueloGestionar?.avionId);

  $: avionesDisponibles = todosAviones.filter(a => {
    // Excluir ocupados, excepto el actual del vuelo
    if (avionesOcupados.has(a.id) && a.id !== vueloGestionar?.avionId) return false;
    // Excluir aviones con menor capacidad que el actual (integridad de ruta)
    if (avionActual && a.capacidadPasajeros < avionActual.capacidadPasajeros) return false;
    // Filtrar por búsqueda
    if (busquedaAvion.trim()) {
      const q = busquedaAvion.trim().toLowerCase();
      return (a.nombreCompleto ?? `${a.marca} ${a.modelo}`).toLowerCase().includes(q);
    }
    return true;
  });

  $: avionSeleccionado = todosAviones.find(a => a.id === avionSeleccionadoId);
  $: vendidos          = vueloGestionar?.boletosVendidosReal ?? 0;
  $: capacidadVsVendidos = avionSeleccionado ? avionSeleccionado.capacidadPasajeros >= vendidos : false;
  $: capacidadVsAvionAnterior = avionSeleccionado && avionActual ? avionSeleccionado.capacidadPasajeros >= avionActual.capacidadPasajeros : true;
  $: capacidadOk       = capacidadVsVendidos && capacidadVsAvionAnterior;
  $: mismoAvion        = avionSeleccionadoId === vueloGestionar?.avionId;

  async function abrirModal(vuelo) {
    vueloGestionar       = vuelo;
    avionSeleccionadoId  = vuelo.avionId ?? null;
    busquedaAvion        = '';
    mostrarModal         = true;
    cargandoDisponibilidad = true;
    try {
      const url = `${API}/api/admin/vuelos/aviones-ocupados?fecha=${vuelo.fecha}&horaSalida=${vuelo.horaSalida}&excluirVueloId=${vuelo.id}`;
      const r   = await fetch(url, { credentials: 'include' });
      if (r.ok) avionesOcupados = new Set(await r.json());
    } catch { console.error('Error cargando disponibilidad'); }
    finally { cargandoDisponibilidad = false; }
  }

  function cerrarModal() {
    mostrarModal         = false;
    vueloGestionar       = null;
    avionSeleccionadoId  = null;
    avionesOcupados      = new Set();
    busquedaAvion        = '';
  }

  async function guardarCambio() {
    if (!avionSeleccionadoId) {
      mostrarToast('error', 'Selecciona un avión');
      return;
    }
    if (mismoAvion) {
      mostrarToast('error', 'El avión seleccionado es el mismo que el actual');
      return;
    }
    if (!capacidadOk) {
      let mensajeError = '';
      if (!capacidadVsVendidos) {
        mensajeError = `${avionSeleccionado?.nombreCompleto ?? 'El avión'} tiene capacidad para ` +
          `${avionSeleccionado?.capacidadPasajeros} pasajeros, pero hay ${vendidos} boleto(s) vendido(s)`;
      } else if (!capacidadVsAvionAnterior) {
        const capacidadActual = avionActual?.capacidadPasajeros ?? 0;
        mensajeError = `No se puede cambiar a un avión con menor capacidad. ` +
          `Avión actual: ${avionActual?.nombreCompleto ?? `${avionActual?.marca} ${avionActual?.modelo}`} (${capacidadActual} pasajeros). ` +
          `Avión seleccionado: ${avionSeleccionado?.nombreCompleto ?? `${avionSeleccionado?.marca} ${avionSeleccionado?.modelo}`} (${avionSeleccionado?.capacidadPasajeros} pasajeros)`;
      }
      mostrarToast('error', mensajeError);
      return;
    }

    guardando = true;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/${vueloGestionar.id}/avion`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ avionId: avionSeleccionadoId })
      });
      if (r.ok) {
        const nombre = avionSeleccionado?.nombreCompleto
          ?? `${avionSeleccionado?.marca} ${avionSeleccionado?.modelo}`;
        mostrarToast('success', 'Avión actualizado correctamente');
        vuelos = vuelos.map(v => v.id === vueloGestionar.id
          ? { ...v, avionId: avionSeleccionadoId, avionNombre: nombre }
          : v);
        cerrarModal();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al actualizar el avión');
      }
    } catch { mostrarToast('error', 'Error de conexión'); }
    finally { guardando = false; }
  }
</script>

<section class="admin-section">
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Aviones por Vuelo</h2>
      <p class="admin-section__subtitle">Cambia el avión asignado a vuelos activos (mínimo 48h de anticipación)</p>
    </div>
    <button class="btn-add" style="background:#4b5563"
      on:click={() => Promise.all([cargarVuelos(), cargarAviones()])}>
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
        stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px">
        <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
        <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
      </svg>Actualizar
    </button>
  </div>

  <!-- Buscador -->
  <div class="gav-search-wrap" style="margin-bottom:1rem">
    <svg class="gav-search-icon" viewBox="0 0 20 20" fill="currentColor">
      <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
    </svg>
    <input type="text" class="gav-search" bind:value={filtroBusqueda}
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
    <div class="gav-vuelos-grid">
      {#each vuelosFiltrados as v}
        {@const horas    = horasHastaVuelo(v)}
        {@const bloqueado = horas < 48}
        <div class="gav-card" class:gav-card--bloqueado={bloqueado}>

          <!-- Encabezado -->
          <div class="gav-card__header">
            <span class="gav-card__num">{v.numeroVuelo}</span>
            <span class="gav-card__ruta">{v.origen} → {v.destino}</span>
            {#if bloqueado}
              <span class="gav-badge gav-badge--blocked">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:2px"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>&lt;48h
              </span>
            {:else}
              <span class="gav-badge gav-badge--ok">Editable</span>
            {/if}
          </div>

          <!-- Meta del vuelo -->
          <div class="gav-card__meta">
            <span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>{v.fecha}
            </span>
            <span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>{v.horaSalida}
            </span>
          </div>

          <!-- Avión actual -->
          <div class="gav-avion-actual">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:5px;color:#6b7280"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>
            <span class="gav-avion-nombre">{v.avionNombre || 'Sin avión'}</span>
            <span class="gav-avion-cap">
              {#if v.asientosTotales > 0}· {v.asientosTotales} pax{/if}
            </span>
          </div>

          {#if v.boletosVendidosReal > 0}
            <div class="gav-vendidos">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/><path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z"/></svg>
              {v.boletosVendidosReal} boleto(s) vendido(s)
            </div>
          {/if}

          <button class="gav-card__btn" class:gav-card__btn--blocked={bloqueado}
            disabled={bloqueado} on:click={() => abrirModal(v)}>
            {#if bloqueado}
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>Bloqueado (&lt;48h)
            {:else}
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>Cambiar avión
            {/if}
          </button>
        </div>
      {/each}
    </div>
  {/if}
</section>

<!-- Modal -->
{#if mostrarModal && vueloGestionar}
  <div class="modal-overlay" role="dialog" aria-modal="true">
    <div class="modal modal--gav" on:click|stopPropagation>

      {#if guardando}
        <div class="gav-saving-overlay">
          <div class="gav-saving-box">
            <div class="gav-spinner"></div>
            <p class="gav-saving-title">Guardando cambio de avión…</p>
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

        <!-- Avión actual -->
        <p class="gav-section-label">Avión actual</p>
        <div class="gav-actual-card">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="color:#6b7280;flex-shrink:0"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>
          <div>
            <p class="gav-actual-nombre">{vueloGestionar.avionNombre || 'Sin avión asignado'}</p>
            {#if vueloGestionar.asientosTotales > 0}
              <p class="gav-actual-cap">Capacidad: {vueloGestionar.asientosTotales} pasajeros</p>
            {/if}
          </div>
          {#if vendidos > 0}
            <span class="gav-vendidos-badge">{vendidos} vendido(s)</span>
          {/if}
        </div>

        {#if vendidos > 0}
          <p class="gav-hint">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            El nuevo avión debe tener capacidad para al menos <strong>{vendidos}</strong> pasajero(s).
          </p>
        {/if}

        <!-- Avión seleccionado -->
        {#if avionSeleccionado && !mismoAvion}
          <div class="gav-nuevo-card" class:gav-nuevo-card--ok={capacidadOk} class:gav-nuevo-card--err={!capacidadOk}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>
            <div style="flex:1">
              <p style="font-weight:700;font-size:0.88rem;margin:0">{avionSeleccionado.nombreCompleto ?? `${avionSeleccionado.marca} ${avionSeleccionado.modelo}`}</p>
              <p style="font-size:0.78rem;color:#6b7280;margin:0">Capacidad: {avionSeleccionado.capacidadPasajeros} pasajeros</p>
            </div>
            {#if capacidadOk}
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#166534" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            {:else}
              <span style="color:#b91c1c;font-size:0.75rem;font-weight:700">Sin capacidad</span>
            {/if}
          </div>
        {/if}

        <!-- Buscador de aviones -->
        <p class="gav-section-label" style="margin-top:1rem">Seleccionar nuevo avión</p>
        <div class="gav-search-wrap" style="margin-bottom:0.5rem">
          <svg class="gav-search-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input type="text" class="gav-search" bind:value={busquedaAvion} placeholder="Buscar avión..." />
        </div>

        {#if cargandoDisponibilidad}
          <p class="gav-loading">Verificando disponibilidad…</p>
        {:else}
          <div class="gav-aviones-grid">
            {#each avionesDisponibles as a}
              {@const ocupado  = avionesOcupados.has(a.id) && a.id !== vueloGestionar.avionId}
              {@const esActual = a.id === vueloGestionar.avionId}
              {@const sinCap   = vendidos > 0 && a.capacidadPasajeros < vendidos}
              {@const sel      = a.id === avionSeleccionadoId}
              <button type="button"
                class="gav-avion-btn"
                class:gav-avion-btn--sel={sel}
                class:gav-avion-btn--actual={esActual}
                class:gav-avion-btn--sinCap={sinCap && !esActual}
                disabled={ocupado}
                on:click={() => avionSeleccionadoId = a.id}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0"><path d="M21 16v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>
                <span class="gav-avion-btn__nombre">{a.nombreCompleto ?? `${a.marca} ${a.modelo}`}</span>
                <span class="gav-avion-btn__cap">{a.capacidadPasajeros} pax</span>
                {#if esActual}
                  <span class="gav-tag gav-tag--actual">Actual</span>
                {:else if ocupado}
                  <span class="gav-tag gav-tag--ocupado">Ocupado</span>
                {:else if sinCap}
                  <span class="gav-tag gav-tag--sincap">Sin cap.</span>
                {/if}
              </button>
            {/each}
            {#if avionesDisponibles.length === 0}
              <p class="gav-loading">No hay aviones disponibles para esta fecha.</p>
            {/if}
          </div>
        {/if}

      </div>

      <div class="modal__actions" style="padding:1rem 1.5rem;border-top:1px solid #e5e7eb">
        <button class="btn-primary" on:click={guardarCambio}
          disabled={guardando || mismoAvion || !avionSeleccionadoId || !capacidadOk}>
          {guardando ? 'Guardando…' : 'Confirmar cambio de avión'}
        </button>
        <button class="btn-secondary" on:click={cerrarModal} disabled={guardando}>Cancelar</button>
      </div>
    </div>
  </div>
{/if}

<style>
  /* Grid de vuelos */
  .gav-vuelos-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; }

  /* Tarjeta */
  .gav-card { border: 1.5px solid #e5e7eb; border-radius: 12px; padding: 1rem; background: #fafafa; display: flex; flex-direction: column; gap: 0.55rem; }
  .gav-card--bloqueado { background: #f9fafb; border-color: #d1d5db; opacity: 0.85; }

  .gav-card__header { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
  .gav-card__num    { font-weight: 700; font-size: 0.9rem; color: #1C1A18; }
  .gav-card__ruta   { font-size: 0.82rem; color: #374151; flex: 1; }

  .gav-badge { font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.45rem; border-radius: 4px; }
  .gav-badge--ok      { background: #dcfce7; color: #166534; }
  .gav-badge--blocked { background: #fef2f2; color: #b91c1c; }

  .gav-card__meta { display: flex; gap: 0.75rem; font-size: 0.78rem; color: #6b7280; }

  .gav-avion-actual { display: flex; align-items: center; gap: 0.4rem; font-size: 0.82rem; }
  .gav-avion-nombre { font-weight: 600; color: #1C1A18; }
  .gav-avion-cap    { color: #9ca3af; font-size: 0.75rem; }

  .gav-vendidos { font-size: 0.75rem; color: #92400e; background: #fef3c7; border: 1px solid #fcd34d; border-radius: 5px; padding: 0.2rem 0.5rem; display: inline-flex; align-items: center; width: fit-content; }

  .gav-card__btn { align-self: flex-start; padding: 0.4rem 0.9rem; background: #1C1A18; color: #D4AF37; border: none; border-radius: 7px; font-size: 0.82rem; font-weight: 600; cursor: pointer; transition: background 0.15s; margin-top: 0.15rem; display: flex; align-items: center; }
  .gav-card__btn:hover:not(:disabled) { background: #2d2b28; }
  .gav-card__btn:disabled { cursor: not-allowed; }
  .gav-card__btn--blocked { background: #9ca3af !important; color: #f9fafb !important; }

  /* Buscador */
  .gav-search-wrap  { position: relative; }
  .gav-search-icon  { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 16px; height: 16px; color: #9ca3af; pointer-events: none; }
  .gav-search       { width: 100%; padding: 9px 12px 9px 36px; font-size: 0.875rem; border: 1.5px solid #d1d5db; border-radius: 8px; background: #fff; box-sizing: border-box; }
  .gav-search:focus { outline: none; border-color: #6366f1; }

  /* Modal */
  .modal--gav { max-width: 600px; width: 96%; max-height: 90vh; display: flex; flex-direction: column; position: relative; }
  .modal__body { overflow-y: auto; padding: 1.25rem 1.5rem; flex: 1; }

  .gav-section-label { font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #6b7280; margin: 0 0 0.4rem; }

  /* Avión actual */
  .gav-actual-card { display: flex; align-items: center; gap: 0.75rem; padding: 0.75rem 1rem; background: #f9fafb; border: 1.5px solid #e5e7eb; border-radius: 10px; margin-bottom: 0.5rem; }
  .gav-actual-nombre { font-weight: 700; font-size: 0.9rem; color: #1C1A18; margin: 0; }
  .gav-actual-cap    { font-size: 0.78rem; color: #6b7280; margin: 0; }
  .gav-vendidos-badge { margin-left: auto; background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; border-radius: 5px; font-size: 0.72rem; font-weight: 700; padding: 0.2rem 0.5rem; white-space: nowrap; }

  .gav-hint { font-size: 0.78rem; color: #6b7280; margin-bottom: 0.75rem; display: flex; align-items: center; }

  /* Avión nuevo seleccionado */
  .gav-nuevo-card { display: flex; align-items: center; gap: 0.6rem; padding: 0.65rem 0.9rem; border-radius: 8px; border: 1.5px solid; margin-bottom: 0.75rem; }
  .gav-nuevo-card--ok  { background: #f0fdf4; border-color: #86efac; }
  .gav-nuevo-card--err { background: #fef2f2; border-color: #fca5a5; }

  /* Grid de aviones seleccionables */
  .gav-aviones-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 0.4rem; max-height: 240px; overflow-y: auto; }
  .gav-avion-btn { display: flex; align-items: center; gap: 0.4rem; padding: 0.5rem 0.65rem; border: 1.5px solid #e5e7eb; border-radius: 8px; background: #fff; cursor: pointer; font-size: 0.8rem; text-align: left; transition: all 0.15s; }
  .gav-avion-btn:hover:not(:disabled) { border-color: #B89A7A; background: #fdf8f3; }
  .gav-avion-btn--sel    { border-color: #1C1A18; background: #1C1A18; color: #F2EFEA; }
  .gav-avion-btn--actual { border-color: #6366f1; background: #eef2ff; }
  .gav-avion-btn--sinCap { opacity: 0.55; }
  .gav-avion-btn:disabled { opacity: 0.4; cursor: not-allowed; }
  .gav-avion-btn__nombre { font-weight: 600; flex: 1; line-height: 1.2; font-size: 0.78rem; }
  .gav-avion-btn__cap    { font-size: 0.7rem; color: #6b7280; white-space: nowrap; }
  .gav-avion-btn--sel .gav-avion-btn__cap { color: #D4AF37; }

  .gav-tag { font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.35rem; border-radius: 3px; white-space: nowrap; }
  .gav-tag--actual  { background: #e0e7ff; color: #4338ca; }
  .gav-tag--ocupado { background: #fef2f2; color: #b91c1c; }
  .gav-tag--sincap  { background: #fef3c7; color: #92400e; }

  /* Overlay guardando */
  .gav-saving-overlay { position: fixed; inset: 0; z-index: 9999; background: rgba(28,26,24,0.65); display: flex; align-items: center; justify-content: center; }
  .gav-saving-box     { display: flex; flex-direction: column; align-items: center; gap: 1rem; background: #fff; border-radius: 12px; padding: 2rem 2.5rem; }
  .gav-spinner        { width: 38px; height: 38px; border: 4px solid #e5e7eb; border-top-color: #D4AF37; border-radius: 50%; animation: gavSpin 0.75s linear infinite; }
  @keyframes gavSpin  { to { transform: rotate(360deg); } }
  .gav-saving-title   { font-size: 0.9rem; font-weight: 700; color: #1C1A18; margin: 0; }

  .gav-loading { font-size: 0.82rem; color: #9ca3af; font-style: italic; padding: 0.5rem 0; }
</style>
