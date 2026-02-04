<script>
  import '../styles/profile.css';
  export let navigateTo;

  let activeTab = 'personal';

  let userData = {
    nombre: 'Juan Carlos',
    apellido: 'Lopez Garcia',
    email: 'juancarlos@ejemplo.com',
    telefono: '+502 1234-5678',
    fechaNacimiento: '1990-05-15',
    pais: 'Guatemala',
    ciudad: 'Ciudad de Guatemala',
    direccion: 'Zona 10, Ciudad de Guatemala',
    codigoPostal: '01010'
  };

  let passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  function handleUpdateProfile() {
    console.log('Actualizando perfil:', userData);
    alert('Perfil actualizado exitosamente');
  }

  function handleChangePassword() {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert('Las contraseñas no coinciden');
      return;
    }
    console.log('Cambiando contraseña');
    alert('Contraseña actualizada exitosamente');
    passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }

  function handleLogout() {
    console.log('Cerrando sesion');
    navigateTo('home');
  }
</script>

<div class="profile">
  <div class="profile__container">
    <div class="profile__header">
      <button class="profile__back" on:click={() => navigateTo('home')}>
        Volver al inicio
      </button>
      <h1 class="profile__title">Mi Perfil</h1>
    </div>

    <div class="profile__content">
      <aside class="profile__sidebar">
        <div class="profile-card">
          <div class="profile-card__avatar">
            <span class="profile-card__avatar-text">
              {userData.nombre.charAt(0)}{userData.apellido.charAt(0)}
            </span>
          </div>
          <h2 class="profile-card__name">{userData.nombre} {userData.apellido}</h2>
          <p class="profile-card__email">{userData.email}</p>
        </div>

        <nav class="profile-nav">
          <button 
            class="profile-nav__item" 
            class:profile-nav__item--active={activeTab === 'personal'}
            on:click={() => activeTab = 'personal'}
          >
            Informacion Personal
          </button>
          <button 
            class="profile-nav__item" 
            class:profile-nav__item--active={activeTab === 'security'}
            on:click={() => activeTab = 'security'}
          >
            Seguridad
          </button>
        </nav>

        <button class="profile-logout" on:click={handleLogout}>
          Cerrar Sesion
        </button>
      </aside>

      <main class="profile__main">
        {#if activeTab === 'personal'}
          <section class="profile-section">
            <h2 class="profile-section__title">Informacion Personal</h2>
            <p class="profile-section__subtitle">Actualiza tus datos personales</p>

            <form class="profile-form" on:submit|preventDefault={handleUpdateProfile}>
              <div class="profile-form__row">
                <div class="profile-form__field">
                  <label for="nombre" class="profile-form__label">Nombre</label>
                  <input 
                    type="text" 
                    id="nombre"
                    class="profile-form__input"
                    bind:value={userData.nombre}
                    required
                  />
                </div>

                <div class="profile-form__field">
                  <label for="apellido" class="profile-form__label">Apellido</label>
                  <input 
                    type="text" 
                    id="apellido"
                    class="profile-form__input"
                    bind:value={userData.apellido}
                    required
                  />
                </div>
              </div>

              <div class="profile-form__row">
                <div class="profile-form__field">
                  <label for="email" class="profile-form__label">Email</label>
                  <input 
                    type="email" 
                    id="email"
                    class="profile-form__input"
                    bind:value={userData.email}
                    required
                  />
                </div>

                <div class="profile-form__field">
                  <label for="telefono" class="profile-form__label">Telefono</label>
                  <input 
                    type="tel" 
                    id="telefono"
                    class="profile-form__input"
                    bind:value={userData.telefono}
                    required
                  />
                </div>
              </div>

              <div class="profile-form__row">
                <div class="profile-form__field">
                  <label for="fechaNacimiento" class="profile-form__label">Fecha de Nacimiento</label>
                  <input 
                    type="date" 
                    id="fechaNacimiento"
                    class="profile-form__input"
                    bind:value={userData.fechaNacimiento}
                    required
                  />
                </div>

                <div class="profile-form__field">
                  <label for="pais" class="profile-form__label">Pais</label>
                  <input 
                    type="text" 
                    id="pais"
                    class="profile-form__input"
                    bind:value={userData.pais}
                    required
                  />
                </div>
              </div>

              <div class="profile-form__row">
                <div class="profile-form__field">
                  <label for="ciudad" class="profile-form__label">Ciudad</label>
                  <input 
                    type="text" 
                    id="ciudad"
                    class="profile-form__input"
                    bind:value={userData.ciudad}
                    required
                  />
                </div>

                <div class="profile-form__field">
                  <label for="codigoPostal" class="profile-form__label">Codigo Postal</label>
                  <input 
                    type="text" 
                    id="codigoPostal"
                    class="profile-form__input"
                    bind:value={userData.codigoPostal}
                    required
                  />
                </div>
              </div>

              <div class="profile-form__row">
                <div class="profile-form__field profile-form__field--full">
                  <label for="direccion" class="profile-form__label">Direccion</label>
                  <input 
                    type="text" 
                    id="direccion"
                    class="profile-form__input"
                    bind:value={userData.direccion}
                    required
                  />
                </div>
              </div>

              <button type="submit" class="profile-form__submit">
                Guardar Cambios
              </button>
            </form>
          </section>

        {:else if activeTab === 'security'}
          <section class="profile-section">
            <h2 class="profile-section__title">Seguridad</h2>
            <p class="profile-section__subtitle">Cambia tu contraseña</p>

            <form class="profile-form" on:submit|preventDefault={handleChangePassword}>
              <div class="profile-form__row">
                <div class="profile-form__field profile-form__field--full">
                  <label for="currentPassword" class="profile-form__label">Contraseña Actual</label>
                  <input 
                    type="password" 
                    id="currentPassword"
                    class="profile-form__input"
                    bind:value={passwordData.currentPassword}
                    placeholder="Tu contraseña actual"
                    required
                  />
                </div>
              </div>

              <div class="profile-form__row">
                <div class="profile-form__field">
                  <label for="newPassword" class="profile-form__label">Nueva Contraseña</label>
                  <input 
                    type="password" 
                    id="newPassword"
                    class="profile-form__input"
                    bind:value={passwordData.newPassword}
                    placeholder="Minimo 8 caracteres"
                    minlength="8"
                    required
                  />
                </div>

                <div class="profile-form__field">
                  <label for="confirmNewPassword" class="profile-form__label">Confirmar Nueva Contraseña</label>
                  <input 
                    type="password" 
                    id="confirmNewPassword"
                    class="profile-form__input"
                    bind:value={passwordData.confirmPassword}
                    placeholder="Repite la contraseña"
                    minlength="8"
                    required
                  />
                </div>
              </div>

              <button type="submit" class="profile-form__submit">
                Cambiar Contraseña
              </button>
            </form>

            <div class="security-info">
              <h3 class="security-info__title">Consejos de Seguridad</h3>
              <ul class="security-info__list">
                <li class="security-info__item">Usa una contraseña unica y segura</li>
                <li class="security-info__item">Combina letras mayusculas, minusculas, numeros y simbolos</li>
                <li class="security-info__item">No compartas tu contraseña con nadie</li>
                <li class="security-info__item">Cambia tu contraseña periodicamente</li>
              </ul>
            </div>
          </section>
        {/if}
      </main>
    </div>
  </div>
</div>