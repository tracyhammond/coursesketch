package com.coursesketch.test.utilities;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparison {
    private final List<Descriptors.FieldDescriptor> ignoredFields;
    private final List<Descriptors.Descriptor> ignoredMessages;
    private final boolean isDeepEquals;

    /**
     * If true it will ignore if the expected has fields not set but they actually have values in actual.
     */
    private final boolean ignoreNonSetFields;
    private final boolean ignoreSetDefaultFields;
    private final boolean failAtFirstMisMatch;

    public ProtobufComparison(final List<Descriptors.FieldDescriptor> ignoredFields, final List<Descriptors.Descriptor> ignoredMessages,
            final boolean isDeepEquals, final boolean ignoreNonSetFields, final boolean ignoreSetDefaultFields, final boolean failAtFirstMisMatch) {
        this.isDeepEquals = isDeepEquals;
        this.ignoreNonSetFields = ignoreNonSetFields;
        this.ignoreSetDefaultFields = ignoreSetDefaultFields;
        this.failAtFirstMisMatch = failAtFirstMisMatch;
        this.ignoredFields = ignoredFields == null ? new ArrayList<Descriptors.FieldDescriptor>() : ignoredFields;
        this.ignoredMessages = ignoredMessages == null ? new ArrayList<Descriptors.Descriptor>() : ignoredMessages;
    }

    public void equals(final GeneratedMessage expected, final GeneratedMessage actual) {
        // TODO: instead of field descriptor use a field descriptor chain in case there are duplicate (or repeated) objects.
        final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields = new HashMap<>();
        equals(expected, actual, incorrectFields);
        if (!failAtFirstMisMatch && incorrectFields.size() > 0) {
            final StringBuilder message = new StringBuilder();
            message.append("There were " + incorrectFields.size() + " mismatches in this protobuf:");
            for (Descriptors.FieldDescriptor field : incorrectFields.keySet()) {
                final ExpectationPair<Object, Object> pair = incorrectFields.get(field);
                message.append("\n").append(createFailMessage(field, pair.getExpected(), pair.getActual()));
            }
            Assert.fail(message.toString());
        }
    }

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

    private void compareFields(final Object expectedValue, final Object actualValue, final Descriptors.FieldDescriptor field,
            final Map<Descriptors.FieldDescriptor, ExpectationPair<Object, Object>> incorrectFields) {
        if (expectedValue instanceof GeneratedMessage) {
            if (!(actualValue instanceof GeneratedMessage)) {
                Assert.fail("Different types");
            }
            if (isDeepEquals) {
                equals((GeneratedMessage) expectedValue, (GeneratedMessage) actualValue);
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
     * Creates a fail message.
     * @param field
     * @param expected If this value is null we assume that it was not set in the expected protobuf.
     * @param actual
     * @return
     */
    private String createFailMessage(final Descriptors.FieldDescriptor field, final Object expectedValue, final Object actualValue) {
        if (expectedValue == null) {
            return "Expected no value for field <" + field.getFullName() + "> but instead got value <" + actualValue + ">";
        }
        return "Expected <" + expectedValue + "> but got <" + actualValue + "> for field [" + field.getFullName() + "]";
    }
}
