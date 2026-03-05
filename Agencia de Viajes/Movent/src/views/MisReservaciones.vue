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

            <div v-if="panelLoading" class="mv-panel__center">
              <div class="mv-spinner mv-spinner--lg"></div>
              <p>Cargando...</p>
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
                  <div v-if="panelReserva.fechaExpiracion" class="mv-panel__icell">
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
                  <div class="mv-panel__icell">
                    <span class="mv-panel__ilbl">Usuario</span>
                    <span class="mv-panel__ival">{{ panelReserva.usuarioNombre }}</span>
                  </div>
                </div>
              </div>

              <!-- BOLETOS (vuelo o paquete) -->
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

              <!-- HABITACIONES (hotel o paquete) -->
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
                  <!-- Desglose -->
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

              <!-- Calificar vuelo -->
              <div v-if="(panelReserva._categoria==='vuelo' || panelReserva._categoria==='paquete') && panelReserva.estadoReserva?.toLowerCase()==='completada' && panelReserva.boletos?.[0]" class="mv-panel__section">
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                  Calificar vuelo {{ panelReserva.boletos[0].origenCodigo }} → {{ panelReserva.boletos[0].destinoCodigo }}
                </h4>
                <div v-if="yaComentaRuta(panelReserva.boletos[0].rutaId) || calExito" class="mv-ya-califico">
                  <div class="mv-ya-califico__stars">
                    <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
                      :fill="n<=(obtenerComentarioRuta(panelReserva.boletos[0].rutaId)?.cantidadEstrellas??5)?'#FFCC00':'none'"
                      :stroke="n<=(obtenerComentarioRuta(panelReserva.boletos[0].rutaId)?.cantidadEstrellas??5)?'#FFCC00':'#ccc'"
                      stroke-width="2" width="18" height="18"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                  </div>
                  <p v-if="obtenerComentarioRuta(panelReserva.boletos[0].rutaId)" class="mv-ya-califico__texto">{{ obtenerComentarioRuta(panelReserva.boletos[0].rutaId)?.contenido }}</p>
                  <span class="mv-ya-califico__badge">✓ Ya calificaste este vuelo</span>
                </div>
                <div v-else class="mv-calificar">
                  <div class="mv-calificar__stars">
                    <button v-for="n in 5" :key="n" type="button" class="mv-calificar__star"
                      @mouseenter="calHover=n" @mouseleave="calHover=0" @click="calEstrellas=n">
                      <svg viewBox="0 0 24 24" :fill="n<=(calHover||calEstrellas)?'#FFCC00':'none'" :stroke="n<=(calHover||calEstrellas)?'#FFCC00':'#ccc'" stroke-width="2" width="26" height="26"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                    </button>
                    <span class="mv-calificar__lbl">{{ calEstrellas > 0 ? `${calEstrellas}/5` : 'Selecciona' }}</span>
                  </div>
                  <textarea class="mv-calificar__textarea" v-model="calContenido" placeholder="Cuéntanos tu experiencia en este vuelo..." rows="3"></textarea>
                  <p v-if="calError" class="mv-form-error">{{ calError }}</p>
                  <button class="mv-btn mv-btn--primary" @click="enviarCalificacion(panelReserva.boletos[0].rutaId)" :disabled="calLoading" type="button">
                    <span v-if="calLoading" class="mv-btn__spin mv-btn__spin--light"></span>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                    {{ calLoading ? 'Enviando...' : 'Enviar calificación' }}
                  </button>
                </div>
              </div>

              <!-- Reseña hotel -->
              <div v-if="(panelReserva._categoria==='hotel' || panelReserva._categoria==='paquete') && panelReserva.estadoReserva?.toLowerCase()==='completada'" class="mv-panel__section">
                <h4 class="mv-panel__stitle">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  Reseña · {{ panelReserva.nombreHotel }}
                </h4>
                <div v-if="resenaOk || hotelesConResena.has(panelReserva.hotelId)" class="mv-ya-califico">
                  <span class="mv-ya-califico__badge">✓ Ya dejaste una reseña para este hospedaje</span>
                </div>
                <div v-else class="mv-calificar">
                  <div class="mv-calificar__stars">
                    <button v-for="n in 5" :key="n" type="button" class="mv-calificar__star"
                      @mouseenter="resHover=n" @mouseleave="resHover=0" @click="resEstrellas=n">
                      <svg viewBox="0 0 24 24" :fill="n<=(resHover||resEstrellas)?'#FFCC00':'none'" :stroke="n<=(resHover||resEstrellas)?'#FFCC00':'#ccc'" stroke-width="2" width="26" height="26"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                    </button>
                    <span class="mv-calificar__lbl">{{ resEstrellas > 0 ? `${resEstrellas}/5` : 'Selecciona' }}</span>
                  </div>
                  <textarea class="mv-calificar__textarea" v-model="resContenido" placeholder="¿Cómo fue tu estadía?" rows="3"></textarea>
                  <p v-if="resError" class="mv-form-error">{{ resError }}</p>
                  <button class="mv-btn mv-btn--primary" @click="enviarResenaHotel" :disabled="resLoading" type="button">
                    <span v-if="resLoading" class="mv-btn__spin mv-btn__spin--light"></span>
                    {{ resLoading ? 'Enviando...' : 'Enviar reseña' }}
                  </button>
                </div>
              </div>

              <!-- Cancelar — RAV17: confirmada o pendiente (no expirada/completada/cancelada) -->
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
                      <p class="mv-cancelar__sub">Se verificarán las reglas del proveedor. Puede aplicar penalización.</p>
                    </div>
                  </div>

                  <!-- Penalización simulada -->
                  <div style="background:#fff8f0;border:1px solid #fed7aa;border-radius:8px;padding:0.75rem 1rem;font-size:0.82rem;display:flex;flex-direction:column;gap:0.3rem;">
                    <span style="font-weight:700;color:#c2410c;">⚠ Política de cancelación del proveedor</span>
                    <template v-if="panelReserva.estadoReserva?.toLowerCase() === 'pendiente'">
                      <span style="color:#15803d;font-weight:600;">Sin penalización — reserva aún no confirmada por el proveedor.</span>
                      <span style="color:#9a7060;">Monto a reembolsar: <strong>${{ calcReembolso(panelReserva) }}</strong> (reembolso completo)</span>
                    </template>
                    <template v-else>
                      <span style="color:#7c4b1e;">Penalización estimada: <strong>${{ calcPenalizacion(panelReserva) }}</strong> (15% del total)</span>
                      <span style="color:#9a7060;">Monto a reembolsar: <strong>${{ calcReembolso(panelReserva) }}</strong></span>
                    </template>
                  </div>

                  <textarea class="mv-cancelar__textarea" v-model="cancelMotivo" placeholder="Motivo de cancelación (requerido)..." rows="2"></textarea>

                  <!-- Checkbox aceptar términos -->
                  <label style="display:flex;align-items:flex-start;gap:0.6rem;cursor:pointer;font-size:0.82rem;color:#4a4035;">
                    <input type="checkbox" v-model="cancelTerminos" style="margin-top:2px;accent-color:#D40511;" />
                    <span>Acepto los términos de cancelación y la penalización indicada por el proveedor.</span>
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
                <button class="mv-btn mv-btn--outline" @click="descargarPDF(panelReserva.reservacionId)" :disabled="pdfLoading" type="button">
                  <span v-if="pdfLoading" class="mv-btn__spin mv-btn__spin--dark"></span>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                  {{ pdfLoading ? 'Descargando...' : 'Descargar PDF' }}
                </button>
                <button class="mv-btn mv-btn--secondary" @click="enviarCorreo(panelReserva.reservacionId)" :disabled="correoLoading" type="button">
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

    <!-- PÁGINA PRINCIPAL -->
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

          <!-- Hero financiero -->
          <div class="mv-resumen__hero">
            <div class="mv-resumen__hero-left">
              <p class="mv-resumen__hero-lbl">Total invertido en viajes</p>
              <p class="mv-resumen__hero-monto">${{ resumen.totalGastado?.toFixed(2) ?? '0.00' }}</p>
              <p class="mv-resumen__hero-sub">{{ resumen.totalReservaciones ?? 0 }} reservaciones en total</p>
            </div>
            <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,204,0,0.25)" stroke-width="0.8" width="100" height="100" class="mv-resumen__hero-deco"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
          </div>

          <!-- Estados -->
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

          <!-- Categorías -->
          <div class="mv-resumen__cats">
            <div class="mv-cat mv-cat--vuelo">
              <div class="mv-cat__icon-wrap">
                <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
              </div>
              <div class="mv-cat__body">
                <span class="mv-cat__num">{{ resumen.vuelos ?? countCategoria('vuelo') }}</span>
                <span class="mv-cat__label">Vuelos</span>
                <span class="mv-cat__sub">Boletos y rutas aéreas</span>
              </div>
            </div>
            <div class="mv-cat mv-cat--hotel">
              <div class="mv-cat__icon-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              </div>
              <div class="mv-cat__body">
                <span class="mv-cat__num">{{ resumen.hoteles ?? countCategoria('hotel') }}</span>
                <span class="mv-cat__label">Hospedajes</span>
                <span class="mv-cat__sub">Doble, Suite y más</span>
              </div>
            </div>
            <div class="mv-cat mv-cat--paquete">
              <div class="mv-cat__icon-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              </div>
              <div class="mv-cat__body">
                <span class="mv-cat__num">{{ resumen.paquetes ?? countCategoria('paquete') }}</span>
                <span class="mv-cat__label">Paquetes</span>
                <span class="mv-cat__sub">Vuelo + hospedaje</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Controles -->
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

        <!-- Tabs categoría -->
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

        <!-- Lista de cards -->
        <div v-else class="mv-lista">
          <article v-for="r in reservasFiltradas" :key="r.reservacionId"
            class="mv-card" :class="`mv-card--${r._categoria}`"
            @click="abrirPanel(r)" tabindex="0" @keydown.enter="abrirPanel(r)">

            <!-- Franja superior con categoría -->
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

              <!-- Vista vuelo -->
              <template v-if="(r._categoria==='vuelo' || r._categoria==='paquete') && r.boletos?.[0]">
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
                  <span>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    {{ formatFecha(r.boletos[0].fechaVuelo) }}
                  </span>
                  <span>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    {{ formatHora(r.boletos[0].horaSalida) }} → {{ formatHora(r.boletos[0].horaLlegada) }}
                  </span>
                  <span>{{ r.boletos.length }} boleto{{ r.boletos.length!==1?'s':'' }}</span>
                </div>
              </template>

              <!-- Vista hotel -->
              <template v-if="(r._categoria==='hotel' || r._categoria==='paquete') && r.habitaciones?.[0]">
                <div class="mv-card__hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  <div>
                    <span class="mv-card__hotel-nombre">{{ r.nombreHotel }}</span>
                    <span class="mv-card__hotel-hab">{{ r.habitaciones.map(h=>h.tipoHabitacion).join(' · ') }}</span>
                  </div>
                </div>
                <div class="mv-card__meta">
                  <span>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    {{ formatFecha(r.habitaciones[0].fechaCheckIn) }} → {{ formatFecha(r.habitaciones[0].fechaCheckOut) }}
                  </span>
                  <span>{{ calcNoches(r.habitaciones[0].fechaCheckIn, r.habitaciones[0].fechaCheckOut) }} noches</span>
                  <span>{{ r.habitaciones.reduce((s,h)=>s+(h.cantidadPersonas??0),0) }} huéspedes</span>
                </div>
              </template>

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

