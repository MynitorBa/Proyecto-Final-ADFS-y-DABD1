<script>
/**
 * @file InformacionSeguridad.svelte
 * @description Pagina informativa estatica que cubre las pautas de seguridad y proceso de viaje
 * para pasajeros de Broom AirLine. Organizada en tres secciones: equipaje permitido (de mano
 * y documentado con limites de peso y articulos prohibidos), el proceso de check-in como lista
 * de pasos numerados y las reglas de orden de abordaje que incluyen grupos prioritarios y
 * reglamento general. Accesible desde la navegacion principal con un boton de regreso al inicio.
 */
  import '../styles/infoseguridad.css';

  /** Funcion utilizada para navegar a otra pagina en la aplicacion. @type {function} */
  export let navigateTo;

  /**
   * Arreglo estatico de secciones de contenido. Cada seccion tiene un id, titulo y datos
   * especificos por tipo: la seccion de equipaje tiene subsecciones con arreglos de elementos
   * y una nota, la seccion de check-in tiene un arreglo de pasos, y la seccion de abordaje
   * tiene arreglos de orderGroups y reglas.
   * @type {Array<object>}
   */
  const sections = [
    {
      id: 'equipaje',
      title: 'Equipaje permitido',
      subsections: [
        {
          name: 'Equipaje de mano',
          items: [
            { label: 'Dimensiones generales', value: '55 x 40 x 23 cm (22 x 16 x 9 pulgadas)' },
            { label: 'Peso maximo', value: '10 kg (22 libras)' }
          ]
        },
        {
          name: 'Equipaje documentado',
          items: [
            { label: 'Cantidad permitida', value: '1-2 maletas segun clase de servicio' },
            { label: 'Peso maximo', value: '23 kg (50 libras) por maleta en clase turista, 32 kg (70 libras) en business' }
          ]
        }
      ],
      note: 'Objetos prohibidos incluyen: armas, liquidos en envases mayores a 100ml (en equipaje de mano), sustancias inflamables, explosivos y materiales peligrosos. Consulta la lista completa en el aeropuerto.'
    },
    {
      id: 'checkin',
      title: 'Proceso de check-in',
      steps: [
        {
          number: 1,
          title: 'Llegar con anticipacion',
          description: 'Se recomienda llegar 3 horas antes para vuelos internacionales y 2 horas para vuelos nacionales'
        },
        {
          number: 2,
          title: 'Presentar documentos',
          description: 'Ten a la mano tu pasaporte/identificacion oficial y confirmacion de reserva'
        },
        {
          number: 3,
          title: 'Entregar equipaje (si aplica)',
          description: 'Documenta tu equipaje en el mostrador o en los quioscos automatizados'
        },
        {
          number: 4,
          title: 'Recibir pase de abordar',
          description: 'Obtendras tu pase de abordar impreso o digital con tu numero de asiento y puerta de embarque'
        }
      ]
    },
    {
      id: 'abordaje',
      title: 'Abordaje',

      orderGroups: [
        {
          priority: 'Prioridad',
          description: 'Pasajeros de clase business y personas con necesidades especiales'
        },
        {
          priority: 'Grupos generales',
          description: 'Abordaje por zonas o numeros de asiento segun indicaciones del personal'
        }
      ],
      rules: [
        'Manten tu pase de abordar y documento de identidad a la mano',
        'Respeta las instrucciones del personal de vuelo en todo momento',
        'Asegurate de abordar por la puerta correcta segun tu pase'
      ]
    }
  ];
</script>

<!-- Contenedor principal de la pagina de informacion de seguridad -->
<div class="info-seguridad">
  <div class="info-seguridad__container">

    <!-- Encabezado de pagina con boton de regreso y titulo -->
    <header class="info-seguridad__header">
      <button
        class="info-seguridad__back"
        on:click={() => navigateTo('home')}
        aria-label="Volver al inicio"
      >
        ← Volver
      </button>
      <h1 class="info-seguridad__title">Seguridad y procesos</h1>
      <p class="info-seguridad__subtitle">
        Todo lo que necesitas saber sobre normas y pasos basicos para tu viaje
      </p>
    </header>

    <!-- Grid de tarjetas de secciones informativas -->
    <div class="secciones-container">

      <!-- Seccion de equipaje permitido con subsecciones y nota de objetos prohibidos -->
      <section class="seccion-card" style="animation-delay: 0.1s">
        <div class="seccion-card__header">
          <h2 class="seccion-card__title">{sections[0].title}</h2>
        </div>

        <div class="seccion-card__content">
          {#each sections[0].subsections as subsection}
            <div class="equipaje-subsection">
              <h3 class="equipaje-subsection__name">{subsection.name}</h3>
              <div class="equipaje-details">
                {#each subsection.items as item}
                  <div class="equipaje-item">
                    <dt class="equipaje-item__label">{item.label}</dt>
                    <dd class="equipaje-item__value">{item.value}</dd>
                  </div>
                {/each}
              </div>
            </div>
          {/each}

          <div class="seccion-note">
            <strong>Nota importante:</strong> {sections[0].note}
          </div>
        </div>
      </section>

      <!-- Seccion de proceso de check-in con pasos numerados -->
      <section class="seccion-card" style="animation-delay: 0.2s">
        <div class="seccion-card__header">
          <h2 class="seccion-card__title">{sections[1].title}</h2>
        </div>

        <div class="seccion-card__content">
          <div class="steps-container">
            {#each sections[1].steps as step}
              <div class="step-item">
                <div class="step-item__number">{step.number}</div>
                <div class="step-item__content">
                  <h3 class="step-item__title">{step.title}</h3>
                  <p class="step-item__description">{step.description}</p>
                </div>
              </div>
            {/each}
          </div>
        </div>
      </section>

      <!-- Seccion de abordaje con grupos de orden y reglas basicas -->
      <section class="seccion-card" style="animation-delay: 0.3s">
        <div class="seccion-card__header">
          <h2 class="seccion-card__title">{sections[2].title}</h2>
        </div>

        <div class="seccion-card__content">
          <div class="abordaje-section">
            <h3 class="abordaje-section__subtitle">Orden general</h3>
            <div class="abordaje-groups">
              {#each sections[2].orderGroups as group}
                <div class="abordaje-group">
                  <h4 class="abordaje-group__priority">{group.priority}</h4>
                  <p class="abordaje-group__description">{group.description}</p>
                </div>
              {/each}
            </div>
          </div>

          <div class="abordaje-section">
            <h3 class="abordaje-section__subtitle">Reglas basicas</h3>
            <ul class="reglas-list">
              {#each sections[2].rules as rule}
                <li class="reglas-list__item">{rule}</li>
              {/each}
            </ul>
          </div>
        </div>
      </section>

    </div>

  </div>
</div>
