<script>
  // @ts-nocheck
  import '../styles/detallesv.css';
  import { onMount } from 'svelte';

  export let flight;
  export let onClose;

  let selectedClass = 'economico';
  let comentarios = [];
  let loadingComentarios = true;

  const amenidades = {
    economico: [
      'Equipaje de mano incluido (8kg)',
      'Asiento estándar',
      'Comida y bebida incluida',
      'Entretenimiento a bordo',
      'USB en asiento'
    ],
    ejecutivo: [
      'Equipaje de mano incluido (12kg)',
      'Equipaje facturado incluido (32kg x2)',
      'Asiento cama totalmente reclinable',
      'Menú gourmet y bar completo',
      'Entretenimiento premium con pantalla grande',
      'Kit de amenidades de lujo',
      'Acceso a sala VIP',
      'Embarque prioritario',
      'USB, toma de corriente y WiFi'
    ]
  };

  onMount(async () => {
    if (flight.rutaId) {
      await cargarComentarios();
    }
  });

  async function cargarComentarios() {
    try {
      const response = await fetch(`https://localhost:7107/api/comentarios/ruta/${flight.rutaId}`);
      if (response.ok) {
        comentarios = await response.json();
      }
    } catch (error) {
      console.error('Error cargando comentarios:', error);
    } finally {
      loadingComentarios = false;
    }
  }

  function formatHora(h) {
    if (!h) return '--:--';
    return h.substring(0, 5);
  }

  function formatDuracion(min) {
    if (!min) return 'N/A';
    const horas = Math.floor(min / 60);
    const minutos = min % 60;
    return `${horas}h ${minutos}m`;
  }

  function formatPrecio(precio) {
    if (!precio) return 'No disponible';
    return precio.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function formatFecha(fecha) {
    if (!fecha) return '';
    const date = new Date(fecha);
    const opciones = { year: 'numeric', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('es-ES', opciones);
  }

  function getEstrellas(cantidad) {
    const estrellas = [];
    for (let i = 0; i < 5; i++) {
      estrellas.push(i < cantidad);
    }
    return estrellas;
  }

  $: precioTurista   = flight.precioTurista ?? 0;
  $: precioEjecutiva = flight.precioEjecutiva ?? 0;
  $: precioMostrado  = selectedClass === 'economico' ? precioTurista : precioEjecutiva;
  $: asientosTurista = flight.boletosDisponiblesTurista ?? 0;
  $: asientosEjecutiva = flight.boletosDisponiblesEjecutiva ?? 0;
  $: asientosMostrados = selectedClass === 'economico' ? asientosTurista : asientosEjecutiva;
  $: turistaDisponible = precioTurista > 0 && asientosTurista > 0;
  $: ejecutivaDisponible = precioEjecutiva > 0 && asientosEjecutiva > 0;
  $: tripulantes = flight.tripulantes || [];

  function handleBackdropClick(e) {
    if (e.target === e.currentTarget) onClose();
  }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="dv-backdrop" on:click={handleBackdropClick}>
  <div class="dv-modal">

    <!-- HEADER -->
    <div class="dv-header">
      <div class="dv-header-left">
        <span class="dv-code">{flight.numeroVuelo}</span>
        <span class="dv-aircraft">{flight.avionMarca} {flight.avionModelo}</span>
      </div>
      <button class="dv-close" on:click={onClose}>✕ Cerrar</button>
    </div>

    <!-- HERO -->
    <div class="dv-hero">
      <div class="dv-hero-content">
        <div class="dv-hero-point">
          <div class="dv-hero-time">{formatHora(flight.horaSalida)}</div>
          <div class="dv-hero-code">{flight.origenCodigo ?? '---'}</div>
          <div class="dv-hero-label">Salida</div>
        </div>
        <div class="dv-hero-middle">
          <div class="dv-hero-line">
            <div class="dv-hero-dot"></div>
            <div class="dv-hero-track"></div>
            <span class="dv-hero-plane">✈</span>
            <div class="dv-hero-track"></div>
            <div class="dv-hero-dot"></div>
          </div>
          <div class="dv-hero-dur">{formatDuracion(flight.duracionMinutos)}</div>
          <div class="dv-hero-tipo">Directo</div>
        </div>
        <div class="dv-hero-point dv-hero-point--right">
          <div class="dv-hero-time">{formatHora(flight.horaLlegada)}</div>
          <div class="dv-hero-code">{flight.destinoCodigo ?? '---'}</div>
          <div class="dv-hero-label">Llegada</div>
        </div>
      </div>
    </div>

    <!-- BODY -->
    <div class="dv-body">
      <div class="dv-grid">

        <!-- MAIN -->
        <div class="dv-main">
          <div class="dv-section-title">Información del vuelo</div>
          <div class="dv-specs">
            <div class="dv-spec">
              <div class="dv-spec-label">Número de vuelo</div>
              <div class="dv-spec-value">{flight.numeroVuelo ?? 'N/A'}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Fecha</div>
              <div class="dv-spec-value">{flight.fecha ? flight.fecha.substring(0,10) : 'N/A'}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Aeronave</div>
              <div class="dv-spec-value">{flight.avionMarca} {flight.avionModelo}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Duración</div>
              <div class="dv-spec-value">{formatDuracion(flight.duracionMinutos)}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Asientos disponibles</div>
              <div class="dv-spec-value dv-spec-value--gold">{flight.boletosDisponibles ?? 0}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Tipo de vuelo</div>
              <div class="dv-spec-value">Directo</div>
            </div>
          </div>

          <!-- TRIPULACIÓN -->
          {#if tripulantes.length > 0}
            <div class="dv-section-title dv-section-title--mt">Tripulación</div>
            <div class="dv-tripulantes">
              {#each tripulantes as tripulante}
                <div class="dv-tripulante">
                  <div class="dv-tripulante-avatar">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </div>
                  <div class="dv-tripulante-info">
                    <div class="dv-tripulante-nombre">{tripulante.nombreCompleto}</div>
                    <div class="dv-tripulante-rol">{tripulante.nombreRol}</div>
                  </div>
                </div>
              {/each}
            </div>
          {/if}

          <div class="dv-section-title dv-section-title--mt">Clases disponibles</div>
          <div class="dv-clases">

            <button
              class="dv-clase"
              class:dv-clase--selected={selectedClass === 'economico'}
              class:dv-clase--disabled={!turistaDisponible}
              disabled={!turistaDisponible}
              on:click={() => turistaDisponible && (selectedClass = 'economico')}
            >
              {#if selectedClass === 'economico' && turistaDisponible}
                <div class="dv-clase-badge">✓ Seleccionada</div>
              {/if}
              <div class="dv-clase-name">Clase Turista</div>
              {#if turistaDisponible}
                <div class="dv-clase-avail">{asientosTurista} asientos disponibles</div>
                <div class="dv-clase-price">
                  <span class="dv-clase-desde">Desde</span>
                  <span class="dv-clase-amount">$ {formatPrecio(precioTurista)}</span>
                  <span class="dv-clase-pp">/ persona</span>
                </div>
              {:else}
                <div class="dv-clase-unavailable">No disponible</div>
              {/if}
              <ul class="dv-amenities">
                {#each amenidades.economico as a}<li>{a}</li>{/each}
              </ul>
            </button>

            <button
              class="dv-clase dv-clase--premium"
              class:dv-clase--selected={selectedClass === 'ejecutivo'}
              class:dv-clase--disabled={!ejecutivaDisponible}
              disabled={!ejecutivaDisponible}
              on:click={() => ejecutivaDisponible && (selectedClass = 'ejecutivo')}
            >
              {#if selectedClass === 'ejecutivo' && ejecutivaDisponible}
                <div class="dv-clase-badge">✓ Seleccionada</div>
              {/if}
              <div class="dv-clase-ribbon">Premium</div>
              <div class="dv-clase-name">Clase Ejecutiva</div>
              {#if ejecutivaDisponible}
                <div class="dv-clase-avail">{asientosEjecutiva} asientos disponibles</div>
                <div class="dv-clase-price">
                  <span class="dv-clase-desde">Desde</span>
                  <span class="dv-clase-amount">$ {formatPrecio(precioEjecutiva)}</span>
                  <span class="dv-clase-pp">/ persona</span>
                </div>
              {:else}
                <div class="dv-clase-unavailable">No disponible</div>
              {/if}
              <ul class="dv-amenities">
                {#each amenidades.ejecutivo as a}<li>{a}</li>{/each}
              </ul>
            </button>

          </div>

          <!-- COMENTARIOS Y RESEÑAS -->
          <div class="dv-section-title dv-section-title--mt">
            Reseñas de la ruta {flight.origenCodigo} → {flight.destinoCodigo}
          </div>
          
          {#if loadingComentarios}
            <div class="dv-loading">Cargando reseñas...</div>
          {:else if comentarios.length === 0}
            <div class="dv-no-comentarios">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
              <p>Aún no hay reseñas para esta ruta</p>
            </div>
          {:else}
            <div class="dv-comentarios">
              {#each comentarios as comentario}
                <div class="dv-comentario">
                  <div class="dv-comentario-header">
                    <div class="dv-comentario-user">
                      <div class="dv-comentario-avatar">
                        {comentario.nombreCompleto.charAt(0).toUpperCase()}
                      </div>
                      <div class="dv-comentario-user-info">
                        <div class="dv-comentario-nombre">{comentario.nombreCompleto}</div>
                        <div class="dv-comentario-username">@{comentario.username}</div>
                      </div>
                    </div>
                    <div class="dv-comentario-fecha">{formatFecha(comentario.fecha)}</div>
                  </div>
                  
                  <div class="dv-comentario-estrellas">
                    {#each getEstrellas(comentario.cantidadEstrellas) as llena}
                      <svg class="dv-estrella" class:dv-estrella--llena={llena} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill={llena ? 'currentColor' : 'none'} stroke="currentColor" stroke-width="2">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                      </svg>
                    {/each}
                  </div>

                  <div class="dv-comentario-contenido">{comentario.contenido}</div>

                  {#if comentario.downs > 0}
                    <div class="dv-comentario-downs">
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17"></path>
                      </svg>
                      {comentario.downs}
                    </div>
                  {/if}
                </div>
              {/each}
            </div>
          {/if}

        </div>

        <!-- SIDEBAR -->
        <aside class="dv-sidebar">
          <div class="dv-summary">
            <div class="dv-summary-title">Resumen del vuelo</div>
            <div class="dv-summary-row"><span>Vuelo</span><span>{flight.numeroVuelo ?? 'N/A'}</span></div>
            <div class="dv-summary-row"><span>Fecha</span><span>{flight.fecha ? flight.fecha.substring(0,10) : 'N/A'}</span></div>
            <div class="dv-summary-row"><span>Salida</span><span>{flight.origenCodigo ?? '---'} · {formatHora(flight.horaSalida)}</span></div>
            <div class="dv-summary-row"><span>Llegada</span><span>{flight.destinoCodigo ?? '---'} · {formatHora(flight.horaLlegada)}</span></div>
            <div class="dv-summary-row"><span>Duración</span><span>{formatDuracion(flight.duracionMinutos)}</span></div>
            <div class="dv-summary-row"><span>Aeronave</span><span>{flight.avionMarca} {flight.avionModelo}</span></div>
            <div class="dv-summary-divider"></div>
            <div class="dv-summary-row dv-summary-row--highlight">
              <span>Clase</span>
              <span>{selectedClass === 'economico' ? 'Turista' : 'Ejecutiva'}</span>
            </div>
            <div class="dv-summary-divider"></div>
            {#if precioMostrado > 0}
              <div class="dv-summary-row">
                <span>Precio / persona</span>
                <span class="dv-summary-price">$ {formatPrecio(precioMostrado)}</span>
              </div>
              <div class="dv-summary-row">
                <span>Asientos disponibles</span>
                <span class="dv-summary-seats">{asientosMostrados}</span>
              </div>
            {:else}
              <div class="dv-summary-row">
                <span class="dv-summary-unavailable">Clase no disponible</span>
              </div>
            {/if}
            <div class="dv-summary-note">Precio referencial · sujeto a cambios</div>
            <div class="dv-summary-disclaimer">Vista informativa. Para reservar, selecciona el vuelo en la lista principal.</div>
          </div>
        </aside>

      </div>
    </div>

  </div>
</div>

<style>
  .dv-clase--disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }

  .dv-clase-unavailable {
    color: #dc2626;
    font-weight: 700;
    font-size: 1.1rem;
    margin: 1rem 0;
  }

  .dv-summary-seats {
    color: #c9a96e;
    font-weight: 700;
  }

  .dv-summary-unavailable {
    color: #dc2626;
    font-weight: 600;
    text-align: center;
  }

  /* Estilos para tripulantes */
  .dv-tripulantes {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
    margin-top: 1rem;
  }

  .dv-tripulante {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border: 1px solid #e5e7eb;
    border-radius: 0.5rem;
    transition: all 0.2s ease;
  }

  .dv-tripulante:hover {
    border-color: #c9a96e;
    box-shadow: 0 2px 8px rgba(201, 169, 110, 0.1);
  }

  .dv-tripulante-avatar {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #c9a96e 0%, #d4b782 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .dv-tripulante-avatar svg {
    width: 24px;
    height: 24px;
    color: white;
  }

  .dv-tripulante-info {
    flex: 1;
    min-width: 0;
  }

  .dv-tripulante-nombre {
    font-weight: 600;
    font-size: 0.875rem;
    color: #1f2937;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .dv-tripulante-rol {
    font-size: 0.75rem;
    color: #c9a96e;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.025em;
    margin-top: 0.125rem;
  }

  /* Estilos para comentarios */
  .dv-loading {
    text-align: center;
    padding: 2rem;
    color: #6b7280;
    font-style: italic;
  }

  .dv-no-comentarios {
    text-align: center;
    padding: 3rem 2rem;
    color: #9ca3af;
  }

  .dv-no-comentarios svg {
    width: 48px;
    height: 48px;
    margin: 0 auto 1rem;
    opacity: 0.5;
  }

  .dv-no-comentarios p {
    font-size: 0.95rem;
    margin: 0;
  }

  .dv-comentarios {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-top: 1rem;
  }

  .dv-comentario {
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-radius: 0.75rem;
    padding: 1.25rem;
    transition: all 0.2s ease;
  }

  .dv-comentario:hover {
    border-color: #c9a96e;
    box-shadow: 0 4px 12px rgba(201, 169, 110, 0.08);
  }

  .dv-comentario-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 0.75rem;
  }

  .dv-comentario-user {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .dv-comentario-avatar {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #c9a96e 0%, #d4b782 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    color: white;
    font-size: 1.125rem;
  }

  .dv-comentario-user-info {
    display: flex;
    flex-direction: column;
  }

  .dv-comentario-nombre {
    font-weight: 600;
    color: #1f2937;
    font-size: 0.95rem;
  }

  .dv-comentario-username {
    font-size: 0.8rem;
    color: #6b7280;
  }

  .dv-comentario-fecha {
    font-size: 0.8rem;
    color: #9ca3af;
  }

  .dv-comentario-estrellas {
    display: flex;
    gap: 0.25rem;
    margin-bottom: 0.75rem;
  }

  .dv-estrella {
    width: 18px;
    height: 18px;
    color: #d1d5db;
    transition: color 0.2s;
  }

  .dv-estrella--llena {
    color: #fbbf24;
  }

  .dv-comentario-contenido {
    color: #374151;
    font-size: 0.95rem;
    line-height: 1.6;
    margin-bottom: 0.5rem;
  }

  .dv-comentario-downs {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    color: #9ca3af;
    font-size: 0.85rem;
    margin-top: 0.75rem;
  }

  .dv-comentario-downs svg {
    width: 16px;
    height: 16px;
  }
</style>