<script>
  export let navigateTo;
  export let hotelId = null;
  import '../styles/hoteldetail.css';
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
    <div class="hdet__container">
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
    <div class="hdet__container">
      <div class="hdet__hotel-header">
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
          
          <h1 class="hdet__hotel-name">{hotel.name}</h1>
          
          <div class="hotel-stars-location">
            <span class="hdet__hotel-stars">{getStars(hotel.stars)}</span>
            <span class="hdet__property-type">{hotel.propertyType}</span>
            <span class="separator">•</span>
            <span class="hdet__hotel-location">
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
            <button class="hdet__action-btn action-btn-secondary" on:click={toggleFavorite} title="Agregar a favoritos">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              Guardar
            </button>
            <button class="hdet__action-btn action-btn-secondary" on:click={shareHotel} title="Compartir">
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
    <div class="hdet__container">
      <div class="gallery-grid">
        <button class="hdet__gallery-main-image" on:click={() => openGallery(0)}>
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
        
        <div class="hdet__gallery-thumbnails">
          {#each hotel.images.slice(1, 5) as image, i}
            <button class="hdet__gallery-thumb" on:click={() => openGallery(i + 1)}>
              <img src={image} alt="{hotel.name} - Imagen {i + 2}" />
            </button>
          {/each}
          {#if hotel.images.length > 5}
            <button class="hdet__gallery-thumb hdet__gallery-more" on:click={() => openGallery(5)}>
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
    <div class="hdet__container">
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
              <h2 class="hdet__section-title">Acerca de {hotel.name}</h2>
              <p class="hotel-long-description">{hotel.longDescription}</p>
              
              <div class="hdet__info-grid">
                <div class="hdet__info-item">
                  <div class="hdet__info-icon">🕒</div>
                  <div class="info-content">
                    <strong>Check-in</strong>
                    <span>{hotel.checkInTime}</span>
                  </div>
                </div>
                <div class="hdet__info-item">
                  <div class="hdet__info-icon">🕐</div>
                  <div class="info-content">
                    <strong>Check-out</strong>
                    <span>{hotel.checkOutTime}</span>
                  </div>
                </div>
                <div class="hdet__info-item">
                  <div class="hdet__info-icon">✅</div>
                  <div class="info-content">
                    <strong>Cancelación</strong>
                    <span class="text-success">{hotel.cancellationPolicy}</span>
                  </div>
                </div>
              </div>
            </section>
            
            <!-- Amenities Section -->
            <section class="content-section">
              <h2 class="hdet__section-title">Servicios y Comodidades</h2>
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
              <h2 class="hdet__section-title">Políticas del Hotel</h2>
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
              <h2 class="hdet__section-title">Selecciona tu Habitación</h2>
              <p class="hdet__section-description">
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
                            <span class="hdet__price-amount">{formatPrice(room.pricePerNight)}</span>
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
                <h2 class="hdet__section-title">Opiniones de Huéspedes</h2>
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
                        <span class="hdet__rating-score">4.7</span>
                      </div>
                      <div class="rating-bar-item">
                        <span>Confort</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 92%"></div>
                        </div>
                        <span class="hdet__rating-score">5</span>
                      </div>
                      <div class="rating-bar-item">
                        <span>Ubicación</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 98%"></div>
                        </div>
                        <span class="hdet__rating-score">4.9</span>
                      </div>
                      <div class="rating-bar-item">
                        <span>Personal</span>
                        <div class="rating-bar">
                          <div class="rating-bar-fill" style="width: 94%"></div>
                        </div>
                        <span class="hdet__rating-score">4.5</span>
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
              <h2 class="hdet__section-title">Ubicación</h2>
              <p class="hdet__section-description">{hotel.zoneInfo.description}</p>
              
              <!-- Map Placeholder -->
              <div class="hdet__map-container">
                <div class="hdet__map-placeholder">
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
                
                <div class="hdet__price-divider"></div>
                
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
              <div class="hdet__cancellation-info">
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