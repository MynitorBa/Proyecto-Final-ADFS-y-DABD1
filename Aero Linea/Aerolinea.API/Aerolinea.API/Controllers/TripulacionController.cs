using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de tripulacion. Expone endpoints REST para consultar, crear, actualizar
    /// y eliminar tripulantes, asi como gestionar sus imagenes y obtener el catalogo de roles.
    /// Los endpoints de lectura son publicos; los de escritura requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/tripulacion")]
    public class TripulacionController : ControllerBase
    {
        private readonly ITripulacionService _service;
        private readonly AdminVueloRepository _adminVueloRepo;
        private readonly EmailHelper _emailHelper;

        /// <summary>
        /// Inicializa el controlador con el servicio de tripulacion, el repositorio de vuelos admin
        /// (usado para verificar conflictos al desactivar un tripulante) y el helper de correo
        /// (usado para notificar a pasajeros afectados por cambios de personal).
        /// </summary>
        public TripulacionController(
            ITripulacionService service,
            AdminVueloRepository adminVueloRepo,
            EmailHelper emailHelper)
        {
            _service        = service;
            _adminVueloRepo = adminVueloRepo;
            _emailHelper    = emailHelper;
        }

        // Público: necesario para cargar listas en formularios del panel
        /// <summary>
        /// Retorna la lista de tripulantes registrados. Por defecto solo retorna tripulantes activos.
        /// Con incluirInactivos=true retorna todos (activos e inactivos). Endpoint publico, utilizado
        /// para poblar selectores en el formulario de creacion de vuelos del panel de admin.
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> ObtenerTodos([FromQuery] bool incluirInactivos = false)
        {
            var tripulantes = await _service.ObtenerTodos(incluirInactivos);
            return Ok(tripulantes);
        }

        /// <summary>
        /// Retorna los datos de un tripulante especifico por su identificador.
        /// Devuelve 404 si el tripulante no existe.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            var tripulante = await _service.ObtenerPorId(id);

            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(tripulante);
        }

        /// <summary>
        /// Retorna el catalogo de roles de tripulacion disponibles (piloto, copiloto,
        /// auxiliar de vuelo, etc.). Endpoint publico.
        /// </summary>
        [HttpGet("roles")]
        public async Task<IActionResult> ObtenerRoles()
        {
            var roles = await _service.ObtenerRoles();
            return Ok(roles);
        }

        // Solo administradores: operaciones de escritura
        /// <summary>
        /// Crea un nuevo tripulante con los datos del DTO. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> Crear([FromBody] CrearTripulanteDTO crearTripulanteDTO)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var tripulante = await _service.Crear(crearTripulanteDTO);
            return CreatedAtAction(nameof(ObtenerPorId), new { id = tripulante.Id }, tripulante);
        }

        /// <summary>
        /// Actualiza los datos de un tripulante existente. Requiere rol Administrador.
        /// Retorna 404 si el tripulante no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] CrearTripulanteDTO actualizarTripulanteDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var resultado = await _service.Actualizar(id, actualizarTripulanteDto);

            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante actualizado correctamente" });
        }

        /// <summary>
        /// Retorna los vuelos activos futuros asignados al tripulante, separados en dos grupos:
        /// vuelos en menos de 48 horas (bloquean la desactivacion) y vuelos lejanos
        /// (el tripulante sera desasignado al confirmar la desactivacion). Requiere Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("{id}/vuelos-asignados")]
        public async Task<IActionResult> ObtenerVuelosAsignados(int id)
        {
            var vuelos      = await _service.ObtenerVuelosAsignadosDetallados(id);
            var vuelos48h   = vuelos.Where(v => v.HorasRestantes <= 48).ToList();
            var vuelosLejos = vuelos.Where(v => v.HorasRestantes > 48).ToList();
            return Ok(new { vuelos48h, vuelosLejanos = vuelosLejos });
        }

        /// <summary>
        /// Retorna el equipo actual (tripulantes asignados) de un vuelo especifico.
        /// Usado en el modal de reemplazo para mostrar la composicion actual del vuelo
        /// y calcular que roles faltan al retirar al tripulante que se desactiva.
        /// Requiere Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("vuelo/{vueloId}/equipo")]
        public async Task<IActionResult> ObtenerEquipoVuelo(int vueloId)
        {
            var equipo = await _service.ObtenerEquipoVuelo(vueloId);
            return Ok(equipo);
        }

        /// <summary>
        /// Elimina un tripulante por su identificador. Requiere rol Administrador.
        /// Retorna 400 si el tripulante tiene vuelos asignados activos; 404 si no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}")]
        public async Task<IActionResult> Eliminar(int id)
        {
            var (totalFuturos, numeros48h) = await _service.VerificarVuelosAsignados(id);

            if (numeros48h.Count > 0)
                return BadRequest(new
                {
                    message = $"No se puede eliminar. El tripulante tiene {numeros48h.Count} vuelo(s) asignado(s) en menos de 48 horas.",
                    vuelos = numeros48h
                });

            if (totalFuturos > 0)
                return BadRequest(new
                {
                    message = $"No se puede eliminar. El tripulante tiene {totalFuturos} vuelo(s) activo(s) asignados.",
                    cantidadVuelos = totalFuturos
                });

            var resultado = await _service.Eliminar(id);

            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante eliminado correctamente" });
        }

        /// <summary>
        /// Cambia el estado activo/inactivo de un tripulante. Requiere rol Administrador.
        /// Al desactivar:
        ///   - Bloquea si hay vuelos asignados en menos de 48 horas.
        ///   - Si hay vuelos con mas de 48 horas: requiere que el cuerpo incluya Reemplazos
        ///     (uno por vuelo) para garantizar que cada vuelo mantenga 1 piloto + 1 copiloto
        ///     + 3 auxiliares. Agrega los reemplazos, desasigna al tripulante saliente y
        ///     notifica a los pasajeros con reservas activas o pendientes de pago.
        ///   - Si no hay vuelos futuros: desactiva directamente.
        /// Al reactivar: sin validaciones adicionales.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/estado")]
        public async Task<IActionResult> CambiarEstadoTripulante(int id, [FromBody] DesactivarTripulanteDTO dto)
        {
            if (!dto.Activo)
            {
                var vuelos      = await _service.ObtenerVuelosAsignadosDetallados(id);
                var vuelos48h   = vuelos.Where(v => v.HorasRestantes <= 48).ToList();
                var vuelosLejos = vuelos.Where(v => v.HorasRestantes > 48).ToList();

                // Bloquear si hay vuelos inminentes (< 48 h)
                if (vuelos48h.Count > 0)
                    return BadRequest(new
                    {
                        message  = $"No se puede desactivar. El tripulante tiene {vuelos48h.Count} vuelo(s) asignado(s) en menos de 48 horas.",
                        vuelos48h
                    });

                // Vuelos lejanos: requerir reemplazos antes de proceder
                if (vuelosLejos.Count > 0)
                {
                    if (dto.Reemplazos == null || dto.Reemplazos.Count == 0)
                        return BadRequest(new
                        {
                            message = "Debes asignar reemplazos para todos los vuelos afectados antes de desactivar al tripulante."
                        });

                    var vueloIdsAfectados = vuelosLejos.Select(v => v.Id).ToHashSet();
                    // Obtener roles para validar composicion 1 piloto + 1 copiloto + 3 auxiliares
                    var roles = await _service.ObtenerRoles();
                    int? rolPilotoId   = roles.FirstOrDefault(r =>
                        r.Cargo.Contains("Piloto", StringComparison.OrdinalIgnoreCase) &&
                        !r.Cargo.Contains("Co",    StringComparison.OrdinalIgnoreCase))?.Id;
                    int? rolCopiloId   = roles.FirstOrDefault(r =>
                        r.Cargo.Contains("Copiloto", StringComparison.OrdinalIgnoreCase) ||
                        r.Cargo.Contains("Co-Piloto", StringComparison.OrdinalIgnoreCase))?.Id;

                    // Validar composicion por vuelo ANTES de hacer cambios
                    foreach (var vuelo in vuelosLejos)
                    {
                        var equipoActual    = await _service.ObtenerEquipoVuelo(vuelo.Id);
                        var equipoSinSaliente = equipoActual.Where(t => t.Id != id).ToList();

                        var reemplazo      = dto.Reemplazos.FirstOrDefault(r => r.VueloId == vuelo.Id);
                        var nuevosIds      = reemplazo?.NuevosTripulantesIds ?? new List<int>();
                        var rolesNuevos    = nuevosIds.Count > 0
                                              ? (await _adminVueloRepo.ObtenerTripulantesPorIds(nuevosIds)).Select(t => t.RolID)
                                              : Enumerable.Empty<int>();
                        var equipoFinal    = equipoSinSaliente.Select(t => t.RolID)
                                              .Concat(rolesNuevos)
                                              .ToList();

                        int pilotos    = rolPilotoId.HasValue  ? equipoFinal.Count(r => r == rolPilotoId.Value)  : 0;
                        int copilotos  = rolCopiloId.HasValue  ? equipoFinal.Count(r => r == rolCopiloId.Value)  : 0;
                        int auxiliares = equipoFinal.Count(r =>
                            (!rolPilotoId.HasValue  || r != rolPilotoId.Value) &&
                            (!rolCopiloId.HasValue  || r != rolCopiloId.Value));

                        if (pilotos < 1 || copilotos < 1 || auxiliares < 3)
                            return BadRequest(new
                            {
                                message = $"El vuelo {vuelo.NumeroVuelo} no cumple la composicion minima (1 piloto, 1 copiloto, 3 auxiliares) con los reemplazos seleccionados."
                            });
                    }

                    // Composicion validada — aplicar cambios
                    foreach (var reemplazo in dto.Reemplazos)
                    {
                        if (!vueloIdsAfectados.Contains(reemplazo.VueloId))
                            continue;

                        // Agregar los nuevos tripulantes al vuelo
                        await _service.AsignarTripulantesAVuelo(reemplazo.VueloId, reemplazo.NuevosTripulantesIds);
                    }

                    // Desasignar al tripulante saliente de todos los vuelos lejanos
                    await _service.DesasignarDeFuturosVuelos(id, vuelosLejos.Select(v => v.Id));

                    // Notificar a los pasajeros afectados (solo reservas activas/pendientes de pago)
                    int notificados = 0;
                    foreach (var vuelo in vuelosLejos)
                    {
                        var afectados = await _adminVueloRepo.ObtenerAfectadosPorVuelo(vuelo.Id);
                        foreach (var afectado in afectados.Where(a => !string.IsNullOrEmpty(a.EmailUsuario)))
                        {
                            _ = _emailHelper.Enviar(
                                afectado.EmailUsuario,
                                "Broom Airline — Actualización de personal en tu vuelo",
                                EmailTemplates.CorreoCambioPersonal(
                                    afectado.NombreUsuario,
                                    afectado.NoReservacion,
                                    afectado.NumeroVuelo,
                                    afectado.OrigenCodigo,
                                    afectado.DestinoCodigo,
                                    afectado.FechaVuelo
                                )
                            );
                            notificados++;
                        }
                    }

                    var resultado2 = await _service.CambiarEstado(id, false);
                    if (!resultado2)
                        return NotFound(new { message = "Tripulante no encontrado" });

                    return Ok(new
                    {
                        message              = "Tripulante desactivado correctamente. Los reemplazos han sido asignados.",
                        vuelosDesasignados   = vuelosLejos.Count,
                        pasajerosNotificados = notificados
                    });
                }

                // Sin vuelos futuros: desactivar directamente
                var resultado3 = await _service.CambiarEstado(id, false);
                if (!resultado3)
                    return NotFound(new { message = "Tripulante no encontrado" });

                return Ok(new { message = "Tripulante desactivado correctamente" });
            }

            var resultado = await _service.CambiarEstado(id, dto.Activo);
            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante activado correctamente" });
        }

        // ===== GESTIÓN DE EQUIPO POR VUELO =====

        /// <summary>
        /// Reemplaza toda la tripulacion de un vuelo con la lista de tripulantes indicada.
        /// Primero limpia el equipo actual y luego asigna los nuevos. Requiere Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("vuelo/{vueloId}/equipo")]
        public async Task<IActionResult> SetEquipoVuelo(int vueloId, [FromBody] List<int> tripulantesIds)
        {
            // Validar que falte al menos 1 hora para el despegue
            var vuelo = await _adminVueloRepo.ObtenerVueloPorId(vueloId);
            if (vuelo == null)
                return NotFound(new { message = "Vuelo no encontrado" });

            var salidaDateTime = vuelo.Fecha.Date + vuelo.HoraSalida;
            var horasRestantes = (salidaDateTime - DateTime.Now).TotalHours;
            if (horasRestantes < 1)
                return BadRequest(new
                {
                    message = $"No se puede modificar la tripulación: el vuelo despega en {Math.Round(horasRestantes * 60, 0)} minutos. El límite es 1 hora antes del despegue."
                });

            await _service.LimpiarEquipoVuelo(vueloId);
            if (tripulantesIds != null && tripulantesIds.Count > 0)
                await _service.AsignarTripulantesAVuelo(vueloId, tripulantesIds);
            return Ok(new { message = "Tripulación del vuelo actualizada correctamente" });
        }

        /// <summary>
        /// Elimina toda la tripulacion asignada a un vuelo. Requiere Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpDelete("vuelo/{vueloId}/equipo")]
        public async Task<IActionResult> LimpiarEquipoVuelo(int vueloId)
        {
            await _service.LimpiarEquipoVuelo(vueloId);
            return Ok(new { message = "Tripulación del vuelo eliminada correctamente" });
        }

        // ===== ENDPOINTS DE IMAGEN =====

        /// <summary>
        /// Sube o reemplaza la imagen de un tripulante enviada como cadena Base64.
        /// Requiere rol Administrador. Retorna 404 si el tripulante no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPost("{id}/imagen")]
        public async Task<IActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
        {
            if (string.IsNullOrEmpty(dto.ImagenBase64))
                return BadRequest(new { message = "La imagen no puede estar vacía" });

            var tripulante = await _service.ObtenerPorId(id);
            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            await _service.GuardarImagen(id, dto.ImagenBase64);
            return Ok(new { message = "Imagen guardada correctamente" });
        }

        /// <summary>
        /// Elimina la imagen asociada a un tripulante. Requiere rol Administrador.
        /// Retorna 404 si el tripulante no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}/imagen")]
        public async Task<IActionResult> EliminarImagen(int id)
        {
            var tripulante = await _service.ObtenerPorId(id);
            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            await _service.EliminarImagen(id);
            return Ok(new { message = "Imagen eliminada correctamente" });
        }
    }
}
