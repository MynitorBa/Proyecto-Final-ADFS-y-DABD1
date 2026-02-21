<script>
import '../styles/myreservations.css';
  // Datos de ejemplo de reservaciones
  let reservations = [
    {
      id: "MIKU-12345678",
      status: "confirmed",
      hotelName: "Miku Inn París Centro",
      roomType: "Suite",
      image: "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=300",
      checkIn: "2026-02-15",
      checkOut: "2026-02-18",
      nights: 3,
      guests: 2,
      totalPrice: 660,
      bookingDate: "2026-01-20"
    },
    {
      id: "MIKU-87654321",
      status: "completed",
      hotelName: "Miku Inn Torre Eiffel",
      roomType: "Gran Suite",
      image: "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=300",
      checkIn: "2026-01-10",
      checkOut: "2026-01-14",
      nights: 4,
      guests: 3,
      totalPrice: 880,
      bookingDate: "2025-12-15"
    },
    {
      id: "MIKU-11223344",
      status: "cancelled",
      hotelName: "Miku Inn Champs-Élysées",
      roomType: "Junior Suite",
      image: "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=300",
      checkIn: "2026-03-20",
      checkOut: "2026-03-25",
      nights: 5,
      guests: 2,
      totalPrice: 825,
      bookingDate: "2026-01-18",
      cancellationDate: "2026-01-22"
    }
  ];
  
  let selectedFilter = 'all';
  let searchQuery = '';
  
  $: filteredReservations = reservations.filter(r => {
    const matchesFilter = selectedFilter === 'all' || r.status === selectedFilter;
    const matchesSearch = r.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         r.hotelName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesFilter && matchesSearch;
  });
  
  function getStatusBadge(status) {
    const badges = {
      confirmed: { text: 'Confirmada', class: 'status-confirmed', icon: '✓' },
      completed: { text: 'Completada', class: 'status-completed', icon: '✓' },
      cancelled: { text: 'Cancelada', class: 'status-cancelled', icon: '✕' }
    };
    return badges[status] || badges.confirmed;
  }
  
  function viewDetails(reservationId) {
    console.log('Ver detalles de:', reservationId);
  }
  
  function downloadReceipt(reservationId) {
    console.log('Descargar comprobante:', reservationId);
  }
  
  function cancelReservation(reservationId) {
    if (confirm('¿Estás seguro de que deseas cancelar esta reserva?')) {
      console.log('Cancelando reserva:', reservationId);
      // Aquí iría la lógica de cancelación
    }
  }
</script>

<div class="reservations-container">
  <div class="myres__container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1>Mis Reservas</h1>
        <p class="myres__subtitle">Gestiona todas tus reservaciones en un solo lugar</p>
      </div>
      
      <button class="btn-new-booking" on:click={() => window.location.href = '#/'}>
        + Nueva Reserva
      </button>
    </div>
    
    <!-- Filters and Search -->
    <div class="controls">
      <div class="filters">
        <button
          class="myres__filter-btn"
          class:active={selectedFilter === 'all'}
          on:click={() => selectedFilter = 'all'}
        >
          Todas
        </button>
        <button
          class="myres__filter-btn"
          class:active={selectedFilter === 'confirmed'}
          on:click={() => selectedFilter = 'confirmed'}
        >
          Confirmadas
        </button>
        <button
          class="myres__filter-btn"
          class:active={selectedFilter === 'completed'}
          on:click={() => selectedFilter = 'completed'}
        >
          Completadas
        </button>
        <button
          class="myres__filter-btn"
          class:active={selectedFilter === 'cancelled'}
          on:click={() => selectedFilter = 'cancelled'}
        >
          Canceladas
        </button>
      </div>
      
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input
          type="text"
          bind:value={searchQuery}
          placeholder="Buscar por código o nombre de hotel..."
        />
      </div>
    </div>
    
    <!-- Reservations List -->
    <div class="reservations-list">
      {#if filteredReservations.length === 0}
        <div class="myres__no-results">
          <div class="myres__no-results-icon">📋</div>
          <h2>No se encontraron reservas</h2>
          <p>Intenta ajustar tus filtros o realiza una nueva búsqueda</p>
        </div>
      {:else}
        {#each filteredReservations as reservation}
          <div class="reservation-card">
            <div class="reservation-image">
              <img src={reservation.image} alt={reservation.hotelName} />
              <div class="status-badge {getStatusBadge(reservation.status).class}">
                {getStatusBadge(reservation.status).icon} {getStatusBadge(reservation.status).text}
              </div>
            </div>
            
            <div class="reservation-info">
              <div class="myres__info-header">
                <div>
                  <h3>{reservation.hotelName}</h3>
                  <p class="room-type">{reservation.roomType}</p>
                </div>
                <div class="reservation-code">
                  <small>Código de Reserva</small>
                  <strong>{reservation.id}</strong>
                </div>
              </div>
              
              <div class="myres__info-grid">
                <div class="myres__info-item">
                  <span class="myres__info-label">📅 Check-in</span>
                  <span class="myres__info-value">{reservation.checkIn}</span>
                </div>
                
                <div class="myres__info-item">
                  <span class="myres__info-label">📅 Check-out</span>
                  <span class="myres__info-value">{reservation.checkOut}</span>
                </div>
                
                <div class="myres__info-item">
                  <span class="myres__info-label">🌙 Noches</span>
                  <span class="myres__info-value">{reservation.nights}</span>
                </div>
                
                <div class="myres__info-item">
                  <span class="myres__info-label">👥 Huéspedes</span>
                  <span class="myres__info-value">{reservation.guests}</span>
                </div>
                
                <div class="myres__info-item">
                  <span class="myres__info-label">💰 Total Pagado</span>
                  <span class="myres__info-value myres__price">${reservation.totalPrice}</span>
                </div>
                
                <div class="myres__info-item">
                  <span class="myres__info-label">📆 Fecha de Reserva</span>
                  <span class="myres__info-value">{reservation.bookingDate}</span>
                </div>
              </div>
              
              {#if reservation.cancellationDate}
                <div class="myres__cancellation-info">
                  ⚠️ Cancelada el {reservation.cancellationDate}
                </div>
              {/if}
            </div>
            
            <div class="reservation-actions">
              <button class="myres__action-btn btn-details" on:click={() => viewDetails(reservation.id)}>
                Ver Detalles
              </button>
              
              <button class="myres__action-btn btn-download" on:click={() => downloadReceipt(reservation.id)}>
                📄 Descargar
              </button>
              
              {#if reservation.status === 'confirmed'}
                <button
                  class="myres__action-btn btn-cancel"
                  on:click={() => cancelReservation(reservation.id)}
                >
                  Cancelar Reserva
                </button>
              {/if}
            </div>
          </div>
        {/each}
      {/if}
    </div>
  </div>
</div>