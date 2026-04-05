// Configuración global de Docsify con sidebar, búsqueda y plugin del hero
window.$docsify = {
  name: '',
  repo: '',

  // Carga el sidebar desde _sidebar.md con hasta 2 niveles de anidación
  loadSidebar: true,
  subMaxLevel: 2,

  // Sin coverpage, sube al top automáticamente al navegar
  coverpage: false,
  auto2top: true,

  // Textos del plugin de búsqueda en español
  search: {
    placeholder: 'Buscar...',
    noData: 'Sin resultados'
  },

  plugins: [
    function(hook) {

      // Se ejecuta después de que cada página termina de renderizarse
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
          </div>
        `;

        // Inserta el hero al inicio del área de contenido principal
        var content = document.querySelector('.content');
        if (content) content.prepend(hero);
      });
    }
  ]
};