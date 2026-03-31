<template>
  <div class="page">
    <Encabezado />

    <!-- ═══ OVERLAY: Creando reserva ═══ -->
    <div v-if="creandoReserva" class="res-overlay">
      <div class="res-overlay__card">
        <div class="res-spinner-xl"></div>
        <p class="res-overlay__txt">Asegurando disponibilidad...</p>
        <small class="res-overlay__sub">Esto solo toma un momento</small>
      </div>
    </div>

    <!-- ═══ OVERLAY: Error crítico (no se pudo crear) ═══ -->
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

    <!-- ═══ OVERLAY: Reserva expirada ═══ -->
    <div v-else-if="tiempoRestante === 0 && reservacionId" class="res-overlay">
      <div class="res-overlay__card res-overlay__card--error">
        <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="52" height="52">
          <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
        </svg>
        <h3 class="res-overlay__titulo">Reserva expirada</h3>
        <p class="res-overlay__msg">
          El tiempo para completar la reserva <strong>{{ noReservacion }}</strong> ha vencido.
          Los asientos pueden ya no estar disponibles.
        </p>
        <button class="res-btn res-btn--yellow" @click="$router.push('/principal')" type="button">
          Realizar nueva búsqueda
        </button>
      </div>
    </div>

    <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
    <div v-else class="res-page">
      <div class="res-container">

        <!-- Header -->
        <div class="res-header">
          <div class="res-header__text">
            <h1 class="res-header__title">Completar Reserva</h1>
            <p class="res-header__sub">Completa los datos del pasajero para confirmar</p>
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

          <!-- ═══ IZQUIERDA: FORMULARIO ═══ -->
          <div class="res-form-col">
            <div class="res-form-card">
              <div class="res-form-card__head">
                <div class="res-form-card__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <h2 class="res-form-card__title">Datos del Pasajero</h2>
              </div>

              <div class="res-form-card__body">

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

                <div class="res-form-row">
                  <div class="res-field">
                    <label class="res-field__label">Número de Pasaporte *</label>
                    <input class="res-field__input" type="text" v-model="form.pasaporte"
                      placeholder="Solo números" autocomplete="off"
                      @input="form.pasaporte = form.pasaporte.replace(/\D/g, '')" />
                    <span v-if="errors.pasaporte" class="res-field__error">{{ errors.pasaporte }}</span>
                  </div>
                  <div class="res-field">
                    <label class="res-field__label">Fecha de Nacimiento *</label>
                    <input class="res-field__input" type="date" v-model="form.fechaNacimiento" />
                    <span v-if="errors.fechaNacimiento" class="res-field__error">{{ errors.fechaNacimiento }}</span>
                  </div>
                </div>

                <div class="res-form-row">
                  <div class="res-field res-field--full">
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
                  <div class="res-field res-field--full">
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

                <div class="res-form-row">
                  <div class="res-field res-field--full">
                    <label class="res-field__label">
                      Teléfono de contacto *
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
                  <div class="res-field res-field--full">
                    <label class="res-field__label">Correo electrónico *</label>
                    <input class="res-field__input" type="email" v-model="form.email"
                      placeholder="ejemplo@correo.com" autocomplete="off" />
                    <span v-if="errors.email" class="res-field__error">{{ errors.email }}</span>
                  </div>
                </div>

                <div class="res-field res-field--full" style="margin-top:8px">
                  <label class="res-field__label">
                    Nota adicional <span class="res-field__optional">(opcional)</span>
                  </label>
                  <textarea class="res-field__textarea" v-model="form.nota"
                    placeholder="Peticiones especiales, alergias, asistencia, etc." rows="3"></textarea>
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
          </div>

          <!-- ═══ DERECHA: RESUMEN ═══ -->
          <aside class="res-summary-col">
            <div class="res-summary">

              <!-- Head con timer -->
              <div class="res-summary__head">
                <div class="res-summary__head-row">
                  <h2 class="res-summary__title">Resumen</h2>
                  <span v-if="noReservacion" class="res-summary__num">{{ noReservacion }}</span>
                </div>
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
                      :style="{ width: (tiempoRestante / tiempoTotal * 100) + '%' }">
                    </div>
                  </div>
                </div>
              </div>

              <!-- Sin item -->
              <div v-if="!item" class="res-summary__empty">
                <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="1.5" width="40" height="40">
                  <rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>
                </svg>
                <p>No hay ningún item seleccionado.</p>
                <button class="res-btn res-btn--ghost" @click="$router.push('/principal')" type="button">
                  Buscar viajes
                </button>
              </div>

              <!-- ══ VUELO SOLO IDA ══ -->
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
                <div v-if="detalleVuelo?.detalle?.boletos?.length" class="res-boletos">
                  <p class="res-boletos__titulo">Boletos confirmados</p>
                  <div v-for="b in detalleVuelo.detalle.boletos" :key="b.boletoId" class="res-boleto">
                    <div class="res-boleto__left">
                      <span class="res-boleto__no">{{ b.noBoleto }}</span>
                      <span class="res-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                    </div>
                    <span class="res-boleto__precio">${{ b.precio.toFixed(2) }}</span>
                  </div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total</span>
                  <span class="res-summary__precio">
                    ${{ (detalleVuelo?.total_con_ganancia ?? (item.precio || 0) * (item.busqueda?.cantidadPasajeros || 1)).toFixed(2) }}
                  </span>
                </div>
              </template>

              <!-- ══ VUELO IDA Y VUELTA ══ -->
              <template v-else-if="tipoItem === 'vuelo' && item.tipoVuelo === 'idaVuelta'">
                <div class="res-summary__tag res-summary__tag--vuelo">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  Vuelo · Ida y vuelta
                </div>

                <div class="res-sub-vuelo">
                  <div class="res-sub-vuelo__badge">Ida</div>
                  <div class="res-summary__aerolinea" style="padding:8px 20px 0">
                    <strong>{{ item.ida?.aerolinea }}</strong>
                    <span class="res-summary__num-vuelo">Nro. {{ item.ida?.numeroVuelo }}</span>
                  </div>
                  <div class="res-summary__ruta">
                    <div class="res-summary__punto">
                      <span class="res-summary__iata">{{ item.ida?.origenCodigo }}</span>
                      <span class="res-summary__ciudad">{{ item.ida?.origenCiudad }}</span>
                      <span class="res-summary__hora">{{ item.ida?.horaSalida }}</span>
                    </div>
                    <div class="res-summary__track">
                      <div class="res-summary__track-line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <div class="res-summary__track-line"></div>
                    </div>
                    <div class="res-summary__punto res-summary__punto--r">
                      <span class="res-summary__iata">{{ item.ida?.destinoCodigo }}</span>
                      <span class="res-summary__ciudad">{{ item.ida?.destinoCiudad }}</span>
                      <span class="res-summary__hora">{{ item.ida?.horaLlegada }}</span>
                    </div>
                  </div>
                  <div class="res-sub-vuelo__meta">
                    <span style="text-transform:capitalize">{{ item.ida?.clase }}</span>
                    <span>·</span>
                    <span>{{ formatDuracion(item.ida?.duracionMinutos) }}</span>
                    <span>·</span>
                    <span>${{ ((item.ida?.precio || 0) * (item.busqueda?.cantidadPasajeros || 1)).toFixed(2) }}</span>
                  </div>
                </div>

                <div class="res-sub-vuelo res-sub-vuelo--regreso">
                  <div class="res-sub-vuelo__badge res-sub-vuelo__badge--regreso">Regreso</div>
                  <div class="res-summary__aerolinea" style="padding:8px 20px 0">
                    <strong>{{ item.regreso?.aerolinea }}</strong>
                    <span class="res-summary__num-vuelo">Nro. {{ item.regreso?.numeroVuelo }}</span>
                  </div>
                  <div class="res-summary__ruta">
                    <div class="res-summary__punto">
                      <span class="res-summary__iata">{{ item.regreso?.origenCodigo }}</span>
                      <span class="res-summary__ciudad">{{ item.regreso?.origenCiudad }}</span>
                      <span class="res-summary__hora">{{ item.regreso?.horaSalida }}</span>
                    </div>
                    <div class="res-summary__track">
                      <div class="res-summary__track-line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <div class="res-summary__track-line"></div>
                    </div>
                    <div class="res-summary__punto res-summary__punto--r">
                      <span class="res-summary__iata">{{ item.regreso?.destinoCodigo }}</span>
                      <span class="res-summary__ciudad">{{ item.regreso?.destinoCiudad }}</span>
                      <span class="res-summary__hora">{{ item.regreso?.horaLlegada }}</span>
                    </div>
                  </div>
                  <div class="res-sub-vuelo__meta">
                    <span style="text-transform:capitalize">{{ item.regreso?.clase }}</span>
                    <span>·</span>
                    <span>{{ formatDuracion(item.regreso?.duracionMinutos) }}</span>
                    <span>·</span>
                    <span>${{ ((item.regreso?.precio || 0) * (item.busqueda?.cantidadPasajeros || 1)).toFixed(2) }}</span>
                  </div>
                </div>

                <div v-if="detalleVuelo?.detalle?.boletos?.length" class="res-boletos">
                  <p class="res-boletos__titulo">Boletos confirmados</p>
                  <div v-for="b in detalleVuelo.detalle.boletos" :key="b.boletoId" class="res-boleto">
                    <div class="res-boleto__left">
                      <span class="res-boleto__no">{{ b.noBoleto }}</span>
                      <span class="res-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }} · Vuelo {{ b.numeroVuelo }}</span>
                    </div>
                    <span class="res-boleto__precio">${{ b.precio.toFixed(2) }}</span>
                  </div>
                </div>

                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total ({{ item.busqueda?.cantidadPasajeros || 1 }} pax)</span>
                  <span class="res-summary__precio">
                    ${{ (detalleVuelo?.total_con_ganancia ?? (((item.ida?.precio || 0) + (item.regreso?.precio || 0)) * (item.busqueda?.cantidadPasajeros || 1))).toFixed(2) }}
                  </span>
                </div>
              </template>

              <!-- ══ HOTEL ══ -->
              <template v-else-if="tipoItem === 'hotel'">
                <div class="res-summary__tag res-summary__tag--hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="13" height="13">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                  Hospedaje
                </div>
                <h3 class="res-summary__hotel-nombre">{{ item.nombreHotel }}</h3>
                <p class="res-summary__hotel-ubicacion">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                  </svg>
                  {{ item.ciudad }}
                </p>
                <div class="res-summary__details">
                  <div class="res-summary__detail-row"><span>Habitación</span><span>{{ item.tipoHabitacion }}</span></div>
                  <div class="res-summary__detail-row"><span>Cama</span><span>{{ item.tipoCama }}</span></div>
                  <div class="res-summary__detail-row"><span>Check-in</span><span>{{ item.checkIn }}</span></div>
                  <div class="res-summary__detail-row"><span>Check-out</span><span>{{ item.checkOut }}</span></div>
                  <div class="res-summary__detail-row"><span>Noches</span><span>{{ item.noches }}</span></div>
                  <div class="res-summary__detail-row"><span>Personas</span><span>{{ item.cantidadPersonas }}</span></div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total ({{ item.noches }} noches)</span>
                  <span class="res-summary__precio">${{ item.totalEstancia?.toFixed(2) }}</span>
                </div>
              </template>

              <!-- ══ PAQUETE ══ -->
              <template v-else-if="tipoItem === 'paquete'">
                <div class="res-summary__tag res-summary__tag--paquete">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                  Paquete completo
                </div>
                <div class="res-sub-vuelo">
                  <div class="res-sub-vuelo__badge">Vuelo ida</div>
                  <div class="res-summary__ruta" style="padding:10px 20px">
                    <div class="res-summary__punto">
                      <span class="res-summary__iata">{{ item.vuelo?.origenCodigo }}</span>
                      <span class="res-summary__ciudad">{{ item.vuelo?.aerolinea }}</span>
                    </div>
                    <div class="res-summary__track">
                      <div class="res-summary__track-line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <div class="res-summary__track-line"></div>
                    </div>
                    <div class="res-summary__punto res-summary__punto--r">
                      <span class="res-summary__iata">{{ item.vuelo?.destinoCodigo }}</span>
                      <span class="res-summary__ciudad" style="text-transform:capitalize">{{ item.vuelo?.clase }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="item.vueloRegreso" class="res-sub-vuelo res-sub-vuelo--regreso">
                  <div class="res-sub-vuelo__badge res-sub-vuelo__badge--regreso">Vuelo regreso</div>
                  <div class="res-summary__ruta" style="padding:10px 20px">
                    <div class="res-summary__punto">
                      <span class="res-summary__iata">{{ item.vueloRegreso?.origenCodigo }}</span>
                      <span class="res-summary__ciudad">{{ item.vueloRegreso?.aerolinea }}</span>
                    </div>
                    <div class="res-summary__track">
                      <div class="res-summary__track-line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <div class="res-summary__track-line"></div>
                    </div>
                    <div class="res-summary__punto res-summary__punto--r">
                      <span class="res-summary__iata">{{ item.vueloRegreso?.destinoCodigo }}</span>
                      <span class="res-summary__ciudad" style="text-transform:capitalize">{{ item.vueloRegreso?.clase }}</span>
                    </div>
                  </div>
                </div>
                <div class="res-paquete-hotel">
                  <div class="res-sub-vuelo__badge" style="margin:0 0 8px">Hotel</div>
                  <span class="res-paquete-hotel__nombre">{{ item.hotel?.nombreHotel }}</span>
                  <span class="res-paquete-hotel__meta">
                    {{ item.hotel?.ciudad }} · {{ item.noches }}n · {{ item.hotel?.tipoHabitacion }}
                  </span>
                </div>
                <div v-if="detalleVuelo?.detalle?.boletos?.length" class="res-boletos">
                  <p class="res-boletos__titulo">Boletos confirmados</p>
                  <div v-for="b in detalleVuelo.detalle.boletos" :key="b.boletoId" class="res-boleto">
                    <div class="res-boleto__left">
                      <span class="res-boleto__no">{{ b.noBoleto }}</span>
                      <span class="res-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                    </div>
                    <span class="res-boleto__precio">${{ b.precio.toFixed(2) }}</span>
                  </div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Total paquete</span>
                  <span class="res-summary__precio">
                    ${{ (detalleVuelo?.total_con_ganancia ?? item.precioTotal ?? 0).toFixed(2) }}
                  </span>
                </div>
              </template>

              <!-- Botón confirmar -->
              <div v-if="item" class="res-summary__footer">
                <button class="res-btn res-btn--yellow res-btn--full"
                  @click="handleReservar" type="button"
                  :disabled="submitting || tiempoRestante === 0">
                  <div v-if="submitting" class="res-spinner-sm"></div>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15">
                    <path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/>
                    <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/>
                    <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
                  </svg>
                  {{ submitting ? 'Procesando...' : 'Confirmar Reserva' }}
                </button>
                <p class="res-summary__aviso">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  Los datos deben coincidir con el pasaporte
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/reserva.css'

const router = useRouter()
const API    = 'http://localhost:8080'

// ── Estado de reservación ─────────────────────────────────────
const reservacionId  = ref(null)
const noReservacion  = ref('')
const detalleVuelo   = ref(null)
const tiempoRestante = ref(600)
const tiempoTotal    = ref(600)  // base para la barra de progreso
const creandoReserva = ref(true)
const errorCreacion  = ref('')
const timerInterval  = ref(null)

// ── Item seleccionado ─────────────────────────────────────────
const item     = ref(null)
const tipoItem = ref('')  // 'vuelo' | 'hotel' | 'paquete'

// ── Formulario ────────────────────────────────────────────────
const form = ref({
  nombre: '', apellido: '', pasaporte: '',
  fechaNacimiento: '', pais: '', ciudad: '',
  telefono: '', email: '', nota: '',
})
const errors     = ref({})
const submitting = ref(false)

// ── Países / ciudades ─────────────────────────────────────────
const todosLosPaises    = ref([])
const paisQuery         = ref('')
const paisesSugeridos   = ref([])
const paisSeleccionado  = ref(null)
const ciudadQuery       = ref('')
const ciudadesSugeridas = ref([])

// ── Teléfono ──────────────────────────────────────────────────
const dialCode     = ref('')
const phoneDigits  = ref(8)
const dialCodesMap = ref({})

const knownDigits = {
  '+1':10,'+502':8,'+503':8,'+504':8,'+505':8,'+506':8,'+507':8,
  '+52':10,'+53':8,'+54':10,'+55':11,'+56':9,'+57':10,'+58':10,
  '+34':9,'+44':10,'+49':10,'+33':9,'+39':10,'+81':10,'+86':11,
  '+91':10,'+7':10,'+20':10,'+27':9,'+82':10,'+84':9,
}

// ── Helpers de ID ─────────────────────────────────────────────
// Extrae el vueloId numérico del id compuesto "proveedorId-[d|e]-vueloId"
// Ej: "1-d-52" → 52
function parseVueloId(compositeId) {
  if (!compositeId) return null
  const parts = String(compositeId).split('-')
  const val   = parseFloat(parts[parts.length - 1])
  return Number.isFinite(val) ? Math.round(val) : null
}

// Extrae el proveedorId del id compuesto. Ej: "1-d-52" → 1
function parseProveedorId(compositeId) {
  if (!compositeId) return null
  return parseInt(String(compositeId).split('-')[0]) || null
}

// Convierte clase string a claseId: economica→1, ejecutiva→2
function claseToId(clase) {
  return clase === 'ejecutiva' ? 2 : 1
}

// ── Timer ─────────────────────────────────────────────────────
function formatTiempo(seconds) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = (seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

function startTimer(segundosRestantes) {
  tiempoRestante.value = segundosRestantes
  tiempoTotal.value    = segundosRestantes
  if (timerInterval.value) clearInterval(timerInterval.value)
  timerInterval.value = setInterval(() => {
    tiempoRestante.value = Math.max(0, tiempoRestante.value - 1)
    if (tiempoRestante.value === 0) clearInterval(timerInterval.value)
  }, 1000)
}

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value)
})

