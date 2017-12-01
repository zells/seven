package org.zells.spec;

import org.zells.dish.network.DefaultPost;
import org.zells.dish.network.Peer;
import org.zells.dish.Signal;
import org.zells.dish.peers.ClientSocketPeer;
import org.zells.dish.Dish;
import org.zells.dish.zells.ReceivingZell;

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

        assertSignalIsForwarded();

        System.exit(0);
    }

    private static DefaultPost buildPost() {
        return new DefaultPost();
    }

    private static void assertSignalIsForwarded() throws IOException {
        Dish dish1 = new Dish(buildPost().debugging());
        Peer peer1 = dish1.join(new ClientSocketPeer(port));

        Dish dish2 = new Dish(buildPost());
        Peer peer2 = dish2.join(new ClientSocketPeer(port));
        ReceivingZell zell2 = new ReceivingZell();
        dish2.put(zell2);

        Dish dish3 = new Dish(buildPost());
        Peer peer3 = dish3.join(new ClientSocketPeer(port));
        ReceivingZell zell3 = new ReceivingZell();
        dish3.put(zell3);

        Signal signal = new Signal((byte)new Random().nextInt(255));

        eventually("the Signal is forwarded",
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
        System.out.println("--------- " + name);

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
                    System.out.println("-> FAILED");
                    System.exit(1);
                }

                tries++;
            }
        }

        then.run();
        System.out.println("-> OK");
    }
}