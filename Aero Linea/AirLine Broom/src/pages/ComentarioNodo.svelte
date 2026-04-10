<script>
  // @ts-nocheck
/**
 * @file ComentarioNodo.svelte
 * @description Componente de nodo de comentario recursivo que renderiza un comentario individual
 * y todas sus respuestas anidadas en un diseño en hilo estilo Reddit. Cada nodo muestra el avatar
 * del autor (primera letra de su nombre), nombre de usuario, fecha de publicacion, una calificacion
 * de estrellas opcional y el cuerpo del comentario. Los usuarios autenticados pueden dar voto
 * positivo o negativo al comentario (+1 / -1) y enviar respuestas de texto. Los comentarios hijo
 * se revelan u ocultan mediante un boton de alternancia. El componente usa svelte:self para
 * renderizar recursivamente nodos hijo con niveles de profundidad crecientes. Todos los callbacks
 * interactivos (votar, toggleForm, toggleExpandido, enviarRespuesta, onTextoChange) se pasan como
 * props desde la pagina padre para que el estado se administre de forma centralizada.
 */

  /** El objeto de comentario a renderizar, incluyendo id, nombreCompleto, username, fecha, contenido, cantidadEstrellas y downs. @type {object} */
  export let comentario;

  /** Funcion que devuelve un arreglo de comentarios hijo para un id de comentario dado. @type {Function} */
  export let getHijos;

  /** Mapa de objetos de estado de interfaz por comentario indexados por id, cada uno con expandido, mostrandoForm, textoRespuesta, enviando y votoActual. @type {object} */
  export let estadoNodos;

  /** Indica si el usuario actual tiene sesion activa, controla la visibilidad de las acciones de voto y respuesta. @type {boolean} */
  export let haySession;

  /** Funcion que formatea un valor de fecha en una cadena de visualizacion localizada. @type {Function} */
  export let formatFecha;

  /** Funcion que devuelve un arreglo de booleanos (true = estrella llena) para una cantidad de estrellas dada. @type {Function} */
  export let getEstrellas;

  /** Funcion llamada cuando el usuario hace clic en una flecha de voto positivo o negativo, recibe el id del comentario y el valor (+1 o -1). @type {Function} */
  export let votar;

  /** Funcion que alterna la visibilidad del formulario de respuesta para un id de comentario dado. @type {Function} */
  export let toggleForm;

  /** Funcion que alterna el estado expandido (mostrar/ocultar hijos) para un id de comentario dado. @type {Function} */
  export let toggleExpandido;

  /** Funcion asincrona que envia el texto de respuesta para un id de comentario dado. @type {Function} */
  export let enviarRespuesta;

  /** Funcion llamada al ingresar texto en el textarea, recibe el id del comentario y el nuevo valor de texto. @type {Function} */
  export let onTextoChange;

  /** Nivel de anidamiento actual de este nodo, se incrementa en 1 por cada renderizado recursivo de un hijo. @type {number} */
  export let profundidad = 0;

  import ComentarioNodo from './ComentarioNodo.svelte';

  // Obtiene de forma reactiva el arreglo de comentarios hijo directos de este nodo cada vez que comentario.id cambia.
  $: hijos  = getHijos(comentario.id);

  // Obtiene de forma reactiva el estado de interfaz de este comentario desde estadoNodos, usando un objeto de estado vacio por defecto.
  $: estado = estadoNodos[comentario.id] ?? {
    expandido: false,
    mostrandoForm: false,
    textoRespuesta: '',
    enviando: false,
    votoActual: null
  };

  // Registra el voto actual del usuario para este comentario (+1, -1 o null) para aplicar el estilo activo a la flecha.
  $: votoActual = estado.votoActual;

  // El campo de la API "downs" almacena la puntuacion neta; un valor positivo muestra +puntuacion, negativo muestra -puntuacion.
  $: score = comentario.downs ?? 0;

  // Determina la clase CSS aplicada a la visualizacion de puntuacion segun sea positiva, negativa o cero.
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
