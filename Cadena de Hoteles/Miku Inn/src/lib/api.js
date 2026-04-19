/**
 * @file api.js
 * @description Exporta la URL base para todas las solicitudes a la API del backend realizadas por el
 * frontend de Hotel Inn. El valor se resuelve en tiempo de construccion desde la variable de entorno
 * de Vite VITE_API_URL, usando la direccion del servidor de desarrollo local como respaldo
 * cuando la variable no esta definida.
 */

/** URL base de la API REST del backend utilizada en todas las llamadas fetch de la aplicacion. @type {string} */
export const API = import.meta.env.VITE_API_URL || 'http://localhost:7000';
