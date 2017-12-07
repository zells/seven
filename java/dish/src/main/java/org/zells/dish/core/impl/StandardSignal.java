package org.zells.dish.core.impl;

import org.zells.dish.core.Signal;

import java.util.ArrayList;
import java.util.List;

public class StandardSignal implements Signal {

    private List<Byte> bytes = new ArrayList<>();

    public StandardSignal() {
    }

    public StandardSignal(Byte... bytes) {
        for (byte b : bytes) {
            add(b);
        }
    }

    public static StandardSignal from(int... bytes) {
        StandardSignal signal = new StandardSignal();
        for (int b : bytes) {
            signal.add(b);
        }
        return signal;
    }

    public static StandardSignal from(byte[] bytes) {
        StandardSignal signal = new StandardSignal();
        for (int b : bytes) {
            signal.add(b);
        }
        return signal;
    }

    private int size() {
        return bytes.size();
    }

    private Byte at(int position) {
        return bytes.get(position);
    }

    public StandardSignal add(byte b) {
        bytes.add(b);
        return this;
    }

    private StandardSignal add(int b) {
        return add((byte) b);
    }

    public byte[] toBytes() {
        byte[] out = new byte[size()];
        for (int i = 0; i < size(); i++) {
            out[i] = at(i);
        }
        return out;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @Override
    public int hashCode() {
        return bytes.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StandardSignal
                && ((StandardSignal) obj).bytes.equals(bytes);
    }
}
