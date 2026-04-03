<template>
  <div class="page">
    <Encabezado />

    <!-- TOASTS -->
    <div class="mv-toast-stack">
      <div v-for="t in toasts" :key="t.id" :class="['mv-toast', `mv-toast--${t.tipo}`]">
        <svg v-if="t.tipo==='success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        <span>{{ t.msg }}</span>
      </div>
    </div>

    <!-- PANEL LATERAL -->
    <Transition name="mv-panel">
      <div v-if="panelReserva" class="mv-overlay" @click.self="cerrarPanel">
        <div class="mv-panel" role="dialog" aria-modal="true">

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

            <!-- Hero del panel -->
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

            <!-- Loading panel detail -->
            <div v-if="panelLoading" class="mv-panel__center">
              <div class="mv-spinner mv-spinner--lg"></div>
              <p>Cargando detalles del proveedor...</p>
            </div>

            <template v-else-if="!panelError">

              <!-- Info general -->
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

              <!-- BOLETOS — datos reales de Broom AirLine (data_proveedor) -->
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
                      <div class="mv-boleto__cell"><span class="mv-boleto__clbl">Precio</span><span class="mv-boleto__cval mv-boleto__cval--price">${{ boleto.precio?.toFixed(2) }}</span></div>
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

              <!-- HABITACIONES — datos reales de Miku Inn (data_proveedor) -->
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
                      <span class="mv-hab__precio">${{ h.totalDetalle?.toFixed(2) }}</span>
                    </div>
                    <p class="mv-hab__desc">{{ h.descripcionHabitacion }}</p>
                    <div class="mv-hab__meta">
                      <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg> {{ formatFecha(h.fechaCheckIn) }} → {{ formatFecha(h.fechaCheckOut) }}</span>
                      <span>{{ calcNoches(h.fechaCheckIn, h.fechaCheckOut) }} noches</span>
                      <span><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> {{ h.cantidadPersonas }} huésped{{ h.cantidadPersonas !== 1 ? 'es' : '' }}</span>
                    </div>
                  </div>
                  <!-- Desglose de costos -->
                  <div class="mv-desglose">
                    <div v-for="h in panelReserva.habitaciones" :key="h.detalleId" class="mv-desglose__row">
                      <span>{{ h.tipoHabitacion }}</span><span>${{ h.totalDetalle?.toFixed(2) }}</span>
                    </div>
                    <div class="mv-desglose__total">
                      <span>Total hospedaje</span>
                      <strong>${{ panelReserva.habitaciones.reduce((s,h)=>s+(h.totalDetalle??0),0).toFixed(2) }}</strong>
                    </div>
                  </div>
                </div>
              </template>

              <!-- ═══════ COMENTARIOS DEL PROVEEDOR (lectura) ═══════ -->
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

              <!-- ═══════ CALIFICAR VUELO ═══════ -->
              <!-- Solo si: categoría vuelo/paquete + estado completada + rutaId disponible -->
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

                <!-- Loading comentarios vuelo -->
                <div v-if="comentariosLoading" class="mv-panel__center" style="padding:1.2rem">
                  <div class="mv-spinner"></div>
                  <p style="font-size:0.8rem;color:#7a7067">Verificando calificaciones...</p>
                </div>

                <!-- Ya calificó -->
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

                <!-- Formulario calificación -->
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

              <!-- ═══════ RESEÑA HOTEL ═══════ -->
              <!-- Solo si: categoría hotel/paquete + estado completada -->
              <div
                v-if="(panelReserva._categoria==='hotel' || panelReserva._categoria==='paquete')
                       && panelReserva.estadoReserva?.toLowerCase()==='completada'"
                class="mv-panel__section"
              >
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  Reseña · {{ panelReserva.nombreHotel }}
                </h4>

                <!-- Loading comentarios hotel -->
                <div v-if="comentariosLoading" class="mv-panel__center" style="padding:1.2rem">
                  <div class="mv-spinner"></div>
                  <p style="font-size:0.8rem;color:#7a7067">Verificando reseñas...</p>
                </div>

                <!-- Ya reseñó -->
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

                <!-- Formulario reseña -->
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

              <!-- ═══════ CANCELAR ═══════ -->
              <!-- Solo Pendiente o Confirmada -->
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

            <!-- Footer panel -->
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

    <!-- ═══════════════════ PÁGINA PRINCIPAL ═══════════════════ -->
    <div class="mv-page">
      <div class="mv-container">

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

        <!-- RESUMEN -->
        <div v-if="resumen" class="mv-resumen">
          <div class="mv-resumen__hero">
            <div class="mv-resumen__hero-left">
              <p class="mv-resumen__hero-lbl">Total invertido en viajes</p>
              <p class="mv-resumen__hero-monto">${{ resumen.totalGastado?.toFixed(2) ?? '0.00' }}</p>
              <p class="mv-resumen__hero-sub">{{ resumen.totalReservaciones ?? 0 }} reservaciones en total</p>
            </div>
            <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,204,0,0.25)" stroke-width="0.8" width="100" height="100" class="mv-resumen__hero-deco"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
          </div>

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

        <!-- Filtros de estado -->
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

        <!-- Tabs por categoría -->
        <div class="mv-tabs">
          <button v-for="t in tabs" :key="t.key"
            :class="['mv-tab', { 'mv-tab--active': tabActivo===t.key }]"
            @click="tabActivo=t.key" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15" v-html="t.icon"></svg>
            {{ t.label }}
            <span class="mv-tab__n">{{ countPorTab(t.key) }}</span>
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="mv-empty">
          <div class="mv-spinner mv-spinner--xl"></div>
          <p>Cargando tus reservaciones...</p>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="mv-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="48" height="48"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <p>{{ error }}</p>
          <button class="mv-btn mv-btn--primary" @click="cargarTodo" type="button">Reintentar</button>
        </div>

        <!-- Vacío -->
        <div v-else-if="reservasFiltradas.length===0" class="mv-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="56" height="56"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
          <p class="mv-empty__title">Sin reservaciones</p>
          <p class="mv-empty__sub">{{ filtroActivo !== 'todas' || tabActivo !== 'todas' ? 'Prueba cambiando el filtro' : 'Haz tu primera reserva' }}</p>
          <button v-if="filtroActivo==='todas' && tabActivo==='todas'" class="mv-btn mv-btn--primary" @click="$router.push('/principal')" type="button">Buscar viajes</button>
          <button v-else class="mv-btn mv-btn--ghost" @click="filtroActivo='todas'; tabActivo='todas'" type="button">Ver todas</button>
        </div>

        <!-- ═══════════════════ LISTA DE CARDS ═══════════════════ -->
        <div v-else class="mv-lista">
          <article v-for="r in reservasFiltradas" :key="r.id"
            class="mv-card" :class="`mv-card--${r._categoria}`"
            @click="abrirPanel(r)" tabindex="0" @keydown.enter="abrirPanel(r)">

            <!-- Franja de categoría -->
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

              <!-- VUELO / PAQUETE -->
              <template v-if="(r._categoria==='vuelo' || r._categoria==='paquete') && r.boletos?.length">
                <!-- Ruta completa (data_proveedor, solo desde panel) -->
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

                <!-- Preview compacto: datos snapshot (parametros_json) -->
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

              <!-- HOTEL / PAQUETE — habitaciones -->
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

              <template v-else-if="(r._categoria==='hotel' || r._categoria==='paquete') && r._habitacionesPreview?.[0]">
                <div class="mv-card__hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  <div>
                    <span class="mv-card__hotel-nombre">Hospedaje reservado</span>
                    <span class="mv-card__hotel-hab">{{ r._habitacionesPreview[0].noReservacion }}</span>
                  </div>
                </div>
              </template>

              <!-- Fecha de creación siempre visible -->
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/misreservaciones.css'
import ComentarioNodo from '../components/Comentarionodo.vue'

