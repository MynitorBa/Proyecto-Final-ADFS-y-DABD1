/**
 * @file api.js
 * @description Exports the base URL for all backend API requests made by the
 * AirLine Broom frontend. The value is resolved at build time from the Vite
 * environment variable VITE_API_URL, falling back to the local development
 * server address when the variable is not defined.
 */

/** Base URL of the backend REST API used across all fetch calls in the app. @type {string} */
export const API = import.meta.env.VITE_API_URL || 'http://localhost:5190';
