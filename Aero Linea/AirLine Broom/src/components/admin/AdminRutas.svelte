<script>
/**
 * @file AdminRutas.svelte
 * @description Seccion del panel de administracion para gestionar rutas de vuelo entre aeropuertos.
 * Muestra un toggle de tabs (Activas / Inactivas) que cambia la vista con un solo clic. La duracion
 * estimada de rutas activas puede editarse inline. Nuevas rutas se crean con un modal.
 */
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  export let API;
  export let aeropuertos = [];
  export let mostrarToast;

  const dispatch = createEventDispatcher();

  let rutas        = [];
  let loadingRutas = false;

  /** Vista activa del toggle: 'activas' | 'inactivas' */
  let vistaRutas = 'activas';

  let filtroRutas      = '';
  let editandoRutaId   = null;
  let rutaDuracionEdit = '';
  let guardandoDuracion = false;

  let mostrarModalCrearRuta = false;
  let nuevaRuta  = { origenId: '', destinoId: '', duracion: 120 };
  let creandoRuta = false;

  let desactivandoRutaId     = null;
  let guardandoDesactivacion = false;
  let activandoRutaId        = null;
  let guardandoActivacion    = false;

  // Listas separadas por estado
  $: rutasActivas   = rutas.filter(r => r.activo !== false);
  $: rutasInactivas = rutas.filter(r => r.activo === false);
  $: totalActivas   = rutasActivas.length;
  $: totalInactivas = rutasInactivas.length;

  // La lista que se muestra según el tab activo, filtrada por búsqueda
  $: rutasMostradas = vistaRutas === 'activas' ? rutasActivas : rutasInactivas;
  $: rutasFiltradas = rutasMostradas.filter(r =>
    !filtroRutas || [r.codigoOrigen, r.origen, r.codigoDestino, r.destino]
      .some(v => v?.toLowerCase().includes(filtroRutas.toLowerCase()))
  );

  // Dropdowns del modal
  let busquedaOrigenModal  = '';
  let busquedaDestinoModal = '';
  let mostrarDropdownOrigenModal  = false;
  let mostrarDropdownDestinoModal = false;

  $: aeropuertosFiltradosOrigenModal = busquedaOrigenModal.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaOrigenModal.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaOrigenModal.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaOrigenModal.toLowerCase())
      ).slice(0, 10);

  $: aeropuertosFiltradosDestinoModal = busquedaDestinoModal.length < 2
    ? aeropuertos.filter(a => a.id !== parseInt(nuevaRuta.origenId)).slice(0, 5)
    : aeropuertos.filter(a =>
        a.id !== parseInt(nuevaRuta.origenId) && (
          a.nombre.toLowerCase().includes(busquedaDestinoModal.toLowerCase()) ||
          a.codigo.toLowerCase().includes(busquedaDestinoModal.toLowerCase()) ||
          a.ciudad.toLowerCase().includes(busquedaDestinoModal.toLowerCase())
        )
      ).slice(0, 10);

  $: aeropuertoOrigenSeleccionado  = aeropuertos.find(a => a.id === parseInt(nuevaRuta.origenId));
  $: aeropuertoDestinoSeleccionado = aeropuertos.find(a => a.id === parseInt(nuevaRuta.destinoId));

  function seleccionarOrigenModal(a) {
    nuevaRuta.origenId = a.id;
    busquedaOrigenModal = `${a.codigo} — ${a.nombre}`;
    mostrarDropdownOrigenModal = false;
    if (parseInt(nuevaRuta.destinoId) === a.id) { nuevaRuta.destinoId = ''; busquedaDestinoModal = ''; }
  }
  function seleccionarDestinoModal(a) {
    nuevaRuta.destinoId = a.id;
    busquedaDestinoModal = `${a.codigo} — ${a.nombre}`;
    mostrarDropdownDestinoModal = false;
  }

  onMount(() => { cargarRutas(); });

  async function cargarRutas() {
    loadingRutas = true;
    try {
      const r = await fetch(`${API}/api/rutas`, { credentials: 'include' });
      if (r.ok) rutas = await r.json();
      else mostrarToast('error', 'Error al cargar rutas');
    } catch { mostrarToast('error', 'Error de conexion al cargar rutas'); }
    finally { loadingRutas = false; }
  }

  async function handleCrearRuta() {
    if (!nuevaRuta.origenId)  { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevaRuta.destinoId) { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (nuevaRuta.origenId === nuevaRuta.destinoId) { mostrarToast('error', 'El origen y destino no pueden ser el mismo'); return; }
    if (!nuevaRuta.duracion || nuevaRuta.duracion <= 0) { mostrarToast('error', 'Ingresa una duracion valida'); return; }
    creandoRuta = true;
    try {
      const r = await fetch(`${API}/api/rutas`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ origenId: parseInt(nuevaRuta.origenId), destinoId: parseInt(nuevaRuta.destinoId), duracionEstimada: parseInt(nuevaRuta.duracion) })
      });
      if (r.ok) {
        mostrarToast('success', '¡Ruta creada correctamente!');
        mostrarModalCrearRuta = false;
        nuevaRuta = { origenId: '', destinoId: '', duracion: 120 };
        busquedaOrigenModal = ''; busquedaDestinoModal = '';
        await cargarRutas();
        dispatch('rutaCreada');
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al crear la ruta');
      }
    } catch { mostrarToast('error', 'Error de conexion al crear la ruta'); }
    finally { creandoRuta = false; }
  }

  async function guardarDuracionRuta(rutaId) {
    const minutos = parseInt(rutaDuracionEdit);
    if (!minutos || minutos <= 0) { mostrarToast('error', 'La duracion debe ser mayor a 0 minutos'); return; }
    guardandoDuracion = true;
    try {
      const r = await fetch(`${API}/api/rutas/${rutaId}/duracion`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ duracionEstimada: minutos })
      });
      if (r.ok) {
        mostrarToast('success', 'Duracion actualizada correctamente');
        editandoRutaId = null; rutaDuracionEdit = '';
        await cargarRutas();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al actualizar la duracion');
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { guardandoDuracion = false; }
  }

  async function desactivarRuta(rutaId) {
    guardandoDesactivacion = true;
    try {
      const r = await fetch(`${API}/api/rutas/${rutaId}/desactivar`, { method: 'PUT', credentials: 'include' });
      const data = await r.json();
      if (r.ok) { mostrarToast('success', data.message || 'Ruta desactivada correctamente'); desactivandoRutaId = null; await cargarRutas(); }
      else mostrarToast('error', data.message || 'No se pudo desactivar la ruta');
    } catch { mostrarToast('error', 'Error de conexion al desactivar la ruta'); }
    finally { guardandoDesactivacion = false; }
  }

  async function activarRuta(rutaId) {
    guardandoActivacion = true;
    try {
      const r = await fetch(`${API}/api/rutas/${rutaId}/activar`, { method: 'PUT', credentials: 'include' });
      const data = await r.json();
      if (r.ok) { mostrarToast('success', data.message || 'Ruta activada correctamente'); activandoRutaId = null; await cargarRutas(); }
      else mostrarToast('error', data.message || 'No se pudo activar la ruta');
    } catch { mostrarToast('error', 'Error de conexion al activar la ruta'); }
    finally { guardandoActivacion = false; }
  }

  function abrirModalCrearRuta() {
    nuevaRuta = { origenId: '', destinoId: '', duracion: 120 };
    busquedaOrigenModal = ''; busquedaDestinoModal = '';
    mostrarModalCrearRuta = true;
  }

  // Al cambiar de tab limpiar la búsqueda y el edit inline
  function cambiarVista(vista) {
    vistaRutas = vista;
    filtroRutas = '';
    editandoRutaId = null;
    rutaDuracionEdit = '';
  }
