const Dish = require('./src/dish');
const ServerPeer = require('./src/peers/socket-server-peer');
const ClientPeer = require('./src/peers/socket-client-peer');
const WebSocketPeer = require('./src/peers/web-socket-server-peer');

const DEFAULT_PORT = 1337;

const dish = new Dish();

dish.put({
    receive: (signal) => console.log('RECEIVED', signal)
});

ServerPeer.listen(dish, process.env.PORT || DEFAULT_PORT);

const httpPort = process.env.HTTP_PORT;
if (httpPort) {
    WebSocketPeer.listen(dish, httpPort, __dirname + '/web/');
}

module.exports = {dish, ClientPeer};