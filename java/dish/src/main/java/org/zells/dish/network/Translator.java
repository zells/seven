package org.zells.dish.network;

import org.zells.dish.Signal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Translator {
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
            return Signal.from(((String) object).getBytes(StandardCharsets.UTF_8));
        } else if (object instanceof Boolean) {
            return Signal.from(((boolean) object) ? 1 : 0);
        } else if (object == null) {
            return new ArrayList<>();
        }
        return object;
    }

    public boolean asBoolean(Object object) {
        return object.equals(Signal.from(1));
    }

    public String asString(Object object) {
        if (object instanceof Signal) {
            return new String(((Signal) object).toBytes(), StandardCharsets.UTF_8);
        }

        return "";
    }

    public String[] asStrings(Object object) {
        if (object instanceof List) {
            List list = (List) object;
            String[] strings = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                strings[i] = asString(list.get(i));
            }
            return strings;
        }

        return new String[0];
    }
}
