require('buffer');

var NIL = 0;
var VAL = 1;
var END = 2;
var LST = 3;
var ESC = 4;

function translate(data) {
    if (data === false) {
        data = 0
    } else if (data === true) {
        data = 1
    }

    if (data && typeof data == 'object' && !Array.isArray(data) && !Buffer.isBuffer(data)) {
        data = [Object.keys(data), Object.keys(data).map(k => data[k])];
    } else if (typeof data == 'string') {
        data = new Buffer(data);
    } else if (typeof data == 'number') {
        var negative = false;
        var number = data;
        data = [];
        if (number == 0) {
            data.push(0);
        }
        if (number < 0) {
            negative = true;
            number = -number;
        }
        while (number > 0) {
            var byte = number & 0xFF;
            data.unshift(byte);
            number = number >> 8;
        }
        data = Buffer.from(data);

        if (negative) {
            data = [Buffer.from('-'), data];
        }
    }
    return data;
}

function encoder(writer) {
    function encodeValue(data) {
        data = translate(data);

        var bytes = [];
        if (data === null) {
            bytes.push(NIL);
        } else if (Array.isArray(data)) {
            bytes.push(LST);
            data.forEach(d => encodeValue(d).forEach(b => bytes.push(b)));
            bytes.push(END);
        } else if (Buffer.isBuffer(data)) {
            bytes.push(VAL);
            for (var i = 0; i < data.length; i++) {
                if (data[i] == END || data[i] == ESC) bytes.push(ESC);
                bytes.push(data[i]);
            }
            bytes.push(END);
        }

        return bytes;
    }

    return (data) => {
        var bytes = encodeValue(data);
        if (bytes.length) writer.write(Buffer.from(bytes));
    }
}

function decoder(reader, data) {
    var stack = [];
    var inValue = false;
    var escaped = false;

    function end(value) {
        if (!stack.length) {
            data(value);
        } else {
            stack[stack.length - 1].push(value)
        }
    }

    reader.on('data', buffer => {
        for (var i = 0; i < buffer.length; i++) {
            var byte = buffer[i];

            if (inValue) {
                if (!escaped && byte == END) {
                    inValue = false;
                    end(Buffer.from(stack.pop()));
                } else if (escaped || byte != ESC) {
                    stack[stack.length - 1].push(byte)
                }
            } else if (byte == NIL) {
                end(null)
            } else if (byte == VAL) {
                stack.push([]);
                inValue = true;
            } else if (byte == LST) {
                stack.push([]);
            } else if (byte == END) {
                end(stack.pop());
            }

            if (escaped) {
                escaped = false;
            } else if (byte == ESC) {
                escaped = true;
            }
        }
    })
}

module.exports = {
    Encoder: encoder,
    Decoder: decoder
};