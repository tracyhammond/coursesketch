package database.auth;

import database.RequestConverter;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthenticatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void returnsTrueWhenValidTimeGiven() {
        final DateTime t = DateTime.now();
        final DateTime open = t.minus(1000);
        final DateTime close = t.plus(1000);
        assertTrue(Authenticator.isTimeValid(t.getMillis(), RequestConverter.getProtoFromDate(open), RequestConverter.getProtoFromDate(close)));
        assertTrue(Authenticator.isTimeValid(t.getMillis(), open, close));
    }

    @Test
    public void returnsFalseWhenInvalidTimeGivenTimeIsBeforeOpen() {
        final DateTime t = DateTime.now();
        final DateTime open = t.minus(1000);
        final DateTime close = t.plus(1000);
        assertFalse(Authenticator.isTimeValid(t.getMillis(), RequestConverter.getProtoFromDate(close), RequestConverter.getProtoFromDate(close)));
        assertFalse(Authenticator.isTimeValid(t.getMillis(), close, close));
    }

    @Test
    public void returnsFalseWhenInvalidTimeGivenTimeIsAfterClsoe() {
        final DateTime t = DateTime.now();
        final DateTime open = t.minus(1000);
        final DateTime close = t.plus(1000);
        assertFalse(Authenticator.isTimeValid(t.getMillis(), RequestConverter.getProtoFromDate(open), RequestConverter.getProtoFromDate(open)));
        assertFalse(Authenticator.isTimeValid(t.getMillis(), open, open));
    }
}
