<script>
  import '../styles/infoseguridad.css';
  
  export let navigateTo;
  
  const sections = [
    {
      id: 'equipaje',
      title: 'Equipaje permitido',
      subsections: [
        {
          name: 'Equipaje de mano',
          items: [
            { label: 'Dimensiones generales', value: '55 x 40 x 23 cm (22 x 16 x 9 pulgadas)' },
            { label: 'Peso máximo', value: '10 kg (22 libras)' }
          ]
        },
        {
          name: 'Equipaje documentado',
          items: [
            { label: 'Cantidad permitida', value: '1-2 maletas según clase de servicio' },
            { label: 'Peso máximo', value: '23 kg (50 libras) por maleta en clase turista, 32 kg (70 libras) en business' }
          ]
        }
      ],
      note: 'Objetos prohibidos incluyen: armas, líquidos en envases mayores a 100ml (en equipaje de mano), sustancias inflamables, explosivos y materiales peligrosos. Consulta la lista completa en el aeropuerto.'
    },
    {
      id: 'checkin',
      title: 'Proceso de check-in',
      steps: [
        {
          number: 1,
          title: 'Llegar con anticipación',
          description: 'Se recomienda llegar 3 horas antes para vuelos internacionales y 2 horas para vuelos nacionales'
        },
        {
          number: 2,
          title: 'Presentar documentos',
          description: 'Ten a la mano tu pasaporte/identificación oficial y confirmación de reserva'
        },
        {
          number: 3,
          title: 'Entregar equipaje (si aplica)',
          description: 'Documenta tu equipaje en el mostrador o en los quioscos automatizados'
        },
        {
          number: 4,
          title: 'Recibir pase de abordar',
          description: 'Obtendrás tu pase de abordar impreso o digital con tu número de asiento y puerta de embarque'
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
          description: 'Abordaje por zonas o números de asiento según indicaciones del personal'
        }
      ],
      rules: [
        'Mantén tu pase de abordar y documento de identidad a la mano',
        'Respeta las instrucciones del personal de vuelo en todo momento',
        'Asegúrate de abordar por la puerta correcta según tu pase'
      ]
    }
  ];
</script>

<div class="info-seguridad">
  <div class="info-seguridad__container">
    
    <!-- Header -->
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
        Todo lo que necesitas saber sobre normas y pasos básicos para tu viaje
      </p>
    </header>

    <!-- Secciones -->
    <div class="secciones-container">
      
      <!-- SECCIÓN: EQUIPAJE -->
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

      <!-- SECCIÓN: CHECK-IN -->
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

      <!-- SECCIÓN: ABORDAJE -->
      <section class="seccion-card" style="animation-delay: 0.3s">
        <div class="seccion-card__header">
          <h2 class="seccion-card__title">{sections[2].title}</h2>
        </div>

        <div class="seccion-card__content">
          <!-- Orden de abordaje -->
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

          <!-- Reglas básicas -->
          <div class="abordaje-section">
            <h3 class="abordaje-section__subtitle">Reglas básicas</h3>
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