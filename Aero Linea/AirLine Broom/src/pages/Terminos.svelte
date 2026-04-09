<script>
/**
 * @file Terminos.svelte
 * @description Pagina estatica de Terminos y Condiciones de Broom AirLine. Cubre seis areas:
 * requisitos de registro de cuenta (correo unico, verificacion, asignacion de rol), reglas de
 * busqueda y disponibilidad de vuelos (actualizaciones en tiempo real, filtros), el proceso de
 * compra y reserva (confirmacion de pago, generacion de codigo unico, bloqueo de asiento por 10
 * minutos, compras grupales, correo de confirmacion con PDF), metodos de pago y procesamiento
 * SSL, politica de comentarios y calificaciones (solo vuelos completados, escala de 1 a 5
 * estrellas, una calificacion por vuelo, moderacion de administrador) y responsabilidades de la
 * plataforma incluyendo obligaciones de documentacion del pasajero y el derecho de la aerolinea a
 * cancelar vuelos con notificacion automatica. Accesible desde la navegacion principal con un
 * boton de regreso al inicio.
 */
// @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion utilizada para navegar a otra pagina en la aplicacion. @type {function} */
  export let navigateTo;
</script>

<!-- Contenedor principal de la pagina de Terminos y Condiciones -->
<div class="info-page">

  <!-- Seccion hero con icono de documento, nombre de la aerolinea y subtitulo -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Terminos y Condiciones</h1>
      <p class="info-hero__subtitle">Lee atentamente los terminos que rigen el uso de nuestra plataforma y la compra de boletos.</p>
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

    <!-- Tarjeta unica con todas las secciones de los terminos legales vigentes -->
    <div class="info-card">
      <span class="info-badge">Vigente desde febrero 2026</span>

      <!-- Terminos de registro: requisitos de cuenta, verificacion y restricciones de rol -->
      <h2 class="info-section-title">Registro y acceso</h2>
      <p class="info-prose">Para utilizar Broom AirLine debes crear una cuenta proporcionando nombre, apellido, correo electronico, telefono, fecha de nacimiento, pais, ciudad y una contrasena que cumpla con los requisitos minimos de seguridad (8 caracteres, al menos 1 mayuscula y 1 numero).</p>
      <ul class="info-list">
        <li>El correo electronico debe ser unico: el sistema no permite cuentas duplicadas</li>
        <li>Recibiras un correo de verificacion — no podras iniciar sesion hasta verificar tu direccion de email</li>
        <li>Los usuarios nuevos son asignados automaticamente al rol de Cliente Registrado</li>
        <li>Solo un Administrador puede cambiar el rol de otros usuarios</li>
        <li>Queda prohibido el uso de cuentas de terceros o la creacion de cuentas multiples</li>
      </ul>

      <!-- Reglas de busqueda y disponibilidad de vuelos en tiempo real -->
      <h2 class="info-section-title">Busqueda y disponibilidad de vuelos</h2>
      <ul class="info-list">
        <li>El sistema muestra unicamente vuelos con asientos disponibles; los vuelos pasados o sin asientos no aparecen en resultados</li>
        <li>Puedes filtrar por origen, destino, fecha, cantidad de pasajeros, rango de precio, tipo de vuelo (directo o con escalas) y rating</li>
        <li>La disponibilidad se actualiza en tiempo real despues de cada reserva o cancelacion</li>
        <li>Si un vuelo se llena mientras lo visualizas, el sistema mostrara un mensaje al intentar reservar</li>
      </ul>

      <!-- Flujo de compra y reserva: confirmacion de pago, bloqueo de asiento y boleto PDF -->
      <h2 class="info-section-title">Proceso de compra y reservas</h2>
      <ul class="info-list">
        <li>Una reserva se confirma unicamente al completar el pago exitosamente</li>
        <li>Al confirmar se genera un codigo unico alfanumerico (ejemplo: #VGT-2026-A7B9C2) que identifica tu reserva</li>
        <li>El sistema bloquea temporalmente el asiento por 10 minutos durante el proceso de compra; si no se completa, el asiento se libera automaticamente</li>
        <li>Para compras grupales se solicita la informacion de cada pasajero; todos los boletos del grupo comparten el mismo codigo de reserva</li>
        <li>Inmediatamente despues del pago recibiras un correo con los detalles de tu reserva y el boleto en PDF adjunto</li>
      </ul>

      <!-- Metodos de pago aceptados y politica de seguridad de datos financieros -->
      <h2 class="info-section-title">Pagos</h2>
      <ul class="info-list">
        <li>Aceptamos tarjetas de credito y debito</li>
        <li>Todos los pagos se procesan con cifrado SSL de extremo a extremo</li>
        <li>No almacenamos los datos completos de tu tarjeta de pago</li>
        <li>Cada compra genera un numero de transaccion unico registrado en el sistema de forma permanente</li>
        <li>Si el pago falla, se muestra un mensaje de error especifico y la reserva no se guarda</li>
      </ul>

      <!-- Politica de comentarios y sistema de calificacion por estrellas para vuelos completados -->
      <h2 class="info-section-title">Comentarios y calificaciones</h2>
      <ul class="info-list">
        <li>Solo usuarios que hayan completado un vuelo pueden dejar comentarios (hasta 500 caracteres) o calificaciones</li>
        <li>El sistema de rating usa una escala de 1 a 5 estrellas; cada usuario puede dar una sola calificacion por vuelo</li>
        <li>Los administradores pueden moderar y eliminar comentarios inapropiados</li>
      </ul>

      <!-- Responsabilidades de la aerolinea y obligaciones del pasajero -->
      <h2 class="info-section-title">Responsabilidades</h2>
      <ul class="info-list">
        <li>Broom AirLine no se hace responsable por retrasos o cancelaciones causados por fuerza mayor, condiciones climaticas extremas o disposiciones de terceros</li>
        <li>El pasajero es responsable de contar con la documentacion vigente requerida para cada vuelo</li>
        <li>Broom AirLine se reserva el derecho de cancelar vuelos completos notificando automaticamente a todos los pasajeros afectados</li>
      </ul>

      <!-- Bloque de contacto para consultas sobre los terminos legales -->
      <div class="info-highlight">
        Para consultas sobre estos terminos, contactanos en <strong>info@broomairline.com</strong> o al <strong>+502 2000-0000</strong>
      </div>
    </div>
  </div>
</div>
