package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"context"
	"database/sql"
	"fmt"
	"io"
	"net/http"
)

// ImagenProveedorService
//
// Servicio que actua como proxy de imagenes hacia proveedores hoteleros aliados.
// Dada la URL del proveedor y el ID de la imagen, realiza la peticion HTTP
// al proveedor y retorna los bytes de la imagen para servirlos directamente
// al frontend sin almacenamiento intermedio.
type ImagenProveedorService struct {
	repo   *repositories.BusquedaRepository
	client *http.Client
}

// NewImagenProveedorService
//
// Crea e inicializa una nueva instancia de ImagenProveedorService.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *ImagenProveedorService: instancia lista para usar
func NewImagenProveedorService(db *sql.DB) *ImagenProveedorService {
	return &ImagenProveedorService{
		repo:   repositories.NewBusquedaRepository(db),
		client: &http.Client{Timeout: timeoutProveedor},
	}
}

// ObtenerImagen
//
// Resuelve la URL y el token del proveedor a partir del proveedorID,
// construye la ruta de imagen segun el tipo indicado y retorna los bytes
// de la imagen obtenida del proveedor.
//
// Parametros:
//   - proveedorID: ID del proveedor hotelero aliado en el catalogo
//   - tipoImagen:  "hotel", "habitacion" o "amenidad"
//   - imagenID:    ID de la imagen en el sistema del proveedor
//
// Retorna:
//   - []byte: bytes de la imagen lista para servir al cliente
//   - string: content-type retornado por el proveedor (ej: "image/jpeg")
//   - error:  si el proveedor no existe, no responde o retorna un estado no exitoso
func (s *ImagenProveedorService) ObtenerImagen(proveedorID int, tipoImagen string, imagenID int) ([]byte, string, error) {
	acceso, err := s.repo.ObtenerURLYTokenProveedor(proveedorID)
	if err != nil {
		return nil, "", fmt.Errorf("error al consultar proveedor: %w", err)
	}
	if acceso == nil {
		return nil, "", fmt.Errorf("proveedor %d no encontrado o inactivo", proveedorID)
	}

	url := construirURLImagen(acceso, tipoImagen, imagenID)

	ctx, cancel := context.WithTimeout(context.Background(), timeoutProveedor)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, "", fmt.Errorf("error al construir peticion: %w", err)
	}
	req.Header.Set("X-Agencia-Token", acceso.TokenEntrada)

	resp, err := s.client.Do(req)
	if err != nil {
		return nil, "", fmt.Errorf("proveedor no disponible: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, "", fmt.Errorf("proveedor respondio con status %d", resp.StatusCode)
	}

	bytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, "", fmt.Errorf("error al leer imagen: %w", err)
	}

	contentType := resp.Header.Get("Content-Type")
	if contentType == "" {
		contentType = "image/jpeg"
	}

	return bytes, contentType, nil
}

// construirURLImagen arma la ruta completa segun el tipo de imagen.
// Usa las mismas rutas que expone el proveedor en su ImagenAgenciaController.
func construirURLImagen(acceso *dto.ProveedorAcceso, tipoImagen string, imagenID int) string {
	switch tipoImagen {
	case "hotel":
		return fmt.Sprintf("%s/agencia/imagenes/hotel/%d", acceso.URLApi, imagenID)
	case "habitacion":
		return fmt.Sprintf("%s/agencia/imagenes/habitacion/%d", acceso.URLApi, imagenID)
	case "amenidad":
		return fmt.Sprintf("%s/agencia/imagenes/amenidad/%d", acceso.URLApi, imagenID)
	default:
		return fmt.Sprintf("%s/agencia/imagenes/hotel/%d", acceso.URLApi, imagenID)
	}
}
