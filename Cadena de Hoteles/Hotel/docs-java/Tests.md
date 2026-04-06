# Tests

> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.

## Resumen

- Estado: TODO VERDE
- Total: 229
- Pasaron: 229
- Fallaron: 0
- Saltados: 0
- Duracion: 41.2s
- Ejecutado: 05/04/2026 19:54:01

---

## Suites

### helpers.CombinacionHelperTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** calcular_sinCombinacionesValidas_retornaVacio `(318ms)`

**[PASO]** calcular_stockVacio_retornaListaVacia `(1ms)`

**[PASO]** calcular_eliminaCombinacionExactaUnaHabitacion `(30ms)`

**[PASO]** calcular_conMultiplesCapacidades_retornaCombinaciones `(2ms)`

**[PASO]** calcular_retornaMaximo3Combinaciones `(105ms)`

**[PASO]** calcular_combinacionDosHabitaciones `(3ms)`

---

### helpers.EmailHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** enviar_conDatosValidos_completaSinExcepcion `(12.4s)`

**[PASO]** enviar_conCuerpoMinimo_completaSinExcepcion `(2.9s)`

---

### helpers.JwtHelperTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** esValido_tokenInvalido_retornaFalse `(1.6s)`

**[PASO]** getRolId_rolDistinto_retornaValorCorrecto `(672ms)`

**[PASO]** getUsuarioId_idBorde_retornaIdCorrecto `(1ms)`

**[PASO]** getUsername_usernameDistinto_retornaValorCorrecto `(1ms)`

**[PASO]** verificarToken_tokenInvalido_lanzaExcepcion `(25ms)`

**[PASO]** generarToken_retornaTokenNoNulo `(34ms)`

**[PASO]** esValido_tokenValido_retornaTrue `(2ms)`

**[PASO]** generarToken_tieneEstructuraJwt `(2ms)`

**[PASO]** getUsername_retornaUsernameCorrecto `(2ms)`

**[PASO]** verificarToken_tokenValido_retornaClaims `(2ms)`

**[PASO]** getRolId_retornaRolCorrecto `(2ms)`

**[PASO]** getUsuarioId_retornaIdCorrecto `(1ms)`

---

### helpers.PasswordHelperTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** hashear_retornaHashNoNulo `(331ms)`

**[PASO]** verificar_contrasenaIncorrecta_retornaFalse `(543ms)`

**[PASO]** hashear_resultadoDistintoCadaVez `(586ms)`

**[PASO]** verificar_contrasenaCorrecta_retornaTrue `(543ms)`

---

### helpers.PdfHelperTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** generarPdfReservacion_retornaPdfNoNulo `(1.5s)`

**[PASO]** generarPdfReservacion_conFactura_retornaPdf `(73ms)`

---

### helpers.TarjetaHelperTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** validar_titularVacio_lanzaExcepcion `(36ms)`

**[PASO]** validar_formatoFechaInvalido_lanzaExcepcion `(71ms)`

**[PASO]** validar_numeroConEspacios_esValido `(106ms)`

**[PASO]** validar_numeroInvalido_lanzaExcepcion `(3ms)`

**[PASO]** validar_datosValidos_noLanzaExcepcion `(3ms)`

**[PASO]** validar_cvvInvalido_lanzaExcepcion `(0ms)`

**[PASO]** validar_cvv4digitos_esValido `(1ms)`

**[PASO]** validar_tarjetaVencida_lanzaExcepcion `(1ms)`

**[PASO]** validar_titularNull_lanzaExcepcion `(2ms)`

---

### helpers.TokenHelperTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarTokenHash_retornaCadena64Caracteres `(18ms)`

**[PASO]** generarTokenHash_dosLlamadasRetornanDistinto `(1ms)`

**[PASO]** generarTokenHash_retornaSoloHexadecimal `(2ms)`

---

### services.AdminBusquedaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** listar_conFechasInvalidas_trataNullLasFechas `(6.2s)`

**[PASO]** listar_conTipoWeb_pasaTipoBusquedaId1 `(5ms)`