// ── Construir payload de vuelos ───────────────────────────────
function buildVuelosPayload() {
  let vuelosArr   = []
  let proveedorId = null

  if (tipoItem.value === 'vuelo') {
    if (item.value.tipoVuelo === 'idaVuelta') {
      const ida     = item.value.ida
      const regreso = item.value.regreso
      const pax     = item.value.busqueda?.cantidadPasajeros || 1
      proveedorId   = parseProveedorId(ida.id)
      vuelosArr = [
        { vueloId: parseVueloId(ida.id),     claseId: claseToId(ida.clase),     cantidadPasajeros: pax },
        { vueloId: parseVueloId(regreso.id), claseId: claseToId(regreso.clase), cantidadPasajeros: pax },
      ]
    } else {
      const pax   = item.value.busqueda?.cantidadPasajeros || 1
      proveedorId = parseProveedorId(item.value.id)
      vuelosArr   = [{
        vueloId:           parseVueloId(item.value.id),
        claseId:           claseToId(item.value.clase),
        cantidadPasajeros: pax,
      }]
    }
  } else if (tipoItem.value === 'paquete') {
    const v   = item.value.vuelo
    const pax = item.value.cantidadPersonas || 1
    proveedorId = parseProveedorId(v.id)
    vuelosArr   = [{
      vueloId:           parseVueloId(v.id),
      claseId:           claseToId(v.clase),
      cantidadPasajeros: pax,
    }]
    if (item.value.vueloRegreso) {
      const vr = item.value.vueloRegreso
      vuelosArr.push({
        vueloId:           parseVueloId(vr.id),
        claseId:           claseToId(vr.clase),
        cantidadPasajeros: pax,
      })
    }
  }

  return { proveedorId, vuelosArr }
}

