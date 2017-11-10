const msgpack = require("msgpack-lite");

function Post() {
    this.receivers = {
        join: () => null,
        transmit: (receiver, args) => receiver.apply(null, args)
    };
}

Post.prototype.decode = msgpack.decode;
Post.prototype.encode = msgpack.encode;

Post.prototype.receive = function (packet, receiver) {
    if (!(packet[0] in this.receivers)) {
        return console.error('Unknown packet', packet);
    }

    this.receivers[packet[0]](receiver, packet.slice(1));
};

Post.prototype.joinPacket = function () {
    return ['join'];
};

Post.prototype.transmitPacket = function (id, signal) {
    return ['transmit', id, signal];
};

module.exports = Post;