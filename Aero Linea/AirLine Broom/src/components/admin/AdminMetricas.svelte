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

  /** Correo electronico ingresado en el modal de exportacion. @type {string} */
  let correoExportar       = '';

  /** Indica si el modal de exportacion por correo esta visible. @type {boolean} */
  let mostrarModalExportar = false;

  /** Indica si la solicitud de exportacion por correo a la API esta en progreso. @type {boolean} */
  let enviandoCorreo       = false;

  /**
   * Al montar: carga las metricas de resumen y la primera pagina del registro de busquedas en paralelo.
   */
  onMount(() => {
    cargarMetricas();
    cargarListadoBusquedas(1);
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
   * Vuelve a aplicar todos los filtros actuales recargando las metricas de resumen y la pagina 1
   * del registro de busquedas de forma simultanea.
   */
  function aplicarFiltros() {
    cargarMetricas();
    cargarListadoBusquedas(1);
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
  async function handleExportarCorreo() {
    if (!correoExportar) return;
    enviandoCorreo = true;
    try {
      const r = await fetch(`${API}/api/metricas/exportar-correo`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          correo:     correoExportar,
          fechaDesde: metFechaDesde,
          fechaHasta: metFechaHasta,
          tipo:       metTipo    || null,
          usuario:    metUsuario || null
        })
      });
      if (r.ok) {
        mostrarToast('success', `Listado enviado a ${correoExportar}`);
        mostrarModalExportar = false;
        correoExportar = '';
      } else { mostrarToast('error', 'No se pudo enviar el correo'); }
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { enviandoCorreo = false; }
  }

  // Tasa de conversion: total de reservaciones dividido por total de busquedas, expresado como porcentaje.
  // Evalua a '0.0' cuando no hay busquedas o no hay datos de resumen aun.
  $: tasaConversion = (metricasResumen && metricasResumen.totalBusquedas > 0)
    ? ((metricasResumen.ingresosKpi.totalReservaciones / metricasResumen.totalBusquedas) * 100).toFixed(1)
    : '0.0';
</script>

<!-- Seccion de analiticos con KPIs, graficas SVG, registro de busquedas y exportacion -->
<section class="admin-section met-section">
  <div class="met-header">
    <div>
      <h2 class="admin-section__title">Analiticos y Reportes</h2>
      <p class="admin-section__subtitle">Busquedas, ingresos y conversion del sistema</p>
    </div>
  </div>

  <!-- Controles de filtro por rango de fechas, canal y usuario -->
  <div class="met-filtros">
    <div class="met-filtro-grupo">
      <label for="mf-desde" class="met-label">Desde</label>
      <input id="mf-desde" type="date" class="met-input" bind:value={metFechaDesde} />
    </div>
    <div class="met-filtro-grupo">
      <label for="mf-hasta" class="met-label">Hasta</label>
      <input id="mf-hasta" type="date" class="met-input" bind:value={metFechaHasta} />
    </div>
    <div class="met-filtro-grupo">
      <label for="mf-canal" class="met-label">Canal</label>
      <select id="mf-canal" class="met-input" bind:value={metTipo}>
        <option value="">Todos</option>
        <option value="Web">Web</option>
        <option value="REST">REST</option>
      </select>
    </div>
    <div class="met-filtro-grupo">
      <label for="mf-usuario" class="met-label">Usuario</label>
      <input id="mf-usuario" type="text" class="met-input"
        placeholder="username..." bind:value={metUsuario} />
    </div>
    <button class="met-btn-aplicar" on:click={aplicarFiltros} disabled={loadingMetricas}>
      {loadingMetricas ? 'Cargando...' : 'Aplicar filtros'}
    </button>
  </div>

  {#if loadingMetricas}
    <div class="met-loading"><div class="met-spinner"></div><p>Cargando analiticos...</p></div>
  {:else if metricasResumen}

    <!-- Banner de KPIs de ingresos: total, por clase, ticket promedio y tasa de conversion -->
    {#each [metricasResumen.ingresosKpi ?? {ingresosTotales:0,ingresosTurista:0,ingresosEjecutivo:0,totalBoletos:0,totalReservaciones:0,ticketPromedio:0}] as kpi}
      <div class="met-ingresos-banner">
        <div class="met-ing-main">
          <span class="met-ing-label">Ingresos Totales</span>
          <span class="met-ing-valor">
            ${kpi.ingresosTotales.toLocaleString('es-GT', {minimumFractionDigits:2, maximumFractionDigits:2})}
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
              <span class="met-ing-stat__label">Busquedas totales</span>
              <span class="met-ing-stat__val">{metricasResumen.totalBusquedas.toLocaleString()}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot" style="background:#a78bfa"></span>
            <div>
              <span class="met-ing-stat__label">Tasa conversion</span>
              <span class="met-ing-stat__val">{tasaConversion}%</span>
            </div>
          </div>
        </div>
      </div>
    {/each}

    <!-- Graficas SVG: busquedas diarias, top rutas por demanda e ingresos por clase -->
    <div class="met-graficas">

      <div class="met-grafica met-grafica--wide">
        <h3 class="met-grafica__titulo">Busquedas diarias</h3>
        <p class="met-grafica__subtitulo">Volumen de busquedas por dia en el periodo seleccionado</p>
        {#if metricasResumen.busquedasPorDia.length === 0}
          <div class="met-empty">Sin datos en el periodo seleccionado</div>
        {:else}
          {@const datos  = metricasResumen.busquedasPorDia}
          {@const maxVal = Math.max(...datos.map(d => d.total), 1)}
          {@const W=700} {@const H=220}
          {@const PAD={l:44,r:20,t:20,b:30}}
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
                    <title>{d.fecha}: {d.total} busquedas</title>
                  </circle>
                {/each}
              {:else}
                <circle cx={PAD.l+gW/2} cy={PAD.t+gH/2} r="6" fill="#D4AF37"/>
              {/if}
              {#each datos as d,i}
                {#if i % Math.ceil(datos.length/7) === 0 || i === datos.length-1}
                  {@const x=datos.length>1 ? PAD.l+(i/(datos.length-1))*gW : PAD.l+gW/2}
                  <text x={x} y={H-6} font-size="8" fill="#b8b0a5" text-anchor="middle">{d.fecha.slice(5)}</text>
                {/if}
              {/each}
            </svg>
          </div>
          <div class="met-grafica__leyenda">
            <span><span class="met-leyenda-dot" style="background:#D4AF37"></span> Busquedas diarias</span>
          </div>
        {/if}
      </div>

      <div class="met-grafica">
        <h3 class="met-grafica__titulo">Top rutas por demanda</h3>
        <p class="met-grafica__subtitulo">Rutas mas buscadas en el periodo seleccionado</p>
        {#if metricasResumen.rutasMasBuscadas.length === 0}
          <div class="met-empty">Sin datos en el periodo seleccionado</div>
        {:else}
          {@const maxBusc = Math.max(...metricasResumen.rutasMasBuscadas.map(r => r.total), 1)}
          <div class="met-barras-dobles">
            {#each metricasResumen.rutasMasBuscadas as ruta, i}
              <div class="met-barra-doble-row">
                <div class="met-barra-doble-label">
                  <span class="met-barra-rank">#{i+1}</span>
                  <span class="met-barra-ruta">{ruta.origenCodigo} → {ruta.destinoCodigo}</span>
                </div>
                <div class="met-barra-doble-tracks">
                  <div class="met-barra-doble-track">
                    <div class="met-barra-fill met-barra-fill--gold"
                      style="width:{(ruta.total/maxBusc)*100}%"></div>
                    <span class="met-barra-val">{ruta.total} busq.</span>
                  </div>
                </div>
              </div>
            {/each}
          </div>
          <div class="met-grafica__leyenda">
            <span><span class="met-leyenda-dot" style="background:#D4AF37"></span> Busquedas</span>
          </div>
        {/if}
      </div>

      <div class="met-grafica met-grafica--donut">
        <h3 class="met-grafica__titulo">Ingresos por clase</h3>
        <p class="met-grafica__subtitulo">Contribucion de Turista vs Ejecutivo al revenue total</p>
        {#if !metricasResumen.distribucionClase || metricasResumen.distribucionClase.length === 0}
          <div class="met-empty">Sin boletos vendidos en el periodo</div>
        {:else}
          {@const donutData     = metricasResumen.distribucionClase}
          {@const totalIngClase = donutData.reduce((s,c) => s+(c.ingresos??0), 0) || 1}
          {@const colores       = ['#D4AF37','#1C1A18','#10b981','#C9A961']}
          {@const segClase = (() => {
            let ang = -90;
            return donutData.map((c,i) => {
              const ing  = c.ingresos ?? 0;
              const pct  = ing / totalIngClase;
              const start = ang;
              ang += pct * 360;
              return {
                clase:    c.clase ?? c.Clase ?? '',
                ingresos: ing,
                boletos:  c.boletos ?? c.Boletos ?? 0,
                start, angle: pct*360,
                color: colores[i % colores.length],
                pct: (pct*100).toFixed(1)
              };
            });
          })()}
          <div class="met-donut-wrap">
            <svg viewBox="0 0 200 200" class="met-donut-svg">
              {#each segClase as seg}
                {#if seg.angle > 0.5}
                  {@const cx=100} {@const cy=100} {@const R=80} {@const r=48}
                  {@const s=seg.start*Math.PI/180}
                  {@const e=(seg.start+seg.angle)*Math.PI/180}
                  {@const large=seg.angle>180?1:0}
                  {@const x1=cx+R*Math.cos(s)} {@const y1=cy+R*Math.sin(s)}
                  {@const x2=cx+R*Math.cos(e)} {@const y2=cy+R*Math.sin(e)}
                  {@const x3=cx+r*Math.cos(e)} {@const y3=cy+r*Math.sin(e)}
                  {@const x4=cx+r*Math.cos(s)} {@const y4=cy+r*Math.sin(s)}
                  <path d="M{x1},{y1} A{R},{R} 0 {large},1 {x2},{y2} L{x3},{y3} A{r},{r} 0 {large},0 {x4},{y4} Z"
                    fill={seg.color} stroke="white" stroke-width="2">
                    <title>{seg.clase}: ${seg.ingresos.toLocaleString('es-GT')} ({seg.pct}%)</title>
                  </path>
                {/if}
              {/each}
              <circle cx="100" cy="100" r="44" fill="white"/>
              <text x="100" y="96" text-anchor="middle" font-size="11" font-weight="700" fill="#1C1A18">
                ${(totalIngClase/1000).toFixed(1)}k
              </text>
              <text x="100" y="111" text-anchor="middle" font-size="8" fill="#b8b0a5">INGRESOS</text>
            </svg>
            <div class="met-donut-leyenda">
              {#each segClase as seg}
                <div class="met-ley-item">
                  <span class="met-leyenda-dot" style="background:{seg.color}"></span>
                  <div>
                    <span class="met-ley-tipo">{seg.clase}</span>
                    <span class="met-ley-num">${seg.ingresos.toLocaleString('es-GT')} <em>({seg.pct}%)</em></span>
                    <span class="met-ley-sub">{seg.boletos} boletos</span>
                  </div>
                </div>
              {/each}
            </div>
          </div>
        {/if}
      </div>

    </div>

    <!-- Desglose de busquedas por canal: Web vs REST con barras de porcentaje -->
    {#if metricasResumen.busquedasPorTipo && metricasResumen.busquedasPorTipo.length > 0}
      {@const totalTipo = metricasResumen.totalBusquedas || 1}
      <div class="met-canal-wrap">
        <h3 class="met-canal__titulo">Canal de busqueda: Web vs REST</h3>
        <div class="met-canal-pills">
          {#each metricasResumen.busquedasPorTipo as tipo}
            {@const pct   = ((tipo.total/totalTipo)*100).toFixed(1)}
            {@const color = tipo.tipo === 'Web' ? '#D4AF37' : '#1C1A18'}
            <div class="met-canal-pill">
              <div class="met-canal-bar" style="--pct:{pct}%;--color:{color}"></div>
              <span class="met-canal-tipo">{tipo.tipo}</span>
              <span class="met-canal-num">{tipo.total.toLocaleString()} <em>({pct}%)</em></span>
            </div>
          {/each}
        </div>
      </div>
    {/if}

  {/if}

  <!-- Tabla paginada del registro de busquedas con opcion de exportacion por correo -->
  <div class="met-listado">
    <div class="met-listado__header">
      <h3 class="met-listado__titulo">Registro de busquedas</h3>
      <button class="met-btn-exportar" on:click={() => mostrarModalExportar = true}>
        Exportar por correo
      </button>
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
                <td class="table__cell">{b.usuario ?? '—'}</td>
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

</section>

<!-- Modal de exportacion del listado filtrado a un correo electronico -->
{#if mostrarModalExportar}
  <div class="modal-overlay" on:click={() => mostrarModalExportar = false}
    role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:420px">
      <div class="modal__header">
        <h3 class="modal__title">Exportar listado</h3>
        <button class="modal__close" on:click={() => mostrarModalExportar = false}>×</button>
      </div>
      <div style="padding:1.5rem;display:flex;flex-direction:column;gap:1rem">
        <p style="color:var(--text-muted);font-size:0.9rem">
          El listado filtrado actual se enviara como archivo adjunto al correo indicado.
        </p>
        <div class="form-field">
          <label for="met-correo" class="met-label">Correo electronico</label>
          <input id="met-correo" type="email" class="met-input"
            placeholder="correo@ejemplo.com"
            bind:value={correoExportar} />
        </div>
        <div style="display:flex;gap:1rem;justify-content:flex-end;margin-top:0.5rem">
          <button class="btn-secondary" on:click={() => mostrarModalExportar = false}>Cancelar</button>
          <button class="btn-primary"
            disabled={enviandoCorreo || !correoExportar}
            on:click={handleExportarCorreo}>
            {enviandoCorreo ? 'Enviando...' : 'Enviar'}
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}
