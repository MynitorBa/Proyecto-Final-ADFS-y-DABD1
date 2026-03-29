<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">

        <button class="back-link" @click="$router.push('/principal')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          Volver al inicio
        </button>

        <div class="register-header">
          <img src="/movent.png" alt="Movent" class="register-logo" />
          <h2>Crear tu Cuenta</h2>
          <p>Únete a Movent y comienza a reservar experiencias de viaje</p>
        </div>

        <!-- Success -->
        <div v-if="registrationSuccess" class="success-box">
          <div class="success-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <h3>¡Cuenta Creada!</h3>
          <p>Redirigiendo al inicio de sesión...</p>
          <div class="loading-dots"><span></span><span></span><span></span></div>
        </div>

        <form v-else @submit.prevent="handleRegister" class="register-form">

          <!-- ── Información Personal ── -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              Información Personal
            </h3>

            <div class="form-grid-2">
              <div class="form-field">
                <label>Nombre <span class="req">*</span></label>
                <input type="text" v-model="formData.firstName" placeholder="Nombres"
                  :class="{ error: errors.firstName }" autocomplete="given-name" />
                <span v-if="errors.firstName" class="error-text">{{ errors.firstName }}</span>
              </div>
              <div class="form-field">
                <label>Apellidos <span class="req">*</span></label>
                <input type="text" v-model="formData.lastName" placeholder="Apellidos"
                  :class="{ error: errors.lastName }" autocomplete="family-name" />
                <span v-if="errors.lastName" class="error-text">{{ errors.lastName }}</span>
              </div>
            </div>

            <div class="form-grid-2">
              <div class="form-field">
                <label>Fecha de Nacimiento <span class="req">*</span></label>
                <input type="date" v-model="formData.birthDate" :class="{ error: errors.birthDate }" />
                <span v-if="formData.birthDate && userAge >= 18" class="match-ok">✓ {{ userAge }} años</span>
                <span v-else-if="formData.birthDate && userAge < 18" class="match-no">✗ Debes tener al menos 18 años</span>
                <span v-if="errors.birthDate" class="error-text">{{ errors.birthDate }}</span>
              </div>
              <div class="form-field">
                <label>Número de Pasaporte <span class="req">*</span></label>
                <input type="text" v-model="formData.pasaporte" placeholder="AB123456"
                  :class="{ error: errors.pasaporte }" autocomplete="off" />
                <span v-if="errors.pasaporte" class="error-text">{{ errors.pasaporte }}</span>
              </div>
            </div>

            <!-- País + Teléfono -->
            <div class="form-grid-2">
              <div class="form-field">
                <label>País <span class="req">*</span></label>
                <div class="autocomplete-wrap">
                  <input type="text" v-model="paisQuery" @input="onPaisInput" @blur="blurPais"
                    placeholder="Escribe tu país..." :class="{ error: errors.country }" autocomplete="off" />
                  <ul v-if="paisesSugeridos.length > 0" class="autocomplete-list">
                    <li v-for="p in paisesSugeridos" :key="p.country">
                      <button type="button" class="autocomplete-btn" @click="seleccionarPais(p)">{{ p.country }}</button>
                    </li>
                  </ul>
                </div>
                <span v-if="errors.country" class="error-text">{{ errors.country }}</span>
              </div>

              <div class="form-field">
                <label>
                  Teléfono <span class="req">*</span>
                  <span v-if="dialCode" class="label-hint">— {{ phoneDigitCount }} dígitos requeridos</span>
                </label>
                <div class="phone-field" :class="{ error: errors.phone }">
                  <span v-if="dialCode" class="phone-prefix">{{ dialCode }}</span>
                  <input type="tel" v-model="formData.phone" @input="onPhoneInput"
                    :placeholder="dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un país primero'"
                    :disabled="!dialCode" autocomplete="tel" />
                </div>
                <span v-if="formData.phone && !errors.phone">
                  <span v-if="phoneDigits === phoneDigitCount" class="match-ok">✓ Número completo</span>
                  <span v-else class="match-no">{{ phoneDigits }}/{{ phoneDigitCount }} dígitos</span>
                </span>
                <span v-if="errors.phone" class="error-text">{{ errors.phone }}</span>
              </div>
            </div>

            <!-- Ciudad -->
            <div class="form-field">
              <label>Ciudad <span class="req">*</span>
                <span v-if="ciudadLoading" class="label-hint">— Cargando ciudades...</span>
              </label>
              <div class="autocomplete-wrap">
                <input type="text" v-model="ciudadQuery" @input="onCiudadInput" @blur="blurCiudad"
                  :placeholder="!paisSeleccionado ? 'Primero selecciona un país' : ciudadLoading ? 'Cargando...' : 'Escribe tu ciudad...'"
                  :disabled="!paisSeleccionado || ciudadLoading"
                  :class="{ error: errors.city }" autocomplete="off" />
                <ul v-if="ciudadesSugeridas.length > 0" class="autocomplete-list">
                  <li v-for="c in ciudadesSugeridas" :key="c">
                    <button type="button" class="autocomplete-btn" @click="seleccionarCiudad(c)">{{ c }}</button>
                  </li>
                </ul>
              </div>
              <span v-if="errors.city" class="error-text">{{ errors.city }}</span>
            </div>

            <!-- Nacionalidades (múltiples, desde API) -->
            <div class="form-field">
              <label>Nacionalidad(es) <span class="req">*</span></label>
              <div v-for="(nac, i) in nacionalidades" :key="i" class="nac-row">
                <div class="autocomplete-wrap" style="flex:1">
                  <input type="text" v-model="nacionalidades[i].query"
                    @input="onNacInput(i)" @blur="blurNac(i)"
                    placeholder="Ej: Guatemalteca"
                    :class="{ error: errors.nacionalidades && !nacionalidades[i].seleccionada }"
                    autocomplete="off" />
                  <ul v-if="nacionalidades[i].sugerencias.length > 0" class="autocomplete-list">
                    <li v-for="s in nacionalidades[i].sugerencias" :key="s.pais">
                      <button type="button" class="autocomplete-btn" @click="seleccionarNac(i, s)">
                        {{ s.pais }} — {{ s.demonym }}
                      </button>
                    </li>
                  </ul>
                </div>
                <button v-if="i > 0" type="button" class="btn-quitar" @click="quitarNac(i)">✕</button>
              </div>
              <button type="button" class="link-btn" style="margin-top:0.5rem;font-size:0.875rem;" @click="agregarNac">
                + Agregar otra nacionalidad
              </button>
              <span v-if="errors.nacionalidades" class="error-text">{{ errors.nacionalidades }}</span>
            </div>
          </div>

          <!-- ── Credenciales ── -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              Credenciales de Acceso
            </h3>

            <div class="form-field">
              <label>Nombre de Usuario <span class="req">*</span></label>
              <div class="input-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <input type="text" v-model="formData.username" @input="onUsernameInput"
                  placeholder="Ej: viajero2026" :class="{ error: errors.username }" autocomplete="username" />
              </div>
              <span v-if="formData.username && !errors.username" class="match-ok">Tu usuario será: <strong>{{ formData.username }}</strong></span>
              <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
            </div>

            <div class="form-field">
              <label>Correo Electrónico <span class="req">*</span></label>
              <div class="input-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                <input type="email" v-model="formData.email" placeholder="tu@email.com"
                  :class="{ error: errors.email }" autocomplete="email" />
              </div>
              <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
            </div>

            <div class="form-grid-2">
              <div class="form-field">
                <label>Contraseña <span class="req">*</span></label>
                <div class="password-wrap">
                  <input :type="showPassword ? 'text' : 'password'" v-model="formData.password"
                    placeholder="Mínimo 8 caracteres" :class="{ error: errors.password }" autocomplete="new-password" />
                  <button type="button" class="toggle-pass" @click="showPassword = !showPassword" tabindex="-1">
                    <svg v-if="showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                  </button>
                </div>
                <div v-if="formData.password" class="strength-bar-wrap">
                  <div class="strength-bar"><div class="strength-fill" :style="{ width: passwordStrength.width, background: passwordStrength.color }"></div></div>
                  <span class="strength-label" :style="{ color: passwordStrength.color }">{{ passwordStrength.text }}</span>
                </div>
                <div class="requirements">
                  <span :class="['req-item', { met: passVal.minLength }]">✓ 8 caracteres</span>
                  <span :class="['req-item', { met: passVal.hasUpperCase }]">✓ Mayúscula</span>
                  <span :class="['req-item', { met: passVal.hasLowerCase }]">✓ Minúscula</span>
                  <span :class="['req-item', { met: passVal.hasNumber }]">✓ Número</span>
                </div>
                <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
              </div>

              <div class="form-field">
                <label>Confirmar Contraseña <span class="req">*</span></label>
                <div class="password-wrap">
                  <input :type="showConfirmPassword ? 'text' : 'password'" v-model="formData.confirmPassword"
                    placeholder="Repite tu contraseña" :class="{ error: errors.confirmPassword }" autocomplete="new-password" />
                  <button type="button" class="toggle-pass" @click="showConfirmPassword = !showConfirmPassword" tabindex="-1">
                    <svg v-if="showConfirmPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                  </button>
                </div>
                <span v-if="formData.confirmPassword && formData.password === formData.confirmPassword" class="match-ok">✓ Contraseñas coinciden</span>
                <span v-else-if="formData.confirmPassword" class="match-no">✗ No coinciden</span>
                <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
              </div>
            </div>
          </div>

          <!-- Términos -->
          <div class="terms-section">
            <label class="checkbox-label">
              <input type="checkbox" v-model="acceptTerms" />
              <span class="checkbox-custom"></span>
              <span>Acepto los <button type="button" class="link-btn">Términos y Condiciones</button> <span class="req">*</span></span>
            </label>
            <span v-if="errors.terms" class="error-text">{{ errors.terms }}</span>

            <label class="checkbox-label">
              <input type="checkbox" v-model="acceptPrivacy" />
              <span class="checkbox-custom"></span>
              <span>Acepto la <button type="button" class="link-btn">Política de Privacidad</button> <span class="req">*</span></span>
            </label>
            <span v-if="errors.privacy" class="error-text">{{ errors.privacy }}</span>
          </div>

          <span v-if="errors.submit" class="error-text" style="text-align:center;">{{ errors.submit }}</span>

          <button type="submit" class="submit-btn" :disabled="isSubmitting">
            <svg v-if="isSubmitting" class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
            {{ isSubmitting ? 'Creando cuenta...' : 'Crear Cuenta' }}
          </button>

          <p class="footer-text">
            ¿Ya tienes una cuenta?
            <router-link to="/ingreso" class="link-btn">Inicia sesión aquí</router-link>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import '../styles/registrarse.css'

