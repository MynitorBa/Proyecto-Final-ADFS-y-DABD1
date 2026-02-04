<script>
  import { onMount } from 'svelte';
  
  // Datos de ejemplo del carrito
  let cartItems = [
    {
      hotelName: "Grand Miku Palace Paris",
      roomType: "Habitación Deluxe",
      checkIn: "2026-02-15",
      checkOut: "2026-02-18",
      nights: 3,
      pricePerNight: 280,
      guests: 2
    }
  ];
  
  let currentStep = 1;
  
  // Datos del formulario
  let cardNumber = '';
  let cardName = '';
  let expiryDate = '';
  let cvv = '';
  let billingAddress = '';
  let city = '';
  let country = '';
  let zipCode = '';
  
  let acceptTerms = false;
  
  $: totalPrice = cartItems.reduce((sum, item) => sum + (item.pricePerNight * item.nights), 0);
  $: taxes = totalPrice * 0.12; // 12% impuestos
  $: finalTotal = totalPrice + taxes;
  
  function formatCardNumber(value) {
    return value.replace(/\s/g, '').match(/.{1,4}/g)?.join(' ') || '';
  }
  
  function handleCardNumberInput(e) {
    let value = e.target.value.replace(/\s/g, '');
    if (value.length <= 16) {
      cardNumber = formatCardNumber(value);
    }
  }
  
  function handleExpiryInput(e) {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length >= 2) {
      value = value.slice(0, 2) + '/' + value.slice(2, 4);
    }
    expiryDate = value.slice(0, 5);
  }
  
  function handleCvvInput(e) {
    cvv = e.target.value.replace(/\D/g, '').slice(0, 3);
  }
  
  function nextStep() {
    if (currentStep < 3) {
      currentStep++;
    }
  }
  
  function prevStep() {
    if (currentStep > 1) {
      currentStep--;
    }
  }
  
  function processPayment() {
    if (!acceptTerms) {
      alert('Debes aceptar los términos y condiciones');
      return;
    }
    
    console.log('Procesando pago...', {
      cardNumber,
      cardName,
      expiryDate,
      cvv,
      billingAddress,
      city,
      country,
      zipCode,
      total: finalTotal
    });
    
    currentStep = 3;
  }
</script>

