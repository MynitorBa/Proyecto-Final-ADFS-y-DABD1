# Tests Go - Agencia de Viajes

> Reporte de pruebas unitarias generado automaticamente.

## Resumen

- Estado: **TODO VERDE**
- Total: 49
- Pasaron: 49
- Fallaron: 0
- Ejecutado: 23/04/2026 18:44:56

---

## Detalle

### TestTipoProveedorStr

**[PASS]** `TestTipoProveedorStr` (0.00s)

**[PASS]** `TestTipoProveedorStr/Aerolinea` (0.00s)

**[PASS]** `TestTipoProveedorStr/Hotelera` (0.00s)

**[PASS]** `TestTipoProveedorStr/Proveedor` (0.00s)

**[PASS]** `TestTipoProveedorStr/Proveedor#01` (0.00s)

**[PASS]** `TestTipoProveedorStr/Proveedor#02` (0.00s)

### TestErrorProveedorUsuario

**[PASS]** `TestErrorProveedorUsuario` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/error_de_red_dial_tcp` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/error_de_red_connectex` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/error_de_red_no_such_host` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/timeout_deadline_exceeded` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/timeout_explicito` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/error_generico_desconocido` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/error_HTTP_nil_incluye_rechazo` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/error_HTTP_nil_incluye_accion` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/mensaje_contiene_nombre_del_proveedor_en_error_red` (0.00s)

**[PASS]** `TestErrorProveedorUsuario/mensaje_contiene_tipo_en_timeout` (0.00s)

### TestLoginService_Login

**[PASS]** `TestLoginService_Login` (0.21s)

**[PASS]** `TestLoginService_Login/error_si_login_esta_vacio` (0.00s)

**[PASS]** `TestLoginService_Login/error_si_contrase├▒a_esta_vacia` (0.00s)

**[PASS]** `TestLoginService_Login/error_si_login_es_solo_espacios` (0.00s)

**[PASS]** `TestLoginService_Login/error_si_captcha_esta_vacio` (0.00s)

**[PASS]** `TestLoginService_Login/error_si_captcha_es_solo_espacios` (0.00s)

**[PASS]** `TestLoginService_Login/error_si_captcha_es_rechazado_por_google` (0.00s)

**[PASS]** `TestLoginService_Login/error_si_el_repositorio_falla_en_BD` (0.00s)

**[PASS]** `TestLoginService_Login/error_credenciales_invalidas_si_usuario_no_existe_en_BD` (0.00s)

**[PASS]** `TestLoginService_Login/error_usuario_deshabilitado_si_EstadoID_distinto_de_1` (0.00s)

**[PASS]** `TestLoginService_Login/error_credenciales_invalidas_si_contrasena_no_coincide` (0.05s)

**[PASS]** `TestLoginService_Login/login_exitoso_retorna_datos_del_usuario` (0.05s)

### TestProveedorService_CrearProveedor

**[PASS]** `TestProveedorService_CrearProveedor` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/crea_exitosamente_cuando_todo_es_valido` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_ObtenerRolUsuario_falla_en_BD` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_usuario_no_existe_rolID_cero` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_usuario_no_tiene_rol_webservice` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_UsuarioYaTieneProveedor_falla_en_BD` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_usuario_ya_tiene_proveedor_asignado` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_ExisteTipoProveedor_falla_en_BD` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_tipo_de_proveedor_no_existe` (0.00s)

**[PASS]** `TestProveedorService_CrearProveedor/error_si_CrearProveedor_falla_en_BD` (0.00s)

### TestUsuarioService_ValidarDatosUnicos

**[PASS]** `TestUsuarioService_ValidarDatosUnicos` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/todos_los_campos_disponibles_ÔÇö_sin_conflictos` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/correo_ya_registrado` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/pasaporte_ya_registrado` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/username_ya_registrado` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/correo_y_username_ya_registrados` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/los_tres_campos_ya_registrados` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/error_de_BD_al_verificar_correo` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/error_de_BD_al_verificar_pasaporte` (0.00s)

**[PASS]** `TestUsuarioService_ValidarDatosUnicos/error_de_BD_al_verificar_username` (0.00s)

---

_Generado por generar-tests-go.ps1_