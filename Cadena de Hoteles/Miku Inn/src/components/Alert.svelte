<script>
  import { createEventDispatcher } from 'svelte';
  
  const dispatch = createEventDispatcher();
  
  export let type = 'info'; // 'success', 'error', 'warning', 'info'
  export let title = '';
  export let message = '';
  export let dismissible = true;
  export let icon = '';
  
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };
  
  function close() {
    dispatch('close');
  }
  
  $: displayIcon = icon || icons[type];
</script>

<div class="alert {type}" role="alert">
  <div class="alert-icon">
    {displayIcon}
  </div>
  
  <div class="alert-content">
    {#if title}
      <strong class="alert-title">{title}</strong>
    {/if}
    {#if message}
      <p class="alert-message">{message}</p>
    {/if}
    <slot />
  </div>
  
  {#if dismissible}
    <button class="alert-close" on:click={close} aria-label="Cerrar">
      ✕
    </button>
  {/if}
</div>

<style>
  .alert {
    display: flex;
    align-items: start;
    gap: 1rem;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    border: 2px solid;
    animation: slideIn 0.3s ease;
  }
  
  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  /* Success */
  .alert.success {
    background: #e8f5e9;
    border-color: #4caf50;
    color: #2e7d32;
  }
  
  .alert.success .alert-icon {
    background: #4caf50;
  }
  
  /* Error */
  .alert.error {
    background: #ffebee;
    border-color: #f44336;
    color: #c62828;
  }
  
  .alert.error .alert-icon {
    background: #f44336;
  }
  
  /* Warning */
  .alert.warning {
    background: #fff3e0;
    border-color: #ff9800;
    color: #e65100;
  }
  
  .alert.warning .alert-icon {
    background: #ff9800;
  }
  
  /* Info */
  .alert.info {
    background: #e3f2fd;
    border-color: #2196f3;
    color: #0d47a1;
  }
  
  .alert.info .alert-icon {
    background: #2196f3;
  }
  
  .alert-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 700;
    font-size: 1.2rem;
    flex-shrink: 0;
  }
  
  .alert-content {
    flex: 1;
  }
  
  .alert-title {
    display: block;
    font-size: 1.05rem;
    margin-bottom: 0.25rem;
  }
  
  .alert-message {
    margin: 0;
    line-height: 1.5;
  }
  
  .alert-close {
    background: none;
    border: none;
    font-size: 1.2rem;
    cursor: pointer;
    padding: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    opacity: 0.7;
    transition: all 0.2s ease;
    flex-shrink: 0;
  }
  
  .alert-close:hover {
    opacity: 1;
    background: rgba(0, 0, 0, 0.1);
  }
</style>