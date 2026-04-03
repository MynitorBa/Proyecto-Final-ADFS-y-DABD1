<template>
  <div class="page">
    <Encabezado />

    <!-- Toasts -->
    <div class="prf-toast-stack">
      <div v-for="t in toasts" :key="t.id" :class="['prf-toast', t.tipo === 'error' ? 'prf-toast--error' : '']">
        <svg v-if="t.tipo === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><polyline points="20 6 9 17 4 12"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        {{ t.msg }}
      </div>
    </div>

    <div class="prf-page">
      <div class="prf-container">

        <!-- Loading -->
        <div v-if="loading" class="prf-loading">
          <div class="prf-spinner"></div>
          <span>Cargando tu perfil...</span>
        </div>

        <template v-else-if="perfil">

          <!-- Header -->
          <div class="prf-header">
            <div class="prf-avatar">
              <span class="prf-avatar__initials">{{ iniciales }}</span>
            </div>
            <div class="prf-header__info">
              <h1>{{ perfil.nombre }} {{ perfil.apellido }}</h1>
              <p>@{{ perfil.username }} · {{ perfil.correo }}</p>
            </div>
          </div>

          <!-- ══ INFORMACIÓN PERSONAL ══ -->
          <div class="prf-card">
            <div class="prf-card__head">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              Información personal
            </div>
            <div class="prf-card__body">
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
            </div>
          </div>

          <!-- ══ TELÉFONO ══ -->
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
                  <span class="prf-hint">{{ telPrefijo }} · {{ telDigitCount }} dígitos requeridos</span>
                </label>
                <div class="prf-input-wrap" :class="{ 'prf-input-wrap--focus': focusTel, 'prf-input-wrap--error': telError }">
                  <span class="prf-prefix-fixed">{{ telPrefijo }}</span>
                  <input class="prf-input" v-model="telNumero"
                    @input="onTelInput"
                    @focus="focusTel=true" @blur="focusTel=false"
                    :placeholder="telPlaceholder"
                    inputmode="numeric" type="text" />
                </div>
                <span v-if="telNumero && !telError">
                  <span v-if="telDigitsCount === telDigitCount" class="prf-success">✓ Número completo</span>
                  <span v-else class="prf-warn">{{ telDigitsCount }}/{{ telDigitCount }} dígitos</span>
                </span>
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

          <!-- ══ CONTRASEÑA ══ -->
          <div class="prf-card">
            <div class="prf-card__head">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              Cambiar contraseña
            </div>
            <div class="prf-card__body">
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
import { ref, computed, onMounted, reactive } from 'vue'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/Profile.css'

const API = 'http://localhost:8080'

// ── Estado ────────────────────────────────────────────────────────────────────
const perfil   = ref(null)
const loading  = ref(true)
const toasts   = ref([])

// Teléfono
const telPrefijo = ref('+502')
const telNumero   = ref('')
const telError    = ref('')
const focusTel    = ref(false)
const savingTel   = ref(false)

const telDigitCount  = computed(() => getDigitCount(telPrefijo.value))
const telDigitsCount = computed(() => telNumero.value.replace(/\D/g, '').length)
const telPlaceholder = computed(() => formatLocalPhone('5'.repeat(telDigitCount.value), telDigitCount.value))

function onTelInput(e) {
  const raw = e.target.value.replace(/\D/g, '').slice(0, telDigitCount.value)
  telNumero.value = formatLocalPhone(raw, telDigitCount.value)
}

// Contraseña
const pwd      = reactive({ actual: '', nueva: '', confirma: '' })
const pwdErrors = ref({})
const focusPwd  = ref('')
const savingPwd = ref(false)
const showPwd   = reactive({ actual: false, nueva: false, confirma: false })

// ── Computed ──────────────────────────────────────────────────────────────────
const iniciales = computed(() => {
  if (!perfil.value) return ''
  return (perfil.value.nombre?.[0] || '') + (perfil.value.apellido?.[0] || '')
})

// Prefijos por país
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