</script>

<section class="admin-section">
  <!-- Encabezado -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Rutas</h2>
      <p class="admin-section__subtitle">
        Edita la duracion estimada de rutas activas. La hora de llegada se calcula usando las zonas horarias de cada aeropuerto.
      </p>
    </div>
    <div style="display:flex;gap:.75rem">
      <button class="btn-add" on:click={abrirModalCrearRuta}>+ Nueva Ruta</button>
      <button class="btn-add" on:click={cargarRutas} style="background:#4b5563">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>Actualizar
      </button>
    </div>
  </div>

  {#if loadingRutas}
    <p class="loading-text">Cargando rutas...</p>

  {:else if rutas.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">
        No hay rutas registradas. Crea una con <strong>+ Nueva Ruta</strong> o selecciona aeropuertos al crear un vuelo.
      </p>
    </div>

  {:else}
    <!-- Aviso TZ -->
    <div class="rutas-tz-note">
      <span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" style="vertical-align:-3px;flex-shrink:0"><path d="M9 18h6M10 22h4M12 2a7 7 0 0 1 4 12.74V17H8v-2.26A7 7 0 0 1 12 2z"/></svg></span>
      <span>
        Las rutas con <strong><svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="vertical-align:-1px;margin-right:1px"><path d="M1.5 6l3 3 6-6"/></svg>TZ</strong> en ambos aeropuertos calculan la hora de llegada con conversion de zona horaria real.
        Si un aeropuerto no tiene timezone, editalo en <em>Gestionar Aeropuertos</em>.
      </span>
    </div>

    <!-- Toggle tabs -->
    <div class="admin-filter-bar">
      <div class="filtro-tabs">
        <button
          class="filtro-tab"
          class:filtro-tab--active={vistaRutas === 'activas'}
          on:click={() => cambiarVista('activas')}
          type="button">
          <svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="vertical-align:-1px"><path d="M1.5 6l3 3 6-6"/></svg>
          Activas
          <span class="filtro-tab__count">{totalActivas}</span>
        </button>
        <button
          class="filtro-tab"
          class:filtro-tab--active={vistaRutas === 'inactivas'}
          on:click={() => cambiarVista('inactivas')}
          type="button">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:-1px"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>
          Inactivas
          <span class="filtro-tab__count">{totalInactivas}</span>
        </button>
      </div>
    </div>

    <!-- Barra de búsqueda -->
    <div class="admin-search-bar">
      <input
        type="text"
        bind:value={filtroRutas}
        placeholder="Buscar por código IATA o ciudad (ej: GUA, Madrid, MIA)..."
        class="admin-search-input"
      />
      <span class="admin-search-count">
        {rutasFiltradas.length} de {rutasMostradas.length} ruta{rutasMostradas.length !== 1 ? 's' : ''}
      </span>
    </div>

    <!-- Tabla -->
    {#if rutasFiltradas.length === 0}
      <div class="placeholder-card">
        <p class="placeholder-card__text">
          {#if filtroRutas}
            No hay rutas {vistaRutas} que coincidan con "{filtroRutas}".
          {:else}
            No hay rutas {vistaRutas}.
          {/if}
        </p>
      </div>
    {:else}
      <table class="table">
        <thead class="table__head">
          <tr>
            <th class="table__header">Origen</th>
            <th class="table__header">Destino</th>
            <th class="table__header">TZ Origen</th>
            <th class="table__header">TZ Destino</th>
            <th class="table__header">Duracion (min)</th>
            <th class="table__header">Vuelos</th>
            <th class="table__header">Acciones</th>
          </tr>
        </thead>
        <tbody class="table__body">
          {#each rutasFiltradas as ruta}
            <tr class="table__row" style={vistaRutas === 'inactivas' ? 'opacity:0.7' : ''}>
              <td class="table__cell">
                <span class="ruta-code">{ruta.codigoOrigen}</span>
                <span class="ruta-name">{ruta.origen}</span>
              </td>
              <td class="table__cell">
                <span class="ruta-code">{ruta.codigoDestino}</span>
                <span class="ruta-name">{ruta.destino}</span>
              </td>
              <td class="table__cell">
                {#if ruta.zonaHorariaOrigen}
                  <span class="tz-badge tz-badge--ok"><svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="vertical-align:-1px;margin-right:2px"><path d="M1.5 6l3 3 6-6"/></svg>{ruta.zonaHorariaOrigen}</span>
                {:else}
                  <span class="tz-badge tz-badge--missing">Sin TZ</span>
                {/if}
              </td>
              <td class="table__cell">
                {#if ruta.zonaHorariaDestino}
                  <span class="tz-badge tz-badge--ok"><svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="vertical-align:-1px;margin-right:2px"><path d="M1.5 6l3 3 6-6"/></svg>{ruta.zonaHorariaDestino}</span>
                {:else}
                  <span class="tz-badge tz-badge--missing">Sin TZ</span>
                {/if}
              </td>
              <td class="table__cell">
                {#if vistaRutas === 'activas' && editandoRutaId === ruta.id}
                  <div class="duracion-edit">
                    <input type="number" class="form-input duracion-input" min="1" max="10000"
                      bind:value={rutaDuracionEdit} placeholder="min" />
                    <button class="table__action-btn table__action-btn--view"
                      disabled={guardandoDuracion}
                      on:click={() => guardarDuracionRuta(ruta.id)}>
                      {#if guardandoDuracion}...{:else}<svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M1.5 6l3 3 6-6"/></svg>{/if}
                    </button>
                    <button class="table__action-btn table__action-btn--cancel"
                      on:click={() => { editandoRutaId = null; rutaDuracionEdit = ''; }}>
                      <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:-1px"><path d="M2 2l8 8M10 2L2 10"/></svg>
                    </button>
                  </div>
                {:else}
                  <span class="duracion-display">
                    <strong>{ruta.duracionEstimada}</strong> min
                    ({Math.floor(ruta.duracionEstimada / 60)}h {ruta.duracionEstimada % 60}m)
                  </span>
                {/if}
              </td>
              <td class="table__cell">{ruta.totalVuelos}</td>
              <td class="table__cell" style="display:flex;gap:.4rem;flex-wrap:wrap">
                {#if vistaRutas === 'activas'}
                  {#if editandoRutaId !== ruta.id}
                    <button class="table__action-btn table__action-btn--view"
                      on:click={() => { editandoRutaId = ruta.id; rutaDuracionEdit = String(ruta.duracionEstimada); }}>
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>Editar duracion
                    </button>
                    <button class="table__action-btn table__action-btn--cancel"
                      on:click={() => desactivandoRutaId = ruta.id}
                      title="Desactivar ruta">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>Desactivar
                    </button>
                  {/if}
                {:else}
                  <button class="table__action-btn" style="background:#16a34a;color:#fff;border-color:#16a34a"
                    on:click={() => activandoRutaId = ruta.id}
                    title="Reactivar esta ruta">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>Activar
                  </button>
                {/if}
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  {/if}
</section>

<!-- Dialog: desactivar ruta -->
{#if desactivandoRutaId !== null}
  {@const rutaADesactivar = rutas.find(r => r.id === desactivandoRutaId)}
  <div class="modal-overlay" on:click={() => desactivandoRutaId = null} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:420px">
      <div class="modal__header">
        <h3 class="modal__title" style="color:#ef4444">Desactivar Ruta</h3>
        <button class="modal__close" on:click={() => desactivandoRutaId = null}>×</button>
      </div>
      <div style="padding:1.25rem 1.5rem">
        <p style="font-size:.95rem;color:var(--text-primary);margin-bottom:.75rem">
          ¿Deseas desactivar la ruta <strong>{rutaADesactivar?.codigoOrigen} → {rutaADesactivar?.codigoDestino}</strong>?
        </p>
        <p style="font-size:.85rem;color:var(--text-muted);margin-bottom:1.5rem">
          Solo es posible si la ruta no tiene reservaciones activas (Pendiente o Confirmada). Una ruta inactiva no acepta nuevos vuelos.
        </p>
        <div class="modal__actions">
          <button class="btn-primary" style="background:#ef4444"
            disabled={guardandoDesactivacion}
            on:click={() => desactivarRuta(desactivandoRutaId)}>
            {guardandoDesactivacion ? 'Desactivando...' : 'Sí, desactivar'}
          </button>
          <button class="btn-secondary" on:click={() => desactivandoRutaId = null}>Cancelar</button>
        </div>
      </div>
    </div>
  </div>
{/if}

<!-- Dialog: activar ruta -->
{#if activandoRutaId !== null}
  {@const rutaAActivar = rutas.find(r => r.id === activandoRutaId)}
  <div class="modal-overlay" on:click={() => activandoRutaId = null} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:420px">
      <div class="modal__header">
        <h3 class="modal__title" style="color:#16a34a">Activar Ruta</h3>
        <button class="modal__close" on:click={() => activandoRutaId = null}>×</button>
      </div>
      <div style="padding:1.25rem 1.5rem">
        <p style="font-size:.95rem;color:var(--text-primary);margin-bottom:.75rem">
          ¿Deseas reactivar la ruta <strong>{rutaAActivar?.codigoOrigen} → {rutaAActivar?.codigoDestino}</strong>?
        </p>
        <p style="font-size:.85rem;color:var(--text-muted);margin-bottom:1.5rem">
          La ruta volvera a estar disponible para crear nuevos vuelos.
        </p>
        <div class="modal__actions">
          <button class="btn-primary" style="background:#16a34a"
            disabled={guardandoActivacion}
            on:click={() => activarRuta(activandoRutaId)}>
            {guardandoActivacion ? 'Activando...' : 'Sí, activar'}
          </button>
          <button class="btn-secondary" on:click={() => activandoRutaId = null}>Cancelar</button>
        </div>
      </div>
    </div>
  </div>
{/if}

<!-- Modal: crear nueva ruta -->
{#if mostrarModalCrearRuta}
  <div class="modal-overlay" on:click={() => mostrarModalCrearRuta = false} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:480px">
      <div class="modal__header">
        <h3 class="modal__title">Crear Nueva Ruta</h3>
        <button class="modal__close" on:click={() => mostrarModalCrearRuta = false}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleCrearRuta}>
        <p style="font-size:.88rem;color:var(--text-muted);margin-bottom:.5rem">
          Una ruta define el trayecto entre dos aeropuertos y su duracion estimada.
        </p>

        <div class="form-field">
          <label class="form-label">Aeropuerto de Origen *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={busquedaOrigenModal}
              on:focus={() => mostrarDropdownOrigenModal = true}
              on:blur={() => setTimeout(() => mostrarDropdownOrigenModal = false, 200)}
              placeholder="Buscar aeropuerto de origen..."
              autocomplete="off" />
            {#if mostrarDropdownOrigenModal && aeropuertosFiltradosOrigenModal.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaOrigenModal.length < 2}
                  <div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>
                {/if}
                {#each aeropuertosFiltradosOrigenModal as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarOrigenModal(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoOrigenSeleccionado}
              <p class="selected-item"><svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="vertical-align:-1px;margin-right:3px"><path d="M1.5 6l3 3 6-6"/></svg>{aeropuertoOrigenSeleccionado.codigo} — {aeropuertoOrigenSeleccionado.nombre}</p>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label class="form-label">Aeropuerto de Destino *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={busquedaDestinoModal}
              on:focus={() => mostrarDropdownDestinoModal = true}
              on:blur={() => setTimeout(() => mostrarDropdownDestinoModal = false, 200)}
              placeholder="Buscar aeropuerto de destino..."
              autocomplete="off" />
            {#if mostrarDropdownDestinoModal && aeropuertosFiltradosDestinoModal.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaDestinoModal.length < 2}
                  <div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>
                {/if}
                {#each aeropuertosFiltradosDestinoModal as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarDestinoModal(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoDestinoSeleccionado}
              <p class="selected-item"><svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" style="vertical-align:-1px;margin-right:3px"><path d="M1.5 6l3 3 6-6"/></svg>{aeropuertoDestinoSeleccionado.codigo} — {aeropuertoDestinoSeleccionado.nombre}</p>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label for="ar-duracion" class="form-label">Duracion Estimada (minutos) *</label>
          <input id="ar-duracion" type="number" class="form-input"
            bind:value={nuevaRuta.duracion} min="1" max="10000" placeholder="Ej: 180 para 3 horas" required />
          {#if nuevaRuta.duracion > 0}
            <small class="img-hint">≈ {Math.floor(nuevaRuta.duracion / 60)}h {nuevaRuta.duracion % 60}m</small>
          {/if}
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary" disabled={creandoRuta}>
            {creandoRuta ? 'Creando...' : 'Crear Ruta'}
          </button>
          <button type="button" class="btn-secondary" on:click={() => mostrarModalCrearRuta = false}>Cancelar</button>
        </div>
      </form>
    </div>
  </div>
{/if}

<style>
.admin-filter-bar { margin-bottom: 1rem; }
.filtro-tabs { display: flex; gap: 0; border: 1px solid #d1d5db; border-radius: 8px; overflow: hidden; width: fit-content; }
.filtro-tab { display: flex; align-items: center; gap: 0.45rem; padding: 0.4rem 1.1rem; font-size: 0.875rem; font-weight: 600; border: none; background: #f9fafb; color: #6b7280; cursor: pointer; transition: all 0.18s; }
.filtro-tab:hover:not(.filtro-tab--active) { background: #f3f4f6; color: #374151; }
.filtro-tab--active { background: #1C1A18; color: #D4AF37; }
.filtro-tab__count { display: inline-flex; align-items: center; justify-content: center; min-width: 1.4rem; height: 1.4rem; padding: 0 0.35rem; border-radius: 999px; font-size: 0.7rem; font-weight: 700; }
.filtro-tab--active .filtro-tab__count { background: rgba(212,175,55,0.25); color: #D4AF37; }
.filtro-tab:not(.filtro-tab--active) .filtro-tab__count { background: #e5e7eb; color: #374151; }

.admin-search-bar { display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem; }
.admin-search-input { flex: 1; padding: 10px 14px; border: 1.5px solid #ddd; border-radius: 8px; font-size: 0.9rem; outline: none; transition: border-color 0.2s; }
.admin-search-input:focus { border-color: var(--primary-color, #7a5c3f); }
.admin-search-count { font-size: 0.8rem; color: #888; white-space: nowrap; }
</style>
