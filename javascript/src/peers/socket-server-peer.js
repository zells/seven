const Post = require('../post');

function SocketServerPeer( socket) {
    this.post = new Post();
    this.socket = socket;
}

SocketServerPeer.prototype.receive = function (id, signal) {
    this.socket.write(this.post.encode(this.post.transmitPacket(id, signal)))
};

SocketServerPeer.prototype.onReceive = function (callback) {
    this.post.readFrom(this.socket, callback);
};

SocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('end', () => callback())
};

module.exports = SocketServerPeer;