const router = useRouter()

// ─── CONFIG ────────────────────────────────────────────────────────────────────
const BASE = 'http://localhost:8080'



// ─── JWT DECODE ────────────────────────────────────────────────────────────────
/**
 * Extrae el usuarioId del JWT guardado en storage.
 * Soporta claims: id | usuarioId | userId | sub (como número).
 * Si no encuentra ninguno, retorna null y los checks de "ya calificó"
 * quedarán en false (el usuario verá el formulario siempre).
 */
function getUsuarioIdActual() {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    if (!token) return null
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.usuarioId ?? payload.id ?? payload.userId ?? (Number(payload.sub) || null)
  } catch {
    return null
  }
}

// ─── AUTH HEADERS ─────────────────────────────────────────────────────────────
function authHeaders() {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  }
}

async function apiFetch(url, opts = {}) {
  const res = await fetch(url, { headers: authHeaders(), credentials: 'include', ...opts })
  if (!res.ok) {
    const msg = await res.text().catch(() => `Error ${res.status}`)
    throw new Error(msg || `Error ${res.status}`)
  }
  return res.json()
}

// ─── ESTADO ID MAP ─────────────────────────────────────────────────────────────
// 1=Pendiente  2=Confirmada  3=Cancelada  4=Expirada  5=Completada  6=En Curso
const ESTADO_ID_MAP = { 1:'Pendiente', 2:'Confirmada', 3:'Cancelada', 4:'Expirada', 5:'Completada', 6:'En Curso' }

