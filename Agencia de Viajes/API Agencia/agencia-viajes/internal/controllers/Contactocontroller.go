// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"fmt"
	"strings"

	"github.com/gin-gonic/gin"
)

// contactoRequest
//
// Estructura que representa el body esperado en el endpoint de contacto.
// Los campos nombre, correo y mensaje son obligatorios.
type contactoRequest struct {
	Nombre  string `json:"nombre"  binding:"required"`
	Correo  string `json:"correo"  binding:"required"`
	Asunto  string `json:"asunto"`
	Mensaje string `json:"mensaje" binding:"required"`
}

// EnviarContacto
//
// Procesa el formulario de contacto enviado desde el sitio web. Valida que
// el mensaje tenga al menos 10 caracteres, construye un correo HTML con los
// datos del remitente y lo envia a la bandeja de entrada de soporte de MOVENT.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion al visitante
//   - HTTP 400 Bad Request: si faltan campos requeridos o el mensaje es muy corto
//   - HTTP 500 Internal Server Error: si ocurre un error al enviar el correo
//
// Notas:
//   - Si el asunto llega vacio se usa el valor por defecto "Consulta desde el sitio web"
//   - El correo se envia a la cuenta configurada en el SMTP (cfg.User)
func EnviarContacto(c *gin.Context) {
	var req contactoRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(400, gin.H{"mensaje": "Nombre, correo y mensaje son requeridos."})
		return
	}
	if len(strings.TrimSpace(req.Mensaje)) < 10 {
		c.JSON(400, gin.H{"mensaje": "El mensaje debe tener al menos 10 caracteres."})
		return
	}

	asunto := strings.TrimSpace(req.Asunto)
	if asunto == "" {
		asunto = "Consulta desde el sitio web"
	}

	html := buildHTMLContacto(req.Nombre, req.Correo, asunto, req.Mensaje)
	subject := fmt.Sprintf("[Contacto MOVENT] %s", asunto)

	cfg := helpers.GetSMTPConfig()
	if err := helpers.EnviarEmailHTML(cfg.User, subject, html); err != nil {
		c.JSON(500, gin.H{"mensaje": "Error al enviar el mensaje. Intenta de nuevo más tarde."})
		return
	}

	c.JSON(200, gin.H{"mensaje": "Mensaje enviado correctamente. Te responderemos pronto."})
}

// buildHTMLContacto
//
// Construye el cuerpo HTML del correo de contacto con el estilo visual de
// MOVENT, incluyendo los datos del remitente y el mensaje recibido.
//
// Parametros:
//   - nombre: nombre del visitante que envia el mensaje
//   - correo: direccion de correo del remitente
//   - asunto: asunto del mensaje
//   - mensaje: contenido del mensaje enviado por el visitante
//
// Retorna:
//   - string: cadena HTML lista para ser enviada como cuerpo del correo
func buildHTMLContacto(nombre, correo, asunto, mensaje string) string {
	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>MOVENT</title></head>
<body style="margin:0;padding:0;background:#F5F2EC;font-family:Helvetica,Arial,sans-serif;">
<table width="100%%" cellpadding="0" cellspacing="0" style="background:#F5F2EC;min-height:100vh;">
<tr><td align="center" style="padding:32px 12px;">
<table width="600" cellpadding="0" cellspacing="0"
  style="max-width:600px;width:100%%;border-radius:12px;overflow:hidden;
         box-shadow:0 4px 24px rgba(28,26,24,0.12);border:1px solid #ddd6cc;">
  <tr><td style="background:#1C1A18;padding:26px 28px 20px;">
    <table width="100%%" cellpadding="0" cellspacing="0"><tr>
      <td>
        <div style="font-size:28px;font-weight:bold;color:#FFCC00;letter-spacing:4px;line-height:1;">MOVENT</div>
        <div style="font-size:10px;color:#6b6358;margin-top:5px;">Agencia de Viajes &middot; Guatemala City</div>
      </td>
      <td align="right">
        <div style="font-size:9px;color:#6b6358;letter-spacing:2px;text-transform:uppercase;margin-bottom:6px;">Formulario de</div>
        <div style="font-size:14px;font-weight:bold;color:#FFCC00;letter-spacing:2px;">CONTACTO</div>
      </td>
    </tr></table>
  </td></tr>
  <tr><td style="background:#FFCC00;height:3px;font-size:0;">&nbsp;</td></tr>
  <tr><td style="background:#F5F2EC;">
    <table width="100%%" cellpadding="0" cellspacing="0">
      <tr><td style="padding:28px 28px 16px;">
        <p style="margin:0;font-size:17px;font-weight:bold;color:#1C1A18;">Nuevo mensaje de contacto</p>
        <p style="margin:8px 0 0;font-size:13px;color:#5a5047;line-height:1.7;">
          Un visitante del sitio web envió el siguiente mensaje.
        </p>
      </td></tr>
      <tr><td style="padding:0 28px 20px;">
        <table width="100%%" cellpadding="0" cellspacing="0"
          style="border:1px solid #ddd6cc;border-radius:8px;overflow:hidden;">
          <tr style="background:#1C1A18;">
            <td colspan="2" style="padding:10px 16px;font-size:9px;font-weight:bold;color:#fff;letter-spacing:1.5px;">
              DATOS DEL REMITENTE
            </td>
          </tr>
          <tr style="background:#fff;">
            <td style="padding:10px 16px;font-size:12px;color:#9a9089;width:30%%;border-bottom:1px solid #f0ebe3;">Nombre</td>
            <td style="padding:10px 16px;font-size:13px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;">%s</td>
          </tr>
          <tr style="background:#faf8f5;">
            <td style="padding:10px 16px;font-size:12px;color:#9a9089;border-bottom:1px solid #f0ebe3;">Correo</td>
            <td style="padding:10px 16px;font-size:13px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;">%s</td>
          </tr>
          <tr style="background:#fff;">
            <td style="padding:10px 16px;font-size:12px;color:#9a9089;">Asunto</td>
            <td style="padding:10px 16px;font-size:13px;font-weight:bold;color:#1C1A18;">%s</td>
          </tr>
        </table>
      </td></tr>
      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0">
          <tr><td style="border-left:3px solid #FFCC00;padding-left:10px;margin-bottom:8px;">
            <div style="font-size:11px;font-weight:bold;color:#1C1A18;letter-spacing:1px;text-transform:uppercase;">Mensaje</div>
          </td></tr>
        </table>
        <div style="background:#fff;border:1px solid #ddd6cc;border-radius:8px;padding:16px 20px;
                    font-size:13px;color:#3a3530;line-height:1.7;white-space:pre-wrap;margin-top:8px;">%s</div>
      </td></tr>
      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0"><tr>
          <td style="font-size:10px;color:#9a9089;line-height:1.7;border-top:1px solid #ddd6cc;padding-top:16px;">
            Responde directamente a <strong>%s</strong> para atender esta consulta.
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
          MOVENT &nbsp;&middot;&nbsp; info@movent.gt &nbsp;&middot;&nbsp; +502 5754-5388 &nbsp;&middot;&nbsp; Guatemala City, Guatemala
        </div>
        <div style="font-size:10px;color:#3d3935;margin-top:4px;">Correo automatico generado desde el formulario de contacto.</div>
      </td>
    </tr></table>
  </td></tr>
</table>
</td></tr>
</table>
</body>
</html>`, nombre, correo, asunto, mensaje, correo)
}
