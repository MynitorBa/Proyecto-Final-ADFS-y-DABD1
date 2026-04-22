<template>
  <div class="page">
    <Encabezado />

    <!-- Overlay: procesando la creación de la reserva en el servidor -->
    <div v-if="creandoReserva" class="res-overlay">
      <div class="res-overlay__card">
        <div class="res-spinner-xl"></div>
        <p class="res-overlay__txt">Asegurando disponibilidad...</p>
        <small class="res-overlay__sub">Esto solo toma un momento</small>
      </div>
    </div>

    <!-- Overlay: error crítico al intentar crear la reserva -->
    <div v-else-if="errorCreacion && !reservacionId" class="res-overlay">
      <div class="res-overlay__card res-overlay__card--error">
        <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="52" height="52">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
          <line x1="12" y1="9" x2="12" y2="13"/>
          <circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/>
        </svg>
        <h3 class="res-overlay__titulo">No se pudo crear la reserva</h3>
        <p class="res-overlay__msg">{{ errorCreacion }}</p>
        <button class="res-btn res-btn--yellow" @click="crearReservacion" type="button">Reintentar</button>
        <button class="res-btn res-btn--ghost" @click="$router.back()" type="button">Volver atrás</button>
      </div>
    </div>

    <!-- Overlay: el temporizador llegó a cero y la reserva expiró -->
    <div v-else-if="tiempoRestante === 0 && reservacionId" class="res-overlay">
      <div class="res-overlay__card res-overlay__card--error">
        <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="52" height="52">
          <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
        </svg>
        <h3 class="res-overlay__titulo">Reserva expirada</h3>
        <p class="res-overlay__msg">
          El tiempo para completar la reserva <strong>{{ noReservacion }}</strong> ha vencido.
        </p>
        <button class="res-btn res-btn--yellow" @click="$router.push('/principal')" type="button">
          Realizar nueva búsqueda
        </button>
      </div>
    </div>

    <!-- Contenido principal: visible cuando la reserva está activa y en tiempo -->
    <div v-else class="res-page">
      <div class="res-container">

        <!-- Encabezado con título, número de reserva y badge de referencia -->
        <div class="res-header">
          <div class="res-header__text">
            <h1 class="res-header__title">Completar Reserva</h1>
            <p class="res-header__sub">
              Completa los datos de
              {{ totalBoletos > 1 ? `los ${totalBoletos} pasajeros` : 'el pasajero' }}
              para confirmar
            </p>
          </div>
          <div v-if="reservacionId" class="res-header__badge">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
              <path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/>
              <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/>
              <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
            </svg>
            Reserva {{ noReservacion }}
          </div>
        </div>

        <div class="res-layout">

          <!-- Columna izquierda: formulario de pasajeros (solo para vuelos y paquetes) -->
          <div class="res-form-col" v-if="tipoItem !== 'hotel'">

            <!-- Formulario del pasajero principal (boleto 1) -->
            <div class="res-form-card">
              <div class="res-form-card__head">
                <div class="res-form-card__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <div class="res-form-card__head-info">
                  <h2 class="res-form-card__title">
                    {{ totalBoletos > 1 ? 'Pasajero 1' : 'Datos del Pasajero' }}
                  </h2>
                  <span v-if="boletos[0]" class="res-form-card__boleto-meta">
                    {{ boletos[0].noBoleto }} · Asiento {{ boletos[0].noAsiento }} · {{ boletos[0].clase }}
                  </span>
                </div>
              </div>

              <div class="res-form-card__body">
                <!-- Nombre y apellidos del pasajero principal -->
                <div class="res-form-row">
                  <div class="res-field">
                    <label class="res-field__label">Nombre(s) *</label>
                    <input class="res-field__input" type="text" v-model="form.nombre"
                      placeholder="Ej: Carlos Andrés" autocomplete="off" />
                    <span v-if="errors.nombre" class="res-field__error">{{ errors.nombre }}</span>
                  </div>
                  <div class="res-field">
                    <label class="res-field__label">Apellido(s) *</label>
                    <input class="res-field__input" type="text" v-model="form.apellido"
                      placeholder="Ej: López García" autocomplete="off" />
                    <span v-if="errors.apellido" class="res-field__error">{{ errors.apellido }}</span>
                  </div>
                </div>

                <!-- Pasaporte (solo números) y país con autocompletado -->
                <div class="res-form-row">
                  <div class="res-field">
                    <label class="res-field__label">Número de Pasaporte *</label>
                    <input class="res-field__input" type="text" v-model="form.pasaporte"
                      placeholder="Solo números" autocomplete="off"
                      @input="form.pasaporte = form.pasaporte.replace(/\D/g, '')" />
                    <span v-if="errors.pasaporte" class="res-field__error">{{ errors.pasaporte }}</span>
                  </div>
                  <div class="res-field">
                    <label class="res-field__label">País de residencia *</label>
                    <div class="res-autocomplete">
                      <input class="res-field__input" type="text" v-model="paisQuery"
                        @input="onPaisInput" @blur="validarPais"
                        placeholder="Escribe tu país..." autocomplete="off" />
                      <ul v-if="paisesSugeridos.length" class="res-autocomplete__list">
                        <li v-for="p in paisesSugeridos" :key="p.country"
                          class="res-autocomplete__item"
                          @mousedown.prevent="seleccionarPais(p)">{{ p.country }}</li>
                      </ul>
                    </div>
                    <span v-if="errors.pais" class="res-field__error">{{ errors.pais }}</span>
                  </div>
                </div>

                <!-- Teléfono con prefijo dinámico y ciudad con autocompletado -->
                <div class="res-form-row">
                  <div class="res-field">
                    <label class="res-field__label">
                      Teléfono *
                      <span v-if="dialCode" class="res-field__hint">— {{ phoneDigits }} dígitos locales</span>
                    </label>
                    <div class="res-phone" :class="{ 'res-phone--error': errors.telefono }">
                      <span v-if="dialCode" class="res-phone__prefix">{{ dialCode }}</span>
                      <input class="res-field__input" type="tel" v-model="form.telefono"
                        @input="onPhoneInput"
                        :placeholder="dialCode ? phonePlaceholder : 'Selecciona un país primero'"
                        :disabled="!dialCode" autocomplete="off" />
                    </div>
                    <span v-if="form.telefono && !errors.telefono && dialCode" class="res-field__ok">
                      {{ telefonoDigitos === phoneDigits ? '✓ Número completo' : telefonoDigitos + '/' + phoneDigits + ' dígitos' }}
                    </span>
                    <span v-if="errors.telefono" class="res-field__error">{{ errors.telefono }}</span>
                  </div>
                  <div class="res-field">
                    <label class="res-field__label">Ciudad *</label>
                    <div class="res-autocomplete">
                      <input class="res-field__input" type="text" v-model="ciudadQuery"
                        @input="onCiudadInput" @blur="validarCiudad"
                        :placeholder="paisSeleccionado ? 'Escribe tu ciudad...' : 'Selecciona un país primero'"
                        :disabled="!paisSeleccionado" autocomplete="off" />
                      <ul v-if="ciudadesSugeridas.length" class="res-autocomplete__list">
                        <li v-for="c in ciudadesSugeridas" :key="c"
                          class="res-autocomplete__item"
                          @mousedown.prevent="seleccionarCiudad(c)">{{ c }}</li>
                      </ul>
                    </div>
                    <span v-if="errors.ciudad" class="res-field__error">{{ errors.ciudad }}</span>
                  </div>
                </div>

                <!-- Error general del formulario del pasajero principal -->
                <div v-if="errors.general" class="res-error-general">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  {{ errors.general }}
                </div>
              </div>
            </div>

            <!-- Formularios para pasajeros adicionales (boletos 2, 3...) -->
            <template v-for="(pax, idx) in pasajerosAdicionales" :key="idx">

              <!-- Separador visual entre pasajeros -->
              <div class="res-pax-divider">
                <div class="res-pax-divider__line"></div>
                <span class="res-pax-divider__label">Pasajero {{ idx + 2 }}</span>
                <div class="res-pax-divider__line"></div>
              </div>

              <div class="res-form-card res-form-card--adicional">
                <div class="res-form-card__head">
                  <div class="res-form-card__icon res-form-card__icon--num">{{ idx + 2 }}</div>
                  <div class="res-form-card__head-info">
                    <h2 class="res-form-card__title">Pasajero {{ idx + 2 }}</h2>
                    <span class="res-form-card__boleto-meta">
                      {{ boletos[idx + 1]?.noBoleto }}
                      · Asiento {{ boletos[idx + 1]?.noAsiento }}
                      · {{ boletos[idx + 1]?.clase }}
                      <template v-if="boletos[idx + 1]?.numeroVuelo">
                        · Vuelo {{ boletos[idx + 1]?.numeroVuelo }}
                      </template>
                    </span>
                  </div>
                </div>

                <div class="res-form-card__body">
                  <!-- Nombre y apellidos del pasajero adicional -->
                  <div class="res-form-row">
                    <div class="res-field">
                      <label class="res-field__label">Nombre(s) *</label>
                      <input class="res-field__input" type="text" v-model="pax.nombre"
                        placeholder="Ej: María José" autocomplete="off" />
                      <span v-if="erroresPasajeros[idx]?.nombre" class="res-field__error">
                        {{ erroresPasajeros[idx].nombre }}
                      </span>
                    </div>
                    <div class="res-field">
                      <label class="res-field__label">Apellido(s) *</label>
                      <input class="res-field__input" type="text" v-model="pax.apellido"
                        placeholder="Ej: García Morales" autocomplete="off" />
                      <span v-if="erroresPasajeros[idx]?.apellido" class="res-field__error">
                        {{ erroresPasajeros[idx].apellido }}
                      </span>
                    </div>
                  </div>

                  <!-- Pasaporte y país del pasajero adicional -->
                  <div class="res-form-row">
                    <div class="res-field">
                      <label class="res-field__label">Número de Pasaporte *</label>
                      <input class="res-field__input" type="text" v-model="pax.pasaporte"
                        placeholder="Solo números" autocomplete="off"
                        @input="pax.pasaporte = pax.pasaporte.replace(/\D/g, '')" />
                      <span v-if="erroresPasajeros[idx]?.pasaporte" class="res-field__error">
                        {{ erroresPasajeros[idx].pasaporte }}
                      </span>
                    </div>
                    <div class="res-field">
                      <label class="res-field__label">País de residencia *</label>
                      <div class="res-autocomplete">
                        <input class="res-field__input" type="text"
                          v-model="paxAcState[idx].paisQuery"
                          @input="onPaxPaisInput(idx)"
                          @blur="() => { setTimeout(() => { if(paxAcState[idx]) paxAcState[idx].paisesSugeridos = [] }, 200) }"
                          placeholder="Escribe tu país..." autocomplete="off" />
                        <ul v-if="paxAcState[idx]?.paisesSugeridos?.length" class="res-autocomplete__list">
                          <li v-for="p in paxAcState[idx].paisesSugeridos" :key="p.country"
                            class="res-autocomplete__item"
                            @mousedown.prevent="seleccionarPaxPais(idx, p)">{{ p.country }}</li>
                        </ul>
                      </div>
                      <span v-if="erroresPasajeros[idx]?.pais" class="res-field__error">
                        {{ erroresPasajeros[idx].pais }}
                      </span>
                    </div>
                  </div>

                  <!-- Teléfono y ciudad del pasajero adicional -->
                  <div class="res-form-row">
                    <div class="res-field">
                      <label class="res-field__label">
                        Teléfono *
                        <span v-if="paxAcState[idx]?.dialCode" class="res-field__hint">
                          — {{ paxAcState[idx].phoneDigits }} dígitos
                        </span>
                      </label>
                      <div class="res-phone"
                        :class="{ 'res-phone--error': erroresPasajeros[idx]?.telefono }">
                        <span v-if="paxAcState[idx]?.dialCode" class="res-phone__prefix">
                          {{ paxAcState[idx].dialCode }}
                        </span>
                        <input class="res-field__input" type="tel" v-model="pax.telefono"
                          @input="onPaxPhoneInput(idx, $event)"
                          :placeholder="paxAcState[idx]?.dialCode ? 'Número local' : 'Selecciona país primero'"
                          :disabled="!paxAcState[idx]?.dialCode"
                          autocomplete="off" />
                      </div>
                      <span v-if="erroresPasajeros[idx]?.telefono" class="res-field__error">
                        {{ erroresPasajeros[idx].telefono }}
                      </span>
                    </div>
                    <div class="res-field">
                      <label class="res-field__label">
                        Ciudad *
                        <span v-if="paxAcState[idx]?.ciudadLoading" class="res-field__hint">cargando...</span>
                      </label>
                      <div class="res-autocomplete">
                        <input class="res-field__input" type="text"
                          v-model="paxAcState[idx].ciudadQuery"
                          @input="onPaxCiudadInput(idx)"
                          @blur="() => { setTimeout(() => { if(paxAcState[idx]) paxAcState[idx].ciudadesSugeridas = [] }, 200) }"
                          :placeholder="paxAcState[idx]?.paisSel ? 'Escribe tu ciudad...' : 'Selecciona un país primero'"
                          :disabled="!paxAcState[idx]?.paisSel || paxAcState[idx]?.ciudadLoading"
                          autocomplete="off" />
                        <ul v-if="paxAcState[idx]?.ciudadesSugeridas?.length" class="res-autocomplete__list">
                          <li v-for="c in paxAcState[idx].ciudadesSugeridas" :key="c"
                            class="res-autocomplete__item"
                            @mousedown.prevent="seleccionarPaxCiudad(idx, c)">{{ c }}</li>
                        </ul>
                      </div>
                      <span v-if="erroresPasajeros[idx]?.ciudad" class="res-field__error">
                        {{ erroresPasajeros[idx].ciudad }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </template>

          </div><!-- /res-form-col vuelos y paquetes -->

          <!-- Columna izquierda: mensaje informativo para reservas de hotel (sin datos de pasajero) -->
          <div class="res-form-col" v-else>
            <div class="res-form-card">
              <div class="res-form-card__head">
                <div class="res-form-card__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                </div>
                <div class="res-form-card__head-info">
                  <h2 class="res-form-card__title">Habitación reservada</h2>
                  <span class="res-form-card__boleto-meta">
                    Revisa el resumen y confirma tu reserva
                  </span>
                </div>
              </div>
              <div class="res-form-card__body">
                <div style="display:flex;align-items:center;gap:12px;padding:8px 0;color:#5a5047;font-size:14px;">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="20" height="20">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                  Tu habitación está disponible y lista para confirmar.
                  No se requieren datos de pasajero para reservas de hospedaje.
                </div>
                <div v-if="errors.general" class="res-error-general">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  {{ errors.general }}
                </div>
              </div>
            </div>
          </div><!-- /res-form-col hotel -->

          <!-- Columna derecha: resumen del item seleccionado con temporizador y precio -->
          <aside class="res-summary-col">
            <div class="res-summary">

              <div class="res-summary__head">
                <div class="res-summary__head-row">
                  <h2 class="res-summary__title">Resumen</h2>
                  <span v-if="noReservacion" class="res-summary__num">{{ noReservacion }}</span>
                </div>
                <!-- Temporizador de cuenta regresiva con estados visual de urgencia -->
                <div v-if="reservacionId" class="res-timer"
                  :class="{
                    'res-timer--warn':    tiempoRestante <= 180 && tiempoRestante > 60,
                    'res-timer--urgente': tiempoRestante <= 60
                  }">
                  <div class="res-timer__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                      <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
                    </svg>
                  </div>
                  <div class="res-timer__body">
                    <span class="res-timer__label">Tiempo para confirmar</span>
                    <span class="res-timer__tiempo">{{ formatTiempo(tiempoRestante) }}</span>
                  </div>
                  <div class="res-timer__barra">
                    <div class="res-timer__barra-fill"
                      :style="{ width: (tiempoRestante / tiempoTotal * 100) + '%' }"></div>
                  </div>
                </div>
              </div>

              <!-- Estado vacío: ningún item cargado desde sessionStorage -->
              <div v-if="!item" class="res-summary__empty">
                <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="1.5" width="40" height="40">
                  <rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>
                </svg>
                <p>No hay ningún item seleccionado.</p>
                <button class="res-btn res-btn--ghost" @click="$router.push('/principal')" type="button">
                  Buscar viajes
                </button>
              </div>

              <!-- Resumen: vuelo de solo ida -->
              <template v-else-if="tipoItem === 'vuelo' && item.tipoVuelo === 'ida'">
                <div class="res-summary__tag res-summary__tag--vuelo">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  Vuelo · Solo ida
                </div>
                <div class="res-summary__aerolinea">
                  <strong>{{ item.aerolinea }}</strong>
                  <span class="res-summary__num-vuelo">Nro. {{ item.numeroVuelo }}</span>
                </div>
                <div class="res-summary__ruta">
                  <div class="res-summary__punto">
                    <span class="res-summary__iata">{{ item.origenCodigo }}</span>
                    <span class="res-summary__ciudad">{{ item.origenCiudad }}</span>
                    <span class="res-summary__hora">{{ item.horaSalida }}</span>
                  </div>
                  <div class="res-summary__track">
                    <div class="res-summary__track-line"></div>
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <div class="res-summary__track-line"></div>
                  </div>
                  <div class="res-summary__punto res-summary__punto--r">
                    <span class="res-summary__iata">{{ item.destinoCodigo }}</span>
                    <span class="res-summary__ciudad">{{ item.destinoCiudad }}</span>
                    <span class="res-summary__hora">{{ item.horaLlegada }}</span>
                  </div>
                </div>
                <div class="res-summary__details">
                  <div class="res-summary__detail-row"><span>Aerolínea</span><span>{{ item.aerolinea }}</span></div>
                  <div class="res-summary__detail-row"><span>Clase</span><span style="text-transform:capitalize">{{ item.clase }}</span></div>
                  <div class="res-summary__detail-row"><span>Duración</span><span>{{ formatDuracion(item.duracionMinutos) }}</span></div>
                  <div class="res-summary__detail-row"><span>Escalas</span><span>{{ item.escalas === 0 ? 'Directo' : item.escalas + ' escala(s)' }}</span></div>
                  <div class="res-summary__detail-row"><span>Pasajeros</span><span>{{ item.busqueda?.cantidadPasajeros || 1 }}</span></div>
                </div>
                <!-- Boletos asignados por el servidor para este vuelo -->
                <div v-if="detalleVuelo?.detalle?.boletos?.length" class="res-boletos">
                  <p class="res-boletos__titulo">Boletos confirmados</p>
                  <div v-for="b in detalleVuelo.detalle.boletos" :key="b.boletoId" class="res-boleto">
                    <div class="res-boleto__left">
                      <span class="res-boleto__no">{{ b.noBoleto }}</span>
                      <span class="res-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                    </div>
                  </div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total</span>
                  <span class="res-summary__precio">${{ (detalleVuelo?.total_con_ganancia ?? (item.precio || 0) * (item.busqueda?.cantidadPasajeros || 1)).toFixed(2) }}</span>
                </div>
              </template>

              <!-- Resumen: vuelo de ida y vuelta con ambas etapas -->
              <template v-else-if="tipoItem === 'vuelo' && item.tipoVuelo === 'idaVuelta'">
                <div class="res-summary__tag res-summary__tag--vuelo">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  Vuelo · Ida y vuelta
                </div>
                <!-- Sub-sección: vuelo de ida -->
                <div class="res-sub-vuelo">
                  <div class="res-sub-vuelo__badge">Ida</div>
                  <div class="res-summary__aerolinea" style="padding:8px 20px 0">
                    <strong>{{ item.ida?.aerolinea }}</strong>
                    <span class="res-summary__num-vuelo">Nro. {{ item.ida?.numeroVuelo }}</span>
                  </div>
                  <div class="res-summary__ruta">
                    <div class="res-summary__punto"><span class="res-summary__iata">{{ item.ida?.origenCodigo }}</span><span class="res-summary__ciudad">{{ item.ida?.origenCiudad }}</span><span class="res-summary__hora">{{ item.ida?.horaSalida }}</span></div>
                    <div class="res-summary__track"><div class="res-summary__track-line"></div><svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg><div class="res-summary__track-line"></div></div>
                    <div class="res-summary__punto res-summary__punto--r"><span class="res-summary__iata">{{ item.ida?.destinoCodigo }}</span><span class="res-summary__ciudad">{{ item.ida?.destinoCiudad }}</span><span class="res-summary__hora">{{ item.ida?.horaLlegada }}</span></div>
                  </div>
                  <div class="res-sub-vuelo__meta"><span style="text-transform:capitalize">{{ item.ida?.clase }}</span><span>·</span><span>{{ formatDuracion(item.ida?.duracionMinutos) }}</span><span>·</span><span>${{ ((item.ida?.precio || 0) * (item.busqueda?.cantidadPasajeros || 1)).toFixed(2) }}</span></div>
                </div>
                <!-- Sub-sección: vuelo de regreso -->
                <div class="res-sub-vuelo res-sub-vuelo--regreso">
                  <div class="res-sub-vuelo__badge res-sub-vuelo__badge--regreso">Regreso</div>
                  <div class="res-summary__aerolinea" style="padding:8px 20px 0">
                    <strong>{{ item.regreso?.aerolinea }}</strong>
                    <span class="res-summary__num-vuelo">Nro. {{ item.regreso?.numeroVuelo }}</span>
                  </div>
                  <div class="res-summary__ruta">
                    <div class="res-summary__punto"><span class="res-summary__iata">{{ item.regreso?.origenCodigo }}</span><span class="res-summary__ciudad">{{ item.regreso?.origenCiudad }}</span><span class="res-summary__hora">{{ item.regreso?.horaSalida }}</span></div>
                    <div class="res-summary__track"><div class="res-summary__track-line"></div><svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg><div class="res-summary__track-line"></div></div>
                    <div class="res-summary__punto res-summary__punto--r"><span class="res-summary__iata">{{ item.regreso?.destinoCodigo }}</span><span class="res-summary__ciudad">{{ item.regreso?.destinoCiudad }}</span><span class="res-summary__hora">{{ item.regreso?.horaLlegada }}</span></div>
                  </div>
                  <div class="res-sub-vuelo__meta"><span style="text-transform:capitalize">{{ item.regreso?.clase }}</span><span>·</span><span>{{ formatDuracion(item.regreso?.duracionMinutos) }}</span><span>·</span><span>${{ ((item.regreso?.precio || 0) * (item.busqueda?.cantidadPasajeros || 1)).toFixed(2) }}</span></div>
                </div>
                <div v-if="detalleVuelo?.detalle?.boletos?.length" class="res-boletos">
                  <p class="res-boletos__titulo">Boletos confirmados</p>
                  <div v-for="b in detalleVuelo.detalle.boletos" :key="b.boletoId" class="res-boleto">
                    <div class="res-boleto__left"><span class="res-boleto__no">{{ b.noBoleto }}</span><span class="res-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }} · Vuelo {{ b.numeroVuelo }}</span></div>
                  </div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total ({{ item.busqueda?.cantidadPasajeros || 1 }} pax)</span>
                  <span class="res-summary__precio">${{ (detalleVuelo?.total_con_ganancia ?? (((item.ida?.precio || 0) + (item.regreso?.precio || 0)) * (item.busqueda?.cantidadPasajeros || 1))).toFixed(2) }}</span>
                </div>
              </template>

              <!-- Resumen: hotel con fechas de check-in/out y total por noches -->
              <template v-else-if="tipoItem === 'hotel'">
                <div class="res-summary__tag res-summary__tag--hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="13" height="13">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                  Hospedaje
                </div>
                <h3 class="res-summary__hotel-nombre">{{ item.nombreHotel }}</h3>
                <p class="res-summary__hotel-ubicacion">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                  </svg>
                  {{ item.hotelCiudad || item.busqueda?.ciudad }}
                </p>
                <div class="res-summary__details">
                  <div class="res-summary__detail-row"><span>Habitación</span><span>{{ item.tipoHabitacion || item.tipo }}</span></div>
                  <div v-if="item.tipoCama" class="res-summary__detail-row"><span>Cama</span><span>{{ item.tipoCama }}</span></div>
                  <div class="res-summary__detail-row"><span>Check-in</span><span>{{ item.busqueda?.checkIn }}</span></div>
                  <div class="res-summary__detail-row"><span>Check-out</span><span>{{ item.busqueda?.checkOut }}</span></div>
                  <div class="res-summary__detail-row"><span>Noches</span><span>{{ item.noches }}</span></div>
                  <div class="res-summary__detail-row"><span>Personas</span><span>{{ item.busqueda?.cantidadPersonas }}</span></div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total ({{ item.noches }} noches)</span>
                  <span class="res-summary__precio">
                    ${{ (detalleHotel?.total_con_ganancia ?? item.totalEstancia ?? 0).toFixed(2) }}
                  </span>
                </div>
              </template>

              <!-- Resumen: paquete completo (vuelo + hotel) con desglose de precios y descuento -->
              <template v-else-if="tipoItem === 'paquete'">
                <div class="res-summary__tag res-summary__tag--paquete">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                  Paquete completo
                </div>
                <div class="res-sub-vuelo">
                  <div class="res-sub-vuelo__badge">Vuelo ida</div>
                  <div class="res-summary__ruta" style="padding:10px 20px">
                    <div class="res-summary__punto"><span class="res-summary__iata">{{ item.vuelo?.origenCodigo }}</span><span class="res-summary__ciudad">{{ item.vuelo?.aerolinea }}</span></div>
                    <div class="res-summary__track"><div class="res-summary__track-line"></div><svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg><div class="res-summary__track-line"></div></div>
                    <div class="res-summary__punto res-summary__punto--r"><span class="res-summary__iata">{{ item.vuelo?.destinoCodigo }}</span><span class="res-summary__ciudad" style="text-transform:capitalize">{{ item.vuelo?.clase }}</span></div>
                  </div>
                </div>
                <div v-if="item.vueloRegreso" class="res-sub-vuelo res-sub-vuelo--regreso">
                  <div class="res-sub-vuelo__badge res-sub-vuelo__badge--regreso">Vuelo regreso</div>
                  <div class="res-summary__ruta" style="padding:10px 20px">
                    <div class="res-summary__punto"><span class="res-summary__iata">{{ item.vueloRegreso?.origenCodigo }}</span><span class="res-summary__ciudad">{{ item.vueloRegreso?.aerolinea }}</span></div>
                    <div class="res-summary__track"><div class="res-summary__track-line"></div><svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg><div class="res-summary__track-line"></div></div>
                    <div class="res-summary__punto res-summary__punto--r"><span class="res-summary__iata">{{ item.vueloRegreso?.destinoCodigo }}</span><span class="res-summary__ciudad" style="text-transform:capitalize">{{ item.vueloRegreso?.clase }}</span></div>
                  </div>
                </div>
                <!-- Hotel incluido en el paquete -->
                <div class="res-paquete-hotel">
                  <div class="res-sub-vuelo__badge" style="margin:0 0 8px">Hotel</div>
                  <span class="res-paquete-hotel__nombre">{{ item.hotel?.nombreHotel }}</span>
                  <span class="res-paquete-hotel__meta">{{ item.hotel?.ciudad }} · {{ item.noches }}n · {{ item.hotel?.tipoHabitacion }}</span>
                </div>
                <div v-if="detalleVuelo?.detalle?.boletos?.length" class="res-boletos">
                  <p class="res-boletos__titulo">Boletos confirmados</p>
                  <div v-for="b in detalleVuelo.detalle.boletos" :key="b.boletoId" class="res-boleto">
                    <div class="res-boleto__left"><span class="res-boleto__no">{{ b.noBoleto }}</span><span class="res-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span></div>
                  </div>
                </div>
                <!-- Desglose de precio del paquete con descuento aplicado si corresponde -->
                <div class="res-summary__precio-wrap res-summary__precio-wrap--desglose">
                  <div class="res-desglose">
                    <div class="res-desglose__row">
                      <span>✈ Vuelo{{ item.vueloRegreso ? 's' : '' }}</span>
                      <span>${{ (detalleVuelo?.total_con_ganancia ?? ((item.vuelo?.precio || 0) + (item.vueloRegreso?.precio || 0)) * (item.cantidadPersonas || 1)).toFixed(2) }}</span>
                    </div>
                    <div class="res-desglose__row">
                      <span>🏨 Hotel ({{ item.noches }}n)</span>
                      <span>${{ (detalleHotel?.total_con_ganancia ?? (item.hotel?.precioNoche || 0) * (item.noches || 1)).toFixed(2) }}</span>
                    </div>
                  </div>
                  <!-- Subtotal antes del descuento -->
                  <div class="res-desglose__total">
                    <span class="res-summary__precio-lbl">Subtotal</span>
                    <span class="res-summary__precio" style="font-size:18px">${{ totalPaquete.toFixed(2) }}</span>
                  </div>
                  <!-- Descuento y total final: solo visible cuando hay porcentaje configurado -->
                  <template v-if="porcentajeDescuento > 0 && montoDescuentoPaquete > 0">
                    <div class="res-desglose__row" style="color:#16a34a;font-weight:700;padding:4px 20px;">
                      <span>✓ Descuento paquete ({{ porcentajeDescuento }}%)</span>
                      <span>-${{ montoDescuentoPaquete.toFixed(2) }}</span>
                    </div>
                    <div class="res-desglose__row" style="padding:6px 20px 14px;font-weight:800;font-size:15px;color:#1C1A18;border-top:1px dashed #e8e3dc;margin-top:4px;">
                      <span>Total con descuento</span>
                      <span>${{ (totalPaquete - montoDescuentoPaquete).toFixed(2) }}</span>
                    </div>
                  </template>
                </div>
              </template>

              <!-- Botón de acción principal: seleccionar asientos (vuelo/paquete) o confirmar (hotel) -->
              <div v-if="item" class="res-summary__footer">
                <button class="res-btn res-btn--yellow res-btn--full"
                  @click="handleReservar" type="button"
                  :disabled="submitting || tiempoRestante === 0">
                  <div v-if="submitting" class="res-spinner-sm"></div>
                  <!-- Vuelo / Paquete: continúa a selección de asientos -->
                  <template v-else-if="tipoItem !== 'hotel'">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <path d="M3 9h18M9 21V9"/>
                    </svg>
                    {{ submitting ? 'Procesando...' : 'Seleccionar Asientos' }}
                  </template>
                  <!-- Hotel: confirma la reserva directamente -->
                  <template v-else>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15">
                      <path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/>
                      <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/>
                      <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
                    </svg>
                    {{ submitting ? 'Procesando...' : 'Confirmar Reserva' }}
                  </template>
                </button>
                <p class="res-summary__aviso">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12">
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  {{ tipoItem !== 'hotel' ? 'Siguiente: elegir asientos en el avión' : 'Confirma para asegurar tu habitación' }}
                </p>
              </div>

            </div>
          </aside>

        </div>
      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Reserva.vue
 * @description Vista para completar una reserva de vuelo, hotel o paquete.
 * Crea la reservación en el backend al montar, muestra un temporizador de
 * cuenta regresiva y recopila los datos de los pasajeros antes de avanzar
 * a la selección de asientos (vuelos/paquetes) o al checkout (hoteles).
 */
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/reserva.css'

