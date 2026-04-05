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
          <svg width="56" height="56" viewBox="0 0 24 24">
            <path
              d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"
              fill="rgba(255,255,255,0.95)"
            />
          </svg>
          <h1>Movent Docs</h1>
          <p>Documentación Técnica del Frontend &middot; Vue 3 + Vite</p>
          <div class="hero-badges">
            <span class="hero-badge">Vue 3</span>
            <span class="hero-badge">Vue Router</span>
            <span class="hero-badge">Vite</span>
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