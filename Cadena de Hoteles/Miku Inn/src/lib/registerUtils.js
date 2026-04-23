/**
 * @file registerUtils.js
 * @description Funciones utilitarias puras extraídas de Register.svelte.
 * Separadas del componente para poder testarlas con Jest.
 *
 * Ubicación: src/lib/registerUtils.js
 *
 * Uso en Register.svelte — agrega este import al bloque <script>:
 *
 *   import {
 *     validatePassword,
 *     getPasswordStrength,
 *     calculateAge,
 *     formatLocalPhone,
 *     getPhonePlaceholder,
 *     validateForm
 *   } from '../lib/registerUtils.js';
 *
 * Luego elimina las definiciones originales de esas funciones en Register.svelte.
 */

// ─── VALIDACIÓN DE CONTRASEÑA ────────────────────────────────────────────────

/**
 * Evalúa si la contraseña cumple cada requisito mínimo de seguridad.
 * @param {string} p
 * @returns {{ minLength: boolean, hasUpperCase: boolean, hasLowerCase: boolean,
 *             hasNumber: boolean, hasSpecial: boolean }}
 */
export function validatePassword(p) {
  return {
    minLength:    p.length >= 8,
    hasUpperCase: /[A-Z]/.test(p),
    hasLowerCase: /[a-z]/.test(p),
    hasNumber:    /[0-9]/.test(p),
    hasSpecial:   /[!@#$%^&*(),.?":{}|<>]/.test(p)
  };
}

/**
 * Calcula el nivel de seguridad visual de la contraseña (Muy débil → Excelente).
 * @param {string} p
 * @returns {{ text: string, color: string, width: string }}
 */
export function getPasswordStrength(p) {
  const n = Object.values(validatePassword(p)).filter(Boolean).length;
  if (n <= 2) return { text: 'Muy débil', color: '#ef4444', width: '25%'  };
  if (n <= 3) return { text: 'Débil',     color: '#f59e0b', width: '50%'  };
  if (n <= 4) return { text: 'Buena',     color: '#3b82f6', width: '75%'  };
  return              { text: 'Excelente', color: '#10b981', width: '100%' };
}

// ─── CÁLCULO DE EDAD ─────────────────────────────────────────────────────────

/**
 * Calcula la edad en años cumplidos a partir de una fecha YYYY-MM-DD.
 * @param {string} birthDate
 * @returns {number} 0 si la fecha está vacía.
 */
export function calculateAge(birthDate) {
  if (!birthDate) return 0;
  const today = new Date();

  // ⚠️  FIX TIMEZONE: new Date('YYYY-MM-DD') interpreta la cadena como UTC midnight.
  // En zonas horarias negativas (Guatemala = UTC-6) eso significa que .getDate()
  // devuelve el día ANTERIOR al esperado, rompiendo la lógica "¿ya pasó el cumpleaños?".
  // Solución: construir con new Date(year, month-1, day) que usa hora LOCAL.
  const [y, m, d] = birthDate.split('-').map(Number);
  const birth = new Date(y, m - 1, d);

  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) age--;
  return age;
}


// ─── FORMATO DE TELÉFONO ─────────────────────────────────────────────────────

/**
 * Formatea dígitos de teléfono local con espacios según el estándar ITU del país.
 * @param {string} digits  - Solo números, sin espacios.
 * @param {number} total   - Cantidad de dígitos esperados para el país.
 * @returns {string}
 */
export function formatLocalPhone(digits, total) {
  if (total <= 7)   return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
  if (total === 8)  return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
  if (total === 9)  return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
  if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
  return                   digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
}

/**
 * Genera el placeholder de ejemplo del campo teléfono para el país dado.
 * @param {number} digits - Total de dígitos del país.
 * @returns {string}
 */
export function getPhonePlaceholder(digits) {
  return formatLocalPhone('5'.repeat(digits), digits);
}

// ─── VALIDACIÓN COMPLETA DEL FORMULARIO ──────────────────────────────────────

