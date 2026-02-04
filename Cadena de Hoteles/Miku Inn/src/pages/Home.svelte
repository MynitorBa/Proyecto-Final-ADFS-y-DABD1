<script>
  import { onMount } from 'svelte';
  import { navigate } from 'svelte-routing';
  
  let destination = '';
  let checkIn = '';
  let checkOut = '';
  let guests = 1;
  
  onMount(() => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    checkIn = today.toISOString().split('T')[0];
    checkOut = tomorrow.toISOString().split('T')[0];
  });
  
  function handleSearch(e) {
    e.preventDefault();
    if (destination.trim()) {
      console.log('Buscando hoteles...', { destination, checkIn, checkOut, guests });
      // navigate('/search');
    }
  }
  
  function updateCheckOut() {
    if (checkIn) {
      const checkInDate = new Date(checkIn);
      const minCheckOut = new Date(checkInDate);
      minCheckOut.setDate(minCheckOut.getDate() + 1);
      
      if (!checkOut || new Date(checkOut) <= checkInDate) {
        checkOut = minCheckOut.toISOString().split('T')[0];
      }
    }
  }
  
  const features = [
    {
      icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
        <polyline points="9 22 9 12 15 12 15 22"></polyline>
      </svg>`,
      title: 'Hoteles de Lujo',
      description: 'Experimenta comodidad y elegancia en cada una de nuestras propiedades premium'
    },
    {
      icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="2" y1="12" x2="22" y2="12"></line>
        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
      </svg>`,
      title: 'Ubicaciones Premium',
      description: 'Presencia en los destinos más exclusivos y demandados del mundo'
    },
    {
      icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
        <circle cx="9" cy="7" r="4"></circle>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
      </svg>`,
      title: 'Servicio Excepcional',
      description: 'Atención personalizada las 24 horas con personal altamente capacitado'
    },
    {
      icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <line x1="12" y1="1" x2="12" y2="23"></line>
        <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
      </svg>`,
      title: 'Mejor Precio Garantizado',
      description: 'Las mejores tarifas directamente con nosotros, sin intermediarios'
    }
  ];
  
  const destinations = [
    {
      name: 'París',
      country: 'Francia',
      hotels: 5,
      image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&q=80'
    },
    {
      name: 'Londres',
      country: 'Reino Unido',
      hotels: 4,
      image: 'https://images.unsplash.com/photo-1513581166391-887a96ddeafd?w=800&q=80'
    },
    {
      name: 'Tokio',
      country: 'Japón',
      hotels: 6,
      image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800&q=80'
    },
    {
      name: 'Nueva York',
      country: 'Estados Unidos',
      hotels: 7,
      image: 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=800&q=80'
    }
  ];
  
  const testimonials = [
    {
      name: 'María González',
      location: 'Madrid, España',
      rating: 5,
      text: 'Una experiencia inolvidable. El servicio superó todas mis expectativas. Las instalaciones son impecables y el personal extremadamente atento.',
      avatar: 'MG'
    },
    {
      name: 'Carlos Méndez',
      location: 'Guatemala',
      rating: 5,
      text: 'Instalaciones de primer nivel y un servicio que realmente marca la diferencia. Sin duda volveré en mi próximo viaje.',
      avatar: 'CM'
    },
    {
      name: 'Ana Rodríguez',
      location: 'Buenos Aires, Argentina',
      rating: 5,
      text: 'Cada detalle está pensado para la comodidad del huésped. Ubicación perfecta y amenidades de lujo. Altamente recomendado.',
      avatar: 'AR'
    }
  ];
</script>

