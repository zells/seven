package org.zells.dish;

import org.zells.dish.codec.impl.FlatByteTreeCodec;
import org.zells.dish.core.Dish;
import org.zells.dish.core.impl.StandardDish;
import org.zells.dish.core.impl.peers.ClientSocketPeer;
import org.zells.dish.core.impl.peers.ServerSocketPeer;
import org.zells.dish.network.impl.DishNetworkProtocolPost;
import org.zells.dish.zells.TurtleZell;

import java.io.IOException;

public class Node {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("" +
                    "Usage: java -jar node.jar\n" +
                    "\n" +
                    "Options:\n" +
                    "  -l <local_port>\n" +
                    "  -p <peer_port> <peer_host>");
            System.exit(0);
        }

        Dish dish = new StandardDish(new DishNetworkProtocolPost(new FlatByteTreeCodec()));
        dish.put(new TurtleZell(dish, "shelly"));

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-l")) {
                ServerSocketPeer.listen(dish, Integer.parseInt(args[++i]));
            } else if (args[i].equals("-p")) {
                dish.join(new ClientSocketPeer(Integer.parseInt(args[++i]), args[++i]));
            }
        }
    }
}