const API_BASE = 'http://localhost:8080'
const router   = useRouter()

// ── Dial codes (ITU) ────────────────────────────────────────────────
const knownDigits = {
  '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,'+31':9,'+32':9,'+33':9,'+34':9,
  '+36':9,'+39':10,'+40':9,'+41':9,'+43':10,'+44':10,'+45':8,'+46':9,'+47':8,
  '+48':9,'+49':10,'+51':9,'+52':10,'+53':8,'+54':10,'+55':11,'+56':9,'+57':10,
  '+58':10,'+60':9,'+61':9,'+62':9,'+63':10,'+64':9,'+65':8,'+66':9,'+81':10,
  '+82':10,'+84':9,'+86':11,'+90':10,'+91':10,'+92':10,'+93':9,'+94':9,'+95':8,
  '+98':10,'+212':9,'+213':9,'+216':8,'+218':9,'+220':7,'+221':9,'+222':8,
  '+223':8,'+224':9,'+225':8,'+226':8,'+227':8,'+228':8,'+229':8,'+230':8,
  '+231':8,'+232':8,'+233':9,'+234':10,'+237':9,'+241':8,'+242':9,'+243':9,
  '+244':9,'+249':9,'+250':9,'+251':9,'+252':8,'+253':8,'+254':9,'+255':9,
  '+256':9,'+257':8,'+258':9,'+260':9,'+261':9,'+263':9,'+264':9,'+265':9,
  '+266':8,'+267':8,'+268':8,'+291':7,'+351':9,'+352':9,'+353':9,'+354':7,
  '+355':9,'+356':8,'+357':8,'+358':9,'+359':9,'+370':8,'+371':8,'+372':8,
  '+373':8,'+374':8,'+375':9,'+380':9,'+381':9,'+385':9,'+386':8,'+387':8,
  '+389':8,'+420':9,'+421':9,'+501':7,'+502':8,'+503':8,'+504':8,'+505':8,
  '+506':8,'+507':8,'+509':8,'+591':8,'+592':7,'+593':9,'+595':9,'+597':7,
  '+598':8,'+855':9,'+856':10,'+880':10,'+886':9,'+960':7,'+961':8,'+962':9,
  '+963':9,'+964':10,'+965':8,'+966':9,'+967':9,'+968':8,'+970':9,'+971':9,
  '+972':9,'+973':8,'+974':8,'+975':8,'+976':8,'+977':10,'+992':9,'+993':8,
  '+994':9,'+995':9,'+996':9,'+998':9,
}

