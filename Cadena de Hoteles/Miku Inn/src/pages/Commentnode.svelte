<script>
  import { createEventDispatcher } from 'svelte';
  import CommentNode from './CommentNode.svelte';

  /** @type {any}   el comentario actual */
  export let comment;
  /** @type {any[]} todos los comentarios (para buscar hijos) */
  export let allComments = [];
  /** @type {Map<number,number>} */
  export let misDowns = new Map();
  /** @type {boolean} */
  export let isReply = false;

  const dispatch = createEventDispatcher();

  // Hijos directos de este comentario
  $: hijos = allComments.filter(c => c.comentarioPadreId === comment.id);

  let replyOpen    = false;
  let repliesOpen  = false;   // hijos colapsados por defecto
  let replyText    = '';
  let replySaving  = false;

  function fmt(n) {
    return n > 0 ? `+${n}` : `${n}`;
  }

  function starLabel(n) {
    return ['','Muy malo','Malo','Regular','Bueno','Excelente'][n] || '';
  }

  /** @param {1|-1} valor */
  function vote(valor) {
    dispatch('vote', { comentarioId: comment.id, valor });
  }

  async function sendReply() {
    const texto = replyText.trim();
    if (!texto) return;
    replySaving = true;
    dispatch('reply', {
      parentId: comment.id,
      contenido: texto,
      done: (/** @type {boolean} */ ok) => {
        replySaving = false;
        if (ok) { replyText = ''; replyOpen = false; }
      },
    });
  }

  $: miDown = misDowns.get(comment.id);
</script>

<div class="cmt-card" class:cmt-reply-card={isReply} class:cmt-resena={!isReply && comment.resena !== null}>
  <!-- Columna votos -->
  <div class="cmt-votes" class:cmt-votes-sm={isReply} class:cmt-voted-up={miDown === 1} class:cmt-voted-down={miDown === -1}>
    <button class="cmt-arrow" class:active-up={miDown === 1} on:click={() => vote(1)} aria-label="Voto positivo">
      <svg width={isReply ? 14 : 18} height={isReply ? 14 : 18} viewBox="0 0 24 24"
        fill={miDown === 1 ? 'currentColor' : 'none'} stroke="currentColor" stroke-width="2.5">
        <polyline points="18 15 12 9 6 15"/>
      </svg>
    </button>
    <span class="cmt-score" class:cmt-score-sm={isReply} class:score-up={miDown === 1} class:score-down={miDown === -1}>
      {comment.downs ?? 0}
    </span>
    <button class="cmt-arrow" class:active-down={miDown === -1} on:click={() => vote(-1)} aria-label="Voto negativo">
      <svg width={isReply ? 14 : 18} height={isReply ? 14 : 18} viewBox="0 0 24 24"
        fill={miDown === -1 ? 'currentColor' : 'none'} stroke="currentColor" stroke-width="2.5">
        <polyline points="6 9 12 15 18 9"/>
      </svg>
    </button>
  </div>

  <!-- Cuerpo -->
  <div class="cmt-body">
    <div class="cmt-header">
      <div class="cmt-avatar" class:cmt-avatar-sm={isReply}>
        {comment.username?.[0]?.toUpperCase() ?? '?'}
      </div>
      <div class="cmt-meta">
        <strong class="cmt-user">{comment.username}</strong>
        <span class="cmt-date">{comment.fecha}</span>
      </div>
      {#if comment.resena !== null && comment.resena !== undefined && !isReply}
        <div class="cmt-stars-box">
          <div class="cmt-stars">
            {#each [1,2,3,4,5] as s}
              <span class:filled={comment.resena >= s}>★</span>
            {/each}
          </div>
          <span class="cmt-star-label">{starLabel(comment.resena)}</span>
        </div>
      {/if}
    </div>

    <p class="cmt-text">{comment.contenido}</p>

    <!-- Acciones -->
    <div class="cmt-actions">
      <button class="cmt-reply-toggle" on:click={() => { repliesOpen = !repliesOpen; }}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        Responder{hijos.length > 0 ? ` (${hijos.length})` : ''}
        {#if hijos.length > 0}
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
            style="transition: transform .2s; transform: rotate({repliesOpen ? 180 : 0}deg)">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        {/if}
      </button>
      <button class="cmt-reply-toggle" on:click={() => { replyOpen = !replyOpen; repliesOpen = true; }}>
        + Responder
      </button>
    </div>

    <!-- Hijos recursivos -->
    {#if repliesOpen && hijos.length > 0}
      <div class="cmt-replies">
        {#each hijos as hijo}
          <svelte:self
            comment={hijo}
            allComments={allComments}
            misDowns={misDowns}
            isReply={true}
            on:vote
            on:reply
          />
        {/each}
      </div>
    {/if}

    <!-- Form responder -->
    {#if replyOpen}
      <div class="cmt-reply-form">
        <textarea
          class="cmt-reply-input"
          bind:value={replyText}
          placeholder="Escribe tu respuesta..."
          rows="2"
          autofocus
        ></textarea>
        <div class="cmt-reply-form-actions">
          <button class="cmt-reply-cancel" on:click={() => { replyOpen = false; replyText = ''; }}>
            Cancelar
          </button>
          <button class="cmt-reply-send" on:click={sendReply} disabled={replySaving}>
            {replySaving ? 'Enviando...' : 'Enviar respuesta'}
          </button>
        </div>
      </div>
    {/if}
  </div>
</div>