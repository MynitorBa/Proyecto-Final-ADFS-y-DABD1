package helpers

import "fmt"

// BuildHTMLCancelacionUsuario
//
// Genera el HTML del correo electronico que se envía al usuario cuando
// el mismo cancela una de sus reservaciones desde la plataforma.
// La plantilla muestra el numero de reservacion, el motivo indicado por
// el usuario y una nota sobre el proceso de reembolso.
//
// Parametros:
//   - nombre:        nombre del usuario destinatario
//   - apellido:      apellido del usuario destinatario
//   - noReservacion: numero de reservacion cancelada (ej: "1B037995")
//   - motivoUsuario: texto del motivo que el usuario ingreso al cancelar
//
// Retorna:
//   - string: HTML completo listo para enviarse como cuerpo del correo
func BuildHTMLCancelacionUsuario(nombre, apellido, noReservacion, motivoUsuario string) string {
	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>MOVENT &middot; Cancelacion confirmada</title>
</head>
<body style="margin:0;padding:0;background:#F5F2EC;font-family:Helvetica,Arial,sans-serif;">
<table width="100%%" cellpadding="0" cellspacing="0" style="background:#F5F2EC;min-height:100vh;">
<tr><td align="center" style="padding:32px 12px;">
<table width="600" cellpadding="0" cellspacing="0"
style="max-width:600px;width:100%%;border-radius:12px;overflow:hidden;
box-shadow:0 4px 24px rgba(28,26,24,0.12);border:1px solid #ddd6cc;">

<tr><td style="background:#1C1A18;padding:0;">
<table width="100%%" cellpadding="0" cellspacing="0">
<tr><td style="padding:26px 28px 20px;">
<table width="100%%" cellpadding="0" cellspacing="0"><tr>
<td style="vertical-align:middle;">
<div style="font-size:28px;font-weight:bold;color:#FFCC00;letter-spacing:4px;line-height:1;">MOVENT</div>
<div style="font-size:10px;color:#6b6358;margin-top:5px;">Agencia de Viajes &nbsp;&middot;&nbsp; Guatemala City</div>
</td>
<td style="vertical-align:middle;text-align:right;">
<div style="font-size:9px;color:#6b6358;letter-spacing:2px;text-transform:uppercase;margin-bottom:5px;">Reservacion</div>
<div style="font-size:18px;font-weight:bold;color:#ffffff;letter-spacing:1px;">%s</div>
<div style="margin-top:6px;">
<span style="display:inline-block;padding:3px 12px;border-radius:20px;font-size:10px;
font-weight:bold;color:#fff;background:#C0392B;">Cancelada</span>
</div>
</td>
</tr></table>
</td></tr>
<tr><td style="background:#FFCC00;height:3px;font-size:0;line-height:0;">&nbsp;</td></tr>
</table>
</td></tr>

<tr><td style="background:#F5F2EC;">
<table width="100%%" cellpadding="0" cellspacing="0">

  <tr><td style="padding:28px 28px 16px;">
    <p style="margin:0;font-size:17px;font-weight:bold;color:#1C1A18;">Hola, %s %s</p>
    <h2 style="margin:16px 0 0;font-size:18px;color:#1C1A18;">Cancelacion confirmada</h2>
    <p style="margin:10px 0 0;font-size:13px;color:#5a5047;line-height:1.7;">
      Hemos procesado la cancelacion de tu reservacion <strong>%s</strong> con exito.
    </p>
  </td></tr>

  <tr><td style="padding:0 28px;">
    <table width="100%%" cellpadding="0" cellspacing="0">
      <tr><td style="border-top:1px solid #ddd6cc;"></td></tr>
    </table>
  </td></tr>

  <tr><td style="padding:20px 28px 8px;">
    <table width="100%%" cellpadding="0" cellspacing="0"><tr>
      <td style="border-left:3px solid #FFCC00;padding-left:10px;">
        <div style="font-size:11px;font-weight:bold;color:#1C1A18;letter-spacing:1px;text-transform:uppercase;">
          Motivo de cancelacion
        </div>
      </td>
    </tr></table>
  </td></tr>
  <tr><td style="padding:0 28px 20px;">
    <table width="100%%" cellpadding="0" cellspacing="0"
      style="background:#fff;border:1px solid #ddd6cc;border-radius:8px;
             border-left:4px solid #FFCC00;overflow:hidden;">
      <tr><td style="padding:16px 20px;">
        <div style="font-size:13px;color:#1C1A18;line-height:1.7;font-style:italic;">
          &ldquo;%s&rdquo;
        </div>
      </td></tr>
    </table>
  </td></tr>

  <tr><td style="padding:0 28px 20px;">
    <table width="100%%" cellpadding="0" cellspacing="0"
      style="background:#fff;border:1px solid #ddd6cc;border-radius:8px;
             border-left:4px solid #C0392B;overflow:hidden;">
      <tr><td style="padding:14px 16px;">
        <div style="font-size:12px;font-weight:bold;color:#1C1A18;margin-bottom:3px;">
          &#128221; Proximos pasos
        </div>
        <div style="font-size:12px;color:#5a5047;line-height:1.6;">
          Tu cancelacion fue procesada con exito. Si tenias pagos asociados, se reembolsaran
          segun los terminos y condiciones de Movent y del proveedor.
          Si necesitas mayor informacion sobre tu reembolso, contactanos.
        </div>
      </td></tr>
    </table>
  </td></tr>

  <tr><td style="padding:0 28px 24px;">
    <table width="100%%" cellpadding="0" cellspacing="0"
      style="background:#fff;border:1px solid #ddd6cc;border-radius:8px;
             border-left:4px solid #FFCC00;overflow:hidden;">
      <tr><td style="padding:14px 16px;">
        <div style="font-size:12px;font-weight:bold;color:#1C1A18;margin-bottom:3px;">
          &#128222; Necesitas ayuda?
        </div>
        <div style="font-size:12px;color:#5a5047;line-height:1.6;">
          Comunicate con nosotros a
          <a href="mailto:info@movent.gt" style="color:#4a4035;font-weight:bold;">info@movent.gt</a>
          o al <strong>+502 0000-0000</strong> y con gusto te asistimos.
        </div>
      </td></tr>
    </table>
  </td></tr>

  <tr><td style="padding:0 28px 24px;">
    <table width="100%%" cellpadding="0" cellspacing="0"><tr>
      <td style="font-size:10px;color:#9a9089;line-height:1.7;
                 border-top:1px solid #ddd6cc;padding-top:16px;">
        Esta es una confirmacion automatica de la cancelacion solicitada por ti.
      </td>
    </tr></table>
  </td></tr>

</table>
</td></tr>

<tr><td style="background:#1C1A18;padding:0;">
<div style="background:#FFCC00;height:2px;font-size:0;"></div>
<table width="100%%" cellpadding="0" cellspacing="0"><tr>
<td style="padding:18px 28px;text-align:center;">
<div style="font-size:11px;color:#6b6358;line-height:1.8;">
MOVENT &nbsp;&middot;&nbsp; info@movent.gt &nbsp;&middot;&nbsp; +502 0000-0000 &nbsp;&middot;&nbsp; Guatemala City, Guatemala
</div>
<div style="font-size:10px;color:#3d3935;margin-top:4px;">
Correo automatico &mdash; por favor no respondas a este mensaje.
</div>
</td>
</tr></table>
</td></tr>

</table>
</td></tr>
</table>
</body>
</html>`,
		noReservacion,
		nombre,
		apellido,
		noReservacion,
		motivoUsuario,
	)
}
