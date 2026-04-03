package repositories

import (
	"context"
	"database/sql"
)

type PerfilRepository struct {
	db *sql.DB
}

func NewPerfilRepository(db *sql.DB) *PerfilRepository {
	return &PerfilRepository{db: db}
}

// ObtenerPerfil devuelve todos los datos del usuario incluyendo ciudad, país y nacionalidades.
func (r *PerfilRepository) ObtenerPerfil(usuarioID int) (map[string]interface{}, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var (
		nombre, apellido, correo, username string
		pasaporte, telefono                string
		fechaNacimiento                    sql.NullString
		ciudad, pais                       sql.NullString
	)

	err = conn.QueryRowContext(context.Background(), `
		SELECT
			u.Nombre, u.Apellido, u.Correo, u.Username,
			u.Pasaporte, u.Telefono, u.FechaNacimiento,
			c.Nombre AS Ciudad, p.Nombre AS Pais
		FROM Usuario u
		LEFT JOIN Ciudad c ON u.CiudadID = c.ID
		LEFT JOIN Pais   p ON c.PaisID   = p.ID
		WHERE u.ID = ?
	`, usuarioID).Scan(
		&nombre, &apellido, &correo, &username,
		&pasaporte, &telefono, &fechaNacimiento,
		&ciudad, &pais,
	)
	if err != nil {
		return nil, err
	}

	// Nacionalidades
	rows, err := conn.QueryContext(context.Background(), `
		SELECT n.Nombre
		FROM UsuarioNacionalidad un
		JOIN Nacionalidad n ON un.NacionalidadID = n.ID
		WHERE un.UsuarioID = ?
	`, usuarioID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var nacs []string
	for rows.Next() {
		var n string
		if err := rows.Scan(&n); err == nil {
			nacs = append(nacs, n)
		}
	}

	return map[string]interface{}{
		"nombre":          nombre,
		"apellido":        apellido,
		"correo":          correo,
		"username":        username,
		"pasaporte":       pasaporte,
		"telefono":        telefono,
		"fechaNacimiento": fechaNacimiento.String,
		"ciudad":          ciudad.String,
		"pais":            pais.String,
		"nacionalidades":  nacs,
	}, nil
}

func (r *PerfilRepository) ActualizarTelefono(usuarioID int, telefono string) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(),
		"UPDATE Usuario SET Telefono = ? WHERE ID = ?",
		telefono, usuarioID,
	)
	return err
}

func (r *PerfilRepository) ObtenerHash(usuarioID int) (string, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return "", err
	}
	defer conn.Close()

	var hash string
	err = conn.QueryRowContext(context.Background(),
		"SELECT Contrasena FROM Usuario WHERE ID = ?", usuarioID,
	).Scan(&hash)
	return hash, err
}

func (r *PerfilRepository) ActualizarContrasena(usuarioID int, hash string) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(),
		"UPDATE Usuario SET Contrasena = ? WHERE ID = ?",
		hash, usuarioID,
	)
	return err
}