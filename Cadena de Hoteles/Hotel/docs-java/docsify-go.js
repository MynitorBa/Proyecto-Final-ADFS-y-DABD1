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

        var content = document.querySelector('.content');
        if (content) content.prepend(hero);
      });
    }
  ]
};