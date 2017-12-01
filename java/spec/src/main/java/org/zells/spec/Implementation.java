package org.zells.spec;

import org.zells.dish.Dish;
import org.zells.dish.ServerSocketPeer;

public class Implementation {
    public static void main(String[] args) {
        Dish dish1 = new Dish();
        ServerSocketPeer.listen(dish1, 1337);
    }
}
