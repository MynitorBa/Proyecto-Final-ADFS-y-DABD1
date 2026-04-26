<script>
/**
 * @file Profile.svelte
 * @description Pagina de perfil de usuario con dos pestanas: informacion personal y seguridad. Al montar
 * redirige al login si no existe sesion, carga los codigos de marcado internacional desde la API de
 * restcountries para determinar el prefijo y conteo de digitos del pais del usuario, luego obtiene los datos
 * de perfil del usuario autenticado desde GET /api/perfil/:id. La pestana personal muestra campos de solo
 * lectura (nombre, apellido, correo, username, pasaporte, fecha de nacimiento, pais, ciudad) mas un input
 * editable de telefono con prefijo de codigo de marcado y validacion de conteo de digitos enviado via PATCH
 * /api/perfil/:id/telefono. La pestana de seguridad permite cambiar la contrasena usando PATCH
 * /api/perfil/:id/contrasena con indicadores de fortaleza en tiempo real y validacion de coincidencia.
 * Un boton de logout llama a la funcion logout del store de sesion y navega a la pagina de inicio.
 */
  import '../styles/profile.css';
  import { onMount } from 'svelte';
  import { sesion, logout } from '../stores/sesion.js';

  /** Funcion usada para navegar entre paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** El ID del usuario autenticado obtenido del store de sesion, o null si no esta logueado. @type {number|null} */
  let usuarioId = null;
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  import { API } from '../lib/api.js';

  /** Identificador de la pestana activa, ya sea 'personal' o 'security'. @type {string} */
  let activeTab = 'personal';

  /** True mientras se obtienen los datos iniciales de perfil de la API. @type {boolean} */
  let cargando = true;

  /** Objeto de datos de perfil poblado desde la respuesta de la API. @type {{nombre: string, apellido: string, correo: string, username: string, telefono: string, pasaporte: string, fechaNacimiento: string, pais: string, ciudad: string}} */
  let perfil = {
    nombre: '', apellido: '', correo: '', username: '',
    telefono: '', pasaporte: '', fechaNacimiento: '', pais: '', ciudad: ''
  };

  /** Digitos de telefono formateados localmente sin prefijo de codigo de marcado, vinculado al input de telefono. @type {string} */
  let telefonoEditado  = '';

  /** Prefijo de codigo de marcado internacional para el pais del usuario, por ejemplo '+502'. @type {string} */
  let dialCode         = '';

  /** Numero de digitos locales requeridos para numeros de telefono en el pais del usuario. @type {number} */
  let phoneDigitCount  = 8;

  /** Mapa de nombre de pais (en minusculas) a codigo de marcado y conteo de digitos construido desde la API de restcountries. @type {Object.<string, {code: string, digits: number}>} */
  let dialCodesMap     = {};

  /** Mensaje de exito mostrado despues de una actualizacion exitosa del telefono. @type {string} */
  let telefonoMensaje  = '';

  /** Mensaje de error mostrado cuando la validacion del telefono falla o la solicitud PATCH falla. @type {string} */
  let telefonoError    = '';

  /** True mientras la solicitud PATCH del telefono esta en progreso. @type {boolean} */
  let guardandoTelefono = false;

  /**
   * Mapa de busqueda estatico de codigos de marcado internacional a conteos de digitos locales esperados.
   * Las claves son cadenas de codigo de marcado (por ejemplo '+502'), los valores son conteos de digitos (por ejemplo 8).
   * @type {Object.<string, number>}
   */
  const knownDigits = {
    '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,'+31':9,'+32':9,'+33':9,'+34':9,
    '+36':9,'+39':10,'+40':9,'+41':9,'+43':10,'+44':10,'+45':8,'+46':9,'+47':8,
    '+48':9,'+49':10,'+51':9,'+52':10,'+53':8,'+54':10,'+55':11,'+56':9,'+57':10,
    '+58':10,'+60':9,'+61':9,'+62':9,'+63':10,'+64':9,'+65':8,'+66':9,'+81':10,
    '+82':10,'+84':9,'+86':11,'+90':10,'+91':10,'+92':10,'+94':9,'+98':10,
    '+212':9,'+213':9,'+216':8,'+218':9,'+234':10,'+254':9,'+255':9,'+256':9,
    '+351':9,'+352':9,'+353':9,'+358':9,'+380':9,'+381':9,'+385':9,'+386':8,
    '+420':9,'+421':9,'+501':7,'+502':8,'+503':8,'+504':8,'+505':8,'+506':8,
    '+507':8,'+509':8,'+591':8,'+592':7,'+593':9,'+595':9,'+597':7,'+598':8,
    '+855':9,'+856':10,'+880':10,'+960':7,'+961':8,'+962':9,'+963':9,'+964':10,
    '+965':8,'+966':9,'+967':9,'+968':8,'+971':9,'+972':9,'+973':8,'+974':8,
    '+975':8,'+976':8,'+977':10,'+992':9,'+993':8,'+994':9,'+995':9,'+996':9,'+998':9,
  };

  /**
   * Formatea una cadena de caracteres de digitos sin procesar en un numero de telefono local legible usando
   * grupos separados por espacios cuyo tamano depende del conteo total de digitos esperado del pais.
   * @param {string} digits - Cadena de digitos sin procesar a formatear.
   * @param {number} total - Conteo total de digitos esperado para el pais.
   * @returns {string} La cadena de telefono formateada con espacios entre grupos de digitos.
   */
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  /**
   * Maneja el evento de input del telefono eliminando los no-digitos, limitando a phoneDigitCount, formateando
   * el resultado con formatLocalPhone y almacenandolo en telefonoEditado. Limpia telefonoError.
   * @param {Event} e - El evento de input del campo de texto del telefono.
   */
  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '').slice(0, phoneDigitCount);
    telefonoEditado = formatLocalPhone(raw, phoneDigitCount);
    telefonoError = '';
  }

  /**
   * Genera una cadena de placeholder de telefono de muestra formateando una cadena de digito '5' repetido
   * de la longitud requerida para mostrar el formato esperado del pais actual.
   * @param {number} digits - Conteo total de digitos esperado para el pais.
   * @returns {string} Una cadena de placeholder formateada como '5555 5555'.
   */
  function getPhonePlaceholder(digits) {
    return formatLocalPhone('5'.repeat(digits), digits);
  }

  /** Objeto de datos del formulario de cambio de contrasena. @type {{currentPassword: string, newPassword: string, confirmPassword: string}} */
  let passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };

  /** Mensaje de exito mostrado despues de un cambio de contrasena exitoso. @type {string} */
  let passwordMensaje = '';

  /** Mensaje de error mostrado cuando la validacion de contrasena falla o la solicitud PATCH falla. @type {string} */
  let passwordError   = '';

  /** True mientras la solicitud PATCH de contrasena esta en progreso. @type {boolean} */
  let guardandoPassword = false;

  /** Nuevo correo electronico ingresado por el usuario. @type {string} */
  let nuevoCorreo = '';

  /** Mensaje de exito mostrado despues de actualizar el correo. @type {string} */
  let correoMensaje = '';

  /** Mensaje de error mostrado cuando la actualizacion del correo falla. @type {string} */
  let correoError = '';

  /** True mientras la solicitud PATCH de correo esta en progreso. @type {boolean} */
  let guardandoCorreo = false;

  // Indicadores de fortaleza de contrasena calculados para el input de nueva contrasena.
  $: ps = {
    length:    passwordData.newPassword.length >= 8,
    uppercase: /[A-Z]/.test(passwordData.newPassword),
    number:    /[0-9]/.test(passwordData.newPassword),
    special:   /[^A-Za-z0-9]/.test(passwordData.newPassword)
  };

  // True cuando los cuatro requisitos de fortaleza de contrasena se cumplen.
  $: passwordValid = ps.length && ps.uppercase && ps.number && ps.special;

  onMount(async () => {
    if (!usuarioId) { navigateTo('login'); return; }

    try {
      const res  = await fetch('https://restcountries.com/v3.1/all?fields=name,idd');
      const data = await res.json();
      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root;
          const digits = knownDigits[code] ?? 9;
          dialCodesMap[p.name.common.toLowerCase()] = { code, digits };
          if (p.name.official) dialCodesMap[p.name.official.toLowerCase()] = { code, digits };
        }
      });
    } catch { console.error('Error cargando dial codes'); }

    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}`, { credentials: 'include' });
      if (!res.ok) { navigateTo('acceso-denegado'); return; }

      const data = await res.json();
      perfil = {
        nombre:          data.nombre,
        apellido:        data.apellido,
        correo:          data.correo,
        username:        data.username,
        telefono:        data.telefono,
        pasaporte:       data.pasaporte,
        fechaNacimiento: data.fechaNacimiento
          ? new Date(data.fechaNacimiento).toLocaleDateString('es-GT') : '—',
        pais:   data.pais,
        ciudad: data.ciudad
      };

      const info = dialCodesMap[data.pais?.toLowerCase()];
      if (info) {
        dialCode        = info.code;
        phoneDigitCount = info.digits;
        let tel = data.telefono ?? '';
        if (tel.startsWith(info.code)) {
          tel = tel.slice(info.code.length).replace(/^\s+/, '');
        }
        const raw = tel.replace(/\D/g, '').slice(0, phoneDigitCount);
        telefonoEditado = formatLocalPhone(raw, phoneDigitCount);
      } else {
        telefonoEditado = data.telefono ?? '';
      }
    } catch {
      navigateTo('acceso-denegado');
    } finally {
      cargando = false;
    }

    return () => unsubscribe();
  });

  /**
   * Valida el conteo de digitos del input de telefono, construye la cadena de telefono completa con prefijo de
   * codigo de marcado y la envia via PATCH /api/perfil/:id/telefono. Al tener exito actualiza perfil.telefono
   * y establece telefonoMensaje. En caso de fallo establece telefonoError con el mensaje del servidor o de validacion.
   * @async
   * @returns {Promise<void>}
   */
  async function handleActualizarTelefono() {
    telefonoMensaje = '';
    telefonoError   = '';

    if (!telefonoEditado.trim()) {
      telefonoError = 'El telefono no puede estar vacio.'; return;
    }

    if (dialCode) {
      const digitosIngresados = telefonoEditado.replace(/\D/g, '').length;
      if (digitosIngresados !== phoneDigitCount) {
        telefonoError = `Se requieren ${phoneDigitCount} digitos para ${perfil.pais} (ingresaste ${digitosIngresados}).`;
        return;
      }
    }

    const telefonoCompleto = dialCode
      ? dialCode + ' ' + telefonoEditado.replace(/\s/g, '')
      : telefonoEditado;

    guardandoTelefono = true;
    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}/telefono`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ telefono: telefonoCompleto })
      });
      const data = await res.json();
      if (res.ok) {
        perfil.telefono = telefonoCompleto;
        telefonoMensaje = data.message;
      } else {
        telefonoError = data.message;
      }
    } catch {
      telefonoError = 'Error de conexion.';
    } finally {
      guardandoTelefono = false;
    }
  }

  /**
   * Valida la nueva contrasena contra los requisitos de fortaleza y el campo de confirmacion, luego
   * envia el cambio de contrasena via PATCH /api/perfil/:id/contrasena. Al tener exito establece
   * passwordMensaje y limpia los campos del formulario. En caso de fallo establece passwordError con el
   * mensaje del servidor o de validacion.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCambiarContrasena() {
    passwordMensaje = '';
    passwordError   = '';

    if (!passwordValid) {
      passwordError = 'La contrasena debe tener al menos 8 caracteres, 1 mayuscula, 1 numero y 1 caracter especial.'; return;
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      passwordError = 'Las contrasenas no coinciden.'; return;
    }

    guardandoPassword = true;
    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}/contrasena`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contrasenaActual: passwordData.currentPassword,
          nuevaContrasena:  passwordData.newPassword
        })
      });
      const data = await res.json();
      if (res.ok) {
        passwordMensaje = data.message;
        passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
      } else {
        passwordError = data.message;
      }
    } catch {
      passwordError = 'Error de conexion.';
    } finally {
      guardandoPassword = false;
    }
  }

  /**
   * Valida el formato del nuevo correo y lo envia via PATCH /api/perfil/:id/correo.
   * Al tener exito actualiza perfil.correo y establece correoMensaje.
   * @async
   * @returns {Promise<void>}
   */
  async function handleActualizarCorreo() {
    correoMensaje = '';
    correoError   = '';

    if (!nuevoCorreo.trim()) {
      correoError = 'El correo no puede estar vacio.'; return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(nuevoCorreo.trim())) {
      correoError = 'El formato del correo no es valido.'; return;
    }

    guardandoCorreo = true;
    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}/correo`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nuevoCorreo: nuevoCorreo.trim() })
      });
      const data = await res.json();
      if (res.ok) {
        perfil.correo = nuevoCorreo.trim();
        nuevoCorreo   = '';
        correoMensaje = data.message;
      } else {
        correoError = data.message;
      }
    } catch {
      correoError = 'Error de conexion.';
    } finally {
      guardandoCorreo = false;
    }
  }

  /**
   * Llama a la funcion logout del store de sesion para limpiar la sesion, luego navega a la pagina de inicio.
   * @async
   * @returns {Promise<void>}
   */
  async function handleLogout() {
    await logout();
    navigateTo('home');
  }
</script>

<!-- Contenedor principal de la pagina de perfil de usuario -->
<div class="profile">
  <div class="profile__container">
    <!-- Encabezado con boton de regreso y titulo de la pagina -->
    <div class="profile__header">
      <button class="profile__back" on:click={() => navigateTo('home')}>Volver al inicio</button>
      <h1 class="profile__title">Mi Perfil</h1>
    </div>

    <!-- Estado de carga o layout de dos columnas con sidebar y contenido principal -->
    {#if cargando}
      <p style="text-align:center;padding:2rem">Cargando perfil...</p>
    {:else}
      <div class="profile__content">

        <!-- Sidebar con avatar, nombre, navegacion de pestanas y boton de logout -->
        <aside class="profile__sidebar">
          <div class="profile-card">
            <div class="profile-card__avatar">
              <span class="profile-card__avatar-text">
                {perfil.nombre.charAt(0)}{perfil.apellido.charAt(0)}
              </span>
            </div>
            <h2 class="profile-card__name">{perfil.nombre} {perfil.apellido}</h2>
            <p class="profile-card__email">{perfil.correo}</p>
          </div>

          <nav class="profile-nav">
            <button class="profile-nav__item"
              class:profile-nav__item--active={activeTab === 'personal'}
              on:click={() => activeTab = 'personal'}>
              Informacion Personal
            </button>
            <button class="profile-nav__item"
              class:profile-nav__item--active={activeTab === 'security'}
              on:click={() => activeTab = 'security'}>
              Seguridad
            </button>
          </nav>

          <button class="profile-logout" on:click={handleLogout}>Cerrar Sesion</button>
        </aside>

        <!-- Area principal que alterna entre la pestana personal y la pestana de seguridad -->
        <main class="profile__main">

          {#if activeTab === 'personal'}
            <!-- Pestana de informacion personal con campos de solo lectura y edicion de telefono -->
            <section class="profile-section">
              <h2 class="profile-section__title">Informacion Personal</h2>
              <p class="profile-section__subtitle">Tus datos registrados en el sistema</p>

              <!-- Campos de lectura del perfil del usuario registrado -->
              <div class="profile-form">
                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label class="profile-form__label">Nombre</label>
                    <input class="profile-form__input" value={perfil.nombre} disabled />
                  </div>
                  <div class="profile-form__field">
                    <label class="profile-form__label">Apellido</label>
                    <input class="profile-form__input" value={perfil.apellido} disabled />
                  </div>
                </div>
                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label class="profile-form__label">Correo</label>
                    <input class="profile-form__input" value={perfil.correo} disabled />
                  </div>
                  <div class="profile-form__field">
                    <label class="profile-form__label">Username</label>
                    <input class="profile-form__input" value={perfil.username} disabled />
                  </div>
                </div>
                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label class="profile-form__label">Pasaporte</label>
                    <input class="profile-form__input" value={perfil.pasaporte} disabled />
                  </div>
                  <div class="profile-form__field">
                    <label class="profile-form__label">Fecha de Nacimiento</label>
                    <input class="profile-form__input" value={perfil.fechaNacimiento} disabled />
                  </div>
                </div>
                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label class="profile-form__label">Pais</label>
                    <input class="profile-form__input" value={perfil.pais} disabled />
                  </div>
                  <div class="profile-form__field">
                    <label class="profile-form__label">Ciudad</label>
                    <input class="profile-form__input" value={perfil.ciudad} disabled />
                  </div>
                </div>
              </div>

              <!-- Subseccion editable para actualizar el telefono con prefijo internacional -->
              <div style="margin-top:2.5rem">
                <h3 class="profile-section__title" style="font-size:1.1rem;margin-bottom:.4rem">
                  Actualizar Telefono
                </h3>
                <p style="color:var(--text-muted);font-size:.85rem;margin-bottom:1.5rem">
                  {#if dialCode}
                    Numero local de <strong>{perfil.pais}</strong> — se guardara como
                    <strong>{dialCode}</strong> + tus digitos
                  {:else}
                    Ingresa tu numero de telefono
                  {/if}
                </p>

                <div class="profile-form">
                  <div class="profile-form__row">
                    <div class="profile-form__field profile-form__field--full">
                      <label class="profile-form__label">
                        Telefono
                        {#if dialCode}
                          <span style="font-weight:400;text-transform:none;letter-spacing:0;color:var(--primary-color)">
                            — {phoneDigitCount} digitos requeridos
                          </span>
                        {/if}
                      </label>

                      <div class="phone-field" style="display:flex;align-items:center;gap:0;border:2px solid var(--perla-dark);border-radius:0 12px 0 12px;overflow:hidden;background:#fcfbf9">
                        {#if dialCode}
                          <span style="
                            padding:.85rem 1rem;
                            background:var(--secondary-color);
                            color:var(--white);
                            font-weight:700;
                            font-size:.95rem;
                            letter-spacing:.5px;
                            white-space:nowrap;
                            flex-shrink:0">
                            {dialCode}
                          </span>
                        {/if}
                        <input
                          type="tel"
                          bind:value={telefonoEditado}
                          on:input={onPhoneInput}
                          placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Telefono'}
                          autocomplete="off"
                          style="
                            flex:1;
                            padding:.85rem 1.1rem;
                            border:none;
                            background:transparent;
                            font-size:1rem;
                            font-family:inherit;
                            color:var(--secondary-color);
                            outline:none;
                            letter-spacing:.5px" />
                      </div>

                      {#if telefonoEditado && dialCode}
                        {@const d = telefonoEditado.replace(/\D/g,'').length}
                        {#if d === phoneDigitCount}
                          <span style="color:#2e7d32;font-size:.82rem;margin-top:.3rem;display:block">
                            Numero completo ({d}/{phoneDigitCount} digitos)
                          </span>
                        {:else}
                          <span style="color:var(--text-muted);font-size:.82rem;margin-top:.3rem;display:block">
                            {d}/{phoneDigitCount} digitos
                          </span>
                        {/if}
                      {/if}

                      {#if telefonoError}
                        <span style="color:#c62828;font-size:.82rem;margin-top:.3rem;display:block">
                          {telefonoError}
                        </span>
                      {/if}
                      {#if telefonoMensaje}
                        <span style="color:#2e7d32;font-size:.85rem;margin-top:.3rem;display:block">
                          {telefonoMensaje}
                        </span>
                      {/if}
                    </div>
                  </div>

                  <button class="profile-form__submit"
                    on:click={handleActualizarTelefono}
                    disabled={guardandoTelefono}>
                    {guardandoTelefono ? 'Guardando...' : 'Guardar Telefono'}
                  </button>
                </div>
              </div>

              <!-- Subseccion editable para actualizar el correo electronico -->
              <div style="margin-top:2.5rem">
                <h3 class="profile-section__title" style="font-size:1.1rem;margin-bottom:.4rem">
                  Cambiar Correo Electronico
                </h3>
                <p style="color:var(--text-muted);font-size:.85rem;margin-bottom:1.5rem">
                  Correo actual: <strong>{perfil.correo}</strong>
                </p>

                <div class="profile-form">
                  <div class="profile-form__row">
                    <div class="profile-form__field profile-form__field--full">
                      <label class="profile-form__label">Nuevo Correo</label>
                      <input
                        type="email"
                        class="profile-form__input"
                        bind:value={nuevoCorreo}
                        placeholder="nuevo@correo.com"
                        autocomplete="off"
                        on:input={() => { correoError = ''; correoMensaje = ''; }} />

                      {#if correoError}
                        <span style="color:#c62828;font-size:.82rem;margin-top:.3rem;display:block">
                          {correoError}
                        </span>
                      {/if}
                      {#if correoMensaje}
                        <span style="color:#2e7d32;font-size:.85rem;margin-top:.3rem;display:block">
                          {correoMensaje}
                        </span>
                      {/if}
                    </div>
                  </div>

                  <button class="profile-form__submit"
                    on:click={handleActualizarCorreo}
                    disabled={guardandoCorreo}>
                    {guardandoCorreo ? 'Guardando...' : 'Guardar Correo'}
                  </button>
                </div>
              </div>
            </section>

          {:else if activeTab === 'security'}
            <!-- Pestana de seguridad con formulario de cambio de contrasena y consejos -->
            <section class="profile-section">
              <h2 class="profile-section__title">Seguridad</h2>
              <p class="profile-section__subtitle">Cambia tu contrasena</p>

              <!-- Formulario de cambio de contrasena con indicadores de fortaleza -->
              <div class="profile-form">
                <div class="profile-form__row">
                  <div class="profile-form__field profile-form__field--full">
                    <label for="profile-current-pw" class="profile-form__label">Contrasena Actual</label>
                    <input type="password" id="profile-current-pw" name="profile-current-pw"
                      class="profile-form__input"
                      bind:value={passwordData.currentPassword}
                      placeholder="Tu contrasena actual"
                      autocomplete="new-password" />
                  </div>
                </div>

                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label for="profile-new-pw" class="profile-form__label">Nueva Contrasena</label>
                    <input type="password" id="profile-new-pw" name="profile-new-pw"
                      class="profile-form__input"
                      bind:value={passwordData.newPassword}
                      placeholder="Minimo 8 caracteres"
                      autocomplete="new-password" />
                    {#if passwordData.newPassword.length > 0}
                      <div class="password-strength">
                        <span class="password-strength__item" class:ok={ps.length}>
                          {ps.length ? '✓' : '✗'} 8 caracteres minimo
                        </span>
                        <span class="password-strength__item" class:ok={ps.uppercase}>
                          {ps.uppercase ? '✓' : '✗'} 1 mayuscula
                        </span>
                        <span class="password-strength__item" class:ok={ps.number}>
                          {ps.number ? '✓' : '✗'} 1 numero
                        </span>
                        <span class="password-strength__item" class:ok={ps.special}>
                          {ps.special ? '✓' : '✗'} 1 caracter especial (#, @, !, $...)
                        </span>
                      </div>
                    {/if}
                  </div>
                  <div class="profile-form__field">
                    <label for="profile-confirm-pw" class="profile-form__label">Confirmar Nueva Contrasena</label>
                    <input type="password" id="profile-confirm-pw" name="profile-confirm-pw"
                      class="profile-form__input"
                      bind:value={passwordData.confirmPassword}
                      placeholder="Repite la contrasena"
                      autocomplete="new-password" />
                    {#if passwordData.confirmPassword.length > 0 && passwordData.newPassword !== passwordData.confirmPassword}
                      <span style="color:#c62828;font-size:.82rem">Las contrasenas no coinciden.</span>
                    {/if}
                  </div>
                </div>

                {#if passwordError}
                  <span style="color:#c62828;font-size:.85rem">{passwordError}</span>
                {/if}
                {#if passwordMensaje}
                  <span style="color:#2e7d32;font-size:.85rem">{passwordMensaje}</span>
                {/if}

                <button class="profile-form__submit"
                  on:click={handleCambiarContrasena}
                  disabled={guardandoPassword}>
                  {guardandoPassword ? 'Actualizando...' : 'Cambiar Contrasena'}
                </button>
              </div>

              <!-- Lista de consejos de seguridad para el manejo de la contrasena -->
              <div class="security-info" style="margin-top:2rem">
                <h3 class="security-info__title">Consejos de Seguridad</h3>
                <ul class="security-info__list">
                  <li class="security-info__item">Usa una contrasena unica y segura</li>
                  <li class="security-info__item">Combina letras mayusculas, minusculas, numeros y simbolos</li>
                  <li class="security-info__item">No compartas tu contrasena con nadie</li>
                  <li class="security-info__item">Cambia tu contrasena periodicamente</li>
                </ul>
              </div>
            </section>
          {/if}

        </main>
      </div>
    {/if}
  </div>
</div>