// ── Crear reservación + detalle (llamado al montar) ───────────
async function crearReservacion() {
  creandoReserva.value = true
  errorCreacion.value  = ''

  let tipoReservaId = 1
  if (tipoItem.value === 'hotel')   tipoReservaId = 2
  if (tipoItem.value === 'paquete') tipoReservaId = 3

  try {
    // PASO 1: Crear reservación
    const res = await fetch(`${API}/api/reservaciones`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tipo_reserva_id: tipoReservaId }),
    })
    if (!res.ok) throw new Error(`Error al crear reserva: ${res.status}`)
    const data = await res.json()

    reservacionId.value = data.id
    noReservacion.value = data.no_reservacion

    // Calcular segundos restantes desde fecha_expiracion del servidor
    let segundos = 600
    if (data.fecha_expiracion) {
      const expDate = new Date(data.fecha_expiracion.replace(' ', 'T'))
      const calc    = Math.floor((expDate.getTime() - Date.now()) / 1000)
      if (calc > 0) segundos = calc
    }

    // PASO 2: Agregar detalle de vuelo inmediatamente (vuelo y paquete)
    if (tipoItem.value === 'vuelo' || tipoItem.value === 'paquete') {
      const { proveedorId, vuelosArr } = buildVuelosPayload()

      const resDetalle = await fetch(`${API}/api/reservaciones/detalle/vuelo`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reservacion_id: data.id,
          proveedor_id:   proveedorId,
          vuelos:         vuelosArr,
        }),
      })
      if (!resDetalle.ok) {
        const errData = await resDetalle.json().catch(() => ({}))
        throw new Error(errData?.mensaje || `Error al agregar detalle: ${resDetalle.status}`)
      }
      detalleVuelo.value = await resDetalle.json()

      // Si el detalle devuelve minutosRestantes, usarlo para mayor precisión
      if (detalleVuelo.value?.detalle?.minutosRestantes) {
        segundos = detalleVuelo.value.detalle.minutosRestantes * 60
      }
    }

    // PASO 3: Arrancar timer con segundos calculados desde el servidor
    startTimer(segundos)

  } catch (err) {
    console.error('Error en creación/detalle:', err)
    errorCreacion.value = err.message || 'No se pudo crear la reserva. Verifica tu conexión e intenta de nuevo.'
  } finally {
    creandoReserva.value = false
  }
}

