<script>
  // @ts-nocheck
  import '../styles/Agradecimiento.css';

  export let navigateTo;
  export let agradecimientoData = null;

  const API = 'http://localhost:7000';

  // ── Descarga de factura ───────────────────────────────────
  let downloadingId = null;
  let downloadError = '';

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

  const fmt = (p) =>
    new Intl.NumberFormat('es-GT', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(p || 0);

  function fmtDate(str) {
    if (!str) return '—';
    const d = new Date(str);
    if (isNaN(d)) return str;
    return d.toLocaleDateString('es-GT', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  $: facturas = agradecimientoData?.facturas ?? [];
  $: grandTotal = facturas.reduce((s, f) => s + (f.total || 0), 0);
</script>

<div class="agradecimiento">
  <div class="agradecimiento__inner">

    <!-- ── Hero banner ── -->
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

    <!-- ── Facturas ── -->
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
                🏨 {f._reservacion?.nombreHotel ?? 'Hotel'}
              </span>
              <span class="ag-badge">✓ {f.estado ?? 'Pagada'}</span>
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

            <!-- ── Botón descargar factura ── -->
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

      <!-- Grand total si hay más de 1 -->
      {#if facturas.length > 1}
        <div class="ag-total-bar">
          <span class="ag-total-bar__label">
            {facturas.length} reservaciones · Total general
          </span>
          <span class="ag-total-bar__amount">{fmt(grandTotal)}</span>
        </div>
      {/if}
    {/if}

    <!-- ── Acciones ── -->
    <div class="ag-actions">
      <button class="ag-btn ag-btn--primary" on:click={() => navigateTo('home')}>
        🏠 Volver al inicio
      </button>
      <button class="ag-btn ag-btn--outline" on:click={() => navigateTo('reservations')}>
        📋 Mis reservaciones
      </button>
    </div>

  </div>
</div>