package org.zells.dish.network;

import org.zells.dish.Signal;

public interface Sender {

    void send(Signal signal) throws SenderClosedException;

    class SenderClosedException extends RuntimeException {
    }
}