**[PASO]** listar_conTipoTodos_pasaTipoBusquedaIdNull `(4ms)`

**[PASO]** listar_pagina3ConPorPagina5_calculaOffsetCorrecto `(3ms)`

**[PASO]** listar_conTipoRest_pasaTipoBusquedaId2 `(4ms)`

**[PASO]** resumen_llamadoNormal_invocaRepoConArgumentosCorrectos `(5ms)`

**[PASO]** exportar_sinFiltros_invocaRepoExportarConNulls `(1.6s)`

**[PASO]** resumen_llamadoNormal_retornaMapaConTodasLasClaves `(4ms)`

**[PASO]** exportar_conFiltros_invocaRepoExportarYEnviaCorreo `(2.9s)`

**[PASO]** listar_conFechasValidas_parseaCorrectamenteLasFechas `(5ms)`

**[PASO]** listar_sinFiltros_retornaMapaConBusquedasYTotal `(4ms)`

---

### services.AdminReservacionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException `(107ms)`

**[PASO]** listarTodas_repositorioRetornaLista_devuelveMismaLista `(4ms)`

**[PASO]** cancelarReservacion_estadoConfirmada_ejecutaCancelacion `(3ms)`

**[PASO]** cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException `(11ms)`

**[PASO]** listarTodas_repositorioRetornaListaVacia_devuelveListaVacia `(3ms)`

**[PASO]** cancelarReservacion_estadoPendiente_ejecutaCancelacion `(3ms)`

**[PASO]** cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException `(5ms)`

---

### services.AgenciaServiceTest

- Tests: 11
- Pasaron: 11
- Fallaron: 0

**[PASO]** eliminar_conParametrosValidos_delegaAlRepo `(166ms)`

**[PASO]** listarTodas_sinAgencias_retornaListaVacia `(4ms)`

**[PASO]** listarPorUsuario_sinAgencias_retornaListaVacia `(2ms)`

**[PASO]** cambiarEstado_estadoInvalido_lanzaIllegalArgumentException `(4ms)`

**[PASO]** cambiarEstado_estadoCero_lanzaIllegalArgumentException `(4ms)`

**[PASO]** listarTodas_retornaListaCompleta `(2ms)`

**[PASO]** cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos `(1ms)`

**[PASO]** crear_conRequestValido_retornaAgenciaCreadaDelRepo `(3ms)`

**[PASO]** editar_conRequestValido_delegaAlRepo `(2ms)`

**[PASO]** cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos `(2ms)`

**[PASO]** listarPorUsuario_conUsuarioValido_retornaListaDelRepo `(3ms)`

---

### services.AuthServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** login_contrasenaIncorrecta_lanzaCredencialesInvalidasException `(624ms)`

**[PASO]** login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta `(537ms)`

**[PASO]** login_usuarioNoExiste_lanzaCredencialesInvalidasException `(5ms)`

---

### services.BusquedaAgenciaServiceTest

