<script>
// @ts-nocheck
  import { onMount } from 'svelte';

  export let API;
  export let mostrarToast;
  export let mostrarConfirm;

  // ── Estado principal ──────────────────────────────────────────
  let agencias          = [];
  let usuariosDisponibles = []; // webservice sin agencia
  let cargando          = false;

  // ── Modal crear agencia ───────────────────────────────────────
  let modalCrear        = false;
  let creando           = false;
  let crearNombre       = '';
  let crearCorreo       = '';
  let crearUsuarioId    = '';
  let crearDescuento    = 0;
  let crearErrores      = {};

  // ── Modal asignar usuario ─────────────────────────────────────
  let modalAsignar      = false;
  let asignando         = false;
  let agenciaSeleccionada = null;
  let asignarUsuarioId  = '';

  // ── Modal editar descuento ────────────────────────────────────
  let modalDescuento    = false;
  let guardandoDescuento = false;
  let descuentoEditando = 0;
  let agenciaDescuento  = null;

  // ── Helpers ───────────────────────────────────────────────────
  const estadoOpciones = [
    { id: 1, label: 'Activa',     class: 'badge--active'    },
    { id: 2, label: 'Inactiva',   class: 'badge--inactive'  },
    { id: 3, label: 'Suspendida', class: 'badge--suspended' },
  ];
  const estadoInfo = (id) => estadoOpciones.find(e => e.id === id) ?? { label: 'Desconocido', class: '' };

  // ── Carga de datos ────────────────────────────────────────────
  onMount(() => { cargarTodo(); });

  async function cargarTodo() {
    cargando = true;
    try {
      const [rAgencias, rUsuarios] = await Promise.all([
        fetch(`${API}/api/agencias/todas`,                 { credentials: 'include' }),
        fetch(`${API}/api/agencias/webservice-disponibles`,{ credentials: 'include' }),
      ]);
      if (rAgencias.ok)  agencias           = await rAgencias.json();
      else mostrarToast('error', 'Error al cargar agencias.');
      if (rUsuarios.ok)  usuariosDisponibles = await rUsuarios.json();
    } catch { mostrarToast('error', 'Error de conexión.'); }
    finally  { cargando = false; }
  }

  async function recargarUsuariosDisponibles() {
    try {
      const r = await fetch(`${API}/api/agencias/webservice-disponibles`, { credentials: 'include' });
      if (r.ok) usuariosDisponibles = await r.json();
    } catch {}
  }

  // ── Crear agencia ─────────────────────────────────────────────
  function abrirModalCrear() {
    crearNombre = ''; crearCorreo = ''; crearUsuarioId = ''; crearDescuento = 0;
    crearErrores = {};
    modalCrear = true;
  }

  function validarCrear() {
    crearErrores = {};
    if (!crearNombre.trim()) crearErrores.nombre = 'Requerido.';
    if (!crearCorreo.trim()) crearErrores.correo = 'Requerido.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(crearCorreo)) crearErrores.correo = 'Correo inválido.';
    if (!crearUsuarioId) crearErrores.usuario = 'Debes seleccionar un usuario Webservice.';
    if (crearDescuento < 0 || crearDescuento > 100) crearErrores.descuento = 'Entre 0 y 100.';
    return Object.keys(crearErrores).length === 0;
  }

  async function handleCrearAgencia() {
    if (!validarCrear()) return;
    creando = true;
    try {
      const r = await fetch(`${API}/api/agencias`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:              crearNombre.trim(),
          correo:              crearCorreo.trim(),
          usuarioWebID:        parseInt(crearUsuarioId),
          porcentajeDescuento: parseFloat(crearDescuento),
        })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Agencia creada correctamente.');
        modalCrear = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al crear la agencia.');
      }
    } catch { mostrarToast('error', 'Error de conexión.'); }
    finally { creando = false; }
  }

  // ── Asignar usuario ───────────────────────────────────────────
  function abrirModalAsignar(agencia) {
    agenciaSeleccionada = agencia;
    asignarUsuarioId    = '';
    modalAsignar        = true;
  }

  async function handleAsignarUsuario() {
    if (!asignarUsuarioId) { mostrarToast('error', 'Selecciona un usuario.'); return; }
    asignando = true;
    try {
      const r = await fetch(`${API}/api/agencias/${agenciaSeleccionada.id}/asignar-usuario`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioWebId: parseInt(asignarUsuarioId) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Usuario asignado correctamente.');
        modalAsignar = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al asignar usuario.');
      }
    } catch { mostrarToast('error', 'Error de conexión.'); }
    finally { asignando = false; }
  }

  // ── Editar descuento ──────────────────────────────────────────
  function abrirModalDescuento(agencia) {
    agenciaDescuento  = agencia;
    descuentoEditando = agencia.porcentajeDescuento;
    modalDescuento    = true;
  }

  async function handleGuardarDescuento() {
    if (descuentoEditando < 0 || descuentoEditando > 100) {
      mostrarToast('error', 'El descuento debe estar entre 0 y 100.'); return;
    }
    guardandoDescuento = true;
    try {
      const r = await fetch(`${API}/api/agencias/${agenciaDescuento.id}/descuento`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ descuento: parseFloat(descuentoEditando) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Descuento actualizado.');
        modalDescuento = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al actualizar descuento.');
      }
    } catch { mostrarToast('error', 'Error de conexión.'); }
    finally { guardandoDescuento = false; }
  }

  // ── Cambiar estado (inline select) ───────────────────────────
  async function handleCambiarEstado(agencia, nuevoEstadoId) {
    try {
      const r = await fetch(`${API}/api/agencias/${agencia.id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estadoId: parseInt(nuevoEstadoId) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Estado actualizado.');
        agencias = agencias.map(a => a.id === agencia.id ? { ...a, estadoAgenciaID: parseInt(nuevoEstadoId) } : a);
      } else {
        mostrarToast('error', data.message || 'Error al cambiar estado.');
        await cargarTodo();
      }
    } catch { mostrarToast('error', 'Error de conexión.'); }
  }

  // ── Stats ─────────────────────────────────────────────────────
  $: totalAgencias  = agencias.length;
  $: totalActivas   = agencias.filter(a => a.estadoAgenciaID === 1).length;
  $: totalInactivas = agencias.filter(a => a.estadoAgenciaID !== 1).length;
  $: sinUsuario     = agencias.filter(a => !a.usuarioWebID).length;
</script>

<!-- ══════════════════ MODAL CREAR AGENCIA ══════════════════ -->
{#if modalCrear}
  <div class="ag-overlay" on:click={() => modalCrear = false} role="dialog" aria-modal="true">
    <div class="ag-modal" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Nueva Agencia</h3>
        <button class="ag-modal__close" on:click={() => modalCrear = false}>×</button>
      </div>

      <div class="ag-modal__body">
        <div class="ag-field">
          <label class="ag-field__label">Nombre <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.nombre}
            type="text" bind:value={crearNombre} placeholder="Agencia Viajes GT" maxlength="120" />
          {#if crearErrores.nombre}<p class="ag-field__err">{crearErrores.nombre}</p>{/if}
        </div>

        <div class="ag-field">
          <label class="ag-field__label">Correo <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.correo}
            type="email" bind:value={crearCorreo} placeholder="agencia@ejemplo.com" maxlength="200" />
          {#if crearErrores.correo}<p class="ag-field__err">{crearErrores.correo}</p>{/if}
        </div>

        <div class="ag-field">
          <label class="ag-field__label">
            Usuario Webservice <span class="ag-required">*</span>
          </label>
          {#if usuariosDisponibles.length === 0}
            <div class="ag-notice ag-notice--warn">
              No hay usuarios Webservice disponibles sin agencia asignada.
            </div>
          {:else}
            <select class="ag-field__select" class:ag-field__input--err={crearErrores.usuario}
              bind:value={crearUsuarioId}>
              <option value="">— Seleccionar usuario —</option>
              {#each usuariosDisponibles as u}
                <option value={u.id}>{u.nombre} (@{u.username}) · {u.correo}</option>
              {/each}
            </select>
          {/if}
          {#if crearErrores.usuario}<p class="ag-field__err">{crearErrores.usuario}</p>{/if}
        </div>

        <div class="ag-field">
          <label class="ag-field__label">Descuento inicial (%)</label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.descuento}
            type="number" min="0" max="100" step="0.01" bind:value={crearDescuento} />
          {#if crearErrores.descuento}<p class="ag-field__err">{crearErrores.descuento}</p>{/if}
        </div>
      </div>

      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalCrear = false} disabled={creando}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleCrearAgencia} disabled={creando || usuariosDisponibles.length === 0}>
          {creando ? 'Creando...' : 'Crear Agencia'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- ══════════════════ MODAL ASIGNAR USUARIO ════════════════ -->
{#if modalAsignar}
  <div class="ag-overlay" on:click={() => modalAsignar = false} role="dialog" aria-modal="true">
    <div class="ag-modal ag-modal--sm" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Asignar Usuario</h3>
        <button class="ag-modal__close" on:click={() => modalAsignar = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">
          Agencia: <strong>{agenciaSeleccionada?.nombre}</strong>
        </p>
        {#if usuariosDisponibles.length === 0}
          <div class="ag-notice ag-notice--warn">
            No hay usuarios Webservice disponibles sin agencia asignada.
          </div>
        {:else}
          <div class="ag-field">
            <label class="ag-field__label">Usuario Webservice disponible</label>
            <select class="ag-field__select" bind:value={asignarUsuarioId}>
              <option value="">— Seleccionar —</option>
              {#each usuariosDisponibles as u}
                <option value={u.id}>{u.nombre} (@{u.username})</option>
              {/each}
            </select>
          </div>
        {/if}
      </div>
      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalAsignar = false} disabled={asignando}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleAsignarUsuario}
          disabled={asignando || !asignarUsuarioId || usuariosDisponibles.length === 0}>
          {asignando ? 'Asignando...' : 'Asignar'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- ══════════════════ MODAL DESCUENTO ══════════════════════ -->
{#if modalDescuento}
  <div class="ag-overlay" on:click={() => modalDescuento = false} role="dialog" aria-modal="true">
    <div class="ag-modal ag-modal--sm" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Editar Descuento</h3>
        <button class="ag-modal__close" on:click={() => modalDescuento = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">Agencia: <strong>{agenciaDescuento?.nombre}</strong></p>
        <div class="ag-field">
          <label class="ag-field__label">Porcentaje de descuento (0 – 100)</label>
          <input class="ag-field__input" type="number" min="0" max="100" step="0.01"
            bind:value={descuentoEditando} />
        </div>
      </div>
      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalDescuento = false} disabled={guardandoDescuento}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleGuardarDescuento} disabled={guardandoDescuento}>
          {guardandoDescuento ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- ══════════════════ CONTENIDO PRINCIPAL ══════════════════ -->
<section class="admin-section">

  <!-- Header de sección -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Agencias</h2>
      <p class="admin-section__subtitle">Crea y gestiona las agencias Webservice</p>
    </div>
    <div style="display:flex; gap:.75rem;">
      <button class="btn-add" on:click={cargarTodo} style="background:#4b5563">
        ↻ Actualizar
      </button>
      <button class="btn-add" on:click={abrirModalCrear}>
        + Nueva Agencia
      </button>
    </div>
  </div>

  <!-- Stats -->
  <div class="ag-stats">
    <div class="ag-stat">
      <span class="ag-stat__num">{totalAgencias}</span>
      <span class="ag-stat__lbl">Total</span>
    </div>
    <div class="ag-stat ag-stat--green">
      <span class="ag-stat__num">{totalActivas}</span>
      <span class="ag-stat__lbl">Activas</span>
    </div>
    <div class="ag-stat ag-stat--gray">
      <span class="ag-stat__num">{totalInactivas}</span>
      <span class="ag-stat__lbl">Inactivas / Susp.</span>
    </div>
    <div class="ag-stat ag-stat--warn">
      <span class="ag-stat__num">{sinUsuario}</span>
      <span class="ag-stat__lbl">Sin usuario</span>
    </div>
  </div>

  <!-- Tabla -->
  {#if cargando}
    <p class="loading-text">Cargando agencias...</p>

  {:else if agencias.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay agencias registradas todavía.</p>
    </div>

  {:else}
    <div class="vuelos-table">
      <table class="table">
        <thead class="table__head">
          <tr>
            <th class="table__header">ID</th>
            <th class="table__header">Nombre</th>
            <th class="table__header">Correo</th>
            <th class="table__header">Usuario Webservice</th>
            <th class="table__header">Descuento</th>
            <th class="table__header">Estado</th>
            <th class="table__header">Acciones</th>
          </tr>
        </thead>
        <tbody class="table__body">
          {#each agencias as ag}
            <tr class="table__row">
              <td class="table__cell" data-label="ID">#{ag.id}</td>

              <td class="table__cell" data-label="Nombre">
                <strong>{ag.nombre}</strong>
              </td>

              <td class="table__cell" data-label="Correo">
                <span style="font-size:.82rem; color:var(--text-muted)">{ag.correo}</span>
              </td>

              <td class="table__cell" data-label="Usuario">
                {#if ag.usuarioWebID}
                  <div class="ag-user-cell">
                    <span class="ag-user-cell__name">{ag.usuarioWebNombre}</span>
                    <span class="ag-user-cell__user">@{ag.usuarioWebUsername}</span>
                  </div>
                {:else}
                  <span class="ag-sin-usuario">Sin asignar</span>
                {/if}
              </td>

              <td class="table__cell" data-label="Descuento">
                <button class="ag-discount-btn" on:click={() => abrirModalDescuento(ag)}
                  title="Editar descuento">
                  {ag.porcentajeDescuento}%
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
              </td>

              <td class="table__cell" data-label="Estado">
                <select class="ag-estado-select ag-estado--{ag.estadoAgenciaID}"
                  value={ag.estadoAgenciaID}
                  on:change={(e) => handleCambiarEstado(ag, e.target.value)}>
                  {#each estadoOpciones as op}
                    <option value={op.id}>{op.label}</option>
                  {/each}
                </select>
              </td>

              <td class="table__cell" data-label="Acciones">
                <div class="table__actions">
                  <button class="table__action-btn ag-btn-asignar"
                    on:click={() => abrirModalAsignar(ag)}
                    title="Asignar usuario Webservice">
                    👤 Asignar usuario
                  </button>
                </div>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}

</section>