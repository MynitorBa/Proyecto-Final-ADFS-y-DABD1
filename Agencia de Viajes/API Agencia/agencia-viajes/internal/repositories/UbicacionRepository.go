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

// UbicacionRepository
//
// Repositorio encargado de la gestion de entidades geograficas como paises,
// ciudades y nacionalidades. Implementa el patron buscar-o-crear para garantizar
// que no se dupliquen registros existentes en la base de datos.
type UbicacionRepository struct {
	db *sql.DB
}

// NewUbicacionRepository
//
// Crea e inicializa una nueva instancia de UbicacionRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *UbicacionRepository: instancia lista para usar
func NewUbicacionRepository(db *sql.DB) *UbicacionRepository {
	return &UbicacionRepository{db: db}
}

// BuscarOCrearPais
//
// Busca un pais por nombre en la base de datos. Si no existe, lo inserta
// y retorna el registro recien creado con su ID autogenerado.
//
// Parametros:
//   - nombre: nombre del pais a buscar o crear
//
// Retorna:
//   - models.Pais: entidad del pais encontrado o creado
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *UbicacionRepository) BuscarOCrearPais(nombre string) (models.Pais, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return models.Pais{}, err
	}
	defer conn.Close()

	var pais models.Pais
	err = conn.QueryRowContext(context.Background(), "SELECT ID, Nombre FROM Pais WHERE Nombre = ?", nombre).
		Scan(&pais.ID, &pais.Nombre)

	if err == sql.ErrNoRows {
		result, err := conn.ExecContext(context.Background(), "INSERT INTO Pais (Nombre) VALUES (?)", nombre)
		if err != nil {
			return models.Pais{}, err
		}
		id, _ := result.LastInsertId()
		pais = models.Pais{ID: int(id), Nombre: nombre}
	} else if err != nil {
		return models.Pais{}, err
	}

	return pais, nil
}

// BuscarOCrearCiudad
//
// Busca una ciudad por nombre y pais en la base de datos. Si no existe, la inserta
// y retorna el registro recien creado con su ID autogenerado.
//
// Parametros:
//   - nombre: nombre de la ciudad a buscar o crear
//   - paisID: ID del pais al que pertenece la ciudad
//
// Retorna:
//   - models.Ciudad: entidad de la ciudad encontrada o creada
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *UbicacionRepository) BuscarOCrearCiudad(nombre string, paisID int) (models.Ciudad, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return models.Ciudad{}, err
	}
	defer conn.Close()

	var ciudad models.Ciudad
	err = conn.QueryRowContext(context.Background(), "SELECT ID, Nombre, PaisID FROM Ciudad WHERE Nombre = ? AND PaisID = ?", nombre, paisID).
		Scan(&ciudad.ID, &ciudad.Nombre, &ciudad.PaisID)

	if err == sql.ErrNoRows {
		result, err := conn.ExecContext(context.Background(), "INSERT INTO Ciudad (Nombre, PaisID) VALUES (?, ?)", nombre, paisID)
		if err != nil {
			return models.Ciudad{}, err
		}
		id, _ := result.LastInsertId()
		ciudad = models.Ciudad{ID: int(id), Nombre: nombre, PaisID: paisID}
	} else if err != nil {
		return models.Ciudad{}, err
	}

	return ciudad, nil
}

// BuscarOCrearNacionalidad
//
// Busca una nacionalidad por nombre en la base de datos. Si no existe, la inserta
// y retorna el registro recien creado con su ID autogenerado.
//
// Parametros:
//   - nombre: nombre de la nacionalidad a buscar o crear
//
// Retorna:
//   - models.Nacionalidad: entidad de la nacionalidad encontrada o creada
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *UbicacionRepository) BuscarOCrearNacionalidad(nombre string) (models.Nacionalidad, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return models.Nacionalidad{}, err
	}
	defer conn.Close()

	var nacionalidad models.Nacionalidad
	err = conn.QueryRowContext(context.Background(),
		"SELECT ID, Nombre FROM Nacionalidad WHERE Nombre = ?", nombre).
		Scan(&nacionalidad.ID, &nacionalidad.Nombre)

	if err == sql.ErrNoRows {
		result, err := conn.ExecContext(context.Background(),
			"INSERT INTO Nacionalidad (Nombre) VALUES (?)", nombre)
		if err != nil {
			return models.Nacionalidad{}, err
		}
		id, _ := result.LastInsertId()
		nacionalidad = models.Nacionalidad{ID: int(id), Nombre: nombre}
	} else if err != nil {
		return models.Nacionalidad{}, err
	}

	return nacionalidad, nil
}
