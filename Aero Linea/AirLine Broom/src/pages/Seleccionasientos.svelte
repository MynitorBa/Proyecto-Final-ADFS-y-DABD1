<script>
/**
 * @file Seleccionasientos.svelte
 * @description Pagina interactiva de mapa de asientos para seleccionar asientos en uno o varios
 * grupos de vuelo. El avion se renderiza visualmente con un SVG de nariz, un cuerpo de cabina
 * dividido en una seccion Ejecutiva (4 filas fijas, columnas A-F, IDs: E-{col}{fila}) y una
 * seccion Turista (filas dinamicas, IDs: {col}{fila}), y un SVG de cola. Al montar, carga el
 * mapa de asientos desde GET /api/asientos/:vueloId y construye un mapa local indexado por
 * IDs de asientos del backend. Cuando el usuario hace clic en un asiento libre, llama a
 * PUT /api/asientos/:boletoId para persistir la asignacion y actualiza el estado local sin
 * recargar. Un panel lateral muestra el progreso de asignacion de pasajeros, una leyenda de
 * asientos y un boton de continuar que avanza al siguiente grupo de vuelo o navega a 'carrito'
 * cuando todos los grupos estan completos.
 */
// @ts-nocheck
  import '../styles/asientos.css';
  import { onMount } from 'svelte';

  /** Funcion para navegar entre las paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** Arreglo de objetos de grupo de vuelo pasado desde el paso anterior, cada uno con vueloId, numeroVuelo, avionModelo, avionMarca, clase y arreglo de boletos. @type {Array<object>} */
  export let flightData = [];

  import { API } from '../lib/api.js';

  /** Cantidad de columnas de asientos en las zonas Ejecutiva y Turista. @type {number} */
  const NUM_COLUMNAS    = 6;

  /** Cantidad fija de filas en la zona Ejecutiva. @type {number} */
  const FILAS_EJECUTIVA = 4;

  /** Etiquetas de letras de columna usadas como identificadores de columnas de asiento. @type {string[]} */
  const COLS_LABEL      = ['A','B','C','D','E','F'];

  /** Indice del grupo de vuelo activo actualmente siendo mapeado. @type {number} */
  let grupoActualIdx = 0;

  // Objeto del grupo de vuelo actual resuelto desde flightData en grupoActualIdx.
  $: grupoActual   = flightData?.[grupoActualIdx] ?? null;

  // Cantidad total de grupos de vuelo en flightData.
  $: totalGrupos   = flightData?.length ?? 0;

  // Verdadero cuando grupoActualIdx apunta al ultimo grupo de vuelo.
  $: esUltimoGrupo = grupoActualIdx === totalGrupos - 1;

  // ID de vuelo del grupo actual, o null si no esta disponible.
  $: vueloId          = grupoActual?.vueloId      ?? null;

  // Cadena de numero de vuelo para el encabezado de display del grupo actual.
  $: numeroVuelo      = grupoActual?.numeroVuelo   ?? '';

  // Nombre del modelo de avion para el encabezado de display del grupo actual.
  $: avionModelo      = grupoActual?.avionModelo   ?? '';

  // Nombre de la marca del avion para el encabezado de display del grupo actual.
  $: avionMarca       = grupoActual?.avionMarca    ?? '';

  // Clase de asiento del grupo actual ('Turista' o 'Ejecutiva'), restringe los asientos seleccionables.
  $: claseActual      = grupoActual?.clase         ?? 'Turista';

  // Cantidad total de pasajeros (boletos) en el grupo actual.
  $: pasajerosTotales = grupoActual?.boletos?.length ?? 1;

  /** Verdadero mientras la solicitud de la API del mapa de asientos esta en progreso. @type {boolean} */
  let loading      = true;

  /** Mensaje de error mostrado si el mapa de asientos no puede cargarse. @type {string|null} */
  let error        = null;

  /** Verdadero mientras una solicitud PUT de asignacion de asiento esta en progreso. @type {boolean} */
  let guardando    = false;

  /** Mensaje de error mostrado si una solicitud PUT de asignacion de asiento falla. @type {string|null} */
  let errorGuardar = null;

  /** Cantidad total de filas (Ejecutiva + Turista) para el avion del vuelo actual. @type {number} */
  let totalFilas       = 0;

  /** Conjunto de cadenas de ID de asiento ya ocupados por otros pasajeros. @type {Set<string>} */
  let asientosOcupados = new Set();

  /** Arreglo de objetos de boleto con boletoId y asiento para la reservacion del usuario actual. @type {Array<{boletoId: number, asiento: string}>} */
  let boletosUsuario   = [];

  /** Mapa de cadena de ID de asiento a objeto de asiento con id, fila, col, clase y estado. @type {Object.<string, object>} */
  let asientos         = {};

  /** Arreglo de cadenas de ID de asiento, una por pasajero, representando las asignaciones de asiento actuales. @type {string[]} */
  let seleccionados    = [];

  /** Indice del pasajero cuyo asiento se esta asignando actualmente. @type {number} */
  let pasajeroActual   = 0;

  /**
   * Construye la cadena de ID de asiento del backend para un asiento Ejecutiva dado el numero de fila y la letra de columna.
   * @param {number} fila - Numero de fila (base 1).
   * @param {string} col - Letra de columna (A-F).
   * @returns {string} ID de asiento en el formato 'E-{col}{fila}', por ejemplo 'E-A1'.
   */
  function idEjecutiva(fila, col) { return `E-${col}${fila}`; }

  /**
   * Construye la cadena de ID de asiento del backend para un asiento Turista dado el numero de fila y la letra de columna.
   * @param {number} fila - Numero de fila (base 1).
   * @param {string} col - Letra de columna (A-F).
   * @returns {string} ID de asiento en el formato '{col}{fila}', por ejemplo 'A1'.
   */
  function idTurista(fila, col)   { return `${col}${fila}`;   }

  // Arreglo de numeros de fila para la zona Ejecutiva, siempre [1, 2, 3, 4].
  $: filasEje = Array.from({ length: FILAS_EJECUTIVA }, (_, i) => i + 1);

  // Arreglo de numeros de fila para la zona Turista, dimensionado dinamicamente desde totalFilas menos filas Ejecutiva.
  $: filasT = totalFilas > FILAS_EJECUTIVA
      ? Array.from({ length: totalFilas - FILAS_EJECUTIVA }, (_, i) => i + 1)
      : [];

  onMount(async () => {
    if (!flightData || flightData.length === 0) {
      error = 'No se recibio informacion de vuelos.';
      loading = false;
      return;
    }
    await cargarAsientos();
  });

  /** Rastrea el ultimo grupoActualIdx para el que se cargaron asientos para evitar recargas redundantes. @type {number} */
  let ultimoGrupoIdx = -1;

  // Recarga el mapa de asientos cada vez que el indice del grupo activo cambia y hay un vueloId valido.
  $: if (grupoActualIdx !== ultimoGrupoIdx && vueloId) {
    ultimoGrupoIdx = grupoActualIdx;
    cargarAsientos();
  }

  /**
   * Obtiene la disponibilidad de asientos para el vueloId actual desde GET /api/asientos/:vueloId.
   * Llena totalFilas, asientosOcupados, boletosUsuario y seleccionados desde la respuesta, restablece
   * pasajeroActual a 0 y reconstruye el mapa de asientos mediante construirMapa.
   * @async
   * @returns {Promise<void>}
   */
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

  /**
   * Construye el mapa de asientos indexado por ID de asiento del backend para las zonas Ejecutiva y Turista.
   * Cada entrada tiene id, fila, col, clase y estado ('ocupado', 'propio' o 'libre').
   * @param {number} totalF - Cantidad total de filas del avion (filas Ejecutiva + Turista combinadas).
   */
  function construirMapa(totalF) {
    const mapa = {};
    const filasT = totalF - FILAS_EJECUTIVA;

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

  /**
   * Retorna verdadero si el objeto de asiento dado puede ser seleccionado por el pasajero actual. Un asiento
   * es seleccionable cuando existe, no esta ocupado y pertenece a la misma clase que claseActual.
   * @param {object} a - Objeto de asiento del mapa de asientos.
   * @returns {boolean} Si el asiento puede ser seleccionado.
   */
  function puedeSeleccionar(a) {
    if (!a) return false;
    if (a.estado === 'ocupado') return false;
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return false;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return false;
    return true;
  }

  /**
   * Retorna verdadero si el boton de asiento debe estar deshabilitado. Un asiento esta bloqueado si es null, esta
   * ocupado o pertenece a una clase diferente a claseActual. Los asientos con estado 'propio' nunca se bloquean
   * para que el usuario pueda hacer clic en ellos para cambiar el foco de pasajero.
   * @param {object} a - Objeto de asiento del mapa de asientos.
   * @returns {boolean} Si el boton de asiento debe renderizarse como deshabilitado.
   */
  function esBloqueado(a) {
    if (!a) return true;
    if (a.estado === 'ocupado') return true;
    if (a.estado === 'propio')  return false;
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return true;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return true;
    return false;
  }

  /**
   * Retorna el indice del ID de asiento dado en el arreglo seleccionados, o -1 si no se encuentra.
   * Usado para renderizar el badge de numero de pasajero en asientos ya asignados.
   * @param {string} id - Cadena de ID de asiento a buscar.
   * @returns {number} Indice en seleccionados, o -1.
   */
  function indicePasajero(id) { return seleccionados.indexOf(id); }

  /**
   * Maneja el clic en un boton de asiento. Si el asiento ya pertenece al usuario, desplaza pasajeroActual
   * al indice de ese pasajero. De lo contrario, valida la seleccionabilidad, envia PUT /api/asientos/:boletoId
   * para actualizar la asignacion, actualiza el mapa local de asientos y el arreglo seleccionados sin
   * recargar, y avanza pasajeroActual al siguiente pasajero sin asiento asignado.
   * @async
   * @param {object} a - Objeto de asiento del mapa de asientos.
   * @returns {Promise<void>}
   */
  async function seleccionarAsiento(a) {
    if (guardando || !a || a.estado === 'ocupado') return;

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

      if (asientoAnterior && asientos[asientoAnterior])
        asientos[asientoAnterior] = { ...asientos[asientoAnterior], estado: 'libre' };
      asientos[a.id] = { ...asientos[a.id], estado: 'propio' };

      seleccionados[pasajeroActual] = a.id;
      seleccionados = [...seleccionados];
      asientos      = { ...asientos };

      const sig = seleccionados.findIndex((s, i) => i > pasajeroActual && !s);
      if (sig !== -1) pasajeroActual = sig;

    } catch (e) {
      errorGuardar = e.message;
    } finally {
      guardando = false;
    }
  }

  /**
   * Retorna la cadena de clase CSS para un boton de asiento segun su estado y compatibilidad de clase.
   * Valores de retorno posibles: 'asiento--vacio', 'asiento--ocupado', 'asiento--seleccionado',
   * 'asiento--clase-incorrecta' o 'asiento--libre'.
   * @param {object} a - Objeto de asiento del mapa de asientos, o null/undefined.
   * @returns {string} Cadena de clase CSS modificadora.
   */
  function claseAsiento(a) {
    if (!a) return 'asiento--vacio';
    if (a.estado === 'ocupado') return 'asiento--ocupado';
    if (a.estado === 'propio')  return 'asiento--seleccionado';
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return 'asiento--clase-incorrecta';
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return 'asiento--clase-incorrecta';
    return 'asiento--libre';
  }

  // Verdadero cuando todos los pasajeros del grupo actual tienen un asiento asignado.
  $: todoSeleccionado = seleccionados.length === pasajerosTotales && seleccionados.every(Boolean);

  // Porcentaje de asientos asignados sobre el total de pasajeros para la barra de progreso.
  $: progreso         = (seleccionados.filter(Boolean).length / pasajerosTotales) * 100;

  /**
   * Avanza al siguiente grupo de vuelo si no esta en el ultimo, o navega a 'carrito' cuando
   * todos los grupos de vuelo han sido mapeados. No hace nada si no todos los asientos estan seleccionados.
   */
  function handleContinuar() {
    if (!todoSeleccionado) return;
    if (!esUltimoGrupo) grupoActualIdx++;
    else navigateTo('carrito');
  }
