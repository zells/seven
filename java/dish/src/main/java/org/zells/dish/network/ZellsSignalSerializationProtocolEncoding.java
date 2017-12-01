package org.zells.dish.network;

import org.zells.dish.Signal;

import java.io.IOException;

public class ZellsSignalSerializationProtocolEncoding implements Encoding {

    private static int END = 0;
    private static int ESC = 1;

    @Override
    public byte[] encode(Signal signal) {
        Signal encoded = new Signal();
        for (byte b : signal.toBytes()) {
            if (b == END || b == ESC) {
                encoded = encoded.with(ESC);
            }
            encoded = encoded.with(b);
        }
        return encoded.with(END).toBytes();
    }

    @Override
    public Signal decode(Peer peer) throws IOException {
        Signal signal = new Signal();

        boolean escaped = false;
        while (true) {
            byte read = peer.read();

            if (!escaped && read == ESC) {
                escaped = true;
            } else if (!escaped && read == END) {
                return signal;
            } else {
                signal = signal.with(read);
                escaped = false;
            }
        }
    }

    public Signal decode(Signal signal) {
        try {
            return decode(new SignalPeer(signal));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class SignalPeer implements Peer {
        private final Signal signal;
        private int position = 0;

        SignalPeer(Signal signal) {
            this.signal = signal;
        }

        @Override
        public byte read() throws IOException {
            return signal.at(position++);
        }

        @Override
        public void write(byte[] signal) {

        }

        @Override
        public void close() {

        }
    }
}
