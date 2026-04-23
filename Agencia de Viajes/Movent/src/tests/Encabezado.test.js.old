/**
 * Encabezado.test.js
 * Pruebas unitarias para la lógica de sesión del componente Encabezado:
 * cargarSesion(), nombreVisible y flags isAdmin/isWS que controlan
 * qué elementos del navbar se muestran.
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'

// ─────────────────────────────────────────────────────────────────────────────
// Lógica extraída del componente Encabezado.vue
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

/**
 * nombreVisible replica el computed del componente.
 * Prioriza: nombre → username → usuario → 'Usuario'.
 */
function nombreVisible(sesion) {
  if (!sesion) return 'Usuario'
  return sesion.nombre || sesion.username || sesion.usuario || 'Usuario'
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

function guardarSesion(obj) {
  sessionStorage.setItem('usuario_sesion', JSON.stringify(obj))
}

// ─────────────────────────────────────────────────────────────────────────────
// cargarSesion — sin sesión
// ─────────────────────────────────────────────────────────────────────────────

describe('Encabezado — cargarSesion sin datos', () => {
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

describe('Encabezado — cargarSesion usuario cliente', () => {
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

describe('Encabezado — cargarSesion usuario administrador', () => {
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

describe('Encabezado — cargarSesion usuario WebService', () => {
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

// ─────────────────────────────────────────────────────────────────────────────
// nombreVisible
// ─────────────────────────────────────────────────────────────────────────────

describe('Encabezado — nombreVisible', () => {
  it('retorna nombre si está disponible', () => {
    expect(nombreVisible({ nombre: 'Ana', usuario: 'ana_l' })).toBe('Ana')
  })

  it('retorna usuario si nombre no está disponible', () => {
    expect(nombreVisible({ usuario: 'ana_l' })).toBe('ana_l')
  })

  it('retorna "Usuario" si sesion es null', () => {
    expect(nombreVisible(null)).toBe('Usuario')
  })

  it('retorna "Usuario" si todos los campos de nombre están vacíos', () => {
    expect(nombreVisible({ nombre: '', username: '', usuario: '' })).toBe('Usuario')
  })
})
