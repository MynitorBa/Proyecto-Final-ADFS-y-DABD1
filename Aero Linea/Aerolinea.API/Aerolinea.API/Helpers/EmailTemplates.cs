using Aerolinea.API.DTOs;

namespace Aerolinea.API.Helpers
{
    public static class EmailTemplates
    {
        // ══════════════════════════════════════════════════════════════════
        //  CORREO DE RESERVACIÓN — enviado al usuario con detalle de boletos
        // ══════════════════════════════════════════════════════════════════
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

        <!-- INFO RESERVACIÓN -->
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

        <!-- TABLA DE BOLETOS -->
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

            <!-- TOTAL -->
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

        <!-- TÉRMINOS -->
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
        //  CORREO DE CONTACTO — enviado al admin
        // ══════════════════════════════════════════════════════════════════
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
        //  CORREO DE CANCELACIÓN — enviado al usuario
        // ══════════════════════════════════════════════════════════════════
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