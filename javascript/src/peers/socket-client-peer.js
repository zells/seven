const net = require('net');
const Post = require('../post');

function SocketClientPeer(port, host) {
    this.port = port;
    this.host = host || '127.0.0.1';

    this.client = new net.Socket();
    this.post = new Post(this.client);
}

SocketClientPeer.prototype.connect = function () {
    return new Promise((y) => {
        this.client.connect(this.port, this.host, y);
    })
};

SocketClientPeer.prototype.receive = function (id, signal) {
    this.post.transmit(id, signal);
};

SocketClientPeer.prototype.onReceive = function (callback) {
    this.post.readFrom(this.client, callback);
};

SocketClientPeer.prototype.onClose = function (callback) {
    this.client.on('close', () => callback())
};

module.exports = SocketClientPeer;