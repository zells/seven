package org.zells.dish.network;

import org.zells.dish.Signal;

public interface Encoding {

    Signal encode(Object object);

    Object decode(Receiver receiver);
}
