<script>
/**
 * @file Register.svelte
 * @description Full user registration page for Broom AirLine. On mount it clears the form and
 * loads country/city data from countriesnow.space and nationality demonyms plus international
 * dial codes from restcountries.com. Provides autocomplete inputs for country (which also sets
 * the dial code and digit count), city (dependent on selected country), and one or more
 * nationalities with add/remove controls. The phone input uses a dial-code prefix and formats
 * digits based on the selected country's expected count. Password strength is shown live with
 * three requirements (length, uppercase, digit). Before registration, the form calls
 * POST /api/usuarios/verificar to check for duplicate email, username, or passport. On passing,
 * it submits to POST /api/usuarios and redirects to login after 2 seconds.
 */
  import '../styles/Register.css';
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** Registration form data object bound to all form inputs. @type {{correo: string, contrasena: string, confirmPassword: string, pasaporte: string, username: string, nombre: string, apellido: string, telefono: string, fechaNacimiento: string, ciudad: string, pais: string}} */
  let registerData = {
    correo: '',
    contrasena: '',
    confirmPassword: '',
    pasaporte: '',
    username: '',
    nombre: '',
    apellido: '',
    telefono: '',
    fechaNacimiento: '',
    ciudad: '',
    pais: ''
  };

  /** True when the user has checked the terms and conditions checkbox. @type {boolean} */
  let acceptTerms = false;

  /** True when the user has checked the receive promotions checkbox. @type {boolean} */
  let receivePromotions = false;

  /** True when the user has checked the CAPTCHA checkbox. @type {boolean} */
  let captchaVerified = false;

  /** Global submission error message shown below the form. @type {string} */
  let submitError = '';

  /** True after the registration API call succeeds, triggers the success message and redirect. @type {boolean} */
  let submitSuccess = false;

  /** True while the registration API call is in progress. @type {boolean} */
  let submitting = false;

  /** Field-level validation error messages keyed by field name. @type {{correo: string, username: string, pasaporte: string, contrasena: string, pais: string, ciudad: string, nacionalidad: string, telefono: string}} */
  let errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '', telefono: '' };

  // Computed password strength flags for the contrasena field.
  $: ps = {
    length:    registerData.contrasena.length >= 8,
    uppercase: /[A-Z]/.test(registerData.contrasena),
    number:    /[0-9]/.test(registerData.contrasena)
  };

  // True when all three password strength requirements are satisfied.
  $: passwordValid = ps.length && ps.uppercase && ps.number;

  /** All countries with their city lists loaded from countriesnow.space. @type {Array<{country: string, cities: string[]}>} */
  let todosLosPaises = [];

  /** Current text in the country autocomplete input. @type {string} */
  let paisQuery = '';

  /** Country suggestions filtered from todosLosPaises by paisQuery. @type {Array<object>} */
  let paisesSugeridos = [];

  /** The selected country object from todosLosPaises, or null if none selected yet. @type {object|null} */
  let paisSeleccionado = null;

  /** Current text in the city autocomplete input. @type {string} */
  let ciudadQuery = '';

  /** City name suggestions filtered from paisSeleccionado.cities by ciudadQuery. @type {string[]} */
  let ciudadesSugeridas = [];

  /** True once the user has clicked a city suggestion from the dropdown. @type {boolean} */
  let ciudadSeleccionada = false;

  /** Array of nationality text values, one per nationality row (at least one). @type {string[]} */
  let nacionalidades = [''];

  /** Array of suggestion arrays for each nationality input, indexed parallel to nacionalidades. @type {Array<Array<{pais: string, demonym: string}>>} */
  let sugerenciasNac = [[]];

  /** All nationality demonym entries loaded from restcountries.com. @type {Array<{pais: string, demonym: string}>} */
  let todosNacionalidades = [];

  /** Array of booleans indicating whether each nationality has been selected from the suggestions. @type {boolean[]} */
  let nacionalidadesSeleccionadas = [false];

  /** International dial code prefix for the selected country, e.g. '+502'. @type {string} */
  let dialCode = '';

  /** Map of country name (lowercased) to dial code and digit count. @type {Object.<string, {code: string, digits: number}>} */
  let dialCodesMap = {};

  /** Number of local digits required for phone numbers in the selected country. @type {number} */
  let phoneDigitCount = 8;

  /**
   * Static lookup map of international dial codes to expected local digit counts.
   * Keys are dial code strings (e.g. '+502'), values are digit counts.
   * @type {Object.<string, number>}
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
   * Formats a raw digit string into a human-readable local phone number using space-separated
   * groups whose sizes depend on the total expected digit count for the country.
   * @param {string} digits - Raw digit string to format.
   * @param {number} total - Total expected digit count for the country.
   * @returns {string} Formatted phone string with spaces between digit groups.
   */
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  /**
   * Handles the phone input event by stripping non-digits, capping to phoneDigitCount, formatting
   * with formatLocalPhone, and assigning the result to registerData.telefono. Clears errores.telefono.
   * @param {Event} e - The input event from the phone text field.
   */
  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '');
    const capped = raw.slice(0, phoneDigitCount);
    registerData.telefono = formatLocalPhone(capped, phoneDigitCount);
    errores.telefono = '';
  }

  /**
   * Generates a sample placeholder phone number string by formatting a repeated '5' digit string
   * to show the expected format for the current country.
   * @param {number} digits - Total digit count expected for the country.
   * @returns {string} A formatted placeholder string.
   */
  function getPhonePlaceholder(digits) {
    const sample = '5'.repeat(digits);
    return formatLocalPhone(sample, digits);
  }

  onMount(async () => {
    sessionStorage.clear();
    limpiarFormulario();

    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch { console.error('Error cargando paises'); }

    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,demonyms,idd');
      const data = await res.json();

      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1
            ? p.idd.root + suffixes[0]
            : p.idd.root;
          const digits = knownDigits[code] ?? 9;
          const key = p.name.common.toLowerCase();
          dialCodesMap[key] = { code, digits };
          if (p.name.official) dialCodesMap[p.name.official.toLowerCase()] = { code, digits };
        }
      });

      todosNacionalidades = data
        .filter(p => p.demonyms?.eng?.m)
        .map(p => ({ pais: p.name.common, demonym: p.demonyms.eng.m }))
        .sort((a, b) => a.pais.localeCompare(b.pais));
    } catch { console.error('Error cargando nacionalidades / dial codes'); }
  });

  /**
   * Resets all form fields, error messages, autocomplete state, and nationality rows to their
   * initial empty state. Called on mount to ensure a clean form even if the component is reused.
   */
  function limpiarFormulario() {
    registerData = {
      correo: '', contrasena: '', confirmPassword: '', pasaporte: '',
      username: '', nombre: '', apellido: '', telefono: '',
      fechaNacimiento: '', ciudad: '', pais: ''
    };
    acceptTerms = false;
    receivePromotions = false;
    captchaVerified = false;
    submitError = '';
    submitSuccess = false;
    errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '', telefono: '' };
    paisQuery = '';
    ciudadQuery = '';
    paisSeleccionado = null;
    ciudadSeleccionada = false;
    nacionalidades = [''];
    nacionalidadesSeleccionadas = [false];
    sugerenciasNac = [[]];
    paisesSugeridos = [];
    ciudadesSugeridas = [];
    dialCode = '';
    phoneDigitCount = 8;
  }

  /**
   * Handles the correo input event by forcing the value to lowercase before assignment.
   * @param {Event} e - The input event from the correo text field.
   */
  function onCorreoInput(e) {
    registerData.correo = e.target.value.toLowerCase();
  }

  /**
   * Handles the pasaporte input event by stripping all non-numeric characters from the value.
   * @param {Event} e - The input event from the pasaporte text field.
   */
  function onPasaporteInput(e) {
    registerData.pasaporte = e.target.value.replace(/[^0-9]/g, '');
  }

  /**
   * Filters todosLosPaises by the current paisQuery (minimum 2 characters) to populate
   * paisesSugeridos. Clears registerData.pais if the query has changed and no country is selected.
   */
  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (paisQuery && !paisSeleccionado) {
      registerData.pais = '';
      errores.pais = '';
    }
  }

  /**
   * Sets the selected country, updates registerData.pais, resets city state, and resolves the
   * dial code and digit count for the selected country from dialCodesMap.
   * @param {{country: string, cities: string[]}} p - The selected country object.
   */
  function seleccionarPais(p) {
    paisSeleccionado = p;
    paisQuery = p.country;
    registerData.pais = p.country;
    paisesSugeridos = [];
    ciudadQuery = '';
    registerData.ciudad = '';
    ciudadesSugeridas = [];
    ciudadSeleccionada = false;
    errores.pais = '';

    const info = dialCodesMap[p.country.toLowerCase()];
    dialCode = info?.code ?? '';
    phoneDigitCount = info?.digits ?? 9;
    registerData.telefono = '';
    errores.telefono = '';
  }

  /**
   * On blur of the country input, if text was typed but no country was selected from the list,
   * sets errores.pais and clears paisQuery to force a valid selection.
   */
  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) {
      errores.pais = 'Debes seleccionar un pais de la lista';
      paisQuery = '';
    }
  }

  /**
   * Filters the selected country's city list by ciudadQuery (minimum 2 characters) to populate
   * ciudadesSugeridas. Clears registerData.ciudad if the query has changed and no city is selected.
   * Does nothing if no country has been selected yet.
   */
  function onCiudadInput() {
    if (!paisSeleccionado) return;
    const q = ciudadQuery.toLowerCase();
    ciudadesSugeridas = q.length < 2 ? [] : paisSeleccionado.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    if (ciudadQuery && !ciudadSeleccionada) {
      registerData.ciudad = '';
      errores.ciudad = '';
    }
  }

  /**
   * Sets the selected city string into ciudadQuery and registerData.ciudad, marks ciudadSeleccionada
   * as true, clears ciudadesSugeridas, and clears errores.ciudad.
   * @param {string} c - The city name selected from the dropdown.
   */
  function seleccionarCiudad(c) {
    ciudadQuery = c;
    registerData.ciudad = c;
    ciudadesSugeridas = [];
    ciudadSeleccionada = true;
    errores.ciudad = '';
  }

  /**
   * On blur of the city input, if text was typed but no city was selected from the list, sets
   * errores.ciudad and clears ciudadQuery to force a valid selection.
   */
  function validarCiudadSeleccionada() {
    if (ciudadQuery && !ciudadSeleccionada) {
      errores.ciudad = 'Debes seleccionar una ciudad de la lista';
      ciudadQuery = '';
    }
  }

  /**
   * Filters todosNacionalidades by the text at index i in the nacionalidades array (matching
   * country name or demonym) to populate sugerenciasNac[i]. Clears errores.nacionalidad if
   * text is typed without a confirmed selection.
   * @param {number} i - The index of the nationality row to filter suggestions for.
   */
  function onNacInput(i) {
    const q = nacionalidades[i].toLowerCase();
    sugerenciasNac[i] = q.length < 2 ? [] : todosNacionalidades
      .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q))
      .slice(0, 6);
    sugerenciasNac = [...sugerenciasNac];
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errores.nacionalidad = '';
    }
  }

  /**
   * Assigns the chosen demonym string to nacionalidades[i], marks it as selected, clears its
   * suggestion list, and clears errores.nacionalidad.
   * @param {number} i - The index of the nationality row.
   * @param {string} demonym - The demonym string from the selected suggestion.
   */
  function seleccionarNac(i, demonym) {
    nacionalidades[i] = demonym;
    nacionalidades = [...nacionalidades];
    nacionalidadesSeleccionadas[i] = true;
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas];
    sugerenciasNac[i] = [];
    sugerenciasNac = [...sugerenciasNac];
    errores.nacionalidad = '';
  }

  /**
   * On blur of a nationality input, if text was typed at index i but no suggestion was selected,
   * sets errores.nacionalidad and clears the text at that index to force a valid selection.
   * @param {number} i - The index of the nationality row to validate.
   */
  function validarNacionalidadSeleccionada(i) {
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errores.nacionalidad = 'Debes seleccionar una nacionalidad de la lista';
      nacionalidades[i] = '';
      nacionalidades = [...nacionalidades];
    }
  }

  /**
   * Appends a new empty nationality row with an empty suggestion list and an unselected flag
   * to the parallel nacionalidades, sugerenciasNac, and nacionalidadesSeleccionadas arrays.
   */
  function agregarNac() {
    nacionalidades = [...nacionalidades, ''];
    sugerenciasNac = [...sugerenciasNac, []];
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas, false];
  }

  /**
   * Removes the nationality row at index i from all three parallel arrays by filtering out that index.
   * @param {number} i - The index of the nationality row to remove.
   */
  function quitarNac(i) {
    nacionalidades = nacionalidades.filter((_, idx) => idx !== i);
    sugerenciasNac = sugerenciasNac.filter((_, idx) => idx !== i);
    nacionalidadesSeleccionadas = nacionalidadesSeleccionadas.filter((_, idx) => idx !== i);
  }

  /**
   * Validates all form fields, checks for duplicate email/username/passport via
   * POST /api/usuarios/verificar, then submits the registration to POST /api/usuarios.
   * On success sets submitSuccess and schedules navigation to 'login' after 2 seconds.
   * On validation or API failure sets the appropriate errores or submitError messages.
   * @async
   * @returns {Promise<void>}
   */
  async function handleRegister() {
    submitError = '';
    errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '', telefono: '' };

    if (!passwordValid) { errores.contrasena = 'Minimo 8 caracteres, 1 mayuscula y 1 numero.'; return; }
    if (registerData.contrasena !== registerData.confirmPassword) { submitError = 'Las contrasenas no coinciden.'; return; }
    if (!acceptTerms) { submitError = 'Debes aceptar los terminos y condiciones.'; return; }
    if (!captchaVerified) { submitError = 'Confirma que no eres un robot.'; return; }

    if (!paisSeleccionado || !registerData.pais) {
      errores.pais = 'Debes seleccionar un pais de la lista.';
      return;
    }
    if (!ciudadSeleccionada || !registerData.ciudad) {
      errores.ciudad = 'Debes seleccionar una ciudad de la lista.';
      return;
    }

    if (dialCode) {
      const digitosIngresados = registerData.telefono.replace(/\D/g, '').length;
      if (digitosIngresados !== phoneDigitCount) {
        errores.telefono = `Se requieren ${phoneDigitCount} digitos para ${registerData.pais} (ingresaste ${digitosIngresados}).`;
        return;
      }
    } else if (!registerData.telefono.trim()) {
      errores.telefono = 'Ingresa tu numero de telefono.';
      return;
    }

    const nacionalidadesValidas = nacionalidades.filter((n, i) => n.trim() !== '' && nacionalidadesSeleccionadas[i]);
    if (nacionalidadesValidas.length === 0) {
      errores.nacionalidad = 'Debes seleccionar al menos una nacionalidad de la lista.';
      return;
    }

    submitting = true;

    const telefonoCompleto = dialCode
      ? dialCode + ' ' + registerData.telefono.replace(/\s/g, '')
      : registerData.telefono;

    const payload = {
      correo:          registerData.correo,
      contrasena:      registerData.contrasena,
      pasaporte:       registerData.pasaporte,
      username:        registerData.username,
      nombre:          registerData.nombre,
      apellido:        registerData.apellido,
      telefono:        telefonoCompleto,
      fechaNacimiento: registerData.fechaNacimiento,
      ciudad:          registerData.ciudad,
      pais:            registerData.pais,
      nacionalidades:  nacionalidadesValidas
    };

    try {
      const vRes = await fetch(`${API}/api/usuarios/verificar`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      const c = await vRes.json();

      if (c.correoExiste)    errores.correo    = 'Este correo ya esta registrado.';
      if (c.usernameExiste)  errores.username  = 'Este username ya esta en uso.';
      if (c.pasaporteExiste) errores.pasaporte = 'Este pasaporte ya esta registrado.';
      if (c.correoExiste || c.usernameExiste || c.pasaporteExiste) { submitting = false; return; }

      const res = await fetch(`${API}/api/usuarios`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error();

      submitSuccess = true;
      setTimeout(() => navigateTo('login'), 2000);
    } catch {
      submitError = 'No se pudo crear la cuenta. Intenta de nuevo.';
    } finally {
      submitting = false;
    }
  }
</script>

<!-- Contenedor principal de la pagina de registro con layout de dos paneles -->
<div class="register">
  <div class="register__container">
    <div class="register__content">

      <!-- Panel decorativo izquierdo con beneficios de la cuenta Broom AirLine -->
      <div class="register__image-section">
        <div class="register__image-overlay">
          <h2 class="register__image-title">Unete a Broom AirLine</h2>
          <p class="register__image-subtitle">Crea tu cuenta y empieza a disfrutar de vuelos increibles con ofertas exclusivas</p>
          <ul class="register__benefits">
            <li class="register__benefit">Acumula puntos en cada vuelo</li>
            <li class="register__benefit">Acceso a promociones exclusivas</li>
            <li class="register__benefit">Gestion facil de tus reservas</li>
            <li class="register__benefit">Soporte prioritario 24/7</li>
          </ul>
        </div>
      </div>

      <!-- Panel derecho con formulario de registro de nueva cuenta -->
      <div class="register__form-section">
        <div class="register__form-container">
          <button class="register__back" on:click={() => navigateTo('home')}>Volver al inicio</button>

          <!-- Encabezado del formulario con titulo y subtitulo -->
          <div class="register__header">
            <h1 class="register__title">Crear cuenta</h1>
            <p class="register__subtitle">Completa tus datos para registrarte</p>
          </div>

          <!-- Mensaje de exito post-registro o formulario de registro completo -->
          {#if submitSuccess}
            <div class="register-form__success">Cuenta creada exitosamente! Redirigiendo al login...</div>
          {:else}
            <form class="register-form" on:submit|preventDefault={handleRegister}>

              <!-- Campos de datos personales basicos del nuevo usuario -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="nombre" class="register-form__label">Nombre</label>
                  <input type="text" id="nombre" class="register-form__input" bind:value={registerData.nombre} placeholder="Tu nombre" autocomplete="off" required />
                </div>
                <div class="register-form__field">
                  <label for="apellido" class="register-form__label">Apellido</label>
                  <input type="text" id="apellido" class="register-form__input" bind:value={registerData.apellido} placeholder="Tu apellido" autocomplete="off" required />
                </div>
              </div>

              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="reg-username" class="register-form__label">Username</label>
                  <input type="text" id="reg-username" name="reg-username" class="register-form__input {errores.username ? 'register-form__input--error' : ''}" bind:value={registerData.username} placeholder="usuario123" autocomplete="new-password" required />
                  {#if errores.username}<span class="register-form__field-error">{errores.username}</span>{/if}
                </div>
                <div class="register-form__field">
                  <label for="fechaNacimiento" class="register-form__label">Fecha de nacimiento</label>
                  <input type="date" id="fechaNacimiento" class="register-form__input" bind:value={registerData.fechaNacimiento} autocomplete="off" required />
                </div>
              </div>

              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="reg-correo" class="register-form__label">Correo electronico</label>
                  <input type="email" id="reg-correo" name="reg-correo" class="register-form__input {errores.correo ? 'register-form__input--error' : ''}" value={registerData.correo} on:input={onCorreoInput} placeholder="correo@ejemplo.com" autocomplete="new-password" required />
                  {#if errores.correo}<span class="register-form__field-error">{errores.correo}</span>{/if}
                </div>
              </div>

              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="pasaporte" class="register-form__label">Pasaporte (solo numeros)</label>
                  <input type="text" id="pasaporte" class="register-form__input {errores.pasaporte ? 'register-form__input--error' : ''}" value={registerData.pasaporte} on:input={onPasaporteInput} placeholder="12345678" autocomplete="off" required />
                  {#if errores.pasaporte}<span class="register-form__field-error">{errores.pasaporte}</span>{/if}
                </div>
              </div>

              <!-- Autocomplete de pais con lista desplegable de sugerencias -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="paisInput" class="register-form__label">Pais</label>
                  <div class="autocomplete">
                    <input
                      type="text"
                      id="paisInput"
                      class="register-form__input {errores.pais ? 'register-form__input--error' : ''}"
                      bind:value={paisQuery}
                      on:input={onPaisInput}
                      on:blur={validarPaisSeleccionado}
                      placeholder="Escribe tu pais..."
                      autocomplete="off"
                    />
                    {#if paisesSugeridos.length > 0}
                      <ul class="autocomplete__list">
                        {#each paisesSugeridos as p}
                          <li class="autocomplete__item">
                            <button type="button" class="autocomplete__btn" on:click={() => seleccionarPais(p)}>{p.country}</button>
                          </li>
                        {/each}
                      </ul>
                    {/if}
                  </div>
                  {#if errores.pais}<span class="register-form__field-error">{errores.pais}</span>{/if}
                </div>
              </div>

              <!-- Campo de telefono con prefijo internacional determinado por el pais -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="telefono" class="register-form__label">
                    Telefono
                    {#if dialCode}
                      <span class="register-form__label-hint">— {phoneDigitCount} digitos requeridos</span>
                    {/if}
                  </label>
                  <div class="phone-field" class:phone-field--error={errores.telefono}>
                    {#if dialCode}
                      <span class="phone-field__prefix">{dialCode}</span>
                    {/if}
                    <input
                      type="tel"
                      id="telefono"
                      class="register-form__input"
                      bind:value={registerData.telefono}
                      on:input={onPhoneInput}
                      placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un pais primero'}
                      disabled={!dialCode}
                      autocomplete="off"
                    />
                  </div>
                  {#if registerData.telefono && !errores.telefono && dialCode}
                    {@const d = registerData.telefono.replace(/\D/g, '').length}
                    {#if d === phoneDigitCount}
                      <span class="register-form__field-ok">Numero completo</span>
                    {:else}
                      <span class="register-form__field-hint">{d}/{phoneDigitCount} digitos</span>
                    {/if}
                  {/if}
                  {#if errores.telefono}<span class="register-form__field-error">{errores.telefono}</span>{/if}
                </div>
              </div>

              <!-- Autocomplete de ciudad dependiente del pais seleccionado -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="ciudadInput" class="register-form__label">Ciudad</label>
                  <div class="autocomplete">
                    <input
                      type="text"
                      id="ciudadInput"
                      class="register-form__input {errores.ciudad ? 'register-form__input--error' : ''}"
                      bind:value={ciudadQuery}
                      on:input={onCiudadInput}
                      on:blur={validarCiudadSeleccionada}
                      placeholder={paisSeleccionado ? 'Escribe tu ciudad...' : 'Primero selecciona un pais'}
                      disabled={!paisSeleccionado}
                      autocomplete="off"
                    />
                    {#if ciudadesSugeridas.length > 0}
                      <ul class="autocomplete__list">
                        {#each ciudadesSugeridas as c}
                          <li class="autocomplete__item">
                            <button type="button" class="autocomplete__btn" on:click={() => seleccionarCiudad(c)}>{c}</button>
                          </li>
                        {/each}
                      </ul>
                    {/if}
                  </div>
                  {#if errores.ciudad}<span class="register-form__field-error">{errores.ciudad}</span>{/if}
                </div>
              </div>

              <!-- Campo de nacionalidades con soporte para multiples entradas y autocomplete -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <span class="register-form__label">Nacionalidad(es)</span>
                  <div class="nacionalidades-grid">
                    {#each nacionalidades as nac, i}
                      <div class="nacionalidad-item">
                        <span class="nacionalidad-number">Nacionalidad {i + 1}</span>
                        <div class="nacionalidad-input-wrapper">
                          <div class="autocomplete">
                            <input
                              type="text"
                              id="nac-{i}"
                              class="register-form__input"
                              bind:value={nacionalidades[i]}
                              on:input={() => onNacInput(i)}
                              on:blur={() => validarNacionalidadSeleccionada(i)}
                              placeholder="Ej: Guatemalteco"
                              autocomplete="off"
                            />
                            {#if sugerenciasNac[i]?.length > 0}
                              <ul class="autocomplete__list">
                                {#each sugerenciasNac[i] as s}
                                  <li class="autocomplete__item">
                                    <button type="button" class="autocomplete__btn" on:click={() => seleccionarNac(i, s.demonym)}>
                                      {s.pais} — {s.demonym}
                                    </button>
                                  </li>
                                {/each}
                              </ul>
                            {/if}
                          </div>
                          {#if i > 0}
                            <button type="button" class="nacionalidad-remove" on:click={() => quitarNac(i)}>✕</button>
                          {/if}
                        </div>
                      </div>
                    {/each}
                  </div>
                  <button type="button" class="nacionalidad-add" on:click={agregarNac}>Agregar otra nacionalidad</button>
                  {#if errores.nacionalidad}<span class="register-form__field-error">{errores.nacionalidad}</span>{/if}
                </div>
              </div>

              <!-- Campos de contrasena con indicadores de fortaleza en tiempo real -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="reg-contrasena" class="register-form__label">Contrasena</label>
                  <input type="password" id="reg-contrasena" name="reg-contrasena" class="register-form__input {errores.contrasena ? 'register-form__input--error' : ''}" bind:value={registerData.contrasena} placeholder="Minimo 8 caracteres" autocomplete="new-password" required />
                  {#if registerData.contrasena.length > 0}
                    <div class="password-strength">
                      <span class="password-strength__item" class:ok={ps.length}>{ps.length ? '✓' : '✗'} 8 caracteres minimo</span>
                      <span class="password-strength__item" class:ok={ps.uppercase}>{ps.uppercase ? '✓' : '✗'} 1 mayuscula</span>
                      <span class="password-strength__item" class:ok={ps.number}>{ps.number ? '✓' : '✗'} 1 numero</span>
                    </div>
                  {/if}
                  {#if errores.contrasena}<span class="register-form__field-error">{errores.contrasena}</span>{/if}
                </div>
                <div class="register-form__field">
                  <label for="reg-confirmPassword" class="register-form__label">Confirmar contrasena</label>
                  <input type="password" id="reg-confirmPassword" name="reg-confirmPassword" class="register-form__input" bind:value={registerData.confirmPassword} placeholder="Repite tu contrasena" autocomplete="new-password" required />
                  {#if registerData.confirmPassword.length > 0 && registerData.contrasena !== registerData.confirmPassword}
                    <span class="register-form__field-error">Las contrasenas no coinciden.</span>
                  {/if}
                </div>
              </div>

              <!-- Checkboxes de aceptacion de terminos y preferencia de promociones -->
              <div class="register-form__checkboxes">
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={acceptTerms} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">Acepto los terminos y condiciones y la politica de privacidad</span>
                </label>
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={receivePromotions} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">Deseo recibir promociones y ofertas por correo electronico</span>
                </label>
              </div>

              <!-- Widget de verificacion CAPTCHA de no soy un robot -->
              <div class="register-form__captcha">
                <div class="captcha-box">
                  <label class="captcha-box__checkbox">
                    <input type="checkbox" bind:checked={captchaVerified} class="captcha-box__input" />
                    <span class="captcha-box__label">No soy un robot</span>
                  </label>
                  <div class="captcha-box__logo">
                    <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                      <rect width="40" height="40" rx="4" fill="#f1f1f1"/>
                      <circle cx="20" cy="20" r="8" stroke="#4285F4" stroke-width="2"/>
                      <path d="M20 12V20L24 24" stroke="#4285F4" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <span class="captcha-box__text">reCAPTCHA</span>
                  </div>
                </div>
              </div>

              <!-- Mensaje de error global y boton de envio del formulario -->
              {#if submitError}
                <div class="register-form__error">{submitError}</div>
              {/if}

              <button type="submit" class="register-form__submit" disabled={submitting}>
                {submitting ? 'Verificando...' : 'Crear cuenta'}
              </button>

            </form>
          {/if}

          <!-- Enlace de navegacion para usuarios que ya tienen cuenta -->
          <div class="register__login">
            <p class="register__login-text">
              Ya tienes una cuenta?
              <button type="button" class="register__login-link" on:click={() => navigateTo('login')}>Inicia sesion</button>
            </p>
          </div>

        </div>
      </div>
    </div>
  </div>
</div>
