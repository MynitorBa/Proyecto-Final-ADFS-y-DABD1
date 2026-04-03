package repositories

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"context"
	"database/sql"
)

type UsuarioRepository struct {
	db *sql.DB
}

func NewUsuarioRepository(db *sql.DB) *UsuarioRepository {
	return &UsuarioRepository{db: db}
}

func (r *UsuarioRepository) ExisteCorreo(correo string) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var id int
	err = conn.QueryRowContext(context.Background(), "SELECT ID FROM Usuario WHERE Correo = ?", correo).Scan(&id)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err != nil {
		return false, err
	}
	return true, nil
}

func (r *UsuarioRepository) ExistePasaporte(pasaporte string) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var id int
	err = conn.QueryRowContext(context.Background(), "SELECT ID FROM Usuario WHERE Pasaporte = ?", pasaporte).Scan(&id)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err != nil {
		return false, err
	}
	return true, nil
}

func (r *UsuarioRepository) ExisteUsername(username string) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var id int
	err = conn.QueryRowContext(context.Background(), "SELECT ID FROM Usuario WHERE Username = ?", username).Scan(&id)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err != nil {
		return false, err
	}
	return true, nil
}

func (r *UsuarioRepository) CrearUsuario(req dto.RegistroUsuarioRequest, ciudadID, rolID, estadoID int) (int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	hashedPassword, err := helpers.HashPassword(req.Contrasena)
	if err != nil {
		return 0, err
	}

	result, err := conn.ExecContext(context.Background(), `
        INSERT INTO Usuario (Nombre, Apellido, Correo, Username, Contrasena, Pasaporte, Telefono, FechaNacimiento, CiudadID, RolID, EstadoID)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
		req.Nombre, req.Apellido, req.Correo, req.Username,
		hashedPassword, req.Pasaporte, req.Telefono, req.FechaNacimiento,
		ciudadID, rolID, estadoID,
	)
	if err != nil {
		return 0, err
	}

	id, _ := result.LastInsertId()
	return int(id), nil
}

func (r *UsuarioRepository) AsignarNacionalidades(usuarioID int, nacionalidadIDs []int) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	for _, nacID := range nacionalidadIDs {
		_, err := conn.ExecContext(context.Background(),
			"INSERT INTO UsuarioNacionalidad (UsuarioID, NacionalidadID) VALUES (?, ?)",
			usuarioID, nacID,
		)
		if err != nil {
			return err
		}
	}

	return nil
}

// ObtenerNombreYEmail devuelve nombre completo y correo del usuario por ID.
// Escanea Nombre y Apellido por separado para evitar problemas de concatenación en Oracle.
func (r *UsuarioRepository) ObtenerNombreYEmail(usuarioID int) (nombre, email string, err error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return
	}
	defer conn.Close()

	var apellido string
	err = conn.QueryRowContext(context.Background(), `
		SELECT Nombre, Apellido, Correo
		FROM Usuario
		WHERE ID = ?
	`, usuarioID).Scan(&nombre, &apellido, &email)
	if err != nil {
		return
	}

	// Concatenar en Go, no en SQL
	if apellido != "" {
		nombre = nombre + " " + apellido
	}
	return
}