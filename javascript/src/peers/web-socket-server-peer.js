var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
const Post = require('../post');

function WebSocketServerPeer(socket, io) {
    this.socket = socket;
    this.io = io;
    this.post = new Post({
        write: (data) => this.io.emit('data', data)
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
    this.post.readFrom({
        on: (event, then) => {
            if (event == 'data') {
                this.socket.on(event, (data) => {
                    if (data.type == 'Buffer') {
                        then(Buffer.from(data.data));
                    }
                });
            } else {
                this.socket.on(event, then);
            }
        }
    }, callback);
};

WebSocketServerPeer.prototype.onClose = function (callback) {
    this.socket.on('disconnect', () => callback());
};

module.exports = WebSocketServerPeer;