const router = useRouter()
const API = 'http://localhost:7000'

const reservas         = ref([])
const resumen          = ref(null)
const loading          = ref(true)
const error            = ref('')
const filtroActivo     = ref('todas')
const tabActivo        = ref('todas')
const busqueda         = ref('')
const misComentarios   = ref([])
const hotelesConResena = ref(new Set())
const toasts           = ref([])

const panelReserva  = ref(null)
const panelLoading  = ref(false)
const panelError    = ref('')

const cancelAbierto = ref(false)
const cancelMotivo  = ref('')
const cancelLoading = ref(false)
const cancelError   = ref('')
const cancelTerminos = ref(false)

const calEstrellas  = ref(0)
const calHover      = ref(0)
const calContenido  = ref('')
const calLoading    = ref(false)
const calError      = ref('')
const calExito      = ref('')

const resEstrellas  = ref(0)
const resHover      = ref(0)
const resContenido  = ref('')
const resLoading    = ref(false)
const resError      = ref('')
const resenaOk      = ref(false)

const pdfLoading    = ref(false)
const correoLoading = ref(false)

const filtros = [
  { key: 'todas',      label: 'Todas',       campo: 'totalReservaciones' },
  { key: 'confirmada', label: 'Confirmadas',  campo: 'confirmadas' },
  { key: 'pendiente',  label: 'Pendientes',   campo: 'pendientes' },
  { key: 'completada', label: 'Completadas',  campo: 'completadas' },
  { key: 'cancelada',  label: 'Canceladas',   campo: 'canceladas' },
  { key: 'expirada',   label: 'Expiradas',    campo: 'expiradas' },
]

