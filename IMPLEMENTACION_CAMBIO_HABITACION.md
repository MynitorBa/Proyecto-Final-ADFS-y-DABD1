# Implementación: Cambio de Habitación con Criterios de Búsqueda Almacenados

## Descripción General

Se implementó la solución "Correcta" (Opción 2) para permitir que los usuarios cambien de habitación después de hacer una reserva de hotel. El sistema almacena los criterios de búsqueda original en la base de datos y los reutiliza para obtener alternativas de habitaciones del proveedor.

## Cambios en el Backend (Go/Gin)

### 1. **DTOs** (`internal/dto/DetallesReservacionesDTO.go`)

```go
// Agregado campo para criterios de búsqueda
type AgregarDetalleHotelRequest struct {
    ReservacionID     int                       `json:"reservacionId"`
    ProveedorID       int                       `json:"proveedorId"`
    Habitaciones      []SeleccionHabitacion     `json:"habitaciones"`
    CriteriosBusqueda *BusquedaHotelesRequest   `json:"criteriosBusqueda,omitempty"` // ← NUEVO
}
```

### 2. **Repository** (`internal/repositories/DetalleReservacionRepository.go`)

**Nuevo Método:**
```go
ObtenerProveedorYCriterios(detalleID, usuarioID)
  ↓
  Retrieves:
  - proveedorID, urlAPI, tokenEntrada
  - criterios (from Parametros_Json)
```

### 3. **Service** (`internal/services/DetalleReservacionService.go`)

**Modificado: `AgregarDetalleHotel()`**
```go
// Antes de insertar el detalle, se crea una estructura que contiene:
parametrosCompletos := map[string]interface{}{
    "respuestaHotel":     respHotel,        // Respuesta del proveedor
    "criteriosBusqueda":  req.CriteriosBusqueda, // Criterios original
}
// Se inserta en Parametros_Json del detalle
```

**Completamente Reescrito: `ObtenerHabitacionesElegibles()`**
```go
// 1. Valida que el detalle pertenezca al usuario
// 2. Obtiene URL, token y criterios almacenados del proveedor
// 3. Llama a la API del proveedor: POST {urlAPI}/agencia/busqueda
// 4. Filtra resultados para retornar solo habitaciones elegibles
//    (mismo tipo, mismo precio que la original)
// 5. Limita máximo 5 resultados
```

**Nuevos Métodos:**
- `llamarProveedorBusquedaHotel()`: Realiza la llamada HTTP al proveedor
- `filtrarHabitacionesElegibles()`: Filtra habitaciones por tipo y precio

## Cambios en el Frontend (Vue 3)

### 1. **ResultadosHoteles.vue**

```javascript
// Modificado buildHotelPayload()
function buildHotelPayload(reservaId, itemData) {
  // ... código existente ...
  
  // NUEVO: Construir criterios de búsqueda
  const criteriosBusqueda = {
    ciudad: b.ciudad,
    pais: b.pais,
    fechaCheckIn: b.checkIn,
    fechaCheckOut: b.checkOut,
    cantidadPersonas: b.cantidadPersonas,
  }

  return {
    reservacionId: reservaId,
    proveedorId: itemData.proveedorId,
    habitaciones,
    criteriosBusqueda,  // ← NUEVO
  }
}
```

### 2. **Reserva.vue**

```javascript
// Mismo cambio: buildHotelPayload() incluye criteriosBusqueda
```

## Flujo de Datos

### Creación de Reserva (Primer Booking)

```
1. Usuario busca hoteles (BusquedaHotelesRequest)
   ├─ ciudad, pais, fechaCheckIn, fechaCheckOut, cantidadPersonas
   
2. Usuario selecciona habitación → precrearReservacionHotel()
   ├─ buildHotelPayload() recibe itemData.busqueda
   ├─ Construye criteriosBusqueda con datos de búsqueda original
   
3. POST /api/reservaciones/detalle/hotel
   ├─ AgregarDetalleHotel() recibe AgregarDetalleHotelRequest
   ├─ Llama al proveedor para crear reserva
   ├─ Almacena en Parametros_Json:
   │  {
   │    "respuestaHotel": { ... },
   │    "criteriosBusqueda": {
   │      "ciudad": "Miami",
   │      "pais": "USA",
   │      "fechaCheckIn": "2026-05-15",
   │      "fechaCheckOut": "2026-05-20",
   │      "cantidadPersonas": 2
   │    }
   │  }
```

### Cambio de Habitación (Semanas Después)

```
1. Usuario en "Mis Reservaciones" hace click en "Cambiar Habitación"
   ├─ openCambiarHab() → Modal se abre
   
2. Sistema llama: GET /api/reservaciones/{detalleID}/habitaciones-elegibles
   
3. ObtenerHabitacionesElegibles()
   ├─ ObtenerProveedorYCriterios() extrae del Parametros_Json:
   │  ├─ urlAPI, tokenEntrada del proveedor
   │  └─ criteriosBusqueda original
   │
   ├─ llamarProveedorBusquedaHotel()
   │  └─ POST {urlAPI}/agencia/busqueda con criteriosBusqueda
   │     ↓ Respuesta: lista de hoteles con habitaciones disponibles
   │
   ├─ filtrarHabitacionesElegibles()
   │  └─ Filtra por:
   │     - Mismo tipo de habitación
   │     - Mismo precio por noche
   │     - Excluye la habitación actual (ID)
   │     - Máximo 5 resultados
   │
   └─ Retorna: DetalleHabitacionElegibleDTO[]

4. Usuario selecciona nueva habitación en modal
   ├─ POST /api/reservaciones/{detalleID}/cambiar-habitacion
   ├─ Backend actualiza Habitacion_ID en detalle
   └─ Email de confirmación enviado
```

## Beneficios

✅ **Información Completa:** Los criterios de búsqueda se almacenan permanentemente  
✅ **Sin Reintroducción:** Usuario no debe volver a ingresar ciudad, fechas, etc.  
✅ **Datos Frescos:** Habitaciones se consultan al momento (disponibilidad actual)  
✅ **Flexibilidad Temporal:** Funciona aunque pasen semanas desde la reserva original  
✅ **Validación Automática:** Solo se muestran habitaciones elegibles (tipo/precio)  

## Datos Almacenados en BD

### Tabla: `Detalles_Reservacion`
```
Columna: Parametros_Json (VARCHAR, JSON)

Contenido:
{
  "respuestaHotel": {
    "id": 5847,
    "nombre": "Miami Beach Resort",
    "habitaciones": [
      {
        "id": 493,
        "numero": "1245",
        "tipo": "Suite Deluxe",
        "precioPorNoche": 71.92,
        ...
      }
    ]
  },
  "criteriosBusqueda": {
    "ciudad": "Miami",
    "pais": "United States",
    "fechaCheckIn": "2026-05-15",
    "fechaCheckOut": "2026-05-20",
    "cantidadPersonas": 2
  }
}
```

## Estado de Compilación

✅ Backend compila sin errores  
✅ Todos los imports incluidos  
✅ Métodos correctamente tipificados  

## Próximos Pasos (Opcional)

1. **Tests:** Agregar pruebas unitarias para filtrado de habitaciones
2. **Logs:** Registrar en auditoría cada cambio de habitación
3. **Errores:** Implementar retry logic si la llamada al proveedor falla
4. **Caché:** Cachear respuestas del proveedor por 5-10 minutos
5. **Optimización:** Filtrado de habitaciones en memoria vs. en proveedor

---
**Fecha:** 2026-04-27  
**Estatus:** Implementado ✅  
**Próximo:** Pruebas end-to-end con datos reales del proveedor
