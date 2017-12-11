package org.zells.spec;

import org.zells.dish.core.Dish;
import org.zells.dish.core.Peer;
import org.zells.dish.core.Signal;
import org.zells.dish.codec.impl.SignalByteSource;
import org.zells.dish.core.impl.StandardSignal;
import org.zells.dish.core.Zell;
import org.zells.dish.codec.Codec;
import org.zells.dish.codec.impl.FlatByteTreeCodec;
import org.zells.dish.codec.impl.SignalTreeCodec;
import org.zells.dish.network.impl.DishNetworkProtocolPost;
import org.zells.dish.core.impl.peers.ClientSocketPeer;
import org.zells.dish.core.impl.StandardDish;

import java.io.IOException;
import java.util.*;

public class Specification {
    private static final byte LST = 0;
    private static final byte END = 1;
    private static final byte ESC = 2;

    private static final int OP_NEGATE = 1;
    private static final int OP_CAPITALIZE = 2;
    private static final int OP_CONCAT = 3;
    private static final int OP_ADD = 4;
    private static final int OP_MULTIPLY = 5;
    private static final int OP_FIND = 6;

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
        assertAsciiString();
        assertUtf8String();
        assertStringConcatenation();
        assertEmptyString();
        assertEmptyStrings();

        assertNullIsZero();
        assertNaturalNumbers();
        assertLargeNaturalNumbers();
        assertNegativeNumbers();
        assertFractions();

        assertDictionary();
        assertDictionaryWithNullValue();

