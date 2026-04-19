<script>
  /**
   * @file Checkout.svelte
   * @description Pagina de pago de reservaciones pendientes. Muestra un resumen de las
   * reservaciones del usuario, permite ingresar los datos de la tarjeta y procesa el pago
   * contra el backend. Al finalizar redirige a la pagina de agradecimiento con las facturas.
   */

  import '../styles/checkout.css';

  /** Funcion de navegacion inyectada por el router. @type {Function} */
  export let navigateTo;

  /**
   * Datos opcionales para pre-cargar reservaciones desde otra pagina.
   * Si se provee, se omite la llamada al API.
   * @type {any}
   */
  export let checkoutData = null;

  /** URL base del backend. @type {string} */
      import { API } from '../lib/api.js';


  /** Indica si las reservaciones aun estan cargando. @type {boolean} */
  let loading = true;

  /** Mensaje de error al cargar las reservaciones. @type {string} */
  let loadError = '';

  /** Lista de reservaciones agrupadas y pendientes de pago. @type {any[]} */
  let reservations = [];

  /** Indica si el formulario de pago se esta enviando. @type {boolean} */
  let submitting = false;

  /** Mensaje de error que puede surgir al intentar pagar. @type {string} */
  let payError = '';

  /**
   * Porcentaje de descuento de alianza propagado desde HotelDetail via checkoutData.
   * Se muestra en el banner informativo al inicio del checkout.
   * @type {number|null}
   */
  let porcentajeDescuento = checkoutData?.pendingReservations?.[0]?.porcentajeDescuento
    ?? (sessionStorage.getItem('alianzaDescuento') ? Number(sessionStorage.getItem('alianzaDescuento')) : null);

  /**
   * Datos del formulario de pago.
   * @type {{ nit: string, codigoPostal: string, numeroTarjeta: string, nombreTitular: string, fechaVencimiento: string, cvv: string }}
   */
  let form = {
    nit:             '',
    codigoPostal:    '',
    numeroTarjeta:   '',
    nombreTitular:   '',
    fechaVencimiento:'',
    cvv:             '',
  };

  /**
   * Formatea un numero como moneda USD con separadores guatemaltecos.
   * @param {number} p - El valor numerico a formatear.
   * @returns {string}
   */
  const fmt = p => new Intl.NumberFormat('es-GT', {
    style: 'currency', currency: 'USD', minimumFractionDigits: 2
  }).format(p);

  /**
   * Extrae solo la parte de fecha (YYYY-MM-DD) de una cadena de fecha completa.
   * @param {string} str - Fecha en formato string.
   * @returns {string}
   */
  function fmtDate(str) {
    if (!str) return '—';
    return str.toString().split(' ')[0];
  }

  /**
   * Formatea el numero de tarjeta con espacios cada 4 digitos al escribir.
   * @param {Event} e - Evento del input.
   */
  function formatCardNumber(e) {
    let v = /** @type {HTMLInputElement} */ (e.target).value.replace(/\D/g,'').slice(0,16);
    form.numeroTarjeta = v.replace(/(.{4})/g,'$1 ').trim();
  }

  /**
   * Formatea la fecha de vencimiento de la tarjeta en formato MM/AA al escribir.
   * @param {Event} e - Evento del input.
   */
  function formatExpiry(e) {
    let v = /** @type {HTMLInputElement} */ (e.target).value.replace(/\D/g,'').slice(0,4);
    if (v.length > 2) v = v.slice(0,2)+'/'+v.slice(2);
    form.fechaVencimiento = v;
  }

  /**
   * Agrupa las filas planas que devuelve el API en objetos de reservacion,
   * cada uno con su array de habitaciones asociadas.
   * @param {any[]} rows - Filas crudas del API.
   * @returns {any[]}
   */
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

  /**
   * Carga las reservaciones con estado "pendiente" del usuario autenticado.
   * Si se recibieron datos previos via prop, los usa directamente sin llamar al API.
   * @async
   * @returns {Promise<void>}
   */
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

  // Suma total de todas las reservaciones a pagar.
  $: totalSelected = reservations.reduce((s, r) => s + (r.total || 0), 0);

  // True si hay al menos una reservacion lista para pagar.
  $: anySelected = reservations.length > 0;

  /**
   * Valida los campos del formulario de pago antes de enviarlo.
   * @returns {string|null} Mensaje de error o null si todo es valido.
   */
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

  /**
   * Procesa el pago de todas las reservaciones pendientes.
   * Itera cada reservacion, llama al endpoint de pago y acumula las facturas generadas.
   * Al terminar, si hay facturas exitosas navega a la pantalla de agradecimiento.
   * Tambien dispara el envio del correo de confirmacion de forma asincrona.
   * @async
   * @returns {Promise<void>}
   */
  async function handlePay() {
    payError = '';
    const err = validate();
    if (err) { payError = err; return; }

    submitting = true;
    // Solo incluir el token si hay descuento activo para esta reservacion.
    // Si la busqueda fue en una ciudad diferente a la del token, porcentajeDescuento
    // sera null y el token no se envia, evitando el error del backend.
    const alianzaTokenActivo = porcentajeDescuento
      ? (sessionStorage.getItem('alianzaTokenActivo') || null)
      : null;
    const payload = {
      nit:              form.nit.trim(),
      codigoPostal:     form.codigoPostal.trim(),
      numeroTarjeta:    form.numeroTarjeta.replace(/\s/g,''),
      nombreTitular:    form.nombreTitular.trim(),
      fechaVencimiento: form.fechaVencimiento.trim(),
      cvv:              form.cvv.trim(),
      tokenAlianza:     alianzaTokenActivo,
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
      // Solo limpiar el estado de alianza si el token fue realmente enviado y aplicado.
      // Si el pago fue sin descuento (alianzaTokenActivo == null), el token sigue
      // valido en el backend y el usuario puede usarlo en la ciudad correcta.
      if (alianzaTokenActivo) {
        sessionStorage.removeItem('alianzaTokenActivo');
        sessionStorage.removeItem('alianzaDescuento');
        sessionStorage.removeItem('alianzaCiudad');
        sessionStorage.removeItem('alianzaPais');
        sessionStorage.removeItem('alianzaPct');
      }
      navigateTo('agradecimiento', { facturas });
    }
  }
