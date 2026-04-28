<template>
  <div class="page">
    <Encabezado />

    <!-- Stack de notificaciones toast (éxito / error) -->
    <div class="mv-toast-stack">
      <div v-for="t in toasts" :key="t.id" :class="['mv-toast', `mv-toast--${t.tipo}`]">
        <svg v-if="t.tipo==='success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        <span>{{ t.msg }}</span>
      </div>
    </div>

    <!-- PANEL LATERAL: detalle completo de una reserva seleccionada -->
    <Transition name="mv-panel">
      <div v-if="panelReserva" class="mv-overlay" @click.self="cerrarPanel">
        <div class="mv-panel" role="dialog" aria-modal="true">

          <!-- Cabecera del panel: tipo, estado y botón de cierre -->
          <div class="mv-panel__head">
            <div class="mv-panel__head-left">
              <div class="mv-panel__tipo-badge" :class="`mv-panel__tipo-badge--${panelReserva._categoria}`">
                <svg v-if="panelReserva._categoria==='vuelo'" viewBox="0 0 24 24" fill="currentColor" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                <svg v-else-if="panelReserva._categoria==='hotel'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                {{ panelReserva._categoria === 'vuelo' ? 'Vuelo' : panelReserva._categoria === 'hotel' ? 'Hospedaje' : 'Paquete turístico' }}
              </div>
              <span :class="['mv-badge', estadoClase(panelReserva.estadoReserva)]">{{ panelReserva.estadoReserva }}</span>
            </div>
            <button class="mv-panel__close" @click="cerrarPanel" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="18" height="18"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <div class="mv-panel__body">


            <!-- DETALLES DE LA RESERVACIÓN -->
              <!-- Hero del panel: código de reserva y total pagado -->
              <div class="mv-panel__hero">
                <div>
                  <p class="mv-panel__hero-lbl">Código de reserva</p>
                  <p class="mv-panel__hero-codigo">{{ panelReserva.noReservacion }}</p>
                </div>
                <div class="mv-panel__hero-right">
                  <p class="mv-panel__hero-lbl">Total pagado</p>
                  <p class="mv-panel__hero-monto">${{ panelReserva.total?.toFixed(2) }}</p>
                </div>
              </div>

              <!-- Spinner mientras se carga el detalle del proveedor -->
              <div v-if="panelLoading" class="mv-panel__center">
                <div class="mv-spinner mv-spinner--lg"></div>
                <p>Cargando detalles del proveedor...</p>
              </div>

              <template v-if="!panelLoading">
              <!-- Información general: fechas, usuario y cancelación -->
              <div class="mv-panel__section">
                <h4 class="mv-panel__stitle">Información general</h4>
                <div class="mv-panel__igrid">
                  <div class="mv-panel__icell">
                    <span class="mv-panel__ilbl">Reservado el</span>
                    <span class="mv-panel__ival">{{ formatFecha(panelReserva.fechaCreacion) }}</span>
                  </div>
                  <div v-if="panelReserva.fechaExpiracion && panelReserva.estadoReserva?.toLowerCase() === 'pendiente'" class="mv-panel__icell">
                    <span class="mv-panel__ilbl">Expira</span>
                    <span class="mv-panel__ival mv-panel__ival--warn">{{ formatFechaHora(panelReserva.fechaExpiracion) }}</span>
                  </div>
                  <div v-if="panelReserva.fechaCancelacion" class="mv-panel__icell">
                    <span class="mv-panel__ilbl">Cancelada el</span>
                    <span class="mv-panel__ival mv-panel__ival--danger">{{ formatFecha(panelReserva.fechaCancelacion) }}</span>
                  </div>
                  <div v-if="panelReserva.motivoCancelacion" class="mv-panel__icell mv-panel__icell--full">
                    <span class="mv-panel__ilbl">Motivo cancelación</span>
                    <span class="mv-panel__ival">{{ panelReserva.motivoCancelacion }}</span>
                  </div>
                  <div v-if="panelReserva.usuarioNombre" class="mv-panel__icell">
                    <span class="mv-panel__ilbl">Usuario</span>
                    <span class="mv-panel__ival">{{ panelReserva.usuarioNombre }}</span>
                  </div>
                </div>
              </div>

              <!-- BOLETOS: datos reales del proveedor Broom AirLine (data_proveedor) -->
              <template v-if="(panelReserva._categoria==='vuelo' || panelReserva._categoria==='paquete') && (panelReserva.boletos?.length ?? 0) > 0">
                <div class="mv-panel__section">
                  <h4 class="mv-panel__stitle">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
                    Boletos ({{ panelReserva.boletos.length }})
                  </h4>
                  <div v-for="boleto in panelReserva.boletos" :key="boleto.noBoleto" class="mv-boleto">
                    <div class="mv-boleto__top">
                      <div class="mv-boleto__info">
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <span class="mv-boleto__num">{{ boleto.numeroVuelo }}</span>
                        <span class="mv-boleto__avion">{{ boleto.avionMarca }} {{ boleto.avionModelo }}</span>
                      </div>
                      <span :class="['mv-badge', 'mv-badge--sm', estadoClase(boleto.estadoBoleto)]">{{ boleto.estadoBoleto }}</span>
                    </div>
                    <div class="mv-boleto__ruta">
                      <div class="mv-boleto__punto">
                        <span class="mv-boleto__iata">{{ boleto.origenCodigo }}</span>
                        <span class="mv-boleto__ciudad">{{ boleto.origenCiudad }}</span>
                        <span class="mv-boleto__hora">{{ formatHora(boleto.horaSalida) }}</span>
                      </div>
                      <div class="mv-boleto__linea">
                        <div class="mv-boleto__linea-track"></div>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" class="mv-boleto__avion-svg" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <span class="mv-boleto__dur">{{ formatDuracion(boleto.duracionMinutos) }}</span>
                      </div>
                      <div class="mv-boleto__punto mv-boleto__punto--r">
                        <span class="mv-boleto__iata">{{ boleto.destinoCodigo }}</span>
                        <span class="mv-boleto__ciudad">{{ boleto.destinoCiudad }}</span>
                        <span class="mv-boleto__hora">{{ formatHora(boleto.horaLlegada) }}</span>
                      </div>
                    </div>
                    <div class="mv-boleto__grid">
                      <div class="mv-boleto__cell"><span class="mv-boleto__clbl">Asiento</span><span class="mv-boleto__cval">{{ boleto.noAsiento }}</span></div>
                      <div class="mv-boleto__cell"><span class="mv-boleto__clbl">Clase</span><span class="mv-boleto__cval">{{ boleto.clase }}</span></div>
                      <div class="mv-boleto__cell"><span class="mv-boleto__clbl">Fecha</span><span class="mv-boleto__cval">{{ formatFecha(boleto.fechaVuelo) }}</span></div>
                    </div>
                    <div v-if="boleto.pasajero" class="mv-boleto__pasajero">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="13" height="13"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      <div>
                        <span class="mv-boleto__pas-name">{{ boleto.pasajero.nombre }} {{ boleto.pasajero.apellido }}</span>
                        <span class="mv-boleto__pas-info">{{ boleto.pasajero.pasaporte }} · {{ boleto.pasajero.ciudad }}, {{ boleto.pasajero.pais }}</span>
                      </div>
                    </div>
                  </div>

                </div>
              </template>

              <!-- HABITACIONES: detalles sin precios individuales -->
              <template v-if="(panelReserva._categoria==='hotel' || panelReserva._categoria==='paquete') && (panelReserva.habitaciones?.length ?? 0) > 0">
                <div class="mv-panel__section">
                  <h4 class="mv-panel__stitle">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    {{ panelReserva.nombreHotel }} · Habitaciones ({{ panelReserva.habitaciones.length }})
                  </h4>
                  <div v-for="h in panelReserva.habitaciones" :key="h.detalleId" class="mv-hab">
                    <div class="mv-hab__top">
                      <div>
                        <strong class="mv-hab__tipo">{{ h.tipoHabitacion }}</strong>
                        <span class="mv-hab__cama">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                          {{ h.tipoCama }}
                        </span>
                      </div>
                    </div>
                    <p class="mv-hab__desc">{{ h.descripcionHabitacion }}</p>
                    <div class="mv-hab__meta">
                      <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg> {{ formatFecha(h.fechaCheckIn) }} → {{ formatFecha(h.fechaCheckOut) }}</span>
                      <span>{{ calcNoches(h.fechaCheckIn, h.fechaCheckOut) }} noches</span>
                      <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> {{ h.cantidadPersonas }} huésped{{ h.cantidadPersonas !== 1 ? 'es' : '' }}</span>
                    </div>
                  </div>
                </div>
              </template>


              <!-- Total de la reservación -->
              <div class="mv-panel__section">
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                  Total de la Reservación
                </h4>
                <div style="font-size: 1.3rem; font-weight: bold; color: #2a2520; text-align: center; padding: 1rem;">
                  ${{ panelReserva.total?.toFixed(2) }}
                </div>
              </div>

              <!-- COMENTARIOS DEL PROVEEDOR (solo lectura, reservas completadas) -->
              <div
                v-if="panelReserva.estadoReserva?.toLowerCase()==='completada' && getComentariosRaizMR().length > 0"
                class="mv-panel__section"
              >
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  Opiniones · {{ getResenasRaizMR().length > 0 ? getPromedioMR().toFixed(1) + ' ★' : '' }}
                </h4>
                <div class="mv-comentarios-lista">
                  <ComentarioNodo
                    v-for="c in getComentariosRaizMR()"
                    :key="c.id"
                    :comentario="c"
                    :getHijos="getHijosMR"
                    :estadoNodos="estadoNodosMR"
                    :haySession="false"
                    :formatFecha="formatFecha"
                    @votar="() => {}"
                    @toggleForm="() => {}"
                    @toggleExpandido="toggleExpandidoMR"
                    @enviarRespuesta="() => {}"
                    @textoChange="() => {}"
                  />
                </div>
              </div>

              <!-- CALIFICAR VUELO: solo si categoría vuelo/paquete + completada + rutaId disponible -->
              <div
                v-if="(panelReserva._categoria==='vuelo' || panelReserva._categoria==='paquete')
                       && panelReserva.estadoReserva?.toLowerCase()==='completada'
                       && panelReserva.boletos?.[0]?.rutaId"
                class="mv-panel__section"
              >
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                  Calificar vuelo {{ panelReserva.boletos[0].origenCodigo }} → {{ panelReserva.boletos[0].destinoCodigo }}
                </h4>

                <!-- Spinner mientras se verifican calificaciones existentes -->
                <div v-if="comentariosLoading" class="mv-panel__center" style="padding:1.2rem">
                  <div class="mv-spinner"></div>
                  <p style="font-size:0.8rem;color:#7a7067">Verificando calificaciones...</p>
                </div>

                <!-- El usuario ya calificó: muestra su calificación existente -->
                <div v-else-if="yaComentaRuta(panelReserva.boletos[0].rutaId) || calExito" class="mv-ya-califico">
                  <div class="mv-ya-califico__stars">
                    <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
                      :fill="n<=(obtenerComentarioRuta(panelReserva.boletos[0].rutaId)?.cantidadEstrellas ?? calEstrellas)?'#FFCC00':'none'"
                      :stroke="n<=(obtenerComentarioRuta(panelReserva.boletos[0].rutaId)?.cantidadEstrellas ?? calEstrellas)?'#FFCC00':'#ccc'"
                      stroke-width="2" width="18" height="18">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                  </div>
                  <p v-if="obtenerComentarioRuta(panelReserva.boletos[0].rutaId)" class="mv-ya-califico__texto">
                    {{ obtenerComentarioRuta(panelReserva.boletos[0].rutaId)?.contenido }}
                  </p>
                  <span class="mv-ya-califico__badge">✓ Ya calificaste este vuelo</span>
                </div>

                <!-- Formulario para enviar nueva calificación de vuelo -->
                <div v-else class="mv-calificar">
                  <div class="mv-calificar__stars">
                    <button v-for="n in 5" :key="n" type="button" class="mv-calificar__star"
                      @mouseenter="calHover=n" @mouseleave="calHover=0" @click="calEstrellas=n">
                      <svg viewBox="0 0 24 24"
                        :fill="n<=(calHover||calEstrellas)?'#FFCC00':'none'"
                        :stroke="n<=(calHover||calEstrellas)?'#FFCC00':'#ccc'"
                        stroke-width="2" width="26" height="26">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                    </button>
                    <span class="mv-calificar__lbl">{{ calEstrellas > 0 ? `${calEstrellas}/5` : 'Selecciona' }}</span>
                  </div>
                  <textarea class="mv-calificar__textarea" v-model="calContenido"
                    placeholder="Cuéntanos tu experiencia en este vuelo..." rows="3"></textarea>
                  <p v-if="calError" class="mv-form-error">{{ calError }}</p>
                  <button class="mv-btn mv-btn--primary"
                    @click="enviarCalificacion(panelReserva.boletos[0].rutaId)"
                    :disabled="calLoading" type="button">
                    <span v-if="calLoading" class="mv-btn__spin mv-btn__spin--light"></span>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                    {{ calLoading ? 'Enviando...' : 'Enviar calificación' }}
                  </button>
                </div>
              </div>

              <!-- RESEÑA HOTEL: solo si categoría hotel/paquete + completada -->
              <div
                v-if="(panelReserva._categoria==='hotel' || panelReserva._categoria==='paquete')
                       && panelReserva.estadoReserva?.toLowerCase()==='completada'"
                class="mv-panel__section"
              >
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  Reseña · {{ panelReserva.nombreHotel }}
                </h4>

                <!-- Spinner mientras se verifican reseñas existentes -->
                <div v-if="comentariosLoading" class="mv-panel__center" style="padding:1.2rem">
                  <div class="mv-spinner"></div>
                  <p style="font-size:0.8rem;color:#7a7067">Verificando reseñas...</p>
                </div>

                <!-- El usuario ya reseñó este hotel: muestra su reseña -->
                <div v-else-if="resenaOk || yaResenaHotel(panelReserva.hotelId)" class="mv-ya-califico">
                  <div v-if="obtenerResenaHotel(panelReserva.hotelId)" class="mv-ya-califico__stars">
                    <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
                      :fill="n<=(obtenerResenaHotel(panelReserva.hotelId)?.resena ?? 5)?'#FFCC00':'none'"
                      :stroke="n<=(obtenerResenaHotel(panelReserva.hotelId)?.resena ?? 5)?'#FFCC00':'#ccc'"
                      stroke-width="2" width="18" height="18">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                  </div>
                  <p v-if="obtenerResenaHotel(panelReserva.hotelId)" class="mv-ya-califico__texto">
                    {{ obtenerResenaHotel(panelReserva.hotelId)?.contenido }}
                  </p>
                  <span class="mv-ya-califico__badge">✓ Ya dejaste una reseña para este hospedaje</span>
                </div>

                <!-- Formulario para enviar nueva reseña del hotel -->
                <div v-else class="mv-calificar">
                  <div class="mv-calificar__stars">
                    <button v-for="n in 5" :key="n" type="button" class="mv-calificar__star"
                      @mouseenter="resHover=n" @mouseleave="resHover=0" @click="resEstrellas=n">
                      <svg viewBox="0 0 24 24"
                        :fill="n<=(resHover||resEstrellas)?'#FFCC00':'none'"
                        :stroke="n<=(resHover||resEstrellas)?'#FFCC00':'#ccc'"
                        stroke-width="2" width="26" height="26">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                    </button>
                    <span class="mv-calificar__lbl">{{ resEstrellas > 0 ? `${resEstrellas}/5` : 'Selecciona' }}</span>
                  </div>
                  <textarea class="mv-calificar__textarea" v-model="resContenido"
                    placeholder="¿Cómo fue tu estadía?" rows="3"></textarea>
                  <p v-if="resError" class="mv-form-error">{{ resError }}</p>
                  <button class="mv-btn mv-btn--primary" @click="enviarResenaHotel"
                    :disabled="resLoading" type="button">
                    <span v-if="resLoading" class="mv-btn__spin mv-btn__spin--light"></span>
                    {{ resLoading ? 'Enviando...' : 'Enviar reseña' }}
                  </button>
                </div>
              </div>

              <!-- EDITAR FECHAS HOTEL: solo para reservas hotel/paquete pendientes o confirmadas -->
              <div v-if="(panelReserva._categoria==='hotel' || panelReserva._categoria==='paquete') && ['confirmada','pendiente'].includes(panelReserva.estadoReserva?.toLowerCase())" class="mv-panel__section">
                <button v-if="!panelEditandoReservacion" class="mv-btn mv-btn--outline" @click="openEditarReservacion" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><polyline points="3 12 3 20 12 20 20 12"/><path d="M16 5l-3.086-3.086a2 2 0 0 0-2.828 0L2.914 10.914a2 2 0 0 0 0 2.828l3.086 3.086a2 2 0 0 0 2.828 0l10.172-10.172a2 2 0 0 0 0-2.828z"/></svg>
                  Cambiar fechas de hospedaje
                </button>
                <div v-else class="mv-editar">
                  <div class="mv-editar__head">
                    <h4>Cambiar fechas de hospedaje</h4>
                    <button class="mv-editar__close" @click="closeEditarReservacion" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  </div>
                  <div v-if="editOk" class="mv-editar__ok">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                    <p>Cambio exitoso. Los datos se han actualizado.</p>
                  </div>
                  <div v-else class="mv-editar__form">
                    <div class="mv-editar__row">
                      <div class="mv-editar__field">
                        <label class="mv-editar__label">Check-in</label>
                        <input v-model="editForm.fechaCheckIn" type="date" class="mv-editar__input" />
                      </div>
                      <div class="mv-editar__field">
                        <label class="mv-editar__label">Check-out</label>
                        <input v-model="editForm.fechaCheckOut" type="date" class="mv-editar__input" />
                      </div>
                    </div>
                    <p v-if="editError" class="mv-form-error">{{ editError }}</p>
                    <div class="mv-editar__actions">
                      <button class="mv-btn mv-btn--ghost" @click="closeEditarReservacion" :disabled="editLoading" type="button">Cancelar</button>
                      <button class="mv-btn mv-btn--primary" @click="confirmarEdicion" :disabled="editLoading" type="button">
                        <span v-if="editLoading" class="mv-btn__spin mv-btn__spin--light"></span>
                        {{ editLoading ? 'Guardando...' : 'Guardar cambios' }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- CANCELAR: disponible solo para reservas pendientes o confirmadas -->
              <div v-if="['confirmada','pendiente'].includes(panelReserva.estadoReserva?.toLowerCase())" class="mv-panel__section">
                <button v-if="!cancelAbierto" class="mv-btn mv-btn--danger-ghost" @click="cancelAbierto=true" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                  Solicitar cancelación
                </button>
                <div v-else class="mv-cancelar">
                  <div class="mv-cancelar__head">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="2" width="20" height="20"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                    <div>
                      <p class="mv-cancelar__title">Solicitar cancelación</p>
                      <p class="mv-cancelar__sub">Esta acción no se puede deshacer. El proveedor procesará la cancelación.</p>
                    </div>
                  </div>
                  <textarea class="mv-cancelar__textarea" v-model="cancelMotivo"
                    placeholder="Motivo de cancelación (requerido)..." rows="2"></textarea>
                  <label style="display:flex;align-items:flex-start;gap:0.6rem;cursor:pointer;font-size:0.82rem;color:#4a4035;">
                    <input type="checkbox" v-model="cancelTerminos" style="margin-top:2px;accent-color:#D40511;" />
                    <span>Entiendo que esta cancelación es definitiva y será procesada por el proveedor.</span>
                  </label>
                  <p v-if="cancelError" class="mv-form-error">{{ cancelError }}</p>
                  <div class="mv-cancelar__actions">
                    <button class="mv-btn mv-btn--ghost" @click="cancelAbierto=false; cancelTerminos=false" :disabled="cancelLoading" type="button">Volver</button>
                    <button class="mv-btn mv-btn--danger" @click="confirmarCancelar" :disabled="cancelLoading" type="button">
                      <span v-if="cancelLoading" class="mv-btn__spin mv-btn__spin--light"></span>
                      {{ cancelLoading ? 'Procesando...' : 'Confirmar cancelación' }}
                    </button>
                  </div>
                </div>
              </div>

              </template>

              <div v-if="panelError" class="mv-panel__error">{{ panelError }}</div>

            <!-- Footer del panel: cerrar, descargar PDF y enviar al correo -->
            <div class="mv-panel__footer">
              <button class="mv-btn mv-btn--ghost" @click="cerrarPanel" type="button">Cerrar</button>
              <template v-if="['confirmada','completada'].includes(panelReserva.estadoReserva?.toLowerCase())">
                <button class="mv-btn mv-btn--outline" @click="descargarPDF(panelReserva.id)" :disabled="pdfLoading" type="button">
                  <span v-if="pdfLoading" class="mv-btn__spin mv-btn__spin--dark"></span>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                  {{ pdfLoading ? 'Descargando...' : 'Descargar PDF' }}
                </button>
                <button class="mv-btn mv-btn--secondary" @click="enviarCorreo(panelReserva.id)" :disabled="correoLoading" type="button">
                  <span v-if="correoLoading" class="mv-btn__spin mv-btn__spin--dark"></span>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                  {{ correoLoading ? 'Enviando...' : 'Enviar al correo' }}
                </button>
              </template>
            </div>

          </div>
        </div>
      </div>
    </Transition>


    <!-- PÁGINA PRINCIPAL: listado de reservaciones del usuario -->
    <div class="mv-page">
      <div class="mv-container">

        <!-- Cabecera con título y buscador por código -->
        <div class="mv-header">
          <div>
            <h1 class="mv-title">Mis Reservaciones</h1>
            <p class="mv-subtitle">Historial completo de viajes y hospedajes</p>
          </div>
          <div class="mv-buscar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input v-model="busqueda" placeholder="Buscar por código..." />
          </div>
        </div>

        <!-- RESUMEN: totales y conteo por estado y categoría -->
        <div v-if="resumen" class="mv-resumen">
          <div class="mv-resumen__hero">
            <div class="mv-resumen__hero-left">
              <p class="mv-resumen__hero-lbl">Total invertido en viajes</p>
              <p class="mv-resumen__hero-monto">${{ resumen.totalGastado?.toFixed(2) ?? '0.00' }}</p>
              <p class="mv-resumen__hero-sub">{{ resumen.totalReservaciones ?? 0 }} reservaciones en total</p>
            </div>
            <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,204,0,0.25)" stroke-width="0.8" width="100" height="100" class="mv-resumen__hero-deco"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
          </div>

          <!-- Contadores por estado de reservación -->
          <div class="mv-resumen__estados">
            <div class="mv-estado mv-estado--confirmada">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="17" height="17"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              <span class="mv-estado__num">{{ resumen.confirmadas ?? 0 }}</span>
              <span class="mv-estado__lbl">Confirmadas</span>
            </div>
            <div class="mv-estado mv-estado--pendiente">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="17" height="17"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              <span class="mv-estado__num">{{ resumen.pendientes ?? 0 }}</span>
              <span class="mv-estado__lbl">Pendientes</span>
            </div>
            <div class="mv-estado mv-estado--encurso">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="17" height="17"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/><path d="M12 2a10 10 0 0 1 7.07 17.07"/></svg>
              <span class="mv-estado__num">{{ resumen.enCurso ?? 0 }}</span>
              <span class="mv-estado__lbl">En Curso</span>
            </div>
            <div class="mv-estado mv-estado--completada">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="17" height="17"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>
              <span class="mv-estado__num">{{ resumen.completadas ?? 0 }}</span>
              <span class="mv-estado__lbl">Completadas</span>
            </div>
            <div class="mv-estado mv-estado--cancelada">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="17" height="17"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
              <span class="mv-estado__num">{{ resumen.canceladas ?? 0 }}</span>
              <span class="mv-estado__lbl">Canceladas</span>
            </div>
            <div class="mv-estado mv-estado--expirada">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="17" height="17"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/></svg>
              <span class="mv-estado__num">{{ resumen.expiradas ?? 0 }}</span>
              <span class="mv-estado__lbl">Expiradas</span>
            </div>
          </div>

          <!-- Contadores por categoría: vuelos, hoteles y paquetes -->
          <div class="mv-resumen__cats">
            <div class="mv-cat mv-cat--vuelo">
              <div class="mv-cat__icon-wrap"><svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg></div>
              <div class="mv-cat__body"><span class="mv-cat__num">{{ resumen.vuelos ?? 0 }}</span><span class="mv-cat__label">Vuelos</span><span class="mv-cat__sub">Boletos y rutas aéreas</span></div>
            </div>
            <div class="mv-cat mv-cat--hotel">
              <div class="mv-cat__icon-wrap"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></div>
              <div class="mv-cat__body"><span class="mv-cat__num">{{ resumen.hoteles ?? 0 }}</span><span class="mv-cat__label">Hospedajes</span><span class="mv-cat__sub">Doble, Suite y más</span></div>
            </div>
            <div class="mv-cat mv-cat--paquete">
              <div class="mv-cat__icon-wrap"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg></div>
              <div class="mv-cat__body"><span class="mv-cat__num">{{ resumen.paquetes ?? 0 }}</span><span class="mv-cat__label">Paquetes</span><span class="mv-cat__sub">Vuelo + hospedaje</span></div>
            </div>
          </div>
        </div>

        <!-- Filtros de estado (todas, pendientes, confirmadas, etc.) -->
        <div class="mv-controles">
          <div class="mv-filtros">
            <button v-for="f in filtros" :key="f.key"
              :class="['mv-filtro', { 'mv-filtro--active': filtroActivo===f.key }]"
              @click="filtroActivo=f.key" type="button">
              {{ f.label }}
              <span v-if="resumen && f.key !== 'todas'" class="mv-filtro__n">{{ resumen[f.campo] ?? '' }}</span>
            </button>
          </div>
        </div>

        <!-- Tabs por categoría (todas, vuelos, hoteles, paquetes) -->
        <div class="mv-tabs">
          <button v-for="t in tabs" :key="t.key"
            :class="['mv-tab', { 'mv-tab--active': tabActivo===t.key }]"
            @click="tabActivo=t.key" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15" v-html="t.icon"></svg>
            {{ t.label }}
            <span class="mv-tab__n">{{ countPorTab(t.key) }}</span>
          </button>
        </div>

        <!-- Estado de carga inicial -->
        <div v-if="loading" class="mv-empty">
          <div class="mv-spinner mv-spinner--xl"></div>
          <p>Cargando tus reservaciones...</p>
        </div>

        <!-- Estado de error con opción de reintentar -->
        <div v-else-if="error" class="mv-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="48" height="48"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <p>{{ error }}</p>
          <button class="mv-btn mv-btn--primary" @click="cargarTodo" type="button">Reintentar</button>
        </div>

        <!-- Estado vacío cuando no hay resultados con el filtro activo -->
        <div v-else-if="reservasFiltradas.length===0" class="mv-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="56" height="56"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
          <p class="mv-empty__title">Sin reservaciones</p>
          <p class="mv-empty__sub">{{ filtroActivo !== 'todas' || tabActivo !== 'todas' ? 'Prueba cambiando el filtro' : 'Haz tu primera reserva' }}</p>
          <button v-if="filtroActivo==='todas' && tabActivo==='todas'" class="mv-btn mv-btn--primary" @click="$router.push('/principal')" type="button">Buscar viajes</button>
          <button v-else class="mv-btn mv-btn--ghost" @click="filtroActivo='todas'; tabActivo='todas'" type="button">Ver todas</button>
        </div>

        <!-- LISTA DE CARDS: una tarjeta por cada reservación filtrada -->
        <div v-else class="mv-lista">
          <article v-for="r in reservasFiltradas" :key="r.id"
            class="mv-card" :class="`mv-card--${r._categoria}`"
            @click="abrirPanel(r)" tabindex="0" @keydown.enter="abrirPanel(r)">

            <!-- Franja de categoría con icono y etiqueta -->
            <div class="mv-card__franja">
              <svg v-if="r._categoria==='vuelo'" viewBox="0 0 24 24" fill="currentColor" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
              <svg v-else-if="r._categoria==='hotel'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              {{ r._categoria === 'vuelo' ? 'Vuelo' : r._categoria === 'hotel' ? 'Hospedaje' : 'Paquete turístico' }}
            </div>

            <div class="mv-card__cuerpo">
              <div class="mv-card__top">
                <span class="mv-card__codigo">{{ r.noReservacion }}</span>
                <span :class="['mv-badge', estadoClase(r.estadoReserva)]">{{ r.estadoReserva }}</span>
              </div>

              <!-- VUELO / PAQUETE: ruta con datos del proveedor si están disponibles -->
              <template v-if="(r._categoria==='vuelo' || r._categoria==='paquete') && r.boletos?.length">
                <!-- Ruta completa (disponible solo desde el detalle del proveedor) -->
                <template v-if="r.boletos[0]?.origenCodigo">
                  <div class="mv-card__ruta">
                    <div class="mv-card__punto">
                      <span class="mv-card__iata">{{ r.boletos[0].origenCodigo }}</span>
                      <span class="mv-card__ciudad">{{ r.boletos[0].origenCiudad }}</span>
                    </div>
                    <div class="mv-card__flecha">
                      <div class="mv-card__flecha-line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" class="mv-card__avion" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    </div>
                    <div class="mv-card__punto mv-card__punto--r">
                      <span class="mv-card__iata">{{ r.boletos[0].destinoCodigo }}</span>
                      <span class="mv-card__ciudad">{{ r.boletos[0].destinoCiudad }}</span>
                    </div>
                  </div>
                  <div class="mv-card__meta">
                    <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>{{ formatFecha(r.boletos[0].fechaVuelo) }}</span>
                    <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>{{ formatHora(r.boletos[0].horaSalida) }} → {{ formatHora(r.boletos[0].horaLlegada) }}</span>
                    <span>{{ r.boletos.length }} boleto{{ r.boletos.length!==1?'s':'' }}</span>
                  </div>
                </template>

                <!-- Preview compacto con datos snapshot (parametros_json) cuando no hay data_proveedor -->
                <template v-else>
                  <div class="mv-card__meta" style="padding:0.3rem 0">
                    <span>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      {{ r.boletos.length }} boleto{{ r.boletos.length!==1?'s':'' }}
                    </span>
                    <span v-if="r.boletos[0]?.numeroVuelo">Vuelo #{{ r.boletos[0].numeroVuelo }}</span>
                    <span v-if="r.boletos[0]?.clase">{{ r.boletos[0].clase }}</span>
                  </div>
                  <p style="font-size:0.73rem;color:#9a9089;margin:0">Ver detalle para info completa de ruta</p>
                </template>
              </template>

              <!-- HOTEL / PAQUETE: habitaciones con datos completos del proveedor -->
              <template v-if="(r._categoria==='hotel' || r._categoria==='paquete') && r.habitaciones?.[0]">
                <div class="mv-card__hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  <div>
                    <span class="mv-card__hotel-nombre">{{ r.nombreHotel }}</span>
                    <span class="mv-card__hotel-hab">{{ r.habitaciones.map(h=>h.tipoHabitacion).join(' · ') }}</span>
                  </div>
                </div>
                <div class="mv-card__meta">
                  <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>{{ formatFecha(r.habitaciones[0].fechaCheckIn) }} → {{ formatFecha(r.habitaciones[0].fechaCheckOut) }}</span>
                  <span>{{ calcNoches(r.habitaciones[0].fechaCheckIn, r.habitaciones[0].fechaCheckOut) }} noches</span>
                  <span>{{ r.habitaciones.reduce((s,h)=>s+(h.cantidadPersonas??0),0) }} huéspedes</span>
                </div>
              </template>

              <!-- Preview hotel cuando solo está disponible el snapshot -->
              <template v-else-if="(r._categoria==='hotel' || r._categoria==='paquete') && r._habitacionesPreview?.[0]">
                <div class="mv-card__hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  <div>
                    <span class="mv-card__hotel-nombre">Hospedaje reservado</span>
                    <span class="mv-card__hotel-hab">{{ r._habitacionesPreview[0].noReservacion }}</span>
                  </div>
                </div>
              </template>

              <!-- Fecha de creación siempre visible en la parte inferior de la card -->
              <div class="mv-card__meta" style="margin-top:auto">
                <span>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Reservado {{ formatFecha(r.fechaCreacion) }}
                </span>
              </div>

              <div class="mv-card__bottom">
                <span class="mv-card__total">${{ r.total?.toFixed(2) }}</span>
                <span class="mv-card__ver">
                  Ver detalle
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                </span>
              </div>
            </div>
          </article>
        </div>

      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file MisReservaciones.vue
 * @description Vista del historial de reservaciones del usuario autenticado.
 * Carga un listado paginado desde el backend (snapshot), y al abrir una reserva
 * consulta los datos reales al proveedor (Broom AirLine / Miku Inn).
 * Permite filtrar por estado y categoría, cancelar reservas, calificar vuelos,
 * dejar reseñas de hoteles, descargar PDF y enviar confirmación por correo.
 */

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/misreservaciones.css'
import ComentarioNodo from '../components/Comentarionodo.vue'

