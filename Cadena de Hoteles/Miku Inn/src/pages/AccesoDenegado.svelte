<script>
  /**
   * @file AccesoDenegado.svelte
   * @description Pagina de error 403 que se muestra cuando un usuario intenta
   * acceder a una ruta para la que no tiene permisos. Incluye una cuenta
   * regresiva de 5 segundos que redirige automaticamente al inicio.
   */

  // @ts-nocheck
  import { onMount } from 'svelte';
  import '../styles/acceso-denegado.css';

  /** Funcion de navegacion recibida desde App. @type {Function} */
  export let navigateTo;

  /** Segundos restantes antes de la redireccion automatica al inicio. @type {number} */
  let countdown = 5;

  /** Referencia al intervalo del temporizador para poder cancelarlo. @type {number} */
  let interval;

  onMount(() => {
    // Iniciar la cuenta regresiva que redirige al home al llegar a 0
    interval = setInterval(() => {
      countdown -= 1;
      if (countdown <= 0) {
        clearInterval(interval);
        navigateTo('home');
      }
    }, 1000);

    return () => clearInterval(interval);
  });

  /**
   * Cancela el temporizador y navega al inicio de forma inmediata.
   */
  function goHome() {
    clearInterval(interval);
    navigateTo('home');
  }
</script>

<div class="acceso-denegado-page">

  <!-- Fondo animado con orbs decorativos y cuadricula -->
  <div class="ad-bg">
    <div class="ad-orb ad-orb--1"></div>
    <div class="ad-orb ad-orb--2"></div>
    <div class="ad-orb ad-orb--3"></div>
    <div class="ad-grid"></div>
  </div>

  <div class="ad-card">

    <!-- Icono de candado con gradiente SVG -->
    <div class="ad-lock-wrapper">
      <div class="ad-lock-ring"></div>
      <div class="ad-lock-icon">
        <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" width="52" height="52">
          <rect x="12" y="28" width="40" height="28" rx="5" fill="url(#lockGrad)" />
          <path d="M20 28V20a12 12 0 0 1 24 0v8" stroke="url(#lockGrad)" stroke-width="4" stroke-linecap="round"/>
          <circle cx="32" cy="42" r="4" fill="white"/>
          <rect x="30" y="42" width="4" height="7" rx="2" fill="white"/>
          <defs>
            <linearGradient id="lockGrad" x1="12" y1="20" x2="52" y2="56" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#667eea"/>
              <stop offset="100%" stop-color="#764ba2"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
    </div>

    <!-- Codigo de error HTTP -->
    <div class="ad-error-code">403</div>

    <!-- Titulo y mensajes explicativos -->
    <h1 class="ad-title">Acceso Denegado</h1>
    <p class="ad-subtitle">
      No tienes los permisos necesarios para acceder a esta sección.
    </p>
    <p class="ad-detail">
      Esta área está restringida. Si crees que esto es un error,
      contacta al administrador del sistema.
    </p>

    <!-- Cuenta regresiva visual con barra de progreso -->
    <div class="ad-countdown">
      <p class="ad-countdown__label">
        Redirigiendo al inicio en <span class="ad-countdown__number">{countdown}</span> segundos...
      </p>
      <div class="ad-progress-bar">
        <div class="ad-progress-fill"></div>
      </div>
    </div>

    <!-- Boton para ir al inicio sin esperar la cuenta regresiva -->
    <button class="ad-home-btn" on:click={goHome}>
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
        <path d="M3 12L12 3L21 12V21H15V15H9V21H3V12Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      </svg>
      Ir al Inicio Ahora
    </button>

  </div>
</div>
