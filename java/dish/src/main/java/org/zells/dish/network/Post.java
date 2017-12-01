package org.zells.dish.network;

import java.io.IOException;

public interface Post {

    Sender send(SignalPacket packet);

    Packet receive(Peer peer) throws IOException;

    interface Sender {
        void to(Peer peer);
    }
}
