package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
)

type ProveedorRepository struct {
	db *sql.DB
}

func NewProveedorRepository(db *sql.DB) *ProveedorRepository {
	return &ProveedorRepository{db: db}
}

// Verifica que el usuario exista y sea rol 3 (webservice)
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

// Verifica si el usuario webservice ya tiene un proveedor asignado
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

// Verifica que el TipoProveedor exista
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

// Crea el proveedor — EstadoID=1 (Activo) por defecto
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

// Obtener URL_API del proveedor para llamar a la aerolinea
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
