<script>
  export let navigateTo;
  export let searchParams = {};
  import '../styles/searchresults.css';

  let isLoading = false;
  let showMap = false;
  let viewMode = 'list';

  let search = {
    destination: searchParams?.destination ?? "París, Francia",
    checkIn:     searchParams?.checkIn     ?? "2026-02-15",
    checkOut:    searchParams?.checkOut    ?? "2026-02-20",
    rooms:       searchParams?.rooms       ?? 1,
    adults:      searchParams?.adults      ?? 2,
    children:    searchParams?.children    ?? 0
  };

  $: nights = Math.ceil(Math.abs(new Date(search.checkOut).getTime() - new Date(search.checkIn).getTime()) / 86400000);

  let filters = {
    priceMin: 0, priceMax: 1000,
    stars: [], roomTypes: [], amenities: [], propertyTypes: [],
    cancellationPolicy: 'all', guestRating: 0, sortBy: 'recommended'
  };

  const starOptions        = [5, 4, 3, 2, 1];
  const roomTypeOptions    = ['Gran Suite', 'Suite', 'Junior Suite', 'Habitación Doble', 'Habitación Simple'];
  const propertyTypeOptions= ['Hotel', 'Resort', 'Boutique Hotel', 'Apart Hotel', 'Hostal'];
  const amenityOptions     = [
    { id:'wifi',         label:'WiFi Gratis',              icon:'📶' },
    { id:'piscina',      label:'Piscina',                  icon:'🏊' },
    { id:'spa',          label:'Spa',                      icon:'💆' },
    { id:'gym',          label:'Gimnasio',                 icon:'💪' },
    { id:'parking',      label:'Estacionamiento',          icon:'🅿️' },
    { id:'restaurant',   label:'Restaurante',              icon:'🍽️' },
    { id:'bar',          label:'Bar',                      icon:'🍹' },
    { id:'breakfast',    label:'Desayuno incluido',        icon:'🍳' },
    { id:'pets',         label:'Mascotas permitidas',      icon:'🐕' },
    { id:'aircon',       label:'Aire Acondicionado',       icon:'❄️' },
    { id:'room-service', label:'Servicio a la habitación', icon:'🛎️' },
    { id:'airport',      label:'Traslado aeropuerto',      icon:'✈️' }
  ];

  const allHotels = [
    { id:1, name:"Grand Miku Palace Paris",         address:"15 Avenue des Champs-Élysées",  stars:5, rating:4.8, reviews:1247, guestScore:5,   guestScoreText:"Extraordinario", propertyType:"Hotel",        images:["https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800","https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800","https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800"], roomType:"Doble",       pricePerNight:280, totalPrice:1400, originalPrice:1750, discount:17, amenities:['wifi','piscina','spa','gym','parking','restaurant','bar','breakfast','aircon','room-service','airport'], amenitiesDisplay:["WiFi Gratis","Piscina","Spa","Gimnasio","Desayuno incluido"],  cancellationPolicy:"Cancelación gratuita hasta 24h antes", freeCancellation:true,  availableRooms:3, badges:['Mejor valorado','Mejor precio'], distanceToCenter:1.2, sustainableCertified:true  },
    { id:2, name:"Le Miku Boutique Hotel",           address:"28 Rue du Roi de Sicile",       stars:4, rating:4.6, reviews:856,  guestScore:4.6, guestScoreText:"Fabuloso",       propertyType:"Boutique Hotel",images:["https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800","https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800"],                                                                                                                                    roomType:"Suite",       pricePerNight:245, totalPrice:1225, originalPrice:null,  discount:0,  amenities:['wifi','restaurant','bar','breakfast','aircon','room-service'],                                      amenitiesDisplay:["WiFi Gratis","Restaurante","Bar","Desayuno incluido"],           cancellationPolicy:"Cancelación gratuita hasta 48h antes", freeCancellation:true,  availableRooms:5, badges:['Mejor ubicación'],              distanceToCenter:0.8, sustainableCertified:false },
    { id:3, name:"Paris Eiffel Tower Miku Resort",   address:"7 Avenue de la Bourdonnais",    stars:5, rating:4.9, reviews:2134, guestScore:5,   guestScoreText:"Extraordinario", propertyType:"Resort",        images:["https://images.unsplash.com/photo-1549294413-26f195200c16?w=800","https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800","https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800","https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"], roomType:"Gran Suite",  pricePerNight:450, totalPrice:2250, originalPrice:2700, discount:17, amenities:['wifi','piscina','spa','gym','parking','restaurant','bar','breakfast','aircon','room-service','airport','pets'],amenitiesDisplay:["WiFi Gratis","Piscina","Spa","Gimnasio","Vista Torre Eiffel"], cancellationPolicy:"Cancelación gratuita hasta 72h antes", freeCancellation:true,  availableRooms:2, badges:['Más popular','Mejor valorado','Vista exclusiva'], distanceToCenter:1.5, sustainableCertified:true  },
    { id:4, name:"Hotel Montmartre Miku Charm",      address:"42 Rue Lepic",                  stars:4, rating:4.5, reviews:623,  guestScore:4.5, guestScoreText:"Muy bueno",      propertyType:"Hotel",        images:["https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800","https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800"],                                                                                                                                    roomType:"Junior Suite",pricePerNight:180, totalPrice:900,  originalPrice:null,  discount:0,  amenities:['wifi','restaurant','breakfast','aircon','room-service'],                                            amenitiesDisplay:["WiFi Gratis","Restaurante","Desayuno","Aire Acondicionado"],     cancellationPolicy:"No reembolsable",                      freeCancellation:false, availableRooms:8, badges:['Mejor precio'],                  distanceToCenter:2.8, sustainableCertified:false },
    { id:5, name:"Riverside Luxury Miku Paris",      address:"13 Quai de la Tournelle",       stars:5, rating:4.7, reviews:945,  guestScore:4.7, guestScoreText:"Sobresaliente",  propertyType:"Hotel",        images:["https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=800","https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"],                                                                                                                                    roomType:"Suite",       pricePerNight:385, totalPrice:1925, originalPrice:2310, discount:17, amenities:['wifi','piscina','spa','gym','restaurant','bar','breakfast','aircon','room-service'],                  amenitiesDisplay:["WiFi Gratis","Vista al Sena","Spa","Gimnasio","Desayuno"],       cancellationPolicy:"Cancelación gratuita hasta 48h antes", freeCancellation:true,  availableRooms:4, badges:['Vista al río'],                  distanceToCenter:1.0, sustainableCertified:true  },
    { id:6, name:"Opera District Miku Apart Hotel",  address:"8 Rue Scribe",                  stars:4, rating:4.4, reviews:534,  guestScore:4,   guestScoreText:"Bueno",          propertyType:"Apart Hotel",  images:["https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=800","https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800"],                                                                                                                                    roomType:"Suite",       pricePerNight:210, totalPrice:1050, originalPrice:null,  discount:0,  amenities:['wifi','gym','breakfast','aircon','parking'],                                                        amenitiesDisplay:["WiFi Gratis","Cocina","Gimnasio","Estacionamiento"],             cancellationPolicy:"Cancelación gratuita hasta 24h antes", freeCancellation:true,  availableRooms:6, badges:[],                              distanceToCenter:1.8, sustainableCertified:false }
  ];

  $: filteredHotels = filterAndSort(allHotels, filters);

  function filterAndSort(hotels, f) {
    return hotels
      .filter(h =>
        h.pricePerNight >= f.priceMin && h.pricePerNight <= f.priceMax &&
        (f.stars.length === 0         || f.stars.includes(h.stars)) &&
        (f.roomTypes.length === 0     || f.roomTypes.includes(h.roomType)) &&
        (f.propertyTypes.length === 0 || f.propertyTypes.includes(h.propertyType)) &&
        (f.amenities.length === 0     || f.amenities.every(a => h.amenities.includes(a))) &&
        (f.cancellationPolicy !== 'free' || h.freeCancellation) &&
        h.guestScore >= f.guestRating
      )
      .sort((a, b) => {
        if (f.sortBy === 'price-low')  return a.pricePerNight - b.pricePerNight;
        if (f.sortBy === 'price-high') return b.pricePerNight - a.pricePerNight;
        if (f.sortBy === 'rating')     return b.guestScore - a.guestScore;
        if (f.sortBy === 'distance')   return a.distanceToCenter - b.distanceToCenter;
        return (b.guestScore * b.availableRooms) - (a.guestScore * a.availableRooms);
      });
  }

  function resetFilters() {
    filters = { priceMin:0, priceMax:1000, stars:[], roomTypes:[], amenities:[], propertyTypes:[], cancellationPolicy:'all', guestRating:0, sortBy:'recommended' };
  }

  function toggle(arr, val) {
    const i = arr.indexOf(val);
    i > -1 ? arr.splice(i, 1) : arr.push(val);
    filters = filters;
  }

  const fmt  = p => new Intl.NumberFormat('es-GT', { style:'currency', currency:'USD', minimumFractionDigits:0 }).format(p);
  const stars = n => '⭐'.repeat(n);
  const fmtDate = d => new Date(d).toLocaleDateString('es-ES', { day:'numeric', month:'short' });

  const SORTS = [
    { id:'recommended', label:'Recomendado' },
    { id:'price-low',   label:'Precio: Menor' },
    { id:'price-high',  label:'Precio: Mayor' },
    { id:'rating',      label:'Mejor Valorado' },
    { id:'distance',    label:'Distancia' }
  ];
