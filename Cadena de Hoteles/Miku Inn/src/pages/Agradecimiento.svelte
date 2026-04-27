<script>
  /**
   * @file Agradecimiento.svelte
   * @description Pagina de confirmacion de pago. Muestra el detalle de las
   * facturas generadas tras completar una reservacion, permite descargar
   * cada factura en PDF y presenta el total general si hay mas de una.
   * Tambien muestra recomendaciones de aerolineas aliadas activas.
   */

  // @ts-nocheck
  import '../styles/Agradecimiento.css';
  import { onMount } from 'svelte';

  /** Funcion de navegacion recibida desde App. @type {Function} */
  export let navigateTo;

  /**
   * Datos de la confirmacion de pago, incluyendo el array de facturas.
   * Se pasa desde App al navegar a esta pagina.
   * @type {object|null}
   */
  export let agradecimientoData = null;

  /** URL base del backend. @type {string} */
  import { API } from '../lib/api.js';

  /** ID de la reservacion cuya factura PDF se esta descargando en este momento. @type {number|null} */
  let downloadingId = null;

  /** Mensaje de error que se muestra si falla la descarga del PDF. @type {string} */
  let downloadError = '';

  /** Lista de aerolineas aliadas activas con su urlHome. @type {Array<{id:number, nombre:string, urlHome:string}>} */
  let aerolineas = [];

  /**
   * Carga las aerolineas aliadas activas para mostrar como recomendaciones.
   * @async
   */
  async function cargarAerolineas() {
    try {
      const res = await fetch(`${API}/aerolineas/home`, { credentials: 'include' });
      if (res.ok) aerolineas = await res.json();
    } catch (_) {
      // Si falla, simplemente no se muestran recomendaciones
    }
  }

  onMount(() => { cargarAerolineas(); });

  /**
   * Solicita al backend el PDF de la factura y lo descarga en el navegador.
   * @async
   * @param {number|null} reservacionId
   * @returns {Promise<void>}
   */
  async function downloadFactura(reservacionId) {
    if (!reservacionId) { downloadError = 'ID de reservación no disponible.'; return; }
    downloadingId = reservacionId;
    downloadError = '';
    try {
      const res = await fetch(`${API}/reservaciones/${reservacionId}/pdf`, {
        credentials: 'include',
      });
      if (!res.ok) {
        let msg = `Error ${res.status}`;
        try { const d = await res.json(); msg = d.mensaje || d.message || msg; } catch(_) {}
        throw new Error(msg);
      }
      const blob = await res.blob();
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `factura-${reservacionId}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch(e) {
      downloadError = e.message || 'Error al descargar la factura';
    } finally {
      downloadingId = null;
    }
  }

  /**
   * Formatea un numero como moneda USD con dos decimales.
   * @param {number} p
   * @returns {string}
   */
  const fmt = (p) =>
    new Intl.NumberFormat('es-GT', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(p || 0);

  /**
   * Formatea una cadena de fecha ISO en formato legible en espanol.
   * @param {string} str
   * @returns {string}
   */
  function fmtDate(str) {
    if (!str) return '—';
    const d = new Date(str);
    if (isNaN(d)) return str;
    return d.toLocaleDateString('es-GT', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  $: facturas   = agradecimientoData?.facturas ?? [];
  $: grandTotal = facturas.reduce((s, f) => s + (f.total || 0), 0);
</script>

<div class="agradecimiento">
  <div class="agradecimiento__inner">

    <!-- Banner principal de confirmacion exitosa -->
    <div class="ag-hero">
      <div class="ag-hero__content">
        <div class="ag-check">
          <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.8"
               stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <p class="ag-eyebrow">Confirmación de pago</p>
        <h1 class="ag-title">¡Gracias por tu preferencia!</h1>
        <p class="ag-subtitle">
          Tu pago fue procesado exitosamente. A continuación encontrarás el detalle
          de tu{facturas.length !== 1 ? 's' : ''} factura{facturas.length !== 1 ? 's' : ''}.
        </p>
      </div>
    </div>

    <!-- Listado de facturas generadas por la compra -->
    {#if facturas.length === 0}
      <p style="text-align:center; color: var(--ag-muted); padding: 2rem 0;">
        No hay información de pago disponible.
      </p>
    {:else}
      <div class="ag-facturas">
        {#each facturas as f, i}
          {@const reservacionId = f._reservacion?.id ?? f.reservacionId ?? null}
          <div class="ag-factura" style="animation-delay: {i * 0.08}s">

            <div class="ag-factura__head">
              <span class="ag-factura__hotel">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:4px"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                {f._reservacion?.nombreHotel ?? 'Hotel'}
              </span>
              <span class="ag-badge">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" style="vertical-align:-1px;margin-right:2px"><polyline points="20 6 9 17 4 12"/></svg>
                {f.estado ?? 'Pagada'}
              </span>
            </div>

            <div class="ag-factura__body">
              <div class="ag-field">
                <span class="ag-field__label">Factura #</span>
                <span class="ag-field__value">{f.facturaId ?? '—'}</span>
              </div>
              <div class="ag-field">
                <span class="ag-field__label">Fecha</span>
                <span class="ag-field__value">{fmtDate(f.fecha)}</span>
              </div>
              <div class="ag-field">
                <span class="ag-field__label">NIT</span>
                <span class="ag-field__value">{f.nit ?? '—'}</span>
              </div>
              <div class="ag-field">
                <span class="ag-field__label">Cód. Postal</span>
                <span class="ag-field__value">{f.codigoPostal ?? '—'}</span>
              </div>

              {#if f._reservacion?.habitaciones?.length}
                <div class="ag-field ag-field--full">
                  <span class="ag-field__label">Habitaciones</span>
                  <span class="ag-field__value">
                    {f._reservacion.habitaciones
                      .map(h => h.tipoHabitacion + (h.tipoCama ? ' · ' + h.tipoCama : ''))
                      .join(' — ')}
                  </span>
                </div>
              {/if}

              {#if f._reservacion?.fechaCheckIn || f._reservacion?._checkIn}
                <div class="ag-field">
                  <span class="ag-field__label">Check-in</span>
                  <span class="ag-field__value">
                    {fmtDate(f._reservacion._checkIn ?? f._reservacion.fechaCheckIn)}
                  </span>
                </div>
                <div class="ag-field">
                  <span class="ag-field__label">Check-out</span>
                  <span class="ag-field__value">
                    {fmtDate(f._reservacion._checkOut ?? f._reservacion.fechaCheckOut)}
                  </span>
                </div>
              {/if}

              <div class="ag-field ag-field--total">
                <span class="ag-field__label">Total pagado</span>
                <span class="ag-field__value">{fmt(f.total)}</span>
              </div>
            </div>

            <div class="ag-factura__code">
              <span class="ag-factura__code-label">Reservación</span>
              <span class="ag-factura__code-val">{f.noReservacion ?? '—'}</span>
            </div>

            <div class="ag-factura__download">
              <button
                class="ag-download-btn"
                on:click={() => downloadFactura(reservacionId)}
                disabled={downloadingId === reservacionId}
              >
                {#if downloadingId === reservacionId}
                  <span class="ag-spinner"></span>
                  Descargando...
                {:else}
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  Descargar Factura PDF
                {/if}
              </button>
              {#if downloadError && downloadingId === null}
                <p class="ag-download-error">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  {downloadError}
                </p>
              {/if}
            </div>

          </div>
        {/each}
      </div>

      {#if facturas.length > 1}
        <div class="ag-total-bar">
          <span class="ag-total-bar__label">
            {facturas.length} reservaciones · Total general
          </span>
          <span class="ag-total-bar__amount">{fmt(grandTotal)}</span>
        </div>
      {/if}
    {/if}

    <!-- Seccion de recomendaciones de aerolineas aliadas -->
    {#if aerolineas.length > 0}
      <div class="ag-recomendaciones">
        <div class="ag-recomendaciones__header">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
          </svg>
          <span>¿Necesitas vuelos para tu viaje?</span>
        </div>
        <p class="ag-recomendaciones__sub">
          Nuestras aerolineas aliadas te ofrecen las mejores tarifas.
        </p>
        <div class="ag-aerolineas">
          {#each aerolineas as ae}
            <a
              class="ag-aerolinea-card"
              href={ae.urlHome}
              target="_blank"
              rel="noopener noreferrer"
            >
              <div class="ag-aerolinea-card__icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                </svg>
              </div>
              <span class="ag-aerolinea-card__nombre">{ae.nombre}</span>
              <svg class="ag-aerolinea-card__arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </a>
          {/each}
        </div>
      </div>
    {/if}

    <!-- Acciones finales -->
    <div class="ag-actions">
      <button class="ag-btn ag-btn--primary" on:click={() => navigateTo('home')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        Volver al inicio
      </button>
      <button class="ag-btn ag-btn--outline" on:click={() => navigateTo('reservations')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="9" y1="7" x2="15" y2="7"/><line x1="9" y1="11" x2="15" y2="11"/><line x1="9" y1="15" x2="12" y2="15"/></svg>
        Mis reservaciones
      </button>
    </div>

  </div>
</div>