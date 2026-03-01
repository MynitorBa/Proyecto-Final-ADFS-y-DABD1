<script>
  // @ts-nocheck
  // ComentarioNodo.svelte — comentario recursivo con votos estilo Reddit

  export let comentario;
  export let getHijos;
  export let estadoNodos;
  export let haySession;
  export let formatFecha;
  export let getEstrellas;
  export let votar;
  export let toggleForm;
  export let toggleExpandido;
  export let enviarRespuesta;
  export let onTextoChange;
  export let profundidad = 0;

  import ComentarioNodo from './ComentarioNodo.svelte';

  $: hijos  = getHijos(comentario.id);
  $: estado = estadoNodos[comentario.id] ?? {
    expandido: false,
    mostrandoForm: false,
    textoRespuesta: '',
    enviando: false,
    votoActual: null
  };
  $: votoActual = estado.votoActual;

  // `downs` en la API es el score neto. Se envía +1 / -1 al votar.
  $: score = comentario.downs ?? 0;
  $: scoreClass = score > 0 ? 'score-pos' : score < 0 ? 'score-neg' : 'score-zero';
</script>

<div class="dv-comentario-nodo">
  <div class="dv-comentario-card">

    <!-- Layout: columna votos + cuerpo -->
    <div class="dv-cmt-layout">

      <!-- Columna de votos estilo Reddit -->
      <div class="dv-cmt-votes" class:voted-up={votoActual === 1} class:voted-down={votoActual === -1}>

        <button
          class="dv-vote-arrow dv-vote-arrow--up"
          class:dv-vote-arrow--active={votoActual === 1}
          on:click={() => votar(comentario.id, 1)}
          disabled={!haySession}
          title={haySession ? 'Útil' : 'Inicia sesión para votar'}
          aria-label="Voto positivo"
        >
          <svg viewBox="0 0 24 24" fill={votoActual === 1 ? 'currentColor' : 'none'} stroke="currentColor" stroke-width="2.5">
            <polyline points="18 15 12 9 6 15"/>
          </svg>
        </button>

        <span class="dv-vote-score {scoreClass}">
          {score > 0 ? '+' : ''}{score}
        </span>

        <button
          class="dv-vote-arrow dv-vote-arrow--down"
          class:dv-vote-arrow--active={votoActual === -1}
          on:click={() => votar(comentario.id, -1)}
          disabled={!haySession}
          title={haySession ? 'No útil' : 'Inicia sesión para votar'}
          aria-label="Voto negativo"
        >
          <svg viewBox="0 0 24 24" fill={votoActual === -1 ? 'currentColor' : 'none'} stroke="currentColor" stroke-width="2.5">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </button>

      </div>

      <!-- Cuerpo -->
      <div class="dv-cmt-body">

        <div class="dv-comentario-header">
          <div class="dv-comentario-user">
            <div class="dv-comentario-avatar">
              {comentario.nombreCompleto?.charAt(0)?.toUpperCase() ?? '?'}
            </div>
            <div class="dv-comentario-user-info">
              <div class="dv-comentario-nombre">{comentario.nombreCompleto}</div>
              <div class="dv-comentario-username">@{comentario.username}</div>
            </div>
          </div>
          <div class="dv-comentario-fecha">{formatFecha(comentario.fecha)}</div>
        </div>

        {#if comentario.cantidadEstrellas !== null && comentario.cantidadEstrellas !== undefined}
          <div class="dv-comentario-estrellas">
            {#each getEstrellas(comentario.cantidadEstrellas) as llena}
              <svg class="dv-estrella" class:dv-estrella--llena={llena}
                xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                fill={llena ? 'currentColor' : 'none'} stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
            {/each}
          </div>
        {/if}

        <div class="dv-comentario-contenido">{comentario.contenido}</div>

        <div class="dv-comentario-acciones">
          {#if haySession}
            <button class="dv-responder-btn" on:click={() => toggleForm(comentario.id)}>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 17 4 12 9 7"/>
                <path d="M20 18v-2a4 4 0 0 0-4-4H4"/>
              </svg>
              {estado.mostrandoForm ? 'Cancelar' : 'Responder'}
            </button>
          {:else}
            <span class="dv-login-hint">Inicia sesión para responder</span>
          {/if}

          {#if hijos.length > 0}
            <button
              class="dv-ver-mas"
              class:dv-ver-mas--expanded={estado.expandido}
              on:click={() => toggleExpandido(comentario.id)}
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
              {estado.expandido
                ? 'Ocultar respuestas'
                : `Ver ${hijos.length} ${hijos.length === 1 ? 'respuesta' : 'respuestas'}`}
            </button>
          {/if}
        </div>

        {#if estado.mostrandoForm}
          <div class="dv-reply-form">
            <textarea
              placeholder="Escribe tu respuesta..."
              value={estado.textoRespuesta}
              on:input={e => onTextoChange(comentario.id, e.target.value)}
              rows="3"
            ></textarea>
            <div class="dv-reply-actions">
              <button class="dv-reply-cancel" on:click={() => toggleForm(comentario.id)}>Cancelar</button>
              <button
                class="dv-reply-submit"
                on:click={() => enviarRespuesta(comentario.id)}
                disabled={!estado.textoRespuesta.trim() || estado.enviando}
              >
                {estado.enviando ? 'Enviando...' : 'Responder'}
              </button>
            </div>
          </div>
        {/if}

      </div>
    </div>

    <!-- Hijos recursivos -->
    {#if hijos.length > 0 && estado.expandido}
      <div class="dv-comentario-hilo">
        <div class="dv-hilo-linea"></div>
        <div class="dv-hilo-respuestas">
          {#each hijos as hijo (hijo.id)}
            <svelte:self
              comentario={hijo}
              {getHijos}
              {estadoNodos}
              {haySession}
              {formatFecha}
              {getEstrellas}
              {votar}
              {toggleForm}
              {toggleExpandido}
              {enviarRespuesta}
              {onTextoChange}
              profundidad={profundidad + 1}
            />
          {/each}
        </div>
      </div>
    {/if}

  </div>
</div>