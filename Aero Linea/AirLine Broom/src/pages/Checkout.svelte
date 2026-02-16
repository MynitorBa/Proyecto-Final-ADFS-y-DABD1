<script>
  import '../styles/checkout.css';
  import { onMount } from 'svelte';
  export let navigateTo;

  let usuarioId = null;
  let loading = true;
  let error = null;
  let reservacionesPendientes = [];
  let submitting = false;

  onMount(async () => {
    const isLoggedIn = !!sessionStorage.getItem('usuarioId');
    if (!isLoggedIn) {
      navigateTo('acceso-denegado');
      return;
    }
    
    usuarioId = parseInt(sessionStorage.getItem('usuarioId'));
    await cargarReservacionesPendientes();
  });

  async function cargarReservacionesPendientes() {
    loading = true;
    error = null;
    
    try {
      const response = await fetch(`https://localhost:7107/api/mis-reservaciones/usuario/${usuarioId}`);
      
      if (!response.ok) {
        throw new Error('Error al cargar las reservaciones');
      }
      
      const reservaciones = await response.json();
      
      // Filtrar solo las pendientes (estadoReservaId === 1)
      reservacionesPendientes = reservaciones.filter(r => r.estadoReservaId === 1);
      
      // Si no hay reservaciones pendientes, redirigir
      if (reservacionesPendientes.length === 0) {
        navigateTo('home');
        return;
      }
      
    } catch (err) {
      console.error('Error cargando reservaciones:', err);
      error = 'No se pudieron cargar las reservaciones pendientes.';
    } finally {
      loading = false;
    }
  }

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

  async function handlePayment() {
    submitting = true;
    
    try {
      // Confirmar cada reservación pendiente
      const promises = reservacionesPendientes.map(reserva => 
        fetch(`https://localhost:7107/api/reservaciones/${reserva.reservacionId}/confirmar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' }
        })
      );
      
      const responses = await Promise.all(promises);
      
      // Verificar que todas fueron exitosas
      const allSuccess = responses.every(r => r.ok);
      
      if (allSuccess) {
        // Navegar a confirmación con los datos de las reservaciones confirmadas
        navigateTo('confirmacion', { 
          reservaciones: reservacionesPendientes 
        });
      }
      
    } catch (err) {
      console.error('Error al confirmar reservaciones:', err);
    } finally {
      submitting = false;
    }
  }

  function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  function formatTime(timeSpan) {
    if (!timeSpan) return '';
    const parts = timeSpan.split(':');
    return `${parts[0]}:${parts[1]}`;
  }

  function agruparVuelosPorRuta(boletos) {
    if (!boletos || boletos.length === 0) return [];
    
    const vuelos = {};
    
    boletos.forEach(boleto => {
      const key = `${boleto.vueloId}-${boleto.origenCodigo}-${boleto.destinoCodigo}`;
      
      if (!vuelos[key]) {
        vuelos[key] = {
          vueloId: boleto.vueloId,
          numeroVuelo: boleto.numeroVuelo,
          origen: boleto.origenCiudad,
          origenCodigo: boleto.origenCodigo,
          destino: boleto.destinoCiudad,
          destinoCodigo: boleto.destinoCodigo,
          fecha: boleto.fechaVuelo,
          horaSalida: boleto.horaSalida,
          horaLlegada: boleto.horaLlegada,
          clase: boleto.clase,
          cantidadPasajeros: 0,
          precioTotal: 0
        };
      }
      
      vuelos[key].cantidadPasajeros++;
      vuelos[key].precioTotal += boleto.precio;
    });
    
    return Object.values(vuelos);
  }

  $: totalGeneral = reservacionesPendientes.reduce((sum, r) => sum + r.total, 0);
</script>

<div class="checkout">
  <div class="checkout__container">
    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('datos-pasajeros')}>
        Volver a datos de pasajeros
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
        {#if loading}
          <div class="order-summary">
            <p>Cargando resumen...</p>
          </div>
        {:else if error}
          <div class="order-summary">
            <p style="color: #dc2626;">{error}</p>
          </div>
        {:else}
          <div class="order-summary">
            <h2 class="order-summary__title">Resumen del pedido</h2>

            <div class="order-summary__items">
              {#each reservacionesPendientes as reserva}
                {@const vuelos = agruparVuelosPorRuta(reserva.boletos)}
                
                <div class="reserva-group">
                  <div class="reserva-group__header">
                    <strong>Reservación:</strong> {reserva.noReservacion}
                  </div>
                  
                  {#each vuelos as vuelo}
                    <div class="order-item">
                      <div class="order-item__header">
                        <span class="order-item__type">{vuelo.numeroVuelo}</span>
                        <span class="order-item__class">{vuelo.clase}</span>
                      </div>
                      <p class="order-item__route">
                        {vuelo.origen} ({vuelo.origenCodigo}) → {vuelo.destino} ({vuelo.destinoCodigo})
                      </p>
                      <div class="order-item__details">
                        <span class="order-item__date">{formatDate(vuelo.fecha)}</span>
                        <span class="order-item__passengers">
                          {vuelo.cantidadPasajeros} pasajero{vuelo.cantidadPasajeros > 1 ? 's' : ''}
                        </span>
                      </div>
                      <div class="order-item__price">${vuelo.precioTotal.toFixed(2)}</div>
                    </div>
                  {/each}
                  
                  <div class="reserva-group__total">
                    Subtotal: ${reserva.total.toFixed(2)}
                  </div>
                </div>
              {/each}
            </div>

            <div class="order-summary__divider"></div>

            <div class="order-summary__total">
              <span class="order-summary__total-label">Total a pagar</span>
              <span class="order-summary__total-value">${totalGeneral.toFixed(2)}</span>
            </div>

            <button 
              class="order-summary__btn-pay" 
              on:click={handlePayment}
              disabled={submitting}
            >
              {submitting ? 'Procesando...' : 'Pagar'}
            </button>

            <div class="order-summary__security">
              <p class="security-badge">Pago 100% seguro</p>
              <p class="security-note">Tus datos estan protegidos con encriptacion SSL</p>
            </div>
          </div>
        {/if}
      </aside>
    </div>
  </div>
</div>

<style>
  .reserva-group {
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #e5e5e5;
  }

  .reserva-group__header {
    font-size: 0.875rem;
    font-weight: 600;
    color: #666;
    margin-bottom: 0.75rem;
  }

  .reserva-group__total {
    font-size: 0.875rem;
    font-weight: 600;
    color: #c9a96e;
    margin-top: 0.75rem;
    text-align: right;
  }

  .order-summary__btn-pay:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
</style>