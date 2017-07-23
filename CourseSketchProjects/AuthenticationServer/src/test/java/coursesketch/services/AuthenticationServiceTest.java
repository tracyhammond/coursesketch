package coursesketch.services;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.DbAuthChecker;
import coursesketch.database.auth.DbAuthManager;
import coursesketch.database.util.DatabaseAccessException;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.request.Message;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbAuthChecker.class, DbAuthManager.class })
public class AuthenticationServiceTest {
    private static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;

    private static final String PARENT_ITEM_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_ID = new ObjectId().toHexString();

    private static final String VALID_OWNER_ID = new ObjectId().toHexString();

    private static final String REGISTRATION_KEY = new ObjectId().toHexString();

    @Mock
    private DbAuthChecker authChecker;
    @Mock
    private DbAuthManager authManager;
    @Mock
    private RpcController controller;
    @Mock
    private RpcCallback<Authentication.AuthResponse> authResponseRpcCallback;
    @Mock
    private RpcCallback<Message.DefaultResponse> defaultResponseRpcCallback;

    private AuthenticationService service;
    private Authentication.AuthType emptyParams;
    private Authentication.AuthRequest authRequest;
    private Authentication.AuthResponse authResponse;
    private AuthenticationException authenticationException;
    private DatabaseAccessException databaseAccessException;
    private Message.DefaultResponse defaultResponse;
    private Authentication.AuthCreationRequest creationRequest;
    private Authentication.UserRegistration userRegistration;