// ── Datos de API (cargados en onMounted) ────────────────────────────
const todosLosPaises     = ref([])  // [{ country, cities[] }] — countriesnow
const dialCodesMap       = ref({})  // { 'guatemala': { code: '+502', digits: 8 } }
const todosNacionalidades= ref([])  // [{ pais: 'Guatemala', demonym: 'Guatemalan' }]

onMounted(async () => {
  // Países + ciudades
  try {
    const res  = await fetch('https://countriesnow.space/api/v0.1/countries')
    const data = await res.json()
    todosLosPaises.value = data.data || []
  } catch { console.error('Error cargando países') }

  // Dial codes + demónimos de nacionalidad (misma llamada, igual que el Svelte)
  try {
    const res  = await fetch('https://restcountries.com/v3.1/all?fields=name,demonyms,idd')
    const data = await res.json()

    data.forEach(p => {
      if (p.idd?.root) {
        const suffixes = p.idd.suffixes ?? ['']
        const code     = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root
        const digits   = knownDigits[code] ?? 9
        const key      = p.name.common.toLowerCase()
        dialCodesMap.value[key] = { code, digits }
        if (p.name.official)
          dialCodesMap.value[p.name.official.toLowerCase()] = { code, digits }
      }
    })

    todosNacionalidades.value = data
      .filter(p => p.demonyms?.eng?.m)
      .map(p => ({ pais: p.name.common, demonym: p.demonyms.eng.m }))
      .sort((a, b) => a.pais.localeCompare(b.pais))
  } catch { console.error('Error cargando nationalidades / dial codes') }
})

