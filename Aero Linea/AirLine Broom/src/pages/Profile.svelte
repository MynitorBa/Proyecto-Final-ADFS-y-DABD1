<script>
  import '../styles/profile.css';
  import { onMount } from 'svelte';
  import { sesion, logout } from '../stores/sesion.js';
  export let navigateTo;

  let usuarioId = null;
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  const API = 'http://localhost:5190';

  let activeTab = 'personal';
  let cargando = true;

  let perfil = {
    nombre: '', apellido: '', correo: '', username: '',
    telefono: '', pasaporte: '', fechaNacimiento: '', pais: '', ciudad: ''
  };

  // ── Teléfono con dial code ───────────────────────────────────────
  let telefonoEditado  = '';   // solo los dígitos locales (formateados)
  let dialCode         = '';   // ej: +502
  let phoneDigitCount  = 8;    // cuántos dígitos locales requiere el país
  let dialCodesMap     = {};   // { 'guatemala': { code: '+502', digits: 8 } }
  let telefonoMensaje  = '';
  let telefonoError    = '';
  let guardandoTelefono = false;

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

  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '').slice(0, phoneDigitCount);
    telefonoEditado = formatLocalPhone(raw, phoneDigitCount);
    telefonoError = '';
  }

  function getPhonePlaceholder(digits) {
    return formatLocalPhone('5'.repeat(digits), digits);
  }

  // ── Password ─────────────────────────────────────────────────────
  let passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
  let passwordMensaje = '';
  let passwordError   = '';
  let guardandoPassword = false;

  $: ps = {
    length:    passwordData.newPassword.length >= 8,
    uppercase: /[A-Z]/.test(passwordData.newPassword),
    number:    /[0-9]/.test(passwordData.newPassword)
  };
  $: passwordValid = ps.length && ps.uppercase && ps.number;

  // ── Mount ────────────────────────────────────────────────────────
  onMount(async () => {
    if (!usuarioId) { navigateTo('login'); return; }

    // Cargar dial codes desde restcountries
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

    // Cargar perfil
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

      // Detectar dial code del país del usuario
      const info = dialCodesMap[data.pais?.toLowerCase()];
      if (info) {
        dialCode        = info.code;
        phoneDigitCount = info.digits;
        // Si el teléfono almacenado tiene el prefijo, extraer solo dígitos locales
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

  // ── Guardar teléfono ─────────────────────────────────────────────
  async function handleActualizarTelefono() {
    telefonoMensaje = '';
    telefonoError   = '';

    if (!telefonoEditado.trim()) {
      telefonoError = 'El teléfono no puede estar vacío.'; return;
    }

    if (dialCode) {
      const digitosIngresados = telefonoEditado.replace(/\D/g, '').length;
      if (digitosIngresados !== phoneDigitCount) {
        telefonoError = `Se requieren ${phoneDigitCount} dígitos para ${perfil.pais} (ingresaste ${digitosIngresados}).`;
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
      telefonoError = 'Error de conexión.';
    } finally {
      guardandoTelefono = false;
    }
  }

  // ── Cambiar contraseña ───────────────────────────────────────────
  async function handleCambiarContrasena() {
    passwordMensaje = '';
    passwordError   = '';

    if (!passwordValid) {
      passwordError = 'La contraseña debe tener al menos 8 caracteres, 1 mayúscula y 1 número.'; return;
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      passwordError = 'Las contraseñas no coinciden.'; return;
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
      passwordError = 'Error de conexión.';
    } finally {
      guardandoPassword = false;
    }
  }

  async function handleLogout() {
    await logout();
    navigateTo('home');
  }
</script>

<div class="profile">
  <div class="profile__container">
    <div class="profile__header">
      <button class="profile__back" on:click={() => navigateTo('home')}>Volver al inicio</button>
      <h1 class="profile__title">Mi Perfil</h1>
    </div>

    {#if cargando}
      <p style="text-align:center;padding:2rem">Cargando perfil...</p>
    {:else}
      <div class="profile__content">

        <!-- Sidebar -->
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
              Información Personal
            </button>
            <button class="profile-nav__item"
              class:profile-nav__item--active={activeTab === 'security'}
              on:click={() => activeTab = 'security'}>
              Seguridad
            </button>
          </nav>

          <button class="profile-logout" on:click={handleLogout}>Cerrar Sesión</button>
        </aside>

        <!-- Main -->
        <main class="profile__main">

          {#if activeTab === 'personal'}
            <section class="profile-section">
              <h2 class="profile-section__title">Información Personal</h2>
              <p class="profile-section__subtitle">Tus datos registrados en el sistema</p>

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
                    <label class="profile-form__label">País</label>
                    <input class="profile-form__input" value={perfil.pais} disabled />
                  </div>
                  <div class="profile-form__field">
                    <label class="profile-form__label">Ciudad</label>
                    <input class="profile-form__input" value={perfil.ciudad} disabled />
                  </div>
                </div>
              </div>

              <!-- ── Teléfono editable con dial code ── -->
              <div style="margin-top:2.5rem">
                <h3 class="profile-section__title" style="font-size:1.1rem;margin-bottom:.4rem">
                  Actualizar Teléfono
                </h3>
                <p style="color:var(--text-muted);font-size:.85rem;margin-bottom:1.5rem">
                  {#if dialCode}
                    Número local de <strong>{perfil.pais}</strong> — se guardará como
                    <strong>{dialCode}</strong> + tus dígitos
                  {:else}
                    Ingresa tu número de teléfono
                  {/if}
                </p>

                <div class="profile-form">
                  <div class="profile-form__row">
                    <div class="profile-form__field profile-form__field--full">
                      <label class="profile-form__label">
                        Teléfono
                        {#if dialCode}
                          <span style="font-weight:400;text-transform:none;letter-spacing:0;color:var(--primary-color)">
                            — {phoneDigitCount} dígitos requeridos
                          </span>
                        {/if}
                      </label>

                      <!-- Campo con prefijo -->
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
                          placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Teléfono'}
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

                      <!-- Indicador de progreso -->
                      {#if telefonoEditado && dialCode}
                        {@const d = telefonoEditado.replace(/\D/g,'').length}
                        {#if d === phoneDigitCount}
                          <span style="color:#2e7d32;font-size:.82rem;margin-top:.3rem;display:block">
                            ✓ Número completo ({d}/{phoneDigitCount} dígitos)
                          </span>
                        {:else}
                          <span style="color:var(--text-muted);font-size:.82rem;margin-top:.3rem;display:block">
                            {d}/{phoneDigitCount} dígitos
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
                          ✓ {telefonoMensaje}
                        </span>
                      {/if}
                    </div>
                  </div>

                  <button class="profile-form__submit"
                    on:click={handleActualizarTelefono}
                    disabled={guardandoTelefono}>
                    {guardandoTelefono ? 'Guardando...' : 'Guardar Teléfono'}
                  </button>
                </div>
              </div>
            </section>

          {:else if activeTab === 'security'}
            <section class="profile-section">
              <h2 class="profile-section__title">Seguridad</h2>
              <p class="profile-section__subtitle">Cambia tu contraseña</p>

              <div class="profile-form">
                <div class="profile-form__row">
                  <div class="profile-form__field profile-form__field--full">
                    <label for="profile-current-pw" class="profile-form__label">Contraseña Actual</label>
                    <input type="password" id="profile-current-pw" name="profile-current-pw"
                      class="profile-form__input"
                      bind:value={passwordData.currentPassword}
                      placeholder="Tu contraseña actual"
                      autocomplete="new-password" />
                  </div>
                </div>

                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label for="profile-new-pw" class="profile-form__label">Nueva Contraseña</label>
                    <input type="password" id="profile-new-pw" name="profile-new-pw"
                      class="profile-form__input"
                      bind:value={passwordData.newPassword}
                      placeholder="Mínimo 8 caracteres"
                      autocomplete="new-password" />
                    {#if passwordData.newPassword.length > 0}
                      <div class="password-strength">
                        <span class="password-strength__item" class:ok={ps.length}>
                          {ps.length ? '✓' : '✗'} 8 caracteres mínimo
                        </span>
                        <span class="password-strength__item" class:ok={ps.uppercase}>
                          {ps.uppercase ? '✓' : '✗'} 1 mayúscula
                        </span>
                        <span class="password-strength__item" class:ok={ps.number}>
                          {ps.number ? '✓' : '✗'} 1 número
                        </span>
                      </div>
                    {/if}
                  </div>
                  <div class="profile-form__field">
                    <label for="profile-confirm-pw" class="profile-form__label">Confirmar Nueva Contraseña</label>
                    <input type="password" id="profile-confirm-pw" name="profile-confirm-pw"
                      class="profile-form__input"
                      bind:value={passwordData.confirmPassword}
                      placeholder="Repite la contraseña"
                      autocomplete="new-password" />
                    {#if passwordData.confirmPassword.length > 0 && passwordData.newPassword !== passwordData.confirmPassword}
                      <span style="color:#c62828;font-size:.82rem">Las contraseñas no coinciden.</span>
                    {/if}
                  </div>
                </div>

                {#if passwordError}
                  <span style="color:#c62828;font-size:.85rem">{passwordError}</span>
                {/if}
                {#if passwordMensaje}
                  <span style="color:#2e7d32;font-size:.85rem">✓ {passwordMensaje}</span>
                {/if}

                <button class="profile-form__submit"
                  on:click={handleCambiarContrasena}
                  disabled={guardandoPassword}>
                  {guardandoPassword ? 'Actualizando...' : 'Cambiar Contraseña'}
                </button>
              </div>

              <div class="security-info" style="margin-top:2rem">
                <h3 class="security-info__title">Consejos de Seguridad</h3>
                <ul class="security-info__list">
                  <li class="security-info__item">Usa una contraseña única y segura</li>
                  <li class="security-info__item">Combina letras mayúsculas, minúsculas, números y símbolos</li>
                  <li class="security-info__item">No compartas tu contraseña con nadie</li>
                  <li class="security-info__item">Cambia tu contraseña periódicamente</li>
                </ul>
              </div>
            </section>
          {/if}

        </main>
      </div>
    {/if}
  </div>
</div>