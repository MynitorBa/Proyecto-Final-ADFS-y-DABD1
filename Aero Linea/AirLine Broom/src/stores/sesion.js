import { writable } from 'svelte/store';

// null  = todavía no sabemos (cargando)
// false = no hay sesión
// { usuarioId, nombre, correo, rolId, rolNombre } = autenticado
export const sesion = writable(null);

const API = 'http://localhost:5190';

/**
 * Llama a GET /api/auth/sesion y actualiza el store.
 * Se llama al arrancar la app para restaurar sesión si la cookie sigue vigente.
 */
export async function cargarSesion() {
    try {
        const res = await fetch(`${API}/api/auth/sesion`, {
            credentials: 'include'
        });

        if (res.ok) {
            const data = await res.json();
            sesion.set(data);
        } else {
            sesion.set(false);
        }
    } catch {
        sesion.set(false);
    }
}

/**
 * Hace login, guarda la sesión en el store y devuelve true/false.
 */
export async function login(correoOUsername, contrasena) {
    const res = await fetch(`${API}/api/auth/login`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correoOUsername, contrasena })
    });

    if (res.ok) {
        const data = await res.json();
        sesion.set(data);
        return { ok: true, data };
    }

    sesion.set(false);
    return { ok: false };
}

/**
 * Hace logout, destruye la cookie y limpia el store.
 */
export async function logout() {
    await fetch(`${API}/api/auth/logout`, {
        method: 'POST',
        credentials: 'include'
    });
    sesion.set(false);
}