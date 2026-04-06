// Configuracion global de Docsify para Broom AirLine Docs.
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

        // Crea el hero header con icono SVG de avion, titulo y badges de tecnologia
        var hero = document.createElement('div');
        hero.className = 'hero-header';
        hero.innerHTML = `
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.95)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z"/>
          </svg>
          <h1>Broom AirLine Docs</h1>
          <p>Documentacion Tecnica del Backend &middot; C# + ASP.NET + Svelte</p>
          <div class="hero-badges">
            <span class="hero-badge">C#</span>
            <span class="hero-badge">ASP.NET</span>
            <span class="hero-badge">Svelte</span>
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