/**
 * @file main.js
 * @description Punto de entrada principal de la aplicacion Miku Inn.
 * Monta el componente raiz App en el elemento DOM con id "app".
 */

import { mount } from 'svelte'
import App from './App.svelte'

/**
 * Instancia principal de la aplicacion Svelte montada en el DOM.
 * @type {object}
 */
const app = mount(App, {
  target: document.getElementById('app'),
})

export default app
