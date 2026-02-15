<script>
// @ts-nocheck
  import '../styles/admin.css';
  import DetalleVueloAdmin from './DetalleVueloAdmin.svelte';
  import { onMount } from 'svelte';

  export let navigateTo;

  let activeSection = 'vuelos-base';
  let showDetailModal = false;
  let detailVuelo = null;

  let modoAgregarVuelo = 'nuevo';
  let vueloBaseSeleccionado = null;

  let usuarios = [];
  let loadingUsuarios = false;
  let rolesDisponibles = [
    { id: 1, nombre: 'Usuario' },
    { id: 2, nombre: 'Administrador' }
  ];

  let nuevoVuelo = {
    numeroVuelo: '',
    origen: '',
    destino: '',
    duracion: '',
    aeronave: '',
    terminal: '',
    puerta: '',
    asientosTurista: '',
    asientosEjecutiva: '',
    precioTurista: '',
    precioEjecutiva: '',
    escalas: 'directo'
  };

  const vuelosBase = [
    {
      id: 'VB-001',
      numeroVuelo: 'AF 1234',
      origen: 'Ciudad de Guatemala',
      destino: 'Paris',
      duracion: '10h 30m',
      aeronave: 'Boeing 787-9',
      escalas: 'Directo',
      asientosTurista: 200,
      asientosEjecutiva: 40,
      precioTurista: 650,
      precioEjecutiva: 1450,
      vecesUtilizado: 24
    },
    {
      id: 'VB-002',
      numeroVuelo: 'IB 9876',
      origen: 'Ciudad de Guatemala',
      destino: 'Madrid',
      duracion: '10h 30m',
      aeronave: 'Airbus A330',
      escalas: '2 Escalas',
      asientosTurista: 220,
      asientosEjecutiva: 35,
      precioTurista: 520,
      precioEjecutiva: 1180,
      vecesUtilizado: 18
    },
    {
      id: 'VB-003',
      numeroVuelo: 'LA 7890',
      origen: 'Ciudad de Guatemala',
      destino: 'Lima',
      duracion: '5h 30m',
      aeronave: 'Boeing 737-800',
      escalas: 'Directo',
      asientosTurista: 150,
      asientosEjecutiva: 20,
      precioTurista: 380,
      precioEjecutiva: 850,
      vecesUtilizado: 42
    }
  ];

  const historialVuelos = [
    {
      id: 'VH-2026-001',
      numeroVuelo: 'AF 1234',
      ruta: 'Ciudad de Guatemala → Paris',
      fecha: '2026-02-15',
      horaSalida: '08:00',
      horaLlegada: '18:30',
      estado: 'activo',
      asientosVendidos: 185,
      asientosTotales: 240
    },
    {
      id: 'VH-2026-002',
      numeroVuelo: 'IB 9876',
      ruta: 'Ciudad de Guatemala → Madrid',
      fecha: '2026-03-10',
      horaSalida: '14:00',
      horaLlegada: '00:30',
      estado: 'activo',
      asientosVendidos: 142,
      asientosTotales: 255
    },
    {
      id: 'VH-2025-087',
      numeroVuelo: 'LA 7890',
      ruta: 'Ciudad de Guatemala → Lima',
      fecha: '2025-12-15',
      horaSalida: '06:00',
      horaLlegada: '11:30',
      estado: 'completado',
      asientosVendidos: 170,
      asientosTotales: 170
    },
    {
      id: 'VH-2025-065',
      numeroVuelo: 'AF 1234',
      ruta: 'Ciudad de Guatemala → Paris',
      fecha: '2025-11-20',
      horaSalida: '08:00',
      horaLlegada: '18:30',
      estado: 'completado',
      asientosVendidos: 240,
      asientosTotales: 240
    },
    {
      id: 'VH-2025-043',
      numeroVuelo: 'CM 4521',
      ruta: 'Ciudad de Guatemala → Panama',
      fecha: '2025-10-05',
      horaSalida: '09:00',
      horaLlegada: '11:15',
      estado: 'cancelado',
      asientosVendidos: 0,
      asientosTotales: 180
    }
  ];

  onMount(async () => {
    const isAdmin = parseInt(sessionStorage.getItem('rolId')) === 2;
    if (!isAdmin) {
      navigateTo('acceso-denegado');
      return;
    }
    await cargarUsuarios();
  });

  async function cargarUsuarios() {
    loadingUsuarios = true;
    try {
      const rolId = sessionStorage.getItem('rolId');
      const response = await fetch('http://localhost:5190/api/usuarios', {
        headers: {
          'X-RolId': rolId
        }
      });

      if (response.ok) {
        usuarios = await response.json();
      } else {
        console.error('Error al cargar usuarios');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      loadingUsuarios = false;
    }
  }

  function viewVueloDetails(vuelo) {
    detailVuelo = vuelo;
    showDetailModal = true;
  }

  function closeModal() {
    showDetailModal = false;
    detailVuelo = null;
  }

  function handleSeleccionarVueloBase(vueloBase) {
    vueloBaseSeleccionado = vueloBase;
    nuevoVuelo = {
      numeroVuelo: vueloBase.numeroVuelo,
      origen: vueloBase.origen,
      destino: vueloBase.destino,
      duracion: vueloBase.duracion,
      aeronave: vueloBase.aeronave,
      terminal: '',
      puerta: '',
      asientosTurista: vueloBase.asientosTurista,
      asientosEjecutiva: vueloBase.asientosEjecutiva,
      precioTurista: vueloBase.precioTurista,
      precioEjecutiva: vueloBase.precioEjecutiva,
      escalas: vueloBase.escalas
    };
  }

  function handleAgregarVuelo() {
    console.log('Agregando nuevo vuelo:', nuevoVuelo);
    alert('Vuelo agregado exitosamente');
    nuevoVuelo = {
      numeroVuelo: '',
      origen: '',
      destino: '',
      duracion: '',
      aeronave: '',
      terminal: '',
      puerta: '',
      asientosTurista: '',
      asientosEjecutiva: '',
      precioTurista: '',
      precioEjecutiva: '',
      escalas: 'directo'
    };
    vueloBaseSeleccionado = null;
  }

  async function handleCambiarRol(userId, nuevoRolId) {
    try {
      const rolId = sessionStorage.getItem('rolId');
      const response = await fetch('http://localhost:5190/api/usuarios/cambiar-rol', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-RolId': rolId
        },
        body: JSON.stringify({ 
          usuarioId: parseInt(userId),
          nuevoRolId: parseInt(nuevoRolId) 
        })
      });

      if (response.ok) {
        const result = await response.json();
        await cargarUsuarios(); // Recargar la lista de usuarios
      } else {
        const error = await response.json();
        alert(error.message || 'Error al cambiar el rol');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error de conexión al cambiar el rol');
    }
  }

  function handleCambiarEstadoVuelo(vueloId, nuevoEstado) {
    console.log('Cambiando estado del vuelo:', vueloId, 'a', nuevoEstado);
  }
