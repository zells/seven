package org.zells.spec;

import org.zells.dish.Dish;
import org.zells.dish.ServerSocketPeer;

import java.io.IOException;

public class Implementation {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java -jar implemenation.jar <port>");
            System.exit(0);
        }

        Dish dish1 = new Dish();
        ServerSocketPeer.listen(dish1, Integer.parseInt(args[0]));
    }
}
