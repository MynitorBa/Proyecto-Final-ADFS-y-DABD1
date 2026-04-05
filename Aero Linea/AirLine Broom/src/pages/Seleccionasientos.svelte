<script>
  // @ts-nocheck
  import '../styles/asientos.css';
  import { onMount } from 'svelte';

  export let navigateTo;
  export let flightData = [];

  import { API } from '../lib/api.js';

  // ── Nomenclatura del backend ─────────────────────────────────────────────
  // Las FILAS son NÚMEROS (1, 2, 3...) y las COLUMNAS son LETRAS (A-F).
  //
  // Ejecutiva: E-A1..E-F1, E-A2..E-F2, ... (fila numérica, col letra)
  //   → ID = "E-{colLetra}{numFila}"   ej: E-A1, E-B1, E-C2
  // Turista:   A1..F1, A2..F2, ...
  //   → ID = "{colLetra}{numFila}"     ej: A1, B1, C2
  //
  // Visualmente: número de fila en el margen izquierdo, letras A-F arriba.

  const NUM_COLUMNAS    = 6;
  const FILAS_EJECUTIVA = 4;
  const COLS_LABEL      = ['A','B','C','D','E','F'];

  // ── Grupos de vuelo ──────────────────────────────────────────────────────
  let grupoActualIdx = 0;
  $: grupoActual   = flightData?.[grupoActualIdx] ?? null;
  $: totalGrupos   = flightData?.length ?? 0;
  $: esUltimoGrupo = grupoActualIdx === totalGrupos - 1;

  $: vueloId          = grupoActual?.vueloId      ?? null;
  $: numeroVuelo      = grupoActual?.numeroVuelo   ?? '';
  $: avionModelo      = grupoActual?.avionModelo   ?? '';
  $: avionMarca       = grupoActual?.avionMarca    ?? '';
  $: claseActual      = grupoActual?.clase         ?? 'Turista';
  $: pasajerosTotales = grupoActual?.boletos?.length ?? 1;

  // ── Estado ───────────────────────────────────────────────────────────────
  let loading      = true;
  let error        = null;
  let guardando    = false;
  let errorGuardar = null;

  let totalFilas       = 0;
  let asientosOcupados = new Set();
  let boletosUsuario   = [];
  let asientos         = {};   // clave = id backend exacto
  let seleccionados    = [];
  let pasajeroActual   = 0;

  // ── IDs backend ──────────────────────────────────────────────────────────
  // fila  = número (1,2,3...)   col = letra ('A','B',...)
  function idEjecutiva(fila, col) { return `E-${col}${fila}`; }
  function idTurista(fila, col)   { return `${col}${fila}`;   }

  // ── Filas reactivas ──────────────────────────────────────────────────────
  // Ejecutiva: siempre 4 filas numéricas [1,2,3,4]
  $: filasEje = Array.from({ length: FILAS_EJECUTIVA }, (_, i) => i + 1);
  // Turista: filas dinámicas según totalFilas
  $: filasT = totalFilas > FILAS_EJECUTIVA
      ? Array.from({ length: totalFilas - FILAS_EJECUTIVA }, (_, i) => i + 1)
      : [];

  // ── onMount ──────────────────────────────────────────────────────────────
  onMount(async () => {
    if (!flightData || flightData.length === 0) {
      error = 'No se recibió información de vuelos.';
      loading = false;
      return;
    }
    await cargarAsientos();
  });

  let ultimoGrupoIdx = -1;
  $: if (grupoActualIdx !== ultimoGrupoIdx && vueloId) {
    ultimoGrupoIdx = grupoActualIdx;
    cargarAsientos();
  }

  async function cargarAsientos() {
    if (!vueloId) return;
    loading = true; error = null; errorGuardar = null;
    try {
      const res = await fetch(`${API}/api/asientos/${vueloId}`, { credentials: 'include' });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message ?? 'Error al cargar asientos.'); }
      const data = await res.json();

      totalFilas       = data.totalFilas;
      asientosOcupados = new Set(data.asientosOcupados ?? []);
      boletosUsuario   = data.boletosUsuario ?? [];
      seleccionados    = boletosUsuario.map(b => b.asiento);
      pasajeroActual   = 0;
      construirMapa(data.totalFilas);
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  // ── Construir mapa de asientos ───────────────────────────────────────────
  // Filas = números (1..N), Columnas = letras (A-F)
  // ID ejecutiva: E-{colLetra}{numFila}  →  E-A1, E-B1, E-A2...
  // ID turista:   {colLetra}{numFila}    →  A1, B1, A2...
  function construirMapa(totalF) {
    const mapa = {};
    const filasT = totalF - FILAS_EJECUTIVA;

    // ── Ejecutiva: filas 1..FILAS_EJECUTIVA, columnas A-F ────────────────
    for (let row = 1; row <= FILAS_EJECUTIVA; row++) {
      for (const col of COLS_LABEL) {
        const id = idEjecutiva(row, col);
        mapa[id] = {
          id, fila: row, col,
          clase: 'Ejecutiva',
          estado: asientosOcupados.has(id) ? 'ocupado'
                : seleccionados.includes(id) ? 'propio'
                : 'libre'
        };
      }
    }

    // ── Turista: filas 1..filasT, columnas A-F ────────────────────────────
    for (let row = 1; row <= filasT; row++) {
      for (const col of COLS_LABEL) {
        const id = idTurista(row, col);
        mapa[id] = {
          id, fila: row, col,
          clase: 'Turista',
          estado: asientosOcupados.has(id) ? 'ocupado'
                : seleccionados.includes(id) ? 'propio'
                : 'libre'
        };
      }
    }

    asientos = mapa;
  }

  // ── Selección ────────────────────────────────────────────────────────────
  function puedeSeleccionar(a) {
    if (!a) return false;
    if (a.estado === 'ocupado') return false;
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return false;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return false;
    return true;
  }

  function esBloqueado(a) {
    if (!a) return true;
    if (a.estado === 'ocupado') return true;
    if (a.estado === 'propio')  return false; // siempre clickeable para cambiar foco
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return true;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return true;
    return false;
  }

  function indicePasajero(id) { return seleccionados.indexOf(id); }

  async function seleccionarAsiento(a) {
    if (guardando || !a || a.estado === 'ocupado') return;

    // Click en asiento propio → solo cambiar foco al pasajero correspondiente
    if (a.estado === 'propio') {
      const idx = seleccionados.indexOf(a.id);
      if (idx !== -1) pasajeroActual = idx;
      return;
    }

    if (!puedeSeleccionar(a)) return;
    if (seleccionados[pasajeroActual] === a.id) return;

    const boletoId        = boletosUsuario[pasajeroActual]?.boletoId;
    const asientoAnterior = seleccionados[pasajeroActual];
    if (!boletoId) return;

    guardando = true; errorGuardar = null;
    try {
      const res = await fetch(`${API}/api/asientos/${boletoId}`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nuevoAsiento: a.id })
      });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message ?? 'Error al cambiar asiento.'); }

      // Actualizar mapa local sin recargar
      if (asientoAnterior && asientos[asientoAnterior])
        asientos[asientoAnterior] = { ...asientos[asientoAnterior], estado: 'libre' };
      asientos[a.id] = { ...asientos[a.id], estado: 'propio' };

      seleccionados[pasajeroActual] = a.id;
      seleccionados = [...seleccionados];
      asientos      = { ...asientos };

      // Avanzar al siguiente pasajero sin asiento
      const sig = seleccionados.findIndex((s, i) => i > pasajeroActual && !s);
      if (sig !== -1) pasajeroActual = sig;

    } catch (e) {
      errorGuardar = e.message;
    } finally {
      guardando = false;
    }
  }

  function claseAsiento(a) {
    if (!a) return 'asiento--vacio';
    if (a.estado === 'ocupado') return 'asiento--ocupado';
    if (a.estado === 'propio')  return 'asiento--seleccionado';
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return 'asiento--clase-incorrecta';
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return 'asiento--clase-incorrecta';
    return 'asiento--libre';
  }

  $: todoSeleccionado = seleccionados.length === pasajerosTotales && seleccionados.every(Boolean);
  $: progreso         = (seleccionados.filter(Boolean).length / pasajerosTotales) * 100;

  function handleContinuar() {
    if (!todoSeleccionado) return;
    if (!esUltimoGrupo) grupoActualIdx++;
    else navigateTo('carrito');
  }
