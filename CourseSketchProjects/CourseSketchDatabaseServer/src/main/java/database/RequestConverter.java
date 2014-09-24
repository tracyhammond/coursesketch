package database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import protobuf.srl.school.School.DateTime;
import protobuf.srl.school.School.SrlGroup;

public class RequestConverter {

    public static DateTime getProtoFromDate(final Calendar cal) {
        final DateTime.Builder result = DateTime.newBuilder();
        result.setMillisecond(cal.getTimeInMillis());
        final Date date = cal.getTime();
        result.setYear(date.getYear());
        result.setMonth(date.getMonth());
        result.setDay(date.getDay());
        result.setHour(date.getHours());
        result.setMinute(date.getMinutes());
        return result.build();
    }

    public static DateTime getProtoFromDate(final Date date) {
        final DateTime.Builder result = DateTime.newBuilder();
        result.setYear(date.getYear() + 1900);
        result.setMonth(date.getMonth());
        result.setDay(date.getDay());
        result.setHour(date.getHours());
        result.setMinute(date.getMinutes());
        return result.build();
    }

    /**
     * Creates a proto version of a date from the given milliseconds.
     *
     * @param date
     * @return
     */
    public static DateTime getProtoFromMilliseconds(final long date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        return getProtoFromDate(cal);
    }

    public static Date getDateFromProto(final DateTime date) {
        return new Date(date.getMillisecond());
    }

    private static ArrayList<String> GroupToString(final SrlGroup group) {
        final ArrayList<String> inputGroup = new ArrayList<String>();
        inputGroup.add(group.getGroupId());
        return inputGroup;
    }
}
