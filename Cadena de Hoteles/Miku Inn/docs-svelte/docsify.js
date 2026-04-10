// Configuración global de Docsify con sidebar, búsqueda y plugin del hero
window.$docsify = {
  name: '',
  repo: '',
  loadSidebar: true,
  subMaxLevel: 2,
  coverpage: false,
  auto2top: true,
  search: { placeholder: 'Buscar...', noData: 'Sin resultados' },

  plugins: [
    function(hook) {
      hook.doneEach(function() {

        // Elimina el hero previo si existe para evitar duplicados al navegar
        var existing = document.querySelector('.hero-header');
        if (existing) existing.remove();

        // Construye el hero header con ícono, título, subtítulo y badges
        var hero = document.createElement('div');
        hero.className = 'hero-header';
        hero.innerHTML = `
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="1.5">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          <h1>Miku Inn Docs</h1>
          <p>Documentación Técnica del Frontend &middot; Svelte + Vite</p>
          <div class="hero-badges">
            <span class="hero-badge">Svelte</span>
            <span class="hero-badge">Vite</span>
            <span class="hero-badge">JavaScript</span>
            <span class="hero-badge">DABD1 2026</span>
          </div>`;

        var content = document.querySelector('.content');
        if (content) content.prepend(hero);

        // Añade el botón flotante de descarga PDF una sola vez
        if (!document.querySelector('.pdf-download-btn')) {
          var btn = document.createElement('button');
          btn.className = 'pdf-download-btn';
          btn.innerHTML = `
            <span class="pdf-btn-spinner"></span>
            <svg class="pdf-btn-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            <span class="pdf-btn-text">Descargar PDF</span>`;
          btn.onclick = exportarPDF;
          document.body.appendChild(btn);
        }
      });
    }
  ]
};

// ─── Layout A4 ───────────────────────────────────────────────────────────────
var PL = 14;        // margen izquierdo
var PR = 196;       // margen derecho
var PW = 182;       // ancho útil
var PT = 24;        // primera línea de contenido (bajo el header de página)
var PB = 276;       // límite inferior (sobre el footer)
var PH = 297;       // alto total A4
var PW2 = 210;      // ancho total A4

// ─── Colores de marca ────────────────────────────────────────────────────────
var C = {
  dark:    [15,  32,  39 ],
  mid:     [19,  78,  74 ],
  teal:    [15, 118, 110 ],
  accent:  [45, 212, 191 ],
  white:   [255,255, 255 ],
  text:    [30,  41,  59 ],
  muted:   [100,116, 139 ],
  border:  [204,251, 241 ],
  bglight: [240,253, 250 ],
  code_bg: [30,  41,  59 ],
  code_fg: [226,232, 240 ],
  h2col:   [19,  78,  74 ],
  h3col:   [15, 118, 110 ],
  amber:   [180,120,  20 ]
};

/**
 * Aplica un color de texto al documento PDF.
 * @param {object} doc - Instancia de jsPDF.
 * @param {number[]} rgb - Array [r, g, b].
 */
function tc(doc, rgb) { doc.setTextColor(rgb[0], rgb[1], rgb[2]); }

/**
 * Aplica un color de relleno al documento PDF.
 * @param {object} doc - Instancia de jsPDF.
 * @param {number[]} rgb - Array [r, g, b].
 */
function fc(doc, rgb) { doc.setFillColor(rgb[0], rgb[1], rgb[2]); }

/**
 * Aplica un color de trazo al documento PDF.
 * @param {object} doc - Instancia de jsPDF.
 * @param {number[]} rgb - Array [r, g, b].
 */
function dc(doc, rgb) { doc.setDrawColor(rgb[0], rgb[1], rgb[2]); }

/**
 * Comprueba si la posición Y supera el límite inferior y agrega página si es necesario.
 * @param {object} doc - Instancia de jsPDF.
 * @param {number} y - Posición Y actual.
 * @param {number} [need=0] - Espacio requerido para el próximo elemento.
 * @param {object} ctx - Contexto con título de página y número para el header.
 * @returns {number} Posición Y actualizada.
 */
