package org.zells.dish.network;

import org.zells.dish.Signal;

import java.util.Arrays;
import java.util.List;

public class NetworkPost implements Post {

    private boolean isDebugging = false;
    private Encoding encoding;

    public NetworkPost(Encoding encoding) {
        this.encoding = encoding;
    }

    @Override
    public Packet receive(Receiver receiver) {
        //noinspection unchecked
        List<Signal> unpacked = (List<Signal>) encoding.decode(receiver);
        debug("Receive Packet: " + unpacked);
        return new SignalPacket(unpacked.get(0), unpacked.get(1));
    }

    @Override
    public void send(Packet packet, Sender sender) {
        if (packet instanceof SignalPacket) {
            Signal packed = encoding.encode(Arrays.asList(
                    ((SignalPacket) packet).getId(),
                    ((SignalPacket) packet).getSignal()));

            debug("Send Packet: " + packed);
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

    public NetworkPost debugging() {
        this.isDebugging = true;
        return this;
    }
}
