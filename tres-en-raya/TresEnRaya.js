const FILES = 3;
const COLUMNES = 3;

function inicialitzarTauler() {
  return Array.from({ length: FILES }, () => Array(COLUMNES).fill(0));
}

function columnaPlena(tauler, columna) {
  return tauler[0][columna] !== 0;
}

function ferMoviment(tauler, fila, columna, jugador) {
  if (tauler[fila][columna] === 0) {
    tauler[fila][columna] = jugador;
    return true; // Movimiento válido
  }
  return false; // Movimiento inválido
}

function checkVictoria(tauler, jugador) {
  // Comprobar filas y columnas
  for (let i = 0; i < FILES; i++) {
    if (
      (tauler[i][0] === jugador && tauler[i][1] === jugador && tauler[i][2] === jugador) ||
      (tauler[0][i] === jugador && tauler[1][i] === jugador && tauler[2][i] === jugador)
    ) {
      return true;
    }
  }

  // Comprobar diagonales
  if (
    (tauler[0][0] === jugador && tauler[1][1] === jugador && tauler[2][2] === jugador) ||
    (tauler[0][2] === jugador && tauler[1][1] === jugador && tauler[2][0] === jugador)
  ) {
    return true;
  }

  // Comprobar empate
  const isEmpate = tauler.flat().every(cell => cell !== 0);
  if (isEmpate) {
    return 'empate';
  }

  return false;
}

module.exports = { inicialitzarTauler, columnaPlena, ferMoviment, checkVictoria };
