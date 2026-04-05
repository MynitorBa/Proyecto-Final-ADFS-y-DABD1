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
	"fmt"
)

// ProveedorRepository
//
// Repositorio encargado de las operaciones sobre la entidad Proveedor,
// incluyendo creacion, validacion de roles y existencia, almacenamiento de tokens
// y consulta de datos de conexion por distintos criterios.
type ProveedorRepository struct {
	db *sql.DB
}

// NewProveedorRepository
//
// Crea e inicializa una nueva instancia de ProveedorRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *ProveedorRepository: instancia lista para usar
func NewProveedorRepository(db *sql.DB) *ProveedorRepository {
	return &ProveedorRepository{db: db}
}

// ObtenerRolUsuario
//
// Consulta el RolID asignado a un usuario especifico. Retorna 0 si el usuario
// no existe en la base de datos.
//
// Parametros:
//   - usuarioID: ID del usuario a consultar
//
// Retorna:
//   - int: ID del rol del usuario, 0 si no existe
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) ObtenerRolUsuario(usuarioID int) (int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	var rolID int
	err = conn.QueryRowContext(context.Background(),
		"SELECT RolID FROM Usuario WHERE ID = ?", usuarioID,
	).Scan(&rolID)
	if err == sql.ErrNoRows {
		return 0, nil // usuario no existe
	}
	return rolID, err
}

// UsuarioYaTieneProveedor
//
// Verifica si un usuario webservice ya tiene un proveedor registrado y asociado
// a su cuenta, para evitar duplicidad.
//
// Parametros:
//   - usuarioID: ID del usuario a verificar
//
// Retorna:
//   - bool: true si ya existe un proveedor asociado, false en caso contrario
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) UsuarioYaTieneProveedor(usuarioID int) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var id int
	err = conn.QueryRowContext(context.Background(),
		"SELECT ID FROM Proveedor WHERE Usuario_ID = ?", usuarioID,
	).Scan(&id)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err != nil {
		return false, err
	}
	return true, nil
}

// ExisteTipoProveedor
//
// Verifica si existe un registro en la tabla Tipo_Proveedor con el ID indicado.
//
// Parametros:
//   - tipoID: ID del tipo de proveedor a validar
//
// Retorna:
//   - bool: true si el tipo existe, false en caso contrario
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) ExisteTipoProveedor(tipoID int) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var id int
	err = conn.QueryRowContext(context.Background(),
		"SELECT ID FROM Tipo_Proveedor WHERE ID = ?", tipoID,
	).Scan(&id)
	if err == sql.ErrNoRows {
		return false, nil
	}
	return true, err
}

// CrearProveedor
//
// Inserta un nuevo proveedor en la base de datos con estado activo (EstadoID = 1)
// y tokens vacios que seran generados y guardados posteriormente.
//
// Parametros:
//   - req: DTO con los datos necesarios para crear el proveedor
//
// Retorna:
//   - dto.CrearProveedorResponse: datos del proveedor recien creado incluyendo su ID
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) CrearProveedor(req dto.CrearProveedorRequest) (dto.CrearProveedorResponse, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return dto.CrearProveedorResponse{}, err
	}
	defer conn.Close()

	const estadoActivo = 1

	result, err := conn.ExecContext(context.Background(), `
		INSERT INTO Proveedor
			(Nombre, Tipo_Proveedor_ID, URL_API, Usuario_ID, EstadoID, Porcentaje_Ganancia, Token_HASH_Entrada, Token_HASH_Salida)
		VALUES (?, ?, ?, ?, ?, ?, '', '')`,
		req.Nombre,
		req.TipoProveedorID,
		req.URLAPI,
		req.UsuarioID,
		estadoActivo,
		req.PorcentajeGanancia,
	)
	if err != nil {
		return dto.CrearProveedorResponse{}, err
	}

	id, _ := result.LastInsertId()

	return dto.CrearProveedorResponse{
		ID:                 int(id),
		Nombre:             req.Nombre,
		TipoProveedorID:    req.TipoProveedorID,
		URLAPI:             req.URLAPI,
		UsuarioID:          req.UsuarioID,
		EstadoID:           estadoActivo,
		PorcentajeGanancia: req.PorcentajeGanancia,
	}, nil
}

