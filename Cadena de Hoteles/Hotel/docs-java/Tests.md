# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 249
- Pasaron: 249
- Fallaron: 0
- Saltados: 0
- Duracion: 45s
- Ejecutado: 09/04/2026 00:13:50

---

## Suites

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(57ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(0ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(2ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(2ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(6ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(3ms)`

---

### helpers.EmailHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** enviar_conDatosValidos_completaSinExcepcion `(3.6s)`

**[PASO]** enviar_conCuerpoMinimo_completaSinExcepcion `(3.3s)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(350ms)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(125ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(3ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(1ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(3ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(2ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(3ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(2ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(1ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(3ms)`

**[PASO]** getRolId_retornaRolCorrecto `(3ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(2ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(274ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(520ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(529ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(527ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(347ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(36ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(3ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(9ms)`

**[PASO]** validar_numeroConEspacios_esValido `(7ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(0ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(0ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(0ms)`

**[PASO]** validar_cvv4digitos_esValido `(4ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(1ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(2ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(5ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(2ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(2ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(1.3s)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(5ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(6ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(5ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(5ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(4ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(3s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(6ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(3.4s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(3ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(3ms)`

---

### services.AdminReservacionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(30ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(3ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacion `(2ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(6ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(5ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacion `(2ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(3ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(43ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(3ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(1ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** listarTodas_retornaListaCompleta `(1ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(3ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(3ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(3ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(2ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(2s)`

**[PASO]** loginExitosoConCorreo `(649ms)`

**[PASO]** loginFallaUsuarioInexistente `(406ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(662ms)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(543ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(530ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(3ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(47ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(1ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(5ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(1ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(2ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(1ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(1ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(2.1s)`

**[PASO]** busquedaRegistraEventoEnOracle `(2s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(2.6s)`

**[PASO]** busquedaFallaCiudadInexistente `(498ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(45ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(3ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(2ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(2ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(4ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(2ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(786ms)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(599ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(3s)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(28ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(4ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(1ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(25ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(0ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(1ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(3ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(1ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(1ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(2ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(2ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(1ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(63ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(2ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(1ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(2ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(2ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(0ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(90ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(1ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(2ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(0ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(77ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(7ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(2ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(3ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(4ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(4ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(3ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(3ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(2ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(1ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(1ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(3ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(38ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(2.4s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(1ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(2ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(71ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(4ms)`

**[PASO]** iniciar_noLanzaExcepcion `(2ms)`

**[PASO]** detener_noLanzaExcepcion `(4ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(2ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(2ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(0ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(5ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(34ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(64ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(2ms)`

---

### services.HotelServiceTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(172ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(8ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(3ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelExiste_invocaEliminarHotel `(2ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(3ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(1ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(1ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(3ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(43ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(2ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(1ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(0ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(2ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(43ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(2ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(833ms)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(627ms)`

**[PASO]** pagoFallaCvvInvalido `(627ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(70ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(4ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(7ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(1ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(2ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(3ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(84ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(2ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(47ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(1ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(1ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(2ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(5ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(16ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(790ms)`

**[PASO]** crearReservacionFallaSinHabitaciones `(402ms)`

**[PASO]** crearReservacionFallaFechaPasada `(407ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(4ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(0ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(2ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(38ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(2ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(1ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(0ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(106ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(13ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(1ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(2ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(1ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(2ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(3ms)`

**[PASO]** validarDisponibilidad_todosLibres `(4ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(515ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(3ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(2.6s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(2ms)`

---