</script>

{#if showDetailModal && detailVuelo}
  <DetalleVueloAdmin vuelo={detailVuelo} onClose={closeModal} />
{/if}

<div class="admin">
  <div class="admin__container">
    <div class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>
        Salir del Panel
      </button>
      <h1 class="admin__title">Panel de Administracion</h1>
      <p class="admin__subtitle">Gestion de vuelos, usuarios y metricas</p>
    </div>

    <div class="admin__content">
      <aside class="admin__sidebar">
        <nav class="admin-nav">
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'vuelos-base'}
            on:click={() => activeSection = 'vuelos-base'}
          >
            Vuelos Base
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'agregar-vuelo'}
            on:click={() => activeSection = 'agregar-vuelo'}
          >
            Agregar Vuelo
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'historial'}
            on:click={() => activeSection = 'historial'}
          >
            Historial
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'usuarios'}
            on:click={() => activeSection = 'usuarios'}
          >
            Usuarios
          </button>
          <button 
            class="admin-nav__item" 
            class:admin-nav__item--active={activeSection === 'metricas'}
            on:click={() => activeSection = 'metricas'}
          >
            Metricas
          </button>
        </nav>
      </aside>

      <main class="admin__main">
        {#if activeSection === 'vuelos-base'}
          <section class="admin-section">
            <h2 class="admin-section__title">Vuelos Base</h2>
            <p class="admin-section__subtitle">Resumen de todos los vuelos distintos reutilizables</p>

            <div class="vuelos-table">
              <table class="table">
                <thead class="table__head">
                  <tr class="table__row">
                    <th class="table__header">Numero de Vuelo</th>
                    <th class="table__header">Ruta</th>
                    <th class="table__header">Duracion</th>
                    <th class="table__header">Aeronave</th>
                    <th class="table__header">Escalas</th>
                    <th class="table__header">Veces Utilizado</th>
                    <th class="table__header">Acciones</th>
                  </tr>
                </thead>
                <tbody class="table__body">
                  {#each vuelosBase as vuelo}
                    <tr class="table__row">
                      <td class="table__cell">{vuelo.numeroVuelo}</td>
                      <td class="table__cell">{vuelo.origen} → {vuelo.destino}</td>
                      <td class="table__cell">{vuelo.duracion}</td>
                      <td class="table__cell">{vuelo.aeronave}</td>
                      <td class="table__cell">{vuelo.escalas}</td>
                      <td class="table__cell">{vuelo.vecesUtilizado}</td>
                      <td class="table__cell">
                        <div class="table__actions">
                          <button 
                            class="table__action-btn table__action-btn--view"
                            on:click={() => viewVueloDetails(vuelo)}
                          >
                            Ver Detalles
                          </button>
                        </div>
                      </td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            </div>
          </section>

        {:else if activeSection === 'agregar-vuelo'}
          <section class="admin-section">
            <h2 class="admin-section__title">Agregar Nuevo Vuelo</h2>
            <p class="admin-section__subtitle">Crea un vuelo nuevo o reutiliza uno existente</p>

            <div class="admin-form__mode-selector">
              <button 
                class="mode-btn" 
                class:mode-btn--active={modoAgregarVuelo === 'reutilizar'}
                on:click={() => {
                  modoAgregarVuelo = 'reutilizar';
                  vueloBaseSeleccionado = null;
                }}
              >
                Reutilizar Vuelo Base
              </button>
              <button 
                class="mode-btn" 
                class:mode-btn--active={modoAgregarVuelo === 'nuevo'}
                on:click={() => {
                  modoAgregarVuelo = 'nuevo';
                  vueloBaseSeleccionado = null;
                  nuevoVuelo = {
                    numeroVuelo: '',
                    origen: '',
                    destino: '',
                    duracion: '',
                    aeronave: '',
                    terminal: '',
                    puerta: '',
                    asientosTurista: '',
                    asientosEjecutiva: '',
                    precioTurista: '',
                    precioEjecutiva: '',
                    escalas: 'directo'
                  };
                }}
              >
                Vuelo Completamente Nuevo
              </button>
            </div>

            {#if modoAgregarVuelo === 'reutilizar'}
              <div class="vuelos-base-selector">
                <h3 class="selector-title">Selecciona un vuelo base para reutilizar:</h3>
                <div class="vuelos-base-grid">
                  {#each vuelosBase as vueloBase}
                    <button 
                      class="vuelo-base-card"
                      class:vuelo-base-card--selected={vueloBaseSeleccionado?.id === vueloBase.id}
                      on:click={() => handleSeleccionarVueloBase(vueloBase)}
                    >
                      <div class="vuelo-base-card__number">{vueloBase.numeroVuelo}</div>
                      <div class="vuelo-base-card__route">{vueloBase.origen} → {vueloBase.destino}</div>
                      <div class="vuelo-base-card__details">
                        <span>{vueloBase.duracion}</span>
                        <span>{vueloBase.escalas}</span>
                      </div>
                    </button>
                  {/each}
                </div>
              </div>
            {/if}

            {#if modoAgregarVuelo === 'nuevo' || vueloBaseSeleccionado}
              <form class="admin-form" on:submit|preventDefault={handleAgregarVuelo}>
                <div class="admin-form__group">
                  <h3 class="admin-form__group-title">Informacion Basica</h3>
                  
                  <div class="admin-form__row">
                    <div class="admin-form__field">
                      <label for="numeroVuelo" class="admin-form__label">Numero de Vuelo</label>
                      <input 
                        type="text" 
                        id="numeroVuelo"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.numeroVuelo}
                        placeholder="AF 1234"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="aeronave" class="admin-form__label">Aeronave</label>
                      <input 
                        type="text" 
                        id="aeronave"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.aeronave}
                        placeholder="Boeing 787-9"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="escalas" class="admin-form__label">Escalas</label>
                      <select 
                        id="escalas"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.escalas}
                      >
                        <option value="directo">Directo</option>
                        <option value="1 Escala">1 Escala</option>
                        <option value="2 Escalas">2 Escalas</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div class="admin-form__group">
                  <h3 class="admin-form__group-title">Ruta</h3>
                  
                  <div class="admin-form__row">
                    <div class="admin-form__field">
                      <label for="origen" class="admin-form__label">Origen</label>
                      <input 
                        type="text" 
                        id="origen"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.origen}
                        placeholder="Ciudad de Guatemala"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="destino" class="admin-form__label">Destino</label>
                      <input 
                        type="text" 
                        id="destino"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.destino}
                        placeholder="Paris"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="duracion" class="admin-form__label">Duracion</label>
                      <input 
                        type="text" 
                        id="duracion"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.duracion}
                        placeholder="10h 30m"
                        required
                      />
                    </div>
                  </div>
                </div>

                <div class="admin-form__group">
                  <h3 class="admin-form__group-title">Terminal y Puerta</h3>
                  
                  <div class="admin-form__row">
                    <div class="admin-form__field">
                      <label for="terminal" class="admin-form__label">Terminal</label>
                      <input 
                        type="text" 
                        id="terminal"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.terminal}
                        placeholder="Terminal 2E"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="puerta" class="admin-form__label">Puerta</label>
                      <input 
                        type="text" 
                        id="puerta"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.puerta}
                        placeholder="K45"
                        required
                      />
                    </div>
                  </div>
                </div>

                <div class="admin-form__group">
                  <h3 class="admin-form__group-title">Capacidad</h3>
                  
                  <div class="admin-form__row">
                    <div class="admin-form__field">
                      <label for="asientosTurista" class="admin-form__label">Asientos Turista</label>
                      <input 
                        type="number" 
                        id="asientosTurista"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.asientosTurista}
                        placeholder="200"
                        min="0"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="asientosEjecutiva" class="admin-form__label">Asientos Ejecutiva</label>
                      <input 
                        type="number" 
                        id="asientosEjecutiva"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.asientosEjecutiva}
                        placeholder="40"
                        min="0"
                        required
                      />
                    </div>
                  </div>
                </div>

                <div class="admin-form__group">
                  <h3 class="admin-form__group-title">Precios</h3>
                  
                  <div class="admin-form__row">
                    <div class="admin-form__field">
                      <label for="precioTurista" class="admin-form__label">Precio Turista ($)</label>
                      <input 
                        type="number" 
                        id="precioTurista"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.precioTurista}
                        placeholder="650"
                        min="0"
                        step="0.01"
                        required
                      />
                    </div>

                    <div class="admin-form__field">
                      <label for="precioEjecutiva" class="admin-form__label">Precio Ejecutiva ($)</label>
                      <input 
                        type="number" 
                        id="precioEjecutiva"
                        class="admin-form__input"
                        bind:value={nuevoVuelo.precioEjecutiva}
                        placeholder="1450"
                        min="0"
                        step="0.01"
                        required
                      />
                    </div>
                  </div>
                </div>

                <button type="submit" class="admin-form__submit">
                  Agregar Vuelo
                </button>
              </form>
            {/if}
          </section>

        {:else if activeSection === 'historial'}
          <section class="admin-section">
            <h2 class="admin-section__title">Historial de Vuelos</h2>
            <p class="admin-section__subtitle">Todos los vuelos programados, activos, completados y cancelados</p>

            <div class="vuelos-table">
              <table class="table">
                <thead class="table__head">
                  <tr class="table__row">
                    <th class="table__header">ID</th>
                    <th class="table__header">Numero de Vuelo</th>
                    <th class="table__header">Ruta</th>
                    <th class="table__header">Fecha</th>
                    <th class="table__header">Horario</th>
                    <th class="table__header">Ocupacion</th>
                    <th class="table__header">Estado</th>
                    <th class="table__header">Acciones</th>
                  </tr>
                </thead>
                <tbody class="table__body">
                  {#each historialVuelos as vuelo}
                    <tr class="table__row">
                      <td class="table__cell">{vuelo.id}</td>
                      <td class="table__cell">{vuelo.numeroVuelo}</td>
                      <td class="table__cell">{vuelo.ruta}</td>
                      <td class="table__cell">{vuelo.fecha}</td>
                      <td class="table__cell">{vuelo.horaSalida} - {vuelo.horaLlegada}</td>
                      <td class="table__cell">{vuelo.asientosVendidos}/{vuelo.asientosTotales}</td>
                      <td class="table__cell">
                        <span class="status-badge status-badge--{vuelo.estado}">
                          {vuelo.estado}
                        </span>
                      </td>
                      <td class="table__cell">
                        <div class="table__actions">
                          {#if vuelo.estado === 'activo'}
                            <button 
                              class="table__action-btn table__action-btn--cancel"
                              on:click={() => handleCambiarEstadoVuelo(vuelo.id, 'cancelado')}
                            >
                              Cancelar
                            </button>
                          {/if}
                        </div>
                      </td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            </div>
          </section>

        {:else if activeSection === 'usuarios'}
          <section class="admin-section">
            <h2 class="admin-section__title">Gestion de Usuarios</h2>
            <p class="admin-section__subtitle">Administra los roles de los usuarios del sistema</p>

            {#if loadingUsuarios}
              <p>Cargando usuarios...</p>
            {:else}
              <div class="vuelos-table">
                <table class="table">
                  <thead class="table__head">
                    <tr class="table__row">
                      <th class="table__header">ID</th>
                      <th class="table__header">Nombre</th>
                      <th class="table__header">Email</th>
                      <th class="table__header">Username</th>
                      <th class="table__header">Rol Actual</th>
                      <th class="table__header">Cambiar Rol</th>
                    </tr>
                  </thead>
                  <tbody class="table__body">
                    {#each usuarios as usuario}
                      <tr class="table__row">
                        <td class="table__cell">{usuario.id}</td>
                        <td class="table__cell">{usuario.nombre} {usuario.apellido}</td>
                        <td class="table__cell">{usuario.correo}</td>
                        <td class="table__cell">{usuario.username}</td>
                        <td class="table__cell">
                          <span class="rol-badge rol-badge--{usuario.rolNombre.toLowerCase().replace(/\s+/g, '-')}">
                            {usuario.rolNombre}
                          </span>
                        </td>
                        <td class="table__cell">
                          <div class="table__actions">
                            <select 
                              class="rol-select"
                              value={usuario.rolId}
                              on:change={(e) => handleCambiarRol(usuario.id, e.target.value)}
                            >
                              {#each rolesDisponibles as rol}
                                <option value={rol.id}>
                                  {rol.nombre}
                                </option>
                              {/each}
                            </select>
                          </div>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </section>

        {:else if activeSection === 'metricas'}
          <section class="admin-section">
            <h2 class="admin-section__title">Metricas</h2>
            <p class="admin-section__subtitle">Analisis y estadisticas del sistema</p>

            <div class="placeholder-card">
              <p class="placeholder-card__text">
                Esta seccion estara disponible proximamente con metricas detalladas.
              </p>
            </div>
          </section>
        {/if}
      </main>
    </div>
  </div>
</div>