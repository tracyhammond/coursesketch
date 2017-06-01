package coursesketch.auth;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.identity.IdentityWebSocketClient;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.request.Message;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.services.identity.Identity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by dtracers on 12/30/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Authentication.AuthenticationService.class, AuthenticationWebSocketClient.class})
public class AuthenticationWebSocketClientTest {

    private static final String VALID_USERNAME = "Valid username";
    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";
    private static final String INVALID_REGISTRATION_KEY = "NOT VALID KEY YO";
    private static final String INVALID_USERNAME = "NOT VALID USERNAME";

    public static final School.ItemType INVALID_ITEM_TYPE = School.ItemType.LECTURE;
    public static final School.ItemType VALID_ITEM_TYPE = School.ItemType.COURSE;
    public static final School.ItemType VALID_ITEM_CHILD_TYPE = School.ItemType.ASSIGNMENT;

    public static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_CHILD_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_ID = new ObjectId().toHexString();

    public static final String VALID_GROUP_ID = new ObjectId().toHexString();
    public static final String INVALID_GROUP_ID = new ObjectId().toHexString();

    public static final String TEACHER_AUTH_ID = new ObjectId().toHexString();
    public static final String STUDENT_AUTH_ID = new ObjectId().toHexString();
    public static final String MOD_AUTH_ID = new ObjectId().toHexString();

    public static final String TEACHER_USER_ID = new ObjectId().toHexString();
    public static final String STUDENT_USER_ID = new ObjectId().toHexString();
    public static final String MOD_USER_ID = new ObjectId().toHexString();

    // this user id is not in the db
    public static final String NO_ACCESS_ID = new ObjectId().toHexString();

    @Mock
    Authentication.AuthenticationService.BlockingInterface mockAuthenticationService;
    URI mockUri;
    @Mock
    AbstractServerWebSocketHandler mockHandler;

    @Mock
    private AuthenticationChecker authChecker;
    @Mock
    private AuthenticationOptionChecker optionChecker;

    AuthenticationWebSocketClient webclient;

    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(Authentication.AuthenticationService.class);

        when(Authentication.AuthenticationService.newBlockingStub(any(BlockingRpcChannel.class))).thenReturn(mockAuthenticationService);

        mockUri = new URI("http://localhost");
        webclient = new AuthenticationWebSocketClient(mockUri, mockHandler);

        webclient = PowerMockito.spy(webclient);

        PowerMockito.doReturn(mock(RpcClientChannel.class)).when(webclient).getRpcChannel();

        final Authentication.AuthResponse authResponse = Authentication.AuthResponse.newBuilder()
                .build();

        when(mockAuthenticationService.authorizeUser(any(RpcController.class), any(Authentication.AuthRequest.class)))
                .thenReturn(authResponse);

        when(mockAuthenticationService.createNewItem(any(RpcController.class), any(Authentication.AuthCreationRequest.class)))
                .thenReturn(Message.DefaultResponse.getDefaultInstance());

        when(mockAuthenticationService.registerUser(any(RpcController.class), any(Authentication.UserRegistration.class)))
                .thenReturn(Message.DefaultResponse.getDefaultInstance());
    }

    @Test(expected = AuthenticationException.class)
    public void throwsIfCalledWithNoAuthType() throws Exception {
        Authentication.AuthType authType = Authentication.AuthType.getDefaultInstance();
        webclient.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, VALID_USERNAME, authType);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .setAuthParams(authType)
                .build();

        Mockito.verify(mockAuthenticationService).authorizeUser(any(RpcController.class), eq(request));
    }

    @Test
    public void callsAuthorizeUserServiceCorrectly() throws Exception {
        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build();
        webclient.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, authType);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .setAuthParams(authType)
                .build();

        Mockito.verify(mockAuthenticationService).authorizeUser(any(RpcController.class), eq(request));
    }

    @Test
    public void createNewItemCallsServiceWithCorrectValuesNullParent() throws Exception {

        webclient.createNewItem(VALID_ITEM_TYPE, VALID_ITEM_ID, null, VALID_USERNAME, VALID_REGISTRATION_KEY);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(VALID_USERNAME)
                .build();

        final Authentication.AuthCreationRequest build = Authentication.AuthCreationRequest.newBuilder()
                .setItemRequest(request)
                .setRegistrationKey(VALID_REGISTRATION_KEY)
                .build();

        Mockito.verify(mockAuthenticationService).createNewItem(any(RpcController.class), eq(build));
    }

    @Test
    public void createNewItemCallsServiceWithCorrectValuesValidParent() throws Exception {

        webclient.createNewItem(VALID_ITEM_TYPE, VALID_ITEM_CHILD_ID, VALID_ITEM_ID, VALID_USERNAME, VALID_REGISTRATION_KEY);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_CHILD_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(VALID_USERNAME)
                .build();

        final Authentication.AuthCreationRequest build = Authentication.AuthCreationRequest.newBuilder()
                .setItemRequest(request)
                .setRegistrationKey(VALID_REGISTRATION_KEY)
                .setParentItemId(VALID_ITEM_ID)
                .build();

        Mockito.verify(mockAuthenticationService).createNewItem(any(RpcController.class), eq(build));
    }

    @Test
    public void registerUserCallsServiceWithCorrectValues() throws Exception {

        webclient.registerUser(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_USER_ID, VALID_REGISTRATION_KEY);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_USER_ID)
                .build();

        final Authentication.UserRegistration userRequest = Authentication.UserRegistration.newBuilder()
                .setItemRequest(request)
                .setRegistrationKey(VALID_REGISTRATION_KEY)
                .build();


        Mockito.verify(mockAuthenticationService).registerUser(any(RpcController.class), eq(userRequest));
    }
}
