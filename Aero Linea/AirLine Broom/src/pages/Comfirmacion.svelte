<script>
  import '../styles/confirmacion.css';
  import { onMount } from 'svelte';
  export let navigateTo;
  export let reservaciones = [];

  onMount(() => {
    const isLoggedIn = !!sessionStorage.getItem('usuarioId');
    if (!isLoggedIn) {
      navigateTo('acceso-denegado');
      return;
    }

    // Si no hay reservaciones, redirigir
    if (!reservaciones || reservaciones.length === 0) {
      navigateTo('home');
    }
  });

  function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  function formatTime(timeSpan) {
    if (!timeSpan) return '';
    const parts = timeSpan.split(':');
    return `${parts[0]}:${parts[1]}`;
  }

  function agruparVuelosPorRuta(boletos) {
    if (!boletos || boletos.length === 0) return [];
    
    const vuelos = {};
    
    boletos.forEach(boleto => {
      const key = `${boleto.vueloId}-${boleto.origenCodigo}-${boleto.destinoCodigo}`;
      
      if (!vuelos[key]) {
        vuelos[key] = {
          vueloId: boleto.vueloId,
          numeroVuelo: boleto.numeroVuelo,
          origen: boleto.origenCiudad,
          origenCodigo: boleto.origenCodigo,
          destino: boleto.destinoCiudad,
          destinoCodigo: boleto.destinoCodigo,
          fecha: boleto.fechaVuelo,
          horaSalida: boleto.horaSalida,
          horaLlegada: boleto.horaLlegada,
          duracion: boleto.duracionMinutos,
          clase: boleto.clase,
          cantidadPasajeros: 0
        };
      }
      
      vuelos[key].cantidadPasajeros++;
    });
    
    return Object.values(vuelos);
  }

  function obtenerPasajerosUnicos(boletos) {
    if (!boletos || boletos.length === 0) return [];
    
    const pasajerosMap = new Map();
    
    boletos.forEach(boleto => {
      if (boleto.pasajero && boleto.pasajero.id) {
        if (!pasajerosMap.has(boleto.pasajero.id)) {
          pasajerosMap.set(boleto.pasajero.id, boleto.pasajero);
        }
      }
    });
    
    return Array.from(pasajerosMap.values());
  }

  function handleDownloadPDF() {
    console.log('Descargando PDF...');
  }

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

  function handleHotelClick(hotelName) {
    console.log('Hotel seleccionado:', hotelName);
  }

  $: totalGeneral = reservaciones.reduce((sum, r) => sum + r.total, 0);
</script>

