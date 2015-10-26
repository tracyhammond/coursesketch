package coursesketch.database.auth;

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

        // Auth checking checking
        final AuthenticationThreadData authThreadData = new AuthenticationThreadData(authBuilder, checkType, ExceptionUtilities.getExceptionHolder(),
                totalLatch, checkLatch);
        checkAuth(collectionType, itemId, userId, authThreadData);

        // Date checking
        final AuthenticationThreadData optionThreadData = new AuthenticationThreadData(authBuilder, checkType,
                ExceptionUtilities.getExceptionHolder(), totalLatch, checkLatch);
        checkOption(collectionType, itemId, checkTime, optionThreadData);

        authThreadData.awaitMainLatch();

        if (validUserAccessRequest(checkType)) {
            authBuilder.setHasAccess(authBuilder.getHasAccess()
                    || authBuilder.getPermissionLevel().getNumber() >= Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE);
        }

        // hasAccess will be true if they have a permission level that is not above
        if (checkType.getCheckAccess()) {
            authBuilder.setHasAccess(authBuilder.getHasAccess() || !authBuilder.getIsRegistrationRequired());
        }

        authThreadData.checkException();
        optionThreadData.checkException();

        return new AuthenticationResponder(authBuilder.build());
    }

    /**
     * Checks if the user has access to the item.
     * @param collectionType The type of item that is being checked.
     * @param itemId The id of the item being checked.
     * @param userId The user trying to authenticate.
     * @param threadData Object that contains information useful to all threads.
     */
    private void checkAuth(final School.ItemType collectionType, final String itemId, final String userId,
            final AuthenticationThreadData threadData) {
        final Authentication.AuthType checkType = threadData.authType;
        if (validUserAccessRequest(checkType)) {
            EXECUTOR_SERVICE.execute(new Runnable() {
                @Override
                @SuppressWarnings("PMD.CommentRequired")
                public void run() {
                    try {
                        final Authentication.AuthResponse result = checker.isAuthenticated(collectionType, itemId, userId, checkType);
                        synchronized (threadData.authBuilder) {
                            threadData.authBuilder.mergeFrom(result);
                        }
                    } catch (DatabaseAccessException e) {
                        threadData.exceptionHolder.exception = e;
                        LOG.error("Exception was thrown while accessing database", e);
                    } catch (AuthenticationException e) {
                        threadData.exceptionHolder.exception = e;
                        LOG.error("Exception was thrown while authenticating person", e);
                    }
                    threadData.secondaryLatch.countDown();
                    threadData.mainLatch.countDown();
                }
            });
        } else {
            threadData.secondaryLatch.countDown();
            threadData.mainLatch.countDown();
        }
    }

    /**
     * Checks for miscellaneous data that is useful for authentication.
     * @param collectionType The type of item that is being checked.
     * @param itemId The id of the item being checked.
     * @param checkTime The time that the item is being accessed.  (Or perceived to be accessed at)
     * @param threadData Object that contains information useful to all threads.
     */
    private void checkOption(final School.ItemType collectionType, final String itemId, final long checkTime,
            final AuthenticationThreadData threadData) {
        final Authentication.AuthType checkType = threadData.authType;
        if (checkType.getCheckDate() || checkType.getCheckAccess() || checkType.getCheckIsPublished() || checkType.getCheckIsRegistrationRequired()) {
            EXECUTOR_SERVICE.execute(new Runnable() {
                @SuppressWarnings("PMD.CommentRequired")
                public void run() {
                    try {
                        final Authentication.AuthResponse.Builder result = Authentication.AuthResponse.newBuilder();
                        setOptionAuthData(collectionType, itemId, checkTime, result, threadData.authType);
                        threadData.awaitSecondaryLatch();
                        synchronized (threadData.authBuilder) {
                            threadData.authBuilder.mergeFrom(result.build());
                        }
                        // set auth data.
                    } catch (DatabaseAccessException | AuthenticationException e) {
                        threadData.exceptionHolder.exception = e;
                        LOG.error("Exception was thrown while accessing database", e);
                    }
                    threadData.mainLatch.countDown();
                }


            });
        } else {
            // this is one of the two
            threadData.mainLatch.countDown();
        }
    }

    /**
     * Sets the data received by the option authenticator.
     * @param collectionType The type of item it is. Ex: A course or an assignment
     * @param itemId The id for the item that the authentication is being checked.
     * @param checkTime The time that the user wants access to the item.
     * @param authBuilder Contains the respond.
     * @param checkType Contains what is being checked.
     * @throws DatabaseAccessException Thrown if there are problems reading the data from the database.
     */
    private void setOptionAuthData(final School.ItemType collectionType, final String itemId, final long checkTime,
            final Authentication.AuthResponse.Builder authBuilder, final Authentication.AuthType checkType) throws DatabaseAccessException {
        final AuthenticationDataCreator dataCreator = optionChecker.createDataGrabber(collectionType, itemId);
        if (checkType.getCheckDate()) {
            authBuilder.setIsItemOpen(optionChecker.authenticateDate(dataCreator, checkTime));
        }
        if ((checkType.getCheckAccess() || checkType.getCheckIsRegistrationRequired())
                && (School.ItemType.COURSE == collectionType || School.ItemType.BANK_PROBLEM == collectionType)) {
            authBuilder.setIsRegistrationRequired(optionChecker.isItemRegistrationRequired(dataCreator));
        }
        // Course Problems can not be published only assignments!
        if (checkType.getCheckIsPublished() && School.ItemType.COURSE_PROBLEM != collectionType) {
            authBuilder.setIsItemPublished(optionChecker.isItemPublished(dataCreator));
        }
    }

    /**
     * Holds data related to managing threads.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private static class AuthenticationThreadData {
        /**
         * The latch that locks all threads together.
         */
        private final CountDownLatch mainLatch;

        /**
         * Latch used to ensure order of the sub threads.
         */
        private final CountDownLatch secondaryLatch;

        /**
         * The type of checks that are wanted to be returned.
         */
        private final Authentication.AuthType authType;

        /**
         * The result being built by the different threads.
         */
        private final Authentication.AuthResponse.Builder authBuilder;

        /**
         * Holds any exceptions that are thrown.
         */
        private final ExceptionUtilities.ExceptionHolder exceptionHolder;

        /**
         * Constructor for the thread data.
         *
         * @param authBuilder
         *         {@link #authBuilder}
         * @param authType
         *         {@link #authType}
         * @param exceptionHolder
         *         {@link #exceptionHolder}
         * @param mainLatch
         *         {@link #mainLatch}
         * @param secondaryLatch
         *         {@link #secondaryLatch}
         */
        AuthenticationThreadData(final Authentication.AuthResponse.Builder authBuilder,
                final Authentication.AuthType authType, final ExceptionUtilities.ExceptionHolder exceptionHolder, final CountDownLatch mainLatch,
                final CountDownLatch secondaryLatch) {
            this.authBuilder = authBuilder;
            this.authType = authType;
            this.exceptionHolder = exceptionHolder;
            this.mainLatch = mainLatch;
            this.secondaryLatch = secondaryLatch;
        }

        /**
         * Awaits for the secondary latch (preventing the need for the exception).
         *
         * @throws AuthenticationException
         *         thrown if there could be potential synchronization problems.
         */
        public final void awaitSecondaryLatch() throws AuthenticationException {
            try {
                secondaryLatch.await();
            } catch (InterruptedException e) {
                LOG.error("Await was interrupted while authenticating date", e);
                throw new AuthenticationException(e);
            }
        }

        /**
         * Awaits for the secondary latch (preventing the need for the exception).
         *
         * @throws AuthenticationException
         *         thrown if there could be potential synchronization problems.
         */
        public final void awaitMainLatch() throws AuthenticationException {
            try {
                mainLatch.await();
            } catch (InterruptedException e) {
                LOG.error("Await was interrupted while authenticating date", e);
                throw new AuthenticationException(e);

            }
        }

        /**
         * Checks to see if there is an exception throws if there is.
         * @throws AuthenticationException Thrown if an exception exist in the exception holder.
         */
        public void checkException() throws AuthenticationException {
            if (exceptionHolder.exception != null) {
                throw new AuthenticationException(exceptionHolder.exception);
            }
        }
    }
}