/** Instancia del router (no se usa actualmente pero está disponible para navegación). */
const router = useRouter()

/** URL base del backend Go/Gin. @type {string} */
const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/**
 * Extrae el usuarioId del JWT guardado en localStorage o sessionStorage.
 * Soporta claims: id | usuarioId | userId | sub (como número).
 * Retorna null si el token no existe o no se puede decodificar.
 *
 * @returns {number|null}
 */
function getUsuarioIdActual() {
  // La autenticacion viaja por cookie HttpOnly — no hay token en storage
  return null
}

/**
 * Construye los headers de autenticación para las peticiones al backend.
 * Incluye Authorization: Bearer <token> si existe alguno en storage.
 *
 * @returns {Object} Headers listo para usar en fetch.
 */
function authHeaders() {
  // La autenticacion viaja por cookie HttpOnly (credentials: 'include')
  return { 'Content-Type': 'application/json' }
}

/**
 * Wrapper de fetch que incluye credenciales y headers de autenticación.
 * Lanza un Error con el mensaje del backend si el status no es 2xx.
 *
 * @async
 * @param {string} url - Endpoint a consumir.
 * @param {RequestInit} [opts={}] - Opciones adicionales para fetch.
 * @returns {Promise<any>} JSON de la respuesta.
 */
async function apiFetch(url, opts = {}) {
  const res = await fetch(url, { headers: authHeaders(), credentials: 'include', ...opts })
  if (!res.ok) {
    let msg = `Error ${res.status}`
    try {
      const json = await res.json()
      msg = json.error || json.mensaje || msg
    } catch {
      msg = await res.text().catch(() => msg)
    }
    throw new Error(msg || `Error ${res.status}`)
  }
  return res.json()
}

