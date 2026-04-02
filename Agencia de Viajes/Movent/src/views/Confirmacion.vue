<template>
  <div class="page">
    <Encabezado />
    <div class="conf-page">
      <div class="conf-container">

        <!-- ═══ STEPS ═══ -->
        <div class="conf-steps-bar">
          <div class="conf-step conf-step--done">
            <div class="conf-step__num">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span class="conf-step__lbl">Datos</span>
          </div>
          <div class="conf-step__connector conf-step__connector--done"></div>
          <div class="conf-step conf-step--done">
            <div class="conf-step__num">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span class="conf-step__lbl">Pago</span>
          </div>
          <div class="conf-step__connector conf-step__connector--done"></div>
          <div class="conf-step conf-step--active">
            <div class="conf-step__num">3</div>
            <span class="conf-step__lbl">Confirmación</span>
          </div>
        </div>

        <!-- ═══ HERO ═══ -->
        <div class="conf-hero">
          <div class="conf-hero__check">
            <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3" width="32" height="32"><polyline points="20 6 9 17 4 12"/></svg>
          </div>
          <h1 class="conf-hero__title">¡Reserva confirmada!</h1>
          <p class="conf-hero__sub">Tu compra fue procesada exitosamente. Puedes descargar tu comprobante a continuación.</p>
          <div class="conf-hero__no">
            <span class="conf-hero__no-lbl">No. de reserva</span>
            <span class="conf-hero__no-val">{{ noReservacion || '—' }}</span>
          </div>
        </div>

        <!-- ═══ CONTENIDO ═══ -->
        <div class="conf-grid">

          <div class="conf-main">

            <!-- ── TARJETA DE DETALLES ── -->
            <div class="conf-card">
              <div class="conf-card__head">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Detalles de tu reserva
              </div>
              <div class="conf-card__body">

                <!-- ══ VUELO SOLO IDA ══ -->
                <template v-if="tipoItem === 'vuelo' && itemData?.tipoVuelo === 'ida'">
                  <div class="conf-ruta-wrap">
                    <div class="conf-ruta">
                      <div class="conf-ruta__punto">
                        <span class="conf-ruta__iata">{{ itemData.origenCodigo }}</span>
                        <span class="conf-ruta__ciudad">{{ itemData.origenCiudad }}</span>
                        <span class="conf-ruta__hora">{{ itemData.horaSalida }}</span>
                      </div>
                      <div class="conf-ruta__track">
                        <div class="conf-ruta__line"></div>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <div class="conf-ruta__line"></div>
                      </div>
                      <div class="conf-ruta__punto conf-ruta__punto--r">
                        <span class="conf-ruta__iata">{{ itemData.destinoCodigo }}</span>
                        <span class="conf-ruta__ciudad">{{ itemData.destinoCiudad }}</span>
                        <span class="conf-ruta__hora">{{ itemData.horaLlegada }}</span>
                      </div>
                    </div>
                    <div class="conf-detalles-row">
                      <div class="conf-detalle"><span>Aerolínea</span><strong>{{ itemData.aerolinea }}</strong></div>
                      <div class="conf-detalle"><span>Vuelo</span><strong>{{ itemData.numeroVuelo }}</strong></div>
                      <div class="conf-detalle"><span>Clase</span><strong style="text-transform:capitalize">{{ itemData.clase }}</strong></div>
                      <div class="conf-detalle"><span>Escalas</span><strong>{{ itemData.escalas === 0 ? 'Directo' : itemData.escalas + ' escala(s)' }}</strong></div>
                    </div>
                  </div>
                  <!-- Boletos -->
                  <div v-if="boletos.length" class="conf-boletos">
                    <div class="conf-boletos__titulo">Boletos</div>
                    <div v-for="b in boletos" :key="b.boletoId" class="conf-boleto">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                      <span class="conf-boleto__no">{{ b.noBoleto }}</span>
                      <span class="conf-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                    </div>
                  </div>
                </template>

                <!-- ══ VUELO IDA Y VUELTA ══ -->
                <template v-else-if="tipoItem === 'vuelo' && itemData?.tipoVuelo === 'idaVuelta'">
                  <div class="conf-ruta-wrap">
                    <!-- Ida -->
                    <div class="conf-tramo">
                      <div class="conf-tramo__badge">Ida</div>
                      <div class="conf-ruta">
                        <div class="conf-ruta__punto">
                          <span class="conf-ruta__iata">{{ itemData.ida?.origenCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.ida?.origenCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.ida?.horaSalida }}</span>
                        </div>
                        <div class="conf-ruta__track">
                          <div class="conf-ruta__line"></div>
                          <svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                          <div class="conf-ruta__line"></div>
                        </div>
                        <div class="conf-ruta__punto conf-ruta__punto--r">
                          <span class="conf-ruta__iata">{{ itemData.ida?.destinoCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.ida?.destinoCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.ida?.horaLlegada }}</span>
                        </div>
                      </div>
                      <div class="conf-detalles-row conf-detalles-row--sm">
                        <div class="conf-detalle"><span>Aerolínea</span><strong>{{ itemData.ida?.aerolinea }}</strong></div>
                        <div class="conf-detalle"><span>Vuelo</span><strong>{{ itemData.ida?.numeroVuelo }}</strong></div>
                        <div class="conf-detalle"><span>Clase</span><strong style="text-transform:capitalize">{{ itemData.ida?.clase }}</strong></div>
                      </div>
                    </div>
                    <!-- Regreso -->
                    <div class="conf-tramo conf-tramo--regreso">
                      <div class="conf-tramo__badge conf-tramo__badge--reg">Regreso</div>
                      <div class="conf-ruta">
                        <div class="conf-ruta__punto">
                          <span class="conf-ruta__iata">{{ itemData.regreso?.origenCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.regreso?.origenCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.regreso?.horaSalida }}</span>
                        </div>
                        <div class="conf-ruta__track">
                          <div class="conf-ruta__line"></div>
                          <svg viewBox="0 0 24 24" fill="#9a9089" width="18" height="18" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                          <div class="conf-ruta__line"></div>
                        </div>
                        <div class="conf-ruta__punto conf-ruta__punto--r">
                          <span class="conf-ruta__iata">{{ itemData.regreso?.destinoCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.regreso?.destinoCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.regreso?.horaLlegada }}</span>
                        </div>
                      </div>
                      <div class="conf-detalles-row conf-detalles-row--sm">
                        <div class="conf-detalle"><span>Aerolínea</span><strong>{{ itemData.regreso?.aerolinea }}</strong></div>
                        <div class="conf-detalle"><span>Vuelo</span><strong>{{ itemData.regreso?.numeroVuelo }}</strong></div>
                        <div class="conf-detalle"><span>Clase</span><strong style="text-transform:capitalize">{{ itemData.regreso?.clase }}</strong></div>
                      </div>
                    </div>
                  </div>
                  <!-- Boletos -->
                  <div v-if="boletos.length" class="conf-boletos">
                    <div class="conf-boletos__titulo">Boletos</div>
                    <div v-for="b in boletos" :key="b.boletoId" class="conf-boleto">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                      <span class="conf-boleto__no">{{ b.noBoleto }}</span>
                      <span class="conf-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}<template v-if="b.numeroVuelo"> · Vuelo {{ b.numeroVuelo }}</template></span>
                    </div>
                  </div>
                </template>

                <!-- ══ HOTEL ══ -->
                <template v-else-if="tipoItem === 'hotel' && itemData">
                  <div class="conf-hotel-wrap">
                    <div class="conf-hotel__top">
                      <h3 class="conf-hotel__nombre">{{ itemData.nombreHotel }}</h3>
                    </div>
                    <p class="conf-hotel__ubicacion">{{ itemData.hotelCiudad || itemData.busqueda?.ciudad }}</p>
                    <div class="conf-detalles-row">
                      <div class="conf-detalle"><span>Habitación</span><strong>{{ itemData.tipoHabitacion || itemData.tipo }}</strong></div>
                      <div v-if="itemData.tipoCama" class="conf-detalle"><span>Cama</span><strong>{{ itemData.tipoCama }}</strong></div>
                      <div class="conf-detalle"><span>Check-in</span><strong>{{ itemData.busqueda?.checkIn }}</strong></div>
                      <div class="conf-detalle"><span>Check-out</span><strong>{{ itemData.busqueda?.checkOut }}</strong></div>
                      <div class="conf-detalle"><span>Noches</span><strong>{{ itemData.noches }}</strong></div>
                      <div class="conf-detalle"><span>Personas</span><strong>{{ itemData.busqueda?.cantidadPersonas }}</strong></div>
                    </div>
                  </div>
                </template>

                <!-- ══ PAQUETE ══ -->
                <template v-else-if="tipoItem === 'paquete' && itemData">
                  <div class="conf-paquete-wrap">
                    <!-- Vuelo -->
                    <div class="conf-paquete__seccion">
                      <div class="conf-paquete__lbl">
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="12" height="12"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        Vuelo incluido
                      </div>
                      <div class="conf-ruta conf-ruta--sm">
                        <span class="conf-ruta__iata conf-ruta__iata--sm">{{ itemData.vuelo?.origenCodigo }}</span>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <span class="conf-ruta__iata conf-ruta__iata--sm">{{ itemData.vuelo?.destinoCodigo }}</span>
                      </div>
                      <p class="conf-paquete__val">{{ itemData.vuelo?.aerolinea }} · Vuelo {{ itemData.vuelo?.numeroVuelo }} · <span style="text-transform:capitalize">{{ itemData.vuelo?.clase }}</span></p>
                    </div>
                    <!-- Hotel -->
                    <div class="conf-paquete__seccion">
                      <div class="conf-paquete__lbl">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="12" height="12"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                        Hotel incluido
                      </div>
                      <p class="conf-paquete__val conf-paquete__val--nombre">{{ itemData.hotel?.nombreHotel }}</p>
                      <p class="conf-paquete__val">{{ itemData.hotel?.ciudad }} · {{ itemData.noches }} noches · {{ itemData.hotel?.tipoHabitacion }}</p>
                    </div>
                    <!-- Boletos -->
                    <div v-if="boletos.length" class="conf-boletos">
                      <div class="conf-boletos__titulo">Boletos</div>
                      <div v-for="b in boletos" :key="b.boletoId" class="conf-boleto">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                        <span class="conf-boleto__no">{{ b.noBoleto }}</span>
                        <span class="conf-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                      </div>
                    </div>
                  </div>
                </template>

              </div>
            </div><!-- /conf-card -->

            <!-- ── Acciones ── -->
            <div class="conf-actions">
              <button class="conf-btn conf-btn--pdf" @click="descargarPDF" :disabled="descargando" type="button">
                <span v-if="descargando" class="conf-btn__spin"></span>
                <template v-else>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>
                  Descargar comprobante PDF
                </template>
              </button>
            </div>

            <p v-if="pdfError" class="conf-toast conf-toast--err">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ pdfError }}
            </p>

            <!-- ── Recomendaciones ── -->
            <div class="conf-recomendaciones">
              <h2 class="conf-recom__title">Destinos que te podrían interesar</h2>
              <p class="conf-recom__sub">Vuelos, hoteles y paquetes combinados desde Guatemala</p>
              <div class="conf-recom-grid">
                <div class="conf-recom-card" v-for="r in recomendaciones" :key="r.id">
                  <div class="conf-recom-card__img" :style="{ background: r.gradient }">
                    <div class="conf-recom-card__overlay"></div>
                    <div class="conf-recom-card__tipo">
                      <svg v-if="r.tipo==='vuelo'" viewBox="0 0 24 24" fill="currentColor" width="11" height="11"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <svg v-else-if="r.tipo==='hotel'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      <svg v-else viewBox="0 0 24 24" fill="currentColor" width="11" height="11"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                      {{ r.tipo === 'vuelo' ? 'Vuelo' : r.tipo === 'hotel' ? 'Hotel' : 'Paquete' }}
                    </div>
                    <div class="conf-recom-card__ruta">
                      <span>{{ r.origen }}</span>
                      <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <span>{{ r.destino }}</span>
                    </div>
                  </div>
                  <div class="conf-recom-card__body">
                    <p class="conf-recom-card__nombre">{{ r.nombre }}</p>
                    <p class="conf-recom-card__desc">{{ r.desc }}</p>
                    <div class="conf-recom-card__footer">
                      <span class="conf-recom-card__precio">{{ r.precio }}</span>
                      <button class="conf-recom-card__btn" @click="$router.push(r.ruta)" type="button">Ver más</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div><!-- /conf-main -->

          <!-- ═══ SIDEBAR ═══ -->
          <aside class="conf-sidebar">
            <div class="conf-resumen">
              <div class="conf-resumen__head">Tu reserva</div>

              <div class="conf-resumen__check-row">
                <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5" width="18" height="18"><polyline points="20 6 9 17 4 12"/></svg>
                <span>Pago procesado exitosamente</span>
              </div>

              <div class="conf-resumen__body">
                <div class="conf-resumen__row">
                  <span>No. reserva</span>
                  <strong style="font-family:'Space Mono',monospace;font-size:11px">{{ noReservacion || '—' }}</strong>
                </div>
                <div class="conf-resumen__row" v-if="pasajeroNombre">
                  <span>Pasajero</span>
                  <strong>{{ pasajeroNombre }}</strong>
                </div>
                <div class="conf-resumen__row">
                  <span>Tipo</span>
                  <strong style="text-transform:capitalize">{{ tipoItem }}</strong>
                </div>
                <div class="conf-resumen__row">
                  <span>Fecha</span>
                  <strong>{{ fechaHoy }}</strong>
                </div>
              </div>

              <div class="conf-resumen__total">
                <span>Total pagado</span>
                <strong>{{ totalPagado }}</strong>
              </div>
            </div>

            <div class="conf-nav-btns">
              <button class="conf-nav-btn conf-nav-btn--primary" @click="$router.push('/principal')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                Ir al inicio
              </button>
              <button class="conf-nav-btn conf-nav-btn--secondary" @click="$router.push('/mis-reservaciones')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Mis Reservaciones
              </button>
            </div>
          </aside>

        </div>
      </div>
    </div>
    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/confirmacion.css'

