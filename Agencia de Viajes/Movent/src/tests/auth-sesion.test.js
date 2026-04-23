/**
 * auth-sesion.test.js
 * Pruebas unitarias para la gestión de sesión:
 * calcularDestino (redirección por rol), buildSesion (construcción del objeto),
 * persistencia en sessionStorage y carga de sesión desde sessionStorage.
 * Lógica extraída de IniciarSesion.vue y Encabezado.vue.
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'

// ─────────────────────────────────────────────────────────────────────────────
// Lógica extraída de IniciarSesion.vue
// ─────────────────────────────────────────────────────────────────────────────

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
// Lógica extraída de Encabezado.vue
// ─────────────────────────────────────────────────────────────────────────────

/**
 * cargarSesion replica la función del componente Encabezado.vue.
 * Lee sessionStorage y enriquece el objeto con isAdmin e isWS.
 * Retorna el objeto de sesión o null si no hay sesión / hay error.
 */
function cargarSesion() {
  try {
    const raw = sessionStorage.getItem('usuario_sesion')
    if (!raw) return null
    const p = JSON.parse(raw)
    p.isAdmin = p.isAdmin === true || p.rol_id === 2 || p.rol === 'Administrador'
    p.isWS    = p.isWS    === true || p.rol_id === 3 || p.rol === 'WebService'
    return p
  } catch {
    return null
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

function guardarSesion(obj) {
  sessionStorage.setItem('usuario_sesion', JSON.stringify(obj))
}

// ─────────────────────────────────────────────────────────────────────────────
// calcularDestino
// ─────────────────────────────────────────────────────────────────────────────

describe('Gestion de Sesion — calcularDestino', () => {
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
// buildSesion
// ─────────────────────────────────────────────────────────────────────────────

describe('Gestion de Sesion — buildSesion', () => {
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

describe('Gestion de Sesion — sessionStorage', () => {
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

// ─────────────────────────────────────────────────────────────────────────────
// cargarSesion — sin sesión
// ─────────────────────────────────────────────────────────────────────────────

describe('Gestion de Sesion — cargarSesion sin datos', () => {
  beforeEach(() => sessionStorage.clear())
  afterEach(() => sessionStorage.clear())

  it('retorna null si sessionStorage está vacío', () => {
    expect(cargarSesion()).toBeNull()
  })

  it('retorna null si el JSON almacenado es inválido', () => {
    sessionStorage.setItem('usuario_sesion', 'no-json{{')
    expect(cargarSesion()).toBeNull()
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// cargarSesion — usuario cliente
// ─────────────────────────────────────────────────────────────────────────────

describe('Gestion de Sesion — cargarSesion usuario cliente', () => {
  beforeEach(() => {
    sessionStorage.clear()
    guardarSesion({ id: 1, nombre: 'María', usuario: 'maria_g', correo: 'm@m.com', rol: 'Registrado', isAdmin: false, isWS: false })
  })
  afterEach(() => sessionStorage.clear())

  it('retorna el objeto de sesión del cliente', () => {
    const s = cargarSesion()
    expect(s).not.toBeNull()
    expect(s.nombre).toBe('María')
  })

  it('cliente no tiene isAdmin', () => {
    const s = cargarSesion()
    expect(s.isAdmin).toBe(false)
  })

  it('cliente no tiene isWS', () => {
    const s = cargarSesion()
    expect(s.isWS).toBe(false)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// cargarSesion — usuario administrador
// ─────────────────────────────────────────────────────────────────────────────

describe('Gestion de Sesion — cargarSesion usuario administrador', () => {
  beforeEach(() => {
    sessionStorage.clear()
    guardarSesion({ id: 2, nombre: 'Carlos', usuario: 'carlos_a', correo: 'c@m.com', rol: 'Administrador', isAdmin: true, isWS: false })
  })
  afterEach(() => sessionStorage.clear())

  it('isAdmin es true para rol Administrador (flag directo)', () => {
    const s = cargarSesion()
    expect(s.isAdmin).toBe(true)
  })

  it('isAdmin es true cuando rol_id es 2', () => {
    sessionStorage.clear()
    guardarSesion({ id: 5, nombre: 'Admin2', rol_id: 2 })
    const s = cargarSesion()
    expect(s.isAdmin).toBe(true)
  })

  it('isAdmin es true cuando rol === "Administrador" (string)', () => {
    sessionStorage.clear()
    guardarSesion({ id: 6, nombre: 'Admin3', rol: 'Administrador', isAdmin: false })
    const s = cargarSesion()
    expect(s.isAdmin).toBe(true)
  })

  it('isWS es false para administrador', () => {
    const s = cargarSesion()
    expect(s.isWS).toBe(false)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// cargarSesion — usuario WebService
// ─────────────────────────────────────────────────────────────────────────────

describe('Gestion de Sesion — cargarSesion usuario WebService', () => {
  beforeEach(() => {
    sessionStorage.clear()
    guardarSesion({ id: 3, nombre: 'Agencia', usuario: 'agencia_x', correo: 'ag@m.com', rol: 'WebService', isAdmin: false, isWS: true })
  })
  afterEach(() => sessionStorage.clear())

  it('isWS es true para rol WebService (flag directo)', () => {
    const s = cargarSesion()
    expect(s.isWS).toBe(true)
  })

  it('isWS es true cuando rol_id es 3', () => {
    sessionStorage.clear()
    guardarSesion({ id: 7, nombre: 'WS2', rol_id: 3 })
    const s = cargarSesion()
    expect(s.isWS).toBe(true)
  })

  it('isAdmin es false para WebService', () => {
    const s = cargarSesion()
    expect(s.isAdmin).toBe(false)
  })

  it('navbar solo muestra Panel WS si isWS && !isAdmin', () => {
    const s = cargarSesion()
    const mostrarWS    = s?.isWS && !s?.isAdmin
    const mostrarAdmin = s?.isAdmin
    expect(mostrarWS).toBe(true)
    expect(mostrarAdmin).toBe(false)
  })
})