/**
 * Mapa de ID de estado a etiqueta legible.
 * 1=Pendiente  2=Confirmada  3=Cancelada  4=Expirada  5=Completada  6=En Curso
 * @type {Object<number, string>}
 */
const ESTADO_ID_MAP = { 1:'Pendiente', 2:'Confirmada', 3:'Cancelada', 4:'Expirada', 5:'Completada', 6:'En Curso', 7:'Retenida' }

/**
 * Mapas de ID de estado_detalle a etiqueta legible, diferenciados por tipo de detalle.
 * Cada proveedor maneja un orden distinto de estados:
 *   - Vuelo (tipo_detalle_id=1): 1=Pendiente, 2=Confirmada, 3=Cancelada, 4=Expirada, 5=Completada
 *   - Hotel (tipo_detalle_id=2): 1=Pendiente, 2=Confirmada, 3=Completada, 4=Cancelada, 5=Expirada
 */
const ESTADO_DETALLE_MAP = {
  1: { 1:'Pendiente', 2:'Confirmada', 3:'Cancelada',  4:'Expirada',  5:'Completada' }, // Vuelo
  2: { 1:'Pendiente', 2:'Confirmada', 3:'Completada', 4:'Cancelada', 5:'Expirada'   }, // Hotel
}

/**
 * Convierte el ID numérico de estado en su etiqueta de texto.
 *
 * @param {number} id - ID del estado.
 * @returns {string}
 */