// ── onMounted ─────────────────────────────────────────────────
onMounted(async () => {
  // Cargar países
  try {
    const res  = await fetch('https://countriesnow.space/api/v0.1/countries')
    const data = await res.json()
    todosLosPaises.value = data.data || []
  } catch { console.warn('No se cargaron países') }

  // Cargar dial codes
  try {
    const res  = await fetch('https://restcountries.com/v3.1/all?fields=name,idd')
    const data = await res.json()
    data.forEach(p => {
      if (p.idd?.root) {
        const suffixes = p.idd.suffixes ?? ['']
        const code     = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root
        const digits   = knownDigits[code] ?? 9
        dialCodesMap.value[p.name.common.toLowerCase()] = { code, digits }
      }
    })
  } catch { console.warn('No se cargaron dial codes') }

  // Leer item de sessionStorage
  const vuelo   = sessionStorage.getItem('vuelo_seleccionado')
  const hotel   = sessionStorage.getItem('hotel_seleccionado')
  const paquete = sessionStorage.getItem('paquete_seleccionado')

  if (vuelo)        { item.value = JSON.parse(vuelo);   tipoItem.value = 'vuelo'   }
  else if (hotel)   { item.value = JSON.parse(hotel);   tipoItem.value = 'hotel'   }
  else if (paquete) { item.value = JSON.parse(paquete); tipoItem.value = 'paquete' }

  // Crear reservación + detalle inmediatamente al cargar la página
  if (item.value) {
    await crearReservacion()
  } else {
    creandoReserva.value = false
  }
})

