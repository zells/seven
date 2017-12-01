package org.zells.spec;

import org.zells.dish.Signal;
import org.zells.dish.Zell;
import org.zells.dish.network.ZellsNetworkProtocolPost;
import org.zells.dish.Dish;
import org.zells.dish.network.ZellsSignalSerializationProtocolEncoding;
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

        Dish dish = new Dish(new ZellsNetworkProtocolPost(new ZellsSignalSerializationProtocolEncoding()));
        dish.put(new TestZell(dish));
        ServerSocketPeer.listen(dish, Integer.parseInt(args[0]));
    }

    private static class TestZell implements Zell {
        private final ZellsSignalSerializationProtocolEncoding encoding;
        private final Dish dish;
        private final List<Signal> responses = new ArrayList<>();

        TestZell(Dish dish) {
            this.dish = dish;
            this.encoding = new ZellsSignalSerializationProtocolEncoding();
        }

        @Override
        public void receive(Signal signal) {
            signal = encoding.decode(signal);

            if (responses.contains(signal)) {
                return;
            }

            if (signal.size() > 1) {
                Signal response = new Signal();
                for (int i = 0; i < signal.size(); i++) {
                    response = response.with(signal.at(signal.size() - i - 1));
                }

                responses.add(response);
                dish.transmit(new Signal(encoding.encode(response)));
            }
        }
    }
}