function estadoLabel(id) { return ESTADO_ID_MAP[id] ?? 'Pendiente' }

/**
 * Convierte el ID numérico de estado_detalle a etiqueta legible según el tipo.
 * Los IDs tienen significados diferentes entre vuelos (1) y hoteles (2).
 *
 * REGLA ESPECIAL: si la reserva padre está cancelada o expirada, todos sus
 * detalles se muestran con ese mismo estado, independientemente del ID que
 * el backend haya guardado. Esto cubre casos donde el backend no actualiza
 * correctamente el estado de los detalles al cancelar/expirar la reserva.
 *
 * @param {number} id - ID del estado del detalle.
 * @param {number} tipoDetalleId - 1 para vuelo, 2 para hotel.
 * @returns {string}
 */
function estadoDetalleLabel(id, tipoDetalleId) {
  // Si la reserva padre está cancelada → todos los detalles son "Cancelada"
  const estadoReserva = panelReserva.value?.estadoReserva?.toLowerCase()
  if (estadoReserva === 'cancelada') return 'Cancelada'
  if (estadoReserva === 'expirada')  return 'Expirada'

  // Si la reserva padre está retenida → mostrar el estado REAL del detalle
  // Los detalles cancelados por proveedor tienen estado_detalle_id = 3 (Cancelada)
  // según el backend. El hotel "Completada" era porque su ID 3 en el mapa de hotel
  // significa Completada (no Cancelada como en vuelo).
  if (estadoReserva === 'retenida') {
    // En hotel (tipo 2), el estado 3 es Completada según tu mapa,
    // pero cuando viene de una cancelación por proveedor, realmente está Cancelada.
    // Fuerza "Cancelada" si la reserva padre está retenida Y el detalle es hotel con ID 3.
    if (tipoDetalleId === 2 && id === 3) return 'Cancelada'
    if (tipoDetalleId === 2 && id === 4) return 'Cancelada'
    if (tipoDetalleId === 1 && id === 3) return 'Cancelada'
  }

  return ESTADO_DETALLE_MAP[tipoDetalleId]?.[id] ?? 'Desconocido'
}

