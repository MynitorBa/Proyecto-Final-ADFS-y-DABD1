<template>
  <div class="register-page">

    <div class="register-container">
      <div class="register-card">

        <!-- Botón para regresar a la página principal sin guardar datos -->
        <button class="back-link" @click="$router.push('/principal')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          Volver al inicio
        </button>

        <!-- Encabezado con logo y descripción del formulario -->
        <div class="register-header">
          <img src="/movent.png" alt="Movent" class="register-logo" />
          <h2>Crear tu Cuenta</h2>
          <p>Únete a Movent y comienza a reservar experiencias de viaje</p>
        </div>

        <!-- Pantalla de éxito que aparece tras el registro exitoso -->
        <div v-if="registrationSuccess" class="success-box">
          <div class="success-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <h3>¡Cuenta Creada!</h3>
          <p>Redirigiendo al inicio de sesión...</p>
          <div class="loading-dots"><span></span><span></span><span></span></div>
        </div>

        <!-- Formulario principal de registro, oculto tras registro exitoso -->
        <form v-else @submit.prevent="handleRegister" class="register-form">

          <!-- Banner elegante para mostrar errores devueltos por el backend -->
          <div v-if="serverError" class="server-error-banner">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <div>
              <strong>{{ serverErrorTitle }}</strong>
              <p>{{ serverError }}</p>
            </div>
            <button type="button" class="banner-close" @click="serverError = ''" aria-label="Cerrar">✕</button>
          </div>

          <!-- Sección: información personal del usuario -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              Información Personal
            </h3>

            <!-- Nombre y apellidos -->
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

            <!-- Fecha de nacimiento con indicador informativo y número de pasaporte -->
            <div class="form-grid-2">
              <div class="form-field">
                <label>Fecha de Nacimiento <span class="req">*</span></label>
                <input type="date" v-model="formData.birthDate" :class="{ error: errors.birthDate }" />
                <span v-if="formData.birthDate && userAge >= 18" class="match-ok">✓ {{ userAge }} años</span>
                <span v-else-if="formData.birthDate && userAge < 18 && userAge >= 0" class="hint-text">ℹ Edad: {{ userAge }} años</span>
                <span v-if="errors.birthDate" class="error-text">{{ errors.birthDate }}</span>
              </div>
              <div class="form-field">
                <label>Número de Pasaporte <span class="req">*</span></label>
                <input type="text" v-model="formData.pasaporte"
                  @input="formData.pasaporte = formData.pasaporte.toUpperCase()"
                  placeholder="AB123456"
                  :class="{ error: errors.pasaporte }" autocomplete="off" />
                <span v-if="errors.pasaporte" class="error-text">{{ errors.pasaporte }}</span>
              </div>
            </div>

            <!-- País con autocompletado y teléfono con prefijo dinámico -->
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

              <!-- Teléfono deshabilitado hasta que se elija un país (UX pura) -->
              <div class="form-field">
                <label>
                  Teléfono <span class="req">*</span>
                  <span v-if="dialCode" class="label-hint">— {{ phoneDigitCount }} dígitos sugeridos</span>
                </label>
                <div class="phone-field" :class="{ error: errors.phone }">
                  <span v-if="dialCode" class="phone-prefix">{{ dialCode }}</span>
                  <input type="tel" v-model="formData.phone" @input="onPhoneInput"
                    :placeholder="dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un país primero'"
                    :disabled="!dialCode" autocomplete="tel" />
                </div>
                <span v-if="formData.phone && phoneDigits === phoneDigitCount" class="match-ok">✓ Número completo</span>
                <span v-else-if="formData.phone" class="hint-text">ℹ {{ phoneDigits }}/{{ phoneDigitCount }} dígitos</span>
                <span v-if="errors.phone" class="error-text">{{ errors.phone }}</span>
              </div>
            </div>

            <!-- Ciudad: autocompletado dependiente del país seleccionado (UX pura) -->
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

            <!-- Nacionalidades: permite agregar múltiples con autocompletado (UX pura) -->
            <div class="form-field">
              <label>Nacionalidad(es) <span class="req">*</span></label>
              <div v-for="(nac, i) in nacionalidades" :key="i" class="nac-row">
                <div class="autocomplete-wrap" style="flex:1">
                  <input type="text" v-model="nacionalidades[i].query"
                    @input="onNacInput(i)" @blur="blurNac(i)"
                    placeholder="Ej: Guatemalteca"
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

          <!-- Sección: credenciales de acceso (usuario, correo y contraseñas) -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              Credenciales de Acceso
            </h3>

            <!-- Nombre de usuario con sanitización automática (UX) -->
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

            <!-- Correo electrónico -->
            <div class="form-field">
              <label>Correo Electrónico <span class="req">*</span></label>
              <div class="input-icon-wrap">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                <input type="email" v-model="formData.email" placeholder="tu@email.com"
                  :class="{ error: errors.email }" autocomplete="email" />
              </div>
              <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
            </div>

            <!-- Contraseña con barra de fortaleza e indicadores de requisitos (UX) -->
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
                <!-- Barra de fortaleza de contraseña (indicador visual UX) -->
                <div v-if="formData.password" class="strength-bar-wrap">
                  <div class="strength-bar"><div class="strength-fill" :style="{ width: passwordStrength.width, background: passwordStrength.color }"></div></div>
                  <span class="strength-label" :style="{ color: passwordStrength.color }">{{ passwordStrength.text }}</span>
                </div>
                <!-- Indicadores de requisitos de contraseña (visual UX) -->
                <div class="requirements">
                  <span :class="['req-item', { met: passVal.minLength }]">✓ 8 caracteres</span>
                  <span :class="['req-item', { met: passVal.hasUpperCase }]">✓ Mayúscula</span>
                  <span :class="['req-item', { met: passVal.hasLowerCase }]">✓ Minúscula</span>
                  <span :class="['req-item', { met: passVal.hasNumber }]">✓ Número</span>
                </div>
                <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
              </div>

              <!-- Campo de confirmación de contraseña (UX) -->
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
                <span v-else-if="formData.confirmPassword" class="hint-text">ℹ Aún no coinciden</span>
                <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
              </div>
            </div>
          </div>

          <!-- Aceptación de términos y política de privacidad (solo UX, no se envía al backend) -->
          <div class="terms-section">
            <label class="checkbox-label">
              <input type="checkbox" v-model="acceptTerms" />
              <span class="checkbox-custom"></span>
              <span>Acepto los <button type="button" class="link-btn">Términos y Condiciones</button> <span class="req">*</span></span>
            </label>

            <label class="checkbox-label">
              <input type="checkbox" v-model="acceptPrivacy" />
              <span class="checkbox-custom"></span>
              <span>Acepto la <button type="button" class="link-btn">Política de Privacidad</button> <span class="req">*</span></span>
            </label>
          </div>

          <!-- Botón de envío con spinner mientras procesa -->
          <button type="submit" class="submit-btn" :disabled="isSubmitting">
            <svg v-if="isSubmitting" class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
            {{ isSubmitting ? 'Creando cuenta...' : 'Crear Cuenta' }}
          </button>

          <!-- Enlace hacia la pantalla de inicio de sesión -->
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
/**
 * @file Registrarse.vue
 * @description Formulario de registro de nuevos usuarios en Movent.
 * IMPORTANTE: Este formulario NO valida los datos en el cliente - todas las
 * validaciones se delegan al backend como fuente de verdad. El frontend
 * mantiene solo los indicadores visuales (barra de contraseña, autocompletado,
 * formato de teléfono) como ayuda de UX, pero deja que el backend decida
 * si un registro es válido o no. Los errores del servidor se muestran en
 * un banner elegante arriba del formulario.
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import '../styles/registrarse.css'

/** URL base del backend. @type {string} */
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Instancia del router para redirigir tras el registro exitoso. */
const router   = useRouter()

