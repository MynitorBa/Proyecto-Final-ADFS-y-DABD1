<script>
  // @ts-nocheck
/**
 * @file Confirmacion.svelte
 * @description Post-payment confirmation page shown after a successful checkout. Receives the
 * list of paid reservations and their corresponding invoice objects as props from the checkout
 * page. Displays a success hero section followed by invoice cards that include billing details,
 * per-ticket breakdown, and action buttons to download or email the PDF receipt for each
 * reservation. Also handles the fallback case where facturas is empty by rendering basic
 * reservation summary cards instead. Provides navigation actions to search for more flights
 * or view the user's reservations. Redirects unauthenticated users to the login page on mount.
 */

  import '../styles/confirmacion.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  /** Navigation function provided by the app router to switch pages. @type {Function} */
  export let navigateTo;

  /** Array of reservation objects passed from the checkout page after successful payment. @type {Array} */
  export let reservaciones = [];

  /** Array of invoice objects returned by the API after payment, one per reservation. @type {Array} */
  export let facturas      = [];

  import { API } from '../lib/api.js';

  /** ID of the currently authenticated user, read from the session store. @type {number|null} */
  let usuarioId = null;

  /** Unsubscribe handle for the session store subscription. @type {Function} */
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  /**
   * Lifecycle hook that runs after the component mounts.
   * Redirects to login if no user session exists.
   * Returns the unsubscribe function for session store cleanup.
   * @returns {Function}
   */
  onMount(() => {
    if (!usuarioId) { navigateTo('login'); return; }
    return () => unsubscribe();
  });

  /** Array of active toast notification objects, each containing id, msg, and tipo. @type {Array} */
  let toasts = [];

  /**
   * Adds a toast notification to the stack and auto-removes it after 4 seconds.
   * @param {string} msg - The message text to display in the toast.
   * @param {string} [tipo='success'] - Visual style: 'success' or 'error'.
   */
  function addToast(msg, tipo = 'success') {
    const id = Date.now();
    toasts = [...toasts, { id, msg, tipo }];
    setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 4000);
  }

  /**
   * Formats a date/time string into a localized long-form date with time using the es-GT locale.
   * Returns an em dash if the input is falsy.
   * @param {string|null} f - ISO date-time string to format.
   * @returns {string} Formatted string such as "15 de enero de 2025, 10:30" or "—".
   */
  function formatFecha(f) {
    if (!f) return '—';
    return new Date(f).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  /**
   * Formats a numeric price into a USD string with two decimal places.
   * @param {number} p - The price value to format.
   * @returns {string} Formatted price string such as "$ 1,250.00".
   */
  function formatPrecio(p) {
    return `$ ${Number(p).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  /** Map of reservacionId to boolean tracking which receipts are currently being downloaded. @type {object} */
  let descargando = {};

  /** Map of reservacionId to boolean tracking which receipts are currently being emailed. @type {object} */
  let enviando = {};

  /**
   * Downloads the PDF receipt for a specific reservation by calling the comprobante endpoint.
   * Guards against concurrent calls using the descargando map. Creates a temporary anchor element
   * to trigger a browser file download, then revokes the object URL and shows a success or
   * error toast depending on the outcome.
   * @async
   * @param {number} reservacionId - The ID of the reservation whose receipt to download.
   * @param {string} noReservacion - The human-readable reservation number used as the filename.
   * @returns {Promise<void>}
   */
  async function descargarComprobante(reservacionId, noReservacion) {
    if (descargando[reservacionId]) return;
    descargando[reservacionId] = true;
    descargando = { ...descargando };

    try {
      window.open(`${API}/api/mis-reservaciones/${reservacionId}/comprobante`, '_blank');
      addToast('Comprobante abierto en nueva pestana');
    } catch {
      addToast('No se pudo abrir el comprobante', 'error');
    } finally {
      descargando[reservacionId] = false;
      descargando = { ...descargando };
    }
  }

  /**
   * Sends the PDF receipt for a specific reservation to the user's registered email by calling
   * the enviar-comprobante endpoint. Guards against concurrent calls using the enviando map.
   * Shows a success toast on 200 OK, or an error toast with the API message on failure.
   * @async
   * @param {number} reservacionId - The ID of the reservation whose receipt to email.
   * @returns {Promise<void>}
   */
  async function enviarComprobantePorCorreo(reservacionId) {
    if (enviando[reservacionId]) return;
    enviando[reservacionId] = true;
    enviando = { ...enviando };

    try {
      const res = await fetch(`${API}/api/mis-reservaciones/${reservacionId}/enviar-comprobante`, {
        method: 'POST',
        credentials: 'include'
      });
      if (res.ok) {
        addToast('Comprobante enviado a tu correo');
      } else {
        const body = await res.json().catch(() => ({}));
        addToast(body.message || 'No se pudo enviar el comprobante', 'error');
      }
    } catch {
      addToast('Error de conexion', 'error');
    } finally {
      enviando[reservacionId] = false;
      enviando = { ...enviando };
    }
  }

  /**
   * Finds a reservation in the reservaciones prop array by its ID and returns its boletos array.
   * Returns an empty array if no matching reservation is found.
   * @param {number} reservacionId - The ID of the reservation to look up.
   * @returns {Array} The boletos array of the matching reservation, or an empty array.
   */
  function getBoletos(reservacionId) {
    const reserva = reservaciones.find(r => r.reservacionId === reservacionId);
    return reserva?.boletos ?? [];
  }
</script>

<!-- Pila de notificaciones toast para confirmar descarga o envio de comprobante -->
<div class="conf-toast-container">
  {#each toasts as t (t.id)}
    <div class="conf-toast conf-toast--{t.tipo}">
      {#if t.tipo === 'success'}
        <svg class="conf-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
      {:else}
        <svg class="conf-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      {/if}
      <span>{t.msg}</span>
    </div>
  {/each}
</div>

<div class="confirmacion">
  <div class="confirmacion__container">

    <!-- Hero de exito con icono de confirmacion y mensaje de compra realizada -->
    <div class="confirmacion__hero">
      <div class="confirmacion__icono-wrap">
        <div class="confirmacion__icono">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="40" height="40">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
      </div>
      <h1 class="confirmacion__titulo">Compra realizada con exito</h1>
      <p class="confirmacion__subtitulo">
        Tu reservacion ha sido confirmada. Descarga tu comprobante a continuacion
        o consultalo en cualquier momento desde tus reservaciones.
      </p>
      <div class="confirmacion__linea-deco"></div>
    </div>

    <!-- Tarjetas de factura con detalle de boletos y acciones de descarga o envio por correo -->
    {#if facturas.length > 0}
      <div class="confirmacion__facturas">
        {#each facturas as factura}
          {@const boletos = getBoletos(factura.reservacionId)}
          <div class="factura-card">

            <div class="factura-card__header">
              <div class="factura-card__header-left">
                <span class="factura-card__etiqueta">Comprobante de pago</span>
                <span class="factura-card__nro">{factura.noReservacion}</span>
              </div>
              <div class="factura-card__header-right">
                <span class="factura-card__estado">Confirmada</span>
              </div>
            </div>

            <div class="factura-card__body">
              <div class="factura-grid">
                <div class="factura-dato">
                  <span class="factura-dato__label">Factura ID</span>
                  <span class="factura-dato__valor">FAC-{String(factura.facturaId).padStart(6,'0')}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">No. Reservacion</span>
                  <span class="factura-dato__valor">{factura.noReservacion}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Fecha de pago</span>
                  <span class="factura-dato__valor">{formatFecha(factura.fecha)}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">NIT</span>
                  <span class="factura-dato__valor">{factura.nit ?? 'CF'}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Codigo postal</span>
                  <span class="factura-dato__valor">{factura.codigoPostal ?? '—'}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Total pagado</span>
                  <span class="factura-dato__valor factura-dato__valor--gold">{formatPrecio(factura.total)}</span>
                </div>
              </div>

              {#if boletos.length > 0}
                <div class="factura-card__separador"></div>
                <h4 class="factura-card__subtitulo">Detalle de boletos</h4>
                <div class="factura-boletos">
                  {#each boletos as boleto}
                    <div class="factura-boleto">
                      <div class="factura-boleto__col">
                        <span class="factura-boleto__asiento">Asiento {boleto.noAsiento}</span>
                        {#if boleto.pasajero}
                          <span class="factura-boleto__pasajero">
                            {boleto.pasajero.nombre} {boleto.pasajero.apellido}
                          </span>
                        {/if}
                        <span class="factura-boleto__vuelo">
                          Vuelo {boleto.numeroVuelo} &middot; {boleto.clase} &middot; {boleto.origenCodigo} &rarr; {boleto.destinoCodigo}
                        </span>
                      </div>
                      <span class="factura-boleto__precio">{formatPrecio(boleto.precio)}</span>
                    </div>
                  {/each}
                </div>
              {/if}

              <div class="factura-card__footer">
                <button class="btn-pdf"
                  class:btn-pdf--loading={descargando[factura.reservacionId]}
                  disabled={descargando[factura.reservacionId]}
                  on:click={() => descargarComprobante(factura.reservacionId, factura.noReservacion)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  {descargando[factura.reservacionId] ? "Descargando..." : "Descargar PDF"}
                </button>
                <button class="btn-correo"
                  class:btn-correo--loading={enviando[factura.reservacionId]}
                  disabled={enviando[factura.reservacionId]}
                  on:click={() => enviarComprobantePorCorreo(factura.reservacionId)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  {enviando[factura.reservacionId] ? "Enviando..." : "Enviar al correo"}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>

    {:else}
      <div class="confirmacion__facturas">
        {#each reservaciones as reserva}
          <div class="factura-card">
            <div class="factura-card__header">
              <div class="factura-card__header-left">
                <span class="factura-card__etiqueta">Reservacion confirmada</span>
                <span class="factura-card__nro">{reserva.noReservacion}</span>
              </div>
              <span class="factura-card__estado">Confirmada</span>
            </div>
            <div class="factura-card__body">
              <div class="factura-grid">
                <div class="factura-dato">
                  <span class="factura-dato__label">No. Reservacion</span>
                  <span class="factura-dato__valor">{reserva.noReservacion}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Total</span>
                  <span class="factura-dato__valor factura-dato__valor--gold">{formatPrecio(reserva.total)}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Boletos</span>
                  <span class="factura-dato__valor">{reserva.boletos?.length ?? 0}</span>
                </div>
              </div>
              <div class="factura-card__footer">
                <button class="btn-pdf"
                  class:btn-pdf--loading={descargando[reserva.reservacionId]}
                  disabled={descargando[reserva.reservacionId]}
                  on:click={() => descargarComprobante(reserva.reservacionId, reserva.noReservacion)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  {descargando[reserva.reservacionId] ? "Descargando..." : "Descargar PDF"}
                </button>
                <button class="btn-correo"
                  class:btn-correo--loading={enviando[reserva.reservacionId]}
                  disabled={enviando[reserva.reservacionId]}
                  on:click={() => enviarComprobantePorCorreo(reserva.reservacionId)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  {enviando[reserva.reservacionId] ? "Enviando..." : "Enviar al correo"}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}

    <!-- Botones de navegacion para buscar mas vuelos o ver reservaciones -->
    <div class="confirmacion__acciones">
      <h2 class="confirmacion__acciones-titulo">Que deseas hacer ahora?</h2>
      <div class="confirmacion__btns">
        <button class="btn-primary" on:click={() => navigateTo('home')}>
          Buscar mas vuelos
        </button>
        <button class="btn-secondary" on:click={() => navigateTo('reservas')}>
          Ver mis reservaciones
        </button>
      </div>
    </div>

  </div>
</div>
