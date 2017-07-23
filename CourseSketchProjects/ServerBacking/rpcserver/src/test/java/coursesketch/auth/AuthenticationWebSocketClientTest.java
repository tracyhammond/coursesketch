package coursesketch.auth;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
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
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by dtracers on 12/30/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Authentication.AuthenticationService.class, AuthenticationWebSocketClient.class })
public class AuthenticationWebSocketClientTest {

    private static final String VALID_USERNAME = "Valid username";
    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";

    private static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;

    private static final String VALID_ITEM_CHILD_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_ID = new ObjectId().toHexString();

    private static final String TEACHER_AUTH_ID = new ObjectId().toHexString();

    private static final String TEACHER_USER_ID = new ObjectId().toHexString();

    @Mock
    private Authentication.AuthenticationService.BlockingInterface mockAuthenticationService;
    @Mock
    private AbstractServerWebSocketHandler mockHandler;

    private AuthenticationWebSocketClient webclient;

    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(Authentication.AuthenticationService.class);

        when(Authentication.AuthenticationService.newBlockingStub(any(BlockingRpcChannel.class))).thenReturn(mockAuthenticationService);

        URI mockUri = new URI("http://localhost");
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
        when(mockAuthenticationService.addUser(any(RpcController.class), any(Authentication.UserRegistration.class)))
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

    @Test(expected = AuthenticationException.class)
    public void errorsOutIfResponseHasError() throws Exception {
        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build();

        final Authentication.AuthResponse build =
                Authentication.AuthResponse.newBuilder().setDefaultResponse(Message.DefaultResponse.newBuilder().setException(
                        Message.ProtoException.newBuilder().setMssg("Messg"))).build();

        when(mockAuthenticationService.authorizeUser(any(RpcController.class), any(Authentication.AuthRequest.class)))
                .thenReturn(build);

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

        webclient.createNewItem(VALID_USERNAME, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY);

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

        webclient.createNewItem(VALID_USERNAME, VALID_ITEM_CHILD_ID, VALID_ITEM_TYPE, VALID_ITEM_ID, VALID_REGISTRATION_KEY);

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

        webclient.registerUser(TEACHER_USER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_USER_ID)
                .setAuthParams(Authentication.AuthType.getDefaultInstance())
                .build();

        final Authentication.UserRegistration userRequest = Authentication.UserRegistration.newBuilder()
                .setItemRequest(request)
                .setRegistrationKey(VALID_REGISTRATION_KEY)
                .build();


        Mockito.verify(mockAuthenticationService).registerUser(any(RpcController.class), eq(userRequest));
    }

    @Test
    public void addUserCallsServiceWithCorrectValues() throws Exception {

        webclient.addUser(VALID_REGISTRATION_KEY, TEACHER_USER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, Authentication.AuthResponse.PermissionLevel.STUDENT);

        final Authentication.AuthRequest request = Authentication.AuthRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_USER_ID)
                .setAuthParams(Authentication.AuthType.newBuilder().setCheckingOwner(true).setCheckingUser(true))
                .build();

        final Authentication.UserRegistration userRequest = Authentication.UserRegistration.newBuilder()
                .setItemRequest(request)
                .setRegistrationKey(VALID_REGISTRATION_KEY)
                .build();

        Mockito.verify(mockAuthenticationService).addUser(any(RpcController.class), eq(userRequest));
    }
}
