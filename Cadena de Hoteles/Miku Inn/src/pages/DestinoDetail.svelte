<script>
  export let navigateTo;
  export let destinoId = 'paris';
  
  // Vuelo recomendado para París (Broom Airline)
  const vueloRecomendado = {
    id: 'out-1',
    airline: 'Broom Airline',
    airlineLogo: '✈️',
    code: 'AF 1234',
    image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
    departure: { time: '08:00', code: 'GUA', city: 'Guatemala City' },
    arrival: { time: '18:30', code: 'CDG', city: 'París' },
    type: 'Directo',
    duration: '10h 30m',
    prices: { turista: 650, ejecutiva: 1450 },
    rating: 4.5,
    aircraft: 'Boeing 787-9',
    seatsAvailable: { turista: 45, ejecutiva: 12 },
    amenities: ['WiFi a bordo', 'Entretenimiento', 'Comida incluida', 'Equipaje: 2 maletas']
  };
  
  // Información del destino (solo París implementado completamente)
  const destinoInfo = {
    name: 'París',
    country: 'Francia',
    flag: '🇫🇷',
    heroImage: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1600',
    description: 'París, la Ciudad de la Luz, es uno de los destinos más románticos y culturalmente ricos del mundo. Con sus icónicos monumentos, museos de clase mundial, gastronomía exquisita y un encanto único en cada rincón.',
    highlights: [
      'Torre Eiffel y Campos de Marte',
      'Museo del Louvre y sus obras maestras',
      'Catedral de Notre-Dame',
      'Arco del Triunfo y Campos Elíseos',
      'Barrio de Montmartre',
      'Crucero por el Sena'
    ],
    weather: {
      temp: '18°C',
      condition: 'Soleado',
      icon: '☀️'
    },
    bestTime: 'Primavera (Abril-Junio) y Otoño (Septiembre-Octubre)',
    currency: 'Euro (€)',
    language: 'Francés'
  };
  
  // Hoteles destacados en París
  const hotelesDestacados = [
    {
      id: 1,
      name: 'Grand Miku Palace Paris',
      image: 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=600',
      stars: 5,
      rating: 4.8,
      reviews: 1247,
      price: 280,
      location: 'Champs-Élysées',
      amenities: ['WiFi', 'Spa', 'Piscina', 'Restaurante']
    },
    {
      id: 2,
      name: 'Miku Luxury Suites',
      image: 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=600',
      stars: 5,
      rating: 4.9,
      reviews: 892,
      price: 320,
      location: 'Torre Eiffel',
      amenities: ['WiFi', 'Desayuno', 'Gimnasio', 'Bar']
    },
    {
      id: 3,
      name: 'Miku Boutique Hotel',
      image: 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=600',
      stars: 4,
      rating: 4.6,
      reviews: 654,
      price: 180,
      location: 'Le Marais',
      amenities: ['WiFi', 'Desayuno', 'Terraza', 'Concierge']
    }
  ];
  
  function buscarHoteles() {
    // Navegar a búsqueda con París pre-seleccionado
    navigateTo('search-results', { destination: 'Paris' });
  }
  
  function verHotelDetalle(hotelId) {
    navigateTo('hotel-detail', hotelId);
  }
</script>

