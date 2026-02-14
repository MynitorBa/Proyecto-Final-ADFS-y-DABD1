<script>
  import '../styles/reslog.css';
  import { onMount } from 'svelte';
  export let navigateTo;

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

  let acceptTerms = false;
  let receivePromotions = false;
  let captchaVerified = false;
  let submitError = '';
  let submitSuccess = false;
  let submitting = false;

  let errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '' };

  // Validación contraseña
  $: ps = {
    length:    registerData.contrasena.length >= 8,
    uppercase: /[A-Z]/.test(registerData.contrasena),
    number:    /[0-9]/.test(registerData.contrasena)
  };
  $: passwordValid = ps.length && ps.uppercase && ps.number;

  // País autocomplete
  let todosLosPaises = [];
  let paisQuery = '';
  let paisesSugeridos = [];
  let paisSeleccionado = null;

  // Ciudad autocomplete
  let ciudadQuery = '';
  let ciudadesSugeridas = [];
  let ciudadSeleccionada = false;

  // Nacionalidades múltiples
  let nacionalidades = [''];
  let sugerenciasNac = [[]];
  let todosNacionalidades = [];
  let nacionalidadesSeleccionadas = [false];

  // RECARGAR PÁGINA COMPLETA AL ENTRAR PARA LIMPIAR TODO
  onMount(async () => {
    // Si hay datos guardados en sessionStorage, limpiar
    sessionStorage.clear();
    
    // Limpiar el formulario
    limpiarFormulario();

    // Países y ciudades
    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch { console.error('Error cargando países'); }

    // Nacionalidades (demonyms)
    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,demonyms');
      const data = await res.json();
      todosNacionalidades = data
        .filter(p => p.demonyms?.eng?.m)
        .map(p => ({ pais: p.name.common, demonym: p.demonyms.eng.m }))
        .sort((a, b) => a.pais.localeCompare(b.pais));
    } catch { console.error('Error cargando nacionalidades'); }
  });

  function limpiarFormulario() {
    registerData = {
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
    acceptTerms = false;
    receivePromotions = false;
    captchaVerified = false;
    submitError = '';
    submitSuccess = false;
    errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '' };
    paisQuery = '';
    ciudadQuery = '';
    paisSeleccionado = null;
    ciudadSeleccionada = false;
    nacionalidades = [''];
    nacionalidadesSeleccionadas = [false];
    sugerenciasNac = [[]];
    paisesSugeridos = [];
    ciudadesSugeridas = [];
  }

  // CONVERTIR EMAIL A MINÚSCULAS AUTOMÁTICAMENTE
  function onCorreoInput(e) {
    registerData.correo = e.target.value.toLowerCase();
  }

  // VALIDAR QUE PASAPORTE SOLO TENGA NÚMEROS
  function onPasaporteInput(e) {
    registerData.pasaporte = e.target.value.replace(/[^0-9]/g, '');
  }

  // País
  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    
    // Si el usuario escribe pero no selecciona de la lista
    if (paisQuery && !paisSeleccionado) {
      registerData.pais = '';
      errores.pais = '';
    }
  }

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
  }

  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) {
      errores.pais = 'Debes seleccionar un país de la lista';
      paisQuery = '';
    }
  }

  // Ciudad
  function onCiudadInput() {
    if (!paisSeleccionado) return;
    const q = ciudadQuery.toLowerCase();
    ciudadesSugeridas = q.length < 2 ? [] : paisSeleccionado.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    
    // Si el usuario escribe pero no selecciona de la lista
    if (ciudadQuery && !ciudadSeleccionada) {
      registerData.ciudad = '';
      errores.ciudad = '';
    }
  }

  function seleccionarCiudad(c) {
    ciudadQuery = c;
    registerData.ciudad = c;
    ciudadesSugeridas = [];
    ciudadSeleccionada = true;
    errores.ciudad = '';
  }

  function validarCiudadSeleccionada() {
    if (ciudadQuery && !ciudadSeleccionada) {
      errores.ciudad = 'Debes seleccionar una ciudad de la lista';
      ciudadQuery = '';
    }
  }

  // Nacionalidades
  function onNacInput(i) {
    const q = nacionalidades[i].toLowerCase();
    sugerenciasNac[i] = q.length < 2 ? [] : todosNacionalidades
      .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q))
      .slice(0, 6);
    sugerenciasNac = [...sugerenciasNac];
    
    // Si el usuario escribe pero no selecciona de la lista
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errores.nacionalidad = '';
    }
  }

  function seleccionarNac(i, demonym) {
    nacionalidades[i] = demonym;
    nacionalidades = [...nacionalidades];
    nacionalidadesSeleccionadas[i] = true;
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas];
    sugerenciasNac[i] = [];
    sugerenciasNac = [...sugerenciasNac];
    errores.nacionalidad = '';
  }

  function validarNacionalidadSeleccionada(i) {
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errores.nacionalidad = 'Debes seleccionar una nacionalidad de la lista';
      nacionalidades[i] = '';
      nacionalidades = [...nacionalidades];
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

  async function handleRegister() {
    submitError = '';
    errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '' };

    // Validaciones
    if (!passwordValid) { errores.contrasena = 'Mínimo 8 caracteres, 1 mayúscula y 1 número.'; return; }
    if (registerData.contrasena !== registerData.confirmPassword) { submitError = 'Las contraseñas no coinciden.'; return; }
    if (!acceptTerms) { submitError = 'Debes aceptar los términos y condiciones.'; return; }
    if (!captchaVerified) { submitError = 'Confirma que no eres un robot.'; return; }
    
    // Validar que se haya seleccionado de la lista
    if (!paisSeleccionado || !registerData.pais) { 
      errores.pais = 'Debes seleccionar un país de la lista.'; 
      return; 
    }
    if (!ciudadSeleccionada || !registerData.ciudad) { 
      errores.ciudad = 'Debes seleccionar una ciudad de la lista.'; 
      return; 
    }

    // Validar nacionalidades seleccionadas
    const nacionalidadesValidas = nacionalidades.filter((n, i) => n.trim() !== '' && nacionalidadesSeleccionadas[i]);
    if (nacionalidadesValidas.length === 0) {
      errores.nacionalidad = 'Debes seleccionar al menos una nacionalidad de la lista.';
      return;
    }

    submitting = true;

    const payload = {
      correo:          registerData.correo,
      contrasena:      registerData.contrasena,
      pasaporte:       registerData.pasaporte,
      username:        registerData.username,
      nombre:          registerData.nombre,
      apellido:        registerData.apellido,
      telefono:        registerData.telefono,
      fechaNacimiento: registerData.fechaNacimiento,
      ciudad:          registerData.ciudad,
      pais:            registerData.pais,
      nacionalidades:  nacionalidadesValidas
    };

    try {
      const vRes = await fetch('http://localhost:5190/api/usuarios/verificar', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      const c = await vRes.json();

      if (c.correoExiste)    errores.correo    = 'Este correo ya está registrado.';
      if (c.usernameExiste)  errores.username  = 'Este username ya está en uso.';
      if (c.pasaporteExiste) errores.pasaporte = 'Este pasaporte ya está registrado.';
      if (c.correoExiste || c.usernameExiste || c.pasaporteExiste) { submitting = false; return; }

      const res = await fetch('http://localhost:5190/api/usuarios', {
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

<div class="register">
  <div class="register__container">
    <div class="register__content">

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

      <div class="register__form-section">
        <div class="register__form-container">
          <button class="register__back" on:click={() => navigateTo('home')}>Volver al inicio</button>

          <div class="register__header">
            <h1 class="register__title">Crear cuenta</h1>
            <p class="register__subtitle">Completa tus datos para registrarte</p>
          </div>

          {#if submitSuccess}
            <div class="register-form__success">¡Cuenta creada exitosamente! Redirigiendo al login...</div>
          {:else}
            <form class="register-form" on:submit|preventDefault={handleRegister}>

              <!-- Nombre y Apellido -->
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

              <!-- Username y Teléfono -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="username" class="register-form__label">Username</label>
                  <input type="text" id="username" class="register-form__input {errores.username ? 'register-form__input--error' : ''}" bind:value={registerData.username} placeholder="usuario123" autocomplete="off" required />
                  {#if errores.username}<span class="register-form__field-error">{errores.username}</span>{/if}
                </div>
                <div class="register-form__field">
                  <label for="telefono" class="register-form__label">Teléfono</label>
                  <input type="tel" id="telefono" class="register-form__input" bind:value={registerData.telefono} placeholder="50211223344" autocomplete="off" required />
                </div>
              </div>

              <!-- Correo (CONVERTIR A MINÚSCULAS) -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="correo" class="register-form__label">Correo electrónico</label>
                  <input type="email" id="correo" class="register-form__input {errores.correo ? 'register-form__input--error' : ''}" value={registerData.correo} on:input={onCorreoInput} placeholder="correo@ejemplo.com" autocomplete="off" required />
                  {#if errores.correo}<span class="register-form__field-error">{errores.correo}</span>{/if}
                </div>
              </div>

              <!-- Fecha nacimiento y Pasaporte (SOLO NÚMEROS) -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="fechaNacimiento" class="register-form__label">Fecha de nacimiento</label>
                  <input type="date" id="fechaNacimiento" class="register-form__input" bind:value={registerData.fechaNacimiento} autocomplete="off" required />
                </div>
                <div class="register-form__field">
                  <label for="pasaporte" class="register-form__label">Pasaporte (solo números)</label>
                  <input type="text" id="pasaporte" class="register-form__input {errores.pasaporte ? 'register-form__input--error' : ''}" value={registerData.pasaporte} on:input={onPasaporteInput} placeholder="12345678" autocomplete="off" required />
                  {#if errores.pasaporte}<span class="register-form__field-error">{errores.pasaporte}</span>{/if}
                </div>
              </div>

              <!-- País autocomplete (VALIDAR SELECCIÓN) -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="paisInput" class="register-form__label">País</label>
                  <div class="autocomplete">
                    <input 
                      type="text" 
                      id="paisInput" 
                      class="register-form__input {errores.pais ? 'register-form__input--error' : ''}" 
                      bind:value={paisQuery} 
                      on:input={onPaisInput} 
                      on:blur={validarPaisSeleccionado}
                      placeholder="Escribe tu país..." 
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

              <!-- Ciudad autocomplete (VALIDAR SELECCIÓN) -->
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
                      placeholder={paisSeleccionado ? 'Escribe tu ciudad...' : 'Primero selecciona un país'} 
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

              <!-- Nacionalidades múltiples (VALIDAR SELECCIÓN) -->
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

              <!-- Contraseñas -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="contrasena" class="register-form__label">Contraseña</label>
                  <input type="password" id="contrasena" class="register-form__input {errores.contrasena ? 'register-form__input--error' : ''}" bind:value={registerData.contrasena} placeholder="Mínimo 8 caracteres" required />
                  {#if registerData.contrasena.length > 0}
                    <div class="password-strength">
                      <span class="password-strength__item" class:ok={ps.length}>{ps.length ? '✓' : '✗'} 8 caracteres mínimo</span>
                      <span class="password-strength__item" class:ok={ps.uppercase}>{ps.uppercase ? '✓' : '✗'} 1 mayúscula</span>
                      <span class="password-strength__item" class:ok={ps.number}>{ps.number ? '✓' : '✗'} 1 número</span>
                    </div>
                  {/if}
                  {#if errores.contrasena}<span class="register-form__field-error">{errores.contrasena}</span>{/if}
                </div>
                <div class="register-form__field">
                  <label for="confirmPassword" class="register-form__label">Confirmar contraseña</label>
                  <input type="password" id="confirmPassword" class="register-form__input" bind:value={registerData.confirmPassword} placeholder="Repite tu contraseña" required />
                  {#if registerData.confirmPassword.length > 0 && registerData.contrasena !== registerData.confirmPassword}
                    <span class="register-form__field-error">Las contraseñas no coinciden.</span>
                  {/if}
                </div>
              </div>

              <!-- Checkboxes -->
              <div class="register-form__checkboxes">
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={acceptTerms} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">Acepto los términos y condiciones y la política de privacidad</span>
                </label>
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={receivePromotions} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">Deseo recibir promociones y ofertas por correo electrónico</span>
                </label>
              </div>

              <!-- CAPTCHA -->
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

              {#if submitError}
                <div class="register-form__error">{submitError}</div>
              {/if}

              <button type="submit" class="register-form__submit" disabled={submitting}>
                {submitting ? 'Verificando...' : 'Crear cuenta'}
              </button>

            </form>
          {/if}

          <div class="register__login">
            <p class="register__login-text">
              ¿Ya tienes una cuenta?
              <button type="button" class="register__login-link" on:click={() => navigateTo('login')}>Inicia sesión</button>
            </p>
          </div>

        </div>
      </div>
    </div>
  </div>
</div>