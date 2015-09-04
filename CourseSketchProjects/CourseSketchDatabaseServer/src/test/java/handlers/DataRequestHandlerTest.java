package handlers;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import utilities.BreakDatabase;
import utilities.ProtobufUtilities;
import utilities.SocketMocker;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by Rauank on 4/8/15.
 */
public class DataRequestHandlerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;
    public BreakDatabase breakDatabase;

    private static final Logger LOG = LoggerFactory.getLogger(DataRequestHandlerTest.class);

    @Before
    public void before() {
        db = fongo.getDB();
        fauth = new Authenticator(new MongoAuthenticator(fongo.getDB()));
        breakDatabase = new BreakDatabase(db);
    }

    @Test
    public void brokenDbReturnsAuthenticationExceptionRequest() throws DatabaseAccessException {
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

        DataRequestHandler.handleRequest(request, session, instance, "sessionId", mock(MultiConnectionManager.class));
    }

    @Test
    public void brokenDbReturnsDatabaseAccessExceptionRequest() throws DatabaseAccessException {
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

        DataRequestHandler.handleRequest(request, session, instance, "sessionId", mock(MultiConnectionManager.class));
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
