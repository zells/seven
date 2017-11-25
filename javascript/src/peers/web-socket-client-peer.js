const Post = require('../post');

function WebSocketClientPeer(io) {
    this.socket = io();
    this.post = new Post({
        write: (data) => this.socket.emit('data', data)
    });
}

WebSocketClientPeer.prototype.connect = function () {
    return Promise.resolve();
};

WebSocketClientPeer.prototype.receive = function (id, signal) {
    this.post.transmit(id, signal);
};

WebSocketClientPeer.prototype.onReceive = function (callback) {
    this.post.readFrom(this.socket, callback);
};

WebSocketClientPeer.prototype.onClose = function (callback) {
};

module.exports = WebSocketClientPeer;