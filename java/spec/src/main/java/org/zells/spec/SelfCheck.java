package org.zells.spec;

public class SelfCheck {

    public static void main(final String[] args) {
        new Thread(() -> Implementation.main(args)).start();

        new Thread(() -> Specification.main(args)).start();
    }
}