/** Instancia del router para navegar entre pasos del flujo de reserva. */
const router = useRouter()

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/**
 * Rutas que forman parte del flujo de reserva.
 * Al salir hacia cualquier otra ruta se limpia la sesión de reserva.
 * @type {string[]}
 */
const FLUJO_RESERVA = ['/reservar', '/seleccion-asientos', '/checkout', '/confirmacion']

/**
 * Elimina todas las claves de sessionStorage relacionadas con el flujo de reserva activo.
 */
function limpiarSesionReserva() {
  sessionStorage.removeItem('checkout_data')
  sessionStorage.removeItem('_reserva_expires_at')
  sessionStorage.removeItem('_reserva_id')
  sessionStorage.removeItem('_reserva_no')
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
}

/** Limpia la sesión si el usuario navega fuera del flujo de reserva. */
onBeforeRouteLeave((to) => {
  if (!FLUJO_RESERVA.includes(to.path)) {
    if (timerInterval.value) clearInterval(timerInterval.value)
    limpiarSesionReserva()
  }
})

/** ID numérico de la reservación creada en el servidor. @type {import('vue').Ref<string|null>} */
const reservacionId  = ref(null)

/** Número legible de la reservación (ej. 'MOV-0001'). @type {import('vue').Ref<string>} */
const noReservacion  = ref('')

