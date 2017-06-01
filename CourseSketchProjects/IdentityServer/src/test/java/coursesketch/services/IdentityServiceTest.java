package coursesketch.identity;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.identity.IdentityManager;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import coursesketch.services.IdentityService;
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
import protobuf.srl.services.identity.Identity;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by dtracers on 6/1/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Authenticator.class, IdentityManager.class})
public class IdentityServiceTest {

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

    IdentityService identityService;

    @Mock
    IdentityManager identityManager;

    Authenticator dbAuthChecker;
    @Mock
    private AuthenticationChecker authChecker;
    @Mock
    private AuthenticationOptionChecker optionChecker;

    @Mock RpcController controller;

    @Mock
    RpcCallback callback;

    @Before
    public void before() throws Exception {
        identityService = new IdentityService(dbAuthChecker, identityManager);
    }

    @Test
    public void getRosterCallsServiceCorrectlyWithNullList() throws Exception {

        final Identity.IdentityRequest request = Identity.IdentityRequest.newBuilder()
                .setItemId(VALID_ITEM_ID)
                .setItemType(VALID_ITEM_TYPE)
                .setAuthId(TEACHER_AUTH_ID)
                .build();

        final Identity.RequestRoster build = Identity.RequestRoster.newBuilder()
                .setRequestData(request)
                .build();

        Map<String, String> map = new HashMap<>();
        map.put(TEACHER_AUTH_ID, TEACHER_USER_ID);
        when(identityManager.getItemRoster(anyString(), anyString(), any(School.ItemType.class), anyList(),
                eq(dbAuthChecker))).thenReturn(map);

        Identity.UserNameResponse response = Identity.UserNameResponse.newBuilder()
                .addUserNames(
                        Identity.UserNameResponse.MapFieldEntry.newBuilder()
                                .setKey(TEACHER_AUTH_ID)
                                .setValue(TEACHER_USER_ID)
                                .build())
                .build();

        identityService.getItemRoster(controller, build, callback);
        Mockito.verify(callback).run(response);
    }
}
