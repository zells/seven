# dish

import sys
from src.Dish import Dish
from src.peers.ClientSocketPeer import ClientSocketPeer
from src.zells.TurtleZell import TurtleZell

dish = Dish()

dish.zells.append(TurtleZell(sys.argv[1], dish.transmit))

if len(sys.argv) >= 3:
    port = int(sys.argv[2])
    host = 'localhost'
    if len(sys.argv) == 4:
        host = sys.argv[3]

    dish.join(ClientSocketPeer(port, host))

while 1:
    msg = sys.stdin.readline().strip().split(' ')
    if msg[0] == 'exit':
        sys.exit()
    elif msg[0] == 'join':
        if len(msg) == 2:
            dish.join(ClientSocketPeer(int(msg[1]), 'localhost'))
        elif len(msg) == 3:
            dish.join(ClientSocketPeer(int(msg[1]), msg[2]))
    elif msg[0] == 'turtle':
        dish.zells.append(TurtleZell(msg[1], dish.transmit))
    else:
        print "Unknown command"
