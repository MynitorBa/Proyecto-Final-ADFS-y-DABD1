<template>
  <div class="cn-nodo">
    <div class="cn-card">

      <!-- Layout principal: columna de votos a la izquierda + cuerpo del comentario -->
      <div class="cn-layout">

        <!-- Columna de votación estilo Reddit con flechas arriba/abajo y puntuación -->
        <div class="cn-votes"
          :class="{ 'cn-votes--up': votoActual === 1, 'cn-votes--down': votoActual === -1 }">

          <!-- Botón para votar positivo (útil) -->
          <button
            class="cn-arrow cn-arrow--up"
            :class="{ 'cn-arrow--active': votoActual === 1 }"
            @click="emit('votar', comentario.id, 1)"
            :disabled="!haySession"
            :title="haySession ? 'Útil' : 'Inicia sesión para votar'"
            type="button"
          >
            <svg viewBox="0 0 24 24"
              :fill="votoActual === 1 ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2.5"
              width="14" height="14">
              <polyline points="18 15 12 9 6 15"/>
            </svg>
          </button>

          <!-- Puntuación neta del comentario (positivos - negativos) -->
          <span class="cn-score" :class="scoreClass">
            {{ score > 0 ? '+' : '' }}{{ score }}
          </span>

          <!-- Botón para votar negativo (no útil) -->
          <button
            class="cn-arrow cn-arrow--down"
            :class="{ 'cn-arrow--active': votoActual === -1 }"
            @click="emit('votar', comentario.id, -1)"
            :disabled="!haySession"
            :title="haySession ? 'No útil' : 'Inicia sesión para votar'"
            type="button"
          >
            <svg viewBox="0 0 24 24"
              :fill="votoActual === -1 ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2.5"
              width="14" height="14">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>

        </div>

        <!-- Cuerpo principal: cabecera, estrellas, contenido y acciones -->
        <div class="cn-body">

          <!-- Cabecera con avatar, nombre de usuario y fecha del comentario -->
          <div class="cn-header">
            <div class="cn-user">
              <div class="cn-avatar">
                {{ (comentario.nombreCompleto ?? comentario.username)?.charAt(0)?.toUpperCase() ?? '?' }}
              </div>
              <div class="cn-user-info">
                <span class="cn-nombre">
                  {{ comentario.nombreCompleto ?? comentario.username }}
                </span>
                <span class="cn-username">@{{ comentario.username }}</span>
              </div>
            </div>
            <span class="cn-fecha">{{ formatFecha(comentario.fecha) }}</span>
          </div>

          <!-- Estrellas de valoración: vuelo usa cantidadEstrellas, hotel usa resena -->
          <div v-if="estrellas !== null" class="cn-estrellas">
            <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
              :fill="n <= estrellas ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2"
              class="cn-estrella" :class="{ 'cn-estrella--llena': n <= estrellas }"
              width="14" height="14">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
          </div>

          <!-- Texto del comentario -->
          <p class="cn-contenido">{{ comentario.contenido }}</p>

          <!-- Acciones: responder y ver/ocultar respuestas anidadas -->
          <div class="cn-acciones">
            <!-- Botón de respuesta, solo visible si el usuario tiene sesión activa -->
            <button v-if="haySession"
              class="cn-responder-btn"
              @click="emit('toggleForm', comentario.id)"
              type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                <polyline points="9 17 4 12 9 7"/>
                <path d="M20 18v-2a4 4 0 0 0-4-4H4"/>
              </svg>
              {{ estado.mostrandoForm ? 'Cancelar' : 'Responder' }}
            </button>

            <!-- Botón para expandir o colapsar los comentarios hijo -->
            <button v-if="hijos.length > 0"
              class="cn-ver-mas"
              :class="{ 'cn-ver-mas--expanded': estado.expandido }"
              @click="emit('toggleExpandido', comentario.id)"
              type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"
                :style="{ transform: estado.expandido ? 'rotate(180deg)' : 'none', transition: 'transform .2s' }">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
              {{ estado.expandido
                ? 'Ocultar respuestas'
                : `Ver ${hijos.length} ${hijos.length === 1 ? 'respuesta' : 'respuestas'}` }}
            </button>
          </div>

          <!-- Formulario inline para escribir y enviar una respuesta al comentario -->
          <div v-if="estado.mostrandoForm" class="cn-reply-form">
            <textarea
              :value="estado.textoRespuesta"
              @input="emit('textoChange', comentario.id, ($event.target as HTMLTextAreaElement).value)"
              placeholder="Escribe tu respuesta..."
              rows="3"
              class="cn-reply-textarea"
            ></textarea>
            <div class="cn-reply-actions">
              <button class="cn-reply-cancel"
                @click="emit('toggleForm', comentario.id)" type="button">
                Cancelar
              </button>
              <button class="cn-reply-submit"
                @click="emit('enviarRespuesta', comentario.id)"
                :disabled="!estado.textoRespuesta?.trim() || estado.enviando"
                type="button">
                <span v-if="estado.enviando" class="cn-spin"></span>
                {{ estado.enviando ? 'Enviando...' : 'Responder' }}
              </button>
            </div>
          </div>

        </div>
      </div>

      <!-- Hilo de respuestas anidadas (recursivo), visible cuando está expandido -->
      <div v-if="hijos.length > 0 && estado.expandido" class="cn-hilo">
        <div class="cn-hilo-linea"></div>
        <div class="cn-hilo-respuestas">
          <!-- Renderizado recursivo del componente para soportar hilos de cualquier profundidad -->
          <ComentarioNodo
            v-for="hijo in hijos"
            :key="hijo.id"
            :comentario="hijo"
            :getHijos="getHijos"
            :estadoNodos="estadoNodos"
            :haySession="haySession"
            :formatFecha="formatFecha"
            :profundidad="(profundidad ?? 0) + 1"
            @votar="(id, voto) => emit('votar', id, voto)"
            @toggleForm="(id) => emit('toggleForm', id)"
            @toggleExpandido="(id) => emit('toggleExpandido', id)"
            @enviarRespuesta="(id) => emit('enviarRespuesta', id)"
            @textoChange="(id, txt) => emit('textoChange', id, txt)"
          />
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file Comentarionodo.vue
 * @description Componente recursivo para renderizar un comentario y sus respuestas
 * anidadas en forma de hilo. Soporta votación (útil/no útil), respuestas inline,
 * expansión de sub-hilos y calificación con estrellas para vuelos y hoteles.
 */
