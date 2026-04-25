# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 822
- Pasaron: 822
- Fallaron: 0
- Saltados: 0
- Duracion: 129.4s
- Ejecutado: 23/04/2026 20:52:52

---

## Suites

### clients.MoventClientTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall `(85ms)`

**[PASO]** notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(517ms)`

**[PASO]** notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion `(20ms)`

**[PASO]** notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall `(2ms)`

**[PASO]** notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(17ms)`

**[PASO]** notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall `(4ms)`

**[PASO]** notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall `(10ms)`

**[PASO]** notificarHabitacionCerrada_multiplesReservas_swallowsExcepcion `(29ms)`

---

### config.ServerConfigTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** metodoCreateServer_existe_esPublicoEstatico `(5ms)`

**[PASO]** claseServerConfig_existe_esPublica `(3ms)`

---

### controllers.AdminBusquedaControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleResumen_conRolAdmin_retornaResumenEstadistico `(2.1s)`

**[PASO]** handleExportar_conRolIncorrecto_retorna403YNoLlamaServicio `(11ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_aplicaFiltrosDeQueryParams `(72ms)`

**[PASO]** handleExportar_conEmailValidoSinFiltros_llamaServicioYRetornaMensaje `(5ms)`

**[PASO]** handleExportar_conEmailInvalido_retorna400ConMensaje `(7ms)`

**[PASO]** handleExportar_conEmailValidoYFiltros_llamaServicioConFiltros `(5ms)`

**[PASO]** handleListarBusquedas_conRolAdmin_llamaServicioYRetornaResultado `(12ms)`

**[PASO]** handleListarBusquedas_conRolIncorrecto_retorna403YNoLlamaServicio `(5ms)`

**[PASO]** handleResumen_conRolIncorrecto_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleExportar_conEmailBlanco_retorna400ConMensaje `(5ms)`

---

### controllers.AerolineaAdminControllerTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** handleEditar_conRolIncorrecto_retorna403YNoLlamaServicio `(26ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(4ms)`

**[PASO]** handleListarLibres_conRolAdmin_retornaListaDeWebserviceLibres `(3ms)`

**[PASO]** handleEditar_conRolAdminYDatosValidos_retornaMensajeExito `(27ms)`

**[PASO]** handleEditar_conRolAdminYDatosInvalidos_retorna400ConMensaje `(11ms)`

**[PASO]** handleCrear_conRolAdminYDatosValidos_retorna201ConNuevaAerolinea `(20ms)`

**[PASO]** handleListar_conRolAdmin_retornaListaDeAerolineas `(3ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(22ms)`

**[PASO]** handleCrear_conRolAdminYDatosInvalidos_retorna400ConMensaje `(4ms)`

**[PASO]** handleListarLibres_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

---

### controllers.AerolineaWebserviceControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleListar_conRolWebservice_retornaAerolineasDelUsuario `(27ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosInvalidos_retorna400ConMensaje `(26ms)`

**[PASO]** handleCambiarEstado_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCrear_conRolWebserviceYDatosValidos_retorna201ConNuevaAerolinea `(3ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYEstadoInvalido_retorna400ConMensaje `(4ms)`

**[PASO]** handleListar_conRolNulo_retorna403YNoLlamaServicio `(1ms)`

**[PASO]** handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio `(2ms)`

**[PASO]** handleListar_conRolIncorrecto_retorna403YNoLlamaServicio `(3ms)`

**[PASO]** handleCambiarEstado_conRolWebserviceYDatosValidos_retornaMensajeExito `(4ms)`

---

### controllers.AgenciaControllerTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** handleEditarAdmin_datosValidos_retornaMensaje `(152ms)`

**[PASO]** handleEliminarWebservice_rolNoWebservice_retorna403 `(6ms)`

**[PASO]** handleCrearAdmin_datosInvalidos_retorna400 `(7ms)`

**[PASO]** handleCambiarEstadoWebservice_estadoInvalido_retorna400 `(7ms)`

**[PASO]** handleListarWebservice_rolNoWebservice_retorna403 `(4ms)`

**[PASO]** handleListarAdmin_rolNoAdministrador_retorna403 `(4ms)`

**[PASO]** handleListarWebservice_rolWebservice_retornaLista `(3ms)`

**[PASO]** handleCrearWebservice_rolNoWebservice_retorna403 `(3ms)`

**[PASO]** handleCambiarEstadoWebservice_rolNoWebservice_retorna403 `(3ms)`

**[PASO]** handleEditarAdmin_agenciaNoEncontrada_retorna400 `(6ms)`

**[PASO]** handleEliminarWebservice_agenciaExistente_retornaMensaje `(7ms)`

**[PASO]** handleCrearAdmin_datosValidos_retorna201 `(5ms)`

**[PASO]** handleCrearAdmin_rolNoAdministrador_retorna403 `(4ms)`

**[PASO]** handleCrearWebservice_datosValidos_retorna201 `(6ms)`

**[PASO]** handleHandshake_agenciaNoRegistrada_retorna400 `(8ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaTodasLasAgencias `(5ms)`

**[PASO]** handleCambiarEstadoWebservice_datosValidos_retornaMensaje `(5ms)`

**[PASO]** handleEliminarWebservice_agenciaNoPertenece_retorna400 `(7ms)`

**[PASO]** handleCrearWebservice_argumentoInvalido_retorna400 `(8ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(5ms)`

**[PASO]** handleEditarAdmin_rolNoAdministrador_retorna403 `(5ms)`

---

### controllers.AuthControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleLogout_siempre_invalidaCookieYRetorna200 `(53ms)`

**[PASO]** handleLogin_credencialesValidas_emiteCookieYRetorna200 `(37ms)`

**[PASO]** handleLogin_credencialesInvalidas_retorna401 `(3ms)`

---

### controllers.BusquedaAerolineaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(49ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(5ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(5ms)`

---

### controllers.BusquedaAgenciaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_requestInvalido_retorna400ConMensaje `(57ms)`

**[PASO]** handleBuscar_authFalla_noInvocaServicio `(12ms)`

**[PASO]** handleBuscar_requestValido_retornaResultados200 `(11ms)`

---

### controllers.BusquedaControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleBuscar_tokenValido_buscaConUsuarioId `(183ms)`

**[PASO]** handleBuscar_servicioLanzaIllegalArgument_retorna404 `(4ms)`

**[PASO]** handleBuscar_sinToken_buscaComoAnonimo `(5ms)`

---

### controllers.CancelacionAgenciaControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handlePuedeCancelar_reservacionValida_retornaResultado200 `(53ms)`

**[PASO]** handleCancelar_authFalla_noInvocaServicio `(7ms)`

**[PASO]** handlePuedeCancelar_errorServicio_retorna500ConMensaje `(5ms)`

**[PASO]** handlePuedeCancelar_authFalla_noInvocaServicio `(5ms)`

**[PASO]** handleCancelar_motivoInvalido_retorna400ConMensaje `(22ms)`

**[PASO]** handleCancelar_motivoValido_cancelaYRetorna200 `(8ms)`

---

### controllers.CancelacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleCancelarReservacion_reservacionValida_retorna200 `(8ms)`

**[PASO]** handleCancelarReservacion_reservacionInvalida_retorna400 `(3ms)`

---

### controllers.ComentarioControllerTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** handleObtenerPorHotelAgencia_authOk_retorna200ConLista `(78ms)`

**[PASO]** handleObtenerPorUsuario_usuarioConComentarios_retorna200ConLista `(3ms)`

**[PASO]** handleObtenerPorHotelAgencia_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleObtenerPorHotel_hotelValido_retorna200ConLista `(4ms)`

**[PASO]** handleObtenerPorUsuario_usuarioSinComentarios_retorna200ConListaVacia `(3ms)`

**[PASO]** handleObtenerPorHotel_hotelSinComentarios_retorna200ConListaVacia `(2ms)`

**[PASO]** handleAgregarComentario_argumentoInvalido_retorna400 `(4ms)`

**[PASO]** handleObtenerPorHotelAgencia_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleAgregarComentario_datosValidos_retorna201 `(2ms)`

---

### controllers.DestinosControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerDestinos_conDestinosExistentes_retorna200ConLista `(24ms)`

**[PASO]** handleObtenerDestinos_sinDestinos_retorna200ConListaVacia `(2ms)`

---

### controllers.DownsControllerTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** handleAgregarDown_datosValidos_retorna201ConMensaje `(54ms)`

**[PASO]** handleObtenerDowns_usuarioConDowns_retorna200ConLista `(3ms)`

**[PASO]** handleActualizarDown_datosValidos_retorna200ConMensaje `(4ms)`

**[PASO]** handleEliminarDown_downExistente_retorna200ConMensaje `(4ms)`

**[PASO]** handleObtenerDowns_usuarioSinDowns_retorna200ConListaVacia `(3ms)`

**[PASO]** handleAgregarDown_argumentoInvalido_retorna400 `(3ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelValido_retorna200ConLista `(5ms)`

**[PASO]** handleObtenerDownsPorHotel_hotelSinDowns_retorna200ConListaVacia `(2ms)`

**[PASO]** handleActualizarDown_downNoExistente_retorna400 `(5ms)`

**[PASO]** handleEliminarDown_downNoExistente_retorna400 `(5ms)`

---

### controllers.EmailReservacionControllerTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** handleNewsletter_correoValido_enviaCorreoYRetorna200 `(59ms)`

**[PASO]** handleEnviarCorreoReservacion_reservacionNoExiste_retorna404 `(2ms)`

**[PASO]** handleEnviarCorreoReservacion_rolAutorizado_enviaCorreoYRetorna200 `(2ms)`

**[PASO]** handleContacto_camposObligatoriosFaltantes_retorna400 `(3ms)`

**[PASO]** handleEnviarCorreoReservacion_errorRuntime_retorna500 `(3ms)`

**[PASO]** handleNewsletter_correoSinArroba_retorna400 `(4ms)`

**[PASO]** handleContacto_formularioValido_enviaCorreoYRetorna200 `(19ms)`

**[PASO]** handleEnviarCorreoReservacion_rolNoAutorizado_retorna403 `(3ms)`

---

### controllers.HandshakeAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleHandshake_tokenEntradaIncorrecto_retorna400 `(33ms)`

**[PASO]** handleHandshake_servicioExitoso_noLlamaStatus `(6ms)`

**[PASO]** handleHandshake_aerolineaNoRegistrada_retorna400 `(28ms)`

**[PASO]** handleHandshake_datosValidos_retornaResponse `(9ms)`

---

### controllers.HotelAgenciaControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleObtenerHoteles_autenticacionFallida_noRetornaDatos `(57ms)`

**[PASO]** handleObtenerHoteles_autenticacionValida_retornaListaHoteles `(3ms)`

---

### controllers.HotelControllerTest

- Tests: 83
- Pasaron: 83
- Fallaron: 0

**[PASO]** handleReactivarHabitacion_habitacionNoEncontrada_retorna404 `(121ms)`

**[PASO]** handleEliminarHotel_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleEliminarHabitacion_sinRolAdmin_retorna403 `(4ms)`

**[PASO]** handleEditarHotel_servicioLanzaExcepcion_retorna400 `(38ms)`

**[PASO]** handleEliminarImagenAmenidad_rolAdmin_eliminaImagenExitosamente `(4ms)`

**[PASO]** handleReservasActivasHotel_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleEliminarImagenAmenidad_sinRolAdmin_retorna403 `(5ms)`

**[PASO]** handleCrearHabitacion_servicioLanzaExcepcion_retorna400 `(31ms)`

**[PASO]** handleAgregarImagenHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleListarAmenidadesHotel_rolAdmin_retornaAmenidadesDelHotel `(3ms)`

**[PASO]** handleEliminarHotel_sinRolAdmin_retorna403 `(5ms)`

**[PASO]** handleCerrarHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearAmenidad_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleListarCiudades_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearAmenidad_sinRolAdmin_retorna403 `(5ms)`

**[PASO]** handleEliminarImagenHotel_rolAdmin_eliminaImagenExitosamente `(3ms)`

**[PASO]** handleAgregarAmenidadHotel_rolAdmin_agregaAmenidadYRetorna201 `(24ms)`

**[PASO]** handleListarHabitaciones_sinRolAdmin_retorna403 `(4ms)`

**[PASO]** handleReservasActivasHotel_rolAdmin_retornaReservasActivas `(4ms)`

**[PASO]** handleReservasActivasHabitacion_habitacionNoEncontrada_retorna404 `(3ms)`

**[PASO]** handleEditarHabitacion_rolAdmin_editaHabitacionExitosamente `(31ms)`

**[PASO]** handleListarHabitaciones_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleEliminarImagenHabitacion_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleEliminarAmenidadHotel_rolAdmin_eliminaAmenidadExitosamente `(3ms)`

**[PASO]** handleListarPaises_rolAdmin_retornaListaDePaises `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleEliminarHabitacion_habitacionNoEncontrada_retorna404 `(4ms)`

**[PASO]** handleCrearAmenidad_rolAdmin_creaAmenidadYRetorna201 `(3ms)`

**[PASO]** handleCrearHabitacion_rolAdmin_creaHabitacionYRetorna201 `(4ms)`

**[PASO]** handleAgregarImagenHotel_rolAdmin_agregaImagenYRetorna201 `(26ms)`

**[PASO]** handleEliminarHotel_rolAdmin_eliminaHotelExitosamente `(3ms)`

**[PASO]** handleReactivarHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCerrarHabitacion_rolAdmin_cierraHabitacionExitosamente `(3ms)`

**[PASO]** handleListarHoteles_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleListarReservaciones_rolAdmin_retornaTodasLasReservaciones `(3ms)`

**[PASO]** handleEditarHabitacion_servicioLanzaExcepcion_retorna400 `(6ms)`

**[PASO]** handleListarAmenidadesHotel_hotelNoEncontrado_retorna404 `(4ms)`

**[PASO]** handleEditarHotel_sinRolAdmin_retorna403 `(4ms)`

**[PASO]** handleObtenerMetricas_rolAdmin_retornaMetricasDelSistema `(7ms)`

**[PASO]** handleCrearHotel_rolAdmin_creaHotelYRetorna201 `(37ms)`

**[PASO]** handleCerrarHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarAmenidadHotel_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleAgregarImagenAmenidad_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleAgregarAmenidadHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleCrearHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCrearHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEliminarHabitacion_rolAdmin_eliminaHabitacionExitosamente `(1ms)`

**[PASO]** handleListarAmenidadesHotel_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHotel_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleListarHabitaciones_rolAdmin_retornaHabitacionesDelHotel `(2ms)`

**[PASO]** handleAgregarImagenHabitacion_servicioLanzaExcepcion_retorna400 `(4ms)`

**[PASO]** handleEditarHotel_rolAdmin_editaHotelExitosamente `(4ms)`

**[PASO]** handleEliminarImagenHabitacion_rolAdmin_eliminaImagenExitosamente `(3ms)`

**[PASO]** handleListarAmenidades_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleCancelarReservacion_rolAdminConMotivo_cancelaYRetornaRespuesta `(31ms)`

**[PASO]** handleCerrarHabitacion_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleListarHoteles_rolAdmin_retornaListaDeHoteles `(3ms)`

**[PASO]** handleReservasActivasHabitacion_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleListarAmenidades_rolAdmin_retornaListaDelServicio `(2ms)`

**[PASO]** handleCrearHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleReactivarHotel_sinRolAdmin_retorna403 `(3ms)`

**[PASO]** handleCancelarReservacion_bodyLanzaExcepcion_usaMotivoDefault `(2ms)`

**[PASO]** handleReactivarHotel_hotelNoEncontrado_retorna404 `(3ms)`

**[PASO]** handleCancelarReservacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleCerrarHotel_rolAdmin_cierraHotelExitosamente `(2ms)`

**[PASO]** handleReactivarHabitacion_rolAdmin_reactivaHabitacionExitosamente `(2ms)`

**[PASO]** handleListarCiudades_rolAdmin_retornaListaDeCiudades `(13ms)`

**[PASO]** handleEliminarImagenHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleActualizarAmenidadHotel_rolAdmin_actualizaAmenidadExitosamente `(2ms)`

**[PASO]** handleAgregarImagenAmenidad_rolAdmin_agregaImagenYRetorna201 `(3ms)`

**[PASO]** handleAgregarImagenHotel_sinRolAdmin_retorna403 `(6ms)`

**[PASO]** handleAgregarImagenHabitacion_rolAdmin_agregaImagenYRetorna201 `(1ms)`

**[PASO]** handleReservasActivasHabitacion_rolAdmin_retornaReservasActivas `(2ms)`

**[PASO]** handleListarPaises_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleReactivarHotel_rolAdmin_reactivaHotelExitosamente `(8ms)`

**[PASO]** handleObtenerMetricas_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleEditarHabitacion_sinRolAdmin_retorna403 `(1ms)`

**[PASO]** handleEliminarAmenidadHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleAgregarImagenHotel_servicioLanzaExcepcion_retorna400 `(3ms)`

**[PASO]** handleCancelarReservacion_servicioLanzaExcepcion_retorna400 `(2ms)`

**[PASO]** handleReservasActivasHotel_sinRolAdmin_retorna403 `(2ms)`

**[PASO]** handleActualizarAmenidadHotel_sinRolAdmin_retorna403 `(4ms)`

**[PASO]** handleListarReservaciones_sinRolAdmin_retorna403 `(3ms)`

---

### controllers.ImagenControllerTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** handleObtenerImagenAmenidad_imagenExiste_retornaImagenJpeg `(40ms)`

**[PASO]** handleObtenerImagenHotel_imagenNoExiste_retorna404 `(6ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenExiste_retornaImagenJpeg `(3ms)`

**[PASO]** handleObtenerImagenAmenidad_imagenNoExiste_retorna404 `(3ms)`

**[PASO]** handleObtenerImagenHabitacion_imagenNoExiste_retorna404 `(4ms)`

**[PASO]** handleObtenerImagenHotel_imagenExiste_retornaImagenJpeg `(3ms)`

---

### controllers.PagoAgenciaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleProcesarPago_authFalla_noInvocaServicio `(33ms)`

**[PASO]** handleProcesarPago_errorPasarela_retorna500ConMensaje `(27ms)`

**[PASO]** handleProcesarPago_pagoValido_retornaConfirmacion200 `(3ms)`

**[PASO]** handleProcesarPago_pagoInvalido_retorna400ConMensaje `(5ms)`

---

### controllers.PagoControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleProcesarPago_errorRuntime_retorna500 `(69ms)`

**[PASO]** handleProcesarPago_pagoExitoso_retorna200 `(2ms)`

**[PASO]** handleProcesarPago_argumentoInvalido_retorna400 `(2ms)`

---

### controllers.PdfReservacionControllerTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** handleDescargarPdf_reservacionNoEncontrada_retorna404 `(31ms)`

**[PASO]** handleDescargarPdf_reservacionValida_retornaPdfComoAdjunto `(11ms)`

---

### controllers.ReservacionAgenciaControllerTest

- Tests: 13
- Pasaron: 13
- Fallaron: 0

**[PASO]** handleObtenerDetalleReservacion_reservacionNoEncontrada_retorna404ConMensaje `(41ms)`

**[PASO]** handleCrearReservacion_errorInterno_retorna500ConMensaje `(26ms)`

**[PASO]** handleExpirarReservacion_reservacionValida_expiraYRetornaMensaje `(3ms)`

**[PASO]** handleObtenerReservaciones_agenciaValida_retornaLista200 `(3ms)`

**[PASO]** handleExpirarReservacion_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleObtenerDetalleReservacion_errorInterno_retorna500ConMensaje `(4ms)`

**[PASO]** handleObtenerDetalleReservacion_reservacionExiste_retornaDetalle200 `(2ms)`

**[PASO]** handleCrearReservacion_datosInvalidos_retorna400ConMensaje `(6ms)`

**[PASO]** handleExpirarReservacion_reservacionInvalida_retorna400ConMensaje `(4ms)`

**[PASO]** handleObtenerDetalleReservacion_authFalla_noInvocaServicio `(1ms)`

**[PASO]** handleCrearReservacion_authFalla_noInvocaServicio `(3ms)`

**[PASO]** handleObtenerReservaciones_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleCrearReservacion_requestValido_retornaReservacion201 `(4ms)`

---

### controllers.ReservacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleCrearReservacion_datosValidos_retorna201 `(31ms)`

**[PASO]** handleObtenerReservaciones_usuarioConReservaciones_retorna200ConLista `(9ms)`

**[PASO]** handleCrearReservacion_errorRuntime_retorna500 `(2ms)`

**[PASO]** handleCrearReservacion_argumentoInvalido_retorna400 `(2ms)`

**[PASO]** handleObtenerReservaciones_usuarioSinReservaciones_retorna200ConListaVacia `(2ms)`

---

### controllers.SesionControllerTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** handleSesion_tokenInvalido_retornaSinSesion `(31ms)`

**[PASO]** handleSesion_tokenValido_retornaConSesion `(5ms)`

**[PASO]** handleSesion_sinToken_retornaSinSesion `(2ms)`

---

### controllers.TokenAerolineaControllerTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** handleGenerarToken_authOkYDatosValidos_retorna201ConToken `(57ms)`

**[PASO]** handleGenerarToken_authOkYTokenHashNulo_servicioLlamadoConNulo `(3ms)`

**[PASO]** handleGenerarToken_authFalla_noInvocaServicio `(2ms)`

**[PASO]** handleGenerarToken_servicioLanzaExcepcion_retorna400 `(5ms)`

---

### controllers.TokenValidacionControllerTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** handleValidar_tokenNull_retorna400SinLlamarServicio `(29ms)`

**[PASO]** handleValidar_tokenVacio_retorna400SinLlamarServicio `(3ms)`

**[PASO]** handleValidar_tokenBlanco_retorna400SinLlamarServicio `(3ms)`

**[PASO]** handleValidar_tokenValido_retorna200ConResultado `(4ms)`

**[PASO]** handleValidar_tokenExpirado_retorna400ConMensaje `(4ms)`

---

### controllers.UsuarioControllerTest

- Tests: 15
- Pasaron: 15
- Fallaron: 0

**[PASO]** handleRegistrar_camposDuplicados_retorna409 `(208ms)`

**[PASO]** handleCambiarTelefono_telefonoInvalido_retorna400 `(4ms)`

**[PASO]** handleObtenerPerfil_usuarioAutenticado_retornaPerfil `(3ms)`

**[PASO]** handleCambiarContrasena_credencialesValidas_retorna200 `(4ms)`

**[PASO]** handleCambiarRol_rolNoAutorizado_retorna403 `(3ms)`

**[PASO]** handleListarAdmin_rolAdministrador_retornaLista `(2ms)`

**[PASO]** handleCambiarTelefono_telefonoValido_retorna200 `(3ms)`

**[PASO]** handleCambiarRol_adminActualiza_retorna200 `(5ms)`

**[PASO]** handleValidar_servicioRetornaFalse_retornaResultadoNoDisponible `(3ms)`

**[PASO]** handleCambiarRol_rolInvalido_retorna400 `(4ms)`

**[PASO]** handleValidar_requestValido_retornaResultado `(4ms)`

**[PASO]** handleCambiarContrasena_credencialesInvalidas_retorna401 `(26ms)`

**[PASO]** handleRegistrar_nuevoUsuario_retorna201ConId `(3ms)`

**[PASO]** handleObtenerPerfil_diferentesUsuarios_llamaServicioConIdCorrecto `(2ms)`

**[PASO]** handleListarAdmin_rolNoAutorizado_retorna403 `(3ms)`

---

### data.DataAccessExceptionTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** sePuedeLanzarYCapturar_comoRuntimeException `(2ms)`

**[PASO]** constructor_conMensajeYCausa_almacenaAmbosValores `(5ms)`

**[PASO]** mensajeDescriptivo_sePropaga_correctamente `(1ms)`

**[PASO]** causa_puedeSerSQLException_simulada `(4ms)`

**[PASO]** sePuedeCapturar_comoDataAccessException `(2ms)`

**[PASO]** esSubclase_deRuntimeException `(1ms)`

---

### data.ResultSetMapperTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** implementacion_conLambda_mapeaStringCorrectamente `(210ms)`

**[PASO]** implementacion_propagaSQLException_cuandoResultSetFalla `(2ms)`

**[PASO]** implementacion_conLambda_construyeObjetoCompuesto `(2ms)`

**[PASO]** implementacion_conLambda_mapeaDoubleCorrectamente `(1ms)`

**[PASO]** implementacion_conLambda_mapeaEnteroCorrectamente `(3ms)`

---

### dtos.DtosTest

- Tests: 78
- Pasaron: 78
- Fallaron: 0

**[PASO]** puedeCancelarDTO_constructorFalse_almacenaCorrecto `(0ms)`

**[PASO]** crearHotelRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_constructor_almacenaBooleans `(2ms)`

**[PASO]** tokenValidacionResponseDTO_constructor_almacenaTodosLosCampos `(0ms)`

**[PASO]** aerolineaIdentidadDTO_constructor_almacenaNombreYUrl `(1ms)`

**[PASO]** cambiarContrasenaRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** editarHotelRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** hotelAdminDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionAgenciaDTO_settersYGetters_funcionan `(3ms)`

**[PASO]** reservacionRequestDTO_setterHabitaciones_funciona `(2ms)`

**[PASO]** hotelResultadoDTO_listas_seAsignanCorrectamente `(2ms)`

**[PASO]** reservacionResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** editarAerolineaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** crearAerolineaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** loginResponseDTO_constructor_almacenaTodosLosCampos `(1ms)`

**[PASO]** cambiarRolRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** usuarioAdminDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_setter_sobrescribeToken `(0ms)`

**[PASO]** crearAerolineaAdminRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** ciudadDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** reservacionDetalleDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** paisDTO_constructor_almacenaIdYNombre `(0ms)`

**[PASO]** downResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** agregarAmenidadRequestDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** comentarioRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** cancelacionRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** paisDTO_setters_sobrescribenValores `(1ms)`

**[PASO]** reservacionAgenciaResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** usuarioValidacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** tokenAerolineaResponseDTO_constructor_almacenaTodosLosCampos `(0ms)`

**[PASO]** busquedaRequestDTO_valoresPorDefecto_sonNullOCero `(1ms)`

**[PASO]** downRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** downRequestDTO_valorNegativo_seAlmacenaCorrectamente `(2ms)`

**[PASO]** usuarioPerfilResponseDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** tokenValidacionResponseDTO_porcentajeCero_seAlmacenaCorrectamente `(1ms)`

**[PASO]** editarAgenciaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** resultadoNotificacionDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** tokenAerolineaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** aerolineaAdminDTO_valoresPorDefecto_sonCero `(2ms)`

**[PASO]** comentarioResponseDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** habitacionAgenciaResponseDTO_settersYGetters_funcionan `(2ms)`

**[PASO]** reservacionRequestDTO_listaVacia_seAsignaCorrectamente `(1ms)`

**[PASO]** habitacionAdminDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** pagoRequestDTO_settersYGetters_facturacion_funcionan `(0ms)`

**[PASO]** cambiarTelefonoRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** usuarioWebserviceLibreDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** sesionDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** loginRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** agenciaIdentidad_settersYGetters_funcionan `(1ms)`

**[PASO]** tipoHabitacionResultadoDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** loginResponseDTO_diferencteRol_seAlmacenaCorrectamente `(0ms)`

**[PASO]** busquedaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** agenciaDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** amenidadDTO_setters_sobrescribenValores `(4ms)`

**[PASO]** hotelAmenidadDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** editarHabitacionRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** habitacionReservaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** crearAgenciaRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** sesionDTO_autenticadoFalsePorDefecto `(0ms)`

**[PASO]** usuarioValidacionResponseDTO_todosFalse_cuandoNingunExiste `(0ms)`

**[PASO]** crearAgenciaAdminRequestDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** amenidadHotelDTO_settersYGetters_funcionan `(8ms)`

**[PASO]** hotelAgenciaDTO_settersYGetters_funcionan `(0ms)`

**[PASO]** hotelResultadoDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** aerolineaWebserviceDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** puedeCancelarDTO_constructorTrue_almacenaCorrecto `(0ms)`

**[PASO]** habitacionResumenDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** crearHabitacionRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** handshakeResponseDTO_constructor_almacenaToken `(1ms)`

**[PASO]** amenidadDTO_constructor_almacenaIdYNombre `(2ms)`

**[PASO]** pagoAgenciaRequestDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** subirImagenRequestDTO_setterYGetter_funcionan `(0ms)`

**[PASO]** loginRequestDTO_valoresPorDefecto_sonNull `(0ms)`

**[PASO]** habitacionDTO_settersYGetters_funcionan `(1ms)`

**[PASO]** agenciaDTO_valoresPorDefecto_sonCero `(0ms)`

**[PASO]** pagoResponseDTO_settersYGetters_funcionan `(0ms)`

---

### helpers.AerolineaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(71ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAerolinea `(2ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(4ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(4ms)`

---

### helpers.AgenciaAuthMiddlewareTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** verificar_tokenNoReconocido_retornaFalseYStatus401 `(42ms)`

**[PASO]** verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo `(4ms)`

**[PASO]** verificar_tokenValido_retornaTrueEInyectaAtributosDeAgencia `(2ms)`

**[PASO]** verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo `(3ms)`

---

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(9ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(0ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(3ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(1ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(5ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(1ms)`

---

### helpers.EmailHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** enviar_datosValidos_noLanzaExcepcion `(4ms)`

**[PASO]** enviar_cuerpoMinimo_noLanzaExcepcion `(10ms)`

**[PASO]** enviar_argumentosExactos_invocaMetodoConParametrosCorrectos `(3ms)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(600ms)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(161ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(3ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(1ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(4ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(2ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(2ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(0ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(2ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(2ms)`

**[PASO]** getRolId_retornaRolCorrecto `(3ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(2ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(408ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(729ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(601ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(583ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(433ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(54ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(4ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(5ms)`

**[PASO]** validar_numeroConEspacios_esValido `(5ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(2ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(1ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(1ms)`

**[PASO]** validar_cvv4digitos_esValido `(2ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(1ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(1ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(2ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(1ms)`

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

**[PASO]** ciudadId_conValor_seAlmacenaCorrectamente `(1ms)`

**[PASO]** constructorVacio_creaInstanciaNoNula `(0ms)`

**[PASO]** settersYGetters_camposNumericos_funcionan `(2ms)`

**[PASO]** settersYGetters_camposTexto_funcionan `(0ms)`

---

### repositories.AdminReservacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarTodas_retornaListaConReservacionDePrueba `(8.9s)`

**[PASO]** obtenerReservacion_idExistente_retornaDatos `(1.3s)`

**[PASO]** obtenerReservacion_idInexistente_retornaNull `(2.5s)`

**[PASO]** obtenerDatosUsuarioPorReservacion_reservacionExistente_retornaDatos `(1.2s)`

**[PASO]** cancelarReservacion_reservacionPendiente_cambiaEstado `(1.3s)`

---

### repositories.AerolineaAdminRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarTodas_retornaListaNoNula `(606ms)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(1.1s)`

**[PASO]** editar_aerolineaExistente_actualizaDatos `(606ms)`

**[PASO]** listarWebserviceLibres_retornaListaNoNula `(553ms)`

---

### repositories.AerolineaAliadaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerAerolineaPorToken_tokenActivo_retornaDto `(722ms)`

**[PASO]** obtenerAerolineaPorToken_tokenInexistente_retornaNull `(692ms)`

**[PASO]** obtenerDescuentoAerolinea_tokenActivo_retornaDescuentoPositivo `(723ms)`

**[PASO]** buscarCiudadId_ciudadExistente_retornaId `(647ms)`

**[PASO]** guardarBusqueda_datosValidos_persisteEnOracle `(758ms)`

**[PASO]** obtenerAerolineaIdPorURL_urlExistente_retornaId `(702ms)`

**[PASO]** guardarTokensAerolinea_datosValidos_actualizaToken `(724ms)`

---

### repositories.AerolineaWebserviceRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAerolinea_retornaListaConAlMenosUna `(518ms)`

**[PASO]** crear_datosValidos_retornaAerolineaConId `(1s)`

**[PASO]** cambiarEstado_aerolineaActiva_actualizaEstado `(672ms)`

**[PASO]** listarPorUsuario_usuarioSinAerolinea_retornaListaVacia `(558ms)`

---

### repositories.AgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioConAgencia_retornaListaConAlMenosUna `(587ms)`

**[PASO]** crear_datosValidos_retornaAgenciaConId `(1.2s)`

**[PASO]** editar_datosNuevos_actualizaNombre `(2.8s)`

**[PASO]** cambiarEstado_agenciaActiva_cambiaEstado `(1.1s)`

**[PASO]** obtenerAgenciaPorToken_sinToken_retornaNull `(2.1s)`

---

### repositories.AuthRepositoryIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** buscarPorIdentificador_porUsername_retornaUsuario `(1s)`

**[PASO]** buscarPorIdentificador_porCorreo_retornaUsuario `(617ms)`

**[PASO]** buscarPorIdentificador_identificadorInexistente_retornaNull `(366ms)`

---

### repositories.BusquedaAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(502ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(642ms)`

**[PASO]** obtenerDescuentoAgencia_usuarioConAgencia_retornaDescuento `(515ms)`

**[PASO]** guardarBusqueda_datosValidos_noLanzaExcepcion `(548ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(511ms)`

---

### repositories.BusquedaRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo `(123ms)`

**[PASO]** buscarCiudadId_ciudadInexistente_retornaNull `(135ms)`

**[PASO]** guardarBusqueda_sinUsuario_noLanzaExcepcion `(149ms)`

**[PASO]** buscarHotelesPorCiudad_retornaListaNoNula `(106ms)`

**[PASO]** buscarImagenesHotel_hotelInexistente_retornaListaVacia `(136ms)`

**[PASO]** buscarAmenidadesHotel_hotelInexistente_retornaListaVacia `(119ms)`

**[PASO]** buscarImagenesHabitacion_habitacionInexistente_retornaListaVacia `(107ms)`

---

### repositories.CancelacionRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaCancelar_reservacionDelUsuario_retornaDatos `(453ms)`

**[PASO]** obtenerReservacionParaCancelar_otroUsuarioId_retornaNull `(475ms)`

**[PASO]** obtenerFechaCheckInMasReciente_sinDetalles_retornaNull `(462ms)`

**[PASO]** cancelarReservacion_estadoPendiente_actualizaAEstado4 `(538ms)`

**[PASO]** obtenerReservacionAgenciaParaCancelar_sinAgenciaVinculada_retornaNull `(568ms)`

---

### repositories.ComentarioRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** existeComentarioConResena_sinComentarios_retornaFalse `(584ms)`

**[PASO]** crearComentario_conResena_retornaIdPositivo `(615ms)`

**[PASO]** existeComentarioConResena_despuesDeCrear_retornaTrue `(598ms)`

**[PASO]** crearComentario_sinResena_esRespuestaAOtro `(620ms)`

**[PASO]** actualizarRatingHotel_conResena_noLanzaExcepcion `(2s)`

**[PASO]** obtenerComentario_comentarioExistente_retornaDtoConDatos `(591ms)`

**[PASO]** obtenerComentariosPorUsuario_retornaListaConAlMenosUno `(601ms)`

**[PASO]** obtenerComentariosPorHotel_retornaListaConAlMenosUno `(636ms)`

---

### repositories.DestinosRepositoryIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerTodosLosHoteles_retornaListaNoNula `(43ms)`

**[PASO]** obtenerTodosLosHoteles_conHotelesActivos_retornaDtosValidos `(47ms)`

**[PASO]** obtenerImagenesHotel_hotelExistente_retornaListaNoNula `(69ms)`

**[PASO]** obtenerImagenesHotel_hotelInexistente_retornaListaVacia `(35ms)`

---

### repositories.DownsRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerValorDown_sinDown_retornaNull `(765ms)`

**[PASO]** insertarDown_registraDown_obtenibleEnOracle `(820ms)`

**[PASO]** obtenerDownsDeUsuario_trasInsertarDown_retornaListaConDown `(788ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_filtraPorHotel `(807ms)`

**[PASO]** actualizarContadorDown_incrementaContador `(807ms)`

**[PASO]** eliminarDown_eliminaDown_obtenerValorRetornaNull `(860ms)`

---

### repositories.HotelRepositoryIntegrationTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** listarAmenidades_retornaListaNoNula `(438ms)`

**[PASO]** crearAmenidad_nombreValido_retornaIdPositivo `(486ms)`

**[PASO]** listarTodos_retornaListaConElHotelInsertado `(460ms)`

**[PASO]** actualizarHotel_datosNuevos_actualizaNombreEnOracle `(491ms)`

**[PASO]** cerrarHotel_hotelActivo_cambiaEstadoId `(438ms)`

**[PASO]** reactivarHotel_hotelCerrado_restauraEstadoId `(542ms)`

**[PASO]** existe_hotelExistente_retornaTrue `(456ms)`

**[PASO]** crearHabitacion_datosValidos_retornaIdPositivo `(581ms)`

**[PASO]** obtenerMetricas_retornaMapaConClaves `(648ms)`

---

### repositories.ImagenRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerImagenHotel_idExistente_retornaBytes `(259ms)`

**[PASO]** obtenerImagenHotel_idInexistente_retornaNull `(268ms)`

**[PASO]** obtenerImagenHabitacion_idExistente_retornaBytes `(233ms)`

**[PASO]** obtenerImagenHabitacion_idInexistente_retornaNull `(270ms)`

**[PASO]** eliminarImagenHotel_eliminaImagen_noObtenible `(236ms)`

**[PASO]** eliminarImagenHabitacion_eliminaImagen_noObtenible `(233ms)`

**[PASO]** obtenerImagenAmenidad_idInexistente_retornaNull `(227ms)`

---

### repositories.PagoAgenciaRepositoryIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDeAgencia_retornaDatos `(915ms)`

**[PASO]** obtenerReservacionParaPago_agenciaIncorrecta_retornaNull `(908ms)`

**[PASO]** confirmarReservacion_estadoPendiente_actualizaAEstado2 `(981ms)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(926ms)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(1s)`

---

### repositories.PagoRepositoryIntegrationTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** obtenerReservacionParaPago_reservacionDelUsuario_retornaDatosCorrectos `(815ms)`

**[PASO]** obtenerReservacionParaPago_otroUsuarioId_retornaNull `(847ms)`

**[PASO]** confirmarReservacion_estadoPendiente_cambiaAEstado2 `(879ms)`

**[PASO]** crearFactura_datosValidos_retornaIdPositivo `(832ms)`

**[PASO]** obtenerFactura_facturaExistente_retornaDtoConDatos `(885ms)`

**[PASO]** actualizarTotalReservacion_nuevoTotal_actualizaCorrectamente `(830ms)`

**[PASO]** obtenerCiudadReservacion_conDetallesYHotel_retornaNombreCiudad `(793ms)`

---

### repositories.ReservacionAgenciaRepositoryIntegrationTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerDescuentoAgencia_agenciaConDescuento_retornaValorPositivo `(1.1s)`

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(1.1s)`

**[PASO]** existeTraslape_fechasSinConflicto_retornaFalse `(1.1s)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(1.1s)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatos `(1.1s)`

**[PASO]** expirarReservacion_reservacionPendiente_actualizaEstado `(1.2s)`

---

### repositories.ReservacionRepositoryIntegrationTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** obtenerPrecios_habitacionExistente_retornaArrayConPrecios `(930ms)`

**[PASO]** existeTraslape_sinReservacionesConflicto_retornaFalse `(1.8s)`

**[PASO]** existeTraslape_conReservacionConflicto_retornaTrue `(758ms)`

**[PASO]** crearReservacion_datosValidos_retornaIdPositivo `(791ms)`

**[PASO]** obtenerReservacion_reservacionExistente_retornaDatosCorrectos `(849ms)`

**[PASO]** obtenerReservacionesDeUsuario_conDetalle_retornaListaConAlMenosUno `(822ms)`

**[PASO]** expirarReservacionesVencidas_noLanzaExcepcion_retornaEntero `(800ms)`

**[PASO]** obtenerImagenesHotel_hotelSinImagenes_retornaListaVacia `(810ms)`

---

### repositories.UsuarioRepositoryIntegrationTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** existeUsername_usernameExistente_retornaTrue `(395ms)`

**[PASO]** existeUsername_usernameInexistente_retornaFalse `(374ms)`

**[PASO]** existeCorreo_correoExistente_retornaTrue `(389ms)`

**[PASO]** existePasaporte_pasaporteExistente_retornaTrue `(380ms)`

**[PASO]** existePasaporte_pasaporteNuloOVacio_retornaFalse `(350ms)`

**[PASO]** crearUsuario_datosValidos_retornaIdPositivo `(671ms)`

**[PASO]** obtenerPerfil_usuarioExistente_retornaDtoConDatos `(378ms)`

**[PASO]** actualizarTelefono_usuarioExistente_cambiaElCampo `(459ms)`

**[PASO]** obtenerContrasena_usuarioExistente_retornaHashNoNulo `(392ms)`

**[PASO]** listarTodosConRol_retornaListaNoNula `(393ms)`

---

### services.AdminBusquedaServiceIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarSinFiltrosRetornaResultadosYTotalCorrecto `(609ms)`

**[PASO]** listarConFiltroDestinoFiltraCorrectamente `(632ms)`

**[PASO]** listarConTipoWebRetornaSoloBusquedasWeb `(603ms)`

**[PASO]** listarPaginacionRetornaSegundaPagina `(690ms)`

**[PASO]** resumenRetornaEstructuraCompletaDesdeOracle `(679ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(56ms)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(1ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(2ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(1ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(2ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(1ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(4s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(1ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(2.7s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(1ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(2ms)`

---

### services.AdminReservacionServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(74ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(2ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacionYNotificaAgencia `(0ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(2ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacionYNotificaAgencia `(0ms)`

**[PASO]** cancelarReservacion_notificadorRetornaErrorHTTP_completaCancelacionConError `(1ms)`

**[PASO]** cancelarReservacion_notificadorRetornaNoEsReservaAgencia_completaCancelacion `(2ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(1ms)`

---

### services.AerolineaAdminServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarWebserviceLibres_conUsuariosDisponibles_retornaListaDelRepo `(45ms)`

**[PASO]** editar_requestValido_delegaAlRepo `(1ms)`

**[PASO]** listarTodas_conAerolineas_retornaListaDelRepo `(2ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(0ms)`

**[PASO]** listarWebserviceLibres_todosAsignados_retornaListaVacia `(1ms)`

**[PASO]** listarTodas_sinAerolineas_retornaListaVacia `(2ms)`

**[PASO]** editar_idDistinto_invocaRepoConIdCorrecto `(0ms)`

---

### services.AerolineaWebserviceServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** listarPorUsuario_usuarioSinAerolineas_retornaListaVacia `(39ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(0ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(0ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentExceptionSinInvocarRepo `(2ms)`

**[PASO]** crear_requestValido_retornaAerolineaCreadaDelRepo `(0ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(1ms)`

**[PASO]** listarPorUsuario_usuarioConAerolineas_retornaListaDelRepo `(0ms)`

---

### services.AgenciaNotificadorExternoServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** notificarCancelacion_agenciaSinToken_retornaErrorSinHTTP `(625ms)`

**[PASO]** notificarCancelacion_httpError500_retornaEnviadoTrueConStatus500 `(7ms)`

**[PASO]** notificarCancelacion_agenciaSinURL_retornaErrorSinHTTP `(3ms)`

**[PASO]** notificarCancelacion_httpExitoso200_retornaEnviadoTrueYStatusCorrecto `(3ms)`

**[PASO]** notificarCancelacion_excepcionRed_retornaErrorEnDTOSinPropagar `(5ms)`

**[PASO]** notificarCancelacion_noEsReservaDeAgencia_retornaEsReservaFalseYSinHTTP `(1ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(0ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(0ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(3ms)`

**[PASO]** listarTodas_retornaListaCompleta `(3ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(45ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(3ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(4ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(4ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(2ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(687ms)`

**[PASO]** loginExitosoConCorreo `(635ms)`

**[PASO]** loginFallaUsuarioInexistente `(350ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(667ms)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(583ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(507ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(2ms)`

---

### services.BusquedaAerolineaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_guardaBusquedaEnRepo `(1ms)`

**[PASO]** buscar_conDescuento10Porciento_aplicaDescuentoAPrecios `(3ms)`

**[PASO]** buscar_tokenInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscar_dosHotelesEnCiudad_retornaListaConDosHoteles `(3ms)`

**[PASO]** buscar_tokenValidoCiudadExistente_retornaListaDeHoteles `(2ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(73ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(1ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(0ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(0ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(0ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(1ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(1.5s)`

**[PASO]** busquedaRegistraEventoEnOracle `(1.5s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(1.5s)`

**[PASO]** busquedaFallaCiudadInexistente `(482ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(49ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(3ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(2ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(1ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(1ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(2ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(634ms)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(580ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(655ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(37ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(1ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(2ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(0ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(1ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(2ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(0ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(0ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(0ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(45ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(1ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(0ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(1ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(2ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(2ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(0ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(91ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(2ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(1ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(2ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(40ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(2ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(1ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(0ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(1ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(2ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(2ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(0ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(1ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(1ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(0ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(48ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(1.8s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(0ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(1ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(43ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(3ms)`

**[PASO]** iniciar_noLanzaExcepcion `(3ms)`

**[PASO]** detener_noLanzaExcepcion `(1ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(2ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(2ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(2ms)`

---

### services.HandshakeAerolineaServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(7ms)`

**[PASO]** procesarHandshake_urlRegistrada_retornaResponseDTOConTokenDeSalida `(1ms)`

**[PASO]** procesarHandshake_tokensNoPersistidos_lanzaIllegalArgumentException `(5ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(2ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(47ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(2ms)`

---

### services.HotelServiceTest

- Tests: 69
- Pasaron: 69
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(135ms)`

**[PASO]** agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(2ms)`

**[PASO]** editarHabitacion_tipoInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** eliminarAmenidadHotel_delegaAlRepositorio `(0ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero `(0ms)`

**[PASO]** agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** listarTodasReservaciones_delegaAlRepositorio `(0ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarImagenHabitacion_delegaAlRepositorio `(1ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(3ms)`

**[PASO]** listarPaises_delegaAlRepositorioDePaises `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva `(3ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerMetricas_delegaAlRepositorio `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo `(4ms)`

**[PASO]** cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException `(3ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra `(5ms)`

**[PASO]** listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException `(6ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarImagenHabitacion_habitacionExiste_retornaMapaConId `(1ms)`

**[PASO]** reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero `(0ms)`

**[PASO]** agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarHabitaciones_hotelExiste_retornaListaConImagenes `(0ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos `(2ms)`

**[PASO]** editarHabitacion_habitacionExiste_invocaActualizarHabitacion `(1ms)`

**[PASO]** eliminarImagenAmenidad_delegaAlRepositorio `(1ms)`

**[PASO]** eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar `(0ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(0ms)`

**[PASO]** listarCiudades_delegaAlRepositorioDePaises `(0ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(2ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(1ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra `(5ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion `(3ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel `(1ms)`

**[PASO]** agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarImagenHotel_delegaAlRepositorio `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar `(4ms)`

**[PASO]** agregarImagenAmenidad_base64Valido_retornaMapaConId `(2ms)`

**[PASO]** obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(2ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos `(2ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarAmenidadHotel_delegaAlRepositorio `(1ms)`

**[PASO]** reactivarHabitacion_habitacionExiste_invocaReactivar `(1ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** reactivarHotel_hotelExiste_invocaReactivarHotel `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException `(1ms)`

**[PASO]** editarHabitacion_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarImagenHotel_hotelExiste_retornaMapaConId `(1ms)`

**[PASO]** listarAmenidades_delegaAlRepositorio `(2ms)`

**[PASO]** eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(46ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(0ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(0ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(47ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(0ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(800ms)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(573ms)`

**[PASO]** pagoFallaCvvInvalido `(547ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(79ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(1ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(0ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(1ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(2ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(42ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(2ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(51ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(1ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(0ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(0ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(1ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(3ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(765ms)`

**[PASO]** crearReservacionFallaSinHabitaciones `(402ms)`

**[PASO]** crearReservacionFallaFechaPasada `(401ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(2ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(2ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(0ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(2ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(39ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(0ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(2ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(0ms)`

---

### services.TokenAerolineaServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** generarToken_datosValidos_insertaTokenConIdsCorrectos `(43ms)`

**[PASO]** generarToken_tokenValidoCiudadExistente_retornaResponseDTOCompleto `(2ms)`

**[PASO]** generarToken_ciudadNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** generarToken_tokenInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** generarToken_datosValidos_urlRedireccionContieneUrlBaseYToken `(3ms)`

---

### services.TokenValidacionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** validar_tokenInexistente_lanzaIllegalArgumentException `(3ms)`

**[PASO]** validar_tokenExpirado_lanzaIllegalArgumentException `(1ms)`

**[PASO]** validar_tokenValido_retornaTokenValidacionResponseDTO `(0ms)`

**[PASO]** validar_tokenYaUtilizado_lanzaIllegalArgumentException `(1ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(113ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(2ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(1ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(0ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(0ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(0ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(2ms)`

**[PASO]** validarDisponibilidad_todosLibres `(0ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(517ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(0ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(2.5s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(1ms)`

---
