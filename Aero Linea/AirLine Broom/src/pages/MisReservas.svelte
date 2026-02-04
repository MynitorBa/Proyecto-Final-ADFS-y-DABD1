<script>
// @ts-nocheck

  import '../styles/misreservaciones.css';
  import DetallesReservaModal from './DetallesReserva.svelte';

  export let navigateTo;

  let showDetailModal = false;
  let detailReservation = null;

  const reservasActivas = [
    {
      id: 'VGT-2026-A7B9C2',
      type: 'ida-vuelta',
      flights: [
        {
          type: 'ida',
          flightNumber: 'AF 1234',
          origin: 'Ciudad de Guatemala',
          destination: 'Paris',
          departureDate: '2026-02-15',
          departureTime: '08:00',
          arrivalTime: '18:30',
          duration: '10h 30m',
          airline: 'Air France',
          terminal: 'Terminal 2E',
          gate: 'K45',
          class: 'ejecutivo'
        },
        {
          type: 'regreso',
          flightNumber: 'AF 5678',
          origin: 'Paris',
          destination: 'Ciudad de Guatemala',
          departureDate: '2026-02-25',
          departureTime: '10:00',
          arrivalTime: '16:30',
          duration: '10h 30m',
          airline: 'Air France',
          terminal: 'Terminal 2E',
          gate: 'M12',
          class: 'ejecutivo'
        }
      ],
      passengers: [
        {
          nombre: 'Juan Carlos',
          apellido: 'Lopez Garcia',
          numeroDocumento: 'A12345678',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1990-05-15',
          genero: 'masculino',
          email: 'juan.lopez@ejemplo.com',
          telefono: '+502 1234-5678'
        },
        {
          nombre: 'Maria Elena',
          apellido: 'Rodriguez Perez',
          numeroDocumento: 'B98765432',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1992-08-20',
          genero: 'femenino',
          email: 'maria.rodriguez@ejemplo.com',
          telefono: '+502 9876-5432'
        }
      ],
      status: 'confirmado',
      total: 7448,
      paymentMethod: 'Tarjeta de credito',
      bookingDate: '2026-01-20',
      confirmationCode: 'VGT-2026-A7B9C2'
    },
    {
      id: 'VGT-2026-B3K8L5',
      type: 'ida',
      flights: [
        {
          type: 'ida',
          flightNumber: 'IB 9876',
          origin: 'Ciudad de Guatemala',
          destination: 'Madrid',
          departureDate: '2026-03-10',
          departureTime: '14:00',
          arrivalTime: '00:30',
          duration: '10h 30m',
          airline: 'Iberia',
          terminal: 'Terminal 4',
          gate: 'D12',
          class: 'economico'
        }
      ],
      passengers: [
        {
          nombre: 'Carlos Alberto',
          apellido: 'Martinez Gonzalez',
          numeroDocumento: 'C45678901',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1985-03-25',
          genero: 'masculino',
          email: 'carlos.martinez@ejemplo.com',
          telefono: '+502 5555-1234'
        }
      ],
      status: 'confirmado',
      total: 1700,
      paymentMethod: 'Tarjeta de debito',
      bookingDate: '2026-01-25',
      confirmationCode: 'VGT-2026-B3K8L5'
    }
  ];

  const reservasFinalizadas = [
    {
      id: 'VGT-2025-X9Y2Z7',
      type: 'ida-vuelta',
      flights: [
        {
          type: 'ida',
          flightNumber: 'LA 7890',
          origin: 'Ciudad de Guatemala',
          destination: 'Lima',
          departureDate: '2025-12-15',
          departureTime: '06:00',
          arrivalTime: '11:30',
          airline: 'LATAM',
          class: 'economico'
        },
        {
          type: 'regreso',
          flightNumber: 'LA 7891',
          origin: 'Lima',
          destination: 'Ciudad de Guatemala',
          departureDate: '2025-12-25',
          departureTime: '13:00',
          arrivalTime: '18:30',
          airline: 'LATAM',
          class: 'economico'
        }
      ],
      passengers: [
        {
          nombre: 'Juan Carlos',
          apellido: 'Lopez Garcia',
          numeroDocumento: 'A12345678',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1990-05-15',
          genero: 'masculino',
          email: 'juan.lopez@ejemplo.com',
          telefono: '+502 1234-5678'
        }
      ],
      status: 'completado',
      total: 850,
      paymentMethod: 'Tarjeta de credito',
      bookingDate: '2025-11-10',
      completedDate: '2025-12-25',
      confirmationCode: 'VGT-2025-X9Y2Z7'
    },
    {
      id: 'VGT-2025-P4Q7R1',
      type: 'ida',
      flights: [
        {
          type: 'ida',
          flightNumber: 'CM 4521',
          origin: 'Ciudad de Guatemala',
          destination: 'Panama',
          departureDate: '2025-10-05',
          departureTime: '09:00',
          arrivalTime: '11:15',
          airline: 'Copa Airlines',
          class: 'economico'
        }
      ],
      passengers: [
        {
          nombre: 'Ana Sofia',
          apellido: 'Hernandez Castro',
          numeroDocumento: 'D78901234',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1988-11-30',
          genero: 'femenino',
          email: 'ana.hernandez@ejemplo.com',
          telefono: '+502 7777-8888'
        }
      ],
      status: 'completado',
      total: 450,
      paymentMethod: 'PayPal',
      bookingDate: '2025-09-15',
      completedDate: '2025-10-05',
      confirmationCode: 'VGT-2025-P4Q7R1'
    },
    {
      id: 'VGT-2025-W5E8T2',
      type: 'ida-vuelta',
      flights: [
        {
          type: 'ida',
          flightNumber: 'AA 2134',
          origin: 'Ciudad de Guatemala',
          destination: 'Miami',
          departureDate: '2025-08-20',
          departureTime: '11:00',
          arrivalTime: '15:30',
          airline: 'American Airlines',
          class: 'ejecutivo'
        },
        {
          type: 'regreso',
          flightNumber: 'AA 2135',
          origin: 'Miami',
          destination: 'Ciudad de Guatemala',
          departureDate: '2025-08-30',
          departureTime: '08:00',
          arrivalTime: '12:30',
          airline: 'American Airlines',
          class: 'ejecutivo'
        }
      ],
      passengers: [
        {
          nombre: 'Laura Patricia',
          apellido: 'Morales Alvarez',
          numeroDocumento: 'E23456789',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1995-07-12',
          genero: 'femenino',
          email: 'laura.morales@ejemplo.com',
          telefono: '+502 4444-9999'
        },
        {
          nombre: 'Roberto',
          apellido: 'Jimenez Torres',
          numeroDocumento: 'F87654321',
          tipoPasaporte: 'ordinario',
          nacionalidad: 'Guatemala',
          fechaNacimiento: '1993-12-05',
          genero: 'masculino',
          email: 'roberto.jimenez@ejemplo.com',
          telefono: '+502 6666-3333'
        }
      ],
      status: 'completado',
      total: 4500,
      paymentMethod: 'Tarjeta de credito',
      bookingDate: '2025-07-10',
      completedDate: '2025-08-30',
      confirmationCode: 'VGT-2025-W5E8T2'
    }
  ];

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

  function viewDetails(reservation) {
    detailReservation = reservation;
    showDetailModal = true;
  }

  function closeModal() {
    showDetailModal = false;
    detailReservation = null;
  }

  function handleDownloadTicket(reservationId) {
    console.log('Descargando boleto:', reservationId);
  }

  function handleCheckIn(reservationId) {
    console.log('Check-in para reserva:', reservationId);
  }
