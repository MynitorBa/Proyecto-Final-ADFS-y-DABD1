<script>
// @ts-nocheck
  import '../styles/destinos.css';
  export let navigateTo;

  const destinosDestacados = [
    {
      id: 1,
      nombre: 'Paris',
      pais: 'Francia',
      imagen: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800',
      descripcion: 'La ciudad del amor y la luz. Descubre la Torre Eiffel, el Louvre, Notre-Dame y disfruta de la exquisita gastronomia francesa.',
      destacados: [
        'Torre Eiffel',
        'Museo del Louvre',
        'Arco del Triunfo',
        'Catedral de Notre-Dame',
        'Campos Eliseos'
      ],
      precioDesde: 650,
      duracionVuelo: '10h 30m',
      mejorTemporada: 'Abril - Octubre',
      clima: 'Templado',
      moneda: 'Euro',
      idioma: 'Frances',
      zonaHoraria: 'GMT+1'
    },
    {
      id: 2,
      nombre: 'Tokio',
      pais: 'Japon',
      imagen: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800',
      descripcion: 'Una mezcla perfecta de tradicion y modernidad. Templos ancestrales, tecnologia de vanguardia y una cultura unica.',
      destacados: [
        'Torre de Tokio',
        'Templo Senso-ji',
        'Cruce de Shibuya',
        'Palacio Imperial',
        'Monte Fuji'
      ],
      precioDesde: 890,
      duracionVuelo: '16h 30m',
      mejorTemporada: 'Marzo - Mayo, Septiembre - Noviembre',
      clima: 'Subtropical humedo',
      moneda: 'Yen',
      idioma: 'Japones',
      zonaHoraria: 'GMT+9'
    },
    {
      id: 3,
      nombre: 'Madrid',
      pais: 'España',
      imagen: 'https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=800',
      descripcion: 'Capital vibrante llena de arte, cultura y vida nocturna. Museos de clase mundial, parques hermosos y tapas deliciosas.',
      destacados: [
        'Museo del Prado',
        'Palacio Real',
        'Parque del Retiro',
        'Plaza Mayor',
        'Puerta del Sol'
      ],
      precioDesde: 580,
      duracionVuelo: '10h 15m',
      mejorTemporada: 'Mayo - Octubre',
      clima: 'Mediterraneo continental',
      moneda: 'Euro',
      idioma: 'Español',
      zonaHoraria: 'GMT+1'
    },
    {
      id: 4,
      nombre: 'Londres',
      pais: 'Reino Unido',
      imagen: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800',
      descripcion: 'Ciudad cosmopolita con historia milenaria. Desde el Big Ben hasta los modernos rascacielos, una experiencia inolvidable.',
      destacados: [
        'Big Ben',
        'Torre de Londres',
        'Palacio de Buckingham',
        'London Eye',
        'British Museum'
      ],
      precioDesde: 710,
      duracionVuelo: '10h 30m',
      mejorTemporada: 'Mayo - Septiembre',
      clima: 'Oceanico',
      moneda: 'Libra Esterlina',
      idioma: 'Ingles',
      zonaHoraria: 'GMT+0'
    },
    {
      id: 5,
      nombre: 'Roma',
      pais: 'Italia',
      imagen: 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800',
      descripcion: 'La ciudad eterna. Historia antigua, arte renacentista, arquitectura barroca y la mejor comida italiana.',
      destacados: [
        'Coliseo Romano',
        'Ciudad del Vaticano',
        'Fontana di Trevi',
        'Foro Romano',
        'Capilla Sixtina'
      ],
      precioDesde: 690,
      duracionVuelo: '10h 15m',
      mejorTemporada: 'Abril - Junio, Septiembre - Octubre',
      clima: 'Mediterraneo',
      moneda: 'Euro',
      idioma: 'Italiano',
      zonaHoraria: 'GMT+1'
    },
    {
      id: 6,
      nombre: 'Dubai',
      pais: 'Emiratos Arabes Unidos',
      imagen: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800',
      descripcion: 'Ciudad del futuro en el desierto. Rascacielos impresionantes, lujo y compras de clase mundial.',
      destacados: [
        'Burj Khalifa',
        'Palm Jumeirah',
        'Dubai Mall',
        'Desierto de Dubai',
        'Burj Al Arab'
      ],
      precioDesde: 950,
      duracionVuelo: '19h 30m',
      mejorTemporada: 'Noviembre - Marzo',
      clima: 'Desertico',
      moneda: 'Dirham',
      idioma: 'Arabe',
      zonaHoraria: 'GMT+4'
    }
  ];

  function buscarVuelos(destino) {
    console.log('Buscando vuelos a:', destino);
    navigateTo('vuelos');
  }
