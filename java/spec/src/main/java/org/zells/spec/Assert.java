package org.zells.spec;

class Assert {
    private String error;
    private Runnable that;

    private Assert(Runnable that) {
        this.that = that;
    }

    boolean holds() {
        try {
            that.run();
            return true;
        } catch (Exception e) {
            error = e.getMessage();
            return false;
        }
    }

    String getError() {
        return error;
    }

    static Assert that(Deferred condition) {
        return new Assert(() -> {
            if (!(boolean)condition.eval()) {
                throw new RuntimeException("Condition failed");
            }
        });
    }

    static Assert equal(Object expected, Deferred actual) {
        return new Assert(() -> {
            Object evald = actual.eval();
            if (!expected.equals(evald)) {
                throw new RuntimeException("Expected " + evald + " to equal " + expected);
            }
        });
    }

    public interface Deferred {
        Object eval();
    }
}
