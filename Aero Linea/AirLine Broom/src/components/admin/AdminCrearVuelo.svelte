<script>
// @ts-nocheck
  import { createEventDispatcher } from 'svelte';

  export let API;
  export let aeropuertos  = [];
  export let aviones      = [];
  export let tripulantes  = [];
  export let mostrarToast;
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  let busquedaOrigen        = '';
  let busquedaDestino       = '';
  let busquedaAvion         = '';
  let busquedaTripulante    = '';
  let mostrarDropdownOrigen     = false;
  let mostrarDropdownDestino    = false;
  let mostrarDropdownAvion      = false;
  let mostrarDropdownTripulante = false;

  let nuevoVuelo = {
    numeroVuelo: '',
    aeropuertoOrigenId: '',
    aeropuertoDestinoId: '',
    avionId: '',
    fecha: '',
    horaSalida: '',
    boletosTurista: '',
    boletosEjecutivo: '',
    precioTurista: '',
    precioEjecutiva: '',
    tripulantesSeleccionados: []
  };

  let previewLlegada       = null;
  let loadingPreview       = false;
  let previewDebounceTimer = null;
  let rutaExisteStatus     = null;
  let rutaCheckTimer       = null;
  let lastOrigenId         = null;
  let lastDestinoId        = null;

  let avionesOcupadosIds     = new Set();
  let tripulantesOcupadosIds = new Set();
  let cargandoDisponibilidad = false;

  const hoyStr = new Date().toISOString().split('T')[0];

  function formatearNumeroVuelo(e) {
    let val = e.target.value.toUpperCase().replace(/[^A-Z0-9 ]/g, '');
    let letras = val.slice(0, 2).replace(/[^A-Z]/g, '');
    let resto  = val.slice(2).replace(/[^0-9]/g, '');
    if (letras.length === 2 && resto.length > 0) {
      nuevoVuelo.numeroVuelo = `${letras} ${resto}`;
    } else {
      nuevoVuelo.numeroVuelo = letras + resto;
    }
    e.target.value = nuevoVuelo.numeroVuelo;
  }

  function fechaEsValida(fecha) {
    if (!fecha || fecha.length < 10) return false;
    const year = parseInt(fecha.split('-')[0]);
    const hoy  = new Date();
    return year >= hoy.getFullYear() && year <= 2099;
  }

  function fechaEsPasada(fecha) {
    if (!fecha) return false;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return new Date(fecha) < hoy;
  }

  $: aeropuertosFiltradosOrigen = busquedaOrigen.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaOrigen.toLowerCase())
      ).slice(0, 10);

  $: aeropuertosFiltradosDestino = busquedaDestino.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaDestino.toLowerCase())
      ).slice(0, 10);

  $: avionesFiltrados = aviones.filter(a => {
    const coincide =
      a.nombreCompleto.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.marca.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.modelo.toLowerCase().includes(busquedaAvion.toLowerCase());
    return coincide && !avionesOcupadosIds.has(a.id);
  });

  $: tripulantesFiltrados = tripulantes.filter(t => {
    const yaSeleccionado = nuevoVuelo.tripulantesSeleccionados.some(ts => ts.id === t.id);
    const coincide =
      t.nombreCompleto.toLowerCase().includes(busquedaTripulante.toLowerCase()) ||
      t.nombreRol.toLowerCase().includes(busquedaTripulante.toLowerCase());
    return !yaSeleccionado && !tripulantesOcupadosIds.has(t.id) && coincide;
  });

  $: aeropuertoOrigen  = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoOrigenId));
  $: aeropuertoDestino = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoDestinoId));
  $: avionSeleccionado = aviones.find(a => a.id === parseInt(nuevoVuelo.avionId));

  $: totalBoletosAsignados = (parseInt(nuevoVuelo.boletosTurista)   || 0) +
                             (parseInt(nuevoVuelo.boletosEjecutivo) || 0);
  $: capacidadAvion    = avionSeleccionado?.capacidadPasajeros ?? 0;
  $: excedeLimite      = capacidadAvion > 0 && totalBoletosAsignados > capacidadAvion;
  $: porcentajeOcupado = capacidadAvion > 0
    ? Math.min(100, Math.round(totalBoletosAsignados / capacidadAvion * 100))
    : 0;

  $: camposListos = !!nuevoVuelo.aeropuertoOrigenId &&
                    !!nuevoVuelo.aeropuertoDestinoId &&
                    fechaEsValida(nuevoVuelo.fecha) &&
                    !fechaEsPasada(nuevoVuelo.fecha) &&
                    !!nuevoVuelo.horaSalida;

  $: if (avionSeleccionado && !nuevoVuelo.boletosTurista && !nuevoVuelo.boletosEjecutivo) {
    const cap = avionSeleccionado.capacidadPasajeros;
    const eje = Math.floor(cap * 0.25);
    nuevoVuelo.boletosEjecutivo = eje;
    nuevoVuelo.boletosTurista   = cap - eje;
  }

  $: { nuevoVuelo.aeropuertoOrigenId; nuevoVuelo.aeropuertoDestinoId; verificarRutaSiCambioAeropuerto(); }

  $: {
    nuevoVuelo.fecha; nuevoVuelo.horaSalida;
    if (fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) actualizarPreviewLlegada();
    else previewLlegada = null;
  }

  $: {
    nuevoVuelo.fecha; nuevoVuelo.horaSalida;
    if (fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) cargarDisponibilidad();
    else { avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set(); }
  }

  function verificarRutaSiCambioAeropuerto() {
    const origenId  = nuevoVuelo.aeropuertoOrigenId;
    const destinoId = nuevoVuelo.aeropuertoDestinoId;
    if (!origenId || !destinoId) { rutaExisteStatus = null; previewLlegada = null; return; }
    if (origenId === lastOrigenId && destinoId === lastDestinoId) return;
    lastOrigenId = origenId; lastDestinoId = destinoId;
    clearTimeout(rutaCheckTimer);
    rutaCheckTimer = setTimeout(async () => {
      rutaExisteStatus = 'checking';
      try {
        const rc = await fetch(`${API}/api/rutas/existe?origenId=${origenId}&destinoId=${destinoId}`, { credentials: 'include' });
        if (rc.ok) { const { existe } = await rc.json(); rutaExisteStatus = existe ? 'ok' : 'missing'; }
        else rutaExisteStatus = null;
      } catch { rutaExisteStatus = null; }
      if (rutaExisteStatus === 'ok' && fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) calcularPreviewLlegada();
      else { previewLlegada = null; loadingPreview = false; }
    }, 300);
  }

  function actualizarPreviewLlegada() {
    if (rutaExisteStatus !== 'ok') return;
    calcularPreviewLlegada();
  }

  function calcularPreviewLlegada() {
    const origenId  = parseInt(nuevoVuelo.aeropuertoOrigenId);
    const destinoId = parseInt(nuevoVuelo.aeropuertoDestinoId);
    if (!origenId || !destinoId || isNaN(origenId) || isNaN(destinoId) ||
        !fechaEsValida(nuevoVuelo.fecha) || fechaEsPasada(nuevoVuelo.fecha) ||
        !nuevoVuelo.horaSalida) {
      previewLlegada = null; loadingPreview = false; return;
    }
    clearTimeout(previewDebounceTimer);
    previewDebounceTimer = setTimeout(async () => {
      loadingPreview = true; previewLlegada = null;
      const controller = new AbortController();
      const tid = setTimeout(() => controller.abort(), 8000);
      try {
        const r = await fetch(`${API}/api/rutas/calcular-llegada`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          signal: controller.signal,
          body: JSON.stringify({
            aeropuertoOrigenId:  origenId,
            aeropuertoDestinoId: destinoId,
            fechaSalida: nuevoVuelo.fecha,
            horaSalida:  nuevoVuelo.horaSalida
          })
        });
        if (r.ok) previewLlegada = await r.json();
        else previewLlegada = null;
      } catch { previewLlegada = null; }
      finally { clearTimeout(tid); loadingPreview = false; }
    }, 600);
  }

  async function cargarDisponibilidad() {
    if (!fechaEsValida(nuevoVuelo.fecha) || fechaEsPasada(nuevoVuelo.fecha)) {
      avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set(); return;
    }
    cargandoDisponibilidad = true;
    try {
      const urlAviones = `${API}/api/admin/vuelos/aviones-ocupados?fecha=${nuevoVuelo.fecha}${nuevoVuelo.horaSalida ? `&horaSalida=${nuevoVuelo.horaSalida}` : ''}`;
      const urlTrip    = nuevoVuelo.horaSalida
        ? `${API}/api/admin/vuelos/tripulantes-ocupados?fecha=${nuevoVuelo.fecha}&horaSalida=${nuevoVuelo.horaSalida}`
        : null;
      const [rA, rT] = await Promise.all([
        fetch(urlAviones, { credentials: 'include' }),
        urlTrip ? fetch(urlTrip, { credentials: 'include' }) : Promise.resolve(null)
      ]);
      if (rA.ok) avionesOcupadosIds = new Set(await rA.json());
      if (rT && rT.ok) tripulantesOcupadosIds = new Set(await rT.json());
      if (nuevoVuelo.avionId && avionesOcupadosIds.has(parseInt(nuevoVuelo.avionId))) {
        nuevoVuelo.avionId = ''; nuevoVuelo.boletosTurista = ''; nuevoVuelo.boletosEjecutivo = '';
        busquedaAvion = '';
      }
      nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados
        .filter(t => !tripulantesOcupadosIds.has(t.id));
    } catch(e) { console.error('Error cargando disponibilidad', e); }
    finally { cargandoDisponibilidad = false; }
  }

  function seleccionarAeropuertoOrigen(a)  { nuevoVuelo.aeropuertoOrigenId  = a.id; busquedaOrigen  = `${a.codigo} - ${a.nombre}`; mostrarDropdownOrigen  = false; }
  function seleccionarAeropuertoDestino(a) { nuevoVuelo.aeropuertoDestinoId = a.id; busquedaDestino = `${a.codigo} - ${a.nombre}`; mostrarDropdownDestino = false; }
  function seleccionarAvion(a)   { nuevoVuelo.avionId = a.id; busquedaAvion = a.nombreCompleto; mostrarDropdownAvion = false; nuevoVuelo.boletosTurista = ''; nuevoVuelo.boletosEjecutivo = ''; }
  function agregarTripulante(t)  { nuevoVuelo.tripulantesSeleccionados = [...nuevoVuelo.tripulantesSeleccionados, t]; busquedaTripulante = ''; mostrarDropdownTripulante = false; }
  function quitarTripulante(id)  { nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados.filter(t => t.id !== id); }

  function limpiarFormularioVuelo() {
    nuevoVuelo = { numeroVuelo: '', aeropuertoOrigenId: '', aeropuertoDestinoId: '', avionId: '', fecha: '', horaSalida: '', boletosTurista: '', boletosEjecutivo: '', precioTurista: '', precioEjecutiva: '', tripulantesSeleccionados: [] };
    busquedaOrigen = ''; busquedaDestino = ''; busquedaAvion = ''; busquedaTripulante = '';
    previewLlegada = null; rutaExisteStatus = null; lastOrigenId = null; lastDestinoId = null;
    avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set();
  }

  async function handleCrearVuelo() {
    if (!nuevoVuelo.numeroVuelo || nuevoVuelo.numeroVuelo.length < 4) { mostrarToast('error', 'Ingresa el número de vuelo (ej: AA 1234)'); return; }
    if (!nuevoVuelo.aeropuertoOrigenId)   { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevoVuelo.aeropuertoDestinoId)  { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (!nuevoVuelo.avionId)              { mostrarToast('error', 'Selecciona un avión'); return; }
    if (!fechaEsValida(nuevoVuelo.fecha)) { mostrarToast('error', 'Ingresa una fecha válida'); return; }
    if (fechaEsPasada(nuevoVuelo.fecha))  { mostrarToast('error', 'La fecha del vuelo no puede ser en el pasado'); return; }
    if (!nuevoVuelo.horaSalida)           { mostrarToast('error', 'Ingresa la hora de salida'); return; }
    if (!nuevoVuelo.boletosTurista || parseInt(nuevoVuelo.boletosTurista) < 0)     { mostrarToast('error', 'Ingresa los boletos de clase turista'); return; }
    if (!nuevoVuelo.boletosEjecutivo || parseInt(nuevoVuelo.boletosEjecutivo) < 0) { mostrarToast('error', 'Ingresa los boletos de clase ejecutiva'); return; }
    if (excedeLimite) { mostrarToast('error', `Los boletos (${totalBoletosAsignados}) exceden la capacidad del avión (${capacidadAvion})`); return; }
    if (!nuevoVuelo.precioTurista || !nuevoVuelo.precioEjecutiva) { mostrarToast('error', 'Ingresa los precios de ambas clases'); return; }
    try {
      const rCheck = await fetch(`${API}/api/rutas/existe?origenId=${nuevoVuelo.aeropuertoOrigenId}&destinoId=${nuevoVuelo.aeropuertoDestinoId}`, { credentials: 'include' });
      if (rCheck.ok) { const { existe } = await rCheck.json(); if (!existe) { mostrarToast('error', 'No existe una ruta entre estos aeropuertos. Créala primero en "Gestionar Rutas".'); return; } }
    } catch { }
    try {
      const r = await fetch(`${API}/api/admin/vuelos`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          numeroVuelo:         nuevoVuelo.numeroVuelo,
          aeropuertoOrigenId:  parseInt(nuevoVuelo.aeropuertoOrigenId),
          aeropuertoDestinoId: parseInt(nuevoVuelo.aeropuertoDestinoId),
          avionId:             parseInt(nuevoVuelo.avionId),
          fecha:               nuevoVuelo.fecha,
          horaSalida:          nuevoVuelo.horaSalida,
          boletosTurista:      parseInt(nuevoVuelo.boletosTurista),
          boletosEjecutivo:    parseInt(nuevoVuelo.boletosEjecutivo),
          precioTurista:       parseFloat(nuevoVuelo.precioTurista),
          precioEjecutiva:     parseFloat(nuevoVuelo.precioEjecutiva),
          tripulantesIds:      nuevoVuelo.tripulantesSeleccionados.map(t => t.id)
        })
      });
      if (r.ok) { mostrarToast('success', '¡Vuelo creado exitosamente!'); limpiarFormularioVuelo(); dispatch('vueloCreado'); }
      else { const err = await r.json(); mostrarToast('error', err.message || 'Error al crear el vuelo'); }
    } catch { mostrarToast('error', 'Error de conexión al crear el vuelo'); }
  }
