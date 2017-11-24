function Emitter(signal, caption) {
    var id = 'emitter_' + Math.floor(Math.random() * 10000000);

    this.transmit = function () {
    };
    this.remove = function () {
    };
    this.onChange = function () {
    };

    caption = caption || JSON.stringify(signal);

    this.signal = signal;
    this.caption = caption;

    this.element = $(this.render(id, caption));
    $('body').append(this.element);

    interact('#' + id)
        .draggable({
            onmove: this.dragMoveListener.bind(this)
        });

    var $emitter = this.element;
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
        '<div class="btn btn-success btn-lg" id="' + id + '" style="position:absolute;">' +
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

    this.onChange();
};

Emitter.prototype.restore = function (state) {
    this.element.css('webkitTransform', state.transform);
    this.element.css('transform', state.transform);
    this.element.attr('data-x', state.x);
    this.element.attr('data-y', state.y);

    return this;
};

Emitter.prototype.state = function () {
    return {
        transform: this.element.css('transform'),
        x: this.element.attr('data-x'),
        y: this.element.attr('data-y')
    };
};

Emitter.prototype.serialize = function () {
    return '(new Emitter("' + this.signal + '", "' + this.caption + '")).restore(' + JSON.stringify(this.state()) + ')';
};

module.exports = Emitter;