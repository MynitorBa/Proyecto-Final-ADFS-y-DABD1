<script>
  /** @type {{ pais?:string, ciudad?:string, fechaCheckIn?:string, fechaCheckOut?:string, cantidadPersonas?:number, hotels?:any[] } | null} */
  export let searchParams = null;
  export let navigateTo;
  import '../styles/searchresults.css';

  const API = 'http://localhost:7000';

  let isLoading    = false;
  let isSearching  = false;
  let viewMode     = 'list';
  let searchError  = '';

  let pais             = (searchParams && searchParams.pais)             ? searchParams.pais             : '';
  let ciudad           = (searchParams && searchParams.ciudad)           ? searchParams.ciudad           : '';
  let fechaCheckIn     = (searchParams && searchParams.fechaCheckIn)     ? searchParams.fechaCheckIn     : '';
  let fechaCheckOut    = (searchParams && searchParams.fechaCheckOut)    ? searchParams.fechaCheckOut    : '';
  let cantidadPersonas = (searchParams && searchParams.cantidadPersonas) ? searchParams.cantidadPersonas : 1;
  let hotelsRaw        = (searchParams && Array.isArray(searchParams.hotels)) ? searchParams.hotels      : [];

  let filters = {
    priceMin:    0,
    priceMax:    0,
    tiposHab:    [],
    amenidades:  [],
    sortBy:      'recommended'
  };

  $: nights = (fechaCheckIn && fechaCheckOut)
    ? Math.max(1, Math.ceil((Number(new Date(fechaCheckOut)) - Number(new Date(fechaCheckIn))) / 86400000))
    : 1;

  $: allAmenidades = (() => {
    const set = new Set();
    hotelsRaw.forEach(h => h.amenidades?.forEach(a => set.add(a.nombre)));
    return [...set].sort();
  })();

  $: allTiposHab = (() => {
    const set = new Set();
    hotelsRaw.forEach(h => h.habitaciones?.forEach(r => set.add(r.tipoHabitacion)));
    return [...set].sort();
  })();

  $: filteredHotels = filterAndSort(hotelsRaw, filters);

  /** Precio mínimo de habitación directa. Null si no hay ninguna. */
  function getMinPrice(hotel) {
    if (!hotel.habitaciones || hotel.habitaciones.length === 0) return null;
    return Math.min(...hotel.habitaciones.map(r => r.precioPorNoche));
  }

  /**
   * Construye la lista de habs de la primera combinación exacta del backend.
   * Solo para combos de 2+ habitaciones.
   */
  function getComboHabs(hotel) {
    if (!hotel.combinacionesNumericas?.length) return null;
    const combo = hotel.combinacionesNumericas[0];
    if (combo.length <= 1) return null;
    const usados = {};
    const result = [];
    for (const cap of combo) {
      const key   = String(cap);
      const rooms = hotel.habitacionesPorCapacidad?.[key];
      if (!rooms?.length) return null;
      const idx = usados[key] ?? 0;
      if (idx >= rooms.length) return null;
      result.push({ tipo: rooms[idx].tipoHabitacion, precio: rooms[idx].precioPorNoche, cap });
      usados[key] = idx + 1;
    }
    return result;
  }

  /**
   * Cuando no hay disponibilidad exacta (habitaciones=[] y combinacionesNumericas=[]),
   * intenta construir la mejor combinación aproximada usando habitacionesPorCapacidad,
   * siempre que la capacidad total esté entre cantidadPersonas y cantidadPersonas+2.
   *
   * Algoritmo: toma todas las habitaciones disponibles en habitacionesPorCapacidad,
   * las ordena de mayor a menor capacidad, y va sumando hasta cubrir el objetivo.
   * Si el total queda en [personas, personas+2] → devuelve la combinación.
   * Si no → null.
   */
  function getComboAproximado(hotel, personas) {
    // Solo aplica si no tiene disponibilidad exacta
    const tieneDirecta = hotel.habitaciones && hotel.habitaciones.length > 0;
    const tieneCombo   = !!getComboHabs(hotel);
    if (tieneDirecta || tieneCombo) return null;

    const porCapacidad = hotel.habitacionesPorCapacidad;
    if (!porCapacidad || Object.keys(porCapacidad).length === 0) return null;

    // Aplanar todas las habitaciones disponibles
    const todasHabs = [];
    for (const [capStr, rooms] of Object.entries(porCapacidad)) {
      const cap = Number(capStr);
      for (const room of rooms) {
        todasHabs.push({ tipo: room.tipoHabitacion, precio: room.precioPorNoche, cap });
      }
    }

    // Ordenar de mayor a menor capacidad para cubrir con el mínimo de habitaciones
    todasHabs.sort((a, b) => b.cap - a.cap);

    // Selección greedy: ir sumando habs hasta cubrir el objetivo
    let sumCap   = 0;
    const selec  = [];
    const limite = personas + 2; // margen máximo

    for (const hab of todasHabs) {
      if (sumCap >= personas) break;
      selec.push(hab);
      sumCap += hab.cap;
    }

    // Validar que la combinación encontrada esté en [personas, personas+2]
    if (sumCap < personas || sumCap > limite) return null;
    // Debe ser más de 1 hab para ser considerada combinación
    if (selec.length <= 1) return null;

    return { habs: selec, capacidadTotal: sumCap, esAproximado: true };
  }

  /** Precio total de un array de habs */
  function sumPrecios(habs) {
    return habs.reduce((s, h) => s + h.precio, 0);
  }

  /**
   * Precio "Desde" para ordenar/filtrar.
   * Toma el mínimo entre directo, combo exacto y combo aproximado.
   */
  function getDesde(hotel) {
    const directo = getMinPrice(hotel);
    const combo   = getComboHabs(hotel);
    const aprox   = getComboAproximado(hotel, cantidadPersonas);

    const precios = [];
    if (directo !== null)  precios.push(directo);
    if (combo   !== null)  precios.push(sumPrecios(combo));
    if (aprox   !== null)  precios.push(sumPrecios(aprox.habs));

    return precios.length ? Math.min(...precios) : null;
  }

  function filterAndSort(hotels, f) {
    return hotels
      .filter(h => {
        const tieneDirecta = h.habitaciones && h.habitaciones.length > 0;
        const tieneCombo   = !!getComboHabs(h);
        const tieneAprox   = !!getComboAproximado(h, cantidadPersonas);
        if (!tieneDirecta && !tieneCombo && !tieneAprox) return false;

        const desde  = getDesde(h);
        const priceOk = desde === null || (desde >= f.priceMin && (f.priceMax === 0 || desde <= f.priceMax));

        const tipoOk = f.tiposHab.length === 0 ||
          h.habitaciones?.some(r => f.tiposHab.includes(r.tipoHabitacion));

        const amenOk = f.amenidades.length === 0 ||
          f.amenidades.every(a => h.amenidades?.some(am => am.nombre === a));

        return priceOk && tipoOk && amenOk;
      })
      .sort((a, b) => {
        if (f.sortBy === 'price-low')  return (getDesde(a) ?? 0) - (getDesde(b) ?? 0);
        if (f.sortBy === 'price-high') return (getDesde(b) ?? 0) - (getDesde(a) ?? 0);
        if (f.sortBy === 'rating')     return (b.rating ?? 0) - (a.rating ?? 0);
        return (b.rating ?? 0) - (a.rating ?? 0);
      });
  }

  function toggleArr(arr, val) {
    const i = arr.indexOf(val);
    i > -1 ? arr.splice(i, 1) : arr.push(val);
    filters = { ...filters };
  }

  function resetFilters() {
    filters = { priceMin: 0, priceMax: 0, tiposHab: [], amenidades: [], sortBy: 'recommended' };
  }

  async function handleReSearch(e) {
    e.preventDefault();
    if (!pais.trim() || !ciudad.trim()) return;
    isSearching = true;
    searchError = '';
    try {
      const res = await fetch(`${API}/busqueda`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          pais:             pais.trim(),
          ciudad:           ciudad.trim(),
          fechaCheckIn,
          fechaCheckOut,
          cantidadPersonas: Number(cantidadPersonas)
        })
      });
      if (!res.ok) { searchError = 'Error al buscar.'; return; }
      hotelsRaw = await res.json();
      resetFilters();
    } catch(err) {
      searchError = 'Error de conexion: ' + err.message;
    } finally {
      isSearching = false;
    }
  }

  const fmt = p => new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD', minimumFractionDigits: 0 }).format(p);

  const SORTS = [
    { id: 'recommended', label: 'Recomendado' },
    { id: 'price-low',   label: 'Precio: Menor' },
    { id: 'price-high',  label: 'Precio: Mayor' },
    { id: 'rating',      label: 'Mejor Valorado' }
  ];

  function amenidadIcon(nombre) {
    const n = nombre.toLowerCase();
    if (n.includes('wifi'))           return 'M5 12.55a11 11 0 0 1 14.08 0M1.42 9a16 16 0 0 1 21.16 0M8.53 16.11a6 6 0 0 1 6.95 0M12 20h.01';
    if (n.includes('piscina'))        return 'M2 12h20M2 17h20M2 7h20';
    if (n.includes('gimnasio'))       return 'M6.5 6.5h11M18 12H6M6.5 17.5h11';
    if (n.includes('estacionamiento'))return 'M5 17H3a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2h-3';
    if (n.includes('restaurante'))    return 'M3 11l19-9-9 19-2-8-8-2z';
    if (n.includes('spa'))            return 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z';
    if (n.includes('bar'))            return 'M8 22h8M7 10h10l-1 7H8L7 10zM5 10l2-7h10l2 7';
    if (n.includes('desayuno'))       return 'M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z';
    return 'M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 21 12 17.77 5.82 21 7 14.14 2 9.27l6.91-1.01L12 2z';
  }
