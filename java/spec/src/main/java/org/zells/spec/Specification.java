package org.zells.spec;

import org.zells.dish.Zell;
import org.zells.dish.network.DefaultPost;
import org.zells.dish.network.Peer;
import org.zells.dish.Signal;
import org.zells.dish.peers.ClientSocketPeer;
import org.zells.dish.Dish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Specification {
    private static int port;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar specification.jar <port>");
            System.exit(0);
        }

        port = Integer.parseInt(args[0]);

        assertSignalIsForwarded();
        assertSignalIsReceived();

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

        Signal signal = new Signal((byte) 42);

        new Assertion("the Signal is forwarded")
                .when(() -> dish1.transmit(signal))
                .thenAssert(() -> zell2.hasReceived(signal))
                .thenAssert(() -> zell3.hasReceived(signal));

        dish1.leave(peer1);
        dish2.leave(peer2);
        dish3.leave(peer3);
    }

    private static void assertSignalIsReceived() throws IOException {
        Dish dish = new Dish(buildPost().debugging());
        Peer peer = dish.join(new ClientSocketPeer(port));
        ReceivingZell zell = new ReceivingZell();
        dish.put(zell);

        new Assertion("the Signal is echoed reversed")
                .when(() -> dish.transmit(new Signal(42, 21)))
                .thenAssert(() -> zell.hasReceived(new Signal(21, 42)));

        dish.leave(peer);
    }

    public static class ReceivingZell implements Zell {

        private List<Signal> received = new ArrayList<>();

        public void receive(Signal signal) {
            received.add(signal);
        }

        boolean hasReceived(Signal signal) {
            return received.contains(signal);
        }
    }
}