<script>
  import { onMount } from 'svelte';
  import { navigate } from 'svelte-routing';
  import '../styles/home.css';
  
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
  <section class="home__hero-section">
    <div class="home__hero-overlay"></div>
    <div class="home__hero-content">
      <div class="hero-text">
        <h1 class="home__hero-title">Bienvenido a Miku Inn</h1>
        <p class="hero-subtitle">Descubre experiencias únicas en nuestros hoteles alrededor del mundo</p>
      </div>
      
      <!-- Search Card -->
      <div class="search-card">
        <h2 class="home__search-title">Encuentra tu hotel ideal</h2>
        
        <form class="search-form" on:submit={handleSearch}>
          <div class="home__form-grid">
            <div class="home__form-group">
              <label for="destination" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                Destino
              </label>
              <input
                type="text"
                id="destination"
                class="home__form-input"
                bind:value={destination}
                placeholder="¿A dónde viajas?"
                required
              />
            </div>
            
            <div class="home__form-group">
              <label for="checkIn" class="home__form-label">
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
                class="home__form-input"
                bind:value={checkIn}
                on:change={updateCheckOut}
                min={new Date().toISOString().split('T')[0]}
                required
              />
            </div>
            
            <div class="home__form-group">
              <label for="checkOut" class="home__form-label">
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
                class="home__form-input"
                bind:value={checkOut}
                min={checkIn}
                required
              />
            </div>
            
            <div class="home__form-group">
              <label for="guests" class="home__form-label">
                <svg class="label-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                Huéspedes
              </label>
              <select id="guests" class="home__form-input" bind:value={guests}>
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
    <div class="home__container">
      <div class="home__section-header">
        <h2 class="home__section-title">¿Por qué elegir Miku Inn?</h2>
        <p class="home__section-description">Experiencias excepcionales que superan expectativas</p>
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
    <div class="home__container">
      <div class="home__section-header">
        <h2 class="home__section-title">Destinos Populares</h2>
        <p class="home__section-description">Explora nuestras ubicaciones más demandadas</p>
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
    <div class="home__container">
      <div class="home__section-header">
        <h2 class="home__section-title">Lo que dicen nuestros huéspedes</h2>
        <p class="home__section-description">Experiencias reales de clientes satisfechos</p>
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
  <section class="home__cta-section">
    <div class="home__container">
      <div class="home__cta-content">
        <h2 class="home__cta-title">¿Listo para tu próxima aventura?</h2>
        <p class="home__cta-description">Únete a miles de viajeros que confían en Miku Inn para sus experiencias de lujo</p>
        <div class="cta-buttons">
          <a href="#/search" class="home__cta-button primary">
            Explorar Hoteles
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="5" y1="12" x2="19" y2="12"></line>
              <polyline points="12 5 19 12 12 19"></polyline>
            </svg>
          </a>
          <a href="#/offers" class="home__cta-button secondary">
            Ver Ofertas Especiales
          </a>
        </div>
      </div>
    </div>
  </section>
</div>