<div class="home-page">
  <!-- Hero Section -->
  <section class="hero-section">
    <div class="hero-overlay"></div>
    <div class="hero-content">
      <div class="hero-text">
        <h1 class="hero-title">Bienvenido a Miku Inn</h1>
        <p class="hero-subtitle">Descubre experiencias únicas en nuestros hoteles alrededor del mundo</p>
      </div>
      
      <!-- Search Card -->
      <div class="search-card">
        <h2 class="search-title">Encuentra tu hotel ideal</h2>
        
        <form class="search-form" on:submit={handleSearch}>
          <div class="form-grid">
            <div class="form-group">
              <label for="destination" class="form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                Destino
              </label>
              <input
                type="text"
                id="destination"
                class="form-input"
                bind:value={destination}
                placeholder="¿A dónde viajas?"
                required
              />
            </div>
            
            <div class="form-group">
              <label for="checkIn" class="form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                Check-in
              </label>
              <input
                type="date"
                id="checkIn"
                class="form-input"
                bind:value={checkIn}
                on:change={updateCheckOut}
                min={new Date().toISOString().split('T')[0]}
                required
              />
            </div>
            
            <div class="form-group">
              <label for="checkOut" class="form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                Check-out
              </label>
              <input
                type="date"
                id="checkOut"
                class="form-input"
                bind:value={checkOut}
                min={checkIn}
                required
              />
            </div>
            
            <div class="form-group">
              <label for="guests" class="form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                Huéspedes
              </label>
              <select id="guests" class="form-input" bind:value={guests}>
                {#each Array(10) as _, i}
                  <option value={i + 1}>{i + 1} {i === 0 ? 'Huésped' : 'Huéspedes'}</option>
                {/each}
              </select>
            </div>
          </div>
          
          <button type="submit" class="search-button">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
            Buscar Hoteles
          </button>
        </form>
      </div>
    </div>
  </section>
  
  <!-- Features Section -->
  <section class="features-section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">¿Por qué elegir Miku Inn?</h2>
        <p class="section-description">Experiencias excepcionales que superan expectativas</p>
      </div>
      
      <div class="features-grid">
        {#each features as feature}
          <div class="feature-card">
            <div class="feature-icon">
              {@html feature.icon}
            </div>
            <h3 class="feature-title">{feature.title}</h3>
            <p class="feature-description">{feature.description}</p>
          </div>
        {/each}
      </div>
    </div>
  </section>
  
  <!-- Destinations Section -->
  <section class="destinations-section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Destinos Populares</h2>
        <p class="section-description">Explora nuestras ubicaciones más demandadas</p>
      </div>
      
      <div class="destinations-grid">
        {#each destinations as destination}
          <a href="#/search?dest={destination.name}" class="destination-card">
            <img src={destination.image} alt={destination.name} class="destination-image" />
            <div class="destination-overlay">
              <div class="destination-content">
                <h3 class="destination-name">{destination.name}</h3>
                <p class="destination-country">{destination.country}</p>
                <div class="destination-hotels">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                  </svg>
                  {destination.hotels} hoteles
                </div>
              </div>
            </div>
          </a>
        {/each}
      </div>
    </div>
  </section>
  
  <!-- Testimonials Section -->
  <section class="testimonials-section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Lo que dicen nuestros huéspedes</h2>
        <p class="section-description">Experiencias reales de clientes satisfechos</p>
      </div>
      
      <div class="testimonials-grid">
        {#each testimonials as testimonial}
          <div class="testimonial-card">
            <div class="testimonial-header">
              <div class="testimonial-avatar">
                {testimonial.avatar}
              </div>
              <div class="testimonial-info">
                <h4 class="testimonial-name">{testimonial.name}</h4>
                <p class="testimonial-location">{testimonial.location}</p>
              </div>
            </div>
            
            <div class="testimonial-rating">
              {#each Array(testimonial.rating) as _}
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
              {/each}
            </div>
            
            <p class="testimonial-text">{testimonial.text}</p>
          </div>
        {/each}
      </div>
    </div>
  </section>
  
  <!-- CTA Section -->
  <section class="cta-section">
    <div class="container">
      <div class="cta-content">
        <h2 class="cta-title">¿Listo para tu próxima aventura?</h2>
        <p class="cta-description">Únete a miles de viajeros que confían en Miku Inn para sus experiencias de lujo</p>
        <div class="cta-buttons">
          <a href="#/search" class="cta-button primary">
            Explorar Hoteles
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <polyline points="12 5 19 12 12 19"></polyline>
            </svg>
          </a>
          <a href="#/offers" class="cta-button secondary">
            Ver Ofertas Especiales
          </a>
        </div>
      </div>
    </div>
  </section>
</div>

<style>
  /* ========== PÁGINA HOME ========== */
  .home-page {
    width: 100%;
    min-height: 100vh;
  }
  
  /* ========== HERO SECTION ========== */
  .hero-section {
    position: relative;
    min-height: 85vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-image: url('https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1600&q=80');
    background-size: cover;
    background-position: center;
    background-attachment: fixed;
  }
  
  .hero-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      135deg,
      rgba(15, 23, 42, 0.85) 0%,
      rgba(102, 126, 234, 0.75) 50%,
      rgba(118, 75, 162, 0.85) 100%
    );
  }
  
  .hero-content {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 1100px;
    padding: 2rem;
    display: flex;
    flex-direction: column;
    gap: 3rem;
  }
  
  .hero-text {
    text-align: center;
  }
  
  .hero-title {
    font-size: clamp(2.5rem, 5vw, 4rem);
    font-weight: 800;
    color: white;
    margin-bottom: 1rem;
    line-height: 1.1;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    letter-spacing: -0.02em;
  }
  
  .hero-subtitle {
    font-size: clamp(1.1rem, 2vw, 1.4rem);
    color: rgba(255, 255, 255, 0.95);
    font-weight: 400;
    max-width: 600px;
    margin: 0 auto;
    line-height: 1.6;
  }
  
  /* ========== SEARCH CARD ========== */
  .search-card {
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 2.5rem;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .search-title {
    color: #1e293b;
    font-size: 1.75rem;
    font-weight: 700;
    margin-bottom: 2rem;
    text-align: center;
  }
  
  .search-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .form-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 1.25rem;
  }
  
  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 600;
    color: #475569;
    font-size: 0.9rem;
  }
  
  .label-icon {
    color: #667eea;
  }
  
  .form-input {
    padding: 0.875rem 1rem;
    border: 2px solid #e2e8f0;
    border-radius: 10px;
    font-size: 1rem;
    font-family: inherit;
    background: white;
    color: #1e293b;
    transition: all 0.2s ease;
  }
  
  .form-input:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  }
  
  .search-button {
    padding: 1.125rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 1.1rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  }
  
  .search-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 30px rgba(102, 126, 234, 0.5);
  }
  
  /* ========== CONTAINER ========== */
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 2rem;
  }
  
  /* ========== SECTION HEADERS ========== */
  .section-header {
    text-align: center;
    margin-bottom: 4rem;
  }
  
  .section-title {
    font-size: clamp(2rem, 4vw, 2.5rem);
    font-weight: 800;
    color: #0f172a;
    margin-bottom: 0.75rem;
  }
  
  .section-description {
    font-size: 1.125rem;
    color: #64748b;
    max-width: 600px;
    margin: 0 auto;
  }
  
  /* ========== FEATURES SECTION ========== */
  .features-section {
    padding: 6rem 0;
    background: white;
  }
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 2.5rem;
  }
  
  .feature-card {
    text-align: center;
    padding: 2.5rem 2rem;
    border-radius: 16px;
    background: white;
    border: 1px solid #e2e8f0;
    transition: all 0.3s ease;
  }
  
  .feature-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(102, 126, 234, 0.15);
    border-color: #667eea;
  }
  
  .feature-icon {
    color: #667eea;
    margin-bottom: 1.5rem;
    display: inline-block;
  }
  
  .feature-title {
    font-size: 1.375rem;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 0.875rem;
  }
  
  .feature-description {
    color: #64748b;
    line-height: 1.6;
    font-size: 1rem;
  }
  
  /* ========== DESTINATIONS SECTION ========== */
  .destinations-section {
    padding: 6rem 0;
    background: #f8fafc;
  }
  
  .destinations-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
  }
  
  .destination-card {
    position: relative;
    height: 400px;
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.3s ease;
    text-decoration: none;
  }
  
  .destination-card:hover {
    transform: scale(1.03);
  }
  
  .destination-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .destination-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to top,
      rgba(15, 23, 42, 0.9) 0%,
      rgba(15, 23, 42, 0.4) 50%,
      transparent 100%
    );
    display: flex;
    align-items: flex-end;
    padding: 2rem;
    transition: all 0.3s ease;
  }
  
  .destination-card:hover .destination-overlay {
    background: linear-gradient(
      to top,
      rgba(102, 126, 234, 0.9) 0%,
      rgba(102, 126, 234, 0.4) 50%,
      transparent 100%
    );
  }
  
  .destination-content {
    color: white;
  }
  
  .destination-name {
    font-size: 2rem;
    font-weight: 800;
    margin-bottom: 0.25rem;
  }
  
  .destination-country {
    font-size: 1.125rem;
    margin-bottom: 0.75rem;
    opacity: 0.9;
  }
  
  .destination-hotels {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.95rem;
    font-weight: 600;
  }
  
  /* ========== TESTIMONIALS SECTION ========== */
  .testimonials-section {
    padding: 6rem 0;
    background: white;
  }
  
  .testimonials-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: 2rem;
  }
  
  .testimonial-card {
    background: white;
    padding: 2rem;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    transition: all 0.3s ease;
  }
  
  .testimonial-card:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
    border-color: #667eea;
  }
  
  .testimonial-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  
  .testimonial-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.25rem;
  }
  
  .testimonial-name {
    font-size: 1.125rem;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 0.25rem;
  }
  
  .testimonial-location {
    font-size: 0.9rem;
    color: #64748b;
  }
  
  .testimonial-rating {
    display: flex;
    gap: 0.25rem;
    color: #fbbf24;
    margin-bottom: 1rem;
  }
  
  .testimonial-text {
    color: #475569;
    line-height: 1.7;
    font-size: 1rem;
  }
  
  /* ========== CTA SECTION ========== */
  .cta-section {
    padding: 6rem 0;
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
    position: relative;
    overflow: hidden;
  }
  
  .cta-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1600&q=80');
    background-size: cover;
    background-position: center;
    opacity: 0.1;
  }
  
  .cta-content {
    position: relative;
    text-align: center;
    max-width: 800px;
    margin: 0 auto;
  }
  
  .cta-title {
    font-size: clamp(2rem, 4vw, 3rem);
    font-weight: 800;
    color: white;
    margin-bottom: 1rem;
  }
  
  .cta-description {
    font-size: 1.25rem;
    color: rgba(255, 255, 255, 0.85);
    margin-bottom: 2.5rem;
    line-height: 1.6;
  }
  
  .cta-buttons {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .cta-button {
    padding: 1.125rem 2.5rem;
    border-radius: 12px;
    font-weight: 700;
    font-size: 1.05rem;
    text-decoration: none;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .cta-button.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  }
  
  .cta-button.primary:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 30px rgba(102, 126, 234, 0.5);
  }
  
  .cta-button.secondary {
    background: transparent;
    color: white;
    border: 2px solid rgba(255, 255, 255, 0.3);
  }
  
  .cta-button.secondary:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.5);
  }
  
  /* ========== RESPONSIVE ========== */
  @media (max-width: 768px) {
    .hero-section {
      min-height: 100vh;
      background-attachment: scroll;
    }
    
    .hero-content {
      gap: 2rem;
    }
    
    .search-card {
      padding: 1.75rem;
    }
    
    .form-grid {
      grid-template-columns: 1fr;
    }
    
    .section-header {
      margin-bottom: 2.5rem;
    }
    
    .features-section,
    .destinations-section,
    .testimonials-section,
    .cta-section {
      padding: 4rem 0;
    }
    
    .cta-buttons {
      flex-direction: column;
    }
    
    .cta-button {
      width: 100%;
      justify-content: center;
    }
  }
  
  /* ========== DARK MODE ========== */
  @media (prefers-color-scheme: dark) {
    .search-card {
      background: rgba(30, 41, 59, 0.95);
    }
    
    .search-title {
      color: white;
    }
    
    .form-label {
      color: #cbd5e1;
    }
    
    .form-input {
      background: #1e293b;
      border-color: #334155;
      color: white;
    }
    
    .features-section {
      background: #0f172a;
    }
    
    .section-title {
      color: white;
    }
    
    .feature-card {
      background: #1e293b;
      border-color: #334155;
    }
    
    .feature-title {
      color: white;
    }
    
    .testimonials-section {
      background: #0f172a;
    }
    
    .testimonial-card {
      background: #1e293b;
      border-color: #334155;
    }
    
    .testimonial-name {
      color: white;
    }
  }
</style>