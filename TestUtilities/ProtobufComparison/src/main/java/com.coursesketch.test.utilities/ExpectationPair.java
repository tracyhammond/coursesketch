package com.coursesketch.test.utilities;

/**
 * For comparing protobuf objects.
 *
 * Things to note! if the value should not exist then expected is null.
 * Created by gigemjt on 9/9/15.
 *
 * @param <A>
 *         The type of the expected Object
 * @param <B>
 *         The type of the actual Object.
 */
public final class ExpectationPair<A, B> {
    /**
     * The expected value.
     */
    private A expected;
    /**
     * The actual value.
     */
    private B actual;

    /**
     * @param expected
     *         The expected object.
     * @param actual
     *         The actual object.
     */
    public ExpectationPair(final A expected, final B actual) {
        super();
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * @return The hashcode of ehe pair.
     */
    public int hashCode() {
        final int hashFirst = expected != null ? expected.hashCode() : 0;
        final int hashSecond = actual != null ? actual.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    /**
     * @param other Another {@link ExpectationPair}.
     * @return True if the two pairs are equal.
     */
    public boolean equals(final Object other) {
        if (other instanceof ExpectationPair) {
            final ExpectationPair otherPair = (ExpectationPair) other;
            return ((this.expected == otherPair.expected
                            || (this.expected != null && otherPair.expected != null
                                    && this.expected.equals(otherPair.expected)))
                    && (this.actual == otherPair.actual
                            || (this.actual != null && otherPair.actual != null
                                    && this.actual.equals(otherPair.actual))));
        }

        return false;
    }

    /**
     * @return A string representation of this object.
     */
    public String toString() {
        return "(" + expected + ", " + actual + ")";
    }

    /**
     * @return The expected value.
     */
    public A getExpected() {
        return expected;
    }

    /**
     * @param expected The expected value.
     */
    public void setExpected(final A expected) {
        this.expected = expected;
    }

    /**
     * @return The actual value.
     */
    public B getActual() {
        return actual;
    }

    /**
     * @param actual THe actual value.
     */
    public void setActual(final B actual) {
        this.actual = actual;
    }
}