const tabs = [
  { key: 'todas',   label: 'Todas',    icon: '<circle cx="12" cy="12" r="10"/>' },
  { key: 'vuelo',   label: 'Vuelos',   icon: '<path fill="currentColor" stroke="none" d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>' },
  { key: 'hotel',   label: 'Hoteles',  icon: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>' },
  { key: 'paquete', label: 'Paquetes', icon: '<rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/>' },
]

const reservasFiltradas = computed(() => {
  let list = reservas.value
  if (filtroActivo.value !== 'todas') list = list.filter(r => r.estadoReserva?.toLowerCase() === filtroActivo.value)
  if (tabActivo.value !== 'todas') list = list.filter(r => r._categoria === tabActivo.value)
  if (busqueda.value.trim()) list = list.filter(r => (r.noReservacion||'').toLowerCase().includes(busqueda.value.toLowerCase()))
  return list
})

function countPorTab(key) {
  if (key === 'todas') return reservas.value.length
  return reservas.value.filter(r => r._categoria === key).length
}
function countCategoria(cat) { return reservas.value.filter(r => r._categoria === cat).length }

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
  return 'mv-badge--pendiente'
}

function formatFechaHora(f) {
  if (!f) return '--'
  const d = new Date(f)
  return d.toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' }) + ' ' +
         d.toLocaleTimeString('es-GT', { hour:'2-digit', minute:'2-digit' })
}
function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}
function formatHora(h) { return h ? h.substring(0,5) : '--' }
function formatDuracion(min) {
  if (!min) return '--'
  return `${Math.floor(min/60)}h${min%60>0?' '+min%60+'m':''}`
}
function calcNoches(ci, co) {
  if (!ci || !co) return 0
  return Math.max(0, Math.ceil((new Date(co)-new Date(ci))/86400000))
}
function detectarCategoria(r) {
  const v = (r.boletos?.length ?? 0) > 0
  const h = (r.habitaciones?.length ?? 0) > 0
  if (v && h) return 'paquete'
  if (v) return 'vuelo'
  if (h) return 'hotel'
  return 'vuelo'
}
function yaComentaRuta(rutaId) {
  return misComentarios.value.some(c => c.rutaId === rutaId && c.comentarioPadreId === null && c.cantidadEstrellas !== null)
}
function obtenerComentarioRuta(rutaId) {
  return misComentarios.value.find(c => c.rutaId === rutaId && c.comentarioPadreId === null && c.cantidadEstrellas !== null)
}

