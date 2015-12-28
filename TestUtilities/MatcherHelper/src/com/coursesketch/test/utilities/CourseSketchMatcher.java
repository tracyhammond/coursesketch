package com.coursesketch.test.utilities;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;

/**
 * Created by dtracers on 12/27/2015.
 */
public final class CourseSketchMatcher {

    public static <E extends Comparable> Matcher<E[]> iterableEqualAnyOrder(final Iterable<E> list) {
        return new ArrayInAnyOrderMatcher<E>(list);
    }

    public static <E extends Comparable> Matcher<E[]> iterableEqualAnyOrder(final E[] list) {
        return new ArrayInAnyOrderMatcher<E>(Arrays.asList(list));
    }

}
