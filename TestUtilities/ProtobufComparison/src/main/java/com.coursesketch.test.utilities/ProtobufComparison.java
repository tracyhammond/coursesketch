package com.coursesketch.test.utilities;

import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Compares two protobuf objects.
 *
 * Throws an assertion error if the objects are not equal.
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparison {
    /**
     * A list containing fields that are not checked..
     */
    private final List<Descriptors.FieldDescriptor> ignoredFields;
    /**
     * A list contains messages that are not checked.
     */
    private final List<Descriptors.Descriptor> ignoredMessages;

    /**
     * Determines if nested protobufs are checked or if only the top level is checked.
     */
    private final boolean isDeepEquals;

    /**
     * If true it will ignore if the expected has fields not set but they actually have values in actual.
     */
    private final boolean ignoreNonSetFields;

    /**
     * if True then it will ignore cases where a field is set but it is set to the default value of the protobuf.
     * Basically this ignores fields that are set on the actual but not the expected if they are equal to the default value.
     */
    private final boolean ignoreSetDefaultFields;

    /**
     * If true then the first fail will be asserted instead of all fails.
     */
    private final boolean failAtFirstMisMatch;

    /**
     * If true then the list order is ignored when comparing protobuf objects otherwise the list is not ignored when comparing protobuf objects.
     */
    private final boolean ignoreListOrder;

    /**
     * Constructor for setting values.
     *  @param ignoredFields {@link #ignoredFields}.
     * @param ignoredMessages {@link #ignoredMessages}.
     * @param isDeepEquals {@link #isDeepEquals}.
     * @param ignoreNonSetFields {@link #ignoreNonSetFields}.
     * @param ignoreSetDefaultFields {@link #ignoreSetDefaultFields}.
     * @param failAtFirstMisMatch {@link #failAtFirstMisMatch}.
     * @param ignoreListOrder {@link #ignoreListOrder}.
     */
    public ProtobufComparison(final List<Descriptors.FieldDescriptor> ignoredFields, final List<Descriptors.Descriptor> ignoredMessages,
            final boolean isDeepEquals, final boolean ignoreNonSetFields, final boolean ignoreSetDefaultFields, final boolean failAtFirstMisMatch,
            final boolean ignoreListOrder) {
        this.isDeepEquals = isDeepEquals;
        this.ignoreNonSetFields = ignoreNonSetFields;
        this.ignoreSetDefaultFields = ignoreSetDefaultFields;
        this.failAtFirstMisMatch = failAtFirstMisMatch;
        this.ignoreListOrder = ignoreListOrder;
        this.ignoredFields = ignoredFields == null ? new ArrayList<Descriptors.FieldDescriptor>() : ignoredFields;
        this.ignoredMessages = ignoredMessages == null ? new ArrayList<Descriptors.Descriptor>() : ignoredMessages;
    }

    /**
     * Compares two Protobuf Messages.
     *
     * @param expected The expected message that is being compared against.
     * @param actual The protobuf that was generated during the test.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void equals(final GeneratedMessage expected, final GeneratedMessage actual) {
        // FUTURE: instead of field descriptor use a field descriptor chain in case there are duplicate (or repeated) objects.
        final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields = new HashMap<>();
        equals(expected, actual, incorrectFields);
        if (!failAtFirstMisMatch && incorrectFields.size() > 0) {
            final StringBuilder message = new StringBuilder();
            message.append("There were " + incorrectFields.size() + " mismatches in this protobuf:");
            for (Map.Entry<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> field : incorrectFields.entrySet()) {
                final ExpectationPair<Object, Object> pair = field.getValue();
                message.append("\n").append(createFailMessage(field.getKey(), pair.getExpected(), pair.getActual()));
            }
            Assert.fail(message.toString());
        }
    }

    /**
     * Compares two Protobuf Messages.
     *
     * @param expected The expected message that is being compared against.
     * @param actual The protobuf that was generated during the test.
     * @param incorrectFields A map containing fields that were found to be incorrect.
     */
    private void equals(final GeneratedMessage expected, final GeneratedMessage actual,
            final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields) {
        if (!expected.getDescriptorForType().equals(actual.getDescriptorForType())) {
            Assert.fail("Expected Message " + expected.getDescriptorForType().getFullName()
                    + " but got Message " + actual.getDescriptorForType().getFullName());
        }

        final List<Descriptors.FieldDescriptor> expectedAllFields = expected.getDescriptorForType().getFields();

        for (Descriptors.FieldDescriptor field : expectedAllFields) {
            if (ignoredFields.contains(field)) {
                continue;
            }
            if (!field.isRepeated() && !expected.hasField(field)) {
                if (ignoreSetDefaultFields && actual.hasField(field)) {
                    if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                        compareFields(expected.getField(field), actual.getField(field), field, incorrectFields);
                    } else {
                        compareFields(expected.getField(field), actual.getField(field), field, incorrectFields);
                    }
                    // if the comparing of the fields passes then it overrides the ignoreNonSetFields
                    continue;
                }
                if (!ignoreNonSetFields && actual.hasField(field)) {
                    if (failAtFirstMisMatch) {
                        Assert.fail(createFailMessage(field, null, actual.getField(field)));
                    } else {
                        incorrectFields.put(field, new ExpectationPair<Object, Object>(null, actual.getField(field)));
                    }
                }
                continue;
            }
            final Object expectedValue = expected.getField(field);
            final Object actualValue = actual.getField(field);
            compareFields(expectedValue, actualValue, field, incorrectFields);
        }
    }

    /**
     * Compares two fields to each other.
     *
     * @param expectedValue The expected value for this specific field.
     * @param actualValue The actual value for this specific field.
     * @param field The field that is being compared.
     * @param incorrectFields A map containing fields that were found to be incorrect.
     */
    private void compareFields(final Object expectedValue, final Object actualValue, final Descriptors.FieldDescriptor field,
            final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields) {
        if (field.isRepeated()) {
            compareRepeatedFields(expectedValue, actualValue, field, incorrectFields);
            return;
        }
        if (expectedValue instanceof GeneratedMessage) {
            if (!(actualValue instanceof GeneratedMessage)) {
                Assert.fail("Different types");
            }
            if (isDeepEquals) {
                equals((GeneratedMessage) expectedValue, (GeneratedMessage) actualValue, incorrectFields);
            }
            return;
        }
        if (!expectedValue.equals(actualValue)) {
            if (failAtFirstMisMatch) {
                Assert.fail(createFailMessage(field, expectedValue, actualValue));
            } else {
                incorrectFields.put(field, new ExpectationPair<Object, Object>(expectedValue, actualValue));
            }
        }
    }

    /**
     * Compares two fields to each other.
     *
     * @param expectedValue The expected value for this specific field.
     * @param actualValue The actual value for this specific field.
     * @param field The field that is being compared.
     * @param incorrectFields A map containing fields that were found to be incorrect.
     * @return True if the data is equal, false otherwise.
     */
    private boolean compareDataOfRepeatedField(final Object expectedValue, final Object actualValue, final Descriptors.FieldDescriptor field,
            final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields) {
        if (expectedValue instanceof GeneratedMessage) {
            final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFieldClone = new HashMap<>();
            if (isDeepEquals) {
                equals((GeneratedMessage) expectedValue, (GeneratedMessage) actualValue, incorrectFieldClone);
            }

            if (incorrectFieldClone.size() > 0) {
                if (this.ignoreListOrder) {
                    return false;
                } else {
                    incorrectFields.putAll(incorrectFieldClone);
                    return false;
                }
            }
        }
        if (!expectedValue.equals(actualValue)) {
            if (this.ignoreListOrder) {
                return false;
            } else {
                incorrectFields.put(field, new ExpectationPair<Object, Object>(expectedValue, actualValue));
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the fields as if they are a list of messages (or simple objects).
     *
     * @param expectedValue The expected value for this specific field.
     * @param actualValue The actual value for this specific field.
     * @param field The field that is being compared.
     * @param incorrectFields A map containing fields that were found to be incorrect.
     */
    private void compareRepeatedFields(final Object expectedValue, final Object actualValue, final Descriptors.FieldDescriptor field,
            final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields) {
        final List expectedList = Lists.newArrayList((Iterable) expectedValue);
        final List actualList = Lists.newArrayList((Iterable) actualValue);
        Iterator actualListIterator = actualList.iterator();
        for (Object expectedValueItem : expectedList) {
            if (ignoreListOrder) {
                boolean foundOneMatch = false;
                Object actualValueItem = null;
                while (actualListIterator.hasNext()) {
                    actualValueItem = actualListIterator.next();
                    final boolean resultOfComparison = compareDataOfRepeatedField(expectedValueItem, actualValueItem, field, incorrectFields);
                    if (!resultOfComparison) {
                        continue;
                    }
                    foundOneMatch = true;
                    actualListIterator.remove();
                    break;
                }
                if (!foundOneMatch) {
                    if (failAtFirstMisMatch) {
                        Assert.fail(createFailMessage(field, expectedValueItem, null));
                    } else if (!failAtFirstMisMatch) {
                        incorrectFields.put(field, new ExpectationPair<Object, Object>(expectedValueItem, null));
                        continue;
                    }
                }
                actualListIterator = actualList.iterator();
                // Need to remove items from the list as they are matched!0

            } else {
                final Object actualValueItem = actualListIterator.next();
                final boolean resultOfComparison = compareDataOfRepeatedField(expectedValueItem, actualValueItem, field, incorrectFields);
                if (!resultOfComparison && failAtFirstMisMatch) {
                    Assert.fail(createFailMessage(field, expectedValueItem, actualValueItem));
                } else if (!failAtFirstMisMatch) {
                    continue;
                }
            }
        }
        // After comparing the objects

        // only if we are not ignoring list order
        while (actualListIterator.hasNext()) {
            if (failAtFirstMisMatch) {
                Assert.fail(createFailMessage(field, "Extra value in list: ", actualListIterator.next()));
            } else {
                incorrectFields.put(field, new ExpectationPair<Object, Object>("Extra value in list: ", actualListIterator.next()));
            }
        }
    }

    /**
     * Creates a fail message.
     *
     * @param field The field that failed.
     * @param expectedValue If this value is null we assume that it was not set in the expected protobuf.
     * @param actualValue The value that failed to match the expected value.
     * @return A string representing the failed message.
     */
    private String createFailMessage(final Descriptors.FieldDescriptor field, final Object expectedValue, final Object actualValue) {
        if (expectedValue == null) {
            return "Expected no value for field <" + field.getFullName() + "> but instead got value <" + actualValue + ">";
        }
        return "Expected <" + expectedValue + "> but got <" + actualValue + "> for field [" + field.getFullName() + "]";
    }
}
