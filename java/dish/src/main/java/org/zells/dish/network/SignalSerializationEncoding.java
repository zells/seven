package org.zells.dish.network;

import org.zells.dish.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SignalSerializationEncoding implements Encoding {

    private byte BEG = 0x11;
    private byte END;
    private byte LST;
    private byte ESC;

    public SignalSerializationEncoding(byte END, byte LST, byte ESC) {
        this.END = END;
        this.LST = LST;
        this.ESC = ESC;
    }

    public SignalSerializationEncoding() {
        this((byte) 25, (byte) 26, (byte) 27);
    }

    @Override
    public Signal encode(Object object) {
        Signal encoded = new Signal(BEG, END, LST, ESC);
        for (byte b : _encode(object).toBytes()) {
            encoded.add(b);
        }
        return encoded;
    }

    private Signal _encode(Object object) {
        if (object instanceof Signal) {
            if (((Signal) object).size() == 0) {
                return new Signal(LST, END);
            }
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
                for (byte b : _encode(e).toBytes()) {
                    encoded.add(b);
                }
            }
            encoded.add(END);
            return encoded;
        }

        throw new RuntimeException("Cannot encode " + object);
    }

    enum State {READ_BEG, READ_END, READ_LST, READ_ESC, START, VALUE, LIST}

    @Override
    public Object decode(Receiver receiver) {
        byte END = -1;
        byte LST = -1;
        byte ESC = -1;

        Stack<Object> stack = new Stack<>();
        State state = State.READ_BEG;

        boolean escaped = false;

        while (true) {
            byte b = receiver.receive();

            switch (state) {
                case READ_BEG:
                    if (b != BEG) {
                        throw new RuntimeException("Expected " + BEG + ", found " + b);
                    }
                    state = State.READ_END;
                    break;
                case READ_END:
                    END = b;
                    state = State.READ_LST;
                    break;
                case READ_LST:
                    LST = b;
                    state = State.READ_ESC;
                    break;
                case READ_ESC:
                    ESC = b;
                    state = State.START;
                    break;
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
                        ((List) stack.lastElement()).add(value);
                        state = State.LIST;
                    } else {
                        ((Signal) stack.lastElement()).add(b);
                        escaped = false;
                    }
                    break;
                case LIST:
                    if (!escaped && b == ESC) {
                        escaped = true;
                    } else if (!escaped && b == LST) {
                        stack.push(new ArrayList<>());
                        state = State.LIST;
                    } else if (!escaped && b == END) {
                        Object value = stack.pop();
                        if (stack.isEmpty()) {
                            return value;
                        }
                        //noinspection unchecked
                        ((List) stack.lastElement()).add(value);
                        state = State.LIST;
                    } else {
                        stack.push(new Signal(b));
                        state = State.VALUE;
                        escaped = false;
                    }
                    break;
            }
        }
    }
}
