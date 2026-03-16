<script>
  import { onMount } from 'svelte';

  export let API_BASE;
  export let badge;
  export let count = 0;

  let usuarios = [];
  let cargandoUsuarios = false;
  let errorUsuarios = null;

  let busquedaUsuario = '';
  let filtroRol = 'todos';

  // Modal editar rol
  let showModalEditarUsuario = false;
  let usuarioSeleccionado = null;
  let editUsuario = { rolId: 1 };
  let guardandoRol = false;
  let mensajeRol = null;

  $: usuariosFiltrados = usuarios.filter(u => {
    const q = busquedaUsuario.toLowerCase();
    const matchBusqueda = q === '' || u.nombre.toLowerCase().includes(q) || u.apellido.toLowerCase().includes(q) || u.username.toLowerCase().includes(q) || u.correo.toLowerCase().includes(q);
    return matchBusqueda && (filtroRol === 'todos' || String(u.rolId) === filtroRol);
  });

  $: count = usuarios.length;

  onMount(() => { cargarUsuarios(); });

  async function cargarUsuarios() {
    cargandoUsuarios = true;
    errorUsuarios = null;
    try {
      const res = await fetch(`${API_BASE}/admin/usuarios`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      usuarios = await res.json();
    } catch (e) {
      errorUsuarios = 'No se pudo cargar la lista de usuarios. ' + e.message;
    } finally {
      cargandoUsuarios = false;
    }
  }

  function abrirEditarUsuario(u) {
    usuarioSeleccionado = u;
    editUsuario = { rolId: u.rolId };
    mensajeRol = null;
    showModalEditarUsuario = true;
  }

  function cerrarModal() {
    showModalEditarUsuario = false;
    usuarioSeleccionado = null;
    mensajeRol = null;
  }

  function handleOverlayKey(e) {
    if (e.key === 'Escape') cerrarModal();
  }

  async function guardarCambioRol() {
    if (!usuarioSeleccionado) return;
    guardandoRol = true;
    mensajeRol = null;
    try {
      const res = await fetch(`${API_BASE}/admin/usuarios/${usuarioSeleccionado.id}/rol`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rolId: editUsuario.rolId })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
const rolNombres = { 1: 'Usuario Registrado', 2: 'Administrador', 3: 'Webservice' };
      usuarios = usuarios.map(u =>
        u.id === usuarioSeleccionado.id
          ? { ...u, rolId: editUsuario.rolId, rolNombre: rolNombres[editUsuario.rolId] ?? 'Usuario Registrado' }
          : u
      );
      cerrarModal();
    } catch (e) {
      mensajeRol = { tipo: 'error', texto: e.message };
    } finally {
      guardandoRol = false;
    }
  }
</script>

<!-- Filtros -->
<div class="adm__filters-bar">
  <div class="adm__search-wrap">
    <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
    <input class="adm__search-input" type="text" bind:value={busquedaUsuario} placeholder="Buscar por nombre, username, correo..." aria-label="Buscar usuarios" />
  </div>
  <select class="adm__select" bind:value={filtroRol} aria-label="Filtrar por rol">
    <option value="todos">Todos los roles</option>
    <option value="1">Usuarios</option>
    <option value="2">Administradores</option>
    <option value="3">Webservice</option>
  </select>
  <span class="adm__count-label">{usuariosFiltrados.length} resultado(s)</span>
  <button class="adm__btn adm__btn--ghost" on:click={cargarUsuarios} title="Recargar lista">
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
  </button>
</div>

