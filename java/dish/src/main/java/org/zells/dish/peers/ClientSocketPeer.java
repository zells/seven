package org.zells.dish.peers;

import org.zells.dish.Peer;

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
    public int read() {
        try {
            return is.read();
        } catch (IOException e) {
            return -1;
        }
    }

    public void write(int packet) {
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
