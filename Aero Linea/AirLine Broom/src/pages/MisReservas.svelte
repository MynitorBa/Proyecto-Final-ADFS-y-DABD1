<script>
// @ts-nocheck
  import '../styles/misreservaciones.css';
  import DetallesReservaModal from './DetallesReserva.svelte';
  import { onMount } from 'svelte';

  export let navigateTo;

  let showDetailModal = false;
  let detailReservation = null;
  let usuarioId = null;
  let loading = true;
  let error = null;
  
  let reservasActivas = [];
  let reservasFinalizadas = [];
  
  let cancelando = false;
  let errorCancelacion = null;

  onMount(async () => {
    const isLoggedIn = !!sessionStorage.getItem('usuarioId');
    if (!isLoggedIn) {
      navigateTo('acceso-denegado');
      return;
    }
    
    usuarioId = parseInt(sessionStorage.getItem('usuarioId'));
    await cargarReservaciones();
  });

  async function cargarReservaciones() {
    loading = true;
    error = null;
    
    try {
      const response = await fetch(`http://localhost:5190/api/mis-reservaciones/usuario/${usuarioId}`);
      
      if (!response.ok) {
        throw new Error('Error al cargar las reservaciones');
      }
      
      const reservaciones = await response.json();
      console.log('Reservaciones cargadas:', reservaciones);
      
      // Separar reservaciones activas (Pendiente o Confirmada) de finalizadas (Cancelada, Expirada)
      reservasActivas = reservaciones.filter(r => 
        r.estadoReservaId === 1 || r.estadoReservaId === 2
      );
      
      reservasFinalizadas = reservaciones.filter(r => 
        r.estadoReservaId === 3 || r.estadoReservaId === 4
      );
      
    } catch (err) {
      console.error('Error cargando reservaciones:', err);
      error = 'No se pudieron cargar las reservaciones. Intenta de nuevo.';
    } finally {
      loading = false;
    }
  }

  function getStatusClass(estadoReserva) {
    const statusMap = {
      'Pendiente': 'reserva-card__status--pending',
      'Confirmada': 'reserva-card__status--confirmed',
      'Cancelada': 'reserva-card__status--cancelled',
      'Expirada': 'reserva-card__status--expired'
    };
    return statusMap[estadoReserva] || '';
  }

  function getStatusText(estadoReserva) {
    return estadoReserva;
  }

  function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit' 
    });
  }

  function formatTime(timeSpan) {
    if (!timeSpan) return '';
    const parts = timeSpan.split(':');
    return `${parts[0]}:${parts[1]}`;
  }

  function formatDuration(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  function viewDetails(reserva) {
    detailReservation = reserva;
    showDetailModal = true;
  }

  function closeModal() {
    showDetailModal = false;
    detailReservation = null;
  }

  function handleDownloadTicket(reservacionId) {
    console.log('Descargar boleto para reserva:', reservacionId);
    // Funcion pendiente - proximamente
  }

  async function handleCancelReservation(reservacionId) {
    if (!confirm('¿Estas seguro de que deseas cancelar esta reservacion? Esta accion no se puede deshacer.')) {
      return;
    }
    
    cancelando = true;
    errorCancelacion = null;
    
    try {
      const response = await fetch(
        `http://localhost:5190/api/mis-reservaciones/${reservacionId}/cancelar/usuario/${usuarioId}`,
        { method: 'POST' }
      );
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Error al cancelar la reservacion');
      }
      
      alert('Reservacion cancelada exitosamente');
      await cargarReservaciones();
      
    } catch (err) {
      console.error('Error cancelando reservacion:', err);
      errorCancelacion = err.message;
      alert(errorCancelacion);
    } finally {
      cancelando = false;
    }
  }

  function getTipoReserva(boletos) {
    if (!boletos || boletos.length === 0) return 'Sin vuelos';
    
    const origenes = new Set(boletos.map(b => b.origenCodigo));
    const destinos = new Set(boletos.map(b => b.destinoCodigo));
    
    if (origenes.size > 1 && destinos.size > 1) {
      return 'Ida y Vuelta';
    }
    return 'Solo Ida';
  }

  function agruparVuelosPorRuta(boletos) {
    if (!boletos || boletos.length === 0) return [];
    
    const vuelos = {};
    
    boletos.forEach(boleto => {
      const key = `${boleto.vueloId}-${boleto.origenCodigo}-${boleto.destinoCodigo}`;
      
      if (!vuelos[key]) {
        vuelos[key] = {
          vueloId: boleto.vueloId,
          numeroVuelo: boleto.numeroVuelo,
          origen: boleto.origenCiudad,
          origenCodigo: boleto.origenCodigo,
          destino: boleto.destinoCiudad,
          destinoCodigo: boleto.destinoCodigo,
          fecha: boleto.fechaVuelo,
          horaSalida: boleto.horaSalida,
          horaLlegada: boleto.horaLlegada,
          duracion: boleto.duracionMinutos,
          avion: `${boleto.avionMarca} ${boleto.avionModelo}`,
          clase: boleto.clase,
          cantidadPasajeros: 0
        };
      }
      
      vuelos[key].cantidadPasajeros++;
    });
    
    return Object.values(vuelos);
  }

  function obtenerPasajerosUnicos(boletos) {
    if (!boletos || boletos.length === 0) return [];
    
    const pasajerosMap = new Map();
    
    boletos.forEach(boleto => {
      if (boleto.pasajero && boleto.pasajero.id) {
        if (!pasajerosMap.has(boleto.pasajero.id)) {
          pasajerosMap.set(boleto.pasajero.id, boleto.pasajero);
        }
      }
    });
    
    return Array.from(pasajerosMap.values());
  }