/** Respuesta del servidor para el detalle de vuelo, incluye boletos y precio con ganancia. @type {import('vue').Ref<object|null>} */
const detalleVuelo   = ref(null)

/** Respuesta del servidor para el detalle de hotel, incluye precio con ganancia. @type {import('vue').Ref<object|null>} */
const detalleHotel   = ref(null)

/** Segundos restantes en el temporizador de la reserva. @type {import('vue').Ref<number>} */
const tiempoRestante = ref(600)

/** Segundos totales con los que se inició el temporizador (para calcular la barra de progreso). @type {import('vue').Ref<number>} */
const tiempoTotal    = ref(600)

/** Indica si el overlay de "creando reserva" está visible. @type {import('vue').Ref<boolean>} */
const creandoReserva = ref(true)

/** Mensaje de error cuando falla la creación de la reserva. @type {import('vue').Ref<string>} */
const errorCreacion  = ref('')

/** Referencia al intervalo del temporizador para poder limpiarlo. @type {import('vue').Ref<number|null>} */
const timerInterval  = ref(null)

/** Item seleccionado (vuelo, hotel o paquete) leído desde sessionStorage. @type {import('vue').Ref<object|null>} */
const item     = ref(null)

/** Tipo del item activo: 'vuelo' | 'hotel' | 'paquete'. @type {import('vue').Ref<string>} */
const tipoItem = ref('')

