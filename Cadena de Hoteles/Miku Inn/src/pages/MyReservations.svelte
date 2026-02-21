<script>
  import '../styles/myreservations.css';

  export let navigateTo = (page, data = null) => {};

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

  const STATUS = {
    confirmed: { text: 'Confirmada', cls: 'confirmed', icon: '✓' },
    completed:  { text: 'Completada', cls: 'completed', icon: '✓' },
    cancelled:  { text: 'Cancelada',  cls: 'cancelled', icon: '✕' }
  };

  const FILTERS = [['all','Todas'],['confirmed','Confirmadas'],['completed','Completadas'],['cancelled','Canceladas']];

  let filter = 'all';
  let search = '';

  $: filtered = reservations.filter(r =>
    (filter === 'all' || r.status === filter) &&
    (r.id.toLowerCase().includes(search.toLowerCase()) ||
     r.hotelName.toLowerCase().includes(search.toLowerCase()))
  );

  function cancel(id) {
    if (confirm('¿Cancelar esta reserva?')) {
      reservations = reservations.map(r =>
        r.id === id ? { ...r, status: 'cancelled', cancellationDate: new Date().toISOString().split('T')[0] } : r
      );
    }
  }
</script>

<div class="wrap">
  <div class="inner">

    <header class="hdr">
      <div>
        <h1>Mis Reservas</h1>
        <p class="sub">Gestiona todas tus reservaciones en un solo lugar</p>
      </div>
      <button class="btn-new" on:click={() => navigateTo('home')}>+ Nueva Reserva</button>
    </header>

    <div class="controls">
      <div class="filters">
        {#each FILTERS as [val, label]}
          <button class="fbtn" class:active={filter === val} on:click={() => filter = val}>{label}</button>
        {/each}
      </div>
      <div class="search">
        <span>🔍</span>
        <input bind:value={search} placeholder="Buscar por código o nombre de hotel..." />
      </div>
    </div>

    <div class="list">
      {#if filtered.length === 0}
        <div class="empty">
          <div class="empty-icon">📋</div>
          <h2>No se encontraron reservas</h2>
          <p>Intenta ajustar los filtros o realiza una nueva búsqueda</p>
        </div>
      {:else}
        {#each filtered as r}
          {@const s = STATUS[r.status]}
          <div class="card">
            <div class="img-wrap">
              <img src={r.image} alt={r.hotelName} />
              <span class="badge {s.cls}">{s.icon} {s.text}</span>
            </div>

            <div class="info">
              <div class="info-hdr">
                <div>
                  <h3>{r.hotelName}</h3>
                  <p class="room">{r.roomType}</p>
                </div>
                <div class="code">
                  <small>Código de Reserva</small>
                  <strong>{r.id}</strong>
                </div>
              </div>

              <div class="grid">
                {#each [['📅 Check-in', r.checkIn],['📅 Check-out', r.checkOut],['🌙 Noches', r.nights],['👥 Huéspedes', r.guests],['💰 Total', `$${r.totalPrice}`],['📆 Reservado', r.bookingDate]] as [label, val]}
                  <div class="cell">
                    <span class="lbl">{label}</span>
                    <span class="val">{val}</span>
                  </div>
                {/each}
              </div>

              {#if r.cancellationDate}
                <div class="cancel-note">⚠️ Cancelada el {r.cancellationDate}</div>
              {/if}
            </div>

            <div class="actions">
              <button class="abtn primary">Ver Detalles</button>
              <button class="abtn">📄 Descargar</button>
              {#if r.status === 'confirmed'}
                <button class="abtn danger" on:click={() => cancel(r.id)}>Cancelar Reserva</button>
              {/if}
            </div>
          </div>
        {/each}
      {/if}
    </div>

  </div>
</div>