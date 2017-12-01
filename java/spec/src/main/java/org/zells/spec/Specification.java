package org.zells.spec;

import org.zells.dish.ClientSocketPeer;
import org.zells.dish.Dish;

public class Specification {
    public static void main(String[] args) {
        Dish dish1 = new Dish();
        dish1.join(new ClientSocketPeer(1337));
        dish1.transmit(42);
    }
}