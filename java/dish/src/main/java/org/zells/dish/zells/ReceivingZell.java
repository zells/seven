package org.zells.dish.zells;

import org.zells.dish.Signal;
import org.zells.dish.Zell;

import java.util.ArrayList;
import java.util.List;

public class ReceivingZell implements Zell {

    private List<Signal> received = new ArrayList<>();

    public void receive(Signal signal) {
        received.add(signal);
    }

    public boolean hasReceived(Signal signal) {
        return received.contains(signal);
    }
}