function estadoLabel(id) { return ESTADO_ID_MAP[id] ?? 'Pendiente' }
function tipoLabel(t)    { return t === 1 ? 'vuelo' : t === 2 ? 'hotel' : 'paquete' }

// ─── NORMALIZADORES ────────────────────────────────────────────────────────────

/**
 * fromLista — snapshot de GET /api/reservaciones/mias
 * Solo contiene parametros_json. Sin data_proveedor (real).
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
 * fromDetalle — datos reales de GET /api/reservaciones/mias/:id
 * Incluye data_proveedor (estado actual en Broom / Miku).
 */
function fromDetalle(r) {
  const boletos      = []
  const habitaciones = []
  let nombreHotel = null, hotelId = null
  let usuarioNombre = null, usuarioEmail = null
  let fechaCancelacion = null, motivoCancelacion = null
  let estadoReserva    = estadoLabel(r.estado_id)
  // proveedorId extraído dinámicamente del detalle (soporta múltiples proveedores)
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
    boletos, _habitacionesPreview: [],
    habitaciones, nombreHotel, hotelId,
    proveedorIdVuelo, proveedorIdHotel,
    usuarioNombre, usuarioEmail,
    fechaCancelacion, motivoCancelacion
  }
}

// ─── ESTADO REACTIVO ───────────────────────────────────────────────────────────
const reservas     = ref([])
const resumen      = ref(null)
const loading      = ref(true)
const error        = ref('')
const filtroActivo = ref('todas')
const tabActivo    = ref('todas')
const busqueda     = ref('')
const toasts       = ref([])

const panelReserva = ref(null)
const panelLoading = ref(false)
const panelError   = ref('')

const cancelAbierto  = ref(false)
const cancelMotivo   = ref('')
const cancelLoading  = ref(false)
const cancelError    = ref('')
const cancelTerminos = ref(false)

// ─── ESTADO COMENTARIOS PANEL ─────────────────────────────────────────────────
/**
 * comentariosPanel: comentarios del panel actualmente abierto.
 * Cargados desde el proveedor real al abrir el panel.
 * Se vacían al cerrar.
 *
 * Estructura mezclada:
 *   Vuelo: { id, rutaId, usuarioId, username, cantidadEstrellas, contenido,
 *             comentarioPadreId, fecha, origen, destino, downs }
 *   Hotel: { id, hotelId, usuarioId, username, resena, contenido,
 *             comentarioPadreId, fecha, downs }
 */
const comentariosPanel  = ref([])
const comentariosLoading = ref(false)

// Cal vuelo
const calEstrellas = ref(0)
const calHover     = ref(0)
const calContenido = ref('')
const calLoading   = ref(false)
const calError     = ref('')
const calExito     = ref(false)

// Res hotel
const resEstrellas = ref(0)
const resHover     = ref(0)
const resContenido = ref('')
const resLoading   = ref(false)
const resError     = ref('')
const resenaOk     = ref(false)

const pdfLoading    = ref(false)
const correoLoading = ref(false)

// ─── ESTADO COMENTARIOS PANEL (modo lectura) ──────────────────────────────────
const estadoNodosMR = ref({})

function toggleExpandidoMR(id) {
  estadoNodosMR.value = {
    ...estadoNodosMR.value,
    [id]: {
      ...(estadoNodosMR.value[id] ?? { expandido: false, mostrandoForm: false, textoRespuesta: '', enviando: false, votoActual: null }),
      expandido: !estadoNodosMR.value[id]?.expandido
    }
  }
}

