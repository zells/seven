package org.zells.dish;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientSocketPeer implements Peer {
    public ClientSocketPeer(int port) {

    }

    public void write(int packet) {
        Socket socket;
        DataOutputStream os;
        DataInputStream is;

        try {
            socket = new Socket("localhost", 1337);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());

            os.write(42);

            int read;
            while ((read = is.read()) != -1) {
                System.out.println(read);
            }
            os.close();
            is.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
