<script>
/**
 * @file AdminMetricas.svelte
 * @description Seccion de analiticos y reportes del panel de administracion. Obtiene dos conjuntos de datos
 * del backend al montar: un resumen (banner de KPI, grafica de volumen de busquedas diarias, grafica de
 * barras de rutas principales, grafica de dona de ingresos por clase, y division de canal Web vs REST)
 * y un registro de busquedas paginado. Todos los datos se filtran por rango de fechas, tipo de canal y
 * nombre de usuario. El administrador puede aplicar o restablecer filtros y paginar el registro de busquedas.
 * Un modal permite exportar la lista filtrada actual a un correo electronico mediante el endpoint
 * exportar-correo del backend. Todas las graficas SVG se renderizan en linea usando matematica de
 * plantilla de Svelte sin librerias externas de graficas.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Objeto de metricas de resumen devuelto por el endpoint resumen; null mientras carga. @type {any} */
  let metricasResumen = null;

  /** Objeto de registro de busquedas paginado devuelto por el endpoint listado; null mientras carga. @type {any} */
  let metricasListado = null;

  /** Indica si la carga de metricas de resumen esta en progreso. @type {boolean} */
  let loadingMetricas = false;

  /** Indica si la carga del registro de busquedas esta en progreso. @type {boolean} */
  let loadingListado  = false;

  /**
   * Filtro de fecha de inicio inicializado a 30 dias antes de hoy en formato YYYY-MM-DD.
   * @type {string}
   */
  let metFechaDesde = (() => {
    const d = new Date(); d.setDate(d.getDate() - 30);
    return d.toISOString().split('T')[0];
  })();

  /** Filtro de fecha de fin inicializado a hoy en formato YYYY-MM-DD. @type {string} */
  let metFechaHasta = new Date().toISOString().split('T')[0];

  /** Valor del filtro de canal: '' (todos), 'Web' o 'REST'. @type {string} */
  let metTipo       = '';

  /** Filtro de texto de nombre de usuario aplicado a la consulta del registro de busquedas. @type {string} */
  let metUsuario    = '';

  /** Numero de pagina actual para el registro de busquedas paginado. @type {number} */
  let metPagina     = 1;

  /** Controla visibilidad del modal de exportación. @type {boolean} */
  let mostrarModalExportar = false;

  /** Formato seleccionado en el modal: 'excel' | 'csv' | 'email'. @type {string} */
  let expFormato = 'excel';

  /** Correos destino cuando el formato es 'email'. @type {string[]} */
  let correosExportar = [''];

  /** Formato del archivo adjunto cuando se envía por correo: 'excel' | 'csv' | 'pdf'. @type {string} */
  let expFormatoEmail = 'excel';

  /** Secciones a incluir en el export. @type {Object} */
  let expSec = {
    kpi: true, busquedasDiarias: true, canal: true,
    embudo: true, rutas: true, cancelaciones: true,
    tendencia: true, heatmap: true, registro: true
  };

  /** Indica si el export está en progreso. @type {boolean} */
  let enviandoExportar = false;

  /** Período rápido seleccionado: '7d' | '30d' | 'mes' | 'anio' | 'custom'. @type {string} */
  let metPeriodo = '30d';

  /** Panel activo (filtro interno frontend): 'busquedas' | 'negocio' | 'listado'. @type {string} */
  let panelActivo = 'busquedas';


  /** Datos de analisis de negocio (embudo, rutas, cancelaciones, tendencia, heatmap). @type {any} */
  let negocioData = null;

  /** Indica si la carga del analisis de negocio esta en progreso. @type {boolean} */
  let loadingNegocio = false;

  /**
   * Al montar: carga las metricas de resumen y la primera pagina del registro de busquedas en paralelo.
   */
  onMount(() => {
    cargarMetricas();
    cargarListadoBusquedas(1);
    cargarNegocio();
  });

  /**
   * Obtiene las metricas de resumen (KPIs, datos de grafica diaria, rutas principales, distribucion de clase,
   * division de canal) del backend usando los filtros actuales de fecha y canal. Actualiza
   * metricasResumen al tener exito y muestra un toast en caso de error.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarMetricas() {
    loadingMetricas = true;
    try {
      const params = new URLSearchParams({ fechaDesde: metFechaDesde, fechaHasta: metFechaHasta });
      const r = await fetch(`${API}/api/metricas/resumen?${params}`, { credentials: 'include' });
      if (r.ok) metricasResumen = await r.json();
      else mostrarToast('error', 'Error al cargar metricas');
    } catch { mostrarToast('error', 'Error de conexion con metricas'); }
    finally { loadingMetricas = false; }
  }

  /**
   * Obtiene una pagina especifica del registro de busquedas paginado del backend usando los filtros
   * actuales (rango de fechas, canal, nombre de usuario, tamano de pagina 25). Actualiza metricasListado al tener exito.
   * @async
   * @param {number} pagina - El numero de pagina basado en 1 a cargar.
   * @returns {Promise<void>}
   */
  async function cargarListadoBusquedas(pagina = 1) {
    loadingListado = true;
    metPagina = pagina;
    try {
      const r = await fetch(`${API}/api/metricas/listado`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fechaDesde:   metFechaDesde,
          fechaHasta:   metFechaHasta,
          tipo:         metTipo    || null,
          usuario:      metUsuario || null,
          pagina,
          tamanoPagina: 25
        })
      });
      if (r.ok) metricasListado = await r.json();
      else mostrarToast('error', 'Error al cargar listado');
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { loadingListado = false; }
  }

  /**
   * Obtiene el analisis de negocio (5 graficas) del backend usando los filtros actuales de fecha.
   */
  async function cargarNegocio() {
    loadingNegocio = true;
    try {
      const params = new URLSearchParams({ fechaDesde: metFechaDesde, fechaHasta: metFechaHasta });
      const r = await fetch(`${API}/api/metricas/negocio?${params}`, { credentials: 'include' });
      if (r.ok) negocioData = await r.json();
      else mostrarToast('error', 'Error al cargar análisis de negocio');
    } catch { mostrarToast('error', 'Error de conexión con análisis de negocio'); }
    finally { loadingNegocio = false; }
  }

  /**
   * Aplica un período rápido actualizando fechaDesde y fechaHasta.
   * @param {'7d'|'30d'|'mes'|'anio'|'todo'} periodo
   */
  function setPeriodo(periodo) {
    metPeriodo = periodo;
    const hoy = new Date();
    const fmt = d => d.toISOString().split('T')[0];
    metFechaHasta = fmt(hoy);
    if (periodo === '7d') {
      const d = new Date(hoy); d.setDate(d.getDate() - 6);
      metFechaDesde = fmt(d);
    } else if (periodo === '30d') {
      const d = new Date(hoy); d.setDate(d.getDate() - 29);
      metFechaDesde = fmt(d);
    } else if (periodo === 'mes') {
      metFechaDesde = fmt(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
    } else if (periodo === 'anio') {
      metFechaDesde = fmt(new Date(hoy.getFullYear(), 0, 1));
    } else if (periodo === 'todo') {
      metFechaDesde = '2000-01-01';
    }
  }

  /**
   * Vuelve a aplicar todos los filtros actuales recargando las metricas de resumen y la pagina 1
   * del registro de busquedas de forma simultanea.
   */
  function aplicarFiltros() {
    cargarMetricas();
    cargarListadoBusquedas(1);
    cargarNegocio();
  }

  /**
   * Navega a la pagina indicada del registro de busquedas llamando a cargarListadoBusquedas.
   * @param {number} pagina - El numero de pagina al que navegar.
   */
  function cambiarPagina(pagina) {
    cargarListadoBusquedas(pagina);
  }

  /**
   * Envia con POST una solicitud de exportacion al backend con los filtros actuales y el correo
   * electronico proporcionado. El backend enviara el registro de busquedas filtrado como archivo adjunto.
   * Muestra un toast de exito o error y cierra el modal al completar.
   * @async
   * @returns {Promise<void>}
   */
  async function handleExportar() {
    enviandoExportar = true;
    const payload = {
      fechaDesde: metFechaDesde,
      fechaHasta: metFechaHasta,
      tipo:       metTipo    || null,
      usuario:    metUsuario || null,
      secciones:  expSec
    };
    try {
      if (expFormato === 'email') {
        const validos = correosExportar.map(c => c.trim()).filter(c => c.includes('@'));
        if (validos.length === 0) { mostrarToast('error', 'Ingresa al menos un correo válido'); return; }
        const r = await fetch(`${API}/api/metricas/exportar-correo`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...payload, correos: validos, formato: expFormatoEmail })
        });
        if (r.ok) {
          mostrarToast('success', `Reporte enviado a ${validos.length === 1 ? validos[0] : validos.length + ' destinatarios'}`);
          mostrarModalExportar = false; correosExportar = [''];
        } else { mostrarToast('error', 'No se pudo enviar el correo'); }
      } else {
        const ext  = expFormato === 'excel' ? 'xlsx' : expFormato === 'pdf' ? 'pdf' : 'zip';
        const mime = expFormato === 'excel'
          ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          : expFormato === 'pdf' ? 'application/pdf'
          : 'application/zip';
        const endpoint = expFormato === 'pdf' ? 'exportar-pdf' : 'exportar-archivo';
        const r = await fetch(`${API}/api/metricas/${endpoint}`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...payload, formato: expFormato })
        });
        if (r.ok) {
          const blob = await r.blob();
          const url  = URL.createObjectURL(new Blob([blob], { type: mime }));
          const a    = document.createElement('a');
          a.href = url; a.download = `metricas_broom_${metFechaDesde}_${metFechaHasta}.${ext}`;
          document.body.appendChild(a); a.click();
          document.body.removeChild(a); URL.revokeObjectURL(url);
          mostrarToast('success', 'Archivo descargado');
          mostrarModalExportar = false;
        } else { mostrarToast('error', 'No se pudo generar el archivo'); }
      }
    } catch { mostrarToast('error', 'Error de conexión'); }
    finally { enviandoExportar = false; }
  }

