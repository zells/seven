function Communicator(name) {
    var id = 'communicator_' + Math.floor(Math.random() * 10000000);
    window[id] = this;

    this.name = name || 'com' + Math.floor(Math.random() * 10000);

    this.transmit = function () {
    };
    this.remove = function () {
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

        if (signal.content.substr(0, 1) == '{') {
            try {
                signal.content = JSON.parse(signal.content);
            } catch (e) {
            }
        }

        that.transmit(signal);
        return false;
    });

    this.element.find('.name').on('keyup', function (event) {
        that.name = $(event.target).val();
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
            onmove: that.dragMoveListener
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
};

module.exports = Communicator;