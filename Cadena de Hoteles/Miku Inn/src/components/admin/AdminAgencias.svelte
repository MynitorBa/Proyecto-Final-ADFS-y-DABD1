<script>
  import { onMount } from 'svelte';

  export let API_BASE;
  export let badge;
  export let count = 0;

  let agencias = [];
  let cargandoAgencias = false;
  let errorAgencias = null;
  let busquedaAgencia = '';

  let showModalEditarAgencia = false;
  let agenciaEditando = null;
  let editAgencia = { nombre: '', correo: '', porcentajeDescuento: 0, estadoId: 1 };
  let guardandoAgencia = false;
  let mensajeAgencia = null;

  $: agenciasFiltradas = agencias.filter(a =>
    a.nombre?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    a.correo?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    String(a.id).includes(busquedaAgencia)
  );

  $: count = agencias.length;

  onMount(() => { cargarAgencias(); });

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

  function abrirEditarAgencia(ag) {
    agenciaEditando = ag;
    editAgencia = {
      nombre:              ag.nombre              ?? '',
      correo:              ag.correo              ?? '',
      porcentajeDescuento: ag.porcentajeDescuento ?? 0,
      estadoId:            ag.estadoId            ?? 1,
    };
    mensajeAgencia = null;
    showModalEditarAgencia = true;
  }

  function cerrarModalAgencia() {
    showModalEditarAgencia = false;
    agenciaEditando = null;
    mensajeAgencia = null;
  }

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
          porcentajeDescuento: Number(editAgencia.porcentajeDescuento),
          estadoId:            Number(editAgencia.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);
      agencias = agencias.map(a =>
        a.id === agenciaEditando.id
          ? { ...a, ...editAgencia, estado: Number(editAgencia.estadoId) === 1 ? 'Activo' : 'Cerrado' }
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
</script>

<div class="adm__filters-bar">
  <div class="adm__search-wrap">
    <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
    <input class="adm__search-input" type="text" placeholder="Buscar por nombre, correo o ID..." bind:value={busquedaAgencia} />
  </div>
  <button class="adm__btn adm__btn--ghost" on:click={cargarAgencias} disabled={cargandoAgencias} title="Recargar">
    <svg class={cargandoAgencias ? 'adm__spinner' : ''} width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
    Recargar
  </button>
</div>

{#if errorAgencias}
  <div class="adm__feedback adm__feedback--error" style="margin-bottom:1rem">
    {errorAgencias}
    <button class="adm__btn adm__btn--ghost" on:click={cargarAgencias}>Reintentar</button>
  </div>
{/if}

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
            {#each agenciasFiltradas as ag (ag.id)}
              <tr>
                <td class="adm__table-mono" style="color:var(--adm-text-muted);font-size:.8rem">#{ag.id}</td>
                <td style="font-weight:600">{ag.nombre}</td>
                <td style="color:var(--adm-text-muted)">{ag.correo}</td>
                <td class="adm__table-mono" style="font-size:.8rem">WS #{ag.usuarioWebisId}</td>
                <td><span style="font-weight:700;color:#2dd4bf">{ag.porcentajeDescuento?.toFixed(2)}%</span></td>
                <td><span class="adm__badge {badge(ag.estado)}">{ag.estado}</span></td>
                <td>
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

<!-- Modal editar agencia -->
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
        <div class="adm__field">
          <label for="ea-descuento">Porcentaje de descuento (0–100)</label>
          <input id="ea-descuento" type="number" min="0" max="100" step="0.01" bind:value={editAgencia.porcentajeDescuento} />
        </div>
        <div class="adm__field">
          <label for="ea-estado">Estado</label>
          <select id="ea-estado" bind:value={editAgencia.estadoId}>
            <option value={1}>Activo</option>
            <option value={2}>Cerrado</option>
          </select>
        </div>
      </div>

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