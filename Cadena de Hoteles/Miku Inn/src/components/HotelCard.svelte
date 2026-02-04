<script>
  import { createEventDispatcher } from 'svelte';
  
  const dispatch = createEventDispatcher();
  
  export let hotel = {
    id: 1,
    name: '',
    city: '',
    country: '',
    rating: 0,
    reviews: 0,
    price: 0,
    image: '',
    roomType: '',
    amenities: []
  };
  
  function handleClick() {
    dispatch('select', hotel.id);
  }
  
  function renderStars(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    let stars = '⭐'.repeat(fullStars);
    if (hasHalfStar) stars += '⭐';
    return stars;
  }
</script>

<div class="hotel-card" on:click={handleClick}>
  <div class="hotel-image">
    <img src={hotel.image} alt={hotel.name} />
    <div class="hotel-badge">{hotel.roomType}</div>
  </div>
  
  <div class="hotel-info">
    <div class="hotel-header">
      <h3>{hotel.name}</h3>
      <div class="hotel-rating">
        <span class="rating-number">{hotel.rating}</span>
        <span class="stars">{renderStars(hotel.rating)}</span>
        <span class="reviews">({hotel.reviews} opiniones)</span>
      </div>
    </div>
    
    <div class="hotel-location">
      📍 {hotel.city}, {hotel.country}
    </div>
    
    <div class="hotel-amenities">
      {#each hotel.amenities as amenity}
        <span class="amenity-tag">{amenity}</span>
      {/each}
    </div>
    
    <div class="hotel-footer">
      <div class="price-info">
        <span class="price-label">Desde</span>
        <span class="price">${hotel.price}</span>
        <span class="price-period">por noche</span>
      </div>
      
      <button class="view-hotel-btn" on:click|stopPropagation={handleClick}>
        Ver Detalles
      </button>
    </div>
  </div>
</div>

<style>
  .hotel-card {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: flex;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .hotel-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    transform: translateY(-4px);
  }
  
  .hotel-image {
    position: relative;
    width: 300px;
    flex-shrink: 0;
  }
  
  .hotel-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .hotel-badge {
    position: absolute;
    top: 1rem;
    right: 1rem;
    background: rgba(255, 255, 255, 0.95);
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
    color: #667eea;
  }
  
  .hotel-info {
    flex: 1;
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
  }
  
  .hotel-header {
    display: flex;
    justify-content: space-between;
    align-items: start;
    margin-bottom: 0.75rem;
  }
  
  .hotel-header h3 {
    color: #333;
    font-size: 1.5rem;
    margin: 0;
  }
  
  .hotel-rating {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-shrink: 0;
  }
  
  .rating-number {
    background: #667eea;
    color: white;
    padding: 0.25rem 0.625rem;
    border-radius: 6px;
    font-weight: 700;
    font-size: 1rem;
  }
  
  .stars {
    font-size: 0.9rem;
  }
  
  .reviews {
    color: #666;
    font-size: 0.85rem;
  }
  
  .hotel-location {
    color: #666;
    margin-bottom: 1rem;
    font-size: 0.95rem;
  }
  
  .hotel-amenities {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
  }
  
  .amenity-tag {
    background: #f0f2ff;
    color: #667eea;
    padding: 0.4rem 0.875rem;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 500;
  }
  
  .hotel-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
    padding-top: 1rem;
    border-top: 1px solid #e0e0e0;
  }
  
  .price-info {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }
  
  .price-label {
    color: #666;
    font-size: 0.9rem;
  }
  
  .price {
    color: #333;
    font-size: 2rem;
    font-weight: 700;
  }
  
  .price-period {
    color: #666;
    font-size: 0.9rem;
  }
  
  .view-hotel-btn {
    padding: 0.875rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .view-hotel-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  @media (max-width: 768px) {
    .hotel-card {
      flex-direction: column;
    }
    
    .hotel-image {
      width: 100%;
      height: 200px;
    }
    
    .hotel-header {
      flex-direction: column;
      gap: 0.5rem;
    }
    
    .hotel-footer {
      flex-direction: column;
      gap: 1rem;
      align-items: stretch;
    }
    
    .view-hotel-btn {
      width: 100%;
    }
  }
</style>