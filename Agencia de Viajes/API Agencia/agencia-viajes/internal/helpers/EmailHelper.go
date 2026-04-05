// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import (
	"bytes"
	"crypto/tls"
	"encoding/base64"
	"fmt"
	"mime/multipart"
	"net"
	"net/smtp"
	"net/textproto"
	"os"
	"strings"
	"time"
)

// ── Config SMTP ────────────────────────────────────────────────────────────

// SMTPConfig
//
// Contiene los parametros de conexion al servidor de correo SMTP.
// Se pobla desde variables de entorno mediante GetSMTPConfig.
type SMTPConfig struct {
	Host     string
	Port     string
	User     string
	Password string
	From     string
}

// GetSMTPConfig
//
// Lee las variables de entorno SMTP y retorna un SMTPConfig listo
// para usar. Si alguna variable no esta definida se usa el valor
// por defecto indicado (host: smtp.gmail.com, puerto: 587).
//
// Retorna:
//   - SMTPConfig: struct con los datos de conexion SMTP
func GetSMTPConfig() SMTPConfig {
	return SMTPConfig{
		Host:     getEnv("SMTP_HOST", "smtp.gmail.com"),
		Port:     getEnv("SMTP_PORT", "587"),
		User:     getEnv("SMTP_USER", ""),
		Password: getEnv("SMTP_PASS", ""),
		From:     getEnv("SMTP_FROM", ""),
	}
}

// ── Envío con PDF adjunto ──────────────────────────────────────────────────

