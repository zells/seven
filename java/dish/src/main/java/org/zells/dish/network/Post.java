package org.zells.dish.network;

public interface Post {

    Sender send(SignalPacket packet);

    Packet receive(Peer peer);

    interface Sender {
        void to(Peer peer);
    }
}
