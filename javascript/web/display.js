var encoding = require('../src/encoding');

function Display(width, height, name) {
    var id = 'display_' + Math.floor(Math.random() * 10000000);

    width = width || 400;
    height = height || 200;
    name = name || 'display' + Math.floor(Math.random() * 10000);

    this.signals = {};
    this.transmit = function () {
    };
    this.remove = function () {
    };
    this.onChange = function () {
    };

    this.layers = [];

    this.name = name;

    this.cx = width / 2;
    this.cy = width / 2;

    this.zoomLevel = 0;
    this.dotsPerPixelLevel = 0;

    this.element = $(this.render(id, width, height));
    $('body').append(this.element);

    this.$canvas = this.element.find('canvas');
    this.$receiver = this.element.find('input.receiver');
    this.$zoomValue = this.element.find('.zoom-control .value');
    this.$resolutionValue = this.element.find('.resolution-control .value');

    this.registerClickHandlers();
    this.registerDragHandler(id);
    this.registerScrollHandler();

    this.$receiver.on('keyup', (function () {
        this.onChange();
    }).bind(this))
}

Display.prototype.render = function (id, width, height) {
    return '' +
        '<div id="' + id + '" class="display-container resize-drag card text-center" style="width: ' + (width + 80) + 'px; position:absolute;">' +
        ' <div class="card-header">' +
        '   <button type="button" class="close"><span aria-hidden="true">×</span></button>' +
        '   <strong>' + this.name + '</strong>' +
        ' </div>' +
        ' <div class="card-body">' +
        '<div id="' + id + '-canvas" style="background-color: #000; border-radius: 8px; padding: 20px; box-sizing: border-box;">' +
        ' <canvas width="' + width + '" height="' + height + '">Your browser does not support the HTML5 canvas tag.</canvas>' +
        '</div>' +
        ' </div>' +
        ' <div class="card-footer text-muted">' +
        ' <div style="color: black;">' +
        '   <input placeholder="receiver" class="receiver" style="width: 10em;">' +
        '   <button class="btn btn-secondary btn-sm draw-here">draw here</button>' +
        '   &nbsp;&nbsp;|&nbsp;&nbsp;' +
        '   <span class="zoom-control">Zoom' +
        '     <button class="btn btn-secondary btn-sm less">-</button>' +
        '     <span class="badge badge-secondary value" style="width: 2em;">1</span>' +
        '     <button class="btn btn-secondary btn-sm more">+</button>' +
        '   </span>' +
        '   &nbsp;&nbsp;|&nbsp;&nbsp;' +
        '   <span class="resolution-control">Resolution' +
        '     <button class="btn btn-secondary btn-sm less">-</button>' +
        '     <span class="badge badge-secondary value" style="width: 3em;">1</span>' +
        '     <button class="btn btn-secondary btn-sm more">+</button>' +
        '   </span>' +
        ' </div>' +
        ' </div>' +
        '</div>'
};

Display.prototype.registerClickHandlers = function () {
    this.element.find('button.draw-here').on('click', this.drawHere.bind(this));

    this.element.find('.zoom-control button.less').on('click', (function () {
        this.changeZoom(-.1)
    }).bind(this));
    this.element.find('.zoom-control button.more').on('click', (function () {
        this.changeZoom(.1)
    }).bind(this));

    this.element.find('.resolution-control button.less').on('click', (function () {
        this.changeZoom(0, .1)
    }).bind(this));
    this.element.find('.resolution-control button.more').on('click', (function () {
        this.changeZoom(0, -.1)
    }).bind(this));

    this.element.find('.close').on('click', (function () {
        this.element.remove();
        this.remove();
    }).bind(this));
};

Display.prototype.registerDragHandler = function (id) {
    interact('#' + id)
        .draggable({
            onmove: this.dragMoveListener.bind(this)
        });

    interact('#' + id + '-canvas')
        .resizable({
            preserveAspectRatio: false,
            edges: {left: true, right: true, bottom: true, top: true}
        })
        .on('resizemove', this.resizeMove.bind(this));
};

