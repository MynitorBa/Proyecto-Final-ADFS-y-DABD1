<script>
  export let navigateTo;
  export let hotelId = null;
  
  // Mock data - En producción vendría del backend basado en hotelId
  let hotel = {
    id: hotelId || 1,
    name: "Grand Miku Palace Paris",
    provider: "Miku Inn",
    providerLogo: "🏨",
    city: "París",
    address: "15 Avenue des Champs-Élysées, 75008 París",
    country: "Francia",
    coordinates: { lat: 48.8738, lng: 2.2950 },
    stars: 5,
    rating: 4.8,
    reviews: 1247,
    guestScore: 5,
    guestScoreText: "Excepcional",
    propertyType: "Hotel",
    description: "Ubicado en el corazón de París, el Grand Miku Palace ofrece una experiencia única combinando elegancia moderna con el encanto parisino tradicional. Disfrute de vistas espectaculares, servicio excepcional y habitaciones de lujo en uno de los hoteles más prestigiosos de la ciudad.",
    longDescription: "El Grand Miku Palace Paris es un hotel de 5 estrellas que combina la elegancia clásica francesa con las comodidades modernas. Ubicado en la prestigiosa Avenue des Champs-Élysées, ofrece fácil acceso a las principales atracciones de París como la Torre Eiffel, el Arco del Triunfo y el Museo del Louvre. Nuestras habitaciones y suites están decoradas con un estilo contemporáneo elegante, equipadas con tecnología de última generación y cuentan con hermosas vistas a la ciudad. El hotel cuenta con un restaurante galardonado con estrella Michelin, un spa de clase mundial, piscina interior climatizada y gimnasio completamente equipado.",
    images: [
      "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200",
      "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1200",
      "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=1200",
      "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=1200",
      "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200",
      "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200"
    ],
    amenities: [
      { id: 'wifi', icon: '📶', name: 'WiFi de Alta Velocidad' },
      { id: 'pool', icon: '🏊', name: 'Piscina Interior Climatizada' },
      { id: 'spa', icon: '💆', name: 'Spa & Centro de Bienestar' },
      { id: 'gym', icon: '💪', name: 'Gimnasio 24/7' },
      { id: 'restaurant', icon: '🍽️', name: 'Restaurante Gourmet' },
      { id: 'bar', icon: '🍹', name: 'Bar & Lounge' },
      { id: 'breakfast', icon: '🍳', name: 'Desayuno Buffet' },
      { id: 'parking', icon: '🅿️', name: 'Estacionamiento Valet' },
      { id: 'concierge', icon: '🛎️', name: 'Concierge 24/7' },
      { id: 'room-service', icon: '🏨', name: 'Servicio a Habitación' },
      { id: 'laundry', icon: '👔', name: 'Servicio de Lavandería' },
      { id: 'airport', icon: '✈️', name: 'Traslado Aeropuerto' },
      { id: 'meeting', icon: '🏢', name: 'Salas de Reuniones' },
      { id: 'pets', icon: '🐕', name: 'Mascotas Permitidas' },
      { id: 'aircon', icon: '❄️', name: 'Aire Acondicionado' },
      { id: 'safe', icon: '🔒', name: 'Caja Fuerte' }
    ],
    checkInTime: "15:00",
    checkOutTime: "12:00",
    cancellationPolicy: "Cancelación gratuita hasta 48h antes del check-in",
    freeCancellation: true,
    policies: [
      "Check-in desde las 15:00",
      "Check-out hasta las 12:00",
      "Se requiere identificación oficial con foto",
      "Se acepta tarjeta de crédito para depósito",
      "No fumar en habitaciones",
      "Mascotas permitidas (cargo adicional)",
      "Edad mínima para check-in: 18 años"
    ],
    zoneInfo: {
      description: "El hotel está ubicado en el prestigioso distrito 8 de París, sobre la famosa Avenue des Champs-Élysées. Esta ubicación privilegiada le permite acceder fácilmente a las principales atracciones de la ciudad.",
      nearbyAttractions: [
        { name: "Torre Eiffel", distance: "1.5 km", time: "15 min a pie" },
        { name: "Arco del Triunfo", distance: "800 m", time: "10 min a pie" },
        { name: "Museo del Louvre", distance: "2.5 km", time: "5 min en taxi" },
        { name: "Ópera Garnier", distance: "2.0 km", time: "20 min a pie" },
        { name: "Jardines de las Tullerías", distance: "2.2 km", time: "25 min a pie" }
      ],
      transportation: [
        { type: "Metro", name: "George V", distance: "200 m" },
        { type: "Bus", name: "Líneas 28, 32, 73", distance: "100 m" },
        { type: "Aeropuerto CDG", name: "Charles de Gaulle", distance: "25 km" }
      ]
    },
    distanceToCenter: 1.2,
    sustainableCertified: true
  };
  
  // Room types con disponibilidad
  let roomTypes = [
    {
      id: 1,
      name: "Doble",
      description: "Habitación elegante con cama king size y vistas a la ciudad",
      size: 28,
      maxGuests: 2,
      bedType: "1 Cama King Size",
      pricePerNight: 280,
      originalPrice: 350,
      discount: 20,
      available: 5,
      image: "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600",
      images: [
        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800"
      ],
      features: [
        "WiFi de alta velocidad",
        "TV LED 55\"",
        "Minibar premium",
        "Baño de mármol",
        "Escritorio ejecutivo",
        "Cafetera Nespresso",
        "Caja fuerte",
        "Aire acondicionado"
      ],
      amenities: ['wifi', 'aircon', 'safe', 'room-service']
    },
    {
      id: 2,
      name: "Junior Suite",
      description: "Suite espaciosa con área de estar separada y balcón privado",
      size: 42,
      maxGuests: 3,
      bedType: "1 Cama King Size + Sofá Cama",
      pricePerNight: 420,
      originalPrice: 525,
      discount: 20,
      available: 3,
      image: "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600",
      images: [
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",
        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800"
      ],
      features: [
        "WiFi de alta velocidad",
        "2 TV LED 55\"",
        "Minibar premium",
        "Baño de mármol con tina",
        "Sala de estar",
        "Balcón con vista",
        "Escritorio ejecutivo",
        "Cafetera Nespresso",
        "Caja fuerte",
        "Aire acondicionado"
      ],
      amenities: ['wifi', 'aircon', 'safe', 'room-service', 'breakfast']
    },
    {
      id: 3,
      name: "Suite",
      description: "Suite ejecutiva de lujo con sala de estar, comedor y vista panorámica",
      size: 65,
      maxGuests: 4,
      bedType: "1 Cama King Size + 1 Cama Queen",
      pricePerNight: 680,
      originalPrice: 850,
      discount: 20,
      available: 2,
      image: "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=600",
      images: [
        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800",
        "https://images.unsplash.com/photo-1591088398332-8a7791972843?w=800"
      ],
      features: [
        "WiFi de alta velocidad",
        "3 TV LED 65\"",
        "Minibar premium",
        "2 Baños de mármol",
        "Sala de estar amplia",
        "Comedor para 6 personas",
        "Cocina equipada",
        "Balcón panorámico",
        "Escritorio ejecutivo",
        "Cafetera Nespresso",
        "Caja fuerte",
        "Aire acondicionado"
      ],
      amenities: ['wifi', 'aircon', 'safe', 'room-service', 'breakfast', 'concierge']
    },
    {
      id: 4,
      name: "Grand Suite",
      description: "Suite presidencial con vistas espectaculares de la Torre Eiffel",
      size: 120,
      maxGuests: 6,
      bedType: "2 Camas King Size + 1 Cama Queen",
      pricePerNight: 1250,
      originalPrice: null,
      discount: 0,
      available: 1,
      image: "https://images.unsplash.com/photo-1591088398332-8a7791972843?w=600",
      images: [
        "https://images.unsplash.com/photo-1591088398332-8a7791972843?w=800",
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",
        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800"
      ],
      features: [
        "WiFi de alta velocidad",
        "4 TV LED 75\"",
        "Minibar premium con champagne",
        "3 Baños de mármol con jacuzzi",
        "Sala de estar de lujo",
        "Comedor para 10 personas",
        "Cocina completamente equipada",
        "Terraza privada con vista Torre Eiffel",
        "Oficina privada",
        "Sistema de sonido Bang & Olufsen",
        "Mayordomo personal 24/7",
        "Cafetera Nespresso",
        "Caja fuerte grande",
        "Aire acondicionado"
      ],
      amenities: ['wifi', 'aircon', 'safe', 'room-service', 'breakfast', 'concierge', 'spa']
    }
  ];
  
  // Reviews/Comments
  let reviews = [
    {
      id: 1,
      author: "María González",
      country: "España",
      avatar: "👩",
      date: "2026-01-25",
      rating: 5,
      guestScore: 5,
      roomType: "Executive Suite",
      stayDuration: "3 noches",
      verifiedStay: true,
      title: "Experiencia inolvidable",
      text: "Mi estancia en el Grand Miku Palace fue absolutamente excepcional. Desde el momento en que llegué, el personal fue extremadamente atento y profesional. La suite ejecutiva era espaciosa, elegante y con vistas impresionantes. El desayuno buffet era de primera clase y la ubicación es perfecta para explorar París. Sin duda volveré.",
      helpful: 24,
      photos: ["https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=400"],
      pros: ["Ubicación excepcional", "Personal amable", "Habitaciones impecables"],
      cons: [],
      hotelResponse: {
        author: "Hotel Miku Inn",
        date: "2026-01-26",
        text: "Estimada María, muchísimas gracias por sus maravillosas palabras. Nos llena de orgullo saber que disfrutó de su estancia con nosotros. Esperamos recibirla nuevamente pronto. Saludos cordiales, Equipo Grand Miku Palace"
      }
    },
    {
      id: 2,
      author: "John Smith",
      country: "USA",
      avatar: "👨",
      date: "2026-01-20",
      rating: 5,
      guestScore: 4.7,
      roomType: "Junior Suite",
      stayDuration: "5 noches",
      verifiedStay: true,
      title: "Perfect location and service",
      text: "The Grand Miku Palace exceeded all expectations. The location on the Champs-Élysées is unbeatable. Room was clean, modern and comfortable. Staff went above and beyond to make our stay special. Highly recommend!",
      helpful: 18,
      photos: [],
      pros: ["Amazing location", "Excellent service", "Clean rooms"],
      cons: ["Breakfast could have more variety"],
      hotelResponse: null
    },
    {
      id: 3,
      author: "Sophie Dubois",
      country: "Francia",
      avatar: "👩",
      date: "2026-01-15",
      rating: 4,
      guestScore: 4.3,
      roomType: "Habitación Deluxe",
      stayDuration: "2 noches",
      verifiedStay: true,
      title: "Très bon séjour",
      text: "Hotel muy agradable con excelente ubicación. La habitación era confortable aunque un poco pequeña. El personal es muy servicial. El spa es fantástico. Recomendado para una escapada de fin de semana en París.",
      helpful: 12,
      photos: [],
      pros: ["Ubicación", "Spa", "Personal"],
      cons: ["Habitación algo pequeña"],
      hotelResponse: {
        author: "Hotel Miku Inn",
        date: "2026-01-16",
        text: "Chère Sophie, merci beaucoup pour votre avis. Nous sommes ravis que vous ayez apprécié votre séjour. Nous prenons note de vos commentaires pour améliorer constamment nos services. Au plaisir de vous revoir!"
      }
    }
  ];
  
  // Estados de UI
  let selectedRoom = null;
  let selectedRoomQuantity = 1;
  let currentImageIndex = 0;
  let showImageGallery = false;
  let galleryStartIndex = 0;
  let activeTab = 'overview'; // overview, rooms, reviews, location
  
  // Búsqueda
  let checkInDate = '';
  let checkOutDate = '';
  let adults = 2;
  let children = 0;
  let rooms = 1;
  
  // Inicializar fechas
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const dayAfter = new Date(tomorrow);
  dayAfter.setDate(dayAfter.getDate() + 1);
  
  checkInDate = tomorrow.toISOString().split('T')[0];
  checkOutDate = dayAfter.toISOString().split('T')[0];
  
  // Calcular noches
  $: nights = calculateNights(checkInDate, checkOutDate);
  $: totalGuests = adults + children;
  
  function calculateNights(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 0;
    const date1 = new Date(checkIn);
    const date2 = new Date(checkOut);
    const diffTime = Math.abs(date2 - date1);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }
  
  // Calcular precio total
  $: totalPrice = selectedRoom ? (selectedRoom.pricePerNight * nights * selectedRoomQuantity) : 0;
  $: totalOriginalPrice = selectedRoom && selectedRoom.originalPrice ? (selectedRoom.originalPrice * nights * selectedRoomQuantity) : null;
  $: totalSavings = totalOriginalPrice ? (totalOriginalPrice - totalPrice) : 0;
  
  // Galería de imágenes
  function openGallery(index = 0) {
    galleryStartIndex = index;
    currentImageIndex = index;
    showImageGallery = true;
    document.body.style.overflow = 'hidden';
  }
  
  function closeGallery() {
    showImageGallery = false;
    document.body.style.overflow = 'auto';
  }
  
  function nextGalleryImage() {
    currentImageIndex = (currentImageIndex + 1) % hotel.images.length;
  }
  
  function prevGalleryImage() {
    currentImageIndex = (currentImageIndex - 1 + hotel.images.length) % hotel.images.length;
  }
  
  // Selección de habitación
  function selectRoom(room) {
    selectedRoom = room;
    selectedRoomQuantity = 1;
    scrollToBookingSummary();
  }
  
  function scrollToBookingSummary() {
    const summary = document.querySelector('.booking-summary');
    if (summary) {
      summary.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
  
  // Acciones principales
  function bookNow() {
    if (!selectedRoom) {
      alert('Por favor selecciona una habitación');
      return;
    }
    
    if (!checkInDate || !checkOutDate) {
      alert('Por favor selecciona las fechas de tu estadía');
      return;
    }
    
    // En producción, esto navegaría al checkout
    console.log('Reservar ahora:', {
      hotel: hotel.name,
      room: selectedRoom.name,
      quantity: selectedRoomQuantity,
      checkIn: checkInDate,
      checkOut: checkOutDate,
      guests: { adults, children },
      nights,
      totalPrice
    });
    
    navigateTo('checkout', {
      hotelId: hotel.id,
      roomId: selectedRoom.id,
      quantity: selectedRoomQuantity,
      checkIn: checkInDate,
      checkOut: checkOutDate,
      adults,
      children,
      nights,
      totalPrice
    });
  }
  
  function addToCart() {
    if (!selectedRoom) {
      alert('Por favor selecciona una habitación');
      return;
    }
    
    console.log('Agregar al carrito:', selectedRoom);
    alert('Habitación agregada al carrito');
  }
  
  function toggleFavorite() {
    console.log('Toggle favorito para hotel:', hotel.id);
    alert('Hotel agregado a favoritos');
  }
  
  function shareHotel() {
    if (navigator.share) {
      navigator.share({
        title: hotel.name,
        text: hotel.description,
        url: window.location.href
      });
    } else {
      // Fallback: copiar URL al portapapeles
      navigator.clipboard.writeText(window.location.href);
      alert('Enlace copiado al portapapeles');
    }
  }
  
  // Helpers
  function getStars(rating) {
    return '⭐'.repeat(rating);
  }
  
  function formatPrice(price) {
    return new Intl.NumberFormat('es-GT', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0
    }).format(price);
  }
  
  function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    });
  }
  
  function getScoreColor(score) {
    if (score >= 5.0) return '#10b981';
    if (score >= 4.5) return '#3b82f6';
    if (score >= 4.0) return '#f59e0b';
    return '#64748b';
  }
  
  function getScoreText(score) {
    if (score >= 5.0) return 'Extraordinario';
    if (score >= 4.5) return 'Excepcional';
    if (score >= 4.0) return 'Fabuloso';
    if (score >= 8.5) return 'Muy bueno';
    if (score >= 3.0) return 'Bueno';
    return 'Aceptable';
  }
