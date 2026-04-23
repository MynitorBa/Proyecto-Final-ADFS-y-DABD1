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

test.describe('Pruebas Broom AirLine - Autenticacion', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL);
  });

  // TEST 01 - Login exitoso
  test('01 - Login exitoso', async ({ page }) => {
    await page.screenshot({ path: 'screenshots/broom-01-paso1-home-sin-sesion.png' });

    await page.locator('.broom-header__menu-toggle').click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-01-paso2-menu-abierto.png' });

    await page.locator('.broom-header__nav-link', { hasText: 'Iniciar Sesion' }).click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-01-paso3-pagina-login.png' });

    await expect(page.locator('.login__title')).toHaveText('Iniciar sesion');
    await page.screenshot({ path: 'screenshots/broom-01-paso4-titulo-login.png' });

    await page.fill('#correoOUsername', 'MATIAS');
    await page.fill('#contrasena', '11111111A');
    await page.screenshot({ path: 'screenshots/broom-01-paso5-credenciales-llenadas.png' });

    await page.click('.login-form__submit');
    await page.screenshot({ path: 'screenshots/broom-01-paso6-submit-enviado.png' });

    await page.waitForURL(`${BASE_URL}/home`, { timeout: 30000 });
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-01-paso7-home-autenticado.png' });

    await expect(page.locator('.broom-header__user')).toBeVisible();
    await page.screenshot({ path: 'screenshots/broom-01-paso8-sesion-confirmada.png' });
  });

  // TEST 02 - Logout exitoso
  test('02 - Logout exitoso', async ({ page }) => {
    await login(page);
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-02-paso1-home-autenticado.png' });

    await page.locator('.broom-header__menu-toggle').click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-02-paso2-menu-abierto.png' });

    await page.locator('.broom-header__nav-link--logout', { hasText: 'Cerrar Sesion' }).click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-02-paso3-logout-ejecutado.png' });

    await page.locator('.broom-header__menu-toggle').click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/broom-02-paso4-menu-post-logout.png' });

    await expect(page.locator('.broom-header__nav-link', { hasText: 'Iniciar Sesion' })).toBeVisible();
    await page.screenshot({ path: 'screenshots/broom-02-paso5-sesion-cerrada-confirmada.png' });
  });

});
