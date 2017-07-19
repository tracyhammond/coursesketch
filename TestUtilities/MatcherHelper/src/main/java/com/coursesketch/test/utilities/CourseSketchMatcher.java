package com.coursesketch.test.utilities;

import org.hamcrest.Matcher;

import java.util.Arrays;

/**
 * Created by dtracers on 12/27/2015.
 */
public final class CourseSketchMatcher {

    /**
     * Creates a matcher for the given list that can be matched in any order.
     *
     * It takes in an iterable.
     * @param list The expected list that is being matched against.
     * @param <E> Must extend comparable as the lists are sorted before matching.
     * @return A matcher that compares lists that can be in any order.
     */
    public static <E extends Comparable> Matcher<E[]> iterableEqualAnyOrder(final Iterable<E> list) {
        return new ArrayInAnyOrderMatcher<E>(list);
    }

    /**
     * Creates a matcher for the given list that can be matched in any order.
     *
     * It takes in an array.
     * @param list The expected list that is being matched against.
     * @param <E> Must extend comparable as the lists are sorted before matching.
     * @return A matcher that compares lists that can be in any order.
     */
    public static <E extends Comparable> Matcher<E[]> iterableEqualAnyOrder(final E[] list) {
        return new ArrayInAnyOrderMatcher<E>(Arrays.asList(list));
    }

}
