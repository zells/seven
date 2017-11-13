import math


class TurtleZell(object):
    position = [0, 0]
    angle = 90
    segments = []
    canvases = {}

    def __init__(self, name, emit):
        self.name = name
        self.emit = emit

    def receive(self, signal):
        if signal['to'] != self.name:
            return

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

    def draw(self):
        for canvas in self.canvases:

            size = self.canvases[canvas]
            stepX = size['width'] / 100
            stepY = size['height'] / 100

            point1x = self.position[0] * stepX + math.cos(math.radians(self.angle + 90)) * 10
            point1y = self.position[1] * stepY + math.sin(math.radians(self.angle + 90)) * 10
            point2x = self.position[0] * stepX + math.cos(math.radians(self.angle - 90)) * 10
            point2y = self.position[1] * stepY + math.sin(math.radians(self.angle - 90)) * 10
            point3x = self.position[0] * stepX + math.cos(math.radians(self.angle + 0)) * 20
            point3y = self.position[1] * stepY + math.sin(math.radians(self.angle + 0)) * 20

            strokes = []
            strokes.append('clear')

            for segment in self.segments:
                strokes.append({'line': {'from': {'x': segment[0][0] * stepX, 'y': segment[0][1] * stepY},
                                         'to': {'x': segment[1][0] * stepX, 'y': segment[1][1] * stepY},
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