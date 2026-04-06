/**
 * Datos de las suites de prueba.
 * Cada suite representa una clase *Test.java.
 * Para agregar mas suites, agrega un objeto al array DATA.
 */
const DATA = [
  {
    suite: "UsuarioService",
    clase: "org.example.services.UsuarioServiceTest",
    tests: [
      { name: "Todos los campos disponibles retorna todo en false",    method: "validarDisponibilidad_todosLibres",                    status: "pass", ms: 8    },
      { name: "Username ocupado retorna usernameExiste en true",       method: "validarDisponibilidad_usernameOcupado",                status: "pass", ms: 15   },
      { name: "Username duplicado lanza CamposDuplicadosException",   method: "registrarUsuario_usernameDuplicado_lanzaExcepcion",    status: "pass", ms: 11   },
      { name: "Registro exitoso retorna el ID del nuevo usuario",      method: "registrarUsuario_exitoso_retornaId",                   status: "pass", ms: 4280 },
      { name: "Perfil de usuario inexistente lanza RuntimeException",  method: "obtenerPerfil_noExiste_lanzaExcepcion",               status: "pass", ms: 7487 },
      { name: "Perfil existente retorna datos con nacionalidades",     method: "obtenerPerfil_existente_retornaPerfil",               status: "pass", ms: 12   },
      { name: "Telefono vacio lanza IllegalArgumentException",         method: "cambiarTelefono_vacio_lanzaExcepcion",                status: "pass", ms: 9    },
      { name: "Telefono valido llama al repositorio correctamente",    method: "cambiarTelefono_valido_actualizaRepositorio",         status: "pass", ms: 4    },
      { name: "Rol invalido lanza IllegalArgumentException",           method: "cambiarRol_invalido_lanzaExcepcion",                  status: "pass", ms: 5    },
      { name: "Rol valido llama al repositorio correctamente",         method: "cambiarRol_valido_actualizaRepositorio",              status: "pass", ms: 4    },
    ]
  }
];

/**
 * Formatea milisegundos a una cadena legible.
 * @param {number} ms
 * @returns {string}
 */
function fmtMs(ms) {
  return ms >= 1000 ? (ms / 1000).toFixed(1) + 's' : ms + 'ms';
}

/**
 * Renderiza las suites y actualiza las metricas resumen.
 */
function render() {
  const currentFilter = document.querySelector('.filter-btn.active').dataset.filter;
  const container     = document.getElementById('suites');
  container.innerHTML = '';

  let totalTests = 0, totalPass = 0, totalFail = 0, totalMs = 0;

  DATA.forEach(suite => {
    const tests = suite.tests.filter(t =>
      currentFilter === 'all' ? true : t.status === currentFilter
    );
    if (!tests.length) return;

    const passCount = suite.tests.filter(t => t.status === 'pass').length;
    const failCount = suite.tests.filter(t => t.status === 'fail').length;
    totalTests += suite.tests.length;
    totalPass  += passCount;
    totalFail  += failCount;
    totalMs    += suite.tests.reduce((a, t) => a + t.ms, 0);

    const div = document.createElement('div');
    div.className = 'suite';
    div.innerHTML = `
      <div class="suite-header">
        <h2>${suite.suite}</h2>
        <span class="suite-badge">${passCount}/${suite.tests.length} pasaron</span>
      </div>
      ${tests.map(t => `
        <div class="test-row">
          <div class="icon ${t.status}">${t.status === 'pass' ? '&#10003;' : '&#10007;'}</div>
          <div class="test-name">${t.name}</div>
          <div class="test-method">${t.method}()</div>
          <div class="test-time">${fmtMs(t.ms)}</div>
        </div>
      `).join('')}
    `;
    container.appendChild(div);
  });

  document.getElementById('total').textContent    = totalTests;
  document.getElementById('passed').textContent   = totalPass;
  document.getElementById('failed').textContent   = totalFail;
  document.getElementById('duration').textContent = fmtMs(totalMs);
  document.getElementById('run-date').textContent = 'Ejecutado: ' + new Date().toLocaleString('es-GT');
}

/**
 * Inicializa los botones de filtro y renderiza la primera vez.
 */
document.querySelectorAll('.filter-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    render();
  });
});

render();