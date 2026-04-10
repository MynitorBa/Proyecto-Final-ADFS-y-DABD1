<script>
  // @ts-nocheck
/**
 * @file Confirmacion.svelte
 * @description Pagina de confirmacion post-pago mostrada tras un checkout exitoso. Recibe la
 * lista de reservaciones pagadas y sus objetos de factura correspondientes como props desde la
 * pagina de checkout. Muestra una seccion hero de exito seguida de tarjetas de factura con
 * detalles de facturacion, desglose por boleto y botones de accion para descargar o enviar por
 * correo el recibo PDF de cada reservacion. Tambien maneja el caso de reserva cuando facturas
 * esta vacio mostrando tarjetas de resumen de reservacion basicas. Despues de las facturas,
 * muestra una seccion promocional de hoteles aliados disponibles en la ciudad destino para la
 * noche posterior a la fecha del vuelo, resolviendo el pais destino desde el codigo IATA del
 * boleto mediante un mapa de busqueda local. Cada tarjeta de hotel expone un boton de
 * redireccion de un solo uso que solicita un token de descuento al backend y navega
 * inmediatamente al usuario al sitio del hotel aliado. Solo se puede generar un token por aliado
 * por sesion de compra para prevenir el abuso de tokens. Proporciona acciones de navegacion para
 * buscar mas vuelos o ver las reservaciones del usuario. Redirige a usuarios no autenticados a
 * la pagina de login al montar.
 */

  import '../styles/confirmacion.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  /** Funcion de navegacion proporcionada por el enrutador de la aplicacion para cambiar la pagina. @type {Function} */
  export let navigateTo;

  /** Arreglo de objetos de reservacion pasado desde la pagina de checkout tras el pago exitoso. @type {Array} */
  export let reservaciones = [];

  /** Arreglo de objetos de factura retornados por la API tras el pago, uno por reservacion. @type {Array} */
  export let facturas      = [];

  import { API } from '../lib/api.js';

  /** ID del usuario autenticado actualmente, leido del store de sesion. @type {number|null} */
  let usuarioId = null;

  /** Manejador de desuscripcion para la suscripcion al store de sesion. @type {Function} */
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  /**
   * Hook de ciclo de vida que se ejecuta tras el montaje del componente.
   * Redirige al login si no existe sesion de usuario, luego dispara la busqueda de hoteles
   * aliados en la ciudad destino del primer boleto.
   * Retorna la funcion de desuscripcion para limpieza del store de sesion.
   * @returns {Function}
   */
  onMount(() => {
    if (!usuarioId) { navigateTo('login'); return; }
    buscarHoteles();
    return () => unsubscribe();
  });

  /** Arreglo de objetos de notificacion toast activos, cada uno con id, msg y tipo. @type {Array} */
  let toasts = [];

  /**
   * Agrega una notificacion toast a la pila y la elimina automaticamente despues de 4 segundos.
   * @param {string} msg - El texto del mensaje a mostrar en el toast.
   * @param {string} [tipo='success'] - Estilo visual: 'success' o 'error'.
   */
  function addToast(msg, tipo = 'success') {
    const id = Date.now();
    toasts = [...toasts, { id, msg, tipo }];
    setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 4000);
  }

  /**
   * Formatea una cadena de fecha/hora en una fecha larga localizada con hora usando el locale es-GT.
   * Retorna un guion si el input es falsy.
   * @param {string|null} f - Cadena ISO de fecha y hora a formatear.
   * @returns {string} Cadena formateada como "15 de enero de 2025, 10:30" o "-".
   */
  function formatFecha(f) {
    if (!f) return '—';
    return new Date(f).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  /**
   * Formatea un precio numerico en una cadena USD con dos decimales.
   * @param {number} p - El valor del precio a formatear.
   * @returns {string} Cadena de precio formateada como "$ 1,250.00".
   */
  function formatPrecio(p) {
    return `$ ${Number(p).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  /** Mapa de reservacionId a booleano que rastrea que comprobantes se estan descargando actualmente. @type {object} */
  let descargando = {};

  /** Mapa de reservacionId a booleano que rastrea que comprobantes se estan enviando por correo actualmente. @type {object} */
  let enviando = {};

  /**
   * Descarga el recibo PDF de una reservacion especifica llamando al endpoint de comprobante.
   * Protege contra llamadas concurrentes usando el mapa descargando. Abre el comprobante en una nueva
   * pestana del navegador y muestra un toast de exito o error segun el resultado.
   * @async
   * @param {number} reservacionId - El ID de la reservacion cuyo recibo descargar.
   * @param {string} noReservacion - El numero de reservacion legible usado como nombre de archivo.
   * @returns {Promise<void>}
   */
  async function descargarComprobante(reservacionId, noReservacion) {
    if (descargando[reservacionId]) return;
    descargando[reservacionId] = true;
    descargando = { ...descargando };

    try {
      window.open(`${API}/api/mis-reservaciones/${reservacionId}/comprobante`, '_blank');
      addToast('Comprobante abierto en nueva pestana');
    } catch {
      addToast('No se pudo abrir el comprobante', 'error');
    } finally {
      descargando[reservacionId] = false;
      descargando = { ...descargando };
    }
  }

  /**
   * Envia el recibo PDF de una reservacion especifica al correo registrado del usuario llamando
   * al endpoint enviar-comprobante. Protege contra llamadas concurrentes usando el mapa enviando.
   * Muestra un toast de exito en 200 OK, o un toast de error con el mensaje de la API ante fallo.
   * @async
   * @param {number} reservacionId - El ID de la reservacion cuyo recibo enviar por correo.
   * @returns {Promise<void>}
   */
  async function enviarComprobantePorCorreo(reservacionId) {
    if (enviando[reservacionId]) return;
    enviando[reservacionId] = true;
    enviando = { ...enviando };

    try {
      const res = await fetch(`${API}/api/mis-reservaciones/${reservacionId}/enviar-comprobante`, {
        method: 'POST',
        credentials: 'include'
      });
      if (res.ok) {
        addToast('Comprobante enviado a tu correo');
      } else {
        const body = await res.json().catch(() => ({}));
        addToast(body.message || 'No se pudo enviar el comprobante', 'error');
      }
    } catch {
      addToast('Error de conexion', 'error');
    } finally {
      enviando[reservacionId] = false;
      enviando = { ...enviando };
    }
  }

  /**
   * Busca una reservacion en el arreglo prop reservaciones por su ID y retorna su arreglo de boletos.
   * Retorna un arreglo vacio si no se encuentra ninguna reservacion coincidente.
   * @param {number} reservacionId - El ID de la reservacion a buscar.
   * @returns {Array} El arreglo de boletos de la reservacion coincidente, o un arreglo vacio.
   */
  function getBoletos(reservacionId) {
    const reserva = reservaciones.find(r => r.reservacionId === reservacionId);
    return reserva?.boletos ?? [];
  }

  /** Arreglo de objetos de hotel aliado retornados por el endpoint de busqueda de hoteles. @type {Array} */
  let hoteles = [];

  /** Verdadero mientras la solicitud POST de busqueda de hoteles aliados esta en progreso. @type {boolean} */
  let hotelesCargando = false;

  /**
   * Mapa de busqueda de codigo IATA de aeropuerto a nombre de pais, usado para resolver el campo
   * Pais requerido por los endpoints de busqueda de hoteles y de token. Usa destinoCiudad como
   * respaldo si el codigo no esta en el mapa. Ampliar este mapa para que coincida con las rutas
   * manejadas por la aerolinea.
   * @type {Record<string, string>}
   */
  const paisPorIATA = {
    GUA: 'Guatemala',        FRS: 'Guatemala',        HUG: 'Guatemala',
    MEX: 'Mexico',           CUN: 'Mexico',           GDL: 'Mexico',           MTY: 'Mexico',
    LAX: 'United States',    JFK: 'United States',    MIA: 'United States',
    ORD: 'United States',    SFO: 'United States',    DFW: 'United States',
    BOG: 'Colombia',         MDE: 'Colombia',
    LIM: 'Peru',
    SCL: 'Chile',
    EZE: 'Argentina',        AEP: 'Argentina',
    GRU: 'Brazil',           GIG: 'Brazil',
    MAD: 'Spain',            BCN: 'Spain',
    CDG: 'France',
    LHR: 'United Kingdom',
    FCO: 'Italy',
    AMS: 'Netherlands',
    FRA: 'Germany',
    SVO: 'Russia',           DME: 'Russia',
    NRT: 'Japan',            HND: 'Japan',
    PEK: 'China',            PVG: 'China',
    DXB: 'United Arab Emirates',
    SYD: 'Australia',
  };

  /**
   * Busca hoteles aliados disponibles en la ciudad destino del primer boleto. Resuelve el campo
   * Pais requerido desde el codigo IATA destino usando paisPorIATA, usando destinoCiudad como
   * respaldo si el codigo no esta mapeado. Usa la fecha del vuelo mas un dia como check-in y
   * el dia siguiente como check-out, cubriendo una noche promocional. La cantidad de personas
   * coincide con el total de boletos de la primera reservacion. Ignora errores silenciosamente
   * ya que la seccion es promocional y no critica para el flujo.
   * @async
   * @returns {Promise<void>}
   */
  async function buscarHoteles() {
    if (!reservaciones.length) return;
    const primerBoleto = reservaciones[0]?.boletos?.[0];
    if (!primerBoleto) return;

    const fechaVuelo = new Date(primerBoleto.fechaVuelo);
    const checkIn    = new Date(fechaVuelo); checkIn.setDate(checkIn.getDate() + 1);
    const checkOut   = new Date(fechaVuelo); checkOut.setDate(checkOut.getDate() + 2);
    const fmt        = d => d.toISOString().split('T')[0];
    const pais       = paisPorIATA[primerBoleto.destinoCodigo] ?? primerBoleto.destinoCiudad;

    hotelesCargando = true;
    try {
      const res = await fetch(`${API}/api/hoteles-aliados/busqueda`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ciudad:           primerBoleto.destinoCiudad,
          pais:             pais,
          fechaCheckIn:     fmt(checkIn),
          fechaCheckOut:    fmt(checkOut),
          cantidadPersonas: reservaciones[0].boletos.length
        })
      });
      if (res.ok) hoteles = await res.json();
    } catch { /* seccion promocional, error silencioso */ }
    finally { hotelesCargando = false; }
  }

  /**
   * Conjunto de valores aliadoId ya usados en esta sesion de confirmacion para prevenir la
   * generacion duplicada de tokens para el mismo aliado dentro de un solo flujo de compra.
   * @type {Set<number>}
   */
  let tokenUsados = new Set();

  /** Mapa de aliadoId a booleano que rastrea que solicitudes de token estan actualmente en progreso. @type {object} */
  let tokenCargando = {};

  /**
   * Solicita un token de redireccion de un solo uso al endpoint del hotel aliado y navega
   * inmediatamente al usuario al sitio del hotel con el descuento aplicado. Protege contra
   * llamadas duplicadas por aliado dentro de la misma sesion de confirmacion usando tokenUsados.
   * Solo se puede generar un token por aliado por compra para prevenir el abuso de tokens.
   * @async
   * @param {object} hotel - El objeto de hotel aliado que contiene aliadoId y datos del destino.
   * @returns {Promise<void>}
   */
  async function irAlHotel(hotel) {
    if (tokenUsados.has(hotel.aliadoId) || tokenCargando[hotel.aliadoId]) return;

    tokenCargando[hotel.aliadoId] = true;
    tokenCargando = { ...tokenCargando };

    const primerBoleto = reservaciones[0]?.boletos?.[0];
    const pais         = paisPorIATA[primerBoleto?.destinoCodigo] ?? primerBoleto?.destinoCiudad ?? '';

    try {
      const res = await fetch(`${API}/api/hoteles-aliados/${hotel.aliadoId}/token`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ciudad: primerBoleto?.destinoCiudad ?? '',
          pais:   pais
        })
      });

      if (res.ok) {
        const data = await res.json();
        tokenUsados = new Set([...tokenUsados, hotel.aliadoId]);
        window.location.href = data.urlRedireccion;
      } else {
        addToast('No se pudo generar el enlace al hotel', 'error');
      }
    } catch {
      addToast('Error de conexion con el hotel', 'error');
    } finally {
      tokenCargando[hotel.aliadoId] = false;
      tokenCargando = { ...tokenCargando };
    }
  }
</script>

<!-- Pila de notificaciones toast para confirmar descarga o envio de comprobante -->
<div class="conf-toast-container">
  {#each toasts as t (t.id)}
    <div class="conf-toast conf-toast--{t.tipo}">
      {#if t.tipo === 'success'}
        <svg class="conf-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
      {:else}
        <svg class="conf-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      {/if}
      <span>{t.msg}</span>
    </div>
  {/each}
</div>

<div class="confirmacion">
  <div class="confirmacion__container">

    <!-- Hero de exito con icono de confirmacion y mensaje de compra realizada -->
    <div class="confirmacion__hero">
      <div class="confirmacion__icono-wrap">
        <div class="confirmacion__icono">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="40" height="40">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
      </div>
      <h1 class="confirmacion__titulo">Compra realizada con exito</h1>
      <p class="confirmacion__subtitulo">
        Tu reservacion ha sido confirmada. Descarga tu comprobante a continuacion
        o consultalo en cualquier momento desde tus reservaciones.
      </p>
      <div class="confirmacion__linea-deco"></div>
    </div>

    <!-- Tarjetas de factura con detalle de boletos y acciones de descarga o envio por correo -->
    {#if facturas.length > 0}
      <div class="confirmacion__facturas">
        {#each facturas as factura}
          {@const boletos = getBoletos(factura.reservacionId)}
          <div class="factura-card">

            <div class="factura-card__header">
              <div class="factura-card__header-left">
                <span class="factura-card__etiqueta">Comprobante de pago</span>
                <span class="factura-card__nro">{factura.noReservacion}</span>
              </div>
              <div class="factura-card__header-right">
                <span class="factura-card__estado">Confirmada</span>
              </div>
            </div>

            <div class="factura-card__body">
              <div class="factura-grid">
                <div class="factura-dato">
                  <span class="factura-dato__label">Factura ID</span>
                  <span class="factura-dato__valor">FAC-{String(factura.facturaId).padStart(6,'0')}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">No. Reservacion</span>
                  <span class="factura-dato__valor">{factura.noReservacion}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Fecha de pago</span>
                  <span class="factura-dato__valor">{formatFecha(factura.fecha)}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">NIT</span>
                  <span class="factura-dato__valor">{factura.nit ?? 'CF'}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Codigo postal</span>
                  <span class="factura-dato__valor">{factura.codigoPostal ?? '—'}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Total pagado</span>
                  <span class="factura-dato__valor factura-dato__valor--gold">{formatPrecio(factura.total)}</span>
                </div>
              </div>

              {#if boletos.length > 0}
                <div class="factura-card__separador"></div>
                <h4 class="factura-card__subtitulo">Detalle de boletos</h4>
                <div class="factura-boletos">
                  {#each boletos as boleto}
                    <div class="factura-boleto">
                      <div class="factura-boleto__col">
                        <span class="factura-boleto__asiento">Asiento {boleto.noAsiento}</span>
                        {#if boleto.pasajero}
                          <span class="factura-boleto__pasajero">
                            {boleto.pasajero.nombre} {boleto.pasajero.apellido}
                          </span>
                        {/if}
                        <span class="factura-boleto__vuelo">
                          Vuelo {boleto.numeroVuelo} &middot; {boleto.clase} &middot; {boleto.origenCodigo} &rarr; {boleto.destinoCodigo}
                        </span>
                      </div>
                      <span class="factura-boleto__precio">{formatPrecio(boleto.precio)}</span>
                    </div>
                  {/each}
                </div>
              {/if}

              <div class="factura-card__footer">
                <button class="btn-pdf"
                  class:btn-pdf--loading={descargando[factura.reservacionId]}
                  disabled={descargando[factura.reservacionId]}
                  on:click={() => descargarComprobante(factura.reservacionId, factura.noReservacion)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  {descargando[factura.reservacionId] ? "Descargando..." : "Descargar PDF"}
                </button>
                <button class="btn-correo"
                  class:btn-correo--loading={enviando[factura.reservacionId]}
                  disabled={enviando[factura.reservacionId]}
                  on:click={() => enviarComprobantePorCorreo(factura.reservacionId)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  {enviando[factura.reservacionId] ? "Enviando..." : "Enviar al correo"}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>

    {:else}
      <div class="confirmacion__facturas">
        {#each reservaciones as reserva}
          <div class="factura-card">
            <div class="factura-card__header">
              <div class="factura-card__header-left">
                <span class="factura-card__etiqueta">Reservacion confirmada</span>
                <span class="factura-card__nro">{reserva.noReservacion}</span>
              </div>
              <span class="factura-card__estado">Confirmada</span>
            </div>
            <div class="factura-card__body">
              <div class="factura-grid">
                <div class="factura-dato">
                  <span class="factura-dato__label">No. Reservacion</span>
                  <span class="factura-dato__valor">{reserva.noReservacion}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Total</span>
                  <span class="factura-dato__valor factura-dato__valor--gold">{formatPrecio(reserva.total)}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Boletos</span>
                  <span class="factura-dato__valor">{reserva.boletos?.length ?? 0}</span>
                </div>
              </div>
              <div class="factura-card__footer">
                <button class="btn-pdf"
                  class:btn-pdf--loading={descargando[reserva.reservacionId]}
                  disabled={descargando[reserva.reservacionId]}
                  on:click={() => descargarComprobante(reserva.reservacionId, reserva.noReservacion)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  {descargando[reserva.reservacionId] ? "Descargando..." : "Descargar PDF"}
                </button>
                <button class="btn-correo"
                  class:btn-correo--loading={enviando[reserva.reservacionId]}
                  disabled={enviando[reserva.reservacionId]}
                  on:click={() => enviarComprobantePorCorreo(reserva.reservacionId)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  {enviando[reserva.reservacionId] ? "Enviando..." : "Enviar al correo"}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}

    <!-- Hoteles aliados disponibles en la ciudad destino para la noche posterior al vuelo -->
    {#if hotelesCargando}
      <div class="confirmacion__hoteles-loading">
        <div class="conf-spinner"></div>
        <p>Buscando hoteles disponibles en tu destino...</p>
      </div>
    {:else if hoteles.length > 0}
      <section class="confirmacion__hoteles">
        <div class="confirmacion__hoteles-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          <div>
            <h2 class="confirmacion__hoteles-titulo">Hoteles aliados en tu destino</h2>
            <p class="confirmacion__hoteles-sub">Hospedaje disponible para tu llegada — oferta de una noche</p>
          </div>
        </div>

        <!-- Grid de tarjetas de hotel con nombre, aliado, rating, descripcion, direccion y boton de redireccion con token -->
        <div class="confirmacion__hoteles-grid">
          {#each hoteles as hotel}
            <div class="hotel-card">
              <div class="hotel-card__top">
                <span class="hotel-card__aliado">{hotel.aliadoNombre}</span>
                <div class="hotel-card__rating">
                  <svg viewBox="0 0 24 24" fill="#D4A056" stroke="none" width="14" height="14">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  <span>{hotel.rating}</span>
                </div>
              </div>
              <h3 class="hotel-card__nombre">{hotel.nombre}</h3>
              <p class="hotel-card__desc">{hotel.descripcion}</p>
              <div class="hotel-card__direccion">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                <span>{hotel.direccion}</span>
              </div>
              <button
                class="hotel-card__btn"
                class:hotel-card__btn--usado={tokenUsados.has(hotel.aliadoId)}
                class:hotel-card__btn--cargando={tokenCargando[hotel.aliadoId]}
                disabled={tokenUsados.has(hotel.aliadoId) || tokenCargando[hotel.aliadoId]}
                on:click={() => irAlHotel(hotel)}
                type="button">
                {#if tokenCargando[hotel.aliadoId]}
                  <span class="conf-spinner conf-spinner--sm"></span> Generando enlace...
                {:else if tokenUsados.has(hotel.aliadoId)}
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                  Oferta aplicada
                {:else}
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
                  Ver oferta con descuento
                {/if}
              </button>
            </div>
          {/each}
        </div>
      </section>
    {/if}

    <!-- Botones de navegacion para buscar mas vuelos o ver reservaciones -->
    <div class="confirmacion__acciones">
      <h2 class="confirmacion__acciones-titulo">Que deseas hacer ahora?</h2>
      <div class="confirmacion__btns">
        <button class="btn-primary" on:click={() => navigateTo('home')}>
          Buscar mas vuelos
        </button>
        <button class="btn-secondary" on:click={() => navigateTo('reservas')}>
          Ver mis reservaciones
        </button>
      </div>
    </div>

  </div>
</div>