Display.prototype.dragMoveListener = function (event) {
    if (event.buttons == 4) {
        this.cx += event.dx;
        this.cy += event.dy;
        this.redraw();
        return;
    }

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

Display.prototype.resizeMove = function (event) {
    var target = event.target,
        x = (parseFloat(target.getAttribute('data-x')) || 0),
        y = (parseFloat(target.getAttribute('data-y')) || 0);

    // update the element's style
    target.style.width = event.rect.width + 'px';
    target.style.height = event.rect.height + 'px';

    // translate when resizing from top or left edges
    x += event.deltaRect.left;
    y += event.deltaRect.top;

    target.style.webkitTransform = target.style.transform =
        'translate(' + x + 'px,' + y + 'px)';

    target.setAttribute('data-x', x);
    target.setAttribute('data-y', y);

    var c = this.$canvas[0];
    c.width = target.style.width.substr(0, target.style.width.length - 2) - 40;
    c.height = target.style.height.substr(0, target.style.height.length - 2) - 40;

    this.element.width(c.width + 80);

    this.cx -= event.deltaRect.left;
    this.cy -= event.deltaRect.top;

    this.drawHere();
    this.redraw();

    this.onChange();
};

Display.prototype.registerScrollHandler = function () {
    var c = this.$canvas[0];
    if (c.addEventListener) {
        c.addEventListener("mousewheel", this.mouseWheelHandler.bind(this), false); // IE9, Chrome, Safari, Opera
        c.addEventListener("DOMMouseScroll", this.mouseWheelHandler.bind(this), false); // Firefox
    } else {
        c.attachEvent("onmousewheel", this.mouseWheelHandler.bind(this)); // IE 6/7/8
    }
};

Display.prototype.mouseWheelHandler = function (e) {
    // cross-browser wheel delta
    var e = window.event || e; // old IE support
    var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));

    if (e.altKey) {
        var f = [e.layerX - 40 - this.cx, -(e.layerY - 40 - this.cy)];

        if (e.shiftKey) {
            this.changeZoom(0, -delta / 10, f)
        } else {
            this.changeZoom(delta / 10, 0, f)
        }
    }

    return false;
};

Display.prototype.changeZoom = function (zoomLevelDelta, dotsPerPixelDelta, centerPoint) {
    var c = this.$canvas[0];

    zoomLevelDelta = zoomLevelDelta || 0;
    dotsPerPixelDelta = dotsPerPixelDelta || 0;
    centerPoint = centerPoint || [c.width / 2 - this.cx, -c.height / 2 + this.cy];

    var zoomBefore = this.totalZoom();

    this.zoomLevel = Math.max(this.zoomLevel + zoomLevelDelta, 0);
    this.dotsPerPixelLevel += dotsPerPixelDelta;

    var proportionalTranslation = 1 - this.totalZoom() / zoomBefore;
    this.cx += centerPoint[0] * proportionalTranslation;
    this.cy -= centerPoint[1] * proportionalTranslation;

    if (dotsPerPixelDelta != 0) {
        this.drawHere();
    }
    this.redraw();

    this.$zoomValue.html(Math.round(this.zoomFactor() * 10) / 10);
    this.$resolutionValue.html(Math.round(this.dotsPerPixel() * 100) / 100);

    this.onChange();
};

Display.prototype.displaySize = function () {
    return {
        width: this.$canvas.width() * this.dotsPerPixel(),
        height: this.$canvas.height() * this.dotsPerPixel()
    };
};

Display.prototype.dotsPerPixel = function () {
    return Math.pow(10, this.dotsPerPixelLevel);
};

Display.prototype.zoomFactor = function () {
    return Math.pow(10, this.zoomLevel);
};

Display.prototype.totalZoom = function () {
    return this.zoomFactor() / this.dotsPerPixel();
};

Display.prototype.drawHere = function () {
    if (this.signals['drawHere']) {
        return;
    }

    if (!this.$receiver.val()) {
        return;
    }

    var that = this;
    this.signals['drawHere'] = setTimeout(function () {
        console.log('########### transmit');
        that.transmit({
            to: that.$receiver.val(),
            content: {drawOn: that.name, size: that.displaySize()},
        });
        delete that.signals['drawHere']
    }, 100);
};

