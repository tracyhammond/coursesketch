package handlers;

import com.github.fakemongo.junit.FongoRule;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DB;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import protobuf.srl.utils.Util;
import utilities.BreakDatabase;
import utilities.SocketMocker;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

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
    public void brokenDbReturnsExceptionRequest() throws DatabaseAccessException {
        String[] values = breakDatabase.invalidCourseAuthentication();
        Data.ItemRequest.Builder itemRequest = Data.ItemRequest.newBuilder();
        itemRequest.setQuery(Data.ItemQuery.COURSE);
        itemRequest.addItemId(values[1]);

        Message.Request request = createRequest(values[0], 0, itemRequest.build());

        SocketSession session = SocketMocker.mockedSocket(new SocketMocker.SocketListener() {
            @Override public void listen(Message.Request r) throws Exception {
                Message.ProtoException pException = Message.ProtoException.parseFrom(r.getOtherData());
                LOG.info("decoded Exception object {}", pException.getMssg().toString());
                LOG.info("message object {}", r.toString());
                Data.DataResult dataResult = Data.DataResult.parseFrom(r.getOtherData());
                LOG.info("result object {}", dataResult.toString());
            }
        });

        DataRequestHandler.handleRequest(request, session, "sessionId", mock(MultiConnectionManager.class));
    }

    public Message.Request createRequest(String userID, long messageTime, Data.ItemRequest... requests) {
        Data.DataRequest.Builder dataRequest = Data.DataRequest.newBuilder();
        dataRequest.addAllItems(Arrays.asList(requests));

        Message.Request.Builder request = Message.Request.newBuilder();
        request.setServersideId(userID);
        request.setMessageTime(messageTime);
        request.setOtherData(dataRequest.build().toByteString());
        request.setRequestType(Message.Request.MessageType.DATA_REQUEST);
        return request.build();
    }
}
