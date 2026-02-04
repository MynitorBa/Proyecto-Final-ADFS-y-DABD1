<script>
  export let navigateTo;
  export let searchParams = null;
  
  // Estados de UI
  let isLoading = false;
  let showMap = false;
  let viewMode = 'list'; // 'list' o 'grid'
  
  // Parámetros de búsqueda
  let search = {
    destination: searchParams?.destination || "París, Francia",
    checkIn: searchParams?.checkIn || "2026-02-15",
    checkOut: searchParams?.checkOut || "2026-02-20",
    rooms: searchParams?.rooms || 1,
    adults: searchParams?.adults || 2,
    children: searchParams?.children || 0
  };
  
  // Calcular noches
  $: nights = calculateNights(search.checkIn, search.checkOut);
  
  function calculateNights(checkIn, checkOut) {
    const date1 = new Date(checkIn);
    const date2 = new Date(checkOut);
    const diffTime = Math.abs(date2 - date1);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }
  
  // Filtros
  let filters = {
    priceMin: 0,
    priceMax: 1000,
    stars: [],
    roomTypes: [],
    amenities: [],
    propertyTypes: [],
    cancellationPolicy: 'all',
    guestRating: 0,
    sortBy: 'recommended' // 'recommended', 'price-low', 'price-high', 'rating', 'distance'
  };
  
  // Opciones de filtros
  const starOptions = [5, 4, 3, 2, 1];
  const roomTypeOptions = ['Suite Ejecutiva', 'Suite', 'Junior Suite', 'Habitación Doble', 'Habitación Simple'];
  const amenityOptions = [
    { id: 'wifi', label: 'WiFi Gratis', icon: '📶' },
    { id: 'piscina', label: 'Piscina', icon: '🏊' },
    { id: 'spa', label: 'Spa', icon: '💆' },
    { id: 'gym', label: 'Gimnasio', icon: '💪' },
    { id: 'parking', label: 'Estacionamiento', icon: '🅿️' },
    { id: 'restaurant', label: 'Restaurante', icon: '🍽️' },
    { id: 'bar', label: 'Bar', icon: '🍹' },
    { id: 'breakfast', label: 'Desayuno incluido', icon: '🍳' },
    { id: 'pets', label: 'Mascotas permitidas', icon: '🐕' },
    { id: 'aircon', label: 'Aire Acondicionado', icon: '❄️' },
    { id: 'room-service', label: 'Servicio a la habitación', icon: '🛎️' },
    { id: 'airport', label: 'Traslado aeropuerto', icon: '✈️' }
  ];
  
  const propertyTypeOptions = ['Hotel', 'Resort', 'Boutique Hotel', 'Apart Hotel', 'Hostal'];
  
  // Mock data de hoteles (simulando resultados de múltiples proveedores)
  let allHotels = [
    {
      id: 1,
      name: "Grand Miku Palace Paris",
      provider: "Miku Inn",
      providerLogo: "🏨",
      city: "París",
      address: "15 Avenue des Champs-Élysées",
      country: "Francia",
      coordinates: { lat: 48.8738, lng: 2.2950 },
      stars: 5,
      rating: 4.8,
      reviews: 1247,
      guestScore: 9.2,
      guestScoreText: "Excepcional",
      propertyType: "Hotel",
      images: [
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800",
        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800"
      ],
      roomType: "Suite Ejecutiva",
      pricePerNight: 320,
      totalPrice: 1600,
      originalPrice: 1920,
      discount: 17,
      amenities: ['wifi', 'piscina', 'spa', 'gym', 'parking', 'restaurant', 'bar', 'breakfast', 'aircon', 'room-service', 'airport'],
      amenitiesDisplay: ["WiFi Gratis", "Piscina", "Spa", "Gimnasio", "Desayuno incluido"],
      description: "Lujoso hotel 5 estrellas en el corazón de París",
      cancellationPolicy: "Cancelación gratuita hasta 24h antes",
      freeCancellation: true,
      availableRooms: 3,
      badges: ['Mejor valorado', 'Mejor precio'],
      distanceToCenter: 1.2,
      sustainableCertified: true
    },
    {
      id: 2,
      name: "Le Miku Boutique Hotel",
      provider: "Miku Inn",
      providerLogo: "🏨",
      city: "París",
      address: "28 Rue du Roi de Sicile",
      country: "Francia",
      coordinates: { lat: 48.8567, lng: 2.3622 },
      stars: 4,
      rating: 4.6,
      reviews: 856,
      guestScore: 8.9,
      guestScoreText: "Fabuloso",
      propertyType: "Boutique Hotel",
      images: [
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800"
      ],
      roomType: "Suite",
      pricePerNight: 245,
      totalPrice: 1225,
      originalPrice: null,
      discount: 0,
      amenities: ['wifi', 'restaurant', 'bar', 'breakfast', 'aircon', 'room-service'],
      amenitiesDisplay: ["WiFi Gratis", "Restaurante", "Bar", "Desayuno incluido"],
      description: "Encantador hotel boutique en el histórico barrio Le Marais",
      cancellationPolicy: "Cancelación gratuita hasta 48h antes",
      freeCancellation: true,
      availableRooms: 5,
      badges: ['Mejor ubicación'],
      distanceToCenter: 0.8,
      sustainableCertified: false
    },
    {
      id: 3,
      name: "Paris Eiffel Tower Miku Resort",
      provider: "Miku Inn",
      providerLogo: "🏨",
      city: "París",
      address: "7 Avenue de la Bourdonnais",
      country: "Francia",
      coordinates: { lat: 48.8584, lng: 2.2945 },
      stars: 5,
      rating: 4.9,
      reviews: 2134,
      guestScore: 9.5,
      guestScoreText: "Extraordinario",
      propertyType: "Resort",
      images: [
        "https://images.unsplash.com/photo-1549294413-26f195200c16?w=800",
        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800",
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"
      ],
      roomType: "Suite Ejecutiva",
      pricePerNight: 450,
      totalPrice: 2250,
      originalPrice: 2700,
      discount: 17,
      amenities: ['wifi', 'piscina', 'spa', 'gym', 'parking', 'restaurant', 'bar', 'breakfast', 'aircon', 'room-service', 'airport', 'pets'],
      amenitiesDisplay: ["WiFi Gratis", "Piscina", "Spa", "Gimnasio", "Vista Torre Eiffel"],
      description: "Resort de lujo con vistas espectaculares a la Torre Eiffel",
      cancellationPolicy: "Cancelación gratuita hasta 72h antes",
      freeCancellation: true,
      availableRooms: 2,
      badges: ['Más popular', 'Mejor valorado', 'Vista exclusiva'],
      distanceToCenter: 1.5,
      sustainableCertified: true
    },
    {
      id: 4,
      name: "Hotel Montmartre Miku Charm",
      provider: "Miku Inn",
      providerLogo: "🏨",
      city: "París",
      address: "42 Rue Lepic",
      country: "Francia",
      coordinates: { lat: 48.8853, lng: 2.3354 },
      stars: 4,
      rating: 4.5,
      reviews: 623,
      guestScore: 8.6,
      guestScoreText: "Muy bueno",
      propertyType: "Hotel",
      images: [
        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800"
      ],
      roomType: "Junior Suite",
      pricePerNight: 180,
      totalPrice: 900,
      originalPrice: null,
      discount: 0,
      amenities: ['wifi', 'restaurant', 'breakfast', 'aircon', 'room-service'],
      amenitiesDisplay: ["WiFi Gratis", "Restaurante", "Desayuno", "Aire Acondicionado"],
      description: "Hotel con encanto en el artístico barrio de Montmartre",
      cancellationPolicy: "No reembolsable",
      freeCancellation: false,
      availableRooms: 8,
      badges: ['Mejor precio'],
      distanceToCenter: 2.8,
      sustainableCertified: false
    },
    {
      id: 5,
      name: "Riverside Luxury Miku Paris",
      provider: "Miku Inn",
      providerLogo: "🏨",
      city: "París",
      address: "13 Quai de la Tournelle",
      country: "Francia",
      coordinates: { lat: 48.8519, lng: 2.3556 },
      stars: 5,
      rating: 4.7,
      reviews: 945,
      guestScore: 9.0,
      guestScoreText: "Sobresaliente",
      propertyType: "Hotel",
      images: [
        "https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=800",
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"
      ],
      roomType: "Suite",
      pricePerNight: 385,
      totalPrice: 1925,
      originalPrice: 2310,
      discount: 17,
      amenities: ['wifi', 'piscina', 'spa', 'gym', 'restaurant', 'bar', 'breakfast', 'aircon', 'room-service'],
      amenitiesDisplay: ["WiFi Gratis", "Vista al Sena", "Spa", "Gimnasio", "Desayuno"],
      description: "Hotel de lujo a orillas del río Sena con vistas panorámicas",
      cancellationPolicy: "Cancelación gratuita hasta 48h antes",
      freeCancellation: true,
      availableRooms: 4,
      badges: ['Vista al río'],
      distanceToCenter: 1.0,
      sustainableCertified: true
    },
    {
      id: 6,
      name: "Opera District Miku Apart Hotel",
      provider: "Miku Inn",
      providerLogo: "🏨",
      city: "París",
      address: "8 Rue Scribe",
      country: "Francia",
      coordinates: { lat: 48.8718, lng: 2.3312 },
      stars: 4,
      rating: 4.4,
      reviews: 534,
      guestScore: 8.4,
      guestScoreText: "Muy bueno",
      propertyType: "Apart Hotel",
      images: [
        "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800",
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"
      ],
      roomType: "Suite",
      pricePerNight: 210,
      totalPrice: 1050,
      originalPrice: null,
      discount: 0,
      amenities: ['wifi', 'gym', 'breakfast', 'aircon', 'parking'],
      amenitiesDisplay: ["WiFi Gratis", "Cocina", "Gimnasio", "Estacionamiento"],
      description: "Apart hotel moderno cerca de la Ópera Garnier",
      cancellationPolicy: "Cancelación gratuita hasta 24h antes",
      freeCancellation: true,
      availableRooms: 6,
      badges: [],
      distanceToCenter: 1.8,
      sustainableCertified: false
    }
  ];
  
  // Hoteles filtrados
  $: filteredHotels = filterAndSortHotels(allHotels, filters);
  
  function filterAndSortHotels(hotels, filters) {
    let result = hotels.filter(hotel => {
      // Filtro de precio
      if (hotel.pricePerNight < filters.priceMin || hotel.pricePerNight > filters.priceMax) {
        return false;
      }
      
      // Filtro de estrellas
      if (filters.stars.length > 0 && !filters.stars.includes(hotel.stars)) {
        return false;
      }
      
      // Filtro de tipo de habitación
      if (filters.roomTypes.length > 0 && !filters.roomTypes.includes(hotel.roomType)) {
        return false;
      }
      
      // Filtro de amenidades (debe tener TODAS las seleccionadas)
      if (filters.amenities.length > 0) {
        const hasAllAmenities = filters.amenities.every(amenity => 
          hotel.amenities.includes(amenity)
        );
        if (!hasAllAmenities) return false;
      }
      
      // Filtro de tipo de propiedad
      if (filters.propertyTypes.length > 0 && !filters.propertyTypes.includes(hotel.propertyType)) {
        return false;
      }
      
      // Filtro de política de cancelación
      if (filters.cancellationPolicy === 'free' && !hotel.freeCancellation) {
        return false;
      }
      
      // Filtro de calificación de huéspedes
      if (hotel.guestScore < filters.guestRating) {
        return false;
      }
      
      return true;
    });
    
    // Ordenamiento
    result.sort((a, b) => {
      switch(filters.sortBy) {
        case 'price-low':
          return a.pricePerNight - b.pricePerNight;
        case 'price-high':
          return b.pricePerNight - a.pricePerNight;
        case 'rating':
          return b.guestScore - a.guestScore;
        case 'distance':
          return a.distanceToCenter - b.distanceToCenter;
        case 'recommended':
        default:
          // Algoritmo de recomendación simple: rating * disponibilidad
          return (b.guestScore * b.availableRooms) - (a.guestScore * a.availableRooms);
      }
    });
    
    return result;
  }
  
  // Función para resetear filtros
  function resetFilters() {
    filters = {
      priceMin: 0,
      priceMax: 1000,
      stars: [],
      roomTypes: [],
      amenities: [],
      propertyTypes: [],
      cancellationPolicy: 'all',
      guestRating: 0,
      sortBy: 'recommended'
    };
  }
  
  // Toggle de filtros
  function toggleFilter(filterArray, value) {
    const index = filterArray.indexOf(value);
    if (index > -1) {
      filterArray.splice(index, 1);
    } else {
      filterArray.push(value);
    }
    filters = filters; // Trigger reactivity
  }
  
  // Funciones de UI
  function goToHotelDetail(hotelId) {
    navigateTo('hotel-detail', hotelId);
  }
  
  function modifySearch() {
    // En una implementación real, esto abriría un modal o colapsaría la sección de búsqueda
    console.log('Modificar búsqueda');
  }
  
  // Generar estrellas
  function getStars(count) {
    return '⭐'.repeat(count);
  }
  
  // Formatear precio
  function formatPrice(price) {
    return new Intl.NumberFormat('es-GT', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0
    }).format(price);
  }
  
  // Skeleton loader simulado
  function simulateLoading() {
    isLoading = true;
    setTimeout(() => {
      isLoading = false;
    }, 1500);
  }
