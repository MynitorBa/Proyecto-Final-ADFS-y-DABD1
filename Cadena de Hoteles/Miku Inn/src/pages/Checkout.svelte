<script>
  import '../styles/checkout.css';
  export let navigateTo;

  let loading = false, submitting = false;
  let paymentMethod = 'tarjeta';
  let billingInfo = { nombre:'', apellido:'', email:'', telefono:'', pais:'', ciudad:'', direccion:'', codigoPostal:'' };
  let cardInfo = { numero:'', titular:'', expiracion:'', cvv:'' };

  const reservacionesPendientes = [
    {
      reservacionId: 1, noReservacion: 'RES-001', total: 850,
      boletos: [
        { vueloId:1, numeroVuelo:'MK101', origenCiudad:'Guatemala', origenCodigo:'GUA', destinoCiudad:'Miami', destinoCodigo:'MIA', fechaVuelo:'2026-03-15', clase:'Económica', precio:425 },
        { vueloId:1, numeroVuelo:'MK101', origenCiudad:'Guatemala', origenCodigo:'GUA', destinoCiudad:'Miami', destinoCodigo:'MIA', fechaVuelo:'2026-03-15', clase:'Económica', precio:425 }
      ]
    }
  ];

  function handlePayment() {
    submitting = true;
    setTimeout(() => {
      navigateTo('home');
      submitting = false;
    }, 1000);
  }

  const formatDate = d => d ? new Date(d).toLocaleDateString('es-ES', { year:'numeric', month:'long', day:'numeric' }) : '';

  function agruparVuelos(boletos = []) {
    const map = {};
    boletos.forEach(b => {
      const k = `${b.vueloId}-${b.origenCodigo}-${b.destinoCodigo}`;
      if (!map[k]) map[k] = { ...b, cantidadPasajeros:0, precioTotal:0, origen:b.origenCiudad, destino:b.destinoCiudad, fecha:b.fechaVuelo };
      map[k].cantidadPasajeros++;
      map[k].precioTotal += b.precio;
    });
    return Object.values(map);
  }

  $: total = reservacionesPendientes.reduce((s, r) => s + r.total, 0);

  const PAYMENT_METHODS = [
    ['tarjeta', 'Tarjeta de crédito/débito'],
    ['paypal', 'PayPal'],
    ['transferencia', 'Transferencia bancaria']
  ];

  const BILLING_FIELDS = [
    ['nombre','Nombre','Tu nombre','text'],
    ['apellido','Apellido','Tu apellido','text'],
    ['email','Email','correo@ejemplo.com','email'],
    ['telefono','Teléfono','+502 1234-5678','tel'],
    ['pais','País','Guatemala','text'],
    ['ciudad','Ciudad','Ciudad de Guatemala','text']
  ];
</script>

<div class="checkout">
  <div class="checkout__container">
    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('home')}>← Volver</button>
      <h1 class="checkout__title">Finalizar compra</h1>
    </div>

    <div class="checkout__content">
      <div class="checkout__main">

        <!-- Facturación -->
        <section class="checkout-section">
          <h2 class="checkout-section__title">Información de facturación</h2>
          <form class="billing-form">
            {#each BILLING_FIELDS as [key, lbl, ph, type]}
              <div class="form-field">
                <label for={key} class="form-field__label">{lbl}</label>
                <input type={type} id={key} class="form-field__input" bind:value={billingInfo[key]} placeholder={ph} required />
              </div>
            {/each}
            <div class="form-field form-field--full">
              <label for="direccion" class="form-field__label">Dirección</label>
              <input type="text" id="direccion" class="form-field__input" bind:value={billingInfo.direccion} placeholder="Calle, número, zona" required />
            </div>
            <div class="form-field">
              <label for="cp" class="form-field__label">Código Postal</label>
              <input type="text" id="cp" class="form-field__input" bind:value={billingInfo.codigoPostal} placeholder="01001" required />
            </div>
          </form>
        </section>

        <!-- Pago -->
        <section class="checkout-section">
          <h2 class="checkout-section__title">Método de pago</h2>
          <div class="payment-methods">
            {#each PAYMENT_METHODS as [val, lbl]}
              <label class="payment-method">
                <input type="radio" name="paymentMethod" value={val} bind:group={paymentMethod} class="payment-method__radio" />
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
            <form class="card-form">
              <div class="form-field form-field--full">
                <label for="cardNum" class="form-field__label">Número de tarjeta</label>
                <input type="text" id="cardNum" class="form-field__input" bind:value={cardInfo.numero} placeholder="1234 5678 9012 3456" maxlength="19" required />
              </div>
              <div class="form-field form-field--full">
                <label for="cardHolder" class="form-field__label">Titular</label>
                <input type="text" id="cardHolder" class="form-field__input" bind:value={cardInfo.titular} placeholder="Nombre en la tarjeta" required />
              </div>
              <div class="form-field">
                <label for="cardExp" class="form-field__label">Expiración</label>
                <input type="text" id="cardExp" class="form-field__input" bind:value={cardInfo.expiracion} placeholder="MM/AA" maxlength="5" required />
              </div>
              <div class="form-field">
                <label for="cardCvv" class="form-field__label">CVV</label>
                <input type="text" id="cardCvv" class="form-field__input" bind:value={cardInfo.cvv} placeholder="123" maxlength="4" required />
              </div>
            </form>
          {:else}
            <div class="payment-info">
              <p class="payment-info__text">
                {paymentMethod === 'paypal' ? 'Serás redirigido a PayPal para completar tu pago.' : 'Recibirás instrucciones de transferencia por correo electrónico.'}
              </p>
            </div>
          {/if}
        </section>
      </div>

      <!-- Sidebar -->
      <aside class="checkout__sidebar">
        <div class="order-summary">
          <h2 class="order-summary__title">Resumen del pedido</h2>
          <div class="order-summary__items">
            {#each reservacionesPendientes as reserva}
              <div class="reserva-group">
                <div class="reserva-group__header"><strong>Reservación:</strong> {reserva.noReservacion}</div>
                {#each agruparVuelos(reserva.boletos) as v}
                  <div class="order-item">
                    <div class="order-item__header">
                      <span class="order-item__type">{v.numeroVuelo}</span>
                      <span class="order-item__class">{v.clase}</span>
                    </div>
                    <p class="order-item__route">{v.origen} ({v.origenCodigo}) → {v.destino} ({v.destinoCodigo})</p>
                    <div class="order-item__details">
                      <span>{formatDate(v.fecha)}</span>
                      <span>{v.cantidadPasajeros} pasajero{v.cantidadPasajeros > 1 ? 's' : ''}</span>
                    </div>
                    <div class="order-item__price">${v.precioTotal.toFixed(2)}</div>
                  </div>
                {/each}
                <div class="reserva-group__total">Subtotal: ${reserva.total.toFixed(2)}</div>
              </div>
            {/each}
          </div>
          <div class="order-summary__divider"></div>
          <div class="order-summary__total">
            <span class="order-summary__total-label">Total a pagar</span>
            <span class="order-summary__total-value">${total.toFixed(2)}</span>
          </div>
          <button class="order-summary__btn-pay" on:click={handlePayment} disabled={submitting}>
            {submitting ? 'Procesando...' : 'Pagar'}
          </button>
          <div class="order-summary__security">
            <p class="security-badge">Pago 100% seguro</p>
            <p class="security-note">Tus datos están protegidos con encriptación SSL</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</div>