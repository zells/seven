var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var Writable = require('stream').Writable;
const Post = require('../post');

function WebSocketServerPeer(socket, io) {
    this.socket = socket;
    this.io = io;
    this.post = new Post({
        write: (data) => this.io.emit('data', new ArrayBuffer(data))
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

WebSocketServerPeer.prototype.receive = function (id, signal) {
    this.post.transmit(id, signal);
};

WebSocketServerPeer.prototype.onReceive = function (callback) {
    var stream = new Writable();
    this.socket.on('data', data => {
        console.log('DATA', new Buffer(data));
        this.io.emit('data', new Buffer(data));
    });
    this.post.readFrom(stream, callback);
};

WebSocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('disconnect', () => callback());
};

module.exports = WebSocketServerPeer;