/**
 * Valida que las fechas de check-in y check-out sean correctas.
 * @param {string} checkIn   - Fecha check-in  (YYYY-MM-DD)
 * @param {string} checkOut  - Fecha check-out (YYYY-MM-DD)
 * @param {string} today     - Fecha de hoy    (YYYY-MM-DD)
 * @returns {{ valido: boolean, error: string }}
 */
export function validarFechas(checkIn, checkOut, today) {
    if (!checkIn)  return { valido: false, error: 'Selecciona la fecha de check-in.' };
    if (!checkOut) return { valido: false, error: 'Selecciona la fecha de check-out.' };
    if (checkIn < today)                              return { valido: false, error: 'El check-in no puede ser una fecha pasada.' };
    if (new Date(checkOut) <= new Date(checkIn))      return { valido: false, error: 'El check-out debe ser al menos un día después del check-in.' };

    return { valido: true, error: '' };
}