function calcPenalizacion(r) {
  if (r.estadoReserva?.toLowerCase() === 'pendiente') return '0.00'
  return ((r.total ?? 0) * 0.15).toFixed(2)
}
function calcReembolso(r) {
  if (r.estadoReserva?.toLowerCase() === 'pendiente') return (r.total ?? 0).toFixed(2)
  return ((r.total ?? 0) * 0.85).toFixed(2)
}

// ── DEMO DATA ────────────────────────────────────────────────
const RESERVAS_DEMO = [
  {
    reservacionId: 1, noReservacion: 'MV-2026-04821', estadoReserva: 'confirmada',
    total: 320.00, fechaCreacion: new Date(Date.now() - 2*86400000).toISOString(),
    usuarioNombre: 'Carlos Méndez',
    boletos: [{
      noBoleto: 'BL-001', numeroVuelo: 'MV-101', avionMarca: 'Boeing', avionModelo: '737',
      estadoBoleto: 'confirmado', origenCodigo: 'GUA', origenCiudad: 'Guatemala City',
      destinoCodigo: 'MIA', destinoCiudad: 'Miami', horaSalida: '08:30:00', horaLlegada: '11:45:00',
      duracionMinutos: 195, fechaVuelo: '2026-03-15', noAsiento: '14A', clase: 'Económica',
      precio: 320.00, rutaId: 1,
      pasajero: { nombre: 'Carlos', apellido: 'Méndez', pasaporte: 'GT123456', ciudad: 'Guatemala', pais: 'Guatemala' }
    }],
    habitaciones: []
  },
  {
    reservacionId: 2, noReservacion: 'MV-2026-04820', estadoReserva: 'confirmada',
    total: 850.00, fechaCreacion: new Date(Date.now() - 5*86400000).toISOString(),
    usuarioNombre: 'Carlos Méndez',
    boletos: [],
    habitaciones: [{
      detalleId: 1, tipoHabitacion: 'Suite Deluxe', tipoCama: 'King', descripcionHabitacion: 'Suite con vista al jardín, jacuzzi y balcón privado.',
      totalDetalle: 850.00, fechaCheckIn: '2026-03-20', fechaCheckOut: '2026-03-25', cantidadPersonas: 2, hotelId: 1
    }],
    nombreHotel: 'Casa Santo Domingo', hotelId: 1
  },
  {
    reservacionId: 3, noReservacion: 'MV-2026-04819', estadoReserva: 'completada',
    total: 1999.00, fechaCreacion: new Date(Date.now() - 30*86400000).toISOString(),
    usuarioNombre: 'Carlos Méndez',
    boletos: [{
      noBoleto: 'BL-002', numeroVuelo: 'MV-101', avionMarca: 'Airbus', avionModelo: 'A320',
      estadoBoleto: 'usado', origenCodigo: 'GUA', origenCiudad: 'Guatemala City',
      destinoCodigo: 'CUN', destinoCiudad: 'Cancún', horaSalida: '09:00:00', horaLlegada: '11:30:00',
      duracionMinutos: 150, fechaVuelo: '2026-02-01', noAsiento: '8C', clase: 'Económica',
      precio: 320.00, rutaId: 2,
      pasajero: { nombre: 'Carlos', apellido: 'Méndez', pasaporte: 'GT123456', ciudad: 'Guatemala', pais: 'Guatemala' }
    }],
    habitaciones: [{
      detalleId: 2, tipoHabitacion: 'Ocean View', tipoCama: 'Queen', descripcionHabitacion: 'Habitación con vista al mar y acceso a alberca.',
      totalDetalle: 1540.00, fechaCheckIn: '2026-02-01', fechaCheckOut: '2026-02-08', cantidadPersonas: 2, hotelId: 2
    }],
    nombreHotel: 'Hotel Cancún Palace', hotelId: 2
  },
  {
    reservacionId: 4, noReservacion: 'MV-2026-04818', estadoReserva: 'pendiente',
    total: 210.00, fechaCreacion: new Date(Date.now() - 1*86400000).toISOString(),
    fechaExpiracion: new Date(Date.now() + 2*86400000).toISOString(),
    usuarioNombre: 'Carlos Méndez',
    boletos: [{
      noBoleto: 'BL-003', numeroVuelo: 'AV-309', avionMarca: 'Airbus', avionModelo: 'A319',
      estadoBoleto: 'pendiente', origenCodigo: 'GUA', origenCiudad: 'Guatemala City',
      destinoCodigo: 'PTY', destinoCiudad: 'Ciudad de Panamá', horaSalida: '14:20:00', horaLlegada: '16:00:00',
      duracionMinutos: 100, fechaVuelo: '2026-04-10', noAsiento: '22B', clase: 'Económica',
      precio: 210.00, rutaId: 3,
      pasajero: { nombre: 'Carlos', apellido: 'Méndez', pasaporte: 'GT123456', ciudad: 'Guatemala', pais: 'Guatemala' }
    }],
    habitaciones: []
  },
  {
    reservacionId: 5, noReservacion: 'MV-2026-04815', estadoReserva: 'cancelada',
    total: 185.00, fechaCreacion: new Date(Date.now() - 10*86400000).toISOString(),
    fechaCancelacion: new Date(Date.now() - 8*86400000).toISOString(),
    motivoCancelacion: 'Cambio de planes de viaje.',
    usuarioNombre: 'Carlos Méndez',
    boletos: [],
    habitaciones: [{
      detalleId: 3, tipoHabitacion: 'Superior King', tipoCama: 'King', descripcionHabitacion: 'Habitación amplia con escritorio ejecutivo.',
      totalDetalle: 185.00, fechaCheckIn: '2026-02-20', fechaCheckOut: '2026-02-22', cantidadPersonas: 1, hotelId: 3
    }],
    nombreHotel: 'Barceló Guatemala', hotelId: 3
  },
]

