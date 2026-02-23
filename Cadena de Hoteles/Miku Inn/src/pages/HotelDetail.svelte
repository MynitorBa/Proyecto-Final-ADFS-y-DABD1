<script>
  import { onMount } from 'svelte';
  import '../styles/hoteldetail.css';
  import CommentNode from './CommentNode.svelte';

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

  $: habitacionesDisponibles = hotel?.habitaciones || [];

  // ── UI state ─────────────────────────────────────────────
  let activeTab         = 'overview';
  let selectedRoom      = null;
  let currentImageIndex = 0;
  let showImageGallery  = false;

  // ── Combinaciones ─────────────────────────────────────────
  // combinacionesSeleccionadas: array de { capRequerida, habitacionId, habitacion }
  // una por cada slot de la combinación activa
  let comboActivo = null; // null | { tipo: 'exacta', slots: [...] } | { tipo: 'aproximada', slots: [...] }

  /**
   * Construye las combinaciones sugeridas desde el API.
   * Cada combinación es un array de capacidades, p.ej. [2, 2].
   * Para cada capacidad devolvemos las habitaciones disponibles con esa capacidad.
   */
  $: combosSugeridos = (() => {
    if (!hotel) return [];
    const combNums = hotel.combinacionesNumericas || [];
    return combNums.map(combo => {
      const slots = combo.map(cap => {
        const opciones = (hotel.habitacionesPorCapacidad?.[String(cap)] || []);
        return { capRequerida: cap, opciones, seleccionada: opciones[0] || null };
      });
      return { slots, esAproximada: false };
    });
  })();

  /**
   * Combinación aproximada cuando no hay exactas ni habitaciones directas.
   * Misma lógica que en SearchResults.
   */
  $: comboAproximada = (() => {
    if (!hotel) return null;
    const tieneDirecta = hotel.habitaciones && hotel.habitaciones.length > 0;
    const tieneExacta  = (hotel.combinacionesNumericas || []).length > 0;
    if (tieneDirecta || tieneExacta) return null;

    const porCapacidad = hotel.habitacionesPorCapacidad;
    if (!porCapacidad || Object.keys(porCapacidad).length === 0) return null;

    const todasHabs = [];
    for (const [capStr, rooms] of Object.entries(porCapacidad)) {
      const cap = Number(capStr);
      for (const room of rooms) {
        todasHabs.push({ ...room, cap });
      }
    }
    todasHabs.sort((a, b) => b.cap - a.cap);

    let sumCap  = 0;
    const selec = [];
    const limite = cantidadPersonas + 2;

    for (const hab of todasHabs) {
      if (sumCap >= cantidadPersonas) break;
      selec.push(hab);
      sumCap += hab.cap;
    }

    if (sumCap < cantidadPersonas || sumCap > limite) return null;
    if (selec.length <= 1) return null;

    // Construir slots para la UI
    const slots = selec.map(hab => {
      const capStr = String(hab.cap);
      const opciones = hotel.habitacionesPorCapacidad?.[capStr] || [];
      return { capRequerida: hab.cap, opciones, seleccionada: hab };
    });
    return { slots, esAproximada: true, capacidadTotal: sumCap };
  })();

  // Inicializar comboActivo con el primer combo exacto si existe
  $: if (combosSugeridos.length > 0 && comboActivo === null) {
    comboActivo = deepCloneCombo(combosSugeridos[0]);
  } else if (combosSugeridos.length === 0 && comboAproximada && comboActivo === null) {
    comboActivo = deepCloneCombo(comboAproximada);
  }

  function deepCloneCombo(combo) {
    return {
      ...combo,
      slots: combo.slots.map(s => ({ ...s, seleccionada: s.seleccionada }))
    };
  }

  function selectComboSugerido(idx) {
    comboActivo = deepCloneCombo(combosSugeridos[idx]);
  }

  function selectComboAproximado() {
    if (comboAproximada) comboActivo = deepCloneCombo(comboAproximada);
  }

  function cambiarHabEnSlot(slotIdx, habitacion) {
    if (!comboActivo) return;
    comboActivo.slots[slotIdx].seleccionada = habitacion;
    comboActivo = { ...comboActivo, slots: [...comboActivo.slots] };
  }

  /**
   * Devuelve true si una opción debe bloquearse en un slot dado.
   * Solo se bloquea si ESA habitación ya está seleccionada en OTRO slot
   * Y ese otro slot tiene más de una opción (para no dejar al usuario sin elección).
   */
  function esOpcionBloqueada(comboActivo, slotIdx, opcionId) {
    if (!comboActivo) return false;
    return comboActivo.slots.some((s, i) => {
      if (i === slotIdx) return false;
      if (s.seleccionada?.id !== opcionId) return false;
      // Solo bloquear si el otro slot tiene más de 1 opción (así puede cambiar)
      return s.opciones.length > 1;
    });
  }

  // Personas distribuidas entre slots (reactivo)
  $: personasPorSlotActivo = comboActivo
    ? distribuirPersonas(comboActivo.slots, cantidadPersonas)
    : [];

  // Precio total por noche sin personas
  $: comboTotalPrecio = comboActivo
    ? comboActivo.slots.reduce((sum, s) => sum + (s.seleccionada?.precioPorNoche || 0), 0)
    : 0;

  // Precio total por noche CON personas distribuidas
  $: comboTotalConPersonas = comboActivo
    ? comboActivo.slots.reduce((sum, s, i) => {
        const h = s.seleccionada;
        if (!h) return sum;
        const personas = personasPorSlotActivo[i] || 0;
        return sum + h.precioPorNoche + h.precioPorPersona * personas;
      }, 0)
    : 0;

  // ── Precio habitación individual ─────────────────────────
  $: totalPrice = selectedRoom
    ? (selectedRoom.precioPorNoche + selectedRoom.precioPorPersona * cantidadPersonas) * nights
    : 0;

  // ── Modo de reserva ───────────────────────────────────────
  // 'single' | 'combo'
  let bookMode = 'single';

  // ── Imágenes ─────────────────────────────────────────────
  $: images = (() => {
    if (!hotel) return [];
    const imgs = [];
    if (hotel.imagenesIds?.length > 0) {
      for (const imgId of hotel.imagenesIds) imgs.push(`${API}/imagenes/hotel/${imgId}`);
    }
    if (hotel.habitaciones?.length > 0) {
      for (const room of hotel.habitaciones) {
        if (room.imagenesIds?.length > 0) {
          for (const imgId of room.imagenesIds) imgs.push(`${API}/imagenes/habitacion/${imgId}`);
        }
      }
    }
    if (hotel.amenidades?.length > 0) {
      for (const am of hotel.amenidades) {
        if (am.imagenesIds?.length > 0) {
          for (const imgId of am.imagenesIds) imgs.push(`${API}/imagenes/amenidad/${imgId}`);
        }
      }
    }
    return imgs;
  })();

  function roomImage(room) {
    if (room.imagenesIds?.length > 0) return `${API}/imagenes/habitacion/${room.imagenesIds[0]}`;
    return null;
  }

  function amenityImage(amenidad) {
    if (amenidad.imagenesIds?.length > 0) return `${API}/imagenes/amenidad/${amenidad.imagenesIds[0]}`;
    return null;
  }

  // ── Iconos amenidades ─────────────────────────────────────
  const amenityIcons = {
    'wifi':'📶','piscina':'🏊','gimnasio':'💪','estacionamiento':'🅿️',
    'restaurante':'🍽️','spa':'💆','bar':'🍹','desayuno':'🍳','default':'✨',
  };
  function getAmenityIcon(nombre) {
    const key = nombre.toLowerCase();
    for (const k of Object.keys(amenityIcons)) { if (key.includes(k)) return amenityIcons[k]; }
    return amenityIcons.default;
  }

  // ── Gallery ───────────────────────────────────────────────
  function openGallery(index = 0) {
    currentImageIndex = index; showImageGallery = true;
    document.body.style.overflow = 'hidden';
  }
  function closeGallery() { showImageGallery = false; document.body.style.overflow = 'auto'; }
  function nextImage() { currentImageIndex = (currentImageIndex + 1) % images.length; }
  function prevImage() { currentImageIndex = (currentImageIndex - 1 + images.length) % images.length; }

  // ── Selección habitación individual ──────────────────────
  function selectRoom(room) {
    selectedRoom = room;
    bookMode = 'single';
    document.querySelector('.booking-summary')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // ── Estado booking ────────────────────────────────────────
  let booking     = false;
  let bookError   = '';
  let reservacion = null;

  // ── Validar fechas ────────────────────────────────────────
  function validarFechas() {
    if (!checkInDate || !checkOutDate) {
      bookError = 'Por favor selecciona las fechas de check-in y check-out.';
      return false;
    }
    if (new Date(checkOutDate) <= new Date(checkInDate)) {
      bookError = 'El check-out debe ser al menos un día después del check-in.';
      return false;
    }
    return true;
  }

  // ── Reservar habitación individual ────────────────────────
  async function bookNow() {
    if (!selectedRoom) return;
    if (!validarFechas()) return;
    bookError = ''; booking = true; reservacion = null;
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
        let msg = 'No se pudo completar la reservación.';
        try {
          const data = await res.json();
          msg = data.mensaje || data.message || data.error || msg;
        } catch(_) {}
        throw new Error(msg);
      }
      reservacion = await res.json();
      reservacion._modo = 'single';
    } catch(e) {
      bookError = e.message || 'Error al crear la reservación';
    } finally {
      booking = false;
    }
  }

  // ── Reservar combinación ──────────────────────────────────
  async function bookCombo() {
    if (!comboActivo) return;
    if (!validarFechas()) return;

    // Verificar que todos los slots tienen habitación seleccionada
    const slotsInvalidos = comboActivo.slots.filter(s => !s.seleccionada);
    if (slotsInvalidos.length > 0) {
      bookError = 'Por favor selecciona una habitación para cada slot.';
      return;
    }

    // Verificar que no hay duplicados
    const ids = comboActivo.slots.map(s => s.seleccionada.id);
    const idsUnicos = new Set(ids);
    if (idsUnicos.size !== ids.length) {
      bookError = 'No puedes seleccionar la misma habitación dos veces.';
      return;
    }

    bookError = ''; booking = true; reservacion = null;

    // Distribuir personas entre habitaciones proporcionalmente
    const personasPorSlot = distribuirPersonas(comboActivo.slots, cantidadPersonas);

    try {
      const res = await fetch(`${API}/reservaciones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          habitaciones: comboActivo.slots.map((s, i) => ({
            habitacionId:    s.seleccionada.id,
            cantidadPersonas: personasPorSlot[i],
            fechaCheckIn:    checkInDate,
            fechaCheckOut:   checkOutDate,
          }))
        }),
      });
      if (!res.ok) {
        let msg = 'No se pudo completar la reservación.';
        try {
          const data = await res.json();
          msg = data.mensaje || data.message || data.error || msg;
        } catch(_) {}
        throw new Error(msg);
      }
      reservacion = await res.json();
      reservacion._modo = 'combo';
      reservacion._comboSlots = comboActivo.slots.map(s => s.seleccionada);
    } catch(e) {
      bookError = e.message || 'Error al crear la reservación';
    } finally {
      booking = false;
    }
  }

  /**
   * Distribuye cantidadPersonas entre slots respetando la capacidad máxima de cada uno.
   */
  function distribuirPersonas(slots, total) {
    const result = new Array(slots.length).fill(0);
    let restante = total;
    for (let i = 0; i < slots.length && restante > 0; i++) {
      const cap = slots[i].seleccionada?.capacidadMaxima || slots[i].capRequerida || 1;
      const asig = Math.min(cap, restante);
      result[i] = asig;
      restante -= asig;
    }
    // Si aún quedan personas, distribuir en el primer slot
    if (restante > 0) result[0] += restante;
    return result;
  }

  // ── Ir a pagar ────────────────────────────────────────────
  function goToCheckout() {
    if (reservacion._modo === 'combo') {
      navigateTo('checkout', {
        pendingReservations: [{
          ...reservacion,
          _hotel:    hotel,
          _rooms:    reservacion._comboSlots,
          _checkIn:  checkInDate,
          _checkOut: checkOutDate,
          _nights:   nights,
          _guests:   cantidadPersonas,
          _isCombo:  true,
        }],
      });
    } else {
      navigateTo('checkout', {
        pendingReservations: [{
          ...reservacion,
          _hotel:    hotel,
          _room:     selectedRoom,
          _checkIn:  checkInDate,
          _checkOut: checkOutDate,
          _nights:   nights,
          _guests:   cantidadPersonas,
        }],
      });
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

  function handleImgError(e) {
    /** @type {HTMLImageElement} */ (e.target).style.display = 'none';
  }

  // ── Comentarios ───────────────────────────────────────────
  /** @type {any[]} */
  let comentarios   = [];
  /** @type {Map<number,number>} */
  let misDowns      = new Map();
  let comentLoading = false;

  /** @type {Record<number,string>} */
  let replyTexts  = {};
  /** @type {Record<number,boolean>} */
  let replyOpen   = {};
  /** @type {Record<number,boolean>} */
  let replySaving = {};

  $: if (activeTab === 'comments' && hotel?.id) { loadComentarios(); }

  async function loadComentarios() {
    comentLoading = true;
    try {
      const [cRes, dRes] = await Promise.all([
        fetch(`${API}/comentarios/hotel/${hotel.id}`, { credentials: 'include' }),
        fetch(`${API}/downs/hotel/${hotel.id}`,       { credentials: 'include' }),
      ]);
      if (cRes.ok) comentarios = await cRes.json();
      if (dRes.ok) {
        const downs = await dRes.json();
        misDowns = new Map(downs.map((/** @type {any} */ d) => [d.comentarioId, d.valor]));
      }
    } catch(_) {}
    comentLoading = false;
  }

  $: resenasRaiz    = comentarios.filter(c => c.comentarioPadreId === null && c.resena !== null);
  $: comentariosRaiz = comentarios.filter(c => c.comentarioPadreId === null && c.resena === null);

  /** @param {number} id */
  function getRespuestas(id) {
    return comentarios.filter(c => c.comentarioPadreId === id);
  }

  /**
   * @param {number} comentarioId
   * @param {1|-1} valor
   */
  async function handleDown(comentarioId, valor) {
    const actual = misDowns.get(comentarioId);
    try {
      if (actual === undefined) {
        await fetch(`${API}/comentarios/${comentarioId}/downs`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ valor }),
        });
      } else if (actual === valor) {
        await fetch(`${API}/comentarios/${comentarioId}/downs`, {
          method: 'DELETE', credentials: 'include',
        });
      } else {
        await fetch(`${API}/comentarios/${comentarioId}/downs`, {
          method: 'PATCH', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ valor }),
        });
      }
    } catch(_) {}
    await loadComentarios();
  }

  /** @param {number} parentId */
  async function sendReply(parentId) {
    const texto = (replyTexts[parentId] || '').trim();
    if (!texto) return;
    replySaving[parentId] = true;
    try {
      const res = await fetch(`${API}/comentarios`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ hotelId: hotel.id, comentarioPadreId: parentId, contenido: texto }),
      });
      if (res.ok) {
        replyTexts[parentId] = '';
        replyOpen[parentId]  = false;
        await loadComentarios();
      }
    } catch(_) {}
    replySaving[parentId] = false;
  }

  async function sendReplyFromNode({ parentId, contenido, done }) {
    try {
      const res = await fetch(`${API}/comentarios`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ hotelId: hotel.id, comentarioPadreId: parentId, contenido }),
      });
      if (res.ok) {
        await loadComentarios();
        done(true);
      } else {
        done(false);
      }
    } catch(_) {
      done(false);
    }
  }

  /** @param {number} n */
  function starLabel(n) {
    return ['','Muy malo','Malo','Regular','Bueno','Excelente'][n] || '';
  }

  // ── Helpers combinaciones ─────────────────────────────────
  $: hayCombosSugeridos  = combosSugeridos.length > 0;
  $: hayComboAproximado  = !!comboAproximada;
  $: hayCombinaciones    = hayCombosSugeridos || hayComboAproximado;

  // Habitaciones "superiores" (capacidad >= cantidadPersonas pero sin ser combo)
  $: habitacionesSuperiores = (() => {
    if (!hotel) return [];
    const directas = hotel.habitaciones || [];
    const porCap   = hotel.habitacionesPorCapacidad || {};
    const todas    = [...directas];
    // Agregar habitaciones de porCapacidad que no estén en directas
    for (const rooms of Object.values(porCap)) {
      for (const r of rooms) {
        if (!todas.find(x => x.id === r.id)) todas.push(r);
      }
    }
    return todas.filter(r => r.capacidadMaxima >= cantidadPersonas && !directas.find(d => d.id === r.id));
  })();
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
              { id: 'overview',  label: 'Descripción'  },
              { id: 'rooms',     label: 'Habitaciones' },
              { id: 'location',  label: 'Ubicación'    },
              { id: 'comments',  label: 'Comentarios'  },
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
                        <img src={amenityImage(am)} alt={am.nombre} class="amenity-image" on:error={(e) => { /** @type {HTMLImageElement} */ (e.target).style.display = 'none'; }} />
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

            <!-- ═══ HABITACIONES INDIVIDUALES DISPONIBLES ═══ -->
            {#if habitacionesDisponibles.length > 0}
              <section class="content-section">
                <h2 class="hdet__section-title">Habitaciones Disponibles</h2>
                <p class="hdet__section-description">
                  Habitaciones con capacidad para {cantidadPersonas} {cantidadPersonas === 1 ? 'persona' : 'personas'}.
                </p>
                <div class="rooms-list">
                  {#each habitacionesDisponibles as room}
                    <article class="room-detail-card" class:selected={selectedRoom?.id === room.id && bookMode === 'single'}>
                      <div class="room-images-section">
                        {#if room.imagenesIds?.length > 0}
                          <img src={roomImage(room)} alt={room.tipoHabitacion} class="room-main-image" on:error={(e) => { /** @type {HTMLImageElement} */ (e.target).parentElement.innerHTML = `<div class="room-no-image"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg><span>Sin imagen disponible</span></div>`; }} />
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
                            <div class="price-per-person price-per-person--prominent">
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                              + {fmt(room.precioPorPersona)} / persona
                            </div>
                            {#if nights > 0}
                              <div class="total-nights-price">
                                Total estimado: {fmt((room.precioPorNoche + room.precioPorPersona * cantidadPersonas) * nights)}
                                {#if nights > 1}· {nights} noches{/if}
                              </div>
                            {/if}
                          </div>
                          <button class="btn-select-room" class:selected={selectedRoom?.id === room.id && bookMode === 'single'} on:click={() => selectRoom(room)}>
                            {#if selectedRoom?.id === room.id && bookMode === 'single'}
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
              </section>
            {/if}

            <!-- ═══ HABITACIONES CON CAPACIDAD SUPERIOR ═══ -->
            {#if habitacionesSuperiores.length > 0}
              <section class="content-section hdet__superior-section">
                <div class="hdet__superior-header">
                  <div class="hdet__superior-badge">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    Capacidad superior
                  </div>
                  <h2 class="hdet__section-title">Habitaciones para más huéspedes</h2>
                  <p class="hdet__section-description">
                    Estas habitaciones tienen una capacidad mayor a {cantidadPersonas} personas. Puedes reservarlas si deseas más espacio.
                  </p>
                </div>
                <div class="rooms-list">
                  {#each habitacionesSuperiores as room}
                    <article class="room-detail-card room-detail-card--superior" class:selected={selectedRoom?.id === room.id && bookMode === 'single'}>
                      <div class="room-images-section">
                        {#if room.imagenesIds?.length > 0}
                          <img src={roomImage(room)} alt={room.tipoHabitacion} class="room-main-image" on:error={(e) => { (e.target).parentElement.innerHTML = `<div class="room-no-image"><span>Sin imagen</span></div>`; }} />
                        {:else}
                          <div class="room-no-image">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                            <span>Sin imagen disponible</span>
                          </div>
                        {/if}
                        <div class="room-capacity-badge room-capacity-badge--superior">
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
                            <div class="price-per-person price-per-person--prominent">
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                              + {fmt(room.precioPorPersona)} / persona
                            </div>
                            {#if nights > 0}
                              <div class="total-nights-price">
                                Total estimado: {fmt((room.precioPorNoche + room.precioPorPersona * cantidadPersonas) * nights)}
                                {#if nights > 1}· {nights} noches{/if}
                              </div>
                            {/if}
                          </div>
                          <button class="btn-select-room" class:selected={selectedRoom?.id === room.id && bookMode === 'single'} on:click={() => selectRoom(room)}>
                            {#if selectedRoom?.id === room.id && bookMode === 'single'}
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
              </section>
            {/if}

            <!-- ═══ SIN HABITACIONES DISPONIBLES ═══ -->
            {#if habitacionesDisponibles.length === 0 && habitacionesSuperiores.length === 0 && !hayCombinaciones}
              <section class="content-section">
                <h2 class="hdet__section-title">Habitaciones</h2>
                <div class="hdet__no-rooms">
                  <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                  <p>Este hotel no tiene habitaciones disponibles para tu búsqueda.</p>
                  <span>Intenta con otras fechas o un número diferente de huéspedes.</span>
                </div>
              </section>
            {/if}

            <!-- ═══ CREAR TU COMBINACIÓN ═══ -->
            {#if hayCombinaciones}
              <section class="content-section hdet__combo-section">
                <div class="hdet__combo-header">
                  <div class="hdet__combo-badge">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" fill="currentColor"/></svg>
                    Combinación recomendada
                  </div>
                  <h2 class="hdet__section-title">Crea tu combinación de habitaciones</h2>
                  <p class="hdet__section-description">
                    Para {cantidadPersonas} {cantidadPersonas === 1 ? 'persona' : 'personas'}, el hotel sugiere combinar varias habitaciones.
                    Puedes elegir qué habitación va en cada slot.
                  </p>
                </div>

                <!-- Selector de combinación cuando hay varias exactas -->
                {#if combosSugeridos.length > 1}
                  <div class="hdet__combo-selector">
                    <p class="hdet__combo-selector-label">Elige una combinación sugerida:</p>
                    <div class="hdet__combo-selector-btns">
                      {#each combosSugeridos as combo, idx}
                        <button
                          class="hdet__combo-selector-btn"
                          class:active={comboActivo && !comboActivo.esAproximada && JSON.stringify(comboActivo.slots.map(s=>s.capRequerida)) === JSON.stringify(combo.slots.map(s=>s.capRequerida))}
                          on:click={() => selectComboSugerido(idx)}
                        >
                          {combo.slots.map(s => s.capRequerida + ' pers.').join(' + ')}
                        </button>
                      {/each}
                      {#if hayComboAproximado}
                        <button
                          class="hdet__combo-selector-btn hdet__combo-selector-btn--aprox"
                          class:active={comboActivo?.esAproximada}
                          on:click={selectComboAproximado}
                        >
                          Opción cercana ({comboAproximada.capacidadTotal} pers.)
                        </button>
                      {/if}
                    </div>
                  </div>
                {:else if hayComboAproximado && combosSugeridos.length === 0}
                  <div class="hdet__combo-aprox-notice">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    No hay combinación exacta para {cantidadPersonas} personas. Mostramos la opción más cercana
                    con capacidad para {comboAproximada.capacidadTotal} personas.
                  </div>
                {/if}

                <!-- Slots de la combinación activa -->
                {#if comboActivo}
                  <div class="hdet__combo-slots">
                    {#each comboActivo.slots as slot, slotIdx}
                      {@const personasEnSlot = personasPorSlotActivo[slotIdx] || 0}
                      {@const precioSlotConPersonas = slot.seleccionada
                        ? slot.seleccionada.precioPorNoche + slot.seleccionada.precioPorPersona * personasEnSlot
                        : 0}
                      <div class="hdet__combo-slot">
                        <div class="hdet__combo-slot-header">
                          <div class="hdet__combo-slot-num">Hab. {slotIdx + 1}</div>
                          <div class="hdet__combo-slot-cap">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                            {slot.capRequerida} {slot.capRequerida === 1 ? 'persona' : 'personas'}
                          </div>
                          {#if personasEnSlot > 0}
                            <div class="hdet__combo-slot-personas">
                              {personasEnSlot} {personasEnSlot === 1 ? 'huésped' : 'huéspedes'} asignados
                            </div>
                          {/if}
                        </div>

                        <!-- Opciones para este slot -->
                        <div class="hdet__combo-slot-opciones">
                          {#each slot.opciones as opcion}
                            {@const bloqueada = esOpcionBloqueada(comboActivo, slotIdx, opcion.id)}
                            <button
                              class="hdet__combo-slot-opcion"
                              class:active={slot.seleccionada?.id === opcion.id}
                              class:blocked={bloqueada}
                              disabled={bloqueada}
                              on:click={() => cambiarHabEnSlot(slotIdx, opcion)}
                              title={bloqueada ? 'Seleccionada en otro slot (cámbiala allí primero)' : opcion.tipoHabitacion}
                            >
                              <div class="hdet__combo-opcion-img">
                                {#if opcion.imagenesIds?.length > 0}
                                  <img src="{API}/imagenes/habitacion/{opcion.imagenesIds[0]}" alt={opcion.tipoHabitacion} on:error={handleImgError} />
                                {:else}
                                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                                {/if}
                              </div>
                              <div class="hdet__combo-opcion-info">
                                <span class="hdet__combo-opcion-tipo">{opcion.tipoHabitacion}</span>
                                <span class="hdet__combo-opcion-cama">{opcion.tipoCama} · {opcion.metrosCuadrados}m²</span>
                                <div class="hdet__combo-opcion-precios">
                                  <span class="hdet__combo-opcion-precio">{fmt(opcion.precioPorNoche)}<span class="hdet__combo-opcion-precio-lbl">/noche</span></span>
                                  <span class="hdet__combo-opcion-ppersona">+ {fmt(opcion.precioPorPersona)}<span class="hdet__combo-opcion-precio-lbl">/persona</span></span>
                                </div>
                              </div>
                              {#if slot.seleccionada?.id === opcion.id}
                                <div class="hdet__combo-opcion-check">
                                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                                </div>
                              {/if}
                              {#if bloqueada}
                                <div class="hdet__combo-opcion-blocked">
                                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                                </div>
                              {/if}
                            </button>
                          {/each}
                        </div>

                        <!-- Resumen del slot seleccionado -->
                        {#if slot.seleccionada}
                          <div class="hdet__combo-slot-resumen">
                            <div class="hdet__combo-slot-resumen-left">
                              <span class="hdet__combo-slot-resumen-name">{slot.seleccionada.tipoHabitacion}</span>
                              <span class="hdet__combo-slot-resumen-breakdown">
                                {fmt(slot.seleccionada.precioPorNoche)} hab. + {fmt(slot.seleccionada.precioPorPersona)} × {personasEnSlot} pers.
                              </span>
                            </div>
                            <span class="hdet__combo-slot-resumen-precio">
                              {fmt(precioSlotConPersonas)}/noche
                              {#if nights > 1}
                                <span class="hdet__combo-slot-resumen-total">· {fmt(precioSlotConPersonas * nights)} total</span>
                              {/if}
                            </span>
                          </div>
                        {/if}
                      </div>
                    {/each}
                  </div>

                  <!-- Resumen total del combo -->
                  <div class="hdet__combo-total-bar">
                    <div class="hdet__combo-total-info">
                      <span class="hdet__combo-total-label">Total combinación</span>
                      <span class="hdet__combo-total-habs">{comboActivo.slots.length} habitaciones · {cantidadPersonas} huéspedes</span>
                    </div>
                    <div class="hdet__combo-total-precios">
                      <span class="hdet__combo-total-precio-noche">{fmt(comboTotalConPersonas)}/noche</span>
                      {#if nights > 1}
                        <span class="hdet__combo-total-precio-total">{fmt(comboTotalConPersonas * nights)} por {nights} noches</span>
                      {/if}
                    </div>
                    <button
                      class="hdet__combo-btn-seleccionar"
                      class:active={bookMode === 'combo'}
                      on:click={() => { bookMode = 'combo'; selectedRoom = null; document.querySelector('.booking-summary')?.scrollIntoView({ behavior: 'smooth', block: 'start' }); }}
                    >
                      {#if bookMode === 'combo'}
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                        Combinación activa
                      {:else}
                        Reservar esta combinación
                      {/if}
                    </button>
                  </div>
                {/if}
              </section>
            {/if}

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

          {:else if activeTab === 'comments'}
            <section class="content-section">
              <h2 class="hdet__section-title">Reseñas y Comentarios</h2>

              {#if comentLoading}
                <div class="cmt-loading">
                  <div class="cmt-spinner"></div>
                  <span>Cargando comentarios...</span>
                </div>
              {:else if comentarios.length === 0}
                <div class="cmt-empty">
                  <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  <p>Este hotel aún no tiene comentarios.</p>
                </div>
              {:else}
                {#if resenasRaiz.length > 0}
                  <h3 class="cmt-group-title">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                    Reseñas de huéspedes
                  </h3>
                  <div class="cmt-list">
                    {#each resenasRaiz as c (c.id)}
                      <CommentNode
                        comment={c}
                        allComments={comentarios}
                        misDowns={misDowns}
                        isReply={false}
                        on:vote={e => handleDown(e.detail.comentarioId, e.detail.valor)}
                        on:reply={e => sendReplyFromNode(e.detail)}
                      />
                    {/each}
                  </div>
                {/if}
                {#if comentariosRaiz.length > 0}
                  <h3 class="cmt-group-title" style="margin-top: 2rem;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    Comentarios
                  </h3>
                  <div class="cmt-list">
                    {#each comentariosRaiz as c (c.id)}
                      <CommentNode
                        comment={c}
                        allComments={comentarios}
                        misDowns={misDowns}
                        isReply={false}
                        on:vote={e => handleDown(e.detail.comentarioId, e.detail.valor)}
                        on:reply={e => sendReplyFromNode(e.detail)}
                      />
                    {/each}
                  </div>
                {/if}
              {/if}
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

            <!-- ── Resumen selección ── -->
            {#if bookMode === 'single' && selectedRoom}
              <div class="booking-section selected-room-section">
                <p class="booking-label">Habitación Seleccionada</p>
                <div class="selected-room-card">
                  <div class="selected-room-info">
                    <strong>{selectedRoom.tipoHabitacion}</strong>
                    <span>{fmt(selectedRoom.precioPorNoche)}/noche</span>
                  </div>
                  <button class="remove-room-btn" on:click={() => { selectedRoom = null; bookMode = 'single'; }} aria-label="Quitar">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                  </button>
                </div>
              </div>
            {:else if bookMode === 'combo' && comboActivo}
              <div class="booking-section selected-room-section">
                <p class="booking-label">Combinación Seleccionada</p>
                <div class="hdet__sidebar-combo">
                  {#each comboActivo.slots as slot, i}
                    {@const personas = personasPorSlotActivo[i] || 0}
                    {@const precioConPersonas = slot.seleccionada
                      ? slot.seleccionada.precioPorNoche + slot.seleccionada.precioPorPersona * personas
                      : 0}
                    <div class="hdet__sidebar-combo-slot">
                      <span class="hdet__sidebar-combo-num">Hab.{i+1}</span>
                      <div class="hdet__sidebar-combo-middle">
                        <span class="hdet__sidebar-combo-name">{slot.seleccionada?.tipoHabitacion || '—'}</span>
                        <span class="hdet__sidebar-combo-personas">{personas} {personas === 1 ? 'huésped' : 'huéspedes'}</span>
                      </div>
                      <span class="hdet__sidebar-combo-precio">{fmt(precioConPersonas)}</span>
                    </div>
                  {/each}
                </div>
                <button class="remove-room-btn" style="margin-top:.5rem;" on:click={() => bookMode = 'single'} aria-label="Quitar combinación">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                </button>
              </div>
            {:else}
              <div class="no-room-selected">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                <p>Selecciona una habitación o combinación para continuar</p>
              </div>
            {/if}

            <!-- Desglose de precios -->
            {#if bookMode === 'single' && selectedRoom && nights > 0}
              <div class="price-summary">
                <div class="price-row">
                  <span>{fmt(selectedRoom.precioPorNoche)}/noche × {nights} {nights === 1 ? 'noche' : 'noches'}</span>
                  <span>{fmt(selectedRoom.precioPorNoche * nights)}</span>
                </div>
                <div class="price-row price-row--persona">
                  <span>{fmt(selectedRoom.precioPorPersona)}/persona × {cantidadPersonas} × {nights}</span>
                  <span>{fmt(selectedRoom.precioPorPersona * cantidadPersonas * nights)}</span>
                </div>
                <div class="hdet__price-divider"></div>
                <div class="price-row total">
                  <span>Total</span>
                  <span class="total-amount">{fmt(totalPrice)}</span>
                </div>
                <div class="taxes-note">Incluye impuestos y cargos</div>
              </div>
            {:else if bookMode === 'combo' && comboActivo && nights > 0}
              <div class="price-summary">
                {#each comboActivo.slots as slot, i}
                  {#if slot.seleccionada}
                    {@const personas = personasPorSlotActivo[i] || 0}
                    {@const precioHab = slot.seleccionada.precioPorNoche * nights}
                    {@const precioPersonas = slot.seleccionada.precioPorPersona * personas * nights}
                    <div class="price-row price-row--hab-label">
                      <span>Hab.{i+1} — {slot.seleccionada.tipoHabitacion}</span>
                    </div>
                    <div class="price-row price-row--sub">
                      <span>{fmt(slot.seleccionada.precioPorNoche)} × {nights}n</span>
                      <span>{fmt(precioHab)}</span>
                    </div>
                    <div class="price-row price-row--sub price-row--persona">
                      <span>{fmt(slot.seleccionada.precioPorPersona)}/pers. × {personas} × {nights}n</span>
                      <span>{fmt(precioPersonas)}</span>
                    </div>
                  {/if}
                {/each}
                <div class="hdet__price-divider"></div>
                <div class="price-row total">
                  <span>Total</span>
                  <span class="total-amount">{fmt(comboTotalConPersonas * nights)}</span>
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
              {#if bookMode === 'single'}
                <button class="btn-book-now" on:click={bookNow} disabled={!selectedRoom || booking}>
                  {booking ? 'Procesando...' : 'Reservar Ahora'}
                </button>
              {:else if bookMode === 'combo'}
                <button class="btn-book-now btn-book-combo" on:click={bookCombo} disabled={!comboActivo || booking}>
                  {booking ? 'Procesando...' : `Reservar ${comboActivo?.slots.length || ''} habitaciones`}
                </button>
              {/if}
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

    <!-- Modal confirmación -->
    {#if reservacion}
      <div class="hdet__confirm-overlay" role="dialog" aria-modal="true">
        <div class="hdet__confirm-modal">
          <div class="hdet__confirm-icon">✓</div>
          <h2 class="hdet__confirm-title">¡Reservación creada!</h2>
          <p class="hdet__confirm-code">{reservacion.noReservacion}</p>
          <div class="hdet__confirm-rows">
            <div class="hdet__confirm-row"><span>Hotel</span><strong>{hotel?.nombre}</strong></div>
            {#if reservacion._modo === 'combo' && reservacion._comboSlots}
              {#each reservacion._comboSlots as r, i}
                <div class="hdet__confirm-row"><span>Hab.{i+1}</span><strong>{r.tipoHabitacion}</strong></div>
              {/each}
            {:else}
              <div class="hdet__confirm-row"><span>Habitación</span><strong>{selectedRoom?.tipoHabitacion}</strong></div>
            {/if}
            <div class="hdet__confirm-row"><span>Check-in</span><strong>{checkInDate}</strong></div>
            <div class="hdet__confirm-row"><span>Check-out</span><strong>{checkOutDate}</strong></div>
            <div class="hdet__confirm-row"><span>Huéspedes</span><strong>{cantidadPersonas}</strong></div>
            <div class="hdet__confirm-row"><span>Estado</span><strong class="hdet__confirm-estado">{reservacion.estado}</strong></div>
            <div class="hdet__confirm-row hdet__confirm-row--total"><span>Total</span><strong>{fmt(reservacion.total)}</strong></div>
          </div>
          <p class="hdet__confirm-expira">Expira: {reservacion.fechaExpiracion}</p>
          <div class="hdet__confirm-btns">
            <button class="hdet__confirm-btn-home" on:click={() => navigateTo('home')}>Volver al inicio</button>
            <button class="hdet__confirm-btn-pay" on:click={goToCheckout}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
              Pagar ahora
            </button>
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