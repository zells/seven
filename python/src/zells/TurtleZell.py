import math


class TurtleZell(object):
    def __init__(self, name, emit):
        self.name = name
        self.emit = emit

        self.position = [0, 0]
        self.angle = 90
        self.segments = []
        self.canvases = {}

        print "Created Turtle " + name

    def receive(self, signal):
        if signal['to'] == '?':
            self.emit({
                'to': signal['from'],
                'content': [{
                    'to': self.name,
                    'from': signal['from'],
                    'content': '?'
                }]
            })

        if signal['to'] != self.name:
            return

        if '?' == signal['content']:
            self.emit({
                'to': signal['from'],
                'content': [
                    {
                        "to": self.name,
                        "content": {
                            "drawOn": "a display",
                            "size": {"width": 400, "height": 200}
                        }
                    }, {
                        'to': self.name,
                        'content': {'go': 'forward'}
                    }, {
                        'to': self.name,
                        'content': {'turn': 'left'}
                    }, {
                        'to': self.name,
                        'content': {'turn': 'right'}
                    }, {
                        'to': self.name,
                        'content': 'reset'
                    }]
            })

        if 'drawOn' in signal['content']:
            self.canvases[signal['content']['drawOn']] = signal['content']['size']
            self.draw()

        if 'go' in signal['content']:
            first = [self.position[0], self.position[1]]
            self.position[0] += math.cos(math.radians(self.angle))
            self.position[1] += math.sin(math.radians(self.angle))
            self.segments.append([first, [self.position[0], self.position[1]]])
            self.draw()

        if 'turn' in signal['content']:
            if signal['content']['turn'] == 'right':
                self.angle -= 15
            else:
                self.angle += 15
            self.draw()

        if 'reset' == signal['content']:
            self.position = [0, 0]
            self.angle = 90
            self.segments = []
            self.draw()

    def draw(self):
        for canvas in self.canvases:

            size = self.canvases[canvas]
            stepX = size['width'] / 100
            stepY = size['height'] / 100

            point1x = round(self.position[0] * stepX + math.cos(math.radians(self.angle + 90)) * 10, 2)
            point1y = round(self.position[1] * stepY + math.sin(math.radians(self.angle + 90)) * 10, 2)
            point2x = round(self.position[0] * stepX + math.cos(math.radians(self.angle - 90)) * 10, 2)
            point2y = round(self.position[1] * stepY + math.sin(math.radians(self.angle - 90)) * 10, 2)
            point3x = round(self.position[0] * stepX + math.cos(math.radians(self.angle + 0)) * 20, 2)
            point3y = round(self.position[1] * stepY + math.sin(math.radians(self.angle + 0)) * 20, 2)

            strokes = []
            strokes.append('clear')

            for segment in self.segments:
                strokes.append({'line': {'from': {'x': round(segment[0][0] * stepX, 2), 'y': round(segment[0][1] * stepY, 2)},
                                         'to': {'x': round(segment[1][0] * stepX, 2), 'y': round(segment[1][1] * stepY, 2)},
                                         'color': {'r': 1, 'g': 1, 'b': 1},
                                         'width': 1}})

            strokes.append({'line': {'from': {'x': point1x, 'y': point1y},
                                     'to': {'x': point2x, 'y': point2y},
                                     'color': {'r': 0, 'g': 1, 'b': 0},
                                     'width': 1}})
            strokes.append({'line': {'from': {'x': point2x, 'y': point2y},
                                     'to': {'x': point3x, 'y': point3y},
                                     'color': {'r': 0, 'g': 1, 'b': 0},
                                     'width': 1}})
            strokes.append({'line': {'from': {'x': point3x, 'y': point3y},
                                     'to': {'x': point1x, 'y': point1y},
                                     'color': {'r': 0, 'g': 1, 'b': 0},
                                     'width': 1}})

            self.emit({
                'to': canvas,
                'content': {'draw': {'strokes': strokes}}})
