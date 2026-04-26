<script>
/**
 * @file Register.svelte
 * @description Pagina completa de registro de usuario de Broom AirLine. Al montar limpia el formulario y
 * carga datos de paises/ciudades desde countriesnow.space y demonimos de nacionalidad mas codigos de
 * marcado internacional desde restcountries.com. Proporciona inputs de autocompletado para pais (que tambien
 * establece el codigo de marcado y el conteo de digitos), ciudad (dependiente del pais seleccionado) y una
 * o mas nacionalidades con controles para agregar/quitar. El input de telefono usa un prefijo de codigo de
 * marcado y formatea los digitos segun el conteo esperado del pais. La fortaleza de la contrasena se muestra
 * en tiempo real con tres requisitos (longitud, mayuscula, digito). Antes del registro, el formulario llama a
 * POST /api/usuarios/verificar para verificar correo, username o pasaporte duplicados. Al pasar,
 * envia a POST /api/usuarios y redirige al login despues de 2 segundos.
 */
  import '../styles/Register.css';
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';

  /** Funcion usada para navegar entre paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** Objeto de datos del formulario de registro vinculado a todos los inputs del formulario. @type {{correo: string, contrasena: string, confirmPassword: string, pasaporte: string, username: string, nombre: string, apellido: string, telefono: string, fechaNacimiento: string, ciudad: string, pais: string}} */
  let registerData = {
    correo: '',
    contrasena: '',
    confirmPassword: '',
    pasaporte: '',
    username: '',
    nombre: '',
    apellido: '',
    telefono: '',
    fechaNacimiento: '',
    ciudad: '',
    pais: ''
  };

  /** True cuando el usuario ha marcado el checkbox de terminos y condiciones. @type {boolean} */
  let acceptTerms = false;

  /** True cuando el usuario ha marcado el checkbox de recibir promociones. @type {boolean} */
  let receivePromotions = false;

  /** Token generado por reCAPTCHA v2 tras completar el desafio. @type {string} */
  let captchaToken = '';

  /** ID del widget de reCAPTCHA para poder resetearlo. @type {number|null} */
  let recaptchaWidgetId = null;

  /** Clave publica de reCAPTCHA v2 (test key — siempre pasa). @type {string} */
  const RECAPTCHA_SITE_KEY = '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI';

  function renderCaptcha() {
    if (window.grecaptcha && document.getElementById('recaptcha-register')) {
      recaptchaWidgetId = window.grecaptcha.render('recaptcha-register', {
        sitekey: RECAPTCHA_SITE_KEY,
        callback: (token) => { captchaToken = token; },
        'expired-callback': () => { captchaToken = ''; },
        'error-callback':   () => { captchaToken = ''; }
      });
    }
  }

  function loadRecaptcha() {
    if (window.grecaptcha) { renderCaptcha(); return; }
    window.onRecaptchaLoad = renderCaptcha;
    const script = document.createElement('script');
    script.src = 'https://www.google.com/recaptcha/api.js?onload=onRecaptchaLoad&render=explicit';
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);
  }

  function resetCaptcha() {
    captchaToken = '';
    if (window.grecaptcha && recaptchaWidgetId !== null) {
      window.grecaptcha.reset(recaptchaWidgetId);
    }
  }

  /** Mensaje de error global de envio mostrado debajo del formulario. @type {string} */
  let submitError = '';

  /** True despues de que la llamada a la API de registro tiene exito, activa el mensaje de exito y la redireccion. @type {boolean} */
  let submitSuccess = false;

  /** True mientras la llamada a la API de registro esta en progreso. @type {boolean} */
  let submitting = false;

  /** Mensajes de error de validacion por campo, indexados por nombre de campo. @type {{correo: string, username: string, pasaporte: string, contrasena: string, pais: string, ciudad: string, nacionalidad: string, telefono: string}} */
  let errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '', telefono: '' };

  // Indicadores de fortaleza de contrasena calculados para el campo contrasena.
  $: ps = {
    length:    registerData.contrasena.length >= 8,
    uppercase: /[A-Z]/.test(registerData.contrasena),
    number:    /[0-9]/.test(registerData.contrasena)
  };

  // True cuando los tres requisitos de fortaleza de contrasena se cumplen.
  $: passwordValid = ps.length && ps.uppercase && ps.number;

  /** Todos los paises con sus listas de ciudades cargadas desde countriesnow.space. @type {Array<{country: string, cities: string[]}>} */
  let todosLosPaises = [];

  /** Texto actual en el input de autocompletado de pais. @type {string} */
  let paisQuery = '';

  /** Sugerencias de paises filtradas de todosLosPaises por paisQuery. @type {Array<object>} */
  let paisesSugeridos = [];

  /** El objeto de pais seleccionado de todosLosPaises, o null si aun no se ha seleccionado ninguno. @type {object|null} */
  let paisSeleccionado = null;

  /** Texto actual en el input de autocompletado de ciudad. @type {string} */
  let ciudadQuery = '';

  /** Sugerencias de nombre de ciudad filtradas de paisSeleccionado.cities por ciudadQuery. @type {string[]} */
  let ciudadesSugeridas = [];

  /** True una vez que el usuario ha clickeado una sugerencia de ciudad del dropdown. @type {boolean} */
  let ciudadSeleccionada = false;

  /** Arreglo de valores de texto de nacionalidad, uno por fila de nacionalidad (al menos uno). @type {string[]} */
  let nacionalidades = [''];

  /** Arreglo de arreglos de sugerencias para cada input de nacionalidad, indexado en paralelo a nacionalidades. @type {Array<Array<{pais: string, demonym: string}>>} */
  let sugerenciasNac = [[]];

  /** Todas las entradas de demonimos de nacionalidad cargadas desde restcountries.com. @type {Array<{pais: string, demonym: string}>} */
  let todosNacionalidades = [];

  /** Arreglo de booleanos que indican si cada nacionalidad ha sido seleccionada de las sugerencias. @type {boolean[]} */
  let nacionalidadesSeleccionadas = [false];

  /** Prefijo de codigo de marcado internacional para el pais seleccionado, por ejemplo '+502'. @type {string} */
  let dialCode = '';

  /** Mapa de nombre de pais (en minusculas) a codigo de marcado y conteo de digitos. @type {Object.<string, {code: string, digits: number}>} */
  let dialCodesMap = {};

  /** Numero de digitos locales requeridos para numeros de telefono en el pais seleccionado. @type {number} */
  let phoneDigitCount = 8;

  /**
   * Mapa de busqueda estatico de codigos de marcado internacional a conteos de digitos locales esperados.
   * Las claves son cadenas de codigo de marcado (por ejemplo '+502'), los valores son conteos de digitos.
   * @type {Object.<string, number>}
   */
  const knownDigits = {
    '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,
    '+31':9,'+32':9,'+33':9,'+34':9,'+36':9,
    '+39':10,'+40':9,'+41':9,'+43':10,'+44':10,
    '+45':8,'+46':9,'+47':8,'+48':9,'+49':10,
    '+51':9,'+52':10,'+53':8,'+54':10,'+55':11,
    '+56':9,'+57':10,'+58':10,'+60':9,'+61':9,
    '+62':9,'+63':10,'+64':9,'+65':8,'+66':9,
    '+81':10,'+82':10,'+84':9,'+86':11,'+90':10,
    '+91':10,'+92':10,'+93':9,'+94':9,'+95':8,
    '+98':10,'+212':9,'+213':9,'+216':8,'+218':9,
    '+220':7,'+221':9,'+222':8,'+223':8,'+224':9,
    '+225':8,'+226':8,'+227':8,'+228':8,'+229':8,
    '+230':8,'+231':8,'+232':8,'+233':9,'+234':10,
    '+235':8,'+236':8,'+237':9,'+238':7,'+239':7,
    '+240':9,'+241':8,'+242':9,'+243':9,'+244':9,
    '+245':7,'+246':7,'+247':4,'+248':7,'+249':9,
    '+250':9,'+251':9,'+252':8,'+253':8,'+254':9,
    '+255':9,'+256':9,'+257':8,'+258':9,'+260':9,
    '+261':9,'+262':9,'+263':9,'+264':9,'+265':9,
    '+266':8,'+267':8,'+268':8,'+269':7,'+290':4,
    '+291':7,'+297':7,'+298':6,'+299':6,'+350':8,
    '+351':9,'+352':9,'+353':9,'+354':7,'+355':9,
    '+356':8,'+357':8,'+358':9,'+359':9,'+370':8,
    '+371':8,'+372':8,'+373':8,'+374':8,'+375':9,
    '+376':6,'+377':8,'+378':10,'+380':9,'+381':9,
    '+382':8,'+385':9,'+386':8,'+387':8,'+389':8,
    '+420':9,'+421':9,'+423':7,'+500':5,'+501':7,
    '+502':8,'+503':8,'+504':8,'+505':8,'+506':8,
    '+507':8,'+508':6,'+509':8,'+590':9,'+591':8,
    '+592':7,'+593':9,'+594':9,'+595':9,'+596':9,
    '+597':7,'+598':8,'+599':7,'+670':8,'+672':6,
    '+673':7,'+674':7,'+675':8,'+676':7,'+677':7,
    '+678':7,'+679':7,'+680':7,'+681':6,'+682':5,
    '+683':4,'+685':7,'+686':8,'+687':6,'+688':5,
    '+689':8,'+690':4,'+691':7,'+692':7,'+850':10,
    '+852':8,'+853':8,'+855':9,'+856':10,'+880':10,
    '+886':9,'+960':7,'+961':8,'+962':9,'+963':9,
    '+964':10,'+965':8,'+966':9,'+967':9,'+968':8,
    '+970':9,'+971':9,'+972':9,'+973':8,'+974':8,
    '+975':8,'+976':8,'+977':10,'+992':9,'+993':8,
    '+994':9,'+995':9,'+996':9,'+998':9,
  };

  /**
   * Formatea una cadena de digitos sin procesar en un numero de telefono local legible usando
   * grupos separados por espacios cuyo tamano depende del conteo total de digitos esperado del pais.
   * @param {string} digits - Cadena de digitos sin procesar a formatear.
   * @param {number} total - Conteo total de digitos esperado para el pais.
   * @returns {string} Cadena de telefono formateada con espacios entre grupos de digitos.
   */
  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  /**
   * Maneja el evento de input del telefono eliminando los no-digitos, limitando a phoneDigitCount, formateando
   * con formatLocalPhone y asignando el resultado a registerData.telefono. Limpia errores.telefono.
   * @param {Event} e - El evento de input del campo de texto del telefono.
   */
  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '');
    const capped = raw.slice(0, phoneDigitCount);
    registerData.telefono = formatLocalPhone(capped, phoneDigitCount);
    errores.telefono = '';
  }

  /**
   * Genera una cadena de placeholder de telefono de muestra formateando una cadena de digito '5' repetido
   * para mostrar el formato esperado del pais actual.
   * @param {number} digits - Conteo total de digitos esperado para el pais.
   * @returns {string} Una cadena de placeholder formateada.
   */
  function getPhonePlaceholder(digits) {
    const sample = '5'.repeat(digits);
    return formatLocalPhone(sample, digits);
  }

  onMount(async () => {
    sessionStorage.clear();
    limpiarFormulario();
    loadRecaptcha();

    try {
      const res = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaises = data.data;
    } catch { console.error('Error cargando paises'); }

    try {
      const res = await fetch('https://restcountries.com/v3.1/all?fields=name,demonyms,idd');
      const data = await res.json();

      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1
            ? p.idd.root + suffixes[0]
            : p.idd.root;
          const digits = knownDigits[code] ?? 9;
          const key = p.name.common.toLowerCase();
          dialCodesMap[key] = { code, digits };
          if (p.name.official) dialCodesMap[p.name.official.toLowerCase()] = { code, digits };
        }
      });

      todosNacionalidades = data
        .filter(p => p.demonyms?.eng?.m)
        .map(p => ({ pais: p.name.common, demonym: p.demonyms.eng.m }))
        .sort((a, b) => a.pais.localeCompare(b.pais));
    } catch { console.error('Error cargando nacionalidades / dial codes'); }
  });

  /**
   * Reinicia todos los campos del formulario, mensajes de error, estado de autocompletado y filas de
   * nacionalidad a su estado vacio inicial. Se llama al montar para asegurar un formulario limpio aunque el componente se reutilice.
   */
  function limpiarFormulario() {
    registerData = {
      correo: '', contrasena: '', confirmPassword: '', pasaporte: '',
      username: '', nombre: '', apellido: '', telefono: '',
      fechaNacimiento: '', ciudad: '', pais: ''
    };
    acceptTerms = false;
    receivePromotions = false;
    captchaToken = '';
    submitError = '';
    submitSuccess = false;
    errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '', telefono: '' };
    paisQuery = '';
    ciudadQuery = '';
    paisSeleccionado = null;
    ciudadSeleccionada = false;
    nacionalidades = [''];
    nacionalidadesSeleccionadas = [false];
    sugerenciasNac = [[]];
    paisesSugeridos = [];
    ciudadesSugeridas = [];
    dialCode = '';
    phoneDigitCount = 8;
  }

  /**
   * Maneja el evento de input del correo convirtiendo el valor a minusculas antes de asignarlo.
   * @param {Event} e - El evento de input del campo de texto de correo.
   */
  function onCorreoInput(e) {
    registerData.correo = e.target.value.toLowerCase();
  }

  /**
   * Maneja el evento de input del pasaporte eliminando todos los caracteres no numericos del valor.
   * @param {Event} e - El evento de input del campo de texto del pasaporte.
   */
  function onPasaporteInput(e) {
    registerData.pasaporte = e.target.value.replace(/[^0-9]/g, '');
  }

  /**
   * Filtra todosLosPaises por el paisQuery actual (minimo 2 caracteres) para poblar
   * paisesSugeridos. Limpia registerData.pais si la consulta ha cambiado y no hay pais seleccionado.
   */
  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (paisQuery && !paisSeleccionado) {
      registerData.pais = '';
      errores.pais = '';
    }
  }

  /**
   * Establece el pais seleccionado, actualiza registerData.pais, reinicia el estado de ciudad y resuelve
   * el codigo de marcado y el conteo de digitos del pais seleccionado desde dialCodesMap.
   * @param {{country: string, cities: string[]}} p - El objeto de pais seleccionado.
   */
  function seleccionarPais(p) {
    paisSeleccionado = p;
    paisQuery = p.country;
    registerData.pais = p.country;
    paisesSugeridos = [];
    ciudadQuery = '';
    registerData.ciudad = '';
    ciudadesSugeridas = [];
    ciudadSeleccionada = false;
    errores.pais = '';

    const info = dialCodesMap[p.country.toLowerCase()];
    dialCode = info?.code ?? '';
    phoneDigitCount = info?.digits ?? 9;
    registerData.telefono = '';
    errores.telefono = '';
  }

  /**
   * Al perder el foco del input de pais, si se escribio texto pero no se selecciono ningun pais de la lista,
   * establece errores.pais y limpia paisQuery para forzar una seleccion valida.
   */
  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) {
      errores.pais = 'Debes seleccionar un pais de la lista';
      paisQuery = '';
    }
  }

  /**
   * Filtra la lista de ciudades del pais seleccionado por ciudadQuery (minimo 2 caracteres) para poblar
   * ciudadesSugeridas. Limpia registerData.ciudad si la consulta ha cambiado y no hay ciudad seleccionada.
   * No hace nada si aun no se ha seleccionado ningun pais.
   */
  function onCiudadInput() {
    if (!paisSeleccionado) return;
    const q = ciudadQuery.toLowerCase();
    ciudadesSugeridas = q.length < 2 ? [] : paisSeleccionado.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    if (ciudadQuery && !ciudadSeleccionada) {
      registerData.ciudad = '';
      errores.ciudad = '';
    }
  }

  /**
   * Asigna la cadena de ciudad seleccionada a ciudadQuery y registerData.ciudad, marca ciudadSeleccionada
   * como true, limpia ciudadesSugeridas y limpia errores.ciudad.
   * @param {string} c - El nombre de ciudad seleccionado del dropdown.
   */
  function seleccionarCiudad(c) {
    ciudadQuery = c;
    registerData.ciudad = c;
    ciudadesSugeridas = [];
    ciudadSeleccionada = true;
    errores.ciudad = '';
  }

  /**
   * Al perder el foco del input de ciudad, si se escribio texto pero no se selecciono ninguna ciudad de la lista,
   * establece errores.ciudad y limpia ciudadQuery para forzar una seleccion valida.
   */
  function validarCiudadSeleccionada() {
    if (ciudadQuery && !ciudadSeleccionada) {
      errores.ciudad = 'Debes seleccionar una ciudad de la lista';
      ciudadQuery = '';
    }
  }

  /**
   * Filtra todosNacionalidades por el texto en el indice i del arreglo nacionalidades (coincidiendo
   * nombre de pais o demonimo) para poblar sugerenciasNac[i]. Limpia errores.nacionalidad si
   * se escribe texto sin una seleccion confirmada.
   * @param {number} i - El indice de la fila de nacionalidad para filtrar sugerencias.
   */
  function onNacInput(i) {
    const q = nacionalidades[i].toLowerCase();
    sugerenciasNac[i] = q.length < 2 ? [] : todosNacionalidades
      .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q))
      .slice(0, 6);
    sugerenciasNac = [...sugerenciasNac];
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errores.nacionalidad = '';
    }
  }

  /**
   * Asigna la cadena de demonimo elegida a nacionalidades[i], la marca como seleccionada, limpia su
   * lista de sugerencias y limpia errores.nacionalidad.
   * @param {number} i - El indice de la fila de nacionalidad.
   * @param {string} demonym - La cadena de demonimo de la sugerencia seleccionada.
   */
  function seleccionarNac(i, demonym) {
    nacionalidades[i] = demonym;
    nacionalidades = [...nacionalidades];
    nacionalidadesSeleccionadas[i] = true;
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas];
    sugerenciasNac[i] = [];
    sugerenciasNac = [...sugerenciasNac];
    errores.nacionalidad = '';
  }

  /**
   * Al perder el foco de un input de nacionalidad, si se escribio texto en el indice i pero no se selecciono
   * ninguna sugerencia, establece errores.nacionalidad y limpia el texto en ese indice para forzar una seleccion valida.
   * @param {number} i - El indice de la fila de nacionalidad a validar.
   */
  function validarNacionalidadSeleccionada(i) {
    if (nacionalidades[i] && !nacionalidadesSeleccionadas[i]) {
      errores.nacionalidad = 'Debes seleccionar una nacionalidad de la lista';
      nacionalidades[i] = '';
      nacionalidades = [...nacionalidades];
    }
  }

  /**
   * Agrega una nueva fila de nacionalidad vacia con una lista de sugerencias vacia y una bandera
   * no seleccionada a los arreglos paralelos nacionalidades, sugerenciasNac y nacionalidadesSeleccionadas.
   */
  function agregarNac() {
    nacionalidades = [...nacionalidades, ''];
    sugerenciasNac = [...sugerenciasNac, []];
    nacionalidadesSeleccionadas = [...nacionalidadesSeleccionadas, false];
  }

  /**
   * Elimina la fila de nacionalidad en el indice i de los tres arreglos paralelos filtrando ese indice.
   * @param {number} i - El indice de la fila de nacionalidad a eliminar.
   */
  function quitarNac(i) {
    nacionalidades = nacionalidades.filter((_, idx) => idx !== i);
    sugerenciasNac = sugerenciasNac.filter((_, idx) => idx !== i);
    nacionalidadesSeleccionadas = nacionalidadesSeleccionadas.filter((_, idx) => idx !== i);
  }

  /**
   * Valida todos los campos del formulario, verifica duplicados de correo/username/pasaporte via
   * POST /api/usuarios/verificar, luego envia el registro a POST /api/usuarios.
   * Al tener exito establece submitSuccess y programa la navegacion a 'login' despues de 2 segundos.
   * En fallo de validacion o API establece los mensajes correspondientes de errores o submitError.
   * @async
   * @returns {Promise<void>}
   */
  async function handleRegister() {
    submitError = '';
    errores = { correo: '', username: '', pasaporte: '', contrasena: '', pais: '', ciudad: '', nacionalidad: '', telefono: '' };

    if (!passwordValid) { errores.contrasena = 'Minimo 8 caracteres, 1 mayuscula y 1 numero.'; return; }
    if (registerData.contrasena !== registerData.confirmPassword) { submitError = 'Las contrasenas no coinciden.'; return; }
    if (!acceptTerms) { submitError = 'Debes aceptar los terminos y condiciones.'; return; }
    if (!captchaToken) { submitError = 'Por favor completa la verificacion de seguridad.'; return; }

    if (!paisSeleccionado || !registerData.pais) {
      errores.pais = 'Debes seleccionar un pais de la lista.';
      return;
    }
    if (!ciudadSeleccionada || !registerData.ciudad) {
      errores.ciudad = 'Debes seleccionar una ciudad de la lista.';
      return;
    }

    if (dialCode) {
      const digitosIngresados = registerData.telefono.replace(/\D/g, '').length;
      if (digitosIngresados !== phoneDigitCount) {
        errores.telefono = `Se requieren ${phoneDigitCount} digitos para ${registerData.pais} (ingresaste ${digitosIngresados}).`;
        return;
      }
    } else if (!registerData.telefono.trim()) {
      errores.telefono = 'Ingresa tu numero de telefono.';
      return;
    }

    const nacionalidadesValidas = nacionalidades.filter((n, i) => n.trim() !== '' && nacionalidadesSeleccionadas[i]);
    if (nacionalidadesValidas.length === 0) {
      errores.nacionalidad = 'Debes seleccionar al menos una nacionalidad de la lista.';
      return;
    }

    submitting = true;

    const telefonoCompleto = dialCode
      ? dialCode + ' ' + registerData.telefono.replace(/\s/g, '')
      : registerData.telefono;

    const payload = {
      correo:          registerData.correo,
      contrasena:      registerData.contrasena,
      pasaporte:       registerData.pasaporte,
      username:        registerData.username,
      nombre:          registerData.nombre,
      apellido:        registerData.apellido,
      telefono:        telefonoCompleto,
      fechaNacimiento: registerData.fechaNacimiento,
      ciudad:          registerData.ciudad,
      pais:            registerData.pais,
      nacionalidades:  nacionalidadesValidas,
      recibirOfertas:  receivePromotions
    };

    try {
      const vRes = await fetch(`${API}/api/usuarios/verificar`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      const c = await vRes.json();

      if (c.correoExiste)    errores.correo    = 'Este correo ya esta registrado.';
      if (c.usernameExiste)  errores.username  = 'Este username ya esta en uso.';
      if (c.pasaporteExiste) errores.pasaporte = 'Este pasaporte ya esta registrado.';
      if (c.correoExiste || c.usernameExiste || c.pasaporteExiste) { submitting = false; return; }

      const res = await fetch(`${API}/api/usuarios`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error();

      submitSuccess = true;
      setTimeout(() => navigateTo('login'), 2000);
    } catch {
      submitError = 'No se pudo crear la cuenta. Intenta de nuevo.';
      resetCaptcha();
    } finally {
      submitting = false;
    }
  }
</script>

<!-- Contenedor principal de la pagina de registro con layout de dos paneles -->
<div class="register">
  <div class="register__container">
    <div class="register__content">

      <!-- Panel decorativo izquierdo con beneficios de la cuenta Broom AirLine -->
      <div class="register__image-section">
        <div class="register__image-overlay">
          <h2 class="register__image-title">Unete a Broom AirLine</h2>
          <p class="register__image-subtitle">Crea tu cuenta y empieza a disfrutar de vuelos increibles con ofertas exclusivas</p>
          <ul class="register__benefits">
            <li class="register__benefit">Acumula puntos en cada vuelo</li>
            <li class="register__benefit">Acceso a promociones exclusivas</li>
            <li class="register__benefit">Gestion facil de tus reservas</li>
            <li class="register__benefit">Soporte prioritario 24/7</li>
          </ul>
        </div>
      </div>

      <!-- Panel derecho con formulario de registro de nueva cuenta -->
      <div class="register__form-section">
        <div class="register__form-container">
          <button class="register__back" on:click={() => navigateTo('home')}>Volver al inicio</button>

          <!-- Encabezado del formulario con titulo y subtitulo -->
          <div class="register__header">
            <h1 class="register__title">Crear cuenta</h1>
            <p class="register__subtitle">Completa tus datos para registrarte</p>
          </div>

          <!-- Mensaje de exito post-registro o formulario de registro completo -->
          {#if submitSuccess}
            <div class="register-form__success">Cuenta creada exitosamente! Redirigiendo al login...</div>
          {:else}
            <form class="register-form" on:submit|preventDefault={handleRegister}>

              <!-- Campos de datos personales basicos del nuevo usuario -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="nombre" class="register-form__label">Nombre</label>
                  <input type="text" id="nombre" class="register-form__input" bind:value={registerData.nombre} placeholder="Tu nombre" autocomplete="off" required />
                </div>
                <div class="register-form__field">
                  <label for="apellido" class="register-form__label">Apellido</label>
                  <input type="text" id="apellido" class="register-form__input" bind:value={registerData.apellido} placeholder="Tu apellido" autocomplete="off" required />
                </div>
              </div>

              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="reg-username" class="register-form__label">Username</label>
                  <input type="text" id="reg-username" name="reg-username" class="register-form__input {errores.username ? 'register-form__input--error' : ''}" bind:value={registerData.username} placeholder="usuario123" autocomplete="new-password" required />
                  {#if errores.username}<span class="register-form__field-error">{errores.username}</span>{/if}
                </div>
                <div class="register-form__field">
                  <label for="fechaNacimiento" class="register-form__label">Fecha de nacimiento</label>
                  <input type="date" id="fechaNacimiento" class="register-form__input" bind:value={registerData.fechaNacimiento} autocomplete="off" required />
                </div>
              </div>

              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="reg-correo" class="register-form__label">Correo electronico</label>
                  <input type="email" id="reg-correo" name="reg-correo" class="register-form__input {errores.correo ? 'register-form__input--error' : ''}" value={registerData.correo} on:input={onCorreoInput} placeholder="correo@ejemplo.com" autocomplete="new-password" required />
                  {#if errores.correo}<span class="register-form__field-error">{errores.correo}</span>{/if}
                </div>
              </div>

              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="pasaporte" class="register-form__label">Pasaporte (solo numeros)</label>
                  <input type="text" id="pasaporte" class="register-form__input {errores.pasaporte ? 'register-form__input--error' : ''}" value={registerData.pasaporte} on:input={onPasaporteInput} placeholder="12345678" autocomplete="off" required />
                  {#if errores.pasaporte}<span class="register-form__field-error">{errores.pasaporte}</span>{/if}
                </div>
              </div>

              <!-- Autocomplete de pais con lista desplegable de sugerencias -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="paisInput" class="register-form__label">Pais</label>
                  <div class="autocomplete">
                    <input
                      type="text"
                      id="paisInput"
                      class="register-form__input {errores.pais ? 'register-form__input--error' : ''}"
                      bind:value={paisQuery}
                      on:input={onPaisInput}
                      on:blur={validarPaisSeleccionado}
                      placeholder="Escribe tu pais..."
                      autocomplete="off"
                    />
                    {#if paisesSugeridos.length > 0}
                      <ul class="autocomplete__list">
                        {#each paisesSugeridos as p}
                          <li class="autocomplete__item">
                            <button type="button" class="autocomplete__btn" on:click={() => seleccionarPais(p)}>{p.country}</button>
                          </li>
                        {/each}
                      </ul>
                    {/if}
                  </div>
                  {#if errores.pais}<span class="register-form__field-error">{errores.pais}</span>{/if}
                </div>
              </div>

              <!-- Campo de telefono con prefijo internacional determinado por el pais -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="telefono" class="register-form__label">
                    Telefono
                    {#if dialCode}
                      <span class="register-form__label-hint">— {phoneDigitCount} digitos requeridos</span>
                    {/if}
                  </label>
                  <div class="phone-field" class:phone-field--error={errores.telefono}>
                    {#if dialCode}
                      <span class="phone-field__prefix">{dialCode}</span>
                    {/if}
                    <input
                      type="tel"
                      id="telefono"
                      class="register-form__input"
                      bind:value={registerData.telefono}
                      on:input={onPhoneInput}
                      placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un pais primero'}
                      disabled={!dialCode}
                      autocomplete="off"
                    />
                  </div>
                  {#if registerData.telefono && !errores.telefono && dialCode}
                    {@const d = registerData.telefono.replace(/\D/g, '').length}
                    {#if d === phoneDigitCount}
                      <span class="register-form__field-ok">Numero completo</span>
                    {:else}
                      <span class="register-form__field-hint">{d}/{phoneDigitCount} digitos</span>
                    {/if}
                  {/if}
                  {#if errores.telefono}<span class="register-form__field-error">{errores.telefono}</span>{/if}
                </div>
              </div>

              <!-- Autocomplete de ciudad dependiente del pais seleccionado -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="ciudadInput" class="register-form__label">Ciudad</label>
                  <div class="autocomplete">
                    <input
                      type="text"
                      id="ciudadInput"
                      class="register-form__input {errores.ciudad ? 'register-form__input--error' : ''}"
                      bind:value={ciudadQuery}
                      on:input={onCiudadInput}
                      on:blur={validarCiudadSeleccionada}
                      placeholder={paisSeleccionado ? 'Escribe tu ciudad...' : 'Primero selecciona un pais'}
                      disabled={!paisSeleccionado}
                      autocomplete="off"
                    />
                    {#if ciudadesSugeridas.length > 0}
                      <ul class="autocomplete__list">
                        {#each ciudadesSugeridas as c}
                          <li class="autocomplete__item">
                            <button type="button" class="autocomplete__btn" on:click={() => seleccionarCiudad(c)}>{c}</button>
                          </li>
                        {/each}
                      </ul>
                    {/if}
                  </div>
                  {#if errores.ciudad}<span class="register-form__field-error">{errores.ciudad}</span>{/if}
                </div>
              </div>

              <!-- Campo de nacionalidades con soporte para multiples entradas y autocomplete -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <span class="register-form__label">Nacionalidad(es)</span>
                  <div class="nacionalidades-grid">
                    {#each nacionalidades as nac, i}
                      <div class="nacionalidad-item">
                        <span class="nacionalidad-number">Nacionalidad {i + 1}</span>
                        <div class="nacionalidad-input-wrapper">
                          <div class="autocomplete">
                            <input
                              type="text"
                              id="nac-{i}"
                              class="register-form__input"
                              bind:value={nacionalidades[i]}
                              on:input={() => onNacInput(i)}
                              on:blur={() => validarNacionalidadSeleccionada(i)}
                              placeholder="Ej: Guatemalteco"
                              autocomplete="off"
                            />
                            {#if sugerenciasNac[i]?.length > 0}
                              <ul class="autocomplete__list">
                                {#each sugerenciasNac[i] as s}
                                  <li class="autocomplete__item">
                                    <button type="button" class="autocomplete__btn" on:click={() => seleccionarNac(i, s.demonym)}>
                                      {s.pais} — {s.demonym}
                                    </button>
                                  </li>
                                {/each}
                              </ul>
                            {/if}
                          </div>
                          {#if i > 0}
                            <button type="button" class="nacionalidad-remove" on:click={() => quitarNac(i)}>✕</button>
                          {/if}
                        </div>
                      </div>
                    {/each}
                  </div>
                  <button type="button" class="nacionalidad-add" on:click={agregarNac}>Agregar otra nacionalidad</button>
                  {#if errores.nacionalidad}<span class="register-form__field-error">{errores.nacionalidad}</span>{/if}
                </div>
              </div>

              <!-- Campos de contrasena con indicadores de fortaleza en tiempo real -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="reg-contrasena" class="register-form__label">Contrasena</label>
                  <input type="password" id="reg-contrasena" name="reg-contrasena" class="register-form__input {errores.contrasena ? 'register-form__input--error' : ''}" bind:value={registerData.contrasena} placeholder="Minimo 8 caracteres" autocomplete="new-password" required />
                  {#if registerData.contrasena.length > 0}
                    <div class="password-strength">
                      <span class="password-strength__item" class:ok={ps.length}>{ps.length ? '✓' : '✗'} 8 caracteres minimo</span>
                      <span class="password-strength__item" class:ok={ps.uppercase}>{ps.uppercase ? '✓' : '✗'} 1 mayuscula</span>
                      <span class="password-strength__item" class:ok={ps.number}>{ps.number ? '✓' : '✗'} 1 numero</span>
                    </div>
                  {/if}
                  {#if errores.contrasena}<span class="register-form__field-error">{errores.contrasena}</span>{/if}
                </div>
                <div class="register-form__field">
                  <label for="reg-confirmPassword" class="register-form__label">Confirmar contrasena</label>
                  <input type="password" id="reg-confirmPassword" name="reg-confirmPassword" class="register-form__input" bind:value={registerData.confirmPassword} placeholder="Repite tu contrasena" autocomplete="new-password" required />
                  {#if registerData.confirmPassword.length > 0 && registerData.contrasena !== registerData.confirmPassword}
                    <span class="register-form__field-error">Las contrasenas no coinciden.</span>
                  {/if}
                </div>
              </div>

              <!-- Checkboxes de aceptacion de terminos y preferencia de promociones -->
              <div class="register-form__checkboxes">
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={acceptTerms} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">Acepto los terminos y condiciones y la politica de privacidad</span>
                </label>
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={receivePromotions} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">Deseo recibir promociones y ofertas por correo electronico</span>
                </label>
              </div>

              <!-- Widget de reCAPTCHA v2 para verificacion de seguridad -->
              <div class="register-form__captcha">
                <div id="recaptcha-register"></div>
              </div>

              <!-- Mensaje de error global y boton de envio del formulario -->
              {#if submitError}
                <div class="register-form__error">{submitError}</div>
              {/if}

              <button type="submit" class="register-form__submit" disabled={submitting || !captchaToken}>
                {submitting ? 'Verificando...' : 'Crear cuenta'}
              </button>

            </form>
          {/if}

          <!-- Enlace de navegacion para usuarios que ya tienen cuenta -->
          <div class="register__login">
            <p class="register__login-text">
              Ya tienes una cuenta?
              <button type="button" class="register__login-link" on:click={() => navigateTo('login')}>Inicia sesion</button>
            </p>
          </div>

        </div>
      </div>
    </div>
  </div>
</div>
