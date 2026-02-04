<script>
  import logo from '../assets/mikuinn-logo.png';
  
  const currentYear = new Date().getFullYear();
  
  const quickLinks = [
    { name: 'Inicio', href: '#/' },
    { name: 'Sobre Nosotros', href: '#/about' },
    { name: 'Hoteles', href: '#/search' },
    { name: 'Destinos', href: '#/destinations' },
    { name: 'Ofertas', href: '#/offers' }
  ];
  
  const services = [
    { name: 'Reservaciones', href: '#/reservations' },
    { name: 'Programa de Lealtad', href: '#/loyalty' },
    { name: 'Clientes Corporativos', href: '#/business' },
    { name: 'Grupos y Eventos', href: '#/groups' },
    { name: 'Gift Cards', href: '#/giftcards' }
  ];
  
  const support = [
    { name: 'Centro de Ayuda', href: '#/help' },
    { name: 'Contáctanos', href: '#/contact' },
    { name: 'Preguntas Frecuentes', href: '#/faq' },
    { name: 'Política de Cancelación', href: '#/cancellation' },
    { name: 'Términos y Condiciones', href: '#/terms' }
  ];
  
  const socialLinks = [
    {
      name: 'Facebook',
      href: 'https://facebook.com',
      icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>`
    },
    {
      name: 'Instagram',
      href: 'https://instagram.com',
      icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C8.74 0 8.333.015 7.053.072 5.775.132 4.905.333 4.14.63c-.789.306-1.459.717-2.126 1.384S.935 3.35.63 4.14C.333 4.905.131 5.775.072 7.053.012 8.333 0 8.74 0 12s.015 3.667.072 4.947c.06 1.277.261 2.148.558 2.913.306.788.717 1.459 1.384 2.126.667.666 1.336 1.079 2.126 1.384.766.296 1.636.499 2.913.558C8.333 23.988 8.74 24 12 24s3.667-.015 4.947-.072c1.277-.06 2.148-.262 2.913-.558.788-.306 1.459-.718 2.126-1.384.666-.667 1.079-1.335 1.384-2.126.296-.765.499-1.636.558-2.913.06-1.28.072-1.687.072-4.947s-.015-3.667-.072-4.947c-.06-1.277-.262-2.149-.558-2.913-.306-.789-.718-1.459-1.384-2.126C21.319 1.347 20.651.935 19.86.63c-.765-.297-1.636-.499-2.913-.558C15.667.012 15.26 0 12 0zm0 2.16c3.203 0 3.585.016 4.85.071 1.17.055 1.805.249 2.227.415.562.217.96.477 1.382.896.419.42.679.819.896 1.381.164.422.36 1.057.413 2.227.057 1.266.07 1.646.07 4.85s-.015 3.585-.074 4.85c-.061 1.17-.256 1.805-.421 2.227-.224.562-.479.96-.899 1.382-.419.419-.824.679-1.38.896-.42.164-1.065.36-2.235.413-1.274.057-1.649.07-4.859.07-3.211 0-3.586-.015-4.859-.074-1.171-.061-1.816-.256-2.236-.421-.569-.224-.96-.479-1.379-.899-.421-.419-.69-.824-.9-1.38-.165-.42-.359-1.065-.42-2.235-.045-1.26-.061-1.649-.061-4.844 0-3.196.016-3.586.061-4.861.061-1.17.255-1.814.42-2.234.21-.57.479-.96.9-1.381.419-.419.81-.689 1.379-.898.42-.166 1.051-.361 2.221-.421 1.275-.045 1.65-.06 4.859-.06l.045.03zm0 3.678c-3.405 0-6.162 2.76-6.162 6.162 0 3.405 2.76 6.162 6.162 6.162 3.405 0 6.162-2.76 6.162-6.162 0-3.405-2.76-6.162-6.162-6.162zM12 16c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4zm7.846-10.405c0 .795-.646 1.44-1.44 1.44-.795 0-1.44-.646-1.44-1.44 0-.794.646-1.439 1.44-1.439.793-.001 1.44.645 1.44 1.439z"/></svg>`
    },
    {
      name: 'Twitter',
      href: 'https://twitter.com',
      icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"/></svg>`
    },
    {
      name: 'LinkedIn',
      href: 'https://linkedin.com',
      icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/></svg>`
    }
  ];
</script>

<footer class="footer">
  <!-- Main Footer -->
  <div class="footer-main">
    <div class="footer-container">
      <!-- Company Info -->
      <div class="footer-section company-section">
        <div class="footer-logo">
          <img src={logo} alt="Miku Inn" class="footer-logo-image" />
        </div>
        <p class="company-description">
          Experiencias de lujo en los destinos más exclusivos del mundo. Más de 20 años de excelencia hotelera.
        </p>
        <div class="social-links">
          {#each socialLinks as social}
            <a 
              href={social.href} 
              class="social-link"
              target="_blank"
              rel="noopener noreferrer"
              aria-label={social.name}
            >
              {@html social.icon}
            </a>
          {/each}
        </div>
      </div>
      
      <!-- Quick Links -->
      <div class="footer-section">
        <h3 class="footer-title">Enlaces Rápidos</h3>
        <ul class="footer-links">
          {#each quickLinks as link}
            <li>
              <a href={link.href} class="footer-link">
                <svg class="link-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
                {link.name}
              </a>
            </li>
          {/each}
        </ul>
      </div>
      
      <!-- Services -->
      <div class="footer-section">
        <h3 class="footer-title">Servicios</h3>
        <ul class="footer-links">
          {#each services as link}
            <li>
              <a href={link.href} class="footer-link">
                <svg class="link-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
                {link.name}
              </a>
            </li>
          {/each}
        </ul>
      </div>
      
      <!-- Support -->
      <div class="footer-section">
        <h3 class="footer-title">Soporte</h3>
        <ul class="footer-links">
          {#each support as link}
            <li>
              <a href={link.href} class="footer-link">
                <svg class="link-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
                {link.name}
              </a>
            </li>
          {/each}
        </ul>
      </div>
      
      <!-- Contact Info -->
      <div class="footer-section contact-section">
        <h3 class="footer-title">Contáctanos</h3>
        
        <div class="contact-items">
          <div class="contact-item">
            <div class="contact-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
            </div>
            <div class="contact-details">
              <strong>Atención 24/7</strong>
              <p>+502 4276-8687</p>
            </div>
          </div>
          
          <div class="contact-item">
            <div class="contact-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                <polyline points="22,6 12,13 2,6"></polyline>
              </svg>
            </div>
            <div class="contact-details">
              <strong>Email</strong>
              <p>info@mikuinn.com</p>
            </div>
          </div>
          
          <div class="contact-item">
            <div class="contact-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
            </div>
            <div class="contact-details">
              <strong>Oficina Central</strong>
              <p>Guatemala City, Guatemala</p>
            </div>
          </div>
        </div>
        
        <!-- Newsletter -->
        <div class="newsletter">
          <h4 class="newsletter-title">Suscríbete a nuestro boletín</h4>
          <form class="newsletter-form">
            <input 
              type="email" 
              placeholder="Tu correo electrónico" 
              class="newsletter-input"
              required
            />
            <button type="submit" class="newsletter-button">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Bottom Bar -->
  <div class="footer-bottom">
    <div class="footer-container">
      <div class="footer-bottom-content">
        <p class="copyright">
          © {currentYear} <strong>Miku Inn</strong>. Todos los derechos reservados.
        </p>
        
        <div class="footer-legal">
          <a href="#/privacy" class="legal-link">Privacidad</a>
          <span class="separator">•</span>
          <a href="#/terms" class="legal-link">Términos</a>
          <span class="separator">•</span>
          <a href="#/cookies" class="legal-link">Cookies</a>
          <span class="separator">•</span>
          <a href="#/sitemap" class="legal-link">Mapa del Sitio</a>
        </div>
        
        <div class="payment-methods">
          <span class="payment-label">Aceptamos:</span>
          <div class="payment-icons">
            <div class="payment-icon">💳</div>
            <div class="payment-icon">🏦</div>
            <div class="payment-icon">💰</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</footer>

<style>
  /* ========== FOOTER PRINCIPAL ========== */
  .footer {
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
    color: #e2e8f0;
    margin-top: auto;
  }
  
  .footer-main {
    padding: 4rem 0 2rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .footer-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 2rem;
  }
  
  /* ========== GRID DE SECCIONES ========== */
  .footer-main .footer-container {
    display: grid;
    grid-template-columns: 1.5fr 1fr 1fr 1fr 1.5fr;
    gap: 3rem;
  }
  
  /* ========== COMPANY SECTION ========== */
  .company-section {
    padding-right: 2rem;
  }
  
  .footer-logo {
    margin-bottom: 1.5rem;
  }
  
  .footer-logo-image {
    height: 200px;
    width: auto;
    object-fit: contain;
    filter: brightness(0) invert(1);
  }
  
  .company-description {
    color: #cbd5e1;
    line-height: 1.7;
    margin-bottom: 2rem;
    font-size: 0.95rem;
  }
  
  /* ========== SOCIAL LINKS ========== */
  .social-links {
    display: flex;
    gap: 0.75rem;
  }
  
  .social-link {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    color: #e2e8f0;
    transition: all 0.3s ease;
  }
  
  .social-link:hover {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  .social-link svg {
    width: 20px;
    height: 20px;
  }
  
  /* ========== FOOTER SECTIONS ========== */
  .footer-section {
    display: flex;
    flex-direction: column;
  }
  
  .footer-title {
    color: #f8fafc;
    font-size: 1.125rem;
    font-weight: 700;
    margin-bottom: 1.5rem;
    letter-spacing: 0.02em;
  }
  
  .footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .footer-link {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #cbd5e1;
    text-decoration: none;
    font-size: 0.95rem;
    transition: all 0.2s ease;
    padding: 0.25rem 0;
  }
  
  .footer-link:hover {
    color: #667eea;
    padding-left: 0.5rem;
  }
  
  .link-icon {
    opacity: 0;
    transition: opacity 0.2s ease;
  }
  
  .footer-link:hover .link-icon {
    opacity: 1;
  }
  
  /* ========== CONTACT SECTION ========== */
  .contact-section {
    padding-left: 2rem;
  }
  
  .contact-items {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    margin-bottom: 2rem;
  }
  
  .contact-item {
    display: flex;
    align-items: start;
    gap: 1rem;
  }
  
  .contact-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(102, 126, 234, 0.15);
    border-radius: 10px;
    color: #667eea;
    flex-shrink: 0;
  }
  
  .contact-details strong {
    color: #f8fafc;
    font-size: 0.95rem;
    display: block;
    margin-bottom: 0.25rem;
  }
  
  .contact-details p {
    color: #cbd5e1;
    font-size: 0.9rem;
    margin: 0;
  }
  
  /* ========== NEWSLETTER ========== */
  .newsletter {
    margin-top: 2rem;
  }
  
  .newsletter-title {
    color: #f8fafc;
    font-size: 0.95rem;
    font-weight: 600;
    margin-bottom: 1rem;
  }
  
  .newsletter-form {
    display: flex;
    gap: 0.5rem;
  }
  
  .newsletter-input {
    flex: 1;
    padding: 0.75rem 1rem;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    color: white;
    font-size: 0.9rem;
    transition: all 0.2s ease;
  }
  
  .newsletter-input::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }
  
  .newsletter-input:focus {
    outline: none;
    background: rgba(255, 255, 255, 0.15);
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
  }
  
  .newsletter-button {
    padding: 0.75rem 1rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    color: white;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .newsletter-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  /* ========== FOOTER BOTTOM ========== */
  .footer-bottom {
    padding: 2rem 0;
    background: rgba(0, 0, 0, 0.2);
  }
  
  .footer-bottom-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 1.5rem;
  }
  
  .copyright {
    color: #94a3b8;
    font-size: 0.9rem;
    margin: 0;
  }
  
  .copyright strong {
    color: #e2e8f0;
  }
  
  .footer-legal {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .legal-link {
    color: #94a3b8;
    text-decoration: none;
    font-size: 0.9rem;
    transition: color 0.2s ease;
  }
  
  .legal-link:hover {
    color: #667eea;
  }
  
  .separator {
    color: rgba(148, 163, 184, 0.5);
  }
  
  .payment-methods {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .payment-label {
    color: #94a3b8;
    font-size: 0.85rem;
  }
  
  .payment-icons {
    display: flex;
    gap: 0.5rem;
  }
  
  .payment-icon {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 6px;
    font-size: 1rem;
  }
  
  /* ========== RESPONSIVE ========== */
  @media (max-width: 1200px) {
    .footer-main .footer-container {
      grid-template-columns: repeat(3, 1fr);
    }
    
    .company-section,
    .contact-section {
      grid-column: span 3;
      padding: 0;
    }
  }
  
  @media (max-width: 768px) {
    .footer-main {
      padding: 3rem 0 1.5rem;
    }
    
    .footer-main .footer-container {
      grid-template-columns: 1fr;
      gap: 2.5rem;
    }
    
    .footer-bottom-content {
      flex-direction: column;
      text-align: center;
    }
    
    .footer-legal {
      flex-wrap: wrap;
      justify-content: center;
    }
    
    .payment-methods {
      justify-content: center;
    }
  }
  
  /* ========== DARK MODE (ya está oscuro) ========== */
</style>