const RESUMEN_DEMO = {
  totalGastado: 3179.00, totalReservaciones: 5,
  confirmadas: 2, pendientes: 1, completadas: 1, canceladas: 1, expiradas: 0,
  vuelos: 3, hoteles: 1, paquetes: 1,
}

const COMENTARIOS_DEMO = [
  { rutaId: 2, comentarioPadreId: null, cantidadEstrellas: 5, contenido: '¡Excelente vuelo! Muy puntual y cómodo.', resena: null, hotelId: null },
]
// ─────────────────────────────────────────────────────────────

onMounted(() => cargarTodo())

async function cargarTodo() {
  loading.value = true; error.value = ''
  await new Promise(r => setTimeout(r, 500))
  reservas.value = RESERVAS_DEMO.map(x => ({ ...x, _categoria: detectarCategoria(x) }))
  resumen.value  = RESUMEN_DEMO
  misComentarios.value = COMENTARIOS_DEMO
  hotelesConResena.value = new Set(COMENTARIOS_DEMO.filter(c => c.resena != null).map(c => c.hotelId))
  loading.value = false
}

async function cargarReservas() {
  reservas.value = RESERVAS_DEMO.map(x => ({ ...x, _categoria: detectarCategoria(x) }))
}

async function cargarResumen() {
  resumen.value = { ...RESUMEN_DEMO }
}

