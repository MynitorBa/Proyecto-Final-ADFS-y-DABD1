<script>
  import { onMount } from 'svelte';
  export let API_BASE; export let badge; export let fileToBase64;
  export let tiposHabitacion; export let tiposCama; export let onFinish = () => {};

  let pasoActual = 'info'; let hotelCreadoId = null; let hotelCreadoNombre = '';
  let amenidades = [];

  // Custom confirm + toasts
  let confirmDialog = null;
  let toasts = []; let toastId = 0;
  function pedirConfirmacion(t, m, fn) { confirmDialog = { titulo: t, mensaje: m, onConfirm: fn }; }
  function cerrarConfirm() { confirmDialog = null; }
  function ejecutarConfirm() { if (confirmDialog?.onConfirm) confirmDialog.onConfirm(); confirmDialog = null; }
  function toast(texto, tipo = 'ok') { const id = ++toastId; toasts = [...toasts, { id, texto, tipo }]; setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 3000); }

  // Paso 1
  let nuevoHotel = { nombre: '', direccion: '', descripcion: '', rating: 3.0, estadoId: 1, ciudadNombre: '' };
  let guardandoNuevoHotel = false; let mensajeNuevoHotel = null;
  let todosLosPaisesWizard = []; let wizardPaisQuery = ''; let wizardPaisesSugeridos = [];
  let wizardPaisSeleccionado = null; let wizardPaisError = '';
  let wizardCiudadesSugeridas = []; let wizardCiudadSeleccionada = false; let wizardCiudadError = '';

  // Paso 2
  let imagenesNuevoHotel = []; let subiendoImgNuevoHotel = false;
  let amenidadesNuevoHotel = []; let showFormAmenidad = false;
  let nuevaAmenidad = { amenidadId: 1, descripcion: '' }; let mensajeAmenidad = null;
  let subiendoImgAmenidadSet = new Set(); let amenidadEditandoId = null; let editDescAmenidad = '';

  // Paso 3
  let habitacionesNuevas = []; let showFormHabNueva = false;
  let nuevaHabitacion = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1, cantidad: 1, mismasImagenes: true };
  let guardandoNuevaHab = false; let mensajeNuevaHab = null;
  let imagenesHabNueva = {}; let subiendoImgHabNuevaSet = new Set();
  let editandoHabId = null; let editHab = {};
  let gruposHab = [];

  // Overlay bloqueante para creación masiva
  let creandoMasivo = false;
  let creandoMasivoProgreso = '';

  // Overlay bloqueante para eliminación
  let eliminandoMasivo = false;
  let eliminandoMasivoProgreso = '';

  onMount(() => { cargarPaisesWizard(); cargarAmenidadesCatalogo(); });

  async function cargarPaisesWizard() { if (todosLosPaisesWizard.length) return; try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); todosLosPaisesWizard = d.data ?? []; } catch {} }
  async function cargarAmenidadesCatalogo() { try { const r = await fetch(`${API_BASE}/admin/amenidades`, { credentials: 'include' }); if (r.ok) amenidades = await r.json(); } catch {} }

  function onWizardPaisInput() { wizardPaisSeleccionado = null; wizardPaisError = ''; nuevoHotel.ciudadNombre = ''; wizardCiudadesSugeridas = []; wizardCiudadSeleccionada = false; const q = wizardPaisQuery.toLowerCase(); wizardPaisesSugeridos = q.length < 2 ? [] : todosLosPaisesWizard.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6); }
  function seleccionarWizardPais(p) { wizardPaisSeleccionado = p; wizardPaisQuery = p.country; wizardPaisesSugeridos = []; wizardPaisError = ''; nuevoHotel.ciudadNombre = ''; wizardCiudadesSugeridas = []; wizardCiudadSeleccionada = false; }
  function validarWizardPais() { if (wizardPaisQuery && !wizardPaisSeleccionado) { wizardPaisError = 'Selecciona un país de la lista.'; wizardPaisQuery = ''; } }
  function onWizardCiudadInput() { wizardCiudadSeleccionada = false; wizardCiudadError = ''; if (!wizardPaisSeleccionado) return; const q = nuevoHotel.ciudadNombre.toLowerCase(); wizardCiudadesSugeridas = q.length < 2 ? [] : (wizardPaisSeleccionado.cities ?? []).filter(c => c.toLowerCase().includes(q)).slice(0, 6); }
  function seleccionarWizardCiudad(c) { nuevoHotel.ciudadNombre = c; wizardCiudadesSugeridas = []; wizardCiudadSeleccionada = true; wizardCiudadError = ''; }

  async function crearNuevoHotel() {
    if (!nuevoHotel.nombre.trim()) { mensajeNuevoHotel = { tipo: 'error', texto: 'El nombre es obligatorio.' }; return; }
    if (!wizardPaisSeleccionado) { wizardPaisError = 'Selecciona un país de la lista.'; return; }
    if (!nuevoHotel.ciudadNombre.trim()) { mensajeNuevoHotel = { tipo: 'error', texto: 'La ciudad es obligatoria.' }; return; }
    guardandoNuevoHotel = true; mensajeNuevoHotel = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nombre: nuevoHotel.nombre.trim(), direccion: nuevoHotel.direccion.trim(), descripcion: nuevoHotel.descripcion.trim(), rating: Number(nuevoHotel.rating), estadoId: Number(nuevoHotel.estadoId), ciudad: nuevoHotel.ciudadNombre.trim(), paisNombre: wizardPaisSeleccionado.country }) });
      const data = await res.json(); if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      hotelCreadoId = data.id; hotelCreadoNombre = nuevoHotel.nombre;
      toast(`Hotel "${nuevoHotel.nombre}" creado (ID #${data.id})`); pasoActual = 'contenido';
    } catch (e) { mensajeNuevoHotel = { tipo: 'error', texto: e.message }; } finally { guardandoNuevoHotel = false; }
  }

  async function subirImagenNuevoHotel(ev) { const f = ev.target.files[0]; if (!f || !hotelCreadoId) return; subiendoImgNuevoHotel = true; try { const b = await fileToBase64(f); const r = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); const d = await r.json(); if (!r.ok) throw new Error(d.mensaje || 'Error'); imagenesNuevoHotel = [...imagenesNuevoHotel, { id: d.id, preview: b }]; toast('Imagen subida'); } catch (e) { toast(e.message, 'error'); } finally { subiendoImgNuevoHotel = false; ev.target.value = ''; } }
  function pedirEliminarImgHotel(id) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen del hotel?', () => _eliminarImgHotel(id)); }
  async function _eliminarImgHotel(id) { try { const r = await fetch(`${API_BASE}/admin/hoteles/imagenes/${id}`, { method: 'DELETE', credentials: 'include' }); if (!r.ok) throw new Error('Error'); imagenesNuevoHotel = imagenesNuevoHotel.filter(i => i.id !== id); toast('Imagen eliminada'); } catch (e) { toast(e.message, 'error'); } }

  async function agregarAmenidad() { if (!hotelCreadoId) return; try { const r = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/amenidades`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: Number(nuevaAmenidad.amenidadId), descripcion: nuevaAmenidad.descripcion }) }); const d = await r.json(); if (!r.ok) throw new Error(d.mensaje || 'Error'); const cat = amenidades.find(a => a.id === Number(nuevaAmenidad.amenidadId)); amenidadesNuevoHotel = [...amenidadesNuevoHotel, { id: d.id, amenidadId: Number(nuevaAmenidad.amenidadId), amenidadNombre: cat?.nombre ?? '', descripcion: nuevaAmenidad.descripcion, imagenesIds: [] }]; showFormAmenidad = false; nuevaAmenidad = { amenidadId: 1, descripcion: '' }; toast('Amenidad agregada'); } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; } }
  async function guardarDescAmenidad(ha) { try { const r = await fetch(`${API_BASE}/admin/hoteles/amenidades/${ha.id}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: ha.amenidadId, descripcion: editDescAmenidad }) }); if (!r.ok) throw new Error('Error'); amenidadesNuevoHotel = amenidadesNuevoHotel.map(a => a.id === ha.id ? { ...a, descripcion: editDescAmenidad } : a); amenidadEditandoId = null; toast('Descripción actualizada'); } catch(e) { toast(e.message, 'error'); } }
  function pedirEliminarAmenidad(id, nom) { pedirConfirmacion('Eliminar amenidad', `¿Eliminar "${nom}" y sus imágenes?`, () => _eliminarAmenidad(id)); }
  async function _eliminarAmenidad(id) { try { await fetch(`${API_BASE}/admin/hoteles/amenidades/${id}`, { method: 'DELETE', credentials: 'include' }); amenidadesNuevoHotel = amenidadesNuevoHotel.filter(a => a.id !== id); toast('Amenidad eliminada'); } catch(e) { toast(e.message, 'error'); } }
  async function subirImgAmenidad(ev, haId) { const f = ev.target.files[0]; if (!f) return; subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet, haId]); try { const b = await fileToBase64(f); const r = await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); const d = await r.json(); if (!r.ok) throw new Error('Error'); amenidadesNuevoHotel = amenidadesNuevoHotel.map(a => a.id === haId ? { ...a, imagenesIds: [...(a.imagenesIds ?? []), d.id] } : a); } catch(e) { toast('Error: ' + e.message, 'error'); } finally { subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet].filter(i => i !== haId)); ev.target.value = ''; } }
  function pedirEliminarImgAmenidad(haId, imgId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen de la amenidad?', () => _eliminarImgAmenidad(haId, imgId)); }
  async function _eliminarImgAmenidad(haId, imgId) { try { const r = await fetch(`${API_BASE}/admin/hoteles/amenidades/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' }); if (!r.ok) throw new Error('Error'); amenidadesNuevoHotel = amenidadesNuevoHotel.map(a => a.id !== haId ? a : { ...a, imagenesIds: (a.imagenesIds ?? []).filter(i => i !== imgId) }); toast('Imagen eliminada'); } catch(e) { toast(e.message, 'error'); } }

  async function crearHabitacion() {
    if (!hotelCreadoId) return;
    const cant = Math.max(1, Math.min(50, Number(nuevaHabitacion.cantidad) || 1));
    const usarMismas = cant > 1 && nuevaHabitacion.mismasImagenes;
    guardandoNuevaHab = true; mensajeNuevaHab = null;
    if (cant > 1) { creandoMasivo = true; creandoMasivoProgreso = `Creando habitación 0 de ${cant}...`; }
    try {
      const p = { tipoHabitacionId: Number(nuevaHabitacion.tipoHabitacionId), camaId: Number(nuevaHabitacion.camaId), precioPorPersona: Number(nuevaHabitacion.precioPorPersona), precioPorNoche: Number(nuevaHabitacion.precioPorNoche), capacidadMaxima: Number(nuevaHabitacion.capacidadMaxima), metrosCuadrados: Number(nuevaHabitacion.metrosCuadrados), descripcion: nuevaHabitacion.descripcion, estadoId: Number(nuevaHabitacion.estadoId) };
      const tn = tiposHabitacion.find(t => t.id === p.tipoHabitacionId)?.nombre ?? '';
      const cn = tiposCama.find(c => c.id === p.camaId)?.nombre ?? '';
      let creadas = 0; let idsLote = [];
      for (let i = 0; i < cant; i++) {
        if (cant > 1) creandoMasivoProgreso = `Creando habitación ${i + 1} de ${cant}...`;
        const r = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/habitaciones`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(p) });
        const d = await r.json(); if (!r.ok) throw new Error(d.mensaje || 'Error');
        const newH = { ...p, id: d.id, tipoHabitacion: tn, tipoCama: cn, imagenesIds: [] };
        if (!usarMismas) { habitacionesNuevas = [...habitacionesNuevas, newH]; }
        imagenesHabNueva = { ...imagenesHabNueva, [d.id]: [] }; idsLote.push(d.id); creadas++;
      }
      if (usarMismas) { gruposHab = [...gruposHab, { ids: idsLote, tipoHabitacion: tn, tipoCama: cn, cantidad: cant, precioPorNoche: p.precioPorNoche, precioPorPersona: p.precioPorPersona, capacidadMaxima: p.capacidadMaxima, metrosCuadrados: p.metrosCuadrados, tipoHabitacionId: p.tipoHabitacionId, camaId: p.camaId, descripcion: p.descripcion ?? '' }]; }
      toast(`${creadas} habitación(es) ${tn} creada(s)`); showFormHabNueva = false;
      nuevaHabitacion = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1, cantidad: 1, mismasImagenes: true };
    } catch (e) { mensajeNuevaHab = { tipo: 'error', texto: e.message }; }
    finally { guardandoNuevaHab = false; creandoMasivo = false; }
  }

  // Subir imagen a TODAS las habitaciones de un grupo
  let subiendoImgGrupoSet = new Set();
  async function subirImgGrupo(ev, grupoIdx) { const f = ev.target.files[0]; if (!f) return; const grupo = gruposHab[grupoIdx]; if (!grupo) return; subiendoImgGrupoSet = new Set([...subiendoImgGrupoSet, grupoIdx]); try { const b = await fileToBase64(f); for (const hId of grupo.ids) { const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); const d = await r.json(); if (!r.ok) throw new Error('Error'); imagenesHabNueva = { ...imagenesHabNueva, [hId]: [...(imagenesHabNueva[hId] ?? []), { id: d.id, preview: b }] }; } toast(`Imagen agregada a ${grupo.ids.length} habitaciones`); } catch(e) { toast('Error: ' + e.message, 'error'); } finally { subiendoImgGrupoSet = new Set([...subiendoImgGrupoSet].filter(i => i !== grupoIdx)); ev.target.value = ''; } }

  function pedirEliminarImgGrupo(grupoIdx, imgIdx) { pedirConfirmacion('Eliminar imagen', `¿Eliminar esta imagen de las ${gruposHab[grupoIdx]?.ids.length} habitaciones?`, () => _eliminarImgGrupo(grupoIdx, imgIdx)); }
  async function _eliminarImgGrupo(grupoIdx, imgIdx) { const grupo = gruposHab[grupoIdx]; if (!grupo) return; const firstId = grupo.ids[0]; const imgs = imagenesHabNueva[firstId] ?? []; const img = imgs[imgIdx]; if (!img) return; try { for (const hId of grupo.ids) { const habImgs = imagenesHabNueva[hId] ?? []; const match = habImgs[imgIdx]; if (match) { await fetch(`${API_BASE}/admin/habitaciones/imagenes/${match.id}`, { method: 'DELETE', credentials: 'include' }); imagenesHabNueva = { ...imagenesHabNueva, [hId]: habImgs.filter(i => i.id !== match.id) }; } } toast('Imagen eliminada de todas las habitaciones'); } catch(e) { toast(e.message, 'error'); } }

  function pedirEliminarGrupo(grupoIdx) { const g = gruposHab[grupoIdx]; pedirConfirmacion('Eliminar grupo', `¿Eliminar las ${g.cantidad} habitaciones ${g.tipoHabitacion} y sus imágenes?`, () => _eliminarGrupo(grupoIdx)); }
  async function _eliminarGrupo(grupoIdx) { const g = gruposHab[grupoIdx]; if (!g) return; eliminandoMasivo = true; eliminandoMasivoProgreso = `Eliminando ${g.cantidad} habitaciones...`; try { for (let i = 0; i < g.ids.length; i++) { eliminandoMasivoProgreso = `Eliminando habitación ${i + 1} de ${g.ids.length}...`; await fetch(`${API_BASE}/admin/habitaciones/${g.ids[i]}`, { method: 'DELETE', credentials: 'include' }); delete imagenesHabNueva[g.ids[i]]; } imagenesHabNueva = { ...imagenesHabNueva }; gruposHab = gruposHab.filter((_, i) => i !== grupoIdx); toast(`${g.cantidad} habitaciones eliminadas`); } catch(e) { toast(e.message, 'error'); } finally { eliminandoMasivo = false; } }
  async function subirImgHab(ev, hId) { const f = ev.target.files[0]; if (!f) return; subiendoImgHabNuevaSet = new Set([...subiendoImgHabNuevaSet, hId]); try { const b = await fileToBase64(f); const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); const d = await r.json(); if (!r.ok) throw new Error('Error'); imagenesHabNueva = { ...imagenesHabNueva, [hId]: [...(imagenesHabNueva[hId] ?? []), { id: d.id, preview: b }] }; } catch {} finally { subiendoImgHabNuevaSet = new Set([...subiendoImgHabNuevaSet].filter(i => i !== hId)); ev.target.value = ''; } }
  function pedirEliminarImgHab(hId, imgId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen?', () => _eliminarImgHab(hId, imgId)); }
  async function _eliminarImgHab(hId, imgId) { try { await fetch(`${API_BASE}/admin/habitaciones/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' }); imagenesHabNueva = { ...imagenesHabNueva, [hId]: (imagenesHabNueva[hId] ?? []).filter(i => i.id !== imgId) }; toast('Imagen eliminada'); } catch(e) { toast(e.message, 'error'); } }
  function abrirEditHab(h) { editandoHabId = h.id; editHab = { tipoHabitacionId: h.tipoHabitacionId, camaId: h.camaId, precioPorPersona: h.precioPorPersona, precioPorNoche: h.precioPorNoche, capacidadMaxima: h.capacidadMaxima, metrosCuadrados: h.metrosCuadrados, descripcion: h.descripcion ?? '', estadoId: h.estadoId }; }
  async function guardarEditHab(hId) { try { const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...editHab, tipoHabitacionId: Number(editHab.tipoHabitacionId), camaId: Number(editHab.camaId), precioPorPersona: Number(editHab.precioPorPersona), precioPorNoche: Number(editHab.precioPorNoche), capacidadMaxima: Number(editHab.capacidadMaxima), metrosCuadrados: Number(editHab.metrosCuadrados), estadoId: Number(editHab.estadoId) }) }); if (!r.ok) throw new Error('Error'); const tn = tiposHabitacion.find(t => t.id === Number(editHab.tipoHabitacionId))?.nombre ?? ''; const cn = tiposCama.find(c => c.id === Number(editHab.camaId))?.nombre ?? ''; habitacionesNuevas = habitacionesNuevas.map(h => h.id === hId ? { ...h, ...editHab, tipoHabitacion: tn, tipoCama: cn } : h); editandoHabId = null; toast('Habitación actualizada'); } catch(e) { toast(e.message, 'error'); } }
  function pedirEliminarHab(hId, tipo) { pedirConfirmacion('Eliminar habitación', `¿Eliminar "${tipo}" (ID #${hId})?`, () => _eliminarHab(hId)); }
  async function _eliminarHab(hId) { try { await fetch(`${API_BASE}/admin/habitaciones/${hId}`, { method: 'DELETE', credentials: 'include' }); habitacionesNuevas = habitacionesNuevas.filter(h => h.id !== hId); delete imagenesHabNueva[hId]; imagenesHabNueva = { ...imagenesHabNueva }; toast('Habitación eliminada'); } catch(e) { toast(e.message, 'error'); } }

  // Editar grupo completo
  let editandoGrupoIdx = null;
  let editGrupo = {};

  function abrirEditGrupo(gi) {
    const g = gruposHab[gi]; if (!g) return;
    editandoGrupoIdx = gi;
    editGrupo = { tipoHabitacionId: g.tipoHabitacionId ?? 1, camaId: g.camaId ?? 1, precioPorPersona: g.precioPorPersona ?? 0, precioPorNoche: g.precioPorNoche ?? 0, capacidadMaxima: g.capacidadMaxima ?? 2, metrosCuadrados: g.metrosCuadrados ?? 25, descripcion: g.descripcion ?? '' };
  }

  async function guardarEditGrupo(gi) {
    const g = gruposHab[gi]; if (!g) return;
    const payload = { tipoHabitacionId: Number(editGrupo.tipoHabitacionId), camaId: Number(editGrupo.camaId), precioPorPersona: Number(editGrupo.precioPorPersona), precioPorNoche: Number(editGrupo.precioPorNoche), capacidadMaxima: Number(editGrupo.capacidadMaxima), metrosCuadrados: Number(editGrupo.metrosCuadrados), descripcion: editGrupo.descripcion };
    try {
      for (const hId of g.ids) {
        const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        if (!r.ok) throw new Error('Error al actualizar habitación #' + hId);
      }
      const tn = tiposHabitacion.find(t => t.id === payload.tipoHabitacionId)?.nombre ?? '';
      const cn = tiposCama.find(c => c.id === payload.camaId)?.nombre ?? '';
      gruposHab = gruposHab.map((gr, i) => i === gi ? { ...gr, ...payload, tipoHabitacion: tn, tipoCama: cn } : gr);
      editandoGrupoIdx = null;
      toast(`${g.ids.length} habitaciones actualizadas`);
    } catch(e) { toast(e.message, 'error'); }
  }

  function resetCrearHotel() { pasoActual = 'info'; hotelCreadoId = null; hotelCreadoNombre = ''; nuevoHotel = { nombre: '', direccion: '', descripcion: '', rating: 3.0, estadoId: 1, ciudadNombre: '' }; mensajeNuevoHotel = null; imagenesNuevoHotel = []; amenidadesNuevoHotel = []; habitacionesNuevas = []; gruposHab = []; showFormHabNueva = false; showFormAmenidad = false; nuevaHabitacion = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1, cantidad: 1, mismasImagenes: true }; nuevaAmenidad = { amenidadId: 1, descripcion: '' }; mensajeNuevaHab = null; mensajeAmenidad = null; imagenesHabNueva = {}; subiendoImgHabNuevaSet = new Set(); subiendoImgAmenidadSet = new Set(); subiendoImgGrupoSet = new Set(); wizardPaisQuery = ''; wizardPaisSeleccionado = null; wizardPaisError = ''; wizardPaisesSugeridos = []; wizardCiudadesSugeridas = []; wizardCiudadSeleccionada = false; wizardCiudadError = ''; amenidadEditandoId = null; editandoHabId = null; editandoGrupoIdx = null; }
</script>

<!-- Overlay bloqueante creación masiva -->
{#if creandoMasivo}
  <div class="adm__overlay" style="z-index:3500"></div>
  <div class="adm__confirm" style="z-index:3501;text-align:center">
    <div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem">
      <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#7b93ff" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p class="adm__confirm__title" style="font-size:1rem">Creando habitaciones</p>
    </div>
    <div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem">
      <p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{creandoMasivoProgreso}</p>
      <p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera, esto puede tardar unos minutos si son muchas habitaciones. No cierres esta página.</p>
    </div>
  </div>
{/if}

<!-- Overlay bloqueante eliminación -->
{#if eliminandoMasivo}
  <div class="adm__overlay" style="z-index:3500"></div>
  <div class="adm__confirm" style="z-index:3501;text-align:center">
    <div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem">
      <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f85149" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p class="adm__confirm__title" style="font-size:1rem">Eliminando</p>
    </div>
    <div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem">
      <p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{eliminandoMasivoProgreso}</p>
      <p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera, no cierres esta página.</p>
    </div>
  </div>
{/if}

<!-- Toasts -->
{#if toasts.length > 0}<div class="adm__toast-container">{#each toasts as t (t.id)}<div class="adm__toast adm__toast--{t.tipo}">{#if t.tipo === 'ok'}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>{/if}{t.texto}<button class="adm__toast__close" on:click={() => toasts = toasts.filter(x => x.id !== t.id)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>{/if}

<!-- Steps -->
<div class="adm__wizard-steps">
  <div class="adm__wizard-step" class:adm__wizard-step--done={hotelCreadoId && pasoActual !== 'info'} class:adm__wizard-step--active={pasoActual === 'info'}><div class="adm__wizard-step-num">1</div><span>Información</span></div>
  <div class="adm__wizard-connector"></div>
  <div class="adm__wizard-step" class:adm__wizard-step--done={pasoActual === 'habitaciones'} class:adm__wizard-step--active={pasoActual === 'contenido'} class:adm__wizard-step--disabled={!hotelCreadoId}><div class="adm__wizard-step-num">2</div><span>Imágenes y Amenidades</span></div>
  <div class="adm__wizard-connector"></div>
  <div class="adm__wizard-step" class:adm__wizard-step--active={pasoActual === 'habitaciones'} class:adm__wizard-step--disabled={!hotelCreadoId}><div class="adm__wizard-step-num">3</div><span>Habitaciones</span></div>
</div>

{#if pasoActual === 'info'}
<div class="adm__wizard-card">
  <h3 class="adm__wizard-card-title">Datos del nuevo hotel</h3>
  <div class="adm__form-grid adm__form-grid--wizard">
    <div class="adm__field adm__field--full"><label>Nombre del Hotel *</label><input type="text" bind:value={nuevoHotel.nombre} placeholder="Ej: Miku Inn Paris" /></div>
    <div class="adm__field"><label>País *</label><div class="adm__autocomplete-wrap"><input type="text" bind:value={wizardPaisQuery} on:input={onWizardPaisInput} on:blur={() => setTimeout(validarWizardPais, 150)} placeholder="Escribe el país..." autocomplete="off" />{#if wizardPaisesSugeridos.length > 0}<ul class="adm__autocomplete-list">{#each wizardPaisesSugeridos as p}<li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarWizardPais(p)}>{p.country}</button></li>{/each}</ul>{/if}</div>{#if wizardPaisError}<span class="adm__field-error">{wizardPaisError}</span>{/if}</div>
    <div class="adm__field"><label>Ciudad *</label><div class="adm__autocomplete-wrap"><input type="text" bind:value={nuevoHotel.ciudadNombre} on:input={onWizardCiudadInput} on:blur={() => setTimeout(() => wizardCiudadesSugeridas = [], 150)} placeholder={wizardPaisSeleccionado ? "Escribe la ciudad..." : "Primero selecciona un país"} disabled={!wizardPaisSeleccionado} autocomplete="off" />{#if wizardCiudadesSugeridas.length > 0}<ul class="adm__autocomplete-list">{#each wizardCiudadesSugeridas as c}<li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarWizardCiudad(c)}>{c}</button></li>{/each}</ul>{/if}</div>{#if wizardCiudadError}<span class="adm__field-error">{wizardCiudadError}</span>{/if}</div>
    <div class="adm__field adm__field--full"><label>Dirección</label><input type="text" bind:value={nuevoHotel.direccion} placeholder="Calle, número, colonia..." /></div>
    <div class="adm__field"><label>Rating (0–5)</label><input type="number" bind:value={nuevoHotel.rating} min="0" max="5" step="0.1" /></div>
    <div class="adm__field"><label>Estado</label><select bind:value={nuevoHotel.estadoId}><option value={1}>Activo</option><option value={2}>Cerrado</option></select></div>
    <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevoHotel.descripcion} rows="4" placeholder="Describe el hotel..."></textarea></div>
  </div>
  {#if mensajeNuevoHotel}<div class="adm__feedback adm__feedback--{mensajeNuevoHotel.tipo}" style="margin-top:1rem">{mensajeNuevoHotel.texto}</div>{/if}
  <div class="adm__wizard-actions"><button class="adm__btn adm__btn--ghost" on:click={resetCrearHotel}>Limpiar</button><button class="adm__btn adm__btn--primary adm__btn--lg" on:click={crearNuevoHotel} disabled={guardandoNuevoHotel}>{#if guardandoNuevoHotel}Creando...{:else}Crear Hotel y Continuar →{/if}</button></div>
</div>

{:else if pasoActual === 'contenido'}
<div class="adm__wizard-card">
  <h3 class="adm__wizard-card-title">Imágenes y Amenidades — <strong>{hotelCreadoNombre}</strong></h3>
  <p class="adm__modal-section-title">Imágenes del hotel</p>
  <div class="adm__img-section-header" style="margin-bottom:.75rem"><span style="font-size:.8rem;color:var(--adm-text-muted)">{imagenesNuevoHotel.length} imagen(es)</span><label class="adm__btn adm__btn--ghost adm__upload-btn">{#if subiendoImgNuevoHotel}Subiendo...{:else}+ Agregar imagen{/if}<input type="file" accept="image/*" on:change={subirImagenNuevoHotel} disabled={subiendoImgNuevoHotel} style="display:none" /></label></div>
  {#if imagenesNuevoHotel.length > 0}<div class="adm__img-grid" style="margin-bottom:1.5rem">{#each imagenesNuevoHotel as img (img.id)}<div class="adm__img-card"><img src={img.preview} alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHotel(img.id)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>{:else}<div class="adm__img-empty" style="padding:1rem 0;margin-bottom:1.5rem"><p>Sin imágenes aún.</p></div>{/if}

  <div class="adm__modal-section-divider"></div>
  <div style="display:flex;justify-content:space-between;align-items:center;margin:.75rem 0"><p class="adm__modal-section-title" style="margin:0">Amenidades</p><button class="adm__btn adm__btn--ghost" on:click={() => { showFormAmenidad = !showFormAmenidad; mensajeAmenidad = null; }}>+ Agregar</button></div>
  {#if showFormAmenidad}<div class="adm__wizard-subcard" style="margin-bottom:1rem"><div class="adm__form-grid adm__form-grid--wizard"><div class="adm__field"><label>Tipo</label><select bind:value={nuevaAmenidad.amenidadId}>{#each amenidades.filter(a => !amenidadesNuevoHotel.some(h => h.amenidadId === a.id)) as a}<option value={a.id}>{a.nombre}</option>{/each}</select></div><div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaAmenidad.descripcion} rows="2" placeholder="Describe..."></textarea></div></div><div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem"><button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = false}>Cancelar</button><button class="adm__btn adm__btn--primary" on:click={agregarAmenidad}>Agregar</button></div></div>{/if}
  {#if mensajeAmenidad}<div class="adm__feedback adm__feedback--{mensajeAmenidad.tipo}" style="margin-bottom:.75rem">{mensajeAmenidad.texto}</div>{/if}

  {#if amenidadesNuevoHotel.length > 0}<div style="display:flex;flex-direction:column;gap:.75rem;margin-bottom:1rem">{#each amenidadesNuevoHotel as ha (ha.id)}<div class="adm__amenidad-card"><div class="adm__amenidad-header"><div class="adm__amenidad-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg></div><div style="flex:1;min-width:0"><p class="adm__amenidad-nombre">{ha.amenidadNombre}</p>{#if amenidadEditandoId === ha.id}<textarea class="adm__amenidad-desc-input" bind:value={editDescAmenidad} rows="2"></textarea><div style="display:flex;gap:.5rem;margin-top:.5rem"><button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarDescAmenidad(ha)}>Guardar</button><button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => amenidadEditandoId = null}>Cancelar</button></div>{:else}<p class="adm__amenidad-desc">{ha.descripcion || '—'}</p>{/if}</div><div style="display:flex;gap:.4rem;flex-shrink:0">{#if amenidadEditandoId !== ha.id}<button class="adm__icon-btn adm__icon-btn--edit" on:click={() => { amenidadEditandoId = ha.id; editDescAmenidad = ha.descripcion; }}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>{/if}<button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarAmenidad(ha.id, ha.amenidadNombre)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button></div></div>
  <div class="adm__amenidad-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (ha.imagenesIds ?? []) as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/amenidad/{imgId}" alt="amenidad" /><button class="adm__img-delete" on:click={() => pedirEliminarImgAmenidad(ha.id, imgId)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgAmenidadSet.has(ha.id)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImgAmenidad(e, ha.id)} disabled={subiendoImgAmenidadSet.has(ha.id)} style="display:none" /></label></div></div></div>{/each}</div>{:else if !showFormAmenidad}<div class="adm__img-empty" style="padding:1rem 0"><p>Sin amenidades aún.</p></div>{/if}

  <div class="adm__wizard-actions"><button class="adm__btn adm__btn--ghost" on:click={() => pasoActual = 'info'}>← Volver</button><button class="adm__btn adm__btn--primary adm__btn--lg" on:click={() => pasoActual = 'habitaciones'}>Continuar a Habitaciones →</button></div>
</div>

{:else if pasoActual === 'habitaciones'}
<div class="adm__wizard-card">
  <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:1.25rem;flex-wrap:wrap;gap:.75rem"><h3 class="adm__wizard-card-title" style="margin:0">Habitaciones — <strong>{hotelCreadoNombre}</strong></h3><button class="adm__btn adm__btn--primary" on:click={() => { showFormHabNueva = true; mensajeNuevaHab = null; }}>+ Agregar Habitación</button></div>

  {#if showFormHabNueva}<div class="adm__wizard-subcard"><p class="adm__modal-section-title">Nueva Habitación</p><div class="adm__form-grid adm__form-grid--wizard"><div class="adm__field"><label>Tipo</label><select bind:value={nuevaHabitacion.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div><div class="adm__field"><label>Cama</label><select bind:value={nuevaHabitacion.camaId}>{#each tiposCama as c}<option value={c.id}>{c.nombre}</option>{/each}</select></div><div class="adm__field"><label>$/Noche</label><input type="number" bind:value={nuevaHabitacion.precioPorNoche} min="0" step="0.01" /></div><div class="adm__field"><label>$/Persona</label><input type="number" bind:value={nuevaHabitacion.precioPorPersona} min="0" step="0.01" /></div><div class="adm__field"><label>Capacidad</label><input type="number" bind:value={nuevaHabitacion.capacidadMaxima} min="1" /></div><div class="adm__field"><label>m²</label><input type="number" bind:value={nuevaHabitacion.metrosCuadrados} min="0" step="0.1" /></div><div class="adm__field"><label>Estado</label><select bind:value={nuevaHabitacion.estadoId}><option value={1}>Activa</option><option value={2}>Cerrada</option></select></div><div class="adm__field"><label>Cantidad</label><input type="number" bind:value={nuevaHabitacion.cantidad} min="1" max="50" />{#if nuevaHabitacion.cantidad > 50}<span class="adm__field-error">Máximo 50 habitaciones por lote</span>{:else if nuevaHabitacion.cantidad > 1}<span style="font-size:.72rem;color:var(--adm-blue);margin-top:2px">Se crearán {nuevaHabitacion.cantidad} idénticas</span>{/if}<span style="font-size:.68rem;color:var(--adm-text-muted);margin-top:2px">Máximo 50 por lote</span></div><div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaHabitacion.descripcion} rows="2" placeholder="Descripción..."></textarea></div>
  {#if nuevaHabitacion.cantidad > 1}
    <div class="adm__field adm__field--full">
      <label>Imágenes de las habitaciones</label>
      <div style="display:flex;gap:.5rem;margin-top:.35rem">
        <button type="button" class="adm__btn {nuevaHabitacion.mismasImagenes ? 'adm__btn--primary' : 'adm__btn--ghost'} adm__btn--xs" style="flex:1;justify-content:center" on:click={() => nuevaHabitacion.mismasImagenes = true}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          Mismas imágenes para todas
        </button>
        <button type="button" class="adm__btn {!nuevaHabitacion.mismasImagenes ? 'adm__btn--primary' : 'adm__btn--ghost'} adm__btn--xs" style="flex:1;justify-content:center" on:click={() => nuevaHabitacion.mismasImagenes = false}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          Imágenes individuales por cada una
        </button>
      </div>
      <span style="font-size:.72rem;color:var(--adm-text-muted);margin-top:4px">
        {#if nuevaHabitacion.mismasImagenes}Se mostrará 1 sola tarjeta y las imágenes se aplicarán a las {nuevaHabitacion.cantidad} habitaciones.{:else}Se mostrará cada habitación por separado para agregar imágenes individualmente.{/if}
      </span>
    </div>
  {/if}
  </div>{#if mensajeNuevaHab}<div class="adm__feedback adm__feedback--{mensajeNuevaHab.tipo}" style="margin:.75rem 0">{mensajeNuevaHab.texto}</div>{/if}<div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:1rem"><button class="adm__btn adm__btn--ghost" on:click={() => showFormHabNueva = false}>Cancelar</button><button class="adm__btn adm__btn--primary" on:click={crearHabitacion} disabled={guardandoNuevaHab || nuevaHabitacion.cantidad > 50}>{#if guardandoNuevaHab}Creando...{:else}Crear {nuevaHabitacion.cantidad > 1 ? `${Math.min(nuevaHabitacion.cantidad, 50)} Habitaciones` : 'Habitación'}{/if}</button></div></div>{/if}

  <!-- GRUPOS (mismas imágenes para todas) -->
  {#if gruposHab.length > 0}<div style="display:flex;flex-direction:column;gap:1rem;margin-top:1rem">{#each gruposHab as g, gi (gi)}
    <div class="adm__wizard-hab-card">
      <div class="adm__wizard-hab-info" style="display:flex;justify-content:space-between;align-items:flex-start">
        <div style="flex:1">
          {#if editandoGrupoIdx === gi}
            <div class="adm__form-grid adm__form-grid--wizard" style="margin-bottom:.75rem">
              <div class="adm__field"><label>Tipo</label><select bind:value={editGrupo.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
              <div class="adm__field"><label>Cama</label><select bind:value={editGrupo.camaId}>{#each tiposCama as c}<option value={c.id}>{c.nombre}</option>{/each}</select></div>
              <div class="adm__field"><label>$/Noche</label><input type="number" bind:value={editGrupo.precioPorNoche} min="0" step="0.01" /></div>
              <div class="adm__field"><label>$/Persona</label><input type="number" bind:value={editGrupo.precioPorPersona} min="0" step="0.01" /></div>
              <div class="adm__field"><label>Capacidad</label><input type="number" bind:value={editGrupo.capacidadMaxima} min="1" /></div>
              <div class="adm__field"><label>m²</label><input type="number" bind:value={editGrupo.metrosCuadrados} min="0" step="0.1" /></div>
              <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editGrupo.descripcion} rows="2" placeholder="Descripción..."></textarea></div>
            </div>
            <div style="display:flex;gap:.5rem">
              <button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarEditGrupo(gi)}>Guardar ({g.ids.length} habitaciones)</button>
              <button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => editandoGrupoIdx = null}>Cancelar</button>
            </div>
          {:else}
            <p class="adm__wizard-hab-tipo">{g.tipoHabitacion} <span class="adm__badge badge--blue" style="font-size:.68rem;margin-left:.4rem">x{g.cantidad} habitaciones</span></p>
            <p class="adm__wizard-hab-meta">{g.tipoCama} · {g.capacidadMaxima} pers. · {g.metrosCuadrados} m² · $ {Number(g.precioPorNoche).toFixed(2)}/noche</p>
            <p style="font-size:.72rem;color:var(--adm-accent);margin:4px 0 0;font-weight:600">Las imágenes se aplican a las {g.cantidad} habitaciones (IDs: {g.ids.join(', ')})</p>
          {/if}
        </div>
        {#if editandoGrupoIdx !== gi}
          <div style="display:flex;gap:.3rem;flex-shrink:0">
            <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditGrupo(gi)} title="Editar todas"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
            <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarGrupo(gi)} title="Eliminar todas"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
          </div>
        {/if}
      </div>
      <div class="adm__wizard-hab-imgs">
        <div class="adm__img-grid adm__img-grid--sm">
          {#each (imagenesHabNueva[g.ids[0]] ?? []) as img, imgIdx (img.id)}
            <div class="adm__img-card"><img src={img.preview} alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgGrupo(gi, imgIdx)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
          {/each}
          <label class="adm__wizard-add-img-btn adm__upload-btn">
            {#if subiendoImgGrupoSet.has(gi)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}
            <input type="file" accept="image/*" on:change={(e) => subirImgGrupo(e, gi)} style="display:none" />
          </label>
        </div>
      </div>
    </div>
  {/each}</div>{/if}

  <!-- INDIVIDUALES -->
  {#if habitacionesNuevas.length > 0}<div style="display:flex;flex-direction:column;gap:1rem;margin-top:1rem">{#each habitacionesNuevas as h (h.id)}<div class="adm__wizard-hab-card"><div class="adm__wizard-hab-info" style="display:flex;justify-content:space-between;align-items:flex-start"><div style="flex:1">{#if editandoHabId === h.id}<div class="adm__form-grid adm__form-grid--wizard" style="margin-bottom:.75rem"><div class="adm__field"><label>Tipo</label><select bind:value={editHab.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div><div class="adm__field"><label>Cama</label><select bind:value={editHab.camaId}>{#each tiposCama as c}<option value={c.id}>{c.nombre}</option>{/each}</select></div><div class="adm__field"><label>$/Noche</label><input type="number" bind:value={editHab.precioPorNoche} min="0" step="0.01" /></div><div class="adm__field"><label>$/Persona</label><input type="number" bind:value={editHab.precioPorPersona} min="0" step="0.01" /></div><div class="adm__field"><label>Cap.</label><input type="number" bind:value={editHab.capacidadMaxima} min="1" /></div><div class="adm__field"><label>m²</label><input type="number" bind:value={editHab.metrosCuadrados} min="0" step="0.1" /></div></div><div style="display:flex;gap:.5rem"><button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarEditHab(h.id)}>Guardar</button><button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => editandoHabId = null}>Cancelar</button></div>{:else}<p class="adm__wizard-hab-tipo">{h.tipoHabitacion} <span style="font-size:.72rem;color:var(--adm-text-muted)">#{h.id}</span></p><p class="adm__wizard-hab-meta">{h.tipoCama} · {h.capacidadMaxima} pers. · {h.metrosCuadrados} m² · $ {Number(h.precioPorNoche).toFixed(2)}/noche</p>{/if}</div>{#if editandoHabId !== h.id}<div style="display:flex;gap:.3rem;flex-shrink:0"><button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditHab(h)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button><button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarHab(h.id, h.tipoHabitacion)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button></div>{/if}</div>
  <div class="adm__wizard-hab-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (imagenesHabNueva[h.id] ?? []) as img (img.id)}<div class="adm__img-card"><img src={img.preview} alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHab(h.id, img.id)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgHabNuevaSet.has(h.id)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImgHab(e, h.id)} style="display:none" /></label></div></div></div>{/each}</div>{/if}

  {#if gruposHab.length === 0 && habitacionesNuevas.length === 0 && !showFormHabNueva}<div class="adm__img-empty" style="padding:2rem 0"><p>Agrega las habitaciones del hotel.</p></div>{/if}

  <div class="adm__wizard-actions" style="margin-top:1.5rem"><button class="adm__btn adm__btn--ghost" on:click={() => pasoActual = 'contenido'}>← Volver</button><button class="adm__btn adm__btn--success adm__btn--lg" on:click={() => { resetCrearHotel(); onFinish(); }}>Finalizar — Ver Hoteles</button></div>
</div>
{/if}

<!-- Custom confirm -->
{#if confirmDialog}<div class="adm__overlay" on:click={cerrarConfirm} on:keydown={e => e.key === 'Escape' && cerrarConfirm()} role="button" tabindex="-1" aria-label="Cerrar"></div><div class="adm__confirm"><div class="adm__confirm__header"><div class="adm__confirm__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div><p class="adm__confirm__title">{confirmDialog.titulo}</p></div><div class="adm__confirm__body"><p>{confirmDialog.mensaje}</p></div><div class="adm__confirm__footer"><button class="adm__confirm__btn-cancel" on:click={cerrarConfirm}>Cancelar</button><button class="adm__confirm__btn-ok" on:click={ejecutarConfirm}>Confirmar</button></div></div>{/if}