/** Boletos asignados por el servidor, tomados del detalle de vuelo. @type {import('vue').ComputedRef<Array>} */
const boletos              = computed(() => detalleVuelo.value?.detalle?.boletos || [])

/** Cantidad total de boletos (al menos 1). @type {import('vue').ComputedRef<number>} */
const totalBoletos         = computed(() => boletos.value.length || 1)

/** Array de objetos con los datos de los pasajeros adicionales (boletos 2, 3...). @type {import('vue').Ref<Array>} */
const pasajerosAdicionales = ref([])

/** Errores de validación por pasajero adicional, indexados igual que `pasajerosAdicionales`. @type {import('vue').Ref<Array>} */
const erroresPasajeros     = ref([])

/**
 * Estado de los autocompletados de país/ciudad/teléfono para cada pasajero adicional.
 * @type {import('vue').Ref<Array>}
 */
const paxAcState           = ref([])

/**
 * Inicializa los formularios de pasajeros adicionales cada vez que llegan los boletos del servidor.
 * Se ejecuta de forma inmediata y reactiva cuando cambia `detalleVuelo`.
 */
watch(detalleVuelo, (val) => {
  const bols = val?.detalle?.boletos || []
  if (bols.length > 1) {
    pasajerosAdicionales.value = bols.slice(1).map(() =>
      ({ nombre: '', apellido: '', pasaporte: '', telefono: '', pais: '', ciudad: '' })
    )
    erroresPasajeros.value = bols.slice(1).map(() => ({}))
    paxAcState.value = bols.slice(1).map(() => ({
      paisQuery:         '',
      paisesSugeridos:   [],
      paisSel:           null,
      ciudades:          [],
      ciudadQuery:       '',
      ciudadesSugeridas: [],
      ciudadLoading:     false,
      dialCode:          '',
      phoneDigits:       8,
    }))
  }
}, { immediate: true })

