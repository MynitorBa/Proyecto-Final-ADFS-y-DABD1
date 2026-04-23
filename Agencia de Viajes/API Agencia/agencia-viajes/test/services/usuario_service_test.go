package services_test

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"errors"
	"testing"
)

// ── Mock del repositorio ──────────────────────────────────────────────────────

type mockUsuarioRepo struct {
	correoExiste    bool
	correoErr       error
	pasaporteExiste bool
	pasaporteErr    error
	usernameExiste  bool
	usernameErr     error
}

func (m *mockUsuarioRepo) ExisteCorreo(correo string) (bool, error) {
	return m.correoExiste, m.correoErr
}
func (m *mockUsuarioRepo) ExistePasaporte(pasaporte string) (bool, error) {
	return m.pasaporteExiste, m.pasaporteErr
}
func (m *mockUsuarioRepo) ExisteUsername(username string) (bool, error) {
	return m.usernameExiste, m.usernameErr
}
func (m *mockUsuarioRepo) CrearUsuario(req dto.RegistroUsuarioRequest, ciudadID, rolID, estadoID int) (int, error) {
	return 0, nil
}
func (m *mockUsuarioRepo) AsignarNacionalidades(usuarioID int, nacionalidadIDs []int) error {
	return nil
}
func (m *mockUsuarioRepo) ObtenerTodos() ([]dto.UsuarioResumen, error) {
	return nil, nil
}

// ── Helper ────────────────────────────────────────────────────────────────────

func buildRegistroReq() dto.RegistroUsuarioRequest {
	return dto.RegistroUsuarioRequest{
		Nombre:    "Carlos",
		Apellido:  "Ramos",
		Correo:    "carlos@movent.com",
		Username:  "carlos_r",
		Pasaporte: "GT123456",
	}
}

// ── Tests ─────────────────────────────────────────────────────────────────────

func TestUsuarioService_ValidarDatosUnicos(t *testing.T) {
	tests := []struct {
		name            string
		mock            mockUsuarioRepo
		wantErr         bool
		wantCorreo      bool
		wantPasaporte   bool
		wantUsername    bool
	}{
		{
			name:          "todos los campos disponibles — sin conflictos",
			mock:          mockUsuarioRepo{},
			wantErr:       false,
			wantCorreo:    false,
			wantPasaporte: false,
			wantUsername:  false,
		},
		{
			name:          "correo ya registrado",
			mock:          mockUsuarioRepo{correoExiste: true},
			wantErr:       false,
			wantCorreo:    true,
			wantPasaporte: false,
			wantUsername:  false,
		},
		{
			name:          "pasaporte ya registrado",
			mock:          mockUsuarioRepo{pasaporteExiste: true},
			wantErr:       false,
			wantCorreo:    false,
			wantPasaporte: true,
			wantUsername:  false,
		},
		{
			name:          "username ya registrado",
			mock:          mockUsuarioRepo{usernameExiste: true},
			wantErr:       false,
			wantCorreo:    false,
			wantPasaporte: false,
			wantUsername:  true,
		},
		{
			name:          "correo y username ya registrados",
			mock:          mockUsuarioRepo{correoExiste: true, usernameExiste: true},
			wantErr:       false,
			wantCorreo:    true,
			wantPasaporte: false,
			wantUsername:  true,
		},
		{
			name:          "los tres campos ya registrados",
			mock:          mockUsuarioRepo{correoExiste: true, pasaporteExiste: true, usernameExiste: true},
			wantErr:       false,
			wantCorreo:    true,
			wantPasaporte: true,
			wantUsername:  true,
		},
		{
			name:    "error de BD al verificar correo",
			mock:    mockUsuarioRepo{correoErr: errors.New("db error")},
			wantErr: true,
		},
		{
			name:    "error de BD al verificar pasaporte",
			mock:    mockUsuarioRepo{pasaporteErr: errors.New("connection reset")},
			wantErr: true,
		},
		{
			name:    "error de BD al verificar username",
			mock:    mockUsuarioRepo{usernameErr: errors.New("timeout")},
			wantErr: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			svc := services.NewUsuarioServiceConRepo(&tt.mock, nil)
			resp, err := svc.ValidarDatosUnicos(buildRegistroReq())

			if tt.wantErr {
				if err == nil {
					t.Fatalf("esperaba error pero no hubo ninguno")
				}
				return
			}

			if err != nil {
				t.Fatalf("no esperaba error, pero got: %v", err)
			}
			if resp.Correo != tt.wantCorreo {
				t.Errorf("Correo: got %v, want %v", resp.Correo, tt.wantCorreo)
			}
			if resp.Pasaporte != tt.wantPasaporte {
				t.Errorf("Pasaporte: got %v, want %v", resp.Pasaporte, tt.wantPasaporte)
			}
			if resp.Username != tt.wantUsername {
				t.Errorf("Username: got %v, want %v", resp.Username, tt.wantUsername)
			}
		})
	}
}
