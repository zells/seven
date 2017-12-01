package org.zells.dish.peers;

import org.zells.dish.network.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSocketPeer implements Peer {

    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;

    public ClientSocketPeer(int port) throws IOException {
        socket = new Socket("localhost", port);
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());
    }

    @Override
    public byte read() throws IOException {
        return is.readByte();
    }

    public void write(byte[] packet) {
        try {
            os.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            os.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