/**
 * Mapa de cantidad de dígitos locales por código de marcación internacional (ITU).
 * Se usa solo para formatear el teléfono, no para validar (eso lo hace el backend).
 * @type {Record<string, number>}
 */
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

/** Lista de países con sus ciudades, cargada desde countriesnow. @type {import('vue').Ref<Array>} */
const todosLosPaises      = ref([])

/** Mapa de país → { code, digits } construido desde restcountries. @type {import('vue').Ref<Record<string, object>>} */
const dialCodesMap        = ref({})

/** Lista de nacionalidades con sus demónimos, para el autocompletado. @type {import('vue').Ref<Array>} */
const todosNacionalidades = ref([])

/** Carga países, prefijos telefónicos y nacionalidades al montar el componente. */
onMounted(async () => {
  try {
    const res  = await fetch('https://countriesnow.space/api/v0.1/countries')
    const data = await res.json()
    todosLosPaises.value = data.data || []
  } catch { console.error('Error cargando países') }

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
        if (p.name.official) dialCodesMap.value[p.name.official.toLowerCase()] = { code, digits }
      }
    })
    todosNacionalidades.value = data
      .filter(p => p.demonyms?.eng?.m)
      .map(p => ({ pais: p.name.common, demonym: p.demonyms.eng.m }))
      .sort((a, b) => a.pais.localeCompare(b.pais))
  } catch { console.error('Error cargando nacionalidades / dial codes') }
})

