package org.zells.dish;

import java.util.Arrays;
import java.util.List;

public class Signal {
    private byte[] bytes;

    public Signal(byte b) {
        bytes = new byte[]{b};
    }

    public Signal(byte... bytes) {
        this.bytes = bytes;
    }

    public Signal(int... bytes) {
        this.bytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            this.bytes[i] = (byte) bytes[i];
        }
    }

    public Signal(List<Byte> bytes) {
        this.bytes = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            this.bytes[i] = bytes.get(i);
        }
    }

    public byte[] toBytes() {
        return bytes;
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
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Signal
                && Arrays.equals(((Signal) obj).bytes, bytes);
    }

    public int size() {
        return bytes.length;
    }

    public byte at(int position) {
        return bytes[position];
    }
}