function chy(doc, y, need, ctx) {
  if (y + (need || 0) > PB) {
    doc.addPage();
    ctx.pagina++;
    dibujarHeader(doc, ctx.titulo, ctx.pagina);
    return PT;
  }
  return y;
}

/**
 * Escribe un bloque de texto con salto automático de página.
 * @param {object} doc - Instancia de jsPDF.
 * @param {string[]} lines - Líneas ya divididas por splitTextToSize.
 * @param {number} x - Posición X.
 * @param {number} y - Posición Y inicial.
 * @param {number} lead - Interlineado en mm.
 * @param {object} ctx - Contexto para el header de página.
 * @returns {number} Posición Y final.
 */
function wr(doc, lines, x, y, lead, ctx) {
  for (var i = 0; i < lines.length; i++) {
    y = chy(doc, y, 0, ctx);
    doc.text(lines[i], x, y);
    y += lead;
  }
  return y;
}

/**
 * Dibuja el header y footer de cada página del PDF.
 * Header: banda teal oscura con nombre del documento y título de sección.
 * Footer: número de página centrado con línea separadora.
 * @param {object} doc - Instancia de jsPDF.
 * @param {string} titulo - Título de la página actual para el header.
 * @param {number} num - Número de página para el footer.
 */
function dibujarHeader(doc, titulo, num) {
  // Header superior con gradiente simulado por dos rectángulos
  fc(doc, C.dark); doc.rect(0, 0, PW2, 14, 'F');
  fc(doc, C.mid);  doc.rect(0, 10, PW2, 4, 'F');
  fc(doc, C.accent); doc.rect(0, 13.5, PW2, 0.8, 'F');

  // Logo / nombre a la izquierda del header
  tc(doc, C.accent);
  doc.setFontSize(7.5); doc.setFont('helvetica', 'bold');
  doc.text('MIKU INN DOCS', PL, 9);

  // Título de la sección a la derecha del header
  tc(doc, [200, 240, 235]);
  doc.setFontSize(7); doc.setFont('helvetica', 'normal');
  var tit = titulo.length > 55 ? titulo.slice(0, 52) + '...' : titulo;
  doc.text(sanitizar(tit), PR, 9, { align: 'right' });

  // Footer con número de página
  dc(doc, C.border); doc.setLineWidth(0.3);
  doc.line(PL, PH - 10, PR, PH - 10);
  tc(doc, C.muted);
  doc.setFontSize(7.5); doc.setFont('helvetica', 'normal');
  doc.text('Pag. ' + num, PW2 / 2, PH - 6, { align: 'center' });
}

/**
 * Sanitiza una cadena para compatibilidad con la fuente Helvetica de jsPDF.
 * Helvetica cubre Latin-1 (Win-1252). Caracteres fuera de ese rango como
 * flechas, comillas tipográficas o em-dashes se reemplazan por equivalentes ASCII
 * para evitar caracteres corruptos o cuadros en el PDF resultante.
 * @param {string} str - Cadena a sanitizar.
 * @returns {string}
 */
function sanitizar(str) {
  if (!str) return '';
  return String(str)
    .replace(/\u2192|\u21D2|\u27A1/g, '->')   // flechas derecha →
    .replace(/\u2190|\u21D0/g, '<-')           // flechas izquierda ←
    .replace(/\u2194/g, '<->').replace(/\u2191/g, '^').replace(/\u2193/g, 'v')
    .replace(/[\u201C\u201D\u00AB\u00BB]/g, '"') // comillas tipograficas
    .replace(/[\u2018\u2019]/g, "'")             // comillas simples tipograficas
    .replace(/[\u2014\u2013]/g, '-')             // em-dash / en-dash
    .replace(/\u2026/g, '...')                   // elipsis
    .replace(/\u2022/g, '*')                     // bullet •
    .replace(/\u00B7/g, '.')                     // punto medio ·
    .replace(/\u00D7/g, 'x')                     // multiplicacion x
    .replace(/\u2260/g, '!=').replace(/\u2264/g, '<=').replace(/\u2265/g, '>=')
    .replace(/[^\x00-\xFF]/g, '?');              // resto fuera de Latin-1
}

