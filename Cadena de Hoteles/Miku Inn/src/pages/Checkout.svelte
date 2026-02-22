<script>
  import '../styles/checkout.css';

  export let navigateTo;
  /** @type {{ hotel: any, room: any, checkInDate: string, checkOutDate: string, nights: number, cantidadPersonas: number, totalPrice: number } | null} */
  export let checkoutData = null;

  const API = 'http://localhost:7000';

  // ── Datos de la reserva ───────────────────────────────────
  const hotel           = checkoutData?.hotel           ?? null;
  const room            = checkoutData?.room            ?? null;
  const checkInDate     = checkoutData?.checkInDate     ?? '';
  const checkOutDate    = checkoutData?.checkOutDate    ?? '';
  const nights          = checkoutData?.nights          ?? 0;
  const cantidadPersonas = checkoutData?.cantidadPersonas ?? 1;
  const totalPrice      = checkoutData?.totalPrice      ?? 0;

  // ── Estado ────────────────────────────────────────────────
  let submitting = false;
  let error      = '';
  let reservacion = null;   // respuesta del backend

  let paymentMethod = 'tarjeta';

  // Datos de facturación (nombre del titular + dirección)
  let factura = {
    nombreTitular: '',
    direccion:     '',
    ciudad:        '',
    pais:          '',
    codigoPostal:  '',
    email:         '',
  };

  // Datos de tarjeta
  let cardInfo = {
    numero:     '',
    titular:    '',
    expiracion: '',
    cvv:        '',
  };

  // ── Helpers ───────────────────────────────────────────────
  const fmt = p => new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD', minimumFractionDigits: 2 }).format(p);
  const formatDate = d => d ? new Date(d + 'T12:00:00').toLocaleDateString('es-GT', { year: 'numeric', month: 'long', day: 'numeric' }) : '';

  // ── Validación básica ─────────────────────────────────────
  function validate() {
    if (paymentMethod === 'tarjeta') {
      if (!cardInfo.numero.replace(/\s/g,'') || cardInfo.numero.replace(/\s/g,'').length < 16) return 'Ingresa un número de tarjeta válido';
      if (!cardInfo.titular.trim()) return 'Ingresa el titular de la tarjeta';
      if (!cardInfo.expiracion.trim()) return 'Ingresa la fecha de expiración';
      if (!cardInfo.cvv.trim()) return 'Ingresa el CVV';
    }
    if (!factura.nombreTitular.trim()) return 'Ingresa el nombre para facturación';
    if (!factura.email.trim()) return 'Ingresa el email de facturación';
    return null;
  }

  // ── Formateo de número de tarjeta ─────────────────────────
  function formatCardNumber(e) {
    let v = e.target.value.replace(/\D/g, '').slice(0, 16);
    cardInfo.numero = v.replace(/(.{4})/g, '$1 ').trim();
  }

  function formatExpiry(e) {
    let v = e.target.value.replace(/\D/g, '').slice(0, 4);
    if (v.length > 2) v = v.slice(0, 2) + '/' + v.slice(2);
    cardInfo.expiracion = v;
  }

  // ── POST reservación ──────────────────────────────────────
  async function handlePayment() {
    error = '';
    const validationError = validate();
    if (validationError) { error = validationError; return; }
    if (!room) { error = 'No hay habitación seleccionada'; return; }

    submitting = true;
    try {
      const body = {
        habitaciones: [
          {
            habitacionId:    room.id,
            cantidadPersonas: cantidadPersonas,
            fechaCheckIn:    checkInDate,
            fechaCheckOut:   checkOutDate,
          }
        ]
      };

      const res = await fetch(`${API}/reservaciones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || `Error ${res.status}`);
      }

      reservacion = await res.json();
    } catch (e) {
      error = e.message || 'Error al procesar la reservación';
    } finally {
      submitting = false;
    }
  }

  const PAYMENT_METHODS = [
    ['tarjeta',       'Tarjeta de crédito / débito'],
    ['paypal',        'PayPal'],
    ['transferencia', 'Transferencia bancaria'],
  ];

  const FACTURA_FIELDS = [
    ['nombreTitular', 'Nombre para facturación', 'Nombre completo', 'text'],
    ['email',         'Email de facturación',    'correo@ejemplo.com', 'email'],
    ['pais',          'País',                    'Guatemala',          'text'],
    ['ciudad',        'Ciudad',                  'Ciudad de Guatemala','text'],
    ['direccion',     'Dirección',               'Calle, número, zona','text'],
    ['codigoPostal',  'Código Postal',           '01001',              'text'],
  ];
</script>

<div class="checkout">
  <div class="checkout__container">

    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('home')}>← Volver</button>
      <h1 class="checkout__title">Finalizar reservación</h1>
    </div>

    <!-- ── Confirmación ────────────────────────────────────── -->
    {#if reservacion}
      <div class="checkout-confirm">
        <div class="confirm-icon">✓</div>
        <h2 class="confirm-title">¡Reservación confirmada!</h2>
        <p class="confirm-code">{reservacion.noReservacion}</p>
        <div class="confirm-grid">
          <div class="confirm-item">
            <span class="confirm-lbl">Hotel</span>
            <strong>{hotel?.nombre ?? '—'}</strong>
          </div>
          <div class="confirm-item">
            <span class="confirm-lbl">Habitación</span>
            <strong>{room?.tipoHabitacion ?? '—'}</strong>
          </div>
          <div class="confirm-item">
            <span class="confirm-lbl">Check-in</span>
            <strong>{formatDate(checkInDate)}</strong>
          </div>
          <div class="confirm-item">
            <span class="confirm-lbl">Check-out</span>
            <strong>{formatDate(checkOutDate)}</strong>
          </div>
          <div class="confirm-item">
            <span class="confirm-lbl">Huéspedes</span>
            <strong>{cantidadPersonas}</strong>
          </div>
          <div class="confirm-item">
            <span class="confirm-lbl">Estado</span>
            <strong class="confirm-estado">{reservacion.estado}</strong>
          </div>
          <div class="confirm-item confirm-item--full">
            <span class="confirm-lbl">Total</span>
            <strong class="confirm-total">{fmt(reservacion.total)}</strong>
          </div>
          <div class="confirm-item confirm-item--full">
            <span class="confirm-lbl">Expira</span>
            <strong>{reservacion.fechaExpiracion}</strong>
          </div>
        </div>
        <button class="confirm-btn" on:click={() => navigateTo('home')}>Volver al inicio</button>
      </div>

    {:else}
      <!-- ── Formulario ───────────────────────────────────── -->
      <div class="checkout__content">
        <div class="checkout__main">

          <!-- Facturación -->
          <section class="checkout-section">
            <h2 class="checkout-section__title">Datos de facturación</h2>
            <div class="billing-form">
              {#each FACTURA_FIELDS as [key, lbl, ph, type]}
                <div class="form-field {key === 'direccion' ? 'form-field--full' : ''}">
                  <label for={key} class="form-field__label">{lbl}</label>
                  <input {type} id={key} class="form-field__input"
                    bind:value={factura[key]} placeholder={ph} />
                </div>
              {/each}
            </div>
          </section>

          <!-- Método de pago -->
          <section class="checkout-section">
            <h2 class="checkout-section__title">Método de pago</h2>
            <div class="payment-methods">
              {#each PAYMENT_METHODS as [val, lbl]}
                <label class="payment-method">
                  <input type="radio" name="pm" value={val} bind:group={paymentMethod} class="payment-method__radio" />
                  <div class="payment-method__content">
                    <span class="payment-method__name">{lbl}</span>
                    {#if val === 'tarjeta'}
                      <div class="payment-method__icons">
                        {#each ['VISA','MC','AMEX'] as ic}<span class="payment-icon">{ic}</span>{/each}
                      </div>
                    {/if}
                  </div>
                </label>
              {/each}
            </div>

            {#if paymentMethod === 'tarjeta'}
              <div class="card-form">
                <div class="form-field form-field--full">
                  <label for="cardNum" class="form-field__label">Número de tarjeta</label>
                  <input type="text" id="cardNum" class="form-field__input"
                    value={cardInfo.numero} on:input={formatCardNumber}
                    placeholder="1234 5678 9012 3456" maxlength="19" />
                </div>
                <div class="form-field form-field--full">
                  <label for="cardHolder" class="form-field__label">Titular de la tarjeta</label>
                  <input type="text" id="cardHolder" class="form-field__input"
                    bind:value={cardInfo.titular} placeholder="Nombre como aparece en la tarjeta" />
                </div>
                <div class="form-field">
                  <label for="cardExp" class="form-field__label">Expiración</label>
                  <input type="text" id="cardExp" class="form-field__input"
                    value={cardInfo.expiracion} on:input={formatExpiry}
                    placeholder="MM/AA" maxlength="5" />
                </div>
                <div class="form-field">
                  <label for="cardCvv" class="form-field__label">CVV</label>
                  <input type="password" id="cardCvv" class="form-field__input"
                    bind:value={cardInfo.cvv} placeholder="•••" maxlength="4" />
                </div>
              </div>
            {:else}
              <div class="payment-info">
                <p class="payment-info__text">
                  {paymentMethod === 'paypal'
                    ? 'Serás redirigido a PayPal para completar tu pago.'
                    : 'Recibirás instrucciones de transferencia por correo electrónico.'}
                </p>
              </div>
            {/if}
          </section>

          {#if error}
            <div class="checkout-error">{error}</div>
          {/if}
        </div>

        <!-- Sidebar resumen -->
        <aside class="checkout__sidebar">
          <div class="order-summary">
            <h2 class="order-summary__title">Resumen</h2>

            {#if hotel && room}
              <p class="order-hotel-name">{hotel.nombre}</p>
              <p class="order-hotel-loc">📍 {hotel.ciudad}, {hotel.pais}</p>
              <span class="order-room-badge">🛏 {room.tipoHabitacion}</span>
              <div class="order-rows">
                <div class="order-row">
                  <span>Huéspedes</span>
                  <strong>{cantidadPersonas}</strong>
                </div>
                <div class="order-row">
                  <span>Check-in</span>
                  <strong>{formatDate(checkInDate)}</strong>
                </div>
                <div class="order-row">
                  <span>Check-out</span>
                  <strong>{formatDate(checkOutDate)}</strong>
                </div>
                <div class="order-row">
                  <span>{nights} noche{nights !== 1 ? 's' : ''} × {fmt(room.precioPorNoche)}</span>
                  <strong>{fmt(totalPrice)}</strong>
                </div>
              </div>
            {:else}
              <p style="color:#94a3b8; font-size:.9rem; margin-bottom:1.5rem;">No hay habitación seleccionada.</p>
            {/if}

            <div class="order-summary__divider"></div>

            <div class="order-summary__total">
              <span class="order-summary__total-label">Total estimado</span>
              <span class="order-summary__total-value">{fmt(totalPrice)}</span>
            </div>
            <p class="order-summary__note">El total final lo calcula el servidor al confirmar.</p>

            <button class="order-summary__btn-pay"
              on:click={handlePayment}
              disabled={submitting || !room}>
              {submitting ? 'Procesando...' : 'Confirmar reservación'}
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