async function cargarComentarios() {
  misComentarios.value = [...COMENTARIOS_DEMO]
}

async function abrirPanel(reserva) {
  panelReserva.value = { ...reserva }
  panelLoading.value = true
  panelError.value = ''
  cancelAbierto.value = false; cancelMotivo.value = ''; cancelError.value = ''
  resetCal(); resetRes()
  await new Promise(r => setTimeout(r, 300))
  // En demo usamos los datos que ya tenemos en memoria
  const completa = RESERVAS_DEMO.find(r => r.reservacionId === reserva.reservacionId)
  if (completa) panelReserva.value = { ...completa, _categoria: detectarCategoria(completa) }
  panelLoading.value = false
}

function cerrarPanel() { panelReserva.value = null; panelError.value = ''; resetCal(); resetRes() }

async function confirmarCancelar() {
  if (!cancelMotivo.value.trim()) { cancelError.value = 'Escribe un motivo.'; return }
  if (!cancelTerminos.value) { cancelError.value = 'Debes aceptar los términos de cancelación.'; return }
  cancelLoading.value = true; cancelError.value = ''
  await new Promise(r => setTimeout(r, 1200))
  const idx = RESERVAS_DEMO.findIndex(r => r.reservacionId === panelReserva.value.reservacionId)
  if (idx !== -1) {
    RESERVAS_DEMO[idx].estadoReserva = 'cancelada'
    RESERVAS_DEMO[idx].fechaCancelacion = new Date().toISOString()
    RESERVAS_DEMO[idx].motivoCancelacion = cancelMotivo.value.trim()
  }
  addToast('Reservación cancelada · Confirmación enviada al correo')
  cancelAbierto.value = false
  cancelTerminos.value = false
  await cargarReservas()
  await cargarResumen()
  const completa = RESERVAS_DEMO.find(r => r.reservacionId === panelReserva.value.reservacionId)
  if (completa) panelReserva.value = { ...completa, _categoria: detectarCategoria(completa) }
  cancelLoading.value = false
}

