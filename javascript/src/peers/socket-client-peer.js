const net = require('net');
const Post = require('../post');

function SocketClientPeer(port, host) {
    this.post = new Post();
    this.connected = false;
    this.client = new net.Socket();

    this.client.connect(port, host || '127.0.0.1', () => {
        this.connected = true;
    });
}

SocketClientPeer.prototype.receive = function (id, signal) {
    if (!this.connected) return;
    this.client.write(this.post.encode(this.post.transmitPacket(id, signal)));
};

SocketClientPeer.prototype.onReceive = function (callback) {
    this.client.on("data", (data) => this.post.receive(this.post.decode(data), callback))
};

SocketClientPeer.prototype.onClose = function (callback) {
    this.client.on('close', () => callback())
};

module.exports = SocketClientPeer;