# Análisis y Refactor: Patrón Miku Inn vs. Go Actual

## Estado Actual (Go)

### Ruta
- **PUT** `/reservaciones/:id/editar` (general edit, multi-propósito)
- Mezcla: pasajeros + fechas + validaciones

### Flujo Actual
```
Controller.EditarReservacion()
  ↓
Service.EditarReservacion()
  ├─ Valida ownership
  ├─ Actualiza pasajeros
  ├─ Actualiza fechas vuelo (todo registro con Tipo_Detalle=1)
  ├─ Actualiza fechas hotel (todo registro con Tipo_Detalle=2)
  └─ LOG (sin recalc total ✓)
  ↓
Repository.ActualizarFechasVuelo/Hotel()
  └─ UPDATE un registro WHERE Tipo_Detalle
```

### Problemas Identificados
1. ❌ **Sin transacción** - Si actualiza vuelo exitoso pero hotel falla, inconsistencia
2. ❌ **Validaciones incompletas**:
   - No verifica traslapes (overlaps) con otras reservas del hotel
   - No verifica 48-hour advance rule
   - No verifica que duraciones sean iguales
3. ❌ **Mezcla de responsabilidades** - Pasajeros + Fechas en un endpoint
4. ⚠️ **Disponibilidad optimista** - Verifica con proveedor pero sin traslape checking

## Patrón Miku Inn (Java)

### Ruta
- **PATCH** `/agencia/reservaciones/{id}/fechas` (dates-only, cleanly separated)
- Único propósito: cambiar fechas

### Validaciones Antes de Actualizar
```java
✓ Hotel existe
✓ Usuario pertenece a agencia
✓ Estado es "pendiente" O "confirmada"
✓ 48-hour rule: no check-in en < 48 horas
✓ Traslapes: verifica NO hay conflictos con otras reservas
✓ Duraciones iguales: misma cantidad de noches/días
```

### Flujo Atómico (Transacción)
```
Transaction.begin()
  ├─ Validaciones exhaustivas
  ├─ UPDATE habitaciones (todas en mismo hotel)
  ├─ UPDATE detalle_json
  ├─ SIN tocar total ✓
  └─ LOG
Transaction.commit()
```

## Plan de Refactor a Go

### Phase 1: Agregar Transacciones (Atomic Updates)
- Envolver ActualizarFechasVuelo/Hotel en tx
- Rollback automático si cualquier operación falla

### Phase 2: Validaciones Faltantes
- ✓ Traslape checking (comparar con otras reservas)
- ✓ 48-hour advance rule
- ✓ Validar duraciones iguales (si se cambian ambas fechas)

### Phase 3: Separación de Endpoints (REFACTOR)
- Mover lógica de fechas a PATCH `/reservaciones/:id/fechas`
- Mantener PUT `/reservaciones/:id/editar` para pasajeros solamente
- O: mantener PUT pero limpiar lógica interna

### Phase 4: Response DTO
- Estructura clara: "Reservación actualizada exitosamente"
- Cambios realizados (pasajeros, fechas de vuelo, fechas de hotel)

---

**Decision:** Implementar Phase 1 + 2 inmediatamente (crítico).
Fase 3 es refactor a futuro (puede ser big change).

