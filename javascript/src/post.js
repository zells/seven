var encoding = require('./encoding');

var SIGNAL = 1;

function Post(writer, reader) {
    this.read = reader || writer;
    this.write = encoding.Encoder(writer);
}

Post.prototype.sendSignal = function (id, signal) {
    this.write([SIGNAL, id, signal]);
};

Post.prototype.onSignal = function (receiver) {
    encoding.Decoder(this.read, (packet) => {
        if (packet && packet.length == 3 && packet[0][0] == SIGNAL) {
            receiver(packet[1], packet[2]);
        }
    });
};

module.exports = Post;