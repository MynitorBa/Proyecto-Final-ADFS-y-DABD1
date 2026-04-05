<script>
  // @ts-nocheck
  import '../styles/carrito.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;

  import { API } from '../lib/api.js';

  // ── misma lógica de acceso que Profile ──
  let usuarioId = null;
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  let cargando    = true;
  let errorMsg    = '';
  let reservas    = [];   // solo las Pendientes (estadoReservaId === 1)

  onMount(async () => {
    if (!usuarioId) { navigateTo('login'); return; }
    await cargarReservasPendientes();
    return () => unsubscribe();
  });

  async function cargarReservasPendientes() {
    cargando = true; errorMsg = '';
    try {
      const res = await fetch(`${API}/api/mis-reservaciones`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error('Error al cargar reservaciones.');
      const todas = await res.json();
      // Filtrar solo las Pendientes
      reservas = todas.filter(r => r.estadoReservaId === 1);
    } catch (e) {
      errorMsg = e.message ?? 'Error de conexión.';
    } finally {
      cargando = false;
    }
  }

  function formatFecha(f) {
    if (!f) return '—';
    return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  function formatHora(h) { return h ? h.substring(0, 5) : '—'; }

  function formatPrecio(p) {
    return `$ ${Number(p).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }

  // Agrupar boletos de una reserva por vuelo
  function agruparPorVuelo(boletos) {
    const map = {};
    for (const b of boletos) {
      if (!map[b.vueloId]) map[b.vueloId] = { vuelo: b, boletos: [] };
      map[b.vueloId].boletos.push(b);
    }
    return Object.values(map);
  }

  function calcularTotal(reserva) {
    return reserva.boletos.reduce((s, b) => s + b.precio, 0);
  }

  function goToCheckout() {
    navigateTo('checkout');
  }
</script>

<div class="carrito">
  <div class="carrito__container">

    <div class="carrito__header">
      <button class="carrito__back" on:click={() => navigateTo('home')}>
        Continuar comprando
      </button>
      <h1 class="carrito__title">Carrito de pasajeros</h1>
      {#if !cargando}
        <p class="carrito__subtitle">{reservas.length} reservación{reservas.length !== 1 ? 'es' : ''} pendiente{reservas.length !== 1 ? 's' : ''}</p>
      {/if}
    </div>

    {#if cargando}
      <div style="text-align:center; padding: 3rem; color: #9c8a78;">Cargando reservaciones...</div>

    {:else if errorMsg}
      <div style="text-align:center; padding: 2rem; color:#dc2626;">{errorMsg}</div>

    {:else if reservas.length === 0}
      <div class="carrito__empty">
        <p class="carrito__empty-message">No tienes reservaciones pendientes</p>
        <button class="carrito__empty-btn" on:click={() => navigateTo('home')}>
          Buscar vuelos
        </button>
      </div>

    {:else}
      <div class="carrito__content">
        <div class="carrito__items">

          {#each reservas as reserva}
            {@const grupos = agruparPorVuelo(reserva.boletos)}

            <div class="cart-item">
              <!-- Header de la reserva -->
              <div class="cart-item__header">
                <div style="display:flex; flex-direction:column; gap:0.2rem;">
                  <span class="cart-item__type-badge">{reserva.noReservacion}</span>
                  <span style="font-size:0.78rem; color:#9c8a78;">Creada {formatFecha(reserva.fechaCreacion)}</span>
                </div>
                <span style="font-weight:700; color:#c9a96e; font-size:1.05rem;">{formatPrecio(reserva.total)}</span>
              </div>

              <!-- Vuelos de la reserva -->
              {#each grupos as grupo}
                {@const b0 = grupo.vuelo}
                <div class="cart-item__content">
                  <div class="cart-item__flight-info">
                    <h3 class="cart-item__flight-number">{b0.numeroVuelo}</h3>
                    <p class="cart-item__route">
                      {b0.origenCodigo} → {b0.destinoCodigo}
                    </p>
                  </div>

                  <div class="cart-item__details">
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Fecha vuelo</span>
                      <span class="cart-item__detail-value">{formatFecha(b0.fechaVuelo)}</span>
                    </div>
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Horario</span>
                      <span class="cart-item__detail-value">{formatHora(b0.horaSalida)} – {formatHora(b0.horaLlegada)}</span>
                    </div>
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Avión</span>
                      <span class="cart-item__detail-value">{b0.avionMarca} {b0.avionModelo}</span>
                    </div>
                    <div class="cart-item__detail">
                      <span class="cart-item__detail-label">Clase</span>
                      <span class="cart-item__detail-value cart-item__detail-value--class">{b0.clase}</span>
                    </div>
                  </div>

                  <!-- Boletos / pasajeros -->
                  <div style="margin-top:0.75rem;">
                    {#each grupo.boletos as boleto}
                      <div style="display:flex; justify-content:space-between; align-items:center; padding:0.4rem 0; border-top:1px solid #f0e8da; font-size:0.85rem;">
                        <div>
                          <span style="font-weight:600; color:#2c1810;">Asiento {boleto.noAsiento}</span>
                          {#if boleto.pasajero}
                            <span style="color:#9c8a78; margin-left:0.5rem;">
                              — {boleto.pasajero.nombre} {boleto.pasajero.apellido}
                            </span>
                          {:else}
                            <span style="color:#f97316; margin-left:0.5rem;">Sin pasajero asignado</span>
                          {/if}
                        </div>
                        <span style="color:#c9a96e; font-weight:700;">{formatPrecio(boleto.precio)}</span>
                      </div>
                    {/each}
                  </div>
                </div>
              {/each}
            </div>
          {/each}

        </div>

        <!-- Sidebar resumen -->
        <aside class="carrito__summary">
          <div class="cart-summary">
            <h2 class="cart-summary__title">Resumen</h2>

            <div class="cart-summary__items">
              {#each reservas as reserva}
                <div class="summary-item">
                  <span class="summary-item__description">{reserva.noReservacion}</span>
                  <span class="summary-item__price">{formatPrecio(reserva.total)}</span>
                </div>
              {/each}
            </div>

            <div class="cart-summary__divider"></div>

            <div class="cart-summary__total">
              <span class="cart-summary__total-label">Total</span>
              <span class="cart-summary__total-value">
                {formatPrecio(reservas.reduce((s, r) => s + r.total, 0))}
              </span>
            </div>

            <button class="cart-summary__btn-checkout" on:click={goToCheckout}>
              Proceder al pago
            </button>

            <p class="cart-summary__note">
              Completa el pago antes de que expiren tus reservaciones
            </p>
          </div>
        </aside>
      </div>
    {/if}

  </div>
</div>