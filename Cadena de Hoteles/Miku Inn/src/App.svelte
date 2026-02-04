<script>
  // Components
  import Header from './components/Header.svelte';
  import Footer from './components/Footer.svelte';
  import LoadingSpinner from './components/LoadingSpinner.svelte';
  
  // Pages
  import Home from './pages/Home.svelte';
  import SearchResults from './pages/SearchResults.svelte';
  import HotelDetail from './pages/HotelDetail.svelte';
  import Checkout from './pages/Checkout.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import MyReservations from './pages/MyReservations.svelte';
  import Destinos from './pages/Destinos.svelte';
  import DestinoDetail from './pages/DestinoDetail.svelte';
  import Ofertas from './pages/Ofertas.svelte';
  
  // Styles
  import './styles/variables.css';
  import './styles/global.css';
  import './styles/forms.css';
  import './styles/animations.css';
  import './app.css';
  
  // Estado de la aplicación
  let isLoading = true;
  let currentPage = 'home';
  let isLoggedIn = false;
  let userName = '';
  let cartItemsCount = 0;
  
  // Datos de navegación
  let currentHotelId = null;
  let currentDestinoId = null;
  let searchParams = null;
  let destinationSuggestion = null;
  
  // Simular carga inicial
  setTimeout(() => {
    isLoading = false;
  }, 1500);
  
  // Función principal de navegación
  function navigateTo(page, data = null) {
    currentPage = page;
    
    // Manejar datos específicos según la página
    if (page === 'hotel-detail') {
      currentHotelId = data;
    }
    
    if (page === 'destino-detail') {
      currentDestinoId = data;
    }
    
    if (page === 'search-results') {
      searchParams = data;
    }
    
    // Scroll al inicio
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  
  // Manejar sugerencias de destino
  function handleDestinationSuggestion(destination) {
    destinationSuggestion = destination;
    
    if (currentPage !== 'home') {
      navigateTo('home');
    }
    
    setTimeout(() => {
      const searchSection = document.querySelector('.search-section');
      if (searchSection) {
        searchSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 100);
  }
  
  // Manejar búsqueda desde header
  function handleHeaderSearch(event) {
    const query = event.detail;
    console.log('Buscando:', query);
    navigateTo('search-results', { query });
  }
  
  // Manejar logout
  function handleLogout() {
    isLoggedIn = false;
    userName = '';
    cartItemsCount = 0;
    console.log('Sesión cerrada');
    navigateTo('home');
  }
  
  // Determinar si mostrar header/footer
  $: showHeaderFooter = currentPage !== 'login' && currentPage !== 'register';
  
  // Para testing - Descomenta para simular usuario logueado
  // isLoggedIn = true;
  // userName = 'Juan Pérez';
  // cartItemsCount = 2;
</script>

<div class="app-wrapper">
  {#if isLoading}
    <div class="loading-screen">
      <LoadingSpinner 
        fullscreen={true}
        size="large"
        text="Preparando tu estancia perfecta..."
      />
    </div>
  {/if}
  
  {#if !isLoading}
    {#if showHeaderFooter}
      <Header 
        {navigateTo}
        {currentPage}
        {isLoggedIn}
        {userName}
        {cartItemsCount}
        on:search={handleHeaderSearch}
        on:logout={handleLogout}
      />
    {/if}
    
    <main class="app-main">
      {#if currentPage === 'home'}
        <Home {navigateTo} {destinationSuggestion} />
      {:else if currentPage === 'search-results'}
        <SearchResults {navigateTo} {searchParams} />
      {:else if currentPage === 'hotel-detail'}
        <HotelDetail {navigateTo} hotelId={currentHotelId} />
      {:else if currentPage === 'checkout'}
        <Checkout {navigateTo} />
      {:else if currentPage === 'login'}
        <Login {navigateTo} />
      {:else if currentPage === 'register'}
        <Register {navigateTo} />
      {:else if currentPage === 'reservations'}
        <MyReservations {navigateTo} />
      {:else if currentPage === 'destinations'}
        <Destinos {navigateTo} />
      {:else if currentPage === 'destino-detail'}
        <DestinoDetail {navigateTo} destinoId={currentDestinoId} />
      {:else if currentPage === 'offers'}
        <Ofertas {navigateTo} />
      {:else if currentPage === 'about'}
        <div class="page-placeholder">
          <div class="placeholder-content">
            <h1>ℹ️ Sobre Nosotros</h1>
            <p>Página sobre nosotros en construcción</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {:else if currentPage === 'contact'}
        <div class="page-placeholder">
          <div class="placeholder-content">
            <h1>📧 Contáctanos</h1>
            <p>Página de contacto en construcción</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {:else if currentPage === 'help'}
        <div class="page-placeholder">
          <div class="placeholder-content">
            <h1>❓ Centro de Ayuda</h1>
            <p>Centro de ayuda en construcción</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {:else if currentPage === 'profile'}
        <div class="page-placeholder">
          <div class="placeholder-content">
            <h1>👤 Mi Perfil</h1>
            <p>Página de perfil en construcción</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {:else if currentPage === 'favorites'}
        <div class="page-placeholder">
          <div class="placeholder-content">
            <h1>❤️ Favoritos</h1>
            <p>Página de favoritos en construcción</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {:else if currentPage === 'settings'}
        <div class="page-placeholder">
          <div class="placeholder-content">
            <h1>⚙️ Configuración</h1>
            <p>Página de configuración en construcción</p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {:else}
        <!-- Página 404 -->
        <div class="not-found-page">
          <div class="not-found-content">
            <div class="not-found-icon">
              <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
            </div>
            <h1 class="not-found-title">404</h1>
            <h2 class="not-found-subtitle">Página no encontrada</h2>
            <p class="not-found-text">
              Lo sentimos, la página que buscas no existe o ha sido movida.
            </p>
            <button class="btn-primary" on:click={() => navigateTo('home')}>
              Volver al Inicio
            </button>
          </div>
        </div>
      {/if}
    </main>
    
    {#if showHeaderFooter}
      <Footer />
    {/if}
  {/if}
</div>

<style>
  /* Contenedor principal de la app */
  .app-wrapper {
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }
  
  /* Pantalla de carga */
  .loading-screen {
    width: 100%;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  /* Área de contenido principal */
  .app-main {
    flex: 1;
    width: 100%;
    background: var(--color-bg, #f8fafc);
  }
  
  /* ========== PÁGINA PLACEHOLDER ========== */
  .page-placeholder {
    min-height: 70vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    background: var(--color-bg, #f8fafc);
  }
  
  .placeholder-content {
    text-align: center;
    max-width: 600px;
    background: white;
    padding: 3rem 2rem;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  
  .placeholder-content h1 {
    font-size: 3rem;
    margin-bottom: 1rem;
    color: #1e293b;
  }
  
  .placeholder-content p {
    font-size: 1.125rem;
    color: #64748b;
    margin-bottom: 2rem;
  }
  
  /* ========== PÁGINA 404 ========== */
  .not-found-page {
    min-height: 70vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    background: var(--color-bg, #f8fafc);
  }
  
  .not-found-content {
    text-align: center;
    max-width: 600px;
    animation: fadeInUp 0.6s ease-out;
  }
  
  .not-found-icon {
    color: #667eea;
    margin-bottom: 1rem;
    display: flex;
    justify-content: center;
  }
  
  .not-found-title {
    font-size: 8rem;
    font-weight: 800;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    line-height: 1;
    margin: 0 0 1rem 0;
  }
  
  .not-found-subtitle {
    font-size: 2rem;
    color: #1e293b;
    margin: 0 0 1rem 0;
  }
  
  .not-found-text {
    font-size: 1.1rem;
    color: #64748b;
    margin: 0 0 2rem 0;
    line-height: 1.6;
  }
  
  .btn-primary {
    padding: 1rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    font-size: 1.05rem;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
  
  .btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  }
  
  /* Animación */
  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  /* ========== RESPONSIVE ========== */
  @media (max-width: 768px) {
    .not-found-title {
      font-size: 5rem;
    }
    
    .not-found-subtitle {
      font-size: 1.5rem;
    }
    
    .not-found-text {
      font-size: 1rem;
    }
    
    .placeholder-content {
      padding: 2rem 1.5rem;
    }
    
    .placeholder-content h1 {
      font-size: 2rem;
    }
  }
  
  /* ========== DARK MODE ========== */
  @media (prefers-color-scheme: dark) {
    .app-main,
    .page-placeholder,
    .not-found-page {
      background: #0f172a;
    }
    
    .placeholder-content {
      background: #1e293b;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    
    .placeholder-content h1,
    .not-found-subtitle {
      color: #f8fafc;
    }
    
    .placeholder-content p,
    .not-found-text {
      color: #cbd5e1;
    }
  }
</style>