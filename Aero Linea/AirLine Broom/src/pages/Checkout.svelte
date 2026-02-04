<script>
  import '../styles/checkout.css';
  export let navigateTo;

  let paymentMethod = 'tarjeta';
  let billingInfo = {
    nombre: '',
    apellido: '',
    email: '',
    telefono: '',
    pais: '',
    ciudad: '',
    direccion: '',
    codigoPostal: ''
  };

  let cardInfo = {
    numero: '',
    titular: '',
    expiracion: '',
    cvv: ''
  };

  const orderSummary = {
    items: [
      {
        type: 'ida',
        route: 'Ciudad de Guatemala → Paris',
        date: '2026-02-15',
        passengers: 2,
        class: 'ejecutivo',
        subtotal: 2900
      },
      {
        type: 'regreso',
        route: 'Paris → Ciudad de Guatemala',
        date: '2026-02-25',
        passengers: 2,
        class: 'ejecutivo',
        subtotal: 2900
      },
    ],
    subtotal: 5800,
    taxes: 696,
    total: 6496
  };

  function handlePayment() {
    console.log('Procesando pago:', {
      paymentMethod,
      billingInfo,
      cardInfo: paymentMethod === 'tarjeta' ? cardInfo : null
    });
    navigateTo('confirmacion');
  }
</script>

