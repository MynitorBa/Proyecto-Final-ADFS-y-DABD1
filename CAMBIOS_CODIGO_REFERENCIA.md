# Cambios de Código - Referencia Rápida

## Archivos Modificados

### 1. `internal/repositories/DetalleReservacionRepository.go`

#### Cambio 1: Agregar Import Time
```go
import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"time"  // ← NUEVO
)
```

#### Cambio 2: Agregar 4 Funciones (al final del archivo)

**A. VerificarTraslapeHotel**
```go
func (r *DetalleReservacionRepository) VerificarTraslapeHotel(proveedorID int, fechaCheckIn, fechaCheckOut string, excludeReservacionID int) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var count int
	err = conn.QueryRowContext(context.Background(), `
		SELECT COUNT(*) FROM Detalles_Reservacion dr
		INNER JOIN Reservacion res ON dr.Reservacion_ID = res.ID
		WHERE dr.Proveedor_ID = ?
		  AND res.ID != ?
		  AND res.EstadoID IN (2, 4)
		  AND dr.Tipo_Detalle_ID = 2
		  AND JSON_EXTRACT(dr.Parametros_Json, '$.respuestaHotel.fechaCheckIn') < ?
		  AND JSON_EXTRACT(dr.Parametros_Json, '$.respuestaHotel.fechaCheckOut') > ?
	`, proveedorID, excludeReservacionID, fechaCheckOut, fechaCheckIn).Scan(&count)

	if err != nil {
		return false, err
	}

	return count > 0, nil
}
```

**B. VerificarDuracionIgual**
```go
func VerificarDuracionIgual(fechaCheckInActual, fechaCheckOutActual, fechaCheckInNueva, fechaCheckOutNueva string) bool {
	checkinActual := parseFecha(fechaCheckInActual)
	checkoutActual := parseFecha(fechaCheckOutActual)
	duracionActual := checkoutActual.Sub(checkinActual).Hours() / 24

	checkinNueva := parseFecha(fechaCheckInNueva)
	checkoutNueva := parseFecha(fechaCheckOutNueva)
	duracionNueva := checkoutNueva.Sub(checkinNueva).Hours() / 24

	return duracionActual > duracionNueva-0.1 && duracionActual < duracionNueva+0.1
}
```

**C. ActualizarFechasHotelAtomico**
```go
func (r *DetalleReservacionRepository) ActualizarFechasHotelAtomico(reservacionID int, fechaCheckIn, fechaCheckOut string) error {
	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	var detalles []struct {
		ID          int
		DetalleJSON string
	}

	rows, err := tx.QueryContext(context.Background(), `
		SELECT ID, Parametros_Json FROM Detalles_Reservacion
		WHERE Reservacion_ID = ? AND Tipo_Detalle_ID = 2
	`, reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}
	defer rows.Close()

	for rows.Next() {
		var detalle struct {
			ID          int
			DetalleJSON string
		}
		if err := rows.Scan(&detalle.ID, &detalle.DetalleJSON); err != nil {
			tx.Rollback()
			return err
		}
		detalles = append(detalles, detalle)
	}

	for _, detalle := range detalles {
		var jsonData map[string]interface{}
		if err := json.Unmarshal([]byte(detalle.DetalleJSON), &jsonData); err != nil {
			tx.Rollback()
			return err
		}

		if respuesta, ok := jsonData["respuestaHotel"].(map[string]interface{}); ok {
			respuesta["fechaCheckIn"] = fechaCheckIn
			respuesta["fechaCheckOut"] = fechaCheckOut
		}

		updatedJSON, err := json.Marshal(jsonData)
		if err != nil {
			tx.Rollback()
			return err
		}

		_, err = tx.ExecContext(context.Background(), `
			UPDATE Detalles_Reservacion
			SET Parametros_Json = ?
			WHERE ID = ?
		`, string(updatedJSON), detalle.ID)
		if err != nil {
			tx.Rollback()
			return err
		}
	}

	return tx.Commit()
}
```

