<script>
  export let navigateTo;
  export const destinoId = 'paris'; // fix: export const, no se usa internamente

  import '../styles/destinodetail.css';

  const vueloRecomendado = {
    id: 'out-1', airline: 'Broom Airline', airlineLogo: '✈️', code: 'AF 1234',
    image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
    departure: { time: '08:00', code: 'GUA', city: 'Guatemala City' },
    arrival:   { time: '18:30', code: 'CDG', city: 'París' },
    type: 'Directo', duration: '10h 30m', rating: 4.5, aircraft: 'Boeing 787-9',
    prices: { turista: 650, ejecutiva: 1450 },
    seatsAvailable: { turista: 45, ejecutiva: 12 },
    amenities: ['WiFi a bordo', 'Entretenimiento', 'Comida incluida', 'Equipaje: 2 maletas']
  };

  const destinoInfo = {
    name: 'París', country: 'Francia', flag: '🇫🇷',
    heroImage: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1600',
    description: 'París, la Ciudad de la Luz, es uno de los destinos más románticos y culturalmente ricos del mundo. Con sus icónicos monumentos, museos de clase mundial, gastronomía exquisita y un encanto único en cada rincón.',
    highlights: ['Torre Eiffel y Campos de Marte', 'Museo del Louvre y sus obras maestras', 'Catedral de Notre-Dame', 'Arco del Triunfo y Campos Elíseos', 'Barrio de Montmartre', 'Crucero por el Sena'],
    weather: { temp: '18°C', condition: 'Soleado', icon: '☀️' },
    bestTime: 'Primavera (Abril-Junio) y Otoño (Septiembre-Octubre)',
    currency: 'Euro (€)', language: 'Francés'
  };

  const hotelesDestacados = [
    { id: 1, name: 'Grand Miku Palace Paris', image: 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=600', stars: 5, rating: 4.8, reviews: 1247, price: 280, location: 'Champs-Élysées', amenities: ['WiFi', 'Spa', 'Piscina', 'Restaurante'] },
    { id: 2, name: 'Miku Luxury Suites',      image: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=600', stars: 5, rating: 4.9, reviews: 892,  price: 320, location: 'Torre Eiffel',    amenities: ['WiFi', 'Desayuno', 'Gimnasio', 'Bar'] },
    { id: 3, name: 'Miku Boutique Hotel',      image: 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=600', stars: 4, rating: 4.6, reviews: 654,  price: 180, location: 'Le Marais',       amenities: ['WiFi', 'Desayuno', 'Terraza', 'Concierge'] }
  ];

  const buscarHoteles   = () => navigateTo('search-results', { destination: 'Paris' });
  const verHotelDetalle = (id) => navigateTo('hotel-detail', id);
</script>

<div class="destino-detail-page">
  <button class="destdet__back-btn" on:click={() => navigateTo('destinos')}>
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
    Volver a Destinos
  </button>

  <!-- Hero -->
  <div class="destdet__hero-section">
    <img src={destinoInfo.heroImage} alt={destinoInfo.name} class="hero-image" />
    <div class="destdet__hero-overlay"></div>
    <div class="destdet__hero-content">
      <div class="hero-badge">
        <span class="destdet__flag" aria-hidden="true">{destinoInfo.flag}</span>
        <span>{destinoInfo.country}</span>
      </div>
      <h1 class="destdet__hero-title">{destinoInfo.name}</h1>
      <p class="destdet__hero-description">{destinoInfo.description}</p>
      <div class="quick-info">
        <div class="quick-info-item"><span class="icon" aria-hidden="true">{destinoInfo.weather.icon}</span><span>{destinoInfo.weather.temp}</span></div>
        <div class="quick-info-item"><span class="icon" aria-hidden="true">💶</span><span>{destinoInfo.currency}</span></div>
        <div class="quick-info-item"><span class="icon" aria-hidden="true">🗣️</span><span>{destinoInfo.language}</span></div>
      </div>
    </div>
  </div>

  <div class="destdet__container">

    <!-- Vuelo Recomendado -->
    <div class="section">
      <div class="section-header-with-badge">
        <h2 class="destdet__section-title">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M17 8h5l-1.405-1.405A2.032 2.032 0 0 0 19.185 6H15m-7 8H3l1.405 1.405A2.032 2.032 0 0 0 5.815 16H9m6-11 3.5 3.5M6 18l3.5-3.5M4 12h16"></path></svg>
          Vuelo Recomendado con Nuestra Aerolínea Aliada
        </h2>
        <div class="recommendation-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
          Recomendado
        </div>
      </div>

      <div class="flight-card">
        <div class="flight-header">
          <div class="airline-info">
            <div class="airline-logo" aria-hidden="true">{vueloRecomendado.airlineLogo}</div>
            <div>
              <div class="airline-name">{vueloRecomendado.airline}</div>
              <div class="flight-code">{vueloRecomendado.code} • {vueloRecomendado.aircraft}</div>
            </div>
          </div>
          <div class="flight-rating">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
            <span>{vueloRecomendado.rating}</span>
          </div>
        </div>

        <div class="flight-route">
          <div class="route-point">
            <div class="time">{vueloRecomendado.departure.time}</div>
            <div class="destdet__code">{vueloRecomendado.departure.code}</div>
            <div class="city">{vueloRecomendado.departure.city}</div>
          </div>
          <div class="route-middle">
            <div class="duration-badge">{vueloRecomendado.duration}</div>
            <div class="route-line">
              <div class="route-dot"></div>
              <div class="route-plane" aria-hidden="true">✈️</div>
              <div class="route-dot"></div>
            </div>
            <div class="flight-type">{vueloRecomendado.type}</div>
          </div>
          <div class="route-point">
            <div class="time">{vueloRecomendado.arrival.time}</div>
            <div class="destdet__code">{vueloRecomendado.arrival.code}</div>
            <div class="city">{vueloRecomendado.arrival.city}</div>
          </div>
        </div>

        <div class="flight-amenities">
          {#each vueloRecomendado.amenities as amenity}
            <div class="destdet__amenity-tag">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="20 6 9 17 4 12"></polyline></svg>
              {amenity}
            </div>
          {/each}
        </div>

        <div class="flight-footer">
          <div class="flight-prices">
            <div class="price-option">
              <span class="class-label">Turista</span>
              <span class="destdet__price">${vueloRecomendado.prices.turista}</span>
            </div>
            <div class="destdet__price-divider"></div>
            <div class="price-option">
              <span class="class-label">Ejecutiva</span>
              <span class="destdet__price">${vueloRecomendado.prices.ejecutiva}</span>
            </div>
          </div>
          <div class="flight-info-text">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
            Vuelo informativo. Para reservar, contacta con nuestros aliados de {vueloRecomendado.airline}
          </div>
        </div>
      </div>
    </div>

    <!-- Info Destino -->
    <div class="section">
      <h2 class="destdet__section-title">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
        Sobre {destinoInfo.name}
      </h2>
      <div class="destdet__info-grid">
        <div class="info-card">
          <h3>Mejor Época para Visitar</h3>
          <p>{destinoInfo.bestTime}</p>
        </div>
        <div class="info-card">
          <h3>Lugares Destacados</h3>
          <ul class="destdet__highlights-list">
            {#each destinoInfo.highlights as highlight}
              <li>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="20 6 9 17 4 12"></polyline></svg>
                {highlight}
              </li>
            {/each}
          </ul>
        </div>
      </div>
    </div>

    <!-- Hoteles -->
    <div class="section">
      <div class="section-header-actions">
        <h2 class="destdet__section-title">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
          Hoteles Destacados en {destinoInfo.name}
        </h2>
        <button class="view-all-btn" on:click={buscarHoteles}>
          Ver Todos los Hoteles
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
        </button>
      </div>

      <div class="destdet__hotels-grid">
        {#each hotelesDestacados as hotel}
          <!-- button en vez de div con on:click -->
          <button class="destdet__hotel-card" on:click={() => verHotelDetalle(hotel.id)} aria-label="Ver detalles de {hotel.name}">
            <div class="hotel-image">
              <img src={hotel.image} alt={hotel.name} />
              <div class="hotel-overlay" aria-hidden="true">
                <span class="view-hotel-btn">Ver Detalles</span>
              </div>
            </div>
            <div class="destdet__hotel-content">
              <div class="destdet__hotel-header">
                <div>
                  <h3 class="destdet__hotel-name">{hotel.name}</h3>
                  <p class="destdet__hotel-location">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                    {hotel.location}
                  </p>
                </div>
                <div class="destdet__hotel-stars" aria-label="{hotel.stars} estrellas">
                  {#each Array(hotel.stars) as _}
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                  {/each}
                </div>
              </div>

              <div class="hotel-rating">
                <div class="destdet__rating-score">{hotel.rating}</div>
                <div class="destdet__rating-text">
                  <div class="rating-label">Excepcional</div>
                  <div class="destdet__rating-reviews">{hotel.reviews} opiniones</div>
                </div>
              </div>

              <div class="hotel-amenities">
                {#each hotel.amenities as amenity}
                  <span class="destdet__amenity-chip">{amenity}</span>
                {/each}
              </div>

              <div class="hotel-footer">
                <div class="hotel-price">
                  <span class="destdet__from">Desde</span>
                  <span class="destdet__amount">${hotel.price}</span>
                  <span class="destdet__per-night">/noche</span>
                </div>
                <span class="destdet__book-btn">Reservar</span>
              </div>
            </div>
          </button>
        {/each}
      </div>

      <div class="destdet__cta-section">
        <h3>¿Listo para tu aventura en {destinoInfo.name}?</h3>
        <p>Explora todos nuestros hoteles disponibles y encuentra el perfecto para ti</p>
        <button class="destdet__cta-btn" on:click={buscarHoteles}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
          Buscar Hoteles en {destinoInfo.name}
        </button>
      </div>
    </div>

  </div>
</div>