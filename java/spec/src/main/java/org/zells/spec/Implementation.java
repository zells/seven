package org.zells.spec;

import org.zells.dish.Signal;
import org.zells.dish.Zell;
import org.zells.dish.network.NetworkPost;
import org.zells.dish.Dish;
import org.zells.dish.network.SignalSerializationEncoding;
import org.zells.dish.network.Translator;
import org.zells.dish.peers.ServerSocketPeer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Implementation {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar implemenation.jar <port>");
            System.exit(0);
        }

        Dish dish = new Dish(new NetworkPost(new SignalSerializationEncoding()));
        dish.put(new TestZell(dish));
        ServerSocketPeer.listen(dish, Integer.parseInt(args[0]));
    }

    private static class TestZell implements Zell {
        private final SignalSerializationEncoding encoding;
        private final Dish dish;
        private final List<Signal> responses = new ArrayList<>();

        TestZell(Dish dish) {
            this.dish = dish;
            this.encoding = new SignalSerializationEncoding();
        }

        @Override
        public void receive(Signal signal) {
            if (responses.contains(signal)) {
                return;
            }

            Object decoded = encoding.decode(signal.tap());

            if (decoded instanceof Signal) {
                signal = (Signal) decoded;

                if (signal.size() > 1) {
                    Signal response = new Signal();
                    for (int i = 0; i < signal.size(); i++) {
                        response.add(signal.at(signal.size() - i - 1));
                    }

                    Signal responded = encoding.encode(response);

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

                    Signal responded = encoding.encode(reversed);

                    responses.add(responded);
                    dish.transmit(responded);

                } else if (list.size() == 2) {
                    Translator translator = new Translator();
                    Object response = new Signal();

                    Object argument = list.get(1);
                    switch (list.get(0).toString()) {
                        case "01":
                            response = !translator.asBoolean(argument);
                            break;
                        case "02":
                            response = translator.asString(argument).toUpperCase();
                            break;
                        case "03":
                            String[] arguments = translator.asStrings(argument);
                            response = arguments[0] + arguments[1];
                    }

                    Signal responded = encoding.encode(translator.translate(response));

                    responses.add(responded);
                    dish.transmit(responded);
                }
            }
        }
    }
}
