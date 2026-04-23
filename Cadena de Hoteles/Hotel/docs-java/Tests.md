# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 822
- Pasaron: 822
- Fallaron: 0
- Saltados: 0
- Duracion: 162.4s
- Ejecutado: 23/04/2026 09:23:56

---

## Suites

### clients.MoventClientTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall `(81ms)`

**[PASO]** notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(956ms)`

**[PASO]** notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion `(40ms)`

**[PASO]** notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall `(3ms)`

**[PASO]** notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(110ms)`

**[PASO]** notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall `(2ms)`

**[PASO]** notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall `(2ms)`

**[PASO]** notificarHabitacionCerrada_multiplesReservas_swallowsExcepcion `(97ms)`

---

### config.ServerConfigTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** metodoCreateServer_existe_esPublicoEstatico `(16ms)`

**[PASO]** claseServerConfig_existe_esPublica `(2ms)`

---

### controllers.AdminBusquedaControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleResumen_conRolAdmin_retornaResumenEstadistico `(5s)`

**[PASO]** handleExportar_conRolIncorrecto_retorna403YNoLlamaServicio `(11ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_aplicaFiltrosDeQueryParams `(100ms)`

**[PASO]** handleExportar_conEmailValidoSinFiltros_llamaServicioYRetornaMensaje `(5ms)`

**[PASO]** handleExportar_conEmailInvalido_retorna400ConMensaje `(4ms)`

**[PASO]** handleExportar_conEmailValidoYFiltros_llamaServicioConFiltros `(4ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_llamaServicioYRetornaResultado `(8ms)`

**[PASO]** handleListarBusquedas_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleResumen_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleExportar_conEmailBlanco_retorna400ConMensaje `(4ms)`

---

### controllers.AerolineaAdminControllerTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** handleEditar_conRolIncorrecto_retorna403YNoLlamaServicio `(59ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleListarLibres_conRolAdmin_retornaListaDeWebserviceLibres `(4ms)`

**[PASO]** handleEditar_conRolAdminYDatosValidos_retornaMensajeExito `(28ms)`

**[PASO]** handleEditar_conRolAdminYDatosInvalidos_retorna400ConMensaje `(7ms)`

**[PASO]** handleCrear_conRolAdminYDatosValidos_retorna201ConNuevaAerolinea `(25ms)`

**[PASO]** handleListar_conRolAdmin_retornaListaDeAerolineas `(4ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCrear_conRolAdminYDatosInvalidos_retorna400ConMensaje `(5ms)`

**[PASO]** handleListarLibres_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

---

### controllers.AerolineaWebserviceControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleListar_conRolWebservice_retornaAerolineasDelUsuario `(25ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosInvalidos_retorna400ConMensaje `(27ms)`

**[PASO]** handleCambiarEstado_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosValidos_retorna201ConNuevaAerolinea `(5ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYEstadoInvalido_retorna400ConMensaje `(4ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(7ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(5ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYDatosValidos_retornaMensajeExito `(6ms)`

---

### controllers.AgenciaControllerTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** handleEditarAdmin_datosValidos_retornaMensaje `(151ms)`

**[PASO]** handleEliminarWebservice_rolNoWebservice_retorna403 `(3ms)`

**[PASO]** handleCrearAdmin_datosInvalidos_retorna400 `(6ms)`

**[PASO]** handleCambiarEstadoWebservice_estadoInvalido_retorna400 `(7ms)`

**[PASO]** handleListarWebservice_rolNoWebservice_retorna403 `(5ms)`

**[PASO]** handleListarAdmin_rolNoAdministrador_retorna403 `(4ms)`

**[PASO]** handleListarWebservice_rolWebservice_retornaLista `(5ms)`

**[PASO]** handleCrearWebservice_rolNoWebservice_retorna403 `(3ms)`

**[PASO]** handleCambiarEstadoWebservice_rolNoWebservice_retorna403 `(4ms)`

**[PASO]** handleEditarAdmin_agenciaNoEncontrada_retorna400 `(5ms)`

**[PASO]** handleEliminarWebservice_agenciaExistente_retornaMensaje `(5ms)`

**[PASO]** handleCrearAdmin_datosValidos_retorna201 `(3ms)`

**[PASO]** handleCrearAdmin_rolNoAdministrador_retorna403 `(4ms)`

**[PASO]** handleCrearWebservice_datosValidos_retorna201 `(5ms)`

**[PASO]** handleHandshake_agenciaNoRegistrada_retorna400 `(8ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaTodasLasAgencias `(3ms)`

**[PASO]** handleCambiarEstadoWebservice_datosValidos_retornaMensaje `(4ms)`

**[PASO]** handleEliminarWebservice_agenciaNoPertenece_retorna400 `(6ms)`

**[PASO]** handleCrearWebservice_argumentoInvalido_retorna400 `(5ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(4ms)`

**[PASO]** handleEditarAdmin_rolNoAdministrador_retorna403 `(5ms)`

---

### controllers.AuthControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleLogout_siempre_invalidaCookieYRetorna200 `(59ms)`

**[PASO]** handleLogin_credencialesValidas_emiteCookieYRetorna200 `(57ms)`

**[PASO]** handleLogin_credencialesInvalidas_retorna401 `(3ms)`

---

### controllers.BusquedaAerolineaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(69ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(3ms)`

---

### controllers.BusquedaAgenciaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(58ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(5ms)`

---

### controllers.BusquedaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_tokenValido_buscaConUsuarioId `(178ms)`

**[PASO]** handleBuscar_servicioLanzaIllegalArgument_retorna404 `(3ms)`

**[PASO]** handleBuscar_sinToken_buscaComoAnonimo `(3ms)`

---

### controllers.CancelacionAgenciaControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handlePuedeCancelar_reservacionValida_retornaResultado200 `(27ms)`

**[PASO]** handleCancelar_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handlePuedeCancelar_errorServicio_retorna500ConMensaje `(5ms)`

**[PASO]** handlePuedeCancelar_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleCancelar_motivoInvalido_retorna400ConMensaje `(28ms)`

**[PASO]** handleCancelar_motivoValido_cancelaYRetorna200 `(4ms)`

---

### controllers.CancelacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleCancelarReservacion_reservacionValida_retorna200 `(6ms)`

**[PASO]** handleCancelarReservacion_reservacionInvalida_retorna400 `(3ms)`

---

### controllers.ComentarioControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleObtenerPorHotelAgencia_authOk_retorna200ConLista `(61ms)`

**[PASO]** handleObtenerPorUsuario_usuarioConComentarios_retorna200ConLista `(4ms)`

**[PASO]** handleObtenerPorHotelAgencia_servicioLanzaExcepcion_retorna400 `(5ms)`

**[PASO]** handleObtenerPorHotel_hotelValido_retorna200ConLista `(2ms)`

**[PASO]** handleObtenerPorUsuario_usuarioSinComentarios_retorna200ConListaVacia `(2ms)`

**[PASO]** handleObtenerPorHotel_hotelSinComentarios_retorna200ConListaVacia `(5ms)`

**[PASO]** handleAgregarComentario_argumentoInvalido_retorna400 `(6ms)`

**[PASO]** handleObtenerPorHotelAgencia_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleAgregarComentario_datosValidos_retorna201 `(4ms)`

---

### controllers.DestinosControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerDestinos_conDestinosExistentes_retorna200ConLista `(27ms)`

**[PASO]** handleObtenerDestinos_sinDestinos_retorna200ConListaVacia `(2ms)`

---

### controllers.DownsControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleAgregarDown_datosValidos_retorna201ConMensaje `(55ms)`

**[PASO]** handleObtenerDowns_usuarioConDowns_retorna200ConLista `(2ms)`

**[PASO]** handleActualizarDown_datosValidos_retorna200ConMensaje `(4ms)`

**[PASO]** handleEliminarDown_downExistente_retorna200ConMensaje `(4ms)`

**[PASO]** handleObtenerDowns_usuarioSinDowns_retorna200ConListaVacia `(2ms)`

**[PASO]** handleAgregarDown_argumentoInvalido_retorna400 `(3ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelValido_retorna200ConLista `(2ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelSinDowns_retorna200ConListaVacia `(4ms)`

**[PASO]** handleActualizarDown_downNoExistente_retorna400 `(5ms)`

**[PASO]** handleEliminarDown_downNoExistente_retorna400 `(4ms)`

---

### controllers.EmailReservacionControllerTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** handleNewsletter_correoValido_enviaCorreoYRetorna200 `(105ms)`

**[PASO]** handleEnviarCorreoReservacion_reservacionNoExiste_retorna404 `(5ms)`

**[PASO]** handleEnviarCorreoReservacion_rolAutorizado_enviaCorreoYRetorna200 `(3ms)`

**[PASO]** handleContacto_camposObligatoriosFaltantes_retorna400 `(2ms)`

**[PASO]** handleEnviarCorreoReservacion_errorRuntime_retorna500 `(3ms)`

**[PASO]** handleNewsletter_correoSinArroba_retorna400 `(2ms)`

**[PASO]** handleContacto_formularioValido_enviaCorreoYRetorna200 `(20ms)`

**[PASO]** handleEnviarCorreoReservacion_rolNoAutorizado_retorna403 `(3ms)`

---

### controllers.HandshakeAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleHandshake_tokenEntradaIncorrecto_retorna400 `(25ms)`

**[PASO]** handleHandshake_servicioExitoso_noLlamaStatus `(2ms)`

**[PASO]** handleHandshake_aerolineaNoRegistrada_retorna400 `(6ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(3ms)`

---

### controllers.HotelAgenciaControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerHoteles_autenticacionFallida_noRetornaDatos `(36ms)`

**[PASO]** handleObtenerHoteles_autenticacionValida_retornaListaHoteles `(2ms)`

---

### controllers.HotelControllerTest

- Tests: 83
- Pasaron: 83
- Fallaron: 0

**[PASO]** handleReactivarHabitacion_habitacionNoEncontrada_retorna404 `(119ms)`

**[PASO]** handleEliminarHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleEliminarHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleEditarHotel_servicioLanzaExcepcion_retorna400 `(28ms)`

**[PASO]** handleEliminarImagenAmenidad_rolAdmin_eliminaImagenExitosamente `(3ms)`

**[PASO]** handleReservasActivasHotel_hotelNoEncontrado_retorna404 `(2ms)`

**[PASO]** handleEliminarImagenAmenidad_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCrearHabitacion_servicioLanzaExcepcion_retorna400 `(22ms)`

**[PASO]** handleAgregarImagenHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleListarAmenidadesHotel_rolAdmin_retornaAmenidadesDelHotel `(3ms)`

**[PASO]** handleEliminarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCerrarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearAmenidad_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarCiudades_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearAmenidad_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarImagenHotel_rolAdmin_eliminaImagenExitosamente `(4ms)`

**[PASO]** handleAgregarAmenidadHotel_rolAdmin_agregaAmenidadYRetorna201 `(26ms)`

**[PASO]** handleListarHabitaciones_sinRolAdmin_retorna403 `(4ms)`

**[PASO]** handleReservasActivasHotel_rolAdmin_retornaReservasActivas `(2ms)`

**[PASO]** handleReservasActivasHabitacion_habitacionNoEncontrada_retorna404 `(3ms)`

**[PASO]** handleEditarHabitacion_rolAdmin_editaHabitacionExitosamente `(30ms)`

**[PASO]** handleListarHabitaciones_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleEliminarImagenHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleEliminarAmenidadHotel_rolAdmin_eliminaAmenidadExitosamente `(3ms)`

**[PASO]** handleListarPaises_rolAdmin_retornaListaDePaises `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarHabitacion_habitacionNoEncontrada_retorna404 `(5ms)`

**[PASO]** handleCrearAmenidad_rolAdmin_creaAmenidadYRetorna201 `(3ms)`

**[PASO]** handleCrearHabitacion_rolAdmin_creaHabitacionYRetorna201 `(3ms)`

**[PASO]** handleAgregarImagenHotel_rolAdmin_agregaImagenYRetorna201 `(26ms)`

**[PASO]** handleEliminarHotel_rolAdmin_eliminaHotelExitosamente `(2ms)`

**[PASO]** handleReactivarHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHabitacion_rolAdmin_cierraHabitacionExitosamente `(2ms)`

**[PASO]** handleListarHoteles_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleListarReservaciones_rolAdmin_retornaTodasLasReservaciones `(2ms)`

**[PASO]** handleEditarHabitacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarAmenidadesHotel_hotelNoEncontrado_retorna404 `(4ms)`

**[PASO]** handleEditarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleObtenerMetricas_rolAdmin_retornaMetricasDelSistema `(2ms)`

**[PASO]** handleCrearHotel_rolAdmin_creaHotelYRetorna201 `(27ms)`

**[PASO]** handleCerrarHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleAgregarAmenidadHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleAgregarAmenidadHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleCrearHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarHabitacion_rolAdmin_eliminaHabitacionExitosamente `(1ms)`

**[PASO]** handleListarAmenidadesHotel_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleCerrarHotel_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleListarHabitaciones_rolAdmin_retornaHabitacionesDelHotel `(16ms)`

**[PASO]** handleAgregarImagenHabitacion_servicioLanzaExcepcion_retorna400 `(5ms)`

**[PASO]** handleEditarHotel_rolAdmin_editaHotelExitosamente `(3ms)`

**[PASO]** handleEliminarImagenHabitacion_rolAdmin_eliminaImagenExitosamente `(3ms)`

**[PASO]** handleListarAmenidades_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCancelarReservacion_rolAdminConMotivo_cancelaYRetornaRespuesta `(20ms)`

**[PASO]** handleCerrarHabitacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarHoteles_rolAdmin_retornaListaDeHoteles `(2ms)`

**[PASO]** handleReservasActivasHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleListarAmenidades_rolAdmin_retornaListaDelServicio `(2ms)`

**[PASO]** handleCrearHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleReactivarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCancelarReservacion_bodyLanzaExcepcion_usaMotivoDefault `(3ms)`

**[PASO]** handleReactivarHotel_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleCancelarReservacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCerrarHotel_rolAdmin_cierraHotelExitosamente `(3ms)`

**[PASO]** handleReactivarHabitacion_rolAdmin_reactivaHabitacionExitosamente `(2ms)`

**[PASO]** handleListarCiudades_rolAdmin_retornaListaDeCiudades `(2ms)`

**[PASO]** handleEliminarImagenHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleActualizarAmenidadHotel_rolAdmin_actualizaAmenidadExitosamente `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_rolAdmin_agregaImagenYRetorna201 `(3ms)`

**[PASO]** handleAgregarImagenHotel_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleAgregarImagenHabitacion_rolAdmin_agregaImagenYRetorna201 `(4ms)`

**[PASO]** handleReservasActivasHabitacion_rolAdmin_retornaReservasActivas `(2ms)`

**[PASO]** handleListarPaises_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleReactivarHotel_rolAdmin_reactivaHotelExitosamente `(3ms)`

**[PASO]** handleObtenerMetricas_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEditarHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarAmenidadHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarImagenHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleCancelarReservacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleReservasActivasHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleActualizarAmenidadHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleListarReservaciones_sinRolAdmin_retorna403 `(4ms)`

---

### controllers.ImagenControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handleObtenerImagenAmenidad_imagenExiste_retornaImagenJpeg `(29ms)`

**[PASO]** handleObtenerImagenHotel_imagenNoExiste_retorna404 `(2ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenExiste_retornaImagenJpeg `(2ms)`

**[PASO]** handleObtenerImagenAmenidad_imagenNoExiste_retorna404 `(4ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenNoExiste_retorna404 `(3ms)`

**[PASO]** handleObtenerImagenHotel_imagenExiste_retornaImagenJpeg `(5ms)`

---

### controllers.PagoAgenciaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleProcesarPago_authFalla_noInvocaServicio `(30ms)`

**[PASO]** handleProcesarPago_errorPasarela_retorna500ConMensaje `(28ms)`

**[PASO]** handleProcesarPago_pagoValido_retornaConfirmacion200 `(3ms)`

**[PASO]** handleProcesarPago_pagoInvalido_retorna400ConMensaje `(5ms)`

---

### controllers.PagoControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleProcesarPago_errorRuntime_retorna500 `(63ms)`

**[PASO]** handleProcesarPago_pagoExitoso_retorna200 `(2ms)`

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

**[PASO]** handleObtenerDetalleReservacion_reservacionNoEncontrada_retorna404ConMensaje `(39ms)`

**[PASO]** handleCrearReservacion_errorInterno_retorna500ConMensaje `(30ms)`

**[PASO]** handleExpirarReservacion_reservacionValida_expiraYRetornaMensaje `(4ms)`

**[PASO]** handleObtenerReservaciones_agenciaValida_retornaLista200 `(3ms)`

**[PASO]** handleExpirarReservacion_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleObtenerDetalleReservacion_errorInterno_retorna500ConMensaje `(3ms)`

**[PASO]** handleObtenerDetalleReservacion_reservacionExiste_retornaDetalle200 `(2ms)`

**[PASO]** handleCrearReservacion_datosInvalidos_retorna400ConMensaje `(6ms)`

**[PASO]** handleExpirarReservacion_reservacionInvalida_retorna400ConMensaje `(4ms)`

**[PASO]** handleObtenerDetalleReservacion_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleCrearReservacion_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleObtenerReservaciones_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleCrearReservacion_requestValido_retornaReservacion201 `(3ms)`

---

### controllers.ReservacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleCrearReservacion_datosValidos_retorna201 `(33ms)`

**[PASO]** handleObtenerReservaciones_usuarioConReservaciones_retorna200ConLista `(2ms)`

**[PASO]** handleCrearReservacion_errorRuntime_retorna500 `(3ms)`

**[PASO]** handleCrearReservacion_argumentoInvalido_retorna400 `(3ms)`

**[PASO]** handleObtenerReservaciones_usuarioSinReservaciones_retorna200ConListaVacia `(2ms)`

---

### controllers.SesionControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleSesion_tokenInvalido_retornaSinSesion `(24ms)`

**[PASO]** handleSesion_tokenValido_retornaConSesion `(4ms)`

**[PASO]** handleSesion_sinToken_retornaSinSesion `(1ms)`

---

### controllers.TokenAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleGenerarToken_authOkYDatosValidos_retorna201ConToken `(54ms)`

**[PASO]** handleGenerarToken_authOkYTokenHashNulo_servicioLlamadoConNulo `(6ms)`

**[PASO]** handleGenerarToken_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleGenerarToken_servicioLanzaExcepcion_retorna400 `(3ms)`

---

### controllers.TokenValidacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleValidar_tokenNull_retorna400SinLlamarServicio `(28ms)`

**[PASO]** handleValidar_tokenVacio_retorna400SinLlamarServicio `(3ms)`

**[PASO]** handleValidar_tokenBlanco_retorna400SinLlamarServicio `(4ms)`

**[PASO]** handleValidar_tokenValido_retorna200ConResultado `(3ms)`

**[PASO]** handleValidar_tokenExpirado_retorna400ConMensaje `(3ms)`

---

### controllers.UsuarioControllerTest

- Tests: 15
- Pasaron: 15
- Fallaron: 0

**[PASO]** handleRegistrar_camposDuplicados_retorna409 `(236ms)`

**[PASO]** handleCambiarTelefono_telefonoInvalido_retorna400 `(4ms)`

**[PASO]** handleObtenerPerfil_usuarioAutenticado_retornaPerfil `(3ms)`

**[PASO]** handleCambiarContrasena_credencialesValidas_retorna200 `(4ms)`

**[PASO]** handleCambiarRol_rolNoAutorizado_retorna403 `(3ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaLista `(3ms)`

**[PASO]** handleCambiarTelefono_telefonoValido_retorna200 `(4ms)`

**[PASO]** handleCambiarRol_adminActualiza_retorna200 `(5ms)`

**[PASO]** handleValidar_servicioRetornaFalse_retornaResultadoNoDisponible `(2ms)`

**[PASO]** handleCambiarRol_rolInvalido_retorna400 `(3ms)`

**[PASO]** handleValidar_requestValido_retornaResultado `(2ms)`

**[PASO]** handleCambiarContrasena_credencialesInvalidas_retorna401 `(17ms)`

**[PASO]** handleRegistrar_nuevoUsuario_retorna201ConId `(4ms)`

**[PASO]** handleObtenerPerfil_diferentesUsuarios_llamaServicioConIdCorrecto `(3ms)`

**[PASO]** handleListarAdmin_rolNoAutorizado_retorna403 `(3ms)`

---

### data.DataAccessExceptionTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** sePuedeLanzarYCapturar_comoRuntimeException `(2ms)`

**[PASO]** constructor_conMensajeYCausa_almacenaAmbosValores `(2ms)`

**[PASO]** mensajeDescriptivo_sePropaga_correctamente `(1ms)`

**[PASO]** causa_puedeSerSQLException_simulada `(5ms)`

**[PASO]** sePuedeCapturar_comoDataAccessException `(1ms)`

**[PASO]** esSubclase_deRuntimeException `(1ms)`

---

### data.ResultSetMapperTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** implementacion_conLambda_mapeaStringCorrectamente `(156ms)`

**[PASO]** implementacion_propagaSQLException_cuandoResultSetFalla `(2ms)`

**[PASO]** implementacion_conLambda_construyeObjetoCompuesto `(1ms)`

**[PASO]** implementacion_conLambda_mapeaDoubleCorrectamente `(2ms)`

**[PASO]** implementacion_conLambda_mapeaEnteroCorrectamente `(2ms)`

---

### dtos.DtosTest

- Tests: 78
- Pasaron: 78
- Fallaron: 0

**[PASO]** puedeCancelarDTO_constructorFalse_almacenaCorrecto `(1ms)`

**[PASO]** crearHotelRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_constructor_almacenaBooleans `(0ms)`

**[PASO]** tokenValidacionResponseDTO_constructor_almacenaTodosLosCampos `(0ms)`

**[PASO]** aerolineaIdentidadDTO_constructor_almacenaNombreYUrl `(2ms)`

**[PASO]** cambiarContrasenaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** editarHotelRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** hotelAdminDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionAgenciaDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** reservacionRequestDTO_setterHabitaciones_funciona `(2ms)`

**[PASO]** hotelResultadoDTO_listas_seAsignanCorrectamente `(2ms)`

**[PASO]** reservacionResponseDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** editarAerolineaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** crearAerolineaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** loginResponseDTO_constructor_almacenaTodosLosCampos `(1ms)`

**[PASO]** cambiarRolRequestDTO_setterYGetter_funcionan `(1ms)`

**[PASO]** usuarioAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** aerolineaAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_setter_sobrescribeToken `(0ms)`

**[PASO]** crearAerolineaAdminRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** ciudadDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** reservacionDetalleDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** paisDTO_constructor_almacenaIdYNombre `(1ms)`

**[PASO]** downResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** agregarAmenidadRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** comentarioRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** cancelacionRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** paisDTO_setters_sobrescribenValores `(1ms)`

**[PASO]** reservacionAgenciaResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** usuarioValidacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** tokenAerolineaResponseDTO_constructor_almacenaTodosLosCampos `(0ms)`

**[PASO]** busquedaRequestDTO_valoresPorDefecto_sonNullOCero `(1ms)`

**[PASO]** downRequestDTO_setterYGetter_funcionan `(1ms)`

**[PASO]** downRequestDTO_valorNegativo_seAlmacenaCorrectamente `(0ms)`

**[PASO]** usuarioPerfilResponseDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** tokenValidacionResponseDTO_porcentajeCero_seAlmacenaCorrectamente `(1ms)`

**[PASO]** editarAgenciaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** resultadoNotificacionDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** tokenAerolineaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaAdminDTO_valoresPorDefecto_sonCero `(1ms)`

**[PASO]** comentarioResponseDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** habitacionAgenciaResponseDTO_settersYGetters_funcionan `(8ms)`

**[PASO]** reservacionRequestDTO_listaVacia_seAsignaCorrectamente `(1ms)`

**[PASO]** habitacionAdminDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** pagoRequestDTO_settersYGetters_facturacion_funcionan `(0ms)`

**[PASO]** cambiarTelefonoRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** usuarioWebserviceLibreDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** sesionDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** loginRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaIdentidad_settersYGetters_funcionan `(2ms)`

**[PASO]** tipoHabitacionResultadoDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** loginResponseDTO_diferencteRol_seAlmacenaCorrectamente `(0ms)`

**[PASO]** busquedaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** agenciaDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** amenidadDTO_setters_sobrescribenValores `(0ms)`

**[PASO]** hotelAmenidadDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** editarHabitacionRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionReservaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** crearAgenciaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** sesionDTO_autenticadoFalsePorDefecto `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_todosFalse_cuandoNingunExiste `(0ms)`

**[PASO]** crearAgenciaAdminRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** amenidadHotelDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** hotelAgenciaDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** hotelResultadoDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** aerolineaWebserviceDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** puedeCancelarDTO_constructorTrue_almacenaCorrecto `(0ms)`

**[PASO]** habitacionResumenDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** crearHabitacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_constructor_almacenaToken `(0ms)`

**[PASO]** amenidadDTO_constructor_almacenaIdYNombre `(0ms)`

**[PASO]** pagoAgenciaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** subirImagenRequestDTO_setterYGetter_funcionan `(1ms)`

**[PASO]** loginRequestDTO_valoresPorDefecto_sonNull `(0ms)`

**[PASO]** habitacionDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaDTO_valoresPorDefecto_sonCero `(0ms)`

**[PASO]** pagoResponseDTO_settersYGetters_funcionan `(0ms)`

---

### helpers.AerolineaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(39ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAerolinea `(2ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(3ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(3ms)`

---

### helpers.AgenciaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(34ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(3ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAgencia `(2ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(3ms)`

---

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(5ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(1ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(2ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(1ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(6ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(1ms)`

---

### helpers.EmailHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** enviar_datosValidos_noLanzaExcepcion `(2ms)`

**[PASO]** enviar_cuerpoMinimo_noLanzaExcepcion `(1ms)`

**[PASO]** enviar_argumentosExactos_invocaMetodoConParametrosCorrectos `(2ms)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(544ms)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(142ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(2ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(2ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(2ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(2ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(2ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(1ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(4ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(2ms)`

**[PASO]** getRolId_retornaRolCorrecto `(2ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(3ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(381ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(660ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(604ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(787ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(530ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(49ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(2ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(3ms)`

**[PASO]** validar_numeroConEspacios_esValido `(30ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(0ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(0ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(0ms)`

**[PASO]** validar_cvv4digitos_esValido `(2ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(1ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(0ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(3ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(0ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(0ms)`

---

### models.UsuarioModelTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** ciudadId_aceptaNull `(2ms)`

**[PASO]** valoresPorDefecto_sonNullOCero `(1ms)`

**[PASO]** reasignarUsername_noAfectaOtrosCampos `(0ms)`

**[PASO]** setterYGetter_fechaNacimiento_funciona `(0ms)`

**[PASO]** ciudadId_conValor_seAlmacenaCorrectamente `(0ms)`

**[PASO]** constructorVacio_creaInstanciaNoNula `(1ms)`

**[PASO]** settersYGetters_camposNumericos_funcionan `(0ms)`

**[PASO]** settersYGetters_camposTexto_funcionan `(2ms)`

---

### repositories.AdminReservacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarTodas_retornaListaConReservacionDePrueba `(4.8s)`

**[PASO]** obtenerReservacion_idExistente_retornaDatos `(1.7s)`

**[PASO]** obtenerReservacion_idInexistente_retornaNull `(1.5s)`

**[PASO]** obtenerDatosUsuarioPorReservacion_reservacionExistente_retornaDatos `(1.8s)`

**[PASO]** cancelarReservacion_reservacionPendiente_cambiaEstado `(3s)`

---

### repositories.AerolineaAdminRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarTodas_retornaListaNoNula `(1.5s)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(2.2s)`

**[PASO]** editar_aerolineaExistente_actualizaDatos `(857ms)`

**[PASO]** listarWebserviceLibres_retornaListaNoNula `(886ms)`

---

### repositories.AerolineaAliadaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerAerolineaPorToken_tokenActivo_retornaDto `(1.1s)`

**[PASO]** obtenerAerolineaPorToken_tokenInexistente_retornaNull `(852ms)`

**[PASO]** obtenerDescuentoAerolinea_tokenActivo_retornaDescuentoPositivo `(793ms)`

**[PASO]** buscarCiudadId_ciudadExistente_retornaId `(848ms)`

**[PASO]** guardarBusqueda_datosValidos_persisteEnOracle `(944ms)`

**[PASO]** obtenerAerolineaIdPorURL_urlExistente_retornaId `(982ms)`

**[PASO]** guardarTokensAerolinea_datosValidos_actualizaToken `(1.4s)`

---

### repositories.AerolineaWebserviceRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAerolinea_retornaListaConAlMenosUna `(1.2s)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(2.1s)`

**[PASO]** cambiarEstado_aerolineaActiva_actualizaEstado `(722ms)`

**[PASO]** listarPorUsuario_usuarioSinAerolinea_retornaListaVacia `(692ms)`

---

### repositories.AgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAgencia_retornaListaConAlMenosUna `(719ms)`

**[PASO]** crear_datosValidos_retornaAgenciaConId `(1.3s)`

**[PASO]** editar_datosNuevos_actualizaNombre `(1.8s)`

**[PASO]** cambiarEstado_agenciaActiva_cambiaEstado `(1.3s)`

**[PASO]** obtenerAgenciaPorToken_sinToken_retornaNull `(1.4s)`

---

### repositories.AuthRepositoryIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** buscarPorIdentificador_porUsername_retornaUsuario `(823ms)`

**[PASO]** buscarPorIdentificador_porCorreo_retornaUsuario `(412ms)`

**[PASO]** buscarPorIdentificador_identificadorInexistente_retornaNull `(397ms)`

---

### repositories.BusquedaAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(703ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(687ms)`

**[PASO]** obtenerDescuentoAgencia_usuarioConAgencia_retornaDescuento `(882ms)`

**[PASO]** guardarBusqueda_datosValidos_noLanzaExcepcion `(907ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(651ms)`

---

### repositories.BusquedaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(189ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(378ms)`

**[PASO]** guardarBusqueda_sinUsuario_noLanzaExcepcion `(959ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(796ms)`

**[PASO]** buscarImagenesHotel_hotelInexistente_retornaListaVacia `(1.1s)`

**[PASO]** buscarAmenidadesHotel_hotelInexistente_retornaListaVacia `(726ms)`

**[PASO]** buscarImagenesHabitacion_habitacionInexistente_retornaListaVacia `(920ms)`

---

### repositories.CancelacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaCancelar_reservacionDelUsuario_retornaDatos `(513ms)`

**[PASO]** obtenerReservacionParaCancelar_otroUsuarioId_retornaNull `(495ms)`

**[PASO]** obtenerFechaCheckInMasReciente_sinDetalles_retornaNull `(558ms)`

**[PASO]** cancelarReservacion_estadoPendiente_actualizaAEstado4 `(598ms)`

**[PASO]** obtenerReservacionAgenciaParaCancelar_sinAgenciaVinculada_retornaNull `(673ms)`

---

### repositories.ComentarioRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** existeComentarioConResena_sinComentarios_retornaFalse `(707ms)`

**[PASO]** crearComentario_conResena_retornaIdPositivo `(749ms)`

**[PASO]** existeComentarioConResena_despuesDeCrear_retornaTrue `(934ms)`

**[PASO]** crearComentario_sinResena_esRespuestaAOtro `(712ms)`

**[PASO]** actualizarRatingHotel_conResena_noLanzaExcepcion `(722ms)`

**[PASO]** obtenerComentario_comentarioExistente_retornaDtoConDatos `(722ms)`

**[PASO]** obtenerComentariosPorUsuario_retornaListaConAlMenosUno `(719ms)`

**[PASO]** obtenerComentariosPorHotel_retornaListaConAlMenosUno `(704ms)`

---

### repositories.DestinosRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerTodosLosHoteles_retornaListaNoNula `(72ms)`

**[PASO]** obtenerTodosLosHoteles_conHotelesActivos_retornaDtosValidos `(54ms)`

**[PASO]** obtenerImagenesHotel_hotelExistente_retornaListaNoNula `(87ms)`

**[PASO]** obtenerImagenesHotel_hotelInexistente_retornaListaVacia `(45ms)`

---

### repositories.DownsRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerValorDown_sinDown_retornaNull `(926ms)`

**[PASO]** insertarDown_registraDown_obtenibleEnOracle `(951ms)`

**[PASO]** obtenerDownsDeUsuario_trasInsertarDown_retornaListaConDown `(977ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_filtraPorHotel `(969ms)`

**[PASO]** actualizarContadorDown_incrementaContador `(882ms)`

**[PASO]** eliminarDown_eliminaDown_obtenerValorRetornaNull `(1.7s)`

---

### repositories.HotelRepositoryIntegrationTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** listarAmenidades_retornaListaNoNula `(1.2s)`

**[PASO]** crearAmenidad_nombreValido_retornaIdPositivo `(541ms)`

**[PASO]** listarTodos_retornaListaConElHotelInsertado `(470ms)`

**[PASO]** actualizarHotel_datosNuevos_actualizaNombreEnOracle `(529ms)`

**[PASO]** cerrarHotel_hotelActivo_cambiaEstadoId `(791ms)`

**[PASO]** reactivarHotel_hotelCerrado_restauraEstadoId `(675ms)`

**[PASO]** existe_hotelExistente_retornaTrue `(603ms)`

**[PASO]** crearHabitacion_datosValidos_retornaIdPositivo `(656ms)`

**[PASO]** obtenerMetricas_retornaMapaConClaves `(821ms)`

---

### repositories.ImagenRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerImagenHotel_idExistente_retornaBytes `(319ms)`

**[PASO]** obtenerImagenHotel_idInexistente_retornaNull `(285ms)`

**[PASO]** obtenerImagenHabitacion_idExistente_retornaBytes `(274ms)`

**[PASO]** obtenerImagenHabitacion_idInexistente_retornaNull `(258ms)`

**[PASO]** eliminarImagenHotel_eliminaImagen_noObtenible `(265ms)`

**[PASO]** eliminarImagenHabitacion_eliminaImagen_noObtenible `(303ms)`

**[PASO]** obtenerImagenAmenidad_idInexistente_retornaNull `(271ms)`

---

### repositories.PagoAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDeAgencia_retornaDatos `(2.1s)`

**[PASO]** obtenerReservacionParaPago_agenciaIncorrecta_retornaNull `(1s)`

**[PASO]** confirmarReservacion_estadoPendiente_actualizaAEstado2 `(1.1s)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(1.1s)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(2.7s)`

---

### repositories.PagoRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDelUsuario_retornaDatosCorrectos `(875ms)`

**[PASO]** obtenerReservacionParaPago_otroUsuarioId_retornaNull `(938ms)`

**[PASO]** confirmarReservacion_estadoPendiente_cambiaAEstado2 `(1s)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(2.3s)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(987ms)`

**[PASO]** actualizarTotalReservacion_nuevoTotal_actualizaCorrectamente `(1s)`

**[PASO]** obtenerCiudadReservacion_conDetallesYHotel_retornaNombreCiudad `(971ms)`

---

### repositories.ReservacionAgenciaRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerDescuentoAgencia_agenciaConDescuento_retornaValorPositivo `(1.3s)`

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(1.4s)`

**[PASO]** existeTraslape_fechasSinConflicto_retornaFalse `(1.3s)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(1.2s)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatos `(1.3s)`

**[PASO]** expirarReservacion_reservacionPendiente_actualizaEstado `(1.4s)`

---

### repositories.ReservacionRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(882ms)`

**[PASO]** existeTraslape_sinReservacionesConflicto_retornaFalse `(919ms)`

**[PASO]** existeTraslape_conReservacionConflicto_retornaTrue `(959ms)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(1.3s)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatosCorrectos `(1s)`

**[PASO]** obtenerReservacionesDeUsuario_conDetalle_retornaListaConAlMenosUno `(971ms)`

**[PASO]** expirarReservacionesVencidas_noLanzaExcepcion_retornaEntero `(1.1s)`

**[PASO]** obtenerImagenesHotel_hotelSinImagenes_retornaListaVacia `(1.1s)`

---

### repositories.UsuarioRepositoryIntegrationTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** existeUsername_usernameExistente_retornaTrue `(525ms)`

**[PASO]** existeUsername_usernameInexistente_retornaFalse `(559ms)`

**[PASO]** existeCorreo_correoExistente_retornaTrue `(669ms)`

**[PASO]** existePasaporte_pasaporteExistente_retornaTrue `(624ms)`

**[PASO]** existePasaporte_pasaporteNuloOVacio_retornaFalse `(579ms)`

**[PASO]** crearUsuario_datosValidos_retornaIdPositivo `(1s)`

**[PASO]** obtenerPerfil_usuarioExistente_retornaDtoConDatos `(1s)`

**[PASO]** actualizarTelefono_usuarioExistente_cambiaElCampo `(1.5s)`

**[PASO]** obtenerContrasena_usuarioExistente_retornaHashNoNulo `(1.5s)`

**[PASO]** listarTodosConRol_retornaListaNoNula `(534ms)`

---

### services.AdminBusquedaServiceIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarSinFiltrosRetornaResultadosYTotalCorrecto `(952ms)`

**[PASO]** listarConFiltroDestinoFiltraCorrectamente `(920ms)`

**[PASO]** listarConTipoWebRetornaSoloBusquedasWeb `(935ms)`

**[PASO]** listarPaginacionRetornaSegundaPagina `(858ms)`

**[PASO]** resumenRetornaEstructuraCompletaDesdeOracle `(790ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(186ms)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(8ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(2ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(2ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(2ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(8ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(2.7s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(1ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(1.9s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(2ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(1ms)`

---

### services.AdminReservacionServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(100ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(2ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacionYNotificaAgencia `(2ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacionYNotificaAgencia `(1ms)`

**[PASO]** cancelarReservacion_notificadorRetornaErrorHTTP_completaCancelacionConError `(3ms)`

**[PASO]** cancelarReservacion_notificadorRetornaNoEsReservaAgencia_completaCancelacion `(2ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(1ms)`

---

### services.AerolineaAdminServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarWebserviceLibres_conUsuariosDisponibles_retornaListaDelRepo `(64ms)`

**[PASO]** editar_requestValido_delegaAlRepo `(1ms)`

**[PASO]** listarTodas_conAerolineas_retornaListaDelRepo `(4ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(1ms)`

**[PASO]** listarWebserviceLibres_todosAsignados_retornaListaVacia `(1ms)`

**[PASO]** listarTodas_sinAerolineas_retornaListaVacia `(2ms)`

**[PASO]** editar_idDistinto_invocaRepoConIdCorrecto `(2ms)`

---

### services.AerolineaWebserviceServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioSinAerolineas_retornaListaVacia `(57ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(0ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentExceptionSinInvocarRepo `(2ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(2ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** listarPorUsuario_usuarioConAerolineas_retornaListaDelRepo `(1ms)`

---

### services.AgenciaNotificadorExternoServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** notificarCancelacion_agenciaSinToken_retornaErrorSinHTTP `(228ms)`

**[PASO]** notificarCancelacion_httpError500_retornaEnviadoTrueConStatus500 `(14ms)`

**[PASO]** notificarCancelacion_agenciaSinURL_retornaErrorSinHTTP `(5ms)`

**[PASO]** notificarCancelacion_httpExitoso200_retornaEnviadoTrueYStatusCorrecto `(6ms)`

**[PASO]** notificarCancelacion_excepcionRed_retornaErrorEnDTOSinPropagar `(13ms)`

**[PASO]** notificarCancelacion_noEsReservaDeAgencia_retornaEsReservaFalseYSinHTTP `(9ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(1ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(1ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(4ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(4ms)`

**[PASO]** listarTodas_retornaListaCompleta `(1ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(1ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(1ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(1ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(1ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(831ms)`

**[PASO]** loginExitosoConCorreo `(759ms)`

**[PASO]** loginFallaUsuarioInexistente `(380ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(1.7s)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(653ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(579ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(0ms)`

---

### services.BusquedaAerolineaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_guardaBusquedaEnRepo `(1ms)`

**[PASO]** buscar_conDescuento10Porciento_aplicaDescuentoAPrecios `(5ms)`

**[PASO]** buscar_tokenInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** buscar_dosHotelesEnCiudad_retornaListaConDosHoteles `(2ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_retornaListaDeHoteles `(1ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(60ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(9ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(4ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(0ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(5ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(2ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(2.5s)`

**[PASO]** busquedaRegistraEventoEnOracle `(1.8s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(1.7s)`

**[PASO]** busquedaFallaCiudadInexistente `(526ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(64ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(4ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(4ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(2ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(4ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(3ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(873ms)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(732ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(946ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(57ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(3ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(1ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(1ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(1ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(4ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(3ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(3ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(2ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(57ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(2ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(1ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(1ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(1ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(9ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(1ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(148ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(3ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(2ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(1ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(62ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(2ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(2ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(2ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(5ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(2ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(2ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(4ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(4ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(5ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(1ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(10ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(2ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(61ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(1.9s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(0ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(0ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(51ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(3ms)`

**[PASO]** iniciar_noLanzaExcepcion `(3ms)`

**[PASO]** detener_noLanzaExcepcion `(2ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(2ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(9ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(3ms)`

---

### services.HandshakeAerolineaServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(7ms)`

**[PASO]** procesarHandshake_urlRegistrada_retornaResponseDTOConTokenDeSalida `(5ms)`

**[PASO]** procesarHandshake_tokensNoPersistidos_lanzaIllegalArgumentException `(4ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(7ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(3ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(58ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(0ms)`

---

### services.HotelServiceTest

- Tests: 69
- Pasaron: 69
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(273ms)`

**[PASO]** agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje `(0ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(3ms)`

**[PASO]** editarHabitacion_tipoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** eliminarAmenidadHotel_delegaAlRepositorio `(5ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero `(2ms)`

**[PASO]** agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarTodasReservaciones_delegaAlRepositorio `(1ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarImagenHabitacion_delegaAlRepositorio `(1ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarPaises_delegaAlRepositorioDePaises `(0ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva `(2ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerMetricas_delegaAlRepositorio `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo `(1ms)`

**[PASO]** cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException `(6ms)`

**[PASO]** eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra `(8ms)`

**[PASO]** listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException `(5ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(4ms)`

**[PASO]** agregarImagenHabitacion_habitacionExiste_retornaMapaConId `(2ms)`

**[PASO]** reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero `(2ms)`

**[PASO]** agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarHabitaciones_hotelExiste_retornaListaConImagenes `(2ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos `(2ms)`

**[PASO]** editarHabitacion_habitacionExiste_invocaActualizarHabitacion `(1ms)`

**[PASO]** eliminarImagenAmenidad_delegaAlRepositorio `(1ms)`

**[PASO]** eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar `(2ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(4ms)`

**[PASO]** listarCiudades_delegaAlRepositorioDePaises `(1ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(2ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(0ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra `(4ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion `(2ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException `(6ms)`

**[PASO]** eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel `(2ms)`

**[PASO]** agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarImagenHotel_delegaAlRepositorio `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar `(4ms)`

**[PASO]** agregarImagenAmenidad_base64Valido_retornaMapaConId `(2ms)`

**[PASO]** obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(3ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos `(6ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException `(3ms)`

**[PASO]** actualizarAmenidadHotel_delegaAlRepositorio `(2ms)`

**[PASO]** reactivarHabitacion_habitacionExiste_invocaReactivar `(3ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(0ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** reactivarHotel_hotelExiste_invocaReactivarHotel `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException `(9ms)`

**[PASO]** editarHabitacion_estadoInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarImagenHotel_hotelExiste_retornaMapaConId `(2ms)`

**[PASO]** listarAmenidades_delegaAlRepositorio `(0ms)`

**[PASO]** eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(52ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(2ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(2ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(1ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(58ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(3ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(1.8s)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(730ms)`

**[PASO]** pagoFallaCvvInvalido `(660ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(90ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(2ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(1ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(2ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(4ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(64ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(2ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(59ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(4ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(3ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(3ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(0ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(3ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(3ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(4ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(1.6s)`

**[PASO]** crearReservacionFallaSinHabitaciones `(572ms)`

**[PASO]** crearReservacionFallaFechaPasada `(536ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(4ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(2ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(52ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(1ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(1ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(0ms)`

---

### services.TokenAerolineaServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** generarToken_datosValidos_insertaTokenConIdsCorrectos `(55ms)`

**[PASO]** generarToken_tokenValidoCiudadExistente_retornaResponseDTOCompleto `(10ms)`

**[PASO]** generarToken_ciudadNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** generarToken_tokenInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** generarToken_datosValidos_urlRedireccionContieneUrlBaseYToken `(1ms)`

---

### services.TokenValidacionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** validar_tokenInexistente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** validar_tokenExpirado_lanzaIllegalArgumentException `(10ms)`

**[PASO]** validar_tokenValido_retornaTokenValidacionResponseDTO `(1ms)`

**[PASO]** validar_tokenYaUtilizado_lanzaIllegalArgumentException `(13ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(236ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(2ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(1ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(2ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(2ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(2ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(1ms)`

**[PASO]** validarDisponibilidad_todosLibres `(2ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(828ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(2ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(2.2s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(0ms)`

---
