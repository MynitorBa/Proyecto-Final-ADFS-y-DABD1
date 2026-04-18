# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 249
- Pasaron: 249
- Fallaron: 0
- Saltados: 0
- Duracion: 75.4s
- Ejecutado: 09/04/2026 18:39:08

---

## Suites

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(42ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(0ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(3ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(2ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(5ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(3ms)`

---

### helpers.EmailHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** enviar_conDatosValidos_completaSinExcepcion `(9.9s)`

**[PASO]** enviar_conCuerpoMinimo_completaSinExcepcion `(4.2s)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(2.4s)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(828ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(9ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(11ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(4ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(3ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(3ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(3ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(3ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(2ms)`

**[PASO]** getRolId_retornaRolCorrecto `(4ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(30ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(830ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(982ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(943ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(820ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(673ms)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(43ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(3ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(7ms)`

**[PASO]** validar_numeroConEspacios_esValido `(41ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(3ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(2ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(2ms)`

**[PASO]** validar_cvv4digitos_esValido `(4ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(2ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(1ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(4ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(14ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(3ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(2.2s)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(10ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(6ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(5ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(8ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(8ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(3.2s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(5ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(2.3s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(6ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(6ms)`

---

### services.AdminReservacionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(43ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(3ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacion `(12ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(34ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(4ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacion `(4ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(4ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(71ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(45ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(3ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(6ms)`

**[PASO]** listarTodas_retornaListaCompleta `(4ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(3ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(4ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(2ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(3ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(4ms)`

---

### services.AuthServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** loginExitosoConUsername `(8s)`

**[PASO]** loginExitosoConCorreo `(1s)`

**[PASO]** loginFallaUsuarioInexistente `(592ms)`

**[PASO]** loginFallaContrasenaIncorrecta `(1.1s)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(880ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(885ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(5ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(193ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(2ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(4ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(5ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(3ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(3ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(3ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(1ms)`

---

### services.BusquedaServiceIntegrationTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** busquedaConCiudadRealRetornaHoteles `(4.3s)`

**[PASO]** busquedaRegistraEventoEnOracle `(3.1s)`

**[PASO]** busquedaAnonimaRegistraEventoConUsuarioNull `(4.2s)`

**[PASO]** busquedaFallaCiudadInexistente `(778ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(55ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(3ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(4ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(4ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(4ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(4ms)`

---

### services.CancelacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** cancelacionExitosaPersistidaEnOracle `(1.1s)`

**[PASO]** cancelacionFallaUsuarioIncorrecto `(916ms)`

**[PASO]** cancelacionFallaMenosDe24Horas `(980ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(43ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(2ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(3ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(9ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(3ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(3ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(3ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(3ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(2ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(1ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(2ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(1ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(4ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(3ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(4ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(54ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(3ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(5ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(4ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(3ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(2ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(2ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(114ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(3ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(4ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(1ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(51ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(2ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(3ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(3ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(5ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(3ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(3ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(1ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(3ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(1ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(4ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(3ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(44ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(4.1s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(4ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(4ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(4ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(71ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(1ms)`

**[PASO]** iniciar_noLanzaExcepcion `(2ms)`

**[PASO]** detener_noLanzaExcepcion `(4ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(4ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(4ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(2ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(27ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(74ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(1ms)`

---

### services.HotelServiceTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(258ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(3ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(2ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHotel_hotelExiste_invocaEliminarHotel `(2ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(3ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(2ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(2ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(3ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(39ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(3ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(2ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(2ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(4ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(80ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(2ms)`

---

### services.PagoServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** pagoExitosoConfirmaReservacionYCreaFactura `(1.5s)`

**[PASO]** pagoFallaReservacionDeOtroUsuario `(994ms)`

**[PASO]** pagoFallaCvvInvalido `(922ms)`

---

### services.PagoServiceTest

- Tests: 10
- Pasaron: 10
- Fallaron: 0

**[PASO]** procesarPago_tokenAlianzaBlanco_procesaSinDescuento `(87ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado `(3ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_datosValidosSinToken_retornaPagoResponseDTO `(2ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(3ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(3ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(113ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(2ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(63ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(1ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(1ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(2ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(2ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(18ms)`

---

### services.ReservacionServiceIntegrationTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** crearReservacionExitosa `(1.3s)`

**[PASO]** crearReservacionFallaSinHabitaciones `(662ms)`

**[PASO]** crearReservacionFallaFechaPasada `(648ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(5ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(2ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(6ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(46ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(2ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(1ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(1ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(142ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(5ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(1ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(1ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(2ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(1ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(1ms)`

**[PASO]** validarDisponibilidad_todosLibres `(2ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(842ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(1ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(4.4s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(3ms)`

---
