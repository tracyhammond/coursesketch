package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.institution.Institution;
import coursesketch.database.util.user.UserClient;
import handlers.subhandlers.GradingPolicyUpsertHandler;
import handlers.subhandlers.GradingUpsertHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.school.Problem.LectureSlide;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.Assignment.SrlAssignment;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.Problem.SrlProblem;
import protobuf.srl.school.School.SrlUser;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

import java.util.ArrayList;

import static handlers.ResultBuilder.ID_SEPARATOR;
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
public final class DataInsertHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataInsertHandler.class);

    /**
     * A message returned when the insert was successful.
     */
    private static final String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";

    /**
     * Private constructor.
     */
    private DataInsertHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param instance
     *         The object that interfaces with the coursesketch.util.util and handles specific requests.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl", "checkstyle:avoidnestedblocks" })
    public static void handleData(final Request req, final SocketSession conn, final Institution instance) {
        try {
            LOG.info("Receiving DATA INSERT Request...");

            final String authId = req.getServersideId();
            final String userId = req.getServerUserId();
            final DataSend request = DataSend.parseFrom(req.getOtherData());
            validateIds(authId, userId);
            final ArrayList<ItemResult> results = new ArrayList<>();

            for (int itemInsertIndex = 0; itemInsertIndex < request.getItemsList().size(); itemInsertIndex++) {
                final ItemSend itemSet = request.getItemsList().get(itemInsertIndex);
                try {
                    switch (itemSet.getQuery()) {
                        case COURSE: {
                            final SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
                            final String resultId = instance.insertCourse(userId, authId, course);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + course.getId()));
                        }
                        break;
                        case ASSIGNMENT: {
                            final SrlAssignment assignment = SrlAssignment.parseFrom(itemSet.getData());
                            final String resultId = instance.insertAssignment(userId, authId, assignment);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + assignment.getId()));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final SrlProblem problem = SrlProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertCourseProblem(userId, authId, problem);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + problem.getId()));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final SrlBankProblem problem = SrlBankProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertBankProblem(userId, authId, problem);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + problem.getId()));
                        }
                        break;
                        case USER_INFO: {
                            UserClient.insertUser(SrlUser.parseFrom(itemSet.getData()), authId);
                        }
                        break;
                        case REGISTER: {
                            final SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
                            final String courseId = course.getId();
                            final boolean success = instance.putUserInCourse(userId, authId, courseId, course.getRegistrationKey());
                            if (!success) {
                                throw new DatabaseAccessException("User was already registered for course!");
                            } else {
                                results.add(ResultBuilder.buildResult(itemSet.getQuery(), SUCCESS_MESSAGE));
                            }
                        }
                        break;
                        case LECTURE: {
                            final SrlAssignment lecture = SrlAssignment.parseFrom(itemSet.getData());
                            final String resultId = instance.insertLecture(userId, authId, lecture);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + lecture.getId()));
                        }
                        break;
                        case LECTURESLIDE: {
                            final LectureSlide lectureSlide = LectureSlide.parseFrom(itemSet.getData());
                            final String resultId = instance.insertLectureSlide(authId, lectureSlide);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + lectureSlide.getId()));
                        }
                        break;
                        case GRADE: {
                            GradingUpsertHandler.gradingUpsertHandler(instance, itemSet, authId, req.getMessageTime());
                        }
                        break;
                        case GRADING_POLICY: {
                            GradingPolicyUpsertHandler.gradingPolicyUpsertHandler(instance, itemSet, authId);
                        }
                        break;
                        default:
                            throw new Exception("Insert type not supported.");
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder itemResult = ItemResult.newBuilder();
                        itemResult.setQuery(itemSet.getQuery());
                        results.add(ResultBuilder.buildResult(e.getMessage(), itemSet.getQuery(), itemResult.build()));
                    } else {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                        throw e;
                    }
                } catch (DatabaseAccessException e) {
                    if (e.isRecoverable()) {
                        final ItemResult.Builder itemResult = ItemResult.newBuilder();
                        itemResult.setQuery(itemSet.getQuery());
                        results.add(ResultBuilder.buildResult(e.getMessage(), itemSet.getQuery(), itemResult.build()));
                    } else {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                        throw e;
                    }
                } catch (Exception e) {
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                    conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                    final ItemResult.Builder itemResult = ItemResult.newBuilder();
                    itemResult.setQuery(itemSet.getQuery());
                    itemResult.addData(itemSet.toByteString());
                    results.add(ResultBuilder.buildResult(e.getMessage(), ItemQuery.ERROR, itemResult.build()));
                }
            }
            if (!results.isEmpty()) {
                conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
            }
        } catch (AuthenticationException | DatabaseAccessException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx,
                    "user was not authenticated or had a coursesketch.util.util error " + protoEx.getMssg()));
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx,
                    "A exception occurred while inserting data" + protoEx.getMssg()));
        }
    }
}
