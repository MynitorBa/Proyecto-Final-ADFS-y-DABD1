// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"context"
	"database/sql"
)

// CatalogoRepository
//
// Repositorio encargado de las operaciones sobre el catalogo de proveedores,
// incluyendo consulta de tipos, datos de conexion, sincronizacion y actualizacion
// de rutas disponibles por proveedor.
type CatalogoRepository struct {
	db *sql.DB
}

// NewCatalogoRepository
//
// Crea e inicializa una nueva instancia de CatalogoRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *CatalogoRepository: instancia lista para usar
func NewCatalogoRepository(db *sql.DB) *CatalogoRepository {
	return &CatalogoRepository{db: db}
}

// ObtenerTipoProveedor
//
// Consulta el tipo de proveedor asociado a un ID dado.
// El tipo determina la categoria del servicio (1=aerolinea, 2=hotelera).
//
// Parametros:
//   - proveedorID: ID del proveedor a consultar
//
// Retorna:
//   - int: ID del tipo de proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerDatosConexion
//
// Recupera la URL de la API y el token de entrada del proveedor especificado,
// necesarios para realizar llamadas al webservice externo.
//
// Parametros:
//   - proveedorID: ID del proveedor a consultar
//
// Retorna:
//   - urlAPI: URL base del API del proveedor
//   - tokenEntrada: token de autenticacion para las peticiones al proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerProveedoresActivos
//
// Recupera los IDs de todos los proveedores cuyo estado es activo (EstadoID = 1).
//
// Parametros:
//   - (ninguno)
//
// Retorna:
//   - []int: lista de IDs de proveedores activos
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// EliminarCatalogoPorProveedor
//
// Elimina todas las entradas del catalogo asociadas a un proveedor especifico.
// Utilizado antes de sincronizar el catalogo con datos actualizados del webservice.
//
// Parametros:
//   - proveedorID: ID del proveedor cuyo catalogo se desea eliminar
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// InsertarEntrada
//
// Registra una nueva entrada en el catalogo del proveedor, asociando una ciudad
// de origen, opcionalmente una ciudad de destino, el tipo de catalogo y el proveedor.
//
// Parametros:
//   - ciudadOrigenID: ID de la ciudad de origen del servicio
//   - ciudadDestinoID: puntero al ID de la ciudad de destino; puede ser nil para servicios hoteleros
//   - tipoCatalogoID: ID del tipo de catalogo (1=aerolinea, 2=hotelera)
//   - proveedorID: ID del proveedor al que pertenece esta entrada
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
//
// Notas:
//   - Si ciudadDestinoID es nil, se inserta NULL en la columna Ciudad_Destino_ID
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