</script>

<div class="sr-page">

  <div class="sr-container">

    <!-- Header -->
    <div class="sr-header">
      <div>
        <h1>{search.destination}: {filteredHotels.length} hoteles encontrados</h1>
        <p class="sr-subtitle">{nights} {nights === 1 ? 'noche' : 'noches'} • {search.adults} {search.adults === 1 ? 'adulto' : 'adultos'}{search.children > 0 ? ` • ${search.children} niños` : ''}</p>
      </div>
      <div class="sr-actions">
        <div class="view-toggle">
          <button class="vbtn" class:active={viewMode === 'list'} on:click={() => viewMode = 'list'} title="Lista">☰</button>
          <button class="vbtn" class:active={viewMode === 'grid'} on:click={() => viewMode = 'grid'} title="Cuadrícula">⊞</button>
        </div>
        <button class="btn-map" class:active={showMap} on:click={() => showMap = !showMap}>
          🗺 {showMap ? 'Ocultar' : 'Ver'} mapa
        </button>
      </div>
    </div>

    <div class="sr-layout">

      <!-- Filters -->
      <aside class="sr-filters">
        <div class="sr-filters-hdr">
          <h2>Filtrar por:</h2>
          <button class="btn-reset" on:click={resetFilters}>↺ Limpiar</button>
        </div>

        <!-- Price -->
        <div class="filter-group">
          <h3 class="filter-title">💰 Precio por noche</h3>
          <div class="price-row">
            <label>Mín <div class="price-inp-wrap"><span>$</span><input type="number" bind:value={filters.priceMin} min="0" /></div></label>
            <span>—</span>
            <label>Máx <div class="price-inp-wrap"><span>$</span><input type="number" bind:value={filters.priceMax} min="0" /></div></label>
          </div>
          <div class="price-bar"><div class="price-fill" style="left:{(filters.priceMin/1000)*100}%;right:{100-(filters.priceMax/1000)*100}%"></div></div>
          <div class="price-display">${filters.priceMin} — ${filters.priceMax} / noche</div>
        </div>

        <!-- Stars -->
        <div class="filter-group">
          <h3 class="filter-title">⭐ Categoría</h3>
          {#each starOptions as s}
            <label class="chk-label">
              <input type="checkbox" checked={filters.stars.includes(s)} on:change={() => toggle(filters.stars, s)} />
              <span>{'⭐'.repeat(s)} {s} {s === 1 ? 'estrella' : 'estrellas'}</span>
            </label>
          {/each}
        </div>

        <!-- Amenities -->
        <div class="filter-group">
          <h3 class="filter-title">🛎 Servicios</h3>
          {#each amenityOptions as a}
            <label class="chk-label">
              <input type="checkbox" checked={filters.amenities.includes(a.id)} on:change={() => toggle(filters.amenities, a.id)} />
              <span>{a.icon} {a.label}</span>
            </label>
          {/each}
        </div>

        <!-- Room type -->
        <div class="filter-group">
          <h3 class="filter-title">🛏 Tipo de habitación</h3>
          {#each roomTypeOptions as r}
            <label class="chk-label">
              <input type="checkbox" checked={filters.roomTypes.includes(r)} on:change={() => toggle(filters.roomTypes, r)} />
              <span>{r}</span>
            </label>
          {/each}
        </div>

        <!-- Property type -->
        <div class="filter-group">
          <h3 class="filter-title">🏨 Tipo de propiedad</h3>
          {#each propertyTypeOptions as p}
            <label class="chk-label">
              <input type="checkbox" checked={filters.propertyTypes.includes(p)} on:change={() => toggle(filters.propertyTypes, p)} />
              <span>{p}</span>
            </label>
          {/each}
        </div>

        <!-- Cancellation -->
        <div class="filter-group">
          <h3 class="filter-title">📋 Cancelación</h3>
          <label class="chk-label"><input type="radio" bind:group={filters.cancellationPolicy} value="all" /> <span>Todas</span></label>
          <label class="chk-label"><input type="radio" bind:group={filters.cancellationPolicy} value="free" /> <span>Solo cancelación gratuita</span></label>
        </div>

        <!-- Guest rating -->
        <div class="filter-group">
          <h3 class="filter-title">👍 Calificación</h3>
          <div class="rating-chips">
            {#each [0, 7, 8, 8.5, 9] as r}
              <button class="chip" class:active={filters.guestRating === r} on:click={() => filters.guestRating = r}>
                {r === 0 ? 'Todas' : `${r}+`}
              </button>
            {/each}
          </div>
        </div>
      </aside>

      <!-- Results -->
      <main class="sr-main">

        <!-- Sort -->
        <div class="sort-bar">
          <span class="sort-lbl">Ordenar:</span>
          {#each SORTS as s}
            <button class="sort-btn" class:active={filters.sortBy === s.id} on:click={() => filters.sortBy = s.id}>{s.label}</button>
          {/each}
        </div>

        <!-- Hotel list -->
        {#if isLoading}
          {#each Array(3) as _}
            <div class="hotel-card skeleton"><div class="sk-img"></div><div class="sk-body"><div class="sk-line"></div><div class="sk-line short"></div><div class="sk-line medium"></div></div></div>
          {/each}
        {:else if filteredHotels.length === 0}
          <div class="no-results">
            <div class="no-results-icon">🔍</div>
            <h2>No encontramos hoteles con estos criterios</h2>
            <p>Intenta ajustar tus filtros</p>
            <button class="btn-primary" on:click={resetFilters}>Limpiar filtros</button>
          </div>
        {:else}
          <div class="hotels-grid" class:list-view={viewMode === 'list'} class:grid-view={viewMode === 'grid'}>
            {#each filteredHotels as h (h.id)}
              <div class="hotel-card" role="button" tabindex="0"
                on:click={() => navigateTo('hotel-detail', h.id)}
                on:keydown={e => e.key === 'Enter' && navigateTo('hotel-detail', h.id)}>

                <div class="hotel-gallery">
                  <div class="gallery-main">
                    <img src={h.images[0]} alt={h.name} />
                    {#if h.discount > 0}<div class="disc-badge">-{h.discount}%</div>{/if}
                    {#if h.sustainableCertified}<div class="eco-badge" title="Certificado sostenible">🌱</div>{/if}
                  </div>
                  {#if h.images.length > 1}
                    <div class="gallery-thumbs">
                      {#each h.images.slice(1, 4) as img, i}
                        <div class="thumb"><img src={img} alt="{h.name} {i+2}" /></div>
                      {/each}
                      {#if h.images.length > 4}<div class="thumb-more">+{h.images.length - 4}</div>{/if}
                    </div>
                  {/if}
                  <button class="fav-btn" aria-label="Favorito" on:click|stopPropagation={() => {}}>♡</button>
                </div>

                <div class="hotel-content">
                  <div class="hotel-hdr">
                    <div class="hotel-title-wrap">
                      {#if h.badges.length > 0}
                        <div class="badges-row">{#each h.badges as b}<span class="badge">{b}</span>{/each}</div>
                      {/if}
                      <h2 class="hotel-name">{h.name}</h2>
                      <div class="hotel-stars">{stars(h.stars)} <span class="prop-type">{h.propertyType}</span></div>
                    </div>
                    <div class="rating-box">
                      <div class="rating-score">{h.guestScore}</div>
                      <div class="rating-text">{h.guestScoreText}</div>
                      <div class="rating-reviews">{h.reviews} opiniones</div>
                    </div>
                  </div>

                  <div class="hotel-loc">📍 {h.address} <span class="dist-pill">{h.distanceToCenter} km del centro</span></div>

                  <div class="amenities-row">
                    {#each h.amenitiesDisplay.slice(0, 5) as a}<span class="amenity-pill">{a}</span>{/each}
                    {#if h.amenitiesDisplay.length > 5}<span class="amenity-more">+{h.amenitiesDisplay.length - 5} más</span>{/if}
                  </div>

                  <div class="room-info">
                    🛏 {h.roomType}
                    {#if h.availableRooms <= 3}<span class="urgency"> • Solo quedan {h.availableRooms}</span>{/if}
                  </div>

                  <div class="cancellation" class:free={h.freeCancellation}>
                    {h.freeCancellation ? '✓' : '✕'} {h.cancellationPolicy}
                  </div>

                  <div class="hotel-footer">
                    <div class="pricing">
                      <div class="price-detail">
                        {#if h.originalPrice}<div class="orig-price">{fmt(h.originalPrice)}</div>{/if}
                        <div class="curr-price"><span class="price-amount">{fmt(h.totalPrice)}</span> <span class="price-lbl">total</span></div>
                        <div class="per-night">{fmt(h.pricePerNight)}/noche</div>
                      </div>
                      <button class="btn-view">Ver disponibilidad →</button>
                    </div>
                  </div>
                </div>
              </div>
            {/each}
          </div>

          <div class="pagination">
            <button class="pag-btn" disabled>← Anterior</button>
            <div class="pag-nums">
              <button class="pag-num active">1</button>
              <button class="pag-num">2</button>
              <button class="pag-num">3</button>
              <span>...</span>
              <button class="pag-num">10</button>
            </div>
            <button class="pag-btn">Siguiente →</button>
          </div>
        {/if}
      </main>
    </div>
  </div>

  <!-- Map -->
  {#if showMap}
    <div class="map-overlay">
      <div class="map-container">
        <button class="map-close" on:click={() => showMap = false}>✕</button>
        <div class="map-placeholder">
          <div style="font-size:4rem">🗺️</div>
          <h3>Vista de mapa</h3>
          <p>Aquí se mostraría un mapa interactivo con la ubicación de cada hotel</p>
        </div>
      </div>
    </div>
  {/if}
</div>