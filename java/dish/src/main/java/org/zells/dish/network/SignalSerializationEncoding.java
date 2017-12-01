package org.zells.dish.network;

import org.zells.dish.Signal;

public class SignalSerializationEncoding implements Encoding {

    private static byte END = 0;
    private static byte ESC = 1;

    @Override
    public Signal encode(Object object) {
        if (object instanceof Signal) {
            Signal encoded = new Signal();

            for (byte b : ((Signal) object).toBytes()) {
                if (b == END || b == ESC) {
                    encoded.add(ESC);
                }
                encoded.add(b);
            }
            encoded.add(END);

            return encoded;
        }

        throw new RuntimeException("Cannot encode " + object);
    }

    @Override
    public Object decode(Receiver stream) {
        Signal signal = new Signal();

        boolean escaped = false;
        boolean inList = false;
        while (true) {
            byte b = stream.receive();

//            if (inList) {
//                if (!escaped && b == END) {
//                    return new ArrayList<Signal>();
//                }
//            } else {
                if (!escaped && b == ESC) {
                    escaped = true;
                } else if (!escaped && b == END) {
                    return signal;
//                } else if (!escaped && b == LST) {
//                    inList = true;
                } else {
                    signal.add(b);
                    escaped = false;
//                }
            }
        }
    }
}
