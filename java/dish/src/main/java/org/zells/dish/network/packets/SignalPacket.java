package org.zells.dish.network.packets;

import org.zells.dish.core.Signal;
import org.zells.dish.core.impl.StandardSignal;
import org.zells.dish.network.Packet;

import java.util.Random;

public class SignalPacket implements Packet {

    private Signal id;
    private Signal signal;

    public SignalPacket(Signal signal) {
        this(generateId(), signal);
    }

    public SignalPacket(Signal id, Signal signal) {
        this.signal = signal;
        this.id = id;
    }

    private static Signal generateId() {
        Random random = new Random();

        Byte[] idBytes = new Byte[2];
        for (int i = 0; i < idBytes.length; i++) {
            idBytes[i] = (byte) (random.nextInt(254) + 1);
        }

        return new StandardSignal(idBytes);
    }

    public Signal getSignal() {
        return signal;
    }

    public Signal getId() {
        return id;
    }

    @Override
    public String toString() {
        return "(" + id + ": " + signal + "";
    }
}
