/**
 * @file tests/validarFechas.test.js
 * @description Suite completa de pruebas unitarias para src/utils/validarFechas.js
 *
 * Función bajo prueba:
 *   validarFechas(checkIn, checkOut, today)
 *   Recibe tres strings YYYY-MM-DD → devuelve { valido: boolean, error: string }
 *
 * Ejecutar solo esta suite:
 *   npm run test:fechas
 */

import { validarFechas } from '../src/utils/validarFechas.js';

// Fecha fija de "hoy" — los tests no dependen del calendario real.
const TODAY = '2026-04-22';

// ═══════════════════════════════════════════════════════════════════
// GRUPO 1 — CASOS VÁLIDOS
// ═══════════════════════════════════════════════════════════════════
describe('validarFechas — casos válidos', () => {

  test('TC-01 | check-in futuro y check-out posterior → válido', () => {
    const r = validarFechas('2026-06-01', '2026-06-05', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

  test('TC-02 | check-in hoy mismo (no es pasado) → válido', () => {
    const r = validarFechas(TODAY, '2026-04-25', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

  test('TC-03 | una sola noche (días consecutivos) → válido', () => {
    const r = validarFechas('2026-07-10', '2026-07-11', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

  test('TC-04 | estadía larga de varios meses → válido', () => {
    const r = validarFechas('2026-08-01', '2027-01-31', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

  test('TC-05 | check-in mañana, check-out pasado mañana → válido', () => {
    const r = validarFechas('2026-04-23', '2026-04-24', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

  test('TC-06 | cruce de año (31-dic → 1-ene) → válido', () => {
    const r = validarFechas('2026-12-31', '2027-01-01', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 2 — CAMPOS VACÍOS / NULOS
// ═══════════════════════════════════════════════════════════════════
describe('validarFechas — campos vacíos o nulos', () => {

  test('TC-07 | check-in vacío → error check-in', () => {
    const r = validarFechas('', '2026-06-05', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('Selecciona la fecha de check-in.');
  });

  test('TC-08 | check-out vacío → error check-out', () => {
    const r = validarFechas('2026-06-01', '', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('Selecciona la fecha de check-out.');
  });

  test('TC-09 | ambos vacíos → prioriza error de check-in (se valida primero)', () => {
    const r = validarFechas('', '', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('Selecciona la fecha de check-in.');
  });

  test('TC-10 | check-in null → inválido', () => {
    const r = validarFechas(null, '2026-06-05', TODAY);

    expect(r.valido).toBe(false);
  });

  test('TC-11 | check-out undefined → inválido', () => {
    const r = validarFechas('2026-06-01', undefined, TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('Selecciona la fecha de check-out.');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 3 — CHECK-IN EN EL PASADO
// ═══════════════════════════════════════════════════════════════════
describe('validarFechas — check-in pasado', () => {

  test('TC-12 | check-in ayer → error pasado', () => {
    const r = validarFechas('2026-04-21', '2026-04-25', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-in no puede ser una fecha pasada.');
  });

  test('TC-13 | check-in hace un mes → error pasado', () => {
    const r = validarFechas('2026-03-01', '2026-03-15', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-in no puede ser una fecha pasada.');
  });

  test('TC-14 | check-in en año anterior → error pasado', () => {
    const r = validarFechas('2025-12-31', '2026-06-05', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-in no puede ser una fecha pasada.');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 4 — CHECK-OUT INVÁLIDO RESPECTO AL CHECK-IN
// ═══════════════════════════════════════════════════════════════════
describe('validarFechas — check-out no posterior al check-in', () => {

  test('TC-15 | check-out mismo día que check-in → error (requiere al menos 1 noche)', () => {
    const r = validarFechas('2026-06-10', '2026-06-10', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-out debe ser al menos un día después del check-in.');
  });

  test('TC-16 | check-out un día antes del check-in → error', () => {
    const r = validarFechas('2026-06-10', '2026-06-09', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-out debe ser al menos un día después del check-in.');
  });

  test('TC-17 | check-out un año antes del check-in → error', () => {
    const r = validarFechas('2027-01-01', '2026-01-01', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-out debe ser al menos un día después del check-in.');
  });

});

// ═══════════════════════════════════════════════════════════════════
// GRUPO 5 — CASOS BORDE
// ═══════════════════════════════════════════════════════════════════
describe('validarFechas — casos borde', () => {

  test('TC-18 | check-in hoy + check-out mañana = caso mínimo aceptable → válido', () => {
    const r = validarFechas(TODAY, '2026-04-23', TODAY);

    expect(r.valido).toBe(true);
    expect(r.error).toBe('');
  });

  test('TC-19 | check-in ayer + check-out mañana → error de "pasado" (no de orden)', () => {
    // La validación de check-in pasado se ejecuta ANTES que la de orden.
    const r = validarFechas('2026-04-21', '2026-04-23', TODAY);

    expect(r.valido).toBe(false);
    expect(r.error).toBe('El check-in no puede ser una fecha pasada.');
  });

  test('TC-20 | la función devuelve siempre { valido, error } — shape del objeto', () => {
    const ok  = validarFechas('2026-06-01', '2026-06-05', TODAY);
    const err = validarFechas('', '2026-06-05', TODAY);

    expect(ok).toHaveProperty('valido');
    expect(ok).toHaveProperty('error');
    expect(err).toHaveProperty('valido');
    expect(err).toHaveProperty('error');
  });

});