/**
 * Convierte el tipo de reserva numérico en su categoría de texto.
 * 1 = vuelo, 2 = hotel, cualquier otro = paquete.
 *
 * @param {number} t - Tipo de reserva.
 * @returns {'vuelo'|'hotel'|'paquete'}
 */
function tipoLabel(t) { return t === 1 ? 'vuelo' : t === 2 ? 'hotel' : 'paquete' }

/**
 * Normaliza una reserva del listado (GET /api/reservaciones/mias).
 * Solo contiene datos del snapshot (parametros_json), sin data_proveedor real.
 * Los boletos carecen de rutaId, origenCodigo, etc. hasta abrir el detalle.
 *
 * @param {Object} r - Objeto de reserva crudo del endpoint de listado.
 * @returns {Object} Reserva normalizada con _preview: true.
 */
function fromLista(r) {
  const boletosSnapshot    = []
  const habitacionesPreview = []

  for (const det of r.detalles ?? []) {
    if (det.tipo_detalle_id === 1 && det.parametros_json?.boletos) {
      for (const b of det.parametros_json.boletos) {
        boletosSnapshot.push({
          boletoId: b.boletoId, noBoleto: b.noBoleto,
          numeroVuelo: b.numeroVuelo, clase: b.clase,
          noAsiento: b.noAsiento, precio: b.precio,
          origenCodigo: null, origenCiudad: null, origenNombre: null,
          destinoCodigo: null, destinoCiudad: null, destinoNombre: null,
          horaSalida: null, horaLlegada: null, duracionMinutos: null,
          fechaVuelo: null, estadoBoleto: null, pasajero: null,
          rutaId: null, vueloId: null, avionMarca: null, avionModelo: null
        })
      }
    }
    if (det.tipo_detalle_id === 2 && det.parametros_json) {
      habitacionesPreview.push({
        noReservacion: det.parametros_json.noReservacion,
        total: det.parametros_json.total,
        estado: det.parametros_json.estado
      })
    }
  }

  return {
    id: r.id, reservacionId: r.id,
    noReservacion: r.no_reservacion,
    estadoReserva: estadoLabel(r.estado_id),
    total: r.total, fechaCreacion: r.fecha_creacion, fechaExpiracion: r.fecha_expiracion,
    _categoria: tipoLabel(r.tipo_reserva),
    _preview: true,
    boletos: boletosSnapshot,
    _habitacionesPreview: habitacionesPreview,
    habitaciones: [], nombreHotel: null, hotelId: null,
    usuarioNombre: null, usuarioEmail: null,
    fechaCancelacion: null, motivoCancelacion: null
  }
}

/**
 * Normaliza una reserva del detalle (GET /api/reservaciones/mias/:id).
 * Incluye los datos reales del proveedor: boletos con rutaId, habitaciones, etc.
 * Extrae también proveedorId para construir las URLs de comentarios.
 *
 * @param {Object} r - Objeto de reserva crudo del endpoint de detalle.
 * @returns {Object} Reserva normalizada con _preview: false.
 */
function fromDetalle(r) {
  const boletos      = []
  const habitaciones = []
  const detallesRaw  = r.detalles ?? []
  let nombreHotel = null, hotelId = null
  let usuarioNombre = null, usuarioEmail = null
  let fechaCancelacion = null, motivoCancelacion = null
  let estadoReserva    = estadoLabel(r.estado_id)
  let proveedorIdVuelo = null
  let proveedorIdHotel = null

  for (const det of r.detalles ?? []) {
    if (det.tipo_detalle_id === 1 && det.data_proveedor) {
      const dp = det.data_proveedor
      boletos.push(...(dp.boletos ?? []))
      if (!proveedorIdVuelo)  proveedorIdVuelo = det.proveedor_id ?? det.proveedorId ?? null
      if (!usuarioNombre)     usuarioNombre    = dp.usuarioNombre
      if (!usuarioEmail)      usuarioEmail     = dp.usuarioEmail
      if (!fechaCancelacion)  fechaCancelacion = dp.fechaCancelacion
      if (!motivoCancelacion) motivoCancelacion = dp.motivoCancelacion
      if (dp.estadoReserva)   estadoReserva    = dp.estadoReserva
    }
    if (det.tipo_detalle_id === 2 && det.data_proveedor) {
      const rooms = Array.isArray(det.data_proveedor) ? det.data_proveedor : [det.data_proveedor]
      habitaciones.push(...rooms)
      if (!proveedorIdHotel)  proveedorIdHotel = det.proveedor_id ?? det.proveedorId ?? null
      if (!nombreHotel && rooms[0]?.nombreHotel)             nombreHotel       = rooms[0].nombreHotel
      if (!hotelId     && rooms[0]?.hotelId)                 hotelId           = rooms[0].hotelId
      if (!fechaCancelacion  && rooms[0]?.fechaCancelacion)  fechaCancelacion  = rooms[0].fechaCancelacion
      if (!motivoCancelacion && rooms[0]?.motivoCancelacion) motivoCancelacion = rooms[0].motivoCancelacion
    }
  }

return {
    id: r.id, reservacionId: r.id,
    noReservacion: r.no_reservacion,
    estadoReserva, total: r.total,
    fechaCreacion: r.fecha_creacion, fechaExpiracion: r.fecha_expiracion,
    _categoria: tipoLabel(r.tipo_reserva),
    _preview: false,
    _detallesRaw: detallesRaw,
    boletos, _habitacionesPreview: [],
    habitaciones, nombreHotel, hotelId,
    proveedorIdVuelo, proveedorIdHotel,
    usuarioNombre, usuarioEmail,
    fechaCancelacion, motivoCancelacion
  }
}

/** Lista completa de reservaciones del usuario (snapshots). @type {import('vue').Ref<Object[]>} */
const reservas = ref([])

/** Objeto de resumen calculado localmente con totales y conteos. @type {import('vue').Ref<Object|null>} */
const resumen = ref(null)

/** Indica si la carga inicial está en progreso. @type {boolean} */
const loading = ref(true)

/** Mensaje de error de la carga principal. @type {string} */
const error = ref('')

/** Clave del filtro de estado activo. @type {string} */
const filtroActivo = ref('todas')

/** Clave del tab de categoría activo. @type {string} */
const tabActivo = ref('todas')

/** Texto del buscador por código de reservación. @type {string} */
const busqueda = ref('')

/** Lista de notificaciones toast visibles. @type {Array<{ id: number, msg: string, tipo: string }>} */
const toasts = ref([])

/** Reserva actualmente abierta en el panel lateral (null si está cerrado). @type {Object|null} */
const panelReserva = ref(null)

/** Indica si el panel está cargando los datos del proveedor. @type {boolean} */
const panelLoading = ref(false)

/** Error del panel lateral al cargar datos del proveedor. @type {string} */
const panelError = ref('')

/** Controla si el formulario de cancelación está expandido. @type {boolean} */
const cancelAbierto = ref(false)

/** Texto del motivo de cancelación escrito por el usuario. @type {string} */
const cancelMotivo = ref('')

/** Indica si la petición de cancelación está en curso. @type {boolean} */
const cancelLoading = ref(false)

/** Error de validación o del backend al intentar cancelar. @type {string} */
const cancelError = ref('')

/** Indica si el usuario aceptó los términos de cancelación. @type {boolean} */
const cancelTerminos = ref(false)

/**
 * Comentarios cargados desde el proveedor para la reserva abierta en el panel.
 * Mezcla comentarios de vuelo (con rutaId / cantidadEstrellas) y de hotel (con hotelId / resena).
 * Se vacía al cerrar el panel.
 * @type {import('vue').Ref<Array>}
 */
const comentariosPanel = ref([])

/** Indica si los comentarios del panel están cargando. @type {boolean} */
const comentariosLoading = ref(false)

/** Número de estrellas seleccionado en el formulario de calificación de vuelo (1-5). @type {number} */
const calEstrellas = ref(0)

/** Estrella sobre la que está el cursor (hover) en el selector de calificación. @type {number} */
const calHover = ref(0)

/** Texto del comentario de calificación de vuelo. @type {string} */
const calContenido = ref('')

/** Indica si la petición de calificación de vuelo está en curso. @type {boolean} */
const calLoading = ref(false)

/** Error de validación al enviar la calificación de vuelo. @type {string} */
const calError = ref('')

/** Se activa cuando la calificación de vuelo fue enviada exitosamente. @type {boolean} */
const calExito = ref(false)

/** Número de estrellas seleccionado en el formulario de reseña de hotel (1-5). @type {number} */
const resEstrellas = ref(0)

/** Estrella sobre la que está el cursor (hover) en el selector de reseña. @type {number} */
const resHover = ref(0)

/** Texto del contenido de la reseña del hotel. @type {string} */
const resContenido = ref('')

/** Indica si la petición de reseña de hotel está en curso. @type {boolean} */
const resLoading = ref(false)

/** Error de validación al enviar la reseña del hotel. @type {string} */
const resError = ref('')

/** Se activa cuando la reseña del hotel fue enviada exitosamente. @type {boolean} */
const resenaOk = ref(false)

/** Indica si la descarga del PDF está en curso. @type {boolean} */
const pdfLoading = ref(false)

/** Indica si el envío del correo de confirmación está en curso. @type {boolean} */
const correoLoading = ref(false)

/** Indica si está en modo edición dentro del panel. @type {boolean} */
const panelEditandoReservacion = ref(false)

/** Indica si está guardando la edición. @type {boolean} */
const editLoading = ref(false)

