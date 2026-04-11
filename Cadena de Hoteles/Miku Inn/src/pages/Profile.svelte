<script>
  /**
   * @file Profile.svelte
   * @description Pagina de perfil del usuario autenticado. Muestra la informacion
   * de la cuenta en modo solo lectura y ofrece formularios para actualizar el
   * numero de telefono (con selector de pais y codigo de marcado) y cambiar la
   * contrasena actual.
   */

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo;
  import '../styles/profile.css';
  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  const API = 'http://localhost:7000';

  /** Datos del perfil cargados desde el servidor. @type {any} */
  let perfil = null;

  /** Indica si la carga inicial esta en curso. @type {boolean} */
  let loading = true;

  /** Mensaje de error global si el perfil no se pudo cargar. @type {string} */
  let serverError = '';

  /** Nuevo numero de telefono que el usuario esta escribiendo. @type {string} */
  let nuevoTelefono = '';

  /** Mensaje de exito al guardar el telefono. @type {string} */
  let telefonoMsg = '';

  /** Mensaje de error en el formulario de telefono. @type {string} */
  let telefonoError = '';

  /** Indica si la peticion de actualizar telefono esta en vuelo. @type {boolean} */
  let savingTelefono = false;

  /** Texto del buscador de pais. @type {string} */
  let paisQuery = '';

  /** Sugerencias de paises filtradas mientras el usuario escribe. @type {any[]} */
  let paisesSugeridos = [];

  /** Lista completa de paises cargada desde countriesnow. @type {any[]} */
  let todosLosPaises = [];

  /** Mapa de codigo de marcado por nombre de pais. @type {Object.<string, {code: string, digits: number}>} */
  let dialCodesMap = {};

  /** Codigo de marcado internacional del pais seleccionado (ej. "+502"). @type {string} */
  let dialCode = '';

  /** Cantidad de digitos locales requeridos para el pais seleccionado. @type {number} */
  let phoneDigitCount = 9;

  /** Pais seleccionado del autocomplete. @type {string|null} */
  let paisSeleccionado = null;

  /** Contrasena actual ingresada por el usuario. @type {string} */
  let contrasenaActual = '';

  /** Nueva contrasena deseada. @type {string} */
  let contrasenaNueva = '';

  /** Confirmacion de la nueva contrasena. @type {string} */
  let confirmarNueva = '';

  /** Alterna la visibilidad del campo de contrasena actual. @type {boolean} */
  let showActual = false;

  /** Alterna la visibilidad del campo de nueva contrasena. @type {boolean} */
  let showNueva = false;

  /** Alterna la visibilidad del campo de confirmacion. @type {boolean} */
  let showConfirmar = false;

  /** Mensaje de exito al cambiar la contrasena. @type {string} */
  let contrasenaMsg = '';

  /** Mensaje de error en el formulario de contrasena. @type {string} */
  let contrasenaError = '';

  /** Indica si la peticion de cambio de contrasena esta en vuelo. @type {boolean} */
  let savingContrasena = false;

  /**
   * Tabla de cantidad de digitos locales por codigo de marcado internacional.
   * Basada en los estandares ITU, igual que en Register.svelte.
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
   * Da formato visual a un numero local segun la cantidad de digitos del pais.
   * @param {string} digits - Digitos sin formato.
   * @param {number} total - Total de digitos esperados para el pais.
   * @returns {string} Numero formateado con espacios.
   */
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  /**
   * Maneja el evento de escritura en el campo de telefono.
   * Extrae solo digitos, limita al maximo del pais y aplica formato visual.
   * @param {Event} e - Evento de input.
   */
  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '');
    const capped = raw.slice(0, phoneDigitCount);
    nuevoTelefono = formatLocalPhone(capped, phoneDigitCount);
  }

  /**
   * Genera el texto placeholder de ejemplo para el campo de telefono.
   * @param {number} digits - Total de digitos del pais.
   * @returns {string}
   */
  function getPhonePlaceholder(digits) {
    return formatLocalPhone('5'.repeat(digits), digits);
  }

  /**
   * Carga el perfil del usuario, la lista de paises y los codigos de marcado
   * al montar el componente. Pre-selecciona el pais del perfil si ya esta guardado.
   */
  onMount(async () => {
    // Cargar datos del perfil desde el backend
    try {
      const res = await fetch(`${API}/usuarios/perfil`, { credentials: 'include' });
      if (!res.ok) { serverError = 'No se pudo cargar el perfil.'; loading = false; return; }
      perfil = await res.json();
      nuevoTelefono = '';
    } catch { serverError = 'Error de conexión.'; }
    finally { loading = false; }

    // Cargar lista de paises desde la API de countriesnow
    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch { console.error('Error cargando países'); }

    // Construir mapa de codigos de marcado usando restcountries + knownDigits
    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,idd');
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

      // Pre-seleccionar el pais guardado en el perfil
      if (perfil?.pais) {
        const info = dialCodesMap[perfil.pais.toLowerCase()];
        if (info) {
          dialCode = info.code;
          phoneDigitCount = info.digits;
          paisQuery = perfil.pais;
          paisSeleccionado = perfil.pais;
          // Extraer solo los digitos locales sin el codigo de marcado
          if (perfil.telefono) {
            const sinCodigo = perfil.telefono.replace(info.code, '').trim();
            const soloDigitos = sinCodigo.replace(/\D/g, '').slice(0, info.digits);
            nuevoTelefono = formatLocalPhone(soloDigitos, info.digits);
          }
        }
      }
    } catch { console.error('Error cargando dial codes'); }
  });

  /**
   * Filtra las sugerencias de paises a medida que el usuario escribe.
   * Limpia el codigo de marcado si no hay pais confirmado.
   */
  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2
      ? []
      : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (!paisSeleccionado) { dialCode = ''; nuevoTelefono = ''; }
  }

  /**
   * Confirma la seleccion de un pais del autocomplete y actualiza el codigo
   * de marcado y el contador de digitos correspondiente.
   * @param {{ country: string }} p - Pais seleccionado.
   */
  function seleccionarPais(p) {
    paisSeleccionado = p.country;
    paisQuery = p.country;
    paisesSugeridos = [];
    const info = dialCodesMap[p.country.toLowerCase()];
    dialCode = info?.code ?? '';
    phoneDigitCount = info?.digits ?? 9;
    nuevoTelefono = '';
    telefonoError = '';
  }

  /**
   * Valida que el usuario haya seleccionado un pais de la lista al salir
   * del campo. Si escribio texto libre sin confirmar, lo limpia.
   */
  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) {
      telefonoError = 'Selecciona un país de la lista';
      paisQuery = '';
    }
  }

  /**
   * Envia el nuevo numero de telefono al servidor tras validar que tenga
   * exactamente la cantidad de digitos requerida para el pais elegido.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarTelefono() {
    telefonoMsg = ''; telefonoError = '';
    if (!paisSeleccionado) { telefonoError = 'Selecciona un país primero.'; return; }
    if (!nuevoTelefono.trim()) { telefonoError = 'Ingresa un número válido.'; return; }
    const digitCount = nuevoTelefono.replace(/\D/g, '').length;
    if (digitCount !== phoneDigitCount) {
      telefonoError = `El número debe tener exactamente ${phoneDigitCount} dígitos para ${paisSeleccionado}.`;
      return;
    }
    savingTelefono = true;
    const telefonoCompleto = dialCode + ' ' + nuevoTelefono.replace(/\s/g, '');
    try {
      const res = await fetch(`${API}/usuarios/telefono`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ telefono: telefonoCompleto })
      });
      const data = await res.json();
      if (res.ok) {
        telefonoMsg = data.mensaje ?? 'Teléfono actualizado.';
        perfil.telefono = telefonoCompleto;
      } else {
        telefonoError = data.mensaje ?? 'Error al actualizar.';
      }
    } catch { telefonoError = 'Error de conexión.'; }
    finally { savingTelefono = false; }
  }

  /**
   * Valida y envia el cambio de contrasena al servidor. Verifica que la nueva
   * contrasena tenga al menos 8 caracteres y que la confirmacion coincida.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarContrasena() {
    contrasenaMsg = ''; contrasenaError = '';
    if (!contrasenaActual) { contrasenaError = 'Ingresa tu contraseña actual.'; return; }
    if (contrasenaNueva.length < 8) { contrasenaError = 'Mínimo 8 caracteres.'; return; }
    if (contrasenaNueva !== confirmarNueva) { contrasenaError = 'Las contraseñas no coinciden.'; return; }
    savingContrasena = true;
    try {
      const res = await fetch(`${API}/usuarios/contrasena`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ contrasenaActual, contrasenaNueva })
      });
      const data = await res.json();
      if (res.ok) {
        contrasenaMsg = data.mensaje ?? 'Contraseña actualizada.';
        contrasenaActual = ''; contrasenaNueva = ''; confirmarNueva = '';
      } else {
        contrasenaError = data.mensaje ?? 'Error al actualizar.';
      }
    } catch { contrasenaError = 'Error de conexión.'; }
    finally { savingContrasena = false; }
  }
</script>

<div class="profile-page">
  <div class="profile-container">

    <!-- Estado de carga -->
    {#if loading}
      <div class="profile-loading">
        <div class="profile-spinner"></div>
        <p>Cargando perfil...</p>
      </div>

    <!-- Estado de error con opcion de volver al inicio -->
    {:else if serverError}
      <div class="profile-error-box">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p>{serverError}</p>
        <button class="profile-btn-primary" on:click={() => navigateTo('home')}>Volver al inicio</button>
      </div>

    {:else if perfil}

      <!-- Hero del perfil con avatar, nombre y etiquetas de pais/nacionalidad -->
      <div class="profile-hero">
        <div class="profile-avatar-big">{perfil.nombre.charAt(0).toUpperCase()}</div>
        <div class="profile-hero-info">
          <h1 class="profile-nombre">{perfil.nombre} {perfil.apellido}</h1>
          <p class="profile-username">@{perfil.username}</p>
          <div class="profile-tags">
            <span class="profile-tag">{perfil.pais}</span>
            <span class="profile-tag">{perfil.ciudad}</span>
            {#each perfil.nacionalidades as nac}
              <span class="profile-tag nac">{nac}</span>
            {/each}
          </div>
        </div>
      </div>

      <!-- Tarjeta de informacion de la cuenta en modo solo lectura -->
      <div class="profile-card">
        <h2 class="profile-section-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
          </svg>
          Información de la cuenta
        </h2>
        <div class="profile-info-grid">
          <div class="profile-info-item">
            <span class="profile-info-label">Correo</span>
            <span class="profile-info-value">{perfil.correo}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-info-label">Pasaporte</span>
            <span class="profile-info-value">{perfil.pasaporte}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-info-label">Fecha de nacimiento</span>
            <span class="profile-info-value">{perfil.fechaNacimiento}</span>
          </div>
          <div class="profile-info-item">
            <span class="profile-info-label">Teléfono actual</span>
            <span class="profile-info-value">{perfil.telefono}</span>
          </div>
        </div>
      </div>

      <!-- Tarjeta para actualizar el numero de telefono -->
      <div class="profile-card">
        <h2 class="profile-section-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.21h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.96a16 16 0 0 0 6 6l.92-.92a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
          Actualizar teléfono
        </h2>

        {#if telefonoMsg}<div class="profile-alert success">{telefonoMsg}</div>{/if}
        {#if telefonoError}<div class="profile-alert error">{telefonoError}</div>{/if}

        <!-- Selector de pais con autocompletado -->
        <div class="profile-field">
          <label for="pais-telefono">País del número</label>
          <div class="profile-autocomplete-wrap">
            <input
              type="text"
              id="pais-telefono"
              bind:value={paisQuery}
              on:input={onPaisInput}
              on:blur={validarPaisSeleccionado}
              placeholder="Escribe tu país..."
              autocomplete="off"
            />
            {#if paisesSugeridos.length > 0}
              <ul class="profile-autocomplete-list">
                {#each paisesSugeridos as p}
                  <li>
                    <button type="button" class="profile-autocomplete-btn" on:click={() => seleccionarPais(p)}>
                      {p.country}
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>
        </div>

        <!-- Campo de telefono con prefijo de marcado internacional -->
        <div class="profile-field">
          <label for="telefono">
            Nuevo número
            {#if paisSeleccionado && phoneDigitCount}
              <span class="profile-info-label"> — {phoneDigitCount} dígitos requeridos</span>
            {/if}
          </label>
          <div class="profile-phone-wrap">
            {#if dialCode}
              <span class="profile-dial-code">{dialCode}</span>
            {/if}
            <input
              type="tel"
              id="telefono"
              bind:value={nuevoTelefono}
              on:input={onPhoneInput}
              placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un país primero'}
              disabled={!dialCode}
            />
          </div>
          <!-- Indicador de progreso de digitos ingresados -->
          {#if nuevoTelefono && !telefonoError}
            {@const d = nuevoTelefono.replace(/\D/g, '').length}
            {#if d === phoneDigitCount}
              <span class="profile-match ok">✓ Número completo</span>
            {:else}
              <span class="profile-match err">{d}/{phoneDigitCount} dígitos</span>
            {/if}
          {/if}
        </div>

        <button class="profile-btn-primary" on:click={guardarTelefono} disabled={savingTelefono}>
          {#if savingTelefono}
            <svg class="btn-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            Guardando...
          {:else}
            Guardar teléfono
          {/if}
        </button>
      </div>

      <!-- Tarjeta para cambiar la contrasena de la cuenta -->
      <div class="profile-card">
        <h2 class="profile-section-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          Cambiar contraseña
        </h2>

        {#if contrasenaMsg}<div class="profile-alert success">{contrasenaMsg}</div>{/if}
        {#if contrasenaError}<div class="profile-alert error">{contrasenaError}</div>{/if}

        <!-- Campo de contrasena actual con toggle de visibilidad -->
        <div class="profile-field">
          <label for="pwd-actual">Contraseña actual</label>
          <div class="profile-pwd-wrap">
            <input type={showActual ? 'text' : 'password'} id="pwd-actual" bind:value={contrasenaActual} placeholder="Tu contraseña actual" />
            <button type="button" class="profile-eye" on:click={() => showActual = !showActual} tabindex="-1">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {#if showActual}<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                {:else}<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>{/if}
              </svg>
            </button>
          </div>
        </div>

        <!-- Campo de nueva contrasena con toggle de visibilidad -->
        <div class="profile-field">
          <label for="pwd-nueva">Nueva contraseña</label>
          <div class="profile-pwd-wrap">
            <input type={showNueva ? 'text' : 'password'} id="pwd-nueva" bind:value={contrasenaNueva} placeholder="Mínimo 8 caracteres" />
            <button type="button" class="profile-eye" on:click={() => showNueva = !showNueva} tabindex="-1">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {#if showNueva}<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                {:else}<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>{/if}
              </svg>
            </button>
          </div>
        </div>

        <!-- Campo de confirmacion de nueva contrasena con indicador de coincidencia -->
        <div class="profile-field">
          <label for="pwd-confirmar">Confirmar nueva contraseña</label>
          <div class="profile-pwd-wrap">
            <input type={showConfirmar ? 'text' : 'password'} id="pwd-confirmar" bind:value={confirmarNueva} placeholder="Repite la nueva contraseña" />
            <button type="button" class="profile-eye" on:click={() => showConfirmar = !showConfirmar} tabindex="-1">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {#if showConfirmar}<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                {:else}<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>{/if}
              </svg>
            </button>
          </div>
          {#if confirmarNueva && contrasenaNueva === confirmarNueva}
            <span class="profile-match ok">✓ Las contraseñas coinciden</span>
          {:else if confirmarNueva}
            <span class="profile-match err">✗ No coinciden</span>
          {/if}
        </div>

        <button class="profile-btn-primary" on:click={guardarContrasena} disabled={savingContrasena}>
          {#if savingContrasena}
            <svg class="btn-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            Guardando...
          {:else}
            Cambiar contraseña
          {/if}
        </button>
      </div>

    {/if}
  </div>
</div>
