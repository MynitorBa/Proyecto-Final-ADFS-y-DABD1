// # Package services
//
// Servicio de envio de ofertas periodicas por correo electronico.
// Cada 5 dias consulta los destinos mas populares, calcula un precio
// con descuento y envia un correo HTML a todos los usuarios suscritos.
package services

import (
	"agencia-viajes/internal/helpers"
	"context"
	"database/sql"
	"log"
	"sync"
	"time"
)

// OfertasService
//
// Servicio que ejecuta en segundo plano el envio de ofertas de paquetes
// a los usuarios que optaron por recibirlas (Recibir_Ofertas = 1).
type OfertasService struct {
	db     *sql.DB
	ticker *time.Ticker
	stop   chan struct{}
	wg     sync.WaitGroup
}

// NewOfertasService
//
// Crea una nueva instancia del servicio de ofertas.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *OfertasService: instancia lista para iniciar
func NewOfertasService(db *sql.DB) *OfertasService {
	return &OfertasService{db: db}
}

// Iniciar
//
// Arranca el ticker de 5 dias en una goroutine independiente.
// El primer envio ocurrira despues del primer tick (5 dias).
func (s *OfertasService) Iniciar() {
	s.ticker = time.NewTicker(5 * 24 * time.Hour)
	s.stop = make(chan struct{})
	s.wg.Add(1)
	go func() {
		defer s.wg.Done()
		for {
			select {
			case <-s.ticker.C:
				s.enviarOfertas()
			case <-s.stop:
				return
			}
		}
	}()
	log.Println("[Ofertas] Servicio iniciado — ciclo de 5 días")
}

// Detener
//
// Detiene el ticker y espera a que la goroutine termine correctamente.
func (s *OfertasService) Detener() {
	if s.ticker != nil {
		s.ticker.Stop()
	}
	if s.stop != nil {
		close(s.stop)
	}
	s.wg.Wait()
}

// ofertaItem representa un destino con precio base, descuento y precio final.
type ofertaItem struct {
	Destino     string
	Tipo        string
	PrecioBase  float64
	Descuento   float64
	PrecioFinal float64
}

// enviarOfertas consulta los destinos populares, construye el email y lo envia.
func (s *OfertasService) enviarOfertas() {
	log.Println("[Ofertas] Ejecutando envio de ofertas...")

	conn, err := s.db.Conn(context.Background())
	if err != nil {
		log.Println("[Ofertas] Error de conexion:", err)
		return
	}
	defer conn.Close()

	// Obtener usuarios suscritos y activos
	rows, err := conn.QueryContext(context.Background(), `
		SELECT ID, Nombre, Correo
		FROM Usuario
		WHERE Recibir_Ofertas = 1 AND EstadoID = 1
	`)
	if err != nil {
		log.Println("[Ofertas] Error consultando usuarios:", err)
		return
	}
	defer rows.Close()

	type usuarioBasico struct {
		ID     int
		Nombre string
		Correo string
	}
	var usuarios []usuarioBasico
	for rows.Next() {
		var u usuarioBasico
		if rows.Scan(&u.ID, &u.Nombre, &u.Correo) == nil {
			usuarios = append(usuarios, u)
		}
	}
	rows.Close()

	if len(usuarios) == 0 {
		log.Println("[Ofertas] No hay usuarios suscritos")
		return
	}

	// Obtener destinos mas buscados en los ultimos 180 dias
	destRows, err := conn.QueryContext(context.Background(), `
		SELECT b.Destino, b.Tipo_Busqueda_ID, COUNT(*) as cnt
		FROM busqueda b
		WHERE b.Destino IS NOT NULL AND b.Destino != ''
		  AND b.Fecha_Busqueda >= DATE_SUB(NOW(), INTERVAL 180 DAY)
		GROUP BY b.Destino, b.Tipo_Busqueda_ID
		ORDER BY cnt DESC
		LIMIT 3
	`)
	if err != nil {
		log.Println("[Ofertas] Error consultando destinos:", err)
		return
	}
	defer destRows.Close()

	// Precio promedio por tipo de reservacion para estimar el valor del paquete
	precioPromedio := map[int]float64{}
	for _, tipoID := range []int{1, 2, 3} {
		var avg sql.NullFloat64
		conn.QueryRowContext(context.Background(), `
			SELECT AVG(dr.Total)
			FROM detalles_reservacion dr
			JOIN Reservacion r ON r.ID = dr.ReservacionID
			WHERE r.Tipo_Reserva_ID = ? AND r.EstadoID IN (2, 5)
		`, tipoID).Scan(&avg)
		if avg.Valid && avg.Float64 > 0 {
			precioPromedio[tipoID] = avg.Float64
		} else {
			// Valores de referencia si no hay datos historicos
			switch tipoID {
			case 1:
				precioPromedio[tipoID] = 350.0
			case 2:
				precioPromedio[tipoID] = 180.0
			case 3:
				precioPromedio[tipoID] = 520.0
			}
		}
	}

	const descuento = 15.0
	tipoNombre := map[int]string{1: "Vuelo", 2: "Hotel", 3: "Paquete"}

	var ofertas []ofertaItem
	for destRows.Next() {
		var destino string
		var tipoID int
		var cnt int
		if destRows.Scan(&destino, &tipoID, &cnt) != nil {
			continue
		}
		base := precioPromedio[tipoID]
		ofertas = append(ofertas, ofertaItem{
			Destino:     destino,
			Tipo:        tipoNombre[tipoID],
			PrecioBase:  base,
			Descuento:   descuento,
			PrecioFinal: base * (1 - descuento/100),
		})
	}

	if len(ofertas) == 0 {
		log.Println("[Ofertas] No hay destinos disponibles para ofertar")
		return
	}

	// Enviar correo a cada usuario
	enviados := 0
	for _, u := range usuarios {
		html := helpers.BuildHTMLOfertas(u.Nombre, convertirOfertas(ofertas))
		asunto := "Ofertas especiales de Movent — ¡No te las pierdas!"
		if err := helpers.EnviarEmailHTML(u.Correo, asunto, html); err != nil {
			log.Printf("[Ofertas] Error enviando a %s: %v\n", u.Correo, err)
		} else {
			enviados++
		}
	}
	log.Printf("[Ofertas] Enviado a %d/%d usuarios\n", enviados, len(usuarios))
}

// convertirOfertas convierte el slice interno a la estructura publica del helper.
func convertirOfertas(items []ofertaItem) []helpers.OfertaItem {
	result := make([]helpers.OfertaItem, len(items))
	for i, o := range items {
		result[i] = helpers.OfertaItem{
			Destino:     o.Destino,
			Tipo:        o.Tipo,
			PrecioBase:  o.PrecioBase,
			Descuento:   o.Descuento,
			PrecioFinal: o.PrecioFinal,
		}
	}
	return result
}