/**
 * Devuelve el array de países cargados en memoria.
 * @returns {Array}
 */
function getPaises() {
  return todosLosPaises.value
}

/**
 * Obtiene las ciudades de un país: primero busca en el array local
 * y si no tiene datos, hace un fetch a countriesnow como fallback.
 * @param {string} country - Nombre del país en inglés
 * @returns {Promise<string[]>}
 */
async function getCiudades(country) {
  const found = todosLosPaises.value.find(
    p => p.country.toLowerCase() === country.toLowerCase()
  )
  if (found?.cities?.length) return found.cities

  try {
    const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ country }),
    })
    const d = await r.json()
    return d.data || []
  } catch {
    return []
  }
}

/**
 * Filtra países para el autocompletado de un pasajero adicional y resetea ciudad/teléfono.
 * @param {number} idx - Índice del pasajero adicional
 */
async function onPaxPaisInput(idx) {
  const st = paxAcState.value[idx]
  if (!st) return
  st.paisSel = null
  st.ciudadQuery = ''
  st.ciudadesSugeridas = []
  pasajerosAdicionales.value[idx].pais   = ''
  pasajerosAdicionales.value[idx].ciudad = ''
  const q = st.paisQuery.trim()
  if (q.length < 2) { st.paisesSugeridos = []; return }
  st.paisesSugeridos = getPaises()
    .filter(x => x.country.toLowerCase().includes(q.toLowerCase()))
    .slice(0, 6)
}

/**
 * Fija el país elegido para un pasajero adicional y carga sus ciudades y código telefónico.
 * @param {number} idx - Índice del pasajero
 * @param {object} p   - Objeto de país seleccionado
 */
async function seleccionarPaxPais(idx, p) {
  const st = paxAcState.value[idx]
  if (!st) return
  st.paisSel   = p
  st.paisQuery = p.country
  st.paisesSugeridos = []
  pasajerosAdicionales.value[idx].pais = p.country

  st.ciudadLoading = true
  const ciudades = await getCiudades(p.country)
  st.ciudadLoading = false
  st.ciudades = ciudades

  const info = dialCodesMap.value[p.country.toLowerCase()]
  st.dialCode    = info?.code   ?? ''
  st.phoneDigits = info?.digits ?? 9
  pasajerosAdicionales.value[idx].telefono = ''
}

/**
 * Filtra las ciudades disponibles para el pasajero adicional según lo escrito.
 * @param {number} idx
 */
function onPaxCiudadInput(idx) {
  const st = paxAcState.value[idx]
  if (!st) return
  const q = st.ciudadQuery.toLowerCase()
  st.ciudadesSugeridas = q.length < 2
    ? []
    : (st.ciudades || []).filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  pasajerosAdicionales.value[idx].ciudad = ''
}

/**
 * Confirma la ciudad seleccionada para el pasajero adicional.
 * @param {number} idx
 * @param {string} c - Ciudad elegida
 */
function seleccionarPaxCiudad(idx, c) {
  const st = paxAcState.value[idx]
  if (!st) return
  st.ciudadQuery       = c
  st.ciudadesSugeridas = []
  pasajerosAdicionales.value[idx].ciudad = c
}

/**
 * Formatea el número de teléfono del pasajero adicional según los dígitos requeridos por su país.
 * @param {number} idx - Índice del pasajero
 * @param {Event}  e
 */
function onPaxPhoneInput(idx, e) {
  const st = paxAcState.value[idx]
  if (!st) return
  const n   = st.phoneDigits
  const raw = e.target.value.replace(/\D/g, '').slice(0, n)
  let f = raw
  if (n <= 7)       f = raw.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  else if (n === 8) f = raw.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  else if (n === 9) f = raw.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  else              f = raw.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  pasajerosAdicionales.value[idx].telefono = f
  if (erroresPasajeros.value[idx]) erroresPasajeros.value[idx].telefono = ''
}

/** Datos del pasajero principal (boleto 1). @type {import('vue').Ref<object>} */
const form = ref({ nombre:'', apellido:'', pasaporte:'', pais:'', ciudad:'', telefono:'' })

/** Errores de validación del formulario del pasajero principal. @type {import('vue').Ref<object>} */
const errors     = ref({})

/** Previene doble click mientras se procesa la confirmación. @type {import('vue').Ref<boolean>} */
const submitting = ref(false)

/** Lista de países cargada desde countriesnow. @type {import('vue').Ref<Array>} */
const todosLosPaises    = ref([])

/** Texto escrito en el buscador de país del pasajero principal. @type {import('vue').Ref<string>} */
const paisQuery         = ref('')

/** Países filtrados para el dropdown del pasajero principal. @type {import('vue').Ref<Array>} */
const paisesSugeridos   = ref([])

/** País seleccionado por el pasajero principal (objeto con cities). @type {import('vue').Ref<object|null>} */
const paisSeleccionado  = ref(null)

/** Texto escrito en el buscador de ciudad del pasajero principal. @type {import('vue').Ref<string>} */
const ciudadQuery       = ref('')

/** Ciudades filtradas para el dropdown del pasajero principal. @type {import('vue').Ref<Array>} */
const ciudadesSugeridas = ref([])

/** Código de marcación del país del pasajero principal (ej. '+502'). @type {import('vue').Ref<string>} */
const dialCode     = ref('')

/** Dígitos locales requeridos para el teléfono del pasajero principal. @type {import('vue').Ref<number>} */
const phoneDigits  = ref(8)

/** Mapa de país → { code, digits } construido desde restcountries. @type {import('vue').Ref<Record<string, object>>} */
const dialCodesMap = ref({})

/**
 * Dígitos requeridos por código de marcación, usados como fallback cuando
 * la API de restcountries no retorna sufijos únicos.
 * @type {Record<string, number>}
 */
const knownDigits = {
  '+1':10,'+502':8,'+503':8,'+504':8,'+505':8,'+506':8,'+507':8,
  '+52':10,'+53':8,'+54':10,'+55':11,'+56':9,'+57':10,'+58':10,
  '+34':9,'+44':10,'+49':10,'+33':9,'+39':10,'+81':10,'+86':11,
  '+91':10,'+7':10,'+20':10,'+27':9,'+82':10,'+84':9,
}

