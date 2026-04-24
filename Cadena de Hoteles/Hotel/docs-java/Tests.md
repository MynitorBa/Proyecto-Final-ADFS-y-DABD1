# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 822
- Pasaron: 822
- Fallaron: 0
- Saltados: 0
- Duracion: 118.4s
- Ejecutado: 23/04/2026 18:41:39

---

## Suites

### clients.MoventClientTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall `(57ms)`

**[PASO]** notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(398ms)`

**[PASO]** notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion `(11ms)`

**[PASO]** notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall `(3ms)`

**[PASO]** notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(13ms)`

**[PASO]** notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall `(3ms)`

**[PASO]** notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall `(4ms)`

**[PASO]** notificarHabitacionCerrada_multiplesReservas_swallowsExcepcion `(12ms)`

---

### config.ServerConfigTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** metodoCreateServer_existe_esPublicoEstatico `(12ms)`

**[PASO]** claseServerConfig_existe_esPublica `(2ms)`

---

### controllers.AdminBusquedaControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleResumen_conRolAdmin_retornaResumenEstadistico `(1.8s)`

**[PASO]** handleExportar_conRolIncorrecto_retorna403YNoLlamaServicio `(9ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_aplicaFiltrosDeQueryParams `(91ms)`

**[PASO]** handleExportar_conEmailValidoSinFiltros_llamaServicioYRetornaMensaje `(4ms)`

**[PASO]** handleExportar_conEmailInvalido_retorna400ConMensaje `(2ms)`

**[PASO]** handleExportar_conEmailValidoYFiltros_llamaServicioConFiltros `(2ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_llamaServicioYRetornaResultado `(7ms)`

**[PASO]** handleListarBusquedas_conRolIncorrecto_retorna403YNoLlamaServicio `(5ms)`

**[PASO]** handleResumen_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleExportar_conEmailBlanco_retorna400ConMensaje `(3ms)`

---

### controllers.AerolineaAdminControllerTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** handleEditar_conRolIncorrecto_retorna403YNoLlamaServicio `(24ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(1ms)`

**[PASO]** handleListarLibres_conRolAdmin_retornaListaDeWebserviceLibres `(2ms)`

**[PASO]** handleEditar_conRolAdminYDatosValidos_retornaMensajeExito `(29ms)`

**[PASO]** handleEditar_conRolAdminYDatosInvalidos_retorna400ConMensaje `(15ms)`

**[PASO]** handleCrear_conRolAdminYDatosValidos_retorna201ConNuevaAerolinea `(41ms)`

**[PASO]** handleListar_conRolAdmin_retornaListaDeAerolineas `(3ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleCrear_conRolAdminYDatosInvalidos_retorna400ConMensaje `(3ms)`

**[PASO]** handleListarLibres_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

---

### controllers.AerolineaWebserviceControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleListar_conRolWebservice_retornaAerolineasDelUsuario `(20ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosInvalidos_retorna400ConMensaje `(19ms)`

**[PASO]** handleCambiarEstado_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosValidos_retorna201ConNuevaAerolinea `(2ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYEstadoInvalido_retorna400ConMensaje `(4ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYDatosValidos_retornaMensajeExito `(2ms)`

---

### controllers.AgenciaControllerTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** handleEditarAdmin_datosValidos_retornaMensaje `(111ms)`

**[PASO]** handleEliminarWebservice_rolNoWebservice_retorna403 `(3ms)`

**[PASO]** handleCrearAdmin_datosInvalidos_retorna400 `(5ms)`

**[PASO]** handleCambiarEstadoWebservice_estadoInvalido_retorna400 `(5ms)`

**[PASO]** handleListarWebservice_rolNoWebservice_retorna403 `(5ms)`

**[PASO]** handleListarAdmin_rolNoAdministrador_retorna403 `(5ms)`

**[PASO]** handleListarWebservice_rolWebservice_retornaLista `(4ms)`

**[PASO]** handleCrearWebservice_rolNoWebservice_retorna403 `(2ms)`

**[PASO]** handleCambiarEstadoWebservice_rolNoWebservice_retorna403 `(2ms)`

**[PASO]** handleEditarAdmin_agenciaNoEncontrada_retorna400 `(6ms)`

**[PASO]** handleEliminarWebservice_agenciaExistente_retornaMensaje `(4ms)`

**[PASO]** handleCrearAdmin_datosValidos_retorna201 `(4ms)`

**[PASO]** handleCrearAdmin_rolNoAdministrador_retorna403 `(5ms)`

**[PASO]** handleCrearWebservice_datosValidos_retorna201 `(5ms)`

**[PASO]** handleHandshake_agenciaNoRegistrada_retorna400 `(12ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaTodasLasAgencias `(3ms)`

**[PASO]** handleCambiarEstadoWebservice_datosValidos_retornaMensaje `(5ms)`

**[PASO]** handleEliminarWebservice_agenciaNoPertenece_retorna400 `(5ms)`

**[PASO]** handleCrearWebservice_argumentoInvalido_retorna400 `(5ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(4ms)`

**[PASO]** handleEditarAdmin_rolNoAdministrador_retorna403 `(2ms)`

---

### controllers.AuthControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleLogout_siempre_invalidaCookieYRetorna200 `(55ms)`

**[PASO]** handleLogin_credencialesValidas_emiteCookieYRetorna200 `(43ms)`

**[PASO]** handleLogin_credencialesInvalidas_retorna401 `(5ms)`

---

### controllers.BusquedaAerolineaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(59ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(3ms)`

---

### controllers.BusquedaAgenciaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(40ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(3ms)`

---

### controllers.BusquedaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_tokenValido_buscaConUsuarioId `(156ms)`

**[PASO]** handleBuscar_servicioLanzaIllegalArgument_retorna404 `(2ms)`

**[PASO]** handleBuscar_sinToken_buscaComoAnonimo `(3ms)`

---

### controllers.CancelacionAgenciaControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handlePuedeCancelar_reservacionValida_retornaResultado200 `(21ms)`

**[PASO]** handleCancelar_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handlePuedeCancelar_errorServicio_retorna500ConMensaje `(5ms)`

**[PASO]** handlePuedeCancelar_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleCancelar_motivoInvalido_retorna400ConMensaje `(35ms)`

**[PASO]** handleCancelar_motivoValido_cancelaYRetorna200 `(4ms)`

---

### controllers.CancelacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleCancelarReservacion_reservacionValida_retorna200 `(5ms)`

**[PASO]** handleCancelarReservacion_reservacionInvalida_retorna400 `(2ms)`

---

### controllers.ComentarioControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleObtenerPorHotelAgencia_authOk_retorna200ConLista `(50ms)`

**[PASO]** handleObtenerPorUsuario_usuarioConComentarios_retorna200ConLista `(2ms)`

**[PASO]** handleObtenerPorHotelAgencia_servicioLanzaExcepcion_retorna400 `(6ms)`

**[PASO]** handleObtenerPorHotel_hotelValido_retorna200ConLista `(1ms)`

**[PASO]** handleObtenerPorUsuario_usuarioSinComentarios_retorna200ConListaVacia `(1ms)`

**[PASO]** handleObtenerPorHotel_hotelSinComentarios_retorna200ConListaVacia `(1ms)`

**[PASO]** handleAgregarComentario_argumentoInvalido_retorna400 `(4ms)`

**[PASO]** handleObtenerPorHotelAgencia_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleAgregarComentario_datosValidos_retorna201 `(3ms)`

---

### controllers.DestinosControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerDestinos_conDestinosExistentes_retorna200ConLista `(20ms)`

**[PASO]** handleObtenerDestinos_sinDestinos_retorna200ConListaVacia `(2ms)`

---

### controllers.DownsControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleAgregarDown_datosValidos_retorna201ConMensaje `(40ms)`

**[PASO]** handleObtenerDowns_usuarioConDowns_retorna200ConLista `(4ms)`

**[PASO]** handleActualizarDown_datosValidos_retorna200ConMensaje `(3ms)`

**[PASO]** handleEliminarDown_downExistente_retorna200ConMensaje `(3ms)`

**[PASO]** handleObtenerDowns_usuarioSinDowns_retorna200ConListaVacia `(1ms)`

**[PASO]** handleAgregarDown_argumentoInvalido_retorna400 `(5ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelValido_retorna200ConLista `(3ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelSinDowns_retorna200ConListaVacia `(3ms)`

**[PASO]** handleActualizarDown_downNoExistente_retorna400 `(6ms)`

**[PASO]** handleEliminarDown_downNoExistente_retorna400 `(4ms)`

---

### controllers.EmailReservacionControllerTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** handleNewsletter_correoValido_enviaCorreoYRetorna200 `(45ms)`

**[PASO]** handleEnviarCorreoReservacion_reservacionNoExiste_retorna404 `(2ms)`

**[PASO]** handleEnviarCorreoReservacion_rolAutorizado_enviaCorreoYRetorna200 `(1ms)`

**[PASO]** handleContacto_camposObligatoriosFaltantes_retorna400 `(2ms)`

**[PASO]** handleEnviarCorreoReservacion_errorRuntime_retorna500 `(3ms)`

**[PASO]** handleNewsletter_correoSinArroba_retorna400 `(1ms)`

**[PASO]** handleContacto_formularioValido_enviaCorreoYRetorna200 `(24ms)`

**[PASO]** handleEnviarCorreoReservacion_rolNoAutorizado_retorna403 `(1ms)`

---

### controllers.HandshakeAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleHandshake_tokenEntradaIncorrecto_retorna400 `(17ms)`

**[PASO]** handleHandshake_servicioExitoso_noLlamaStatus `(2ms)`

**[PASO]** handleHandshake_aerolineaNoRegistrada_retorna400 `(5ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(1ms)`

---

### controllers.HotelAgenciaControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerHoteles_autenticacionFallida_noRetornaDatos `(24ms)`

**[PASO]** handleObtenerHoteles_autenticacionValida_retornaListaHoteles `(2ms)`

---

### controllers.HotelControllerTest

- Tests: 83
- Pasaron: 83
- Fallaron: 0

**[PASO]** handleReactivarHabitacion_habitacionNoEncontrada_retorna404 `(78ms)`

**[PASO]** handleEliminarHotel_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleEliminarHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEditarHotel_servicioLanzaExcepcion_retorna400 `(18ms)`

**[PASO]** handleEliminarImagenAmenidad_rolAdmin_eliminaImagenExitosamente `(3ms)`

**[PASO]** handleReservasActivasHotel_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleEliminarImagenAmenidad_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearHabitacion_servicioLanzaExcepcion_retorna400 `(17ms)`

**[PASO]** handleAgregarImagenHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleListarAmenidadesHotel_rolAdmin_retornaAmenidadesDelHotel `(1ms)`

**[PASO]** handleEliminarHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCrearAmenidad_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleListarCiudades_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearAmenidad_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarImagenHotel_rolAdmin_eliminaImagenExitosamente `(1ms)`

**[PASO]** handleAgregarAmenidadHotel_rolAdmin_agregaAmenidadYRetorna201 `(24ms)`

**[PASO]** handleListarHabitaciones_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleReservasActivasHotel_rolAdmin_retornaReservasActivas `(0ms)`

**[PASO]** handleReservasActivasHabitacion_habitacionNoEncontrada_retorna404 `(1ms)`

**[PASO]** handleEditarHabitacion_rolAdmin_editaHabitacionExitosamente `(25ms)`

**[PASO]** handleListarHabitaciones_hotelNoEncontrado_retorna404 `(1ms)`

**[PASO]** handleEliminarImagenHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEliminarAmenidadHotel_rolAdmin_eliminaAmenidadExitosamente `(1ms)`

**[PASO]** handleListarPaises_rolAdmin_retornaListaDePaises `(3ms)`

**[PASO]** handleAgregarImagenAmenidad_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarHabitacion_habitacionNoEncontrada_retorna404 `(2ms)`

**[PASO]** handleCrearAmenidad_rolAdmin_creaAmenidadYRetorna201 `(3ms)`

**[PASO]** handleCrearHabitacion_rolAdmin_creaHabitacionYRetorna201 `(2ms)`

**[PASO]** handleAgregarImagenHotel_rolAdmin_agregaImagenYRetorna201 `(15ms)`

**[PASO]** handleEliminarHotel_rolAdmin_eliminaHotelExitosamente `(1ms)`

**[PASO]** handleReactivarHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHabitacion_rolAdmin_cierraHabitacionExitosamente `(2ms)`

**[PASO]** handleListarHoteles_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleListarReservaciones_rolAdmin_retornaTodasLasReservaciones `(3ms)`

**[PASO]** handleEditarHabitacion_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleListarAmenidadesHotel_hotelNoEncontrado_retorna404 `(2ms)`

**[PASO]** handleEditarHotel_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleObtenerMetricas_rolAdmin_retornaMetricasDelSistema `(1ms)`

**[PASO]** handleCrearHotel_rolAdmin_creaHotelYRetorna201 `(21ms)`

**[PASO]** handleCerrarHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleAgregarAmenidadHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleAgregarImagenAmenidad_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleAgregarAmenidadHotel_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleCrearHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCrearHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEliminarHabitacion_rolAdmin_eliminaHabitacionExitosamente `(1ms)`

**[PASO]** handleListarAmenidadesHotel_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleCerrarHotel_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarHabitaciones_rolAdmin_retornaHabitacionesDelHotel `(1ms)`

**[PASO]** handleAgregarImagenHabitacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleEditarHotel_rolAdmin_editaHotelExitosamente `(1ms)`

**[PASO]** handleEliminarImagenHabitacion_rolAdmin_eliminaImagenExitosamente `(1ms)`

**[PASO]** handleListarAmenidades_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCancelarReservacion_rolAdminConMotivo_cancelaYRetornaRespuesta `(18ms)`

**[PASO]** handleCerrarHabitacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarHoteles_rolAdmin_retornaListaDeHoteles `(2ms)`

**[PASO]** handleReservasActivasHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleListarAmenidades_rolAdmin_retornaListaDelServicio `(1ms)`

**[PASO]** handleCrearHotel_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleReactivarHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCancelarReservacion_bodyLanzaExcepcion_usaMotivoDefault `(2ms)`

**[PASO]** handleReactivarHotel_hotelNoEncontrado_retorna404 `(2ms)`

**[PASO]** handleCancelarReservacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHotel_rolAdmin_cierraHotelExitosamente `(2ms)`

**[PASO]** handleReactivarHabitacion_rolAdmin_reactivaHabitacionExitosamente `(0ms)`

**[PASO]** handleListarCiudades_rolAdmin_retornaListaDeCiudades `(1ms)`

**[PASO]** handleEliminarImagenHotel_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleActualizarAmenidadHotel_rolAdmin_actualizaAmenidadExitosamente `(1ms)`

**[PASO]** handleAgregarImagenAmenidad_rolAdmin_agregaImagenYRetorna201 `(1ms)`

**[PASO]** handleAgregarImagenHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleAgregarImagenHabitacion_rolAdmin_agregaImagenYRetorna201 `(1ms)`

**[PASO]** handleReservasActivasHabitacion_rolAdmin_retornaReservasActivas `(1ms)`

**[PASO]** handleListarPaises_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleReactivarHotel_rolAdmin_reactivaHotelExitosamente `(0ms)`

**[PASO]** handleObtenerMetricas_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEditarHabitacion_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleEliminarAmenidadHotel_sinRolAdmin_retorna403 `(0ms)`

**[PASO]** handleAgregarImagenHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleCancelarReservacion_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleReservasActivasHotel_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleActualizarAmenidadHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleListarReservaciones_sinRolAdmin_retorna403 `(1ms)`

---

### controllers.ImagenControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handleObtenerImagenAmenidad_imagenExiste_retornaImagenJpeg `(29ms)`

**[PASO]** handleObtenerImagenHotel_imagenNoExiste_retorna404 `(1ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenExiste_retornaImagenJpeg `(1ms)`

**[PASO]** handleObtenerImagenAmenidad_imagenNoExiste_retorna404 `(0ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenNoExiste_retorna404 `(0ms)`

**[PASO]** handleObtenerImagenHotel_imagenExiste_retornaImagenJpeg `(1ms)`

---

### controllers.PagoAgenciaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleProcesarPago_authFalla_noInvocaServicio `(25ms)`

**[PASO]** handleProcesarPago_errorPasarela_retorna500ConMensaje `(30ms)`

**[PASO]** handleProcesarPago_pagoValido_retornaConfirmacion200 `(3ms)`

**[PASO]** handleProcesarPago_pagoInvalido_retorna400ConMensaje `(4ms)`

---

### controllers.PagoControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleProcesarPago_errorRuntime_retorna500 `(45ms)`

**[PASO]** handleProcesarPago_pagoExitoso_retorna200 `(1ms)`

**[PASO]** handleProcesarPago_argumentoInvalido_retorna400 `(2ms)`

---

### controllers.PdfReservacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleDescargarPdf_reservacionNoEncontrada_retorna404 `(27ms)`

**[PASO]** handleDescargarPdf_reservacionValida_retornaPdfComoAdjunto `(3ms)`

---

### controllers.ReservacionAgenciaControllerTest

- Tests: 13
- Pasaron: 13
- Fallaron: 0

**[PASO]** handleObtenerDetalleReservacion_reservacionNoEncontrada_retorna404ConMensaje `(37ms)`

**[PASO]** handleCrearReservacion_errorInterno_retorna500ConMensaje `(26ms)`

**[PASO]** handleExpirarReservacion_reservacionValida_expiraYRetornaMensaje `(3ms)`

**[PASO]** handleObtenerReservaciones_agenciaValida_retornaLista200 `(3ms)`

**[PASO]** handleExpirarReservacion_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleObtenerDetalleReservacion_errorInterno_retorna500ConMensaje `(3ms)`

**[PASO]** handleObtenerDetalleReservacion_reservacionExiste_retornaDetalle200 `(3ms)`

**[PASO]** handleCrearReservacion_datosInvalidos_retorna400ConMensaje `(2ms)`

**[PASO]** handleExpirarReservacion_reservacionInvalida_retorna400ConMensaje `(4ms)`

**[PASO]** handleObtenerDetalleReservacion_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleCrearReservacion_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleObtenerReservaciones_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleCrearReservacion_requestValido_retornaReservacion201 `(2ms)`

---

### controllers.ReservacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleCrearReservacion_datosValidos_retorna201 `(23ms)`

**[PASO]** handleObtenerReservaciones_usuarioConReservaciones_retorna200ConLista `(1ms)`

**[PASO]** handleCrearReservacion_errorRuntime_retorna500 `(1ms)`

**[PASO]** handleCrearReservacion_argumentoInvalido_retorna400 `(2ms)`

**[PASO]** handleObtenerReservaciones_usuarioSinReservaciones_retorna200ConListaVacia `(1ms)`

---

### controllers.SesionControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleSesion_tokenInvalido_retornaSinSesion `(20ms)`

**[PASO]** handleSesion_tokenValido_retornaConSesion `(8ms)`

**[PASO]** handleSesion_sinToken_retornaSinSesion `(2ms)`

---

### controllers.TokenAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleGenerarToken_authOkYDatosValidos_retorna201ConToken `(42ms)`

**[PASO]** handleGenerarToken_authOkYTokenHashNulo_servicioLlamadoConNulo `(6ms)`

**[PASO]** handleGenerarToken_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleGenerarToken_servicioLanzaExcepcion_retorna400 `(4ms)`

---

### controllers.TokenValidacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleValidar_tokenNull_retorna400SinLlamarServicio `(28ms)`

**[PASO]** handleValidar_tokenVacio_retorna400SinLlamarServicio `(2ms)`

**[PASO]** handleValidar_tokenBlanco_retorna400SinLlamarServicio `(2ms)`

**[PASO]** handleValidar_tokenValido_retorna200ConResultado `(1ms)`

**[PASO]** handleValidar_tokenExpirado_retorna400ConMensaje `(3ms)`

---

### controllers.UsuarioControllerTest

- Tests: 15
- Pasaron: 15
- Fallaron: 0

**[PASO]** handleRegistrar_camposDuplicados_retorna409 `(156ms)`

**[PASO]** handleCambiarTelefono_telefonoInvalido_retorna400 `(4ms)`

**[PASO]** handleObtenerPerfil_usuarioAutenticado_retornaPerfil `(0ms)`

**[PASO]** handleCambiarContrasena_credencialesValidas_retorna200 `(3ms)`

**[PASO]** handleCambiarRol_rolNoAutorizado_retorna403 `(3ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaLista `(2ms)`

**[PASO]** handleCambiarTelefono_telefonoValido_retorna200 `(3ms)`

**[PASO]** handleCambiarRol_adminActualiza_retorna200 `(3ms)`

**[PASO]** handleValidar_servicioRetornaFalse_retornaResultadoNoDisponible `(1ms)`

**[PASO]** handleCambiarRol_rolInvalido_retorna400 `(3ms)`

**[PASO]** handleValidar_requestValido_retornaResultado `(2ms)`

**[PASO]** handleCambiarContrasena_credencialesInvalidas_retorna401 `(13ms)`

**[PASO]** handleRegistrar_nuevoUsuario_retorna201ConId `(2ms)`

**[PASO]** handleObtenerPerfil_diferentesUsuarios_llamaServicioConIdCorrecto `(0ms)`

**[PASO]** handleListarAdmin_rolNoAutorizado_retorna403 `(2ms)`

---

### data.DataAccessExceptionTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** sePuedeLanzarYCapturar_comoRuntimeException `(4ms)`

**[PASO]** constructor_conMensajeYCausa_almacenaAmbosValores `(10ms)`

**[PASO]** mensajeDescriptivo_sePropaga_correctamente `(0ms)`

**[PASO]** causa_puedeSerSQLException_simulada `(12ms)`

**[PASO]** sePuedeCapturar_comoDataAccessException `(0ms)`

**[PASO]** esSubclase_deRuntimeException `(3ms)`

---

### data.ResultSetMapperTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** implementacion_conLambda_mapeaStringCorrectamente `(126ms)`

**[PASO]** implementacion_propagaSQLException_cuandoResultSetFalla `(5ms)`

**[PASO]** implementacion_conLambda_construyeObjetoCompuesto `(2ms)`

**[PASO]** implementacion_conLambda_mapeaDoubleCorrectamente `(1ms)`

**[PASO]** implementacion_conLambda_mapeaEnteroCorrectamente `(1ms)`

---

### dtos.DtosTest

- Tests: 78
- Pasaron: 78
- Fallaron: 0

**[PASO]** puedeCancelarDTO_constructorFalse_almacenaCorrecto `(3ms)`

**[PASO]** crearHotelRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_constructor_almacenaBooleans `(1ms)`

**[PASO]** tokenValidacionResponseDTO_constructor_almacenaTodosLosCampos `(1ms)`

**[PASO]** aerolineaIdentidadDTO_constructor_almacenaNombreYUrl `(4ms)`

**[PASO]** cambiarContrasenaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** editarHotelRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** hotelAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** habitacionAgenciaDTO_settersYGetters_funcionan `(9ms)`

**[PASO]** reservacionRequestDTO_setterHabitaciones_funciona `(1ms)`

**[PASO]** hotelResultadoDTO_listas_seAsignanCorrectamente `(4ms)`

**[PASO]** reservacionResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** editarAerolineaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** crearAerolineaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** loginResponseDTO_constructor_almacenaTodosLosCampos `(0ms)`

**[PASO]** cambiarRolRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** usuarioAdminDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_setter_sobrescribeToken `(0ms)`

**[PASO]** crearAerolineaAdminRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** ciudadDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** reservacionDetalleDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** paisDTO_constructor_almacenaIdYNombre `(0ms)`

**[PASO]** downResponseDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agregarAmenidadRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** comentarioRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** cancelacionRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** paisDTO_setters_sobrescribenValores `(0ms)`

**[PASO]** reservacionAgenciaResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** usuarioValidacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** tokenAerolineaResponseDTO_constructor_almacenaTodosLosCampos `(1ms)`

**[PASO]** busquedaRequestDTO_valoresPorDefecto_sonNullOCero `(0ms)`

**[PASO]** downRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** downRequestDTO_valorNegativo_seAlmacenaCorrectamente `(1ms)`

**[PASO]** usuarioPerfilResponseDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** tokenValidacionResponseDTO_porcentajeCero_seAlmacenaCorrectamente `(0ms)`

**[PASO]** editarAgenciaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** resultadoNotificacionDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** handshakeRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** tokenAerolineaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaAdminDTO_valoresPorDefecto_sonCero `(1ms)`

**[PASO]** comentarioResponseDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionAgenciaResponseDTO_settersYGetters_funcionan `(5ms)`

**[PASO]** reservacionRequestDTO_listaVacia_seAsignaCorrectamente `(0ms)`

**[PASO]** habitacionAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** pagoRequestDTO_settersYGetters_facturacion_funcionan `(0ms)`

**[PASO]** cambiarTelefonoRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** usuarioWebserviceLibreDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** sesionDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** loginRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaIdentidad_settersYGetters_funcionan `(4ms)`

**[PASO]** tipoHabitacionResultadoDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** loginResponseDTO_diferencteRol_seAlmacenaCorrectamente `(0ms)`

**[PASO]** busquedaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** amenidadDTO_setters_sobrescribenValores `(1ms)`

**[PASO]** hotelAmenidadDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** editarHabitacionRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionReservaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** crearAgenciaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** sesionDTO_autenticadoFalsePorDefecto `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_todosFalse_cuandoNingunExiste `(0ms)`

**[PASO]** crearAgenciaAdminRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** amenidadHotelDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** hotelAgenciaDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** hotelResultadoDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaWebserviceDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** puedeCancelarDTO_constructorTrue_almacenaCorrecto `(1ms)`

**[PASO]** habitacionResumenDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** crearHabitacionRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** handshakeResponseDTO_constructor_almacenaToken `(0ms)`

**[PASO]** amenidadDTO_constructor_almacenaIdYNombre `(0ms)`

**[PASO]** pagoAgenciaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** subirImagenRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** loginRequestDTO_valoresPorDefecto_sonNull `(0ms)`

**[PASO]** habitacionDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaDTO_valoresPorDefecto_sonCero `(0ms)`

**[PASO]** pagoResponseDTO_settersYGetters_funcionan `(1ms)`

---

### helpers.AerolineaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(37ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAerolinea `(1ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(3ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(2ms)`

---

### helpers.AgenciaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(41ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(3ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAgencia `(1ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(2ms)`

---

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(8ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(1ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(3ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(1ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(6ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(1ms)`

---

### helpers.EmailHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** enviar_datosValidos_noLanzaExcepcion `(4ms)`

**[PASO]** enviar_cuerpoMinimo_noLanzaExcepcion `(5ms)`

**[PASO]** enviar_argumentosExactos_invocaMetodoConParametrosCorrectos `(3ms)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(1.3s)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(318ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(1ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(2ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(2ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(0ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(1ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(1ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(1ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(1ms)`

**[PASO]** getRolId_retornaRolCorrecto `(3ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(2ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(302ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(441ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(435ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(433ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(302ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(35ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(4ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(3ms)`

**[PASO]** validar_numeroConEspacios_esValido `(6ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(1ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(0ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(0ms)`

**[PASO]** validar_cvv4digitos_esValido `(2ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(1ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(2ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(3ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(1ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(0ms)`

---

### models.UsuarioModelTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** ciudadId_aceptaNull `(6ms)`

**[PASO]** valoresPorDefecto_sonNullOCero `(0ms)`

**[PASO]** reasignarUsername_noAfectaOtrosCampos `(0ms)`

**[PASO]** setterYGetter_fechaNacimiento_funciona `(0ms)`

**[PASO]** ciudadId_conValor_seAlmacenaCorrectamente `(0ms)`

**[PASO]** constructorVacio_creaInstanciaNoNula `(1ms)`

**[PASO]** settersYGetters_camposNumericos_funcionan `(0ms)`

**[PASO]** settersYGetters_camposTexto_funcionan `(1ms)`

---

### repositories.AdminReservacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarTodas_retornaListaConReservacionDePrueba `(2.2s)`

**[PASO]** obtenerReservacion_idExistente_retornaDatos `(1.3s)`

**[PASO]** obtenerReservacion_idInexistente_retornaNull `(1.2s)`

**[PASO]** obtenerDatosUsuarioPorReservacion_reservacionExistente_retornaDatos `(1.2s)`

**[PASO]** cancelarReservacion_reservacionPendiente_cambiaEstado `(1.2s)`

---

### repositories.AerolineaAdminRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarTodas_retornaListaNoNula `(613ms)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(1.1s)`

**[PASO]** editar_aerolineaExistente_actualizaDatos `(643ms)`

**[PASO]** listarWebserviceLibres_retornaListaNoNula `(589ms)`

---

### repositories.AerolineaAliadaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerAerolineaPorToken_tokenActivo_retornaDto `(716ms)`

**[PASO]** obtenerAerolineaPorToken_tokenInexistente_retornaNull `(722ms)`

**[PASO]** obtenerDescuentoAerolinea_tokenActivo_retornaDescuentoPositivo `(700ms)`

**[PASO]** buscarCiudadId_ciudadExistente_retornaId `(713ms)`

**[PASO]** guardarBusqueda_datosValidos_persisteEnOracle `(713ms)`

**[PASO]** obtenerAerolineaIdPorURL_urlExistente_retornaId `(686ms)`

**[PASO]** guardarTokensAerolinea_datosValidos_actualizaToken `(735ms)`

---

### repositories.AerolineaWebserviceRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAerolinea_retornaListaConAlMenosUna `(515ms)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(952ms)`

**[PASO]** cambiarEstado_aerolineaActiva_actualizaEstado `(604ms)`

**[PASO]** listarPorUsuario_usuarioSinAerolinea_retornaListaVacia `(529ms)`

---

### repositories.AgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAgencia_retornaListaConAlMenosUna `(560ms)`

**[PASO]** crear_datosValidos_retornaAgenciaConId `(986ms)`

**[PASO]** editar_datosNuevos_actualizaNombre `(1.1s)`

**[PASO]** cambiarEstado_agenciaActiva_cambiaEstado `(662ms)`

**[PASO]** obtenerAgenciaPorToken_sinToken_retornaNull `(531ms)`

---

### repositories.AuthRepositoryIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** buscarPorIdentificador_porUsername_retornaUsuario `(333ms)`

**[PASO]** buscarPorIdentificador_porCorreo_retornaUsuario `(352ms)`

**[PASO]** buscarPorIdentificador_identificadorInexistente_retornaNull `(339ms)`

---

### repositories.BusquedaAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(518ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(514ms)`

**[PASO]** obtenerDescuentoAgencia_usuarioConAgencia_retornaDescuento `(482ms)`

**[PASO]** guardarBusqueda_datosValidos_noLanzaExcepcion `(587ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(503ms)`

---

### repositories.BusquedaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(139ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(116ms)`

**[PASO]** guardarBusqueda_sinUsuario_noLanzaExcepcion `(157ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(127ms)`

**[PASO]** buscarImagenesHotel_hotelInexistente_retornaListaVacia `(112ms)`

**[PASO]** buscarAmenidadesHotel_hotelInexistente_retornaListaVacia `(113ms)`

**[PASO]** buscarImagenesHabitacion_habitacionInexistente_retornaListaVacia `(113ms)`

---

### repositories.CancelacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaCancelar_reservacionDelUsuario_retornaDatos `(432ms)`

**[PASO]** obtenerReservacionParaCancelar_otroUsuarioId_retornaNull `(436ms)`

**[PASO]** obtenerFechaCheckInMasReciente_sinDetalles_retornaNull `(402ms)`

**[PASO]** cancelarReservacion_estadoPendiente_actualizaAEstado4 `(483ms)`

**[PASO]** obtenerReservacionAgenciaParaCancelar_sinAgenciaVinculada_retornaNull `(414ms)`

---

### repositories.ComentarioRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** existeComentarioConResena_sinComentarios_retornaFalse `(553ms)`

**[PASO]** crearComentario_conResena_retornaIdPositivo `(541ms)`

**[PASO]** existeComentarioConResena_despuesDeCrear_retornaTrue `(591ms)`

**[PASO]** crearComentario_sinResena_esRespuestaAOtro `(576ms)`

**[PASO]** actualizarRatingHotel_conResena_noLanzaExcepcion `(591ms)`

**[PASO]** obtenerComentario_comentarioExistente_retornaDtoConDatos `(552ms)`

**[PASO]** obtenerComentariosPorUsuario_retornaListaConAlMenosUno `(576ms)`

**[PASO]** obtenerComentariosPorHotel_retornaListaConAlMenosUno `(609ms)`

---

### repositories.DestinosRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerTodosLosHoteles_retornaListaNoNula `(49ms)`

**[PASO]** obtenerTodosLosHoteles_conHotelesActivos_retornaDtosValidos `(50ms)`

**[PASO]** obtenerImagenesHotel_hotelExistente_retornaListaNoNula `(86ms)`

**[PASO]** obtenerImagenesHotel_hotelInexistente_retornaListaVacia `(47ms)`

---

### repositories.DownsRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerValorDown_sinDown_retornaNull `(811ms)`

**[PASO]** insertarDown_registraDown_obtenibleEnOracle `(849ms)`

**[PASO]** obtenerDownsDeUsuario_trasInsertarDown_retornaListaConDown `(866ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_filtraPorHotel `(899ms)`

**[PASO]** actualizarContadorDown_incrementaContador `(868ms)`

**[PASO]** eliminarDown_eliminaDown_obtenerValorRetornaNull `(862ms)`

---

### repositories.HotelRepositoryIntegrationTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** listarAmenidades_retornaListaNoNula `(533ms)`

**[PASO]** crearAmenidad_nombreValido_retornaIdPositivo `(622ms)`

**[PASO]** listarTodos_retornaListaConElHotelInsertado `(483ms)`

**[PASO]** actualizarHotel_datosNuevos_actualizaNombreEnOracle `(568ms)`

**[PASO]** cerrarHotel_hotelActivo_cambiaEstadoId `(564ms)`

**[PASO]** reactivarHotel_hotelCerrado_restauraEstadoId `(655ms)`

**[PASO]** existe_hotelExistente_retornaTrue `(571ms)`

**[PASO]** crearHabitacion_datosValidos_retornaIdPositivo `(700ms)`

**[PASO]** obtenerMetricas_retornaMapaConClaves `(760ms)`

---

### repositories.ImagenRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerImagenHotel_idExistente_retornaBytes `(297ms)`

**[PASO]** obtenerImagenHotel_idInexistente_retornaNull `(311ms)`

**[PASO]** obtenerImagenHabitacion_idExistente_retornaBytes `(307ms)`

**[PASO]** obtenerImagenHabitacion_idInexistente_retornaNull `(304ms)`

**[PASO]** eliminarImagenHotel_eliminaImagen_noObtenible `(295ms)`

**[PASO]** eliminarImagenHabitacion_eliminaImagen_noObtenible `(286ms)`

**[PASO]** obtenerImagenAmenidad_idInexistente_retornaNull `(301ms)`

---

### repositories.PagoAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDeAgencia_retornaDatos `(970ms)`

**[PASO]** obtenerReservacionParaPago_agenciaIncorrecta_retornaNull `(926ms)`

**[PASO]** confirmarReservacion_estadoPendiente_actualizaAEstado2 `(971ms)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(1s)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(1s)`

---

### repositories.PagoRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDelUsuario_retornaDatosCorrectos `(852ms)`

**[PASO]** obtenerReservacionParaPago_otroUsuarioId_retornaNull `(909ms)`

**[PASO]** confirmarReservacion_estadoPendiente_cambiaAEstado2 `(940ms)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(953ms)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(1s)`

**[PASO]** actualizarTotalReservacion_nuevoTotal_actualizaCorrectamente `(931ms)`

**[PASO]** obtenerCiudadReservacion_conDetallesYHotel_retornaNombreCiudad `(901ms)`

---

### repositories.ReservacionAgenciaRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerDescuentoAgencia_agenciaConDescuento_retornaValorPositivo `(1.3s)`

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(1.3s)`

**[PASO]** existeTraslape_fechasSinConflicto_retornaFalse `(1.2s)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(1.2s)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatos `(1.2s)`

**[PASO]** expirarReservacion_reservacionPendiente_actualizaEstado `(1.3s)`

---

### repositories.ReservacionRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(912ms)`

**[PASO]** existeTraslape_sinReservacionesConflicto_retornaFalse `(921ms)`

**[PASO]** existeTraslape_conReservacionConflicto_retornaTrue `(898ms)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(902ms)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatosCorrectos `(835ms)`

**[PASO]** obtenerReservacionesDeUsuario_conDetalle_retornaListaConAlMenosUno `(867ms)`

**[PASO]** expirarReservacionesVencidas_noLanzaExcepcion_retornaEntero `(913ms)`

**[PASO]** obtenerImagenesHotel_hotelSinImagenes_retornaListaVacia `(896ms)`

---

### repositories.UsuarioRepositoryIntegrationTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** existeUsername_usernameExistente_retornaTrue `(391ms)`

**[PASO]** existeUsername_usernameInexistente_retornaFalse `(388ms)`

**[PASO]** existeCorreo_correoExistente_retornaTrue `(392ms)`

**[PASO]** existePasaporte_pasaporteExistente_retornaTrue `(402ms)`

**[PASO]** existePasaporte_pasaporteNuloOVacio_retornaFalse `(342ms)`

**[PASO]** crearUsuario_datosValidos_retornaIdPositivo `(663ms)`

**[PASO]** obtenerPerfil_usuarioExistente_retornaDtoConDatos `(402ms)`

**[PASO]** actualizarTelefono_usuarioExistente_cambiaElCampo `(445ms)`

**[PASO]** obtenerContrasena_usuarioExistente_retornaHashNoNulo `(390ms)`

**[PASO]** listarTodosConRol_retornaListaNoNula `(384ms)`

---

### services.AdminBusquedaServiceIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarSinFiltrosRetornaResultadosYTotalCorrecto `(653ms)`

**[PASO]** listarConFiltroDestinoFiltraCorrectamente `(635ms)`

**[PASO]** listarConTipoWebRetornaSoloBusquedasWeb `(628ms)`

**[PASO]** listarPaginacionRetornaSegundaPagina `(719ms)`

**[PASO]** resumenRetornaEstructuraCompletaDesdeOracle `(718ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(37ms)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(0ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(1ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(1ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(1ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(1ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(3.2s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(1ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(3.1s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(2ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(1ms)`

---

### services.AdminReservacionServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(63ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(2ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacionYNotificaAgencia `(0ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacionYNotificaAgencia `(1ms)`

**[PASO]** cancelarReservacion_notificadorRetornaErrorHTTP_completaCancelacionConError `(1ms)`

**[PASO]** cancelarReservacion_notificadorRetornaNoEsReservaAgencia_completaCancelacion `(1ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(2ms)`

---

### services.AerolineaAdminServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarWebserviceLibres_conUsuariosDisponibles_retornaListaDelRepo `(37ms)`

**[PASO]** editar_requestValido_delegaAlRepo `(1ms)`

**[PASO]** listarTodas_conAerolineas_retornaListaDelRepo `(0ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(1ms)`

**[PASO]** listarWebserviceLibres_todosAsignados_retornaListaVacia `(0ms)`

**[PASO]** listarTodas_sinAerolineas_retornaListaVacia `(0ms)`

**[PASO]** editar_idDistinto_invocaRepoConIdCorrecto `(2ms)`

---

### services.AerolineaWebserviceServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioSinAerolineas_retornaListaVacia `(32ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(1ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentExceptionSinInvocarRepo `(1ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(0ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(1ms)`

**[PASO]** listarPorUsuario_usuarioConAerolineas_retornaListaDelRepo `(0ms)`

---

### services.AgenciaNotificadorExternoServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** notificarCancelacion_agenciaSinToken_retornaErrorSinHTTP `(118ms)`

**[PASO]** notificarCancelacion_httpError500_retornaEnviadoTrueConStatus500 `(12ms)`

**[PASO]** notificarCancelacion_agenciaSinURL_retornaErrorSinHTTP `(3ms)`

**[PASO]** notificarCancelacion_httpExitoso200_retornaEnviadoTrueYStatusCorrecto `(3ms)`

**[PASO]** notificarCancelacion_excepcionRed_retornaErrorEnDTOSinPropagar `(6ms)`

**[PASO]** notificarCancelacion_noEsReservaDeAgencia_retornaEsReservaFalseYSinHTTP `(2ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(1ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(1ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(1ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodas_retornaListaCompleta `(1ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(0ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(1ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(0ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(0ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(1ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(625ms)`

**[PASO]** loginExitosoConCorreo `(596ms)`

**[PASO]** loginFallaUsuarioInexistente `(362ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(601ms)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(475ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(447ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(1ms)`

---

### services.BusquedaAerolineaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_guardaBusquedaEnRepo `(1ms)`

**[PASO]** buscar_conDescuento10Porciento_aplicaDescuentoAPrecios `(2ms)`

**[PASO]** buscar_tokenInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscar_dosHotelesEnCiudad_retornaListaConDosHoteles `(1ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_retornaListaDeHoteles `(0ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(32ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(1ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(1ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(1ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(0ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(1ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(2.4s)`

**[PASO]** busquedaRegistraEventoEnOracle `(2.5s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(2.6s)`

**[PASO]** busquedaFallaCiudadInexistente `(447ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(35ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(1ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(0ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(2ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(1ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(1ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(695ms)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(586ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(706ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(27ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(1ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(1ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(0ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(1ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(0ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(0ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(1ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(0ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(0ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(0ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(1ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(0ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(27ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(0ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(0ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(0ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(0ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(0ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(0ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(66ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(1ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(1ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(0ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(30ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(0ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(1ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(1ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(1ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(1ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(1ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(0ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(1ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(1ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(2ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(34ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(3.3s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(1ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(2ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(29ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(0ms)`

**[PASO]** iniciar_noLanzaExcepcion `(1ms)`

**[PASO]** detener_noLanzaExcepcion `(0ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(1ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(0ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(1ms)`

---

### services.HandshakeAerolineaServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarHandshake_urlRegistrada_retornaResponseDTOConTokenDeSalida `(1ms)`

**[PASO]** procesarHandshake_tokensNoPersistidos_lanzaIllegalArgumentException `(4ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(1ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(29ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(1ms)`

---

### services.HotelServiceTest

- Tests: 69
- Pasaron: 69
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(93ms)`

**[PASO]** agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(2ms)`

**[PASO]** editarHabitacion_tipoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** eliminarAmenidadHotel_delegaAlRepositorio `(1ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero `(1ms)`

**[PASO]** agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodasReservaciones_delegaAlRepositorio `(0ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarImagenHabitacion_delegaAlRepositorio `(1ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarPaises_delegaAlRepositorioDePaises `(0ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva `(2ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerMetricas_delegaAlRepositorio `(0ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo `(1ms)`

**[PASO]** cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra `(6ms)`

**[PASO]** listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarImagenHabitacion_habitacionExiste_retornaMapaConId `(3ms)`

**[PASO]** reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero `(1ms)`

**[PASO]** agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarHabitaciones_hotelExiste_retornaListaConImagenes `(0ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos `(1ms)`

**[PASO]** editarHabitacion_habitacionExiste_invocaActualizarHabitacion `(3ms)`

**[PASO]** eliminarImagenAmenidad_delegaAlRepositorio `(2ms)`

**[PASO]** eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar `(2ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(1ms)`

**[PASO]** listarCiudades_delegaAlRepositorioDePaises `(2ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(1ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(1ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra `(6ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion `(1ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel `(1ms)`

**[PASO]** agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarImagenHotel_delegaAlRepositorio `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar `(1ms)`

**[PASO]** agregarImagenAmenidad_base64Valido_retornaMapaConId `(1ms)`

**[PASO]** obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(2ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos `(1ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarAmenidadHotel_delegaAlRepositorio `(2ms)`

**[PASO]** reactivarHabitacion_habitacionExiste_invocaReactivar `(1ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** reactivarHotel_hotelExiste_invocaReactivarHotel `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException `(2ms)`

**[PASO]** editarHabitacion_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarImagenHotel_hotelExiste_retornaMapaConId `(3ms)`

**[PASO]** listarAmenidades_delegaAlRepositorio `(1ms)`

**[PASO]** eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(45ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(1ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(0ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(32ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(0ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(803ms)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(574ms)`

**[PASO]** pagoFallaCvvInvalido `(594ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(50ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(1ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(1ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(1ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(1ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(25ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(2ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(34ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(1ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(0ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(2ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(1ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(1ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(750ms)`

**[PASO]** crearReservacionFallaSinHabitaciones `(378ms)`

**[PASO]** crearReservacionFallaFechaPasada `(384ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(0ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(0ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(0ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(1ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(26ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(0ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(1ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(0ms)`

---

### services.TokenAerolineaServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** generarToken_datosValidos_insertaTokenConIdsCorrectos `(27ms)`

**[PASO]** generarToken_tokenValidoCiudadExistente_retornaResponseDTOCompleto `(2ms)`

**[PASO]** generarToken_ciudadNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** generarToken_tokenInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** generarToken_datosValidos_urlRedireccionContieneUrlBaseYToken `(0ms)`

---

### services.TokenValidacionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** validar_tokenInexistente_lanzaIllegalArgumentException `(1ms)`

**[PASO]** validar_tokenExpirado_lanzaIllegalArgumentException `(0ms)`

**[PASO]** validar_tokenValido_retornaTokenValidacionResponseDTO `(0ms)`

**[PASO]** validar_tokenYaUtilizado_lanzaIllegalArgumentException `(0ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(84ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(1ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(0ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(0ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(1ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(0ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(1ms)`

**[PASO]** validarDisponibilidad_todosLibres `(0ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(448ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(0ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(2.6s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(1ms)`

---
