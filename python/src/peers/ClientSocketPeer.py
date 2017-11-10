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
        self.socket.settimeout(2)

        try:
            self.socket.connect((host, port))
        except:
            print 'Unable to connect'

        start_new_thread(self.thread, ())

    def onReceive(self, then):
        self.whenReceive = then

    def onClose(self, then):
        self.whenClose = then

    def receive(self, id, signal):
        self.socket.send(msgpack.packb(['transmit', id, signal]))

    def thread(self):
        while 1:
            ready_to_read, ready_to_write, in_error = select.select([self.socket], [], [])

            for sock in ready_to_read:
                if sock == self.socket:
                    # incoming message from remote server, s
                    data = sock.recv(4096)
                    if not data:
                        self.whenClose()
                        return
                    else:
                        transmission = msgpack.unpackb(data)

                        if transmission[0] == 'join':
                            pass
                        if transmission[0] == 'transmit':
                            self.whenReceive(transmission[1], transmission[2])
                        else:
                            print "Unknown transmission"