package org.zells.dish;

import org.zells.dish.network.*;

import java.io.IOException;
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

        for (Zell z : new HashSet<>(zells)) {
            z.receive(packet.getSignal());
        }

        for (Peer p : new HashSet<>(peers)) {
            try {
                post.send(packet, p);
            } catch (Sender.SenderClosedException e) {
                leave(p);
            }
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
                } catch (Receiver.ReceiverClosedException e) {
                    leave(peer);
                    return;
                }
            }
        }).start();

        return peer;
    }

    public void leave(Peer peer) {
        peers.remove(peer);

        try {
            peer.close();
        } catch (IOException ignored) {
        }
    }
}
