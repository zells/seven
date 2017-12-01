package org.zells.dish.network;

public interface Receiver {

    Byte receive() throws ReceiverClosedException;

    class ReceiverClosedException extends RuntimeException {
    }
}
