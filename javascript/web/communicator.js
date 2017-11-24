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

        that.transmit(signal);
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

Communicator.prototype.receive = function (signal) {
    if (!this.name || signal.to == this.name || signal.to == '' || signal.from == this.name) {
        var type = 'light';
        if (signal.to == this.name) type = 'primary';
        if (signal.from == this.name) type = 'secondary';

        this.element.find('ul').prepend('<li class="text-left list-group-item list-group-item-' + type + '">' +
            '<div><small>' + new Date().toISOString() + '</small></div>' +
            '<pre>' + JSON.stringify(signal, null, 2) + '</pre>' +
            '</li>')
    }
};

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