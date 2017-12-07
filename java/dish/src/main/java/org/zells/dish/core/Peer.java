package org.zells.dish.core;

public interface Peer {

    Byte receive() throws PeerClosedException;

    void send(Signal signal) throws PeerClosedException;

    void close();

    class PeerClosedException extends RuntimeException {
    }

}
