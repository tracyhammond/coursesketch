package database;

import protobuf.srl.school.School.DateTime;

/**
 * Converts a request from different values to the protobuf version.
 * @author gigemjt
 *
 */
public class RequestConverter {

    /**
     * Creates a proto version of a date from the given milliseconds.
     *
     * @param date The date in milliseconds.
     * @return {@link DateTime} contains all values needed.
     */
    public static DateTime getProtoFromMilliseconds(final long date) {
        final org.joda.time.DateTime jodaDate = new org.joda.time.DateTime(date);
        return getProtoFromDate(jodaDate);
    }

    /**
     * Creates a protobuf {@link DateTime} from the {@link org.joda.time.DateTime}.
     * @param jodaDate the input date and time.
     * @return {@link DateTime}
     */
    public static DateTime getProtoFromDate(final org.joda.time.DateTime jodaDate) {
        final DateTime.Builder result = DateTime.newBuilder();
        result.setYear(jodaDate.getYear());
        result.setMonth(jodaDate.getMonthOfYear());
        result.setDay(jodaDate.getDayOfMonth());
        result.setHour(jodaDate.getHourOfDay());
        result.setMinute(jodaDate.getMinuteOfHour());
        result.setMillisecond(jodaDate.getMillis());
        return result.build();
    }

    /**
     * Converts the protobuf {@link DateTime}  to a joda {@link org.joda.time.DateTime}.
     * @param date The protobuf date time object
     * @return {@link org.joda.time.DateTime}.
     */
    public static org.joda.time.DateTime getDateFromProto(final DateTime date) {
        return new org.joda.time.DateTime(date.getMillisecond());
    }

    /*
    private static ArrayList<String> GroupToString(final SrlGroup group) {
        final ArrayList<String> inputGroup = new ArrayList<String>();
        inputGroup.add(group.getGroupId());
        return inputGroup;
    }
    */
}
