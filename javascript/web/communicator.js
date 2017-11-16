function Communicator(name) {
    var id = 'communicator_' + Math.floor(Math.random() * 10000000);
    window[id] = this;

    this.name = name || '';

    this.transmit = function () {
    };

    this.element = $(this.render(id, this.name));

    $('body').append(this.element);

    var that = this;
    this.element.find('form').submit(function () {
        that.transmit({
            to: that.element.find('input.receiver').val(),
            from: that.name,
            content: that.element.find('input.message').val()
        });
        that.element.find('input.message').val('');
        return false;
    });

    this.element.find('.name').on('keyup', function (event) {
        that.name = $(event.target).val();
    });

    interact('#' + id)
        .draggable({
            onmove: that.dragMoveListener
        });
}

Communicator.prototype.render = function (id, name) {
    return '<div id="' + id + '" class="card text-center w-25" style="position: absolute">' +
        ' <div class="card-header"><input class="name" value="' + name + '"></div>' +
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
        '         <button type="submit" class="btn btn-primary">Send</button>' +
        '       </div>' +
        '     </div>' +
        '   </form>' +
        ' </div>' +
        '</div>'
};

Communicator.prototype.receive = function (signal) {
    if (!this.name || signal.to == this.name || signal.from == this.name) {
        this.element.find('ul').prepend('<li class="list-group-item">' +
            '<div><small>' + new Date().toISOString() + '</small></div>' +
            JSON.stringify(signal) +
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