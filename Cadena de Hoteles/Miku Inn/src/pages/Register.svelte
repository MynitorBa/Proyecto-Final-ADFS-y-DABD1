<script>
  /**
   * @file Register.svelte
   * @description Formulario de registro de nuevos usuarios de Miku Inn. Recoge
   * informacion personal (nombre, apellidos, fecha de nacimiento, pasaporte,
   * pais, ciudad, telefono, nacionalidades), credenciales de acceso (username,
   * correo y contrasena) y verifica la aceptacion de terminos con un captcha
   * simulado antes de enviar el registro al backend.
   */

  import logo from '../assets/mikuinn-logo.png';
  import { API } from '../lib/api.js';
  import {
    validatePassword,
    getPasswordStrength,
    calculateAge,
    formatLocalPhone,
    getPhonePlaceholder,
    validateForm,
  } from '../lib/registerUtils.js';

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo;
  import '../styles/register.css';
  import { onMount } from 'svelte';

  /**
   * Objeto con todos los campos del formulario de registro.
   * @type {{ firstName: string, lastName: string, birthDate: string, phone: string,
   *          pasaporte: string, country: string, city: string, username: string,
   *          email: string, password: string, confirmPassword: string }}
   */
  let formData = {
    firstName: '',
    lastName: '',
    birthDate: '',
    phone: '',
    pasaporte: '',
    country: '',
    city: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  /** Alterna la visibilidad del campo de contrasena. @type {boolean} */
  let showPassword = false;

  /** Alterna la visibilidad del campo de confirmacion de contrasena. @type {boolean} */
  let showConfirmPassword = false;

  /** Indica si el usuario acepto los terminos y condiciones. @type {boolean} */
  let acceptTerms = false;

  /** Indica si el usuario acepto la politica de privacidad. @type {boolean} */
  let acceptPrivacy = false;

  /** Indica si el usuario desea recibir ofertas y promociones. @type {boolean} */
  let recibirOfertas = false;

  /**
   * Preferencias de viaje cuando el usuario opta por recibir ofertas.
   * @type {{ tiposHabitacion: string[], presupuesto: string, combinacion: string, personasExtra: number }}
   */
  let preferencias = {
    tiposHabitacion: [],
    presupuesto: 'Estándar',
    combinacion: 'Una habitación',
    personasExtra: 1
  };

  /** Tipos de habitacion disponibles para la seleccion de preferencias. */
  const tipoHabOpciones = ['Doble', 'Junior Suite', 'Suite', 'Gran Suite'];

  /** Alterna la seleccion de un tipo de habitacion en las preferencias. */
  function toggleTipoHab(tipo) {
    if (preferencias.tiposHabitacion.includes(tipo)) {
      preferencias.tiposHabitacion = preferencias.tiposHabitacion.filter(t => t !== tipo);
    } else {
      preferencias.tiposHabitacion = [...preferencias.tiposHabitacion, tipo];
    }
  }

  /** Mapa de errores de validacion por campo. @type {Object.<string, string>} */
  let errors = {};

  /** Indica si la peticion de registro esta en vuelo. @type {boolean} */
  let isSubmitting = false;

  /** Indica si el registro fue completado con exito. @type {boolean} */
  let registrationSuccess = false;

  /** Indica si el captcha fue verificado. @type {boolean} */
  let captchaVerified = false;

  /** Indica si la verificacion del captcha esta en proceso. @type {boolean} */
  let captchaLoading = false;

  /** Indica si hubo un error en la verificacion del captcha. @type {boolean} */
  let captchaError = false;

  /** Lista completa de paises cargada desde countriesnow. @type {any[]} */
  let todosLosPaises = [];

  /** Texto escrito en el campo de busqueda de pais. @type {string} */
  let paisQuery = '';

  /** Sugerencias filtradas para el autocomplete de pais. @type {any[]} */
  let paisesSugeridos = [];

  /** Objeto del pais seleccionado del autocomplete. @type {any} */
  let paisSeleccionado = null;

  /** Texto escrito en el campo de busqueda de ciudad. @type {string} */
  let ciudadQuery = '';

  /** Sugerencias filtradas para el autocomplete de ciudad. @type {string[]} */
  let ciudadesSugeridas = [];

  /** Indica si una ciudad fue seleccionada formalmente del autocomplete. @type {boolean} */
  let ciudadSeleccionada = false;

  /** Array de nacionalidades que el usuario esta ingresando (minimo una). @type {string[]} */
  let nacionalidades = [''];

  /** Sugerencias por indice para cada campo de nacionalidad. @type {any[][]} */
  let sugerenciasNac = [[]];

  /** Lista de demonimos y paises cargada desde restcountries. @type {any[]} */
  let todosNacionalidades = [];

  /** Indica si cada entrada de nacionalidad fue seleccionada del autocomplete. @type {boolean[]} */
  let nacionalidadesSeleccionadas = [false];

  /** Codigo de marcado internacional del pais seleccionado (ej. "+502"). @type {string} */
  let dialCode = '';

  /** Mapa de codigo de marcado y digitos por nombre de pais. @type {Object.<string, {code: string, digits: number}>} */
  let dialCodesMap = {};

  /** Cantidad de digitos locales requeridos para el numero de telefono. @type {number} */
  let phoneDigitCount = 8;

  /**
   * Tabla de digitos locales por codigo de marcado (fuente: estandares ITU).
   * @type {Object.<string, number>}
   */
  const knownDigits = {
    '+1':    10, '+7':    10, '+20':   10, '+27':   9,  '+30':   10,
    '+31':   9,  '+32':   9,  '+33':   9,  '+34':   9,  '+36':   9,
    '+39':   10, '+40':   9,  '+41':   9,  '+43':   10, '+44':   10,
    '+45':   8,  '+46':   9,  '+47':   8,  '+48':   9,  '+49':   10,
    '+51':   9,  '+52':   10, '+53':   8,  '+54':   10, '+55':   11,
    '+56':   9,  '+57':   10, '+58':   10, '+60':   9,  '+61':   9,
    '+62':   9,  '+63':   10, '+64':   9,  '+65':   8,  '+66':   9,
    '+81':   10, '+82':   10, '+84':   9,  '+86':   11, '+90':   10,
    '+91':   10, '+92':   10, '+93':   9,  '+94':   9,  '+95':   8,
    '+98':   10, '+212':  9,  '+213':  9,  '+216':  8,  '+218':  9,
    '+220':  7,  '+221':  9,  '+222':  8,  '+223':  8,  '+224':  9,
    '+225':  8,  '+226':  8,  '+227':  8,  '+228':  8,  '+229':  8,
    '+230':  8,  '+231':  8,  '+232':  8,  '+233':  9,  '+234':  10,
    '+235':  8,  '+236':  8,  '+237':  9,  '+238':  7,  '+239':  7,
    '+240':  9,  '+241':  8,  '+242':  9,  '+243':  9,  '+244':  9,
    '+245':  7,  '+246':  7,  '+247':  4,  '+248':  7,  '+249':  9,
    '+250':  9,  '+251':  9,  '+252':  8,  '+253':  8,  '+254':  9,
    '+255':  9,  '+256':  9,  '+257':  8,  '+258':  9,  '+260':  9,
    '+261':  9,  '+262':  9,  '+263':  9,  '+264':  9,  '+265':  9,
    '+266':  8,  '+267':  8,  '+268':  8,  '+269':  7,  '+290':  4,
    '+291':  7,  '+297':  7,  '+298':  6,  '+299':  6,  '+350':  8,
    '+351':  9,  '+352':  9,  '+353':  9,  '+354':  7,  '+355':  9,
    '+356':  8,  '+357':  8,  '+358':  9,  '+359':  9,  '+370':  8,
    '+371':  8,  '+372':  8,  '+373':  8,  '+374':  8,  '+375':  9,
    '+376':  6,  '+377':  8,  '+378':  10, '+380':  9,  '+381':  9,
    '+382':  8,  '+385':  9,  '+386':  8,  '+387':  8,  '+389':  8,
    '+420':  9,  '+421':  9,  '+423':  7,  '+500':  5,  '+501':  7,
    '+502':  8,  '+503':  8,  '+504':  8,  '+505':  8,  '+506':  8,
    '+507':  8,  '+508':  6,  '+509':  8,  '+590':  9,  '+591':  8,
    '+592':  7,  '+593':  9,  '+594':  9,  '+595':  9,  '+596':  9,
    '+597':  7,  '+598':  8,  '+599':  7,  '+670':  8,  '+672':  6,
    '+673':  7,  '+674':  7,  '+675':  8,  '+676':  7,  '+677':  7,
    '+678':  7,  '+679':  7,  '+680':  7,  '+681':  6,  '+682':  5,
    '+683':  4,  '+685':  7,  '+686':  8,  '+687':  6,  '+688':  5,
    '+689':  8,  '+690':  4,  '+691':  7,  '+692':  7,  '+850':  10,
    '+852':  8,  '+853':  8,  '+855':  9,  '+856':  10, '+880':  10,
    '+886':  9,  '+960':  7,  '+961':  8,  '+962':  9,  '+963':  9,
    '+964':  10, '+965':  8,  '+966':  9,  '+967':  9,  '+968':  8,
    '+970':  9,  '+971':  9,  '+972':  9,  '+973':  8,  '+974':  8,
    '+975':  8,  '+976':  8,  '+977':  10, '+992':  9,  '+993':  8,
    '+994':  9,  '+995':  9,  '+996':  9,  '+998':  9,
  };

  /**
   * Maneja la entrada en el campo de telefono, extrae digitos, los limita
   * al maximo del pais y aplica formato visual.
   * @param {Event} e - Evento de input.
   */
  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '');
    const capped = raw.slice(0, phoneDigitCount);
    formData.phone = formatLocalPhone(capped, phoneDigitCount);
  }

  /**
   * Limpia caracteres no permitidos del campo de nombre de usuario
   * (solo letras, numeros, punto y guion bajo).
   * @param {Event} e - Evento de input.
   */
  function onUsernameInput(e) {
    formData.username = e.target.value.replace(/[^a-zA-Z0-9_.]/g, '');
    if (errors.username) errors.username = '';
  }

  /**
   * Carga la lista de paises y los codigos de marcado y demonimos al montar.
   */
  onMount(async () => {
    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch { console.error('Error cargando países'); }

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
   * Filtra sugerencias de pais mientras el usuario escribe.
   * Resetea ciudad y telefono si no hay pais confirmado.
   */
  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (!paisSeleccionado) { formData.country = ''; errors.country = ''; }
  }

  /**
   * Confirma la seleccion de un pais, carga sus ciudades y actualiza el
   * codigo de marcado del telefono.
   * @param {any} p - Objeto del pais seleccionado.
   */
  function seleccionarPais(p) {
    paisSeleccionado = p;
    paisQuery = p.country;
    formData.country = p.country;
    paisesSugeridos = [];
    ciudadQuery = ''; formData.city = '';
    ciudadesSugeridas = []; ciudadSeleccionada = false;
    errors.country = '';
    const info = dialCodesMap[p.country.toLowerCase()];
    dialCode = info?.code ?? '';
    phoneDigitCount = info?.digits ?? 9;
    formData.phone = '';
  }

  /**
   * Valida al perder el foco que el usuario haya seleccionado un pais de la lista.
   */
  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) { errors.country = 'Selecciona un país de la lista'; paisQuery = ''; }
  }

  /**
   * Filtra las ciudades disponibles del pais seleccionado segun lo escrito.
   */
  function onCiudadInput() {
    if (!paisSeleccionado) return;
    const q = ciudadQuery.toLowerCase();
    ciudadesSugeridas = q.length < 2 ? [] : paisSeleccionado.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    if (!ciudadSeleccionada) { formData.city = ''; errors.city = ''; }
  }

  /**
   * Confirma la seleccion de una ciudad del autocomplete.
   * @param {string} c - Nombre de la ciudad seleccionada.
   */
  function seleccionarCiudad(c) {
    ciudadQuery = c; formData.city = c;
    ciudadesSugeridas = []; ciudadSeleccionada = true;
    errors.city = '';
  }

  /**
   * Valida al perder el foco que el usuario haya seleccionado una ciudad de la lista.
   */
  function validarCiudadSeleccionada() {
    if (ciudadQuery && !ciudadSeleccionada) { errors.city = 'Selecciona una ciudad de la lista'; ciudadQuery = ''; }
  }

  /**
   * Filtra las sugerencias de nacionalidades para el indice dado.
   * @param {number} i - Indice del campo de nacionalidad.
   */
  function onNacInput(i) {
    const q = nacionalidades[i].toLowerCase();
    sugerenciasNac[i] = q.length < 2 ? [] : todosNacionalidades
      .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q)).slice(0, 6);
    sugerenciasNac = [...sugerenciasNac];
  }

  /**
   * Confirma la seleccion de una nacionalidad en el indice indicado.
   * @param {number} i - Indice del campo de nacionalidad.
   * @param {string} demonym - Demonimo seleccionado.
   */
  function seleccionarNac(i, demonym) {
    nacionalidades[i] = demonym; nacionalidades = [...nacionalidades];
    nacionalidadesSeleccionadas[i] = true; nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas];
    sugerenciasNac[i] = []; sugerenciasNac = [...sugerenciasNac];
    errors.nacionalidades = '';
  }

  /**
   * Valida al salir del campo que la nacionalidad fue seleccionada de la lista.
   * @param {number} i - Indice del campo de nacionalidad.
   */
  function validarNacionalidadSeleccionada(i) {
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errors.nacionalidades = 'Selecciona una nacionalidad de la lista';
      nacionalidades[i] = ''; nacionalidades = [...nacionalidades];
    }
  }

  /**
   * Agrega un campo adicional para ingresar otra nacionalidad.
   */
  function agregarNac() {
    nacionalidades = [...nacionalidades, ''];
    sugerenciasNac = [...sugerenciasNac, []];
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas, false];
  }

  /**
   * Elimina el campo de nacionalidad en el indice indicado.
   * @param {number} i - Indice del campo a eliminar.
   */
  function quitarNac(i) {
    nacionalidades = nacionalidades.filter((_, idx) => idx !== i);
    sugerenciasNac = sugerenciasNac.filter((_, idx) => idx !== i);
    nacionalidadesSeleccionadas = nacionalidadesSeleccionadas.filter((_, idx) => idx !== i);
  }

  // Edad calculada de forma reactiva cuando cambia la fecha de nacimiento.
  $: userAge            = calculateAge(formData.birthDate);

  // Nivel de seguridad de la contrasena, reactivo al campo.
  $: passwordStrength   = formData.password ? getPasswordStrength(formData.password) : null;

  // Resultado de validacion de cada requisito de la contrasena.
  $: passwordValidation = validatePassword(formData.password);

  /**
   * Simula la verificacion del captcha con un pequeno retraso y un 5% de
   * probabilidad de fallo para imitar el comportamiento de reCAPTCHA.
   */
  function handleCaptchaClick() {
    if (captchaVerified) return;
    captchaLoading = true; captchaError = false;
    setTimeout(() => {
      captchaLoading = false;
      if (Math.random() > 0.05) { captchaVerified = true; }
      else { captchaError = true; setTimeout(() => { captchaError = false; }, 3000); }
    }, 1500);
  }

  /**
   * Reinicia el estado del captcha para que el usuario pueda intentarlo de nuevo.
   */
  function resetCaptcha() { captchaVerified = false; captchaLoading = false; captchaError = false; }

  /**
   * Maneja el envio del formulario de registro. Valida, construye el payload
   * y hace POST a /usuarios/registrar. Redirige al login tras el exito.
   * @async
   * @param {Event} e - Evento de submit del formulario.
   * @returns {Promise<void>}
   */
  async function handleRegister(e) {
    e.preventDefault();
    errors = validateForm({
      formData, userAge, phoneDigitCount,
      paisSeleccionado, ciudadSeleccionada,
      nacionalidades, nacionalidadesSeleccionadas,
      passwordValidation, acceptTerms, acceptPrivacy, captchaVerified,
    });
    if (Object.keys(errors).length !== 0) {
      document.querySelector('.register__error-text')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
    isSubmitting = true;

    const nacsValidas = nacionalidades.filter((n, i) => n.trim() && nacionalidadesSeleccionadas[i]);

    const payload = {
      username:           formData.username.trim(),
      correo:             formData.email.toLowerCase(),
      contrasena:         formData.password,
      pasaporte:          formData.pasaporte.trim(),
      nombre:             formData.firstName.trim(),
      apellido:           formData.lastName.trim(),
      telefono:           dialCode + ' ' + formData.phone.replace(/\s/g, ''),
      fechaNacimiento:    formData.birthDate,
      pais:               formData.country,
      ciudad:             formData.city,
      nacionalidades:     nacsValidas,
      preferenciasOferta: recibirOfertas ? JSON.stringify(preferencias) : null
    };

    try {
      const res = await fetch(`${API}/usuarios/registrar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const text = await res.text();
      let data = null;
      try { data = JSON.parse(text); } catch { /* no JSON */ }

      // Manejo de conflictos: correo, pasaporte o username ya registrados
      if (res.status === 409 && data?.campos) {
        if (data.campos.correoExiste)    errors.email     = 'Este correo ya está registrado.';
        if (data.campos.pasaporteExiste) errors.pasaporte = 'Este pasaporte ya está registrado.';
        if (data.campos.usernameExiste)  errors.username  = 'Este nombre de usuario ya está en uso.';
        isSubmitting = false;
        return;
      }

      if (!res.ok) {
        errors.submit = `Error del servidor (${res.status}): ${text}`;
        isSubmitting = false;
        return;
      }

      registrationSuccess = true;
      setTimeout(() => navigateTo('login'), 2000);

    } catch (err) {
      console.error('Fetch error:', err);
      errors.submit = `Error de conexión: ${err.message}`;
    } finally {
      isSubmitting = false;
    }
  }
</script>

<div class="register-page">
  <div class="register-container">
    <div class="register-card">

      <!-- Boton de regreso al inicio -->
      <button class="register__back-link" on:click={() => navigateTo('home')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </button>

      <!-- Encabezado con logo y titulo -->
      <div class="register__header">
        <div class="register__logo-section">
          <img src="{logo}" alt="Miku Inn Logo" class="register__logo-image" />
        </div>
        <h2 class="register__title">Crear tu Cuenta</h2>
        <p class="register__subtitle">Únete a nuestra comunidad y comienza a reservar experiencias inolvidables</p>
      </div>

      {#if registrationSuccess}
        <!-- Pantalla de exito con redireccion automatica al login -->
        <div class="register__success-message">
          <div class="register__success-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          </div>
          <h3>¡Cuenta Creada Exitosamente!</h3>
          <p>Te estamos redirigiendo al inicio de sesión...</p>
          <div class="register__loading-dots">
            <span></span><span></span><span></span>
          </div>
        </div>

      {:else}
        <form on:submit={handleRegister} class="register-form">

          <!-- Seccion de informacion personal -->
          <div class="form-section">
            <h3 class="register__section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              Información Personal
            </h3>

            <!-- Nombre y apellidos -->
            <div class="register__form-grid">
              <div class="register__form-field">
                <label for="firstName">Nombre <span class="required">*</span></label>
                <input type="text" id="firstName" bind:value={formData.firstName}
                  placeholder="Nombres" class:error={errors.firstName} autocomplete="given-name" />
                {#if errors.firstName}<span class="register__error-text">{errors.firstName}</span>{/if}
              </div>
              <div class="register__form-field">
                <label for="lastName">Apellidos <span class="required">*</span></label>
                <input type="text" id="lastName" bind:value={formData.lastName}
                  placeholder="Apellidos" class:error={errors.lastName} autocomplete="family-name" />
                {#if errors.lastName}<span class="register__error-text">{errors.lastName}</span>{/if}
              </div>
            </div>

            <!-- Fecha de nacimiento y numero de pasaporte -->
            <div class="register__form-grid">
              <div class="register__form-field">
                <label for="birthDate">Fecha de Nacimiento <span class="required">*</span></label>
                <input type="date" id="birthDate" bind:value={formData.birthDate}
                  max={new Date(new Date().setFullYear(new Date().getFullYear() - 18)).toISOString().split('T')[0]}
                  class:error={errors.birthDate} />
                {#if formData.birthDate && userAge >= 18}
                  <span class="helper-text success">✓ {userAge} años</span>
                {:else if formData.birthDate && userAge < 18}
                  <span class="helper-text error-text">✗ Debes tener al menos 18 años</span>
                {/if}
                {#if errors.birthDate}<span class="register__error-text">{errors.birthDate}</span>{/if}
              </div>
              <div class="register__form-field">
                <label for="pasaporte">Número de Pasaporte <span class="required">*</span></label>
                  <input type="text" id="pasaporte" bind:value={formData.pasaporte}
                      on:input={() => formData.pasaporte = formData.pasaporte.toUpperCase()}
                      placeholder="AB123456" class:error={errors.pasaporte} autocomplete="off"
                      style="text-transform:uppercase" />
                {#if errors.pasaporte}<span class="register__error-text">{errors.pasaporte}</span>{/if}
              </div>
            </div>

            <!-- Pais con autocomplete y campo de telefono con codigo de marcado -->
            <div class="register__form-grid">
              <div class="register__form-field">
                <label for="paisInput">País <span class="required">*</span></label>
                <div class="autocomplete-wrap">
                  <input type="text" id="paisInput" bind:value={paisQuery}
                    on:input={onPaisInput} on:blur={validarPaisSeleccionado}
                    placeholder="Escribe tu país..." class:error={errors.country} autocomplete="off" />
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
                {#if errors.country}<span class="register__error-text">{errors.country}</span>{/if}
              </div>

              <div class="register__form-field">
                <label for="phone">
                  Teléfono <span class="required">*</span>
                  {#if dialCode}
                    <span style="font-weight:400;color:rgba(255,255,255,0.45);font-size:0.8rem;">
                      — {phoneDigitCount} dígitos requeridos
                    </span>
                  {/if}
                </label>
                <div class="phone-field" class:error={errors.phone}>
                  {#if dialCode}
                    <span class="phone-prefix">{dialCode}</span>
                  {/if}
                  <input type="tel" id="phone" bind:value={formData.phone}
                    on:input={onPhoneInput}
                    placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un país primero'}
                    disabled={!dialCode}
                    autocomplete="tel" />
                </div>
                <!-- Indicador de digitos completados -->
                {#if formData.phone && !errors.phone}
                  {@const d = formData.phone.replace(/\D/g, '').length}
                  {#if d === phoneDigitCount}
                    <span class="helper-text success">✓ Número completo</span>
                  {:else}
                    <span class="helper-text error-text">{d}/{phoneDigitCount} dígitos</span>
                  {/if}
                {/if}
                {#if errors.phone}<span class="register__error-text">{errors.phone}</span>{/if}
              </div>
            </div>

            <!-- Ciudad con autocomplete (dependiente del pais) -->
            <div class="register__form-field">
              <label for="ciudadInput">Ciudad <span class="required">*</span></label>
              <div class="autocomplete-wrap">
                <input type="text" id="ciudadInput" bind:value={ciudadQuery}
                  on:input={onCiudadInput} on:blur={validarCiudadSeleccionada}
                  placeholder={paisSeleccionado ? 'Escribe tu ciudad...' : 'Primero selecciona un país'}
                  disabled={!paisSeleccionado} class:error={errors.city} autocomplete="off" />
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
              {#if errors.city}<span class="register__error-text">{errors.city}</span>{/if}
            </div>

            <!-- Campo(s) de nacionalidad con autocomplete dinamico -->
            <div class="register__form-field">
              <label for="nac-0">Nacionalidad(es) <span class="required">*</span></label>
              {#each nacionalidades as _nac, i}
                <div class="nac-row">
                  <div class="autocomplete-wrap" style="flex:1">
                    <input type="text" id="nac-{i}" bind:value={nacionalidades[i]}
                      on:input={() => onNacInput(i)} on:blur={() => validarNacionalidadSeleccionada(i)}
                      placeholder="Ej: Guatemalan"
                      class:error={errors.nacionalidades && !nacionalidadesSeleccionadas[i]}
                      autocomplete="off" />
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
                    <button type="button" class="btn-quitar" on:click={() => quitarNac(i)}>✕</button>
                  {/if}
                </div>
              {/each}
              <button type="button" class="register__link-btn" on:click={agregarNac}>+ Agregar otra nacionalidad</button>
              {#if errors.nacionalidades}<span class="register__error-text">{errors.nacionalidades}</span>{/if}
            </div>
          </div>

          <!-- Seccion de credenciales de acceso -->
          <div class="form-section">
            <h3 class="register__section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              Credenciales de Acceso
            </h3>

            <!-- Campo de nombre de usuario -->
            <div class="register__form-field">
              <label for="username">Nombre de Usuario <span class="required">*</span></label>
              <div class="register__input-with-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                <input type="text" id="username" bind:value={formData.username}
                  on:input={onUsernameInput}
                  placeholder="Ej: Pine2" class:error={errors.username} autocomplete="username" />
              </div>
              {#if formData.username && !errors.username}
                <span class="helper-text" style="color:#64748b;">Tu usuario será: <strong style="color:var(--primary);">{formData.username}</strong></span>
              {/if}
              {#if errors.username}<span class="register__error-text">{errors.username}</span>{/if}
            </div>

            <!-- Campo de correo electronico -->
            <div class="register__form-field">
              <label for="email">Correo Electrónico <span class="required">*</span></label>
              <div class="register__input-with-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                <input type="email" id="email" bind:value={formData.email}
                  placeholder="tu@email.com" class:error={errors.email} autocomplete="email" />
              </div>
              {#if errors.email}<span class="register__error-text">{errors.email}</span>{/if}
            </div>

            <!-- Campo de contrasena con indicador de fortaleza y requisitos -->
            <div class="register__form-field">
              <label for="password">Contraseña <span class="required">*</span></label>
              <div class="register__password-field">
                <input type={showPassword ? 'text' : 'password'} id="password"
                  bind:value={formData.password} placeholder="Mínimo 8 caracteres"
                  class:error={errors.password} autocomplete="new-password" />
                <button type="button" class="register__toggle-btn" on:click={() => showPassword = !showPassword} tabindex="-1">
                  {#if showPassword}
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  {:else}
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                  {/if}
                </button>
              </div>
              <!-- Barra de fortaleza de la contrasena -->
              {#if formData.password && passwordStrength}
                <div class="strength-indicator">
                  <div class="strength-bar">
                    <div class="strength-fill" style="width:{passwordStrength.width};background:{passwordStrength.color}"></div>
                  </div>
                  <span class="strength-text" style="color:{passwordStrength.color}">{passwordStrength.text}</span>
                </div>
              {/if}
              <!-- Lista de requisitos con estado visual -->
              <div class="requirements">
                <div class="req" class:met={passwordValidation.minLength}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  Mínimo 8 caracteres
                </div>
                <div class="req" class:met={passwordValidation.hasUpperCase}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  Una mayúscula
                </div>
                <div class="req" class:met={passwordValidation.hasLowerCase}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  Una minúscula
                </div>
                <div class="req" class:met={passwordValidation.hasNumber}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  Un número
                </div>
              </div>
              {#if errors.password}<span class="register__error-text">{errors.password}</span>{/if}
            </div>

            <!-- Campo de confirmacion de contrasena -->
            <div class="register__form-field">
              <label for="confirmPassword">Confirmar Contraseña <span class="required">*</span></label>
              <div class="register__password-field">
                <input type={showConfirmPassword ? 'text' : 'password'} id="confirmPassword"
                  bind:value={formData.confirmPassword} placeholder="Repite tu contraseña"
                  class:error={errors.confirmPassword} autocomplete="new-password" />
                <button type="button" class="register__toggle-btn" on:click={() => showConfirmPassword = !showConfirmPassword} tabindex="-1">
                  {#if showConfirmPassword}
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  {:else}
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                  {/if}
                </button>
              </div>
              {#if formData.confirmPassword && formData.password === formData.confirmPassword}
                <span class="helper-text success">✓ Las contraseñas coinciden</span>
              {:else if formData.confirmPassword}
                <span class="helper-text error-text">✗ Las contraseñas no coinciden</span>
              {/if}
              {#if errors.confirmPassword}<span class="register__error-text">{errors.confirmPassword}</span>{/if}
            </div>
          </div>

          <!-- Captcha simulado estilo reCAPTCHA -->
          <div class="captcha-container">
            <div class="captcha-box" class:verified={captchaVerified} class:error={captchaError}>
              <button type="button" class="captcha-label" on:click={handleCaptchaClick}
                style="background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:0.875rem;padding:0;">
                <div class="captcha-checkbox">
                  {#if captchaLoading}
                    <div class="captcha-spinner"></div>
                  {:else if captchaVerified}
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  {/if}
                </div>
                <span class="captcha-text">No soy un robot</span>
              </button>
              <div class="captcha-logo">
                <div class="recaptcha-badge">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#667eea"/>
                    <path d="M2 17L12 22L22 17" stroke="#667eea" stroke-width="2"/>
                    <path d="M2 12L12 17L22 12" stroke="#667eea" stroke-width="2"/>
                  </svg>
                  <div class="recaptcha-text">
                    <span>reCAPTCHA</span>
                    <div class="recaptcha-links"><span>Privacidad</span><span>-</span><span>Términos</span></div>
                  </div>
                </div>
              </div>
            </div>
            {#if errors.captcha}<span class="register__error-text">{errors.captcha}</span>{/if}
            {#if captchaError}
              <div class="captcha-error-message">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                Error en la verificación. Intenta de nuevo.
                <button type="button" class="retry-captcha" on:click={resetCaptcha}>Reintentar</button>
              </div>
            {/if}
          </div>

          <!-- Seccion de ofertas y promociones -->
          <div class="offers-section">
            <label class="register__checkbox-label offers-toggle-label">
              <input type="checkbox" bind:checked={recibirOfertas} />
              <span class="register__checkbox-custom offers-checkbox-custom"></span>
              <span class="register__checkbox-text offers-toggle-text">
                <span class="offers-icon">🎁</span>
                Quiero recibir ofertas y promociones personalizadas
              </span>
            </label>

            {#if recibirOfertas}
              <div class="offers-panel">
                <p class="offers-panel__desc">
                  Cuéntanos tus preferencias de viaje para enviarte las mejores ofertas
                </p>

                <!-- Tipos de habitacion favoritos -->
                <div class="offers-group">
                  <span class="offers-group__label">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
                    </svg>
                    Tipos de habitación favoritos
                  </span>
                  <div class="offers-chips">
                    {#each tipoHabOpciones as tipo}
                      <button type="button"
                        class="offers-chip"
                        class:offers-chip--active={preferencias.tiposHabitacion.includes(tipo)}
                        on:click={() => toggleTipoHab(tipo)}>
                        {tipo}
                      </button>
                    {/each}
                  </div>
                </div>

                <!-- Preferencia de precio/presupuesto -->
                <div class="offers-group">
                  <span class="offers-group__label">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
                    </svg>
                    Presupuesto estimado por noche
                  </span>
                  <div class="offers-radios">
                    {#each ['Económico', 'Estándar', 'Premium', 'Lujo'] as op}
                      <label class="offers-radio-label">
                        <input type="radio" bind:group={preferencias.presupuesto} value={op} />
                        <span class="offers-radio-btn" class:offers-radio-btn--active={preferencias.presupuesto === op}>{op}</span>
                      </label>
                    {/each}
                  </div>
                </div>

                <!-- Preferencia de combinacion -->
                <div class="offers-group">
                  <span class="offers-group__label">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="2" y="3" width="9" height="18" rx="2"/><rect x="13" y="3" width="9" height="18" rx="2"/>
                    </svg>
                    Preferencia de habitación
                  </span>
                  <div class="offers-radios">
                    {#each ['Una habitación', 'Combinar habitaciones'] as op}
                      <label class="offers-radio-label">
                        <input type="radio" bind:group={preferencias.combinacion} value={op} />
                        <span class="offers-radio-btn" class:offers-radio-btn--active={preferencias.combinacion === op}>{op}</span>
                      </label>
                    {/each}
                  </div>
                </div>

                <!-- Personas extra habituales -->
                <div class="offers-group">
                  <span class="offers-group__label">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
                      <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
                    </svg>
                    ¿Cuántas personas viajan usualmente contigo?
                  </span>
                  <div class="offers-radios">
                    {#each [1, 2, 3, 4] as n}
                      <label class="offers-radio-label">
                        <input type="radio" bind:group={preferencias.personasExtra} value={n} />
                        <span class="offers-radio-btn" class:offers-radio-btn--active={preferencias.personasExtra === n}>
                          {n === 4 ? '4+' : n} {n === 1 ? 'persona' : 'personas'}
                        </span>
                      </label>
                    {/each}
                  </div>
                </div>
              </div>
            {/if}
          </div>

          <!-- Checkboxes de terminos, privacidad y marketing -->
          <div class="terms-section">
            <label class="register__checkbox-label" class:error={errors.terms}>
              <input type="checkbox" bind:checked={acceptTerms} />
              <span class="register__checkbox-custom"></span>
              <span class="register__checkbox-text">
                Acepto los <button type="button" class="register__link-btn">Términos y Condiciones</button> <span class="required">*</span>
              </span>
            </label>
            {#if errors.terms}<span class="register__error-text">{errors.terms}</span>{/if}

            <label class="register__checkbox-label" class:error={errors.privacy}>
              <input type="checkbox" bind:checked={acceptPrivacy} />
              <span class="register__checkbox-custom"></span>
              <span class="register__checkbox-text">
                Acepto la <button type="button" class="register__link-btn">Política de Privacidad</button> <span class="required">*</span>
              </span>
            </label>
            {#if errors.privacy}<span class="register__error-text">{errors.privacy}</span>{/if}
          </div>

          {#if errors.submit}
            <div class="captcha-error-message">{errors.submit}</div>
          {/if}

          <!-- Boton de envio del formulario -->
          <button type="submit" class="register__submit-btn" disabled={isSubmitting}>
            {#if isSubmitting}
              <svg class="register__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 1 1-6.219-8.56" />
              </svg>
              Creando cuenta...
            {:else}
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                <polyline points="10 17 15 12 10 7"></polyline>
                <line x1="15" y1="12" x2="3" y2="12"></line>
              </svg>
              Crear Cuenta
            {/if}
          </button>

        </form>
      {/if}
    </div>
  </div>
</div>
