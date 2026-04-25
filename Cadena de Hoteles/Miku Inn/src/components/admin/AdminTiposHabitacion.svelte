<script>
  /**
   * @file AdminTiposHabitacion.svelte
   * @description Gestión de tipos de habitación del sistema: edición de precios y administración de imágenes.
   * Permite actualizar precios por persona y por noche mediante PATCH, y gestionar galería de imágenes.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /**
   * Función que retorna la clase CSS del badge según el estado.
   * @type {function(string): string}
   */
  export let badge;

  /**
   * Convierte un File del input a una cadena Base64 para enviarla al backend.
   * @type {function(File): Promise<string>}
   */
  export let fileToBase64;

  /** Cantidad total de tipos de habitación (para mostrar en header). @type {number} */
  export let count = 0;

  /** Lista de tipos de habitación cargados desde el backend. @type {Array<Object>} */
  let tiposHabitacion = [];

  /** Indica si la carga inicial de tipos está en progreso. @type {boolean} */
  let cargandoTipos = false;

  /** Indica si el guardado de precios está en progreso. @type {boolean} */
  let guardandoPrecios = false;

  /** Indica si una imagen está siendo subida. @type {boolean} */
  let subiendoImagen = false;

  /** Diálogo de confirmación personalizado. @type {{titulo: string, mensaje: string, onConfirm: function}|null} */
  let confirmDialog = null;

  /** Lista de notificaciones toast activas. @type {Array<{id: number, texto: string, tipo: string}>} */
  let toasts = [];

  /** Contador incremental para los IDs únicos de cada toast. @type {number} */
  let toastId = 0;

  /**
   * Abre un diálogo de confirmación personalizado antes de ejecutar una acción destructiva.
   * @param {string} t - Título del diálogo.
   * @param {string} m - Mensaje descriptivo.
   * @param {function} fn - Función a ejecutar si el usuario confirma.
   */
  function pedirConfirmacion(t, m, fn) { confirmDialog = { titulo: t, mensaje: m, onConfirm: fn }; }

  /** Cierra el diálogo de confirmación sin ejecutar la acción. */
  function cerrarConfirm() { confirmDialog = null; }

  /** Ejecuta la acción confirmada y cierra el diálogo. */
  function ejecutarConfirm() { if (confirmDialog?.onConfirm) confirmDialog.onConfirm(); confirmDialog = null; }

  /**
   * Muestra una notificación toast que desaparece automáticamente a los 3 segundos.
   * @param {string} texto - Mensaje a mostrar.
   * @param {'ok'|'error'} tipo - Tipo de notificación para el estilo visual.
   */
  function toast(texto, tipo = 'ok') { const id = ++toastId; toasts = [...toasts, { id, texto, tipo }]; setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 3000); }

  /** Tipo de habitación siendo editado actualmente (modal de precios). @type {Object|null} */
  let tipoEditando = null;

  /** Precio temporal por persona mientras se edita. @type {number} */
  let editPrecioPorPersona = 0;

  /** Precio temporal por noche mientras se edita. @type {number} */
  let editPrecioPorNoche = 0;

  /** Mensaje de retroalimentación en modal de edición de precios. @type {{tipo: string, texto: string}|null} */
  let mensajeEditPrecios = null;

  /** Tipo de habitación cuyas imágenes se están gestionando (modal de imágenes). @type {Object|null} */
  let tipoImagenes = null;

  /** ID de la imagen actualmente expandida en el lightbox. @type {number|null} */
  let imagenExpandida = null;

  onMount(() => { cargarTiposHabitacion(); });

  /**
   * Carga todos los tipos de habitación desde el backend.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarTiposHabitacion() {
    cargandoTipos = true;
    try {
      const r = await fetch(`${API_BASE}/admin/tipos-habitacion`, { credentials: 'include' });
      if (!r.ok) { const d = await r.json(); throw new Error(d.mensaje || 'Error'); }
      tiposHabitacion = await r.json();
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      cargandoTipos = false;
    }
  }

  /**
   * Abre el modal de edición de precios para un tipo específico.
   * @param {Object} tipo - Tipo de habitación a editar.
   */
  function abrirEditPrecios(tipo) {
    tipoEditando = tipo;
    editPrecioPorPersona = tipo.precioPorPersona;
    editPrecioPorNoche = tipo.precioPorNoche;
    mensajeEditPrecios = null;
  }

  /**
   * Guarda los precios actualizados mediante PATCH y actualiza la lista local.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarPrecios() {
    if (!tipoEditando) return;
    if (editPrecioPorPersona <= 0 || editPrecioPorNoche <= 0) {
      mensajeEditPrecios = { tipo: 'error', texto: 'Los precios deben ser mayores que 0.' };
      return;
    }
    guardandoPrecios = true;
    mensajeEditPrecios = null;
    try {
      const r = await fetch(`${API_BASE}/admin/tipos-habitacion/${tipoEditando.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ precioPorPersona: editPrecioPorPersona, precioPorNoche: editPrecioPorNoche })
      });
      if (!r.ok) { const d = await r.json(); throw new Error(d.mensaje || 'Error'); }
      // Actualizar en la lista local
      tiposHabitacion = tiposHabitacion.map(t => t.id === tipoEditando.id ? { ...t, precioPorPersona: editPrecioPorPersona, precioPorNoche: editPrecioPorNoche } : t);
      mensajeEditPrecios = { tipo: 'ok', texto: 'Precios actualizados.' };
      setTimeout(() => { tipoEditando = null; mensajeEditPrecios = null; }, 1200);
    } catch (e) {
      mensajeEditPrecios = { tipo: 'error', texto: e.message };
    } finally {
      guardandoPrecios = false;
    }
  }

  /**
   * Abre el modal de gestión de imágenes para un tipo específico.
   * @param {Object} tipo - Tipo de habitación.
   */
  function abrirModalImagenes(tipo) { tipoImagenes = tipo; }

  /**
   * Sube una imagen al tipo de habitación seleccionado.
   * @async
   * @param {Event} ev - Evento del input file.
   * @returns {Promise<void>}
   */
  async function subirImagenTipo(ev) {
    const f = ev.target.files[0];
    if (!f || !tipoImagenes) return;
    if (f.size > 7 * 1024 * 1024) {
      toast('La imagen excede 7 MB. Usa una imagen más pequeña.', 'error');
      ev.target.value = '';
      return;
    }
    subiendoImagen = true;
    try {
      const b = await fileToBase64(f);
      const r = await fetch(`${API_BASE}/admin/tipos-habitacion/${tipoImagenes.id}/imagenes`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ base64: b })
      });
      if (!r.ok) {
        const ct = r.headers.get('content-type') || '';
        let msg;
        if (ct.includes('application/json')) {
          const err = await r.json().catch(() => ({}));
          msg = err.mensaje || `Error ${r.status}`;
        } else {
          msg = r.status === 413 ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).' : (await r.text().catch(() => '') || `Error ${r.status}`);
        }
        throw new Error(msg);
      }
      const d = await r.json();
      // Actualizar el tipo localmente
      tiposHabitacion = tiposHabitacion.map(t => t.id === tipoImagenes.id ? { ...t, imagenesIds: [...(t.imagenesIds ?? []), d.id] } : t);
      tipoImagenes = tiposHabitacion.find(t => t.id === tipoImagenes.id);
      toast('Imagen subida');
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      subiendoImagen = false;
      ev.target.value = '';
    }
  }

  /**
   * Solicita confirmación antes de eliminar una imagen del tipo de habitación.
   * @param {number} imgId - ID de la imagen a eliminar.
   */
  function pedirEliminarImagen(imgId) {
    pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen del tipo?', () => _eliminarImagen(imgId));
  }

  /**
   * Elimina una imagen del tipo de habitación en el backend.
   * @async
   * @param {number} imgId - ID de la imagen.
   * @returns {Promise<void>}
   */
  async function _eliminarImagen(imgId) {
    try {
      await fetch(`${API_BASE}/admin/tipos-habitacion/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' });
      // Actualizar localmente
      tiposHabitacion = tiposHabitacion.map(t => t.id === tipoImagenes.id ? { ...t, imagenesIds: (t.imagenesIds ?? []).filter(i => i !== imgId) } : t);
      tipoImagenes = tiposHabitacion.find(t => t.id === tipoImagenes.id);
      toast('Imagen eliminada');
    } catch (e) {
      toast(e.message, 'error');
    }
  }
</script>

<svelte:window on:keydown={(e) => { if (e.key === 'Escape' && imagenExpandida !== null) imagenExpandida = null; }} />

<!-- Notificaciones toast -->
{#if toasts.length > 0}<div class="adm__toast-container">{#each toasts as t (t.id)}<div class="adm__toast adm__toast--{t.tipo}">{#if t.tipo === 'ok'}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/></svg>{/if}{t.texto}<button class="adm__toast__close" on:click={() => toasts = toasts.filter(x => x.id !== t.id)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>{/if}

<!-- Barra de filtros con contador y botón recargar -->
<div class="adm__filters-bar">
  <div style="display:flex;align-items:center;gap:.75rem">
    <h2 style="margin:0;font-size:1.125rem;font-weight:600;color:var(--adm-text)">Tipos de Habitación</h2>
    {#if count > 0}<span class="adm__count-label">{count} tipo{count !== 1 ? 's' : ''}</span>{/if}
  </div>
  <button class="adm__btn adm__btn--ghost" on:click={cargarTiposHabitacion} disabled={cargandoTipos}>
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
    Recargar
  </button>
</div>

<!-- Estados de carga y vacío -->
{#if cargandoTipos}
  <div class="adm__loading-state">
    <svg class="adm__spinner" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
    <p>Cargando tipos de habitación...</p>
  </div>
{:else if tiposHabitacion.length === 0}
  <div class="adm__empty-cell" style="margin-top:2rem"><p>No hay tipos de habitación en el sistema.</p></div>
{:else}
  <!-- Grid de tipos de habitación -->
  <div class="adm__stats-grid">
    {#each tiposHabitacion as tipo (tipo.id)}
      <div class="adm__card">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:1rem">
          <div style="flex:1;min-width:0">
            <h3 style="margin:0 0 .35rem;font-size:1rem;font-weight:600;color:var(--adm-text)">{tipo.nombre}</h3>
            <p style="margin:0;font-size:.78rem;color:var(--adm-text-muted)">{tipo.tipoCama}</p>
          </div>
          <div style="display:flex;flex-direction:column;align-items:flex-end;gap:.25rem;flex-shrink:0">
            <span style="font-size:.7rem;color:var(--adm-text-muted);font-weight:600">{tipo.capacidadMaxima} pers.</span>
            <span style="font-size:.7rem;color:var(--adm-text-muted)">{tipo.metrosCuadrados} m²</span>
          </div>
        </div>
        
        <!-- Precios con iconos -->
        <div style="display:flex;gap:1rem;margin:1rem 0;padding:.75rem 0;border-top:1px solid var(--adm-border-subtle);border-bottom:1px solid var(--adm-border-subtle)">
          <div style="flex:1">
            <div style="display:flex;align-items:center;gap:.35rem;margin-bottom:.25rem">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              <span style="font-size:.7rem;color:var(--adm-text-muted);text-transform:uppercase;letter-spacing:.03em">Por Persona</span>
            </div>
            <p style="margin:0;font-size:1.05rem;font-weight:700;color:var(--adm-green);font-family:monospace">${tipo.precioPorPersona.toFixed(2)}</p>
          </div>
          <div style="flex:1">
            <div style="display:flex;align-items:center;gap:.35rem;margin-bottom:.25rem">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
              <span style="font-size:.7rem;color:var(--adm-text-muted);text-transform:uppercase;letter-spacing:.03em">Por Noche</span>
            </div>
            <p style="margin:0;font-size:1.05rem;font-weight:700;color:var(--adm-green);font-family:monospace">${tipo.precioPorNoche.toFixed(2)}</p>
          </div>
        </div>

        <!-- Métrica de imágenes -->
        <div style="margin-bottom:1rem">
          <div style="display:flex;align-items:center;justify-content:space-between;padding:.5rem 0">
            <span style="font-size:.78rem;color:var(--adm-text-muted)">Imágenes registradas:</span>
            <span style="font-size:.85rem;font-weight:700;color:var(--adm-text)">{tipo.imagenesIds?.length ?? 0}</span>
          </div>
        </div>

        <!-- Botones de acción -->
        <div style="display:flex;flex-direction:column;gap:.5rem">
          <button class="adm__btn adm__btn--ghost" style="justify-content:center" on:click={() => abrirEditPrecios(tipo)}>Editar Precios</button>
          <button class="adm__btn adm__btn--primary" style="justify-content:center" on:click={() => abrirModalImagenes(tipo)}>Imágenes</button>
        </div>
      </div>
    {/each}
  </div>
{/if}

<!-- Modal de edición de precios -->
{#if tipoEditando}
  <div class="adm__overlay" on:click={() => tipoEditando = null} on:keydown={e => e.key === 'Escape' && (tipoEditando = null)} role="button" tabindex="-1" aria-label="Cerrar"></div>
  <div class="adm__hotel-modal">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:linear-gradient(135deg,#7b93ff,#4158d0)">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
      </div>
      <div class="adm__hotel-modal__info">
        <p class="adm__hotel-modal__name">{tipoEditando.nombre}</p>
        <p class="adm__hotel-modal__loc">{tipoEditando.tipoCama} · {tipoEditando.capacidadMaxima} personas · {tipoEditando.metrosCuadrados} m²</p>
      </div>
      <button class="adm__hotel-modal__close" on:click={() => tipoEditando = null}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>
    <div class="adm__hotel-modal__body">
      <div class="adm__form-grid adm__form-grid--wizard">
        <div class="adm__field">
          <label>Precio por Persona</label>
          <input type="number" bind:value={editPrecioPorPersona} min="0.01" step="0.01" />
        </div>
        <div class="adm__field">
          <label>Precio por Noche</label>
          <input type="number" bind:value={editPrecioPorNoche} min="0.01" step="0.01" />
        </div>
      </div>
      {#if mensajeEditPrecios}<div class="adm__feedback adm__feedback--{mensajeEditPrecios.tipo}" style="margin:.75rem 0">{mensajeEditPrecios.texto}</div>{/if}
    </div>
    <div class="adm__hotel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={() => tipoEditando = null}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={guardarPrecios} disabled={guardandoPrecios}>
        {#if guardandoPrecios}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Guardando...{:else}Guardar Cambios{/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal de imágenes -->
{#if tipoImagenes}
  <div class="adm__overlay" on:click={() => tipoImagenes = null} on:keydown={e => e.key === 'Escape' && (tipoImagenes = null)} role="button" tabindex="-1" aria-label="Cerrar"></div>
  <div class="adm__hotel-modal adm__hotel-modal--wide">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:linear-gradient(135deg,#7b93ff,#4158d0)">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
      </div>
      <div class="adm__hotel-modal__info">
        <p class="adm__hotel-modal__name">Imágenes — {tipoImagenes.nombre}</p>
        <p class="adm__hotel-modal__loc">{tipoImagenes.imagenesIds?.length ?? 0} imagen(es)</p>
      </div>
      <button class="adm__hotel-modal__close" on:click={() => tipoImagenes = null}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>
    <div class="adm__hotel-modal__body">
      <div class="adm__img-section-header" style="margin-bottom:.75rem">
        <span class="adm__img-section-title">{tipoImagenes.imagenesIds?.length ?? 0} imagen(es)</span>
        <label class="adm__btn adm__btn--ghost adm__upload-btn">{#if subiendoImagen}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Subiendo...{:else}+ Agregar imagen{/if}<input type="file" accept="image/*" on:change={subirImagenTipo} disabled={subiendoImagen} style="display:none" /></label>
      </div>
      {#if (tipoImagenes.imagenesIds?.length ?? 0) > 0}
        <div class="adm__img-grid">
          {#each (tipoImagenes.imagenesIds ?? []) as imgId (imgId)}
            <div class="adm__img-card">
              <img src="{API_BASE}/imagenes/habitacion/{imgId}" alt="imagen tipo {imgId}" style="cursor:zoom-in" on:click={() => imagenExpandida = imgId} on:error={(e) => { e.target.style.display='none'; e.target.nextElementSibling?.remove(); const p = e.target.parentElement; if(p && !p.querySelector('.adm__img-broken')) { const d = document.createElement('div'); d.className='adm__img-broken'; d.innerHTML='<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>'; p.appendChild(d); } }} />
              <button class="adm__img-delete" on:click={() => pedirEliminarImagen(imgId)}>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          {/each}
        </div>
      {:else}
        <div class="adm__img-empty" style="padding:2rem 0"><p>Sin imágenes aún.</p></div>
      {/if}
    </div>
    <div class="adm__hotel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={() => tipoImagenes = null}>Cerrar</button>
    </div>
  </div>
{/if}

<!-- Diálogo de confirmación personalizado -->
{#if confirmDialog}<div class="adm__overlay" on:click={cerrarConfirm} on:keydown={e => e.key === 'Escape' && cerrarConfirm()} role="button" tabindex="-1" aria-label="Cerrar"></div><div class="adm__confirm"><div class="adm__confirm__header"><div class="adm__confirm__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div><p class="adm__confirm__title">{confirmDialog.titulo}</p></div><div class="adm__confirm__body"><p>{confirmDialog.mensaje}</p></div><div class="adm__confirm__footer"><button class="adm__confirm__btn-cancel" on:click={cerrarConfirm}>Cancelar</button><button class="adm__confirm__btn-ok" on:click={ejecutarConfirm}>Confirmar</button></div></div>{/if}

{#if imagenExpandida !== null}
  <div class="adm__lightbox" on:click={() => imagenExpandida = null} on:keydown={e => e.key === 'Escape' && (imagenExpandida = null)} role="button" tabindex="-1" aria-label="Cerrar imagen">
    <img src="{API_BASE}/imagenes/habitacion/{imagenExpandida}" alt="Imagen expandida" on:click|stopPropagation style="max-width:90vw;max-height:87vh;object-fit:contain;border-radius:8px;box-shadow:0 20px 60px rgba(0,0,0,.7)" />
    <button class="adm__lightbox-close" on:click={() => imagenExpandida = null} aria-label="Cerrar">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
    </button>
  </div>
{/if}