- Tests: 8
- Pasaron: 8
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(68ms)`

**[PASO]** buscar_sinAgenciaActiva_lanzaIllegalArgumentException `(3ms)`

**[PASO]** buscar_parametrosValidos_guardaBusquedaEnRepo `(4ms)`

**[PASO]** buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException `(4ms)`

**[PASO]** buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario `(3ms)`

**[PASO]** buscar_parametrosValidos_retornaListaDeHoteles `(3ms)`

**[PASO]** buscarPorToken_parametrosValidos_retornaListaDeHoteles `(3ms)`

**[PASO]** buscarPorToken_tokenInvalido_lanzaIllegalArgumentException `(3ms)`

---

### services.BusquedaServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** buscar_ciudadNoEncontrada_lanzaIllegalArgumentException `(46ms)`

**[PASO]** buscar_ciudadNoEncontrada_noInvocaBuscarHoteles `(6ms)`

**[PASO]** buscar_sinUsuario_guardaBusquedaConUsuarioIdNull `(4ms)`

**[PASO]** buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId `(2ms)`

**[PASO]** buscar_ciudadEncontradaConHoteles_retornaListaNoVacia `(12ms)`

**[PASO]** buscar_ciudadEncontrada_retornaListaDeHoteles `(4ms)`

---

### services.CancelacionServiceTest

- Tests: 17
- Pasaron: 17
- Fallaron: 0

**[PASO]** cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException `(33ms)`

**[PASO]** cancelarReservacionAgencia_valida_cancelaExitosamente `(6ms)`

**[PASO]** cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente `(4ms)`

**[PASO]** puedeCancelar_reservacionNula_retornaFalso `(3ms)`

**[PASO]** puedeCancelar_estadoPendiente_retornaVerdadero `(2ms)`

**[PASO]** cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente `(3ms)`

**[PASO]** cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException `(4ms)`

**[PASO]** cancelarReservacion_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException `(2ms)`

**[PASO]** cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** puedeCancelar_confirmadaMenosDe24Horas_retornaFalso `(2ms)`

**[PASO]** cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException `(3ms)`

**[PASO]** puedeCancelar_estadoNoPermite_retornaFalso `(2ms)`

**[PASO]** puedeCancelar_confirmadaSinHabitaciones_retornaFalso `(2ms)`

**[PASO]** cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException `(3ms)`

**[PASO]** puedeCancelar_confirmadaFuturoLejano_retornaVerdadero `(4ms)`

**[PASO]** cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException `(3ms)`

---

### services.ComentarioServiceTest

- Tests: 14
- Pasaron: 14
- Fallaron: 0

**[PASO]** obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista `(43ms)`

**[PASO]** agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException `(3ms)`

**[PASO]** agregarComentario_contenidoBlanco_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_respuestaValida_noActualizaRating `(2ms)`

**[PASO]** agregarComentario_respuestaConResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarComentario_contenidoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalValido_retornaComentario `(3ms)`

**[PASO]** agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException `(2ms)`

**[PASO]** obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia `(2ms)`

**[PASO]** obtenerComentariosPorHotel_hotelConComentarios_retornaLista `(3ms)`

**[PASO]** agregarComentario_resenaMenorACero_lanzaIllegalArgumentException `(3ms)`

**[PASO]** obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia `(3ms)`

---

### services.DestinosServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerDestinos_hotelSinImagenes_asignaListaVacia `(103ms)`

**[PASO]** obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas `(5ms)`

**[PASO]** obtenerDestinos_variosHoteles_asignaImagenesAcadaUno `(3ms)`

**[PASO]** obtenerDestinos_sinHoteles_retornaListaVacia `(2ms)`

---

### services.DownsServiceTest

- Tests: 18
- Pasaron: 18
- Fallaron: 0

**[PASO]** actualizarDown_valorDistinto_eliminaEInsertaNuevoDown `(42ms)`

**[PASO]** agregarDown_valorNegativo_insertaYActualizaContador `(2ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista `(2ms)`

**[PASO]** eliminarDown_downNegativoExistente_actualizaContadorConPositivo `(3ms)`

**[PASO]** actualizarDown_noExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** actualizarDown_valorInvalido_lanzaIllegalArgumentException `(1ms)`

**[PASO]** agregarDown_valorDos_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_valido_insertaYActualizaContador `(3ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioConDowns_retornaLista `(4ms)`

**[PASO]** eliminarDown_noExisteDown_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarDown_downExistente_actualizaContadorYElimina `(2ms)`

**[PASO]** agregarDown_comentarioNoExiste_lanzaIllegalArgumentException `(4ms)`

**[PASO]** obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia `(3ms)`

**[PASO]** actualizarDown_mismoValor_lanzaIllegalArgumentException `(3ms)`

**[PASO]** obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia `(3ms)`

**[PASO]** agregarDown_valorInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** agregarDown_yaExisteDown_lanzaIllegalArgumentException `(3ms)`

**[PASO]** actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente `(3ms)`

---

### services.EmailReservacionServiceTest

- Tests: 5
- Pasaron: 5
- Fallaron: 0

**[PASO]** enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo `(39ms)`

**[PASO]** enviarCorreoReservacion_datosValidos_completaSinExcepcion `(1.9s)`

**[PASO]** enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException `(3ms)`

**[PASO]** enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException `(2ms)`

**[PASO]** enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException `(2ms)`

---

### services.ExpiracionServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** iniciar_luegoDdetener_cicloCompletoCorrecto `(33ms)`

**[PASO]** iniciar_llamadaMultiple_noLanzaExcepcion `(3ms)`

**[PASO]** iniciar_noLanzaExcepcion `(2ms)`

**[PASO]** detener_noLanzaExcepcion `(2ms)`

**[PASO]** constructor_repositoryValido_noLanzaExcepcion `(4ms)`

**[PASO]** expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio `(4ms)`

**[PASO]** detener_sinIniciarPreviamente_noLanzaExcepcion `(2ms)`

---

### services.HandshakeServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException `(6ms)`

**[PASO]** procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException `(6ms)`

**[PASO]** procesarHandshake_urlValida_retornaResponseDTONoNulo `(14ms)`

---

### services.HotelAgenciaServiceTest

- Tests: 2
- Pasaron: 2
- Fallaron: 0

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia `(25ms)`

**[PASO]** obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento `(2ms)`

---

### services.HotelServiceTest

- Tests: 21
- Pasaron: 21
- Fallaron: 0

**[PASO]** crearHotel_datosValidos_retornaMapaConIdMensaje `(125ms)`

**[PASO]** editarHotel_hotelExiste_invocaActualizarHotel `(2ms)`

**[PASO]** crearHabitacion_datosValidos_retornaMapaConIdMensaje `(6ms)`

**[PASO]** editarHotel_hotelNoExiste_lanzaIllegalArgumentException `(6ms)`

**[PASO]** crearHotel_estadoInvalido_lanzaIllegalArgumentException `(2ms)`

**[PASO]** crearAmenidad_nombreBlanco_lanzaIllegalArgumentException `(5ms)`

**[PASO]** crearAmenidad_nombreNulo_lanzaIllegalArgumentException `(4ms)`

**[PASO]** crearHotel_paisNulo_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_ciudadNula_lanzaIllegalArgumentException `(2ms)`

**[PASO]** eliminarHotel_hotelExiste_invocaEliminarHotel `(5ms)`

**[PASO]** listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades `(1ms)`

**[PASO]** listarTodos_repositorioRetornaListaVacia_retornaListaVacia `(6ms)`

**[PASO]** crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje `(2ms)`

**[PASO]** listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHabitacion_estadoInvalido_lanzaIllegalArgumentException `(5ms)`

**[PASO]** listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes `(3ms)`

**[PASO]** crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException `(3ms)`

**[PASO]** crearHotel_nombreNulo_lanzaIllegalArgumentException `(12ms)`

**[PASO]** eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException `(4ms)`

---

### services.ImagenServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos `(39ms)`

**[PASO]** obtenerImagenAmenidad_imagenNoExiste_retornaNull `(3ms)`

**[PASO]** obtenerImagenHotel_imagenExiste_retornaBytesCorrectos `(2ms)`

**[PASO]** obtenerImagenHabitacion_imagenNoExiste_retornaNull `(1ms)`

**[PASO]** obtenerImagenHotel_imagenNoExiste_retornaNull `(2ms)`

**[PASO]** obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos `(1ms)`

---

### services.PagoAgenciaServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException `(45ms)`

**[PASO]** procesarPago_nitNulo_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_codigoPostalNulo_lanzaIllegalArgumentException `(6ms)`

**[PASO]** procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_nitBlanco_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(1ms)`

---

### services.PagoServiceTest

- Tests: 7
- Pasaron: 7
- Fallaron: 0

**[PASO]** procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException `(38ms)`

**[PASO]** procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException `(6ms)`

**[PASO]** procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException `(3ms)`

**[PASO]** procesarPago_nombreTitularVacio_lanzaIllegalArgumentException `(2ms)`

**[PASO]** procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO `(4ms)`

**[PASO]** procesarPago_cvvInvalido_lanzaIllegalArgumentException `(4ms)`

**[PASO]** procesarPago_tarjetaVencida_lanzaIllegalArgumentException `(3ms)`

---

### services.PdfReservacionServiceTest

- Tests: 3
- Pasaron: 3
- Fallaron: 0

**[PASO]** generarPdf_detallesVacios_lanzaExcepcion `(4ms)`

**[PASO]** generarPdf_guardasSuperadas_invocaRepositorio `(78ms)`

**[PASO]** generarPdf_noPertenece_lanzaExcepcion `(3ms)`

---

### services.ReservacionAgenciaServiceTest

- Tests: 9
- Pasaron: 9
- Fallaron: 0

**[PASO]** obtenerReservaciones_agenciaExistente_retornaLista `(51ms)`

**[PASO]** obtenerDetalleReservacion_sinDetalles_lanzaExcepcion `(4ms)`

**[PASO]** expirarReservacion_reservacionInvalida_lanzaExcepcion `(8ms)`

**[PASO]** crearReservacion_sinHabitaciones_lanzaExcepcion `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(3ms)`

**[PASO]** expirarReservacion_reservacionValida_invocaRepositorio `(2ms)`

**[PASO]** crearReservacion_agenciaNoActiva_lanzaExcepcion `(3ms)`

**[PASO]** obtenerDetalleReservacion_conDetalles_retornaListaConImagenes `(2ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(19ms)`

---

### services.ReservacionServiceTest

- Tests: 6
- Pasaron: 6
- Fallaron: 0

**[PASO]** crearReservacion_traslape_lanzaExcepcion `(4ms)`

**[PASO]** obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes `(2ms)`

**[PASO]** obtenerReservaciones_sinReservaciones_retornaListaVacia `(1ms)`

**[PASO]** crearReservacion_habitacionesNull_lanzaExcepcion `(4ms)`

**[PASO]** crearReservacion_habitacionesVacias_lanzaExcepcion `(3ms)`

**[PASO]** crearReservacion_datosValidos_retornaResponse `(4ms)`

---

### services.SesionServiceTest

- Tests: 4
- Pasaron: 4
- Fallaron: 0

**[PASO]** obtenerSesion_datosValidos_retornaDtoCompleto `(31ms)`

**[PASO]** sinSesion_siempre_retornaDtoNoAutenticado `(4ms)`

**[PASO]** sinSesion_siempre_retornaDtoSinDatosDeUsuario `(2ms)`

**[PASO]** obtenerSesion_rolConsultado_invocaRepositorio `(3ms)`

---

### services.UsuarioServiceTest

- Tests: 12
- Pasaron: 12
- Fallaron: 0

**[PASO]** obtenerPerfil_noExiste_lanzaExcepcion `(109ms)`

**[PASO]** validarDisponibilidad_usernameOcupado `(4ms)`

**[PASO]** cambiarRol_valido_actualizaRepositorio `(2ms)`

**[PASO]** listarTodosUsuarios_retornaLista `(2ms)`

**[PASO]** cambiarTelefono_vacio_lanzaExcepcion `(4ms)`

**[PASO]** obtenerPerfil_existente_retornaPerfil `(2ms)`

**[PASO]** registrarUsuario_usernameDuplicado_lanzaExcepcion `(4ms)`

**[PASO]** validarDisponibilidad_todosLibres `(3ms)`

**[PASO]** cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion `(665ms)`

**[PASO]** cambiarRol_invalido_lanzaExcepcion `(5ms)`

**[PASO]** registrarUsuario_exitoso_retornaId `(2.6s)`

**[PASO]** cambiarTelefono_valido_actualizaRepositorio `(1ms)`

---
