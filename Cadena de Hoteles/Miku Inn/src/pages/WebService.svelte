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
  // Modal crear
  let showModalCrear  = false;
  let creando         = false;
  let mensajeCrear    = null;
  let nuevaAgencia    = { nombre: '', correo: '' };

  // Feedback inline
  let mensajeTabla = null;

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
    nuevaAgencia = { nombre: '', correo: '' };
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
          correo: nuevaAgencia.correo.trim()
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

    {#if activeSection === 'agencias'}

      <!-- ══ Cargando ══ -->
      {#if cargandoAgencias}
        <div class="ws__loading-full">
          <svg class="ws__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          <p>Cargando…</p>
        </div>

      <!-- ══ Error ══ -->
      {:else if errorAgencias}
        <div class="ws__error-full">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#f85149" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p>{errorAgencias}</p>
          <button class="ws__btn ws__btn--ghost" on:click={cargarAgencias}>Reintentar</button>
        </div>

      <!-- ══ SIN AGENCIA → pantalla de registro ══ -->
      {:else if agencias.length === 0}
        <div class="ws__onboarding">
          <div class="ws__onboarding-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
            </svg>
          </div>
          <h2 class="ws__onboarding-title">Aún no tienes agencia</h2>
          <p class="ws__onboarding-sub">Registra tu agencia para comenzar a operar en la plataforma Miku Inn.</p>
          <button class="ws__btn ws__btn--primary ws__btn--lg" on:click={abrirModalCrear}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Registrar mi agencia
          </button>
        </div>

      <!-- ══ CON AGENCIA → tarjeta de detalle ══ -->
      {:else}
        {@const ag = agencias[0]}

        <div class="ws__agency-view">

          <!-- Header de sección -->
          <div class="ws__page-header" style="margin-bottom:1.5rem">
            <div>
              <h1 class="ws__page-title">Mi Agencia</h1>
              <p class="ws__page-sub">Información de tu agencia registrada en la plataforma.</p>
            </div>
            <button class="ws__btn ws__btn--ghost ws__btn--sm" on:click={cargarAgencias} disabled={cargandoAgencias}>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                class={cargandoAgencias ? 'ws__spinner' : ''}>
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
              Recargar
            </button>
          </div>

          {#if mensajeTabla}
            <div class="ws__feedback ws__feedback--{mensajeTabla.tipo}" style="margin-bottom:1.25rem">
              {#if mensajeTabla.tipo === 'ok'}
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              {:else}
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/></svg>
              {/if}
              {mensajeTabla.texto}
            </div>
          {/if}

          <!-- Tarjeta principal -->
          <div class="ws__agency-card">

            <!-- Banner superior con nombre y estado -->
            <div class="ws__agency-banner">
              <div class="ws__agency-banner-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#0d1117" stroke-width="2">
                  <path d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                </svg>
              </div>
              <div class="ws__agency-banner-info">
                <h2 class="ws__agency-name">{ag.nombre}</h2>
                <p class="ws__agency-id">ID #{ag.id}</p>
              </div>
              <span class="ws__badge ws__badge--lg {badgeClass(ag.estadoId)}">
                <span class="ws__badge-dot"></span>
                {ag.estado}
              </span>
            </div>

            <!-- Grid de datos -->
            <div class="ws__agency-grid">

              <div class="ws__agency-stat">
                <div class="ws__agency-stat-icon ws__agency-stat-icon--teal">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                </div>
                <div>
                  <p class="ws__agency-stat-label">Correo</p>
                  <p class="ws__agency-stat-value">{ag.correo}</p>
                </div>
              </div>

              <div class="ws__agency-stat">
                <div class="ws__agency-stat-icon ws__agency-stat-icon--amber">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="12" y1="1" x2="12" y2="23"/>
                    <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
                  </svg>
                </div>
                <div>
                  <p class="ws__agency-stat-label">Descuento asignado</p>
                  <p class="ws__agency-stat-value ws__agency-stat-value--accent">{ag.porcentajeDescuento?.toFixed(2)}%</p>
                  <p class="ws__agency-stat-hint">Asignado por el administrador</p>
                </div>
              </div>

              <div class="ws__agency-stat">
                <div class="ws__agency-stat-icon ws__agency-stat-icon--blue">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                    <line x1="7" y1="7" x2="7.01" y2="7"/>
                  </svg>
                </div>
                <div>
                  <p class="ws__agency-stat-label">Estado actual</p>
                  <p class="ws__agency-stat-value">{ag.estado}</p>
                </div>
              </div>

            </div>

            <!-- Acción: cambiar estado -->
            <div class="ws__agency-actions">
              {#if ag.estadoId === 1}
                <button class="ws__btn ws__btn--warning" on:click={() => toggleEstado(ag)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                  </svg>
                  Cerrar agencia temporalmente
                </button>
              {:else}
                <button class="ws__btn ws__btn--primary" on:click={() => toggleEstado(ag)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  Reactivar agencia
                </button>
              {/if}
            </div>

          </div><!-- /ws__agency-card -->

        </div><!-- /ws__agency-view -->

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

      <div class="ws__form-group" style="background:rgba(45,212,191,0.06);border:1px solid rgba(45,212,191,0.15);border-radius:8px;padding:0.6rem 0.875rem">
        <p style="margin:0;font-size:0.78rem;color:var(--ws-muted)">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2dd4bf" stroke-width="2" style="vertical-align:middle;margin-right:4px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="8"/><line x1="12" y1="12" x2="12" y2="16"/></svg>
          El porcentaje de descuento inicia en <strong style="color:#2dd4bf">0%</strong> y solo el administrador puede modificarlo.
        </p>
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