// ── Form state ──────────────────────────────────────────────────────
const formData = ref({
  firstName: '', lastName: '', birthDate: '', pasaporte: '',
  phone: '', username: '', email: '', password: '', confirmPassword: ''
})

const showPassword        = ref(false)
const showConfirmPassword = ref(false)
const acceptTerms         = ref(false)
const acceptPrivacy       = ref(false)
const errors              = ref({})
const isSubmitting        = ref(false)
const registrationSuccess = ref(false)

// ── País ────────────────────────────────────────────────────────────
const paisQuery        = ref('')
const paisesSugeridos  = ref([])
const paisSeleccionado = ref(null)

// ── Ciudad ──────────────────────────────────────────────────────────
const ciudadQuery        = ref('')
const ciudadesSugeridas  = ref([])
const ciudadSeleccionada = ref(false)
const ciudadLoading      = ref(false)
const todasLasCiudades   = ref([])

// ── Teléfono ────────────────────────────────────────────────────────
const dialCode        = ref('')
const phoneDigitCount = ref(9)

// ── Nacionalidades — array de objetos igual que el Svelte ───────────
// Cada elemento: { query: '', seleccionada: false, sugerencias: [] }
const nacionalidades = ref([{ query: '', seleccionada: false, sugerencias: [] }])

// ── Computed ────────────────────────────────────────────────────────
const userAge = computed(() => {
  if (!formData.value.birthDate) return 0
  const today = new Date(), birth = new Date(formData.value.birthDate)
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--
  return age
})

const phoneDigits = computed(() => formData.value.phone.replace(/\D/g, '').length)

const passVal = computed(() => ({
  minLength:    formData.value.password.length >= 8,
  hasUpperCase: /[A-Z]/.test(formData.value.password),
  hasLowerCase: /[a-z]/.test(formData.value.password),
  hasNumber:    /[0-9]/.test(formData.value.password),
}))

const passwordStrength = computed(() => {
  const n = Object.values(passVal.value).filter(Boolean).length
  if (n <= 1) return { text: 'Muy débil', color: '#ef4444', width: '25%' }
  if (n <= 2) return { text: 'Débil',     color: '#f59e0b', width: '50%' }
  if (n <= 3) return { text: 'Buena',     color: '#3b82f6', width: '75%' }
  return              { text: 'Excelente', color: '#10b981', width: '100%' }
})

// ── Teléfono helpers ─────────────────────────────────────────────────
function formatLocalPhone(digits, total) {
  if (total <= 7)   return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  if (total === 8)  return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  if (total === 9)  return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim()
}
function getPhonePlaceholder(total) { return formatLocalPhone('5'.repeat(total), total) }
function onPhoneInput(e) {
  const raw = e.target.value.replace(/\D/g, '').slice(0, phoneDigitCount.value)
  formData.value.phone = formatLocalPhone(raw, phoneDigitCount.value)
}

