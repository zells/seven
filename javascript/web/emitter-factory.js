function EmitterFactory(dish) {
    var id = 'emitter_factory_' + Math.floor(Math.random() * 10000000);

    $('body').append(this.render(id));

    interact('#' + id, {
        ignoreFrom: 'form'
    }).draggable({
        onmove: function (event) {
            var target = event.target,
                // keep the dragged position in the data-x/data-y attributes
                x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
                y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;

            // translate the element
            target.style.webkitTransform =
                target.style.transform =
                    'translate(' + x + 'px, ' + y + 'px)';

            // update the posiion attributes
            target.setAttribute('data-x', x);
            target.setAttribute('data-y', y);
        }
    });

    var $emitterFactory = $('#' + id);
    $emitterFactory.find('form').submit(function () {
        try {
            dish.put(new Emitter(
                JSON.parse($emitterFactory.find('input.signal').val()),
                $emitterFactory.find('input.caption').val()));
        } catch (e) {
            alert(e);
        }

        return false;
    });

    $emitterFactory.find('.close').on('click', (function () {
        $emitterFactory.remove();
    }));
}

EmitterFactory.prototype.receive = function () {
};

EmitterFactory.prototype.render = function (id) {
    return '' +
        '<div id="' + id + '" class="card text-center w-50">' +
        ' <div class="card-header">' +
        '   <button type="button" class="close"><span aria-hidden="true">×</span></button>' +
        ' </div>' +
        ' <div class="card-body">' +
        '    <form>' +
        '    <div class="form-row">' +
        '      <div class="col col-md-2"><input class="caption form-control" placeholder="caption" type="text"></div>' +
        '      <div class="col col-md-8"><input class="signal form-control" placeholder="signal" type="text"></div>' +
        '      <div class="col col-md-2">' +
        '        <button type="submit" class="btn btn-primary">Create Emitter</button>' +
        '      </div>' +
        '    </div>' +
        '    </form>' +
        ' </div>' +
        '</div>'
};

EmitterFactory.prototype.dragMoveListener = function (event) {
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

module.exports = EmitterFactory;