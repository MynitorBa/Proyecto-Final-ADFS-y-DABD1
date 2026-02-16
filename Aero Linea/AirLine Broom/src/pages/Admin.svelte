<script>
// @ts-nocheck
  import '../styles/admin.css';
  import { onMount } from 'svelte';

  export let navigateTo;

  let activeSection = 'crear-vuelo';

  // Variables para datos de la API
  let usuarios = [];
  let aviones = [];
  let tripulantes = [];
  let aeropuertos = [];
  
  let loadingUsuarios = false;
  let loadingAviones = false;
  let loadingTripulantes = false;
  let loadingAeropuertos = false;
  let loadingHistorialVuelos = false;

  // Variables para búsqueda
  let busquedaOrigen = '';
  let busquedaDestino = '';
  let busquedaAvion = '';
  let busquedaTripulante = '';

  // Variables para mostrar/ocultar dropdowns
  let mostrarDropdownOrigen = false;
  let mostrarDropdownDestino = false;
  let mostrarDropdownAvion = false;
  let mostrarDropdownTripulante = false;

  // Variables para modales y formularios
  let mostrarFormularioAvion = false;
  let mostrarFormularioTripulante = false;
  let mostrarFormularioAeropuerto = false;
  let modoEdicion = false; // false = crear, true = editar

  let rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' }
  ];

  let rolesTripulacion = [];
  
  // Variables para autocomplete de ciudad/país en formulario de aeropuerto
  let todosLosPaises = [];
  let paisQueryAeropuerto = '';
  let paisesSugeridosAeropuerto = [];
  let paisSeleccionadoAeropuerto = null;
  let ciudadQueryAeropuerto = '';
  let ciudadesSugeridasAeropuerto = [];
  let ciudadSeleccionadaAeropuerto = false;

  // Objeto para nuevo vuelo
  let nuevoVuelo = {
    numeroVuelo: '',
    aeropuertoOrigenId: '',
    aeropuertoDestinoId: '',
    avionId: '',
    fecha: '',
    horaSalida: '',
    horaLlegada: '',
    precioTurista: '',
    precioEjecutiva: '',
    tripulantesSeleccionados: []
  };

  // Objeto para avión (crear/editar)
  let avionForm = {
    id: null,
    marca: '',
    modelo: '',
    capacidadPasajeros: ''
  };

  // Objeto para tripulante (crear/editar)
  let tripulanteForm = {
    id: null,
    nombre: '',
    apellido: '',
    rolID: ''
  };

  // Objeto para aeropuerto (crear/editar)
  let aeropuertoForm = {
    id: null,
    codigo: '',
    nombre: '',
    ciudad: '',
    pais: ''
  };

  let historialVuelos = [];

  onMount(async () => {
    const isAdmin = parseInt(sessionStorage.getItem('rolId')) === 2;
    if (!isAdmin) {
      navigateTo('acceso-denegado');
      return;
    }
    await cargarDatosIniciales();
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

  async function cargarUsuarios() {
    loadingUsuarios = true;
    try {
      const rolId = sessionStorage.getItem('rolId');
      const response = await fetch('http://localhost:5190/api/usuarios', {
        headers: {
          'X-RolId': rolId
        }
      });

      if (response.ok) {
        usuarios = await response.json();
      } else {
        console.error('Error al cargar usuarios');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      loadingUsuarios = false;
    }
  }

  async function cargarAviones() {
    loadingAviones = true;
    try {
      const response = await fetch('http://localhost:5190/api/aviones');

      if (response.ok) {
        aviones = await response.json();
      } else {
        console.error('Error al cargar aviones');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      loadingAviones = false;
    }
  }

  async function cargarTripulantes() {
    loadingTripulantes = true;
    try {
      const response = await fetch('http://localhost:5190/api/tripulacion');

      if (response.ok) {
        tripulantes = await response.json();
      } else {
        console.error('Error al cargar tripulantes');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      loadingTripulantes = false;
    }
  }

  async function cargarAeropuertos() {
    loadingAeropuertos = true;
    try {
      const response = await fetch('http://localhost:5190/api/aeropuertos');

      if (response.ok) {
        aeropuertos = await response.json();
      } else {
        console.error('Error al cargar aeropuertos');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      loadingAeropuertos = false;
    }
  }

  async function cargarRolesTripulacion() {
    try {
      const response = await fetch('http://localhost:5190/api/tripulacion/roles');

      if (response.ok) {
        const roles = await response.json();
        rolesTripulacion = roles.map(rol => ({
          id: rol.id,
          nombre: rol.cargo
        }));
      } else {
        console.error('Error al cargar roles de tripulación');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  }

  async function cargarPaises() {
    try {
      const response = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await response.json();
      todosLosPaises = data.data;
    } catch (error) {
      console.error('Error cargando países:', error);
    }
  }

  async function cargarHistorialVuelos() {
    loadingHistorialVuelos = true;
    try {
      const rolId = sessionStorage.getItem('rolId');
      const response = await fetch('http://localhost:5190/api/admin/vuelos/historial', {
        headers: {
          'X-RolId': rolId
        }
      });

      if (response.ok) {
        historialVuelos = await response.json();
        console.log('Vuelos cargados:', historialVuelos);
        console.log('Estados de los vuelos:', historialVuelos.map(v => ({ id: v.id, estado: v.estado })));
      } else {
        console.error('Error al cargar historial de vuelos');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      loadingHistorialVuelos = false;
    }
  }

  // Funciones para autocompletado de país en aeropuertos
  function onPaisAeropuertoInput() {
    const q = paisQueryAeropuerto.toLowerCase();
    paisesSugeridosAeropuerto = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    
    if (paisQueryAeropuerto && !paisSeleccionadoAeropuerto) {
      aeropuertoForm.pais = '';
    }
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
    if (paisQueryAeropuerto && !paisSeleccionadoAeropuerto) {
      paisQueryAeropuerto = '';
    }
  }

  // Funciones para autocompletado de ciudad en aeropuertos
  function onCiudadAeropuertoInput() {
    if (!paisSeleccionadoAeropuerto) return;
    const q = ciudadQueryAeropuerto.toLowerCase();
    ciudadesSugeridasAeropuerto = q.length < 2 ? [] : paisSeleccionadoAeropuerto.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    
    if (ciudadQueryAeropuerto && !ciudadSeleccionadaAeropuerto) {
      aeropuertoForm.ciudad = '';
    }
  }

  function seleccionarCiudadAeropuerto(c) {
    ciudadQueryAeropuerto = c;
    aeropuertoForm.ciudad = c;
    ciudadesSugeridasAeropuerto = [];
    ciudadSeleccionadaAeropuerto = true;
  }

  function validarCiudadAeropuertoSeleccionada() {
    if (ciudadQueryAeropuerto && !ciudadSeleccionadaAeropuerto) {
      ciudadQueryAeropuerto = '';
    }
  }

  // Filtrar aeropuertos según búsqueda
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

  // Filtrar aviones según búsqueda
  $: avionesFiltrados = aviones.filter(a => 
    a.nombreCompleto.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
    a.marca.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
    a.modelo.toLowerCase().includes(busquedaAvion.toLowerCase())
  );

  // Filtrar tripulantes según búsqueda y excluir ya seleccionados
  $: tripulantesFiltrados = tripulantes.filter(t => {
    const yaSeleccionado = nuevoVuelo.tripulantesSeleccionados.some(ts => ts.id === t.id);
    const coincideBusqueda = t.nombreCompleto.toLowerCase().includes(busquedaTripulante.toLowerCase()) ||
                            t.nombreRol.toLowerCase().includes(busquedaTripulante.toLowerCase());
    return !yaSeleccionado && coincideBusqueda;
  });

  // Obtener aeropuerto seleccionado
  $: aeropuertoOrigen = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoOrigenId));
  $: aeropuertoDestino = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoDestinoId));
  $: avionSeleccionado = aviones.find(a => a.id === parseInt(nuevoVuelo.avionId));

  function seleccionarAeropuertoOrigen(aeropuerto) {
    nuevoVuelo.aeropuertoOrigenId = aeropuerto.id;
    busquedaOrigen = `${aeropuerto.codigo} - ${aeropuerto.nombre}`;
    mostrarDropdownOrigen = false;
  }

  function seleccionarAeropuertoDestino(aeropuerto) {
    nuevoVuelo.aeropuertoDestinoId = aeropuerto.id;
    busquedaDestino = `${aeropuerto.codigo} - ${aeropuerto.nombre}`;
    mostrarDropdownDestino = false;
  }

  function seleccionarAvion(avion) {
    nuevoVuelo.avionId = avion.id;
    busquedaAvion = avion.nombreCompleto;
    mostrarDropdownAvion = false;
  }

  function agregarTripulante(tripulante) {
    nuevoVuelo.tripulantesSeleccionados = [...nuevoVuelo.tripulantesSeleccionados, tripulante];
    busquedaTripulante = '';
    mostrarDropdownTripulante = false;
  }

  function quitarTripulante(tripulanteId) {
    nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados.filter(t => t.id !== tripulanteId);
  }

  async function handleCrearVuelo() {
    // Validar que todos los campos obligatorios estén llenos
    if (!nuevoVuelo.numeroVuelo) {
      alert('Por favor ingresa el número de vuelo');
      return;
    }

    if (!nuevoVuelo.aeropuertoOrigenId) {
      alert('Por favor selecciona el aeropuerto de origen');
      return;
    }

    if (!nuevoVuelo.aeropuertoDestinoId) {
      alert('Por favor selecciona el aeropuerto de destino');
      return;
    }

    if (!nuevoVuelo.avionId) {
      alert('Por favor selecciona un avión');
      return;
    }

    if (!nuevoVuelo.fecha) {
      alert('Por favor selecciona la fecha del vuelo');
      return;
    }

    if (!nuevoVuelo.horaSalida || !nuevoVuelo.horaLlegada) {
      alert('Por favor ingresa las horas de salida y llegada');
      return;
    }

    if (!nuevoVuelo.precioTurista || !nuevoVuelo.precioEjecutiva) {
      alert('Por favor ingresa los precios');
      return;
    }

    try {
      const rolId = sessionStorage.getItem('rolId');
      
      const datosVuelo = {
        numeroVuelo: nuevoVuelo.numeroVuelo,
        aeropuertoOrigenId: parseInt(nuevoVuelo.aeropuertoOrigenId),
        aeropuertoDestinoId: parseInt(nuevoVuelo.aeropuertoDestinoId),
        avionId: parseInt(nuevoVuelo.avionId),
        fecha: nuevoVuelo.fecha,
        horaSalida: nuevoVuelo.horaSalida,
        horaLlegada: nuevoVuelo.horaLlegada,
        precioTurista: parseFloat(nuevoVuelo.precioTurista),
        precioEjecutiva: parseFloat(nuevoVuelo.precioEjecutiva),
        tripulantesIds: nuevoVuelo.tripulantesSeleccionados.map(t => t.id)
      };

      const response = await fetch('http://localhost:5190/api/admin/vuelos', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-RolId': rolId
        },
        body: JSON.stringify(datosVuelo)
      });

      if (response.ok) {
        const result = await response.json();
        alert('¡Vuelo creado exitosamente!');
        limpiarFormulario();
        
        // Recargar el historial de vuelos
        await cargarHistorialVuelos();
        
        // Cambiar a la pestaña de historial
        activeSection = 'historial';
      } else {
        const error = await response.json();
        alert(error.message || 'Error al crear el vuelo');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al crear el vuelo');
    }
  }

  function limpiarFormulario() {
    nuevoVuelo = {
      numeroVuelo: '',
      aeropuertoOrigenId: '',
      aeropuertoDestinoId: '',
      avionId: '',
      fecha: '',
      horaSalida: '',
      horaLlegada: '',
      precioTurista: '',
      precioEjecutiva: '',
      tripulantesSeleccionados: []
    };
    busquedaOrigen = '';
    busquedaDestino = '';
    busquedaAvion = '';
    busquedaTripulante = '';
  }

  async function handleCambiarRol(userId, nuevoRolId) {
    try {
      const rolId = sessionStorage.getItem('rolId');
      const response = await fetch('http://localhost:5190/api/usuarios/cambiar-rol', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-RolId': rolId
        },
        body: JSON.stringify({ 
          usuarioId: parseInt(userId),
          nuevoRolId: parseInt(nuevoRolId) 
        })
      });

      if (response.ok) {

      } else {
        const error = await response.json();
        alert(error.message || 'Error al cambiar el rol');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al cambiar el rol');
    }
  }

  async function handleCambiarEstadoVuelo(vueloId, nuevoEstado) {
    if (!confirm('¿Estás seguro de que deseas cancelar este vuelo?')) {
      return;
    }

    try {
      const rolId = sessionStorage.getItem('rolId');
      
      const response = await fetch(`http://localhost:5190/api/admin/vuelos/${vueloId}/cancelar`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'X-RolId': rolId
        }
      });

      if (response.ok) {
        alert('Vuelo cancelado exitosamente');
        await cargarHistorialVuelos();
      } else {
        const error = await response.json();
        alert(error.message || 'Error al cancelar el vuelo');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al cancelar el vuelo');
    }
  }

  // ===== FUNCIONES PARA AVIONES =====
  
  function abrirFormularioNuevoAvion() {
    modoEdicion = false;
    avionForm = {
      id: null,
      marca: '',
      modelo: '',
      capacidadPasajeros: ''
    };
    mostrarFormularioAvion = true;
  }

  function abrirFormularioEditarAvion(avion) {
    modoEdicion = true;
    avionForm = {
      id: avion.id,
      marca: avion.marca,
      modelo: avion.modelo,
      capacidadPasajeros: avion.capacidadPasajeros
    };
    mostrarFormularioAvion = true;
  }

  function cerrarFormularioAvion() {
    mostrarFormularioAvion = false;
    avionForm = {
      id: null,
      marca: '',
      modelo: '',
      capacidadPasajeros: ''
    };
  }

  async function handleGuardarAvion() {
    try {
      if (modoEdicion) {
        // Actualizar avión existente
        const response = await fetch(`http://localhost:5190/api/aviones/${avionForm.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            marca: avionForm.marca,
            modelo: avionForm.modelo,
            capacidadPasajeros: parseInt(avionForm.capacidadPasajeros)
          })
        });

        if (response.ok) {
          alert('Avión actualizado correctamente');
          await cargarAviones();
          cerrarFormularioAvion();
        } else {
          const error = await response.json();
          alert(error.message || 'Error al actualizar el avión');
        }
      } else {
        // Crear nuevo avión
        const response = await fetch('http://localhost:5190/api/aviones', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            marca: avionForm.marca,
            modelo: avionForm.modelo,
            capacidadPasajeros: parseInt(avionForm.capacidadPasajeros)
          })
        });

        if (response.ok) {
          alert('Avión creado correctamente');
          await cargarAviones();
          cerrarFormularioAvion();
        } else {
          const error = await response.json();
          alert(error.message || 'Error al crear el avión');
        }
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al guardar el avión');
    }
  }

  async function handleEliminarAvion(avionId) {
     if (confirm('¿Estás seguro de que deseas eliminar este avión?')) {
      console.log('Eliminando avión:', avionId);
      alert('avión eliminado (backend pendiente)');
      // Aquí irá el DELETE cuando tengas el backend
    }
  }

  // ===== FUNCIONES PARA TRIPULANTES =====
  
  function abrirFormularioNuevoTripulante() {
    modoEdicion = false;
    tripulanteForm = {
      id: null,
      nombre: '',
      apellido: '',
      rolID: ''
    };
    mostrarFormularioTripulante = true;
  }

  function abrirFormularioEditarTripulante(tripulante) {
    modoEdicion = true;
    tripulanteForm = {
      id: tripulante.id,
      nombre: tripulante.nombre,
      apellido: tripulante.apellido,
      rolID: tripulante.rolID
    };
    mostrarFormularioTripulante = true;
  }

  function cerrarFormularioTripulante() {
    mostrarFormularioTripulante = false;
    tripulanteForm = {
      id: null,
      nombre: '',
      apellido: '',
      rolID: ''
    };
  }

  async function handleGuardarTripulante() {
    try {
      if (modoEdicion) {
        // Actualizar tripulante existente
        const response = await fetch(`http://localhost:5190/api/tripulacion/${tripulanteForm.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            nombre: tripulanteForm.nombre,
            apellido: tripulanteForm.apellido,
            rolID: parseInt(tripulanteForm.rolID)
          })
        });

        if (response.ok) {
          alert('Tripulante actualizado correctamente');
          await cargarTripulantes();
          cerrarFormularioTripulante();
        } else {
          const error = await response.json();
          alert(error.message || 'Error al actualizar el tripulante');
        }
      } else {
        // Crear nuevo tripulante
        const response = await fetch('http://localhost:5190/api/tripulacion', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            nombre: tripulanteForm.nombre,
            apellido: tripulanteForm.apellido,
            rolID: parseInt(tripulanteForm.rolID)
          })
        });

        if (response.ok) {
          alert('Tripulante creado correctamente');
          await cargarTripulantes();
          cerrarFormularioTripulante();
        } else {
          const error = await response.json();
          alert(error.message || 'Error al crear el tripulante');
        }
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al guardar el tripulante');
    }
  }

  function handleEliminarTripulante(tripulanteId) {
    if (confirm('¿Estás seguro de que deseas eliminar este tripulante?')) {
      console.log('Eliminando tripulante:', tripulanteId);
      alert('Tripulante eliminado (backend pendiente)');
      // Aquí irá el DELETE cuando tengas el backend
    }
  }

  // ===== FUNCIONES PARA AEROPUERTOS =====
  
  function abrirFormularioNuevoAeropuerto() {
    modoEdicion = false;
    aeropuertoForm = {
      id: null,
      codigo: '',
      nombre: '',
      ciudad: '',
      pais: ''
    };
    paisQueryAeropuerto = '';
    ciudadQueryAeropuerto = '';
    paisSeleccionadoAeropuerto = null;
    ciudadSeleccionadaAeropuerto = false;
    paisesSugeridosAeropuerto = [];
    ciudadesSugeridasAeropuerto = [];
    mostrarFormularioAeropuerto = true;
  }

  async function abrirFormularioEditarAeropuerto(aeropuerto) {
    modoEdicion = true;
    
    // Cargar el aeropuerto completo desde el servidor
    try {
      const response = await fetch(`http://localhost:5190/api/aeropuertos/${aeropuerto.id}`);
      
      if (response.ok) {
        const aeropuertoCompleto = await response.json();
        
        aeropuertoForm = {
          id: aeropuertoCompleto.id,
          codigo: aeropuertoCompleto.codigo,
          nombre: aeropuertoCompleto.nombre,
          ciudad: aeropuertoCompleto.ciudad,
          pais: aeropuertoCompleto.pais
        };

        // Pre-cargar los valores del autocomplete
        paisQueryAeropuerto = aeropuertoCompleto.pais;
        ciudadQueryAeropuerto = aeropuertoCompleto.ciudad;
        
        // Buscar el país en la lista para poder seleccionar ciudad
        const paisEncontrado = todosLosPaises.find(p => 
          p.country.toLowerCase() === aeropuertoCompleto.pais.toLowerCase()
        );
        
        if (paisEncontrado) {
          paisSeleccionadoAeropuerto = paisEncontrado;
        }
        
        ciudadSeleccionadaAeropuerto = true;
        
        mostrarFormularioAeropuerto = true;
      } else {
        console.error('Error al cargar el aeropuerto');
        alert('Error al cargar los datos del aeropuerto');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al cargar el aeropuerto');
    }
  }

  function cerrarFormularioAeropuerto() {
    mostrarFormularioAeropuerto = false;
    aeropuertoForm = {
      id: null,
      codigo: '',
      nombre: '',
      ciudad: '',
      pais: ''
    };
    paisQueryAeropuerto = '';
    ciudadQueryAeropuerto = '';
    paisSeleccionadoAeropuerto = null;
    ciudadSeleccionadaAeropuerto = false;
    paisesSugeridosAeropuerto = [];
    ciudadesSugeridasAeropuerto = [];
  }

  async function handleGuardarAeropuerto() {
    // Validar que se haya seleccionado país y ciudad de la lista
    if (!paisSeleccionadoAeropuerto || !aeropuertoForm.pais) {
      alert('Debes seleccionar un país de la lista');
      return;
    }
    
    if (!ciudadSeleccionadaAeropuerto || !aeropuertoForm.ciudad) {
      alert('Debes seleccionar una ciudad de la lista');
      return;
    }

    try {
      if (modoEdicion) {
        // Actualizar aeropuerto existente
        const response = await fetch(`http://localhost:5190/api/aeropuertos/${aeropuertoForm.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            nombre: aeropuertoForm.nombre,
            codigo: aeropuertoForm.codigo.toUpperCase(),
            ciudad: aeropuertoForm.ciudad,
            pais: aeropuertoForm.pais
          })
        });

        if (response.ok) {
          alert('Aeropuerto actualizado correctamente');
          await cargarAeropuertos();
          cerrarFormularioAeropuerto();
        } else {
          const error = await response.json();
          alert(error.message || 'Error al actualizar el aeropuerto');
        }
      } else {
        // Crear nuevo aeropuerto
        const response = await fetch('http://localhost:5190/api/aeropuertos', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            nombre: aeropuertoForm.nombre,
            codigo: aeropuertoForm.codigo.toUpperCase(),
            ciudad: aeropuertoForm.ciudad,
            pais: aeropuertoForm.pais
          })
        });

        if (response.ok) {
          alert('Aeropuerto creado correctamente');
          await cargarAeropuertos();
          cerrarFormularioAeropuerto();
        } else {
          const error = await response.json();
          alert(error.message || 'Error al crear el aeropuerto');
        }
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al guardar el aeropuerto');
    }
  }

  function handleEliminarAeropuerto(aeropuertoId) {
    if (confirm('¿Estás seguro de que deseas eliminar este aeropuerto?')) {
      console.log('Eliminando aeropuerto:', aeropuertoId);
      alert('Aeropuerto eliminado (backend pendiente)');
      // Aquí irá el DELETE cuando tengas el backend
    }
  }
</script>

<div class="admin">
  <div class="admin__container">
    <div class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>
        Salir del Panel
      </button>
      <h1 class="admin__title">Panel de Administracion</h1>
      <p class="admin__subtitle">Gestion de vuelos, usuarios y metricas</p>
    </div>

    <div class="admin__content">
      <aside class="admin__sidebar">
        <nav class="admin-nav">
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'crear-vuelo'}
            on:click={() => activeSection = 'crear-vuelo'}
          >
            Crear Vuelo
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'gestionar-aviones'}
            on:click={() => activeSection = 'gestionar-aviones'}
          >
            Gestionar Aviones
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'gestionar-tripulantes'}
            on:click={() => activeSection = 'gestionar-tripulantes'}
          >
            Gestionar Tripulantes
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'gestionar-aeropuertos'}
            on:click={() => activeSection = 'gestionar-aeropuertos'}
          >
            Gestionar Aeropuertos
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'historial'}
            on:click={() => activeSection = 'historial'}
          >
            Historial
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'usuarios'}
            on:click={() => activeSection = 'usuarios'}
          >
            Usuarios
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'metricas'}
            on:click={() => activeSection = 'metricas'}
          >
            Metricas
          </button>
        </nav>
      </aside>

      <main class="admin__main">
        {#if activeSection === 'crear-vuelo'}
          <section class="admin-section">
            <h2 class="admin-section__title">Crear Nuevo Vuelo</h2>
            <p class="admin-section__subtitle">Completa todos los datos del vuelo</p>

            <form class="admin-form" on:submit|preventDefault={handleCrearVuelo}>
              
              <!-- Información Básica -->
              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Informacion Basica</h3>
                
                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="numeroVuelo" class="admin-form__label">Numero de Vuelo *</label>
                    <input 
                      type="text" 
                      id="numeroVuelo"
                      class="admin-form__input"
                      bind:value={nuevoVuelo.numeroVuelo}
                      placeholder="Ej: AA 1234"
                      required
                    />
                  </div>

                  <div class="admin-form__field">
                    <label for="fecha" class="admin-form__label">Fecha del Vuelo *</label>
                    <input 
                      type="date" 
                      id="fecha"
                      class="admin-form__input"
                      bind:value={nuevoVuelo.fecha}
                      required
                    />
                  </div>
                </div>
              </div>

              <!-- Ruta -->
              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Ruta</h3>
                
                <div class="admin-form__row">
                  <!-- Aeropuerto Origen -->
                  <div class="admin-form__field">
                    <label for="origen" class="admin-form__label">Aeropuerto de Origen *</label>
                    {#if loadingAeropuertos}
                      <p class="loading-text">Cargando aeropuertos...</p>
                    {:else}
                      <div class="searchable-select">
                        <input 
                          type="text"
                          class="admin-form__input"
                          bind:value={busquedaOrigen}
                          on:focus={() => mostrarDropdownOrigen = true}
                          on:blur={() => setTimeout(() => mostrarDropdownOrigen = false, 200)}
                          placeholder="Buscar aeropuerto..."
                          autocomplete="off"
                        />
                        {#if mostrarDropdownOrigen && aeropuertosFiltradosOrigen.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each aeropuertosFiltradosOrigen.slice(0, 10) as aeropuerto}
                              <button
                                type="button"
                                class="searchable-select__option"
                                on:click={() => seleccionarAeropuertoOrigen(aeropuerto)}
                              >
                                <span class="searchable-select__option-code">{aeropuerto.codigo}</span>
                                <span class="searchable-select__option-name">{aeropuerto.nombre}</span>
                                <span class="searchable-select__option-city">{aeropuerto.ciudad}</span>
                              </button>
                            {/each}
                          </div>
                        {/if}
                        {#if aeropuertoOrigen}
                          <p class="selected-item">
                            âœ“ Seleccionado: {aeropuertoOrigen.codigo} - {aeropuertoOrigen.nombre}
                          </p>
                        {/if}
                      </div>
                    {/if}
                  </div>

                  <!-- Aeropuerto Destino -->
                  <div class="admin-form__field">
                    <label for="destino" class="admin-form__label">Aeropuerto de Destino *</label>
                    {#if loadingAeropuertos}
                      <p class="loading-text">Cargando aeropuertos...</p>
                    {:else}
                      <div class="searchable-select">
                        <input 
                          type="text"
                          class="admin-form__input"
                          bind:value={busquedaDestino}
                          on:focus={() => mostrarDropdownDestino = true}
                          on:blur={() => setTimeout(() => mostrarDropdownDestino = false, 200)}
                          placeholder="Buscar aeropuerto..."
                          autocomplete="off"
                        />
                        {#if mostrarDropdownDestino && aeropuertosFiltradosDestino.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each aeropuertosFiltradosDestino.slice(0, 10) as aeropuerto}
                              <button
                                type="button"
                                class="searchable-select__option"
                                on:click={() => seleccionarAeropuertoDestino(aeropuerto)}
                              >
                                <span class="searchable-select__option-code">{aeropuerto.codigo}</span>
                                <span class="searchable-select__option-name">{aeropuerto.nombre}</span>
                                <span class="searchable-select__option-city">{aeropuerto.ciudad}</span>
                              </button>
                            {/each}
                          </div>
                        {/if}
                        {#if aeropuertoDestino}
                          <p class="selected-item">
                            âœ“ Seleccionado: {aeropuertoDestino.codigo} - {aeropuertoDestino.nombre}
                          </p>
                        {/if}
                      </div>
                    {/if}
                  </div>
                </div>
              </div>

              <!-- Horarios -->
              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Horarios</h3>
                
                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="horaSalida" class="admin-form__label">Hora de Salida *</label>
                    <input 
                      type="time" 
                      id="horaSalida"
                      class="admin-form__input"
                      bind:value={nuevoVuelo.horaSalida}
                      required
                    />
                  </div>

                  <div class="admin-form__field">
                    <label for="horaLlegada" class="admin-form__label">Hora de Llegada *</label>
                    <input 
                      type="time" 
                      id="horaLlegada"
                      class="admin-form__input"
                      bind:value={nuevoVuelo.horaLlegada}
                      required
                    />
                  </div>
                </div>
              </div>

              <!-- Aeronave -->
              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Aeronave</h3>
                
                <div class="admin-form__row">
                  <div class="admin-form__field admin-form__field--full">
                    <label for="avion" class="admin-form__label">Seleccionar Avion *</label>
                    {#if loadingAviones}
                      <p class="loading-text">Cargando aviones...</p>
                    {:else}
                      <div class="searchable-select">
                        <input 
                          type="text"
                          class="admin-form__input"
                          bind:value={busquedaAvion}
                          on:focus={() => mostrarDropdownAvion = true}
                          on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
                          placeholder="Buscar avión (marca, modelo)..."
                          autocomplete="off"
                        />
                        {#if mostrarDropdownAvion && avionesFiltrados.length > 0}
                          <div class="searchable-select__dropdown">
                            {#each avionesFiltrados.slice(0, 10) as avion}
                              <button
                                type="button"
                                class="searchable-select__option"
                                on:click={() => seleccionarAvion(avion)}
                              >
                                <span class="searchable-select__option-name">{avion.nombreCompleto}</span>
                                <span class="searchable-select__option-detail">Capacidad: {avion.capacidadPasajeros} pasajeros</span>
                              </button>
                            {/each}
                          </div>
                        {/if}
                      </div>
                    {/if}
                  </div>
                </div>

                {#if avionSeleccionado}
                  <div class="avion-info">
                    <p class="avion-info__text">
                      <strong>Avión seleccionado:</strong> {avionSeleccionado.nombreCompleto}
                    </p>
                    <p class="avion-info__text">
                      <strong>Capacidad total:</strong> {avionSeleccionado.capacidadPasajeros} pasajeros
                    </p>
                  </div>
                {/if}
              </div>

              <!-- Precios -->
              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Precios</h3>
                
                <div class="admin-form__row">
                  <div class="admin-form__field">
                    <label for="precioTurista" class="admin-form__label">Precio Clase Turista ($) *</label>
                    <input 
                      type="number" 
                      id="precioTurista"
                      class="admin-form__input"
                      bind:value={nuevoVuelo.precioTurista}
                      placeholder="650"
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>

                  <div class="admin-form__field">
                    <label for="precioEjecutiva" class="admin-form__label">Precio Clase Ejecutiva ($) *</label>
                    <input 
                      type="number" 
                      id="precioEjecutiva"
                      class="admin-form__input"
                      bind:value={nuevoVuelo.precioEjecutiva}
                      placeholder="1450"
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>
                </div>
              </div>

              <!-- Tripulación -->
              <div class="admin-form__group">
                <h3 class="admin-form__group-title">Tripulacion</h3>
                <p class="admin-form__subtitle">Busca y agrega miembros de la tripulacion uno por uno</p>
                
                {#if loadingTripulantes}
                  <p class="loading-text">Cargando tripulantes...</p>
                {:else}
                  <!-- Lista de tripulantes seleccionados -->
                  {#if nuevoVuelo.tripulantesSeleccionados.length > 0}
                    <div class="tripulantes-seleccionados">
                      <h4 class="tripulantes-seleccionados__title">
                        Tripulantes Agregados ({nuevoVuelo.tripulantesSeleccionados.length})
                      </h4>
                      <div class="tripulantes-seleccionados__list">
                        {#each nuevoVuelo.tripulantesSeleccionados as tripulante}
                          <div class="tripulante-item">
                            <div class="tripulante-item__info">
                              <span class="tripulante-item__name">{tripulante.nombreCompleto}</span>
                              <span class="tripulante-item__rol">{tripulante.nombreRol}</span>
                            </div>
                            <button
                              type="button"
                              class="tripulante-item__remove"
                              on:click={() => quitarTripulante(tripulante.id)}
                              title="Quitar tripulante"
                            >
                              ×
                            </button>
                          </div>
                        {/each}
                      </div>
                    </div>
                  {/if}

                  <!-- Buscador para agregar tripulante -->
                  <div class="agregar-tripulante">
                    <label class="agregar-tripulante__label">Buscar y Agregar Tripulante</label>
                    <div class="searchable-select">
                      <input 
                        type="text"
                        class="admin-form__input"
                        bind:value={busquedaTripulante}
                        on:focus={() => mostrarDropdownTripulante = true}
                        on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
                        placeholder="Buscar por nombre o rol..."
                        autocomplete="off"
                      />
                      {#if mostrarDropdownTripulante && tripulantesFiltrados.length > 0}
                        <div class="searchable-select__dropdown">
                          {#each tripulantesFiltrados.slice(0, 10) as tripulante}
                            <button
                              type="button"
                              class="searchable-select__option"
                              on:click={() => agregarTripulante(tripulante)}
                            >
                              <span class="searchable-select__option-name">{tripulante.nombreCompleto}</span>
                              <span class="searchable-select__option-role">{tripulante.nombreRol}</span>
                            </button>
                          {/each}
                        </div>
                      {/if}
                      {#if busquedaTripulante && tripulantesFiltrados.length === 0}
                        <p class="no-results">No se encontraron tripulantes disponibles</p>
                      {/if}
                    </div>
                  </div>
                {/if}
              </div>

              <!-- Botones de Acción -->
              <div class="admin-form__actions">
                <button type="submit" class="admin-form__submit">
                  Crear Vuelo
                </button>
                <button 
                  type="button" 
                  class="admin-form__cancel"
                  on:click={limpiarFormulario}
                >
                  Limpiar Formulario
                </button>
              </div>
            </form>
          </section>

        {:else if activeSection === 'gestionar-aviones'}
          <section class="admin-section">
            <div class="section-header">
              <div>
                <h2 class="admin-section__title">Gestionar Aviones</h2>
                <p class="admin-section__subtitle">Administra la flota de aviones disponibles</p>
              </div>
              <button class="btn-add" on:click={abrirFormularioNuevoAvion}>
                <span class="btn-add__icon">+</span>
                Agregar Avion
              </button>
            </div>

            {#if loadingAviones}
              <p class="loading-text">Cargando aviones...</p>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr class="table__row">
                      <th class="table__header">ID</th>
                      <th class="table__header">Marca</th>
                      <th class="table__header">Modelo</th>
                      <th class="table__header">Nombre Completo</th>
                      <th class="table__header">Capacidad</th>
                      <th class="table__header">Acciones</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each aviones as avion}
                      <tr class="table__row">
                        <td class="table__cell">{avion.id}</td>
                        <td class="table__cell">{avion.marca}</td>
                        <td class="table__cell">{avion.modelo}</td>
                        <td class="table__cell"><strong>{avion.nombreCompleto}</strong></td>
                        <td class="table__cell">{avion.capacidadPasajeros} pasajeros</td>
                        <td class="table__cell">
                          <div class="table__actions">
                            <button 
                              class="table__action-btn table__action-btn--edit"
                              on:click={() => abrirFormularioEditarAvion(avion)}
                            >
                              Editar
                            </button>
                            
                          </div>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </section>

        {:else if activeSection === 'gestionar-tripulantes'}
          <section class="admin-section">
            <div class="section-header">
              <div>
                <h2 class="admin-section__title">Gestionar Tripulantes</h2>
                <p class="admin-section__subtitle">Administra los miembros de la tripulacion</p>
              </div>
              <button class="btn-add" on:click={abrirFormularioNuevoTripulante}>
                <span class="btn-add__icon">+</span>
                Agregar Tripulante
              </button>
            </div>

            {#if loadingTripulantes}
              <p class="loading-text">Cargando tripulantes...</p>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr class="table__row">
                      <th class="table__header">ID</th>
                      <th class="table__header">Nombre</th>
                      <th class="table__header">Apellido</th>
                      <th class="table__header">Nombre Completo</th>
                      <th class="table__header">Rol</th>
                      <th class="table__header">Acciones</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each tripulantes as tripulante}
                      <tr class="table__row">
                        <td class="table__cell">{tripulante.id}</td>
                        <td class="table__cell">{tripulante.nombre}</td>
                        <td class="table__cell">{tripulante.apellido}</td>
                        <td class="table__cell"><strong>{tripulante.nombreCompleto}</strong></td>
                        <td class="table__cell">
                          <span class="rol-badge rol-badge--tripulacion">{tripulante.nombreRol}</span>
                        </td>
                        <td class="table__cell">
                          <div class="table__actions">
                            <button 
                              class="table__action-btn table__action-btn--edit"
                              on:click={() => abrirFormularioEditarTripulante(tripulante)}
                            >
                              Editar
                            </button>
                            
                          </div>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </section>

        {:else if activeSection === 'gestionar-aeropuertos'}
          <section class="admin-section">
            <div class="section-header">
              <div>
                <h2 class="admin-section__title">Gestionar Aeropuertos</h2>
                <p class="admin-section__subtitle">Administra los aeropuertos disponibles</p>
              </div>
              <button class="btn-add" on:click={abrirFormularioNuevoAeropuerto}>
                <span class="btn-add__icon">+</span>
                Agregar Aeropuerto
              </button>
            </div>

            {#if loadingAeropuertos}
              <p class="loading-text">Cargando aeropuertos...</p>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr class="table__row">
                      <th class="table__header">ID</th>
                      <th class="table__header">Codigo</th>
                      <th class="table__header">Nombre</th>
                      <th class="table__header">Ciudad</th>
                      <th class="table__header">Pais</th>
                      <th class="table__header">Acciones</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each aeropuertos as aeropuerto}
                      <tr class="table__row">
                        <td class="table__cell">{aeropuerto.id}</td>
                        <td class="table__cell"><strong>{aeropuerto.codigo}</strong></td>
                        <td class="table__cell">{aeropuerto.nombre}</td>
                        <td class="table__cell">{aeropuerto.ciudad}</td>
                        <td class="table__cell">{aeropuerto.pais}</td>
                        <td class="table__cell">
                          <div class="table__actions">
                            <button 
                              class="table__action-btn table__action-btn--edit"
                              on:click={() => abrirFormularioEditarAeropuerto(aeropuerto)}
                            >
                              Editar
                            </button>
                            
                          </div>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </section>

        {:else if activeSection === 'historial'}
          <section class="admin-section">
            <h2 class="admin-section__title">Historial de Vuelos</h2>
            <p class="admin-section__subtitle">Todos los vuelos programados, activos, completados y cancelados</p>

            {#if loadingHistorialVuelos}
              <p class="loading-text">Cargando historial de vuelos...</p>
            {:else if historialVuelos.length === 0}
              <div class="placeholder-card">
                <p class="placeholder-card__text">
                  No hay vuelos registrados. Crea tu primer vuelo desde la pestaña "Crear Vuelo".
                </p>
              </div>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr class="table__row">
                      <th class="table__header">ID</th>
                      <th class="table__header">Numero de Vuelo</th>
                      <th class="table__header">Ruta</th>
                      <th class="table__header">Fecha</th>
                      <th class="table__header">Horario</th>
                      <th class="table__header">Ocupacion</th>
                      <th class="table__header">Estado</th>
                      <th class="table__header">Acciones</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each historialVuelos as vuelo}
                      <tr class="table__row">
                        <td class="table__cell">{vuelo.id}</td>
                        <td class="table__cell">{vuelo.numeroVuelo}</td>
                        <td class="table__cell">{vuelo.ruta}</td>
                        <td class="table__cell">{vuelo.fecha}</td>
                        <td class="table__cell">{vuelo.horaSalida} - {vuelo.horaLlegada}</td>
                        <td class="table__cell">{vuelo.asientosVendidos}/{vuelo.asientosTotales}</td>
                        <td class="table__cell">
                          <span class="status-badge status-badge--{vuelo.estado}">
                            {vuelo.estado}
                          </span>
                        </td>
                        <td class="table__cell">
                          <div class="table__actions">
                            {#if vuelo.estado === 'activo'}
                              <button 
                                class="table__action-btn table__action-btn--cancel"
                                on:click={() => handleCambiarEstadoVuelo(vuelo.id, 'cancelado')}
                              >
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

        {:else if activeSection === 'usuarios'}
          <section class="admin-section">
            <h2 class="admin-section__title">Gestion de Usuarios</h2>
            <p class="admin-section__subtitle">Administra los roles de los usuarios del sistema</p>

            {#if loadingUsuarios}
              <p>Cargando usuarios...</p>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr class="table__row">
                      <th class="table__header">ID</th>
                      <th class="table__header">Nombre</th>
                      <th class="table__header">Email</th>
                      <th class="table__header">Username</th>
                      <th class="table__header">Rol Actual</th>
                      <th class="table__header">Cambiar Rol</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each usuarios as usuario}
                      <tr class="table__row">
                        <td class="table__cell">{usuario.id}</td>
                        <td class="table__cell">{usuario.nombre} {usuario.apellido}</td>
                        <td class="table__cell">{usuario.correo}</td>
                        <td class="table__cell">{usuario.username}</td>
                        <td class="table__cell">
                          <span class="rol-badge rol-badge--{usuario.rolNombre.toLowerCase().replace(/\s+/g, '-')}">
                            {usuario.rolNombre}
                          </span>
                        </td>
                        <td class="table__cell">
                          <div class="table__actions">
                            <select 
                              class="rol-select"
                              value={usuario.rolId}
                              on:change={(e) => handleCambiarRol(usuario.id, e.target.value)}
                            >
                              {#each rolesDisponibles as rol}
                                <option value={rol.id}>
                                  {rol.nombre}
                                </option>
                              {/each}
                            </select>
                          </div>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </section>

        {:else if activeSection === 'metricas'}
          <section class="admin-section">
            <h2 class="admin-section__title">Metricas</h2>
            <p class="admin-section__subtitle">Analisis y estadisticas del sistema</p>

            <div class="placeholder-card">
              <p class="placeholder-card__text">
                Esta seccion estara disponible proximamente con metricas detalladas.
              </p>
            </div>
          </section>
        {/if}
      </main>
    </div>
  </div>
</div>

<!-- Modal para Avión -->
{#if mostrarFormularioAvion}
  <div class="modal-overlay" on:click={cerrarFormularioAvion}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Avion</h3>
        <button class="modal__close" on:click={cerrarFormularioAvion}>×</button>
      </div>
      
      <form class="modal__form" on:submit|preventDefault={handleGuardarAvion}>
        <div class="form-field">
          <label for="marca" class="form-label">Marca *</label>
          <input 
            type="text" 
            id="marca"
            class="form-input"
            bind:value={avionForm.marca}
            placeholder="Ej: Boeing"
            required
          />
        </div>

        <div class="form-field">
          <label for="modelo" class="form-label">Modelo *</label>
          <input 
            type="text" 
            id="modelo"
            class="form-input"
            bind:value={avionForm.modelo}
            placeholder="Ej: 787-9"
            required
          />
        </div>

        <div class="form-field">
          <label for="capacidad" class="form-label">Capacidad de Pasajeros *</label>
          <input 
            type="number" 
            id="capacidad"
            class="form-input"
            bind:value={avionForm.capacidadPasajeros}
            placeholder="Ej: 240"
            min="1"
            required
          />
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary">
            {modoEdicion ? 'Actualizar' : 'Crear'} Avion
          </button>
          <button type="button" class="btn-secondary" on:click={cerrarFormularioAvion}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- Modal para Tripulante -->
{#if mostrarFormularioTripulante}
  <div class="modal-overlay" on:click={cerrarFormularioTripulante}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Tripulante</h3>
        <button class="modal__close" on:click={cerrarFormularioTripulante}>×</button>
      </div>
      
      <form class="modal__form" on:submit|preventDefault={handleGuardarTripulante}>
        <div class="form-field">
          <label for="nombre-tripulante" class="form-label">Nombre *</label>
          <input 
            type="text" 
            id="nombre-tripulante"
            class="form-input"
            bind:value={tripulanteForm.nombre}
            placeholder="Ej: Juan"
            required
          />
        </div>

        <div class="form-field">
          <label for="apellido-tripulante" class="form-label">Apellido *</label>
          <input 
            type="text" 
            id="apellido-tripulante"
            class="form-input"
            bind:value={tripulanteForm.apellido}
            placeholder="Ej: Pérez"
            required
          />
        </div>

        <div class="form-field">
          <label for="rol-tripulante" class="form-label">Rol *</label>
          <select 
            id="rol-tripulante"
            class="form-input"
            bind:value={tripulanteForm.rolID}
            required
          >
            <option value="">Selecciona un rol</option>
            {#each rolesTripulacion as rol}
              <option value={rol.id}>{rol.nombre}</option>
            {/each}
          </select>
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary">
            {modoEdicion ? 'Actualizar' : 'Crear'} Tripulante
          </button>
          <button type="button" class="btn-secondary" on:click={cerrarFormularioTripulante}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- Modal para Aeropuerto -->
{#if mostrarFormularioAeropuerto}
  <div class="modal-overlay" on:click={cerrarFormularioAeropuerto}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Aeropuerto</h3>
        <button class="modal__close" on:click={cerrarFormularioAeropuerto}>×</button>
      </div>
      
      <form class="modal__form" on:submit|preventDefault={handleGuardarAeropuerto}>
        <div class="form-field">
          <label for="codigo-aeropuerto" class="form-label">Codigo IATA *</label>
          <input 
            type="text" 
            id="codigo-aeropuerto"
            class="form-input"
            bind:value={aeropuertoForm.codigo}
            placeholder="Ej: GUA"
            maxlength="3"
            style="text-transform: uppercase;"
            required
          />
        </div>

        <div class="form-field">
          <label for="nombre-aeropuerto" class="form-label">Nombre del Aeropuerto *</label>
          <input 
            type="text" 
            id="nombre-aeropuerto"
            class="form-input"
            bind:value={aeropuertoForm.nombre}
            placeholder="Ej: La Aurora"
            required
          />
        </div>

        <div class="form-field">
          <label for="pais-aeropuerto" class="form-label">País *</label>
          <div class="autocomplete">
            <input 
              type="text" 
              id="pais-aeropuerto" 
              class="form-input" 
              bind:value={paisQueryAeropuerto} 
              on:input={onPaisAeropuertoInput} 
              on:blur={validarPaisAeropuertoSeleccionado}
              placeholder="Escribe el país..." 
              autocomplete="off"
              required
            />
            {#if paisesSugeridosAeropuerto.length > 0}
              <ul class="autocomplete__list">
                {#each paisesSugeridosAeropuerto as p}
                  <li class="autocomplete__item">
                    <button type="button" class="autocomplete__btn" on:click={() => seleccionarPaisAeropuerto(p)}>{p.country}</button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label for="ciudad-aeropuerto" class="form-label">Ciudad *</label>
          <div class="autocomplete">
            <input 
              type="text" 
              id="ciudad-aeropuerto" 
              class="form-input" 
              bind:value={ciudadQueryAeropuerto} 
              on:input={onCiudadAeropuertoInput} 
              on:blur={validarCiudadAeropuertoSeleccionada}
              placeholder={paisSeleccionadoAeropuerto ? 'Escribe la ciudad...' : 'Primero selecciona un país'} 
              disabled={!paisSeleccionadoAeropuerto} 
              autocomplete="off"
              required
            />
            {#if ciudadesSugeridasAeropuerto.length > 0}
              <ul class="autocomplete__list">
                {#each ciudadesSugeridasAeropuerto as c}
                  <li class="autocomplete__item">
                    <button type="button" class="autocomplete__btn" on:click={() => seleccionarCiudadAeropuerto(c)}>{c}</button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary">
            {modoEdicion ? 'Actualizar' : 'Crear'} Aeropuerto
          </button>
          <button type="button" class="btn-secondary" on:click={cerrarFormularioAeropuerto}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}