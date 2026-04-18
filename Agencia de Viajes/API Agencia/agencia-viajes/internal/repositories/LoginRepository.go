// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"agencia-viajes/internal/models"
	"context"
	"database/sql"
)

// LoginRepository
//
// Repositorio encargado de las consultas necesarias para el proceso
// de autenticacion de usuarios, permitiendo buscar por nombre de usuario
// o por correo electronico.
type LoginRepository struct {
	db *sql.DB
}

// NewLoginRepository
//
// Crea e inicializa una nueva instancia de LoginRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *LoginRepository: instancia lista para usar
func NewLoginRepository(db *sql.DB) *LoginRepository {
	return &LoginRepository{db: db}
}

// ObtenerPorUsernameOCorreo
//
// Busca un usuario en la base de datos comparando el valor recibido contra
// el campo Username y el campo Correo. Retorna el modelo completo del usuario
// si se encuentra una coincidencia, o un modelo vacio si no existe.
//
// Parametros:
//   - login: valor a buscar, puede ser el username o el correo del usuario
//
// Retorna:
//   - models.Usuario: datos completos del usuario encontrado, vacio si no existe
//   - error: error de base de datos, nil si la operacion fue exitosa
//
// Notas:
//   - Si no se encuentra el usuario se retorna un struct vacio sin error
func (r *LoginRepository) ObtenerPorUsernameOCorreo(login string) (models.Usuario, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return models.Usuario{}, err
	}
	defer conn.Close()

	var usuario models.Usuario
	err = conn.QueryRowContext(context.Background(), `
		SELECT ID, Nombre, Apellido, Correo, Username, Contrasena, RolID, EstadoID
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
			&usuario.EstadoID,
		)

	if err == sql.ErrNoRows {
		return models.Usuario{}, nil
	}
	if err != nil {
		return models.Usuario{}, err
	}

	return usuario, nil
}
