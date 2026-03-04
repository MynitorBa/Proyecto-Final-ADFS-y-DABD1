<script>
  // @ts-nocheck
  import '../styles/pasajeros.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  export let navigateTo;

  const API = 'https://localhost:7107';

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

  let dialCodes = {};
  let phoneDigitCounts = {};
  let phoneErrors = {};
  let pasaporteErrors = {};
  let dialCodesMap = {};

  const knownDigits = {
    '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,
    '+31':9,'+32':9,'+33':9,'+34':9,'+36':9,
    '+39':10,'+40':9,'+41':9,'+43':10,'+44':10,
    '+45':8,'+46':9,'+47':8,'+48':9,'+49':10,
    '+51':9,'+52':10,'+53':8,'+54':10,'+55':11,
    '+56':9,'+57':10,'+58':10,'+60':9,'+61':9,
    '+62':9,'+63':10,'+64':9,'+65':8,'+66':9,
    '+81':10,'+82':10,'+84':9,'+86':11,'+90':10,
    '+91':10,'+92':10,'+93':9,'+94':9,'+95':8,
    '+98':10,'+212':9,'+213':9,'+216':8,'+218':9,
    '+220':7,'+221':9,'+222':8,'+223':8,'+224':9,
    '+225':8,'+226':8,'+227':8,'+228':8,'+229':8,
    '+230':8,'+231':8,'+232':8,'+233':9,'+234':10,
    '+235':8,'+236':8,'+237':9,'+238':7,'+239':7,
    '+240':9,'+241':8,'+242':9,'+243':9,'+244':9,
    '+245':7,'+246':7,'+247':4,'+248':7,'+249':9,
    '+250':9,'+251':9,'+252':8,'+253':8,'+254':9,
    '+255':9,'+256':9,'+257':8,'+258':9,'+260':9,
    '+261':9,'+262':9,'+263':9,'+264':9,'+265':9,
    '+266':8,'+267':8,'+268':8,'+269':7,'+290':4,
    '+291':7,'+297':7,'+298':6,'+299':6,'+350':8,
    '+351':9,'+352':9,'+353':9,'+354':7,'+355':9,
    '+356':8,'+357':8,'+358':9,'+359':9,'+370':8,
    '+371':8,'+372':8,'+373':8,'+374':8,'+375':9,
    '+376':6,'+377':8,'+378':10,'+380':9,'+381':9,
    '+382':8,'+385':9,'+386':8,'+387':8,'+389':8,
    '+420':9,'+421':9,'+423':7,'+500':5,'+501':7,
    '+502':8,'+503':8,'+504':8,'+505':8,'+506':8,
    '+507':8,'+508':6,'+509':8,'+590':9,'+591':8,
    '+592':7,'+593':9,'+594':9,'+595':9,'+596':9,
    '+597':7,'+598':8,'+599':7,'+670':8,'+672':6,
    '+673':7,'+674':7,'+675':8,'+676':7,'+677':7,
    '+678':7,'+679':7,'+680':7,'+681':6,'+682':5,
    '+683':4,'+685':7,'+686':8,'+687':6,'+688':5,
    '+689':8,'+690':4,'+691':7,'+692':7,'+850':10,
    '+852':8,'+853':8,'+855':9,'+856':10,'+880':10,
    '+886':9,'+960':7,'+961':8,'+962':9,'+963':9,
    '+964':10,'+965':8,'+966':9,'+967':9,'+968':8,
    '+970':9,'+971':9,'+972':9,'+973':8,'+974':8,
    '+975':8,'+976':8,'+977':10,'+992':9,'+993':8,
    '+994':9,'+995':9,'+996':9,'+998':9,
  };

  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  function onPhoneInput(e, boletoId) {
    const raw = e.target.value.replace(/\D/g, '');
    const maxDigits = phoneDigitCounts[boletoId] || 8;
    const capped = raw.slice(0, maxDigits);
    passengerData[boletoId].telefono = formatLocalPhone(capped, maxDigits);
    passengerData = { ...passengerData };
    phoneErrors[boletoId] = '';
    phoneErrors = { ...phoneErrors };
  }

  function getPhonePlaceholder(digits) {
    const sample = '5'.repeat(digits);
    return formatLocalPhone(sample, digits);
  }

  // Agrupa boletos por vueloId para pasarle a SeleccionAsientos
  function construirGruposVuelo(boletos) {
    const mapa = {};
    boletos.forEach(b => {
      if (!mapa[b.vueloId]) {
        mapa[b.vueloId] = {
          vueloId:     b.vueloId,
          numeroVuelo: b.numeroVuelo,
          avionModelo: b.avionModelo,
          avionMarca:  b.avionMarca,
          clase:       b.clase,
          boletos:     []
        };
      }
      mapa[b.vueloId].boletos.push(b);
    });
    return Object.values(mapa);
  }

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

    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,idd');
      const data = await res.json();
      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root;
          const digits = knownDigits[code] ?? 9;
          const key = p.name.common.toLowerCase();
          dialCodesMap[key] = { code, digits };
          if (p.name.official) dialCodesMap[p.name.official.toLowerCase()] = { code, digits };
        }
      });
    } catch {
      console.error('Error cargando dial codes');
    }

    await cargarReservacionesPendientes();
    return () => unsubscribe();
  });

  async function cargarReservacionesPendientes() {
    loading = true;
    error = null;

    try {
      const response = await fetch(`${API}/api/mis-reservaciones`, { credentials: 'include' });
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

      if (todosLosBoletos.length === 0) {
        loading = false;
        return;
      }

      if (todosTienenPasajero) {
        // ── Todos tienen pasajero → saltar a selección de asientos ──────────
        const grupos = construirGruposVuelo(todosLosBoletos);
        navigateTo('seleccion-asientos', grupos);
        return;
      }

      // ── No tienen pasajero → preparar formulario ─────────────────────────
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
        paisQueries[boleto.boletoId]           = '';
        paisesSugeridos[boleto.boletoId]       = [];
        paisesSeleccionados[boleto.boletoId]   = null;
        ciudadQueries[boleto.boletoId]         = '';
        ciudadesSugeridas[boleto.boletoId]     = [];
        ciudadesSeleccionadas[boleto.boletoId] = false;
        dialCodes[boleto.boletoId]             = '';
        phoneDigitCounts[boleto.boletoId]      = 8;
        phoneErrors[boleto.boletoId]           = '';
        pasaporteErrors[boleto.boletoId]       = '';
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
    paisesSeleccionados[boletoId]   = pais;
    paisQueries[boletoId]           = pais.country;
    passengerData[boletoId].pais    = pais.country;
    paisesSugeridos[boletoId]       = [];
    ciudadQueries[boletoId]         = '';
    passengerData[boletoId].ciudad  = '';
    ciudadesSugeridas[boletoId]     = [];
    ciudadesSeleccionadas[boletoId] = false;

    const info = dialCodesMap[pais.country.toLowerCase()];
    dialCodes[boletoId]              = info?.code ?? '';
    phoneDigitCounts[boletoId]       = info?.digits ?? 9;
    passengerData[boletoId].telefono = '';
    phoneErrors[boletoId]            = '';

    paisQueries = { ...paisQueries }; paisesSeleccionados = { ...paisesSeleccionados };
    paisesSugeridos = { ...paisesSugeridos }; ciudadQueries = { ...ciudadQueries };
    ciudadesSugeridas = { ...ciudadesSugeridas }; ciudadesSeleccionadas = { ...ciudadesSeleccionadas };
    dialCodes = { ...dialCodes }; phoneDigitCounts = { ...phoneDigitCounts };
    passengerData = { ...passengerData }; phoneErrors = { ...phoneErrors };
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
    ciudadQueries[boletoId]         = ciudad;
    passengerData[boletoId].ciudad  = ciudad;
    ciudadesSugeridas[boletoId]     = [];
    ciudadesSeleccionadas[boletoId] = true;
    ciudadQueries = { ...ciudadQueries }; ciudadesSugeridas = { ...ciudadesSugeridas };
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
        const pd = passengerData[boleto.boletoId];
        const dc = dialCodes[boleto.boletoId] || '';
        const telefonoCompleto = dc ? dc + ' ' + pd.telefono.replace(/\s/g, '') : pd.telefono;
        boletosAgrupados[boleto.reservacionId].push({ ...pd, telefono: telefonoCompleto });
      });

      for (const reservacionId in boletosAgrupados) {
        for (const p of boletosAgrupados[reservacionId]) {
          if (!p.nombre || !p.apellido || !p.pasaporte || !p.telefono || !p.pais || !p.ciudad) {
            alert('Por favor completa todos los campos de todos los pasajeros');
            submitting = false; return;
          }
        }
      }

      for (const boleto of todosLosBoletos) {
        const pasaporte = passengerData[boleto.boletoId].pasaporte;
        if (!/^\d+$/.test(pasaporte)) {
          pasaporteErrors[boleto.boletoId] = 'El pasaporte debe contener solo números.';
          pasaporteErrors = { ...pasaporteErrors };
          currentPassengerIndex = todosLosBoletos.indexOf(boleto);
          submitting = false; return;
        }
      }

      for (const boleto of todosLosBoletos) {
        const dc = dialCodes[boleto.boletoId];
        if (dc) {
          const digitosIngresados = passengerData[boleto.boletoId].telefono.replace(/\D/g, '').length;
          const requeridos = phoneDigitCounts[boleto.boletoId];
          if (digitosIngresados !== requeridos) {
            phoneErrors[boleto.boletoId] = `Se requieren ${requeridos} dígitos (ingresaste ${digitosIngresados}).`;
            phoneErrors = { ...phoneErrors };
            currentPassengerIndex = todosLosBoletos.indexOf(boleto);
            submitting = false; return;
          }
        }
      }

      const promises = Object.entries(boletosAgrupados).map(([reservacionId, body]) =>
        fetch(`${API}/api/reservaciones/${reservacionId}/pasajeros`, {
          method: 'PUT', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        })
      );

      const responses = await Promise.all(promises);
      const allSuccess = responses.every(r => r.ok);

      if (allSuccess) {
        // ── Datos guardados → ir a selección de asientos ─────────────────
        const grupos = construirGruposVuelo(todosLosBoletos);
        navigateTo('seleccion-asientos', grupos);
      } else {
        for (const res of responses) {
          if (!res.ok) {
            try {
              const errData = await res.json();
              alert(errData.message || errData.mensaje || 'Error al procesar los datos.');
            } catch {
              alert('Hubo un error al procesar algunos datos. Por favor intenta de nuevo.');
            }
            break;
          }
        }
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
                      <input type="text" id="nombre-{currentBoleto.boletoId}" class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].nombre}
                        placeholder="Nombre(s)" autocomplete="off" required />
                    </div>
                    <div class="form-field">
                      <label for="apellido-{currentBoleto.boletoId}" class="form-field__label">Apellido *</label>
                      <input type="text" id="apellido-{currentBoleto.boletoId}" class="form-field__input"
                        bind:value={passengerData[currentBoleto.boletoId].apellido}
                        placeholder="Apellido(s)" autocomplete="off" required />
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-field">
                      <label for="pasaporte-{currentBoleto.boletoId}" class="form-field__label">Número de Pasaporte (solo números) *</label>
                      <input type="text" id="pasaporte-{currentBoleto.boletoId}" class="form-field__input"
                        class:form-field__input--error={pasaporteErrors[currentBoleto.boletoId]}
                        value={passengerData[currentBoleto.boletoId].pasaporte}
                        on:input={(e) => {
                          const raw = e.target.value;
                          const soloNumeros = raw.replace(/[^0-9]/g, '');
                          passengerData[currentBoleto.boletoId].pasaporte = soloNumeros;
                          passengerData = { ...passengerData };
                          pasaporteErrors[currentBoleto.boletoId] = raw !== soloNumeros ? 'Solo se permiten números.' : '';
                          pasaporteErrors = { ...pasaporteErrors };
                        }}
                        placeholder="12345678" autocomplete="off" required />
                      {#if pasaporteErrors[currentBoleto.boletoId]}
                        <span class="form-field__error">{pasaporteErrors[currentBoleto.boletoId]}</span>
                      {/if}
                    </div>
                    <div class="form-field">
                      <label for="pais-{currentBoleto.boletoId}" class="form-field__label">País *</label>
                      <div class="autocomplete">
                        <input type="text" id="pais-{currentBoleto.boletoId}" class="form-field__input"
                          bind:value={paisQueries[currentBoleto.boletoId]}
                          on:input={() => onPaisInput(currentBoleto.boletoId)}
                          on:blur={() => validarPaisSeleccionado(currentBoleto.boletoId)}
                          placeholder="Escribe tu país..." autocomplete="off" required />
                        {#if paisesSugeridos[currentBoleto.boletoId]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each paisesSugeridos[currentBoleto.boletoId] as pais}
                              <li class="autocomplete__item">
                                <button type="button" class="autocomplete__btn"
                                  on:click={() => seleccionarPais(currentBoleto.boletoId, pais)}>
                                  {pais.country}
                                </button>
                              </li>
                            {/each}
                          </ul>
                        {/if}
                      </div>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-field">
                      <label for="telefono-{currentBoleto.boletoId}" class="form-field__label">
                        Teléfono de contacto *
                        {#if dialCodes[currentBoleto.boletoId]}
                          <span class="form-field__label-hint">— {phoneDigitCounts[currentBoleto.boletoId]} dígitos</span>
                        {/if}
                      </label>
                      <div class="phone-field" class:phone-field--error={phoneErrors[currentBoleto.boletoId]}>
                        {#if dialCodes[currentBoleto.boletoId]}
                          <span class="phone-field__prefix">{dialCodes[currentBoleto.boletoId]}</span>
                        {/if}
                        <input type="tel" id="telefono-{currentBoleto.boletoId}" class="form-field__input"
                          bind:value={passengerData[currentBoleto.boletoId].telefono}
                          on:input={(e) => onPhoneInput(e, currentBoleto.boletoId)}
                          placeholder={dialCodes[currentBoleto.boletoId] ? getPhonePlaceholder(phoneDigitCounts[currentBoleto.boletoId]) : 'Selecciona un país primero'}
                          disabled={!dialCodes[currentBoleto.boletoId]}
                          autocomplete="off" required />
                      </div>
                      {#if passengerData[currentBoleto.boletoId]?.telefono && !phoneErrors[currentBoleto.boletoId] && dialCodes[currentBoleto.boletoId]}
                        {@const d = passengerData[currentBoleto.boletoId].telefono.replace(/\D/g, '').length}
                        {@const total = phoneDigitCounts[currentBoleto.boletoId]}
                        {#if d === total}
                          <span class="form-field__ok">✓ Número completo</span>
                        {:else}
                          <span class="form-field__hint">{d}/{total} dígitos</span>
                        {/if}
                      {/if}
                      {#if phoneErrors[currentBoleto.boletoId]}
                        <span class="form-field__error">{phoneErrors[currentBoleto.boletoId]}</span>
                      {/if}
                    </div>
                    <div class="form-field">
                      <label for="ciudad-{currentBoleto.boletoId}" class="form-field__label">Ciudad *</label>
                      <div class="autocomplete">
                        <input type="text" id="ciudad-{currentBoleto.boletoId}" class="form-field__input"
                          bind:value={ciudadQueries[currentBoleto.boletoId]}
                          on:input={() => onCiudadInput(currentBoleto.boletoId)}
                          on:blur={() => validarCiudadSeleccionada(currentBoleto.boletoId)}
                          placeholder={paisesSeleccionados[currentBoleto.boletoId] ? 'Escribe tu ciudad...' : 'Primero selecciona un país'}
                          disabled={!paisesSeleccionados[currentBoleto.boletoId]}
                          autocomplete="off" required />
                        {#if ciudadesSugeridas[currentBoleto.boletoId]?.length > 0}
                          <ul class="autocomplete__list">
                            {#each ciudadesSugeridas[currentBoleto.boletoId] as ciudad}
                              <li class="autocomplete__item">
                                <button type="button" class="autocomplete__btn"
                                  on:click={() => seleccionarCiudad(currentBoleto.boletoId, ciudad)}>
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
              <button type="button" class="passengers-form__btn-prev"
                on:click={handlePrevious} disabled={isFirstPassenger}>
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
                  <div class="recap-reservation__total">Total: $ {reserva.total.toFixed(2)}</div>
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