<template>
  <div class="page">
    <Encabezado />

    <!-- Stack de notificaciones tipo toast (éxito y error) -->
    <div class="prf-toast-stack">
      <div v-for="t in toasts" :key="t.id" :class="['prf-toast', t.tipo === 'error' ? 'prf-toast--error' : '']">
        <svg v-if="t.tipo === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><polyline points="20 6 9 17 4 12"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        {{ t.msg }}
      </div>
    </div>

    <div class="prf-page">
      <div class="prf-container">

        <!-- Estado de carga mientras se obtiene el perfil del servidor -->
        <div v-if="loading" class="prf-loading">
          <div class="prf-spinner"></div>
          <span>Cargando tu perfil...</span>
        </div>

        <template v-else-if="perfil">

          <!-- Encabezado con avatar de iniciales y nombre del usuario -->
          <div class="prf-header">
            <div class="prf-avatar">
              <span class="prf-avatar__initials">{{ iniciales }}</span>
            </div>
            <div class="prf-header__info">
              <h1>{{ perfil.nombre }} {{ perfil.apellido }}</h1>
              <p>@{{ perfil.username }} · {{ perfil.correo }}</p>
            </div>
          </div>

          <!-- Tarjeta: información personal editable -->
          <div class="prf-card">
            <div class="prf-card__head" style="display:flex;align-items:center;justify-content:space-between;">
              <span style="display:flex;align-items:center;gap:6px;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                Información personal
              </span>
              <button v-if="!editandoInfo" type="button" class="prf-btn prf-btn--ghost" style="padding:4px 12px;font-size:12px;" @click="iniciarEdicionInfo">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Editar
              </button>
            </div>
            <div class="prf-card__body">

              <!-- Vista de lectura -->
              <template v-if="!editandoInfo">
                <div class="prf-info-grid">
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Nombre</span>
                    <span class="prf-info-cell__val">{{ perfil.nombre }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Apellido</span>
                    <span class="prf-info-cell__val">{{ perfil.apellido }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Username</span>
                    <span class="prf-info-cell__val">{{ perfil.username }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Correo</span>
                    <span class="prf-info-cell__val">{{ perfil.correo }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Pasaporte</span>
                    <span class="prf-info-cell__val">{{ perfil.pasaporte || '—' }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Fecha de nacimiento</span>
                    <span class="prf-info-cell__val">{{ formatFecha(perfil.fechaNacimiento) || '—' }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">Ciudad</span>
                    <span class="prf-info-cell__val">{{ perfil.ciudad || '—' }}</span>
                  </div>
                  <div class="prf-info-cell">
                    <span class="prf-info-cell__lbl">País</span>
                    <span class="prf-info-cell__val">{{ perfil.pais || '—' }}</span>
                  </div>
                  <div v-if="perfil.nacionalidades?.length" class="prf-info-cell prf-info-cell--full">
                    <span class="prf-info-cell__lbl">Nacionalidades</span>
                    <div class="prf-nac-tags">
                      <span v-for="n in perfil.nacionalidades" :key="n" class="prf-nac-tag">{{ n }}</span>
                    </div>
                  </div>
                </div>
              </template>

              <!-- Formulario de edición -->
              <template v-else>
                <div class="prf-info-grid">
                  <div class="prf-field">
                    <label class="prf-label">Nombre</label>
                    <div class="prf-input-wrap" :class="{ 'prf-input-wrap--error': infoErrors.nombre }">
                      <input class="prf-input" v-model="infoForm.nombre" placeholder="Nombre" />
                    </div>
                    <span v-if="infoErrors.nombre" class="prf-error">{{ infoErrors.nombre }}</span>
                  </div>
                  <div class="prf-field">
                    <label class="prf-label">Apellido</label>
                    <div class="prf-input-wrap" :class="{ 'prf-input-wrap--error': infoErrors.apellido }">
                      <input class="prf-input" v-model="infoForm.apellido" placeholder="Apellido" />
                    </div>
                    <span v-if="infoErrors.apellido" class="prf-error">{{ infoErrors.apellido }}</span>
                  </div>
                  <div class="prf-field">
                    <label class="prf-label">Username</label>
                    <div class="prf-input-wrap" :class="{ 'prf-input-wrap--error': infoErrors.username }">
                      <input class="prf-input" v-model="infoForm.username" placeholder="nombre_usuario" @input="infoForm.username = infoForm.username.replace(/[^a-zA-Z0-9_.]/g, '')" />
                    </div>
                    <span v-if="infoErrors.username" class="prf-error">{{ infoErrors.username }}</span>
                  </div>
                  <div class="prf-field">
                    <label class="prf-label">Correo</label>
                    <div class="prf-input-wrap" :class="{ 'prf-input-wrap--error': infoErrors.correo }">
                      <input class="prf-input" v-model="infoForm.correo" type="email" placeholder="correo@ejemplo.com" />
                    </div>
                    <span v-if="infoErrors.correo" class="prf-error">{{ infoErrors.correo }}</span>
                  </div>
                  <div class="prf-field">
                    <label class="prf-label">Pasaporte</label>
                    <div class="prf-input-wrap" :class="{ 'prf-input-wrap--error': infoErrors.pasaporte }">
                      <input class="prf-input" v-model="infoForm.pasaporte" placeholder="AB123456" @input="infoForm.pasaporte = infoForm.pasaporte.toUpperCase()" />
                    </div>
                    <span v-if="infoErrors.pasaporte" class="prf-error">{{ infoErrors.pasaporte }}</span>
                  </div>
                  <div class="prf-field">
                    <label class="prf-label">Fecha de nacimiento</label>
                    <div class="prf-input-wrap">
                      <input class="prf-input" v-model="infoForm.fechaNacimiento" type="date" />
                    </div>
                  </div>
                </div>

                <div style="display:flex;gap:10px;margin-top:16px;">
                  <button class="prf-btn prf-btn--primary" @click="guardarInfo" :disabled="savingInfo" type="button">
                    <span v-if="savingInfo" class="prf-btn__spin"></span>
                    <template v-else>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><polyline points="20 6 9 17 4 12"/></svg>
                      Guardar cambios
                    </template>
                  </button>
                  <button class="prf-btn prf-btn--ghost" @click="editandoInfo = false" :disabled="savingInfo" type="button">
                    Cancelar
                  </button>
                </div>
              </template>

            </div>
          </div>

          <!-- Tarjeta: preferencia de ofertas por correo -->
          <div class="prf-card">
            <div class="prf-card__head">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              Ofertas por correo
            </div>
            <div class="prf-card__body">
              <p style="font-size:.85rem;color:#6a6058;margin:0 0 16px;line-height:1.6;">
                Recibe cada 5 días las mejores ofertas de paquetes, vuelos y hoteles directamente en tu correo.
                Puedes activarlo o desactivarlo en cualquier momento.
              </p>
              <label style="display:flex;align-items:center;gap:14px;cursor:pointer;user-select:none;">
                <div style="position:relative;width:46px;height:26px;flex-shrink:0;" @click="toggleOfertas">
                  <input type="checkbox" :checked="perfil.recibirOfertas" style="opacity:0;position:absolute;" />
                  <div :style="{
                    width:'46px', height:'26px', borderRadius:'13px',
                    background: perfil.recibirOfertas ? '#FFCC00' : '#3a3028',
                    transition:'background .2s', position:'relative'
                  }">
                    <div :style="{
                      position:'absolute', top:'3px',
                      left: perfil.recibirOfertas ? '23px' : '3px',
                      width:'20px', height:'20px', borderRadius:'50%',
                      background: perfil.recibirOfertas ? '#1a1410' : '#6a6058',
                      transition:'left .2s'
                    }"></div>
                  </div>
                </div>
                <div>
                  <p style="margin:0;font-size:.9rem;font-weight:600;color:#f0e8dc;">
                    {{ perfil.recibirOfertas ? 'Suscrito a ofertas' : 'No suscrito' }}
                  </p>
                  <p style="margin:0;font-size:.78rem;color:#6a6058;">
                    {{ perfil.recibirOfertas ? 'Recibirás ofertas cada 5 días por correo' : 'Actívalo para recibir promociones exclusivas' }}
                  </p>
                </div>
              </label>
            </div>
          </div>

          <!-- Tarjeta: edición del número de teléfono -->
          <div class="prf-card">
            <div class="prf-card__head">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
              Teléfono
            </div>
            <div class="prf-card__body">
              <p style="font-size:.85rem;color:#6a6058;margin:0 0 1rem;">
                Actual: <strong>{{ perfil.telefono || 'No registrado' }}</strong>
              </p>
              <div class="prf-field">
                <label class="prf-label">
                  Nuevo número
                  <span class="prf-hint">{{ telPrefijo }} · {{ telDigitCount }} dígitos</span>
                </label>
                <!-- Input con prefijo fijo según el país de registro -->
                <div class="prf-input-wrap" :class="{ 'prf-input-wrap--focus': focusTel, 'prf-input-wrap--error': telError }">
                  <span class="prf-prefix-fixed">{{ telPrefijo }}</span>
                  <input class="prf-input" v-model="telNumero"
                    @input="onTelInput"
                    @focus="focusTel=true" @blur="focusTel=false"
                    :placeholder="telPlaceholder"
                    inputmode="numeric" type="text" />
                </div>
                <span v-if="telError" class="prf-error">{{ telError }}</span>
              </div>
              <button class="prf-btn prf-btn--primary" @click="guardarTelefono" :disabled="savingTel" type="button">
                <span v-if="savingTel" class="prf-btn__spin"></span>
                <template v-else>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><polyline points="20 6 9 17 4 12"/></svg>
                  Guardar teléfono
                </template>
              </button>
            </div>
          </div>

          <!-- Tarjeta: formulario para cambiar la contraseña actual -->
          <div class="prf-card">
            <div class="prf-card__head">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              Cambiar contraseña
            </div>
            <div class="prf-card__body">

              <!-- Campo: contraseña actual con toggle de visibilidad -->
              <div class="prf-field">
                <label class="prf-label">Contraseña actual</label>
                <div class="prf-input-wrap" :class="{ 'prf-input-wrap--focus': focusPwd==='actual', 'prf-input-wrap--error': pwdErrors.actual }">
                  <div class="prf-input-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></div>
                  <input class="prf-input" v-model="pwd.actual" :type="showPwd.actual ? 'text' : 'password'"
                    @focus="focusPwd='actual'" @blur="focusPwd=''" placeholder="Tu contraseña actual" />
                  <button type="button" class="prf-input-ico" style="cursor:pointer;background:none;border:none;"
                    @click="showPwd.actual = !showPwd.actual">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
                      <path v-if="showPwd.actual" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22"/>
                      <path v-else d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="!showPwd.actual" cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <span v-if="pwdErrors.actual" class="prf-error">{{ pwdErrors.actual }}</span>
              </div>

              <!-- Campo: nueva contraseña con toggle de visibilidad -->
              <div class="prf-field">
                <label class="prf-label">Nueva contraseña <span class="prf-hint">(mín. 8 caracteres)</span></label>
                <div class="prf-input-wrap" :class="{ 'prf-input-wrap--focus': focusPwd==='nueva', 'prf-input-wrap--error': pwdErrors.nueva }">
                  <div class="prf-input-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
                  <input class="prf-input" v-model="pwd.nueva" :type="showPwd.nueva ? 'text' : 'password'"
                    @focus="focusPwd='nueva'" @blur="focusPwd=''" placeholder="Nueva contraseña" />
                  <button type="button" class="prf-input-ico" style="cursor:pointer;background:none;border:none;"
                    @click="showPwd.nueva = !showPwd.nueva">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
                      <path v-if="showPwd.nueva" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22"/>
                      <path v-else d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="!showPwd.nueva" cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <span v-if="pwdErrors.nueva" class="prf-error">{{ pwdErrors.nueva }}</span>
              </div>

              <!-- Campo: confirmación de nueva contraseña con indicador de coincidencia -->
              <div class="prf-field">
                <label class="prf-label">Confirmar nueva contraseña</label>
                <div class="prf-input-wrap" :class="{ 'prf-input-wrap--focus': focusPwd==='confirma', 'prf-input-wrap--error': pwdErrors.confirma }">
                  <div class="prf-input-ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
                  <input class="prf-input" v-model="pwd.confirma" :type="showPwd.confirma ? 'text' : 'password'"
                    @focus="focusPwd='confirma'" @blur="focusPwd=''" placeholder="Repite la nueva contraseña" />
                  <button type="button" class="prf-input-ico" style="cursor:pointer;background:none;border:none;"
                    @click="showPwd.confirma = !showPwd.confirma">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
                      <path v-if="showPwd.confirma" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24M1 1l22 22"/>
                      <path v-else d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="!showPwd.confirma" cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <span v-if="pwdErrors.confirma" class="prf-error">{{ pwdErrors.confirma }}</span>
                <!-- Indicador de coincidencia -->
                <span v-if="pwd.confirma && pwd.nueva && pwd.confirma === pwd.nueva" class="prf-success">
                  ✓ Las contraseñas coinciden
                </span>
              </div>

              <button class="prf-btn prf-btn--primary" @click="cambiarContrasena" :disabled="savingPwd" type="button">
                <span v-if="savingPwd" class="prf-btn__spin"></span>
                <template v-else>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  Cambiar contraseña
                </template>
              </button>
            </div>
          </div>

        </template>

      </div>
    </div>
    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Profile.vue
 * @description Vista del perfil de usuario autenticado. Muestra su información personal
 * de solo lectura y permite editar el número de teléfono y cambiar la contraseña.
 * Todas las validaciones de datos (formato, longitud, unicidad) las hace el backend;
 * este archivo solo limita la entrada (solo dígitos, máximo según país) y muestra
 * los errores que devuelve el servidor.
 */
import { ref, computed, onMounted, reactive } from 'vue'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/Profile.css'

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Datos del perfil cargados desde el servidor. @type {import('vue').Ref<object|null>} */
const perfil = ref(null)

/** Indica si la petición de perfil sigue en curso. @type {import('vue').Ref<boolean>} */
const loading = ref(true)

/** Lista de notificaciones activas en pantalla. @type {import('vue').Ref<Array>} */
const toasts = ref([])

/** Prefijo telefónico del país con que se registró el usuario (ej. '+502'). @type {import('vue').Ref<string>} */
const telPrefijo = ref('+502')

/** Número local que el usuario escribe en el campo de teléfono. @type {import('vue').Ref<string>} */
const telNumero = ref('')

/** Mensaje de error del campo teléfono (viene del backend). @type {import('vue').Ref<string>} */
const telError = ref('')

/** Controla el estado de foco visual del input de teléfono. @type {import('vue').Ref<boolean>} */
const focusTel = ref(false)

/** Previene doble click en el botón guardar teléfono. @type {import('vue').Ref<boolean>} */
const savingTel = ref(false)

/** Cantidad de dígitos requeridos según el prefijo del país (solo para limitar entrada). @type {import('vue').ComputedRef<number>} */
const telDigitCount = computed(() => getDigitCount(telPrefijo.value))

/** Placeholder visual con el formato esperado. @type {import('vue').ComputedRef<string>} */
const telPlaceholder = computed(() => formatLocalPhone('5'.repeat(telDigitCount.value), telDigitCount.value))

/**
 * Maneja el evento input del campo teléfono: solo permite dígitos y aplica
 * formato visual. No valida nada — el backend se encarga de eso.
 * @param {Event} e
 */
function onTelInput(e) {
  const raw = e.target.value.replace(/\D/g, '').slice(0, telDigitCount.value)
  telNumero.value = formatLocalPhone(raw, telDigitCount.value)
  // Limpiar error al empezar a escribir de nuevo
  if (telError.value) telError.value = ''
}

/** Campos de la nueva contraseña agrupados en un objeto reactivo. */
const pwd = reactive({ actual: '', nueva: '', confirma: '' })

/** Errores de validación del formulario de contraseña por campo (vienen del backend). @type {import('vue').Ref<object>} */
const pwdErrors = ref({})

/** Campo que actualmente tiene el foco en el bloque de contraseña. @type {import('vue').Ref<string>} */
const focusPwd = ref('')

/** Previene doble envío al guardar la contraseña. @type {import('vue').Ref<boolean>} */
const savingPwd = ref(false)

/** Controla la visibilidad de cada campo de contraseña. */
const showPwd = reactive({ actual: false, nueva: false, confirma: false })

/**
 * Genera las iniciales del usuario a partir de nombre y apellido.
 * @type {import('vue').ComputedRef<string>}
 */
const iniciales = computed(() => {
  if (!perfil.value) return ''
  return (perfil.value.nombre?.[0] || '') + (perfil.value.apellido?.[0] || '')
})

/**
 * Mapa de prefijos telefónicos internacionales por nombre de país en minúsculas.
 * Se usa solo para mostrar el prefijo; el backend revalida el formato completo.
 * @type {Record<string, string>}
 */
const DIAL_CODES = {
  'guatemala': '+502', 'méxico': '+52', 'mexico': '+52',
  'el salvador': '+503', 'honduras': '+504', 'nicaragua': '+505',
  'costa rica': '+506', 'panamá': '+507', 'panama': '+507',
  'colombia': '+57', 'venezuela': '+58', 'ecuador': '+593',
  'perú': '+51', 'peru': '+51', 'bolivia': '+591',
  'chile': '+56', 'argentina': '+54', 'uruguay': '+598',
  'paraguay': '+595', 'brasil': '+55', 'brazil': '+55',
  'república dominicana': '+1', 'cuba': '+53', 'españa': '+34',
  'spain': '+34', 'united states': '+1', 'estados unidos': '+1',
  'germany': '+49', 'france': '+33', 'italy': '+39',
  'china': '+86', 'japan': '+81', 'india': '+91',
  'canada': '+1', 'australia': '+61',
}

/**
 * Cantidad de dígitos locales por código (solo para limitar entrada en UI).
 * El backend revalida con su propio mapa.
 * @type {Record<string, number>}
 */
const DIGIT_COUNTS = {
  '+502':8, '+52':10, '+503':8, '+504':8, '+505':8, '+506':8, '+507':8,
  '+57':10, '+58':10, '+593':9, '+51':9, '+591':8, '+56':9, '+54':10,
  '+598':8, '+595':9, '+55':11, '+53':8, '+34':9, '+1':10,
  '+49':10, '+33':9, '+39':10, '+86':11, '+81':10, '+91':10, '+61':9,
}

/**
 * Devuelve el prefijo de marcación para un país dado.
 * @param {string} pais
 * @returns {string}
 */
function getDialCode(pais) {
  if (!pais) return '+502'
  return DIAL_CODES[pais.toLowerCase()] ?? '+502'
}

/**
 * Devuelve la cantidad de dígitos locales que requiere un código.
 * @param {string} code
 * @returns {number}
 */
function getDigitCount(code) {
  return DIGIT_COUNTS[code] ?? 8
}

/**
 * Formatea una cadena de dígitos como número local según su longitud total.
 * @param {string} digits - Solo dígitos
 * @param {number} total  - Cantidad total esperada
 * @returns {string}
 */
function formatLocalPhone(digits, total) {
  if (total <= 7)   return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  if (total === 8)  return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  if (total === 9)  return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim()
}

/**
 * Agrega un toast a la pila y lo elimina automáticamente a los 4 segundos.
 * @param {string} msg
 * @param {'success'|'error'} tipo
 */
function addToast(msg, tipo = 'success') {
  const id = Date.now()
  toasts.value.push({ id, msg, tipo })
  setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 4000)
}

/**
 * Formatea una fecha ISO como texto legible en español guatemalteco.
 * @param {string} f
 * @returns {string}
 */
function formatFecha(f) {
  if (!f) return ''
  return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'long', year: 'numeric' })
}

/**
 * Wrapper de fetch que incluye credenciales y lanza error con el mensaje del backend.
 * @param {string} url
 * @param {RequestInit} opts
 * @returns {Promise<any>}
 */
async function apiFetch(url, opts = {}) {
  const res = await fetch(url, { credentials: 'include', ...opts })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.error || `Error ${res.status}`)
  return data
}

