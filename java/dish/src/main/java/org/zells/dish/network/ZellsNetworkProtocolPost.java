package org.zells.dish.network;

import org.zells.dish.Signal;

import java.io.IOException;

public class ZellsNetworkProtocolPost implements Post {

    private boolean isDebugging = false;
    private Encoding encoding;

    public ZellsNetworkProtocolPost(Encoding encoding) {
        this.encoding = encoding;
    }

    @Override
    public Sender send(SignalPacket packet) {
        return peer -> {
            byte[] one = encoding.encode(packet.getId());
            byte[] two = encoding.encode(packet.getSignal());

            debug("SignalPacket: " + printHex(one) + " " + printHex(two));

            peer.write(one);
            peer.write(two);
        };
    }

    private String printHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @Override
    public Packet receive(Peer peer) throws IOException {
        Signal id = encoding.decode(peer);
        Signal signal = encoding.decode(peer);

        return new SignalPacket(id, signal);
    }

    private void debug(String message) {
        if (isDebugging) {
            System.out.println(message);
        }
    }

    public ZellsNetworkProtocolPost debugging() {
        this.isDebugging = true;
        return this;
    }
}
