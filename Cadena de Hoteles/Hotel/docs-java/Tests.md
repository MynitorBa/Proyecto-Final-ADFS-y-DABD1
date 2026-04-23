# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 822
- Pasaron: 822
- Fallaron: 0
- Saltados: 0
- Duracion: 112.2s
- Ejecutado: 23/04/2026 15:25:37

---

## Suites

### clients.MoventClientTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall `(59ms)`

**[PASO]** notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(424ms)`

**[PASO]** notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion `(26ms)`

**[PASO]** notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall `(5ms)`

**[PASO]** notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(19ms)`

**[PASO]** notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall `(2ms)`

**[PASO]** notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall `(3ms)`

**[PASO]** notificarHabitacionCerrada_multiplesReservas_swallowsExcepcion `(26ms)`

---

### config.ServerConfigTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** metodoCreateServer_existe_esPublicoEstatico `(87ms)`

**[PASO]** claseServerConfig_existe_esPublica `(2ms)`

---

### controllers.AdminBusquedaControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleResumen_conRolAdmin_retornaResumenEstadistico `(2.3s)`

**[PASO]** handleExportar_conRolIncorrecto_retorna403YNoLlamaServicio `(11ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_aplicaFiltrosDeQueryParams `(67ms)`

**[PASO]** handleExportar_conEmailValidoSinFiltros_llamaServicioYRetornaMensaje `(5ms)`

**[PASO]** handleExportar_conEmailInvalido_retorna400ConMensaje `(4ms)`

**[PASO]** handleExportar_conEmailValidoYFiltros_llamaServicioConFiltros `(4ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_llamaServicioYRetornaResultado `(12ms)`

**[PASO]** handleListarBusquedas_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleResumen_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleExportar_conEmailBlanco_retorna400ConMensaje `(4ms)`

---

### controllers.AerolineaAdminControllerTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** handleEditar_conRolIncorrecto_retorna403YNoLlamaServicio `(37ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(24ms)`

**[PASO]** handleListarLibres_conRolAdmin_retornaListaDeWebserviceLibres `(12ms)`

**[PASO]** handleEditar_conRolAdminYDatosValidos_retornaMensajeExito `(26ms)`

**[PASO]** handleEditar_conRolAdminYDatosInvalidos_retorna400ConMensaje `(8ms)`

**[PASO]** handleCrear_conRolAdminYDatosValidos_retorna201ConNuevaAerolinea `(19ms)`

**[PASO]** handleListar_conRolAdmin_retornaListaDeAerolineas `(13ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleCrear_conRolAdminYDatosInvalidos_retorna400ConMensaje `(4ms)`

**[PASO]** handleListarLibres_conRolIncorrecto_retorna403YNoLlamaServicio `(1ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

---

### controllers.AerolineaWebserviceControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleListar_conRolWebservice_retornaAerolineasDelUsuario `(28ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosInvalidos_retorna400ConMensaje `(20ms)`

**[PASO]** handleCambiarEstado_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosValidos_retorna201ConNuevaAerolinea `(3ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYEstadoInvalido_retorna400ConMensaje `(4ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYDatosValidos_retornaMensajeExito `(3ms)`

---

### controllers.AgenciaControllerTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** handleEditarAdmin_datosValidos_retornaMensaje `(136ms)`

**[PASO]** handleEliminarWebservice_rolNoWebservice_retorna403 `(4ms)`

**[PASO]** handleCrearAdmin_datosInvalidos_retorna400 `(3ms)`

**[PASO]** handleCambiarEstadoWebservice_estadoInvalido_retorna400 `(6ms)`

**[PASO]** handleListarWebservice_rolNoWebservice_retorna403 `(4ms)`

**[PASO]** handleListarAdmin_rolNoAdministrador_retorna403 `(3ms)`

**[PASO]** handleListarWebservice_rolWebservice_retornaLista `(3ms)`

**[PASO]** handleCrearWebservice_rolNoWebservice_retorna403 `(3ms)`

**[PASO]** handleCambiarEstadoWebservice_rolNoWebservice_retorna403 `(5ms)`

**[PASO]** handleEditarAdmin_agenciaNoEncontrada_retorna400 `(3ms)`

**[PASO]** handleEliminarWebservice_agenciaExistente_retornaMensaje `(3ms)`

**[PASO]** handleCrearAdmin_datosValidos_retorna201 `(3ms)`

**[PASO]** handleCrearAdmin_rolNoAdministrador_retorna403 `(3ms)`

**[PASO]** handleCrearWebservice_datosValidos_retorna201 `(2ms)`

**[PASO]** handleHandshake_agenciaNoRegistrada_retorna400 `(7ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaTodasLasAgencias `(6ms)`

**[PASO]** handleCambiarEstadoWebservice_datosValidos_retornaMensaje `(7ms)`

**[PASO]** handleEliminarWebservice_agenciaNoPertenece_retorna400 `(6ms)`

**[PASO]** handleCrearWebservice_argumentoInvalido_retorna400 `(14ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(8ms)`

**[PASO]** handleEditarAdmin_rolNoAdministrador_retorna403 `(4ms)`

---

### controllers.AuthControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleLogout_siempre_invalidaCookieYRetorna200 `(65ms)`

**[PASO]** handleLogin_credencialesValidas_emiteCookieYRetorna200 `(38ms)`

**[PASO]** handleLogin_credencialesInvalidas_retorna401 `(3ms)`

---

### controllers.BusquedaAerolineaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(78ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(2ms)`

---

### controllers.BusquedaAgenciaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(94ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(3ms)`

---

### controllers.BusquedaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_tokenValido_buscaConUsuarioId `(168ms)`

**[PASO]** handleBuscar_servicioLanzaIllegalArgument_retorna404 `(2ms)`

**[PASO]** handleBuscar_sinToken_buscaComoAnonimo `(2ms)`

---

### controllers.CancelacionAgenciaControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handlePuedeCancelar_reservacionValida_retornaResultado200 `(29ms)`

**[PASO]** handleCancelar_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handlePuedeCancelar_errorServicio_retorna500ConMensaje `(3ms)`

**[PASO]** handlePuedeCancelar_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleCancelar_motivoInvalido_retorna400ConMensaje `(22ms)`

**[PASO]** handleCancelar_motivoValido_cancelaYRetorna200 `(3ms)`

---

### controllers.CancelacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleCancelarReservacion_reservacionValida_retorna200 `(4ms)`

**[PASO]** handleCancelarReservacion_reservacionInvalida_retorna400 `(2ms)`

---

### controllers.ComentarioControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleObtenerPorHotelAgencia_authOk_retorna200ConLista `(52ms)`

**[PASO]** handleObtenerPorUsuario_usuarioConComentarios_retorna200ConLista `(3ms)`

**[PASO]** handleObtenerPorHotelAgencia_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleObtenerPorHotel_hotelValido_retorna200ConLista `(1ms)`

**[PASO]** handleObtenerPorUsuario_usuarioSinComentarios_retorna200ConListaVacia `(1ms)`

**[PASO]** handleObtenerPorHotel_hotelSinComentarios_retorna200ConListaVacia `(2ms)`

**[PASO]** handleAgregarComentario_argumentoInvalido_retorna400 `(1ms)`

**[PASO]** handleObtenerPorHotelAgencia_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleAgregarComentario_datosValidos_retorna201 `(3ms)`

---

### controllers.DestinosControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerDestinos_conDestinosExistentes_retorna200ConLista `(18ms)`

**[PASO]** handleObtenerDestinos_sinDestinos_retorna200ConListaVacia `(2ms)`

---

### controllers.DownsControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleAgregarDown_datosValidos_retorna201ConMensaje `(47ms)`

**[PASO]** handleObtenerDowns_usuarioConDowns_retorna200ConLista `(3ms)`

**[PASO]** handleActualizarDown_datosValidos_retorna200ConMensaje `(2ms)`

**[PASO]** handleEliminarDown_downExistente_retorna200ConMensaje `(3ms)`

**[PASO]** handleObtenerDowns_usuarioSinDowns_retorna200ConListaVacia `(3ms)`

**[PASO]** handleAgregarDown_argumentoInvalido_retorna400 `(3ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelValido_retorna200ConLista `(4ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelSinDowns_retorna200ConListaVacia `(2ms)`

**[PASO]** handleActualizarDown_downNoExistente_retorna400 `(5ms)`

**[PASO]** handleEliminarDown_downNoExistente_retorna400 `(3ms)`

---

### controllers.EmailReservacionControllerTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** handleNewsletter_correoValido_enviaCorreoYRetorna200 `(78ms)`

**[PASO]** handleEnviarCorreoReservacion_reservacionNoExiste_retorna404 `(5ms)`

**[PASO]** handleEnviarCorreoReservacion_rolAutorizado_enviaCorreoYRetorna200 `(9ms)`

**[PASO]** handleContacto_camposObligatoriosFaltantes_retorna400 `(4ms)`

**[PASO]** handleEnviarCorreoReservacion_errorRuntime_retorna500 `(4ms)`

**[PASO]** handleNewsletter_correoSinArroba_retorna400 `(2ms)`

**[PASO]** handleContacto_formularioValido_enviaCorreoYRetorna200 `(32ms)`

**[PASO]** handleEnviarCorreoReservacion_rolNoAutorizado_retorna403 `(7ms)`

---

### controllers.HandshakeAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleHandshake_tokenEntradaIncorrecto_retorna400 `(43ms)`

**[PASO]** handleHandshake_servicioExitoso_noLlamaStatus `(2ms)`

**[PASO]** handleHandshake_aerolineaNoRegistrada_retorna400 `(3ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(3ms)`

---

### controllers.HotelAgenciaControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerHoteles_autenticacionFallida_noRetornaDatos `(54ms)`

**[PASO]** handleObtenerHoteles_autenticacionValida_retornaListaHoteles `(4ms)`

---

### controllers.HotelControllerTest

- Tests: 83
- Pasaron: 83
- Fallaron: 0

**[PASO]** handleReactivarHabitacion_habitacionNoEncontrada_retorna404 `(137ms)`

**[PASO]** handleEliminarHotel_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleEliminarHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleEditarHotel_servicioLanzaExcepcion_retorna400 `(27ms)`

**[PASO]** handleEliminarImagenAmenidad_rolAdmin_eliminaImagenExitosamente `(1ms)`

**[PASO]** handleReservasActivasHotel_hotelNoEncontrado_retorna404 `(1ms)`

**[PASO]** handleEliminarImagenAmenidad_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearHabitacion_servicioLanzaExcepcion_retorna400 `(22ms)`

**[PASO]** handleAgregarImagenHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleListarAmenidadesHotel_rolAdmin_retornaAmenidadesDelHotel `(1ms)`

**[PASO]** handleEliminarHotel_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleCerrarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearAmenidad_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarCiudades_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleCrearAmenidad_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEliminarImagenHotel_rolAdmin_eliminaImagenExitosamente `(2ms)`

**[PASO]** handleAgregarAmenidadHotel_rolAdmin_agregaAmenidadYRetorna201 `(31ms)`

**[PASO]** handleListarHabitaciones_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleReservasActivasHotel_rolAdmin_retornaReservasActivas `(2ms)`

**[PASO]** handleReservasActivasHabitacion_habitacionNoEncontrada_retorna404 `(4ms)`

**[PASO]** handleEditarHabitacion_rolAdmin_editaHabitacionExitosamente `(21ms)`

**[PASO]** handleListarHabitaciones_hotelNoEncontrado_retorna404 `(4ms)`

**[PASO]** handleEliminarImagenHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleEliminarAmenidadHotel_rolAdmin_eliminaAmenidadExitosamente `(1ms)`

**[PASO]** handleListarPaises_rolAdmin_retornaListaDePaises `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEliminarHabitacion_habitacionNoEncontrada_retorna404 `(1ms)`

**[PASO]** handleCrearAmenidad_rolAdmin_creaAmenidadYRetorna201 `(2ms)`

**[PASO]** handleCrearHabitacion_rolAdmin_creaHabitacionYRetorna201 `(2ms)`

**[PASO]** handleAgregarImagenHotel_rolAdmin_agregaImagenYRetorna201 `(23ms)`

**[PASO]** handleEliminarHotel_rolAdmin_eliminaHotelExitosamente `(3ms)`

**[PASO]** handleReactivarHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCerrarHabitacion_rolAdmin_cierraHabitacionExitosamente `(3ms)`

**[PASO]** handleListarHoteles_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleListarReservaciones_rolAdmin_retornaTodasLasReservaciones `(3ms)`

**[PASO]** handleEditarHabitacion_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleListarAmenidadesHotel_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleEditarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleObtenerMetricas_rolAdmin_retornaMetricasDelSistema `(2ms)`

**[PASO]** handleCrearHotel_rolAdmin_creaHotelYRetorna201 `(20ms)`

**[PASO]** handleCerrarHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarAmenidadHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_servicioLanzaExcepcion_retorna400 `(1ms)`

**[PASO]** handleAgregarAmenidadHotel_servicioLanzaExcepcion_retorna400 `(1ms)`

**[PASO]** handleCrearHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarHabitacion_rolAdmin_eliminaHabitacionExitosamente `(2ms)`

**[PASO]** handleListarAmenidadesHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHotel_servicioLanzaExcepcion_retorna400 `(1ms)`

**[PASO]** handleListarHabitaciones_rolAdmin_retornaHabitacionesDelHotel `(2ms)`

**[PASO]** handleAgregarImagenHabitacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleEditarHotel_rolAdmin_editaHotelExitosamente `(1ms)`

**[PASO]** handleEliminarImagenHabitacion_rolAdmin_eliminaImagenExitosamente `(1ms)`

**[PASO]** handleListarAmenidades_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCancelarReservacion_rolAdminConMotivo_cancelaYRetornaRespuesta `(20ms)`

**[PASO]** handleCerrarHabitacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarHoteles_rolAdmin_retornaListaDeHoteles `(2ms)`

**[PASO]** handleReservasActivasHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleListarAmenidades_rolAdmin_retornaListaDelServicio `(2ms)`

**[PASO]** handleCrearHotel_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleReactivarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCancelarReservacion_bodyLanzaExcepcion_usaMotivoDefault `(1ms)`

**[PASO]** handleReactivarHotel_hotelNoEncontrado_retorna404 `(1ms)`

**[PASO]** handleCancelarReservacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCerrarHotel_rolAdmin_cierraHotelExitosamente `(2ms)`

**[PASO]** handleReactivarHabitacion_rolAdmin_reactivaHabitacionExitosamente `(1ms)`

**[PASO]** handleListarCiudades_rolAdmin_retornaListaDeCiudades `(2ms)`

**[PASO]** handleEliminarImagenHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleActualizarAmenidadHotel_rolAdmin_actualizaAmenidadExitosamente `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_rolAdmin_agregaImagenYRetorna201 `(2ms)`

**[PASO]** handleAgregarImagenHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarImagenHabitacion_rolAdmin_agregaImagenYRetorna201 `(1ms)`

**[PASO]** handleReservasActivasHabitacion_rolAdmin_retornaReservasActivas `(2ms)`

**[PASO]** handleListarPaises_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleReactivarHotel_rolAdmin_reactivaHotelExitosamente `(2ms)`

**[PASO]** handleObtenerMetricas_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEditarHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarAmenidadHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleAgregarImagenHotel_servicioLanzaExcepcion_retorna400 `(1ms)`

**[PASO]** handleCancelarReservacion_servicioLanzaExcepcion_retorna400 `(1ms)`

**[PASO]** handleReservasActivasHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleActualizarAmenidadHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleListarReservaciones_sinRolAdmin_retorna403 `(1ms)`

---

### controllers.ImagenControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handleObtenerImagenAmenidad_imagenExiste_retornaImagenJpeg `(28ms)`

**[PASO]** handleObtenerImagenHotel_imagenNoExiste_retorna404 `(1ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenExiste_retornaImagenJpeg `(1ms)`

**[PASO]** handleObtenerImagenAmenidad_imagenNoExiste_retorna404 `(2ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenNoExiste_retorna404 `(0ms)`

**[PASO]** handleObtenerImagenHotel_imagenExiste_retornaImagenJpeg `(1ms)`

---

### controllers.PagoAgenciaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleProcesarPago_authFalla_noInvocaServicio `(30ms)`

**[PASO]** handleProcesarPago_errorPasarela_retorna500ConMensaje `(23ms)`

**[PASO]** handleProcesarPago_pagoValido_retornaConfirmacion200 `(2ms)`

**[PASO]** handleProcesarPago_pagoInvalido_retorna400ConMensaje `(3ms)`

---

### controllers.PagoControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleProcesarPago_errorRuntime_retorna500 `(54ms)`

**[PASO]** handleProcesarPago_pagoExitoso_retorna200 `(2ms)`

**[PASO]** handleProcesarPago_argumentoInvalido_retorna400 `(3ms)`

---

### controllers.PdfReservacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleDescargarPdf_reservacionNoEncontrada_retorna404 `(26ms)`

**[PASO]** handleDescargarPdf_reservacionValida_retornaPdfComoAdjunto `(4ms)`

---

### controllers.ReservacionAgenciaControllerTest

- Tests: 13
- Pasaron: 13
- Fallaron: 0

**[PASO]** handleObtenerDetalleReservacion_reservacionNoEncontrada_retorna404ConMensaje `(46ms)`

**[PASO]** handleCrearReservacion_errorInterno_retorna500ConMensaje `(29ms)`

**[PASO]** handleExpirarReservacion_reservacionValida_expiraYRetornaMensaje `(3ms)`

**[PASO]** handleObtenerReservaciones_agenciaValida_retornaLista200 `(4ms)`

**[PASO]** handleExpirarReservacion_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleObtenerDetalleReservacion_errorInterno_retorna500ConMensaje `(2ms)`

**[PASO]** handleObtenerDetalleReservacion_reservacionExiste_retornaDetalle200 `(2ms)`

**[PASO]** handleCrearReservacion_datosInvalidos_retorna400ConMensaje `(4ms)`

**[PASO]** handleExpirarReservacion_reservacionInvalida_retorna400ConMensaje `(2ms)`

**[PASO]** handleObtenerDetalleReservacion_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleCrearReservacion_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleObtenerReservaciones_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleCrearReservacion_requestValido_retornaReservacion201 `(3ms)`

---

### controllers.ReservacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleCrearReservacion_datosValidos_retorna201 `(128ms)`

**[PASO]** handleObtenerReservaciones_usuarioConReservaciones_retorna200ConLista `(2ms)`

**[PASO]** handleCrearReservacion_errorRuntime_retorna500 `(13ms)`

**[PASO]** handleCrearReservacion_argumentoInvalido_retorna400 `(7ms)`

**[PASO]** handleObtenerReservaciones_usuarioSinReservaciones_retorna200ConListaVacia `(7ms)`

---

### controllers.SesionControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleSesion_tokenInvalido_retornaSinSesion `(53ms)`

**[PASO]** handleSesion_tokenValido_retornaConSesion `(12ms)`

**[PASO]** handleSesion_sinToken_retornaSinSesion `(1ms)`

---

### controllers.TokenAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleGenerarToken_authOkYDatosValidos_retorna201ConToken `(68ms)`

**[PASO]** handleGenerarToken_authOkYTokenHashNulo_servicioLlamadoConNulo `(3ms)`

**[PASO]** handleGenerarToken_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleGenerarToken_servicioLanzaExcepcion_retorna400 `(3ms)`

---

### controllers.TokenValidacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleValidar_tokenNull_retorna400SinLlamarServicio `(48ms)`

**[PASO]** handleValidar_tokenVacio_retorna400SinLlamarServicio `(5ms)`

**[PASO]** handleValidar_tokenBlanco_retorna400SinLlamarServicio `(1ms)`

**[PASO]** handleValidar_tokenValido_retorna200ConResultado `(5ms)`

**[PASO]** handleValidar_tokenExpirado_retorna400ConMensaje `(12ms)`

---

### controllers.UsuarioControllerTest

- Tests: 15
- Pasaron: 15
- Fallaron: 0

**[PASO]** handleRegistrar_camposDuplicados_retorna409 `(803ms)`

**[PASO]** handleCambiarTelefono_telefonoInvalido_retorna400 `(9ms)`

**[PASO]** handleObtenerPerfil_usuarioAutenticado_retornaPerfil `(5ms)`

**[PASO]** handleCambiarContrasena_credencialesValidas_retorna200 `(8ms)`

**[PASO]** handleCambiarRol_rolNoAutorizado_retorna403 `(2ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaLista `(2ms)`

**[PASO]** handleCambiarTelefono_telefonoValido_retorna200 `(7ms)`

**[PASO]** handleCambiarRol_adminActualiza_retorna200 `(100ms)`

**[PASO]** handleValidar_servicioRetornaFalse_retornaResultadoNoDisponible `(10ms)`

**[PASO]** handleCambiarRol_rolInvalido_retorna400 `(3ms)`

**[PASO]** handleValidar_requestValido_retornaResultado `(3ms)`

**[PASO]** handleCambiarContrasena_credencialesInvalidas_retorna401 `(30ms)`

**[PASO]** handleRegistrar_nuevoUsuario_retorna201ConId `(7ms)`

**[PASO]** handleObtenerPerfil_diferentesUsuarios_llamaServicioConIdCorrecto `(2ms)`

**[PASO]** handleListarAdmin_rolNoAutorizado_retorna403 `(3ms)`

---

### data.DataAccessExceptionTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** sePuedeLanzarYCapturar_comoRuntimeException `(5ms)`

**[PASO]** constructor_conMensajeYCausa_almacenaAmbosValores `(2ms)`

**[PASO]** mensajeDescriptivo_sePropaga_correctamente `(0ms)`

**[PASO]** causa_puedeSerSQLException_simulada `(134ms)`

**[PASO]** sePuedeCapturar_comoDataAccessException `(4ms)`

**[PASO]** esSubclase_deRuntimeException `(2ms)`

---

### data.ResultSetMapperTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** implementacion_conLambda_mapeaStringCorrectamente `(176ms)`

**[PASO]** implementacion_propagaSQLException_cuandoResultSetFalla `(2ms)`

**[PASO]** implementacion_conLambda_construyeObjetoCompuesto `(1ms)`

**[PASO]** implementacion_conLambda_mapeaDoubleCorrectamente `(1ms)`

**[PASO]** implementacion_conLambda_mapeaEnteroCorrectamente `(0ms)`

---

### dtos.DtosTest

- Tests: 78
- Pasaron: 78
- Fallaron: 0

**[PASO]** puedeCancelarDTO_constructorFalse_almacenaCorrecto `(2ms)`

**[PASO]** crearHotelRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_constructor_almacenaBooleans `(0ms)`

**[PASO]** tokenValidacionResponseDTO_constructor_almacenaTodosLosCampos `(0ms)`

**[PASO]** aerolineaIdentidadDTO_constructor_almacenaNombreYUrl `(100ms)`

**[PASO]** cambiarContrasenaRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** editarHotelRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** hotelAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** habitacionAgenciaDTO_settersYGetters_funcionan `(57ms)`

**[PASO]** reservacionRequestDTO_setterHabitaciones_funciona `(15ms)`

**[PASO]** hotelResultadoDTO_listas_seAsignanCorrectamente `(24ms)`

**[PASO]** reservacionResponseDTO_settersYGetters_funcionan `(6ms)`

**[PASO]** editarAerolineaRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** crearAerolineaRequestDTO_settersYGetters_funcionan `(3ms)`

**[PASO]** loginResponseDTO_constructor_almacenaTodosLosCampos `(5ms)`

**[PASO]** cambiarRolRequestDTO_setterYGetter_funcionan `(1ms)`

**[PASO]** usuarioAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** aerolineaAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_setter_sobrescribeToken `(8ms)`

**[PASO]** crearAerolineaAdminRequestDTO_settersYGetters_funcionan `(4ms)`

**[PASO]** ciudadDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** reservacionDetalleDTO_settersYGetters_funcionan `(4ms)`

**[PASO]** paisDTO_constructor_almacenaIdYNombre `(1ms)`

**[PASO]** downResponseDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agregarAmenidadRequestDTO_settersYGetters_funcionan `(9ms)`

**[PASO]** comentarioRequestDTO_settersYGetters_funcionan `(6ms)`

**[PASO]** cancelacionRequestDTO_setterYGetter_funcionan `(1ms)`

**[PASO]** paisDTO_setters_sobrescribenValores `(1ms)`

**[PASO]** reservacionAgenciaResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** usuarioValidacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** tokenAerolineaResponseDTO_constructor_almacenaTodosLosCampos `(1ms)`

**[PASO]** busquedaRequestDTO_valoresPorDefecto_sonNullOCero `(1ms)`

**[PASO]** downRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** downRequestDTO_valorNegativo_seAlmacenaCorrectamente `(2ms)`

**[PASO]** usuarioPerfilResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** tokenValidacionResponseDTO_porcentajeCero_seAlmacenaCorrectamente `(1ms)`

**[PASO]** editarAgenciaRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** resultadoNotificacionDTO_settersYGetters_funcionan `(15ms)`

**[PASO]** handshakeRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** tokenAerolineaRequestDTO_settersYGetters_funcionan `(9ms)`

**[PASO]** aerolineaAdminDTO_valoresPorDefecto_sonCero `(3ms)`

**[PASO]** comentarioResponseDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionAgenciaResponseDTO_settersYGetters_funcionan `(13ms)`

**[PASO]** reservacionRequestDTO_listaVacia_seAsignaCorrectamente `(0ms)`

**[PASO]** habitacionAdminDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** pagoRequestDTO_settersYGetters_facturacion_funcionan `(8ms)`

**[PASO]** cambiarTelefonoRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** usuarioWebserviceLibreDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** sesionDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** loginRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaIdentidad_settersYGetters_funcionan `(3ms)`

**[PASO]** tipoHabitacionResultadoDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** loginResponseDTO_diferencteRol_seAlmacenaCorrectamente `(2ms)`

**[PASO]** busquedaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** amenidadDTO_setters_sobrescribenValores `(0ms)`

**[PASO]** hotelAmenidadDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** editarHabitacionRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionReservaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** crearAgenciaRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** sesionDTO_autenticadoFalsePorDefecto `(1ms)`

**[PASO]** usuarioValidacionResponseDTO_todosFalse_cuandoNingunExiste `(1ms)`

**[PASO]** crearAgenciaAdminRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** amenidadHotelDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** hotelAgenciaDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** hotelResultadoDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaWebserviceDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** puedeCancelarDTO_constructorTrue_almacenaCorrecto `(1ms)`

**[PASO]** habitacionResumenDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** crearHabitacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_constructor_almacenaToken `(1ms)`

**[PASO]** amenidadDTO_constructor_almacenaIdYNombre `(1ms)`

**[PASO]** pagoAgenciaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** subirImagenRequestDTO_setterYGetter_funcionan `(2ms)`

**[PASO]** loginRequestDTO_valoresPorDefecto_sonNull `(0ms)`

**[PASO]** habitacionDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaDTO_valoresPorDefecto_sonCero `(1ms)`

**[PASO]** pagoResponseDTO_settersYGetters_funcionan `(0ms)`

---

### helpers.AerolineaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(51ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAerolinea `(4ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(3ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(5ms)`

---

### helpers.AgenciaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(45ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(6ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAgencia `(14ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(2ms)`

---

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(13ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(1ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(2ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(3ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(4ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(0ms)`

---

### helpers.EmailHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** enviar_datosValidos_noLanzaExcepcion `(8ms)`

**[PASO]** enviar_cuerpoMinimo_noLanzaExcepcion `(12ms)`

**[PASO]** enviar_argumentosExactos_invocaMetodoConParametrosCorrectos `(3ms)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(605ms)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(112ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(2ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(1ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(2ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(2ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(1ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(2ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(1ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(2ms)`

**[PASO]** getRolId_retornaRolCorrecto `(2ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(2ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(320ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(542ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(517ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(507ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(381ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(42ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(4ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(3ms)`

**[PASO]** validar_numeroConEspacios_esValido `(5ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(2ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(1ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(2ms)`

**[PASO]** validar_cvv4digitos_esValido `(2ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(0ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(1ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(2ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(0ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(1ms)`

---

### models.UsuarioModelTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** ciudadId_aceptaNull `(2ms)`

**[PASO]** valoresPorDefecto_sonNullOCero `(1ms)`

**[PASO]** reasignarUsername_noAfectaOtrosCampos `(0ms)`

**[PASO]** setterYGetter_fechaNacimiento_funciona `(1ms)`

**[PASO]** ciudadId_conValor_seAlmacenaCorrectamente `(0ms)`

**[PASO]** constructorVacio_creaInstanciaNoNula `(1ms)`

**[PASO]** settersYGetters_camposNumericos_funcionan `(0ms)`

**[PASO]** settersYGetters_camposTexto_funcionan `(0ms)`

---

### repositories.AdminReservacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarTodas_retornaListaConReservacionDePrueba `(2.9s)`

**[PASO]** obtenerReservacion_idExistente_retornaDatos `(1.4s)`

**[PASO]** obtenerReservacion_idInexistente_retornaNull `(1.1s)`

**[PASO]** obtenerDatosUsuarioPorReservacion_reservacionExistente_retornaDatos `(1.1s)`

**[PASO]** cancelarReservacion_reservacionPendiente_cambiaEstado `(1.1s)`

---

### repositories.AerolineaAdminRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarTodas_retornaListaNoNula `(581ms)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(1.1s)`

**[PASO]** editar_aerolineaExistente_actualizaDatos `(669ms)`

**[PASO]** listarWebserviceLibres_retornaListaNoNula `(550ms)`

---

### repositories.AerolineaAliadaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerAerolineaPorToken_tokenActivo_retornaDto `(768ms)`

**[PASO]** obtenerAerolineaPorToken_tokenInexistente_retornaNull `(680ms)`

**[PASO]** obtenerDescuentoAerolinea_tokenActivo_retornaDescuentoPositivo `(730ms)`

**[PASO]** buscarCiudadId_ciudadExistente_retornaId `(649ms)`

**[PASO]** guardarBusqueda_datosValidos_persisteEnOracle `(759ms)`

**[PASO]** obtenerAerolineaIdPorURL_urlExistente_retornaId `(657ms)`

**[PASO]** guardarTokensAerolinea_datosValidos_actualizaToken `(750ms)`

---

### repositories.AerolineaWebserviceRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAerolinea_retornaListaConAlMenosUna `(520ms)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(963ms)`

**[PASO]** cambiarEstado_aerolineaActiva_actualizaEstado `(593ms)`

**[PASO]** listarPorUsuario_usuarioSinAerolinea_retornaListaVacia `(583ms)`

---

### repositories.AgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAgencia_retornaListaConAlMenosUna `(572ms)`

**[PASO]** crear_datosValidos_retornaAgenciaConId `(932ms)`

**[PASO]** editar_datosNuevos_actualizaNombre `(1.1s)`

**[PASO]** cambiarEstado_agenciaActiva_cambiaEstado `(660ms)`

**[PASO]** obtenerAgenciaPorToken_sinToken_retornaNull `(548ms)`

---

### repositories.AuthRepositoryIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** buscarPorIdentificador_porUsername_retornaUsuario `(374ms)`

**[PASO]** buscarPorIdentificador_porCorreo_retornaUsuario `(362ms)`

**[PASO]** buscarPorIdentificador_identificadorInexistente_retornaNull `(376ms)`

---

### repositories.BusquedaAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(531ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(542ms)`

**[PASO]** obtenerDescuentoAgencia_usuarioConAgencia_retornaDescuento `(719ms)`

**[PASO]** guardarBusqueda_datosValidos_noLanzaExcepcion `(543ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(540ms)`

---

### repositories.BusquedaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(111ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(101ms)`

**[PASO]** guardarBusqueda_sinUsuario_noLanzaExcepcion `(139ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(98ms)`

**[PASO]** buscarImagenesHotel_hotelInexistente_retornaListaVacia `(149ms)`

**[PASO]** buscarAmenidadesHotel_hotelInexistente_retornaListaVacia `(107ms)`

**[PASO]** buscarImagenesHabitacion_habitacionInexistente_retornaListaVacia `(115ms)`

---

### repositories.CancelacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaCancelar_reservacionDelUsuario_retornaDatos `(452ms)`

**[PASO]** obtenerReservacionParaCancelar_otroUsuarioId_retornaNull `(480ms)`

**[PASO]** obtenerFechaCheckInMasReciente_sinDetalles_retornaNull `(445ms)`

**[PASO]** cancelarReservacion_estadoPendiente_actualizaAEstado4 `(480ms)`

**[PASO]** obtenerReservacionAgenciaParaCancelar_sinAgenciaVinculada_retornaNull `(459ms)`

---

### repositories.ComentarioRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** existeComentarioConResena_sinComentarios_retornaFalse `(532ms)`

**[PASO]** crearComentario_conResena_retornaIdPositivo `(529ms)`

**[PASO]** existeComentarioConResena_despuesDeCrear_retornaTrue `(579ms)`

**[PASO]** crearComentario_sinResena_esRespuestaAOtro `(527ms)`

**[PASO]** actualizarRatingHotel_conResena_noLanzaExcepcion `(594ms)`

**[PASO]** obtenerComentario_comentarioExistente_retornaDtoConDatos `(581ms)`

**[PASO]** obtenerComentariosPorUsuario_retornaListaConAlMenosUno `(583ms)`

**[PASO]** obtenerComentariosPorHotel_retornaListaConAlMenosUno `(592ms)`

---

### repositories.DestinosRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerTodosLosHoteles_retornaListaNoNula `(45ms)`

**[PASO]** obtenerTodosLosHoteles_conHotelesActivos_retornaDtosValidos `(38ms)`

**[PASO]** obtenerImagenesHotel_hotelExistente_retornaListaNoNula `(106ms)`

**[PASO]** obtenerImagenesHotel_hotelInexistente_retornaListaVacia `(34ms)`

---

### repositories.DownsRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerValorDown_sinDown_retornaNull `(761ms)`

**[PASO]** insertarDown_registraDown_obtenibleEnOracle `(770ms)`

**[PASO]** obtenerDownsDeUsuario_trasInsertarDown_retornaListaConDown `(799ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_filtraPorHotel `(783ms)`

**[PASO]** actualizarContadorDown_incrementaContador `(788ms)`

**[PASO]** eliminarDown_eliminaDown_obtenerValorRetornaNull `(891ms)`

---

### repositories.HotelRepositoryIntegrationTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** listarAmenidades_retornaListaNoNula `(447ms)`

**[PASO]** crearAmenidad_nombreValido_retornaIdPositivo `(485ms)`

**[PASO]** listarTodos_retornaListaConElHotelInsertado `(456ms)`

**[PASO]** actualizarHotel_datosNuevos_actualizaNombreEnOracle `(517ms)`

**[PASO]** cerrarHotel_hotelActivo_cambiaEstadoId `(476ms)`

**[PASO]** reactivarHotel_hotelCerrado_restauraEstadoId `(489ms)`

**[PASO]** existe_hotelExistente_retornaTrue `(493ms)`

**[PASO]** crearHabitacion_datosValidos_retornaIdPositivo `(583ms)`

**[PASO]** obtenerMetricas_retornaMapaConClaves `(687ms)`

---

### repositories.ImagenRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerImagenHotel_idExistente_retornaBytes `(301ms)`

**[PASO]** obtenerImagenHotel_idInexistente_retornaNull `(262ms)`

**[PASO]** obtenerImagenHabitacion_idExistente_retornaBytes `(267ms)`

**[PASO]** obtenerImagenHabitacion_idInexistente_retornaNull `(296ms)`

**[PASO]** eliminarImagenHotel_eliminaImagen_noObtenible `(277ms)`

**[PASO]** eliminarImagenHabitacion_eliminaImagen_noObtenible `(279ms)`

**[PASO]** obtenerImagenAmenidad_idInexistente_retornaNull `(261ms)`

---

### repositories.PagoAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDeAgencia_retornaDatos `(918ms)`

**[PASO]** obtenerReservacionParaPago_agenciaIncorrecta_retornaNull `(925ms)`

**[PASO]** confirmarReservacion_estadoPendiente_actualizaAEstado2 `(931ms)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(991ms)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(1s)`

---

### repositories.PagoRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDelUsuario_retornaDatosCorrectos `(920ms)`

**[PASO]** obtenerReservacionParaPago_otroUsuarioId_retornaNull `(877ms)`

**[PASO]** confirmarReservacion_estadoPendiente_cambiaAEstado2 `(870ms)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(830ms)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(969ms)`

**[PASO]** actualizarTotalReservacion_nuevoTotal_actualizaCorrectamente `(814ms)`

**[PASO]** obtenerCiudadReservacion_conDetallesYHotel_retornaNombreCiudad `(789ms)`

---

### repositories.ReservacionAgenciaRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerDescuentoAgencia_agenciaConDescuento_retornaValorPositivo `(1.3s)`

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(1.1s)`

**[PASO]** existeTraslape_fechasSinConflicto_retornaFalse `(1.2s)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(1.1s)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatos `(1.2s)`

**[PASO]** expirarReservacion_reservacionPendiente_actualizaEstado `(1.2s)`

---

### repositories.ReservacionRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(885ms)`

**[PASO]** existeTraslape_sinReservacionesConflicto_retornaFalse `(841ms)`

**[PASO]** existeTraslape_conReservacionConflicto_retornaTrue `(841ms)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(956ms)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatosCorrectos `(780ms)`

**[PASO]** obtenerReservacionesDeUsuario_conDetalle_retornaListaConAlMenosUno `(766ms)`

**[PASO]** expirarReservacionesVencidas_noLanzaExcepcion_retornaEntero `(806ms)`

**[PASO]** obtenerImagenesHotel_hotelSinImagenes_retornaListaVacia `(776ms)`

---

### repositories.UsuarioRepositoryIntegrationTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** existeUsername_usernameExistente_retornaTrue `(386ms)`

**[PASO]** existeUsername_usernameInexistente_retornaFalse `(388ms)`

**[PASO]** existeCorreo_correoExistente_retornaTrue `(402ms)`

**[PASO]** existePasaporte_pasaporteExistente_retornaTrue `(427ms)`

**[PASO]** existePasaporte_pasaporteNuloOVacio_retornaFalse `(351ms)`

**[PASO]** crearUsuario_datosValidos_retornaIdPositivo `(680ms)`

**[PASO]** obtenerPerfil_usuarioExistente_retornaDtoConDatos `(389ms)`

**[PASO]** actualizarTelefono_usuarioExistente_cambiaElCampo `(427ms)`

**[PASO]** obtenerContrasena_usuarioExistente_retornaHashNoNulo `(391ms)`

**[PASO]** listarTodosConRol_retornaListaNoNula `(426ms)`

---

### services.AdminBusquedaServiceIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarSinFiltrosRetornaResultadosYTotalCorrecto `(648ms)`

**[PASO]** listarConFiltroDestinoFiltraCorrectamente `(604ms)`

**[PASO]** listarConTipoWebRetornaSoloBusquedasWeb `(655ms)`

**[PASO]** listarPaginacionRetornaSegundaPagina `(704ms)`

**[PASO]** resumenRetornaEstructuraCompletaDesdeOracle `(704ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(48ms)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(1ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(2ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(1ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(2ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(1ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(2.3s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(2ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(1.6s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(2ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(1ms)`

---

### services.AdminReservacionServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(77ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(1ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacionYNotificaAgencia `(1ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacionYNotificaAgencia `(1ms)`

**[PASO]** cancelarReservacion_notificadorRetornaErrorHTTP_completaCancelacionConError `(2ms)`

**[PASO]** cancelarReservacion_notificadorRetornaNoEsReservaAgencia_completaCancelacion `(1ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(1ms)`

---

### services.AerolineaAdminServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarWebserviceLibres_conUsuariosDisponibles_retornaListaDelRepo `(52ms)`

**[PASO]** editar_requestValido_delegaAlRepo `(2ms)`

**[PASO]** listarTodas_conAerolineas_retornaListaDelRepo `(1ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(2ms)`

**[PASO]** listarWebserviceLibres_todosAsignados_retornaListaVacia `(0ms)`

**[PASO]** listarTodas_sinAerolineas_retornaListaVacia `(0ms)`

**[PASO]** editar_idDistinto_invocaRepoConIdCorrecto `(1ms)`

---

### services.AerolineaWebserviceServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioSinAerolineas_retornaListaVacia `(42ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentExceptionSinInvocarRepo `(0ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(1ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(3ms)`

**[PASO]** listarPorUsuario_usuarioConAerolineas_retornaListaDelRepo `(1ms)`

---

### services.AgenciaNotificadorExternoServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** notificarCancelacion_agenciaSinToken_retornaErrorSinHTTP `(150ms)`

**[PASO]** notificarCancelacion_httpError500_retornaEnviadoTrueConStatus500 `(7ms)`

**[PASO]** notificarCancelacion_agenciaSinURL_retornaErrorSinHTTP `(2ms)`

**[PASO]** notificarCancelacion_httpExitoso200_retornaEnviadoTrueYStatusCorrecto `(13ms)`

**[PASO]** notificarCancelacion_excepcionRed_retornaErrorEnDTOSinPropagar `(5ms)`

**[PASO]** notificarCancelacion_noEsReservaDeAgencia_retornaEsReservaFalseYSinHTTP `(5ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(1ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodas_retornaListaCompleta `(2ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(1ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(1ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(2ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(669ms)`

**[PASO]** loginExitosoConCorreo `(600ms)`

**[PASO]** loginFallaUsuarioInexistente `(371ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(599ms)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(557ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(499ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(1ms)`

---

### services.BusquedaAerolineaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_guardaBusquedaEnRepo `(2ms)`

**[PASO]** buscar_conDescuento10Porciento_aplicaDescuentoAPrecios `(3ms)`

**[PASO]** buscar_tokenInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscar_dosHotelesEnCiudad_retornaListaConDosHoteles `(2ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_retornaListaDeHoteles `(2ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(47ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(4ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(2ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(1ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(2ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(0ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(0ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(1.5s)`

**[PASO]** busquedaRegistraEventoEnOracle `(1.5s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(1.5s)`

**[PASO]** busquedaFallaCiudadInexistente `(489ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(57ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(3ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(1ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(1ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(2ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(2ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(645ms)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(568ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(701ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(42ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(2ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(0ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(0ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(0ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(1ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(2ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(2ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(2ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(51ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(1ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(1ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(0ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(0ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(2ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(0ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(120ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(1ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(1ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(0ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(46ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(0ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(0ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(2ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(1ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(2ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(0ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(0ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(2ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(2ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(0ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(1ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(4ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(42ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(1.8s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(1ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(2ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(55ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(2ms)`

**[PASO]** iniciar_noLanzaExcepcion `(1ms)`

**[PASO]** detener_noLanzaExcepcion `(3ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(1ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(2ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(1ms)`

---

### services.HandshakeAerolineaServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarHandshake_urlRegistrada_retornaResponseDTOConTokenDeSalida `(1ms)`

**[PASO]** procesarHandshake_tokensNoPersistidos_lanzaIllegalArgumentException `(3ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(2ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(43ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(1ms)`

---

### services.HotelServiceTest

- Tests: 69
- Pasaron: 69
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(134ms)`

**[PASO]** agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje `(0ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(2ms)`

**[PASO]** editarHabitacion_tipoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(2ms)`

**[PASO]** eliminarAmenidadHotel_delegaAlRepositorio `(0ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero `(0ms)`

**[PASO]** agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** listarTodasReservaciones_delegaAlRepositorio `(2ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarImagenHabitacion_delegaAlRepositorio `(1ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarPaises_delegaAlRepositorioDePaises `(1ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva `(4ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerMetricas_delegaAlRepositorio `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(0ms)`

**[PASO]** editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra `(17ms)`

**[PASO]** listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** agregarImagenHabitacion_habitacionExiste_retornaMapaConId `(2ms)`

**[PASO]** reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero `(2ms)`

**[PASO]** agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarHabitaciones_hotelExiste_retornaListaConImagenes `(2ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos `(0ms)`

**[PASO]** editarHabitacion_habitacionExiste_invocaActualizarHabitacion `(3ms)`

**[PASO]** eliminarImagenAmenidad_delegaAlRepositorio `(2ms)`

**[PASO]** eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar `(1ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(0ms)`

**[PASO]** listarCiudades_delegaAlRepositorioDePaises `(2ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(2ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(0ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra `(5ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion `(2ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel `(2ms)`

**[PASO]** agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarImagenHotel_delegaAlRepositorio `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar `(2ms)`

**[PASO]** agregarImagenAmenidad_base64Valido_retornaMapaConId `(1ms)`

**[PASO]** obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(0ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos `(0ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException `(1ms)`

**[PASO]** actualizarAmenidadHotel_delegaAlRepositorio `(2ms)`

**[PASO]** reactivarHabitacion_habitacionExiste_invocaReactivar `(1ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** reactivarHotel_hotelExiste_invocaReactivarHotel `(0ms)`

**[PASO]** cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException `(2ms)`

**[PASO]** editarHabitacion_estadoInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(0ms)`

**[PASO]** agregarImagenHotel_hotelExiste_retornaMapaConId `(0ms)`

**[PASO]** listarAmenidades_delegaAlRepositorio `(2ms)`

**[PASO]** eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(62ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(2ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(1ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(1ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(43ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(2ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(836ms)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(567ms)`

**[PASO]** pagoFallaCvvInvalido `(597ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(73ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(2ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(0ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(2ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(2ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(50ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(2ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(49ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(1ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(0ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(1ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(2ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(1ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(10ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(712ms)`

**[PASO]** crearReservacionFallaSinHabitaciones `(393ms)`

**[PASO]** crearReservacionFallaFechaPasada `(399ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(3ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(0ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(2ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(41ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(2ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(2ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(1ms)`

---

### services.TokenAerolineaServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** generarToken_datosValidos_insertaTokenConIdsCorrectos `(42ms)`

**[PASO]** generarToken_tokenValidoCiudadExistente_retornaResponseDTOCompleto `(0ms)`

**[PASO]** generarToken_ciudadNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** generarToken_tokenInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** generarToken_datosValidos_urlRedireccionContieneUrlBaseYToken `(1ms)`

---

### services.TokenValidacionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** validar_tokenInexistente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** validar_tokenExpirado_lanzaIllegalArgumentException `(0ms)`

**[PASO]** validar_tokenValido_retornaTokenValidacionResponseDTO `(0ms)`

**[PASO]** validar_tokenYaUtilizado_lanzaIllegalArgumentException `(2ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(120ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(1ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(1ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(1ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(1ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(1ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(1ms)`

**[PASO]** validarDisponibilidad_todosLibres `(1ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(507ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(0ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(2s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(2ms)`

---
