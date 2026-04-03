<template>
  <div class="cn-nodo">
    <div class="cn-card">

      <!-- Layout: columna votos + cuerpo -->
      <div class="cn-layout">

        <!-- Columna de votos estilo Reddit -->
        <div class="cn-votes"
          :class="{ 'cn-votes--up': votoActual === 1, 'cn-votes--down': votoActual === -1 }">

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

          <span class="cn-score" :class="scoreClass">
            {{ score > 0 ? '+' : '' }}{{ score }}
          </span>

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

        <!-- Cuerpo del comentario -->
        <div class="cn-body">

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

          <!-- Estrellas: vuelo usa cantidadEstrellas, hotel usa resena -->
          <div v-if="estrellas !== null" class="cn-estrellas">
            <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
              :fill="n <= estrellas ? 'currentColor' : 'none'"
              stroke="currentColor" stroke-width="2"
              class="cn-estrella" :class="{ 'cn-estrella--llena': n <= estrellas }"
              width="14" height="14">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
          </div>

          <p class="cn-contenido">{{ comentario.contenido }}</p>

          <!-- Acciones -->
          <div class="cn-acciones">
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

          <!-- Formulario de respuesta -->
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

      <!-- Hijos recursivos -->
      <div v-if="hijos.length > 0 && estado.expandido" class="cn-hilo">
        <div class="cn-hilo-linea"></div>
        <div class="cn-hilo-respuestas">
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
import { computed } from 'vue'
// @ts-ignore
import '../styles/ComentarioNodo.css'

defineOptions({ name: 'ComentarioNodo' })

const props = defineProps<{
  comentario: {
    id: number
    username: string
    nombreCompleto?: string
    cantidadEstrellas?: number | null
    resena?: number | null
    contenido: string
    fecha: string
    downs: number
    comentarioPadreId: number | null
  }
  getHijos: (id: number) => any[]
  estadoNodos: Record<number, {
    expandido: boolean
    mostrandoForm: boolean
    textoRespuesta: string
    enviando: boolean
    votoActual: 1 | -1 | null
  }>
  haySession: boolean
  formatFecha: (fecha: string) => string
  profundidad?: number
}>()

const emit = defineEmits<{
  (e: 'votar',           id: number, voto: 1 | -1): void
  (e: 'toggleForm',      id: number): void
  (e: 'toggleExpandido', id: number): void
  (e: 'enviarRespuesta', id: number): void
  (e: 'textoChange',     id: number, texto: string): void
}>()

const hijos      = computed(() => props.getHijos(props.comentario.id))
const estado     = computed(() => props.estadoNodos[props.comentario.id] ?? {
  expandido: false, mostrandoForm: false,
  textoRespuesta: '', enviando: false, votoActual: null
})
const votoActual = computed(() => estado.value.votoActual)
const score      = computed(() => props.comentario.downs ?? 0)
const scoreClass = computed(() =>
  score.value > 0 ? 'cn-score--pos' : score.value < 0 ? 'cn-score--neg' : 'cn-score--zero'
)
const estrellas  = computed(() => {
  const v = props.comentario.cantidadEstrellas ?? props.comentario.resena ?? null
  return (v !== null && v !== undefined) ? v : null
})
</script>