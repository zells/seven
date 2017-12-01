package org.zells.spec;

public class SelfCheck {

    public static void main(final String[] args) {
        new Thread(new Runnable() {
            public void run() {
                Implementation.main(args);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                Specification.main(args);
            }
        }).start();
    }
}
