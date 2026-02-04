<script>
  import { createEventDispatcher } from 'svelte';
  
  const dispatch = createEventDispatcher();
  
  export let rooms = [];
  
  function addToCart(room) {
    dispatch('add-to-cart', room);
  }
</script>

<div class="room-selector">
  <h2>Selecciona tu Habitación</h2>
  
  <div class="rooms-grid">
    {#each rooms as room}
      <div class="room-card">
        <img src={room.image} alt={room.name} class="room-image" />
        
        <div class="room-info">
          <h3>{room.name}</h3>
          
          <div class="room-details">
            <div class="room-detail">
              <strong>Tamaño:</strong> {room.size} m²
            </div>
            <div class="room-detail">
              <strong>Huéspedes:</strong> Máx. {room.maxGuests}
            </div>
            <div class="room-detail">
              <strong>Cama:</strong> {room.bedType}
            </div>
          </div>
          
          <div class="room-features">
            {#each room.features as feature}
              <span class="feature-tag">{feature}</span>
            {/each}
          </div>
          
          <div class="room-footer">
            <div class="room-price">
              <span class="price">${room.price}</span>
              <span class="period">por noche</span>
            </div>
            
            <button class="add-to-cart-btn" on:click={() => addToCart(room)}>
              Agregar al Carrito
            </button>
          </div>
        </div>
      </div>
    {/each}
  </div>
</div>

<style>
  .room-selector {
    margin: 2rem 0;
  }
  
  h2 {
    color: #333;
    font-size: 1.8rem;
    margin-bottom: 2rem;
  }
  
  .rooms-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
  }
  
  .room-card {
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;
    background: white;
  }
  
  .room-card:hover {
    border-color: #667eea;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  }
  
  .room-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
  }
  
  .room-info {
    padding: 1.5rem;
  }
  
  .room-info h3 {
    color: #333;
    font-size: 1.3rem;
    margin-bottom: 1rem;
  }
  
  .room-details {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 1rem;
    color: #666;
  }
  
  .room-features {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
  }
  
  .feature-tag {
    background: #f0f2ff;
    color: #667eea;
    padding: 0.3rem 0.75rem;
    border-radius: 15px;
    font-size: 0.85rem;
  }
  
  .room-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 1rem;
    border-top: 1px solid #e0e0e0;
  }
  
  .room-price {
    display: flex;
    flex-direction: column;
  }
  
  .room-price .price {
    font-size: 1.8rem;
    font-weight: 700;
    color: #333;
  }
  
  .room-price .period {
    font-size: 0.9rem;
    color: #666;
  }
  
  .add-to-cart-btn {
    padding: 0.875rem 1.5rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .add-to-cart-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  @media (max-width: 768px) {
    .rooms-grid {
      grid-template-columns: 1fr;
    }
    
    .room-footer {
      flex-direction: column;
      gap: 1rem;
      align-items: stretch;
    }
    
    .add-to-cart-btn {
      width: 100%;
    }
  }
</style>