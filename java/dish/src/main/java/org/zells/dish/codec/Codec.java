package org.zells.dish.codec;

public interface Codec {

    byte[] encode(Object object);

    Object decode(ByteSource source);
}
