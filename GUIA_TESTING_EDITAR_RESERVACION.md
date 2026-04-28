# Guía de Testing: Editar Reservación

## Quick Start

### 1. Compilar Backend
```bash
cd "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\API Agencia\agencia-viajes"
go build -o bin/agencia-viajes.exe ./cmd/server
./bin/agencia-viajes.exe
```

### 2. Iniciar Frontend
```bash
cd "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"
npm run dev
```

---

## Test Manual via cURL

### Headers Requeridos
```
Content-Type: application/json
Authorization: Bearer <TOKEN_JWT>
```

### Test 1: Editar Solo Pasajeros

```bash
curl -X PUT http://localhost:8080/api/reservaciones/1/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "pasajeros": [
      {
        "id": 1,
        "nombre": "Juan Carlos",
        "apellido": "García López",
        "numPasaporte": "12345678",
        "nacionalidad": "MX",
        "fechaNac": "1990-05-15"
      }
    ]
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "exitoso": true,
  "mensaje": "Reservación actualizada exitosamente",
  "cambios": [
    "Actualizado(s) 1 pasajero(s)"
  ]
}
```

---

### Test 2: Editar Fechas de Vuelo

```bash
curl -X PUT http://localhost:8080/api/reservaciones/1/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fechaIda": "2026-06-15",
    "fechaRetorno": "2026-06-22"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "exitoso": true,
  "mensaje": "Reservación actualizada exitosamente",
  "cambios": [
    "Fechas de vuelo actualizadas"
  ]
}
```

---

### Test 3: Editar Fechas de Hotel

```bash
curl -X PUT http://localhost:8080/api/reservaciones/2/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fechaCheckIn": "2026-06-15",
    "fechaCheckOut": "2026-06-22"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "exitoso": true,
  "mensaje": "Reservación actualizada exitosamente",
  "cambios": [
    "Fechas de hotel actualizadas"
  ]
}
```

---

### Test 4: Editar Todo

```bash
curl -X PUT http://localhost:8080/api/reservaciones/3/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "pasajeros": [
      {
        "id": 1,
        "nombre": "Ana María",
        "apellido": "Rodríguez",
        "numPasaporte": "98765432",
        "nacionalidad": "GT",
        "fechaNac": "1985-03-20"
      }
    ],
    "fechaIda": "2026-07-01",
    "fechaRetorno": "2026-07-08",
    "fechaCheckIn": "2026-07-01",
    "fechaCheckOut": "2026-07-08"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "exitoso": true,
  "mensaje": "Reservación actualizada exitosamente",
  "cambios": [
    "Actualizado(s) 1 pasajero(s)",
    "Fechas de vuelo actualizadas",
    "Fechas de hotel actualizadas"
  ]
}
```

---

## Test Manual via Frontend (UI)

### Paso 1: Navegar a "Mis Reservaciones"
1. Inicia sesión en la aplicación
2. Ve a "Mis Reservaciones"
3. Busca una reservación de tipo "Vuelo" o "Hotel" (no Paquete)

### Paso 2: Abrir Modal de Edición
1. Haz clic en el botón "Editar Reservación"
2. Verifica que el modal se abra en fullscreen
3. Verifica que aparezca:
   - Header con categoría (✈️ para vuelo, 🏨 para hotel)
   - Fecha actual en formato rango (ej: "14 May 2026 → 20 May 2026")
   - Campos de fecha en el modal

### Paso 3: Editar Datos
- **Para Vuelos:** Cambia Fecha Ida y/o Fecha Retorno
- **Para Hoteles:** Cambia Check-in y/o Check-out
- Haz clic en "Guardar Cambios"

### Paso 4: Verificar Resultado
- ✅ Modal cierra
- ✅ Página se recarga
- ✅ Toast muestra "Reservación actualizada exitosamente"
- ✅ Nuevas fechas aparecen en la reservación

---

## Test de Validación (Deben fallar)

### ❌ Pasaporte con caracteres especiales

