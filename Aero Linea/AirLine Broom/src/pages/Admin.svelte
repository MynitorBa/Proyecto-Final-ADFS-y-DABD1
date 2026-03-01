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
  let usuarios = [];
  let aviones = [];
  let tripulantes = [];
  let aeropuertos = [];
  let historialVuelos = [];
  let rolesTripulacion = [];
  let todosLosPaises = [];

  // ========= LOADING =========
  let loadingUsuarios = false;
  let loadingAviones = false;
  let loadingTripulantes = false;
  let loadingAeropuertos = false;
  let loadingHistorialVuelos = false;

  // ========= BÚSQUEDA VUELO =========
  let busquedaOrigen = '';
  let busquedaDestino = '';
  let busquedaAvion = '';
  let busquedaTripulante = '';
  let mostrarDropdownOrigen = false;
  let mostrarDropdownDestino = false;
  let mostrarDropdownAvion = false;
  let mostrarDropdownTripulante = false;

  // ========= MODALES =========
  let mostrarFormularioAvion = false;
  let mostrarFormularioTripulante = false;
  let mostrarFormularioAeropuerto = false;
  let modoEdicion = false;

  // ========= ROLES =========
  let rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' }
  ];

  // ========= AUTOCOMPLETE AEROPUERTO =========
  let paisQueryAeropuerto = '';
  let paisesSugeridosAeropuerto = [];
  let paisSeleccionadoAeropuerto = null;
  let ciudadQueryAeropuerto = '';
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
    boletosTurista: '',
    boletosEjecutivo: '',
    precioTurista: '',
    precioEjecutiva: '',
    tripulantesSeleccionados: []
  };

  // ========= FORMULARIO AVIÓN =========
  let avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
  let avionImagenPreview = null;
  let avionImagenBase64 = null;

  // ========= FORMULARIO TRIPULANTE =========
  let tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
  let tripulanteImagenPreview = null;
  let tripulanteImagenBase64 = null;

  // ========= FORMULARIO AEROPUERTO =========
  let aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '' };
  let aeropuertoImagenPreview = null;
  let aeropuertoImagenBase64 = null;

  // ========= LIFECYCLE =========
  onMount(async () => {
    if (rolNombre !== 'Administrador') {
      navigateTo('acceso-denegado');
      return;
    }
    await cargarDatosIniciales();
  });

  onDestroy(() => {
    unsubscribeSesion();
  });

  async function cargarDatosIniciales() {
    await Promise.all([
      cargarUsuarios(),
      cargarAviones(),
      cargarTripulantes(),
      cargarAeropuertos(),
      cargarRolesTripulacion(),
      cargarPaises(),
      cargarHistorialVuelos()
    ]);
  }

  // ========= CARGA DE DATOS =========
  async function cargarUsuarios() {
    loadingUsuarios = true;
    try {
      const r = await fetch(`${API}/api/usuarios`, { credentials: 'include' });
      if (r.ok) usuarios = await r.json();
    } catch (e) { console.error('Error cargando usuarios:', e); }
    finally { loadingUsuarios = false; }
  }

  async function cargarAviones() {
    loadingAviones = true;
    try {
      const r = await fetch(`${API}/api/aviones`);
      if (r.ok) aviones = await r.json();
    } catch (e) { console.error('Error cargando aviones:', e); }
    finally { loadingAviones = false; }
  }

  async function cargarTripulantes() {
    loadingTripulantes = true;
    try {
      const r = await fetch(`${API}/api/tripulacion`);
      if (r.ok) tripulantes = await r.json();
    } catch (e) { console.error('Error cargando tripulantes:', e); }
    finally { loadingTripulantes = false; }
  }

  async function cargarAeropuertos() {
    loadingAeropuertos = true;
    try {
      const r = await fetch(`${API}/api/aeropuertos`);
      if (r.ok) aeropuertos = await r.json();
    } catch (e) { console.error('Error cargando aeropuertos:', e); }
    finally { loadingAeropuertos = false; }
  }

  async function cargarRolesTripulacion() {
    try {
      const r = await fetch(`${API}/api/tripulacion/roles`);
      if (r.ok) {
        const roles = await r.json();
        rolesTripulacion = roles.map(rol => ({ id: rol.id, nombre: rol.cargo }));
      }
    } catch (e) { console.error('Error cargando roles:', e); }
  }

  async function cargarPaises() {
    try {
      const r = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await r.json();
      todosLosPaises = data.data;
    } catch (e) { console.error('Error cargando países:', e); }
  }

  async function cargarHistorialVuelos() {
    loadingHistorialVuelos = true;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/historial`, { credentials: 'include' });
      if (r.ok) historialVuelos = await r.json();
    } catch (e) { console.error('Error cargando historial:', e); }
    finally { loadingHistorialVuelos = false; }
  }

  // ========= IMAGEN HELPER =========
  function leerImagenComoBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  function onAvionImagenChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    leerImagenComoBase64(file).then(b64 => {
      avionImagenBase64 = b64;
      avionImagenPreview = b64;
    });
  }

  function onTripulanteImagenChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    leerImagenComoBase64(file).then(b64 => {
      tripulanteImagenBase64 = b64;
      tripulanteImagenPreview = b64;
    });
  }

  function onAeropuertoImagenChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    leerImagenComoBase64(file).then(b64 => {
      aeropuertoImagenBase64 = b64;
      aeropuertoImagenPreview = b64;
    });
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
    ciudadQueryAeropuerto = '';
    aeropuertoForm.ciudad = '';
    ciudadesSugeridasAeropuerto = [];
    ciudadSeleccionadaAeropuerto = false;
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
    ciudadQueryAeropuerto = c;
    aeropuertoForm.ciudad = c;
    ciudadesSugeridasAeropuerto = [];
    ciudadSeleccionadaAeropuerto = true;
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

  // Cuando cambia el avión, sugerir distribución 75/25 automáticamente
  $: if (avionSeleccionado && !nuevoVuelo.boletosTurista && !nuevoVuelo.boletosEjecutivo) {
    const cap = avionSeleccionado.capacidadPasajeros;
    const eje = Math.floor(cap * 0.25);
    nuevoVuelo.boletosEjecutivo = eje;
    nuevoVuelo.boletosTurista   = cap - eje;
  }

  $: totalBoletosAsignados = (parseInt(nuevoVuelo.boletosTurista) || 0) +
                             (parseInt(nuevoVuelo.boletosEjecutivo) || 0);
  $: capacidadAvion = avionSeleccionado?.capacidadPasajeros ?? 0;
  $: excedeLimite = capacidadAvion > 0 && totalBoletosAsignados > capacidadAvion;

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
    // Limpiar para que el reactivo sugiera distribución según nueva capacidad
    nuevoVuelo.boletosTurista = '';
    nuevoVuelo.boletosEjecutivo = '';
  }

  function agregarTripulante(t) {
    nuevoVuelo.tripulantesSeleccionados = [...nuevoVuelo.tripulantesSeleccionados, t];
    busquedaTripulante = '';
    mostrarDropdownTripulante = false;
  }

  function quitarTripulante(id) {
    nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados.filter(t => t.id !== id);
  }

  function limpiarFormularioVuelo() {
    nuevoVuelo = {
      numeroVuelo: '', aeropuertoOrigenId: '', aeropuertoDestinoId: '',
      avionId: '', fecha: '', horaSalida: '', horaLlegada: '',
      boletosTurista: '', boletosEjecutivo: '',
      precioTurista: '', precioEjecutiva: '', tripulantesSeleccionados: []
    };
    busquedaOrigen = ''; busquedaDestino = ''; busquedaAvion = ''; busquedaTripulante = '';
  }

  async function handleCrearVuelo() {
    if (!nuevoVuelo.numeroVuelo)         { alert('Por favor ingresa el número de vuelo'); return; }
    if (!nuevoVuelo.aeropuertoOrigenId)  { alert('Por favor selecciona el aeropuerto de origen'); return; }
    if (!nuevoVuelo.aeropuertoDestinoId) { alert('Por favor selecciona el aeropuerto de destino'); return; }
    if (!nuevoVuelo.avionId)             { alert('Por favor selecciona un avión'); return; }
    if (!nuevoVuelo.fecha)               { alert('Por favor selecciona la fecha del vuelo'); return; }
    if (!nuevoVuelo.horaSalida || !nuevoVuelo.horaLlegada) { alert('Por favor ingresa las horas'); return; }
    if (!nuevoVuelo.boletosTurista || parseInt(nuevoVuelo.boletosTurista) < 0) { alert('Por favor ingresa los boletos de clase turista'); return; }
    if (!nuevoVuelo.boletosEjecutivo || parseInt(nuevoVuelo.boletosEjecutivo) < 0) { alert('Por favor ingresa los boletos de clase ejecutiva'); return; }
    if (excedeLimite) { alert(`La suma de boletos (${totalBoletosAsignados}) excede la capacidad del avión (${capacidadAvion})`); return; }
    if (!nuevoVuelo.precioTurista || !nuevoVuelo.precioEjecutiva) { alert('Por favor ingresa los precios'); return; }

    try {
      const datos = {
        numeroVuelo: nuevoVuelo.numeroVuelo,
        aeropuertoOrigenId:  parseInt(nuevoVuelo.aeropuertoOrigenId),
        aeropuertoDestinoId: parseInt(nuevoVuelo.aeropuertoDestinoId),
        avionId:      parseInt(nuevoVuelo.avionId),
        fecha:        nuevoVuelo.fecha,
        horaSalida:   nuevoVuelo.horaSalida,
        horaLlegada:  nuevoVuelo.horaLlegada,
        boletosTurista:   parseInt(nuevoVuelo.boletosTurista),
        boletosEjecutivo: parseInt(nuevoVuelo.boletosEjecutivo),
        precioTurista:   parseFloat(nuevoVuelo.precioTurista),
        precioEjecutiva: parseFloat(nuevoVuelo.precioEjecutiva),
        tripulantesIds: nuevoVuelo.tripulantesSeleccionados.map(t => t.id)
      };

      const r = await fetch(`${API}/api/admin/vuelos`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
      });

      if (r.ok) {
        alert('¡Vuelo creado exitosamente!');
        limpiarFormularioVuelo();
        await cargarHistorialVuelos();
        activeSection = 'historial';
      } else {
        const err = await r.json();
        alert(err.message || 'Error al crear el vuelo');
      }
    } catch (e) {
      alert('Error de conexión al crear el vuelo');
    }
  }

  async function handleCambiarRol(userId, nuevoRolId) {
    try {
      const r = await fetch(`${API}/api/usuarios/cambiar-rol`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioId: parseInt(userId), nuevoRolId: parseInt(nuevoRolId) })
      });
      if (!r.ok) {
        const err = await r.json();
        alert(err.message || 'Error al cambiar el rol');
      }
    } catch (e) {
      alert('Error de conexión al cambiar el rol');
    }
  }

  async function handleCambiarEstadoVuelo(vueloId) {
    if (!confirm('¿Estás seguro de que deseas cancelar este vuelo?')) return;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/${vueloId}/cancelar`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      });
      if (r.ok) {
        alert('Vuelo cancelado exitosamente');
        await cargarHistorialVuelos();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al cancelar el vuelo');
      }
    } catch (e) {
      alert('Error de conexión al cancelar el vuelo');
    }
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
    avionImagenBase64 = null;
    avionImagenPreview = avion.imagenBase64 || null;
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
        marca: avionForm.marca,
        modelo: avionForm.modelo,
        capacidadPasajeros: parseInt(avionForm.capacidadPasajeros),
        imagenBase64: avionImagenBase64 || null
      };

      const url    = modoEdicion ? `${API}/api/aviones/${avionForm.id}` : `${API}/api/aviones`;
      const method = modoEdicion ? 'PUT' : 'POST';

      const r = await fetch(url, {
        method,
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (r.ok) {
        alert(modoEdicion ? 'Avión actualizado correctamente' : 'Avión creado correctamente');
        await cargarAviones();
        cerrarFormularioAvion();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al guardar el avión');
      }
    } catch (e) {
      alert('Error de conexión al guardar el avión');
    }
  }

  async function handleEliminarAvion(avionId) {
    if (!confirm('¿Estás seguro de que deseas eliminar este avión?')) return;
    try {
      const r = await fetch(`${API}/api/aviones/${avionId}`, {
        method: 'DELETE', credentials: 'include'
      });
      if (r.ok) {
        alert('Avión eliminado correctamente');
        await cargarAviones();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al eliminar el avión');
      }
    } catch (e) {
      alert('Error de conexión al eliminar el avión');
    }
  }

  async function handleEliminarImagenAvion(avionId) {
    if (!confirm('¿Eliminar la imagen de este avión?')) return;
    try {
      const r = await fetch(`${API}/api/aviones/${avionId}/imagen`, {
        method: 'DELETE', credentials: 'include'
      });
      if (r.ok) await cargarAviones();
      else alert('Error al eliminar la imagen');
    } catch (e) { alert('Error de conexión'); }
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
    tripulanteImagenBase64 = null;
    tripulanteImagenPreview = t.imagenBase64 || null;
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
        nombre: tripulanteForm.nombre,
        apellido: tripulanteForm.apellido,
        rolID: parseInt(tripulanteForm.rolID),
        imagenBase64: tripulanteImagenBase64 || null
      };

      const url    = modoEdicion ? `${API}/api/tripulacion/${tripulanteForm.id}` : `${API}/api/tripulacion`;
      const method = modoEdicion ? 'PUT' : 'POST';

      const r = await fetch(url, {
        method,
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (r.ok) {
        alert(modoEdicion ? 'Tripulante actualizado correctamente' : 'Tripulante creado correctamente');
        await cargarTripulantes();
        cerrarFormularioTripulante();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al guardar el tripulante');
      }
    } catch (e) {
      alert('Error de conexión al guardar el tripulante');
    }
  }

  async function handleEliminarTripulante(tripulanteId) {
    if (!confirm('¿Estás seguro de que deseas eliminar este tripulante?')) return;
    try {
      const r = await fetch(`${API}/api/tripulacion/${tripulanteId}`, {
        method: 'DELETE', credentials: 'include'
      });
      if (r.ok) {
        alert('Tripulante eliminado correctamente');
        await cargarTripulantes();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al eliminar el tripulante');
      }
    } catch (e) {
      alert('Error de conexión al eliminar el tripulante');
    }
  }

  async function handleEliminarImagenTripulante(tripulanteId) {
    if (!confirm('¿Eliminar la foto de este tripulante?')) return;
    try {
      const r = await fetch(`${API}/api/tripulacion/${tripulanteId}/imagen`, {
        method: 'DELETE', credentials: 'include'
      });
      if (r.ok) await cargarTripulantes();
      else alert('Error al eliminar la foto');
    } catch (e) { alert('Error de conexión'); }
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
        aeropuertoForm = {
          id: completo.id, codigo: completo.codigo, nombre: completo.nombre,
          ciudad: completo.ciudad, pais: completo.pais
        };
        paisQueryAeropuerto = completo.pais;
        ciudadQueryAeropuerto = completo.ciudad;
        const paisEncontrado = todosLosPaises.find(p =>
          p.country.toLowerCase() === completo.pais.toLowerCase());
        if (paisEncontrado) paisSeleccionadoAeropuerto = paisEncontrado;
        ciudadSeleccionadaAeropuerto = true;
        aeropuertoImagenBase64 = null;
        aeropuertoImagenPreview = completo.imagenBase64 || null;
        mostrarFormularioAeropuerto = true;
      }
    } catch (e) {
      alert('Error al cargar los datos del aeropuerto');
    }
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
      alert('Debes seleccionar un país de la lista'); return;
    }
    if (!ciudadSeleccionadaAeropuerto || !aeropuertoForm.ciudad) {
      alert('Debes seleccionar una ciudad de la lista'); return;
    }

    try {
      const payload = {
        nombre: aeropuertoForm.nombre,
        codigo: aeropuertoForm.codigo.toUpperCase(),
        ciudad: aeropuertoForm.ciudad,
        pais: aeropuertoForm.pais,
        imagenBase64: aeropuertoImagenBase64 || null
      };

      const url    = modoEdicion ? `${API}/api/aeropuertos/${aeropuertoForm.id}` : `${API}/api/aeropuertos`;
      const method = modoEdicion ? 'PUT' : 'POST';

      const r = await fetch(url, {
        method,
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (r.ok) {
        alert(modoEdicion ? 'Aeropuerto actualizado correctamente' : 'Aeropuerto creado correctamente');
        await cargarAeropuertos();
        cerrarFormularioAeropuerto();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al guardar el aeropuerto');
      }
    } catch (e) {
      alert('Error de conexión al guardar el aeropuerto');
    }
  }

  async function handleEliminarAeropuerto(aeropuertoId) {
    if (!confirm('¿Estás seguro de que deseas eliminar este aeropuerto?')) return;
    try {
      const r = await fetch(`${API}/api/aeropuertos/${aeropuertoId}`, {
        method: 'DELETE', credentials: 'include'
      });
      if (r.ok) {
        alert('Aeropuerto eliminado correctamente');
        await cargarAeropuertos();
      } else {
        const err = await r.json();
        alert(err.message || 'Error al eliminar el aeropuerto');
      }
    } catch (e) {
      alert('Error de conexión al eliminar el aeropuerto');
    }
  }

  async function handleEliminarImagenAeropuerto(aeropuertoId) {
    if (!confirm('¿Eliminar la imagen de este aeropuerto?')) return;
    try {
      const r = await fetch(`${API}/api/aeropuertos/${aeropuertoId}/imagen`, {
        method: 'DELETE', credentials: 'include'
      });
      if (r.ok) await cargarAeropuertos();
      else alert('Error al eliminar la imagen');
    } catch (e) { alert('Error de conexión'); }
  }
</script>

<div class="admin">
  <div class="admin__container">

    <div class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>
        ← Salir del Panel
      </button>
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
            on:click={() => activeSection = 'metricas'}>Metricas</button>
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
                        <input type="text" class="admin-form__input"
                          bind:value={busquedaOrigen}
                          on:focus={() => mostrarDropdownOrigen = true}
                          on:blur={() => setTimeout(() => mostrarDropdownOrigen = false, 200)}
                          placeholder="Buscar aeropuerto..." autocomplete="off" />
                        {#if mostrarDropdownOrigen && aeropuertosFiltradosOrigen.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each aeropuertosFiltradosOrigen.slice(0, 10) as a}
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
                        <input type="text" class="admin-form__input"
                          bind:value={busquedaDestino}
                          on:focus={() => mostrarDropdownDestino = true}
                          on:blur={() => setTimeout(() => mostrarDropdownDestino = false, 200)}
                          placeholder="Buscar aeropuerto..." autocomplete="off" />
                        {#if mostrarDropdownDestino && aeropuertosFiltradosDestino.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each aeropuertosFiltradosDestino.slice(0, 10) as a}
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
              </div>

              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Aeronave</h3>
                <div class="admin-form__field admin-form__field--full">
                  <label class="admin-form__label">Seleccionar Avion *</label>
                  {#if loadingAviones}
                    <p class="loading-text">Cargando aviones...</p>
                  {:else}
                    <div class="searchable-select">
                      <input type="text" class="admin-form__input"
                        bind:value={busquedaAvion}
                        on:focus={() => mostrarDropdownAvion = true}
                        on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
                        placeholder="Buscar avion..." autocomplete="off" />
                      {#if mostrarDropdownAvion && avionesFiltrados.length > 0}
                        <div class="searchable-select__dropdown">
                          {#each avionesFiltrados.slice(0, 10) as a}
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
                <h3 class="admin-form__group-title">Distribución de Asientos y Precios</h3>

                {#if avionSeleccionado}
                  <div class="avion-info" style="margin-bottom: 1.5rem;">
                    <p class="avion-info__text">
                      <strong>Capacidad total del avión:</strong> {avionSeleccionado.capacidadPasajeros} pasajeros
                    </p>
                    <p class="avion-info__text">
                      <strong>Asignados:</strong> {totalBoletosAsignados} / {capacidadAvion}
                      {#if excedeLimite}
                        &nbsp;<span style="color:#ef4444;font-weight:700;">⚠ Excede la capacidad</span>
                      {:else if totalBoletosAsignados === capacidadAvion}
                        &nbsp;<span style="color:#16a34a;font-weight:700;">✔ Capacidad completa</span>
                      {/if}
                    </p>
                  </div>
                {/if}

                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="boletosTurista" class="admin-form__label">Boletos Clase Turista *</label>
                    <input type="number" id="boletosTurista" class="admin-form__input" min="0"
                      bind:value={nuevoVuelo.boletosTurista}
                      placeholder="Ej: 180"
                      max={capacidadAvion > 0 ? capacidadAvion : undefined}
                      required />
                  </div>
                  <div class="admin-form__field">
                    <label for="boletosEjecutivo" class="admin-form__label">Boletos Clase Ejecutiva *</label>
                    <input type="number" id="boletosEjecutivo" class="admin-form__input" min="0"
                      bind:value={nuevoVuelo.boletosEjecutivo}
                      placeholder="Ej: 60"
                      max={capacidadAvion > 0 ? capacidadAvion : undefined}
                      required />
                  </div>
                </div>

                <div class="admin-form__row" style="margin-top: 1.5rem;">
                  <div class="admin-form__field">
                    <label for="precioTurista" class="admin-form__label">Precio Turista (Q) *</label>
                    <input type="number" id="precioTurista" class="admin-form__input" min="0" step="0.01"
                      bind:value={nuevoVuelo.precioTurista} placeholder="Ej: 1500.00" required />
                  </div>
                  <div class="admin-form__field">
                    <label for="precioEjecutiva" class="admin-form__label">Precio Ejecutiva (Q) *</label>
                    <input type="number" id="precioEjecutiva" class="admin-form__input" min="0" step="0.01"
                      bind:value={nuevoVuelo.precioEjecutiva} placeholder="Ej: 3000.00" required />
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
                      <input type="text" class="admin-form__input"
                        bind:value={busquedaTripulante}
                        on:focus={() => mostrarDropdownTripulante = true}
                        on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
                        placeholder="Buscar por nombre o rol..." autocomplete="off" />
                      {#if mostrarDropdownTripulante && tripulantesFiltrados.length > 0}
                        <div class="searchable-select__dropdown">
                          {#each tripulantesFiltrados.slice(0, 10) as t}
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
                <button type="button" class="admin-form__cancel" on:click={limpiarFormularioVuelo}>
                  Limpiar
                </button>
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
              <div class="placeholder-card">
                <p class="placeholder-card__text">No hay aviones registrados.</p>
              </div>
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
                        {:else}
                          <span style="color:#9ca3af">—</span>
                        {/if}
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
              <div class="placeholder-card">
                <p class="placeholder-card__text">No hay tripulantes registrados.</p>
              </div>
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
                          <img src={t.imagenBase64} alt={t.nombreCompleto}
                            class="entity-thumb entity-thumb--circle" />
                        {:else}
                          <span style="color:#9ca3af">—</span>
                        {/if}
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
              <div class="placeholder-card">
                <p class="placeholder-card__text">No hay aeropuertos registrados.</p>
              </div>
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
                        {:else}
                          <span style="color:#9ca3af">—</span>
                        {/if}
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
              <div class="placeholder-card">
                <p class="placeholder-card__text">No hay vuelos registrados.</p>
              </div>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr>
                      <th class="table__header">No. Vuelo</th>
                      <th class="table__header">Origen</th>
                      <th class="table__header">Destino</th>
                      <th class="table__header">Fecha</th>
                      <th class="table__header">Salida</th>
                      <th class="table__header">Llegada</th>
                      <th class="table__header">Turista disp.</th>
                      <th class="table__header">Ejecutiva disp.</th>
                      <th class="table__header">Precio T.</th>
                      <th class="table__header">Precio E.</th>
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
                        <td class="table__cell" data-label="Fecha">{vuelo.fecha}</td>
                        <td class="table__cell" data-label="Salida">{vuelo.horaSalida}</td>
                        <td class="table__cell" data-label="Llegada">{vuelo.horaLlegada}</td>
                        <td class="table__cell" data-label="Turista disp.">{vuelo.boletosTurista}</td>
                        <td class="table__cell" data-label="Ejecutiva disp.">{vuelo.boletosEjecutivo}</td>
                        <td class="table__cell" data-label="Precio T.">Q{vuelo.precioTurista}</td>
                        <td class="table__cell" data-label="Precio E.">Q{vuelo.precioEjecutiva}</td>
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
                                on:click={() => handleCambiarEstadoVuelo(vuelo.id)}>
                                Cancelar
                              </button>
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
              <div class="placeholder-card">
                <p class="placeholder-card__text">No hay usuarios registrados.</p>
              </div>
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
                        <select class="rol-select"
                          value={usuario.rolId}
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

        <!-- ===== MÉTRICAS ===== -->
        {:else if activeSection === 'metricas'}
          <section class="admin-section">
            <h2 class="admin-section__title">Metricas</h2>
            <p class="admin-section__subtitle">Analisis y estadisticas del sistema</p>
            <div class="placeholder-card">
              <p class="placeholder-card__text">Esta seccion estara disponible proximamente.</p>
            </div>
          </section>
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
              on:click={() => { avionImagenPreview = null; avionImagenBase64 = null; }}>
              Quitar imagen
            </button>
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
              on:click={() => { tripulanteImagenPreview = null; tripulanteImagenBase64 = null; }}>
              Quitar foto
            </button>
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
              bind:value={paisQueryAeropuerto}
              on:input={onPaisAeropuertoInput}
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
              bind:value={ciudadQueryAeropuerto}
              on:input={onCiudadAeropuertoInput}
              on:blur={validarCiudadAeropuertoSeleccionada}
              placeholder={paisSeleccionadoAeropuerto ? 'Escribe la ciudad...' : 'Primero selecciona un pais'}
              disabled={!paisSeleccionadoAeropuerto}
              autocomplete="off" required />
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
              on:click={() => { aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null; }}>
              Quitar imagen
            </button>
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