/**
 * Carga los datos del perfil desde la API y prepara el campo de teléfono
 * con el prefijo correcto y el número ya formateado si existía.
 */
async function cargarPerfil() {
  loading.value = true
  try {
    perfil.value = await apiFetch(`${API}/api/perfil`)
    telPrefijo.value = getDialCode(perfil.value.pais)
    if (perfil.value.telefono) {
      const code = telPrefijo.value
      let soloNumero = ''
      if (perfil.value.telefono.startsWith(code)) {
        soloNumero = perfil.value.telefono.replace(code, '').trim().replace(/\D/g, '')
      } else {
        soloNumero = perfil.value.telefono.replace(/^\+\d+\s*/, '').replace(/\D/g, '')
      }
      telNumero.value = formatLocalPhone(soloNumero, getDigitCount(code))
    }
  } catch {
    addToast('No se pudo cargar el perfil', 'error')
  } finally {
    loading.value = false
  }
}

/**
 * Envía el nuevo número de teléfono al servidor. No valida localmente;
 * el backend se encarga de todas las reglas (formato, longitud, unicidad,
 * que sea distinto al actual).
 */
async function guardarTelefono() {
  telError.value = ''
  savingTel.value = true
  try {
    const telefonoCompleto = `${telPrefijo.value} ${telNumero.value.trim()}`
    await apiFetch(`${API}/api/perfil/telefono`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ telefono: telefonoCompleto })
    })
    perfil.value.telefono = telefonoCompleto
    addToast('Teléfono actualizado correctamente')
  } catch (err) {
    telError.value = err.message || 'Error al actualizar el teléfono'
  } finally {
    savingTel.value = false
  }
}

