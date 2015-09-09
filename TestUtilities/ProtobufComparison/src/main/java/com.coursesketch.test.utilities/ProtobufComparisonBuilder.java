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
    private boolean isDeepEquals = true;
    private boolean ignoreNonSetFields = false;
    /**
     * Basically this ignores fields that are set on the actual but not the expected if they are equal to the default value
     */
    private boolean ignoreSetDefaultFields = true;
    private boolean failAtFirstMisMatch = true;

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
        return new ProtobufComparison(ignoredFields, ignoredMessages, isDeepEquals, ignoreNonSetFields, ignoreSetDefaultFields, failAtFirstMisMatch);
    }

    public ProtobufComparisonBuilder setIsDeepEquals(final boolean isDeepEquals) {
        this.isDeepEquals = isDeepEquals;
        return this;
    }

    /**
     * Fails at the first mismatch of fields if true.
     *
     * NOTE: if the values sent in are different message types that will still fail immediately even if this is false.
     * @param failAtFirstMisMatch Default is true.
     * @return
     */
    public ProtobufComparisonBuilder setFailAtFirstMisMatch(final boolean failAtFirstMisMatch) {
        this.failAtFirstMisMatch = failAtFirstMisMatch;
        return this;
    }
}
