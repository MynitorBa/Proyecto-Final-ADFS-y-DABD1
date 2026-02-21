<script>
  export let navigateTo;
  import '../styles/ofertas.css';

  let selectedCategory = 'all';

  const categories = [
    { id: 'all',        name: 'Todas',        icon: '<rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect>' },
    { id: 'flash',      name: 'Flash',        icon: '<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>' },
    { id: 'lastminute', name: 'Última Hora',  icon: '<circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline>' },
    { id: 'season',     name: 'Temporada',    icon: '<circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>' },
    { id: 'premium',    name: 'Premium',      icon: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>' },
    { id: 'packages',   name: 'Paquetes',     icon: '<polyline points="20 12 20 22 4 22 4 12"></polyline><rect x="2" y="7" width="20" height="5"></rect><line x1="12" y1="22" x2="12" y2="7"></line><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"></path><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"></path>' }
  ];

  const ofertas = [
    { id:1, hotelId:1, name:"Grand Miku Palace Paris",    destination:"París, Francia",         image:"https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800",  category:"flash",      discount:45, originalPrice:350, offerPrice:192, rating:4.9, reviews:1247, stars:5, featured:true,  badge:"Oferta Flash",      timeLeft:{days:7,  hours:12}, perks:["Desayuno incluido","Cancelación gratis","WiFi premium"] },
    { id:2, hotelId:2, name:"Miku Luxury Suites Tokyo",   destination:"Tokio, Japón",           image:"https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800",  category:"lastminute", discount:35, originalPrice:420, offerPrice:273, rating:4.8, reviews:892,  stars:5, featured:true,  badge:"Última Hora",       timeLeft:{days:2,  hours:8},  perks:["Spa gratis","Late check-out","Upgrade garantizado"] },
    { id:3, hotelId:3, name:"Miku Beach Resort Cancún",   destination:"Cancún, México",         image:"https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800",  category:"season",     discount:30, originalPrice:280, offerPrice:196, rating:4.7, reviews:654,  stars:4, featured:false, badge:"Temporada Baja",    timeLeft:{days:56, hours:0},  perks:["Todo incluido","Playa privada","Actividades gratis"] },
    { id:4, hotelId:4, name:"Miku Royal London",          destination:"Londres, Reino Unido",   image:"https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800",  category:"premium",    discount:25, originalPrice:480, offerPrice:360, rating:4.9, reviews:1103, stars:5, featured:false, badge:"Suite Premium",     timeLeft:{days:25, hours:0},  perks:["Suite ejecutiva","Mayordomo 24/7","Vista panorámica"] },
    { id:5, hotelId:5, name:"Miku City New York",         destination:"Nueva York, USA",        image:"https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",  category:"packages",   discount:40, originalPrice:520, offerPrice:312, rating:4.6, reviews:987,  stars:5, featured:true,  badge:"Paquete 3 Noches",  timeLeft:{days:12, hours:6},  perks:["3 noches al 2x1","Tours incluidos","Cena de cortesía"] },
    { id:6, hotelId:6, name:"Miku Mountain Resort",       destination:"Aspen, Colorado",        image:"https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",  category:"season",     discount:35, originalPrice:380, offerPrice:247, rating:4.8, reviews:543,  stars:4, featured:false, badge:"Fin de Temporada",  timeLeft:{days:17, hours:0},  perks:["Ski pass incluido","Equipo gratis","Chocolate caliente"] }
  ];

  $: filtered = selectedCategory === 'all' ? ofertas : ofertas.filter(o => o.category === selectedCategory);

  const fmt = p => new Intl.NumberFormat('es-GT', { style:'currency', currency:'USD', minimumFractionDigits:0 }).format(p);
  const count = id => id === 'all' ? ofertas.length : ofertas.filter(o => o.category === id).length;
</script>

<div class="ofertas-page">

  <section class="ofertas__hero-section">
    <div class="hero-background"></div>
    <div class="hero-container">
      <div class="ofertas__hero-content">
        <span class="hero-label">Descuentos Exclusivos</span>
        <h1 class="ofertas__hero-title">Ofertas Especiales</h1>
        <p class="ofertas__hero-description">Descubre nuestras promociones seleccionadas con descuentos de hasta 45%</p>
      </div>
      <div class="hero-stats-grid">
        {#each [['{ofertas.length}', 'Ofertas Activas', 'M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z'], ['45%', 'Descuento Máximo', 'M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6'], ['6', 'Destinos', 'M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0zM12 10m-3 0a3 3 0 1 0 6 0 3 3 0 0 0-6 0']] as [val, lbl, path]}
          <div class="stat-card">
            <div class="stat-icon"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d={path}></path></svg></div>
            <div class="stat-value">{val === '{ofertas.length}' ? ofertas.length : val}</div>
            <div class="stat-label">{lbl}</div>
          </div>
        {/each}
      </div>
    </div>
  </section>

  <div class="main-container">

    <section class="filters-section">
      <div class="filters-wrapper">
        {#each categories as c}
          <button class="ofertas__filter-btn" class:active={selectedCategory === c.id} on:click={() => selectedCategory = c.id}>
            <span class="filter-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">{@html c.icon}</svg></span>
            <span class="filter-text">{c.name}</span>
            <span class="filter-count">{count(c.id)}</span>
          </button>
        {/each}
      </div>
    </section>

    <section class="offers-section">
      <div class="ofertas__section-header">
        <h2 class="ofertas__section-title">{selectedCategory === 'all' ? 'Todas las Ofertas' : categories.find(c => c.id === selectedCategory).name}</h2>
        <p class="section-subtitle">{filtered.length} {filtered.length === 1 ? 'oferta disponible' : 'ofertas disponibles'}</p>
      </div>

      <div class="offers-grid">
        {#each filtered as o}
          <div class="offer-card" role="button" tabindex="0"
            on:click={() => navigateTo('hotel-detail', o.hotelId)}
            on:keydown={e => e.key === 'Enter' && navigateTo('hotel-detail', o.hotelId)}>
            <div class="offer-image-wrapper">
              <img src={o.image} alt={o.name} class="offer-image" />
              <div class="badges-container">
                <div class="ofertas__discount-badge">-{o.discount}%</div>
                {#if o.featured}<div class="ofertas__featured-badge">★ Destacada</div>{/if}
              </div>
            </div>

            <div class="offer-body">
              <div class="offer-header">
                <div class="hotel-info">
                  <h3 class="ofertas__hotel-name">{o.name}</h3>
                  <div class="ofertas__hotel-location">📍 {o.destination}</div>
                </div>
                <div class="rating-badge">★ {o.rating}</div>
              </div>

              <div class="hotel-details">
                <div class="stars">
                  {'★'.repeat(o.stars)} <span class="reviews-count">({o.reviews})</span>
                </div>
                <div class="category-tag">{o.badge}</div>
              </div>

              <div class="perks-list">
                {#each o.perks as perk}
                  <div class="perk-item">✓ {perk}</div>
                {/each}
              </div>

              {#if o.timeLeft.days < 30}
                <div class="countdown-banner">⏱ Termina en <strong>{o.timeLeft.days}d {o.timeLeft.hours}h</strong></div>
              {/if}

              <div class="offer-footer">
                <div class="price-section">
                  <div class="price-original">{fmt(o.originalPrice)}</div>
                  <div class="price-current"><span class="ofertas__amount">{fmt(o.offerPrice)}</span> <span class="period">/ noche</span></div>
                  <div class="savings-text">Ahorra {fmt(o.originalPrice - o.offerPrice)}</div>
                </div>
                <button class="view-offer-btn">Ver Oferta →</button>
              </div>
            </div>
          </div>
        {/each}
      </div>
    </section>

    <section class="final-cta">
      <div class="cta-card">
        <div class="ofertas__cta-content">
          <h2 class="ofertas__cta-title">¿No encontraste lo que buscabas?</h2>
          <p class="ofertas__cta-description">Explora nuestra colección completa de hoteles alrededor del mundo</p>
        </div>
        <button class="ofertas__cta-button" on:click={() => navigateTo('search-results')}>🔍 Buscar Todos los Hoteles</button>
      </div>
    </section>

  </div>
</div>