        System.exit(0);
    }

    private static DishNetworkProtocolPost buildPost() {
        return new DishNetworkProtocolPost(new FlatByteTreeCodec());
    }

    private static Codec buildEncoding() {
        return new FlatByteTreeCodec(LST, END, ESC);
    }

    private static SignalTreeCodec buildTranslator() {
        return new SignalTreeCodec();
    }

    private static void assertSignalIsForwarded() throws IOException {
        Dish dish1 = new StandardDish(buildPost().debugging());
        Peer peer1 = dish1.join(new ClientSocketPeer(port));

        Dish dish2 = new StandardDish(buildPost());
        Peer peer2 = dish2.join(new ClientSocketPeer(port));
        ReceivingZell zell2 = new ReceivingZell();
        dish2.put(zell2);

        Dish dish3 = new StandardDish(buildPost());
        Peer peer3 = dish3.join(new ClientSocketPeer(port));
        ReceivingZell zell3 = new ReceivingZell();
        dish3.put(zell3);

        Signal signal = StandardSignal.from(42);

        new Assertion("the Signal is forwarded")
                .when(() -> transmit(dish1, signal))
                .then(Assert.that(() -> zell2.hasReceived(signal)))
                .then(Assert.that(() -> zell3.hasReceived(signal)));

        dish1.leave(peer1);
        dish2.leave(peer2);
        dish3.leave(peer3);
    }

    private static void setUp() throws IOException {
        dish = new StandardDish(buildPost().debugging());
        peer = dish.join(new ClientSocketPeer(port));
        zell = new ReceivingZell();
        dish.put(zell);
    }

    private static void tearDown() {
        dish.leave(peer);
    }

    private static void assertSignalIsReceived() throws IOException {
        setUp();

        new Assertion("the Signal is echoed reversed")
                .when(() -> transmit(dish, StandardSignal.from(42, 21)))
                .then(Assert.that(() -> zell.hasReceived(StandardSignal.from(21, 42))));

        tearDown();
    }

    private static void assertMultipleSignalsAreTransmitted() throws IOException {
        setUp();

        new Assertion("multiple Signals are echoed reversed")
                .when(() -> {
                    transmit(dish, StandardSignal.from(42, 21));
                    transmit(dish, StandardSignal.from(42, 21));
                })
                .then(Assert.equal(2, () -> zell.countReceived(StandardSignal.from(21, 42))));

        tearDown();
    }

    private static void assertEscapeSignalContent() throws IOException {
        setUp();

        new Assertion("Signal content is escaped")
                .when(() -> {
                    transmit(dish, StandardSignal.from(END, LST));
                    transmit(dish, StandardSignal.from(LST, ESC));
                    transmit(dish, StandardSignal.from(ESC, END));
                })
                .then(Assert.that(() -> zell.hasReceived(StandardSignal.from(LST, END))))
                .then(Assert.that(() -> zell.hasReceived(StandardSignal.from(ESC, LST))))
                .then(Assert.that(() -> zell.hasReceived(StandardSignal.from(END, ESC))));

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
                .when(() -> transmit(dish, new StandardSignal()))
                .then(Assert.that(() -> zell.hasReceived(new ArrayList<>())));

        tearDown();
    }

    private static void assertListWithValues() throws IOException {
        setUp();

        new Assertion("List is reversed")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(41, 42),
                        StandardSignal.from(42, 43),
                        StandardSignal.from(43, 44))))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        StandardSignal.from(43, 44),
                        StandardSignal.from(42, 43),
                        StandardSignal.from(41, 42)))));

        tearDown();
    }

    private static void assertListWithEscapedValues() throws IOException {
        setUp();

        new Assertion("List with escaped values is reversed")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(END, LST),
                        StandardSignal.from(ESC, END),
                        StandardSignal.from(LST, ESC))))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        StandardSignal.from(LST, ESC),
                        StandardSignal.from(ESC, END),
                        StandardSignal.from(END, LST)))));

        tearDown();
    }

    private static void assertListWithinLists() throws IOException {
        setUp();

        new Assertion("List within lists are reversed")
                .when(() -> transmit(dish, Arrays.asList(
                        Arrays.asList(StandardSignal.from(42), new ArrayList<StandardSignal>()),
                        new ArrayList<StandardSignal>(),
                        new StandardSignal())))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        Arrays.asList(StandardSignal.from(42), new ArrayList<StandardSignal>())))));

        tearDown();
    }

    private static void assertBooleanFalse() throws IOException {
        setUp();

        new Assertion("negate false")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_NEGATE), false)))
                .then(Assert.that(() -> zell.hasReceived(true)));

        tearDown();
    }

    private static void assertBooleanTrue() throws IOException {
        setUp();

        new Assertion("negate true")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_NEGATE), true)))
                .then(Assert.that(() -> zell.hasReceived(false)));

        tearDown();
    }

    private static void assertBooleanNull() throws IOException {
        setUp();

        new Assertion("negate null")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_NEGATE), null)))
                .then(Assert.that(() -> zell.hasReceived(true)));

        tearDown();
    }

    private static void assertNullAsEmptyString() throws IOException {
        setUp();

        new Assertion("null is empty string")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_CAPITALIZE), null)))
                .then(Assert.that(() -> zell.hasReceived(null)));

        tearDown();
    }

    private static void assertAsciiString() throws IOException {
        setUp();

        new Assertion("string with ASCII characters")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_CAPITALIZE), "abc")))
                .then(Assert.that(() -> zell.hasReceived("ABC")));

        tearDown();
    }

    private static void assertUtf8String() throws IOException {
        setUp();

        new Assertion("string with UTF-8 characters")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_CAPITALIZE), "äöü")))
                .then(Assert.that(() -> zell.hasReceived("ÄÖÜ")));

        tearDown();
    }

    private static void assertStringConcatenation() throws IOException {
        setUp();

        new Assertion("concatenate strings")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_CONCAT), Arrays.asList("foo", "bar"))))
                .then(Assert.that(() -> zell.hasReceived("foobar")));

        tearDown();
    }

    private static void assertEmptyString() throws IOException {
        setUp();

        new Assertion("concatenate empty string")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_CONCAT), Arrays.asList("foo", ""))))
                .then(Assert.that(() -> zell.hasReceived("foo")));

        tearDown();
    }

    private static void assertEmptyStrings() throws IOException {
        setUp();

        new Assertion("concatenate two empty strings")
                .when(() -> transmit(dish, Arrays.asList(StandardSignal.from(OP_CONCAT), Arrays.asList("", ""))))
                .then(Assert.that(() -> zell.hasReceived("")));

        tearDown();
    }

    private static void assertNullIsZero() throws IOException {
        setUp();

        new Assertion("add null to number")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_ADD),
                        Arrays.asList(3, null))))
                .then(Assert.that(() -> zell.hasReceived(3)));

        tearDown();
    }

    private static void assertNaturalNumbers() throws IOException {
        setUp();

        new Assertion("add natural numbers")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_ADD),
                        Arrays.asList(3, 4))))
                .then(Assert.that(() -> zell.hasReceived(7)));

        tearDown();
    }

    private static void assertLargeNaturalNumbers() throws IOException {
        setUp();

        new Assertion("add large natural numbers")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_ADD),
                        Arrays.asList(999999, 1))))
                .then(Assert.that(() -> zell.hasReceived(1000000)))
                .then(Assert.that(() -> zell.hasReceived(StandardSignal.from(0x0f, 0x42, 0x40))));

        tearDown();
    }

    private static void assertNegativeNumbers() throws IOException {
        setUp();

        new Assertion("add negative number")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_ADD),
                        Arrays.asList(3, -4))))
                .then(Assert.that(() -> zell.hasReceived(-1)))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList("-", 1))));

        tearDown();
    }

    private static void assertFractions() throws IOException {
        setUp();

        new Assertion("multiply fraction")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_MULTIPLY),
                        Arrays.asList(0.01, 2))))
                .then(Assert.that(() -> zell.hasReceived(0.02)))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList("/", 2, 100))));

        tearDown();
    }

    private static void assertDictionary() throws IOException {
        setUp();

        new Assertion("find dictionary key")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_FIND),
                        Arrays.asList("foo", createMap("foo", 1, "bar", 2)))))
                .then(Assert.that(() -> zell.hasReceived(createMap("found", 1))))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        Collections.singletonList("found"),
                        Collections.singletonList(1)
                ))));

        tearDown();
    }

    private static void assertDictionaryWithNullValue() throws IOException {
        setUp();

        new Assertion("find dictionary key")
                .when(() -> transmit(dish, Arrays.asList(
                        StandardSignal.from(OP_FIND),
                        Arrays.asList("bar", createMap("bar", null, "foo", 1)))))
                .then(Assert.that(() -> zell.hasReceived(createMap("found", null))))
                .then(Assert.that(() -> zell.hasReceived(Arrays.asList(
                        Collections.singletonList("found"),
                        Collections.singletonList(null)
                ))));

        tearDown();
    }

    private static Map<String, Integer> createMap(String key, Integer value) {
        Map<String, Integer> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static Map<String, Integer> createMap(String key1, Integer value1, String key2, Integer value2) {
        Map<String, Integer> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private static void transmit(Dish dish, Object object) {
        Signal encoded = StandardSignal.from(buildEncoding().encode(buildTranslator().translate(object)));
        System.out.println("Encoded: " + encoded);
        dish.transmit(encoded);
    }

    public static class ReceivingZell implements Zell {

        private List<Object> received = new ArrayList<>();

        public void receive(Signal signal) {
            Object decoded = buildEncoding().decode(new SignalByteSource(signal));
            System.out.println("Received: " + decoded);
            received.add(decoded);
        }

        boolean hasReceived(Object signal) {
            Object translated = buildTranslator().translate(signal);
            System.out.println("Expected: " + translated);
            return received.contains(translated);
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