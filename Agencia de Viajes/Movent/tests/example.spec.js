import { test, expect } from '@playwright/test';

test.use({ 
  browserName: 'chromium',
  locale: 'es-ES', 
  timezoneId: 'America/Guatemala',
});

test.setTimeout(180000);

const BASE_URL = 'http://localhost:5173';

async function login(page, username, password) {
  await page.goto(`${BASE_URL}/ingreso`);
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'screenshots/movent-login-paso1-pagina-login.png' });
  
  await page.fill('#login', username);
  await page.fill('#password', password);
  await page.screenshot({ path: 'screenshots/movent-login-paso2-credenciales-llenadas.png' });
  
  await page.click('.submit-btn');
  await page.waitForTimeout(2000);
  
  const captchaFrame = page.frameLocator('iframe[src*="recaptcha"]').first();
  const captchaVisible = await captchaFrame.locator('.recaptcha-checkbox-border').isVisible().catch(() => false);
  
  if (captchaVisible) {
    console.log('CAPTCHA detectado. Resuelvelo manualmente...');
    await page.screenshot({ path: 'screenshots/movent-login-paso3-captcha-detectado.png' });
    
    try {
      await captchaFrame.locator('.recaptcha-checkbox-border').click();
      await page.waitForTimeout(2000);
    } catch (e) {
      console.log('Click automatico fallido');
    }
    
    for (let i = 0; i < 60; i++) {
      await page.waitForTimeout(1000);
      const captchaResuelto = await captchaFrame.locator('.recaptcha-checkbox-checked').isVisible().catch(() => false);
      if (captchaResuelto) break;
    }
    
    await page.click('.submit-btn');
    await page.waitForTimeout(2000);
  }
  
  await page.waitForURL(`${BASE_URL}/principal`, { timeout: 60000 });
  await page.waitForTimeout(2000);
  await page.screenshot({ path: 'screenshots/movent-login-paso4-login-exitoso.png' });
}

