function translate(data) {
    if (data === false) {
        data = 0
    } else if (data === true) {
        data = 1
    }

    if (data && typeof data == 'object' && !Array.isArray(data) && !Buffer.isBuffer(data)) {
        data = [Object.keys(data), Object.keys(data).map(k => data[k])];
    } else if (typeof data == 'string') {
        data = Buffer.from(data || []);
    } else if (typeof data == 'undefined') {
        data = [];
    } else if (data === null) {
        data = [];
    } else if (typeof data == 'number') {
        var number = data;
        data = [];
        if (number == 0) {
            data.push(0);
        }

        var nominator = Math.floor(number);
        var denominator = 1;

        while (nominator != number * denominator) {
            denominator *= 10;
            nominator = Math.floor(number * denominator);
        }

        if (denominator > 1) {
            data = [Buffer.from('/'), translate(nominator), translate(denominator)];
        } else {
            var negative = false;
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
    }
    return data;
}

function encoder(writer, LST, END, ESC) {
    LST = LST || 25;
    END = END || 26;
    ESC = ESC || 27;

    function encodeValue(data) {
        data = translate(data);

        var bytes = [];
        if (Array.isArray(data)) {
            bytes.push(LST);
            data.forEach(d => encodeValue(d).forEach(b => bytes.push(b)));
            bytes.push(END);
            return bytes;
        }

        if (Buffer.isBuffer(data)) {
            if (!data.length) {
                return [LST, END];
            }

            for (var i = 0; i < data.length; i++) {
                if ([END, LST, ESC].indexOf(data[i]) > -1) bytes.push(ESC);
                bytes.push(data[i]);
            }
            bytes.push(END);
        }

        return bytes;
    }

    return (data) => {
        var bytes = encodeValue(data);
        bytes.unshift(LST, END, ESC);
        writer.write(Buffer.from(bytes));
    }
}

function decoder(reader, data) {
    var stack = [];
    var state = 'LST';
    var escaped = false;

    var LST, END, ESC;

    function end(value) {
        if (!stack.length) {
            state = 'LST';
            data(value);
        }
        else stack[stack.length - 1].push(value)
    }

    reader.onData(buffer => {
        for (var i = 0; i < buffer.length; i++) {
            var byte = buffer[i];

            if (state == 'LST') {
                LST = byte;
                state = 'END'

            } else if (state == 'END') {
                END = byte;
                state = 'ESC';

            } else if (state == 'ESC') {
                ESC = byte;
                state = 'list';

            } else if (state == 'list') {
                if (!escaped && byte == ESC) {
                    escaped = true;
                } else if (!escaped && byte == LST) {
                    stack.push([]);
                } else if (!escaped && byte == END) {
                    end(stack.pop());
                } else {
                    stack.push([byte]);
                    escaped = false;
                    state = 'value';
                }

            } else if (state == 'value') {
                if (!escaped && byte == ESC) {
                    escaped = true;
                } else if (!escaped && byte == END) {
                    end(Buffer.from(stack.pop()));
                    state = 'list';
                } else {
                    stack[stack.length - 1].push(byte);
                    escaped = false;
                }
            }
        }
    })
}

var toNumber = (buffer) => {
    if (Buffer.isBuffer(buffer)) {
        if (!buffer.length) return 0;
        var number = 0;
        for (var i = 0; i < buffer.length; i++) {
            number += buffer[i];
            number = number << 8;
        }
        number = number >> 8;
        return number;

    } else if (!buffer.length) {
        return 0;
    } else if (buffer[0] == '-') {
        return -toNumber(buffer[1]);
    } else if (buffer[0] == '/') {
        return toNumber(buffer[1]) / toNumber(buffer[2])
    }
};

var toObject = function (buffer) {
    var keys = buffer[0];
    var values = buffer[1];
    var object = {};
    for (var i = 0; i < keys.length; i++) {
        object[keys[i]] = values[i]
    }
    return object;
};

module.exports = {
    Encoder: encoder,
    Decoder: decoder,
    translate: {
        toNumber: toNumber,
        toObject: toObject
    }
};