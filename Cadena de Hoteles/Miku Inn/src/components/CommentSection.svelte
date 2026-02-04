<script>
  import { createEventDispatcher } from 'svelte';
  import RatingStars from './RatingStars.svelte';
  
  const dispatch = createEventDispatcher();
  
  export let comments = [];
  export let allowNewComments = true;
  
  let newComment = '';
  let newRating = 5;
  let replyingTo = null;
  let replyText = '';
  
  function submitComment() {
    if (newComment.trim()) {
      dispatch('add-comment', {
        text: newComment,
        rating: newRating
      });
      newComment = '';
      newRating = 5;
    }
  }
  
  function startReply(commentId) {
    replyingTo = commentId;
    replyText = '';
  }
  
  function cancelReply() {
    replyingTo = null;
    replyText = '';
  }
  
  function submitReply(commentId) {
    if (replyText.trim()) {
      dispatch('add-reply', {
        commentId,
        text: replyText
      });
      replyingTo = null;
      replyText = '';
    }
  }
  
  function renderStars(rating) {
    return '⭐'.repeat(rating) + '☆'.repeat(5 - rating);
  }
</script>

<div class="comments-section">
  <h2>Opiniones de los Huéspedes</h2>
  
  {#if allowNewComments}
    <!-- Add Comment Form -->
    <div class="add-comment-form">
      <h3>Deja tu Opinión</h3>
      
      <div class="rating-selector">
        <label>Tu valoración:</label>
        <RatingStars 
          bind:rating={newRating}
          interactive={true}
          size="large"
          on:rate={(e) => newRating = e.detail}
        />
      </div>
      
      <textarea
        bind:value={newComment}
        placeholder="Comparte tu experiencia..."
        rows="4"
      ></textarea>
      
      <button class="submit-comment-btn" on:click={submitComment}>
        Publicar Opinión
      </button>
    </div>
  {/if}
  
  <!-- Comments List -->
  <div class="comments-list">
    {#if comments.length === 0}
      <div class="no-comments">
        <p>Aún no hay opiniones. ¡Sé el primero en comentar!</p>
      </div>
    {:else}
      {#each comments as comment}
        <div class="comment">
          <div class="comment-header">
            <div class="comment-author">
              <div class="author-avatar">
                {comment.author.charAt(0).toUpperCase()}
              </div>
              <div>
                <strong>{comment.author}</strong>
                <span class="comment-date">{comment.date}</span>
              </div>
            </div>
            {#if comment.rating}
              <div class="comment-rating">
                {renderStars(comment.rating)}
              </div>
            {/if}
          </div>
          
          <p class="comment-text">{comment.text}</p>
          
          <div class="comment-actions">
            {#if allowNewComments}
              <button class="reply-btn" on:click={() => startReply(comment.id)}>
                💬 Responder
              </button>
            {/if}
          </div>
          
          <!-- Reply Form -->
          {#if replyingTo === comment.id}
            <div class="reply-form">
              <textarea
                bind:value={replyText}
                placeholder="Escribe tu respuesta..."
                rows="3"
              ></textarea>
              <div class="reply-actions">
                <button class="btn-cancel" on:click={cancelReply}>
                  Cancelar
                </button>
                <button class="btn-submit" on:click={() => submitReply(comment.id)}>
                  Enviar
                </button>
              </div>
            </div>
          {/if}
          
          <!-- Replies -->
          {#if comment.replies && comment.replies.length > 0}
            <div class="replies">
              {#each comment.replies as reply}
                <div class="reply">
                  <div class="reply-header">
                    <div class="reply-author">
                      <div class="author-avatar small">
                        {reply.author.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <strong>{reply.author}</strong>
                        <span class="reply-date">{reply.date}</span>
                      </div>
                    </div>
                  </div>
                  <p class="reply-text">{reply.text}</p>
                </div>
              {/each}
            </div>
          {/if}
        </div>
      {/each}
    {/if}
  </div>
</div>

<style>
  .comments-section {
    margin: 2rem 0;
  }
  
  h2 {
    color: #333;
    font-size: 1.8rem;
    margin-bottom: 2rem;
  }
  
  .add-comment-form {
    background: #f8f9fa;
    padding: 2rem;
    border-radius: 12px;
    margin-bottom: 2rem;
  }
  
  .add-comment-form h3 {
    color: #333;
    margin-bottom: 1.5rem;
  }
  
  .rating-selector {
    margin-bottom: 1.5rem;
  }
  
  .rating-selector label {
    display: block;
    font-weight: 600;
    color: #555;
    margin-bottom: 0.75rem;
  }
  
  .add-comment-form textarea,
  .reply-form textarea {
    width: 100%;
    padding: 1rem;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
    font-family: inherit;
    resize: vertical;
    margin-bottom: 1rem;
  }
  
  .add-comment-form textarea:focus,
  .reply-form textarea:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .submit-comment-btn {
    padding: 0.875rem 2rem;
    background: #667eea;
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .submit-comment-btn:hover {
    background: #5568d3;
  }
  
  .comments-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .no-comments {
    text-align: center;
    padding: 2rem;
    color: #888;
  }
  
  .comment {
    padding: 1.5rem;
    background: #f8f9fa;
    border-radius: 12px;
  }
  
  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: start;
    margin-bottom: 1rem;
  }
  
  .comment-author,
  .reply-author {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .author-avatar {
    width: 45px;
    height: 45px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.2rem;
  }
  
  .author-avatar.small {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
  
  .comment-author strong,
  .reply-author strong {
    color: #333;
    display: block;
  }
  
  .comment-date,
  .reply-date {
    color: #888;
    font-size: 0.85rem;
  }
  
  .comment-text,
  .reply-text {
    color: #555;
    line-height: 1.6;
    margin-bottom: 1rem;
  }
  
  .comment-actions {
    display: flex;
    gap: 1rem;
  }
  
  .reply-btn {
    background: none;
    border: none;
    color: #667eea;
    font-weight: 600;
    cursor: pointer;
    padding: 0.5rem 0;
    transition: color 0.2s ease;
  }
  
  .reply-btn:hover {
    color: #5568d3;
  }
  
  .reply-form {
    margin-top: 1rem;
    padding: 1rem;
    background: white;
    border-radius: 8px;
  }
  
  .reply-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
  }
  
  .btn-cancel,
  .btn-submit {
    padding: 0.625rem 1.5rem;
    border: none;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .btn-cancel {
    background: #e0e0e0;
    color: #555;
  }
  
  .btn-cancel:hover {
    background: #d0d0d0;
  }
  
  .btn-submit {
    background: #667eea;
    color: white;
  }
  
  .btn-submit:hover {
    background: #5568d3;
  }
  
  .replies {
    margin-top: 1rem;
    margin-left: 2rem;
    padding-left: 1.5rem;
    border-left: 3px solid #e0e0e0;
  }
  
  .reply {
    background: white;
    padding: 1rem;
    border-radius: 8px;
    margin-bottom: 1rem;
  }
  
  .reply:last-child {
    margin-bottom: 0;
  }
  
  .reply-header {
    margin-bottom: 0.75rem;
  }
  
  @media (max-width: 768px) {
    .replies {
      margin-left: 1rem;
      padding-left: 1rem;
    }
  }
</style>