function getHijosMR(parentId) {
  return comentariosPanel.value.filter(c => c.comentarioPadreId === parentId)
}

function getComentariosRaizMR() {
  return comentariosPanel.value.filter(c => c.comentarioPadreId === null)
}

function getResenasRaizMR() {
  return comentariosPanel.value.filter(c => c.comentarioPadreId === null && (c.resena !== null || c.cantidadEstrellas !== null))
}

function getPromedioMR() {
  const r = getResenasRaizMR()
  if (!r.length) return 0
  return r.reduce((s, c) => s + (c.resena ?? c.cantidadEstrellas ?? 0), 0) / r.length
}

// ─── FILTROS / TABS ────────────────────────────────────────────────────────────
const filtros = [
  { key: 'todas',      label: 'Todas',      campo: 'totalReservaciones' },
  { key: 'pendiente',  label: 'Pendientes',  campo: 'pendientes' },
  { key: 'confirmada', label: 'Confirmadas', campo: 'confirmadas' },
  { key: 'en curso',   label: 'En Curso',    campo: 'enCurso' },
  { key: 'completada', label: 'Completadas', campo: 'completadas' },
  { key: 'cancelada',  label: 'Canceladas',  campo: 'canceladas' },
  { key: 'expirada',   label: 'Expiradas',   campo: 'expiradas' },
]

const tabs = [
  { key: 'todas',   label: 'Todas',    icon: '<circle cx="12" cy="12" r="10"/>' },
  { key: 'vuelo',   label: 'Vuelos',   icon: '<path fill="currentColor" stroke="none" d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>' },
  { key: 'hotel',   label: 'Hoteles',  icon: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>' },
  { key: 'paquete', label: 'Paquetes', icon: '<rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>' },
]

// ─── COMPUTADOS ────────────────────────────────────────────────────────────────
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

function countPorTab(key) {
  if (key === 'todas') return reservas.value.length
  return reservas.value.filter(r => r._categoria === key).length
}

// ─── HELPERS GENERALES ─────────────────────────────────────────────────────────
function addToast(msg, tipo = 'success') {
  const id = Date.now()
  toasts.value.push({ id, msg, tipo })
  setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 4000)
}

function estadoClase(e) {
  if (!e) return 'mv-badge--pendiente'
  const s = e.toLowerCase()
  if (s === 'confirmada') return 'mv-badge--confirmada'
  if (s === 'cancelada')  return 'mv-badge--cancelada'
  if (s === 'completada') return 'mv-badge--completada'
  if (s === 'expirada')   return 'mv-badge--expirada'
  if (s === 'en curso')   return 'mv-badge--encurso'
  return 'mv-badge--pendiente'
}

function formatFechaHora(f) {
  if (!f) return '--'
  const d = new Date(f)
  return d.toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
    + ' ' + d.toLocaleTimeString('es-GT', { hour:'2-digit', minute:'2-digit' })
}
function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}
function formatHora(h)       { return h ? h.substring(0, 5) : '--' }
function formatDuracion(min) { if (!min) return '--'; return `${Math.floor(min/60)}h${min%60>0?' '+min%60+'m':''}` }
function calcNoches(ci, co)  { if (!ci||!co) return 0; return Math.max(0, Math.ceil((new Date(co)-new Date(ci))/86400000)) }

// ─── HELPERS COMENTARIOS ──────────────────────────────────────────────────────

/**
 * yaComentaRuta — ¿el usuario actual ya calificó esta ruta?
 * Busca en comentariosPanel: comentario raíz (sin padre) con estrellas
 * y que pertenezca al usuario actual.
 */
function yaComentaRuta(rutaId) {
  if (!rutaId) return false
  const uid = getUsuarioIdActual()
  return comentariosPanel.value.some(c =>
    c.rutaId === rutaId &&
    c.comentarioPadreId === null &&
    c.cantidadEstrellas !== null &&
    (uid === null || c.usuarioId === uid)  // si no hay uid, consideramos que no ha calificado
  )
}