/**
 * Limpia la sintaxis Markdown de una línea devolviendo solo el texto plano
 * sanitizado para jsPDF (sin caracteres fuera de Latin-1).
 * @param {string} line - Línea de markdown.
 * @returns {string}
 */
function lmd(line) {
  return sanitizar(line
    .replace(/\*\*([^*]+)\*\*/g, '$1').replace(/\*([^*]+)\*/g, '$1')
    .replace(/__([^_]+)__/g, '$1').replace(/_([^_]+)_/g, '$1')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/^#{1,6}\s+/, '').replace(/^[-*+]\s+/, '')
    .replace(/^\d+\.\s+/, '').replace(/^>\s+/, '').trim());
}

/**
 * Renderiza el contenido Markdown de una página directamente en jsPDF.
 * Maneja h1-h4, bloques de código con fondo oscuro, blockquotes, listas,
 * separadores, tablas y párrafos normales con colores de marca.
 * @param {object} doc - Instancia de jsPDF.
 * @param {string} md - Contenido markdown.
 * @param {number} startY - Posición Y inicial.
 * @param {object} ctx - Contexto con titulo y pagina para el header.
 * @returns {number} Posición Y final.
 */
function rmd(doc, md, startY, ctx) {
  var y     = startY;
  var lines = md.split('\n');
  var i     = 0;

  while (i < lines.length) {
    var raw = lines[i]; var tri = raw.trim(); i++;

    if (!tri) { y += 2.5; continue; }

    // Ignorar comentarios HTML generados por documentation.js
    if (/^<!--/.test(tri)) continue;

    // Ignorar definiciones de referencia markdown: [1]: #anchor o [1]: https://...
    if (/^\[\d+\]:\s/.test(tri)) continue;

    // H1
    if (/^#\s/.test(tri)) {
      y = chy(doc, y, 12, ctx);
      doc.setFontSize(15); doc.setFont('helvetica', 'bold'); tc(doc, C.dark);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 8, ctx);
      dc(doc, C.border); doc.setLineWidth(0.5);
      doc.line(PL, y, PL + PW, y); y += 5;
      continue;
    }

    // H2
    if (/^##\s/.test(tri)) {
      y = chy(doc, y, 10, ctx);
      doc.setFontSize(12); doc.setFont('helvetica', 'bold'); tc(doc, C.h2col);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 7, ctx);
      dc(doc, C.border); doc.setLineWidth(0.3);
      doc.line(PL, y, PL + PW * 0.5, y); y += 4;
      continue;
    }

    // H3
    if (/^###\s/.test(tri)) {
      y = chy(doc, y, 8, ctx);
      doc.setFontSize(10.5); doc.setFont('helvetica', 'bold'); tc(doc, C.h3col);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 6, ctx); y += 1;
      continue;
    }

    // H4
    if (/^####\s/.test(tri)) {
      y = chy(doc, y, 7, ctx);
      doc.setFontSize(9.5); doc.setFont('helvetica', 'bold'); tc(doc, C.mid);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 5.5, ctx); y += 0.5;
      continue;
    }

    // Bloque de código ```
    if (tri.startsWith('```')) {
      var clines = [];
      while (i < lines.length && !lines[i].trim().startsWith('```')) { clines.push(lines[i]); i++; }
      i++;
      if (!clines.length) continue;
      var vis  = clines.slice(0, 25);
      var bh   = vis.length * 4.2 + 7;
      y = chy(doc, y, bh, ctx);
      fc(doc, C.code_bg);
      doc.roundedRect(PL, y - 2, PW, bh, 2, 2, 'F');
      tc(doc, C.accent); doc.setFontSize(6.5); doc.setFont('helvetica', 'bold');
      doc.text('CODIGO', PL + 3, y + 2);
      var cy = y + 5;
      tc(doc, C.code_fg); doc.setFontSize(7.2); doc.setFont('courier', 'normal');
      for (var ci = 0; ci < vis.length; ci++) {
        var cs = doc.splitTextToSize(sanitizar(vis[ci] || ' '), PW - 6);
        for (var cj = 0; cj < cs.length; cj++) {
          cy = chy(doc, cy, 0, ctx);
          doc.text(cs[cj], PL + 3, cy); cy += 4.2;
        }
      }
      y = cy + 3; continue;
    }

    // Blockquote
    if (tri.startsWith('> ')) {
      var txt = lmd(tri.slice(2));
      var sp  = doc.splitTextToSize(txt, PW - 10);
      var bh  = sp.length * 5.2 + 5;
      y = chy(doc, y, bh, ctx);
      fc(doc, C.bglight); doc.rect(PL, y - 3, PW, bh, 'F');
      fc(doc, C.accent);  doc.rect(PL, y - 3, 2, bh, 'F');
      tc(doc, [45, 55, 72]); doc.setFontSize(9); doc.setFont('helvetica', 'italic');
      y = wr(doc, sp, PL + 5, y + 1.5, 5.2, ctx); y += 2; continue;
    }

    // Separador ---
    if (/^-{3,}$/.test(tri) || /^={3,}$/.test(tri)) {
      y = chy(doc, y, 4, ctx);
      dc(doc, C.border); doc.setLineWidth(0.3);
      doc.line(PL, y, PR, y); y += 4; continue;
    }

    // Lista de viñetas
    if (/^[-*+]\s/.test(tri)) {
      var txt = lmd(tri);
      var sp  = doc.splitTextToSize(txt, PW - 9);
      y = chy(doc, y, sp.length * 5, ctx);
      tc(doc, C.accent); doc.setFontSize(9); doc.setFont('helvetica', 'normal');
      doc.text('-', PL + 2, y);
      tc(doc, C.text);
      y = wr(doc, sp, PL + 7, y, 5, ctx); continue;
    }

    // Lista numerada
    if (/^\d+\.\s/.test(tri)) {
      var num  = tri.match(/^(\d+)\./)[1];
      var txt  = lmd(tri);
      var sp   = doc.splitTextToSize(txt, PW - 9);
      y = chy(doc, y, sp.length * 5, ctx);
      tc(doc, C.teal); doc.setFontSize(8.5); doc.setFont('helvetica', 'bold');
      doc.text(num + '.', PL + 2, y);
      tc(doc, C.text); doc.setFont('helvetica', 'normal');
      y = wr(doc, sp, PL + 8, y, 5, ctx); continue;
    }

    // Tabla: omite líneas de separador
    if (tri.startsWith('|')) {
      if (/^\|[\s\-:|]+\|/.test(tri)) continue;
      var cells = tri.split('|').filter(function(c) { return c.trim(); }).map(function(c) { return c.trim(); });
      var row   = cells.join('   ');
      var sp    = doc.splitTextToSize(row, PW);
      y = chy(doc, y, sp.length * 4.8, ctx);
      fc(doc, C.bglight); doc.rect(PL, y - 3, PW, sp.length * 4.8 + 1, 'F');
      tc(doc, C.text); doc.setFontSize(8); doc.setFont('helvetica', 'normal');
      y = wr(doc, sp, PL + 2, y, 4.8, ctx); y += 0.5; continue;
    }

    // Párrafo normal
    var txt = lmd(tri);
    if (!txt) continue;
    if (/^type:/i.test(txt)) {
      y = chy(doc, y, 7, ctx);
      tc(doc, C.muted); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
      doc.text('TYPE', PL, y);
      tc(doc, C.teal); doc.setFont('courier', 'normal');
      var sp = doc.splitTextToSize(txt.replace(/^type:\s*/i, ''), PW - 16);
      y = wr(doc, sp, PL + 16, y, 5, ctx); y += 1; continue;
    }
    if (/^returns?\s/i.test(txt)) {
      y = chy(doc, y, 7, ctx);
      tc(doc, C.muted); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
      doc.text('RETURNS', PL, y);
      tc(doc, C.teal); doc.setFont('courier', 'normal');
      var sp = doc.splitTextToSize(txt.replace(/^returns?\s*/i, ''), PW - 20);
      y = wr(doc, sp, PL + 20, y, 5, ctx); y += 1; continue;
    }

    y = chy(doc, y, 6, ctx);
    doc.setFontSize(9); doc.setFont('helvetica', 'normal'); tc(doc, C.text);
    var sp = doc.splitTextToSize(txt, PW);
    y = wr(doc, sp, PL, y, 5.2, ctx);
  }
  return y;
}

