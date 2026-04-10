<script>
/**
 * @file AdminRutas.svelte
 * @description Seccion del panel de administracion para gestionar rutas de vuelo entre aeropuertos. Muestra
 * una tabla de todas las rutas registradas con origen, destino, disponibilidad de zona horaria de cada
 * aeropuerto, duracion estimada y total de vuelos. La duracion estimada de cualquier ruta puede editarse
 * directamente en la fila de la tabla. Las nuevas rutas se pueden crear mediante un modal con dropdowns
 * de busqueda de aeropuerto que excluyen el origen ya seleccionado de la lista de destinos. El componente
 * recibe la lista de aeropuertos del padre y despacha 'rutaCreada' tras una creacion de ruta exitosa.
 */
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Lista de todos los aeropuertos, proporcionada por el padre, usada para poblar los dropdowns de origen y destino. @type {any[]} */
  export let aeropuertos = [];

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  const dispatch = createEventDispatcher();

  /** Lista de rutas cargadas desde el backend. @type {any[]} */
  let rutas        = [];

  /** Indica si la carga de la lista de rutas esta en progreso. @type {boolean} */
  let loadingRutas = false;

  /** ID de la fila de ruta actualmente en modo de edicion de duracion inline, o null si ninguna. @type {number|null} */
  let editandoRutaId   = null;

  /** Valor actual del input de duracion durante la edicion inline. @type {string} */
  let rutaDuracionEdit = '';

  /** Indica si la solicitud de la API para guardar duracion esta en vuelo. @type {boolean} */
  let guardandoDuracion = false;

  /** Indica si el modal de creacion de ruta esta visible. @type {boolean} */
  let mostrarModalCrearRuta = false;

  /**
   * Datos del formulario para la nueva ruta que se esta creando en el modal.
   * @type {{ origenId: string, destinoId: string, duracion: number }}
   */
  let nuevaRuta  = { origenId: '', destinoId: '', duracion: 120 };

  /** Indica si la solicitud de la API para crear ruta esta en vuelo. @type {boolean} */
  let creandoRuta = false;

  /** Texto actual en el input de busqueda de aeropuerto de origen dentro del modal. @type {string} */
  let busquedaOrigenModal  = '';

  /** Texto actual en el input de busqueda de aeropuerto de destino dentro del modal. @type {string} */
  let busquedaDestinoModal = '';

  /** Indica si el dropdown de aeropuerto de origen dentro del modal esta abierto. @type {boolean} */
  let mostrarDropdownOrigenModal  = false;

  /** Indica si el dropdown de aeropuerto de destino dentro del modal esta abierto. @type {boolean} */
  let mostrarDropdownDestinoModal = false;

  // Filtra aeropuertos para el dropdown de origen en el modal.
  // Muestra los primeros 5 cuando la consulta tiene menos de 2 caracteres, de lo contrario filtra por nombre, codigo o ciudad.
  $: aeropuertosFiltradosOrigenModal = busquedaOrigenModal.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaOrigenModal.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaOrigenModal.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaOrigenModal.toLowerCase())
      ).slice(0, 10);

  // Filtra aeropuertos para el dropdown de destino en el modal.
  // Excluye el aeropuerto de origen actualmente seleccionado. Muestra los primeros 5 cuando la consulta tiene menos de 2 caracteres.
  $: aeropuertosFiltradosDestinoModal = busquedaDestinoModal.length < 2
    ? aeropuertos.filter(a => a.id !== parseInt(nuevaRuta.origenId)).slice(0, 5)
    : aeropuertos.filter(a =>
        a.id !== parseInt(nuevaRuta.origenId) && (
          a.nombre.toLowerCase().includes(busquedaDestinoModal.toLowerCase()) ||
          a.codigo.toLowerCase().includes(busquedaDestinoModal.toLowerCase()) ||
          a.ciudad.toLowerCase().includes(busquedaDestinoModal.toLowerCase())
        )
      ).slice(0, 10);

  // Resuelve el objeto del aeropuerto de origen actualmente seleccionado para la etiqueta de confirmacion.
  $: aeropuertoOrigenSeleccionado  = aeropuertos.find(a => a.id === parseInt(nuevaRuta.origenId));

  // Resuelve el objeto del aeropuerto de destino actualmente seleccionado para la etiqueta de confirmacion.
  $: aeropuertoDestinoSeleccionado = aeropuertos.find(a => a.id === parseInt(nuevaRuta.destinoId));

  /**
   * Establece el aeropuerto de origen desde el dropdown del modal, actualiza el texto de busqueda, cierra
   * el dropdown y limpia el destino si era el mismo aeropuerto.
   * @param {any} a - El objeto de aeropuerto seleccionado del dropdown de origen.
   */
  function seleccionarOrigenModal(a) {
    nuevaRuta.origenId = a.id;
    busquedaOrigenModal = `${a.codigo} — ${a.nombre}`;
    mostrarDropdownOrigenModal = false;
    if (parseInt(nuevaRuta.destinoId) === a.id) {
      nuevaRuta.destinoId = '';
      busquedaDestinoModal = '';
    }
  }

  /**
   * Establece el aeropuerto de destino desde el dropdown del modal y cierra el dropdown.
   * @param {any} a - El objeto de aeropuerto seleccionado del dropdown de destino.
   */
  function seleccionarDestinoModal(a) {
    nuevaRuta.destinoId = a.id;
    busquedaDestinoModal = `${a.codigo} — ${a.nombre}`;
    mostrarDropdownDestinoModal = false;
  }

  /**
   * Al montar: carga la lista de rutas desde el backend.
   */
  onMount(() => { cargarRutas(); });

  /**
   * Obtiene todas las rutas desde la API del backend y las almacena en rutas. Muestra un toast en caso
   * de error y establece loadingRutas durante la solicitud.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarRutas() {
    loadingRutas = true;
    try {
      const r = await fetch(`${API}/api/rutas`, { credentials: 'include' });
      if (r.ok) rutas = await r.json();
      else mostrarToast('error', 'Error al cargar rutas');
    } catch { mostrarToast('error', 'Error de conexion al cargar rutas'); }
    finally { loadingRutas = false; }
  }

  /**
   * Valida el formulario del modal (origen, destino, duracion valida, origen diferente a destino) y luego
   * envia la nueva ruta al backend con POST. Si tiene exito cierra el modal, reinicia el estado del formulario,
   * recarga las rutas y despacha 'rutaCreada'. Muestra toasts de error para fallos de validacion o de la API.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearRuta() {
    if (!nuevaRuta.origenId)  { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevaRuta.destinoId) { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (nuevaRuta.origenId === nuevaRuta.destinoId) { mostrarToast('error', 'El origen y destino no pueden ser el mismo'); return; }
    if (!nuevaRuta.duracion || nuevaRuta.duracion <= 0) { mostrarToast('error', 'Ingresa una duracion valida'); return; }

    creandoRuta = true;
    try {
      const r = await fetch(`${API}/api/rutas`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          origenId:         parseInt(nuevaRuta.origenId),
          destinoId:        parseInt(nuevaRuta.destinoId),
          duracionEstimada: parseInt(nuevaRuta.duracion)
        })
      });
      if (r.ok) {
        mostrarToast('success', '¡Ruta creada correctamente!');
        mostrarModalCrearRuta = false;
        nuevaRuta = { origenId: '', destinoId: '', duracion: 120 };
        busquedaOrigenModal = ''; busquedaDestinoModal = '';
        await cargarRutas();
        dispatch('rutaCreada');
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al crear la ruta');
      }
    } catch { mostrarToast('error', 'Error de conexion al crear la ruta'); }
    finally { creandoRuta = false; }
  }

  /**
   * Valida que la duracion editada inline sea un entero positivo, luego envia el valor actualizado al backend
   * con PUT. Si tiene exito reinicia el estado de edicion inline y recarga las rutas.
   * Muestra toasts de error para fallos de validacion o de la API.
   * @async
   * @param {number} rutaId - El ID de la ruta cuya duracion se esta actualizando.
   * @returns {Promise<void>}
   */
  async function guardarDuracionRuta(rutaId) {
    const minutos = parseInt(rutaDuracionEdit);
    if (!minutos || minutos <= 0) { mostrarToast('error', 'La duracion debe ser mayor a 0 minutos'); return; }
    guardandoDuracion = true;
    try {
      const r = await fetch(`${API}/api/rutas/${rutaId}/duracion`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ duracionEstimada: minutos })
      });
      if (r.ok) {
        mostrarToast('success', 'Duracion actualizada correctamente');
        editandoRutaId = null; rutaDuracionEdit = '';
        await cargarRutas();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al actualizar la duracion');
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { guardandoDuracion = false; }
  }

  /**
   * Reinicia el formulario de creacion de ruta y abre el modal.
   */
  function abrirModalCrearRuta() {
    nuevaRuta = { origenId: '', destinoId: '', duracion: 120 };
    busquedaOrigenModal = ''; busquedaDestinoModal = '';
    mostrarModalCrearRuta = true;
  }
