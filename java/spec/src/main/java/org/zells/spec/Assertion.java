package org.zells.spec;

class Assertion {

    Assertion(String name) {
        System.out.println();
        System.out.println("--------- " + name);
    }

    Assertion when(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("-> ERROR");
            System.exit(1);
        }
        return this;
    }

    Assertion then(Assert that) {
        int tries = 0;
        while (true) {

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored2) {
            }

            if (that.holds()) {
                break;
            }

            if (tries > 20) {
                System.out.println(that.getError());
                System.out.println("-> FAILED");
                System.exit(1);
            }

            tries++;
        }

        return this;
    }
}
