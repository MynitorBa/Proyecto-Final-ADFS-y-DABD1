package repositories

import (
	"context"
	"database/sql"
)

type CatalogoRepository struct {
	db *sql.DB
}

func NewCatalogoRepository(db *sql.DB) *CatalogoRepository {
	return &CatalogoRepository{db: db}
}

// Obtener tipo de catálogo del proveedor (1=aerolinea, 2=hotelera)
func (r *CatalogoRepository) ObtenerTipoProveedor(proveedorID int) (int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	var tipoID int
	err = conn.QueryRowContext(context.Background(),
		"SELECT Tipo_Proveedor_ID FROM Proveedor WHERE ID = ?", proveedorID,
	).Scan(&tipoID)
	return tipoID, err
}

// Obtener URL y token del proveedor para llamarlo
func (r *CatalogoRepository) ObtenerDatosConexion(proveedorID int) (urlAPI, tokenEntrada string, err error) {
	conn, connErr := r.db.Conn(context.Background())
	if connErr != nil {
		return "", "", connErr
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(),
		"SELECT URL_API, Token_HASH_Entrada FROM Proveedor WHERE ID = ?", proveedorID,
	).Scan(&urlAPI, &tokenEntrada)
	return
}

// Obtener todos los proveedores activos
func (r *CatalogoRepository) ObtenerProveedoresActivos() ([]int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(),
		"SELECT ID FROM Proveedor WHERE EstadoID = 1",
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var ids []int
	for rows.Next() {
		var id int
		if err := rows.Scan(&id); err != nil {
			return nil, err
		}
		ids = append(ids, id)
	}
	return ids, nil
}

// Eliminar catálogo actual de un proveedor
func (r *CatalogoRepository) EliminarCatalogoPorProveedor(proveedorID int) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(),
		"DELETE FROM Catalogo_Proveedor WHERE Proveedor_ID = ?", proveedorID,
	)
	return err
}

// Insertar una entrada en el catálogo
func (r *CatalogoRepository) InsertarEntrada(
	ciudadOrigenID int,
	ciudadDestinoID *int, // puntero — puede ser nil si es hotelera
	tipoCatalogoID int,
	proveedorID int,
) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	var destino interface{}
	if ciudadDestinoID != nil {
		destino = *ciudadDestinoID
	} else {
		destino = nil
	}

	_, err = conn.ExecContext(context.Background(), `
		INSERT INTO Catalogo_Proveedor 
			(Ciudad_Origen_ID, Ciudad_Destino_ID, Tipo_Catalogo_ID, Proveedor_ID, Ultima_Actualizacion)
		VALUES (?, ?, ?, ?, NOW())`,
		ciudadOrigenID, destino, tipoCatalogoID, proveedorID,
	)
	return err
}
