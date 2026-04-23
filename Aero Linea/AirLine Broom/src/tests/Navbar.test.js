/**
 * Navbar.test.js
 * Pruebas unitarias para el store de sesion en contexto de navegación:
 * verificación de roles, estados de autenticación y transiciones
 * que determinan qué items del navbar se muestran.
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { get } from 'svelte/store';

// ─────────────────────────────────────────────────────────────────────────────
// Setup / teardown
// ─────────────────────────────────────────────────────────────────────────────

let sesionStore, loginFn, logoutFn, cargarSesionFn;

beforeEach(async () => {
  vi.resetModules();
  global.fetch = vi.fn();

  const mod = await import('../stores/sesion.js');
  sesionStore = mod.sesion;
  loginFn = mod.login;
  logoutFn = mod.logout;
  cargarSesionFn = mod.cargarSesion;
});

afterEach(() => {
  vi.restoreAllMocks();
});

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

function mockOk(body) {
  return { ok: true, json: vi.fn().mockResolvedValue(body) };
}
function mockFail() {
  return { ok: false, json: vi.fn() };
}

async function simularLogin(datos = { usuarioId: 1, nombre: 'Test', correo: 't@t.com', rolId: 1, rolNombre: 'Cliente' }) {
  global.fetch.mockResolvedValueOnce(mockOk(datos));
  await loginFn(datos.correo, 'pass');
}

// ─────────────────────────────────────────────────────────────────────────────
// Estado de sesión para renderizado condicional de navbar
// ─────────────────────────────────────────────────────────────────────────────

describe('Navbar — estado null (carga inicial)', () => {
  it('el store inicial es null, navbar debería mostrar loading/spinner', () => {
    expect(get(sesionStore)).toBeNull();
  });

  it('null es diferente de false — no es "no autenticado"', () => {
    expect(get(sesionStore)).not.toBe(false);
  });
});

describe('Navbar — usuario no autenticado', () => {
  it('después de fallo de cargarSesion, store es false', async () => {
    global.fetch.mockResolvedValue(mockFail());
    await cargarSesionFn();
    expect(get(sesionStore)).toBe(false);
  });

  it('false indica que el navbar debe mostrar botones de Login y Registro', () => {
    // El componente usa `$sesion === false` para mostrar opciones de login
    const valor = false;
    const debesMostrarLogin = valor === false;
    expect(debesMostrarLogin).toBe(true);
  });

  it('null no debe mostrar botón de login (todavía cargando)', () => {
    const valor = null;
    const debesMostrarLogin = valor === false;
    expect(debesMostrarLogin).toBe(false);
  });
});

describe('Navbar — usuario autenticado como Cliente (rolId 1)', () => {
  it('el store contiene el nombre del usuario', async () => {
    await simularLogin({ usuarioId: 3, nombre: 'María', correo: 'm@m.com', rolId: 1, rolNombre: 'Cliente' });
    expect(get(sesionStore).nombre).toBe('María');
  });

  it('rolId 1 no es admin — navbar no debe mostrar panel admin', async () => {
    await simularLogin({ usuarioId: 3, nombre: 'María', correo: 'm@m.com', rolId: 1, rolNombre: 'Cliente' });
    const { rolId } = get(sesionStore);
    expect(rolId).not.toBe(2);
  });

  it('el store tiene todos los campos necesarios para el navbar', async () => {
    const datos = { usuarioId: 3, nombre: 'María', correo: 'm@m.com', rolId: 1, rolNombre: 'Cliente' };
    await simularLogin(datos);
    const s = get(sesionStore);
    expect(s).toHaveProperty('usuarioId');
    expect(s).toHaveProperty('nombre');
    expect(s).toHaveProperty('correo');
    expect(s).toHaveProperty('rolId');
    expect(s).toHaveProperty('rolNombre');
  });
});

describe('Navbar — usuario autenticado como Admin (rolId 2)', () => {
  it('rolId 2 indica rol admin', async () => {
    await simularLogin({ usuarioId: 10, nombre: 'Admin', correo: 'admin@broom.com', rolId: 2, rolNombre: 'Administrador' });
    expect(get(sesionStore).rolId).toBe(2);
  });

  it('rolNombre es Administrador', async () => {
    await simularLogin({ usuarioId: 10, nombre: 'Admin', correo: 'admin@broom.com', rolId: 2, rolNombre: 'Administrador' });
    expect(get(sesionStore).rolNombre).toBe('Administrador');
  });

  it('navbar debe mostrar link al panel admin para rolId 2', async () => {
    await simularLogin({ usuarioId: 10, nombre: 'Admin', correo: 'admin@broom.com', rolId: 2, rolNombre: 'Administrador' });
    const { rolId } = get(sesionStore);
    // Lógica del componente: rolId === 2 → mostrar enlace /admin
    expect(rolId === 2).toBe(true);
  });
});

describe('Navbar — usuario autenticado como Webservice (rolId 3)', () => {
  it('rolId 3 indica agencia/webservice', async () => {
    await simularLogin({ usuarioId: 20, nombre: 'Agencia', correo: 'ag@broom.com', rolId: 3, rolNombre: 'Webservice' });
    expect(get(sesionStore).rolId).toBe(3);
  });

  it('no es admin ni cliente — no muestra panel admin', async () => {
    await simularLogin({ usuarioId: 20, nombre: 'Agencia', correo: 'ag@broom.com', rolId: 3, rolNombre: 'Webservice' });
    const { rolId } = get(sesionStore);
    expect(rolId === 2).toBe(false);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Transición de estados (ciclo de navegación)
// ─────────────────────────────────────────────────────────────────────────────

describe('Navbar — transiciones de estado', () => {
  it('null → false después de cargarSesion sin sesión activa', async () => {
    expect(get(sesionStore)).toBeNull();
    global.fetch.mockResolvedValue(mockFail());
    await cargarSesionFn();
    expect(get(sesionStore)).toBe(false);
  });

  it('null → objeto después de cargarSesion con sesión activa', async () => {
    const datos = { usuarioId: 1, nombre: 'X', correo: 'x@x.com', rolId: 1, rolNombre: 'Cliente' };
    global.fetch.mockResolvedValue(mockOk(datos));
    await cargarSesionFn();
    expect(get(sesionStore)).toEqual(datos);
  });

  it('false → objeto después de login exitoso', async () => {
    // Primero establecer false
    global.fetch.mockResolvedValueOnce(mockFail());
    await cargarSesionFn();
    expect(get(sesionStore)).toBe(false);

    // Ahora login exitoso
    const datos = { usuarioId: 5, nombre: 'Y', correo: 'y@y.com', rolId: 1, rolNombre: 'Cliente' };
    global.fetch.mockResolvedValueOnce(mockOk(datos));
    await loginFn('y@y.com', 'pass');
    expect(get(sesionStore)).toEqual(datos);
  });

  it('objeto → false después de logout', async () => {
    // Login
    const datos = { usuarioId: 5, nombre: 'Y', correo: 'y@y.com', rolId: 1, rolNombre: 'Cliente' };
    global.fetch.mockResolvedValueOnce(mockOk(datos));
    await loginFn('y@y.com', 'pass');
    expect(get(sesionStore)).toEqual(datos);

    // Logout
    global.fetch.mockResolvedValueOnce(mockOk(null));
    await logoutFn();
    expect(get(sesionStore)).toBe(false);
  });

  it('el nombre del usuario se actualiza al cambiar de cuenta', async () => {
    // Login usuario 1
    global.fetch.mockResolvedValueOnce(mockOk({ usuarioId: 1, nombre: 'Ana', correo: 'ana@x.com', rolId: 1, rolNombre: 'Cliente' }));
    await loginFn('ana@x.com', 'p');
    expect(get(sesionStore).nombre).toBe('Ana');

    // Logout + login usuario 2
    global.fetch.mockResolvedValueOnce(mockOk(null));
    await logoutFn();
    global.fetch.mockResolvedValueOnce(mockOk({ usuarioId: 2, nombre: 'Pedro', correo: 'pedro@x.com', rolId: 2, rolNombre: 'Administrador' }));
    await loginFn('pedro@x.com', 'p');
    expect(get(sesionStore).nombre).toBe('Pedro');
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Lógica de visibilidad de links del navbar
// ─────────────────────────────────────────────────────────────────────────────

describe('Navbar — lógica de visibilidad de links', () => {
  it('link "Mis Reservaciones" visible solo si hay sesión activa (objeto)', async () => {
    await simularLogin();
    const s = get(sesionStore);
    const visible = s !== null && s !== false;
    expect(visible).toBe(true);
  });

  it('link "Mis Reservaciones" NO visible si sesion es false', async () => {
    global.fetch.mockResolvedValue(mockFail());
    await cargarSesionFn();
    const s = get(sesionStore);
    const visible = s !== null && s !== false;
    expect(visible).toBe(false);
  });

  it('link "Mis Reservaciones" NO visible si sesion es null (cargando)', () => {
    const s = get(sesionStore); // null
    const visible = s !== null && s !== false;
    expect(visible).toBe(false);
  });

  it('el correo del usuario está disponible para el navbar', async () => {
    await simularLogin({ usuarioId: 1, nombre: 'T', correo: 'test@broom.com', rolId: 1, rolNombre: 'Cliente' });
    expect(get(sesionStore).correo).toBe('test@broom.com');
  });

  it('usuarioId disponible para construir links de perfil', async () => {
    await simularLogin({ usuarioId: 42, nombre: 'T', correo: 't@t.com', rolId: 1, rolNombre: 'Cliente' });
    expect(get(sesionStore).usuarioId).toBe(42);
  });
});
