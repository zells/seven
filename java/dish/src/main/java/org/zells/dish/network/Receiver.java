package org.zells.dish.network;

public interface Receiver {

    byte receive() throws ReceiverClosedException;

    class ReceiverClosedException extends RuntimeException {
    }
}