/** Error al editar. @type {string} */
const editError = ref('')

/** Indica éxito en la edición. @type {boolean} */
const editOk = ref(false)

/** Lista de cambios realizados. @type {Array} */
const editChanges = ref([])

/** Formulario de edición de reservación. @type {Object} */
const editForm = ref({
  pasajeros: [],
  fechaIda: '',
  fechaRetorno: '',
  fechaCheckIn: '',
  fechaCheckOut: ''
})

/** Fechas originales guardadas cuando se abre el panel. @type {Object} */
const editFormOriginal = ref({
  fechaIda: '',
  fechaRetorno: '',
  fechaCheckIn: '',
  fechaCheckOut: ''
})

/**
 * Estado de expansión de nodos en el árbol de comentarios del panel (modo lectura).
 * Clave: id del comentario, valor: { expandido, mostrandoForm, textoRespuesta, enviando, votoActual }.
 * @type {import('vue').Ref<Object>}
 */
const estadoNodosMR = ref({})

/**
 * Alterna el estado expandido de un nodo de comentario en el panel.
 *
 * @param {number} id - ID del comentario a expandir/colapsar.
 */
function toggleExpandidoMR(id) {
  estadoNodosMR.value = {
    ...estadoNodosMR.value,
    [id]: {
      ...(estadoNodosMR.value[id] ?? { expandido: false, mostrandoForm: false, textoRespuesta: '', enviando: false, votoActual: null }),
      expandido: !estadoNodosMR.value[id]?.expandido
    }
  }
}

/**
 * Devuelve los comentarios hijos de un comentario padre dado.
 *
 * @param {number} parentId - ID del comentario padre.
 * @returns {Array}
 */
function getHijosMR(parentId) {
  return comentariosPanel.value.filter(c => c.comentarioPadreId === parentId)
}

/**
 * Devuelve los comentarios raíz (sin padre) del panel actual.
 *
 * @returns {Array}
 */
function getComentariosRaizMR() {
  return comentariosPanel.value.filter(c => c.comentarioPadreId === null)
}

/**
 * Devuelve solo los comentarios raíz que tienen puntuación (reseñas reales).
 *
 * @returns {Array}
 */
function getResenasRaizMR() {
  return comentariosPanel.value.filter(c => c.comentarioPadreId === null && (c.resena !== null || c.cantidadEstrellas !== null))
}

/**
 * Calcula el promedio de estrellas de las reseñas raíz del panel actual.
 *
 * @returns {number} Promedio entre 0 y 5.
 */
function getPromedioMR() {
  const r = getResenasRaizMR()
  if (!r.length) return 0
  return r.reduce((s, c) => s + (c.resena ?? c.cantidadEstrellas ?? 0), 0) / r.length
}

/**
 * Definición de los filtros de estado disponibles en la barra de controles.
 * @type {Array<{ key: string, label: string, campo: string }>}
 */
const filtros = [
  { key: 'todas',      label: 'Todas',      campo: 'totalReservaciones' },
  { key: 'pendiente',  label: 'Pendientes',  campo: 'pendientes' },
  { key: 'confirmada', label: 'Confirmadas', campo: 'confirmadas' },
  { key: 'en curso',   label: 'En Curso',    campo: 'enCurso' },
  { key: 'completada', label: 'Completadas', campo: 'completadas' },
  { key: 'cancelada',  label: 'Canceladas',  campo: 'canceladas' },
  { key: 'expirada',   label: 'Expiradas',   campo: 'expiradas' },
  { key: 'retenida',   label: 'Retenidas',   campo: 'retenidas' },
]

/**
 * Definición de los tabs de categoría con su icono SVG inline.
 * @type {Array<{ key: string, label: string, icon: string }>}
 */
const tabs = [
  { key: 'todas',   label: 'Todas',    icon: '<circle cx="12" cy="12" r="10"/>' },
  { key: 'vuelo',   label: 'Vuelos',   icon: '<path fill="currentColor" stroke="none" d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>' },
  { key: 'hotel',   label: 'Hoteles',  icon: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>' },
  { key: 'paquete', label: 'Paquetes', icon: '<rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>' },
]

/**
 * Lista cruda de detalles de la reserva activa (del endpoint /mias/:id).
 * Se usa para filtrar por tipo y calcular subtotales + estado de cada detalle.
 * @type {import('vue').ComputedRef<Array>}
 */
const detallesRaw = computed(() => panelReserva.value?._detallesRaw ?? [])

/**
 * Detalles del panel activo que son de tipo vuelo (tipo_detalle_id === 1).
 * @type {import('vue').ComputedRef<Array>}
 */
const detallesVuelos = computed(() => detallesRaw.value.filter(d => d.tipo_detalle_id === 1))

/**
 * Detalles del panel activo que son de tipo hotel (tipo_detalle_id === 2).
 * @type {import('vue').ComputedRef<Array>}
 */
const detallesHoteles = computed(() => detallesRaw.value.filter(d => d.tipo_detalle_id === 2))

/**
 * Extrae el total del proveedor desde data_proveedor según el tipo de detalle.
 * Para vuelos: suma los precios de boletos.
 * Para hoteles: suma el totalDetalle de las habitaciones.
 * Usado para calcular el subtotal sin mostrar detalles individuales.
 *
 * @param {Object} d - Detalle con tipo_detalle_id y data_proveedor
 * @returns {number} Total extraído del proveedor, o 0 si no disponible
 */
function extraerTotalProveedor(d) {
  if (!d.data_proveedor) return 0

  // Vuelo: sumar precios de boletos
  if (d.tipo_detalle_id === 1) {
    const dp = d.data_proveedor
    if (Array.isArray(dp.boletos)) {
      let total = 0
      for (const boleto of dp.boletos) {
        const precio = boleto.precio ?? 0
        total += precio
      }
      return total
    }
    return dp.total ?? 0
  }

  // Hotel: sumar totalDetalle de habitaciones
  if (d.tipo_detalle_id === 2) {
    const dp = d.data_proveedor
    let total = 0

    if (Array.isArray(dp)) {
      // Array de habitaciones
      for (const hab of dp) {
        total += hab.totalDetalle ?? hab.total ?? 0
      }
    } else if (Array.isArray(dp.habitaciones)) {
      // Objeto con array de habitaciones
      for (const hab of dp.habitaciones) {
        total += hab.totalDetalle ?? hab.total ?? 0
      }
    } else {
      // Fallback: total directo
      total = dp.total ?? dp.totalDetalle ?? 0
    }
    return total
  }

  return 0
}

/**
 * Subtotal de la reserva calculado desde los totales del proveedor en data_proveedor.
 * Usa la data del proveedor para calcular, pero NO la muestra individualmente.
 * @type {import('vue').ComputedRef<number>}
 */
const subtotalReserva = computed(() =>
  detallesRaw.value.reduce((s, d) => {
    const totalProveedor = extraerTotalProveedor(d)
    return s + totalProveedor
  }, 0)
)

/**
 * Monto de impuestos mostrado al usuario (internamente es la ganancia de la agencia).
 * Se calcula como la diferencia entre el total pagado y la suma de los detalles.
 * Si la diferencia es negativa (no debería pasar), retorna 0.
 * @type {import('vue').ComputedRef<number>}
 */
const montoImpuestos = computed(() => {
  const total = panelReserva.value?.total ?? 0
  const sub   = subtotalReserva.value
  const diff  = total - sub
  return diff > 0 ? diff : 0
})

/**
 * Etiqueta descriptiva de una habitación para mostrar en el desglose.
 * Usa el tipo de habitación del data_proveedor si está disponible,
 * o un genérico "Habitación" si el proveedor no respondió.
 *
 * @param {Object} d - Detalle crudo del backend.
 * @returns {string}
 */
function etiquetaHabitacion(d) {
  const hab = panelReserva.value?.habitaciones?.find(h => h.detalleId === d.id)
  return hab?.tipoHabitacion ?? 'Habitación'
}

/**
 * Lista de reservaciones filtradas por estado activo, tab de categoría y búsqueda por código.
 * @type {import('vue').ComputedRef<Object[]>}
 */
const reservasFiltradas = computed(() => {
  let list = reservas.value
  if (filtroActivo.value !== 'todas')
    list = list.filter(r => r.estadoReserva?.toLowerCase() === filtroActivo.value)
  if (tabActivo.value !== 'todas')
    list = list.filter(r => r._categoria === tabActivo.value)
  if (busqueda.value.trim())
    list = list.filter(r => (r.noReservacion || '').toLowerCase().includes(busqueda.value.toLowerCase()))
  return list
})

/**
 * Cuenta las reservaciones de una categoría específica (o todas si key = 'todas').
 *
 * @param {string} key - Clave del tab ('todas' | 'vuelo' | 'hotel' | 'paquete').
 * @returns {number}
 */
function countPorTab(key) {
  if (key === 'todas') return reservas.value.length
  return reservas.value.filter(r => r._categoria === key).length
}

/**
 * Agrega un mensaje toast a la pila y lo elimina automáticamente después de 4 segundos.
 *
 * @param {string} msg - Mensaje a mostrar.
 * @param {'success'|'error'} [tipo='success'] - Tipo de notificación.
 */
function addToast(msg, tipo = 'success') {
  const id = Date.now()
  toasts.value.push({ id, msg, tipo })
  setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 4000)
}

/**
 * Devuelve la clase CSS del badge según el estado de una reserva o boleto.
 *
 * @param {string} e - Etiqueta del estado.
 * @returns {string} Clase CSS correspondiente.
 */
