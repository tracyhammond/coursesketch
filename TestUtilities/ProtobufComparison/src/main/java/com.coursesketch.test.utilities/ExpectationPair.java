package com.coursesketch.test.utilities;

/**
 * For comparing protobuf objects.
 *
 * Things to note! if the value should not exist then expected is null.
 * Created by gigemjt on 9/9/15.
 */
public final class ExpectationPair<A, B> {
    private A expected;
    private B actual;

    /**
     *
     * @param expected
     * @param actual
     */
    public ExpectationPair(final A expected, final B actual) {
        super();
        this.expected = expected;
        this.actual = actual;
    }

    public int hashCode() {
        int hashFirst = expected != null ? expected.hashCode() : 0;
        int hashSecond = actual != null ? actual.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
        if (other instanceof ExpectationPair) {
            ExpectationPair otherPair = (ExpectationPair) other;
            return
                    ((  this.expected == otherPair.expected ||
                            ( this.expected != null && otherPair.expected != null &&
                                    this.expected.equals(otherPair.expected))) &&
                            (	this.actual == otherPair.actual ||
                                    ( this.actual != null && otherPair.actual != null &&
                                            this.actual.equals(otherPair.actual))) );
        }

        return false;
    }

    public String toString()
    {
        return "(" + expected + ", " + actual + ")";
    }

    public A getExpected() {
        return expected;
    }

    public void setExpected(A expected) {
        this.expected = expected;
    }

    public B getActual() {
        return actual;
    }

    public void setActual(B actual) {
        this.actual = actual;
    }
}
