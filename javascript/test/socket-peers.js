var assert = require('assert');

const Dish = require('../src/dish');
const ServerPeer = require('../src/peers/socket-server-peer');
const ClientPeer = require('../src/peers/socket-client-peer');

const dish1 = new Dish();
const dish2 = new Dish();

Promise.all([
    ServerPeer.listen(dish2, 4242)
]).then(() => {
    return dish1.join(new ClientPeer(4242));
}).then(() => {
    var promise = new Promise(y => dish2.put({receive: y}));
    dish1.transmit('Hello');
    return promise;
}).then((signal) => {
    assert.equal('Hello', signal.toString());
}).then(() => {
    var promise = new Promise(y => dish1.put({receive: y}));
    dish2.transmit('Hello Back');
    return promise;
}).then((signal) => {
    assert.equal('Hello Back', signal.toString());
}).then(() => {
    console.log('OK');
    process.exit(0);
}).catch((err) => {
    console.log(err.stack);
});