<script>
  import { onMount } from 'svelte';
  import '../styles/hoteldetail.css';

  export let navigateTo = (page, data = null) => {};
  /** @type {any} */
  export let hotel = null;
  /** @type {number} */
  export let cantidadPersonas = 1;
  /** @type {string} */
  export let fechaCheckIn = '';
  /** @type {string} */
  export let fechaCheckOut = '';

  const API = 'http://localhost:7000';

  // ── Fechas ────────────────────────────────────────────────
  let checkInDate  = fechaCheckIn  || '';
  let checkOutDate = fechaCheckOut || '';

  function toLocalDateStr(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  onMount(() => {
    if (!checkInDate || !checkOutDate) {
      const today    = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const dayAfter = new Date(tomorrow);
      dayAfter.setDate(dayAfter.getDate() + 1);
      checkInDate  = toLocalDateStr(tomorrow);
      checkOutDate = toLocalDateStr(dayAfter);
    }
  });

  const todayStr = toLocalDateStr(new Date());

  $: nights = (() => {
    if (!checkInDate || !checkOutDate) return 0;
    return Math.max(0, Math.ceil(
      (Number(new Date(checkOutDate)) - Number(new Date(checkInDate))) / 86400000
    ));
  })();

  // ── Habitaciones — el backend ya las filtra por capacidad ─
  $: habitacionesDisponibles = hotel?.habitaciones || [];

  // ── UI state ─────────────────────────────────────────────
  let activeTab         = 'overview';
  let selectedRoom      = null;
  let currentImageIndex = 0;
  let showImageGallery  = false;

  $: totalPrice = selectedRoom ? (selectedRoom.precioPorNoche + selectedRoom.precioPorPersona * cantidadPersonas) * nights : 0;

  // ── Imágenes ─────────────────────────────────────────────
  // Cada entidad trae imagenesIds: [1, 2, 3...] con los IDs reales de sus imágenes.
  // Las URLs se construyen: /imagenes/hotel/{id_imagen}, /imagenes/habitacion/{id_imagen}, etc.
  // Ejemplo: imagenesIds: [1, 2, 3] genera:
  //   <img src="http://localhost:7000/imagenes/hotel/1" />
  //   <img src="http://localhost:7000/imagenes/hotel/2" />
  //   <img src="http://localhost:7000/imagenes/hotel/3" />
  $: images = (() => {
    if (!hotel) return [];
    const imgs = [];

    // Imágenes del hotel (pueden ser varias)
    if (hotel.imagenesIds?.length > 0) {
      for (const imgId of hotel.imagenesIds) {
        imgs.push(`${API}/imagenes/hotel/${imgId}`);
      }
    }

    // Imágenes de cada habitación (cada una puede tener varias)
    if (hotel.habitaciones?.length > 0) {
      for (const room of hotel.habitaciones) {
        if (room.imagenesIds?.length > 0) {
          for (const imgId of room.imagenesIds) {
            imgs.push(`${API}/imagenes/habitacion/${imgId}`);
          }
        }
      }
    }

    // Imágenes de cada amenidad (cada una puede tener varias)
    if (hotel.amenidades?.length > 0) {
      for (const am of hotel.amenidades) {
        if (am.imagenesIds?.length > 0) {
          for (const imgId of am.imagenesIds) {
            imgs.push(`${API}/imagenes/amenidad/${imgId}`);
          }
        }
      }
    }

    return imgs;
  })();

  // Primera imagen de una habitación (para la tarjeta), o null si no tiene
  function roomImage(room) {
    if (room.imagenesIds?.length > 0) {
      return `${API}/imagenes/habitacion/${room.imagenesIds[0]}`;
    }
    return null;
  }

  // Todas las imágenes de una habitación
  function roomImages(room) {
    return (room.imagenesIds || []).map(id => `${API}/imagenes/habitacion/${id}`);
  }

  // Primera imagen de una amenidad, o null si no tiene
  function amenityImage(amenidad) {
    if (amenidad.imagenesIds?.length > 0) {
      return `${API}/imagenes/amenidad/${amenidad.imagenesIds[0]}`;
    }
    return null;
  }

  // ── Iconos de amenidades ─────────────────────────────────
  const amenityIcons = {
    'wifi':            '📶',
    'piscina':         '🏊',
    'gimnasio':        '💪',
    'estacionamiento': '🅿️',
    'restaurante':     '🍽️',
    'spa':             '💆',
    'bar':             '🍹',
    'desayuno':        '🍳',
    'default':         '✨',
  };

  function getAmenityIcon(nombre) {
    const key = nombre.toLowerCase();
    for (const k of Object.keys(amenityIcons)) {
      if (key.includes(k)) return amenityIcons[k];
    }
    return amenityIcons.default;
  }

  // ── Gallery ───────────────────────────────────────────────
  function openGallery(index = 0) {
    currentImageIndex = index;
    showImageGallery  = true;
    document.body.style.overflow = 'hidden';
  }
  function closeGallery() {
    showImageGallery = false;
    document.body.style.overflow = 'auto';
  }
  function nextImage() { currentImageIndex = (currentImageIndex + 1) % images.length; }
  function prevImage() { currentImageIndex = (currentImageIndex - 1 + images.length) % images.length; }

  // ── Selección habitación ──────────────────────────────────
  function selectRoom(room) {
    selectedRoom = room;
    document.querySelector('.booking-summary')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  let booking      = false;
  let bookError    = '';
  let reservacion  = null;

  async function bookNow() {
    if (!selectedRoom) return;
    if (!checkInDate || !checkOutDate) {
      bookError = 'Por favor selecciona las fechas de check-in y check-out.';
      return;
    }
    if (new Date(checkOutDate) <= new Date(checkInDate)) {
      bookError = 'La reservación es por noche — el check-out debe ser al menos un día después del check-in.';
      return;
    }
    bookError   = '';
    booking     = true;
    reservacion = null;
    try {
      const res = await fetch(`${API}/reservaciones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          habitaciones: [{
            habitacionId:    selectedRoom.id,
            cantidadPersonas,
            fechaCheckIn:    checkInDate,
            fechaCheckOut:   checkOutDate,
          }]
        }),
      });
      if (!res.ok) {
        let msg = 'No se pudo completar la reservación. Intenta de nuevo.';
        try {
          const data = await res.json();
          if (data.mensaje) msg = data.mensaje;
          else if (data.message) msg = data.message;
          else if (data.error) msg = data.error;
        } catch(_) {}
        throw new Error(msg);
      }
      reservacion = await res.json();
    } catch(e) {
      bookError = e.message || 'Error al crear la reservación';
    } finally {
      booking = false;
    }
  }

  // ── Helpers ───────────────────────────────────────────────
  function fmt(p) {
    return new Intl.NumberFormat('es-GT', {
      style: 'currency', currency: 'USD', minimumFractionDigits: 0
    }).format(p);
  }

  function toggleFavorite() { alert('Hotel guardado en favoritos'); }
  function shareHotel() {
    navigator.clipboard?.writeText(window.location.href);
    alert('Enlace copiado al portapapeles');
  }

  // Manejo de errores de imagen (fallback si el endpoint no tiene imagen)
  function handleImgError(e) {
    e.target.style.display = 'none';
  }
</script>

<svelte:window on:keydown={(e) => {
  if (!showImageGallery) return;
  if (e.key === 'Escape')     closeGallery();
  if (e.key === 'ArrowLeft')  prevImage();
  if (e.key === 'ArrowRight') nextImage();
}} />

{#if !hotel}
  <div class="hdet__no-hotel">
    <p>No se encontró información del hotel.</p>
    <button on:click={() => navigateTo('home')}>Volver al inicio</button>
  </div>
{:else}
<div class="hotel-detail-page">

  <!-- Breadcrumb -->
  <div class="breadcrumb-container">
    <div class="hdet__container">
      <nav class="breadcrumb">
        <button class="breadcrumb-link" on:click={() => navigateTo('home')}>Inicio</button>
        <span class="breadcrumb-separator">/</span>
        <button class="breadcrumb-link" on:click={() => navigateTo('search-results')}>Búsqueda</button>
        <span class="breadcrumb-separator">/</span>
        <span class="breadcrumb-current">{hotel.nombre}</span>
      </nav>
    </div>
  </div>

  <!-- Hotel Header -->
  <div class="hotel-header-section">
    <div class="hdet__container">
      <div class="hdet__hotel-header">
        <div class="hotel-title-group">
          <h1 class="hdet__hotel-name">{hotel.nombre}</h1>
          <div class="hotel-stars-location">
            <span class="hdet__hotel-location">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="10" r="3"></circle><path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path></svg>
              {hotel.direccion}
            </span>
            <span class="separator">•</span>
            <span class="hdet__property-type">{hotel.ciudad}, {hotel.pais}</span>
          </div>
        </div>

        <div class="hotel-rating-actions">
          <div class="rating-box-large" style="background: linear-gradient(135deg, #3b82f6, #1d4ed8);">
            <div class="score-number">{hotel.rating}</div>
            <div class="score-text">{hotel.rating >= 4.8 ? 'Excepcional' : hotel.rating >= 4.5 ? 'Fabuloso' : 'Muy bueno'}</div>
          </div>
          <div class="header-actions">
            <button class="hdet__action-btn action-btn-secondary" on:click={toggleFavorite}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
              Guardar
            </button>
            <button class="hdet__action-btn action-btn-secondary" on:click={shareHotel}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
              Compartir
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Gallery -->
  {#if images.length > 0}
  <div class="gallery-preview-section">
    <div class="hdet__container">
      <div class="gallery-grid">
        <button class="hdet__gallery-main-image" on:click={() => openGallery(0)} aria-label="Ver todas las fotos">
          <img src={images[0]} alt={hotel.nombre} on:error={handleImgError} />
          <div class="gallery-overlay" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
            Ver todas las fotos
          </div>
        </button>
        <div class="hdet__gallery-thumbnails">
          {#each images.slice(1, 5) as img, i}
            <button class="hdet__gallery-thumb" on:click={() => openGallery(i + 1)} aria-label="Ver imagen {i + 2}">
              <img src={img} alt="{hotel.nombre} {i + 2}" on:error={handleImgError} />
            </button>
          {/each}
        </div>
      </div>
    </div>
  </div>
  {/if}

  <!-- Main Content -->
  <div class="main-content-section">
    <div class="hdet__container">
      <div class="content-layout">

        <!-- LEFT -->
        <div class="content-main">
          <nav class="tabs-nav">
            {#each [
              { id: 'overview', label: 'Descripción' },
              { id: 'rooms',    label: 'Habitaciones' },
              { id: 'location', label: 'Ubicación'   },
            ] as tab}
              <button class="tab-btn" class:active={activeTab === tab.id} on:click={() => activeTab = tab.id}>
                {tab.label}
              </button>
            {/each}
          </nav>

          <!-- Descripción -->
          {#if activeTab === 'overview'}
            <section class="content-section">
              <h2 class="hdet__section-title">Acerca de {hotel.nombre}</h2>
              <p class="hotel-long-description">{hotel.descripcion}</p>
            </section>

            {#if hotel.amenidades?.length > 0}
              <section class="content-section">
                <h2 class="hdet__section-title">Servicios y Comodidades</h2>
                <div class="amenities-grid">
                  {#each hotel.amenidades as am}
                    <div class="amenity-card">
                      {#if am.imagenesIds?.length > 0}
                        <img src={amenityImage(am)} alt={am.nombre} class="amenity-image" on:error={(e) => { e.target.style.display = 'none'; }} />
                      {:else}
                        <span class="amenity-icon-large" aria-hidden="true">{getAmenityIcon(am.nombre)}</span>
                      {/if}
                      <div class="amenity-info">
                        <span class="amenity-name">{am.nombre}</span>
                        {#if am.descripcion}
                          <span class="amenity-desc">{am.descripcion}</span>
                        {/if}
                      </div>
                    </div>
                  {/each}
                </div>
              </section>
            {/if}

          <!-- Habitaciones -->
          {:else if activeTab === 'rooms'}
            <section class="content-section">
              <h2 class="hdet__section-title">Habitaciones Disponibles</h2>

              {#if habitacionesDisponibles.length === 0}
                <div class="hdet__no-rooms">
                  <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                  <p>Este hotel no tiene habitaciones disponibles para tu búsqueda.</p>
                  <span>Intenta con otras fechas o un número diferente de huéspedes.</span>
                </div>
              {:else}
                <div class="rooms-list">
                  {#each habitacionesDisponibles as room, idx}
                    <article class="room-detail-card" class:selected={selectedRoom?.id === room.id}>
                      <div class="room-images-section">
                        {#if room.imagenesIds?.length > 0}
                          <img src={roomImage(room)} alt={room.tipoHabitacion} class="room-main-image" on:error={(e) => { e.target.parentElement.innerHTML = `<div class="room-no-image"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg><span>Sin imagen disponible</span></div>`; }} />
                        {:else}
                          <div class="room-no-image">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                            <span>Sin imagen disponible</span>
                          </div>
                        {/if}
                        <div class="room-capacity-badge">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle></svg>
                          Máx. {room.capacidadMaxima}
                        </div>
                      </div>

                      <div class="room-content-section">
                        <div>
                          <h3 class="room-name">{room.tipoHabitacion}</h3>
                          <p class="room-description">{room.descripcion}</p>
                        </div>

                        <div class="room-specs">
                          <div class="spec-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                            {room.tipoCama}
                          </div>
                          <div class="spec-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>
                            {room.metrosCuadrados} m²
                          </div>
                          <div class="spec-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                            Máx. {room.capacidadMaxima} huéspedes
                          </div>
                        </div>

                        <div class="room-footer">
                          <div class="room-pricing">
                            <div class="current-price-room">
                              <span class="hdet__price-amount">{fmt(room.precioPorNoche)}</span>
                              <span class="price-period">/ noche</span>
                            </div>
                            <div class="price-per-person">{fmt(room.precioPorPersona)} por persona</div>
                            {#if nights > 1}
                              <div class="total-nights-price">{fmt(room.precioPorNoche * nights)} por {nights} {nights === 1 ? 'noche' : 'noches'}</div>
                            {/if}
                          </div>

                          <button
                            class="btn-select-room"
                            class:selected={selectedRoom?.id === room.id}
                            on:click={() => selectRoom(room)}
                          >
                            {#if selectedRoom?.id === room.id}
                              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg>
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
              {/if}
            </section>

          <!-- Ubicación -->
          {:else if activeTab === 'location'}
            <section class="content-section">
              <h2 class="hdet__section-title">Ubicación</h2>
              <p class="hdet__section-description">{hotel.direccion} — {hotel.ciudad}, {hotel.pais}</p>
              <div class="hdet__map-container">
                <div class="hdet__map-placeholder">
                  <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="10" r="3"></circle><path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"></path></svg>
                  <p>{hotel.nombre}</p>
                  <span>{hotel.direccion}</span>
                </div>
              </div>
            </section>
          {/if}
        </div>

        <!-- SIDEBAR -->
        <aside class="booking-sidebar">
          <div class="booking-summary">
            <h3 class="booking-title">Reserva tu Estancia</h3>

            <div class="booking-section">
              <p class="booking-label">Fechas</p>
              <div class="date-inputs">
                <div class="date-input-group">
                  <span class="input-label">Check-in</span>
                  <input type="date" bind:value={checkInDate} min={todayStr} class="date-input" />
                </div>
                <div class="date-input-group">
                  <span class="input-label">Check-out</span>
                  <input type="date" bind:value={checkOutDate} min={checkInDate ? (() => { const d = new Date(checkInDate); d.setDate(d.getDate()+1); return toLocalDateStr(d); })() : ''} class="date-input" />
                </div>
              </div>
              {#if nights > 0}
                <div class="nights-display">{nights} {nights === 1 ? 'noche' : 'noches'}</div>
              {/if}
            </div>

            <div class="booking-section">
              <p class="booking-label">Huéspedes</p>
              <div class="hdet__guests-display">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                <span>{cantidadPersonas} {cantidadPersonas === 1 ? 'huésped' : 'huéspedes'}</span>
              </div>
            </div>

            {#if selectedRoom}
              <div class="booking-section selected-room-section">
                <p class="booking-label">Habitación Seleccionada</p>
                <div class="selected-room-card">
                  <div class="selected-room-info">
                    <strong>{selectedRoom.tipoHabitacion}</strong>
                    <span>{fmt(selectedRoom.precioPorNoche)}/noche</span>
                  </div>
                  <button class="remove-room-btn" on:click={() => selectedRoom = null} aria-label="Quitar">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                  </button>
                </div>
              </div>
            {:else}
              <div class="no-room-selected">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                <p>Selecciona una habitación para continuar</p>
              </div>
            {/if}

            {#if selectedRoom && nights > 0}
              <div class="price-summary">
                <div class="price-row">
                  <span>{fmt(selectedRoom.precioPorNoche)}/noche × {nights} {nights === 1 ? 'noche' : 'noches'}</span>
                  <span>{fmt(selectedRoom.precioPorNoche * nights)}</span>
                </div>
                <div class="price-row">
                  <span>{fmt(selectedRoom.precioPorPersona)}/persona × {cantidadPersonas} × {nights} {nights === 1 ? 'noche' : 'noches'}</span>
                  <span>{fmt(selectedRoom.precioPorPersona * cantidadPersonas * nights)}</span>
                </div>
                <div class="hdet__price-divider"></div>
                <div class="price-row total">
                  <span>Total</span>
                  <span class="total-amount">{fmt(totalPrice)}</span>
                </div>
                <div class="taxes-note">Incluye impuestos y cargos</div>
              </div>
            {/if}

            {#if bookError}
              <div class="hdet__book-notice">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {bookError}
              </div>
            {/if}

            <div class="booking-actions">
              <button class="btn-book-now" on:click={bookNow} disabled={!selectedRoom || booking}>
                {booking ? 'Procesando...' : 'Reservar Ahora'}
              </button>
            </div>

            <div class="trust-badges">
              <div class="trust-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                <span>Pago Seguro</span>
              </div>
              <div class="trust-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>
                <span>Mejor Precio Garantizado</span>
              </div>
            </div>
          </div>
        </aside>

      </div>
    </div>

  <!-- Modal confirmación reservación -->
  {#if reservacion}
    <div class="hdet__confirm-overlay" role="dialog" aria-modal="true">
      <div class="hdet__confirm-modal">
        <div class="hdet__confirm-icon">✓</div>
        <h2 class="hdet__confirm-title">¡Reservación confirmada!</h2>
        <p class="hdet__confirm-code">{reservacion.noReservacion}</p>
        <div class="hdet__confirm-rows">
          <div class="hdet__confirm-row">
            <span>Hotel</span>
            <strong>{hotel?.nombre}</strong>
          </div>
          <div class="hdet__confirm-row">
            <span>Habitación</span>
            <strong>{selectedRoom?.tipoHabitacion}</strong>
          </div>
          <div class="hdet__confirm-row">
            <span>Check-in</span>
            <strong>{checkInDate}</strong>
          </div>
          <div class="hdet__confirm-row">
            <span>Check-out</span>
            <strong>{checkOutDate}</strong>
          </div>
          <div class="hdet__confirm-row">
            <span>Huéspedes</span>
            <strong>{cantidadPersonas}</strong>
          </div>
          <div class="hdet__confirm-row">
            <span>Estado</span>
            <strong class="hdet__confirm-estado">{reservacion.estado}</strong>
          </div>
          <div class="hdet__confirm-row hdet__confirm-row--total">
            <span>Total</span>
            <strong>{fmt(reservacion.total)}</strong>
          </div>
        </div>
        <p class="hdet__confirm-expira">Expira: {reservacion.fechaExpiracion}</p>
        <div class="hdet__confirm-btns">
          <button class="hdet__confirm-btn-home" on:click={() => navigateTo('home')}>Volver al inicio</button>
          <button class="hdet__confirm-btn-close" on:click={() => reservacion = null}>Cerrar</button>
        </div>
      </div>
    </div>
  {/if}
  </div>

  <!-- Gallery Modal -->
  {#if showImageGallery && images.length > 0}
    <div class="gallery-modal" role="dialog" aria-modal="true" aria-label="Galería de fotos">
      <button class="gallery-close" on:click={closeGallery} aria-label="Cerrar galería">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
      </button>
      <button class="gallery-nav-btn gallery-prev" on:click={prevImage} aria-label="Anterior">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </button>
      <button class="gallery-nav-btn gallery-next" on:click={nextImage} aria-label="Siguiente">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"></polyline></svg>
      </button>
      <div class="gallery-content">
        <img src={images[currentImageIndex]} alt="{hotel.nombre} {currentImageIndex + 1}" class="gallery-image" on:error={handleImgError} />
        <div class="gallery-counter">{currentImageIndex + 1} / {images.length}</div>
      </div>
    </div>
  {/if}

</div>
{/if}