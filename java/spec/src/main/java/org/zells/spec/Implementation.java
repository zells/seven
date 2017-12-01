package org.zells.spec;

import org.zells.dish.Signal;
import org.zells.dish.Zell;
import org.zells.dish.network.DefaultPost;
import org.zells.dish.Dish;
import org.zells.dish.peers.ServerSocketPeer;

import java.io.IOException;

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

        TestZell(Dish dish) {
            this.dish = dish;
        }

        @Override
        public void receive(Signal signal) {
            byte[] response = new byte[signal.size()];
            for (int i=0; i<signal.size(); i++) {
                response[signal.size() - 1 - i] = signal.at(i);
            }
            dish.transmit(new Signal(response));
        }
    }
}
