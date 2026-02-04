<script>
  import '../styles/pasajeros.css';
  export let navigateTo;

  const flights = [
    {
      id: 1,
      type: 'ida',
      route: 'Ciudad de Guatemala → Paris',
      date: '2026-02-15',
      flightNumber: 'AF 1234',
      passengers: 2
    },
    {
      id: 2,
      type: 'regreso',
      route: 'Paris → Ciudad de Guatemala',
      date: '2026-02-25',
      flightNumber: 'AF 5678',
      passengers: 2
    },
  ];

  let passengerData = {};
  let currentPassengerIndex = 0;

  // Crear array de todos los pasajeros
  let allPassengers = [];
  flights.forEach(flight => {
    for (let i = 0; i < flight.passengers; i++) {
      allPassengers.push({
        flightId: flight.id,
        flightType: flight.type,
        flightNumber: flight.flightNumber,
        flightRoute: flight.route,
        flightDate: flight.date,
        passengerNumber: i + 1,
        key: `${flight.id}-${i}`
      });
    }
  });

  // Inicializar datos de pasajeros
  allPassengers.forEach(passenger => {
    passengerData[passenger.key] = {
      nombre: '',
      apellido: '',
      tipoPasaporte: 'ordinario',
      numeroDocumento: '',
      nacionalidad: '',
      fechaNacimiento: '',
      genero: '',
      email: '',
      telefono: ''
    };
  });

  function handleNext() {
    if (currentPassengerIndex < allPassengers.length - 1) {
      currentPassengerIndex++;
    }
  }

  function handlePrevious() {
    if (currentPassengerIndex > 0) {
      currentPassengerIndex--;
    }
  }

  function handleSubmit() {
    console.log('Datos de todos los pasajeros:', passengerData);
    navigateTo('carrito');
  }

  function getTotalPassengers() {
    return flights.reduce((total, flight) => total + flight.passengers, 0);
  }

  $: currentPassenger = allPassengers[currentPassengerIndex];
  $: isFirstPassenger = currentPassengerIndex === 0;
  $: isLastPassenger = currentPassengerIndex === allPassengers.length - 1;
</script>

