package org.zells.dish.core;

public interface Dish {

    Zell put(Zell zell);

    void remove(Zell zell);

    void transmit(Signal signal);

    Peer join(Peer peer);

    void leave(Peer peer);
}