</script>

<div class="search-results-page">
  <!-- Barra de modificación de búsqueda -->
  <div class="search-modify-bar">
    <div class="container">
      <div class="search-modify-content">
        <div class="search-summary-inline">
          <div class="search-item">
            <span class="search-label">Destino:</span>
            <strong>{search.destination}</strong>
          </div>
          <div class="search-item">
            <span class="search-label">Check-in:</span>
            <strong>{new Date(search.checkIn).toLocaleDateString('es-ES', { day: 'numeric', month: 'short' })}</strong>
          </div>
          <div class="search-item">
            <span class="search-label">Check-out:</span>
            <strong>{new Date(search.checkOut).toLocaleDateString('es-ES', { day: 'numeric', month: 'short' })}</strong>
          </div>
          <div class="search-item">
            <span class="search-label">Huéspedes:</span>
            <strong>{search.adults + search.children} personas</strong>
          </div>
          <div class="search-item">
            <span class="search-label">Habitaciones:</span>
            <strong>{search.rooms}</strong>
          </div>
        </div>
        <button class="modify-search-btn" on:click={modifySearch}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
          </svg>
          Modificar búsqueda
        </button>
      </div>
    </div>
  </div>
  
  <!-- Contenedor principal -->
  <div class="container">
    <div class="results-header">
      <div class="results-info">
        <h1>{search.destination}: {filteredHotels.length} hoteles encontrados</h1>
        <p class="results-subtitle">
          {nights} {nights === 1 ? 'noche' : 'noches'} • 
          {search.adults} {search.adults === 1 ? 'adulto' : 'adultos'}
          {#if search.children > 0}
            • {search.children} {search.children === 1 ? 'niño' : 'niños'}
          {/if}
        </p>
      </div>
      
      <div class="results-actions">
        <div class="view-mode-toggle">
          <button 
            class="view-mode-btn" 
            class:active={viewMode === 'list'}
            on:click={() => viewMode = 'list'}
            title="Vista de lista"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"></line>
              <line x1="8" y1="12" x2="21" y2="12"></line>
              <line x1="8" y1="18" x2="21" y2="18"></line>
              <line x1="3" y1="6" x2="3.01" y2="6"></line>
              <line x1="3" y1="12" x2="3.01" y2="12"></line>
              <line x1="3" y1="18" x2="3.01" y2="18"></line>
            </svg>
          </button>
          <button 
            class="view-mode-btn" 
            class:active={viewMode === 'grid'}
            on:click={() => viewMode = 'grid'}
            title="Vista de cuadrícula"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </button>
        </div>
        
        <button 
          class="map-toggle-btn" 
          class:active={showMap}
          on:click={() => showMap = !showMap}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon>
            <line x1="8" y1="2" x2="8" y2="18"></line>
            <line x1="16" y1="6" x2="16" y2="22"></line>
          </svg>
          {showMap ? 'Ocultar' : 'Ver'} mapa
        </button>
      </div>
    </div>
    
    <div class="results-layout">
      <!-- Panel de filtros lateral -->
      <aside class="filters-panel">
        <div class="filters-header">
          <h2>Filtrar por:</h2>
          <button class="reset-all-btn" on:click={resetFilters}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="1 4 1 10 7 10"></polyline>
              <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
            </svg>
            Limpiar todo
          </button>
        </div>
        
        <!-- Filtro de precio -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
            Rango de precio por noche
          </h3>
          <div class="price-filter">
            <div class="price-inputs-row">
              <div class="price-input-group">
                <label>Mínimo</label>
                <div class="input-with-currency">
                  <span class="currency">$</span>
                  <input 
                    type="number" 
                    bind:value={filters.priceMin}
                    min="0"
                    class="price-input"
                  />
                </div>
              </div>
              <div class="price-separator">—</div>
              <div class="price-input-group">
                <label>Máximo</label>
                <div class="input-with-currency">
                  <span class="currency">$</span>
                  <input 
                    type="number" 
                    bind:value={filters.priceMax}
                    min="0"
                    class="price-input"
                  />
                </div>
              </div>
            </div>
            <div class="price-range-visual">
              <div class="price-range-bar">
                <div 
                  class="price-range-fill"
                  style="left: {(filters.priceMin / 1000) * 100}%; right: {100 - (filters.priceMax / 1000) * 100}%"
                ></div>
              </div>
            </div>
            <div class="price-range-display">
              ${filters.priceMin} - ${filters.priceMax} por noche
            </div>
          </div>
        </div>
        
        <!-- Filtro de estrellas -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            Categoría del hotel
          </h3>
          <div class="checkbox-group">
            {#each starOptions as stars}
              <label class="checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.stars.includes(stars)}
                  on:change={() => toggleFilter(filters.stars, stars)}
                />
                <span class="checkbox-text">
                  <span class="stars-display">{getStars(stars)}</span>
                  <span>{stars} {stars === 1 ? 'estrella' : 'estrellas'}</span>
                </span>
              </label>
            {/each}
          </div>
        </div>
        
        <!-- Filtro de amenidades -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            Servicios y comodidades
          </h3>
          <div class="checkbox-group">
            {#each amenityOptions as amenity}
              <label class="checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.amenities.includes(amenity.id)}
                  on:change={() => toggleFilter(filters.amenities, amenity.id)}
                />
                <span class="checkbox-text">
                  <span class="amenity-icon">{amenity.icon}</span>
                  {amenity.label}
                </span>
              </label>
            {/each}
          </div>
        </div>
        
        <!-- Filtro de tipo de habitación -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            </svg>
            Tipo de habitación
          </h3>
          <div class="checkbox-group">
            {#each roomTypeOptions as roomType}
              <label class="checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.roomTypes.includes(roomType)}
                  on:change={() => toggleFilter(filters.roomTypes, roomType)}
                />
                <span class="checkbox-text">{roomType}</span>
              </label>
            {/each}
          </div>
        </div>
        
        <!-- Filtro de tipo de propiedad -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            Tipo de propiedad
          </h3>
          <div class="checkbox-group">
            {#each propertyTypeOptions as propertyType}
              <label class="checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.propertyTypes.includes(propertyType)}
                  on:change={() => toggleFilter(filters.propertyTypes, propertyType)}
                />
                <span class="checkbox-text">{propertyType}</span>
              </label>
            {/each}
          </div>
        </div>
        
        <!-- Filtro de política de cancelación -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            Política de cancelación
          </h3>
          <div class="radio-group">
            <label class="radio-label">
              <input 
                type="radio" 
                bind:group={filters.cancellationPolicy} 
                value="all"
              />
              <span>Todas</span>
            </label>
            <label class="radio-label">
              <input 
                type="radio" 
                bind:group={filters.cancellationPolicy} 
                value="free"
              />
              <span>Solo cancelación gratuita</span>
            </label>
          </div>
        </div>
        
        <!-- Filtro de calificación de huéspedes -->
        <div class="filter-group">
          <h3 class="filter-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
            </svg>
            Calificación de huéspedes
          </h3>
          <div class="rating-slider">
            {#each [0, 7.0, 8.0, 8.5, 9.0] as rating}
              <button 
                class="rating-chip"
                class:active={filters.guestRating === rating}
                on:click={() => filters.guestRating = rating}
              >
                {rating === 0 ? 'Todas' : `${rating}+`}
              </button>
            {/each}
          </div>
        </div>
      </aside>
      
      <!-- Área de resultados -->
      <main class="results-main">
        <!-- Barra de ordenamiento -->
        <div class="sort-bar">
          <div class="sort-label">Ordenar por:</div>
          <div class="sort-options">
            <button 
              class="sort-option"
              class:active={filters.sortBy === 'recommended'}
              on:click={() => filters.sortBy = 'recommended'}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
              Recomendado
            </button>
            <button 
              class="sort-option"
              class:active={filters.sortBy === 'price-low'}
              on:click={() => filters.sortBy = 'price-low'}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="1" x2="12" y2="23"></line>
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
              </svg>
              Precio: Menor a Mayor
            </button>
            <button 
              class="sort-option"
              class:active={filters.sortBy === 'price-high'}
              on:click={() => filters.sortBy = 'price-high'}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="1" x2="12" y2="23"></line>
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
              </svg>
              Precio: Mayor a Menor
            </button>
            <button 
              class="sort-option"
              class:active={filters.sortBy === 'rating'}
              on:click={() => filters.sortBy = 'rating'}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
              </svg>
              Mejor Valorado
            </button>
            <button 
              class="sort-option"
              class:active={filters.sortBy === 'distance'}
              on:click={() => filters.sortBy = 'distance'}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="10" r="3"></circle>
                <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
              </svg>
              Distancia al centro
            </button>
          </div>
        </div>
        
        <!-- Lista de hoteles -->
        {#if isLoading}
          <!-- Skeleton Loader -->
          <div class="hotels-grid" class:list-view={viewMode === 'list'}>
            {#each Array(3) as _}
              <div class="hotel-card skeleton">
                <div class="skeleton-image"></div>
                <div class="skeleton-content">
                  <div class="skeleton-line"></div>
                  <div class="skeleton-line short"></div>
                  <div class="skeleton-line medium"></div>
                </div>
              </div>
            {/each}
          </div>
        {:else if filteredHotels.length === 0}
          <!-- No results -->
          <div class="no-results">
            <div class="no-results-icon">
              <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
                <line x1="11" y1="8" x2="11" y2="14"></line>
                <line x1="11" y1="16" x2="11.01" y2="16"></line>
              </svg>
            </div>
            <h2>No encontramos hoteles con estos criterios</h2>
            <p>Intenta ajustar tus filtros o realizar una nueva búsqueda</p>
            <button class="btn-primary" on:click={resetFilters}>
              Limpiar todos los filtros
            </button>
          </div>
        {:else}
          <!-- Hoteles -->
          <div class="hotels-grid" class:list-view={viewMode === 'list'} class:grid-view={viewMode === 'grid'}>
            {#each filteredHotels as hotel (hotel.id)}
              <article class="hotel-card" on:click={() => goToHotelDetail(hotel.id)}>
                <!-- Galería de imágenes -->
                <div class="hotel-gallery">
                  <div class="gallery-main-image">
                    <img src={hotel.images[0]} alt={hotel.name} />
                    {#if hotel.discount > 0}
                      <div class="discount-badge">
                        -{hotel.discount}%
                      </div>
                    {/if}
                    {#if hotel.sustainableCertified}
                      <div class="sustainable-badge" title="Certificado sostenible">
                        🌱
                      </div>
                    {/if}
                  </div>
                  {#if hotel.images.length > 1}
                    <div class="gallery-thumbnails">
                      {#each hotel.images.slice(1, 4) as image, i}
                        <div class="gallery-thumb">
                          <img src={image} alt="{hotel.name} - imagen {i + 2}" />
                        </div>
                      {/each}
                      {#if hotel.images.length > 4}
                        <div class="gallery-more">
                          +{hotel.images.length - 4}
                        </div>
                      {/if}
                    </div>
                  {/if}
                  <button class="favorite-btn" on:click|stopPropagation={() => console.log('Toggle favorite')}>
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                  </button>
                </div>
                
                <!-- Información del hotel -->
                <div class="hotel-content">
                  <div class="hotel-header-row">
                    <div class="hotel-title-section">
                      <div class="hotel-badges-row">
                        {#each hotel.badges as badge}
                          <span class="hotel-badge">{badge}</span>
                        {/each}
                      </div>
                      <h2 class="hotel-name">{hotel.name}</h2>
                      <div class="hotel-stars">
                        {getStars(hotel.stars)}
                        <span class="property-type">{hotel.propertyType}</span>
                      </div>
                    </div>
                    
                    <div class="hotel-rating-box">
                      <div class="rating-score">{hotel.guestScore}</div>
                      <div class="rating-text">{hotel.guestScoreText}</div>
                      <div class="rating-reviews">{hotel.reviews} opiniones</div>
                    </div>
                  </div>
                  
                  <div class="hotel-location-row">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="10" r="3"></circle>
                      <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
                    </svg>
                    <span>{hotel.address}</span>
                    <span class="distance-pill">{hotel.distanceToCenter} km del centro</span>
                  </div>
                  
                  <p class="hotel-description">{hotel.description}</p>
                  
                  <div class="hotel-amenities-row">
                    {#each hotel.amenitiesDisplay.slice(0, 5) as amenity}
                      <span class="amenity-pill">{amenity}</span>
                    {/each}
                    {#if hotel.amenitiesDisplay.length > 5}
                      <span class="amenity-more">+{hotel.amenitiesDisplay.length - 5} más</span>
                    {/if}
                  </div>
                  
                  <div class="hotel-room-info">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                      <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                    </svg>
                    <span>{hotel.roomType}</span>
                    {#if hotel.availableRooms <= 3}
                      <span class="urgency-text">• Solo quedan {hotel.availableRooms}</span>
                    {/if}
                  </div>
                  
                  <div class="hotel-cancellation">
                    {#if hotel.freeCancellation}
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                      <span class="cancellation-free">{hotel.cancellationPolicy}</span>
                    {:else}
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="15" y1="9" x2="9" y2="15"></line>
                        <line x1="9" y1="9" x2="15" y2="15"></line>
                      </svg>
                      <span class="cancellation-non-refundable">{hotel.cancellationPolicy}</span>
                    {/if}
                  </div>
                  
                  <div class="hotel-footer-row">
                    <div class="hotel-provider">
                      <span class="provider-logo">{hotel.providerLogo}</span>
                      <span class="provider-name">Proveedor: {hotel.provider}</span>
                    </div>
                    
                    <div class="hotel-pricing">
                      <div class="pricing-details">
                        {#if hotel.originalPrice}
                          <div class="original-price">{formatPrice(hotel.originalPrice)}</div>
                        {/if}
                        <div class="current-price">
                          <span class="price-amount">{formatPrice(hotel.totalPrice)}</span>
                          <span class="price-label">total</span>
                        </div>
                        <div class="price-per-night">
                          {formatPrice(hotel.pricePerNight)}/noche
                        </div>
                      </div>
                      
                      <button class="btn-view-hotel">
                        Ver disponibilidad
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <line x1="5" y1="12" x2="19" y2="12"></line>
                          <polyline points="12 5 19 12 12 19"></polyline>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </article>
            {/each}
          </div>
          
          <!-- Paginación -->
          <div class="pagination">
            <button class="pagination-btn" disabled>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"></polyline>
              </svg>
              Anterior
            </button>
            
            <div class="pagination-numbers">
              <button class="pagination-number active">1</button>
              <button class="pagination-number">2</button>
              <button class="pagination-number">3</button>
              <span class="pagination-dots">...</span>
              <button class="pagination-number">10</button>
            </div>
            
            <button class="pagination-btn">
              Siguiente
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>
        {/if}
      </main>
    </div>
  </div>
  
  <!-- Mapa (simulado) -->
  {#if showMap}
    <div class="map-overlay">
      <div class="map-container">
        <button class="map-close-btn" on:click={() => showMap = false}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
        <div class="map-placeholder">
          <div class="map-icon">
            <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon>
              <line x1="8" y1="2" x2="8" y2="18"></line>
              <line x1="16" y1="6" x2="16" y2="22"></line>
            </svg>
          </div>
          <h3>Vista de mapa</h3>
          <p>Aquí se mostraría un mapa interactivo con la ubicación de cada hotel</p>
        </div>
      </div>
    </div>
  {/if}
</div>

<style>
  /* ========== VARIABLES ========== */
  :root {
    --primary-color: #667eea;
    --primary-dark: #5568d3;
    --secondary-color: #764ba2;
    --success-color: #10b981;
    --danger-color: #ef4444;
    --warning-color: #f59e0b;
    --text-dark: #1e293b;
    --text-medium: #475569;
    --text-light: #64748b;
    --border-color: #e2e8f0;
    --bg-light: #f8fafc;
    --bg-white: #ffffff;
    --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08);
    --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.1);
    --shadow-lg: 0 10px 30px rgba(0, 0, 0, 0.15);
    --radius-sm: 8px;
    --radius-md: 12px;
    --radius-lg: 16px;
  }
  
  /* ========== BASE ========== */
  .search-results-page {
    min-height: 100vh;
    background: var(--bg-light);
  }
  
  .container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 2rem;
  }
  
  /* ========== SEARCH MODIFY BAR ========== */
  .search-modify-bar {
    background: var(--bg-white);
    border-bottom: 1px solid var(--border-color);
    padding: 1rem 0;
    position: sticky;
    top: 0;
    z-index: 100;
    box-shadow: var(--shadow-sm);
  }
  
  .search-modify-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 2rem;
  }
  
  .search-summary-inline {
    display: flex;
    align-items: center;
    gap: 2rem;
    flex-wrap: wrap;
    flex: 1;
  }
  
  .search-item {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }
  
  .search-label {
    color: var(--text-light);
    font-size: 0.875rem;
  }
  
  .search-item strong {
    color: var(--text-dark);
    font-weight: 600;
  }
  
  .modify-search-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    background: var(--bg-light);
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    color: var(--text-dark);
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
  }
  
  .modify-search-btn:hover {
    background: var(--bg-white);
    border-color: var(--primary-color);
    color: var(--primary-color);
  }
  
  /* ========== RESULTS HEADER ========== */
  .results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 2rem 0 1.5rem;
    gap: 2rem;
  }
  
  .results-info h1 {
    color: var(--text-dark);
    font-size: 2rem;
    margin: 0 0 0.5rem 0;
    font-weight: 700;
  }
  
  .results-subtitle {
    color: var(--text-light);
    font-size: 1rem;
    margin: 0;
  }
  
  .results-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .view-mode-toggle {
    display: flex;
    background: var(--bg-white);
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    overflow: hidden;
  }
  
  .view-mode-btn {
    padding: 0.625rem 0.875rem;
    background: var(--bg-white);
    border: none;
    color: var(--text-medium);
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .view-mode-btn:not(:last-child) {
    border-right: 1px solid var(--border-color);
  }
  
  .view-mode-btn.active,
  .view-mode-btn:hover {
    background: var(--primary-color);
    color: white;
  }
  
  .map-toggle-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.625rem 1.25rem;
    background: var(--bg-white);
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    color: var(--text-dark);
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .map-toggle-btn:hover,
  .map-toggle-btn.active {
    background: var(--primary-color);
    color: white;
    border-color: var(--primary-color);
  }
  
  /* ========== LAYOUT ========== */
  .results-layout {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 2rem;
    align-items: start;
    margin-bottom: 3rem;
  }
  
  /* ========== FILTERS PANEL ========== */
  .filters-panel {
    background: var(--bg-white);
    border-radius: var(--radius-md);
    padding: 1.5rem;
    box-shadow: var(--shadow-sm);
    position: sticky;
    top: 100px;
    max-height: calc(100vh - 120px);
    overflow-y: auto;
  }
  
  .filters-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 2px solid var(--border-color);
  }
  
  .filters-header h2 {
    color: var(--text-dark);
    font-size: 1.25rem;
    margin: 0;
  }
  
  .reset-all-btn {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    padding: 0.5rem 0.75rem;
    background: transparent;
    border: none;
    color: var(--primary-color);
    font-weight: 600;
    font-size: 0.875rem;
    cursor: pointer;
    border-radius: var(--radius-sm);
    transition: all 0.2s ease;
  }
  
  .reset-all-btn:hover {
    background: rgba(102, 126, 234, 0.1);
  }
  
  .filter-group {
    margin-bottom: 1.5rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid var(--border-color);
  }
  
  .filter-group:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
  
  .filter-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-dark);
    font-size: 0.95rem;
    font-weight: 600;
    margin: 0 0 1rem 0;
  }
  
  /* Price filter */
  .price-filter {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .price-inputs-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .price-input-group {
    flex: 1;
  }
  
  .price-input-group label {
    display: block;
    font-size: 0.75rem;
    color: var(--text-medium);
    margin-bottom: 0.375rem;
    font-weight: 600;
  }
  
  .input-with-currency {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .currency {
    position: absolute;
    left: 0.75rem;
    color: var(--text-medium);
    font-weight: 600;
  }
  
  .price-input {
    width: 100%;
    padding: 0.625rem 0.75rem 0.625rem 2rem;
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--text-dark);
    background: var(--bg-white);
  }
  
  .price-input:focus {
    outline: none;
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    background: var(--bg-white);
  }
  
  .price-separator {
    color: var(--text-light);
    margin-top: 1.5rem;
  }
  
  .price-range-display {
    color: var(--text-medium);
    font-size: 0.875rem;
    font-weight: 600;
    text-align: center;
    padding: 0.5rem;
    background: var(--bg-light);
    border-radius: var(--radius-sm);
    margin-top: 0.5rem;
  }
  
  .price-range-visual {
    padding-top: 0.5rem;
  }
  
  .price-range-bar {
    position: relative;
    height: 4px;
    background: var(--border-color);
    border-radius: 2px;
  }
  
  .price-range-fill {
    position: absolute;
    top: 0;
    height: 100%;
    background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
    border-radius: 2px;
  }
  
  /* Checkbox group */
  .checkbox-group {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    cursor: pointer;
    padding: 0.375rem 0;
  }
  
  .checkbox-label input[type="checkbox"] {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: var(--primary-color);
  }
  
  .checkbox-text {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-medium);
    font-size: 0.9rem;
  }
  
  .stars-display {
    font-size: 0.75rem;
  }
  
  .amenity-icon {
    font-size: 1rem;
  }
  
  /* Radio group */
  .radio-group {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .radio-label {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    cursor: pointer;
    padding: 0.375rem 0;
  }
  
  .radio-label input[type="radio"] {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: var(--primary-color);
  }
  
  .radio-label span {
    color: var(--text-medium);
    font-size: 0.9rem;
  }
  
  /* Rating slider */
  .rating-slider {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
  }
  
  .rating-chip {
    padding: 0.5rem 1rem;
    background: var(--bg-light);
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    color: var(--text-medium);
    font-weight: 600;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .rating-chip.active,
  .rating-chip:hover {
    background: var(--primary-color);
    color: white;
    border-color: var(--primary-color);
  }
  
  /* ========== RESULTS MAIN ========== */
  .results-main {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  /* Sort bar */
  .sort-bar {
    background: var(--bg-white);
    border-radius: var(--radius-md);
    padding: 1rem 1.5rem;
    box-shadow: var(--shadow-sm);
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .sort-label {
    color: var(--text-medium);
    font-weight: 600;
    font-size: 0.95rem;
  }
  
  .sort-options {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
    flex: 1;
  }
  
  .sort-option {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    padding: 0.5rem 1rem;
    background: var(--bg-light);
    border: 1.5px solid transparent;
    border-radius: var(--radius-sm);
    color: var(--text-medium);
    font-weight: 600;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
  }
  
  .sort-option.active {
    background: var(--primary-color);
    color: white;
    border-color: var(--primary-color);
  }
  
  .sort-option:not(.active):hover {
    background: var(--bg-white);
    border-color: var(--border-color);
    color: var(--primary-color);
  }
  
  /* ========== HOTELS GRID ========== */
  .hotels-grid {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .hotels-grid.grid-view {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
  
  /* ========== HOTEL CARD ========== */
  .hotel-card {
    background: var(--bg-white);
    border-radius: var(--radius-lg);
    overflow: hidden;
    box-shadow: var(--shadow-sm);
    transition: all 0.3s ease;
    cursor: pointer;
    display: flex;
    flex-direction: column;
  }
  
  .list-view .hotel-card {
    flex-direction: row;
  }
  
  .grid-view .hotel-card {
    flex-direction: column;
  }
  
  .hotel-card:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-4px);
  }
  
  /* Gallery */
  .hotel-gallery {
    position: relative;
    flex-shrink: 0;
  }
  
  .list-view .hotel-gallery {
    width: 350px;
    height: 280px;
  }
  
  .grid-view .hotel-gallery {
    width: 100%;
    height: 220px;
  }
  
  .gallery-main-image {
    position: relative;
    width: 100%;
    height: 100%;
  }
  
  .gallery-main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .discount-badge {
    position: absolute;
    top: 1rem;
    left: 1rem;
    background: linear-gradient(135deg, #ef4444, #dc2626);
    color: white;
    padding: 0.5rem 0.875rem;
    border-radius: 6px;
    font-weight: 700;
    font-size: 0.875rem;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  }
  
  .sustainable-badge {
    position: absolute;
    top: 1rem;
    right: 1rem;
    background: rgba(255, 255, 255, 0.95);
    padding: 0.5rem;
    border-radius: 50%;
    font-size: 1.25rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  .gallery-thumbnails {
    position: absolute;
    bottom: 0.75rem;
    right: 0.75rem;
    display: flex;
    gap: 0.375rem;
  }
  
  .gallery-thumb {
    width: 50px;
    height: 50px;
    border-radius: 6px;
    overflow: hidden;
    border: 2px solid white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
  
  .gallery-thumb img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .gallery-more {
    width: 50px;
    height: 50px;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 0.875rem;
    border: 2px solid white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
  
  .favorite-btn {
    position: absolute;
    top: 1rem;
    right: 1rem;
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.95);
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  .favorite-btn:hover {
    background: white;
    transform: scale(1.1);
  }
  
  .favorite-btn svg {
    color: var(--text-medium);
    transition: all 0.2s ease;
  }
  
  .favorite-btn:hover svg {
    color: #ef4444;
    fill: #ef4444;
  }
  
  /* Content */
  .hotel-content {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    flex: 1;
  }
  
  .hotel-header-row {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
  }
  
  .hotel-title-section {
    flex: 1;
  }
  
  .hotel-badges-row {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 0.625rem;
  }
  
  .hotel-badge {
    padding: 0.25rem 0.75rem;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
    color: var(--primary-color);
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .hotel-name {
    color: var(--text-dark);
    font-size: 1.5rem;
    margin: 0 0 0.5rem 0;
    font-weight: 700;
    line-height: 1.3;
  }
  
  .hotel-stars {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    font-size: 0.875rem;
  }
  
  .property-type {
    color: var(--text-light);
    font-weight: 500;
  }
  
  .hotel-rating-box {
    flex-shrink: 0;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    padding: 0.75rem 1rem;
    border-radius: var(--radius-md);
    text-align: center;
    min-width: 90px;
  }
  
  .rating-score {
    color: white;
    font-size: 2rem;
    font-weight: 700;
    line-height: 1;
    margin-bottom: 0.25rem;
  }
  
  .rating-text {
    color: white;
    font-size: 0.75rem;
    font-weight: 600;
    margin-bottom: 0.25rem;
  }
  
  .rating-reviews {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.7rem;
  }
  
  .hotel-location-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-light);
    font-size: 0.9rem;
    flex-wrap: wrap;
  }
  
  .distance-pill {
    padding: 0.25rem 0.625rem;
    background: var(--bg-light);
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--text-medium);
  }
  
  .hotel-description {
    color: var(--text-medium);
    font-size: 0.95rem;
    line-height: 1.5;
    margin: 0;
  }
  
  .hotel-amenities-row {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .amenity-pill {
    padding: 0.375rem 0.875rem;
    background: rgba(102, 126, 234, 0.08);
    color: var(--primary-color);
    border-radius: 12px;
    font-size: 0.8rem;
    font-weight: 600;
  }
  
  .amenity-more {
    padding: 0.375rem 0.875rem;
    background: var(--bg-light);
    color: var(--text-medium);
    border-radius: 12px;
    font-size: 0.8rem;
    font-weight: 600;
  }
  
  .hotel-room-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-medium);
    font-size: 0.9rem;
  }
  
  .urgency-text {
    color: #ef4444;
    font-weight: 600;
  }
  
  .hotel-cancellation {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
  }
  
  .cancellation-free {
    color: var(--success-color);
    font-weight: 600;
  }
  
  .cancellation-non-refundable {
    color: var(--text-light);
  }
  
  .hotel-footer-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 1rem;
    margin-top: auto;
    padding-top: 1rem;
    border-top: 1.5px solid var(--border-color);
  }
  
  .hotel-provider {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .provider-logo {
    font-size: 1.5rem;
  }
  
  .provider-name {
    color: var(--text-light);
    font-size: 0.85rem;
  }
  
  .hotel-pricing {
    display: flex;
    align-items: flex-end;
    gap: 1rem;
  }
  
  .pricing-details {
    text-align: right;
  }
  
  .original-price {
    color: var(--text-light);
    font-size: 0.875rem;
    text-decoration: line-through;
  }
  
  .current-price {
    display: flex;
    align-items: baseline;
    gap: 0.375rem;
    justify-content: flex-end;
  }
  
  .price-amount {
    color: var(--text-dark);
    font-size: 2rem;
    font-weight: 700;
    line-height: 1;
  }
  
  .price-label {
    color: var(--text-light);
    font-size: 0.875rem;
  }
  
  .price-per-night {
    color: var(--text-medium);
    font-size: 0.875rem;
    margin-top: 0.25rem;
  }
  
  .btn-view-hotel {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.875rem 1.5rem;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
    border: none;
    border-radius: var(--radius-sm);
    font-weight: 700;
    font-size: 0.95rem;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    white-space: nowrap;
  }
  
  .btn-view-hotel:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }
  
  /* ========== PAGINATION ========== */
  .pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    padding: 2rem 0;
  }
  
  .pagination-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    background: var(--bg-white);
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    color: var(--text-dark);
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .pagination-btn:not(:disabled):hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
  }
  
  .pagination-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .pagination-numbers {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .pagination-number {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-white);
    border: 1.5px solid var(--border-color);
    border-radius: var(--radius-sm);
    color: var(--text-dark);
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .pagination-number:hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
  }
  
  .pagination-number.active {
    background: var(--primary-color);
    border-color: var(--primary-color);
    color: white;
  }
  
  .pagination-dots {
    color: var(--text-light);
    padding: 0 0.5rem;
  }
  
  /* ========== SKELETON LOADER ========== */
  .hotel-card.skeleton {
    pointer-events: none;
  }
  
  .skeleton-image {
    width: 350px;
    height: 280px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }
  
  .skeleton-content {
    padding: 1.5rem;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .skeleton-line {
    height: 20px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
  }
  
  .skeleton-line.short {
    width: 60%;
  }
  
  .skeleton-line.medium {
    width: 80%;
  }
  
  @keyframes shimmer {
    0% {
      background-position: -200% 0;
    }
    100% {
      background-position: 200% 0;
    }
  }
  
  /* ========== NO RESULTS ========== */
  .no-results {
    background: var(--bg-white);
    padding: 4rem 2rem;
    border-radius: var(--radius-lg);
    text-align: center;
    box-shadow: var(--shadow-sm);
  }
  
  .no-results-icon {
    color: var(--text-light);
    margin-bottom: 1.5rem;
  }
  
  .no-results h2 {
    color: var(--text-dark);
    font-size: 1.5rem;
    margin: 0 0 0.75rem 0;
  }
  
  .no-results p {
    color: var(--text-medium);
    font-size: 1.05rem;
    margin: 0 0 2rem 0;
  }
  
  .btn-primary {
    padding: 0.875rem 2rem;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
    border: none;
    border-radius: var(--radius-sm);
    font-weight: 700;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
  
  .btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }
  
  /* ========== MAP OVERLAY ========== */
  .map-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    animation: fadeIn 0.3s ease;
  }
  
  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
  
  .map-container {
    position: relative;
    width: 100%;
    max-width: 1200px;
    height: 80vh;
    background: var(--bg-white);
    border-radius: var(--radius-lg);
    overflow: hidden;
    box-shadow: var(--shadow-lg);
  }
  
  .map-close-btn {
    position: absolute;
    top: 1rem;
    right: 1rem;
    width: 40px;
    height: 40px;
    background: white;
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    z-index: 10;
    transition: all 0.2s ease;
  }
  
  .map-close-btn:hover {
    background: var(--bg-light);
    transform: scale(1.1);
  }
  
  .map-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: var(--bg-light);
  }
  
  .map-icon {
    color: var(--primary-color);
    margin-bottom: 1rem;
  }
  
  .map-placeholder h3 {
    color: var(--text-dark);
    font-size: 1.5rem;
    margin: 0 0 0.5rem 0;
  }
  
  .map-placeholder p {
    color: var(--text-medium);
    font-size: 1rem;
    margin: 0;
  }
  
  /* ========== RESPONSIVE ========== */
  @media (max-width: 1200px) {
    .results-layout {
      grid-template-columns: 260px 1fr;
    }
  }
  
  @media (max-width: 1024px) {
    .results-layout {
      grid-template-columns: 1fr;
    }
    
    .filters-panel {
      position: static;
      max-height: none;
    }
    
    .list-view .hotel-card {
      flex-direction: column;
    }
    
    .list-view .hotel-gallery {
      width: 100%;
      height: 250px;
    }
  }
  
  @media (max-width: 768px) {
    .container {
      padding: 0 1rem;
    }
    
    .search-modify-content {
      flex-direction: column;
      align-items: stretch;
    }
    
    .search-summary-inline {
      flex-direction: column;
      gap: 0.75rem;
    }
    
    .results-header {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .results-actions {
      width: 100%;
      justify-content: space-between;
    }
    
    .sort-bar {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .sort-options {
      width: 100%;
    }
    
    .hotels-grid.grid-view {
      grid-template-columns: 1fr;
    }
    
    .hotel-footer-row {
      flex-direction: column;
      align-items: stretch;
      gap: 1rem;
    }
    
    .hotel-pricing {
      flex-direction: column;
      align-items: stretch;
    }
    
    .pricing-details {
      text-align: left;
    }
    
    .current-price {
      justify-content: flex-start;
    }
    
    .btn-view-hotel {
      width: 100%;
      justify-content: center;
    }
  }
</style>