```bash
curl -X PUT http://localhost:8080/api/reservaciones/1/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "pasajeros": [
      {
        "id": 1,
        "nombre": "Juan",
        "apellido": "García",
        "numPasaporte": "ABC-12345",
        "nacionalidad": "MX",
        "fechaNac": "1990-05-15"
      }
    ]
  }'
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "error": "pasaporte de Juan García debe contener solo números"
}
```

---

### ❌ Fecha de nacimiento formato incorrecto

```bash
curl -X PUT http://localhost:8080/api/reservaciones/1/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "pasajeros": [
      {
        "id": 1,
        "nombre": "Juan",
        "apellido": "García",
        "numPasaporte": "12345678",
        "nacionalidad": "MX",
        "fechaNac": "15/05/1990"
      }
    ]
  }'
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "error": "fecha de nacimiento inválida para Juan García"
}
```

---

### ❌ Fecha anterior a hoy

```bash
curl -X PUT http://localhost:8080/api/reservaciones/1/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fechaIda": "2020-06-15"
  }'
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "error": "la fecha de ida no puede ser anterior a hoy"
}
```

---

### ❌ Usuario no autenticado

```bash
curl -X PUT http://localhost:8080/api/reservaciones/1/editar \
  -H "Content-Type: application/json" \
  -d '{ "fechaIda": "2026-06-15" }'
```

**Respuesta esperada (401 Unauthorized):**
```json
{
  "error": "usuario no autenticado"
}
```

---

### ❌ Usuario no propietario de reservación

Intenta editar una reservación que pertenece a otro usuario:

```bash
curl -X PUT http://localhost:8080/api/reservaciones/999/editar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_USUARIO_A>" \
  -d '{ "fechaIda": "2026-06-15" }'
```

**Respuesta esperada (403 Forbidden):**
```json
{
  "error": "no autorizado"
}
```

---

## Verificar Logs de Auditoría

### En Base de Datos
```sql
SELECT * FROM Sesion_Log 
WHERE Usuario_ID = <ID_USUARIO> 
  AND Tipo_Evento = 63  -- TipoOutEditarReservacionExitosa
ORDER BY Fecha_Hora DESC
LIMIT 10;
```

### En Console del Backend
Busca logs con patrón: `"Reservación editada con cambios:"`

```
INFO    Reservación editada con cambios: [Actualizado(s) 1 pasajero(s) Fechas de vuelo actualizadas]
```

---

## Checklist de Testing

- [ ] Test 1: Editar solo pasajeros - ✅ Funciona
- [ ] Test 2: Editar fechas vuelo - ✅ Funciona
- [ ] Test 3: Editar fechas hotel - ✅ Funciona
- [ ] Test 4: Editar todo junto - ✅ Funciona
- [ ] ❌ Pasaporte inválido - ✅ Rechazado
- [ ] ❌ Fecha formato inválido - ✅ Rechazado
- [ ] ❌ Fecha anterior a hoy - ✅ Rechazado
- [ ] ❌ No autenticado - ✅ Rechazado (401)
- [ ] ❌ No propietario - ✅ Rechazado (403)
- [ ] Modal fullscreen aparece - ✅ Funciona
- [ ] Modal muestra fechas actual - ✅ Funciona
- [ ] Logs de auditoría se registran - ✅ Funciona

---

## Troubleshooting

### Error: "reservación no encontrada"
- Verifica que el ID de reservación existe en la BD
- Verifica que pertenece al usuario autenticado

### Error: "datos inválidos"
- Verifica que el JSON está bien formado
- Verifica que Content-Type: application/json está presente

### Error: "usuario no autenticado"
- Verifica que el token JWT es válido
- Verifica que Authorization header está presente

### El modal no aparece
- Abre la consola del navegador (F12)
- Busca errores JavaScript en la tab Console
- Verifica que los SVG icons están cargando correctamente

### Las fechas no se actualizan
- Recarga la página manualmente (Ctrl+F5)
- Verifica en BD que la columna Detalle_JSON fue actualizada
- Revisa logs de backend para errores de BD

---

**Última actualización:** 2026-04-27  
**Estado:** ✅ Guía lista para testing
