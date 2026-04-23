/**
 * jest.config.cjs
 * Configuración de Jest para Miku Inn Frontend.
 * Extensión .cjs obligatoria porque package.json tiene "type":"module".
 *
 * Ubicación: raíz del proyecto (junto a package.json)
 */
module.exports = {
  // Entorno jsdom simula el navegador (necesario para @testing-library/svelte)
  testEnvironment: 'jsdom',

  // Transforma .svelte con svelte-jester y .js con babel-jest
  transform: {
    '^.+\\.svelte$': ['svelte-jester', { preprocess: false }],
    '^.+\\.js$':     ['babel-jest', { configFile: './babel.config.json' }],
  },

  // Extensiones que Jest reconoce
  moduleFileExtensions: ['js', 'svelte'],

  // Agrega los matchers de @testing-library/jest-dom
  setupFilesAfterEnv: ['@testing-library/jest-dom'],

  // Jest busca tests SOLO dentro de /tests (en la raíz, NO dentro de src)
  roots: ['<rootDir>/tests'],
  testMatch: ['**/*.test.js'],

  // Mocks para archivos que Jest no puede procesar nativamente
  moduleNameMapper: {
    '\\.css$':                         '<rootDir>/__mocks__/fileMock.cjs',
    '\\.(png|jpg|jpeg|gif|svg|webp)$': '<rootDir>/__mocks__/fileMock.cjs',
    '^.*/lib/api\\.js$':               '<rootDir>/__mocks__/api.cjs',
  },

  testPathIgnorePatterns: ['/node_modules/', '/screenshots/'],

  // Archivos incluidos en el reporte de cobertura (npm run test:coverage)
  collectCoverageFrom: [
    'src/utils/validarFechas.js',
    'src/lib/registerUtils.js',
  ],
};
