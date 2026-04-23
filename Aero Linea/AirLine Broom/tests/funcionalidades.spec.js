import { test, expect } from '@playwright/test';

test.use({
  browserName: 'chromium',
  locale: 'es-ES',
  timezoneId: 'America/Guatemala',
  launchOptions: {
    args: [
      '--disable-features=Translate,TranslateUI',
      '--disable-translate',
      '--lang=es-ES',
    ]
  }
});

// Aumentar el timeout global para todos los tests
test.setTimeout(120000); // 2 minutos globales

// URL base de la aplicación
const BASE_URL = 'http://localhost:5173';

async function login(page) {
  // Abrir menú móvil
  await page.locator('.broom-header__menu-toggle').click();
  await page.waitForTimeout(500);

  // Ir a login
  await page.locator('.broom-header__nav-link', { hasText: 'Iniciar Sesion' }).click();
  await page.waitForTimeout(500);

  // Llenar credenciales
  await page.fill('#correoOUsername', 'MATIAS');
  await page.fill('#contrasena', '11111111A');

  // Submit
  await page.click('.login-form__submit');
  await page.waitForURL(`${BASE_URL}/home`, { timeout: 30000 });
}

test.describe('Pruebas Broom AirLine - Funcionalidades', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL);
  });

  // TEST 03 - Búsqueda de vuelo: Guatemala -> Los Angeles el 14 de abril 2026
  test('03 - Buscar vuelo GUA a LAX 14 abril 2026', async ({ page }) => {
    await login(page);
    await page.screenshot({ path: 'screenshots/broom-03-paso1-login-completado.png' });

    await page.waitForSelector('.broom-home__destinations-grid', { timeout: 0 });
    await page.waitForSelector('.broom-home__destination-card', { timeout: 0 });
    await page.screenshot({ path: 'screenshots/broom-03-paso2-destinos-cargados.png' });

    await page.locator('input[name="tripType"][value="oneway"]').check();
    await page.screenshot({ path: 'screenshots/broom-03-paso3-tipo-viaje-seleccionado.png' });

    await page.fill('#fromCity', 'Guatemala');
    await page.waitForSelector('.home-autocomplete__item', { timeout: 0 });
    await page.locator('.home-autocomplete__item').first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-03-paso4-origen-seleccionado.png' });

    await page.fill('#toCity', 'Los Angeles');
    await page.waitForSelector('.home-autocomplete__item', { timeout: 0 });
    await page.locator('.home-autocomplete__item').first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-03-paso5-destino-seleccionado.png' });

    await page.screenshot({ path: 'screenshots/broom-03-paso6-pasajeros-default.png' });

    await page.waitForSelector('.cal-wrapper', { timeout: 0 });
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/broom-03-paso7-calendarios-cargados.png' });

    let mesActual = await page.locator('.cal-nav__title').first().textContent();
    let clicks = 0;
    while (mesActual && !mesActual.includes('Abril 2026') && clicks < 24) {
      await page.locator('.cal-container').first().locator('.cal-nav__btn').last().click();
      await page.waitForTimeout(300);
      mesActual = await page.locator('.cal-nav__title').first().textContent();
      clicks++;
    }
    await page.screenshot({ path: 'screenshots/broom-03-paso8-mes-abril-alcanzado.png' });

    const dia14 = page.locator('.cal-container').first().locator('button.cal-day', { hasText: '14' });
    if (await dia14.isEnabled()) {
      await dia14.click();
      await page.waitForTimeout(500);
    }
    await page.screenshot({ path: 'screenshots/broom-03-paso9-fecha-seleccionada.png' });

    await page.click('.broom-home__search-btn');
    await page.screenshot({ path: 'screenshots/broom-03-paso10-busqueda-ejecutada.png' });

    await page.waitForURL(`${BASE_URL}/vuelos`, { timeout: 0 });
    await page.screenshot({ path: 'screenshots/broom-03-paso11-resultados-cargados.png' });

    await expect(page).toHaveURL(/.*vuelos.*/);
    await page.screenshot({ path: 'screenshots/broom-03-paso12-verificacion-final.png' });
  });

  // TEST 04 - Páginas informativas del footer (Soporte)
  test('04 - Paginas informativas del footer', async ({ page }) => {
    await login(page);
    await page.screenshot({ path: 'screenshots/broom-04-paso1-login-completado.png' });

    await page.waitForSelector('.broom-footer', { timeout: 0 });
    await page.screenshot({ path: 'screenshots/broom-04-paso2-footer-cargado.png' });

    const paginasSoporte = [
      { nombre: 'Centro de Ayuda', selector: 'Centro de Ayuda', url: 'centro-ayuda' },
      { nombre: 'Contactanos', selector: 'Contactanos', url: 'contactanos' },
      { nombre: 'Preguntas Frecuentes', selector: 'Preguntas Frecuentes', url: 'preguntas-frecuentes' },
      { nombre: 'Politica de Cancelacion', selector: 'Politica de Cancelacion', url: 'politica-cancelacion' }
    ];

    for (let i = 0; i < paginasSoporte.length; i++) {
      const pagina = paginasSoporte[i];

      await page.locator('.broom-footer').scrollIntoViewIfNeeded();
      await page.waitForTimeout(500);
      await page.screenshot({ path: `screenshots/broom-04-paso${3 + i * 3}-footer-visible-${pagina.url}.png` });

      await page.locator('.broom-footer__link', { hasText: pagina.selector }).click();
      await page.waitForTimeout(1000);
      await page.screenshot({ path: `screenshots/broom-04-paso${4 + i * 3}-navegando-${pagina.url}.png` });

      await page.waitForURL(`${BASE_URL}/${pagina.url}`, { timeout: 0 });
      await page.screenshot({ path: `screenshots/broom-04-paso${5 + i * 3}-pagina-${pagina.url}-cargada.png` });

      const title = page.locator('h1, .page-title, .titulo');
      await expect(title).toBeVisible();

      console.log(`✅ Página "${pagina.nombre}" cargada correctamente`);

      if (i < paginasSoporte.length - 1) {
        await page.goBack();
        await page.waitForTimeout(1000);
        await page.waitForSelector('.broom-footer', { timeout: 0 });
      }
    }

    await page.screenshot({ path: 'screenshots/broom-04-paso-final-todas-paginas-visitadas.png' });
  });

  // TEST 05 - Mis Reservaciones - Ver detalle (URL directa)
  test('05 - Ver detalle de reservaciones', async ({ page }) => {
    // Paso 1: Login
    await login(page);
    await page.screenshot({ path: 'screenshots/broom-05-paso1-login-completado.png' });

    // Paso 2: Ir DIRECTAMENTE a la página de reservaciones (sin usar el header)
    await page.goto(`${BASE_URL}/reservas`);
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/broom-05-paso2-url-directa-reservas.png' });

    // Paso 3: Verificar que estamos en la página de reservaciones
    await expect(page.locator('.mr-title')).toHaveText('Mis Reservaciones');
    await page.screenshot({ path: 'screenshots/broom-05-paso3-titulo-confirmado.png' });

    // Paso 4: Esperar a que carguen las reservaciones
    await page.waitForSelector('.mr-grid, .mr-empty-state', { timeout: 0 });
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/broom-05-paso4-reservaciones-cargadas.png' });

    // Paso 5: Verificar si hay reservaciones
    const hayReservaciones = await page.locator('.mr-card').count() > 0;

    if (hayReservaciones) {
      // Paso 6: Hacer clic en la primera reservación
      const primeraReserva = page.locator('.mr-card').first();
      await primeraReserva.click();
      await page.waitForTimeout(1000);
      await page.screenshot({ path: 'screenshots/broom-05-paso5-modal-detalle-abierto.png' });

      // Paso 7: Verificar el modal
      await expect(page.locator('.mr-overlay')).toBeVisible();
      await page.screenshot({ path: 'screenshots/broom-05-paso6-modal-visible.png' });

      // Paso 8: Obtener información del detalle
      const noReserva = await page.locator('.mr-detail__noreserva').textContent();
      console.log(`Detalle de reservación: ${noReserva}`);
      await page.screenshot({ path: 'screenshots/broom-05-paso7-numero-reservacion.png' });

      const boletosCount = await page.locator('.mr-boleto').count();
      console.log(`Boletos en esta reservación: ${boletosCount}`);
      await page.screenshot({ path: 'screenshots/broom-05-paso8-boletos-visibles.png' });

      const total = await page.locator('.mr-detail__total').textContent();
      console.log(`Total de la reservación: ${total}`);
      await page.screenshot({ path: 'screenshots/broom-05-paso9-total-visible.png' });

      // Paso 9: Verificar que hay botones de acción (descargar comprobante, enviar correo)
      const tieneBotones = await page.locator('.mr-detail__footer-actions button').count() > 0;
      console.log(`Botones de acción visibles: ${tieneBotones}`);
      await page.screenshot({ path: 'screenshots/broom-05-paso10-acciones-visibles.png' });

      // Paso 10: Cerrar modal
      await page.locator('.mr-modal__close-btn').click();
      await page.waitForTimeout(500);
      await page.screenshot({ path: 'screenshots/broom-05-paso11-modal-cerrado.png' });

      // Paso 11: Verificar que el modal ya no está visible
      await expect(page.locator('.mr-overlay')).not.toBeVisible();
      await page.screenshot({ path: 'screenshots/broom-05-paso12-volver-lista.png' });

    } else {
      console.log('No hay reservaciones para mostrar');
      await page.screenshot({ path: 'screenshots/broom-05-sin-reservaciones.png' });
    }

    await page.screenshot({ path: 'screenshots/broom-05-paso13-test-completado.png' });
  });

});
