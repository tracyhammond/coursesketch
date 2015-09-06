package com.coursesketch.test.utilities;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

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

    public ProtobufComparison(final List<Descriptors.FieldDescriptor> ignoredFields, final List<Descriptors.Descriptor> ignoredMessages,
            final boolean isDeepEquals, final boolean ignoreNonSetFields) {
        this.isDeepEquals = isDeepEquals;
        this.ignoreNonSetFields = ignoreNonSetFields;
        this.ignoredFields = ignoredFields == null ? new ArrayList<Descriptors.FieldDescriptor>() : ignoredFields;
        this.ignoredMessages = ignoredMessages == null ? new ArrayList<Descriptors.Descriptor>() : ignoredMessages;
    }


    public void equals(GeneratedMessage expected, GeneratedMessage actual) {
        if (!expected.getDescriptorForType().equals(actual.getDescriptorForType())) {
            Assert.fail("Expected Message " + expected.getDescriptorForType().getFullName()
                    + " but got Message " + actual.getDescriptorForType().getFullName());
        }

        final List<Descriptors.FieldDescriptor> expectedAllFields = expected.getDescriptorForType().getFields();


        List<Descriptors.FieldDescriptor> incorrectFields = new ArrayList<>();

        for (Descriptors.FieldDescriptor field : expectedAllFields) {
            if (ignoredFields.contains(field)) {
                continue;
            }
            if (!expected.hasField(field)) {
                if (!ignoreNonSetFields && actual.hasField(field)) {
                    Assert.fail("Expected no value for field [" + field.getFullName() + "] but instead got value [" + actual.getField(field) + "]");
                }
                continue;
            }
            Object expectedValue = expected.getField(field);
            Object actualValue = actual.getField(field);
            if (expectedValue instanceof GeneratedMessage) {
                if (!(actualValue instanceof GeneratedMessage)) {
                    Assert.fail("Different types");
                }
                if (isDeepEquals) {
                    equals((GeneratedMessage) expectedValue, (GeneratedMessage) actualValue);
                }
                continue;
            }
            if (!expectedValue.equals(actualValue)) {
                Assert.fail("Expected [" + expectedValue + "] but got [" + actualValue + "] for field [" + field.getFullName() + "]");
            }
        }
    }
}
