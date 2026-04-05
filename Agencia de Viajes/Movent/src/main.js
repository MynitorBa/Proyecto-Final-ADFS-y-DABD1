/**
 * @file main.js
 * @description Punto de entrada principal de la aplicación Vue de MOVENT.
 * Crea la instancia de la app, registra el router y monta el componente raíz
 * en el elemento #app del DOM.
 */

import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import './style.css'

/** Crea la app, inyecta el router y la monta en el div #app del index.html. */
createApp(App).use(router).mount('#app')
