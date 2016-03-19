package com.coursesketch.test.utilities;

import com.google.protobuf.Descriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates an {@link ProtobufComparison}.
 *
 * The {@link ProtobufComparison} that is returned will throw an exception if the protobuf objects are not equal.
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
     * If true then the list order is ignored when comparing protobuf objects otherwise the list is not ignored when comparing protobuf objects.
     */
    private boolean ignoreListOrder;

    /**
     * Creates a new instance of the ProtobufComparisonBuilder.
     */
    public ProtobufComparisonBuilder() {
        ignoredFields = new ArrayList<>();
        ignoredMessages = new ArrayList<>();
    }

    /**
     * Ignores the field when comparing protobufs.
     *
     * @param ignoreField A field that will be ignored by the Comparison.
     * @return Itself.
     */
    public final ProtobufComparisonBuilder ignoreField(final Descriptors.FieldDescriptor ignoreField) {
        ignoredFields.add(ignoreField);
        return this;
    }

    /**
     * Ignores the field when comparing protobufs.
     *
     * @param descriptor A descriptor for the message that contains the field that is being ignored.
     * @param fieldNumberToIgnore The field number that should be ignored by the given descriptor.
     * @return Itself.
     */
    public final ProtobufComparisonBuilder ignoreField(final Descriptors.Descriptor descriptor, final int fieldNumberToIgnore) {
        ignoredFields.add(descriptor.findFieldByNumber(fieldNumberToIgnore));
        return this;
    }

    /**
     * Ignores the message when comparing protobufs.
     *
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
        return new ProtobufComparison(ignoredFields, ignoredMessages, isDeepEquals, ignoreNonSetFields, ignoreSetDefaultFields, failAtFirstMisMatch,
                ignoreListOrder);
    }

    /**
     * Sets if a deep comparison should happen.
     *
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
     * If this value is true, the comparison will also accept the default value for a field when it is expecting a blank value.
     * If this value is false, it will throw an exception if the expected blank fields are not blank
     *
     * <pre>
     * EX:
     * Expected protobuf does not have field A (which is a string) set.
     * Actual protobuf does have field A set, but it is set to the default value.
     * This will not throw an assertion error.
     * </pre>
     * @param ignoreSetDefaultFields False to throw an assertion error if the expected field has no value, but the actual field has a default value.
     *                               The param is true by default (and thus accepts the default field value for a blank expected value).
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setIgnoreSetDefaultFields(final boolean ignoreSetDefaultFields) {
        this.ignoreSetDefaultFields = ignoreSetDefaultFields;
        return this;
    }

    /**
     * If true, this will ignore fields where the expected protobuf has no value even if the actual protobuf has a value.
     * If false, this will throw an assertion error if the expected protobuf has no value for a field
     * and the actual protobuf has a value for that field.
     *
     * Basically, if true it only compares fields that are set in the expected protobuf.
     * @param ignoreNonSetFields True to ignore any field that is not set on the expected protobuf.  This is false by default.
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setIgnoreNonSetFields(final boolean ignoreNonSetFields) {
        this.ignoreNonSetFields = ignoreNonSetFields;
        return this;
    }

    /**
     * Sets if lists should ignore order.
     *
     * This value is false by default.
     * @param ignoreListOrder True if lists comparison should ignore order false otherwise.
     * @return Itself.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public final ProtobufComparisonBuilder setIgnoreListOrder(final boolean ignoreListOrder) {
        this.ignoreListOrder = ignoreListOrder;
        return this;
    }
}