</script>

<section class="admin-section">
  <h2 class="admin-section__title">Crear Nuevo Vuelo</h2>
  <p class="admin-section__subtitle">Completa todos los datos del vuelo</p>

  <form class="admin-form" on:submit|preventDefault={handleCrearVuelo}>

    <!-- Información básica -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Información Básica</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-numero" class="admin-form__label">Número de Vuelo *</label>
          <input type="text" id="cv-numero" class="admin-form__input"
            value={nuevoVuelo.numeroVuelo}
            on:input={formatearNumeroVuelo}
            placeholder="Ej: AA 1234" maxlength="8"
            style="text-transform:uppercase;letter-spacing:1px"
            autocomplete="off" required />
          <small class="img-hint">2 letras + espacio + número (ej: AA 1234, LA 820)</small>
        </div>
        <div class="admin-form__field">
          <label for="cv-fecha" class="admin-form__label">Fecha del Vuelo *</label>
          <input type="date" id="cv-fecha" class="admin-form__input"
            bind:value={nuevoVuelo.fecha} min={hoyStr} required />
          {#if nuevoVuelo.fecha && fechaEsPasada(nuevoVuelo.fecha)}
            <small style="color:#c62828;font-size:.78rem">⚠ La fecha no puede ser en el pasado</small>
          {/if}
        </div>
      </div>
    </div>

    <!-- Ruta -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Ruta</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-origen" class="admin-form__label">Aeropuerto de Origen *</label>
          <div class="searchable-select">
            <input id="cv-origen" type="text" class="admin-form__input" bind:value={busquedaOrigen}
              on:focus={() => mostrarDropdownOrigen = true}
              on:blur={() => setTimeout(() => mostrarDropdownOrigen = false, 200)}
              placeholder="Buscar aeropuerto..." autocomplete="off" />
            {#if mostrarDropdownOrigen && aeropuertosFiltradosOrigen.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaOrigen.length < 2}<div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>{/if}
                {#each aeropuertosFiltradosOrigen as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarAeropuertoOrigen(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoOrigen}<p class="selected-item">✔ {aeropuertoOrigen.codigo} — {aeropuertoOrigen.nombre}</p>{/if}
          </div>
        </div>
        <div class="admin-form__field">
          <label for="cv-destino" class="admin-form__label">Aeropuerto de Destino *</label>
          <div class="searchable-select">
            <input id="cv-destino" type="text" class="admin-form__input" bind:value={busquedaDestino}
              on:focus={() => mostrarDropdownDestino = true}
              on:blur={() => setTimeout(() => mostrarDropdownDestino = false, 200)}
              placeholder="Buscar aeropuerto..." autocomplete="off" />
            {#if mostrarDropdownDestino && aeropuertosFiltradosDestino.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaDestino.length < 2}<div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>{/if}
                {#each aeropuertosFiltradosDestino as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarAeropuertoDestino(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoDestino}<p class="selected-item">✔ {aeropuertoDestino.codigo} — {aeropuertoDestino.nombre}</p>{/if}
          </div>
        </div>
      </div>
    </div>

    <!-- Horarios -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Horarios</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-hora" class="admin-form__label">Hora de Salida *</label>
          <input type="time" id="cv-hora" class="admin-form__input" bind:value={nuevoVuelo.horaSalida} required />
          <small class="img-hint">Hora local en el aeropuerto de origen</small>
        </div>

        <div class="admin-form__field">
          <p class="admin-form__label">Hora de Llegada</p>

          {#if rutaExisteStatus === 'checking' || loadingPreview}
            <div class="llegada-preview llegada-preview--loading">
              <div class="llegada-loader">
                <span class="llegada-loader__plane">✈</span>
                <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                <span class="llegada-loader__text">
                  {rutaExisteStatus === 'checking' ? 'Verificando ruta...' : 'Calculando hora de llegada...'}
                </span>
              </div>
            </div>

          {:else if rutaExisteStatus === 'missing'}
            <div class="llegada-preview--no-ruta">
              <span class="llegada-preview__no-ruta-icon">🚫</span>
              <span class="llegada-preview__no-ruta-title">No existe esta ruta</span>
              <small class="llegada-preview__no-ruta-msg">Créala en <strong>Gestionar Rutas</strong> antes de crear el vuelo.</small>
              <button type="button" class="llegada-preview__no-ruta-btn" on:click={() => dispatch('irARutas')}>→ Ir a crear la ruta</button>
            </div>

          {:else if previewLlegada}
            <div class="llegada-preview" class:llegada-preview--tz={previewLlegada.usoZonasHorarias}>
              <span class="llegada-preview__time">🛬 {previewLlegada.horaLlegada}
                {#if previewLlegada.fechaLlegada !== nuevoVuelo.fecha}
                  <span class="llegada-preview__nextday">(+1 día)</span>
                {/if}
              </span>
              <span class="llegada-preview__meta">{previewLlegada.duracionMinutos} min ·
                {#if previewLlegada.usoZonasHorarias}
                  <span class="tz-badge tz-badge--ok">✔ Con zona horaria</span>
                {:else}
                  <span class="tz-badge tz-badge--missing">⚠ Sin zona horaria</span>
                {/if}
              </span>
              <small class="llegada-preview__nota">{previewLlegada.nota}</small>
            </div>

          {:else}
            <div class="llegada-preview llegada-preview--empty">
              Se calcula automáticamente al completar origen, destino, fecha y hora de salida
              {#if camposListos}
                <div class="llegada-loader" style="margin-top:.5rem">
                  <span class="llegada-loader__plane">✈</span>
                  <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                  <span class="llegada-loader__text">Preparando cálculo...</span>
                </div>
              {/if}
            </div>
          {/if}
        </div>
      </div>
    </div>

    <!-- Aeronave -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Aeronave</h3>
      <div class="admin-form__field admin-form__field--full">
        <label for="cv-avion" class="admin-form__label">Seleccionar Avión *</label>
        {#if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && avionesOcupadosIds.size > 0}
          <small class="disponibilidad-hint disponibilidad-hint--info">
            ℹ {avionesOcupadosIds.size} avión(es) ya asignado(s) a otro vuelo en esta fecha no aparecen en la lista
          </small>
        {:else if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && !cargandoDisponibilidad}
          <small class="disponibilidad-hint disponibilidad-hint--ok">
            ✔ Mostrando aviones disponibles para {nuevoVuelo.fecha}
          </small>
        {/if}
        <div class="searchable-select">
          <input id="cv-avion" type="text" class="admin-form__input" bind:value={busquedaAvion}
            on:focus={() => mostrarDropdownAvion = true}
            on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
            placeholder="Buscar avión..." autocomplete="off" />
          {#if mostrarDropdownAvion && avionesFiltrados.length > 0}
            <div class="searchable-select__dropdown">
              {#each avionesFiltrados.slice(0, 10) as a}
                <button type="button" class="searchable-select__option" on:click={() => seleccionarAvion(a)}>
                  {#if a.imagenBase64}<img src={a.imagenBase64} alt={a.nombreCompleto} class="dropdown-thumb" />{/if}
                  <span class="searchable-select__option-name">{a.nombreCompleto}</span>
                  <span class="searchable-select__option-detail">{a.capacidadPasajeros} pasajeros</span>
                </button>
              {/each}
            </div>
          {:else if mostrarDropdownAvion && avionesFiltrados.length === 0 && aviones.length > 0}
            <div class="searchable-select__dropdown">
              <p class="searchable-select__empty">🚫 Todos los aviones están ocupados para el {nuevoVuelo.fecha || 'día seleccionado'}</p>
            </div>
          {/if}
          {#if avionSeleccionado}<p class="selected-item">✔ {avionSeleccionado.nombreCompleto}</p>{/if}
        </div>
      </div>
    </div>

    <!-- Asientos y precios -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Distribución de Asientos y Precios</h3>
      {#if avionSeleccionado}
        <div class="capacidad-bar">
          <div class="capacidad-bar__labels">
            <span>Capacidad total: <strong>{capacidadAvion} pax</strong></span>
            <span class="capacidad-bar__count"
              class:capacidad-bar__count--ok={totalBoletosAsignados === capacidadAvion && !excedeLimite}
              class:capacidad-bar__count--error={excedeLimite}>
              {totalBoletosAsignados} asignados
              {#if excedeLimite}&nbsp;⚠ Excede límite{:else if totalBoletosAsignados === capacidadAvion}&nbsp;✔ Completo{/if}
            </span>
          </div>
          <div class="capacidad-bar__track">
            <div class="capacidad-bar__fill" class:capacidad-bar__fill--error={excedeLimite} style="width:{porcentajeOcupado}%"></div>
          </div>
        </div>
      {/if}
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-turista" class="admin-form__label">Boletos Clase Turista *</label>
          <input type="number" id="cv-turista" class="admin-form__input" min="0"
            bind:value={nuevoVuelo.boletosTurista} placeholder="Ej: 180"
            max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
        </div>
        <div class="admin-form__field">
          <label for="cv-ejecutivo" class="admin-form__label">Boletos Clase Ejecutiva *</label>
          <input type="number" id="cv-ejecutivo" class="admin-form__input" min="0"
            bind:value={nuevoVuelo.boletosEjecutivo} placeholder="Ej: 60"
            max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
        </div>
      </div>
      <div class="admin-form__row" style="margin-top:1.5rem">
        <div class="admin-form__field">
          <label for="cv-precio-turista" class="admin-form__label">Precio Turista (USD) *</label>
          <input type="number" id="cv-precio-turista" class="admin-form__input" min="0" step="0.01"
            bind:value={nuevoVuelo.precioTurista} placeholder="Ej: 150.00" required />
        </div>
        <div class="admin-form__field">
          <label for="cv-precio-eje" class="admin-form__label">Precio Ejecutiva (USD) *</label>
          <input type="number" id="cv-precio-eje" class="admin-form__input" min="0" step="0.01"
            bind:value={nuevoVuelo.precioEjecutiva} placeholder="Ej: 300.00" required />
        </div>
      </div>
    </div>

    <!-- Tripulación -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Tripulación</h3>
      <div class="admin-form__field admin-form__field--full">
        <label for="cv-trip" class="admin-form__label">Agregar Tripulantes</label>
        {#if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && tripulantesOcupadosIds.size > 0}
          <small class="disponibilidad-hint disponibilidad-hint--warn">
            ⚠ {tripulantesOcupadosIds.size} tripulante(s) ya asignado(s) a otro vuelo y no aparecen en la lista
          </small>
        {:else if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && nuevoVuelo.horaSalida && !cargandoDisponibilidad}
          <small class="disponibilidad-hint disponibilidad-hint--ok">
            ✔ Mostrando tripulantes disponibles para {nuevoVuelo.fecha} a las {nuevoVuelo.horaSalida}
          </small>
        {/if}
        <div class="searchable-select">
          <input id="cv-trip" type="text" class="admin-form__input" bind:value={busquedaTripulante}
            on:focus={() => mostrarDropdownTripulante = true}
            on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
            placeholder="Buscar por nombre o rol..." autocomplete="off" />
          {#if mostrarDropdownTripulante && tripulantesFiltrados.length > 0}
            <div class="searchable-select__dropdown">
              {#each tripulantesFiltrados.slice(0, 10) as t}
                <button type="button" class="searchable-select__option" on:click={() => agregarTripulante(t)}>
                  {#if t.imagenBase64}<img src={t.imagenBase64} alt={t.nombreCompleto} class="dropdown-thumb dropdown-thumb--circle" />{/if}
                  <span class="searchable-select__option-name">{t.nombreCompleto}</span>
                  <span class="searchable-select__option-role">{t.nombreRol}</span>
                </button>
              {/each}
            </div>
          {:else if mostrarDropdownTripulante && tripulantesFiltrados.length === 0 && tripulantes.length > 0}
            <div class="searchable-select__dropdown">
              <p class="searchable-select__empty">🚫 Ningún tripulante disponible.<br><small>Deben pasar 24h desde su vuelo anterior.</small></p>
            </div>
          {/if}
        </div>
        {#if nuevoVuelo.tripulantesSeleccionados.length > 0}
          <div class="tripulantes-seleccionados">
            <p class="tripulantes-seleccionados__title">Tripulantes seleccionados ({nuevoVuelo.tripulantesSeleccionados.length})</p>
            <div class="tripulantes-seleccionados__list">
              {#each nuevoVuelo.tripulantesSeleccionados as t}
                <div class="tripulante-item">
                  <div class="tripulante-item__info">
                    <span class="tripulante-item__name">{t.nombreCompleto}</span>
                    <span class="tripulante-item__rol">{t.nombreRol}</span>
                  </div>
                  <button type="button" class="tripulante-item__remove" on:click={() => quitarTripulante(t.id)}>×</button>
                </div>
              {/each}
            </div>
          </div>
        {/if}
      </div>
    </div>

    <div class="admin-form__actions">
      <button type="submit" class="admin-form__submit">Crear Vuelo</button>
      <button type="button" class="admin-form__cancel" on:click={limpiarFormularioVuelo}>Limpiar</button>
    </div>

  </form>
</section>