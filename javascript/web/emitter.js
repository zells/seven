function Emitter(signal, caption) {
    caption = caption || JSON.stringify(signal);

    var id = 'emitter_' + Math.floor(Math.random() * 10000000);

    this.transmit = function () {
    };

    if (!window.emitters) window.emitters = {};
    window.emitters[this.id] = this;

    $('body').append(this.render(id, caption));

    var that = this;
    interact('#' + id)
        .draggable({
            onmove: that.dragMoveListener
        })
        .on('tap', function (event) {
            that.transmit(signal);
            event.preventDefault();
        });
}

Emitter.prototype.receive = function () {
};

Emitter.prototype.render = function (id, caption) {
    return '<div class="emitter" id="' + id + '" style="background-color: #2f65a4;color: black;font-family: sans-serif;border-radius: 8px;padding: 20px;margin: 30px 20px;width: 200px; box-sizing: border-box;">' + caption + '</div>'
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