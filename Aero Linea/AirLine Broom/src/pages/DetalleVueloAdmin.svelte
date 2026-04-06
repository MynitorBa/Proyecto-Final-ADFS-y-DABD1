<script>
/**
 * @file DetalleVueloAdmin.svelte
 * @description Admin-only modal that displays read-only details of a base flight record.
 * Shows general information (ID, flight number, aircraft), route with origin and destination,
 * seat capacity broken down by cabin class, pricing per class, and usage statistics.
 * Rendered as an overlay on the admin flight management panel when an admin clicks a flight row.
 */
  import '../styles/DetalleVueloAdmin.css';

  /** The base flight object to display, containing all flight fields from the admin API. @type {object} */
  export let vuelo;

  /** Callback invoked to dismiss this modal from the parent component. @type {function} */
  export let onClose;

  /**
   * Closes the modal when the user clicks directly on the semi-transparent backdrop
   * rather than on the modal content itself.
   * @param {MouseEvent} event - The DOM click event from the backdrop element.
   */
  function handleBackdropClick(event) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- Modal de solo lectura con detalles del vuelo base seleccionado por el administrador -->
<div class="modal-backdrop-admin" on:click={handleBackdropClick}>
  <div class="detalle-vuelo-admin">
    <!-- Encabezado del modal con titulo y boton de cierre -->
    <div class="detalle-vuelo-admin__header">
      <h2 class="detalle-vuelo-admin__title">Detalles del Vuelo Base</h2>
      <button class="detalle-vuelo-admin__close" on:click={onClose}>
        Cerrar
      </button>
    </div>

    <!-- Contenido del modal dividido en secciones: general, ruta, capacidad, precios y estadisticas -->
    <div class="detalle-vuelo-admin__content">
      <div class="info-section">
        <h3 class="info-section__title">Informacion General</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item__label">ID del Vuelo</span>
            <span class="info-item__value">{vuelo.id}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">Numero de Vuelo</span>
            <span class="info-item__value">{vuelo.numeroVuelo}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">Aeronave</span>
            <span class="info-item__value">{vuelo.aeronave}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="info-section__title">Ruta</h3>
        <div class="ruta-display">
          <div class="ruta-point">
            <span class="ruta-label">Origen</span>
            <span class="ruta-value">{vuelo.origen}</span>
          </div>
          <div class="ruta-arrow">→</div>
          <div class="ruta-point">
            <span class="ruta-label">Destino</span>
            <span class="ruta-value">{vuelo.destino}</span>
          </div>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item__label">Duracion</span>
            <span class="info-item__value">{vuelo.duracion}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">Escalas</span>
            <span class="info-item__value">{vuelo.escalas}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="info-section__title">Capacidad</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item__label">Asientos Turista</span>
            <span class="info-item__value">{vuelo.asientosTurista}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">Asientos Ejecutiva</span>
            <span class="info-item__value">{vuelo.asientosEjecutiva}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">Total de Asientos</span>
            <span class="info-item__value">{vuelo.asientosTurista + vuelo.asientosEjecutiva}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="info-section__title">Precios</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item__label">Precio Turista</span>
            <span class="info-item__value info-item__value--price">${vuelo.precioTurista}</span>
          </div>
          <div class="info-item">
            <span class="info-item__label">Precio Ejecutiva</span>
            <span class="info-item__value info-item__value--price">${vuelo.precioEjecutiva}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="info-section__title">Estadisticas</h3>
        <div class="stats-card">
          <div class="stat-item">
            <span class="stat-label">Veces Utilizado</span>
            <span class="stat-value">{vuelo.vecesUtilizado}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