</script>

<!-- Contenedor principal de la pagina de seleccion de asientos -->
<div class="asientos-page">
  <div class="asientos-page__container">

    <!-- Cabecera con boton de regreso e informacion del vuelo y clase activos -->
    <div class="asientos-page__header">
      <button class="asientos-page__back"
        on:click={() => grupoActualIdx > 0 ? grupoActualIdx-- : navigateTo('datos-pasajeros')}>
        Volver
      </button>
      <div class="asientos-titulo">
        <h1 class="asientos-titulo__main">Seleccion de Asientos</h1>
        <p class="asientos-titulo__sub">
          {avionMarca} {avionModelo}
          &nbsp;·&nbsp; Vuelo {numeroVuelo}
          &nbsp;·&nbsp; Clase {claseActual}
          &nbsp;·&nbsp; {pasajerosTotales} pasajero{pasajerosTotales > 1 ? 's' : ''}
          {#if totalGrupos > 1}&nbsp;·&nbsp;<strong>Vuelo {grupoActualIdx + 1} de {totalGrupos}</strong>{/if}
        </p>
      </div>
    </div>

    <!-- Indicador de progreso por vuelo cuando hay multiples grupos de vuelos -->
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

    <!-- Estado de carga o error mientras se obtiene el mapa de asientos desde la API -->
    {#if loading}
      <div class="asientos-estado">Cargando mapa de asientos...</div>
    {:else if error}
      <div class="asientos-estado asientos-estado--error">{error}</div>
    {:else}
      <div class="asientos-page__body">
        <div class="asientos-page__mapa-wrap">

          <!-- SVG decorativo de la nariz del avion -->
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

          <!-- Cuerpo del avion con encabezado de columnas, zona Ejecutiva y zona Turista -->
          <div class="avion-cuerpo">

            <!-- Etiquetas de columnas A-F con separador de pasillo central -->
            <div class="avion-cols-header">
              <div class="avion-col-label"></div>
              {#each COLS_LABEL as lbl, ci}
                {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                <div class="avion-col-label">{lbl}</div>
              {/each}
            </div>

            <!-- Filas de asientos de la zona Ejecutiva (filas 1 a 4) -->
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

            <!-- Separador visual entre la cabina Ejecutiva y la zona Turista -->
            <div class="zona-separador">
              <div class="zona-separador__line"></div>
              <span class="zona-separador__label">Separador de Cabina</span>
              <div class="zona-separador__line"></div>
            </div>

            <!-- Filas de asientos de la zona Turista (filas dinamicas segun el avion) -->
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

          <!-- SVG decorativo de la cola del avion -->
          <div class="avion-cola">
            <svg viewBox="0 0 220 50" fill="none" xmlns="http://www.w3.org/2000/svg" class="avion-cola__svg">
              <path d="M10 0 L210 0 L210 24 C180 44, 140 50, 110 50 C80 50, 40 44, 10 24 Z" fill="#1C1A18" stroke="#B89A7A" stroke-width="1.2"/>
            </svg>
          </div>

        </div>

        <!-- Panel lateral con progreso de asignacion, lista de pasajeros, leyenda y boton de continuar -->
        <aside class="asientos-sidebar">

          <!-- Barra de progreso de asientos asignados sobre el total de pasajeros -->
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

          <!-- Lista de pasajeros con estado de asiento asignado o pendiente por cada boleto -->
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

          <!-- Leyenda de colores de los estados posibles de un asiento -->
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

          <!-- Boton de avance al siguiente vuelo o confirmacion cuando todos los asientos estan asignados -->
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
              Selecciona {faltantes} asiento{faltantes !== 1 ? 's' : ''} mas
            {:else if !esUltimoGrupo}
              Siguiente vuelo
            {:else}
              Confirmar Asientos
            {/if}
          </button>

        </aside>
      </div>
    {/if}

  </div>
</div>
