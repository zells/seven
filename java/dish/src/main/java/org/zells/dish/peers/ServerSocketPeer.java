package org.zells.dish.peers;

import org.zells.dish.Dish;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketPeer extends SocketPeer {

    private static boolean running = true;

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
        super(socket);
    }
}
