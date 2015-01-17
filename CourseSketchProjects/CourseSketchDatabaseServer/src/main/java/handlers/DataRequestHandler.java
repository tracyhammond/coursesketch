package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.lecturedata.Lecturedata.SrlLectureDataHolder;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlSchool;

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
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.NcssMethodCount" })
    public static void handleRequest(final Request req, final SocketSession conn, final String sessionId,
            final MultiConnectionManager internalConnections) {
        try {
            System.out.println("Receiving DATA Request...");

            final String userId = req.getServersideId();
            final DataRequest request = DataRequest.parseFrom(req.getOtherData());
            if (userId == null) {
                throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
            }
            final ArrayList<ItemResult> results = new ArrayList<ItemResult>();
            final Institution instance = MongoInstitution.getInstance();
            for (int p = 0; p < request.getItemsList().size(); p++) {
                final ItemRequest itrequest = request.getItemsList().get(p);
                try {
                    System.out.println("looking at query " + itrequest.getQuery().name());
                    switch (itrequest.getQuery()) {
                        case COURSE: {
                            final List<SrlCourse> courseLoop = instance.getCourses(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder courseSchool = SrlSchool.newBuilder();
                            courseSchool.addAllCourses(courseLoop);
                            results.add(ResultBuilder.buildResult(courseSchool.build().toByteString(), ItemQuery.COURSE));
                        }
                        break;
                        case ASSIGNMENT: {
                            final List<SrlAssignment> assignmentLoop = instance.getAssignment(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder assignmentSchool = SrlSchool.newBuilder();
                            assignmentSchool.addAllAssignments(assignmentLoop);
                            results.add(ResultBuilder.buildResult(assignmentSchool.build().toByteString(), ItemQuery.ASSIGNMENT));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final List<SrlProblem> courseProblemLoop = instance.getCourseProblem(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder problemSchool = SrlSchool.newBuilder();
                            problemSchool.addAllProblems(courseProblemLoop);
                            results.add(ResultBuilder.buildResult(problemSchool.build().toByteString(), ItemQuery.COURSE_PROBLEM));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final List<SrlBankProblem> bankProblemLoop = instance.getProblem(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder bankproblemSchool = SrlSchool.newBuilder();
                            bankproblemSchool.addAllBankProblems(bankProblemLoop);
                            results.add(ResultBuilder.buildResult(bankproblemSchool.build().toByteString(), ItemQuery.BANK_PROBLEM));
                        }
                        break;
                        case COURSE_SEARCH: {
                            final List<SrlCourse> courseLoop = instance.getAllPublicCourses();
                            System.out.println("Searching all public courses: " + courseLoop);
                            final SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
                            courseSearch.addAllCourses(courseLoop);
                            results.add(ResultBuilder.buildResult(courseSearch.build().toByteString(), ItemQuery.COURSE_SEARCH));
                        }
                        break;
                        case SCHOOL: {
                            final List<SrlCourse> courseLoop = instance.getUserCourses(userId);
                            final SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
                            courseSearch.addAllCourses(courseLoop);
                            if (courseLoop.size() <= 0) {
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
                            if (!itrequest.hasAdvanceQuery()) {
                                for (String itemId : itrequest.getItemIdList()) {
                                    System.out.println("Trying to retrieve an experiemnt from a user!");
                                    try {
                                        instance.getExperimentAsUser(userId, itemId, req.getSessionInfo() + "+" + sessionId, internalConnections);

                                        results.add(ResultBuilder.buildResult(null, NO_OP_MESSAGE, ItemQuery.NO_OP));
                                    } catch (DatabaseAccessException e) {
                                        results.add(ResultBuilder.buildResult(null, e.getLocalizedMessage(), ItemQuery.EXPERIMENT));
                                        break;
                                    }
                                }
                            } else {
                                for (String itemId : itrequest.getItemIdList()) {
                                    instance.getExperimentAsInstructor(userId, itemId, req.getSessionInfo() + "+" + sessionId, internalConnections,
                                            itrequest.getAdvanceQuery());
                                }
                                results.add(ResultBuilder.buildResult(null, NO_OP_MESSAGE, ItemQuery.NO_OP));
                            }
                        }
                        break;
                        case UPDATE: {
                            long lastRequestTime = 0;
                            if (itrequest.getItemIdCount() > 0) {
                                lastRequestTime = Long.parseLong(itrequest.getItemId(0));
                            }
                            System.out.println("Last request time! " + lastRequestTime);
                            // for now get all updates!
                            final SrlSchool updates = UserClient.mongoGetReleventUpdates(userId, lastRequestTime);
                            results.add(ResultBuilder.buildResult(updates.toByteString(), ItemQuery.UPDATE));
                        }
                        break;
                        case LECTURE: {
                            final List<Lecture> lectureLoop = instance.getLecture(itrequest.getItemIdList(), userId);
                            final SrlLectureDataHolder.Builder lectureBuilder = SrlLectureDataHolder.newBuilder();
                            lectureBuilder.addAllLectures(lectureLoop);
                            results.add(ResultBuilder.buildResult(lectureBuilder.build().toByteString(), ItemQuery.LECTURE));
                        }
                        break;
                        case LECTURESLIDE: {
                            final List<LectureSlide> lectureSlideLoop = instance.getLectureSlide(itrequest.getItemIdList(), userId);
                            final SrlLectureDataHolder.Builder lectureBuilder = SrlLectureDataHolder.newBuilder();
                            lectureBuilder.addAllSlides(lectureSlideLoop);
                            results.add(ResultBuilder.buildResult(lectureBuilder.build().toByteString(), ItemQuery.LECTURESLIDE));
                        }
                        break;
                        default:
                            break;
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder build = ItemResult.newBuilder();
                        build.setQuery(itrequest.getQuery());
                        results.add(ResultBuilder.buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                    } else {
                        e.printStackTrace();
                        throw e;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final ItemResult.Builder build = ItemResult.newBuilder();
                    build.setQuery(itrequest.getQuery());
                    build.setData(itrequest.toByteString());
                    results.add(ResultBuilder.buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                }
            }
            conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
        } catch (AuthenticationException e) {
            e.printStackTrace();
            conn.send(ResultBuilder.buildRequest(null, "user was not authenticated to access data " + e.getMessage(), req));
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            e.printStackTrace();
            conn.send(ResultBuilder.buildRequest(null, e.getMessage(), req));
        }
    }
}
