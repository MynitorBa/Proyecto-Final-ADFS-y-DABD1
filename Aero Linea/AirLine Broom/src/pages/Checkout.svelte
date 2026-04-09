<script>
  // @ts-nocheck
/**
 * @file Checkout.svelte
 * @description Pagina de pago que permite a un usuario autenticado pagar todas sus reservaciones
 * pendientes en una sola transaccion. Obtiene las reservaciones pendientes al montar y redirige
 * al inicio si no hay ninguna. Muestra una tarjeta visual interactiva que se actualiza en tiempo
 * real mientras el usuario ingresa el numero de tarjeta, nombre del titular y fecha de expiracion.
 * Realiza validacion del lado del cliente en todos los campos de pago antes de enviar. Al enviar,
 * manda solicitudes POST en paralelo a la API para cada reservacion pendiente y navega a la pagina
 * de confirmacion con los datos de reservacion y factura en caso de exito, o muestra mensajes de
 * error en linea ante fallos.
 */

  import '../styles/checkout.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  /** Funcion de navegacion proporcionada por el enrutador de la aplicacion para cambiar la pagina. @type {Function} */
  export let navigateTo;

  import { API } from '../lib/api.js';

  /** ID del usuario autenticado actualmente, leido del store de sesion. @type {number|null} */
  let usuarioId = null;

  /** Manejador de desuscripcion para la suscripcion al store de sesion. @type {Function} */
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  /** Indica si los datos iniciales de reservacion se estan cargando. @type {boolean} */
  let loading    = true;

  /** Mensaje de error establecido cuando falla la carga inicial de reservaciones. @type {string|null} */
  let error      = null;

  /** Indica si un envio de pago esta actualmente en progreso. @type {boolean} */
  let submitting = false;

  /** Arreglo de reservaciones pendientes obtenidas de la API, usado para construir el resumen del pedido. @type {Array} */
  let reservacionesPendientes = [];

  /** Objeto con mensajes de error de validacion por campo para el formulario de pago. @type {object} */
  let errores = {
    numeroTarjeta:   '',
    nombreTitular:   '',
    fechaExpiracion: '',
    cvv:             '',
    nit:             '',
    codigoPostal:    ''
  };

  /** Objeto con los valores actuales ingresados por el usuario en los campos del formulario de pago. @type {object} */
  let cardInfo = {
    nit:             '',
    codigoPostal:    '',
    numeroTarjeta:   '',
    nombreTitular:   '',
    fechaExpiracion: '',
    cvv:             ''
  };

  // Deriva valores para mostrar en la tarjeta visual a partir de cardInfo; muestra marcadores de posicion cuando los campos estan vacios.
  $: cardDisplay = {
    numero:  cardInfo.numeroTarjeta || '•••• •••• •••• ••••',
    titular: cardInfo.nombreTitular  || 'NOMBRE TITULAR',
    expira:  cardInfo.fechaExpiracion || 'MM/AA'
  };

  /**
   * Hook de ciclo de vida que se ejecuta tras el montaje del componente.
   * Redirige al login si no existe sesion de usuario, de lo contrario carga las reservaciones pendientes.
   * Retorna la funcion de desuscripcion para limpieza.
   * @async
   * @returns {Promise<Function>}
   */
  onMount(async () => {
    if (!usuarioId) { navigateTo('login'); return; }
    await cargarReservacionesPendientes();
    return () => unsubscribe();
  });

  /**
   * Obtiene las reservaciones del usuario desde la API y filtra solo aquellas con
   * estadoReservaId === 1 (Pendiente). Si no hay reservaciones pendientes, redirige
   * al inicio inmediatamente. Establece loading y error como efectos secundarios.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarReservacionesPendientes() {
    loading = true; error = null;
    try {
      const res = await fetch(`${API}/api/mis-reservaciones`, { credentials: 'include' });
      if (!res.ok) throw new Error('Error al cargar las reservaciones');
      const todas = await res.json();
      reservacionesPendientes = todas.filter(r => r.estadoReservaId === 1);
      if (reservacionesPendientes.length === 0) { navigateTo('home'); return; }
    } catch (err) {
      error = 'No se pudieron cargar las reservaciones pendientes.';
    } finally {
      loading = false;
    }
  }

  /**
   * Maneja la entrada en el campo de numero de tarjeta. Elimina no-digitos, limita a 16 digitos
   * como maximo, formatea el valor en grupos de cuatro separados por espacios y limpia el error del campo.
   * @param {Event} e - El evento de entrada del campo de numero de tarjeta.
   */
  function onNumeroInput(e) {
    let v = e.target.value.replace(/\D/g, '').substring(0, 16);
    cardInfo.numeroTarjeta = v.replace(/(.{4})/g, '$1 ').trim();
    errores.numeroTarjeta = '';
  }

  /**
   * Maneja la entrada en el campo de fecha de expiracion. Elimina no-digitos, limita a 4 digitos
   * como maximo e inserta automaticamente una barra despues del segundo digito para producir
   * el formato MM/AA. Limpia el error del campo en cada evento de entrada.
   * @param {Event} e - El evento de entrada del campo de fecha de expiracion.
   */
  function onExpiryInput(e) {
    let v = e.target.value.replace(/[^\d]/g, '').substring(0, 4);
    if (v.length >= 3) v = v.substring(0, 2) + '/' + v.substring(2);
    cardInfo.fechaExpiracion = v;
    errores.fechaExpiracion = '';
  }

  /**
   * Maneja la entrada en el campo de CVV. Elimina no-digitos y limita a 4 digitos como maximo.
   * Limpia el error de CVV en cada evento de entrada.
   * @param {Event} e - El evento de entrada del campo de CVV.
   */
  function onCvvInput(e) {
    cardInfo.cvv = e.target.value.replace(/\D/g, '').substring(0, 4);
    errores.cvv = '';
  }

  /**
   * Maneja la entrada en el campo de NIT. Convierte a mayusculas y permite los valores especiales
   * "C" y "CF" (consumidor final), o elimina todos los caracteres no numericos hasta 12 digitos.
   * Limpia el error de NIT en cada evento de entrada.
   * @param {Event} e - El evento de entrada del campo de NIT.
   */
  function onNitInput(e) {
    let v = e.target.value.toUpperCase();
    if (v === 'C' || v === 'CF') {
      cardInfo.nit = v;
    } else {
      cardInfo.nit = v.replace(/[^0-9]/g, '').substring(0, 12);
    }
    errores.nit = '';
  }

  /**
   * Maneja la entrada en el campo de codigo postal. Elimina no-digitos y limita a 10 digitos
   * como maximo. Limpia el error de codigo postal en cada evento de entrada.
   * @param {Event} e - El evento de entrada del campo de codigo postal.
   */
  function onCodigoPostalInput(e) {
    cardInfo.codigoPostal = e.target.value.replace(/\D/g, '').substring(0, 10);
    errores.codigoPostal = '';
  }

  /**
   * Valida todos los campos requeridos del formulario de pago y llena el objeto errores con
   * mensajes especificos para cada campo invalido. Verifica que el numero de tarjeta tenga
   * exactamente 16 digitos, que el nombre del titular contenga solo letras y sea de al menos
   * 3 caracteres, que la fecha de expiracion coincida con el formato MM/AA y no haya pasado,
   * y que el CVV tenga 3 o 4 digitos.
   * @returns {boolean} Verdadero si todas las validaciones pasan, falso si algun campo tiene error.
   */
  function validar() {
    let ok = true;
    errores = { numeroTarjeta: '', nombreTitular: '', fechaExpiracion: '', cvv: '', nit: '', codigoPostal: '' };

    const raw = cardInfo.numeroTarjeta.replace(/\s/g, '');
    if (!raw) {
      errores.numeroTarjeta = 'El número de tarjeta es requerido.'; ok = false;
    } else if (!/^\d{16}$/.test(raw)) {
      errores.numeroTarjeta = 'Debe tener exactamente 16 dígitos.'; ok = false;
    }

    if (!cardInfo.nombreTitular.trim()) {
      errores.nombreTitular = 'El nombre del titular es requerido.'; ok = false;
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]{3,60}$/.test(cardInfo.nombreTitular.trim())) {
      errores.nombreTitular = 'Solo letras, mínimo 3 caracteres.'; ok = false;
    }

    if (!cardInfo.fechaExpiracion) {
      errores.fechaExpiracion = 'La fecha de expiración es requerida.'; ok = false;
    } else if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(cardInfo.fechaExpiracion)) {
      errores.fechaExpiracion = 'Formato MM/AA requerido (ej. 12/28).'; ok = false;
    } else {
      const [mm, yy] = cardInfo.fechaExpiracion.split('/');
      const exp = new Date(2000 + parseInt(yy), parseInt(mm) - 1 + 1, 0);
      if (exp < new Date()) {
        errores.fechaExpiracion = 'Esta tarjeta está vencida.'; ok = false;
      }
    }

    if (!cardInfo.cvv) {
      errores.cvv = 'El CVV es requerido.'; ok = false;
    } else if (!/^\d{3,4}$/.test(cardInfo.cvv)) {
      errores.cvv = 'El CVV debe tener 3 o 4 dígitos.'; ok = false;
    }

    return ok;
  }

  /**
   * Valida el formulario y luego envia el pago de todas las reservaciones pendientes en paralelo.
   * Para cada reservacion, envia un POST al endpoint /api/reservaciones/:id/comprar con los datos
   * de la tarjeta. Si todas las solicitudes tienen exito, navega a la pagina de confirmacion
   * pasando la lista de reservaciones y el arreglo de objetos de factura retornados. Ante fallo
   * parcial o total, muestra el mensaje de error de la API en el campo de error del numero de tarjeta.
   * @async
   * @returns {Promise<void>}
   */
  async function handlePayment() {
    if (!validar()) return;

    submitting = true;
    try {
      const raw = cardInfo.numeroTarjeta.replace(/\s/g, '');
      const reservacionesParaConfirmacion = JSON.parse(JSON.stringify(reservacionesPendientes));

      const promises = reservacionesPendientes.map(reserva =>
        fetch(`${API}/api/reservaciones/${reserva.reservacionId}/comprar`, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            nit:             cardInfo.nit || 'CF',
            codigoPostal:    cardInfo.codigoPostal || '01001',
            numeroTarjeta:   raw,
            nombreTitular:   cardInfo.nombreTitular,
            fechaExpiracion: cardInfo.fechaExpiracion,
            cvv:             cardInfo.cvv
          })
        })
      );

      const responses = await Promise.all(promises);
      const jsons     = await Promise.all(responses.map(r => r.json()));
      const allOk     = responses.every(r => r.ok);

      if (allOk) {
        navigateTo('confirmacion', {
          reservaciones: reservacionesParaConfirmacion,
          facturas: jsons
        });
      } else {
        const msg = jsons.find(j => j.mensaje || j.message);
        errores.numeroTarjeta = msg?.mensaje ?? msg?.message ?? 'Error al procesar el pago.';
      }
    } catch (err) {
      errores.numeroTarjeta = 'Error de conexión. Intenta de nuevo.';
    } finally {
      submitting = false;
    }
  }

  /**
   * Agrupa el arreglo plano de boletos de una reservacion por vuelo (vueloId) y calcula
   * la cantidad de pasajeros y el precio total por grupo de vuelo.
   * @param {Array} boletos - Arreglo de objetos de boleto de una sola reservacion.
   * @returns {Array<{numeroVuelo: string, origenCodigo: string, destinoCodigo: string, origenCiudad: string, destinoCiudad: string, fecha: string, clase: string, cantidadPasajeros: number, precioTotal: number}>}
   */
  function agruparVuelosPorRuta(boletos) {
    if (!boletos?.length) return [];
    const m = {};
    boletos.forEach(b => {
      if (!m[b.vueloId]) m[b.vueloId] = {
        numeroVuelo: b.numeroVuelo,
        origenCodigo: b.origenCodigo, destinoCodigo: b.destinoCodigo,
        origenCiudad: b.origenCiudad, destinoCiudad: b.destinoCiudad,
        fecha: b.fechaVuelo, clase: b.clase, cantidadPasajeros: 0, precioTotal: 0
      };
      m[b.vueloId].cantidadPasajeros++;
      m[b.vueloId].precioTotal += b.precio;
    });
    return Object.values(m);
  }

  /**
   * Formatea una cadena de fecha usando el locale es-ES con el nombre completo del mes.
   * Retorna una cadena vacia si el input es falsy.
   * @param {string|null} d - Cadena de fecha ISO a formatear.
   * @returns {string} Fecha formateada como "15 de enero de 2025" o "".
   */
  function formatDate(d) {
    if (!d) return '';
    return new Date(d).toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  // Calcula la suma de todos los totales de reservaciones pendientes para mostrar el gran total en el resumen del pedido.
  $: totalGeneral = reservacionesPendientes.reduce((s, r) => s + r.total, 0);
</script>

<!-- Contenedor principal de la pagina de pago -->
<div class="checkout">
  <div class="checkout__container">

    <!-- Encabezado con titulo y boton de regreso al carrito -->
    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('carrito')}>
        &larr; Volver al carrito
      </button>
      <h1 class="checkout__title">Finalizar compra</h1>
    </div>

    <div class="checkout__content">

      <!-- Formulario de pago con visual de tarjeta y campos de datos -->
      <div class="checkout__main">
        <section class="checkout-section">
          <h2 class="checkout-section__title">Datos de la tarjeta</h2>

          <!-- Visual interactivo de la tarjeta que refleja los datos ingresados -->
          <div class="card-visual">
            <div class="card-visual__chip">
              <svg viewBox="0 0 40 30" width="40" height="30">
                <rect width="40" height="30" rx="4" fill="url(#chipGrad)"/>
                <line x1="0" y1="10" x2="40" y2="10" stroke="rgba(0,0,0,0.15)" stroke-width="1"/>
                <line x1="0" y1="20" x2="40" y2="20" stroke="rgba(0,0,0,0.15)" stroke-width="1"/>
                <line x1="13" y1="0" x2="13" y2="30" stroke="rgba(0,0,0,0.15)" stroke-width="1"/>
                <line x1="27" y1="0" x2="27" y2="30" stroke="rgba(0,0,0,0.15)" stroke-width="1"/>
                <defs>
                  <linearGradient id="chipGrad" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="#c9a96e"/>
                    <stop offset="100%" stop-color="#8B6B4A"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <div class="card-visual__number">{cardDisplay.numero}</div>
            <div class="card-visual__bottom">
              <div>
                <span class="card-visual__label">TITULAR</span>
                <span class="card-visual__value">{cardDisplay.titular}</span>
              </div>
              <div>
                <span class="card-visual__label">EXPIRA</span>
                <span class="card-visual__value">{cardDisplay.expira}</span>
              </div>
              <div class="card-visual__logo">BROOM</div>
            </div>
          </div>

          <div class="form-campo">
            <label for="card-numero" class="form-campo__label">Número de tarjeta</label>
            <input
              id="card-numero"
              type="text"
              class="form-campo__input"
              class:form-campo__input--error={errores.numeroTarjeta}
              value={cardInfo.numeroTarjeta}
              on:input={onNumeroInput}
              placeholder="1234 5678 9012 3456"
              maxlength="19"
              inputmode="numeric"
              autocomplete="one-time-code"
            />
            {#if errores.numeroTarjeta}
              <span class="form-campo__error">{errores.numeroTarjeta}</span>
            {/if}
          </div>

          <div class="form-campo">
            <label for="card-titular" class="form-campo__label">Nombre del titular</label>
            <input
              id="card-titular"
              type="text"
              class="form-campo__input"
              class:form-campo__input--error={errores.nombreTitular}
              bind:value={cardInfo.nombreTitular}
              on:input={() => errores.nombreTitular = ''}
              placeholder="Como aparece en la tarjeta"
              autocomplete="one-time-code"
            />
            {#if errores.nombreTitular}
              <span class="form-campo__error">{errores.nombreTitular}</span>
            {/if}
          </div>

          <div class="form-fila">
            <div class="form-campo">
              <label for="card-expiry" class="form-campo__label">Fecha de expiración</label>
              <input
                id="card-expiry"
                type="text"
                class="form-campo__input"
                class:form-campo__input--error={errores.fechaExpiracion}
                value={cardInfo.fechaExpiracion}
                on:input={onExpiryInput}
                placeholder="MM/AA"
                maxlength="5"
                inputmode="numeric"
                autocomplete="one-time-code"
              />
              {#if errores.fechaExpiracion}
                <span class="form-campo__error">{errores.fechaExpiracion}</span>
              {/if}
            </div>
            <div class="form-campo">
              <label for="card-cvv" class="form-campo__label">CVV</label>
              <input
                id="card-cvv"
                type="text"
                class="form-campo__input form-campo__input--cvv"
                class:form-campo__input--error={errores.cvv}
                value={cardInfo.cvv}
                on:input={onCvvInput}
                placeholder="•••"
                maxlength="4"
                inputmode="numeric"
                autocomplete="one-time-code"
              />
              {#if errores.cvv}
                <span class="form-campo__error">{errores.cvv}</span>
              {/if}
            </div>
          </div>

          <div class="form-fila">
            <div class="form-campo">
              <label for="card-nit" class="form-campo__label">NIT <span class="form-campo__opcional">(opcional)</span></label>
              <input
                id="card-nit"
                type="text"
                class="form-campo__input"
                class:form-campo__input--error={errores.nit}
                value={cardInfo.nit}
                on:input={onNitInput}
                placeholder="CF"
                maxlength="12"
                autocomplete="off"
              />
              {#if errores.nit}
                <span class="form-campo__error">{errores.nit}</span>
              {/if}
            </div>
            <div class="form-campo">
              <label for="card-postal" class="form-campo__label">Código postal <span class="form-campo__opcional">(opcional)</span></label>
              <input
                id="card-postal"
                type="text"
                class="form-campo__input"
                class:form-campo__input--error={errores.codigoPostal}
                value={cardInfo.codigoPostal}
                on:input={onCodigoPostalInput}
                placeholder="01001"
                maxlength="10"
                inputmode="numeric"
                autocomplete="off"
              />
              {#if errores.codigoPostal}
                <span class="form-campo__error">{errores.codigoPostal}</span>
              {/if}
            </div>
          </div>

          <div class="checkout-seguridad">
            <span class="checkout-seguridad__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              SSL Seguro
            </span>
            <span class="checkout-seguridad__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
              VISA / Mastercard / AMEX
            </span>
          </div>
        </section>
      </div>

      <!-- Sidebar con resumen del pedido, total y boton de pago -->
      <aside class="checkout__sidebar">
        {#if loading}
          <div class="order-summary"><p class="checkout-loading">Cargando...</p></div>
        {:else if error}
          <div class="order-summary"><p class="checkout-error">{error}</p></div>
        {:else}
          <div class="order-summary">
            <h2 class="order-summary__title">Resumen del pedido</h2>

            <div class="order-summary__items">
              {#each reservacionesPendientes as reserva}
                {@const vuelos = agruparVuelosPorRuta(reserva.boletos)}
                <div class="reserva-group">
                  <div class="reserva-group__header">Reservación: {reserva.noReservacion}</div>
                  {#each vuelos as vuelo}
                    <div class="order-item">
                      <div class="order-item__header">
                        <span class="order-item__type">{vuelo.numeroVuelo}</span>
                        <span class="order-item__clase">{vuelo.clase}</span>
                      </div>
                      <p class="order-item__route">{vuelo.origenCodigo} &rarr; {vuelo.destinoCodigo}</p>
                      <div class="order-item__details">
                        <span>{formatDate(vuelo.fecha)}</span>
                        <span>{vuelo.cantidadPasajeros} pasajero{vuelo.cantidadPasajeros > 1 ? 's' : ''}</span>
                      </div>
                      <div class="order-item__price">$ {vuelo.precioTotal.toFixed(2)}</div>
                    </div>
                  {/each}
                  <div class="reserva-group__total">Subtotal: $ {reserva.total.toFixed(2)}</div>
                </div>
              {/each}
            </div>

            <div class="order-summary__divider"></div>

            <div class="order-summary__total">
              <span class="order-summary__total-label">Total a pagar</span>
              <span class="order-summary__total-value">$ {totalGeneral.toFixed(2)}</span>
            </div>

            <button
              class="order-summary__btn-pay"
              on:click={handlePayment}
              disabled={submitting}
            >
              {#if submitting}
                <svg class="btn-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
                </svg>
                Procesando...
              {:else}
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                Pagar ahora
              {/if}
            </button>

            <div class="order-security">
              <p class="order-security__badge">PAGO 100% SEGURO</p>
              <p class="order-security__note">Tus datos están protegidos con encriptación SSL de 256 bits</p>
            </div>
          </div>
        {/if}
      </aside>
    </div>
  </div>
</div>
