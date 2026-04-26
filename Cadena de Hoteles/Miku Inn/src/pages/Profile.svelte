<script>
  /**
   * @file Profile.svelte
   * @description Página de perfil del usuario. Muestra toda la información
   * de la cuenta y permite editar todos los campos: datos personales,
   * credenciales, ubicación, nacionalidades, teléfono, contraseña y
   * preferencias de ofertas.
   */

  export let navigateTo;
  import '../styles/profile.css';
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';

  let perfil       = null;
  let loading      = true;
  let serverError  = '';

  // ── Secciones editables (toggle) ─────────────────────────────────────────
  let editPersonal     = false;
  let editCredenciales = false;
  let editUbicacion    = false;
  let editNacs         = false;
  let editPreferencias = false;

  // ── Datos personales ──────────────────────────────────────────────────────
  let epNombre    = '';
  let epApellido  = '';
  let epFecha     = '';
  let savingPersonal = false;
  let personalMsg  = '';
  let personalErr  = '';

  function abrirEditPersonal() {
    epNombre   = perfil.nombre;
    epApellido = perfil.apellido;
    epFecha    = perfil.fechaNacimiento ?? '';
    personalMsg = ''; personalErr = '';
    editPersonal = true;
  }

  async function guardarPersonal() {
    personalMsg = ''; personalErr = '';
    if (!epNombre.trim()) { personalErr = 'El nombre es requerido.'; return; }
    if (!epApellido.trim()) { personalErr = 'El apellido es requerido.'; return; }
    if (!epFecha) { personalErr = 'La fecha de nacimiento es requerida.'; return; }
    savingPersonal = true;
    try {
      const r = await fetch(`${API}/usuarios/datos-personales`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: epNombre.trim(), apellido: epApellido.trim(), fechaNacimiento: epFecha })
      });
      const d = await r.json();
      if (r.ok) {
        personalMsg = d.mensaje ?? 'Datos actualizados.';
        perfil.nombre = epNombre.trim();
        perfil.apellido = epApellido.trim();
        perfil.fechaNacimiento = epFecha;
        editPersonal = false;
      } else { personalErr = d.mensaje ?? 'Error al actualizar.'; }
    } catch { personalErr = 'Error de conexión.'; }
    finally { savingPersonal = false; }
  }

  // ── Credenciales ──────────────────────────────────────────────────────────
  let ecUsername  = '';
  let ecCorreo    = '';
  let ecPasaporte = '';
  let savingCred  = false;
  let credMsg     = '';
  let credErr     = '';

  function abrirEditCred() {
    ecUsername  = perfil.username;
    ecCorreo    = perfil.correo;
    ecPasaporte = perfil.pasaporte ?? '';
    credMsg = ''; credErr = '';
    editCredenciales = true;
  }

  async function guardarCredenciales() {
    credMsg = ''; credErr = '';
    if (!ecUsername.trim()) { credErr = 'El username es requerido.'; return; }
    if (!ecCorreo.trim())   { credErr = 'El correo es requerido.'; return; }
    savingCred = true;
    try {
      const r = await fetch(`${API}/usuarios/credenciales`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: ecUsername.trim(), correo: ecCorreo.trim(), pasaporte: ecPasaporte.trim() || null })
      });
      const d = await r.json();
      if (r.ok) {
        credMsg = d.mensaje ?? 'Credenciales actualizadas.';
        perfil.username  = ecUsername.trim();
        perfil.correo    = ecCorreo.trim();
        perfil.pasaporte = ecPasaporte.trim() || perfil.pasaporte;
        editCredenciales = false;
      } else if (r.status === 409 && d.campos) {
        if (d.campos.usernameExiste)  credErr = 'Ese nombre de usuario ya está en uso.';
        else if (d.campos.correoExiste)    credErr = 'Ese correo ya está registrado.';
        else if (d.campos.pasaporteExiste) credErr = 'Ese pasaporte ya está registrado.';
        else credErr = 'Algunos campos ya existen.';
      } else { credErr = d.mensaje ?? 'Error al actualizar.'; }
    } catch { credErr = 'Error de conexión.'; }
    finally { savingCred = false; }
  }

  // ── Ubicación (país / ciudad) ─────────────────────────────────────────────
  let euPaisQuery       = '';
  let euPaisesSugeridos = [];
  let euPaisSeleccionado = null;
  let euCiudadQuery      = '';
  let euCiudadesSugeridas = [];
  let euCiudadSeleccionada = false;
  let todosLosPaisesUbic = [];
  let savingUbicacion    = false;
  let ubicMsg            = '';
  let ubicErr            = '';

  function abrirEditUbicacion() {
    euPaisQuery = perfil.pais ?? '';
    euPaisSeleccionado = { country: perfil.pais, cities: [] };
    euCiudadQuery = perfil.ciudad ?? '';
    euCiudadSeleccionada = true;
    ubicMsg = ''; ubicErr = '';
    editUbicacion = true;
  }

  function onEuPaisInput() {
    const q = euPaisQuery.toLowerCase();
    euPaisesSugeridos = q.length < 2 ? [] : todosLosPaisesUbic.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (!euPaisSeleccionado) { euCiudadQuery = ''; euCiudadSeleccionada = false; }
  }

  function seleccionarEuPais(p) {
    euPaisSeleccionado = p; euPaisQuery = p.country;
    euPaisesSugeridos = []; euCiudadQuery = ''; euCiudadesSugeridas = []; euCiudadSeleccionada = false;
    ubicErr = '';
  }

  function validarEuPais() {
    if (euPaisQuery && !euPaisSeleccionado) { ubicErr = 'Selecciona un país de la lista'; euPaisQuery = ''; }
  }

  function onEuCiudadInput() {
    if (!euPaisSeleccionado) return;
    const q = euCiudadQuery.toLowerCase();
    euCiudadesSugeridas = q.length < 2 ? [] : (euPaisSeleccionado.cities || []).filter(c => c.toLowerCase().includes(q)).slice(0, 6);
    euCiudadSeleccionada = false;
  }

  function seleccionarEuCiudad(c) {
    euCiudadQuery = c; euCiudadSeleccionada = true;
    euCiudadesSugeridas = []; ubicErr = '';
  }

  function validarEuCiudad() {
    if (euCiudadQuery && !euCiudadSeleccionada) { ubicErr = 'Selecciona una ciudad de la lista'; euCiudadQuery = ''; }
  }

  async function guardarUbicacion() {
    ubicMsg = ''; ubicErr = '';
    if (!euPaisSeleccionado) { ubicErr = 'Selecciona un país.'; return; }
    if (!euCiudadQuery.trim() || !euCiudadSeleccionada) { ubicErr = 'Selecciona una ciudad.'; return; }
    savingUbicacion = true;
    try {
      const r = await fetch(`${API}/usuarios/ciudad`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ pais: euPaisSeleccionado.country, ciudad: euCiudadQuery.trim() })
      });
      const d = await r.json();
      if (r.ok) {
        ubicMsg = d.mensaje ?? 'Ubicación actualizada.';
        perfil.pais = euPaisSeleccionado.country;
        perfil.ciudad = euCiudadQuery.trim();
        editUbicacion = false;
      } else { ubicErr = d.mensaje ?? 'Error al actualizar.'; }
    } catch { ubicErr = 'Error de conexión.'; }
    finally { savingUbicacion = false; }
  }

  // ── Nacionalidades ────────────────────────────────────────────────────────
  let enNacs            = [''];
  let enSugerencias     = [[]];
  let enSeleccionadas   = [false];
  let todosNacionalidades = [];
  let savingNacs        = false;
  let nacsMsg           = '';
  let nacsErr           = '';

  function abrirEditNacs() {
    enNacs          = perfil.nacionalidades.length ? [...perfil.nacionalidades] : [''];
    enSugerencias   = enNacs.map(() => []);
    enSeleccionadas = enNacs.map(() => true);
    nacsMsg = ''; nacsErr = '';
    editNacs = true;
  }

  function onEnNacInput(i) {
    const q = enNacs[i].toLowerCase();
    enSugerencias[i] = q.length < 2 ? [] : todosNacionalidades
      .filter(n => n.pais.toLowerCase().includes(q) || n.demonym.toLowerCase().includes(q)).slice(0, 6);
    enSugerencias = [...enSugerencias];
    enSeleccionadas[i] = false; enSeleccionadas = [...enSeleccionadas];
  }

  function seleccionarEnNac(i, demonym) {
    enNacs[i] = demonym; enNacs = [...enNacs];
    enSeleccionadas[i] = true; enSeleccionadas = [...enSeleccionadas];
    enSugerencias[i] = []; enSugerencias = [...enSugerencias];
    nacsErr = '';
  }

  function validarEnNac(i) {
    if (enNacs[i] && !enSeleccionadas[i]) { nacsErr = 'Selecciona la nacionalidad de la lista'; enNacs[i] = ''; enNacs = [...enNacs]; }
  }

  function agregarEnNac() {
    enNacs = [...enNacs, '']; enSugerencias = [...enSugerencias, []]; enSeleccionadas = [...enSeleccionadas, false];
  }

  function quitarEnNac(i) {
    enNacs          = enNacs.filter((_, idx) => idx !== i);
    enSugerencias   = enSugerencias.filter((_, idx) => idx !== i);
    enSeleccionadas = enSeleccionadas.filter((_, idx) => idx !== i);
  }

  async function guardarNacs() {
    nacsMsg = ''; nacsErr = '';
    const validas = enNacs.filter((n, i) => n.trim() && enSeleccionadas[i]);
    if (!validas.length) { nacsErr = 'Agrega al menos una nacionalidad.'; return; }
    savingNacs = true;
    try {
      const r = await fetch(`${API}/usuarios/nacionalidades`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nacionalidades: validas })
      });
      const d = await r.json();
      if (r.ok) {
        nacsMsg = d.mensaje ?? 'Nacionalidades actualizadas.';
        perfil.nacionalidades = validas;
        editNacs = false;
      } else { nacsErr = d.mensaje ?? 'Error al actualizar.'; }
    } catch { nacsErr = 'Error de conexión.'; }
    finally { savingNacs = false; }
  }

  // ── Teléfono ──────────────────────────────────────────────────────────────
  let nuevoTelefono   = '';
  let telefonoMsg     = '';
  let telefonoError   = '';
  let savingTelefono  = false;
  let paisQuery       = '';
  let paisesSugeridos = [];
  let todosLosPaises  = [];
  let dialCodesMap    = {};
  let dialCode        = '';
  let phoneDigitCount = 9;
  let paisSeleccionado = null;

  const knownDigits = {
    '+1':10,'+7':10,'+20':10,'+27':9,'+30':10,'+31':9,'+32':9,'+33':9,'+34':9,'+36':9,
    '+39':10,'+40':9,'+41':9,'+43':10,'+44':10,'+45':8,'+46':9,'+47':8,'+48':9,'+49':10,
    '+51':9,'+52':10,'+53':8,'+54':10,'+55':11,'+56':9,'+57':10,'+58':10,'+60':9,'+61':9,
    '+62':9,'+63':10,'+64':9,'+65':8,'+66':9,'+81':10,'+82':10,'+84':9,'+86':11,'+90':10,
    '+91':10,'+92':10,'+93':9,'+94':9,'+95':8,'+98':10,'+212':9,'+213':9,'+216':8,'+218':9,
    '+220':7,'+221':9,'+222':8,'+223':8,'+224':9,'+225':8,'+226':8,'+227':8,'+228':8,'+229':8,
    '+230':8,'+231':8,'+232':8,'+233':9,'+234':10,'+235':8,'+236':8,'+237':9,'+238':7,'+239':7,
    '+240':9,'+241':8,'+242':9,'+243':9,'+244':9,'+245':7,'+246':7,'+247':4,'+248':7,'+249':9,
    '+250':9,'+251':9,'+252':8,'+253':8,'+254':9,'+255':9,'+256':9,'+257':8,'+258':9,'+260':9,
    '+261':9,'+262':9,'+263':9,'+264':9,'+265':9,'+266':8,'+267':8,'+268':8,'+269':7,'+290':4,
    '+291':7,'+297':7,'+298':6,'+299':6,'+350':8,'+351':9,'+352':9,'+353':9,'+354':7,'+355':9,
    '+356':8,'+357':8,'+358':9,'+359':9,'+370':8,'+371':8,'+372':8,'+373':8,'+374':8,'+375':9,
    '+376':6,'+377':8,'+378':10,'+380':9,'+381':9,'+382':8,'+385':9,'+386':8,'+387':8,'+389':8,
    '+420':9,'+421':9,'+423':7,'+500':5,'+501':7,'+502':8,'+503':8,'+504':8,'+505':8,'+506':8,
    '+507':8,'+508':6,'+509':8,'+590':9,'+591':8,'+592':7,'+593':9,'+594':9,'+595':9,'+596':9,
    '+597':7,'+598':8,'+599':7,'+670':8,'+672':6,'+673':7,'+674':7,'+675':8,'+676':7,'+677':7,
    '+678':7,'+679':7,'+680':7,'+681':6,'+682':5,'+683':4,'+685':7,'+686':8,'+687':6,'+688':5,
    '+689':8,'+690':4,'+691':7,'+692':7,'+850':10,'+852':8,'+853':8,'+855':9,'+856':10,'+880':10,
    '+886':9,'+960':7,'+961':8,'+962':9,'+963':9,'+964':10,'+965':8,'+966':9,'+967':9,'+968':8,
    '+970':9,'+971':9,'+972':9,'+973':8,'+974':8,'+975':8,'+976':8,'+977':10,'+992':9,'+993':8,
    '+994':9,'+995':9,'+996':9,'+998':9,
  };

  function formatLocalPhone(digits, total) {
    if (total <= 7)  return digits.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim();
    if (total === 8) return digits.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim();
    if (total === 9) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim();
    if (total === 10) return digits.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim();
    return digits.replace(/^(\d{2})(\d{0,4})(\d{0,5})/, '$1 $2 $3').trim();
  }

  function onPhoneInput(e) {
    const raw = e.target.value.replace(/\D/g, '');
    nuevoTelefono = formatLocalPhone(raw.slice(0, phoneDigitCount), phoneDigitCount);
  }

  function getPhonePlaceholder(digits) { return formatLocalPhone('5'.repeat(digits), digits); }

  function onPaisInput() {
    const q = paisQuery.toLowerCase();
    paisesSugeridos = q.length < 2 ? [] : todosLosPaises.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
    if (!paisSeleccionado) { dialCode = ''; nuevoTelefono = ''; }
  }

  function seleccionarPais(p) {
    paisSeleccionado = p.country; paisQuery = p.country; paisesSugeridos = [];
    const info = dialCodesMap[p.country.toLowerCase()];
    dialCode = info?.code ?? ''; phoneDigitCount = info?.digits ?? 9; nuevoTelefono = ''; telefonoError = '';
  }

  function validarPaisSeleccionado() {
    if (paisQuery && !paisSeleccionado) { telefonoError = 'Selecciona un país de la lista'; paisQuery = ''; }
  }

  async function guardarTelefono() {
    telefonoMsg = ''; telefonoError = '';
    if (!paisSeleccionado) { telefonoError = 'Selecciona un país primero.'; return; }
    if (!nuevoTelefono.trim()) { telefonoError = 'Ingresa un número válido.'; return; }
    if (nuevoTelefono.replace(/\D/g, '').length !== phoneDigitCount) {
      telefonoError = `El número debe tener exactamente ${phoneDigitCount} dígitos.`; return;
    }
    savingTelefono = true;
    const telefonoCompleto = dialCode + ' ' + nuevoTelefono.replace(/\s/g, '');
    try {
      const r = await fetch(`${API}/usuarios/telefono`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ telefono: telefonoCompleto })
      });
      const d = await r.json();
      if (r.ok) { telefonoMsg = d.mensaje ?? 'Teléfono actualizado.'; perfil.telefono = telefonoCompleto; }
      else { telefonoError = d.mensaje ?? 'Error al actualizar.'; }
    } catch { telefonoError = 'Error de conexión.'; }
    finally { savingTelefono = false; }
  }

  // ── Contraseña ────────────────────────────────────────────────────────────
  let contrasenaActual = '';
  let contrasenaNueva  = '';
  let confirmarNueva   = '';
  let showActual = false;
  let showNueva  = false;
  let showConfirmar = false;
  let contrasenaMsg   = '';
  let contrasenaError = '';
  let savingContrasena = false;

  async function guardarContrasena() {
    contrasenaMsg = ''; contrasenaError = '';
    if (!contrasenaActual) { contrasenaError = 'Ingresa tu contraseña actual.'; return; }
    if (contrasenaNueva.length < 8) { contrasenaError = 'Mínimo 8 caracteres.'; return; }
    if (contrasenaNueva !== confirmarNueva) { contrasenaError = 'Las contraseñas no coinciden.'; return; }
    savingContrasena = true;
    try {
      const r = await fetch(`${API}/usuarios/contrasena`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ contrasenaActual, contrasenaNueva })
      });
      const d = await r.json();
      if (r.ok) {
        contrasenaMsg = d.mensaje ?? 'Contraseña actualizada.';
        contrasenaActual = ''; contrasenaNueva = ''; confirmarNueva = '';
      } else { contrasenaError = d.mensaje ?? 'Error al actualizar.'; }
    } catch { contrasenaError = 'Error de conexión.'; }
    finally { savingContrasena = false; }
  }

  // ── Preferencias de ofertas ───────────────────────────────────────────────
  let prefData        = { tiposHabitacion: [], presupuesto: 'Estándar', combinacion: 'Una habitación', personasExtra: 1 };
  let recibirOfertas  = false;
  let savingPref      = false;
  let prefMsg         = '';
  let prefErr         = '';
  const tipoHabOpciones = ['Doble', 'Junior Suite', 'Suite', 'Gran Suite'];

  function togglePrefTipoHab(tipo) {
    if (prefData.tiposHabitacion.includes(tipo))
      prefData.tiposHabitacion = prefData.tiposHabitacion.filter(t => t !== tipo);
    else
      prefData.tiposHabitacion = [...prefData.tiposHabitacion, tipo];
    prefData = { ...prefData };
  }

  function abrirEditPref() {
    if (perfil.preferenciasOferta) {
      try {
        const p = JSON.parse(perfil.preferenciasOferta);
        prefData = { tiposHabitacion: p.tiposHabitacion ?? [], presupuesto: p.presupuesto ?? 'Estándar',
                     combinacion: p.combinacion ?? 'Una habitación', personasExtra: p.personasExtra ?? 1 };
        recibirOfertas = true;
      } catch { recibirOfertas = false; }
    } else { recibirOfertas = false; }
    prefMsg = ''; prefErr = '';
    editPreferencias = true;
  }

  async function guardarPreferencias() {
    prefMsg = ''; prefErr = '';
    savingPref = true;
    const payload = recibirOfertas ? JSON.stringify(prefData) : null;
    try {
      const r = await fetch(`${API}/usuarios/preferencias`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ preferenciasOferta: payload })
      });
      const d = await r.json();
      if (r.ok) {
        prefMsg = d.mensaje ?? 'Preferencias guardadas.';
        perfil.preferenciasOferta = payload;
        editPreferencias = false;
      } else { prefErr = d.mensaje ?? 'Error al guardar.'; }
    } catch { prefErr = 'Error de conexión.'; }
    finally { savingPref = false; }
  }

  // ── onMount ───────────────────────────────────────────────────────────────
  onMount(async () => {
    try {
      const r = await fetch(`${API}/usuarios/perfil`, { credentials: 'include' });
      if (!r.ok) { serverError = 'No se pudo cargar el perfil.'; loading = false; return; }
      perfil = await r.json();
    } catch { serverError = 'Error de conexión.'; }
    finally { loading = false; }

    try {
      const r = await fetch('https://countriesnow.space/api/v0.1/countries');
      const d = await r.json();
      todosLosPaises = d.data;
      todosLosPaisesUbic = d.data;
    } catch {}

    try {
      const r = await fetch('https://restcountries.com/v3.1/all?fields=name,demonyms,idd');
      const data = await r.json();
      data.forEach(p => {
        if (p.idd?.root) {
          const suffixes = p.idd.suffixes ?? [''];
          const code = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root;
          const digits = knownDigits[code] ?? 9;
          dialCodesMap[p.name.common.toLowerCase()] = { code, digits };
          if (p.name.official) dialCodesMap[p.name.official.toLowerCase()] = { code, digits };
        }
      });

      todosNacionalidades = data
        .filter(p => p.demonyms?.eng?.m)
        .map(p => ({ pais: p.name.common, demonym: p.demonyms.eng.m }))
        .sort((a, b) => a.pais.localeCompare(b.pais));

      if (perfil?.pais) {
        const info = dialCodesMap[perfil.pais.toLowerCase()];
        if (info) {
          dialCode = info.code; phoneDigitCount = info.digits;
          paisQuery = perfil.pais; paisSeleccionado = perfil.pais;
          if (perfil.telefono) {
            const sinCodigo = perfil.telefono.replace(info.code, '').trim();
            nuevoTelefono = formatLocalPhone(sinCodigo.replace(/\D/g, '').slice(0, info.digits), info.digits);
          }
        }
      }
    } catch {}
  });

  // ── Helpers ───────────────────────────────────────────────────────────────
  function parsePrefs(json) {
    try { return JSON.parse(json); } catch { return null; }
  }
