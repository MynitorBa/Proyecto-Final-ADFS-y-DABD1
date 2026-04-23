package helpers_test

import (
	"agencia-viajes/internal/helpers"
	"errors"
	"strings"
	"testing"
)

func TestTipoProveedorStr(t *testing.T) {
	tests := []struct {
		tipoID int
		want   string
	}{
		{1, "Aerolinea"},
		{2, "Hotelera"},
		{0, "Proveedor"},
		{99, "Proveedor"},
		{-1, "Proveedor"},
	}

	for _, tt := range tests {
		t.Run(tt.want, func(t *testing.T) {
			got := helpers.TipoProveedorStr(tt.tipoID)
			if got != tt.want {
				t.Errorf("TipoProveedorStr(%d) = %q, want %q", tt.tipoID, got, tt.want)
			}
		})
	}
}

func TestErrorProveedorUsuario(t *testing.T) {
	nombre := "Broom AirLine"
	tipo := "Aerolinea"

	tests := []struct {
		name       string
		errTecnico error
		accion     string
		wantSubstr string
	}{
		{
			name:       "error de red dial tcp",
			errTecnico: errors.New("dial tcp: connection refused"),
			accion:     "reservar vuelo",
			wantSubstr: "no esta disponible",
		},
		{
			name:       "error de red connectex",
			errTecnico: errors.New("connectex: no connection could be made"),
			accion:     "reservar vuelo",
			wantSubstr: "no esta disponible",
		},
		{
			name:       "error de red no such host",
			errTecnico: errors.New("no such host: broom.example.com"),
			accion:     "reservar vuelo",
			wantSubstr: "no esta disponible",
		},
		{
			name:       "timeout deadline exceeded",
			errTecnico: errors.New("context deadline exceeded"),
			accion:     "verificar disponibilidad",
			wantSubstr: "no respondio a tiempo",
		},
		{
			name:       "timeout explicito",
			errTecnico: errors.New("request timeout after 30s"),
			accion:     "verificar disponibilidad",
			wantSubstr: "no respondio a tiempo",
		},
		{
			name:       "error generico desconocido",
			errTecnico: errors.New("unexpected EOF"),
			accion:     "cancelar reserva",
			wantSubstr: "no se pudo completar cancelar reserva",
		},
		{
			name:       "error HTTP nil incluye rechazo",
			errTecnico: nil,
			accion:     "confirmar pago",
			wantSubstr: "rechazo la operacion",
		},
		{
			name:       "error HTTP nil incluye accion",
			errTecnico: nil,
			accion:     "confirmar pago",
			wantSubstr: "confirmar pago",
		},
		{
			name:       "mensaje contiene nombre del proveedor en error red",
			errTecnico: errors.New("dial tcp: refused"),
			accion:     "cualquier cosa",
			wantSubstr: nombre,
		},
		{
			name:       "mensaje contiene tipo en timeout",
			errTecnico: errors.New("timeout"),
			accion:     "cualquier cosa",
			wantSubstr: tipo,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := helpers.ErrorProveedorUsuario(nombre, tipo, tt.errTecnico, tt.accion)
			if err == nil {
				t.Fatal("esperaba un error pero fue nil")
			}
			if !strings.Contains(err.Error(), tt.wantSubstr) {
				t.Errorf("got %q, queria contener %q", err.Error(), tt.wantSubstr)
			}
		})
	}
}
