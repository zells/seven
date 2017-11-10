const Post = require('../post');

function WebSocketServerPeer(socket, io) {
    this.post = new Post();
    this.socket = socket;
    this.io = io;
}

WebSocketServerPeer.prototype.receive = function (id, signal) {
    this.io.emit('packet', this.post.encode(this.post.transmitPacket(id, signal)))
};

WebSocketServerPeer.prototype.onReceive = function (callback) {
    this.socket.on('packet', (data) => this.post.receive(this.post.decode(data.data), callback));
};

WebSocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('disconnect', () => callback());
};

module.exports = WebSocketServerPeer;