**D. ActualizarFechasVueloAtomico**
```go
func (r *DetalleReservacionRepository) ActualizarFechasVueloAtomico(reservacionID int, fechaIda, fechaRetorno string) error {
	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	var detalles []struct {
		ID          int
		DetalleJSON string
	}

	rows, err := tx.QueryContext(context.Background(), `
		SELECT ID, Parametros_Json FROM Detalles_Reservacion
		WHERE Reservacion_ID = ? AND Tipo_Detalle_ID = 1
	`, reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}
	defer rows.Close()

	for rows.Next() {
		var detalle struct {
			ID          int
			DetalleJSON string
		}
		if err := rows.Scan(&detalle.ID, &detalle.DetalleJSON); err != nil {
			tx.Rollback()
			return err
		}
		detalles = append(detalles, detalle)
	}

	for _, detalle := range detalles {
		var jsonData map[string]interface{}
		if err := json.Unmarshal([]byte(detalle.DetalleJSON), &jsonData); err != nil {
			tx.Rollback()
			return err
		}

		if fechaIda != "" {
			jsonData["fechaIda"] = fechaIda
		}
		if fechaRetorno != "" {
			jsonData["fechaRetorno"] = fechaRetorno
		}

		updatedJSON, err := json.Marshal(jsonData)
		if err != nil {
			tx.Rollback()
			return err
		}

		_, err = tx.ExecContext(context.Background(), `
			UPDATE Detalles_Reservacion
			SET Parametros_Json = ?
			WHERE ID = ?
		`, string(updatedJSON), detalle.ID)
		if err != nil {
			tx.Rollback()
			return err
		}
	}

	return tx.Commit()
}
```

**E. parseFecha (Helper)**
```go
func parseFecha(fechaStr string) time.Time {
	fecha, _ := time.Parse("2006-01-02", fechaStr)
	return fecha
}
```

---

### 2. `internal/services/DetalleReservacionService.go`

#### Cambio 1: Sección de Vuelos (Línea ~588)

**Antes:**
```go
	// Verificar y actualizar fechas de vuelo si se proporcionan
	if req.FechaIda != "" || req.FechaRetorno != "" {
		// Validar formato de fechas
		// ... (validaciones básicas)
		
		// Obtener detalle de vuelo para verificar disponibilidad
		detalleVuelo, err := s.repo.ObtenerDetalleVueloParaEditar(resID)
		if err == nil && detalleVuelo != nil {
			uid := usuarioID
			// Llamar al proveedor para verificar disponibilidad en las nuevas fechas
			disponible, err := s.verificarDisponibilidadVuelo(c, &uid, detalleVuelo, req.FechaIda, req.FechaRetorno)
			if err != nil {
				return nil, fmt.Errorf("error verificando disponibilidad de vuelo: %w", err)
			}
			if !disponible {
				return nil, errors.New("los vuelos no están disponibles en las fechas solicitadas")
			}

			// Actualizar fechas en BD
			err = s.repo.ActualizarFechasVuelo(resID, req.FechaIda, req.FechaRetorno)
			if err != nil {
				return nil, fmt.Errorf("error actualizando fechas de vuelo: %w", err)
			}
		}

		cambios = append(cambios, "Fechas de vuelo actualizadas")
	}
```

