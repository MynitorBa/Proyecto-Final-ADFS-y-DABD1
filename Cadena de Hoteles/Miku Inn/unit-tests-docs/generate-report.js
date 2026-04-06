import { execSync }      from 'child_process';
import fs                from 'fs';
import path              from 'path';
import http              from 'http';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname  = path.dirname(__filename);
const rootDir    = path.join(__dirname, '..');

// 1. Correr Jest con salida JSON
let jestOutput = '';

try {
  jestOutput = execSync('npx jest --json', {
    encoding: 'utf-8',
    cwd: rootDir,
    stdio: ['pipe', 'pipe', 'pipe'],
  });
} catch (err) {
  jestOutput = err.stdout || '';
}

// 2. Parsear resultado
let results;
try {
  results = JSON.parse(jestOutput);
} catch {
  console.error('No se pudo parsear la salida de Jest.');
  console.error('Primeros 500 chars recibidos:');
  console.error(jestOutput.slice(0, 500));
  process.exit(1);
}

const totalTests = results.numTotalTests  ?? 0;
const passed     = results.numPassedTests ?? 0;
const failed     = results.numFailedTests ?? 0;
const allPassed  = results.success        ?? false;
const duration   = ((results.testResults || [])
  .reduce((a, s) => a + (s.perfStats?.runtime ?? 0), 0) / 1000).toFixed(3);
const runDate    = new Date().toLocaleString('es-GT');

// 3. Construir filas de tests
let testRows = '';
for (const suite of results.testResults || []) {
  const filePath   = suite.testFilePath ?? suite.name ?? '';
  const suiteName  = filePath ? path.basename(filePath) : '(desconocido)';
  const innerTests = suite.testResults ?? suite.assertionResults ?? [];

  for (const t of innerTests) {
    const ok      = t.status === 'passed';
    const icon    = ok ? 'OK' : 'FAIL';
    const badge   = ok
      ? `<span class="badge pass">PASS</span>`
      : `<span class="badge fail">FAIL</span>`;
    const dur     = t.duration != null ? `${t.duration} ms` : '-';
    const name    = t.fullName ?? t.title ?? '(sin nombre)';
    const errHtml = (!ok && t.failureMessages?.length)
      ? `<pre class="error-msg">${t.failureMessages.join('\n')}</pre>`
      : '';

    testRows += `
      <tr class="${ok ? 'row-pass' : 'row-fail'}">
        <td class="icon-cell">${icon}</td>
        <td class="suite-name">${suiteName}</td>
        <td>${name}</td>
        <td>${badge}</td>
        <td class="dur">${dur}</td>
      </tr>
      ${errHtml ? `<tr class="row-error"><td colspan="5">${errHtml}</td></tr>` : ''}`;
  }
}

// 4. HTML final
const statusColor = allPassed ? '#16a34a' : '#dc2626';
const statusBg    = allPassed ? '#dcfce7' : '#fee2e2';
const failColor   = failed > 0 ? '#dc2626' : '#718096';

