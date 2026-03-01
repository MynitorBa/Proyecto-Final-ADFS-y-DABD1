<script>
  // @ts-nocheck
  import '../styles/pasajeros.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;

  const API = 'https://localhost:7107';

  // ── Sesión desde store (igual que Profile) ──
  let usuarioId = null;
  const unsubscribe = sesion.subscribe(s => { usuarioId = s?.usuarioId ?? null; });

  let loading = true;
  let error = null;
  let reservacionesPendientes = [];
  let todosLosBoletos = [];
  let passengerData = {};
  let currentPassengerIndex = 0;
  let submitting = false;

  let todosLosPaises = [];
  let paisQueries = {};
  let paisesSugeridos = {};
  let paisesSeleccionados = {};
  let ciudadQueries = {};
  let ciudadesSugeridas = {};
  let ciudadesSeleccionadas = {};

  onMount(async () => {
    if (!usuarioId) {
      navigateTo('login');
      return;
    }

    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch (err) {
      console.error('Error cargando países:', err);
    }

    await cargarReservacionesPendientes();

    return () => unsubscribe();
  });

  async function cargarReservacionesPendientes() {
    loading = true;
    error = null;

    try {
      const response = await fetch(`${API}/api/mis-reservaciones`, {
        credentials: 'include'
      });

      if (!response.ok) throw new Error('Error al cargar las reservaciones');

      const reservaciones = await response.json();

      reservacionesPendientes = reservaciones.filter(r => r.estadoReservaId === 1);

      todosLosBoletos = [];
      let todosTienenPasajero = true;

      reservacionesPendientes.forEach(reserva => {
        reserva.boletos.forEach(boleto => {
          todosLosBoletos.push({
            ...boleto,
            reservacionId: reserva.reservacionId,
            noReservacion: reserva.noReservacion
          });
          if (!boleto.pasajero || !boleto.pasajero.id) {
            todosTienenPasajero = false;
          }
        });
      });

      // Si todos ya tienen pasajero → ir al carrito
      if (todosLosBoletos.length > 0 && todosTienenPasajero) {
        navigateTo('carrito');
        return;
      }

      todosLosBoletos.forEach(boleto => {
        passengerData[boleto.boletoId] = {
          boletoId:  boleto.boletoId,
          nombre:    '',
          apellido:  '',
          pasaporte: '',
          telefono:  '',
          pais:      '',
          ciudad:    ''
        };
        paisQueries[boleto.boletoId]          = '';
        paisesSugeridos[boleto.boletoId]      = [];
        paisesSeleccionados[boleto.boletoId]  = null;
        ciudadQueries[boleto.boletoId]        = '';
        ciudadesSugeridas[boleto.boletoId]    = [];
        ciudadesSeleccionadas[boleto.boletoId] = false;
      });

    } catch (err) {
      console.error('Error cargando reservaciones:', err);
      error = 'No se pudieron cargar las reservaciones pendientes.';
    } finally {
      loading = false;
    }
  }

  function onPaisInput(boletoId) {
    const q = paisQueries[boletoId].toLowerCase();
    paisesSugeridos[boletoId] = q.length < 2 ? [] :
      todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    paisesSugeridos = { ...paisesSugeridos };
    if (paisQueries[boletoId] && !paisesSeleccionados[boletoId])
      passengerData[boletoId].pais = '';
  }

  function seleccionarPais(boletoId, pais) {
    paisesSeleccionados[boletoId]     = pais;
    paisQueries[boletoId]             = pais.country;
    passengerData[boletoId].pais      = pais.country;
    paisesSugeridos[boletoId]         = [];
    ciudadQueries[boletoId]           = '';
    passengerData[boletoId].ciudad    = '';
    ciudadesSugeridas[boletoId]       = [];
    ciudadesSeleccionadas[boletoId]   = false;
    paisQueries          = { ...paisQueries };
    paisesSeleccionados  = { ...paisesSeleccionados };
    paisesSugeridos      = { ...paisesSugeridos };
    ciudadQueries        = { ...ciudadQueries };
    ciudadesSugeridas    = { ...ciudadesSugeridas };
    ciudadesSeleccionadas = { ...ciudadesSeleccionadas };
  }

  function validarPaisSeleccionado(boletoId) {
    if (paisQueries[boletoId] && !paisesSeleccionados[boletoId]) {
      paisQueries[boletoId] = '';
      paisQueries = { ...paisQueries };
    }
  }

  function onCiudadInput(boletoId) {
    if (!paisesSeleccionados[boletoId]) return;
    const q = ciudadQueries[boletoId].toLowerCase();
    ciudadesSugeridas[boletoId] = q.length < 2 ? [] :
      paisesSeleccionados[boletoId].cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    ciudadesSugeridas = { ...ciudadesSugeridas };
    if (ciudadQueries[boletoId] && !ciudadesSeleccionadas[boletoId])
      passengerData[boletoId].ciudad = '';
  }

  function seleccionarCiudad(boletoId, ciudad) {
    ciudadQueries[boletoId]           = ciudad;
    passengerData[boletoId].ciudad    = ciudad;
    ciudadesSugeridas[boletoId]       = [];
    ciudadesSeleccionadas[boletoId]   = true;
    ciudadQueries        = { ...ciudadQueries };
    ciudadesSugeridas    = { ...ciudadesSugeridas };
    ciudadesSeleccionadas = { ...ciudadesSeleccionadas };
  }

  function validarCiudadSeleccionada(boletoId) {
    if (ciudadQueries[boletoId] && !ciudadesSeleccionadas[boletoId]) {
      ciudadQueries[boletoId] = '';
      ciudadQueries = { ...ciudadQueries };
    }
  }

  function handleNext()     { if (currentPassengerIndex < todosLosBoletos.length - 1) currentPassengerIndex++; }
  function handlePrevious() { if (currentPassengerIndex > 0) currentPassengerIndex--; }

  async function handleSubmit() {
    submitting = true;

    try {
      const boletosAgrupados = {};
      todosLosBoletos.forEach(boleto => {
        if (!boletosAgrupados[boleto.reservacionId])
          boletosAgrupados[boleto.reservacionId] = [];
        boletosAgrupados[boleto.reservacionId].push(passengerData[boleto.boletoId]);
      });

      for (const reservacionId in boletosAgrupados) {
        for (const p of boletosAgrupados[reservacionId]) {
          if (!p.nombre || !p.apellido || !p.pasaporte || !p.telefono || !p.pais || !p.ciudad) {
            alert('Por favor completa todos los campos de todos los pasajeros');
            submitting = false;
            return;
          }
        }
      }

      const promises = Object.entries(boletosAgrupados).map(([reservacionId, body]) =>
        fetch(`${API}/api/reservaciones/${reservacionId}/pasajeros`, {
          method: 'PUT',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        })
      );

      const responses = await Promise.all(promises);
      const allSuccess = responses.every(r => r.ok);

      if (allSuccess) {
        navigateTo('carrito');  // ← carrito, no checkout
      } else {
        alert('Hubo un error al procesar algunos datos. Por favor intenta de nuevo.');
      }

    } catch (err) {
      console.error('Error al enviar datos de pasajeros:', err);
      alert('Error al enviar los datos. Por favor intenta de nuevo.');
    } finally {
      submitting = false;
    }
  }

  function formatDate(d) {
    if (!d) return '';
    return new Date(d).toLocaleDateString('es-ES', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  function formatTime(t) {
    if (!t) return '';
    const p = t.split(':');
    return `${p[0]}:${p[1]}`;
  }

  $: currentBoleto    = todosLosBoletos[currentPassengerIndex];
  $: isFirstPassenger = currentPassengerIndex === 0;
  $: isLastPassenger  = currentPassengerIndex === todosLosBoletos.length - 1;
</script>

<div class="datos-pasajeros">
  <div class="datos-pasajeros__container">
    <div class="datos-pasajeros__header">
      <button class="datos-pasajeros__back" on:click={() => navigateTo('home')}>
        Volver al inicio
      </button>
      <h1 class="datos-pasajeros__title">Datos de los pasajeros</h1>
      <p class="datos-pasajeros__subtitle">
        Completa la información de todos los pasajeros para tus reservaciones pendientes
      </p>
    </div>

    <div class="datos-pasajeros__content">
      {#if loading}
        <div class="datos-pasajeros__loading">Cargando reservaciones pendientes...</div>

      {:else if error}
        <div class="datos-pasajeros__error">{error}</div>

      {:else if todosLosBoletos.length === 0}
        <div class="datos-pasajeros__empty">
          <p>No tienes reservaciones pendientes por completar.</p>
          <button class="action-btn action-btn--primary" on:click={() => navigateTo('home')}>
            Buscar Vuelos
          </button>
        </div>

      {:else}
        <div class="datos-pasajeros__main">
          <div class="datos-pasajeros__notice">
            <h3 class="notice__title">Información importante</h3>
            <ul class="notice__list">
              <li class="notice__item">Los nombres deben coincidir exactamente con el pasaporte</li>
              <li class="notice__item">Verifica que los datos sean correctos antes de enviar</li>
              <li class="notice__item">Tienes 10 minutos para completar los datos antes de que expire la reserva</li>
            </ul>
          </div>

          <div class="passenger-tabs">
            {#each todosLosBoletos as boleto, index}
              <button
                class="passenger-tab"
                class:passenger-tab--active={index === currentPassengerIndex}
                class:passenger-tab--completed={index < currentPassengerIndex}
                on:click={() => currentPassengerIndex = index}
              >
                <span class="passenger-tab__number">{index + 1}</span>
                <span class="passenger-tab__label">Boleto {index + 1}</span>
              </button>
            {/each}
          </div>

          <form class="passengers-form" on:submit|preventDefault={handleSubmit}>
            <section class="flight-passengers-section">
              <div class="flight-passengers-section__header">
                <h2 class="flight-passengers-section__title">
                  Vuelo {currentBoleto.numeroVuelo} - {currentBoleto.clase}
                </h2>
                <p class="flight-passengers-section__info">
                  {currentBoleto.origenCiudad} ({currentBoleto.origenCodigo}) → {currentBoleto.destinoCiudad} ({currentBoleto.destinoCodigo})
                </p>
                <p class="flight-passengers-section__info">
                  {formatDate(currentBoleto.fechaVuelo)} | Salida: {formatTime(currentBoleto.horaSalida)} - Llegada: {formatTime(currentBoleto.horaLlegada)}
                </p>
                <p class="flight-passengers-section__info">
                  <strong>Reservación:</strong> {currentBoleto.noReservacion} |
                  <strong>Boleto:</strong> {currentBoleto.noBoleto} |
                  <strong>Asiento:</strong> {currentBoleto.noAsiento}
                </p>
              </div>

              <article class="passenger-form-card">
                <h3 class="passenger-form-card__title">
                  Datos del Pasajero {currentPassengerIndex + 1} de {todosLosBoletos.length}
                </h3>

                <div class="passenger-form-card__content">
                  <div class="form-row">
                    <div class="form-field">
                      <label for="nombre-{currentBoleto.boletoId}" class="form-field__label">Nombre *</label>
                      <input
                        type="text"
                        id="nombre-{currentBoleto.boletoId}"
                        class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].nombre}
                        placeholder="Nombre(s)"
                        autocomplete="off"
                        required
                      />
                    </div>
                    <div class="form-field">
                      <label for="apellido-{currentBoleto.boletoId}" class="form-field__label">Apellido *</label>
                      <input
                        type="text"
                        id="apellido-{currentBoleto.boletoId}"
                        class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].apellido}
                        placeholder="Apellido(s)"
                        autocomplete="off"
                        required
                      />
                    </div>
                  </div>

                  <div class="form-row">
                    <div class="form-field">
                      <label for="pasaporte-{currentBoleto.boletoId}" class="form-field__label">Número de Pasaporte *</label>
                      <input
                        type="text"
                        id="pasaporte-{currentBoleto.boletoId}"
                        class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].pasaporte}
                        placeholder="A12345678"
                        autocomplete="off"
                        required
                      />
                    </div>
                    <div class="form-field">
                      <label for="telefono-{currentBoleto.boletoId}" class="form-field__label">Teléfono de contacto *</label>
                      <input
                        type="tel"
                        id="telefono-{currentBoleto.boletoId}"
                        class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].telefono}
                        placeholder="+502 1234-5678"
                        autocomplete="off"
                        required
                      />
                    </div>
                  </div>

                  <div class="form-row">
                    <!-- País con autocomplete -->
                    <div class="form-field">
                      <label for="pais-{currentBoleto.boletoId}" class="form-field__label">País *</label>
                      <div class="autocomplete">
                        <input
                          type="text"
                          id="pais-{currentBoleto.boletoId}"
                          class="form-field__input"
                          bind:value={paisQueries[currentBoleto.boletoId]}
                          on:input={() => onPaisInput(currentBoleto.boletoId)}
                          on:blur={() => validarPaisSeleccionado(currentBoleto.boletoId)}
                          placeholder="Escribe tu país..."
                          autocomplete="off"
                          required
                        />
                        {#if paisesSugeridos[currentBoleto.boletoId]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each paisesSugeridos[currentBoleto.boletoId] as pais}
                              <li class="autocomplete__item">
                                <button
                                  type="button"
                                  class="autocomplete__btn"
                                  on:click={() => seleccionarPais(currentBoleto.boletoId, pais)}
                                >
                                  {pais.country}
                                </button>
                              </li>
                            {/each}
                          </ul>
                        {/if}
                      </div>
                    </div>

                    <!-- Ciudad con autocomplete -->
                    <div class="form-field">
                      <label for="ciudad-{currentBoleto.boletoId}" class="form-field__label">Ciudad *</label>
                      <div class="autocomplete">
                        <input
                          type="text"
                          id="ciudad-{currentBoleto.boletoId}"
                          class="form-field__input"
                          bind:value={ciudadQueries[currentBoleto.boletoId]}
                          on:input={() => onCiudadInput(currentBoleto.boletoId)}
                          on:blur={() => validarCiudadSeleccionada(currentBoleto.boletoId)}
                          placeholder={paisesSeleccionados[currentBoleto.boletoId] ? 'Escribe tu ciudad...' : 'Primero selecciona un país'}
                          disabled={!paisesSeleccionados[currentBoleto.boletoId]}
                          autocomplete="off"
                          required
                        />
                        {#if ciudadesSugeridas[currentBoleto.boletoId]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each ciudadesSugeridas[currentBoleto.boletoId] as ciudad}
                              <li class="autocomplete__item">
                                <button
                                  type="button"
                                  class="autocomplete__btn"
                                  on:click={() => seleccionarCiudad(currentBoleto.boletoId, ciudad)}
                                >
                                  {ciudad}
                                </button>
                              </li>
                            {/each}
                          </ul>
                        {/if}
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            </section>

            <div class="passengers-form__navigation">
              <button
                type="button"
                class="passengers-form__btn-prev"
                on:click={handlePrevious}
                disabled={isFirstPassenger}
              >
                Anterior
              </button>

              {#if isLastPassenger}
                <button type="submit" class="passengers-form__btn-submit" disabled={submitting}>
                  {submitting ? 'Enviando...' : 'Confirmar Datos'}
                </button>
              {:else}
                <button type="button" class="passengers-form__btn-next" on:click={handleNext}>
                  Siguiente
                </button>
              {/if}
            </div>
          </form>
        </div>

        <aside class="datos-pasajeros__sidebar">
          <div class="booking-recap">
            <h2 class="booking-recap__title">Resumen de reservaciones pendientes</h2>
            <div class="booking-recap__flights">
              {#each reservacionesPendientes as reserva}
                <div class="recap-reservation">
                  <div class="recap-reservation__header">
                    <strong>Reservación:</strong> {reserva.noReservacion}
                  </div>
                  <div class="recap-reservation__total">
                    Total: $ {reserva.total.toFixed(2)}
                  </div>
                  <div class="recap-reservation__boletos">
                    {reserva.boletos.length} boleto{reserva.boletos.length > 1 ? 's' : ''}
                  </div>
                </div>
              {/each}
            </div>
            <div class="booking-recap__divider"></div>
            <div class="booking-recap__help">
              <h3 class="booking-recap__help-title">¿Necesitas ayuda?</h3>
              <p class="booking-recap__help-text">Contáctanos al +502 2345-6789</p>
            </div>
          </div>
        </aside>
      {/if}
    </div>
  </div>
</div>