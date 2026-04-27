<style>
/* ── Variables locales ──────────────────────────────────────────────── */
.met-section {
  --bg:         #161b22;
  --bg2:        #1c2128;
  --bg3:        #21262d;
  --border:     rgba(255,255,255,0.08);
  --border2:    rgba(255,255,255,0.13);
  --text:       #e6edf3;
  --text-muted: #8b949e;
  --accent:     #667eea;
  --gold:       #D4AF37;
  --gold-dim:   rgba(212,175,55,0.12);
  --radius:     10px;
  --transition: all .18s ease;
}
@keyframes metSpin { to { transform:rotate(360deg); } }
@keyframes sectionFadeIn { from{opacity:0;transform:translateY(8px)} to{opacity:1;transform:none} }

/* ── Layout contenedor ───────────────────────────────────────────────── */
.admin-section { animation:sectionFadeIn .3s ease-out; }
.admin-section__title { font-size:1.5rem; font-weight:700; color:var(--text); margin:0 0 .35rem; }
.admin-section__subtitle { color:var(--text-muted); margin:0 0 1.5rem; font-size:.88rem; }

/* ── Header con botón exportar ──────────────────────────────────────── */
.met-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:1.5rem; }
.met-btn-exportar {
  display:flex; align-items:center; gap:.4rem; padding:.55rem 1.25rem;
  background:transparent; border:1.5px solid var(--border2);
  border-radius:0 10px 0 10px; font-size:.78rem; font-weight:700;
  text-transform:uppercase; letter-spacing:1px; cursor:pointer;
  color:var(--text-muted); transition:var(--transition); font-family:inherit;
}
.met-btn-exportar:hover { background:var(--bg3); color:var(--text); border-color:var(--accent); }

