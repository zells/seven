package org.zells.dish.codec;

public interface ByteSource {

    byte next() throws ExhaustedSourceException;

    class ExhaustedSourceException extends RuntimeException {
    }
}