<div class="checkout-container">
  <div class="container">
    <!-- Progress Steps -->
    <div class="progress-steps">
      <div class="step" class:active={currentStep >= 1} class:completed={currentStep > 1}>
        <div class="step-number">1</div>
        <span>Resumen</span>
      </div>
      
      <div class="step-line" class:completed={currentStep > 1}></div>
      
      <div class="step" class:active={currentStep >= 2} class:completed={currentStep > 2}>
        <div class="step-number">2</div>
        <span>Pago</span>
      </div>
      
      <div class="step-line" class:completed={currentStep > 2}></div>
      
      <div class="step" class:active={currentStep >= 3}>
        <div class="step-number">3</div>
        <span>Confirmación</span>
      </div>
    </div>
    
    <div class="checkout-layout">
      <!-- Main Content -->
      <div class="checkout-main">
        {#if currentStep === 1}
          <!-- Step 1: Order Summary -->
          <div class="card">
            <h2>Resumen de tu Reserva</h2>
            
            {#each cartItems as item}
              <div class="reservation-item">
                <div class="item-header">
                  <h3>{item.hotelName}</h3>
                  <span class="room-type">{item.roomType}</span>
                </div>
                
                <div class="item-details">
                  <div class="detail-row">
                    <span class="label">📅 Check-in:</span>
                    <span class="value">{item.checkIn}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">📅 Check-out:</span>
                    <span class="value">{item.checkOut}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">🌙 Noches:</span>
                    <span class="value">{item.nights}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">👥 Huéspedes:</span>
                    <span class="value">{item.guests}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">💰 Precio por noche:</span>
                    <span class="value">${item.pricePerNight}</span>
                  </div>
                </div>
                
                <div class="item-total">
                  Subtotal: ${item.pricePerNight * item.nights}
                </div>
              </div>
            {/each}
            
            <div class="step-actions">
              <button class="btn-primary" on:click={nextStep}>
                Continuar al Pago
              </button>
            </div>
          </div>
        
        {:else if currentStep === 2}
          <!-- Step 2: Payment -->
          <div class="card">
            <h2>Información de Pago</h2>
            
            <form on:submit|preventDefault={processPayment}>
              <div class="form-section">
                <h3>Datos de la Tarjeta</h3>
                
                <div class="form-group">
                  <label for="cardNumber">Número de Tarjeta</label>
                  <input
                    type="text"
                    id="cardNumber"
                    value={cardNumber}
                    on:input={handleCardNumberInput}
                    placeholder="1234 5678 9012 3456"
                    required
                  />
                </div>
                
                <div class="form-group">
                  <label for="cardName">Nombre en la Tarjeta</label>
                  <input
                    type="text"
                    id="cardName"
                    bind:value={cardName}
                    placeholder="NOMBRE APELLIDO"
                    required
                  />
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label for="expiryDate">Fecha de Expiración</label>
                    <input
                      type="text"
                      id="expiryDate"
                      value={expiryDate}
                      on:input={handleExpiryInput}
                      placeholder="MM/AA"
                      required
                    />
                  </div>
                  
                  <div class="form-group">
                    <label for="cvv">CVV</label>
                    <input
                      type="text"
                      id="cvv"
                      value={cvv}
                      on:input={handleCvvInput}
                      placeholder="123"
                      required
                    />
                  </div>
                </div>
              </div>
              
              <div class="form-section">
                <h3>Dirección de Facturación</h3>
                
                <div class="form-group">
                  <label for="billingAddress">Dirección</label>
                  <input
                    type="text"
                    id="billingAddress"
                    bind:value={billingAddress}
                    placeholder="Calle Principal 123"
                    required
                  />
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label for="city">Ciudad</label>
                    <input
                      type="text"
                      id="city"
                      bind:value={city}
                      placeholder="Ciudad"
                      required
                    />
                  </div>
                  
                  <div class="form-group">
                    <label for="zipCode">Código Postal</label>
                    <input
                      type="text"
                      id="zipCode"
                      bind:value={zipCode}
                      placeholder="01010"
                      required
                    />
                  </div>
                </div>
                
                <div class="form-group">
                  <label for="country">País</label>
                  <select id="country" bind:value={country} required>
                    <option value="">Selecciona un país</option>
                    <option value="GT">Guatemala</option>
                    <option value="MX">México</option>
                    <option value="US">Estados Unidos</option>
                    <option value="ES">España</option>
                    <option value="FR">Francia</option>
                  </select>
                </div>
              </div>
              
              <div class="terms-section">
                <label class="checkbox-label">
                  <input type="checkbox" bind:checked={acceptTerms} />
                  Acepto los términos y condiciones de Miku Inn
                </label>
              </div>
              
              <div class="step-actions">
                <button type="button" class="btn-secondary" on:click={prevStep}>
                  Volver
                </button>
                <button type="submit" class="btn-primary">
                  Confirmar y Pagar ${finalTotal.toFixed(2)}
                </button>
              </div>
            </form>
          </div>
        
        {:else}
          <!-- Step 3: Confirmation -->
          <div class="card confirmation-card">
            <div class="success-icon">✅</div>
            <h2>¡Reserva Confirmada!</h2>
            
            <p class="confirmation-message">
              Tu reserva ha sido procesada exitosamente. Recibirás un correo electrónico
              con todos los detalles de tu reserva y el código de confirmación.
            </p>
            
            <div class="confirmation-code">
              <strong>Código de Confirmación:</strong>
              <span class="code">MIKU-{Date.now().toString().slice(-8)}</span>
            </div>
            
            <div class="confirmation-details">
              <h3>Detalles de tu Reserva</h3>
              {#each cartItems as item}
                <div class="detail-item">
                  <strong>{item.hotelName}</strong>
                  <p>{item.roomType}</p>
                  <p>Check-in: {item.checkIn}</p>
                  <p>Check-out: {item.checkOut}</p>
                  <p>Total: ${(item.pricePerNight * item.nights).toFixed(2)}</p>
                </div>
              {/each}
            </div>
            
            <div class="step-actions">
              <button class="btn-primary" on:click={() => window.location.href = '#/'}>
                Volver al Inicio
              </button>
              <button class="btn-secondary">
                Descargar Comprobante
              </button>
            </div>
          </div>
        {/if}
      </div>
      
      <!-- Sidebar with Order Summary -->
      <aside class="checkout-sidebar">
        <div class="card">
          <h3>Resumen del Pedido</h3>
          
          <div class="summary-items">
            {#each cartItems as item}
              <div class="summary-item">
                <span class="item-name">{item.roomType}</span>
                <span class="item-price">${item.pricePerNight * item.nights}</span>
              </div>
            {/each}
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row">
            <span>Subtotal</span>
            <span>${totalPrice.toFixed(2)}</span>
          </div>
          
          <div class="summary-row">
            <span>Impuestos (12%)</span>
            <span>${taxes.toFixed(2)}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-total">
            <span>Total</span>
            <span>${finalTotal.toFixed(2)}</span>
          </div>
          
          <div class="security-badge">
            <span>🔒</span>
            <span>Pago seguro y encriptado</span>
          </div>
        </div>
      </aside>
    </div>
  </div>
</div>

<style>
  .checkout-container {
    min-height: 100vh;
    background: #f8f9fa;
    padding: 3rem 0;
  }
  
  .container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 2rem;
  }
  
  .progress-steps {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 3rem;
  }
  
  .step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
  }
  
  .step-number {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: #e0e0e0;
    color: #999;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.2rem;
    transition: all 0.3s ease;
  }
  
  .step.active .step-number {
    background: #667eea;
    color: white;
  }
  
  .step.completed .step-number {
    background: #4caf50;
    color: white;
  }
  
  .step span {
    color: #999;
    font-weight: 600;
  }
  
  .step.active span {
    color: #333;
  }
  
  .step-line {
    width: 100px;
    height: 3px;
    background: #e0e0e0;
    transition: all 0.3s ease;
  }
  
  .step-line.completed {
    background: #4caf50;
  }
  
  .checkout-layout {
    display: grid;
    grid-template-columns: 1fr 400px;
    gap: 2rem;
    align-items: start;
  }
  
  .card {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
  
  .card h2 {
    color: #333;
    font-size: 1.8rem;
    margin-bottom: 2rem;
  }
  
  .card h3 {
    color: #333;
    font-size: 1.3rem;
    margin-bottom: 1rem;
  }
  
  .reservation-item {
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: start;
    margin-bottom: 1rem;
  }
  
  .item-header h3 {
    color: #333;
    font-size: 1.3rem;
    margin: 0;
  }
  
  .room-type {
    background: #f0f2ff;
    color: #667eea;
    padding: 0.4rem 0.875rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
  }
  
  .item-details {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-bottom: 1rem;
  }
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    color: #555;
  }
  
  .detail-row .label {
    font-weight: 600;
  }
  
  .item-total {
    padding-top: 1rem;
    border-top: 2px solid #e0e0e0;
    text-align: right;
    font-size: 1.2rem;
    font-weight: 700;
    color: #333;
  }
  
  .form-section {
    margin-bottom: 2rem;
    padding-bottom: 2rem;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .form-group {
    margin-bottom: 1.5rem;
  }
  
  .form-group label {
    display: block;
    font-weight: 600;
    color: #555;
    margin-bottom: 0.5rem;
  }
  
  .form-group input,
  .form-group select {
    width: 100%;
    padding: 0.875rem;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
    transition: all 0.3s ease;
  }
  
  .form-group input:focus,
  .form-group select:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
  
  .terms-section {
    margin-bottom: 2rem;
  }
  
  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    cursor: pointer;
    color: #555;
  }
  
  .checkbox-label input[type="checkbox"] {
    width: 20px;
    height: 20px;
    cursor: pointer;
  }
  
  .step-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    margin-top: 2rem;
  }
  
  .btn-primary,
  .btn-secondary {
    padding: 1rem 2rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    font-size: 1.05rem;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
  
  .btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  .btn-secondary {
    background: white;
    color: #667eea;
    border: 2px solid #667eea;
  }
  
  .btn-secondary:hover {
    background: #f0f2ff;
  }
  
  .confirmation-card {
    text-align: center;
  }
  
  .success-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
  }
  
  .confirmation-message {
    color: #555;
    line-height: 1.6;
    margin-bottom: 2rem;
  }
  
  .confirmation-code {
    background: #f0f2ff;
    padding: 1.5rem;
    border-radius: 12px;
    margin-bottom: 2rem;
  }
  
  .confirmation-code strong {
    display: block;
    color: #333;
    margin-bottom: 0.5rem;
  }
  
  .code {
    font-size: 1.5rem;
    font-weight: 700;
    color: #667eea;
    font-family: monospace;
  }
  
  .confirmation-details {
    text-align: left;
    margin-bottom: 2rem;
  }
  
  .detail-item {
    padding: 1rem;
    background: #f8f9fa;
    border-radius: 8px;
    margin-top: 1rem;
  }
  
  .detail-item strong {
    color: #333;
    font-size: 1.1rem;
  }
  
  .detail-item p {
    color: #666;
    margin: 0.25rem 0;
  }
  
  .checkout-sidebar .card h3 {
    font-size: 1.3rem;
    margin-bottom: 1.5rem;
  }
  
  .summary-items {
    margin-bottom: 1.5rem;
  }
  
  .summary-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    color: #555;
  }
  
  .summary-divider {
    height: 1px;
    background: #e0e0e0;
    margin: 1.5rem 0;
  }
  
  .summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.75rem;
    color: #555;
  }
  
  .summary-total {
    display: flex;
    justify-content: space-between;
    font-size: 1.5rem;
    font-weight: 700;
    color: #333;
  }
  
  .security-badge {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    margin-top: 2rem;
    padding: 1rem;
    background: #f8f9fa;
    border-radius: 8px;
    color: #555;
    font-size: 0.9rem;
  }
  
  @media (max-width: 1024px) {
    .checkout-layout {
      grid-template-columns: 1fr;
    }
    
    .checkout-sidebar {
      order: -1;
    }
  }
  
  @media (max-width: 768px) {
    .progress-steps {
      font-size: 0.85rem;
    }
    
    .step-number {
      width: 40px;
      height: 40px;
      font-size: 1rem;
    }
    
    .step-line {
      width: 50px;
    }
    
    .step-actions {
      flex-direction: column;
    }
    
    .form-row {
      grid-template-columns: 1fr;
    }
  }
</style>