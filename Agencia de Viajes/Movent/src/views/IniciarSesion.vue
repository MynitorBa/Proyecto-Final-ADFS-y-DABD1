<template>
  <div class="login-page">
    <div class="login-split">

      <!-- Panel izquierdo — branding -->
      <div class="login-brand">
        <div class="login-brand__content">
          <img src="/movent.png" alt="Movent" class="login-brand__logo" />
          <h1 class="login-brand__title">Bienvenido de vuelta</h1>
          <p class="login-brand__subtitle">Tu agencia de viajes de confianza. Vuelos y hospedajes de múltiples proveedores en un solo lugar.</p>
          <div class="login-brand__features">
            <div class="login-brand__feature">
              <div class="login-brand__feature-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              </div>
              <span>Vuelos globales</span>
            </div>
            <div class="login-brand__feature">
              <div class="login-brand__feature-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              </div>
              <span>Hospedaje premium</span>
            </div>
            <div class="login-brand__feature">
              <div class="login-brand__feature-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              </div>
              <span>Mejor precio garantizado</span>
            </div>
          </div>
        </div>
        <div class="login-brand__overlay"></div>
      </div>

      <!-- Panel derecho — formulario -->
      <div class="login-form-panel">
        <div class="login-form-wrapper">

          <button class="back-link" @click="$router.push('/principal')" type="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
            Volver al inicio
          </button>

          <div class="login-form-header">
            <h2>Iniciar Sesión</h2>
            <p>Accede a tu cuenta y gestiona tus reservas</p>
          </div>

          <!-- Success -->
          <div v-if="loginSuccess" class="success-box">
            <div class="success-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            </div>
            <h3>¡Bienvenido de vuelta!</h3>
            <p>{{ destino === '/admin/dashboard' ? 'Redirigiendo al panel admin...' : destino === '/admin/webservice' ? 'Redirigiendo al panel WebService...' : 'Iniciando sesión...' }}</p>
            <div class="loading-dots"><span></span><span></span><span></span></div>
          </div>

          <form v-else @submit.prevent="handleLogin" class="login-form">

            <div v-if="serverError" class="alert-error">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ serverError }}
            </div>

            <div class="form-field">
              <label for="login">Usuario o Correo Electrónico</label>
              <div class="input-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <input type="text" id="login" v-model="formData.login" @input="onFieldChange"
                  placeholder="usuario o tu@email.com" :class="{ error: errors.login }" autocomplete="username" />
              </div>
              <span v-if="errors.login" class="error-text">{{ errors.login }}</span>
            </div>

            <div class="form-field">
              <label for="password">Contraseña</label>
              <div class="password-wrap">
                <input :type="showPassword ? 'text' : 'password'" id="password"
                  v-model="formData.password" @input="onFieldChange"
                  placeholder="Ingresa tu contraseña" :class="{ error: errors.password }" autocomplete="current-password" />
                <button type="button" class="toggle-pass" @click="showPassword = !showPassword" tabindex="-1">
                  <svg v-if="showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                </button>
              </div>
              <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
            </div>

            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe" />
              <span class="checkbox-custom"></span>
              <span>Recordarme</span>
            </label>

            <!-- reCAPTCHA v2 -->
            <div class="captcha-wrap">
              <div id="recaptcha-login"></div>
              <span v-if="errors.captcha" class="error-text">{{ errors.captcha }}</span>
            </div>

            <button type="submit" class="submit-btn" :disabled="isSubmitting">
              <svg v-if="isSubmitting" class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
              {{ isSubmitting ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
            </button>

            <p class="footer-text">
              ¿No tienes una cuenta?
              <router-link to="/registro" class="link-btn">Regístrate aquí</router-link>
            </p>

          </form>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import '../styles/iniciarsesion.css'

const API_BASE = 'http://localhost:8080'

const router = useRouter()

const formData     = ref({ login: '', password: '' })
const showPassword = ref(false)
const rememberMe   = ref(false)
const errors       = ref({})
const isSubmitting = ref(false)
const loginSuccess = ref(false)
const serverError  = ref('')
const destino      = ref('/principal')
const captchaToken = ref('')

// ── CAPTCHA ──────────────────────────────────────────────────────────
const RECAPTCHA_SITE_KEY = '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI'
let recaptchaWidgetId = null

const loadRecaptcha = () => {
  if (window.grecaptcha) { renderCaptcha(); return }
  if (!document.getElementById('recaptcha-script')) {
    const script = document.createElement('script')
    script.id    = 'recaptcha-script'
    script.src   = 'https://www.google.com/recaptcha/api.js?onload=onRecaptchaLoad&render=explicit'
    script.async = true
    script.defer = true
    document.head.appendChild(script)
  }
  window.onRecaptchaLoad = renderCaptcha
}

const renderCaptcha = () => {
  if (!window.grecaptcha || !document.getElementById('recaptcha-login')) return
  recaptchaWidgetId = window.grecaptcha.render('recaptcha-login', {
    sitekey:            RECAPTCHA_SITE_KEY,
    theme:              'light',
    callback:           (token) => { captchaToken.value = token; errors.value.captcha = '' },
    'expired-callback': () => { captchaToken.value = '' },
  })
}

const resetCaptcha = () => {
  if (window.grecaptcha && recaptchaWidgetId !== null) {
    window.grecaptcha.reset(recaptchaWidgetId)
    captchaToken.value = ''
  }
}

onMounted(() => loadRecaptcha())
onUnmounted(() => { delete window.onRecaptchaLoad })

// ── Helpers ───────────────────────────────────────────────────────────
const onFieldChange = () => {
  serverError.value = ''
  errors.value = {}
}

const validateForm = () => {
  errors.value = {}
  if (!formData.value.login.trim()) errors.value.login    = 'El usuario o email es requerido'
  if (!formData.value.password)     errors.value.password = 'La contraseña es requerida'
  if (!captchaToken.value)          errors.value.captcha  = 'Completa el CAPTCHA para continuar'
  return Object.keys(errors.value).length === 0
}

// ── Destino por rol ───────────────────────────────────────────────────
// rol_id: 1=Registrado  2=Administrador  3=WebService
const calcularDestino = (rolId) => {
  if (rolId === 2) return '/admin/dashboard'
  if (rolId === 3) return '/admin/webservice'
  return '/principal'
}

// ── Login ─────────────────────────────────────────────────────────────
const handleLogin = async () => {
  if (!validateForm()) return
  isSubmitting.value = true
  serverError.value  = ''

  try {
    const res = await fetch(`${API_BASE}/api/usuarios/login`, {
      method:      'POST',
      credentials: 'include',
      headers:     { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        login:      formData.value.login.trim(),
        contrasena: formData.value.password,
      }),
    })

    let data = null
    const text = await res.text()
    try { data = JSON.parse(text) } catch { /* no-JSON */ }

    if (!res.ok) {
      serverError.value = data?.error || `Error ${res.status}`
      resetCaptcha()
      return
    }

    // Guardar sesión
    sessionStorage.setItem('usuario_sesion', JSON.stringify({
      id:       data.id,
      nombre:   data.nombre,
      apellido: data.apellido,
      usuario:  data.username,
      correo:   data.correo,
      rol:      data.rol_id === 2 ? 'Administrador' : data.rol_id === 3 ? 'WebService' : 'Registrado',
      isAdmin:  data.rol_id === 2,
      isWS:     data.rol_id === 3,
    }))

    destino.value      = calcularDestino(data.rol_id)
    loginSuccess.value = true
    setTimeout(() => router.push(destino.value), 1400)

  } catch {
    serverError.value = 'Error de conexión. Verifica que el servidor esté activo.'
    resetCaptcha()
  } finally {
    isSubmitting.value = false
  }
}
</script>