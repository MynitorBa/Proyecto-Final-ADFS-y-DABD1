<script>
  import { onMount } from 'svelte';

  const API_BASE = 'http://localhost:7000';

  export let navigateTo = (page, data = null) => {};

  // ── Session ───────────────────────────────────────────────────────────────
  let username    = '';
  let usuarioId   = null;

  // ── Navegación ────────────────────────────────────────────────────────────
  let activeSection = 'agencias';

  // ── Agencias ──────────────────────────────────────────────────────────────
  let agencias          = [];
  let cargandoAgencias  = false;
  let errorAgencias     = null;
  let busquedaAgencia   = '';

  // Modal crear
  let showModalCrear  = false;
  let creando         = false;
  let mensajeCrear    = null;
  let nuevaAgencia    = { nombre: '', correo: '', porcentajeDescuento: 0 };

  // Confirmación de eliminar
  let showModalEliminar  = false;
  let agenciaEliminar    = null;
  let eliminando         = false;
  let mensajeEliminar    = null;

  // Feedback inline en tabla
  let mensajeTabla = null;   // { tipo: 'ok'|'error', texto: string }

  // ── Computed ──────────────────────────────────────────────────────────────
  $: agenciasFiltradas = agencias.filter(a =>
    a.nombre?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    a.correo?.toLowerCase().includes(busquedaAgencia.toLowerCase())
  );

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  onMount(async () => {
    await cargarSesion();
    await cargarAgencias();
  });

  async function cargarSesion() {
    try {
      const res = await fetch(`${API_BASE}/sesion`, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        username  = data.username ?? '';
        usuarioId = data.usuarioId ?? null;
      }
    } catch (_) {}
  }

  // ════════════════════════════════════════════════════
  //  AGENCIAS
  // ════════════════════════════════════════════════════

  async function cargarAgencias() {
    cargandoAgencias = true;
    errorAgencias    = null;
    try {
      const res = await fetch(`${API_BASE}/webservice/agencias`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      agencias = await res.json();
    } catch (e) {
      errorAgencias = 'No se pudo cargar la lista de agencias. ' + e.message;
    } finally {
      cargandoAgencias = false;
    }
  }

  function abrirModalCrear() {
    nuevaAgencia = { nombre: '', correo: '', porcentajeDescuento: 0 };
    mensajeCrear = null;
    showModalCrear = true;
  }

  function cerrarModalCrear() {
    showModalCrear = false;
  }

  async function crearAgencia() {
    if (!nuevaAgencia.nombre.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    if (!nuevaAgencia.correo.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El correo es obligatorio.' };
      return;
    }

    creando = true;
    mensajeCrear = null;
    try {
      const res = await fetch(`${API_BASE}/webservice/agencias`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre: nuevaAgencia.nombre.trim(),
          correo: nuevaAgencia.correo.trim(),
          porcentajeDescuento: Number(nuevaAgencia.porcentajeDescuento)
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      agencias = [...agencias, data];
      mensajeTabla = { tipo: 'ok', texto: `Agencia "${data.nombre}" creada correctamente.` };
      cerrarModalCrear();
    } catch (e) {
      mensajeCrear = { tipo: 'error', texto: e.message };
    } finally {
      creando = false;
    }
  }

  async function toggleEstado(ag) {
    const nuevoEstado = ag.estadoId === 1 ? 2 : 1;
    const texto       = nuevoEstado === 1 ? 'Activo' : 'Cerrado';
    mensajeTabla = null;
    try {
      const res = await fetch(`${API_BASE}/webservice/agencias/${ag.id}/estado`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estadoId: nuevoEstado })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      agencias = agencias.map(a =>
        a.id === ag.id ? { ...a, estadoId: nuevoEstado, estado: texto } : a
      );
      mensajeTabla = { tipo: 'ok', texto: `Estado cambiado a "${texto}" correctamente.` };
    } catch (e) {
      mensajeTabla = { tipo: 'error', texto: e.message };
    }
  }

  function abrirModalEliminar(ag) {
    agenciaEliminar = ag;
    mensajeEliminar = null;
    showModalEliminar = true;
  }

  function cerrarModalEliminar() {
    showModalEliminar = false;
    agenciaEliminar   = null;
  }

  async function confirmarEliminar() {
    if (!agenciaEliminar) return;
    eliminando = true;
    mensajeEliminar = null;
    try {
      const res = await fetch(`${API_BASE}/webservice/agencias/${agenciaEliminar.id}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      agencias     = agencias.filter(a => a.id !== agenciaEliminar.id);
      mensajeTabla = { tipo: 'ok', texto: `Agencia eliminada correctamente.` };
      cerrarModalEliminar();
    } catch (e) {
      mensajeEliminar = { tipo: 'error', texto: e.message };
    } finally {
      eliminando = false;
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  function handleKeyOverlay(e, fn) {
    if (e.key === 'Escape') fn();
  }

  function badgeClass(estadoId) {
    return estadoId === 1 ? 'ws__badge--activo' : 'ws__badge--cerrado';
  }

  async function salir() {
    try {
      await fetch(`${API_BASE}/auth/logout`, { method: 'POST', credentials: 'include' });
    } catch (_) {}
    navigateTo('home');
  }
</script>

<div class="ws">

  <!-- ═══════════════════════ SIDEBAR ═══════════════════════ -->
  <aside class="ws__sidebar">

    <!-- Logo -->
    <div class="ws__sidebar-header">
      <div class="ws__sidebar-logo">
        <div class="ws__sidebar-logo-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0d1117" stroke-width="2.5">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
        </div>
        <div>
          <p class="ws__sidebar-logo-title">MikuInn</p>
          <p class="ws__sidebar-logo-sub">Portal Webservice</p>
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="ws__sidebar-nav">
      <button
        class="ws__nav-btn {activeSection === 'agencias' ? 'ws__nav-btn--active' : ''}"
        on:click={() => activeSection = 'agencias'}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        Mis Agencias
      </button>
    </nav>

    <!-- Footer: usuario + salir -->
    <div class="ws__sidebar-footer">
      <div class="ws__user-badge">
        <div class="ws__user-avatar">{username.charAt(0).toUpperCase()}</div>
        <div>
          <p class="ws__user-name">@{username}</p>
          <p class="ws__user-role">Webservice</p>
        </div>
      </div>
      <button class="ws__btn-exit" on:click={salir}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Salir del panel
      </button>
    </div>
  </aside>

  <!-- ═══════════════════════ MAIN ═══════════════════════ -->
  <main class="ws__main">

    <!-- ═══ SECCIÓN: AGENCIAS ═══ -->
    {#if activeSection === 'agencias'}

      <div class="ws__page-header">
        <h1 class="ws__page-title">Mis Agencias</h1>
        <p class="ws__page-sub">Agencias asociadas a tu cuenta webservice. Puedes añadir, activar/cerrar o eliminarlas.</p>
      </div>

      <!-- Toolbar -->
      <div class="ws__toolbar">
        <div class="ws__search-box">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input type="text" placeholder="Buscar por nombre o correo…" bind:value={busquedaAgencia} />
        </div>

        <button class="ws__btn ws__btn--primary" on:click={abrirModalCrear}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Nueva agencia
        </button>

        <button class="ws__btn ws__btn--ghost" on:click={cargarAgencias} disabled={cargandoAgencias}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
            class={cargandoAgencias ? 'ws__spinner' : ''}>
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Recargar
        </button>
      </div>

      <!-- Feedback inline -->
      {#if mensajeTabla}
        <div class="ws__feedback ws__feedback--{mensajeTabla.tipo}" style="margin-bottom:1rem">
          {#if mensajeTabla.tipo === 'ok'}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          {:else}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {/if}
          {mensajeTabla.texto}
        </div>
      {/if}

      <!-- Error de carga -->
      {#if errorAgencias}
        <div class="ws__error-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {errorAgencias}
        </div>
      {/if}

      <!-- Loading -->
      {#if cargandoAgencias}
        <div class="ws__loading">
          <svg class="ws__spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Cargando agencias…
        </div>

      <!-- Tabla -->
      {:else}
        <div class="ws__table-card">
          <div class="ws__table-wrap">
            <table class="ws__table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Correo</th>
                  <th>Descuento %</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {#if agenciasFiltradas.length === 0}
                  <tr>
                    <td colspan="6">
                      <div class="ws__empty">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                          <polyline points="9 22 9 12 15 12 15 22"/>
                        </svg>
                        <p>No hay agencias registradas</p>
                        <span>Haz clic en "Nueva agencia" para añadir una.</span>
                      </div>
                    </td>
                  </tr>
                {:else}
                  {#each agenciasFiltradas as ag (ag.id)}
                    <tr>
                      <td style="color: var(--ws-muted); font-size:.8rem">#{ag.id}</td>
                      <td style="font-weight:600">{ag.nombre}</td>
                      <td style="color: var(--ws-muted)">{ag.correo}</td>
                      <td>
                        <span style="font-weight:600; color: var(--ws-accent)">
                          {ag.porcentajeDescuento?.toFixed(2)}%
                        </span>
                      </td>
                      <td>
                        <span class="ws__badge {badgeClass(ag.estadoId)}">
                          <span style="width:6px;height:6px;border-radius:50%;background:currentColor;display:inline-block"></span>
                          {ag.estado}
                        </span>
                      </td>
                      <td>
                        <div class="ws__actions">
                          <!-- Botón toggle estado -->
                          {#if ag.estadoId === 1}
                            <button
                              class="ws__icon-btn ws__icon-btn--toggle-active"
                              on:click={() => toggleEstado(ag)}
                              title="Cambiar a Cerrado"
                            >
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"/>
                                <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                              </svg>
                            </button>
                          {:else}
                            <button
                              class="ws__icon-btn ws__icon-btn--toggle-cerrado"
                              on:click={() => toggleEstado(ag)}
                              title="Cambiar a Activo"
                            >
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="20 6 9 17 4 12"/>
                              </svg>
                            </button>
                          {/if}

                          <!-- Eliminar -->
                          <button
                            class="ws__icon-btn ws__icon-btn--danger"
                            on:click={() => abrirModalEliminar(ag)}
                            title="Eliminar agencia"
                          >
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                              <polyline points="3 6 5 6 21 6"/>
                              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                              <path d="M10 11v6"/><path d="M14 11v6"/>
                              <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                            </svg>
                          </button>
                        </div>
                      </td>
                    </tr>
                  {/each}
                {/if}
              </tbody>
            </table>
          </div>
        </div>
      {/if}

    {/if}<!-- /agencias -->

  </main>
</div>

<!-- ═══ MODAL: CREAR AGENCIA ═══ -->
{#if showModalCrear}
  <div
    class="ws__overlay"
    on:click={cerrarModalCrear}
    on:keydown={e => handleKeyOverlay(e, cerrarModalCrear)}
    role="button" tabindex="-1" aria-label="Cerrar"
  ></div>

  <div class="ws__modal" role="dialog" aria-modal="true" aria-labelledby="modal-crear-titulo">

    <div class="ws__modal-header">
      <p class="ws__modal-title" id="modal-crear-titulo">Nueva Agencia</p>
      <button class="ws__modal-close" on:click={cerrarModalCrear} aria-label="Cerrar">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <div class="ws__modal-body">

      <div class="ws__form-group">
        <label class="ws__form-label" for="ag-nombre">Nombre de la agencia *</label>
        <input
          id="ag-nombre"
          class="ws__form-input"
          type="text"
          placeholder="Ej: Agencia Turística Central"
          bind:value={nuevaAgencia.nombre}
        />
      </div>

      <div class="ws__form-group">
        <label class="ws__form-label" for="ag-correo">Correo electrónico *</label>
        <input
          id="ag-correo"
          class="ws__form-input"
          type="email"
          placeholder="agencia@ejemplo.com"
          bind:value={nuevaAgencia.correo}
        />
      </div>

      <div class="ws__form-group">
        <label class="ws__form-label" for="ag-descuento">Porcentaje de descuento (0–100)</label>
        <input
          id="ag-descuento"
          class="ws__form-input"
          type="number"
          min="0"
          max="100"
          step="0.01"
          placeholder="0.00"
          bind:value={nuevaAgencia.porcentajeDescuento}
        />
      </div>

      {#if mensajeCrear}
        <div class="ws__feedback ws__feedback--{mensajeCrear.tipo}">
          {#if mensajeCrear.tipo === 'ok'}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          {:else}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {/if}
          {mensajeCrear.texto}
        </div>
      {/if}

    </div>

    <div class="ws__modal-footer">
      <button class="ws__btn ws__btn--ghost" on:click={cerrarModalCrear} disabled={creando}>Cancelar</button>
      <button class="ws__btn ws__btn--primary" on:click={crearAgencia} disabled={creando}>
        {#if creando}
          <svg class="ws__spinner" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Guardando…
        {:else}
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          Crear agencia
        {/if}
      </button>
    </div>

  </div>
{/if}

<!-- ═══ MODAL: CONFIRMAR ELIMINAR ═══ -->
{#if showModalEliminar && agenciaEliminar}
  <div
    class="ws__overlay"
    on:click={cerrarModalEliminar}
    on:keydown={e => handleKeyOverlay(e, cerrarModalEliminar)}
    role="button" tabindex="-1" aria-label="Cerrar"
  ></div>

  <div class="ws__modal" role="dialog" aria-modal="true">

    <div class="ws__modal-header" style="background: rgba(248,81,73,0.08)">
      <p class="ws__modal-title" style="color: var(--ws-red)">Eliminar Agencia</p>
      <button class="ws__modal-close" on:click={cerrarModalEliminar} aria-label="Cerrar">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <div class="ws__modal-body">
      <p style="color: var(--ws-text); margin:0; font-size:.9rem; line-height:1.5">
        ¿Estás seguro de que deseas eliminar la agencia
        <strong style="color: var(--ws-accent)">{agenciaEliminar.nombre}</strong>?
        Esta acción no se puede deshacer.
      </p>

      {#if mensajeEliminar}
        <div class="ws__feedback ws__feedback--error">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {mensajeEliminar.texto}
        </div>
      {/if}
    </div>

    <div class="ws__modal-footer">
      <button class="ws__btn ws__btn--ghost" on:click={cerrarModalEliminar} disabled={eliminando}>Cancelar</button>
      <button class="ws__btn ws__btn--danger" on:click={confirmarEliminar} disabled={eliminando}>
        {#if eliminando}
          <svg class="ws__spinner" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Eliminando…
        {:else}
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
          </svg>
          Sí, eliminar
        {/if}
      </button>
    </div>

  </div>
{/if}