<div class="confirmacion">
  <div class="confirmacion__container">
    <div class="confirmacion__header">
      <div class="confirmacion__icon">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="38" stroke="#8B6B4A" stroke-width="4"/>
          <path d="M25 40L35 50L55 30" stroke="#8B6B4A" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h1 class="confirmacion__title">¡Reserva{reservaciones.length > 1 ? 's' : ''} confirmada{reservaciones.length > 1 ? 's' : ''}!</h1>
      <p class="confirmacion__subtitle">
        Tu{reservaciones.length > 1 ? 's' : ''} vuelo{reservaciones.length > 1 ? 's han' : ' ha'} sido reservado{reservaciones.length > 1 ? 's' : ''} exitosamente. Hemos enviado los detalles a tu correo electrónico.
      </p>
      <div class="confirmacion__codes">
        {#each reservaciones as reserva}
          <div class="confirmacion__code">
            <span class="confirmacion__code-label">Código de reservación</span>
            <span class="confirmacion__code-value">{reserva.noReservacion}</span>
          </div>
        {/each}
      </div>
    </div>

    <div class="confirmacion__content">
      {#each reservaciones as reserva, index}
        {@const vuelos = agruparVuelosPorRuta(reserva.boletos)}
        {@const pasajeros = obtenerPasajerosUnicos(reserva.boletos)}
        
        <section class="confirmacion__flight-summary">
          <h2 class="confirmacion__section-title">
            Resumen de Reservación {index + 1}
            {#if reservaciones.length > 1}
              <span class="section-subtitle">({reserva.noReservacion})</span>
            {/if}
          </h2>
          
          {#each vuelos as vuelo}
            <div class="flight-summary-card">
              <div class="flight-summary-card__header">
                <div class="flight-summary-card__route">
                  <span class="flight-summary-card__airport">{vuelo.origenCodigo}</span>
                  <div class="flight-summary-card__arrow">
                    <svg width="40" height="20" viewBox="0 0 40 20">
                      <path d="M0 10 L35 10 M25 3 L35 10 L25 17" stroke="#1C1A18" stroke-width="2" fill="none"/>
                    </svg>
                  </div>
                  <span class="flight-summary-card__airport">{vuelo.destinoCodigo}</span>
                </div>
                <span class="flight-summary-card__flight-number">{vuelo.numeroVuelo}</span>
              </div>

              <div class="flight-summary-card__details">
                <div class="flight-detail">
                  <span class="flight-detail__label">Ruta</span>
                  <span class="flight-detail__value">
                    {vuelo.origen} → {vuelo.destino}
                  </span>
                </div>
                
                <div class="flight-detail">
                  <span class="flight-detail__label">Fecha</span>
                  <span class="flight-detail__value">{formatDate(vuelo.fecha)}</span>
                </div>
                
                <div class="flight-detail">
                  <span class="flight-detail__label">Salida</span>
                  <span class="flight-detail__time">{formatTime(vuelo.horaSalida)}</span>
                </div>
                
                <div class="flight-detail">
                  <span class="flight-detail__label">Llegada</span>
                  <span class="flight-detail__time">{formatTime(vuelo.horaLlegada)}</span>
                </div>
                
                <div class="flight-detail">
                  <span class="flight-detail__label">Pasajeros</span>
                  <span class="flight-detail__value">
                    {vuelo.cantidadPasajeros} pasajero{vuelo.cantidadPasajeros > 1 ? 's' : ''}
                  </span>
                </div>
                
                <div class="flight-detail">
                  <span class="flight-detail__label">Clase</span>
                  <span class="flight-detail__value">{vuelo.clase}</span>
                </div>
              </div>
            </div>
          {/each}

          {#if pasajeros.length > 0}
            <div class="passengers-summary">
              <h3 class="passengers-summary__title">Pasajeros</h3>
              <ul class="passengers-list">
                {#each pasajeros as pasajero}
                  <li class="passenger-item">
                    <span class="passenger-name">{pasajero.nombre} {pasajero.apellido}</span>
                    <span class="passenger-passport">Pasaporte: {pasajero.pasaporte}</span>
                  </li>
                {/each}
              </ul>
            </div>
          {/if}

          <div class="flight-summary-card__footer">
            <span class="flight-summary-card__total-label">Total pagado</span>
            <span class="flight-summary-card__total-value">${reserva.total.toFixed(2)}</span>
          </div>
        </section>
      {/each}

      {#if reservaciones.length > 1}
        <div class="total-general">
          <span class="total-general__label">Total General</span>
          <span class="total-general__value">${totalGeneral.toFixed(2)}</span>
        </div>
      {/if}

      <div class="confirmacion__actions">
        <button class="confirmacion__btn confirmacion__btn--primary" on:click={handleDownloadPDF}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M6 9L10 13M10 13L14 9M10 13V3M3 17H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Descargar boleto{reservaciones.length > 1 ? 's' : ''} en PDF
        </button>
        
        <button class="confirmacion__btn confirmacion__btn--secondary" on:click={() => navigateTo('mis-reservas')}>
          Ver mis reservaciones
        </button>
        
        <button class="confirmacion__btn confirmacion__btn--outline" on:click={() => navigateTo('home')}>
          Volver al inicio
        </button>
      </div>
    </div>

    <section class="confirmacion__hotels">
      <div class="confirmacion__hotels-header">
        <h2 class="confirmacion__section-title">Hoteles recomendados</h2>
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

<style>
  .confirmacion__codes {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-top: 1.5rem;
  }

  .section-subtitle {
    font-size: 0.875rem;
    font-weight: 400;
    color: #666;
    margin-left: 0.5rem;
  }

  .passengers-summary {
    margin-top: 1.5rem;
    padding: 1rem;
    background-color: #f9f9f9;
    border-radius: 0.5rem;
  }

  .passengers-summary__title {
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 0.75rem;
    color: #333;
  }

  .passengers-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }

  .passenger-item {
    padding: 0.5rem 0;
    border-bottom: 1px solid #e5e5e5;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .passenger-item:last-child {
    border-bottom: none;
  }

  .passenger-name {
    font-weight: 500;
    color: #333;
  }

  .passenger-passport {
    font-size: 0.875rem;
    color: #666;
  }

  .total-general {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    background-color: #8B6B4A;
    color: white;
    border-radius: 0.5rem;
    margin: 2rem 0;
  }

  .total-general__label {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .total-general__value {
    font-size: 1.5rem;
    font-weight: 700;
  }
</style>