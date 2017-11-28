var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
const Post = require('../post');

function WebSocketServerPeer(socket) {
    this.socket = socket;
    this.post = new Post({
        write: (data) => {
            io.emit('data', data)
        }
    }, {
        onData: (callback) => this.socket.on('data', (data) => {
            callback(new Buffer(data))
        })
    });
}

WebSocketServerPeer.listen = function (dish, port, webRoot) {
    app.get('/', (req, res) => {
        res.sendFile(webRoot + 'index.html');
    });

    app.get('/:resource', (req, res) => {
        res.sendFile(webRoot + req.params.resource);
    });

    io.on('connection', (socket) => {
        dish.join(new WebSocketServerPeer(socket, io))
    });

    http.listen(port, () => {
        console.log('listening on http://localhost:' + port);
    });
};

WebSocketServerPeer.prototype.connect = function () {
    return Promise.resolve();
};

WebSocketServerPeer.prototype.sendSignal = function (id, signal) {
    this.post.sendSignal(id, signal);
};

WebSocketServerPeer.prototype.onSignal = function (callback) {
    this.post.onSignal(callback);
};

WebSocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('disconnect', () => callback());
};

module.exports = WebSocketServerPeer;