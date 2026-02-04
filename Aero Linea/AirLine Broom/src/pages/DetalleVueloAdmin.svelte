<script>
  export let vuelo;
  export let onClose;

  function handleBackdropClick(event) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<!-- svelte-ignore a11y_click_events_have_key_events -->
<div class="modal-backdrop-admin" on:click={handleBackdropClick}>
  <div class="detalle-vuelo-admin">
    <div class="detalle-vuelo-admin__header">
      <h2 class="detalle-vuelo-admin__title">Detalles del Vuelo Base</h2>
      <button class="detalle-vuelo-admin__close" on:click={onClose}>
        Cerrar
      </button>
    </div>

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

<style>
    .modal-backdrop-admin {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.75);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 2rem;
    animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

.detalle-vuelo-admin {
    background: var(--white);
    border-radius: 4px 30px 4px 30px;
    max-width: 800px;
    width: 100%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 50px 100px rgba(0, 0, 0, 0.5);
    animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(50px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.detalle-vuelo-admin__header {
    background: #fcfbf9;
    padding: 2rem 2.5rem;
    border-bottom: 2px solid var(--perla-dark);
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.detalle-vuelo-admin__title {
    font-size: 1.5rem;
    font-weight: 300;
    color: var(--secondary-color);
    margin: 0;
}

.detalle-vuelo-admin__close {
    background: transparent;
    border: 2px solid var(--text-muted);
    color: var(--text-muted);
    font-size: 0.85rem;
    text-transform: uppercase;
    letter-spacing: 2px;
    cursor: pointer;
    padding: 0.6rem 1.2rem;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.detalle-vuelo-admin__close:hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
    transform: scale(1.05);
}

.detalle-vuelo-admin__content {
    padding: 2.5rem;
    display: flex;
    flex-direction: column;
    gap: 2.5rem;
}

.info-section {
    border-bottom: 1px solid var(--perla-dark);
    padding-bottom: 2rem;
}

.info-section:last-child {
    border-bottom: none;
    padding-bottom: 0;
}

.info-section__title {
    font-size: 0.9rem;
    text-transform: uppercase;
    color: var(--primary-color);
    letter-spacing: 2px;
    margin-bottom: 1.5rem;
    font-weight: 600;
}

.info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
}

.info-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.info-item__label {
    font-size: 0.7rem;
    text-transform: uppercase;
    letter-spacing: 1px;
    color: var(--text-muted);
    font-weight: 600;
}

.info-item__value {
    font-size: 1.1rem;
    color: var(--secondary-color);
    font-weight: 500;
}

.info-item__value--price {
    font-size: 1.3rem;
    color: var(--primary-color);
    font-weight: 600;
}

.ruta-display {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fcfbf9;
    padding: 2rem;
    border-radius: 4px 20px;
    margin-bottom: 1.5rem;
}

.ruta-point {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.ruta-label {
    font-size: 0.7rem;
    text-transform: uppercase;
    color: var(--text-muted);
    letter-spacing: 1px;
}

.ruta-value {
    font-size: 1.2rem;
    font-weight: 600;
    color: var(--secondary-color);
}

.ruta-arrow {
    font-size: 2rem;
    color: var(--primary-color);
    margin: 0 1rem;
}

.stats-card {
    background: linear-gradient(135deg, rgba(139, 107, 74, 0.08), rgba(139, 107, 74, 0.03));
    padding: 2rem;
    border-radius: 4px 20px;
    text-align: center;
}

.stat-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.stat-label {
    font-size: 0.8rem;
    text-transform: uppercase;
    color: var(--text-muted);
    letter-spacing: 1.5px;
}

.stat-value {
    font-size: 2.5rem;
    font-weight: 300;
    color: var(--primary-color);
}

@media (max-width: 768px) {
    .modal-backdrop-admin {
        padding: 1rem;
    }

    .detalle-vuelo-admin {
        border-radius: 4px 20px 4px 20px;
    }

    .detalle-vuelo-admin__header {
        padding: 1.5rem;
        flex-direction: column;
        gap: 1rem;
        align-items: flex-start;
    }

    .detalle-vuelo-admin__title {
        font-size: 1.2rem;
    }

    .detalle-vuelo-admin__content {
        padding: 1.5rem;
        gap: 2rem;
    }

    .info-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }

    .ruta-display {
        flex-direction: column;
        gap: 1rem;
        padding: 1.5rem;
    }

    .ruta-arrow {
        transform: rotate(90deg);
        margin: 0;
    }

    .stat-value {
        font-size: 2rem;
    }
}
</style>