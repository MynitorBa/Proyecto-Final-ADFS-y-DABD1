// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"context"
	"database/sql"
)

// UsuarioRepository
//
// Repositorio encargado de las operaciones sobre la entidad Usuario,
// incluyendo validacion de unicidad de campos, creacion de nuevos usuarios,
// asignacion de nacionalidades y consulta de datos de contacto.
type UsuarioRepository struct {
	db *sql.DB
}

// NewUsuarioRepository
//
// Crea e inicializa una nueva instancia de UsuarioRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *UsuarioRepository: instancia lista para usar
func NewUsuarioRepository(db *sql.DB) *UsuarioRepository {
	return &UsuarioRepository{db: db}
}

// ExisteCorreo
//
// Verifica si ya existe un usuario registrado con el correo electronico indicado.
//
// Parametros:
//   - correo: correo electronico a verificar
//
// Retorna:
//   - bool: true si el correo ya esta en uso, false en caso contrario
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ExistePasaporte
//
// Verifica si ya existe un usuario registrado con el numero de pasaporte indicado.
//
// Parametros:
//   - pasaporte: numero de pasaporte a verificar
//
// Retorna:
//   - bool: true si el pasaporte ya esta en uso, false en caso contrario
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ExisteUsername
//
// Verifica si ya existe un usuario registrado con el nombre de usuario indicado.
//
// Parametros:
//   - username: nombre de usuario a verificar
//
// Retorna:
//   - bool: true si el username ya esta en uso, false en caso contrario
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// CrearUsuario
//
// Inserta un nuevo usuario en la base de datos. La contrasena se hashea antes
// de persistirse utilizando el helper de seguridad.
//
// Parametros:
//   - req: DTO con los datos del formulario de registro del usuario
//   - ciudadID: ID de la ciudad de residencia del usuario
//   - rolID: ID del rol asignado al nuevo usuario
//   - estadoID: ID del estado inicial del usuario (activo, pendiente, etc.)
//
// Retorna:
//   - int: ID autogenerado del usuario recien creado
//   - error: error de hasheo o de base de datos, nil si la operacion fue exitosa
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

// AsignarNacionalidades
//
// Inserta las asociaciones entre un usuario y sus nacionalidades en la tabla
// UsuarioNacionalidad. Itera sobre la lista de IDs de nacionalidad proporcionada.
//
// Parametros:
//   - usuarioID: ID del usuario al que se le asignan las nacionalidades
//   - nacionalidadIDs: lista de IDs de nacionalidades a asociar
//
// Retorna:
//   - error: error de base de datos si alguna insercion falla, nil si todas fueron exitosas
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

// ObtenerNombreYEmail
//
// Recupera el nombre completo y el correo electronico de un usuario por su ID.
// El nombre completo se construye concatenando Nombre y Apellido en Go para
// evitar problemas de concatenacion en distintos motores de base de datos.
//
// Parametros:
//   - usuarioID: ID del usuario a consultar
//
// Retorna:
//   - nombre: nombre completo del usuario (Nombre + Apellido)
//   - email: correo electronico del usuario
//   - error: error de base de datos, nil si la operacion fue exitosa
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

	if apellido != "" {
		nombre = nombre + " " + apellido
	}
	return
}

// ObtenerTodos
//
// Recupera la lista completa de usuarios registrados en el sistema,
// incluyendo su rol asignado mediante un JOIN con la tabla rol.
// Usado por el panel de administracion para gestion de roles
// y asignacion de usuarios WebService a proveedores.
//
// Retorna:
//   - []dto.UsuarioResumen: lista de usuarios con id, nombre, apellido, correo y rol
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *UsuarioRepository) ObtenerTodos() ([]dto.UsuarioResumen, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT u.ID, u.Nombre, u.Apellido, u.Correo, u.RolID, r.RolNombre
		FROM usuario u
		JOIN rol r ON r.ID = u.RolID
		ORDER BY u.ID
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var lista []dto.UsuarioResumen
	for rows.Next() {
		var u dto.UsuarioResumen
		rows.Scan(&u.ID, &u.Nombre, &u.Apellido, &u.Correo, &u.RolID, &u.Rol)
		lista = append(lista, u)
	}
	if lista == nil {
		lista = []dto.UsuarioResumen{}
	}
	return lista, nil
}