{#if cargandoUsuarios}
  <div class="adm__loading-state">
    <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
    <p>Cargando usuarios...</p>
  </div>
{:else if errorUsuarios}
  <div class="adm__error-state">
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
    <p>{errorUsuarios}</p>
    <button class="adm__btn adm__btn--ghost" on:click={cargarUsuarios}>Reintentar</button>
  </div>
{:else}
  <div class="adm__card adm__card--no-pad">
    <div class="adm__table-wrap">
      <table class="adm__table">
        <thead>
          <tr><th>Usuario</th><th>Correo</th><th>País</th><th>Rol</th><th>Ciudad</th><th>Nacimiento</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          {#each usuariosFiltrados as u (u.id)}
            <tr>
              <td>
                <div class="adm__user-mini">
                  <div class="adm__user-mini-avatar" style="background: {u.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : u.rolId === 3 ? 'linear-gradient(135deg,#8b5cf6,#6d28d9)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
                    {u.nombre.charAt(0)}
                  </div>
                  <div>
                    <p class="adm__user-mini-name">{u.nombre} {u.apellido}</p>
                    <p class="adm__user-mini-sub">@{u.username}</p>
                  </div>
                </div>
              </td>
              <td class="adm__table-mono">{u.correo}</td>
              <td>{u.pais}</td>
              <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : u.rolId === 3 ? 'badge--purple' : 'badge--blue'}">{u.rolNombre}</span></td>
              <td>{u.ciudad ?? '—'}</td>
              <td>{u.fechaNacimiento ?? '—'}</td>
              <td>
                <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditarUsuario(u)} title="Cambiar rol">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
              </td>
            </tr>
          {/each}
          {#if usuariosFiltrados.length === 0}
            <tr><td colspan="7" class="adm__empty-cell">No se encontraron usuarios con esos filtros.</td></tr>
          {/if}
        </tbody>
      </table>
    </div>
  </div>
{/if}

<!-- Modal cambiar rol -->
{#if showModalEditarUsuario && usuarioSeleccionado}
  <div class="adm__overlay" on:click={cerrarModal} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal">
    <div class="adm__rol-modal__header">
      <div class="adm__rol-modal__avatar" style="background: {usuarioSeleccionado.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : usuarioSeleccionado.rolId === 3 ? 'linear-gradient(135deg,#8b5cf6,#6d28d9)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
        {usuarioSeleccionado.nombre.charAt(0)}
      </div>
      <div class="adm__rol-modal__user-info">
        <p class="adm__rol-modal__name">{usuarioSeleccionado.nombre} {usuarioSeleccionado.apellido}</p>
        <p class="adm__rol-modal__username">@{usuarioSeleccionado.username}</p>
        <p class="adm__rol-modal__email">{usuarioSeleccionado.correo}</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModal} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <div class="adm__rol-modal__body">
      <p class="adm__rol-modal__label">Seleccionar nuevo rol</p>
      <div class="adm__rol-cards">
        {#each [
          { id: 1, title: 'Usuario Registrado', desc: 'Acceso estándar al sistema', iconClass: 'user', iconPath: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8z' },
          { id: 2, title: 'Administrador',      desc: 'Acceso total al panel',      iconClass: 'admin', iconPath: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z' },
          { id: 3, title: 'Webservice',          desc: 'Acceso para integraciones API', iconClass: 'ws', iconPath: 'M18 20V10 M12 20V4 M6 20v-6' },
        ] as rol}
          <button
            class="adm__rol-card {editUsuario.rolId === rol.id ? `adm__rol-card--active-${rol.iconClass}` : ''}"
            on:click={() => editUsuario.rolId = rol.id}
          >
            <div class="adm__rol-card__icon adm__rol-card__icon--{rol.iconClass}">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d={rol.iconPath}/></svg>
            </div>
            <div class="adm__rol-card__text">
              <span class="adm__rol-card__title">{rol.title}</span>
              <span class="adm__rol-card__desc">{rol.desc}</span>
            </div>
            {#if editUsuario.rolId === rol.id}
              <div class="adm__rol-card__check">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
            {/if}
          </button>
        {/each}
      </div>

      {#if mensajeRol}
        <div class="adm__feedback adm__feedback--{mensajeRol.tipo}">
          {#if mensajeRol.tipo === 'ok'}
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          {:else}
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {/if}
          {mensajeRol.texto}
        </div>
      {/if}
    </div>

    <div class="adm__rol-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModal}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={guardarCambioRol} disabled={guardandoRol}>
        {#if guardandoRol}
          <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
          Guardando...
        {:else}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          Guardar cambio
        {/if}
      </button>
    </div>
  </div>
{/if}