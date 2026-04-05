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

// PerfilRepository
//
// Repositorio encargado de las operaciones de consulta y actualizacion
// del perfil del usuario autenticado, incluyendo datos personales,
// ubicacion, nacionalidades y gestion de contrasena.
type PerfilRepository struct {
	db *sql.DB
}

// NewPerfilRepository
//
// Crea e inicializa una nueva instancia de PerfilRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *PerfilRepository: instancia lista para usar
func NewPerfilRepository(db *sql.DB) *PerfilRepository {
	return &PerfilRepository{db: db}
}

// ObtenerPerfil
//
// Recupera todos los datos del perfil del usuario incluyendo nombre, apellido,
// correo, username, pasaporte, telefono, fecha de nacimiento, ciudad, pais
// y la lista de nacionalidades asociadas.
//
// Parametros:
//   - usuarioID: ID del usuario cuyo perfil se desea consultar
//
// Retorna:
//   - map[string]interface{}: mapa con todos los campos del perfil del usuario
//   - error: error de base de datos, nil si la operacion fue exitosa
//
// Notas:
//   - Ciudad y pais pueden ser NULL si el usuario no tiene ciudad asignada
//   - La lista de nacionalidades puede estar vacia si no se asignaron al registrarse
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

// ActualizarTelefono
//
// Actualiza el numero de telefono del usuario identificado por su ID.
//
// Parametros:
//   - usuarioID: ID del usuario a actualizar
//   - telefono: nuevo numero de telefono a registrar
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerHash
//
// Recupera el hash de la contrasena actual del usuario, utilizado para
// validar la contrasena anterior antes de permitir un cambio.
//
// Parametros:
//   - usuarioID: ID del usuario cuyo hash se desea obtener
//
// Retorna:
//   - string: hash bcrypt de la contrasena actual
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ActualizarContrasena
//
// Actualiza el hash de la contrasena del usuario en la base de datos.
// Debe recibir el hash ya procesado, no la contrasena en texto plano.
//
// Parametros:
//   - usuarioID: ID del usuario cuya contrasena se desea actualizar
//   - hash: nuevo hash bcrypt de la contrasena a persistir
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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
