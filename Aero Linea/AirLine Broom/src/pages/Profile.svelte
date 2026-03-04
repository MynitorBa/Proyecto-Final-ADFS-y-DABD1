<script>
  import '../styles/profile.css';
  import { onMount } from 'svelte';
  import { sesion, logout } from '../stores/sesion.js';
  export let navigateTo;

  // Leemos del store en vez de sessionStorage
  let usuarioId = null;
  const unsubscribe = sesion.subscribe(s => {
    usuarioId = s?.usuarioId ?? null;
  });

  const API = 'http://localhost:5190';

  let activeTab = 'personal';
  let cargando = true;

  let perfil = {
    nombre: '', apellido: '', correo: '', username: '',
    telefono: '', pasaporte: '', fechaNacimiento: '', pais: '', ciudad: ''
  };

  let telefonoEditado = '';
  let telefonoMensaje = '';
  let telefonoError = '';
  let guardandoTelefono = false;

  let passwordData = { currentPassword: '', newPassword: '', confirmPassword: '' };
  let passwordMensaje = '';
  let passwordError = '';
  let guardandoPassword = false;

  $: ps = {
    length:    passwordData.newPassword.length >= 8,
    uppercase: /[A-Z]/.test(passwordData.newPassword),
    number:    /[0-9]/.test(passwordData.newPassword)
  };
  $: passwordValid = ps.length && ps.uppercase && ps.number;

  // @ts-ignore
  onMount(async () => {
    if (!usuarioId) {
      navigateTo('login');
      return;
    }

    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}`, {
        credentials: 'include'
      });
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
          ? new Date(data.fechaNacimiento).toLocaleDateString('es-GT')
          : '—',
        pais:            data.pais,
        ciudad:          data.ciudad
      };
      telefonoEditado = data.telefono;
    } catch {
      navigateTo('acceso-denegado');
    } finally {
      cargando = false;
    }

    return () => unsubscribe();
  });

  async function handleActualizarTelefono() {
    telefonoMensaje = '';
    telefonoError = '';

    if (!telefonoEditado.trim()) {
      telefonoError = 'El teléfono no puede estar vacío.';
      return;
    }

    guardandoTelefono = true;
    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}/telefono`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ telefono: telefonoEditado })
      });
      const data = await res.json();
      if (res.ok) {
        perfil.telefono = telefonoEditado;
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

  async function handleCambiarContrasena() {
    passwordMensaje = '';
    passwordError = '';

    if (!passwordValid) {
      passwordError = 'La contraseña debe tener al menos 8 caracteres, 1 mayúscula y 1 número.';
      return;
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      passwordError = 'Las contraseñas no coinciden.';
      return;
    }

    guardandoPassword = true;
    try {
      const res = await fetch(`${API}/api/perfil/${usuarioId}/contrasena`, {
        method: 'PATCH',
        credentials: 'include',
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
      <p style="text-align:center; padding: 2rem;">Cargando perfil...</p>
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
            <button
              class="profile-nav__item"
              class:profile-nav__item--active={activeTab === 'personal'}
              on:click={() => activeTab = 'personal'}
            >Información Personal</button>
            <button
              class="profile-nav__item"
              class:profile-nav__item--active={activeTab === 'security'}
              on:click={() => activeTab = 'security'}
            >Seguridad</button>
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

              <!-- Teléfono editable -->
              <div style="margin-top: 2rem;">
                <h3 class="profile-section__title" style="font-size: 1rem;">Actualizar Teléfono</h3>
                <div class="profile-form">
                  <div class="profile-form__row">
                    <div class="profile-form__field profile-form__field--full">
                      <label class="profile-form__label">Teléfono</label>
                      <input
                        type="tel"
                        class="profile-form__input"
                        bind:value={telefonoEditado}
                        placeholder="50211223344"
                      />
                      {#if telefonoError}
                        <span class="register-form__field-error">{telefonoError}</span>
                      {/if}
                      {#if telefonoMensaje}
                        <span style="color: green; font-size: 0.85rem;">{telefonoMensaje}</span>
                      {/if}
                    </div>
                  </div>
                  <button
                    class="profile-form__submit"
                    on:click={handleActualizarTelefono}
                    disabled={guardandoTelefono}
                  >
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
                    <input
                      type="password"
                      id="profile-current-pw"
                      name="profile-current-pw"
                      class="profile-form__input"
                      bind:value={passwordData.currentPassword}
                      placeholder="Tu contraseña actual"
                      autocomplete="new-password"
                    />
                  </div>
                </div>

                <div class="profile-form__row">
                  <div class="profile-form__field">
                    <label for="profile-new-pw" class="profile-form__label">Nueva Contraseña</label>
                    <input
                      type="password"
                      id="profile-new-pw"
                      name="profile-new-pw"
                      class="profile-form__input"
                      bind:value={passwordData.newPassword}
                      placeholder="Mínimo 8 caracteres"
                      autocomplete="new-password"
                    />
                    {#if passwordData.newPassword.length > 0}
                      <div class="password-strength">
                        <span class="password-strength__item" class:ok={ps.length}>{ps.length ? '✓' : '✗'} 8 caracteres mínimo</span>
                        <span class="password-strength__item" class:ok={ps.uppercase}>{ps.uppercase ? '✓' : '✗'} 1 mayúscula</span>
                        <span class="password-strength__item" class:ok={ps.number}>{ps.number ? '✓' : '✗'} 1 número</span>
                      </div>
                    {/if}
                  </div>
                  <div class="profile-form__field">
                    <label for="profile-confirm-pw" class="profile-form__label">Confirmar Nueva Contraseña</label>
                    <input
                      type="password"
                      id="profile-confirm-pw"
                      name="profile-confirm-pw"
                      class="profile-form__input"
                      bind:value={passwordData.confirmPassword}
                      placeholder="Repite la contraseña"
                      autocomplete="new-password"
                    />
                    {#if passwordData.confirmPassword.length > 0 && passwordData.newPassword !== passwordData.confirmPassword}
                      <span class="register-form__field-error">Las contraseñas no coinciden.</span>
                    {/if}
                  </div>
                </div>

                {#if passwordError}
                  <span class="register-form__field-error">{passwordError}</span>
                {/if}
                {#if passwordMensaje}
                  <span style="color: green; font-size: 0.85rem;">{passwordMensaje}</span>
                {/if}

                <button
                  class="profile-form__submit"
                  on:click={handleCambiarContrasena}
                  disabled={guardandoPassword}
                >
                  {guardandoPassword ? 'Actualizando...' : 'Cambiar Contraseña'}
                </button>
              </div>

              <div class="security-info" style="margin-top: 2rem;">
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