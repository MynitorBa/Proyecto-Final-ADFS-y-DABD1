// Configuracion global de Docsify para Broom AirLine Docs.
window.$docsify = {
  name: '',
  repo: '',

  loadSidebar: true,
  subMaxLevel: 0,
  coverpage: false,
  auto2top: true,

  search: {
    placeholder: 'Buscar...',
    noData: 'Sin resultados'
  },

  plugins: [
    function(hook) {
      hook.doneEach(function() {

        var existing = document.querySelector('.hero-header');
        if (existing) existing.remove();

        var hero = document.createElement('div');
        hero.className = 'hero-header';
        hero.innerHTML = `
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z"/>
          </svg>
          <h1>Broom AirLine Docs</h1>
          <p>Documentacion Tecnica del Backend &middot; C# + ASP.NET</p>
          <div class="hero-badges">
            <span class="hero-badge">C#</span>
            <span class="hero-badge">ASP.NET</span>
            <span class="hero-badge">Oracle</span>
            <span class="hero-badge">REST API</span>
            <span class="hero-badge">UNIS 2026</span>
          </div>
        `;

        var content = document.querySelector('.content');
        if (content) content.prepend(hero);

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
var PL  = 14;
var PR  = 196;
var PW  = 182;
var PT  = 24;
var PB  = 276;
var PH  = 297;
var PW2 = 210;

// ─── Paleta Broom AirLine ─────────────────────────────────────────────────────
var C = {
  dark:   [26,  26,  46 ],
  mid:    [22,  33,  62 ],
  blue:   [15,  52,  96 ],
  accent: [74, 158, 255 ],
  white:  [255,255, 255 ],
  text:   [30,  41,  59 ],
  muted:  [100,116, 139 ],
  border: [232,236, 243 ],
  bglt:   [238,246, 255 ],
  codebg: [17,  24,  39 ],
  codefg: [226,232, 240 ],
  h2:     [15,  52,  96 ],
  h3:     [74, 158, 255 ]
};

function tc(doc, rgb) { doc.setTextColor(rgb[0], rgb[1], rgb[2]); }
function fc(doc, rgb) { doc.setFillColor(rgb[0], rgb[1], rgb[2]); }
function dc(doc, rgb) { doc.setDrawColor(rgb[0], rgb[1], rgb[2]); }

function chy(doc, y, need, ctx) {
  if (y + (need || 0) > PB) {
    doc.addPage();
    ctx.pagina++;
    dibujarHeader(doc, ctx.titulo, ctx.pagina);
    return PT;
  }
  return y;
}

function wr(doc, lines, x, y, lead, ctx) {
  for (var i = 0; i < lines.length; i++) {
    y = chy(doc, y, 0, ctx);
    doc.text(lines[i], x, y);
    y += lead;
  }
  return y;
}

function dibujarHeader(doc, titulo, num) {
  fc(doc, C.dark); doc.rect(0, 0, PW2, 14, 'F');
  fc(doc, C.mid);  doc.rect(0, 10, PW2, 4, 'F');
  fc(doc, C.accent); doc.rect(0, 13.5, PW2, 0.8, 'F');

  tc(doc, C.accent);
  doc.setFontSize(7.5); doc.setFont('helvetica', 'bold');
  doc.text('BROOM AIRLINE DOCS', PL, 9);

  tc(doc, [200, 225, 255]);
  doc.setFontSize(7); doc.setFont('helvetica', 'normal');
  var tit = titulo.length > 55 ? titulo.slice(0, 52) + '...' : titulo;
  doc.text(sanitizar(tit), PR, 9, { align: 'right' });

  dc(doc, C.border); doc.setLineWidth(0.3);
  doc.line(PL, PH - 10, PR, PH - 10);
  tc(doc, C.muted);
  doc.setFontSize(7.5); doc.setFont('helvetica', 'normal');
  doc.text('Pag. ' + num, PW2 / 2, PH - 6, { align: 'center' });
}

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

function lmd(line) {
  return sanitizar(line
    .replace(/\*\*([^*]+)\*\*/g, '$1').replace(/\*([^*]+)\*/g, '$1')
    .replace(/__([^_]+)__/g, '$1').replace(/_([^_]+)_/g, '$1')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/^#{1,6}\s+/, '').replace(/^[-*+]\s+/, '')
    .replace(/^\d+\.\s+/, '').replace(/^>\s+/, '').trim());
}

function rmd(doc, md, startY, ctx) {
  var y = startY; var lines = md.split('\n'); var i = 0;

  while (i < lines.length) {
    var raw = lines[i]; var tri = raw.trim(); i++;

    if (!tri) { y += 2.5; continue; }

    if (/^<!--/.test(tri)) continue;
    if (/^\[\d+\]:\s/.test(tri)) continue;

    // H1: ignorado — el titulo ya aparece en el recuadro azul de la seccion
    if (/^#\s/.test(tri)) continue;

    // H2: nombre de clase — bloque con fondo azul claro y barra accent izquierda
    if (/^##\s/.test(tri)) {
      var h2txt = lmd(tri);
      if (!h2txt) continue;
      y = chy(doc, y, 14, ctx);
      fc(doc, C.bglt); doc.rect(PL, y - 4, PW, 10, 'F');
      fc(doc, C.accent); doc.rect(PL, y - 4, 3, 10, 'F');
      tc(doc, C.blue); doc.setFontSize(10); doc.setFont('helvetica', 'bold');
      doc.text(sanitizar(h2txt), PL + 7, y + 2);
      y += 10;
      continue;
    }

    // H3
    if (/^###\s/.test(tri)) {
      y = chy(doc, y, 8, ctx);
      doc.setFontSize(10.5); doc.setFont('helvetica', 'bold'); tc(doc, C.h3);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 6, ctx); y += 1;
      continue;
    }

    // H4
    if (/^####\s/.test(tri)) {
      y = chy(doc, y, 7, ctx);
      doc.setFontSize(9.5); doc.setFont('helvetica', 'bold'); tc(doc, C.blue);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 5.5, ctx); y += 0.5;
      continue;
    }

    // Bloque de codigo
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

    // Blockquote
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

    // Lista de vinetas
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
      tc(doc, C.blue); doc.setFontSize(8.5); doc.setFont('helvetica', 'bold');
      doc.text(num + '.', PL + 2, y);
      tc(doc, C.text); doc.setFont('helvetica', 'normal');
      y = wr(doc, sp, PL + 8, y, 5, ctx); continue;
    }

    // Tabla
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

    if (/^type:/i.test(txt)) {
      y = chy(doc, y, 7, ctx);
      tc(doc, C.muted); doc.setFontSize(8); doc.setFont('helvetica', 'bold');
      doc.text('TYPE', PL, y);
      tc(doc, C.blue); doc.setFont('courier', 'normal');
      var sp = doc.splitTextToSize(txt.replace(/^type:\s*/i, ''), PW - 16);
      y = wr(doc, sp, PL + 16, y, 5, ctx); y += 1; continue;
    }

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

async function fetchMd(url) {
  var res = await fetch(url);
  if (!res.ok) return null;
  var buf   = await res.arrayBuffer();
  var bytes = new Uint8Array(buf);
  if (bytes[0] === 0xFF && bytes[1] === 0xFE) return new TextDecoder('utf-16le').decode(buf);
  if (bytes[0] === 0xFE && bytes[1] === 0xFF) return new TextDecoder('utf-16be').decode(buf);
  if (bytes[0] === 0xEF && bytes[1] === 0xBB && bytes[2] === 0xBF) return new TextDecoder('utf-8').decode(buf);
  if (bytes.length > 10 && bytes[1] === 0x00) return new TextDecoder('utf-16le').decode(buf);
  return new TextDecoder('utf-8').decode(buf);
}

async function exportarPDF() {
  var btn     = document.querySelector('.pdf-download-btn');
  var texto   = btn.querySelector('.pdf-btn-text');
  var spinner = btn.querySelector('.pdf-btn-spinner');
  var icono   = btn.querySelector('.pdf-btn-icon');

  btn.disabled = true; texto.textContent = 'Generando...';
  spinner.style.display = 'inline-block'; icono.style.display = 'none';

  try {
    var sbMd = await fetchMd('_sidebar.md');
    if (!sbMd) throw new Error('No se pudo cargar _sidebar.md');
    var secAct = 'General'; var paginas = [];

    sbMd.split('\n').forEach(function(l) {
      var ms = l.match(/\*\*([^*]+)\*\*/);
      var ml = l.match(/\[([^\]]+)\]\(([^)]+)\)/);
      if (ms) secAct = ms[1];
      if (ml) paginas.push({ titulo: ml[1], archivo: ml[2], seccion: secAct });
    });

    var contenidos = await Promise.all(paginas.map(async function(p) {
      try {
        var md = await fetchMd(p.archivo + '.md');
        if (md === null) return null;
        return { titulo: p.titulo, seccion: p.seccion, md: md };
      } catch(_) { return null; }
    }));

    var validos = contenidos.filter(function(p) { return p != null; });
    if (!validos.length) throw new Error('No se pudo cargar ninguna pagina .md');

    var jsPDF  = window.jspdf.jsPDF;
    var doc    = new jsPDF('p', 'mm', 'a4');
    var numPag = 0;

    // ── Portada ───────────────────────────────────────────────────────────────
    fc(doc, C.dark); doc.rect(0, 0, PW2, PH, 'F');
    fc(doc, C.mid);    doc.rect(0, 105, PW2, 70, 'F');
    fc(doc, C.accent); doc.rect(0, 103, PW2, 3.5, 'F');
    fc(doc, C.accent); doc.rect(0, 174, PW2, 1.5, 'F');
    fc(doc, [30, 50, 80]);
    for (var di = 0; di < 8; di++) doc.circle(PL + di * 24, 95, 1.5, 'F');
    tc(doc, C.white); doc.setFontSize(28); doc.setFont('helvetica', 'bold');
    doc.text('Broom AirLine', PW2 / 2, 120, { align: 'center' });
    doc.setFontSize(14); doc.setFont('helvetica', 'normal');
    doc.text('Documentacion Tecnica del Backend', PW2 / 2, 133, { align: 'center' });
    tc(doc, C.accent); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
    doc.text('C# + ASP.NET  |  UNIS 2026', PW2 / 2, 148, { align: 'center' });
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

    // ── Contenido ─────────────────────────────────────────────────────────────
    var secPrev = null;
    for (var pi = 0; pi < validos.length; pi++) {
      var p = validos[pi];
      doc.addPage(); numPag++;
      dibujarHeader(doc, p.titulo, numPag);
      var y = PT;

      if (p.seccion !== secPrev) {
        secPrev = p.seccion;
        fc(doc, C.bglt); doc.rect(PL, y - 4, PW, 8, 'F');
        fc(doc, C.accent); doc.rect(PL, y - 4, 2, 8, 'F');
        tc(doc, C.blue); doc.setFontSize(7); doc.setFont('helvetica', 'bold');
        doc.text(sanitizar(p.seccion.toUpperCase()), PL + 5, y + 1);
        y += 10;
      }

      fc(doc, C.mid); doc.roundedRect(PL, y, PW, 10, 2, 2, 'F');
      fc(doc, C.accent); doc.roundedRect(PL, y, 3, 10, 1, 1, 'F');
      tc(doc, C.white); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
      doc.text(sanitizar(p.titulo), PL + 7, y + 6.8); y += 15;

      var ctx = { titulo: p.titulo, pagina: numPag };
      rmd(doc, p.md, y, ctx);
      numPag = ctx.pagina;
    }

    doc.save('Broom-AirLine-Docs.pdf');

  } catch(err) {
    alert('Error al generar el PDF: ' + (err.message || err));
  } finally {
    btn.disabled = false; texto.textContent = 'Descargar PDF';
    spinner.style.display = 'none'; icono.style.display = 'inline';
  }
}