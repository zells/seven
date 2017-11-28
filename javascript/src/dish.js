const uuid = require('uuid/v4');

function Dish() {
    this.zells = {};
    this.peers = {};
    this.received = {};
}

Dish.prototype.put = function (zell) {
    var id = Math.max(0, Math.max.apply(null, Object.keys(this.zells).map(k => Number.parseInt(k))));
    this.zells[id] = zell;
    zell.transmit = this.transmit.bind(this);
    return id;
};

Dish.prototype.remove = function (zellId) {
    if (!(zellId in this.zells)) return;
    delete this.zells[zellId];
};

Dish.prototype.join = function (peer) {
    var id = Math.max(0, Math.max.apply(null, Object.keys(this.peers).map(k => Number.parseInt(k))));

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
    if (id in this.received) {
        console.log('received', id);
        return;
    }
    this.received[id] = true;
    console.log('receive', id);

    Object.keys(this.zells).forEach(z => this.zells[z].receive(signal));
    Object.keys(this.peers).forEach(p => this.peers[p].sendSignal(id, signal));
};

Dish.prototype.transmit = function (signal) {
    this.receive(uuid(), signal);
};

module.exports = Dish;
