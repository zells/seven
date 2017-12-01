package org.zells.spec;

import org.zells.dish.Zell;
import org.zells.dish.network.NetworkPost;
import org.zells.dish.network.Peer;
import org.zells.dish.Signal;
import org.zells.dish.network.SignalSerializationEncoding;
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
        assertMultipleSignalsAreTransmitted();
        assertSignalCanContainAnything();

        System.exit(0);
    }

    private static NetworkPost buildPost() {
        return new NetworkPost(new SignalSerializationEncoding());
    }

    private static SignalSerializationEncoding buildEncoding() {
        return new SignalSerializationEncoding();
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

        Signal signal = new Signal(42);

        new Assertion("the Signal is forwarded")
                .when(() -> transmit(dish1, signal))
                .then(Assert.that(() -> zell2.hasReceived(signal)))
                .then(Assert.that(() -> zell3.hasReceived(signal)));

        dish1.leave(peer1);
        dish2.leave(peer2);
        dish3.leave(peer3);
    }

    private static void assertSignalIsReceived() throws IOException {
        Dish dish = new Dish(buildPost().debugging());
        Peer peer = dish.join(new ClientSocketPeer(port));
        ReceivingZell zell = new ReceivingZell();
        dish.put(zell);

        new Assertion("the Signal is received")
                .when(() -> transmit(dish, new Signal(42, 21)))
                .then(Assert.that(() -> zell.hasReceived(new Signal(21, 42))));

        dish.leave(peer);
    }

    private static void assertMultipleSignalsAreTransmitted() throws IOException {
        Dish dish = new Dish(buildPost().debugging());
        Peer peer = dish.join(new ClientSocketPeer(port));
        ReceivingZell zell = new ReceivingZell();
        dish.put(zell);

        new Assertion("multiple Signals are echoed reversed")
                .when(() -> {
                    transmit(dish, new Signal(42, 21));
                    transmit(dish, new Signal(42, 21));
                })
                .then(Assert.equal(2, () -> zell.countReceived(new Signal(21, 42))));

        dish.leave(peer);
    }

    private static void assertSignalCanContainAnything() throws IOException {
        Dish dish = new Dish(buildPost().debugging());
        Peer peer = dish.join(new ClientSocketPeer(port));
        ReceivingZell zell = new ReceivingZell();
        dish.put(zell);

        new Assertion("escape Signal content")
                .when(() -> {
                    Signal signal = new Signal();
                    for (int i = 0; i < 256; i++) {
                        signal.add(i);
                    }
                    transmit(dish, signal);
                })
                .then(Assert.that(() -> {
                    Signal signal = new Signal();
                    for (int i = 0; i < 256; i++) {
                        signal.add(255 - i);
                    }
                    return zell.hasReceived(signal);
                }));

        dish.leave(peer);
    }

    private static void transmit(Dish dish, Object object) {
        Signal encoded = buildEncoding().encode(object);
        System.out.println("Encoded: " + encoded);
        dish.transmit(encoded);
    }

    public static class ReceivingZell implements Zell {

        private List<Object> received = new ArrayList<>();

        public void receive(Signal signal) {
            Signal decoded = (Signal) buildEncoding().decode(signal.tap());
            System.out.println("Received: " + decoded);
            received.add(decoded);
        }

        boolean hasReceived(Object signal) {
            return received.contains(signal);
        }

        int countReceived(Object signal) {
            int count = 0;
            for (Object s : received) {
                if (s.equals(signal)) {
                    count++;
                }
            }
            return count;
        }
    }
}