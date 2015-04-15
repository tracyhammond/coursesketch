package utilities;

import coursesketch.server.interfaces.SocketSession;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import protobuf.srl.request.Message;

import java.awt.event.ActionListener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Rauank on 4/9/15.
 */
public class SocketMocker {

    /**
     * An implementable listener
     */
    public static interface SocketListener {
        public void listen(Message.Request r) throws Exception;
    }

    /**
     * Mocks a socket that you can send a method to.
     * @param listener
     * @return a mocked socket.
     */
    public static SocketSession mockedSocket(final SocketListener listener) {
        SocketSession mockedSocket = mock(SocketSession.class);
        when(mockedSocket.send(any(Message.Request.class))).then(new Answer<Message.Request>() {
            @Override public Message.Request answer(InvocationOnMock invocationOnMock) throws Throwable {
                listener.listen((Message.Request) invocationOnMock.getArguments()[0]);
                return null;
            }
        });
        return mockedSocket;
    }
}
