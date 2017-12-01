package org.zells.dish.network;

import org.zells.dish.Signal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefaultPost implements Post {

    private boolean isDebugging = false;

    @Override
    public Sender send(SignalPacket packet) {
        return peer -> {
            debug("SignalPacket: " + packet.getId() + " 00 " + packet.getSignal() + " 00");

            peer.write(packet.getId().toBytes());
            peer.write(new byte[]{0});
            peer.write(packet.getSignal().toBytes());
            peer.write(new byte[]{0});
        };
    }

    @Override
    public Packet receive(Peer peer) throws IOException {
        byte read;

        List<Byte> id = new ArrayList<>();
        while ((read = peer.read()) != 0) {
            id.add(read);
        }

        List<Byte> signal = new ArrayList<>();
        while ((read = peer.read()) != 0) {
            signal.add(read);
        }

        return new SignalPacket(new Signal(id), new Signal(signal));
    }

    private void debug(String message) {
        if (isDebugging) {
            System.out.println(message);
        }
    }

    public DefaultPost debugging() {
        this.isDebugging = true;
        return this;
    }
}
