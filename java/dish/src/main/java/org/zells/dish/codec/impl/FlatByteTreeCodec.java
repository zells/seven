package org.zells.dish.codec.impl;

import org.zells.dish.codec.ByteSource;
import org.zells.dish.codec.Codec;
import org.zells.dish.core.Signal;
import org.zells.dish.core.impl.StandardSignal;

import java.util.*;

public class FlatByteTreeCodec implements Codec {

    private Byte BEG = 0x11;
    private Byte END;
    private Byte LST;
    private Byte ESC;

    public FlatByteTreeCodec(byte END, byte LST, byte ESC) {
        this.END = END;
        this.LST = LST;
        this.ESC = ESC;
    }

    public FlatByteTreeCodec() {
        this((byte) 25, (byte) 26, (byte) 27);
    }

    @Override
    public byte[] encode(Object object) {
        List<Byte> encoded = new ArrayList<>(Arrays.asList(BEG, END, LST, ESC));
        encoded.addAll(_encode(object));
        return toBytes(encoded);
    }

    private List<Byte> _encode(Object object) {

        if (object instanceof Signal) {
            byte[] bytes = ((Signal) object).toBytes();

            if (bytes.length == 0) {
                return Arrays.asList(LST, END);
            }
            List<Byte> encoded = new ArrayList<>();

            //noinspection unchecked
            for (Byte b : bytes) {
                if (Arrays.asList(END, ESC, LST).contains(b)) {
                    encoded.add(ESC);
                }
                encoded.add(b);
            }
            encoded.add(END);

            return encoded;

        } else if (object instanceof List) {
            List<Byte> encoded = new ArrayList<>();

            encoded.add(LST);
            //noinspection unchecked
            for (Object item : (List) object) {
                encoded.addAll(_encode(item));
            }
            encoded.add(END);
            return encoded;
        }

        throw new RuntimeException("Cannot encode " + object);
    }

    enum State {READ_BEG, READ_END, READ_LST, READ_ESC, VALUE, LIST}

    @Override
    public Object decode(ByteSource source) {
        byte END = -1;
        byte LST = -1;
        byte ESC = -1;

        Stack<Object> stack = new Stack<>();
        State state = State.READ_BEG;

        boolean escaped = false;

        while (true) {
            byte b = source.next();

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
                    state = State.LIST;
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
                        ((StandardSignal) stack.lastElement()).add(b);
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
                        stack.push(new StandardSignal(b));
                        state = State.VALUE;
                        escaped = false;
                    }
                    break;
            }
        }
    }

    private byte[] toBytes(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i=0; i<list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }
}
