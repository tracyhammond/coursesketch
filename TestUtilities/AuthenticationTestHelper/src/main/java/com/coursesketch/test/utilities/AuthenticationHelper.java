package com.coursesketch.test.utilities;

import database.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helps with Authentication Testing.
 *
 * Currently sets mock values for the {@link AuthenticationChecker} and the {@link AuthenticationOptionChecker}.
 * This makes it easier to set permissions or other authentication states.
 * Created by gigemjt on 9/5/15.
 */
public class AuthenticationHelper {

    /**
     * If element is not null it will create an eq matcher. If element is not null then it will create an any matcher.
     *
     * @param element The object that a matcher is being created for.
     * @param tClass The class that represents the object.
     * @param <T> The object type
     * @return A mockito matcher that is created for the given element.
     */
    public static <T> T createAnyEqMatcher(final T element, final Class<T> tClass) {
        if (element == null && tClass == null) {
            return eq(null);
        } else if (element == null) {
            return any(tClass);
        } else {
            return eq(element);
        }
    }

    /**
     * Sets permissions for the given authentication checker.
     *
     * @param authChecker The authentication checker that the permissions are being created for.
     * @param type The {@code ItemType} which will be matched for this permission. Send in null to apply this to any {@code ItemType}.
     * @param itemId The {@code itemId} that will be matched for this permission. Send in null to apply this to any (@code itemId}.
     * @param userId The {@code userId} that will be matched for this permission. Send in null to apply this to any (@code userId}.
     * @param authType The {@code AuthType} that will be matched for this permission. Send in null to apply this to any (@code AuthType}.
     * @param permissionLevel The resulting permission level that will be returned if the {@code authChecker} is given the above matches.
     * @throws DatabaseAccessException Should not be thrown because authChecker should be a mock.
     * @throws AuthenticationException Should not be thrown because authChecker should be a mock.
     */
    public static void setMockPermissions(final AuthenticationChecker authChecker, final School.ItemType type,
            final String itemId, final String userId,
            final Authentication.AuthType authType, final Authentication.AuthResponse.PermissionLevel permissionLevel)
            throws DatabaseAccessException, AuthenticationException {
        // specific results
        when(authChecker.isAuthenticated(
                createAnyEqMatcher(type, School.ItemType.class),
                createAnyEqMatcher(itemId, String.class),
                createAnyEqMatcher(userId, String.class),
                createAnyEqMatcher(authType, Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(permissionLevel)
                        .build());
    }

    /**
     * Sets mock date for the given {@link AuthenticationOptionChecker}.
     *
     * @param option The {@link AuthenticationOptionChecker} that the date is being created for.
     * @param creator The {@link AuthenticationDataCreator} that will be matched for this date.
     *                Send in null to apply this to a new {@link AuthenticationDataCreator}.
     * @param type The {@code ItemType} which will be matched for this date. Send in null to apply this to any {@code ItemType}.
     * @param itemId The {@code itemId} that will be matched for this date. Send in null to apply this to any (@code itemId}.
     * @param checkTime The {@code checkTime} which will be matched for this date.
     * @param isOpen The resulting mock date that will be returned if the {@code authChecker} is given the above matches.
     * @throws DatabaseAccessException Should not be thrown because authChecker should be a mock.
     * @return {@link AuthenticationDataCreator}, A new instance if {@code creator} is null otherwise it is the value of {@code creator}.
     * @see #setMockPublished(AuthenticationOptionChecker, AuthenticationDataCreator, School.ItemType, String, boolean)
     * @see #setMockRegistrationRequired(AuthenticationOptionChecker, AuthenticationDataCreator, School.ItemType, String, boolean)
     */
    public static AuthenticationDataCreator setMockDate(final AuthenticationOptionChecker option, final AuthenticationDataCreator creator,
            final School.ItemType type, final String itemId, final long checkTime, final boolean isOpen) throws DatabaseAccessException {
        // specific results
        AuthenticationDataCreator tempCreator = creator;
        if (tempCreator == null) {
            tempCreator = mock(AuthenticationDataCreator.class);
        }
        when(option.createDataGrabber(
                createAnyEqMatcher(type, School.ItemType.class),
                createAnyEqMatcher(itemId, String.class)))
                .thenReturn(tempCreator);
        when(option.authenticateDate(tempCreator, checkTime))
                .thenReturn(isOpen);
        return tempCreator;
    }

    /**
     * Sets the mock publish state for the given {@link AuthenticationOptionChecker}.
     *
     * @param option The {@link AuthenticationOptionChecker} that the publish state is being created for.
     * @param creator The {@link AuthenticationDataCreator} that will be matched for this publish state.
     *                Send in null to apply this to a new {@link AuthenticationDataCreator}.
     * @param type The {@code ItemType} which will be matched for this publish state. Send in null to apply this to any {@code ItemType}.
     * @param itemId The {@code itemId} that will be matched for this publish state. Send in null to apply this to any (@code itemId}.
     * @param isPublished The resulting mock publish state that will be returned if the {@code authChecker} is given the above matches.
     * @throws DatabaseAccessException Should not be thrown because authChecker should be a mock.
     * @return {@link AuthenticationDataCreator}, A new instance if {@code creator} is null otherwise it is the value of {@code creator}.
     * @see #setMockDate(AuthenticationOptionChecker, AuthenticationDataCreator, School.ItemType, String, long, boolean)
     * @see #setMockRegistrationRequired(AuthenticationOptionChecker, AuthenticationDataCreator, School.ItemType, String, boolean)
     */
    public static AuthenticationDataCreator setMockPublished(final AuthenticationOptionChecker option, final AuthenticationDataCreator creator,
            final School.ItemType type, final String itemId, final boolean isPublished) throws DatabaseAccessException {
        // specific results
        AuthenticationDataCreator tempCreator = creator;
        if (tempCreator == null) {
            tempCreator = mock(AuthenticationDataCreator.class);
        }
        when(option.createDataGrabber(
                createAnyEqMatcher(type, School.ItemType.class),
                createAnyEqMatcher(itemId, String.class)))
                .thenReturn(tempCreator);
        when(option.isItemPublished(tempCreator))
                .thenReturn(isPublished);
        return tempCreator;
    }

    /**
     * Sets the mock registration state for the given {@link AuthenticationOptionChecker}.
     *
     * @param option The {@link AuthenticationOptionChecker} that the registration state is being created for.
     * @param creator The {@link AuthenticationDataCreator} that will be matched for this registration state.
     *                Send in null to apply this to a new {@link AuthenticationDataCreator}.
     * @param type The {@code ItemType} which will be matched for this registration state. Send in null to apply this to any {@code ItemType}.
     * @param itemId The {@code itemId} that will be matched for this registration state. Send in null to apply this to any (@code itemId}.
     * @param isRegistrationRequired The resulting mock registration state that will be returned if the {@code authChecker} is given the above matches.
     * @throws DatabaseAccessException Should not be thrown because authChecker should be a mock.
     * @return {@link AuthenticationDataCreator}, A new instance if {@code creator} is null otherwise it is the value of {@code creator}.
     * @see #setMockDate(AuthenticationOptionChecker, AuthenticationDataCreator, School.ItemType, String, long, boolean)
     * @see #setMockPublished(AuthenticationOptionChecker, AuthenticationDataCreator, School.ItemType, String, boolean)
     */
    public static AuthenticationDataCreator setMockRegistrationRequired(final AuthenticationOptionChecker option,
            final AuthenticationDataCreator creator,
            final School.ItemType type, final String itemId, final boolean isRegistrationRequired) throws DatabaseAccessException {
        // specific results
        AuthenticationDataCreator tempCreator = creator;
        if (tempCreator == null) {
            tempCreator = mock(AuthenticationDataCreator.class);
        }
        when(option.createDataGrabber(
                createAnyEqMatcher(type, School.ItemType.class),
                createAnyEqMatcher(itemId, String.class)))
                .thenReturn(tempCreator);
        when(option.isItemRegistrationRequired(tempCreator))
                .thenReturn(isRegistrationRequired);
        return tempCreator;
    }
}