import { computed } from 'vue'
// @ts-ignore
import '../styles/ComentarioNodo.css'

defineOptions({ name: 'ComentarioNodo' })

const props = defineProps<{
  /** El objeto comentario con sus datos básicos y metadata de votación. */
  comentario: {
    id: number
    username: string
    nombreCompleto?: string
    /** Estrellas de valoración para vuelos. */
    cantidadEstrellas?: number | null
    /** Estrellas de valoración para hoteles. */
    resena?: number | null
    contenido: string
    fecha: string
    /** Score actual del comentario (ups - downs). */
    downs: number
    comentarioPadreId: number | null
  }
  /** Función que devuelve los comentarios hijo de un ID dado. */
  getHijos: (id: number) => any[]
  /** Mapa de estado de la UI para cada nodo, indexado por ID de comentario. */
  estadoNodos: Record<number, {
    expandido: boolean
    mostrandoForm: boolean
    textoRespuesta: string
    enviando: boolean
    votoActual: 1 | -1 | null
  }>
  /** Indica si el usuario actual tiene sesión activa (habilita votar y responder). */
  haySession: boolean
  /** Función formateadora de fechas inyectada desde el componente padre. */
  formatFecha: (fecha: string) => string
  /** Nivel de anidamiento actual, usado para limitar la indentación visual. */
  profundidad?: number
}>()

const emit = defineEmits<{
  (e: 'votar',           id: number, voto: 1 | -1): void
  (e: 'toggleForm',      id: number): void
  (e: 'toggleExpandido', id: number): void
  (e: 'enviarRespuesta', id: number): void
  (e: 'textoChange',     id: number, texto: string): void
}>()

/** Lista de comentarios hijo directos de este nodo. @type {import('vue').ComputedRef<Array>} */
const hijos      = computed(() => props.getHijos(props.comentario.id))

/**
 * Estado de la UI para este nodo específico.
 * Si no existe en el mapa, devuelve valores por defecto para evitar errores.
 * @type {import('vue').ComputedRef<Object>}
 */
const estado     = computed(() => props.estadoNodos[props.comentario.id] ?? {
  expandido: false, mostrandoForm: false,
  textoRespuesta: '', enviando: false, votoActual: null
})

/** Voto actual del usuario en este comentario (1, -1 o null). @type {import('vue').ComputedRef<1|-1|null>} */
const votoActual = computed(() => estado.value.votoActual)

/** Puntuación del comentario leída del campo 'downs'. @type {import('vue').ComputedRef<number>} */
const score      = computed(() => props.comentario.downs ?? 0)

/**
 * Clase CSS para colorear la puntuación según si es positiva, negativa o neutra.
 * @type {import('vue').ComputedRef<string>}
 */
const scoreClass = computed(() =>
  score.value > 0 ? 'cn-score--pos' : score.value < 0 ? 'cn-score--neg' : 'cn-score--zero'
)

/**
 * Número de estrellas a mostrar. Acepta cantidadEstrellas (vuelos) o resena (hoteles).
 * Devuelve null si no hay valoración disponible para ocultar la sección.
 * @type {import('vue').ComputedRef<number|null>}
 */
const estrellas  = computed(() => {
  const v = props.comentario.cantidadEstrellas ?? props.comentario.resena ?? null
  return (v !== null && v !== undefined) ? v : null
})
</script>
