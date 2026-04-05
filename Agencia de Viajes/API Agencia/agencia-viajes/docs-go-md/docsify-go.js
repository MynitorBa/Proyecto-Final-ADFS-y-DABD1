// Configuración global de Docsify con sidebar, búsqueda y plugin del hero
window.$docsify = {
  name: '',
  repo: '',

  // Carga el sidebar desde _sidebar.md sin subniveles para mantenerlo limpio
  loadSidebar: true,
  subMaxLevel: 0,

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

        // Construye el hero header con ícono de servidor, título y badges del stack
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
          <p>Documentación Técnica del Backend &middot; Go + Gin</p>
          <div class="hero-badges">
            <span class="hero-badge">Go</span>
            <span class="hero-badge">Gin</span>
            <span class="hero-badge">MariaDB</span>
            <span class="hero-badge">REST API</span>
            <span class="hero-badge">UNIS 2026</span>
          </div>
        `;

        // Inserta el hero al inicio del área de contenido principal
        var content = document.querySelector('.content');
        if (content) content.prepend(hero);
      });
    }
  ]
};