// Cantidad de dígitos del número local por código
const DIGIT_COUNTS = {
  '+502':8, '+52':10, '+503':8, '+504':8, '+505':8, '+506':8, '+507':8,
  '+57':10, '+58':10, '+593':9, '+51':9, '+591':8, '+56':9, '+54':10,
  '+598':8, '+595':9, '+55':11, '+53':8, '+34':9, '+1':10,
  '+49':10, '+33':9, '+39':10, '+86':11, '+81':10, '+91':10, '+61':9,
}

function getDialCode(pais) {
  if (!pais) return '+502'
  return DIAL_CODES[pais.toLowerCase()] ?? '+502'
}

function getDigitCount(code) {
  return DIGIT_COUNTS[code] ?? 8
}

// Formato local igual que en Registrarse.vue
function formatLocalPhone(digits, total) {
  if (total <= 7)   return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  if (total === 8)  return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  if (total === 9)  return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim()
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function addToast(msg, tipo = 'success') {
  const id = Date.now()
  toasts.value.push({ id, msg, tipo })
  setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 4000)
}

function formatFecha(f) {
  if (!f) return ''
  return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'long', year: 'numeric' })
}

async function apiFetch(url, opts = {}) {
  const res = await fetch(url, { credentials: 'include', ...opts })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.error || `Error ${res.status}`)
  return data
}

// ── Carga ─────────────────────────────────────────────────────────────────────
async function cargarPerfil() {
  loading.value = true
  try {
    perfil.value = await apiFetch(`${API}/api/perfil`)
    // Prefijo fijo según el país con que se registró — no se puede cambiar
    telPrefijo.value = getDialCode(perfil.value.pais)
    // Separar el número del prefijo si ya tiene teléfono guardado
    if (perfil.value.telefono) {
      const code = telPrefijo.value
      let soloNumero = ''
      if (perfil.value.telefono.startsWith(code)) {
        soloNumero = perfil.value.telefono.replace(code, '').trim().replace(/\D/g, '')
      } else {
        soloNumero = perfil.value.telefono.replace(/^\+\d+\s*/, '').replace(/\D/g, '')
      }
      // Aplicar formato visual al número existente
      telNumero.value = formatLocalPhone(soloNumero, getDigitCount(code))
    }
  } catch {
    addToast('No se pudo cargar el perfil', 'error')
  } finally {
    loading.value = false
  }
}

// ── Guardar teléfono ──────────────────────────────────────────────────────────
async function guardarTelefono() {
  telError.value = ''
  const numero = telNumero.value.trim()
  if (!numero) { telError.value = 'Ingresa el número de teléfono'; return }
  if (telDigitsCount.value !== telDigitCount.value) {
    telError.value = `Número incompleto: se requieren ${telDigitCount.value} dígitos (ingresaste ${telDigitsCount.value})`
    return
  }

  savingTel.value = true
  try {
    const telefonoCompleto = `${telPrefijo.value} ${numero}`
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

// ── Cambiar contraseña ────────────────────────────────────────────────────────
async function cambiarContrasena() {
  pwdErrors.value = {}
  const e = {}
  if (!pwd.actual)           e.actual   = 'Ingresa tu contraseña actual'
  if (!pwd.nueva)            e.nueva    = 'Ingresa la nueva contraseña'
  else if (pwd.nueva.length < 8) e.nueva = 'Mínimo 8 caracteres'
  if (!pwd.confirma)         e.confirma = 'Confirma la nueva contraseña'
  else if (pwd.nueva !== pwd.confirma) e.confirma = 'Las contraseñas no coinciden'
  if (Object.keys(e).length) { pwdErrors.value = e; return }

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
    if (err.message?.toLowerCase().includes('incorrecta')) {
      pwdErrors.value = { actual: 'La contraseña actual es incorrecta' }
    } else {
      addToast(err.message || 'Error al cambiar la contraseña', 'error')
    }
  } finally {
    savingPwd.value = false
  }
}

// ── Init ──────────────────────────────────────────────────────────────────────
onMounted(() => cargarPerfil())
</script>