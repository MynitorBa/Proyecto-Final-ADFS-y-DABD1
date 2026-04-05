<script>
// @ts-nocheck
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';
  import '../styles/admin.css';
  import '../styles/miagencia.css';

  export let navigateTo;

  // ── Estado ────────────────────────────────────────────────────
  let cargando      = true;
  let enviando      = false;
  let tieneAgencia  = false;
  let agencia       = null;
  let errorGlobal   = '';
  let toast         = null; // { tipo: 'success'|'error', mensaje }

  // Formulario de creación
  let nombre = '';
  let correo = '';
  let errores = { nombre: '', correo: '' };

  // ── Carga inicial ─────────────────────────────────────────────
  onMount(async () => {
    await cargarMiAgencia();
  });

  async function cargarMiAgencia() {
    cargando = true;
    errorGlobal = '';
    try {
      const r = await fetch(`${API}/api/agencias/mi-agencia`, { credentials: 'include' });
      if (r.status === 401 || r.status === 403) {
        navigateTo('acceso-denegado');
        return;
      }
      if (!r.ok) { errorGlobal = 'Error al consultar tu agencia.'; return; }
      const data = await r.json();
      tieneAgencia = data.tieneAgencia;
      agencia      = data.agencia;
    } catch {
      errorGlobal = 'Error de conexión. Intenta de nuevo.';
    } finally {
      cargando = false;
    }
  }

  // ── Validación ────────────────────────────────────────────────
  function validar() {
    errores = { nombre: '', correo: '' };
    let ok = true;
    if (!nombre.trim()) { errores.nombre = 'El nombre es obligatorio.'; ok = false; }
    if (!correo.trim()) { errores.correo = 'El correo es obligatorio.'; ok = false; }
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) { errores.correo = 'Ingresa un correo válido.'; ok = false; }
    return ok;
  }

  // ── Crear agencia ─────────────────────────────────────────────
  async function handleCrear() {
    if (!validar()) return;
    enviando = true;
    try {
      const r = await fetch(`${API}/api/agencias/mi-agencia`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: nombre.trim(), correo: correo.trim() })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', '¡Agencia registrada exitosamente!');
        await cargarMiAgencia();
        nombre = ''; correo = '';
      } else {
        mostrarToast('error', data.message || 'Error al crear la agencia.');
      }
    } catch {
      mostrarToast('error', 'Error de conexión. Intenta de nuevo.');
    } finally {
      enviando = false;
    }
  }

  // ── Toast ─────────────────────────────────────────────────────
  function mostrarToast(tipo, mensaje) {
    toast = { tipo, mensaje };
    setTimeout(() => { toast = null; }, 4000);
  }

  const estadoLabel = (id) => ({ 1: 'Activa', 2: 'Inactiva', 3: 'Suspendida' }[id] ?? 'Desconocido');
  const estadoClass = (id) => ({ 1: 'badge--active', 2: 'badge--inactive', 3: 'badge--suspended' }[id] ?? '');
</script>

