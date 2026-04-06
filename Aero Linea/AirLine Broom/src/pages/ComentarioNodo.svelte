<script>
  // @ts-nocheck
/**
 * @file ComentarioNodo.svelte
 * @description Recursive comment node component that renders a single comment and all its
 * nested replies in a Reddit-style threaded layout. Each node displays the author avatar
 * (first letter of their name), username, post date, an optional star rating, and the
 * comment body. Authenticated users can upvote or downvote the comment (+1 / -1) and
 * submit text replies. Child comments are revealed or hidden via a toggle button. The
 * component uses svelte:self to recursively render child nodes at increasing profundidad
 * levels. All interactive callbacks (votar, toggleForm, toggleExpandido, enviarRespuesta,
 * onTextoChange) are passed in as props from the parent page so state is managed centrally.
 */

  /** The comment object to render, including id, nombreCompleto, username, fecha, contenido, cantidadEstrellas, and downs. @type {object} */
  export let comentario;

  /** Function that returns an array of child comment objects for a given comment id. @type {Function} */
  export let getHijos;

  /** Map of per-comment UI state objects keyed by comment id, each containing expandido, mostrandoForm, textoRespuesta, enviando, and votoActual. @type {object} */
  export let estadoNodos;

  /** Whether the current user has an active session, controls visibility of voting and reply actions. @type {boolean} */
  export let haySession;

  /** Function that formats a date value into a localized display string. @type {Function} */
  export let formatFecha;

  /** Function that returns an array of boolean values (true = filled star) for a given star count. @type {Function} */
  export let getEstrellas;

  /** Function called when the user clicks an upvote or downvote arrow, receives commentId and value (+1 or -1). @type {Function} */
  export let votar;

  /** Function that toggles the reply form visibility for a given comment id. @type {Function} */
  export let toggleForm;

  /** Function that toggles the expanded state (show/hide children) for a given comment id. @type {Function} */
  export let toggleExpandido;

  /** Async function that submits the reply text for a given comment id. @type {Function} */
  export let enviarRespuesta;

  /** Function called on textarea input, receives commentId and the new text value. @type {Function} */
  export let onTextoChange;

  /** Current nesting depth of this node, incremented by 1 for each recursive child render. @type {number} */
  export let profundidad = 0;

  import ComentarioNodo from './ComentarioNodo.svelte';

  // Reactively retrieves the array of direct child comments for this node whenever comentario.id changes.
  $: hijos  = getHijos(comentario.id);

  // Reactively retrieves this comment's UI state from estadoNodos, falling back to a default empty state object.
  $: estado = estadoNodos[comentario.id] ?? {
    expandido: false,
    mostrandoForm: false,
    textoRespuesta: '',
    enviando: false,
    votoActual: null
  };

  // Tracks the user's current vote for this comment (+1, -1, or null) for active arrow styling.
  $: votoActual = estado.votoActual;

  // The API field `downs` stores the net score; a positive value shows +score, negative shows -score.
  $: score = comentario.downs ?? 0;

  // Determines the CSS class applied to the score display based on whether it is positive, negative, or zero.
  $: scoreClass = score > 0 ? 'score-pos' : score < 0 ? 'score-neg' : 'score-zero';
</script>

<!-- Nodo de comentario individual con columna de votos y cuerpo de contenido -->
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

        <!-- Formulario inline para escribir una respuesta al comentario -->
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

    <!-- Respuestas anidadas renderizadas recursivamente cuando el nodo esta expandido -->
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
