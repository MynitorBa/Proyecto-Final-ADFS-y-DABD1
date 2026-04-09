// Configuracion global de Docsify con sidebar, busqueda y plugin del hero
window.$docsify = {
  name: '',
  repo: '',

  // Carga el sidebar desde _sidebar.md con hasta 2 niveles de anidacion
  loadSidebar: true,
  subMaxLevel: 2,

  // Sin coverpage, sube al top automaticamente al navegar
  coverpage: false,
  auto2top: true,

  // Textos del plugin de busqueda en espanol
  search: {
    placeholder: 'Buscar...',
    noData: 'Sin resultados'
  },

  plugins: [
    function(hook) {

      // Se ejecuta despues de que cada pagina termina de renderizarse
      hook.doneEach(function() {

        // Elimina el hero previo si existe para evitar duplicados al navegar
        var existing = document.querySelector('.hero-header');
        if (existing) existing.remove();

        // Construye el hero header con icono, titulo, subtitulo y badges
        var hero = document.createElement('div');
        hero.className = 'hero-header';
        hero.innerHTML = `
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="1.5">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.15 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.06 1h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.09 8.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 21 16z"/>
          </svg>
          <h1>AirLine Broom Docs</h1>
          <p>Documentacion Tecnica del Frontend &middot; Svelte + Vite</p>
          <div class="hero-badges">
            <span class="hero-badge">Svelte</span>
            <span class="hero-badge">Vite</span>
            <span class="hero-badge">JavaScript</span>
            <span class="hero-badge">DABD1 2026</span>
          </div>
        `;

        // Inserta el hero al inicio del area de contenido principal
        var content = document.querySelector('.content');
        if (content) content.prepend(hero);

        // Añade el boton flotante de descarga PDF una sola vez
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
            <span class="pdf-btn-text">Descargar PDF</span>
          `;
          btn.onclick = exportarPDF;
          document.body.appendChild(btn);
        }
      });
    }
  ]
};

// ─── Layout A4 ───────────────────────────────────────────────────────────────
var PL  = 14;   // margen izquierdo
var PR  = 196;  // margen derecho
var PW  = 182;  // ancho util
var PT  = 24;   // primera linea de contenido (bajo el header de pagina)
var PB  = 276;  // limite inferior (sobre el footer)
var PH  = 297;  // alto total A4
var PW2 = 210;  // ancho total A4

// ─── Paleta de colores AirLine Broom ─────────────────────────────────────────
var C = {
  dark:   [17,  24,  39 ],   // #111827
  mid:    [30,  58, 138 ],   // #1e3a8a
  blue:   [29,  78, 216 ],   // #1d4ed8
  accent: [96, 165, 250 ],   // #60a5fa
  white:  [255,255, 255 ],
  text:   [30,  41,  59 ],
  muted:  [100,116, 139 ],
  border: [219,234, 254 ],   // #dbeafe
  bglt:   [239,246, 255 ],   // #eff6ff
  codebg: [17,  24,  39 ],
  codefg: [226,232, 240 ],
  h2:     [30,  58, 138 ],
  h3:     [29,  78, 216 ]
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
 * Comprueba si la posicion Y supera el limite inferior y agrega pagina si es necesario.
 * @param {object} doc - Instancia de jsPDF.
 * @param {number} y - Posicion Y actual.
 * @param {number} [need=0] - Espacio requerido para el proximo elemento.
 * @param {object} ctx - Contexto con titulo de pagina y numero para el header.
 * @returns {number} Posicion Y actualizada.
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
 * Escribe un bloque de texto con salto automatico de pagina.
 * @param {object} doc - Instancia de jsPDF.
 * @param {string[]} lines - Lineas ya divididas por splitTextToSize.
 * @param {number} x - Posicion X.
 * @param {number} y - Posicion Y inicial.
 * @param {number} lead - Interlineado en mm.
 * @param {object} ctx - Contexto para el header de pagina.
 * @returns {number} Posicion Y final.
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
 * Dibuja el header y footer de cada pagina interior del PDF.
 * Header: banda azul oscura con nombre del sistema y titulo de la seccion.
 * Footer: numero de pagina centrado con linea separadora en color de marca.
 * @param {object} doc - Instancia de jsPDF.
 * @param {string} titulo - Titulo de la pagina actual para el header.
 * @param {number} num - Numero de pagina para el footer.
 */
function dibujarHeader(doc, titulo, num) {
  // Banda superior: dos capas simulando gradiente oscuro
  fc(doc, C.dark); doc.rect(0, 0, PW2, 14, 'F');
  fc(doc, C.mid);  doc.rect(0, 10, PW2, 4, 'F');
  fc(doc, C.accent); doc.rect(0, 13.5, PW2, 0.8, 'F');

  // Nombre del sistema a la izquierda
  tc(doc, C.accent);
  doc.setFontSize(7.5); doc.setFont('helvetica', 'bold');
  doc.text('AIRLINE BROOM DOCS', PL, 9);

  // Titulo de la seccion a la derecha
  tc(doc, [200, 220, 255]);
  doc.setFontSize(7); doc.setFont('helvetica', 'normal');
  var tit = titulo.length > 55 ? titulo.slice(0, 52) + '...' : titulo;
  doc.text(sanitizar(tit), PR, 9, { align: 'right' });

  // Footer con linea y numero de pagina centrado
  dc(doc, C.border); doc.setLineWidth(0.3);
  doc.line(PL, PH - 10, PR, PH - 10);
  tc(doc, C.muted);
  doc.setFontSize(7.5); doc.setFont('helvetica', 'normal');
  doc.text('Pag. ' + num, PW2 / 2, PH - 6, { align: 'center' });
}

/**
 * Sanitiza una cadena para compatibilidad con Helvetica/WinAnsiEncoding en jsPDF.
 * Reemplaza caracteres fuera de Latin-1 por equivalentes ASCII visibles para
 * evitar cuadros o glifos corruptos en el PDF resultante.
 * @param {string} str - Cadena a sanitizar.
 * @returns {string}
 */
function sanitizar(str) {
  if (!str) return '';
  return String(str)
    .replace(/\u2192|\u21D2|\u27A1/g, '->')
    .replace(/\u2190|\u21D0/g, '<-')
    .replace(/\u2194/g, '<->').replace(/\u2191/g, '^').replace(/\u2193/g, 'v')
    .replace(/[\u201C\u201D\u00AB\u00BB]/g, '"')
    .replace(/[\u2018\u2019]/g, "'")
    .replace(/[\u2014\u2013]/g, '-')
    .replace(/\u2026/g, '...')
    .replace(/\u2022/g, '*')
    .replace(/\u00B7/g, '.')
    .replace(/\u00D7/g, 'x')
    .replace(/\u2260/g, '!=').replace(/\u2264/g, '<=').replace(/\u2265/g, '>=')
    .replace(/[^\x00-\xFF]/g, '?');
}

/**
 * Limpia la sintaxis Markdown de una linea devolviendo texto plano sanitizado.
 * Elimina negritas, cursivas, codigo inline, links, encabezados, listas y blockquotes.
 * @param {string} line - Linea de markdown.
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
 * Renderiza el contenido Markdown de una pagina directamente en jsPDF.
 * Maneja h1-h4, bloques de codigo, blockquotes, listas, separadores, tablas
 * y parrafos con deteccion de patrones de documentacion (Type, Returns).
 * @param {object} doc - Instancia de jsPDF.
 * @param {string} md - Contenido markdown de la pagina.
 * @param {number} startY - Posicion Y donde comienza el contenido.
 * @param {object} ctx - Contexto con titulo y pagina para el header.
 * @returns {number} Posicion Y final tras renderizar todo el contenido.
 */
function rmd(doc, md, startY, ctx) {
  var y = startY; var lines = md.split('\n'); var i = 0;

  while (i < lines.length) {
    var raw = lines[i]; var tri = raw.trim(); i++;

    if (!tri) { y += 2.5; continue; }

    // Omitir comentarios HTML generados por documentation.js
    if (/^<!--/.test(tri)) continue;

    // Omitir definiciones de referencia markdown: [1]: #anchor o [1]: https://...
    if (/^\[\d+\]:\s/.test(tri)) continue;

    // H1 con linea divisoria completa
    if (/^#\s/.test(tri)) {
      y = chy(doc, y, 12, ctx);
      doc.setFontSize(15); doc.setFont('helvetica', 'bold'); tc(doc, C.dark);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 8, ctx);
      dc(doc, C.border); doc.setLineWidth(0.5);
      doc.line(PL, y, PL + PW, y); y += 5;
      continue;
    }

    // H2 con linea parcial en azul de marca
    if (/^##\s/.test(tri)) {
      y = chy(doc, y, 10, ctx);
      doc.setFontSize(12); doc.setFont('helvetica', 'bold'); tc(doc, C.h2);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 7, ctx);
      dc(doc, C.border); doc.setLineWidth(0.3);
      doc.line(PL, y, PL + PW * 0.5, y); y += 4;
      continue;
    }

    // H3 en accent azul claro
    if (/^###\s/.test(tri)) {
      y = chy(doc, y, 8, ctx);
      doc.setFontSize(10.5); doc.setFont('helvetica', 'bold'); tc(doc, C.h3);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 6, ctx); y += 1;
      continue;
    }

    // H4 en azul oscuro
    if (/^####\s/.test(tri)) {
      y = chy(doc, y, 7, ctx);
      doc.setFontSize(9.5); doc.setFont('helvetica', 'bold'); tc(doc, C.blue);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 5.5, ctx); y += 0.5;
      continue;
    }

    // Bloque de codigo con fondo oscuro de marca
    if (tri.startsWith('```')) {
      var clines = [];
      while (i < lines.length && !lines[i].trim().startsWith('```')) { clines.push(lines[i]); i++; }
      i++;
      if (!clines.length) continue;
      var vis = clines.slice(0, 25);
      var bh  = vis.length * 4.2 + 7;
      y = chy(doc, y, bh, ctx);
      fc(doc, C.codebg);
      doc.roundedRect(PL, y - 2, PW, bh, 2, 2, 'F');
      tc(doc, C.accent); doc.setFontSize(6.5); doc.setFont('helvetica', 'bold');
      doc.text('CODIGO', PL + 3, y + 2);
      var cy = y + 5;
      tc(doc, C.codefg); doc.setFontSize(7.2); doc.setFont('courier', 'normal');
      for (var ci = 0; ci < vis.length; ci++) {
        var cs = doc.splitTextToSize(sanitizar(vis[ci] || ' '), PW - 6);
        for (var cj = 0; cj < cs.length; cj++) {
          cy = chy(doc, cy, 0, ctx);
          doc.text(cs[cj], PL + 3, cy); cy += 4.2;
        }
      }
      y = cy + 3; continue;
    }

    // Blockquote con fondo azul muy claro y barra accent
    if (tri.startsWith('> ')) {
      var txt = lmd(tri.slice(2));
      var sp  = doc.splitTextToSize(txt, PW - 10);
      var bh  = sp.length * 5.2 + 5;
      y = chy(doc, y, bh, ctx);
      fc(doc, C.bglt); doc.rect(PL, y - 3, PW, bh, 'F');
      fc(doc, C.accent); doc.rect(PL, y - 3, 2, bh, 'F');
      tc(doc, [30, 50, 100]); doc.setFontSize(9); doc.setFont('helvetica', 'italic');
      y = wr(doc, sp, PL + 5, y + 1.5, 5.2, ctx); y += 2; continue;
    }

    // Separador horizontal
    if (/^-{3,}$/.test(tri) || /^={3,}$/.test(tri)) {
      y = chy(doc, y, 4, ctx);
      dc(doc, C.border); doc.setLineWidth(0.3);
      doc.line(PL, y, PR, y); y += 4; continue;
    }

    // Lista de vinetas con guion en accent azul
    if (/^[-*+]\s/.test(tri)) {
      var txt = lmd(tri);
      var sp  = doc.splitTextToSize(txt, PW - 9);
      y = chy(doc, y, sp.length * 5, ctx);
      tc(doc, C.accent); doc.setFontSize(9); doc.setFont('helvetica', 'normal');
      doc.text('-', PL + 2, y);
      tc(doc, C.text);
      y = wr(doc, sp, PL + 7, y, 5, ctx); continue;
    }

    // Lista numerada con numero en azul de marca
    if (/^\d+\.\s/.test(tri)) {
      var num  = tri.match(/^(\d+)\./)[1];
      var txt  = lmd(tri);
      var sp   = doc.splitTextToSize(txt, PW - 9);
      y = chy(doc, y, sp.length * 5, ctx);
      tc(doc, C.blue); doc.setFontSize(8.5); doc.setFont('helvetica', 'bold');
      doc.text(num + '.', PL + 2, y);
      tc(doc, C.text); doc.setFont('helvetica', 'normal');
      y = wr(doc, sp, PL + 8, y, 5, ctx); continue;
    }

    // Tabla: omite lineas de separador guiones
    if (tri.startsWith('|')) {
      if (/^\|[\s\-:|]+\|/.test(tri)) continue;
      var cells = tri.split('|').filter(function(c){return c.trim();}).map(function(c){return c.trim();});
      var row   = cells.join('   ');
      var sp    = doc.splitTextToSize(row, PW);
      y = chy(doc, y, sp.length * 4.8, ctx);
      fc(doc, C.bglt); doc.rect(PL, y - 3, PW, sp.length * 4.8 + 1, 'F');
      tc(doc, C.text); doc.setFontSize(8); doc.setFont('helvetica', 'normal');
      y = wr(doc, sp, PL + 2, y, 4.8, ctx); y += 0.5; continue;
    }

    // Parrafo normal con deteccion de patrones de documentacion
    var txt = lmd(tri);
    if (!txt) continue;

    // Etiqueta TYPE en muted con valor en courier azul
    if (/^type:/i.test(txt)) {
      y = chy(doc, y, 7, ctx);
      tc(doc, C.muted); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
      doc.text('TYPE', PL, y);
      tc(doc, C.blue); doc.setFont('courier', 'normal');
      var sp = doc.splitTextToSize(txt.replace(/^type:\s*/i, ''), PW - 16);
      y = wr(doc, sp, PL + 16, y, 5, ctx); y += 1; continue;
    }

    // Etiqueta RETURNS en muted con valor en courier azul
    if (/^returns?\s/i.test(txt)) {
      y = chy(doc, y, 7, ctx);
      tc(doc, C.muted); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
      doc.text('RETURNS', PL, y);
      tc(doc, C.blue); doc.setFont('courier', 'normal');
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
 * Descarga un archivo .md con deteccion automatica de encoding.
 * PowerShell genera archivos UTF-16 LE con BOM cuando usa el operador >
 * para redirigir salida. fetch().text() asume UTF-8, por lo que los
 * archivos UTF-16 LE producen null bytes entre cada caracter en el string.
 * Esta funcion detecta el BOM y usa TextDecoder adecuado.
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
  // Heuristica sin BOM: si el segundo byte es nulo probablemente es UTF-16 LE
  if (bytes.length > 10 && bytes[1] === 0x00) {
    return new TextDecoder('utf-16le').decode(buf);
  }
  // Por defecto UTF-8
  return new TextDecoder('utf-8').decode(buf);
}

/**
 * Genera y descarga un PDF con toda la documentacion ordenada segun el sidebar.
 * Parsea _sidebar.md para obtener el orden, descarga cada .md con fetchMd()
 * y usa jsPDF directamente (sin html2canvas) para maxima compatibilidad.
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
    // Leer el sidebar con deteccion de encoding para obtener el orden exacto
    var sbMd = await fetchMd('_sidebar.md');
    if (!sbMd) throw new Error('No se pudo cargar _sidebar.md');
    var secAct = 'General'; var paginas = [];

    sbMd.split('\n').forEach(function(l) {
      var ms = l.match(/\*\*([^*]+)\*\*/);
      var ml = l.match(/\[([^\]]+)\]\(([^)]+)\)/);
      if (ms) secAct = ms[1];
      if (ml) paginas.push({ titulo: ml[1], archivo: ml[2], seccion: secAct });
    });

    // Descargar todos los .md en paralelo con deteccion automatica de encoding.
    // El script PowerShell genera UTF-16 LE, fetchMd detecta el BOM y decodifica.
    var contenidos = await Promise.all(paginas.map(async function(p) {
      try {
        var md = await fetchMd(p.archivo + '.md');
        if (md === null) return null;
        return { titulo: p.titulo, seccion: p.seccion, md: md };
      } catch(_) { return null; }
    }));
    var validos = contenidos.filter(Boolean);
    if (!validos.length) throw new Error('No se pudo cargar ninguna pagina .md');

    var jsPDF  = window.jspdf.jsPDF;
    var doc    = new jsPDF('p', 'mm', 'a4');
    var numPag = 0;

    // ── Portada ───────────────────────────────────────────────────────────────
    fc(doc, C.dark); doc.rect(0, 0, PW2, PH, 'F');
    // Franja central en azul medio
    fc(doc, C.mid);    doc.rect(0, 105, PW2, 70, 'F');
    fc(doc, C.accent); doc.rect(0, 103, PW2, 3.5, 'F');
    fc(doc, C.accent); doc.rect(0, 174, PW2, 1.5, 'F');
    // Puntos decorativos en tono azul suave
    fc(doc, [25, 45, 100]);
    for (var di = 0; di < 8; di++) doc.circle(PL + di * 24, 95, 1.5, 'F');
    // Textos de portada
    tc(doc, C.white); doc.setFontSize(28); doc.setFont('helvetica', 'bold');
    doc.text('AirLine Broom', PW2 / 2, 120, { align: 'center' });
    doc.setFontSize(14); doc.setFont('helvetica', 'normal');
    doc.text('Documentacion Tecnica del Frontend', PW2 / 2, 133, { align: 'center' });
    tc(doc, C.accent); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
    doc.text('Svelte + Vite  |  DABD1 2026', PW2 / 2, 148, { align: 'center' });
    tc(doc, [180, 210, 255]); doc.setFontSize(8.5); doc.setFont('helvetica', 'normal');
    doc.text('Generado el ' + new Date().toLocaleDateString('es-GT', { year: 'numeric', month: 'long', day: 'numeric' }), PW2 / 2, 162, { align: 'center' });

    // ── Indice ────────────────────────────────────────────────────────────────
    doc.addPage(); numPag++;
    dibujarHeader(doc, 'Indice de contenidos', numPag);
    var iy = PT;
    tc(doc, C.dark); doc.setFontSize(13); doc.setFont('helvetica', 'bold');
    doc.text('Indice de contenidos', PL, iy); iy += 8;
    dc(doc, C.border); doc.setLineWidth(0.5);
    doc.line(PL, iy, PR, iy); iy += 5;

    var secAnterior = null;
    for (var vi = 0; vi < validos.length; vi++) {
      var vp = validos[vi];
      if (vp.seccion !== secAnterior) {
        secAnterior = vp.seccion;
        iy = chy(doc, iy, 8, { titulo: 'Indice', pagina: numPag });
        tc(doc, C.blue); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
        doc.text(sanitizar(vp.seccion.toUpperCase()), PL, iy); iy += 5.5;
      }
      iy = chy(doc, iy, 5, { titulo: 'Indice', pagina: numPag });
      tc(doc, C.muted); doc.setFontSize(7.5); doc.setFont('helvetica', 'normal');
      doc.text('-', PL + 4, iy);
      tc(doc, C.text);
      doc.text(sanitizar(vp.titulo), PL + 8, iy); iy += 5;
    }

    // ── Contenido: una pagina por cada .md ────────────────────────────────────
    var secPrev = null;
    for (var pi = 0; pi < validos.length; pi++) {
      var p = validos[pi];
      doc.addPage(); numPag++;
      dibujarHeader(doc, p.titulo, numPag);
      var y = PT;

      // Banda de seccion cuando cambia el grupo del sidebar
      if (p.seccion !== secPrev) {
        secPrev = p.seccion;
        fc(doc, C.bglt); doc.rect(PL, y - 4, PW, 8, 'F');
        fc(doc, C.accent); doc.rect(PL, y - 4, 2, 8, 'F');
        tc(doc, C.blue); doc.setFontSize(7); doc.setFont('helvetica', 'bold');
        doc.text(sanitizar(p.seccion.toUpperCase()), PL + 5, y + 1);
        y += 10;
      }

      // Barra de titulo de la pagina en azul de marca
      fc(doc, C.mid); doc.roundedRect(PL, y, PW, 10, 2, 2, 'F');
      fc(doc, C.accent); doc.roundedRect(PL, y, 3, 10, 1, 1, 'F');
      tc(doc, C.white); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
      doc.text(sanitizar(p.titulo), PL + 7, y + 6.8); y += 15;

      // Renderizar el contenido del .md
      var ctx = { titulo: p.titulo, pagina: numPag };
      rmd(doc, p.md, y, ctx);
      numPag = ctx.pagina;
    }

    doc.save('AirLine-Broom-Docs.pdf');

  } catch(err) {
    alert('Error al generar el PDF: ' + (err.message || err));
  } finally {
    btn.disabled = false; texto.textContent = 'Descargar PDF';
    spinner.style.display = 'none'; icono.style.display = 'inline';
  }
}