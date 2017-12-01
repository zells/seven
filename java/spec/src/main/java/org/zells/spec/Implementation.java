package org.zells.spec;

import org.zells.dish.Signal;
import org.zells.dish.Zell;
import org.zells.dish.network.NetworkPost;
import org.zells.dish.Dish;
import org.zells.dish.network.SignalSerializationEncoding;
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
            }
        }
    }
}
