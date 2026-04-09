<script>
/**
 * @file Privacidad.svelte
 * @description Pagina estatica de politica de privacidad de Broom AirLine. Describe los datos
 * personales recopilados en el registro (nombre, correo, telefono, fecha de nacimiento, pais,
 * ciudad, hash de contrasena) y en la compra (datos del pasajero, datos de pago procesados con
 * SSL, codigo de reserva, registro de transaccion). Explica el uso de los datos (autenticacion,
 * procesamiento de reservas, notificaciones por correo, visualizacion de historial, estadisticas
 * anonimizadas, cumplimiento legal). Lista medidas de seguridad (SSL, hash de contrasena, control
 * de acceso por roles, verificacion de permisos en el servidor, logs de operaciones de API) y
 * derechos del usuario (acceso, correccion, consulta de historial, solicitud de eliminacion).
 * Accesible desde la navegacion principal con un boton de regreso al inicio.
 */
// @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion utilizada para navegar a otra pagina en la aplicacion. @type {function} */
  export let navigateTo;
</script>

<!-- Contenedor principal de la pagina de politica de privacidad -->
<div class="info-page">
  <!-- Hero con icono de escudo, titulo y descripcion general de la politica -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Politica de Privacidad</h1>
      <p class="info-hero__subtitle">Conoce como recopilamos, usamos y protegemos tu informacion personal dentro de nuestra plataforma.</p>
    </div>
  </div>

  <div class="info-container">
    <!-- Boton de regreso a la pagina de inicio -->
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
      </svg>
      Volver al inicio
    </button>

    <!-- Tarjeta de contenido con las cinco secciones de la politica de privacidad -->
    <div class="info-card">
      <span class="info-badge">Vigente desde febrero 2026</span>

      <!-- Datos personales recopilados durante el registro de cuenta -->
      <h2 class="info-section-title">Datos que recopilamos al registrarte</h2>
      <p class="info-prose">Para crear tu cuenta en Broom AirLine, el sistema solicita la siguiente informacion personal:</p>
      <ul class="info-list">
        <li>Nombre y apellido</li>
        <li>Correo electronico (unico por cuenta, verificado antes de activar el acceso)</li>
        <li>Numero de telefono</li>
        <li>Fecha de nacimiento</li>
        <li>Pais y ciudad de residencia</li>
        <li>Contrasena (almacenada con hash, nunca en texto plano)</li>
      </ul>

      <!-- Datos de transaccion y pasajero recopilados en cada compra de boleto -->
      <h2 class="info-section-title">Datos que recopilamos al comprar</h2>
      <p class="info-prose">Al realizar una reserva o compra de boleto, el sistema registra:</p>
      <ul class="info-list">
        <li>Informacion de cada pasajero (nombre, datos de identificacion)</li>
        <li>Datos del vuelo seleccionado: origen, destino, fecha, tipo de asiento</li>
        <li>Informacion de pago: numero de tarjeta, fecha de vencimiento, CVV y nombre del titular — procesada con cifrado SSL, no almacenamos datos completos de tarjetas</li>
        <li>Codigo unico de reserva generado automaticamente</li>
        <li>Registro de la transaccion: fecha, hora, monto y numero de transaccion unico</li>
      </ul>

      <!-- Finalidades para las que se utiliza la informacion recopilada -->
      <h2 class="info-section-title">Uso de tu informacion</h2>
      <p class="info-prose">Utilizamos tus datos exclusivamente para:</p>
      <ul class="info-list">
        <li>Autenticar tu acceso a la plataforma segun tu rol (Cliente, Administrador o WebService)</li>
        <li>Procesar y confirmar tus reservas de vuelo</li>
        <li>Enviarte el correo de confirmacion de compra con el boleto electronico en PDF adjunto</li>
        <li>Enviarte notificaciones sobre cambios o cancelaciones de vuelos</li>
        <li>Mostrarte tu historial de reservas y cancelaciones desde tu perfil</li>
        <li>Generar estadisticas de ventas y busquedas para mejora del servicio (datos anonimizados)</li>
        <li>Cumplir con obligaciones legales y regulatorias del sector aereo</li>
      </ul>

      <!-- Medidas tecnicas de seguridad aplicadas para proteger los datos -->
      <h2 class="info-section-title">Seguridad de tus datos</h2>
      <p class="info-prose">Broom AirLine implementa las siguientes medidas de seguridad:</p>
      <ul class="info-list">
        <li>Cifrado SSL en todas las comunicaciones y transacciones de pago</li>
        <li>Almacenamiento seguro de contrasenas mediante hashing</li>
        <li>Control de acceso basado en roles: cada usuario solo accede a las funciones de su perfil</li>
        <li>El panel administrativo es accesible unicamente para usuarios con rol Administrador</li>
        <li>Los permisos se verifican en cada peticion al servidor, no solo en la interfaz</li>
        <li>Registro de logs de todas las operaciones realizadas via API con identificacion del sistema</li>
      </ul>

      <!-- Derechos del usuario sobre sus datos con bloque de contacto -->
      <h2 class="info-section-title">Tus derechos como usuario</h2>
      <ul class="info-list">
        <li>Acceder a los datos de tu cuenta desde tu perfil en cualquier momento</li>
        <li>Solicitar la correccion de informacion incorrecta contactandonos por correo</li>
        <li>Consultar tu historial completo de reservas y cancelaciones</li>
        <li>Solicitar la eliminacion de tu cuenta escribiendonos a info@broomairline.com</li>
      </ul>

      <div class="info-highlight">
        Para ejercer cualquiera de estos derechos o si tienes dudas sobre el manejo de tu informacion, contactanos en <strong>info@broomairline.com</strong>
      </div>
    </div>
  </div>
</div>