test.describe('Pruebas Movent - Agencia de Viajes', () => {

  // TEST 01 - Login exitoso
  test('01 - Login exitoso', async ({ page }) => {
    console.log('TEST 01: Login');
    
    await login(page, 'Chuchitos', 'Chuchitos123');
    
    await page.waitForSelector('.user-chip', { timeout: 10000 });
    await expect(page.locator('.user-chip')).toBeVisible();
    await page.screenshot({ path: 'screenshots/test01-paso1-usuario-logueado.png' });
  });

  // TEST 02 - Logout exitoso
  test('02 - Logout exitoso', async ({ page }) => {
    console.log('TEST 02: Logout');
    
    await login(page, 'Chuchitos', 'Chuchitos123');
    await page.waitForSelector('.user-chip', { timeout: 10000 });
    await page.screenshot({ path: 'screenshots/test02-paso1-usuario-logueado.png' });
    
    await page.locator('.user-chip').click();
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/test02-paso2-menu-desplegado.png' });
    
    await page.waitForSelector('.user-dropdown', { timeout: 5000 });
    await page.screenshot({ path: 'screenshots/test02-paso3-dropdown-visible.png' });
    
    const cerrarSesionBtn = page.locator('button:has-text("Cerrar sesión")');
    await cerrarSesionBtn.waitFor({ timeout: 5000 });
    await cerrarSesionBtn.click();
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/test02-paso4-click-cerrar-sesion.png' });
    
    await page.waitForURL(`${BASE_URL}/principal`, { timeout: 10000 });
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'screenshots/test02-paso5-post-logout.png' });
    
    const userChipExists = await page.locator('.user-chip').isVisible().catch(() => false);
    expect(userChipExists).toBeFalsy();
    
    await expect(page.locator('.btn-secondary', { hasText: 'Iniciar Sesión' })).toBeVisible();
    await page.screenshot({ path: 'screenshots/test02-paso6-sesion-cerrada.png' });
  });

  // TEST 03 - Buscar vuelo Flores a Guatemala City
  test('03 - Buscar vuelo Flores a Guatemala City', async ({ page }) => {
    console.log('TEST 03: Buscar vuelo Flores -> Guatemala City');
    
    await login(page, 'Chuchitos', 'Chuchitos123');
    await page.waitForSelector('.user-chip', { timeout: 10000 });
    await page.screenshot({ path: 'screenshots/test03-paso1-usuario-logueado.png' });
    
    await page.locator('.search-tabs button', { hasText: 'Vuelos' }).click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test03-paso2-tab-vuelos.png' });
    
    const origenPaisInput = page.locator('.vuelo-card').first().locator('.campo-input').first();
    await origenPaisInput.click();
    await origenPaisInput.fill('Guatemala');
    await page.waitForTimeout(800);
    
    const sugerencias = page.locator('.inline-autocomplete button');
    await sugerencias.first().waitFor({ timeout: 5000 });
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test03-paso3-origen-pais-seleccionado.png' });
    
    const origenCiudadInput = page.locator('.vuelo-card').first().locator('.campo-input').last();
    await origenCiudadInput.click();
    await origenCiudadInput.fill('Flores');
    await page.waitForTimeout(800);
    
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test03-paso4-origen-ciudad-seleccionada.png' });
    
    const destinoPaisInput = page.locator('.vuelo-card').last().locator('.campo-input').first();
    await destinoPaisInput.click();
    await destinoPaisInput.fill('Guatemala');
    await page.waitForTimeout(800);
    
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test03-paso5-destino-pais-seleccionado.png' });
    
    const destinoCiudadInput = page.locator('.vuelo-card').last().locator('.campo-input').last();
    await destinoCiudadInput.click();
    await destinoCiudadInput.fill('Guatemala City');
    await page.waitForTimeout(800);
    
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test03-paso6-destino-ciudad-seleccionada.png' });
    
    const fechaInput = page.locator('input[type="date"]').first();
    await fechaInput.fill('2026-04-13');
    await page.screenshot({ path: 'screenshots/test03-paso7-fecha-ida.png' });
    
    await page.locator('.search-btn', { hasText: 'Buscar Vuelos' }).click();
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'screenshots/test03-paso8-buscando-vuelos.png' });
    
    await page.waitForURL('**/resultados-vuelos', { timeout: 30000 });
    await page.screenshot({ path: 'screenshots/test03-paso9-resultados-vuelos.png' });
    
    console.log('TEST 03 completado');
  });

  // TEST 04 - Buscar hoteles en Guatemala City
  test('04 - Buscar hoteles en Guatemala City', async ({ page }) => {
    console.log('TEST 04: Buscar hoteles en Guatemala City');
    
    await login(page, 'Chuchitos', 'Chuchitos123');
    await page.waitForSelector('.user-chip', { timeout: 10000 });
    await page.screenshot({ path: 'screenshots/test04-paso1-usuario-logueado.png' });
    
    await page.locator('.search-tabs button', { hasText: 'Hoteles' }).click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test04-paso2-tab-hoteles.png' });
    
    const destinoPaisInput = page.locator('.vuelo-card .campo-input').first();
    await destinoPaisInput.click();
    await destinoPaisInput.fill('Guatemala');
    await page.waitForTimeout(800);
    
    const sugerencias = page.locator('.inline-autocomplete button');
    await sugerencias.first().waitFor({ timeout: 5000 });
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test04-paso3-destino-pais-seleccionado.png' });
    
    const destinoCiudadInput = page.locator('.vuelo-card .campo-input').last();
    await destinoCiudadInput.click();
    await destinoCiudadInput.fill('Guatemala City');
    await page.waitForTimeout(800);
    
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test04-paso4-destino-ciudad-seleccionada.png' });
    
    const fechas = page.locator('input[type="date"]');
    await fechas.first().fill('2026-04-13');
    await fechas.last().fill('2026-04-17');
    await page.screenshot({ path: 'screenshots/test04-paso5-fechas-checkin-checkout.png' });
    
    await page.selectOption('select', '2');
    await page.screenshot({ path: 'screenshots/test04-paso6-personas-seleccionadas.png' });
    
    await page.locator('.search-btn', { hasText: 'Buscar Hoteles' }).click();
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'screenshots/test04-paso7-buscando-hoteles.png' });
    
    await page.waitForURL('**/resultados-hoteles', { timeout: 30000 });
    await page.screenshot({ path: 'screenshots/test04-paso8-resultados-hoteles.png' });
    
    console.log('TEST 04 completado');
  });

  // TEST 05 - Buscar paquete vuelo+hotel Flores a Guatemala City
  test('05 - Buscar paquete vuelo+hotel Flores a Guatemala City', async ({ page }) => {
    console.log('TEST 05: Buscar paquete Flores -> Guatemala City');
    
    await login(page, 'Chuchitos', 'Chuchitos123');
    await page.waitForSelector('.user-chip', { timeout: 10000 });
    await page.screenshot({ path: 'screenshots/test05-paso1-usuario-logueado.png' });
    
    await page.locator('.search-tabs button', { hasText: 'Vuelo + Hotel' }).click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test05-paso2-tab-paquete.png' });
    
    const sugerencias = page.locator('.inline-autocomplete button');
    
    const origenPaisInput = page.locator('.vuelo-card').first().locator('.campo-input').first();
    await origenPaisInput.click();
    await origenPaisInput.fill('Guatemala');
    await page.waitForTimeout(800);
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    
    const origenCiudadInput = page.locator('.vuelo-card').first().locator('.campo-input').last();
    await origenCiudadInput.click();
    await origenCiudadInput.fill('Flores');
    await page.waitForTimeout(800);
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test05-paso3-origen-flores-seleccionado.png' });
    
    const destinoPaisInput = page.locator('.vuelo-card').last().locator('.campo-input').first();
    await destinoPaisInput.click();
    await destinoPaisInput.fill('Guatemala');
    await page.waitForTimeout(800);
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    
    const destinoCiudadInput = page.locator('.vuelo-card').last().locator('.campo-input').last();
    await destinoCiudadInput.click();
    await destinoCiudadInput.fill('Guatemala City');
    await page.waitForTimeout(800);
    await sugerencias.first().click();
    await page.waitForTimeout(500);
    await page.screenshot({ path: 'screenshots/test05-paso4-destino-guatemala-seleccionado.png' });
    
    await page.locator('input[type="date"]').first().fill('2026-04-13');
    await page.screenshot({ path: 'screenshots/test05-paso5-fecha-vuelo.png' });
    
    await page.waitForTimeout(1000);
    
    await page.locator('input[type="date"]').nth(2).fill('2026-04-17');
    await page.screenshot({ path: 'screenshots/test05-paso6-fechas-hotel-4-dias.png' });
    
    await page.selectOption('select', '2');
    await page.screenshot({ path: 'screenshots/test05-paso7-personas-seleccionadas.png' });
    
    await page.locator('.search-btn', { hasText: 'Buscar Paquete Completo' }).click();
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'screenshots/test05-paso8-buscando-paquete.png' });
    
    await page.waitForURL('**/resultados-paquetes', { timeout: 30000 });
    await page.screenshot({ path: 'screenshots/test05-paso9-resultados-paquetes.png' });
    
    console.log('TEST 05 completado');
  });

});