</script>

<div class="sr-page">
  <div class="sr-container">

    <div class="sr-modify-bar">
      <div class="sr-modify-content">
        <form class="sr-modify-form" on:submit={handleReSearch}>
          <div class="sr-form-fields">
            <div class="sr-field-group">
              <label for="sr-pais">Pais</label>
              <input id="sr-pais" type="text" bind:value={pais} placeholder="Guatemala" required />
            </div>
            <div class="sr-field-group">
              <label for="sr-ciudad">Ciudad</label>
              <input id="sr-ciudad" type="text" bind:value={ciudad} placeholder="Ciudad de Guatemala" required />
            </div>
            <div class="sr-field-group">
              <label for="sr-checkin">Check-in</label>
              <input id="sr-checkin" type="date" bind:value={fechaCheckIn} required />
            </div>
            <div class="sr-field-group">
              <label for="sr-checkout">Check-out</label>
              <input id="sr-checkout" type="date" bind:value={fechaCheckOut} min={fechaCheckIn} required />
            </div>
            <div class="sr-field-group">
              <label for="sr-personas">Huéspedes</label>
              <input id="sr-personas" type="number" bind:value={cantidadPersonas} min="1" placeholder="Nº huéspedes" />
            </div>
          </div>
          {#if searchError}
            <p class="sr-error">{searchError}</p>
          {/if}
          <button type="submit" class="btn-modify" disabled={isSearching}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            {isSearching ? 'Buscando...' : 'Buscar'}
          </button>
        </form>
      </div>
    </div>

    <div class="sr-header">
      <div>
        <h1>{ciudad}{pais ? ', ' + pais : ''}: {filteredHotels.length} hotel{filteredHotels.length !== 1 ? 'es' : ''} encontrado{filteredHotels.length !== 1 ? 's' : ''}</h1>
        <p class="sr-subtitle">{nights} {nights === 1 ? 'noche' : 'noches'} · {cantidadPersonas} {cantidadPersonas === 1 ? 'persona' : 'personas'}</p>
      </div>
      <div class="sr-actions">
        <div class="view-toggle">
          <button class="vbtn" class:active={viewMode === 'list'} on:click={() => viewMode = 'list'} title="Lista">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
          </button>
          <button class="vbtn" class:active={viewMode === 'grid'} on:click={() => viewMode = 'grid'} title="Cuadricula">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          </button>
        </div>
      </div>
    </div>

    <div class="sr-layout">

      <aside class="sr-filters">
        <div class="sr-filters-hdr">
          <h2>Filtrar por:</h2>
          <button class="btn-reset" on:click={resetFilters}>Limpiar</button>
        </div>

        <div class="filter-group">
          <h3 class="filter-title">Precio por noche</h3>
          <div class="price-row">
            <label for="pmin">Min
              <div class="price-inp-wrap">
                <span>$</span>
                <input id="pmin" type="number" bind:value={filters.priceMin} min="0" />
              </div>
            </label>
            <span>—</span>
            <label for="pmax">Max
              <div class="price-inp-wrap">
                <span>$</span>
                <input id="pmax" type="number" bind:value={filters.priceMax} min="0" />
              </div>
            </label>
          </div>
          <div class="price-display">${filters.priceMin || '0'} — {filters.priceMax ? '$' + filters.priceMax : 'Sin límite'} / noche</div>
        </div>

        {#if allTiposHab.length > 0}
          <div class="filter-group">
            <h3 class="filter-title">Tipo de habitacion</h3>
            {#each allTiposHab as tipo}
              <label class="chk-label">
                <input type="checkbox" checked={filters.tiposHab.includes(tipo)} on:change={() => toggleArr(filters.tiposHab, tipo)} />
                <span>{tipo}</span>
              </label>
            {/each}
          </div>
        {/if}

        {#if allAmenidades.length > 0}
          <div class="filter-group">
            <h3 class="filter-title">Servicios y amenidades</h3>
            {#each allAmenidades as amen}
              <label class="chk-label">
                <input type="checkbox" checked={filters.amenidades.includes(amen)} on:change={() => toggleArr(filters.amenidades, amen)} />
                <span>{amen}</span>
              </label>
            {/each}
          </div>
        {/if}
      </aside>

      <main class="sr-main">

        <div class="sort-bar">
          <span class="sort-lbl">Ordenar:</span>
          {#each SORTS as s}
            <button class="sort-btn" class:active={filters.sortBy === s.id} on:click={() => filters.sortBy = s.id}>{s.label}</button>
          {/each}
        </div>

        {#if isLoading || isSearching}
          {#each Array(3) as _}
            <div class="hotel-card skeleton">
              <div class="sk-img"></div>
              <div class="sk-body">
                <div class="sk-line"></div>
                <div class="sk-line short"></div>
                <div class="sk-line medium"></div>
              </div>
            </div>
          {/each}

        {:else if hotelsRaw.length === 0}
          <div class="no-results">
            <div class="no-results-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            </div>
            <h2>No encontramos hoteles</h2>
            <p>Intenta con otro país, ciudad o fechas diferentes.</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>Nueva búsqueda</button>
          </div>

        {:else if filteredHotels.length === 0}
          <div class="no-results">
            <div class="no-results-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            </div>
            <h2>No hay hoteles con esos filtros</h2>
            <p>Intenta ajustar los filtros de la izquierda</p>
            <button class="btn-primary" on:click={resetFilters}>Limpiar filtros</button>
          </div>

        {:else}
          <div class="hotels-grid" class:list-view={viewMode === 'list'} class:grid-view={viewMode === 'grid'}>
            {#each filteredHotels as hotel (hotel.id)}
              {@const minPrice  = getMinPrice(hotel)}
              {@const comboHabs = getComboHabs(hotel)}
              {@const comboAprox = getComboAproximado(hotel, cantidadPersonas)}
              {@const comboTotal = comboHabs ? sumPrecios(comboHabs) : null}
              {@const aproxTotal = comboAprox ? sumPrecios(comboAprox.habs) : null}
              {@const desde     = getDesde(hotel)}

              <div class="hotel-card"
                role="button"
                tabindex="0"
                on:click={() => navigateTo('hotel-detail', { hotel, cantidadPersonas, fechaCheckIn, fechaCheckOut })}
                on:keydown={e => e.key === 'Enter' && navigateTo('hotel-detail', { hotel, cantidadPersonas, fechaCheckIn, fechaCheckOut })}>

                <!-- GALERÍA -->
                <div class="hotel-gallery">
                  {#if hotel.imagenesIds && hotel.imagenesIds.length > 0}
                    <img
                      src="{API}/imagenes/hotel/{hotel.imagenesIds[0]}"
                      alt={hotel.nombre}
                      class="hotel-img-real"
                      on:error={e => {
                        e.currentTarget.style.display = 'none';
                        e.currentTarget.nextElementSibling.style.display = 'flex';
                      }}
                    />
                    <div class="hotel-img-placeholder" style="display:none">
                      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      <p class="img-count">Sin imagenes aun</p>
                    </div>
                  {:else}
                    <div class="hotel-img-placeholder">
                      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      <p class="img-count">Sin imagenes aun</p>
                    </div>
                  {/if}
                  {#if hotel.estado === 'Activo'}
                    <div class="hotel-estado-badge hotel-estado-badge--activo">Disponible</div>
                  {/if}
                  <!-- Badge de aproximado encima de la imagen -->
                  {#if comboAprox}
                    <div class="hotel-estado-badge hotel-estado-badge--aprox" style="left:auto;right:0.75rem;">
                      Opción cercana · {comboAprox.capacidadTotal} pers.
                    </div>
                  {/if}
                </div>

                <div class="hotel-content">
                  <div class="hotel-hdr">
                    <div class="hotel-title-wrap">
                      <h2 class="hotel-name">{hotel.nombre}</h2>
                      <div class="hotel-loc">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                        {hotel.direccion}
                      </div>
                    </div>
                    {#if hotel.rating}
                      <div class="rating-box">
                        <div class="rating-score">{hotel.rating.toFixed(1)}</div>
                        <div class="rating-text">{hotel.rating >= 4.8 ? 'Extraordinario' : hotel.rating >= 4.5 ? 'Fabuloso' : hotel.rating >= 4 ? 'Muy bueno' : 'Bueno'}</div>
                      </div>
                    {/if}
                  </div>

                  <p class="hotel-desc">{hotel.descripcion}</p>

                  {#if hotel.amenidades && hotel.amenidades.length > 0}
                    <div class="amenities-row">
                      {#each hotel.amenidades.slice(0, 5) as am}
                        <span class="amenity-pill">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d={amenidadIcon(am.nombre)}/></svg>
                          {am.nombre}
                        </span>
                      {/each}
                      {#if hotel.amenidades.length > 5}
                        <span class="amenity-more">+{hotel.amenidades.length - 5} mas</span>
                      {/if}
                    </div>
                  {/if}

                  {#if hotel.habitaciones && hotel.habitaciones.length > 0}
                    <div class="habitaciones-preview">
                      <p class="habitaciones-label">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M2 4v16M22 4v16M2 8h20M2 16h20M6 8v8M10 8v8M14 8v8M18 8v8"/></svg>
                        {hotel.habitaciones.length} tipo{hotel.habitaciones.length !== 1 ? 's' : ''} de habitacion disponible{hotel.habitaciones.length !== 1 ? 's' : ''}
                      </p>
                      <div class="hab-chips">
                        {#each hotel.habitaciones.slice(0, 3) as hab}
                          <span class="hab-chip">
                            {hab.tipoHabitacion}
                            <span class="hab-chip-price">{fmt(hab.precioPorNoche)}/noche</span>
                          </span>
                        {/each}
                        {#if hotel.habitaciones.length > 3}
                          <span class="hab-chip hab-chip--more">+{hotel.habitaciones.length - 3} mas</span>
                        {/if}
                      </div>
                    </div>
                  {/if}

                  <!-- FOOTER DE PRECIOS -->
                  <div class="hotel-footer">
                    <div class="pricing">

                      <!-- Badge "Desde" unificado -->
                      {#if desde !== null}
                        <div class="desde-badge">
                          <span class="desde-lbl">Desde</span>
                          <span class="desde-precio">{fmt(desde)}</span>
                          <span class="desde-sub">/ noche · {fmt(desde * nights)} por {nights} noche{nights !== 1 ? 's' : ''}</span>
                        </div>
                      {/if}

                      <div class="price-boxes">

                        <!-- Bloque habitación directa -->
                        {#if minPrice !== null}
                          <div class="price-box">
                            <div class="price-box-label">Habitación directa</div>
                            <div class="curr-price">
                              <span class="price-amount">{fmt(minPrice)}</span>
                              <span class="price-lbl">/ noche</span>
                            </div>
                            <div class="per-night">{fmt(minPrice * nights)} por {nights} noche{nights !== 1 ? 's' : ''}</div>
                          </div>
                        {/if}

                        {#if minPrice !== null && comboHabs}
                          <div class="price-box-divider">ó</div>
                        {/if}

                        <!-- Bloque combinación exacta del backend -->
                        {#if comboHabs}
                          <div class="price-box price-box--combo">
                            <div class="price-box-label">Combinación de habitaciones</div>
                            {#each comboHabs as hab, i}
                              <div class="combo-hab-row">
                                <span class="combo-hab-name">
                                  Hab.{i + 1} · {hab.tipo}
                                  <span class="combo-cap">({hab.cap} pers.)</span>
                                </span>
                                <span class="combo-hab-price">{fmt(hab.precio)}<span class="price-lbl">/noche</span></span>
                              </div>
                            {/each}
                            <div class="combo-total">
                              Total: <strong>{fmt(comboTotal)}/noche</strong>
                              · {fmt(comboTotal * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                            </div>
                          </div>
                        {/if}

                        <!-- Bloque combinación APROXIMADA (solo si no hay nada exacto) -->
                        {#if comboAprox}
                          <div class="price-box price-box--aprox">
                            <div class="price-box-label">
                              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="vertical-align:-1px;margin-right:3px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                              Opción cercana — {comboAprox.capacidadTotal} de {cantidadPersonas} pers.
                            </div>
                            {#each comboAprox.habs as hab, i}
                              <div class="combo-hab-row">
                                <span class="combo-hab-name">
                                  Hab.{i + 1} · {hab.tipo}
                                  <span class="combo-cap">({hab.cap} pers.)</span>
                                </span>
                                <span class="combo-hab-price">{fmt(hab.precio)}<span class="price-lbl">/noche</span></span>
                              </div>
                            {/each}
                            <div class="combo-total">
                              Total: <strong>{fmt(aproxTotal)}/noche</strong>
                              · {fmt(aproxTotal * nights)} por {nights} noche{nights !== 1 ? 's' : ''}
                            </div>
                          </div>
                        {/if}

                        <!-- Sin ninguna opción -->
                        {#if minPrice === null && !comboHabs && !comboAprox}
                          <div class="price-box">
                            <div class="price-box-label">Precio a consultar</div>
                          </div>
                        {/if}

                      </div>

                      <button class="btn-view" on:click|stopPropagation={() => navigateTo('hotel-detail', { hotel, cantidadPersonas, fechaCheckIn, fechaCheckOut })}>
                        Ver disponibilidad
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                      </button>
                    </div>
                  </div>

                </div>
              </div>
            {/each}
          </div>
        {/if}

      </main>
    </div>
  </div>
</div>