/**
 * Objeto reactivo con todos los campos del formulario.
 * @type {import('vue').Ref<object>}
 */
const formData = ref({
  firstName: '', lastName: '', birthDate: '', pasaporte: '',
  phone: '', username: '', email: '', password: '', confirmPassword: ''
})

/** Controla la visibilidad del campo de contraseña. @type {import('vue').Ref<boolean>} */
const showPassword        = ref(false)

/** Controla la visibilidad del campo de confirmación de contraseña. @type {import('vue').Ref<boolean>} */
const showConfirmPassword = ref(false)

/** Estado del checkbox de términos y condiciones (UX, no se envía al backend). @type {import('vue').Ref<boolean>} */
const acceptTerms         = ref(false)

/** Estado del checkbox de política de privacidad (UX, no se envía al backend). @type {import('vue').Ref<boolean>} */
const acceptPrivacy       = ref(false)

/** Errores devueltos por el backend en respuestas 409 (por campo). @type {import('vue').Ref<object>} */
const errors              = ref({})

/** Mensaje de error general del backend (HTTP 400 o similar). @type {import('vue').Ref<string>} */
const serverError         = ref('')

/** Título del banner de error (se adapta al tipo de error del backend). @type {import('vue').Ref<string>} */
const serverErrorTitle    = ref('Error al registrar')

/** Previene el doble envío mientras la petición está en curso. @type {import('vue').Ref<boolean>} */
const isSubmitting        = ref(false)

/** Determina si se muestra la pantalla de éxito o el formulario. @type {import('vue').Ref<boolean>} */
const registrationSuccess = ref(false)

/** Texto escrito en el campo de búsqueda de país. @type {import('vue').Ref<string>} */
const paisQuery        = ref('')

/** Países filtrados que se muestran en el dropdown. @type {import('vue').Ref<Array>} */
const paisesSugeridos  = ref([])

/** País seleccionado de la lista (objeto completo con cities). @type {import('vue').Ref<object|null>} */
const paisSeleccionado = ref(null)

/** Texto escrito en el campo de búsqueda de ciudad. @type {import('vue').Ref<string>} */
const ciudadQuery        = ref('')

/** Ciudades filtradas para el dropdown de ciudad. @type {import('vue').Ref<Array>} */
const ciudadesSugeridas  = ref([])

/** Indica si el usuario seleccionó una ciudad válida de la lista. @type {import('vue').Ref<boolean>} */
const ciudadSeleccionada = ref(false)

/** Indica que se están cargando ciudades de la API. @type {import('vue').Ref<boolean>} */
const ciudadLoading      = ref(false)

/** Listado completo de ciudades del país seleccionado. @type {import('vue').Ref<Array>} */
const todasLasCiudades   = ref([])

/** Código de marcación del país seleccionado (ej. '+502'). @type {import('vue').Ref<string>} */
const dialCode        = ref('')

/** Cantidad de dígitos locales sugeridos según el país. @type {import('vue').Ref<number>} */
const phoneDigitCount = ref(9)

/**
 * Array de nacionalidades que el usuario puede agregar una a una.
 * Cada elemento tiene query (texto), seleccionada (boolean) y sugerencias.
 * @type {import('vue').Ref<Array>}
 */
const nacionalidades = ref([{ query: '', seleccionada: false, sugerencias: [] }])

/**
 * Calcula la edad del usuario a partir de su fecha de nacimiento (solo visual).
 * @type {import('vue').ComputedRef<number>}
 */
const userAge = computed(() => {
  if (!formData.value.birthDate) return 0
  const today = new Date(), birth = new Date(formData.value.birthDate)
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--
  return age
})

/** Cantidad de dígitos numéricos ingresados en el campo de teléfono. @type {import('vue').ComputedRef<number>} */
const phoneDigits = computed(() => formData.value.phone.replace(/\D/g, '').length)

/**
 * Verifica qué requisitos de la contraseña ya se cumplen (solo visual).
 * @type {import('vue').ComputedRef<{minLength: boolean, hasUpperCase: boolean, hasLowerCase: boolean, hasNumber: boolean}>}
 */
const passVal = computed(() => ({
  minLength:    formData.value.password.length >= 8,
  hasUpperCase: /[A-Z]/.test(formData.value.password),
  hasLowerCase: /[a-z]/.test(formData.value.password),
  hasNumber:    /[0-9]/.test(formData.value.password),
}))

