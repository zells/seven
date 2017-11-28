const net = require('net');
const Post = require('../post');

function SocketServerPeer(socket) {
    this.post = new Post(socket, {onData: callback => socket.on('data', callback)});
    this.socket = socket;
}

SocketServerPeer.listen = function (dish, port) {
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

SocketServerPeer.prototype.sendSignal = function (id, signal) {
    this.post.sendSignal(id, signal)
};

SocketServerPeer.prototype.onSignal = function (callback) {
    this.post.onSignal(callback);
};

SocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('end', () => callback())
};

module.exports = SocketServerPeer;
