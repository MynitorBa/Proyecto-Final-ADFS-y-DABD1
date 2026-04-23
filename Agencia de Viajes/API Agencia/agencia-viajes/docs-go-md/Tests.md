# Tests Go - Agencia de Viajes

> Reporte de pruebas unitarias generado automaticamente.

## Resumen

- Estado: **TODO VERDE**
- Total: 27
- Pasaron: 27
- Fallaron: 0
- Ejecutado: 22/04/2026 19:30:50

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

---

_Generado por generar-tests-go.ps1_