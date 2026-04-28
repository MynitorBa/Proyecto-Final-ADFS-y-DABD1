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

  // ── Cambiar vuelo (Admin) ────────────────────────────────────────────────────
  let cambiarVueloOpen       = false;
  let cambiarVueloCargando   = false;
  let cambiarVueloElegibles  = [];
  let cambiarVueloError      = '';
  let cambiarVueloSeleccion  = null;
  let cambiarVueloGuardando  = false;
  let cambiarVueloReservacionId = null;

  async function abrirCambiarVuelo(reservacionId) {
    cambiarVueloReservacionId = reservacionId;
    cambiarVueloElegibles = []; cambiarVueloError = ''; cambiarVueloSeleccion = null;
    cambiarVueloOpen = true; cambiarVueloCargando = true;
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/${reservacionId}/vuelos-elegibles`, { credentials: 'include' });
      const data = await r.json();
      if (r.ok) cambiarVueloElegibles = data;
      else cambiarVueloError = data.message || 'Error al buscar vuelos disponibles.';
    } catch { cambiarVueloError = 'Error de conexión.'; }
    finally { cambiarVueloCargando = false; }
  }

  async function confirmarCambioVuelo() {
    if (!cambiarVueloSeleccion) { cambiarVueloError = 'Selecciona un vuelo.'; return; }
    cambiarVueloGuardando = true; cambiarVueloError = '';
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/${cambiarVueloReservacionId}/cambiar-vuelo`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nuevoVueloId: cambiarVueloSeleccion })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Vuelo cambiado correctamente.');
        cambiarVueloOpen = false;
        // Recargar lista de reservas del vuelo actual
        if (vueloActual?.vueloId) {
          const rv = await fetch(`${API}/api/admin/reservaciones/vuelo/${vueloActual.vueloId}`, { credentials: 'include' });
          if (rv.ok) reservas = await rv.json();
        }
        // Si el detalle estaba abierto, recargarlo también
        if (reservaDetalle?.reservacionId) await abrirDetalle({ reservacionId: reservaDetalle.reservacionId });
      } else { cambiarVueloError = data.message || 'Error al cambiar el vuelo.'; }
    } catch { cambiarVueloError = 'Error de conexión.'; }
    finally { cambiarVueloGuardando = false; }
  }

  // ── Teléfono con máscara por país (misma lógica que MisReservas) ─────────
  const knownDigits = {
    '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,
    '+31':9,'+32':9,'+33':9,'+34':9,'+36':9,
    '+39':10,'+40':9,'+41':9,'+43':10,'+44':10,
    '+45':8,'+46':9,'+47':8,'+48':9,'+49':10,
    '+51':9,'+52':10,'+53':8,'+54':10,'+55':11,
    '+56':9,'+57':10,'+58':10,'+60':9,'+61':9,
    '+62':9,'+63':10,'+64':9,'+65':8,'+66':9,
    '+81':10,'+82':10,'+84':9,'+86':11,'+90':10,
    '+91':10,'+92':10,'+93':9,'+94':9,'+95':8,
    '+98':10,'+212':9,'+213':9,'+216':8,'+218':9,
    '+220':7,'+221':9,'+222':8,'+223':8,'+224':9,
    '+225':8,'+226':8,'+227':8,'+228':8,'+229':8,
    '+230':8,'+231':8,'+232':8,'+233':9,'+234':10,
    '+235':8,'+236':8,'+237':9,'+238':7,'+239':7,
    '+240':9,'+241':8,'+242':9,'+243':9,'+244':9,
    '+245':7,'+246':7,'+247':4,'+248':7,'+249':9,
    '+250':9,'+251':9,'+252':8,'+253':8,'+254':9,
    '+255':9,'+256':9,'+257':8,'+258':9,'+260':9,
    '+261':9,'+262':9,'+263':9,'+264':9,'+265':9,
    '+266':8,'+267':8,'+268':8,'+269':7,'+290':4,
    '+291':7,'+297':7,'+298':6,'+299':6,'+350':8,
    '+351':9,'+352':9,'+353':9,'+354':7,'+355':9,
    '+356':8,'+357':8,'+358':9,'+359':9,'+370':8,
    '+371':8,'+372':8,'+373':8,'+374':8,'+375':9,
    '+376':6,'+377':8,'+378':10,'+380':9,'+381':9,
    '+382':8,'+385':9,'+386':8,'+387':8,'+389':8,
    '+420':9,'+421':9,'+423':7,'+500':5,'+501':7,
    '+502':8,'+503':8,'+504':8,'+505':8,'+506':8,
    '+507':8,'+508':6,'+509':8,'+590':9,'+591':8,
    '+592':7,'+593':9,'+594':9,'+595':9,'+596':9,
    '+597':7,'+598':8,'+599':7,'+670':8,'+672':6,
    '+673':7,'+674':7,'+675':8,'+676':7,'+677':7,
    '+678':7,'+679':7,'+680':7,'+681':6,'+682':5,
    '+683':4,'+685':7,'+686':8,'+687':6,'+688':5,
    '+689':8,'+690':4,'+691':7,'+692':7,'+850':10,
    '+852':8,'+853':8,'+855':9,'+856':10,'+880':10,
    '+886':9,'+960':7,'+961':8,'+962':9,'+963':9,
    '+964':10,'+965':8,'+966':9,'+967':9,'+968':8,
    '+970':9,'+971':9,'+972':9,'+973':8,'+974':8,
    '+975':8,'+976':8,'+977':10,'+992':9,'+993':8,
    '+994':9,'+995':9,'+996':9,'+998':9,
  };

  function formatLocalPhone(digits, total) {
    if (total <= 7)   return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8)  return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9)  return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  function getPhonePlaceholder(digits) {
    return formatLocalPhone('5'.repeat(digits), digits);
  }

  // ── Editar pasajero (Admin) ───────────────────────────────────────────────
  let editarPasajeroOpen        = false;
  let editarPasajeroBoletoId    = null;
  let editarPasajeroReservacionId = null;
  let editarPasajeroEstado      = '';
  let editarPasajeroForm        = { nombre: '', apellido: '', pasaporte: '', telefono: '' };
  let editarPasajeroLoading     = false;
  let editarPasajeroError       = '';
  let editDialCode              = '';
  let editPhoneDigitCount       = 8;
  let editPhoneError            = '';

  function abrirEditarPasajero(boleto) {
    editarPasajeroBoletoId      = boleto.boletoId;
    editarPasajeroReservacionId = reservaDetalle?.reservacionId ?? null;
    editarPasajeroEstado        = reservaDetalle?.estadoReserva ?? '';

    // Extraer prefijo y dígitos del teléfono guardado (formato: "+502 55555555")
    const storedPhone = boleto.pasajero?.telefono ?? '';
    const parts = storedPhone.split(' ');
    const hasPrefix = parts.length >= 2 && parts[0].startsWith('+');
    if (hasPrefix) {
      editDialCode = parts[0];
      editPhoneDigitCount = knownDigits[editDialCode] ?? 8;
    } else {
      editDialCode = '';
      editPhoneDigitCount = 8;
    }
    const rawDigits = hasPrefix
      ? parts.slice(1).join('').replace(/\D/g, '').slice(0, editPhoneDigitCount)
      : storedPhone.replace(/\D/g, '').slice(0, editPhoneDigitCount);

    editarPasajeroForm = {
      nombre:    boleto.pasajero?.nombre    ?? '',
      apellido:  boleto.pasajero?.apellido  ?? '',
      pasaporte: boleto.pasajero?.pasaporte ?? '',
      telefono:  editDialCode ? formatLocalPhone(rawDigits, editPhoneDigitCount) : storedPhone
    };
    editPhoneError = '';
    editarPasajeroError = '';
    editarPasajeroOpen = true;
  }

  function onEditPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '');
    const capped = raw.slice(0, editPhoneDigitCount);
    editarPasajeroForm.telefono = formatLocalPhone(capped, editPhoneDigitCount);
    editPhoneError = '';
  }

  async function confirmarEditarPasajero() {
    if (!editarPasajeroForm.nombre.trim())    { editarPasajeroError = 'El nombre es obligatorio.';    return; }
    if (!editarPasajeroForm.apellido.trim())  { editarPasajeroError = 'El apellido es obligatorio.';  return; }
    if (!editarPasajeroForm.pasaporte.trim()) { editarPasajeroError = 'El pasaporte es obligatorio.'; return; }
    if (!/^[a-zA-Z0-9]+$/.test(editarPasajeroForm.pasaporte)) {
      editarPasajeroError = 'El pasaporte solo puede contener letras y números.'; return;
    }
    if (!editarPasajeroForm.telefono.trim())  { editarPasajeroError = 'El teléfono es obligatorio.';  return; }
    if (editDialCode) {
      const digitCount = editarPasajeroForm.telefono.replace(/\D/g, '').length;
      if (digitCount !== editPhoneDigitCount) {
        editPhoneError = `Se requieren ${editPhoneDigitCount} dígitos (ingresaste ${digitCount}).`;
        return;
      }
    }

    // Recombinar prefijo + dígitos locales (sin espacios internos) para guardar
    const localDigits = editarPasajeroForm.telefono.replace(/\s/g, '');
    const telefonoCompleto = editDialCode ? `${editDialCode} ${localDigits}` : editarPasajeroForm.telefono;

    editarPasajeroLoading = true; editarPasajeroError = ''; editPhoneError = '';
    try {
      const r = await fetch(`${API}/api/admin/reservaciones/boleto/${editarPasajeroBoletoId}/pasajero`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...editarPasajeroForm, telefono: telefonoCompleto })
      });
      const data = await r.json().catch(() => ({}));
      if (r.ok) {
        mostrarToast('success', 'Datos del pasajero actualizados correctamente.');
        editarPasajeroOpen = false;
        // Recargar detalle para reflejar el cambio
        if (reservaDetalle?.reservacionId) {
          const rd = await fetch(`${API}/api/admin/reservaciones/${reservaDetalle.reservacionId}`, { credentials: 'include' });
          if (rd.ok) reservaDetalle = await rd.json();
        }
      } else {
        editarPasajeroError = data.message || 'No se pudo actualizar.';
      }
    } catch { editarPasajeroError = 'Error de conexion.'; }
    finally { editarPasajeroLoading = false; }
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
                <div class="ar-boleto__pasajero" style="justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:.5rem">
                  <div style="display:flex;align-items:flex-start;gap:.5rem">
                    <svg viewBox="0 0 24 24" fill="none" stroke="var(--primary-color)" stroke-width="2" width="14" height="14" style="margin-top:2px;flex-shrink:0"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    <div>
                      <span class="ar-boleto__pasajero-name">{boleto.pasajero.nombre} {boleto.pasajero.apellido}</span>
                      <span class="ar-boleto__pasajero-info">Pasaporte: {boleto.pasajero.pasaporte} · Tel: {boleto.pasajero.telefono} · {boleto.pasajero.ciudad}</span>
                    </div>
                  </div>
                  <button class="ar-btn ar-btn--ghost" style="font-size:.75rem;padding:.25rem .6rem"
                    on:click|stopPropagation={() => abrirEditarPasajero(boleto)} type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12" style="vertical-align:-1px;margin-right:3px"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>Editar
                  </button>
                </div>
              {/if}
            </div>
          {/each}
        </div>

        {#if reservaDetalle.estadoReserva?.toLowerCase()==='confirmada' || reservaDetalle.estadoReserva?.toLowerCase()==='pendiente'}
          <!-- Cambiar vuelo (Admin) -->
          {#if !cancelarAbierto}
            <div class="ar-cancel-trigger" style="display:flex;gap:.75rem;flex-wrap:wrap">
              <button class="ar-btn" style="background:#2563eb;color:#fff;display:inline-flex;align-items:center;gap:.4rem"
                on:click={() => abrirCambiarVuelo(reservaDetalle.reservacionId)} type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
                Cambiar vuelo
              </button>
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

<!-- ═══════════════ MODAL EDITAR PASAJERO ═══════════════ -->
{#if editarPasajeroOpen}
  <div class="ar-overlay" on:click={() => editarPasajeroOpen = false} role="dialog" aria-modal="true">
    <div class="ar-modal" on:click|stopPropagation style="max-width:420px">
      <button class="ar-modal__close" on:click={() => editarPasajeroOpen = false} aria-label="Cerrar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="18" height="18">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
      <div class="ar-modal__head">
        <div class="ar-modal__head-left">
          <span class="ar-modal__reserva-num">Editar Pasajero</span>
        </div>
      </div>
      <div style="padding:1rem 1.5rem 1.5rem">
        <div style="display:flex;flex-direction:column;gap:.75rem">
          <div>
            <label style="font-size:.8rem;font-weight:600;color:var(--text-muted);display:block;margin-bottom:.25rem">Nombre</label>
            <input class="ar-input" type="text" bind:value={editarPasajeroForm.nombre} maxlength="60" placeholder="Nombre"/>
          </div>
          <div>
            <label style="font-size:.8rem;font-weight:600;color:var(--text-muted);display:block;margin-bottom:.25rem">Apellido</label>
            <input class="ar-input" type="text" bind:value={editarPasajeroForm.apellido} maxlength="60" placeholder="Apellido"/>
          </div>
          <div>
            <label style="font-size:.8rem;font-weight:600;color:var(--text-muted);display:block;margin-bottom:.25rem">Pasaporte (solo letras y números)</label>
            <input class="ar-input" type="text" bind:value={editarPasajeroForm.pasaporte} maxlength="20" placeholder="Ej: A1234567"
              on:input={(e) => { editarPasajeroForm.pasaporte = e.target.value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase(); }}/>
          </div>
          <div>
            <label style="font-size:.8rem;font-weight:600;color:var(--text-muted);display:block;margin-bottom:.25rem">Teléfono</label>
            {#if editDialCode}
              <div style="display:flex;gap:.5rem;align-items:center">
                <span style="background:#1f2937;color:#fff;padding:.35rem .6rem;border-radius:.375rem;font-size:.75rem;font-weight:600;white-space:nowrap">{editDialCode}</span>
                <input
                  class="ar-input"
                  type="text"
                  bind:value={editarPasajeroForm.telefono}
                  on:input={onEditPhoneInput}
                  maxlength={editPhoneDigitCount <= 7 ? 8 : editPhoneDigitCount === 8 ? 9 : editPhoneDigitCount === 9 ? 11 : editPhoneDigitCount === 10 ? 12 : 13}
                  placeholder={getPhonePlaceholder(editPhoneDigitCount)}
                />
              </div>
            {:else}
              <input class="ar-input" type="text" bind:value={editarPasajeroForm.telefono} on:input={onEditPhoneInput} maxlength="20" placeholder="Ej: +502 5555 5555"/>
            {/if}
            {#if editPhoneError}
              <p class="ar-form-error" style="margin-top:.25rem">{editPhoneError}</p>
            {/if}
          </div>
          {#if editarPasajeroError}
            <p class="ar-form-error">{editarPasajeroError}</p>
          {/if}
        </div>
        <div class="ar-cancel-panel__actions" style="margin-top:1.25rem">
          <button class="ar-btn ar-btn--ghost" on:click={() => editarPasajeroOpen = false} disabled={editarPasajeroLoading} type="button">Cancelar</button>
          <button class="ar-btn" style="background:var(--primary-color);color:#fff" on:click={confirmarEditarPasajero} disabled={editarPasajeroLoading} type="button">
            {#if editarPasajeroLoading}<span class="ar-spinner ar-spinner--sm"></span> Guardando...{:else}Guardar cambios{/if}
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}

<!-- Modal de cambiar vuelo (Admin) -->
{#if cambiarVueloOpen}
  <div class="ar-overlay" on:click={() => cambiarVueloOpen = false} role="dialog" aria-modal="true">
    <div class="ar-modal" on:click|stopPropagation style="max-width:620px;max-height:88vh;overflow-y:auto">
      <div class="ar-modal__head">
        <div class="ar-modal__head-left">
          <span class="ar-modal__reserva-num">Cambiar a otro vuelo</span>
        </div>
        <button class="ar-modal__close" on:click={() => cambiarVueloOpen = false}>×</button>
      </div>
      <div style="padding:1rem 1.5rem 0">
        <p style="margin:0 0 1rem;font-size:.85rem;color:#6b7280">Vuelos disponibles con mismo origen (país), destino, precio y clase:</p>
      </div>

      {#if cambiarVueloCargando}
        <div style="padding:2rem;text-align:center;color:#8B6B4A">Buscando vuelos disponibles...</div>
      {:else if cambiarVueloError && cambiarVueloElegibles.length === 0}
        <div style="padding:1rem 1.5rem">
          <p style="color:#ef4444;font-size:.85rem;background:#fef2f2;border:1px solid #fecaca;border-radius:8px;padding:.75rem">{cambiarVueloError}</p>
        </div>
      {:else if cambiarVueloElegibles.length === 0}
        <div style="padding:1.5rem;text-align:center;color:#6b7280;font-size:.9rem">
          No hay vuelos elegibles para cambio (mismo origen, destino y precio).
        </div>
      {:else}
        <div style="padding:0 1.5rem;display:flex;flex-direction:column;gap:.6rem">
          {#each cambiarVueloElegibles as v}
            <label style="display:flex;align-items:flex-start;gap:.75rem;padding:.85rem 1rem;border:2px solid {cambiarVueloSeleccion === v.vueloId ? '#2563eb' : '#e5e7eb'};border-radius:10px;cursor:pointer;background:{cambiarVueloSeleccion === v.vueloId ? '#eff6ff' : '#fff'};transition:border-color .15s">
              <input type="radio" name="ar-vuelo" value={v.vueloId} bind:group={cambiarVueloSeleccion} style="margin-top:.25rem;accent-color:#2563eb;flex-shrink:0" />
              <div style="flex:1;min-width:0">
                <div style="display:flex;align-items:center;gap:.5rem;flex-wrap:wrap">
                  <span style="font-weight:700;font-size:.93rem;color:#1C1A18">{v.numeroVuelo}</span>
                  <span style="font-size:.78rem;color:#6b7280;background:#f3f4f6;padding:.1rem .45rem;border-radius:999px">{v.origenCodigo} → {v.destinoCodigo}</span>
                </div>
                <div style="font-size:.8rem;color:#374151;margin-top:.2rem">{v.fechaSalida} · {v.horaSalida} → {v.horaLlegada}</div>
                <div style="font-size:.78rem;color:#6b7280;margin-top:.2rem">{v.origenCiudad} ({v.origenPais}) → {v.destinoCiudad}</div>
                <div style="display:flex;gap:.75rem;margin-top:.25rem;font-size:.8rem">
                  <span style="color:#059669;font-weight:600">Q{v.precioTotal?.toFixed(2)} total</span>
                  <span style="color:#6b7280">{v.asientosDisponibles} asiento(s) libre(s)</span>
                </div>
              </div>
            </label>
          {/each}
        </div>
      {/if}

      {#if cambiarVueloError && cambiarVueloElegibles.length > 0}
        <p style="margin:.5rem 1.5rem 0;color:#ef4444;font-size:.82rem">{cambiarVueloError}</p>
      {/if}

      <div class="ar-cancel-panel__actions" style="margin:1rem 1.5rem 1.5rem">
        <button class="ar-btn ar-btn--ghost" on:click={() => cambiarVueloOpen = false} disabled={cambiarVueloGuardando} type="button">Cancelar</button>
        <button class="ar-btn" style="background:#2563eb;color:#fff;opacity:{cambiarVueloSeleccion && !cambiarVueloGuardando ? 1 : 0.5}"
          disabled={!cambiarVueloSeleccion || cambiarVueloGuardando}
          on:click={confirmarCambioVuelo} type="button">
          {cambiarVueloGuardando ? 'Procesando...' : 'Confirmar cambio'}
        </button>
      </div>
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