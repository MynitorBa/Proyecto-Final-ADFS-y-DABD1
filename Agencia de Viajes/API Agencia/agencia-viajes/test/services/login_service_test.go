package services_test

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/models"
	"agencia-viajes/internal/services"
	"errors"
	"testing"
)

// ── Mock del repositorio ──────────────────────────────────────────────────────

type mockLoginRepo struct {
	usuario models.Usuario
	err     error
}

func (m *mockLoginRepo) ObtenerPorUsernameOCorreo(login string) (models.Usuario, error) {
	return m.usuario, m.err
}

// ── Helpers ───────────────────────────────────────────────────────────────────

// captchaOk simula un captcha valido.
func captchaOk(token string) (bool, string) { return true, "" }

// captchaFail simula un captcha rechazado por Google.
func captchaFail(token string) (bool, string) { return false, "captcha invalido" }

// usuarioActivo construye un usuario habilitado con contrasena bcrypt de "password123".
func usuarioActivo() models.Usuario {
	hash, _ := helpers.HashPassword("password123")
	return models.Usuario{
		ID:         1,
		Nombre:     "Ana",
		Apellido:   "Lopez",
		Correo:     "ana@movent.com",
		Username:   "ana_l",
		Contrasena: hash,
		EstadoID:   1,
		RolID:      1,
	}
}

func buildLoginReq(login, pass, captcha string) dto.LoginRequest {
	return dto.LoginRequest{Login: login, Contrasena: pass, Captcha: captcha}
}

// ── Tests ─────────────────────────────────────────────────────────────────────

func TestLoginService_Login(t *testing.T) {
	tests := []struct {
		name        string
		req         dto.LoginRequest
		mock        mockLoginRepo
		captchaFn   func(string) (bool, string)
		wantErr     bool
		wantErrIs   error
		wantRolID   int
	}{
		{
			name:      "error si login esta vacio",
			req:       buildLoginReq("", "pass", "tok"),
			mock:      mockLoginRepo{},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCamposVacios,
		},
		{
			name:      "error si contraseña esta vacia",
			req:       buildLoginReq("ana_l", "", "tok"),
			mock:      mockLoginRepo{},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCamposVacios,
		},
		{
			name:      "error si login es solo espacios",
			req:       buildLoginReq("   ", "pass", "tok"),
			mock:      mockLoginRepo{},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCamposVacios,
		},
		{
			name:      "error si captcha esta vacio",
			req:       buildLoginReq("ana_l", "pass", ""),
			mock:      mockLoginRepo{},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCaptchaAusente,
		},
		{
			name:      "error si captcha es solo espacios",
			req:       buildLoginReq("ana_l", "pass", "   "),
			mock:      mockLoginRepo{},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCaptchaAusente,
		},
		{
			name:      "error si captcha es rechazado por google",
			req:       buildLoginReq("ana_l", "pass", "token-invalido"),
			mock:      mockLoginRepo{},
			captchaFn: captchaFail,
			wantErr:   true,
			wantErrIs: services.ErrCaptchaInvalido,
		},
		{
			name:      "error si el repositorio falla en BD",
			req:       buildLoginReq("ana_l", "pass", "tok"),
			mock:      mockLoginRepo{err: errors.New("db timeout")},
			captchaFn: captchaOk,
			wantErr:   true,
		},
		{
			name:      "error credenciales invalidas si usuario no existe en BD",
			req:       buildLoginReq("noexiste", "pass", "tok"),
			mock:      mockLoginRepo{usuario: models.Usuario{ID: 0}},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCredencialesInvalidas,
		},
		{
			name:      "error usuario deshabilitado si EstadoID distinto de 1",
			req:       buildLoginReq("ana_l", "pass", "tok"),
			mock:      mockLoginRepo{usuario: models.Usuario{ID: 5, EstadoID: 2, Contrasena: "x"}},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrUsuarioDeshabilitado,
		},
		{
			name:      "error credenciales invalidas si contrasena no coincide",
			req:       buildLoginReq("ana_l", "contrasena-incorrecta", "tok"),
			mock:      mockLoginRepo{usuario: usuarioActivo()},
			captchaFn: captchaOk,
			wantErr:   true,
			wantErrIs: services.ErrCredencialesInvalidas,
		},
		{
			name:      "login exitoso retorna datos del usuario",
			req:       buildLoginReq("ana_l", "password123", "tok"),
			mock:      mockLoginRepo{usuario: usuarioActivo()},
			captchaFn: captchaOk,
			wantErr:   false,
			wantRolID: 1,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			svc := services.NewLoginServiceConRepo(&tt.mock, tt.captchaFn)
			resp, err := svc.Login(tt.req)

			if tt.wantErr {
				if err == nil {
					t.Fatalf("esperaba error pero no hubo ninguno")
				}
				if tt.wantErrIs != nil && !errors.Is(err, tt.wantErrIs) {
					t.Errorf("error: got %v, queria %v", err, tt.wantErrIs)
				}
				return
			}

			if err != nil {
				t.Fatalf("no esperaba error, pero got: %v", err)
			}
			if resp.RolID != tt.wantRolID {
				t.Errorf("RolID: got %d, want %d", resp.RolID, tt.wantRolID)
			}
			if resp.ID == 0 {
				t.Errorf("esperaba ID no-cero en respuesta exitosa")
			}
		})
	}
}