// EnviarEmailConPDF
//
// Construye y envia un correo electronico multipart con cuerpo HTML
// y un archivo PDF adjunto. Ambas partes se codifican en base64.
// La conexion SMTP se establece con STARTTLS y autenticacion PLAIN.
//
// Parametros:
//   - destinatario: direccion de correo del receptor
//   - asunto: linea de asunto del mensaje
//   - htmlBody: contenido HTML del cuerpo del correo
//   - pdfBytes: bytes del archivo PDF a adjuntar
//   - nombreArchivo: nombre con el que se adjunta el PDF
//
// Retorna:
//   - error: error si la configuracion SMTP es incompleta o el envio falla
func EnviarEmailConPDF(destinatario, asunto, htmlBody string, pdfBytes []byte, nombreArchivo string) error {
	cfg := GetSMTPConfig()
	if cfg.User == "" {
		return fmt.Errorf("SMTP_USER no configurado en .env")
	}
	if cfg.Password == "" {
		return fmt.Errorf("SMTP_PASS no configurado en .env")
	}
	from := cfg.From
	if from == "" {
		from = cfg.User
	}

	var buf bytes.Buffer
	writer := multipart.NewWriter(&buf)

	// Parte 1 — HTML (base64)
	htmlH := make(textproto.MIMEHeader)
	htmlH.Set("Content-Type", "text/html; charset=UTF-8")
	htmlH.Set("Content-Transfer-Encoding", "base64")
	htmlPart, err := writer.CreatePart(htmlH)
	if err != nil {
		return fmt.Errorf("error creando parte HTML: %w", err)
	}
	htmlEncoded := base64.StdEncoding.EncodeToString([]byte(htmlBody))
	var htmlFmt strings.Builder
	for i, ch := range htmlEncoded {
		htmlFmt.WriteRune(ch)
		if (i+1)%76 == 0 {
			htmlFmt.WriteString("\r\n")
		}
	}
	if _, err = htmlPart.Write([]byte(htmlFmt.String())); err != nil {
		return fmt.Errorf("error escribiendo HTML: %w", err)
	}

	// Parte 2 — PDF adjunto (base64)
	pdfH := make(textproto.MIMEHeader)
	pdfH.Set("Content-Type", "application/pdf")
	pdfH.Set("Content-Transfer-Encoding", "base64")
	pdfH.Set("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, nombreArchivo))
	pdfPart, err := writer.CreatePart(pdfH)
	if err != nil {
		return fmt.Errorf("error creando parte PDF: %w", err)
	}
	encoded := base64.StdEncoding.EncodeToString(pdfBytes)
	var encodedFmt strings.Builder
	for i, ch := range encoded {
		encodedFmt.WriteRune(ch)
		if (i+1)%76 == 0 {
			encodedFmt.WriteString("\r\n")
		}
	}
	if _, err = pdfPart.Write([]byte(encodedFmt.String())); err != nil {
		return fmt.Errorf("error escribiendo PDF codificado: %w", err)
	}

	writer.Close()

	header := fmt.Sprintf(
		"From: %s\r\nTo: %s\r\nSubject: %s\r\nDate: %s\r\nMIME-Version: 1.0\r\nContent-Type: multipart/mixed; boundary=\"%s\"\r\n\r\n",
		from, destinatario, asunto,
		time.Now().Format(time.RFC1123Z),
		writer.Boundary(),
	)
	msg := []byte(header + buf.String())

	return enviarSMTP(cfg, destinatario, msg)
}

// ── Envío solo HTML (sin adjunto) ─────────────────────────────────────────

// EnviarEmailHTML
//
// Construye y envia un correo electronico con cuerpo HTML puro,
// sin archivos adjuntos. El cuerpo se codifica en base64 antes
// del envio. Se usa para notificaciones simples como la bienvenida.
//
// Parametros:
//   - destinatario: direccion de correo del receptor
//   - asunto: linea de asunto del mensaje
//   - htmlBody: contenido HTML del cuerpo del correo
//
// Retorna:
//   - error: error si la configuracion SMTP es incompleta o el envio falla
func EnviarEmailHTML(destinatario, asunto, htmlBody string) error {
	cfg := GetSMTPConfig()
	if cfg.User == "" {
		return fmt.Errorf("SMTP_USER no configurado en .env")
	}
	if cfg.Password == "" {
		return fmt.Errorf("SMTP_PASS no configurado en .env")
	}
	from := cfg.From
	if from == "" {
		from = cfg.User
	}

	// Encodear HTML en base64
	htmlEncoded := base64.StdEncoding.EncodeToString([]byte(htmlBody))
	var htmlFmt strings.Builder
	for i, ch := range htmlEncoded {
		htmlFmt.WriteRune(ch)
		if (i+1)%76 == 0 {
			htmlFmt.WriteString("\r\n")
		}
	}

	msg := []byte(fmt.Sprintf(
		"From: %s\r\nTo: %s\r\nSubject: %s\r\nDate: %s\r\nMIME-Version: 1.0\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Transfer-Encoding: base64\r\n\r\n%s",
		from, destinatario, asunto,
		time.Now().Format(time.RFC1123Z),
		htmlFmt.String(),
	))

	return enviarSMTP(cfg, destinatario, msg)
}

// ── SMTP compartido ────────────────────────────────────────────────────────

// enviarSMTP
//
// Logica interna compartida de envio SMTP. Abre una conexion TCP al
// servidor, inicia TLS con STARTTLS, autentica con PLAIN y transmite
// el mensaje ya construido. Es usado por EnviarEmailConPDF y EnviarEmailHTML.
//
// Parametros:
//   - cfg: configuracion SMTP con host, puerto, usuario y contrasena
//   - destinatario: direccion de correo del receptor
//   - msg: mensaje completo en formato RFC 5322 listo para enviar
//
// Retorna:
//   - error: error en cualquier etapa de la comunicacion SMTP
func enviarSMTP(cfg SMTPConfig, destinatario string, msg []byte) error {
	addr := net.JoinHostPort(cfg.Host, cfg.Port)

	conn, err := net.DialTimeout("tcp", addr, 10*time.Second)
	if err != nil {
		return fmt.Errorf("error conectando a %s: %w", addr, err)
	}

	client, err := smtp.NewClient(conn, cfg.Host)
	if err != nil {
		return fmt.Errorf("error creando cliente SMTP: %w", err)
	}
	defer client.Close()

	tlsCfg := &tls.Config{ServerName: cfg.Host}
	if err = client.StartTLS(tlsCfg); err != nil {
		return fmt.Errorf("error iniciando TLS: %w", err)
	}

	auth := smtp.PlainAuth("", cfg.User, cfg.Password, cfg.Host)
	if err = client.Auth(auth); err != nil {
		return fmt.Errorf("error de autenticacion SMTP: %w", err)
	}
	if err = client.Mail(cfg.User); err != nil {
		return fmt.Errorf("error en MAIL FROM: %w", err)
	}
	if err = client.Rcpt(destinatario); err != nil {
		return fmt.Errorf("error en RCPT TO: %w", err)
	}
	wc, err := client.Data()
	if err != nil {
		return fmt.Errorf("error abriendo DATA: %w", err)
	}
	if _, err = wc.Write(msg); err != nil {
		return fmt.Errorf("error enviando mensaje: %w", err)
	}
	if err = wc.Close(); err != nil {
		return fmt.Errorf("error cerrando DATA: %w", err)
	}
	return client.Quit()
}

// ── HTML correo de reservación ─────────────────────────────────────────────

// BuildHTMLEmail
//
// Genera el HTML completo del correo de confirmacion de reservacion.
// Incluye tarjetas para boletos de vuelo y habitaciones de hotel
// con todos los detalles de la reservacion en formato de tabla.
//
// Parametros:
//   - data: struct ReservacionPDFData con todos los datos de la reservacion
//
// Retorna:
//   - string: documento HTML completo listo para enviar como cuerpo de correo
func BuildHTMLEmail(data ReservacionPDFData) string {

	var boletoCards strings.Builder
	for i, b := range data.Boletos {
		boletoCards.WriteString(fmt.Sprintf(`
		<tr><td style="padding:0 28px 12px;">
		  <table width="100%%" cellpadding="0" cellspacing="0"
		    style="border:1px solid #ddd6cc;border-radius:8px;overflow:hidden;">
		    <tr>
		      <td style="background:#2c3a28;padding:10px 16px;">
		        <span style="font-size:11px;font-weight:bold;color:#FFCC00;letter-spacing:1px;">BOLETO %d</span>
		        <span style="font-size:10px;color:#9a9089;margin-left:8px;">%s</span>
		      </td>
		    </tr>
		    <tr><td style="background:#F5F2EC;padding:16px 20px;">
		      <table width="100%%" cellpadding="0" cellspacing="0"><tr>
		        <td style="text-align:center;width:80px;">
		          <div style="font-size:26px;font-weight:bold;color:#1C1A18;line-height:1;">%s</div>
		          <div style="font-size:10px;color:#9a9089;margin-top:3px;">%s</div>
		          <div style="font-size:12px;font-weight:bold;color:#4a4035;margin-top:4px;">%s</div>
		        </td>
		        <td style="text-align:center;padding:0 8px;">
		          <div style="font-size:18px;color:#FFCC00;margin-top:-2px;">&#9992;</div>
		          <div style="font-size:10px;color:#9a9089;margin-top:2px;">%s</div>
		        </td>
		        <td style="text-align:center;width:80px;">
		          <div style="font-size:26px;font-weight:bold;color:#1C1A18;line-height:1;">%s</div>
		          <div style="font-size:10px;color:#9a9089;margin-top:3px;">%s</div>
		          <div style="font-size:12px;font-weight:bold;color:#4a4035;margin-top:4px;">%s</div>
		        </td>
		      </tr></table>
		    </td></tr>
		    <tr><td style="background:#fff;padding:0;">
		      <table width="100%%" cellpadding="0" cellspacing="0">
		        <tr>
		          <td style="padding:8px 16px;font-size:11px;color:#9a9089;border-bottom:1px solid #f0ebe3;width:33%%;">Clase</td>
		          <td style="padding:8px 16px;font-size:11px;color:#9a9089;border-bottom:1px solid #f0ebe3;width:33%%;">Asiento</td>
		          <td style="padding:8px 16px;font-size:11px;color:#9a9089;border-bottom:1px solid #f0ebe3;width:33%%;">Precio</td>
		        </tr>
		        <tr>
		          <td style="padding:8px 16px;font-size:13px;font-weight:bold;color:#1C1A18;">%s</td>
		          <td style="padding:8px 16px;font-size:13px;font-weight:bold;color:#1C1A18;">%s</td>
		          <td style="padding:8px 16px;font-size:13px;font-weight:bold;color:#1C1A18;">$ %.2f</td>
		        </tr>
		      </table>
		    </td></tr>
		  </table>
		</td></tr>`,
			i+1, b.NoBoleto,
			b.OrigenCodigo, b.OrigenCiudad, formatHora(b.HoraSalida),
			formatFecha(b.FechaVuelo),
			b.DestinoCodigo, b.DestinoCiudad, formatHora(b.HoraLlegada),
			b.Clase, b.NoAsiento, b.Precio,
		))
	}

	var habCards strings.Builder
	for i, h := range data.Habitaciones {
		noches := calcNoches(h.FechaCheckIn, h.FechaCheckOut)
		habCards.WriteString(fmt.Sprintf(`
		<tr><td style="padding:0 28px 12px;">
		  <table width="100%%" cellpadding="0" cellspacing="0"
		    style="border:1px solid #ddd6cc;border-radius:8px;overflow:hidden;">
		    <tr>
		      <td style="background:#1C1A18;padding:12px 16px;">
		        <table width="100%%" cellpadding="0" cellspacing="0"><tr>
		          <td>
		            <div style="font-size:13px;font-weight:bold;color:#fff;">%s</div>
		            <div style="font-size:10px;color:#FFCC00;margin-top:2px;">Hab. %d &middot; %s &middot; Nro. %s</div>
		          </td>
		          <td align="right">
		            <span style="display:inline-block;padding:3px 10px;border-radius:20px;
		              font-size:10px;font-weight:bold;color:#1C1A18;background:#FFCC00;">%s</span>
		          </td>
		        </tr></table>
		      </td>
		    </tr>
		    <tr><td style="background:#F5F2EC;padding:14px 16px;">
		      <table width="100%%" cellpadding="0" cellspacing="0"><tr>
		        <td style="text-align:center;width:38%%;">
		          <div style="font-size:9px;color:#9a9089;font-weight:bold;letter-spacing:1px;text-transform:uppercase;">Check-in</div>
		          <div style="font-size:15px;font-weight:bold;color:#1C1A18;margin-top:4px;">%s</div>
		        </td>
		        <td style="text-align:center;width:24%%;">
		          <div style="font-size:20px;color:#FFCC00;">&rarr;</div>
		          <div style="font-size:10px;color:#9a9089;">%d noche(s)</div>
		        </td>
		        <td style="text-align:center;width:38%%;">
		          <div style="font-size:9px;color:#9a9089;font-weight:bold;letter-spacing:1px;text-transform:uppercase;">Check-out</div>
		          <div style="font-size:15px;font-weight:bold;color:#1C1A18;margin-top:4px;">%s</div>
		        </td>
		      </tr></table>
		    </td></tr>
		    <tr><td style="background:#fff;padding:0;">
		      <table width="100%%" cellpadding="0" cellspacing="0">
		        <tr>
		          <td style="padding:8px 16px;font-size:11px;color:#9a9089;border-bottom:1px solid #f0ebe3;width:50%%;">Tipo cama</td>
		          <td style="padding:8px 16px;font-size:11px;color:#9a9089;border-bottom:1px solid #f0ebe3;width:50%%;">Huespedes</td>
		        </tr>
		        <tr>
		          <td style="padding:8px 16px;font-size:13px;font-weight:bold;color:#1C1A18;">%s</td>
		          <td style="padding:8px 16px;font-size:13px;font-weight:bold;color:#1C1A18;">%d</td>
		        </tr>
		      </table>
		    </td></tr>
		  </table>
		</td></tr>`,
			h.NombreHotel,
			i+1, h.TipoHabitacion, h.NumeroHabitacion,
			h.Estado,
			formatFecha(h.FechaCheckIn),
			noches,
			formatFecha(h.FechaCheckOut),
			h.TipoCama, h.CantidadPersonas,
		))
	}

	var secBoletos, secHabs string
	if len(data.Boletos) > 0 {
		secBoletos = fmt.Sprintf(`
		<tr><td style="padding:20px 28px 10px;">
		  <table width="100%%" cellpadding="0" cellspacing="0"><tr>
		    <td style="border-left:3px solid #FFCC00;padding-left:10px;">
		      <div style="font-size:11px;font-weight:bold;color:#1C1A18;letter-spacing:1px;text-transform:uppercase;">
		        Boletos de vuelo (%d)
		      </div>
		    </td>
		  </tr></table>
		</td></tr>
		%s`, len(data.Boletos), boletoCards.String())
	}
	if len(data.Habitaciones) > 0 {
		secHabs = fmt.Sprintf(`
		<tr><td style="padding:20px 28px 10px;">
		  <table width="100%%" cellpadding="0" cellspacing="0"><tr>
		    <td style="border-left:3px solid #FFCC00;padding-left:10px;">
		      <div style="font-size:11px;font-weight:bold;color:#1C1A18;letter-spacing:1px;text-transform:uppercase;">
		        Habitaciones (%d)
		      </div>
		    </td>
		  </tr></table>
		</td></tr>
		%s`, len(data.Habitaciones), habCards.String())
	}

	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <title>MOVENT &middot; Confirmacion de reservacion</title>
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
            <div style="font-size:9px;color:#6b6358;letter-spacing:2px;text-transform:uppercase;margin-bottom:5px;">Comprobante</div>
            <div style="font-size:18px;font-weight:bold;color:#ffffff;letter-spacing:1px;">%s</div>
            <div style="margin-top:6px;">
              <span style="display:inline-block;padding:3px 12px;border-radius:20px;font-size:10px;font-weight:bold;color:#1C1A18;background:#FFCC00;">%s</span>
            </div>
          </td>
        </tr></table>
      </td></tr>
      <tr><td style="background:#FFCC00;height:3px;font-size:0;line-height:0;">&nbsp;</td></tr>
      <tr><td style="background:#242220;padding:10px 28px;">
        <span style="font-size:11px;color:#9a9089;letter-spacing:1px;text-transform:uppercase;">%s</span>
        <span style="font-size:11px;color:#4a4035;margin:0 8px;">&middot;</span>
        <span style="font-size:11px;color:#9a9089;">Reservado el %s</span>
      </td></tr>
    </table>
  </td></tr>
  <tr><td style="background:#F5F2EC;">
    <table width="100%%" cellpadding="0" cellspacing="0">
      <tr><td style="padding:28px 28px 0px;">
        <p style="margin:0;font-size:17px;font-weight:bold;color:#1C1A18;">Hola, %s</p>
        <p style="margin:8px 0 0;font-size:13px;color:#5a5047;line-height:1.7;">
          Tu reservacion de <strong>%s</strong> ha sido procesada correctamente.
          Adjunto encontraras el comprobante PDF con todos los detalles.
        </p>
      </td></tr>
      <tr><td style="padding:20px 28px 0px;">
        <table width="100%%" cellpadding="0" cellspacing="0"><tr><td style="border-top:1px solid #ddd6cc;"></td></tr></table>
      </td></tr>
      %s
      %s
      <tr><td style="padding:16px 28px 20px;">
        <table width="100%%" cellpadding="0" cellspacing="0" style="border-radius:8px;overflow:hidden;">
          <tr>
            <td style="background:#1C1A18;padding:14px 20px;width:60%%;">
              <div style="font-size:11px;font-weight:bold;color:#9a9089;letter-spacing:1.5px;text-transform:uppercase;">Total reservacion</div>
              <div style="font-size:10px;color:#4a4035;margin-top:3px;">Incluye impuestos y servicios</div>
            </td>
            <td style="background:#FFCC00;padding:14px 20px;text-align:right;width:40%%;">
              <div style="font-size:22px;font-weight:bold;color:#1C1A18;">$ %.2f</div>
            </td>
          </tr>
        </table>
      </td></tr>
      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0"
          style="background:#fff;border:1px solid #ddd6cc;border-radius:8px;border-left:4px solid #FFCC00;overflow:hidden;">
          <tr><td style="padding:14px 16px;">
            <div style="font-size:12px;font-weight:bold;color:#1C1A18;margin-bottom:3px;">&#128206; Comprobante PDF adjunto</div>
            <div style="font-size:12px;color:#5a5047;line-height:1.6;">
              El archivo PDF contiene todos los detalles de tu reservacion. Descargalo y presentalo si es necesario.
            </div>
          </td></tr>
        </table>
      </td></tr>
      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0"><tr>
          <td style="font-size:10px;color:#9a9089;line-height:1.7;border-top:1px solid #ddd6cc;padding-top:16px;">
            Cancelaciones sujetas a politicas del proveedor. MOVENT actua como intermediario.
            Soporte: <a href="mailto:info@movent.gt" style="color:#4a4035;">info@movent.gt</a>
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
        <div style="font-size:10px;color:#3d3935;margin-top:4px;">Correo automatico &mdash; por favor no respondas a este mensaje.</div>
      </td>
    </tr></table>
  </td></tr>
</table>
</td></tr>
</table>
</body>
</html>`,
		data.NoReservacion, data.EstadoReserva,
		tipoLabel(data.TipoReserva), formatFecha(data.FechaCreacion),
		ifEmpty(data.UsuarioNombre, "viajero"),
		tipoLabel(data.TipoReserva),
		secBoletos, secHabs,
		data.Total,
	)
}

// ── HTML correo de bienvenida ──────────────────────────────────────────────

// BuildHTMLBienvenida
//
// Genera el HTML completo del correo de bienvenida que se envia al
// usuario tras completar el registro. Incluye una tabla con los
// datos de la cuenta recien creada.
//
// Parametros:
//   - nombre: nombre del usuario registrado
//   - apellido: apellido del usuario registrado
//   - username: nombre de usuario elegido
//   - correo: direccion de correo electronico registrada
//   - telefono: numero de telefono del usuario
//   - fechaNacimiento: fecha de nacimiento en formato string
//   - ciudad: nombre de la ciudad del usuario
//   - pais: nombre del pais del usuario
//   - nacionalidades: slice con los nombres de las nacionalidades del usuario
//
// Retorna:
//   - string: documento HTML completo listo para enviar como cuerpo de correo
func BuildHTMLBienvenida(nombre, apellido, username, correo, telefono, fechaNacimiento, ciudad, pais string, nacionalidades []string) string {
	nacStr := strings.Join(nacionalidades, ", ")
	if nacStr == "" {
		nacStr = "-"
	}

	filaHTML := func(label, valor, bg string) string {
		return fmt.Sprintf(`
        <tr style="background:%s;">
          <td style="padding:10px 16px;font-size:13px;color:#5a5047;border-bottom:1px solid #ede8e0;width:40%%;">%s</td>
          <td style="padding:10px 16px;font-size:13px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #ede8e0;">%s</td>
        </tr>`, bg, label, valor)
	}

	filas := filaHTML("Nombre completo", nombre+" "+apellido, "#fff") +
		filaHTML("Usuario", username, "#faf8f5") +
		filaHTML("Correo", correo, "#fff") +
		filaHTML("Telefono", ifEmpty(telefono, "-"), "#faf8f5") +
		filaHTML("Fecha de nacimiento", ifEmpty(fechaNacimiento, "-"), "#fff") +
		filaHTML("Ciudad", ifEmpty(ciudad, "-"), "#faf8f5") +
		filaHTML("Pais", ifEmpty(pais, "-"), "#fff") +
		filaHTML("Nacionalidad(es)", nacStr, "#faf8f5")

	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>MOVENT</title></head>
<body style="margin:0;padding:0;background:#F5F2EC;font-family:Helvetica,Arial,sans-serif;">
<table width="100%%" cellpadding="0" cellspacing="0" style="background:#F5F2EC;min-height:100vh;">
<tr><td align="center" style="padding:32px 12px;">
<table width="600" cellpadding="0" cellspacing="0"
  style="max-width:600px;width:100%%;border-radius:12px;overflow:hidden;
         box-shadow:0 4px 24px rgba(28,26,24,0.12);border:1px solid #ddd6cc;">

  <!-- Header -->
  <tr><td style="background:#1C1A18;padding:26px 28px 20px;">
    <table width="100%%" cellpadding="0" cellspacing="0"><tr>
      <td>
        <div style="font-size:28px;font-weight:bold;color:#FFCC00;letter-spacing:4px;line-height:1;">MOVENT</div>
        <div style="font-size:10px;color:#6b6358;margin-top:5px;">Agencia de Viajes &middot; Guatemala City</div>
      </td>
      <td align="right">
        <div style="font-size:9px;color:#6b6358;letter-spacing:2px;text-transform:uppercase;margin-bottom:6px;">Bienvenido a</div>
        <div style="font-size:14px;font-weight:bold;color:#FFCC00;letter-spacing:3px;">MOVENT</div>
      </td>
    </tr></table>
  </td></tr>
  <tr><td style="background:#FFCC00;height:3px;font-size:0;">&nbsp;</td></tr>

  <!-- Cuerpo -->
  <tr><td style="background:#F5F2EC;">
    <table width="100%%" cellpadding="0" cellspacing="0">

      <!-- Saludo -->
      <tr><td style="padding:28px 28px 16px;">
        <p style="margin:0;font-size:18px;font-weight:bold;color:#1C1A18;">
          Hola, %s %s
        </p>
        <p style="margin:10px 0 0;font-size:13px;color:#5a5047;line-height:1.7;">
          Tu cuenta en MOVENT ha sido creada exitosamente.
          A continuacion encontraras un resumen de los datos con los que te registraste.
        </p>
      </td></tr>

      <!-- Sección datos -->
      <tr><td style="padding:0 28px 8px;">
        <table width="100%%" cellpadding="0" cellspacing="0">
          <tr><td style="border-left:3px solid #FFCC00;padding-left:10px;margin-bottom:8px;">
            <div style="font-size:11px;font-weight:bold;color:#1C1A18;letter-spacing:1px;text-transform:uppercase;">
              Datos de tu cuenta
            </div>
          </td></tr>
        </table>
      </td></tr>

      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0"
          style="border:1px solid #ddd6cc;border-radius:8px;overflow:hidden;">
          <tr style="background:#1C1A18;">
            <td colspan="2" style="padding:10px 16px;font-size:9px;font-weight:bold;color:#fff;letter-spacing:1.5px;">
              INFORMACION DE REGISTRO
            </td>
          </tr>
          %s
        </table>
      </td></tr>

      <!-- Nota seguridad -->
      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0"
          style="background:#fff;border:1px solid #ddd6cc;border-radius:8px;border-left:4px solid #FFCC00;overflow:hidden;">
          <tr><td style="padding:14px 16px;">
            <div style="font-size:12px;font-weight:bold;color:#1C1A18;margin-bottom:4px;">
              &#128274; Tu contrasena esta protegida
            </div>
            <div style="font-size:12px;color:#5a5047;line-height:1.6;">
              Por seguridad, tu contrasena no se incluye en este correo.
              Si no reconoces este registro, contactanos de inmediato.
            </div>
          </td></tr>
        </table>
      </td></tr>

      <tr><td style="padding:0 28px 24px;">
        <table width="100%%" cellpadding="0" cellspacing="0"><tr>
          <td style="font-size:10px;color:#9a9089;line-height:1.7;border-top:1px solid #ddd6cc;padding-top:16px;">
            Si no creaste esta cuenta, contactanos: <a href="mailto:info@movent.gt" style="color:#4a4035;">info@movent.gt</a>
          </td>
        </tr></table>
      </td></tr>

    </table>
  </td></tr>

  <!-- Footer -->
  <tr><td style="background:#1C1A18;padding:0;">
    <div style="background:#FFCC00;height:2px;font-size:0;"></div>
    <table width="100%%" cellpadding="0" cellspacing="0"><tr>
      <td style="padding:18px 28px;text-align:center;">
        <div style="font-size:11px;color:#6b6358;line-height:1.8;">
          MOVENT &nbsp;&middot;&nbsp; info@movent.gt &nbsp;&middot;&nbsp; +502 0000-0000 &nbsp;&middot;&nbsp; Guatemala City, Guatemala
        </div>
        <div style="font-size:10px;color:#3d3935;margin-top:4px;">Correo automatico &mdash; por favor no respondas a este mensaje.</div>
      </td>
    </tr></table>
  </td></tr>

</table>
</td></tr>
</table>
</body>
</html>`,
		nombre, apellido,
		filas,
	)
}

