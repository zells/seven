const Post = require('../post');

function WebSocketClientPeer(io) {
    this.socket = io();
    this.socket.binaryType = 'arraybuffer';

    this.post = new Post({
        write: (data) => {
            console.log('emit', data);
            this.socket.emit('data', data)
        }
    });
}

WebSocketClientPeer.prototype.connect = function () {
    return Promise.resolve();
};

WebSocketClientPeer.prototype.receive = function (id, signal) {
    this.post.transmit(id, signal);
};

WebSocketClientPeer.prototype.onReceive = function (callback) {
    this.socket.on('data', data => console.log('DATA', new Buffer(data)));
    this.post.readFrom({on: () => null}, callback);
};

WebSocketClientPeer.prototype.onClose = function (callback) {
};

module.exports = WebSocketClientPeer;