</script>

<!-- Seccion de gestion de rutas con tabla de duraciones editables inline -->
<section class="admin-section">
  <!-- Encabezado con descripcion de zonas horarias y botones de accion -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Rutas</h2>
      <p class="admin-section__subtitle">
        Edita la duracion estimada en minutos de cada ruta.
        La hora de llegada se calculara automaticamente usando las zonas horarias de cada aeropuerto.
      </p>
    </div>
    <div style="display:flex;gap:.75rem">
      <button class="btn-add" on:click={abrirModalCrearRuta}>+ Nueva Ruta</button>
      <button class="btn-add" on:click={cargarRutas} style="background:#4b5563">↻ Actualizar</button>
    </div>
  </div>

  {#if loadingRutas}
    <p class="loading-text">Cargando rutas...</p>

  {:else if rutas.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">
        No hay rutas registradas. Crea una ruta con el boton <strong>+ Nueva Ruta</strong>,
        o selecciona aeropuertos al crear un vuelo para generarla automaticamente.
      </p>
    </div>

  {:else}
    <!-- Aviso informativo sobre el calculo de llegada con zona horaria -->
    <div class="rutas-tz-note">
      <span>💡</span>
      <span>
        Las rutas con <strong>✔ TZ</strong> en ambos aeropuertos calcularan la hora de llegada
        con conversion de zona horaria real. Si algun aeropuerto no tiene timezone,
        editalo en <em>Gestionar Aeropuertos</em>.
      </span>
    </div>

    <!-- Tabla de rutas con zonas horarias, duracion editable inline y total de vuelos -->
    <table class="table">
      <thead class="table__head">
        <tr>
          <th class="table__header">Origen</th>
          <th class="table__header">Destino</th>
          <th class="table__header">TZ Origen</th>
          <th class="table__header">TZ Destino</th>
          <th class="table__header">Duracion (min)</th>
          <th class="table__header">Vuelos</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each rutas as ruta}
          <tr class="table__row">
            <td class="table__cell">
              <span class="ruta-code">{ruta.codigoOrigen}</span>
              <span class="ruta-name">{ruta.origen}</span>
            </td>
            <td class="table__cell">
              <span class="ruta-code">{ruta.codigoDestino}</span>
              <span class="ruta-name">{ruta.destino}</span>
            </td>
            <td class="table__cell">
              {#if ruta.zonaHorariaOrigen}
                <span class="tz-badge tz-badge--ok">✔ {ruta.zonaHorariaOrigen}</span>
              {:else}
                <span class="tz-badge tz-badge--missing">Sin TZ</span>
              {/if}
            </td>
            <td class="table__cell">
              {#if ruta.zonaHorariaDestino}
                <span class="tz-badge tz-badge--ok">✔ {ruta.zonaHorariaDestino}</span>
              {:else}
                <span class="tz-badge tz-badge--missing">Sin TZ</span>
              {/if}
            </td>
            <td class="table__cell">
              {#if editandoRutaId === ruta.id}
                <div class="duracion-edit">
                  <input type="number" class="form-input duracion-input" min="1" max="10000"
                    bind:value={rutaDuracionEdit} placeholder="min" />
                  <button class="table__action-btn table__action-btn--view"
                    disabled={guardandoDuracion}
                    on:click={() => guardarDuracionRuta(ruta.id)}>
                    {guardandoDuracion ? '...' : '✔'}
                  </button>
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => { editandoRutaId = null; rutaDuracionEdit = ''; }}>✕</button>
                </div>
              {:else}
                <span class="duracion-display">
                  <strong>{ruta.duracionEstimada}</strong> min
                  ({Math.floor(ruta.duracionEstimada / 60)}h {ruta.duracionEstimada % 60}m)
                </span>
              {/if}
            </td>
            <td class="table__cell">{ruta.totalVuelos}</td>
            <td class="table__cell">
              {#if editandoRutaId !== ruta.id}
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => { editandoRutaId = ruta.id; rutaDuracionEdit = String(ruta.duracionEstimada); }}>
                  ✎ Editar duracion
                </button>
              {/if}
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</section>

<!-- Modal de creacion de nueva ruta con dropdowns de aeropuertos y duracion estimada -->
{#if mostrarModalCrearRuta}
  <div class="modal-overlay" on:click={() => mostrarModalCrearRuta = false}
    role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:480px">
      <div class="modal__header">
        <h3 class="modal__title">Crear Nueva Ruta</h3>
        <button class="modal__close" on:click={() => mostrarModalCrearRuta = false}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleCrearRuta}>
        <p style="font-size:.88rem;color:var(--text-muted);margin-bottom:.5rem">
          Una ruta define el trayecto entre dos aeropuertos y su duracion estimada.
        </p>

        <div class="form-field">
          <label class="form-label">Aeropuerto de Origen *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={busquedaOrigenModal}
              on:focus={() => mostrarDropdownOrigenModal = true}
              on:blur={() => setTimeout(() => mostrarDropdownOrigenModal = false, 200)}
              placeholder="Buscar aeropuerto de origen..."
              autocomplete="off" />
            {#if mostrarDropdownOrigenModal && aeropuertosFiltradosOrigenModal.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaOrigenModal.length < 2}
                  <div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>
                {/if}
                {#each aeropuertosFiltradosOrigenModal as a}
                  <button type="button" class="searchable-select__option"
                    on:click={() => seleccionarOrigenModal(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoOrigenSeleccionado}
              <p class="selected-item">✔ {aeropuertoOrigenSeleccionado.codigo} — {aeropuertoOrigenSeleccionado.nombre}</p>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label class="form-label">Aeropuerto de Destino *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={busquedaDestinoModal}
              on:focus={() => mostrarDropdownDestinoModal = true}
              on:blur={() => setTimeout(() => mostrarDropdownDestinoModal = false, 200)}
              placeholder="Buscar aeropuerto de destino..."
              autocomplete="off" />
            {#if mostrarDropdownDestinoModal && aeropuertosFiltradosDestinoModal.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaDestinoModal.length < 2}
                  <div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>
                {/if}
                {#each aeropuertosFiltradosDestinoModal as a}
                  <button type="button" class="searchable-select__option"
                    on:click={() => seleccionarDestinoModal(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoDestinoSeleccionado}
              <p class="selected-item">✔ {aeropuertoDestinoSeleccionado.codigo} — {aeropuertoDestinoSeleccionado.nombre}</p>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label for="ar-duracion" class="form-label">Duracion Estimada (minutos) *</label>
          <input id="ar-duracion" type="number" class="form-input"
            bind:value={nuevaRuta.duracion}
            min="1" max="10000" placeholder="Ej: 180 para 3 horas" required />
          {#if nuevaRuta.duracion > 0}
            <small class="img-hint">
              ≈ {Math.floor(nuevaRuta.duracion / 60)}h {nuevaRuta.duracion % 60}m
            </small>
          {/if}
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary" disabled={creandoRuta}>
            {creandoRuta ? 'Creando...' : 'Crear Ruta'}
          </button>
          <button type="button" class="btn-secondary"
            on:click={() => mostrarModalCrearRuta = false}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
