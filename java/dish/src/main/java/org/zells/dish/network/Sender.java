package org.zells.dish.network;

public interface Sender {

    void send(byte[] bytes) throws SenderClosedException;

    class SenderClosedException extends RuntimeException {
    }
}