const route  = useRoute()
const router = useRouter()
const API    = 'http://localhost:8080'

// ── Estado ────────────────────────────────────────────────────
const noReservacion  = ref('')
const tipoItem       = ref('')
const itemData       = ref(null)
const detalleVuelo   = ref(null)
const detalleHotel   = ref(null)
const reservacionId  = ref(null)
const pasajeroNombre = ref('')
const totalPagado    = ref('--')
const descargando    = ref(false)
const pdfError       = ref('')

const fechaHoy = computed(() => {
  return new Date().toLocaleDateString('es-GT', { day:'2-digit', month:'long', year:'numeric' })
})

const boletos = computed(() => detalleVuelo.value?.detalle?.boletos || [])

// ── onMounted ─────────────────────────────────────────────────
onMounted(() => {
  noReservacion.value = route.query.noReservacion || ''

  const raw = sessionStorage.getItem('checkout_data')
  if (raw) {
    try {
      const cd = JSON.parse(raw)

      tipoItem.value      = cd.tipoItem || ''
      itemData.value      = cd.item     || null
      detalleVuelo.value  = cd.detalleVuelo || null
      detalleHotel.value  = cd.detalleHotel || null
      reservacionId.value = cd.reservacionId || null

      if (!noReservacion.value) noReservacion.value = cd.noReservacion || ''

      const p = cd.pasajero
      if (p) pasajeroNombre.value = `${p.nombre || ''} ${p.apellido || ''}`.trim()

      // Calcular total
      const tv = cd.detalleVuelo?.total_con_ganancia ?? 0
      const th = cd.detalleHotel?.total_con_ganancia ?? 0
      if      (cd.tipoItem === 'vuelo')   totalPagado.value = tv > 0 ? `$${tv.toFixed(2)}`         : '--'
      else if (cd.tipoItem === 'hotel')   totalPagado.value = th > 0 ? `$${th.toFixed(2)}`         : '--'
      else if (cd.tipoItem === 'paquete') totalPagado.value = (tv+th) > 0 ? `$${(tv+th).toFixed(2)}` : '--'

    } catch { /**/ }
  }

  // Limpiar sesión de reserva — el flujo terminó
  sessionStorage.removeItem('checkout_data')
  sessionStorage.removeItem('_reserva_expires_at')
  sessionStorage.removeItem('_reserva_id')
  sessionStorage.removeItem('_reserva_no')
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
})

