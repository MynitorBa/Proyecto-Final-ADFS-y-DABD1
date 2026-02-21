<script>
  import logo from '../assets/mikuinn-logo.png';
  export let navigateTo;
  import '../styles/register.css';
  import { onMount } from 'svelte';

  let formData = {
    firstName: '',
    lastName: '',
    birthDate: '',
    phone: '',
    pasaporte: '',
    country: '',
    city: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  let showPassword = false;
  let showConfirmPassword = false;
  let acceptTerms = false;
  let acceptPrivacy = false;
  let acceptMarketing = false;
  let errors = {};
  let isSubmitting = false;
  let registrationSuccess = false;

  // Captcha
  let captchaVerified = false;
  let captchaLoading = false;
  let captchaError = false;

  // País
  let todosLosPaises = [];
  let paisQuery = '';
  let paisesSugeridos = [];
  let paisSeleccionado = null;

  // Ciudad
  let ciudadQuery = '';
  let ciudadesSugeridas = [];
  let ciudadSeleccionada = false;

  // Nacionalidades
  let nacionalidades = [''];
  let sugerenciasNac = [[]];
  let todosNacionalidades = [];
  let nacionalidadesSeleccionadas = [false];

  // Teléfono — código de marcado por país
  let dialCode = '';       // ej. "+502"
  let dialCodesMap = {};   // { "australia": { code:"+61", digits:9 }, ... } clave en minúsculas
  let phoneDigitCount = 8;

  // Dígitos locales reales por código de país (fuente: estándares ITU)
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

  // Formatea dígitos locales con espacios según total de dígitos esperados
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    // 11 dígitos (Brasil, China...)
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  function onPhoneInput(e) {
    const raw = /** @type {HTMLInputElement} */ (e.target).value.replace(/\D/g, '');
    const capped = raw.slice(0, phoneDigitCount);
    formData.phone = formatLocalPhone(capped, phoneDigitCount);
  }

  // Genera el placeholder de ejemplo según los dígitos del país
  function getPhonePlaceholder(digits) {
    const sample = '5'.repeat(digits);
    return formatLocalPhone(sample, digits);
  }

  onMount(async () => {
    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch { console.error('Error cargando países'); }

    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,demonyms,idd');
      const data = await res.json();

      // Mapa en minúsculas para matching robusto: "australia" → { code:"+61", digits:9 }
      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1
            ? p.idd.root + suffixes[0]
            : p.idd.root;
          const digits = knownDigits[code] ?? 9; // usar tabla ITU real
          // Indexar por nombre común en minúsculas Y nombre oficial
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

  // País
  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (!paisSeleccionado) { formData.country = ''; errors.country = ''; }
  }

  function seleccionarPais(p) {
    paisSeleccionado = p;
    paisQuery = p.country;
    formData.country = p.country;
    paisesSugeridos = [];
    ciudadQuery = ''; formData.city = '';
    ciudadesSugeridas = []; ciudadSeleccionada = false;
    errors.country = '';
    // Actualizar código telefónico — buscar en minúsculas para match robusto
    const info = dialCodesMap[p.country.toLowerCase()];
    dialCode = info?.code ?? '';
    phoneDigitCount = info?.digits ?? 9;
    formData.phone = '';
  }

  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) { errors.country = 'Selecciona un país de la lista'; paisQuery = ''; }
  }

  // Ciudad
  function onCiudadInput() {
    if (!paisSeleccionado) return;
    const q = ciudadQuery.toLowerCase();
    ciudadesSugeridas = q.length < 2 ? [] : paisSeleccionado.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    if (!ciudadSeleccionada) { formData.city = ''; errors.city = ''; }
  }

  function seleccionarCiudad(c) {
    ciudadQuery = c; formData.city = c;
    ciudadesSugeridas = []; ciudadSeleccionada = true;
    errors.city = '';
  }

  function validarCiudadSeleccionada() {
    if (ciudadQuery && !ciudadSeleccionada) { errors.city = 'Selecciona una ciudad de la lista'; ciudadQuery = ''; }
  }

  // Nacionalidades
  function onNacInput(i) {
    const q = nacionalidades[i].toLowerCase();
    sugerenciasNac[i] = q.length < 2 ? [] : todosNacionalidades
      .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q)).slice(0, 6);
    sugerenciasNac = [...sugerenciasNac];
  }

  function seleccionarNac(i, demonym) {
    nacionalidades[i] = demonym; nacionalidades = [...nacionalidades];
    nacionalidadesSeleccionadas[i] = true; nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas];
    sugerenciasNac[i] = []; sugerenciasNac = [...sugerenciasNac];
    errors.nacionalidades = '';
  }

  function validarNacionalidadSeleccionada(i) {
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errors.nacionalidades = 'Selecciona una nacionalidad de la lista';
      nacionalidades[i] = ''; nacionalidades = [...nacionalidades];
    }
  }

  function agregarNac() {
    nacionalidades = [...nacionalidades, ''];
    sugerenciasNac = [...sugerenciasNac, []];
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas, false];
  }

  function quitarNac(i) {
    nacionalidades = nacionalidades.filter((_, idx) => idx !== i);
    sugerenciasNac = sugerenciasNac.filter((_, idx) => idx !== i);
    nacionalidadesSeleccionadas = nacionalidadesSeleccionadas.filter((_, idx) => idx !== i);
  }

  // Password
  function validatePassword(p) {
    return {
      minLength:    p.length >= 8,
      hasUpperCase: /[A-Z]/.test(p),
      hasLowerCase: /[a-z]/.test(p),
      hasNumber:    /[0-9]/.test(p),
      hasSpecial:   /[!@#$%^&*(),.?":{}|<>]/.test(p)
    };
  }

  function getPasswordStrength(p) {
    const n = Object.values(validatePassword(p)).filter(Boolean).length;
    if (n <= 2) return { text: 'Muy débil', color: '#ef4444', width: '25%' };
    if (n <= 3) return { text: 'Débil',     color: '#f59e0b', width: '50%' };
    if (n <= 4) return { text: 'Buena',     color: '#3b82f6', width: '75%' };
    return              { text: 'Excelente', color: '#10b981', width: '100%' };
  }

  function calculateAge(birthDate) {
    if (!birthDate) return 0;
    const today = new Date(), birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
  }

  $: userAge            = calculateAge(formData.birthDate);
  $: passwordStrength   = formData.password ? getPasswordStrength(formData.password) : null;
  $: passwordValidation = validatePassword(formData.password);

  // Captcha
  function handleCaptchaClick() {
    if (captchaVerified) return;
    captchaLoading = true; captchaError = false;
    setTimeout(() => {
      captchaLoading = false;
      if (Math.random() > 0.05) { captchaVerified = true; }
      else { captchaError = true; setTimeout(() => { captchaError = false; }, 3000); }
    }, 1500);
  }

  function resetCaptcha() { captchaVerified = false; captchaLoading = false; captchaError = false; }

  // Validación
  function validateForm() {
    errors = {};
    if (!formData.firstName.trim() || formData.firstName.trim().length < 2)
      errors.firstName = !formData.firstName.trim() ? 'Nombre requerido' : 'Mínimo 2 caracteres';
    if (!formData.lastName.trim() || formData.lastName.trim().length < 2)
      errors.lastName = !formData.lastName.trim() ? 'Apellidos requeridos' : 'Mínimo 2 caracteres';
    if (!formData.birthDate) errors.birthDate = 'Fecha de nacimiento requerida';
    else if (userAge < 18)   errors.birthDate = 'Debes tener al menos 18 años';
    if (!formData.phone.trim()) errors.phone = 'Teléfono requerido';
    if (!formData.pasaporte.trim() || formData.pasaporte.trim().length < 5)
      errors.pasaporte = !formData.pasaporte.trim() ? 'Pasaporte requerido' : 'Número de pasaporte inválido';
    if (!paisSeleccionado || !formData.country) errors.country = 'Selecciona un país de la lista';
    if (!ciudadSeleccionada || !formData.city)  errors.city    = 'Selecciona una ciudad de la lista';
    if (!formData.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
      errors.email = !formData.email.trim() ? 'Email requerido' : 'Email inválido';
    const nacsValidas = nacionalidades.filter((n, i) => n.trim() && nacionalidadesSeleccionadas[i]);
    if (nacsValidas.length === 0) errors.nacionalidades = 'Selecciona al menos una nacionalidad';
    if (!formData.password)
      errors.password = 'Contraseña requerida';
    else if (!passwordValidation.minLength || !passwordValidation.hasUpperCase ||
             !passwordValidation.hasLowerCase || !passwordValidation.hasNumber)
      errors.password = 'La contraseña no cumple los requisitos';
    if (!formData.confirmPassword) errors.confirmPassword = 'Confirma tu contraseña';
    else if (formData.password !== formData.confirmPassword) errors.confirmPassword = 'Las contraseñas no coinciden';
    if (!acceptTerms)     errors.terms   = 'Debes aceptar los términos y condiciones';
    if (!acceptPrivacy)   errors.privacy = 'Debes aceptar la política de privacidad';
    if (!captchaVerified) errors.captcha = 'Por favor verifica que no eres un robot';
    return Object.keys(errors).length === 0;
  }

  // Submit → POST /usuarios/registrar
  // El backend valida duplicados de correo, pasaporte y username y retorna 409 con campos
  async function handleRegister(e) {
    e.preventDefault();
    if (!validateForm()) {
      document.querySelector('.register__error-text')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
    isSubmitting = true;

    const nacsValidas = nacionalidades.filter((n, i) => n.trim() && nacionalidadesSeleccionadas[i]);

    const payload = {
      username:        formData.email.split('@')[0],
      correo:          formData.email.toLowerCase(),
      contrasena:      formData.password,
      pasaporte:       formData.pasaporte.trim(),
      nombre:          formData.firstName.trim(),
      apellido:        formData.lastName.trim(),
      telefono:        dialCode + ' ' + formData.phone.replace(/\s/g, ''),  // ej. "+502 57878778"
      fechaNacimiento: formData.birthDate,
      pais:            formData.country,
      ciudad:          formData.city,
      nacionalidades:  nacsValidas
    };

    try {
      const res  = await fetch('http://localhost:7000/usuarios/registrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const text = await res.text();
      let data = null;
      try { data = JSON.parse(text); } catch { /* no JSON */ }

      if (res.status === 409 && data?.campos) {
        if (data.campos.correoExiste)    errors.email     = 'Este correo ya está registrado.';
        if (data.campos.pasaporteExiste) errors.pasaporte = 'Este pasaporte ya está registrado.';
        if (data.campos.usernameExiste)  errors.email     = (errors.email || '') + ' Usuario ya en uso.';
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

      <button class="register__back-link" on:click={() => navigateTo('home')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </button>

      <div class="register__header">
        <div class="register__logo-section">
          <img src="{logo}" alt="Miku Inn Logo" class="register__logo-image" />
        </div>
        <h2 class="register__title">Crear tu Cuenta</h2>
        <p class="register__subtitle">Únete a nuestra comunidad y comienza a reservar experiencias inolvidables</p>
      </div>

      {#if registrationSuccess}
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

          <!-- ── Información Personal ── -->
          <div class="form-section">
            <h3 class="register__section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              Información Personal
            </h3>

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

            <!-- Fila: Fecha de nacimiento + Pasaporte -->
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
                  placeholder="AB123456" class:error={errors.pasaporte} autocomplete="off" />
                {#if errors.pasaporte}<span class="register__error-text">{errors.pasaporte}</span>{/if}
              </div>
            </div>

            <!-- Fila: País + Teléfono (el código +502 aparece automático al elegir país) -->
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
                <label for="phone">Teléfono <span class="required">*</span></label>
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
                {#if errors.phone}<span class="register__error-text">{errors.phone}</span>{/if}
              </div>
            </div>

            <!-- Ciudad sola (depende del país) -->
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

            <!-- Nacionalidades -->
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

          <!-- ── Credenciales ── -->
          <div class="form-section">
            <h3 class="register__section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              Credenciales de Acceso
            </h3>

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
              {#if formData.password && passwordStrength}
                <div class="strength-indicator">
                  <div class="strength-bar">
                    <div class="strength-fill" style="width:{passwordStrength.width};background:{passwordStrength.color}"></div>
                  </div>
                  <span class="strength-text" style="color:{passwordStrength.color}">{passwordStrength.text}</span>
                </div>
              {/if}
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

          <!-- ── Captcha ── -->
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

          <!-- ── Términos ── -->
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

            <label class="register__checkbox-label">
              <input type="checkbox" bind:checked={acceptMarketing} />
              <span class="register__checkbox-custom"></span>
              <span class="register__checkbox-text">Deseo recibir ofertas y promociones por email (opcional)</span>
            </label>
          </div>

          {#if errors.submit}
            <div class="captcha-error-message">{errors.submit}</div>
          {/if}

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

          <div class="register__footer-text">
            ¿Ya tienes una cuenta?
            <button type="button" class="register__link-btn" on:click={() => navigateTo('login')}>
              Inicia sesión aquí
            </button>
          </div>

        </form>
      {/if}
    </div>
  </div>
</div>