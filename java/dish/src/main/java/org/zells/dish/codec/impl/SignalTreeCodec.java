package org.zells.dish.codec.impl;

import org.zells.dish.codec.ByteSource;
import org.zells.dish.codec.Codec;
import org.zells.dish.core.Signal;
import org.zells.dish.core.impl.StandardSignal;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

        } else if (object instanceof Map) {
            Map map = (Map) object;
            //noinspection unchecked
            return translate(Arrays.asList(new ArrayList<>(map.keySet()), new ArrayList(map.values())));

        } else if (object instanceof String) {
            if (((String) object).isEmpty()) {
                return new ArrayList<>();
            }
            return StandardSignal.from(((String) object).getBytes(StandardCharsets.UTF_8));

        } else if (object instanceof Boolean) {
            return StandardSignal.from(((boolean) object) ? 1 : 0);

        } else if (object instanceof Number) {
            double number = ((Number) object).doubleValue();
            if (number < 0) {
                return translate(Arrays.asList("-", -number));
            }
            int nominator = (int) number;

            if (nominator == number) {
                int numBytes = (int) Math.max(1, Math.ceil(Math.log(nominator) / Math.log(2) / 8));
                byte[] bytes = new byte[numBytes];
                for (int i = numBytes - 1; i >= 0; i--) {
                    bytes[i] = (byte) nominator;
                    nominator = nominator >> 8;
                }
                return StandardSignal.from(bytes);
            }

            double denominator = 1;
            do {
                denominator *= 10;
                nominator = (int) (number * denominator);
            } while (nominator != number * denominator);

            return translate(Arrays.asList("/", nominator, denominator));

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
            int number = 0;
            byte[] bytes = ((Signal) object).toBytes();
            for (byte b : bytes) {
                number = number << 8;
                number += (int) b;
            }
            return (double) number;
        } else if (object instanceof List) {
            List list = (List) object;
            if (list.size() == 2 && asString(list.get(0)).equals("-")) {
                return -asNumber(list.get(1));
            } else if (list.size() == 3 && asString(list.get(0)).equals("/")) {
                return asNumber(list.get(1)) / asNumber(list.get(2));
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

    public Map<Object, Object> asMap(Object object, Mapper keyMapper) {
        HashMap<Object, Object> map = new HashMap<>();

        if (!(object instanceof List)) {
            return map;
        }
        List list = (List) object;
        if (list.size() != 2 || !(list.get(0) instanceof List) || !(list.get(1) instanceof List)) {
            return map;
        }

        List keys = (List) list.get(0);
        List values = (List) list.get(1);

        for (int i = 0; i < keys.size(); i++) {
            Object value;
            if (values.size() < i - 1) {
                value = null;
            } else {
                value = values.get(i);
            }

            map.put(keyMapper.map(i, keys.get(i)), value);
        }

        return map;
    }

    public interface Mapper {
        Object map(int index, Object object);
    }
}
