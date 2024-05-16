const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const gameManager = require('./GameManager');

const app = express();
const server = http.createServer(app);
const io = socketIO(server);



app.use(express.static(__dirname));

io.on('connection', (socket) => {
    console.log('Un jugador se ha conectado');
    gameManager.handleConnection(socket, io);
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Servidor escuchando en el puerto ${PORT}`);
});
