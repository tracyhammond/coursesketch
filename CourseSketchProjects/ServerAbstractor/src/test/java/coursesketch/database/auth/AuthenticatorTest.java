package coursesketch.database.auth;

import database.DatabaseAccessException;
import database.RequestConverter;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.utils.Util;
import protobuf.srl.services.authentication.Authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatorTest {
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;
    Authenticator authenticator;

    @Before
    public void setup() {
        try {
            when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                    .thenReturn(Authentication.AuthResponse.getDefaultInstance());

            when(optionChecker.authenticateDate(any(AuthenticationDataCreator.class), anyLong()))
                    .thenReturn(false);

            when(optionChecker.isItemPublished(any(AuthenticationDataCreator.class)))
                    .thenReturn(false);

            when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class)))
                    .thenReturn(true);
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        authenticator = new Authenticator(authChecker, optionChecker);
    }

    @After
    public void teardown() {
        authenticator = null;
    }

    // Simple precondition testing

    @Test(expected = NullPointerException.class)
    public void creationFailsIfGivenNullChecker() {
        new Authenticator(null, Mockito.mock(AuthenticationOptionChecker.class));
    }

    @Test(expected = NullPointerException.class)
    public void creationFailsIfGivenOptionChecker() {
        new Authenticator(Mockito.mock(AuthenticationChecker.class), null);
    }

    @Test()
    public void creationSucceedsIfGivenValue() {
        Authenticator auth = new Authenticator(Mockito.mock(AuthenticationChecker.class), Mockito.mock(AuthenticationOptionChecker.class));
        Assert.assertNotNull(auth);
    }

    @Test(expected = NullPointerException.class)
    public void authenticatorThrowsExceptionIfInvalidCollectionType() throws Exception {
        authenticator.checkAuthentication(null, null, null, 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void authenticatorThrowsExceptionIfInvalidItemId() throws Exception {
        authenticator.checkAuthentication(Util.ItemType.COURSE, null, null, 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void authenticatorThrowsExceptionIfInvalidUserId() throws Exception {
        authenticator.checkAuthentication(Util.ItemType.COURSE, "", null, 0, null);
    }

    @Test(expected = NullPointerException.class)
    public void authenticatorThrowsExceptionIfNullAuthType() throws Exception {
        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, null);
    }

    @Test(expected = AuthenticationException.class)
    public void authenticatorThrowsExceptionIfInvalidAuthType() throws Exception {
        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.getDefaultInstance());
    }

    // TIME CHECKING

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

    // Check that code calls methods only when certain auth types are set

    @Test
    public void authenticatorDoesNotCheckDateIfItIsNotSet() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());

        verify(optionChecker, never()).authenticateDate(any(AuthenticationDataCreator.class), anyLong());
    }

    @Test
    public void authenticatorOnlyChecksDate() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .build());

        verify(optionChecker, atLeastOnce()).authenticateDate(any(AuthenticationDataCreator.class), anyLong());
        verify(optionChecker, atLeastOnce()).createDataGrabber(any(Util.ItemType.class), anyString());
        verifyNoMoreInteractions(optionChecker);
    }

    @Test
     public void authenticatorDoesNotIfPublishedAndRegistrationIfItIsNotSet() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .build());

        verify(optionChecker, never()).isItemPublished(any(AuthenticationDataCreator.class));
        verify(optionChecker, never()).isItemRegistrationRequired(any(AuthenticationDataCreator.class));
    }

    @Test
    public void authenticatorDoesNotRegistrationIfItIsAssignment() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.ASSIGNMENT, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckIsRegistrationRequired(true)
                .build());

        verify(optionChecker, never()).isItemPublished(any(AuthenticationDataCreator.class));
        verify(optionChecker, never()).isItemRegistrationRequired(any(AuthenticationDataCreator.class));
    }

    @Test
    public void authenticatorDoesNotCheckPublishedIfItIsCourseProblem() throws Exception {
        authenticator.checkAuthentication(Util.ItemType.COURSE_PROBLEM, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckIsRegistrationRequired(true)
                .build());

        verify(optionChecker, never()).isItemPublished(any(AuthenticationDataCreator.class));
        verify(optionChecker, never()).isItemRegistrationRequired(any(AuthenticationDataCreator.class));
    }

    @Test
    public void authenticatorDoesNotCheckDateAndRegistration() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckIsPublished(true)
                .build());

        verify(optionChecker, atLeastOnce()).createDataGrabber(any(Util.ItemType.class), anyString());
        verify(optionChecker, atLeastOnce()).isItemPublished(any(AuthenticationDataCreator.class));
        verifyNoMoreInteractions(optionChecker);
    }

    @Test
    public void authenticatorOnlyChecksPublishedAndRegistration() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());

        verify(optionChecker, atLeastOnce()).isItemRegistrationRequired(any(AuthenticationDataCreator.class));
        verify(optionChecker, atLeastOnce()).createDataGrabber(any(Util.ItemType.class), anyString());
        verifyNoMoreInteractions(optionChecker);
    }

    @Test
    public void authenticatorDoesNotCheckOptionIfNoneAreSet() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckingUser(true)
                .build());

        verifyNoMoreInteractions(optionChecker);
    }

    @Test
    public void authenticatorDoesNotCheckAuthIfNoneAreSet() throws Exception {

        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, Authentication.AuthType.newBuilder()
                .setCheckDate(true)
                .build());

        verifyNoMoreInteractions(authChecker);
    }

    @Test
    public void authenticatorCheckAuthIfSet() throws Exception {

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckingUser(true)
                .build();
        authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        verify(authChecker, atLeastOnce()).isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), eq(type));
        verifyNoMoreInteractions(optionChecker);
        verifyNoMoreInteractions(authChecker);
    }

    // Authentication exception handling
    @Test(expected = AuthenticationException.class)
    public void authenticatorThrowsWhenAuthCheckThrowsAuthExcep() throws Exception {

        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenThrow(AuthenticationException.class);
        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);
    }

    @Test(expected = AuthenticationException.class)
    public void authenticatorThrowsWhenAuthCheckThrowsDBExcep() throws Exception {

        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenThrow(DatabaseAccessException.class);
        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);
    }

    @Test(expected = AuthenticationException.class)
    public void authenticatorThrowsWhenOptionCheckThrowsDBException() throws Exception {
        when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class)))
                .thenThrow(DatabaseAccessException.class);
        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);
    }

    // Authenticator Result Tests

    @Test
    public void authenticatorCheckNoPermissions() throws Exception {

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        assertFalse(responder.hasAccess());
        assertFalse(responder.hasStudentPermission());
        assertFalse(responder.hasPeerTeacherPermission());
        assertFalse(responder.hasModeratorPermission());
        assertFalse(responder.hasTeacherPermission());
        assertFalse(responder.isItemOpen());
        assertFalse(responder.isItemPublished());
        // remember this is the reverse
        assertTrue(responder.isRegistrationRequired());
    }


    @Test
    public void authenticatorHasAccessIfAuthCheckHasAccess() throws Exception {

        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                .setHasAccess(true)
                .build());

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        // Specific value being tested
        assertTrue(responder.hasAccess());

        // other values that are not the highlight of the test
        assertFalse(responder.hasStudentPermission());
        assertFalse(responder.hasPeerTeacherPermission());
        assertFalse(responder.hasModeratorPermission());
        assertFalse(responder.hasTeacherPermission());
        assertFalse(responder.isItemOpen());
        assertFalse(responder.isItemPublished());
        // remember this is the reverse
        assertTrue(responder.isRegistrationRequired());
    }


    @Test
    public void authenticatorHasAccessIfAuthNoRegistrationAndCourseIsOpen() throws Exception {

        when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class)))
                .thenReturn(false);

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        // Specific values being tested
        assertTrue(responder.hasAccess());
        assertFalse(responder.isRegistrationRequired());

        // other values that should be false
        assertFalse(responder.hasStudentPermission());
        assertFalse(responder.hasPeerTeacherPermission());
        assertFalse(responder.hasModeratorPermission());
        assertFalse(responder.hasTeacherPermission());
        assertFalse(responder.isItemOpen());
        assertFalse(responder.isItemPublished());
    }

    @Test
    public void authenticatorPermissionIfAdminPermissionIsSet() throws Exception {

        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.TEACHER)
                        .build());

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        // Specific value being tested
        assertTrue(responder.hasAccess());
        assertTrue(responder.hasStudentPermission());
        assertTrue(responder.hasPeerTeacherPermission());
        assertTrue(responder.hasModeratorPermission());
        assertTrue(responder.hasTeacherPermission());

        // other values that are not the highlight of the test
        assertFalse(responder.isItemOpen());
        assertFalse(responder.isItemPublished());
        // remember this is the reverse
        assertTrue(responder.isRegistrationRequired());
    }

    @Test
    public void authenticatorPermissionIfStudentPermissionIsSet() throws Exception {

        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.STUDENT)
                        .build());

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        // Specific value being tested
        assertTrue(responder.hasAccess());
        assertTrue(responder.hasStudentPermission());
        assertFalse(responder.hasPeerTeacherPermission());
        assertFalse(responder.hasModeratorPermission());
        assertFalse(responder.hasTeacherPermission());

        // other values that are not the highlight of the test
        assertFalse(responder.isItemOpen());
        assertFalse(responder.isItemPublished());
        // remember this is the reverse
        assertTrue(responder.isRegistrationRequired());
    }

    @Test
    public void authenticatorHasAccessIfItemIsPublished() throws Exception {

        when(optionChecker.isItemPublished(any(AuthenticationDataCreator.class))).thenReturn(true);

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckIsPublished(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        // Specific value being tested
        assertTrue(responder.isItemPublished());

        // other values that are not the highlight of the test
        assertFalse(responder.hasStudentPermission());
        assertFalse(responder.hasPeerTeacherPermission());
        assertFalse(responder.hasModeratorPermission());
        assertFalse(responder.hasTeacherPermission());
        assertFalse(responder.isItemOpen());
        assertFalse(responder.hasAccess());
        // remember this is the reverse
        assertTrue(responder.isRegistrationRequired());
    }

    @Test
    public void authenticatorHasAccessIfItemRequiresRegistration() throws Exception {

        when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class))).thenReturn(false);

        final Authentication.AuthType type = Authentication.AuthType.newBuilder()
                .setCheckIsRegistrationRequired(true)
                .build();
        AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, "", "", 0, type);

        // Specific value being tested
        assertFalse(responder.isRegistrationRequired());

        // other values that are not the highlight of the test
        assertFalse(responder.hasStudentPermission());
        assertFalse(responder.hasPeerTeacherPermission());
        assertFalse(responder.hasModeratorPermission());
        assertFalse(responder.hasTeacherPermission());
        assertFalse(responder.isItemOpen());
        assertFalse(responder.hasAccess());
        assertFalse(responder.isItemPublished());
    }
}