// GuardarTokens
//
// Actualiza los tokens de entrada y salida de un proveedor existente.
// Se utiliza luego de la creacion del proveedor para persistir los hashes generados.
//
// Parametros:
//   - proveedorID: ID del proveedor al que se asignan los tokens
//   - tokenEntrada: hash del token de entrada para autenticar peticiones entrantes
//   - tokenSalida: hash del token de salida para autenticar peticiones salientes
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) GuardarTokens(proveedorID int, tokenEntrada, tokenSalida string) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(), `
		UPDATE Proveedor
		SET Token_HASH_Entrada = ?, Token_HASH_Salida = ?
		WHERE ID = ?`,
		tokenEntrada, tokenSalida, proveedorID,
	)
	return err
}

// ObtenerURLAPI
//
// Recupera la URL del API de un proveedor especifico para realizar llamadas
// al webservice externo.
//
// Parametros:
//   - proveedorID: ID del proveedor a consultar
//
// Retorna:
//   - string: URL del API del proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) ObtenerURLAPI(proveedorID int) (string, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return "", err
	}
	defer conn.Close()

	var urlAPI string
	err = conn.QueryRowContext(context.Background(),
		"SELECT URL_API FROM Proveedor WHERE ID = ?", proveedorID,
	).Scan(&urlAPI)
	return urlAPI, err
}

// ObtenerProveedorPorToken
//
// Busca un proveedor utilizando su token de entrada (Token_HASH_Entrada).
// Retorna nil si no se encuentra ningun proveedor con ese token.
//
// Parametros:
//   - token: hash del token de entrada a buscar
//
// Retorna:
//   - *dto.ProveedorIdentidad: datos de identidad del proveedor encontrado, nil si no existe
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ProveedorRepository) ObtenerProveedorPorToken(token string) (*dto.ProveedorIdentidad, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var p dto.ProveedorIdentidad
	err = conn.QueryRowContext(context.Background(), `
		SELECT ID, Nombre, Tipo_Proveedor_ID
		FROM Proveedor
		WHERE Token_HASH_Entrada = ?`, token,
	).Scan(&p.ID, &p.Nombre, &p.TipoProveedorID)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return &p, nil
}

// ObtenerProveedorPorTipo
//
// Recupera la URL del API y el token de entrada del primer proveedor activo
// que corresponda al tipo indicado. Retorna error si no hay proveedores activos
// de ese tipo.
//
// Parametros:
//   - tipoProveedorID: ID del tipo de proveedor a buscar (1=aerolinea, 2=hotelera)
//
// Retorna:
//   - *dto.DetalleProveedor: datos de conexion del proveedor encontrado
//   - error: error si no hay proveedor activo del tipo indicado o falla la consulta
func (r *ProveedorRepository) ObtenerProveedorPorTipo(tipoProveedorID int) (*dto.DetalleProveedor, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var p dto.DetalleProveedor
	err = conn.QueryRowContext(context.Background(), `
		SELECT URL_API, Token_HASH_Entrada
		FROM Proveedor
		WHERE Tipo_Proveedor_ID = ? AND EstadoID = 1
		LIMIT 1
	`, tipoProveedorID).Scan(&p.URLAPI, &p.TokenEntrada)

	if err == sql.ErrNoRows {
		return nil, fmt.Errorf("no hay proveedor activo de tipo %d", tipoProveedorID)
	}
	if err != nil {
		return nil, err
	}
	return &p, nil
}

// ObtenerProveedorPorID
//
// Recupera la URL del API y el token de entrada de un proveedor especifico
// verificando que se encuentre en estado activo (EstadoID = 1).
//
// Parametros:
//   - proveedorID: ID del proveedor a consultar
//
// Retorna:
//   - *dto.DetalleProveedor: datos de conexion del proveedor encontrado
//   - error: error si el proveedor no existe o esta inactivo, nil si fue exitosa
func (r *ProveedorRepository) ObtenerProveedorPorID(proveedorID int) (*dto.DetalleProveedor, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var p dto.DetalleProveedor
	err = conn.QueryRowContext(context.Background(), `
		SELECT URL_API, Token_HASH_Entrada
		FROM Proveedor
		WHERE ID = ? AND EstadoID = 1
	`, proveedorID).Scan(&p.URLAPI, &p.TokenEntrada)

	if err == sql.ErrNoRows {
		return nil, fmt.Errorf("proveedor %d no encontrado o inactivo", proveedorID)
	}
	return &p, err
}
