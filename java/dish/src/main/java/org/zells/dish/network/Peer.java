package org.zells.dish.network;

import java.io.IOException;

public interface Peer {

    byte read() throws IOException;

    void write(byte[] signal);

    void close();
}
