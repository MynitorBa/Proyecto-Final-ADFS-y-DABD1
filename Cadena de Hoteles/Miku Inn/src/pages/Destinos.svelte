<script>
  export let navigateTo;
  
  // Destinos populares
  const destinos = [
    {
      id: 'paris',
      name: 'París',
      country: 'Francia',
      flag: '🇫🇷',
      image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800',
      description: 'La Ciudad de la Luz te espera con sus icónicos monumentos y encanto único',
      hotelsCount: 1250,
      startingPrice: 89,
      featured: true,
      hasRecommendedFlight: true // Solo París tiene vuelo recomendado
    },
    {
      id: 'tokyo',
      name: 'Tokio',
      country: 'Japón',
      flag: '🇯🇵',
      image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800',
      description: 'Experimenta la perfecta mezcla de tradición y modernidad',
      hotelsCount: 980,
      startingPrice: 95,
      featured: true,
      hasRecommendedFlight: false
    },
    {
      id: 'new-york',
      name: 'Nueva York',
      country: 'Estados Unidos',
      flag: '🇺🇸',
      image: 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=800',
      description: 'La ciudad que nunca duerme te sorprenderá en cada esquina',
      hotelsCount: 1450,
      startingPrice: 120,
      featured: true,
      hasRecommendedFlight: false
    },
    {
      id: 'london',
      name: 'Londres',
      country: 'Reino Unido',
      flag: '🇬🇧',
      image: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800',
      description: 'Historia, cultura y modernidad en la capital británica',
      hotelsCount: 1100,
      startingPrice: 105,
      featured: false,
      hasRecommendedFlight: false
    },
    {
      id: 'barcelona',
      name: 'Barcelona',
      country: 'España',
      flag: '🇪🇸',
      image: 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800',
      description: 'Arte, arquitectura y playas mediterráneas te esperan',
      hotelsCount: 890,
      startingPrice: 78,
      featured: false,
      hasRecommendedFlight: false
    },
    {
      id: 'dubai',
      name: 'Dubái',
      country: 'Emiratos Árabes',
      flag: '🇦🇪',
      image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800',
      description: 'Lujo, arquitectura futurista y experiencias inolvidables',
      hotelsCount: 720,
      startingPrice: 150,
      featured: false,
      hasRecommendedFlight: false
    }
  ];
</script>

