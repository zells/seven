package org.zells.dish.core.impl;

import org.zells.dish.core.Dish;
import org.zells.dish.core.Peer;
import org.zells.dish.core.Signal;
import org.zells.dish.core.Zell;
import org.zells.dish.network.packets.SignalPacket;
import org.zells.dish.network.Packet;
import org.zells.dish.network.Post;

import java.util.HashSet;
import java.util.Set;

public class StandardDish implements Dish {

    private final Post post;

    private Set<Peer> peers = new HashSet<>();
    private Set<Zell> zells = new HashSet<>();
    private Set<Signal> transmitted = new HashSet<>();

    public StandardDish(Post post) {
        this.post = post;
    }

    public Zell put(Zell zell) {
        zells.add(zell);
        return zell;
    }

    public void remove(Zell zell) {
        zells.remove(zell);
    }

    public void transmit(Signal signal) {
        receive(new SignalPacket(signal));
    }

    private void receive(SignalPacket packet) {
        if (transmitted.contains(packet.getId())) {
            return;
        }
        transmitted.add(packet.getId());

        for (Zell z : new HashSet<>(zells)) {
            z.receive(packet.getSignal());
        }

        for (Peer p : new HashSet<>(peers)) {
            try {
                post.send(packet, bytes -> p.send(StandardSignal.from(bytes)));
            } catch (Exception e) {
                leave(p);
            }
        }
    }

    public Peer join(Peer peer) {
        peers.add(peer);

        new Thread(() -> {
            while (true) {
                try {
                    Packet packet = post.receive(peer::receive);
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
