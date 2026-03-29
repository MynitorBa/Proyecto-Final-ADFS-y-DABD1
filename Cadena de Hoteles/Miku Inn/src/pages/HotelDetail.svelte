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

  let datesWarning  = false;
  let fetchingAvail = false;

  function onSidebarDateChange() {
    selectedRoom      = null;
    selectedRoomIsExtra = false;
    bookMode          = 'single';
    comboActivo       = null;
    bookError         = '';
    datesWarning      = true;
  }

  async function refetchDisponibilidad() {
    if (!checkInDate || !checkOutDate) return;
    if (new Date(checkOutDate) <= new Date(checkInDate)) {
      bookError = 'El check-out debe ser posterior al check-in.';
      return;
    }
    if (checkInDate < todayStr) {
      bookError = 'El check-in no puede ser una fecha pasada.';
      return;
    }
    fetchingAvail = true;
    bookError = '';
    try {
      const res = await fetch(`${API}/busqueda`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          pais:             hotel.pais,
          ciudad:           hotel.ciudad,
          fechaCheckIn:     checkInDate,
          fechaCheckOut:    checkOutDate,
          cantidadPersonas: cantidadPersonas
        })
      });
      if (!res.ok) { bookError = 'Error al actualizar disponibilidad.'; return; }
      const hoteles = await res.json();
      const actualizado = hoteles.find(h => h.id === hotel.id);
      if (actualizado) {
        hotel = { ...hotel, ...actualizado };
        comboActivo = null;
        datesWarning = false;
      } else {
        bookError = 'Este hotel no tiene disponibilidad para las fechas seleccionadas.';
      }
    } catch(e) {
      bookError = 'Error de conexión al actualizar disponibilidad.';
    } finally {
      fetchingAvail = false;
    }
  }

  // Solo esto: min reactivo para el checkout (no modifica ninguna variable, solo calcula el mínimo)
  $: minCheckOut = checkInDate
    ? toLocalDateStr(new Date(new Date(checkInDate).getTime() + 86400000))
    : todayStr;

  $: nights = (() => {
    if (!checkInDate || !checkOutDate) return 0;
    return Math.max(0, Math.ceil(
      (Number(new Date(checkOutDate)) - Number(new Date(checkInDate))) / 86400000
    ));
  })();

  $: habitacionesDisponibles = hotel?.tiposHabitacion || [];

  // ── UI state ─────────────────────────────────────────────
  let activeTab         = 'overview';
  let selectedRoom      = null;
  let selectedRoomIsExtra = false;
  let currentImageIndex = 0;
  let showImageGallery  = false;

  // ── Modal de login requerido ──────────────────────────────
  let showLoginRequired = false;

  function promptLogin() {
    showLoginRequired = true;
  }

  function closeLoginPrompt() {
    showLoginRequired = false;
  }

  // ── Combinaciones ─────────────────────────────────────────
  let comboActivo = null;

  $: combosSugeridos = (() => {
    if (!hotel) return [];
    const combNums = hotel.combinacionesNumericas || [];
    return combNums.map(combo => {
      const slots = combo.map(cap => {
        const opciones = (hotel.tiposHabitacionPorCapacidad?.[String(cap)] || []);
        return { capRequerida: cap, opciones, seleccionada: opciones[0] || null };
      });
      return { slots, esAproximada: false };
    });
  })();

  $: comboAproximada = (() => {
    if (!hotel) return null;
    const tieneDirecta = hotel.tiposHabitacion && hotel.tiposHabitacion.length > 0;
    const tieneExacta  = (hotel.combinacionesNumericas || []).length > 0;
    if (tieneDirecta || tieneExacta) return null;

    const porCapacidad = hotel.tiposHabitacionPorCapacidad;
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

    const slots = selec.map(hab => {
      const capStr = String(hab.cap);
      const opciones = hotel.tiposHabitacionPorCapacidad?.[capStr] || [];
      return { capRequerida: hab.cap, opciones, seleccionada: hab };
    });
    return { slots, esAproximada: true, capacidadTotal: sumCap };
  })();

  // ── Helpers combinaciones (declarados antes del reactivo que los usa) ──
  $: hayCombosSugeridos  = combosSugeridos.length > 0;
  $: hayComboAproximado  = !!comboAproximada;
  $: hayCombinaciones    = hayCombosSugeridos || hayComboAproximado;

  // ── Combinaciones especiales (rooms de cap-1 combinadas) ──
  $: combosEspeciales = (() => {
    if (!hotel || cantidadPersonas <= 1) return [];
    if (hayCombinaciones) return [];

    const capTarget = cantidadPersonas - 1;
    const porCap = hotel.tiposHabitacionPorCapacidad || {};
    const rooms = porCap[String(capTarget)] || [];
    const roomsNeeded = Math.ceil(cantidadPersonas / capTarget);
    if (rooms.length < roomsNeeded) return [];

    const combos = [];
    function combine(start, current) {
      if (current.length === roomsNeeded) {
        const slots = current.map(room => ({
          capRequerida: capTarget,
          opciones: rooms,
          seleccionada: room
        }));
        combos.push({ slots, esAproximada: false, esEspecial: true, capacidadTotal: capTarget * roomsNeeded });
        return;
      }
      for (let i = start; i < rooms.length && combos.length < 3; i++) {
        combine(i + 1, [...current, rooms[i]]);
      }
    }
    combine(0, []);
    return combos;
  })();

  $: hayCombosEspeciales = combosEspeciales.length > 0;

  // ── Inicializar comboActivo ───────────────────────────────
  $: if (combosSugeridos.length > 0 && comboActivo === null) {
    comboActivo = deepCloneCombo(combosSugeridos[0]);
  } else if (combosSugeridos.length === 0 && comboAproximada && comboActivo === null) {
    comboActivo = deepCloneCombo(comboAproximada);
  } else if (!hayCombinaciones && combosEspeciales.length > 0 && comboActivo === null) {
    comboActivo = deepCloneCombo(combosEspeciales[0]);
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

  function selectComboEspecial(idx) {
    comboActivo = deepCloneCombo(combosEspeciales[idx]);
  }

  function cambiarHabEnSlot(slotIdx, habitacion) {
    if (!comboActivo) return;
    comboActivo.slots[slotIdx].seleccionada = habitacion;
    comboActivo = { ...comboActivo, slots: [...comboActivo.slots] };
  }

  function esOpcionBloqueada(comboActivo, slotIdx, opcionId) {
    if (!comboActivo) return false;
    return comboActivo.slots.some((s, i) => {
      if (i === slotIdx) return false;
      if (s.seleccionada?.id !== opcionId) return false;
      return s.opciones.length > 1;
    });
  }

  $: personasPorSlotActivo = comboActivo
    ? distribuirPersonas(comboActivo.slots, cantidadPersonas)
    : [];

  $: comboTotalPrecio = comboActivo
    ? comboActivo.slots.reduce((sum, s) => sum + (s.seleccionada?.precioPorNoche || 0), 0)
    : 0;

  $: comboTotalConPersonas = comboActivo
      ? comboActivo.slots.reduce((sum, s) => {
          const h = s.seleccionada;
          if (!h) return sum;
          return sum + h.precioPorNoche;
        }, 0)
      : 0;

  // ── Precio habitación individual ─────────────────────────
  $: totalPrice = selectedRoom
    ? selectedRoomIsExtra
      ? (selectedRoom.precioPorNoche + selectedRoom.precioPorPersona) * nights
      : selectedRoom.precioPorNoche * nights
    : 0;

  // ── Modo de reserva ───────────────────────────────────────
  let bookMode = 'single';

  // ── Imágenes ─────────────────────────────────────────────
  $: images = (() => {
    if (!hotel) return [];
    const imgs = [];
    if (hotel.imagenesIds?.length > 0) {
      for (const imgId of hotel.imagenesIds) imgs.push(`${API}/imagenes/hotel/${imgId}`);
    }
    if (hotel.tiposHabitacion?.length > 0) {
      for (const room of hotel.tiposHabitacion) {
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
  function selectRoom(room, isExtra = false) {
    selectedRoom = room;
    selectedRoomIsExtra = isExtra;
    bookMode = 'single';
    datesWarning = false;
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
    if (checkInDate < todayStr) {
      bookError = 'El check-in no puede ser una fecha pasada.';
      return false;
    }
    if (new Date(checkOutDate) <= new Date(checkInDate)) {
      bookError = 'El check-out debe ser al menos un día después del check-in.';
      return false;
    }
    return true;
  }

  function esErrorDeAutenticacion(status, mensaje) {
    if (status === 401 || status === 403) return true;
    const m = (mensaje || '').toLowerCase();
    if (m.includes('intvalue') && m.includes('null')) return true;
    if (m.includes('no autenticado') || m.includes('no autorizado') || m.includes('sesión') || m.includes('iniciar sesión')) return true;
    return false;
  }

  // ── Reservar habitación individual ────────────────────────
  async function bookNow() {
    if (!selectedRoom) return;
    if (!validarFechas()) return;
    bookError = ''; booking = true; reservacion = null;

    const personasAEnviar = selectedRoomIsExtra
      ? selectedRoom.capacidadMaxima + 1
      : cantidadPersonas;

    // Elegir una habitación disponible aleatoria del tipo seleccionado
    const disponibles = selectedRoom.habitacionesDisponibles || [];
    if (disponibles.length === 0) {
      bookError = 'No hay habitaciones disponibles de este tipo.';
      booking = false;
      return;
    }
    const habitacionElegida = disponibles[Math.floor(Math.random() * disponibles.length)];

    try {
      const res = await fetch(`${API}/reservaciones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          habitaciones: [{
            habitacionId:    habitacionElegida.id,
            cantidadPersonas: personasAEnviar,
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

        if (esErrorDeAutenticacion(res.status, msg)) {
          promptLogin();
          return;
        }
        throw new Error(msg);
      }
      reservacion = await res.json();
      reservacion._modo = 'single';
    } catch(e) {
      if (esErrorDeAutenticacion(0, e.message)) {
        promptLogin();
        return;
      }
      bookError = e.message || 'Error al crear la reservación';
    } finally {
      booking = false;
    }
  }

  // ── Reservar combinación ──────────────────────────────────
  async function bookCombo() {
    if (!comboActivo) return;
    if (!validarFechas()) return;

    const slotsInvalidos = comboActivo.slots.filter(s => !s.seleccionada);
    if (slotsInvalidos.length > 0) {
      bookError = 'Por favor selecciona una habitación para cada slot.';
      return;
    }

    const ids = comboActivo.slots.map(s => s.seleccionada.id);
    const idsUnicos = new Set(ids);
    if (idsUnicos.size !== ids.length) {
      bookError = 'No puedes seleccionar la misma habitación dos veces.';
      return;
    }

    bookError = ''; booking = true; reservacion = null;

    const personasPorSlot = distribuirPersonas(comboActivo.slots, cantidadPersonas);

    // Elegir una habitación disponible aleatoria por cada slot
    const habitacionesPorSlot = comboActivo.slots.map(s => {
      const disponibles = s.seleccionada?.habitacionesDisponibles || [];
      if (disponibles.length === 0) return null;
      return disponibles[Math.floor(Math.random() * disponibles.length)];
    });

    if (habitacionesPorSlot.some(h => !h)) {
      bookError = 'Una o más habitaciones del combo no tienen disponibilidad.';
      booking = false;
      return;
    }

    // Verificar que no se repita el mismo ID de habitación física
    const idsElegidos = habitacionesPorSlot.map(h => h.id);
    if (new Set(idsElegidos).size !== idsElegidos.length) {
      bookError = 'No hay suficientes habitaciones físicas disponibles para esta combinación.';
      booking = false;
      return;
    }

    try {
      const res = await fetch(`${API}/reservaciones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          habitaciones: comboActivo.slots.map((s, i) => ({
            habitacionId:    habitacionesPorSlot[i].id,
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

        if (esErrorDeAutenticacion(res.status, msg)) {
          promptLogin();
          return;
        }
        throw new Error(msg);
      }
      reservacion = await res.json();
      reservacion._modo = 'combo';
      reservacion._comboSlots = comboActivo.slots.map(s => s.seleccionada);
    } catch(e) {
      if (esErrorDeAutenticacion(0, e.message)) {
        promptLogin();
        return;
      }
      bookError = e.message || 'Error al crear la reservación';
    } finally {
      booking = false;
    }
  }

  function distribuirPersonas(slots, total) {
    const result = new Array(slots.length).fill(0);
    let restante = total;
    for (let i = 0; i < slots.length && restante > 0; i++) {
      const cap = slots[i].seleccionada?.capacidadMaxima || slots[i].capRequerida || 1;
      const asig = Math.min(cap, restante);
      result[i] = asig;
      restante -= asig;
    }
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
          _guests:   selectedRoomIsExtra ? selectedRoom.capacidadMaxima + 1 : cantidadPersonas,
          _isPersonaExtra: selectedRoomIsExtra,
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

  function getRespuestas(id) {
    return comentarios.filter(c => c.comentarioPadreId === id);
  }

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

  function starLabel(n) {
    return ['','Muy malo','Malo','Regular','Bueno','Excelente'][n] || '';
  }

  $: habitacionesSuperiores = (() => {
    if (!hotel) return [];
    const directas = hotel.tiposHabitacion || [];
    const porCap   = hotel.tiposHabitacionPorCapacidad || {};
    const todas    = [...directas];
    for (const rooms of Object.values(porCap)) {
      for (const r of rooms) {
        if (!todas.find(x => x.id === r.id)) todas.push(r);
      }
    }
    return todas.filter(r => r.capacidadMaxima >= cantidadPersonas && !directas.find(d => d.id === r.id));
  })();

  // ── Habitaciones con persona extra (+1) ───────────────────
  $: habitacionesPersonaExtra = (() => {
    if (!hotel || cantidadPersonas <= 1) return [];
    const directas = hotel.tiposHabitacion || [];
    const porCap   = hotel.tiposHabitacionPorCapacidad || {};
    const todas    = [...directas];
    for (const rooms of Object.values(porCap)) {
      for (const r of rooms) {
        if (!todas.find(x => x.id === r.id)) todas.push(r);
      }
    }
    const idsDirectas    = new Set(directas.map(h => h.id));
    const idsSuperiores  = new Set(habitacionesSuperiores.map(h => h.id));
    return todas.filter(r =>
      r.capacidadMaxima === cantidadPersonas - 1 &&
      !idsDirectas.has(r.id) &&
      !idsSuperiores.has(r.id)
    );
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
                    <article class="room-detail-card" class:selected={selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && !selectedRoomIsExtra}>
                      <div class="room-images-section">
                        {#if room.imagenesIds?.length > 0}
                          <img src={roomImage(room)} alt={room.tipoHabitacion} class="room-main-image" on:error={(e) => { /** @type {HTMLImageElement} */ /** @type {HTMLImageElement} */ (e.target).parentElement.innerHTML = `<div class="room-no-image"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg><span>Sin imagen disponible</span></div>`; }} />
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
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg> {room.tipoCama}</div>
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg> {room.metrosCuadrados} m²</div>
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg> Máx. {room.capacidadMaxima} huéspedes</div>
                        </div>
                        <div class="room-footer">
                          <div class="room-pricing">
                            <div class="current-price-room"><span class="hdet__price-amount">{fmt(room.precioPorNoche)}</span><span class="price-period">/ noche</span></div>
                            <div class="price-per-person price-per-person--prominent"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> + {fmt(room.precioPorPersona)} / persona</div>
                            {#if nights > 0}<div class="total-nights-price">Total estimado: {fmt(room.precioPorNoche * nights)} {#if nights > 1}· {nights} noches{/if}</div>{/if}
                          </div>
                          <button class="btn-select-room" class:selected={selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && !selectedRoomIsExtra} on:click={() => selectRoom(room, false)}>
                            {#if selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && !selectedRoomIsExtra}<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg> Seleccionada{:else}Seleccionar{/if}
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
                  <div class="hdet__superior-badge"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg> Capacidad superior</div>
                  <h2 class="hdet__section-title">Habitaciones para más huéspedes</h2>
                  <p class="hdet__section-description">Estas habitaciones tienen una capacidad mayor a {cantidadPersonas} personas. Puedes reservarlas si deseas más espacio.</p>
                </div>
                <div class="rooms-list">
                  {#each habitacionesSuperiores as room}
                    <article class="room-detail-card room-detail-card--superior" class:selected={selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && !selectedRoomIsExtra}>
                      <div class="room-images-section">
                        {#if room.imagenesIds?.length > 0}<img src={roomImage(room)} alt={room.tipoHabitacion} class="room-main-image" on:error={(e) => { /** @type {HTMLImageElement} */ (e.target).parentElement.innerHTML = `<div class="room-no-image"><span>Sin imagen</span></div>`; }} />{:else}<div class="room-no-image"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg><span>Sin imagen disponible</span></div>{/if}
                        <div class="room-capacity-badge room-capacity-badge--superior"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle></svg> Máx. {room.capacidadMaxima}</div>
                      </div>
                      <div class="room-content-section">
                        <div><h3 class="room-name">{room.tipoHabitacion}</h3><p class="room-description">{room.descripcion}</p></div>
                        <div class="room-specs">
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg> {room.tipoCama}</div>
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg> {room.metrosCuadrados} m²</div>
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg> Máx. {room.capacidadMaxima} huéspedes</div>
                        </div>
                        <div class="room-footer">
                          <div class="room-pricing">
                            <div class="current-price-room"><span class="hdet__price-amount">{fmt(room.precioPorNoche)}</span><span class="price-period">/ noche</span></div>
                            <div class="price-per-person price-per-person--prominent"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> + {fmt(room.precioPorPersona)} / persona</div>
                            {#if nights > 0}<div class="total-nights-price">Total estimado: {fmt(room.precioPorNoche * nights)} {#if nights > 1}· {nights} noches{/if}</div>{/if}
                          </div>
                          <button class="btn-select-room" class:selected={selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && !selectedRoomIsExtra} on:click={() => selectRoom(room, false)}>
                            {#if selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && !selectedRoomIsExtra}<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg> Seleccionada{:else}Seleccionar{/if}
                          </button>
                        </div>
                      </div>
                    </article>
                  {/each}
                </div>
              </section>
            {/if}

            <!-- ═══ HABITACIONES CON PERSONA EXTRA (+1) ═══ -->
            {#if habitacionesPersonaExtra.length > 0}
              <section class="content-section hdet__superior-section">
                <div class="hdet__superior-header">
                  <div class="hdet__combo-badge"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg> +1 persona extra</div>
                  <h2 class="hdet__section-title">Habitaciones con persona extra</h2>
                  <p class="hdet__section-description">Estas habitaciones tienen capacidad para {cantidadPersonas - 1} {cantidadPersonas - 1 === 1 ? 'persona' : 'personas'}, pero permiten agregar 1 huésped adicional con un cargo por persona extra.</p>
                </div>
                <div class="rooms-list">
                  {#each habitacionesPersonaExtra as room}
                    <article class="room-detail-card room-detail-card--superior" class:selected={selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && selectedRoomIsExtra}>
                      <div class="room-images-section">
                        {#if room.imagenesIds?.length > 0}<img src={roomImage(room)} alt={room.tipoHabitacion} class="room-main-image" on:error={(e) => { /** @type {HTMLImageElement} */ (e.target).parentElement.innerHTML = `<div class="room-no-image"><span>Sin imagen</span></div>`; }} />{:else}<div class="room-no-image"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg><span>Sin imagen disponible</span></div>{/if}
                        <div class="room-capacity-badge room-capacity-badge--superior"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle></svg> {room.capacidadMaxima} + 1 extra</div>
                      </div>
                      <div class="room-content-section">
                        <div><h3 class="room-name">{room.tipoHabitacion}</h3><p class="room-description">{room.descripcion}</p></div>
                        <div class="room-specs">
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg> {room.tipoCama}</div>
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg> {room.metrosCuadrados} m²</div>
                          <div class="spec-item"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg> Máx. {room.capacidadMaxima} + 1 extra</div>
                        </div>
                        <div class="room-footer">
                          <div class="room-pricing">
                            <div class="current-price-room"><span class="hdet__price-amount">{fmt(room.precioPorNoche)}</span><span class="price-period">/ noche</span></div>
                            <div class="price-per-person price-per-person--prominent"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> + {fmt(room.precioPorPersona)} / persona extra</div>
                            {#if nights > 0}<div class="total-nights-price">Total estimado: {fmt((room.precioPorNoche + room.precioPorPersona) * nights)} {#if nights > 1}· {nights} noches{/if} (incluye +1 persona)</div>{/if}
                          </div>
                          <button class="btn-select-room" class:selected={selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && selectedRoomIsExtra} on:click={() => selectRoom(room, true)}>
                            {#if selectedRoom?.tipoHabitacionId === room.tipoHabitacionId && bookMode === 'single' && selectedRoomIsExtra}<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg> Seleccionada{:else}Seleccionar +1 extra{/if}
                          </button>
                        </div>
                      </div>
                    </article>
                  {/each}
                </div>
              </section>
            {/if}

            <!-- ═══ SIN HABITACIONES DISPONIBLES ═══ -->
            {#if habitacionesDisponibles.length === 0 && habitacionesSuperiores.length === 0 && habitacionesPersonaExtra.length === 0 && !hayCombinaciones && !hayCombosEspeciales}
              <section class="content-section">
                <h2 class="hdet__section-title">Habitaciones</h2>
                <div class="hdet__no-rooms">
                  <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                  <p>Este hotel no tiene habitaciones disponibles para tu búsqueda.</p>
                  <span>Intenta con otras fechas o un número diferente de huéspedes.</span>
                </div>
              </section>
            {/if}

            <!-- ═══ CREAR TU COMBINACIÓN (regulares) ═══ -->
            {#if hayCombinaciones}
              <section class="content-section hdet__combo-section">
                <div class="hdet__combo-header">
                  <div class="hdet__combo-badge"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" fill="currentColor"/></svg> Combinación recomendada</div>
                  <h2 class="hdet__section-title">Crea tu combinación de habitaciones</h2>
                  <p class="hdet__section-description">Para {cantidadPersonas} {cantidadPersonas === 1 ? 'persona' : 'personas'}, el hotel sugiere combinar varias habitaciones. Puedes elegir qué habitación va en cada slot.</p>
                </div>

                {#if combosSugeridos.length > 1}
                  <div class="hdet__combo-selector">
                    <p class="hdet__combo-selector-label">Elige una combinación sugerida:</p>
                    <div class="hdet__combo-selector-btns">
                      {#each combosSugeridos as combo, idx}
                        <button class="hdet__combo-selector-btn" class:active={comboActivo && !comboActivo.esAproximada && !comboActivo.esEspecial && JSON.stringify(comboActivo.slots.map(s=>s.capRequerida)) === JSON.stringify(combo.slots.map(s=>s.capRequerida))} on:click={() => selectComboSugerido(idx)}>{combo.slots.map(s => s.capRequerida + ' pers.').join(' + ')}</button>
                      {/each}
                      {#if hayComboAproximado}
                        <button class="hdet__combo-selector-btn hdet__combo-selector-btn--aprox" class:active={comboActivo?.esAproximada} on:click={selectComboAproximado}>Opción cercana ({comboAproximada.capacidadTotal} pers.)</button>
                      {/if}
                    </div>
                  </div>
                {:else if hayComboAproximado && combosSugeridos.length === 0}
                  <div class="hdet__combo-aprox-notice">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    No hay combinación exacta para {cantidadPersonas} personas. Mostramos la opción más cercana con capacidad para {comboAproximada.capacidadTotal} personas.
                  </div>
                {/if}

                {#if comboActivo && !comboActivo.esEspecial}
                  <div class="hdet__combo-slots">
                    {#each comboActivo.slots as slot, slotIdx}
                      {@const personasEnSlot = personasPorSlotActivo[slotIdx] || 0}
                      {@const precioSlotConPersonas = slot.seleccionada ? slot.seleccionada.precioPorNoche : 0}
                      <div class="hdet__combo-slot">
                        <div class="hdet__combo-slot-header">
                          <div class="hdet__combo-slot-num">Hab. {slotIdx + 1}</div>
                          <div class="hdet__combo-slot-cap"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> {slot.capRequerida} {slot.capRequerida === 1 ? 'persona' : 'personas'}</div>
                          {#if personasEnSlot > 0}<div class="hdet__combo-slot-personas">{personasEnSlot} {personasEnSlot === 1 ? 'huésped' : 'huéspedes'} asignados</div>{/if}
                        </div>
                        <div class="hdet__combo-slot-opciones">
                          {#each slot.opciones as opcion}
                            {@const bloqueada = esOpcionBloqueada(comboActivo, slotIdx, opcion.id)}
                            <button class="hdet__combo-slot-opcion" class:active={slot.seleccionada?.id === opcion.id} class:blocked={bloqueada} disabled={bloqueada} on:click={() => cambiarHabEnSlot(slotIdx, opcion)} title={bloqueada ? 'Seleccionada en otro slot (cámbiala allí primero)' : opcion.tipoHabitacion}>
                              <div class="hdet__combo-opcion-img">{#if opcion.imagenesIds?.length > 0}<img src="{API}/imagenes/habitacion/{opcion.imagenesIds[0]}" alt={opcion.tipoHabitacion} on:error={handleImgError} />{:else}<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>{/if}</div>
                              <div class="hdet__combo-opcion-info">
                                <span class="hdet__combo-opcion-tipo">{opcion.tipoHabitacion}</span>
                                <span class="hdet__combo-opcion-cama">{opcion.tipoCama} · {opcion.metrosCuadrados}m²</span>
                                <div class="hdet__combo-opcion-precios"><span class="hdet__combo-opcion-precio">{fmt(opcion.precioPorNoche)}<span class="hdet__combo-opcion-precio-lbl">/noche</span></span><span class="hdet__combo-opcion-ppersona">+ {fmt(opcion.precioPorPersona)}<span class="hdet__combo-opcion-precio-lbl">/persona</span></span></div>
                              </div>
                              {#if slot.seleccionada?.id === opcion.id}<div class="hdet__combo-opcion-check"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>{/if}
                              {#if bloqueada}<div class="hdet__combo-opcion-blocked"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></div>{/if}
                            </button>
                          {/each}
                        </div>
                        {#if slot.seleccionada}
                          <div class="hdet__combo-slot-resumen">
                            <div class="hdet__combo-slot-resumen-left"><span class="hdet__combo-slot-resumen-name">{slot.seleccionada.tipoHabitacion}</span><span class="hdet__combo-slot-resumen-breakdown">{fmt(slot.seleccionada.precioPorNoche)} /noche</span></div>
                            <span class="hdet__combo-slot-resumen-precio">{fmt(precioSlotConPersonas)}/noche{#if nights > 1}<span class="hdet__combo-slot-resumen-total">· {fmt(precioSlotConPersonas * nights)} total</span>{/if}</span>
                          </div>
                        {/if}
                      </div>
                    {/each}
                  </div>
                  <div class="hdet__combo-total-bar">
                    <div class="hdet__combo-total-info"><span class="hdet__combo-total-label">Total combinación</span><span class="hdet__combo-total-habs">{comboActivo.slots.length} habitaciones · {cantidadPersonas} huéspedes</span></div>
                    <div class="hdet__combo-total-precios"><span class="hdet__combo-total-precio-noche">{fmt(comboTotalConPersonas)}/noche</span>{#if nights > 1}<span class="hdet__combo-total-precio-total">{fmt(comboTotalConPersonas * nights)} por {nights} noches</span>{/if}</div>
                    <button class="hdet__combo-btn-seleccionar" class:active={bookMode === 'combo'} on:click={() => { bookMode = 'combo'; selectedRoom = null; selectedRoomIsExtra = false; datesWarning = false; document.querySelector('.booking-summary')?.scrollIntoView({ behavior: 'smooth', block: 'start' }); }}>
                      {#if bookMode === 'combo'}<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg> Combinación activa{:else}Reservar esta combinación{/if}
                    </button>
                  </div>
                {/if}
              </section>
            {/if}

            <!-- ═══ COMBINACIONES ESPECIALES ═══ -->
            {#if hayCombosEspeciales}
              <section class="content-section hdet__combo-section">
                <div class="hdet__combo-header">
                  <div class="hdet__combo-badge"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg> Combinación especial</div>
                  <h2 class="hdet__section-title">Combinación con habitaciones más pequeñas</h2>
                  <p class="hdet__section-description">No hay una habitación individual para {cantidadPersonas} personas, pero puedes combinar {combosEspeciales[0]?.slots.length || 2} habitaciones de {cantidadPersonas - 1} personas para alojar a tu grupo.</p>
                </div>

                {#if combosEspeciales.length > 1}
                  <div class="hdet__combo-selector">
                    <p class="hdet__combo-selector-label">Elige una combinación:</p>
                    <div class="hdet__combo-selector-btns">
                      {#each combosEspeciales as combo, idx}
                        <button class="hdet__combo-selector-btn" class:active={comboActivo?.esEspecial && JSON.stringify(comboActivo.slots.map(s=>s.seleccionada?.id)) === JSON.stringify(combo.slots.map(s=>s.seleccionada?.id))} on:click={() => selectComboEspecial(idx)}>{combo.slots.map((s, i) => s.seleccionada?.tipoHabitacion || `Hab.${i+1}`).join(' + ')}</button>
                      {/each}
                    </div>
                  </div>
                {/if}

                {#if comboActivo && comboActivo.esEspecial}
                  <div class="hdet__combo-slots">
                    {#each comboActivo.slots as slot, slotIdx}
                      {@const personasEnSlot = personasPorSlotActivo[slotIdx] || 0}
                      {@const precioSlot = slot.seleccionada ? slot.seleccionada.precioPorNoche : 0}
                      <div class="hdet__combo-slot">
                        <div class="hdet__combo-slot-header">
                          <div class="hdet__combo-slot-num">Hab. {slotIdx + 1}</div>
                          <div class="hdet__combo-slot-cap"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> {slot.capRequerida} {slot.capRequerida === 1 ? 'persona' : 'personas'}</div>
                          {#if personasEnSlot > 0}<div class="hdet__combo-slot-personas">{personasEnSlot} {personasEnSlot === 1 ? 'huésped' : 'huéspedes'} asignados</div>{/if}
                        </div>
                        <div class="hdet__combo-slot-opciones">
                          {#each slot.opciones as opcion}
                            {@const bloqueada = esOpcionBloqueada(comboActivo, slotIdx, opcion.id)}
                            <button class="hdet__combo-slot-opcion" class:active={slot.seleccionada?.id === opcion.id} class:blocked={bloqueada} disabled={bloqueada} on:click={() => cambiarHabEnSlot(slotIdx, opcion)} title={bloqueada ? 'Seleccionada en otro slot' : opcion.tipoHabitacion}>
                              <div class="hdet__combo-opcion-img">{#if opcion.imagenesIds?.length > 0}<img src="{API}/imagenes/habitacion/{opcion.imagenesIds[0]}" alt={opcion.tipoHabitacion} on:error={handleImgError} />{:else}<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>{/if}</div>
                              <div class="hdet__combo-opcion-info">
                                <span class="hdet__combo-opcion-tipo">{opcion.tipoHabitacion}</span>
                                <span class="hdet__combo-opcion-cama">{opcion.tipoCama} · {opcion.metrosCuadrados}m²</span>
                                <div class="hdet__combo-opcion-precios"><span class="hdet__combo-opcion-precio">{fmt(opcion.precioPorNoche)}<span class="hdet__combo-opcion-precio-lbl">/noche</span></span></div>
                              </div>
                              {#if slot.seleccionada?.id === opcion.id}<div class="hdet__combo-opcion-check"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>{/if}
                              {#if bloqueada}<div class="hdet__combo-opcion-blocked"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></div>{/if}
                            </button>
                          {/each}
                        </div>
                        {#if slot.seleccionada}
                          <div class="hdet__combo-slot-resumen">
                            <div class="hdet__combo-slot-resumen-left"><span class="hdet__combo-slot-resumen-name">{slot.seleccionada.tipoHabitacion}</span><span class="hdet__combo-slot-resumen-breakdown">{fmt(slot.seleccionada.precioPorNoche)} /noche</span></div>
                            <span class="hdet__combo-slot-resumen-precio">{fmt(precioSlot)}/noche{#if nights > 1}<span class="hdet__combo-slot-resumen-total">· {fmt(precioSlot * nights)} total</span>{/if}</span>
                          </div>
                        {/if}
                      </div>
                    {/each}
                  </div>
                  <div class="hdet__combo-total-bar">
                    <div class="hdet__combo-total-info"><span class="hdet__combo-total-label">Total combinación especial</span><span class="hdet__combo-total-habs">{comboActivo.slots.length} habitaciones · {cantidadPersonas} huéspedes</span></div>
                    <div class="hdet__combo-total-precios"><span class="hdet__combo-total-precio-noche">{fmt(comboTotalConPersonas)}/noche</span>{#if nights > 1}<span class="hdet__combo-total-precio-total">{fmt(comboTotalConPersonas * nights)} por {nights} noches</span>{/if}</div>
                    <button class="hdet__combo-btn-seleccionar" class:active={bookMode === 'combo'} on:click={() => { bookMode = 'combo'; selectedRoom = null; selectedRoomIsExtra = false; datesWarning = false; document.querySelector('.booking-summary')?.scrollIntoView({ behavior: 'smooth', block: 'start' }); }}>
                      {#if bookMode === 'combo'}<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg> Combinación activa{:else}Reservar esta combinación{/if}
                    </button>
                  </div>
                {/if}
              </section>
            {/if}

          {:else if activeTab === 'comments'}
            <section class="content-section">
              <h2 class="hdet__section-title">Reseñas y Comentarios</h2>
              {#if comentLoading}<div class="cmt-loading"><div class="cmt-spinner"></div><span>Cargando comentarios...</span></div>
              {:else if comentarios.length === 0}<div class="cmt-empty"><svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg><p>Este hotel aún no tiene comentarios.</p></div>
              {:else}
                {#if resenasRaiz.length > 0}<h3 class="cmt-group-title"><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg> Reseñas de huéspedes</h3><div class="cmt-list">{#each resenasRaiz as c (c.id)}<CommentNode comment={c} allComments={comentarios} misDowns={misDowns} isReply={false} on:vote={e => handleDown(e.detail.comentarioId, e.detail.valor)} on:reply={e => sendReplyFromNode(e.detail)} />{/each}</div>{/if}
                {#if comentariosRaiz.length > 0}<h3 class="cmt-group-title" style="margin-top: 2rem;"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> Comentarios</h3><div class="cmt-list">{#each comentariosRaiz as c (c.id)}<CommentNode comment={c} allComments={comentarios} misDowns={misDowns} isReply={false} on:vote={e => handleDown(e.detail.comentarioId, e.detail.valor)} on:reply={e => sendReplyFromNode(e.detail)} />{/each}</div>{/if}
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
                <div class="date-input-group"><span class="input-label">Check-in</span><input type="date" bind:value={checkInDate} min={todayStr} on:change={onSidebarDateChange} class="date-input" /></div>
                <div class="date-input-group"><span class="input-label">Check-out</span><input type="date" bind:value={checkOutDate} min={minCheckOut} on:change={onSidebarDateChange} class="date-input" /></div>
              </div>
              {#if datesWarning}
                <div class="hdet__book-notice" style="background:rgba(245,158,11,0.08);border-color:rgba(245,158,11,0.4);color:#92400e;flex-direction:column;gap:0.5rem;align-items:flex-start;">
                  <div style="display:flex;align-items:center;gap:0.4rem;">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    Fechas cambiadas. Actualiza la disponibilidad para continuar.
                  </div>
                  <button
                    style="background:linear-gradient(135deg,#f59e0b,#d97706);border:none;color:white;padding:0.45rem 1rem;border-radius:6px;font-weight:700;font-size:0.82rem;cursor:pointer;width:100%;"
                    on:click={refetchDisponibilidad}
                    disabled={fetchingAvail}>
                    {fetchingAvail ? 'Actualizando...' : '🔄 Actualizar disponibilidad'}
                  </button>
                </div>
              {/if}
              {#if nights > 0}<div class="nights-display">{nights} {nights === 1 ? 'noche' : 'noches'}</div>{/if}
            </div>
            <div class="booking-section">
              <p class="booking-label">Huéspedes</p>
              <div class="hdet__guests-display"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg><span>{cantidadPersonas} {cantidadPersonas === 1 ? 'huésped' : 'huéspedes'}</span></div>
            </div>

            {#if bookMode === 'single' && selectedRoom}
              <div class="booking-section selected-room-section">
                <p class="booking-label">Habitación Seleccionada</p>
                <div class="selected-room-card">
                  <div class="selected-room-info">
                    <strong>{selectedRoom.tipoHabitacion}</strong>
                    <span>{fmt(selectedRoom.precioPorNoche)}/noche</span>
                    {#if selectedRoomIsExtra}<span style="color: var(--primary); font-weight: 700; font-size: .82rem;">+1 persona extra · +{fmt(selectedRoom.precioPorPersona)}/noche</span>{/if}
                  </div>
                  <button class="remove-room-btn" on:click={() => { selectedRoom = null; selectedRoomIsExtra = false; bookMode = 'single'; }} aria-label="Quitar"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
                </div>
              </div>
            {:else if bookMode === 'combo' && comboActivo}
              <div class="booking-section selected-room-section">
                <p class="booking-label">Combinación Seleccionada</p>
                <div class="hdet__sidebar-combo">
                  {#each comboActivo.slots as slot, i}
                    {@const personas = personasPorSlotActivo[i] || 0}
                    {@const precioConPersonas = slot.seleccionada ? slot.seleccionada.precioPorNoche : 0}
                    <div class="hdet__sidebar-combo-slot">
                      <span class="hdet__sidebar-combo-num">Hab.{i+1}</span>
                      <div class="hdet__sidebar-combo-middle"><span class="hdet__sidebar-combo-name">{slot.seleccionada?.tipoHabitacion || '—'}</span><span class="hdet__sidebar-combo-personas">{personas} {personas === 1 ? 'huésped' : 'huéspedes'}</span></div>
                      <span class="hdet__sidebar-combo-precio">{fmt(precioConPersonas)}</span>
                    </div>
                  {/each}
                </div>
                <button class="remove-room-btn" style="margin-top:.5rem;" on:click={() => { bookMode = 'single'; selectedRoomIsExtra = false; }} aria-label="Quitar combinación"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
              </div>
            {:else}
              <div class="no-room-selected"><svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg><p>Selecciona una habitación o combinación para continuar</p></div>
            {/if}

            {#if bookMode === 'single' && selectedRoom && nights > 0}
              <div class="price-summary">
                <div class="price-row"><span>{fmt(selectedRoom.precioPorNoche)}/noche × {nights} {nights === 1 ? 'noche' : 'noches'}</span><span>{fmt(selectedRoom.precioPorNoche * nights)}</span></div>
                {#if selectedRoomIsExtra}<div class="price-row" style="color: var(--primary); font-weight: 600;"><span>+1 persona extra × {fmt(selectedRoom.precioPorPersona)} × {nights}n</span><span>{fmt(selectedRoom.precioPorPersona * nights)}</span></div>{/if}
                <div class="hdet__price-divider"></div>
                <div class="price-row total"><span>Total</span><span class="total-amount">{fmt(totalPrice)}</span></div>
                <div class="taxes-note">Incluye impuestos y cargos</div>
              </div>
            {:else if bookMode === 'combo' && comboActivo && nights > 0}
              <div class="price-summary">
                {#each comboActivo.slots as slot, i}
                  {#if slot.seleccionada}
                    {@const precioHab = slot.seleccionada.precioPorNoche * nights}
                    <div class="price-row price-row--hab-label"><span>Hab.{i+1} — {slot.seleccionada.tipoHabitacion}</span></div>
                    <div class="price-row price-row--sub"><span>{fmt(slot.seleccionada.precioPorNoche)} × {nights}n</span><span>{fmt(precioHab)}</span></div>
                  {/if}
                {/each}
                <div class="hdet__price-divider"></div>
                <div class="price-row total"><span>Total</span><span class="total-amount">{fmt(comboTotalConPersonas * nights)}</span></div>
                <div class="taxes-note">Incluye impuestos y cargos</div>
              </div>
            {/if}

            {#if bookError}<div class="hdet__book-notice"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> {bookError}</div>{/if}

            <div class="booking-actions">
              {#if bookMode === 'single'}
                <button class="btn-book-now" on:click={bookNow} disabled={!selectedRoom || booking}>{booking ? 'Procesando...' : selectedRoomIsExtra ? 'Reservar con +1 extra' : 'Reservar Ahora'}</button>
              {:else if bookMode === 'combo'}
                <button class="btn-book-now btn-book-combo" on:click={bookCombo} disabled={!comboActivo || booking}>{booking ? 'Procesando...' : `Reservar ${comboActivo?.slots.length || ''} habitaciones`}</button>
              {/if}
            </div>

            <div class="trust-badges">
              <div class="trust-badge"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg><span>Pago Seguro</span></div>
              <div class="trust-badge"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg><span>Mejor Precio Garantizado</span></div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    {#if reservacion}
      <div class="hdet__confirm-overlay" role="dialog" aria-modal="true">
        <div class="hdet__confirm-modal">
          <div class="hdet__confirm-icon">✓</div>
          <h2 class="hdet__confirm-title">¡Reservación creada!</h2>
          <p class="hdet__confirm-code">{reservacion.noReservacion}</p>
          <div class="hdet__confirm-rows">
            <div class="hdet__confirm-row"><span>Hotel</span><strong>{hotel?.nombre}</strong></div>
            {#if reservacion._modo === 'combo' && reservacion._comboSlots}{#each reservacion._comboSlots as r, i}<div class="hdet__confirm-row"><span>Hab.{i+1}</span><strong>{r.tipoHabitacion}</strong></div>{/each}{:else}<div class="hdet__confirm-row"><span>Habitación</span><strong>{selectedRoom?.tipoHabitacion}{selectedRoomIsExtra ? ' (+1 extra)' : ''}</strong></div>{/if}
            <div class="hdet__confirm-row"><span>Check-in</span><strong>{checkInDate}</strong></div>
            <div class="hdet__confirm-row"><span>Check-out</span><strong>{checkOutDate}</strong></div>
            <div class="hdet__confirm-row"><span>Huéspedes</span><strong>{selectedRoomIsExtra ? selectedRoom?.capacidadMaxima + 1 : cantidadPersonas}</strong></div>
            <div class="hdet__confirm-row"><span>Estado</span><strong class="hdet__confirm-estado">{reservacion.estado}</strong></div>
            <div class="hdet__confirm-row hdet__confirm-row--total"><span>Total</span><strong>{fmt(reservacion.total)}</strong></div>
          </div>
          <p class="hdet__confirm-expira">Expira: {reservacion.fechaExpiracion}</p>
          <div class="hdet__confirm-btns">
            <button class="hdet__confirm-btn-home" on:click={() => navigateTo('home')}>Volver al inicio</button>
            <button class="hdet__confirm-btn-pay" on:click={goToCheckout}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg> Pagar ahora</button>
            <button class="hdet__confirm-btn-close" on:click={() => reservacion = null}>Cerrar</button>
          </div>
        </div>
      </div>
    {/if}

    {#if showLoginRequired}
      <div class="hdet__confirm-overlay" role="dialog" aria-modal="true">
        <div class="hdet__login-prompt-modal">
          <div class="hdet__login-prompt-icon"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg></div>
          <h2 class="hdet__login-prompt-title">¡Necesitas iniciar sesión!</h2>
          <p class="hdet__login-prompt-text">Para poder reservar una habitación necesitas tener una cuenta e iniciar sesión. Es rápido, sencillo y podrás gestionar todas tus reservas.</p>
          <div class="hdet__login-prompt-btns">
            <button class="hdet__login-prompt-btn-login" on:click={() => { closeLoginPrompt(); navigateTo('login'); }}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path><polyline points="10 17 15 12 10 7"></polyline><line x1="15" y1="12" x2="3" y2="12"></line></svg> Iniciar Sesión</button>
            <button class="hdet__login-prompt-btn-register" on:click={() => { closeLoginPrompt(); navigateTo('register'); }}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg> Crear Cuenta</button>
            <button class="hdet__login-prompt-btn-close" on:click={closeLoginPrompt}>Seguir explorando</button>
          </div>
        </div>
      </div>
    {/if}
  </div>

  {#if showImageGallery && images.length > 0}
    <div class="gallery-modal" role="dialog" aria-modal="true" aria-label="Galería de fotos">
      <button class="gallery-close" on:click={closeGallery} aria-label="Cerrar galería"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
      <button class="gallery-nav-btn gallery-prev" on:click={prevImage} aria-label="Anterior"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"></polyline></svg></button>
      <button class="gallery-nav-btn gallery-next" on:click={nextImage} aria-label="Siguiente"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"></polyline></svg></button>
      <div class="gallery-content"><img src={images[currentImageIndex]} alt="{hotel.nombre} {currentImageIndex + 1}" class="gallery-image" on:error={handleImgError} /><div class="gallery-counter">{currentImageIndex + 1} / {images.length}</div></div>
    </div>
  {/if}

</div>
{/if}