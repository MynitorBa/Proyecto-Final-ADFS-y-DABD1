# Testing Checklist: Editar Reservación (Miku Inn Pattern)

## Setup

```bash
# Terminal 1: Backend
cd "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\API Agencia\agencia-viajes"
go run ./cmd/server

# Terminal 2: Frontend
cd "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"
npm run dev

# Browser: http://localhost:5174
```

---

## Test Cases

### ✅ TC-1: Editar Fechas Hotel (Duración Igual, 48h+)
**Setup:** Reservación hotel confirmada, check-in=27 abr, check-out=30 abr (3 noches)

**Steps:**
1. Abre "Mis Reservaciones"
2. Abre reservación hotel
3. Desplazarse a "Editar Reservación"
4. Cambiar check-in → **29 abr**
5. Cambiar check-out → **2 may** (aún 3 noches)
6. Clic "Guardar"

**Expected:**
- ✓ Validación: 48 horas ✓
- ✓ Validación: Duraciones iguales ✓ 
- ✓ Transacción: Éxito
- ✓ Total: Sin cambios
- ✓ Página recarga automáticamente
- ✓ Error: NO debe mostrar

**Result:** _____ (PASS/FAIL)

---

### ❌ TC-2: Rechaza Si Duración Diferente
**Setup:** Misma reservación (27-30 abr, 3 noches)

**Steps:**
1. Intenta cambiar a 28 abr - 30 abr (solo 2 noches)
2. Clic "Guardar"

**Expected:**
- ✓ Error debajo del form: "la duración de la estadía no puede cambiar"
- ✓ BD: Sin cambios (transacción rollback)
- ✓ Página: NO recarga

**Result:** _____ (PASS/FAIL)

---

### ❌ TC-3: Rechaza Si < 48 Horas
**Setup:** Hoy es 27 abr. Hotel: 27 abr (check-in) - 30 abr (check-out)

**Steps:**
1. Intenta cambiar check-in a 28 abr (< 48 horas desde hoy)
2. Clic "Guardar"

**Expected:**
- ✓ Error: "check-in debe ser con al menos 48 horas de anticipación"
- ✓ BD: Sin cambios
- ✓ Página: NO recarga

**Result:** _____ (PASS/FAIL)

---

### ❌ TC-4: Rechaza Si Traslapa Con Otra Reserva
**Setup:** 
- Reservación A: hotel XYZ, 28-30 abr (confirmada)
- Reservación B: hotel XYZ, 1-3 may (confirmada)
- Editando: Reservación B

**Steps:**
1. Intenta cambiar Reservación B a 29 abr - 1 may (se solapa con A)
2. Clic "Guardar"

**Expected:**
- ✓ Error: "hotel no está disponible (hay conflicto con otra reserva)"
- ✓ BD: Sin cambios
- ✓ Página: NO recarga

**Result:** _____ (PASS/FAIL)

---

### ✅ TC-5: Editar Fechas Vuelo (ida < retorno)
**Setup:** Reservación vuelo confirmada, ida=27 abr, retorno=28 abr

**Steps:**
1. Abre "Mis Reservaciones"
2. Abre reservación vuelo
3. Desplazarse a "Editar Reservación"
4. Cambiar ida → **29 abr**
5. Cambiar retorno → **2 may** (ida < retorno ✓)
6. Clic "Guardar"

**Expected:**
- ✓ Validación: Orden lógico ✓
- ✓ Transacción: Éxito
- ✓ Total: Sin cambios
- ✓ Página recarga automáticamente
- ✓ Error: NO debe mostrar

**Result:** _____ (PASS/FAIL)

---

### ❌ TC-6: Rechaza Si Ida >= Retorno
**Setup:** Misma reservación vuelo

**Steps:**
1. Intenta cambiar retorno a fecha ANTES de ida (o igual)
2. Clic "Guardar"

**Expected:**
- ✓ Error: "ida debe ser anterior a retorno"
- ✓ BD: Sin cambios
- ✓ Página: NO recarga

**Result:** _____ (PASS/FAIL)

---

### ✅ TC-7: Múltiples Habitaciones (Transacción Atómica)
**Setup:** Reservación con 2 habitaciones confirmada

**Steps:**
1. Abre reservación
2. Edita fechas (cambio válido, duración igual)
3. Clic "Guardar"

**Expected:**
- ✓ AMBAS habitaciones actualizadas
- ✓ BD: Consistente (transacción exitosa)
- ✓ NO hay sitio donde 1 se actualizó y 1 no

**Verificación SQL:**
```sql
SELECT ID, Parametros_Json FROM Detalles_Reservacion 
WHERE Reservacion_ID = ? AND Tipo_Detalle_ID = 2;
-- Ambas deberían tener las MISMAS fechas nuevas
```

**Result:** _____ (PASS/FAIL)

---

## Regression Tests

### 🔄 RT-1: Editar Pasajeros Aún Funciona
**Steps:**
1. Abre reservación confirmada
2. Desplazarse a "Editar Reservación"
3. Cambiar nombre de pasajero
4. Clic "Guardar"

**Expected:**
- ✓ Nombre se actualiza
- ✓ Página recarga

**Result:** _____ (PASS/FAIL)

---

### 🔄 RT-2: Total No Cambia Nunca
**Setup:** Reservación con total=Q5,000.00

**Steps:**
1. Edita fechas (cualquier cambio válido)
2. Guarda
3. Abre reservación nuevamente

**Expected:**
- ✓ Total sigue siendo Q5,000.00
- ✓ NUNCA cambió a 0.00 ni a otro valor

**Result:** _____ (PASS/FAIL)

---

### 🔄 RT-3: Cancelación Aún Funciona
**Steps:**
1. Abre reservación
2. Presiona "Cancelar Reservación"
3. Confirma

**Expected:**
- ✓ Reservación pasa a estado "Cancelada"
- ✓ No hay errores

**Result:** _____ (PASS/FAIL)

---

## Summary

**Total Test Cases:** 9  
**Passed:** ___ / 9  
**Failed:** ___ / 9

**Overall Status:** _____ (✅ READY FOR PROD / ⚠️ NEEDS FIXES)

**Issues Found:**
```
[List any failures here]
```

**Notes:**
```
[Any additional observations]
```

---

**Date:** 2026-04-27  
**Tester:** ___________

