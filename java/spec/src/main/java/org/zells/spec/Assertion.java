package org.zells.spec;

class Assertion {

    private Runnable action;

    Assertion(String name) {
        System.out.println();
        System.out.println("--------- " + name);
    }

    Assertion when(Runnable action) {
        this.action = action;
        return this;
    }

    Assertion thenAssert(Condition condition) {
        int tries = 0;
        while (true) {
            action.run();

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored2) {
            }

            if (condition.holds()) {
                break;
            }

            if (tries > 20) {
                System.out.println("-> FAILED");
                System.exit(1);
            }

            tries++;
        }

        return this;
    }

    interface Condition {
        boolean holds();
    }
}