async function descargarPDF(id) {
  pdfLoading.value = true
  await new Promise(r => setTimeout(r, 800))
  addToast('PDF descargado (demo)')
  pdfLoading.value = false
}

async function enviarCorreo(id) {
  correoLoading.value = true
  await new Promise(r => setTimeout(r, 600))
  addToast('Enviado al correo (demo)')
  correoLoading.value = false
}

function resetCal() { calEstrellas.value=0; calHover.value=0; calContenido.value=''; calError.value=''; calExito.value='' }
function resetRes()  { resEstrellas.value=0; resHover.value=0; resContenido.value=''; resError.value=''; resenaOk.value=false }

async function enviarCalificacion(rutaId) {
  if (calEstrellas.value < 1) { calError.value = 'Selecciona estrellas.'; return }
  if (!calContenido.value.trim()) { calError.value = 'Escribe tu comentario.'; return }
  calLoading.value = true; calError.value = ''
  await new Promise(r => setTimeout(r, 600))
  COMENTARIOS_DEMO.push({ rutaId, comentarioPadreId: null, cantidadEstrellas: calEstrellas.value, contenido: calContenido.value.trim(), resena: null, hotelId: null })
  misComentarios.value = [...COMENTARIOS_DEMO]
  calExito.value = '¡Calificación enviada!'
  addToast('Calificación enviada')
  calLoading.value = false
}

async function enviarResenaHotel() {
  const hotelId = panelReserva.value.hotelId ?? panelReserva.value.habitaciones?.[0]?.hotelId
  if (!hotelId) { resError.value = 'No se encontró el hotel.'; return }
  if (resEstrellas.value < 1) { resError.value = 'Selecciona estrellas.'; return }
  if (!resContenido.value.trim()) { resError.value = 'Escribe tu reseña.'; return }
  resLoading.value = true; resError.value = ''
  await new Promise(r => setTimeout(r, 600))
  resenaOk.value = true
  hotelesConResena.value = new Set([...hotelesConResena.value, hotelId])
  addToast('Reseña enviada')
  resLoading.value = false
}
</script>