// EnviarBienvenida
//
// Construye el correo HTML de bienvenida con los datos del usuario
// recien registrado y lo envia a su direccion de correo electronico.
//
// Parametros:
//   - correo: direccion de correo del nuevo usuario
//   - nombre: nombre del usuario
//   - apellido: apellido del usuario
//   - username: nombre de usuario elegido
//   - telefono: numero de telefono del usuario
//   - fechaNacimiento: fecha de nacimiento en formato string
//   - ciudad: nombre de la ciudad del usuario
//   - pais: nombre del pais del usuario
//   - nacionalidades: slice con los nombres de las nacionalidades del usuario
//
// Retorna:
//   - error: error si la construccion del HTML o el envio SMTP falla
func EnviarBienvenida(correo, nombre, apellido, username, telefono, fechaNacimiento, ciudad, pais string, nacionalidades []string) error {
	html  := BuildHTMLBienvenida(nombre, apellido, username, correo, telefono, fechaNacimiento, ciudad, pais, nacionalidades)
	asunto := "Bienvenido a MOVENT, " + nombre
	return EnviarEmailHTML(correo, asunto, html)
}

// ── Utilidades ─────────────────────────────────────────────────────────────

// getEnv retorna el valor de la variable de entorno key o fallback si no existe.
func getEnv(key, fallback string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return fallback
}

