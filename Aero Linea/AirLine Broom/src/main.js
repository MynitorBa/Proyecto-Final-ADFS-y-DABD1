/*
 * main.js
 * Punto de entrada de la aplicacion AirLine Broom.
 * Usa el API mount() de Svelte 5 para montar el componente raiz App
 * en el elemento #app definido en index.html.
 */

import { mount } from 'svelte'
import App from './App.svelte'

/* Instancia montada de la aplicacion Svelte adjunta al elemento #app del DOM */
const app = mount(App, {
  target: document.getElementById('app'),
})

export default app
