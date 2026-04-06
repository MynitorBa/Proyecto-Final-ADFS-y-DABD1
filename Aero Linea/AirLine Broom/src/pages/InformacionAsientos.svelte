<script>
/**
 * @file InformacionAsientos.svelte
 * @description Informational page that explains the two available seat classes offered by
 * Broom AirLine: Turista and Business. For each class it renders a photo gallery with
 * three images, a brief description list, and a detailed specs table covering legroom,
 * baggage allowance, and on-board services. Accessible from the main navigation and
 * contains a back button that returns the user to the home page.
 */
  import '../styles/infoasiento.css';

  /** Function used to navigate to another page in the application. @type {function} */
  export let navigateTo;

  /**
   * Static data array describing each seat class. Each entry contains the class name,
   * an array of image URLs for the gallery, a short description list, and a details
   * array with labeled specification rows.
   * @type {Array<{name: string, images: Array<string>, description: Array<string>, details: Array<{label: string, value: string}>}>}
   */
  const seatTypes = [
    {
      name: 'Turista',
      images: [
        'https://th.bing.com/th/id/R.e669280b0176e1fa356cd6f32814e6e2?rik=E1PkLctZnKrzIw&riu=http%3a%2f%2fwww.pasajesaereos.app%2fblog%2fwp-content%2fuploads%2f2023%2f11%2favion-1024x576.jpg&ehk=gbFoL1vPRJlv81Gmqv15P2xaz28MBeFevOpQl5ORCOk%3d&risl=&pid=ImgRaw&r=0',
        'https://imageio.forbes.com/specials-images/imageserve/64775da2a77066e04aaedce3/best-airline-food/960x0.jpg?format=jpg&width=960',
        'https://th.bing.com/th/id/R.583cbc0ac1c1527bfcb78e1974240fad?rik=KHm07Fw1p%2bVYag&pid=ImgRaw&r=0'
      ],
      description: [
        'Espacio estandar',
        'Servicio basico'
      ],
      details: [
        { label: 'Espacio entre asientos', value: '76-81 cm (30-32 pulgadas)' },
        { label: 'Equipaje incluido', value: '1 maleta de 23 kg + 1 equipaje de mano' },
        { label: 'Servicios a bordo', value: 'Bebidas basicas y snacks' }
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
        { label: 'Servicios a bordo mejorados', value: 'Menu gourmet, entretenimiento premium y amenities' }
      ]
    }
  ];
</script>

<!-- Contenedor principal de la pagina de informacion de tipos de asiento -->
<div class="info-asientos">
  <div class="info-asientos__container">

    <!-- Encabezado con titulo y descripcion de la seccion de tipos de asiento -->
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
        Conoce las caracteristicas de cada clase para elegir la mejor opcion para tu viaje
      </p>
    </header>

    <!-- Grilla de tarjetas por clase de asiento con galeria, descripcion y especificaciones -->
    <div class="asientos-grid">
      {#each seatTypes as seat, index}
        <article class="asiento-card" style="animation-delay: {index * 0.15}s">

          <div class="asiento-card__header">
            <h2 class="asiento-card__name">{seat.name}</h2>
          </div>

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

          <div class="asiento-card__description">
            <ul class="description-list">
              {#each seat.description as desc}
                <li class="description-list__item">{desc}</li>
              {/each}
            </ul>
          </div>

          <div class="asiento-card__details">
            <h3 class="asiento-card__details-title">Informacion puntual</h3>
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
