const uuid = require('uuid/v4');

function Dish() {
    this.zells = {};
    this.peers = {};
    this.received = {};
}

Dish.prototype.put = function (zell) {
    var id = Math.max(0, Math.max.apply(null, Object.keys(this.zells).map(k => Number.parseInt(k))) + 1);
    this.zells[id] = zell;
    zell.transmit = this.transmit.bind(this);
    return id;
};

Dish.prototype.remove = function (zellId) {
    if (!(zellId in this.zells)) return;
    delete this.zells[zellId];
};

Dish.prototype.join = function (peer) {
    var id = Math.max(0, Math.max.apply(null, Object.keys(this.peers).map(k => Number.parseInt(k))) + 1);

    console.log('join', id);

    this.peers[id] = peer;
    peer.connect().then(() => {
        peer.onSignal((id, signal) => this.receive(id, signal));
        peer.onClose(() => this.leave(id));
    });

    return id;
};

Dish.prototype.leave = function (peerId) {
    if (!(peerId in this.peers)) return;

    console.log('leave', peerId);
    delete this.peers[peerId];
};

Dish.prototype.receive = function (id, signal) {
    var idHex = id.toString('hex');

    if (idHex in this.received) {
        console.log('received', idHex);
        return;
    }
    this.received[idHex] = true;
    console.log('receive', idHex, signal);

    Object.keys(this.zells).forEach(z => this.zells[z].receive(signal));
    Object.keys(this.peers).forEach(p => this.peers[p].sendSignal(id, signal));
};

Dish.prototype.transmit = function (signal) {
    var id = [];
    uuid({}, id);
    this.receive(Buffer.from(id.slice(2, 4)), signal);
};

module.exports = Dish;
