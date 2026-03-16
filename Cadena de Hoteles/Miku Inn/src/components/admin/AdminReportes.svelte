<script>
  import { onMount } from 'svelte';

  export let API_BASE;
  export let badge;

  let reporteTab = 'listado';

  // ── Listado búsquedas ──
  let busquedasLog = [];
  let cargandoBusquedas = false;
  let errorBusquedas = null;

  // ── Filtros (frontend) ──
  let filtroPais = '';
  let filtroCiudad = '';
  let filtroUsuarioAgencia = '';
  let filtroTipo = 'todos';
  let filtroFechaDesde = '';
  let filtroFechaHasta = '';

  // Autocomplete países/ciudades
  let todosLosPaises = [];
  let paisSugerencias = [];
  let ciudadSugerencias = [];
  let paisSeleccionado = null;

  // ── Exportar ──
  let showModalExportar = false;
  let emailExportar = '';
  let exportando = false;
  let mensajeExportar = null;

  // ── Dashboard ──
  let dashBusquedasResumen = null;
  let cargandoDashBusquedas = false;
  let metricas = null;
  let hoteles = [];
  let reservas = [];

  // Filtrado reactivo frontend
  $: busquedasFiltradas = busquedasLog.filter(b => {
    const matchPais = !filtroPais.trim() || (b.destino ?? b.ciudad ?? '').toLowerCase().includes(filtroPais.toLowerCase().trim());
    const matchCiudad = !filtroCiudad.trim() || (b.destino ?? b.ciudad ?? '').toLowerCase().includes(filtroCiudad.toLowerCase().trim());
    const matchUsuario = !filtroUsuarioAgencia.trim() || (b.usuario ?? b.usuarioAgencia ?? b.agencia ?? '').toLowerCase().includes(filtroUsuarioAgencia.toLowerCase().trim());
    const matchTipo = filtroTipo === 'todos' || (b.tipo ?? '').toLowerCase() === filtroTipo.toLowerCase();

    let matchFechaDesde = true;
    if (filtroFechaDesde) {
      const fechaBusqueda = (b.fechaHora ?? '').slice(0, 10);
      matchFechaDesde = fechaBusqueda >= filtroFechaDesde;
    }
    let matchFechaHasta = true;
    if (filtroFechaHasta) {
      const fechaBusqueda = (b.fechaHora ?? '').slice(0, 10);
      matchFechaHasta = fechaBusqueda <= filtroFechaHasta;
    }

    return matchPais && matchCiudad && matchUsuario && matchTipo && matchFechaDesde && matchFechaHasta;
  });

  $: hayFiltrosActivos = filtroPais.trim() || filtroCiudad.trim() || filtroUsuarioAgencia.trim() || filtroTipo !== 'todos' || filtroFechaDesde || filtroFechaHasta;

  // Dashboard reactives
  $: busquedasPorDia = dashBusquedasResumen?.porDia ?? [];
  $: busquedasPorTipo = { web: dashBusquedasResumen?.totalWeb ?? 0, rest: dashBusquedasResumen?.totalRest ?? 0 };
  $: topDestinos = dashBusquedasResumen?.topDestinos ?? [];
  $: maxBusquedasDia = busquedasPorDia.length ? Math.max(...busquedasPorDia.map(d => d.total), 1) : 1;
  $: maxTopDestino = topDestinos.length ? Math.max(...topDestinos.map(d => d.total), 1) : 1;
  $: totalBusquedasTipo = (busquedasPorTipo.web + busquedasPorTipo.rest) || 1;
  $: pctWeb = Math.round((busquedasPorTipo.web / totalBusquedasTipo) * 100);
  $: pctRest = 100 - pctWeb;

  onMount(() => {
    cargarBusquedas();
    cargarDashBusquedas();
    cargarMetricas();
    cargarHoteles();
    cargarReservas();
    cargarPaisesAutocomplete();
  });

  async function cargarPaisesAutocomplete() {
    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data ?? [];
    } catch (e) { /* silencioso */ }
  }

  // Autocomplete País
  function onPaisInput() {
    paisSeleccionado = null;
    filtroCiudad = '';
    ciudadSugerencias = [];
    const q = filtroPais.toLowerCase().trim();
    if (q.length < 2) { paisSugerencias = []; return; }
    paisSugerencias = todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
  }
  function seleccionarPais(p) {
    paisSeleccionado = p;
    filtroPais = p.country;
    paisSugerencias = [];
    filtroCiudad = '';
    ciudadSugerencias = [];
  }
  function validarPais() {
    if (filtroPais.trim() && !paisSeleccionado) {
      // Intentar match exacto
      const match = todosLosPaises.find(p => p.country.toLowerCase() === filtroPais.toLowerCase().trim());
      if (match) { paisSeleccionado = match; filtroPais = match.country; }
    }
  }

  // Autocomplete Ciudad
  function onCiudadInput() {
    const q = filtroCiudad.toLowerCase().trim();
    if (q.length < 2 || !paisSeleccionado) { ciudadSugerencias = []; return; }
    ciudadSugerencias = (paisSeleccionado.cities ?? []).filter(c => c.toLowerCase().includes(q)).slice(0, 6);
  }
  function seleccionarCiudad(c) {
    filtroCiudad = c;
    ciudadSugerencias = [];
  }

  function limpiarFiltros() {
    filtroPais = ''; filtroCiudad = ''; filtroUsuarioAgencia = ''; filtroTipo = 'todos'; filtroFechaDesde = ''; filtroFechaHasta = '';
    paisSeleccionado = null; paisSugerencias = []; ciudadSugerencias = [];
  }

  // ── API calls ──
  async function cargarBusquedas() {
    cargandoBusquedas = true;
    errorBusquedas = null;
    try {
      const res = await fetch(`${API_BASE}/admin/reportes/busquedas`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const data = await res.json();
      busquedasLog = data.busquedas ?? data ?? [];
    } catch (e) {
      errorBusquedas = 'No se pudieron cargar las búsquedas. ' + e.message;
      busquedasLog = [];
    } finally {
      cargandoBusquedas = false;
    }
  }

  async function cargarDashBusquedas() {
    cargandoDashBusquedas = true;
    try {
      const res = await fetch(`${API_BASE}/admin/reportes/busquedas/resumen`, { credentials: 'include' });
      if (res.ok) dashBusquedasResumen = await res.json();
    } catch (_) { /* silencioso */ }
    finally { cargandoDashBusquedas = false; }
  }

  async function cargarMetricas() {
    try {
      const res = await fetch(`${API_BASE}/admin/metricas`, { credentials: 'include' });
      if (res.ok) metricas = await res.json();
    } catch (_) { /* silencioso */ }
  }

  async function cargarHoteles() {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, { credentials: 'include' });
      if (res.ok) hoteles = await res.json();
    } catch (_) { /* silencioso */ }
  }

  async function cargarReservas() {
    try {
      const res = await fetch(`${API_BASE}/admin/reservaciones`, { credentials: 'include' });
      if (res.ok) reservas = await res.json();
    } catch (_) { /* silencioso */ }
  }

  // ── Exportar ──
  function abrirExportar() { emailExportar = ''; mensajeExportar = null; showModalExportar = true; }
  function cerrarExportar() { showModalExportar = false; mensajeExportar = null; }

  async function confirmarExportar() {
    if (!emailExportar.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailExportar.trim())) {
      mensajeExportar = { tipo: 'error', texto: 'Ingresa un correo electrónico válido.' };
      return;
    }
    exportando = true; mensajeExportar = null;
    try {
      const res = await fetch(`${API_BASE}/admin/reportes/busquedas/exportar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: emailExportar.trim() }),
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      mensajeExportar = { tipo: 'ok', texto: `Se enviará el reporte a ${emailExportar.trim()} en breve.` };
      setTimeout(cerrarExportar, 2500);
    } catch (e) {
      mensajeExportar = { tipo: 'error', texto: 'No se pudo enviar el reporte. ' + e.message };
    } finally { exportando = false; }
  }
</script>

<!-- Sub-tabs -->
<div class="adm__rep-tabs">
  <button class="adm__rep-tab" class:adm__rep-tab--active={reporteTab === 'listado'} on:click={() => reporteTab = 'listado'}>
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
    Listado de Búsquedas
  </button>
  <button class="adm__rep-tab" class:adm__rep-tab--active={reporteTab === 'dashboard'} on:click={() => reporteTab = 'dashboard'}>
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
    Dashboard / Gráficos
  </button>
</div>

<!-- ══ TAB: LISTADO ══ -->
{#if reporteTab === 'listado'}
  <!-- Filtros -->
  <div class="adm__rep-filters">
    <div class="adm__rep-filters-title">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
      Filtros
      {#if hayFiltrosActivos}<span class="adm__rep-filters-badge">Activos</span>{/if}
    </div>
    <div class="adm__rep-filters-grid">
      <!-- País con autocomplete -->
      <div class="adm__rep-filter-field" style="position:relative">
        <label for="f-pais">País</label>
        <input id="f-pais" type="text" bind:value={filtroPais} on:input={onPaisInput} on:blur={() => setTimeout(() => { paisSugerencias = []; validarPais(); }, 150)} placeholder="Ej: Guatemala" autocomplete="off" />
        {#if paisSugerencias.length > 0}
          <ul class="adm__autocomplete-list">
            {#each paisSugerencias as p}
              <li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarPais(p)}>{p.country}</button></li>
            {/each}
          </ul>
        {/if}
      </div>
      <!-- Ciudad con autocomplete -->
      <div class="adm__rep-filter-field" style="position:relative">
        <label for="f-ciudad">Ciudad</label>
        <input id="f-ciudad" type="text" bind:value={filtroCiudad} on:input={onCiudadInput} on:blur={() => setTimeout(() => { ciudadSugerencias = []; }, 150)} placeholder={paisSeleccionado ? 'Ej: Guatemala City' : 'Primero selecciona un país'} disabled={!paisSeleccionado} autocomplete="off" />
        {#if ciudadSugerencias.length > 0}
          <ul class="adm__autocomplete-list">
            {#each ciudadSugerencias as c}
              <li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarCiudad(c)}>{c}</button></li>
            {/each}
          </ul>
        {/if}
      </div>
      <div class="adm__rep-filter-field">
        <label for="f-ua">Usuario o Agencia</label>
        <input id="f-ua" type="text" bind:value={filtroUsuarioAgencia} placeholder="username o nombre agencia" />
      </div>
      <div class="adm__rep-filter-field">
        <label for="f-tipo">Tipo de búsqueda</label>
        <select id="f-tipo" bind:value={filtroTipo}>
          <option value="todos">Todos</option>
          <option value="web">Portal Web</option>
          <option value="rest">Servicio REST</option>
        </select>
      </div>
      <div class="adm__rep-filter-field">
        <label for="f-desde">Fecha desde</label>
        <input id="f-desde" type="date" bind:value={filtroFechaDesde} />
      </div>
      <div class="adm__rep-filter-field">
        <label for="f-hasta">Fecha hasta</label>
        <input id="f-hasta" type="date" bind:value={filtroFechaHasta} />
      </div>
    </div>
    <div class="adm__rep-filters-actions">
      {#if hayFiltrosActivos}
        <button class="adm__btn adm__btn--ghost" on:click={limpiarFiltros}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          Limpiar filtros
        </button>
      {/if}
      <div class="adm__rep-filters-spacer"></div>
      <button class="adm__btn adm__rep-export-btn" on:click={abrirExportar} disabled={busquedasLog.length === 0}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
        Exportar por correo
      </button>
    </div>
  </div>

  <!-- Info resultados -->
  <div class="adm__rep-results-info">
    {#if cargandoBusquedas}
      <span class="adm__rep-results-label">
        <svg class="adm__spinner adm__spinner--sm" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
        Cargando...
      </span>
    {:else if errorBusquedas}
      <span class="adm__rep-results-label" style="color: var(--adm-red)">{errorBusquedas}</span>
    {:else}
      <span class="adm__rep-results-label">
        Mostrando {busquedasFiltradas.length} de {busquedasLog.length} resultado{busquedasLog.length !== 1 ? 's' : ''}
        {#if hayFiltrosActivos} · con filtros activos{/if}
      </span>
    {/if}
  </div>

  <!-- Tabla -->
  <div class="adm__card" style="padding:0; overflow:hidden">
    <div class="adm__table-wrap">
      <table class="adm__table adm__rep-table">
        <thead>
          <tr><th>ID</th><th>País / Ciudad</th><th>Check-in</th><th>Check-out</th><th>Personas</th><th>Usuario / Agencia</th><th>Tipo</th><th>Fecha y hora</th></tr>
        </thead>
        <tbody>
          {#if cargandoBusquedas}
            <tr><td colspan="8" class="adm__empty-cell">Cargando búsquedas...</td></tr>
          {:else if busquedasFiltradas.length === 0}
            <tr><td colspan="8" class="adm__empty-cell">{hayFiltrosActivos ? 'Sin resultados para los filtros aplicados.' : 'No hay búsquedas registradas aún.'}</td></tr>
          {:else}
            {#each busquedasFiltradas as b}
              <tr>
                <td class="adm__table-mono" style="font-size:.8rem; color: var(--adm-text-muted)">{b.id ?? '—'}</td>
                <td style="font-weight:500">{b.destino ?? b.ciudad ?? '—'}</td>
                <td class="adm__table-mono">{b.checkIn ? b.checkIn.slice(0,10) : '—'}</td>
                <td class="adm__table-mono">{b.checkOut ? b.checkOut.slice(0,10) : '—'}</td>
                <td style="text-align:center">{b.personas ?? b.numeroPersonas ?? '—'}</td>
                <td>
                  {#if (b.tipo ?? '').toLowerCase() === 'rest'}
                    <span style="color: var(--adm-blue); font-size:.8rem">{b.agencia ?? b.usuarioAgencia ?? '—'}</span>
                  {:else}
                    <span style="color: var(--adm-text-muted); font-size:.85rem">@{b.usuario ?? b.usuarioAgencia ?? '—'}</span>
                  {/if}
                </td>
                <td>
                  {#if (b.tipo ?? '').toLowerCase() === 'rest'}
                    <span class="adm__badge adm__badge--blue" style="font-size:.7rem">REST</span>
                  {:else}
                    <span class="adm__badge adm__badge--green" style="font-size:.7rem">Web</span>
                  {/if}
                </td>
                <td class="adm__table-mono" style="font-size:.8rem; color: var(--adm-text-muted)">
                  {b.fechaHora ? new Date(b.fechaHora).toLocaleString('es-GT', { dateStyle:'short', timeStyle:'short' }) : '—'}
                </td>
              </tr>
            {/each}
          {/if}
        </tbody>
      </table>
    </div>
  </div>

<!-- ══ TAB: DASHBOARD ══ -->
{:else}
  <div class="adm__reportes-grid">

    <!-- Búsquedas Web vs REST -->
    <div class="adm__card adm__reporte-card">
      <h3 class="adm__card-title">Búsquedas: Web vs REST</h3>
      {#if cargandoDashBusquedas}
        <div class="adm__loading-state" style="padding:1.5rem 0"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
      {:else}
        <div class="adm__donut-wrap">
          <div class="adm__donut" role="img" aria-label="Web vs REST" style="background: conic-gradient(#58a6ff 0% {pctWeb}%, #667eea {pctWeb}% 100%);">
            <span class="adm__donut-center" style="font-size:.85rem">{(busquedasPorTipo.web + busquedasPorTipo.rest).toLocaleString('es-GT')}</span>
          </div>
          <div class="adm__donut-legend">
            <div class="adm__legend-item"><span class="adm__legend-dot" style="background:#58a6ff"></span> Portal Web ({busquedasPorTipo.web.toLocaleString('es-GT')} — {pctWeb}%)</div>
            <div class="adm__legend-item"><span class="adm__legend-dot" style="background:#667eea"></span> Servicio REST ({busquedasPorTipo.rest.toLocaleString('es-GT')} — {pctRest}%)</div>
          </div>
        </div>
      {/if}
    </div>

    <!-- Reservas por estado -->
    <div class="adm__card adm__reporte-card">
      <h3 class="adm__card-title">Reservas por Estado</h3>
      {#if metricas}
        {@const confirmadas = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'confirmada')?.total ?? 0}
        {@const pendientes  = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'pendiente')?.total  ?? 0}
        {@const canceladas  = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'cancelada')?.total  ?? 0}
        {@const expiradas   = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'expirada')?.total   ?? 0}
        {@const totalRes = (confirmadas + pendientes + canceladas + expiradas) || 1}
        {@const s1 = (confirmadas / totalRes) * 100}
        {@const s2 = s1 + (pendientes / totalRes) * 100}
        {@const s3 = s2 + (canceladas / totalRes) * 100}
        <div class="adm__donut-wrap">
          <div class="adm__donut" role="img" style="background: conic-gradient(#3fb950 0% {s1.toFixed(2)}%, #d29922 {s1.toFixed(2)}% {s2.toFixed(2)}%, #f85149 {s2.toFixed(2)}% {s3.toFixed(2)}%, #6b7280 {s3.toFixed(2)}% 100%)">
            <span class="adm__donut-center">{confirmadas + pendientes + canceladas + expiradas}</span>
          </div>
          <div class="adm__donut-legend">
            <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--green"></span> Confirmadas ({confirmadas})</div>
            <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--yellow"></span> Pendientes ({pendientes})</div>
            <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--red"></span> Canceladas ({canceladas})</div>
            <div class="adm__legend-item"><span class="adm__legend-dot" style="background:#6b7280"></span> Expiradas ({expiradas})</div>
          </div>
        </div>
      {:else}
        <div class="adm__loading-state" style="padding:1.5rem 0"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
      {/if}
    </div>

    <!-- Top 5 destinos -->
    <div class="adm__card adm__reporte-card adm__reporte-card--wide">
      <div class="adm__card-header"><h3 class="adm__card-title">Top 5 Destinos Más Buscados</h3><span style="font-size:.78rem; color:var(--adm-text-muted)">Últimos 30 días</span></div>
      {#if cargandoDashBusquedas}
        <div class="adm__loading-state" style="padding:1.5rem 0"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
      {:else if topDestinos.length === 0}
        <p style="color:var(--adm-text-muted);font-size:.85rem;text-align:center;padding:1.5rem 0">Sin datos de destinos aún.</p>
      {:else}
        <div class="adm__hotel-bars">
          {#each topDestinos.slice(0, 5) as dest, i}
            <div class="adm__hotel-bar-item">
              <div class="adm__hotel-bar-info">
                <span class="adm__hotel-bar-name"><span style="color:var(--adm-accent);font-weight:700;margin-right:.35rem">#{i+1}</span>{dest.nombre ?? dest.destino ?? dest.ciudad ?? '—'}</span>
                <span class="adm__hotel-bar-pct">{dest.total.toLocaleString('es-GT')} búsquedas</span>
              </div>
              <div class="adm__hotel-bar-track"><div class="adm__hotel-bar-fill" style="width:{Math.round((dest.total / maxTopDestino) * 100)}%;background:linear-gradient(90deg,#667eea,#764ba2)"></div></div>
            </div>
          {/each}
        </div>
      {/if}
    </div>

    <!-- Búsquedas por día -->
    <div class="adm__card adm__reporte-card adm__reporte-card--wide">
      <div class="adm__card-header"><h3 class="adm__card-title">Volumen de Búsquedas por Día</h3><span style="font-size:.78rem;color:var(--adm-text-muted)">Últimos 14 días</span></div>
      {#if cargandoDashBusquedas}
        <div class="adm__loading-state" style="padding:1.5rem 0"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
      {:else if busquedasPorDia.length === 0}
        <p style="color:var(--adm-text-muted);font-size:.85rem;text-align:center;padding:1.5rem 0">Sin datos de volumen diario aún.</p>
      {:else}
        <div class="adm__rep-bar-chart">
          {#each busquedasPorDia.slice(-14) as dia}
            <div class="adm__rep-bar-col">
              <div class="adm__rep-bar-val">{dia.total}</div>
              <div class="adm__rep-bar-track"><div class="adm__rep-bar-fill" style="height:{Math.round((dia.total / maxBusquedasDia) * 100)}%"></div></div>
              <div class="adm__rep-bar-lbl">{new Date((dia.dia ?? dia.fecha) + 'T00:00:00').toLocaleDateString('es-GT', { day:'2-digit', month:'short' })}</div>
            </div>
          {/each}
        </div>
      {/if}
    </div>

    <!-- Resumen general -->
    <div class="adm__card adm__reporte-card">
      <h3 class="adm__card-title">Resumen General</h3>
      {#if metricas}
        <div class="adm__kpi-grid">
          <div class="adm__kpi-item"><p class="adm__kpi-val">$ {(metricas.ingresosTotales ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p><p class="adm__kpi-lbl">Ingresos confirmados</p></div>
          <div class="adm__kpi-item"><p class="adm__kpi-val">{(metricas.totalUsuarios ?? 0).toLocaleString('es-GT')}</p><p class="adm__kpi-lbl">Usuarios registrados</p></div>
          <div class="adm__kpi-item"><p class="adm__kpi-val">{metricas.reservasActivas ?? 0}</p><p class="adm__kpi-lbl">Reservas confirmadas</p></div>
          <div class="adm__kpi-item"><p class="adm__kpi-val">{metricas.reservasTotales ?? 0}</p><p class="adm__kpi-lbl">Reservas totales</p></div>
          <div class="adm__kpi-item"><p class="adm__kpi-val">{metricas.hotelesActivos ?? 0} / {metricas.hotesTotales ?? 0}</p><p class="adm__kpi-lbl">Hoteles activos / total</p></div>
        </div>
      {:else}
        <div class="adm__loading-state" style="padding:1.5rem 0"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
      {/if}
    </div>

    <!-- Hoteles por rating -->
    <div class="adm__card adm__reporte-card">
      <h3 class="adm__card-title">Hoteles por Rating</h3>
      <div class="adm__hotel-bars">
        {#each [...hoteles].sort((a,b) => b.rating - a.rating).slice(0, 6) as h}
          <div class="adm__hotel-bar-item">
            <div class="adm__hotel-bar-info"><span class="adm__hotel-bar-name">{h.nombre}</span><span class="adm__hotel-bar-pct">★ {h.rating?.toFixed(1)}</span></div>
            <div class="adm__hotel-bar-track"><div class="adm__hotel-bar-fill" style="width:{Math.round((h.rating / 5) * 100)}%"></div></div>
          </div>
        {/each}
        {#if hoteles.length === 0}<p style="color:var(--adm-text-muted);font-size:.85rem;text-align:center;padding:1rem 0">Sin hoteles registrados</p>{/if}
      </div>
    </div>

    <!-- Últimas 10 reservas -->
    <div class="adm__card adm__reporte-card adm__reporte-card--wide">
      <div class="adm__card-header"><h3 class="adm__card-title">Últimas 10 Reservas</h3></div>
      <div class="adm__table-wrap">
        <table class="adm__table">
          <thead><tr><th>No. Reserva</th><th>Usuario</th><th>Hotel</th><th>Total</th><th>Estado</th></tr></thead>
          <tbody>
            {#each reservas.slice(0, 10) as r}
              <tr>
                <td class="adm__table-mono">{r.noReservacion}</td>
                <td>@{r.usuario}</td>
                <td>{r.hotel}</td>
                <td class="adm__table-money">$ {(r.total ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
              </tr>
            {/each}
            {#if reservas.length === 0}<tr><td colspan="5" class="adm__empty-cell">Sin reservas</td></tr>{/if}
          </tbody>
        </table>
      </div>
    </div>
  </div>
{/if}

<!-- Modal exportar -->
{#if showModalExportar}
  <div class="adm__overlay" on:click={cerrarExportar} on:keydown={e => e.key === 'Escape' && cerrarExportar()} role="button" tabindex="-1" aria-label="Cerrar"></div>
  <div class="adm__rep-export-modal">
    <div class="adm__rep-export-modal__header">
      <div style="display:flex;align-items:center;gap:.75rem">
        <div class="adm__rep-export-modal__icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg></div>
        <div>
          <p style="font-weight:700;font-size:.95rem;color:#e8f0fe;margin:0">Exportar reporte</p>
          <p style="font-size:.75rem;color:#8eacc8;margin:0">Se enviará como hoja electrónica (.xlsx)</p>
        </div>
      </div>
      <button on:click={cerrarExportar} aria-label="Cerrar" class="adm__rep-export-modal__close">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>
    <div class="adm__rol-modal__body">
      <p style="font-size:.875rem;color:var(--adm-text-muted);margin:0 0 1rem">
        Se exportarán <strong style="color:var(--adm-text)">{busquedasLog.length.toLocaleString('es-GT')} registros</strong> totales.
      </p>
      <div class="adm__field">
        <label for="exp-email">Correo electrónico</label>
        <input id="exp-email" type="email" bind:value={emailExportar} placeholder="correo@ejemplo.com" on:keydown={e => e.key === 'Enter' && confirmarExportar()} />
      </div>
      {#if mensajeExportar}
        <div class="adm__feedback adm__feedback--{mensajeExportar.tipo}" style="margin-top:.75rem">{mensajeExportar.texto}</div>
      {/if}
    </div>
    <div class="adm__rol-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarExportar} disabled={exportando}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={confirmarExportar} disabled={exportando}>
        {#if exportando}Enviando...{:else}Enviar reporte{/if}
      </button>
    </div>
  </div>
{/if}