</script>

<div class="asientos-page">
  <div class="asientos-page__container">

    <div class="asientos-page__header">
      <button class="asientos-page__back"
        on:click={() => grupoActualIdx > 0 ? grupoActualIdx-- : navigateTo('datos-pasajeros')}>
        Volver
      </button>
      <div class="asientos-titulo">
        <h1 class="asientos-titulo__main">Selección de Asientos</h1>
        <p class="asientos-titulo__sub">
          {avionMarca} {avionModelo}
          &nbsp;·&nbsp; Vuelo {numeroVuelo}
          &nbsp;·&nbsp; Clase {claseActual}
          &nbsp;·&nbsp; {pasajerosTotales} pasajero{pasajerosTotales > 1 ? 's' : ''}
          {#if totalGrupos > 1}&nbsp;·&nbsp;<strong>Vuelo {grupoActualIdx + 1} de {totalGrupos}</strong>{/if}
        </p>
      </div>
    </div>

    {#if totalGrupos > 1}
      <div class="vuelos-progreso">
        {#each flightData as grupo, i}
          <div class="vuelos-progreso__item"
            class:vuelos-progreso__item--activo={i === grupoActualIdx}
            class:vuelos-progreso__item--completado={i < grupoActualIdx}>
            <span class="vuelos-progreso__numero">{i + 1}</span>
            <span class="vuelos-progreso__label">Vuelo {grupo.numeroVuelo}</span>
          </div>
          {#if i < totalGrupos - 1}
            <div class="vuelos-progreso__linea"
              class:vuelos-progreso__linea--completada={i < grupoActualIdx}></div>
          {/if}
        {/each}
      </div>
    {/if}

    {#if loading}
      <div class="asientos-estado">Cargando mapa de asientos...</div>
    {:else if error}
      <div class="asientos-estado asientos-estado--error">{error}</div>
    {:else}
      <div class="asientos-page__body">
        <div class="asientos-page__mapa-wrap">

          <div class="avion-nariz">
            <svg viewBox="0 0 220 90" fill="none" xmlns="http://www.w3.org/2000/svg" class="avion-nariz__svg">
              <path d="M110 4 C70 4, 14 30, 10 58 L10 86 L210 86 L210 58 C206 30, 150 4, 110 4Z" fill="#1C1A18" stroke="#B89A7A" stroke-width="1.2"/>
              <path d="M110 14 C78 14, 28 36, 24 60 L24 78 L196 78 L196 60 C192 36, 142 14, 110 14Z" fill="#2c2a24" stroke="#B89A7A" stroke-width="0.6" opacity="0.6"/>
              <ellipse cx="68"  cy="50" rx="8" ry="5" fill="#c9a96e" opacity="0.3"/>
              <ellipse cx="110" cy="44" rx="8" ry="5" fill="#c9a96e" opacity="0.3"/>
              <ellipse cx="152" cy="50" rx="8" ry="5" fill="#c9a96e" opacity="0.3"/>
              <text x="110" y="73" text-anchor="middle" fill="#B89A7A" font-size="9" letter-spacing="3" font-family="inherit">CABINA</text>
            </svg>
          </div>

          <div class="avion-cuerpo">

            <!-- Cabecera columnas: letras A-F -->
            <div class="avion-cols-header">
              <div class="avion-col-label"></div>
              {#each COLS_LABEL as lbl, ci}
                {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                <div class="avion-col-label">{lbl}</div>
              {/each}
            </div>

            <!-- ── Ejecutiva: filas numéricas 1..FILAS_EJECUTIVA, cols A-F ── -->
            <!-- IDs: E-A1, E-B1, E-C1... / E-A2, E-B2, E-C2... -->
            <div class="zona-label zona-label--ejecutiva"><span>Ejecutiva</span></div>
            {#each filasEje as fila}
              <div class="avion-fila avion-fila--ejecutiva">
                <div class="avion-fila__num">{fila}</div>
                {#each COLS_LABEL as col, ci}
                  {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                  {@const a = asientos[idEjecutiva(fila, col)]}
                  <button
                    class="asiento asiento--ejecutiva {claseAsiento(a)}"
                    disabled={guardando || esBloqueado(a)}
                    on:click={() => seleccionarAsiento(a)}
                    title="E-{col}{fila}"
                  >
                    {#if a?.estado === 'propio'}
                      <span class="asiento__num">{indicePasajero(a.id) + 1}</span>
                    {/if}
                  </button>
                {/each}
              </div>
            {/each}

            <div class="zona-separador">
              <div class="zona-separador__line"></div>
              <span class="zona-separador__label">Separador de Cabina</span>
              <div class="zona-separador__line"></div>
            </div>

            <!-- ── Turista: filas numéricas 1..filasT, cols A-F ── -->
            <!-- IDs: A1, B1, C1... / A2, B2, C2... -->
            <div class="zona-label zona-label--turista"><span>Turista</span></div>
            {#each filasT as fila}
              <div class="avion-fila">
                <div class="avion-fila__num">{fila}</div>
                {#each COLS_LABEL as col, ci}
                  {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                  {@const a = asientos[idTurista(fila, col)]}
                  <button
                    class="asiento {claseAsiento(a)}"
                    disabled={guardando || esBloqueado(a)}
                    on:click={() => seleccionarAsiento(a)}
                    title="{col}{fila}"
                  >
                    {#if a?.estado === 'propio'}
                      <span class="asiento__num">{indicePasajero(a.id) + 1}</span>
                    {/if}
                  </button>
                {/each}
              </div>
            {/each}

          </div>

          <div class="avion-cola">
            <svg viewBox="0 0 220 50" fill="none" xmlns="http://www.w3.org/2000/svg" class="avion-cola__svg">
              <path d="M10 0 L210 0 L210 24 C180 44, 140 50, 110 50 C80 50, 40 44, 10 24 Z" fill="#1C1A18" stroke="#B89A7A" stroke-width="1.2"/>
            </svg>
          </div>

        </div>

        <!-- SIDEBAR -->
        <aside class="asientos-sidebar">

          <div class="seleccion-progreso">
            <div class="seleccion-progreso__header">
              <span class="seleccion-progreso__titulo">Pasajeros</span>
              <span class="seleccion-progreso__conteo">{seleccionados.filter(Boolean).length} / {pasajerosTotales}</span>
            </div>
            <div class="seleccion-progreso__barra">
              <div class="seleccion-progreso__fill" style="width: {progreso}%"></div>
            </div>
            {#if guardando}<p class="seleccion-progreso__guardando">Guardando asiento...</p>{/if}
            {#if errorGuardar}<p class="seleccion-progreso__error">{errorGuardar}</p>{/if}
          </div>

          <div class="pasajeros-lista">
            {#each boletosUsuario as _, i}
              <button
                class="pasajero-item"
                class:pasajero-item--activo={i === pasajeroActual && !todoSeleccionado}
                class:pasajero-item--completo={!!seleccionados[i]}
                on:click={() => { pasajeroActual = i; }}
              >
                <div class="pasajero-item__numero">{i + 1}</div>
                <div class="pasajero-item__info">
                  <span class="pasajero-item__label">Pasajero {i + 1}</span>
                  <span class="pasajero-item__asiento">
                    {seleccionados[i] ? `Asiento ${seleccionados[i]}` : 'Sin asignar'}
                  </span>
                </div>
              </button>
            {/each}
          </div>

          <div class="leyenda">
            <h3 class="leyenda__titulo">Leyenda</h3>
            <div class="leyenda__items">
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--libre asiento--muestra"></div>
                <span>Disponible</span>
              </div>
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--seleccionado asiento--muestra"></div>
                <span>Seleccionado</span>
              </div>
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--ocupado asiento--muestra"></div>
                <span>Ocupado</span>
              </div>
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--clase-incorrecta asiento--muestra"></div>
                <span>Otra clase</span>
              </div>
            </div>
          </div>

          <button
            class="asientos-continuar"
            class:asientos-continuar--listo={todoSeleccionado}
            disabled={!todoSeleccionado || guardando}
            on:click={handleContinuar}
          >
            {#if guardando}
              Guardando...
            {:else if !todoSeleccionado}
              {@const faltantes = pasajerosTotales - seleccionados.filter(Boolean).length}
              Selecciona {faltantes} asiento{faltantes !== 1 ? 's' : ''} más
            {:else if !esUltimoGrupo}
              Siguiente vuelo →
            {:else}
              Confirmar Asientos
            {/if}
          </button>

        </aside>
      </div>
    {/if}

  </div>
</div>