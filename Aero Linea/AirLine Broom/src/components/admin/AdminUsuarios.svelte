<script>
// @ts-nocheck
  import { onMount } from 'svelte';

  export let API;
  export let mostrarToast; // fn(tipo, mensaje)

  // ── Estado ───────────────────────────────────────────────────────
  let usuarios        = [];
  let loadingUsuarios = false;

  const rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' },
    { id: 3, nombre: 'Webservice' }
  ];

  onMount(() => { cargarUsuarios(); });

  // ── Carga ────────────────────────────────────────────────────────
  async function cargarUsuarios() {
    loadingUsuarios = true;
    try {
      const r = await fetch(`${API}/api/usuarios`, { credentials: 'include' });
      if (r.ok) usuarios = await r.json();
      else mostrarToast('error', 'Error al cargar usuarios');
    } catch { mostrarToast('error', 'Error de conexión al cargar usuarios'); }
    finally { loadingUsuarios = false; }
  }

  // ── Cambiar rol ──────────────────────────────────────────────────
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
        // Recargar para revertir el select visualmente
        await cargarUsuarios();
      }
    } catch { mostrarToast('error', 'Error de conexión al cambiar el rol'); }
  }
</script>

<section class="admin-section">
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Usuarios</h2>
      <p class="admin-section__subtitle">Gestión de roles de usuarios</p>
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