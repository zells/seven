var encoding = require('./encoding');

function Post(writer) {
    this.write = encoding.Encoder(writer);
}

Post.prototype.transmit = function (id, signal) {
    this.write(['transmit', id, signal]);
};

Post.prototype.receive = function (packet, receiver) {
    var receivers = {
        join: () => null,
        transmit: (receiver, args) => receiver(args[0].toString(), args[1])
    };

    if (!packet || !(packet[0] in receivers)) return console.error('Unknown packet', packet);
    receivers[packet[0]](receiver, packet.slice(1));
};

Post.prototype.readFrom = function (stream, receiver) {
    encoding.Decoder(stream, (data) => {
        this.receive(data, receiver);
    });
};

module.exports = Post;