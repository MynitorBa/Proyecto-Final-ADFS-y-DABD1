<script>
/**
 * @file AdminUsuarios.svelte
 * @description Seccion del panel de administracion para gestionar usuarios registrados. Muestra una tabla
 * de todos los usuarios del sistema con su ID, nombre completo, correo, nombre de usuario y rol actual.
 * El rol de cada usuario puede cambiarse inline mediante un elemento select que envia inmediatamente una
 * solicitud POST al endpoint cambiar-rol. Si la API falla, la lista de usuarios se recarga para revertir
 * visualmente el select al rol del lado del servidor. Proporciona un boton de actualizacion manual que
 * vuelve a obtener la lista de usuarios.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Lista de todos los usuarios cargados desde el backend. @type {any[]} */
  let usuarios        = [];

  /** Indica si la carga de la lista de usuarios esta en progreso. @type {boolean} */
  let loadingUsuarios = false;

  /**
   * Lista estatica de opciones de rol disponibles renderizadas en el select de rol inline para cada fila de usuario.
   * @type {{ id: number, nombre: string }[]}
   */
  const rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' },
    { id: 3, nombre: 'Webservice' }
  ];

  /**
   * Al montar: carga la lista de usuarios desde el backend.
   */
  onMount(() => { cargarUsuarios(); });

  /**
   * Obtiene todos los usuarios registrados desde la API del backend y los almacena en el arreglo usuarios.
   * Muestra un toast en caso de error y establece loadingUsuarios durante la solicitud.
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
   * Envia con POST una solicitud de cambio de rol al backend para el usuario indicado. Si tiene exito muestra
   * un toast de exito. Si falla muestra un toast de error y recarga la lista de usuarios para que el select
   * revierta visualmente al rol del lado del servidor.
   * @async
   * @param {number} userId - El ID del usuario cuyo rol se esta cambiando.
   * @param {string|number} nuevoRolId - El ID del nuevo rol a asignar.
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
