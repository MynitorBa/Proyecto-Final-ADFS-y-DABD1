/**
 * auth-validaciones.test.js
 * Pruebas unitarias para la validación del formulario de inicio de sesión.
 * Lógica extraída de IniciarSesion.vue — sin dependencias de reCAPTCHA ni Vue Router.
 */

import { describe, it, expect } from 'vitest'

// ─────────────────────────────────────────────────────────────────────────────
// validateForm replica la lógica del componente IniciarSesion.vue.
// Retorna { valid, errors }.
// ─────────────────────────────────────────────────────────────────────────────

function validateForm({ login, password, captchaToken }) {
  const errors = {}
  if (!login.trim())    errors.login    = 'El usuario o email es requerido'
  if (!password)        errors.password = 'La contraseña es requerida'
  if (!captchaToken)    errors.captcha  = 'Completa el CAPTCHA para continuar'
  return { valid: Object.keys(errors).length === 0, errors }
}

// ─────────────────────────────────────────────────────────────────────────────
// validateForm
// ─────────────────────────────────────────────────────────────────────────────

describe('Validaciones de Login — validateForm', () => {
  it('retorna valid=true cuando todos los campos están completos', () => {
    const { valid } = validateForm({ login: 'carlos', password: 'abc123', captchaToken: 'tok' })
    expect(valid).toBe(true)
  })

  it('error en login si el campo está vacío', () => {
    const { valid, errors } = validateForm({ login: '', password: 'abc', captchaToken: 'tok' })
    expect(valid).toBe(false)
    expect(errors.login).toBeDefined()
  })

  it('error en login si solo contiene espacios', () => {
    const { valid, errors } = validateForm({ login: '   ', password: 'abc', captchaToken: 'tok' })
    expect(valid).toBe(false)
    expect(errors.login).toBeDefined()
  })

  it('error en password si el campo está vacío', () => {
    const { valid, errors } = validateForm({ login: 'carlos', password: '', captchaToken: 'tok' })
    expect(valid).toBe(false)
    expect(errors.password).toBeDefined()
  })

  it('error en captcha si el token está vacío', () => {
    const { valid, errors } = validateForm({ login: 'carlos', password: 'abc', captchaToken: '' })
    expect(valid).toBe(false)
    expect(errors.captcha).toBeDefined()
  })

  it('errores en los tres campos cuando todos están vacíos', () => {
    const { valid, errors } = validateForm({ login: '', password: '', captchaToken: '' })
    expect(valid).toBe(false)
    expect(errors.login).toBeDefined()
    expect(errors.password).toBeDefined()
    expect(errors.captcha).toBeDefined()
  })

  it('sin errores si login es correo electrónico válido', () => {
    const { valid } = validateForm({ login: 'carlos@movent.com', password: 'abc', captchaToken: 'tok' })
    expect(valid).toBe(true)
  })
})
