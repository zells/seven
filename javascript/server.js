var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

const Dish = require('./src/dish');
const ClientPeer = require('./src/peers/socket-client-peer');
const WebSocketPeer = require('./src/peers/web-socket-server-peer');

const DEFAULT_PORT = 3000;
const port = process.env.PORT || DEFAULT_PORT;

const dish = new Dish();

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/web/index.html');
});

app.get('/bundle.js', (req, res) => {
    res.sendFile(__dirname + '/web/bundle.js');
});

io.on('connection', (socket) => {
    dish.join(new WebSocketPeer(socket, io))
});

http.listen(port, () => {
    console.log('listening on http://localhost:' + port);
});

module.exports = {dish, ClientPeer};