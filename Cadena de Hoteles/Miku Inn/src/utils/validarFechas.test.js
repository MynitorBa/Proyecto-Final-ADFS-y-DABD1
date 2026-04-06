import { validarFechas } from './validarFechas.js';

const TODAY = '2026-04-05'; // fecha fija para que los tests no dependan del día real

// Test 1 — fechas válidas
test('retorna valido:true cuando check-out es después del check-in', () => {
    const result = validarFechas('2026-06-01', '2026-06-05', TODAY);

    expect(result.valido).toBe(true);
    expect(result.error).toBe('');
});

// Test 2 — check-out antes que check-in
test('retorna error cuando check-out es anterior al check-in', () => {
    const result = validarFechas('2026-06-10', '2026-06-05', TODAY);

    expect(result.valido).toBe(false);
    expect(result.error).toBe('El check-out debe ser al menos un día después del check-in.');
});