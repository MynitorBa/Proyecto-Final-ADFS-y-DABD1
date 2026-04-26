<script>
/**
 * @file AdminCrearVuelo.svelte
 * @description Seccion del panel de administracion para crear un nuevo vuelo. Proporciona un formulario
 * de multiples secciones que cubre informacion basica (numero de vuelo, fecha), ruta (aeropuertos de origen
 * y destino con dropdowns de busqueda), horarios (hora de salida y vista previa de llegada estimada calculada
 * automaticamente), seleccion de aeronave (filtrada por disponibilidad en la fecha seleccionada), distribucion
 * de asientos con una barra visual de capacidad, precios para clases turista y ejecutiva, y asignacion de
 * tripulacion. Valida todos los campos antes de enviar al backend. Verifica la existencia de la ruta y la
 * disponibilidad en tiempo real de aviones y tripulantes para la fecha elegida. Despacha 'vueloCreado' al
 * tener exito e 'irARutas' cuando el usuario solicita crear una ruta faltante.
 */
// @ts-nocheck
  import { createEventDispatcher } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Lista de todos los aeropuertos disponibles, usada para poblar los dropdowns de origen y destino. @type {any[]} */
  export let aeropuertos  = [];

  /** Lista de todos los aviones de la flota, usada para poblar el dropdown de seleccion de aeronave. @type {any[]} */
  export let aviones      = [];

  /** Lista de todos los tripulantes, usada para poblar el dropdown de asignacion de tripulacion. @type {any[]} */
  export let tripulantes  = [];

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Funcion para mostrar un dialogo de confirmacion. Firma: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  /** Texto actual en el input de busqueda del aeropuerto de origen. @type {string} */
  let busquedaOrigen        = '';

  /** Texto actual en el input de busqueda del aeropuerto de destino. @type {string} */
  let busquedaDestino       = '';

  /** Texto actual en el input de busqueda del avion. @type {string} */
  let busquedaAvion         = '';

  /** Texto actual en el input de busqueda del tripulante. @type {string} */
  let busquedaTripulante    = '';

  /** Indica si el dropdown de aeropuerto de origen esta abierto. @type {boolean} */
  let mostrarDropdownOrigen     = false;

  /** Indica si el dropdown de aeropuerto de destino esta abierto. @type {boolean} */
  let mostrarDropdownDestino    = false;

  /** Indica si el dropdown de aeronave esta abierto. @type {boolean} */
  let mostrarDropdownAvion      = false;

  /** Indica si el dropdown de tripulante esta abierto. @type {boolean} */
  let mostrarDropdownTripulante = false;

  /**
   * Objeto que contiene todos los valores de los campos del formulario para el nuevo vuelo que se esta creando.
   * @type {{
   *   numeroVuelo: string,
   *   aeropuertoOrigenId: string,
   *   aeropuertoDestinoId: string,
   *   avionId: string,
   *   fecha: string,
   *   horaSalida: string,
   *   boletosTurista: string,
   *   boletosEjecutivo: string,
   *   precioTurista: string,
   *   precioEjecutiva: string,
   *   tripulantesSeleccionados: any[]
   * }}
   */
  let nuevoVuelo = {
    numeroVuelo: '',
    aeropuertoOrigenId: '',
    aeropuertoDestinoId: '',
    avionId: '',
    fecha: '',
    horaSalida: '',
    boletosTurista: '',
    boletosEjecutivo: '',
    precioTurista: '',
    precioEjecutiva: '',
    tripulantesSeleccionados: []
  };

  /** Objeto de vista previa de hora de llegada devuelto por el endpoint calcular-llegada de la API. @type {any} */
  let previewLlegada       = null;

  /** Indica si la llamada a la API de vista previa de llegada esta en progreso. @type {boolean} */
  let loadingPreview       = false;

  /** ID del temporizador de debounce para el calculo de vista previa de llegada. @type {any} */
  let previewDebounceTimer = null;

  /**
   * Estado actual de verificacion de existencia de ruta.
   * Valores: null (no verificado), 'checking', 'ok', 'missing'.
   * @type {string|null}
   */
  let rutaExisteStatus     = null;

  /** ID del temporizador de debounce para la verificacion de existencia de ruta. @type {any} */
  let rutaCheckTimer       = null;

  /** Ultimo ID de aeropuerto de origen usado en una verificacion de ruta, para evitar llamadas redundantes. @type {string|null} */
  let lastOrigenId         = null;

  /** Ultimo ID de aeropuerto de destino usado en una verificacion de ruta, para evitar llamadas redundantes. @type {string|null} */
  let lastDestinoId        = null;

  /** Conjunto de IDs de aviones ya asignados a otro vuelo en la fecha seleccionada. @type {Set<number>} */
  let avionesOcupadosIds     = new Set();

  /** Conjunto de IDs de tripulantes ya asignados a otro vuelo en la fecha y hora seleccionadas. @type {Set<number>} */
  let tripulantesOcupadosIds = new Set();

  /** Indica si la carga de disponibilidad de aviones y tripulantes esta en progreso. @type {boolean} */
  let cargandoDisponibilidad = false;

  /** Fecha de hoy formateada como YYYY-MM-DD, usada como la fecha minima permitida de vuelo. @type {string} */
  const hoyStr = new Date().toISOString().split('T')[0];

  /** Indica si el calculo de hora de llegada esta en progreso. @type {boolean} */
  let calculandoLlegada = false;

  /** Indica si el envio del formulario al backend esta en progreso. @type {boolean} */
  let creandoVuelo = false;

  /**
   * Clave de los ultimos parametros con los que se lanzo una peticion de calculo de llegada.
   * Formato: "origenId-destinoId-fecha-horaSalida". Si los parametros actuales coinciden
   * con esta clave, el calculo se omite para evitar peticiones duplicadas provocadas por
   * escrituras secundarias a nuevoVuelo (p.ej. al limpiar avion/tripulante no disponible).
   * @type {string|null}
   */
  let lastPreviewKey = null;

  /** Hora de llegada calculada por el endpoint calcular-llegada, o null si no esta disponible. @type {string|null} */
  let horaLlegadaCalculada = null;

  /** Prefijo de exactamente 4 letras para generar el numero de vuelo (ej: BMAA, GTLA). @type {string} */
  let prefijoVuelo = 'BMAA';

  /** Siguiente numero de secuencia para el prefijo actual, formato "0001". @type {string} */
  let numeroSugerido = '';

  /** Numero de vuelo completo construido del prefijo + numero sugerido. @type {string} */
  $: numeroVueloCompleto = prefijoVuelo && numeroSugerido
    ? `${prefijoVuelo} ${numeroSugerido}`
    : '';

  /** Cuando el prefijo tiene exactamente 4 letras consulta el siguiente numero disponible. */
  $: if (prefijoVuelo && prefijoVuelo.length === 4) {
    obtenerSiguienteNumero();
  }

  /**
   * Formatea el input del numero de vuelo: fuerza 2 letras mayusculas + espacio + digitos.
   * Actualiza nuevoVuelo.numeroVuelo y el valor del elemento input en el lugar.
   * @param {Event} e - El evento de input del campo de texto de numero de vuelo.
   */
  function formatearNumeroVuelo(e) {
    let val = e.target.value.toUpperCase().replace(/[^A-Z0-9 ]/g, '');
    let letras = val.slice(0, 2).replace(/[^A-Z]/g, '');
    let resto  = val.slice(2).replace(/[^0-9]/g, '');
    if (letras.length === 2 && resto.length > 0) {
      nuevoVuelo.numeroVuelo = `${letras} ${resto}`;
    } else {
      nuevoVuelo.numeroVuelo = letras + resto;
    }
    e.target.value = nuevoVuelo.numeroVuelo;
  }

  /**
   * Devuelve verdadero cuando la cadena de fecha tiene 10 caracteres y un ano entre el
   * ano actual y 2099.
   * @param {string} fecha - Cadena de fecha en formato YYYY-MM-DD.
   * @returns {boolean}
   */
  function fechaEsValida(fecha) {
    if (!fecha || fecha.length < 10) return false;
    const year = parseInt(fecha.split('-')[0]);
    const hoy  = new Date();
    return year >= hoy.getFullYear() && year <= 2099;
  }

  /**
   * Devuelve verdadero cuando la cadena de fecha representa una fecha anterior a hoy (medianoche hora local).
   * @param {string} fecha - Cadena de fecha en formato YYYY-MM-DD.
   * @returns {boolean}
   */
  function fechaEsPasada(fecha) {
    if (!fecha) return false;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return new Date(fecha) < hoy;
  }

  // Filtra aeropuertos para el dropdown de origen. Muestra los primeros 5 cuando la consulta tiene menos de 2 caracteres,
  // de lo contrario filtra por nombre, codigo o ciudad, limitado a 10.
  $: aeropuertosFiltradosOrigen = busquedaOrigen.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaOrigen.toLowerCase())
      ).slice(0, 10);

  // Filtra aeropuertos para el dropdown de destino. Muestra los primeros 5 cuando la consulta tiene menos de 2 caracteres,
  // de lo contrario filtra por nombre, codigo o ciudad, limitado a 10.
  $: aeropuertosFiltradosDestino = busquedaDestino.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaDestino.toLowerCase())
      ).slice(0, 10);

  // Filtra aviones por coincidencia de nombre/marca/modelo Y excluye IDs en avionesOcupadosIds.
  $: avionesFiltrados = aviones.filter(a => {
    const coincide =
      a.nombreCompleto.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.marca.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.modelo.toLowerCase().includes(busquedaAvion.toLowerCase());
    return coincide && !avionesOcupadosIds.has(a.id);
  });

  // Filtra tripulantes por coincidencia de nombre/rol, excluye IDs ya seleccionados y ocupados.
  $: tripulantesFiltrados = tripulantes.filter(t => {
    const yaSeleccionado = nuevoVuelo.tripulantesSeleccionados.some(ts => ts.id === t.id);
    const coincide =
      t.nombreCompleto.toLowerCase().includes(busquedaTripulante.toLowerCase()) ||
      t.nombreRol.toLowerCase().includes(busquedaTripulante.toLowerCase());
    return !yaSeleccionado && !tripulantesOcupadosIds.has(t.id) && coincide;
  });

  // Resuelve el objeto del aeropuerto de origen seleccionado desde la lista de aeropuertos.
  $: aeropuertoOrigen  = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoOrigenId));

  // Resuelve el objeto del aeropuerto de destino seleccionado desde la lista de aeropuertos.
  $: aeropuertoDestino = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoDestinoId));

  // Resuelve el objeto del avion seleccionado desde la lista de aviones.
  $: avionSeleccionado = aviones.find(a => a.id === parseInt(nuevoVuelo.avionId));

  // Suma los conteos de asientos turista y ejecutivo para verificar contra la capacidad del avion.
  $: totalBoletosAsignados = (parseInt(nuevoVuelo.boletosTurista)   || 0) +
                             (parseInt(nuevoVuelo.boletosEjecutivo) || 0);

  // Capacidad de pasajeros del avion actualmente seleccionado (0 si no hay ninguno seleccionado).
  $: capacidadAvion    = avionSeleccionado?.capacidadPasajeros ?? 0;

  // Verdadero cuando el total de asientos asignados supera la capacidad del avion.
  $: excedeLimite      = capacidadAvion > 0 && totalBoletosAsignados > capacidadAvion;

  // Porcentaje de capacidad ocupada, limitado a 100, usado para impulsar la barra de progreso de capacidad.
  $: porcentajeOcupado = capacidadAvion > 0
    ? Math.min(100, Math.round(totalBoletosAsignados / capacidadAvion * 100))
    : 0;

  // Verdadero cuando origen, destino, una fecha valida no pasada, y hora de salida estan todos establecidos.
  $: camposListos = !!nuevoVuelo.aeropuertoOrigenId &&
                    !!nuevoVuelo.aeropuertoDestinoId &&
                    fechaEsValida(nuevoVuelo.fecha) &&
                    !fechaEsPasada(nuevoVuelo.fecha) &&
                    !!nuevoVuelo.horaSalida;

  // Cuando se selecciona un avion y aun no se han ingresado conteos de asientos, auto-rellena
  // 25% ejecutivo y 75% turista basado en la capacidad total.
  $: if (avionSeleccionado && !nuevoVuelo.boletosTurista && !nuevoVuelo.boletosEjecutivo) {
    const cap = avionSeleccionado.capacidadPasajeros;
    const eje = Math.floor(cap * 0.25);
    nuevoVuelo.boletosEjecutivo = eje;
    nuevoVuelo.boletosTurista   = cap - eje;
  }

  // Activa una verificacion de existencia de ruta cuando cambia el aeropuerto de origen o destino.
  $: { nuevoVuelo.aeropuertoOrigenId; nuevoVuelo.aeropuertoDestinoId; verificarRutaSiCambioAeropuerto(); }

  // Activa un calculo de vista previa de llegada cuando cambia la fecha o la hora de salida y ambas son validas.
  $: {
    nuevoVuelo.fecha; nuevoVuelo.horaSalida;
    if (fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) actualizarPreviewLlegada();
    else { previewLlegada = null; horaLlegadaCalculada = null; }
  }

  // Activa una verificacion de disponibilidad de aviones y tripulantes cuando cambia la fecha o la hora.
  $: {
    nuevoVuelo.fecha; nuevoVuelo.horaSalida;
    if (fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) cargarDisponibilidad();
    else { avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set(); }
  }

  // Contadores reactivos de composicion de tripulacion por rol.
  $: pilotos    = nuevoVuelo.tripulantesSeleccionados.filter(t => t.rolID === 1).length;
  $: copilotos  = nuevoVuelo.tripulantesSeleccionados.filter(t => t.rolID === 2).length;
  $: auxiliares = nuevoVuelo.tripulantesSeleccionados.filter(t => t.rolID === 3).length;
  $: totalTripulantes = nuevoVuelo.tripulantesSeleccionados.length;
  $: tripulacionCompleta = pilotos >= 1 && copilotos >= 1 && auxiliares >= 3 && totalTripulantes === 5;

  /**
   * Verifica si existe una ruta entre los aeropuertos de origen y destino actualmente seleccionados.
   * Hace debounce de la verificacion por 300ms y solo se ejecuta cuando ambos aeropuertos estan establecidos
   * y al menos uno de ellos cambio desde la ultima verificacion. Establece rutaExisteStatus en 'checking',
   * luego 'ok' o 'missing'.
   */
  function verificarRutaSiCambioAeropuerto() {
    const origenId  = nuevoVuelo.aeropuertoOrigenId;
    const destinoId = nuevoVuelo.aeropuertoDestinoId;
    if (!origenId || !destinoId) { rutaExisteStatus = null; previewLlegada = null; horaLlegadaCalculada = null; lastPreviewKey = null; return; }
    if (origenId === lastOrigenId && destinoId === lastDestinoId) return;
    lastOrigenId = origenId; lastDestinoId = destinoId;
    lastPreviewKey = null; // aeropuertos cambiaron → forzar recalculo
    clearTimeout(rutaCheckTimer);
    rutaCheckTimer = setTimeout(async () => {
      rutaExisteStatus = 'checking';
      try {
        const rc = await fetch(`${API}/api/rutas/existe?origenId=${origenId}&destinoId=${destinoId}`, { credentials: 'include' });
        if (rc.ok) { const { existe } = await rc.json(); rutaExisteStatus = existe ? 'ok' : 'missing'; }
        else rutaExisteStatus = null;
      } catch { rutaExisteStatus = null; }
      if (rutaExisteStatus === 'ok' && fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) {
        // Ya esperamos el debounce de verificarRuta — llamar fetch directo sin segundo debounce
        calcularPreviewLlegada(true);
      } else { previewLlegada = null; horaLlegadaCalculada = null; loadingPreview = false; }
    }, 200);
  }

  /**
   * Activa el calculo de vista previa de llegada solo cuando se ha confirmado que la ruta existe.
   */
  function actualizarPreviewLlegada() {
    if (rutaExisteStatus !== 'ok') return;
    calcularPreviewLlegada();
  }

  /**
   * Calcula la hora de llegada llamando al backend.
   * sinDebounce=true: se usa cuando ya se esperó un debounce externo (verificarRuta),
   * evitando el doble espera de 200ms+200ms=400ms. El default es false (espera 200ms).
   * @param {boolean} sinDebounce
   */
  function calcularPreviewLlegada(sinDebounce = false) {
    const origenId  = parseInt(nuevoVuelo.aeropuertoOrigenId);
    const destinoId = parseInt(nuevoVuelo.aeropuertoDestinoId);
    if (!origenId || !destinoId || isNaN(origenId) || isNaN(destinoId) ||
        !fechaEsValida(nuevoVuelo.fecha) || fechaEsPasada(nuevoVuelo.fecha) ||
        !nuevoVuelo.horaSalida) {
      previewLlegada = null; loadingPreview = false; return;
    }

    // Evitar peticion duplicada si los 4 parametros no cambiaron desde el ultimo calculo.
    const clave = `${origenId}-${destinoId}-${nuevoVuelo.fecha}-${nuevoVuelo.horaSalida}`;
    if (clave === lastPreviewKey) return;

    clearTimeout(previewDebounceTimer);

    const doFetch = async () => {
      if (clave === lastPreviewKey) return;
      lastPreviewKey = clave;
      loadingPreview = true; previewLlegada = null;
      calculandoLlegada = true; horaLlegadaCalculada = null;
      const controller = new AbortController();
      const tid = setTimeout(() => controller.abort(), 8000);
      try {
        const r = await fetch(`${API}/api/rutas/calcular-llegada`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          signal: controller.signal,
          body: JSON.stringify({
            aeropuertoOrigenId:  origenId,
            aeropuertoDestinoId: destinoId,
            fechaSalida: nuevoVuelo.fecha,
            horaSalida:  nuevoVuelo.horaSalida
          })
        });
        if (r.ok) {
          previewLlegada = await r.json();
          horaLlegadaCalculada = previewLlegada?.horaLlegada ?? null;
        } else {
          previewLlegada = null; horaLlegadaCalculada = null;
          lastPreviewKey = null;
        }
      } catch {
        previewLlegada = null; horaLlegadaCalculada = null;
        lastPreviewKey = null;
      }
      finally { clearTimeout(tid); loadingPreview = false; calculandoLlegada = false; }
    };

    if (sinDebounce) {
      doFetch();
    } else {
      previewDebounceTimer = setTimeout(doFetch, 200);
    }
  }

  /**
   * Obtiene los IDs de aviones y tripulantes ya asignados a vuelos en la fecha seleccionada
   * (segun la hora de salida). Elimina cualquier avion o tripulante actualmente seleccionado
   * que quede no disponible. Limpia los conjuntos si la fecha es invalida o en el pasado.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarDisponibilidad() {
    if (!fechaEsValida(nuevoVuelo.fecha) || fechaEsPasada(nuevoVuelo.fecha)) {
      avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set(); return;
    }
    cargandoDisponibilidad = true;
    try {
      const urlAviones = `${API}/api/admin/vuelos/aviones-ocupados?fecha=${nuevoVuelo.fecha}${nuevoVuelo.horaSalida ? `&horaSalida=${nuevoVuelo.horaSalida}` : ''}`;
      const urlTrip    = nuevoVuelo.horaSalida
        ? `${API}/api/admin/vuelos/tripulantes-ocupados?fecha=${nuevoVuelo.fecha}&horaSalida=${nuevoVuelo.horaSalida}`
        : null;
      const [rA, rT] = await Promise.all([
        fetch(urlAviones, { credentials: 'include' }),
        urlTrip ? fetch(urlTrip, { credentials: 'include' }) : Promise.resolve(null)
      ]);
      if (rA.ok) avionesOcupadosIds = new Set(await rA.json());
      if (rT && rT.ok) tripulantesOcupadosIds = new Set(await rT.json());
      if (nuevoVuelo.avionId && avionesOcupadosIds.has(parseInt(nuevoVuelo.avionId))) {
        nuevoVuelo.avionId = ''; nuevoVuelo.boletosTurista = ''; nuevoVuelo.boletosEjecutivo = '';
        busquedaAvion = '';
      }
      nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados
        .filter(t => !tripulantesOcupadosIds.has(t.id));
    } catch(e) { console.error('Error cargando disponibilidad', e); }
    finally { cargandoDisponibilidad = false; }
  }

  /**
   * Establece el aeropuerto de origen en el formulario, actualiza el texto de busqueda y cierra el dropdown.
   * @param {any} a - El objeto de aeropuerto seleccionado del dropdown.
   */
  function seleccionarAeropuertoOrigen(a)  { nuevoVuelo.aeropuertoOrigenId  = a.id; busquedaOrigen  = `${a.codigo} - ${a.nombre}`; mostrarDropdownOrigen  = false; }

  /**
   * Establece el aeropuerto de destino en el formulario, actualiza el texto de busqueda y cierra el dropdown.
   * @param {any} a - El objeto de aeropuerto seleccionado del dropdown.
   */
  function seleccionarAeropuertoDestino(a) { nuevoVuelo.aeropuertoDestinoId = a.id; busquedaDestino = `${a.codigo} - ${a.nombre}`; mostrarDropdownDestino = false; }

  /**
   * Establece el avion en el formulario, actualiza el texto de busqueda, cierra el dropdown y reinicia
   * los campos de conteo de asientos para que puedan auto-rellenarse segun la nueva capacidad del avion.
   * @param {any} a - El objeto de avion seleccionado del dropdown.
   */
  function seleccionarAvion(a)   { nuevoVuelo.avionId = a.id; busquedaAvion = a.nombreCompleto; mostrarDropdownAvion = false; nuevoVuelo.boletosTurista = ''; nuevoVuelo.boletosEjecutivo = ''; }

  /**
   * Agrega un tripulante a la lista de tripulacion seleccionada y reinicia el input de busqueda de tripulantes.
   * @param {any} t - El objeto de tripulante seleccionado del dropdown.
   */
  function agregarTripulante(t)  { nuevoVuelo.tripulantesSeleccionados = [...nuevoVuelo.tripulantesSeleccionados, t]; busquedaTripulante = ''; mostrarDropdownTripulante = false; }

  /**
   * Elimina un tripulante de la lista de tripulacion seleccionada por su ID.
   * @param {number} id - El ID del tripulante a eliminar.
   */
  function quitarTripulante(id)  { nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados.filter(t => t.id !== id); }

  /**
   * Selecciona aleatoriamente 1 Piloto, 1 Copiloto y 3 Auxiliares de los tripulantes disponibles
   * (excluyendo los que ya tienen un vuelo asignado en la fecha/hora seleccionada).
   */
  function autorellenarTripulantes() {
    const disponibles = tripulantes.filter(t => !tripulantesOcupadosIds.has(t.id));
    const pilotos_d    = disponibles.filter(t => t.rolID === 1);
    const copilotos_d  = disponibles.filter(t => t.rolID === 2);
    const auxiliares_d = disponibles.filter(t => t.rolID === 3);

    if (pilotos_d.length < 1)    { mostrarToast('error', 'No hay pilotos disponibles');                      return; }
    if (copilotos_d.length < 1)  { mostrarToast('error', 'No hay copilotos disponibles');                    return; }
    if (auxiliares_d.length < 3) { mostrarToast('error', 'No hay suficientes auxiliares disponibles (mín 3)'); return; }

    const rand = arr => arr[Math.floor(Math.random() * arr.length)];
    const piloto   = rand(pilotos_d);
    const copiloto = rand(copilotos_d);

    const pool  = auxiliares_d.filter(a => a.id !== piloto.id && a.id !== copiloto.id);
    const auxSels = [];
    const usados  = new Set();
    while (auxSels.length < 3) {
      const a = rand(pool);
      if (!usados.has(a.id)) { usados.add(a.id); auxSels.push(a); }
    }

    nuevoVuelo.tripulantesSeleccionados = [piloto, copiloto, ...auxSels];
    mostrarToast('success', 'Tripulación autocompletada: 1P · 1C · 3A');
  }

  /**
   * Reinicia todo el formulario de creacion de vuelo a su estado inicial vacio, incluyendo todos
   * los inputs de busqueda, estados de dropdown, datos de vista previa y conjuntos de disponibilidad.
   */
  function limpiarFormularioVuelo() {
    nuevoVuelo = { numeroVuelo: '', aeropuertoOrigenId: '', aeropuertoDestinoId: '', avionId: '', fecha: '', horaSalida: '', boletosTurista: '', boletosEjecutivo: '', precioTurista: '', precioEjecutiva: '', tripulantesSeleccionados: [] };
    busquedaOrigen = ''; busquedaDestino = ''; busquedaAvion = ''; busquedaTripulante = '';
    previewLlegada = null; rutaExisteStatus = null; lastOrigenId = null; lastDestinoId = null;
    avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set();
    prefijoVuelo = 'BMAA'; numeroSugerido = '';
  }

  /**
   * Consulta el backend por el siguiente numero disponible para prefijoVuelo.
   * Actualiza numeroSugerido con el formato "0001".
   * @async
   */
  async function obtenerSiguienteNumero() {
    if (!prefijoVuelo || prefijoVuelo.length !== 4 || !/^[A-Z]{4}$/.test(prefijoVuelo)) {
      numeroSugerido = ''; return;
    }
    try {
      const r = await fetch(
        `${API}/api/admin/vuelos/siguiente-numero?prefijo=${prefijoVuelo}`,
        { credentials: 'include' }
      );
      if (r.ok) {
        const data = await r.json();
        numeroSugerido = data.siguienteNumero;
      }
    } catch { numeroSugerido = '0001'; }
  }

  /**
   * Valida todos los campos requeridos, verifica que la ruta existe mediante la API, luego envia el nuevo
   * vuelo al backend con POST. Si tiene exito limpia el formulario y despacha 'vueloCreado'. Muestra
   * toasts de error especificos para cada fallo de validacion o error de la API.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearVuelo() {
    if (!numeroVueloCompleto) { mostrarToast('error', 'Ingresa el prefijo del vuelo para generar el número'); return; }
    if (!/^[A-Z]{4} \d{4}$/.test(numeroVueloCompleto)) { mostrarToast('error', 'Formato inválido. El prefijo debe tener exactamente 4 letras (ej: BMAA 0001)'); return; }
    if (!nuevoVuelo.aeropuertoOrigenId)   { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevoVuelo.aeropuertoDestinoId)  { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (!nuevoVuelo.avionId)              { mostrarToast('error', 'Selecciona un avion'); return; }
    if (!fechaEsValida(nuevoVuelo.fecha)) { mostrarToast('error', 'Ingresa una fecha valida'); return; }
    if (fechaEsPasada(nuevoVuelo.fecha))  { mostrarToast('error', 'La fecha del vuelo no puede ser en el pasado'); return; }
    if (!nuevoVuelo.horaSalida)           { mostrarToast('error', 'Ingresa la hora de salida'); return; }
    if (!nuevoVuelo.boletosTurista || parseInt(nuevoVuelo.boletosTurista) < 0)     { mostrarToast('error', 'Ingresa los boletos de clase turista'); return; }
    if (!nuevoVuelo.boletosEjecutivo || parseInt(nuevoVuelo.boletosEjecutivo) < 0) { mostrarToast('error', 'Ingresa los boletos de clase ejecutiva'); return; }
    if (excedeLimite) { mostrarToast('error', `Los boletos (${totalBoletosAsignados}) exceden la capacidad del avion (${capacidadAvion})`); return; }
    if (!nuevoVuelo.precioTurista || !nuevoVuelo.precioEjecutiva) { mostrarToast('error', 'Ingresa los precios de ambas clases'); return; }
    // Validar tripulacion
    if (totalTripulantes !== 5) {
      mostrarToast('error', 'Debe asignar exactamente 5 tripulantes al vuelo');
      return;
    }
    if (pilotos < 1) {
      mostrarToast('error', 'Falta asignar 1 Piloto (Rol 1)');
      return;
    }
    if (copilotos < 1) {
      mostrarToast('error', 'Falta asignar 1 Copiloto (Rol 2)');
      return;
    }
    if (auxiliares < 3) {
      mostrarToast('error', 'Faltan Auxiliares de vuelo — mínimo 3 (Rol 3)');
      return;
    }
    creandoVuelo = true;
    try {
      const rCheck = await fetch(`${API}/api/rutas/existe?origenId=${nuevoVuelo.aeropuertoOrigenId}&destinoId=${nuevoVuelo.aeropuertoDestinoId}`, { credentials: 'include' });
      if (rCheck.ok) { const { existe } = await rCheck.json(); if (!existe) { mostrarToast('error', 'No existe una ruta entre estos aeropuertos. Creala primero en "Gestionar Rutas".'); return; } }
    } catch { }
    try {
      const r = await fetch(`${API}/api/admin/vuelos`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          numeroVuelo:         numeroVueloCompleto,
          aeropuertoOrigenId:  parseInt(nuevoVuelo.aeropuertoOrigenId),
          aeropuertoDestinoId: parseInt(nuevoVuelo.aeropuertoDestinoId),
          avionId:             parseInt(nuevoVuelo.avionId),
          fecha:               nuevoVuelo.fecha,
          horaSalida:          nuevoVuelo.horaSalida,
          boletosTurista:      parseInt(nuevoVuelo.boletosTurista),
          boletosEjecutivo:    parseInt(nuevoVuelo.boletosEjecutivo),
          precioTurista:       parseFloat(nuevoVuelo.precioTurista),
          precioEjecutiva:     parseFloat(nuevoVuelo.precioEjecutiva),
          tripulantesIds:      nuevoVuelo.tripulantesSeleccionados.map(t => t.id)
        })
      });
      if (r.ok) { mostrarToast('success', '¡Vuelo creado exitosamente!'); limpiarFormularioVuelo(); dispatch('vueloCreado'); }
      else { const err = await r.json(); mostrarToast('error', err.message || 'Error al crear el vuelo'); }
    } catch { mostrarToast('error', 'Error de conexion al crear el vuelo'); }
    finally { creandoVuelo = false; }
  }
</script>

<!-- Seccion del formulario para crear un nuevo vuelo con validacion y calculo de llegada -->
<section class="admin-section">
  <h2 class="admin-section__title">Crear Nuevo Vuelo</h2>
  <p class="admin-section__subtitle">Completa todos los datos del vuelo</p>

  <form class="admin-form" on:submit|preventDefault={handleCrearVuelo}>

    <!-- Grupo: numero de vuelo y fecha -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Informacion Basica</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label class="admin-form__label">Prefijo *</label>
          <input type="text" class="admin-form__input"
            bind:value={prefijoVuelo}
            placeholder="BMAA" maxlength="4"
            style="text-transform:uppercase;letter-spacing:2px"
            autocomplete="off"
            on:input={e => { prefijoVuelo = e.target.value.toUpperCase().replace(/[^A-Z]/g,'').slice(0,4); e.target.value = prefijoVuelo; }} />
          <small class="img-hint">4 letras mayúsculas (ej: BMAA, GTLA, USFL)</small>
        </div>
        <div class="admin-form__field">
          <label class="admin-form__label">Numero de Vuelo *</label>
          <input type="text" class="admin-form__input"
            value={numeroVueloCompleto}
            readonly
            placeholder="Escribe el prefijo primero"
            style="background:#f8f9fa;cursor:not-allowed;font-weight:600;letter-spacing:1px" />
          {#if numeroVueloCompleto}
            <small class="img-hint">Auto-incrementado: {numeroVueloCompleto}</small>
          {/if}
        </div>
      </div>
      <div class="admin-form__row" style="margin-top:0.75rem">
        <div class="admin-form__field">
          <label for="cv-fecha" class="admin-form__label">Fecha del Vuelo *</label>
          <input type="date" id="cv-fecha" class="admin-form__input"
            bind:value={nuevoVuelo.fecha} min={hoyStr} required />
          {#if nuevoVuelo.fecha && fechaEsPasada(nuevoVuelo.fecha)}
            <small style="color:#c62828;font-size:.78rem">La fecha no puede ser en el pasado</small>
          {/if}
        </div>
      </div>
    </div>

    <!-- Grupo: seleccion de aeropuertos de origen y destino con verificacion de ruta existente -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Ruta</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-origen" class="admin-form__label">Aeropuerto de Origen *</label>
          <div class="searchable-select">
            <input id="cv-origen" type="text" class="admin-form__input" bind:value={busquedaOrigen}
              on:focus={() => mostrarDropdownOrigen = true}
              on:blur={() => setTimeout(() => mostrarDropdownOrigen = false, 200)}
              placeholder="Buscar aeropuerto..." autocomplete="off" />
            {#if mostrarDropdownOrigen && aeropuertosFiltradosOrigen.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaOrigen.length < 2}<div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>{/if}
                {#each aeropuertosFiltradosOrigen as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarAeropuertoOrigen(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoOrigen}<p class="selected-item"><svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><path d="M2 6l3 3 5-5"/></svg>{aeropuertoOrigen.codigo} — {aeropuertoOrigen.nombre}</p>{/if}
          </div>
        </div>
        <div class="admin-form__field">
          <label for="cv-destino" class="admin-form__label">Aeropuerto de Destino *</label>
          <div class="searchable-select">
            <input id="cv-destino" type="text" class="admin-form__input" bind:value={busquedaDestino}
              on:focus={() => mostrarDropdownDestino = true}
              on:blur={() => setTimeout(() => mostrarDropdownDestino = false, 200)}
              placeholder="Buscar aeropuerto..." autocomplete="off" />
            {#if mostrarDropdownDestino && aeropuertosFiltradosDestino.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaDestino.length < 2}<div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>{/if}
                {#each aeropuertosFiltradosDestino as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarAeropuertoDestino(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoDestino}<p class="selected-item"><svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><path d="M2 6l3 3 5-5"/></svg>{aeropuertoDestino.codigo} — {aeropuertoDestino.nombre}</p>{/if}
          </div>
        </div>
      </div>
    </div>

    <!-- Grupo: hora de salida y preview de hora de llegada calculada con zonas horarias -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Horarios</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-hora" class="admin-form__label">Hora de Salida *</label>
          <input type="time" id="cv-hora" class="admin-form__input" bind:value={nuevoVuelo.horaSalida} required />
          <small class="img-hint">Hora local en el aeropuerto de origen</small>
        </div>

        <div class="admin-form__field">
          <p class="admin-form__label">Hora de Llegada</p>

          {#if rutaExisteStatus === 'checking' || loadingPreview}
            <div class="llegada-preview llegada-preview--loading">
              <div class="llegada-loader">
                <span class="llegada-loader__plane"><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg></span>
                <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                <span class="llegada-loader__text">
                  {rutaExisteStatus === 'checking' ? 'Verificando ruta...' : 'Calculando hora de llegada...'}
                </span>
              </div>
            </div>

          {:else if rutaExisteStatus === 'missing'}
            <div class="llegada-preview--no-ruta">
              <span class="llegada-preview__no-ruta-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><path d="M4.93 4.93l14.14 14.14"/></svg></span>
              <span class="llegada-preview__no-ruta-title">No existe esta ruta</span>
              <small class="llegada-preview__no-ruta-msg">Creala en <strong>Gestionar Rutas</strong> antes de crear el vuelo.</small>
              <button type="button" class="llegada-preview__no-ruta-btn" on:click={() => dispatch('irARutas')}>→ Ir a crear la ruta</button>
            </div>

          {:else if previewLlegada}
            <div class="llegada-preview" class:llegada-preview--tz={previewLlegada.usoZonasHorarias}>
              <span class="llegada-preview__time"><svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style="vertical-align:-3px;margin-right:4px"><path d="M2.5 19h19v2h-19v-2zm7.18-1.73l4.35 1.16 5.31 1.42c.8.21 1.62-.26 1.84-1.06.21-.8-.26-1.62-1.06-1.84l-3.23-.86-2.48-5.46-1.69-.45v5.37L8.19 13.9 7.8 9.17l-1.5-.4c-.4-.11-.8.13-.91.52L4.2 13.6l5.48 3.67z"/></svg>{previewLlegada.horaLlegada}
                {#if previewLlegada.fechaLlegada !== nuevoVuelo.fecha}
                  <span class="llegada-preview__nextday">(+1 dia)</span>
                {/if}
              </span>
              <span class="llegada-preview__meta">{previewLlegada.duracionMinutos} min ·
                {#if previewLlegada.usoZonasHorarias}
                  <span class="tz-badge tz-badge--ok"><svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:3px"><path d="M2 6l3 3 5-5"/></svg>Con zona horaria</span>
                {:else}
                  <span class="tz-badge tz-badge--missing">Sin zona horaria</span>
                {/if}
              </span>
              <small class="llegada-preview__nota">{previewLlegada.nota}</small>
            </div>

          {:else}
            <div class="llegada-preview llegada-preview--empty">
              Se calcula automaticamente al completar origen, destino, fecha y hora de salida
              {#if camposListos}
                <div class="llegada-loader" style="margin-top:.5rem">
                  <span class="llegada-loader__plane"><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/></svg></span>
                  <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                  <span class="llegada-loader__text">Preparando calculo...</span>
                </div>
              {/if}
            </div>
          {/if}
        </div>
      </div>
    </div>

    <!-- Grupo: seleccion de aeronave filtrada por disponibilidad en la fecha elegida -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Aeronave</h3>
      <div class="admin-form__field admin-form__field--full">
        <label for="cv-avion" class="admin-form__label">Seleccionar Avion *</label>
        {#if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && avionesOcupadosIds.size > 0}
          <small class="disponibilidad-hint disponibilidad-hint--info">
            {avionesOcupadosIds.size} avion(es) ya asignado(s) a otro vuelo en esta fecha no aparecen en la lista
          </small>
        {:else if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && !cargandoDisponibilidad}
          <small class="disponibilidad-hint disponibilidad-hint--ok">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><path d="M2 6l3 3 5-5"/></svg>Mostrando aviones disponibles para {nuevoVuelo.fecha}
          </small>
        {/if}
        <div class="searchable-select">
          <input id="cv-avion" type="text" class="admin-form__input" bind:value={busquedaAvion}
            on:focus={() => mostrarDropdownAvion = true}
            on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
            placeholder="Buscar avion..." autocomplete="off" />
          {#if mostrarDropdownAvion && avionesFiltrados.length > 0}
            <div class="searchable-select__dropdown">
              {#each avionesFiltrados.slice(0, 10) as a}
                <button type="button" class="searchable-select__option" on:click={() => seleccionarAvion(a)}>
                  {#if a.imagenBase64}<img src={a.imagenBase64} alt={a.nombreCompleto} class="dropdown-thumb" />{/if}
                  <span class="searchable-select__option-name">{a.nombreCompleto}</span>
                  <span class="searchable-select__option-detail">{a.capacidadPasajeros} pasajeros</span>
                </button>
              {/each}
            </div>
          {:else if mostrarDropdownAvion && avionesFiltrados.length === 0 && aviones.length > 0}
            <div class="searchable-select__dropdown">
              <p class="searchable-select__empty"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:-2px;margin-right:5px"><circle cx="12" cy="12" r="10"/><path d="M4.93 4.93l14.14 14.14"/></svg>Todos los aviones estan ocupados para el {nuevoVuelo.fecha || 'dia seleccionado'}</p>
            </div>
          {/if}
          {#if avionSeleccionado}<p class="selected-item"><svg width="13" height="13" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><path d="M2 6l3 3 5-5"/></svg>{avionSeleccionado.nombreCompleto}</p>{/if}
        </div>
      </div>
    </div>

    <!-- Grupo: distribucion de boletos por clase con barra de capacidad y precios -->
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
              {#if excedeLimite}&nbsp;Excede limite{:else if totalBoletosAsignados === capacidadAvion}&nbsp;<svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:2px"><path d="M2 6l3 3 5-5"/></svg>Completo{/if}
            </span>
          </div>
          <div class="capacidad-bar__track">
            <div class="capacidad-bar__fill" class:capacidad-bar__fill--error={excedeLimite} style="width:{porcentajeOcupado}%"></div>
          </div>
        </div>
      {/if}
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-turista" class="admin-form__label">Boletos Clase Turista *</label>
          <input type="number" id="cv-turista" class="admin-form__input" min="0"
            bind:value={nuevoVuelo.boletosTurista} placeholder="Ej: 180"
            max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
        </div>
        <div class="admin-form__field">
          <label for="cv-ejecutivo" class="admin-form__label">Boletos Clase Ejecutiva *</label>
          <input type="number" id="cv-ejecutivo" class="admin-form__input" min="0"
            bind:value={nuevoVuelo.boletosEjecutivo} placeholder="Ej: 60"
            max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
        </div>
      </div>
      <div class="admin-form__row" style="margin-top:1.5rem">
        <div class="admin-form__field">
          <label for="cv-precio-turista" class="admin-form__label">Precio Turista (USD) *</label>
          <input type="number" id="cv-precio-turista" class="admin-form__input" min="0" step="0.01"
            bind:value={nuevoVuelo.precioTurista} placeholder="Ej: 150.00" required />
        </div>
        <div class="admin-form__field">
          <label for="cv-precio-eje" class="admin-form__label">Precio Ejecutiva (USD) *</label>
          <input type="number" id="cv-precio-eje" class="admin-form__input" min="0" step="0.01"
            bind:value={nuevoVuelo.precioEjecutiva} placeholder="Ej: 300.00" required />
        </div>
      </div>
    </div>

    <!-- Grupo: asignacion de tripulantes disponibles con busqueda y lista de seleccionados -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Tripulacion</h3>
      <div class="admin-form__field admin-form__field--full">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.6rem;">
          <label for="cv-trip" class="admin-form__label" style="margin-bottom:0;">Agregar Tripulantes</label>
          <button type="button" class="btn-autorellenar" on:click={autorellenarTripulantes}
            disabled={tripulantes.length < 5}
            title={tripulantes.length < 5 ? 'No hay suficientes tripulantes disponibles' : 'Seleccionar 1 Piloto, 1 Copiloto y 3 Auxiliares aleatorios'}>
            <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0">
              <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
              <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
              <path d="M8 16H3v5"/>
            </svg>
            Autorellenar tripulantes
          </button>
        </div>
        {#if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && tripulantesOcupadosIds.size > 0}
          <small class="disponibilidad-hint disponibilidad-hint--warn">
            {tripulantesOcupadosIds.size} tripulante(s) ya asignado(s) a otro vuelo y no aparecen en la lista
          </small>
        {:else if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && nuevoVuelo.horaSalida && !cargandoDisponibilidad}
          <small class="disponibilidad-hint disponibilidad-hint--ok">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-1px;margin-right:4px"><path d="M2 6l3 3 5-5"/></svg>Mostrando tripulantes disponibles para {nuevoVuelo.fecha} a las {nuevoVuelo.horaSalida}
          </small>
        {/if}
        <div class="searchable-select">
          <input id="cv-trip" type="text" class="admin-form__input" bind:value={busquedaTripulante}
            on:focus={() => mostrarDropdownTripulante = true}
            on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
            placeholder="Buscar por nombre o rol..." autocomplete="off" />
          {#if mostrarDropdownTripulante && tripulantesFiltrados.length > 0}
            <div class="searchable-select__dropdown">
              {#each tripulantesFiltrados.slice(0, 10) as t}
                <button type="button" class="searchable-select__option" on:click={() => agregarTripulante(t)}>
                  {#if t.imagenBase64}<img src={t.imagenBase64} alt={t.nombreCompleto} class="dropdown-thumb dropdown-thumb--circle" />{/if}
                  <span class="searchable-select__option-name">{t.nombreCompleto}</span>
                  <span class="searchable-select__option-role">{t.nombreRol}</span>
                </button>
              {/each}
            </div>
          {:else if mostrarDropdownTripulante && tripulantesFiltrados.length === 0 && tripulantes.length > 0}
            <div class="searchable-select__dropdown">
              <p class="searchable-select__empty"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:-2px;margin-right:5px"><circle cx="12" cy="12" r="10"/><path d="M4.93 4.93l14.14 14.14"/></svg>Ningun tripulante disponible.<br><small>Deben pasar 24h desde su vuelo anterior.</small></p>
            </div>
          {/if}
        </div>
        {#if nuevoVuelo.tripulantesSeleccionados.length > 0}
          <div class="tripulantes-chips">
            {#each nuevoVuelo.tripulantesSeleccionados as t}
              <span class="t-chip" class:t-chip--piloto={t.rolID === 1} class:t-chip--copiloto={t.rolID === 2} class:t-chip--auxiliar={t.rolID === 3}>
                <span class="t-chip__rol">{t.rolID === 1 ? 'P' : t.rolID === 2 ? 'C' : 'A'}</span>
                <span class="t-chip__nombre">{t.nombreCompleto}</span>
                <button type="button" class="t-chip__remove" on:click={() => quitarTripulante(t.id)} aria-label="Quitar">×</button>
              </span>
            {/each}
          </div>
          <div class="crew-checklist">
            <div class="crew-check-item" class:ok={pilotos >= 1} class:missing={pilotos === 0}>
              <span class="crew-check-icon">{#if pilotos >= 1}<svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 6l3 3 5-5"/></svg>{:else}<svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2"><circle cx="6" cy="6" r="4"/></svg>{/if}</span>
              Piloto: {pilotos}/1
            </div>
            <div class="crew-check-item" class:ok={copilotos >= 1} class:missing={copilotos === 0}>
              <span class="crew-check-icon">{copilotos >= 1 ? '✓' : '○'}</span>
              Copiloto: {copilotos}/1
            </div>
            <div class="crew-check-item" class:ok={auxiliares >= 3} class:missing={auxiliares < 3}>
              <span class="crew-check-icon">{auxiliares >= 3 ? '✓' : '○'}</span>
              Auxiliares: {auxiliares}/3
            </div>
            <div class="crew-check-item" class:ok={totalTripulantes === 5} class:missing={totalTripulantes !== 5}>
              <span class="crew-check-icon">{totalTripulantes === 5 ? '✓' : '○'}</span>
              Total: {totalTripulantes}/5
            </div>
          </div>
        {/if}
      </div>
    </div>

    <!-- Acciones del formulario: crear vuelo o limpiar todos los campos -->
    <div class="admin-form__actions">
      <button type="submit" class="admin-form__submit" disabled={creandoVuelo}>
        {#if creandoVuelo}
          Creando vuelo...
        {:else}
          Crear Vuelo
        {/if}
      </button>
      <button type="button" class="admin-form__cancel" on:click={limpiarFormularioVuelo}>Limpiar</button>
    </div>

  </form>
</section>

<style>
/* ── Igualar con AdminCrearVueloEscalas: inputs, labels, spacing ── */