/**
 * Valida todos los campos del formulario de registro.
 * Recibe el estado actual del componente como objeto plano para poder
 * probarse sin necesidad del DOM.
 *
 * @param {{
 *   formData: object,
 *   userAge: number,
 *   phoneDigitCount: number,
 *   paisSeleccionado: object|null,
 *   ciudadSeleccionada: boolean,
 *   nacionalidades: string[],
 *   nacionalidadesSeleccionadas: boolean[],
 *   passwordValidation: object,
 *   acceptTerms: boolean,
 *   acceptPrivacy: boolean,
 *   captchaVerified: boolean
 * }} state
 * @returns {{ [campo: string]: string }} Errores por campo. Vacío = formulario válido.
 */
export function validateForm(state) {
  const {
    formData, userAge, phoneDigitCount,
    paisSeleccionado, ciudadSeleccionada,
    nacionalidades, nacionalidadesSeleccionadas,
    passwordValidation, acceptTerms, acceptPrivacy, captchaVerified
  } = state;

  const errors = {};

  if (!formData.firstName.trim() || formData.firstName.trim().length < 2)
    errors.firstName = !formData.firstName.trim() ? 'Nombre requerido' : 'Mínimo 2 caracteres';

  if (!formData.lastName.trim() || formData.lastName.trim().length < 2)
    errors.lastName = !formData.lastName.trim() ? 'Apellidos requeridos' : 'Mínimo 2 caracteres';

  if (!formData.birthDate)
    errors.birthDate = 'Fecha de nacimiento requerida';
  else if (userAge < 18)
    errors.birthDate = 'Debes tener al menos 18 años';

  if (!formData.phone.trim()) {
    errors.phone = 'Teléfono requerido';
  } else {
    const ingresados = formData.phone.replace(/\D/g, '').length;
    if (ingresados !== phoneDigitCount)
      errors.phone = `Número incompleto: se requieren ${phoneDigitCount} dígitos para ${formData.country || 'el país seleccionado'} (ingresaste ${ingresados}).`;
  }

  if (!formData.pasaporte.trim() || formData.pasaporte.trim().length < 5)
    errors.pasaporte = !formData.pasaporte.trim() ? 'Pasaporte requerido' : 'Número de pasaporte inválido';

  if (!paisSeleccionado || !formData.country) errors.country = 'Selecciona un país de la lista';
  if (!ciudadSeleccionada || !formData.city)  errors.city    = 'Selecciona una ciudad de la lista';

  if (!formData.username.trim())
    errors.username = 'Nombre de usuario requerido';
  else if (formData.username.trim().length < 3)
    errors.username = 'Mínimo 3 caracteres';
  else if (formData.username.trim().length > 20)
    errors.username = 'Máximo 20 caracteres';
  else if (!/^[a-zA-Z0-9_.]+$/.test(formData.username.trim()))
    errors.username = 'Solo letras, números, puntos y guion bajo';

  if (!formData.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
    errors.email = !formData.email.trim() ? 'Email requerido' : 'Email inválido';

  const nacsValidas = nacionalidades.filter((n, i) => n.trim() && nacionalidadesSeleccionadas[i]);
  if (nacsValidas.length === 0) errors.nacionalidades = 'Selecciona al menos una nacionalidad';

  if (!formData.password)
    errors.password = 'Contraseña requerida';
  else if (!passwordValidation.minLength || !passwordValidation.hasUpperCase ||
           !passwordValidation.hasLowerCase || !passwordValidation.hasNumber ||
           !passwordValidation.hasSpecial)
    errors.password = 'La contraseña no cumple los requisitos';

  if (!formData.confirmPassword)
    errors.confirmPassword = 'Confirma tu contraseña';
  else if (formData.password !== formData.confirmPassword)
    errors.confirmPassword = 'Las contraseñas no coinciden';

  if (!acceptTerms)   errors.terms   = 'Debes aceptar los términos y condiciones';
  if (!acceptPrivacy) errors.privacy = 'Debes aceptar la política de privacidad';
  if (!captchaVerified) errors.captcha = 'Por favor verifica que no eres un robot';

  return errors;
}