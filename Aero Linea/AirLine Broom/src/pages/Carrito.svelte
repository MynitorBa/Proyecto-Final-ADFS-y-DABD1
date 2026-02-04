<script>
// @ts-nocheck

  import '../styles/carrito.css';
  export let navigateTo;

  let cartItems = [
    {
      id: 1,
      type: 'ida',
      flightNumber: 'AF 1234',
      origin: 'Ciudad de Guatemala',
      destination: 'Paris',
      departureDate: '2026-02-15',
      departureTime: '08:00',
      arrivalTime: '18:30',
      duration: '10h 30m',
      airline: 'Air France',
      class: 'ejecutivo',
      price: 1450,
      passengers: 2
    },
    {
      id: 2,
      type: 'regreso',
      flightNumber: 'AF 5678',
      origin: 'Paris',
      destination: 'Ciudad de Guatemala',
      departureDate: '2026-02-25',
      departureTime: '10:00',
      arrivalTime: '16:30',
      duration: '10h 30m',
      airline: 'Air France',
      class: 'ejecutivo',
      price: 1450,
      passengers: 2
    },
  ];

  function calculateSubtotal(item) {
    return item.price * item.passengers;
  }

  function calculateTotal() {
    return cartItems.reduce((total, item) => total + calculateSubtotal(item), 0);
  }

  function removeItem(itemId) {
    cartItems = cartItems.filter(item => item.id !== itemId);
  }

  function updatePassengers(itemId, newValue) {
    const item = cartItems.find(i => i.id === itemId);
    if (item) {
      item.passengers = parseInt(newValue);
      cartItems = [...cartItems];
    }
  }

  function goToCheckout() {
    navigateTo('checkout');
  }
</script>

<div class="carrito">
  <div class="carrito__container">
    <div class="carrito__header">
      <button class="carrito__back" on:click={() => navigateTo('home')}>
        Continuar comprando
      </button>
      <h1 class="carrito__title">Carrito de compras</h1>
      <p class="carrito__subtitle">{cartItems.length} vuelos en tu carrito</p>
    </div>

    <div class="carrito__content">
      <div class="carrito__items">
        {#if cartItems.length === 0}
          <div class="carrito__empty">
            <p class="carrito__empty-message">Tu carrito esta vacio</p>
            <button class="carrito__empty-btn" on:click={() => navigateTo('home')}>
              Buscar vuelos
            </button>
          </div>
        {:else}
          {#each cartItems as item}
            <article class="cart-item">
              <div class="cart-item__header">
                <div class="cart-item__type-badge" class:cart-item__type-badge--return={item.type === 'regreso'}>
                  {item.type === 'ida' ? 'Vuelo de Ida' : 'Vuelo de Regreso'}
                </div>
                <button class="cart-item__remove" on:click={() => removeItem(item.id)}>
                  Eliminar
                </button>
              </div>

              <div class="cart-item__content">
                <div class="cart-item__flight-info">
                  <h3 class="cart-item__flight-number">{item.airline} - {item.flightNumber}</h3>
                  <p class="cart-item__route">{item.origin} → {item.destination}</p>
                </div>

                <div class="cart-item__details">
                  <div class="cart-item__detail">
                    <span class="cart-item__detail-label">Fecha</span>
                    <span class="cart-item__detail-value">{item.departureDate}</span>
                  </div>
                  <div class="cart-item__detail">
                    <span class="cart-item__detail-label">Horario</span>
                    <span class="cart-item__detail-value">{item.departureTime} - {item.arrivalTime}</span>
                  </div>
                  <div class="cart-item__detail">
                    <span class="cart-item__detail-label">Duracion</span>
                    <span class="cart-item__detail-value">{item.duration}</span>
                  </div>
                  <div class="cart-item__detail">
                    <span class="cart-item__detail-label">Clase</span>
                    <span class="cart-item__detail-value cart-item__detail-value--class">{item.class}</span>
                  </div>
                </div>

                <div class="cart-item__pricing">
                  <div class="cart-item__passengers">
                    <label for="passengers-{item.id}" class="cart-item__passengers-label">
                      Pasajeros
                    </label>
                    <select 
                      id="passengers-{item.id}"
                      class="cart-item__passengers-select"
                      value={item.passengers}
                      on:change={(e) => updatePassengers(item.id, e.target.value)}
                    >
                      {#each Array(9) as _, i}
                        <option value={i + 1}>{i + 1}</option>
                      {/each}
                    </select>
                  </div>

                  <div class="cart-item__price-breakdown">
                    <div class="price-row">
                      <span class="price-row__label">Precio por pasajero</span>
                      <span class="price-row__value">${item.price}</span>
                    </div>
                    <div class="price-row price-row--total">
                      <span class="price-row__label">Subtotal</span>
                      <span class="price-row__value">${calculateSubtotal(item)}</span>
                    </div>
                  </div>
                </div>
              </div>
            </article>
          {/each}
        {/if}
      </div>

      {#if cartItems.length > 0}
        <aside class="carrito__summary">
          <div class="cart-summary">
            <h2 class="cart-summary__title">Resumen del pedido</h2>

            <div class="cart-summary__items">
              {#each cartItems as item}
                <div class="summary-item">
                  <span class="summary-item__description">
                    {item.origin} → {item.destination} ({item.passengers} {item.passengers === 1 ? 'pasajero' : 'pasajeros'})
                  </span>
                  <span class="summary-item__price">${calculateSubtotal(item)}</span>
                </div>
              {/each}
            </div>

            <div class="cart-summary__divider"></div>

            <div class="cart-summary__taxes">
              <div class="tax-row">
                <span class="tax-row__label">Subtotal</span>
                <span class="tax-row__value">${calculateTotal()}</span>
              </div>
              <div class="tax-row">
                <span class="tax-row__label">Impuestos y cargos</span>
                <span class="tax-row__value">${(calculateTotal() * 0.12).toFixed(2)}</span>
              </div>
            </div>

            <div class="cart-summary__divider"></div>

            <div class="cart-summary__total">
              <span class="cart-summary__total-label">Total</span>
              <span class="cart-summary__total-value">${(calculateTotal() * 1.12).toFixed(2)}</span>
            </div>

            <button class="cart-summary__btn-checkout" on:click={goToCheckout}>
              Proceder al pago
            </button>

            <p class="cart-summary__note">
              Los precios incluyen todos los impuestos y cargos
            </p>
          </div>
        </aside>
      {/if}
    </div>
  </div>
</div>