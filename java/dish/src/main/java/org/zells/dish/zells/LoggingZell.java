package org.zells.dish.zells;

import org.zells.dish.Zell;

import java.util.ArrayList;
import java.util.List;

public class LoggingZell implements Zell {
    private List<Integer> received = new ArrayList<>();

    public void receive(int signal) {
        received.add(signal);
    }

    public boolean hasReceived(int signal) {
        return received.contains(signal);
    }
}