<script>
  export let images = [];
  export let altText = 'Imagen';
  
  let currentIndex = 0;
  let isFullscreen = false;
  
  function nextImage() {
    currentIndex = (currentIndex + 1) % images.length;
  }
  
  function prevImage() {
    currentIndex = (currentIndex - 1 + images.length) % images.length;
  }
  
  function selectImage(index) {
    currentIndex = index;
  }
  
  function toggleFullscreen() {
    isFullscreen = !isFullscreen;
  }
  
  function handleKeydown(event) {
    if (isFullscreen) {
      if (event.key === 'Escape') {
        isFullscreen = false;
      } else if (event.key === 'ArrowRight') {
        nextImage();
      } else if (event.key === 'ArrowLeft') {
        prevImage();
      }
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

<div class="image-gallery">
  <!-- Main Image -->
  <div class="main-image">
    <button class="gallery-nav prev" on:click={prevImage} aria-label="Imagen anterior">
      ‹
    </button>
    
    <img 
      src={images[currentIndex]} 
      alt="{altText} {currentIndex + 1}"
      on:click={toggleFullscreen}
    />
    
    <button class="gallery-nav next" on:click={nextImage} aria-label="Siguiente imagen">
      ›
    </button>
    
    <button class="fullscreen-btn" on:click={toggleFullscreen} aria-label="Pantalla completa">
      {isFullscreen ? '✕' : '⛶'}
    </button>
    
    <div class="image-counter">
      {currentIndex + 1} / {images.length}
    </div>
  </div>
  
  <!-- Thumbnails -->
  <div class="thumbnail-row">
    {#each images as image, index}
      <div 
        class="thumbnail"
        class:active={index === currentIndex}
        on:click={() => selectImage(index)}
        on:keydown={(e) => e.key === 'Enter' && selectImage(index)}
        role="button"
        tabindex="0"
        aria-label="Ver imagen {index + 1}"
      >
        <img src={image} alt="{altText} miniatura {index + 1}" />
      </div>
    {/each}
  </div>
</div>

<!-- Fullscreen Modal -->
{#if isFullscreen}
  <div class="fullscreen-modal" on:click={toggleFullscreen}>
    <button class="close-btn" aria-label="Cerrar pantalla completa">
      ✕
    </button>
    
    <button 
      class="modal-nav prev" 
      on:click|stopPropagation={prevImage}
      aria-label="Imagen anterior"
    >
      ‹
    </button>
    
    <img 
      src={images[currentIndex]} 
      alt="{altText} {currentIndex + 1}"
      on:click|stopPropagation
    />
    
    <button 
      class="modal-nav next" 
      on:click|stopPropagation={nextImage}
      aria-label="Siguiente imagen"
    >
      ›
    </button>
    
    <div class="modal-counter">
      {currentIndex + 1} / {images.length}
    </div>
  </div>
{/if}

<style>
  .image-gallery {
    background: white;
    border-radius: 12px;
    padding: 1.5rem;
  }
  
  .main-image {
    position: relative;
    width: 100%;
    height: 500px;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 1rem;
    background: #f8f9fa;
  }
  
  .main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: zoom-in;
  }
  
  .gallery-nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.9);
    border: none;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    font-size: 2rem;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2;
  }
  
  .gallery-nav:hover {
    background: white;
    transform: translateY(-50%) scale(1.1);
  }
  
  .gallery-nav.prev {
    left: 1rem;
  }
  
  .gallery-nav.next {
    right: 1rem;
  }
  
  .fullscreen-btn {
    position: absolute;
    top: 1rem;
    right: 1rem;
    background: rgba(255, 255, 255, 0.9);
    border: none;
    width: 40px;
    height: 40px;
    border-radius: 8px;
    font-size: 1.2rem;
    cursor: pointer;
    transition: all 0.2s ease;
    z-index: 2;
  }
  
  .fullscreen-btn:hover {
    background: white;
    transform: scale(1.1);
  }
  
  .image-counter {
    position: absolute;
    bottom: 1rem;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.9rem;
    font-weight: 600;
  }
  
  .thumbnail-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 1rem;
  }
  
  .thumbnail {
    height: 100px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 3px solid transparent;
    transition: all 0.2s ease;
    background: #f8f9fa;
  }
  
  .thumbnail:hover {
    border-color: #667eea;
    transform: scale(1.05);
  }
  
  .thumbnail.active {
    border-color: #667eea;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
  }
  
  .thumbnail img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  /* Fullscreen Modal */
  .fullscreen-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.95);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    cursor: zoom-out;
  }
  
  .fullscreen-modal img {
    max-width: 90vw;
    max-height: 90vh;
    object-fit: contain;
    cursor: default;
  }
  
  .close-btn {
    position: absolute;
    top: 2rem;
    right: 2rem;
    background: rgba(255, 255, 255, 0.9);
    border: none;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    font-size: 1.5rem;
    cursor: pointer;
    transition: all 0.2s ease;
    z-index: 10000;
  }
  
  .close-btn:hover {
    background: white;
    transform: scale(1.1);
  }
  
  .modal-nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.9);
    border: none;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    font-size: 2.5rem;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .modal-nav:hover {
    background: white;
    transform: translateY(-50%) scale(1.1);
  }
  
  .modal-nav.prev {
    left: 2rem;
  }
  
  .modal-nav.next {
    right: 2rem;
  }
  
  .modal-counter {
    position: absolute;
    bottom: 2rem;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(255, 255, 255, 0.9);
    color: #333;
    padding: 0.75rem 1.5rem;
    border-radius: 25px;
    font-size: 1.1rem;
    font-weight: 600;
  }
  
  @media (max-width: 768px) {
    .main-image {
      height: 300px;
    }
    
    .gallery-nav,
    .modal-nav {
      width: 40px;
      height: 40px;
      font-size: 1.5rem;
    }
    
    .thumbnail-row {
      grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
    }
    
    .thumbnail {
      height: 80px;
    }
    
    .close-btn {
      width: 40px;
      height: 40px;
      top: 1rem;
      right: 1rem;
    }
    
    .modal-nav.prev {
      left: 1rem;
    }
    
    .modal-nav.next {
      right: 1rem;
    }
  }
</style>