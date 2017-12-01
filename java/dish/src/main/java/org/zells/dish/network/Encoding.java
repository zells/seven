package org.zells.dish.network;

import org.zells.dish.Signal;

import java.io.IOException;

interface Encoding {

    byte[] encode(Signal signal);

    Signal decode(Peer peer) throws IOException;
}
