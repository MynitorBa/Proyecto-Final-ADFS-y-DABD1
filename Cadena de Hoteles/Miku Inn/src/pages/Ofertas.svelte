<script>
  export let navigateTo;
  
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
  <section class="hero-section">
    <div class="hero-background"></div>
    <div class="hero-container">
      <div class="hero-content">
        <span class="hero-label">Descuentos Exclusivos</span>
        <h1 class="hero-title">Ofertas Especiales</h1>
        <p class="hero-description">
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
            class="filter-btn"
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
      <div class="section-header">
        <h2 class="section-title">
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
                <div class="discount-badge">
                  <span class="discount-value">-{oferta.discount}%</span>
                </div>
                {#if oferta.featured}
                  <div class="featured-badge">
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
                  <h3 class="hotel-name">{oferta.name}</h3>
                  <div class="hotel-location">
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
                    <span class="amount">{formatPrice(oferta.offerPrice)}</span>
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
        <div class="cta-content">
          <h2 class="cta-title">¿No encontraste lo que buscabas?</h2>
          <p class="cta-description">Explora nuestra colección completa de hoteles alrededor del mundo</p>
        </div>
        <button class="cta-button" on:click={() => navigateTo('search-results')}>
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

<style>
  /* ============================================
     DISEÑO PROFESIONAL - OFERTAS ESPECIALES
     ============================================ */
  
  /* Variables Globales */
  :root {
    --primary: #667eea;
    --primary-dark: #5568d3;
    --secondary: #764ba2;
    --success: #10b981;
    --warning: #f59e0b;
    --danger: #ef4444;
    --info: #3b82f6;
    
    --gray-50: #f8fafc;
    --gray-100: #f1f5f9;
    --gray-200: #e2e8f0;
    --gray-300: #cbd5e1;
    --gray-400: #94a3b8;
    --gray-500: #64748b;
    --gray-600: #475569;
    --gray-700: #334155;
    --gray-800: #1e293b;
    --gray-900: #0f172a;
    
    --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    --shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
    --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
    --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
    --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
    
    --radius-sm: 6px;
    --radius: 8px;
    --radius-md: 12px;
    --radius-lg: 16px;
    --radius-xl: 20px;
    
    --transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  /* Base Page */
  .ofertas-page {
    min-height: 100vh;
    background: var(--gray-50);
  }
  
  /* ====================
     HERO SECTION
     ==================== */
  .hero-section {
    position: relative;
    padding: 5rem 2rem 4rem;
    background: linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%);
    overflow: hidden;
  }
  
  .hero-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
    opacity: 0.5;
  }
  
  .hero-container {
    position: relative;
    max-width: 1200px;
    margin: 0 auto;
    z-index: 1;
  }
  
  .hero-content {
    text-align: center;
    color: white;
    margin-bottom: 3rem;
  }
  
  .hero-label {
    display: inline-block;
    padding: 0.5rem 1.25rem;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border-radius: 50px;
    font-size: 0.875rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    text-transform: uppercase;
    margin-bottom: 1.5rem;
  }
  
  .hero-title {
    font-size: clamp(2.5rem, 5vw, 3.5rem);
    font-weight: 700;
    margin: 0 0 1rem;
    letter-spacing: -0.02em;
  }
  
  .hero-description {
    font-size: clamp(1rem, 2vw, 1.125rem);
    opacity: 0.95;
    max-width: 600px;
    margin: 0 auto;
    line-height: 1.6;
  }
  
  .hero-stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
    max-width: 800px;
    margin: 0 auto;
  }
  
  .stat-card {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-lg);
    padding: 1.5rem;
    text-align: center;
    color: white;
    transition: var(--transition);
  }
  
  .stat-card:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    margin: 0 auto 1rem;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-md);
  }
  
  .stat-value {
    font-size: 2.5rem;
    font-weight: 700;
    line-height: 1;
    margin-bottom: 0.5rem;
  }
  
  .stat-label {
    font-size: 0.875rem;
    opacity: 0.9;
    font-weight: 500;
  }
  
  /* ====================
     MAIN CONTAINER
     ==================== */
  .main-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 3rem 2rem;
  }
  
  /* ====================
     FILTERS SECTION
     ==================== */
  .filters-section {
    margin-bottom: 3rem;
  }
  
  .filters-wrapper {
    display: flex;
    gap: 0.75rem;
    overflow-x: auto;
    padding-bottom: 0.5rem;
    scrollbar-width: thin;
    scrollbar-color: var(--gray-300) transparent;
  }
  
  .filters-wrapper::-webkit-scrollbar {
    height: 6px;
  }
  
  .filters-wrapper::-webkit-scrollbar-track {
    background: transparent;
  }
  
  .filters-wrapper::-webkit-scrollbar-thumb {
    background: var(--gray-300);
    border-radius: 3px;
  }
  
  .filter-btn {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    padding: 0.75rem 1.25rem;
    background: white;
    border: 2px solid var(--gray-200);
    border-radius: var(--radius-md);
    color: var(--gray-700);
    font-size: 0.9375rem;
    font-weight: 600;
    cursor: pointer;
    white-space: nowrap;
    transition: var(--transition);
    flex-shrink: 0;
  }
  
  .filter-btn:hover {
    border-color: var(--primary);
    background: var(--gray-50);
    transform: translateY(-1px);
  }
  
  .filter-btn.active {
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    border-color: var(--primary);
    color: white;
    box-shadow: var(--shadow-md);
  }
  
  .filter-icon {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .filter-text {
    font-weight: 600;
  }
  
  .filter-count {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 24px;
    height: 24px;
    padding: 0 0.5rem;
    background: var(--gray-100);
    color: var(--gray-700);
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 700;
  }
  
  .filter-btn.active .filter-count {
    background: rgba(255, 255, 255, 0.25);
    color: white;
  }
  
  /* ====================
     OFFERS SECTION
     ==================== */
  .offers-section {
    margin-bottom: 4rem;
  }
  
  .section-header {
    text-align: center;
    margin-bottom: 3rem;
  }
  
  .section-title {
    font-size: 2rem;
    font-weight: 700;
    color: var(--gray-900);
    margin: 0 0 0.5rem;
  }
  
  .section-subtitle {
    font-size: 1rem;
    color: var(--gray-500);
    margin: 0;
  }
  
  .offers-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 2rem;
  }
  
  /* ====================
     OFFER CARDS
     ==================== */
  .offer-card {
    background: white;
    border-radius: var(--radius-xl);
    overflow: hidden;
    box-shadow: var(--shadow);
    transition: var(--transition);
    cursor: pointer;
    border: 1px solid var(--gray-200);
  }
  
  .offer-card:hover {
    box-shadow: var(--shadow-xl);
    transform: translateY(-4px);
    border-color: var(--primary);
  }
  
  .offer-image-wrapper {
    position: relative;
    height: 240px;
    overflow: hidden;
  }
  
  .offer-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  .offer-card:hover .offer-image {
    transform: scale(1.08);
  }
  
  .badges-container {
    position: absolute;
    top: 1rem;
    left: 1rem;
    right: 1rem;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }
  
  .discount-badge {
    padding: 0.625rem 1rem;
    background: var(--danger);
    color: white;
    border-radius: var(--radius);
    font-weight: 700;
    font-size: 1.125rem;
    box-shadow: var(--shadow-lg);
  }
  
  .discount-value {
    display: block;
    line-height: 1;
  }
  
  .featured-badge {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    padding: 0.5rem 0.875rem;
    background: var(--warning);
    color: white;
    border-radius: var(--radius);
    font-size: 0.8125rem;
    font-weight: 600;
    box-shadow: var(--shadow-lg);
  }
  
  .featured-badge svg {
    width: 14px;
    height: 14px;
  }
  
  .offer-body {
    padding: 1.5rem;
  }
  
  .offer-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  
  .hotel-info {
    flex: 1;
    min-width: 0;
  }
  
  .hotel-name {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--gray-900);
    margin: 0 0 0.375rem;
    line-height: 1.3;
  }
  
  .hotel-location {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    color: var(--gray-600);
    font-size: 0.9375rem;
  }
  
  .hotel-location svg {
    flex-shrink: 0;
  }
  
  .rating-badge {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.5rem 0.75rem;
    background: var(--warning);
    color: white;
    border-radius: var(--radius);
    font-weight: 700;
    font-size: 0.9375rem;
    flex-shrink: 0;
  }
  
  .hotel-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--gray-200);
  }
  
  .stars {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }
  
  .star-icon {
    width: 14px;
    height: 14px;
    fill: var(--warning);
  }
  
  .reviews-count {
    margin-left: 0.25rem;
    color: var(--gray-500);
    font-size: 0.875rem;
  }
  
  .category-tag {
    padding: 0.375rem 0.75rem;
    background: var(--gray-100);
    color: var(--gray-700);
    border-radius: var(--radius);
    font-size: 0.8125rem;
    font-weight: 600;
  }
  
  .perks-list {
    display: flex;
    flex-direction: column;
    gap: 0.625rem;
    margin-bottom: 1rem;
  }
  
  .perk-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--gray-600);
    font-size: 0.875rem;
  }
  
  .perk-item svg {
    flex-shrink: 0;
    color: var(--success);
  }
  
  .countdown-banner {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    background: rgba(239, 68, 68, 0.08);
    border-left: 3px solid var(--danger);
    border-radius: var(--radius);
    color: var(--danger);
    font-size: 0.875rem;
    margin-bottom: 1rem;
  }
  
  .countdown-banner svg {
    flex-shrink: 0;
  }
  
  .countdown-banner strong {
    font-weight: 700;
  }
  
  .offer-footer {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--gray-200);
  }
  
  .price-section {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .price-original {
    color: var(--gray-400);
    font-size: 0.875rem;
    text-decoration: line-through;
  }
  
  .price-current {
    display: flex;
    align-items: baseline;
    gap: 0.375rem;
  }
  
  .price-current .amount {
    font-size: 1.875rem;
    font-weight: 700;
    color: var(--gray-900);
    line-height: 1;
  }
  
  .price-current .period {
    font-size: 0.875rem;
    color: var(--gray-500);
  }
  
  .savings-text {
    color: var(--success);
    font-size: 0.875rem;
    font-weight: 600;
  }
  
  .view-offer-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.875rem 1.5rem;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
    border: none;
    border-radius: var(--radius-md);
    font-weight: 700;
    font-size: 0.9375rem;
    cursor: pointer;
    transition: var(--transition);
    box-shadow: var(--shadow-md);
    white-space: nowrap;
  }
  
  .view-offer-btn:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }
  
  .view-offer-btn svg {
    width: 18px;
    height: 18px;
  }
  
  /* ====================
     FINAL CTA
     ==================== */
  .final-cta {
    margin-top: 4rem;
  }
  
  .cta-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 2rem;
    padding: 3rem;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    border-radius: var(--radius-xl);
    color: white;
  }
  
  .cta-content {
    flex: 1;
  }
  
  .cta-title {
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 0.5rem;
  }
  
  .cta-description {
    font-size: 1.125rem;
    margin: 0;
    opacity: 0.95;
  }
  
  .cta-button {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    padding: 1.125rem 2rem;
    background: white;
    color: var(--primary);
    border: none;
    border-radius: var(--radius-md);
    font-size: 1.0625rem;
    font-weight: 700;
    cursor: pointer;
    transition: var(--transition);
    box-shadow: var(--shadow-lg);
    white-space: nowrap;
  }
  
  .cta-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 20px 30px -10px rgba(0, 0, 0, 0.3);
  }
  
  /* ====================
     RESPONSIVE
     ==================== */
  @media (max-width: 1024px) {
    .offers-grid {
      grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
    }
  }
  
  @media (max-width: 768px) {
    .hero-section {
      padding: 3rem 1.5rem 2.5rem;
    }
    
    .hero-title {
      font-size: 2rem;
    }
    
    .hero-description {
      font-size: 1rem;
    }
    
    .hero-stats-grid {
      grid-template-columns: 1fr;
      gap: 1rem;
    }
    
    .main-container {
      padding: 2rem 1rem;
    }
    
    .offers-grid {
      grid-template-columns: 1fr;
      gap: 1.5rem;
    }
    
    .section-title {
      font-size: 1.5rem;
    }
    
    .cta-card {
      flex-direction: column;
      text-align: center;
      padding: 2rem 1.5rem;
    }
    
    .cta-title {
      font-size: 1.5rem;
    }
    
    .cta-description {
      font-size: 1rem;
    }
    
    .cta-button {
      width: 100%;
      justify-content: center;
    }
  }
  
  @media (max-width: 480px) {
    .offer-footer {
      flex-direction: column;
      align-items: stretch;
    }
    
    .view-offer-btn {
      width: 100%;
      justify-content: center;
    }
  }
</style>