<div class="checkout">
  <div class="checkout__container">
    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('carrito')}>
        Volver al carrito
      </button>
      <h1 class="checkout__title">Finalizar compra</h1>
    </div>

    <div class="checkout__content">
      <div class="checkout__main">
        <section class="checkout-section">
          <h2 class="checkout-section__title">Informacion de facturacion</h2>
          
          <form class="billing-form">
            <div class="billing-form__row">
              <div class="form-field">
                <label for="nombre" class="form-field__label">Nombre</label>
                <input 
                  type="text" 
                  id="nombre"
                  class="form-field__input"
                  bind:value={billingInfo.nombre}
                  placeholder="Tu nombre"
                  required
                />
              </div>

              <div class="form-field">
                <label for="apellido" class="form-field__label">Apellido</label>
                <input 
                  type="text" 
                  id="apellido"
                  class="form-field__input"
                  bind:value={billingInfo.apellido}
                  placeholder="Tu apellido"
                  required
                />
              </div>
            </div>

            <div class="billing-form__row">
              <div class="form-field">
                <label for="email" class="form-field__label">Email</label>
                <input 
                  type="email" 
                  id="email"
                  class="form-field__input"
                  bind:value={billingInfo.email}
                  placeholder="correo@ejemplo.com"
                  required
                />
              </div>

              <div class="form-field">
                <label for="telefono" class="form-field__label">Telefono</label>
                <input 
                  type="tel" 
                  id="telefono"
                  class="form-field__input"
                  bind:value={billingInfo.telefono}
                  placeholder="+502 1234-5678"
                  required
                />
              </div>
            </div>

            <div class="billing-form__row">
              <div class="form-field">
                <label for="pais" class="form-field__label">Pais</label>
                <input 
                  type="text" 
                  id="pais"
                  class="form-field__input"
                  bind:value={billingInfo.pais}
                  placeholder="Guatemala"
                  required
                />
              </div>

              <div class="form-field">
                <label for="ciudad" class="form-field__label">Ciudad</label>
                <input 
                  type="text" 
                  id="ciudad"
                  class="form-field__input"
                  bind:value={billingInfo.ciudad}
                  placeholder="Ciudad de Guatemala"
                  required
                />
              </div>
            </div>

            <div class="billing-form__row">
              <div class="form-field form-field--full">
                <label for="direccion" class="form-field__label">Direccion</label>
                <input 
                  type="text" 
                  id="direccion"
                  class="form-field__input"
                  bind:value={billingInfo.direccion}
                  placeholder="Calle, numero, zona"
                  required
                />
              </div>
            </div>

            <div class="billing-form__row">
              <div class="form-field">
                <label for="codigoPostal" class="form-field__label">Codigo Postal</label>
                <input 
                  type="text" 
                  id="codigoPostal"
                  class="form-field__input"
                  bind:value={billingInfo.codigoPostal}
                  placeholder="01001"
                  required
                />
              </div>
            </div>
          </form>
        </section>

        <section class="checkout-section">
          <h2 class="checkout-section__title">Metodo de pago</h2>
          
          <div class="payment-methods">
            <label class="payment-method">
              <input 
                type="radio" 
                name="paymentMethod" 
                value="tarjeta"
                bind:group={paymentMethod}
                class="payment-method__radio"
              />
              <div class="payment-method__content">
                <span class="payment-method__name">Tarjeta de credito/debito</span>
                <div class="payment-method__icons">
                  <span class="payment-icon">VISA</span>
                  <span class="payment-icon">MC</span>
                  <span class="payment-icon">AMEX</span>
                </div>
              </div>
            </label>

            <label class="payment-method">
              <input 
                type="radio" 
                name="paymentMethod" 
                value="paypal"
                bind:group={paymentMethod}
                class="payment-method__radio"
              />
              <div class="payment-method__content">
                <span class="payment-method__name">PayPal</span>
              </div>
            </label>

            <label class="payment-method">
              <input 
                type="radio" 
                name="paymentMethod" 
                value="transferencia"
                bind:group={paymentMethod}
                class="payment-method__radio"
              />
              <div class="payment-method__content">
                <span class="payment-method__name">Transferencia bancaria</span>
              </div>
            </label>
          </div>

          {#if paymentMethod === 'tarjeta'}
            <form class="card-form">
              <div class="form-field form-field--full">
                <label for="cardNumber" class="form-field__label">Numero de tarjeta</label>
                <input 
                  type="text" 
                  id="cardNumber"
                  class="form-field__input"
                  bind:value={cardInfo.numero}
                  placeholder="1234 5678 9012 3456"
                  maxlength="19"
                  required
                />
              </div>

              <div class="form-field form-field--full">
                <label for="cardHolder" class="form-field__label">Titular de la tarjeta</label>
                <input 
                  type="text" 
                  id="cardHolder"
                  class="form-field__input"
                  bind:value={cardInfo.titular}
                  placeholder="Nombre como aparece en la tarjeta"
                  required
                />
              </div>

              <div class="card-form__row">
                <div class="form-field">
                  <label for="cardExpiry" class="form-field__label">Fecha de expiracion</label>
                  <input 
                    type="text" 
                    id="cardExpiry"
                    class="form-field__input"
                    bind:value={cardInfo.expiracion}
                    placeholder="MM/AA"
                    maxlength="5"
                    required
                  />
                </div>

                <div class="form-field">
                  <label for="cardCVV" class="form-field__label">CVV</label>
                  <input 
                    type="text" 
                    id="cardCVV"
                    class="form-field__input"
                    bind:value={cardInfo.cvv}
                    placeholder="123"
                    maxlength="4"
                    required
                  />
                </div>
              </div>
            </form>
          {:else if paymentMethod === 'paypal'}
            <div class="payment-info">
              <p class="payment-info__text">
                Seras redirigido a PayPal para completar tu pago de forma segura.
              </p>
            </div>
          {:else if paymentMethod === 'transferencia'}
            <div class="payment-info">
              <p class="payment-info__text">
                Recibiras las instrucciones de transferencia por correo electronico despues de confirmar tu pedido.
              </p>
            </div>
          {/if}
        </section>
      </div>

      <aside class="checkout__sidebar">
        <div class="order-summary">
          <h2 class="order-summary__title">Resumen del pedido</h2>

          <div class="order-summary__items">
            {#each orderSummary.items as item}
              <div class="order-item">
                <div class="order-item__header">
                  <span class="order-item__type">{item.type === 'ida' ? 'Ida' : 'Regreso'}</span>
                  <span class="order-item__class">{item.class}</span>
                </div>
                <p class="order-item__route">{item.route}</p>
                <div class="order-item__details">
                  <span class="order-item__date">{item.date}</span>
                  <span class="order-item__passengers">{item.passengers} pasajero{item.passengers > 1 ? 's' : ''}</span>
                </div>
                <div class="order-item__price">${item.subtotal}</div>
              </div>
            {/each}
          </div>

          <div class="order-summary__divider"></div>

          <div class="order-summary__totals">
            <div class="total-row">
              <span class="total-row__label">Subtotal</span>
              <span class="total-row__value">${orderSummary.subtotal}</span>
            </div>
            <div class="total-row">
              <span class="total-row__label">Impuestos y cargos</span>
              <span class="total-row__value">${orderSummary.taxes}</span>
            </div>
          </div>

          <div class="order-summary__divider"></div>

          <div class="order-summary__total">
            <span class="order-summary__total-label">Total a pagar</span>
            <span class="order-summary__total-value">${orderSummary.total}</span>
          </div>

          <button class="order-summary__btn-pay" on:click={handlePayment}>
            Pagar
          </button>

          <div class="order-summary__security">
            <p class="security-badge">Pago 100% seguro</p>
            <p class="security-note">Tus datos estan protegidos con encriptacion SSL</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</div>