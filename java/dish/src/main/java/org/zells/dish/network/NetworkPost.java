package org.zells.dish.network;

import org.zells.dish.Signal;

public class NetworkPost implements Post {

    private boolean isDebugging = false;
    private Encoding encoding;

    public NetworkPost(Encoding encoding) {
        this.encoding = encoding;
    }

    @Override
    public Packet receive(Receiver receiver) {
        Signal id = (Signal) encoding.decode(receiver);
        Signal signal = (Signal) encoding.decode(receiver);

        return new SignalPacket(id, signal);
    }

    @Override
    public void send(Packet packet, Sender sender) {
        if (packet instanceof SignalPacket) {
            Signal one = encoding.encode(((SignalPacket) packet).getId());
            Signal two = encoding.encode(((SignalPacket) packet).getSignal());

            debug("SignalPacket: " + one + " " + two);

            sender.send(one);
            sender.send(two);
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
