const uuid = require('uuid/v4');

function Dish() {
    this.peers = {};
    this.peerId = 0;
    this.received = {};

    this.zells = [{
        receive: (signal) => console.log('Received', signal)
    }];
}

Dish.prototype.put = function (zell) {
    this.zells.push(zell);
    zell.transmit = this.transmit.bind(this);
    zell.remove = (function () {
        this.remove(zell);
    }).bind(this);
};

Dish.prototype.remove = function (zell) {
    if (this.zells.indexOf(zell) < 0) return;
    delete this.zells[this.zells.indexOf(zell)];
};

Dish.prototype.join = function (peer) {
    var peerId = this.peerId++;

    console.log('join', peerId);
    
    this.peers[peerId] = peer;
    peer.onReceive((id, signal) => this.receive(id, signal));
    peer.onClose(() => this.leave(peerId));
};

Dish.prototype.leave = function (peerId) {
    console.log('leave', peerId)
    
    delete this.peers[peerId];
};

Dish.prototype.receive = function (id, signal) {
	console.log('receive', id);
	
    if (id in this.received) {
        return;
    }
    this.received[id] = true;

    this.zells.forEach(z => z.receive(signal));

    Object.keys(this.peers).forEach(p => this.peers[p].receive (id, signal));
};

Dish.prototype.transmit = function (signal) {
    this.receive(uuid(), signal);
};

module.exports = Dish;