/**
 * Envía el cambio de contraseña al servidor. El backend valida todas las
 * reglas y devuelve el error correspondiente que se muestra en el campo
 * apropiado.
 */
async function cambiarContrasena() {
  pwdErrors.value = {}
  savingPwd.value = true
  try {
    await apiFetch(`${API}/api/perfil/contrasena`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ actual: pwd.actual, nueva: pwd.nueva, confirma: pwd.confirma })
    })
    pwd.actual = ''; pwd.nueva = ''; pwd.confirma = ''
    addToast('Contraseña actualizada correctamente')
  } catch (err) {
    const msg = (err.message || '').toLowerCase()
    // Asignar el error al campo correspondiente según lo que dice el backend
    if (msg.includes('actual') || msg.includes('incorrecta')) {
      pwdErrors.value = { actual: err.message }
    } else if (msg.includes('nueva') || msg.includes('mínimo') || msg.includes('minimo')) {
      pwdErrors.value = { nueva: err.message }
    } else if (msg.includes('coinciden') || msg.includes('confirma')) {
      pwdErrors.value = { confirma: err.message }
    } else {
      addToast(err.message || 'Error al cambiar la contraseña', 'error')
    }
  } finally {
    savingPwd.value = false
  }
}

// ── Edición de información personal ──────────────────────────────────────────

