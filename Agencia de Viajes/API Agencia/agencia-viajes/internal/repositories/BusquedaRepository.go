// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
)

// BusquedaRepository
//
// Repositorio encargado de las consultas de busqueda de ciudades
// y proveedores disponibles segun origen, destino y tipo de servicio.
type BusquedaRepository struct {
	db *sql.DB
}

// NewBusquedaRepository
//
// Crea e inicializa una nueva instancia de BusquedaRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *BusquedaRepository: instancia lista para usar
func NewBusquedaRepository(db *sql.DB) *BusquedaRepository {
	return &BusquedaRepository{db: db}
}

// BuscarCiudadID
//
// Consulta el ID de una ciudad a partir de su nombre y el nombre del pais
// al que pertenece. La comparacion se realiza ignorando mayusculas y espacios.
//
// Parametros:
//   - ciudad: nombre de la ciudad a buscar
//   - pais: nombre del pais al que pertenece la ciudad
//
// Retorna:
//   - *int: puntero al ID de la ciudad encontrada, nil si no existe
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *BusquedaRepository) BuscarCiudadID(ciudad, pais string) (*int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var id int
	err = conn.QueryRowContext(context.Background(), `
		SELECT c.ID FROM Ciudad c
		JOIN Pais p ON c.PaisID = p.ID
		WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?))
		  AND LOWER(TRIM(p.Nombre)) = LOWER(TRIM(?))
	`, ciudad, pais).Scan(&id)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return &id, nil
}

// ObtenerProveedoresPorOrigenYTipo
//
// Consulta los proveedores activos que operan desde una ciudad de origen dada
// y que pertenecen a un tipo de catalogo especifico (por ejemplo, hotelero o aereo).
//
// Parametros:
//   - ciudadOrigenID: ID de la ciudad de origen
//   - tipoCatalogoID: ID del tipo de catalogo (1=aerolinea, 2=hotelera)
//
// Retorna:
//   - []dto.ProveedorCatalogo: lista de proveedores que cumplen el criterio
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *BusquedaRepository) ObtenerProveedoresPorOrigenYTipo(
	ciudadOrigenID, tipoCatalogoID int,
) ([]dto.ProveedorCatalogo, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT DISTINCT p.ID, p.Nombre, p.URL_API, p.Token_HASH_Entrada, p.Porcentaje_Ganancia
		FROM Catalogo_Proveedor cp
		JOIN Proveedor p ON cp.Proveedor_ID = p.ID
		WHERE cp.Ciudad_Origen_ID  = ?
		  AND cp.Tipo_Catalogo_ID  = ?
		  AND p.EstadoID           = 1
	`, ciudadOrigenID, tipoCatalogoID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var proveedores []dto.ProveedorCatalogo
	for rows.Next() {
		var p dto.ProveedorCatalogo
		if err := rows.Scan(&p.ProveedorID, &p.Nombre, &p.URLApi, &p.TokenEntrada, &p.PorcentajeGanancia); err != nil {
			return nil, err
		}
		proveedores = append(proveedores, p)
	}
	return proveedores, nil
}

// ObtenerAerolineasPorRuta
//
// Consulta los proveedores de tipo aerolinea (Tipo_Catalogo_ID = 1) activos
// que operan desde una ciudad de origen determinada.
//
// Parametros:
//   - ciudadOrigenID: ID de la ciudad de origen del vuelo
//   - ciudadDestinoID: ID de la ciudad de destino (recibido pero no usado en el filtro SQL)
//
// Retorna:
//   - []dto.ProveedorCatalogo: lista de aerolineas disponibles para la ruta
//   - error: error de base de datos, nil si la operacion fue exitosa
//
// Notas:
//   - El filtro por ciudad de destino no se aplica en la consulta actual
func (r *BusquedaRepository) ObtenerAerolineasPorRuta(
	ciudadOrigenID, ciudadDestinoID int,
) ([]dto.ProveedorCatalogo, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT DISTINCT p.ID, p.Nombre, p.URL_API, p.Token_HASH_Entrada, p.Porcentaje_Ganancia
		FROM Catalogo_Proveedor cp
		JOIN Proveedor p ON cp.Proveedor_ID = p.ID
		WHERE cp.Ciudad_Origen_ID  = ?
		  AND cp.Tipo_Catalogo_ID  = 1
		  AND p.EstadoID           = 1
	`, ciudadOrigenID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var proveedores []dto.ProveedorCatalogo
	for rows.Next() {
		var p dto.ProveedorCatalogo
		if err := rows.Scan(&p.ProveedorID, &p.Nombre, &p.URLApi, &p.TokenEntrada, &p.PorcentajeGanancia); err != nil {
			return nil, err
		}
		proveedores = append(proveedores, p)
	}
	return proveedores, nil
}
