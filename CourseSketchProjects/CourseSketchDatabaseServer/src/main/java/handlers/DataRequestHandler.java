package handlers;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.user.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.query.Data;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;
import utilities.ProtobufUtilities;

import java.util.ArrayList;
import java.util.List;

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
     * A message returned when getting the data was successful.
     */
    private static final String NO_OP_MESSAGE = "NO DATA TO RETURN";

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
     * @param instance The database backer.
     * @param sessionId
*         the id of this particular session which is used if another server is talked to.
     * @param internalConnections Connections to other clients.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.NPathComplexity", "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.NcssMethodCount",
            "checkstyle:methodlength" })
    public static void handleRequest(final Request req, final SocketSession conn, final Institution instance,
            final String sessionId,
            final MultiConnectionManager internalConnections) {
        try {
            LOG.info("Receiving DATA Request...");
            final String userId = req.getServersideId();
            final DataRequest request = DataRequest.parseFrom(req.getOtherData());
            if (userId == null) {
                throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
            }
            final ArrayList<ItemResult> results = new ArrayList<ItemResult>();
            for (int p = 0; p < request.getItemsList().size(); p++) {
                final ItemRequest itemRequest = request.getItemsList().get(p);
                try {
                    LOG.info("looking at query {}", itemRequest.getQuery().name());
                    switch (itemRequest.getQuery()) {
                        case COURSE: {
                            final List<SrlCourse> courseLoop = instance.getCourses(itemRequest.getItemIdList(), userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.COURSE, courseLoop));
                        }
                        break;
                        case ASSIGNMENT: {
                            final List<SrlAssignment> assignmentLoop = instance.getAssignment(itemRequest.getItemIdList(), userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.ASSIGNMENT, assignmentLoop));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final List<SrlProblem> courseProblemLoop = instance.getCourseProblem(itemRequest.getItemIdList(), userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.COURSE_PROBLEM, courseProblemLoop));
                        }
                        break;
                        case BANK_PROBLEM: {
                            List<SrlBankProblem> bankProblemLoop = null;
                            if (!itemRequest.hasPage()) {
                                bankProblemLoop = instance.getProblem(itemRequest.getItemIdList(), userId);
                            } else {
                                final int page = itemRequest.getPage();
                                // The first id in the item is the course id.
                                bankProblemLoop = instance.getAllBankProblems(userId, itemRequest.getItemId(0), page);
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
                            final List<SrlCourse> courseLoop = instance.getUserCourses(userId);
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
                                for (String itemId : itemRequest.getItemIdList()) {
                                    LOG.info("Trying to retrieve an experiment from a user!");
                                    try {
                                        final Request.Builder build = ProtobufUtilities.createBaseResponse(req);
                                        build.setSessionInfo(req.getSessionInfo() + "+" + sessionId);
                                        instance.getExperimentAsUser(userId, itemId, build.build(), internalConnections);
                                        results.add(ResultBuilder.buildResult(NO_OP_MESSAGE, ItemQuery.NO_OP, (GeneratedMessage[]) null));
                                    } catch (DatabaseAccessException e) {
                                        final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                                        conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
                                        results.add(ResultBuilder.buildResult(e.getLocalizedMessage(), ItemQuery.EXPERIMENT,
                                                (GeneratedMessage[]) null));
                                        break;
                                    }
                                }
                            } else {
                                final Request.Builder build = ProtobufUtilities.createBaseResponse(req);
                                build.setSessionInfo(req.getSessionInfo() + "+" + sessionId);
                                final Request baseRequest = build.build();
                                for (String itemId : itemRequest.getItemIdList()) {

                                    instance.getExperimentAsInstructor(userId, itemId, baseRequest, internalConnections,
                                            itemRequest.getAdvanceQuery());
                                }
                                results.add(ResultBuilder.buildResult(NO_OP_MESSAGE, ItemQuery.NO_OP, (GeneratedMessage[]) null));
                            }
                        }
                        break;
                        case UPDATE: {
                            long lastRequestTime = 0;
                            if (itemRequest.getItemIdCount() > 0) {
                                lastRequestTime = Long.parseLong(itemRequest.getItemId(0));
                            }
                            LOG.info("Last request time! {}", lastRequestTime);
                            // for now get all updates!
                            final List<Data.ItemResult> updates = UserClient.mongoGetReleventUpdates(userId, lastRequestTime);
                            results.addAll(updates);
                        }
                        break;
                        case LECTURE: {
                            final List<Lecture> lectureLoop = instance.getLecture(itemRequest.getItemIdList(), userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.LECTURE, lectureLoop));
                        }
                        break;
                        case LECTURESLIDE: {
                            final List<LectureSlide> lectureSlideLoop = instance.getLectureSlide(itemRequest.getItemIdList(), userId);
                            results.add(ResultBuilder.buildResult(ItemQuery.LECTURESLIDE, lectureSlideLoop));
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
                    LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                    LOG.error("Exception with item {}", itemRequest);
                    throw e;
                }
            }
            conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
        } catch (AuthenticationException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }  catch (DatabaseAccessException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(req, protoEx));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