/**
 * Devuelve el nivel de fortaleza de la contraseña con texto, color y ancho de barra.
 * Solo informativo, no bloquea el envío.
 * @type {import('vue').ComputedRef<{text: string, color: string, width: string}>}
 */
const passwordStrength = computed(() => {
  const n = Object.values(passVal.value).filter(Boolean).length
  if (n <= 1) return { text: 'Muy débil', color: '#ef4444', width: '25%' }
  if (n <= 2) return { text: 'Débil',     color: '#f59e0b', width: '50%' }
  if (n <= 3) return { text: 'Buena',     color: '#3b82f6', width: '75%' }
  return              { text: 'Excelente', color: '#10b981', width: '100%' }
})

/**
 * Aplica formato de espacios al número local según su longitud total esperada.
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
 * Genera el placeholder del campo de teléfono simulando dígitos con '5'.
 * @param {number} total
 * @returns {string}
 */
function getPhonePlaceholder(total) { return formatLocalPhone('5'.repeat(total), total) }

/**
 * Limpia no numéricos, recorta al máximo de dígitos y reformatea el teléfono al escribir.
 * @param {Event} e
 */
function onPhoneInput(e) {
  const raw = e.target.value.replace(/\D/g, '').slice(0, phoneDigitCount.value)
  formData.value.phone = formatLocalPhone(raw, phoneDigitCount.value)
}

/**
 * Sanitiza el username: solo permite letras, números, puntos y guion bajo.
 * Esta sanitización sigue existiendo porque es más amigable que dejar al usuario escribir
 * caracteres que el backend va a rechazar.
 */
function onUsernameInput() {
  formData.value.username = formData.value.username.replace(/[^a-zA-Z0-9_.]/g, '')
  if (errors.value.username) errors.value.username = ''
}

/**
 * Filtra la lista de países según lo escrito y resetea la ciudad/teléfono si cambia el país.
 */
function onPaisInput() {
  paisSeleccionado.value = null
  ciudadQuery.value = ''; ciudadSeleccionada.value = false
  ciudadesSugeridas.value = []; todasLasCiudades.value = []
  dialCode.value = ''
  const q = paisQuery.value.trim().toLowerCase()
  paisesSugeridos.value = q.length < 2 ? [] :
    todosLosPaises.value.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6)
}

/**
 * Fija el país elegido y carga sus ciudades y código telefónico.
 * @param {object} p - Objeto de país con cities y country
 */
function seleccionarPais(p) {
  paisSeleccionado.value = p; paisQuery.value = p.country; paisesSugeridos.value = []
  ciudadQuery.value = ''; ciudadSeleccionada.value = false; ciudadesSugeridas.value = []
  formData.value.phone = ''
  todasLasCiudades.value = p.cities || []
  const info = dialCodesMap.value[p.country.toLowerCase()]
  dialCode.value        = info?.code   ?? ''
  phoneDigitCount.value = info?.digits ?? 9
}

/**
 * Al perder el foco en el campo de país, solo oculta la lista de sugerencias.
 * Ya NO limpia el texto — se deja lo que el usuario escribió para que el backend lo reciba.
 */
function blurPais() {
  setTimeout(() => { paisesSugeridos.value = [] }, 200)
}

/** Filtra ciudades del país seleccionado según lo escrito. */
function onCiudadInput() {
  ciudadSeleccionada.value = false
  const q = ciudadQuery.value.toLowerCase().trim()
  ciudadesSugeridas.value = q.length < 2 ? [] :
    todasLasCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

/**
 * Confirma la ciudad seleccionada.
 * @param {string} c - Nombre de la ciudad
 */
function seleccionarCiudad(c) {
  ciudadQuery.value = c; ciudadSeleccionada.value = true
  ciudadesSugeridas.value = []
}

/**
 * Al perder el foco en ciudad, solo oculta la lista. No limpia el texto.
 */
function blurCiudad() {
  setTimeout(() => { ciudadesSugeridas.value = [] }, 200)
}

/**
 * Filtra el listado de nacionalidades según el texto en el campo indicado.
 * @param {number} i - Índice del campo de nacionalidad
 */
function onNacInput(i) {
  const q = nacionalidades.value[i].query.toLowerCase().trim()
  nacionalidades.value[i].seleccionada = false
  if (q.length < 2) { nacionalidades.value[i].sugerencias = []; return }
  nacionalidades.value[i].sugerencias = todosNacionalidades.value
    .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q))
    .slice(0, 6)
}

