package org.zells.dish;

import java.util.HashSet;
import java.util.Set;

public class Dish {
    private Set<Peer> peers = new HashSet<Peer>();
    private Set<Zell> zells = new HashSet<Zell>();

    public void put(Zell zell) {
        zells.add(zell);
    }

    public void join(Peer peer) {
        peers.add(peer);
    }

    public void transmit(int signal) {
        for (Peer p : peers) {
            p.write(signal);
        }
    }
}
