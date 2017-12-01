package org.zells.spec;

import org.zells.dish.Signal;
import org.zells.dish.Zell;
import org.zells.dish.network.DefaultPost;
import org.zells.dish.Dish;
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

        Dish dish = new Dish(new DefaultPost());
        dish.put(new TestZell(dish));
        ServerSocketPeer.listen(dish, Integer.parseInt(args[0]));
    }

    private static class TestZell implements Zell {
        private Dish dish;
        private List<Signal> responses = new ArrayList<>();

        TestZell(Dish dish) {
            this.dish = dish;
        }

        @Override
        public void receive(Signal signal) {
            if (responses.contains(signal)) {
                return;
            }

            if (signal.size() > 1) {
                byte[] reversed = new byte[signal.size()];
                for (int i=0; i<signal.size(); i++) {
                    reversed[signal.size() - 1 - i] = signal.at(i);
                }

                Signal response = new Signal(reversed);
                responses.add(response);

                dish.transmit(response);
            }
        }
    }
}
