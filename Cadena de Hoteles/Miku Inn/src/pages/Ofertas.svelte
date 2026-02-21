<script>
  export let navigateTo;
  import '../styles/ofertas.css';
  
  // Categorías de ofertas
  let selectedCategory = 'all';
  const categories = [
    { id: 'all', name: 'Todas', icon: 'grid' },
    { id: 'flash', name: 'Flash', icon: 'zap' },
    { id: 'lastminute', name: 'Última Hora', icon: 'clock' },
    { id: 'season', name: 'Temporada', icon: 'sun' },
    { id: 'premium', name: 'Premium', icon: 'star' },
    { id: 'packages', name: 'Paquetes', icon: 'gift' }
  ];
  
  // Ofertas disponibles
  const ofertas = [
    {
      id: 1,
      hotelId: 1,
      name: "Grand Miku Palace Paris",
      destination: "París, Francia",
      image: "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800",
      category: "flash",
      discount: 45,
      originalPrice: 350,
      offerPrice: 192,
      rating: 4.9,
      reviews: 1247,
      stars: 5,
      validUntil: "2026-02-10",
      featured: true,
      badge: "Oferta Flash",
      timeLeft: { days: 7, hours: 12, minutes: 30 },
      perks: ["Desayuno incluido", "Cancelación gratis", "WiFi premium"]
    },
    {
      id: 2,
      hotelId: 2,
      name: "Miku Luxury Suites Tokyo",
      destination: "Tokio, Japón",
      image: "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
      category: "lastminute",
      discount: 35,
      originalPrice: 420,
      offerPrice: 273,
      rating: 4.8,
      reviews: 892,
      stars: 5,
      validUntil: "2026-02-05",
      featured: true,
      badge: "Última Hora",
      timeLeft: { days: 2, hours: 8, minutes: 15 },
      perks: ["Spa gratis", "Late check-out", "Upgrade garantizado"]
    },
    {
      id: 3,
      hotelId: 3,
      name: "Miku Beach Resort Cancún",
      destination: "Cancún, México",
      image: "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800",
      category: "season",
      discount: 30,
      originalPrice: 280,
      offerPrice: 196,
      rating: 4.7,
      reviews: 654,
      stars: 4,
      validUntil: "2026-03-31",
      featured: false,
      badge: "Temporada Baja",
      timeLeft: { days: 56, hours: 0, minutes: 0 },
      perks: ["Todo incluido", "Playa privada", "Actividades gratis"]
    },
    {
      id: 4,
      hotelId: 4,
      name: "Miku Royal London",
      destination: "Londres, Reino Unido",
      image: "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800",
      category: "premium",
      discount: 25,
      originalPrice: 480,
      offerPrice: 360,
      rating: 4.9,
      reviews: 1103,
      stars: 5,
      validUntil: "2026-02-28",
      featured: false,
      badge: "Suite Premium",
      timeLeft: { days: 25, hours: 0, minutes: 0 },
      perks: ["Suite ejecutiva", "Mayordomo 24/7", "Vista panorámica"]
    },
    {
      id: 5,
      hotelId: 5,
      name: "Miku City New York",
      destination: "Nueva York, USA",
      image: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
      category: "packages",
      discount: 40,
      originalPrice: 520,
      offerPrice: 312,
      rating: 4.6,
      reviews: 987,
      stars: 5,
      validUntil: "2026-02-15",
      featured: true,
      badge: "Paquete 3 Noches",
      timeLeft: { days: 12, hours: 6, minutes: 45 },
      perks: ["3 noches al 2x1", "Tours incluidos", "Cena de cortesía"]
    },
    {
      id: 6,
      hotelId: 6,
      name: "Miku Mountain Resort",
      destination: "Aspen, Colorado",
      image: "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",
      category: "season",
      discount: 35,
      originalPrice: 380,
      offerPrice: 247,
      rating: 4.8,
      reviews: 543,
      stars: 4,
      validUntil: "2026-02-20",
      featured: false,
      badge: "Fin de Temporada",
      timeLeft: { days: 17, hours: 0, minutes: 0 },
      perks: ["Ski pass incluido", "Equipo gratis", "Chocolate caliente"]
    }
  ];
  
  // Filtrar ofertas
  $: filteredOfertas = selectedCategory === 'all' 
    ? ofertas 
    : ofertas.filter(o => o.category === selectedCategory);
  
  $: featuredOfertas = ofertas.filter(o => o.featured);
  
  // Funciones de utilidad
  function formatPrice(price) {
    return new Intl.NumberFormat('es-GT', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0
    }).format(price);
  }
  
  function viewOffer(hotelId) {
    navigateTo('hotel-detail', hotelId);
  }
  
  function getCategoryIcon(icon) {
    const icons = {
      grid: '<rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect>',
      zap: '<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>',
      clock: '<circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline>',
      sun: '<circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>',
      star: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>',
      gift: '<polyline points="20 12 20 22 4 22 4 12"></polyline><rect x="2" y="7" width="20" height="5"></rect><line x1="12" y1="22" x2="12" y2="7"></line><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"></path><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"></path>'
    };
    return icons[icon] || icons.grid;
  }
