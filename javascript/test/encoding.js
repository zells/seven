var assert = require('assert');
var encoding = require('../src/encoding');

var reader = {
    listeners: {},
    on: (event, listener) => {
        if (!this.listeners) this.listeners = {};
        if (!this.listeners[event]) this.listeners[event] = [];
        this.listeners[event].push(listener);
    },
    fire: (event, value) => {
        console.log(' ', value.length, value);
        (this.listeners && this.listeners[event] || []).forEach(l => l(value));
    }
};
var writer = {write: (value) => reader.fire('data', value)};

var encoder = encoding.Encoder(writer);

var decoded = [];
encoding.Decoder(reader, (data) => {
    decoded.push(data);
});

function encodeDecode(value, expected) {
    expected = expected || value;

    console.log('encode & decode', value, ' => ', expected);
    encoder(value);
    assert.equal(1, decoded.length);
    assert.deepEqual(expected, decoded[0]);
    decoded = [];
}

encodeDecode(0, [0]);
encodeDecode(1, [1]);
encodeDecode(2, [2]);
encodeDecode(3, [3]);
encodeDecode(4, [4]);
encodeDecode(255, [255]);
encodeDecode(256, [1, 0]);
encodeDecode(0x7fffffff, [127, 255, 255, 255]);
encodeDecode(Buffer.from([0, 1, 2, 3]));
encodeDecode(Buffer.from([]));
encodeDecode([]);
encodeDecode([1], [[1]]);
encodeDecode([0, 1, 2, 3, 4], [[0], [1], [2], [3], [4]]);
encodeDecode([91, 92, 93], [[91], [92], [93]]);
encodeDecode([[]]);
encodeDecode([[1, 2], [3, [4, []]]], [[[1], [2]], [[3], [[4], []]]]);

encodeDecode(null, []);
encodeDecode(undefined, []);
encodeDecode(true, [1]);
encodeDecode(false, [0]);
encodeDecode(-42, [Buffer.from('-'), [42]]);
encodeDecode('', []);
encodeDecode('foo', Buffer.from('foo'));
encodeDecode({a: 1, b: 2}, [[[97], [98]], [[1], [2]]]);
encodeDecode({a: null, b: undefined}, [[[97], [98]], [[], []]]);
encodeDecode({compact: true, schema: 0}, [[Buffer.from('compact'), Buffer.from('schema')], [[1], [0]]]);