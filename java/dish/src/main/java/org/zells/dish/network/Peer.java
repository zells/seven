package org.zells.dish.network;

public interface Peer {

    byte read();

    void write(byte[] signal);

    void close();
}
