package org.zells.dish;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketPeer extends Thread implements Peer {
    public static void listen(Dish dish, int port) {
        ServerSocket echoServer;

        try {
            echoServer = new ServerSocket(1337);

            while (true) {
                try {
                    System.out.println("Listening on 1337");
                    dish.join(new ServerSocketPeer(echoServer.accept()));
                } catch (IOException ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataInputStream is;
    private DataOutputStream os;

    private ServerSocketPeer(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());

        start();
    }

    public void run() {
        try {
            int read;
            while ((read = is.read()) != -1) {
                System.out.print("server: ");
                System.out.println(read);
                os.writeByte(read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void write(int signal) {

    }
}
