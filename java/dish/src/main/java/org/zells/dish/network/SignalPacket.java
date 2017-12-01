package org.zells.dish.network;

import org.zells.dish.Signal;

public class SignalPacket implements Packet {
    private Signal signal;

    public SignalPacket(Signal signal) {
        this.signal = signal;
    }

    public Signal getSignal() {
        return signal;
    }

    public Signal getId() {
        return signal;
    }

    @Override
    public String toString() {
        return signal.toString();
    }
}
