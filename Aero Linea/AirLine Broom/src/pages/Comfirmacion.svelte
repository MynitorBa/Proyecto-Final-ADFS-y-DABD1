<script>
  import '../styles/confirmacion.css';
  export let navigateTo;

  const reservationCode = 'VGT-2026-A7B9C2';
  
  const flight = {
    route: 'Ciudad de Guatemala → Paris',
    departureDate: '15 de Febrero, 2026',
    departureTime: '08:30 AM',
    arrivalDate: '16 de Febrero, 2026',
    arrivalTime: '10:45 AM',
    flightNumber: 'AF 1234',
    passengers: 2,
    class: 'Económica',
    totalPaid: '$7,448'
  };

  const hotels = [
    {
      id: 1,
      name: 'Grand Miku Palace Paris',
      location: '15 Avenue des Champs-Élysée',
      price: '$320',
      rating: '4.8',
      image: 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800'
    },
    {
      id: 2,
      name: 'Le Miku Boutique Hotel',
      location: '28 Rue du Roi de Sicile',
      price: '$245',
      rating: '4.9',
      image: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800'
    },
    {
      id: 3,
      name: 'Paris Eiffel Tower Miku Resort',
      location: '7 Avenue de la Bourdonnais',
      price: '$450',
      rating: '4.7',
      image: 'https://images.unsplash.com/photo-1549294413-26f195200c16?w=800'
    },
  ];

  function handleDownloadPDF() {
    console.log('Descargando PDF...');
  }

  function handleHotelClick(hotelName) {
    console.log('Hotel seleccionado:', hotelName);
  }
</script>

<div class="confirmacion">
  <div class="confirmacion__container">
    <!-- Success Header -->
    <div class="confirmacion__header">
      <div class="confirmacion__icon">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="38" stroke="#8B6B4A" stroke-width="4"/>
          <path d="M25 40L35 50L55 30" stroke="#8B6B4A" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="confirmacion__title">¡Reserva confirmada!</h1>
      <p class="confirmacion__subtitle">
        Tu vuelo ha sido reservado exitosamente. Hemos enviado los detalles a tu correo electrónico.
      </p>
      <div class="confirmacion__code">
        <span class="confirmacion__code-label">Código de reservación</span>
        <span class="confirmacion__code-value">{reservationCode}</span>
      </div>
    </div>

    <!-- Flight Summary -->
    <div class="confirmacion__content">
      <section class="confirmacion__flight-summary">
        <h2 class="confirmacion__section-title">Resumen de tu vuelo</h2>
        
        <div class="flight-summary-card">
          <div class="flight-summary-card__header">
            <div class="flight-summary-card__route">
              <span class="flight-summary-card__airport">GUA</span>
              <div class="flight-summary-card__arrow">
                <svg width="40" height="20" viewBox="0 0 40 20">
                  <path d="M0 10 L35 10 M25 3 L35 10 L25 17" stroke="#1C1A18" stroke-width="2" fill="none"/>
                </svg>
              </div>
              <span class="flight-summary-card__airport">CDG</span>
            </div>
            <span class="flight-summary-card__flight-number">{flight.flightNumber}</span>
          </div>

          <div class="flight-summary-card__details">
            <div class="flight-detail">
              <span class="flight-detail__label">Ruta</span>
              <span class="flight-detail__value">{flight.route}</span>
            </div>
            
            <div class="flight-detail">
              <span class="flight-detail__label">Salida</span>
              <span class="flight-detail__value">{flight.departureDate}</span>
              <span class="flight-detail__time">{flight.departureTime}</span>
            </div>
            
            <div class="flight-detail">
              <span class="flight-detail__label">Llegada</span>
              <span class="flight-detail__value">{flight.arrivalDate}</span>
              <span class="flight-detail__time">{flight.arrivalTime}</span>
            </div>
            
            <div class="flight-detail">
              <span class="flight-detail__label">Pasajeros</span>
              <span class="flight-detail__value">{flight.passengers} pasajero{flight.passengers > 1 ? 's' : ''}</span>
            </div>
            
            <div class="flight-detail">
              <span class="flight-detail__label">Clase</span>
              <span class="flight-detail__value">{flight.class}</span>
            </div>
          </div>

          <div class="flight-summary-card__footer">
            <span class="flight-summary-card__total-label">Total pagado</span>
            <span class="flight-summary-card__total-value">{flight.totalPaid}</span>
          </div>
        </div>
      </section>

      <!-- Actions -->
      <div class="confirmacion__actions">
        <button class="confirmacion__btn confirmacion__btn--primary" on:click={handleDownloadPDF}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M6 9L10 13M10 13L14 9M10 13V3M3 17H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Descargar boleto en PDF
        </button>
        
        <button class="confirmacion__btn confirmacion__btn--secondary" on:click={() => navigateTo('reservas')}>
          Ver mis reservaciones
        </button>
        
        <button class="confirmacion__btn confirmacion__btn--outline" on:click={() => navigateTo('home')}>
          Volver al inicio
        </button>
      </div>
    </div>

    <!-- Hotels Section -->
    <section class="confirmacion__hotels">
      <div class="confirmacion__hotels-header">
        <h2 class="confirmacion__section-title">Hoteles recomendados en Paris</h2>
        <p class="confirmacion__hotels-subtitle">
          Completa tu viaje reservando un hotel en tu destino
        </p>
      </div>

      <div class="hotels-grid">
        {#each hotels as hotel}
          <!-- svelte-ignore a11y_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
          <article class="hotel-card" on:click={() => handleHotelClick(hotel.name)}>
            <div class="hotel-card__image">
              <img src={hotel.image} alt={hotel.name} />
              <div class="hotel-card__rating">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="#FFD700">
                  <path d="M8 0L10.163 5.112L16 5.888L12 9.764L12.944 16L8 13.112L3.056 16L4 9.764L0 5.888L5.837 5.112L8 0Z"/>
                </svg>
                <span>{hotel.rating}</span>
              </div>
            </div>
            
            <div class="hotel-card__content">
              <h3 class="hotel-card__name">{hotel.name}</h3>
              <p class="hotel-card__location">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M7 0C4.24 0 2 2.24 2 5C2 8.5 7 14 7 14C7 14 12 8.5 12 5C12 2.24 9.76 0 7 0ZM7 7C5.9 7 5 6.1 5 5C5 3.9 5.9 3 7 3C8.1 3 9 3.9 9 5C9 6.1 8.1 7 7 7Z" fill="#8B6B4A"/>
                </svg>
                {hotel.location}
              </p>
              <div class="hotel-card__footer">
                <span class="hotel-card__price">
                  <span class="hotel-card__price-value">{hotel.price}</span>
                  <span class="hotel-card__price-unit">/ noche</span>
                </span>
                <span class="hotel-card__link">Ver más →</span>
              </div>
            </div>
          </article>
        {/each}
      </div>
    </section>
  </div>
</div>