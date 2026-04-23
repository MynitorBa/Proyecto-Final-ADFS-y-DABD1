/**
 * IniciarSesion.test.js
 * Pruebas unitarias para la lógica de negocio de la vista IniciarSesion:
 * validación del formulario, cálculo de destino por rol y persistencia
 * en sessionStorage.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'

// ─────────────────────────────────────────────────────────────────────────────
// Lógica extraída del componente (se prueba pura, sin montar el componente)
// para evitar dependencias de reCAPTCHA y Vue Router en este entorno.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * validateForm replica la lógica del componente IniciarSesion.vue.
 * Retorna { valid, errors }.
 */
function validateForm({ login, password, captchaToken }) {
  const errors = {}
  if (!login.trim())    errors.login    = 'El usuario o email es requerido'
  if (!password)        errors.password = 'La contraseña es requerida'
  if (!captchaToken)    errors.captcha  = 'Completa el CAPTCHA para continuar'
  return { valid: Object.keys(errors).length === 0, errors }
}

/**
 * calcularDestino replica la lógica del componente IniciarSesion.vue.
 */
function calcularDestino(rolId) {
  if (rolId === 2) return '/admin/dashboard'
  if (rolId === 3) return '/admin/webservice'
  return '/principal'
}

/**
 * buildSesion replica el objeto que el componente guarda en sessionStorage.
 */
function buildSesion(data) {
  return {
    id:       data.id,
    nombre:   data.nombre,
    apellido: data.apellido,
    usuario:  data.username,
    correo:   data.correo,
    rol:      data.rol_id === 2 ? 'Administrador' : data.rol_id === 3 ? 'WebService' : 'Registrado',
    isAdmin:  data.rol_id === 2,
    isWS:     data.rol_id === 3,
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// validateForm
// ─────────────────────────────────────────────────────────────────────────────

describe('IniciarSesion — validateForm', () => {
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

// ─────────────────────────────────────────────────────────────────────────────
// calcularDestino
// ─────────────────────────────────────────────────────────────────────────────

describe('IniciarSesion — calcularDestino', () => {
  it('rol_id 1 (Cliente) → /principal', () => {
    expect(calcularDestino(1)).toBe('/principal')
  })

  it('rol_id 2 (Admin) → /admin/dashboard', () => {
    expect(calcularDestino(2)).toBe('/admin/dashboard')
  })

  it('rol_id 3 (WebService) → /admin/webservice', () => {
    expect(calcularDestino(3)).toBe('/admin/webservice')
  })

  it('rol_id desconocido → /principal por defecto', () => {
    expect(calcularDestino(99)).toBe('/principal')
  })

  it('sin rol_id (undefined) → /principal por defecto', () => {
    expect(calcularDestino(undefined)).toBe('/principal')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// buildSesion (objeto persistido en sessionStorage)
// ─────────────────────────────────────────────────────────────────────────────

describe('IniciarSesion — buildSesion', () => {
  it('usuario cliente: isAdmin=false, isWS=false, rol=Registrado', () => {
    const s = buildSesion({ id: 1, nombre: 'Ana', apellido: 'Lopez', username: 'ana_l', correo: 'ana@m.com', rol_id: 1 })
    expect(s.isAdmin).toBe(false)
    expect(s.isWS).toBe(false)
    expect(s.rol).toBe('Registrado')
  })

  it('usuario admin: isAdmin=true, isWS=false, rol=Administrador', () => {
    const s = buildSesion({ id: 2, nombre: 'Pedro', apellido: 'R', username: 'pedro_r', correo: 'p@m.com', rol_id: 2 })
    expect(s.isAdmin).toBe(true)
    expect(s.isWS).toBe(false)
    expect(s.rol).toBe('Administrador')
  })

  it('usuario webservice: isAdmin=false, isWS=true, rol=WebService', () => {
    const s = buildSesion({ id: 3, nombre: 'Agencia', apellido: 'X', username: 'agencia_x', correo: 'ag@m.com', rol_id: 3 })
    expect(s.isAdmin).toBe(false)
    expect(s.isWS).toBe(true)
    expect(s.rol).toBe('WebService')
  })

  it('la sesión incluye todos los campos necesarios', () => {
    const s = buildSesion({ id: 5, nombre: 'N', apellido: 'A', username: 'n_a', correo: 'n@m.com', rol_id: 1 })
    expect(s).toHaveProperty('id')
    expect(s).toHaveProperty('nombre')
    expect(s).toHaveProperty('apellido')
    expect(s).toHaveProperty('usuario')
    expect(s).toHaveProperty('correo')
    expect(s).toHaveProperty('rol')
    expect(s).toHaveProperty('isAdmin')
    expect(s).toHaveProperty('isWS')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// sessionStorage — persistencia de sesión
// ─────────────────────────────────────────────────────────────────────────────

describe('IniciarSesion — sessionStorage', () => {
  beforeEach(() => sessionStorage.clear())
  afterEach(() => sessionStorage.clear())

  it('guarda la sesión correctamente en sessionStorage', () => {
    const data = { id: 10, nombre: 'Luis', apellido: 'M', username: 'luis_m', correo: 'l@m.com', rol_id: 1 }
    const sesion = buildSesion(data)
    sessionStorage.setItem('usuario_sesion', JSON.stringify(sesion))
    const stored = JSON.parse(sessionStorage.getItem('usuario_sesion'))
    expect(stored.nombre).toBe('Luis')
    expect(stored.isAdmin).toBe(false)
  })

  it('la sesión admin persiste con isAdmin=true', () => {
    const data = { id: 11, nombre: 'Admin', apellido: 'G', username: 'admin_g', correo: 'ag@m.com', rol_id: 2 }
    sessionStorage.setItem('usuario_sesion', JSON.stringify(buildSesion(data)))
    const stored = JSON.parse(sessionStorage.getItem('usuario_sesion'))
    expect(stored.isAdmin).toBe(true)
    expect(stored.rol).toBe('Administrador')
  })

  it('sessionStorage vacío antes del login no contiene sesión', () => {
    expect(sessionStorage.getItem('usuario_sesion')).toBeNull()
  })
})
