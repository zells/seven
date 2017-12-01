package org.zells.spec;

import org.zells.dish.peers.ClientSocketPeer;
import org.zells.dish.Dish;
import org.zells.dish.zells.LoggingZell;

import java.io.IOException;
import java.util.Random;

public class Specification {
    private static int port;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar specification.jar <port>");
            System.exit(0);
        }

        port = Integer.parseInt(args[0]);

        assertSignalsAreForwarded();
    }

    private static void assertSignalsAreForwarded() throws IOException {
        Dish dish1 = new Dish();
        Dish dish2 = new Dish();
        Dish dish3 = new Dish();

        ClientSocketPeer peer1 = new ClientSocketPeer(port);
        dish1.join(peer1);

        ClientSocketPeer peer2 = new ClientSocketPeer(port);
        dish2.join(peer2);
        LoggingZell zell2 = new LoggingZell();
        dish2.put(zell2);

        ClientSocketPeer peer3 = new ClientSocketPeer(port);
        dish3.join(peer3);
        LoggingZell zell3 = new LoggingZell();
        dish3.put(zell3);

        int signal = new Random().nextInt(255);

        eventually("Signals are forwarded",
                () -> dish1.transmit(signal),
                () -> {
                    assertThat(zell2.hasReceived(signal));
                    assertThat(zell3.hasReceived(signal));
                }, () -> {
                    dish1.leave(peer1);
                    dish2.leave(peer2);
                    dish3.leave(peer3);
                });
    }

    private static void assertThat(boolean condition) {
        if (!condition) {
            throw new RuntimeException("Assertion failed");
        }
    }

    private static void eventually(String name, Runnable that, Runnable passes, Runnable then) {
        System.out.print(name);

        int tries = 0;
        while (true) {
            that.run();

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored2) {
            }

            try {
                passes.run();
                break;
            } catch (Exception ignored) {
                if (tries > 20) {
                    System.out.println(" -> FAILED");
                    System.exit(1);
                }

                tries++;
            }
        }

        then.run();
        System.out.println(" -> OK");
    }
}