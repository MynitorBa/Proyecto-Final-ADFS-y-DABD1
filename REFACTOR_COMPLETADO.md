# Refactor Completado: Editar Reservación (Patrón Miku Inn)

## Resumen de Cambios

Implementación de **transacciones atómicas** y **validaciones exhaustivas** inspiradas en el patrón de Miku Inn, sin cambiar los endpoints existentes.

---

## Phase 1: Transacciones Atómicas ✅

### Nuevos Métodos en Repository

#### 1. `ActualizarFechasHotelAtomico(reservacionID, fechaCheckIn, fechaCheckOut)`
```go
// Actualiza TODAS las habitaciones del hotel en UNA transacción
// Si falla CUALQUIER UPDATE, todo se revierte (ROLLBACK)
```

**Flujo:**
1. `BeginTx()` - inicia transacción
2. Obtiene TODOS los detalles hotel (Tipo_Detalle_ID=2) para la reservación
3. Para cada habitación:
   - Parsea JSON actual
   - Actualiza fechaCheckIn/Out
   - Escribe de vuelta a BD
4. `Commit()` - confirma todo o `Rollback()` si algo falla

**Garantía:** Si hay 3 habitaciones, se actualizan las 3 o ninguna. Nunca estado inconsistente.

#### 2. `ActualizarFechasVueloAtomico(reservacionID, fechaIda, fechaRetorno)`
```go
// Similar pero para vuelos (Tipo_Detalle_ID=1)
```

#### 3. `VerificarTraslapeHotel(proveedorID, fechaCheckIn, fechaCheckOut, excludeReservacionID)`
```go
// Verifica si las fechas nuevas se solapan con otras reservas
// Dos periodos se solapan si:
//   checkin_nuevo < checkout_existente AND checkout_nuevo > checkin_existente
//
// Busca contra: EstadoID IN (2, 4) = CONFIRMADA o COMPLETADA
// Excluye: la reservación actual (para no compararse a sí misma)
```

#### 4. `VerificarDuracionIgual(fcInActual, fcOutActual, fcInNueva, fcOutNueva)`
```go
// Calcula duraciones (horas / 24)
// Retorna true si son iguales (con tolerancia 0.1 días)
```

---

## Phase 2: Validaciones Mejoradas ✅

### En el Service: `EditarReservacion()`

#### Para Hoteles:

1. ✅ **Formato**: YYYY-MM-DD (existía)
2. ✅ **Rango**: >= hoy (existía)
3. ✅ **48-hour rule**: Check-in debe ser con >= 48 horas anticipación
   ```go
   horasRestantes := fechaCheckIn.Sub(hoy).Hours()
   if horasRestantes < 48 {
       return "check-in debe ser con al menos 48 horas de anticipación"
   }
   ```
4. ✅ **Duraciones iguales**: No cambiar número de noches
   ```go
   if !VerificarDuracionIgual(actual, nuevo) {
       return "la duración de la estadía no puede cambiar"
   }
   ```
5. ✅ **Traslapes**: Verificar contra otras reservas del hotel
   ```go
   hayTraslape, err := repo.VerificarTraslapeHotel(proveedorID, ...)
   ```
6. ✅ **Disponibilidad**: Llamar proveedor (existía)

#### Para Vuelos:

1. ✅ **Formato**: YYYY-MM-DD
2. ✅ **Rango**: >= hoy
3. ✅ **Orden lógico**: Ida < Retorno (nuevo)
   ```go
   if !fechaIda.Before(fechaRetorno) {
       return "ida debe ser anterior a retorno"
   }
   ```
4. ✅ **Disponibilidad**: Llamar proveedor

#### En BD:

- ✅ **NUNCA toca Total** - confirmado en línea 692-693
- ✅ **Usa transacción** - si una falla, todo se revierte

---

## Comparativa: Antes vs Después

### Antes (Vulnerable)

```
EditarReservacion()
├─ Actualiza hotel
│  └─ UPDATE Detalles_Reservacion (sin transacción)
│     └─ Si falla: BD parcialmente actualizada ❌
├─ Actualiza vuelo
│  └─ UPDATE Detalles_Reservacion (sin transacción)
│     └─ Si falla: BD parcialmente actualizada ❌
└─ Sin validar:
   ├─ Traslapes ❌
   ├─ 48-hour rule ❌
   ├─ Duraciones iguales ❌
   └─ Orden ida < retorno ❌
```

### Después (Seguro)

```
EditarReservacion()
├─ Validaciones exhaustivas ✅
│  ├─ Traslape checking
│  ├─ 48-hour rule
│  ├─ Duraciones iguales
│  └─ Orden lógico vuelos
├─ Transacción para hotel ✅
│  ├─ BeginTx
│  ├─ UPDATE todos los registros
│  └─ Commit o Rollback
├─ Transacción para vuelo ✅
│  ├─ BeginTx
│  ├─ UPDATE todos los registros
│  └─ Commit o Rollback
└─ BD: Consistente ✅
   ├─ Todos los cambios aplicados
   ├─ O ninguno aplicado
   └─ Total nunca modificado
```

---

## Archivos Modificados

### 1. `DetalleReservacionRepository.go`
**Línea 8:** Agregado import `"time"`

**Línea 648+:** 4 nuevas funciones
- `VerificarTraslapeHotel()`
- `VerificarDuracionIgual()`
- `ActualizarFechasHotelAtomico()`
- `ActualizarFechasVueloAtomico()`
- `parseFecha()` - helper

### 2. `DetalleReservacionService.go`
**Línea 641-689:** Sección de hoteles refactorizada
- Agregado: validación 48-hour
- Agregado: validación duraciones iguales
- Agregado: verificación traslapes
- Cambiado: `ActualizarFechasHotel()` → `ActualizarFechasHotelAtomico()`

**Línea 588-637:** Sección de vuelos refactorizada
- Agregado: validación ida < retorno
- Cambiado: `ActualizarFechasVuelo()` → `ActualizarFechasVueloAtomico()`

---

## Build Status

✅ **Compilación exitosa**
```bash
cd C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\API Agencia\agencia-viajes
go build -o server ./cmd/server
# Output: 33MB executable, sin errores
```

---

## Testing Próximo

### Flujo 1: Editar Fechas Hotel (Happy Path)
```
1. Usuario abre reservación hotel confirmada
2. Haz clic "Editar Reservación"
3. Cambia check-in a 3 días después, check-out a 3 días después (misma duración)
4. Presiona "Guardar"
→ Validaciones: ✓
→ Transacción: ✓
→ Total sin cambios: ✓
```

### Flujo 2: Rechaza Si Traslapa
```
1. Usuario intenta cambiar a fechas que solapan con otra reserva
2. Presiona "Guardar"
→ Error: "hotel no está disponible (hay conflicto)"
→ BD: Sin cambios
→ Transacción: Rollback automático
```

### Flujo 3: Rechaza Si < 48 Horas
```
1. Usuario intenta check-in en 30 horas
2. Presiona "Guardar"
→ Error: "check-in debe ser con al menos 48 horas"
→ BD: Sin cambios
```

### Flujo 4: Rechaza Si Duraciones Diferentes
```
1. Reservación: 27 abr (check-in) → 30 abr (check-out) = 3 noches
2. Usuario intenta: 28 abr → 30 abr = 2 noches
3. Presiona "Guardar"
→ Error: "la duración de la estadía no puede cambiar"
→ BD: Sin cambios
```

---

**Fecha Refactor:** 2026-04-27  
**Estado:** ✅ Implementado y compilado  
**Próximo:** Testing completo de flujos

