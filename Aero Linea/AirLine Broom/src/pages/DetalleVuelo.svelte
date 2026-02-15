<script>
  // @ts-nocheck
  import '../styles/detallesv.css';

  export let flight;
  export let onClose;

  let selectedClass = 'economico';

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

  $: precioTurista   = flight.precio ?? 0;
  $: precioEjecutiva = flight.precioEjecutiva ?? Math.round(precioTurista * 2.5);
  $: precioMostrado  = selectedClass === 'economico' ? precioTurista : precioEjecutiva;
  $: asientos        = flight.boletosDisponibles ?? 0;

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
              <div class="dv-spec-value dv-spec-value--gold">{asientos}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Tipo de vuelo</div>
              <div class="dv-spec-value">Directo</div>
            </div>
          </div>

          <div class="dv-section-title dv-section-title--mt">Clases disponibles</div>
          <div class="dv-clases">

            <button
              class="dv-clase"
              class:dv-clase--selected={selectedClass === 'economico'}
              on:click={() => selectedClass = 'economico'}
            >
              {#if selectedClass === 'economico'}
                <div class="dv-clase-badge">✓ Seleccionada</div>
              {/if}
              <div class="dv-clase-name">Clase Turista</div>
              <div class="dv-clase-avail">{asientos} asientos disponibles</div>
              <div class="dv-clase-price">
                <span class="dv-clase-desde">Desde</span>
                <span class="dv-clase-amount">${precioTurista.toLocaleString('es-GT')}</span>
                <span class="dv-clase-pp">/ persona</span>
              </div>
              <ul class="dv-amenities">
                {#each amenidades.economico as a}<li>{a}</li>{/each}
              </ul>
            </button>

            <button
              class="dv-clase dv-clase--premium"
              class:dv-clase--selected={selectedClass === 'ejecutivo'}
              on:click={() => selectedClass = 'ejecutivo'}
            >
              {#if selectedClass === 'ejecutivo'}
                <div class="dv-clase-badge">✓ Seleccionada</div>
              {/if}
              <div class="dv-clase-ribbon">Premium</div>
              <div class="dv-clase-name">Clase Ejecutiva</div>
              <div class="dv-clase-avail">Asientos disponibles</div>
              <div class="dv-clase-price">
                <span class="dv-clase-desde">Desde</span>
                <span class="dv-clase-amount">${precioEjecutiva.toLocaleString('es-GT')}</span>
                <span class="dv-clase-pp">/ persona</span>
              </div>
              <ul class="dv-amenities">
                {#each amenidades.ejecutivo as a}<li>{a}</li>{/each}
              </ul>
            </button>

          </div>
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
            <div class="dv-summary-row">
              <span>Precio / persona</span>
              <span class="dv-summary-price">${precioMostrado.toLocaleString('es-GT')}</span>
            </div>
            <div class="dv-summary-note">Precio referencial · sujeto a cambios</div>
            <div class="dv-summary-disclaimer">Vista informativa. Para reservar, selecciona el vuelo en la lista principal.</div>
          </div>
        </aside>

      </div>
    </div>

  </div>
</div>