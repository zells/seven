const net = require('net');
const Post = require('../post');

function SocketServerPeer(socket) {
    this.post = new Post(socket);
    this.socket = socket;
}

SocketServerPeer.listen = function (port, dish) {
    return new Promise((y) => {
        const server = net.createServer((socket) => {
            dish.join(new SocketServerPeer(socket));
        });

        server.listen(port, () => {
            console.log('listening on', port);
            y();
        });
    })
};

SocketServerPeer.prototype.connect = function () {
    return Promise.resolve();
};

SocketServerPeer.prototype.receive = function (id, signal) {
    this.post.transmit(id, signal)
};

SocketServerPeer.prototype.onReceive = function (callback) {
    this.post.readFrom(this.socket, callback);
};

SocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('end', () => callback())
};

module.exports = SocketServerPeer;