// ── Username ─────────────────────────────────────────────────────────
function onUsernameInput() {
  formData.value.username = formData.value.username.replace(/[^a-zA-Z0-9_.]/g, '')
  if (errors.value.username) errors.value.username = ''
}

// ── País ─────────────────────────────────────────────────────────────
function onPaisInput() {
  paisSeleccionado.value = null
  ciudadQuery.value = ''; ciudadSeleccionada.value = false
  ciudadesSugeridas.value = []; todasLasCiudades.value = []
  dialCode.value = ''
  const q = paisQuery.value.trim().toLowerCase()
  paisesSugeridos.value = q.length < 2 ? [] :
    todosLosPaises.value
      .filter(p => p.country.toLowerCase().includes(q))
      .slice(0, 6)
}

function seleccionarPais(p) {
  paisSeleccionado.value = p
  paisQuery.value = p.country
  paisesSugeridos.value = []
  errors.value.country = ''
  ciudadQuery.value = ''; ciudadSeleccionada.value = false
  ciudadesSugeridas.value = []
  formData.value.phone = ''

  // Ciudades ya vienen con el objeto de countriesnow
  todasLasCiudades.value = p.cities || []

  // Dial code desde el map cargado en onMounted
  const info = dialCodesMap.value[p.country.toLowerCase()]
  dialCode.value        = info?.code   ?? ''
  phoneDigitCount.value = info?.digits ?? 9
}

function blurPais() {
  setTimeout(() => {
    if (paisQuery.value && !paisSeleccionado.value) {
      errors.value.country = 'Selecciona un país de la lista'
      paisQuery.value = ''; paisesSugeridos.value = []
    } else { paisesSugeridos.value = [] }
  }, 200)
}

// ── Ciudad ───────────────────────────────────────────────────────────
function onCiudadInput() {
  ciudadSeleccionada.value = false
  const q = ciudadQuery.value.toLowerCase().trim()
  ciudadesSugeridas.value = q.length < 2 ? [] :
    todasLasCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}
function seleccionarCiudad(c) {
  ciudadQuery.value = c; ciudadSeleccionada.value = true
  ciudadesSugeridas.value = []
  errors.value.city = ''
}
function blurCiudad() {
  setTimeout(() => {
    if (ciudadQuery.value && !ciudadSeleccionada.value) {
      errors.value.city = 'Selecciona una ciudad de la lista'
      ciudadQuery.value = ''; ciudadesSugeridas.value = []
    } else { ciudadesSugeridas.value = [] }
  }, 200)
}

// ── Nacionalidades (igual que Svelte) ─────────────────────────────────
function onNacInput(i) {
  const q = nacionalidades.value[i].query.toLowerCase().trim()
  nacionalidades.value[i].seleccionada = false
  if (q.length < 2) { nacionalidades.value[i].sugerencias = []; return }
  nacionalidades.value[i].sugerencias = todosNacionalidades.value
    .filter(n =>
      n.pais.toLowerCase().includes(q) ||
      n.demonym.toLowerCase().includes(q)
    )
    .slice(0, 6)
}

function seleccionarNac(i, s) {
  nacionalidades.value[i].query       = s.demonym
  nacionalidades.value[i].seleccionada= true
  nacionalidades.value[i].sugerencias = []
  errors.value.nacionalidades = ''
}

function blurNac(i) {
  setTimeout(() => {
    if (nacionalidades.value[i].query && !nacionalidades.value[i].seleccionada) {
      errors.value.nacionalidades = 'Selecciona una nacionalidad de la lista'
      nacionalidades.value[i].query = ''
      nacionalidades.value[i].sugerencias = []
    } else {
      nacionalidades.value[i].sugerencias = []
    }
  }, 200)
}

function agregarNac() {
  nacionalidades.value.push({ query: '', seleccionada: false, sugerencias: [] })
}
function quitarNac(i) {
  nacionalidades.value.splice(i, 1)
}

