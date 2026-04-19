<script>
// @ts-nocheck
/**
 * @file AdminReservas.svelte
 * @description Modulo de gestion de reservaciones agrupadas por vuelo.
 * Vista 1: cuadricula de tarjetas de vuelo con estadisticas de reservaciones.
 * Vista 2: tabla de reservaciones del vuelo seleccionado con modal de detalle y cancelacion.
 */

  export let API = '';
  export let mostrarToast   = (tipo, msg) => {};
  export let mostrarConfirm = async (msg, sub, tipo) => false;

  import { onMount } from 'svelte';

  // ── Estado global ─────────────────────────────────────────────────────────────
  let vista       = 'vuelos';   // 'vuelos' | 'reservaciones'
  let vuelos      = [];
  let reservas    = [];
  let vueloActual = null;

  let loadingVuelos   = true;
  let loadingReservas = false;
  let errorVuelos     = '';
  let errorReservas   = '';

  let busquedaVuelo   = '';
  let busquedaReserva = '';
  let filtroEstado    = 'todas';

  // Modal detalle
  let reservaDetalle  = null;
  let detalleLoading  = false;
  let detalleError    = '';
  let cancelarAbierto = false;
  let cancelMotivo    = '';
  let cancelLoading   = false;
  let cancelError     = '';

  // ── Reactivos ─────────────────────────────────────────────────────────────────
  $: vuelosFiltrados = vuelos.filter(v => {
    const q = busquedaVuelo.trim().toLowerCase();
    return !q
      || v.numeroVuelo?.toLowerCase().includes(q)
      || v.origenCodigo?.toLowerCase().includes(q)
      || v.destinoCodigo?.toLowerCase().includes(q)
      || v.origenCiudad?.toLowerCase().includes(q)
      || v.destinoCiudad?.toLowerCase().includes(q);
  });

  $: reservasFiltradas = reservas.filter(r => {
    const matchEstado = filtroEstado === 'todas' || r.estadoReserva?.toLowerCase() === filtroEstado;
    const q = busquedaReserva.trim().toLowerCase();
    const matchBusq = !q
      || r.noReservacion?.toLowerCase().includes(q)
      || r.usuarioNombre?.toLowerCase().includes(q)
      || r.usuarioEmail?.toLowerCase().includes(q);
    return matchEstado && matchBusq;
  });

  const filtros = [
    { key:'todas',      label:'Todas'       },
    { key:'confirmada', label:'Confirmadas' },
    { key:'pendiente',  label:'Pendientes'  },
    { key:'cancelada',  label:'Canceladas'  },
    { key:'completada', label:'Completadas' },
  ];

  // ── Lifecycle ─────────────────────────────────────────────────────────────────
  onMount(cargarVuelos);

  // ── Helpers ───────────────────────────────────────────────────────────────────
  function formatFecha(f) {
    if (!f) return '--';
    return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' });
  }
  function formatHora(h) {
    if (!h) return '--';
    return h.substring(0, 5);
  }
  function formatDuracion(min) {
    if (!min) return '--';
    const h = Math.floor(min / 60), m = min % 60;
    return `${h}h${m > 0 ? ' ' + m + 'm' : ''}`;
  }
  function formatFechaHora(f) {
    if (!f) return '--';
    const d = new Date(f);
    return d.toLocaleDateString('es-GT',{ day:'2-digit', month:'short', year:'numeric' })
      + ' ' + d.toLocaleTimeString('es-GT',{ hour:'2-digit', minute:'2-digit' });
  }
  function estadoClase(e) {
    if (!e) return 'ar-badge--pendiente';
    const s = e.toLowerCase();
    if (s === 'confirmada') return 'ar-badge--confirmada';
    if (s === 'cancelada')  return 'ar-badge--cancelada';
    if (s === 'completada') return 'ar-badge--completada';
    if (s === 'expirada')   return 'ar-badge--expirada';
    return 'ar-badge--pendiente';
  }

  // ── Carga ─────────────────────────────────────────────────────────────────────
  async function cargarVuelos() {
    loadingVuelos = true; errorVuelos = '';
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/vuelos`, { credentials:'include' });
      if (r.ok) vuelos = await r.json();
      else errorVuelos = `Error ${r.status}: No se pudieron cargar los vuelos.`;
    } catch { errorVuelos = 'Error de conexion.'; }
    finally   { loadingVuelos = false; }
  }

  async function entrarVuelo(vuelo) {
    vueloActual      = vuelo;
    vista            = 'reservaciones';
    reservas         = [];
    filtroEstado     = 'todas';
    busquedaReserva  = '';
    loadingReservas  = true; errorReservas = '';
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/vuelo/${vuelo.vueloId}`, { credentials:'include' });
      if (r.ok) reservas = await r.json();
      else errorReservas = `Error ${r.status}: No se pudieron cargar las reservaciones.`;
    } catch { errorReservas = 'Error de conexion.'; }
    finally   { loadingReservas = false; }
  }

  function volverAVuelos() {
    vista = 'vuelos'; vueloActual = null; reservaDetalle = null;
    cargarVuelos();
  }

  async function abrirDetalle(reserva) {
    detalleLoading = true; detalleError = '';
    reservaDetalle = reserva;
    cancelarAbierto = false; cancelMotivo = ''; cancelError = '';
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/${reserva.reservacionId}`, { credentials:'include' });
      if (r.ok) reservaDetalle = await r.json();
      else detalleError = `Error ${r.status}: No se pudo cargar el detalle.`;
    } catch { detalleError = 'Error de conexion.'; }
    finally   { detalleLoading = false; }
  }
  function cerrarDetalle() {
    reservaDetalle = null; detalleError = '';
    cancelarAbierto = false; cancelMotivo = ''; cancelError = '';
  }

  async function confirmarCancelar() {
    if (!cancelMotivo.trim()) { cancelError = 'Escribe un motivo de cancelacion.'; return; }
    const ok = await mostrarConfirm(
      '¿Cancelar esta reservacion?',
      `Se notificará a "${reservaDetalle.usuarioNombre}" y los asientos serán liberados en el vuelo.`,
      'danger'
    );
    if (!ok) return;
    cancelLoading = true; cancelError = '';
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/${reservaDetalle.reservacionId}/cancelar`, {
        method:'POST', credentials:'include',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({ motivo: cancelMotivo.trim() })
      });
      if (r.ok) {
        mostrarToast('success', `Reservacion ${reservaDetalle.noReservacion} cancelada.`);
        cancelarAbierto = false;
        const rv = await fetch(`${API}/api/admin/reservaciones/vuelo/${vueloActual.vueloId}`,{ credentials:'include' });
        if (rv.ok) reservas = await rv.json();
        const rd = await fetch(`${API}/api/admin/reservaciones/${reservaDetalle.reservacionId}`,{ credentials:'include' });
        if (rd.ok) reservaDetalle = await rd.json();
      } else {
        const body = await r.json().catch(() => ({}));
        cancelError = body.message || 'No se pudo cancelar.';
      }
    } catch { cancelError = 'Error de conexion.'; }
    finally   { cancelLoading = false; }
  }
