package org.zells.dish.codec.impl;

import org.zells.dish.core.Signal;
import org.zells.dish.codec.ByteSource;

public class SignalByteSource implements ByteSource {

    private byte[] bytes;
    private int position = 0;

    public SignalByteSource(Signal signal) {
        bytes = signal.toBytes();
    }

    @Override
    public byte next() throws ExhaustedSourceException {
        if (position == bytes.length) {
            throw new ExhaustedSourceException();
        }

        return bytes[position++];
    }
}
