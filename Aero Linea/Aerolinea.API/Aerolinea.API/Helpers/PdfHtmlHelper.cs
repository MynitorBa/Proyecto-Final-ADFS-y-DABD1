using Aerolinea.API.DTOs;

namespace Aerolinea.API.Helpers
{
    public static class PdfHtmlHelper
    {
        private static string E(string? t) => EmailHelper.Esc(t ?? "");
        private static string Nn(string? s) => string.IsNullOrWhiteSpace(s) ? "—" : s;

        // TimeSpan -> "HH:mm"  (evita crash si el valor es default)
        private static string Hm(TimeSpan t) =>
            t == default ? "—" : $"{(int)t.TotalHours:D2}:{t.Minutes:D2}";

        public static string GenerarComprobante(ReservacionDetalleDTO reservacion)
        {
            string estadoColor = reservacion.EstadoReserva?.ToLower() switch
            {
                "confirmada" => "#2C5F2D",
                "cancelada" => "#ef4444",
                "pendiente" => "#B89A7A",
                "expirada" => "#6b7280",
                _ => "#8B6B4A"
            };

            // ── Filas de boletos ──────────────────────────────────────────
            var sb = new System.Text.StringBuilder();
            int idx = 1;
            foreach (var b in reservacion.Boletos)
            {
                string bg = idx % 2 == 0 ? "#F2EFEA" : "#ffffff";
                sb.Append($@"
                <tr style='background:{bg};'>
                    <td class='td-c'>{idx}</td>
                    <td class='td'>{E(b.NoBoleto)}</td>
                    <td class='td'>{E(b.NumeroVuelo)}</td>
                    <td class='td-c'>{E(b.NoAsiento)}</td>
                    <td class='td'>{E(b.OrigenCodigo)} &rarr; {E(b.DestinoCodigo)}</td>
                    <td class='td'>{E(b.OrigenCiudad)} &rarr; {E(b.DestinoCiudad)}</td>
                    <td class='td'>{E(b.Clase)}</td>
                    <td class='td-c'>{b.FechaVuelo:yyyy-MM-dd}</td>
                    <td class='td-c'>{Hm(b.HoraSalida)}</td>
                    <td class='td-c'>{Hm(b.HoraLlegada)}</td>
                    <td class='td-c'>{b.DuracionMinutos} min</td>
                    <td class='td-r bold'>Q {b.Precio:N2}</td>
                </tr>");
                idx++;
            }
            string filasBoletos = sb.ToString();

            // ── Subtotales por vuelo ──────────────────────────────────────
            var sbSub = new System.Text.StringBuilder();
            var grupos = reservacion.Boletos.GroupBy(b => b.NumeroVuelo).ToList();
            if (grupos.Count > 1)
            {
                foreach (var g in grupos)
                {
                    decimal sub = g.Sum(b => b.Precio);
                    sbSub.Append($@"
                    <tr style='background:#EDE8E2;'>
                        <td colspan='9' class='td' style='text-align:right;font-weight:600;color:#3A3531;'>
                            Vuelo {E(g.Key)} ({g.Count()} boleto{(g.Count() > 1 ? "s" : "")})
                        </td>
                        <td colspan='3' class='td-r bold' style='color:#1C1A18;'>Q {sub:N2}</td>
                    </tr>");
                }
                sbSub.Append("<tr><td colspan='12' style='height:1px;background:#C4B8AA;padding:0;'></td></tr>");
            }
            string subtotales = sbSub.ToString();

            // ── Sección pasajeros ─────────────────────────────────────────
            string seccionPasajeros = "";
            var conPax = reservacion.Boletos.Where(b => b.Pasajero != null).ToList();
            if (conPax.Any())
            {
                var sbPax = new System.Text.StringBuilder();
                int pi = 1;
                foreach (var b in conPax)
                {
                    var p = b.Pasajero!;
                    string bgP = pi % 2 == 0 ? "#F2EFEA" : "#ffffff";
                    sbPax.Append($@"
                    <tr style='background:{bgP};'>
                        <td class='td-c'>{pi}</td>
                        <td class='td'>{E(p.Nombre)} {E(p.Apellido)}</td>
                        <td class='td'>{E(p.Pasaporte)}</td>
                        <td class='td'>{Nn(p.Telefono)}</td>
                        <td class='td'>{Nn(p.Ciudad)}, {Nn(p.Pais)}</td>
                        <td class='td-c'>{E(b.NoAsiento)}</td>
                        <td class='td'>{E(b.NumeroVuelo)}</td>
                    </tr>");
                    pi++;
                }
                seccionPasajeros = $@"
                <div class='bloque' style='margin-top:22px;'>
                    <div class='titulo-seccion'>DATOS DE PASAJEROS</div>
                    <table class='tabla'>
                        <tr class='th-row'>
                            <th class='th-c'>#</th>
                            <th class='th'>Nombre Completo</th>
                            <th class='th'>Pasaporte</th>
                            <th class='th'>Telefono</th>
                            <th class='th'>Ciudad, Pais</th>
                            <th class='th-c'>Asiento</th>
                            <th class='th'>Vuelo</th>
                        </tr>
                        {sbPax}
                    </table>
                </div>";
            }

            // ── Info avión ────────────────────────────────────────────────
            string infoAvion = reservacion.Boletos.Any()
                ? $"{E(reservacion.Boletos.First().AvionMarca)} {E(reservacion.Boletos.First().AvionModelo)}"
                : "—";

            return $@"<!DOCTYPE html>
<html lang='es'>
<head>
<meta charset='UTF-8'>
<title>Comprobante — {E(reservacion.NoReservacion)}</title>
<style>
    @page {{ margin:10mm 12mm; size:A4 landscape; }}
    *{{ margin:0;padding:0;box-sizing:border-box; }}
    body{{ font-family:'Segoe UI',Roboto,Arial,sans-serif;color:#1C1A18;background:#F2EFEA;font-size:9pt; }}

    .header{{ background:#1C1A18;color:#F2EFEA;padding:20px 28px;display:flex;justify-content:space-between;align-items:center; }}
    .header-left h1{{ font-size:22pt;font-weight:800;letter-spacing:1.5px;margin-bottom:4px;color:#F2EFEA; }}
    .header-left p{{ font-size:7.5pt;color:#B89A7A; }}
    .header-right{{ text-align:right; }}
    .header-right .tipo{{ font-size:7.5pt;color:#B89A7A;font-weight:700;text-transform:uppercase;letter-spacing:2px;margin-bottom:4px; }}
    .header-right .nro{{ font-size:13pt;font-weight:700;margin-bottom:6px;color:#F2EFEA; }}
    .header-right .estado{{ font-size:7.5pt;font-weight:700;color:{estadoColor};margin-bottom:3px; }}
    .header-right .fecha{{ font-size:7pt;color:#B89A7A; }}
    .linea-header{{ height:3px;background:#8B6B4A; }}

    .contenido{{ padding:20px 28px;background:#ffffff; }}

    .bloque{{ border:0.8px solid #C4B8AA;background:#fff;overflow:hidden;margin-bottom:0; }}
    .titulo-seccion{{ background:#3A3531;color:#F2EFEA;padding:10px 18px;font-size:7.5pt;font-weight:700;text-transform:uppercase;letter-spacing:1.2px; }}

    .info-grid{{ display:flex; }}
    .info-col{{ flex:1;padding:18px; }}
    .info-col+.info-col{{ border-left:1px solid #C4B8AA; }}
    .info-col h3{{ font-size:7.5pt;color:#8B6B4A;font-weight:700;letter-spacing:.8px;text-transform:uppercase;margin-bottom:10px; }}
    .info-row{{ display:flex;border-bottom:.6px solid #C4B8AA; }}
    .info-row:last-child{{ border-bottom:none; }}
    .info-lbl{{ width:130px;padding:8px 10px;font-size:7.5pt;color:#8B6B4A;font-weight:600;background:#F2EFEA; }}
    .info-val{{ flex:1;padding:8px 10px;font-size:7.5pt;color:#1C1A18; }}
    .info-val.highlight{{ font-weight:700; }}

    .tabla{{ width:100%;border-collapse:collapse; }}
    .th-row{{ background:#3A3531; }}
    .th,.th-c{{ padding:10px 8px;font-size:7.5pt;color:#F2EFEA;font-weight:700;text-transform:uppercase;letter-spacing:.8px;text-align:left;border-bottom:1px solid #1C1A18;border-right:.5px solid #1C1A18; }}
    .th-c{{ text-align:center; }}
    .td,.td-c,.td-r{{ padding:9px 8px;font-size:8pt;color:#1C1A18;border-bottom:.6px solid #C4B8AA;border-right:.5px solid #C4B8AA; }}
    .td-c{{ text-align:center; }}
    .td-r{{ text-align:right; }}
    .bold{{ font-weight:700; }}

    .total-row{{ background:#8B6B4A; }}
    .total-row td{{ padding:11px 18px;color:#F2EFEA;font-size:9.5pt;font-weight:700; }}
    .total-row .total-valor{{ background:#F2EFEA;color:#1C1A18;text-align:right;font-size:9.5pt;font-weight:800; }}

    .condiciones{{ margin-top:22px; }}
    .condiciones h4{{ background:#F2EFEA;padding:10px 18px;font-size:7.5pt;color:#8B6B4A;font-weight:700;letter-spacing:.8px;text-transform:uppercase;border-bottom:.8px solid #C4B8AA; }}
    .condiciones p{{ padding:14px 18px;font-size:7.5pt;color:#3A3531;line-height:1.8;background:#fff; }}

    .linea-footer{{ height:2px;background:#8B6B4A; }}
    .footer{{ background:#1C1A18;color:#B89A7A;padding:12px 28px;display:flex;justify-content:space-between;font-size:7pt;margin-top:22px; }}

    @media print{{
        body{{ -webkit-print-color-adjust:exact!important;print-color-adjust:exact!important; }}
        .no-print{{ display:none!important; }}
        .header,.total-row,.titulo-seccion,.th-row,.footer,.linea-header,.linea-footer{{
            -webkit-print-color-adjust:exact!important;
            print-color-adjust:exact!important;
        }}
        tr{{ page-break-inside:avoid; }}
    }}
</style>
</head>
<body>

<div class='no-print' style='text-align:center;padding:16px;background:#1C1A18;'>
    <button onclick='window.print()' style='padding:12px 32px;background:#8B6B4A;color:#F2EFEA;border:none;border-radius:0 16px 0 16px;font-size:14px;font-weight:700;cursor:pointer;'>
        Imprimir / Guardar como PDF
    </button>
</div>

<div class='header'>
    <div class='header-left'>
        <h1>BROOM AIRLINE</h1>
        <p>Aerolinea &middot; Guatemala City &middot; distribuidorapine@gmail.com</p>
    </div>
    <div class='header-right'>
        <div class='tipo'>COMPROBANTE</div>
        <div class='nro'>{E(reservacion.NoReservacion)}</div>
        <div class='estado'>&#9679; {E(reservacion.EstadoReserva?.ToUpper() ?? "—")}</div>
        <div class='fecha'>Emitido: {reservacion.FechaCreacion:yyyy-MM-dd HH:mm}</div>
    </div>
</div>
<div class='linea-header'></div>

<div class='contenido'>
    <div class='bloque'>
        <div class='titulo-seccion'>DATOS DE LA RESERVACION</div>
        <div class='info-grid'>
            <div class='info-col'>
                <h3>Reservacion</h3>
                <div class='info-row'><div class='info-lbl'>Nro. Reservacion</div><div class='info-val highlight'>{E(reservacion.NoReservacion)}</div></div>
                <div class='info-row'><div class='info-lbl'>Pasajero</div><div class='info-val'>{E(reservacion.UsuarioNombre)}</div></div>
                <div class='info-row'><div class='info-lbl'>Email</div><div class='info-val'>{E(reservacion.UsuarioEmail)}</div></div>
                <div class='info-row'><div class='info-lbl'>Avion</div><div class='info-val'>{Nn(infoAvion)}</div></div>
                <div class='info-row'><div class='info-lbl'>Fecha Emision</div><div class='info-val'>{reservacion.FechaCreacion:yyyy-MM-dd HH:mm}</div></div>
            </div>
            <div class='info-col'>
                <h3>Contacto</h3>
                <div class='info-row'><div class='info-lbl'>Aerolinea</div><div class='info-val'>Broom AirLine</div></div>
                <div class='info-row'><div class='info-lbl'>Email</div><div class='info-val'>distribuidorapine@gmail.com</div></div>
                <div class='info-row'><div class='info-lbl'>Ciudad</div><div class='info-val'>Guatemala City, GT</div></div>
            </div>
        </div>
    </div>

    <div class='bloque' style='margin-top:22px;'>
        <div class='titulo-seccion'>DETALLE DE BOLETOS</div>
        <table class='tabla'>
            <tr class='th-row'>
                <th class='th-c'>#</th>
                <th class='th'>Nro. Boleto</th>
                <th class='th'>Vuelo</th>
                <th class='th-c'>Asiento</th>
                <th class='th'>Ruta</th>
                <th class='th'>Ciudades</th>
                <th class='th'>Clase</th>
                <th class='th-c'>Fecha</th>
                <th class='th-c'>Salida</th>
                <th class='th-c'>Llegada</th>
                <th class='th-c'>Duracion</th>
                <th class='th-c'>Precio</th>
            </tr>
            {filasBoletos}
            <tr><td colspan='12' style='height:1px;background:#C4B8AA;padding:0;'></td></tr>
            {subtotales}
            <tr class='total-row'>
                <td colspan='11'>TOTAL RESERVACION</td>
                <td class='total-valor'>Q {reservacion.Total:N2}</td>
            </tr>
        </table>
    </div>

    {seccionPasajeros}

    <div class='bloque condiciones' style='margin-top:22px;'>
        <h4>Terminos y Condiciones</h4>
        <p>
            1. Este comprobante es valido unicamente para los vuelos indicados.<br>
            2. Presentar pasaporte vigente al momento del check-in.<br>
            3. Abordaje cierra 30 minutos antes de la hora de salida.<br>
            4. Cancelaciones estan sujetas a la politica vigente de Broom AirLine.<br>
            5. Este documento es comprobante oficial de reservacion.
        </p>
    </div>
</div>

<div class='linea-footer'></div>
<div class='footer'>
    <span>BROOM AIRLINE &middot; distribuidorapine@gmail.com &middot; Guatemala City, Guatemala</span>
    <span>Comprobante oficial de reservacion</span>
</div>

</body>
</html>";
        }
    }
}