    @Before
    public void setup() throws DatabaseAccessException, AuthenticationException {
        service = new AuthenticationService(authChecker, authManager);
        emptyParams = Authentication.AuthType.newBuilder().build();
        authRequest = Authentication.AuthRequest.newBuilder().setItemType(VALID_ITEM_TYPE).setItemId(VALID_ITEM_ID)
                .setAuthId(VALID_OWNER_ID).setAuthParams(emptyParams).build();
        creationRequest =
                Authentication.AuthCreationRequest.newBuilder().setItemRequest(authRequest).setParentItemId(PARENT_ITEM_ID).setRegistrationKey
                        (REGISTRATION_KEY).build();
        userRegistration =
                Authentication.UserRegistration.newBuilder().setItemRequest(authRequest).setRegistrationKey(REGISTRATION_KEY).build();
        authResponse = Authentication.AuthResponse.newBuilder().build();
        defaultResponse = Message.DefaultResponse.newBuilder().build();
        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class))).thenReturn
                (authResponse);
        authenticationException = new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        databaseAccessException = new DatabaseAccessException("Whoops");
    }

    @Test
    public void successfulAuthorizeUser() throws DatabaseAccessException, AuthenticationException {
        service.authorizeUser(controller, authRequest, authResponseRpcCallback);
        Mockito.verify(authChecker).isAuthenticated(eq(VALID_ITEM_TYPE), eq(VALID_ITEM_ID), eq(VALID_OWNER_ID), eq(emptyParams));
        Mockito.verify(authResponseRpcCallback).run(authResponse);
    }

    @Test
    public void failAuthorizeUser() throws DatabaseAccessException, AuthenticationException {
        when(authChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenThrow(authenticationException);

        service.authorizeUser(controller, authRequest, authResponseRpcCallback);

        Mockito.verify(authChecker).isAuthenticated(eq(VALID_ITEM_TYPE), eq(VALID_ITEM_ID), eq(VALID_OWNER_ID), eq(emptyParams));
        Mockito.verify(controller).setFailed(eq(authenticationException.toString()));
    }

    @Test
    public void successfulCreateNewItem() throws DatabaseAccessException, AuthenticationException {
        service.createNewItem(controller, creationRequest, defaultResponseRpcCallback);
        Mockito.verify(authManager).insertNewItem(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(PARENT_ITEM_ID), eq
                (REGISTRATION_KEY), eq(authChecker));
        Mockito.verify(defaultResponseRpcCallback).run(defaultResponse);
    }

    @Test
    public void failToCreateNewItemAuth() throws DatabaseAccessException, AuthenticationException {
        Mockito.doThrow(
                authenticationException).when(authManager).insertNewItem(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(PARENT_ITEM_ID), eq
                (REGISTRATION_KEY), eq(authChecker));
        service.createNewItem(controller, creationRequest, defaultResponseRpcCallback);
        Mockito.verify(authManager).insertNewItem(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(PARENT_ITEM_ID), eq
                (REGISTRATION_KEY), eq(authChecker));
        Mockito.verify(defaultResponseRpcCallback, never()).run(eq(defaultResponse));
        Mockito.verify(defaultResponseRpcCallback).run(any(defaultResponse.getClass()));
    }

    @Test
    public void failToCreateNewItemDatabase() throws DatabaseAccessException, AuthenticationException {
        Mockito.doThrow(
                databaseAccessException).when(authManager).insertNewItem(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(PARENT_ITEM_ID), eq
                (REGISTRATION_KEY), eq(authChecker));
        service.createNewItem(controller, creationRequest, defaultResponseRpcCallback);
        Mockito.verify(authManager).insertNewItem(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(PARENT_ITEM_ID), eq
                (REGISTRATION_KEY), eq(authChecker));
        Mockito.verify(defaultResponseRpcCallback, never()).run(eq(defaultResponse));
        Mockito.verify(defaultResponseRpcCallback).run(any(defaultResponse.getClass()));
    }

    @Test
    public void successfulRegisterUser() throws DatabaseAccessException, AuthenticationException {
        service.registerUser(controller, userRegistration, defaultResponseRpcCallback);
        Mockito.verify(authManager).registerSelf(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(REGISTRATION_KEY), eq(authChecker));
        Mockito.verify(defaultResponseRpcCallback).run(defaultResponse);
    }

    @Test
    public void failToRegisterUserAuth() throws DatabaseAccessException, AuthenticationException {
        Mockito.doThrow(
                authenticationException).when(authManager).registerSelf(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(REGISTRATION_KEY),
                eq(authChecker));
        service.registerUser(controller, userRegistration, defaultResponseRpcCallback);
        Mockito.verify(authManager).registerSelf(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(REGISTRATION_KEY),
                eq(authChecker));
        Mockito.verify(defaultResponseRpcCallback, never()).run(eq(defaultResponse));
        Mockito.verify(defaultResponseRpcCallback).run(any(defaultResponse.getClass()));
    }

    @Test
    public void failToRegisterUserData() throws DatabaseAccessException, AuthenticationException {
        Mockito.doThrow(
                databaseAccessException).when(authManager).registerSelf(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(REGISTRATION_KEY),
                eq(authChecker));
        service.registerUser(controller, userRegistration, defaultResponseRpcCallback);
        Mockito.verify(authManager).registerSelf(eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(REGISTRATION_KEY),
                eq(authChecker));
        Mockito.verify(defaultResponseRpcCallback, never()).run(eq(defaultResponse));
        Mockito.verify(defaultResponseRpcCallback).run(any(defaultResponse.getClass()));
    }

    @Test
    public void successfulAddUser() throws DatabaseAccessException, AuthenticationException {
        service.addUser(controller, userRegistration, defaultResponseRpcCallback);
        Mockito.verify(authManager).addUser(eq(REGISTRATION_KEY), eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(authChecker), eq
                (emptyParams));
        Mockito.verify(defaultResponseRpcCallback).run(defaultResponse);
    }

    @Test
    public void failToAddUserAuth() throws DatabaseAccessException, AuthenticationException {
        Mockito.doThrow(
                authenticationException).when(authManager).addUser(eq(REGISTRATION_KEY), eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(authChecker), eq
                (emptyParams));
        service.addUser(controller, userRegistration, defaultResponseRpcCallback);
        Mockito.verify(authManager).addUser(eq(REGISTRATION_KEY), eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(authChecker), eq
                (emptyParams));
        Mockito.verify(defaultResponseRpcCallback, never()).run(eq(defaultResponse));
        Mockito.verify(defaultResponseRpcCallback).run(any(defaultResponse.getClass()));
    }

    @Test
    public void failToAddUserDatabase() throws DatabaseAccessException, AuthenticationException {
        Mockito.doThrow(
                databaseAccessException).when(authManager).addUser(eq(REGISTRATION_KEY), eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(authChecker), eq
                (emptyParams));
        service.addUser(controller, userRegistration, defaultResponseRpcCallback);
        Mockito.verify(authManager).addUser(eq(REGISTRATION_KEY), eq(VALID_OWNER_ID), eq(VALID_ITEM_ID), eq(VALID_ITEM_TYPE), eq(authChecker), eq
                (emptyParams));
        Mockito.verify(defaultResponseRpcCallback, never()).run(eq(defaultResponse));
        Mockito.verify(defaultResponseRpcCallback).run(any(defaultResponse.getClass()));
    }
}