<div class="destinos-page">
  <!-- Hero Section -->
  <div class="hero-section">
    <div class="hero-overlay"></div>
    <div class="hero-content">
      <h1 class="hero-title">Descubre tu Próximo Destino</h1>
      <p class="hero-subtitle">Explora los mejores hoteles en los destinos más populares del mundo</p>
      
      <div class="hero-stats">
        <div class="stat">
          <div class="stat-number">1,250+</div>
          <div class="stat-label">Hoteles</div>
        </div>
        <div class="stat">
          <div class="stat-number">50+</div>
          <div class="stat-label">Destinos</div>
        </div>
        <div class="stat">
          <div class="stat-number">4.8</div>
          <div class="stat-label">Rating Promedio</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Destinos Grid -->
  <div class="container">
    <div class="section-header">
      <h2 class="section-title">Destinos Populares</h2>
      <p class="section-subtitle">Descubre los lugares más solicitados por nuestros viajeros</p>
    </div>

    <div class="destinos-grid">
      {#each destinos as destino}
        <div 
          class="destino-card" 
          class:featured={destino.featured}
          on:click={() => navigateTo('destino-detail', destino.id)}
        >
          <!-- Image -->
          <div class="destino-image">
            <img src={destino.image} alt={destino.name} />
            <div class="destino-overlay">
              <button class="view-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
                Explorar
              </button>
            </div>
            
            {#if destino.featured}
              <div class="featured-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
                Popular
              </div>
            {/if}
            
            {#if destino.hasRecommendedFlight}
              <div class="flight-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                  <polyline points="7.5 4.21 12 6.81 16.5 4.21"></polyline>
                  <polyline points="7.5 19.79 7.5 14.6 3 12"></polyline>
                  <polyline points="21 12 16.5 14.6 16.5 19.79"></polyline>
                  <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                  <line x1="12" y1="22.08" x2="12" y2="12"></line>
                </svg>
                Vuelo Recomendado
              </div>
            {/if}
          </div>

          <!-- Content -->
          <div class="destino-content">
            <div class="destino-header">
              <div class="destino-name-wrapper">
                <span class="flag">{destino.flag}</span>
                <div>
                  <h3 class="destino-name">{destino.name}</h3>
                  <p class="destino-country">{destino.country}</p>
                </div>
              </div>
            </div>

            <p class="destino-description">{destino.description}</p>

            <div class="destino-info">
              <div class="info-item">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                  <polyline points="9 22 9 12 15 12 15 22"></polyline>
                </svg>
                <span>{destino.hotelsCount} hoteles</span>
              </div>
              <div class="info-item price">
                <span class="from">Desde</span>
                <span class="amount">${destino.startingPrice}</span>
                <span class="per-night">/noche</span>
              </div>
            </div>
          </div>
        </div>
      {/each}
    </div>
  </div>
</div>

<style>
  /* Variables */
  :root {
    --primary: #667eea;
    --secondary: #764ba2;
    --success: #10b981;
  }

  /* Page */
  .destinos-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  }

  /* Hero Section */
  .hero-section {
    position: relative;
    height: 400px;
    background: linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }

  .hero-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=1200');
    background-size: cover;
    background-position: center;
    opacity: 0.15;
  }

  .hero-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%);
  }

  .hero-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: white;
    padding: 2rem;
  }

  .hero-title {
    font-size: 3rem;
    font-weight: 800;
    margin: 0 0 1rem 0;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  }

  .hero-subtitle {
    font-size: 1.25rem;
    margin: 0 0 3rem 0;
    opacity: 0.95;
  }

  .hero-stats {
    display: flex;
    gap: 4rem;
    justify-content: center;
  }

  .stat {
    text-align: center;
  }

  .stat-number {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
  }

  .stat-label {
    font-size: 0.9rem;
    opacity: 0.9;
  }

  /* Container */
  .container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 4rem 2rem;
  }

  /* Section Header */
  .section-header {
    text-align: center;
    margin-bottom: 3rem;
  }

  .section-title {
    font-size: 2.5rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 1rem 0;
  }

  .section-subtitle {
    font-size: 1.1rem;
    color: #64748b;
    margin: 0;
  }

  /* Destinos Grid */
  .destinos-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 2rem;
  }

  /* Destino Card */
  .destino-card {
    background: white;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    cursor: pointer;
  }

  .destino-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  }

  .destino-card.featured {
    border: 2px solid var(--primary);
  }

  /* Image */
  .destino-image {
    position: relative;
    height: 280px;
    overflow: hidden;
  }

  .destino-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  .destino-card:hover .destino-image img {
    transform: scale(1.1);
  }

  .destino-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  .destino-card:hover .destino-overlay {
    opacity: 1;
  }

  .view-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.875rem 1.5rem;
    background: white;
    color: var(--primary);
    border: none;
    border-radius: 12px;
    font-weight: 600;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .view-btn:hover {
    transform: scale(1.05);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  }

  /* Badges */
  .featured-badge,
  .flight-badge {
    position: absolute;
    top: 1rem;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    backdrop-filter: blur(10px);
  }

  .featured-badge {
    left: 1rem;
    background: rgba(255, 215, 0, 0.95);
    color: #1e293b;
  }

  .flight-badge {
    right: 1rem;
    background: rgba(16, 185, 129, 0.95);
    color: white;
  }

  /* Content */
  .destino-content {
    padding: 1.5rem;
  }

  .destino-header {
    margin-bottom: 1rem;
  }

  .destino-name-wrapper {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .flag {
    font-size: 2.5rem;
  }

  .destino-name {
    font-size: 1.5rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 0.25rem 0;
  }

  .destino-country {
    font-size: 0.9rem;
    color: #64748b;
    margin: 0;
  }

  .destino-description {
    font-size: 0.95rem;
    color: #64748b;
    line-height: 1.6;
    margin: 0 0 1.5rem 0;
  }

  /* Info */
  .destino-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 1rem;
    border-top: 1px solid #e2e8f0;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #64748b;
    font-size: 0.9rem;
  }

  .info-item svg {
    color: var(--primary);
  }

  .info-item.price {
    color: var(--primary);
    font-weight: 600;
  }

  .from {
    color: #64748b;
    font-weight: 400;
    font-size: 0.85rem;
  }

  .amount {
    font-size: 1.5rem;
    font-weight: 700;
  }

  .per-night {
    color: #64748b;
    font-weight: 400;
    font-size: 0.85rem;
  }

  /* Responsive */
  @media (max-width: 1024px) {
    .destinos-grid {
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.5rem;
    }
  }

  @media (max-width: 768px) {
    .hero-title {
      font-size: 2rem;
    }

    .hero-subtitle {
      font-size: 1rem;
    }

    .hero-stats {
      flex-direction: column;
      gap: 1.5rem;
    }

    .section-title {
      font-size: 2rem;
    }

    .destinos-grid {
      grid-template-columns: 1fr;
    }

    .container {
      padding: 2rem 1rem;
    }
  }
</style>