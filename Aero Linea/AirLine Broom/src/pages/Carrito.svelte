<script>
  // @ts-nocheck
/**
 * @file Carrito.svelte
 * @description Pagina del carrito de compras que muestra todas las reservaciones pendientes (estadoReservaId === 1)
 * del usuario autenticado. Obtiene las reservaciones de la API al montar, agrupa los boletos de cada
 * reservacion por vuelo para mostrarlos, y muestra un sidebar de resumen con los totales individuales de cada
 * reservacion y el gran total combinado. Redirige a los usuarios no autenticados a la pagina de login y
 * proporciona un boton para proceder a la pagina de pago.
 */

  import '../styles/carrito.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  /** Funcion de navegacion proporcionada por el enrutador de la aplicacion para cambiar la pagina actual. @type {Function} */
  export let navigateTo;

  import { API } from '../lib/api.js';

  /** ID del usuario autenticado actualmente, leido del store de sesion. @type {number|null} */
  let usuarioId = null;

  /** Manejador de desuscripcion para la suscripcion al store de sesion. @type {Function} */
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  /** Indica si las reservaciones se estan cargando actualmente desde la API. @type {boolean} */
  let cargando    = true;

  /** Cadena de mensaje de error establecida cuando la solicitud a la API falla. @type {string} */
  let errorMsg    = '';

  /** Arreglo de reservaciones pendientes (estadoReservaId === 1) obtenidas de la API. @type {Array} */
  let reservas    = [];

  /**
   * Hook de ciclo de vida que se ejecuta tras el montaje del componente.
   * Redirige al login si no existe sesion de usuario, de lo contrario obtiene las reservaciones pendientes.
   * Retorna la funcion de desuscripcion del store de sesion para limpieza.
   * @async
   * @returns {Promise<Function>}
   */
  onMount(async () => {
    if (!usuarioId) { navigateTo('login'); return; }
    await cargarReservasPendientes();
    return () => unsubscribe();
  });

  /**
   * Obtiene las reservaciones del usuario autenticado desde la API y filtra para conservar solo
   * las que tienen estadoReservaId === 1 (Pendiente). Establece cargando y errorMsg como efectos secundarios.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarReservasPendientes() {
    cargando = true; errorMsg = '';
    try {
      const res = await fetch(`${API}/api/mis-reservaciones`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error('Error al cargar reservaciones.');
      const todas = await res.json();
      reservas = todas.filter(r => r.estadoReservaId === 1);
    } catch (e) {
      errorMsg = e.message ?? 'Error de conexión.';
    } finally {
      cargando = false;
    }
  }

  /**
   * Formatea una cadena de fecha en una fecha corta localizada usando el locale es-GT.
   * Retorna un guion si el input es falsy.
   * @param {string|null} f - Cadena de fecha ISO a formatear.
   * @returns {string} Fecha formateada como "15 ene. 2025" o "-".
   */
  function formatFecha(f) {
    if (!f) return '—';
    return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  /**
   * Extracts and returns the HH:MM portion of a time string.
   * Returns an em dash if the input is falsy.
   * @param {string|null} h - Time string in HH:MM:SS format.
   * @returns {string} The first five characters (HH:MM) or "—".
   */
  function formatHora(h) { return h ? h.substring(0, 5) : '—'; }

  /**
   * Formats a numeric price into a USD string with two decimal places.
   * @param {number} p - The price value to format.
   * @returns {string} Formatted price string such as "$ 1,250.00".
   */
  function formatPrecio(p) {
    return `$ ${Number(p).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  /**
   * Groups a flat array of ticket objects by their vueloId.
   * Returns an array of objects, each containing the first ticket as the flight reference
   * and a boletos array with all tickets for that flight.
   * @param {Array} boletos - Array of ticket objects each containing a vueloId property.
   * @returns {Array<{vuelo: object, boletos: Array}>} Grouped flight entries.
   */
  function agruparPorVuelo(boletos) {
    const map = {};
    for (const b of boletos) {
      if (!map[b.vueloId]) map[b.vueloId] = { vuelo: b, boletos: [] };
      map[b.vueloId].boletos.push(b);
    }
    return Object.values(map);
  }

  /**
   * Calculates the total price of all tickets within a single reservation.
   * @param {object} reserva - Reservation object containing a boletos array with precio fields.
   * @returns {number} Sum of all ticket prices in the reservation.
   */
  function calcularTotal(reserva) {
    return reserva.boletos.reduce((s, b) => s + b.precio, 0);
  }

  /**
   * Navigates the user to the checkout page to complete payment for pending reservations.
   */
  function goToCheckout() {
    navigateTo('checkout');
  }
</script>

<!-- Contenedor principal del carrito de pasajeros -->
<div class="carrito">
  <div class="carrito__container">

    <!-- Encabezado del carrito con titulo y contador de reservaciones pendientes -->
    <div class="carrito__header">
      <button class="carrito__back" on:click={() => navigateTo('home')}>
        Continuar comprando
      </button>
      <h1 class="carrito__title">Carrito de pasajeros</h1>
      {#if !cargando}
        <p class="carrito__subtitle">{reservas.length} reservación{reservas.length !== 1 ? 'es' : ''} pendiente{reservas.length !== 1 ? 's' : ''}</p>
      {/if}
    </div>

    <!-- Estado de carga, error o carrito vacio -->
    {#if cargando}
      <div style="text-align:center; padding: 3rem; color: #9c8a78;">Cargando reservaciones...</div>

    {:else if errorMsg}
      <div style="text-align:center; padding: 2rem; color:#dc2626;">{errorMsg}</div>

    {:else if reservas.length === 0}
      <div class="carrito__empty">
        <p class="carrito__empty-message">No tienes reservaciones pendientes</p>
        <button class="carrito__empty-btn" on:click={() => navigateTo('home')}>
          Buscar vuelos
        </button>
      </div>

    {:else}
      <!-- Listado de reservaciones pendientes agrupadas por vuelo -->
      <div class="carrito__content">
        <div class="carrito__items">

          {#each reservas as reserva}
            {@const grupos = agruparPorVuelo(reserva.boletos)}

            <div class="cart-item">
              <!-- Header de la reserva -->
              <div class="cart-item__header">
                <div style="display:flex; flex-direction:column; gap:0.2rem;">
                  <span class="cart-item__type-badge">{reserva.noReservacion}</span>
                  <span style="font-size:0.78rem; color:#9c8a78;">Creada {formatFecha(reserva.fechaCreacion)}</span>
                </div>
                <span style="font-weight:700; color:#c9a96e; font-size:1.05rem;">{formatPrecio(reserva.total)}</span>
              </div>

              <!-- Vuelos de la reserva -->
              {#each grupos as grupo}
                {@const b0 = grupo.vuelo}
                <div class="cart-item__content">
                  <div class="cart-item__flight-info">
                    <h3 class="cart-item__flight-number">{b0.numeroVuelo}</h3>
                    <p class="cart-item__route">
                      {b0.origenCodigo} → {b0.destinoCodigo}
                    </p>
                  </div>

                  <div class="cart-item__details">
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Fecha vuelo</span>
                      <span class="cart-item__detail-value">{formatFecha(b0.fechaVuelo)}</span>
                    </div>
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Horario</span>
                      <span class="cart-item__detail-value">{formatHora(b0.horaSalida)} – {formatHora(b0.horaLlegada)}</span>
                    </div>
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Avión</span>
                      <span class="cart-item__detail-value">{b0.avionMarca} {b0.avionModelo}</span>
                    </div>
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Clase</span>
                      <span class="cart-item__detail-value cart-item__detail-value--class">{b0.clase}</span>
                    </div>
                  </div>

                  <!-- Boletos / pasajeros -->
                  <div style="margin-top:0.75rem;">
                    {#each grupo.boletos as boleto}
                      <div style="display:flex; justify-content:space-between; align-items:center; padding:0.4rem 0; border-top:1px solid #f0e8da; font-size:0.85rem;">
                        <div>
                          <span style="font-weight:600; color:#2c1810;">Asiento {boleto.noAsiento}</span>
                          {#if boleto.pasajero}
                            <span style="color:#9c8a78; margin-left:0.5rem;">
                              — {boleto.pasajero.nombre} {boleto.pasajero.apellido}
                            </span>
                          {:else}
                            <span style="color:#f97316; margin-left:0.5rem;">Sin pasajero asignado</span>
                          {/if}
                        </div>
                        <span style="color:#c9a96e; font-weight:700;">{formatPrecio(boleto.precio)}</span>
                      </div>
                    {/each}
                  </div>
                </div>
              {/each}
            </div>
          {/each}

        </div>

        <!-- Sidebar con resumen de costos y boton para proceder al pago -->
        <aside class="carrito__summary">
          <div class="cart-summary">
            <h2 class="cart-summary__title">Resumen</h2>

            <div class="cart-summary__items">
              {#each reservas as reserva}
                <div class="summary-item">
                  <span class="summary-item__description">{reserva.noReservacion}</span>
                  <span class="summary-item__price">{formatPrecio(reserva.total)}</span>
                </div>
              {/each}
            </div>

            <div class="cart-summary__divider"></div>

            <div class="cart-summary__total">
              <span class="cart-summary__total-label">Total</span>
              <span class="cart-summary__total-value">
                {formatPrecio(reservas.reduce((s, r) => s + r.total, 0))}
              </span>
            </div>

            <button class="cart-summary__btn-checkout" on:click={goToCheckout}>
              Proceder al pago
            </button>

            <p class="cart-summary__note">
              Completa el pago antes de que expiren tus reservaciones
            </p>
          </div>
        </aside>
      </div>
    {/if}

  </div>
</div>
