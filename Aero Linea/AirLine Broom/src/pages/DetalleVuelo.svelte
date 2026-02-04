<script>
  import '../styles/detallesv.css';

  export let flight;
  export let onClose;

  // Mapear las clases del vuelo original a nuestras 2 clases
  const flightDetails = {
    ...flight,
    prices: {
      economico: flight.prices.turista,
      ejecutivo: flight.prices.ejecutiva
    },
    seatsAvailable: {
      economico: flight.seatsAvailable.turista,
      ejecutivo: flight.seatsAvailable.ejecutiva
    },
    amenities: {
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
    },
    // Información adicional del avión
    aircraftInfo: {
      model: flight.aircraft,
      manufacturer: flight.aircraft.includes('Boeing') ? 'Boeing' : 'Airbus',
      capacity: 296,
      maxSpeed: '954 km/h',
      cruiseSpeed: '913 km/h',
      range: '14,140 km',
      wingspan: '60.1 m',
      length: '62.8 m'
    },
    // Información de la tripulación
    crew: {
      captain: {
        name: 'Capitán Roberto Méndez',
        experience: '15 años de experiencia',
        flights: '8,450 vuelos completados',
        image: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=400'
      },
      firstOfficer: {
        name: 'Primer Oficial Laura Gómez',
        experience: '8 años de experiencia',
        flights: '4,200 vuelos completados',
        image: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400'
      },
      cabinCrew: [
        {
          name: 'María Torres',
          role: 'Jefa de Cabina',
          image: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400'
        },
        {
          name: 'Carlos Ruiz',
          role: 'Sobrecargo Senior',
          image: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400'
        }
      ]
    },
    // Reseñas de ejemplo
    reviews: [
      {
        id: 1,
        author: 'Ana Martínez',
        rating: 5,
        date: '2026-01-15',
        class: 'Clase Ejecutiva',
        comment: 'Excelente servicio, la tripulación fue muy atenta y profesional. El asiento cama es comodísimo para vuelos largos.',
      },
      {
        id: 2,
        author: 'David Chen',
        rating: 4,
        date: '2026-01-10',
        class: 'Clase Económica',
        comment: 'Muy buen vuelo, puntual y cómodo. La comida estuvo deliciosa y el entretenimiento a bordo tiene muchas opciones.',
      },
      {
        id: 3,
        author: 'Sofía Rodríguez',
        rating: 5,
        date: '2026-01-05',
        class: 'Clase Económica',
        comment: 'Sorprendida gratamente con la clase económica. Más espacio del esperado y el personal muy amable.',
      }
    ],
    averageRating: flight.rating,
    totalReviews: 127
  };

  let selectedClass = 'economico';

  function selectClass(classType) {
    selectedClass = classType;
  }

  function handleBackdropClick(event) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }
</script>

