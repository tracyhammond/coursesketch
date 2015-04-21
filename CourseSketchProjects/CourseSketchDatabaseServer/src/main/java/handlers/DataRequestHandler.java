package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.lecturedata.Lecturedata.SrlLectureDataHolder;
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
import protobuf.srl.school.School.SrlSchool;
import protobuf.srl.tutorial.TutorialOuterClass;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

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
     *
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     * @param sessionId
     *         the id of this particular session which is used if another server is talked to.
     * @param internalConnections
     *         Connections to other servers that can be used to grab data from them.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.NPathComplexity", "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.NcssMethodCount",
            "checkstyle:methodlength" })
    public static void handleRequest(final Request req, final SocketSession conn, final String sessionId,
            final MultiConnectionManager internalConnections) {
        try {
            LOG.info("Receiving DATA Request...");
            final String userId = req.getServersideId();
            final DataRequest request = DataRequest.parseFrom(req.getOtherData());
            if (userId == null) {
                throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
            }
            final ArrayList<ItemResult> results = new ArrayList<ItemResult>();
            final Institution instance = MongoInstitution.getInstance();
            for (int p = 0; p < request.getItemsList().size(); p++) {
                final ItemRequest itemRequest = request.getItemsList().get(p);
                try {
                    LOG.info("looking at query {}", itemRequest.getQuery().name());
                    switch (itemRequest.getQuery()) {
                        case COURSE: {
                            final List<SrlCourse> courseLoop = instance.getCourses(itemRequest.getItemIdList(), userId);
                            final SrlSchool.Builder courseSchool = SrlSchool.newBuilder();
                            courseSchool.addAllCourses(courseLoop);
                            results.add(ResultBuilder.buildResult(courseSchool.build().toByteString(), ItemQuery.COURSE));
                        }
                        break;
                        case ASSIGNMENT: {
                            final List<SrlAssignment> assignmentLoop = instance.getAssignment(itemRequest.getItemIdList(), userId);
                            final SrlSchool.Builder assignmentSchool = SrlSchool.newBuilder();
                            assignmentSchool.addAllAssignments(assignmentLoop);
                            results.add(ResultBuilder.buildResult(assignmentSchool.build().toByteString(), ItemQuery.ASSIGNMENT));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final List<SrlProblem> courseProblemLoop = instance.getCourseProblem(itemRequest.getItemIdList(), userId);
                            final SrlSchool.Builder problemSchool = SrlSchool.newBuilder();
                            problemSchool.addAllProblems(courseProblemLoop);
                            results.add(ResultBuilder.buildResult(problemSchool.build().toByteString(), ItemQuery.COURSE_PROBLEM));
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
                            final SrlSchool.Builder bankProblemSchool = SrlSchool.newBuilder();
                            bankProblemSchool.addAllBankProblems(bankProblemLoop);
                            results.add(ResultBuilder.buildResult(bankProblemSchool.build().toByteString(), ItemQuery.BANK_PROBLEM));
                        }
                        break;
                        case COURSE_SEARCH: {
                            final List<SrlCourse> courseLoop = instance.getAllPublicCourses();
                            LOG.info("Searching all public courses: {}", courseLoop);
                            final SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
                            courseSearch.addAllCourses(courseLoop);
                            results.add(ResultBuilder.buildResult(courseSearch.build().toByteString(), ItemQuery.COURSE_SEARCH));
                        }
                        break;
                        case SCHOOL: {
                            final List<SrlCourse> courseLoop = instance.getUserCourses(userId);
                            final SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
                            courseSearch.addAllCourses(courseLoop);
                            if (courseLoop.isEmpty()) {
                                results.add(ResultBuilder.buildResult(courseSearch.build().toByteString(), NO_COURSE_MESSAGE, ItemQuery.SCHOOL));
                            } else {
                                results.add(ResultBuilder.buildResult(courseSearch.build().toByteString(), ItemQuery.SCHOOL));
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
                                        instance.getExperimentAsUser(userId, itemId, req.getSessionInfo() + "+" + sessionId, internalConnections);
                                        results.add(ResultBuilder.buildResult(null, NO_OP_MESSAGE, ItemQuery.NO_OP));
                                    } catch (DatabaseAccessException e) {
                                        final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                                        conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
                                        results.add(ResultBuilder.buildResult(null, e.getLocalizedMessage(), ItemQuery.EXPERIMENT));
                                        break;
                                    }
                                }
                            } else {
                                for (String itemId : itemRequest.getItemIdList()) {
                                    instance.getExperimentAsInstructor(userId, itemId, req.getSessionInfo() + "+" + sessionId, internalConnections,
                                            itemRequest.getAdvanceQuery());
                                }
                                results.add(ResultBuilder.buildResult(null, NO_OP_MESSAGE, ItemQuery.NO_OP));
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
                            final SrlSchool updates = UserClient.mongoGetReleventUpdates(userId, lastRequestTime);
                            results.add(ResultBuilder.buildResult(updates.toByteString(), ItemQuery.UPDATE));
                        }
                        break;
                        case LECTURE: {
                            final List<Lecture> lectureLoop = instance.getLecture(itemRequest.getItemIdList(), userId);
                            final SrlLectureDataHolder.Builder lectureBuilder = SrlLectureDataHolder.newBuilder();
                            lectureBuilder.addAllLectures(lectureLoop);
                            results.add(ResultBuilder.buildResult(lectureBuilder.build().toByteString(), ItemQuery.LECTURE));
                        }
                        break;
                        case LECTURESLIDE: {
                            final List<LectureSlide> lectureSlideLoop = instance.getLectureSlide(itemRequest.getItemIdList(), userId);
                            final SrlLectureDataHolder.Builder lectureBuilder = SrlLectureDataHolder.newBuilder();
                            lectureBuilder.addAllSlides(lectureSlideLoop);
                            results.add(ResultBuilder.buildResult(lectureBuilder.build().toByteString(), ItemQuery.LECTURESLIDE));
                        }
                        break;
                        case TUTORIAL: {
                        }
                        default:
                            break;
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        // If
                        final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                        conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
                        final ItemResult.Builder build = ItemResult.newBuilder();
                        build.setQuery(itemRequest.getQuery());
                        results.add(ResultBuilder.buildResult(build.build().toByteString(),
                                "The item is not valid for access during the specified time range. " + e.getMessage(), ItemQuery.ERROR));
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
            conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }  catch (DatabaseAccessException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
        }
    }
}
