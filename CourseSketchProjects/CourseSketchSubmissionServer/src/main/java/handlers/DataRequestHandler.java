package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import database.DatabaseAccessException;
import database.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlExperimentList;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

/**
 * Handles request for submissions.
 */
public final class DataRequestHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseClient.class);

    /**
     * Private constructor.
     */
    private DataRequestHandler() { }

    /**
     * Handles the request returning one of its own.
     *
     * @param req
     *         The object that represents the request for data.
     * @return A request, any exceptions that occur are stored in the request that is sent back.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static Request handleRequest(final Request req) {
        LOG.info("Parsing data request!");
        DataRequest dataReq;
        try {
            dataReq = DataRequest.parseFrom(req.getOtherData());
            final Request.Builder resultReq = Request.newBuilder(req);
            resultReq.clearOtherData();
            final DataResult.Builder builder = DataResult.newBuilder();
            try {
                for (ItemRequest itemReq : dataReq.getItemsList()) {
                    if (itemReq.getQuery() == ItemQuery.EXPERIMENT) {
                        if (!itemReq.hasAdvanceQuery()) {
                            builder.addResults(handleSingleExperiment(itemReq));
                        } else {
                            builder.addResults(getExperimentsForInstructor(itemReq));
                        }
                        //conn.send(resultReq.build().toByteArray());
                    }
                }
            } catch (Exception e) {
                final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                final Request.Builder build = Request.newBuilder();
                build.setRequestType(Request.MessageType.ERROR);
                build.setResponseText(e.getMessage());
                build.setSessionInfo(req.getSessionInfo());
                build.setOtherData(protoEx.toByteString());
                return build.build();
            }
            resultReq.setOtherData(builder.build().toByteString());
            resultReq.setRequestType(MessageType.DATA_REQUEST);
            return resultReq.build();
        } catch (InvalidProtocolBufferException e) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            final Request.Builder build = Request.newBuilder();
            build.setRequestType(Request.MessageType.ERROR);
            build.setResponseText(e.getMessage());
            build.setSessionInfo(req.getSessionInfo());
            build.setOtherData(protoEx.toByteString());
            return build.build();
        }
    }

    /**
     * Takes in an item request that grabs a single experiment.
     *
     * This request must contain an ID (for a student) and optionally an SRLChecksum
     *
     * @param itemReq
     *         the item request that deals with the single experiment.
     * @return An item result that represents the data.
     */
    private static ItemResult handleSingleExperiment(final ItemRequest itemReq) {
        LOG.info("attempting to get an experiment!");
        SrlExperiment experiment = null;
        String errorMessage = "";
        try {
            experiment = DatabaseClient.getExperiment(itemReq.getItemId(0), DatabaseClient.getInstance());
        } catch (DatabaseAccessException e) {
            errorMessage = e.getMessage();
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }

        final ItemResult.Builder send = ItemResult.newBuilder();
        send.setQuery(ItemQuery.EXPERIMENT);

        if (experiment != null) {
            send.setData(experiment.toByteString());
        } else {
            send.setNoData(true);
            send.setErrorMessage(errorMessage);
            //error stuff
        }
        return send.build();
    }

    /**
     * Grabs an experiment for the instructor.
     *
     * @param itemReq
     *         the object that represents the request.
     * @return All of the experiments for the instructor.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static ItemResult getExperimentsForInstructor(final ItemRequest itemReq) {
        LOG.info("attempting to get an experiment as an instructor");
        final SrlExperimentList.Builder experiments = SrlExperimentList.newBuilder();
        final StringBuilder errorMessage = new StringBuilder();
        for (String item : itemReq.getItemIdList()) {
            try {
                experiments.addExperiments(DatabaseClient.getExperiment(item, DatabaseClient.getInstance()));
            } catch (Exception e) {
                errorMessage.append('\n').append(e.getMessage());
                LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            }
        }
        final ItemResult.Builder send = ItemResult.newBuilder();
        send.setQuery(ItemQuery.EXPERIMENT);
        send.setData(experiments.build().toByteString());
        send.setErrorMessage(errorMessage.toString());
        send.setAdvanceQuery(itemReq.getAdvanceQuery());
        return send.build();
    }
}