</script>

<div class="ofertas-page">
  <!-- Hero Section Profesional -->
  <section class="ofertas__hero-section">
    <div class="hero-background"></div>
    <div class="hero-container">
      <div class="ofertas__hero-content">
        <span class="hero-label">Descuentos Exclusivos</span>
        <h1 class="ofertas__hero-title">Ofertas Especiales</h1>
        <p class="ofertas__hero-description">
          Descubre nuestras promociones seleccionadas con descuentos de hasta 45%
        </p>
      </div>
      
      <div class="hero-stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
            </svg>
          </div>
          <div class="stat-value">{ofertas.length}</div>
          <div class="stat-label">Ofertas Activas</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
          </div>
          <div class="stat-value">45%</div>
          <div class="stat-label">Descuento Máximo</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
          </div>
          <div class="stat-value">6</div>
          <div class="stat-label">Destinos</div>
        </div>
      </div>
    </div>
  </section>

  <div class="main-container">
    <!-- Filtros Profesionales -->
    <section class="filters-section">
      <div class="filters-wrapper">
        {#each categories as category}
          <button 
            class="ofertas__filter-btn"
            class:active={selectedCategory === category.id}
            on:click={() => selectedCategory = category.id}
          >
            <span class="filter-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {@html getCategoryIcon(category.icon)}
              </svg>
            </span>
            <span class="filter-text">{category.name}</span>
            <span class="filter-count">
              {category.id === 'all' ? ofertas.length : ofertas.filter(o => o.category === category.id).length}
            </span>
          </button>
        {/each}
      </div>
    </section>

    <!-- Ofertas Grid Profesional -->
    <section class="offers-section">
      <div class="ofertas__section-header">
        <h2 class="ofertas__section-title">
          {selectedCategory === 'all' ? 'Todas las Ofertas' : categories.find(c => c.id === selectedCategory).name}
        </h2>
        <p class="section-subtitle">{filteredOfertas.length} {filteredOfertas.length === 1 ? 'oferta disponible' : 'ofertas disponibles'}</p>
      </div>

      <div class="offers-grid">
        {#each filteredOfertas as oferta}
          <article class="offer-card" on:click={() => viewOffer(oferta.hotelId)}>
            <!-- Imagen -->
            <div class="offer-image-wrapper">
              <img src={oferta.image} alt={oferta.name} class="offer-image" />
              
              <!-- Badges -->
              <div class="badges-container">
                <div class="ofertas__discount-badge">
                  <span class="discount-value">-{oferta.discount}%</span>
                </div>
                {#if oferta.featured}
                  <div class="ofertas__featured-badge">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                    Destacada
                  </div>
                {/if}
              </div>
            </div>

            <!-- Contenido -->
            <div class="offer-body">
              <!-- Header -->
              <div class="offer-header">
                <div class="hotel-info">
                  <h3 class="ofertas__hotel-name">{oferta.name}</h3>
                  <div class="ofertas__hotel-location">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                      <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                    {oferta.destination}
                  </div>
                </div>
                
                <div class="rating-badge">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                  </svg>
                  <span>{oferta.rating}</span>
                </div>
              </div>

              <!-- Detalles -->
              <div class="hotel-details">
                <div class="stars">
                  {#each Array(oferta.stars) as _, i}
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" class="star-icon">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                  {/each}
                  <span class="reviews-count">({oferta.reviews})</span>
                </div>
                <div class="category-tag">
                  {oferta.badge}
                </div>
              </div>

              <!-- Perks -->
              <div class="perks-list">
                {#each oferta.perks.slice(0, 3) as perk}
                  <div class="perk-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <span>{perk}</span>
                  </div>
                {/each}
              </div>

              <!-- Countdown -->
              {#if oferta.timeLeft.days < 30}
                <div class="countdown-banner">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                  <span>Termina en <strong>{oferta.timeLeft.days}d {oferta.timeLeft.hours}h</strong></span>
                </div>
              {/if}

              <!-- Footer con precio -->
              <div class="offer-footer">
                <div class="price-section">
                  <div class="price-original">{formatPrice(oferta.originalPrice)}</div>
                  <div class="price-current">
                    <span class="ofertas__amount">{formatPrice(oferta.offerPrice)}</span>
                    <span class="period">/ noche</span>
                  </div>
                  <div class="savings-text">Ahorra {formatPrice(oferta.originalPrice - oferta.offerPrice)}</div>
                </div>
                
                <button class="view-offer-btn">
                  Ver Oferta
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                  </svg>
                </button>
              </div>
            </div>
          </article>
        {/each}
      </div>
    </section>

    <!-- CTA Final Profesional -->
    <section class="final-cta">
      <div class="cta-card">
        <div class="ofertas__cta-content">
          <h2 class="ofertas__cta-title">¿No encontraste lo que buscabas?</h2>
          <p class="ofertas__cta-description">Explora nuestra colección completa de hoteles alrededor del mundo</p>
        </div>
        <button class="ofertas__cta-button" on:click={() => navigateTo('search-results')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          Buscar Todos los Hoteles
        </button>
      </div>
    </section>
  </div>
</div>