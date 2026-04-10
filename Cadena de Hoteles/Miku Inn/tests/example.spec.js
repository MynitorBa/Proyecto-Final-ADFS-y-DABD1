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

async function login(page) {
  await page.locator('.btn-secondary').first().click();
  await page.fill('#email', 'Chuchitos');
  await page.fill('#password', 'Chuchito123');
  await page.click('.login__submit-btn');
  await page.waitForURL('**/home', { timeout: 10000 });
}

test.describe('Pruebas del Sistema', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:5173');
  });

  // TEST 01 - Login exitoso
  test('01 - Login exitoso', async ({ page }) => {
    await page.screenshot({ path: 'screenshots/01-paso1-home-sin-sesion.png' });
    await page.locator('.btn-secondary').first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/01-paso2-modal-login-abierto.png' });
    await page.fill('#email', 'Chuchitos');
    await page.fill('#password', 'Chuchito123');
    await page.screenshot({ path: 'screenshots/01-paso3-credenciales-llenadas.png' });
    await page.click('.login__submit-btn');
    await page.screenshot({ path: 'screenshots/01-paso4-submit-enviado.png' });
    await page.waitForURL('**/home', { timeout: 10000 });
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/01-paso5-home-autenticado.png' });
    await expect(page.locator('.user-button')).toBeVisible();
    await page.screenshot({ path: 'screenshots/01-paso6-sesion-confirmada.png' });
  });

  // TEST 02 - Logout exitoso
  test('02 - Logout exitoso', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/02-paso1-home-autenticado.png' });
    await page.locator('.user-button').click();
    await page.waitForSelector('.user-dropdown', { state: 'visible' });
    await page.waitForTimeout(300);
    await page.screenshot({ path: 'screenshots/02-paso2-dropdown-abierto.png' });
    await page.locator('.dropdown-item.logout-button').click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/02-paso3-logout-ejecutado.png' });
    await expect(page.locator('.btn-secondary')).toBeVisible();
    await page.screenshot({ path: 'screenshots/02-paso4-sesion-cerrada-confirmada.png' });
  });

  // TEST 03 - Buscar hotel en Guatemala City
  test('03 - Buscar hotel en Guatemala City', async ({ page }) => {
    await page.screenshot({ path: 'screenshots/03-paso1-home.png' });
    await page.fill('#h-pais', 'Guatemala');
    await page.screenshot({ path: 'screenshots/03-paso2-pais-escrito.png' });
    await page.waitForSelector('.home__autocomplete-list', { state: 'visible', timeout: 8000 });
    await page.screenshot({ path: 'screenshots/03-paso3-sugerencias-pais-visibles.png' });
    await page.locator('.home__autocomplete-btn', { hasText: 'Guatemala' }).first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/03-paso4-pais-seleccionado.png' });
    await page.waitForFunction(() => {
      const input = document.querySelector('#h-ciudad');
      return input && !input.disabled;
    }, { timeout: 15000 });
    await page.screenshot({ path: 'screenshots/03-paso5-ciudades-cargadas.png' });
    await page.fill('#h-ciudad', 'Guatemala City');
    await page.screenshot({ path: 'screenshots/03-paso6-ciudad-escrita.png' });
    await page.waitForSelector('.home__autocomplete-list', { state: 'visible', timeout: 5000 });
    await page.screenshot({ path: 'screenshots/03-paso7-sugerencias-ciudad-visibles.png' });
    await page.locator('.home__autocomplete-btn', { hasText: 'Guatemala City' }).first().click();
    await page.waitForTimeout(300);
    await page.screenshot({ path: 'screenshots/03-paso8-ciudad-seleccionada.png' });
    await page.fill('#h-checkin', '2026-04-08');
    await page.fill('#h-checkout', '2026-04-13');
    await page.selectOption('#h-personas', '2');
    await page.screenshot({ path: 'screenshots/03-paso9-fechas-y-huespedes.png' });
    await page.locator('.search-button').click();
    await page.screenshot({ path: 'screenshots/03-paso10-busqueda-enviada.png' });
    await page.waitForURL('**/search-results', { timeout: 15000 });
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/03-paso11-resultados-cargados.png' });
    await expect(page).toHaveURL(/search-results/);
    await page.screenshot({ path: 'screenshots/03-paso12-url-confirmada.png' });
  });

  // TEST 04 - Cambiar teléfono en perfil
    // TEST 04 - Cambiar teléfono en perfil (CORREGIDO)
  test('04 - Cambiar teléfono en perfil', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/04-paso1-home-autenticado.png' });

    await page.goto('http://localhost:5173/profile');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/04-paso2-perfil-cargando.png' });

    await page.waitForSelector('.profile-loading', { state: 'hidden', timeout: 10000 }).catch(() => {});
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/04-paso3-perfil-cargado.png' });

    // Paso 4: Seleccionar país (escribir y seleccionar de la lista)
    await page.locator('#pais-telefono').click();
    await page.locator('#pais-telefono').fill('Guatemala');
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/04-paso4-pais-escrito.png' });

    // Esperar que aparezcan las sugerencias y seleccionar Guatemala
    await page.waitForSelector('.profile-autocomplete-list', { state: 'visible', timeout: 5000 });
    await page.screenshot({ path: 'screenshots/04-paso5-sugerencias-pais.png' });
    await page.locator('.profile-autocomplete-btn', { hasText: 'Guatemala' }).first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/04-paso6-pais-seleccionado.png' });

    // Paso 5: Escribir nuevo teléfono (8 dígitos para Guatemala)
    await page.locator('#telefono').click();
    await page.locator('#telefono').fill('');
    await page.locator('#telefono').fill('5555 5555');
    await page.screenshot({ path: 'screenshots/04-paso7-telefono-escrito.png' });

    // Verificar que el número está completo (debe mostrar "✓ Número completo")
    await expect(page.locator('.profile-match.ok')).toBeVisible({ timeout: 5000 });
    await page.screenshot({ path: 'screenshots/04-paso8-numero-completo.png' });

    // Paso 6: Guardar
    await page.locator('.profile-btn-primary').first().click();
    await page.screenshot({ path: 'screenshots/04-paso9-guardando.png' });

    // Verificar mensaje de éxito
    await expect(page.locator('.profile-alert.success')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: 'screenshots/04-paso10-exito-confirmado.png' });
  });
  
  // TEST 05 - Ver detalles de una reservación
  test('05 - Ver detalles de una reservación', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/05-paso1-home-autenticado.png' });

    await page.goto('http://localhost:5173/reservations');
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/05-paso2-reservas-cargando.png' });

    await page.waitForFunction(() => {
      const loading = document.querySelector('.empty');
      return !loading || !loading.textContent?.includes('Cargando');
    }, { timeout: 15000 });
    await page.screenshot({ path: 'screenshots/05-paso3-reservas-cargadas.png' });

    const cards = page.locator('.card');
    const count = await cards.count();
    expect(count).toBeGreaterThan(0);
    await page.screenshot({ path: 'screenshots/05-paso4-lista-reservas.png' });

    const hotelName = await cards.first().locator('.info h3').textContent();
    await page.screenshot({ path: 'screenshots/05-paso5-hotel-identificado.png' });

    await cards.first().locator('.abtn.primary', { hasText: 'Ver Detalles' }).click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/05-paso6-panel-abierto.png' });

    await expect(page.locator('.panel-overlay')).toBeVisible();
    await page.screenshot({ path: 'screenshots/05-paso7-panel-visible.png' });

    const panelHotel = await page.locator('.panel-hotel-name').textContent();
    expect(panelHotel?.trim()).toBe(hotelName?.trim());
    await page.screenshot({ path: 'screenshots/05-paso8-hotel-coincide.png' });

    await expect(page.locator('.panel-totals')).toBeVisible();
    await page.screenshot({ path: 'screenshots/05-paso9-desglose-costos.png' });

    await expect(page.locator('.panel-download-btn')).toBeVisible();
    await page.screenshot({ path: 'screenshots/05-paso10-boton-factura.png' });

    await page.locator('.panel-close').click();
    await page.waitForTimeout(300);
    await page.screenshot({ path: 'screenshots/05-paso11-panel-cerrado.png' });

    await expect(page.locator('.panel-overlay')).not.toBeVisible();
    await page.screenshot({ path: 'screenshots/05-paso12-final.png' });
  });

});