<script>
  /**
   * @file AdminAerolineas.svelte
   * @description Panel de administracion para gestionar las aerolineas aliadas registradas en el sistema.
   * Permite buscar, filtrar, crear y editar la informacion de cada aerolinea mediante modales dedicados.
   * Al crear una aerolinea se debe asignar un usuario webservice que no tenga entidad registrada.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /**
   * Funcion que devuelve la clase CSS correspondiente al estado de un registro.
   * @type {function(string): string}
   */
  export let badge;

  /** Contador reactivo que refleja el total de aerolineas cargadas. @type {number} */
  export let count = 0;

  /** Lista completa de aerolineas recibidas del servidor. @type {Array<Object>} */
  let aerolineas = [];

  /** Indica si se esta realizando la peticion para cargar aerolineas. @type {boolean} */
  let cargandoAerolineas = false;

  /** Mensaje de error en caso de que falle la carga de aerolineas. @type {string|null} */
  let errorAerolineas = null;

  /** Texto ingresado en el buscador para filtrar aerolineas por nombre o ID. @type {string} */
  let busquedaAerolinea = '';

  // --- MODAL EDITAR ---

  /** Controla la visibilidad del modal de edicion de aerolinea. @type {boolean} */
  let showModalEditar = false;

  /** Aerolinea que esta siendo editada en el modal. @type {Object|null} */
  let aerolineaEditando = null;

  /** Copia mutable de los campos de la aerolinea en edicion. @type {Object} */
  let editAerolinea = { nombre: '', url: '', urlParaUsuario: '', porcentajeDescuento: 0, estadoId: 1 };

  /** Indica si se esta enviando la peticion para guardar cambios de la aerolinea. @type {boolean} */
  let guardandoAerolinea = false;

  /** Mensaje de retroalimentacion tras guardar o si ocurre un error en edicion. @type {{tipo: string, texto: string}|null} */
  let mensajeEditar = null;

  // --- MODAL CREAR ---

  /** Controla la visibilidad del modal de creacion de aerolinea. @type {boolean} */
  let showModalCrear = false;

  /** Datos del formulario para crear una nueva aerolinea. @type {Object} */
  let nuevaAerolinea = { nombre: '', url: '', urlParaUsuario: '', usuarioWebisId: '' };

  /** Lista de usuarios webservice sin entidad asignada para el selector del modal. @type {Array<Object>} */
  let usuariosLibres = [];

  /** Indica si se estan cargando los usuarios libres en el modal de creacion. @type {boolean} */
  let cargandoUsuarios = false;

  /** Indica si se esta enviando la peticion para crear la aerolinea. @type {boolean} */
  let creandoAerolinea = false;

  /** Mensaje de retroalimentacion tras crear o si ocurre un error en creacion. @type {{tipo: string, texto: string}|null} */
  let mensajeCrear = null;

  // Lista filtrada de aerolineas segun el texto ingresado en la busqueda.
  $: aerolineasFiltradas = aerolineas.filter(a =>
    a.nombre?.toLowerCase().includes(busquedaAerolinea.toLowerCase()) ||
    a.usuarioUsername?.toLowerCase().includes(busquedaAerolinea.toLowerCase()) ||
    String(a.id).includes(busquedaAerolinea)
  );

  // Mantiene el contador exportado sincronizado con el total de aerolineas.
  $: count = aerolineas.length;

  onMount(() => { cargarAerolineas(); });

  /**
   * Obtiene la lista completa de aerolineas desde el endpoint del administrador.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarAerolineas() {
    cargandoAerolineas = true;
    errorAerolineas = null;
    try {
      const res = await fetch(`${API_BASE}/admin/aerolineas`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      aerolineas = await res.json();
    } catch (e) {
      errorAerolineas = 'No se pudo cargar la lista de aerolineas. ' + e.message;
    } finally {
      cargandoAerolineas = false;
    }
  }

  // --- Logica del modal de edicion ---

  /**
   * Abre el modal de edicion precargando los datos de la aerolinea seleccionada.
   * @param {Object} ae - Objeto de aerolinea con sus campos actuales.
   */
  function abrirEditar(ae) {
    aerolineaEditando = ae;
    editAerolinea = {
      nombre:              ae.nombre              ?? '',
      url:                 ae.url                 ?? '',
      urlParaUsuario:      ae.urlParaUsuario       ?? '',
      porcentajeDescuento: ae.porcentajeDescuento ?? 0,
      estadoId:            ae.estadoId            ?? 1,
    };
    mensajeEditar = null;
    showModalEditar = true;
  }

  /**
   * Cierra el modal de edicion y limpia el estado relacionado.
   */
  function cerrarModalEditar() {
    showModalEditar = false;
    aerolineaEditando = null;
    mensajeEditar = null;
  }

  /**
   * Envia los cambios de la aerolinea al servidor mediante PATCH y actualiza la lista local.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarAerolinea() {
    if (!editAerolinea.nombre.trim()) {
      mensajeEditar = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    if (!editAerolinea.url.trim()) {
      mensajeEditar = { tipo: 'error', texto: 'La URL del sistema externo es obligatoria.' };
      return;
    }
    if (!editAerolinea.urlParaUsuario.trim()) {
      mensajeEditar = { tipo: 'error', texto: 'La URL para el usuario final es obligatoria.' };
      return;
    }
    guardandoAerolinea = true;
    mensajeEditar = null;
    try {
      const res = await fetch(`${API_BASE}/admin/aerolineas/${aerolineaEditando.id}`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:              editAerolinea.nombre.trim(),
          url:                 editAerolinea.url.trim(),
          urlParaUsuario:      editAerolinea.urlParaUsuario.trim(),
          porcentajeDescuento: Number(editAerolinea.porcentajeDescuento),
          estadoId:            Number(editAerolinea.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      // EstadoAliado: 1=Activo, 2=Inactivo
      const estadoTexto = Number(editAerolinea.estadoId) === 1 ? 'Activo' : 'Inactivo';
      aerolineas = aerolineas.map(a =>
        a.id === aerolineaEditando.id
          ? { ...a, ...editAerolinea, estado: estadoTexto }
          : a
      );
      mensajeEditar = { tipo: 'ok', texto: 'Aerolinea actualizada correctamente.' };
      setTimeout(cerrarModalEditar, 1200);
    } catch (e) {
      mensajeEditar = { tipo: 'error', texto: e.message };
    } finally {
      guardandoAerolinea = false;
    }
  }

  // --- Logica del modal de creacion ---

  /**
   * Abre el modal de creacion y carga los usuarios webservice disponibles para asignacion.
   * @async
   * @returns {Promise<void>}
   */
  async function abrirCrear() {
    nuevaAerolinea = { nombre: '', url: '', urlParaUsuario: '', usuarioWebisId: '' };
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
   * Envia los datos del formulario al servidor para crear la nueva aerolinea y
   * actualiza la lista local al recibir la respuesta exitosa.
   * @async
   * @returns {Promise<void>}
   */
  async function crearAerolinea() {
    if (!nuevaAerolinea.nombre.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    if (!nuevaAerolinea.url.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'La URL del sistema externo es obligatoria.' };
      return;
    }
    if (!nuevaAerolinea.urlParaUsuario.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'La URL para el usuario final es obligatoria.' };
      return;
    }
    if (!nuevaAerolinea.usuarioWebisId) {
      mensajeCrear = { tipo: 'error', texto: 'Debe seleccionar un usuario webservice.' };
      return;
    }
    creandoAerolinea = true;
    mensajeCrear = null;
    try {
      const res = await fetch(`${API_BASE}/admin/aerolineas`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:         nuevaAerolinea.nombre.trim(),
          url:            nuevaAerolinea.url.trim(),
          urlParaUsuario: nuevaAerolinea.urlParaUsuario.trim(),
          usuarioWebisId: Number(nuevaAerolinea.usuarioWebisId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      // Agrega la nueva aerolinea al inicio de la lista local
      aerolineas = [data, ...aerolineas];
      mensajeCrear = { tipo: 'ok', texto: `Aerolinea "${data.nombre}" creada correctamente.` };
      setTimeout(cerrarModalCrear, 1200);
    } catch (e) {
      mensajeCrear = { tipo: 'error', texto: e.message };
    } finally {
      creandoAerolinea = false;
    }
  }
</script>

<!-- Barra de busqueda, boton de recarga y boton de crear nueva aerolinea -->
<div class="adm__filters-bar">
  <div class="adm__search-wrap">
    <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
    <input class="adm__search-input" type="text" placeholder="Buscar por nombre, usuario o ID..." bind:value={busquedaAerolinea} />
  </div>
  <button class="adm__btn adm__btn--ghost" on:click={cargarAerolineas} disabled={cargandoAerolineas} title="Recargar">
    <svg class={cargandoAerolineas ? 'adm__spinner' : ''} width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
    Recargar
  </button>
  <!-- Boton para abrir el modal de creacion de aerolinea -->
  <button class="adm__btn adm__btn--primary" on:click={abrirCrear}>
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
    Nueva aerolinea
  </button>
</div>

<!-- Mensaje de error si la carga de aerolineas falla -->
{#if errorAerolineas}
  <div class="adm__feedback adm__feedback--error" style="margin-bottom:1rem">
    {errorAerolineas}
    <button class="adm__btn adm__btn--ghost" on:click={cargarAerolineas}>Reintentar</button>
  </div>
{/if}

<!-- Tabla principal de aerolineas -->
<div class="adm__card adm__card--no-pad">
  {#if cargandoAerolineas}
    <div class="adm__loading-state" style="padding:3rem 0">
      <svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p>Cargando aerolineas...</p>
    </div>
  {:else}
    <div class="adm__table-wrap">
      <table class="adm__table">
        <thead>
          <tr><th>ID</th><th>Nombre</th><th>Usuario WS</th><th>Descuento %</th><th>URL Sistema</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          {#if aerolineasFiltradas.length === 0}
            <tr><td colspan="7" class="adm__empty-cell">{busquedaAerolinea ? 'Sin resultados para esa búsqueda.' : 'No hay aerolineas registradas.'}</td></tr>
          {:else}
            <!-- Fila por cada aerolinea filtrada -->
            {#each aerolineasFiltradas as ae (ae.id)}
              <tr>
                <td class="adm__table-mono" style="color:var(--adm-text-muted);font-size:.8rem">#{ae.id}</td>
                <td style="font-weight:600">{ae.nombre}</td>
                <td class="adm__table-mono" style="font-size:.8rem">
                  <span style="color:var(--adm-text-muted)">WS #{ae.usuarioWebis}</span>
                  {#if ae.usuarioUsername && ae.usuarioUsername !== '—'}
                    <span style="margin-left:.4rem;color:var(--adm-text)">@{ae.usuarioUsername}</span>
                  {/if}
                </td>
                <td><span style="font-weight:700;color:#2dd4bf">{ae.porcentajeDescuento?.toFixed(2)}%</span></td>
                <td style="max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.8rem;color:var(--adm-text-muted)" title={ae.url}>{ae.url ?? '—'}</td>
                <td><span class="adm__badge {badge(ae.estado)}">{ae.estado}</span></td>
                <td>
                  <!-- Boton para abrir el modal de edicion de la aerolinea -->
                  <button class="adm__icon-btn adm__icon-btn--edit" title="Editar" on:click={() => abrirEditar(ae)}>
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

<!-- Modal para editar los datos de una aerolinea -->
{#if showModalEditar && aerolineaEditando}
  <div class="adm__overlay" on:click={cerrarModalEditar} on:keydown={e => e.key === 'Escape' && cerrarModalEditar()} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:520px">
    <div class="adm__rol-modal__header" style="background:#1a1a2e; border-bottom-color:#2a2a4a">
      <div style="width:38px;height:38px;border-radius:9px;background:linear-gradient(135deg,#667eea,#764ba2);display:flex;align-items:center;justify-content:center;flex-shrink:0">
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>
      </div>
      <div class="adm__rol-modal__user-info">
        <p class="adm__rol-modal__name">Editar Aerolinea</p>
        <p class="adm__rol-modal__username">ID #{aerolineaEditando.id} · @{aerolineaEditando.usuarioUsername}</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModalEditar} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Formulario de edicion de campos de la aerolinea -->
    <div class="adm__rol-modal__body">
      <div style="display:flex;flex-direction:column;gap:1.1rem">
        <div class="adm__field">
          <label for="ea-nombre">Nombre de la aerolinea</label>
          <input id="ea-nombre" type="text" bind:value={editAerolinea.nombre} placeholder="Nombre" />
        </div>
        <div class="adm__field">
          <label for="ea-url">URL del sistema externo</label>
          <input id="ea-url" type="text" bind:value={editAerolinea.url} placeholder="http://sistema.com" />
        </div>
        <div class="adm__field">
          <label for="ea-url-usuario">URL para el usuario final</label>
          <input id="ea-url-usuario" type="text" bind:value={editAerolinea.urlParaUsuario} placeholder="http://sistema.com/reservar" />
        </div>
        <div class="adm__field">
          <label for="ea-descuento">Porcentaje de descuento (0–100)</label>
          <input id="ea-descuento" type="number" min="0" max="100" step="0.01" bind:value={editAerolinea.porcentajeDescuento} />
        </div>
        <div class="adm__field">
          <label for="ea-estado">Estado</label>
          <!-- EstadoAliado: 1=Activo, 2=Inactivo -->
          <select id="ea-estado" bind:value={editAerolinea.estadoId}>
            <option value={1}>Activo</option>
            <option value={2}>Inactivo</option>
          </select>
        </div>
      </div>

      <!-- Feedback de exito o error al guardar edicion -->
      {#if mensajeEditar}
        <div class="adm__feedback adm__feedback--{mensajeEditar.tipo}" style="margin-top:1rem">
          {mensajeEditar.texto}
        </div>
      {/if}
    </div>

    <div class="adm__rol-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModalEditar} disabled={guardandoAerolinea}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={guardarAerolinea} disabled={guardandoAerolinea}>
        {#if guardandoAerolinea}Guardando...{:else}Guardar cambios{/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal para crear una nueva aerolinea con asignacion de usuario webservice -->
{#if showModalCrear}
  <div class="adm__overlay" on:click={cerrarModalCrear} on:keydown={e => e.key === 'Escape' && cerrarModalCrear()} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:520px">
    <div class="adm__rol-modal__header" style="background:#1a1a2e; border-bottom-color:#2a2a4a">
      <div style="width:38px;height:38px;border-radius:9px;background:linear-gradient(135deg,#667eea,#764ba2);display:flex;align-items:center;justify-content:center;flex-shrink:0">
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg>
      </div>
      <div class="adm__rol-modal__user-info">
        <p class="adm__rol-modal__name">Nueva Aerolinea Aliada</p>
        <p class="adm__rol-modal__username">Completa los datos y asigna un usuario webservice</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModalCrear} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Formulario de creacion de nueva aerolinea -->
    <div class="adm__rol-modal__body">
      <div style="display:flex;flex-direction:column;gap:1.1rem">
        <div class="adm__field">
          <label for="na-nombre">Nombre de la aerolinea *</label>
          <input id="na-nombre" type="text" bind:value={nuevaAerolinea.nombre} placeholder="Ej: Aerolinea Centroamericana" />
        </div>
        <div class="adm__field">
          <label for="na-url">URL del sistema externo *</label>
          <input id="na-url" type="text" bind:value={nuevaAerolinea.url} placeholder="http://sistema.com" />
        </div>
        <div class="adm__field">
          <label for="na-url-usuario">URL para el usuario final *</label>
          <input id="na-url-usuario" type="text" bind:value={nuevaAerolinea.urlParaUsuario} placeholder="http://sistema.com/reservar" />
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
            <select id="na-usuario" bind:value={nuevaAerolinea.usuarioWebisId}>
              <option value="">— Seleccionar usuario —</option>
              {#each usuariosLibres as u}
                <option value={u.id}>#{u.id} · @{u.username}</option>
              {/each}
            </select>
          {/if}
        </div>

        <!-- Aviso informativo sobre valores iniciales -->
        <div style="background:rgba(102,126,234,0.08);border:1px solid rgba(102,126,234,0.2);border-radius:8px;padding:.6rem .875rem">
          <p style="margin:0;font-size:.78rem;color:var(--adm-text-muted)">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2" style="vertical-align:middle;margin-right:4px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            El descuento inicia en <strong style="color:#667eea">0%</strong> y el token de autenticacion se genera al establecer la conexion.
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
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModalCrear} disabled={creandoAerolinea}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={crearAerolinea} disabled={creandoAerolinea || cargandoUsuarios}>
        {#if creandoAerolinea}Creando...{:else}Crear aerolinea{/if}
      </button>
    </div>
  </div>
{/if}