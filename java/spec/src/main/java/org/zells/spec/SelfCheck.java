package org.zells.spec;

import java.io.IOException;

public class SelfCheck {

    public static void main(final String[] args) throws IOException {
//        new Thread(() -> {
//            try {
                Implementation.main(args);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//
//        new Thread(() -> {
//            try {
                Specification.main(args);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }
}