**Después:**
```go
	// Verificar y actualizar fechas de vuelo si se proporcionan
	if req.FechaIda != "" || req.FechaRetorno != "" {
		// Validar formato de fechas
		if req.FechaIda != "" {
			if _, err := time.Parse("2006-01-02", req.FechaIda); err != nil {
				return nil, errors.New("formato de fecha de ida inválido (usar YYYY-MM-DD)")
			}
		}
		if req.FechaRetorno != "" {
			if _, err := time.Parse("2006-01-02", req.FechaRetorno); err != nil {
				return nil, errors.New("formato de fecha de retorno inválido (usar YYYY-MM-DD)")
			}
		}

		// Validar que las fechas sean posteriores o iguales a hoy
		hoy := time.Now().Truncate(24 * time.Hour)
		if req.FechaIda != "" {
			fechaIda, _ := time.Parse("2006-01-02", req.FechaIda)
			if fechaIda.Before(hoy) {
				return nil, errors.New("la fecha de ida no puede ser anterior a hoy")
			}
		}
		if req.FechaRetorno != "" {
			fechaRetorno, _ := time.Parse("2006-01-02", req.FechaRetorno)
			if fechaRetorno.Before(hoy) {
				return nil, errors.New("la fecha de retorno no puede ser anterior a hoy")
			}
		}

		// ← NUEVO: Validar que ida < retorno
		if req.FechaIda != "" && req.FechaRetorno != "" {
			fechaIda, _ := time.Parse("2006-01-02", req.FechaIda)
			fechaRetorno, _ := time.Parse("2006-01-02", req.FechaRetorno)
			if !fechaIda.Before(fechaRetorno) {
				return nil, errors.New("la fecha de ida debe ser anterior a la fecha de retorno")
			}
		}

		// Obtener detalle de vuelo para verificar disponibilidad
		detalleVuelo, err := s.repo.ObtenerDetalleVueloParaEditar(resID)
		if err == nil && detalleVuelo != nil {
			uid := usuarioID
			// Llamar al proveedor para verificar disponibilidad en las nuevas fechas
			disponible, err := s.verificarDisponibilidadVuelo(c, &uid, detalleVuelo, req.FechaIda, req.FechaRetorno)
			if err != nil {
				return nil, fmt.Errorf("error verificando disponibilidad de vuelo: %w", err)
			}
			if !disponible {
				return nil, errors.New("los vuelos no están disponibles en las fechas solicitadas")
			}

			// ← MEJORADO: Usar transacción atómica
			err = s.repo.ActualizarFechasVueloAtomico(resID, req.FechaIda, req.FechaRetorno)
			if err != nil {
				return nil, fmt.Errorf("error actualizando fechas de vuelo: %w", err)
			}
		}

		cambios = append(cambios, "Fechas de vuelo actualizadas")
	}
```

#### Cambio 2: Sección de Hoteles (Línea ~641)

**Antes:**
```go
	// Verificar y actualizar fechas de hotel si se proporcionan
	if req.FechaCheckIn != "" || req.FechaCheckOut != "" {
		// Validar formato de fechas
		// ... (validaciones básicas)

		// Obtener detalle de hotel para verificar disponibilidad
		detalleHotel, err := s.repo.ObtenerDetalleHotelParaEditar(resID)
		if err == nil && detalleHotel != nil {
			uid := usuarioID
			// Llamar al proveedor para verificar disponibilidad en las nuevas fechas
			disponible, err := s.verificarDisponibilidadHotel(c, &uid, detalleHotel, req.FechaCheckIn, req.FechaCheckOut)
			if err != nil {
				return nil, fmt.Errorf("error verificando disponibilidad de hotel: %w", err)
			}
			if !disponible {
				return nil, errors.New("el hotel no está disponible en las fechas solicitadas")
			}

			// Actualizar fechas en BD
			err = s.repo.ActualizarFechasHotel(resID, req.FechaCheckIn, req.FechaCheckOut)
			if err != nil {
				return nil, fmt.Errorf("error actualizando fechas de hotel: %w", err)
			}
		}

		cambios = append(cambios, "Fechas de hotel actualizadas")
	}
```

