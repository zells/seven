package org.zells.dish.peers;

import org.zells.dish.Dish;
import org.zells.dish.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketPeer implements Peer {

    private static boolean running = true;

    private DataInputStream is;
    private DataOutputStream os;
    private final Socket socket;

    public static void listen(Dish dish, int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Listening on " + port);

        new Thread(() -> {
            while (running) {
                try {
                    dish.join(new ServerSocketPeer(server.accept()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private ServerSocketPeer(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
    }


    @Override
    public int read() {
        try {
            return is.read();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void write(int signal) {
        try {
            os.write(signal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}