function Emitter(signal, caption) {
    var id = 'emitter_' + Math.floor(Math.random() * 10000000);

    this.transmit = function () {
    };

    caption = caption || JSON.stringify(signal);

    $('body').append(this.render(id, caption));

    interact('#' + id)
        .draggable({
            onmove: this.dragMoveListener
        });

    var $emitter = $('#' + id);
    $emitter.find('.button').on('click', (function (event) {
        this.transmit(signal);
        event.preventDefault();
    }).bind(this));
    $emitter.find('.close').on('click', (function () {
        $emitter.remove();
    }));
}

Emitter.prototype.receive = function () {
};

Emitter.prototype.render = function (id, caption) {
    return '' +
        '<div class="btn btn-success btn-lg" id="' + id + '">' +
        '  <button type="button" class="close" style="margin-left: 1em"><span aria-hidden="true">×</span></button>' +
        '  <span class="button">' + caption + '</span>' +
        '</div>'
};

Emitter.prototype.dragMoveListener = function (event) {
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

module.exports = Emitter;