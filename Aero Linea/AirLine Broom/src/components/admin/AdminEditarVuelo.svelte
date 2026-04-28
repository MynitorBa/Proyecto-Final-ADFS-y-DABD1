<script>
// @ts-nocheck
/**
 * @file AdminEditarVuelo.svelte
 * @description Modal para editar un vuelo existente. Precarga los datos del vuelo recibido en el prop
 * `vuelo` y permite modificar fecha, hora de salida, avion, tripulacion, precios y boletos. Bloquea
 * la edicion si faltan menos de 48 horas para la salida y muestra advertencia si faltan menos de 72h.
 * Al guardar llama a PUT /api/admin/vuelos/:id.
 */
  import { onMount } from 'svelte';
  import { API } from '../../lib/api.js';

  /** Objeto de vuelo con los datos actuales a editar. @type {any} */
  export let vuelo;

  /** Lista de aeropuertos disponibles para los dropdowns de ruta. @type {any[]} */
  export let aeropuertos = [];

  /** Lista de aviones de la flota para el dropdown de aeronave. @type {any[]} */
  export let aviones = [];

  /** Lista de tripulantes para el dropdown de asignacion. @type {any[]} */
  export let tripulacion = [];

  /** Funcion para mostrar una notificacion toast. @type {Function} */
  export let mostrarToast;

  /** Funcion invocada al cerrar el modal sin guardar. @type {Function} */
  export let onClose;

  /** Funcion invocada tras guardar exitosamente. @type {Function} */
  export let onGuardado;

  // ── Estado de busqueda y dropdowns ──────────────────────────────────────────
  let busquedaAvion         = '';
  let busquedaTripulante    = '';
  let mostrarDropdownAvion      = false;
  let mostrarDropdownTripulante = false;

  // ── Datos del formulario (se rellenan en onMount desde vuelo) ───────────────
  let form = {
    fecha:                   '',
    horaSalida:              '',
    avionId:                 '',
    motivo:                  '',
    tripulantesSeleccionados: []
  };

  // ── Estado de alertas de tiempo ─────────────────────────────────────────────
  /** true si faltan < 60 dias para la salida (edicion bloqueada). @type {boolean} */
  let bloqueadoPorTiempo = false;
  /** Dias que faltan para la salida. @type {number} */
  let diasParaSalida = Infinity;
  /** Horas que faltan para la salida (para mostrar en el banner). @type {number} */
  let horasParaSalida = Infinity;

  // ── Disponibilidad ───────────────────────────────────────────────────────────
  let avionesOcupadosIds     = new Set();
  let tripulantesOcupadosIds = new Set();
  let cargandoDisponibilidad = false;

  // ── Preview llegada ──────────────────────────────────────────────────────────
  let previewLlegada       = null;
  let loadingPreview       = false;
  let previewDebounceTimer = null;
  let calculandoLlegada    = false;
  let horaLlegadaCalculada = null;

  // ── Guardado ─────────────────────────────────────────────────────────────────
  let guardando = false;

  // ── Reactivos: filtrado de dropdowns ────────────────────────────────────────
  $: avionesFiltrados = aviones.filter(a => {
    const coincide =
      a.nombreCompleto.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.marca.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.modelo.toLowerCase().includes(busquedaAvion.toLowerCase());
    // Excluir ocupados, EXCEPTO el avion que ya tiene el vuelo
    const esElActual = a.id === parseInt(form.avionId);
    return coincide && (esElActual || !avionesOcupadosIds.has(a.id));
  });

  $: tripulantesFiltrados = tripulacion.filter(t => {
    const yaSeleccionado = form.tripulantesSeleccionados.some(ts => ts.id === t.id);
    const coincide =
      t.nombreCompleto.toLowerCase().includes(busquedaTripulante.toLowerCase()) ||
      t.nombreRol.toLowerCase().includes(busquedaTripulante.toLowerCase());
    // Excluir ocupados, EXCEPTO los que ya pertenecen a este vuelo
    const esDelVuelo = form.tripulantesSeleccionados.some(ts => ts.id === t.id);
    return !yaSeleccionado && coincide && (esDelVuelo || !tripulantesOcupadosIds.has(t.id));
  });

  $: avionSeleccionado = aviones.find(a => a.id === parseInt(form.avionId));

  // Composicion de tripulacion
  $: pilotos    = form.tripulantesSeleccionados.filter(t => t.rolID === 1).length;
  $: copilotos  = form.tripulantesSeleccionados.filter(t => t.rolID === 2).length;
  $: auxiliares = form.tripulantesSeleccionados.filter(t => t.rolID === 3).length;
  $: totalTripulantes = form.tripulantesSeleccionados.length;
  $: tripulacionCompleta = pilotos >= 1 && copilotos >= 1 && auxiliares >= 3 && totalTripulantes === 5;

  // Recalcular preview cuando cambia fecha u hora
  $: { form.fecha; form.horaSalida; if (form.fecha && form.horaSalida) calcularPreviewLlegada(); }

  // Recalcular disponibilidad cuando cambia fecha u hora
  $: { form.fecha; form.horaSalida; if (form.fecha && form.horaSalida) cargarDisponibilidad(); }

  // ── Ciclo de vida ────────────────────────────────────────────────────────────
  onMount(async () => {
    // Prefill form desde el objeto vuelo
    form.fecha      = vuelo.fecha ?? '';
    form.horaSalida = vuelo.horaSalida ?? '';
    form.avionId    = String(vuelo.avionId ?? '');

    // Calcular horas hasta la salida
    calcularHorasParaSalida();

    // Busqueda del avion en el texto del input
    const avionActual = aviones.find(a => a.id === parseInt(form.avionId));
    if (avionActual) busquedaAvion = avionActual.nombreCompleto;

    // Cargar tripulantes del vuelo desde el backend
    await cargarTripulantesDelVuelo();

    // Cargar disponibilidad (excluyendo este vuelo)
    if (form.fecha && form.horaSalida) await cargarDisponibilidad();

    // Calcular preview de llegada
    if (form.fecha && form.horaSalida) calcularPreviewLlegada();
  });

  // ── Funciones auxiliares ──────────────────────────────────────────────────────

  /**
   * Calcula cuantas horas faltan para la salida del vuelo y actualiza las flags de bloqueo/advertencia.
   */
  function calcularHorasParaSalida() {
    if (!vuelo.fecha || !vuelo.horaSalida) { diasParaSalida = Infinity; horasParaSalida = Infinity; return; }
    const salidaStr = `${vuelo.fecha}T${vuelo.horaSalida}:00`;
    const salida = new Date(salidaStr);
    const ahora  = new Date();
    horasParaSalida = (salida - ahora) / (1000 * 60 * 60);
    diasParaSalida  = horasParaSalida / 24;
    bloqueadoPorTiempo = diasParaSalida < 60;
  }

  /**
   * Carga los tripulantes actualmente asignados a este vuelo desde el backend.
   * @async
   */
  async function cargarTripulantesDelVuelo() {
    try {
      const r = await fetch(`${API}/api/admin/vuelos/${vuelo.id}/tripulantes`, { credentials: 'include' });
      if (r.ok) {
        const data = await r.json();
        // Mapear a la estructura que usa el formulario (igual que tripulacion[])
        form.tripulantesSeleccionados = data.map(t => ({
          id:           t.id ?? t.tripulanteId,
          nombreCompleto: t.nombreCompleto ?? t.nombre,
          nombreRol:    t.nombreRol ?? t.rol,
          rolID:        t.rolID ?? t.rolId,
          imagenBase64: t.imagenBase64 ?? null
        }));
      } else {
        // Si el endpoint no existe, intentar usar tripulacion ya cargada filtrando por IDs del vuelo
        if (Array.isArray(vuelo.tripulantesIds)) {
          form.tripulantesSeleccionados = tripulacion.filter(t => vuelo.tripulantesIds.includes(t.id));
        }
      }
    } catch {
      // Fallback: usar prop de tripulacion si vuelo trae los IDs
      if (Array.isArray(vuelo.tripulantesIds)) {
        form.tripulantesSeleccionados = tripulacion.filter(t => vuelo.tripulantesIds.includes(t.id));
      }
    }
  }

  /**
   * Obtiene aviones y tripulantes ya ocupados para la fecha/hora, excluyendo este vuelo.
   * @async
   */
  async function cargarDisponibilidad() {
    if (!form.fecha || !form.horaSalida) return;
    cargandoDisponibilidad = true;
    try {
      const urlAviones = `${API}/api/admin/vuelos/aviones-ocupados?fecha=${form.fecha}&horaSalida=${form.horaSalida}&excluirVueloId=${vuelo.id}`;
      const urlTrip    = `${API}/api/admin/vuelos/tripulantes-ocupados?fecha=${form.fecha}&horaSalida=${form.horaSalida}&excluirVueloId=${vuelo.id}`;
      const [rA, rT] = await Promise.all([
        fetch(urlAviones, { credentials: 'include' }),
        fetch(urlTrip,    { credentials: 'include' })
      ]);
      if (rA.ok) avionesOcupadosIds     = new Set(await rA.json());
      if (rT.ok) tripulantesOcupadosIds = new Set(await rT.json());
    } catch(e) { console.error('Error cargando disponibilidad', e); }
    finally { cargandoDisponibilidad = false; }
  }

  /**
   * Calcula con debounce la hora estimada de llegada usando el endpoint del backend.
   */
  function calcularPreviewLlegada() {
    if (!vuelo.aeropuertoOrigenId || !vuelo.aeropuertoDestinoId || !form.fecha || !form.horaSalida) {
      previewLlegada = null; horaLlegadaCalculada = null; return;
    }
    clearTimeout(previewDebounceTimer);
    previewDebounceTimer = setTimeout(async () => {
      loadingPreview = true; previewLlegada = null;
      calculandoLlegada = true; horaLlegadaCalculada = null;
      const controller = new AbortController();
      const tid = setTimeout(() => controller.abort(), 8000);
      try {
        const r = await fetch(`${API}/api/rutas/calcular-llegada`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          signal: controller.signal,
          body: JSON.stringify({
            aeropuertoOrigenId:  parseInt(vuelo.aeropuertoOrigenId),
            aeropuertoDestinoId: parseInt(vuelo.aeropuertoDestinoId),
            fechaSalida: form.fecha,
            horaSalida:  form.horaSalida
          })
        });
        if (r.ok) {
          previewLlegada = await r.json();
          horaLlegadaCalculada = previewLlegada?.horaLlegada ?? null;
        } else { previewLlegada = null; horaLlegadaCalculada = null; }
      } catch { previewLlegada = null; horaLlegadaCalculada = null; }
      finally { clearTimeout(tid); loadingPreview = false; calculandoLlegada = false; }
    }, 600);
  }

  function seleccionarAvion(a) {
    form.avionId = a.id;
    busquedaAvion = a.nombreCompleto;
    mostrarDropdownAvion = false;
  }

  function agregarTripulante(t) {
    form.tripulantesSeleccionados = [...form.tripulantesSeleccionados, t];
    busquedaTripulante = '';
    mostrarDropdownTripulante = false;
  }

  function quitarTripulante(id) {
    form.tripulantesSeleccionados = form.tripulantesSeleccionados.filter(t => t.id !== id);
  }

  /**
   * Valida los campos y envia PUT /api/admin/vuelos/:id con los datos modificados.
   * @async
   */
  async function handleGuardar() {
    if (bloqueadoPorTiempo) { mostrarToast('error', `No se puede editar: faltan ${Math.round(diasParaSalida)} días. Se requieren mínimo 60 días de anticipación.`); return; }
    if (!form.fecha)         { mostrarToast('error', 'Ingresa la fecha del vuelo'); return; }
    if (!form.horaSalida)    { mostrarToast('error', 'Ingresa la hora de salida'); return; }
    if (!form.avionId)       { mostrarToast('error', 'Selecciona un avion'); return; }
    if (!form.motivo?.trim()) { mostrarToast('error', 'El motivo del cambio es obligatorio'); return; }
    if (totalTripulantes !== 5) { mostrarToast('error', 'Debe asignar exactamente 5 tripulantes al vuelo'); return; }
    if (pilotos < 1)    { mostrarToast('error', 'Falta asignar 1 Piloto (Rol 1)'); return; }
    if (copilotos < 1)  { mostrarToast('error', 'Falta asignar 1 Copiloto (Rol 2)'); return; }
    if (auxiliares < 3) { mostrarToast('error', 'Faltan Auxiliares de vuelo — minimo 3 (Rol 3)'); return; }

    guardando = true;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/${vuelo.id}`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fecha:          form.fecha,
          horaSalida:     form.horaSalida,
          avionId:        parseInt(form.avionId),
          motivo:         form.motivo.trim(),
          tripulantesIds: form.tripulantesSeleccionados.map(t => t.id)
        })
      });
      if (r.ok) {
        mostrarToast('success', 'Vuelo actualizado exitosamente. Se notificó a los pasajeros.');
        onGuardado();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al actualizar el vuelo');
      }
    } catch { mostrarToast('error', 'Error de conexion al actualizar el vuelo'); }
    finally { guardando = false; }
  }

  /**
   * Auto-rellena la tripulacion con tripulantes disponibles (no ocupados en esta fecha/hora).
   * Selecciona aleatoriamente 1 piloto, 1 copiloto y 3 auxiliares de entre los disponibles.
   * Si no hay suficientes para algún rol, muestra error y no modifica la seleccion.
   */
  function autorellenarTripulantes() {
    const disponibles = tripulacion.filter(t =>
      t.activo !== false &&
      !tripulantesOcupadosIds.has(t.id)
    );
    const pilotos_d    = disponibles.filter(t => t.rolID === 1);
    const copilotos_d  = disponibles.filter(t => t.rolID === 2);
    const auxiliares_d = disponibles.filter(t => t.rolID === 3);

    if (pilotos_d.length < 1)    { mostrarToast('error', 'No hay pilotos disponibles para esta fecha'); return; }
    if (copilotos_d.length < 1)  { mostrarToast('error', 'No hay copilotos disponibles para esta fecha'); return; }
    if (auxiliares_d.length < 3) { mostrarToast('error', `No hay suficientes auxiliares disponibles (${auxiliares_d.length}/3)`); return; }

    const rand = arr => arr[Math.floor(Math.random() * arr.length)];
    const piloto   = rand(pilotos_d);
    const copiloto = rand(copilotos_d);
    const pool  = auxiliares_d.filter(a => a.id !== piloto.id && a.id !== copiloto.id);
    const auxSels = [];
    const usados  = new Set();
    while (auxSels.length < 3 && pool.length > 0) {
      const a = rand(pool);
      if (!usados.has(a.id)) { usados.add(a.id); auxSels.push(a); }
      if (usados.size >= pool.length) break;
    }
    if (auxSels.length < 3) { mostrarToast('error', 'No hay suficientes auxiliares distintos disponibles'); return; }

    form.tripulantesSeleccionados = [piloto, copiloto, ...auxSels];
    mostrarToast('success', 'Tripulacion autocompletada: 1P · 1C · 3A');
  }

  /** Cierra el modal al hacer click en el overlay (fuera del dialogo). */
  function handleOverlayClick(e) {
    if (e.target === e.currentTarget) onClose();
  }
</script>

<!-- Overlay del modal -->
<!-- svelte-ignore a11y-click-events-have-key-events -->
<!-- svelte-ignore a11y-no-static-element-interactions -->
<div class="editar-overlay" on:click={handleOverlayClick} role="dialog" aria-modal="true" aria-label="Editar vuelo">
  <div class="editar-dialog">

    <!-- Encabezado del modal -->
    <div class="editar-dialog__header">
      <div>
        <h2 class="editar-dialog__title">Editar Vuelo</h2>
        <p class="editar-dialog__subtitle">
          {vuelo.numeroVuelo} &mdash; {vuelo.origen} &rarr; {vuelo.destino}
        </p>
      </div>
      <button class="editar-dialog__close" on:click={onClose} aria-label="Cerrar">&times;</button>
    </div>

    <!-- Banner: bloqueado por tiempo -->
    {#if bloqueadoPorTiempo}
      <div class="editar-banner editar-banner--error">
        <strong>Edicion bloqueada</strong> — Faltan {Math.round(diasParaSalida)} días para la salida.
        Se requieren mínimo 60 días de anticipación para editar un vuelo.
      </div>
    {:else}
      <div class="editar-banner editar-banner--warn">
        <strong>Atencion</strong> — Los cambios notificarán por correo a todos los pasajeros con reservacion activa.
        No se puede modificar el precio ni el código del vuelo.
      </div>
    {/if}

    <!-- Cuerpo del formulario -->
    <div class="editar-dialog__body">
      <form class="admin-form" on:submit|preventDefault={handleGuardar}>

        <!-- Grupo: Fecha y hora -->
        <div class="admin-form__group">
          <h3 class="admin-form__group-title">Fecha y Horario</h3>
          <div class="admin-form__row">
            <div class="admin-form__field">
              <label for="ev-fecha" class="admin-form__label">Fecha del Vuelo *</label>
              <input type="date" id="ev-fecha" class="admin-form__input"
                bind:value={form.fecha}
                disabled={bloqueadoPorTiempo} required />
            </div>
            <div class="admin-form__field">
              <label for="ev-hora" class="admin-form__label">Hora de Salida *</label>
              <input type="time" id="ev-hora" class="admin-form__input"
                bind:value={form.horaSalida}
                disabled={bloqueadoPorTiempo} required />
              <small class="img-hint">Hora local en el aeropuerto de origen</small>
            </div>
          </div>

          <!-- Preview llegada -->
          <div class="admin-form__field" style="margin-top:1rem">
            <p class="admin-form__label">Hora de Llegada Estimada</p>
            {#if loadingPreview || calculandoLlegada}
              <div class="llegada-preview llegada-preview--loading">
                <div class="llegada-loader">
                  <span class="llegada-loader__plane">✈</span>
                  <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                  <span class="llegada-loader__text">Calculando hora de llegada...</span>
                </div>
              </div>
            {:else if previewLlegada}
              <div class="llegada-preview" class:llegada-preview--tz={previewLlegada.usoZonasHorarias}>
                <span class="llegada-preview__time">
                  {previewLlegada.horaLlegada}
                  {#if previewLlegada.fechaLlegada !== form.fecha}
                    <span class="llegada-preview__nextday">(+1 dia)</span>
                  {/if}
                </span>
                <span class="llegada-preview__meta">{previewLlegada.duracionMinutos} min ·
                  {#if previewLlegada.usoZonasHorarias}
                    <span class="tz-badge tz-badge--ok">✔ Con zona horaria</span>
                  {:else}
                    <span class="tz-badge tz-badge--missing">Sin zona horaria</span>
                  {/if}
                </span>
                <small class="llegada-preview__nota">{previewLlegada.nota}</small>
              </div>
            {:else}
              <div class="llegada-preview llegada-preview--empty">
                Se calcula automaticamente al completar fecha y hora de salida
              </div>
            {/if}
          </div>
        </div>

        <!-- Grupo: Aeronave -->
        <div class="admin-form__group">
          <h3 class="admin-form__group-title">Aeronave</h3>
          <div class="admin-form__field admin-form__field--full">
            <label for="ev-avion" class="admin-form__label">Seleccionar Avion *</label>
            {#if avionesOcupadosIds.size > 0}
              <small class="disponibilidad-hint disponibilidad-hint--info">
                {avionesOcupadosIds.size} avion(es) ya asignado(s) a otro vuelo en esta fecha no aparecen en la lista
              </small>
            {/if}
            <div class="searchable-select">
              <input id="ev-avion" type="text" class="admin-form__input" bind:value={busquedaAvion}
                on:focus={() => mostrarDropdownAvion = true}
                on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
                placeholder="Buscar avion..." autocomplete="off"
                disabled={bloqueadoPorTiempo} />
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
                  <p class="searchable-select__empty">Todos los aviones estan ocupados para la fecha seleccionada</p>
                </div>
              {/if}
              {#if avionSeleccionado}<p class="selected-item">✔ {avionSeleccionado.nombreCompleto}</p>{/if}
            </div>
          </div>
        </div>

        <!-- Grupo: Capacidad del avion (solo informativo) -->
        {#if avionSeleccionado}
          <div class="admin-form__group">
            <h3 class="admin-form__group-title">Capacidad de la Aeronave</h3>
            <div class="admin-form__field admin-form__field--full">
              <p style="font-size:.88rem;color:var(--text-muted);margin:0">
                El avion seleccionado tiene capacidad para <strong>{avionSeleccionado.capacidadPasajeros} pasajeros</strong>.
                El número de boletos y los precios no pueden modificarse.
              </p>
            </div>
          </div>
        {/if}

        <!-- Grupo: Motivo del cambio -->
        <div class="admin-form__group">
          <h3 class="admin-form__group-title">Motivo del Cambio *</h3>
          <div class="admin-form__field admin-form__field--full">
            <label for="ev-motivo" class="admin-form__label">Motivo (se enviará a todos los pasajeros)</label>
            <textarea id="ev-motivo" class="admin-form__input" rows="3"
              bind:value={form.motivo}
              disabled={bloqueadoPorTiempo}
              placeholder="Ej: Cambio de aeronave por mantenimiento programado..."
              required></textarea>
            <small class="img-hint">Este mensaje se incluirá en el correo de aviso enviado a todos los pasajeros afectados.</small>
          </div>
        </div>

        <!-- Grupo: Tripulacion -->
        <div class="admin-form__group">
          <h3 class="admin-form__group-title">Tripulacion</h3>
          <div class="admin-form__field admin-form__field--full">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:.5rem;">
              <label for="ev-trip" class="admin-form__label" style="margin-bottom:0">Agregar Tripulantes</label>
              {#if !bloqueadoPorTiempo}
                <button type="button"
                  style="padding:.35rem .85rem;font-size:.8rem;font-weight:600;background:#D4AF37;color:#1C1A18;border:none;border-radius:6px;cursor:pointer;"
                  on:click={autorellenarTripulantes}
                  title="Auto-rellena con 1 piloto, 1 copiloto y 3 auxiliares disponibles">
                  &#9881; Auto-rellenar Tripulacion
                </button>
              {/if}
            </div>
            {#if tripulantesOcupadosIds.size > 0}
              <small class="disponibilidad-hint disponibilidad-hint--warn">
                {tripulantesOcupadosIds.size} tripulante(s) ya asignado(s) a otro vuelo y no aparecen en la lista
              </small>
            {/if}
            <div class="searchable-select">
              <input id="ev-trip" type="text" class="admin-form__input" bind:value={busquedaTripulante}
                on:focus={() => mostrarDropdownTripulante = true}
                on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
                placeholder="Buscar por nombre o rol..." autocomplete="off"
                disabled={bloqueadoPorTiempo} />
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
              {:else if mostrarDropdownTripulante && tripulantesFiltrados.length === 0 && tripulacion.length > 0}
                <div class="searchable-select__dropdown">
                  <p class="searchable-select__empty">Ningun tripulante disponible.<br><small>Deben pasar 24h desde su vuelo anterior.</small></p>
                </div>
              {/if}
            </div>

            {#if form.tripulantesSeleccionados.length > 0}
              <div class="tripulantes-seleccionados">
                <p class="tripulantes-seleccionados__title">Tripulantes seleccionados ({form.tripulantesSeleccionados.length})</p>
                <div class="tripulantes-seleccionados__list">
                  {#each form.tripulantesSeleccionados as t}
                    <div class="tripulante-item">
                      <div class="tripulante-item__info">
                        <span class="tripulante-item__name">{t.nombreCompleto}</span>
                        <span class="tripulante-item__rol">{t.nombreRol}</span>
                      </div>
                      {#if !bloqueadoPorTiempo}
                        <button type="button" class="tripulante-item__remove" on:click={() => quitarTripulante(t.id)}>×</button>
                      {/if}
                    </div>
                  {/each}
                </div>
              </div>
              <div class="crew-checklist">
                <div class="crew-check-item" class:ok={pilotos >= 1} class:missing={pilotos === 0}>
                  <span class="crew-check-icon">{pilotos >= 1 ? '✓' : '○'}</span>
                  Piloto: {pilotos}/1
                </div>
                <div class="crew-check-item" class:ok={copilotos >= 1} class:missing={copilotos === 0}>
                  <span class="crew-check-icon">{copilotos >= 1 ? '✓' : '○'}</span>
                  Copiloto: {copilotos}/1
                </div>
                <div class="crew-check-item" class:ok={auxiliares >= 3} class:missing={auxiliares < 3}>
                  <span class="crew-check-icon">{auxiliares >= 3 ? '✓' : '○'}</span>
                  Auxiliares: {auxiliares}/3
                </div>
                <div class="crew-check-item" class:ok={totalTripulantes === 5} class:missing={totalTripulantes !== 5}>
                  <span class="crew-check-icon">{totalTripulantes === 5 ? '✓' : '○'}</span>
                  Total: {totalTripulantes}/5
                </div>
              </div>
            {/if}
          </div>
        </div>

        <!-- Acciones -->
        <div class="admin-form__actions">
          <button type="submit" class="admin-form__submit"
            disabled={bloqueadoPorTiempo || guardando || calculandoLlegada}>
            {#if guardando}Guardando...{:else if calculandoLlegada}Calculando llegada...{:else}Guardar Cambios{/if}
          </button>
          <button type="button" class="admin-form__cancel" on:click={onClose}>
            Cancelar
          </button>
        </div>

      </form>
    </div>
  </div>
</div>

<style>
/* Overlay del modal */
.editar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  z-index: 1000;
  padding: 2rem 1rem;
  overflow-y: auto;
}

/* Dialogo principal */
.editar-dialog {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0,0,0,.22);
  width: 100%;
  max-width: 780px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Encabezado */
.editar-dialog__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 1.5rem 1.75rem 1rem;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}
.editar-dialog__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 .2rem;
}
.editar-dialog__subtitle {
  font-size: .85rem;
  color: #6b7280;
  margin: 0;
}
.editar-dialog__close {
  background: none;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  color: #6b7280;
  cursor: pointer;
  padding: .25rem .5rem;
  border-radius: 6px;
  transition: background .15s;
}
.editar-dialog__close:hover { background: #f3f4f6; color: #111; }

/* Banners de alerta */
.editar-banner {
  padding: .875rem 1.75rem;
  font-size: .875rem;
  line-height: 1.5;
}
.editar-banner--error {
  background: #fef2f2;
  color: #b91c1c;
  border-bottom: 2px solid #fca5a5;
}
.editar-banner--warn {
  background: #fffbeb;
  color: #92400e;
  border-bottom: 2px solid #fcd34d;
}

/* Cuerpo scrolleable */
.editar-dialog__body {
  padding: 1.5rem 1.75rem 2rem;
  overflow-y: auto;
  max-height: calc(90vh - 140px);
}

/* Checklist de tripulacion */
.crew-checklist {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: .5rem;
  margin-top: .75rem;
  padding: .75rem;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #eee;
}
.crew-check-item {
  display: flex;
  align-items: center;
  gap: .4rem;
  font-size: .8rem;
  padding: .4rem .6rem;
  border-radius: 6px;
}
.crew-check-item.ok      { background: #e6f7ee; color: #1a7a3f; }
.crew-check-item.missing { background: #fef2f2; color: #b91c1c; }
.crew-check-icon { font-weight: 700; }
</style>
