package org.zells.dish.network;

import org.zells.dish.Signal;

interface Encoding {

    Signal encode(Object object);

    Object decode(Receiver receiver);
}