</script>

{#if showDetailModal && detailReservation}
  <DetallesReservaModal reservation={detailReservation} onClose={closeModal} />
{/if}

<div class="mis-reservas">
  <div class="mis-reservas__container">
    <div class="mis-reservas__header">
      <button class="mis-reservas__back" on:click={() => navigateTo('home')}>
        Volver al inicio
      </button>
      <h1 class="mis-reservas__title">Mis Reservas</h1>
      <p class="mis-reservas__subtitle">Gestiona todas tus reservas de vuelo</p>
    </div>

    {#if loading}
      <div class="loading-state">
        <p>Cargando tus reservaciones...</p>
      </div>
    {:else if error}
      <div class="error-state">
        <p>{error}</p>
        <button class="action-btn action-btn--primary" on:click={cargarReservaciones}>
          Reintentar
        </button>
      </div>
    {:else}
      <div class="mis-reservas__content">
        <section class="reservas-section">
          <h2 class="reservas-section__title">Reservas Activas</h2>
          <p class="reservas-section__subtitle">
            {reservasActivas.length} reserva{reservasActivas.length !== 1 ? 's' : ''} activa{reservasActivas.length !== 1 ? 's' : ''}
          </p>

          {#if reservasActivas.length === 0}
            <div class="empty-state">
              <p>No tienes reservas activas en este momento</p>
              <button class="action-btn action-btn--primary" on:click={() => navigateTo('home')}>
                Buscar Vuelos
              </button>
            </div>
          {:else}
            <div class="reservas-grid">
              {#each reservasActivas as reserva}
                {@const vuelos = agruparVuelosPorRuta(reserva.boletos)}
                {@const pasajeros = obtenerPasajerosUnicos(reserva.boletos)}
                
                <article class="reserva-card">
                  <div class="reserva-card__header">
                    <div class="reserva-card__id-section">
                      <h3 class="reserva-card__id">#{reserva.noReservacion}</h3>
                      <span class="reserva-card__type">
                        {getTipoReserva(reserva.boletos)}
                      </span>
                    </div>
                    <span class="reserva-card__status {getStatusClass(reserva.estadoReserva)}">
                      {getStatusText(reserva.estadoReserva)}
                    </span>
                  </div>

                  <div class="reserva-card__flights">
                    {#each vuelos as vuelo}
                      <div class="flight-info">
                        <div class="flight-info__badge">
                          Vuelo
                        </div>
                        <div class="flight-info__main">
                          <div class="flight-info__route">
                            <span class="flight-info__airport">{vuelo.origen} ({vuelo.origenCodigo})</span>
                            <span class="flight-info__arrow">→</span>
                            <span class="flight-info__airport">{vuelo.destino} ({vuelo.destinoCodigo})</span>
                          </div>
                          <div class="flight-info__details">
                            <span class="flight-info__detail">{vuelo.avion} - {vuelo.numeroVuelo}</span>
                            <span class="flight-info__detail">{formatDate(vuelo.fecha)}</span>
                            <span class="flight-info__detail">{formatTime(vuelo.horaSalida)} - {formatTime(vuelo.horaLlegada)}</span>
                            <span class="flight-info__detail">Clase {vuelo.clase}</span>
                            <span class="flight-info__detail">Duracion: {formatDuration(vuelo.duracion)}</span>
                          </div>
                        </div>
                      </div>
                    {/each}
                  </div>

                  <div class="reserva-card__passengers">
                    <h4 class="reserva-card__passengers-title">Pasajeros ({pasajeros.length})</h4>
                    <ul class="passengers-list">
                      {#if pasajeros.length > 0}
                        {#each pasajeros as pasajero}
                          <li class="passenger-item">
                            <span class="passenger-item__name">{pasajero.nombre} {pasajero.apellido}</span>
                            <span class="passenger-item__doc">Pasaporte: {pasajero.pasaporte}</span>
                          </li>
                        {/each}
                      {:else}
                        <li class="passenger-item">
                          <span class="passenger-item__name">Datos de pasajeros pendientes</span>
                        </li>
                      {/if}
                    </ul>
                  </div>

                  <div class="reserva-card__details">
                    <div class="detail-row">
                      <span class="detail-row__label">Codigo de confirmacion</span>
                      <span class="detail-row__value">{reserva.noReservacion}</span>
                    </div>
                    <div class="detail-row">
                      <span class="detail-row__label">Fecha de reserva</span>
                      <span class="detail-row__value">{formatDate(reserva.fechaCreacion)}</span>
                    </div>
                    {#if reserva.fechaExpiracion}
                      <div class="detail-row">
                        <span class="detail-row__label">Expira el</span>
                        <span class="detail-row__value">{formatDate(reserva.fechaExpiracion)}</span>
                      </div>
                    {/if}
                    <div class="detail-row detail-row--total">
                      <span class="detail-row__label">Total</span>
                      <span class="detail-row__value">${reserva.total.toFixed(2)}</span>
                    </div>
                  </div>

                  <div class="reserva-card__actions">
                    <button class="action-btn action-btn--primary" on:click={() => viewDetails(reserva)}>
                      Ver Detalles
                    </button>
                    <button class="action-btn" on:click={() => handleDownloadTicket(reserva.reservacionId)}>
                      Descargar Boleto
                    </button>
                    <button 
                      class="action-btn" 
                      on:click={() => handleCancelReservation(reserva.reservacionId)}
                      disabled={cancelando}>
                      {cancelando ? 'Cancelando...' : 'Cancelar Reserva'}
                    </button>
                  </div>
                </article>
              {/each}
            </div>
          {/if}
        </section>

        <section class="reservas-section">
          <h2 class="reservas-section__title">Historial</h2>
          <p class="reservas-section__subtitle">Reservas completadas, canceladas y expiradas</p>

          {#if reservasFinalizadas.length === 0}
            <div class="empty-state">
              <p>No tienes reservas en tu historial</p>
            </div>
          {:else}
            <div class="reservas-grid">
              {#each reservasFinalizadas as reserva}
                {@const vuelos = agruparVuelosPorRuta(reserva.boletos)}
                {@const pasajeros = obtenerPasajerosUnicos(reserva.boletos)}
                
                <article class="reserva-card reserva-card--completed">
                  <div class="reserva-card__header">
                    <div class="reserva-card__id-section">
                      <h3 class="reserva-card__id">#{reserva.noReservacion}</h3>
                      <span class="reserva-card__type">
                        {getTipoReserva(reserva.boletos)}
                      </span>
                    </div>
                    <span class="reserva-card__status {getStatusClass(reserva.estadoReserva)}">
                      {getStatusText(reserva.estadoReserva)}
                    </span>
                  </div>

                  <div class="reserva-card__flights">
                    {#each vuelos as vuelo}
                      <div class="flight-info">
                        <div class="flight-info__badge">
                          Vuelo
                        </div>
                        <div class="flight-info__main">
                          <div class="flight-info__route">
                            <span class="flight-info__airport">{vuelo.origen} ({vuelo.origenCodigo})</span>
                            <span class="flight-info__arrow">→</span>
                            <span class="flight-info__airport">{vuelo.destino} ({vuelo.destinoCodigo})</span>
                          </div>
                          <div class="flight-info__details">
                            <span class="flight-info__detail">{vuelo.avion} - {vuelo.numeroVuelo}</span>
                            <span class="flight-info__detail">{formatDate(vuelo.fecha)}</span>
                            <span class="flight-info__detail">Clase {vuelo.clase}</span>
                          </div>
                        </div>
                      </div>
                    {/each}
                  </div>

                  <div class="reserva-card__passengers">
                    <h4 class="reserva-card__passengers-title">Pasajeros ({pasajeros.length})</h4>
                    <ul class="passengers-list">
                      {#if pasajeros.length > 0}
                        {#each pasajeros as pasajero}
                          <li class="passenger-item">
                            <span class="passenger-item__name">{pasajero.nombre} {pasajero.apellido}</span>
                            <span class="passenger-item__doc">Pasaporte: {pasajero.pasaporte}</span>
                          </li>
                        {/each}
                      {:else}
                        <li class="passenger-item">
                          <span class="passenger-item__name">Sin datos de pasajeros</span>
                        </li>
                      {/if}
                    </ul>
                  </div>

                  <div class="reserva-card__details">
                    <div class="detail-row">
                      <span class="detail-row__label">Fecha de reserva</span>
                      <span class="detail-row__value">{formatDate(reserva.fechaCreacion)}</span>
                    </div>
                    <div class="detail-row detail-row--total">
                      <span class="detail-row__label">Total</span>
                      <span class="detail-row__value">${reserva.total.toFixed(2)}</span>
                    </div>
                  </div>

                  <div class="reserva-card__actions">
                    <button class="action-btn action-btn--primary" on:click={() => viewDetails(reserva)}>
                      Ver Detalles
                    </button>
                    <button class="action-btn" on:click={() => handleDownloadTicket(reserva.reservacionId)}>
                      Descargar Recibo
                    </button>
                  </div>
                </article>
              {/each}
            </div>
          {/if}
        </section>
      </div>
    {/if}
  </div>
</div>