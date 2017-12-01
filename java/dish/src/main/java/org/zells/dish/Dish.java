package org.zells.dish;

import org.zells.dish.network.Packet;
import org.zells.dish.network.Peer;
import org.zells.dish.network.Post;
import org.zells.dish.network.SignalPacket;

import java.util.HashSet;
import java.util.Set;

public class Dish {

    private final Post post;

    private Set<Peer> peers = new HashSet<>();
    private Set<Zell> zells = new HashSet<>();
    private Set<Signal> transmitted = new HashSet<>();

    public Dish(Post post) {
        this.post = post;
    }

    public void put(Zell zell) {
        zells.add(zell);
    }

    public void transmit(Signal signal) {
        receive(new SignalPacket(signal));
    }

    private void receive(SignalPacket packet) {
        if (transmitted.contains(packet.getId())) {
            return;
        }
        transmitted.add(packet.getId());

        for (Zell z : zells) {
            z.receive(packet.getSignal());
        }

        for (Peer p : peers) {
            post.send(packet).to(p);
        }
    }

    public Peer join(Peer peer) {
        peers.add(peer);

        new Thread(() -> {
            while (true) {
                try {
                    Packet packet = post.receive(peer);
                    if (packet instanceof SignalPacket) {
                        receive((SignalPacket) packet);
                    }
                } catch (Exception e) {
                    leave(peer);
                    return;
                }
            }
        }).start();

        return peer;
    }

    public void leave(Peer peer) {
        peers.remove(peer);
        peer.close();
    }
}
