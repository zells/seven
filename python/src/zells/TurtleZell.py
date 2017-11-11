import math


class TurtleZell(object):
    position = [0, 0]
    angle = 90
    canvas = None

    def __init__(self, name, emit):
        self.name = name
        self.emit = emit

    def receive(self, signal):
        if signal['to'] != self.name:
            return

        if 'drawOn' in signal['content']:
            self.canvas = signal['content']['drawOn']
            self.draw()

        if 'go' in signal['content']:
            first = [self.position[0], self.position[1]]
            self.position[0] += math.cos(math.radians(self.angle)) * 10
            self.position[1] += math.sin(math.radians(self.angle)) * 10
            self.drawSegment(first, self.position)
            self.draw()

        if 'turn' in signal['content']:
            if signal['content']['turn'] == 'right':
                self.angle -= 15
            else:
                self.angle += 15
            self.draw()

    def drawSegment(self, p1, p2):
        self.emit({
            'to': self.canvas,
            'content': {'draw': {
                'layer': 0,
                'strokes': [{'line': {'from': {'x': p1[0], 'y': p1[1]},
                                      'to': {'x': p2[0], 'y': p2[1]},
                                      'color': {'r': 1, 'g': 1, 'b': 1},
                                      'width': 1}}]}}})

    def draw(self):
        point1x = self.position[0] + math.cos(math.radians(self.angle + 90)) * 10
        point1y = self.position[1] + math.sin(math.radians(self.angle + 90)) * 10
        point2x = self.position[0] + math.cos(math.radians(self.angle - 90)) * 10
        point2y = self.position[1] + math.sin(math.radians(self.angle - 90)) * 10
        point3x = self.position[0] + math.cos(math.radians(self.angle + 0)) * 20
        point3y = self.position[1] + math.sin(math.radians(self.angle + 0)) * 20

        self.emit({
            'to': self.canvas,
            'content': {'draw': {
                'layer': 1,
                'strokes': ['clear',
                            {'line': {'from': {'x': point1x, 'y': point1y},
                                      'to': {'x': point2x, 'y': point2y},
                                      'color': {'r': 0, 'g': 1, 'b': 0},
                                      'width': 1}},
                            {'line': {'from': {'x': point2x, 'y': point2y},
                                      'to': {'x': point3x, 'y': point3y},
                                      'color': {'r': 0, 'g': 1, 'b': 0},
                                      'width': 1}},
                            {'line': {'from': {'x': point3x, 'y': point3y},
                                      'to': {'x': point1x, 'y': point1y},
                                      'color': {'r': 0, 'g': 1, 'b': 0},
                                      'width': 1}}]}}})
