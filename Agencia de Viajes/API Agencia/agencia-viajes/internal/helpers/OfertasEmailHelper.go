// # Package helpers
//
// Helper para generar el correo HTML de ofertas de paquetes de viaje.
// Se usa desde OfertasService cada 5 dias.
package helpers

import "fmt"

// OfertaItem representa un destino con precio base, descuento y precio final.
type OfertaItem struct {
	Destino     string
	Tipo        string
	PrecioBase  float64
	Descuento   float64
	PrecioFinal float64
}

// BuildHTMLOfertas
//
// Construye el cuerpo HTML del correo de ofertas para el usuario indicado.
// Genera una tarjeta por cada oferta mostrando precio original, ahorro y precio final.
//
// Parametros:
//   - nombreUsuario: nombre del destinatario para personalizar el saludo
//   - ofertas: lista de ofertas a incluir en el correo
//
// Retorna:
//   - string: HTML completo del correo
func BuildHTMLOfertas(nombreUsuario string, ofertas []OfertaItem) string {
	tarjetas := ""
	for _, o := range ofertas {
		ahorro := o.PrecioBase - o.PrecioFinal
		iconos := map[string]string{
			"Vuelo":   "✈️",
			"Hotel":   "🏨",
			"Paquete": "🎒",
		}
		icono := iconos[o.Tipo]
		if icono == "" {
			icono = "🌍"
		}
		tarjetas += fmt.Sprintf(`
		<div style="background:#1c1612;border:1px solid #2e2620;border-radius:12px;padding:20px;margin-bottom:16px;">
			<div style="display:flex;align-items:center;gap:10px;margin-bottom:12px;">
				<span style="font-size:28px;">%s</span>
				<div>
					<p style="margin:0;font-size:11px;color:#a09080;text-transform:uppercase;letter-spacing:1px;">%s</p>
					<p style="margin:0;font-size:18px;font-weight:700;color:#f0e8dc;">%s</p>
				</div>
				<div style="margin-left:auto;background:#FFCC00;color:#1a1410;border-radius:20px;padding:4px 12px;font-size:12px;font-weight:700;">
					-%d%% OFF
				</div>
			</div>
			<div style="display:flex;align-items:flex-end;gap:12px;">
				<div>
					<p style="margin:0;font-size:11px;color:#a09080;">Precio regular</p>
					<p style="margin:0;font-size:14px;color:#6a6058;text-decoration:line-through;">$%.2f</p>
				</div>
				<div>
					<p style="margin:0;font-size:11px;color:#FFCC00;">Precio especial</p>
					<p style="margin:0;font-size:22px;font-weight:700;color:#FFCC00;">$%.2f</p>
				</div>
				<div style="margin-left:auto;">
					<p style="margin:0;font-size:11px;color:#4ade80;">Ahorras $%.2f</p>
				</div>
			</div>
		</div>`, icono, o.Tipo, o.Destino, int(o.Descuento), o.PrecioBase, o.PrecioFinal, ahorro)
	}

	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
<body style="margin:0;padding:0;background:#0f0c0a;font-family:'Segoe UI',Arial,sans-serif;">
  <div style="max-width:600px;margin:0 auto;padding:20px;">

    <!-- Header -->
    <div style="text-align:center;padding:30px 20px;background:linear-gradient(135deg,#1a1410,#2a1f15);border-radius:16px 16px 0 0;border:1px solid #2e2620;border-bottom:none;">
      <p style="margin:0 0 6px;font-size:28px;font-weight:800;color:#FFCC00;letter-spacing:-0.5px;">MOVENT</p>
      <p style="margin:0;font-size:13px;color:#a09080;letter-spacing:2px;text-transform:uppercase;">Ofertas Especiales</p>
    </div>

    <!-- Body -->
    <div style="background:#150f0b;border:1px solid #2e2620;border-top:none;border-bottom:none;padding:28px 24px;">

      <p style="font-size:22px;font-weight:700;color:#f0e8dc;margin:0 0 6px;">
        ¡Hola, %s! 👋
      </p>
      <p style="font-size:14px;color:#a09080;margin:0 0 24px;line-height:1.6;">
        Tenemos ofertas exclusivas seleccionadas para ti. Estos destinos están disponibles
        por tiempo limitado con descuentos especiales.
      </p>

      <!-- Ofertas -->
      %s

      <!-- CTA -->
      <div style="text-align:center;margin-top:28px;">
        <a href="#" style="display:inline-block;background:#FFCC00;color:#1a1410;font-weight:700;font-size:14px;padding:14px 32px;border-radius:8px;text-decoration:none;letter-spacing:0.5px;">
          Ver todas las ofertas →
        </a>
      </div>

      <p style="font-size:12px;color:#4a4035;margin:24px 0 0;line-height:1.6;">
        Los precios mostrados son estimados basados en reservaciones recientes y pueden variar
        al momento de completar tu reserva. Descuentos válidos por tiempo limitado.
      </p>
    </div>

    <!-- Footer -->
    <div style="background:#1a1410;border:1px solid #2e2620;border-top:none;border-radius:0 0 16px 16px;padding:20px 24px;text-align:center;">
      <p style="margin:0 0 8px;font-size:12px;color:#6a6058;">
        Recibiste este correo porque estás suscrito a las ofertas de Movent.
      </p>
      <p style="margin:0;font-size:12px;color:#4a4035;">
        Para cancelar tu suscripción, ve a tu perfil y desactiva "Recibir Ofertas".
      </p>
    </div>

  </div>
</body>
</html>`, nombreUsuario, tarjetas)
}
