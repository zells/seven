var encoding = require('./encoding');

var SIGNAL = 1;

function Post(writer, reader) {
    this.read = reader || writer;
    this.write = encoding.Encoder(writer);
}

Post.prototype.sendSignal = function (id, signal) {
    this.write([id, signal]);
};

Post.prototype.onSignal = function (receiver) {
    encoding.Decoder(this.read, (packet) => {
        receiver(packet[0], packet[1]);
    });
};

module.exports = Post;