Display.prototype.redraw = function () {
    var that = this;

    var displaySize = this.displaySize();

    var zoom = this.totalZoom();

    var margins = {
        left: displaySize.width * zoom / 2 - this.cx,
        right: displaySize.width * zoom / 2 - (this.$canvas.width() - this.cx),
        top: displaySize.height * zoom / 2 - this.cy,
        bottom: displaySize.height * zoom / 2 - (this.$canvas.height() - this.cy)
    };

    if (margins.left < 0) this.cx += margins.left;
    if (margins.right < 0) this.cx -= margins.right;
    if (margins.top < 0) this.cy += margins.top;
    if (margins.bottom < 0) this.cy -= margins.bottom;

    var ctx = this.$canvas[0].getContext("2d");
    ctx.clearRect(0, 0, this.$canvas.width(), this.$canvas.height());

    this.layers.forEach(function (strokes) {
        strokes.forEach(function (stroke) {
            if ('line' in stroke) {
                var line = encoding.translate.toObject(stroke.line);

                var width = encoding.translate.toNumber(line.width);
                var color = encoding.translate.toObject(line.color);
                var from = encoding.translate.toObject(line.from);
                var to = encoding.translate.toObject(line.to);

                ctx.lineWidth = width * zoom;
                ctx.strokeStyle = 'rgb(' + encoding.translate.toNumber(color.r) * 255 + ', '
                    + encoding.translate.toNumber(color.g) * 2555 + ', '
                    + encoding.translate.toNumber(color.b) * 255 + ')';
                ctx.beginPath();
                ctx.moveTo(that.cx + encoding.translate.toNumber(from.x) * zoom, that.cy - encoding.translate.toNumber(from.y) * zoom);
                ctx.lineTo(that.cx + encoding.translate.toNumber(to.x) * zoom, that.cy - encoding.translate.toNumber(to.y) * zoom);
                ctx.stroke();
            }
        });
    })
};

Display.prototype.receive = function (encoded) {
    var transmit = encoding.Encoder({
        write: (function (data) {
            this.transmit(data);
        }).bind(this)
    });

    encoding.Decoder({
        onData: (function (callback) {
            callback(encoded)
        })
    }, (function (signal) {
        signal = encoding.translate.toObject(signal);

        if (signal.to.toString() == '?') {
            transmit({
                to: signal.from.toString(),
                from: this.name,
                content: [{
                    to: this.name,
                    from: signal.from.toString(),
                    content: '?'
                }]
            })
        }

        if (signal.to.toString() != this.name) {
            return
        }

        if (signal.content.toString() == '?')
            transmit({
                to: signal.from.toString(),
                from: this.name,
                content: [{
                    to: this.name,
                    from: signal.from.toString(),
                    content: {
                        draw: {
                            layer: 0,
                            strokes: [
                                'clear',
                                {
                                    line: {
                                        width: 2,
                                        color: {r: 0.5, g: 0, b: 1},
                                        from: {x: -100, y: -100},
                                        to: {x: 100, y: 100}
                                    }
                                }
                            ]
                        }
                    }
                }]
            });

        var content = encoding.translate.toObject(signal.content);
        if ('draw' in content) {
            var draw = encoding.translate.toObject(content.draw);

            var layer = encoding.translate.toNumber(draw.layer);
            if (!(layer in this.layers)) {
                this.layers[layer] = [];
            }

            if ('strokes' in draw) {
                draw.strokes.forEach((function (stroke) {
                    if (stroke.toString() == 'clear') {
                        this.layers[layer] = [];
                    } else {
                        this.layers[layer].push(encoding.translate.toObject(stroke))
                    }
                }).bind(this))
            }

            this.redraw();
        }
    }).bind(this))
};

Display.prototype.restore = function (state) {
    this.element.css('webkitTransform', state.transform);
    this.element.css('transform', state.transform);
    this.element.attr('data-x', state.x);
    this.element.attr('data-y', state.y);

    this.$receiver.val(state.receiver);
    this.zoomLevel = state.zoomLevel;
    this.dotsPerPixelLevel = state.dotsPerPixelLevel;
    this.changeZoom(0, 0);
    this.drawHere();

    return this;
};

Display.prototype.state = function () {
    return {
        transform: this.element.css('transform'),
        x: this.element.attr('data-x'),
        y: this.element.attr('data-y'),

        receiver: this.$receiver.val(),
        zoomLevel: this.zoomLevel,
        dotsPerPixelLevel: this.dotsPerPixelLevel
    };
};

Display.prototype.serialize = function () {
    return '(new Display(' + this.$canvas[0].width + ', ' + this.$canvas[0].height + ', "' + this.name + '")).restore(' + JSON.stringify(this.state()) + ')';
};

module.exports = Display;