package database.auth;

import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util.DateTime;
import utilities.ExceptionUtilities;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
     * Manages the execution of threads.
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(100);

    /**
     * An implementation of the {@link AuthenticationDataCreator}.
     * Allows the data from authentication to come from multiple sources depending on the server.
     */
    private final AuthenticationChecker checker;

    /**
     * Checks for other details relevent to authentication that do not require a userId to check.
     */
    private final AuthenticationOptionChecker optionChecker;

    /**
     * @param authenticationChecker Checks if the user is in the system with their authenticationId.
     * @param optionChecker Checks data that does not require a userId.  Instead it grabs data about the course.
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
     * @param authType The parameters used for an authentication check.
     * @return True if one of the values in AuthType is true.
     */
    public static boolean validRequest(final Authentication.AuthType authType) {
        return authType.getCheckingUser() || authType.getCheckingMod() || authType.getCheckingAdmin()
                || authType.getCheckDate() || authType.getCheckingPeerTeacher() || authType.getCheckAccess()
                || authType.getCheckIsPublished() || authType.getCheckIsRegistrationRequired();
    }

    /**
     * @param authType The parameters used for an authentication check.
     * @return True if one of the values in AuthType is true.
     *          Excluded is anything that the {@link AuthenticationOptionChecker} looks at.
     */
    public static boolean validUserAccessRequest(final Authentication.AuthType authType) {
        return authType.getCheckingUser() || authType.getCheckingMod() || authType.getCheckingAdmin()
                || authType.getCheckingPeerTeacher() || authType.getCheckAccess();
    }

    /**
     * Creates an {@link AuthenticationResponder} when checking authentication.
     * @param collectionType The type of item it is. Ex: A course or an assignment
     * @param itemId The id for the item that the authentication is being checked.
     * @param userId The user who wants to authenticate.
     * @param checkTime The time that the user wants access to the item.
     * @param checkType The type of checks that are wanted to be returned.
     * @return An {@link AuthenticationResponder} that contains the information about the authentication response.
     * @throws AuthenticationException Thrown if there are problems creating the {@link AuthenticationResponder}.
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
        if (validUserAccessRequest(checkType)) {
            EXECUTOR_SERVICE.execute(new Runnable() {
                public void run() {
                    try {
                        final Authentication.AuthResponse result = checker.isAuthenticated(collectionType, itemId, userId, checkType);
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
            });
        } else {
            checkLatch.countDown();
            totalLatch.countDown();
        }

        final ExceptionUtilities.ExceptionHolder optionCheckerException = ExceptionUtilities.getExceptionHolder();
        // Date checking
        if (checkType.getCheckDate() || checkType.getCheckAccess() || checkType.getCheckIsPublished() || checkType.getCheckIsRegistrationRequired()) {
            EXECUTOR_SERVICE.execute(new Runnable() {
                public void run() {
                    boolean validDate = false;
                    try {
                        final AuthenticationDataCreator dataCreator = optionChecker.createDataGrabber(collectionType, itemId);
                        if (checkType.getCheckDate()) {
                            validDate = optionChecker.authenticateDate(dataCreator, checkTime);
                        }
                        boolean registrationRequired = true;
                        boolean itemPublished = false;
                        if (checkType.getCheckAccess() || checkType.getCheckIsRegistrationRequired()) {
                            registrationRequired = optionChecker.isItemRegistrationRequired(dataCreator);
                        }
                        if (checkType.getCheckIsPublished()) {
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
                        if (checkType.getCheckAccess() || checkType.getCheckIsRegistrationRequired()) {
                            authBuilder.setIsRegistrationRequired(registrationRequired);
                        }
                        if (checkType.getCheckIsPublished()) {
                            authBuilder.setIsItemPublished(itemPublished);
                        }
                    } catch (DatabaseAccessException e) {
                        optionCheckerException.exception = e;
                        LOG.error("Exception was thrown while accessing database", e);
                    }
                    totalLatch.countDown();
                }
            });
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
            authBuilder.setHasAccess(authBuilder.getHasAccess()
                    || authBuilder.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE);
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