/* ── Panel de filtros ───────────────────────────────────────────────── */
.met-filtros-panel {
  background:var(--bg); border:1px solid var(--border);
  border-radius:var(--radius); padding:1.1rem 1.4rem;
  margin-bottom:1.25rem; display:flex; flex-direction:column; gap:.85rem;
}
.met-filtro-bloque--periodos { display:flex; align-items:center; gap:1rem; flex-wrap:wrap; }
.met-label { font-size:.7rem; font-weight:700; text-transform:uppercase; letter-spacing:.5px; color:var(--text-muted); }
.met-periodo-btns { display:flex; gap:.35rem; flex-wrap:wrap; }
.met-periodo-btn {
  padding:.35rem .8rem; background:var(--bg2); border:1.5px solid var(--border);
  border-radius:6px; font-size:.75rem; font-weight:600; cursor:pointer;
  color:var(--text-muted); transition:var(--transition); font-family:inherit;
}
.met-periodo-btn:hover { background:var(--bg3); color:var(--text); }
.met-periodo-btn--active { background:var(--gold); color:#0d1117; border-color:var(--gold); }
.met-filtros-row { display:flex; align-items:flex-end; gap:.85rem; flex-wrap:wrap; }
.met-filtro-grupo { display:flex; flex-direction:column; gap:.3rem; min-width:120px; }
.met-input {
  padding:.6rem .85rem; background:var(--bg2); border:1.5px solid var(--border);
  border-radius:0 8px 0 8px; font-size:.85rem; font-family:inherit;
  color:var(--text); transition:var(--transition); outline:none;
}
.met-input:focus { border-color:var(--accent); box-shadow:0 0 0 3px rgba(102,126,234,.12); }
.met-input option { background:#1c2128; }
.met-btn-aplicar {
  padding:.62rem 1.5rem; background:var(--accent); color:white; border:none;
  border-radius:0 10px 0 10px; font-size:.78rem; font-weight:700;
  text-transform:uppercase; letter-spacing:1px; cursor:pointer;
  transition:var(--transition); align-self:flex-end; font-family:inherit;
}
.met-btn-aplicar:hover:not(:disabled) { filter:brightness(1.12); transform:translateY(-1px); }
.met-btn-aplicar:disabled { opacity:.5; cursor:not-allowed; }

/* ── Selector de panel ──────────────────────────────────────────────── */
.met-panel-selector {
  display:flex; gap:.25rem; border-bottom:1.5px solid var(--border);
  margin-bottom:1.25rem; flex-wrap:wrap;
}
.met-panel-btn {
  display:inline-flex; align-items:center; gap:.4rem; padding:.62rem 1.1rem;
  background:transparent; border:none; border-bottom:2.5px solid transparent;
  color:var(--text-muted); font-size:.85rem; font-weight:500;
  cursor:pointer; margin-bottom:-1.5px; transition:var(--transition);
  font-family:inherit; border-radius:6px 6px 0 0;
}
.met-panel-btn:hover { color:var(--text); background:rgba(255,255,255,.04); }
.met-panel-btn--active { color:var(--accent); border-bottom-color:var(--accent); background:rgba(102,126,234,.06); }
.met-panel-btn--active svg { color:var(--accent); }

/* ── Loading / Empty ────────────────────────────────────────────────── */
.met-loading { display:flex; flex-direction:column; align-items:center; gap:.85rem; padding:4rem; color:var(--text-muted); }
.met-loading p { margin:0; font-size:.88rem; }
.met-loading--sm { padding:2rem; }
.met-spinner { width:32px; height:32px; border:3px solid var(--border2); border-top-color:var(--gold); border-radius:50%; animation:metSpin .8s linear infinite; }
.met-empty { text-align:center; padding:2.5rem; color:var(--text-muted); font-size:.88rem; }

/* ── Banner KPI de ingresos ─────────────────────────────────────────── */
.met-ingresos-banner {
  display:flex; align-items:center; gap:2rem;
  background:linear-gradient(135deg,#1a2035,#252b3b);
  border:1px solid var(--border); border-radius:12px;
  padding:1.4rem 1.75rem; margin-bottom:1.25rem; flex-wrap:wrap;
}
.met-ing-main { display:flex; flex-direction:column; min-width:180px; border-right:1px solid var(--border2); padding-right:2rem; flex-shrink:0; }
.met-ing-label { font-size:.68rem; letter-spacing:.1em; text-transform:uppercase; color:var(--text-muted); margin-bottom:.3rem; }
.met-ing-valor { font-size:1.8rem; font-weight:800; color:var(--gold); line-height:1.1; }
.met-ing-sub   { font-size:.75rem; color:var(--text-muted); margin-top:.3rem; }
.met-ing-stats { display:flex; gap:1.5rem; flex-wrap:wrap; flex:1; }
.met-ing-stat  { display:flex; align-items:flex-start; gap:.45rem; }
.met-ing-stat__dot { width:9px; height:9px; border-radius:50%; margin-top:4px; flex-shrink:0; }
.met-ing-stat__dot--turista   { background:var(--gold); }
.met-ing-stat__dot--ejecutivo { background:#10b981; }
.met-ing-stat__dot--ticket    { background:#94a3b8; }
.met-ing-stat__dot--busq      { background:#60a5fa; }
.met-ing-stat__label { display:block; font-size:.67rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:.05em; }
.met-ing-stat__val   { display:block; font-size:.9rem; font-weight:700; color:var(--text); }

/* ── Tarjeta de gráfica ─────────────────────────────────────────────── */
.met-grafica {
  background:var(--bg); border:1px solid var(--border);
  border-radius:4px 16px 4px 16px; padding:1.4rem; margin-bottom:1.25rem;
}
.met-grafica--wide { display:block; }
.met-grafica__titulo    { font-size:.95rem; font-weight:700; color:var(--text); margin:0 0 .2rem; }
.met-grafica__subtitulo { font-size:.75rem; color:var(--text-muted); margin:0 0 1rem; line-height:1.5; }
.met-svg-wrap { width:100%; overflow-x:auto; }
.met-svg { width:100%; height:auto; max-height:200px; display:block; }
.met-grafica__leyenda { display:flex; align-items:center; gap:.5rem; font-size:.75rem; color:var(--text-muted); margin-top:.85rem; flex-wrap:wrap; }
.met-leyenda-dot { display:inline-block; width:11px; height:11px; border-radius:50%; flex-shrink:0; }

/* ── Canal split ────────────────────────────────────────────────────── */
.met-canal-wrap   { background:var(--bg2); border:1px solid var(--border); border-radius:10px; padding:1.1rem 1.4rem; margin-bottom:1.25rem; }
.met-canal__titulo { font-size:.88rem; font-weight:700; color:var(--text); margin:0 0 .7rem; }
.met-canal__subtitulo { font-size:.74rem; color:var(--text-muted); margin:0 0 .8rem; line-height:1.5; }
.met-canal-pills  { display:flex; gap:1.25rem; flex-wrap:wrap; }
.met-canal-pill   { flex:1; min-width:140px; display:flex; flex-direction:column; gap:.28rem; }
.met-canal-bar    { height:7px; border-radius:4px; background:linear-gradient(to right, var(--color,var(--gold)) var(--pct,0%), rgba(255,255,255,.06) var(--pct,0%)); }
.met-canal-tipo   { font-size:.8rem; font-weight:600; }
.met-canal-num    { font-size:.75rem; color:var(--text-muted); }

/* ── Tarjetas análisis de negocio ───────────────────────────────────── */
.neg-card { background:var(--bg); border:1px solid var(--border); border-radius:4px 16px 4px 16px; padding:1.4rem; margin-bottom:1.25rem; }
.neg-sub-titulo { font-size:.85rem; font-weight:700; color:var(--text); margin:0 0 .75rem; }

/* Embudo */
.neg-funnel { display:flex; flex-direction:column; gap:.5rem; margin:.75rem 0 1rem; }
.neg-funnel-row { display:grid; grid-template-columns:200px 1fr 140px; align-items:center; gap:.75rem; }
.neg-funnel-label { font-size:.84rem; color:var(--text-muted); font-weight:500; }
.neg-funnel-track { height:13px; background:rgba(255,255,255,.05); border-radius:7px; overflow:hidden; }
.neg-funnel-bar   { height:100%; border-radius:7px; transition:width .4s ease; }
.neg-funnel-vals  { display:flex; align-items:center; gap:.4rem; }
.neg-funnel-num   { font-size:.9rem; font-weight:700; color:var(--text); }
.neg-funnel-pct   { font-size:.78rem; }
.neg-funnel-pct--over { font-weight:700; text-decoration:underline dotted; }
.neg-funnel-kpis  { display:flex; gap:1.75rem; margin-top:.5rem; padding-top:1rem; border-top:1px solid var(--border); flex-wrap:wrap; }
.neg-kpi      { display:flex; flex-direction:column; gap:.1rem; }
.neg-kpi-val  { font-size:1.3rem; font-weight:800; line-height:1; }
.neg-kpi-num  { font-size:.8rem; font-weight:600; color:var(--text); }
.neg-kpi-label{ font-size:.7rem; color:var(--text-muted); }

/* Top hoteles / rutas */
.ruta-total-banner { display:flex; align-items:center; justify-content:space-between; background:rgba(212,175,55,.08); border:1px solid rgba(212,175,55,.2); border-radius:8px; padding:.65rem 1rem; margin-bottom:.75rem; }
.ruta-total-label { font-size:.78rem; color:var(--gold); font-weight:500; }
.ruta-total-val   { font-size:1.15rem; font-weight:800; color:var(--text); }
.ruta-tabla { display:flex; flex-direction:column; margin-top:.5rem; border:1px solid var(--border); border-radius:8px; overflow:hidden; }
.ruta-tabla-header { display:grid; grid-template-columns:36px 1fr 120px 1fr; align-items:center; gap:.75rem; padding:.6rem 1rem; background:var(--bg2); border-bottom:1px solid var(--border); font-size:.72rem; font-weight:700; text-transform:uppercase; letter-spacing:.04em; color:var(--text-muted); }
.ruta-tabla-row    { display:grid; grid-template-columns:36px 1fr 120px 1fr; align-items:center; gap:.75rem; padding:.65rem 1rem; border-bottom:1px solid var(--border); transition:background .12s; }
.ruta-tabla-row:last-child { border-bottom:none; }
.ruta-tabla-row:hover { background:var(--bg2); }
.ruta-tabla-row--sin-ing { opacity:.65; }
.ruta-rank { font-size:.72rem; color:var(--text-muted); font-weight:700; }
.ruta-nombre { display:flex; align-items:center; gap:.3rem; font-size:.85rem; color:var(--text); }
.ruta-flecha { color:var(--text-muted); }
.ruta-col-center { text-align:center; }
.ruta-stat { font-size:.82rem; font-weight:600; color:var(--text); }
.ruta-ing-wrap { display:flex; align-items:center; gap:.4rem; min-width:0; }
.ruta-ing-pct-label { font-size:.72rem; font-weight:700; color:var(--gold); min-width:36px; text-align:right; }
.ruta-ing-bar-bg { flex:1; height:8px; background:rgba(255,255,255,.06); border-radius:4px; overflow:hidden; min-width:40px; }
.ruta-ing-bar { height:100%; background:var(--gold); border-radius:4px; transition:width .4s ease; }
.ruta-ing-val { font-size:.82rem; font-weight:700; color:var(--text); white-space:nowrap; }

/* Cancelaciones donut */
.neg-tipo-wrap    { display:flex; align-items:center; gap:1.25rem; flex-wrap:wrap; }
.neg-tipo-svg     { width:100px; height:100px; flex-shrink:0; }
.neg-tipo-leyenda { display:flex; flex-direction:column; gap:.4rem; }
.neg-ley-row      { display:flex; align-items:center; gap:.4rem; font-size:.82rem; color:var(--text); }
.neg-ley-num      { margin-left:auto; font-weight:700; font-size:.82rem; }
.neg-ley-num em   { font-style:normal; font-weight:400; color:var(--text-muted); }

/* Tendencia ingresos */
.neg-ingresos-combo { display:grid; grid-template-columns:auto 1fr; gap:1.5rem; align-items:start; }
.neg-combo-donut { display:flex; align-items:flex-start; gap:1rem; }
.neg-combo-tendencia { min-width:0; }
.tend-wrap { display:grid; grid-template-columns:1fr auto; gap:1.25rem; align-items:start; }
.tend-chart { min-width:0; }
.tend-resumen { background:var(--bg2); border:1px solid var(--border); border-radius:8px; padding:.85rem 1rem; min-width:130px; display:flex; flex-direction:column; gap:.75rem; }
.tend-resumen__bloque { display:flex; flex-direction:column; gap:.35rem; }
.tend-resumen__titulo { font-size:.72rem; font-weight:700; text-transform:uppercase; letter-spacing:.06em; color:var(--text-muted); margin:0; }
.tend-resumen__mes { font-weight:400; text-transform:none; letter-spacing:0; color:var(--text-muted); }
.tend-resumen__fila { display:flex; align-items:center; gap:.4rem; }
.tend-resumen__dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.tend-resumen__clase { font-size:.75rem; color:var(--text-muted); flex:1; }
.tend-resumen__val { font-size:.75rem; font-weight:700; color:var(--text); white-space:nowrap; }
.tend-resumen__val--total { font-size:.78rem; color:var(--text); }
.tend-resumen__sep { border-top:1px solid var(--border); margin:.5rem 0; }

/* Heatmap */
.neg-heat-leyenda { display:flex; align-items:center; gap:.75rem; flex-wrap:wrap; margin-top:.75rem; }
.neg-heat-item    { display:flex; align-items:center; gap:.3rem; font-size:.78rem; color:var(--text-muted); }
.neg-heat-dot     { width:14px; height:14px; border-radius:3px; display:inline-block; }
.heat-tip-row { display:flex; flex-wrap:wrap; gap:.4rem .75rem; margin-top:.4rem; }
.heat-tip { display:flex; align-items:center; gap:.3rem; font-size:.72rem; color:var(--text-muted); }

/* ── Listado de búsquedas ────────────────────────────────────────────── */
.met-listado { background:var(--bg); border:1px solid var(--border); border-radius:4px 16px 4px 16px; padding:1.5rem; }
.met-listado__header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1.1rem; }
.met-listado__titulo { font-size:.95rem; font-weight:700; color:var(--text); margin:0; }
.met-tabla-wrap { overflow-x:auto; }

/* Tabla genérica */
.table { width:100%; border-collapse:collapse; font-size:.85rem; }
.table__head { }
.table__header { background:var(--bg2); color:var(--text-muted); font-size:.72rem; font-weight:700; text-transform:uppercase; letter-spacing:.05em; padding:.75rem .875rem; text-align:left; border-bottom:1px solid var(--border); white-space:nowrap; }
.table__row { border-bottom:1px solid var(--border); transition:background .12s; }
.table__row:last-child { border-bottom:none; }
.table__row:hover { background:var(--bg2); }
.table__cell { padding:.65rem .875rem; color:var(--text); vertical-align:middle; }

/* Badges tipo canal */
.met-tipo-badge { display:inline-block; padding:.15rem .55rem; border-radius:999px; font-size:.7rem; font-weight:600; }
.met-tipo-badge--web  { background:rgba(212,175,55,.12); color:var(--gold); border:1px solid rgba(212,175,55,.3); }
.met-tipo-badge--rest { background:rgba(16,185,129,.12); color:#10b981; border:1px solid rgba(16,185,129,.3); }

/* Paginación */
.met-paginado { display:flex; align-items:center; justify-content:center; gap:1.1rem; padding:1.1rem 0 0; border-top:1px solid var(--border); margin-top:1rem; flex-wrap:wrap; }
.met-pag-btn { padding:.5rem 1rem; background:var(--bg2); color:var(--text-muted); border:1px solid var(--border); border-radius:0 8px 0 8px; font-size:.78rem; font-weight:600; cursor:pointer; transition:var(--transition); font-family:inherit; }
.met-pag-btn:hover:not(:disabled) { background:var(--accent); color:white; border-color:var(--accent); }
.met-pag-btn:disabled { opacity:.3; cursor:not-allowed; }
.met-pag-info { font-size:.8rem; color:var(--text-muted); }

/* ── Modal genérico ─────────────────────────────────────────────────── */
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,.72); backdrop-filter:blur(6px); z-index:2000; display:flex; align-items:center; justify-content:center; padding:1rem; }
.modal { background:#1e2330; border:1px solid rgba(255,255,255,.1); border-radius:16px; box-shadow:0 32px 80px rgba(0,0,0,.8); width:100%; overflow:hidden; max-height:90vh; display:flex; flex-direction:column; }
.modal__header { display:flex; align-items:center; justify-content:space-between; padding:1.25rem 1.5rem; background:#252b3b; border-bottom:1px solid rgba(255,255,255,.08); flex-shrink:0; }
.modal__title { font-size:.95rem; font-weight:700; color:#f0f4ff; margin:0; }
.modal__close { background:rgba(255,255,255,.07); border:none; color:#8b95b0; cursor:pointer; width:30px; height:30px; border-radius:7px; display:flex; align-items:center; justify-content:center; font-size:1.2rem; transition:var(--transition); }
.modal__close:hover { background:rgba(255,255,255,.14); color:#f0f4ff; }

/* ── Modal exportar ─────────────────────────────────────────────────── */
.exp-modal { max-width:540px; }
.exp-modal-body { padding:1.25rem 1.5rem 1.5rem; display:flex; flex-direction:column; gap:1rem; overflow-y:auto; }
.exp-periodo { display:flex; align-items:center; gap:.4rem; font-size:.82rem; color:var(--text-muted); background:var(--bg2); border-radius:8px; padding:.6rem .9rem; }
.exp-bloque { display:flex; flex-direction:column; gap:.5rem; }
.exp-bloque-header { display:flex; align-items:center; gap:.4rem; }
.exp-bloque-titulo { font-size:.72rem; font-weight:700; color:var(--text-muted); text-transform:uppercase; letter-spacing:.5px; flex:1; }
.exp-link { font-size:.72rem; color:var(--gold); background:none; border:none; cursor:pointer; padding:0; font-weight:600; font-family:inherit; }
.exp-link:hover { text-decoration:underline; }
.exp-checks { display:grid; grid-template-columns:1fr 1fr; gap:.3rem .75rem; }
.exp-check-label { display:flex; align-items:center; gap:.4rem; font-size:.8rem; color:var(--text-muted); cursor:pointer; transition:color .12s; }
.exp-check-label:hover { color:var(--text); }
.exp-check-input { accent-color:var(--gold); width:14px; height:14px; cursor:pointer; }
.exp-format-tabs { display:flex; gap:.45rem; flex-wrap:wrap; }
.exp-format-tabs--sm .exp-format-tab { padding:.38rem .7rem; font-size:.73rem; }
.exp-format-tab {
  display:flex; align-items:center; gap:.35rem; padding:.5rem .85rem;
  border:1.5px solid var(--border2); border-radius:8px; background:var(--bg2);
  font-size:.8rem; font-weight:500; color:var(--text-muted);
  cursor:pointer; transition:var(--transition); font-family:inherit;
}
.exp-format-tab:hover { border-color:var(--gold); color:var(--text); }
.exp-format-tab--active { border-color:var(--gold); background:rgba(212,175,55,.1); color:var(--text); }
.exp-format-tag { font-size:.65rem; background:var(--gold); color:#0d1117; border-radius:4px; padding:1px 5px; margin-left:.15rem; font-weight:700; }
.exp-correo-row { display:flex; align-items:center; gap:.4rem; margin-bottom:.35rem; }
.exp-correo-row .met-input { flex:1; margin-bottom:0; }
.exp-correo-remove { display:flex; align-items:center; justify-content:center; width:28px; height:28px; border-radius:6px; background:rgba(248,81,73,.1); border:1px solid rgba(248,81,73,.3); color:#f85149; cursor:pointer; transition:var(--transition); }
.exp-correo-remove:hover { background:#f85149; color:white; }
.exp-correo-add { display:flex; align-items:center; gap:.35rem; margin-top:.1rem; padding:.35rem .6rem; background:var(--bg2); border:1px dashed var(--border2); border-radius:6px; font-size:.78rem; color:var(--text-muted); cursor:pointer; font-family:inherit; transition:var(--transition); }
.exp-correo-add:hover { border-color:var(--gold); color:var(--gold); }
.exp-actions { display:flex; gap:.75rem; justify-content:flex-end; padding-top:.75rem; border-top:1px solid var(--border); }

/* Botones genéricos usados en el modal */
.btn-primary { padding:.6rem 1.25rem; background:var(--accent); color:white; border:none; border-radius:8px; font-size:.875rem; font-weight:600; cursor:pointer; transition:var(--transition); font-family:inherit; }
.btn-primary:hover:not(:disabled) { filter:brightness(1.1); transform:translateY(-1px); }
.btn-primary:disabled { opacity:.4; cursor:not-allowed; }
.btn-secondary { padding:.6rem 1.25rem; background:var(--bg2); color:var(--text-muted); border:1px solid var(--border2); border-radius:8px; font-size:.875rem; font-weight:500; cursor:pointer; transition:var(--transition); font-family:inherit; }
.btn-secondary:hover { background:var(--bg3); color:var(--text); }

/* ── Responsive ─────────────────────────────────────────────────────── */
@media (max-width:900px) {
  .neg-funnel-row { grid-template-columns:140px 1fr 100px; }
  .ruta-tabla-header, .ruta-tabla-row { grid-template-columns:30px 1fr 90px; }
  .ruta-tabla-header span:nth-child(3), .ruta-tabla-row span:nth-child(4) { display:none; }
  .neg-ingresos-combo { grid-template-columns:1fr; }
  .tend-wrap { grid-template-columns:1fr; }
}
@media (max-width:640px) {
  .neg-funnel-row { grid-template-columns:1fr; }
  .neg-funnel-label { font-size:.78rem; }
  .met-ingresos-banner { flex-direction:column; gap:1rem; }
  .met-ing-main { border-right:none; border-bottom:1px solid var(--border); padding-right:0; padding-bottom:1rem; }
  .exp-checks { grid-template-columns:1fr; }
  .ruta-tabla-header, .ruta-tabla-row { grid-template-columns:30px 1fr; }
}
</style>

<script>
/**
 * @file AdminReportes.svelte
 * @description Modulo de metricas del panel de administracion. Tres paneles:
 * 1. Reservaciones y Canal — KPI de ingresos, reservaciones por dia, canal Directo/Agencia.
 * 2. Analisis de Negocio — embudo, top hoteles, cancelaciones, tendencia, heatmap.
 * 3. Registro de Busquedas — listado paginado con filtros y exportacion.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /** Funcion badge (compatibilidad con Administrador.svelte). @type {Function} */
  export let badge;

  // ─── Estado general ───────────────────────────────────────────────────────
  let metricasResumen = null;
  let negocioData     = null;
  let metricasListado = null;

  let loadingMetricas = false;
  let loadingNegocio  = false;
  let loadingListado  = false;

  // ─── Filtros ──────────────────────────────────────────────────────────────
  let metFechaDesde = (() => {
    const d = new Date(); d.setDate(d.getDate() - 29);
    return d.toISOString().split('T')[0];
  })();
  let metFechaHasta = new Date().toISOString().split('T')[0];
  let metTipo       = '';
  let metUsuario    = '';
  let metPagina     = 1;
  let metPeriodo    = '30d';

  // ─── Paneles ──────────────────────────────────────────────────────────────
  let panelActivo = 'reservaciones';

  // ─── Modal exportar ───────────────────────────────────────────────────────
  let mostrarModalExportar = false;
  let expFormato           = 'excel';
  let correosExportar      = [''];
  let expFormatoEmail      = 'excel';
  let enviandoExportar     = false;
  let expSec = {
    kpi: true, reservacionesDiarias: true, canal: true,
    embudo: true, hoteles: true, cancelaciones: true,
    tendencia: true, heatmap: true, registro: true
  };

  // ─── Lifecycle ────────────────────────────────────────────────────────────
  onMount(() => {
    cargarMetricas();
    cargarNegocio();
    cargarListadoBusquedas(1);
  });

  // ─── Funciones de carga ───────────────────────────────────────────────────
  async function cargarMetricas() {
    loadingMetricas = true;
    try {
      const params = new URLSearchParams({ fechaDesde: metFechaDesde, fechaHasta: metFechaHasta });
      const r = await fetch(`${API_BASE}/admin/metricas/resumen?${params}`, { credentials: 'include' });
      if (r.ok) metricasResumen = await r.json();
    } catch (_) {}
    finally { loadingMetricas = false; }
  }

  async function cargarNegocio() {
    loadingNegocio = true;
    try {
      const params = new URLSearchParams({ fechaDesde: metFechaDesde, fechaHasta: metFechaHasta });
      const r = await fetch(`${API_BASE}/admin/metricas/negocio?${params}`, { credentials: 'include' });
      if (r.ok) negocioData = await r.json();
    } catch (_) {}
    finally { loadingNegocio = false; }
  }

  async function cargarListadoBusquedas(pagina = 1) {
    loadingListado = true;
    metPagina = pagina;
    try {
      const r = await fetch(`${API_BASE}/admin/metricas/listado`, {
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
    } catch (_) {}
    finally { loadingListado = false; }
  }

  function setPeriodo(periodo) {
    metPeriodo = periodo;
    const hoy = new Date();
    const fmt = d => d.toISOString().split('T')[0];
    metFechaHasta = fmt(hoy);
    if (periodo === '7d') {
      const d = new Date(hoy); d.setDate(d.getDate() - 6); metFechaDesde = fmt(d);
    } else if (periodo === '30d') {
      const d = new Date(hoy); d.setDate(d.getDate() - 29); metFechaDesde = fmt(d);
    } else if (periodo === 'mes') {
      metFechaDesde = fmt(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
    } else if (periodo === 'anio') {
      metFechaDesde = fmt(new Date(hoy.getFullYear(), 0, 1));
    } else if (periodo === 'todo') {
      metFechaDesde = '2000-01-01';
    }
  }

  function aplicarFiltros() {
    cargarMetricas();
    cargarNegocio();
    cargarListadoBusquedas(1);
  }

  function cambiarPagina(pagina) { cargarListadoBusquedas(pagina); }

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
        if (!validos.length) return;
        const r = await fetch(`${API_BASE}/admin/metricas/exportar-correo`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...payload, correos: validos, formato: expFormatoEmail })
        });
        if (r.ok) { mostrarModalExportar = false; correosExportar = ['']; }
      } else {
        const ext  = expFormato === 'excel' ? 'xlsx' : 'zip';
        const mime = expFormato === 'excel'
          ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          : 'application/zip';
        const r = await fetch(`${API_BASE}/admin/metricas/exportar-archivo`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...payload, formato: expFormato })
        });
        if (r.ok) {
          const blob = await r.blob();
          const url  = URL.createObjectURL(new Blob([blob], { type: mime }));
          const a    = document.createElement('a');
          a.href = url; a.download = `metricas_miku_${metFechaDesde}_${metFechaHasta}.${ext}`;
          document.body.appendChild(a); a.click();
          document.body.removeChild(a); URL.revokeObjectURL(url);
          mostrarModalExportar = false;
        }
      }
    } catch (_) {}
    finally { enviandoExportar = false; }
  }

  async function exportarPdf() {
    try {
      const params = {
        fechaDesde: metFechaDesde,
        fechaHasta: metFechaHasta,
        tipo:       metTipo    || null,
        usuario:    metUsuario || null,
        secciones:  expSec
      };
      const r = await fetch(`${API_BASE}/admin/metricas/exportar-pdf`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
      });
      if (r.ok) {
        const blob = await r.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `metricas_miku_${metFechaDesde}_${metFechaHasta}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      }
    } catch (_) {}
  }
</script>

<!-- Seccion de metricas con KPIs, graficas SVG, listado y exportacion -->
<section class="admin-section met-section">
  <div class="met-header">
    <div>
      <h2 class="admin-section__title">Métricas y Estadísticas</h2>
      <p class="admin-section__subtitle">Reservaciones, ingresos y conversion del sistema</p>
    </div>
    <button class="met-btn-exportar" on:click={() => mostrarModalExportar = true}>
      <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor" style="flex-shrink:0">
        <path fill-rule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clip-rule="evenodd"/>
      </svg>
      Exportar
    </button>
  </div>

  <!-- Filtros -->
  <div class="met-filtros-panel">
    <div class="met-filtro-bloque met-filtro-bloque--periodos">
      <span class="met-label">Período</span>
      <div class="met-periodo-btns">
        {#each [
          { id:'7d',   label:'7 días'   },
          { id:'30d',  label:'30 días'  },
          { id:'mes',  label:'Este mes' },
          { id:'anio', label:'Este año' },
          { id:'todo', label:'Todo'     },
        ] as p}
          <button class="met-periodo-btn" class:met-periodo-btn--active={metPeriodo === p.id}
            on:click={() => setPeriodo(p.id)}>{p.label}</button>
        {/each}
      </div>
    </div>

    <div class="met-filtros-row">
      <div class="met-filtro-grupo">
        <label for="mf-desde" class="met-label">Desde</label>
        <input id="mf-desde" type="date" class="met-input"
          bind:value={metFechaDesde} on:change={() => metPeriodo = 'custom'} />
      </div>
      <div class="met-filtro-grupo">
        <label for="mf-hasta" class="met-label">Hasta</label>
        <input id="mf-hasta" type="date" class="met-input"
          bind:value={metFechaHasta} on:change={() => metPeriodo = 'custom'} />
      </div>
      <div class="met-filtro-grupo">
        <label for="mf-canal" class="met-label">Canal</label>
        <select id="mf-canal" class="met-input" bind:value={metTipo}>
          <option value="">Todos los canales</option>
          <option value="web">Web (directo)</option>
          <option value="rest">REST (agencias)</option>
        </select>
      </div>
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

  <!-- Selector de panel -->
  <div class="met-panel-selector">
    <button class="met-panel-btn" class:met-panel-btn--active={panelActivo === 'reservaciones'}
      on:click={() => panelActivo = 'reservaciones'}>
      <svg width="15" height="15" viewBox="0 0 20 20" fill="currentColor">
        <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zm6-4a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zm6-3a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z"/>
      </svg>
      Reservaciones y Canal
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

  <!-- ═══ PANEL 1: RESERVACIONES Y CANAL ═══ -->
  {#if panelActivo === 'reservaciones'}

  {#if loadingMetricas}
    <div class="met-loading"><div class="met-spinner"></div><p>Cargando métricas...</p></div>
  {:else if metricasResumen}

    <!-- Banner KPI de ingresos -->
    {#each [metricasResumen.ingresosKpi ?? {ingresosTotales:0,ingresosDirecto:0,ingresosAgencia:0,reservacionesPagadas:0,totalReservaciones:0,ticketPromedio:0}] as kpi}
      <div class="met-ingresos-banner">
        <div class="met-ing-main">
          <span class="met-ing-label">Ingresos Totales</span>
          <span class="met-ing-valor">
            Q {(kpi.ingresosTotales ?? 0).toLocaleString('es-GT', {minimumFractionDigits:2, maximumFractionDigits:2})}
          </span>
          <span class="met-ing-sub">{kpi.reservacionesPagadas} pagadas · {kpi.totalReservaciones} reservaciones totales</span>
        </div>
        <div class="met-ing-stats">
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--turista"></span>
            <div>
              <span class="met-ing-stat__label">Directo</span>
              <span class="met-ing-stat__val">Q {(kpi.ingresosDirecto ?? 0).toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--ejecutivo"></span>
            <div>
              <span class="met-ing-stat__label">Agencia</span>
              <span class="met-ing-stat__val">Q {(kpi.ingresosAgencia ?? 0).toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--ticket"></span>
            <div>
              <span class="met-ing-stat__label">Ticket promedio</span>
              <span class="met-ing-stat__val">Q {(kpi.ticketPromedio ?? 0).toLocaleString('es-GT',{minimumFractionDigits:2})}</span>
            </div>
          </div>
          <div class="met-ing-stat">
            <span class="met-ing-stat__dot met-ing-stat__dot--busq"></span>
            <div>
              <span class="met-ing-stat__label">Res. pagadas</span>
              <span class="met-ing-stat__val">{(kpi.reservacionesPagadas ?? 0).toLocaleString()}</span>
            </div>
          </div>
        </div>
      </div>
    {/each}

    <!-- Reservaciones por dia -->
    <div class="met-grafica met-grafica--wide">
      <h3 class="met-grafica__titulo">Reservaciones diarias</h3>
      <p class="met-grafica__subtitulo">Cuántas reservaciones se crearon cada día en el período seleccionado.</p>
      {#if !metricasResumen.reservacionesPorDia || metricasResumen.reservacionesPorDia.length === 0}
        <div class="met-empty">Sin datos en el periodo seleccionado</div>
      {:else}
        {@const datos  = metricasResumen.reservacionesPorDia}
        {@const maxVal = Math.max(...datos.map(d => d.total), 1)}
        {@const tot    = datos.reduce((s,d)=>s+d.total,0)}
        {@const W=600} {@const H=180}
        {@const PAD={l:36,r:16,t:16,b:28}}
        {@const gW=W-PAD.l-PAD.r} {@const gH=H-PAD.t-PAD.b}
        <div class="met-svg-wrap">
          <svg viewBox="0 0 {W} {H}" class="met-svg" preserveAspectRatio="xMidYMid meet">
            <defs>
              <linearGradient id="gradRes" x1="0" y1="0" x2="0" y2="1">
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
              <polygon points={`${PAD.l},${PAD.t+gH} ${pts.join(' ')} ${W-PAD.r},${PAD.t+gH}`} fill="url(#gradRes)"/>
              <polyline points={pts.join(' ')} fill="none" stroke="#D4AF37" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>
              {#each datos as d,i}
                {@const x=PAD.l+(i/(datos.length-1))*gW}
                {@const y=PAD.t+gH-(d.total/maxVal)*gH}
                <circle cx={x} cy={y} r="3.5" fill="#D4AF37" stroke="white" stroke-width="1.5">
                  <title>{d.fecha}: {d.total} reservaciones</title>
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
          <span><span class="met-leyenda-dot" style="background:#D4AF37"></span> Reservaciones por día</span>
          <span style="margin-left:auto;color:var(--text-muted);font-size:.78rem">Total período: <strong>{tot.toLocaleString()}</strong> reservaciones</span>
        </div>
      {/if}
    </div>

    <!-- Canal split — RESERVACIONES (solo usuarios con cuenta y agencias) -->
    {#if metricasResumen.canalSplit && metricasResumen.canalSplit.length > 0}
      {@const totalCanal = metricasResumen.canalSplit.reduce((s,c) => s + c.total, 0) || 1}
      {@const canalColores = { 'Directo':'#D4AF37', 'Agencia':'#667eea' }}
      {@const canalDesc    = { 'Directo':'Usuario con cuenta registrada', 'Agencia':'Agencia de viaje externa integrada' }}
      <div class="met-canal-wrap">
        <h3 class="met-canal__titulo">Reservaciones por canal</h3>
        <p class="met-canal__subtitulo">
          Distribución de las <strong style="color:var(--text)">{totalCanal.toLocaleString()}</strong> reservaciones confirmadas según quién las originó.
          Solo usuarios con cuenta pueden reservar.
        </p>
        <div class="met-canal-pills">
          {#each metricasResumen.canalSplit as canal}
            {@const pct   = ((canal.total/totalCanal)*100).toFixed(1)}
            {@const color = canalColores[canal.canal] ?? '#6b7280'}
            {@const desc  = canalDesc[canal.canal]   ?? canal.canal}
            <div class="met-canal-pill" style="background:var(--bg2);border:1px solid var(--border);border-radius:10px;padding:.9rem 1.1rem;gap:.5rem">
              <div style="display:flex;align-items:center;gap:.5rem;margin-bottom:.3rem">
                <span style="width:10px;height:10px;border-radius:50%;background:{color};flex-shrink:0"></span>
                <span class="met-canal-tipo" style="color:{color};font-size:.82rem">{canal.canal}</span>
                <span style="font-size:.7rem;color:var(--text-muted);margin-left:auto">{desc}</span>
              </div>
              <div class="met-canal-bar" style="--pct:{pct}%;--color:{color};height:9px;border-radius:5px;margin-bottom:.35rem"></div>
              <div style="display:flex;align-items:center;justify-content:space-between">
                <span class="met-canal-num" style="font-size:.82rem;font-weight:700;color:var(--text)">{canal.total.toLocaleString()} reservaciones</span>
                <span style="font-size:.85rem;font-weight:800;color:{color}">{pct}%</span>
              </div>
            </div>
          {/each}
        </div>
      </div>
    {/if}

    <!-- Búsquedas por canal — incluye anónimos -->
    {#if metricasResumen.busquedasPorCanal && metricasResumen.busquedasPorCanal.length > 0}
      {@const totalBusq = metricasResumen.busquedasPorCanal.reduce((s,c) => s + c.total, 0) || 1}
      {@const busqColores = { 'Registrado':'#D4AF37', 'Agencia':'#667eea', 'No registrado':'#6b7280' }}
      {@const busqDesc    = {
        'Registrado':    'Usuario con sesión activa',
        'Agencia':       'Petición desde agencia externa (API)',
        'No registrado': 'Visitante sin cuenta (anónimo)'
      }}
      <div class="met-canal-wrap" style="border-top:1px solid var(--border);padding-top:1.25rem">
        <h3 class="met-canal__titulo">Búsquedas por canal <span style="font-size:.75rem;font-weight:400;color:var(--text-muted)">— incluye anónimos</span></h3>
        <p class="met-canal__subtitulo">
          De las <strong style="color:var(--text)">{totalBusq.toLocaleString()}</strong> búsquedas registradas, cuántas vinieron de cada tipo de visitante.
          Los no registrados buscan pero no pueden reservar.
        </p>
        <div class="met-canal-pills">
          {#each metricasResumen.busquedasPorCanal as canal}
            {@const pct   = ((canal.total/totalBusq)*100).toFixed(1)}
            {@const color = busqColores[canal.canal] ?? '#6b7280'}
            {@const desc  = busqDesc[canal.canal]    ?? canal.canal}
            <div class="met-canal-pill" style="background:var(--bg2);border:1px solid var(--border);border-radius:10px;padding:.9rem 1.1rem;gap:.5rem">
              <div style="display:flex;align-items:center;gap:.5rem;margin-bottom:.3rem">
                <span style="width:10px;height:10px;border-radius:50%;background:{color};flex-shrink:0"></span>
                <span class="met-canal-tipo" style="color:{color};font-size:.82rem">{canal.canal}</span>
                <span style="font-size:.7rem;color:var(--text-muted);margin-left:auto">{desc}</span>
              </div>
              <div class="met-canal-bar" style="--pct:{pct}%;--color:{color};height:9px;border-radius:5px;margin-bottom:.35rem"></div>
              <div style="display:flex;align-items:center;justify-content:space-between">
                <span class="met-canal-num" style="font-size:.82rem;font-weight:700;color:var(--text)">{canal.total.toLocaleString()} búsquedas</span>
                <span style="font-size:.85rem;font-weight:800;color:{color}">{pct}%</span>
              </div>
            </div>
          {/each}
        </div>
      </div>
    {/if}

  {/if}<!-- fin reservaciones loading -->
  {/if}<!-- fin panel reservaciones -->

  <!-- ═══ PANEL 2: ANÁLISIS DE NEGOCIO ═══ -->
  {#if panelActivo === 'negocio'}

    {#if loadingNegocio}
      <div class="met-loading"><div class="met-spinner"></div><p>Cargando análisis de negocio...</p></div>
    {:else if negocioData}

      <!-- ── 1. Embudo de conversión ──────────────────────────────────────── -->
      {@const emb = negocioData.embudo ?? {completadas:0,pagadas:0,pendientes:0,expiradas:0,canceladas:0}}
      {@const embBase = Math.max(emb.completadas + emb.pagadas + emb.pendientes + emb.expiradas + emb.canceladas, 1)}
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Embudo de conversión: Estado de Reservaciones</h3>
        <p class="met-grafica__subtitulo">Distribución por estado final de las reservaciones en el período.</p>
        <div class="neg-funnel">
          {#each [
            { label: 'Completadas (check-out realizado)', val: emb.completadas, color: '#8b5cf6' },
            { label: 'Pagadas (confirmadas)',             val: emb.pagadas,     color: '#10b981' },
            { label: 'Pendientes de pago',               val: emb.pendientes,  color: '#60a5fa' },
            { label: 'Expiradas sin pagar',              val: emb.expiradas,   color: '#f59e0b' },
            { label: 'Canceladas',                       val: emb.canceladas,  color: '#ef4444' },
          ] as paso}
            {@const pct = embBase > 0 ? (paso.val / embBase * 100) : 0}
            <div class="neg-funnel-row">
              <div class="neg-funnel-label">{paso.label}</div>
              <div class="neg-funnel-track">
                <div class="neg-funnel-bar" style="width:{Math.min(pct,100)}%;background:{paso.color}"></div>
              </div>
              <div class="neg-funnel-vals">
                <span class="neg-funnel-num">{paso.val.toLocaleString()}</span>
                <span class="neg-funnel-pct" style="color:{paso.color}">({pct.toFixed(2)}%)</span>
              </div>
            </div>
          {/each}
        </div>
        <div class="neg-funnel-kpis">
          {#each [
            { label:'Completadas', val: emb.completadas, color:'#8b5cf6' },
            { label:'Pagadas',     val: emb.pagadas,     color:'#10b981' },
            { label:'Pendientes',  val: emb.pendientes,  color:'#60a5fa' },
            { label:'Expiradas',   val: emb.expiradas,   color:'#f59e0b' },
            { label:'Canceladas',  val: emb.canceladas,  color:'#ef4444' },
          ] as k}
            {@const pct = embBase > 0 ? (k.val / embBase * 100).toFixed(2) : '0.00'}
            <div class="neg-kpi">
              <span class="neg-kpi-val" style="color:{k.color}">{pct}%</span>
              <span class="neg-kpi-num">{k.val.toLocaleString()}</span>
              <span class="neg-kpi-label">{k.label}</span>
            </div>
          {/each}
          <!-- Separador + total 100% -->
          <div style="display:flex;flex-direction:column;gap:.1rem;margin-left:auto;padding-left:1.5rem;border-left:1px solid var(--border2)">
            <span class="neg-kpi-val" style="color:var(--text);font-size:1.5rem">100%</span>
            <span class="neg-kpi-num" style="font-weight:800">{embBase.toLocaleString()}</span>
            <span class="neg-kpi-label">Total</span>
          </div>
        </div>
        <!-- Barra total combinada -->
        <div style="margin-top:.85rem;padding-top:.75rem;border-top:1px solid var(--border)">
          <div style="display:flex;align-items:center;gap:.75rem">
            <span style="font-size:.75rem;color:var(--text-muted);min-width:72px;font-weight:700;text-transform:uppercase;letter-spacing:.04em">Total</span>
            <div style="flex:1;height:11px;background:rgba(255,255,255,.05);border-radius:6px;overflow:hidden;display:flex">
              {#each [
                { val: emb.completadas, color:'#8b5cf6' },
                { val: emb.pagadas,     color:'#10b981' },
                { val: emb.pendientes,  color:'#60a5fa' },
                { val: emb.expiradas,   color:'#f59e0b' },
                { val: emb.canceladas,  color:'#ef4444' },
              ] as s}
                {@const pct = embBase > 0 ? (s.val / embBase * 100) : 0}
                {#if pct > 0}
                  <div style="width:{pct}%;background:{s.color};height:100%"></div>
                {/if}
              {/each}
            </div>
            <span style="font-size:.8rem;font-weight:800;color:var(--text);min-width:36px;text-align:right">100%</span>
          </div>
        </div>
      </div>

      <!-- ── 2. Top hoteles ───────────────────────────────────────────────── -->
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Rendimiento de hoteles</h3>
        <p class="met-grafica__subtitulo">Cuántas reservaciones recibió cada hotel, cuánto generó en ingresos confirmados.</p>
        {#if !negocioData.topHoteles || negocioData.topHoteles.length === 0}
          <div class="met-empty">Sin datos en el período seleccionado</div>
        {:else}
          {@const totalIng  = negocioData.topHoteles.reduce((s,h) => s + (h.ingresosTotales ?? 0), 0)}
          {@const totalRes  = negocioData.topHoteles.reduce((s,h) => s + (h.totalReservaciones ?? 0), 0)}
          <div class="ruta-total-banner">
            <span class="ruta-total-label">Total generado en el período</span>
            <span class="ruta-total-val">Q {totalIng.toLocaleString('es-GT',{minimumFractionDigits:2,maximumFractionDigits:2})}</span>
          </div>
          <div class="ruta-tabla">
            <div class="ruta-tabla-header">
              <span>#</span>
              <span>Hotel</span>
              <span class="ruta-col-center">Reservaciones</span>
              <span>% del total · Ingresos</span>
            </div>
            {#each negocioData.topHoteles as hotel, i}
              {@const tieneIngresos = (hotel.ingresosTotales ?? 0) > 0}
              {@const pctTotal = totalIng > 0 ? ((hotel.ingresosTotales ?? 0) / totalIng * 100) : 0}
              <div class="ruta-tabla-row" class:ruta-tabla-row--sin-ing={!tieneIngresos}>
                <span class="ruta-rank">#{i+1}</span>
                <span class="ruta-nombre" style="font-weight:600">{hotel.hotel}</span>
                <span class="ruta-col-center ruta-stat">{hotel.totalReservaciones}</span>
                <span class="ruta-ing-wrap">
                  {#if tieneIngresos}
                    <span class="ruta-ing-pct-label">{pctTotal.toFixed(1)}%</span>
                    <span class="ruta-ing-bar-bg">
                      <span class="ruta-ing-bar" style="width:{pctTotal}%"></span>
                    </span>
                    <span class="ruta-ing-val">Q {(hotel.ingresosTotales ?? 0).toLocaleString('es-GT',{maximumFractionDigits:0})}</span>
                  {:else}
                    <span class="ruta-ing-pct-label" style="color:var(--text-muted)">0%</span>
                    <span class="ruta-ing-bar-bg"><span class="ruta-ing-bar" style="width:0%"></span></span>
                    <span class="ruta-ing-val" style="color:#b8b0a5">Q 0</span>
                  {/if}
                </span>
              </div>
            {/each}
            <!-- Fila total -->
            <div class="ruta-tabla-row" style="background:rgba(212,175,55,.06);border-top:1.5px solid rgba(212,175,55,.22)">
              <span></span>
              <span style="font-size:.8rem;font-weight:800;color:var(--gold);text-transform:uppercase;letter-spacing:.05em">Total</span>
              <span class="ruta-col-center ruta-stat" style="font-weight:800;color:var(--text)">{totalRes.toLocaleString()}</span>
              <span class="ruta-ing-wrap">
                <span class="ruta-ing-pct-label" style="color:var(--gold);font-size:.8rem">100%</span>
                <span class="ruta-ing-bar-bg">
                  <span class="ruta-ing-bar" style="width:100%;background:linear-gradient(to right,var(--gold),#edd97a)"></span>
                </span>
                <span class="ruta-ing-val" style="color:var(--gold)">Q {totalIng.toLocaleString('es-GT',{maximumFractionDigits:0})}</span>
              </span>
            </div>
          </div>
        {/if}
      </div>

      <!-- ── 3. Cancelaciones por tipo ───────────────────────────────────── -->
      <div class="neg-card">
        <h3 class="met-grafica__titulo" style="padding:0 0 .25rem">Análisis de cancelaciones</h3>
        <p class="met-grafica__subtitulo" style="padding:0 0 .75rem">
          Quién originó la cancelación: usuario final, agencia externa o administrador del sistema.
        </p>
        {#if !negocioData.cancelaciones || negocioData.cancelaciones.length === 0}
          <div class="met-empty">Sin cancelaciones en el período</div>
        {:else}
          {@const totalCancTipo = negocioData.cancelaciones.reduce((s,c)=>s+c.total,0) || 1}
          {@const metaCancelTipo = {
            'Administrador': { color:'#667eea', icon:'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z', desc:'Sistema / acción del admin' },
            'Agencia':        { color:'#D4AF37', icon:'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-2 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4', desc:'Cancelada por agencia externa' },
            'Usuario':        { color:'#10b981', icon:'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z', desc:'Cancelada por el propio usuario' },
          }}
          <!-- Total de cancelaciones -->
          <div style="display:flex;align-items:center;gap:.6rem;margin-bottom:1rem;padding:.6rem .9rem;background:rgba(239,68,68,.06);border:1px solid rgba(239,68,68,.18);border-radius:8px">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <span style="font-size:.8rem;color:#ef4444;font-weight:700">{totalCancTipo.toLocaleString()} cancelaciones totales en el período</span>
          </div>
          <!-- Tarjetas por tipo -->
          <div style="display:flex;flex-direction:column;gap:.65rem">
            {#each negocioData.cancelaciones as c}
              {@const meta  = metaCancelTipo[c.tipo] ?? { color:'#6b7280', icon:'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z', desc:c.tipo }}
              {@const pct   = (c.total / totalCancTipo * 100)}
              <div style="display:flex;align-items:center;gap:1rem;padding:.85rem 1.1rem;background:var(--bg2);border:1px solid var(--border);border-left:3px solid {meta.color};border-radius:0 10px 10px 0;transition:background .15s">
                <!-- Icono -->
                <div style="width:40px;height:40px;border-radius:10px;background:{meta.color}1a;border:1px solid {meta.color}33;display:flex;align-items:center;justify-content:center;flex-shrink:0">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="{meta.color}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="{meta.icon}"/>
                  </svg>
                </div>
                <!-- Info -->
                <div style="flex:1;min-width:0">
                  <div style="display:flex;align-items:center;gap:.5rem;margin-bottom:.3rem">
                    <span style="font-size:.88rem;font-weight:700;color:var(--text)">{c.tipo}</span>
                    <span style="font-size:.7rem;color:var(--text-muted)">{meta.desc}</span>
                  </div>
                  <div style="height:8px;background:rgba(255,255,255,.05);border-radius:4px;overflow:hidden">
                    <div style="width:{pct}%;height:100%;background:{meta.color};border-radius:4px;transition:width .4s ease"></div>
                  </div>
                </div>
                <!-- Cifras -->
                <div style="text-align:right;flex-shrink:0">
                  <div style="font-size:1.1rem;font-weight:800;color:{meta.color}">{pct.toFixed(1)}%</div>
                  <div style="font-size:.78rem;color:var(--text-muted)">{c.total.toLocaleString()} canc.</div>
                </div>
              </div>
            {/each}
          </div>
        {/if}
      </div>

      <!-- ── 4. Ingresos: distribución + tendencia ────────────────────────── -->
      <div class="met-grafica met-grafica--wide neg-card">
        <h3 class="met-grafica__titulo">Ingresos por canal — Distribución y Tendencia</h3>
        <p class="met-grafica__subtitulo">Distribución entre Directo y Agencia, y evolución mes a mes.</p>
        <div class="neg-ingresos-combo">

          <!-- Donut canal split -->
          <div class="neg-combo-donut">
            {#if !metricasResumen?.canalSplit || metricasResumen.canalSplit.length === 0}
              <div class="met-empty">Sin reservaciones en el período</div>
            {:else}
              {@const donutData = metricasResumen.canalSplit}
              {@const totalDonut = donutData.reduce((s,c)=>s+c.total,0)||1}
              {@const coloresDonut = ['#D4AF37','#1C1A18']}
              {@const segs = (() => {
                let ang = -90;
                return donutData.map((c,i) => {
                  const n = c.total; const pct = n/totalDonut;
                  const start = ang; ang += pct*360;
                  return { canal:c.canal, total:n,
                    start, angle:pct*360, color:coloresDonut[i%2],
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
                      <title>{seg.canal}: {seg.total} ({seg.pct}%)</title>
                    </path>
                  {/if}
                {/each}
                <circle cx="80" cy="80" r="34" fill="white"/>
                <text x="80" y="77" text-anchor="middle" font-size="10" font-weight="700" fill="#1C1A18">
                  {totalDonut}
                </text>
                <text x="80" y="90" text-anchor="middle" font-size="8" fill="#b8b0a5">RESERV.</text>
              </svg>
              <div style="display:flex;flex-direction:column;gap:.6rem;margin-top:.25rem">
                {#each segs as seg}
                  <div style="display:flex;flex-direction:column;gap:.1rem">
                    <div style="display:flex;align-items:center;gap:.4rem">
                      <span class="met-leyenda-dot" style="background:{seg.color}"></span>
                      <span style="font-size:.82rem;font-weight:700;color:#1C1A18">{seg.canal}</span>
                      <span style="font-size:.78rem;color:var(--text-muted);margin-left:auto">{seg.pct}%</span>
                    </div>
                    <span style="font-size:.8rem;color:#374151;padding-left:1.1rem">{seg.total} reservaciones</span>
                  </div>
                {/each}
              </div>
            {/if}
          </div>

          <!-- Tendencia mensual -->
          <div class="neg-combo-tendencia">
            {#if !negocioData.ingresosTendencia || negocioData.ingresosTendencia.length === 0}
              <div class="met-empty">Sin ingresos en el período</div>
            {:else}
              {@const meses  = [...new Set(negocioData.ingresosTendencia.map(d=>d.mes))].sort()}
              {@const canales = [...new Set(negocioData.ingresosTendencia.map(d=>d.canal))]}
              {@const byKey  = Object.fromEntries(negocioData.ingresosTendencia.map(d=>[`${d.mes}|${d.canal}`,d.revenue]))}
              {@const maxR   = Math.max(...meses.flatMap(m=>canales.map(cl=>byKey[`${m}|${cl}`]??0)),1)}
              {@const mesFmt = m => `${m.slice(5)}/${m.slice(2,4)}`}
              {@const ultimoMes = meses[meses.length-1]}
              {@const W=420}{@const H=200}
              {@const PAD={l:52,r:16,t:16,b:32}}
              {@const gW=W-PAD.l-PAD.r}{@const gH=H-PAD.t-PAD.b}
              <div class="tend-wrap">
                <div class="tend-chart">
                  <svg viewBox="0 0 {W} {H}" style="width:100%;max-height:200px" preserveAspectRatio="xMidYMid meet">
                    {#each [0,0.25,0.5,0.75,1] as pct}
                      {@const y=PAD.t+gH-pct*gH}
                      <line x1={PAD.l} y1={y} x2={W-PAD.r} y2={y} stroke="#EBE6E0" stroke-width="1"/>
                      <text x={PAD.l-4} y={y+4} font-size="8" fill="#b8b0a5" text-anchor="end">Q{(maxR*pct/1000).toFixed(0)}k</text>
                    {/each}
                    {#each canales as cl, ci}
                      {@const color = ci===0?'#D4AF37':'#1C1A18'}
                      {@const colorPale = ci===0?'#EDD97A':'#9ca3af'}
                      {#if meses.length > 1}
                        {@const ptsPrev=meses.map((m,i)=>{
                          const x=PAD.l+(i/(meses.length-1))*gW;
                          const prevVal=i>0?(byKey[`${meses[i-1]}|${cl}`]??0):0;
                          const y=PAD.t+gH-(prevVal/maxR)*gH;
                          return `${x},${y}`;
                        })}
                        <polyline points={ptsPrev.join(' ')} fill="none" stroke={colorPale} stroke-width="1.5"
                          stroke-linejoin="round" stroke-linecap="round"
                          stroke-dasharray="4,4" opacity="0.55"/>
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
                          <circle cx={x} cy={y} r="3" fill={color} stroke="white" stroke-width="1.5">
                            <title>{cl} {mesFmt(m)}: Q{rev.toLocaleString('es-GT',{minimumFractionDigits:2})}</title>
                          </circle>
                        {/each}
                      {:else if meses.length===1}
                        {@const rev=byKey[`${meses[0]}|${cl}`]??0}
                        {@const x=PAD.l+gW/2}
                        {@const y=PAD.t+gH-(rev/maxR)*gH}
                        {@const yBase=PAD.t+gH}
                        <line x1={x} y1={yBase} x2={x} y2={y} stroke={colorPale} stroke-width="1.5" stroke-dasharray="4,4" opacity="0.55"/>
                        <circle cx={x} cy={y} r="5" fill={color} stroke="white" stroke-width="1.5">
                          <title>{cl}: Q{rev.toLocaleString('es-GT',{minimumFractionDigits:2})}</title>
                        </circle>
                      {/if}
                    {/each}
                    {#each meses as m, i}
                      {@const x=meses.length>1?PAD.l+(i/(meses.length-1))*gW:PAD.l+gW/2}
                      <text x={x} y={H-4} font-size="7" fill="#b8b0a5" text-anchor="middle">{mesFmt(m)}</text>
                    {/each}
                  </svg>
                  <div class="met-grafica__leyenda" style="margin-top:.5rem">
                    {#each canales as cl, ci}
                      <span>
                        <span class="met-leyenda-dot" style="background:{ci===0?'#D4AF37':'#1C1A18'}"></span>
                        {cl}
                      </span>
                    {/each}
                    <span style="color:#b8b0a5;font-size:10px;margin-left:.5rem">· · · mes anterior</span>
                  </div>
                </div>
                <div class="tend-resumen" style="min-width:160px">
                  <!-- Mes anterior -->
                  {#if meses.length >= 2}
                    {@const penultimoMes = meses[meses.length-2]}
                    <div class="tend-resumen__bloque">
                      <p class="tend-resumen__titulo" style="color:#6b7280">Mes anterior <span class="tend-resumen__mes">({mesFmt(penultimoMes)})</span></p>
                      {#each canales as cl, ci}
                        {@const val = byKey[`${penultimoMes}|${cl}`] ?? 0}
                        <div class="tend-resumen__fila">
                          <span class="tend-resumen__dot" style="background:{ci===0?'#D4AF37':'#667eea'};opacity:.55"></span>
                          <span class="tend-resumen__clase">{cl}</span>
                          <span class="tend-resumen__val" style="color:#8b949e">Q{val.toLocaleString('es-GT',{minimumFractionDigits:0,maximumFractionDigits:0})}</span>
                        </div>
                      {/each}
                    </div>
                    <div class="tend-resumen__sep"></div>
                  {/if}
                  <!-- Mes actual + delta -->
                  <div class="tend-resumen__bloque">
                    <p class="tend-resumen__titulo">Mes actual <span class="tend-resumen__mes">({mesFmt(ultimoMes)})</span></p>
                    {#each canales as cl, ci}
                      {@const val    = byKey[`${ultimoMes}|${cl}`] ?? 0}
                      {@const prev   = meses.length >= 2 ? (byKey[`${meses[meses.length-2]}|${cl}`] ?? 0) : 0}
                      {@const delta  = prev > 0 ? ((val - prev) / prev * 100) : null}
                      <div class="tend-resumen__fila" style="align-items:flex-start;flex-wrap:wrap;gap:.2rem">
                        <span class="tend-resumen__dot" style="background:{ci===0?'#D4AF37':'#667eea'};margin-top:3px"></span>
                        <span class="tend-resumen__clase">{cl}</span>
                        <span class="tend-resumen__val">Q{val.toLocaleString('es-GT',{minimumFractionDigits:0,maximumFractionDigits:0})}</span>
                        {#if delta !== null}
                          <span style="font-size:.65rem;font-weight:700;color:{delta>=0?'#10b981':'#ef4444'};margin-left:auto;white-space:nowrap">
                            {delta>=0?'▲':'▼'}{Math.abs(delta).toFixed(1)}%
                          </span>
                        {/if}
                      </div>
                    {/each}
                  </div>
                  <div class="tend-resumen__sep"></div>
                  <!-- Total período -->
                  <div class="tend-resumen__bloque">
                    <p class="tend-resumen__titulo" style="color:var(--gold)">Total período</p>
                    {#each canales as cl, ci}
                      {@const tot = meses.reduce((s,m)=>s+(byKey[`${m}|${cl}`]??0),0)}
                      <div class="tend-resumen__fila">
                        <span class="tend-resumen__dot" style="background:{ci===0?'#D4AF37':'#667eea'}"></span>
                        <span class="tend-resumen__clase">{cl}</span>
                        <span class="tend-resumen__val tend-resumen__val--total" style="color:var(--gold)">Q{tot.toLocaleString('es-GT',{minimumFractionDigits:0,maximumFractionDigits:0})}</span>
                      </div>
                    {/each}
                    <!-- Gran total combinado -->
                    {#each [canales.reduce((s,cl)=>s+meses.reduce((ss,m)=>ss+(byKey[`${m}|${cl}`]??0),0),0)] as grandTotal}
                      <div style="margin-top:.4rem;padding-top:.4rem;border-top:1px dashed rgba(212,175,55,.3)">
                        <div style="display:flex;align-items:center;justify-content:space-between">
                          <span style="font-size:.7rem;color:var(--text-muted);font-weight:700;text-transform:uppercase;letter-spacing:.05em">Combinado</span>
                          <span style="font-size:.88rem;font-weight:900;color:var(--gold)">Q{grandTotal.toLocaleString('es-GT',{minimumFractionDigits:0,maximumFractionDigits:0})}</span>
                        </div>
                      </div>
                    {/each}
                  </div>
                </div>
              </div>
            {/if}
          </div>

        </div>
      </div>

      <!-- ── 5. Mapa de calor de búsquedas ──────────────────────────────── -->
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
          </span>
        </p>
        {#if !negocioData.heatmap || negocioData.heatmap.length === 0}
          <div class="met-empty">Sin búsquedas en el período seleccionado</div>
        {:else}
          {@const DAY_SQL = {1:'Dom',2:'Lun',3:'Mar',4:'Mié',5:'Jue',6:'Vie',7:'Sáb'}}
          {@const DAY_ORDER = [2,3,4,5,6,7,1]}
          {@const horasSet = new Set(negocioData.heatmap.map(c => c.hora))}
          {@const horas = [...horasSet].sort((a,b)=>a-b)}
          {@const heatMap = new Map(negocioData.heatmap.map(c => [`${c.diaSemana},${c.hora}`, c]))}
          {@const pico = Math.max(...negocioData.heatmap.map(c => c.total), 1)}
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
                  {@const cell  = heatMap.get(`${dia},${h}`)}
                  {@const total = cell ? cell.total : 0}
                  {@const pct   = cell ? (total / pico * 100) : -1}
                  {@const fillColor = pct < 0   ? '#F2EFE9'
                    : pct === 0                 ? '#EDEBE5'
                    : pct < 20                  ? '#FEF3C7'
                    : pct < 40                  ? '#FCD34D'
                    : pct < 70                  ? '#D4AF37'
                    : '#92400E'}
                  {@const textColor = pct >= 40 ? 'white' : '#6b5a3a'}
                  <rect x={52+hi*cellW+1} y={20+di*cellH+1}
                    width={cellW-2} height={cellH-2}
                    rx="4" fill={fillColor}>
                    {#if cell}
                      <title>{DAY_SQL[dia]} {h}h — {cell.total} búsquedas ({pct.toFixed(0)}% del pico)</title>
                    {:else}
                      <title>{DAY_SQL[dia]} {h}h — sin búsquedas</title>
                    {/if}
                  </rect>
                  {#if cell && cellW >= 28}
                    <text x={52+hi*cellW+cellW/2} y={20+di*cellH+cellH/2+4}
                      text-anchor="middle" font-size="8" font-weight="600"
                      fill={textColor} pointer-events="none">
                      {cell.total}
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
      <h3 class="met-listado__titulo">Registro de búsquedas</h3>
    </div>

    {#if loadingListado}
      <div class="met-loading met-loading--sm"><div class="met-spinner"></div><p>Cargando listado...</p></div>
    {:else if metricasListado}
      <div class="met-tabla-wrap">
        <table class="table">
          <thead class="table__head">
            <tr>
              <th class="table__header">#</th>
              <th class="table__header">Destino</th>
              <th class="table__header">Check-in</th>
              <th class="table__header">Check-out</th>
              <th class="table__header">Personas</th>
              <th class="table__header">Usuario / Agencia</th>
              <th class="table__header">Canal</th>
              <th class="table__header">Fecha Búsqueda</th>
            </tr>
          </thead>
          <tbody>
            {#each metricasListado.registros as b}
              <tr class="table__row">
                <td class="table__cell">{b.id}</td>
                <td class="table__cell">{b.destino ?? '—'}</td>
                <td class="table__cell">{b.checkIn ?? '—'}</td>
                <td class="table__cell">{b.checkOut ?? '—'}</td>
                <td class="table__cell">{b.personas ?? '—'}</td>
                <td class="table__cell">{b.usuario ?? b.agencia ?? 'Anónimo'}</td>
                <td class="table__cell">
                  <span class="met-tipo-badge met-tipo-badge--{(b.tipo ?? 'web').toLowerCase()}">{b.tipo ?? 'web'}</span>
                </td>
                <td class="table__cell" style="font-size:.8rem;color:var(--text-muted)">{(b.fechaHora ?? '').slice(0,10)}</td>
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
            Página {metricasListado.paginaActual} de {metricasListado.totalPaginas}
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
              { key:'kpi',                   label:'KPI de Ingresos'             },
              { key:'reservacionesDiarias',   label:'Reservaciones diarias'       },
              { key:'canal',                  label:'Canal de reservación'         },
              { key:'embudo',                 label:'Embudo de conversión'         },
              { key:'hoteles',                label:'Rendimiento de hoteles'       },
              { key:'cancelaciones',          label:'Análisis de cancelaciones'    },
              { key:'tendencia',              label:'Tendencia de ingresos'        },
              { key:'heatmap',                label:'Mapa de calor búsquedas'      },
              { key:'registro',               label:'Registro completo búsquedas'  },
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
            <button class="exp-format-tab" class:exp-format-tab--active={expFormato==='email'}
              on:click={() => expFormato='email'}>
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
              </svg>
              Por correo
            </button>
            <button class="exp-format-tab" class:exp-format-tab--active={expFormato==='pdf'}
              on:click={() => expFormato='pdf'}>
              <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/>
              </svg>
              PDF
            </button>
          </div>
        </div>

        <!-- Correos (solo si email) -->
        {#if expFormato === 'email'}
          <div class="exp-bloque">
            <span class="exp-bloque-titulo">Formato del adjunto</span>
            <div class="exp-format-tabs exp-format-tabs--sm">
              <button class="exp-format-tab" class:exp-format-tab--active={expFormatoEmail==='excel'}
                on:click={() => expFormatoEmail='excel'}>Excel (.xlsx)</button>
              <button class="exp-format-tab" class:exp-format-tab--active={expFormatoEmail==='csv'}
                on:click={() => expFormatoEmail='csv'}>CSV (.zip)</button>
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
          {#if expFormato === 'pdf'}
            <button class="btn-primary" disabled={!Object.values(expSec).some(v=>v)}
              on:click={() => { exportarPdf(); mostrarModalExportar = false; }}>
              Descargar PDF
            </button>
          {:else}
            <button class="btn-primary" disabled={enviandoExportar || !Object.values(expSec).some(v=>v)}
              on:click={handleExportar}>
              {#if enviandoExportar}
                {expFormato === 'email' ? 'Enviando...' : 'Generando...'}
              {:else if expFormato === 'email'}
                Enviar por correo
              {:else if expFormato === 'excel'}
                Descargar Excel
              {:else}
                Descargar CSV
              {/if}
            </button>
          {/if}
        </div>

      </div>
    </div>
  </div>
{/if}
