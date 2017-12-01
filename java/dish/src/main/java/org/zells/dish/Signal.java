package org.zells.dish;

import org.zells.dish.network.Receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Signal {

    private List<Byte> bytes = new ArrayList<>();

    public Signal() {
    }

    public Signal(Byte... bytes) {
        for (byte b : bytes) {
            add(b);
        }
    }

    public static Signal from(int... bytes) {
        Signal signal = new Signal();
        for (int b : bytes) {
            signal.add(b);
        }
        return signal;
    }

    public static Signal from(byte[] bytes) {
        Signal signal = new Signal();
        for (int b : bytes) {
            signal.add(b);
        }
        return signal;
    }

    public int size() {
        return bytes.size();
    }

    public Byte at(int position) {
        return bytes.get(position);
    }

    public Signal add(byte b) {
        bytes.add(b);
        return this;
    }

    private Signal add(int b) {
        return add((byte) b);
    }

    public Receiver tap() {
        return new Receiver() {
            private int position = 0;

            @Override
            public Byte receive() {
                if (position == bytes.size()) {
                    throw new ReceiverClosedException();
                }

                return at(position++);
            }
        };
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
        return obj instanceof Signal
                && ((Signal) obj).bytes.equals(bytes);
    }
}
