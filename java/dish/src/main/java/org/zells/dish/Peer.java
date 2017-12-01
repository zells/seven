package org.zells.dish;

public interface Peer {

    int read();

    void write(int signal);

    void close();
}
