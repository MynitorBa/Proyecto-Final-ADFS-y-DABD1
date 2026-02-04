<script>
  import { createEventDispatcher } from 'svelte';
  
  const dispatch = createEventDispatcher();
  
  export let rating = 0;
  export let maxStars = 5;
  export let interactive = false;
  export let size = 'medium'; // 'small', 'medium', 'large'
  
  let hoveredStar = 0;
  
  function handleClick(star) {
    if (interactive) {
      rating = star;
      dispatch('rate', star);
    }
  }
  
  function handleMouseEnter(star) {
    if (interactive) {
      hoveredStar = star;
    }
  }
  
  function handleMouseLeave() {
    if (interactive) {
      hoveredStar = 0;
    }
  }
  
  function getStarDisplay(index) {
    const starValue = index + 1;
    const displayRating = interactive && hoveredStar > 0 ? hoveredStar : rating;
    
    if (displayRating >= starValue) {
      return '⭐'; // Estrella llena
    } else if (displayRating > starValue - 1 && displayRating < starValue) {
      return '⭐'; // Media estrella (usando llena por simplicidad)
    } else {
      return '☆'; // Estrella vacía
    }
  }
</script>

<div 
  class="rating-stars {size}"
  class:interactive
  role={interactive ? 'slider' : 'img'}
  aria-label={`Valoración: ${rating} de ${maxStars} estrellas`}
  aria-valuemin="0"
  aria-valuemax={maxStars}
  aria-valuenow={rating}
>
  {#each Array(maxStars) as _, i}
    <button
      type="button"
      class="star"
      class:filled={rating > i}
      class:hovered={interactive && hoveredStar > i}
      disabled={!interactive}
      on:click={() => handleClick(i + 1)}
      on:mouseenter={() => handleMouseEnter(i + 1)}
      on:mouseleave={handleMouseLeave}
      aria-label={`${i + 1} estrella${i > 0 ? 's' : ''}`}
    >
      {getStarDisplay(i)}
    </button>
  {/each}
  
  {#if $$slots.label}
    <span class="rating-label">
      <slot name="label" />
    </span>
  {/if}
</div>

<style>
  .rating-stars {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
  }
  
  .star {
    background: none;
    border: none;
    padding: 0;
    line-height: 1;
    transition: all 0.2s ease;
  }
  
  .star:not(:disabled) {
    cursor: pointer;
  }
  
  /* Tamaños */
  .small .star {
    font-size: 1rem;
  }
  
  .medium .star {
    font-size: 1.5rem;
  }
  
  .large .star {
    font-size: 2rem;
  }
  
  /* Estados interactivos */
  .interactive .star:not(:disabled):hover {
    transform: scale(1.2);
  }
  
  .interactive .star:not(:disabled):active {
    transform: scale(1.1);
  }
  
  .star.hovered {
    filter: brightness(1.2);
  }
  
  .rating-label {
    margin-left: 0.5rem;
    color: #666;
    font-size: 0.95rem;
  }
  
  /* Accesibilidad */
  .star:focus-visible {
    outline: 2px solid #667eea;
    outline-offset: 2px;
    border-radius: 4px;
  }
</style>