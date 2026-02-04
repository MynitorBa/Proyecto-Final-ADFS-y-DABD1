<script>
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
  <div class="container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1>Mis Reservas</h1>
        <p class="subtitle">Gestiona todas tus reservaciones en un solo lugar</p>
      </div>
      
      <button class="btn-new-booking" on:click={() => window.location.href = '#/'}>
        + Nueva Reserva
      </button>
    </div>
    
    <!-- Filters and Search -->
    <div class="controls">
      <div class="filters">
        <button
          class="filter-btn"
          class:active={selectedFilter === 'all'}
          on:click={() => selectedFilter = 'all'}
        >
          Todas
        </button>
        <button
          class="filter-btn"
          class:active={selectedFilter === 'confirmed'}
          on:click={() => selectedFilter = 'confirmed'}
        >
          Confirmadas
        </button>
        <button
          class="filter-btn"
          class:active={selectedFilter === 'completed'}
          on:click={() => selectedFilter = 'completed'}
        >
          Completadas
        </button>
        <button
          class="filter-btn"
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
        <div class="no-results">
          <div class="no-results-icon">📋</div>
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
              <div class="info-header">
                <div>
                  <h3>{reservation.hotelName}</h3>
                  <p class="room-type">{reservation.roomType}</p>
                </div>
                <div class="reservation-code">
                  <small>Código de Reserva</small>
                  <strong>{reservation.id}</strong>
                </div>
              </div>
              
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">📅 Check-in</span>
                  <span class="info-value">{reservation.checkIn}</span>
                </div>
                
                <div class="info-item">
                  <span class="info-label">📅 Check-out</span>
                  <span class="info-value">{reservation.checkOut}</span>
                </div>
                
                <div class="info-item">
                  <span class="info-label">🌙 Noches</span>
                  <span class="info-value">{reservation.nights}</span>
                </div>
                
                <div class="info-item">
                  <span class="info-label">👥 Huéspedes</span>
                  <span class="info-value">{reservation.guests}</span>
                </div>
                
                <div class="info-item">
                  <span class="info-label">💰 Total Pagado</span>
                  <span class="info-value price">${reservation.totalPrice}</span>
                </div>
                
                <div class="info-item">
                  <span class="info-label">📆 Fecha de Reserva</span>
                  <span class="info-value">{reservation.bookingDate}</span>
                </div>
              </div>
              
              {#if reservation.cancellationDate}
                <div class="cancellation-info">
                  ⚠️ Cancelada el {reservation.cancellationDate}
                </div>
              {/if}
            </div>
            
            <div class="reservation-actions">
              <button class="action-btn btn-details" on:click={() => viewDetails(reservation.id)}>
                Ver Detalles
              </button>
              
              <button class="action-btn btn-download" on:click={() => downloadReceipt(reservation.id)}>
                📄 Descargar
              </button>
              
              {#if reservation.status === 'confirmed'}
                <button
                  class="action-btn btn-cancel"
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

<style>
  .reservations-container {
    min-height: 100vh;
    background: #f8f9fa;
    padding: 3rem 0;
  }
  
  .container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 2rem;
  }
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
  }
  
  .page-header h1 {
    color: #333;
    font-size: 2.5rem;
    margin-bottom: 0.5rem;
  }
  
  .subtitle {
    color: #666;
    font-size: 1.05rem;
  }
  
  .btn-new-booking {
    padding: 0.875rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    font-size: 1.05rem;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .btn-new-booking:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  .controls {
    display: flex;
    justify-content: space-between;
    gap: 2rem;
    margin-bottom: 2rem;
    flex-wrap: wrap;
  }
  
  .filters {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .filter-btn {
    padding: 0.75rem 1.5rem;
    border: 2px solid #e0e0e0;
    background: white;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    color: #555;
    transition: all 0.2s ease;
  }
  
  .filter-btn:hover {
    border-color: #667eea;
    background: #f0f2ff;
  }
  
  .filter-btn.active {
    border-color: #667eea;
    background: #667eea;
    color: white;
  }
  
  .search-box {
    position: relative;
    flex: 1;
    max-width: 400px;
  }
  
  .search-icon {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    font-size: 1.2rem;
  }
  
  .search-box input {
    width: 100%;
    padding: 0.875rem 1rem 0.875rem 3rem;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
  }
  
  .search-box input:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .reservations-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .reservation-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: grid;
    grid-template-columns: 250px 1fr auto;
    transition: all 0.3s ease;
  }
  
  .reservation-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }
  
  .reservation-image {
    position: relative;
  }
  
  .reservation-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .status-badge {
    position: absolute;
    top: 1rem;
    left: 1rem;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
    backdrop-filter: blur(10px);
  }
  
  .status-confirmed {
    background: rgba(76, 175, 80, 0.95);
    color: white;
  }
  
  .status-completed {
    background: rgba(33, 150, 243, 0.95);
    color: white;
  }
  
  .status-cancelled {
    background: rgba(244, 67, 54, 0.95);
    color: white;
  }
  
  .reservation-info {
    padding: 1.5rem;
  }
  
  .info-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1.5rem;
  }
  
  .info-header h3 {
    color: #333;
    font-size: 1.5rem;
    margin-bottom: 0.25rem;
  }
  
  .room-type {
    color: #667eea;
    font-weight: 600;
    font-size: 0.95rem;
  }
  
  .reservation-code {
    text-align: right;
  }
  
  .reservation-code small {
    display: block;
    color: #888;
    font-size: 0.85rem;
    margin-bottom: 0.25rem;
  }
  
  .reservation-code strong {
    color: #333;
    font-family: monospace;
    font-size: 1.05rem;
  }
  
  .info-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
  }
  
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .info-label {
    color: #888;
    font-size: 0.85rem;
  }
  
  .info-value {
    color: #333;
    font-weight: 600;
    font-size: 1rem;
  }
  
  .info-value.price {
    color: #667eea;
    font-size: 1.2rem;
  }
  
  .cancellation-info {
    margin-top: 1rem;
    padding: 0.75rem;
    background: #fff3e0;
    color: #f57c00;
    border-radius: 8px;
    font-size: 0.9rem;
  }
  
  .reservation-actions {
    display: flex;
    flex-direction: column;
    padding: 1.5rem;
    gap: 0.75rem;
    border-left: 1px solid #e0e0e0;
  }
  
  .action-btn {
    padding: 0.75rem 1.5rem;
    border: 2px solid #e0e0e0;
    background: white;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
    white-space: nowrap;
  }
  
  .btn-details {
    background: #667eea;
    color: white;
    border-color: #667eea;
  }
  
  .btn-details:hover {
    background: #5568d3;
  }
  
  .btn-download {
    color: #555;
  }
  
  .btn-download:hover {
    border-color: #667eea;
    background: #f0f2ff;
  }
  
  .btn-cancel {
    color: #f44336;
    border-color: #f44336;
  }
  
  .btn-cancel:hover {
    background: #f44336;
    color: white;
  }
  
  .no-results {
    background: white;
    padding: 4rem 2rem;
    border-radius: 12px;
    text-align: center;
  }
  
  .no-results-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
  }
  
  .no-results h2 {
    color: #333;
    margin-bottom: 0.5rem;
  }
  
  .no-results p {
    color: #666;
  }
  
  @media (max-width: 1024px) {
    .reservation-card {
      grid-template-columns: 1fr;
    }
    
    .reservation-image {
      height: 200px;
    }
    
    .reservation-actions {
      border-left: none;
      border-top: 1px solid #e0e0e0;
      flex-direction: row;
    }
    
    .info-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  @media (max-width: 640px) {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }
    
    .controls {
      flex-direction: column;
    }
    
    .search-box {
      max-width: 100%;
    }
    
    .info-grid {
      grid-template-columns: 1fr;
    }
    
    .reservation-actions {
      flex-direction: column;
    }
  }
</style>