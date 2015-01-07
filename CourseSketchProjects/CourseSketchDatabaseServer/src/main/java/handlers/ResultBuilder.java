package handlers;

import com.google.protobuf.ByteString;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;

import java.util.List;

/**
 * Created by gigemjt on 1/2/15.
 */
final class ResultBuilder {

    /**
     * Utility class.
     */
    private ResultBuilder() { }

    /**
     * Builds a complete result from the query. This one is typically used in
     * the case of success.
     *
     * @param data
     *         The data from the result.
     * @param text
     *         A message from the result (typically used if there is an error
     *         but no data).
     * @param type
     *         What the original query was.
     * @return A fully built item result.
     */
    public static Data.ItemResult buildResult(final ByteString data, final String text, final Data.ItemQuery type) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        result.setData(data);
        result.setQuery(type);
        result.setReturnText(text);
        return result.build();
    }

    /**
     * Builds a result but with no binary data.
     *
     * @param data
     *         The data from the result.
     * @param type
     *         What the original query was.
     * @return A built item result with no binary data.
     */
    public static Data.ItemResult buildResult(final String data, final Data.ItemQuery type) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        result.setReturnText(data);
        result.setQuery(type);
        return result.build();
    }

    /**
     * Builds a request from a list of {@link protobuf.srl.query.Data.ItemResult}.
     *
     * @param results
     *         A list of results that need to be sent back to the user.
     * @param message
     *         A message that goes with the results (could be an error)
     * @param req
     *         The original request that was received.
     * @return A {@link protobuf.srl.request.Message.Request}.
     */
    public static Message.Request buildRequest(final List<Data.ItemResult> results, final String message, final Message.Request req) {

        Data.DataResult.Builder dataResult = null;
        if (results != null && !results.isEmpty()) {
            dataResult = Data.DataResult.newBuilder();
            dataResult.addAllResults(results);
        }

        final Message.Request.Builder dataReq = Message.Request.newBuilder();
        dataReq.setRequestType(req.getRequestType());
        dataReq.setSessionInfo(req.getSessionInfo());
        dataReq.setResponseText(message);
        if (dataResult != null) {
            dataReq.setOtherData(dataResult.build().toByteString());
        }
        return dataReq.build();
    }

    /**
     * Builds a complete result from the query. This one is typically used in
     * the case of success.
     *
     * @param data
     *         The data from the result.
     * @param type
     *         What the original query was.
     * @return A fully built item result.
     */
    public static Data.ItemResult buildResult(final ByteString data, final Data.ItemQuery type) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        result.setData(data);
        result.setQuery(type);
        return result.build();
    }

}
