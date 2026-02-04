<script>
  export let size = 'medium'; // 'small', 'medium', 'large'
  export let color = '#667eea';
  export let fullscreen = false;
  export let text = '';
</script>

{#if fullscreen}
  <div class="loading-overlay">
    <div class="spinner-container">
      <div class="spinner {size}" style="border-top-color: {color}"></div>
      {#if text}
        <p class="loading-text">{text}</p>
      {/if}
    </div>
  </div>
{:else}
  <div class="spinner-wrapper">
    <div class="spinner {size}" style="border-top-color: {color}"></div>
    {#if text}
      <p class="loading-text">{text}</p>
    {/if}
  </div>
{/if}

<style>
  .loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  }
  
  .spinner-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }
  
  .spinner-wrapper {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
  }
  
  .spinner {
    border: 3px solid rgba(0, 0, 0, 0.1);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  
  .spinner.small {
    width: 24px;
    height: 24px;
    border-width: 2px;
  }
  
  .spinner.medium {
    width: 48px;
    height: 48px;
    border-width: 3px;
  }
  
  .spinner.large {
    width: 72px;
    height: 72px;
    border-width: 4px;
  }
  
  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
  
  .loading-text {
    color: #333;
    font-size: 1rem;
    font-weight: 600;
    margin: 0;
  }
  
  .loading-overlay .loading-text {
    color: white;
  }
</style>