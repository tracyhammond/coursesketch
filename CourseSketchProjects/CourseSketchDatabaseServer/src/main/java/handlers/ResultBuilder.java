package handlers;

import com.google.protobuf.GeneratedMessage;
import protobuf.srl.query.Data;
import protobuf.srl.request.Message;
import utilities.ProtobufUtilities;

import java.util.List;

/**
 * Created by gigemjt on 1/2/15.
 */
public final class ResultBuilder {

    /**
     * Utility class.
     */
    private ResultBuilder() { }

    /**
     * Builds a complete result from the query.
     *
     * This one is typically used in the case of success.
     * This uses varags to take in multiple messages.
     *
     * @param text
     *         A message from the result (typically used if there is an error
     *         but no data).
     * @param type
     *         What the original query was.
     * @param data
     *         The data from the result.
     * @return A fully built item result.
     */
    public static Data.ItemResult buildResult(final String text, final Data.ItemQuery type, final GeneratedMessage... data) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        if (data != null) {
            for (GeneratedMessage message : data) {
                result.addData(message.toByteString());
            }
        }
        result.setQuery(type);
        if (text != null) {
            result.setReturnText(text);
        }
        return result.build();
    }

    /**
     * Builds a complete result from the query.
     *
     * This one is typically used in the case of success.
     * This takes in a list.
     *
     * @param text
     *         A message from the result (typically used if there is an error
     *         but no data).
     * @param type
     *         What the original query was.
     * @param data
     *         The data from the result.
     * @return A fully built item result.
     */
    public static Data.ItemResult buildResult(final String text, final Data.ItemQuery type, final List<? extends GeneratedMessage> data) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        if (data != null) {
            for (GeneratedMessage message : data) {
                result.addData(message.toByteString());
            }
        }
        result.setQuery(type);
        if (text != null) {
            result.setReturnText(text);
        }
        return result.build();
    }

    /**
     * Builds a result but with no binary data.
     *
     * @param type
     *         What the original query was.
     * @param data
     *         The data from the result.
     * @return A built item result with no binary data.
     */
    public static Data.ItemResult buildResult(final Data.ItemQuery type, final String data) {
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

        final Message.Request.Builder dataReq = ProtobufUtilities.createBaseResponse(req);
        dataReq.setResponseText(message);
        if (dataResult != null) {
            dataReq.setOtherData(dataResult.build().toByteString());
        }
        return dataReq.build();
    }

    /**
     * Builds a complete result from the query.
     *
     * This one is typically used in the case of success.
     * This uses varags to take in multiple messages.
     *
     * @param type
     *         What the original query was.
     * @param data
     *         The data from the result.
     * @return A fully built item result.
     */
    public static Data.ItemResult buildResult(final Data.ItemQuery type, final GeneratedMessage... data) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        if (data != null) {
            for (GeneratedMessage message : data) {
                result.addData(message.toByteString());
            }
        }
        result.setQuery(type);
        return result.build();
    }

    /**
     * Builds a complete result from the query.
     *
     * This one is typically used in the case of success.
     * This takes in a list.
     *
     * @param type
     *         What the original query was.
     * @param data
     *         The data from the result.
     * @return A fully built item result.
     */
    public static Data.ItemResult buildResult(final Data.ItemQuery type, final List<? extends GeneratedMessage> data) {
        final Data.ItemResult.Builder result = Data.ItemResult.newBuilder();
        if (data != null) {
            for (GeneratedMessage message : data) {
                result.addData(message.toByteString());
            }
        }
        result.setQuery(type);
        return result.build();
    }
}
