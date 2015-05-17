package database;

import static org.junit.Assert.*;

import org.junit.Test;

import protobuf.srl.school.School.DateTime;

public class RequestConverterTest {

    @Test
    public void testProtoCreationFromMillis() {

        final long millis = System.currentTimeMillis();
        final org.joda.time.DateTime t = new org.joda.time.DateTime(millis);
        final DateTime protoDate = RequestConverter.getProtoFromMilliseconds(millis);

        assertEquals(protoDate.getMillisecond(), millis);
        assertEquals(protoDate.getYear(), t.getYear());
        assertEquals(protoDate.getMonth(), t.getMonthOfYear());
        assertEquals(protoDate.getDay(), t.getDayOfMonth());
        assertEquals(protoDate.getHour(), t.getHourOfDay());
        assertEquals(protoDate.getMinute(), t.getMinuteOfHour());
    }

    @Test
    public void testProtoConverstion() {

        final long millis = System.currentTimeMillis();
        final org.joda.time.DateTime t = new org.joda.time.DateTime(millis);
        final DateTime protoDate = RequestConverter.getProtoFromMilliseconds(millis);

        final org.joda.time.DateTime t2 = RequestConverter.getDateFromProto(protoDate);
        assertEquals(t, t2);
    }
}
