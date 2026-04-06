/**
 * @file sesion.js
 * @description Svelte writable store that holds the current user session state
 * for the AirLine Broom application. Exposes helper async functions to load,
 * create and destroy a session by communicating with the backend auth API.
 * The store value follows a three-state convention:
 *   null  = session check not yet completed (initial loading state),
 *   false = no active session (unauthenticated),
 *   object = authenticated user data ({ usuarioId, nombre, correo, rolId, rolNombre }).
 */

import { writable } from 'svelte/store';

/** Reactive store holding the current session state. @type {import('svelte/store').Writable<null|false|{usuarioId: number, nombre: string, correo: string, rolId: number, rolNombre: string}>} */
export const sesion = writable(null);

import { API } from '../lib/api.js';

/**
 * Calls GET /api/auth/sesion with credentials to check whether a valid session
 * cookie already exists on the browser. If the server responds with 200 the
 * parsed JSON (user data object) is written into the store; any other status or
 * network failure sets the store to false, marking the user as unauthenticated.
 * Intended to be called once at application startup inside onMount.
 * @async
 * @returns {Promise<void>}
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
 * Sends a POST request to /api/auth/login with the provided credentials.
 * On success the server sets a session cookie and returns the user data object,
 * which is immediately written into the sesion store. On failure the store is
 * set to false and the function returns { ok: false } so the caller can display
 * an appropriate error message.
 * @async
 * @param {string} correoOUsername - The user's email address or username.
 * @param {string} contrasena - The user's plain-text password.
 * @returns {Promise<{ok: boolean, data?: object}>} Object with ok flag and, on
 *   success, the user data returned by the server.
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
 * Sends a POST request to /api/auth/logout to invalidate the session cookie on
 * the server side. Regardless of the server response the sesion store is set to
 * false, effectively logging the user out on the client immediately.
 * @async
 * @returns {Promise<void>}
 */
export async function logout() {
    await fetch(`${API}/api/auth/logout`, {
        method: 'POST',
        credentials: 'include'
    });
    sesion.set(false);
}