</script>

<div class="profile-page">
  <div class="profile-container">

    {#if loading}
      <div class="profile-loading">
        <div class="profile-spinner"></div>
        <p>Cargando perfil...</p>
      </div>

    {:else if serverError}
      <div class="profile-error-box">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p>{serverError}</p>
        <button class="profile-btn-primary" on:click={() => navigateTo('home')}>Volver al inicio</button>
      </div>

    {:else if perfil}

      <!-- Hero -->
      <div class="profile-hero">
        <div class="profile-avatar-big">{perfil.nombre.charAt(0).toUpperCase()}</div>
        <div class="profile-hero-info">
          <h1 class="profile-nombre">{perfil.nombre} {perfil.apellido}</h1>
          <p class="profile-username">@{perfil.username}</p>
          <div class="profile-tags">
            <span class="profile-tag">{perfil.pais}</span>
            <span class="profile-tag">{perfil.ciudad}</span>
            {#each perfil.nacionalidades as nac}
              <span class="profile-tag nac">{nac}</span>
            {/each}
          </div>
        </div>
      </div>

      <!-- ── Datos personales ─────────────────────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-header">
          <h2 class="profile-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            Datos personales
          </h2>
          {#if !editPersonal}
            <button class="profile-edit-btn" on:click={abrirEditPersonal}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
          {/if}
        </div>

        {#if personalMsg}<div class="profile-alert success">{personalMsg}</div>{/if}

        {#if editPersonal}
          {#if personalErr}<div class="profile-alert error">{personalErr}</div>{/if}
          <div class="profile-edit-grid">
            <div class="profile-field">
              <label for="ep-nombre">Nombre</label>
              <input type="text" id="ep-nombre" bind:value={epNombre} placeholder="Tu nombre" />
            </div>
            <div class="profile-field">
              <label for="ep-apellido">Apellidos</label>
              <input type="text" id="ep-apellido" bind:value={epApellido} placeholder="Tus apellidos" />
            </div>
          </div>
          <div class="profile-field">
            <label for="ep-fecha">Fecha de nacimiento</label>
            <input type="date" id="ep-fecha" bind:value={epFecha}
              max={new Date(new Date().setFullYear(new Date().getFullYear()-18)).toISOString().split('T')[0]} />
          </div>
          <div class="profile-edit-actions">
            <button class="profile-btn-primary" on:click={guardarPersonal} disabled={savingPersonal}>
              {savingPersonal ? 'Guardando...' : 'Guardar cambios'}
            </button>
            <button class="profile-btn-secondary" on:click={() => editPersonal = false}>Cancelar</button>
          </div>
        {:else}
          <div class="profile-info-grid">
            <div class="profile-info-item">
              <span class="profile-info-label">Nombre</span>
              <span class="profile-info-value">{perfil.nombre}</span>
            </div>
            <div class="profile-info-item">
              <span class="profile-info-label">Apellidos</span>
              <span class="profile-info-value">{perfil.apellido}</span>
            </div>
            <div class="profile-info-item">
              <span class="profile-info-label">Fecha de nacimiento</span>
              <span class="profile-info-value">{perfil.fechaNacimiento ?? '—'}</span>
            </div>
          </div>
        {/if}
      </div>

      <!-- ── Credenciales ─────────────────────────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-header">
          <h2 class="profile-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            Credenciales
          </h2>
          {#if !editCredenciales}
            <button class="profile-edit-btn" on:click={abrirEditCred}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
          {/if}
        </div>

        {#if credMsg}<div class="profile-alert success">{credMsg}</div>{/if}

        {#if editCredenciales}
          {#if credErr}<div class="profile-alert error">{credErr}</div>{/if}
          <div class="profile-field">
            <label for="ec-username">Nombre de usuario</label>
            <input type="text" id="ec-username" bind:value={ecUsername}
              on:input={() => ecUsername = ecUsername.replace(/[^a-zA-Z0-9_.]/g, '')}
              placeholder="Ej: Pine2" />
          </div>
          <div class="profile-field">
            <label for="ec-correo">Correo electrónico</label>
            <input type="email" id="ec-correo" bind:value={ecCorreo} placeholder="tu@email.com" />
          </div>
          <div class="profile-field">
            <label for="ec-pasaporte">Pasaporte</label>
            <input type="text" id="ec-pasaporte" bind:value={ecPasaporte}
              on:input={() => ecPasaporte = ecPasaporte.toUpperCase()}
              placeholder="AB123456" style="text-transform:uppercase" />
          </div>
          <div class="profile-edit-actions">
            <button class="profile-btn-primary" on:click={guardarCredenciales} disabled={savingCred}>
              {savingCred ? 'Guardando...' : 'Guardar cambios'}
            </button>
            <button class="profile-btn-secondary" on:click={() => editCredenciales = false}>Cancelar</button>
          </div>
        {:else}
          <div class="profile-info-grid">
            <div class="profile-info-item">
              <span class="profile-info-label">Username</span>
              <span class="profile-info-value">@{perfil.username}</span>
            </div>
            <div class="profile-info-item">
              <span class="profile-info-label">Correo</span>
              <span class="profile-info-value">{perfil.correo}</span>
            </div>
            <div class="profile-info-item">
              <span class="profile-info-label">Pasaporte</span>
              <span class="profile-info-value">{perfil.pasaporte ?? '—'}</span>
            </div>
          </div>
        {/if}
      </div>

      <!-- ── Ubicación ────────────────────────────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-header">
          <h2 class="profile-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
            Ubicación
          </h2>
          {#if !editUbicacion}
            <button class="profile-edit-btn" on:click={abrirEditUbicacion}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
          {/if}
        </div>

        {#if ubicMsg}<div class="profile-alert success">{ubicMsg}</div>{/if}

        {#if editUbicacion}
          {#if ubicErr}<div class="profile-alert error">{ubicErr}</div>{/if}
          <div class="profile-field">
            <label for="eu-pais">País</label>
            <div class="profile-autocomplete-wrap">
              <input type="text" id="eu-pais" bind:value={euPaisQuery}
                on:input={onEuPaisInput} on:blur={validarEuPais}
                placeholder="Escribe tu país..." autocomplete="off" />
              {#if euPaisesSugeridos.length > 0}
                <ul class="profile-autocomplete-list">
                  {#each euPaisesSugeridos as p}
                    <li><button type="button" class="profile-autocomplete-btn" on:click={() => seleccionarEuPais(p)}>{p.country}</button></li>
                  {/each}
                </ul>
              {/if}
            </div>
          </div>
          <div class="profile-field">
            <label for="eu-ciudad">Ciudad</label>
            <div class="profile-autocomplete-wrap">
              <input type="text" id="eu-ciudad" bind:value={euCiudadQuery}
                on:input={onEuCiudadInput} on:blur={validarEuCiudad}
                placeholder={euPaisSeleccionado ? 'Escribe tu ciudad...' : 'Selecciona un país primero'}
                disabled={!euPaisSeleccionado} autocomplete="off" />
              {#if euCiudadesSugeridas.length > 0}
                <ul class="profile-autocomplete-list">
                  {#each euCiudadesSugeridas as c}
                    <li><button type="button" class="profile-autocomplete-btn" on:click={() => seleccionarEuCiudad(c)}>{c}</button></li>
                  {/each}
                </ul>
              {/if}
            </div>
          </div>
          <div class="profile-edit-actions">
            <button class="profile-btn-primary" on:click={guardarUbicacion} disabled={savingUbicacion}>
              {savingUbicacion ? 'Guardando...' : 'Guardar cambios'}
            </button>
            <button class="profile-btn-secondary" on:click={() => editUbicacion = false}>Cancelar</button>
          </div>
        {:else}
          <div class="profile-info-grid">
            <div class="profile-info-item">
              <span class="profile-info-label">País</span>
              <span class="profile-info-value">{perfil.pais}</span>
            </div>
            <div class="profile-info-item">
              <span class="profile-info-label">Ciudad</span>
              <span class="profile-info-value">{perfil.ciudad}</span>
            </div>
          </div>
        {/if}
      </div>

      <!-- ── Nacionalidades ───────────────────────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-header">
          <h2 class="profile-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/>
              <path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z"/>
            </svg>
            Nacionalidades
          </h2>
          {#if !editNacs}
            <button class="profile-edit-btn" on:click={abrirEditNacs}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
          {/if}
        </div>

        {#if nacsMsg}<div class="profile-alert success">{nacsMsg}</div>{/if}

        {#if editNacs}
          {#if nacsErr}<div class="profile-alert error">{nacsErr}</div>{/if}
          {#each enNacs as _n, i}
            <div class="profile-nac-row">
              <div class="profile-autocomplete-wrap" style="flex:1">
                <input type="text" id="en-nac-{i}" bind:value={enNacs[i]}
                  on:input={() => onEnNacInput(i)} on:blur={() => validarEnNac(i)}
                  placeholder="Ej: Guatemalan" autocomplete="off" />
                {#if enSugerencias[i]?.length > 0}
                  <ul class="profile-autocomplete-list">
                    {#each enSugerencias[i] as s}
                      <li><button type="button" class="profile-autocomplete-btn" on:click={() => seleccionarEnNac(i, s.demonym)}>
                        {s.pais} — {s.demonym}
                      </button></li>
                    {/each}
                  </ul>
                {/if}
              </div>
              {#if i > 0}
                <button type="button" class="profile-btn-quitar" on:click={() => quitarEnNac(i)}>✕</button>
              {/if}
            </div>
          {/each}
          <button type="button" class="profile-link-btn" on:click={agregarEnNac}>+ Agregar otra</button>
          <div class="profile-edit-actions">
            <button class="profile-btn-primary" on:click={guardarNacs} disabled={savingNacs}>
              {savingNacs ? 'Guardando...' : 'Guardar cambios'}
            </button>
            <button class="profile-btn-secondary" on:click={() => editNacs = false}>Cancelar</button>
          </div>
        {:else}
          <div class="profile-nac-tags">
            {#each perfil.nacionalidades as nac}
              <span class="profile-tag nac">{nac}</span>
            {/each}
            {#if !perfil.nacionalidades.length}
              <span class="profile-info-label">Sin nacionalidades registradas</span>
            {/if}
          </div>
        {/if}
      </div>

      <!-- ── Teléfono ──────────────────────────────────────────────────────── -->
      <div class="profile-card">
        <h2 class="profile-section-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.21h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.96a16 16 0 0 0 6 6l.92-.92a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
          Actualizar teléfono
        </h2>
        <div class="profile-info-item" style="margin-bottom:.85rem">
          <span class="profile-info-label">Teléfono actual</span>
          <span class="profile-info-value">{perfil.telefono ?? '—'}</span>
        </div>

        {#if telefonoMsg}<div class="profile-alert success">{telefonoMsg}</div>{/if}
        {#if telefonoError}<div class="profile-alert error">{telefonoError}</div>{/if}

        <div class="profile-field">
          <label for="pais-telefono">País del número</label>
          <div class="profile-autocomplete-wrap">
            <input type="text" id="pais-telefono" bind:value={paisQuery}
              on:input={onPaisInput} on:blur={validarPaisSeleccionado}
              placeholder="Escribe tu país..." autocomplete="off" />
            {#if paisesSugeridos.length > 0}
              <ul class="profile-autocomplete-list">
                {#each paisesSugeridos as p}
                  <li><button type="button" class="profile-autocomplete-btn" on:click={() => seleccionarPais(p)}>{p.country}</button></li>
                {/each}
              </ul>
            {/if}
          </div>
        </div>

        <div class="profile-field">
          <label for="telefono">
            Nuevo número
            {#if paisSeleccionado && phoneDigitCount}
              <span class="profile-info-label"> — {phoneDigitCount} dígitos requeridos</span>
            {/if}
          </label>
          <div class="profile-phone-wrap">
            {#if dialCode}<span class="profile-dial-code">{dialCode}</span>{/if}
            <input type="tel" id="telefono" bind:value={nuevoTelefono} on:input={onPhoneInput}
              placeholder={dialCode ? getPhonePlaceholder(phoneDigitCount) : 'Selecciona un país primero'}
              disabled={!dialCode} />
          </div>
          {#if nuevoTelefono && !telefonoError}
            {@const d = nuevoTelefono.replace(/\D/g, '').length}
            {#if d === phoneDigitCount}
              <span class="profile-match ok">✓ Número completo</span>
            {:else}
              <span class="profile-match err">{d}/{phoneDigitCount} dígitos</span>
            {/if}
          {/if}
        </div>

        <button class="profile-btn-primary" on:click={guardarTelefono} disabled={savingTelefono}>
          {#if savingTelefono}
            <svg class="btn-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            Guardando...
          {:else}
            Guardar teléfono
          {/if}
        </button>
      </div>

      <!-- ── Contraseña ────────────────────────────────────────────────────── -->
      <div class="profile-card">
        <h2 class="profile-section-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          Cambiar contraseña
        </h2>

        {#if contrasenaMsg}<div class="profile-alert success">{contrasenaMsg}</div>{/if}
        {#if contrasenaError}<div class="profile-alert error">{contrasenaError}</div>{/if}

        <div class="profile-field">
          <label for="pwd-actual">Contraseña actual</label>
          <div class="profile-pwd-wrap">
            <input type={showActual ? 'text' : 'password'} id="pwd-actual" bind:value={contrasenaActual} placeholder="Tu contraseña actual" />
            <button type="button" class="profile-eye" on:click={() => showActual = !showActual} tabindex="-1">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {#if showActual}<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                {:else}<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>{/if}
              </svg>
            </button>
          </div>
        </div>

        <div class="profile-field">
          <label for="pwd-nueva">Nueva contraseña</label>
          <div class="profile-pwd-wrap">
            <input type={showNueva ? 'text' : 'password'} id="pwd-nueva" bind:value={contrasenaNueva} placeholder="Mínimo 8 caracteres" />
            <button type="button" class="profile-eye" on:click={() => showNueva = !showNueva} tabindex="-1">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {#if showNueva}<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                {:else}<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>{/if}
              </svg>
            </button>
          </div>
        </div>

        <div class="profile-field">
          <label for="pwd-confirmar">Confirmar nueva contraseña</label>
          <div class="profile-pwd-wrap">
            <input type={showConfirmar ? 'text' : 'password'} id="pwd-confirmar" bind:value={confirmarNueva} placeholder="Repite la nueva contraseña" />
            <button type="button" class="profile-eye" on:click={() => showConfirmar = !showConfirmar} tabindex="-1">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                {#if showConfirmar}<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                {:else}<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>{/if}
              </svg>
            </button>
          </div>
          {#if confirmarNueva && contrasenaNueva === confirmarNueva}
            <span class="profile-match ok">✓ Las contraseñas coinciden</span>
          {:else if confirmarNueva}
            <span class="profile-match err">✗ No coinciden</span>
          {/if}
        </div>

        <button class="profile-btn-primary" on:click={guardarContrasena} disabled={savingContrasena}>
          {#if savingContrasena}
            <svg class="btn-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            Guardando...
          {:else}
            Cambiar contraseña
          {/if}
        </button>
      </div>

      <!-- ── Preferencias de ofertas ──────────────────────────────────────── -->
      <div class="profile-card">
        <div class="profile-card-header">
          <h2 class="profile-section-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 12 20 22 4 22 4 12"/><rect x="2" y="7" width="20" height="5"/>
              <line x1="12" y1="22" x2="12" y2="7"/><path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z"/>
              <path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z"/>
            </svg>
            Preferencias de ofertas
          </h2>
          {#if !editPreferencias}
            <button class="profile-edit-btn" on:click={abrirEditPref}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
          {/if}
        </div>

        {#if prefMsg}<div class="profile-alert success">{prefMsg}</div>{/if}

        {#if editPreferencias}
          {#if prefErr}<div class="profile-alert error">{prefErr}</div>{/if}

          <label class="profile-offers-toggle">
            <input type="checkbox" bind:checked={recibirOfertas} />
            <span class="profile-toggle-track" class:profile-toggle-track--on={recibirOfertas}></span>
            <span class="profile-offers-label">Quiero recibir ofertas y promociones</span>
          </label>

          {#if recibirOfertas}
            <div class="profile-prefs-panel">
              <div class="profile-pref-group">
                <span class="profile-pref-label">Tipos de habitación favoritos</span>
                <div class="profile-pref-chips">
                  {#each tipoHabOpciones as tipo}
                    <button type="button" class="profile-pref-chip"
                      class:profile-pref-chip--active={prefData.tiposHabitacion.includes(tipo)}
                      on:click={() => togglePrefTipoHab(tipo)}>{tipo}</button>
                  {/each}
                </div>
              </div>

              <div class="profile-pref-group">
                <span class="profile-pref-label">Presupuesto estimado por noche</span>
                <div class="profile-pref-radios">
                  {#each ['Económico', 'Estándar', 'Premium', 'Lujo'] as op}
                    <label class="profile-pref-radio-lbl">
                      <input type="radio" bind:group={prefData.presupuesto} value={op} />
                      <span class="profile-pref-radio-btn" class:profile-pref-radio-btn--active={prefData.presupuesto === op}>{op}</span>
                    </label>
                  {/each}
                </div>
              </div>

              <div class="profile-pref-group">
                <span class="profile-pref-label">Preferencia de habitación</span>
                <div class="profile-pref-radios">
                  {#each ['Una habitación', 'Combinar habitaciones'] as op}
                    <label class="profile-pref-radio-lbl">
                      <input type="radio" bind:group={prefData.combinacion} value={op} />
                      <span class="profile-pref-radio-btn" class:profile-pref-radio-btn--active={prefData.combinacion === op}>{op}</span>
                    </label>
                  {/each}
                </div>
              </div>

              <div class="profile-pref-group">
                <span class="profile-pref-label">¿Cuántas personas viajan contigo usualmente?</span>
                <div class="profile-pref-radios">
                  {#each [1, 2, 3, 4] as n}
                    <label class="profile-pref-radio-lbl">
                      <input type="radio" bind:group={prefData.personasExtra} value={n} />
                      <span class="profile-pref-radio-btn" class:profile-pref-radio-btn--active={prefData.personasExtra === n}>
                        {n === 4 ? '4+' : n} {n === 1 ? 'persona' : 'personas'}
                      </span>
                    </label>
                  {/each}
                </div>
              </div>
            </div>
          {/if}

          <div class="profile-edit-actions">
            <button class="profile-btn-primary" on:click={guardarPreferencias} disabled={savingPref}>
              {savingPref ? 'Guardando...' : 'Guardar preferencias'}
            </button>
            <button class="profile-btn-secondary" on:click={() => editPreferencias = false}>Cancelar</button>
          </div>

        {:else}
          {@const prefs = parsePrefs(perfil.preferenciasOferta)}
          {#if prefs}
            <div class="profile-prefs-view">
              <div class="profile-pref-badge active">🎁 Suscrito a ofertas y promociones</div>
              <div class="profile-pref-row">
                <span class="profile-pref-key">Habitaciones:</span>
                <span>{prefs.tiposHabitacion?.length ? prefs.tiposHabitacion.join(', ') : 'Cualquiera'}</span>
              </div>
              <div class="profile-pref-row">
                <span class="profile-pref-key">Presupuesto:</span>
                <span>{prefs.presupuesto}</span>
              </div>
              <div class="profile-pref-row">
                <span class="profile-pref-key">Habitación:</span>
                <span>{prefs.combinacion}</span>
              </div>
              <div class="profile-pref-row">
                <span class="profile-pref-key">Viajeros habituales:</span>
                <span>{prefs.personasExtra === 4 ? '4+' : prefs.personasExtra}</span>
              </div>
            </div>
          {:else}
            <p class="profile-info-label" style="padding:.5rem 0">Sin preferencias de ofertas configuradas.</p>
          {/if}
        {/if}
      </div>

    {/if}
  </div>
</div>
