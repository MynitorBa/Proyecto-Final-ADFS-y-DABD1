/**
 * @file sesion.js
 * @description Almacen reactivo (writable) de Svelte que contiene el estado de la sesion del usuario
 * para la aplicacion AirLine Broom. Expone funciones async auxiliares para cargar,
 * crear y destruir una sesion comunicandose con la API de autenticacion del backend.
 * El valor del almacen sigue una convencion de tres estados:
 *   null  = verificacion de sesion aun no completada (estado de carga inicial),
 *   false = sin sesion activa (no autenticado),
 *   object = datos del usuario autenticado ({ usuarioId, nombre, correo, rolId, rolNombre }).
 */

import { writable } from 'svelte/store';

/** Almacen reactivo que contiene el estado de la sesion actual. @type {import('svelte/store').Writable<null|false|{usuarioId: number, nombre: string, correo: string, rolId: number, rolNombre: string}>} */
export const sesion = writable(null);

import { API } from '../lib/api.js';

/**
 * Llama a GET /api/auth/sesion con credenciales para verificar si ya existe una cookie de sesion
 * valida en el navegador. Si el servidor responde con 200, el JSON parseado (objeto de datos del usuario)
 * se escribe en el almacen; cualquier otro estado o fallo de red establece el almacen en false,
 * marcando al usuario como no autenticado.
 * Se recomienda llamar esta funcion una vez al inicio de la aplicacion dentro de onMount.
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
 * Envia una solicitud POST a /api/auth/login con las credenciales proporcionadas.
 * Si tiene exito, el servidor establece una cookie de sesion y devuelve el objeto de datos del usuario,
 * que se escribe inmediatamente en el almacen sesion. Si falla, el almacen se
 * establece en false y la funcion devuelve { ok: false } para que el llamador pueda mostrar
 * un mensaje de error apropiado.
 * @async
 * @param {string} correoOUsername - El correo electronico o nombre de usuario del usuario.
 * @param {string} contrasena - La contrasena en texto plano del usuario.
 * @returns {Promise<{ok: boolean, data?: object}>} Objeto con indicador ok y, si tuvo exito,
 *   los datos del usuario devueltos por el servidor.
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
 * Envia una solicitud POST a /api/auth/logout para invalidar la cookie de sesion en
 * el servidor. Independientemente de la respuesta del servidor, el almacen sesion se establece en
 * false, cerrando la sesion del usuario en el cliente inmediatamente.
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
