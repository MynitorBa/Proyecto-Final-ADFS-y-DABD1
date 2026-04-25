<script>
  // @ts-nocheck
  /**
   * @file DistribucionBoletos.svelte
   * @description Componente compartido para configurar la distribución de boletos y precios
   * de un vuelo. Muestra una barra de capacidad visual y cuatro inputs (turista / ejecutivo
   * para boletos y precios). Los inputs se deshabilitan cuando no hay avión seleccionado.
   * Todos los valores se exponen como props enlazables con bind:.
   */

  /** Avión seleccionado (objeto completo con capacidadPasajeros) o null. @type {any} */
  export let avion = null;

  /** Número de boletos clase turista. @type {number} */
  export let boletosTurista   = 0;

  /** Número de boletos clase ejecutivo. @type {number} */
  export let boletosEjecutivo = 0;

  /** Precio por boleto turista en USD. @type {number} */
  export let precioTurista    = 0;

  /** Precio por boleto ejecutivo en USD. @type {number} */
  export let precioEjecutivo  = 0;

  $: capacidad    = avion?.capacidadPasajeros ?? 0;
  $: totalBoletos = (parseInt(boletosTurista)   || 0)
                  + (parseInt(boletosEjecutivo) || 0);
  $: excedeLimite = capacidad > 0 && totalBoletos > capacidad;
  $: porcentaje   = capacidad > 0
    ? Math.min(100, Math.round(totalBoletos / capacidad * 100))
    : 0;
</script>

<!-- Barra de capacidad (solo cuando hay avión seleccionado) -->
{#if avion}
  <div class="db-capacity-bar">
    <div class="db-capacity-labels">
      <span>Capacidad: <strong>{capacidad} pax</strong></span>
      <span class="db-capacity-count"
        class:db-count--ok={totalBoletos === capacidad && !excedeLimite}
        class:db-count--error={excedeLimite}>
        {totalBoletos} asignados
        {#if excedeLimite}&nbsp;— Excede límite{:else if totalBoletos === capacidad}&nbsp;✔ Completo{/if}
      </span>
    </div>
    <div class="db-capacity-track">
      <div class="db-capacity-fill"
        class:db-fill--error={excedeLimite}
        style="width:{porcentaje}%">
      </div>
    </div>
  </div>
{/if}

<!-- Grid de 4 inputs: boletos y precios -->
<div class="db-grid">
  <div class="db-field">
    <label class="db-label">Boletos Turista *</label>
    <input type="number" class="db-input" min="0"
      bind:value={boletosTurista}
      disabled={!avion}
      placeholder={avion ? 'Ej: 150' : 'Selecciona un avión primero'}
      max={capacidad > 0 ? capacidad : undefined} />
  </div>
  <div class="db-field">
    <label class="db-label">Boletos Ejecutivo *</label>
    <input type="number" class="db-input" min="0"
      bind:value={boletosEjecutivo}
      disabled={!avion}
      placeholder={avion ? 'Ej: 30' : 'Selecciona un avión primero'}
      max={capacidad > 0 ? capacidad : undefined} />
  </div>
  <div class="db-field">
    <label class="db-label">Precio Turista (USD) *</label>
    <input type="number" class="db-input" min="0" step="0.01"
      bind:value={precioTurista}
      disabled={!avion}
      placeholder={avion ? 'Ej: 150.00' : 'Selecciona un avión primero'} />
  </div>
  <div class="db-field">
    <label class="db-label">Precio Ejecutivo (USD) *</label>
    <input type="number" class="db-input" min="0" step="0.01"
      bind:value={precioEjecutivo}
      disabled={!avion}
      placeholder={avion ? 'Ej: 300.00' : 'Selecciona un avión primero'} />
  </div>
</div>

<!-- Advertencia de capacidad excedida -->
{#if excedeLimite}
  <div class="db-capacity-warning">
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0">
      <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/>
      <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
    </svg>
    <span>
      <strong>Capacidad excedida:</strong>
      {totalBoletos} boletos &gt; {capacidad} pasajeros del avión
    </span>
  </div>
{/if}

<style>
  .db-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  .db-field {
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
  }

  .db-label {
    font-size: 0.85rem;
    font-weight: 600;
    color: #444;
  }

  .db-input {
    padding: 9px 12px;
    border: 1.5px solid #c9b99a;
    border-radius: 8px;
    font-size: 0.9rem;
    background: white;
    transition: border-color 0.15s, box-shadow 0.15s;
  }

  .db-input:focus {
    outline: none;
    border-color: var(--primary-color, #7a5c3f);
    box-shadow: 0 0 0 3px rgba(122,92,63,0.1);
  }

  .db-input:disabled {
    background: #f5f5f5;
    color: #999;
    border-color: #ddd;
    cursor: not-allowed;
  }

  /* ── Barra de capacidad ── */
  .db-capacity-bar    { margin-bottom: 0.75rem; }
  .db-capacity-labels { display: flex; justify-content: space-between; font-size: 0.8rem; color: #666; margin-bottom: 0.3rem; }
  .db-capacity-count  { font-weight: 600; }
  .db-count--ok       { color: #1a7a3f; }
  .db-count--error    { color: #b91c1c; }
  .db-capacity-track  { height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; }
  .db-capacity-fill   { height: 100%; background: var(--primary-color, #7a5c3f); border-radius: 4px; transition: width 0.3s ease; }
  .db-fill--error     { background: #ef4444; }

  /* ── Advertencia ── */
  .db-capacity-warning {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: #fff3cd;
    border: 1px solid #ffc107;
    color: #856404;
    padding: 0.5rem 0.75rem;
    border-radius: 6px;
    font-size: 0.82rem;
    margin-top: 0.5rem;
  }
</style>
