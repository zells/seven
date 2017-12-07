package org.zells.dish.core.impl.peers;

import org.zells.dish.core.Peer;
import org.zells.dish.core.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

abstract class SocketPeer implements Peer {

    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;

    SocketPeer(Socket socket) throws IOException {
        this.socket = socket;
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public Byte receive() {
        try {
            return is.readByte();
        } catch (Exception e) {
            throw new PeerClosedException();
        }
    }

    @Override
    public void send(Signal signal) {
        try {
            os.write(signal.toBytes());
        } catch (IOException e) {
            throw new PeerClosedException();
        }
    }

    public void close() {
        try {
            os.close();
        } catch (IOException ignored) {
        }

        try {
            is.close();
        } catch (IOException ignored) {
        }

        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
