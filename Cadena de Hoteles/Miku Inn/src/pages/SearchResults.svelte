<script>
  export let navigateTo;
  export let searchParams = null;
  import '../styles/searchresults.css';
  
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
  const roomTypeOptions = ['Gran Suite', 'Suite', 'Junior Suite', 'Habitación Doble', 'Habitación Simple'];
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
      guestScore: 5,
      guestScoreText: "Extraordinario",
      propertyType: "Hotel",
      images: [
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800",
        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800"
      ],
      roomType: "Doble",
      pricePerNight: 280,
      totalPrice: 1400,
      originalPrice: 1750,
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
      guestScore: 4.6,
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
      guestScore: 5,
      guestScoreText: "Extraordinario",
      propertyType: "Resort",
      images: [
        "https://images.unsplash.com/photo-1549294413-26f195200c16?w=800",
        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",
        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800",
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"
      ],
      roomType: "Gran Suite",
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
      guestScore: 4.5,
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
      guestScore: 4.7,
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
      guestScore: 4,
      guestScoreText: "Bueno",
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
    <div class="search__container">
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
  <div class="search__container">
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
              <label class="search__checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.stars.includes(stars)}
                  on:change={() => toggleFilter(filters.stars, stars)}
                />
                <span class="search__checkbox-text">
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
              <label class="search__checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.amenities.includes(amenity.id)}
                  on:change={() => toggleFilter(filters.amenities, amenity.id)}
                />
                <span class="search__checkbox-text">
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
              <label class="search__checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.roomTypes.includes(roomType)}
                  on:change={() => toggleFilter(filters.roomTypes, roomType)}
                />
                <span class="search__checkbox-text">{roomType}</span>
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
              <label class="search__checkbox-label">
                <input 
                  type="checkbox" 
                  checked={filters.propertyTypes.includes(propertyType)}
                  on:change={() => toggleFilter(filters.propertyTypes, propertyType)}
                />
                <span class="search__checkbox-text">{propertyType}</span>
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
            <label class="search__radio-label">
              <input 
                type="radio" 
                bind:group={filters.cancellationPolicy} 
                value="all"
              />
              <span>Todas</span>
            </label>
            <label class="search__radio-label">
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
          <div class="search__hotels-grid" class:list-view={viewMode === 'list'}>
            {#each Array(3) as _}
              <div class="search__hotel-card skeleton">
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
          <div class="search__no-results">
            <div class="search__no-results-icon">
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
          <div class="search__hotels-grid" class:list-view={viewMode === 'list'} class:grid-view={viewMode === 'grid'}>
            {#each filteredHotels as hotel (hotel.id)}
              <article class="search__hotel-card" on:click={() => goToHotelDetail(hotel.id)}>
                <!-- Galería de imágenes -->
                <div class="hotel-gallery">
                  <div class="search__gallery-main-image">
                    <img src={hotel.images[0]} alt={hotel.name} />
                    {#if hotel.discount > 0}
                      <div class="search__discount-badge">
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
                    <div class="search__gallery-thumbnails">
                      {#each hotel.images.slice(1, 4) as image, i}
                        <div class="search__gallery-thumb">
                          <img src={image} alt="{hotel.name} - imagen {i + 2}" />
                        </div>
                      {/each}
                      {#if hotel.images.length > 4}
                        <div class="search__gallery-more">
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
                <div class="search__hotel-content">
                  <div class="hotel-header-row">
                    <div class="hotel-title-section">
                      <div class="hotel-badges-row">
                        {#each hotel.badges as badge}
                          <span class="hotel-badge">{badge}</span>
                        {/each}
                      </div>
                      <h2 class="search__hotel-name">{hotel.name}</h2>
                      <div class="search__hotel-stars">
                        {getStars(hotel.stars)}
                        <span class="search__property-type">{hotel.propertyType}</span>
                      </div>
                    </div>
                    
                    <div class="hotel-rating-box">
                      <div class="search__rating-score">{hotel.guestScore}</div>
                      <div class="search__rating-text">{hotel.guestScoreText}</div>
                      <div class="search__rating-reviews">{hotel.reviews} opiniones</div>
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
                          <span class="search__price-amount">{formatPrice(hotel.totalPrice)}</span>
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
              <button class="pagination-number search__active">1</button>
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
      <div class="search__map-container">
        <button class="map-close-btn" on:click={() => showMap = false}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
        <div class="search__map-placeholder">
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