package coursesketch.identity;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
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
import protobuf.srl.utils.Util;
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
@PrepareForTest({ Identity.IdentityService.class, IdentityWebSocketClient.class})
public class IdentityWebSocketClientTest {

    private static final String VALID_USERNAME = "Valid username";
    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";
    private static final String INVALID_REGISTRATION_KEY = "NOT VALID KEY YO";
    private static final String INVALID_USERNAME = "NOT VALID USERNAME";

    public static final Util.ItemType INVALID_ITEM_TYPE = Util.ItemType.BANK_PROBLEM;
    public static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;
    public static final Util.ItemType VALID_ITEM_CHILD_TYPE = Util.ItemType.ASSIGNMENT;

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
    Identity.IdentityService.BlockingInterface mockIdentityService;
    URI mockUri;
    @Mock
    AbstractServerWebSocketHandler mockHandler;

    Authenticator dbAuthChecker;
    @Mock
    private AuthenticationChecker authChecker;
    @Mock
    private AuthenticationOptionChecker optionChecker;

    IdentityWebSocketClient webclient;

    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(Identity.IdentityService.class);

        when(Identity.IdentityService.newBlockingStub(any(BlockingRpcChannel.class))).thenReturn(mockIdentityService);

        mockUri = new URI("http://localhost");
        webclient = new IdentityWebSocketClient(mockUri, mockHandler);

        webclient = PowerMockito.spy(webclient);

        PowerMockito.doReturn(mock(RpcClientChannel.class)).when(webclient).getRpcChannel();

        final Identity.UserNameResponse userNameResponse = Identity.UserNameResponse.newBuilder()
                .addUserNames(Identity.UserNameResponse.MapFieldEntry.newBuilder().setKey("KEY").setValue("VALUE"))
                .build();

        when(mockIdentityService.getItemRoster(any(RpcController.class), any(Identity.RequestRoster.class)))
                .thenReturn(userNameResponse);

        when(mockIdentityService.createNewItem(any(RpcController.class), any(Identity.IdentityCreationRequest.class)))
                .thenReturn(Message.DefaultResponse.getDefaultInstance());

        when(mockIdentityService.registerUser(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(Message.DefaultResponse.getDefaultInstance());

        when(mockIdentityService.getUserIdentity(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(userNameResponse);

        when(mockIdentityService.getUserName(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(userNameResponse);

        when(mockIdentityService.createNewUser(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(userNameResponse);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getRosterThrowsIfReturnedEmptyRoster() throws Exception {
        when(mockIdentityService.getItemRoster(any(RpcController.class), any(Identity.RequestRoster.class)))
                .thenReturn(Identity.UserNameResponse.getDefaultInstance());

        webclient.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
    }

    @Test
    public void getRosterCallsServiceCorrectlyWithNullList() throws Exception {
        webclient.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        final Identity.RequestRoster build = Identity.RequestRoster.newBuilder()
                .setRequestData(request)
                .build();

        Mockito.verify(mockIdentityService).getItemRoster(any(RpcController.class), eq(build));
    }

    @Test
    public void getRosterCallsServiceCorrectlyWithFilledList() throws Exception {

        List<String> userIdList = new ArrayList<>();
        userIdList.add(TEACHER_AUTH_ID);
        userIdList.add(VALID_GROUP_ID);
        userIdList.add(VALID_ITEM_ID);
        webclient.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, userIdList, dbAuthChecker);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        final Identity.RequestRoster build = Identity.RequestRoster.newBuilder()
                .setRequestData(request)
                .addAllUserIds(userIdList)
                .build();

        Mockito.verify(mockIdentityService).getItemRoster(any(RpcController.class), eq(build));
    }

    @Test
    public void createNewItemCallsServiceWithCorrectValuesNullParent() throws Exception {

        webclient.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(TEACHER_USER_ID)
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        final Identity.IdentityCreationRequest build = Identity.IdentityCreationRequest.newBuilder()
                .setItemRequest(request)
                .build();

        Mockito.verify(mockIdentityService).createNewItem(any(RpcController.class), eq(build));
    }

    @Test
    public void createNewItemCallsServiceWithCorrectValuesValidParent() throws Exception {

        webclient.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, VALID_ITEM_CHILD_ID, dbAuthChecker);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(TEACHER_USER_ID)
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        final Identity.IdentityCreationRequest build = Identity.IdentityCreationRequest.newBuilder()
                .setItemRequest(request)
                .setParentItemId(VALID_ITEM_CHILD_ID)
                .build();

        Mockito.verify(mockIdentityService).createNewItem(any(RpcController.class), eq(build));
    }

    @Test
    public void registerUserCallsServiceWithCorrectValues() throws Exception {

        webclient.registerUserInItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(TEACHER_USER_ID)
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        Mockito.verify(mockIdentityService).registerUser(any(RpcController.class), eq(request));
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserIdentityThrowsIfResultIsEmpty() throws Exception {
        when(mockIdentityService.getUserIdentity(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(Identity.UserNameResponse.getDefaultInstance());

        webclient.getUserIdentity(VALID_USERNAME, TEACHER_AUTH_ID);
    }

    @Test
    public void getUserIdentityCallsServiceWithCorrectValues() throws Exception {

        webclient.getUserIdentity(VALID_USERNAME, TEACHER_AUTH_ID);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(VALID_USERNAME)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        Mockito.verify(mockIdentityService).getUserIdentity(any(RpcController.class), eq(request));
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserNameThrowsIfResultIsEmpty() throws Exception {
        when(mockIdentityService.getUserName(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(Identity.UserNameResponse.getDefaultInstance());

        webclient.getUserName(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test
    public void getUserNameCallsServiceWithCorrectValues() throws Exception {

        webclient.getUserName(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(TEACHER_USER_ID)
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        Mockito.verify(mockIdentityService).getUserName(any(RpcController.class), eq(request));
    }

    @Test(expected = DatabaseAccessException.class)
    public void createNewUserThrowsIfResultIsEmpty() throws Exception {
        when(mockIdentityService.createNewUser(any(RpcController.class), any(Identity.IdentityRequest.class)))
                .thenReturn(Identity.UserNameResponse.getDefaultInstance());

        webclient.createNewUser(VALID_USERNAME);
    }

    @Test
    public void createNewUserCallsServiceWithCorrectValues() throws Exception {

        webclient.createNewUser(VALID_USERNAME);

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setUserId(VALID_USERNAME)
                .build();

        Mockito.verify(mockIdentityService).createNewUser(any(RpcController.class), eq(request));
    }
}
