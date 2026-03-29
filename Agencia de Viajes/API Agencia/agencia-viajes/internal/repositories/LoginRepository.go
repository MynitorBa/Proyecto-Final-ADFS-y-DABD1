package repositories

import (
	"agencia-viajes/internal/models"
	"context"
	"database/sql"
)

type LoginRepository struct {
	db *sql.DB
}

func NewLoginRepository(db *sql.DB) *LoginRepository {
	return &LoginRepository{db: db}
}

func (r *LoginRepository) ObtenerPorUsernameOCorreo(login string) (models.Usuario, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return models.Usuario{}, err
	}
	defer conn.Close()

	var usuario models.Usuario
	err = conn.QueryRowContext(context.Background(), `
		SELECT ID, Nombre, Apellido, Correo, Username, Contrasena, RolID
		FROM Usuario
		WHERE Username = ? OR Correo = ?`, login, login).
		Scan(
			&usuario.ID,
			&usuario.Nombre,
			&usuario.Apellido,
			&usuario.Correo,
			&usuario.Username,
			&usuario.Contrasena,
			&usuario.RolID,
		)

	if err == sql.ErrNoRows {
		return models.Usuario{}, nil
	}
	if err != nil {
		return models.Usuario{}, err
	}

	return usuario, nil
}