function estadoClase(e) {
  if (!e) return 'mv-badge--pendiente'
  const s = e.toLowerCase()
  if (s === 'confirmada') return 'mv-badge--confirmada'
  if (s === 'cancelada')  return 'mv-badge--cancelada'
  if (s === 'completada') return 'mv-badge--completada'
  if (s === 'expirada')   return 'mv-badge--expirada'
  if (s === 'en curso')   return 'mv-badge--encurso'
  if (s === 'retenida')   return 'mv-badge--retenida'
  return 'mv-badge--pendiente'
}

/**
 * Formatea una fecha ISO incluyendo hora en formato legible en español.
 *
 * @param {string} f - Fecha ISO.
 * @returns {string}
 */
function formatFechaHora(f) {
  if (!f) return '--'
  const d = new Date(f)
  return d.toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
    + ' ' + d.toLocaleTimeString('es-GT', { hour:'2-digit', minute:'2-digit' })
}

/**
 * Formatea una fecha ISO a cadena legible corta en español (ej. "04 abr 2026").
 *
 * @param {string} f - Fecha ISO.
 * @returns {string}
 */
function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}

/**
 * Extrae la hora (HH:MM) de una cadena de tiempo "HH:MM:SS".
 *
 * @param {string} h - Cadena de hora.
 * @returns {string}
 */
function formatHora(h) { return h ? h.substring(0, 5) : '--' }

/**
 * Formatea una duración en minutos a "Xh Ym".
 *
 * @param {number} min - Duración en minutos.
 * @returns {string}
 */
function formatDuracion(min) { if (!min) return '--'; return `${Math.floor(min/60)}h${min%60>0?' '+min%60+'m':''}` }

/**
 * Calcula la cantidad de noches entre check-in y check-out.
 *
 * @param {string} ci - Fecha de check-in ISO.
 * @param {string} co - Fecha de check-out ISO.
 * @returns {number}
 */
function calcNoches(ci, co) { if (!ci||!co) return 0; return Math.max(0, Math.ceil((new Date(co)-new Date(ci))/86400000)) }

/**
 * Verifica si el usuario actual ya calificó una ruta de vuelo específica
 * buscando en los comentarios del panel un comentario raíz con estrellas.
 *
 * @param {number} rutaId - ID de la ruta aérea.
 * @returns {boolean}
 */
function yaComentaRuta(rutaId) {
  if (!rutaId) return false
  const uid = getUsuarioIdActual()
  return comentariosPanel.value.some(c =>
    c.rutaId === rutaId &&
    c.comentarioPadreId === null &&
    c.cantidadEstrellas !== null &&
    (uid === null || c.usuarioId === uid)
  )
}

/**
 * Devuelve el comentario/calificación del usuario para una ruta dada,
 * o null si no existe todavía.
 *
 * @param {number} rutaId - ID de la ruta aérea.
 * @returns {Object|null}
 */
function obtenerComentarioRuta(rutaId) {
  if (!rutaId) return null
  const uid = getUsuarioIdActual()
  return comentariosPanel.value.find(c =>
    c.rutaId === rutaId &&
    c.comentarioPadreId === null &&
    c.cantidadEstrellas !== null &&
    (uid === null || c.usuarioId === uid)
  ) ?? null
}

/**
 * Verifica si el usuario actual ya dejó una reseña para un hotel concreto.
 * Busca comentarios raíz con campo resena != null en los datos del panel.
 *
 * @param {number} hotelId - ID del hotel.
 * @returns {boolean}
 */
function yaResenaHotel(hotelId) {
  if (!hotelId) return false
  const uid = getUsuarioIdActual()
  return comentariosPanel.value.some(c =>
    c.hotelId === hotelId &&
    c.comentarioPadreId === null &&
    c.resena !== null &&
    (uid === null || c.usuarioId === uid)
  )
}

/**
 * Devuelve la reseña del usuario para un hotel dado, o null si no existe.
 *
 * @param {number} hotelId - ID del hotel.
 * @returns {Object|null}
 */
function obtenerResenaHotel(hotelId) {
  if (!hotelId) return null
  const uid = getUsuarioIdActual()
  return comentariosPanel.value.find(c =>
    c.hotelId === hotelId &&
    c.comentarioPadreId === null &&
    c.resena !== null &&
    (uid === null || c.usuarioId === uid)
  ) ?? null
}

/**
 * Calcula el objeto resumen a partir de la lista local de reservaciones.
 * Se llama después de cada carga o actualización del array reservas.
 */
function calcularResumen() {
  const list = reservas.value
  resumen.value = {
    totalGastado:       list.reduce((s, r) => s + (r.total ?? 0), 0),
    totalReservaciones: list.length,
    pendientes:  list.filter(r => r.estadoReserva?.toLowerCase() === 'pendiente').length,
    confirmadas: list.filter(r => r.estadoReserva?.toLowerCase() === 'confirmada').length,
    enCurso:     list.filter(r => r.estadoReserva?.toLowerCase() === 'en curso').length,
    completadas: list.filter(r => r.estadoReserva?.toLowerCase() === 'completada').length,
    canceladas:  list.filter(r => r.estadoReserva?.toLowerCase() === 'cancelada').length,
    expiradas:   list.filter(r => r.estadoReserva?.toLowerCase() === 'expirada').length,
    retenidas:   list.filter(r => r.estadoReserva?.toLowerCase() === 'retenida').length,
    vuelos:   list.filter(r => r._categoria === 'vuelo').length,
    hoteles:  list.filter(r => r._categoria === 'hotel').length,
    paquetes: list.filter(r => r._categoria === 'paquete').length,
  }
}

/**
 * Carga la lista de reservaciones del usuario desde el backend y recalcula el resumen.
 * Se llama al montar la vista y después de cancelar una reserva.
 *
 * @async
 * @returns {Promise<void>}
 */
async function cargarTodo() {
  loading.value = true
  error.value   = ''
  try {
    const data = await apiFetch(`${BASE}/api/reservaciones/mias`)
    reservas.value = data.map(fromLista)
    calcularResumen()
  } catch {
    error.value = 'No se pudieron cargar tus reservaciones. Intenta de nuevo.'
  } finally {
    loading.value = false
  }
}

/**
 * Carga los comentarios y reseñas reales del proveedor para la reserva abierta en el panel.
 * Para cada ruta única en los boletos llama a GET /api/comentarios/vuelo/:provId/:rutaId.
 * Si hay hotel llama a GET /api/comentarios/hotel/:provId/:hotelId.
 * Todas las peticiones se hacen en paralelo con Promise.all.
 *
 * @async
 * @param {Object} reserva - Reserva normalizada (fromDetalle) actualmente en el panel.
 * @returns {Promise<void>}
 */
async function cargarComentariosPanel(reserva) {
  comentariosPanel.value  = []
  comentariosLoading.value = true

  try {
    const promesas = []

    // Comentarios de vuelo: una petición por cada rutaId único en los boletos
    if (reserva._categoria === 'vuelo' || reserva._categoria === 'paquete') {
      const provId = reserva.proveedorIdVuelo
      const rutasUnicas = [...new Set(
        (reserva.boletos ?? [])
          .map(b => b.rutaId)
          .filter(id => id != null)
      )]

      for (const rutaId of rutasUnicas) {
        const url = provId
          ? `${BASE}/api/comentarios/vuelo/${provId}/${rutaId}`
          : `${BASE}/api/comentarios/vuelo/${rutaId}`
        promesas.push(
          apiFetch(url)
            .then(data => { comentariosPanel.value.push(...(data ?? [])) })
            .catch(() => {})
        )
      }
    }

    // Comentarios del hotel si la reserva incluye hospedaje
    if ((reserva._categoria === 'hotel' || reserva._categoria === 'paquete') && reserva.hotelId) {
      const provId = reserva.proveedorIdHotel
      const url = provId
        ? `${BASE}/api/comentarios/hotel/${provId}/${reserva.hotelId}`
        : `${BASE}/api/comentarios/hotel/${reserva.hotelId}`
      promesas.push(
        apiFetch(url)
          .then(data => { comentariosPanel.value.push(...(data ?? [])) })
          .catch(() => {})
      )
    }

    await Promise.all(promesas)
  } finally {
    comentariosLoading.value = false
  }
}

/**
 * Abre el panel lateral para una reserva. Muestra el snapshot inmediatamente
 * y luego carga los datos reales del proveedor en segundo plano.
 * Si el estado es "completada" también carga los comentarios del proveedor.
 *
 * @async
 * @param {Object} reserva - Reserva normalizada del listado.
 * @returns {Promise<void>}
 */
async function abrirPanel(reserva) {
  panelReserva.value  = { ...reserva }
  panelLoading.value  = true
  panelError.value    = ''
  cancelAbierto.value  = false
  cancelMotivo.value   = ''
  cancelError.value    = ''
  cancelTerminos.value = false
  resetCal()
  resetRes()

  try {
    // Obtener datos reales del proveedor (boletos con rutaId, habitaciones, etc.)
    const data = await apiFetch(`${BASE}/api/reservaciones/mias/${reserva.id}`)
    panelReserva.value = fromDetalle(data)
  } catch {
    panelError.value = 'No se pudieron cargar los detalles del proveedor.'
    panelLoading.value = false
    return
  } finally {
    panelLoading.value = false
  }

  // Pre-llenar el formulario de edición con las fechas actuales
  if (panelReserva.value._categoria === 'vuelo' && panelReserva.value.boletos?.length > 0) {
    // Para vuelos: usar la fecha del primer boleto como ida, y del último como retorno
    const fIda = panelReserva.value.boletos[0]?.fechaVuelo || ''
    const fRetorno = panelReserva.value.boletos[panelReserva.value.boletos.length - 1]?.fechaVuelo || ''
    editForm.value.fechaIda = fIda
    editForm.value.fechaRetorno = fRetorno
    editFormOriginal.value.fechaIda = fIda
    editFormOriginal.value.fechaRetorno = fRetorno
  } else if (panelReserva.value._categoria === 'hotel' && panelReserva.value.habitaciones?.length > 0) {
    const hab = panelReserva.value.habitaciones[0]
    const fIn = hab.fechaCheckIn || ''
    const fOut = hab.fechaCheckOut || ''
    editForm.value.fechaCheckIn = fIn
    editForm.value.fechaCheckOut = fOut
    editFormOriginal.value.fechaCheckIn = fIn
    editFormOriginal.value.fechaCheckOut = fOut
  }

  // Cargar comentarios/reseñas solo si la reserva ya fue completada
  if (panelReserva.value.estadoReserva?.toLowerCase() === 'completada') {
    await cargarComentariosPanel(panelReserva.value)
  }
}

