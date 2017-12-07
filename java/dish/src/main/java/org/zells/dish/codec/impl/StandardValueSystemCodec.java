package org.zells.dish.codec.impl;

import org.zells.dish.codec.Codec;

public interface StandardValueSystemCodec extends Codec {

    boolean asBoolean(Object object);

    String asString(Object object);

    String[] asStrings(Object object);
}
