const uuid = require('uuid/v4');

function Dish() {
    this.peers = {};
    this.peerId = 0;
    this.received = {};

    this.zells = [{
        receive: (signal) => console.log('Received', signal)
    }];
}

Dish.prototype.join = function (peer) {
    console.log('join', ++this.peerId)
    
    this.peers[this.peerId] = peer;
    peer.onReceive((id, signal) => this.receive(id, signal));
    peer.onClose(() => this.leave(this.peerId));
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
