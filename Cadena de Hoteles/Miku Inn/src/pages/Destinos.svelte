<script>
  export let navigateTo;
  import '../styles/destinos.css';

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
      hasRecommendedFlight: true
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
        <button
          class="destino-card"
          class:featured={destino.featured}
          on:click={() => navigateTo('destino-detail', destino.id)}
          aria-label="Explorar {destino.name}, {destino.country}"
        >
          <!-- Image -->
          <div class="destino-image">
            <img src={destino.image} alt={destino.name} />
            <div class="destino-overlay" aria-hidden="true">
              <span class="view-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
                Explorar
              </span>
            </div>

            {#if destino.featured}
              <div class="featured-badge" aria-hidden="true">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
                Popular
              </div>
            {/if}

            {#if destino.hasRecommendedFlight}
              <div class="flight-badge" aria-hidden="true">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
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
                <span class="flag" aria-hidden="true">{destino.flag}</span>
                <div>
                  <h3 class="destino-name">{destino.name}</h3>
                  <p class="destino-country">{destino.country}</p>
                </div>
              </div>
            </div>

            <p class="destino-description">{destino.description}</p>

            <div class="destino-info">
              <div class="destinos__info-item">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                  <polyline points="9 22 9 12 15 12 15 22"></polyline>
                </svg>
                <span>{destino.hotelsCount} hoteles</span>
              </div>
              <div class="destinos__info-item price">
                <span class="from">Desde</span>
                <span class="amount">${destino.startingPrice}</span>
                <span class="per-night">/noche</span>
              </div>
            </div>
          </div>
        </button>
      {/each}
    </div>
  </div>
</div>