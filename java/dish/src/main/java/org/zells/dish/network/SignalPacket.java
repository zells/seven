package org.zells.dish.network;

import org.zells.dish.Signal;

import java.util.Random;

public class SignalPacket implements Packet {

    private Signal id;
    private Signal signal;

    SignalPacket(Signal id, Signal signal) {
        this.signal = signal;
        this.id = id;
    }

    public SignalPacket(Signal signal) {
        this(generateId(), signal);
    }

    private static Signal generateId() {
        Random random = new Random();

        Byte[] idBytes = new Byte[2];
        for (int i = 0; i < idBytes.length; i++) {
            idBytes[i] = (byte) (random.nextInt(254) + 1);
        }

        return new Signal(idBytes);
    }

    public Signal getSignal() {
        return signal;
    }

    public Signal getId() {
        return id;
    }

    @Override
    public String toString() {
        return "[" + id + ": " + signal + "]";
    }
}
