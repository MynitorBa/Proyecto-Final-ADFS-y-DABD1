package repositories

import (
	"agencia-viajes/internal/models"
	"context"
	"database/sql"
)

type UbicacionRepository struct {
	db *sql.DB
}

func NewUbicacionRepository(db *sql.DB) *UbicacionRepository {
	return &UbicacionRepository{db: db}
}

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
