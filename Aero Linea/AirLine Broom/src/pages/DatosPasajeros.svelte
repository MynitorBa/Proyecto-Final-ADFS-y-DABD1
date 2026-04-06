<script>
  // @ts-nocheck
/**
 * @file DatosPasajeros.svelte
 * @description Passenger data entry page shown after a user creates a reservation with pending
 * tickets that have no passenger assigned. On mount it fetches the user's pending reservations,
 * flattens all tickets into a single list, and checks whether all tickets already have a
 * passenger — if so it redirects directly to the seat-selection page. Otherwise it presents
 * a stepped form (one tab per ticket) that collects nombre, apellido, pasaporte (digits only),
 * pais, ciudad, and telefono for each passenger. Country and city fields use autocomplete
 * dropdowns powered by the countriesnow.space API. The phone field dynamically sets the
 * expected digit count and dial code prefix based on the selected country using the
 * restcountries.com API combined with a local knownDigits lookup table. On final submission,
 * all passenger records are sent in parallel PUT requests to the API grouped by reservationId,
 * then the user is forwarded to the seat-selection page with the flight grouping data.
 */

  import '../styles/pasajeros.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  /** Navigation function provided by the app router to switch pages. @type {Function} */
  export let navigateTo;

  import { API } from '../lib/api.js';

  /** ID of the currently authenticated user, read from the session store. @type {number|null} */
  let usuarioId = null;

  /** Unsubscribe handle for the session store subscription. @type {Function} */
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  /** Indicates whether the initial reservation data is still being loaded. @type {boolean} */
  let loading = true;

  /** Error message set when the reservation fetch fails. @type {string|null} */
  let error = null;

  /** Array of pending reservations fetched from the API, filtered to estadoReservaId === 1. @type {Array} */
  let reservacionesPendientes = [];

  /** Flat array of all ticket objects across all pending reservations, each augmented with reservacionId and noReservacion. @type {Array} */
  let todosLosBoletos = [];

  /** Map of boletoId to passenger form data objects containing nombre, apellido, pasaporte, telefono, pais, and ciudad. @type {object} */
  let passengerData = {};

  /** Zero-based index of the ticket currently shown in the stepped form UI. @type {number} */
  let currentPassengerIndex = 0;

  /** Indicates whether the passenger data submission is in progress. @type {boolean} */
  let submitting = false;

  /** Full list of countries fetched from countriesnow.space, each entry containing country name and cities array. @type {Array} */
  let todosLosPaises = [];

  /** Map of boletoId to the current text typed in the country autocomplete input. @type {object} */
  let paisQueries = {};

  /** Map of boletoId to the filtered country suggestion list shown in the dropdown. @type {object} */
  let paisesSugeridos = {};

  /** Map of boletoId to the selected country object (with country name and cities), or null if not yet selected. @type {object} */
  let paisesSeleccionados = {};

  /** Map of boletoId to the current text typed in the city autocomplete input. @type {object} */
  let ciudadQueries = {};

  /** Map of boletoId to the filtered city suggestion list shown in the dropdown. @type {object} */
  let ciudadesSugeridas = {};

  /** Map of boletoId to a boolean indicating whether a city has been confirmed from the suggestion list. @type {object} */
  let ciudadesSeleccionadas = {};

  /** Map of boletoId to the international dial code string (e.g. '+502') for the selected country. @type {object} */
  let dialCodes = {};

  /** Map of boletoId to the expected number of local phone digits for the selected country. @type {object} */
  let phoneDigitCounts = {};

  /** Map of boletoId to phone validation error strings displayed below the phone input. @type {object} */
  let phoneErrors = {};

  /** Map of boletoId to passport validation error strings displayed below the passport input. @type {object} */
  let pasaporteErrors = {};

  /** Map of lowercase country name to its dial code and expected digit count, populated from restcountries.com. @type {object} */
  let dialCodesMap = {};

  /**
   * Lookup table of international dial codes to their standard local digit counts.
   * Used as a fallback when the restcountries API does not provide digit information.
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
   * Formats a string of raw phone digits into a localized display format based on the expected
   * digit count. Groups of digits are separated by spaces using different patterns per length
   * (e.g. 8 digits → XXXX XXXX, 9 digits → XXX XXX XXX, 10 digits → XXX XXX XXXX).
   * @param {string} digits - Raw digit string without spaces.
   * @param {number} total - Expected total number of digits for the selected country.
   * @returns {string} Formatted phone string with spaces inserted.
   */
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  /**
   * Handles phone field input for a specific ticket. Strips non-digits, caps at the expected
   * digit count for the selected country, formats the value using formatLocalPhone, updates
   * passengerData and clears the phone error for that ticket.
   * @param {Event} e - The input event from the phone field.
   * @param {number} boletoId - The ticket ID this phone input belongs to.
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
   * Generates a placeholder phone number string by repeating '5' to the expected digit count
   * and then formatting it with formatLocalPhone to show the spacing pattern.
   * @param {number} digits - The expected number of digits for the selected country.
   * @returns {string} A formatted sample phone placeholder such as "5555 5555".
   */
  function getPhonePlaceholder(digits) {
    const sample = '5'.repeat(digits);
    return formatLocalPhone(sample, digits);
  }

  /**
   * Groups a flat array of ticket objects by vueloId to produce the flight grouping structure
   * required by the seat-selection page. Each group contains flight metadata and a boletos array.
   * @param {Array} boletos - Flat array of ticket objects, each with a vueloId field.
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
   * Lifecycle hook that runs after the component mounts. Redirects to login if unauthenticated.
   * Fetches the country list from countriesnow.space and builds the dialCodesMap from
   * restcountries.com, combining the root and suffix fields with the knownDigits fallback.
   * Then loads pending reservations. Returns the session unsubscribe function for cleanup.
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
   * Fetches all of the user's reservations, filters to pending ones, flattens all tickets,
   * and checks if all tickets already have a passenger assigned. If so, redirects immediately
   * to the seat-selection page with the grouped flight data. Otherwise, initializes the
   * passengerData, autocomplete, dial code, and error maps for each ticket and renders the form.
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

      todosLosBoletos.forEach(boleto => {
        passengerData[boleto.boletoId] = {
          boletoId:  boleto.boletoId,
          nombre:    '',
          apellido:  '',
          pasaporte: '',
          telefono:  '',
          pais:      '',
          ciudad:    ''
        };
        paisQueries[boleto.boletoId]           = '';
        paisesSugeridos[boleto.boletoId]       = [];
        paisesSeleccionados[boleto.boletoId]   = null;
        ciudadQueries[boleto.boletoId]         = '';
        ciudadesSugeridas[boleto.boletoId]     = [];
        ciudadesSeleccionadas[boleto.boletoId] = false;
        dialCodes[boleto.boletoId]             = '';
        phoneDigitCounts[boleto.boletoId]      = 8;
        phoneErrors[boleto.boletoId]           = '';
        pasaporteErrors[boleto.boletoId]       = '';
      });

    } catch (err) {
      console.error('Error cargando reservaciones:', err);
      error = 'No se pudieron cargar las reservaciones pendientes.';
    } finally {
      loading = false;
    }
  }

  /**
   * Handles input changes in the country autocomplete field for a specific ticket.
   * Filters todosLosPaises by case-insensitive match (minimum 2 characters) and shows
   * up to 6 suggestions. Clears passengerData.pais if the user types without selecting.
   * @param {number} boletoId - The ticket ID whose country field changed.
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
   * Confirms a country selection for a specific ticket. Updates paisesSeleccionados,
   * paisQueries, and passengerData.pais; clears city fields; and resolves the dial code and
   * expected digit count from dialCodesMap, resetting the phone field in the process.
   * Triggers reactive updates by spreading all affected maps.
   * @param {number} boletoId - The ticket ID whose country is being selected.
   * @param {object} pais - The selected country object from todosLosPaises, with country and cities.
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
   * Blur handler for the country input. If the user has typed but has not selected a country
   * from the list, resets the query string to empty so no invalid value is stored.
   * @param {number} boletoId - The ticket ID whose country field lost focus.
   */
  function validarPaisSeleccionado(boletoId) {
    if (paisQueries[boletoId] && !paisesSeleccionados[boletoId]) {
      paisQueries[boletoId] = '';
      paisQueries = { ...paisQueries };
    }
  }

  /**
   * Handles input changes in the city autocomplete field for a specific ticket.
   * Only runs if a country has been selected. Filters the selected country's cities array
   * by case-insensitive match (minimum 2 characters), shows up to 6 suggestions, and
   * clears passengerData.ciudad if the user types without selecting.
   * @param {number} boletoId - The ticket ID whose city field changed.
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
   * Confirms a city selection for a specific ticket. Updates ciudadQueries, passengerData.ciudad,
   * and marks ciudadesSeleccionadas as true, then clears the suggestion list.
   * @param {number} boletoId - The ticket ID whose city is being selected.
   * @param {string} ciudad - The city name string that was clicked in the dropdown.
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
   * Blur handler for the city input. If the user has typed but has not selected a city
   * from the list, resets the query string to empty so no invalid value is stored.
   * @param {number} boletoId - The ticket ID whose city field lost focus.
   */
  function validarCiudadSeleccionada(boletoId) {
    if (ciudadQueries[boletoId] && !ciudadesSeleccionadas[boletoId]) {
      ciudadQueries[boletoId] = '';
      ciudadQueries = { ...ciudadQueries };
    }
  }

  /**
   * Advances the stepped form to the next ticket if not already on the last one.
   */
  function handleNext()     { if (currentPassengerIndex < todosLosBoletos.length - 1) currentPassengerIndex++; }

  /**
   * Returns the stepped form to the previous ticket if not already on the first one.
   */
  function handlePrevious() { if (currentPassengerIndex > 0) currentPassengerIndex--; }

  /**
   * Validates and submits all passenger data. Groups tickets by reservacionId, checks that all
   * required fields are filled, validates that pasaporte contains only digits, and verifies that
   * the phone digit count matches the expected count for the selected country. On successful
   * validation, sends parallel PUT requests to /api/reservaciones/:id/pasajeros for each
   * reservation. On full success, builds the flight groups and navigates to 'seleccion-asientos'.
   * On validation or API failure, surfaces an alert and/or focuses the offending passenger tab.
   * @async
   * @returns {Promise<void>}
   */
  async function handleSubmit() {
    submitting = true;
    try {
      const boletosAgrupados = {};
      todosLosBoletos.forEach(boleto => {
        if (!boletosAgrupados[boleto.reservacionId])
          boletosAgrupados[boleto.reservacionId] = [];
        const pd = passengerData[boleto.boletoId];
        const dc = dialCodes[boleto.boletoId] || '';
        const telefonoCompleto = dc ? dc + ' ' + pd.telefono.replace(/\s/g, '') : pd.telefono;
        boletosAgrupados[boleto.reservacionId].push({ ...pd, telefono: telefonoCompleto });
      });

      for (const reservacionId in boletosAgrupados) {
        for (const p of boletosAgrupados[reservacionId]) {
          if (!p.nombre || !p.apellido || !p.pasaporte || !p.telefono || !p.pais || !p.ciudad) {
            alert('Por favor completa todos los campos de todos los pasajeros');
            submitting = false; return;
          }
        }
      }

      for (const boleto of todosLosBoletos) {
        const pasaporte = passengerData[boleto.boletoId].pasaporte;
        if (!/^\d+$/.test(pasaporte)) {
          pasaporteErrors[boleto.boletoId] = 'El pasaporte debe contener solo números.';
          pasaporteErrors = { ...pasaporteErrors };
          currentPassengerIndex = todosLosBoletos.indexOf(boleto);
          submitting = false; return;
        }
      }

      for (const boleto of todosLosBoletos) {
        const dc = dialCodes[boleto.boletoId];
        if (dc) {
          const digitosIngresados = passengerData[boleto.boletoId].telefono.replace(/\D/g, '').length;
          const requeridos = phoneDigitCounts[boleto.boletoId];
          if (digitosIngresados !== requeridos) {
            phoneErrors[boleto.boletoId] = `Se requieren ${requeridos} dígitos (ingresaste ${digitosIngresados}).`;
            phoneErrors = { ...phoneErrors };
            currentPassengerIndex = todosLosBoletos.indexOf(boleto);
            submitting = false; return;
          }
        }
      }

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
   * Formats a date string into a localized date using es-ES locale with two-digit day, month, and year.
   * Returns an empty string if the input is falsy.
   * @param {string|null} d - ISO date string to format.
   * @returns {string} Formatted date such as "15/01/2025" or "".
   */
  function formatDate(d) {
    if (!d) return '';
    return new Date(d).toLocaleDateString('es-ES', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  /**
   * Extracts and returns the HH:MM portion from a time string in HH:MM:SS format.
   * Returns an empty string if the input is falsy.
   * @param {string|null} t - Time string to shorten.
   * @returns {string} The HH:MM portion or "".
   */
  function formatTime(t) {
    if (!t) return '';
    const p = t.split(':');
    return `${p[0]}:${p[1]}`;
  }

  // Reactively resolves the ticket object currently displayed in the stepped form.
  $: currentBoleto    = todosLosBoletos[currentPassengerIndex];

  // True when the user is viewing the first ticket, disables the Previous button.
  $: isFirstPassenger = currentPassengerIndex === 0;

  // True when the user is viewing the last ticket, switches the Next button to the Submit button.
  $: isLastPassenger  = currentPassengerIndex === todosLosBoletos.length - 1;
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

          <!-- Pestanas de navegacion entre boletos del formulario por pasos -->
          <div class="passenger-tabs">
            {#each todosLosBoletos as boleto, index}
              <button
                class="passenger-tab"
                class:passenger-tab--active={index === currentPassengerIndex}
                class:passenger-tab--completed={index < currentPassengerIndex}
                on:click={() => currentPassengerIndex = index}
              >
                <span class="passenger-tab__number">{index + 1}</span>
                <span class="passenger-tab__label">Boleto {index + 1}</span>
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
                  Datos del Pasajero {currentPassengerIndex + 1} de {todosLosBoletos.length}
                </h3>
                <div class="passenger-form-card__content">
                  <div class="form-row">
                    <div class="form-field">
                      <label for="nombre-{currentBoleto.boletoId}" class="form-field__label">Nombre *</label>
                      <input type="text" id="nombre-{currentBoleto.boletoId}" class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].nombre}
                        placeholder="Nombre(s)" autocomplete="off" required />
                    </div>
                    <div class="form-field">
                      <label for="apellido-{currentBoleto.boletoId}" class="form-field__label">Apellido *</label>
                      <input type="text" id="apellido-{currentBoleto.boletoId}" class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].apellido}
                        placeholder="Apellido(s)" autocomplete="off" required />
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-field">
                      <label for="pasaporte-{currentBoleto.boletoId}" class="form-field__label">Número de Pasaporte (solo números) *</label>
                      <input type="text" id="pasaporte-{currentBoleto.boletoId}" class="form-field__input"
                        class:form-field__input--error={pasaporteErrors[currentBoleto.boletoId]}
                        value={passengerData[currentBoleto.boletoId].pasaporte}
                        on:input={(e) => {
                          const raw = e.target.value;
                          const soloNumeros = raw.replace(/[^0-9]/g, '');
                          passengerData[currentBoleto.boletoId].pasaporte = soloNumeros;
                          passengerData = { ...passengerData };
                          pasaporteErrors[currentBoleto.boletoId] = raw !== soloNumeros ? 'Solo se permiten números.' : '';
                          pasaporteErrors = { ...pasaporteErrors };
                        }}
                        placeholder="12345678" autocomplete="off" required />
                      {#if pasaporteErrors[currentBoleto.boletoId]}
                        <span class="form-field__error">{pasaporteErrors[currentBoleto.boletoId]}</span>
                      {/if}
                    </div>
                    <div class="form-field">
                      <label for="pais-{currentBoleto.boletoId}" class="form-field__label">País *</label>
                      <div class="autocomplete">
                        <input type="text" id="pais-{currentBoleto.boletoId}" class="form-field__input"
                          bind:value={paisQueries[currentBoleto.boletoId]}
                          on:input={() => onPaisInput(currentBoleto.boletoId)}
                          on:blur={() => validarPaisSeleccionado(currentBoleto.boletoId)}
                          placeholder="Escribe tu país..." autocomplete="off" required />
                        {#if paisesSugeridos[currentBoleto.boletoId]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each paisesSugeridos[currentBoleto.boletoId] as pais}
                              <li class="autocomplete__item">
                                <button type="button" class="autocomplete__btn"
                                  on:click={() => seleccionarPais(currentBoleto.boletoId, pais)}>
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
                      <label for="telefono-{currentBoleto.boletoId}" class="form-field__label">
                        Teléfono de contacto *
                        {#if dialCodes[currentBoleto.boletoId]}
                          <span class="form-field__label-hint">— {phoneDigitCounts[currentBoleto.boletoId]} dígitos</span>
                        {/if}
                      </label>
                      <div class="phone-field" class:phone-field--error={phoneErrors[currentBoleto.boletoId]}>
                        {#if dialCodes[currentBoleto.boletoId]}
                          <span class="phone-field__prefix">{dialCodes[currentBoleto.boletoId]}</span>
                        {/if}
                        <input type="tel" id="telefono-{currentBoleto.boletoId}" class="form-field__input"
                          bind:value={passengerData[currentBoleto.boletoId].telefono}
                          on:input={(e) => onPhoneInput(e, currentBoleto.boletoId)}
                          placeholder={dialCodes[currentBoleto.boletoId] ? getPhonePlaceholder(phoneDigitCounts[currentBoleto.boletoId]) : 'Selecciona un país primero'}
                          disabled={!dialCodes[currentBoleto.boletoId]}
                          autocomplete="off" required />
                      </div>
                      {#if passengerData[currentBoleto.boletoId]?.telefono && !phoneErrors[currentBoleto.boletoId] && dialCodes[currentBoleto.boletoId]}
                        {@const d = passengerData[currentBoleto.boletoId].telefono.replace(/\D/g, '').length}
                        {@const total = phoneDigitCounts[currentBoleto.boletoId]}
                        {#if d === total}
                          <span class="form-field__ok">✓ Número completo</span>
                        {:else}
                          <span class="form-field__hint">{d}/{total} dígitos</span>
                        {/if}
                      {/if}
                      {#if phoneErrors[currentBoleto.boletoId]}
                        <span class="form-field__error">{phoneErrors[currentBoleto.boletoId]}</span>
                      {/if}
                    </div>
                    <div class="form-field">
                      <label for="ciudad-{currentBoleto.boletoId}" class="form-field__label">Ciudad *</label>
                      <div class="autocomplete">
                        <input type="text" id="ciudad-{currentBoleto.boletoId}" class="form-field__input"
                          bind:value={ciudadQueries[currentBoleto.boletoId]}
                          on:input={() => onCiudadInput(currentBoleto.boletoId)}
                          on:blur={() => validarCiudadSeleccionada(currentBoleto.boletoId)}
                          placeholder={paisesSeleccionados[currentBoleto.boletoId] ? 'Escribe tu ciudad...' : 'Primero selecciona un país'}
                          disabled={!paisesSeleccionados[currentBoleto.boletoId]}
                          autocomplete="off" required />
                        {#if ciudadesSugeridas[currentBoleto.boletoId]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each ciudadesSugeridas[currentBoleto.boletoId] as ciudad}
                              <li class="autocomplete__item">
                                <button type="button" class="autocomplete__btn"
                                  on:click={() => seleccionarCiudad(currentBoleto.boletoId, ciudad)}>
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