</script>

<!-- Seccion de analiticos con KPIs, graficas SVG, registro de busquedas y exportacion -->
<section class="admin-section met-section">
  <div class="met-header">
    <div>
      <h2 class="admin-section__title">Analiticos y Reportes</h2>
      <p class="admin-section__subtitle">Busquedas, ingresos y conversion del sistema</p>
    </div>
    <button class="met-btn-exportar" on:click={() => mostrarModalExportar = true}>
      <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor" style="flex-shrink:0">
        <path fill-rule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clip-rule="evenodd"/>
      </svg>
      Exportar
    </button>
  </div>

  <!-- Controles de filtro (5 tipos: período rápido, desde, hasta, canal, usuario) -->
  <div class="met-filtros-panel">

    <!-- Filtro 1: Período rápido -->
    <div class="met-filtro-bloque met-filtro-bloque--periodos">
      <span class="met-label">Período</span>
      <div class="met-periodo-btns">
        {#each [
          { id:'7d',   label:'7 días'  },
          { id:'30d',  label:'30 días' },
          { id:'mes',  label:'Este mes'},
          { id:'anio', label:'Este año'},
          { id:'todo', label:'Todo'    },
        ] as p}
          <button
            class="met-periodo-btn"
            class:met-periodo-btn--active={metPeriodo === p.id}
            on:click={() => { setPeriodo(p.id); }}
          >{p.label}</button>
        {/each}
      </div>
    </div>

    <div class="met-filtros-row">
      <!-- Filtro 2: Desde -->
      <div class="met-filtro-grupo">
        <label for="mf-desde" class="met-label">Desde</label>
        <input id="mf-desde" type="date" class="met-input"
          bind:value={metFechaDesde}
          on:change={() => metPeriodo = 'custom'} />
      </div>

      <!-- Filtro 3: Hasta -->
      <div class="met-filtro-grupo">
        <label for="mf-hasta" class="met-label">Hasta</label>
        <input id="mf-hasta" type="date" class="met-input"
          bind:value={metFechaHasta}
          on:change={() => metPeriodo = 'custom'} />
      </div>

      <!-- Filtro 4: Canal -->
      <div class="met-filtro-grupo">
        <label for="mf-canal" class="met-label">Canal</label>
        <select id="mf-canal" class="met-input" bind:value={metTipo}>
          <option value="">Todos los canales</option>
          <option value="Web">Web (buscador)</option>
          <option value="REST">REST (agencias/API)</option>
        </select>
      </div>

      <!-- Filtro 5: Usuario -->
      <div class="met-filtro-grupo">
        <label for="mf-usuario" class="met-label">Usuario</label>
        <input id="mf-usuario" type="text" class="met-input"
          placeholder="buscar username..." bind:value={metUsuario} />
      </div>

      <button class="met-btn-aplicar" on:click={aplicarFiltros}
        disabled={loadingMetricas || loadingNegocio || loadingListado}>
        {(loadingMetricas || loadingNegocio) ? 'Cargando...' : 'Aplicar'}
      </button>
    </div>
  </div>

  <!-- Selector de panel (filtro interno frontend, sin llamadas al backend) -->
  <div class="met-panel-selector">
    <button class="met-panel-btn" class:met-panel-btn--active={panelActivo === 'busquedas'}
      on:click={() => panelActivo = 'busquedas'}>
      <svg width="15" height="15" viewBox="0 0 20 20" fill="currentColor">
        <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zm6-4a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zm6-3a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z"/>
      </svg>
      Búsquedas y Canal
    </button>
    <button class="met-panel-btn" class:met-panel-btn--active={panelActivo === 'negocio'}
      on:click={() => panelActivo = 'negocio'}>
      <svg width="15" height="15" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M3 3a1 1 0 000 2v8a2 2 0 002 2h2.586l-1.293 1.293a1 1 0 101.414 1.414L10 15.414l2.293 2.293a1 1 0 001.414-1.414L12.414 15H15a2 2 0 002-2V5a1 1 0 100-2H3zm11 4a1 1 0 10-2 0v4a1 1 0 102 0V7zm-3 1a1 1 0 10-2 0v3a1 1 0 102 0V8zM8 9a1 1 0 00-2 0v2a1 1 0 102 0V9z" clip-rule="evenodd"/>
      </svg>
      Análisis de Negocio
    </button>
    <button class="met-panel-btn" class:met-panel-btn--active={panelActivo === 'listado'}
      on:click={() => panelActivo = 'listado'}>
      <svg width="15" height="15" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h6a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd"/>
      </svg>
      Registro de Búsquedas
    </button>
  </div>

  <!-- ═══ PANEL 1: BÚSQUEDAS Y CANAL ═══ -->
  {#if panelActivo === 'busquedas'}

  {#if loadingMetricas}
    <div class="met-loading"><div class="met-spinner"></div><p>Cargando analiticos...</p></div>
  {:else if metricasResumen}

    <!-- Banner de KPIs de ingresos: total, por clase, ticket promedio y tasa de conversion -->
    {#each [metricasResumen.ingresosKpi ?? {ingresosTotales:0,ingresosTurista:0,ingresosEjecutivo:0,totalBoletos:0,totalReservaciones:0,ticketPromedio:0}] as kpi}
      <div class="met-ingresos-banner">
        <div class="met-ing-main">
          <span class="met-ing-label">Ingresos Totales</span>
          <span class="met-ing-valor">
            ${(kpi.ingresosTurista + kpi.ingresosEjecutivo).toLocaleString('es-GT', {minimumFractionDigits:2, maximumFractionDigits:2})}
          </span>
          <span class="met-ing-sub">{kpi.totalBoletos} boletos · {kpi.totalReservaciones} reservaciones</span>
        </div>
        <div class="met-ing-stats">
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--turista"></span>
            <div>
              <span class="met-ing-stat__label">Turista</span>
              <span class="met-ing-stat__val">${kpi.ingresosTurista.toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--ejecutivo"></span>
            <div>
              <span class="met-ing-stat__label">Ejecutivo</span>
              <span class="met-ing-stat__val">${kpi.ingresosEjecutivo.toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--ticket"></span>
            <div>
              <span class="met-ing-stat__label">Ticket promedio</span>
              <span class="met-ing-stat__val">${kpi.ticketPromedio.toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--busq"></span>
            <div>
              <span class="met-ing-stat__label">Reservaciones pagadas</span>
              <span class="met-ing-stat__val">{kpi.totalReservaciones.toLocaleString()}</span>
            </div>
          </div>
        </div>
      </div>
    {/each}

    <!-- Búsquedas diarias -->
    <div class="met-grafica met-grafica--wide">
      <h3 class="met-grafica__titulo">Búsquedas diarias</h3>
      <p class="met-grafica__subtitulo">Cuántas veces se buscaron vuelos cada día en el período seleccionado.</p>
      {#if metricasResumen.busquedasPorDia.length === 0}
        <div class="met-empty">Sin datos en el periodo seleccionado</div>
      {:else}
        {@const datos  = metricasResumen.busquedasPorDia}
        {@const maxVal = Math.max(...datos.map(d => d.total), 1)}
        {@const tot    = datos.reduce((s,d)=>s+d.total,0)}
        {@const W=600} {@const H=180}
        {@const PAD={l:36,r:16,t:16,b:28}}
        {@const gW=W-PAD.l-PAD.r} {@const gH=H-PAD.t-PAD.b}
        <div class="met-svg-wrap">
          <svg viewBox="0 0 {W} {H}" class="met-svg" preserveAspectRatio="xMidYMid meet">
            <defs>
              <linearGradient id="gradBusc" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#D4AF37" stop-opacity="0.3"/>
                <stop offset="100%" stop-color="#D4AF37" stop-opacity="0"/>
              </linearGradient>
            </defs>
            {#each [0,0.25,0.5,0.75,1] as pct}
              {@const y=PAD.t+gH-pct*gH}
              <line x1={PAD.l} y1={y} x2={W-PAD.r} y2={y} stroke="#EBE6E0" stroke-width="1"/>
              <text x={PAD.l-4} y={y+4} font-size="9" fill="#b8b0a5" text-anchor="end">{Math.round(maxVal*pct)}</text>
            {/each}
            {#if datos.length > 1}
              {@const pts=datos.map((d,i)=>{
                const x=PAD.l+(i/(datos.length-1))*gW;
                const y=PAD.t+gH-(d.total/maxVal)*gH;
                return `${x},${y}`;
              })}
              <polygon points={`${PAD.l},${PAD.t+gH} ${pts.join(' ')} ${W-PAD.r},${PAD.t+gH}`} fill="url(#gradBusc)"/>
              <polyline points={pts.join(' ')} fill="none" stroke="#D4AF37" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>
              {#each datos as d,i}
                {@const x=PAD.l+(i/(datos.length-1))*gW}
                {@const y=PAD.t+gH-(d.total/maxVal)*gH}
                <circle cx={x} cy={y} r="3.5" fill="#D4AF37" stroke="white" stroke-width="1.5">
                  <title>{d.fecha}: {d.total} búsquedas</title>
                </circle>
              {/each}
            {:else}
              <circle cx={PAD.l+gW/2} cy={PAD.t+gH/2} r="6" fill="#D4AF37"/>
            {/if}
            {#each datos as d,i}
              {#if i % Math.ceil(datos.length/6) === 0 || i === datos.length-1}
                {@const x=datos.length>1 ? PAD.l+(i/(datos.length-1))*gW : PAD.l+gW/2}
                <text x={x} y={H-4} font-size="8" fill="#b8b0a5" text-anchor="middle">{d.fecha.slice(5)}</text>
              {/if}
            {/each}
          </svg>
        </div>
        <div class="met-grafica__leyenda">
          <span><span class="met-leyenda-dot" style="background:#D4AF37"></span> Búsquedas por día</span>
          <span style="margin-left:auto;color:var(--text-muted);font-size:.78rem">Total período: <strong>{tot.toLocaleString()}</strong> búsquedas</span>
        </div>
      {/if}
    </div>

    <!-- Desglose de busquedas por canal con totales y porcentajes correctos -->
    {#if metricasResumen.busquedasPorTipo && metricasResumen.busquedasPorTipo.length > 0}
      {@const totalTipo = metricasResumen.busquedasPorTipo.reduce((s,t) => s + t.total, 0) || 1}
      {@const canalColores = {'Web':'#D4AF37','REST':'#1C1A18','Usuario':'#10b981'}}
      <div class="met-canal-wrap">
        <h3 class="met-canal__titulo">Búsquedas por canal</h3>
        <p class="met-canal__subtitulo">
          Usuario: pasajero con sesión iniciada · Web: visita anónima · Agencias: agencias de viaje ·
          Total: {totalTipo.toLocaleString()} búsquedas en el período
        </p>
        <div class="met-canal-pills">
          {#each metricasResumen.busquedasPorTipo as tipo}
            {@const pct   = ((tipo.total/totalTipo)*100).toFixed(1)}
            {@const color = canalColores[tipo.tipo] ?? '#6b7280'}
            <div class="met-canal-pill">
              <div class="met-canal-bar" style="--pct:{pct}%;--color:{color}"></div>
              <span class="met-canal-tipo" style="color:{color}">{tipo.tipo}</span>
              <span class="met-canal-num">
                {tipo.total.toLocaleString()} búsquedas
                <em>— {pct}% del total</em>
              </span>
            </div>
          {/each}
        </div>
      </div>
    {/if}

  {/if}<!-- fin búsquedas loadingMetricas -->

  {/if}<!-- fin panel busquedas -->

  <!-- ═══ PANEL 2: ANÁLISIS DE NEGOCIO ═══ -->
  {#if panelActivo === 'negocio'}

    {#if loadingNegocio}
      <div class="met-loading"><div class="met-spinner"></div><p>Cargando análisis de negocio...</p></div>
    {:else if negocioData}

      <!-- ── 1. Embudo conversión ──────────────────────────────────────── -->
      {@const emb = negocioData.embudo}
      {@const embBase = Math.max(emb.completadas + emb.pagadas + emb.pendientes + emb.expiradas + emb.canceladas, 1)}
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Embudo de conversión: Reservación → Pago</h3>
        <p class="met-grafica__subtitulo">Distribución por estado final de las reservaciones en el período.</p>

        <div class="neg-funnel">
          {#each [
            { label: 'Completadas (ya volaron)',val: emb.completadas,   color: '#8b5cf6' },
            { label: 'Pagadas (confirmadas)',   val: emb.pagadas,       color: '#10b981' },
            { label: 'Pendientes de pago',      val: emb.pendientes,    color: '#60a5fa' },
            { label: 'Expiradas sin pagar',     val: emb.expiradas,     color: '#f59e0b' },
            { label: 'Canceladas',              val: emb.canceladas,    color: '#ef4444' },
          ] as paso}
            {@const pct = embBase > 0 ? (paso.val / embBase * 100) : 0}
            <div class="neg-funnel-row">
              <div class="neg-funnel-label">{paso.label}</div>
              <div class="neg-funnel-track">
                <div class="neg-funnel-bar" style="width:{Math.min(pct, 100)}%;background:{paso.color}"></div>
              </div>
              <div class="neg-funnel-vals">
                <span class="neg-funnel-num">{paso.val.toLocaleString()}</span>
                <span class="neg-funnel-pct" style="color:{paso.color}" class:neg-funnel-pct--over={pct>100}>({pct.toFixed(2)}%)</span>
              </div>
            </div>
          {/each}
        </div>

        <div class="neg-funnel-kpis">
          {#each [
            { label:'Completadas',    val: emb.completadas,   color:'#8b5cf6' },
            { label:'Pagadas',        val: emb.pagadas,        color:'#10b981' },
            { label:'Pendientes',     val: emb.pendientes,     color:'#60a5fa' },
            { label:'Expiradas',      val: emb.expiradas,      color:'#f59e0b' },
            { label:'Canceladas',     val: emb.canceladas,     color:'#ef4444' },
          ] as k}
            {@const pct = embBase > 0 ? (k.val / embBase * 100).toFixed(2) : '0.00'}
            <div class="neg-kpi">
              <span class="neg-kpi-val" style="color:{k.color}">{pct}%</span>
              <span class="neg-kpi-num">{k.val.toLocaleString()}</span>
              <span class="neg-kpi-label">{k.label}</span>
            </div>
          {/each}
        </div>
      </div>

      <!-- ── 2. Rutas: Demanda + Ingresos (fusionado) ──────────────────────── -->
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Rendimiento de rutas</h3>
        <p class="met-grafica__subtitulo">
          Cuántas búsquedas recibió cada ruta, cuánto generó en ingresos y cuántos boletos se vendieron.
        </p>
        {#if negocioData.rutasRendimiento.length === 0}
          <div class="met-empty">Sin datos en el período seleccionado</div>
        {:else}
          {@const rutasMerge = negocioData.rutasRendimiento.map(r => ({
            origen: r.origenCodigo, destino: r.destinoCodigo,
            ingresos: r.revenueTotal, boletos: r.boletosVendidos,
            reservaciones: r.totalReservaciones,
            busquedas: r.busquedas ?? 0
          }))}
          {@const totalIng = rutasMerge.reduce((s,r) => s + r.ingresos, 0)}
          <!-- Resumen total del período -->
          <div class="ruta-total-banner">
            <span class="ruta-total-label">Total generado en el período</span>
            <span class="ruta-total-val">${totalIng.toLocaleString('es-GT',{minimumFractionDigits:2,maximumFractionDigits:2})}</span>
          </div>
          <div class="ruta-tabla">
            <!-- Encabezado -->
            <div class="ruta-tabla-header">
              <span>#</span>
              <span>Ruta</span>
              <span class="ruta-col-center">Búsquedas</span>
              <span class="ruta-col-center">Reservaciones</span>
              <span class="ruta-col-center">Boletos</span>
              <span>Ingresos del total</span>
            </div>
            {#each rutasMerge as ruta, i}
              {@const tieneIngresos = ruta.ingresos > 0}
              {@const pctTotal = totalIng > 0 ? (ruta.ingresos / totalIng * 100) : 0}
              <div class="ruta-tabla-row" class:ruta-tabla-row--sin-ing={!tieneIngresos}>
                <span class="ruta-rank">#{i+1}</span>
                <span class="ruta-nombre">
                  <span class="ruta-cod">{ruta.origen}</span>
                  <span class="ruta-flecha">→</span>
                  <span class="ruta-cod">{ruta.destino}</span>
                </span>
                <span class="ruta-col-center ruta-stat">{ruta.busquedas > 0 ? ruta.busquedas : '0'}</span>
                <span class="ruta-col-center ruta-stat">{ruta.reservaciones > 0 ? ruta.reservaciones : '0'}</span>
                <span class="ruta-col-center ruta-stat">{ruta.boletos > 0 ? ruta.boletos : '0'}</span>
                <span class="ruta-ing-wrap">
                  {#if tieneIngresos}
                    <span class="ruta-ing-pct-label">{pctTotal.toFixed(2)}%</span>
                    <span class="ruta-ing-bar-bg">
                      <span class="ruta-ing-bar" style="width:{pctTotal}%"></span>
                    </span>
                    <span class="ruta-ing-val">${ruta.ingresos.toLocaleString('es-GT',{maximumFractionDigits:0})}</span>
                  {:else}
                    <span class="ruta-ing-pct-label">0.00%</span>
                    <span class="ruta-ing-bar-bg"><span class="ruta-ing-bar" style="width:0%"></span></span>
                    <span class="ruta-ing-val" style="color:#b8b0a5">$0</span>
                  {/if}
                </span>
              </div>
            {/each}
          </div>
        {/if}
      </div>

      <!-- ── 3. Análisis de cancelaciones ──────────────────────────────── -->
      <div class="neg-card">
        <h3 class="met-grafica__titulo" style="padding:0 0 .25rem">Análisis de cancelaciones</h3>
        <p class="met-grafica__subtitulo" style="padding:0 0 .75rem">
          Rutas más afectadas · Quién canceló · Con cuánta anticipación al vuelo
        </p>

        <div class="canc-horizontal">

          <!-- Panel 1: Rutas (arriba, ancho completo) -->
          <div class="canc-h-panel canc-h-panel--rutas">
            <h4 class="neg-sub-titulo">Rutas afectadas</h4>
            {#if negocioData.cancelaciones.porRuta.length === 0}
              <div class="met-empty">Sin datos</div>
            {:else}
              {@const maxCanc   = Math.max(...negocioData.cancelaciones.porRuta.map(r => r.total), 1)}
              {@const totalCanc = negocioData.cancelaciones.porRuta.reduce((s,r)=>s+r.total,0)}
              {@const totalResv = negocioData.embudo.canceladas}
              {@const top       = negocioData.cancelaciones.porRuta[0]}
              {@const topPct    = totalCanc > 0 ? (top.total/totalCanc*100).toFixed(1) : 0}
              <!-- Ruta más afectada destacada -->
              <div class="canc-top-ruta">
                <div class="canc-top-ruta__pct">{topPct}%</div>
                <div class="canc-top-ruta__info">
                  <span class="canc-top-ruta__label">Ruta más cancelada</span>
                  <span class="canc-top-ruta__ruta">{top.origenCodigo} → {top.destinoCodigo}</span>
                  <span class="canc-top-ruta__sub">{top.total} cancelación{top.total !== 1 ? 'es' : ''} del total</span>
                </div>
              </div>

              <!-- Resto de rutas -->
              <div class="canc-resto">
                {#each negocioData.cancelaciones.porRuta.slice(1) as ruta}
                  {@const rutaPct = totalCanc > 0 ? (ruta.total/totalCanc*100).toFixed(1) : '0.0'}
                  <div class="canc-ruta-row">
                    <span class="canc-ruta-row__label">{ruta.origenCodigo} → {ruta.destinoCodigo}</span>
                    <div class="canc-ruta-row__track">
                      <div class="canc-ruta-row__bar" style="width:{(ruta.total/maxCanc)*100}%"></div>
                    </div>
                    <div class="canc-ruta-row__stat">
                      <span class="canc-ruta-row__pct">{rutaPct}%</span>
                      <span class="canc-ruta-row__num">{ruta.total}</span>
                    </div>
                  </div>
                {/each}
              </div>

            {/if}
          </div>

          <!-- Divisor horizontal -->
          <div class="canc-h-div"></div>

          <!-- Panel 2: Quién canceló (abajo izquierda) -->
          <div class="canc-h-panel canc-h-panel--tipo">
            <h4 class="neg-sub-titulo">Quién canceló</h4>
            {#if negocioData.cancelaciones.porTipo.length === 0}
              <div class="met-empty">Sin datos</div>
            {:else}
              {@const totalTipo = negocioData.cancelaciones.porTipo.reduce((s,t)=>s+t.total,0) || 1}
              {@const coloresTipo = {'Administrador':'#1C1A18','Agencia':'#D4AF37','Usuario':'#10b981'}}
              {@const segTipo = (() => {
                let ang = -90;
                return negocioData.cancelaciones.porTipo.map(t => {
                  const pct = t.total / totalTipo;
                  const start = ang; ang += pct * 360;
                  return { ...t, start, angle: pct*360,
                    color: coloresTipo[t.tipo] ?? '#6b7280',
                    pct: (pct*100).toFixed(1) };
                });
              })()}
              <div class="neg-tipo-wrap">
                <svg viewBox="0 0 120 120" class="neg-tipo-svg">
                  {#each segTipo as seg}
                    {#if seg.angle > 0.5}
                      {@const cx=60}{@const cy=60}{@const R=50}{@const r=28}
                      {@const s=seg.start*Math.PI/180}{@const e=(seg.start+seg.angle)*Math.PI/180}
                      {@const large=seg.angle>180?1:0}
                      {@const x1=cx+R*Math.cos(s)}{@const y1=cy+R*Math.sin(s)}
                      {@const x2=cx+R*Math.cos(e)}{@const y2=cy+R*Math.sin(e)}
                      {@const x3=cx+r*Math.cos(e)}{@const y3=cy+r*Math.sin(e)}
                      {@const x4=cx+r*Math.cos(s)}{@const y4=cy+r*Math.sin(s)}
                      <path d="M{x1},{y1} A{R},{R} 0 {large},1 {x2},{y2} L{x3},{y3} A{r},{r} 0 {large},0 {x4},{y4} Z"
                        fill={seg.color} stroke="white" stroke-width="2">
                        <title>{seg.tipo}: {seg.total} ({seg.pct}%)</title>
                      </path>
                    {/if}
                  {/each}
                  <circle cx="60" cy="60" r="24" fill="white"/>
                  <text x="60" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#1C1A18">{totalTipo}</text>
                </svg>
                <div class="neg-tipo-leyenda">
                  {#each segTipo as seg}
                    <div class="neg-ley-row">
                      <span class="met-leyenda-dot" style="background:{seg.color}"></span>
                      <span>{seg.tipo}</span>
                      <span class="neg-ley-num">{seg.total} <em>({seg.pct}%)</em></span>
                    </div>
                  {/each}
                </div>
              </div>
            {/if}
          </div>

          <!-- Panel 3: Anticipación (abajo derecha) -->
          <div class="canc-h-panel canc-h-panel--antic">
            <h4 class="neg-sub-titulo">Anticipación al vuelo</h4>
            {#if negocioData.cancelaciones.porAnticipacion.length === 0}
              <div class="met-empty">Sin datos</div>
            {:else}
              {@const BUCKETS_ORDEN = ['Menos de 24h','1-3 días','3-7 días','Más de 7 días']}
              {@const coloresAntic = {'Menos de 24h':'#ef4444','1-3 días':'#f59e0b','3-7 días':'#D4AF37','Más de 7 días':'#10b981'}}
              {@const anticMap = Object.fromEntries(negocioData.cancelaciones.porAnticipacion.map(a=>[a.bucket,a.total]))}
              {@const totalAntic = negocioData.cancelaciones.porAnticipacion.reduce((s,a)=>s+a.total,0) || 1}
              {@const maxAntic   = Math.max(...BUCKETS_ORDEN.map(b=>anticMap[b]??0),1)}
              {#each BUCKETS_ORDEN as bucket}
                {@const val = anticMap[bucket] ?? 0}
                <div class="neg-cancel-row" class:neg-cancel-row--vacio={val === 0}>
                  <span class="neg-cancel-ruta">{bucket}</span>
                  <div class="neg-cancel-track">
                    <div class="neg-cancel-bar"
                      style="width:{(val/maxAntic)*100}%;background:{coloresAntic[bucket]}">
                    </div>
                  </div>
                  <span class="neg-cancel-num">{val}
                    <em style="font-style:normal;color:var(--text-muted);font-size:.7rem">
                      ({(val/totalAntic*100).toFixed(0)}%)
                    </em>
                  </span>
                </div>
              {/each}

              <!-- Nota contextual de anticipación -->
              {@const soloUnBucket = negocioData.cancelaciones.porAnticipacion.length === 1}
              {@const bucketDominante = negocioData.cancelaciones.porAnticipacion[0]}
              {#if soloUnBucket && bucketDominante.bucket === 'Más de 7 días'}
                <div class="antic-nota antic-nota--ok">
                  <svg width="13" height="13" viewBox="0 0 16 16" fill="none" style="flex-shrink:0;margin-top:1px">
                    <circle cx="8" cy="8" r="7.5" stroke="#10b981" stroke-width="1.2"/>
                    <text x="8" y="12" text-anchor="middle" font-size="10" font-weight="700" fill="#10b981">i</text>
                  </svg>
                  <span>
                    Todas las cancelaciones se hicieron con más de 7 días de anticipación — señal positiva. A medida que crezca el volumen aparecerán otros tramos.
                  </span>
                </div>
              {:else if soloUnBucket && bucketDominante.bucket === 'Menos de 24h'}
                <div class="antic-nota antic-nota--warn">
                  <svg width="13" height="13" viewBox="0 0 16 16" fill="none" style="flex-shrink:0;margin-top:1px">
                    <path d="M8 1L15 14H1L8 1Z" stroke="#ef4444" stroke-width="1.2" fill="none"/>
                    <text x="8" y="12" text-anchor="middle" font-size="9" font-weight="700" fill="#ef4444">!</text>
                  </svg>
                  <span>
                    Todas las cancelaciones ocurrieron con menos de 24 h de anticipación — riesgo alto de asientos sin reasignar.
                  </span>
                </div>
              {:else if !soloUnBucket}
                <div class="antic-nota antic-nota--info">
                  <svg width="13" height="13" viewBox="0 0 16 16" fill="none" style="flex-shrink:0;margin-top:1px">
                    <circle cx="8" cy="8" r="7.5" stroke="#D4AF37" stroke-width="1.2"/>
                    <text x="8" y="12" text-anchor="middle" font-size="10" font-weight="700" fill="#D4AF37">i</text>
                  </svg>
                  <span>
                    Hay cancelaciones en distintos momentos. Cuanto más cerca del vuelo, menor la posibilidad de reasignar el asiento.
                  </span>
                </div>
              {/if}
            {/if}
          </div>

        </div>
      </div>

      <!-- ── 4. Ingresos: distribución + tendencia (fusionado) ────────────── -->
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Ingresos por clase — Distribución y Tendencia</h3>
        <p class="met-grafica__subtitulo">Distribución de ingresos entre Turista y Ejecutivo, y cómo evolucionaron mes a mes.</p>
        <div class="neg-ingresos-combo">

          <!-- Donut -->
          <div class="neg-combo-donut">
            {#if !metricasResumen?.distribucionClase || metricasResumen.distribucionClase.length === 0}
              <div class="met-empty">Sin boletos en el período</div>
            {:else}
              {@const donutData = metricasResumen.distribucionClase}
              {@const totalIng  = donutData.reduce((s,c)=>s+(c.ingresos??0),0)||1}
              {@const colores   = ['#D4AF37','#1C1A18']}
              {@const segs = (() => {
                let ang = -90;
                return donutData.map((c,i) => {
                  const ing = c.ingresos??0; const pct = ing/totalIng;
                  const start = ang; ang += pct*360;
                  return { clase:c.clase??c.Clase??'', ingresos:ing,
                    boletos:c.boletos??c.Boletos??0,
                    start, angle:pct*360, color:colores[i%colores.length],
                    pct:(pct*100).toFixed(1) };
                });
              })()}
              <svg viewBox="0 0 160 160" style="width:140px;height:140px;flex-shrink:0">
                {#each segs as seg}
                  {#if seg.angle > 0.5}
                    {@const cx=80}{@const cy=80}{@const R=64}{@const r=38}
                    {@const s=seg.start*Math.PI/180}{@const e=(seg.start+seg.angle)*Math.PI/180}
                    {@const lg=seg.angle>180?1:0}
                    {@const x1=cx+R*Math.cos(s)}{@const y1=cy+R*Math.sin(s)}
                    {@const x2=cx+R*Math.cos(e)}{@const y2=cy+R*Math.sin(e)}
                    {@const x3=cx+r*Math.cos(e)}{@const y3=cy+r*Math.sin(e)}
                    {@const x4=cx+r*Math.cos(s)}{@const y4=cy+r*Math.sin(s)}
                    <path d="M{x1},{y1} A{R},{R} 0 {lg},1 {x2},{y2} L{x3},{y3} A{r},{r} 0 {lg},0 {x4},{y4} Z"
                      fill={seg.color} stroke="white" stroke-width="2">
                      <title>{seg.clase}: ${seg.ingresos.toLocaleString('es-GT')} ({seg.pct}%)</title>
                    </path>
                  {/if}
                {/each}
                <circle cx="80" cy="80" r="34" fill="white"/>
                <text x="80" y="77" text-anchor="middle" font-size="10" font-weight="700" fill="#1C1A18">
                  ${(totalIng/1000).toFixed(1)}k
                </text>
                <text x="80" y="90" text-anchor="middle" font-size="8" fill="#b8b0a5">TOTAL</text>
              </svg>
              <div style="display:flex;flex-direction:column;gap:.6rem;margin-top:.25rem">
                {#each segs as seg}
                  <div style="display:flex;flex-direction:column;gap:.1rem">
                    <div style="display:flex;align-items:center;gap:.4rem">
                      <span class="met-leyenda-dot" style="background:{seg.color}"></span>
                      <span style="font-size:.82rem;font-weight:700;color:#1C1A18">{seg.clase}</span>
                      <span style="font-size:.78rem;color:var(--text-muted);margin-left:auto">{seg.pct}%</span>
                    </div>
                    <span style="font-size:.8rem;color:#374151;padding-left:1.1rem">${seg.ingresos.toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
                    <span style="font-size:.72rem;color:var(--text-muted);padding-left:1.1rem">{seg.boletos} boletos</span>
                  </div>
                {/each}
              </div>
            {/if}
          </div>

          <!-- Tendencia -->
          <div class="neg-combo-tendencia">
            {#if negocioData.ingresosTendencia.length === 0}
              <div class="met-empty">Sin ventas en el período</div>
            {:else}
              {@const meses  = [...new Set(negocioData.ingresosTendencia.map(d=>d.mes))].sort()}
              {@const clases = [...new Set(negocioData.ingresosTendencia.map(d=>d.clase))]}
              {@const byKey  = Object.fromEntries(negocioData.ingresosTendencia.map(d=>[`${d.mes}|${d.clase}`,d.revenue]))}
              {@const maxR   = Math.max(...meses.flatMap(m=>clases.map(cl=>byKey[`${m}|${cl}`]??0)),1)}
              {@const mesFmt = m => `${m.slice(5)}/${m.slice(2,4)}`}
              {@const ultimoMes = meses[meses.length-1]}
              {@const W=420}{@const H=200}
              {@const PAD={l:52,r:16,t:16,b:32}}
              {@const gW=W-PAD.l-PAD.r}{@const gH=H-PAD.t-PAD.b}
              <div class="tend-wrap">
                <!-- Gráfica -->
                <div class="tend-chart">
                  <svg viewBox="0 0 {W} {H}" style="width:100%;max-height:200px" preserveAspectRatio="xMidYMid meet">
                    {#each [0,0.25,0.5,0.75,1] as pct}
                      {@const y=PAD.t+gH-pct*gH}
                      <line x1={PAD.l} y1={y} x2={W-PAD.r} y2={y} stroke="#EBE6E0" stroke-width="1"/>
                      <text x={PAD.l-4} y={y+4} font-size="8" fill="#b8b0a5" text-anchor="end">${(maxR*pct/1000).toFixed(0)}k</text>
                    {/each}
                    {#each clases as cl, ci}
                      {@const color = ci===0?'#D4AF37':'#1C1A18'}
                      {@const colorPale = ci===0?'#EDD97A':'#9ca3af'}
                      {#if meses.length > 1}
                        <!-- Línea pálida: valor del mes anterior desplazado un puesto a la derecha -->
                        {@const ptsPrev=meses.map((m,i)=>{
                          const x=PAD.l+(i/(meses.length-1))*gW;
                          const prevVal=i>0?(byKey[`${meses[i-1]}|${cl}`]??0):0;
                          const y=PAD.t+gH-(prevVal/maxR)*gH;
                          return `${x},${y}`;
                        })}
                        <polyline points={ptsPrev.join(' ')} fill="none" stroke={colorPale} stroke-width="1.5"
                          stroke-linejoin="round" stroke-linecap="round"
                          stroke-dasharray="4,4" opacity="0.55"/>
                        <!-- Línea principal -->
                        {@const pts=meses.map((m,i)=>{
                          const x=PAD.l+(i/(meses.length-1))*gW;
                          const y=PAD.t+gH-((byKey[`${m}|${cl}`]??0)/maxR)*gH;
                          return `${x},${y}`;
                        })}
                        <polyline points={pts.join(' ')} fill="none" stroke={color} stroke-width="2"
                          stroke-linejoin="round" stroke-linecap="round"
                          stroke-dasharray={ci===1?'5,3':'none'}/>
                        {#each meses as m, i}
                          {@const x=PAD.l+(i/(meses.length-1))*gW}
                          {@const y=PAD.t+gH-((byKey[`${m}|${cl}`]??0)/maxR)*gH}
                          {@const rev=byKey[`${m}|${cl}`]??0}
                          {@const prevRev=i>0?(byKey[`${meses[i-1]}|${cl}`]??0):null}
                          <circle cx={x} cy={y} r="3" fill={color} stroke="white" stroke-width="1.5">
                            <title>{cl} {mesFmt(m)}: ${rev.toLocaleString('es-GT',{minimumFractionDigits:2})}{prevRev!==null?' · mes anterior: $'+prevRev.toLocaleString('es-GT',{minimumFractionDigits:2}):''}</title>
                          </circle>
                        {/each}
                      {:else if meses.length===1}
                        {@const rev=byKey[`${meses[0]}|${cl}`]??0}
                        {@const y=PAD.t+gH-(rev/maxR)*gH}
                        <circle cx={PAD.l+gW/2} cy={y} r="5" fill={color} stroke="white" stroke-width="1.5">
                          <title>{cl}: ${rev.toLocaleString('es-GT',{minimumFractionDigits:2})}</title>
                        </circle>
                      {/if}
                    {/each}
                    {#each meses as m, i}
                      {@const x=meses.length>1?PAD.l+(i/(meses.length-1))*gW:PAD.l+gW/2}
                      <text x={x} y={H-4} font-size="7" fill="#b8b0a5" text-anchor="middle">{mesFmt(m)}</text>
                    {/each}
                  </svg>
                  <div class="met-grafica__leyenda" style="margin-top:.5rem">
                    {#each clases as cl, ci}
                      <span>
                        <span class="met-leyenda-dot" style="background:{ci===0?'#D4AF37':'#1C1A18'}"></span>
                        {cl}
                      </span>
                    {/each}
                    <span style="color:#b8b0a5;font-size:10px;margin-left:.5rem">· · · mes anterior</span>
                  </div>
                </div>

                <!-- Card resumen -->
                <div class="tend-resumen">
                  <div class="tend-resumen__bloque">
                    <p class="tend-resumen__titulo">Mes actual <span class="tend-resumen__mes">({mesFmt(ultimoMes)})</span></p>
                    {#each clases as cl, ci}
                      {@const val = byKey[`${ultimoMes}|${cl}`] ?? 0}
                      <div class="tend-resumen__fila">
                        <span class="tend-resumen__dot" style="background:{ci===0?'#D4AF37':'#1C1A18'}"></span>
                        <span class="tend-resumen__clase">{cl}</span>
                        <span class="tend-resumen__val">${val.toLocaleString('es-GT',{minimumFractionDigits:2,maximumFractionDigits:2})}</span>
                      </div>
                    {/each}
                  </div>
                  <div class="tend-resumen__sep"></div>
                  <div class="tend-resumen__bloque">
                    <p class="tend-resumen__titulo">Total período</p>
                    {#each clases as cl, ci}
                      {@const tot = meses.reduce((s,m)=>s+(byKey[`${m}|${cl}`]??0),0)}
                      <div class="tend-resumen__fila">
                        <span class="tend-resumen__dot" style="background:{ci===0?'#D4AF37':'#1C1A18'}"></span>
                        <span class="tend-resumen__clase">{cl}</span>
                        <span class="tend-resumen__val tend-resumen__val--total">${tot.toLocaleString('es-GT',{minimumFractionDigits:2,maximumFractionDigits:2})}</span>
                      </div>
                    {/each}
                  </div>
                </div>
              </div>
            {/if}
          </div>

        </div>
      </div>

      <!-- ── 5. Mapa de calor de búsquedas por día/hora ───────────────────── -->
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Mapa de calor de búsquedas</h3>
        <p class="met-grafica__subtitulo">
          Cuándo busca la gente — día de la semana y hora del día con más actividad. Más oscuro = más búsquedas.
          <span class="heat-tip-row">
            <span class="heat-tip">
              <svg width="12" height="12" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7.5" stroke="#D4AF37" stroke-width="1.2"/><text x="8" y="12" text-anchor="middle" font-size="10" font-weight="700" fill="#D4AF37">i</text></svg>
              Las celdas más oscuras indican cuándo enviar promociones o reforzar soporte.
            </span>
            <span class="heat-tip">
              <svg width="12" height="12" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7.5" stroke="#D4AF37" stroke-width="1.2"/><text x="8" y="12" text-anchor="middle" font-size="10" font-weight="700" fill="#D4AF37">i</text></svg>
              Las celdas vacías son los mejores momentos para hacer mantenimiento.
            </span>
            <span class="heat-tip">
              <svg width="12" height="12" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7.5" stroke="#D4AF37" stroke-width="1.2"/><text x="8" y="12" text-anchor="middle" font-size="10" font-weight="700" fill="#D4AF37">i</text></svg>
              Con más período seleccionado el patrón es más confiable.
            </span>
          </span>
        </p>
        {#if negocioData.heatmap.length === 0}
          <div class="met-empty">Sin búsquedas en el período seleccionado</div>
        {:else}
          {@const DAY_SQL = {1:'Dom',2:'Lun',3:'Mar',4:'Mié',5:'Jue',6:'Vie',7:'Sáb'}}
          {@const DAY_ORDER = [2,3,4,5,6,7,1]}
          {@const horasSet = new Set(negocioData.heatmap.map(c => c.hora))}
          {@const horas = [...horasSet].sort((a,b)=>a-b)}
          {@const heatMap = new Map(negocioData.heatmap.map(c => [`${c.diaSemana},${c.hora}`, c]))}
          {@const pico = Math.max(...negocioData.heatmap.map(c => c.asientosVendidos), 1)}
          {@const cellW = Math.min(44, Math.max(28, Math.floor(560/Math.max(horas.length,1))))}
          {@const cellH = 32}
          {@const W2 = 52 + horas.length * cellW + 8}
          {@const H2 = 20 + DAY_ORDER.length * cellH + 28}
          <div class="met-svg-wrap" style="overflow-x:auto">
            <svg viewBox="0 0 {W2} {H2}" width={W2} height={H2} style="min-width:{W2}px;display:block;margin:0 auto">
              {#each horas as h, hi}
                <text x={52+hi*cellW+cellW/2} y="14" text-anchor="middle" font-size="8" fill="#b8b0a5">{h}h</text>
              {/each}
              {#each DAY_ORDER as dia, di}
                <text x="48" y={20+di*cellH+cellH/2+4} text-anchor="end" font-size="9" fill="#6b7280">{DAY_SQL[dia]}</text>
                {#each horas as h, hi}
                  {@const cell = heatMap.get(`${dia},${h}`)}
                  {@const pct  = cell ? cell.ocupacionPct : -1}
                  {@const fillColor = pct < 0  ? '#F2EFE9'
                    : pct === 0               ? '#EDEBE5'
                    : pct < 20               ? '#FEF3C7'
                    : pct < 40               ? '#FCD34D'
                    : pct < 70               ? '#D4AF37'
                    : '#92400E'}
                  {@const textColor = pct >= 40 ? 'white' : '#6b5a3a'}
                  <rect x={52+hi*cellW+1} y={20+di*cellH+1}
                    width={cellW-2} height={cellH-2}
                    rx="4" fill={fillColor}>
                    {#if cell}
                      <title>{DAY_SQL[dia]} {h}h — {cell.asientosVendidos} búsquedas ({cell.ocupacionPct.toFixed(0)}% del pico de {pico})</title>
                    {:else}
                      <title>{DAY_SQL[dia]} {h}h — sin búsquedas</title>
                    {/if}
                  </rect>
                  {#if cell && cellW >= 28}
                    <text x={52+hi*cellW+cellW/2} y={20+di*cellH+cellH/2+4}
                      text-anchor="middle" font-size="8" font-weight="600"
                      fill={textColor} pointer-events="none">
                      {cell.asientosVendidos}
                    </text>
                  {/if}
                {/each}
              {/each}
            </svg>
          </div>
          <div class="neg-heat-leyenda">
            {#each [
              {color:'#EDEBE5', label:'Sin búsquedas'},
              {color:'#FEF3C7', label:'Baja'},
              {color:'#FCD34D', label:'Media'},
              {color:'#D4AF37', label:'Alta'},
              {color:'#92400E', label:'Pico'},
            ] as l}
              <span class="neg-heat-item">
                <span class="neg-heat-dot" style="background:{l.color};border:1px solid #ddd"></span>{l.label}
              </span>
            {/each}
            <span style="color:var(--text-muted);font-size:.75rem;margin-left:.5rem">· Pasa el cursor sobre una celda para ver el detalle</span>
          </div>
        {/if}
      </div>

    {:else}
      <div class="met-empty" style="padding:3rem 0">
        Aplica los filtros para ver el análisis de negocio.
      </div>
    {/if}

  {/if}<!-- fin panel negocio -->

  <!-- ═══ PANEL 3: REGISTRO DE BÚSQUEDAS ═══ -->
  {#if panelActivo === 'listado'}

  <div class="met-listado">
    <div class="met-listado__header">
      <h3 class="met-listado__titulo">Registro de busquedas</h3>
    </div>

    {#if loadingListado}
      <div class="met-loading met-loading--sm"><div class="met-spinner"></div><p>Cargando listado...</p></div>
    {:else if metricasListado}
      <div class="met-tabla-wrap">
        <table class="table">
          <thead class="table__head">
            <tr>
              <th class="table__header">#</th>
              <th class="table__header">Ruta</th>
              <th class="table__header">Fecha Salida</th>
              <th class="table__header">Pasajeros</th>
              <th class="table__header">Usuario</th>
              <th class="table__header">Canal</th>
              <th class="table__header">Fecha Busqueda</th>
            </tr>
          </thead>
          <tbody>
            {#each metricasListado.registros as b}
              <tr class="table__row">
                <td class="table__cell">{b.id}</td>
                <td class="table__cell">
                  <span class="met-ruta-tag">{b.origenCodigo}</span>
                  <span style="color:var(--text-muted);padding:0 .3rem">→</span>
                  <span class="met-ruta-tag">{b.destinoCodigo}</span>
                </td>
                <td class="table__cell">{b.fechaSalida}</td>
                <td class="table__cell">{b.cantidadPersonas}</td>
                <td class="table__cell">{b.usuario ?? 'No registrado'}</td>
                <td class="table__cell">
                  <span class="met-tipo-badge met-tipo-badge--{b.tipo.toLowerCase()}">{b.tipo}</span>
                </td>
                <td class="table__cell" style="font-size:.8rem;color:var(--text-muted)">{b.fechaBusqueda}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>

      {#if metricasListado.totalPaginas > 1}
        <div class="met-paginado">
          <button class="met-pag-btn"
            disabled={metricasListado.paginaActual <= 1}
            on:click={() => cambiarPagina(metricasListado.paginaActual - 1)}>
            ← Anterior
          </button>
          <span class="met-pag-info">
            Pagina {metricasListado.paginaActual} de {metricasListado.totalPaginas}
            &nbsp;·&nbsp; {metricasListado.totalRegistros.toLocaleString()} registros
          </span>
          <button class="met-pag-btn"
            disabled={metricasListado.paginaActual >= metricasListado.totalPaginas}
            on:click={() => cambiarPagina(metricasListado.paginaActual + 1)}>
            Siguiente →
          </button>
        </div>
      {:else}
        <p class="met-pag-info" style="padding:.75rem 0">
          {metricasListado.totalRegistros.toLocaleString()} registros totales
        </p>
      {/if}
    {:else}
      <div class="met-empty">Aplica filtros para ver el listado</div>
    {/if}
  </div>

  {/if}<!-- fin panel listado -->

</section>

<!-- ═══ MODAL EXPORTAR MÉTRICAS ═══ -->
{#if mostrarModalExportar}
  <div class="modal-overlay" on:click={() => mostrarModalExportar = false} role="dialog" aria-modal="true">
    <div class="modal exp-modal" on:click|stopPropagation>

      <div class="modal__header">
        <h3 class="modal__title">Exportar métricas</h3>
        <button class="modal__close" on:click={() => mostrarModalExportar = false}>×</button>
      </div>

      <div class="exp-modal-body">

        <!-- Período -->
        <div class="exp-periodo">
          <svg width="13" height="13" viewBox="0 0 20 20" fill="currentColor" style="color:#D4AF37;flex-shrink:0">
            <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
          </svg>
          <span>Período: <strong>{metFechaDesde}</strong> → <strong>{metFechaHasta}</strong></span>
        </div>

        <!-- Secciones -->
        <div class="exp-bloque">
          <div class="exp-bloque-header">
            <span class="exp-bloque-titulo">Secciones a incluir</span>
            <button class="exp-link" on:click={() => Object.keys(expSec).forEach(k => expSec[k] = true)}>Todo</button>
            <span style="color:#ccc">·</span>
            <button class="exp-link" on:click={() => Object.keys(expSec).forEach(k => expSec[k] = false)}>Nada</button>
          </div>
          <div class="exp-checks">
            {#each [
              { key:'kpi',              label:'KPI de Ingresos'           },
              { key:'busquedasDiarias', label:'Búsquedas diarias'         },
              { key:'canal',            label:'Canales de búsqueda'        },
              { key:'embudo',           label:'Embudo de conversión'       },
              { key:'rutas',            label:'Rendimiento de rutas'       },
              { key:'cancelaciones',    label:'Análisis de cancelaciones'  },
              { key:'tendencia',        label:'Tendencia de ingresos'      },
              { key:'heatmap',          label:'Mapa de calor búsquedas'    },
              { key:'registro',         label:'Registro completo'          },
            ] as s}
              <label class="exp-check-label">
                <input type="checkbox" bind:checked={expSec[s.key]} class="exp-check-input" />
                {s.label}
              </label>
            {/each}
          </div>
        </div>

        <!-- Formato -->
        <div class="exp-bloque">
          <span class="exp-bloque-titulo">Formato</span>
          <div class="exp-format-tabs">
            <button class="exp-format-tab" class:exp-format-tab--active={expFormato==='excel'}
              on:click={() => expFormato='excel'}>
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/>
              </svg>
              Excel (.xlsx)
              <span class="exp-format-tag">Recomendado</span>
            </button>
            <button class="exp-format-tab" class:exp-format-tab--active={expFormato==='csv'}
              on:click={() => expFormato='csv'}>
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/>
              </svg>
              CSV (.zip)
            </button>
            <button class="exp-format-tab" class:exp-format-tab--active={expFormato==='pdf'}
              on:click={() => expFormato='pdf'}>
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
              </svg>
              PDF (.pdf)
              <span class="exp-format-tag" style="background:#dc2626">Gráficas</span>
            </button>
            <button class="exp-format-tab" class:exp-format-tab--active={expFormato==='email'}
              on:click={() => expFormato='email'}>
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
              </svg>
              Por correo
            </button>
          </div>
        </div>

        <!-- Correos (solo si email) -->
        {#if expFormato === 'email'}
          <!-- Formato del adjunto -->
          <div class="exp-bloque">
            <span class="exp-bloque-titulo">Formato del adjunto</span>
            <div class="exp-format-tabs exp-format-tabs--sm">
              <button class="exp-format-tab" class:exp-format-tab--active={expFormatoEmail==='excel'}
                on:click={() => expFormatoEmail='excel'}>
                <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/>
                </svg>
                Excel (.xlsx)
              </button>
              <button class="exp-format-tab" class:exp-format-tab--active={expFormatoEmail==='csv'}
                on:click={() => expFormatoEmail='csv'}>
                <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/>
                </svg>
                CSV (.zip)
              </button>
              <button class="exp-format-tab" class:exp-format-tab--active={expFormatoEmail==='pdf'}
                on:click={() => expFormatoEmail='pdf'}>
                <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
                </svg>
                PDF (.pdf)
              </button>
            </div>
          </div>
          <div class="exp-bloque">
            <span class="exp-bloque-titulo">Destinatarios</span>
            {#each correosExportar as _, i}
              <div class="exp-correo-row">
                <input type="email" class="met-input" placeholder="correo@ejemplo.com"
                  bind:value={correosExportar[i]} />
                {#if correosExportar.length > 1}
                  <button class="exp-correo-remove"
                    on:click={() => correosExportar = correosExportar.filter((_,j) => j !== i)}>
                    <svg width="13" height="13" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
                    </svg>
                  </button>
                {/if}
              </div>
            {/each}
            <button class="exp-correo-add" on:click={() => correosExportar = [...correosExportar, '']}>
              <svg width="12" height="12" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
              </svg>
              Agregar destinatario
            </button>
          </div>
        {/if}

        <!-- Acciones -->
        <div class="exp-actions">
          <button class="btn-secondary" on:click={() => { mostrarModalExportar = false; correosExportar = ['']; }}>
            Cancelar
          </button>
          <button class="btn-primary" disabled={enviandoExportar || !Object.values(expSec).some(v=>v)}
            on:click={handleExportar}>
            {#if enviandoExportar}
              {expFormato === 'email' ? 'Enviando...' : 'Generando...'}
            {:else if expFormato === 'email'}
              Enviar por correo
            {:else if expFormato === 'excel'}
              Descargar Excel
            {:else if expFormato === 'pdf'}
              Descargar PDF
            {:else}
              Descargar CSV
            {/if}
          </button>
        </div>

      </div>
    </div>
  </div>
{/if}
