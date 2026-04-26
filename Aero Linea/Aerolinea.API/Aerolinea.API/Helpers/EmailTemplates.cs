using Aerolinea.API.DTOs;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Clase estatica que centraliza todas las plantillas HTML de correos electronicos
    /// enviados por la aplicacion. Cada metodo genera y retorna el HTML completo
    /// listo para ser usado como cuerpo del mensaje en EmailHelper.
    /// </summary>
    public static class EmailTemplates
    {
        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE BIENVENIDA — enviado al usuario al crear su cuenta
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de bienvenida que se envia al usuario
        /// inmediatamente despues de crear su cuenta en el sistema.
        /// Incluye los datos personales registrados, credenciales de acceso
        /// y una nota de seguridad sobre el manejo de la contrasena.
        /// </summary>
        public static string CorreoBienvenida(
            string nombre,
            string apellido,
            string username,
            string correo,
            string pasaporte,
            string telefono,
            string pais,
            string ciudad,
            string fechaNacimiento,
            List<string> nacionalidades)
        {
            var e = EmailHelper.Esc;

            string filasNacionalidades = (nacionalidades != null && nacionalidades.Count > 0)
                ? string.Join(", ", nacionalidades.Select(n => e(n)))
                : "—";

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <!-- HEADER -->
    <div style='background:#1C1A18;padding:32px 20px;text-align:center;'>
        <h1 style='margin:0;font-size:26px;color:#F2EFEA;font-weight:800;letter-spacing:1.5px;'>BROOM AIRLINE</h1>
        <p style='margin:10px 0 0;font-size:14px;color:#B89A7A;'>Bienvenido a bordo</p>
    </div>
    <div style='height:3px;background:#8B6B4A;'></div>

    <!-- SALUDO -->
    <div style='padding:30px 24px 10px;text-align:center;background:#ffffff;'>
        <div style='width:72px;height:72px;margin:0 auto 16px;background:#8B6B4A;border-radius:50%;text-align:center;line-height:72px;'>
            <span style='font-size:32px;color:#F2EFEA;font-weight:700;'>{e(nombre.Length > 0 ? nombre.Substring(0, 1).ToUpper() : "?")}</span>
        </div>
        <h2 style='margin:0 0 6px;font-size:22px;color:#1C1A18;font-weight:700;'>¡Hola {e(nombre)}!</h2>
        <p style='margin:0;font-size:14px;color:#3A3531;line-height:1.6;'>
            Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesion y comenzar a reservar tus vuelos.
        </p>
    </div>

    <!-- DATOS DE LA CUENTA -->
    <div style='padding:20px 24px 28px;background:#ffffff;'>

        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.25);border-radius:4px 16px 4px 16px;padding:20px;border-left:4px solid #8B6B4A;margin-bottom:16px;'>
            <h3 style='margin:0 0 14px;font-size:12px;color:#8B6B4A;font-weight:700;text-transform:uppercase;letter-spacing:1.2px;'>Datos de tu Cuenta</h3>
            <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;width:120px;vertical-align:top;'>Nombre completo</td>
                    <td style='padding:7px 0;font-size:14px;color:#1C1A18;font-weight:600;word-break:break-word;'>{e(nombre)} {e(apellido)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Username</td>
                    <td style='padding:7px 0;font-size:14px;color:#1C1A18;font-weight:700;word-break:break-word;'>{e(username)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Correo</td>
                    <td style='padding:7px 0;font-size:14px;color:#8B6B4A;font-weight:600;word-break:break-all;'>{e(correo)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Telefono</td>
                    <td style='padding:7px 0;font-size:14px;color:#3A3531;word-break:break-word;'>{e(telefono)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Pasaporte</td>
                    <td style='padding:7px 0;font-size:14px;color:#3A3531;word-break:break-word;'>{e(pasaporte)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Fecha nacimiento</td>
                    <td style='padding:7px 0;font-size:14px;color:#3A3531;'>{e(fechaNacimiento)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Ubicacion</td>
                    <td style='padding:7px 0;font-size:14px;color:#3A3531;word-break:break-word;'>{e(ciudad)}, {e(pais)}</td>
                </tr>
                <tr>
                    <td style='padding:7px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Nacionalidad(es)</td>
                    <td style='padding:7px 0;font-size:14px;color:#3A3531;word-break:break-word;'>{filasNacionalidades}</td>
                </tr>
            </table>
        </div>

        <!-- SEGURIDAD -->
        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.2);border-radius:4px 16px 4px 16px;padding:16px 20px;border-left:4px solid #B89A7A;margin-bottom:20px;'>
            <h4 style='margin:0 0 8px;font-size:11px;color:#8B6B4A;font-weight:700;text-transform:uppercase;letter-spacing:1px;'>Seguridad</h4>
            <p style='margin:0;font-size:12px;color:#3A3531;line-height:1.7;'>
                Tu contrasena ha sido almacenada de forma segura y encriptada. Por tu seguridad, nunca compartiremos tu contrasena por correo. Si olvidas tu contrasena, puedes restablecerla desde la pagina de inicio de sesion.
            </p>
        </div>

        <!-- CTA -->
        <div style='text-align:center;'>
            <a href='http://localhost:5173/login' style='display:inline-block;padding:14px 36px;background:#8B6B4A;color:#F2EFEA;text-decoration:none;border-radius:0 16px 0 16px;font-size:14px;font-weight:700;letter-spacing:0.5px;'>Iniciar Sesion →</a>
        </div>

    </div>

    <!-- FOOTER -->
    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 20px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine · distribuidorapine@gmail.com · Guatemala City, Guatemala</p>
        <p style='margin:4px 0 0;font-size:10px;color:#3A3531;'>Correo generado automaticamente — No responder</p>
    </div>

</div>
</div>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE RESERVACIÓN — enviado al usuario con detalle de boletos
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de detalle de reservacion que se envia al usuario
        /// con el comprobante completo. Incluye la tabla de boletos, datos de pasajeros
        /// si los hay, el total de la reservacion y los terminos y condiciones del viaje.
        /// </summary>
        public static string CorreoReservacion(ReservacionDetalleDTO reservacion)
        {
            var e = EmailHelper.Esc;

            string filasBoletos = "";
            int idx = 1;
            foreach (var boleto in reservacion.Boletos)
            {
                string bgColor = idx % 2 == 0 ? "#F2EFEA" : "#ffffff";
                filasBoletos += $@"
                <tr style='background-color:{bgColor};'>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;text-align:center;border-bottom:1px solid rgba(139,107,74,0.15);'>{idx}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(boleto.NumeroVuelo)}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;text-align:center;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(boleto.NoAsiento)}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(boleto.OrigenCodigo)} → {e(boleto.DestinoCodigo)}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(boleto.Clase)}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;text-align:center;border-bottom:1px solid rgba(139,107,74,0.15);'>{boleto.FechaVuelo:yyyy-MM-dd}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;text-align:center;border-bottom:1px solid rgba(139,107,74,0.15);'>{boleto.HoraSalida.Hours:D2}:{boleto.HoraSalida.Minutes:D2}</td>
                    <td style='padding:12px 10px;font-size:13px;color:#3A3531;text-align:right;font-weight:700;border-bottom:1px solid rgba(139,107,74,0.15);'>Q {boleto.Precio:N2}</td>
                </tr>";
                idx++;
            }

            string seccionPasajeros = "";
            var boletosConPasajero = reservacion.Boletos.Where(b => b.Pasajero != null).ToList();
            if (boletosConPasajero.Any())
            {
                string filasPasajeros = "";
                int pIdx = 1;
                foreach (var b in boletosConPasajero)
                {
                    var p = b.Pasajero;
                    string bgP = pIdx % 2 == 0 ? "#F2EFEA" : "#ffffff";
                    filasPasajeros += $@"
                    <tr style='background-color:{bgP};'>
                        <td style='padding:10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(p.Nombre)} {e(p.Apellido)}</td>
                        <td style='padding:10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(p.Pasaporte)}</td>
                        <td style='padding:10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(b.NoAsiento)}</td>
                        <td style='padding:10px;font-size:13px;color:#3A3531;border-bottom:1px solid rgba(139,107,74,0.15);'>{e(b.NumeroVuelo)}</td>
                    </tr>";
                    pIdx++;
                }

                seccionPasajeros = $@"
                <div style='margin-top:24px;'>
                    <div style='background:#1C1A18;padding:14px 20px;border-radius:4px 16px 0 0;'>
                        <h3 style='margin:0;font-size:13px;color:#F2EFEA;font-weight:700;text-transform:uppercase;letter-spacing:1.2px;'>Datos de Pasajeros</h3>
                    </div>
                    <table style='width:100%;border-collapse:collapse;'>
                        <tr style='background:#3A3531;'>
                            <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Pasajero</th>
                            <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Pasaporte</th>
                            <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Asiento</th>
                            <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Vuelo</th>
                        </tr>
                        {filasPasajeros}
                    </table>
                </div>";
            }

            string estadoColor = reservacion.EstadoReserva?.ToLower() switch
            {
                "confirmada" => "#2C5F2D",
                "cancelada" => "#ef4444",
                "pendiente" => "#B89A7A",
                "expirada" => "#6b7280",
                _ => "#8B6B4A"
            };

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:700px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <!-- HEADER -->
    <div style='background:#1C1A18;padding:28px 20px;text-align:center;'>
        <h1 style='margin:0;font-size:26px;color:#F2EFEA;font-weight:800;letter-spacing:1.5px;'>BROOM AIRLINE</h1>
        <p style='margin:8px 0 0;font-size:13px;color:#B89A7A;'>Comprobante de Reservacion</p>
    </div>
    <div style='height:3px;background:#8B6B4A;'></div>

    <!-- CUERPO -->
    <div style='padding:24px 20px;background:#ffffff;'>

        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.25);border-radius:4px 16px 4px 16px;padding:20px;margin-bottom:20px;border-left:4px solid #8B6B4A;'>
            <table style='width:100%;border-collapse:collapse;'>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;width:140px;'>Nro. Reservacion</td>
                    <td style='padding:8px 0;font-size:15px;color:#1C1A18;font-weight:700;'>{e(reservacion.NoReservacion)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;'>Estado</td>
                    <td style='padding:8px 0;font-size:14px;color:{estadoColor};font-weight:700;'>● {e(reservacion.EstadoReserva?.ToUpper() ?? "—")}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;'>Pasajero</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;font-weight:600;'>{e(reservacion.UsuarioNombre)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;'>Email</td>
                    <td style='padding:8px 0;font-size:14px;color:#8B6B4A;font-weight:600;'>{e(reservacion.UsuarioEmail)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;'>Fecha de Emision</td>
                    <td style='padding:8px 0;font-size:14px;color:#3A3531;'>{reservacion.FechaCreacion:yyyy-MM-dd HH:mm}</td>
                </tr>
            </table>
        </div>

        <div style='border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);'>
            <div style='background:#1C1A18;padding:14px 20px;'>
                <h3 style='margin:0;font-size:13px;color:#F2EFEA;font-weight:700;text-transform:uppercase;letter-spacing:1.2px;'>Detalle de Boletos</h3>
            </div>
            <table style='width:100%;border-collapse:collapse;'>
                <tr style='background:#3A3531;'>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:center;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>#</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Vuelo</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:center;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Asiento</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Ruta</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:left;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Clase</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:center;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Fecha</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:center;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Salida</th>
                    <th style='padding:12px 10px;font-size:11px;color:#F2EFEA;text-align:right;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;'>Precio</th>
                </tr>
                {filasBoletos}
            </table>

            <div style='background:#8B6B4A;padding:16px 20px;'>
                <table style='width:100%;'>
                    <tr>
                        <td style='font-size:15px;color:#F2EFEA;font-weight:700;'>TOTAL RESERVACION</td>
                        <td style='font-size:18px;color:#F2EFEA;font-weight:800;text-align:right;'>Q {reservacion.Total:N2}</td>
                    </tr>
                </table>
            </div>
        </div>

        {seccionPasajeros}

        <div style='margin-top:24px;background:#F2EFEA;border:1px solid rgba(139,107,74,0.2);border-radius:4px 16px 4px 16px;padding:16px 20px;border-left:4px solid #B89A7A;'>
            <h4 style='margin:0 0 8px;font-size:11px;color:#8B6B4A;font-weight:700;text-transform:uppercase;letter-spacing:1px;'>Terminos y Condiciones</h4>
            <p style='margin:0;font-size:11px;color:#3A3531;line-height:1.8;'>
                1. Este comprobante es valido unicamente para los vuelos indicados.<br>
                2. Presentar pasaporte vigente al momento del check-in.<br>
                3. Abordaje cierra 30 minutos antes de la hora de salida.<br>
                4. Cancelaciones estan sujetas a la politica de Broom AirLine.<br>
                5. Este documento es comprobante oficial de reservacion.
            </p>
        </div>

    </div>

    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 20px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine · distribuidorapine@gmail.com · Guatemala City, Guatemala</p>
        <p style='margin:4px 0 0;font-size:10px;color:#3A3531;'>Correo generado automaticamente — No responder</p>
    </div>

</div>
</div>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE CONTACTO — enviado al admin
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de notificacion que se envia al administrador
        /// cuando un usuario envia un mensaje a traves del formulario de contacto.
        /// Incluye el nombre, correo, asunto y cuerpo del mensaje del remitente,
        /// junto con un boton para responder directamente.
        /// </summary>
        public static string CorreoContacto(string nombre, string correo, string asunto, string mensaje)
        {
            var e = EmailHelper.Esc;
            string asuntoMostrar = string.IsNullOrWhiteSpace(asunto)
                ? "<em style='color:#8B6B4A;'>Sin asunto</em>"
                : e(asunto);

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <div style='background:#1C1A18;padding:24px 16px;text-align:center;'>
        <h1 style='margin:0;font-size:20px;color:#F2EFEA;font-weight:700;'>Nuevo Mensaje de Contacto</h1>
        <p style='margin:6px 0 0;font-size:13px;color:#B89A7A;'>Recibido desde el formulario de Broom AirLine</p>
    </div>
    <div style='height:3px;background:#8B6B4A;'></div>

    <div style='padding:20px 16px;background:#ffffff;'>
        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.25);border-radius:4px 16px 4px 16px;padding:16px;margin-bottom:16px;border-left:4px solid #8B6B4A;'>
            <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;width:80px;vertical-align:top;'>Nombre</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;font-weight:600;word-break:break-word;'>{e(nombre)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Correo</td>
                    <td style='padding:8px 0;font-size:14px;font-weight:600;word-break:break-all;'><a href='mailto:{e(correo)}' style='color:#8B6B4A;text-decoration:none;'>{e(correo)}</a></td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Asunto</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;word-break:break-word;'>{asuntoMostrar}</td>
                </tr>
            </table>
        </div>

        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.2);border-radius:4px 16px 4px 16px;padding:16px;border-left:4px solid #B89A7A;'>
            <h3 style='margin:0 0 10px;font-size:13px;color:#8B6B4A;font-weight:700;text-transform:uppercase;letter-spacing:1px;'>Mensaje</h3>
            <p style='margin:0;font-size:14px;color:#3A3531;line-height:1.7;white-space:pre-wrap;word-break:break-word;'>{e(mensaje)}</p>
        </div>

        <div style='text-align:center;margin-top:16px;'>
            <a href='mailto:{e(correo)}' style='display:inline-block;padding:12px 24px;background:#8B6B4A;color:#F2EFEA;text-decoration:none;border-radius:0 16px 0 16px;font-size:14px;font-weight:700;'>Responder a {e(nombre)} →</a>
        </div>
    </div>

    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:12px 16px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Correo generado automaticamente por Broom AirLine.</p>
    </div>

</div>
</div>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE NEWSLETTER — enviado al admin
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de notificacion al administrador cuando
        /// un nuevo usuario se suscribe al boletin informativo de Broom AirLine.
        /// Muestra el correo del suscriptor y un boton para contactarlo directamente.
        /// </summary>
        public static string CorreoNewsletter(string correo)
        {
            var e = EmailHelper.Esc;

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:40px 20px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <div style='background:#1C1A18;padding:30px;text-align:center;'>
        <h1 style='margin:0;font-size:22px;color:#F2EFEA;font-weight:700;'>Nueva Suscripcion al Boletin</h1>
        <p style='margin:8px 0 0;font-size:13px;color:#B89A7A;'>Broom AirLine</p>
    </div>
    <div style='height:3px;background:#8B6B4A;'></div>

    <div style='padding:30px;text-align:center;background:#ffffff;'>
        <p style='font-size:15px;color:#3A3531;margin:0 0 20px;'>Un nuevo usuario se ha suscrito al boletin de Broom AirLine:</p>
        <div style='display:inline-block;background:#F2EFEA;border:1px solid rgba(139,107,74,0.3);border-radius:4px 16px 4px 16px;padding:16px 32px;border-left:4px solid #8B6B4A;'>
            <p style='margin:0;font-size:18px;color:#8B6B4A;font-weight:700;'>{e(correo)}</p>
        </div>
        <div style='margin-top:20px;'>
            <a href='mailto:{e(correo)}' style='display:inline-block;padding:10px 28px;background:#8B6B4A;color:#F2EFEA;text-decoration:none;border-radius:0 16px 0 16px;font-size:13px;font-weight:600;'>Enviar correo →</a>
        </div>
    </div>

    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 30px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Notificacion automatica de Broom AirLine.</p>
    </div>

</div>
</div>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE CONFIRMACIÓN — enviado al usuario tras confirmar
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML premium del correo de confirmacion con detalle completo de boletos.
        /// Acepta la reservacion completa para incluir vuelos, asientos y clase.
        /// </summary>
        public static string CorreoConfirmacion(ReservacionDetalleDTO reservacion)
        {
            var e = EmailHelper.Esc;
            string nombreUsuario = reservacion.UsuarioNombre ?? "";
            string noReservacion = reservacion.NoReservacion ?? "";
            decimal total        = reservacion.Total;

            // Filas de vuelos — agrupadas por numero de vuelo
            var grupos = reservacion.Boletos.GroupBy(b => b.NumeroVuelo).ToList();
            var sbVuelos = new System.Text.StringBuilder();
            int vIdx = 1;
            foreach (var g in grupos)
            {
                var primer = g.First();
                string asientos = string.Join(", ", g.Select(b => e(b.NoAsiento)));
                sbVuelos.Append($@"
                <tr>
                  <td colspan='5' style='padding:12px 14px;border-bottom:1px solid #F0EBE3;'>
                    <table width='100%' cellpadding='0' cellspacing='0' border='0'>
                      <tr>
                        <td style='vertical-align:top;'>
                          <div style='display:inline-block;width:24px;height:24px;background:#D4AF37;border-radius:50%;text-align:center;line-height:24px;font-size:11px;font-weight:800;color:#1C1A18;margin-right:8px;vertical-align:middle;'>{vIdx}</div>
                          <span style='font-size:15px;font-weight:700;color:#1C1A18;vertical-align:middle;'>{e(primer.OrigenCodigo)} &rarr; {e(primer.DestinoCodigo)}</span>
                          <div style='font-size:11px;color:#8B6B4A;margin-top:4px;'>Vuelo {e(primer.NumeroVuelo)} &middot; {primer.FechaVuelo:dd MMM yyyy} &middot; {primer.HoraSalida.Hours:D2}:{primer.HoraSalida.Minutes:D2}</div>
                          <div style='margin-top:4px;'>
                            <span style='font-size:11px;background:#F2EFEA;border:1px solid rgba(139,107,74,0.3);color:#8B6B4A;padding:2px 8px;border-radius:10px;font-weight:600;'>{e(primer.Clase)}</span>
                            <span style='font-size:11px;color:#6b7280;margin-left:6px;'>Asiento(s): <strong style='color:#1C1A18;'>{asientos}</strong></span>
                          </div>
                        </td>
                        <td style='vertical-align:top;text-align:right;white-space:nowrap;padding-left:8px;'>
                          <div style='font-size:14px;font-weight:700;color:#1C1A18;'>Q {g.Sum(b => b.Precio):N2}</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>");
                vIdx++;
            }

            return $@"<!DOCTYPE html>
<html lang='es'>
<head>
  <meta charset='UTF-8'>
  <meta name='viewport' content='width=device-width,initial-scale=1.0'>
  <meta name='x-apple-disable-message-reformatting'>
</head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;-webkit-text-size-adjust:100%;'>
<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#F2EFEA;'>
  <tr><td align='center' style='padding:24px 12px;'>
  <table width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;background:#ffffff;border-radius:6px;overflow:hidden;border:1px solid rgba(212,175,55,0.25);box-shadow:0 4px 24px rgba(28,26,24,0.10);'>

    <!-- HEADER HERO -->
    <tr>
      <td style='background:linear-gradient(135deg,#1C1A18 0%,#2d2a25 100%);padding:24px 16px;text-align:center;'>
        <div style='font-size:10px;font-weight:700;color:#D4AF37;letter-spacing:3px;text-transform:uppercase;'>Broom AirLine</div>
        <div style='font-size:24px;font-weight:800;color:#F2EFEA;margin-top:6px;line-height:1.2;'>&#10003; Reservaci&#243;n Confirmada</div>
        <div style='font-size:12px;color:rgba(242,239,234,0.6);margin-top:6px;'>{reservacion.FechaCreacion:dd 'de' MMMM 'de' yyyy, HH:mm}</div>
        <div style='margin-top:12px;display:inline-block;background:rgba(212,175,55,0.15);border:1px solid rgba(212,175,55,0.4);border-radius:8px;padding:10px 18px;'>
          <div style='font-size:10px;font-weight:700;color:#D4AF37;letter-spacing:1px;text-transform:uppercase;'>Total Cobrado</div>
          <div style='font-size:26px;font-weight:800;color:#D4AF37;margin-top:2px;'>Q {total:N2}</div>
        </div>
      </td>
    </tr>
    <tr><td style='height:3px;background:linear-gradient(90deg,#D4AF37,#8B6B4A);font-size:0;line-height:0;'>&nbsp;</td></tr>

    <!-- SALUDO + NÚMERO -->
    <tr>
      <td style='padding:20px 16px 0;'>
        <p style='margin:0 0 4px;font-size:15px;color:#1C1A18;'>Hola, <strong>{e(nombreUsuario)}</strong></p>
        <p style='margin:0 0 16px;font-size:13px;color:#5a5249;line-height:1.6;'>Tu pago ha sido procesado exitosamente. Adjunto a este correo encontrar&#225;s el comprobante oficial en PDF con todos los detalles de tu reservaci&#243;n.</p>
        <table width='100%' cellpadding='0' cellspacing='0' border='0'>
          <tr>
            <td style='background:#F9F6F0;border:1px solid rgba(212,175,55,0.3);border-left:4px solid #D4AF37;border-radius:6px;padding:12px 14px;'>
              <div style='font-size:11px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;font-weight:700;'>Nro. de Reservaci&#243;n</div>
              <div style='font-size:20px;font-weight:800;color:#1C1A18;margin-top:4px;letter-spacing:1px;word-break:break-all;overflow-wrap:anywhere;'>{e(noReservacion)}</div>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- VUELOS -->
    <tr>
      <td style='padding:16px 16px 0;'>
        <div style='font-size:11px;font-weight:700;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;'>Itinerario de Vuelo</div>
        <table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#F9F6F0;border:1px solid rgba(139,107,74,0.2);border-radius:6px;overflow:hidden;'>
          {sbVuelos}
          <!-- TOTAL -->
          <tr>
            <td colspan='5' style='padding:12px 14px;background:#1C1A18;font-size:13px;color:#B89A7A;font-weight:600;text-align:right;'>
              Total &nbsp;<span style='font-size:15px;color:#D4AF37;font-weight:800;'>Q {total:N2}</span>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- INSTRUCCIONES -->
    <tr>
      <td style='padding:16px 16px;'>
        <table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#F2EFEA;border-radius:6px;border:1px solid rgba(139,107,74,0.15);'>
          <tr>
            <td style='padding:14px 14px;'>
              <div style='font-size:11px;font-weight:700;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;margin-bottom:10px;'>Instrucciones de abordaje</div>
              <div style='font-size:13px;color:#3A3531;line-height:1.8;word-break:break-word;'>
                &#9679; Presenta tu pasaporte vigente al momento del check-in.<br>
                &#9679; Llega al aeropuerto al menos <strong>2 horas</strong> antes de tu vuelo.<br>
                &#9679; El abordaje cierra <strong>30 minutos</strong> antes de la hora de salida.<br>
                &#9679; Guarda este correo o el PDF adjunto como comprobante.
              </div>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- FOOTER -->
    <tr><td style='height:3px;background:linear-gradient(90deg,#8B6B4A,#D4AF37);font-size:0;line-height:0;'>&nbsp;</td></tr>
    <tr>
      <td style='padding:16px 16px;background:#1C1A18;text-align:center;'>
        <p style='margin:0 0 4px;font-size:12px;color:#B89A7A;font-weight:600;'>Broom AirLine</p>
        <p style='margin:0 0 4px;font-size:11px;color:rgba(184,154,122,0.7);'>distribuidorapine@gmail.com &middot; Guatemala City, Guatemala</p>
        <p style='margin:0;font-size:10px;color:#3A3531;'>Correo generado autom&#225;ticamente &#8212; No responder</p>
      </td>
    </tr>

  </table>
  </td></tr>
</table>
</body>
</html>";
        }

        /// <summary>Sobrecarga de compatibilidad para llamadas antiguas sin el DTO completo.</summary>
        public static string CorreoConfirmacion(string nombreUsuario, string noReservacion, decimal total)
        {
            var e = EmailHelper.Esc;
            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#F2EFEA;'>
  <tr><td align='center' style='padding:24px 12px;'>
  <table width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:560px;background:#fff;border-radius:6px;overflow:hidden;border:1px solid rgba(212,175,55,0.25);'>
    <tr><td style='background:#1C1A18;padding:28px 24px;text-align:center;'>
      <div style='font-size:10px;color:#D4AF37;font-weight:700;letter-spacing:3px;text-transform:uppercase;'>Broom AirLine</div>
      <div style='font-size:24px;font-weight:800;color:#F2EFEA;margin-top:8px;'>&#10003; Reservación Confirmada</div>
    </td></tr>
    <tr><td style='height:3px;background:#D4AF37;font-size:0;'>&nbsp;</td></tr>
    <tr><td style='padding:28px 24px;text-align:center;'>
      <p style='font-size:16px;color:#1C1A18;margin:0 0 6px;'>Hola, <strong>{e(nombreUsuario)}</strong></p>
      <p style='font-size:14px;color:#5a5249;margin:0 0 24px;'>Tu reservación ha sido confirmada. El PDF con el detalle completo va adjunto.</p>
      <table width='100%' cellpadding='0' cellspacing='0' border='0'>
        <tr><td style='background:#F9F6F0;border:1px solid rgba(212,175,55,0.3);border-left:4px solid #D4AF37;border-radius:6px;padding:18px;text-align:center;'>
          <div style='font-size:10px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;font-weight:700;'>Nro. de Reservación</div>
          <div style='font-size:24px;font-weight:800;color:#1C1A18;margin-top:6px 0 16px;'>{e(noReservacion)}</div>
          <div style='height:1px;background:#D4AF37;margin:16px 0;'></div>
          <div style='font-size:10px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;font-weight:700;'>Total Cobrado</div>
          <div style='font-size:26px;font-weight:800;color:#1C1A18;margin-top:6px;'>Q {total:N2}</div>
        </td></tr>
      </table>
    </td></tr>
    <tr><td style='height:3px;background:#8B6B4A;font-size:0;'>&nbsp;</td></tr>
    <tr><td style='padding:14px 24px;background:#1C1A18;text-align:center;'>
      <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine · Guatemala City, Guatemala</p>
      <p style='margin:4px 0 0;font-size:10px;color:#3A3531;'>Correo generado automáticamente — No responder</p>
    </td></tr>
  </table>
  </td></tr>
</table>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE CANCELACION DE VUELO — enviado masivamente a pasajeros
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de aviso masivo que se envia a cada pasajero
        /// afectado cuando el administrador cancela un vuelo completo.
        /// Incluye el numero de vuelo, la ruta, la fecha y el numero de reservacion
        /// del pasajero para facilitar el seguimiento.
        /// </summary>
        public static string CorreoCancelacionVuelo(
            string nombreUsuario,
            string noReservacion,
            string numeroVuelo,
            string origenCodigo,
            string destinoCodigo,
            string fechaVuelo)
        {
            var e = EmailHelper.Esc;

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <!-- HEADER -->
    <div style='background:#1C1A18;padding:28px 20px;text-align:center;'>
        <h1 style='margin:0;font-size:22px;color:#F2EFEA;font-weight:700;'>Vuelo Cancelado</h1>
        <p style='margin:8px 0 0;font-size:13px;color:#B89A7A;'>Broom AirLine</p>
    </div>
    <div style='height:3px;background:#ef4444;'></div>

    <!-- CUERPO -->
    <div style='padding:28px 24px;background:#ffffff;'>
        <p style='font-size:15px;color:#1C1A18;margin:0 0 6px;'>Hola, <strong>{e(nombreUsuario)}</strong>:</p>
        <p style='font-size:14px;color:#3A3531;margin:0 0 24px;line-height:1.6;'>
            Lamentamos informarte que el vuelo que incluye tu reservacion ha sido cancelado
            por parte de la aerolinea. A continuacion encontras el detalle:
        </p>

        <!-- DETALLE DEL VUELO CANCELADO -->
        <div style='background:#F2EFEA;border:1px solid rgba(239,68,68,0.2);border-radius:4px 16px 4px 16px;padding:20px;border-left:4px solid #ef4444;margin-bottom:20px;'>
            <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;width:140px;vertical-align:top;'>N. Reservacion</td>
                    <td style='padding:8px 0;font-size:15px;color:#1C1A18;font-weight:700;font-family:monospace;'>{e(noReservacion)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Vuelo</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;font-weight:600;'>{e(numeroVuelo)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Ruta</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;font-weight:600;'>{e(origenCodigo)} &rarr; {e(destinoCodigo)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Fecha</td>
                    <td style='padding:8px 0;font-size:14px;color:#3A3531;'>{e(fechaVuelo)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Motivo</td>
                    <td style='padding:8px 0;font-size:14px;color:#ef4444;font-weight:600;'>Cancelacion por parte de la aerolinea</td>
                </tr>
            </table>
        </div>

        <!-- MENSAJE DE CONTACTO -->
        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.2);border-radius:4px 16px 4px 16px;padding:16px 20px;border-left:4px solid #B89A7A;'>
            <p style='margin:0;font-size:13px;color:#3A3531;line-height:1.7;'>
                Si tienes preguntas sobre tu reservacion o deseas mas informacion,
                contactanos a <a href='mailto:distribuidorapine@gmail.com' style='color:#8B6B4A;font-weight:600;'>distribuidorapine@gmail.com</a>.
                Lamentamos los inconvenientes ocasionados.
            </p>
        </div>
    </div>

    <!-- FOOTER -->
    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 20px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine &middot; Guatemala City, Guatemala</p>
        <p style='margin:4px 0 0;font-size:10px;color:#3A3531;'>Correo generado automaticamente — No responder</p>
    </div>

</div>
</div>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE CAMBIO DE PERSONAL — enviado a pasajeros cuando
        //  se actualiza la tripulacion de un vuelo activo
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo informativo que se envia a cada pasajero con reserva
        /// activa o pendiente de pago cuando el administrador cambia la tripulacion de un vuelo.
        /// El vuelo NO se cancela; solo se actualiza el personal asignado.
        /// </summary>
        public static string CorreoCambioPersonal(
            string nombreUsuario,
            string noReservacion,
            string numeroVuelo,
            string origenCodigo,
            string destinoCodigo,
            string fechaVuelo)
        {
            var e = EmailHelper.Esc;

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <!-- HEADER -->
    <div style='background:#1C1A18;padding:28px 20px;text-align:center;'>
        <h1 style='margin:0;font-size:22px;color:#F2EFEA;font-weight:700;'>Actualización de Tripulación</h1>
        <p style='margin:8px 0 0;font-size:13px;color:#B89A7A;'>Broom AirLine</p>
    </div>
    <div style='height:3px;background:#B89A7A;'></div>

    <!-- CUERPO -->
    <div style='padding:28px 24px;background:#ffffff;'>
        <p style='font-size:15px;color:#1C1A18;margin:0 0 6px;'>Hola, <strong>{e(nombreUsuario)}</strong>:</p>
        <p style='font-size:14px;color:#3A3531;margin:0 0 24px;line-height:1.6;'>
            Queremos informarte que el personal asignado a tu vuelo ha sido actualizado.
            Tu vuelo sigue programado con normalidad y todos los detalles de tu reservacion
            permanecen sin cambios.
        </p>

        <!-- DETALLE DEL VUELO -->
        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.2);border-radius:4px 16px 4px 16px;padding:20px;border-left:4px solid #B89A7A;margin-bottom:20px;'>
            <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;width:140px;vertical-align:top;'>N. Reservacion</td>
                    <td style='padding:8px 0;font-size:15px;color:#1C1A18;font-weight:700;font-family:monospace;'>{e(noReservacion)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Vuelo</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;font-weight:600;'>{e(numeroVuelo)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Ruta</td>
                    <td style='padding:8px 0;font-size:14px;color:#1C1A18;font-weight:600;'>{e(origenCodigo)} &rarr; {e(destinoCodigo)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Fecha</td>
                    <td style='padding:8px 0;font-size:14px;color:#3A3531;'>{e(fechaVuelo)}</td>
                </tr>
                <tr>
                    <td style='padding:8px 0;font-size:13px;color:#8B6B4A;vertical-align:top;'>Estado</td>
                    <td style='padding:8px 0;font-size:14px;color:#166534;font-weight:600;'>Vuelo activo — sin cambios en horarios</td>
                </tr>
            </table>
        </div>

        <!-- MENSAJE DE CONTACTO -->
        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.2);border-radius:4px 16px 4px 16px;padding:16px 20px;border-left:4px solid #B89A7A;'>
            <p style='margin:0;font-size:13px;color:#3A3531;line-height:1.7;'>
                Si tienes preguntas sobre tu reservacion, contactanos a
                <a href='mailto:distribuidorapine@gmail.com' style='color:#8B6B4A;font-weight:600;'>distribuidorapine@gmail.com</a>.
                Gracias por volar con Broom AirLine.
            </p>
        </div>
    </div>

    <!-- FOOTER -->
    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 20px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine &middot; Guatemala City, Guatemala</p>
        <p style='margin:4px 0 0;font-size:10px;color:#3A3531;'>Correo generado automaticamente — No responder</p>
    </div>

</div>
</div>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE CANCELACIÓN — enviado al usuario
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML premium del correo de cancelacion. Incluye detalle de boletos
        /// si se proporciona la reservacion completa.
        /// </summary>
        public static string CorreoCancelacion(string nombreUsuario, string noReservacion,
            ReservacionDetalleDTO? reservacion = null, string? motivo = null)
        {
            var e = EmailHelper.Esc;
            string fechaCancelacion = DateTime.Now.ToString("dd 'de' MMMM 'de' yyyy, HH:mm");

            // Construir tabla de vuelos cancelados si hay detalle disponible
            string seccionVuelos = "";
            if (reservacion?.Boletos?.Count > 0)
            {
                var sbV = new System.Text.StringBuilder();
                var grupos = reservacion.Boletos.GroupBy(b => b.NumeroVuelo).ToList();
                foreach (var g in grupos)
                {
                    var primer = g.First();
                    sbV.Append($@"
                    <tr>
                      <td style='padding:10px 14px;border-bottom:1px solid #F0EBE3;'>
                        <table width='100%' cellpadding='0' cellspacing='0' border='0'>
                          <tr>
                            <td style='vertical-align:top;'>
                              <div style='font-size:15px;font-weight:800;color:#1C1A18;letter-spacing:0.5px;'>{e(primer.OrigenCodigo)} &rarr; {e(primer.DestinoCodigo)}</div>
                              <div style='font-size:11px;color:#8B6B4A;margin-top:3px;'>Vuelo {e(primer.NumeroVuelo)} &middot; {primer.FechaVuelo:dd MMM yyyy} &middot; {primer.HoraSalida.Hours:D2}:{primer.HoraSalida.Minutes:D2}</div>
                            </td>
                            <td style='vertical-align:top;text-align:right;white-space:nowrap;padding-left:8px;'>
                              <div style='font-size:11px;background:#F2EFEA;border:1px solid rgba(139,107,74,0.3);color:#8B6B4A;padding:2px 8px;border-radius:10px;display:inline-block;'>{e(primer.Clase)}</div>
                              <div style='font-size:12px;font-weight:700;color:#6b7280;text-decoration:line-through;margin-top:3px;'>Q {g.Sum(b => b.Precio):N2}</div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>");
                }
                seccionVuelos = $@"
                <tr><td style='padding:14px 16px 0;'>
                  <div style='font-size:11px;font-weight:700;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;'>Vuelos cancelados</div>
                  <table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#F9F6F0;border:1px solid rgba(239,68,68,0.15);border-radius:6px;overflow:hidden;'>
                    {sbV}
                    <tr><td style='padding:10px 14px;background:#fee2e2;font-size:12px;color:#ef4444;font-weight:700;text-align:right;'>Total cancelado: Q {reservacion.Total:N2}</td></tr>
                  </table>
                </td></tr>";
            }

            string seccionMotivo = "";
            if (!string.IsNullOrWhiteSpace(motivo))
                seccionMotivo = $@"
                <tr><td style='padding:10px 16px 0;'>
                  <table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#FEF2F2;border:1px solid rgba(239,68,68,0.2);border-left:4px solid #ef4444;border-radius:6px;'>
                    <tr><td style='padding:12px 14px;'>
                      <div style='font-size:10px;color:#dc2626;text-transform:uppercase;letter-spacing:1px;font-weight:700;margin-bottom:5px;'>Motivo de cancelación</div>
                      <div style='font-size:13px;color:#3A3531;line-height:1.5;word-break:break-word;'>{e(motivo)}</div>
                    </td></tr>
                  </table>
                </td></tr>";

            return $@"<!DOCTYPE html>
<html lang='es'>
<head>
  <meta charset='UTF-8'>
  <meta name='viewport' content='width=device-width,initial-scale=1.0'>
</head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;-webkit-text-size-adjust:100%;'>
<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#F2EFEA;'>
  <tr><td align='center' style='padding:24px 12px;'>
  <table width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;background:#ffffff;border-radius:6px;overflow:hidden;border:1px solid rgba(239,68,68,0.2);box-shadow:0 4px 24px rgba(28,26,24,0.10);'>

    <!-- HEADER -->
    <tr>
      <td style='background:linear-gradient(135deg,#1C1A18 0%,#2d2a25 100%);padding:24px 16px;'>
        <div style='font-size:10px;font-weight:700;color:#D4AF37;letter-spacing:3px;text-transform:uppercase;'>Broom AirLine</div>
        <div style='font-size:22px;font-weight:800;color:#F2EFEA;margin-top:6px;'>&#10007; Reservación Cancelada</div>
        <div style='font-size:12px;color:rgba(242,239,234,0.6);margin-top:4px;'>{fechaCancelacion}</div>
      </td>
    </tr>
    <tr><td style='height:3px;background:linear-gradient(90deg,#ef4444,#dc2626);font-size:0;line-height:0;'>&nbsp;</td></tr>

    <!-- SALUDO -->
    <tr>
      <td style='padding:16px 16px 12px;'>
        <p style='margin:0 0 6px;font-size:15px;color:#1C1A18;'>Hola, <strong>{e(nombreUsuario)}</strong></p>
        <p style='margin:0 0 14px;font-size:13px;color:#5a5249;line-height:1.6;'>Tu reservación ha sido cancelada. Aquí tienes el detalle de lo cancelado.</p>

        <!-- Número de reservación -->
        <table width='100%' cellpadding='0' cellspacing='0' border='0'>
          <tr>
            <td style='background:#FEF2F2;border:1px solid rgba(239,68,68,0.3);border-left:4px solid #ef4444;border-radius:6px;padding:12px 14px;'>
              <div style='font-size:10px;color:#dc2626;text-transform:uppercase;letter-spacing:1px;font-weight:700;'>Nro. Reservación Cancelada</div>
              <div style='font-size:18px;font-weight:800;color:#1C1A18;margin-top:4px;word-break:break-all;overflow-wrap:anywhere;'>{e(noReservacion)}</div>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    {seccionVuelos}
    {seccionMotivo}

    <!-- SIGUIENTE PASO -->
    <tr>
      <td style='padding:16px 16px;'>
        <table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#F2EFEA;border-radius:6px;border:1px solid rgba(139,107,74,0.15);'>
          <tr><td style='padding:14px 14px;'>
            <div style='font-size:11px;font-weight:700;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;'>¿Tienes preguntas?</div>
            <div style='font-size:13px;color:#3A3531;line-height:1.7;word-break:break-word;'>
              Puedes contactarnos en cualquier momento:<br>
              &#9993; <a href='mailto:distribuidorapine@gmail.com' style='color:#8B6B4A;font-weight:600;text-decoration:none;'>distribuidorapine@gmail.com</a><br>
              Lamentamos los inconvenientes ocasionados.
            </div>
          </td></tr>
        </table>
      </td>
    </tr>

    <!-- FOOTER -->
    <tr><td style='height:3px;background:linear-gradient(90deg,#8B6B4A,#D4AF37);font-size:0;'>&nbsp;</td></tr>
    <tr>
      <td style='padding:16px 16px;background:#1C1A18;text-align:center;'>
        <p style='margin:0 0 4px;font-size:12px;color:#B89A7A;font-weight:600;'>Broom AirLine</p>
        <p style='margin:0 0 4px;font-size:11px;color:rgba(184,154,122,0.7);'>distribuidorapine@gmail.com &middot; Guatemala City, Guatemala</p>
        <p style='margin:0;font-size:10px;color:#3A3531;'>Correo generado autom&#225;ticamente &#8212; No responder</p>
      </td>
    </tr>

  </table>
  </td></tr>
</table>
</body>
</html>";
        }
        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE OFERTA SEMANAL — enviado semanalmente a usuarios suscritos
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de oferta semanal. Muestra el vuelo directo mas barato
        /// y el vuelo con escala mas barato disponibles desde el pais de origen del usuario,
        /// destacando el precio total y la ruta. Si alguno de los dos no esta disponible, se omite.
        /// </summary>
        public static string CorreoOfertaSemanal(
            string nombreUsuario,
            string paisOrigen,
            Services.OfertaVueloInfo? directo,
            Services.OfertaVueloInfo? conEscala)
        {
            var e = EmailHelper.Esc;

            string semana = DateTime.Now.ToString("'semana del' dd 'de' MMMM yyyy", new System.Globalization.CultureInfo("es-ES"));

            static string FormatoHora(TimeSpan t) => $"{t.Hours:D2}:{t.Minutes:D2}";
            static string FormatoFecha(DateTime d) => d.ToString("ddd dd MMM yyyy", new System.Globalization.CultureInfo("es-ES"));

            string bloqueDirecto = directo != null ? $@"
    <tr>
      <td style='padding:0 28px 20px;'>
        <div style='background:#f9f7f4;border:1px solid #e5d9cc;border-radius:10px;overflow:hidden;'>
          <div style='background:linear-gradient(135deg,#1C1A18,#2d2a25);padding:14px 20px;display:flex;align-items:center;gap:10px;'>
            <span style='font-size:16px;color:#D4AF37;'>&#9992;</span>
            <span style='color:#D4AF37;font-size:13px;font-weight:700;letter-spacing:0.5px;'>VUELO DIRECTO</span>
            <span style='margin-left:auto;background:#D4AF37;color:#1C1A18;font-size:11px;font-weight:800;padding:3px 10px;border-radius:20px;'>OFERTA</span>
          </div>
          <div style='padding:16px 20px;'>
            <div style='display:flex;align-items:center;gap:8px;margin-bottom:12px;'>
              <div style='text-align:center;'>
                <div style='font-size:20px;font-weight:800;color:#1C1A18;'>{e(directo.OrigenCodigo)}</div>
                <div style='font-size:11px;color:#7a6a5a;'>{e(directo.OrigenCiudad)}</div>
              </div>
              <div style='flex:1;text-align:center;'>
                <div style='font-size:10px;color:#B89A7A;margin-bottom:2px;'>──────────────────────────────</div>
                <div style='font-size:11px;color:#8B6B4A;font-weight:600;'>DIRECTO · {FormatoHora(directo.HoraSalida)} → {FormatoHora(directo.HoraLlegada)}</div>
                <div style='font-size:10px;color:#aaa;'>{FormatoFecha(directo.Fecha)}</div>
              </div>
              <div style='text-align:center;'>
                <div style='font-size:20px;font-weight:800;color:#1C1A18;'>{e(directo.DestinoCodigo)}</div>
                <div style='font-size:11px;color:#7a6a5a;'>{e(directo.DestinoCiudad)}</div>
              </div>
            </div>
            <div style='text-align:center;border-top:1px solid #e5d9cc;padding-top:12px;'>
              <span style='font-size:28px;font-weight:800;color:#D4AF37;'>Q{directo.TotalPrecio:N2}</span>
              <span style='font-size:12px;color:#8B6B4A;'> / persona · clase turista</span>
            </div>
            <div style='text-align:center;margin-top:4px;font-size:10px;color:#aaa;'>Vuelo {e(directo.NumeroVuelo)}</div>
          </div>
        </div>
      </td>
    </tr>" : "";

            string bloqueEscala = conEscala != null ? $@"
    <tr>
      <td style='padding:0 28px 20px;'>
        <div style='background:#f9f7f4;border:1px solid #e5d9cc;border-radius:10px;overflow:hidden;'>
          <div style='background:linear-gradient(135deg,#2a2540,#3d3660);padding:14px 20px;display:flex;align-items:center;gap:10px;'>
            <span style='font-size:16px;color:#a78bfa;'>&#8634;</span>
            <span style='color:#a78bfa;font-size:13px;font-weight:700;letter-spacing:0.5px;'>CON ESCALA EN {e(conEscala.EscalaCiudad ?? "").ToUpper()}</span>
            <span style='margin-left:auto;background:#7c3aed;color:#fff;font-size:11px;font-weight:800;padding:3px 10px;border-radius:20px;'>MÁS BAJO</span>
          </div>
          <div style='padding:16px 20px;'>
            <!-- Segmento 1 -->
            <div style='font-size:10px;color:#8B6B4A;font-weight:600;margin-bottom:6px;text-transform:uppercase;'>Primer segmento</div>
            <div style='display:flex;align-items:center;gap:8px;margin-bottom:10px;'>
              <div style='text-align:center;'>
                <div style='font-size:16px;font-weight:800;color:#1C1A18;'>{e(conEscala.OrigenCodigo)}</div>
                <div style='font-size:10px;color:#7a6a5a;'>{e(conEscala.OrigenCiudad)}</div>
              </div>
              <div style='flex:1;text-align:center;'>
                <div style='font-size:10px;color:#aaa;'>{FormatoHora(conEscala.HoraSalida)} → {FormatoHora(conEscala.HoraLlegada)} · {FormatoFecha(conEscala.Fecha)}</div>
                <div style='font-size:9px;color:#B89A7A;'>vuelo {e(conEscala.NumeroVuelo)}</div>
              </div>
              <div style='text-align:center;'>
                <div style='font-size:16px;font-weight:800;color:#1C1A18;'>{e(conEscala.EscalaCodigo ?? "")}</div>
                <div style='font-size:10px;color:#7a6a5a;'>{e(conEscala.EscalaCiudad ?? "")}</div>
              </div>
            </div>
            <!-- Segmento 2 -->
            {(conEscala.NumeroVuelo2 != null ? $@"
            <div style='font-size:10px;color:#8B6B4A;font-weight:600;margin-bottom:6px;text-transform:uppercase;'>Segundo segmento</div>
            <div style='display:flex;align-items:center;gap:8px;margin-bottom:10px;'>
              <div style='text-align:center;'>
                <div style='font-size:16px;font-weight:800;color:#1C1A18;'>{e(conEscala.EscalaCodigo ?? "")}</div>
                <div style='font-size:10px;color:#7a6a5a;'>{e(conEscala.EscalaCiudad ?? "")}</div>
              </div>
              <div style='flex:1;text-align:center;'>
                <div style='font-size:10px;color:#aaa;'>{FormatoHora(conEscala.HoraSalida2 ?? TimeSpan.Zero)} → {FormatoHora(conEscala.HoraLlegada2 ?? TimeSpan.Zero)} · {(conEscala.Fecha2.HasValue ? FormatoFecha(conEscala.Fecha2.Value) : "")}</div>
                <div style='font-size:9px;color:#B89A7A;'>vuelo {e(conEscala.NumeroVuelo2)}</div>
              </div>
              <div style='text-align:center;'>
                <div style='font-size:16px;font-weight:800;color:#1C1A18;'>{e(conEscala.DestinoCodigo)}</div>
                <div style='font-size:10px;color:#7a6a5a;'>{e(conEscala.DestinoCiudad)}</div>
              </div>
            </div>" : "")}
            <div style='text-align:center;border-top:1px solid #e5d9cc;padding-top:12px;'>
              <span style='font-size:28px;font-weight:800;color:#7c3aed;'>Q{conEscala.TotalPrecio:N2}</span>
              <span style='font-size:12px;color:#8B6B4A;'> / persona · clase turista (total ruta)</span>
            </div>
          </div>
        </div>
      </td>
    </tr>" : "";

            // Determinar cuál es la más barata
            string mejorOferta = "";
            if (directo != null && conEscala != null)
            {
                if (directo.TotalPrecio <= conEscala.TotalPrecio)
                    mejorOferta = $"<p style='text-align:center;font-size:13px;color:#8B6B4A;margin:0 0 20px;'>La oferta más económica esta semana es el <strong>vuelo directo</strong> desde <strong>{e(directo.OrigenCiudad)}</strong> a <strong>{e(directo.DestinoCiudad)}</strong></p>";
                else
                    mejorOferta = $"<p style='text-align:center;font-size:13px;color:#8B6B4A;margin:0 0 20px;'>La oferta más económica esta semana es el <strong>vuelo con escala</strong> desde <strong>{e(conEscala.OrigenCiudad)}</strong> a <strong>{e(conEscala.DestinoCiudad)}</strong></p>";
            }

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<table width='100%' cellpadding='0' cellspacing='0' style='background:#F2EFEA;'>
  <tr><td align='center' style='padding:20px 12px;'>
  <table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <!-- HEADER -->
    <tr>
      <td style='background:linear-gradient(135deg,#1C1A18,#2d2a25);padding:32px 20px;text-align:center;'>
        <h1 style='margin:0 0 6px;font-size:26px;color:#F2EFEA;font-weight:800;letter-spacing:1.5px;'>BROOM AIRLINE</h1>
        <p style='margin:0;font-size:13px;color:#D4AF37;font-weight:600;letter-spacing:0.5px;'>&#9992; OFERTA DE LA SEMANA &#9992;</p>
        <p style='margin:8px 0 0;font-size:11px;color:#B89A7A;'>{e(semana)}</p>
      </td>
    </tr>

    <!-- GREETING -->
    <tr>
      <td style='padding:28px 28px 16px;'>
        <p style='margin:0 0 8px;font-size:16px;color:#1C1A18;font-weight:700;'>Hola, {e(nombreUsuario)}</p>
        <p style='margin:0 0 16px;font-size:13px;color:#5a4a3a;line-height:1.6;'>Encontramos los vuelos más baratos disponibles esta semana{(string.IsNullOrWhiteSpace(paisOrigen) ? "" : $" desde <strong>{e(paisOrigen)}</strong>")}. Aprovecha antes de que se agoten.</p>
        {mejorOferta}
      </td>
    </tr>

    <!-- OFERTA DIRECTA -->
    {bloqueDirecto}

    <!-- OFERTA CON ESCALA -->
    {bloqueEscala}

    <!-- CTA -->
    <tr>
      <td style='padding:8px 16px 28px;text-align:center;'>
        <p style='margin:0 0 8px;font-size:13px;color:#5a4a3a;line-height:1.6;'>
          Ingresa a <strong>Broom AirLine</strong> y reserva antes de que se agoten estas ofertas.
        </p>
        <p style='margin:0;font-size:11px;color:#aaa;'>
          Para darte de baja de estas ofertas, actualiza tus preferencias en tu perfil.
        </p>
      </td>
    </tr>

    <!-- FOOTER -->
    <tr><td style='height:3px;background:linear-gradient(90deg,#8B6B4A,#D4AF37);font-size:0;'>&nbsp;</td></tr>
    <tr>
      <td style='padding:18px 28px;background:#1C1A18;text-align:center;'>
        <p style='margin:0 0 4px;font-size:12px;color:#B89A7A;font-weight:600;'>Broom AirLine</p>
        <p style='margin:0 0 4px;font-size:11px;color:rgba(184,154,122,0.7);'>distribuidorapine@gmail.com · Guatemala City, Guatemala</p>
        <p style='margin:0;font-size:10px;color:#3A3531;'>Correo generado automáticamente — No responder</p>
      </td>
    </tr>

  </table>
  </td></tr>
</table>
</body>
</html>";
        }

        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Notificacion de actualizacion de datos de perfil.
        /// campo: nombre del campo actualizado (ej. "Teléfono", "Contraseña", "Correo electrónico").
        /// Si cambia el correo: correoAnterior es el correo viejo (se incluye en el cuerpo).
        /// </summary>
        public static string CorreoActualizacionPerfil(
            string nombreUsuario,
            string campo,
            string? correoAnterior = null)
        {
            var e = EmailHelper.Esc;
            string fecha = DateTime.Now.ToString("dd 'de' MMMM 'de' yyyy, HH:mm", new System.Globalization.CultureInfo("es-ES"));
            string notaCorreo = correoAnterior != null
                ? $"<p style='margin:8px 0 0;font-size:12px;color:#6b7280;'>El correo anterior era: <strong style='color:#374151;'>{e(correoAnterior)}</strong>. A partir de ahora uses el nuevo correo para iniciar sesi&#243;n.</p>"
                : "";

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>
<body style='margin:0;padding:0;background:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<table width='100%' cellpadding='0' cellspacing='0' style='background:#F2EFEA;'>
  <tr><td align='center' style='padding:24px 12px;'>
  <table width='100%' cellpadding='0' cellspacing='0' style='max-width:520px;background:#fff;border-radius:6px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 4px 16px rgba(28,26,24,0.08);'>

    <tr><td style='background:linear-gradient(135deg,#1C1A18,#2d2a25);padding:22px 16px;text-align:center;'>
      <div style='font-size:10px;font-weight:700;color:#D4AF37;letter-spacing:3px;text-transform:uppercase;'>Broom AirLine</div>
      <div style='font-size:20px;font-weight:800;color:#F2EFEA;margin-top:6px;'>Actualizaci&#243;n de Perfil</div>
    </td></tr>
    <tr><td style='height:3px;background:#D4AF37;font-size:0;'>&nbsp;</td></tr>

    <tr><td style='padding:20px 16px;'>
      <p style='margin:0 0 6px;font-size:15px;color:#1C1A18;'>Hola, <strong>{e(nombreUsuario)}</strong></p>
      <p style='margin:0 0 16px;font-size:13px;color:#5a5249;line-height:1.6;'>
        Te informamos que tu <strong style='color:#8B6B4A;'>{e(campo)}</strong> fue actualizado exitosamente en tu cuenta de Broom AirLine.
      </p>
      <table width='100%' cellpadding='0' cellspacing='0' style='background:#F9F6F0;border:1px solid rgba(139,107,74,0.2);border-left:4px solid #D4AF37;border-radius:6px;'>
        <tr><td style='padding:12px 14px;'>
          <div style='font-size:11px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;font-weight:700;'>Campo actualizado</div>
          <div style='font-size:16px;font-weight:700;color:#1C1A18;margin-top:4px;'>{e(campo)}</div>
          <div style='font-size:11px;color:#6b7280;margin-top:4px;'>{e(fecha)}</div>
          {notaCorreo}
        </td></tr>
      </table>
      <p style='margin:16px 0 0;font-size:12px;color:#6b7280;line-height:1.6;'>
        Si no realizaste este cambio, por favor contacta a soporte inmediatamente:<br>
        <a href='mailto:distribuidorapine@gmail.com' style='color:#8B6B4A;font-weight:600;text-decoration:none;'>distribuidorapine@gmail.com</a>
      </p>
    </td></tr>

    <tr><td style='height:3px;background:linear-gradient(90deg,#8B6B4A,#D4AF37);font-size:0;'>&nbsp;</td></tr>
    <tr><td style='padding:14px 16px;background:#1C1A18;text-align:center;'>
      <p style='margin:0 0 2px;font-size:11px;color:#B89A7A;font-weight:600;'>Broom AirLine</p>
      <p style='margin:0;font-size:10px;color:#3A3531;'>Correo generado autom&#225;ticamente &#8212; No responder</p>
    </td></tr>

  </table>
  </td></tr>
</table>
</body>
</html>";
        }
    }
}
