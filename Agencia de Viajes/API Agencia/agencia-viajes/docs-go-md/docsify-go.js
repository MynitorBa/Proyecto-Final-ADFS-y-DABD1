// Configuracion global de Docsify con sidebar, busqueda y plugin del hero
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
            <rect x="2" y="3" width="20" height="14" rx="2"/>
            <path d="M8 21h8M12 17v4"/>
            <circle cx="12" cy="10" r="3"/>
            <path d="M12 7v1M12 12v1M9 10H8M16 10h-1"/>
          </svg>
          <h1>Movent API Docs</h1>
          <p>Documentacion Tecnica del Backend &middot; Go + Gin</p>
          <div class="hero-badges">
            <span class="hero-badge">Go</span>
            <span class="hero-badge">Gin</span>
            <span class="hero-badge">MariaDB</span>
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

// ─── Paleta base ─────────────────────────────────────────────────────────────
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
  codebg: [13,  20,  38 ],
  codefg: [200,220, 245 ],
  h2:     [15,  52,  96 ],
  h3:     [74, 158, 255 ]
};

// ─── Colores por tipo de declaracion Go ──────────────────────────────────────
var CK = {
  func:    { stripe: [74,  158, 255], text: [120, 200, 255] },
  type:    { stripe: [160, 100, 255], text: [195, 155, 255] },
  const:   { stripe: [60,  210, 140], text: [100, 240, 175] },
  vari:    { stripe: [255, 170,  55], text: [255, 200, 100] },
  comment: { text:   [85,  120, 165] },
  normal:  { text:   [200, 220, 245] }
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
  doc.text('MOVENT API DOCS', PL, 9);

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

/**
 * Detecta el tipo de declaracion Go de una linea.
 * @param {string} line
 * @returns {'func'|'type'|'const'|'vari'|'comment'|'normal'}
 */
function tipoLinea(line) {
  var t = line.trim();
  if (/^\/\//.test(t))         return 'comment';
  if (/^func[\s(]/.test(t))   return 'func';
  if (/^type\s/.test(t))      return 'type';
  if (/^const[\s(]/.test(t))  return 'const';
  if (/^var[\s(]/.test(t))    return 'vari';
  return 'normal';
}

/**
 * Renderiza el contenido Markdown de una pagina directamente en jsPDF.
 *
 * REGLAS DE RENDERIZADO:
 *  - H1 (#): IGNORADO — el titulo ya esta en el recuadro azul de la seccion.
 *  - H2 (##): renderizado como etiqueta pequeña y sutil (no titulo prominente).
 *    Evita que "FUNCTIONS", "TYPES", "CONSTANTS" etc. parezcan titulos de seccion.
 *  - H3/H4: titulos secundarios normales.
 *  - Code blocks: la barra accent de apertura se dibuja JUNTO con el primer grupo
 *    (chy calcula espacio para ambos), evitando la linea azul huerfana al fondo de pagina.
 */
function rmd(doc, md, startY, ctx) {
  var y = startY;
  var lines = md.split('\n');
  var i = 0;
  var lastWasEmpty = false;

  while (i < lines.length) {
    var raw = lines[i]; var tri = raw.trim(); i++;

    // Colapsar lineas en blanco consecutivas en un unico gap de 3 mm
    if (!tri) {
      if (!lastWasEmpty) y += 3;
      lastWasEmpty = true;
      continue;
    }
    lastWasEmpty = false;

    if (/^<!--/.test(tri)) continue;
    if (/^\[\d+\]:\s/.test(tri)) continue;

    // ── H1: IGNORADO ────────────────────────────────────────────────────────
    // El titulo de la pagina ya se muestra en el recuadro azul de exportarPDF.
    // Renderizarlo aqui lo duplicaria visualmente.
    if (/^#\s/.test(tri)) {
      continue;
    }

    // ── H2: etiqueta sutil de subseccion ────────────────────────────────────
    // Los godoc generan ## FUNCTIONS, ## TYPES, ## CONSTANTS, ## VARIABLES.
    // En lugar de un titulo prominente (que se ve como seccion duplicada y
    // puede quedar huerfano), se renderiza como una pequeña etiqueta muted
    // con una linea fina a la derecha, similar a un divider.
    if (/^##\s/.test(tri)) {
      var h2txt = lmd(tri);
      if (!h2txt) continue;              // ignorar ## vacios
      y = chy(doc, y, 10, ctx);
      // Etiqueta pequeña en color muted/accent apagado
      tc(doc, C.muted);
      doc.setFontSize(6.5); doc.setFont('helvetica', 'bold');
      var labelW = doc.getTextWidth(h2txt) + 2;
      doc.text(h2txt, PL, y);
      // Linea fina a la derecha del label, en el borde color
      dc(doc, C.border); doc.setLineWidth(0.25);
      doc.line(PL + labelW + 1, y - 1, PR, y - 1);
      y += 5;
      continue;
    }

    // H3
    if (/^###\s/.test(tri)) {
      y = chy(doc, y, 10, ctx);
      doc.setFontSize(10.5); doc.setFont('helvetica', 'bold'); tc(doc, C.h3);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 6, ctx); y += 1;
      continue;
    }

    // H4
    if (/^####\s/.test(tri)) {
      y = chy(doc, y, 9, ctx);
      doc.setFontSize(9.5); doc.setFont('helvetica', 'bold'); tc(doc, C.blue);
      var sp = doc.splitTextToSize(lmd(tri), PW);
      y = wr(doc, sp, PL, y, 5.5, ctx); y += 0.5;
      continue;
    }

    // ── Bloque de codigo ────────────────────────────────────────────────────
    if (tri.startsWith('```')) {
      var clines = [];
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        clines.push(lines[i]); i++;
      }
      i++;
      if (!clines.length) continue;

      var esDeclaracion = function(l) {
        var t = l.trim();
        return /^(func|type|const|var|\/\/)[\s(]/.test(t) && t.length > 0;
      };

      // Agrupar lineas por declaracion para no cortar grupos entre paginas
      var grupos = [];
      var grupoActual = [];
      for (var ci = 0; ci < clines.length; ci++) {
        if (esDeclaracion(clines[ci]) && grupoActual.length > 0) {
          grupos.push(grupoActual);
          grupoActual = [];
        }
        grupoActual.push(clines[ci]);
      }
      if (grupoActual.length > 0) grupos.push(grupoActual);

      // ── FIX: calcular espacio necesario antes de dibujar el header ──────
      // Si el primer grupo no cabe junto con el header, saltamos ANTES de
      // dibujar la barra accent, evitando que quede huerfana sola en la pagina.
      var primerGrupoLineas = grupos.length > 0 ? grupos[0].length : 0;
      // Cap en 35 mm para no forzar salto innecesario en grupos muy grandes
      var espacioArranque = Math.min(primerGrupoLineas * 4.8 + 8, 35);
      y = chy(doc, y, espacioArranque, ctx);

      // Franja de apertura: linea accent solida en el tope del bloque
      fc(doc, C.codebg); doc.rect(PL, y - 2, PW, 4, 'F');
      fc(doc, C.accent); doc.rect(PL, y - 2, PW, 0.8, 'F');
      y += 3;

      for (var gi = 0; gi < grupos.length; gi++) {
        var grupo = grupos[gi];
        var alturaGrupo = grupo.length * 4.8 + (gi > 0 ? 4 : 0);

        // Salto de pagina si el grupo completo no cabe (solo si es razonable)
        if (y + alturaGrupo > PB && alturaGrupo < (PB - PT)) {
          doc.addPage(); ctx.pagina++;
          dibujarHeader(doc, ctx.titulo, ctx.pagina); y = PT;
        }

        // Separador fino entre grupos
        if (gi > 0) {
          if (y + 4 > PB) {
            doc.addPage(); ctx.pagina++;
            dibujarHeader(doc, ctx.titulo, ctx.pagina); y = PT;
          }
          fc(doc, C.codebg); doc.rect(PL, y - 1, PW, 5, 'F');
          dc(doc, [38, 62, 98]); doc.setLineWidth(0.15);
          doc.line(PL + 3, y + 1.5, PL + PW - 3, y + 1.5);
          y += 4;
        }

        for (var li = 0; li < grupo.length; li++) {
          var cl    = grupo[li];
          var tipo  = tipoLinea(cl);
          var esDec = esDeclaracion(cl) && li === 0;
          var cs    = doc.splitTextToSize(sanitizar(cl || ' '), PW - 10);

          for (var cj = 0; cj < cs.length; cj++) {
            if (y + 4.5 > PB) {
              doc.addPage(); ctx.pagina++;
              dibujarHeader(doc, ctx.titulo, ctx.pagina); y = PT;
            }

            // Fondo uniforme oscuro
            fc(doc, C.codebg);
            doc.rect(PL, y - 3.5, PW, 4.8, 'F');

            // Barra solida izquierda solo en la primera linea de cada declaracion
            if (esDec && cj === 0 && tipo !== 'normal' && tipo !== 'comment') {
              var pal = CK[tipo] || CK.func;
              fc(doc, pal.stripe);
              doc.rect(PL, y - 3.5, 2.5, 4.8, 'F');
            }

            // Color y estilo de texto segun tipo
            if (tipo === 'comment') {
              tc(doc, CK.comment.text);
              doc.setFontSize(7); doc.setFont('courier', 'italic');
            } else if (esDec && cj === 0 && tipo !== 'normal') {
              var pal = CK[tipo] || CK.func;
              tc(doc, pal.text);
              doc.setFontSize(7.5); doc.setFont('courier', 'bold');
            } else {
              tc(doc, CK.normal.text);
              doc.setFontSize(7.2); doc.setFont('courier', 'normal');
            }

            var xOff = (esDec && cj === 0 && tipo !== 'normal' && tipo !== 'comment')
                         ? PL + 5.5
                         : PL + 4;
            doc.text(cs[cj], xOff, y);
            y += 4.5;
          }
        }
      }

      // Franja de cierre (solo si no estamos justo al tope de una pagina nueva)
      if (y > PT + 6) {
        fc(doc, C.codebg); doc.rect(PL, y - 1, PW, 3.5, 'F');
        fc(doc, [38, 62, 98]); doc.rect(PL, y + 2.2, PW, 0.5, 'F');
      }
      y += 4;
      continue;
    }

    // Blockquote — inline para que rect y texto nunca queden en paginas distintas
    if (tri.startsWith('> ')) {
      var txt = lmd(tri.slice(2));
      var sp  = doc.splitTextToSize(txt, PW - 10);
      var bh  = sp.length * 5.2 + 6;
      y = chy(doc, y, Math.min(bh + 3, PB - PT - 4), ctx);
      fc(doc, C.bglt); doc.rect(PL, y - 3, PW, bh, 'F');
      fc(doc, C.accent); doc.rect(PL, y - 3, 2, bh, 'F');
      tc(doc, [45, 55, 80]); doc.setFontSize(9); doc.setFont('helvetica', 'italic');
      var by = y + 1.5;
      for (var bi = 0; bi < sp.length; bi++) {
        doc.text(sp[bi], PL + 5, by);
        by += 5.2;
      }
      y = by + 1.5;
      continue;
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
      y = chy(doc, y, Math.min(sp.length * 5, 18), ctx);
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
      y = chy(doc, y, Math.min(sp.length * 5, 18), ctx);
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

    // Parrafo normal
    var txt = lmd(tri);
    if (!txt) continue;
    doc.setFontSize(9); doc.setFont('helvetica', 'normal'); tc(doc, C.text);
    var sp = doc.splitTextToSize(txt, PW);
    y = chy(doc, y, Math.min(sp.length * 5.2, 20), ctx);
    y = wr(doc, sp, PL, y, 5.2, ctx);
  }
  return y;
}

/**
 * Descarga un archivo .md con deteccion automatica de encoding.
 * Detecta BOM UTF-16/UTF-8 y usa TextDecoder adecuado.
 */
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

/**
 * Genera y descarga el PDF con toda la documentacion ordenada segun el sidebar.
 */
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
    var validos = contenidos.filter(Boolean);
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
    doc.text('Movent API', PW2 / 2, 120, { align: 'center' });
    doc.setFontSize(14); doc.setFont('helvetica', 'normal');
    doc.text('Documentacion Tecnica del Backend', PW2 / 2, 133, { align: 'center' });
    tc(doc, C.accent); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
    doc.text('Go + Gin  |  UNIS 2026', PW2 / 2, 148, { align: 'center' });
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

      fc(doc, C.blue); doc.roundedRect(PL, y, PW, 10, 2, 2, 'F');
      fc(doc, C.accent); doc.roundedRect(PL, y, 3, 10, 1, 1, 'F');
      tc(doc, C.white); doc.setFontSize(11); doc.setFont('helvetica', 'bold');
      doc.text(sanitizar(p.titulo), PL + 7, y + 6.8); y += 15;

      var ctx = { titulo: p.titulo, pagina: numPag };
      rmd(doc, p.md, y, ctx);
      numPag = ctx.pagina;
    }

    doc.save('Movent-API-Docs.pdf');

  } catch(err) {
    alert('Error al generar el PDF: ' + (err.message || err));
  } finally {
    btn.disabled = false; texto.textContent = 'Descargar PDF';
    spinner.style.display = 'none'; icono.style.display = 'inline';
  }
}