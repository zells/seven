package org.zells.dish.network;

import org.zells.dish.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SignalSerializationEncoding implements Encoding {

    private static byte END = 0;
    private static byte ESC = 1;
    private static byte LST = 2;

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
            Signal encoded = new Signal(LST);
            //noinspection unchecked
            for (Object e : (List<Object>) object) {
                for (byte b : encode(e).toBytes()) {
                    encoded.add(b);
                }
            }
            encoded.add(END);
            return encoded;
        }

        throw new RuntimeException("Cannot encode " + object);
    }

    enum State {START, VALUE, LIST}

    @Override
    public Object decode(Receiver receiver) {
        Stack<Object> stack = new Stack<>();
        State state = State.START;

        boolean escaped = false;

        while (true) {
            byte b = receiver.receive();

            switch (state) {
                case START:
                    if (!escaped && b == ESC) {
                        escaped = true;
                    } else if (!escaped && b == LST) {
                        stack.push(new ArrayList<>());
                        state = State.LIST;
                    } else {
                        stack.push(new Signal(b));
                        state = State.VALUE;
                        escaped = false;
                    }
                    break;
                case VALUE:
                    if (!escaped && b == ESC) {
                        escaped = true;
                    } else if (!escaped && b == END) {
                        Object value = stack.pop();
                        if (stack.isEmpty()) {
                            return value;
                        }
                        //noinspection unchecked
                        ((List)stack.lastElement()).add(value);
                        state = State.LIST;
                    } else {
                        ((Signal) stack.lastElement()).add(b);
                        escaped = false;
                    }
                    break;
                case LIST:
                    if (b == END) {
                        return stack.pop();
                    } else {
                        stack.push(new Signal(b));
                        state = State.VALUE;
                    }
                    break;
            }
        }
    }
}
