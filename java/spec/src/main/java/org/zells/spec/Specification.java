package org.zells.spec;

import org.zells.dish.ClientSocketPeer;
import org.zells.dish.Dish;
import org.zells.dish.LoggingZell;

public class Specification {
    private static int port;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar specification.jar <port>");
            System.exit(0);
        }

        port = Integer.parseInt(args[0]);

        assertSignalsAreForwarded();
    }

    private static void assertSignalsAreForwarded() {
        Dish dish1 = new Dish();
        Dish dish2 = new Dish();
        Dish dish3 = new Dish();

        dish1.join(new ClientSocketPeer(port));
        dish2.join(new ClientSocketPeer(port));
        dish3.join(new ClientSocketPeer(port));

        LoggingZell zell2 = new LoggingZell();
        dish2.put(zell2);

        dish1.transmit(42);
    }
}