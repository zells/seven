var encoding = require('../src/encoding');

function Communicator(name) {
    var id = 'communicator_' + Math.floor(Math.random() * 10000000);
    window[id] = this;

    this.name = name || 'com' + Math.floor(Math.random() * 10000);

    this.transmit = function () {
    };
    this.remove = function () {
    };
    this.onChange = function () {
    };

    this.element = $(this.render(id, this.name));

    $('body').append(this.element);

    var encode = encoding.Encoder({
        write: (function (data) {
            this.transmit(data);
        }).bind(this)
    });

    var that = this;
    this.element.find('form').submit(function () {
        var signal = {
            to: that.element.find('input.receiver').val(),
            from: that.name,
            content: that.element.find('input.message').val()
        };

        if (signal.content.substr(0, 1) in {'{': true, '[': true}) {
            try {
                signal.content = JSON.parse(signal.content);
            } catch (e) {
            }
        }

        if (!signal.to && !signal.from) {
            signal = signal.content;
        }

        encode(signal);
        return false;
    });
    this.element.find('input.receiver').on('keyup', function () {
        that.onChange();
    });
    this.element.find('input.message').on('keyup', function () {
        that.onChange();
    });

    this.element.find('.name').on('keyup', function (event) {
        that.name = $(event.target).val();
        that.onChange();
    });

    this.element.find('.close').on('click', (function () {
        this.element.remove();
        this.remove();
    }).bind(this));

    interact('#' + id,
        {
            ignoreFrom: '.card-body'
        })
        .draggable({
            onmove: that.dragMoveListener.bind(this)
        });
}

Communicator.prototype.render = function (id, name) {
    return '' +
        '<div id="' + id + '" class="card text-center" style="position: absolute; width: 500px">' +
        ' <div class="card-header">' +
        '   <button type="button" class="close"><span aria-hidden="true">×</span></button>' +
        '   <input class="name" value="' + name + '">' +
        ' </div>' +
        ' <div class="card-body" style="max-height: 20em; overflow: auto">' +
        '   <ul class="list-group list-group-flush"></ul>' +
        ' </div>' +
        ' <div class="card-footer text-muted">' +
        '   <form>' +
        '     <div class="form-row">' +
        '       <div class="col col-md-2">' +
        '         <input type="text" class="receiver form-control" placeholder="to">' +
        '       </div>' +
        '       <div class="col col-md-8">' +
        '         <input type="text" class="message form-control" placeholder="message">' +
        '       </div>' +
        '       <div class="col col-md-2">' +
        '         <button type="submit" class="btn btn-primary">Emit</button>' +
        '       </div>' +
        '     </div>' +
        '   </form>' +
        ' </div>' +
        '</div>'
};

Communicator.prototype.receive = function (encoded) {
    encoding.Decoder({
        onData: (function (callback) {
            callback(encoded)
        }).bind(this)
    }, (function (signal) {
        var isMessage = Array.isArray(signal) && signal.length == 2 && signal[0].length >= 3;

        var forMe = false;
        if (isMessage) {
            var toIndex = signal[0].map(function (val) {
                return val.toString();
            }).indexOf('to');

            if (toIndex > -1) {
                var to = signal[1][toIndex].toString();
                forMe = !to || !this.name || to == this.name
            }
        }

        if (!isMessage || forMe) {
            var type = 'light';
            if (signal.to == this.name) type = 'primary';
            if (signal.from == this.name) type = 'secondary';

            this.element.find('ul').prepend('<li class="text-left list-group-item list-group-item-' + type + '">' +
                '<div><small>' + new Date().toISOString() + '</small></div>' +
                '<pre>' + print(signal, '') + '</pre>' +
                '</li>')
        }

    }).bind(this));
};

function print(signal, indent) {
    if (Array.isArray(signal)) {
        if (!signal.length) {
            return '';
        }
        if (signal.length == 2 && Array.isArray(signal[0]) && Array.isArray(signal[1]) && signal[0].length == signal[1].length) {
            var pairs = [];
            for (var i = 0; i < signal[0].length; i++) {
                pairs.push('\n' + indent + ' ' + signal[0][i] + ':' + print(signal[1][i], indent + ' ').trim());
            }
            return '\n' + indent + '{' + pairs.join(',') + ' }';
        }

        return '\n' + indent + '[' + signal.map(function (s) {
                return print(s, indent + ' ');
            }).join(',').trim() + ']';
    } else if (Math.min.apply(null, signal) > 31 && Math.max.apply(null, signal) < 127) {
        return signal.toString();
    } else {
        return '0x' + signal.toString('hex');
    }
}

Communicator.prototype.dragMoveListener = function (event) {
    var target = event.target,
        // keep the dragged position in the data-x/data-y attributes
        x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
        y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;

    // translate the element
    target.style.webkitTransform = target.style.transform = 'translate(' + x + 'px, ' + y + 'px)';

    // update the posiion attributes
    target.setAttribute('data-x', x);
    target.setAttribute('data-y', y);

    this.onChange();
};

Communicator.prototype.restore = function (state) {
    this.name = state.name;
    this.element.find('.name').val(state.name);

    this.element.css('webkitTransform', state.transform);
    this.element.css('transform', state.transform);
    this.element.attr('data-x', state.x);
    this.element.attr('data-y', state.y);
    this.element.find('input.receiver').val(state.receiver);
    this.element.find('input.message').val(state.message);

    return this;
};

Communicator.prototype.state = function () {
    return {
        name: this.name,
        transform: this.element.css('transform'),
        x: this.element.attr('data-x'),
        y: this.element.attr('data-y'),
        receiver: this.element.find('input.receiver').val(),
        message: this.element.find('input.message').val()
    };
};

Communicator.prototype.serialize = function () {
    return '(new Communicator("' + this.name + '")).restore(' + JSON.stringify(this.state()) + ')';
};

module.exports = Communicator;