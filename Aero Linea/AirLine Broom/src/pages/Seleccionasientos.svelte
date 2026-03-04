<script>
  // @ts-nocheck
  import '../styles/asientos.css';
  import { onMount } from 'svelte';

  export let navigateTo;
  export let flightData = [];

  const API = 'https://localhost:7107';

  // ── Nomenclatura del backend ─────────────────────────────────────────────
  // Ejecutiva: E-A1..E-A6, E-B1..E-B6, E-C1..E-C6, E-D1..E-D6  (4 filas fijas)
  // Turista:   A1..A6, B1..B6, C1..C6, ...                       (empieza en A, crece según capacidad)
  // Son dos espacios de nombres totalmente independientes.
  //
  // Visualmente mostramos:
  //   Ejecutiva → filas A, B, C, D  (con label fijo, 4 filas)
  //   Turista   → filas A, B, C...  (dinámico según totalFilas - 4)

  const NUM_COLUMNAS      = 6;
  const FILAS_EJECUTIVA   = 4;
  const COLS_LABEL        = ['A','B','C','D','E','F']; // solo para cabecera visual

  // Letras de fila para ejecutiva: siempre A,B,C,D
  const FILAS_EJE_LETRAS = ['A','B','C','D'];

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

  let totalFilas       = 0;   // total de filas (ejecutiva + turista)
  let asientosOcupados = new Set();
  let boletosUsuario   = [];
  let asientos         = {};  // clave = id backend exacto
  let seleccionados    = [];
  let pasajeroActual   = 0;

  // ── Generador letras: A,B,...,Z,AA,AB,... ───────────────────────────────
  function* generarLetras(cantidad) {
    const abc = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    for (let i = 0; i < cantidad; i++) {
      let s = '', n = i;
      do { s = abc[n % 26] + s; n = Math.floor(n / 26) - 1; } while (n >= 0);
      yield s;
    }
  }

  // ID backend: ejecutiva → "E-A1", turista → "A1"
  function idEjecutiva(fila, col) { return `E-${fila}${col}`; }
  function idTurista(fila, col)   { return `${fila}${col}`;   }

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

  function construirMapa(totalF) {
    const mapa = {};
    const filasT = totalF - FILAS_EJECUTIVA; // filas de turista

    // ── Ejecutiva: siempre 4 filas, IDs E-A1..E-D6 ──────────────────────
    for (const fila of FILAS_EJE_LETRAS) {
      for (let col = 1; col <= NUM_COLUMNAS; col++) {
        const id = idEjecutiva(fila, col);
        mapa[id] = {
          id, fila, col,
          clase: 'Ejecutiva',
          estado: asientosOcupados.has(id) ? 'ocupado'
                : seleccionados.includes(id) ? 'propio'
                : 'libre'
        };
      }
    }

    // ── Turista: filas dinámicas A,B,C,... independientes de ejecutiva ───
    for (const fila of generarLetras(filasT)) {
      for (let col = 1; col <= NUM_COLUMNAS; col++) {
        const id = idTurista(fila, col);
        mapa[id] = {
          id, fila, col,
          clase: 'Turista',
          estado: asientosOcupados.has(id) ? 'ocupado'
                : seleccionados.includes(id) ? 'propio'
                : 'libre'
        };
      }
    }

    asientos = mapa;
  }

  // ── Filas para el template ───────────────────────────────────────────────
  $: filasT = totalFilas > FILAS_EJECUTIVA
      ? [...generarLetras(totalFilas - FILAS_EJECUTIVA)]
      : [];

  // ── Selección ────────────────────────────────────────────────────────────
  function puedeSeleccionar(a) {
    if (!a) return false;
    if (a.estado === 'ocupado') return false;
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return false;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return false;
    return true;
  }

  // disabled solo si ocupado o clase incorrecta (nunca para 'propio')
  function esBloqueado(a) {
    if (!a) return true;
    if (a.estado === 'ocupado') return true;
    if (a.estado === 'propio')  return false; // siempre clickeable
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

      // Actualizar mapa local
      if (asientoAnterior && asientos[asientoAnterior])
        asientos[asientoAnterior] = { ...asientos[asientoAnterior], estado: 'libre' };
      asientos[a.id] = { ...asientos[a.id], estado: 'propio' };

      seleccionados[pasajeroActual] = a.id;
      seleccionados = [...seleccionados];
      asientos      = { ...asientos };

      // Avanzar al siguiente sin asiento
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

            <!-- Cabecera columnas -->
            <div class="avion-cols-header">
              <div class="avion-col-label"></div>
              {#each COLS_LABEL as label, ci}
                {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                <div class="avion-col-label">{label}</div>
              {/each}
            </div>

            <!-- ── Ejecutiva: filas A,B,C,D con IDs E-A1..E-D6 ── -->
            <div class="zona-label zona-label--ejecutiva"><span>Ejecutiva</span></div>
            {#each FILAS_EJE_LETRAS as fila}
              <div class="avion-fila avion-fila--ejecutiva">
                <div class="avion-fila__num">{fila}</div>
                {#each {length: NUM_COLUMNAS} as _, ci}
                  {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                  {@const a = asientos[idEjecutiva(fila, ci + 1)]}
                  <button
                    class="asiento asiento--ejecutiva {claseAsiento(a)}"
                    disabled={guardando || esBloqueado(a)}
                    on:click={() => seleccionarAsiento(a)}
                    title="E-{fila}{ci + 1}"
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

            <!-- ── Turista: filas A,B,C,... con IDs A1..Z6,AA1... ── -->
            <div class="zona-label zona-label--turista"><span>Turista</span></div>
            {#each filasT as fila}
              <div class="avion-fila">
                <div class="avion-fila__num">{fila}</div>
                {#each {length: NUM_COLUMNAS} as _, ci}
                  {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                  {@const a = asientos[idTurista(fila, ci + 1)]}
                  <button
                    class="asiento {claseAsiento(a)}"
                    disabled={guardando || esBloqueado(a)}
                    on:click={() => seleccionarAsiento(a)}
                    title="{fila}{ci + 1}"
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

<style>
  .vuelos-progreso {
    display: flex; align-items: center; justify-content: center;
    padding: 0.75rem 2rem; background: #f5f3ef; border-bottom: 1px solid #e8e0d4;
  }
  .vuelos-progreso__item {
    display: flex; align-items: center; gap: 0.4rem;
    font-size: 0.78rem; color: #b0a89a; font-weight: 500; letter-spacing: 0.5px;
  }
  .vuelos-progreso__item--activo     { color: #c9a96e; }
  .vuelos-progreso__item--completado { color: #7a9e7e; }
  .vuelos-progreso__numero {
    width: 22px; height: 22px; border-radius: 50%; background: #e8e0d4;
    display: flex; align-items: center; justify-content: center;
    font-size: 0.7rem; font-weight: 700;
  }
  .vuelos-progreso__item--activo .vuelos-progreso__numero     { background: #c9a96e; color: #fff; }
  .vuelos-progreso__item--completado .vuelos-progreso__numero { background: #7a9e7e; color: #fff; }
  .vuelos-progreso__linea { width: 3rem; height: 2px; background: #e8e0d4; margin: 0 0.5rem; }
  .vuelos-progreso__linea--completada { background: #7a9e7e; }
</style>