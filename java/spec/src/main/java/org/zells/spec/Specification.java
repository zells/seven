package org.zells.spec;

import org.zells.dish.Zell;
import org.zells.dish.network.ZellsNetworkProtocolPost;
import org.zells.dish.network.Peer;
import org.zells.dish.Signal;
import org.zells.dish.network.ZellsSignalSerializationProtocolEncoding;
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

    private static ZellsNetworkProtocolPost buildPost() {
        return new ZellsNetworkProtocolPost(new ZellsSignalSerializationProtocolEncoding());
    }

    private static ZellsSignalSerializationProtocolEncoding buildEncoding() {
        return new ZellsSignalSerializationProtocolEncoding();
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
                .when(() -> dish1.transmit(new Signal(buildEncoding().encode(signal))))
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
                .when(() -> dish.transmit(new Signal(buildEncoding().encode(new Signal(42, 21)))))
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
                    dish.transmit(new Signal(buildEncoding().encode(new Signal(42, 21))));
                    dish.transmit(new Signal(buildEncoding().encode(new Signal(42, 21))));
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
                        signal = signal.with(i);
                    }
                    dish.transmit(new Signal(buildEncoding().encode(signal)));
                })
                .then(Assert.that(() -> {
                    Signal response = new Signal();
                    for (int i = 0; i < 256; i++) {
                        response = response.with(255 - i);
                    }
                    return zell.hasReceived(response);
                }));

        dish.leave(peer);
    }

    public static class ReceivingZell implements Zell {

        private List<Signal> received = new ArrayList<>();

        public void receive(Signal signal) {
            received.add(buildEncoding().decode(signal));
        }

        boolean hasReceived(Signal signal) {
            return received.contains(signal);
        }

        int countReceived(Signal signal) {
            int count = 0;
            for (Signal s : received) {
                if (s.equals(signal)) {
                    count++;
                }
            }
            return count;
        }
    }
}