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
      });
    }
  ]
};