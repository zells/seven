package org.zells.dish.network;

import org.zells.dish.Signal;

import java.util.ArrayList;
import java.util.List;

public class SignalSerializationEncoding implements Encoding {

    private static int END = 0;
    private static int ESC = 1;
    private static int LST = 2;

    @Override
    public Signal encode(Object object) {
        if (object instanceof Signal) {
            Signal encoded = new Signal();

            for (byte b : ((Signal) object).toBytes()) {
                if (b == END || b == ESC || b == LST) {
                    encoded.add(ESC);
                }
                encoded.add(b);
            }
            encoded.add(END);

            return encoded;
        } else if (object instanceof List) {
            return new Signal(LST, END);
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

            if (inList) {
                if (!escaped && b == END) {
                    return new ArrayList<Signal>();
                }
            } else {
                if (!escaped && b == ESC) {
                    escaped = true;
                } else if (!escaped && b == END) {
                    return signal;
                } else if (!escaped && b == LST) {
                    inList = true;
                } else {
                    signal.add(b);
                    escaped = false;
                }
            }
        }
    }
}
