package services_test

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"errors"
	"testing"
)

// ── Mock del repositorio ──────────────────────────────────────────────────────

type mockProveedorRepo struct {
	rolID      int
	rolErr     error
	yaTiene    bool
	yaTieneErr error
	tipoValido bool
	tipoErr    error
	crearResp  dto.CrearProveedorResponse
	crearErr   error
}

func (m *mockProveedorRepo) ObtenerRolUsuario(usuarioID int) (int, error) {
	return m.rolID, m.rolErr
}
func (m *mockProveedorRepo) UsuarioYaTieneProveedor(usuarioID int) (bool, error) {
	return m.yaTiene, m.yaTieneErr
}
func (m *mockProveedorRepo) ExisteTipoProveedor(tipoID int) (bool, error) {
	return m.tipoValido, m.tipoErr
}
func (m *mockProveedorRepo) CrearProveedor(req dto.CrearProveedorRequest) (dto.CrearProveedorResponse, error) {
	return m.crearResp, m.crearErr
}

// ── Helper ────────────────────────────────────────────────────────────────────

func buildReq() dto.CrearProveedorRequest {
	return dto.CrearProveedorRequest{
		UsuarioID:          10,
		TipoProveedorID:    1,
		Nombre:             "AeroTest",
		URLAPI:             "https://api.aerotest.com",
		PorcentajeGanancia: 15.0,
	}
}

// ── Tests ─────────────────────────────────────────────────────────────────────

func TestProveedorService_CrearProveedor(t *testing.T) {
	okResp := dto.CrearProveedorResponse{ID: 99, Nombre: "AeroTest"}

	tests := []struct {
		name       string
		mock       mockProveedorRepo
		wantErr    bool
		wantErrMsg string
		wantID     int
	}{
		{
			name:    "crea exitosamente cuando todo es valido",
			mock:    mockProveedorRepo{rolID: 3, tipoValido: true, crearResp: okResp},
			wantErr: false,
			wantID:  99,
		},
		{
			name:       "error si ObtenerRolUsuario falla en BD",
			mock:       mockProveedorRepo{rolErr: errors.New("db down")},
			wantErr:    true,
			wantErrMsg: "db down",
		},
		{
			name:       "error si usuario no existe rolID cero",
			mock:       mockProveedorRepo{rolID: 0},
			wantErr:    true,
			wantErrMsg: "el usuario no existe",
		},
		{
			name:       "error si usuario no tiene rol webservice",
			mock:       mockProveedorRepo{rolID: 1},
			wantErr:    true,
			wantErrMsg: "rol webservice",
		},
		{
			name:       "error si UsuarioYaTieneProveedor falla en BD",
			mock:       mockProveedorRepo{rolID: 3, yaTieneErr: errors.New("conn lost")},
			wantErr:    true,
			wantErrMsg: "conn lost",
		},
		{
			name:       "error si usuario ya tiene proveedor asignado",
			mock:       mockProveedorRepo{rolID: 3, yaTiene: true},
			wantErr:    true,
			wantErrMsg: "ya tiene un proveedor",
		},
		{
			name:       "error si ExisteTipoProveedor falla en BD",
			mock:       mockProveedorRepo{rolID: 3, tipoErr: errors.New("timeout")},
			wantErr:    true,
			wantErrMsg: "timeout",
		},
		{
			name:       "error si tipo de proveedor no existe",
			mock:       mockProveedorRepo{rolID: 3, tipoValido: false},
			wantErr:    true,
			wantErrMsg: "tipo de proveedor no existe",
		},
		{
			name:       "error si CrearProveedor falla en BD",
			mock:       mockProveedorRepo{rolID: 3, tipoValido: true, crearErr: errors.New("insert failed")},
			wantErr:    true,
			wantErrMsg: "insert failed",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			svc := services.NewProveedorServiceConRepo(&tt.mock)
			got, err := svc.CrearProveedor(buildReq())

			if tt.wantErr {
				if err == nil {
					t.Fatalf("esperaba error pero no hubo ninguno")
				}
				if tt.wantErrMsg != "" && !containsStr(err.Error(), tt.wantErrMsg) {
					t.Errorf("mensaje de error: got %q, queria contener %q", err.Error(), tt.wantErrMsg)
				}
				return
			}

			if err != nil {
				t.Fatalf("no esperaba error, pero got: %v", err)
			}
			if got.ID != tt.wantID {
				t.Errorf("ID: got %d, want %d", got.ID, tt.wantID)
			}
		})
	}
}

func containsStr(s, sub string) bool {
	return len(sub) == 0 || (len(s) >= len(sub) && func() bool {
		for i := 0; i <= len(s)-len(sub); i++ {
			if s[i:i+len(sub)] == sub {
				return true
			}
		}
		return false
	}())
}
