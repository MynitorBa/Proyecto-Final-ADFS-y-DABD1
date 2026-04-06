// Configuracion global de Docsify para Miku Inn Docs.
// Define el sidebar, busqueda, paginacion y el hero header inyectado por plugin.
window.$docsify = {
  name: '',
  repo: '',

  // Carga el sidebar desde _sidebar.md y no expande subniveles automaticamente
  loadSidebar: true,
  subMaxLevel: 0,
  coverpage: false,

  // Sube al tope de la pagina al navegar entre secciones
  auto2top: true,

  // Configuracion del plugin de busqueda
  search: {
    placeholder: 'Buscar...',
    noData: 'Sin resultados'
  },

  plugins: [
    function(hook) {
      // Se ejecuta cada vez que Docsify termina de renderizar una pagina
      hook.doneEach(function() {

        // Elimina el hero previo para evitar duplicados al navegar
        var existing = document.querySelector('.hero-header');
        if (existing) existing.remove();

        // Crea el hero header con icono SVG de casa, titulo y badges de tecnologia
        var hero = document.createElement('div');
        hero.className = 'hero-header';
        hero.innerHTML = `
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          <h1>Miku Inn Docs</h1>
          <p>Documentacion Tecnica del Backend &middot; Java + Javalin</p>
          <div class="hero-badges">
            <span class="hero-badge">Java</span>
            <span class="hero-badge">Javalin</span>
            <span class="hero-badge">Oracle</span>
            <span class="hero-badge">REST API</span>
            <span class="hero-badge">UNIS 2026</span>
          </div>
        `;

        // Inserta el hero al inicio del area de contenido principal
        var content = document.querySelector('.content');
        if (content) content.prepend(hero);
      });
    }
  ]
};