<div class="destino-detail-page">
  <!-- Back Button -->
  <button class="back-btn" on:click={() => navigateTo('destinos')}>
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M19 12H5M12 19l-7-7 7-7"/>
    </svg>
    Volver a Destinos
  </button>

  <!-- Hero Section -->
  <div class="hero-section">
    <img src={destinoInfo.heroImage} alt={destinoInfo.name} class="hero-image" />
    <div class="hero-overlay"></div>
    <div class="hero-content">
      <div class="hero-badge">
        <span class="flag">{destinoInfo.flag}</span>
        <span>{destinoInfo.country}</span>
      </div>
      <h1 class="hero-title">{destinoInfo.name}</h1>
      <p class="hero-description">{destinoInfo.description}</p>
      
      <div class="quick-info">
        <div class="quick-info-item">
          <span class="icon">{destinoInfo.weather.icon}</span>
          <span>{destinoInfo.weather.temp}</span>
        </div>
        <div class="quick-info-item">
          <span class="icon">💶</span>
          <span>{destinoInfo.currency}</span>
        </div>
        <div class="quick-info-item">
          <span class="icon">🗣️</span>
          <span>{destinoInfo.language}</span>
        </div>
      </div>
    </div>
  </div>

  <div class="container">
    <!-- Vuelo Recomendado (Solo para París) -->
    <div class="section">
      <div class="section-header-with-badge">
        <h2 class="section-title">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 8h5l-1.405-1.405A2.032 2.032 0 0 0 19.185 6H15m-7 8H3l1.405 1.405A2.032 2.032 0 0 0 5.815 16H9m6-11 3.5 3.5M6 18l3.5-3.5M4 12h16"></path>
          </svg>
          Vuelo Recomendado con Nuestra Aerolínea Aliada
        </h2>
        <div class="recommendation-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
          Recomendado
        </div>
      </div>

      <div class="flight-card">
        <div class="flight-header">
          <div class="airline-info">
            <div class="airline-logo">{vueloRecomendado.airlineLogo}</div>
            <div>
              <div class="airline-name">{vueloRecomendado.airline}</div>
              <div class="flight-code">{vueloRecomendado.code} • {vueloRecomendado.aircraft}</div>
            </div>
          </div>
          <div class="flight-rating">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span>{vueloRecomendado.rating}</span>
          </div>
        </div>

        <div class="flight-route">
          <div class="route-point">
            <div class="time">{vueloRecomendado.departure.time}</div>
            <div class="code">{vueloRecomendado.departure.code}</div>
            <div class="city">{vueloRecomendado.departure.city}</div>
          </div>

          <div class="route-middle">
            <div class="duration-badge">{vueloRecomendado.duration}</div>
            <div class="route-line">
              <div class="route-dot"></div>
              <div class="route-plane">✈️</div>
              <div class="route-dot"></div>
            </div>
            <div class="flight-type">{vueloRecomendado.type}</div>
          </div>

          <div class="route-point">
            <div class="time">{vueloRecomendado.arrival.time}</div>
            <div class="code">{vueloRecomendado.arrival.code}</div>
            <div class="city">{vueloRecomendado.arrival.city}</div>
          </div>
        </div>

        <div class="flight-amenities">
          {#each vueloRecomendado.amenities as amenity}
            <div class="amenity-tag">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              {amenity}
            </div>
          {/each}
        </div>

        <div class="flight-footer">
          <div class="flight-prices">
            <div class="price-option">
              <span class="class-label">Turista</span>
              <span class="price">${vueloRecomendado.prices.turista}</span>
            </div>
            <div class="price-divider"></div>
            <div class="price-option">
              <span class="class-label">Ejecutiva</span>
              <span class="price">${vueloRecomendado.prices.ejecutiva}</span>
            </div>
          </div>
          <div class="flight-info-text">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            Vuelo informativo. Para reservar, contacta con nuestros aliados de {vueloRecomendado.airline}
          </div>
        </div>
      </div>
    </div>

    <!-- Información del Destino -->
    <div class="section">
      <h2 class="section-title">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="16" x2="12" y2="12"></line>
          <line x1="12" y1="8" x2="12.01" y2="8"></line>
        </svg>
        Sobre {destinoInfo.name}
      </h2>

      <div class="info-grid">
        <div class="info-card">
          <h3>Mejor Época para Visitar</h3>
          <p>{destinoInfo.bestTime}</p>
        </div>
        
        <div class="info-card">
          <h3>Lugares Destacados</h3>
          <ul class="highlights-list">
            {#each destinoInfo.highlights as highlight}
              <li>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                {highlight}
              </li>
            {/each}
          </ul>
        </div>
      </div>
    </div>

    <!-- Hoteles Destacados -->
    <div class="section">
      <div class="section-header-actions">
        <h2 class="section-title">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            <polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
          Hoteles Destacados en {destinoInfo.name}
        </h2>
        <button class="view-all-btn" on:click={buscarHoteles}>
          Ver Todos los Hoteles
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
          </svg>
        </button>
      </div>

      <div class="hotels-grid">
        {#each hotelesDestacados as hotel}
          <div class="hotel-card" on:click={() => verHotelDetalle(hotel.id)}>
            <div class="hotel-image">
              <img src={hotel.image} alt={hotel.name} />
              <div class="hotel-overlay">
                <button class="view-hotel-btn">Ver Detalles</button>
              </div>
            </div>

            <div class="hotel-content">
              <div class="hotel-header">
                <div>
                  <h3 class="hotel-name">{hotel.name}</h3>
                  <p class="hotel-location">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                      <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                    {hotel.location}
                  </p>
                </div>
                <div class="hotel-stars">
                  {#each Array(hotel.stars) as _}
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                  {/each}
                </div>
              </div>

              <div class="hotel-rating">
                <div class="rating-score">{hotel.rating}</div>
                <div class="rating-text">
                  <div class="rating-label">Excepcional</div>
                  <div class="rating-reviews">{hotel.reviews} opiniones</div>
                </div>
              </div>

              <div class="hotel-amenities">
                {#each hotel.amenities as amenity}
                  <span class="amenity-chip">{amenity}</span>
                {/each}
              </div>

              <div class="hotel-footer">
                <div class="hotel-price">
                  <span class="from">Desde</span>
                  <span class="amount">${hotel.price}</span>
                  <span class="per-night">/noche</span>
                </div>
                <button class="book-btn">Reservar</button>
              </div>
            </div>
          </div>
        {/each}
      </div>

      <!-- CTA Final -->
      <div class="cta-section">
        <h3>¿Listo para tu aventura en {destinoInfo.name}?</h3>
        <p>Explora todos nuestros hoteles disponibles y encuentra el perfecto para ti</p>
        <button class="cta-btn" on:click={buscarHoteles}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          Buscar Hoteles en {destinoInfo.name}
        </button>
      </div>
    </div>
  </div>
</div>

<style>
  /* Variables */
  :root {
    --primary: #667eea;
    --secondary: #764ba2;
    --success: #10b981;
    --warning: #f59e0b;
  }

  /* Page */
  .destino-detail-page {
    min-height: 100vh;
    background: #f5f7fa;
  }

  /* Back Button */
  .back-btn {
    position: fixed;
    top: 2rem;
    left: 2rem;
    z-index: 100;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    background: white;
    border: none;
    border-radius: 12px;
    font-weight: 600;
    color: #1e293b;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;
  }

  .back-btn:hover {
    transform: translateX(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  }

  /* Hero Section */
  .hero-section {
    position: relative;
    height: 500px;
    overflow: hidden;
  }

  .hero-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .hero-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.7) 100%);
  }

  .hero-content {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 3rem;
    color: white;
  }

  .hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    font-weight: 600;
    margin-bottom: 1rem;
  }

  .flag {
    font-size: 1.5rem;
  }

  .hero-title {
    font-size: 4rem;
    font-weight: 800;
    margin: 0 0 1rem 0;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  }

  .hero-description {
    font-size: 1.25rem;
    max-width: 800px;
    line-height: 1.6;
    margin: 0 0 2rem 0;
    opacity: 0.95;
  }

  .quick-info {
    display: flex;
    gap: 2rem;
  }

  .quick-info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    font-weight: 600;
  }

  .icon {
    font-size: 1.5rem;
  }

  /* Container */
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem;
  }

  /* Section */
  .section {
    margin-bottom: 4rem;
  }

  .section-header-with-badge {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 2rem;
  }

  .section-header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 2rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 2rem 0;
  }

  .section-title svg {
    color: var(--primary);
  }

  .recommendation-badge {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    background: linear-gradient(135deg, var(--warning), #f97316);
    color: white;
    border-radius: 20px;
    font-weight: 600;
    font-size: 0.9rem;
  }

  /* Flight Card */
  .flight-card {
    background: white;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border: 2px solid var(--success);
  }

  .flight-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1.5rem;
    border-bottom: 2px solid #f1f5f9;
  }

  .airline-info {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .airline-logo {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    border-radius: 12px;
    font-size: 2rem;
  }

  .airline-name {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1e293b;
  }

  .flight-code {
    font-size: 0.9rem;
    color: #64748b;
  }

  .flight-rating {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    background: var(--warning);
    color: white;
    border-radius: 12px;
    font-weight: 700;
  }

  /* Flight Route */
  .flight-route {
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    gap: 2rem;
    margin-bottom: 2rem;
    padding: 2rem;
    background: #f8fafc;
    border-radius: 16px;
  }

  .route-point {
    text-align: center;
  }

  .time {
    font-size: 2rem;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 0.5rem;
  }

  .code {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--primary);
    margin-bottom: 0.25rem;
  }

  .city {
    font-size: 0.9rem;
    color: #64748b;
  }

  .route-middle {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
  }

  .duration-badge {
    padding: 0.5rem 1rem;
    background: var(--primary);
    color: white;
    border-radius: 20px;
    font-weight: 600;
    font-size: 0.9rem;
  }

  .route-line {
    display: flex;
    align-items: center;
    gap: 1rem;
    width: 100%;
    position: relative;
  }

  .route-line::before {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(to right, var(--primary), var(--secondary));
  }

  .route-dot {
    width: 12px;
    height: 12px;
    background: var(--primary);
    border-radius: 50%;
    border: 3px solid white;
    position: relative;
    z-index: 1;
  }

  .route-plane {
    font-size: 1.5rem;
    position: relative;
    z-index: 1;
    background: #f8fafc;
    padding: 0 0.5rem;
  }

  .flight-type {
    padding: 0.375rem 0.75rem;
    background: var(--success);
    color: white;
    border-radius: 12px;
    font-size: 0.85rem;
    font-weight: 600;
  }

  /* Flight Amenities */
  .flight-amenities {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
  }

  .amenity-tag {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    background: #f1f5f9;
    border-radius: 20px;
    font-size: 0.85rem;
    color: #475569;
  }

  .amenity-tag svg {
    color: var(--success);
  }

  /* Flight Footer */
  .flight-footer {
    padding-top: 1.5rem;
    border-top: 2px solid #f1f5f9;
  }

  .flight-prices {
    display: flex;
    align-items: center;
    gap: 2rem;
    margin-bottom: 1rem;
  }

  .price-option {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .class-label {
    font-size: 0.85rem;
    color: #64748b;
    font-weight: 500;
  }

  .price {
    font-size: 2rem;
    font-weight: 700;
    color: var(--primary);
  }

  .price-divider {
    width: 1px;
    height: 40px;
    background: #e2e8f0;
  }

  .flight-info-text {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 1rem;
    background: #eff6ff;
    border-radius: 12px;
    font-size: 0.9rem;
    color: #1e40af;
  }

  .flight-info-text svg {
    color: #3b82f6;
    flex-shrink: 0;
  }

  /* Info Grid */
  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
  }

  .info-card {
    background: white;
    padding: 2rem;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }

  .info-card h3 {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 1rem 0;
  }

  .info-card p {
    color: #64748b;
    line-height: 1.6;
    margin: 0;
  }

  .highlights-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .highlights-list li {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    color: #475569;
  }

  .highlights-list svg {
    color: var(--success);
    flex-shrink: 0;
  }

  /* Hotels Grid */
  .hotels-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 2rem;
    margin-bottom: 3rem;
  }

  .hotel-card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    cursor: pointer;
  }

  .hotel-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  }

  .hotel-image {
    position: relative;
    height: 200px;
    overflow: hidden;
  }

  .hotel-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  .hotel-card:hover .hotel-image img {
    transform: scale(1.1);
  }

  .hotel-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  .hotel-card:hover .hotel-overlay {
    opacity: 1;
  }

  .view-hotel-btn {
    padding: 0.75rem 1.5rem;
    background: white;
    color: var(--primary);
    border: none;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
  }

  .hotel-content {
    padding: 1.5rem;
  }

  .hotel-header {
    display: flex;
    justify-content: space-between;
    align-items: start;
    margin-bottom: 1rem;
  }

  .hotel-name {
    font-size: 1.125rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 0.5rem 0;
  }

  .hotel-location {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    font-size: 0.85rem;
    color: #64748b;
    margin: 0;
  }

  .hotel-stars {
    display: flex;
    gap: 0.125rem;
  }

  .hotel-stars svg {
    color: var(--warning);
  }

  .hotel-rating {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1rem;
  }

  .rating-score {
    padding: 0.5rem 0.75rem;
    background: var(--primary);
    color: white;
    border-radius: 8px;
    font-weight: 700;
    font-size: 1.125rem;
  }

  .rating-text {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }

  .rating-label {
    font-weight: 600;
    color: #1e293b;
    font-size: 0.9rem;
  }

  .rating-reviews {
    font-size: 0.8rem;
    color: #64748b;
  }

  .hotel-amenities {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
  }

  .amenity-chip {
    padding: 0.375rem 0.75rem;
    background: #f1f5f9;
    border-radius: 12px;
    font-size: 0.8rem;
    color: #475569;
  }

  .hotel-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 1rem;
    border-top: 1px solid #e2e8f0;
  }

  .hotel-price {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }

  .from {
    font-size: 0.8rem;
    color: #64748b;
  }

  .amount {
    font-size: 1.75rem;
    font-weight: 700;
    color: var(--primary);
  }

  .per-night {
    font-size: 0.8rem;
    color: #64748b;
  }

  .book-btn {
    padding: 0.75rem 1.5rem;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
    border: none;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .book-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4);
  }

  .view-all-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    background: white;
    color: var(--primary);
    border: 2px solid var(--primary);
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .view-all-btn:hover {
    background: var(--primary);
    color: white;
  }

  /* CTA Section */
  .cta-section {
    text-align: center;
    padding: 3rem 2rem;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    border-radius: 20px;
    color: white;
  }

  .cta-section h3 {
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 1rem 0;
  }

  .cta-section p {
    font-size: 1.125rem;
    margin: 0 0 2rem 0;
    opacity: 0.95;
  }

  .cta-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem 2rem;
    background: white;
    color: var(--primary);
    border: none;
    border-radius: 12px;
    font-size: 1.125rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .cta-btn:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
  }

  /* Responsive */
  @media (max-width: 1024px) {
    .hotels-grid {
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    }

    .flight-route {
      grid-template-columns: 1fr;
      gap: 1.5rem;
    }

    .route-line {
      transform: rotate(90deg);
      width: 100px;
      margin: 1rem auto;
    }
  }

  @media (max-width: 768px) {
    .back-btn {
      top: 1rem;
      left: 1rem;
    }

    .hero-title {
      font-size: 2.5rem;
    }

    .hero-description {
      font-size: 1rem;
    }

    .quick-info {
      flex-wrap: wrap;
    }

    .container {
      padding: 2rem 1rem;
    }

    .section-title {
      font-size: 1.5rem;
    }

    .section-header-with-badge,
    .section-header-actions {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }

    .flight-prices {
      flex-direction: column;
      gap: 1rem;
    }

    .price-divider {
      display: none;
    }

    .hotels-grid {
      grid-template-columns: 1fr;
    }
  }
</style>