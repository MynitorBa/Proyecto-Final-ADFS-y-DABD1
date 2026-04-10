<script>
/**
 * @file DetallesReserva.svelte
 * @description Componente modal que muestra los detalles completos de una sola reservacion.
 * Muestra segmentos de vuelo agrupados por ruta, informacion de pasajeros, resumen de pago
 * y un formulario de calificacion/comentario para reservaciones completadas. Aparece como
 * una superposicion sobre la pagina MisReservas cuando el usuario hace clic en una tarjeta
 * de reservacion.
 */
  import '../styles/detallereserva.css';
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';

  /** El objeto de reservacion a mostrar, que contiene boletos, estado, total y metadatos. @type {object} */
  export let reservation;

  /** Funcion de callback invocada cuando el modal debe cerrarse. @type {function} */
  export let onClose;

  /** ID del usuario autenticado actualmente, leido desde sessionStorage al montar. @type {number|null} */
  let usuarioId = null;

  /** Objeto de comentario existente para la ruta si el usuario ya comento, de lo contrario null. @type {object|null} */
  let comentarioExistente = null;

  /** Verdadero mientras se obtiene el comentario existente desde la API. @type {boolean} */
  let cargandoComentario = false;

  /** Verdadero mientras una solicitud POST de nuevo comentario esta en progreso. @type {boolean} */
  let enviandoComentario = false;

  /** Estado borrador de un nuevo comentario que el usuario esta redactando. @type {{cantidadEstrellas: number, contenido: string}} */
  let nuevoComentario = {
    cantidadEstrellas: 5,
    contenido: ''
  };

  onMount(async () => {
    usuarioId = parseInt(sessionStorage.getItem('usuarioId'));

    if (reservation.estadoReservaId === 2 && reservation.boletos.length > 0) {
      await verificarComentarioExistente();
    }
  });

  /**
   * Obtiene todos los comentarios para la ruta del primer boleto y verifica si el usuario
   * actual ya publico uno. Establece comentarioExistente si se encuentra. Requiere al menos
   * un boleto en la reservacion para extraer el rutaId.
   * @async
   * @returns {Promise<void>}
   */
  async function verificarComentarioExistente() {
    cargandoComentario = true;

    try {
      if (!reservation.boletos || reservation.boletos.length === 0) {
        console.log('No hay boletos en la reservacion');
        return;
      }

      const rutaId = reservation.boletos[0].rutaId;
      console.log('Verificando comentarios para ruta:', rutaId);

      const response = await fetch(`${API}/api/comentarios/ruta/${rutaId}`);

      if (response.ok) {
        const comentarios = await response.json();
        console.log('Comentarios obtenidos:', comentarios);
        comentarioExistente = comentarios.find(c => c.usuarioId === usuarioId);

        if (comentarioExistente) {
          console.log('Ya existe un comentario de este usuario');
        }
      }
    } catch (error) {
      console.error('Error verificando comentario:', error);
    } finally {
      cargandoComentario = false;
    }
  }

  /**
   * Envia el nuevo comentario redactado en nuevoComentario al endpoint de la API POST /api/comentarios.
   * Valida que el campo contenido no este vacio antes de enviar. En caso de exito, establece
   * comentarioExistente en el comentario recien creado y restablece el formulario borrador.
   * @async
   * @returns {Promise<void>}
   */
  async function enviarComentario() {
    if (!nuevoComentario.contenido.trim()) {
      return;
    }

    enviandoComentario = true;

    try {
      if (!reservation.boletos || reservation.boletos.length === 0) {
        throw new Error('No hay boletos en la reservacion');
      }

      const rutaId = reservation.boletos[0].rutaId;

      const response = await fetch('${API}/api/comentarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          usuarioId: usuarioId,
          rutaId: rutaId,
          cantidadEstrellas: nuevoComentario.cantidadEstrellas,
          contenido: nuevoComentario.contenido
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message);
      }

      const comentarioCreado = await response.json();
      comentarioExistente = comentarioCreado;

      nuevoComentario = {
        cantidadEstrellas: 5,
        contenido: ''
      };

    } catch (error) {
      console.error('Error enviando comentario:', error);
    } finally {
      enviandoComentario = false;
    }
  }

  /**
   * Mapea una cadena de estado de reservacion a su clase CSS modificadora correspondiente
   * usada para dar estilo al badge de estado.
   * @param {string} estadoReserva - La etiqueta de estado como 'Pendiente', 'Confirmada', etc.
   * @returns {string} Una cadena de clase CSS como 'status--confirmed', o cadena vacia si es desconocido.
   */
  function getStatusClass(estadoReserva) {
    const statusMap = {
      'Pendiente': 'status--pending',
      'Confirmada': 'status--confirmed',
      'Cancelada': 'status--cancelled',
      'Expirada': 'status--expired'
    };
    return statusMap[estadoReserva] || '';
  }

  /**
   * Formatea una cadena ISO de fecha en una cadena DD/MM/YYYY localizada usando el locale es-ES.
   * Retorna una cadena vacia si el input es falsy.
   * @param {string} dateString - Cadena ISO de fecha a formatear.
   * @returns {string} Fecha formateada o cadena vacia.
   */
  function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  /**
   * Extrae la porcion HH:MM de una cadena de tiempo en formato HH:MM:SS.
   * Retorna una cadena vacia si el input es falsy.
   * @param {string} timeSpan - Cadena de tiempo en formato HH:MM:SS.
   * @returns {string} Hora en formato HH:MM o cadena vacia.
   */
  function formatTime(timeSpan) {
    if (!timeSpan) return '';
    const parts = timeSpan.split(':');
    return `${parts[0]}:${parts[1]}`;
  }

  /**
   * Convierte una duracion expresada en minutos totales a una cadena legible Xh Ym.
   * @param {number} minutes - Duracion total en minutos.
   * @returns {string} Cadena de duracion formateada como '2h 30m'.
   */
  function formatDuration(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  /**
   * Agrupa objetos de boleto individuales por su clave de vuelo unica (vueloId + origen + destino).
   * Cada grupo acumula numeros de asiento de todos los boletos pertenecientes al mismo segmento de vuelo.
   * Retorna un arreglo de objetos de vuelo cada uno con informacion de ruta, horario, avion y asientos.
   * @param {Array<object>} boletos - Arreglo de objetos de boleto de la reservacion.
   * @returns {Array<object>} Arreglo de objetos de vuelo agrupados con un arreglo asientos.
   */
  function agruparVuelosPorRuta(boletos) {
    if (!boletos || boletos.length === 0) return [];

    const vuelos = {};

    boletos.forEach(boleto => {
      const key = `${boleto.vueloId}-${boleto.origenCodigo}-${boleto.destinoCodigo}`;

      if (!vuelos[key]) {
        vuelos[key] = {
          vueloId: boleto.vueloId,
          numeroVuelo: boleto.numeroVuelo,
          origenCodigo: boleto.origenCodigo,
          origenNombre: boleto.origenNombre,
          origenCiudad: boleto.origenCiudad,
          destinoCodigo: boleto.destinoCodigo,
          destinoNombre: boleto.destinoNombre,
          destinoCiudad: boleto.destinoCiudad,
          fecha: boleto.fechaVuelo,
          horaSalida: boleto.horaSalida,
          horaLlegada: boleto.horaLlegada,
          duracion: boleto.duracionMinutos,
          avion: `${boleto.avionMarca} ${boleto.avionModelo}`,
          clase: boleto.clase,
          rutaId: boleto.rutaId,
          asientos: []
        };
      }

      vuelos[key].asientos.push(boleto.noAsiento);
    });

    return Object.values(vuelos);
  }

  /**
   * Deduplica pasajeros de todos los boletos usando un Map indexado por ID de pasajero.
   * Retorna cada objeto de pasajero unico una sola vez, incluso si aparece en multiples boletos.
   * @param {Array<object>} boletos - Arreglo de objetos de boleto, cada uno potencialmente con un sub-objeto pasajero.
   * @returns {Array<object>} Arreglo de objetos de pasajero unicos.
   */
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

  /**
   * Maneja los clics en el fondo semitransparente del modal. Invoca onClose solo cuando
   * el objetivo del clic es el propio elemento de fondo, no un elemento hijo dentro del modal.
   * @param {MouseEvent} event - El evento clic del DOM del div de fondo.
   */
  function handleBackdropClick(event) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }

  /**
   * Manejador de marcador de posicion para el boton de descarga de boleto. Actualmente registra
   * un mensaje indicando que la funcionalidad aun no esta implementada.
   */
  function handleDownloadTicket() {
    console.log('Descargar boleto - proximamente');
  }

  // Agrupa boletos por ruta de vuelo para mostrar cada segmento de vuelo una sola vez.
  $: vuelos = agruparVuelosPorRuta(reservation.boletos);

  // Produce una lista deduplicada de objetos de pasajero de todos los boletos.
  $: pasajeros = obtenerPasajerosUnicos(reservation.boletos);

  // Verdadero cuando la reservacion esta en estado confirmado (id 2) y tiene al menos un vuelo, habilitando la seccion de comentarios.
  $: puedeComentarYCalificar = reservation.estadoReservaId === 2 && vuelos.length > 0;
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<!-- Modal backdrop: clic fuera del modal lo cierra -->
<div class="modal-backdrop" on:click={handleBackdropClick}>
  <div class="detalle-reserva-modal">
    <div class="detalle-reserva__container">
      <!-- Encabezado del modal con numero de reserva y estado -->
      <div class="detalle-reserva__header">
        <button class="detalle-reserva__close" on:click={onClose}>
          Cerrar
        </button>
        <div class="detalle-reserva__header-content">
          <h1 class="detalle-reserva__title">Detalle de Reserva</h1>
          <span class="detalle-reserva__status {getStatusClass(reservation.estadoReserva)}">
            {reservation.estadoReserva}
          </span>
        </div>
        <p class="detalle-reserva__subtitle">Reserva #{reservation.noReservacion}</p>
      </div>

      <div class="detalle-reserva__content">
        <div class="detalle-reserva__main">
          <!-- Tarjeta con codigo de confirmacion para el check-in -->
          <section class="confirmation-card">
            <div class="confirmation-card__content">
              <h2 class="confirmation-card__title">Codigo de Confirmacion</h2>
              <div class="confirmation-card__code">
                <span class="confirmation-card__code-value">{reservation.noReservacion}</span>
              </div>
              <p class="confirmation-card__note">Presenta este codigo al momento del check-in</p>
            </div>
          </section>

          <!-- Detalle de segmentos de vuelo agrupados por ruta -->
          <section class="flight-details-section">
            <h2 class="section-title">Detalles de Vuelo</h2>

            {#each vuelos as vuelo}
              <article class="flight-detail-card">
                <div class="flight-detail-card__header">
                  <span class="flight-detail-card__badge">
                    Vuelo
                  </span>
                  <span class="flight-detail-card__number">{vuelo.avion} - {vuelo.numeroVuelo}</span>
                </div>

                <div class="flight-detail-card__route">
                  <div class="route-point">
                    <span class="route-point__code">{vuelo.origenCodigo}</span>
                    <span class="route-point__city">{vuelo.origenCiudad}</span>
                  </div>
                  <div class="route-arrow">
                    <svg width="60" height="24" viewBox="0 0 60 24" fill="none">
                      <path d="M0 12 L55 12 M45 4 L55 12 L45 20" stroke="currentColor" stroke-width="2"/>
                    </svg>
                  </div>
                  <div class="route-point">
                    <span class="route-point__code">{vuelo.destinoCodigo}</span>
                    <span class="route-point__city">{vuelo.destinoCiudad}</span>
                  </div>
                </div>

                <div class="flight-detail-card__info">
                  <div class="info-group">
                    <div class="info-item">
                      <span class="info-item__label">Salida</span>
                      <span class="info-item__value">{formatDate(vuelo.fecha)}</span>
                      <span class="info-item__time">{formatTime(vuelo.horaSalida)}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-item__label">Llegada</span>
                      <span class="info-item__value">{formatDate(vuelo.fecha)}</span>
                      <span class="info-item__time">{formatTime(vuelo.horaLlegada)}</span>
                    </div>
                  </div>

                  <div class="info-group">
                    <div class="info-item">
                      <span class="info-item__label">Duracion</span>
                      <span class="info-item__value">{formatDuration(vuelo.duracion)}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-item__label">Clase</span>
                      <span class="info-item__value">{vuelo.clase}</span>
                    </div>
                  </div>

                  <div class="info-group">
                    <div class="info-item">
                      <span class="info-item__label">Aeropuerto de Salida</span>
                      <span class="info-item__value">{vuelo.origenNombre}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-item__label">Aeropuerto de Llegada</span>
                      <span class="info-item__value">{vuelo.destinoNombre}</span>
                    </div>
                  </div>

                  <div class="info-group">
                    <div class="info-item">
                      <span class="info-item__label">Asientos</span>
                      <span class="info-item__value">{vuelo.asientos.join(', ')}</span>
                    </div>
                  </div>
                </div>
              </article>
            {/each}
          </section>

          <!-- Informacion de pasajeros unicos asociados a la reserva -->
          {#if pasajeros.length > 0}
            <section class="passengers-section">
              <h2 class="section-title">Informacion de Pasajeros</h2>

              <div class="passengers-grid">
                {#each pasajeros as pasajero, index}
                  <article class="passenger-detail-card">
                    <div class="passenger-detail-card__header">
                      <h3 class="passenger-detail-card__title">Pasajero {index + 1}</h3>
                    </div>

                    <div class="passenger-detail-card__content">
                      <div class="passenger-info-row">
                        <div class="passenger-info-item">
                          <span class="passenger-info-item__label">Nombre Completo</span>
                          <span class="passenger-info-item__value">{pasajero.nombre} {pasajero.apellido}</span>
                        </div>
                      </div>

                      <div class="passenger-info-row">
                        <div class="passenger-info-item">
                          <span class="passenger-info-item__label">Pasaporte</span>
                          <span class="passenger-info-item__value">{pasajero.pasaporte}</span>
                        </div>
                        <div class="passenger-info-item">
                          <span class="passenger-info-item__label">Telefono</span>
                          <span class="passenger-info-item__value">{pasajero.telefono}</span>
                        </div>
                      </div>

                      <div class="passenger-info-row">
                        <div class="passenger-info-item">
                          <span class="passenger-info-item__label">Pais</span>
                          <span class="passenger-info-item__value">{pasajero.pais}</span>
                        </div>
                        <div class="passenger-info-item">
                          <span class="passenger-info-item__label">Ciudad</span>
                          <span class="passenger-info-item__value">{pasajero.ciudad}</span>
                        </div>
                      </div>
                    </div>
                  </article>
                {/each}
              </div>
            </section>
          {/if}

          <!-- Resumen de fechas, expiracion y total pagado de la reserva -->
          <section class="payment-section">
            <h2 class="section-title">Informacion de Reserva</h2>

            <div class="payment-card">
              <div class="payment-detail">
                <span class="payment-detail__label">Fecha de Reserva</span>
                <span class="payment-detail__value">{formatDate(reservation.fechaCreacion)}</span>
              </div>
              {#if reservation.fechaExpiracion}
                <div class="payment-detail">
                  <span class="payment-detail__label">Expira el</span>
                  <span class="payment-detail__value">{formatDate(reservation.fechaExpiracion)}</span>
                </div>
              {/if}
              <div class="payment-detail payment-detail--total">
                <span class="payment-detail__label">Total Pagado</span>
                <span class="payment-detail__value">${reservation.total.toFixed(2)}</span>
              </div>
            </div>
          </section>

          <!-- Seccion de calificacion y comentario disponible para reservas confirmadas -->
          {#if puedeComentarYCalificar}
            <section class="review-section">
              <h2 class="section-title">Califica tu Experiencia</h2>

              {#if cargandoComentario}
                <div class="review-loading">
                  <p>Verificando si ya has comentado...</p>
                </div>
              {:else if comentarioExistente}
                <div class="review-existing">
                  <div class="review-existing__header">
                    <span class="review-existing__title">Tu calificacion</span>
                    <div class="review-stars review-stars--display">
                      {#each Array(5) as _, i}
                        <span class="review-star {i < comentarioExistente.cantidadEstrellas ? 'review-star--filled' : ''}">★</span>
                      {/each}
                    </div>
                  </div>
                  <p class="review-existing__content">{comentarioExistente.contenido}</p>
                  <p class="review-existing__date">Publicado el {formatDate(comentarioExistente.fecha)}</p>
                </div>
              {:else}
                <div class="review-form">
                  <div class="review-form__field">
                    <!-- svelte-ignore a11y_label_has_associated_control -->
                    <label class="review-form__label">Calificacion</label>
                    <div class="review-stars">
                      {#each Array(5) as _, i}
                        <button
                          type="button"
                          class="review-star {i < nuevoComentario.cantidadEstrellas ? 'review-star--filled' : ''}"
                          on:click={() => nuevoComentario.cantidadEstrellas = i + 1}>
                          ★
                        </button>
                      {/each}
                    </div>
                  </div>

                  <div class="review-form__field">
                    <!-- svelte-ignore a11y_label_has_associated_control -->
                    <label class="review-form__label">Tu comentario</label>
                    <textarea
                      class="review-form__textarea"
                      bind:value={nuevoComentario.contenido}
                      placeholder="Cuentanos sobre tu experiencia en este vuelo..."
                      maxlength="500"
                      rows="4"></textarea>
                    <span class="review-form__counter">{nuevoComentario.contenido.length}/500</span>
                  </div>

                  <button
                    type="button"
                    class="review-form__submit"
                    on:click={enviarComentario}
                    disabled={enviandoComentario || !nuevoComentario.contenido.trim()}>
                    {enviandoComentario ? 'Publicando...' : 'Publicar Comentario'}
                  </button>
                </div>
              {/if}
            </section>
          {/if}

          <div class="detalle-reserva__actions">
            <button class="action-btn action-btn--secondary" on:click={handleDownloadTicket}>
              Descargar Boleto
            </button>
            <button class="action-btn action-btn--outline" on:click={onClose}>
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
