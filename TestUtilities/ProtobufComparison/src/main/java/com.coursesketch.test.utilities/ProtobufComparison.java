package com.coursesketch.test.utilities;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparison {
    private final List<Descriptors.FieldDescriptor> ignoredFields;
    private final List<Descriptors.Descriptor> ignoredMessages;

    public ProtobufComparison(final List<Descriptors.FieldDescriptor> ignoredFields, final List<Descriptors.Descriptor> ignoredMessages) {
        this.ignoredFields = ignoredFields;
        this.ignoredMessages = ignoredMessages;
    }

    public void equals(GeneratedMessage expected, GeneratedMessage actual) {
        if (!expected.getDescriptorForType().equals(actual.getDescriptorForType())) {
            Assert.fail("Exepcted Message " + expected.getDescriptorForType().getFullName()
                    + " but got Message " + actual.getDescriptorForType().getFullName());
        }
        final Map<Descriptors.FieldDescriptor, Object> expectedAllFields = expected.getAllFields();
        final Map<Descriptors.FieldDescriptor, Object> actualAllFields = actual.getAllFields();

        List<Descriptors.FieldDescriptor> incorrectFields = new ArrayList<>();
    }
}