/**
 * Extrae el ID numérico del vuelo desde un identificador compuesto (ej. '1-100').
 * @param {string|number} id
 * @returns {number|null}
 */
function parseVueloId(id) {
  if (!id) return null
  const parts = String(id).split('-'), val = parseFloat(parts[parts.length - 1])
  return Number.isFinite(val) ? Math.round(val) : null
}

/**
 * Extrae el ID de proveedor desde el identificador compuesto del vuelo.
 * @param {string|number} id
 * @returns {number|null}
 */
function parseProveedorId(id) { return id ? parseInt(String(id).split('-')[0]) || null : null }

/**
 * Convierte el nombre de clase a su ID numérico del backend.
 * @param {string} clase - 'ejecutiva' o cualquier otro valor (económica)
 * @returns {number}
 */
function claseToId(clase) { return clase === 'ejecutiva' ? 2 : 1 }

/**
 * Suma el total del paquete usando los precios con ganancia del servidor.
 * Si aún no llegaron del servidor, usa el precio calculado del item.
 * @type {import('vue').ComputedRef<number>}
 */
const totalPaquete = computed(() => {
  const tv = detalleVuelo.value?.total_con_ganancia ?? 0
  const th = detalleHotel.value?.total_con_ganancia ?? 0
  if (tv > 0 && th > 0) return tv + th
  return item.value?.precioTotal ?? 0
})

/**
 * Porcentaje de descuento para paquetes leído desde el endpoint de configuración del backend.
 * Se carga en onMounted solo cuando el tipo de item es paquete.
 * @type {import('vue').Ref<number>}
 */
const porcentajeDescuento = ref(0)

/**
 * Monto en dólares del descuento calculado sobre el total del paquete.
 * Retorna 0 si el tipo no es paquete o si no hay porcentaje configurado.
 * @type {import('vue').ComputedRef<number>}
 */
const montoDescuentoPaquete = computed(() => {
  if (tipoItem.value !== 'paquete' || porcentajeDescuento.value <= 0) return 0
  return Math.round(totalPaquete.value * (porcentajeDescuento.value / 100) * 100) / 100
})

/**
 * Convierte segundos a formato mm:ss para mostrar en el temporizador.
 * @param {number} s
 * @returns {string}
 */
function formatTiempo(s) {
  return `${Math.floor(s/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`
}

/**
 * Inicia el temporizador de cuenta regresiva.
 * Si se provee una fecha de expiración absoluta, calcula los segundos reales restantes.
 * @param {number}      seg       - Segundos de duración
 * @param {number|null} expiresAt - Timestamp absoluto de expiración (ms)
 */
function startTimer(seg, expiresAt) {
  const segReales = expiresAt
    ? Math.max(30, Math.floor((expiresAt - Date.now()) / 1000))
    : seg
  if (expiresAt) sessionStorage.setItem('_reserva_expires_at', String(expiresAt))
  tiempoRestante.value = segReales
  tiempoTotal.value    = segReales
  if (timerInterval.value) clearInterval(timerInterval.value)
  timerInterval.value = setInterval(() => {
    tiempoRestante.value = Math.max(0, tiempoRestante.value - 1)
    if (tiempoRestante.value === 0) clearInterval(timerInterval.value)
  }, 1000)
}

/** Limpia el intervalo al desmontar el componente para evitar memory leaks. */
onUnmounted(() => { if (timerInterval.value) clearInterval(timerInterval.value) })

/**
 * Construye el array de vuelos y el proveedorId para la petición de detalle de vuelo.
 * Funciona para vuelos de ida, ida y vuelta, y el vuelo incluido en un paquete.
 * @returns {{ proveedorId: number|null, vuelosArr: Array }}
 */
function buildVuelosPayload() {
  let vuelosArr = [], proveedorId = null
  if (tipoItem.value === 'vuelo') {
    if (item.value.tipoVuelo === 'idaVuelta') {
      const { ida, regreso } = item.value, pax = item.value.busqueda?.cantidadPasajeros || 1
      proveedorId = parseProveedorId(ida.id)
      // Expande un vuelo a N entradas: una por tramo si es con escala, una si es directo.
      // La clase se hereda del vuelo padre (seleccionada a nivel de resultado de búsqueda).
      const expandirVuelo = (v, clase) => {
        if (v.tramos?.length > 1) {
          return v.tramos
            .filter(t => {
              if (!t?.id) { console.warn('[buildVuelosPayload idaVuelta] tramo sin id ignorado:', t); return false }
              return true
            })
            .map(t => ({ vueloId: parseVueloId(t.id), claseId: claseToId(clase), cantidadPasajeros: pax }))
        }
        return [{ vueloId: parseVueloId(v.id), claseId: claseToId(clase), cantidadPasajeros: pax }]
      }
      vuelosArr = [...expandirVuelo(ida, ida.clase), ...expandirVuelo(regreso, regreso.clase)]
    } else {
      const pax = item.value.busqueda?.cantidadPasajeros || 1
      proveedorId = parseProveedorId(item.value.id)
      if (item.value.tramos?.length > 1) {
        // Vuelo con escala: generar una entrada por cada tramo usando su propio vueloId.
        // La clase se selecciona a nivel del vuelo padre y aplica a todos los tramos.
        vuelosArr = item.value.tramos
          .filter(t => {
            if (!t?.id) {
              console.warn('[buildVuelosPayload] tramo sin id ignorado:', t)
              return false
            }
            return true
          })
          .map(t => ({ vueloId: parseVueloId(t.id), claseId: claseToId(item.value.clase), cantidadPasajeros: pax }))
      } else {
        // Vuelo directo (o escala con un solo tramo): comportamiento original.
        vuelosArr = [{ vueloId: parseVueloId(item.value.id), claseId: claseToId(item.value.clase), cantidadPasajeros: pax }]
      }
    }
  } else if (tipoItem.value === 'paquete') {
    const v = item.value.vuelo, pax = item.value.cantidadPersonas || 1
    proveedorId = parseProveedorId(v.id)
    const expandirVueloPaq = (vx, clase) => {
      if (vx.tramos?.length > 1) {
        return vx.tramos
          .filter(t => {
            if (!t?.id) { console.warn('[buildVuelosPayload paquete] tramo sin id ignorado:', t); return false }
            return true
          })
          .map(t => ({ vueloId: parseVueloId(t.id), claseId: claseToId(clase), cantidadPasajeros: pax }))
      }
      return [{ vueloId: parseVueloId(vx.id), claseId: claseToId(clase), cantidadPasajeros: pax }]
    }
    vuelosArr = expandirVueloPaq(v, v.clase)
    if (item.value.vueloRegreso) {
      vuelosArr = [...vuelosArr, ...expandirVueloPaq(item.value.vueloRegreso, item.value.vueloRegreso.clase)]
    }
  }
  return { proveedorId, vuelosArr }
}

/**
 * Construye el payload para el detalle de hotel dentro de un paquete.
 * Soporta habitaciones combinadas (combo) y habitaciones simples.
 * @param {number} reservacionIdArg - ID de la reservación ya creada
 * @returns {object|null}
 */
function buildPaqueteHotelPayload(reservacionIdArg) {
  const h = item.value?.hotel
  if (!h) return null
  let habitaciones = []
  if (h.tipo === 'combo') {
    const contadorCombo = {}
    habitaciones = (h.habs || [])
      .map(hab => {
        const disponibles = hab.habitacionesDisponibles || []
        if (!disponibles.length) return null
        const key = `${hab.cap}-${hab.tipo}`
        const idx = contadorCombo[key] ?? 0
        contadorCombo[key] = idx + 1
        if (idx >= disponibles.length) return null
        return { habitacionId: disponibles[idx].id, fechaCheckIn: h.checkIn, fechaCheckOut: h.checkOut, cantidadPersonas: hab.cap }
      })
      .filter(hab => hab !== null)
  } else {
    const rooms = h.habitacionesDisponibles || []
    if (!rooms.length) return null
    habitaciones = [{
      habitacionId: rooms[0].id,
      fechaCheckIn: h.checkIn,
      fechaCheckOut: h.checkOut,
      cantidadPersonas: h.cantidadPersonas || item.value.cantidadPersonas || 1,
    }]
  }
  if (!habitaciones.length) return null
  return { reservacionId: reservacionIdArg, proveedorId: h.proveedorId, habitaciones }
}

