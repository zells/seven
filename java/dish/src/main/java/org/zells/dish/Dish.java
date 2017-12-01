package org.zells.dish;

import java.util.HashSet;
import java.util.Set;

public class Dish {
    private Set<Peer> peers = new HashSet<>();
    private Set<Zell> zells = new HashSet<>();
    private Set<Integer> transmitted = new HashSet<>();

    public void put(Zell zell) {
        zells.add(zell);
    }

    public void transmit(int signal) {
        if (transmitted.contains(signal)) {
            return;
        }
        transmitted.add(signal);

        for (Zell z : zells) {
            z.receive(signal);
        }

        for (Peer p : peers) {
            p.write(signal);
        }
    }

    public void join(Peer peer) {
        peers.add(peer);

        new Thread(() -> {
            while(true) {
                int signal = peer.read();
                if (signal == -1) {
                    leave(peer);
                    return;
                }

                transmit(signal);
            }
        }).start();
    }

    public void leave(Peer peer) {
        peers.remove(peer);
        peer.close();
    }
}