</script>

<svelte:window on:keydown={(e) => {
  if (showImageGallery) {
    if (e.key === 'Escape') closeGallery();
    if (e.key === 'ArrowLeft') prevGalleryImage();
    if (e.key === 'ArrowRight') nextGalleryImage();
  }
}} />

<div class="hotel-detail-page">
  <!-- Breadcrumb Navigation -->
  <div class="breadcrumb-container">
    <div class="container">
      <nav class="breadcrumb">
        <button class="breadcrumb-link" on:click={() => navigateTo('home')}>
          Inicio
        </button>
        <span class="breadcrumb-separator">/</span>
        <button class="breadcrumb-link" on:click={() => navigateTo('search-results')}>
          Búsqueda
        </button>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-current">{hotel.name}</span>
      </nav>
    </div>
  </div>
  
  <!-- Hotel Header -->
  <div class="hotel-header-section">
    <div class="container">
      <div class="hotel-header">
        <div class="hotel-title-group">
          <div class="hotel-badges">
            {#if hotel.sustainableCertified}
              <span class="badge-sustainable" title="Certificado Sostenible">
                🌱 Certificado Sostenible
              </span>
            {/if}
            <span class="badge-provider">
              {hotel.providerLogo} {hotel.provider}
            </span>
          </div>
          
          <h1 class="hotel-name">{hotel.name}</h1>
          
          <div class="hotel-stars-location">
            <span class="hotel-stars">{getStars(hotel.stars)}</span>
            <span class="property-type">{hotel.propertyType}</span>
            <span class="separator">•</span>
            <span class="hotel-location">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="10" r="3"></circle>
                <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
              </svg>
              {hotel.address}
            </span>
            <span class="separator">•</span>
            <span class="distance-center">{hotel.distanceToCenter} km del centro</span>
          </div>
        </div>
        
        <div class="hotel-rating-actions">
          <div class="rating-box-large" style="background: linear-gradient(135deg, {getScoreColor(hotel.guestScore)}, {getScoreColor(hotel.guestScore)}dd);">
            <div class="score-number">{hotel.guestScore}</div>
            <div class="score-text">{hotel.guestScoreText}</div>
            <div class="score-reviews">{hotel.reviews} opiniones</div>
          </div>
          
          <div class="header-actions">
            <button class="action-btn action-btn-secondary" on:click={toggleFavorite} title="Agregar a favoritos">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              Guardar
            </button>
            <button class="action-btn action-btn-secondary" on:click={shareHotel} title="Compartir">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"></circle>
                <circle cx="6" cy="12" r="3"></circle>
                <circle cx="18" cy="19" r="3"></circle>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
              </svg>
              Compartir
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Image Gallery Preview -->
  <div class="gallery-preview-section">
    <div class="container">
      <div class="gallery-grid">
        <button class="gallery-main-image" on:click={() => openGallery(0)}>
          <img src={hotel.images[0]} alt="{hotel.name} - Imagen principal" />
          <div class="gallery-overlay">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
            Ver todas las fotos
          </div>
        </button>
        
        <div class="gallery-thumbnails">
          {#each hotel.images.slice(1, 5) as image, i}
            <button class="gallery-thumb" on:click={() => openGallery(i + 1)}>
              <img src={image} alt="{hotel.name} - Imagen {i + 2}" />
            </button>
          {/each}
          {#if hotel.images.length > 5}
            <button class="gallery-thumb gallery-more" on:click={() => openGallery(5)}>
              <img src={hotel.images[5]} alt="{hotel.name} - Más imágenes" />
              <div class="gallery-more-overlay">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <circle cx="8.5" cy="8.5" r="1.5"></circle>
                  <polyline points="21 15 16 10 5 21"></polyline>
                </svg>
                <span>+{hotel.images.length - 5} fotos</span>
              </div>
            </button>
          {/if}
        </div>
      </div>
    </div>
  </div>
  
  <!-- Main Content -->
  <div class="main-content-section">
    <div class="container">
      <div class="content-layout">
        <!-- Left Content -->
        <div class="content-main">
          <!-- Tabs Navigation -->
          <nav class="tabs-nav">
            <button 
              class="tab-btn" 
              class:active={activeTab === 'overview'}
              on:click={() => activeTab = 'overview'}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              </svg>
              Descripción
            </button>
            <button 
              class="tab-btn" 
              class:active={activeTab === 'rooms'}
              on:click={() => activeTab = 'rooms'}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
              </svg>
              Habitaciones
            </button>
            <button 
              class="tab-btn" 
              class:active={activeTab === 'reviews'}
              on:click={() => activeTab = 'reviews'}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
              Opiniones ({hotel.reviews})
            </button>
            <button 
              class="tab-btn" 
              class:active={activeTab === 'location'}
              on:click={() => activeTab = 'location'}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="10" r="3"></circle>
                <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
              </svg>
              Ubicación
            </button>
          </nav>
          
          <!-- Tab Content -->
          {#if activeTab === 'overview'}
            <!-- Overview Tab -->
            <section class="content-section">
              <h2 class="section-title">Acerca de {hotel.name}</h2>
              <p class="hotel-long-description">{hotel.longDescription}</p>
              
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-icon">🕒</div>
                  <div class="info-content">
                    <strong>Check-in</strong>
                    <span>{hotel.checkInTime}</span>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-icon">🕐</div>
                  <div class="info-content">
                    <strong>Check-out</strong>
                    <span>{hotel.checkOutTime}</span>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-icon">✅</div>
                  <div class="info-content">
                    <strong>Cancelación</strong>
                    <span class="text-success">{hotel.cancellationPolicy}</span>
                  </div>
                </div>
              </div>
            </section>
            
            <!-- Amenities Section -->
            <section class="content-section">
              <h2 class="section-title">Servicios y Comodidades</h2>
              <div class="amenities-grid">
                {#each hotel.amenities as amenity}
                  <div class="amenity-card">
                    <span class="amenity-icon-large">{amenity.icon}</span>
                    <span class="amenity-name">{amenity.name}</span>
                  </div>
                {/each}
              </div>
            </section>
            
            <!-- Policies Section -->
            <section class="content-section">
              <h2 class="section-title">Políticas del Hotel</h2>
              <ul class="policies-list">
                {#each hotel.policies as policy}
                  <li class="policy-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    {policy}
                  </li>
                {/each}
              </ul>
            </section>
            
          {:else if activeTab === 'rooms'}
            <!-- Rooms Tab -->
            <section class="content-section">
              <h2 class="section-title">Selecciona tu Habitación</h2>
              <p class="section-description">
                Todas las habitaciones incluyen WiFi gratis, aire acondicionado y servicio de limpieza diario.
              </p>
              
              <div class="rooms-list">
                {#each roomTypes as room}
                  <article class="room-detail-card" class:selected={selectedRoom?.id === room.id}>
                    <div class="room-images-section">
                      <img src={room.image} alt={room.name} class="room-main-image" />
                      {#if room.discount > 0}
                        <div class="room-discount-badge">
                          -{room.discount}%
                        </div>
                      {/if}
                      {#if room.available <= 3}
                        <div class="room-urgency-badge">
                          Solo quedan {room.available}
                        </div>
                      {/if}
                    </div>
                    
                    <div class="room-content-section">
                      <div class="room-header">
                        <div>
                          <h3 class="room-name">{room.name}</h3>
                          <p class="room-description">{room.description}</p>
                        </div>
                      </div>
                      
                      <div class="room-specs">
                        <div class="spec-item">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                          </svg>
                          Máx. {room.maxGuests} huéspedes
                        </div>
                        <div class="spec-item">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                          </svg>
                          {room.bedType}
                        </div>
                        <div class="spec-item">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                          </svg>
                          {room.size} m²
                        </div>
                      </div>
                      
                      <div class="room-features-list">
                        {#each room.features.slice(0, 8) as feature}
                          <span class="feature-badge">{feature}</span>
                        {/each}
                        {#if room.features.length > 8}
                          <span class="feature-more">+{room.features.length - 8} más</span>
                        {/if}
                      </div>
                      
                      <div class="room-footer">
                        <div class="room-pricing">
                          {#if room.originalPrice}
                            <div class="original-price-room">{formatPrice(room.originalPrice)}</div>
                          {/if}
                          <div class="current-price-room">
                            <span class="price-amount">{formatPrice(room.pricePerNight)}</span>
                            <span class="price-period">por noche</span>
                          </div>
                          {#if nights > 1}
                            <div class="total-nights-price">
                              {formatPrice(room.pricePerNight * nights)} por {nights} noches
                            </div>
                          {/if}
                        </div>
                        
                        <button 
                          class="btn-select-room"
                          class:selected={selectedRoom?.id === room.id}
                          on:click={() => selectRoom(room)}
                        >
                          {#if selectedRoom?.id === room.id}
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                              <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                            Seleccionada
                          {:else}
                            Seleccionar
                          {/if}
                        </button>
                      </div>
                    </div>
                  </article>
                {/each}
              </div>
            </section>
            
          {:else if activeTab === 'reviews'}
            <!-- Reviews Tab -->
            <section class="content-section">
              <div class="reviews-header">
                <h2 class="section-title">Opiniones de Huéspedes</h2>
                <div class="reviews-summary">
                  <div class="reviews-score-box" style="background: linear-gradient(135deg, {getScoreColor(hotel.guestScore)}, {getScoreColor(hotel.guestScore)}dd);">
                    <div class="score-large">{hotel.guestScore}</div>
                    <div class="score-label">{hotel.guestScoreText}</div>
                  </div>
                  <div class="reviews-breakdown">
                    <p class="reviews-count-text">Basado en {hotel.reviews} opiniones verificadas</p>
                    <div class="rating-bars">
                      <div class="rating-bar-item">
                        <span>Limpieza</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 95%"></div>
                        </div>
                        <span class="rating-score">4.7</span>
                      </div>
                      <div class="rating-bar-item">
                        <span>Confort</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 92%"></div>
                        </div>
                        <span class="rating-score">5</span>
                      </div>
                      <div class="rating-bar-item">
                        <span>Ubicación</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 98%"></div>
                        </div>
                        <span class="rating-score">4.9</span>
                      </div>
                      <div class="rating-bar-item">
                        <span>Personal</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 94%"></div>
                        </div>
                        <span class="rating-score">4.5</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="reviews-list">
                {#each reviews as review}
                  <article class="review-card">
                    <div class="review-header">
                      <div class="reviewer-info">
                        <div class="reviewer-avatar">{review.avatar}</div>
                        <div class="reviewer-details">
                          <strong class="reviewer-name">{review.author}</strong>
                          <span class="reviewer-country">{review.country}</span>
                          {#if review.verifiedStay}
                            <span class="verified-badge">
                              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <polyline points="20 6 9 17 4 12"></polyline>
                              </svg>
                              Estancia verificada
                            </span>
                          {/if}
                        </div>
                      </div>
                      <div class="review-score-box" style="background: {getScoreColor(review.guestScore)}">
                        {review.guestScore}
                      </div>
                    </div>
                    
                    <div class="review-meta">
                      <span>{review.roomType}</span>
                      <span>•</span>
                      <span>{review.stayDuration}</span>
                      <span>•</span>
                      <span>{formatDate(review.date)}</span>
                    </div>
                    
                    {#if review.title}
                      <h4 class="review-title">{review.title}</h4>
                    {/if}
                    
                    <p class="review-text">{review.text}</p>
                    
                    {#if review.pros.length > 0 || review.cons.length > 0}
                      <div class="pros-cons">
                        {#if review.pros.length > 0}
                          <div class="pros">
                            <strong>👍 Pros:</strong>
                            <span>{review.pros.join(' • ')}</span>
                          </div>
                        {/if}
                        {#if review.cons.length > 0}
                          <div class="cons">
                            <strong>👎 Contras:</strong>
                            <span>{review.cons.join(' • ')}</span>
                          </div>
                        {/if}
                      </div>
                    {/if}
                    
                    {#if review.photos && review.photos.length > 0}
                      <div class="review-photos">
                        {#each review.photos as photo}
                          <img src={photo} alt="Foto de huésped" class="review-photo" />
                        {/each}
                      </div>
                    {/if}
                    
                    {#if review.hotelResponse}
                      <div class="hotel-response">
                        <div class="response-header">
                          <strong>🏨 Respuesta de {review.hotelResponse.author}</strong>
                          <span>{formatDate(review.hotelResponse.date)}</span>
                        </div>
                        <p class="response-text">{review.hotelResponse.text}</p>
                      </div>
                    {/if}
                    
                    <div class="review-actions">
                      <button class="review-action-btn">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
                        </svg>
                        Útil ({review.helpful})
                      </button>
                    </div>
                  </article>
                {/each}
              </div>
            </section>
            
          {:else if activeTab === 'location'}
            <!-- Location Tab -->
            <section class="content-section">
              <h2 class="section-title">Ubicación</h2>
              <p class="section-description">{hotel.zoneInfo.description}</p>
              
              <!-- Map Placeholder -->
              <div class="map-container">
                <div class="map-placeholder">
                  <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <circle cx="12" cy="10" r="3"></circle>
                    <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
                  </svg>
                  <p>Mapa interactivo</p>
                  <span>{hotel.address}</span>
                </div>
              </div>
              
              <!-- Nearby Attractions -->
              <div class="nearby-section">
                <h3 class="subsection-title">Lugares de Interés Cercanos</h3>
                <div class="nearby-list">
                  {#each hotel.zoneInfo.nearbyAttractions as attraction}
                    <div class="nearby-item">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="10" r="3"></circle>
                        <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path>
                      </svg>
                      <div class="nearby-info">
                        <strong>{attraction.name}</strong>
                        <span>{attraction.distance} • {attraction.time}</span>
                      </div>
                    </div>
                  {/each}
                </div>
              </div>
              
              <!-- Transportation -->
              <div class="nearby-section">
                <h3 class="subsection-title">Transporte</h3>
                <div class="nearby-list">
                  {#each hotel.zoneInfo.transportation as transport}
                    <div class="nearby-item">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="5" y="11" width="14" height="10" rx="2" ry="2"></rect>
                        <circle cx="9" cy="16" r="1"></circle>
                        <circle cx="15" cy="16" r="1"></circle>
                        <path d="M5 11V6a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v5"></path>
                      </svg>
                      <div class="nearby-info">
                        <strong>{transport.name}</strong>
                        <span>{transport.type} • {transport.distance}</span>
                      </div>
                    </div>
                  {/each}
                </div>
              </div>
            </section>
          {/if}
        </div>
        
        <!-- Right Sidebar - Booking Summary -->
        <aside class="booking-sidebar">
          <div class="booking-summary">
            <h3 class="booking-title">Reserva tu Estancia</h3>
            
            <!-- Date Selection -->
            <div class="booking-section">
              <label class="booking-label">Fechas</label>
              <div class="date-inputs">
                <div class="date-input-group">
                  <label class="input-label">Check-in</label>
                  <input 
                    type="date" 
                    bind:value={checkInDate}
                    min={today.toISOString().split('T')[0]}
                    class="date-input"
                  />
                </div>
                <div class="date-input-group">
                  <label class="input-label">Check-out</label>
                  <input 
                    type="date" 
                    bind:value={checkOutDate}
                    min={checkInDate}
                    class="date-input"
                  />
                </div>
              </div>
              {#if nights > 0}
                <div class="nights-display">
                  {nights} {nights === 1 ? 'noche' : 'noches'}
                </div>
              {/if}
            </div>
            
            <!-- Guests Selection -->
            <div class="booking-section">
              <label class="booking-label">Huéspedes</label>
              <div class="guests-selector">
                <div class="guest-row">
                  <div>
                    <strong>Adultos</strong>
                    <span class="guest-age">18+ años</span>
                  </div>
                  <div class="counter">
                    <button 
                      class="counter-btn" 
                      on:click={() => adults = Math.max(1, adults - 1)}
                      disabled={adults <= 1}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                    </button>
                    <span class="counter-value">{adults}</span>
                    <button 
                      class="counter-btn" 
                      on:click={() => adults = Math.min(10, adults + 1)}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                    </button>
                  </div>
                </div>
                
                <div class="guest-row">
                  <div>
                    <strong>Niños</strong>
                    <span class="guest-age">0-17 años</span>
                  </div>
                  <div class="counter">
                    <button 
                      class="counter-btn" 
                      on:click={() => children = Math.max(0, children - 1)}
                      disabled={children <= 0}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                    </button>
                    <span class="counter-value">{children}</span>
                    <button 
                      class="counter-btn" 
                      on:click={() => children = Math.min(10, children + 1)}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Selected Room Display -->
            {#if selectedRoom}
              <div class="booking-section selected-room-section">
                <label class="booking-label">Habitación Seleccionada</label>
                <div class="selected-room-card">
                  <img src={selectedRoom.image} alt={selectedRoom.name} class="selected-room-image" />
                  <div class="selected-room-info">
                    <strong>{selectedRoom.name}</strong>
                    <span>{formatPrice(selectedRoom.pricePerNight)}/noche</span>
                  </div>
                  <button class="remove-room-btn" on:click={() => selectedRoom = null} title="Quitar selección">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                </div>
                
                <!-- Room Quantity -->
                <div class="room-quantity-selector">
                  <label>Cantidad de habitaciones</label>
                  <div class="counter">
                    <button 
                      class="counter-btn" 
                      on:click={() => selectedRoomQuantity = Math.max(1, selectedRoomQuantity - 1)}
                      disabled={selectedRoomQuantity <= 1}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                    </button>
                    <span class="counter-value">{selectedRoomQuantity}</span>
                    <button 
                      class="counter-btn" 
                      on:click={() => selectedRoomQuantity = Math.min(selectedRoom.available, selectedRoomQuantity + 1)}
                      disabled={selectedRoomQuantity >= selectedRoom.available}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            {:else}
              <div class="no-room-selected">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                </svg>
                <p>Selecciona una habitación para continuar</p>
              </div>
            {/if}
            
            <!-- Price Summary -->
            {#if selectedRoom && nights > 0}
              <div class="price-summary">
                <div class="price-row">
                  <span>{formatPrice(selectedRoom.pricePerNight)} × {nights} {nights === 1 ? 'noche' : 'noches'} × {selectedRoomQuantity}</span>
                  <span>{formatPrice(totalPrice)}</span>
                </div>
                
                {#if totalSavings > 0}
                  <div class="price-row savings">
                    <span>Ahorras</span>
                    <span class="savings-amount">-{formatPrice(totalSavings)}</span>
                  </div>
                {/if}
                
                <div class="price-divider"></div>
                
                <div class="price-row total">
                  <span>Total</span>
                  <span class="total-amount">{formatPrice(totalPrice)}</span>
                </div>
                
                <div class="taxes-note">
                  Incluye impuestos y cargos
                </div>
              </div>
            {/if}
            
            <!-- Booking Actions -->
            <div class="booking-actions">
              <button 
                class="btn-book-now" 
                on:click={bookNow}
                disabled={!selectedRoom}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                Reservar Ahora
              </button>
              
              <button 
                class="btn-add-cart" 
                on:click={addToCart}
                disabled={!selectedRoom}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="9" cy="21" r="1"></circle>
                  <circle cx="20" cy="21" r="1"></circle>
                  <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                </svg>
                Agregar al Carrito
              </button>
            </div>
            
            <!-- Cancellation Info -->
            {#if hotel.freeCancellation}
              <div class="cancellation-info">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                <span>{hotel.cancellationPolicy}</span>
              </div>
            {/if}
            
            <!-- Trust Badges -->
            <div class="trust-badges">
              <div class="trust-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
                <span>Pago Seguro</span>
              </div>
              <div class="trust-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                <span>Mejor Precio Garantizado</span>
              </div>
              <div class="trust-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                <span>Confirmación Instantánea</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
  
  <!-- Full Screen Image Gallery Modal -->
  {#if showImageGallery}
    <div class="gallery-modal" on:click={closeGallery}>
      <button class="gallery-close" on:click={closeGallery}>
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
      
      <button class="gallery-nav-btn gallery-prev" on:click|stopPropagation={prevGalleryImage}>
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>
      
      <button class="gallery-nav-btn gallery-next" on:click|stopPropagation={nextGalleryImage}>
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>
      
      <div class="gallery-content" on:click|stopPropagation>
        <img src={hotel.images[currentImageIndex]} alt="{hotel.name} - Imagen {currentImageIndex + 1}" class="gallery-image" />
        <div class="gallery-counter">
          {currentImageIndex + 1} / {hotel.images.length}
        </div>
      </div>
      
      <div class="gallery-thumbnails-bottom">
        {#each hotel.images as image, i}
          <button 
            class="gallery-thumb-small"
            class:active={i === currentImageIndex}
            on:click|stopPropagation={() => currentImageIndex = i}
          >
            <img src={image} alt="Thumbnail {i + 1}" />
          </button>
        {/each}
      </div>
    </div>
  {/if}
</div>

<style>
  /* Variables */
  :root {
    --primary: #667eea;
    --primary-dark: #5568d3;
    --secondary: #764ba2;
    --success: #10b981;
    --danger: #ef4444;
    --warning: #f59e0b;
    --text-dark: #1e293b;
    --text-medium: #475569;
    --text-light: #64748b;
    --border: #e2e8f0;
    --bg-light: #f8fafc;
    --bg-white: #ffffff;
  }
  
  /* Base */
  .hotel-detail-page {
    background: var(--bg-light);
    min-height: 100vh;
    padding-bottom: 4rem;
  }
  
  .container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 2rem;
  }
  
  /* Breadcrumb */
  .breadcrumb-container {
    background: var(--bg-white);
    border-bottom: 1px solid var(--border);
    padding: 1rem 0;
  }
  
  .breadcrumb {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.9rem;
  }
  
  .breadcrumb-link {
    color: var(--primary);
    background: none;
    border: none;
    cursor: pointer;
    text-decoration: none;
    padding: 0;
    font: inherit;
  }
  
  .breadcrumb-link:hover {
    text-decoration: underline;
  }
  
  .breadcrumb-separator {
    color: var(--text-light);
  }
  
  .breadcrumb-current {
    color: var(--text-medium);
  }
  
  /* Hotel Header */
  .hotel-header-section {
    background: var(--bg-white);
    padding: 2rem 0;
    border-bottom: 1px solid var(--border);
  }
  
  .hotel-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 2rem;
  }
  
  .hotel-title-group {
    flex: 1;
  }
  
  .hotel-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
  }
  
  .badge-sustainable,
  .badge-provider {
    padding: 0.375rem 0.875rem;
    background: rgba(16, 185, 129, 0.1);
    color: var(--success);
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
  }
  
  .badge-provider {
    background: rgba(102, 126, 234, 0.1);
    color: var(--primary);
  }
  
  .hotel-name {
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0 0 0.75rem 0;
    line-height: 1.2;
  }
  
  .hotel-stars-location {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    flex-wrap: wrap;
    font-size: 0.95rem;
    color: var(--text-medium);
  }
  
  .hotel-stars {
    font-size: 1rem;
  }
  
  .property-type {
    color: var(--text-light);
  }
  
  .separator {
    color: var(--text-light);
  }
  
  .hotel-location {
    display: flex;
    align-items: center;
    gap: 0.375rem;
  }
  
  .distance-center {
    color: var(--text-light);
    font-size: 0.875rem;
  }
  
  .hotel-rating-actions {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    align-items: flex-end;
  }
  
  .rating-box-large {
    padding: 1rem 1.5rem;
    border-radius: 12px;
    text-align: center;
    min-width: 110px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  .score-number {
    font-size: 2.5rem;
    font-weight: 700;
    color: white;
    line-height: 1;
    margin-bottom: 0.25rem;
  }
  
  .score-text {
    font-size: 0.85rem;
    font-weight: 600;
    color: white;
    margin-bottom: 0.25rem;
  }
  
  .score-reviews {
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.9);
  }
  
  .header-actions {
    display: flex;
    gap: 0.75rem;
  }
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    border-radius: 10px;
    font-weight: 600;
    font-size: 0.95rem;
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
  }
  
  .action-btn-secondary {
    background: var(--bg-white);
    color: var(--text-dark);
    border: 1.5px solid var(--border);
  }
  
  .action-btn-secondary:hover {
    background: var(--bg-light);
    border-color: var(--primary);
    color: var(--primary);
  }
  
  /* Gallery Preview */
  .gallery-preview-section {
    padding: 1.5rem 0;
  }
  
  .gallery-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 0.75rem;
    border-radius: 16px;
    overflow: hidden;
  }
  
  .gallery-main-image {
    position: relative;
    height: 500px;
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    overflow: hidden;
    border-radius: 12px;
  }
  
  .gallery-main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }
  
  .gallery-main-image:hover img {
    transform: scale(1.05);
  }
  
  .gallery-overlay {
    position: absolute;
    bottom: 1.5rem;
    right: 1.5rem;
    background: rgba(255, 255, 255, 0.95);
    padding: 0.75rem 1.25rem;
    border-radius: 10px;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 600;
    color: var(--text-dark);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.2s ease;
  }
  
  .gallery-main-image:hover .gallery-overlay {
    background: var(--primary);
    color: white;
  }
  
  .gallery-thumbnails {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
  }
  
  .gallery-thumb {
    position: relative;
    height: 160px;
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    overflow: hidden;
    border-radius: 12px;
  }
  
  .gallery-thumb img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }
  
  .gallery-thumb:hover img {
    transform: scale(1.1);
  }
  
  .gallery-more {
    position: relative;
  }
  
  .gallery-more-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    color: white;
    font-weight: 600;
  }
  
  .gallery-more:hover .gallery-more-overlay {
    background: rgba(102, 126, 234, 0.9);
  }
  
  /* Main Content */
  .main-content-section {
    padding: 2rem 0;
  }
  
  .content-layout {
    display: grid;
    grid-template-columns: 1fr 400px;
    gap: 2rem;
    align-items: start;
  }
  
  /* Tabs */
  .tabs-nav {
    display: flex;
    gap: 0.5rem;
    background: var(--bg-white);
    padding: 0.75rem;
    border-radius: 12px;
    margin-bottom: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow-x: auto;
  }
  
  .tab-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    background: transparent;
    border: none;
    border-radius: 8px;
    color: var(--text-medium);
    font-weight: 600;
    font-size: 0.95rem;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
  }
  
  .tab-btn:hover {
    background: var(--bg-light);
    color: var(--primary);
  }
  
  .tab-btn.active {
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
  }
  
  /* Content Sections */
  .content-section {
    background: var(--bg-white);
    padding: 2rem;
    border-radius: 16px;
    margin-bottom: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
  
  .section-title {
    font-size: 1.75rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0 0 1rem 0;
  }
  
  .section-description {
    color: var(--text-medium);
    line-height: 1.7;
    margin-bottom: 1.5rem;
  }
  
  .hotel-long-description {
    color: var(--text-medium);
    line-height: 1.8;
    font-size: 1.05rem;
    margin-bottom: 2rem;
  }
  
  /* Info Grid */
  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
    margin-top: 2rem;
  }
  
  .info-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1.25rem;
    background: var(--bg-light);
    border-radius: 12px;
  }
  
  .info-icon {
    font-size: 2rem;
  }
  
  .info-content {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .info-content strong {
    color: var(--text-dark);
    font-weight: 600;
  }
  
  .info-content span {
    color: var(--text-medium);
    font-size: 0.95rem;
  }
  
  .text-success {
    color: var(--success) !important;
    font-weight: 600 !important;
  }
  
  /* Amenities */
  .amenities-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }
  
  .amenity-card {
    display: flex;
    align-items: center;
    gap: 0.875rem;
    padding: 1rem;
    background: var(--bg-light);
    border-radius: 10px;
    transition: all 0.2s ease;
  }
  
  .amenity-card:hover {
    background: rgba(102, 126, 234, 0.08);
    transform: translateY(-2px);
  }
  
  .amenity-icon-large {
    font-size: 1.75rem;
  }
  
  .amenity-name {
    color: var(--text-dark);
    font-weight: 500;
    font-size: 0.95rem;
  }
  
  /* Policies */
  .policies-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.875rem;
  }
  
  .policy-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    color: var(--text-medium);
    font-size: 0.95rem;
    line-height: 1.6;
  }
  
  .policy-item svg {
    color: var(--success);
    flex-shrink: 0;
  }
  
  /* Rooms List */
  .rooms-list {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }
  
  .room-detail-card {
    display: grid;
    grid-template-columns: 350px 1fr;
    border: 2px solid var(--border);
    border-radius: 16px;
    overflow: hidden;
    transition: all 0.3s ease;
  }
  
  .room-detail-card:hover {
    border-color: var(--primary);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  }
  
  .room-detail-card.selected {
    border-color: var(--primary);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
    background: rgba(102, 126, 234, 0.02);
  }
  
  .room-images-section {
    position: relative;
  }
  
  .room-main-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .room-discount-badge {
    position: absolute;
    top: 1rem;
    left: 1rem;
    background: linear-gradient(135deg, var(--danger), #dc2626);
    color: white;
    padding: 0.5rem 0.875rem;
    border-radius: 8px;
    font-weight: 700;
    font-size: 0.875rem;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
  }
  
  .room-urgency-badge {
    position: absolute;
    bottom: 1rem;
    left: 1rem;
    background: rgba(239, 68, 68, 0.95);
    color: white;
    padding: 0.5rem 0.875rem;
    border-radius: 8px;
    font-weight: 600;
    font-size: 0.85rem;
  }
  
  .room-content-section {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }
  
  .room-name {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0 0 0.5rem 0;
  }
  
  .room-description {
    color: var(--text-medium);
    font-size: 0.95rem;
    margin: 0;
  }
  
  .room-specs {
    display: flex;
    flex-wrap: wrap;
    gap: 1.25rem;
  }
  
  .spec-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-medium);
    font-size: 0.9rem;
  }
  
  .spec-item svg {
    color: var(--primary);
  }
  
  .room-features-list {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .feature-badge {
    padding: 0.375rem 0.875rem;
    background: rgba(102, 126, 234, 0.08);
    color: var(--primary);
    border-radius: 12px;
    font-size: 0.85rem;
    font-weight: 500;
  }
  
  .feature-more {
    padding: 0.375rem 0.875rem;
    background: var(--bg-light);
    color: var(--text-medium);
    border-radius: 12px;
    font-size: 0.85rem;
    font-weight: 600;
  }
  
  .room-footer {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-top: auto;
    padding-top: 1.25rem;
    border-top: 1.5px solid var(--border);
  }
  
  .room-pricing {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .original-price-room {
    color: var(--text-light);
    font-size: 0.95rem;
    text-decoration: line-through;
  }
  
  .current-price-room {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }
  
  .price-amount {
    font-size: 2.25rem;
    font-weight: 700;
    color: var(--text-dark);
    line-height: 1;
  }
  
  .price-period {
    color: var(--text-light);
    font-size: 0.95rem;
  }
  
  .total-nights-price {
    color: var(--text-medium);
    font-size: 0.875rem;
    font-weight: 600;
  }
  
  .btn-select-room {
    padding: 0.875rem 2rem;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
    border: none;
    border-radius: 10px;
    font-weight: 700;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .btn-select-room:hover:not(.selected) {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }
  
  .btn-select-room.selected {
    background: linear-gradient(135deg, var(--success), #059669);
  }
  
  .btn-select-room:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  /* Reviews */
  .reviews-header {
    margin-bottom: 2rem;
  }
  
  .reviews-summary {
    display: grid;
    grid-template-columns: 180px 1fr;
    gap: 2rem;
    margin-top: 1.5rem;
  }
  
  .reviews-score-box {
    padding: 2rem 1.5rem;
    border-radius: 16px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  .score-large {
    font-size: 4rem;
    font-weight: 700;
    color: white;
    line-height: 1;
    margin-bottom: 0.5rem;
  }
  
  .score-label {
    font-size: 1rem;
    font-weight: 600;
    color: white;
  }
  
  .reviews-breakdown {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .reviews-count-text {
    color: var(--text-medium);
    margin: 0;
  }
  
  .rating-bars {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .rating-bar-item {
    display: grid;
    grid-template-columns: 100px 1fr 40px;
    align-items: center;
    gap: 1rem;
  }
  
  .rating-bar {
    height: 8px;
    background: var(--bg-light);
    border-radius: 4px;
    overflow: hidden;
  }
  
  .rating-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--primary), var(--secondary));
    transition: width 0.3s ease;
  }
  
  .rating-score {
    color: var(--text-dark);
    font-weight: 700;
    text-align: right;
  }
  
  .reviews-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .review-card {
    padding: 1.75rem;
    background: var(--bg-light);
    border-radius: 16px;
    transition: all 0.2s ease;
  }
  
  .review-card:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
  
  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1rem;
  }
  
  .reviewer-info {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .reviewer-avatar {
    width: 50px;
    height: 50px;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
  }
  
  .reviewer-details {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .reviewer-name {
    color: var(--text-dark);
    font-size: 1rem;
  }
  
  .reviewer-country {
    color: var(--text-light);
    font-size: 0.875rem;
  }
  
  .verified-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.25rem 0.625rem;
    background: rgba(16, 185, 129, 0.1);
    color: var(--success);
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 600;
  }
  
  .review-score-box {
    padding: 0.5rem 0.875rem;
    border-radius: 8px;
    color: white;
    font-weight: 700;
    font-size: 1.125rem;
  }
  
  .review-meta {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-light);
    font-size: 0.875rem;
    margin-bottom: 1rem;
  }
  
  .review-title {
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0 0 0.75rem 0;
  }
  
  .review-text {
    color: var(--text-medium);
    line-height: 1.7;
    margin: 0 0 1rem 0;
  }
  
  .pros-cons {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-top: 1rem;
  }
  
  .pros,
  .cons {
    display: flex;
    gap: 0.5rem;
    font-size: 0.9rem;
  }
  
  .pros strong {
    color: var(--success);
  }
  
  .cons strong {
    color: var(--danger);
  }
  
  .pros span,
  .cons span {
    color: var(--text-medium);
  }
  
  .review-photos {
    display: flex;
    gap: 0.75rem;
    margin-top: 1rem;
  }
  
  .review-photo {
    width: 120px;
    height: 120px;
    border-radius: 10px;
    object-fit: cover;
    cursor: pointer;
    transition: transform 0.2s ease;
  }
  
  .review-photo:hover {
    transform: scale(1.05);
  }
  
  .hotel-response {
    margin-top: 1rem;
    padding: 1.25rem;
    background: var(--bg-white);
    border-left: 3px solid var(--primary);
    border-radius: 10px;
  }
  
  .response-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
    color: var(--primary);
    font-weight: 600;
  }
  
  .response-header span {
    color: var(--text-light);
    font-size: 0.85rem;
    font-weight: 400;
  }
  
  .response-text {
    color: var(--text-medium);
    line-height: 1.6;
    margin: 0;
  }
  
  .review-actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 1rem;
  }
  
  .review-action-btn {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    padding: 0.5rem 1rem;
    background: transparent;
    border: 1.5px solid var(--border);
    border-radius: 8px;
    color: var(--text-medium);
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .review-action-btn:hover {
    background: var(--bg-light);
    border-color: var(--primary);
    color: var(--primary);
  }
  
  /* Location */
  .map-container {
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 2rem;
  }
  
  .map-placeholder {
    height: 400px;
    background: var(--bg-light);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    color: var(--text-light);
  }
  
  .map-placeholder p {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0;
  }
  
  .map-placeholder span {
    color: var(--text-medium);
  }
  
  .nearby-section {
    margin-top: 2rem;
  }
  
  .subsection-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0 0 1.25rem 0;
  }
  
  .nearby-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .nearby-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: var(--bg-light);
    border-radius: 10px;
  }
  
  .nearby-item svg {
    color: var(--primary);
    flex-shrink: 0;
  }
  
  .nearby-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .nearby-info strong {
    color: var(--text-dark);
    font-weight: 600;
  }
  
  .nearby-info span {
    color: var(--text-medium);
    font-size: 0.9rem;
  }
  
  /* Booking Sidebar */
  .booking-sidebar {
    position: sticky;
    top: 100px;
  }
  
  .booking-summary {
    background: var(--bg-white);
    border: 2px solid var(--border);
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
  
  .booking-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0 0 1.5rem 0;
  }
  
  .booking-section {
    margin-bottom: 1.5rem;
    padding-bottom: 1.5rem;
    border-bottom: 1.5px solid var(--border);
  }
  
  .booking-section:last-of-type {
    border-bottom: none;
  }
  
  .booking-label {
    display: block;
    font-weight: 600;
    color: var(--text-dark);
    margin-bottom: 0.75rem;
  }
  
  .date-inputs {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .date-input-group {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
  }
  
  .input-label {
    font-size: 0.85rem;
    color: var(--text-medium);
    font-weight: 500;
  }
  
  .date-input {
    width: 100%;
    padding: 0.75rem;
    border: 1.5px solid var(--border);
    border-radius: 10px;
    font-size: 1rem;
    font-family: inherit;
    color: var(--text-dark);
    background: var(--bg-white);
    transition: all 0.2s ease;
  }
  
  .date-input:focus {
    outline: none;
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .nights-display {
    margin-top: 0.75rem;
    padding: 0.75rem;
    background: rgba(102, 126, 234, 0.08);
    border-radius: 8px;
    color: var(--primary);
    font-weight: 600;
    text-align: center;
  }
  
  .guests-selector {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .guest-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .guest-row > div:first-child {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .guest-row strong {
    color: var(--text-dark);
    font-weight: 600;
  }
  
  .guest-age {
    color: var(--text-light);
    font-size: 0.85rem;
  }
  
  .counter {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .counter-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
    border: 1.5px solid #cbd5e1;
    border-radius: 8px;
    color: #1e293b;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .counter-btn svg {
    stroke: #1e293b;
    stroke-width: 2.5;
  }
  
  .counter-btn:hover:not(:disabled) {
    background: var(--primary);
    border-color: var(--primary);
    color: white;
  }
  
  .counter-btn:hover:not(:disabled) svg {
    stroke: white;
  }
  
  .counter-btn:disabled {
    opacity: 0.3;
    cursor: not-allowed;
    background: #f1f5f9;
  }
  
  .counter-btn:disabled svg {
    stroke: #94a3b8;
  }
  
  .counter-value {
    min-width: 30px;
    text-align: center;
    font-weight: 600;
    color: var(--text-dark);
  }
  
  .selected-room-section {
    background: rgba(102, 126, 234, 0.03);
    padding: 1.25rem;
    border-radius: 12px;
  }
  
  .selected-room-card {
    display: flex;
    align-items: center;
    gap: 1rem;
    position: relative;
  }
  
  .selected-room-image {
    width: 80px;
    height: 80px;
    border-radius: 10px;
    object-fit: cover;
  }
  
  .selected-room-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .selected-room-info strong {
    color: var(--text-dark);
    font-weight: 600;
  }
  
  .selected-room-info span {
    color: var(--text-medium);
    font-size: 0.9rem;
  }
  
  .remove-room-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
    border: 1.5px solid #cbd5e1;
    border-radius: 8px;
    color: #ef4444;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .remove-room-btn svg {
    stroke: #ef4444;
    stroke-width: 2.5;
  }
  
  .remove-room-btn:hover {
    background: var(--danger);
    border-color: var(--danger);
    color: white;
    transform: scale(1.05);
  }
  
  .remove-room-btn:hover svg {
    stroke: white;
  }
  
  .room-quantity-selector {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--border);
  }
  
  .room-quantity-selector label {
    font-size: 0.9rem;
    color: var(--text-medium);
    font-weight: 500;
  }
  
  .no-room-selected {
    text-align: center;
    padding: 2rem 1rem;
    color: var(--text-light);
  }
  
  .no-room-selected svg {
    margin-bottom: 1rem;
  }
  
  .no-room-selected p {
    margin: 0;
    font-size: 0.95rem;
  }
  
  .price-summary {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
  }
  
  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: var(--text-medium);
    font-size: 0.95rem;
  }
  
  .price-row.savings {
    color: var(--success);
    font-weight: 600;
  }
  
  .savings-amount {
    font-weight: 700;
  }
  
  .price-divider {
    height: 1px;
    background: var(--border);
    margin: 0.5rem 0;
  }
  
  .price-row.total {
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--text-dark);
  }
  
  .total-amount {
    font-size: 1.75rem;
    color: var(--primary);
  }
  
  .taxes-note {
    color: var(--text-light);
    font-size: 0.8rem;
    text-align: center;
    margin-top: 0.5rem;
  }
  
  .booking-actions {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
  }
  
  .btn-book-now,
  .btn-add-cart {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    padding: 1rem 1.5rem;
    border: none;
    border-radius: 12px;
    font-weight: 700;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .btn-book-now {
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
  
  .btn-book-now:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }
  
  .btn-add-cart {
    background: var(--bg-white);
    color: var(--text-dark);
    border: 2px solid var(--border);
  }
  
  .btn-add-cart:hover:not(:disabled) {
    background: var(--bg-light);
    border-color: var(--primary);
    color: var(--primary);
  }
  
  .btn-book-now:disabled,
  .btn-add-cart:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .cancellation-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 1rem;
    background: rgba(16, 185, 129, 0.08);
    border-radius: 10px;
    color: var(--success);
    font-size: 0.875rem;
    font-weight: 500;
    margin-bottom: 1.5rem;
  }
  
  .cancellation-info svg {
    flex-shrink: 0;
  }
  
  .trust-badges {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .trust-badge {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--text-medium);
    font-size: 0.85rem;
  }
  
  .trust-badge svg {
    color: var(--primary);
    flex-shrink: 0;
  }
  
  /* Gallery Modal */
  .gallery-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.95);
    z-index: 10000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    animation: fadeIn 0.3s ease;
  }
  
  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }
  
  .gallery-close {
    position: absolute;
    top: 1.5rem;
    right: 1.5rem;
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    border-radius: 50%;
    color: white;
    cursor: pointer;
    transition: all 0.2s ease;
    z-index: 10;
  }
  
  .gallery-close:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.1);
  }
  
  .gallery-nav-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    border-radius: 50%;
    color: white;
    cursor: pointer;
    transition: all 0.2s ease;
    z-index: 10;
  }
  
  .gallery-nav-btn:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-50%) scale(1.1);
  }
  
  .gallery-prev {
    left: 2rem;
  }
  
  .gallery-next {
    right: 2rem;
  }
  
  .gallery-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    max-width: 90vw;
    max-height: 85vh;
  }
  
  .gallery-image {
    max-width: 100%;
    max-height: calc(85vh - 100px);
    border-radius: 12px;
    object-fit: contain;
  }
  
  .gallery-counter {
    background: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-weight: 600;
  }
  
  .gallery-thumbnails-bottom {
    position: absolute;
    bottom: 2rem;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 0.5rem;
    max-width: 90vw;
    overflow-x: auto;
    padding: 0.5rem;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 12px;
  }
  
  .gallery-thumb-small {
    width: 80px;
    height: 60px;
    border-radius: 6px;
    overflow: hidden;
    border: 2px solid transparent;
    background: none;
    padding: 0;
    cursor: pointer;
    transition: all 0.2s ease;
    flex-shrink: 0;
  }
  
  .gallery-thumb-small img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .gallery-thumb-small:hover {
    border-color: rgba(255, 255, 255, 0.5);
  }
  
  .gallery-thumb-small.active {
    border-color: white;
  }
  
  /* Responsive */
  @media (max-width: 1200px) {
    .content-layout {
      grid-template-columns: 1fr;
    }
    
    .booking-sidebar {
      position: static;
      max-width: 500px;
      margin: 0 auto;
    }
  }
  
  @media (max-width: 992px) {
    .hotel-header {
      flex-direction: column;
    }
    
    .hotel-rating-actions {
      width: 100%;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
    }
    
    .gallery-grid {
      grid-template-columns: 1fr;
    }
    
    .gallery-main-image {
      height: 400px;
    }
    
    .gallery-thumbnails {
      grid-template-columns: repeat(4, 1fr);
    }
    
    .room-detail-card {
      grid-template-columns: 1fr;
    }
    
    .reviews-summary {
      grid-template-columns: 1fr;
    }
  }
  
  @media (max-width: 768px) {
    .container {
      padding: 0 1rem;
    }
    
    .hotel-name {
      font-size: 1.75rem;
    }
    
    .tabs-nav {
      padding: 0.5rem;
    }
    
    .tab-btn {
      padding: 0.625rem 1rem;
      font-size: 0.875rem;
    }
    
    .content-section {
      padding: 1.5rem 1rem;
    }
    
    .booking-summary {
      padding: 1.5rem 1rem;
    }
    
    .gallery-nav-btn {
      width: 44px;
      height: 44px;
    }
    
    .gallery-prev {
      left: 0.5rem;
    }
    
    .gallery-next {
      right: 0.5rem;
    }
  }
</style>