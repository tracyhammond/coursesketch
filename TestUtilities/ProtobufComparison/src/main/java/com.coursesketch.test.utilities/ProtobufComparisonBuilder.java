package com.coursesketch.test.utilities;

import com.google.protobuf.Descriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigemjt on 9/6/15.
 */
public class ProtobufComparisonBuilder {

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
    private boolean isDeepEquals = true;

    /**
     * If true it will ignore if the expected has fields not set but they actually have values in actual.
     */
    private boolean ignoreNonSetFields = false;

    /**
     * if True then it will ignore cases where a field is set but it is set to the default value of the protobuf.
     * Basically this ignores fields that are set on the actual but not the expected if they are equal to the default value.
     */
    private boolean ignoreSetDefaultFields = true;

    /**
     * If true then the first fail will be asserted instead of all fails.
     */
    private boolean failAtFirstMisMatch = true;

    /**
     * Creates a new instance of the ProtobufComparisonBuilder.
     */
    public ProtobufComparisonBuilder() {
        ignoredFields = new ArrayList<>();
        ignoredMessages = new ArrayList<>();
    }

    /**
     * Ignores the field when comparing protobufs.
     * @param ignoreField A field that will be ignored by the Comparison.
     * @return Itself.
     */
    public final ProtobufComparisonBuilder ignoreField(final Descriptors.FieldDescriptor ignoreField) {
        ignoredFields.add(ignoreField);
        return this;
    }

    /**
     * Ignores the message when comparing protobufs.
     * @param ignoreMessage The message that will be ignored by the Comparison.
     * @return Itself.
     */
    public final ProtobufComparisonBuilder ignoreMessage(final Descriptors.Descriptor ignoreMessage) {
        ignoredMessages.add(ignoreMessage);
        return this;
    }

    /**
     * @return A built ProtobufComparison Object.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public ProtobufComparison build() {
        return new ProtobufComparison(ignoredFields, ignoredMessages, isDeepEquals, ignoreNonSetFields, ignoreSetDefaultFields, failAtFirstMisMatch);
    }

    /**
     * Sets if a deep comparison should happen.
     * @param isDeepEquals true if a deep equals comparison should happen. False otherwise.
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setIsDeepEquals(final boolean isDeepEquals) {
        this.isDeepEquals = isDeepEquals;
        return this;
    }

    /**
     * Fails at the first mismatch of fields if true.
     *
     * NOTE: if the values sent in are different message types that will still fail immediately even if this is false.
     * @param failAtFirstMisMatch Default is true.
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setFailAtFirstMisMatch(final boolean failAtFirstMisMatch) {
        this.failAtFirstMisMatch = failAtFirstMisMatch;
        return this;
    }

    /**
     * If true it will ignore if the expected has fields not set but the actual protobuf has the fields set but they are set to the same values
     * as the default protobuf.
     * <p/>
     * <pre>
     * EX:
     * expected protobuf does not have field A which is a string set.
     * actual protobuf does have the field set but it is set to what the message returns for a default value.
     * This will not through an assertion error.
     * </pre>
     * @param ignoreSetDefaultFields false to throw an assertion if the expected has no value but the actual does.  This is true by default.
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setIgnoreSetDefaultFields(final boolean ignoreSetDefaultFields) {
        this.ignoreSetDefaultFields = ignoreSetDefaultFields;
        return this;
    }

    /**
     * If true this will ignore any field where the expected has no value but the actual protobuf does have a value.
     *
     * <p/>
     * (Basically it ignores any field not set on the expected protobuf)
     *
     * If this is false it will consider this a mismatch and throw an assertion error.
     * @param ignoreNonSetFields True to ignore any field that is not set on the expected protobuf.  This is false by default.
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setIgnoreNonSetFields(final boolean ignoreNonSetFields) {
        this.ignoreNonSetFields = ignoreNonSetFields;
        return this;
    }
}
