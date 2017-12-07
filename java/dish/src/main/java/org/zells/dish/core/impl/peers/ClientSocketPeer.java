package org.zells.dish.core.impl.peers;

import java.io.IOException;
import java.net.Socket;

public class ClientSocketPeer extends SocketPeer {

    public ClientSocketPeer(int port) throws IOException {
        super(new Socket("localhost", port));
    }
}
