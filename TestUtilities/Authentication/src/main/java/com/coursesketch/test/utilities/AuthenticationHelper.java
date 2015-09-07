package com.coursesketch.test.utilities;

import database.DatabaseAccessException;
import database.auth.AuthenticationChecker;
import database.auth.AuthenticationDataCreator;
import database.auth.AuthenticationException;
import database.auth.AuthenticationOptionChecker;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by gigemjt on 9/5/15.
 */
public class AuthenticationHelper {

    /**
     * TODO: add this to a general course sketch testing library.
     * If element is not null it will create an eq matcher if element is not null then it will create an any matcher
     * @param element
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T createAnyEqMatcher(T element, Class<T> tClass) {
        if (element == null && tClass == null) {
            return eq(null);
        } else if(element == null) {
            return any(tClass);
        } else {
            return eq(element);
        }
    }

    /**
     * Send in null to match any type
     * @param type
     * @param itemId
     */
    public static void setMockPermissions(AuthenticationChecker authChecker, School.ItemType type, String itemId, String userId,
            Authentication.AuthType authType, Authentication.AuthResponse.PermissionLevel result)
            throws DatabaseAccessException, AuthenticationException {
        // specific results
        when(authChecker.isAuthenticated(
                createAnyEqMatcher(type, School.ItemType.class),
                createAnyEqMatcher(itemId, String.class),
                createAnyEqMatcher(userId, String.class),
                createAnyEqMatcher(authType, Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(result)
                        .build());
    }

    /**
     * Send in null to match any type.
     * Returns the tempCreator that is created if what is sent in is null
     * @param option
     * @param type
     * @param itemId
     * @param isOpen
     * @return
     */
    public static AuthenticationDataCreator setMockDate(AuthenticationOptionChecker option, AuthenticationDataCreator creator,
            School.ItemType type, String itemId, long checkTime, boolean isOpen) throws DatabaseAccessException {
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
     * Send in null to match any type.
     * Returns the tempCreator that is created if what is sent in is null
     * @param option
     * @param type
     * @param itemId
     * @param isPublished
     * @return
     */
    public static AuthenticationDataCreator setMockPublished(AuthenticationOptionChecker option, AuthenticationDataCreator creator,
            School.ItemType type, String itemId, boolean isPublished) throws DatabaseAccessException {
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
     * Send in null to match any type.
     * Returns the tempCreator that is created if what is sent in is null
     * @param option
     * @param type
     * @param itemId
     * @param isRegistraionRequired
     * @return
     */
    public static AuthenticationDataCreator setMockRegistrationRequired(AuthenticationOptionChecker option, AuthenticationDataCreator creator,
            School.ItemType type, String itemId, boolean isRegistraionRequired) throws DatabaseAccessException {
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
                .thenReturn(isRegistraionRequired);
        return tempCreator;
    }
}
