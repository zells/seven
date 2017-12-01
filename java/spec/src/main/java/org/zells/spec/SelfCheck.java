package org.zells.spec;

import java.io.IOException;

public class SelfCheck {

    public static void main(final String[] args) throws IOException {
        Implementation.main(args);
        Specification.main(args);
    }
}
