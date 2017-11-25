const net = require('net');
var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

const Dish = require('./src/dish');
const ServerPeer = require('./src/peers/socket-server-peer');
const ClientPeer = require('./src/peers/socket-client-peer');
const WebSocketPeer = require('./src/peers/web-socket-server-peer');

const DEFAULT_PORT = 1337;

const dish = new Dish();

ServerPeer.listen(process.env.PORT || DEFAULT_PORT);

const httpPort = process.env.HTTP_PORT;

if (httpPort) {
    app.get('/', (req, res) => {
        res.sendFile(__dirname + '/web/index.html');
    });

    app.get('/:resource', (req, res) => {
        res.sendFile(__dirname + '/web/' + req.params.resource);
    });

    io.on('connection', (socket) => {
        dish.join(new WebSocketPeer(socket, io))
    });

    http.listen(httpPort, () => {
        console.log('listening on http://localhost:' + httpPort);
    });
}

module.exports = {dish, ClientPeer};