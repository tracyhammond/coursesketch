package database.auth;

import database.DatabaseAccessException;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.DateTime;

import java.util.concurrent.CountDownLatch;

/**
 * A class that performs authentication.
 *
 * This is abstract as the actual location where the data is grabbed is up to
 * implementation.
 *
 * @author gigemjt
 */
public final class Authenticator {

    /**
     * An implementation of the {@link AuthenticationDataCreator}.
     * Allows the data from authentication to come from multiple sources depending on the server.
     */
    private final AuthenticationChecker checker;
    private final AuthenticationDateChecker dateChecker;

    /**
     * @param authenticationChecker Implements where the data actually comes from.
     */
    public Authenticator(final AuthenticationChecker authenticationChecker, final AuthenticationDateChecker dateChecker) {
        if (authenticationChecker == null) {
            throw new IllegalArgumentException("The AuthenticationChecker can not be null.");
        }
        if (dateChecker == null) {
            throw new IllegalArgumentException("The AuthenticationDateChecker can not be null.");
        }
        checker = authenticationChecker;
        this.dateChecker = dateChecker;
    }

    /**
     * @param time The input time that is being checked.
     * @param openDate the input time has to be larger or equal to this date.
     * @param closeDate the input time has to be smaller or equal to this date.
     * @return True if the input time is between openDate and CloseDate.
     */
    public static boolean isTimeValid(final long time, final DateTime openDate, final DateTime closeDate) {
        return time >= openDate.getMillisecond() && time <= closeDate.getMillisecond();
    }

    /**
     * @param time The input time that is being checked.
     * @param openDate the input time has to be larger or equal to this date.
     * @param closeDate the input time has to be smaller or equal to this date.
     * @return True if the input time is between openDate and CloseDate.
     */
    public static boolean isTimeValid(final long time, final org.joda.time.DateTime openDate, final org.joda.time.DateTime closeDate) {
        return time >= openDate.getMillis() && time <= closeDate.getMillis();
    }

    /**
     * @return True if one of the values in AuthType is true.
     */
    public static boolean validRequest(Authentication.AuthType authType) {
        return authType.getCheckingUser() || authType.getCheckingMod() || authType.getCheckingAdmin()
                || authType.getCheckDate() || authType.getCheckAdminOrMod() || authType.getCheckAccess();
    }

    public AuthenticationResponder checkAuthentication(final School.ItemType collectionType, final String itemId,
            final String userId, final long checkTime, final Authentication.AuthType checkType) {
    }
}
