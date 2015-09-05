package database.auth;

import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.DateTime;
import utilities.ExceptionUtilities;

import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;

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
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Authenticator.class);

    /**
     * An implementation of the {@link AuthenticationDataCreator}.
     * Allows the data from authentication to come from multiple sources depending on the server.
     */
    private final AuthenticationChecker checker;
    private final AuthenticationOptionChecker optionChecker;

    /**
     * @param authenticationChecker Implements where the data actually comes from.
     */
    public Authenticator(final AuthenticationChecker authenticationChecker, final AuthenticationOptionChecker optionChecker) {
        checker = checkNotNull(authenticationChecker, "authenticationChecker");
        this.optionChecker = checkNotNull(optionChecker, "optionChecker");
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
                || authType.getCheckDate() || authType.getCheckingPeerTeacher() || authType.getCheckAccess();
    }

    /**
     * @return True if one of the values in AuthType is true. (Excluded is dateChecking)
     */
    public static boolean validAccessRequest(Authentication.AuthType authType) {
        return authType.getCheckingUser() || authType.getCheckingMod() || authType.getCheckingAdmin()
                || authType.getCheckingPeerTeacher() || authType.getCheckAccess();
    }

    /**
     *
     * @param collectionType
     * @param itemId
     * @param userId
     * @param checkTime
     * @param checkType
     * @return
     * @throws AuthenticationException
     */
    public AuthenticationResponder checkAuthentication(final School.ItemType collectionType, final String itemId,
            final String userId, final long checkTime, final Authentication.AuthType checkType) throws AuthenticationException {
        checkNotNull(collectionType, "collectionType");
        checkNotNull(itemId, "itemId");
        checkNotNull(userId, "userId");
        checkNotNull(checkType, "checkType");

        if (!validRequest(checkType)) {
            throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
        }

        final Authentication.AuthResponse.Builder authBuilder = Authentication.AuthResponse.newBuilder();
        final CountDownLatch totalLatch = new CountDownLatch(2);
        final CountDownLatch checkLatch = new CountDownLatch(1);

        final ExceptionUtilities.ExceptionHolder checkerException = ExceptionUtilities.getExceptionHolder();
        // Auth checking checking
        if (validAccessRequest(checkType)) {
            new Thread() {
                public void run() {
                    try {
                        Authentication.AuthResponse result = checker.isAuthenticated(collectionType, itemId, userId, checkType);
                        synchronized (authBuilder) {
                            authBuilder.mergeFrom(result);
                        }
                    } catch (DatabaseAccessException e) {
                        checkerException.exception = e;
                        LOG.error("Exception was thrown while accessing database", e);
                    } catch (AuthenticationException e) {
                        checkerException.exception = e;
                        LOG.error("Exception was thrown while authenticating person", e);
                    }
                    checkLatch.countDown();
                    totalLatch.countDown();
                }
            }.start();
        } else {
            checkLatch.countDown();
            totalLatch.countDown();
        }

        final ExceptionUtilities.ExceptionHolder optionCheckerException = ExceptionUtilities.getExceptionHolder();
        // Date checking
        if (checkType.getCheckDate() || checkType.getCheckAccess()) {
            new Thread() {
                public void run() {
                    boolean validDate = false;
                    try {
                        final AuthenticationDataCreator dataCreator = optionChecker.createDataGrabber(collectionType, itemId);
                        if (checkType.getCheckDate()) {
                            validDate = optionChecker.authenticateDate(dataCreator, checkTime);
                        }
                        boolean registrationRequired = true;
                        boolean itemPublished = false;
                        if (checkType.getCheckAccess()) {
                            registrationRequired = optionChecker.isItemRegistrationRequired(dataCreator);
                            itemPublished = optionChecker.isItemPublished(dataCreator);
                        }
                        try {
                            checkLatch.await();
                        } catch (InterruptedException e) {
                            LOG.error("Await was interrupted while authenticating date", e);
                        }
                        if (checkType.getCheckDate()) {
                            authBuilder.setIsItemOpen(validDate);
                        }
                        if (checkType.getCheckAccess()) {
                            authBuilder.setIsRegistrationRequired(registrationRequired);
                            authBuilder.setIsItemPublished(itemPublished);
                        }
                    } catch (DatabaseAccessException e) {
                        optionCheckerException.exception = e;
                        LOG.error("Exception was thrown while accessing database", e);
                    }
                    totalLatch.countDown();
                }
            }.start();
        } else {
            // this is one of the two
            totalLatch.countDown();
        }

        try {
            totalLatch.await();
        } catch (InterruptedException e) {
            throw new AuthenticationException(e);
        }

        if (checkType.getCheckingAdmin() || checkType.getCheckingMod() || checkType.getCheckingPeerTeacher() || checkType.getCheckingUser()
                || checkType.getCheckAccess()) {
            authBuilder.setHasAccess(authBuilder.getHasAccess() ||
                    authBuilder.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE);
        }

        // hasAccess will be true if they have a permission level that is not above
        if (checkType.getCheckAccess()) {
            authBuilder.setHasAccess(authBuilder.getHasAccess()
                    || (!authBuilder.getIsRegistrationRequired()));
        }

        if (checkerException.exception != null) {
            throw new AuthenticationException(checkerException.exception);
        }
        if (optionCheckerException.exception != null) {
            throw new AuthenticationException(optionCheckerException.exception);
        }

        return new AuthenticationResponder(authBuilder.build());
    }
}
