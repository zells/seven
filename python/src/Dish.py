import uuid
from zells.PrintZell import PrintZell


class Dish:
    def __init__(self):
        self.peerId = 0
        self.received = []
        self.peers = {}
        self.zells = [PrintZell()]

    def transmit(self, signal):
        self.receive(uuid.uuid4().hex, signal)

    def join(self, peer):
        self.peerId += 1
        peerId = self.peerId

        self.peers[peerId] = peer
        peer.onReceive(self.receive)
        peer.onClose(lambda: self.leave(peerId))

        print "join " + str(peerId)
        peer.open()

    def receive(self, id, signal):
        print "receive " + str(id)

        if str(id) in self.received:
            return
        self.received.append(str(id))

        for zell in self.zells:
            try:
                zell.receive(signal)
            except:
                pass

        for peerId in self.peers:
            self.peers[peerId].receive(id, signal)

    def leave(self, peerId):
        print "leave " + str(peerId)
        self.peers.pop(peerId, None)
