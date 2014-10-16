package handlers;

import java.util.ArrayList;
import java.util.List;

import multiconnection.GeneralConnectionServer;
import multiconnection.MultiConnectionManager;

import org.eclipse.jetty.websocket.api.Session;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlSchool;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;

/**
 * Handles all request for data.
 *
 * Typcially an Id is given of that data requested it is then sent back to the client that requested it.
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
public final class DataRequestHandler {

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
     * @param req The request that has data being inserted.
     * @param conn The connection where the result is sent to.
     * @param sessionId the id of this particular session which is used if another server is talked to.
     * @param internalConnections Connections to other servers that can be used to grab data from them.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.NcssMethodCount" })
    public static void handleRequest(final Request req, final Session conn, final String sessionId,
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
                            results.add(buildResult(courseSchool.build().toByteString(), ItemQuery.COURSE));
                        }
                        break;
                        case ASSIGNMENT: {
                            final List<SrlAssignment> assignmentLoop = instance.getAssignment(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder assignmentSchool = SrlSchool.newBuilder();
                            assignmentSchool.addAllAssignments(assignmentLoop);
                            results.add(buildResult(assignmentSchool.build().toByteString(), ItemQuery.ASSIGNMENT));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final List<SrlProblem> courseProblemLoop = instance.getCourseProblem(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder problemSchool = SrlSchool.newBuilder();
                            problemSchool.addAllProblems(courseProblemLoop);
                            results.add(buildResult(problemSchool.build().toByteString(), ItemQuery.COURSE_PROBLEM));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final List<SrlBankProblem> bankProblemLoop = instance.getProblem(itrequest.getItemIdList(), userId);
                            final SrlSchool.Builder bankproblemSchool = SrlSchool.newBuilder();
                            bankproblemSchool.addAllBankProblems(bankProblemLoop);
                            results.add(buildResult(bankproblemSchool.build().toByteString(), ItemQuery.BANK_PROBLEM));
                        }
                        break;
                        case COURSE_SEARCH: {
                            final List<SrlCourse> courseLoop = instance.getAllPublicCourses();
                            System.out.println("Searching all public courses: " + courseLoop);
                            final SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
                            courseSearch.addAllCourses(courseLoop);
                            results.add(buildResult(courseSearch.build().toByteString(), ItemQuery.COURSE_SEARCH));
                        }
                        break;
                        case SCHOOL: {
                            final ArrayList<SrlCourse> courseLoop = instance.getUserCourses(userId);
                            final SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
                            courseSearch.addAllCourses(courseLoop);
                            if (courseLoop.size() <= 0) {
                                results.add(buildResult(courseSearch.build().toByteString(), NO_COURSE_MESSAGE, ItemQuery.SCHOOL));
                            } else {
                                results.add(buildResult(courseSearch.build().toByteString(), ItemQuery.SCHOOL));
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
                                    } catch (Exception e) {
                                        results.add(buildResult(null, e.getLocalizedMessage(), ItemQuery.EXPERIMENT));
                                        break;
                                    }
                                }
                            } else {
                                for (String itemId : itrequest.getItemIdList()) {
                                    instance.getExperimentAsInstructor(userId, itemId, req.getSessionInfo() + "+" + sessionId, internalConnections,
                                            itrequest.getAdvanceQuery());
                                }
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
                            results.add(buildResult(updates.toByteString(), ItemQuery.UPDATE));
                        }
                        break;
                        default:
                        break;
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder build = ItemResult.newBuilder();
                        build.setQuery(itrequest.getQuery());
                        results.add(buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                    } else {
                        e.printStackTrace();
                        throw e;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final ItemResult.Builder build = ItemResult.newBuilder();
                    build.setQuery(itrequest.getQuery());
                    build.setData(itrequest.toByteString());
                    results.add(buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                }
            }
            GeneralConnectionServer.send(conn, buildRequest(results, SUCCESS_MESSAGE, req));
        } catch (AuthenticationException e) {
            e.printStackTrace();
            GeneralConnectionServer.send(conn, buildRequest(null, "user was not authenticated to access data " + e.getMessage(), req));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            GeneralConnectionServer.send(conn, buildRequest(null, e.getMessage(), req));
        } catch (Exception e) {
            e.printStackTrace();
            GeneralConnectionServer.send(conn, buildRequest(null, e.getMessage(), req));
        }
    }

    /**
     * Builds a complete result from the query. This one is typically used in
     * the case of success.
     *
     * @param data
     *            The data from the result.
     * @param type
     *            What the original query was.
     * @return A fully built item result.
     */
    private static ItemResult buildResult(final ByteString data, final ItemQuery type) {
        final ItemResult.Builder result = ItemResult.newBuilder();
        result.setData(data);
        result.setQuery(type);
        return result.build();
    }

    /**
     * Builds a complete result from the query. This one is typically used in
     * the case of success.
     *
     * @param data
     *            The data from the result.
     * @param text
     *            A message from the result (typically used if there is an error
     *            but no data).
     * @param type
     *            What the original query was.
     * @return A fully built item result.
     */
    private static ItemResult buildResult(final ByteString data, final String text, final ItemQuery type) {
        final ItemResult.Builder result = ItemResult.newBuilder();
        result.setData(data);
        result.setQuery(type);
        result.setReturnText(text);
        return result.build();
    }

    /**
     * Builds a request from a list of {@link ItemResult}.
     * @param results A list of results that need to be sent back to the user.
     * @param message A message that goes with the results (could be an error)
     * @param req The original request that was received.
     * @return A {@link Request}.
     */
    private static Request buildRequest(final List<ItemResult> results, final String message, final Request req) {

        final DataResult.Builder dataResult = DataResult.newBuilder();
        if (results != null && !results.isEmpty()) {
            dataResult.addAllResults(results);
        }

        final Request.Builder dataReq = Request.newBuilder();
        dataReq.setRequestType(MessageType.DATA_REQUEST);
        dataReq.setSessionInfo(req.getSessionInfo());
        dataReq.setResponseText(message);

        dataReq.setOtherData(dataResult.build().toByteString());
        return dataReq.build();
    }
}