// ── Validación ────────────────────────────────────────────────────────
function validateForm() {
  errors.value = {}
  if (!formData.value.firstName.trim() || formData.value.firstName.trim().length < 2)
    errors.value.firstName = !formData.value.firstName.trim() ? 'Nombre requerido' : 'Mínimo 2 caracteres'
  if (!formData.value.lastName.trim() || formData.value.lastName.trim().length < 2)
    errors.value.lastName = !formData.value.lastName.trim() ? 'Apellidos requeridos' : 'Mínimo 2 caracteres'
  if (!formData.value.birthDate)  errors.value.birthDate = 'Fecha de nacimiento requerida'
  else if (userAge.value < 18)    errors.value.birthDate = 'Debes tener al menos 18 años'
  if (!formData.value.pasaporte.trim() || formData.value.pasaporte.trim().length < 5)
    errors.value.pasaporte = !formData.value.pasaporte.trim() ? 'Pasaporte requerido' : 'Número de pasaporte inválido'
  if (!paisSeleccionado.value)    errors.value.country = 'Selecciona un país de la lista'
  if (!ciudadSeleccionada.value)  errors.value.city    = 'Selecciona una ciudad de la lista'
  if (!formData.value.phone.trim()) {
    errors.value.phone = 'Teléfono requerido'
  } else if (phoneDigits.value !== phoneDigitCount.value) {
    errors.value.phone = `Número incompleto: se requieren ${phoneDigitCount.value} dígitos (ingresaste ${phoneDigits.value})`
  }

  // Nacionalidades válidas
  const nacsValidas = nacionalidades.value.filter(n => n.query.trim() && n.seleccionada)
  if (nacsValidas.length === 0)   errors.value.nacionalidades = 'Selecciona al menos una nacionalidad'

  if (!formData.value.username.trim()) errors.value.username = 'Usuario requerido'
  else if (formData.value.username.length < 3) errors.value.username = 'Mínimo 3 caracteres'
  else if (!/^[a-zA-Z0-9_.]+$/.test(formData.value.username)) errors.value.username = 'Solo letras, números, puntos y guion bajo'

  if (!formData.value.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email))
    errors.value.email = 'Email inválido'
  if (!formData.value.password) errors.value.password = 'Contraseña requerida'
  else if (!passVal.value.minLength || !passVal.value.hasUpperCase || !passVal.value.hasLowerCase || !passVal.value.hasNumber)
    errors.value.password = 'La contraseña no cumple los requisitos'
  if (!formData.value.confirmPassword) errors.value.confirmPassword = 'Confirma tu contraseña'
  else if (formData.value.password !== formData.value.confirmPassword) errors.value.confirmPassword = 'Las contraseñas no coinciden'
  if (!acceptTerms.value)   errors.value.terms   = 'Debes aceptar los términos'
  if (!acceptPrivacy.value) errors.value.privacy = 'Debes aceptar la política de privacidad'
  return Object.keys(errors.value).length === 0
}

// ── Submit ────────────────────────────────────────────────────────────
async function handleRegister() {
  if (!validateForm()) {
    document.querySelector('.error-text')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  isSubmitting.value = true

  try {
    const nacsValidas = nacionalidades.value
      .filter(n => n.query.trim() && n.seleccionada)
      .map(n => n.query.trim())

    const payload = {
      correo:           formData.value.email.toLowerCase().trim(),
      pasaporte:        formData.value.pasaporte.trim().toUpperCase(),
      username:         formData.value.username.trim(),
      nombre:           formData.value.firstName.trim(),
      apellido:         formData.value.lastName.trim(),
      contrasena:       formData.value.password,
      telefono: dialCode.value
                        ? dialCode.value + ' ' + formData.value.phone.replace(/\s/g, '')
                        : formData.value.phone.replace(/\s/g, ''),
      fecha_nacimiento: formData.value.birthDate,
      ciudad:           ciudadQuery.value,
      pais:             paisQuery.value,
      nacionalidades:   nacsValidas,           // ["Guatemalan", "Polish"]
    }

    const res = await fetch(`${API_BASE}/api/usuarios/registro`, {
      method:  'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(payload),
    })

    const text = await res.text()
    let data = null
    try { data = JSON.parse(text) } catch { /* no-JSON */ }

    // 409 — { correo: true, pasaporte: true, username: true }
    if (res.status === 409 && data) {
      if (data.correo)    errors.value.email     = 'Este correo ya está registrado.'
      if (data.pasaporte) errors.value.pasaporte = 'Este pasaporte ya está registrado.'
      if (data.username)  errors.value.username  = 'Este nombre de usuario ya está en uso.'
      document.querySelector('.error-text')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
      return
    }

    if (!res.ok) {
      errors.value.submit = data?.error || data?.mensaje || `Error del servidor (${res.status})`
      return
    }

    // Éxito — { "mensaje": "Usuario registrado exitosamente" }
    registrationSuccess.value = true
    setTimeout(() => router.push('/ingreso'), 2000)

  } catch (err) {
    errors.value.submit = 'Error de conexión. Verifica que el servidor esté activo.'
  } finally {
    isSubmitting.value = false
  }
}
</script>