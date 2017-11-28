const net = require('net');
const Post = require('../post');

function SocketClientPeer(port, host) {
    this.port = port;
    this.host = host || '127.0.0.1';

    this.client = new net.Socket();
    this.post = new Post(this.client, {onData: callback => this.client.on('data', callback)});
}

SocketClientPeer.prototype.connect = function () {
    return new Promise((y) => {
        this.client.connect(this.port, this.host, y);
    })
};

SocketClientPeer.prototype.sendSignal = function (id, signal) {
    this.post.sendSignal(id, signal);
};

SocketClientPeer.prototype.onSignal = function (callback) {
    this.post.onSignal(callback);
};

SocketClientPeer.prototype.onClose = function (callback) {
    this.client.on('close', () => callback())
};

module.exports = SocketClientPeer;