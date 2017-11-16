function Display(name, width, height) {
    width = width || 400;
    height = height || 200;

    this.signals = {};
    this.transmit = function () {
    };

    if (!window.displays) window.displays = {};
    window.displays[name] = this;

    this.layers = [];

    this.name = name;

    this.cx = width / 2;
    this.cy = width / 2;

    this.zoomLevel = 0;
    this.dotsPerPixelLevel = 0;

    $('body').append(this.render(width, height));

    this.$canvas = $('#' + name + ' canvas');
    this.$receiver = $('#' + name + ' .receiver-value');
    this.$zoomValue = $('#' + name + ' .zoom-value');
    this.$resolutionValue = $('#' + name + ' .resolution-value');

    this.registerDragHandler();
    this.registerScrollHandler();
}

Display.prototype.registerDragHandler = function () {
    interact('#' + this.name)
        .draggable({
            onmove: this.dragMoveListener.bind(this)
        })
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

    this.cx -= event.deltaRect.left;
    this.cy -= event.deltaRect.top;

    this.drawHere();
    this.redraw();
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
    } else {
        this.redraw();
    }
};

Display.prototype.render = function (width, height) {
    return '<div id="' + this.name + '" class="display-container resize-drag" style="width: ' + (width + 40) + 'px; height: ' + (height + 40) + 'px; background-color: #000; color: white; font-family: sans-serif; border-radius: 8px; padding: 20px; margin: 30px 20px; box-sizing: border-box; position: absolute;">' +
        ' <canvas width="' + width + '" height="' + height + '">Your browser does not support the HTML5 canvas tag.</canvas>' +
        ' <div style="color: black; top: 30px; position: relative;">' +
        '   <strong>' + this.name + '</strong>' +
        '   <input placeholder="receiver" class="receiver-value" style="width: 10em;"><button onclick="displays[\'' + this.name + '\'].drawHere()">draw here</button>' +
        '   &nbsp;&nbsp;|&nbsp;&nbsp;' +
        '   Zoom<button onclick="changeZoom(this, -.1)">-</button><span class="zoom-value">1</span><button onclick="changeZoom(this, .1)">+</button>' +
        '   &nbsp;&nbsp;|&nbsp;&nbsp;' +
        '   Resolution<button onclick="changeZoom(this, 0, -.1)">-</button><span class="resolution-value">1</span><button onclick="changeZoom(0, .1)">+</button>' +
        ' </div>' +
        '</div>';
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

    this.$zoomValue.html(Math.round(this.zoomFactor() * 10) / 10);
    this.$resolutionValue.html(Math.round(this.dotsPerPixel() * 100) / 100);

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
                ctx.lineWidth = stroke.line.width * zoom;
                ctx.strokeStyle = 'rgb(' + stroke.line.color.r * 255 + ', '
                    + stroke.line.color.g * 2555 + ', '
                    + stroke.line.color.b * 255 + ')';
                ctx.beginPath();
                ctx.moveTo(that.cx + stroke.line.from.x * zoom, that.cy - stroke.line.from.y * zoom);
                ctx.lineTo(that.cx + stroke.line.to.x * zoom, that.cy - stroke.line.to.y * zoom);
                ctx.stroke();
            }
        });
    })
};

Display.prototype.receive = function (signal) {
    var that = this;

    if (signal.to != that.name) {
        return
    }

    if ('draw' in signal.content) {
        var layer = signal.content.draw.layer || 0;
        if (!(layer in that.layers)) {
            that.layers[layer] = [];
        }

        if ('strokes' in signal.content.draw) {
            signal.content.draw.strokes.forEach(function (stroke) {
                if (stroke == 'clear') {
                    that.layers[layer] = [];
                } else {
                    that.layers[layer].push(stroke)
                }
            })
        }

        that.redraw();
    }
};

module.exports = Display;