<!-- Modal Backdrop -->
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="modal-backdrop" on:click={handleBackdropClick}>
  <div class="detalle-vuelo-modal">
    <div class="detalle-vuelo__container">
      <div class="detalle-vuelo__header">
        <button class="detalle-vuelo__close" on:click={onClose}>
         Cerrar
        </button>
      </div>

      <div class="detalle-vuelo__hero">
        <img src={flight.image} alt="" class="detalle-vuelo__hero-image" />
        <div class="detalle-vuelo__hero-overlay">
          <div class="hero-subtitle">{flight.code} - {flight.aircraft}</div>
        </div>
      </div>

      <div class="detalle-vuelo__content">
        <div class="detalle-vuelo__main">
          <!-- Información del vuelo -->
          <section class="flight-info-section">
            <div class="section-title">Información del vuelo</div>
            
            <div class="flight-timeline">
              <div class="flight-timeline__point">
                <div class="timeline-point">
                  <div class="timeline-point__content">
                    <div class="timeline-time">{flight.departure.time}</div>
                    <div class="timeline-location">{flight.departure.code}</div>
                  </div>
                </div>
              </div>

              <div class="timeline-duration">
                <div class="timeline-duration__line"></div>
                <div class="timeline-duration__info">
                  <div class="timeline-text">{flight.duration}</div>
                  <div class="timeline-text">{flight.type}</div>
                </div>
              </div>

              <div class="flight-timeline__point">
                <div class="timeline-point">
                  <div class="timeline-point__content">
                    <div class="timeline-time">{flight.arrival.time}</div>
                    <div class="timeline-location">{flight.arrival.code}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="flight-specs">
              <div class="spec-item">
                <div class="spec-label">Número de vuelo</div>
                <div class="spec-value">{flight.code}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">Aeronave</div>
                <div class="spec-value">{flight.aircraft}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">Duración</div>
                <div class="spec-value">{flight.duration}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">Tipo</div>
                <div class="spec-value">{flight.type}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">Rating</div>
                <div class="spec-value">{flight.rating} ★</div>
              </div>
            </div>
          </section>

          <!-- Detalles de Escalas (solo si hay escalas) -->
          {#if flight.stops && flight.stops.length > 0}
            <section class="stops-section">
              <div class="section-title">Detalles de escalas</div>
              
              <div class="stops-container">
                {#each flight.stops as stop, index}
                  <div class="stop-card">
                    <div class="stop-card__header">
                      <div class="stop-number">Escala {index + 1}</div>
                      <div class="stop-city">{stop.city} ({stop.code})</div>
                    </div>
                    
                    <div class="stop-card__details">
                      <div class="stop-detail-row">
                        <div class="stop-detail">
                          <div class="stop-detail__label">Llegada</div>
                          <div class="stop-detail__value">{stop.arrival}</div>
                        </div>
                        <div class="stop-detail">
                          <div class="stop-detail__label">Salida</div>
                          <div class="stop-detail__value">{stop.departure}</div>
                        </div>
                      </div>
                      
                      <div class="stop-detail-row">
                        <div class="stop-detail">
                          <div class="stop-detail__label">Tiempo de espera</div>
                          <div class="stop-detail__value stop-detail__value--highlight">{stop.duration}</div>
                        </div>
                        <div class="stop-detail">
                          <div class="stop-detail__label">Terminal</div>
                          <div class="stop-detail__value">{stop.terminal}</div>
                        </div>
                      </div>
                    </div>
                    
                    <div class="stop-card__info">
                      Durante esta escala permanecerás en el aeropuerto
                    </div>
                  </div>
                {/each}
              </div>
            </section>
          {/if}

          <!-- Información del Avión -->
          <section class="aircraft-info-section">
            <div class="section-title">Información de la aeronave</div>
            
            <div class="aircraft-card">
              <div class="aircraft-card__header">
                <div class="aircraft-model">{flightDetails.aircraftInfo.model}</div>
                <div class="aircraft-manufacturer">{flightDetails.aircraftInfo.manufacturer}</div>
              </div>

              <div class="aircraft-specs-grid">
                <div class="aircraft-spec">
                  <div class="spec-label">Capacidad</div>
                  <div class="spec-value">{flightDetails.aircraftInfo.capacity} pasajeros</div>
                </div>
                <div class="aircraft-spec">
                  <div class="spec-label">Velocidad máxima</div>
                  <div class="spec-value">{flightDetails.aircraftInfo.maxSpeed}</div>
                </div>
                <div class="aircraft-spec">
                  <div class="spec-label">Velocidad crucero</div>
                  <div class="spec-value">{flightDetails.aircraftInfo.cruiseSpeed}</div>
                </div>
                <div class="aircraft-spec">
                  <div class="spec-label">Alcance</div>
                  <div class="spec-value">{flightDetails.aircraftInfo.range}</div>
                </div>
                <div class="aircraft-spec">
                  <div class="spec-label">Envergadura</div>
                  <div class="spec-value">{flightDetails.aircraftInfo.wingspan}</div>
                </div>
                <div class="aircraft-spec">
                  <div class="spec-label">Longitud</div>
                  <div class="spec-value">{flightDetails.aircraftInfo.length}</div>
                </div>
              </div>
            </div>
          </section>

          <!-- Clases Disponibles -->
          <section class="class-selection-section">
            <div class="section-title">Clases disponibles</div>
            
            <div class="class-options">
              <button 
                class="class-card" 
                class:class-card--selected={selectedClass === 'economico'}
                on:click={() => selectClass('economico')}
              >
                <div class="class-card__header">
                  <div class="class-name">Clase Económica</div>
                  <div class="class-availability">
                    {flightDetails.seatsAvailable.economico} asientos disponibles
                  </div>
                </div>
                <div class="class-card__price">
                  <div class="price-label">Desde</div>
                  <div class="price-value">${flightDetails.prices.economico}</div>
                  <div class="price-sublabel">por persona</div>
                </div>
                <ul class="class-amenities">
                  {#each flightDetails.amenities.economico as amenity}
                    <li class="amenity-item">{amenity}</li>
                  {/each}
                </ul>
              </button>

              <button 
                class="class-card class-card--featured" 
                class:class-card--selected={selectedClass === 'ejecutivo'}
                on:click={() => selectClass('ejecutivo')}
              >
                <div class="class-card__header">
                  <div class="class-name">Clase Ejecutiva</div>
                  <div class="class-availability">
                    {flightDetails.seatsAvailable.ejecutivo} asientos disponibles
                  </div>
                </div>
                <div class="class-card__price">
                  <div class="price-label">Desde</div>
                  <div class="price-value">${flightDetails.prices.ejecutivo}</div>
                  <div class="price-sublabel">por persona</div>
                </div>
                <ul class="class-amenities">
                  {#each flightDetails.amenities.ejecutivo as amenity}
                    <li class="amenity-item">{amenity}</li>
                  {/each}
                </ul>
              </button>
            </div>
          </section>

          <!-- Reseñas -->
          <section class="reviews-section">
            <div class="section-title">Reseñas de pasajeros</div>
            
            <div class="reviews-summary">
              <div class="reviews-summary__rating">
                <div class="rating-number">{flightDetails.averageRating}</div>
                <div class="reviews-summary__stars">
                  {#each Array(5) as _, i}
                    <span class="star" class:star--filled={i < Math.floor(flightDetails.averageRating)}
                          class:star--half={i === Math.floor(flightDetails.averageRating) && flightDetails.averageRating % 1 !== 0}>
                      ★
                    </span>
                  {/each}
                </div>
                <div class="reviews-count">Basado en {flightDetails.totalReviews} reseñas</div>
              </div>
            </div>

            <div class="reviews-list">
              {#each flightDetails.reviews as review}
                <div class="review-card">
                  <div class="review-card__header">
                    <div class="review-card__author-info">
                      <div class="review-author">{review.author}</div>
                    </div>
                    <div class="review-card__rating">
                      {#each Array(5) as _, i}
                        <span class="star star--small" class:star--filled={i < review.rating}>★</span>
                      {/each}
                    </div>
                  </div>
                  <div class="review-card__meta">
                    <div class="review-class">{review.class}</div>
                    <div class="review-date">{review.date}</div>
                  </div>
                  <div class="review-comment">{review.comment}</div>
                </div>
              {/each}
            </div>
          </section>

          <!-- Tripulación -->
          <section class="crew-section">
            <div class="section-title">Tripulación</div>
            
            <div class="crew-grid">
              <!-- Capitán -->
              <div class="crew-card crew-card--main">
                <div class="crew-card__image-wrapper">
                  <img src={flightDetails.crew.captain.image} alt={flightDetails.crew.captain.name} class="crew-card__image" />
                </div>
                <div class="crew-card__info">
                  <div class="crew-name">{flightDetails.crew.captain.name}</div>
                  <div class="crew-detail">{flightDetails.crew.captain.experience}</div>
                  <div class="crew-detail">{flightDetails.crew.captain.flights}</div>
                </div>
              </div>

              <!-- Primer Oficial -->
              <div class="crew-card crew-card--main">
                <div class="crew-card__image-wrapper">
                  <img src={flightDetails.crew.firstOfficer.image} alt={flightDetails.crew.firstOfficer.name} class="crew-card__image" />
                </div>
                <div class="crew-card__info">
                  <div class="crew-name">{flightDetails.crew.firstOfficer.name}</div>
                  <div class="crew-detail">{flightDetails.crew.firstOfficer.experience}</div>
                  <div class="crew-detail">{flightDetails.crew.firstOfficer.flights}</div>
                </div>
              </div>

              <!-- Personal de Cabina -->
              {#each flightDetails.crew.cabinCrew as crew}
                <div class="crew-card">
                  <div class="crew-card__image-wrapper">
                    <img src={crew.image} alt={crew.name} class="crew-card__image" />
                  </div>
                  <div class="crew-card__info">
                    <div class="crew-name">{crew.name}</div>
                    <div class="crew-role">{crew.role}</div>
                  </div>
                </div>
              {/each}
            </div>
          </section>
        </div>

        <!-- Sidebar informativo simplificado -->
        <aside class="detalle-vuelo__sidebar">
          <div class="booking-summary">
            <div class="summary-title">Información de vuelo</div>
            
            <div class="booking-summary__flight">
              <div class="summary-row">
                <div class="summary-label">Vuelo</div>
                <div class="summary-value">{flight.code}</div>
              </div>
              <div class="summary-row">
                <div class="summary-label">Salida</div>
                <div class="summary-value">{flight.departure.code} {flight.departure.time}</div>
              </div>
              <div class="summary-row">
                <div class="summary-label">Llegada</div>
                <div class="summary-value">{flight.arrival.code} {flight.arrival.time}</div>
              </div>
              <div class="summary-row">
                <div class="summary-label">Duración</div>
                <div class="summary-value">{flight.duration}</div>
              </div>
              <div class="summary-row">
                <div class="summary-label">Tipo</div>
                <div class="summary-value">{flight.type}</div>
              </div>
            </div>

            <div class="booking-summary__divider"></div>

            <div class="booking-summary__class">
              <div class="summary-row summary-row--highlighted">
                <div class="summary-label">Clase vista</div>
                <div class="summary-value">{selectedClass === 'economico' ? 'Económica' : 'Ejecutiva'}</div>
              </div>
            </div>

            <div class="booking-summary__divider"></div>

            <div class="booking-summary__price">
              <div class="summary-row">
                <div class="summary-label">Precio {selectedClass === 'economico' ? 'económico' : 'ejecutivo'}</div>
                <div class="summary-value">${flightDetails.prices[selectedClass]}</div>
              </div>
              <div class="summary-note">Precio por persona</div>
            </div>

            <div class="booking-disclaimer">
              Esta es solo información de referencia. Para reservar, selecciona el vuelo en la página principal.
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</div>