</script>

<!-- ═══════════════ MODAL DETALLE ═══════════════ -->
{#if reservaDetalle}
  <div class="ar-overlay" on:click={cerrarDetalle} role="dialog" aria-modal="true">
    <div class="ar-modal" on:click|stopPropagation>

      <button class="ar-modal__close" on:click={cerrarDetalle} aria-label="Cerrar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="18" height="18">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>

      {#if detalleLoading}
        <div class="ar-modal__center"><div class="ar-spinner ar-spinner--lg"></div><p>Cargando detalle...</p></div>
      {:else if detalleError}
        <div class="ar-modal__center" style="color:#c0392b"><p>{detalleError}</p></div>
      {:else}
        <div class="ar-modal__head">
          <div class="ar-modal__head-left">
            <span class="ar-modal__reserva-num">{reservaDetalle.noReservacion}</span>
            <span class="ar-badge {estadoClase(reservaDetalle.estadoReserva)}">{reservaDetalle.estadoReserva}</span>
          </div>
          <span class="ar-modal__total">${reservaDetalle.total?.toFixed(2)}</span>
        </div>

        <div class="ar-modal__meta">
          <div class="ar-meta-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            {reservaDetalle.usuarioNombre}
          </div>
          {#if reservaDetalle.usuarioEmail}
            <div class="ar-meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              {reservaDetalle.usuarioEmail}
            </div>
          {/if}
          <div class="ar-meta-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            Creada: {formatFechaHora(reservaDetalle.fechaCreacion)}
          </div>
          {#if reservaDetalle.fechaCancelacion}
            <div class="ar-meta-item ar-meta-item--danger">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
              Cancelada: {formatFechaHora(reservaDetalle.fechaCancelacion)}
              {#if reservaDetalle.motivoCancelacion} · {reservaDetalle.motivoCancelacion}{/if}
            </div>
          {/if}
        </div>

        <div class="ar-section-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="var(--primary-color)" stroke-width="2" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
          Boletos ({reservaDetalle.boletos?.length ?? 0})
        </div>

        <div class="ar-boletos">
          {#each reservaDetalle.boletos ?? [] as boleto}
            <div class="ar-boleto">
              <div class="ar-boleto__header">
                <div class="ar-boleto__flight">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <strong>{boleto.numeroVuelo}</strong>
                  {#if boleto.avionMarca}<span class="ar-boleto__avion">{boleto.avionMarca} {boleto.avionModelo}</span>{/if}
                </div>
                <span class="ar-badge ar-badge--sm {estadoClase(boleto.estadoBoleto)}">{boleto.estadoBoleto}</span>
              </div>
              <div class="ar-boleto__ruta">
                <div class="ar-boleto__punto">
                  <span class="ar-boleto__code">{boleto.origenCodigo}</span>
                  <span class="ar-boleto__city">{boleto.origenCiudad}</span>
                  <span class="ar-boleto__hour">{formatHora(boleto.horaSalida)}</span>
                </div>
                <div class="ar-boleto__linea">
                  <div class="ar-boleto__track"></div>
                  <svg viewBox="0 0 24 24" fill="var(--primary-color)" stroke="none" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <span class="ar-boleto__dur">{formatDuracion(boleto.duracionMinutos)}</span>
                </div>
                <div class="ar-boleto__punto ar-boleto__punto--right">
                  <span class="ar-boleto__code">{boleto.destinoCodigo}</span>
                  <span class="ar-boleto__city">{boleto.destinoCiudad}</span>
                </div>
              </div>
              <div class="ar-boleto__grid">
                <div class="ar-boleto__cell"><span class="ar-boleto__label">Asiento</span><span class="ar-boleto__val">{boleto.noAsiento||'--'}</span></div>
                <div class="ar-boleto__cell"><span class="ar-boleto__label">Clase</span><span class="ar-boleto__val">{boleto.clase||'--'}</span></div>
                <div class="ar-boleto__cell"><span class="ar-boleto__label">Fecha vuelo</span><span class="ar-boleto__val">{formatFecha(boleto.fechaVuelo)}</span></div>
                <div class="ar-boleto__cell"><span class="ar-boleto__label">Precio</span><span class="ar-boleto__val ar-boleto__val--price">${boleto.precio?.toFixed(2)}</span></div>
                <div class="ar-boleto__cell ar-boleto__cell--wide"><span class="ar-boleto__label">No. Boleto</span><span class="ar-boleto__val ar-boleto__val--mono">{boleto.noBoleto}</span></div>
              </div>
              {#if boleto.pasajero}
                <div class="ar-boleto__pasajero">
                  <svg viewBox="0 0 24 24" fill="none" stroke="var(--primary-color)" stroke-width="2" width="14" height="14"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <div>
                    <span class="ar-boleto__pasajero-name">{boleto.pasajero.nombre} {boleto.pasajero.apellido}</span>
                    <span class="ar-boleto__pasajero-info">Pasaporte: {boleto.pasajero.pasaporte} · Tel: {boleto.pasajero.telefono} · {boleto.pasajero.ciudad}</span>
                  </div>
                </div>
              {/if}
            </div>
          {/each}
        </div>

        {#if reservaDetalle.estadoReserva?.toLowerCase()==='confirmada' || reservaDetalle.estadoReserva?.toLowerCase()==='pendiente'}
          {#if !cancelarAbierto}
            <div class="ar-cancel-trigger">
              <button class="ar-btn ar-btn--danger-outline" on:click={() => cancelarAbierto=true} type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                Cancelar reservacion (Admin)
              </button>
            </div>
          {:else}
            <div class="ar-cancel-panel">
              <div class="ar-cancel-panel__header">
                <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2.5" width="22" height="22"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                <div>
                  <h4 class="ar-cancel-panel__title">Cancelar como Administrador</h4>
                  <p class="ar-cancel-panel__sub">Se notificará a <strong>{reservaDetalle.usuarioNombre}</strong> y se devolverá disponibilidad al vuelo.</p>
                </div>
              </div>
              <label class="ar-cancel-panel__label">Motivo de cancelacion (obligatorio)</label>
              <textarea class="ar-cancel-panel__textarea" bind:value={cancelMotivo} rows="3" placeholder="Ej: Cancelacion administrativa, irregularidad detectada..."></textarea>
              {#if cancelError}<p class="ar-form-error">{cancelError}</p>{/if}
              <div class="ar-cancel-panel__actions">
                <button class="ar-btn ar-btn--ghost" on:click={() => {cancelarAbierto=false;cancelMotivo='';cancelError='';}} disabled={cancelLoading} type="button">Volver</button>
                <button class="ar-btn ar-btn--danger" on:click={confirmarCancelar} disabled={cancelLoading} type="button">
                  {#if cancelLoading}<span class="ar-spinner ar-spinner--sm"></span> Cancelando...{:else}Confirmar cancelacion{/if}
                </button>
              </div>
            </div>
          {/if}
        {:else if reservaDetalle.estadoReserva?.toLowerCase()==='cancelada'}
          <div class="ar-ya-cancelada">
            <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            Esta reservacion ya fue cancelada.
          </div>
        {/if}

        <div class="ar-modal__footer">
          <button class="ar-btn ar-btn--ghost" on:click={cerrarDetalle} type="button">Cerrar</button>
        </div>
      {/if}
    </div>
  </div>
{/if}

<!-- ═══════════════ CONTENIDO PRINCIPAL ═══════════════ -->
<section class="admin-section ar-section">

  <!-- ───────────── VISTA 1: VUELOS ───────────── -->
  {#if vista === 'vuelos'}

    <div class="section-header">
      <div>
        <h2 class="admin-section__title">Gestión de Reservaciones</h2>
        <p class="admin-section__subtitle">
          Reservaciones agrupadas por vuelo. Selecciona un vuelo para ver y gestionar sus reservaciones individuales.
        </p>
      </div>
      <button class="btn-add" on:click={cargarVuelos}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
        Recargar
      </button>
    </div>

    <div class="ar-search" style="margin-bottom:1.5rem;max-width:480px">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15" class="ar-search__icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input class="ar-search__input" type="text" placeholder="Buscar por número de vuelo, origen o destino..." bind:value={busquedaVuelo}/>
      {#if busquedaVuelo}<button class="ar-search__clear" on:click={() => busquedaVuelo=''}>×</button>{/if}
    </div>

    {#if loadingVuelos}
      <div class="ar-empty"><div class="ar-spinner ar-spinner--lg"></div><p>Cargando vuelos...</p></div>
    {:else if errorVuelos}
      <div class="ar-empty ar-empty--error">
        <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        <p>{errorVuelos}</p>
        <button class="btn-add" on:click={cargarVuelos}>Reintentar</button>
      </div>
    {:else if vuelosFiltrados.length === 0}
      <div class="ar-empty">
        <svg viewBox="0 0 24 24" fill="none" stroke="#B89A7A" stroke-width="1.5" width="52" height="52"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
        <p>No hay vuelos con reservaciones{busquedaVuelo ? ' que coincidan' : ''}.</p>
      </div>
    {:else}
      <p class="ar-result-count">{vuelosFiltrados.length} vuelo{vuelosFiltrados.length !== 1 ? 's' : ''} con reservaciones</p>
      <div class="arv-grid">
        {#each vuelosFiltrados as vuelo (vuelo.vueloId)}
          <article class="arv-card" on:click={() => entrarVuelo(vuelo)} role="button" tabindex="0"
            on:keydown={e => e.key === 'Enter' && entrarVuelo(vuelo)}>

            <!-- Cabecera -->
            <div class="arv-card__header">
              <div class="arv-card__vuelo-id">
                <div class="arv-card__icon-wrap">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" width="18" height="18">
                    <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                  </svg>
                </div>
                <span class="arv-card__numero">{vuelo.numeroVuelo}</span>
              </div>
              <div class="arv-card__fecha-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                <span class="arv-card__fecha">{formatFecha(vuelo.fechaVuelo)}</span>
              </div>
            </div>

            <!-- Ruta visual -->
            <div class="arv-card__ruta">
              <div class="arv-card__aeropuerto">
                <span class="arv-card__iata">{vuelo.origenCodigo}</span>
                <span class="arv-card__ciudad">{vuelo.origenCiudad}</span>
                {#if vuelo.horaSalida}
                  <span class="arv-card__hora">{formatHora(vuelo.horaSalida)}</span>
                {/if}
              </div>

              <div class="arv-card__linea-wrap">
                <div class="arv-card__linea-track"></div>
                <svg viewBox="0 0 24 24" fill="var(--primary-color)" stroke="none" width="20" height="20" class="arv-card__linea-plane">
                  <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>
                </svg>
                {#if vuelo.duracionEstimada > 0}
                  <span class="arv-card__duracion">{formatDuracion(vuelo.duracionEstimada)}</span>
                {/if}
              </div>

              <div class="arv-card__aeropuerto arv-card__aeropuerto--right">
                <span class="arv-card__iata">{vuelo.destinoCodigo}</span>
                <span class="arv-card__ciudad">{vuelo.destinoCiudad}</span>
                {#if vuelo.horaLlegada}
                  <span class="arv-card__hora">{formatHora(vuelo.horaLlegada)}</span>
                {/if}
              </div>
            </div>

            <!-- Estadísticas -->
            <div class="arv-card__stats">
              <div class="arv-stat arv-stat--total">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                <span class="arv-stat__num">{vuelo.totalReservaciones}</span>
                <span class="arv-stat__lbl">reservaciones</span>
              </div>
              {#if vuelo.confirmadas > 0}
                <div class="arv-stat arv-stat--confirmada">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                  <span class="arv-stat__num">{vuelo.confirmadas}</span>
                  <span class="arv-stat__lbl">confirmadas</span>
                </div>
              {/if}
              {#if vuelo.pendientes > 0}
                <div class="arv-stat arv-stat--pendiente">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  <span class="arv-stat__num">{vuelo.pendientes}</span>
                  <span class="arv-stat__lbl">pendientes</span>
                </div>
              {/if}
              {#if vuelo.canceladas > 0}
                <div class="arv-stat arv-stat--cancelada">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                  <span class="arv-stat__num">{vuelo.canceladas}</span>
                  <span class="arv-stat__lbl">canceladas</span>
                </div>
              {/if}
              {#if vuelo.completadas > 0}
                <div class="arv-stat arv-stat--completada">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>
                  <span class="arv-stat__num">{vuelo.completadas}</span>
                  <span class="arv-stat__lbl">completadas</span>
                </div>
              {/if}
            </div>

            <!-- CTA -->
            <div class="arv-card__cta">
              <span>Ver reservaciones</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><polyline points="9 18 15 12 9 6"/></svg>
            </div>

          </article>
        {/each}
      </div>
    {/if}

  <!-- ───────────── VISTA 2: RESERVACIONES DEL VUELO ───────────── -->
  {:else if vista === 'reservaciones' && vueloActual}

    <!-- Topbar del vuelo seleccionado -->
    <div class="arv-topbar">
      <button class="arv-topbar__back" on:click={volverAVuelos} type="button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><polyline points="15 18 9 12 15 6"/></svg>
        Volver a vuelos
      </button>

      <div class="arv-topbar__info">
        <div class="arv-topbar__left">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" width="18" height="18" style="flex-shrink:0;opacity:.7">
            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
          </svg>
          <span class="arv-topbar__numero">{vueloActual.numeroVuelo}</span>
          <div class="arv-topbar__ruta-pill">
            <span class="arv-topbar__iata">{vueloActual.origenCodigo}</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            <span class="arv-topbar__iata">{vueloActual.destinoCodigo}</span>
          </div>
          <span class="arv-topbar__ciudades">{vueloActual.origenCiudad} · {vueloActual.destinoCiudad}</span>
        </div>
        <div class="arv-topbar__right">
          {#if vueloActual.fechaVuelo}
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            {formatFecha(vueloActual.fechaVuelo)}
          {/if}
          {#if vueloActual.horaSalida}
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            {formatHora(vueloActual.horaSalida)}
          {/if}
        </div>
      </div>
    </div>

    <!-- Filtros -->
    <div class="ar-toolbar" style="margin-top:1.25rem">
      <div class="ar-search" style="flex:1;min-width:220px">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15" class="ar-search__icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input class="ar-search__input" type="text" placeholder="Buscar por N° reservación, usuario o correo..." bind:value={busquedaReserva}/>
        {#if busquedaReserva}<button class="ar-search__clear" on:click={() => busquedaReserva=''}>×</button>{/if}
      </div>
      <div class="ar-filtros">
        {#each filtros as f}
          <button class="ar-filtro" class:ar-filtro--active={filtroEstado===f.key} on:click={() => filtroEstado=f.key} type="button">
            {f.label}
          </button>
        {/each}
      </div>
    </div>

    <!-- Tabla -->
    {#if loadingReservas}
      <div class="ar-empty" style="margin-top:2rem"><div class="ar-spinner ar-spinner--lg"></div><p>Cargando reservaciones...</p></div>
    {:else if errorReservas}
      <div class="ar-empty ar-empty--error" style="margin-top:2rem"><p>{errorReservas}</p></div>
    {:else if reservasFiltradas.length === 0}
      <div class="ar-empty" style="margin-top:2rem">
        <svg viewBox="0 0 24 24" fill="none" stroke="#B89A7A" stroke-width="1.5" width="48" height="48"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        <p>No hay reservaciones{filtroEstado !== 'todas' ? ` con estado "${filtroEstado}"` : ''} para este vuelo.</p>
        {#if filtroEstado !== 'todas'}<button class="ar-btn ar-btn--ghost" on:click={() => filtroEstado='todas'}>Ver todas</button>{/if}
      </div>
    {:else}
      <p class="ar-result-count" style="margin-top:1rem">
        {reservasFiltradas.length} reservacion{reservasFiltradas.length !== 1 ? 'es' : ''}
      </p>
      <div class="ar-table-wrap" style="margin-top:.5rem">
        <table class="ar-table">
          <thead class="ar-table__head">
            <tr>
              <th class="ar-table__th">N° Reservación</th>
              <th class="ar-table__th">Usuario</th>
              <th class="ar-table__th">Correo</th>
              <th class="ar-table__th">Boletos</th>
              <th class="ar-table__th">Total</th>
              <th class="ar-table__th">Estado</th>
              <th class="ar-table__th">Fecha</th>
              <th class="ar-table__th">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {#each reservasFiltradas as reserva (reserva.reservacionId)}
              <tr class="ar-table__row" class:ar-table__row--cancelada={reserva.estadoReserva?.toLowerCase()==='cancelada'}>
                <td class="ar-table__td ar-table__td--mono">{reserva.noReservacion}</td>
                <td class="ar-table__td">{reserva.usuarioNombre ?? '--'}</td>
                <td class="ar-table__td ar-table__td--email">{reserva.usuarioEmail ?? '--'}</td>
                <td class="ar-table__td ar-table__td--center">{reserva.boletos?.length ?? 0}</td>
                <td class="ar-table__td ar-table__td--price">${reserva.total?.toFixed(2) ?? '0.00'}</td>
                <td class="ar-table__td"><span class="ar-badge {estadoClase(reserva.estadoReserva)}">{reserva.estadoReserva}</span></td>
                <td class="ar-table__td ar-table__td--date">{formatFecha(reserva.fechaCreacion)}</td>
                <td class="ar-table__td">
                  <div class="ar-row-actions">
                    <button class="ar-action-btn ar-action-btn--view" on:click={() => abrirDetalle(reserva)} type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                      Ver
                    </button>
                    {#if reserva.estadoReserva?.toLowerCase()==='confirmada' || reserva.estadoReserva?.toLowerCase()==='pendiente'}
                      <button class="ar-action-btn ar-action-btn--cancel"
                        on:click={() => { abrirDetalle(reserva).then(() => { cancelarAbierto = true; }); }}
                        type="button">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                        Cancelar
                      </button>
                    {/if}
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}

  {/if}
</section>