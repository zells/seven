package org.zells.dish.codec.impl;

import org.zells.dish.core.Signal;
import org.zells.dish.codec.ByteSource;
import org.zells.dish.codec.Codec;
import org.zells.dish.core.impl.StandardSignal;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalTreeCodec implements Codec {

    @Override
    public byte[] encode(Object object) {
        return null;
    }

    @Override
    public Object decode(ByteSource source) {
        return null;
    }

    public Object translate(Object object) {
        if (object instanceof List) {
            List<Object> translated = new ArrayList<>();
            for (Object o : (List) object) {
                translated.add(translate(o));
            }
            return translated;
        } else if (object instanceof String) {
            if (((String) object).isEmpty()) {
                return new ArrayList<>();
            }
            return StandardSignal.from(((String) object).getBytes(StandardCharsets.UTF_8));
        } else if (object instanceof Boolean) {
            return StandardSignal.from(((boolean) object) ? 1 : 0);
        } else if (object instanceof Number) {
            int integer = ((Number) object).intValue();
            if (integer < 0) {
                return translate(Arrays.asList("-", -integer));
            }
            return StandardSignal.from(ByteBuffer.allocate(4).putInt(integer).array());
        } else if (object == null) {
            return new ArrayList<>();
        }
        return object;
    }

    public boolean asBoolean(Object object) {
        return object.equals(StandardSignal.from(1));
    }

    public String asString(Object object) {
        if (object instanceof Signal) {
            return new String(((Signal) object).toBytes(), StandardCharsets.UTF_8);
        }

        return "";
    }

    private Double asNumber(Object object) {
        if (object instanceof Signal) {
            return ((Integer) ByteBuffer.wrap(((Signal) object).toBytes()).getInt()).doubleValue();
        } else if (object instanceof List) {
            List list = (List) object;
            if (list.size() == 2 && asString(list.get(0)).equals("-")) {
                return -asNumber(list.get(1));
            }
        }
        return 0.0;
    }

    public String[] asStrings(Object object) {
        if (!(object instanceof List)) {
            return new String[0];
        }

        List list = (List) object;
        String[] strings = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strings[i] = asString(list.get(i));
        }
        return strings;
    }

    public Double[] asNumbers(Object object) {
        if (!(object instanceof List)) {
            return new Double[0];
        }

        List list = (List) object;
        Double[] numbers = new Double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            numbers[i] = asNumber(list.get(i));
        }
        return numbers;
    }
}
