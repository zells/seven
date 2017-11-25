const net = require('net');

const Dish = require('../src/dish');
const ServerPeer = require('../src/peers/socket-server-peer');
const ClientPeer = require('../src/peers/socket-client-peer');

const dish1 = new Dish();
const dish2 = new Dish();

Promise.all([
    ServerPeer.listen(4242, dish2)
]).then(() => {
    return dish1.join(new ClientPeer(4242));
}).then(() => {
    var promise = new Promise(y => dish2.put({receive: y }));
    dish1.transmit('Hello');
    return promise;
}).catch((err) => {
    console.log(err.stack);
}).then(() => {
    console.log('Done');
    process.exit(0);
});