package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
)

type BusquedaRepository struct {
	db *sql.DB
}

func NewBusquedaRepository(db *sql.DB) *BusquedaRepository {
	return &BusquedaRepository{db: db}
}

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
		  AND cp.Ciudad_Destino_ID = ?
		  AND cp.Tipo_Catalogo_ID  = 1
		  AND p.EstadoID           = 1
	`, ciudadOrigenID, ciudadDestinoID)
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
