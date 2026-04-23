/**
 * navbar-ui.test.js
 * Pruebas unitarias para la interfaz del navbar:
 * nombre visible del usuario y lógica de display por rol.
 * Lógica extraída de Encabezado.vue.
 */

import { describe, it, expect } from 'vitest'

// ─────────────────────────────────────────────────────────────────────────────
// nombreVisible replica el computed del componente Encabezado.vue.
// Prioriza: nombre → username → usuario → 'Usuario'.
// ─────────────────────────────────────────────────────────────────────────────

function nombreVisible(sesion) {
  if (!sesion) return 'Usuario'
  return sesion.nombre || sesion.username || sesion.usuario || 'Usuario'
}

// ─────────────────────────────────────────────────────────────────────────────
// nombreVisible
// ─────────────────────────────────────────────────────────────────────────────

describe('Navbar — nombreVisible', () => {
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