</script>

<!-- Contenedor principal del checkout -->
<div class="checkout">
  <div class="checkout__container">

    <!-- Cabecera con boton para volver y titulo de la seccion -->
    <div class="checkout__header">
      <button class="checkout__back" on:click={() => navigateTo('home')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver al inicio
      </button>
      <h1 class="checkout__title">Pagar reservaciones</h1>
    </div>

    <!-- Banner de descuento de alianza, visible cuando la reservacion viene de un token de alianza -->
    {#if porcentajeDescuento}
      <div style="background:linear-gradient(135deg,#064e3b,#059669 55%,#34d399);border-radius:16px;padding:1.25rem 2rem;margin-bottom:2rem;display:flex;align-items:center;gap:1.5rem;position:relative;overflow:hidden;box-shadow:0 8px 28px rgba(16,185,129,.38);color:white;">
        <!-- Burbujas decorativas de fondo -->
        <div style="position:absolute;right:-28px;top:-28px;width:140px;height:140px;border-radius:50%;background:rgba(255,255,255,.07);pointer-events:none;"></div>
        <div style="position:absolute;right:60px;bottom:-38px;width:100px;height:100px;border-radius:50%;background:rgba(255,255,255,.05);pointer-events:none;"></div>
        <!-- Semicirculos que simulan el corte de un ticket -->
        <div style="position:absolute;left:86px;top:-15px;width:30px;height:30px;border-radius:50%;background:#f8fafc;pointer-events:none;"></div>
        <div style="position:absolute;left:86px;bottom:-15px;width:30px;height:30px;border-radius:50%;background:#f8fafc;pointer-events:none;"></div>
        <!-- Icono de etiqueta con label Alianza -->
        <div style="display:flex;flex-direction:column;align-items:center;gap:.3rem;min-width:70px;padding-right:1.25rem;border-right:2px dashed rgba(255,255,255,.35);flex-shrink:0;position:relative;z-index:1;">
          <svg width="38" height="38" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.5" aria-hidden="true">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <circle cx="7" cy="7" r="1.3" fill="white" stroke="none"/>
          </svg>
          <span style="font-size:.6rem;font-weight:800;text-transform:uppercase;letter-spacing:.6px;opacity:.9;">Alianza</span>
        </div>
        <!-- Porcentaje y descripcion -->
        <div style="flex:1;min-width:0;position:relative;z-index:1;">
          <div style="display:flex;align-items:baseline;gap:.5rem;flex-wrap:wrap;line-height:1.1;">
            <span style="font-size:2.75rem;font-weight:900;">{porcentajeDescuento}%</span>
            <span style="font-size:1.15rem;font-weight:700;opacity:.95;">de descuento especial</span>
          </div>
          <p style="margin:.3rem 0 0;font-size:.83rem;opacity:.85;">Precio preferencial por alianza · Se aplica automáticamente en tu reservación</p>
        </div>
        <!-- Checkmark decorativo a la derecha -->
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none" style="opacity:.18;flex-shrink:0;" aria-hidden="true">
          <circle cx="32" cy="32" r="29" stroke="white" stroke-width="2.5" stroke-dasharray="7 4"/>
          <path d="M19 32l9 10 17-19" stroke="white" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    {/if}

    <!-- Estado de carga inicial -->
    {#if loading}
      <div class="checkout-loading">
        <div class="checkout-loading__spinner"></div>
        <p>Cargando reservaciones pendientes...</p>
      </div>

    <!-- Error al intentar cargar las reservaciones -->
    {:else if loadError}
      <div class="checkout-empty">
        <div class="checkout-empty__icon">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        </div>
        <h2>Error al cargar</h2>
        <p>{loadError}</p>
        <button class="confirm-btn" on:click={loadPending}>Reintentar</button>
      </div>

    <!-- No hay reservaciones pendientes de pago -->
    {:else if reservations.length === 0}
      <div class="checkout-empty">
        <div class="checkout-empty__icon">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
        </div>
        <h2>No hay reservaciones pendientes</h2>
        <p>Realiza una reservación primero para poder pagar.</p>
        <button class="confirm-btn" on:click={() => navigateTo('home')}>Buscar hoteles</button>
      </div>

    <!-- Contenido principal: lista de reservaciones + formulario de pago + sidebar -->
    {:else}
      <div class="checkout__content">

        <!-- Columna izquierda: lista de reservaciones y formulario de tarjeta -->
        <div class="checkout__main">

          <!-- Lista de reservaciones pendientes de pago -->
          <section class="checkout-section">
            <h2 class="checkout-section__title">Reservaciones pendientes de pago</h2>
            <p class="checkout-section__sub">Reservaciones a pagar en este momento</p>

            <div class="reservations-list">
              {#each reservations as r}
                <div class="reservation-item checked">
                  <div class="reservation-item__body">
                    <div class="reservation-item__top">
                      <span class="reservation-item__code">{r.noReservacion}</span>
                      <span class="reservation-item__total">
                        {#if porcentajeDescuento}
                          {fmt(Math.round(r.total * (1 - porcentajeDescuento / 100) * 100) / 100)}
                        {:else}
                          {fmt(r.total)}
                        {/if}
                      </span>
                    </div>
                    <div class="reservation-item__meta">
                      {#if r.nombreHotel}
                        <span>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                          {r.nombreHotel}
                        </span>
                      {/if}
                      {#if r.habitaciones?.length}
                        <span>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                          {r.habitaciones.map(h => h.tipoHabitacion).join(' · ')}
                        </span>
                      {/if}
                      {#if r._checkIn}
                        <span>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                          {r._checkIn} → {r._checkOut}
                        </span>
                        <span>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                          {r._guests} huésped{r._guests !== 1 ? 'es' : ''} · {r._nights} noche{r._nights !== 1 ? 's' : ''}
                        </span>
                      {:else}
                        <span>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                          {fmtDate(r.fechaCheckIn)} → {fmtDate(r.fechaCheckOut)}
                        </span>
                      {/if}
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          </section>

          <!-- Formulario con los datos de la tarjeta de credito/debito -->
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

            <!-- Mensaje de error del proceso de pago -->
            {#if payError}
              <div class="checkout-error">{payError}</div>
            {/if}
          </section>

        </div>

        <!-- Sidebar lateral con resumen del total y boton de pagar -->
        <aside class="checkout__sidebar">
          <div class="order-summary">
            <h2 class="order-summary__title">Resumen</h2>

            {#if reservations.length === 0}
              <p class="order-empty-note">Cargando reservaciones...</p>
            {:else}
              <!-- Filas individuales por reservacion -->
              <div class="order-rows">
                {#each reservations as r}
                  <div class="order-row">
                    <span class="order-row__code">{r.noReservacion}</span>
                    <strong>{fmt(r.total)}</strong>
                  </div>
                {/each}
              </div>

              <div class="order-summary__divider"></div>

              <!-- Total con descuento de alianza si aplica -->
              {#if porcentajeDescuento}
                {@const totalConDescuento = Math.round(totalSelected * (1 - porcentajeDescuento / 100) * 100) / 100}
                {@const ahorrado = Math.round((totalSelected - totalConDescuento) * 100) / 100}
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:.4rem;">
                  <span style="font-size:.85rem;color:#64748b;">Precio original</span>
                  <span style="font-size:.95rem;color:#94a3b8;text-decoration:line-through;">{fmt(totalSelected)}</span>
                </div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:.75rem;background:rgba(16,185,129,.08);padding:.5rem .75rem;border-radius:8px;border:1px solid rgba(16,185,129,.2);">
                  <span style="font-size:.85rem;font-weight:700;color:#059669;">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="vertical-align:-1px;margin-right:3px"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><circle cx="7" cy="7" r="1.3" fill="currentColor" stroke="none"/></svg>
                    Descuento alianza {porcentajeDescuento}%
                  </span>
                  <span style="font-size:.95rem;font-weight:700;color:#059669;">-{fmt(ahorrado)}</span>
                </div>
              {/if}

              <!-- Total acumulado de todas las reservaciones -->
              <div class="order-summary__total">
                <span class="order-summary__total-label">{reservations.length} reservación{reservations.length !== 1 ? 'es' : ''}</span>
                <span class="order-summary__total-value">
                  {#if porcentajeDescuento}
                    {fmt(Math.round(totalSelected * (1 - porcentajeDescuento / 100) * 100) / 100)}
                  {:else}
                    {fmt(totalSelected)}
                  {/if}
                </span>
              </div>
            {/if}

            <!-- Boton principal de pago -->
            <button class="order-summary__btn-pay"
              on:click={handlePay}
              disabled={submitting || !anySelected}>
              {#if submitting}
                <span class="btn-spinner"></span>
                Procesando...
              {:else}
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
                Pagar {reservations.length > 0 ? fmt(porcentajeDescuento ? Math.round(totalSelected * (1 - porcentajeDescuento / 100) * 100) / 100 : totalSelected) : ''}
              {/if}
            </button>

            <!-- Indicadores de seguridad -->
            <div class="order-summary__security">
              <p class="security-badge">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="vertical-align:-1px"><polyline points="20 6 9 17 4 12"/></svg>
                Pago 100% seguro
              </p>
              <p class="security-note">Tus datos están protegidos con encriptación SSL</p>
            </div>
          </div>
        </aside>

      </div>
    {/if}

  </div>
</div>