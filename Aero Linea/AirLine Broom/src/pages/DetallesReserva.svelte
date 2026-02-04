<script>
  import '../styles/detallereserva.css';

  export let reservation;
  export let onClose;

  const reservationData = {
    ...reservation,
    flights: reservation.flights.map(flight => ({
      ...flight,
      originCode: flight.origin === 'Ciudad de Guatemala' ? 'GUA' :
                  flight.origin === 'Paris' ? 'CDG' :
                  flight.origin === 'Madrid' ? 'MAD' :
                  flight.origin === 'Lima' ? 'LIM' :
                  flight.origin === 'Panama' ? 'PTY' :
                  flight.origin === 'Miami' ? 'MIA' : 'XXX',
      destinationCode: flight.destination === 'Ciudad de Guatemala' ? 'GUA' :
                       flight.destination === 'Paris' ? 'CDG' :
                       flight.destination === 'Madrid' ? 'MAD' :
                       flight.destination === 'Lima' ? 'LIM' :
                       flight.destination === 'Panama' ? 'PTY' :
                       flight.destination === 'Miami' ? 'MIA' : 'XXX',
      arrivalDate: flight.departureDate
    }))
  };

  function getStatusClass(status) {
    const statusMap = {
      'completado': 'status--completed',
      'confirmado': 'status--confirmed',
      'cancelado': 'status--cancelled'
    };
    return statusMap[status] || '';
  }

  function getStatusText(status) {
    const statusMap = {
      'completado': 'Completado',
      'confirmado': 'Confirmado',
      'cancelado': 'Cancelado'
    };
    return statusMap[status] || status;
  }

  function getTipoPasaporteText(tipo) {
    const tipoMap = {
      'ordinario': 'Ordinario',
      'diplomatico': 'Diplomatico',
      'oficial': 'Oficial'
    };
    return tipoMap[tipo] || tipo;
  }

  function getGeneroText(genero) {
    const generoMap = {
      'masculino': 'Masculino',
      'femenino': 'Femenino',
      'otro': 'Otro'
    };
    return generoMap[genero] || genero;
  }

  function handleDownloadTicket() {
    console.log('Descargando boleto');
  }

  function handleCheckIn() {
    console.log('Iniciando check-in');
  }

  function handleBackdropClick(event) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="modal-backdrop" on:click={handleBackdropClick}>
  <div class="detalle-reserva-modal">
    <div class="detalle-reserva__container">
      <div class="detalle-reserva__header">
        <button class="detalle-reserva__close" on:click={onClose}>
          Cerrar
        </button>
        <div class="detalle-reserva__header-content">
          <h1 class="detalle-reserva__title">Detalle de Reserva</h1>
          <span class="detalle-reserva__status {getStatusClass(reservationData.status)}">
            {getStatusText(reservationData.status)}
          </span>
        </div>
        <p class="detalle-reserva__subtitle">Reserva #{reservationData.id}</p>
      </div>

      <div class="detalle-reserva__content">
        <div class="detalle-reserva__main">
          <section class="confirmation-card">
            <div class="confirmation-card__content">
              <h2 class="confirmation-card__title">Codigo de Confirmacion</h2>
              <div class="confirmation-card__code">
                <span class="confirmation-card__code-value">{reservationData.confirmationCode}</span>
              </div>
              <p class="confirmation-card__note">Presenta este codigo al momento del check-in</p>
            </div>
          </section>

          <section class="flight-details-section">
            <h2 class="section-title">Detalles de Vuelo</h2>
            
            {#each reservationData.flights as flight, index}
              <article class="flight-detail-card">
                <div class="flight-detail-card__header">
                  <span class="flight-detail-card__badge">
                    {flight.type === 'ida' ? 'Vuelo de Ida' : 'Vuelo de Regreso'}
                  </span>
                  <span class="flight-detail-card__number">{flight.airline} {flight.flightNumber}</span>
                </div>

                <div class="flight-detail-card__route">
                  <div class="route-point">
                    <span class="route-point__code">{flight.originCode}</span>
                    <span class="route-point__city">{flight.origin}</span>
                  </div>
                  <div class="route-arrow">
                    <svg width="60" height="24" viewBox="0 0 60 24" fill="none">
                      <path d="M0 12 L55 12 M45 4 L55 12 L45 20" stroke="currentColor" stroke-width="2"/>
                    </svg>
                  </div>
                  <div class="route-point">
                    <span class="route-point__code">{flight.destinationCode}</span>
                    <span class="route-point__city">{flight.destination}</span>
                  </div>
                </div>

                <div class="flight-detail-card__info">
                  <div class="info-group">
                    <div class="info-item">
                      <span class="info-item__label">Salida</span>
                      <span class="info-item__value">{flight.departureDate}</span>
                      <span class="info-item__time">{flight.departureTime}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-item__label">Llegada</span>
                      <span class="info-item__value">{flight.arrivalDate}</span>
                      <span class="info-item__time">{flight.arrivalTime}</span>
                    </div>
                  </div>

                  <div class="info-group">
                    <div class="info-item">
                      <span class="info-item__label">Duracion</span>
                      <span class="info-item__value">{flight.duration}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-item__label">Clase</span>
                      <span class="info-item__value">{flight.class}</span>
                    </div>
                  </div>

                  {#if flight.terminal}
                    <div class="info-group">
                      <div class="info-item">
                        <span class="info-item__label">Terminal</span>
                        <span class="info-item__value">{flight.terminal}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-item__label">Puerta</span>
                        <span class="info-item__value">{flight.gate}</span>
                      </div>
                    </div>
                  {/if}
                </div>
              </article>
            {/each}
          </section>

          <section class="passengers-section">
            <h2 class="section-title">Informacion de Pasajeros</h2>
            
            <div class="passengers-grid">
              {#each reservationData.passengers as passenger, index}
                <article class="passenger-detail-card">
                  <div class="passenger-detail-card__header">
                    <h3 class="passenger-detail-card__title">Pasajero {index + 1}</h3>
                  </div>
                  
                  <div class="passenger-detail-card__content">
                    <div class="passenger-info-row">
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Nombre Completo</span>
                        <span class="passenger-info-item__value">{passenger.nombre} {passenger.apellido}</span>
                      </div>
                    </div>

                    <div class="passenger-info-row">
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Tipo de Pasaporte</span>
                        <span class="passenger-info-item__value">{getTipoPasaporteText(passenger.tipoPasaporte)}</span>
                      </div>
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Numero de Documento</span>
                        <span class="passenger-info-item__value">{passenger.numeroDocumento}</span>
                      </div>
                    </div>

                    <div class="passenger-info-row">
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Nacionalidad</span>
                        <span class="passenger-info-item__value">{passenger.nacionalidad}</span>
                      </div>
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Fecha de Nacimiento</span>
                        <span class="passenger-info-item__value">{passenger.fechaNacimiento}</span>
                      </div>
                    </div>

                    <div class="passenger-info-row">
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Genero</span>
                        <span class="passenger-info-item__value">{getGeneroText(passenger.genero)}</span>
                      </div>
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Email</span>
                        <span class="passenger-info-item__value">{passenger.email}</span>
                      </div>
                    </div>

                    <div class="passenger-info-row">
                      <div class="passenger-info-item">
                        <span class="passenger-info-item__label">Telefono</span>
                        <span class="passenger-info-item__value">{passenger.telefono}</span>
                      </div>
                    </div>
                  </div>
                </article>
              {/each}
            </div>
          </section>

          <section class="payment-section">
            <h2 class="section-title">Informacion de Reserva</h2>
            
            <div class="payment-card">
              <div class="payment-detail">
                <span class="payment-detail__label">Fecha de Reserva</span>
                <span class="payment-detail__value">{reservationData.bookingDate}</span>
              </div>
              <div class="payment-detail payment-detail--total">
                <span class="payment-detail__label">Total Pagado</span>
                <span class="payment-detail__value">${reservationData.total}</span>
              </div>
            </div>
          </section>

          <div class="detalle-reserva__actions">
            {#if reservationData.status === 'confirmado'}
              <button class="action-btn action-btn--primary" on:click={handleCheckIn}>
                Hacer Check-in
              </button>
              <button class="action-btn action-btn--secondary" on:click={handleDownloadTicket}>
                Descargar Boleto
              </button>
            {:else if reservationData.status === 'completado'}
              <button class="action-btn action-btn--secondary" on:click={handleDownloadTicket}>
                Descargar Recibo
              </button>
            {/if}
            <button class="action-btn action-btn--outline" on:click={onClose}>
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>