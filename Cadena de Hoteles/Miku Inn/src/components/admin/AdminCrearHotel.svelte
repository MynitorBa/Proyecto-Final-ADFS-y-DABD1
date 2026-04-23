<script>
  /**
   * @file AdminCrearHotel.svelte
   * @description Asistente (wizard) de 3 pasos para crear un nuevo hotel desde el panel de administracion.
   * Paso 1: informacion basica (nombre, pais, ciudad, rating, estado, descripcion).
   * Paso 2: imagenes del hotel y amenidades con sus propias imagenes.
   * Paso 3: habitaciones individuales o en lote con imagenes por grupo.
   * El numero de habitacion es asignado automaticamente por el backend.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /**
   * Funcion que retorna la clase CSS del badge segun el estado.
   * @type {function(string): string}
   */
  export let badge;

  /**
   * Convierte un File del input a una cadena Base64 para enviarla al backend.
   * @type {function(File): Promise<string>}
   */
  export let fileToBase64;

  /** Lista de tipos de habitacion disponibles (proveniente del padre). @type {Array<{id: number, nombre: string}>} */
  export let tiposHabitacion;

  /**
   * Callback ejecutado cuando el usuario finaliza el wizard y vuelve al listado de hoteles.
   * @type {function(): void}
   */
  export let onFinish = () => {};

  /** Lista del catalogo global de amenidades disponibles. @type {Array<{id: number, nombre: string}>} */
  let amenidades = [];

  /** Dialogo de confirmacion personalizado. @type {{titulo: string, mensaje: string, onConfirm: function}|null} */
  let confirmDialog = null;

  /** Lista de notificaciones toast activas. @type {Array<{id: number, texto: string, tipo: string}>} */
  let toasts = [];

  /** Contador incremental para los IDs unicos de cada toast. @type {number} */
  let toastId = 0;

  /**
   * Abre un dialogo de confirmacion personalizado antes de ejecutar una accion destructiva.
   * @param {string} t - Titulo del dialogo.
   * @param {string} m - Mensaje descriptivo.
   * @param {function} fn - Funcion a ejecutar si el usuario confirma.
   */
  function pedirConfirmacion(t, m, fn) { confirmDialog = { titulo: t, mensaje: m, onConfirm: fn }; }

  /** Cierra el dialogo de confirmacion sin ejecutar la accion. */
  function cerrarConfirm() { confirmDialog = null; }

  /** Ejecuta la accion confirmada y cierra el dialogo. */
  function ejecutarConfirm() { if (confirmDialog?.onConfirm) confirmDialog.onConfirm(); confirmDialog = null; }

  /**
   * Muestra una notificacion toast que desaparece automaticamente a los 3 segundos.
   * @param {string} texto - Mensaje a mostrar.
   * @param {'ok'|'error'} tipo - Tipo de notificacion para el estilo visual.
   */
  function toast(texto, tipo = 'ok') { const id = ++toastId; toasts = [...toasts, { id, texto, tipo }]; setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 3000); }

  /** Paso activo del wizard: 'info', 'contenido' o 'habitaciones'. @type {string} */
  let pasoActual = 'info';

  /** ID del hotel creado en el paso 1, necesario para los pasos siguientes. @type {number|null} */
  let hotelCreadoId = null;

  /** Nombre del hotel creado, usado en los encabezados de los pasos 2 y 3. @type {string} */
  let hotelCreadoNombre = '';

  /** Datos del formulario del paso 1. @type {Object} */
  let nuevoHotel = { nombre: '', direccion: '', descripcion: '', rating: 3.0, estadoId: 1, ciudadNombre: '' };

  /** Indica si la peticion para crear el hotel esta en curso. @type {boolean} */
  let guardandoNuevoHotel = false;

  /** Mensaje de retroalimentacion del paso 1. @type {{tipo: string, texto: string}|null} */
  let mensajeNuevoHotel = null;

  /** Lista completa de paises cargados desde la API externa para el autocomplete. @type {Array<Object>} */
  let todosLosPaisesWizard = [];

  /** Texto ingresado en el campo de pais del wizard. @type {string} */
  let wizardPaisQuery = '';

  /** Sugerencias de paises mostradas en el dropdown. @type {Array<Object>} */
  let wizardPaisesSugeridos = [];

  /** Pais seleccionado en el wizard; necesario para cargar ciudades. @type {Object|null} */
  let wizardPaisSeleccionado = null;

  /** Mensaje de error especifico del campo de pais. @type {string} */
  let wizardPaisError = '';

  /** Sugerencias de ciudades del pais seleccionado. @type {Array<string>} */
  let wizardCiudadesSugeridas = [];

  /** Mensaje de error especifico del campo de ciudad. @type {string} */
  let wizardCiudadError = '';

  /** Imagenes subidas al hotel en el paso 2, guardadas con ID y preview Base64. @type {Array<{id: number, preview: string}>} */
  let imagenesNuevoHotel = [];

  /** Indica si hay una imagen de hotel siendo subida. @type {boolean} */
  let subiendoImgNuevoHotel = false;

  /** Amenidades asignadas al nuevo hotel. @type {Array<Object>} */
  let amenidadesNuevoHotel = [];

  /** Controla la visibilidad del formulario para asignar una amenidad. @type {boolean} */
  let showFormAmenidad = false;

  /** Datos de la nueva amenidad a asignar. @type {{amenidadId: number, descripcion: string}} */
  let nuevaAmenidad = { amenidadId: 1, descripcion: '' };

  /** Mensaje de retroalimentacion del formulario de amenidades. @type {{tipo: string, texto: string}|null} */
  let mensajeAmenidad = null;

  /** Set con los IDs de las amenidades cuya imagen se esta subiendo actualmente. @type {Set<number>} */
  let subiendoImgAmenidadSet = new Set();

  /** ID de la amenidad cuya descripcion esta siendo editada inline. @type {number|null} */
  let amenidadEditandoId = null;

  /** Texto temporal mientras se edita la descripcion de una amenidad. @type {string} */
  let editDescAmenidad = '';

  /** Controla la visibilidad del formulario para crear una nueva categoria de amenidad. @type {boolean} */
  let showFormNuevaAmenidadCatalogo = false;

  /** Nombre de la nueva categoria de amenidad a crear en el catalogo global. @type {string} */
  let nuevaAmenidadCatalogoNombre = '';

  /** Indica si se esta creando una nueva categoria de amenidad. @type {boolean} */
  let creandoAmenidadCatalogo = false;

  /** Mensaje de retroalimentacion al crear una categoria de amenidad. @type {{tipo: string, texto: string}|null} */
  let mensajeNuevaAmenidadCatalogo = null;

  /** Habitaciones individuales creadas en el paso 3. @type {Array<Object>} */
  let habitacionesNuevas = [];

  /** Controla la visibilidad del formulario de nueva habitacion. @type {boolean} */
  let showFormHabNueva = false;

  /** Datos del formulario de nueva habitacion. @type {Object} */
  let nuevaHabitacion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1, mismasImagenes: true };

  /** Indica si se esta procesando la creacion de habitaciones. @type {boolean} */
  let guardandoNuevaHab = false;

  /** Mensaje de retroalimentacion al crear habitaciones. @type {{tipo: string, texto: string}|null} */
  let mensajeNuevaHab = null;

  /**
   * Mapa de imagenes por habitacion: clave = ID de habitacion, valor = array de {id, preview}.
   * @type {Object<number, Array<{id: number, preview: string}>>}
   */
  let imagenesHabNueva = {};

  /** Set con los IDs de habitaciones individuales cuya imagen se esta subiendo. @type {Set<number>} */
  let subiendoImgHabNuevaSet = new Set();

  /** ID de la habitacion individual que esta siendo editada. @type {number|null} */
  let editandoHabId = null;

  /** Datos temporales mientras se edita una habitacion individual. @type {Object} */
  let editHab = {};

  /**
   * Grupos de habitaciones creadas en lote (mismas imagenes para todas).
   * Cada grupo contiene los IDs de todas las habitaciones del lote.
   * @type {Array<{ids: number[], tipoHabitacion: string, cantidad: number, tipoHabitacionId: number, descripcion: string}>}
   */
  let gruposHab = [];

  /** Indica si hay una creacion masiva de habitaciones en progreso. @type {boolean} */
  let creandoMasivo = false;

  /** Mensaje de progreso durante la creacion masiva. @type {string} */
  let creandoMasivoProgreso = '';

  /** Indica si hay una eliminacion masiva de habitaciones en progreso. @type {boolean} */
  let eliminandoMasivo = false;

  /** Mensaje de progreso durante la eliminacion masiva. @type {string} */
  let eliminandoMasivoProgreso = '';

  onMount(() => { cargarPaisesWizard(); cargarAmenidadesCatalogo(); });

  /**
   * Carga la lista de paises desde la API externa. Solo carga una vez si ya estan en memoria.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarPaisesWizard() {
    if (todosLosPaisesWizard.length) return;
    try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); todosLosPaisesWizard = d.data ?? []; } catch {}
  }

  /**
   * Carga el catalogo global de amenidades desde el backend.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarAmenidadesCatalogo() {
    try { const r = await fetch(`${API_BASE}/admin/amenidades`, { credentials: 'include' }); if (r.ok) amenidades = await r.json(); } catch {}
  }

  /** Actualiza las sugerencias de paises conforme el usuario escribe. */
  function onWizardPaisInput() { wizardPaisSeleccionado = null; wizardPaisError = ''; nuevoHotel.ciudadNombre = ''; wizardCiudadesSugeridas = []; const q = wizardPaisQuery.toLowerCase(); wizardPaisesSugeridos = q.length < 2 ? [] : todosLosPaisesWizard.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6); }

  /**
   * Selecciona un pais del dropdown y limpia el campo de ciudad.
   * @param {Object} p - Pais seleccionado.
   */
  function seleccionarWizardPais(p) { wizardPaisSeleccionado = p; wizardPaisQuery = p.country; wizardPaisesSugeridos = []; wizardPaisError = ''; nuevoHotel.ciudadNombre = ''; wizardCiudadesSugeridas = []; }

  /** Valida que el usuario haya seleccionado un pais del dropdown y no solo escrito texto libre. */
  function validarWizardPais() { if (wizardPaisQuery && !wizardPaisSeleccionado) { wizardPaisError = 'Selecciona un país de la lista.'; wizardPaisQuery = ''; } }

  /** Actualiza las sugerencias de ciudades segun el texto ingresado en el campo de ciudad. */
  function onWizardCiudadInput() { wizardCiudadError = ''; if (!wizardPaisSeleccionado) return; const q = nuevoHotel.ciudadNombre.toLowerCase(); wizardCiudadesSugeridas = q.length < 2 ? [] : (wizardPaisSeleccionado.cities ?? []).filter(c => c.toLowerCase().includes(q)).slice(0, 6); }

  /**
   * Selecciona una ciudad del dropdown.
   * @param {string} c - Nombre de la ciudad.
   */
  function seleccionarWizardCiudad(c) { nuevoHotel.ciudadNombre = c; wizardCiudadesSugeridas = []; wizardCiudadError = ''; }

  /**
   * Valida el formulario del paso 1 y crea el hotel en el backend.
   * Si tiene exito, guarda el ID y avanza al paso 2.
   * @async
   * @returns {Promise<void>}
   */
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
    } catch (e) { mensajeNuevoHotel = { tipo: 'error', texto: e.message }; }
    finally { guardandoNuevoHotel = false; }
  }

  /**
   * Sube una imagen al hotel creado en el paso 1.
   * @async
   * @param {Event} ev - Evento del input file.
   * @returns {Promise<void>}
   */
  async function subirImagenNuevoHotel(ev) { const f = ev.target.files[0]; if (!f || !hotelCreadoId) return; if (f.size > 7 * 1024 * 1024) { toast('La imagen excede 7 MB. Usa una imagen más pequeña.', 'error'); ev.target.value = ''; return; } subiendoImgNuevoHotel = true; try { const b = await fileToBase64(f); const r = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); if (!r.ok) { const ct = r.headers.get('content-type') || ''; let msg; if (ct.includes('application/json')) { const err = await r.json().catch(() => ({})); msg = err.mensaje || `Error ${r.status}`; } else { msg = r.status === 413 ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).' : (await r.text().catch(() => '') || `Error ${r.status}`); } throw new Error(msg); } const d = await r.json(); imagenesNuevoHotel = [...imagenesNuevoHotel, { id: d.id, preview: b }]; toast('Imagen subida'); } catch (e) { toast(e.message, 'error'); } finally { subiendoImgNuevoHotel = false; ev.target.value = ''; } }

  /**
   * Solicita confirmacion antes de eliminar una imagen del hotel.
   * @param {number} id - ID de la imagen a eliminar.
   */
  function pedirEliminarImgHotel(id) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen del hotel?', () => _eliminarImgHotel(id)); }

  /**
   * Elimina una imagen del hotel en el backend y la quita de la lista local.
   * @async
   * @param {number} id - ID de la imagen.
   * @returns {Promise<void>}
   */
  async function _eliminarImgHotel(id) { try { await fetch(`${API_BASE}/admin/hoteles/imagenes/${id}`, { method: 'DELETE', credentials: 'include' }); imagenesNuevoHotel = imagenesNuevoHotel.filter(i => i.id !== id); toast('Imagen eliminada'); } catch (e) { toast(e.message, 'error'); } }

  /**
   * Crea una nueva categoria de amenidad en el catalogo global del sistema.
   * @async
   * @returns {Promise<void>}
   */
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

  /**
   * Asigna una amenidad del catalogo al hotel recien creado.
   * @async
   * @returns {Promise<void>}
   */
  async function agregarAmenidad() { if (!hotelCreadoId) return; try { const r = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/amenidades`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: Number(nuevaAmenidad.amenidadId), descripcion: nuevaAmenidad.descripcion }) }); const d = await r.json(); if (!r.ok) throw new Error(d.mensaje || 'Error'); const cat = amenidades.find(a => a.id === Number(nuevaAmenidad.amenidadId)); amenidadesNuevoHotel = [...amenidadesNuevoHotel, { id: d.id, amenidadId: Number(nuevaAmenidad.amenidadId), amenidadNombre: cat?.nombre ?? '', descripcion: nuevaAmenidad.descripcion, imagenesIds: [] }]; showFormAmenidad = false; nuevaAmenidad = { amenidadId: 1, descripcion: '' }; toast('Amenidad agregada'); } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; } }

  /**
   * Guarda la descripcion editada de una amenidad del hotel.
   * @async
   * @param {Object} ha - Objeto de la amenidad asignada al hotel.
   * @returns {Promise<void>}
   */
  async function guardarDescAmenidad(ha) { try { const r = await fetch(`${API_BASE}/admin/hoteles/amenidades/${ha.id}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: ha.amenidadId, descripcion: editDescAmenidad }) }); if (!r.ok) throw new Error('Error'); amenidadesNuevoHotel = amenidadesNuevoHotel.map(a => a.id === ha.id ? { ...a, descripcion: editDescAmenidad } : a); amenidadEditandoId = null; toast('Descripción actualizada'); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Solicita confirmacion antes de eliminar una amenidad del hotel.
   * @param {number} id - ID de la amenidad asignada.
   * @param {string} nom - Nombre de la amenidad para el mensaje de confirmacion.
   */
  function pedirEliminarAmenidad(id, nom) { pedirConfirmacion('Eliminar amenidad', `¿Eliminar "${nom}" y sus imágenes?`, () => _eliminarAmenidad(id)); }

  /**
   * Elimina una amenidad del hotel en el backend.
   * @async
   * @param {number} id - ID de la amenidad asignada al hotel.
   * @returns {Promise<void>}
   */
  async function _eliminarAmenidad(id) { try { await fetch(`${API_BASE}/admin/hoteles/amenidades/${id}`, { method: 'DELETE', credentials: 'include' }); amenidadesNuevoHotel = amenidadesNuevoHotel.filter(a => a.id !== id); toast('Amenidad eliminada'); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Sube una imagen a una amenidad especifica del hotel.
   * @async
   * @param {Event} ev - Evento del input file.
   * @param {number} haId - ID de la amenidad destino.
   * @returns {Promise<void>}
   */
  async function subirImgAmenidad(ev, haId) { const f = ev.target.files[0]; if (!f) return; if (f.size > 7 * 1024 * 1024) { toast('La imagen excede 7 MB. Usa una imagen más pequeña.', 'error'); ev.target.value = ''; return; } subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet, haId]); try { const b = await fileToBase64(f); const r = await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); if (!r.ok) { const ct = r.headers.get('content-type') || ''; let msg; if (ct.includes('application/json')) { const err = await r.json().catch(() => ({})); msg = err.mensaje || `Error ${r.status}`; } else { msg = r.status === 413 ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).' : (await r.text().catch(() => '') || `Error ${r.status}`); } throw new Error(msg); } const d = await r.json(); amenidadesNuevoHotel = amenidadesNuevoHotel.map(a => a.id === haId ? { ...a, imagenesIds: [...(a.imagenesIds ?? []), d.id] } : a); } catch(e) { toast('Error: ' + e.message, 'error'); } finally { subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet].filter(i => i !== haId)); ev.target.value = ''; } }

  /**
   * Solicita confirmacion antes de eliminar una imagen de una amenidad.
   * @param {number} haId - ID de la amenidad.
   * @param {number} imgId - ID de la imagen.
   */
  function pedirEliminarImgAmenidad(haId, imgId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen?', () => _eliminarImgAmenidad(haId, imgId)); }

  /**
   * Elimina una imagen de una amenidad del hotel.
   * @async
   * @param {number} haId - ID de la amenidad.
   * @param {number} imgId - ID de la imagen a eliminar.
   * @returns {Promise<void>}
   */
  async function _eliminarImgAmenidad(haId, imgId) { try { await fetch(`${API_BASE}/admin/hoteles/amenidades/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' }); amenidadesNuevoHotel = amenidadesNuevoHotel.map(a => a.id !== haId ? a : { ...a, imagenesIds: (a.imagenesIds ?? []).filter(i => i !== imgId) }); toast('Imagen eliminada'); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Crea una o varias habitaciones en el hotel del paso 3.
   * Si la cantidad es mayor a 1 y se eligio "mismas imagenes", las agrupa para compartir fotos.
   * El numero de habitacion es asignado automaticamente por el backend (count + 1).
   * @async
   * @returns {Promise<void>}
   */
  async function crearHabitacion() {
    if (!hotelCreadoId) return;
    const cant = Math.max(1, Math.min(50, Number(nuevaHabitacion.cantidad) || 1));
    const usarMismas = cant > 1 && nuevaHabitacion.mismasImagenes;
    guardandoNuevaHab = true; mensajeNuevaHab = null;
    if (cant > 1) { creandoMasivo = true; creandoMasivoProgreso = `Creando habitación 0 de ${cant}...`; }
    try {
      // Payload sin numeroHabitacion — el backend lo genera automaticamente.
      // estadoId siempre 1 (Activa): las habitaciones nuevas nacen activas.
      const payload = {
        tipoHabitacionId: Number(nuevaHabitacion.tipoHabitacionId),
        descripcion:      nuevaHabitacion.descripcion,
        estadoId:         1,
      };
      const tn = tiposHabitacion.find(t => t.id === payload.tipoHabitacionId)?.nombre ?? '';
      let creadas = 0; let idsLote = [];
      for (let i = 0; i < cant; i++) {
        if (cant > 1) creandoMasivoProgreso = `Creando habitación ${i + 1} de ${cant}...`;
        const r = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/habitaciones`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        const d = await r.json(); if (!r.ok) throw new Error(d.mensaje || 'Error');
        const newH = { ...payload, id: d.id, tipoHabitacion: tn, imagenesIds: [] };
        if (!usarMismas) { habitacionesNuevas = [...habitacionesNuevas, newH]; }
        imagenesHabNueva = { ...imagenesHabNueva, [d.id]: [] }; idsLote.push(d.id); creadas++;
      }
      if (usarMismas) { gruposHab = [...gruposHab, { ids: idsLote, tipoHabitacion: tn, cantidad: cant, tipoHabitacionId: payload.tipoHabitacionId, descripcion: payload.descripcion ?? '' }]; }
      toast(`${creadas} habitación(es) ${tn} creada(s)`); showFormHabNueva = false;
      nuevaHabitacion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1, mismasImagenes: true };
    } catch (e) { mensajeNuevaHab = { tipo: 'error', texto: e.message }; }
    finally { guardandoNuevaHab = false; creandoMasivo = false; }
  }

  /** Set con los indices de grupo cuya imagen se esta subiendo actualmente. @type {Set<number>} */
  let subiendoImgGrupoSet = new Set();

  /**
   * Sube una imagen a todas las habitaciones de un grupo (lote con mismas imagenes).
   * @async
   * @param {Event} ev - Evento del input file.
   * @param {number} gi - Indice del grupo en el array gruposHab.
   * @returns {Promise<void>}
   */
  async function subirImgGrupo(ev, gi) { const f = ev.target.files[0]; if (!f) return; const g = gruposHab[gi]; if (!g) return; if (f.size > 7 * 1024 * 1024) { toast('La imagen excede 7 MB. Usa una imagen más pequeña.', 'error'); ev.target.value = ''; return; } subiendoImgGrupoSet = new Set([...subiendoImgGrupoSet, gi]); try { const b = await fileToBase64(f); for (const hId of g.ids) { const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); if (!r.ok) { const ct = r.headers.get('content-type') || ''; let msg; if (ct.includes('application/json')) { const err = await r.json().catch(() => ({})); msg = err.mensaje || `Error ${r.status}`; } else { msg = r.status === 413 ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).' : (await r.text().catch(() => '') || `Error ${r.status}`); } throw new Error(msg); } const d = await r.json(); imagenesHabNueva = { ...imagenesHabNueva, [hId]: [...(imagenesHabNueva[hId] ?? []), { id: d.id, preview: b }] }; } toast(`Imagen agregada a ${g.ids.length} habitaciones`); } catch(e) { toast('Error: ' + e.message, 'error'); } finally { subiendoImgGrupoSet = new Set([...subiendoImgGrupoSet].filter(i => i !== gi)); ev.target.value = ''; } }

  /**
   * Solicita confirmacion antes de eliminar una imagen de todas las habitaciones de un grupo.
   * @param {number} gi - Indice del grupo.
   * @param {number} imgIdx - Posicion de la imagen dentro del array de imagenes del grupo.
   */
  function pedirEliminarImgGrupo(gi, imgIdx) { pedirConfirmacion('Eliminar imagen', `¿Eliminar de las ${gruposHab[gi]?.ids.length} habitaciones?`, () => _eliminarImgGrupo(gi, imgIdx)); }

  /**
   * Elimina una imagen de todas las habitaciones de un grupo sincronizando el indice.
   * @async
   * @param {number} gi - Indice del grupo.
   * @param {number} imgIdx - Indice de la imagen a eliminar.
   * @returns {Promise<void>}
   */
  async function _eliminarImgGrupo(gi, imgIdx) { const g = gruposHab[gi]; if (!g) return; const firstId = g.ids[0]; const imgs = imagenesHabNueva[firstId] ?? []; const img = imgs[imgIdx]; if (!img) return; try { for (const hId of g.ids) { const habImgs = imagenesHabNueva[hId] ?? []; const match = habImgs[imgIdx]; if (match) { await fetch(`${API_BASE}/admin/habitaciones/imagenes/${match.id}`, { method: 'DELETE', credentials: 'include' }); imagenesHabNueva = { ...imagenesHabNueva, [hId]: habImgs.filter(i => i.id !== match.id) }; } } toast('Imagen eliminada de todas las habitaciones'); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Solicita confirmacion antes de eliminar todas las habitaciones de un grupo.
   * @param {number} gi - Indice del grupo a eliminar.
   */
  function pedirEliminarGrupo(gi) { const g = gruposHab[gi]; pedirConfirmacion('Eliminar grupo', `¿Eliminar las ${g.cantidad} habitaciones?`, () => _eliminarGrupo(gi)); }

  /**
   * Elimina todas las habitaciones de un grupo en el backend de forma secuencial.
   * @async
   * @param {number} gi - Indice del grupo.
   * @returns {Promise<void>}
   */
  async function _eliminarGrupo(gi) { const g = gruposHab[gi]; if (!g) return; eliminandoMasivo = true; eliminandoMasivoProgreso = `Eliminando ${g.cantidad} habitaciones...`; try { for (let i = 0; i < g.ids.length; i++) { eliminandoMasivoProgreso = `Eliminando ${i+1} de ${g.ids.length}...`; await fetch(`${API_BASE}/admin/habitaciones/${g.ids[i]}`, { method: 'DELETE', credentials: 'include' }); delete imagenesHabNueva[g.ids[i]]; } imagenesHabNueva = { ...imagenesHabNueva }; gruposHab = gruposHab.filter((_, i) => i !== gi); toast(`${g.cantidad} habitaciones eliminadas`); } catch(e) { toast(e.message, 'error'); } finally { eliminandoMasivo = false; } }

  /**
   * Sube una imagen a una habitacion individual.
   * @async
   * @param {Event} ev - Evento del input file.
   * @param {number} hId - ID de la habitacion.
   * @returns {Promise<void>}
   */
  async function subirImgHab(ev, hId) { const f = ev.target.files[0]; if (!f) return; if (f.size > 7 * 1024 * 1024) { toast('La imagen excede 7 MB. Usa una imagen más pequeña.', 'error'); ev.target.value = ''; return; } subiendoImgHabNuevaSet = new Set([...subiendoImgHabNuevaSet, hId]); try { const b = await fileToBase64(f); const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64: b }) }); if (!r.ok) { const ct = r.headers.get('content-type') || ''; let msg; if (ct.includes('application/json')) { const err = await r.json().catch(() => ({})); msg = err.mensaje || `Error ${r.status}`; } else { msg = r.status === 413 ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).' : (await r.text().catch(() => '') || `Error ${r.status}`); } throw new Error(msg); } const d = await r.json(); imagenesHabNueva = { ...imagenesHabNueva, [hId]: [...(imagenesHabNueva[hId] ?? []), { id: d.id, preview: b }] }; } catch (e) { if (e.message) toast(e.message, 'error'); } finally { subiendoImgHabNuevaSet = new Set([...subiendoImgHabNuevaSet].filter(i => i !== hId)); ev.target.value = ''; } }

  /**
   * Solicita confirmacion antes de eliminar una imagen de una habitacion individual.
   * @param {number} hId - ID de la habitacion.
   * @param {number} imgId - ID de la imagen.
   */
  function pedirEliminarImgHab(hId, imgId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen?', () => _eliminarImgHab(hId, imgId)); }

  /**
   * Elimina una imagen de una habitacion individual.
   * @async
   * @param {number} hId - ID de la habitacion.
   * @param {number} imgId - ID de la imagen.
   * @returns {Promise<void>}
   */
  async function _eliminarImgHab(hId, imgId) { try { await fetch(`${API_BASE}/admin/habitaciones/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' }); imagenesHabNueva = { ...imagenesHabNueva, [hId]: (imagenesHabNueva[hId] ?? []).filter(i => i.id !== imgId) }; toast('Imagen eliminada'); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Abre el formulario de edicion inline para una habitacion individual.
   * @param {Object} h - Habitacion a editar.
   */
  function abrirEditHab(h) { editandoHabId = h.id; editHab = { tipoHabitacionId: h.tipoHabitacionId, descripcion: h.descripcion ?? '', estadoId: h.estadoId }; }

  /**
   * Guarda los cambios de una habitacion individual mediante PATCH.
   * @async
   * @param {number} hId - ID de la habitacion.
   * @returns {Promise<void>}
   */
  async function guardarEditHab(hId) { try { const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ tipoHabitacionId: Number(editHab.tipoHabitacionId), descripcion: editHab.descripcion, estadoId: Number(editHab.estadoId) }) }); if (!r.ok) throw new Error('Error'); const tn = tiposHabitacion.find(t => t.id === Number(editHab.tipoHabitacionId))?.nombre ?? ''; habitacionesNuevas = habitacionesNuevas.map(h => h.id === hId ? { ...h, ...editHab, tipoHabitacion: tn } : h); editandoHabId = null; toast('Habitación actualizada'); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Solicita confirmacion antes de eliminar una habitacion individual.
   * @param {number} hId - ID de la habitacion.
   * @param {string} tipo - Nombre del tipo de habitacion para el mensaje.
   */
  function pedirEliminarHab(hId, tipo) { pedirConfirmacion('Eliminar habitación', `¿Eliminar "${tipo}" (ID #${hId})?`, () => _eliminarHab(hId)); }

  /**
   * Elimina una habitacion individual del backend y la quita de la lista local.
   * @async
   * @param {number} hId - ID de la habitacion.
   * @returns {Promise<void>}
   */
  async function _eliminarHab(hId) { try { await fetch(`${API_BASE}/admin/habitaciones/${hId}`, { method: 'DELETE', credentials: 'include' }); habitacionesNuevas = habitacionesNuevas.filter(h => h.id !== hId); delete imagenesHabNueva[hId]; imagenesHabNueva = { ...imagenesHabNueva }; toast('Habitación eliminada'); } catch(e) { toast(e.message, 'error'); } }

  /** Indice del grupo siendo editado actualmente. @type {number|null} */
  let editandoGrupoIdx = null;

  /** Datos temporales mientras se edita un grupo de habitaciones. @type {Object} */
  let editGrupo = {};

  /**
   * Abre el formulario de edicion para un grupo de habitaciones.
   * @param {number} gi - Indice del grupo.
   */
  function abrirEditGrupo(gi) { const g = gruposHab[gi]; if (!g) return; editandoGrupoIdx = gi; editGrupo = { tipoHabitacionId: g.tipoHabitacionId ?? 1, descripcion: g.descripcion ?? '' }; }

  /**
   * Guarda los cambios del grupo aplicandolos a todas sus habitaciones en el backend.
   * @async
   * @param {number} gi - Indice del grupo.
   * @returns {Promise<void>}
   */
  async function guardarEditGrupo(gi) { const g = gruposHab[gi]; if (!g) return; const payload = { tipoHabitacionId: Number(editGrupo.tipoHabitacionId), descripcion: editGrupo.descripcion }; try { for (const hId of g.ids) { const r = await fetch(`${API_BASE}/admin/habitaciones/${hId}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) }); if (!r.ok) throw new Error('Error al actualizar #' + hId); } const tn = tiposHabitacion.find(t => t.id === payload.tipoHabitacionId)?.nombre ?? ''; gruposHab = gruposHab.map((gr, i) => i === gi ? { ...gr, ...payload, tipoHabitacion: tn } : gr); editandoGrupoIdx = null; toast(`${g.ids.length} habitaciones actualizadas`); } catch(e) { toast(e.message, 'error'); } }

  /**
   * Reinicia completamente el wizard a su estado inicial para crear otro hotel.
   */
  function resetCrearHotel() {
    pasoActual = 'info'; hotelCreadoId = null; hotelCreadoNombre = '';
    nuevoHotel = { nombre: '', direccion: '', descripcion: '', rating: 3.0, estadoId: 1, ciudadNombre: '' };
    mensajeNuevoHotel = null; imagenesNuevoHotel = []; amenidadesNuevoHotel = []; habitacionesNuevas = []; gruposHab = [];
    showFormHabNueva = false; showFormAmenidad = false; showFormNuevaAmenidadCatalogo = false;
    nuevaHabitacion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1, mismasImagenes: true };
    nuevaAmenidad = { amenidadId: 1, descripcion: '' }; nuevaAmenidadCatalogoNombre = '';
    mensajeNuevaHab = null; mensajeAmenidad = null; mensajeNuevaAmenidadCatalogo = null;
    imagenesHabNueva = {}; subiendoImgHabNuevaSet = new Set(); subiendoImgAmenidadSet = new Set(); subiendoImgGrupoSet = new Set();
    wizardPaisQuery = ''; wizardPaisSeleccionado = null; wizardPaisError = ''; wizardPaisesSugeridos = []; wizardCiudadesSugeridas = []; wizardCiudadError = '';
    amenidadEditandoId = null; editandoHabId = null; editandoGrupoIdx = null;
  }
</script>

<!-- Overlay bloqueante durante creacion masiva de habitaciones -->
{#if creandoMasivo}<div class="adm__overlay" style="z-index:3500"></div><div class="adm__confirm" style="z-index:3501;text-align:center"><div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem"><svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#7b93ff" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p class="adm__confirm__title" style="font-size:1rem">Creando habitaciones</p></div><div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem"><p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{creandoMasivoProgreso}</p><p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera. No cierres esta página.</p></div></div>{/if}

<!-- Overlay bloqueante durante eliminacion masiva de habitaciones -->
{#if eliminandoMasivo}<div class="adm__overlay" style="z-index:3500"></div><div class="adm__confirm" style="z-index:3501;text-align:center"><div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem"><svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f85149" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p class="adm__confirm__title" style="font-size:1rem">Eliminando</p></div><div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem"><p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{eliminandoMasivoProgreso}</p></div></div>{/if}

<!-- Notificaciones toast -->
{#if toasts.length > 0}<div class="adm__toast-container">{#each toasts as t (t.id)}<div class="adm__toast adm__toast--{t.tipo}">{#if t.tipo === 'ok'}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/></svg>{/if}{t.texto}<button class="adm__toast__close" on:click={() => toasts = toasts.filter(x => x.id !== t.id)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>{/if}

<!-- Indicador de pasos del wizard con estados activo, completado y deshabilitado -->
<div class="adm__wizard-steps">
  <div class="adm__wizard-step" class:adm__wizard-step--done={hotelCreadoId && pasoActual !== 'info'} class:adm__wizard-step--active={pasoActual === 'info'}><div class="adm__wizard-step-num">1</div><span>Información</span></div>
  <div class="adm__wizard-connector"></div>
  <div class="adm__wizard-step" class:adm__wizard-step--done={pasoActual === 'habitaciones'} class:adm__wizard-step--active={pasoActual === 'contenido'} class:adm__wizard-step--disabled={!hotelCreadoId}><div class="adm__wizard-step-num">2</div><span>Imágenes y Amenidades</span></div>
  <div class="adm__wizard-connector"></div>
  <div class="adm__wizard-step" class:adm__wizard-step--active={pasoActual === 'habitaciones'} class:adm__wizard-step--disabled={!hotelCreadoId}><div class="adm__wizard-step-num">3</div><span>Habitaciones</span></div>
</div>

<!-- PASO 1: Datos basicos del hotel (nombre, pais, ciudad, rating, estado, descripcion) -->
{#if pasoActual === 'info'}
<div class="adm__wizard-card">
  <h3 class="adm__wizard-card-title">Datos del nuevo hotel</h3>
  <div class="adm__form-grid adm__form-grid--wizard">
    <div class="adm__field adm__field--full"><label>Nombre del Hotel *</label><input type="text" bind:value={nuevoHotel.nombre} placeholder="Ej: Miku Inn Paris" /></div>
    <div class="adm__field"><label>País *</label><div class="adm__autocomplete-wrap"><input type="text" bind:value={wizardPaisQuery} on:input={onWizardPaisInput} on:blur={() => setTimeout(validarWizardPais, 150)} placeholder="Escribe el país..." autocomplete="off" />{#if wizardPaisesSugeridos.length > 0}<ul class="adm__autocomplete-list">{#each wizardPaisesSugeridos as p}<li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarWizardPais(p)}>{p.country}</button></li>{/each}</ul>{/if}</div>{#if wizardPaisError}<span class="adm__field-error">{wizardPaisError}</span>{/if}</div>
    <div class="adm__field"><label>Ciudad *</label><div class="adm__autocomplete-wrap"><input type="text" bind:value={nuevoHotel.ciudadNombre} on:input={onWizardCiudadInput} on:blur={() => setTimeout(() => wizardCiudadesSugeridas = [], 150)} placeholder={wizardPaisSeleccionado ? "Escribe la ciudad..." : "Primero selecciona un país"} disabled={!wizardPaisSeleccionado} autocomplete="off" />{#if wizardCiudadesSugeridas.length > 0}<ul class="adm__autocomplete-list">{#each wizardCiudadesSugeridas as c}<li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarWizardCiudad(c)}>{c}</button></li>{/each}</ul>{/if}</div></div>
    <div class="adm__field adm__field--full"><label>Dirección</label><input type="text" bind:value={nuevoHotel.direccion} placeholder="Calle, número, colonia..." /></div>
    <div class="adm__field"><label>Rating (0–5)</label><input type="number" bind:value={nuevoHotel.rating} min="0" max="5" step="0.1" /></div>
    <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevoHotel.descripcion} rows="4" placeholder="Describe el hotel..."></textarea></div>
  </div>
  {#if mensajeNuevoHotel}<div class="adm__feedback adm__feedback--{mensajeNuevoHotel.tipo}" style="margin-top:1rem">{mensajeNuevoHotel.texto}</div>{/if}
  <div class="adm__wizard-actions">
    <button class="adm__btn adm__btn--ghost" on:click={resetCrearHotel}>Limpiar</button>
    <button class="adm__btn adm__btn--primary adm__btn--lg" on:click={crearNuevoHotel} disabled={guardandoNuevoHotel}>{#if guardandoNuevoHotel}Creando...{:else}Crear Hotel y Continuar →{/if}</button>
  </div>
</div>

<!-- PASO 2: Imagenes del hotel y amenidades con sus imagenes -->
{:else if pasoActual === 'contenido'}
<div class="adm__wizard-card">
  <h3 class="adm__wizard-card-title">Imágenes y Amenidades — <strong>{hotelCreadoNombre}</strong></h3>

  <!-- Seccion de imagenes del hotel -->
  <p class="adm__modal-section-title">Imágenes del hotel</p>
  <div class="adm__img-section-header" style="margin-bottom:.75rem">
    <span style="font-size:.8rem;color:var(--adm-text-muted)">{imagenesNuevoHotel.length} imagen(es)</span>
    <label class="adm__btn adm__btn--ghost adm__upload-btn">{#if subiendoImgNuevoHotel}Subiendo...{:else}+ Agregar imagen{/if}<input type="file" accept="image/*" on:change={subirImagenNuevoHotel} disabled={subiendoImgNuevoHotel} style="display:none" /></label>
  </div>
  {#if imagenesNuevoHotel.length > 0}<div class="adm__img-grid" style="margin-bottom:1.5rem">{#each imagenesNuevoHotel as img (img.id)}<div class="adm__img-card"><img src={img.preview} alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHotel(img.id)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>{:else}<div class="adm__img-empty" style="padding:1rem 0;margin-bottom:1.5rem"><p>Sin imágenes aún.</p></div>{/if}

  <div class="adm__modal-section-divider"></div>

  <!-- Seccion de amenidades: asignar del catalogo o crear una nueva categoria -->
  <div style="display:flex;justify-content:space-between;align-items:center;margin:.75rem 0;flex-wrap:wrap;gap:.5rem">
    <p class="adm__modal-section-title" style="margin:0">Amenidades</p>
    <div style="display:flex;gap:.6rem">
      <button class="adm__btn adm__btn--ghost" on:click={() => { showFormNuevaAmenidadCatalogo = !showFormNuevaAmenidadCatalogo; mensajeNuevaAmenidadCatalogo = null; nuevaAmenidadCatalogoNombre = ''; }}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/><line x1="12" y1="9" x2="12" y2="15"/><line x1="9" y1="12" x2="15" y2="12"/></svg>
        Nueva categoría
      </button>
      <button class="adm__btn adm__btn--ghost" on:click={() => { showFormAmenidad = !showFormAmenidad; mensajeAmenidad = null; }}>+ Asignar</button>
    </div>
  </div>

  <!-- Formulario para crear una nueva categoria de amenidad en el catalogo global -->
  {#if showFormNuevaAmenidadCatalogo}
    <div class="adm__wizard-subcard" style="margin-bottom:1rem;border-color:rgba(123,147,255,.35)">
      <p class="adm__modal-section-title" style="color:#a5b4fc">Nueva categoría (catálogo global)</p>
      <div class="adm__form-grid adm__form-grid--wizard">
        <div class="adm__field adm__field--full"><label>Nombre</label><input type="text" bind:value={nuevaAmenidadCatalogoNombre} placeholder="Ej: Spa, Terraza..." /></div>
      </div>
      {#if mensajeNuevaAmenidadCatalogo}<div class="adm__feedback adm__feedback--{mensajeNuevaAmenidadCatalogo.tipo}" style="margin:.75rem 0">{mensajeNuevaAmenidadCatalogo.texto}</div>{/if}
      <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem">
        <button class="adm__btn adm__btn--ghost" on:click={() => showFormNuevaAmenidadCatalogo = false}>Cancelar</button>
        <button class="adm__btn adm__btn--primary" on:click={crearAmenidadCatalogo} disabled={creandoAmenidadCatalogo}>{#if creandoAmenidadCatalogo}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Creando...{:else}Crear en catálogo{/if}</button>
      </div>
    </div>
  {/if}

  <!-- Formulario para asignar una amenidad existente al hotel -->
  {#if showFormAmenidad}
    <div class="adm__wizard-subcard" style="margin-bottom:1rem">
      <div class="adm__form-grid adm__form-grid--wizard">
        <div class="adm__field"><label>Tipo</label><select bind:value={nuevaAmenidad.amenidadId}>{#each amenidades.filter(a => !amenidadesNuevoHotel.some(h => h.amenidadId === a.id)) as a}<option value={a.id}>{a.nombre}</option>{/each}</select></div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaAmenidad.descripcion} rows="2" placeholder="Describe..."></textarea></div>
      </div>
      <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem">
        <button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = false}>Cancelar</button>
        <button class="adm__btn adm__btn--primary" on:click={agregarAmenidad}>Asignar</button>
      </div>
    </div>
  {/if}

  {#if mensajeAmenidad}<div class="adm__feedback adm__feedback--{mensajeAmenidad.tipo}" style="margin-bottom:.75rem">{mensajeAmenidad.texto}</div>{/if}

  <!-- Lista de amenidades asignadas con edicion inline de descripcion e imagenes -->
  {#if amenidadesNuevoHotel.length > 0}
    <div style="display:flex;flex-direction:column;gap:.75rem;margin-bottom:1rem">
      {#each amenidadesNuevoHotel as ha (ha.id)}
        <div class="adm__amenidad-card"><div class="adm__amenidad-header"><div class="adm__amenidad-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg></div><div style="flex:1;min-width:0"><p class="adm__amenidad-nombre">{ha.amenidadNombre}</p>{#if amenidadEditandoId === ha.id}<textarea class="adm__amenidad-desc-input" bind:value={editDescAmenidad} rows="2"></textarea><div style="display:flex;gap:.5rem;margin-top:.5rem"><button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarDescAmenidad(ha)}>Guardar</button><button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => amenidadEditandoId = null}>Cancelar</button></div>{:else}<p class="adm__amenidad-desc">{ha.descripcion || '—'}</p>{/if}</div><div style="display:flex;gap:.4rem;flex-shrink:0">{#if amenidadEditandoId !== ha.id}<button class="adm__icon-btn adm__icon-btn--edit" on:click={() => { amenidadEditandoId = ha.id; editDescAmenidad = ha.descripcion; }}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>{/if}<button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarAmenidad(ha.id, ha.amenidadNombre)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button></div></div>
        <div class="adm__amenidad-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (ha.imagenesIds ?? []) as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/amenidad/{imgId}" alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgAmenidad(ha.id, imgId)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgAmenidadSet.has(ha.id)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImgAmenidad(e, ha.id)} disabled={subiendoImgAmenidadSet.has(ha.id)} style="display:none" /></label></div></div></div>
      {/each}
    </div>
  {:else if !showFormAmenidad && !showFormNuevaAmenidadCatalogo}
    <div class="adm__img-empty" style="padding:1rem 0"><p>Sin amenidades aún.</p></div>
  {/if}

  <div class="adm__wizard-actions">
    <button class="adm__btn adm__btn--ghost" on:click={() => pasoActual = 'info'}>← Volver</button>
    <button class="adm__btn adm__btn--primary adm__btn--lg" on:click={() => pasoActual = 'habitaciones'}>Continuar a Habitaciones →</button>
  </div>
</div>

<!-- PASO 3: Habitaciones individuales o en lote con imagenes por grupo -->
{:else if pasoActual === 'habitaciones'}
<div class="adm__wizard-card">
  <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:1.25rem;flex-wrap:wrap;gap:.75rem">
    <h3 class="adm__wizard-card-title" style="margin:0">Habitaciones — <strong>{hotelCreadoNombre}</strong></h3>
    <button class="adm__btn adm__btn--primary" on:click={() => { showFormHabNueva = true; mensajeNuevaHab = null; }}>+ Agregar Habitación</button>
  </div>

  <!-- Formulario de nueva habitacion: tipo, estado, cantidad y modo de imagenes -->
  {#if showFormHabNueva}
  <div class="adm__wizard-subcard">
    <p class="adm__modal-section-title">Nueva Habitación</p>
    <!-- El numero de habitacion es asignado automaticamente por el backend -->
    <div style="margin-bottom:.75rem;padding:.6rem .9rem;background:rgba(45,212,191,.06);border:1px solid rgba(45,212,191,.2);border-radius:8px;font-size:.78rem;color:#8b95b0">
      El número de habitación se asigna automáticamente según el total existente en el hotel.
    </div>
    <div class="adm__form-grid adm__form-grid--wizard">
      <div class="adm__field">
        <label>Tipo de Habitación</label>
        <select bind:value={nuevaHabitacion.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select>
      </div>
      <div class="adm__field">
        <label>Cantidad</label>
        <input type="number" bind:value={nuevaHabitacion.cantidad} min="1" max="50" />
        {#if nuevaHabitacion.cantidad > 50}<span class="adm__field-error">Máximo 50 por lote</span>{:else if nuevaHabitacion.cantidad > 1}<span style="font-size:.72rem;color:var(--adm-blue);margin-top:2px">Se crearán {nuevaHabitacion.cantidad} idénticas</span>{/if}
      </div>
      <div class="adm__field adm__field--full">
        <label>Descripción</label>
        <textarea bind:value={nuevaHabitacion.descripcion} rows="2" placeholder="Descripción de la habitación..."></textarea>
      </div>
      <!-- Selector de modo de imagenes solo disponible en lotes de mas de 1 habitacion -->
      {#if nuevaHabitacion.cantidad > 1}
        <div class="adm__field adm__field--full">
          <label>Imágenes</label>
          <div style="display:flex;gap:.5rem;margin-top:.35rem">
            <button type="button" class="adm__btn {nuevaHabitacion.mismasImagenes ? 'adm__btn--primary' : 'adm__btn--ghost'} adm__btn--xs" style="flex:1;justify-content:center" on:click={() => nuevaHabitacion.mismasImagenes = true}>Mismas para todas</button>
            <button type="button" class="adm__btn {!nuevaHabitacion.mismasImagenes ? 'adm__btn--primary' : 'adm__btn--ghost'} adm__btn--xs" style="flex:1;justify-content:center" on:click={() => nuevaHabitacion.mismasImagenes = false}>Individuales</button>
          </div>
        </div>
      {/if}
    </div>
    {#if mensajeNuevaHab}<div class="adm__feedback adm__feedback--{mensajeNuevaHab.tipo}" style="margin:.75rem 0">{mensajeNuevaHab.texto}</div>{/if}
    <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:1rem">
      <button class="adm__btn adm__btn--ghost" on:click={() => showFormHabNueva = false}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={crearHabitacion} disabled={guardandoNuevaHab || nuevaHabitacion.cantidad > 50}>{#if guardandoNuevaHab}Creando...{:else}Crear {nuevaHabitacion.cantidad > 1 ? `${Math.min(nuevaHabitacion.cantidad,50)} Habitaciones` : 'Habitación'}{/if}</button>
    </div>
  </div>
  {/if}

  <!-- Grupos de habitaciones creados en lote (mismas imagenes para todas) -->
  {#if gruposHab.length > 0}
  <div style="display:flex;flex-direction:column;gap:1rem;margin-top:1rem">
    {#each gruposHab as g, gi (gi)}
    <div class="adm__wizard-hab-card">
      <div class="adm__wizard-hab-info" style="display:flex;justify-content:space-between;align-items:flex-start">
        <div style="flex:1">
          {#if editandoGrupoIdx === gi}
            <div class="adm__form-grid adm__form-grid--wizard" style="margin-bottom:.75rem">
              <div class="adm__field"><label>Tipo</label><select bind:value={editGrupo.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
              <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editGrupo.descripcion} rows="2"></textarea></div>
            </div>
            <div style="display:flex;gap:.5rem">
              <button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarEditGrupo(gi)}>Guardar ({g.ids.length} hab.)</button>
              <button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => editandoGrupoIdx = null}>Cancelar</button>
            </div>
          {:else}
            <p class="adm__wizard-hab-tipo">{g.tipoHabitacion} <span class="adm__badge badge--blue" style="font-size:.68rem;margin-left:.4rem">x{g.cantidad}</span></p>
            <p style="font-size:.72rem;color:var(--adm-accent);margin:4px 0 0;font-weight:600">IDs: {g.ids.join(', ')}</p>
          {/if}
        </div>
        {#if editandoGrupoIdx !== gi}
          <div style="display:flex;gap:.3rem;flex-shrink:0">
            <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditGrupo(gi)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
            <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarGrupo(gi)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
          </div>
        {/if}
      </div>
      <!-- Imagenes compartidas: se muestra el set del primer ID del grupo -->
      <div class="adm__wizard-hab-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (imagenesHabNueva[g.ids[0]] ?? []) as img, imgIdx (img.id)}<div class="adm__img-card"><img src={img.preview} alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgGrupo(gi, imgIdx)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgGrupoSet.has(gi)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImgGrupo(e, gi)} style="display:none" /></label></div></div>
    </div>
    {/each}
  </div>
  {/if}

  <!-- Habitaciones individuales creadas con imagenes independientes -->
  {#if habitacionesNuevas.length > 0}
  <div style="display:flex;flex-direction:column;gap:1rem;margin-top:1rem">
    {#each habitacionesNuevas as h (h.id)}
    <div class="adm__wizard-hab-card">
      <div class="adm__wizard-hab-info" style="display:flex;justify-content:space-between;align-items:flex-start">
        <div style="flex:1">
          {#if editandoHabId === h.id}
            <div class="adm__form-grid adm__form-grid--wizard" style="margin-bottom:.75rem">
              <div class="adm__field"><label>Tipo</label><select bind:value={editHab.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
              <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editHab.descripcion} rows="2"></textarea></div>
            </div>
            <div style="display:flex;gap:.5rem">
              <button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarEditHab(h.id)}>Guardar</button>
              <button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => editandoHabId = null}>Cancelar</button>
            </div>
          {:else}
            <p class="adm__wizard-hab-tipo">{h.tipoHabitacion} <span style="font-size:.72rem;color:var(--adm-text-muted)">#{h.id}</span></p>
          {/if}
        </div>
        {#if editandoHabId !== h.id}
          <div style="display:flex;gap:.3rem;flex-shrink:0">
            <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditHab(h)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
            <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarHab(h.id, h.tipoHabitacion)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
          </div>
        {/if}
      </div>
      <div class="adm__wizard-hab-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (imagenesHabNueva[h.id] ?? []) as img (img.id)}<div class="adm__img-card"><img src={img.preview} alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHab(h.id, img.id)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgHabNuevaSet.has(h.id)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImgHab(e, h.id)} style="display:none" /></label></div></div>
    </div>
    {/each}
  </div>
  {/if}

  {#if gruposHab.length === 0 && habitacionesNuevas.length === 0 && !showFormHabNueva}
    <div class="adm__img-empty" style="padding:2rem 0"><p>Agrega las habitaciones del hotel.</p></div>
  {/if}

  <div class="adm__wizard-actions" style="margin-top:1.5rem">
    <button class="adm__btn adm__btn--ghost" on:click={() => pasoActual = 'contenido'}>← Volver</button>
    <!-- Finalizar reinicia el wizard y llama el callback del padre para volver al listado -->
    <button class="adm__btn adm__btn--success adm__btn--lg" on:click={() => { resetCrearHotel(); onFinish(); }}>Finalizar — Ver Hoteles</button>
  </div>
</div>
{/if}

<!-- Dialogo de confirmacion personalizado para acciones destructivas -->
{#if confirmDialog}<div class="adm__overlay" on:click={cerrarConfirm} on:keydown={e => e.key === 'Escape' && cerrarConfirm()} role="button" tabindex="-1" aria-label="Cerrar"></div><div class="adm__confirm"><div class="adm__confirm__header"><div class="adm__confirm__icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div><p class="adm__confirm__title">{confirmDialog.titulo}</p></div><div class="adm__confirm__body"><p>{confirmDialog.mensaje}</p></div><div class="adm__confirm__footer"><button class="adm__confirm__btn-cancel" on:click={cerrarConfirm}>Cancelar</button><button class="adm__confirm__btn-ok" on:click={ejecutarConfirm}>Confirmar</button></div></div>{/if}
