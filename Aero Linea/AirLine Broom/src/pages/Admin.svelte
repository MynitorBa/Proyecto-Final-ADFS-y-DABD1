<script>
// @ts-nocheck
  import '../styles/admin.css';
  import { onMount, onDestroy } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;

  const API = 'http://localhost:5190';

  // ========= SESIÓN =========
  let rolNombre = null;
  const unsubscribeSesion = sesion.subscribe(s => {
    rolNombre = s?.rolNombre ?? null;
  });

  let activeSection = 'crear-vuelo';

  // ========= DATOS =========
  let usuarios        = [];
  let aviones         = [];
  let tripulantes     = [];
  let aeropuertos     = [];
  let historialVuelos = [];
  let rolesTripulacion = [];
  let todosLosPaises  = [];

  // ========= LOADING =========
  let loadingUsuarios        = false;
  let loadingAviones         = false;
  let loadingTripulantes     = false;
  let loadingAeropuertos     = false;
  let loadingHistorialVuelos = false;

  // ========= BÚSQUEDA VUELO =========
  let busquedaOrigen        = '';
  let busquedaDestino       = '';
  let busquedaAvion         = '';
  let busquedaTripulante    = '';
  let mostrarDropdownOrigen     = false;
  let mostrarDropdownDestino    = false;
  let mostrarDropdownAvion      = false;
  let mostrarDropdownTripulante = false;

  // ========= MODALES ENTIDADES =========
  let mostrarFormularioAvion      = false;
  let mostrarFormularioTripulante = false;
  let mostrarFormularioAeropuerto = false;
  let modoEdicion = false;

  // ========= ROLES =========
  let rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' }
  ];

  // ─────────────────────────────────────────────────────────────────
  //  SISTEMA DE TOASTS
  // ─────────────────────────────────────────────────────────────────
  let toasts  = [];
  let toastId = 0;

  function mostrarToast(tipo, mensaje, duracion = 4000) {
    const id = ++toastId;
    toasts = [...toasts, { id, tipo, mensaje }];
    setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, duracion);
  }

  function cerrarToast(id) {
    toasts = toasts.filter(t => t.id !== id);
  }

  // ─────────────────────────────────────────────────────────────────
  //  MODAL DE CONFIRMACIÓN
  // ─────────────────────────────────────────────────────────────────
  let confirmVisible  = false;
  let confirmMensaje  = '';
  let confirmSubtexto = '';
  let confirmTipo     = 'danger';
  let confirmResolve  = null;

  function mostrarConfirm(mensaje, subtexto = '', tipo = 'danger') {
    confirmMensaje  = mensaje;
    confirmSubtexto = subtexto;
    confirmTipo     = tipo;
    confirmVisible  = true;
    return new Promise(resolve => { confirmResolve = resolve; });
  }

  function confirmarAccion() {
    confirmVisible = false;
    if (confirmResolve) confirmResolve(true);
    confirmResolve = null;
  }

  function cancelarConfirm() {
    confirmVisible = false;
    if (confirmResolve) confirmResolve(false);
    confirmResolve = null;
  }

  // ========= AUTOCOMPLETE AEROPUERTO =========
  let paisQueryAeropuerto       = '';
  let paisesSugeridosAeropuerto = [];
  let paisSeleccionadoAeropuerto = null;
  let ciudadQueryAeropuerto       = '';
  let ciudadesSugeridasAeropuerto = [];
  let ciudadSeleccionadaAeropuerto = false;

  // ========= FORMULARIO VUELO =========
  let nuevoVuelo = {
    numeroVuelo: '',
    aeropuertoOrigenId: '',
    aeropuertoDestinoId: '',
    avionId: '',
    fecha: '',
    horaSalida: '',
    horaLlegada: '',
    fechaLlegada: '',
    boletosTurista: '',
    boletosEjecutivo: '',
    precioTurista: '',
    precioEjecutiva: '',
    tripulantesSeleccionados: []
  };

  // ========= FORMULARIO AVIÓN =========
  let avionForm          = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
  let avionImagenPreview = null;
  let avionImagenBase64  = null;

  // ========= FORMULARIO TRIPULANTE =========
  let tripulanteForm          = { id: null, nombre: '', apellido: '', rolID: '' };
  let tripulanteImagenPreview = null;
  let tripulanteImagenBase64  = null;

  // ========= FORMULARIO AEROPUERTO =========
  let aeropuertoForm          = { id: null, codigo: '', nombre: '', ciudad: '', pais: '' };
  let aeropuertoImagenPreview = null;
  let aeropuertoImagenBase64  = null;

  // ========= MÉTRICAS =========
  let metricasResumen = null;
  let metricasListado = null;
  let loadingMetricas = false;
  let loadingListado  = false;

  // Filtros
  let metFechaDesde = (() => {
    const d = new Date(); d.setDate(d.getDate() - 30);
    return d.toISOString().split('T')[0];
  })();
  let metFechaHasta = new Date().toISOString().split('T')[0];
  let metTipo       = '';    // '' | 'Web' | 'REST'
  let metUsuario    = '';
  let metPagina     = 1;

  // Estado de envío de correo del listado
  let correoExportar       = '';
  let mostrarModalExportar = false;
  let enviandoCorreo       = false;

  async function cargarMetricas() {
    loadingMetricas = true;
    try {
      const params = new URLSearchParams({
        fechaDesde: metFechaDesde,
        fechaHasta: metFechaHasta
      });
      const r = await fetch(`${API}/api/metricas/resumen?${params}`, { credentials: 'include' });
      if (r.ok) metricasResumen = await r.json();
      else mostrarToast('error', 'Error al cargar métricas');
    } catch (e) {
      mostrarToast('error', 'Error de conexión con métricas');
    } finally {
      loadingMetricas = false;
    }
  }

  async function cargarListadoBusquedas(pagina = 1) {
    loadingListado = true;
    metPagina = pagina;
    try {
      const r = await fetch(`${API}/api/metricas/listado`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fechaDesde: metFechaDesde,
          fechaHasta: metFechaHasta,
          tipo:       metTipo     || null,
          usuario:    metUsuario  || null,
          pagina,
          tamañoPagina: 25
        })
      });
      if (r.ok) metricasListado = await r.json();
      else mostrarToast('error', 'Error al cargar listado');
    } catch (e) {
      mostrarToast('error', 'Error de conexión');
    } finally {
      loadingListado = false;
    }
  }

  function aplicarFiltrosMetricas() {
    cargarMetricas();
    cargarListadoBusquedas(1);
  }

  async function activarMetricas() {
    if (!metricasResumen) await cargarMetricas();
    if (!metricasListado) await cargarListadoBusquedas(1);
  }

  // Gráficas SVG helpers
  function svgLinea(datos, w = 700, h = 200) {
    if (!datos || datos.length === 0) return '';
    const maxVal = Math.max(...datos.map(d => d.total), 1);
    const pts = datos.map((d, i) => {
      const x = (i / (datos.length - 1)) * (w - 40) + 20;
      const y = h - 20 - ((d.total / maxVal) * (h - 40));
      return `${x},${y}`;
    });
    return pts.join(' ');
  }

  function svgPuntos(datos, w = 700, h = 200) {
    if (!datos || datos.length === 0) return [];
    const maxVal = Math.max(...datos.map(d => d.total), 1);
    return datos.map((d, i) => ({
      x: (i / (datos.length - 1)) * (w - 40) + 20,
      y: h - 20 - ((d.total / maxVal) * (h - 40)),
      val: d.total,
      label: d.fecha
    }));
  }

  // Donut helpers
  function calcularDonut(porTipo) {
    if (!porTipo || porTipo.length === 0) return [];
    const total = porTipo.reduce((s, t) => s + t.total, 0);
    let startAngle = 0;
    const colores = { 'Web': '#D4AF37', 'REST': '#1C1A18' };
    return porTipo.map(t => {
      const angle = (t.total / total) * 360;
      const start = startAngle;
      startAngle += angle;
      return { ...t, angle, start, color: colores[t.tipo] || '#888', porcentaje: Math.round((t.total / total) * 100) };
    });
  }

  function polarToXY(angleDeg, r) {
    const rad = ((angleDeg - 90) * Math.PI) / 180;
    return { x: 100 + r * Math.cos(rad), y: 100 + r * Math.sin(rad) };
  }

  function donutPath(startDeg, endDeg, r = 70, innerR = 40) {
    const s = polarToXY(startDeg, r);
    const e = polarToXY(endDeg, r);
    const si = polarToXY(startDeg, innerR);
    const ei = polarToXY(endDeg, innerR);
    const large = endDeg - startDeg > 180 ? 1 : 0;
    return `M ${s.x} ${s.y} A ${r} ${r} 0 ${large} 1 ${e.x} ${e.y} L ${ei.x} ${ei.y} A ${innerR} ${innerR} 0 ${large} 0 ${si.x} ${si.y} Z`;
  }

  // ========= LIFECYCLE =========
  onMount(async () => {
    if (rolNombre !== 'Administrador') { navigateTo('acceso-denegado'); return; }
    await cargarDatosIniciales();
  });

  onDestroy(() => { unsubscribeSesion(); });

  async function cargarDatosIniciales() {
    await Promise.all([
      cargarUsuarios(), cargarAviones(), cargarTripulantes(),
      cargarAeropuertos(), cargarRolesTripulacion(),
      cargarPaises(), cargarHistorialVuelos()
    ]);
  }

  // ========= CARGA DE DATOS =========
  async function cargarUsuarios() {
    loadingUsuarios = true;
    try {
      const r = await fetch(`${API}/api/usuarios`, { credentials: 'include' });
      if (r.ok) usuarios = await r.json();
    } catch (e) { console.error(e); } finally { loadingUsuarios = false; }
  }

  async function cargarAviones() {
    loadingAviones = true;
    try {
      const r = await fetch(`${API}/api/aviones`);
      if (r.ok) aviones = await r.json();
    } catch (e) { console.error(e); } finally { loadingAviones = false; }
  }

  async function cargarTripulantes() {
    loadingTripulantes = true;
    try {
      const r = await fetch(`${API}/api/tripulacion`);
      if (r.ok) tripulantes = await r.json();
    } catch (e) { console.error(e); } finally { loadingTripulantes = false; }
  }

  async function cargarAeropuertos() {
    loadingAeropuertos = true;
    try {
      const r = await fetch(`${API}/api/aeropuertos`);
      if (r.ok) aeropuertos = await r.json();
    } catch (e) { console.error(e); } finally { loadingAeropuertos = false; }
  }

  async function cargarRolesTripulacion() {
    try {
      const r = await fetch(`${API}/api/tripulacion/roles`);
      if (r.ok) {
        const roles = await r.json();
        rolesTripulacion = roles.map(rol => ({ id: rol.id, nombre: rol.cargo }));
      }
    } catch (e) { console.error(e); }
  }

  async function cargarPaises() {
    try {
      const r = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await r.json();
      todosLosPaises = data.data;
    } catch (e) { console.error(e); }
  }

  async function cargarHistorialVuelos() {
    loadingHistorialVuelos = true;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/historial`, { credentials: 'include' });
      if (r.ok) historialVuelos = await r.json();
    } catch (e) { console.error(e); } finally { loadingHistorialVuelos = false; }
  }

  // ========= IMAGEN HELPER =========
  function leerImagenComoBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload  = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  function onAvionImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    leerImagenComoBase64(file).then(b64 => { avionImagenBase64 = b64; avionImagenPreview = b64; });
  }
  function onTripulanteImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    leerImagenComoBase64(file).then(b64 => { tripulanteImagenBase64 = b64; tripulanteImagenPreview = b64; });
  }
  function onAeropuertoImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    leerImagenComoBase64(file).then(b64 => { aeropuertoImagenBase64 = b64; aeropuertoImagenPreview = b64; });
  }

  // ========= AUTOCOMPLETE AEROPUERTO =========
  function onPaisAeropuertoInput() {
    const q = paisQueryAeropuerto.toLowerCase();
    paisesSugeridosAeropuerto = q.length < 2 ? [] :
      todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (paisQueryAeropuerto && !paisSeleccionadoAeropuerto) aeropuertoForm.pais = '';
  }

  function seleccionarPaisAeropuerto(p) {
    paisSeleccionadoAeropuerto = p;
    paisQueryAeropuerto = p.country;
    aeropuertoForm.pais = p.country;
    paisesSugeridosAeropuerto = [];
    ciudadQueryAeropuerto = ''; aeropuertoForm.ciudad = '';
    ciudadesSugeridasAeropuerto = []; ciudadSeleccionadaAeropuerto = false;
  }

  function validarPaisAeropuertoSeleccionado() {
    if (paisQueryAeropuerto && !paisSeleccionadoAeropuerto) paisQueryAeropuerto = '';
  }

  function onCiudadAeropuertoInput() {
    if (!paisSeleccionadoAeropuerto) return;
    const q = ciudadQueryAeropuerto.toLowerCase();
    ciudadesSugeridasAeropuerto = q.length < 2 ? [] :
      paisSeleccionadoAeropuerto.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    if (ciudadQueryAeropuerto && !ciudadSeleccionadaAeropuerto) aeropuertoForm.ciudad = '';
  }

  function seleccionarCiudadAeropuerto(c) {
    ciudadQueryAeropuerto = c; aeropuertoForm.ciudad = c;
    ciudadesSugeridasAeropuerto = []; ciudadSeleccionadaAeropuerto = true;
  }

  function validarCiudadAeropuertoSeleccionada() {
    if (ciudadQueryAeropuerto && !ciudadSeleccionadaAeropuerto) ciudadQueryAeropuerto = '';
  }

  // ========= FILTROS REACTIVOS =========
  $: aeropuertosFiltradosOrigen = aeropuertos.filter(a =>
    a.nombre.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
    a.codigo.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
    a.ciudad.toLowerCase().includes(busquedaOrigen.toLowerCase())
  );
  $: aeropuertosFiltradosDestino = aeropuertos.filter(a =>
    a.nombre.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
    a.codigo.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
    a.ciudad.toLowerCase().includes(busquedaDestino.toLowerCase())
  );
  $: avionesFiltrados = aviones.filter(a =>
    a.nombreCompleto.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
    a.marca.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
    a.modelo.toLowerCase().includes(busquedaAvion.toLowerCase())
  );
  $: tripulantesFiltrados = tripulantes.filter(t => {
    const yaSeleccionado = nuevoVuelo.tripulantesSeleccionados.some(ts => ts.id === t.id);
    const coincide =
      t.nombreCompleto.toLowerCase().includes(busquedaTripulante.toLowerCase()) ||
      t.nombreRol.toLowerCase().includes(busquedaTripulante.toLowerCase());
    return !yaSeleccionado && coincide;
  });

  $: aeropuertoOrigen  = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoOrigenId));
  $: aeropuertoDestino = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoDestinoId));
  $: avionSeleccionado = aviones.find(a => a.id === parseInt(nuevoVuelo.avionId));

  $: if (nuevoVuelo.fecha && !nuevoVuelo.fechaLlegada) {
    nuevoVuelo.fechaLlegada = nuevoVuelo.fecha;
  }

  $: if (avionSeleccionado && !nuevoVuelo.boletosTurista && !nuevoVuelo.boletosEjecutivo) {
    const cap = avionSeleccionado.capacidadPasajeros;
    const eje = Math.floor(cap * 0.25);
    nuevoVuelo.boletosEjecutivo = eje;
    nuevoVuelo.boletosTurista   = cap - eje;
  }

  $: totalBoletosAsignados = (parseInt(nuevoVuelo.boletosTurista)   || 0) +
                             (parseInt(nuevoVuelo.boletosEjecutivo) || 0);
  $: capacidadAvion = avionSeleccionado?.capacidadPasajeros ?? 0;
  $: excedeLimite   = capacidadAvion > 0 && totalBoletosAsignados > capacidadAvion;
  $: porcentajeOcupado = capacidadAvion > 0
    ? Math.min(100, Math.round(totalBoletosAsignados / capacidadAvion * 100))
    : 0;

  // ========= VUELO HELPERS =========
  function seleccionarAeropuertoOrigen(a) {
    nuevoVuelo.aeropuertoOrigenId = a.id;
    busquedaOrigen = `${a.codigo} - ${a.nombre}`;
    mostrarDropdownOrigen = false;
  }
  function seleccionarAeropuertoDestino(a) {
    nuevoVuelo.aeropuertoDestinoId = a.id;
    busquedaDestino = `${a.codigo} - ${a.nombre}`;
    mostrarDropdownDestino = false;
  }
  function seleccionarAvion(a) {
    nuevoVuelo.avionId = a.id;
    busquedaAvion = a.nombreCompleto;
    mostrarDropdownAvion = false;
    nuevoVuelo.boletosTurista   = '';
    nuevoVuelo.boletosEjecutivo = '';
  }
  function agregarTripulante(t) {
    nuevoVuelo.tripulantesSeleccionados = [...nuevoVuelo.tripulantesSeleccionados, t];
    busquedaTripulante = ''; mostrarDropdownTripulante = false;
  }
  function quitarTripulante(id) {
    nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados.filter(t => t.id !== id);
  }
  function limpiarFormularioVuelo() {
    nuevoVuelo = {
      numeroVuelo: '', aeropuertoOrigenId: '', aeropuertoDestinoId: '',
      avionId: '', fecha: '', horaSalida: '', horaLlegada: '',
      fechaLlegada: '', boletosTurista: '', boletosEjecutivo: '',
      precioTurista: '', precioEjecutiva: '', tripulantesSeleccionados: []
    };
    busquedaOrigen = ''; busquedaDestino = ''; busquedaAvion = ''; busquedaTripulante = '';
  }

  async function handleCrearVuelo() {
    if (!nuevoVuelo.numeroVuelo)         { mostrarToast('error', 'Ingresa el número de vuelo'); return; }
    if (!nuevoVuelo.aeropuertoOrigenId)  { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevoVuelo.aeropuertoDestinoId) { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (!nuevoVuelo.avionId)             { mostrarToast('error', 'Selecciona un avión'); return; }
    if (!nuevoVuelo.fecha)               { mostrarToast('error', 'Selecciona la fecha del vuelo'); return; }
    if (!nuevoVuelo.horaSalida || !nuevoVuelo.horaLlegada) { mostrarToast('error', 'Ingresa las horas de salida y llegada'); return; }
    if (!nuevoVuelo.fechaLlegada) { mostrarToast('error', 'Ingresa la fecha de llegada'); return; }
    if (nuevoVuelo.fechaLlegada < nuevoVuelo.fecha) { mostrarToast('error', 'La fecha de llegada no puede ser anterior a la fecha de salida'); return; }
    if (!nuevoVuelo.boletosTurista || parseInt(nuevoVuelo.boletosTurista) < 0)   { mostrarToast('error', 'Ingresa los boletos de clase turista'); return; }
    if (!nuevoVuelo.boletosEjecutivo || parseInt(nuevoVuelo.boletosEjecutivo) < 0) { mostrarToast('error', 'Ingresa los boletos de clase ejecutiva'); return; }
    if (excedeLimite) { mostrarToast('error', `La suma de boletos (${totalBoletosAsignados}) excede la capacidad del avión (${capacidadAvion})`); return; }
    if (!nuevoVuelo.precioTurista || !nuevoVuelo.precioEjecutiva) { mostrarToast('error', 'Ingresa los precios de ambas clases'); return; }

    try {
      const datos = {
        numeroVuelo:         nuevoVuelo.numeroVuelo,
        aeropuertoOrigenId:  parseInt(nuevoVuelo.aeropuertoOrigenId),
        aeropuertoDestinoId: parseInt(nuevoVuelo.aeropuertoDestinoId),
        avionId:             parseInt(nuevoVuelo.avionId),
        fecha:               nuevoVuelo.fecha,
        horaSalida:          nuevoVuelo.horaSalida,
        horaLlegada:         nuevoVuelo.horaLlegada,
        fechaLlegada:        nuevoVuelo.fechaLlegada || null,
        boletosTurista:      parseInt(nuevoVuelo.boletosTurista),
        boletosEjecutivo:    parseInt(nuevoVuelo.boletosEjecutivo),
        precioTurista:       parseFloat(nuevoVuelo.precioTurista),
        precioEjecutiva:     parseFloat(nuevoVuelo.precioEjecutiva),
        tripulantesIds:      nuevoVuelo.tripulantesSeleccionados.map(t => t.id)
      };

      const r = await fetch(`${API}/api/admin/vuelos`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
      });

      if (r.ok) {
        mostrarToast('success', '¡Vuelo creado exitosamente!');
        limpiarFormularioVuelo();
        await cargarHistorialVuelos();
        activeSection = 'historial';
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al crear el vuelo');
      }
    } catch (e) {
      mostrarToast('error', 'Error de conexión al crear el vuelo');
    }
  }

  async function handleCambiarRol(userId, nuevoRolId) {
    try {
      const r = await fetch(`${API}/api/usuarios/cambiar-rol`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioId: parseInt(userId), nuevoRolId: parseInt(nuevoRolId) })
      });
      if (r.ok) {
        mostrarToast('success', 'Rol actualizado correctamente');
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al cambiar el rol');
      }
    } catch (e) { mostrarToast('error', 'Error de conexión al cambiar el rol'); }
  }

  async function handleCambiarEstadoVuelo(vueloId) {
    const ok = await mostrarConfirm(
      '¿Cancelar este vuelo?',
      'Se cancelarán también los boletos activos y las reservaciones asociadas.',
      'danger'
    );
    if (!ok) return;

    try {
      const r = await fetch(`${API}/api/admin/vuelos/${vueloId}/cancelar`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      });
      if (r.ok) {
        mostrarToast('success', 'Vuelo cancelado exitosamente');
        await cargarHistorialVuelos();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al cancelar el vuelo');
      }
    } catch (e) { mostrarToast('error', 'Error de conexión al cancelar el vuelo'); }
  }

  // ===== AVIONES =====
  function abrirFormularioNuevoAvion() {
    modoEdicion = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
    mostrarFormularioAvion = true;
  }
  function abrirFormularioEditarAvion(avion) {
    modoEdicion = true;
    avionForm = { id: avion.id, marca: avion.marca, modelo: avion.modelo, capacidadPasajeros: avion.capacidadPasajeros };
    avionImagenBase64 = null; avionImagenPreview = avion.imagenBase64 || null;
    mostrarFormularioAvion = true;
  }
  function cerrarFormularioAvion() {
    mostrarFormularioAvion = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
  }

  async function handleGuardarAvion() {
    try {
      const payload = {
        marca: avionForm.marca, modelo: avionForm.modelo,
        capacidadPasajeros: parseInt(avionForm.capacidadPasajeros),
        imagenBase64: avionImagenBase64 || null
      };
      const url    = modoEdicion ? `${API}/api/aviones/${avionForm.id}` : `${API}/api/aviones`;
      const method = modoEdicion ? 'PUT' : 'POST';
      const r = await fetch(url, {
        method, credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (r.ok) {
        mostrarToast('success', modoEdicion ? 'Avión actualizado correctamente' : 'Avión creado correctamente');
        await cargarAviones(); cerrarFormularioAvion();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el avión');
      }
    } catch (e) { mostrarToast('error', 'Error de conexión al guardar el avión'); }
  }

  async function handleEliminarImagenAvion(avionId) {
    const ok = await mostrarConfirm('¿Quitar la imagen de este avión?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/aviones/${avionId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) { mostrarToast('success', 'Imagen eliminada'); await cargarAviones(); }
      else mostrarToast('error', 'Error al eliminar la imagen');
    } catch (e) { mostrarToast('error', 'Error de conexión'); }
  }

  // ===== TRIPULANTES =====
  function abrirFormularioNuevoTripulante() {
    modoEdicion = false;
    tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
    tripulanteImagenPreview = null; tripulanteImagenBase64 = null;
    mostrarFormularioTripulante = true;
  }
  function abrirFormularioEditarTripulante(t) {
    modoEdicion = true;
    tripulanteForm = { id: t.id, nombre: t.nombre, apellido: t.apellido, rolID: t.rolID };
    tripulanteImagenBase64 = null; tripulanteImagenPreview = t.imagenBase64 || null;
    mostrarFormularioTripulante = true;
  }
  function cerrarFormularioTripulante() {
    mostrarFormularioTripulante = false;
    tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
    tripulanteImagenPreview = null; tripulanteImagenBase64 = null;
  }

  async function handleGuardarTripulante() {
    try {
      const payload = {
        nombre: tripulanteForm.nombre, apellido: tripulanteForm.apellido,
        rolID: parseInt(tripulanteForm.rolID),
        imagenBase64: tripulanteImagenBase64 || null
      };
      const url    = modoEdicion ? `${API}/api/tripulacion/${tripulanteForm.id}` : `${API}/api/tripulacion`;
      const method = modoEdicion ? 'PUT' : 'POST';
      const r = await fetch(url, {
        method, credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (r.ok) {
        mostrarToast('success', modoEdicion ? 'Tripulante actualizado correctamente' : 'Tripulante creado correctamente');
        await cargarTripulantes(); cerrarFormularioTripulante();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el tripulante');
      }
    } catch (e) { mostrarToast('error', 'Error de conexión al guardar el tripulante'); }
  }

  async function handleEliminarImagenTripulante(tripulanteId) {
    const ok = await mostrarConfirm('¿Quitar la foto de este tripulante?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/tripulacion/${tripulanteId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) { mostrarToast('success', 'Foto eliminada'); await cargarTripulantes(); }
      else mostrarToast('error', 'Error al eliminar la foto');
    } catch (e) { mostrarToast('error', 'Error de conexión'); }
  }

  // ===== AEROPUERTOS =====
  function abrirFormularioNuevoAeropuerto() {
    modoEdicion = false;
    aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '' };
    paisQueryAeropuerto = ''; ciudadQueryAeropuerto = '';
    paisSeleccionadoAeropuerto = null; ciudadSeleccionadaAeropuerto = false;
    paisesSugeridosAeropuerto = []; ciudadesSugeridasAeropuerto = [];
    aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null;
    mostrarFormularioAeropuerto = true;
  }

  async function abrirFormularioEditarAeropuerto(aeropuerto) {
    modoEdicion = true;
    try {
      const r = await fetch(`${API}/api/aeropuertos/${aeropuerto.id}`);
      if (r.ok) {
        const completo = await r.json();
        aeropuertoForm = { id: completo.id, codigo: completo.codigo, nombre: completo.nombre, ciudad: completo.ciudad, pais: completo.pais };
        paisQueryAeropuerto   = completo.pais;
        ciudadQueryAeropuerto = completo.ciudad;
        const paisEncontrado = todosLosPaises.find(p => p.country.toLowerCase() === completo.pais.toLowerCase());
        if (paisEncontrado) paisSeleccionadoAeropuerto = paisEncontrado;
        ciudadSeleccionadaAeropuerto = true;
        aeropuertoImagenBase64  = null;
        aeropuertoImagenPreview = completo.imagenBase64 || null;
        mostrarFormularioAeropuerto = true;
      }
    } catch (e) { mostrarToast('error', 'Error al cargar los datos del aeropuerto'); }
  }

  function cerrarFormularioAeropuerto() {
    mostrarFormularioAeropuerto = false;
    aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '' };
    paisQueryAeropuerto = ''; ciudadQueryAeropuerto = '';
    paisSeleccionadoAeropuerto = null; ciudadSeleccionadaAeropuerto = false;
    paisesSugeridosAeropuerto = []; ciudadesSugeridasAeropuerto = [];
    aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null;
  }

  async function handleGuardarAeropuerto() {
    if (!paisSeleccionadoAeropuerto || !aeropuertoForm.pais) {
      mostrarToast('error', 'Debes seleccionar un país de la lista'); return;
    }
    if (!ciudadSeleccionadaAeropuerto || !aeropuertoForm.ciudad) {
      mostrarToast('error', 'Debes seleccionar una ciudad de la lista'); return;
    }
    try {
      const payload = {
        nombre: aeropuertoForm.nombre, codigo: aeropuertoForm.codigo.toUpperCase(),
        ciudad: aeropuertoForm.ciudad, pais: aeropuertoForm.pais,
        imagenBase64: aeropuertoImagenBase64 || null
      };
      const url    = modoEdicion ? `${API}/api/aeropuertos/${aeropuertoForm.id}` : `${API}/api/aeropuertos`;
      const method = modoEdicion ? 'PUT' : 'POST';
      const r = await fetch(url, {
        method, credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (r.ok) {
        mostrarToast('success', modoEdicion ? 'Aeropuerto actualizado correctamente' : 'Aeropuerto creado correctamente');
        await cargarAeropuertos(); cerrarFormularioAeropuerto();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el aeropuerto');
      }
    } catch (e) { mostrarToast('error', 'Error de conexión al guardar el aeropuerto'); }
  }

  async function handleEliminarImagenAeropuerto(aeropuertoId) {
    const ok = await mostrarConfirm('¿Quitar la imagen de este aeropuerto?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/aeropuertos/${aeropuertoId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) { mostrarToast('success', 'Imagen eliminada'); await cargarAeropuertos(); }
      else mostrarToast('error', 'Error al eliminar la imagen');
    } catch (e) { mostrarToast('error', 'Error de conexión'); }
  }
</script>

<!-- ═══════════════════════════════════════════════════════════
     TOASTS
════════════════════════════════════════════════════════════ -->
<div class="toast-stack" aria-live="polite">
  {#each toasts as toast (toast.id)}
    <div class="toast toast--{toast.tipo}" role="alert">
      <span class="toast__icon">
        {#if toast.tipo === 'success'}✓{:else if toast.tipo === 'error'}✕{:else}⚠{/if}
      </span>
      <span class="toast__msg">{toast.mensaje}</span>
      <button class="toast__close" on:click={() => cerrarToast(toast.id)} aria-label="Cerrar">×</button>
    </div>
  {/each}
</div>

<!-- ═══════════════════════════════════════════════════════════
     MODAL CONFIRMACIÓN
════════════════════════════════════════════════════════════ -->
{#if confirmVisible}
  <div class="modal-overlay" on:click={cancelarConfirm} role="dialog" aria-modal="true">
    <div class="confirm-dialog" on:click|stopPropagation>
      <div class="confirm-dialog__icon confirm-dialog__icon--{confirmTipo}">
        {#if confirmTipo === 'danger'}
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/></svg>
        {:else}
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/></svg>
        {/if}
      </div>
      <h3 class="confirm-dialog__title">{confirmMensaje}</h3>
      {#if confirmSubtexto}
        <p class="confirm-dialog__sub">{confirmSubtexto}</p>
      {/if}
      <div class="confirm-dialog__actions">
        <button class="confirm-dialog__btn confirm-dialog__btn--cancel" on:click={cancelarConfirm}>
          No, cancelar
        </button>
        <button class="confirm-dialog__btn confirm-dialog__btn--{confirmTipo}" on:click={confirmarAccion}>
          Sí, confirmar
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- ═══════════════════════════════════════════════════════════
     PANEL PRINCIPAL
════════════════════════════════════════════════════════════ -->
<div class="admin">
  <div class="admin__container">

    <div class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>← Salir del Panel</button>
      <h1 class="admin__title">Panel de Administracion</h1>
      <p class="admin__subtitle">Gestion de vuelos, usuarios y metricas</p>
    </div>

    <div class="admin__content">
      <aside class="admin__sidebar">
        <nav class="admin-nav">
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'crear-vuelo'}
            on:click={() => activeSection = 'crear-vuelo'}>Crear Vuelo</button>
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'gestionar-aviones'}
            on:click={() => activeSection = 'gestionar-aviones'}>Gestionar Aviones</button>
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'gestionar-tripulantes'}
            on:click={() => activeSection = 'gestionar-tripulantes'}>Gestionar Tripulantes</button>
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'gestionar-aeropuertos'}
            on:click={() => activeSection = 'gestionar-aeropuertos'}>Gestionar Aeropuertos</button>
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'historial'}
            on:click={() => activeSection = 'historial'}>Historial</button>
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'usuarios'}
            on:click={() => activeSection = 'usuarios'}>Usuarios</button>
          <button class="admin-nav__item" class:admin-nav__item--active={activeSection === 'metricas'}
            on:click={() => { activeSection = 'metricas'; activarMetricas(); }}>Metricas</button>
        </nav>
      </aside>

      <main class="admin__main">

        <!-- ===== CREAR VUELO ===== -->
        {#if activeSection === 'crear-vuelo'}
          <section class="admin-section">
            <h2 class="admin-section__title">Crear Nuevo Vuelo</h2>
            <p class="admin-section__subtitle">Completa todos los datos del vuelo</p>

            <form class="admin-form" on:submit|preventDefault={handleCrearVuelo}>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Informacion Basica</h3>
                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="numeroVuelo" class="admin-form__label">Numero de Vuelo *</label>
                    <input type="text" id="numeroVuelo" class="admin-form__input"
                      bind:value={nuevoVuelo.numeroVuelo} placeholder="Ej: AA 1234" required />
                  </div>
                  <div class="admin-form__field">
                    <label for="fecha" class="admin-form__label">Fecha del Vuelo *</label>
                    <input type="date" id="fecha" class="admin-form__input"
                      bind:value={nuevoVuelo.fecha} required />
                  </div>
                </div>
              </div>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Ruta</h3>
                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label class="admin-form__label">Aeropuerto de Origen *</label>
                    {#if loadingAeropuertos}
                      <p class="loading-text">Cargando aeropuertos...</p>
                    {:else}
                      <div class="searchable-select">
                        <input type="text" class="admin-form__input" bind:value={busquedaOrigen}
                          on:focus={() => mostrarDropdownOrigen = true}
                          on:blur={() => setTimeout(() => mostrarDropdownOrigen = false, 200)}
                          placeholder="Buscar aeropuerto..." autocomplete="off" />
                        {#if mostrarDropdownOrigen && aeropuertosFiltradosOrigen.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each aeropuertosFiltradosOrigen.slice(0,10) as a}
                              <button type="button" class="searchable-select__option"
                                on:click={() => seleccionarAeropuertoOrigen(a)}>
                                <span class="searchable-select__option-code">{a.codigo}</span>
                                <span class="searchable-select__option-name">{a.nombre}</span>
                                <span class="searchable-select__option-city">{a.ciudad}</span>
                              </button>
                            {/each}
                          </div>
                        {/if}
                        {#if aeropuertoOrigen}
                          <p class="selected-item">✔ {aeropuertoOrigen.codigo} — {aeropuertoOrigen.nombre}</p>
                        {/if}
                      </div>
                    {/if}
                  </div>
                  <div class="admin-form__field">
                    <label class="admin-form__label">Aeropuerto de Destino *</label>
                    {#if loadingAeropuertos}
                      <p class="loading-text">Cargando aeropuertos...</p>
                    {:else}
                      <div class="searchable-select">
                        <input type="text" class="admin-form__input" bind:value={busquedaDestino}
                          on:focus={() => mostrarDropdownDestino = true}
                          on:blur={() => setTimeout(() => mostrarDropdownDestino = false, 200)}
                          placeholder="Buscar aeropuerto..." autocomplete="off" />
                        {#if mostrarDropdownDestino && aeropuertosFiltradosDestino.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each aeropuertosFiltradosDestino.slice(0,10) as a}
                              <button type="button" class="searchable-select__option"
                                on:click={() => seleccionarAeropuertoDestino(a)}>
                                <span class="searchable-select__option-code">{a.codigo}</span>
                                <span class="searchable-select__option-name">{a.nombre}</span>
                                <span class="searchable-select__option-city">{a.ciudad}</span>
                              </button>
                            {/each}
                          </div>
                        {/if}
                        {#if aeropuertoDestino}
                          <p class="selected-item">✔ {aeropuertoDestino.codigo} — {aeropuertoDestino.nombre}</p>
                        {/if}
                      </div>
                    {/if}
                  </div>
                </div>
              </div>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Horarios</h3>
                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="horaSalida" class="admin-form__label">Hora de Salida *</label>
                    <input type="time" id="horaSalida" class="admin-form__input"
                      bind:value={nuevoVuelo.horaSalida} required />
                  </div>
                  <div class="admin-form__field">
                    <label for="horaLlegada" class="admin-form__label">Hora de Llegada *</label>
                    <input type="time" id="horaLlegada" class="admin-form__input"
                      bind:value={nuevoVuelo.horaLlegada} required />
                  </div>
                </div>
                <div class="admin-form__row" style="margin-top:1.25rem">
                  <div class="admin-form__field">
                    <label for="fechaLlegada" class="admin-form__label">Fecha de Llegada *</label>
                    <input type="date" id="fechaLlegada" class="admin-form__input"
                      bind:value={nuevoVuelo.fechaLlegada}
                      min={nuevoVuelo.fecha || undefined}
                      required />
                    {#if nuevoVuelo.fecha && nuevoVuelo.fechaLlegada && nuevoVuelo.fechaLlegada > nuevoVuelo.fecha}
                      <p class="vuelo-nextday-note">✈ Vuelo con llegada al día siguiente o posterior</p>
                    {/if}
                  </div>
                </div>
              </div>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Aeronave</h3>
                <div class="admin-form__field admin-form__field--full">
                  <label class="admin-form__label">Seleccionar Avion *</label>
                  {#if loadingAviones}
                    <p class="loading-text">Cargando aviones...</p>
                  {:else}
                    <div class="searchable-select">
                      <input type="text" class="admin-form__input" bind:value={busquedaAvion}
                        on:focus={() => mostrarDropdownAvion = true}
                        on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
                        placeholder="Buscar avion..." autocomplete="off" />
                      {#if mostrarDropdownAvion && avionesFiltrados.length > 0}
                        <div class="searchable-select__dropdown">
                          {#each avionesFiltrados.slice(0,10) as a}
                            <button type="button" class="searchable-select__option"
                              on:click={() => seleccionarAvion(a)}>
                              {#if a.imagenBase64}
                                <img src={a.imagenBase64} alt={a.nombreCompleto} class="dropdown-thumb" />
                              {/if}
                              <span class="searchable-select__option-name">{a.nombreCompleto}</span>
                              <span class="searchable-select__option-detail">{a.capacidadPasajeros} pasajeros</span>
                            </button>
                          {/each}
                        </div>
                      {/if}
                      {#if avionSeleccionado}
                        <p class="selected-item">✔ {avionSeleccionado.nombreCompleto}</p>
                      {/if}
                    </div>
                  {/if}
                </div>
              </div>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Distribucion de Asientos y Precios</h3>

                {#if avionSeleccionado}
                  <div class="capacidad-bar">
                    <div class="capacidad-bar__labels">
                      <span>Capacidad total: <strong>{capacidadAvion} pax</strong></span>
                      <span class="capacidad-bar__count"
                        class:capacidad-bar__count--ok={totalBoletosAsignados === capacidadAvion && !excedeLimite}
                        class:capacidad-bar__count--error={excedeLimite}>
                        {totalBoletosAsignados} asignados
                        {#if excedeLimite}&nbsp;⚠ Excede límite{:else if totalBoletosAsignados === capacidadAvion}&nbsp;✔ Completo{/if}
                      </span>
                    </div>
                    <div class="capacidad-bar__track">
                      <div class="capacidad-bar__fill"
                        class:capacidad-bar__fill--error={excedeLimite}
                        style="width:{porcentajeOcupado}%">
                      </div>
                    </div>
                  </div>
                {/if}

                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="boletosTurista" class="admin-form__label">Boletos Clase Turista *</label>
                    <input type="number" id="boletosTurista" class="admin-form__input" min="0"
                      bind:value={nuevoVuelo.boletosTurista} placeholder="Ej: 180"
                      max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
                  </div>
                  <div class="admin-form__field">
                    <label for="boletosEjecutivo" class="admin-form__label">Boletos Clase Ejecutiva *</label>
                    <input type="number" id="boletosEjecutivo" class="admin-form__input" min="0"
                      bind:value={nuevoVuelo.boletosEjecutivo} placeholder="Ej: 60"
                      max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
                  </div>
                </div>

                <div class="admin-form__row" style="margin-top:1.5rem">
                  <div class="admin-form__field">
                    <label for="precioTurista" class="admin-form__label">Precio Turista (USD) *</label>
                    <input type="number" id="precioTurista" class="admin-form__input" min="0" step="0.01"
                      bind:value={nuevoVuelo.precioTurista} placeholder="Ej: 150.00" required />
                  </div>
                  <div class="admin-form__field">
                    <label for="precioEjecutiva" class="admin-form__label">Precio Ejecutiva (USD) *</label>
                    <input type="number" id="precioEjecutiva" class="admin-form__input" min="0" step="0.01"
                      bind:value={nuevoVuelo.precioEjecutiva} placeholder="Ej: 300.00" required />
                  </div>
                </div>
              </div>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Tripulacion</h3>
                <div class="admin-form__field admin-form__field--full">
                  <label class="admin-form__label">Agregar Tripulantes</label>
                  {#if loadingTripulantes}
                    <p class="loading-text">Cargando tripulantes...</p>
                  {:else}
                    <div class="searchable-select">
                      <input type="text" class="admin-form__input" bind:value={busquedaTripulante}
                        on:focus={() => mostrarDropdownTripulante = true}
                        on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
                        placeholder="Buscar por nombre o rol..." autocomplete="off" />
                      {#if mostrarDropdownTripulante && tripulantesFiltrados.length > 0}
                        <div class="searchable-select__dropdown">
                          {#each tripulantesFiltrados.slice(0,10) as t}
                            <button type="button" class="searchable-select__option"
                              on:click={() => agregarTripulante(t)}>
                              {#if t.imagenBase64}
                                <img src={t.imagenBase64} alt={t.nombreCompleto}
                                  class="dropdown-thumb dropdown-thumb--circle" />
                              {/if}
                              <span class="searchable-select__option-name">{t.nombreCompleto}</span>
                              <span class="searchable-select__option-role">{t.nombreRol}</span>
                            </button>
                          {/each}
                        </div>
                      {/if}
                    </div>
                    {#if nuevoVuelo.tripulantesSeleccionados.length > 0}
                      <div class="tripulantes-seleccionados">
                        <p class="tripulantes-seleccionados__title">
                          Tripulantes seleccionados ({nuevoVuelo.tripulantesSeleccionados.length})
                        </p>
                        <div class="tripulantes-seleccionados__list">
                          {#each nuevoVuelo.tripulantesSeleccionados as t}
                            <div class="tripulante-item">
                              <div class="tripulante-item__info">
                                <span class="tripulante-item__name">{t.nombreCompleto}</span>
                                <span class="tripulante-item__rol">{t.nombreRol}</span>
                              </div>
                              <button type="button" class="tripulante-item__remove"
                                on:click={() => quitarTripulante(t.id)}>×</button>
                            </div>
                          {/each}
                        </div>
                      </div>
                    {/if}
                  {/if}
                </div>
              </div>

              <div class="admin-form__actions">
                <button type="submit" class="admin-form__submit">Crear Vuelo</button>
                <button type="button" class="admin-form__cancel" on:click={limpiarFormularioVuelo}>Limpiar</button>
              </div>
            </form>
          </section>

        <!-- ===== GESTIONAR AVIONES ===== -->
        {:else if activeSection === 'gestionar-aviones'}
          <section class="admin-section">
            <div class="section-header">
              <div>
                <h2 class="admin-section__title">Gestionar Aviones</h2>
                <p class="admin-section__subtitle">Administra la flota de aviones</p>
              </div>
              <button class="btn-add" on:click={abrirFormularioNuevoAvion}>
                <span class="btn-add__icon">+</span> Nuevo Avion
              </button>
            </div>
            {#if loadingAviones}
              <p class="loading-text">Cargando aviones...</p>
            {:else if aviones.length === 0}
              <div class="placeholder-card"><p class="placeholder-card__text">No hay aviones registrados.</p></div>
            {:else}
              <table class="table">
                <thead class="table__head">
                  <tr>
                    <th class="table__header">Imagen</th>
                    <th class="table__header">ID</th>
                    <th class="table__header">Marca</th>
                    <th class="table__header">Modelo</th>
                    <th class="table__header">Capacidad</th>
                    <th class="table__header">Acciones</th>
                  </tr>
                </thead>
                <tbody class="table__body">
                  {#each aviones as avion}
                    <tr class="table__row">
                      <td class="table__cell" data-label="Imagen">
                        {#if avion.imagenBase64}
                          <img src={avion.imagenBase64} alt={avion.nombreCompleto} class="entity-thumb" />
                        {:else}<span style="color:#9ca3af">—</span>{/if}
                      </td>
                      <td class="table__cell" data-label="ID">{avion.id}</td>
                      <td class="table__cell" data-label="Marca">{avion.marca}</td>
                      <td class="table__cell" data-label="Modelo">{avion.modelo}</td>
                      <td class="table__cell" data-label="Capacidad">{avion.capacidadPasajeros} pax</td>
                      <td class="table__cell" data-label="Acciones">
                        <div class="table__actions">
                          <button class="table__action-btn table__action-btn--view"
                            on:click={() => abrirFormularioEditarAvion(avion)}>Editar</button>
                          {#if avion.imagenBase64}
                            <button class="table__action-btn table__action-btn--cancel"
                              on:click={() => handleEliminarImagenAvion(avion.id)}>Quitar img</button>
                          {/if}
                        </div>
                      </td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            {/if}
          </section>

        <!-- ===== GESTIONAR TRIPULANTES ===== -->
        {:else if activeSection === 'gestionar-tripulantes'}
          <section class="admin-section">
            <div class="section-header">
              <div>
                <h2 class="admin-section__title">Gestionar Tripulantes</h2>
                <p class="admin-section__subtitle">Administra los miembros de tripulacion</p>
              </div>
              <button class="btn-add" on:click={abrirFormularioNuevoTripulante}>
                <span class="btn-add__icon">+</span> Nuevo Tripulante
              </button>
            </div>
            {#if loadingTripulantes}
              <p class="loading-text">Cargando tripulantes...</p>
            {:else if tripulantes.length === 0}
              <div class="placeholder-card"><p class="placeholder-card__text">No hay tripulantes registrados.</p></div>
            {:else}
              <table class="table">
                <thead class="table__head">
                  <tr>
                    <th class="table__header">Foto</th>
                    <th class="table__header">ID</th>
                    <th class="table__header">Nombre</th>
                    <th class="table__header">Apellido</th>
                    <th class="table__header">Rol</th>
                    <th class="table__header">Acciones</th>
                  </tr>
                </thead>
                <tbody class="table__body">
                  {#each tripulantes as t}
                    <tr class="table__row">
                      <td class="table__cell" data-label="Foto">
                        {#if t.imagenBase64}
                          <img src={t.imagenBase64} alt={t.nombreCompleto} class="entity-thumb entity-thumb--circle" />
                        {:else}<span style="color:#9ca3af">—</span>{/if}
                      </td>
                      <td class="table__cell" data-label="ID">{t.id}</td>
                      <td class="table__cell" data-label="Nombre">{t.nombre}</td>
                      <td class="table__cell" data-label="Apellido">{t.apellido}</td>
                      <td class="table__cell" data-label="Rol">
                        <span class="rol-badge--tripulacion">{t.nombreRol}</span>
                      </td>
                      <td class="table__cell" data-label="Acciones">
                        <div class="table__actions">
                          <button class="table__action-btn table__action-btn--view"
                            on:click={() => abrirFormularioEditarTripulante(t)}>Editar</button>
                          {#if t.imagenBase64}
                            <button class="table__action-btn table__action-btn--cancel"
                              on:click={() => handleEliminarImagenTripulante(t.id)}>Quitar foto</button>
                          {/if}
                        </div>
                      </td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            {/if}
          </section>

        <!-- ===== GESTIONAR AEROPUERTOS ===== -->
        {:else if activeSection === 'gestionar-aeropuertos'}
          <section class="admin-section">
            <div class="section-header">
              <div>
                <h2 class="admin-section__title">Gestionar Aeropuertos</h2>
                <p class="admin-section__subtitle">Administra los aeropuertos del sistema</p>
              </div>
              <button class="btn-add" on:click={abrirFormularioNuevoAeropuerto}>
                <span class="btn-add__icon">+</span> Nuevo Aeropuerto
              </button>
            </div>
            {#if loadingAeropuertos}
              <p class="loading-text">Cargando aeropuertos...</p>
            {:else if aeropuertos.length === 0}
              <div class="placeholder-card"><p class="placeholder-card__text">No hay aeropuertos registrados.</p></div>
            {:else}
              <table class="table">
                <thead class="table__head">
                  <tr>
                    <th class="table__header">Imagen</th>
                    <th class="table__header">Codigo</th>
                    <th class="table__header">Nombre</th>
                    <th class="table__header">Ciudad</th>
                    <th class="table__header">Pais</th>
                    <th class="table__header">Acciones</th>
                  </tr>
                </thead>
                <tbody class="table__body">
                  {#each aeropuertos as a}
                    <tr class="table__row">
                      <td class="table__cell" data-label="Imagen">
                        {#if a.imagenBase64}
                          <img src={a.imagenBase64} alt={a.nombre} class="entity-thumb" />
                        {:else}<span style="color:#9ca3af">—</span>{/if}
                      </td>
                      <td class="table__cell" data-label="Codigo"><strong>{a.codigo}</strong></td>
                      <td class="table__cell" data-label="Nombre">{a.nombre}</td>
                      <td class="table__cell" data-label="Ciudad">{a.ciudad}</td>
                      <td class="table__cell" data-label="Pais">{a.pais}</td>
                      <td class="table__cell" data-label="Acciones">
                        <div class="table__actions">
                          <button class="table__action-btn table__action-btn--view"
                            on:click={() => abrirFormularioEditarAeropuerto(a)}>Editar</button>
                          {#if a.imagenBase64}
                            <button class="table__action-btn table__action-btn--cancel"
                              on:click={() => handleEliminarImagenAeropuerto(a.id)}>Quitar img</button>
                          {/if}
                        </div>
                      </td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            {/if}
          </section>

        <!-- ===== HISTORIAL ===== -->
        {:else if activeSection === 'historial'}
          <section class="admin-section">
            <h2 class="admin-section__title">Historial de Vuelos</h2>
            <p class="admin-section__subtitle">Todos los vuelos del sistema</p>
            {#if loadingHistorialVuelos}
              <p class="loading-text">Cargando historial...</p>
            {:else if historialVuelos.length === 0}
              <div class="placeholder-card"><p class="placeholder-card__text">No hay vuelos registrados.</p></div>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr>
                      <th class="table__header">No. Vuelo</th>
                      <th class="table__header">Origen</th>
                      <th class="table__header">Destino</th>
                      <th class="table__header">Fecha Salida</th>
                      <th class="table__header">Salida</th>
                      <th class="table__header">Fecha Llegada</th>
                      <th class="table__header">Llegada</th>
                      <th class="table__header">Turista</th>
                      <th class="table__header">Ejecutiva</th>
                      <th class="table__header">P. Turista</th>
                      <th class="table__header">P. Ejecutiva</th>
                      <th class="table__header">Estado</th>
                      <th class="table__header">Acciones</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each historialVuelos as vuelo}
                      <tr class="table__row">
                        <td class="table__cell" data-label="No. Vuelo">{vuelo.numeroVuelo}</td>
                        <td class="table__cell" data-label="Origen">{vuelo.origen}</td>
                        <td class="table__cell" data-label="Destino">{vuelo.destino}</td>
                        <td class="table__cell" data-label="Fecha Salida">{vuelo.fecha}</td>
                        <td class="table__cell" data-label="Salida">{vuelo.horaSalida}</td>
                        <td class="table__cell" data-label="Fecha Llegada">
                          {#if vuelo.fechaLlegada && vuelo.fechaLlegada !== vuelo.fecha}
                            <span class="fecha-llegada-distinta">{vuelo.fechaLlegada} <span class="nextday-tag">+día</span></span>
                          {:else}
                            {vuelo.fechaLlegada ?? vuelo.fecha}
                          {/if}
                        </td>
                        <td class="table__cell" data-label="Llegada">{vuelo.horaLlegada}</td>
                        <td class="table__cell" data-label="Turista">{vuelo.boletosTurista} disp.</td>
                        <td class="table__cell" data-label="Ejecutiva">{vuelo.boletosEjecutivo} disp.</td>
                        <td class="table__cell" data-label="P. Turista">${vuelo.precioTurista}</td>
                        <td class="table__cell" data-label="P. Ejecutiva">${vuelo.precioEjecutiva}</td>
                        <td class="table__cell" data-label="Estado">
                          {#if vuelo.estado === 'Activo'}
                            <span class="status-badge status-badge--activo">{vuelo.estado}</span>
                          {:else if vuelo.estado === 'Cancelado'}
                            <span class="status-badge status-badge--cancelado">{vuelo.estado}</span>
                          {:else if vuelo.estado === 'Finalizado'}
                            <span class="status-badge status-badge--completado">{vuelo.estado}</span>
                          {:else}
                            <span class="status-badge status-badge--activo">{vuelo.estado}</span>
                          {/if}
                        </td>
                        <td class="table__cell" data-label="Acciones">
                          <div class="table__actions">
                            {#if vuelo.estado === 'Activo' || vuelo.estado === 'En curso'}
                              <button class="table__action-btn table__action-btn--cancel"
                                on:click={() => handleCambiarEstadoVuelo(vuelo.id)}>Cancelar</button>
                            {/if}
                          </div>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </section>

        <!-- ===== USUARIOS ===== -->
        {:else if activeSection === 'usuarios'}
          <section class="admin-section">
            <h2 class="admin-section__title">Usuarios</h2>
            <p class="admin-section__subtitle">Gestion de roles de usuarios</p>
            {#if loadingUsuarios}
              <p class="loading-text">Cargando usuarios...</p>
            {:else if usuarios.length === 0}
              <div class="placeholder-card"><p class="placeholder-card__text">No hay usuarios registrados.</p></div>
            {:else}
              <table class="table">
                <thead class="table__head">
                  <tr>
                    <th class="table__header">ID</th>
                    <th class="table__header">Nombre</th>
                    <th class="table__header">Correo</th>
                    <th class="table__header">Username</th>
                    <th class="table__header">Rol</th>
                  </tr>
                </thead>
                <tbody class="table__body">
                  {#each usuarios as usuario}
                    <tr class="table__row">
                      <td class="table__cell" data-label="ID">{usuario.id}</td>
                      <td class="table__cell" data-label="Nombre">{usuario.nombre}</td>
                      <td class="table__cell" data-label="Correo">{usuario.correo}</td>
                      <td class="table__cell" data-label="Username">{usuario.username}</td>
                      <td class="table__cell" data-label="Rol">
                        <select class="rol-select" value={usuario.rolId}
                          on:change={(e) => handleCambiarRol(usuario.id, e.target.value)}>
                          {#each rolesDisponibles as rol}
                            <option value={rol.id}>{rol.nombre}</option>
                          {/each}
                        </select>
                      </td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            {/if}
          </section>

        {:else if activeSection === 'metricas'}
          <!-- ========= MÉTRICAS DASHBOARD ========= -->
          <section class="admin-section met-section">
            <div class="met-header">
              <div>
                <h2 class="admin-section__title">Analiticos y Reportes</h2>
                <p class="admin-section__subtitle">Registro y visualizacion de busquedas del sistema</p>
              </div>
            </div>

            <!-- ── Filtros ───────────────────────────────────────────── -->
            <div class="met-filtros">
              <div class="met-filtro-grupo">
                <label class="met-label">Desde</label>
                <input type="date" class="met-input" bind:value={metFechaDesde} />
              </div>
              <div class="met-filtro-grupo">
                <label class="met-label">Hasta</label>
                <input type="date" class="met-input" bind:value={metFechaHasta} />
              </div>
              <div class="met-filtro-grupo">
                <label class="met-label">Tipo</label>
                <select class="met-input" bind:value={metTipo}>
                  <option value="">Todos</option>
                  <option value="Web">Web</option>
                  <option value="REST">REST</option>
                </select>
              </div>
              <div class="met-filtro-grupo">
                <label class="met-label">Usuario</label>
                <input type="text" class="met-input" placeholder="username..." bind:value={metUsuario} />
              </div>
              <button class="met-btn-aplicar" on:click={aplicarFiltrosMetricas} disabled={loadingMetricas}>
                {loadingMetricas ? 'Cargando...' : 'Aplicar filtros'}
              </button>
            </div>

            {#if loadingMetricas}
              <div class="met-loading">
                <div class="met-spinner"></div>
                <p>Cargando analiticos...</p>
              </div>
            {:else if metricasResumen}

              <!-- ── KPI Cards ───────────────────────────────────────── -->
              <div class="met-kpis">
                <div class="met-kpi">
                  <span class="met-kpi__icon">🔍</span>
                  <span class="met-kpi__value">{metricasResumen.totalBusquedas.toLocaleString()}</span>
                  <span class="met-kpi__label">Total Busquedas</span>
                </div>
                <div class="met-kpi met-kpi--gold">
                  <span class="met-kpi__icon">🌐</span>
                  <span class="met-kpi__value">{metricasResumen.totalBusquedasWeb.toLocaleString()}</span>
                  <span class="met-kpi__label">Busquedas Web</span>
                </div>
                <div class="met-kpi met-kpi--dark">
                  <span class="met-kpi__icon">⚡</span>
                  <span class="met-kpi__value">{metricasResumen.totalBusquedasRest.toLocaleString()}</span>
                  <span class="met-kpi__label">Busquedas REST</span>
                </div>
                <div class="met-kpi">
                  <span class="met-kpi__icon">📅</span>
                  <span class="met-kpi__value">{metricasResumen.busquedasPorDia.length}</span>
                  <span class="met-kpi__label">Dias con actividad</span>
                </div>
              </div>

              <!-- ── Gráficas ────────────────────────────────────────── -->
              <div class="met-graficas">

                <!-- Gráfica 1: Línea temporal de búsquedas por día -->
                <div class="met-grafica met-grafica--wide">
                  <h3 class="met-grafica__titulo">Busquedas por dia</h3>
                  <p class="met-grafica__subtitulo">Volumen diario de busquedas en el periodo seleccionado</p>
                  {#if metricasResumen.busquedasPorDia.length === 0}
                    <div class="met-empty">Sin datos en el periodo seleccionado</div>
                  {:else}
                    {@const datos = metricasResumen.busquedasPorDia}
                    {@const maxVal = Math.max(...datos.map(d => d.total), 1)}
                    {@const W = 700} {@const H = 200}
                    <div class="met-svg-wrap">
                      <svg viewBox="0 0 {W} {H}" class="met-svg" preserveAspectRatio="xMidYMid meet">
                        <!-- Grid lines -->
                        {#each [0.25, 0.5, 0.75, 1] as pct}
                          <line x1="20" y1={H - 20 - pct * (H - 40)} x2={W - 10} y2={H - 20 - pct * (H - 40)}
                            stroke="#EBE6E0" stroke-width="1" />
                          <text x="14" y={H - 20 - pct * (H - 40) + 4} font-size="9" fill="#b8b0a5" text-anchor="end">
                            {Math.round(maxVal * pct)}
                          </text>
                        {/each}
                        <!-- Área rellena -->
                        <defs>
                          <linearGradient id="gradLinea" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stop-color="#D4AF37" stop-opacity="0.3"/>
                            <stop offset="100%" stop-color="#D4AF37" stop-opacity="0"/>
                          </linearGradient>
                        </defs>
                        {#if datos.length > 1}
                          {@const pts = datos.map((d,i) => {
                            const x = (i/(datos.length-1))*(W-40)+20;
                            const y = H-20-((d.total/maxVal)*(H-40));
                            return `${x},${y}`;
                          })}
                          <polygon
                            points={`20,${H-20} ${pts.join(' ')} ${(W-20)},${H-20}`}
                            fill="url(#gradLinea)" />
                          <polyline points={pts.join(' ')}
                            fill="none" stroke="#D4AF37" stroke-width="2.5"
                            stroke-linejoin="round" stroke-linecap="round" />
                          <!-- Puntos interactivos -->
                          {#each datos as d, i}
                            {@const x = (i/(datos.length-1))*(W-40)+20}
                            {@const y = H-20-((d.total/maxVal)*(H-40))}
                            <circle cx={x} cy={y} r="4" fill="#D4AF37" stroke="white" stroke-width="2">
                              <title>{d.fecha}: {d.total} busquedas</title>
                            </circle>
                          {/each}
                        {:else}
                          <!-- Solo 1 punto -->
                          <circle cx={W/2} cy={H/2} r="6" fill="#D4AF37" stroke="white" stroke-width="2" />
                        {/if}
                        <!-- Eje X: etiquetas de fechas (cada ~5) -->
                        {#each datos as d, i}
                          {#if i % Math.ceil(datos.length / 6) === 0 || i === datos.length - 1}
                            {@const x = datos.length > 1 ? (i/(datos.length-1))*(W-40)+20 : W/2}
                            <text x={x} y={H - 4} font-size="8" fill="#b8b0a5" text-anchor="middle">
                              {d.fecha.slice(5)}
                            </text>
                          {/if}
                        {/each}
                      </svg>
                    </div>
                    <div class="met-grafica__leyenda">
                      <span class="met-leyenda-dot" style="background:#D4AF37"></span>
                      Busquedas totales por dia
                    </div>
                  {/if}
                </div>

                <!-- Gráfica 2: Rutas más buscadas (barras horizontales) -->
                <div class="met-grafica">
                  <h3 class="met-grafica__titulo">Rutas mas buscadas</h3>
                  <p class="met-grafica__subtitulo">Top 10 rutas con mayor demanda</p>
                  {#if metricasResumen.rutasMasBuscadas.length === 0}
                    <div class="met-empty">Sin datos en el periodo seleccionado</div>
                  {:else}
                    {@const maxRuta = Math.max(...metricasResumen.rutasMasBuscadas.map(r => r.total), 1)}
                    <div class="met-barras">
                      {#each metricasResumen.rutasMasBuscadas as ruta, i}
                        <div class="met-barra-row">
                          <div class="met-barra-label">
                            <span class="met-barra-rank">#{i+1}</span>
                            <span class="met-barra-ruta">{ruta.origenCodigo} → {ruta.destinoCodigo}</span>
                          </div>
                          <div class="met-barra-track">
                            <div class="met-barra-fill"
                              style="width:{(ruta.total/maxRuta)*100}%;
                                     background: {i < 3 ? '#D4AF37' : i < 6 ? '#C9A961' : '#b8a080'}">
                            </div>
                          </div>
                          <span class="met-barra-val">{ruta.total}</span>
                        </div>
                      {/each}
                    </div>
                  {/if}
                </div>

                <!-- Gráfica 3: Donut Web vs REST -->
                <div class="met-grafica met-grafica--donut">
                  <h3 class="met-grafica__titulo">Canal de busqueda</h3>
                  <p class="met-grafica__subtitulo">Proporcion Web vs REST</p>
                  {#if metricasResumen.busquedasPorTipo.length === 0}
                    <div class="met-empty">Sin datos en el periodo seleccionado</div>
                  {:else}
                    {@const segmentos = calcularDonut(metricasResumen.busquedasPorTipo)}
                    <div class="met-donut-wrap">
                      <svg viewBox="0 0 200 200" class="met-donut-svg">
                        {#each segmentos as seg}
                          <path
                            d={donutPath(seg.start, seg.start + seg.angle)}
                            fill={seg.color}
                            stroke="white"
                            stroke-width="2">
                            <title>{seg.tipo}: {seg.total} ({seg.porcentaje}%)</title>
                          </path>
                        {/each}
                        <!-- Texto central -->
                        <text x="100" y="97" text-anchor="middle" font-size="22" font-weight="700" fill="#1C1A18">
                          {metricasResumen.totalBusquedas}
                        </text>
                        <text x="100" y="113" text-anchor="middle" font-size="9" fill="#b8b0a5">TOTAL</text>
                      </svg>
                      <div class="met-donut-leyenda">
                        {#each segmentos as seg}
                          <div class="met-ley-item">
                            <span class="met-leyenda-dot" style="background:{seg.color}"></span>
                            <span class="met-ley-tipo">{seg.tipo}</span>
                            <span class="met-ley-num">{seg.total} <em>({seg.porcentaje}%)</em></span>
                          </div>
                        {/each}
                      </div>
                    </div>
                  {/if}
                </div>

              </div>
              <!-- /Gráficas -->
            {/if}

            <!-- ── Listado de datos ───────────────────────────────────── -->
            <div class="met-listado">
              <div class="met-listado__header">
                <h3 class="met-listado__titulo">Registro de busquedas</h3>
                <button class="met-btn-exportar" on:click={() => mostrarModalExportar = true}>
                  Exportar por correo
                </button>
              </div>

              {#if loadingListado}
                <div class="met-loading met-loading--sm">
                  <div class="met-spinner"></div>
                  <p>Cargando listado...</p>
                </div>
              {:else if metricasListado}
                <div class="met-tabla-wrap">
                  <table class="table">
                    <thead class="table__head">
                      <tr>
                        <th class="table__header">#</th>
                        <th class="table__header">Ruta</th>
                        <th class="table__header">Fecha Salida</th>
                        <th class="table__header">Pasajeros</th>
                        <th class="table__header">Usuario</th>
                        <th class="table__header">Tipo</th>
                        <th class="table__header">Fecha Busqueda</th>
                      </tr>
                    </thead>
                    <tbody>
                      {#each metricasListado.registros as r}
                        <tr class="table__row">
                          <td class="table__cell">{r.id}</td>
                          <td class="table__cell">
                            <span class="met-ruta-badge">{r.origenCodigo}</span>
                            <span class="met-ruta-arrow">→</span>
                            <span class="met-ruta-badge">{r.destinoCodigo}</span>
                          </td>
                          <td class="table__cell">{r.fechaSalida}</td>
                          <td class="table__cell">{r.cantidadPersonas}</td>
                          <td class="table__cell">{r.usuario ?? '— anon —'}</td>
                          <td class="table__cell">
                            <span class="status-badge status-badge--{r.tipo === 'Web' ? 'activo' : 'cancelado'}">
                              {r.tipo}
                            </span>
                          </td>
                          <td class="table__cell">{r.fechaBusqueda}</td>
                        </tr>
                      {/each}
                    </tbody>
                  </table>
                </div>

                <!-- Paginado -->
                {#if metricasListado.totalPaginas > 1}
                  <div class="met-paginado">
                    <button class="met-pag-btn" disabled={metricasListado.paginaActual <= 1}
                      on:click={() => cargarListadoBusquedas(metricasListado.paginaActual - 1)}>
                      ← Anterior
                    </button>
                    <span class="met-pag-info">
                      Pagina {metricasListado.paginaActual} de {metricasListado.totalPaginas}
                      &nbsp;·&nbsp; {metricasListado.totalRegistros} registros
                    </span>
                    <button class="met-pag-btn"
                      disabled={metricasListado.paginaActual >= metricasListado.totalPaginas}
                      on:click={() => cargarListadoBusquedas(metricasListado.paginaActual + 1)}>
                      Siguiente →
                    </button>
                  </div>
                {:else}
                  <p class="met-pag-info" style="text-align:right;padding:1rem 0">
                    {metricasListado.totalRegistros} registros encontrados
                  </p>
                {/if}
              {:else}
                <div class="met-empty">Aplica filtros para ver el listado</div>
              {/if}
            </div>

          </section>

          <!-- Modal exportar por correo -->
          {#if mostrarModalExportar}
            <div class="modal-overlay" on:click={() => mostrarModalExportar = false}>
              <div class="modal" on:click|stopPropagation style="max-width:420px">
                <div class="modal__header">
                  <h3 class="modal__title">Exportar listado</h3>
                  <button class="modal__close" on:click={() => mostrarModalExportar = false}>×</button>
                </div>
                <div style="padding:1.5rem;display:flex;flex-direction:column;gap:1rem">
                  <p style="color:var(--text-muted);font-size:0.9rem">
                    El listado filtrado actual se enviara como archivo adjunto al correo indicado.
                  </p>
                  <label class="met-label">Correo electronico</label>
                  <input type="email" class="met-input" placeholder="correo@ejemplo.com"
                    bind:value={correoExportar} />
                  <div style="display:flex;gap:1rem;justify-content:flex-end;margin-top:0.5rem">
                    <button class="btn-secondary" on:click={() => mostrarModalExportar = false}>Cancelar</button>
                    <button class="btn-primary" disabled={enviandoCorreo || !correoExportar}
                      on:click={async () => {
                        enviandoCorreo = true;
                        try {
                          const r = await fetch(`${API}/api/metricas/exportar-correo`, {
                            method: 'POST',
                            credentials: 'include',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                              correo: correoExportar,
                              fechaDesde: metFechaDesde,
                              fechaHasta: metFechaHasta,
                              tipo: metTipo || null,
                              usuario: metUsuario || null
                            })
                          });
                          if (r.ok) {
                            mostrarToast('success', `Listado enviado a ${correoExportar}`);
                            mostrarModalExportar = false;
                            correoExportar = '';
                          } else {
                            mostrarToast('error', 'No se pudo enviar el correo');
                          }
                        } catch { mostrarToast('error', 'Error de conexion'); }
                        finally { enviandoCorreo = false; }
                      }}>
                      {enviandoCorreo ? 'Enviando...' : 'Enviar'}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          {/if}
        {/if}

      </main>
    </div>
  </div>
</div>

<!-- ===== MODAL AVIÓN ===== -->
{#if mostrarFormularioAvion}
  <div class="modal-overlay" on:click={cerrarFormularioAvion}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Avion</h3>
        <button class="modal__close" on:click={cerrarFormularioAvion}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardarAvion}>
        <div class="form-field">
          <label for="avion-marca" class="form-label">Marca *</label>
          <input type="text" id="avion-marca" class="form-input"
            bind:value={avionForm.marca} placeholder="Ej: Boeing" required />
        </div>
        <div class="form-field">
          <label for="avion-modelo" class="form-label">Modelo *</label>
          <input type="text" id="avion-modelo" class="form-input"
            bind:value={avionForm.modelo} placeholder="Ej: 787-9" required />
        </div>
        <div class="form-field">
          <label for="avion-capacidad" class="form-label">Capacidad de Pasajeros *</label>
          <input type="number" id="avion-capacidad" class="form-input"
            bind:value={avionForm.capacidadPasajeros} placeholder="Ej: 240" min="1" required />
        </div>
        <div class="form-field">
          <label class="form-label">Imagen del Avion</label>
          {#if avionImagenPreview}
            <img src={avionImagenPreview} alt="Preview" class="img-preview" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { avionImagenPreview = null; avionImagenBase64 = null; }}>Quitar imagen</button>
          {/if}
          <input type="file" accept="image/*" class="form-input" on:change={onAvionImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Max recomendado: 1 MB.</small>
        </div>
        <div class="modal__actions">
          <button type="submit" class="btn-primary">{modoEdicion ? 'Actualizar' : 'Crear'} Avion</button>
          <button type="button" class="btn-secondary" on:click={cerrarFormularioAvion}>Cancelar</button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- ===== MODAL TRIPULANTE ===== -->
{#if mostrarFormularioTripulante}
  <div class="modal-overlay" on:click={cerrarFormularioTripulante}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Tripulante</h3>
        <button class="modal__close" on:click={cerrarFormularioTripulante}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardarTripulante}>
        <div class="form-field">
          <label for="trip-nombre" class="form-label">Nombre *</label>
          <input type="text" id="trip-nombre" class="form-input"
            bind:value={tripulanteForm.nombre} placeholder="Ej: Juan" required />
        </div>
        <div class="form-field">
          <label for="trip-apellido" class="form-label">Apellido *</label>
          <input type="text" id="trip-apellido" class="form-input"
            bind:value={tripulanteForm.apellido} placeholder="Ej: Perez" required />
        </div>
        <div class="form-field">
          <label for="trip-rol" class="form-label">Rol *</label>
          <select id="trip-rol" class="form-input" bind:value={tripulanteForm.rolID} required>
            <option value="">Selecciona un rol</option>
            {#each rolesTripulacion as rol}
              <option value={rol.id}>{rol.nombre}</option>
            {/each}
          </select>
        </div>
        <div class="form-field">
          <label class="form-label">Foto del Tripulante</label>
          {#if tripulanteImagenPreview}
            <img src={tripulanteImagenPreview} alt="Preview" class="img-preview img-preview--circle" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { tripulanteImagenPreview = null; tripulanteImagenBase64 = null; }}>Quitar foto</button>
          {/if}
          <input type="file" accept="image/*" class="form-input" on:change={onTripulanteImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Max recomendado: 1 MB.</small>
        </div>
        <div class="modal__actions">
          <button type="submit" class="btn-primary">{modoEdicion ? 'Actualizar' : 'Crear'} Tripulante</button>
          <button type="button" class="btn-secondary" on:click={cerrarFormularioTripulante}>Cancelar</button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- ===== MODAL AEROPUERTO ===== -->
{#if mostrarFormularioAeropuerto}
  <div class="modal-overlay" on:click={cerrarFormularioAeropuerto}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Aeropuerto</h3>
        <button class="modal__close" on:click={cerrarFormularioAeropuerto}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardarAeropuerto}>
        <div class="form-field">
          <label for="aero-codigo" class="form-label">Codigo IATA *</label>
          <input type="text" id="aero-codigo" class="form-input"
            bind:value={aeropuertoForm.codigo} placeholder="Ej: GUA" maxlength="3"
            style="text-transform:uppercase" required />
        </div>
        <div class="form-field">
          <label for="aero-nombre" class="form-label">Nombre del Aeropuerto *</label>
          <input type="text" id="aero-nombre" class="form-input"
            bind:value={aeropuertoForm.nombre} placeholder="Ej: La Aurora" required />
        </div>
        <div class="form-field">
          <label for="aero-pais" class="form-label">Pais *</label>
          <div class="autocomplete">
            <input type="text" id="aero-pais" class="form-input"
              bind:value={paisQueryAeropuerto} on:input={onPaisAeropuertoInput}
              on:blur={validarPaisAeropuertoSeleccionado}
              placeholder="Escribe el pais..." autocomplete="off" required />
            {#if paisesSugeridosAeropuerto.length > 0}
              <ul class="autocomplete__list">
                {#each paisesSugeridosAeropuerto as p}
                  <li class="autocomplete__item">
                    <button type="button" class="autocomplete__btn"
                      on:click={() => seleccionarPaisAeropuerto(p)}>{p.country}</button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>
        </div>
        <div class="form-field">
          <label for="aero-ciudad" class="form-label">Ciudad *</label>
          <div class="autocomplete">
            <input type="text" id="aero-ciudad" class="form-input"
              bind:value={ciudadQueryAeropuerto} on:input={onCiudadAeropuertoInput}
              on:blur={validarCiudadAeropuertoSeleccionada}
              placeholder={paisSeleccionadoAeropuerto ? 'Escribe la ciudad...' : 'Primero selecciona un pais'}
              disabled={!paisSeleccionadoAeropuerto} autocomplete="off" required />
            {#if ciudadesSugeridasAeropuerto.length > 0}
              <ul class="autocomplete__list">
                {#each ciudadesSugeridasAeropuerto as c}
                  <li class="autocomplete__item">
                    <button type="button" class="autocomplete__btn"
                      on:click={() => seleccionarCiudadAeropuerto(c)}>{c}</button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>
        </div>
        <div class="form-field">
          <label class="form-label">Imagen del Aeropuerto</label>
          {#if aeropuertoImagenPreview}
            <img src={aeropuertoImagenPreview} alt="Preview" class="img-preview" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null; }}>Quitar imagen</button>
          {/if}
          <input type="file" accept="image/*" class="form-input" on:change={onAeropuertoImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Max recomendado: 1 MB.</small>
        </div>
        <div class="modal__actions">
          <button type="submit" class="btn-primary">{modoEdicion ? 'Actualizar' : 'Crear'} Aeropuerto</button>
          <button type="button" class="btn-secondary" on:click={cerrarFormularioAeropuerto}>Cancelar</button>
        </div>
      </form>
    </div>
  </div>
{/if}