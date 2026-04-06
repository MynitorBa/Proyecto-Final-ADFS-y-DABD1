<script>
/**
 * @file AdminUsuarios.svelte
 * @description Admin panel section for managing registered users. Displays a table of all
 * users in the system with their ID, full name, email, username and current role. The role
 * of each user can be changed inline through a select element that immediately sends a POST
 * request to the cambiar-rol endpoint. On API failure the user list is reloaded to visually
 * revert the select to the server-side role. Provides a manual refresh button that re-fetches
 * the user list.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** Base API URL used for all backend requests. @type {string} */
  export let API;

  /** Function to show a toast notification. Signature: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** List of all users loaded from the backend. @type {any[]} */
  let usuarios        = [];

  /** Whether the user list fetch is in progress. @type {boolean} */
  let loadingUsuarios = false;

  /**
   * Static list of available role options rendered in the inline role select for each user row.
   * @type {{ id: number, nombre: string }[]}
   */
  const rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' },
    { id: 3, nombre: 'Webservice' }
  ];

  /**
   * On mount: loads the user list from the backend.
   */
  onMount(() => { cargarUsuarios(); });

  /**
   * Fetches all registered users from the backend API and stores them in the usuarios array.
   * Shows a toast on error and sets loadingUsuarios during the request.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarUsuarios() {
    loadingUsuarios = true;
    try {
      const r = await fetch(`${API}/api/usuarios`, { credentials: 'include' });
      if (r.ok) usuarios = await r.json();
      else mostrarToast('error', 'Error al cargar usuarios');
    } catch { mostrarToast('error', 'Error de conexion al cargar usuarios'); }
    finally { loadingUsuarios = false; }
  }

  /**
   * POSTs a role change request to the backend for the given user. On success shows a success
   * toast. On failure shows an error toast and reloads the user list so the select reverts
   * to the current server-side role visually.
   * @async
   * @param {number} userId - The ID of the user whose role is being changed.
   * @param {string|number} nuevoRolId - The ID of the new role to assign.
   * @returns {Promise<void>}
   */
  async function handleCambiarRol(userId, nuevoRolId) {
    try {
      const r = await fetch(`${API}/api/usuarios/cambiar-rol`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          usuarioId:  parseInt(userId),
          nuevoRolId: parseInt(nuevoRolId)
        })
      });
      if (r.ok) {
        mostrarToast('success', 'Rol actualizado correctamente');
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al cambiar el rol');
        await cargarUsuarios();
      }
    } catch { mostrarToast('error', 'Error de conexion al cambiar el rol'); }
  }
</script>

<!-- Seccion de gestion de usuarios con cambio de rol inline -->
<section class="admin-section">
  <!-- Encabezado con titulo y boton de actualizacion -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Usuarios</h2>
      <p class="admin-section__subtitle">Gestion de roles de usuarios</p>
    </div>
    <button class="btn-add" on:click={cargarUsuarios} style="background:#4b5563">
      ↻ Actualizar
    </button>
  </div>

  {#if loadingUsuarios}
    <p class="loading-text">Cargando usuarios...</p>

  {:else if usuarios.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay usuarios registrados.</p>
    </div>

  <!-- Tabla de usuarios con selector de rol inline -->
  {:else}
    <table class="table">
      <thead class="table__head">
        <tr>
          <th class="table__header">ID</th>
          <th class="table__header">Nombre</th>
          <th class="table__header">Correo</th>
          <th class="table__header">Username</th>
          <th class="table__header">Rol</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each usuarios as usuario}
          <tr class="table__row">
            <td class="table__cell" data-label="ID">{usuario.id}</td>
            <td class="table__cell" data-label="Nombre">{usuario.nombre}</td>
            <td class="table__cell" data-label="Correo">{usuario.correo}</td>
            <td class="table__cell" data-label="Username">{usuario.username}</td>
            <td class="table__cell" data-label="Rol">
              <select class="rol-select" value={usuario.rolId}
                on:change={(e) => handleCambiarRol(usuario.id, e.target.value)}>
                {#each rolesDisponibles as rol}
                  <option value={rol.id}>{rol.nombre}</option>
                {/each}
              </select>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</section>
