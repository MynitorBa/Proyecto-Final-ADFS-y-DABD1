<script>
  import '../styles/infoasiento.css';
  
  export let navigateTo;
  
  const seatTypes = [
    {
      name: 'Turista',
      images: [
        'https://th.bing.com/th/id/R.e669280b0176e1fa356cd6f32814e6e2?rik=E1PkLctZnKrzIw&riu=http%3a%2f%2fwww.pasajesaereos.app%2fblog%2fwp-content%2fuploads%2f2023%2f11%2favion-1024x576.jpg&ehk=gbFoL1vPRJlv81Gmqv15P2xaz28MBeFevOpQl5ORCOk%3d&risl=&pid=ImgRaw&r=0',
        'https://imageio.forbes.com/specials-images/imageserve/64775da2a77066e04aaedce3/best-airline-food/960x0.jpg?format=jpg&width=960',
        'https://th.bing.com/th/id/R.583cbc0ac1c1527bfcb78e1974240fad?rik=KHm07Fw1p%2bVYag&pid=ImgRaw&r=0'
      ],
      description: [
        'Espacio estándar',
        'Servicio básico'
      ],
      details: [
        { label: 'Espacio entre asientos', value: '76-81 cm (30-32 pulgadas)' },
        { label: 'Equipaje incluido', value: '1 maleta de 23 kg + 1 equipaje de mano' },
        { label: 'Servicios a bordo', value: 'Bebidas básicas y snacks' }
      ]
    },
    {
      name: 'Business',
      images: [
        'https://hips.hearstapps.com/hmg-prod/images/volar-mejor-primera-clase-mundo-64528829dbf02.jpg?resize=1200:*',
        'https://www.flightgift.com/media/wp/FG/2023/10/first-class-flight-1.webp',
        'https://th.bing.com/th/id/R.692eaf28957360f2445a9913eca35b90?rik=3wqCQt6k%2bprOLA&pid=ImgRaw&r=0'
      ],
      description: [
        'Mayor comodidad',
        'Servicios premium'
      ],
      details: [
        { label: 'Mayor espacio', value: '127-152 cm (50-60 pulgadas) - Asientos reclinables' },
        { label: 'Prioridad de abordaje', value: 'Acceso prioritario y salas VIP' },
        { label: 'Servicios a bordo mejorados', value: 'Menú gourmet, entretenimiento premium y amenities' }
      ]
    }
  ];
</script>

<div class="info-asientos">
  <div class="info-asientos__container">
    
    <!-- Header -->
    <header class="info-asientos__header">
      <button 
        class="info-asientos__back" 
        on:click={() => navigateTo('home')}
        aria-label="Volver al inicio"
      >
        ← Volver
      </button>
      <h1 class="info-asientos__title">Tipos de asiento</h1>
      <p class="info-asientos__subtitle">
        Conoce las características de cada clase para elegir la mejor opción para tu viaje
      </p>
    </header>

    <!-- Grid de Tipos de Asiento -->
    <div class="asientos-grid">
      {#each seatTypes as seat, index}
        <article class="asiento-card" style="animation-delay: {index * 0.15}s">
          
          <!-- Nombre del Asiento -->
          <div class="asiento-card__header">
            <h2 class="asiento-card__name">{seat.name}</h2>
          </div>

          <!-- Galería de Imágenes -->
          <div class="asiento-card__gallery">
            {#each seat.images as image, imgIndex}
              <div class="gallery-item">
                <img 
                  src={image} 
                  alt="{seat.name} - Vista {imgIndex + 1}"
                  class="gallery-item__image"
                  loading="lazy"
                />
              </div>
            {/each}
          </div>

          <!-- Descripción Breve -->
          <div class="asiento-card__description">
            <ul class="description-list">
              {#each seat.description as desc}
                <li class="description-list__item">{desc}</li>
              {/each}
            </ul>
          </div>

          <!-- Información Detallada -->
          <div class="asiento-card__details">
            <h3 class="asiento-card__details-title">Información puntual</h3>
            <div class="details-list">
              {#each seat.details as detail}
                <div class="detail-item">
                  <dt class="detail-item__label">{detail.label}</dt>
                  <dd class="detail-item__value">{detail.value}</dd>
                </div>
              {/each}
            </div>
          </div>

        </article>
      {/each}
    </div>

  </div>
</div>