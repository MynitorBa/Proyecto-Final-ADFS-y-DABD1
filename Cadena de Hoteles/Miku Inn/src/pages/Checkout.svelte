<script>
  import '../styles/checkout.css';

  export let navigateTo;
  export let checkoutData = null;

  const API = 'http://localhost:7000';

  // ── Estado general ────────────────────────────────────────
  let loading       = true;
  let loadError     = '';
  let reservations  = [];

  // ── Estado de pago ────────────────────────────────────────
  let submitting = false;
  let payError   = '';

  // ── Datos del formulario de pago ──────────────────────────
  let form = {
    nit:             '',
    codigoPostal:    '',
    numeroTarjeta:   '',
    nombreTitular:   '',
    fechaVencimiento:'',
    cvv:             '',
  };

  // ── Helpers ───────────────────────────────────────────────
  const fmt = p => new Intl.NumberFormat('es-GT', {
    style: 'currency', currency: 'USD', minimumFractionDigits: 2
  }).format(p);

  function fmtDate(str) {
    if (!str) return '—';
    return str.toString().split(' ')[0];
  }

  function formatCardNumber(e) {
    let v = /** @type {HTMLInputElement} */ (e.target).value.replace(/\D/g,'').slice(0,16);
    form.numeroTarjeta = v.replace(/(.{4})/g,'$1 ').trim();
  }

  function formatExpiry(e) {
    let v = /** @type {HTMLInputElement} */ (e.target).value.replace(/\D/g,'').slice(0,4);
    if (v.length > 2) v = v.slice(0,2)+'/'+v.slice(2);
    form.fechaVencimiento = v;
  }

  // ── Agrupa filas del API por id de reservación ───────────
  function groupRows(rows) {
    const map = new Map();
    for (const row of rows) {
      if (!map.has(row.id)) {
        map.set(row.id, {
          id:              row.id,
          noReservacion:   row.noReservacion,
          total:           row.total,
          estado:          row.estado,
          fechaCreacion:   row.fechaCreacion,
          fechaExpiracion: row.fechaExpiracion,
          nombreHotel:     row.nombreHotel,
          habitaciones:    [],
          fechaCheckIn:    row.fechaCheckIn,
          fechaCheckOut:   row.fechaCheckOut,
        });
      }
      map.get(row.id).habitaciones.push({
        detalleId:        row.detalleId,
        tipoHabitacion:   row.tipoHabitacion,
        tipoCama:         row.tipoCama,
        cantidadPersonas: row.cantidadPersonas,
        totalDetalle:     row.totalDetalle,
        fechaCheckIn:     row.fechaCheckIn,
        fechaCheckOut:    row.fechaCheckOut,
      });
    }
    return Array.from(map.values());
  }

  // ── Carga reservaciones pendientes ────────────────────────
  async function loadPending() {
    loading = true; loadError = '';
    try {
      if (checkoutData?.pendingReservations?.length > 0) {
        reservations = checkoutData.pendingReservations;
        loading = false;
        return;
      }
      const res = await fetch(`${API}/reservaciones`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const raw = await res.json();
      const grouped = groupRows(raw);
      reservations = grouped.filter(r => r.estado?.toLowerCase() === 'pendiente');
    } catch(e) {
      loadError = e.message || 'Error al cargar reservaciones';
    } finally {
      loading = false;
    }
  }

  loadPending();

  $: totalSelected = reservations.reduce((s, r) => s + (r.total || 0), 0);
  $: anySelected   = reservations.length > 0;

  // ── Validación formulario ─────────────────────────────────
  function validate() {
    const card = form.numeroTarjeta.replace(/\s/g,'');
    if (!form.nit.trim())             return 'Ingresa tu NIT';
    if (!form.codigoPostal.trim())    return 'Ingresa el código postal';
    if (card.length < 16)             return 'Número de tarjeta inválido (16 dígitos)';
    if (!form.nombreTitular.trim())   return 'Ingresa el nombre del titular';
    if (!form.fechaVencimiento.trim())return 'Ingresa la fecha de vencimiento (MM/AA)';
    if (!form.cvv.trim())             return 'Ingresa el CVV';
    return null;
  }

  // ── Procesar pagos ────────────────────────────────────────
  async function handlePay() {
    payError = '';
    const err = validate();
    if (err) { payError = err; return; }

    submitting = true;
    const payload = {
      nit:              form.nit.trim(),
      codigoPostal:     form.codigoPostal.trim(),
      numeroTarjeta:    form.numeroTarjeta.replace(/\s/g,''),
      nombreTitular:    form.nombreTitular.trim(),
      fechaVencimiento: form.fechaVencimiento.trim(),
      cvv:              form.cvv.trim(),
    };

    const facturas = [];
    const errors   = [];

    for (const r of reservations) {
      try {
        const res = await fetch(`${API}/reservaciones/${r.id}/pago`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(payload),
        });
        const data = await res.json();
        if (!res.ok) {
          errors.push(`${r.noReservacion}: ${data.mensaje || data.message || `Error ${res.status}`}`);
        } else {
          facturas.push({ ...data, _reservacion: r });

          // Enviar correo con boleta de pago al usuario (fire & forget)
          fetch(`${API}/reservaciones/${r.id}/correo`, { credentials: 'include' }).catch(() => {});
        }
      } catch(e) {
        errors.push(`${r.noReservacion}: ${e.message}`);
      }
    }

    submitting = false;

    if (errors.length > 0) {
      payError = errors.join(' | ');
    }

    if (facturas.length > 0) {
      navigateTo('agradecimiento', { facturas });
    }
  }
</script>

