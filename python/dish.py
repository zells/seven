# dish

import sys
from src.Dish import Dish
from src.peers.ClientSocketPeer import ClientSocketPeer
from src.zells.TurtleZell import TurtleZell

dish = Dish()

dish.zells.append(TurtleZell('shelly', dish.transmit))

if len(sys.argv) == 2:
    dish.join(ClientSocketPeer(int(sys.argv[1]), 'localhost'))

while 1:
    msg = sys.stdin.readline().strip().split(' ')
    if msg[0] == 'exit':
        sys.exit()
    elif msg[0] == 'join':
        if len(msg) == 2:
            dish.join(ClientSocketPeer(int(msg[1]), 'localhost'))
        elif len(msg) == 3:
            dish.join(ClientSocketPeer(int(msg[1]), msg[2]))
    else:
        print "Unknown command"