// ── Descargar PDF ─────────────────────────────────────────────
async function descargarPDF() {
  if (!reservacionId.value) { pdfError.value = 'No hay reservación disponible.'; return }
  descargando.value = true; pdfError.value = ''
  try {
    const res = await fetch(`${API}/api/reservaciones/${reservacionId.value}/pdf`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const blob = await res.blob()
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href = url
    a.download = `reserva-${noReservacion.value || reservacionId.value}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    pdfError.value = 'No se pudo generar el PDF. Intenta más tarde.'
  } finally {
    descargando.value = false
  }
}

// ── Recomendaciones ───────────────────────────────────────────
const recomendaciones = [
  {
    id: 1, tipo: 'vuelo', origen: 'GUA', destino: 'MIA',
    nombre: 'Miami, Florida',
    desc: 'Avianca · Vuelo directo · Clase económica',
    precio: 'Desde $320',
    gradient: 'linear-gradient(135deg, #1a3a4a 0%, #2d6a7a 50%, #1a3a4a 100%)',
    ruta: '/resultados-vuelos'
  },
  {
    id: 2, tipo: 'paquete', origen: 'GUA', destino: 'CUN',
    nombre: 'Cancún 7 noches',
    desc: 'Vuelo + Hotel incluido · Todo en uno',
    precio: 'Desde Q4,850',
    gradient: 'linear-gradient(135deg, #0d3d2e 0%, #1a6644 50%, #0d3d2e 100%)',
    ruta: '/resultados-paquetes'
  },
  {
    id: 3, tipo: 'hotel', origen: 'ANT', destino: 'ANT',
    nombre: 'Casa Santo Domingo',
    desc: 'Antigua Guatemala · 5 estrellas · Suite Deluxe',
    precio: 'Desde $185/noche',
    gradient: 'linear-gradient(135deg, #3d1a0d 0%, #7a3a1a 50%, #3d1a0d 100%)',
    ruta: '/resultados-hoteles'
  },
  {
    id: 4, tipo: 'paquete', origen: 'GUA', destino: 'MEX',
    nombre: 'Ciudad de México 4 noches',
    desc: 'Copa Airlines + Hotel · Vuelo + Hospedaje',
    precio: 'Desde Q2,600',
    gradient: 'linear-gradient(135deg, #1a1a3d 0%, #3a3a7a 50%, #1a1a3d 100%)',
    ruta: '/resultados-paquetes'
  }
]
</script>