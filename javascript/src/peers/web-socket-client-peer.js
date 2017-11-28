const Post = require('../post');

function WebSocketClientPeer(io) {
    this.socket = io();
    this.socket.binaryType = 'arraybuffer';

    this.post = new Post({
        write: (data) => {
            this.socket.emit('data', data)
        }
    }, {
        onData: (callback) => this.socket.on('data', (data) => {
            callback(new Buffer(data))
        })
    });
}

WebSocketClientPeer.prototype.connect = function () {
    return Promise.resolve();
};

WebSocketClientPeer.prototype.sendSignal = function (id, signal) {
    this.post.sendSignal(id, signal);
};

WebSocketClientPeer.prototype.onSignal = function (callback) {
    this.post.onSignal(callback);
};

WebSocketClientPeer.prototype.onClose = function (callback) {
};

module.exports = WebSocketClientPeer;