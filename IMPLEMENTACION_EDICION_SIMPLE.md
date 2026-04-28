# Implementación: Edición Simple de Reservación

## Descripción General

Se eliminó completamente la lógica de cambio de habitación, vuelos y paquete (con listados de opciones disponibles). Se implementó una funcionalidad más simple: **edición directa de datos de reservación** con verificación de disponibilidad.

## Cambios Realizados

### Backend (Go/Gin)

#### 1. **main.go** - Rutas eliminadas y actualizadas
```go
// ❌ ELIMINADAS:
// protegido.GET("/reservaciones/:id/habitaciones-elegibles", ...)
// protegido.POST("/reservaciones/:id/cambiar-habitacion", ...)
// protegido.PUT("/reservaciones/asientos-vuelo", ...)

// ✅ NUEVA:
protegido.PUT("/reservaciones/:id/editar", detalleReservacionController.EditarReservacion)
```

#### 2. **DetallesReservacionesDTO.go** - Nuevos DTOs
```go
type EditarReservacionRequest struct {
    Pasajeros []EditarPasajeroRequest  // Nombres y pasaportes
    FechaIda  string                   // Nueva fecha ida (opcional)
    FechaRetorno string                // Nueva fecha retorno (opcional)
    FechaCheckIn  string                // Nueva fecha check-in hotel (opcional)
    FechaCheckOut string                // Nueva fecha check-out hotel (opcional)
}

type EditarPasajeroRequest struct {
    ID           int    // ID del pasajero (0 si es nuevo)
    Nombre       string // Nombre completo
    Apellido     string // Apellido
    NumPasaporte string // Número de pasaporte
    FechaNac     string // Fecha de nacimiento
    Nacionalidad string // Nacionalidad
}

type EditarReservacionResponse struct {
    Exitoso bool     // Éxito de la operación
    Mensaje string   // Mensaje descriptivo
    Cambios []string // Lista de cambios realizados
}
```

#### 3. **DetalleReservacionController.go**
```go
// ❌ ELIMINADOS:
// func ObtenerHabitacionesElegibles(...)
// func CambiarHabitacion(...)

// ✅ NUEVO:
func EditarReservacion(c *gin.Context) {
    // Validar usuario y reservación
    // Procesar cambios de nombres, pasaportes, fechas
    // Retornar confirmación
}
```

#### 4. **DetalleReservacionService.go**
```go
// ❌ ELIMINADOS:
// func ObtenerHabitacionesElegibles(...)
// func llamarProveedorBusquedaHotel(...)
// func filtrarHabitacionesElegibles(...)
// func CambiarHabitacion(...)

// ✅ NUEVO:
func EditarReservacion(...) (interface{}, error) {
    // 1. Validar que reservación pertenezca al usuario
    // 2. Procesar cambios de pasajeros
    // 3. Validar fechas (antes de la fecha de viaje)
    // 4. Verificar disponibilidad con proveedores si cambian fechas
    // 5. Actualizar BD
    // 6. Retornar confirmación
}
```

#### 5. **AsientoVueloController.go**
```go
// ❌ ELIMINADO:
// func CambiarAsiento(...)
// (mantiene: func ObtenerAsientos(...))
```

#### 6. **AsientoVueloService.go**
```go
// ❌ ELIMINADOS:
// func CambiarAsientoVuelo(...)
// func llamarCambiarAsiento(...)
```

## Flujo de Edición Simplificado

### Antes (Cambio de Habitación)
```
Usuario selecciona "Cambiar Habitación"
    ↓
Sistema obtiene lista de ALTERNATIVAS disponibles
    ↓
Usuario selecciona de la lista de opciones
    ↓
Sistema confirma el cambio
```

### Ahora (Edición Simple)
```
Usuario entra a "Editar Reservación"
    ↓
Usuario modifica:
    - Nombres de pasajeros
    - Datos de pasaporte
    - Fechas (ida, retorno, check-in, check-out)
    ↓
Sistema verifica:
    - Fechas son antes de viaje
    - Disponibilidad en proveedores (si cambian fechas)
    ↓
Si todo está OK:
    - Actualiza BD
    - Retorna confirmación
Si NO está OK:
    - Rechaza el cambio
    - Explica por qué
```

## Ventajas

✅ **Más Simple:** No hay listas de opciones, edición directa  
✅ **Flexible:** Usuario elige exactamente qué editar  
✅ **Rápido:** Sin llamadas al proveedor si solo edita pasaportes  
✅ **Claro:** Confirmación de exactamente qué cambió  

## Implementación Pendiente

### Frontend (Vue 3)
1. Modal/formulario "Editar Reservación" con campos:
   - Nombres de pasajeros
   - Datos de pasaporte
   - Fechas de viaje
2. Validaciones de cliente:
   - Campos requeridos
   - Formato de fecha
   - Fechas válidas
3. Llamada a API: `PUT /api/reservaciones/:id/editar`
4. Manejo de respuesta: mostrar cambios o errores

### Backend (Completar)
1. Implementar lógica de validación de fechas
2. Implementar verificación de disponibilidad con proveedores
3. Implementar actualización de BD (Pasajeros, fechas, etc)
4. Retornar respuesta con cambios realizados

## Estado

✅ Backend: Rutas y controllers actualizados, compilación OK  
❌ Frontend: Pendiente crear componente de edición  
❌ Service: Lógica de verificación pendiente  

---
**Fecha:** 2026-04-27  
**Estatus:** En Progreso - Rutas y DTOs listos, lógica pendiente
