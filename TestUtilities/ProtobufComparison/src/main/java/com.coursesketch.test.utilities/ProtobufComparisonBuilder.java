package com.coursesketch.test.utilities;

import com.google.protobuf.Descriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparisonBuilder {
    private final List<Descriptors.FieldDescriptor> ignoredFields;
    private final List<Descriptors.Descriptor> ignoredMessages;

    public ProtobufComparisonBuilder() {
        ignoredFields = new ArrayList<>();
        ignoredMessages = new ArrayList<>();
    }

    /**
     * Ignores the field when comparing protobufs
     * @param ignoreField
     */
    public ProtobufComparisonBuilder ignoreField(Descriptors.FieldDescriptor ignoreField) {
        ignoredFields.add(ignoreField);
        return this;
    }

    /**
     * Ignores the message when comparing protobufs
     * @param ignoreMessage
     */
    public ProtobufComparisonBuilder ignoreMessage(Descriptors.Descriptor ignoreMessage) {
        ignoredMessages.add(ignoreMessage);
        return this;
    }

    public ProtobufComparison build() {
        return new ProtobufComparison(ignoredFields, ignoredMessages);
    }
}
