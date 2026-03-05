<script>
  // @ts-nocheck
  import { onMount } from 'svelte';
  import '../styles/acceso-denegado.css';

  export let navigateTo;

  let countdown = 5;
  let interval;

  onMount(() => {
    interval = setInterval(() => {
      countdown -= 1;
      if (countdown <= 0) {
        clearInterval(interval);
        navigateTo('home');
      }
    }, 1000);

    return () => clearInterval(interval);
  });

  function goHome() {
    clearInterval(interval);
    navigateTo('home');
  }
</script>

<div class="acceso-denegado-page">

  <!-- Fondo animado -->
  <div class="ad-bg">
    <div class="ad-orb ad-orb--1"></div>
    <div class="ad-orb ad-orb--2"></div>
    <div class="ad-orb ad-orb--3"></div>
    <div class="ad-grid"></div>
  </div>

  <div class="ad-card">

    <!-- Candado -->
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

    <!-- Código de error -->
    <div class="ad-error-code">403</div>

    <!-- Textos -->
    <h1 class="ad-title">Acceso Denegado</h1>
    <p class="ad-subtitle">
      No tienes los permisos necesarios para acceder a esta sección.
    </p>
    <p class="ad-detail">
      Esta área está restringida. Si crees que esto es un error,
      contacta al administrador del sistema.
    </p>

    <!-- Cuenta regresiva -->
    <div class="ad-countdown">
      <p class="ad-countdown__label">
        Redirigiendo al inicio en <span class="ad-countdown__number">{countdown}</span> segundos...
      </p>
      <div class="ad-progress-bar">
        <div class="ad-progress-fill"></div>
      </div>
    </div>

    <!-- Botón -->
    <button class="ad-home-btn" on:click={goHome}>
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
        <path d="M3 12L12 3L21 12V21H15V15H9V21H3V12Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      </svg>
      Ir al Inicio Ahora
    </button>

  </div>
</div>