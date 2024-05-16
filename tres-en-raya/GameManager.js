const { inicialitzarTauler, ferMoviment, checkVictoria } = require('./TresEnRaya');

const joc = {
  jugadors: [],
  tauler: inicialitzarTauler(),
  tornJugador: 0,
  finpartida: false,
};

function handleConnection(socket, io) {
  socket.on('unirsePartida', ({ nomJugador }) => {
    if (joc.jugadors.length < 2) {
      joc.jugadors.push({ id: socket.id, nom: nomJugador, symbol: joc.jugadors.length === 0 ? 'X' : 'O' });
      io.emit('infoJugadors', { jugadors: joc.jugadors, tornJugador: joc.tornJugador });
      if (joc.jugadors.length === 2) {
        io.emit('actualitzarJoc', joc);
      }
    } else {
      socket.emit('missatgeError', 'La sala de juego esta llena!');
    }
  });

  socket.on('ferMoviment', ({ fila, columna }) => {
    if (!joc.finpartida && joc.jugadors.length === 2 && joc.tornJugador === joc.jugadors.findIndex(player => player.id === socket.id)) {
      if (ferMoviment(joc.tauler, fila, columna, joc.jugadors[joc.tornJugador].symbol)) {
        io.emit('actualitzarJoc', joc);
        const result = checkVictoria(joc.tauler, joc.jugadors[joc.tornJugador].symbol);
        if (result === 'empat') {
          joc.finpartida = true;
          io.emit('gameOver', { resultat: 'empat' });
        } else if (result) {
          joc.finpartida = true;
          io.emit('gameOver', { resultat: 'guanyador', guanyador: joc.jugadors[joc.tornJugador].nom });
        } else {
          joc.tornJugador = 1 - joc.tornJugador; 
          io.emit('infoJugadors', { jugadors: joc.jugadors, tornJugador: joc.tornJugador });
        }
      } else {
        socket.emit('missatgeError', 'La casilla ya esta ocupada!');
      }
    }
  });

 socket.on('reiniciarPartida', () => {
  
  joc.tauler = inicialitzarTauler(); 
  joc.finpartida = false;
  joc.tornJugador = 0;
 
  io.emit('actualitzarJoc', joc);
  io.emit('infoJugadors', { jugadors: joc.jugadors, tornJugador: joc.tornJugador });
  io.emit('amagarResultat');
});


  socket.on('disconnect', () => {
    console.log(`Client desconectado: ${socket.id}`);
    joc.jugadors = joc.jugadors.filter((player) => player.id !== socket.id);
    if (joc.jugadors.length === 0) {
      joc.finpartida = false;
      joc.tauler = inicialitzarTauler();
      joc.tornJugador = 0;
    }
    io.emit('infoJugadors', { jugadors: joc.jugadors, tornJugador: joc.tornJugador });
  });
}

module.exports = { handleConnection };
