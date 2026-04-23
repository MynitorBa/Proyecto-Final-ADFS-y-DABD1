// __mocks__/api.cjs
// Mock de src/lib/api.js — evita que Jest falle leyendo VITE_API_URL
// (esa variable solo existe en tiempo de build de Vite, no en Jest).
module.exports = {
  API: 'http://localhost:7000'
};
