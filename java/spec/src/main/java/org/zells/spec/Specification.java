package org.zells.spec;

import org.zells.dish.Zell;
import org.zells.dish.network.*;
import org.zells.dish.Signal;
import org.zells.dish.peers.ClientSocketPeer;
import org.zells.dish.Dish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Specification {
    private static final byte END = 0;
    private static final byte ESC = 1;
    private static final byte LST = 2;

    private static int port;
    private static Dish dish;
    private static Peer peer;
    private static ReceivingZell zell;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar specification.jar <port>");
            System.exit(0);
        }

        port = Integer.parseInt(args[0]);

        assertSignalIsForwarded();
        assertSignalIsReceived();
        assertMultipleSignalsAreTransmitted();
        assertEscapeSignalContent();
        assertEmptyList();
        assertEmptySignal();
        assertListWithValues();
        assertListWithEscapedValues();
        assertListWithinLists();

        assertBooleanFalse();
        assertBooleanTrue();
        assertBooleanNull();

        assertNullAsEmptyString();

        System.exit(0);
    }

    private static NetworkPost buildPost() {
        return new NetworkPost(new SignalSerializationEncoding(END, LST, ESC));
    }

    private static Encoding buildEncoding() {
        return new SignalSerializationEncoding(END, LST, ESC);
    }

    private static Translator buildTranslator() {
        return new Translator();
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

        Signal signal = Signal.from(42);

        new Assertion("the Signal is forwarded")
                .when(() -> transmit(dish1, signal))
                .then(Assert.that(() -> zell2.hasReceived(signal)))
                .then(Assert.that(() -> zell3.hasReceived(signal)));

        dish1.leave(peer1);
        dish2.leave(peer2);
        dish3.leave(peer3);
    }

    private static void setUp() throws IOException {
        dish = new Dish(buildPost().debugging());
        peer = dish.join(new ClientSocketPeer(port));
        zell = new ReceivingZell();
        dish.put(zell);
    }

    private static void tearDown() {
        dish.leave(peer);
    }

    private static void assertSignalIsReceived() throws IOException {
        setUp();

        new Assertion("the Signal is received")
                .when(() -> transmit(dish, Signal.from(42, 21)))
                .then(Assert.that(() -> zell.hasReceived(Signal.from(21, 42))));

        tearDown();
    }

    private static void assertMultipleSignalsAreTransmitted() throws IOException {
        setUp();

        new Assertion("multiple Signals are echoed reversed")
                .when(() -> {
                    transmit(dish, Signal.from(42, 21));
                    transmit(dish, Signal.from(42, 21));
                })
                .then(Assert.equal(2, () -> zell.countReceived(Signal.from(21, 42))));

        tearDown();
    }

    private static void assertEscapeSignalContent() throws IOException {
        setUp();

        new Assertion("Signal content is escaped")
                .when(() -> {
                    transmit(dish, Signal.from(END, LST));
                    transmit(dish, Signal.from(LST, ESC));
                    transmit(dish, Signal.from(ESC, END));
                })
                .then(Assert.that(() -> zell.hasReceived(Signal.from(LST, END))))
                .then(Assert.that(() -> zell.hasReceived(Signal.from(ESC, LST))))
                .then(Assert.that(() -> zell.hasReceived(Signal.from(END, ESC))));

        tearDown();
    }

    private static void assertEmptyList() throws IOException {
        setUp();

        new Assertion("empty List is echoed")
                .when(() -> transmit(dish, new ArrayList<>()))
                .then(Assert.equal(2, () -> zell.countReceived(new ArrayList<>())));

        tearDown();
    }

    private static void assertEmptySignal() throws IOException {
        setUp();

        new Assertion("empty Signal becomes empty List")
                .when(() -> transmit(dish, new Signal()))
                .then(Assert.that(() -> zell.hasReceived(new ArrayList<>())));

        tearDown();
    }

    private static void assertListWithValues() throws IOException {
        setUp();

        new Assertion("List is reversed")
                .when(() -> transmit(dish, Arrays.asList(
                        Signal.from(41, 42),
                        Signal.from(42, 43),
                        Signal.from(43, 44))))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        Signal.from(43, 44),
                        Signal.from(42, 43),
                        Signal.from(41, 42)))));

        tearDown();
    }

    private static void assertListWithEscapedValues() throws IOException {
        setUp();

        new Assertion("List with escaped values is reversed")
                .when(() -> transmit(dish, Arrays.asList(
                        Signal.from(END, LST),
                        Signal.from(ESC, END),
                        Signal.from(LST, ESC))))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        Signal.from(LST, ESC),
                        Signal.from(ESC, END),
                        Signal.from(END, LST)))));

        tearDown();
    }

    private static void assertListWithinLists() throws IOException {
        setUp();

        new Assertion("List within lists are reversed")
                .when(() -> transmit(dish, Arrays.asList(
                        Arrays.asList(Signal.from(42), new ArrayList<Signal>()),
                        new ArrayList<Signal>(),
                        new Signal())))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        Arrays.asList(Signal.from(42), new ArrayList<Signal>())))));

        tearDown();
    }

    private static void assertBooleanFalse() throws IOException {
        setUp();

        new Assertion("negate false")
                .when(() -> transmit(dish, Arrays.asList(Signal.from(1), false)))
                .then(Assert.that(() -> zell.hasReceived(Signal.from(1))));

        tearDown();
    }

    private static void assertBooleanTrue() throws IOException {
        setUp();

        new Assertion("negate true")
                .when(() -> transmit(dish, Arrays.asList(Signal.from(1), true)))
                .then(Assert.that(() -> zell.hasReceived(Signal.from(0))));

        tearDown();
    }

    private static void assertBooleanNull() throws IOException {
        setUp();

        new Assertion("negate null")
                .when(() -> transmit(dish, Arrays.asList(Signal.from(1), null)))
                .then(Assert.that(() -> zell.hasReceived(Signal.from(1))));

        tearDown();
    }

    private static void assertNullAsEmptyString() throws IOException {
        setUp();

        new Assertion("null is empty string")
                .when(() -> transmit(dish, Arrays.asList(Signal.from(2), null)))
                .then(Assert.that(() -> zell.hasReceived(new ArrayList<>())));

        tearDown();
    }

    private static void transmit(Dish dish, Object object) {
        Signal encoded = buildEncoding().encode(buildTranslator().translate(object));
        System.out.println("Encoded: " + encoded);
        dish.transmit(encoded);
    }

    public static class ReceivingZell implements Zell {

        private List<Object> received = new ArrayList<>();

        public void receive(Signal signal) {
            Object decoded = buildEncoding().decode(signal.tap());
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