<div class="checkout">
  <div class="checkout__container">

    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('home')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver al inicio
      </button>
      <h1 class="checkout__title">Pagar reservaciones</h1>
    </div>

    {#if loading}
      <div class="checkout-loading">
        <div class="checkout-loading__spinner"></div>
        <p>Cargando reservaciones pendientes...</p>
      </div>

    {:else if loadError}
      <div class="checkout-empty">
        <div class="checkout-empty__icon">⚠️</div>
        <h2>Error al cargar</h2>
        <p>{loadError}</p>
        <button class="confirm-btn" on:click={loadPending}>Reintentar</button>
      </div>

    {:else if reservations.length === 0}
      <div class="checkout-empty">
        <div class="checkout-empty__icon">🧾</div>
        <h2>No hay reservaciones pendientes</h2>
        <p>Realiza una reservación primero para poder pagar.</p>
        <button class="confirm-btn" on:click={() => navigateTo('home')}>Buscar hoteles</button>
      </div>

    {:else}
      <div class="checkout__content">

        <!-- ── Columna izquierda: lista + formulario ── -->
        <div class="checkout__main">

          <!-- Lista de reservaciones pendientes -->
          <section class="checkout-section">
            <h2 class="checkout-section__title">Reservaciones pendientes de pago</h2>
            <p class="checkout-section__sub">Reservaciones a pagar en este momento</p>

            <div class="reservations-list">
              {#each reservations as r}
                <div class="reservation-item checked">
                  <div class="reservation-item__body">
                    <div class="reservation-item__top">
                      <span class="reservation-item__code">{r.noReservacion}</span>
                      <span class="reservation-item__total">{fmt(r.total)}</span>
                    </div>
                    <div class="reservation-item__meta">
                      {#if r.nombreHotel}
                        <span>🏨 {r.nombreHotel}</span>
                      {/if}
                      {#if r.habitaciones?.length}
                        <span>🛏 {r.habitaciones.map(h => h.tipoHabitacion).join(' · ')}</span>
                      {/if}
                      {#if r._checkIn}
                        <span>📅 {r._checkIn} → {r._checkOut}</span>
                        <span>👥 {r._guests} huésped{r._guests !== 1 ? 'es' : ''} · {r._nights} noche{r._nights !== 1 ? 's' : ''}</span>
                      {:else}
                        <span>📅 {fmtDate(r.fechaCheckIn)} → {fmtDate(r.fechaCheckOut)}</span>
                      {/if}
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          </section>

          <!-- Formulario de pago -->
          <section class="checkout-section">
            <h2 class="checkout-section__title">Datos de pago</h2>

            <div class="card-form">
              <div class="form-field">
                <label for="nit" class="form-field__label">NIT</label>
                <input type="text" id="nit" class="form-field__input" bind:value={form.nit} placeholder="12345678" />
              </div>
              <div class="form-field">
                <label for="cp" class="form-field__label">Código Postal</label>
                <input type="text" id="cp" class="form-field__input" bind:value={form.codigoPostal} placeholder="01001" />
              </div>
              <div class="form-field form-field--full">
                <label for="cardNum" class="form-field__label">Número de tarjeta</label>
                <input type="text" id="cardNum" class="form-field__input"
                  value={form.numeroTarjeta} on:input={formatCardNumber}
                  placeholder="4111 1111 1111 1111" maxlength="19" />
              </div>
              <div class="form-field form-field--full">
                <label for="cardHolder" class="form-field__label">Titular de la tarjeta</label>
                <input type="text" id="cardHolder" class="form-field__input"
                  bind:value={form.nombreTitular} placeholder="Nombre como aparece en la tarjeta" />
              </div>
              <div class="form-field">
                <label for="cardExp" class="form-field__label">Vencimiento</label>
                <input type="text" id="cardExp" class="form-field__input"
                  value={form.fechaVencimiento} on:input={formatExpiry}
                  placeholder="MM/AA" maxlength="5" />
              </div>
              <div class="form-field">
                <label for="cardCvv" class="form-field__label">CVV</label>
                <input type="password" id="cardCvv" class="form-field__input"
                  bind:value={form.cvv} placeholder="•••" maxlength="4" />
              </div>
            </div>

            {#if payError}
              <div class="checkout-error">{payError}</div>
            {/if}
          </section>

        </div>

        <!-- ── Sidebar resumen ── -->
        <aside class="checkout__sidebar">
          <div class="order-summary">
            <h2 class="order-summary__title">Resumen</h2>

            {#if reservations.length === 0}
              <p class="order-empty-note">Cargando reservaciones...</p>
            {:else}
              <div class="order-rows">
                {#each reservations as r}
                  <div class="order-row">
                    <span class="order-row__code">{r.noReservacion}</span>
                    <strong>{fmt(r.total)}</strong>
                  </div>
                {/each}
              </div>

              <div class="order-summary__divider"></div>

              <div class="order-summary__total">
                <span class="order-summary__total-label">{reservations.length} reservación{reservations.length !== 1 ? 'es' : ''}</span>
                <span class="order-summary__total-value">{fmt(totalSelected)}</span>
              </div>
            {/if}

            <button class="order-summary__btn-pay"
              on:click={handlePay}
              disabled={submitting || !anySelected}>
              {#if submitting}
                <span class="btn-spinner"></span>
                Procesando...
              {:else}
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
                Pagar {reservations.length > 0 ? fmt(totalSelected) : ''}
              {/if}
            </button>

            <div class="order-summary__security">
              <p class="security-badge">✓ Pago 100% seguro</p>
              <p class="security-note">Tus datos están protegidos con encriptación SSL</p>
            </div>
          </div>
        </aside>

      </div>
    {/if}

  </div>
</div>