/**
 * Descarga un archivo .md con detección automática de encoding.
 * PowerShell genera archivos UTF-16 LE con BOM cuando usa el operador >
 * para redirigir salida. fetch().text() asume UTF-8, por lo que los
 * archivos UTF-16 LE producen null bytes entre cada caracter en el string
 * resultante. Esta función detecta el BOM y usa TextDecoder adecuado.
 * @async
 * @param {string} url - URL del archivo .md a descargar.
 * @returns {Promise<string|null>} Contenido decodificado o null si falla.
 */
async function fetchMd(url) {
  var res = await fetch(url);
  if (!res.ok) return null;
  var buf   = await res.arrayBuffer();
  var bytes = new Uint8Array(buf);

  // UTF-16 LE con BOM: FF FE (generado por PowerShell > en Windows)
  if (bytes[0] === 0xFF && bytes[1] === 0xFE) {
    return new TextDecoder('utf-16le').decode(buf);
  }
  // UTF-16 BE con BOM: FE FF
  if (bytes[0] === 0xFE && bytes[1] === 0xFF) {
    return new TextDecoder('utf-16be').decode(buf);
  }
  // UTF-8 con BOM: EF BB BF
  if (bytes[0] === 0xEF && bytes[1] === 0xBB && bytes[2] === 0xBF) {
    return new TextDecoder('utf-8').decode(buf);
  }
  // Heurística sin BOM: si el segundo byte es nulo probablemente es UTF-16 LE
  if (bytes.length > 10 && bytes[1] === 0x00) {
    return new TextDecoder('utf-16le').decode(buf);
  }
  // Por defecto UTF-8
  return new TextDecoder('utf-8').decode(buf);
}

