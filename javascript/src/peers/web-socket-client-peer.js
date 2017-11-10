const Post = require('../post');

function WebSocketClientPeer(io) {
    this.post = new Post();

    this.socket = io();
}

WebSocketClientPeer.prototype.receive = function (id, signal) {
    this.socket.emit('packet', this.post.encode(this.post.transmitPacket(id, signal)))
};

WebSocketClientPeer.prototype.onReceive = function (callback) {
    this.socket.on('packet', (data) => {
        this.post.receive(this.post.decode(new Uint8Array(data)), callback);
    });
};

WebSocketClientPeer.prototype.onClose = function (callback) {
};

module.exports = WebSocketClientPeer;