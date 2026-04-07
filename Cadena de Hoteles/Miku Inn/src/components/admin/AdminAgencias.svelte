<script>
  /**
   * @file AdminAgencias.svelte
   * @description Panel de administracion para gestionar las agencias de viaje registradas en el sistema.
   * Permite buscar, filtrar, crear y editar la informacion de cada agencia mediante modales dedicados.
   * Al crear una agencia se debe asignar un usuario webservice que no tenga entidad registrada.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /**
   * Funcion que devuelve la clase CSS correspondiente al estado de un registro.
   * @type {function(string): string}
   */
  export let badge;

  /** Contador reactivo que refleja el total de agencias cargadas. @type {number} */
  export let count = 0;

  /** Lista completa de agencias recibidas del servidor. @type {Array<Object>} */
  let agencias = [];

  /** Indica si se esta realizando la peticion para cargar agencias. @type {boolean} */
  let cargandoAgencias = false;

  /** Mensaje de error en caso de que falle la carga de agencias. @type {string|null} */
  let errorAgencias = null;

  /** Texto ingresado en el buscador para filtrar agencias por nombre, correo o ID. @type {string} */
  let busquedaAgencia = '';

  // --- MODAL EDITAR ---

  /** Controla la visibilidad del modal de edicion de agencia. @type {boolean} */
  let showModalEditarAgencia = false;

  /** Agencia que esta siendo editada en el modal. @type {Object|null} */
  let agenciaEditando = null;

  /** Copia mutable de los campos de la agencia en edicion. @type {Object} */
  let editAgencia = { nombre: '', correo: '', urlAgencia: '', porcentajeDescuento: 0, estadoId: 1 };

  /** Indica si se esta enviando la peticion para guardar cambios de la agencia. @type {boolean} */
  let guardandoAgencia = false;

  /** Mensaje de retroalimentacion tras guardar o si ocurre un error en edicion. @type {{tipo: string, texto: string}|null} */
  let mensajeAgencia = null;

  // --- MODAL CREAR ---

  /** Controla la visibilidad del modal de creacion de agencia. @type {boolean} */
  let showModalCrear = false;

  /** Datos del formulario para crear una nueva agencia. @type {Object} */
  let nuevaAgencia = { nombre: '', correo: '', urlAgencia: '', usuarioWebisId: '' };

  /** Lista de usuarios webservice sin entidad asignada para el selector del modal. @type {Array<Object>} */
  let usuariosLibres = [];

  /** Indica si se estan cargando los usuarios libres en el modal de creacion. @type {boolean} */
  let cargandoUsuarios = false;

  /** Indica si se esta enviando la peticion para crear la agencia. @type {boolean} */
  let creandoAgencia = false;

  /** Mensaje de retroalimentacion tras crear o si ocurre un error en creacion. @type {{tipo: string, texto: string}|null} */
  let mensajeCrear = null;

  // Lista filtrada de agencias segun el texto ingresado en la busqueda.
  $: agenciasFiltradas = agencias.filter(a =>
    a.nombre?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    a.correo?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    String(a.id).includes(busquedaAgencia)
  );

  // Mantiene el contador exportado sincronizado con el total de agencias.
  $: count = agencias.length;

  onMount(() => { cargarAgencias(); });

  /**
   * Obtiene la lista completa de agencias desde el endpoint del administrador.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarAgencias() {
    cargandoAgencias = true;
    errorAgencias = null;
    try {
      const res = await fetch(`${API_BASE}/admin/agencias`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      agencias = await res.json();
    } catch (e) {
      errorAgencias = 'No se pudo cargar la lista de agencias. ' + e.message;
    } finally {
      cargandoAgencias = false;
    }
  }

  // --- Logica del modal de edicion ---

  /**
   * Abre el modal de edicion precargando los datos de la agencia seleccionada.
   * @param {Object} ag - Objeto de agencia con sus campos actuales.
   */
  function abrirEditarAgencia(ag) {
    agenciaEditando = ag;
    editAgencia = {
      nombre:              ag.nombre              ?? '',
      correo:              ag.correo              ?? '',
      urlAgencia:          ag.urlAgencia          ?? '',
      porcentajeDescuento: ag.porcentajeDescuento ?? 0,
      estadoId:            ag.estadoId            ?? 1,
    };
    mensajeAgencia = null;
    showModalEditarAgencia = true;
  }

  /**
   * Cierra el modal de edicion y limpia el estado relacionado.
   */
  function cerrarModalAgencia() {
    showModalEditarAgencia = false;
    agenciaEditando = null;
    mensajeAgencia = null;
  }

  /**
   * Envia los cambios de la agencia al servidor mediante PATCH y actualiza la lista local.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarAgencia() {
    if (!editAgencia.nombre.trim()) {
      mensajeAgencia = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    guardandoAgencia = true;
    mensajeAgencia = null;
    try {
      const res = await fetch(`${API_BASE}/admin/agencias/${agenciaEditando.id}`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:              editAgencia.nombre.trim(),
          correo:              editAgencia.correo.trim(),
          urlAgencia:          editAgencia.urlAgencia.trim(),
          porcentajeDescuento: Number(editAgencia.porcentajeDescuento),
          estadoId:            Number(editAgencia.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      // Activa=1, Inactiva=2  (tabla EstadoAgencia)
      const estadoTexto = Number(editAgencia.estadoId) === 1 ? 'Activa' : 'Inactiva';
      agencias = agencias.map(a =>
        a.id === agenciaEditando.id
          ? { ...a, ...editAgencia, estado: estadoTexto }
          : a
      );
      mensajeAgencia = { tipo: 'ok', texto: 'Agencia actualizada correctamente.' };
      setTimeout(cerrarModalAgencia, 1200);
    } catch (e) {
      mensajeAgencia = { tipo: 'error', texto: e.message };
    } finally {
      guardandoAgencia = false;
    }
  }

  // --- Logica del modal de creacion ---

  /**
   * Abre el modal de creacion y carga los usuarios webservice disponibles para asignacion.
   * @async
   * @returns {Promise<void>}
   */
  async function abrirCrear() {
    nuevaAgencia = { nombre: '', correo: '', urlAgencia: '', usuarioWebisId: '' };
    mensajeCrear = null;
    showModalCrear = true;
    cargandoUsuarios = true;
    try {
      const res = await fetch(`${API_BASE}/admin/webservice/libres`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      usuariosLibres = await res.json();
    } catch (e) {
      mensajeCrear = { tipo: 'error', texto: 'No se pudo cargar la lista de usuarios disponibles.' };
    } finally {
      cargandoUsuarios = false;
    }
  }

  /**
   * Cierra el modal de creacion y limpia el estado relacionado.
   */
  function cerrarModalCrear() {
    showModalCrear = false;
    mensajeCrear = null;
    usuariosLibres = [];
  }

  /**
   * Envia los datos del formulario al servidor para crear la nueva agencia y
   * actualiza la lista local al recibir la respuesta exitosa.
   * @async
   * @returns {Promise<void>}
   */
  async function crearAgencia() {
    if (!nuevaAgencia.nombre.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    if (!nuevaAgencia.correo.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El correo es obligatorio.' };
      return;
    }
    if (!nuevaAgencia.urlAgencia.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'La URL del sistema externo es obligatoria.' };
      return;
    }
    if (!nuevaAgencia.usuarioWebisId) {
      mensajeCrear = { tipo: 'error', texto: 'Debe seleccionar un usuario webservice.' };
      return;
    }
    creandoAgencia = true;
    mensajeCrear = null;
    try {
      const res = await fetch(`${API_BASE}/admin/agencias`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:         nuevaAgencia.nombre.trim(),
          correo:         nuevaAgencia.correo.trim(),
          urlAgencia:     nuevaAgencia.urlAgencia.trim(),
          usuarioWebisId: Number(nuevaAgencia.usuarioWebisId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      // Agrega la nueva agencia al inicio de la lista local
      agencias = [data, ...agencias];
      mensajeCrear = { tipo: 'ok', texto: `Agencia "${data.nombre}" creada correctamente.` };
      setTimeout(cerrarModalCrear, 1200);
    } catch (e) {
      mensajeCrear = { tipo: 'error', texto: e.message };
    } finally {
      creandoAgencia = false;
    }
  }
</script>

<!-- Barra de busqueda, boton de recarga y boton de crear nueva agencia -->
<div class="adm__filters-bar">
  <div class="adm__search-wrap">
    <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
    <input class="adm__search-input" type="text" placeholder="Buscar por nombre, correo o ID..." bind:value={busquedaAgencia} />
  </div>
  <button class="adm__btn adm__btn--ghost" on:click={cargarAgencias} disabled={cargandoAgencias} title="Recargar">
    <svg class={cargandoAgencias ? 'adm__spinner' : ''} width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
    Recargar
  </button>
  <!-- Boton para abrir el modal de creacion de agencia -->
  <button class="adm__btn adm__btn--primary" on:click={abrirCrear}>
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
    Nueva agencia
  </button>
</div>

<!-- Mensaje de error si la carga de agencias falla -->
{#if errorAgencias}
  <div class="adm__feedback adm__feedback--error" style="margin-bottom:1rem">
    {errorAgencias}
    <button class="adm__btn adm__btn--ghost" on:click={cargarAgencias}>Reintentar</button>
  </div>
{/if}

<!-- Tabla principal de agencias -->
<div class="adm__card adm__card--no-pad">
  {#if cargandoAgencias}
    <div class="adm__loading-state" style="padding:3rem 0">
      <svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p>Cargando agencias...</p>
    </div>
  {:else}
    <div class="adm__table-wrap">
      <table class="adm__table">
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Usuario WS</th><th>Descuento %</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          {#if agenciasFiltradas.length === 0}
            <tr><td colspan="7" class="adm__empty-cell">{busquedaAgencia ? 'Sin resultados para esa búsqueda.' : 'No hay agencias registradas.'}</td></tr>
          {:else}
            <!-- Fila por cada agencia filtrada -->
            {#each agenciasFiltradas as ag (ag.id)}
              <tr>
                <td class="adm__table-mono" style="color:var(--adm-text-muted);font-size:.8rem">#{ag.id}</td>
                <td style="font-weight:600">{ag.nombre}</td>
                <td style="color:var(--adm-text-muted)">{ag.correo}</td>
                <td class="adm__table-mono" style="font-size:.8rem">WS #{ag.usuarioWebisId}</td>
                <td><span style="font-weight:700;color:#2dd4bf">{ag.porcentajeDescuento?.toFixed(2)}%</span></td>
                <td><span class="adm__badge {badge(ag.estado)}">{ag.estado}</span></td>
                <td>
                  <!-- Boton para abrir el modal de edicion de la agencia -->
                  <button class="adm__icon-btn adm__icon-btn--edit" title="Editar" on:click={() => abrirEditarAgencia(ag)}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                </td>
              </tr>
            {/each}
          {/if}
        </tbody>
      </table>
    </div>
  {/if}
</div>

<!-- Modal para editar los datos de una agencia -->
{#if showModalEditarAgencia && agenciaEditando}
  <div class="adm__overlay" on:click={cerrarModalAgencia} on:keydown={e => e.key === 'Escape' && cerrarModalAgencia()} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:520px">
    <div class="adm__rol-modal__header" style="background:#1a2e3a; border-bottom-color:#2a4a5c">
      <div style="width:38px;height:38px;border-radius:9px;background:linear-gradient(135deg,#2dd4bf,#0d9488);display:flex;align-items:center;justify-content:center;flex-shrink:0">
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><path d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
      </div>
      <div class="adm__rol-modal__user-info">
        <p class="adm__rol-modal__name">Editar Agencia</p>
        <p class="adm__rol-modal__username">ID #{agenciaEditando.id} · WS #{agenciaEditando.usuarioWebisId}</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModalAgencia} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Formulario de edicion de campos de la agencia -->
    <div class="adm__rol-modal__body">
      <div style="display:flex;flex-direction:column;gap:1.1rem">
        <div class="adm__field">
          <label for="ea-nombre">Nombre de la agencia</label>
          <input id="ea-nombre" type="text" bind:value={editAgencia.nombre} placeholder="Nombre" />
        </div>
        <div class="adm__field">
          <label for="ea-correo">Correo electrónico</label>
          <input id="ea-correo" type="email" bind:value={editAgencia.correo} placeholder="agencia@ejemplo.com" />
        </div>
        <!-- Campo para editar la URL del sistema externo de la agencia -->
        <div class="adm__field">
          <label for="ea-url">URL del sistema externo</label>
          <input id="ea-url" type="text" bind:value={editAgencia.urlAgencia} placeholder="http://sistema.com" />
        </div>
        <div class="adm__field">
          <label for="ea-descuento">Porcentaje de descuento (0–100)</label>
          <input id="ea-descuento" type="number" min="0" max="100" step="0.01" bind:value={editAgencia.porcentajeDescuento} />
        </div>
        <div class="adm__field">
          <label for="ea-estado">Estado</label>
          <!-- EstadoAgencia: 1=Activa, 2=Inactiva -->
          <select id="ea-estado" bind:value={editAgencia.estadoId}>
            <option value={1}>Activa</option>
            <option value={2}>Inactiva</option>
          </select>
        </div>
      </div>

      <!-- Feedback de exito o error al guardar edicion -->
      {#if mensajeAgencia}
        <div class="adm__feedback adm__feedback--{mensajeAgencia.tipo}" style="margin-top:1rem">
          {mensajeAgencia.texto}
        </div>
      {/if}
    </div>

    <div class="adm__rol-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModalAgencia} disabled={guardandoAgencia}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={guardarAgencia} disabled={guardandoAgencia}>
        {#if guardandoAgencia}Guardando...{:else}Guardar cambios{/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal para crear una nueva agencia con asignacion de usuario webservice -->
{#if showModalCrear}
  <div class="adm__overlay" on:click={cerrarModalCrear} on:keydown={e => e.key === 'Escape' && cerrarModalCrear()} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:520px">
    <div class="adm__rol-modal__header" style="background:#1a2e3a; border-bottom-color:#2a4a5c">
      <div style="width:38px;height:38px;border-radius:9px;background:linear-gradient(135deg,#2dd4bf,#0d9488);display:flex;align-items:center;justify-content:center;flex-shrink:0">
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><path d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
      </div>
      <div class="adm__rol-modal__user-info">
        <p class="adm__rol-modal__name">Nueva Agencia de Viaje</p>
        <p class="adm__rol-modal__username">Completa los datos y asigna un usuario webservice</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModalCrear} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Formulario de creacion de nueva agencia -->
    <div class="adm__rol-modal__body">
      <div style="display:flex;flex-direction:column;gap:1.1rem">
        <div class="adm__field">
          <label for="na-nombre">Nombre de la agencia *</label>
          <input id="na-nombre" type="text" bind:value={nuevaAgencia.nombre} placeholder="Ej: Agencia Turistica Central" />
        </div>
        <div class="adm__field">
          <label for="na-correo">Correo electrónico *</label>
          <input id="na-correo" type="email" bind:value={nuevaAgencia.correo} placeholder="agencia@ejemplo.com" />
        </div>
        <div class="adm__field">
          <label for="na-url">URL del sistema externo *</label>
          <input id="na-url" type="text" bind:value={nuevaAgencia.urlAgencia} placeholder="http://sistema.com" />
        </div>

        <!-- Selector de usuario webservice libre: solo muestra usuarios sin entidad asignada -->
        <div class="adm__field">
          <label for="na-usuario">Usuario webservice *</label>
          {#if cargandoUsuarios}
            <div style="display:flex;align-items:center;gap:.5rem;color:var(--adm-text-muted);font-size:.85rem;padding:.5rem 0">
              <svg class="adm__spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              Cargando usuarios disponibles...
            </div>
          {:else if usuariosLibres.length === 0}
            <p style="margin:0;font-size:.82rem;color:var(--adm-text-muted);padding:.4rem 0">
              No hay usuarios webservice disponibles. Todos ya tienen una entidad asignada.
            </p>
          {:else}
            <select id="na-usuario" bind:value={nuevaAgencia.usuarioWebisId}>
              <option value="">— Seleccionar usuario —</option>
              {#each usuariosLibres as u}
                <option value={u.id}>#{u.id} · @{u.username}</option>
              {/each}
            </select>
          {/if}
        </div>

        <!-- Aviso informativo sobre valores iniciales -->
        <div style="background:rgba(45,212,191,0.08);border:1px solid rgba(45,212,191,0.2);border-radius:8px;padding:.6rem .875rem">
          <p style="margin:0;font-size:.78rem;color:var(--adm-text-muted)">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2dd4bf" stroke-width="2" style="vertical-align:middle;margin-right:4px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            El descuento inicia en <strong style="color:#2dd4bf">0%</strong> y los tokens de autenticacion se generan al establecer la conexion.
          </p>
        </div>
      </div>

      <!-- Feedback de exito o error al crear -->
      {#if mensajeCrear}
        <div class="adm__feedback adm__feedback--{mensajeCrear.tipo}" style="margin-top:1rem">
          {mensajeCrear.texto}
        </div>
      {/if}
    </div>

    <div class="adm__rol-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModalCrear} disabled={creandoAgencia}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={crearAgencia} disabled={creandoAgencia || cargandoUsuarios}>
        {#if creandoAgencia}Creando...{:else}Crear agencia{/if}
      </button>
    </div>
  </div>
{/if}