/**
 * Genera y descarga el PDF de toda la documentación ordenada según el sidebar.
 * Usa jsPDF directamente (sin html2canvas) para máxima compatibilidad.
 * @async
 * @returns {Promise<void>}
 */
async function exportarPDF() {
  var btn     = document.querySelector('.pdf-download-btn');
  var texto   = btn.querySelector('.pdf-btn-text');
  var spinner = btn.querySelector('.pdf-btn-spinner');
  var icono   = btn.querySelector('.pdf-btn-icon');

  btn.disabled = true; texto.textContent = 'Generando...';
  spinner.style.display = 'inline-block'; icono.style.display = 'none';

  try {
    // Leer el sidebar con detección automática de encoding (puede ser UTF-16 LE en Windows)
    var sbMd  = await fetchMd('_sidebar.md');
    var secAct = 'General'; var paginas = [];

    sbMd.split('\n').forEach(function(l) {
      var ms = l.match(/\*\*([^*]+)\*\*/);
      var ml = l.match(/\[([^\]]+)\]\(([^)]+)\)/);
      if (ms) secAct = ms[1];
      if (ml) paginas.push({ titulo: ml[1], archivo: ml[2], seccion: secAct });
    });

    // Descargar todos los .md en paralelo con detección automática de encoding.
    // PowerShell crea los .md con UTF-16 LE (redirect > en Windows). fetch().text()
    // asume UTF-8, resultando en null bytes entre cada carácter. fetchMd() detecta
    // el BOM y usa TextDecoder con el encoding correcto.
    var contenidos = await Promise.all(paginas.map(async function(p) {
      try {
        var md = await fetchMd(p.archivo + '.md');
        if (md === null) return null;
        return { titulo: p.titulo, seccion: p.seccion, md: md };
      } catch(_) { return null; }
    }));
    var validos = contenidos.filter(Boolean);
    if (!validos.length) throw new Error('No se pudo cargar ninguna página .md');

    var jsPDF = window.jspdf.jsPDF;
    var doc   = new jsPDF('p', 'mm', 'a4');
    var numPag = 0;

    // ── Portada ───────────────────────────────────────────────────────────────
    fc(doc, C.dark); doc.rect(0, 0, PW2, PH, 'F');
    fc(doc, C.mid);    doc.rect(0, 105, PW2, 70, 'F');
    fc(doc, C.accent); doc.rect(0, 103, PW2, 3.5, 'F');
    fc(doc, C.accent); doc.rect(0, 174, PW2, 1.5, 'F');
    fc(doc, [20, 55, 50]);
    for (var di = 0; di < 8; di++) doc.circle(PL + di * 24, 95, 1.5, 'F');
    tc(doc, C.white); doc.setFontSize(28); doc.setFont('helvetica', 'bold');
    doc.text('Miku Inn', PW2 / 2, 120, { align: 'center' });
    doc.setFontSize(14); doc.setFont('helvetica', 'normal');
    doc.text('Documentacion Tecnica del Frontend', PW2 / 2, 133, { align: 'center' });
    tc(doc, C.accent); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
    doc.text('Svelte + Vite  |  DABD1 2026', PW2 / 2, 148, { align: 'center' });
    tc(doc, [160, 210, 205]); doc.setFontSize(8.5); doc.setFont('helvetica', 'normal');
    doc.text('Generado el ' + new Date().toLocaleDateString('es-GT', { year:'numeric', month:'long', day:'numeric' }), PW2 / 2, 162, { align: 'center' });

    // ── Índice ────────────────────────────────────────────────────────────────
    doc.addPage(); numPag++;
    dibujarHeader(doc, 'Indice de contenidos', numPag);
    var iy = PT;
    tc(doc, C.dark); doc.setFontSize(13); doc.setFont('helvetica', 'bold');
    doc.text('Indice de contenidos', PL, iy); iy += 8;
    dc(doc, C.border); doc.setLineWidth(0.5); doc.line(PL, iy, PR, iy); iy += 5;

    var secAnterior = null;
    for (var vi = 0; vi < validos.length; vi++) {
      var vp = validos[vi];
      if (vp.seccion !== secAnterior) {
        secAnterior = vp.seccion;
        iy = chy(doc, iy, 8, { titulo: 'Indice', pagina: numPag });
        tc(doc, C.teal); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
        doc.text(sanitizar(vp.seccion.toUpperCase()), PL, iy); iy += 5.5;
      }
      iy = chy(doc, iy, 5, { titulo: 'Indice', pagina: numPag });
      tc(doc, C.muted); doc.setFontSize(7.5); doc.setFont('helvetica', 'normal');
      doc.text('-', PL + 4, iy);
      tc(doc, C.text);
      doc.text(sanitizar(vp.titulo), PL + 8, iy); iy += 5;
    }

    // ── Contenido ─────────────────────────────────────────────────────────────
    var secPrev = null;
    for (var pi = 0; pi < validos.length; pi++) {
      var p = validos[pi];
      doc.addPage(); numPag++;
      dibujarHeader(doc, p.titulo, numPag);
      var y = PT;

      // Banda de sección cuando cambia el grupo
      if (p.seccion !== secPrev) {
        secPrev = p.seccion;
        fc(doc, C.bglight); doc.rect(PL, y - 4, PW, 8, 'F');
        fc(doc, C.accent);  doc.rect(PL, y - 4, 2, 8, 'F');
        tc(doc, C.teal); doc.setFontSize(7); doc.setFont('helvetica', 'bold');
        doc.text(sanitizar(p.seccion.toUpperCase()), PL + 5, y + 1);
        y += 10;
      }

      // Título de la página con fondo oscuro
      fc(doc, C.mid); doc.roundedRect(PL, y, PW, 10, 2, 2, 'F');
      fc(doc, C.accent); doc.roundedRect(PL, y, 3, 10, 1, 1, 'F');
      tc(doc, C.white); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
      doc.text(sanitizar(p.titulo), PL + 7, y + 6.8); y += 15;

      // Contenido del .md
      var ctx = { titulo: p.titulo, pagina: numPag };
      var yFinal = rmd(doc, p.md, y, ctx);
      numPag = ctx.pagina;
    }

    doc.save('Miku-Inn-Docs.pdf');

  } catch(err) {
    alert('Error al generar el PDF: ' + (err.message || err));
  } finally {
    btn.disabled = false; texto.textContent = 'Descargar PDF';
    spinner.style.display = 'none'; icono.style.display = 'inline';
  }
}