</script>

<div class="destinos-destacados">
  <div class="destinos-destacados__container">
    <div class="destinos-destacados__header">
      <button class="destinos-destacados__back" on:click={() => navigateTo('home')}>
        Volver al inicio
      </button>
      <h1 class="destinos-destacados__title">Destinos Destacados</h1>
      <p class="destinos-destacados__subtitle">Explora los destinos mas populares y planifica tu proxima aventura</p>
    </div>

    <div class="destinos-destacados__content">
      <div class="destinos-grid">
        {#each destinosDestacados as destino}
          <article class="destino-card">
            <div class="destino-card__image-container">
              <img 
                src={destino.imagen} 
                alt={destino.nombre} 
                class="destino-card__image"
                on:error={(e) => {
                  e.target.src = 'https://via.placeholder.com/800x600?text=' + destino.nombre;
                }}
              />
              <div class="destino-card__badge">Desde ${destino.precioDesde}</div>
            </div>

            <div class="destino-card__content">
              <div class="destino-card__header">
                <h2 class="destino-card__title">{destino.nombre}</h2>
                <p class="destino-card__country">{destino.pais}</p>
              </div>

              <p class="destino-card__description">{destino.descripcion}</p>

              <div class="destino-card__highlights">
                <h3 class="destino-card__highlights-title">Lugares destacados</h3>
                <ul class="highlights-list">
                  {#each destino.destacados.slice(0, 3) as lugar}
                    <li class="highlights-list__item">{lugar}</li>
                  {/each}
                </ul>
              </div>

              <div class="destino-card__info">
                <div class="info-item">
                  <span class="info-item__label">Duracion de vuelo</span>
                  <span class="info-item__value">{destino.duracionVuelo}</span>
                </div>
                <div class="info-item">
                  <span class="info-item__label">Mejor temporada</span>
                  <span class="info-item__value">{destino.mejorTemporada}</span>
                </div>
              </div>

              <div class="destino-card__actions">
                <button 
                  class="destino-card__btn destino-card__btn--primary"
                  on:click={() => buscarVuelos(destino.nombre)}
                >
                  Buscar Vuelos
                </button>
                
              </div>
            </div>

            <div class="destino-card__details">
              <h3 class="destino-card__details-title">Informacion del destino</h3>
              <div class="destino-details-grid">
                <div class="detail-box">
                  <span class="detail-box__label">Clima</span>
                  <span class="detail-box__value">{destino.clima}</span>
                </div>
                <div class="detail-box">
                  <span class="detail-box__label">Moneda</span>
                  <span class="detail-box__value">{destino.moneda}</span>
                </div>
                <div class="detail-box">
                  <span class="detail-box__label">Idioma</span>
                  <span class="detail-box__value">{destino.idioma}</span>
                </div>
                <div class="detail-box">
                  <span class="detail-box__label">Zona Horaria</span>
                  <span class="detail-box__value">{destino.zonaHoraria}</span>
                </div>
              </div>
            </div>
          </article>
        {/each}
      </div>
    </div>
  </div>
</div>