const html = `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Resultados de Tests - Miku Inn</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
  <style>
    :root {
      --teal-dark: #0f2027;
      --teal-mid:  #134e4a;
      --teal:      #0f766e;
      --pass:      #16a34a;
      --pass-bg:   #dcfce7;
      --fail:      #dc2626;
      --fail-bg:   #fee2e2;
      --border:    #ccfbf1;
      --bg:        #f8fffe;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Inter', sans-serif; background: var(--bg); color: #1a202c; min-height: 100vh; }

    .hero {
      background: linear-gradient(135deg, var(--teal-dark) 0%, var(--teal-mid) 50%, var(--teal) 100%);
      padding: 3rem 2rem; text-align: center; color: white; position: relative; overflow: hidden;
    }
    .hero::before {
      content: ''; position: absolute; inset: 0;
      background: radial-gradient(ellipse at 70% 20%, rgba(45,212,191,0.18) 0%, transparent 60%);
      pointer-events: none;
    }
    .hero h1 { font-size: 2rem; font-weight: 700; letter-spacing: -0.02em; margin-bottom: 0.35rem; }
    .hero p  { opacity: 0.7; font-size: 0.95rem; }
    .hero-badges { margin-top: 1.1rem; display: flex; gap: 0.5rem; justify-content: center; flex-wrap: wrap; }
    .hero-badge {
      background: rgba(255,255,255,0.12); border: 1px solid rgba(255,255,255,0.22);
      padding: 0.25rem 0.8rem; border-radius: 20px; font-size: 0.78rem; font-weight: 500;
    }

    .container { max-width: 1000px; margin: 0 auto; padding: 2rem 1.5rem; }

    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
    .card {
      background: white; border: 1px solid var(--border); border-radius: 12px;
      padding: 1.25rem 1.5rem; display: flex; flex-direction: column; gap: 0.25rem;
      box-shadow: 0 1px 4px rgba(0,0,0,0.05);
    }
    .card-label { font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #718096; }
    .card-value { font-size: 2rem; font-weight: 700; line-height: 1; }
    .card.status .card-value { color: ${statusColor}; }
    .card.pass   .card-value { color: var(--pass); }
    .card.fail   .card-value { color: ${failColor}; }
    .card.time   .card-value { color: var(--teal); font-size: 1.5rem; }

    .table-wrap { background: white; border: 1px solid var(--border); border-radius: 12px; overflow: hidden; box-shadow: 0 1px 8px rgba(0,0,0,0.06); }
    .table-header { padding: 1rem 1.5rem; border-bottom: 1px solid var(--border); font-weight: 600; font-size: 0.9rem; color: var(--teal-dark); background: #f0fdfa; }
    table { width: 100%; border-collapse: collapse; }
    thead th { background: var(--teal); color: white; font-size: 0.8rem; font-weight: 600; text-align: left; padding: 0.7rem 1rem; }
    td { padding: 0.7rem 1rem; font-size: 0.875rem; border-bottom: 1px solid #f0fdfa; vertical-align: middle; }
    .icon-cell { font-size: 0.75rem; font-weight: 700; font-family: 'JetBrains Mono', monospace; }
    .row-pass .icon-cell { color: var(--pass); }
    .row-fail .icon-cell { color: var(--fail); }
    .row-pass:hover td { background: #f0fdf4; }
    .row-fail td       { background: #fff5f5; }
    .row-fail:hover td { background: #fee2e2; }
    .row-error td      { background: #fff8f8; padding: 0; }
    .suite-name { font-family: 'JetBrains Mono', monospace; font-size: 0.8rem; color: #4a5568; }
    .dur        { font-family: 'JetBrains Mono', monospace; font-size: 0.8rem; color: #718096; }
    .badge { display: inline-block; padding: 0.2rem 0.65rem; border-radius: 999px; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.04em; }
    .badge.pass { background: var(--pass-bg); color: var(--pass); }
    .badge.fail { background: var(--fail-bg); color: var(--fail); }
    .error-msg {
      font-family: 'JetBrains Mono', monospace; font-size: 0.78rem;
      background: #fff0f0; color: #c53030; padding: 0.75rem 1rem;
      border-left: 3px solid var(--fail); white-space: pre-wrap; word-break: break-word;
    }
    .footer { margin-top: 2.5rem; text-align: center; font-size: 0.8rem; color: #a0aec0; }
    .status-bar {
      padding: 0.6rem 1.5rem; text-align: center; font-weight: 600; font-size: 0.875rem;
      background: ${statusBg}; color: ${statusColor}; border-bottom: 2px solid ${statusColor};
    }

    /* Banner de cierre */
    .close-banner {
      position: fixed; bottom: 0; left: 0; right: 0;
      background: var(--teal-dark); color: rgba(255,255,255,0.85);
      padding: 0.6rem 1.5rem;
      display: flex; align-items: center; justify-content: space-between;
      font-size: 0.82rem; z-index: 999;
      border-top: 1px solid rgba(45,212,191,0.3);
    }
    .close-banner span { opacity: 0.7; }
    .btn-close {
      background: var(--fail); color: white; border: none; border-radius: 6px;
      padding: 0.35rem 1rem; font-size: 0.8rem; font-weight: 600;
      cursor: pointer; font-family: inherit;
      transition: opacity 0.15s;
    }
    .btn-close:hover { opacity: 0.85; }
  </style>
</head>
<body>

  <div class="hero">
    <h1>Pruebas Unitarias - Frontend</h1>
    <p>Miku Inn &middot; Svelte &middot; validarFechas.js</p>
    <div class="hero-badges">
      <span class="hero-badge">Jest</span>
      <span class="hero-badge">Babel</span>
      <span class="hero-badge">Svelte</span>
      <span class="hero-badge">DABD1 2026</span>
    </div>
  </div>

  <div class="status-bar">
    ${allPassed ? 'Todos los tests pasaron correctamente' : `${failed} test(s) fallaron`}
    &nbsp;&middot;&nbsp; Ejecutado el ${runDate}
  </div>

  <div class="container">
    <div class="summary">
      <div class="card status">
        <span class="card-label">Estado</span>
        <span class="card-value">${allPassed ? 'PASS' : 'FAIL'}</span>
      </div>
      <div class="card pass">
        <span class="card-label">Pasaron</span>
        <span class="card-value">${passed}</span>
      </div>
      <div class="card fail">
        <span class="card-label">Fallaron</span>
        <span class="card-value">${failed}</span>
      </div>
      <div class="card">
        <span class="card-label">Total</span>
        <span class="card-value" style="color:var(--teal-dark)">${totalTests}</span>
      </div>
      <div class="card time">
        <span class="card-label">Duracion</span>
        <span class="card-value">${duration}s</span>
      </div>
    </div>

    <div class="table-wrap">
      <div class="table-header">Detalle de pruebas</div>
      <table>
        <thead>
          <tr>
            <th style="width:60px">Estado</th>
            <th>Archivo</th>
            <th>Prueba</th>
            <th style="width:90px">Resultado</th>
            <th style="width:90px">Tiempo</th>
          </tr>
        </thead>
        <tbody>
          ${testRows || '<tr><td colspan="5" style="text-align:center;color:#718096;padding:2rem">No se encontraron tests.</td></tr>'}
        </tbody>
      </table>
    </div>

    <div class="footer">
      Generado automaticamente &middot; Miku Inn Unit Tests &middot; DABD1 2026
    </div>
  </div>

  <!-- Banner fijo en la parte inferior -->
  <div class="close-banner">
    <span>Servidor activo en http://localhost:3399 &mdash; Cierra esta pestana para detenerlo, o usa el boton.</span>
    <button class="btn-close" onclick="fetch('/shutdown').then(() => window.close())">Cerrar servidor</button>
  </div>

</body>
</html>`;