<div class="datos-pasajeros">
  <div class="datos-pasajeros__container">
    <div class="datos-pasajeros__header">
      <button class="datos-pasajeros__back" on:click={() => navigateTo('vuelos')}>
        Volver a vuelos
      </button>
      <h1 class="datos-pasajeros__title">Datos de los pasajeros</h1>
      <p class="datos-pasajeros__subtitle">
        Completa la informacion de todos los pasajeros ({getTotalPassengers()} en total)
      </p>
    </div>

    <div class="datos-pasajeros__content">
      <div class="datos-pasajeros__main">
        <div class="datos-pasajeros__notice">
          <h3 class="notice__title">Informacion importante</h3>
          <ul class="notice__list">
            <li class="notice__item">Los nombres deben coincidir exactamente con el pasaporte</li>
            <li class="notice__item">Verifica que las fechas de nacimiento sean correctas</li>
            <li class="notice__item">Asegurate de que tu pasaporte sea valido por al menos 6 meses</li>
          </ul>
        </div>

        <!-- Progress Tabs -->
        <div class="passenger-tabs">
          {#each allPassengers as passenger, index}
            <button 
              class="passenger-tab"
              class:passenger-tab--active={index === currentPassengerIndex}
              class:passenger-tab--completed={index < currentPassengerIndex}
              on:click={() => currentPassengerIndex = index}
            >
              <span class="passenger-tab__number">{index + 1}</span>
              <span class="passenger-tab__label">Pasajero {index + 1}</span>
            </button>
          {/each}
        </div>

        <form class="passengers-form" on:submit|preventDefault={handleSubmit}>
          <section class="flight-passengers-section">
            <div class="flight-passengers-section__header">
              <h2 class="flight-passengers-section__title">
                {currentPassenger.flightType === 'ida' ? 'Vuelo de Ida' : 'Vuelo de Regreso'} - {currentPassenger.flightNumber}
              </h2>
              <p class="flight-passengers-section__info">
                {currentPassenger.flightRoute} | {currentPassenger.flightDate}
              </p>
            </div>

            <article class="passenger-form-card">
              <h3 class="passenger-form-card__title">
                Pasajero {currentPassenger.passengerNumber}
              </h3>

              <div class="passenger-form-card__content">
                <div class="form-row">
                  <div class="form-field">
                    <label for="nombre-{currentPassenger.key}" class="form-field__label">
                      Nombre
                    </label>
                    <input 
                      type="text" 
                      id="nombre-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].nombre}
                      placeholder="Nombre(s)"
                    />
                  </div>

                  <div class="form-field">
                    <label for="apellido-{currentPassenger.key}" class="form-field__label">
                      Apellido
                    </label>
                    <input 
                      type="text" 
                      id="apellido-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].apellido}
                      placeholder="Apellido(s)"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-field">
                    <label for="genero-{currentPassenger.key}" class="form-field__label">
                      Genero
                    </label>
                    <select 
                      id="genero-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].genero}
                    >
                      <option value="">Selecciona</option>
                      <option value="masculino">Masculino</option>
                      <option value="femenino">Femenino</option>
                      <option value="otro">Otro</option>
                    </select>
                  </div>

                  <div class="form-field">
                    <label for="fecha-{currentPassenger.key}" class="form-field__label">
                      Fecha de nacimiento
                    </label>
                    <input 
                      type="date" 
                      id="fecha-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].fechaNacimiento}
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-field">
                    <label for="tipo-pasaporte-{currentPassenger.key}" class="form-field__label">
                      Tipo de pasaporte
                    </label>
                    <select 
                      id="tipo-pasaporte-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].tipoPasaporte}
                    >
                      <option value="ordinario">Ordinario</option>
                      <option value="diplomatico">Diplomatico</option>
                      <option value="oficial">Oficial</option>
                    </select>
                  </div>

                  <div class="form-field">
                    <label for="documento-{currentPassenger.key}" class="form-field__label">
                      Numero de pasaporte
                    </label>
                    <input 
                      type="text" 
                      id="documento-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].numeroDocumento}
                      placeholder="A12345678"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-field">
                    <label for="nacionalidad-{currentPassenger.key}" class="form-field__label">
                      Nacionalidad
                    </label>
                    <input 
                      type="text" 
                      id="nacionalidad-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].nacionalidad}
                      placeholder="Guatemala"
                    />
                  </div>

                  <div class="form-field">
                    <label for="email-{currentPassenger.key}" class="form-field__label">
                      Email
                    </label>
                    <input 
                      type="email" 
                      id="email-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].email}
                      placeholder="correo@ejemplo.com"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-field">
                    <label for="telefono-{currentPassenger.key}" class="form-field__label">
                      Telefono de contacto
                    </label>
                    <input 
                      type="tel" 
                      id="telefono-{currentPassenger.key}"
                      class="form-field__input"
                      bind:value={passengerData[currentPassenger.key].telefono}
                      placeholder="+502 1234-5678"
                    />
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
              <button type="submit" class="passengers-form__btn-submit">
                Continuar y pagar
              </button>
            {:else}
              <button 
                type="button" 
                class="passengers-form__btn-next"
                on:click={handleNext}
              >
                Siguiente
              </button>
            {/if}
          </div>
        </form>
      </div>

      <aside class="datos-pasajeros__sidebar">
        <div class="booking-recap">
          <h2 class="booking-recap__title">Resumen de tu reserva</h2>

          <div class="booking-recap__flights">
            {#each flights as flight}
              <div class="recap-flight">
                <div class="recap-flight__header">
                  <span class="recap-flight__type">{flight.type === 'ida' ? 'Ida' : 'Regreso'}</span>
                  <span class="recap-flight__number">{flight.flightNumber}</span>
                </div>
                <p class="recap-flight__route">{flight.route}</p>
                <p class="recap-flight__date">{flight.date}</p>
                <p class="recap-flight__passengers">
                  {flight.passengers} pasajero{flight.passengers > 1 ? 's' : ''}
                </p>
              </div>
            {/each}
          </div>

          <div class="booking-recap__divider"></div>

          <div class="booking-recap__total">
            <span class="booking-recap__total-label">Total pagado</span>
            <span class="booking-recap__total-value">$7,448</span>
          </div>

          <div class="booking-recap__help">
            <h3 class="booking-recap__help-title">Necesitas ayuda?</h3>
            <p class="booking-recap__help-text">
              Contactanos al +502 2345-6789 o por email a ayuda@vuelosgt.com
            </p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</div>