**Después:**
```go
	// Verificar y actualizar fechas de hotel si se proporcionan
	if req.FechaCheckIn != "" || req.FechaCheckOut != "" {
		// Validar formato de fechas
		if req.FechaCheckIn != "" {
			if _, err := time.Parse("2006-01-02", req.FechaCheckIn); err != nil {
				return nil, errors.New("formato de check-in inválido (usar YYYY-MM-DD)")
			}
		}
		if req.FechaCheckOut != "" {
			if _, err := time.Parse("2006-01-02", req.FechaCheckOut); err != nil {
				return nil, errors.New("formato de check-out inválido (usar YYYY-MM-DD)")
			}
		}

		// Validar que las fechas sean posteriores o iguales a hoy
		hoy := time.Now().Truncate(24 * time.Hour)
		if req.FechaCheckIn != "" {
			fechaCheckIn, _ := time.Parse("2006-01-02", req.FechaCheckIn)
			if fechaCheckIn.Before(hoy) {
				return nil, errors.New("la fecha de check-in no puede ser anterior a hoy")
			}
		}
		if req.FechaCheckOut != "" {
			fechaCheckOut, _ := time.Parse("2006-01-02", req.FechaCheckOut)
			if fechaCheckOut.Before(hoy) {
				return nil, errors.New("la fecha de check-out no puede ser anterior a hoy")
			}
		}

		// ← NUEVO: Validar regla de 48 horas
		if req.FechaCheckIn != "" {
			fechaCheckIn, _ := time.Parse("2006-01-02", req.FechaCheckIn)
			horasRestantes := fechaCheckIn.Sub(hoy).Hours()
			if horasRestantes < 48 {
				return nil, errors.New("el check-in debe ser con al menos 48 horas de anticipación")
			}
		}

		// Obtener detalle de hotel para verificar disponibilidad y duraciones
		detalleHotel, err := s.repo.ObtenerDetalleHotelParaEditar(resID)
		if err == nil && detalleHotel != nil {
			// ← NUEVO: Extraer fechas actuales para verificar duraciones
			var fechaCheckInActual, fechaCheckOutActual string
			if respuesta, ok := detalleHotel["respuestaHotel"].(map[string]interface{}); ok {
				if fcin, ok := respuesta["fechaCheckIn"].(string); ok {
					fechaCheckInActual = fcin
				}
				if fcout, ok := respuesta["fechaCheckOut"].(string); ok {
					fechaCheckOutActual = fcout
				}
			}

			// ← NUEVO: Validar que la duración sea igual
			if req.FechaCheckIn != "" && req.FechaCheckOut != "" && fechaCheckInActual != "" && fechaCheckOutActual != "" {
				if !repositories.VerificarDuracionIgual(fechaCheckInActual, fechaCheckOutActual, req.FechaCheckIn, req.FechaCheckOut) {
					return nil, errors.New("la duración de la estadía no puede cambiar (mismo número de noches)")
				}
			}

			// ← NUEVO: Verificar traslapes con otras reservaciones
			proveedorID, ok := detalleHotel["proveedor_id"].(float64)
			if ok {
				fechaCheckInValidar := req.FechaCheckIn
				if fechaCheckInValidar == "" {
					fechaCheckInValidar = fechaCheckInActual
				}
				fechaCheckOutValidar := req.FechaCheckOut
				if fechaCheckOutValidar == "" {
					fechaCheckOutValidar = fechaCheckOutActual
				}

				hayTraslape, err := s.repo.VerificarTraslapeHotel(int(proveedorID), fechaCheckInValidar, fechaCheckOutValidar, resID)
				if err != nil {
					return nil, fmt.Errorf("error verificando disponibilidad de hotel: %w", err)
				}
				if hayTraslape {
					return nil, errors.New("el hotel no está disponible en las fechas solicitadas (hay conflicto con otra reserva)")
				}
			}

			uid := usuarioID
			// Llamar al proveedor para verificar disponibilidad en las nuevas fechas
			disponible, err := s.verificarDisponibilidadHotel(c, &uid, detalleHotel, req.FechaCheckIn, req.FechaCheckOut)
			if err != nil {
				return nil, fmt.Errorf("error verificando disponibilidad de hotel: %w", err)
			}
			if !disponible {
				return nil, errors.New("el hotel no está disponible en las fechas solicitadas")
			}

			// ← MEJORADO: Usar transacción atómica
			err = s.repo.ActualizarFechasHotelAtomico(resID, req.FechaCheckIn, req.FechaCheckOut)
			if err != nil {
				return nil, fmt.Errorf("error actualizando fechas de hotel: %w", err)
			}
		}

		cambios = append(cambios, "Fechas de hotel actualizadas")
	}
```

---

## Resumen de Cambios

| Aspecto | Antes | Después |
|--------|-------|---------|
| Transacciones | ❌ No | ✅ Sí (BeginTx/Commit) |
| 48-hour rule | ❌ No | ✅ Sí |
| Duraciones iguales | ❌ No | ✅ Sí |
| Traslape checking | ❌ No | ✅ Sí |
| Orden vuelos (ida<ret) | ❌ No | ✅ Sí |
| Total modificado | ❌ Nunca | ✅ Nunca |
| Líneas código | ~150 | ~350 |
| Compilación | ✅ | ✅ |

---

**Generado:** 2026-04-27

