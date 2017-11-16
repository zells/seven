import socket
import select
import msgpack
from thread import start_new_thread


class ClientSocketPeer:
    whenReceive = lambda: None
    whenClose = lambda: None

    def __init__(self, port, host):
        print "connect " + host + ":" + str(port)

        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        try:
            self.socket.connect((host, port))
            start_new_thread(self.thread, ())
        except:
            print 'Unable to connect'

    def onReceive(self, then):
        self.whenReceive = then

    def onClose(self, then):
        self.whenClose = then

    def receive(self, id, signal):
        self.socket.send(msgpack.packb(['transmit', id, signal]))

    def thread(self):
        unpacker = msgpack.Unpacker()

        while True:
            unpacker.feed(self.socket.recv(1))
            for transmission in unpacker:
                if transmission[0] == 'join':
                    pass
                if transmission[0] == 'transmit':
                    self.whenReceive(transmission[1], transmission[2])
                else:
                    print "Unknown transmission"
