const net = require('net');

const Dish = require('./src/dish');
const ServerPeer = require('./src/peers/socket-server-peer');
const ClientPeer = require('./src/peers/socket-client-peer');

const DEFAULT_PORT = 1337;
const port = process.env.PORT || DEFAULT_PORT;

const dish = new Dish();

const server = net.createServer((socket) => {
    dish.join(new ServerPeer(socket));
});

server.listen(port, () => {
    console.log('listening on', port);
});

module.exports = {dish, ClientPeer};