<!-- ── Toast ──────────────────────────────────────────────────── -->
{#if toast}
  <div class="mi-agencia-toast mi-agencia-toast--{toast.tipo}">
    <span class="mi-agencia-toast__icon">{toast.tipo === 'success' ? '✓' : '✕'}</span>
    {toast.mensaje}
  </div>
{/if}

<div class="admin">
  <div class="admin__container">

    <!-- Header -->
    <header class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>
        ← Volver al inicio
      </button>
      <h1 class="admin__title">Mi Agencia</h1>
      <p class="admin__subtitle">
        Panel de gestión de tu agencia Webservice
      </p>
    </header>

    <!-- Cargando -->
    {#if cargando}
      <div class="admin-section" style="text-align:center; padding:4rem 2rem;">
        <div class="mi-agencia-spinner"></div>
        <p style="color:var(--text-muted); margin-top:1rem;">Cargando información...</p>
      </div>

    <!-- Error global -->
    {:else if errorGlobal}
      <div class="admin-section">
        <div class="mi-agencia-alert mi-agencia-alert--error">
          <span>⚠</span> {errorGlobal}
        </div>
        <button class="btn-primary" style="max-width:200px; margin-top:1.5rem;" on:click={cargarMiAgencia}>
          Reintentar
        </button>
      </div>

    <!-- ── YA TIENE AGENCIA ──────────────────────────────────── -->
    {:else if tieneAgencia && agencia}
      <div class="admin-section">
        <div class="section-header">
          <div>
            <h2 class="admin-section__title">Tu agencia registrada</h2>
            <p class="admin-section__subtitle">
              Esta es la información de tu agencia. El descuento y el estado son gestionados por el administrador.
            </p>
          </div>
          <span class="mi-agencia-badge {estadoClass(agencia.estadoAgenciaID)}">
            {estadoLabel(agencia.estadoAgenciaID)}
          </span>
        </div>

        <div class="mi-agencia-cards">

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🏢</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Nombre de la agencia</p>
              <p class="mi-agencia-card__value">{agencia.nombre}</p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">✉️</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Correo de contacto</p>
              <p class="mi-agencia-card__value">{agencia.correo}</p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🏷️</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Descuento asignado</p>
              <p class="mi-agencia-card__value mi-agencia-card__value--highlight">
                {agencia.porcentajeDescuento}%
              </p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🆔</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">ID de agencia</p>
              <p class="mi-agencia-card__value"># {agencia.id}</p>
            </div>
          </div>

        </div>

        <div class="mi-agencia-notice">
          <span>ℹ️</span>
          <p>
            Para modificar los datos de tu agencia, el descuento o el estado,
            comunícate con el administrador del sistema.
          </p>
        </div>
      </div>

    <!-- ── AÚN NO TIENE AGENCIA → FORMULARIO ────────────────── -->
    {:else}
      <div class="admin-section">
        <div class="section-header">
          <div>
            <h2 class="admin-section__title">Registra tu agencia</h2>
            <p class="admin-section__subtitle">
              Como usuario Webservice puedes registrar <strong>una sola agencia</strong>.
              Completa los campos a continuación para comenzar.
            </p>
          </div>
        </div>

        <div class="mi-agencia-form">

          <div class="mi-agencia-field">
            <label class="mi-agencia-field__label" for="nombre">
              Nombre de la agencia <span class="mi-agencia-field__required">*</span>
            </label>
            <input
              id="nombre"
              class="mi-agencia-field__input {errores.nombre ? 'mi-agencia-field__input--error' : ''}"
              type="text"
              placeholder="Ej. Agencia Viajes GT"
              bind:value={nombre}
              disabled={enviando}
              maxlength="120"
            />
            {#if errores.nombre}
              <p class="mi-agencia-field__error">{errores.nombre}</p>
            {/if}
          </div>

          <div class="mi-agencia-field">
            <label class="mi-agencia-field__label" for="correo">
              Correo de contacto <span class="mi-agencia-field__required">*</span>
            </label>
            <input
              id="correo"
              class="mi-agencia-field__input {errores.correo ? 'mi-agencia-field__input--error' : ''}"
              type="email"
              placeholder="agencia@ejemplo.com"
              bind:value={correo}
              disabled={enviando}
              maxlength="200"
            />
            {#if errores.correo}
              <p class="mi-agencia-field__error">{errores.correo}</p>
            {/if}
          </div>

          <div class="mi-agencia-notice">
            <span>ℹ️</span>
            <p>
              El porcentaje de descuento y el estado de tu agencia serán asignados
              posteriormente por el administrador.
            </p>
          </div>

          <div class="mi-agencia-form__actions">
            <button
              class="btn-primary"
              on:click={handleCrear}
              disabled={enviando}
            >
              {#if enviando}
                Registrando...
              {:else}
                Registrar agencia
              {/if}
            </button>
          </div>

        </div>
      </div>
    {/if}

  </div>
</div>