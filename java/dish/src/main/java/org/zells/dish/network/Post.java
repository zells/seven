package org.zells.dish.network;

public interface Post {

    Packet receive(Receiver with);

    void send(Packet packet, Sender with);
}
