<script>
  import { onMount } from 'svelte';

  export let API_BASE;
  export let badge;
  export let fileToBase64;
  export let tiposHabitacion;
  export let count = 0;

  let confirmDialog = null;
  function pedirConfirmacion(t, m, fn) { confirmDialog = { titulo: t, mensaje: m, onConfirm: fn }; }
  function cerrarConfirm() { confirmDialog = null; }
  function ejecutarConfirm() { if (confirmDialog?.onConfirm) confirmDialog.onConfirm(); confirmDialog = null; }

  // ── Lista hoteles ──
  let hoteles = []; let cargandoHoteles = false; let errorHoteles = null;
  let busquedaHotel = ''; let filtroEstadoHotel = 'todos';

  // ── Vista ──
  let vistaHoteles = 'lista'; let hotelDetalle = null; let tabDetalle = 'info';

  // ── Info hotel ──
  let editInfoHotel = { nombre: '', direccion: '', descripcion: '', rating: 0, estadoId: 1 };
  let guardandoInfo = false; let mensajeInfo = null;

  // ── Imágenes hotel ──
  let subiendoImgHotel = false; let mensajeImgHotel = null;

  // ── Amenidades ──
  let amenidades = []; let amenidadesHotel = [];
  let cargandoAmenidades = false; let mensajeAmenidad = null;
  let showFormAmenidad = false;
  let nuevaAmenidad = { amenidadId: 1, descripcion: '' };
  let amenidadEditandoId = null; let editDescAmenidad = '';
  let subiendoImgAmenidadSet = new Set();
  let showFormNuevaAmenidadCatalogo = false;
  let nuevaAmenidadCatalogoNombre = '';
  let creandoAmenidadCatalogo = false;
  let mensajeNuevaAmenidadCatalogo = null;

  // ── Habitaciones ──
  let habitaciones = []; let cargandoHabitaciones = false; let errorHabitaciones = null;

  // Modal editar habitación — mantiene numeroHabitacion (ya existe en BD)
  let showModalHabitacion = false; let habitacionEditando = null;
  let editHabitacion = { tipoHabitacionId: 1, numeroHabitacion: '', descripcion: '', estadoId: 1 };
  let guardandoHabitacion = false; let mensajeHabitacion = null;
  let subiendoImgHab = false; let mensajeImgHab = null;

  // Modal nueva habitación — SIN numeroHabitacion (backend lo auto-asigna)
  let showModalNuevaHab = false;
  let nuevaHabGestion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1 };
  let guardandoNuevaHab = false; let mensajeNuevaHab = null;

  // Modales eliminar
  let showModalEliminarHab = false; let habEliminando = null; let eliminandoHab = false;
  let showModalEliminarHotel = false; let hotelEliminando = null; let eliminandoHotel = false;

  // Overlays
  let creandoMasivo = false; let creandoMasivoProgreso = '';
  let eliminandoMasivo = false; let eliminandoMasivoProgreso = '';

  $: hotelesFiltrados = hoteles.filter(h => {
    const q = busquedaHotel.toLowerCase();
    const matchBusqueda = q === '' || h.nombre.toLowerCase().includes(q) || (h.ciudad ?? '').toLowerCase().includes(q) || (h.pais ?? '').toLowerCase().includes(q);
    const estadoNorm = h.estado?.toLowerCase() ?? '';
    return matchBusqueda && (filtroEstadoHotel === 'todos' || estadoNorm === filtroEstadoHotel);
  });

  $: count = hoteles.length;

  onMount(() => { cargarHoteles(); });

  // ════════════════════════════════════════════════════
  //  HOTELES
  // ════════════════════════════════════════════════════

  async function cargarHoteles() {
    cargandoHoteles = true; errorHoteles = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      hoteles = await res.json();
    } catch (e) { errorHoteles = 'No se pudo cargar la lista de hoteles. ' + e.message; }
    finally { cargandoHoteles = false; }
  }

  function abrirDetalleHotel(h) {
    hotelDetalle = { ...h };
    editInfoHotel = { nombre: h.nombre ?? '', direccion: h.direccion ?? '', descripcion: h.descripcion ?? '', rating: h.rating ?? 0, estadoId: h.estadoId ?? 1 };
    tabDetalle = 'info'; mensajeInfo = null; vistaHoteles = 'detalle'; amenidadesHotel = [];
    cargarHabitacionesDetalle(h.id); cargarAmenidadesHotel(h.id);
  }

  function volverListaHoteles() { vistaHoteles = 'lista'; hotelDetalle = null; habitaciones = []; }

  async function guardarInfoHotel() {
    guardandoInfo = true; mensajeInfo = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}`, {
        method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: editInfoHotel.nombre, direccion: editInfoHotel.direccion, descripcion: editInfoHotel.descripcion, rating: Number(editInfoHotel.rating), estadoId: Number(editInfoHotel.estadoId) })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeInfo = { tipo: 'ok', texto: 'Hotel actualizado correctamente.' };
      const estadoStr = editInfoHotel.estadoId === 1 ? 'Activo' : 'Cerrado';
      hotelDetalle = { ...hotelDetalle, ...editInfoHotel, estado: estadoStr };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, ...editInfoHotel, estado: estadoStr } : h);
    } catch (e) { mensajeInfo = { tipo: 'error', texto: e.message }; }
    finally { guardandoInfo = false; }
  }

  function abrirEliminarHotel(h) { hotelEliminando = h; showModalEliminarHotel = true; }
  function abrirEliminarHotelDetalle() { hotelEliminando = hotelDetalle; showModalEliminarHotel = true; }

  async function _eliminarHotel(volverLista) {
    if (!hotelEliminando) return;
    eliminandoHotel = true; showModalEliminarHotel = false;
    eliminandoMasivo = true; eliminandoMasivoProgreso = `Eliminando hotel "${hotelEliminando.nombre}"...`;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelEliminando.id}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      hoteles = hoteles.filter(h => h.id !== hotelEliminando.id);
      hotelEliminando = null;
      if (volverLista) volverListaHoteles();
    } catch (e) { alert('No se pudo eliminar: ' + e.message); }
    finally { eliminandoHotel = false; eliminandoMasivo = false; }
  }

  // ── Imágenes hotel ──
  async function subirImagenHotel(event) {
    const file = event.target.files[0]; if (!file) return;
    subiendoImgHotel = true; mensajeImgHotel = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64 }) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      hotelDetalle = { ...hotelDetalle, imagenesIds: [...(hotelDetalle.imagenesIds ?? []), data.id] };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, imagenesIds: hotelDetalle.imagenesIds } : h);
      mensajeImgHotel = { tipo: 'ok', texto: 'Imagen agregada.' };
    } catch (e) { mensajeImgHotel = { tipo: 'error', texto: e.message }; }
    finally { subiendoImgHotel = false; event.target.value = ''; }
  }

  function pedirEliminarImgHotel(imagenId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen del hotel?', () => _eliminarImgHotel(imagenId)); }
  async function _eliminarImgHotel(imagenId) {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/imagenes/${imagenId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      hotelDetalle = { ...hotelDetalle, imagenesIds: (hotelDetalle.imagenesIds ?? []).filter(id => id !== imagenId) };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, imagenesIds: hotelDetalle.imagenesIds } : h);
    } catch (e) { mensajeImgHotel = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message }; }
  }

  // ════════════════════════════════════════════════════
  //  AMENIDADES
  // ════════════════════════════════════════════════════

  async function cargarAmenidadesHotel(hotelId) {
    cargandoAmenidades = true; mensajeAmenidad = null;
    try {
      const [rA, rH] = await Promise.all([
        fetch(`${API_BASE}/admin/amenidades`, { credentials: 'include' }),
        fetch(`${API_BASE}/admin/hoteles/${hotelId}/amenidades`, { credentials: 'include' }),
      ]);
      if (rA.ok) amenidades = await rA.json();
      if (rH.ok) amenidadesHotel = await rH.json();
    } catch(e) {}
    finally { cargandoAmenidades = false; }
  }

  async function crearAmenidadCatalogo() {
    if (!nuevaAmenidadCatalogoNombre.trim()) { mensajeNuevaAmenidadCatalogo = { tipo: 'error', texto: 'El nombre es obligatorio.' }; return; }
    creandoAmenidadCatalogo = true; mensajeNuevaAmenidadCatalogo = null;
    try {
      const res = await fetch(`${API_BASE}/admin/amenidades`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nombre: nuevaAmenidadCatalogoNombre.trim() }) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      amenidades = [...amenidades, { id: data.id, nombre: data.nombre }];
      mensajeNuevaAmenidadCatalogo = { tipo: 'ok', texto: `"${data.nombre}" añadida al catálogo.` };
      nuevaAmenidadCatalogoNombre = '';
      setTimeout(() => { showFormNuevaAmenidadCatalogo = false; mensajeNuevaAmenidadCatalogo = null; }, 1500);
    } catch (e) { mensajeNuevaAmenidadCatalogo = { tipo: 'error', texto: e.message }; }
    finally { creandoAmenidadCatalogo = false; }
  }

  async function agregarAmenidadHotel() {
    mensajeAmenidad = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/amenidades`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: Number(nuevaAmenidad.amenidadId), descripcion: nuevaAmenidad.descripcion }) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      const catAm = amenidades.find(a => a.id === Number(nuevaAmenidad.amenidadId));
      amenidadesHotel = [...amenidadesHotel, { id: data.id, hotelId: hotelDetalle.id, amenidadId: Number(nuevaAmenidad.amenidadId), amenidadNombre: catAm?.nombre ?? '', descripcion: nuevaAmenidad.descripcion, imagenesIds: [] }];
      mensajeAmenidad = { tipo: 'ok', texto: 'Amenidad agregada.' };
      showFormAmenidad = false; nuevaAmenidad = { amenidadId: 1, descripcion: '' };
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  async function guardarDescAmenidad(ha) {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${ha.id}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: ha.amenidadId, descripcion: editDescAmenidad }) });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.map(a => a.id === ha.id ? { ...a, descripcion: editDescAmenidad } : a);
      amenidadEditandoId = null;
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  function pedirEliminarAmenidad(haId, nombre) { pedirConfirmacion('Eliminar amenidad', `¿Eliminar "${nombre}" y sus imágenes?`, () => _eliminarAmenidad(haId)); }
  async function _eliminarAmenidad(haId) {
    try {
      await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}`, { method: 'DELETE', credentials: 'include' });
      amenidadesHotel = amenidadesHotel.filter(a => a.id !== haId);
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  async function subirImagenAmenidad(event, haId) {
    const file = event.target.files[0]; if (!file) return;
    subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet, haId]);
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64 }) });
      const data = await res.json();
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.map(a => a.id === haId ? { ...a, imagenesIds: [...(a.imagenesIds ?? []), data.id] } : a);
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: 'Error subiendo imagen: ' + e.message }; }
    finally { subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet].filter(id => id !== haId)); event.target.value = ''; }
  }

  function pedirEliminarImgAmenidad(haId, imgId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen de la amenidad?', () => _eliminarImgAmenidad(haId, imgId)); }
  async function _eliminarImgAmenidad(haId, imgId) {
    try {
      await fetch(`${API_BASE}/admin/hoteles/amenidades/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' });
      amenidadesHotel = amenidadesHotel.map(a => { if (a.id !== haId) return a; return { ...a, imagenesIds: (a.imagenesIds ?? []).filter(i => i !== imgId) }; });
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message }; }
  }

  // ════════════════════════════════════════════════════
  //  HABITACIONES
  // ════════════════════════════════════════════════════

  async function cargarHabitacionesDetalle(hotelId) {
    cargandoHabitaciones = true; errorHabitaciones = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelId}/habitaciones`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      habitaciones = await res.json();
    } catch (e) { errorHabitaciones = 'No se pudieron cargar las habitaciones. ' + e.message; }
    finally { cargandoHabitaciones = false; }
  }

  // Editar: numeroHabitacion SÍ se muestra/edita (ya existe en BD)
  function abrirEditarHabitacion(h) {
    habitacionEditando = { ...h };
    editHabitacion = {
      tipoHabitacionId: h.tipoHabitacionId,
      numeroHabitacion: h.numeroHabitacion ?? '',
      descripcion:      h.descripcion ?? '',
      estadoId:         h.estadoId,
    };
    mensajeHabitacion = null; mensajeImgHab = null; showModalHabitacion = true;
  }

  async function guardarHabitacion() {
    guardandoHabitacion = true; mensajeHabitacion = null;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionEditando.id}`, {
        method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tipoHabitacionId: Number(editHabitacion.tipoHabitacionId),
          numeroHabitacion: editHabitacion.numeroHabitacion.trim(),
          descripcion:      editHabitacion.descripcion,
          estadoId:         Number(editHabitacion.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeHabitacion = { tipo: 'ok', texto: 'Habitación actualizada.' };
      const tipoNombre  = tiposHabitacion.find(t => t.id === Number(editHabitacion.tipoHabitacionId))?.nombre ?? '';
      const estadoNombre = editHabitacion.estadoId == 1 ? 'Activa' : 'Cerrada';
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id
        ? { ...h, ...editHabitacion, tipoHabitacion: tipoNombre, estado: estadoNombre }
        : h);
    } catch (e) { mensajeHabitacion = { tipo: 'error', texto: e.message }; }
    finally { guardandoHabitacion = false; }
  }

  async function subirImagenHabitacion(event) {
    const file = event.target.files[0]; if (!file) return;
    subiendoImgHab = true; mensajeImgHab = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionEditando.id}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64 }) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      const nuevosIds = [...(habitacionEditando.imagenesIds ?? []), data.id];
      habitacionEditando = { ...habitacionEditando, imagenesIds: nuevosIds };
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, imagenesIds: nuevosIds } : h);
      mensajeImgHab = { tipo: 'ok', texto: 'Imagen agregada.' };
    } catch (e) { mensajeImgHab = { tipo: 'error', texto: e.message }; }
    finally { subiendoImgHab = false; event.target.value = ''; }
  }

  function pedirEliminarImgHab(imagenId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen de la habitación?', () => _eliminarImgHab(imagenId)); }
  async function _eliminarImgHab(imagenId) {
    try {
      await fetch(`${API_BASE}/admin/habitaciones/imagenes/${imagenId}`, { method: 'DELETE', credentials: 'include' });
      const nuevosIds = (habitacionEditando.imagenesIds ?? []).filter(id => id !== imagenId);
      habitacionEditando = { ...habitacionEditando, imagenesIds: nuevosIds };
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, imagenesIds: nuevosIds } : h);
    } catch (e) { mensajeImgHab = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message }; }
  }

  function abrirEliminarHab(h) { habEliminando = h; showModalEliminarHab = true; }

  async function confirmarEliminarHab() {
    if (!habEliminando) return;
    eliminandoHab = true; showModalEliminarHab = false;
    eliminandoMasivo = true; eliminandoMasivoProgreso = `Eliminando habitación #${habEliminando.id}...`;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habEliminando.id}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      habitaciones = habitaciones.filter(h => h.id !== habEliminando.id);
      habEliminando = null;
    } catch (e) { alert('No se pudo eliminar: ' + e.message); }
    finally { eliminandoHab = false; eliminandoMasivo = false; }
  }

  // Nueva habitación: SIN numeroHabitacion — el backend lo auto-asigna
  function abrirModalNuevaHab() {
    nuevaHabGestion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1 };
    mensajeNuevaHab = null; showModalNuevaHab = true;
  }

  async function crearHabGestion() {
    if (!hotelDetalle) return;
    const cant = Math.max(1, Math.min(50, Number(nuevaHabGestion.cantidad) || 1));
    guardandoNuevaHab = true; mensajeNuevaHab = null;
    if (cant > 1) { creandoMasivo = true; creandoMasivoProgreso = `Creando habitación 0 de ${cant}...`; }
    try {
      // Payload sin numeroHabitacion — el backend lo genera automáticamente (count + 1)
      const payload = {
        tipoHabitacionId: Number(nuevaHabGestion.tipoHabitacionId),
        descripcion:      nuevaHabGestion.descripcion,
        estadoId:         Number(nuevaHabGestion.estadoId),
      };
      const tipoNom = tiposHabitacion.find(t => t.id === payload.tipoHabitacionId)?.nombre ?? '';
      const estNom  = payload.estadoId === 1 ? 'Activa' : 'Cerrada';
      let creadas = 0;
      for (let i = 0; i < cant; i++) {
        if (cant > 1) creandoMasivoProgreso = `Creando habitación ${i + 1} de ${cant}...`;
        const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/habitaciones`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        const data = await res.json();
        if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
        habitaciones = [...habitaciones, { ...payload, id: data.id, tipoHabitacion: tipoNom, estado: estNom, imagenesIds: [] }];
        creadas++;
      }
      mensajeNuevaHab = { tipo: 'ok', texto: `${creadas} habitación(es) ${tipoNom} creada(s). Número asignado automáticamente.` };
    } catch (e) { mensajeNuevaHab = { tipo: 'error', texto: e.message }; }
    finally { guardandoNuevaHab = false; creandoMasivo = false; }
  }

  function cerrarModales() {
    showModalHabitacion = false; showModalNuevaHab = false;
    showModalEliminarHab = false; showModalEliminarHotel = false;
    habitacionEditando = null; habEliminando = null; hotelEliminando = null;
  }
  function handleOverlayKey(e) { if (e.key === 'Escape') cerrarModales(); }
</script>

<!-- Overlays bloqueantes -->
{#if creandoMasivo}
  <div class="adm__overlay" style="z-index:3500"></div>
  <div class="adm__confirm" style="z-index:3501;text-align:center">
    <div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem">
      <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#7b93ff" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p class="adm__confirm__title" style="font-size:1rem">Creando habitaciones</p>
    </div>
    <div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem">
      <p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{creandoMasivoProgreso}</p>
      <p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera. No cierres esta página.</p>
    </div>
  </div>
{/if}
{#if eliminandoMasivo}
  <div class="adm__overlay" style="z-index:3500"></div>
  <div class="adm__confirm" style="z-index:3501;text-align:center">
    <div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem">
      <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f85149" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p class="adm__confirm__title" style="font-size:1rem">Eliminando</p>
    </div>
    <div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem">
      <p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{eliminandoMasivoProgreso}</p>
      <p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera. No cierres esta página.</p>
    </div>
  </div>
{/if}

{#if vistaHoteles === 'lista'}
<!-- ══ LISTA ══ -->
  <div class="adm__filters-bar">
    <div class="adm__search-wrap">
      <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
      <input class="adm__search-input" type="text" bind:value={busquedaHotel} placeholder="Buscar hotel, ciudad, país..." />
    </div>
    <select class="adm__select" bind:value={filtroEstadoHotel}>
      <option value="todos">Todos los estados</option>
      <option value="activo">Activo</option>
      <option value="cerrado">Cerrado</option>
    </select>
    <span class="adm__count-label">{hotelesFiltrados.length} hotel(es)</span>
    <button class="adm__btn adm__btn--ghost" on:click={cargarHoteles} title="Recargar">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
    </button>
  </div>

  {#if cargandoHoteles}
    <div class="adm__loading-state"><svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando hoteles...</p></div>
  {:else if errorHoteles}
    <div class="adm__error-state"><p>{errorHoteles}</p><button class="adm__btn adm__btn--ghost" on:click={cargarHoteles}>Reintentar</button></div>
  {:else}
    <div class="adm__card adm__card--no-pad">
      <div class="adm__table-wrap">
        <table class="adm__table">
          <thead><tr><th>Hotel</th><th>Ubicación</th><th>Dirección</th><th>Rating</th><th>Habitaciones</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>
            {#each hotelesFiltrados as h (h.id)}
              <tr>
                <td><div class="adm__hotel-mini"><div class="adm__hotel-mini-thumb">{#if h.imagenesIds?.length > 0}<img src="{API_BASE}/imagenes/hotel/{h.imagenesIds[0]}" alt={h.nombre} />{:else}<span>🏨</span>{/if}</div><div><p class="adm__hotel-mini-name">{h.nombre}</p><p class="adm__hotel-mini-id">ID #{h.id}</p></div></div></td>
                <td><p class="adm__hotel-city">{h.ciudad}</p><p class="adm__hotel-country">{h.pais}</p></td>
                <td class="adm__hotel-address">{h.direccion ?? '—'}</td>
                <td><div class="adm__rating-pill"><svg width="12" height="12" viewBox="0 0 24 24" fill="#f0a030" stroke="#f0a030" stroke-width="1"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>{h.rating?.toFixed(1) ?? '—'}</div></td>
                <td class="adm__table-center">{h.cantidadHabitaciones ?? '—'}</td>
                <td><span class="adm__badge {badge(h.estado)}">{h.estado}</span></td>
                <td>
                  <div style="display:flex;gap:.3rem">
                    <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirDetalleHotel(h)} title="Gestionar"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                    <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => abrirEliminarHotel(h)} title="Eliminar"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                  </div>
                </td>
              </tr>
            {/each}
            {#if hotelesFiltrados.length === 0}<tr><td colspan="7" class="adm__empty-cell">No se encontraron hoteles.</td></tr>{/if}
          </tbody>
        </table>
      </div>
    </div>
  {/if}

{:else}
<!-- ══ DETALLE ══ -->
  <div class="adm__detalle-header">
    <button class="adm__btn adm__btn--ghost" on:click={volverListaHoteles}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>Volver a hoteles</button>
    <div class="adm__detalle-title"><h2>{hotelDetalle.nombre}</h2><span class="adm__badge {badge(hotelDetalle.estado)}">{hotelDetalle.estado}</span><span class="adm__detalle-loc">{hotelDetalle.ciudad}, {hotelDetalle.pais}</span></div>
    <button class="adm__btn adm__btn--danger" on:click={abrirEliminarHotelDetalle} style="margin-left:auto"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>Eliminar Hotel</button>
  </div>

  <div class="adm__tabs">
    {#each [
      { key: 'info',         label: 'Información',  icon: 'M12 12a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 16v-4 M12 8h.01' },
      { key: 'imagenes',     label: `Imágenes (${hotelDetalle.imagenesIds?.length ?? 0})`, icon: 'M3 3h18v18H3z M8.5 8.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z M21 15l-5-5L5 21' },
      { key: 'amenidades',   label: `Amenidades (${amenidadesHotel.length})`, icon: 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z' },
      { key: 'habitaciones', label: `Habitaciones (${habitaciones.length})`, icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10' },
    ] as tab}
      <button class="adm__tab" class:adm__tab--active={tabDetalle === tab.key} on:click={() => tabDetalle = tab.key}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d={tab.icon}/></svg>
        {tab.label}
      </button>
    {/each}
  </div>

  <!-- ── Info ── -->
  {#if tabDetalle === 'info'}
    <div class="adm__card adm__detalle-form-card">
      <div class="adm__form-grid">
        <div class="adm__field adm__field--full"><label>Nombre del Hotel</label><input type="text" bind:value={editInfoHotel.nombre} /></div>
        <div class="adm__field adm__field--full"><label>Dirección</label><input type="text" bind:value={editInfoHotel.direccion} /></div>
        <div class="adm__field"><label>Rating (0–5)</label><input type="number" bind:value={editInfoHotel.rating} min="0" max="5" step="0.1" /></div>
        <div class="adm__field"><label>Estado</label><select bind:value={editInfoHotel.estadoId}><option value={1}>Activo</option><option value={2}>Cerrado</option></select></div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editInfoHotel.descripcion} rows="4"></textarea></div>
      </div>
      {#if mensajeInfo}<div class="adm__feedback adm__feedback--{mensajeInfo.tipo}" style="margin-top:1rem">{mensajeInfo.texto}</div>{/if}
      <div style="display:flex;justify-content:flex-end;margin-top:1.25rem">
        <button class="adm__btn adm__btn--primary" on:click={guardarInfoHotel} disabled={guardandoInfo}>{#if guardandoInfo}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Guardando...{:else}Guardar cambios{/if}</button>
      </div>
    </div>

  <!-- ── Imágenes ── -->
  {:else if tabDetalle === 'imagenes'}
    <div class="adm__card">
      <div class="adm__img-section-header">
        <p class="adm__img-section-title">Imágenes del hotel</p>
        <label class="adm__btn adm__btn--primary adm__upload-btn">{#if subiendoImgHotel}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Subiendo...{:else}+ Agregar imagen{/if}<input type="file" accept="image/*" on:change={subirImagenHotel} disabled={subiendoImgHotel} style="display:none" /></label>
      </div>
      {#if mensajeImgHotel}<div class="adm__feedback adm__feedback--{mensajeImgHotel.tipo}" style="margin-bottom:1rem">{mensajeImgHotel.texto}</div>{/if}
      {#if hotelDetalle.imagenesIds?.length > 0}
        <div class="adm__img-grid">{#each hotelDetalle.imagenesIds as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/hotel/{imgId}" alt="Imagen {imgId}" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHotel(imgId)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>
      {:else}<div class="adm__img-empty"><p>Sin imágenes. Agrega la primera.</p></div>{/if}
    </div>

  <!-- ── Amenidades ── -->
  {:else if tabDetalle === 'amenidades'}
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;flex-wrap:wrap;gap:.5rem">
      <span style="color:var(--adm-text-muted);font-size:.85rem">{amenidadesHotel.length} amenidad(es) asignada(s)</span>
      <div style="display:flex;gap:.6rem">
        <button class="adm__btn adm__btn--ghost" on:click={() => { showFormNuevaAmenidadCatalogo = !showFormNuevaAmenidadCatalogo; mensajeNuevaAmenidadCatalogo = null; nuevaAmenidadCatalogoNombre = ''; }}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/><line x1="12" y1="9" x2="12" y2="15"/><line x1="9" y1="12" x2="15" y2="12"/></svg>
          Nueva categoría
        </button>
        <button class="adm__btn adm__btn--primary" on:click={() => { showFormAmenidad = true; mensajeAmenidad = null; }}>+ Asignar amenidad</button>
      </div>
    </div>

    {#if showFormNuevaAmenidadCatalogo}
      <div class="adm__wizard-subcard" style="margin-bottom:1rem;border-color:rgba(123,147,255,.35)">
        <p class="adm__modal-section-title" style="color:#a5b4fc">Nueva categoría (catálogo global)</p>
        <div class="adm__form-grid"><div class="adm__field adm__field--full"><label>Nombre</label><input type="text" bind:value={nuevaAmenidadCatalogoNombre} placeholder="Ej: Spa, Terraza..." /></div></div>
        {#if mensajeNuevaAmenidadCatalogo}<div class="adm__feedback adm__feedback--{mensajeNuevaAmenidadCatalogo.tipo}" style="margin:.75rem 0">{mensajeNuevaAmenidadCatalogo.texto}</div>{/if}
        <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem">
          <button class="adm__btn adm__btn--ghost" on:click={() => showFormNuevaAmenidadCatalogo = false}>Cancelar</button>
          <button class="adm__btn adm__btn--primary" on:click={crearAmenidadCatalogo} disabled={creandoAmenidadCatalogo}>{#if creandoAmenidadCatalogo}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Creando...{:else}Crear en catálogo{/if}</button>
        </div>
      </div>
    {/if}

    {#if showFormAmenidad}
      <div class="adm__wizard-subcard" style="margin-bottom:1rem">
        <p class="adm__modal-section-title">Asignar amenidad a {hotelDetalle.nombre}</p>
        <div class="adm__form-grid">
          <div class="adm__field"><label>Tipo</label><select bind:value={nuevaAmenidad.amenidadId}>{#each amenidades.filter(a => !amenidadesHotel.some(h => h.amenidadId === a.id)) as a}<option value={a.id}>{a.nombre}</option>{/each}</select></div>
          <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaAmenidad.descripcion} rows="2" placeholder="Ej: WiFi de alta velocidad..."></textarea></div>
        </div>
        <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem">
          <button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = false}>Cancelar</button>
          <button class="adm__btn adm__btn--primary" on:click={agregarAmenidadHotel}>Asignar</button>
        </div>
      </div>
    {/if}

    {#if mensajeAmenidad}<div class="adm__feedback adm__feedback--{mensajeAmenidad.tipo}" style="margin-bottom:1rem">{mensajeAmenidad.texto}</div>{/if}

    {#if cargandoAmenidades}
      <div class="adm__loading-state"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
    {:else if amenidadesHotel.length === 0 && !showFormAmenidad && !showFormNuevaAmenidadCatalogo}
      <div class="adm__img-empty" style="padding:2.5rem 0"><p>Sin amenidades. Asigna la primera.</p></div>
    {:else}
      <div style="display:flex;flex-direction:column;gap:.75rem">
        {#each amenidadesHotel as ha (ha.id)}
          <div class="adm__amenidad-card">
            <div class="adm__amenidad-header">
              <div class="adm__amenidad-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg></div>
              <div style="flex:1;min-width:0">
                <p class="adm__amenidad-nombre">{ha.amenidadNombre}</p>
                {#if amenidadEditandoId === ha.id}<textarea class="adm__amenidad-desc-input" bind:value={editDescAmenidad} rows="2"></textarea><div style="display:flex;gap:.5rem;margin-top:.5rem"><button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarDescAmenidad(ha)}>Guardar</button><button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => amenidadEditandoId = null}>Cancelar</button></div>{:else}<p class="adm__amenidad-desc">{ha.descripcion || '—'}</p>{/if}
              </div>
              <div style="display:flex;gap:.4rem;flex-shrink:0">
                {#if amenidadEditandoId !== ha.id}<button class="adm__icon-btn adm__icon-btn--edit" on:click={() => { amenidadEditandoId = ha.id; editDescAmenidad = ha.descripcion; }}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>{/if}
                <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarAmenidad(ha.id, ha.amenidadNombre)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
              </div>
            </div>
            <div class="adm__amenidad-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (ha.imagenesIds ?? []) as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/amenidad/{imgId}" alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgAmenidad(ha.id, imgId)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgAmenidadSet.has(ha.id)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImagenAmenidad(e, ha.id)} disabled={subiendoImgAmenidadSet.has(ha.id)} style="display:none" /></label></div></div>
          </div>
        {/each}
      </div>
    {/if}

  <!-- ── Habitaciones ── -->
  {:else if tabDetalle === 'habitaciones'}
    {#if cargandoHabitaciones}
      <div class="adm__loading-state"><svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando habitaciones...</p></div>
    {:else if errorHabitaciones}
      <div class="adm__error-state"><p>{errorHabitaciones}</p><button class="adm__btn adm__btn--ghost" on:click={() => cargarHabitacionesDetalle(hotelDetalle.id)}>Reintentar</button></div>
    {:else}
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem">
        <span style="color:var(--adm-text-muted);font-size:.85rem">{habitaciones.length} habitación(es)</span>
        <button class="adm__btn adm__btn--primary" on:click={abrirModalNuevaHab}>+ Nueva Habitación</button>
      </div>
      <div class="adm__card adm__card--no-pad">
        <div class="adm__table-wrap">
          <table class="adm__table">
            <thead><tr><th>Tipo</th><th>Nro.</th><th>Cama</th><th>$/Noche</th><th>Cap.</th><th>Estado</th><th>Imgs</th><th>Acciones</th></tr></thead>
            <tbody>
              {#each habitaciones as h (h.id)}
                <tr>
                  <td><p style="font-weight:600;color:var(--adm-text);margin:0 0 2px">{h.tipoHabitacion}</p><p style="font-size:.72rem;color:var(--adm-text-muted);margin:0">ID #{h.id}</p></td>
                  <td class="adm__table-mono" style="font-size:.85rem">{h.numeroHabitacion || '—'}</td>
                  <td style="font-size:.85rem">{h.tipoCama || '—'}</td>
                  <td class="adm__table-money">$ {h.precioPorNoche?.toFixed(2) ?? '—'}</td>
                  <td class="adm__table-center">{h.capacidadMaxima ?? '—'}</td>
                  <td><span class="adm__badge {badge(h.estado)}">{h.estado}</span></td>
                  <td class="adm__table-center">{h.imagenesIds?.length ?? 0}</td>
                  <td>
                    <div style="display:flex;gap:.3rem">
                      <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditarHabitacion(h)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                      <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => abrirEliminarHab(h)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                    </div>
                  </td>
                </tr>
              {/each}
              {#if habitaciones.length === 0}<tr><td colspan="8" class="adm__empty-cell">No hay habitaciones registradas.</td></tr>{/if}
            </tbody>
          </table>
        </div>
      </div>
    {/if}
  {/if}
{/if}

<!-- ═══ MODALES ═══ -->

<!-- EDITAR habitación — mantiene numeroHabitacion editable (ya existe en BD) -->
{#if showModalHabitacion && habitacionEditando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__hotel-modal adm__hotel-modal--wide">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:#252b3b;font-size:1.4rem;display:flex;align-items:center;justify-content:center;">{#if habitacionEditando.imagenesIds?.length > 0}<img src="{API_BASE}/imagenes/habitacion/{habitacionEditando.imagenesIds[0]}" alt="hab" />{:else}<span>🛏</span>{/if}</div>
      <div class="adm__hotel-modal__info"><p class="adm__hotel-modal__name">{habitacionEditando.tipoHabitacion} — ID #{habitacionEditando.id}</p><p class="adm__hotel-modal__loc">Nro. {habitacionEditando.numeroHabitacion || '—'}</p></div>
      <button class="adm__rol-modal__close" on:click={cerrarModales}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__hotel-modal__body">
      <p class="adm__modal-section-title">Datos de la habitación</p>
      <div class="adm__form-grid">
        <div class="adm__field"><label>Tipo de Habitación</label><select bind:value={editHabitacion.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
        <div class="adm__field"><label>Número de Habitación</label><input type="text" bind:value={editHabitacion.numeroHabitacion} placeholder="Ej: 101, 202A..." /></div>
        <div class="adm__field"><label>Estado</label><select bind:value={editHabitacion.estadoId}><option value={1}>Activa</option><option value={2}>Cerrada</option></select></div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editHabitacion.descripcion} rows="3" placeholder="Descripción..."></textarea></div>
      </div>
      <div style="margin:.75rem 0;padding:.75rem 1rem;background:rgba(123,147,255,.06);border:1px solid rgba(123,147,255,.15);border-radius:8px;font-size:.78rem;color:#8b95b0">
        <strong style="color:#a5b4fc">Características (solo lectura):</strong> Cama: {habitacionEditando.tipoCama || '—'} · Capacidad: {habitacionEditando.capacidadMaxima ?? '—'} pers. · Precio noche: $ {habitacionEditando.precioPorNoche?.toFixed(2) ?? '—'}
      </div>
      {#if mensajeHabitacion}<div class="adm__feedback adm__feedback--{mensajeHabitacion.tipo}" style="margin:.75rem 0">{mensajeHabitacion.texto}</div>{/if}
      <div style="display:flex;justify-content:flex-end;margin-bottom:1.5rem"><button class="adm__btn adm__btn--primary" on:click={guardarHabitacion} disabled={guardandoHabitacion}>{#if guardandoHabitacion}Guardando...{:else}Guardar cambios{/if}</button></div>
      <div class="adm__modal-section-divider"></div>
      <div class="adm__img-section-header" style="margin-top:1rem">
        <p class="adm__modal-section-title" style="margin:0">Imágenes</p>
        <label class="adm__btn adm__btn--ghost adm__upload-btn">{#if subiendoImgHab}Subiendo...{:else}+ Agregar{/if}<input type="file" accept="image/*" on:change={subirImagenHabitacion} disabled={subiendoImgHab} style="display:none" /></label>
      </div>
      {#if mensajeImgHab}<div class="adm__feedback adm__feedback--{mensajeImgHab.tipo}" style="margin:.5rem 0">{mensajeImgHab.texto}</div>{/if}
      {#if habitacionEditando.imagenesIds?.length > 0}
        <div class="adm__img-grid adm__img-grid--sm" style="margin-top:.75rem">{#each habitacionEditando.imagenesIds as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/habitacion/{imgId}" alt="hab" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHab(imgId)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>
      {:else}<div class="adm__img-empty" style="padding:1.5rem 0"><p>Sin imágenes.</p></div>{/if}
    </div>
    <div class="adm__hotel-modal__footer"><button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cerrar</button></div>
  </div>
{/if}

<!-- NUEVA habitación — SIN campo numeroHabitacion -->
{#if showModalNuevaHab && hotelDetalle}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__hotel-modal adm__hotel-modal--wide">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:#252b3b;font-size:1.4rem;display:flex;align-items:center;justify-content:center;">🛏</div>
      <div class="adm__hotel-modal__info"><p class="adm__hotel-modal__name">Nueva Habitación</p><p class="adm__hotel-modal__loc">{hotelDetalle.nombre}</p></div>
      <button class="adm__rol-modal__close" on:click={cerrarModales}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__hotel-modal__body">
      <p class="adm__modal-section-title">Datos de la habitación</p>
      <!-- Nota informativa sobre número automático -->
      <div style="margin-bottom:.75rem;padding:.6rem .9rem;background:rgba(45,212,191,.06);border:1px solid rgba(45,212,191,.2);border-radius:8px;font-size:.78rem;color:#8b95b0">
        El número de habitación se asigna automáticamente según el total existente en el hotel.
      </div>
      <div class="adm__form-grid">
        <div class="adm__field"><label>Tipo de Habitación</label><select bind:value={nuevaHabGestion.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
        <div class="adm__field"><label>Estado</label><select bind:value={nuevaHabGestion.estadoId}><option value={1}>Activa</option><option value={2}>Cerrada</option></select></div>
        <div class="adm__field">
          <label>Cantidad a crear</label>
          <input type="number" bind:value={nuevaHabGestion.cantidad} min="1" max="50" />
          {#if nuevaHabGestion.cantidad > 50}<span class="adm__field-error">Máximo 50 por lote</span>{:else if nuevaHabGestion.cantidad > 1}<span style="font-size:.72rem;color:var(--adm-blue);margin-top:2px">Se crearán {nuevaHabGestion.cantidad} idénticas</span>{/if}
        </div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaHabGestion.descripcion} rows="3" placeholder="Descripción..."></textarea></div>
      </div>
      <div style="margin:.75rem 0;padding:.75rem 1rem;background:rgba(45,212,191,.06);border:1px solid rgba(45,212,191,.15);border-radius:8px;font-size:.78rem;color:#8b95b0">
        Las características (precio, cama, capacidad) son heredadas del <strong style="color:#2dd4bf">Tipo de Habitación</strong>.
      </div>
      {#if mensajeNuevaHab}<div class="adm__feedback adm__feedback--{mensajeNuevaHab.tipo}" style="margin:.75rem 0">{mensajeNuevaHab.texto}</div>{/if}
    </div>
    <div class="adm__hotel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={crearHabGestion} disabled={guardandoNuevaHab || nuevaHabGestion.cantidad > 50}>{#if guardandoNuevaHab}Creando...{:else}Crear {nuevaHabGestion.cantidad > 1 ? `${Math.min(nuevaHabGestion.cantidad,50)} Habitaciones` : 'Habitación'}{/if}</button>
    </div>
  </div>
{/if}

<!-- Eliminar habitación -->
{#if showModalEliminarHab && habEliminando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:420px">
    <div class="adm__cancel-modal__header"><div class="adm__cancel-modal__icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></div><div><p class="adm__cancel-modal__title">Eliminar Habitación</p><p class="adm__cancel-modal__subtitle">{habEliminando.tipoHabitacion} Nro. {habEliminando.numeroHabitacion || habEliminando.id}</p></div><button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-info-box"><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Tipo</span><span class="adm__cancel-info-row__value">{habEliminando.tipoHabitacion}</span></div><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Número</span><span class="adm__cancel-info-row__value">{habEliminando.numeroHabitacion || '—'}</span></div></div>
      <div class="adm__cancel-warning"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg><span>Esta acción no se puede deshacer.</span></div>
    </div>
    <div class="adm__cancel-modal__footer"><button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={eliminandoHab}>Cancelar</button><button class="adm__btn--cancel-confirm" on:click={confirmarEliminarHab} disabled={eliminandoHab}>{#if eliminandoHab}Eliminando...{:else}Sí, eliminar{/if}</button></div>
  </div>
{/if}

<!-- Eliminar hotel -->
{#if showModalEliminarHotel && hotelEliminando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:460px">
    <div class="adm__cancel-modal__header"><div class="adm__cancel-modal__icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></div><div><p class="adm__cancel-modal__title">Eliminar Hotel</p><p class="adm__cancel-modal__subtitle">{hotelEliminando.nombre} — ID #{hotelEliminando.id}</p></div><button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-info-box"><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Hotel</span><span class="adm__cancel-info-row__value">{hotelEliminando.nombre}</span></div><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Ubicación</span><span class="adm__cancel-info-row__value">{hotelEliminando.ciudad}, {hotelEliminando.pais}</span></div><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Habitaciones</span><span class="adm__cancel-info-row__value">{hotelEliminando.cantidadHabitaciones ?? 0}</span></div></div>
      <div class="adm__cancel-warning"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg><span>Se eliminarán <strong>todas</strong> las habitaciones, amenidades e imágenes.</span></div>
    </div>
    <div class="adm__cancel-modal__footer"><button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={eliminandoHotel}>Cancelar</button><button class="adm__btn--cancel-confirm" on:click={() => _eliminarHotel(vistaHoteles === 'detalle')} disabled={eliminandoHotel}>{#if eliminandoHotel}Eliminando...{:else}Sí, eliminar hotel{/if}</button></div>
  </div>
{/if}

<!-- Custom confirm -->
{#if confirmDialog}<div class="adm__overlay" on:click={cerrarConfirm} on:keydown={e => e.key === 'Escape' && cerrarConfirm()} role="button" tabindex="-1" aria-label="Cerrar"></div><div class="adm__confirm"><div class="adm__confirm__header"><div class="adm__confirm__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div><p class="adm__confirm__title">{confirmDialog.titulo}</p></div><div class="adm__confirm__body"><p>{confirmDialog.mensaje}</p></div><div class="adm__confirm__footer"><button class="adm__confirm__btn-cancel" on:click={cerrarConfirm}>Cancelar</button><button class="adm__confirm__btn-ok" on:click={ejecutarConfirm}>Confirmar</button></div></div>{/if}