/**
 * Construye el payload para el detalle de hotel en reservas de solo hotel.
 * Lee las habitaciones disponibles directamente del item seleccionado.
 * @param {number} reservacionIdArg - ID de la reservación ya creada
 * @returns {object|null}
 */
function buildHotelPayload(reservacionIdArg) {
  if (!item.value) return null
  const rooms = item.value.habitacionesDisponibles || []
  if (!rooms.length) return null
  return {
    reservacionId: reservacionIdArg,
    proveedorId:   item.value.proveedorId,
    habitaciones:  [{
      habitacionId:     rooms[0].id,
      fechaCheckIn:     item.value.busqueda?.checkIn,
      fechaCheckOut:    item.value.busqueda?.checkOut,
      cantidadPersonas: item.value.busqueda?.cantidadPersonas || 1,
    }],
  }
}

/**
 * Crea la reservación en el backend y luego registra el detalle de vuelo o hotel según el tipo.
 * En caso de error, muestra el overlay de error con opción de reintentar.
 */
async function crearReservacion() {
  creandoReserva.value = true
  errorCreacion.value  = ''
  const tipoReservaId = tipoItem.value === 'hotel' ? 2 : tipoItem.value === 'paquete' ? 3 : 1

  try {
    const res = await fetch(`${API}/api/reservaciones`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tipo_reserva_id: tipoReservaId }),
    })
    if (!res.ok) throw new Error(`Error al crear reserva: ${res.status}`)
    const data = await res.json()

    reservacionId.value = data.id
    noReservacion.value = data.no_reservacion
    sessionStorage.setItem('_reserva_id', String(data.id))
    sessionStorage.setItem('_reserva_no', data.no_reservacion || '')

    let segundos = 600
    if (data.fecha_expiracion) {
      const calc = Math.floor((new Date(data.fecha_expiracion.replace(' ','T')).getTime() - Date.now()) / 1000)
      if (calc > 0) segundos = calc
    }

    if (tipoItem.value === 'vuelo' || tipoItem.value === 'paquete') {
      const { proveedorId, vuelosArr } = buildVuelosPayload()
      const rd = await fetch(`${API}/api/reservaciones/detalle/vuelo`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reservacion_id: data.id, proveedor_id: proveedorId, vuelos: vuelosArr }),
      })
      if (!rd.ok) {
        const e = await rd.json().catch(() => ({}))
        throw new Error(e?.mensaje || 'Los asientos seleccionados ya no están disponibles. Por favor elige otro vuelo.')
      }
      detalleVuelo.value = await rd.json()
      if (detalleVuelo.value?.detalle?.minutosRestantes)
        segundos = detalleVuelo.value.detalle.minutosRestantes * 60

      if (tipoItem.value === 'paquete') {
        const hotelPayload = buildPaqueteHotelPayload(data.id)
        if (hotelPayload) {
          const rh = await fetch(`${API}/api/reservaciones/detalle/hotel`, {
            method: 'POST', credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(hotelPayload),
          })
          if (!rh.ok) throw new Error('La habitación seleccionada ya no está disponible. Vuelve y elige otro hotel.')
          detalleHotel.value = await rh.json()
        }
      }

    } else if (tipoItem.value === 'hotel') {
      const payload = buildHotelPayload(data.id)
      if (payload) {
        const rd = await fetch(`${API}/api/reservaciones/detalle/hotel`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        })
        if (!rd.ok) {
          const e = await rd.json().catch(() => ({}))
          throw new Error(e?.mensaje || `Error detalle hotel: ${rd.status}`)
        }
        detalleHotel.value = await rd.json()
        if (detalleHotel.value?.detalle?.fechaExpiracion) {
          const calc = Math.floor((new Date(detalleHotel.value.detalle.fechaExpiracion.replace(' ','T')).getTime() - Date.now()) / 1000)
          if (calc > 0) segundos = calc
        }
      }
    }

    startTimer(segundos)
  } catch (err) {
    console.error('Error en creación:', err)
    errorCreacion.value = err.message || 'No se pudo crear la reserva. Intenta de nuevo.'
  } finally {
    creandoReserva.value = false
  }
}

/**
 * Al montar: carga países y dial codes, lee el item de sessionStorage, carga el descuento
 * de paquete si aplica y recupera una reservación previa si aún está vigente.
 */
onMounted(async () => {
  try {
    const r = await fetch('https://countriesnow.space/api/v0.1/countries')
    const d = await r.json()
    todosLosPaises.value = d.data || []
  } catch { /**/ }

  try {
    const r = await fetch('https://restcountries.com/v3.1/all?fields=name,idd')
    const d = await r.json()
    d.forEach(p => {
      if (p.idd?.root) {
        const s    = p.idd.suffixes ?? ['']
        const code = s.length === 1 ? p.idd.root + s[0] : p.idd.root
        dialCodesMap.value[p.name.common.toLowerCase()] = {
          code,
          digits: knownDigits[code] ?? 9,
        }
      }
    })
  } catch { /**/ }

  const vuelo   = sessionStorage.getItem('vuelo_seleccionado')
  const hotel   = sessionStorage.getItem('hotel_seleccionado')
  const paquete = sessionStorage.getItem('paquete_seleccionado')

  if (vuelo)        { item.value = JSON.parse(vuelo);   tipoItem.value = 'vuelo'   }
  else if (hotel)   { item.value = JSON.parse(hotel);   tipoItem.value = 'hotel'   }
  else if (paquete) { item.value = JSON.parse(paquete); tipoItem.value = 'paquete' }

  if (!item.value) { creandoReserva.value = false; return }

  // Cargar porcentaje de descuento desde el backend si el tipo es paquete
  if (tipoItem.value === 'paquete') {
    try {
      const rd = await fetch(`${API}/api/configuracion/descuento`)
      if (rd.ok) {
        const dd = await rd.json()
        porcentajeDescuento.value = dd.porcentaje_descuento ?? 0
      }
    } catch { /**/ }
  }

  const savedExpiresAt     = sessionStorage.getItem('_reserva_expires_at')
  const savedReservacionId = sessionStorage.getItem('_reserva_id')
  const savedNoReservacion = sessionStorage.getItem('_reserva_no')

  if (savedExpiresAt && savedReservacionId) {
    const segsRestantes = Math.floor((Number(savedExpiresAt) - Date.now()) / 1000)
    if (segsRestantes > 30) {
      reservacionId.value = savedReservacionId
      noReservacion.value = savedNoReservacion || ''
      startTimer(segsRestantes, Number(savedExpiresAt))
      creandoReserva.value = false
      return
    }
    sessionStorage.removeItem('_reserva_expires_at')
    sessionStorage.removeItem('_reserva_id')
    sessionStorage.removeItem('_reserva_no')
  }

  if (window.__reservaPromise) {
    try {
      const resultado = await window.__reservaPromise
      window.__reservaPromise = null
      if (resultado) {
        if (tipoItem.value !== 'hotel' && !resultado.detalle) {
          await crearReservacion(); return
        }
        reservacionId.value = resultado.reserva.id
        noReservacion.value = resultado.reserva.no_reservacion
        sessionStorage.setItem('_reserva_id', String(resultado.reserva.id))
        sessionStorage.setItem('_reserva_no', resultado.reserva.no_reservacion || '')

        if (tipoItem.value === 'hotel') {
          detalleHotel.value = resultado.detalle
        } else {
          detalleVuelo.value = resultado.detalle
          if (tipoItem.value === 'paquete') {
            const hotelPayload = buildPaqueteHotelPayload(resultado.reserva.id)
            if (hotelPayload) {
              const rh = await fetch(`${API}/api/reservaciones/detalle/hotel`, {
                method: 'POST', credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(hotelPayload),
              })
              if (!rh.ok) {
                errorCreacion.value = 'La habitación seleccionada ya no está disponible. Vuelve y elige otro hotel.'
                creandoReserva.value = false; return
              }
              detalleHotel.value = await rh.json()
            }
          }
        }
        startTimer(resultado.segundos, resultado.expiresAt)
        creandoReserva.value = false; return
      }
    } catch { window.__reservaPromise = null }
  }

  await crearReservacion()
})

/**
 * Filtra la lista de países para el autocompletado del pasajero principal.
 * Resetea el país y ciudad si el texto cambia.
 */
function onPaisInput() {
  const q = paisQuery.value.toLowerCase()
  paisesSugeridos.value = q.length < 2
    ? []
    : todosLosPaises.value.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6)
  if (!paisSeleccionado.value) form.value.pais = ''
}