/* Inputs: border-radius uniforme + borde marrón claro */
.admin-form__input {
  border-radius: 8px !important;
  border: 1.5px solid #c9b99a !important;
  padding: 9px 12px !important;
  font-size: 0.9rem !important;
  background: white !important;
}
.admin-form__input:focus {
  border-color: var(--primary-color, #7a5c3f) !important;
  box-shadow: 0 0 0 3px rgba(122,92,63,0.1) !important;
}

/* Labels: quitar uppercase y letter-spacing excesivo */
.admin-form__label {
  text-transform: none !important;
  letter-spacing: 0 !important;
  font-size: 0.85rem !important;
  font-weight: 600 !important;
  color: #444 !important;
}

/* Títulos de grupo: quitar uppercase */
.admin-form__group-title {
  text-transform: none !important;
  letter-spacing: 0 !important;
  font-size: 0.9rem !important;
  font-weight: 700 !important;
  color: var(--secondary-color, #1a1a2e) !important;
  margin-bottom: 0.75rem !important;
}

/* Grupos: reducir separación y suavizar el divisor */
.admin-form__group {
  padding-bottom: 1.25rem !important;
  border-bottom: 1px solid #e5e0d8 !important;
}
.admin-form__group:last-of-type { border-bottom: none !important; }

/* Subtítulo con borde inferior como en escalas */
.admin-section__subtitle {
  padding-bottom: 1rem !important;
  margin-bottom: 1.5rem !important;
  border-bottom: 2px solid #e5e0d8 !important;
  font-size: 0.9rem !important;
  color: #666 !important;
}

/* Botones de acción */
.admin-form__submit {
  border-radius: 8px !important;
  letter-spacing: 0.5px !important;
  text-transform: none !important;
  font-size: 0.9rem !important;
}
.admin-form__cancel {
  border-radius: 8px !important;
  letter-spacing: 0 !important;
  text-transform: none !important;
  font-size: 0.9rem !important;
}

/* Hints: nunca heredar letter-spacing del campo padre */
.img-hint {
  letter-spacing: normal !important;
  font-size: 0.75rem;
  color: #6c757d;
  margin-top: 3px;
  display: block;
}

/* ── Aviso de cálculo en progreso ── */
.calcular-aviso {
  padding: 10px 14px;
  margin-bottom: 0.75rem;
  background: #fffbeb;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  color: #92400e;
  font-size: 0.82rem;
  font-weight: 500;
}

/* ── Chips de tripulantes seleccionados ── */
.tripulantes-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 0.6rem;
  padding: 8px 10px;
  background: #f9f7f4;
  border: 1px solid #e5e0d8;
  border-radius: 8px;
  min-height: 40px;
}
.t-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px 4px 6px;
  border-radius: 14px;
  font-size: 0.78rem;
  font-weight: 500;
  border: 1.5px solid transparent;
  white-space: nowrap;
}
.t-chip--piloto   { background: #e8f4fd; border-color: #90cdf4; color: #1a4971; }
.t-chip--copiloto { background: #f0fdf4; border-color: #86efac; color: #14532d; }
.t-chip--auxiliar { background: #fdf4ff; border-color: #d8b4fe; color: #581c87; }
.t-chip__rol {
  font-weight: 700;
  font-size: 0.7rem;
  width: 14px;
  text-align: center;
}
.t-chip__nombre {
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.t-chip__remove {
  background: none;
  border: none;
  padding: 0 1px;
  line-height: 1;
  font-size: 1rem;
  opacity: 0.5;
  cursor: pointer;
  transition: opacity 0.15s;
}
.t-chip__remove:hover { opacity: 1; color: #dc2626; }

/* ── Clases propias del componente ── */
.crew-checklist { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.5rem; margin-top: 0.75rem; padding: 0.75rem; background: #f9f9f9; border-radius: 8px; border: 1px solid #eee; }
.crew-check-item { display: flex; align-items: center; gap: 0.4rem; font-size: 0.8rem; padding: 0.4rem 0.6rem; border-radius: 6px; }
.crew-check-item.ok { background: #e6f7ee; color: #1a7a3f; }
.crew-check-item.missing { background: #fef2f2; color: #b91c1c; }
.crew-check-icon { font-weight: 700; }
.btn-autorellenar { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.35rem 0.85rem; background: #f0ebe3; color: #5a3e2a; border: 1.5px solid #c9b99a; border-radius: 8px; font-size: 0.78rem; font-weight: 600; cursor: pointer; transition: background 0.15s, border-color 0.15s; white-space: nowrap; }
.btn-autorellenar:hover:not(:disabled) { background: #e8dfd4; border-color: #a08060; }
.btn-autorellenar:disabled { opacity: 0.45; cursor: not-allowed; }
</style>