</script>

{#if showDetailModal && detailReservation}
  <DetallesReservaModal reservation={detailReservation} onClose={closeModal} />
{/if}

<div class="mis-reservas">
  <div class="mis-reservas__container">
    <div class="mis-reservas__header">
      <button class="mis-reservas__back" on:click={() => navigateTo('home')}>
        Volver al inicio
      </button>
      <h1 class="mis-reservas__title">Mis Reservas</h1>
      <p class="mis-reservas__subtitle">Gestiona todas tus reservas de vuelo</p>
    </div>

    <div class="mis-reservas__content">
      <section class="reservas-section">
        <h2 class="reservas-section__title">Reservas Activas</h2>
        <p class="reservas-section__subtitle">
          {reservasActivas.length} reserva{reservasActivas.length !== 1 ? 's' : ''} activa{reservasActivas.length !== 1 ? 's' : ''}
        </p>

        <div class="reservas-grid">
          {#each reservasActivas as reserva}
            <article class="reserva-card">
              <div class="reserva-card__header">
                <div class="reserva-card__id-section">
                  <h3 class="reserva-card__id">#{reserva.id}</h3>
                  <span class="reserva-card__type">
                    {reserva.type === 'ida-vuelta' ? 'Ida y Vuelta' : 'Solo Ida'}
                  </span>
                </div>
                <span class="reserva-card__status {getStatusClass(reserva.status)}">
                  {getStatusText(reserva.status)}
                </span>
              </div>

              <div class="reserva-card__flights">
                {#each reserva.flights as flight}
                  <div class="flight-info">
                    <div class="flight-info__badge">
                      {flight.type === 'ida' ? 'Ida' : 'Regreso'}
                    </div>
                    <div class="flight-info__main">
                      <div class="flight-info__route">
                        <span class="flight-info__airport">{flight.origin}</span>
                        <span class="flight-info__arrow">→</span>
                        <span class="flight-info__airport">{flight.destination}</span>
                      </div>
                      <div class="flight-info__details">
                        <span class="flight-info__detail">{flight.airline} {flight.flightNumber}</span>
                        <span class="flight-info__detail">{flight.departureDate}</span>
                        <span class="flight-info__detail">{flight.departureTime} - {flight.arrivalTime}</span>
                        <span class="flight-info__detail">Clase {flight.class}</span>
                      </div>
                      {#if flight.terminal}
                        <div class="flight-info__terminal">
                          <span>{flight.terminal}</span>
                          <span>Puerta {flight.gate}</span>
                        </div>
                      {/if}
                    </div>
                  </div>
                {/each}
              </div>

              <div class="reserva-card__passengers">
                <h4 class="reserva-card__passengers-title">Pasajeros ({reserva.passengers.length})</h4>
                <ul class="passengers-list">
                  {#each reserva.passengers as passenger}
                    <li class="passenger-item">
                      <span class="passenger-item__name">{passenger.nombre} {passenger.apellido}</span>
                      <span class="passenger-item__doc">Doc: {passenger.numeroDocumento}</span>
                    </li>
                  {/each}
                </ul>
              </div>

              <div class="reserva-card__details">
                <div class="detail-row">
                  <span class="detail-row__label">Codigo de confirmacion</span>
                  <span class="detail-row__value">{reserva.confirmationCode}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-row__label">Fecha de reserva</span>
                  <span class="detail-row__value">{reserva.bookingDate}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-row__label">Metodo de pago</span>
                  <span class="detail-row__value">{reserva.paymentMethod}</span>
                </div>
                <div class="detail-row detail-row--total">
                  <span class="detail-row__label">Total pagado</span>
                  <span class="detail-row__value">${reserva.total}</span>
                </div>
              </div>

              <div class="reserva-card__actions">
                <button class="action-btn action-btn--primary" on:click={() => viewDetails(reserva)}>
                  Ver Detalles
                </button>
                <button class="action-btn" on:click={() => handleDownloadTicket(reserva.id)}>
                  Descargar Boleto
                </button>
                <button class="action-btn" on:click={() => handleCancelReservation(reserva.id)}>
                  Cancelar Reserva
                </button>
              </div>
            </article>
          {/each}
        </div>
      </section>

      <section class="reservas-section">
        <h2 class="reservas-section__title">Historial</h2>
        <p class="reservas-section__subtitle">Reservas completadas y canceladas</p>

        <div class="reservas-grid">
          {#each reservasFinalizadas as reserva}
            <article class="reserva-card reserva-card--completed">
              <div class="reserva-card__header">
                <div class="reserva-card__id-section">
                  <h3 class="reserva-card__id">#{reserva.id}</h3>
                  <span class="reserva-card__type">
                    {reserva.type === 'ida-vuelta' ? 'Ida y Vuelta' : 'Solo Ida'}
                  </span>
                </div>
                <span class="reserva-card__status {getStatusClass(reserva.status)}">
                  {getStatusText(reserva.status)}
                </span>
              </div>

              <div class="reserva-card__flights">
                {#each reserva.flights as flight}
                  <div class="flight-info">
                    <div class="flight-info__badge">
                      {flight.type === 'ida' ? 'Ida' : 'Regreso'}
                    </div>
                    <div class="flight-info__main">
                      <div class="flight-info__route">
                        <span class="flight-info__airport">{flight.origin}</span>
                        <span class="flight-info__arrow">→</span>
                        <span class="flight-info__airport">{flight.destination}</span>
                      </div>
                      <div class="flight-info__details">
                        <span class="flight-info__detail">{flight.airline} {flight.flightNumber}</span>
                        <span class="flight-info__detail">{flight.departureDate}</span>
                        <span class="flight-info__detail">Clase {flight.class}</span>
                      </div>
                    </div>
                  </div>
                {/each}
              </div>

              <div class="reserva-card__passengers">
                <h4 class="reserva-card__passengers-title">Pasajeros ({reserva.passengers.length})</h4>
                <ul class="passengers-list">
                  {#each reserva.passengers as passenger}
                    <li class="passenger-item">
                      <span class="passenger-item__name">{passenger.nombre} {passenger.apellido}</span>
                      <span class="passenger-item__doc">Doc: {passenger.numeroDocumento}</span>
                    </li>
                  {/each}
                </ul>
              </div>

              <div class="reserva-card__details">
                <div class="detail-row">
                  <span class="detail-row__label">Fecha de reserva</span>
                  <span class="detail-row__value">{reserva.bookingDate}</span>
                </div>
                {#if reserva.status === 'completado'}
                  <div class="detail-row">
                    <span class="detail-row__label">Completado el</span>
                    <span class="detail-row__value">{reserva.completedDate}</span>
                  </div>
                {:else if reserva.status === 'cancelado'}
                  <div class="detail-row">
                    <span class="detail-row__label">Cancelado el</span>
                    <span class="detail-row__value">{reserva.cancelledDate}</span>
                  </div>
                {/if}
                <div class="detail-row detail-row--total">
                  <span class="detail-row__label">Total</span>
                  <span class="detail-row__value">${reserva.total}</span>
                </div>
              </div>

              <div class="reserva-card__actions">
                <button class="action-btn action-btn--primary" on:click={() => viewDetails(reserva)}>
                  Ver Detalles
                </button>
                {#if reserva.status === 'completado'}
                  <button class="action-btn" on:click={() => handleDownloadTicket(reserva.id)}>
                    Descargar Recibo
                  </button>
                {/if}
              </div>
            </article>
          {/each}
        </div>
      </section>
    </div>
  </div>
</div>