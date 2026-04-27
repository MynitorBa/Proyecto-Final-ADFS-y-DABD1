<script>
// @ts-nocheck
/**
 * @file Confirmacion.svelte
 * @description Pagina de confirmacion post-pago mostrada tras un checkout exitoso.
 */

  import '../styles/confirmacion.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;
  export let reservaciones = [];
  export let facturas      = [];

  import { API } from '../lib/api.js';

  let usuarioId  = null;
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  onMount(() => {
    if (!usuarioId) { navigateTo('login'); return; }

    if (esIdaYVuelta()) {
      buscarHoteles();
    } else {
      cargarRecomendaciones();
    }

    return () => unsubscribe();
  });

  // ── Toasts ───────────────────────────────────────────────────────────────
  let toasts = [];

  function addToast(msg, tipo = 'success') {
    const id = Date.now();
    toasts = [...toasts, { id, msg, tipo }];
    setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 4000);
  }

  // ── Formato ──────────────────────────────────────────────────────────────
  function formatFecha(f) {
    if (!f) return '—';
    return new Date(f).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  function formatPrecio(p) {
    return `$ ${Number(p).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  // ── Comprobantes ─────────────────────────────────────────────────────────
  let descargando = {};
  let enviando    = {};

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

  async function enviarComprobantePorCorreo(reservacionId) {
    if (enviando[reservacionId]) return;
    enviando[reservacionId] = true;
    enviando = { ...enviando };
    try {
      const res = await fetch(`${API}/api/mis-reservaciones/${reservacionId}/enviar-comprobante`, {
        method: 'POST', credentials: 'include'
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

  function getBoletos(reservacionId) {
    const reserva = reservaciones.find(r => r.reservacionId === reservacionId);
    return reserva?.boletos ?? [];
  }

  // ── Deteccion de ida y vuelta ─────────────────────────────────────────────
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

  function obtenerTodosBoletos() {
    return reservaciones
      .flatMap(r => r.boletos ?? [])
      .sort((a, b) => a.boletoId - b.boletoId);
  }

  function esIdaYVuelta() {
    const boletos = obtenerTodosBoletos();
    if (boletos.length < 2) return false;
    return boletos[0].origenCodigo === boletos[boletos.length - 1].destinoCodigo;
  }

  function obtenerBoletoLlegadaIda() {
    const boletos = obtenerTodosBoletos();
    if (boletos.length < 2) return null;
    return boletos[Math.ceil(boletos.length / 2) - 1];
  }

  function obtenerBoletoVuelta() {
    const boletos = obtenerTodosBoletos();
    if (boletos.length < 2) return null;
    return boletos[boletos.length - 1];
  }

  // ── Hoteles aliados: ida y vuelta (busqueda completa + token) ─────────────
  let hoteles         = [];
  let hotelesCargando = false;

  async function buscarHoteles() {
    const boletoLlegada = obtenerBoletoLlegadaIda();
    const boletoVuelta  = obtenerBoletoVuelta();
    if (!boletoLlegada || !boletoVuelta) return;

    const ciudad   = boletoLlegada.destinoCiudad;
    const pais     = paisPorIATA[boletoLlegada.destinoCodigo] ?? ciudad;
    const checkIn  = new Date(boletoLlegada.fechaVuelo).toISOString().split('T')[0];
    const checkOut = new Date(boletoVuelta.fechaVuelo).toISOString().split('T')[0];
    const personas = reservaciones[0]?.boletos?.length ?? 1;

    hotelesCargando = true;
    try {
      const res = await fetch(`${API}/api/hoteles-aliados/busqueda`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ciudad, pais, fechaCheckIn: checkIn, fechaCheckOut: checkOut, cantidadPersonas: personas })
      });
      if (res.ok) hoteles = await res.json();
    } catch { }
    finally  { hotelesCargando = false; }
  }

  let tokenUsados   = new Set();
  let tokenCargando = {};

  async function irAlHotel(hotel) {
    if (tokenUsados.has(hotel.aliadoId) || tokenCargando[hotel.aliadoId]) return;

    tokenCargando[hotel.aliadoId] = true;
    tokenCargando = { ...tokenCargando };

    const boletoLlegada = obtenerBoletoLlegadaIda();
    const boletoVuelta  = obtenerBoletoVuelta();
    if (!boletoLlegada || !boletoVuelta) return;

    const pais        = paisPorIATA[boletoLlegada.destinoCodigo] ?? boletoLlegada.destinoCiudad ?? '';
    const fechaIda    = new Date(boletoLlegada.fechaVuelo).toISOString().split('T')[0];
    const fechaVuelta = new Date(boletoVuelta.fechaVuelo).toISOString().split('T')[0];

    try {
      const res = await fetch(`${API}/api/hoteles-aliados/${hotel.aliadoId}/token`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ciudad: boletoLlegada.destinoCiudad, pais, fechaIda, fechaVuelta })
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

  // ── Hoteles aliados: solo ida (recomendaciones simples con URL home) ───────
  let recomendaciones         = [];
  let recomendacionesCargando = false;

  async function cargarRecomendaciones() {
    recomendacionesCargando = true;
    try {
      const res = await fetch(`${API}/api/hoteles-aliados/recomendaciones`, {
        credentials: 'include'
      });
      if (res.ok) recomendaciones = await res.json();
    } catch { }
    finally  { recomendacionesCargando = false; }
  }
</script>

<!-- Pila de notificaciones toast -->
<div class="conf-toast-container">
  {#each toasts as t (t.id)}
    <div class="conf-toast conf-toast--{t.tipo}">
      {#if t.tipo === 'success'}
        <svg class="conf-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
      {:else}
        <svg class="conf-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
      {/if}
      <span>{t.msg}</span>
    </div>
  {/each}
</div>

<div class="confirmacion">
  <div class="confirmacion__container">

    <!-- Hero de exito -->
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

    <!-- ── Tarjetas de factura ─────────────────────────────────────────── -->
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
                          Vuelo {boleto.numeroVuelo} &middot; {boleto.clase} &middot;
                          {boleto.origenCodigo} &rarr; {boleto.destinoCodigo}
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
                  {descargando[factura.reservacionId] ? 'Descargando...' : 'Descargar PDF'}
                </button>
                <button class="btn-correo"
                  class:btn-correo--loading={enviando[factura.reservacionId]}
                  disabled={enviando[factura.reservacionId]}
                  on:click={() => enviarComprobantePorCorreo(factura.reservacionId)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  {enviando[factura.reservacionId] ? 'Enviando...' : 'Enviar al correo'}
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
                  {descargando[reserva.reservacionId] ? 'Descargando...' : 'Descargar PDF'}
                </button>
                <button class="btn-correo"
                  class:btn-correo--loading={enviando[reserva.reservacionId]}
                  disabled={enviando[reserva.reservacionId]}
                  on:click={() => enviarComprobantePorCorreo(reserva.reservacionId)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  {enviando[reserva.reservacionId] ? 'Enviando...' : 'Enviar al correo'}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}

    <!-- ══════════════════════════════════════════════════════════════════
         CASO A — Ida y vuelta: busqueda completa con token de descuento
         ══════════════════════════════════════════════════════════════════ -->
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
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                    <polyline points="22 4 12 14.01 9 11.01"/>
                  </svg>
                  Oferta aplicada
                {:else}
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                    <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
                    <polyline points="15 3 21 3 21 9"/>
                    <line x1="10" y1="14" x2="21" y2="3"/>
                  </svg>
                  Ver oferta con descuento
                {/if}
              </button>
            </div>
          {/each}
        </div>
      </section>
    {/if}

    <!-- ══════════════════════════════════════════════════════════════════
         CASO B — Solo ida: recomendaciones simples con link al home
         ══════════════════════════════════════════════════════════════════ -->
    {#if recomendacionesCargando}
      <div class="confirmacion__hoteles-loading">
        <div class="conf-spinner"></div>
        <p>Cargando hoteles recomendados...</p>
      </div>

    {:else if recomendaciones.length > 0}
      <section class="confirmacion__hoteles">
        <div class="confirmacion__hoteles-header">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          <div>
            <h2 class="confirmacion__hoteles-titulo">Hoteles recomendados</h2>
            <p class="confirmacion__hoteles-sub">Nuestros aliados de hospedaje — visita su sitio para ver disponibilidad</p>
          </div>
        </div>

        <div class="confirmacion__hoteles-grid">
          {#each recomendaciones as hotel}
            <div class="hotel-card">
              <h3 class="hotel-card__nombre">{hotel.nombre}</h3>
              <button
                class="hotel-card__btn hotel-card__btn--link"
                type="button"
                on:click={() => window.open(hotel.urlHomeAliado, '_blank', 'noopener,noreferrer')}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                  <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
                  <polyline points="15 3 21 3 21 9"/>
                  <line x1="10" y1="14" x2="21" y2="3"/>
                </svg>
                Visitar sitio
              </button>
            </div>
          {/each}
        </div>
      </section>
    {/if}

    <!-- Botones de navegacion -->
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