// 5. Guardar el HTML tambien en disco (opcional, por si quieren el archivo)
const outPath = path.join(__dirname, 'results.html');
fs.writeFileSync(outPath, html, 'utf-8');

// 6. Levantar servidor HTTP temporal
const PORT = 3399;

const server = http.createServer((req, res) => {
  if (req.url === '/shutdown') {
    res.writeHead(200);
    res.end('ok');
    console.log('Servidor cerrado por el usuario.');
    server.close(() => process.exit(0));
    return;
  }

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(html);
});

server.listen(PORT, '127.0.0.1', async () => {
  const url = `http://localhost:${PORT}`;
  console.log('');
  console.log(allPassed ? 'Todos los tests pasaron.' : `${failed} test(s) fallaron.`);
  console.log(`${passed}/${totalTests} tests OK - ${duration}s`);
  console.log(`Abriendo reporte en ${url}`);
  console.log('Presiona Ctrl+C o usa el boton en el navegador para cerrar.');
  console.log('');

  // Abrir navegador con el modulo 'open'
  try {
    const { default: open } = await import('open');
    await open(url);
  } catch {
    console.log(`No se pudo abrir el navegador automaticamente.`);
    console.log(`Abre manualmente: ${url}`);
  }
});

// Cerrar con Ctrl+C limpiamente
process.on('SIGINT', () => {
  console.log('\nServidor detenido.');
  server.close(() => process.exit(0));
});