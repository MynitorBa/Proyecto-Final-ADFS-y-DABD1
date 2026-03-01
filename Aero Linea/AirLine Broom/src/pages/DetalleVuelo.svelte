<script>
  // @ts-nocheck
  import '../styles/detallesv.css';
  import { onMount } from 'svelte';
  import { sesion } from '../stores/sesion.js';
  import ComentarioNodo from './ComentarioNodo.svelte';

  export let flight;
  export let onClose;

  const API = 'https://localhost:7107';

  let selectedClass = 'economico';
  let comentariosPlanos = [];
  let loadingComentarios = true;

  // Estado reactivo por comentario: id → { expandido, mostrandoForm, textoRespuesta, enviando, votoActual }
  let estadoNodos = {};

  const amenidades = {
    economico: [
      'Equipaje de mano incluido (8kg)',
      'Asiento estándar',
      'Comida y bebida incluida',
      'Entretenimiento a bordo',
      'USB en asiento'
    ],
    ejecutivo: [
      'Equipaje de mano incluido (12kg)',
      'Equipaje facturado incluido (32kg x2)',
      'Asiento cama totalmente reclinable',
      'Menú gourmet y bar completo',
      'Entretenimiento premium con pantalla grande',
      'Kit de amenidades de lujo',
      'Acceso a sala VIP',
      'Embarque prioritario',
      'USB, toma de corriente y WiFi'
    ]
  };

  onMount(async () => {
    if (flight.rutaId) {
      await cargarComentarios();
    } else {
      loadingComentarios = false;
    }
  });

  /* ─── Carga de comentarios ──────────────── */
  async function cargarComentarios() {
    loadingComentarios = true;
    try {
      const sesionActual = $sesion;
      // Si hay sesión, usamos el endpoint con-voto para obtener votoUsuario
      const endpoint = sesionActual
        ? `${API}/api/comentarios/ruta/${flight.rutaId}/con-voto`
        : `${API}/api/comentarios/ruta/${flight.rutaId}`;

      const res = await fetch(endpoint, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        comentariosPlanos = data;
        // Inicializar estado de cada nodo
        const nuevoEstado = {};
        data.forEach(c => {
          nuevoEstado[c.id] = {
            expandido: false,
            mostrandoForm: false,
            textoRespuesta: '',
            enviando: false,
            votoActual: c.votoUsuario ?? null  // null | 1 | -1
          };
        });
        estadoNodos = nuevoEstado;
      }
    } catch (e) {
      console.error('Error cargando comentarios:', e);
    } finally {
      loadingComentarios = false;
    }
  }

  /* ─── Árbol de comentarios ──────────────── */
  $: raices = comentariosPlanos.filter(c => !c.comentarioPadreId);
  $: hijosMap = (() => {
    const map = {};
    comentariosPlanos.forEach(c => {
      if (c.comentarioPadreId) {
        if (!map[c.comentarioPadreId]) map[c.comentarioPadreId] = [];
        map[c.comentarioPadreId].push(c);
      }
    });
    return map;
  })();

  function getHijos(id) {
    return hijosMap[id] ?? [];
  }

  /* ─── Votos ─────────────────────────────── */
  async function votar(comentarioId, valor) {
    const sesionActual = $sesion;
    if (!sesionActual) return;

    const estado = estadoNodos[comentarioId];
    if (!estado) return;
    const votoActual = estado.votoActual;

    try {
      if (votoActual === valor) {
        // Toggle: eliminar voto existente
        const res = await fetch(`${API}/api/votos/${comentarioId}`, {
          method: 'DELETE',
          credentials: 'include'
        });
        if (res.ok) {
          const data = await res.json();
          _actualizarVoto(comentarioId, null, data.nuevosDowns);
        }
      } else {
        // Crear o cambiar voto
        const res = await fetch(`${API}/api/votos`, {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ comentarioId, valor })
        });
        if (res.ok) {
          const data = await res.json();
          // Si ya votaste igual, la API devuelve { message: "..." }
          if (data.message) return;
          _actualizarVoto(comentarioId, valor, data.nuevosDowns);
        }
      }
    } catch (e) {
      console.error('Error al votar:', e);
    }
  }

  function _actualizarVoto(comentarioId, nuevoVoto, nuevosDowns) {
    estadoNodos[comentarioId] = { ...estadoNodos[comentarioId], votoActual: nuevoVoto };
    comentariosPlanos = comentariosPlanos.map(c =>
      c.id === comentarioId ? { ...c, downs: nuevosDowns } : c
    );
    estadoNodos = { ...estadoNodos };
  }

  /* ─── Formulario de respuesta ───────────── */
  function toggleForm(id) {
    estadoNodos[id] = {
      ...estadoNodos[id],
      mostrandoForm: !estadoNodos[id].mostrandoForm,
      textoRespuesta: ''
    };
    estadoNodos = { ...estadoNodos };
  }

  function toggleExpandido(id) {
    estadoNodos[id] = { ...estadoNodos[id], expandido: !estadoNodos[id].expandido };
    estadoNodos = { ...estadoNodos };
  }

  function onTextoChange(id, val) {
    estadoNodos[id] = { ...estadoNodos[id], textoRespuesta: val };
    estadoNodos = { ...estadoNodos };
  }

  async function enviarRespuesta(padreId) {
    const estado = estadoNodos[padreId];
    if (!estado || !estado.textoRespuesta.trim()) return;

    estadoNodos[padreId] = { ...estado, enviando: true };
    estadoNodos = { ...estadoNodos };

    try {
      const res = await fetch(`${API}/api/comentarios/respuesta`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          comentarioPadreId: padreId,
          contenido: estado.textoRespuesta.trim()
        })
      });

      if (res.ok) {
        const nuevo = await res.json();
        comentariosPlanos = [...comentariosPlanos, nuevo];
        estadoNodos[nuevo.id] = {
          expandido: false,
          mostrandoForm: false,
          textoRespuesta: '',
          enviando: false,
          votoActual: null
        };
        estadoNodos[padreId] = {
          ...estadoNodos[padreId],
          mostrandoForm: false,
          textoRespuesta: '',
          enviando: false,
          expandido: true
        };
        estadoNodos = { ...estadoNodos };
      }
    } catch (e) {
      console.error('Error al enviar respuesta:', e);
      estadoNodos[padreId] = { ...estadoNodos[padreId], enviando: false };
      estadoNodos = { ...estadoNodos };
    }
  }

  /* ─── Helpers de formato ────────────────── */
  function formatHora(h) {
    if (!h) return '--:--';
    return h.substring(0, 5);
  }

  function formatDuracion(min) {
    if (!min) return 'N/A';
    return `${Math.floor(min / 60)}h ${min % 60}m`;
  }

  function formatPrecio(precio) {
    if (!precio) return 'No disponible';
    return precio.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function formatFecha(fecha) {
    if (!fecha) return '';
    return new Date(fecha).toLocaleDateString('es-ES', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  }

  function getEstrellas(cantidad) {
    return Array.from({ length: 5 }, (_, i) => i < (cantidad ?? 0));
  }

  /* ─── Reactivos ─────────────────────────── */
  $: precioTurista      = flight.precioTurista ?? 0;
  $: precioEjecutiva    = flight.precioEjecutiva ?? 0;
  $: precioMostrado     = selectedClass === 'economico' ? precioTurista : precioEjecutiva;
  $: asientosTurista    = flight.boletosDisponiblesTurista ?? 0;
  $: asientosEjecutiva  = flight.boletosDisponiblesEjecutiva ?? 0;
  $: asientosMostrados  = selectedClass === 'economico' ? asientosTurista : asientosEjecutiva;
  // Total real = suma de ambas clases (boletosDisponibles del API puede venir en 0)
  $: totalAsientosDisponibles = asientosTurista + asientosEjecutiva;
  $: turistaDisponible  = precioTurista > 0 && asientosTurista > 0;
  $: ejecutivaDisponible = precioEjecutiva > 0 && asientosEjecutiva > 0;
  $: tripulantes        = flight.tripulantes || [];
  $: haySession         = !!$sesion;

  function handleBackdropClick(e) {
    if (e.target === e.currentTarget) onClose();
  }
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="dv-backdrop" on:click={handleBackdropClick}>
  <div class="dv-modal">

    <!-- ── HEADER ── -->
    <div class="dv-header">
      <div class="dv-header-left">
        <span class="dv-code">{flight.numeroVuelo}</span>
        <span class="dv-aircraft">{flight.avionMarca} {flight.avionModelo}</span>
      </div>
      <button class="dv-close" on:click={onClose}>✕ Cerrar</button>
    </div>

    <!-- ── HERO ── -->
    <div class="dv-hero">
      <div class="dv-hero-content">
        <div class="dv-hero-point">
          <div class="dv-hero-time">{formatHora(flight.horaSalida)}</div>
          <div class="dv-hero-code">{flight.origenCodigo ?? '---'}</div>
          <div class="dv-hero-label">Salida</div>
        </div>
        <div class="dv-hero-middle">
          <div class="dv-hero-line">
            <div class="dv-hero-dot"></div>
            <div class="dv-hero-track"></div>
            <span class="dv-hero-plane">✈</span>
            <div class="dv-hero-track"></div>
            <div class="dv-hero-dot"></div>
          </div>
          <div class="dv-hero-dur">{formatDuracion(flight.duracionMinutos)}</div>
          <div class="dv-hero-tipo">Directo</div>
        </div>
        <div class="dv-hero-point dv-hero-point--right">
          <div class="dv-hero-time">{formatHora(flight.horaLlegada)}</div>
          <div class="dv-hero-code">{flight.destinoCodigo ?? '---'}</div>
          <div class="dv-hero-label">Llegada</div>
        </div>
      </div>
    </div>

    <!-- ── BODY ── -->
    <div class="dv-body">
      <div class="dv-grid">

        <!-- MAIN -->
        <div class="dv-main">

          <!-- Info del vuelo -->
          <div class="dv-section-title">Información del vuelo</div>
          <div class="dv-specs">
            <div class="dv-spec">
              <div class="dv-spec-label">Número de vuelo</div>
              <div class="dv-spec-value">{flight.numeroVuelo ?? 'N/A'}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Fecha</div>
              <div class="dv-spec-value">{flight.fecha ? flight.fecha.substring(0,10) : 'N/A'}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Aeronave</div>
              <div class="dv-spec-value">{flight.avionMarca} {flight.avionModelo}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Duración</div>
              <div class="dv-spec-value">{formatDuracion(flight.duracionMinutos)}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Asientos disponibles</div>
              <div class="dv-spec-value dv-spec-value--gold">{totalAsientosDisponibles}</div>
            </div>
            <div class="dv-spec">
              <div class="dv-spec-label">Tipo de vuelo</div>
              <div class="dv-spec-value">Directo</div>
            </div>
          </div>

          <!-- Tripulación -->
          {#if tripulantes.length > 0}
            <div class="dv-section-title dv-section-title--mt">Tripulación</div>
            <div class="dv-tripulantes">
              {#each tripulantes as tripulante}
                <div class="dv-tripulante">
                  <div class="dv-tripulante-avatar">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </div>
                  <div class="dv-tripulante-info">
                    <div class="dv-tripulante-nombre">{tripulante.nombreCompleto}</div>
                    <div class="dv-tripulante-rol">{tripulante.nombreRol}</div>
                  </div>
                </div>
              {/each}
            </div>
          {/if}

          <!-- Clases disponibles -->
          <div class="dv-section-title dv-section-title--mt">Clases disponibles</div>
          <div class="dv-clases">

            <!-- Turista -->
            <button
              class="dv-clase"
              class:dv-clase--selected={selectedClass === 'economico'}
              class:dv-clase--disabled={!turistaDisponible}
              disabled={!turistaDisponible}
              on:click={() => turistaDisponible && (selectedClass = 'economico')}
            >
              {#if selectedClass === 'economico' && turistaDisponible}
                <div class="dv-clase-badge">✓ Seleccionada</div>
              {/if}
              <div class="dv-clase-name">Clase Turista</div>
              {#if turistaDisponible}
                <div class="dv-clase-avail">{asientosTurista} asientos disponibles</div>
                <div class="dv-clase-price">
                  <span class="dv-clase-desde">Desde</span>
                  <span class="dv-clase-amount">$ {formatPrecio(precioTurista)}</span>
                  <span class="dv-clase-pp">/ persona</span>
                </div>
              {:else}
                <div class="dv-clase-unavailable">No disponible</div>
              {/if}
              <ul class="dv-amenities">
                {#each amenidades.economico as a}<li>{a}</li>{/each}
              </ul>
            </button>

            <!-- Ejecutiva -->
            <button
              class="dv-clase dv-clase--premium"
              class:dv-clase--selected={selectedClass === 'ejecutivo'}
              class:dv-clase--disabled={!ejecutivaDisponible}
              disabled={!ejecutivaDisponible}
              on:click={() => ejecutivaDisponible && (selectedClass = 'ejecutivo')}
            >
              {#if selectedClass === 'ejecutivo' && ejecutivaDisponible}
                <div class="dv-clase-badge">✓ Seleccionada</div>
              {/if}
              <div class="dv-clase-ribbon">Premium</div>
              <div class="dv-clase-name">Clase Ejecutiva</div>
              {#if ejecutivaDisponible}
                <div class="dv-clase-avail">{asientosEjecutiva} asientos disponibles</div>
                <div class="dv-clase-price">
                  <span class="dv-clase-desde">Desde</span>
                  <span class="dv-clase-amount">$ {formatPrecio(precioEjecutiva)}</span>
                  <span class="dv-clase-pp">/ persona</span>
                </div>
              {:else}
                <div class="dv-clase-unavailable">No disponible</div>
              {/if}
              <ul class="dv-amenities">
                {#each amenidades.ejecutivo as a}<li>{a}</li>{/each}
              </ul>
            </button>

          </div>

          <!-- ═══ COMENTARIOS RECURSIVOS ═══ -->
          <div class="dv-section-title dv-section-title--mt">
            Reseñas · {flight.origenCodigo} → {flight.destinoCodigo}
          </div>

          {#if loadingComentarios}
            <div class="dv-loading">Cargando reseñas...</div>

          {:else if raices.length === 0}
            <div class="dv-no-comentarios">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
              <p>Aún no hay reseñas para esta ruta</p>
            </div>

          {:else}
            <div class="dv-comentarios-raiz">
              {#each raices as comentario (comentario.id)}
                <ComentarioNodo
                  {comentario}
                  {getHijos}
                  {estadoNodos}
                  {haySession}
                  {formatFecha}
                  {getEstrellas}
                  {votar}
                  {toggleForm}
                  {toggleExpandido}
                  {enviarRespuesta}
                  {onTextoChange}
                  profundidad={0}
                />
              {/each}
            </div>
          {/if}

        </div>

        <!-- SIDEBAR -->
        <aside class="dv-sidebar">
          <div class="dv-summary">
            <div class="dv-summary-title">Resumen del vuelo</div>
            <div class="dv-summary-row"><span>Vuelo</span><span>{flight.numeroVuelo ?? 'N/A'}</span></div>
            <div class="dv-summary-row"><span>Fecha</span><span>{flight.fecha ? flight.fecha.substring(0,10) : 'N/A'}</span></div>
            <div class="dv-summary-row"><span>Salida</span><span>{flight.origenCodigo ?? '---'} · {formatHora(flight.horaSalida)}</span></div>
            <div class="dv-summary-row"><span>Llegada</span><span>{flight.destinoCodigo ?? '---'} · {formatHora(flight.horaLlegada)}</span></div>
            <div class="dv-summary-row"><span>Duración</span><span>{formatDuracion(flight.duracionMinutos)}</span></div>
            <div class="dv-summary-row"><span>Aeronave</span><span>{flight.avionMarca} {flight.avionModelo}</span></div>
            <div class="dv-summary-divider"></div>
            <div class="dv-summary-row dv-summary-row--highlight">
              <span>Clase</span>
              <span>{selectedClass === 'economico' ? 'Turista' : 'Ejecutiva'}</span>
            </div>
            <div class="dv-summary-divider"></div>
            {#if precioMostrado > 0}
              <div class="dv-summary-row">
                <span>Precio / persona</span>
                <span class="dv-summary-price">$ {formatPrecio(precioMostrado)}</span>
              </div>
              <div class="dv-summary-row">
                <span>Asientos disponibles</span>
                <span class="dv-summary-seats">{asientosMostrados}</span>
              </div>
            {:else}
              <div class="dv-summary-row">
                <span class="dv-summary-unavailable">Clase no disponible</span>
              </div>
            {/if}
            <div class="dv-summary-note">Precio referencial · sujeto a cambios</div>
            <div class="dv-summary-disclaimer">
              Vista informativa. Para reservar, selecciona el vuelo en la lista principal.
            </div>
          </div>
        </aside>

      </div>
    </div>
  </div>
</div>