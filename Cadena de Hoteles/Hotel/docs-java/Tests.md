# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 310
- Pasaron: 310
- Fallaron: 0
- Saltados: 0
- Duracion: 48.5s
- Ejecutado: 22/04/2026 22:25:55

---

## Suites

### clients.MoventClientTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall `(52ms)`

**[PASO]** notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(290ms)`

**[PASO]** notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion `(16ms)`

**[PASO]** notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall `(3ms)`

**[PASO]** notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible `(11ms)`

**[PASO]** notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall `(3ms)`

**[PASO]** notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall `(4ms)`

**[PASO]** notificarHabitacionCerrada_multiples_reservas_swallowsExcepcion `(10ms)`

---

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(4ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(0ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(3ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(2ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(1ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(3ms)`

---

### helpers.EmailHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** enviar_conDatosValidos_completaSinExcepcion `(3.4s)`

**[PASO]** enviar_conCuerpoMinimo_completaSinExcepcion `(3.4s)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(204ms)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(60ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(1ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(3ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(2ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(2ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(1ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(1ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(1ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(1ms)`

**[PASO]** getRolId_retornaRolCorrecto `(2ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(1ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(240ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(462ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(462ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(457ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(347ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(46ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(2ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(3ms)`

**[PASO]** validar_numeroConEspacios_esValido `(5ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(1ms)`

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

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(1ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(0ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(1ms)`

---

### services.AdminBusquedaServiceIntegrationTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** listarSinFiltrosRetornaResultadosYTotalCorrecto `(1.5s)`

**[PASO]** listarConFiltroDestinoFiltraCorrectamente `(708ms)`

**[PASO]** listarConTipoWebRetornaSoloBusquedasWeb `(709ms)`

**[PASO]** listarPaginacionRetornaSegundaPagina `(751ms)`

**[PASO]** resumenRetornaEstructuraCompletaDesdeOracle `(768ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(787ms)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(7ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(5ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(5ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(6ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(4ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(3.9s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(2ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(2.6s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(3ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(2ms)`

---

### services.AdminReservacionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(68ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(1ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacion `(2ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(1ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacion `(3ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(4ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(43ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodas_retornaListaCompleta `(1ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(1ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(0ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(0ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(1ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(579ms)`

**[PASO]** loginExitosoConCorreo `(553ms)`

**[PASO]** loginFallaUsuarioInexistente `(350ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(550ms)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(431ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(431ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(3ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(55ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(4ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(4ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(3ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(4ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(3ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(4ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(2.4s)`

**[PASO]** busquedaRegistraEventoEnOracle `(2.4s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(2.8s)`

**[PASO]** busquedaFallaCiudadInexistente `(663ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(81ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(3ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(1ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(1ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(2ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(1ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(916ms)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(844ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(966ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(74ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(3ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(4ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(3ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(3ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(3ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(3ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(4ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(3ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(2ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(1ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(1ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(71ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(3ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(3ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(1ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(1ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(1ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(127ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(2ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(5ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(1ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(62ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(4ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(1ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(3ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(3ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(3ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(1ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(1ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(3ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(5ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(51ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(3.7s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(1ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(1ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(39ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(1ms)`

**[PASO]** iniciar_noLanzaExcepcion `(1ms)`

**[PASO]** detener_noLanzaExcepcion `(0ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(1ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(2ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(1ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(3ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(37ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(0ms)`

---

### services.HotelServiceTest

- Tests: 69
- Pasaron: 69
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(128ms)`

**[PASO]** agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(2ms)`

**[PASO]** editarHabitacion_tipoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(1ms)`

**[PASO]** eliminarAmenidadHotel_delegaAlRepositorio `(2ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero `(1ms)`

**[PASO]** agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodasReservaciones_delegaAlRepositorio `(0ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarImagenHabitacion_delegaAlRepositorio `(3ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarPaises_delegaAlRepositorioDePaises `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva `(1ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerMetricas_delegaAlRepositorio `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo `(1ms)`

**[PASO]** cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra `(36ms)`

**[PASO]** listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarImagenHabitacion_habitacionExiste_retornaMapaConId `(3ms)`

**[PASO]** reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero `(2ms)`

**[PASO]** agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarHabitaciones_hotelExiste_retornaListaConImagenes `(1ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos `(2ms)`

**[PASO]** editarHabitacion_habitacionExiste_invocaActualizarHabitacion `(1ms)`

**[PASO]** eliminarImagenAmenidad_delegaAlRepositorio `(1ms)`

**[PASO]** eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar `(0ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(1ms)`

**[PASO]** listarCiudades_delegaAlRepositorioDePaises `(2ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(2ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(0ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra `(1ms)`

**[PASO]** cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion `(0ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException `(0ms)`

**[PASO]** eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel `(0ms)`

**[PASO]** agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarImagenHotel_delegaAlRepositorio `(1ms)`

**[PASO]** cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar `(1ms)`

**[PASO]** agregarImagenAmenidad_base64Valido_retornaMapaConId `(3ms)`

**[PASO]** obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(1ms)`

**[PASO]** obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos `(2ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException `(1ms)`

**[PASO]** actualizarAmenidadHotel_delegaAlRepositorio `(0ms)`

**[PASO]** reactivarHabitacion_habitacionExiste_invocaReactivar `(1ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(0ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** reactivarHotel_hotelExiste_invocaReactivarHotel `(2ms)`

**[PASO]** cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException `(1ms)`

**[PASO]** editarHabitacion_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarImagenHotel_hotelExiste_retornaMapaConId `(0ms)`

**[PASO]** listarAmenidades_delegaAlRepositorio `(0ms)`

**[PASO]** eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException `(2ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(37ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(1ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(0ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(42ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(2ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(846ms)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(611ms)`

**[PASO]** pagoFallaCvvInvalido `(595ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(67ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(3ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(0ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(0ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(2ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(1ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(48ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(3ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(44ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(1ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(0ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(1ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(1ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(1ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(4ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(865ms)`

**[PASO]** crearReservacionFallaSinHabitaciones `(392ms)`

**[PASO]** crearReservacionFallaFechaPasada `(403ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(2ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(34ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(1ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(0ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(2ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(100ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(1ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(1ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(0ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(0ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(1ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(1ms)`

**[PASO]** validarDisponibilidad_todosLibres `(0ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(451ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(1ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(3.6s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(3ms)`

---
