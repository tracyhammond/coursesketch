package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.institution.Institution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.Problem.LectureSlide;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.Assignment;
import protobuf.srl.school.Problem;
import protobuf.srl.school.School;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

import java.util.ArrayList;

import static handlers.ResultBuilder.validateIds;

/**
 * Handles data being added or edited.
 *
 * In most cases insert returns the mongoId and the id that was taken in. This
 * allows the client to replace the old assignment id with the new assignment
 * id.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity" })
public final class DataUpdateHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataUpdateHandler.class);

    /**
     * A message returned when the insert was successful.
     */
    private static final String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";

    /**
     * Private constructor.
     */
    private DataUpdateHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link coursesketch.database.institution.Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param instance
     *         The object that interfaces with the database and handles specific requests.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "checkstyle:avoidnestedblocks" })
    public static void handleData(final Request req, final SocketSession conn, final Institution instance) {
        try {
            LOG.info("Receiving DATA UPDATE Request...");

            final String authId = req.getServersideId();
            final String userId = req.getServerUserId();
            validateIds(authId, userId);

            final DataSend request = DataSend.parseFrom(req.getOtherData());

            final ArrayList<ItemResult> results = new ArrayList<>();
            for (int itemUpdateIndex = 0; itemUpdateIndex < request.getItemsList().size(); itemUpdateIndex++) {
                final ItemSend itemSet = request.getItemsList().get(itemUpdateIndex);
                try {
                    switch (itemSet.getQuery()) {
                        // TODO Enable updates for other data
                        case LECTURE: {
                            final Assignment.SrlAssignment lecture = Assignment.SrlAssignment.parseFrom(itemSet.getData());
                            instance.updateLecture(authId, lecture);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), ""));
                        }
                        break;
                        case LECTURESLIDE: {
                            final LectureSlide lectureSlide = LectureSlide.parseFrom(itemSet.getData());
                            instance.updateLectureSlide(authId, lectureSlide);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), ""));
                        }
                        break;
                        case COURSE: {
                            final School.SrlCourse course = School.SrlCourse.parseFrom(itemSet.getData());
                            instance.updateCourse(authId, course);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), ""));
                        }
                        break;
                        case ASSIGNMENT: {
                            final Assignment.SrlAssignment assignment = Assignment.SrlAssignment.parseFrom(itemSet.getData());
                            instance.updateAssignment(authId, assignment);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), ""));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final Problem.SrlProblem srlProblem = Problem.SrlProblem.parseFrom(itemSet.getData());
                            instance.updateCourseProblem(authId, srlProblem);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), ""));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final Problem.SrlBankProblem srlBankProblem = Problem.SrlBankProblem.parseFrom(itemSet.getData());
                            instance.updateBankProblem(authId, srlBankProblem);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), ""));
                        }
                        break;
                        default:
                            final ItemResult.Builder result = ItemResult.newBuilder();
                            result.setQuery(itemSet.getQuery());
                            results.add(ResultBuilder.buildResult("Update is not supported for this type", ItemQuery.ERROR,
                                    result.build()));
                            break;
                    }
                } catch (AuthenticationException e) {
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                    conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder result = ItemResult.newBuilder();
                        result.setQuery(itemSet.getQuery());
                        results.add(ResultBuilder.buildResult(e.getMessage(), ItemQuery.ERROR, result.build()));
                    } else {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                        throw e;
                    }
                } catch (Exception e) {
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                    conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                    final ItemResult.Builder result = ItemResult.newBuilder();
                    result.setQuery(itemSet.getQuery());
                    result.addData(itemSet.toByteString());
                    results.add(ResultBuilder.buildResult(e.getMessage(), ItemQuery.ERROR, result.build()));
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                }
            }
            if (!results.isEmpty()) {
                conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
            }
        } catch (AuthenticationException | DatabaseAccessException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            conn.send(ResultBuilder.buildRequest(null, e.getMessage(), req));
        }
    }
}