// ifEmpty retorna fallback si la cadena s esta vacia o solo tiene espacios.
func ifEmpty(s, fallback string) string {
	if strings.TrimSpace(s) == "" {
		return fallback
	}
	return s
}

// tipoLabel convierte el identificador numerico de tipo de reservacion
// en su etiqueta textual: 1=Vuelo, 2=Hospedaje, 3=Paquete.
func tipoLabel(t int) string {
	switch t {
	case 1:
		return "Vuelo"
	case 2:
		return "Hospedaje"
	case 3:
		return "Paquete"
	default:
		return "-"
	}
}

// formatFecha convierte una cadena de fecha en formato "2006-01-02"
// a una representacion legible como "02 ene 2006".
func formatFecha(f string) string {
	if len(f) < 10 {
		return "-"
	}
	t, err := time.Parse("2006-01-02", f[:10])
	if err != nil {
		return f[:10]
	}
	meses := []string{"", "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"}
	return fmt.Sprintf("%02d %s %d", t.Day(), meses[t.Month()], t.Year())
}

// formatHora retorna los primeros 5 caracteres de una cadena de hora
// en formato "HH:MM" o "-" si la cadena es demasiado corta.
func formatHora(h string) string {
	if len(h) >= 5 {
		return h[:5]
	}
	return "-"
}

// calcNoches calcula la diferencia en noches entre dos fechas
// en formato "2006-01-02". Retorna 0 si alguna fecha es invalida
// o si la diferencia es negativa.
func calcNoches(ci, co string) int {
	if len(ci) < 10 || len(co) < 10 {
		return 0
	}
	t1, e1 := time.Parse("2006-01-02", ci[:10])
	t2, e2 := time.Parse("2006-01-02", co[:10])
	if e1 != nil || e2 != nil {
		return 0
	}
	d := int(t2.Sub(t1).Hours() / 24)
	if d < 0 {
		return 0
	}
	return d
}