/** Controla si se muestra el formulario de edicion o la vista de lectura. */
const editandoInfo = ref(false)

/** Previene doble envio al guardar informacion personal. */
const savingInfo = ref(false)

/** Errores por campo del formulario de informacion personal. */
const infoErrors = ref({})

/** Datos del formulario de edicion de informacion personal. */
const infoForm = ref({
  nombre: '', apellido: '', correo: '', username: '',
  pasaporte: '', fechaNacimiento: ''
})

/**
 * Copia los valores actuales del perfil al formulario antes de abrir el editor.
 */
function iniciarEdicionInfo() {
  infoForm.value = {
    nombre:          perfil.value.nombre         || '',
    apellido:        perfil.value.apellido        || '',
    correo:          perfil.value.correo          || '',
    username:        perfil.value.username        || '',
    pasaporte:       perfil.value.pasaporte       || '',
    fechaNacimiento: perfil.value.fechaNacimiento
      ? perfil.value.fechaNacimiento.slice(0, 10)
      : '',
  }
  infoErrors.value = {}
  editandoInfo.value = true
}

/**
 * Envía los cambios de informacion personal al servidor.
 * Muestra errores por campo si hay duplicados (409).
 */
async function guardarInfo() {
  infoErrors.value = {}
  savingInfo.value = true
  try {
    await apiFetch(`${API}/api/perfil/info`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre:           infoForm.value.nombre.trim(),
        apellido:         infoForm.value.apellido.trim(),
        correo:           infoForm.value.correo.trim().toLowerCase(),
        username:         infoForm.value.username.trim(),
        pasaporte:        infoForm.value.pasaporte.trim().toUpperCase(),
        fecha_nacimiento: infoForm.value.fechaNacimiento,
      })
    })
    // Actualizar el objeto local del perfil con los nuevos valores
    perfil.value.nombre          = infoForm.value.nombre.trim()
    perfil.value.apellido        = infoForm.value.apellido.trim()
    perfil.value.correo          = infoForm.value.correo.trim().toLowerCase()
    perfil.value.username        = infoForm.value.username.trim()
    perfil.value.pasaporte       = infoForm.value.pasaporte.trim().toUpperCase()
    perfil.value.fechaNacimiento = infoForm.value.fechaNacimiento
    editandoInfo.value = false
    addToast('Información actualizada correctamente')
  } catch (err) {
    const msg = (err.message || '').toLowerCase()
    if (msg.includes('correo'))    infoErrors.value.correo    = err.message
    else if (msg.includes('username')) infoErrors.value.username = err.message
    else if (msg.includes('pasaporte')) infoErrors.value.pasaporte = err.message
    else addToast(err.message || 'Error al actualizar', 'error')
  } finally {
    savingInfo.value = false
  }
}

// ── Toggle de ofertas ─────────────────────────────────────────────────────────

/**
 * Cambia la suscripcion de ofertas del usuario y lo persiste en el servidor.
 */
async function toggleOfertas() {
  const nuevo = !perfil.value.recibirOfertas
  try {
    await apiFetch(`${API}/api/perfil/ofertas`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ recibir_ofertas: nuevo })
    })
    perfil.value.recibirOfertas = nuevo
    addToast(nuevo
      ? 'Te has suscrito a las ofertas de Movent'
      : 'Te has dado de baja de las ofertas')
  } catch (err) {
    addToast(err.message || 'Error al actualizar preferencia', 'error')
  }
}

/** Carga el perfil al montar la vista. */
onMounted(() => cargarPerfil())
</script>