/**
 * Cierra el panel lateral y limpia todo su estado interno.
 */
function cerrarPanel() {
  panelReserva.value   = null
  panelError.value     = ''
  comentariosPanel.value = []
  estadoNodosMR.value  = {}
  resetCal()
  resetRes()
}

/**
 * Envía la solicitud de cancelación de la reserva activa en el panel.
 * Requiere que el usuario haya escrito un motivo y aceptado los términos.
 * Recarga la lista y reabre el panel con el estado actualizado.
 *
 * @async
 * @returns {Promise<void>}
 */
async function confirmarCancelar() {
  if (!cancelMotivo.value.trim()) { cancelError.value = 'Escribe un motivo de cancelación.'; return }
  if (!cancelTerminos.value)      { cancelError.value = 'Debes aceptar los términos de cancelación.'; return }

  cancelLoading.value = true
  cancelError.value   = ''

  try {
    const res = await fetch(`${BASE}/api/reservaciones/${panelReserva.value.id}/cancelar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ motivo: cancelMotivo.value.trim() })
    })

    const data = await res.json().catch(() => ({}))

    if (!res.ok) {
      if (res.status === 422) {
        cancelError.value = data.error ?? 'No se puede cancelar esta reservación en su estado actual.'
        return
      }
      cancelError.value = data.error ?? `Error ${res.status} al procesar la cancelación.`
      return
    }

    addToast(data.mensaje ?? 'Reservación cancelada exitosamente')
    cancelAbierto.value  = false
    cancelTerminos.value = false
    cancelMotivo.value   = ''

    const reservaId = panelReserva.value.id
    await cargarTodo()
    await abrirPanel({ id: reservaId })

  } catch {
    cancelError.value = 'Error de conexión. Verifica tu red e intenta de nuevo.'
    addToast('Error al cancelar la reservación', 'error')
  } finally {
    cancelLoading.value = false
  }
}

/**
 * Descarga el PDF de una reservación y lo ofrece al usuario como archivo.
 *
 * @async
 * @param {number} id - ID de la reservación.
 * @returns {Promise<void>}
 */
async function descargarPDF(id) {
  pdfLoading.value = true
  try {
    const res = await fetch(`${BASE}/api/reservaciones/${id}/pdf`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const blob = await res.blob()
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href     = url
    a.download = `reservacion-${id}.pdf`
    a.click()
    URL.revokeObjectURL(url)
    addToast('PDF descargado correctamente')
  } catch {
    addToast('Error al descargar el PDF', 'error')
  } finally {
    pdfLoading.value = false
  }
}

/**
 * Solicita al backend que envíe el correo de confirmación de una reservación.
 *
 * @async
 * @param {number} id - ID de la reservación.
 * @returns {Promise<void>}
 */
async function enviarCorreo(id) {
  correoLoading.value = true
  try {
    await apiFetch(`${BASE}/api/reservaciones/${id}/correo`, { method: 'POST' })
    addToast('Confirmación enviada al correo')
  } catch {
    addToast('Error al enviar el correo', 'error')
  } finally {
    correoLoading.value = false
  }
}

/** Resetea todos los refs del formulario de calificación de vuelo. */
function resetCal() {
  calEstrellas.value = 0
  calHover.value     = 0
  calContenido.value = ''
  calError.value     = ''
  calExito.value     = false
}

/**
 * Envía la calificación de un vuelo al proveedor y recarga los comentarios del panel.
 * Requiere al menos 1 estrella y un comentario no vacío.
 *
 * @async
 * @param {number} rutaId - ID de la ruta aérea a calificar.
 * @returns {Promise<void>}
 */
async function enviarCalificacion(rutaId) {
  if (calEstrellas.value < 1)       { calError.value = 'Selecciona al menos 1 estrella.'; return }
  if (!calContenido.value.trim())   { calError.value = 'Escribe tu comentario.'; return }

  calLoading.value = true
  calError.value   = ''

  try {
    await apiFetch(`${BASE}/api/comentarios/vuelo`, {
      method: 'POST',
      body: JSON.stringify({
        rutaId,
        cantidadEstrellas: calEstrellas.value,
        contenido: calContenido.value.trim()
      })
    })

    // Recargar comentarios para reflejar la nueva calificación
    await cargarComentariosPanel(panelReserva.value)
    calExito.value = true
    addToast('¡Calificación enviada! Gracias por tu opinión.')

  } catch {
    calError.value = 'Error al enviar la calificación. Intenta de nuevo.'
  } finally {
    calLoading.value = false
  }
}

/** Resetea todos los refs del formulario de reseña de hotel. */
function resetRes() {
  resEstrellas.value = 0
  resHover.value     = 0
  resContenido.value = ''
  resError.value     = ''
  resenaOk.value     = false
}

/**
 * Envía la reseña de un hotel al proveedor y recarga los comentarios del panel.
 * Requiere al menos 1 estrella y texto no vacío. Detecta el hotelId desde la reserva activa.
 *
 * @async
 * @returns {Promise<void>}
 */
async function enviarResenaHotel() {
  const hotelId = panelReserva.value?.hotelId ?? panelReserva.value?.habitaciones?.[0]?.hotelId

  if (!hotelId)                   { resError.value = 'No se identificó el hotel.'; return }
  if (resEstrellas.value < 1)     { resError.value = 'Selecciona al menos 1 estrella.'; return }
  if (!resContenido.value.trim()) { resError.value = 'Escribe tu reseña.'; return }

  resLoading.value = true
  resError.value   = ''

  try {
    await apiFetch(`${BASE}/api/comentarios/hotel`, {
      method: 'POST',
      body: JSON.stringify({
        hotelId,
        cantidadEstrellas: resEstrellas.value,
        contenido: resContenido.value.trim()
      })
    })

    // Recargar comentarios para reflejar la nueva reseña
    await cargarComentariosPanel(panelReserva.value)
    resenaOk.value = true
    addToast('¡Reseña enviada! Gracias por compartir tu experiencia.')

  } catch {
    resError.value = 'Error al enviar la reseña. Intenta de nuevo.'
  } finally {
    resLoading.value = false
  }
}

/** Abre modo edición en panel para editar fechas de hospedaje. Solo permite hotel y paquete. */
function openEditarReservacion() {
  if (!panelReserva.value) return

  // Solo permitir editar hotel y paquete
  if (panelReserva.value._categoria !== 'hotel' && panelReserva.value._categoria !== 'paquete') {
    addToast('Solo se pueden editar fechas de hospedaje', 'error')
    return
  }

  // Inicializar formulario con fechas actuales del hotel
  const hab = panelReserva.value.habitaciones?.[0]
  editForm.value = {
    fechaCheckIn: hab?.fechaCheckIn ?? '',
    fechaCheckOut: hab?.fechaCheckOut ?? ''
  }
  editFormOriginal.value = {
    fechaCheckIn: hab?.fechaCheckIn ?? '',
    fechaCheckOut: hab?.fechaCheckOut ?? ''
  }

  editError.value = ''
  editOk.value = false
  editChanges.value = []
  panelEditandoReservacion.value = true
}

/** Cierra panel de editar reservación. */
function closeEditarReservacion() {
  panelEditandoReservacion.value = false
  editForm.value = {
    fechaCheckIn: '',
    fechaCheckOut: ''
  }
  editError.value = ''
  editOk.value = false
  editChanges.value = []
}

/** Confirma y guarda los cambios de la reservación. */
async function confirmarEdicion() {
  if (!panelReserva.value) return

  editLoading.value = true
  editError.value = ''

  try {
    const res = await apiFetch(`${BASE}/api/reservaciones/${panelReserva.value.id}/editar`, {
      method: 'PUT',
      body: JSON.stringify({
        fechaCheckIn: editForm.value.fechaCheckIn,
        fechaCheckOut: editForm.value.fechaCheckOut,
        fechaCheckInActual: editFormOriginal.value.fechaCheckIn,
        fechaCheckOutActual: editFormOriginal.value.fechaCheckOut
      })
    })

    if (res && res.cambios) {
      editChanges.value = res.cambios
    }

    editOk.value = true
    addToast('Reservación actualizada exitosamente')

    // Recargar página después de 1.5 segundos
    // TODO: Comentado para debugueo manual
    // setTimeout(() => {
    //   location.reload()
    // }, 1500)
  } catch (err) {
    editError.value = err.message || 'Error al editar la reservación'
    addToast('Error: ' + (err.message || 'Error al editar'), 'error')
  } finally {
    editLoading.value = false
  }
}

import { useRoute } from 'vue-router'

const route = useRoute()

onMounted(async () => {
  await cargarTodo()
  
  const verReserva = route.query.ver
  if (verReserva) {
    setTimeout(() => {
      const r = reservas.value.find(x => x.noReservacion === verReserva)
      if (r) abrirPanel(r)
    }, 300)
  }
})
</script>