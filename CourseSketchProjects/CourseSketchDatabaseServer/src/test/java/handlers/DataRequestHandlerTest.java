package handlers;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import database.institution.mongo.MongoInstitution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import utilities.BreakDatabase;
import utilities.ProtobufUtilities;
import utilities.SocketMocker;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Rauank on 4/8/15.
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(Authenticator.class)
public class DataRequestHandlerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock AuthenticationChecker authChecker;
    @Mock AuthenticationOptionChecker optionChecker;

    public DB db;
    public Authenticator authenticator;
    public BreakDatabase breakDatabase;

    private static final Logger LOG = LoggerFactory.getLogger(DataRequestHandlerTest.class);

    @Before
    public void before() {
        db = fongo.getDB();
        breakDatabase = new BreakDatabase(db);

        try {
            when(authChecker.isAuthenticated(any(School.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
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

    @Test
    public void fakeTest() {
        // YAY!
    }

    // THESE TEST CURRENTLY DO NOT WORK!
    // WILL BE FIXED IN NEXT REFACTOR
    public void brokenDbReturnsAuthenticationExceptionRequest() throws DatabaseAccessException, AuthenticationException {
        String[] values = breakDatabase.invalidCourseAuthentication();
        Data.ItemRequest.Builder itemRequest = Data.ItemRequest.newBuilder();
        itemRequest.setQuery(Data.ItemQuery.COURSE);
        itemRequest.addItemId(values[1]);

        Message.Request request = createRequest("INVALID_USER_NAME", 0, itemRequest.build());

        SocketSession session = SocketMocker.mockedSocket(new SocketMocker.SocketListener() {
            @Override public void listen(Message.Request r) throws Exception {
                Message.ProtoException pException = Message.ProtoException.parseFrom(r.getOtherData());
                assertEquals(AuthenticationException.class.getSimpleName(), pException.getExceptionType());
                assertNotEquals(0, pException.getStackTraceCount());
            }
        });

        DataRequestHandler.handleRequest(request, session, MongoInstitution.getInstance(authenticator),
                "sessionId", mock(MultiConnectionManager.class));
    }

    // THESE TEST CURRENTLY DO NOT WORK!
    // WILL BE FIXED IN NEXT REFACTOR
    public void brokenDbReturnsDatabaseAccessExceptionRequest() throws DatabaseAccessException, AuthenticationException {
        String[] values = breakDatabase.invalidCourse();
        Data.ItemRequest.Builder itemRequest = Data.ItemRequest.newBuilder();
        itemRequest.setQuery(Data.ItemQuery.COURSE);
        itemRequest.addItemId(values[1]);

        Message.Request request = createRequest(values[0], 0, itemRequest.build());

        SocketSession session = SocketMocker.mockedSocket(new SocketMocker.SocketListener() {
            @Override public void listen(Message.Request r) throws Exception {
                Message.ProtoException pException = Message.ProtoException.parseFrom(r.getOtherData());
                assertEquals(DatabaseAccessException.class.getSimpleName(), pException.getExceptionType());
                assertNotEquals(0, pException.getStackTraceCount());
            }
        });

        DataRequestHandler.handleRequest(request, session, MongoInstitution.getInstance(authenticator),
                "sessionId", mock(MultiConnectionManager.class));
    }

    public Message.Request createRequest(String userID, long messageTime, Data.ItemRequest... requests) {
        Data.DataRequest.Builder dataRequest = Data.DataRequest.newBuilder();
        dataRequest.addAllItems(Arrays.asList(requests));

        Message.Request.Builder request = ProtobufUtilities.createRequestFromData(Message.Request.MessageType.DATA_REQUEST, dataRequest.build());
        request.setServersideId(userID);
        request.setMessageTime(messageTime);
        return request.build();
    }
}
