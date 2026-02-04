<script>
  import { createEventDispatcher } from 'svelte';
  
  const dispatch = createEventDispatcher();
  
  export let isOpen = false;
  export let title = '';
  export let size = 'medium'; // 'small', 'medium', 'large', 'fullscreen'
  export let showCloseButton = true;
  export let closeOnBackdrop = true;
  
  function close() {
    isOpen = false;
    dispatch('close');
  }
  
  function handleBackdropClick() {
    if (closeOnBackdrop) {
      close();
    }
  }
  
  function handleKeydown(event) {
    if (event.key === 'Escape' && isOpen) {
      close();
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

{#if isOpen}
  <div class="modal-backdrop" on:click={handleBackdropClick}>
    <div class="modal {size}" on:click|stopPropagation>
      {#if title || showCloseButton}
        <div class="modal-header">
          {#if title}
            <h2 class="modal-title">{title}</h2>
          {/if}
          {#if showCloseButton}
            <button class="close-button" on:click={close} aria-label="Cerrar">
              ✕
            </button>
          {/if}
        </div>
      {/if}
      
      <div class="modal-body">
        <slot />
      </div>
      
      {#if $$slots.footer}
        <div class="modal-footer">
          <slot name="footer" />
        </div>
      {/if}
    </div>
  </div>
{/if}

<style>
  .modal-backdrop {
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
    padding: 1rem;
    animation: fadeIn 0.2s ease;
  }
  
  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
  
  .modal {
    background: white;
    border-radius: 12px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    display: flex;
    flex-direction: column;
    max-height: 90vh;
    animation: slideUp 0.3s ease;
  }
  
  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .modal.small {
    width: 100%;
    max-width: 400px;
  }
  
  .modal.medium {
    width: 100%;
    max-width: 600px;
  }
  
  .modal.large {
    width: 100%;
    max-width: 900px;
  }
  
  .modal.fullscreen {
    width: 95vw;
    height: 95vh;
    max-height: 95vh;
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .modal-title {
    margin: 0;
    color: #333;
    font-size: 1.5rem;
    font-weight: 700;
  }
  
  .close-button {
    background: none;
    border: none;
    font-size: 1.5rem;
    color: #888;
    cursor: pointer;
    padding: 0.25rem;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    transition: all 0.2s ease;
  }
  
  .close-button:hover {
    background: #f0f0f0;
    color: #333;
  }
  
  .modal-body {
    padding: 2rem;
    overflow-y: auto;
    flex: 1;
  }
  
  .modal-footer {
    padding: 1.5rem 2rem;
    border-top: 1px solid #e0e0e0;
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
  }
  
  @media (max-width: 768px) {
    .modal {
      width: 100%;
      max-width: 100%;
      max-height: 100vh;
      border-radius: 0;
    }
    
    .modal-body {
      padding: 1.5rem;
    }
    
    .modal-header,
    .modal-footer {
      padding: 1rem 1.5rem;
    }
  }
</style>