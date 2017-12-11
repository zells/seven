const Dish = require('../src/dish');
const ServerPeer = require('../src/peers/socket-server-peer');
var encoding = require('../src/encoding');

const DEFAULT_PORT = 1337;

const dish = new Dish();

var responses = {};
var encoder = encoding.Encoder({
    write: signal => {
        console.log('--------- response', signal);
        responses[signal] = true;
        dish.transmit(signal)
    }
});

dish.put({
    receive: (signal) => {
        if (!signal || signal in responses) {
            console.log('--------- echo', signal);
            return;
        }
        console.log('--------- signal', signal);

        encoding.Decoder({onData: cb => cb(signal)}, (message) => {
            console.log('--------- received', message, Buffer.isBuffer(message));

            if (Buffer.isBuffer(message)) {
                var reversed = [];
                for (var i = message.length - 1; i > -1; i--) {
                    reversed.push(message[i]);
                }
                encoder(Buffer.from(reversed));

            } else {

                if (message.length < 2) {
                    encoder(message);
                } else if (message.length == 3) {
                    var reversedList = [];
                    for (var j = message.length - 1; j > -1; j--) {
                        reversedList.push(message[j]);
                    }
                    encoder(reversedList);
                } else if (message.length == 2) {

                    var opCode = message[0].toString('hex');
                    var argument = message[1];

                    if (opCode == '01') {
                        encoder(!argument[0]);
                    } else if (opCode == '02') {
                        encoder(argument.toString().toUpperCase());
                    } else if (opCode == '03') {
                        encoder(argument[0].toString() + argument[1].toString())
                    } else if (opCode == '04') {
                        encoder(encoding.translate.toNumber(argument[0]) + encoding.translate.toNumber(argument[1]));
                    } else if (opCode == '05') {
                        encoder(encoding.translate.toNumber(argument[0]) * encoding.translate.toNumber(argument[1]));
                    } else if (opCode == '06') {
                        encoder({"found": encoding.translate.toObject(argument[1])[argument[0]]});
                    }
                }
            }
        })
    }
});

ServerPeer.listen(dish, process.env.PORT || DEFAULT_PORT);