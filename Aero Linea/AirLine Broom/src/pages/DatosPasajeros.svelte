<script>
  // @ts-nocheck
/**
 * @file DatosPasajeros.svelte
 * @description Pagina de ingreso de datos de pasajeros mostrada despues de que un usuario crea una reservacion
 * con boletos pendientes sin pasajero asignado. Al montar obtiene las reservaciones pendientes del usuario,
 * aplana todos los boletos en una sola lista y verifica si todos los boletos ya tienen pasajero asignado;
 * de ser asi redirige directamente a la pagina de seleccion de asientos. De lo contrario presenta un formulario
 * paso a paso (una pestana por boleto) que recopila nombre, apellido, pasaporte (solo digitos), pais, ciudad y
 * telefono para cada pasajero. Los campos de pais y ciudad usan dropdowns de autocompletado impulsados por la
 * API de countriesnow.space. El campo de telefono establece dinamicamente el conteo de digitos esperado y el
 * prefijo de codigo de marcado segun el pais seleccionado usando la API de restcountries.com combinada con una
 * tabla de busqueda knownDigits local. En el envio final, todos los registros de pasajeros se envian en solicitudes
 * PUT paralelas a la API agrupadas por reservacionId, luego el usuario es redirigido a la pagina de seleccion
 * de asientos con los datos de agrupacion de vuelos.
 */

  import '../styles/pasajeros.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  /** Funcion de navegacion proporcionada por el enrutador de la aplicacion para cambiar paginas. @type {Function} */
  export let navigateTo;

  import { API } from '../lib/api.js';

  /** ID del usuario autenticado actualmente, leido del store de sesion. @type {number|null} */
  let usuarioId = null;

  /** Manejador de desuscripcion para la suscripcion al store de sesion. @type {Function} */
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  /** Indica si los datos iniciales de reservacion aun se estan cargando. @type {boolean} */
  let loading = true;

  /** Mensaje de error establecido cuando la obtencion de reservaciones falla. @type {string|null} */
  let error = null;

  /** Arreglo de reservaciones pendientes obtenidas de la API, filtradas a estadoReservaId === 1. @type {Array} */
  let reservacionesPendientes = [];

  /** Arreglo plano de todos los objetos de boleto de todas las reservaciones pendientes, cada uno aumentado con reservacionId y noReservacion. @type {Array} */
  let todosLosBoletos = [];

  /** Mapa de boletoId a objetos de datos del formulario de pasajero que contienen nombre, apellido, pasaporte, telefono, pais y ciudad. @type {object} */
  let passengerData = {};

  /** Numero de pasajeros unicos deducido de total_boletos / unique_vueloIds. @type {number} */
  let cantidadPasajerosUnicos = 0;

  /** Claves sinteticas para cada pasajero unico: ['pax_0', 'pax_1', ...]. @type {string[]} */
  let pasajeroKeys = [];

  /** Indice basado en cero del boleto actualmente mostrado en la UI del formulario paso a paso. @type {number} */
  let currentPassengerIndex = 0;

  /** Indica si el envio de datos de pasajeros esta en progreso. @type {boolean} */
  let submitting = false;

  /** Lista completa de paises obtenida de countriesnow.space, cada entrada contiene nombre de pais y arreglo de ciudades. @type {Array} */
  let todosLosPaises = [];

  /** Mapa de boletoId al texto actual escrito en el input de autocompletado de pais. @type {object} */
  let paisQueries = {};

  /** Mapa de boletoId a la lista filtrada de sugerencias de pais mostrada en el dropdown. @type {object} */
  let paisesSugeridos = {};

  /** Mapa de boletoId al objeto de pais seleccionado (con nombre de pais y ciudades), o null si aun no se ha seleccionado. @type {object} */
  let paisesSeleccionados = {};

  /** Mapa de boletoId al texto actual escrito en el input de autocompletado de ciudad. @type {object} */
  let ciudadQueries = {};

  /** Mapa de boletoId a la lista filtrada de sugerencias de ciudad mostrada en el dropdown. @type {object} */
  let ciudadesSugeridas = {};

  /** Mapa de boletoId a un booleano que indica si se ha confirmado una ciudad de la lista de sugerencias. @type {object} */
  let ciudadesSeleccionadas = {};

  /** Mapa de boletoId a la cadena de codigo de marcado internacional (por ejemplo '+502') para el pais seleccionado. @type {object} */
  let dialCodes = {};

  /** Mapa de boletoId al numero esperado de digitos de telefono local para el pais seleccionado. @type {object} */
  let phoneDigitCounts = {};

  /** Mapa de boletoId a cadenas de error de validacion de telefono mostradas debajo del input de telefono. @type {object} */
  let phoneErrors = {};

  /** Mapa de boletoId a cadenas de error de validacion de pasaporte mostradas debajo del input de pasaporte. @type {object} */
  let pasaporteErrors = {};

  /** Mapa de nombre de pais en minusculas a su codigo de marcado y conteo de digitos esperado, poblado desde restcountries.com. @type {object} */
  let dialCodesMap = {};

  /**
   * Tabla de busqueda de codigos de marcado internacional a sus conteos de digitos locales estandar.
   * Usada como alternativa cuando la API de restcountries no proporciona informacion de digitos.
   * @type {object}
   */
  const knownDigits = {
    '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,
    '+31':9,'+32':9,'+33':9,'+34':9,'+36':9,
    '+39':10,'+40':9,'+41':9,'+43':10,'+44':10,
    '+45':8,'+46':9,'+47':8,'+48':9,'+49':10,
    '+51':9,'+52':10,'+53':8,'+54':10,'+55':11,
    '+56':9,'+57':10,'+58':10,'+60':9,'+61':9,
    '+62':9,'+63':10,'+64':9,'+65':8,'+66':9,
    '+81':10,'+82':10,'+84':9,'+86':11,'+90':10,
    '+91':10,'+92':10,'+93':9,'+94':9,'+95':8,
    '+98':10,'+212':9,'+213':9,'+216':8,'+218':9,
    '+220':7,'+221':9,'+222':8,'+223':8,'+224':9,
    '+225':8,'+226':8,'+227':8,'+228':8,'+229':8,
    '+230':8,'+231':8,'+232':8,'+233':9,'+234':10,
    '+235':8,'+236':8,'+237':9,'+238':7,'+239':7,
    '+240':9,'+241':8,'+242':9,'+243':9,'+244':9,
    '+245':7,'+246':7,'+247':4,'+248':7,'+249':9,
    '+250':9,'+251':9,'+252':8,'+253':8,'+254':9,
    '+255':9,'+256':9,'+257':8,'+258':9,'+260':9,
    '+261':9,'+262':9,'+263':9,'+264':9,'+265':9,
    '+266':8,'+267':8,'+268':8,'+269':7,'+290':4,
    '+291':7,'+297':7,'+298':6,'+299':6,'+350':8,
    '+351':9,'+352':9,'+353':9,'+354':7,'+355':9,
    '+356':8,'+357':8,'+358':9,'+359':9,'+370':8,
    '+371':8,'+372':8,'+373':8,'+374':8,'+375':9,
    '+376':6,'+377':8,'+378':10,'+380':9,'+381':9,
    '+382':8,'+385':9,'+386':8,'+387':8,'+389':8,
    '+420':9,'+421':9,'+423':7,'+500':5,'+501':7,
    '+502':8,'+503':8,'+504':8,'+505':8,'+506':8,
    '+507':8,'+508':6,'+509':8,'+590':9,'+591':8,
    '+592':7,'+593':9,'+594':9,'+595':9,'+596':9,
    '+597':7,'+598':8,'+599':7,'+670':8,'+672':6,
    '+673':7,'+674':7,'+675':8,'+676':7,'+677':7,
    '+678':7,'+679':7,'+680':7,'+681':6,'+682':5,
    '+683':4,'+685':7,'+686':8,'+687':6,'+688':5,
    '+689':8,'+690':4,'+691':7,'+692':7,'+850':10,
    '+852':8,'+853':8,'+855':9,'+856':10,'+880':10,
    '+886':9,'+960':7,'+961':8,'+962':9,'+963':9,
    '+964':10,'+965':8,'+966':9,'+967':9,'+968':8,
    '+970':9,'+971':9,'+972':9,'+973':8,'+974':8,
    '+975':8,'+976':8,'+977':10,'+992':9,'+993':8,
    '+994':9,'+995':9,'+996':9,'+998':9,
  };

  /**
   * Formatea una cadena de digitos de telefono sin procesar en un formato de visualizacion localizado segun el conteo
   * de digitos esperado. Los grupos de digitos se separan por espacios usando diferentes patrones por longitud
   * (por ejemplo 8 digitos - XXXX XXXX, 9 digitos - XXX XXX XXX, 10 digitos - XXX XXX XXXX).
   * @param {string} digits - Cadena de digitos sin procesar sin espacios.
   * @param {number} total - Numero total esperado de digitos para el pais seleccionado.
   * @returns {string} Cadena de telefono formateada con espacios insertados.
   */
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  /**
   * Maneja el input del campo de telefono para un boleto especifico. Elimina los no-digitos, limita al conteo
   * de digitos esperado del pais seleccionado, formatea el valor usando formatLocalPhone, actualiza
   * passengerData y limpia el error de telefono para ese boleto.
   * @param {Event} e - El evento de input del campo de telefono.
   * @param {number} boletoId - El ID del boleto al que pertenece este input de telefono.
   */
  function onPhoneInput(e, boletoId) {
    const raw = e.target.value.replace(/\D/g, '');
    const maxDigits = phoneDigitCounts[boletoId] || 8;
    const capped = raw.slice(0, maxDigits);
    passengerData[boletoId].telefono = formatLocalPhone(capped, maxDigits);
    passengerData = { ...passengerData };
    phoneErrors[boletoId] = '';
    phoneErrors = { ...phoneErrors };
  }

  /**
   * Genera una cadena de placeholder de telefono de muestra repitiendo '5' hasta el conteo de digitos esperado
   * y luego formateandola con formatLocalPhone para mostrar el patron de espaciado.
   * @param {number} digits - El numero esperado de digitos para el pais seleccionado.
   * @returns {string} Una muestra formateada de placeholder de telefono como "5555 5555".
   */
  function getPhonePlaceholder(digits) {
    const sample = '5'.repeat(digits);
    return formatLocalPhone(sample, digits);
  }

  /**
   * Agrupa un arreglo plano de objetos de boleto por vueloId para producir la estructura de agrupacion de vuelos
   * requerida por la pagina de seleccion de asientos. Cada grupo contiene metadata de vuelo y un arreglo de boletos.
   * @param {Array} boletos - Arreglo plano de objetos de boleto, cada uno con un campo vueloId.
   * @returns {Array<{vueloId: number, numeroVuelo: string, avionModelo: string, avionMarca: string, clase: string, boletos: Array}>}
   */
  function construirGruposVuelo(boletos) {
    const mapa = {};
    boletos.forEach(b => {
      if (!mapa[b.vueloId]) {
        mapa[b.vueloId] = {
          vueloId:     b.vueloId,
          numeroVuelo: b.numeroVuelo,
          avionModelo: b.avionModelo,
          avionMarca:  b.avionMarca,
          clase:       b.clase,
          boletos:     []
        };
      }
      mapa[b.vueloId].boletos.push(b);
    });
    return Object.values(mapa);
  }

  /**
   * Hook de ciclo de vida que se ejecuta tras el montaje del componente. Redirige al login si no esta autenticado.
   * Obtiene la lista de paises desde countriesnow.space y construye el dialCodesMap desde
   * restcountries.com, combinando los campos root y suffix con el fallback de knownDigits.
   * Luego carga las reservaciones pendientes. Retorna la funcion de desuscripcion de sesion para limpieza.
   * @async
   * @returns {Promise<Function>}
   */
  onMount(async () => {
    if (!usuarioId) {
      navigateTo('login');
      return;
    }

    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch (err) {
      console.error('Error cargando países:', err);
    }

    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,idd');
      const data = await res.json();
      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root;
          const digits = knownDigits[code] ?? 9;
          const key = p.name.common.toLowerCase();
          dialCodesMap[key] = { code, digits };
          if (p.name.official) dialCodesMap[p.name.official.toLowerCase()] = { code, digits };
        }
      });
    } catch {
      console.error('Error cargando dial codes');
    }

    await cargarReservacionesPendientes();
    return () => unsubscribe();
  });

  /**
   * Obtiene todas las reservaciones del usuario, filtra las pendientes, aplana todos los boletos
   * y verifica si todos ya tienen un pasajero asignado. De ser asi, redirige inmediatamente
   * a la pagina de seleccion de asientos con los grupos de vuelo. De lo contrario, inicializa
   * los mapas de passengerData, autocompletado, codigo de marcacion y errores para cada boleto
   * y muestra el formulario.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarReservacionesPendientes() {
    loading = true;
    error = null;

    try {
      const response = await fetch(`${API}/api/mis-reservaciones`, { credentials: 'include' });
      if (!response.ok) throw new Error('Error al cargar las reservaciones');

      const reservaciones = await response.json();
      reservacionesPendientes = reservaciones.filter(r => r.estadoReservaId === 1);

      todosLosBoletos = [];
      let todosTienenPasajero = true;

      reservacionesPendientes.forEach(reserva => {
        reserva.boletos.forEach(boleto => {
          todosLosBoletos.push({
            ...boleto,
            reservacionId: reserva.reservacionId,
            noReservacion: reserva.noReservacion
          });
          if (!boleto.pasajero || !boleto.pasajero.id) {
            todosTienenPasajero = false;
          }
        });
      });

      if (todosLosBoletos.length === 0) {
        loading = false;
        return;
      }

      if (todosTienenPasajero) {
        const grupos = construirGruposVuelo(todosLosBoletos);
        navigateTo('seleccion-asientos', grupos);
        return;
      }

      // Derivar cantidad de pasajeros unicos: total boletos / legs de vuelo unicos
      const uniqueFlightLegs = new Set(todosLosBoletos.map(b => b.vueloId)).size;
      cantidadPasajerosUnicos = uniqueFlightLegs > 0
        ? Math.round(todosLosBoletos.length / uniqueFlightLegs)
        : todosLosBoletos.length;
      pasajeroKeys = Array.from({ length: cantidadPasajerosUnicos }, (_, i) => `pax_${i}`);

      pasajeroKeys.forEach(key => {
        passengerData[key] = {
          nombre:    '',
          apellido:  '',
          pasaporte: '',
          telefono:  '',
          pais:      '',
          ciudad:    ''
        };
        paisQueries[key]           = '';
        paisesSugeridos[key]       = [];
        paisesSeleccionados[key]   = null;
        ciudadQueries[key]         = '';
        ciudadesSugeridas[key]     = [];
        ciudadesSeleccionadas[key] = false;
        dialCodes[key]             = '';
        phoneDigitCounts[key]      = 8;
        phoneErrors[key]           = '';
        pasaporteErrors[key]       = '';
      });

    } catch (err) {
      console.error('Error cargando reservaciones:', err);
      error = 'No se pudieron cargar las reservaciones pendientes.';
    } finally {
      loading = false;
    }
  }

  /**
   * Maneja los cambios de entrada en el campo de autocompletado de pais para un boleto especifico.
   * Filtra todosLosPaises por coincidencia sin distinguir mayusculas (minimo 2 caracteres) y muestra
   * hasta 6 sugerencias. Limpia passengerData.pais si el usuario escribe sin seleccionar.
   * @param {number} boletoId - El ID del boleto cuyo campo de pais cambio.
   */
  function onPaisInput(boletoId) {
    const q = paisQueries[boletoId].toLowerCase();
    paisesSugeridos[boletoId] = q.length < 2 ? [] :
      todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    paisesSugeridos = { ...paisesSugeridos };
    if (paisQueries[boletoId] && !paisesSeleccionados[boletoId])
      passengerData[boletoId].pais = '';
  }

  /**
   * Confirma la seleccion de un pais para un boleto especifico. Actualiza paisesSeleccionados,
   * paisQueries y passengerData.pais; limpia los campos de ciudad; y resuelve el codigo de
   * marcacion y la cantidad de digitos esperada desde dialCodesMap, reiniciando el campo
   * de telefono en el proceso. Dispara actualizaciones reactivas propagando todos los mapas afectados.
   * @param {number} boletoId - El ID del boleto cuyo pais esta siendo seleccionado.
   * @param {object} pais - El objeto de pais seleccionado de todosLosPaises, con country y cities.
   */
  function seleccionarPais(boletoId, pais) {
    paisesSeleccionados[boletoId]   = pais;
    paisQueries[boletoId]           = pais.country;
    passengerData[boletoId].pais    = pais.country;
    paisesSugeridos[boletoId]       = [];
    ciudadQueries[boletoId]         = '';
    passengerData[boletoId].ciudad  = '';
    ciudadesSugeridas[boletoId]     = [];
    ciudadesSeleccionadas[boletoId] = false;

    const info = dialCodesMap[pais.country.toLowerCase()];
    dialCodes[boletoId]              = info?.code ?? '';
    phoneDigitCounts[boletoId]       = info?.digits ?? 9;
    passengerData[boletoId].telefono = '';
    phoneErrors[boletoId]            = '';

    paisQueries = { ...paisQueries }; paisesSeleccionados = { ...paisesSeleccionados };
    paisesSugeridos = { ...paisesSugeridos }; ciudadQueries = { ...ciudadQueries };
    ciudadesSugeridas = { ...ciudadesSugeridas }; ciudadesSeleccionadas = { ...ciudadesSeleccionadas };
    dialCodes = { ...dialCodes }; phoneDigitCounts = { ...phoneDigitCounts };
    passengerData = { ...passengerData }; phoneErrors = { ...phoneErrors };
  }

  /**
   * Manejador de blur para el campo de pais. Si el usuario escribio pero no selecciono un pais
   * de la lista, restablece la cadena de busqueda a vacio para que no se almacene un valor invalido.
   * @param {number} boletoId - El ID del boleto cuyo campo de pais perdio el foco.
   */
  function validarPaisSeleccionado(boletoId) {
    if (paisQueries[boletoId] && !paisesSeleccionados[boletoId]) {
      paisQueries[boletoId] = '';
      paisQueries = { ...paisQueries };
    }
  }

  /**
   * Maneja los cambios de entrada en el campo de autocompletado de ciudad para un boleto especifico.
   * Solo se ejecuta si ya se selecciono un pais. Filtra el arreglo de ciudades del pais seleccionado
   * por coincidencia sin distinguir mayusculas (minimo 2 caracteres), muestra hasta 6 sugerencias
   * y limpia passengerData.ciudad si el usuario escribe sin seleccionar.
   * @param {number} boletoId - El ID del boleto cuyo campo de ciudad cambio.
   */
  function onCiudadInput(boletoId) {
    if (!paisesSeleccionados[boletoId]) return;
    const q = ciudadQueries[boletoId].toLowerCase();
    ciudadesSugeridas[boletoId] = q.length < 2 ? [] :
      paisesSeleccionados[boletoId].cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    ciudadesSugeridas = { ...ciudadesSugeridas };
    if (ciudadQueries[boletoId] && !ciudadesSeleccionadas[boletoId])
      passengerData[boletoId].ciudad = '';
  }

  /**
   * Confirma la seleccion de una ciudad para un boleto especifico. Actualiza ciudadQueries,
   * passengerData.ciudad y marca ciudadesSeleccionadas como true, luego limpia la lista de sugerencias.
   * @param {number} boletoId - El ID del boleto cuya ciudad esta siendo seleccionada.
   * @param {string} ciudad - El nombre de la ciudad que fue clickeada en el desplegable.
   */
  function seleccionarCiudad(boletoId, ciudad) {
    ciudadQueries[boletoId]         = ciudad;
    passengerData[boletoId].ciudad  = ciudad;
    ciudadesSugeridas[boletoId]     = [];
    ciudadesSeleccionadas[boletoId] = true;
    ciudadQueries = { ...ciudadQueries }; ciudadesSugeridas = { ...ciudadesSugeridas };
    ciudadesSeleccionadas = { ...ciudadesSeleccionadas };
  }

  /**
   * Manejador de blur para el campo de ciudad. Si el usuario escribio pero no selecciono una ciudad
   * de la lista, restablece la cadena de busqueda a vacio para que no se almacene un valor invalido.
   * @param {number} boletoId - El ID del boleto cuyo campo de ciudad perdio el foco.
   */
  function validarCiudadSeleccionada(boletoId) {
    if (ciudadQueries[boletoId] && !ciudadesSeleccionadas[boletoId]) {
      ciudadQueries[boletoId] = '';
      ciudadQueries = { ...ciudadQueries };
    }
  }

  /**
   * Avanza el formulario escalonado al siguiente pasajero unico si no esta en el ultimo.
   */
  function handleNext()     { if (currentPassengerIndex < cantidadPasajerosUnicos - 1) currentPassengerIndex++; }

  /**
   * Regresa el formulario escalonado al pasajero unico anterior si no esta en el primero.
   */
  function handlePrevious() { if (currentPassengerIndex > 0) currentPassengerIndex--; }

  /**
   * Valida y envia todos los datos de los pasajeros. Agrupa los boletos por reservacionId, verifica
   * que todos los campos requeridos esten llenos, valida que pasaporte contenga solo digitos y
   * verifica que la cantidad de digitos del telefono coincida con la esperada para el pais
   * seleccionado. Con validacion exitosa, envia solicitudes PUT en paralelo a
   * /api/reservaciones/:id/pasajeros para cada reservacion. Con exito total, construye los grupos
   * de vuelo y navega a seleccion-asientos. Ante fallo de validacion o de la API, muestra una alerta
   * y enfoca la pestana del pasajero con error.
   * @async
   * @returns {Promise<void>}
   */
  async function handleSubmit() {
    submitting = true;
    try {
      // Validar que todos los campos de los pasajeros unicos esten completos
      for (const key of pasajeroKeys) {
        const p = passengerData[key];
        if (!p.nombre || !p.apellido || !p.pasaporte || !p.telefono || !p.pais || !p.ciudad) {
          alert('Por favor completa todos los campos de todos los pasajeros');
          submitting = false; return;
        }
      }

      // Validar pasaporte (solo digitos) para cada pasajero unico
      for (let i = 0; i < pasajeroKeys.length; i++) {
        const key = pasajeroKeys[i];
        if (!/^\d+$/.test(passengerData[key].pasaporte)) {
          pasaporteErrors[key] = 'El pasaporte debe contener solo números.';
          pasaporteErrors = { ...pasaporteErrors };
          currentPassengerIndex = i;
          submitting = false; return;
        }
      }

      // Validar telefono para cada pasajero unico
      for (let i = 0; i < pasajeroKeys.length; i++) {
        const key = pasajeroKeys[i];
        const dc = dialCodes[key];
        if (dc) {
          const digitosIngresados = passengerData[key].telefono.replace(/\D/g, '').length;
          const requeridos = phoneDigitCounts[key];
          if (digitosIngresados !== requeridos) {
            phoneErrors[key] = `Se requieren ${requeridos} dígitos (ingresaste ${digitosIngresados}).`;
            phoneErrors = { ...phoneErrors };
            currentPassengerIndex = i;
            submitting = false; return;
          }
        }
      }

      // Mapear cada boleto a su pasajero unico usando modulo y agrupar por reservacionId
      const boletosAgrupados = {};
      todosLosBoletos.forEach((boleto, idx) => {
        if (!boletosAgrupados[boleto.reservacionId])
          boletosAgrupados[boleto.reservacionId] = [];
        const key = pasajeroKeys[idx % cantidadPasajerosUnicos];
        const pd = passengerData[key];
        const dc = dialCodes[key] || '';
        const telefonoCompleto = dc ? dc + ' ' + pd.telefono.replace(/\s/g, '') : pd.telefono;
        boletosAgrupados[boleto.reservacionId].push({
          boletoId: boleto.boletoId,
          nombre:    pd.nombre,
          apellido:  pd.apellido,
          pasaporte: pd.pasaporte,
          telefono:  telefonoCompleto,
          pais:      pd.pais,
          ciudad:    pd.ciudad
        });
      });

      const promises = Object.entries(boletosAgrupados).map(([reservacionId, body]) =>
        fetch(`${API}/api/reservaciones/${reservacionId}/pasajeros`, {
          method: 'PUT', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        })
      );

      const responses = await Promise.all(promises);
      const allSuccess = responses.every(r => r.ok);

      if (allSuccess) {
        const grupos = construirGruposVuelo(todosLosBoletos);
        navigateTo('seleccion-asientos', grupos);
      } else {
        for (const res of responses) {
          if (!res.ok) {
            try {
              const errData = await res.json();
              alert(errData.message || errData.mensaje || 'Error al procesar los datos.');
            } catch {
              alert('Hubo un error al procesar algunos datos. Por favor intenta de nuevo.');
            }
            break;
          }
        }
      }
    } catch (err) {
      console.error('Error al enviar datos de pasajeros:', err);
      alert('Error al enviar los datos. Por favor intenta de nuevo.');
    } finally {
      submitting = false;
    }
  }

  /**
   * Formatea una cadena de fecha en una fecha localizada usando el locale es-ES con dia, mes y
   * anio de dos digitos. Retorna una cadena vacia si el input es falsy.
   * @param {string|null} d - Cadena de fecha ISO a formatear.
   * @returns {string} Fecha formateada como "15/01/2025" o "".
   */
  function formatDate(d) {
    if (!d) return '';
    return new Date(d).toLocaleDateString('es-ES', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  /**
   * Extrae y retorna la porcion HH:MM de una cadena de hora en formato HH:MM:SS.
   * Retorna una cadena vacia si el input es falsy.
   * @param {string|null} t - Cadena de hora a acortar.
   * @returns {string} La porcion HH:MM o "".
   */
  function formatTime(t) {
    if (!t) return '';
    const p = t.split(':');
    return `${p[0]}:${p[1]}`;
  }

  // Clave sintetica del pasajero unico actualmente visible en el formulario.
  $: currentPaxKey    = pasajeroKeys[currentPassengerIndex] ?? 'pax_0';

  // Primer boleto de este pasajero (para mostrar info del vuelo representativo).
  $: currentBoleto    = todosLosBoletos[currentPassengerIndex];

  // Verdadero cuando el usuario ve el primer pasajero, deshabilita el boton Anterior.
  $: isFirstPassenger = currentPassengerIndex === 0;

  // Verdadero cuando el usuario ve el ultimo pasajero, cambia el boton Siguiente por el de Enviar.
  $: isLastPassenger  = currentPassengerIndex === cantidadPasajerosUnicos - 1;
</script>

<!-- Contenedor principal del formulario de datos de pasajeros -->
<div class="datos-pasajeros">
  <div class="datos-pasajeros__container">
    <!-- Encabezado con titulo y descripcion del paso de datos de pasajeros -->
    <div class="datos-pasajeros__header">
      <button class="datos-pasajeros__back" on:click={() => navigateTo('home')}>
        Volver al inicio
      </button>
      <h1 class="datos-pasajeros__title">Datos de los pasajeros</h1>
      <p class="datos-pasajeros__subtitle">
        Completa la información de todos los pasajeros para tus reservaciones pendientes
      </p>
    </div>

    <!-- Area de contenido: carga, error, carrito vacio o formulario por pasos -->
    <div class="datos-pasajeros__content">
      {#if loading}
        <div class="datos-pasajeros__loading">Cargando reservaciones pendientes...</div>

      {:else if error}
        <div class="datos-pasajeros__error">{error}</div>

      {:else if todosLosBoletos.length === 0}
        <div class="datos-pasajeros__empty">
          <p>No tienes reservaciones pendientes por completar.</p>
          <button class="action-btn action-btn--primary" on:click={() => navigateTo('home')}>
            Buscar Vuelos
          </button>
        </div>

      {:else}
        <div class="datos-pasajeros__main">
          <div class="datos-pasajeros__notice">
            <h3 class="notice__title">Información importante</h3>
            <ul class="notice__list">
              <li class="notice__item">Los nombres deben coincidir exactamente con el pasaporte</li>
              <li class="notice__item">Verifica que los datos sean correctos antes de enviar</li>
              <li class="notice__item">Tienes 10 minutos para completar los datos antes de que expire la reserva</li>
            </ul>
          </div>

          <!-- Pestanas de navegacion entre pasajeros unicos del formulario por pasos -->
          <div class="passenger-tabs">
            {#each pasajeroKeys as paxKey, index}
              <button
                class="passenger-tab"
                class:passenger-tab--active={index === currentPassengerIndex}
                class:passenger-tab--completed={index < currentPassengerIndex}
                on:click={() => currentPassengerIndex = index}
              >
                <span class="passenger-tab__number">{index + 1}</span>
                <span class="passenger-tab__label">Pasajero {index + 1}</span>
              </button>
            {/each}
          </div>

          <!-- Formulario escalonado con datos del pasajero actual y controles de navegacion -->
          <form class="passengers-form" on:submit|preventDefault={handleSubmit}>
            <section class="flight-passengers-section">
              <div class="flight-passengers-section__header">
                <h2 class="flight-passengers-section__title">
                  Vuelo {currentBoleto.numeroVuelo} - {currentBoleto.clase}
                </h2>
                <p class="flight-passengers-section__info">
                  {currentBoleto.origenCiudad} ({currentBoleto.origenCodigo}) → {currentBoleto.destinoCiudad} ({currentBoleto.destinoCodigo})
                </p>
                <p class="flight-passengers-section__info">
                  {formatDate(currentBoleto.fechaVuelo)} | Salida: {formatTime(currentBoleto.horaSalida)} - Llegada: {formatTime(currentBoleto.horaLlegada)}
                </p>
                <p class="flight-passengers-section__info">
                  <strong>Reservación:</strong> {currentBoleto.noReservacion} |
                  <strong>Boleto:</strong> {currentBoleto.noBoleto} |
                  <strong>Asiento:</strong> {currentBoleto.noAsiento}
                </p>
              </div>

              <article class="passenger-form-card">
                <h3 class="passenger-form-card__title">
                  Datos del Pasajero {currentPassengerIndex + 1} de {cantidadPasajerosUnicos}
                </h3>
                <div class="passenger-form-card__content">
                  <div class="form-row">
                    <div class="form-field">
                      <label for="nombre-{currentPaxKey}" class="form-field__label">Nombre *</label>
                      <input type="text" id="nombre-{currentPaxKey}" class="form-field__input"
                        bind:value={passengerData[currentPaxKey].nombre}
                        placeholder="Nombre(s)" autocomplete="off" required />
                    </div>
                    <div class="form-field">
                      <label for="apellido-{currentPaxKey}" class="form-field__label">Apellido *</label>
                      <input type="text" id="apellido-{currentPaxKey}" class="form-field__input"
                        bind:value={passengerData[currentPaxKey].apellido}
                        placeholder="Apellido(s)" autocomplete="off" required />
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-field">
                      <label for="pasaporte-{currentPaxKey}" class="form-field__label">Número de Pasaporte (solo números) *</label>
                      <input type="text" id="pasaporte-{currentPaxKey}" class="form-field__input"
                        class:form-field__input--error={pasaporteErrors[currentPaxKey]}
                        value={passengerData[currentPaxKey].pasaporte}
                        on:input={(e) => {
                          const raw = e.target.value;
                          const soloNumeros = raw.replace(/[^0-9]/g, '');
                          passengerData[currentPaxKey].pasaporte = soloNumeros;
                          passengerData = { ...passengerData };
                          pasaporteErrors[currentPaxKey] = raw !== soloNumeros ? 'Solo se permiten números.' : '';
                          pasaporteErrors = { ...pasaporteErrors };
                        }}
                        placeholder="12345678" autocomplete="off" required />
                      {#if pasaporteErrors[currentPaxKey]}
                        <span class="form-field__error">{pasaporteErrors[currentPaxKey]}</span>
                      {/if}
                    </div>
                    <div class="form-field">
                      <label for="pais-{currentPaxKey}" class="form-field__label">País *</label>
                      <div class="autocomplete">
                        <input type="text" id="pais-{currentPaxKey}" class="form-field__input"
                          bind:value={paisQueries[currentPaxKey]}
                          on:input={() => onPaisInput(currentPaxKey)}
                          on:blur={() => validarPaisSeleccionado(currentPaxKey)}
                          placeholder="Escribe tu país..." autocomplete="off" required />
                        {#if paisesSugeridos[currentPaxKey]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each paisesSugeridos[currentPaxKey] as pais}
                              <li class="autocomplete__item">
                                <button type="button" class="autocomplete__btn"
                                  on:click={() => seleccionarPais(currentPaxKey, pais)}>
                                  {pais.country}
                                </button>
                              </li>
                            {/each}
                          </ul>
                        {/if}
                      </div>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-field">
                      <label for="telefono-{currentPaxKey}" class="form-field__label">
                        Teléfono de contacto *
                        {#if dialCodes[currentPaxKey]}
                          <span class="form-field__label-hint">— {phoneDigitCounts[currentPaxKey]} dígitos</span>
                        {/if}
                      </label>
                      <div class="phone-field" class:phone-field--error={phoneErrors[currentPaxKey]}>
                        {#if dialCodes[currentPaxKey]}
                          <span class="phone-field__prefix">{dialCodes[currentPaxKey]}</span>
                        {/if}
                        <input type="tel" id="telefono-{currentPaxKey}" class="form-field__input"
                          bind:value={passengerData[currentPaxKey].telefono}
                          on:input={(e) => onPhoneInput(e, currentPaxKey)}
                          placeholder={dialCodes[currentPaxKey] ? getPhonePlaceholder(phoneDigitCounts[currentPaxKey]) : 'Selecciona un país primero'}
                          disabled={!dialCodes[currentPaxKey]}
                          autocomplete="off" required />
                      </div>
                      {#if passengerData[currentPaxKey]?.telefono && !phoneErrors[currentPaxKey] && dialCodes[currentPaxKey]}
                        {@const d = passengerData[currentPaxKey].telefono.replace(/\D/g, '').length}
                        {@const total = phoneDigitCounts[currentPaxKey]}
                        {#if d === total}
                          <span class="form-field__ok">✓ Número completo</span>
                        {:else}
                          <span class="form-field__hint">{d}/{total} dígitos</span>
                        {/if}
                      {/if}
                      {#if phoneErrors[currentPaxKey]}
                        <span class="form-field__error">{phoneErrors[currentPaxKey]}</span>
                      {/if}
                    </div>
                    <div class="form-field">
                      <label for="ciudad-{currentPaxKey}" class="form-field__label">Ciudad *</label>
                      <div class="autocomplete">
                        <input type="text" id="ciudad-{currentPaxKey}" class="form-field__input"
                          bind:value={ciudadQueries[currentPaxKey]}
                          on:input={() => onCiudadInput(currentPaxKey)}
                          on:blur={() => validarCiudadSeleccionada(currentPaxKey)}
                          placeholder={paisesSeleccionados[currentPaxKey] ? 'Escribe tu ciudad...' : 'Primero selecciona un país'}
                          disabled={!paisesSeleccionados[currentPaxKey]}
                          autocomplete="off" required />
                        {#if ciudadesSugeridas[currentPaxKey]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each ciudadesSugeridas[currentPaxKey] as ciudad}
                              <li class="autocomplete__item">
                                <button type="button" class="autocomplete__btn"
                                  on:click={() => seleccionarCiudad(currentPaxKey, ciudad)}>
                                  {ciudad}
                                </button>
                              </li>
                            {/each}
                          </ul>
                        {/if}
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            </section>

            <div class="passengers-form__navigation">
              <button type="button" class="passengers-form__btn-prev"
                on:click={handlePrevious} disabled={isFirstPassenger}>
                Anterior
              </button>
              {#if isLastPassenger}
                <button type="submit" class="passengers-form__btn-submit" disabled={submitting}>
                  {submitting ? 'Enviando...' : 'Confirmar Datos'}
                </button>
              {:else}
                <button type="button" class="passengers-form__btn-next" on:click={handleNext}>
                  Siguiente
                </button>
              {/if}
            </div>
          </form>
        </div>

        <!-- Sidebar con resumen de reservaciones pendientes y datos de contacto de soporte -->
        <aside class="datos-pasajeros__sidebar">
          <div class="booking-recap">
            <h2 class="booking-recap__title">Resumen de reservaciones pendientes</h2>
            <div class="booking-recap__flights">
              {#each reservacionesPendientes as reserva}
                <div class="recap-reservation">
                  <div class="recap-reservation__header">
                    <strong>Reservación:</strong> {reserva.noReservacion}
                  </div>
                  <div class="recap-reservation__total">Total: $ {reserva.total.toFixed(2)}</div>
                  <div class="recap-reservation__boletos">
                    {reserva.boletos.length} boleto{reserva.boletos.length > 1 ? 's' : ''}
                  </div>
                </div>
              {/each}
            </div>
            <div class="booking-recap__divider"></div>
            <div class="booking-recap__help">
              <h3 class="booking-recap__help-title">¿Necesitas ayuda?</h3>
              <p class="booking-recap__help-text">Contáctanos al +502 2345-6789</p>
            </div>
          </div>
        </aside>
      {/if}
    </div>
  </div>
</div>