/**
 * obtenerComentarioRuta — devuelve el comentario/calificación del usuario
 * para una ruta dada (o null si no existe).
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
 * yaResenaHotel — ¿el usuario actual ya dejó reseña en este hotel?
 * resena !== null identifica comentarios con puntuación (reseña raíz).
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
 * obtenerResenaHotel — devuelve la reseña del usuario para un hotel (o null).
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

// ─── RESUMEN ──────────────────────────────────────────────────────────────────
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
    vuelos:   list.filter(r => r._categoria === 'vuelo').length,
    hoteles:  list.filter(r => r._categoria === 'hotel').length,
    paquetes: list.filter(r => r._categoria === 'paquete').length,
  }
}

// ─── CARGA PRINCIPAL ──────────────────────────────────────────────────────────
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

// ─── CARGA DE COMENTARIOS DEL PANEL ───────────────────────────────────────────
/**
 * cargarComentariosPanel — carga los comentarios/reseñas reales desde el proveedor
 * para la reserva que acaba de abrirse en el panel.
 *
 * Llama:
 *   GET /api/comentarios/vuelo/:proveedorId/:rutaId   → para cada ruta única en boletos
 *   GET /api/comentarios/hotel/:proveedorId/:hotelId  → si hay hotelId
 *
 * Los resultados se acumulan en comentariosPanel.
 */
async function cargarComentariosPanel(reserva) {
  comentariosPanel.value  = []
  comentariosLoading.value = true

  try {
    const promesas = []

    // ── Comentarios de vuelo ─────────────────────────────────────────────────
    if (reserva._categoria === 'vuelo' || reserva._categoria === 'paquete') {
      const provId = reserva.proveedorIdVuelo
      // Rutas únicas con rutaId válido
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

    // ── Comentarios de hotel ─────────────────────────────────────────────────
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

// ─── PANEL LATERAL ────────────────────────────────────────────────────────────
async function abrirPanel(reserva) {
  // Mostrar snapshot inmediatamente (UX sin espera)
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
    // 1. Traer datos reales del proveedor (boletos con rutaId, habitaciones, etc.)
    const data = await apiFetch(`${BASE}/api/reservaciones/mias/${reserva.id}`)
    panelReserva.value = fromDetalle(data)
  } catch {
    panelError.value = 'No se pudieron cargar los detalles del proveedor.'
    panelLoading.value = false
    return
  } finally {
    panelLoading.value = false
  }

  // 2. Cargar comentarios/reseñas reales (async, no bloquea el panel)
  //    Solo si el estado es "completada" (única situación donde se muestran)
  if (panelReserva.value.estadoReserva?.toLowerCase() === 'completada') {
    await cargarComentariosPanel(panelReserva.value)
  }
}

function cerrarPanel() {
  panelReserva.value   = null
  panelError.value     = ''
  comentariosPanel.value = []
  estadoNodosMR.value  = {}
  resetCal()
  resetRes()
}

// ─── CANCELAR ─────────────────────────────────────────────────────────────────
async function confirmarCancelar() {
  if (!cancelMotivo.value.trim()) { cancelError.value = 'Escribe un motivo de cancelación.'; return }
  if (!cancelTerminos.value)      { cancelError.value = 'Debes aceptar los términos de cancelación.'; return }

  cancelLoading.value = true
  cancelError.value   = ''

  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    const res = await fetch(`${BASE}/api/reservaciones/${panelReserva.value.id}/cancelar`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
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

// ─── PDF / CORREO ─────────────────────────────────────────────────────────────
async function descargarPDF(id) {
  pdfLoading.value = true
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    const res = await fetch(`${BASE}/api/reservaciones/${id}/pdf`, {
      headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) },
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

// ─── CALIFICACIÓN VUELO ───────────────────────────────────────────────────────
function resetCal() {
  calEstrellas.value = 0
  calHover.value     = 0
  calContenido.value = ''
  calError.value     = ''
  calExito.value     = false
}

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

    // Recargar comentarios para reflejar el nuevo estado
    await cargarComentariosPanel(panelReserva.value)
    calExito.value = true
    addToast('¡Calificación enviada! Gracias por tu opinión.')

  } catch {
    calError.value = 'Error al enviar la calificación. Intenta de nuevo.'
  } finally {
    calLoading.value = false
  }
}

// ─── RESEÑA HOTEL ─────────────────────────────────────────────────────────────
function resetRes() {
  resEstrellas.value = 0
  resHover.value     = 0
  resContenido.value = ''
  resError.value     = ''
  resenaOk.value     = false
}

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

// ─── INIT ──────────────────────────────────────────────────────────────────────
onMounted(() => cargarTodo())
</script>