/**
 * Confirma el país del pasajero principal y actualiza el prefijo telefónico y ciudades disponibles.
 * @param {object} pais - País seleccionado de la lista
 */
function seleccionarPais(pais) {
  paisSeleccionado.value  = pais
  paisQuery.value         = pais.country
  form.value.pais         = pais.country
  paisesSugeridos.value   = []
  ciudadQuery.value       = ''
  form.value.ciudad       = ''
  ciudadesSugeridas.value = []
  const info = dialCodesMap.value[pais.country.toLowerCase()]
  dialCode.value      = info?.code   ?? ''
  phoneDigits.value   = info?.digits ?? 9
  form.value.telefono = ''
}

/** Limpia el campo de país si se escribe texto pero no se selecciona ninguno. */
function validarPais() {
  if (paisQuery.value && !paisSeleccionado.value) { paisQuery.value = ''; paisesSugeridos.value = [] }
}

/** Filtra ciudades del país seleccionado según lo escrito en el campo. */
function onCiudadInput() {
  if (!paisSeleccionado.value) return
  const q = ciudadQuery.value.toLowerCase()
  ciudadesSugeridas.value = q.length < 2
    ? []
    : (paisSeleccionado.value.cities || []).filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

/**
 * Confirma la ciudad seleccionada del pasajero principal.
 * @param {string} c - Ciudad elegida
 */
function seleccionarCiudad(c) { ciudadQuery.value = c; form.value.ciudad = c; ciudadesSugeridas.value = [] }

/** Limpia el campo de ciudad si se escribe texto pero no se selecciona ninguna. */
function validarCiudad() {
  if (ciudadQuery.value && !form.value.ciudad) { ciudadQuery.value = ''; ciudadesSugeridas.value = [] }
}

/** Cantidad de dígitos numéricos ingresados en el teléfono del pasajero principal. @type {import('vue').ComputedRef<number>} */
const telefonoDigitos  = computed(() => form.value.telefono.replace(/\D/g, '').length)

/** Placeholder del campo de teléfono generado según los dígitos del país. @type {import('vue').ComputedRef<string>} */
const phonePlaceholder = computed(() => {
  const n = phoneDigits.value, s = '5'.repeat(n)
  if (n <= 7)  return s.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  if (n === 8) return s.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  if (n === 9) return s.replace(/^(\d{3})(\d{3})(\d{3})/, '$1 $2 $3')
  return s.replace(/^(\d{3})(\d{3})(\d{4})/, '$1 $2 $3')
})

/**
 * Limpia no numéricos, recorta y aplica formato visual al teléfono del pasajero principal.
 * @param {Event} e
 */
function onPhoneInput(e) {
  const raw = e.target.value.replace(/\D/g, '').slice(0, phoneDigits.value)
  const n = phoneDigits.value
  let f = raw
  if (n <= 7)       f = raw.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  else if (n === 8) f = raw.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  else if (n === 9) f = raw.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  else              f = raw.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  form.value.telefono = f
  errors.value.telefono = ''
}

/**
 * Formatea minutos de duración de vuelo a texto legible (ej. '2h 30m').
 * @param {number} min
 * @returns {string}
 */
function formatDuracion(min) {
  return min ? `${Math.floor(min/60)}h${min%60>0?' '+min%60+'m':''}` : '--'
}

/**
 * Valida los formularios de todos los pasajeros y, si son correctos,
 * guarda los datos de pasajeros en el backend y navega al siguiente paso.
 * Guarda la expiración absoluta en sessionStorage antes de ir a selección de asientos
 * para que el timer se restaure correctamente en esa vista.
 * @returns {Promise<void>}
 */
async function handleReservar() {
  errors.value = {}
  const f = form.value

  if (tipoItem.value !== 'hotel') {
    if (!f.nombre)    errors.value.nombre   = 'Campo requerido'
    if (!f.apellido)  errors.value.apellido = 'Campo requerido'
    if (!f.pasaporte) errors.value.pasaporte = 'Campo requerido'
    if (!f.pais)      errors.value.pais     = 'Selecciona un país de la lista'
    if (!f.ciudad)    errors.value.ciudad   = 'Selecciona una ciudad de la lista'
    if (dialCode.value && telefonoDigitos.value !== phoneDigits.value)
      errors.value.telefono = `Se requieren ${phoneDigits.value} dígitos`

    let adicionalOk = true
    erroresPasajeros.value = pasajerosAdicionales.value.map(p => {
      const e = {}
      if (!p.nombre.trim())    { e.nombre    = 'Requerido'; adicionalOk = false }
      if (!p.apellido.trim())  { e.apellido  = 'Requerido'; adicionalOk = false }
      if (!p.pasaporte.trim()) { e.pasaporte = 'Requerido'; adicionalOk = false }
      if (!p.telefono.trim())  { e.telefono  = 'Requerido'; adicionalOk = false }
      if (!p.pais.trim())      { e.pais      = 'Requerido'; adicionalOk = false }
      if (!p.ciudad.trim())    { e.ciudad    = 'Requerido'; adicionalOk = false }
      return e
    })

    if (Object.keys(errors.value).length || !adicionalOk) return
  }

  if (tiempoRestante.value === 0) {
    errors.value.general = 'La reserva ha expirado. Realiza una nueva búsqueda.'
    return
  }

  submitting.value = true
  try {
    const telefonoCompleto = dialCode.value
      ? `${dialCode.value} ${f.telefono.replace(/\s/g, '')}`
      : f.telefono

    const { proveedorId, vuelosArr } = tipoItem.value !== 'hotel'
      ? buildVuelosPayload()
      : { proveedorId: item.value?.proveedorId, vuelosArr: [] }

    if ((tipoItem.value === 'vuelo' || tipoItem.value === 'paquete') && boletos.value.length > 0) {
      const pasajerosPayload = boletos.value.map((boleto, idx) => {
        if (idx === 0) {
          return {
            boletoId:  boleto.boletoId,
            nombre:    f.nombre,
            apellido:  f.apellido,
            pasaporte: f.pasaporte,
            telefono:  telefonoCompleto,
            pais:      f.pais,
            ciudad:    f.ciudad,
          }
        }
        const pax = pasajerosAdicionales.value[idx - 1]
        const st  = paxAcState.value[idx - 1]
        const tel = st?.dialCode
          ? `${st.dialCode} ${pax.telefono.replace(/\s/g, '')}`
          : pax.telefono
        return {
          boletoId:  boleto.boletoId,
          nombre:    pax.nombre,
          apellido:  pax.apellido,
          pasaporte: pax.pasaporte,
          telefono:  tel,
          pais:      pax.pais,
          ciudad:    pax.ciudad,
        }
      })

      const resP = await fetch(`${API}/api/reservaciones/detalle/pasajeros-vuelo`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reservacion_id: reservacionId.value,
          proveedor_id:   proveedorId,
          pasajeros:      pasajerosPayload,
        }),
      })
      if (!resP.ok) {
        const e = await resP.json().catch(() => ({}))
        throw new Error(e?.mensaje || 'Error al guardar datos de pasajeros.')
      }
    }

    sessionStorage.setItem('checkout_data', JSON.stringify({
      reservacionId:             reservacionId.value,
      noReservacion:             noReservacion.value,
      detalleVuelo:              detalleVuelo.value,
      detalleHotel:              detalleHotel.value,
      item:                      item.value,
      tipoItem:                  tipoItem.value,
      proveedorId,
      vuelos:                    vuelosArr,
      pasajero:                  { ...f, telefono: telefonoCompleto },
      tiempoRestanteAlConfirmar: tiempoRestante.value,
    }))

    if (timerInterval.value) clearInterval(timerInterval.value)

    if (tipoItem.value === 'vuelo' || tipoItem.value === 'paquete') {
      // Guardar expiración absoluta para que SeleccionAsientos restaure el timer correctamente
      sessionStorage.setItem('_reserva_expires_at', String(Date.now() + tiempoRestante.value * 1000))
      router.push('/seleccion-asientos')
    } else {
      sessionStorage.removeItem('_reserva_expires_at')
      sessionStorage.removeItem('_reserva_id')
      sessionStorage.removeItem('_reserva_no')
      router.push('/checkout')
    }

  } catch (err) {
    console.error(err)
    errors.value.general = err.message || 'Error inesperado. Intenta de nuevo.'
  } finally {
    submitting.value = false
  }
}
</script>