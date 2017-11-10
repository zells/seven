const Post = require('../post');

function SocketServerPeer( socket) {
    this.post = new Post();
    this.socket = socket;
}

SocketServerPeer.prototype.receive = function (id, signal) {
    this.socket.write(this.post.encode(this.post.transmitPacket(id, signal)))
};

SocketServerPeer.prototype.onReceive = function (callback) {
    this.socket.on('data', (data) => this.post.receive(this.post.decode(data), callback))
};

SocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('end', () => callback())
};

module.exports = SocketServerPeer;
