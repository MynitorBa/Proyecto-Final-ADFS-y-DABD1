/**
 * @file tests/registerUtils.test.js
 * @description Suite completa de pruebas unitarias para src/lib/registerUtils.js
 * (funciones puras extraídas de src/pages/Register.svelte)
 *
 * Funciones bajo prueba:
 *   validatePassword · getPasswordStrength · calculateAge
 *   formatLocalPhone · getPhonePlaceholder · validateForm
 *
 * Ejecutar solo esta suite:
 *   npm run test:register
 */

import {
  validatePassword,
  getPasswordStrength,
  calculateAge,
  formatLocalPhone,
  getPhonePlaceholder,
  validateForm,
} from '../src/lib/registerUtils.js';

// ═══════════════════════════════════════════════════════════════════
// GRUPO 1 — validatePassword
// ═══════════════════════════════════════════════════════════════════
describe('validatePassword', () => {

  test('TC-01 | contraseña completamente válida cumple los 5 requisitos', () => {
    const r = validatePassword('Abc1@xyz!');

    expect(r.minLength).toBe(true);
    expect(r.hasUpperCase).toBe(true);
    expect(r.hasLowerCase).toBe(true);
    expect(r.hasNumber).toBe(true);
    expect(r.hasSpecial).toBe(true);
  });

  test('TC-02 | contraseña de 7 chars falla minLength; los demás pueden cumplirse', () => {
    const r = validatePassword('Abc1@xy');   // 7 chars

    expect(r.minLength).toBe(false);
    expect(r.hasUpperCase).toBe(true);
    expect(r.hasLowerCase).toBe(true);
    expect(r.hasNumber).toBe(true);
    expect(r.hasSpecial).toBe(true);
  });

  test('TC-03 | exactamente 8 chars cumple minLength', () => {
    expect(validatePassword('Abc1@xyz').minLength).toBe(true);   // 8 ✓
    expect(validatePassword('Abc1@xy').minLength).toBe(false);   // 7 ✗
  });

  test('TC-04 | solo minúsculas falla hasUpperCase', () => {
    const r = validatePassword('abcdefg1');

    expect(r.hasUpperCase).toBe(false);
    expect(r.hasLowerCase).toBe(true);
  });

  test('TC-05 | solo mayúsculas falla hasLowerCase', () => {
    const r = validatePassword('ABCDEFG1');

    expect(r.hasLowerCase).toBe(false);
    expect(r.hasUpperCase).toBe(true);
  });

  test('TC-06 | sin números falla hasNumber', () => {
    expect(validatePassword('Abcdefgh!').hasNumber).toBe(false);
  });

  test('TC-07 | sin caracteres especiales falla hasSpecial', () => {
    expect(validatePassword('Abcdefg1').hasSpecial).toBe(false);
  });

  test('TC-08 | contraseña vacía falla todos los requisitos', () => {
    const r = validatePassword('');

    expect(r.minLength).toBe(false);
    expect(r.hasUpperCase).toBe(false);
    expect(r.hasLowerCase).toBe(false);
    expect(r.hasNumber).toBe(false);
    expect(r.hasSpecial).toBe(false);
  });

  test('TC-09 | todos los caracteres especiales listados son reconocidos', () => {
    '!@#$%^&*(),.?":{}|<>'.split('').forEach(char => {
      expect(validatePassword(`Abcdefg1${char}`).hasSpecial).toBe(true);
    });
  });

  test('TC-10 | retorna siempre un objeto con las 5 claves correctas', () => {
    const r = validatePassword('cualquierCosa');

    expect(r).toHaveProperty('minLength');
    expect(r).toHaveProperty('hasUpperCase');
    expect(r).toHaveProperty('hasLowerCase');
    expect(r).toHaveProperty('hasNumber');
    expect(r).toHaveProperty('hasSpecial');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 2 — getPasswordStrength
// ═══════════════════════════════════════════════════════════════════
describe('getPasswordStrength', () => {

  test('TC-11 | 0 requisitos → Muy débil (rojo, 25%)', () => {
    const r = getPasswordStrength('');  // 0 cumplidos

    expect(r.text).toBe('Muy débil');
    expect(r.color).toBe('#ef4444');
    expect(r.width).toBe('25%');
  });

  test('TC-12 | 2 requisitos → Muy débil', () => {
    // "abcdefgh": cumple minLength + hasLowerCase = 2
    const r = getPasswordStrength('abcdefgh');

    expect(r.text).toBe('Muy débil');
  });

  test('TC-13 | 3 requisitos → Débil (amarillo, 50%)', () => {
    // "abcdefgH": cumple minLength + hasLower + hasUpper = 3
    const r = getPasswordStrength('abcdefgH');

    expect(r.text).toBe('Débil');
    expect(r.color).toBe('#f59e0b');
    expect(r.width).toBe('50%');
  });

  test('TC-14 | 4 requisitos → Buena (azul, 75%)', () => {
    // "abcdefH1": cumple minLength + hasLower + hasUpper + hasNumber = 4
    const r = getPasswordStrength('abcdefH1');

    expect(r.text).toBe('Buena');
    expect(r.color).toBe('#3b82f6');
    expect(r.width).toBe('75%');
  });

  test('TC-15 | 5 requisitos → Excelente (verde, 100%)', () => {
    const r = getPasswordStrength('Abc1@xyz!');

    expect(r.text).toBe('Excelente');
    expect(r.color).toBe('#10b981');
    expect(r.width).toBe('100%');
  });

  test('TC-16 | siempre retorna { text, color, width }', () => {
    const r = getPasswordStrength('test');

    expect(r).toHaveProperty('text');
    expect(r).toHaveProperty('color');
    expect(r).toHaveProperty('width');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 3 — calculateAge
// ═══════════════════════════════════════════════════════════════════
describe('calculateAge', () => {

  test('TC-17 | fecha vacía, null o undefined → devuelve 0', () => {
    expect(calculateAge('')).toBe(0);
    expect(calculateAge(null)).toBe(0);
    expect(calculateAge(undefined)).toBe(0);
  });

  test('TC-18 | nacido en 2000 → mayor de 18 en 2026', () => {
    expect(calculateAge('2000-01-15')).toBeGreaterThanOrEqual(18);
  });

  test('TC-19 | nacido en 2020 → menor de 18 en 2026', () => {
    expect(calculateAge('2020-06-15')).toBeLessThan(18);
  });

  test('TC-20 | cumple 18 exactamente hoy → edad = 18', () => {
    const today = new Date();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const birth = `${today.getFullYear() - 18}-${mm}-${dd}`;

    expect(calculateAge(birth)).toBe(18);
  });

  test('TC-21 | cumpleaños es mañana (aún no pasó este año) → un año menos', () => {
    const today    = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const mm   = String(tomorrow.getMonth() + 1).padStart(2, '0');
    const dd   = String(tomorrow.getDate()).padStart(2, '0');
    const birth = `${today.getFullYear() - 26}-${mm}-${dd}`;

    expect(calculateAge(birth)).toBe(25);
  });

  test('TC-22 | devuelve siempre un número entero', () => {
    expect(Number.isInteger(calculateAge('1990-07-04'))).toBe(true);
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 4 — formatLocalPhone
// ═══════════════════════════════════════════════════════════════════
describe('formatLocalPhone', () => {

  test('TC-23 | total ≤ 7 → bloques 3+4  ("123 4567")', () => {
    expect(formatLocalPhone('1234567', 7)).toBe('123 4567');
  });

  test('TC-24 | total = 8 → bloques 4+4  ("1234 5678") — Guatemala +502', () => {
    expect(formatLocalPhone('12345678', 8)).toBe('1234 5678');
  });

  test('TC-25 | total = 9 → bloques 3+3+3 ("123 456 789")', () => {
    expect(formatLocalPhone('123456789', 9)).toBe('123 456 789');
  });

  test('TC-26 | total = 10 → bloques 3+3+4 ("123 456 7890") — EE.UU.', () => {
    expect(formatLocalPhone('1234567890', 10)).toBe('123 456 7890');
  });

  test('TC-27 | total = 11 → bloques 2+4+5 ("12 3456 78901") — Brasil/China', () => {
    expect(formatLocalPhone('12345678901', 11)).toBe('12 3456 78901');
  });

  test('TC-28 | dígitos parciales no tienen espacios al final (trim)', () => {
    const result = formatLocalPhone('1234', 8);

    expect(result).toBe(result.trim());
  });

  test('TC-29 | cadena vacía → cadena vacía', () => {
    expect(formatLocalPhone('', 8)).toBe('');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 5 — getPhonePlaceholder
// ═══════════════════════════════════════════════════════════════════
describe('getPhonePlaceholder', () => {

  test('TC-30 | 8 dígitos → "5555 5555"', () => {
    expect(getPhonePlaceholder(8)).toBe('5555 5555');
  });

  test('TC-31 | 9 dígitos → "555 555 555"', () => {
    expect(getPhonePlaceholder(9)).toBe('555 555 555');
  });

  test('TC-32 | 10 dígitos → "555 555 5555"', () => {
    expect(getPhonePlaceholder(10)).toBe('555 555 5555');
  });

  test('TC-33 | 7 dígitos → patrón 3+4', () => {
    expect(getPhonePlaceholder(7)).toMatch(/\d{3} \d{4}/);
  });

  test('TC-34 | el placeholder solo contiene dígitos y espacios', () => {
    [7, 8, 9, 10, 11].forEach(n => {
      expect(getPhonePlaceholder(n)).toMatch(/^[\d ]+$/);
    });
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 6 — validateForm
// Helper que construye un estado completamente válido
// ═══════════════════════════════════════════════════════════════════

function estadoValido() {
  const password = 'Abc12345!';
  return {
    formData: {
      firstName:       'María',
      lastName:        'García',
      birthDate:       '1990-05-15',
      phone:           '5555 5555',
      pasaporte:       'AB123456',
      country:         'Guatemala',
      city:            'Guatemala City',
      username:        'maria_garcia',
      email:           'maria@example.com',
      password,
      confirmPassword: password,
    },
    userAge:                     35,
    phoneDigitCount:             8,
    paisSeleccionado:            { country: 'Guatemala', cities: ['Guatemala City'] },
    ciudadSeleccionada:          true,
    nacionalidades:              ['Guatemalan'],
    nacionalidadesSeleccionadas: [true],
    passwordValidation:          validatePassword(password),
    acceptTerms:                 true,
    acceptPrivacy:               true,
    captchaVerified:             true,
  };
}

describe('validateForm — formulario completamente válido', () => {

  test('TC-35 | todos los campos correctos → cero errores', () => {
    expect(Object.keys(validateForm(estadoValido()))).toHaveLength(0);
  });

});

describe('validateForm — nombre y apellidos', () => {

  test('TC-36 | nombre vacío → "Nombre requerido"', () => {
    const s = estadoValido(); s.formData.firstName = '';
    expect(validateForm(s).firstName).toBe('Nombre requerido');
  });

  test('TC-37 | nombre de 1 carácter → "Mínimo 2 caracteres"', () => {
    const s = estadoValido(); s.formData.firstName = 'A';
    expect(validateForm(s).firstName).toBe('Mínimo 2 caracteres');
  });

  test('TC-38 | apellido vacío → "Apellidos requeridos"', () => {
    const s = estadoValido(); s.formData.lastName = '';
    expect(validateForm(s).lastName).toBe('Apellidos requeridos');
  });

  test('TC-39 | apellido de 1 carácter → "Mínimo 2 caracteres"', () => {
    const s = estadoValido(); s.formData.lastName = 'X';
    expect(validateForm(s).lastName).toBe('Mínimo 2 caracteres');
  });

});

describe('validateForm — fecha de nacimiento / edad', () => {

  test('TC-40 | birthDate vacío → error requerida', () => {
    const s = estadoValido(); s.formData.birthDate = '';
    expect(validateForm(s).birthDate).toBe('Fecha de nacimiento requerida');
  });

  test('TC-41 | userAge < 18 → "Debes tener al menos 18 años"', () => {
    const s = estadoValido(); s.userAge = 16;
    expect(validateForm(s).birthDate).toBe('Debes tener al menos 18 años');
  });

  test('TC-42 | userAge === 18 → sin error de edad', () => {
    const s = estadoValido(); s.userAge = 18;
    expect(validateForm(s).birthDate).toBeUndefined();
  });

});

describe('validateForm — teléfono', () => {

  test('TC-43 | teléfono vacío → "Teléfono requerido"', () => {
    const s = estadoValido(); s.formData.phone = '';
    expect(validateForm(s).phone).toBe('Teléfono requerido');
  });

  test('TC-44 | solo 5 dígitos en campo de 8 → error "Número incompleto"', () => {
    const s = estadoValido();
    s.formData.phone = '12345';        // 5 dígitos
    s.phoneDigitCount = 8;
    const e = validateForm(s).phone;

    expect(e).toMatch(/Número incompleto/);
    expect(e).toMatch(/8 dígitos/);
    expect(e).toMatch(/ingresaste 5/);
  });

  test('TC-45 | 8 dígitos completos para GT → sin error', () => {
    const s = estadoValido();
    s.formData.phone = '5555 5555';    // 8 dígitos
    s.phoneDigitCount = 8;
    expect(validateForm(s).phone).toBeUndefined();
  });

});

describe('validateForm — pasaporte', () => {

  test('TC-46 | pasaporte vacío → "Pasaporte requerido"', () => {
    const s = estadoValido(); s.formData.pasaporte = '';
    expect(validateForm(s).pasaporte).toBe('Pasaporte requerido');
  });

  test('TC-47 | 4 caracteres → "Número de pasaporte inválido"', () => {
    const s = estadoValido(); s.formData.pasaporte = 'AB12';
    expect(validateForm(s).pasaporte).toBe('Número de pasaporte inválido');
  });

  test('TC-48 | 5+ caracteres → sin error', () => {
    const s = estadoValido(); s.formData.pasaporte = 'AB12345';
    expect(validateForm(s).pasaporte).toBeUndefined();
  });

});

describe('validateForm — país y ciudad', () => {

  test('TC-49 | paisSeleccionado null → error de país', () => {
    const s = estadoValido(); s.paisSeleccionado = null; s.formData.country = '';
    expect(validateForm(s).country).toBe('Selecciona un país de la lista');
  });

  test('TC-50 | ciudadSeleccionada false → error de ciudad', () => {
    const s = estadoValido(); s.ciudadSeleccionada = false; s.formData.city = '';
    expect(validateForm(s).city).toBe('Selecciona una ciudad de la lista');
  });

});

describe('validateForm — username', () => {

  test('TC-51 | username vacío → "Nombre de usuario requerido"', () => {
    const s = estadoValido(); s.formData.username = '';
    expect(validateForm(s).username).toBe('Nombre de usuario requerido');
  });

  test('TC-52 | 2 caracteres → "Mínimo 3 caracteres"', () => {
    const s = estadoValido(); s.formData.username = 'ab';
    expect(validateForm(s).username).toBe('Mínimo 3 caracteres');
  });

  test('TC-53 | 21 caracteres → "Máximo 20 caracteres"', () => {
    const s = estadoValido(); s.formData.username = 'a'.repeat(21);
    expect(validateForm(s).username).toBe('Máximo 20 caracteres');
  });

  test('TC-54 | username con espacio → error de formato', () => {
    const s = estadoValido(); s.formData.username = 'mi usuario';
    expect(validateForm(s).username).toBe('Solo letras, números, puntos y guion bajo');
  });

  test('TC-55 | punto y guion bajo permitidos → sin error', () => {
    const s = estadoValido(); s.formData.username = 'mi_usuario.ok';
    expect(validateForm(s).username).toBeUndefined();
  });

});

describe('validateForm — email', () => {

  test('TC-56 | email vacío → "Email requerido"', () => {
    const s = estadoValido(); s.formData.email = '';
    expect(validateForm(s).email).toBe('Email requerido');
  });

  test('TC-57 | sin @ → "Email inválido"', () => {
    const s = estadoValido(); s.formData.email = 'nodomain.com';
    expect(validateForm(s).email).toBe('Email inválido');
  });

  test('TC-58 | sin dominio → "Email inválido"', () => {
    const s = estadoValido(); s.formData.email = 'user@';
    expect(validateForm(s).email).toBe('Email inválido');
  });

  test('TC-59 | email correcto → sin error', () => {
    const s = estadoValido(); s.formData.email = 'usuario@dominio.com';
    expect(validateForm(s).email).toBeUndefined();
  });

});

describe('validateForm — contraseña y confirmación', () => {

  test('TC-60 | contraseña vacía → "Contraseña requerida"', () => {
    const s = estadoValido();
    s.formData.password = '';
    s.passwordValidation = validatePassword('');
    expect(validateForm(s).password).toBe('Contraseña requerida');
  });

  test('TC-61 | sin mayúsculas → "La contraseña no cumple los requisitos"', () => {
    const s = estadoValido();
    s.formData.password = 'abc12345!';
    s.passwordValidation = validatePassword('abc12345!');
    expect(validateForm(s).password).toBe('La contraseña no cumple los requisitos');
  });

  test('TC-62 | confirmación vacía → "Confirma tu contraseña"', () => {
    const s = estadoValido(); s.formData.confirmPassword = '';
    expect(validateForm(s).confirmPassword).toBe('Confirma tu contraseña');
  });

  test('TC-63 | contraseñas distintas → "Las contraseñas no coinciden"', () => {
    const s = estadoValido(); s.formData.confirmPassword = 'OtraContra1!';
    expect(validateForm(s).confirmPassword).toBe('Las contraseñas no coinciden');
  });

});

describe('validateForm — términos, privacidad y captcha', () => {

  test('TC-64 | sin aceptar términos → error de términos', () => {
    const s = estadoValido(); s.acceptTerms = false;
    expect(validateForm(s).terms).toBe('Debes aceptar los términos y condiciones');
  });

  test('TC-65 | sin aceptar privacidad → error de privacidad', () => {
    const s = estadoValido(); s.acceptPrivacy = false;
    expect(validateForm(s).privacy).toBe('Debes aceptar la política de privacidad');
  });

  test('TC-66 | captcha no verificado → error de captcha', () => {
    const s = estadoValido(); s.captchaVerified = false;
    expect(validateForm(s).captcha).toBe('Por favor verifica que no eres un robot');
  });

  test('TC-67 | sin nacionalidades → error de nacionalidades', () => {
    const s = estadoValido();
    s.nacionalidades = [''];
    s.nacionalidadesSeleccionadas = [false];
    expect(validateForm(s).nacionalidades).toBe('Selecciona al menos una nacionalidad');
  });

});

describe('validateForm — múltiples errores', () => {

  test('TC-68 | formulario vacío → errores en todos los campos obligatorios', () => {
    const s = {
      formData: {
        firstName: '', lastName: '', birthDate: '', phone: '',
        pasaporte: '', country: '', city: '',
        username: '', email: '', password: '', confirmPassword: '',
      },
      userAge: 0, phoneDigitCount: 8,
      paisSeleccionado: null, ciudadSeleccionada: false,
      nacionalidades: [''], nacionalidadesSeleccionadas: [false],
      passwordValidation: validatePassword(''),
      acceptTerms: false, acceptPrivacy: false, captchaVerified: false,
    };
    const e = validateForm(s);

    expect(e.firstName).toBeDefined();
    expect(e.lastName).toBeDefined();
    expect(e.birthDate).toBeDefined();
    expect(e.phone).toBeDefined();
    expect(e.pasaporte).toBeDefined();
    expect(e.country).toBeDefined();
    expect(e.city).toBeDefined();
    expect(e.username).toBeDefined();
    expect(e.email).toBeDefined();
    expect(e.password).toBeDefined();
    expect(e.confirmPassword).toBeDefined();
    expect(e.terms).toBeDefined();
    expect(e.privacy).toBeDefined();
    expect(e.captcha).toBeDefined();
    expect(e.nacionalidades).toBeDefined();
  });

  test('TC-69 | un solo error (email) → todos los demás campos limpios', () => {
    const s = estadoValido(); s.formData.email = 'no-es-un-email';
    const e = validateForm(s);

    expect(e.email).toBe('Email inválido');
    expect(e.firstName).toBeUndefined();
    expect(e.password).toBeUndefined();
    expect(e.captcha).toBeUndefined();
    expect(e.terms).toBeUndefined();
  });

});