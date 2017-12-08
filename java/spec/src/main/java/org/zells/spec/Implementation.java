package org.zells.spec;

import org.zells.dish.core.Dish;
import org.zells.dish.core.Signal;
import org.zells.dish.core.impl.StandardSignal;
import org.zells.dish.codec.impl.SignalByteSource;
import org.zells.dish.core.Zell;
import org.zells.dish.network.impl.DishNetworkProtocolPost;
import org.zells.dish.core.impl.StandardDish;
import org.zells.dish.codec.impl.FlatByteTreeCodec;
import org.zells.dish.codec.impl.SignalTreeCodec;
import org.zells.dish.core.impl.peers.ServerSocketPeer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Implementation {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar implemenation.jar <port>");
            System.exit(0);
        }

        Dish dish = new StandardDish(new DishNetworkProtocolPost(new FlatByteTreeCodec()));
        dish.put(new TestZell(dish));
        ServerSocketPeer.listen(dish, Integer.parseInt(args[0]));
    }

    private static class TestZell implements Zell {

        private final FlatByteTreeCodec encoding;
        private final Dish dish;
        private final List<Signal> responses = new ArrayList<>();

        TestZell(Dish dish) {
            this.dish = dish;
            this.encoding = new FlatByteTreeCodec();
        }

        @Override
        public void receive(Signal signal) {
            if (responses.contains(signal)) {
                return;
            }

            Object decoded = encoding.decode(new SignalByteSource(signal));

            if (decoded instanceof Signal) {
                byte[] bytes = ((Signal) decoded).toBytes();

                if (bytes.length > 1) {
                    StandardSignal response = new StandardSignal();
                    for (int i = 0; i < bytes.length; i++) {
                        response.add(bytes[bytes.length - i - 1]);
                    }

                    Signal responded = StandardSignal.from(encoding.encode(response));

                    responses.add(responded);
                    dish.transmit(responded);
                }

            } else if (decoded instanceof List) {
                List list = (List) decoded;

                if (list.size() < 2) {
                    responses.add(signal);
                    dish.transmit(signal);

                } else if (list.size() == 3) {
                    List<Object> reversed = new ArrayList<>();
                    for (int i = list.size() - 1; i >= 0; i--) {
                        reversed.add(list.get(i));
                    }

                    Signal responded = StandardSignal.from(encoding.encode(reversed));

                    responses.add(responded);
                    dish.transmit(responded);

                } else if (list.size() == 2) {
                    SignalTreeCodec signalTreeCodec = new SignalTreeCodec();
                    Object response = new StandardSignal();

                    Object argument = list.get(1);
                    switch (list.get(0).toString()) {
                        case "01":
                            response = !signalTreeCodec.asBoolean(argument);
                            break;

                        case "02":
                            response = signalTreeCodec.asString(argument).toUpperCase();
                            break;

                        case "03":
                            String[] strings = signalTreeCodec.asStrings(argument);
                            response = strings[0] + strings[1];
                            break;

                        case "04":
                            Double[] numbers = signalTreeCodec.asNumbers(argument);
                            response = numbers[0] + numbers[1];
                            break;

                        case "05":
                            Double[] fractions = signalTreeCodec.asNumbers(argument);
                            response = fractions[0] * fractions[1];
                            break;
                    }

                    Signal responded = StandardSignal.from(encoding.encode(signalTreeCodec.translate(response)));

                    responses.add(responded);
                    dish.transmit(responded);
                }
            }
        }
    }
}
