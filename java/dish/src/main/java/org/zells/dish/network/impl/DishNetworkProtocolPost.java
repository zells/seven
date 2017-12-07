package org.zells.dish.network.impl;

import org.zells.dish.core.Signal;
import org.zells.dish.codec.Codec;
import org.zells.dish.core.impl.StandardSignal;
import org.zells.dish.network.Packet;
import org.zells.dish.network.Post;
import org.zells.dish.network.Receiver;
import org.zells.dish.network.Sender;
import org.zells.dish.network.packets.SignalPacket;

import java.util.Arrays;
import java.util.List;

public class DishNetworkProtocolPost implements Post {

    private boolean isDebugging = false;
    private Codec codec;

    public DishNetworkProtocolPost(Codec codec) {
        this.codec = codec;
    }

    @Override
    public Packet receive(Receiver receiver) {
        //noinspection unchecked
        List<Signal> unpacked = (List<Signal>) codec.decode(receiver::receive);
        debug("Receive Packet: " + unpacked);
        return new SignalPacket(unpacked.get(0), unpacked.get(1));
    }

    @Override
    public void send(Packet packet, Sender sender) {
        if (packet instanceof SignalPacket) {
            byte[] packed = codec.encode(Arrays.asList(
                    ((SignalPacket) packet).getId(),
                    ((SignalPacket) packet).getSignal()));

            debug("Send Packet: " + StandardSignal.from(packed));
            sender.send(packed);
        } else {
            throw new RuntimeException("Unknown packet: " + packet);
        }
    }

    private void debug(String message) {
        if (isDebugging) {
            System.out.println(message);
        }
    }

    public DishNetworkProtocolPost debugging() {
        this.isDebugging = true;
        return this;
    }
}