// ── Autocomplete países ───────────────────────────────────────
function onPaisInput() {
  const q = paisQuery.value.toLowerCase()
  paisesSugeridos.value = q.length < 2
    ? []
    : todosLosPaises.value.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6)
  if (!paisSeleccionado.value) form.value.pais = ''
}

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

function validarPais() {
  if (paisQuery.value && !paisSeleccionado.value) {
    paisQuery.value       = ''
    paisesSugeridos.value = []
  }
}

function onCiudadInput() {
  if (!paisSeleccionado.value) return
  const q = ciudadQuery.value.toLowerCase()
  ciudadesSugeridas.value = q.length < 2
    ? []
    : (paisSeleccionado.value.cities || []).filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

function seleccionarCiudad(c) {
  ciudadQuery.value       = c
  form.value.ciudad       = c
  ciudadesSugeridas.value = []
}

function validarCiudad() {
  if (ciudadQuery.value && !form.value.ciudad) {
    ciudadQuery.value       = ''
    ciudadesSugeridas.value = []
  }
}

// ── Teléfono ──────────────────────────────────────────────────
const telefonoDigitos = computed(() =>
  form.value.telefono.replace(/\D/g, '').length
)

const phonePlaceholder = computed(() => {
  const n = phoneDigits.value
  const s = '5'.repeat(n)
  if (n <= 7)  return s.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  if (n === 8) return s.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  if (n === 9) return s.replace(/^(\d{3})(\d{3})(\d{3})/, '$1 $2 $3')
  return s.replace(/^(\d{3})(\d{3})(\d{4})/, '$1 $2 $3')
})

function onPhoneInput(e) {
  const raw    = e.target.value.replace(/\D/g, '')
  const capped = raw.slice(0, phoneDigits.value)
  const n      = phoneDigits.value
  let f = capped
  if (n <= 7)       f = capped.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  else if (n === 8) f = capped.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  else if (n === 9) f = capped.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  else              f = capped.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  form.value.telefono   = f
  errors.value.telefono = ''
}

// ── Helper visual ─────────────────────────────────────────────
function formatDuracion(min) {
  if (!min) return '--'
  return `${Math.floor(min / 60)}h${min % 60 > 0 ? ' ' + min % 60 + 'm' : ''}`
}

// ── Confirmar reserva — solo guarda form y navega a checkout ──
async function handleReservar() {
  errors.value = {}
  const f = form.value

  if (!f.nombre)          errors.value.nombre         = 'Campo requerido'
  if (!f.apellido)        errors.value.apellido        = 'Campo requerido'
  if (!f.pasaporte)       errors.value.pasaporte       = 'Campo requerido'
  if (f.pasaporte && !/^\d+$/.test(f.pasaporte)) errors.value.pasaporte = 'Solo números'
  if (!f.fechaNacimiento) errors.value.fechaNacimiento = 'Campo requerido'
  if (!f.pais)            errors.value.pais            = 'Selecciona un país de la lista'
  if (!f.ciudad)          errors.value.ciudad          = 'Selecciona una ciudad de la lista'
  if (!f.email)           errors.value.email           = 'Campo requerido'
  if (f.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) errors.value.email = 'Correo inválido'
  if (dialCode.value && telefonoDigitos.value !== phoneDigits.value)
    errors.value.telefono = `Se requieren ${phoneDigits.value} dígitos`

  if (Object.keys(errors.value).length) return

  if (tiempoRestante.value === 0) {
    errors.value.general = 'La reserva ha expirado. Realiza una nueva búsqueda.'
    return
  }

  submitting.value = true
  try {
    const telefonoCompleto = dialCode.value
      ? `${dialCode.value} ${f.telefono.replace(/\s/g, '')}`
      : f.telefono

    const { proveedorId, vuelosArr } = buildVuelosPayload()

    // Guardar todo lo necesario para checkout y pasos siguientes
    sessionStorage.setItem('checkout_data', JSON.stringify({
      reservacionId:             reservacionId.value,
      noReservacion:             noReservacion.value,
      detalleVuelo:              detalleVuelo.value,   // boletos, asientos, total_con_ganancia
      item:                      item.value,
      tipoItem:                  tipoItem.value,
      proveedorId,
      vuelos:                    vuelosArr,
      pasajero:                  { ...f, telefono: telefonoCompleto },
      tiempoRestanteAlConfirmar: tiempoRestante.value,
    }))

    if (timerInterval.value) clearInterval(timerInterval.value)
    router.push('/checkout')

  } catch (err) {
    console.error(err)
    errors.value.general = 'Error inesperado. Intenta de nuevo.'
  } finally {
    submitting.value = false
  }
}
</script>