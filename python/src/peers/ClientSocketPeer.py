import time
import socket
import select
import msgpack
from thread import start_new_thread


class ClientSocketPeer:
    socket = None
    whenReceive = lambda signal: None
    whenClose = lambda: None

    def __init__(self, port, host):
        self.port = port
        self.host = host

    def open(self):

        tried = 0
        while self.socket is None:
            if tried > 10:
                self.whenClose()
                break

            try:
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.socket.connect((self.host, self.port))
                start_new_thread(self.thread, ())
                print "Connected to " + self.host + ':' + str(self.port)

            except:
                print "Reconnect"
                self.socket = None
                time.sleep(1)
                tried += 1

    def onReceive(self, then):
        self.whenReceive = then

    def onClose(self, then):
        self.whenClose = then

    def receive(self, id, signal):
        self.socket.send(msgpack.packb(['transmit', id, signal]))

    def thread(self):
        unpacker = msgpack.Unpacker()

        while self.socket is not None:
            ready_to_read, ready_to_write, in_error = select.select([self.socket], [], [])

            for sock in ready_to_read:
                if sock == self.socket:
                    nextByte = sock.recv(1)
                    if not nextByte:
                        self.socket = None
                        break

                    unpacker.feed(nextByte)
                    for transmission in unpacker:
                        if transmission[0] == 'join':
                            pass
                        if transmission[0] == 'transmit':
                            self.whenReceive(transmission[1], transmission[2])
                        else:
                            print "Unknown transmission"

        self.open()
