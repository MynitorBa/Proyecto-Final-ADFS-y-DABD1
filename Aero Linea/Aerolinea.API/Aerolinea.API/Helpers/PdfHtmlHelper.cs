using Aerolinea.API.DTOs;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Clase estatica que genera el HTML utilizado para producir el comprobante
    /// de reservacion en formato PDF. Construye un documento HTML completo con
    /// el detalle de boletos, datos del pasajero, informacion fiscal si aplica,
    /// subtotales por vuelo y los terminos y condiciones del servicio.
    /// </summary>
    public static class PdfHtmlHelper
    {
        private static string E(string? t) => EmailHelper.Esc(t ?? "");
        private static string Nn(string? s) => string.IsNullOrWhiteSpace(s) ? "—" : s;
        private static string Hm(TimeSpan t) =>
            t == default ? "—" : $"{(int)t.TotalHours:D2}:{t.Minutes:D2}";

        /// <summary>
        /// Genera el HTML completo del comprobante de reservacion optimizado para
        /// impresion en formato A5 horizontal. Incluye encabezado con numero y estado
        /// de la reservacion, tabla de boletos con subtotales por vuelo, seccion de
        /// datos de pasajeros, datos fiscales si la reservacion tiene factura asociada,
        /// y pie de pagina institucional.
        /// </summary>
        public static string GenerarComprobante(ReservacionDetalleDTO reservacion)
        {
            string ec = reservacion.EstadoReserva?.ToLower() switch
            {
                "confirmada" => "#2C5F2D",
                "cancelada" => "#ef4444",
                "pendiente" => "#B89A7A",
                "expirada" => "#6b7280",
                _ => "#8B6B4A"
            };
            string eu = E(reservacion.EstadoReserva?.ToUpper() ?? "—");

            const string SEC = "background:#8B6B4A;color:#F2EFEA;padding:7px 14px;font-size:8pt;font-weight:700;text-transform:uppercase;letter-spacing:1.2px";
            const string TH = "padding:6px 8px;font-size:6.5pt;color:#D4C5B0;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;text-align:left;background:#1C1A18;border-bottom:2px solid #8B6B4A";
            const string THC = "padding:6px 8px;font-size:6.5pt;color:#D4C5B0;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;text-align:center;background:#1C1A18;border-bottom:2px solid #8B6B4A";
            const string TD = "padding:5px 8px;font-size:8pt;border-bottom:1px solid #E6E1DA";
            const string TDC = "padding:5px 8px;font-size:8pt;border-bottom:1px solid #E6E1DA;text-align:center";
            const string TDR = "padding:5px 8px;font-size:8pt;border-bottom:1px solid #E6E1DA;text-align:right;font-weight:700";
            const string IL = "padding:5px 8px;font-size:7.5pt;color:#8B6B4A;font-weight:600;background:#F7F4EF;border-bottom:1px solid #E6E1DA;white-space:nowrap";
            const string IV = "padding:5px 8px;font-size:8pt;border-bottom:1px solid #E6E1DA";

            var sb = new System.Text.StringBuilder();
            int idx = 1;
            foreach (var b in reservacion.Boletos)
            {
                string bg = idx % 2 == 0 ? "background:#F7F4EF" : "";
                sb.Append($@"<tr style='{bg}'>
                  <td style='{TDC}'>{idx}</td>
                  <td style='{TD}'>{E(b.NoBoleto)}</td>
                  <td style='{TD}'>{E(b.NumeroVuelo)}</td>
                  <td style='{TDC}'>{E(b.NoAsiento)}</td>
                  <td style='{TD}'>{E(b.OrigenCodigo)} &rarr; {E(b.DestinoCodigo)}</td>
                  <td style='{TDC}'>{E(b.Clase)}</td>
                  <td style='{TDC}'>{b.FechaVuelo:yyyy-MM-dd}</td>
                  <td style='{TDC}'>{Hm(b.HoraSalida)} - {Hm(b.HoraLlegada)}</td>
                  <td style='{TDC}'>{b.DuracionMinutos} min</td>
                  <td style='{TDR}'>$ {b.Precio:N2}</td>
                </tr>");
                idx++;
            }
            string filas = sb.ToString();

            var ss = new System.Text.StringBuilder();
            var grupos = reservacion.Boletos.GroupBy(b => b.NumeroVuelo).ToList();
            if (grupos.Count > 1)
                foreach (var g in grupos)
                {
                    decimal sub = g.Sum(b => b.Precio);
                    ss.Append($@"<tr style='background:#EDE8E2'>
                      <td colspan='9' style='{TD};text-align:right;font-weight:600'>Vuelo {E(g.Key)} ({g.Count()} boleto{(g.Count() > 1 ? "s" : "")})</td>
                      <td style='{TDR}'>$ {sub:N2}</td>
                    </tr>");
                }
            string subs = ss.ToString();

            string paxHtml = "";
            var cp = reservacion.Boletos.Where(b => b.Pasajero != null).ToList();
            if (cp.Any())
            {
                var sp = new System.Text.StringBuilder();
                int pi = 1;
                foreach (var b in cp)
                {
                    var p = b.Pasajero!;
                    string bg = pi % 2 == 0 ? "background:#F7F4EF" : "";
                    sp.Append($@"<tr style='{bg}'>
                      <td style='{TDC}'>{pi}</td>
                      <td style='{TD}'>{E(p.Nombre)} {E(p.Apellido)}</td>
                      <td style='{TD}'>{E(p.Pasaporte)}</td>
                      <td style='{TD}'>{Nn(p.Telefono)}</td>
                      <td style='{TD}'>{Nn(p.Ciudad)}, {Nn(p.Pais)}</td>
                      <td style='{TDC}'>{E(b.NoAsiento)}</td>
                      <td style='{TD}' colspan='4'>{E(b.NumeroVuelo)}</td>
                    </tr>");
                    pi++;
                }
                paxHtml = $@"
                <tr><td colspan='10' style='{SEC}'>Datos de Pasajeros</td></tr>
                <tr>
                  <th style='{THC}'>#</th><th style='{TH}'>Nombre Completo</th><th style='{TH}'>Pasaporte</th>
                  <th style='{TH}'>Telefono</th><th style='{TH}'>Ciudad, Pais</th><th style='{THC}'>Asiento</th>
                  <th style='{TH}' colspan='4'>Vuelo</th>
                </tr>{sp}";
            }

            string facHtml = "";
            if (reservacion.Factura != null)
            {
                var f = reservacion.Factura;
                facHtml = $@"
                <tr><td colspan='10' style='{SEC}'>Datos Fiscales</td></tr>
                <tr>
                  <th colspan='3' style='{TH}'>NIT / RFC</th><th colspan='2' style='{TH}'>Codigo Postal</th>
                  <th colspan='3' style='{TH}'>Fecha Emision</th><th colspan='2' style='{TH};text-align:right'>Total Factura</th>
                </tr>
                <tr>
                  <td colspan='3' style='{TDC}'>{E(f.NIT)}</td><td colspan='2' style='{TDC}'>{E(f.CodigoPostal)}</td>
                  <td colspan='3' style='{TDC}'>{f.Fecha:yyyy-MM-dd}</td><td colspan='2' style='{TDR}'>$ {f.Total:N2}</td>
                </tr>";
            }

            string avion = reservacion.Boletos.Any()
                ? $"{E(reservacion.Boletos.First().AvionMarca)} {E(reservacion.Boletos.First().AvionModelo)}" : "—";

            return $@"<!DOCTYPE html>
<html>
<head><meta charset='UTF-8'></head>
<body style='margin:0;padding:0;font-family:Segoe UI,Roboto,Arial,sans-serif;font-size:8pt;color:#1C1A18;height:209mm'>

<table style='width:100%;height:209mm;border-collapse:collapse'>

  <tr>
    <td style='background:#1C1A18;padding:14px 22px 10px;height:1px'>
      <table style='width:100%;border-collapse:collapse'>
        <tr>
          <td style='vertical-align:top'>
            <div style='font-size:22pt;font-weight:800;letter-spacing:2px;color:#F2EFEA;white-space:nowrap'>BROOM AIRLINE</div>
            <div style='font-size:6.5pt;color:#B89A7A;margin-top:1px'>Aerolinea &middot; Guatemala City &middot; distribuidorapine@gmail.com</div>
          </td>
          <td style='vertical-align:top;text-align:right'>
            <div style='display:inline-block;border:1px solid #8B6B4A;color:#D4A056;padding:2px 10px;font-size:7pt;font-weight:700;text-transform:uppercase;letter-spacing:2px'>Comprobante</div>
            <div style='font-size:12pt;font-weight:700;color:#F2EFEA;margin-top:5px'>{E(reservacion.NoReservacion)}</div>
            <div style='font-size:8pt;font-weight:700;color:{ec};margin-top:2px'>&#9679; {eu}</div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr><td style='background:#8B6B4A;height:3px;font-size:0;line-height:0'>&nbsp;</td></tr>

  <tr>
    <td style='padding:12px 22px 6px;vertical-align:top;height:1px'>
      <table style='width:100%;border-collapse:collapse;border:1px solid #D8D1C5'>
        <tr><td colspan='10' style='{SEC}'>Datos de la Reservacion</td></tr>
        <tr>
          <td style='{IL}'>Nro. Reservacion</td><td colspan='4' style='{IV}'>{E(reservacion.NoReservacion)}</td>
          <td style='{IL}'>Avion</td><td colspan='4' style='{IV}'>{Nn(avion)}</td>
        </tr>
        <tr>
          <td style='{IL}'>Pasajero</td><td colspan='4' style='{IV}'>{E(reservacion.UsuarioNombre)}</td>
          <td style='{IL}'>Email</td><td colspan='4' style='{IV}'>{E(reservacion.UsuarioEmail)}</td>
        </tr>
        <tr>
          <td style='{IL}'>Fecha Emision</td><td colspan='4' style='{IV}'>{reservacion.FechaCreacion:yyyy-MM-dd HH:mm}</td>
          <td style='{IL}'>Estado</td><td colspan='4' style='{IV};font-weight:700;color:{ec}'>{eu}</td>
        </tr>
        <tr><td colspan='10' style='{SEC}'>Detalle de Boletos</td></tr>
        <tr>
          <th style='{THC}'>#</th><th style='{TH}'>Nro. Boleto</th><th style='{TH}'>Vuelo</th>
          <th style='{THC}'>Asiento</th><th style='{TH}'>Ruta</th><th style='{THC}'>Clase</th>
          <th style='{THC}'>Fecha</th><th style='{THC}'>Horario</th><th style='{THC}'>Duracion</th><th style='{THC}'>Subtotal</th>
        </tr>
        {filas}
        {subs}
        <tr>
          <td colspan='9' style='background:#3A3531;color:#F2EFEA;padding:8px 14px;font-size:9pt;font-weight:700'>TOTAL RESERVACION</td>
          <td style='background:#3A3531;color:#F2EFEA;padding:8px 14px;font-size:9pt;font-weight:800;text-align:right'>$ {reservacion.Total:N2}</td>
        </tr>
        {paxHtml}
        {facHtml}
        <tr><td colspan='10' style='background:#F7F4EF;padding:5px 14px;font-size:7pt;color:#8B6B4A;font-weight:700;text-transform:uppercase;letter-spacing:0.5px'>Terminos y Condiciones</td></tr>
        <tr><td colspan='10' style='padding:7px 14px;font-size:7pt;color:#5a5249;line-height:1.65'>
          1. Este comprobante es valido unicamente para los vuelos indicados.<br>
          2. Presentar pasaporte vigente al momento del check-in.<br>
          3. Abordaje cierra 30 minutos antes de la hora de salida.<br>
          4. Cancelaciones estan sujetas a la politica vigente de Broom AirLine.<br>
          5. Este documento es comprobante oficial de reservacion.
        </td></tr>
      </table>
    </td>
  </tr>

  <!-- SPACER: llena todo el espacio restante -->
  <tr><td style='height:100%;font-size:0'>&nbsp;</td></tr>

  <tr><td style='background:#8B6B4A;height:3px;font-size:0;line-height:0'>&nbsp;</td></tr>
  <tr>
    <td style='background:#1C1A18;padding:10px 22px;height:1px'>
      <table style='width:100%;border-collapse:collapse'>
        <tr>
          <td style='font-size:6.5pt;color:#B89A7A'>BROOM AIRLINE &middot; distribuidorapine@gmail.com &middot; Guatemala City, Guatemala</td>
          <td style='font-size:6.5pt;color:#B89A7A;text-align:right'>Comprobante oficial de reservacion</td>
        </tr>
      </table>
    </td>
  </tr>

</table>

</body>
</html>";
        }
    }
}
