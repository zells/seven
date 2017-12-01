package org.zells.dish.peers;

import org.zells.dish.Signal;
import org.zells.dish.network.Peer;

import java.io.*;
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
        } catch (IOException e) {
            throw new ReceiverClosedException();
        }
    }

    @Override
    public void send(Signal signal) {
        try {
            os.write(signal.toBytes());
        } catch (IOException e) {
            throw new SenderClosedException();
        }
    }

    public void close() throws IOException {
        os.close();
        is.close();
        socket.close();
    }
}
