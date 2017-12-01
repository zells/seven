package org.zells.dish.network;

import org.zells.dish.Signal;

import java.util.ArrayList;
import java.util.List;

public class DefaultPost implements Post {

    private boolean isDebugging = false;

    @Override
    public Sender send(SignalPacket packet) {
        return peer -> {
            debug("SignalPacket: " + packet + " 00");
            peer.write(packet.getSignal().toBytes());
            peer.write(new byte[]{0});
        };
    }

    @Override
    public Packet receive(Peer peer) {
        List<Byte> bytes = new ArrayList<>();

        byte read;
        while ((read = peer.read()) != 0) {
            bytes.add(read);
        }

        return new SignalPacket(new Signal(bytes));
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
