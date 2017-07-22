package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import coursesketch.services.submission.SubmissionWebSocketClient;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.institution.Institution;
import coursesketch.database.user.UserClient;
import handlers.subhandlers.GradingPolicyRequestHandler;
import handlers.subhandlers.GradingRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.query.Data;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.Assignment.SrlAssignment;
import protobuf.srl.school.Problem.LectureSlide;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.school.Problem.SrlProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.services.identity.Identity;
import protobuf.srl.submission.Submission;
import coursesketch.utilities.ExceptionUtilities;
import utilities.LoggingConstants;
import utilities.ProtobufUtilities;

import java.util.ArrayList;
import java.util.List;

import static handlers.ResultBuilder.validateIds;

/**
 * Handles all request for data.
 *
 * Typcially an Id is given of that data requested it is then sent back to the client that requested it.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
public final class DataRequestHandler {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataRequestHandler.class);

    /**
     * A message returned when getting the data was successful.
     */
    private static final String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";

    /**
     * A message returned if the user does not have any classes.
     */
    private static final String NO_COURSE_MESSAGE = "You do not have any courses associated with this account";

    /**
     * Private constructor.
     */
    private DataRequestHandler() {
    }

    /**
     * Takes in a request that has to deal with requesting data.
     *
     * decode request and pull correct information from {@link Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param instance
     *         The object that interfaces with the database and handles specific requests.
     * @param sessionId
     *         The id of this particular session which is used if another server is talked to.
     * @param internalConnections
     *         Connections to other clients.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.NPathComplexity", "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.NcssMethodCount",
            "checkstyle:methodlength", "checkstyle:avoidnestedblocks" })
    public static void handleRequest(final Request req, final SocketSession conn, final Institution instance,
            final String sessionId,
            final MultiConnectionManager internalConnections) {
        try {
            LOG.info("Receiving DATA Request...");
            final String authId = req.getServersideId();
            final String userId = req.getServerUserId();
            final DataRequest request = DataRequest.parseFrom(req.getOtherData());
            validateIds(authId, userId);
            final ArrayList<ItemResult> results = new ArrayList<>();
            for (int itemRequestIndex = 0; itemRequestIndex < request.getItemsList().size(); itemRequestIndex++) {
                final ItemRequest itemRequest = request.getItemsList().get(itemRequestIndex);
                try {
                    LOG.info("looking at query {}", itemRequest.getQuery().name());
                    switch (itemRequest.getQuery()) {
                        case COURSE: {
                            final List<SrlCourse> courseLoop = instance.getCourses(authId, itemRequest.getItemIdList());
                            results.add(ResultBuilder.buildResult(ItemQuery.COURSE, courseLoop));
                        }
                        break;
                        case ASSIGNMENT: {
                            final List<SrlAssignment> assignmentLoop = instance.getAssignment(authId, itemRequest.getItemIdList());
                            results.add(ResultBuilder.buildResult(ItemQuery.ASSIGNMENT, assignmentLoop));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final List<SrlProblem> courseProblemLoop = instance.getCourseProblem(authId, itemRequest.getItemIdList());
                            results.add(ResultBuilder.buildResult(ItemQuery.COURSE_PROBLEM, courseProblemLoop));
                        }
                        break;
                        case BANK_PROBLEM: {
                            List<SrlBankProblem> bankProblemLoop;
                            if (!itemRequest.hasPage()) {
                                bankProblemLoop = instance.getProblem(authId, itemRequest.getItemIdList());
                            } else {
                                final int page = itemRequest.getPage();
                                // The first id in the item is the course id.
                                bankProblemLoop = instance.getAllBankProblems(authId, itemRequest.getItemId(0), page);
                            }
                            results.add(ResultBuilder.buildResult(ItemQuery.BANK_PROBLEM, bankProblemLoop));
                        }
                        break;
                        case COURSE_SEARCH: {
                            final List<SrlCourse> courseLoop = instance.getAllPublicCourses();
                            LOG.info("Searching all public courses: {}", courseLoop);
                            results.add(ResultBuilder.buildResult(ItemQuery.COURSE_SEARCH, courseLoop));
                        }
                        break;
                        case SCHOOL: {
                            final List<SrlCourse> courseLoop = instance.getUserCourses(authId);
                            if (courseLoop.isEmpty()) {
                                results.add(ResultBuilder.buildResult(NO_COURSE_MESSAGE, ItemQuery.SCHOOL, courseLoop));
                            } else {
                                results.add(ResultBuilder.buildResult(ItemQuery.SCHOOL, courseLoop));
                            }
                        }
                        break;
                        case EXPERIMENT: {
                            // need to get the submission ID?
                            // we send it the CourseProblemId and the userId and we get the submission Id
                            // MongoInstitution.mongoGetExperiment(assignementID, userId)
                            if (!itemRequest.hasAdvanceQuery()) {
                                final ProtocolStringList itemIdList = itemRequest.getItemIdList();
                                LOG.info("Trying to retrieve an experiment from a user!");
                                final Request.Builder build = ProtobufUtilities.createBaseResponse(req);
                                build.setSessionInfo(req.getSessionInfo() + "+" + sessionId);
                                final Submission.SrlExperiment experiment = instance.getExperimentAsUser(userId, authId, itemIdList,
                                        internalConnections.getBestConnection(SubmissionWebSocketClient.class));
                                results.add(ResultBuilder.buildResult(ItemQuery.EXPERIMENT, experiment));
                            } else {
                                final Request.Builder build = ProtobufUtilities.createBaseResponse(req);
                                build.setSessionInfo(req.getSessionInfo() + "+" + sessionId);
                                final Request baseRequest = build.build();
                                final ProtocolStringList itemIdList = itemRequest.getItemIdList();

                                final List<Submission.SrlExperiment> experimentList =
                                        instance.getExperimentAsInstructor(authId, itemIdList, baseRequest, internalConnections,
                                                itemRequest.getAdvanceQuery());
                                for (Submission.SrlExperiment experiment : experimentList) {
                                    results.add(ResultBuilder.buildResult(ItemQuery.EXPERIMENT, experiment));
                                }
                            }
                        }
                        break;
                        case SOLUTION: {
                            final ProtocolStringList itemIdList = itemRequest.getItemIdList();
                            LOG.info("Trying to retrieve a solution from a user!");
                            final Request.Builder build = ProtobufUtilities.createBaseResponse(req);
                            build.setSessionInfo(req.getSessionInfo() + "+" + sessionId);
                            final Submission.SrlSolution experiment = instance.getSolution(userId, authId, itemIdList,
                                    internalConnections.getBestConnection(SubmissionWebSocketClient.class));
                            results.add(ResultBuilder.buildResult(ItemQuery.SOLUTION, experiment));
                        }
                        break;
                        case UPDATE: {
                            long lastRequestTime = 0;
                            if (itemRequest.getItemIdCount() > 0) {
                                lastRequestTime = Long.parseLong(itemRequest.getItemId(0));
                            }
                            LOG.info("Last request time! {}", lastRequestTime);
                            // for now get all updates!
                            final List<Data.ItemResult> updates = UserClient.mongoGetReleventUpdates(authId, lastRequestTime);
                            results.addAll(updates);
                        }
                        break;
                        case LECTURE: {
                            final List<SrlAssignment> lectureLoop = instance.getLecture(authId, itemRequest.getItemIdList());
                            results.add(ResultBuilder.buildResult(ItemQuery.LECTURE, lectureLoop));
                        }
                        break;
                        case LECTURESLIDE: {
                            final List<LectureSlide> lectureSlideLoop = instance.getLectureSlide(authId, itemRequest.getItemIdList());
                            results.add(ResultBuilder.buildResult(ItemQuery.LECTURESLIDE, lectureSlideLoop));
                        }
                        break;
                        case GRADE: {
                            final List<ProtoGrade> gradeList = GradingRequestHandler.gradingRequestHandler(instance, itemRequest, authId, userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.GRADE, gradeList));
                        }
                        break;
                        case COURSE_ROSTER: {
                            final Identity.UserNameResponse courseRoster = instance.getCourseRoster(authId, itemRequest.getItemId(0));
                            results.add(ResultBuilder.buildResult(ItemQuery.COURSE_ROSTER, courseRoster));
                        }
                        break;
                        case GRADING_POLICY: {
                            final ProtoGradingPolicy gradingPolicy = GradingPolicyRequestHandler.gradingPolicyRequestHandler(instance, itemRequest,
                                    userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.GRADING_POLICY, gradingPolicy));
                        }
                        break;
                        default:
                            break;
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        // If
                        final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                        conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                        final ItemResult.Builder result = ItemResult.newBuilder();
                        result.setQuery(itemRequest.getQuery());
                        results.add(ResultBuilder.buildResult("The item is not valid for access during the specified time range. " + e.getMessage(),
                                ItemQuery.ERROR, result.build()));
                    } else {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                        throw e;
                    }
                } catch (DatabaseAccessException e) {
                    if (e.isSendResponse()) {
                        final ItemResult.Builder result = ItemResult.newBuilder();
                        result.setQuery(itemRequest.getQuery());
                        results.add(ResultBuilder.buildResult("The item had an error" + e.getMessage(),
                                ItemQuery.ERROR, result.build()));
                    } else {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                        LOG.error("Exception with item {}", itemRequest);
                        throw e;
                    }
                }
            }
            conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
        } catch (AuthenticationException | DatabaseAccessException | InvalidProtocolBufferException | RuntimeException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
