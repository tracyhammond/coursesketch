package com.coursesketch.test.utilities;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.VarargMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A matcher that matches lists or varags in any order.
 * Created by dtracers on 12/27/2015.
 *
 * @param <E> An element that should be an instance of Comparable
 */
public final class ArrayInAnyOrderMatcher<E> extends ArgumentMatcher<E[]> implements VarargMatcher {

    /**
     * Declaration and Definition.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ArrayInAnyOrderMatcher.class);

    /**
     * The list that is expected.
     */
    private final Iterable<E> listToMatchTo;

    /**
     * Constructor that takes in the iterable.
     * @param listToMatchTo The expected list that is compared later on.
     */
    public ArrayInAnyOrderMatcher(final Iterable<E> listToMatchTo) {
        this.listToMatchTo = listToMatchTo;
    }

    /**
     * Matches if the given object matches the Iterable.
     * @param es
     * @return
     */
    @Override public boolean matches(final Object es) {
        List<E> casting = null;
        if (es.getClass().isArray()) {
            final E[] obj = (E[]) es;
            casting = Arrays.asList(obj);
        } else if (es instanceof Iterable) {
            casting = new ArrayList<>();
            for (E element: (Iterable<E>) es) {
                casting.add(element);
            }
        } else {
            casting = (List<E>) Arrays.asList(es);
        }
        final List<Comparable> matchingList = new ArrayList<>();
        final List<Comparable> expectedList = new ArrayList<>();

        // converts E -> Comparable for the sort later on
        for (E element: casting) {
            matchingList.add((Comparable) element);
        }

        for (E element: listToMatchTo) {
            expectedList.add((Comparable) element);
        }

        if (matchingList.size() != expectedList.size()) {
            return false;
        }
        Collections.sort(matchingList);
        Collections.sort(expectedList);

        for (int k = 0; k < matchingList.size(); k++) {
            if (!expectedList.get(k).equals(matchingList.get(k))) {
                LOG.error("{} does not match {}", expectedList.get(k), matchingList.get(k));
                return false;
            }
        }
        return true;
    }

    @Override public void describeTo(final Description description) {
        description.appendText(listToMatchTo.toString());
    }

}
