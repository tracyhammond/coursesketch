package com.coursesketch.test.utilities;

import org.bson.Document;
import org.junit.Assert;

import java.util.List;
import java.util.Objects;

/**
 * Created by gigemjt on 9/5/15.
 */
public final class DatabaseHelper {
    /**
     * This can take a real object id and make it into one that probabilistically should not exist.
     *
     * @param objectId The object id that we want to make invalid.
     * @return A string that should be a valid Id but should not point to anything in the database.
     */
    public static String createNonExistentObjectId(final String objectId) {
        return objectId.replaceAll("4", "7")
                .replaceAll("2", "4")
                .replaceAll("9", "2")
                .replaceAll("8", "9")
                .replaceAll("1", "8")
                .replaceAll("6", "1")
                .replaceAll("5", "6");
    }

    public static void assertDocumentEquals(Document expected, Document actual) {
        assertDocumentEquals(expected, actual, new DocumentComparisonOptions());
    }

    public static void assertDocumentEquals(Document expected, Document actual, DocumentComparisonOptions options) {
        System.out.println("Comparing Documents");
        System.out.println(expected.toString());
        System.out.println(actual.toString());
        assertDocumentEquals("", expected, actual, options);
    }

    private static void assertDocumentEquals(String key, Document expected, Document actual, DocumentComparisonOptions options) {
        for (String s : expected.keySet()) {
            String mergedKey = key + "." + s;
            Assert.assertEquals("Equality of document for key: " + mergedKey, expected.containsKey(s), actual.containsKey(s));
            Object expectedObj = expected.get(s);
            Object actualObj = actual.get(s);
            compareObjectForDocument(mergedKey, expectedObj, actualObj, options);
        }
    }

    private static void compareObjectForDocument(String key, Object expected, Object actual, DocumentComparisonOptions options) {
        if ((expected == null && actual != null) || (expected != null && actual == null)) {
            Assert.assertEquals(expected, actual);
        }
        if (options.ignoreFloatDoubleComparisons) {
            if (!(isFloatOrDouble(expected, actual))) {
                Assert.assertEquals("Objects are of same instance for key: " + key, expected.getClass(), actual.getClass());
            }
        } else {
            Assert.assertEquals("Objects are of same instance for key: " + key, expected.getClass(), actual.getClass());
        }
        if (expected instanceof Document) {
            assertDocumentEquals(key, (Document) expected, (Document) actual, options);
        } else if (expected instanceof List) {
            List<Object> expectedList = (List<Object>) expected;
            List<Object> actualList = (List<Object>) actual;
            Assert.assertEquals("List has same size for key " + key, expectedList.size(), actualList.size());
            for (int i = 0; i < expectedList.size(); i++) {
                compareObjectForDocument(key + "[" + i + "]", expectedList.get(0), actualList.get(0), options);
            }
        } else if (isFloatOrDouble(expected, actual)) {
            Assert.assertEquals("comparing values for key: " + key, ((Number) expected).doubleValue(), ((Number) actual).doubleValue(), 0);
        } else {
            Assert.assertEquals("comparing values for key: " + key, expected, actual);
        }
    }

    private static boolean isFloatOrDouble(Object expected, Object actual) {
        return (expected instanceof Float || (expected instanceof Double))
                && (actual instanceof Float || (actual instanceof Double));
    }
}
