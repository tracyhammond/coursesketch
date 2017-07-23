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
     * Used for nested objects (mainly deep equals of repeated fields).
     */
    private ProtobufComparisonBuilder nestedBuilder;

    /**
     * Constructor for setting values.
     *
     * @param ignoredFields {@link #ignoredFields}.
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
        final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields = new HashMap<>();
        equals(expected, actual, incorrectFields);
        if (!failAtFirstMisMatch && incorrectFields.size() > 0) {
            final StringBuilder message = new StringBuilder();
            message.append("There were " + incorrectFields.size() + " mismatches in this protobuf:");
            for (Map.Entry<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> field : incorrectFields.entrySet()) {
                final List<ExpectationPair<Object, Object>> pairList = field.getValue();
                for (ExpectationPair<Object, Object> pair : pairList) {
                    message.append("\n").append(createFailMessage(field.getKey(), pair.getExpected(), pair.getActual()));
                }
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
            final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields) {
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
                        addIncorrectField(incorrectFields, field, null, actual.getField(field));
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
            final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields) {
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
                addIncorrectField(incorrectFields, field, expectedValue, actualValue);
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
     * @param nestedComparator A comparator used when comparing nested objects.
     * @return True if the data is equal, false otherwise.
     */
    private boolean compareDataOfRepeatedField(final Object expectedValue, final Object actualValue, final Descriptors.FieldDescriptor field,
            final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields,
            final ProtobufComparison nestedComparator) {
        if (expectedValue instanceof GeneratedMessage) {
            final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFieldClone = new HashMap<>();
            if (isDeepEquals) {
                nestedComparator.equals((GeneratedMessage) expectedValue, (GeneratedMessage) actualValue, incorrectFieldClone);
            }
            if (incorrectFieldClone.size() > 0) {
                if (this.ignoreListOrder) {
                    return false;
                } else {
                    // Copy all of the values from the clone into the original.
                    for (Map.Entry<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> fieldEntry : incorrectFields.entrySet()) {
                        addIncorrectFieldList(incorrectFields, fieldEntry.getKey(), fieldEntry.getValue());
                    }
                    incorrectFields.putAll(incorrectFieldClone);
                    return false;
                }
            } else {
                return true;
            }
        }
        if (!expectedValue.equals(actualValue)) {
            if (this.ignoreListOrder) {
                return false;
            } else {
                addIncorrectField(incorrectFields, field, expectedValue, actualValue);
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
            final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields) {

        if (expectedValue == null || actualValue == null) {
            throw new IllegalArgumentException("Null lists are not supported");
        }

        final List expectedList = Lists.newArrayList((Iterable) expectedValue);
        final List actualList = Lists.newArrayList((Iterable) actualValue);

        // Checking list values themselves (expected is 0 but actual is not)
        if (expectedList.size() == 0 && actualList.size() > 0) {
            if (failAtFirstMisMatch) {
                Assert.fail(createFailMessage(field, null, actualValue));
            } else {
                addIncorrectField(incorrectFields, field, null, actualValue);
            }
            return;
        }

        // Checking list values themselves (checks the opposite! that actual is empty but expected is not empty.)
        if (actualList.size() == 0 && expectedList.size() > 0) {
            if (failAtFirstMisMatch) {
                Assert.fail(createFailMessage(field, expectedValue, null));
            } else {
                addIncorrectField(incorrectFields, field, expectedValue, null);
            }
            return;
        }

        Iterator actualListIterator = actualList.iterator();
        ProtobufComparison nestedComartor = this;
        if (ignoreListOrder) {
            nestedComartor = new ProtobufComparisonBuilder(nestedBuilder).setFailAtFirstMisMatch(false).build();
        }
        for (Object expectedValueItem : expectedList) {
            if (ignoreListOrder) {
                actualListIterator = actualList.iterator();
                boolean foundOneMatch = false;
                Object actualValueItem = null;
                while (actualListIterator.hasNext()) {
                    actualValueItem = actualListIterator.next();
                    final boolean resultOfComparison = compareDataOfRepeatedField(expectedValueItem, actualValueItem,
                            field, incorrectFields, nestedComartor);
                    if (!resultOfComparison) {
                        continue;
                    }
                    foundOneMatch = true;
                    actualList.remove(actualValueItem);
                    break;
                }
                if (!foundOneMatch) {
                    if (failAtFirstMisMatch) {
                        Assert.fail(createFailMessage(field, expectedValueItem, null));
                    } else {
                        addIncorrectField(incorrectFields, field, expectedValueItem, null);
                    }
                }
                // Need to remove items from the list as they are matched!0

            } else {
                if (!actualListIterator.hasNext()) {
                    if (failAtFirstMisMatch) {
                        Assert.fail(createFailMessage(field, expectedValueItem, null));
                    } else {
                        addIncorrectField(incorrectFields, field, expectedValueItem, null);
                    }
                    continue;
                }
                final Object actualValueItem = actualListIterator.next();
                final boolean resultOfComparison = compareDataOfRepeatedField(expectedValueItem, actualValueItem,
                        field, incorrectFields, nestedComartor);
                if (!resultOfComparison) {
                    if (failAtFirstMisMatch) {
                        Assert.fail(createFailMessage(field, expectedValueItem, actualValueItem));
                    } else {
                        addIncorrectField(incorrectFields, field, expectedValueItem, actualValueItem);
                    }
                }
            }
        }
        // After comparing the objects

        actualListIterator = actualList.iterator();
        // only if we are not ignoring list order
        while (ignoreListOrder && actualListIterator.hasNext()) {
            if (failAtFirstMisMatch) {
                Assert.fail(createFailMessage(field, null, actualListIterator.next()));
            } else {
                addIncorrectField(incorrectFields, field, null, actualListIterator.next());
            }
        }

        if (!ignoreListOrder && actualList.size() > expectedList.size()) {
            for (int i = expectedList.size(); i < actualList.size(); i++) {
                if (failAtFirstMisMatch) {
                    Assert.fail(createFailMessage(field, null, actualList.get(i)));
                } else {
                    addIncorrectField(incorrectFields, field, null, actualList.get(i));
                }
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
        if (field.isRepeated()) {
            return createFailListMessage(field, expectedValue, actualValue);
        } else if (expectedValue == null) {
            return "Expected no value for field [" + field.getFullName() + "] but instead got value <" + actualValue + ">";
        }
        return "Expected <" + expectedValue + "> but got <" + actualValue + "> for field [" + field.getFullName() + "]";
    }

    /**
     * Creates a fail message.
     *
     * @param field The field that failed.
     * @param expectedValue If this value is null we assume that it was not set in the expected protobuf.
     * @param actualValue The value that failed to match the expected value.
     * @return A string representing the failed message.
     */
    private String createFailListMessage(final Descriptors.FieldDescriptor field, final Object expectedValue, final Object actualValue) {
        if (expectedValue == null && actualValue != null) {
            if (actualValue instanceof Iterable) {
                return "Expected no value for field [" + field.getFullName() + "] but instead got list <" + actualValue + ">";
            } else {
                return "Extra value <" + actualValue + "> found in list for field [" + field.getFullName() + "]";
            }
        } else if (expectedValue != null && actualValue == null) {
            if (expectedValue instanceof Iterable) {
                return "Expected list <" + expectedValue + "> but got <" + null + "> for field [" + field.getFullName() + "]";
            } else {
                return "Expected value <" + expectedValue + "> was not found in the actual list for field [" + field.getFullName() + "]";
            }
        } else if (expectedValue instanceof Iterator || actualValue instanceof Iterator) {
            return "Expected <" + expectedValue + "> but got <" + actualValue + "> for field [" + field.getFullName() + "]";

        }
        return "Expected <" + expectedValue + "> but got <" + actualValue + "> for a list item in field [" + field.getFullName() + "]";
    }

    private void addIncorrectField(final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields,
            final Descriptors.FieldDescriptor field, final Object expectedValue, final Object actualValue) {
        final ExpectationPair<Object, Object> objectExpectationPair = new ExpectationPair<>(expectedValue, actualValue);
        if (incorrectFields.containsKey(field)) {
            incorrectFields.get(field).add(objectExpectationPair);
        } else {
            final List<ExpectationPair<Object, Object>> list = new ArrayList<>();
            list.add(objectExpectationPair);
            incorrectFields.put(field, list);
        }
    }

    private void addIncorrectFieldList(final Map<Descriptors.FieldDescriptor, List<ExpectationPair<Object, Object>>> incorrectFields,
            final Descriptors.FieldDescriptor field, List<ExpectationPair<Object, Object>> expectationPairs) {
        if (incorrectFields.containsKey(field)) {
            incorrectFields.get(field).addAll(expectationPairs);
        } else {
            final List<ExpectationPair<Object, Object>> list = new ArrayList<>();
            list.addAll(expectationPairs);
            incorrectFields.put(field, list);
        }
    }

    /**
     * Sets a builder that is used for nested purposes.
     *
     * @param nestedBuilder The builder that is used on nested objects.
     */
    public final void setNestedBuilder(final ProtobufComparisonBuilder nestedBuilder) {
        this.nestedBuilder = nestedBuilder;
    }
}
