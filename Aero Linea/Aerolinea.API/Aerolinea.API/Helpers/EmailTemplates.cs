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
        /// Genera el HTML del correo de confirmacion de reservacion que se envia al usuario
        /// una vez que el pago y la confirmacion han sido procesados exitosamente.
        /// Muestra el numero de reservacion y el total cobrado.
        /// </summary>
        public static string CorreoConfirmacion(string nombreUsuario, string noReservacion, decimal total)
        {
            var e = EmailHelper.Esc;

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <div style='background:#1C1A18;padding:28px 20px;text-align:center;'>
        <h1 style='margin:0;font-size:22px;color:#F2EFEA;font-weight:700;'>Reservacion Confirmada</h1>
        <p style='margin:8px 0 0;font-size:13px;color:#B89A7A;'>Broom AirLine</p>
    </div>
    <div style='height:3px;background:#8B6B4A;'></div>

    <div style='padding:30px 20px;text-align:center;background:#ffffff;'>
        <p style='font-size:16px;color:#1C1A18;margin:0 0 8px;'>Hola <strong>{e(nombreUsuario)}</strong></p>
        <p style='font-size:14px;color:#3A3531;margin:0 0 24px;'>Tu reservacion ha sido confirmada exitosamente.</p>

        <div style='background:#F2EFEA;border:1px solid rgba(139,107,74,0.3);border-radius:4px 16px 4px 16px;padding:20px;display:inline-block;border-left:4px solid #8B6B4A;'>
            <p style='margin:0 0 8px;font-size:12px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;'>Nro. Reservacion</p>
            <p style='margin:0 0 16px;font-size:22px;color:#1C1A18;font-weight:800;letter-spacing:1px;'>{e(noReservacion)}</p>
            <p style='margin:0 0 4px;font-size:12px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;'>Total</p>
            <p style='margin:0;font-size:20px;color:#3A3531;font-weight:700;'>Q {total:N2}</p>
        </div>

        <p style='font-size:13px;color:#8B6B4A;margin:24px 0 0;'>Recibiras un correo con el detalle completo de tu reservacion.</p>
    </div>

    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 20px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine · Guatemala City, Guatemala</p>
    </div>

</div>
</div>
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
        //  CORREO DE CANCELACIÓN — enviado al usuario
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Genera el HTML del correo de aviso de cancelacion que se envia al usuario
        /// cuando su reservacion ha sido cancelada, ya sea por el propio usuario o
        /// por el sistema. Indica el numero de reservacion afectado y datos de contacto.
        /// </summary>
        public static string CorreoCancelacion(string nombreUsuario, string noReservacion)
        {
            var e = EmailHelper.Esc;

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'></head>
<body style='margin:0;padding:0;background-color:#F2EFEA;font-family:Segoe UI,Roboto,Arial,sans-serif;'>
<div style='max-width:600px;margin:0 auto;padding:20px 12px;'>
<div style='background:#ffffff;border-radius:4px 16px 4px 16px;overflow:hidden;border:1px solid rgba(139,107,74,0.2);box-shadow:0 8px 32px rgba(28,26,24,0.12);'>

    <div style='background:#1C1A18;padding:28px 20px;text-align:center;'>
        <h1 style='margin:0;font-size:22px;color:#F2EFEA;font-weight:700;'>Reservacion Cancelada</h1>
        <p style='margin:8px 0 0;font-size:13px;color:#B89A7A;'>Broom AirLine</p>
    </div>
    <div style='height:3px;background:#ef4444;'></div>

    <div style='padding:30px 20px;text-align:center;background:#ffffff;'>
        <p style='font-size:16px;color:#1C1A18;margin:0 0 8px;'>Hola <strong>{e(nombreUsuario)}</strong>,</p>
        <p style='font-size:14px;color:#3A3531;margin:0 0 24px;'>Tu reservacion ha sido cancelada.</p>

        <div style='background:#F2EFEA;border:1px solid rgba(239,68,68,0.25);border-radius:4px 16px 4px 16px;padding:20px;display:inline-block;border-left:4px solid #ef4444;'>
            <p style='margin:0 0 8px;font-size:12px;color:#8B6B4A;text-transform:uppercase;letter-spacing:1px;'>Nro. Reservacion</p>
            <p style='margin:0;font-size:22px;color:#ef4444;font-weight:800;letter-spacing:1px;'>{e(noReservacion)}</p>
        </div>

        <p style='font-size:13px;color:#8B6B4A;margin:24px 0 0;'>Si tienes preguntas, contactanos a distribuidorapine@gmail.com</p>
    </div>

    <div style='height:3px;background:#8B6B4A;'></div>
    <div style='padding:16px 20px;background:#1C1A18;text-align:center;'>
        <p style='margin:0;font-size:11px;color:#B89A7A;'>Broom AirLine · Guatemala City, Guatemala</p>
    </div>

</div>
</div>
</body>
</html>";
        }
    }
}