/**
 * Fija la nacionalidad elegida en el campo correspondiente.
 * @param {number} i - Índice del campo
 * @param {{ pais: string, demonym: string }} s - Opción seleccionada
 */
function seleccionarNac(i, s) {
  nacionalidades.value[i].query = s.demonym; nacionalidades.value[i].seleccionada = true
  nacionalidades.value[i].sugerencias = []
}

/**
 * Al perder el foco en nacionalidad, solo oculta sugerencias. No limpia.
 * @param {number} i - Índice del campo
 */
function blurNac(i) {
  setTimeout(() => { nacionalidades.value[i].sugerencias = [] }, 200)
}

/** Agrega un nuevo campo de nacionalidad al array. */
function agregarNac() { nacionalidades.value.push({ query: '', seleccionada: false, sugerencias: [] }) }

/**
 * Elimina un campo de nacionalidad por índice.
 * @param {number} i
 */
function quitarNac(i) { nacionalidades.value.splice(i, 1) }

/**
 * Envía los datos del formulario al backend SIN VALIDACIONES LOCALES.
 * El backend es la fuente de verdad y responde con el error específico si algo está mal.
 * Los errores se muestran en un banner elegante o en los campos específicos (duplicados).
 */
async function handleRegister() {
  // Limpiar errores previos antes de intentar de nuevo
  errors.value   = {}
  serverError.value = ''

  isSubmitting.value = true
  try {
    // Construir el payload tal como lo espera el backend.
    // Se mandan los valores tal cual — aunque estén vacíos o mal — para que el backend valide.
    const nacsEnviar = nacionalidades.value
      .map(n => n.query.trim())
      .filter(q => q.length > 0)

    const payload = {
      correo:           (formData.value.email || '').toLowerCase().trim(),
      pasaporte:        (formData.value.pasaporte || '').trim().toUpperCase(),
      username:         (formData.value.username || '').trim(),
      nombre:           (formData.value.firstName || '').trim(),
      apellido:         (formData.value.lastName || '').trim(),
      contrasena:       formData.value.password || '',
      telefono: dialCode.value
                        ? dialCode.value + ' ' + (formData.value.phone || '').replace(/\s/g, '')
                        : (formData.value.phone || '').replace(/\s/g, ''),
      fecha_nacimiento: formData.value.birthDate || '',
      ciudad:           (ciudadQuery.value || '').trim(),
      pais:             (paisQuery.value || '').trim(),
      nacionalidades:   nacsEnviar,
    }

    const res  = await fetch(`${API_BASE}/api/usuarios/registro`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    const text = await res.text()
    let data = null
    try { data = JSON.parse(text) } catch { /**/ }

    // HTTP 409: datos duplicados — marcar el campo específico (UX amigable)
    if (res.status === 409 && data) {
      if (data.correo)    errors.value.email     = 'Este correo ya está registrado.'
      if (data.pasaporte) errors.value.pasaporte = 'Este pasaporte ya está registrado.'
      if (data.username)  errors.value.username  = 'Este nombre de usuario ya está en uso.'
      serverErrorTitle.value = 'Datos ya registrados'
      serverError.value = 'Algunos datos ya están en uso. Revisa los campos marcados.'
      scrollToTop()
      return
    }

    // HTTP 400: falla de validación del backend — mostrar banner con el mensaje exacto
    if (res.status === 400 && data) {
      serverErrorTitle.value = 'Datos inválidos'
      serverError.value = data.error || 'Hay un problema con los datos enviados.'
      scrollToTop()
      return
    }

    // Otros errores (500 etc.)
    if (!res.ok) {
      serverErrorTitle.value = 'Error del servidor'
      serverError.value = data?.error || data?.mensaje || `Error del servidor (${res.status})`
      scrollToTop()
      return
    }

    // Registro exitoso: mostrar pantalla de confirmación y redirigir
    registrationSuccess.value = true
    setTimeout(() => router.push('/ingreso'), 2000)

  } catch {
    serverErrorTitle.value = 'Error de conexión'
    serverError.value = 'No se pudo conectar con el servidor. Verifica que esté activo.'
    scrollToTop()
  } finally {
    isSubmitting.value = false
  }
}

/**
 * Hace scroll suave al inicio del formulario para que el usuario vea el banner de error.
 */
function scrollToTop() {
  setTimeout(() => {
    document.querySelector('.server-error-banner')?.scrollIntoView({
      behavior: 'smooth', block: 'center'
    })
  }, 100)
}
</script>