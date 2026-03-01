<script>
  // @ts-nocheck
  import '../styles/confirmacion.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;
  export let reservaciones = [];   // viene directo desde App.svelte como prop
  export let facturas      = [];   // si App.svelte lo pasa, si no lo cargamos del searchParams

  const API = 'https://localhost:7107';

  let usuarioId = null;
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  onMount(() => {
    if (!usuarioId) { navigateTo('login'); return; }
    return () => unsubscribe();
  });

  function formatFecha(f) {
    if (!f) return '—';
    return new Date(f).toLocaleDateString('es-GT', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  function formatPrecio(p) {
    return `$ ${Number(p).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  let descargando = {};

  async function descargarComprobante(reservacionId, noReservacion) {
    if (descargando[reservacionId]) return;
    descargando[reservacionId] = true;
    descargando = { ...descargando };

    try {
      // 1. Obtener HTML del backend
      const res = await fetch(`${API}/api/reservaciones/${reservacionId}/comprobante`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error('Error al obtener el comprobante');
      const html = await res.text();

      // 2. Inyectar script de auto-print en el HTML del backend
      //    Reemplaza el botón de imprimir por un trigger automático
      const htmlConAutoPrint = html
        .replace('</body>', `
          <script>
            // Ocultar botón manual y lanzar print automático
            document.querySelectorAll('.no-print').forEach(el => el.style.display = 'none');
            window.onload = function() {
              setTimeout(function() { window.print(); }, 400);
            };
          <\/script>
        </body>`);

      // 3. Abrir en ventana nueva pequeña, print() se dispara solo
      //    El usuario elige "Guardar como PDF" en el diálogo de impresión
      const ventana = window.open('', '_blank', 'width=900,height=700');
      if (!ventana) {
        alert('Permite ventanas emergentes para descargar el comprobante.');
        return;
      }
      ventana.document.open();
      ventana.document.write(htmlConAutoPrint);
      ventana.document.close();

    } catch (err) {
      console.error('Error generando comprobante:', err);
      alert('No se pudo generar el comprobante. Intenta de nuevo.');
    } finally {
      descargando[reservacionId] = false;
      descargando = { ...descargando };
    }
  }

  // Buscar boletos de la reserva que corresponde a cada factura
  function getBoletos(reservacionId) {
    const reserva = reservaciones.find(r => r.reservacionId === reservacionId);
    return reserva?.boletos ?? [];
  }
</script>

<div class="confirmacion">
  <div class="confirmacion__container">

    <!-- Hero -->
    <div class="confirmacion__hero">
      <div class="confirmacion__icono-wrap">
        <div class="confirmacion__icono">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="40" height="40">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
      </div>
      <h1 class="confirmacion__titulo">Compra realizada con exito</h1>
      <p class="confirmacion__subtitulo">
        Tu reservacion ha sido confirmada. Descarga tu comprobante a continuacion
        o consultalo en cualquier momento desde tus reservaciones.
      </p>
      <div class="confirmacion__linea-deco"></div>
    </div>

    <!-- Facturas -->
    {#if facturas.length > 0}
      <div class="confirmacion__facturas">
        {#each facturas as factura}
          {@const boletos = getBoletos(factura.reservacionId)}
          <div class="factura-card">

            <div class="factura-card__header">
              <div class="factura-card__header-left">
                <span class="factura-card__etiqueta">Comprobante de pago</span>
                <span class="factura-card__nro">{factura.noReservacion}</span>
              </div>
              <div class="factura-card__header-right">
                <span class="factura-card__estado">Confirmada</span>
              </div>
            </div>

            <div class="factura-card__body">
              <div class="factura-grid">
                <div class="factura-dato">
                  <span class="factura-dato__label">Factura ID</span>
                  <span class="factura-dato__valor">FAC-{String(factura.facturaId).padStart(6,'0')}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">No. Reservacion</span>
                  <span class="factura-dato__valor">{factura.noReservacion}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Fecha de pago</span>
                  <span class="factura-dato__valor">{formatFecha(factura.fecha)}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">NIT</span>
                  <span class="factura-dato__valor">{factura.nit ?? 'CF'}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Codigo postal</span>
                  <span class="factura-dato__valor">{factura.codigoPostal ?? '—'}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Total pagado</span>
                  <span class="factura-dato__valor factura-dato__valor--gold">{formatPrecio(factura.total)}</span>
                </div>
              </div>

              {#if boletos.length > 0}
                <div class="factura-card__separador"></div>
                <h4 class="factura-card__subtitulo">Detalle de boletos</h4>
                <div class="factura-boletos">
                  {#each boletos as boleto}
                    <div class="factura-boleto">
                      <div class="factura-boleto__col">
                        <span class="factura-boleto__asiento">Asiento {boleto.noAsiento}</span>
                        {#if boleto.pasajero}
                          <span class="factura-boleto__pasajero">
                            {boleto.pasajero.nombre} {boleto.pasajero.apellido}
                          </span>
                        {/if}
                        <span class="factura-boleto__vuelo">
                          Vuelo {boleto.numeroVuelo} &middot; {boleto.clase} &middot; {boleto.origenCodigo} &rarr; {boleto.destinoCodigo}
                        </span>
                      </div>
                      <span class="factura-boleto__precio">{formatPrecio(boleto.precio)}</span>
                    </div>
                  {/each}
                </div>
              {/if}

              <div class="factura-card__footer">
                <button class="btn-pdf"
                  class:btn-pdf--loading={descargando[factura.reservacionId]}
                  disabled={descargando[factura.reservacionId]}
                  on:click={() => descargarComprobante(factura.reservacionId, factura.noReservacion)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="12" y1="18" x2="12" y2="12"/>
                    <line x1="9" y1="15" x2="15" y2="15"/>
                  </svg>
                  {descargando[factura.reservacionId] ? "Generando PDF..." : "Descargar comprobante PDF"}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>

    {:else}
      <!-- Si no hay facturas (navegacion directa), mostrar resumen de reservaciones -->
      <div class="confirmacion__facturas">
        {#each reservaciones as reserva}
          <div class="factura-card">
            <div class="factura-card__header">
              <div class="factura-card__header-left">
                <span class="factura-card__etiqueta">Reservacion confirmada</span>
                <span class="factura-card__nro">{reserva.noReservacion}</span>
              </div>
              <span class="factura-card__estado">Confirmada</span>
            </div>
            <div class="factura-card__body">
              <div class="factura-grid">
                <div class="factura-dato">
                  <span class="factura-dato__label">No. Reservacion</span>
                  <span class="factura-dato__valor">{reserva.noReservacion}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Total</span>
                  <span class="factura-dato__valor factura-dato__valor--gold">{formatPrecio(reserva.total)}</span>
                </div>
                <div class="factura-dato">
                  <span class="factura-dato__label">Boletos</span>
                  <span class="factura-dato__valor">{reserva.boletos?.length ?? 0}</span>
                </div>
              </div>
              <div class="factura-card__footer">
                <button class="btn-pdf"
                  class:btn-pdf--loading={descargando[reserva.reservacionId]}
                  disabled={descargando[reserva.reservacionId]}
                  on:click={() => descargarComprobante(reserva.reservacionId, reserva.noReservacion)}>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="12" y1="18" x2="12" y2="12"/>
                    <line x1="9" y1="15" x2="15" y2="15"/>
                  </svg>
                  {descargando[reserva.reservacionId] ? "Generando PDF..." : "Descargar comprobante PDF"}
                </button>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}

    <!-- Acciones -->
    <div class="confirmacion__acciones">
      <h2 class="confirmacion__acciones-titulo">Que deseas hacer ahora?</h2>
      <div class="confirmacion__btns">
        <button class="btn-primary" on:click={() => navigateTo('home')}>
          Buscar mas vuelos
        </button>
        <button class="btn-secondary" on:click={() => navigateTo('reservas')}>
          Ver mis reservaciones
        </button>
      </div>
    </div>

  </div>
</div>