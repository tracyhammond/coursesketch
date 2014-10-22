package handlers;

import java.util.ArrayList;
import java.util.List;

import multiconnection.GeneralConnectionServer;

import org.eclipse.jetty.websocket.api.Session;

import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlUser;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;

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
     * The string used to separate ids when returning a result.
     */
    private static final String ID_SEPARATOR = " : ";

    /**
     * A message returned when the insert was successful.
     */
    private static final String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";

    /**
     * Private constructor.
     *
     */
    private DataInsertHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     * @param req The request that has data being inserted.
     * @param conn The connection where the result is sent to.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException" })
    public static void handleData(final Request req, final Session conn) {
        try {
            System.out.println("Receiving DATA SEND Request...");

            final String userId = req.getServersideId();
            final DataSend request = DataSend.parseFrom(req.getOtherData());
            if (userId == null || userId.equals("")) {
                throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
            }
            final ArrayList<ItemResult> results = new ArrayList<ItemResult>();

            final Institution instance = MongoInstitution.getInstance();
            for (int p = 0; p < request.getItemsList().size(); p++) {
                final ItemSend itemSet = request.getItemsList().get(p);
                try {
                    switch (itemSet.getQuery()) {
                        case COURSE: {
                            try {
                                final SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
                                final String resultId = instance.insertCourse(userId, course);
                                results.add(buildResult(resultId + ID_SEPARATOR + course.getId(), itemSet.getQuery()));
                            } catch (DatabaseAccessException e) {
                                // unable to register user for course
                                final ItemResult.Builder build = ItemResult.newBuilder();
                                build.setQuery(itemSet.getQuery());
                                results.add(buildResult(build.build().toByteString(), "Unable to register user for course: " + e.getMessage(),
                                        ItemQuery.ERROR));
                            }
                        }
                        break;
                        case ASSIGNMENT: {
                            final SrlAssignment assignment = SrlAssignment.parseFrom(itemSet.getData());
                            final String resultId = instance.insertAssignment(userId, assignment);
                            results.add(buildResult(resultId + ID_SEPARATOR + assignment.getId(), itemSet.getQuery()));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final SrlProblem problem = SrlProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertCourseProblem(userId, problem);
                            results.add(buildResult(resultId + ID_SEPARATOR + problem.getId(), itemSet.getQuery()));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final SrlBankProblem problem = SrlBankProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertBankProblem(userId, problem);
                            results.add(buildResult(resultId + ID_SEPARATOR + problem.getId(), itemSet.getQuery()));
                        }
                        break;
                        /*
                         * case CLASS_GRADE: { SrlGrade grade =
                         * SrlGrade.parseFrom(itemSet.getData()); String
                         * resultId = MongoInstitution.mongoInsertClassGrade(userId,
                         * grade); results.add(buildResult(resultId + " : " +
                         * grade.getId(), itemSet.getQuery())); } break;
                         */
                        case USER_INFO: {
                            UserClient.insertUser(SrlUser.parseFrom(itemSet.getData()), userId);
                        }
                        break;
                        case REGISTER: {
                            final SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
                            final String courseId = course.getId();
                            final boolean success = instance.putUserInCourse(courseId, userId);
                            if (!success) {
                                results.add(buildResult("User was already registered for course!", itemSet.getQuery()));
                            }
                        }
                        break;
                    /*
                     * case USERGROUP: ArrayList<UserGroupBuilder>
                     * assignmentLoop =
                     * MongoInstitution.mongoGetAssignment((ArrayList
                     * )itrequest.getItemIdList(), request.getUserId());
                     * for(AssignmentBuilder loopCourse: assignmentLoop){
                     * finalSchool
                     * .addAssignments(RequestConverter.convertAssignmentToProtobuf
                     * (loopCourse)); } break; case CLASS_GRADE:
                     * ArrayList<AssignmentBuilder> assignmentLoop =
                     * MongoInstitution.
                     * mongoGetAssignment((ArrayList)itrequest.getItemIdList(),
                     * request.getUserId()); for(AssignmentBuilder loopCourse:
                     * assignmentLoop){
                     * finalSchool.addAssignments(RequestConverter
                     * .convertAssignmentToProtobuf(loopCourse)); } break; case
                     * SOLUTION: ArrayList<AssignmentBuilder> assignmentLoop =
                     * MongoInstitution
                     * .mongoGetAssignment((ArrayList)itrequest.getItemIdList(),
                     * request.getUserId()); for(AssignmentBuilder loopCourse:
                     * assignmentLoop){
                     * finalSchool.addAssignments(RequestConverter
                     * .convertAssignmentToProtobuf(loopCourse)); } break; case
                     * EXPERIMENT: ArrayList<AssignmentBuilder> assignmentLoop =
                     * MongoInstitution
                     * .mongoGetAssignment((ArrayList)itrequest.getItemIdList(),
                     * request.getUserId()); for(AssignmentBuilder loopCourse:
                     * assignmentLoop){
                     * finalSchool.addAssignments(RequestConverter
                     * .convertAssignmentToProtobuf(loopCourse)); } break;
                     */
                        default:
                            break;
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder build = ItemResult.newBuilder();
                        build.setQuery(itemSet.getQuery());
                        results.add(buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                    } else {
                        e.printStackTrace();
                        throw e;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final ItemResult.Builder build = ItemResult.newBuilder();
                    build.setQuery(itemSet.getQuery());
                    build.setData(itemSet.toByteString());
                    results.add(buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                }
            }
            if (!results.isEmpty()) {
                GeneralConnectionServer.send(conn, buildRequest(results, SUCCESS_MESSAGE, req));
            }
            return;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            GeneralConnectionServer.send(conn, buildRequest(null, e.getMessage(), req));
        } catch (AuthenticationException e) {
            e.printStackTrace();
            GeneralConnectionServer.send(conn, buildRequest(null, "user was not authenticated to insert data " + e.getMessage(), req));
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
     * Builds a result but with no binary data.
     *
     * @param data
     *            The data from the result.
     * @param type
     *            What the original query was.
     * @return A built item result with no binary data.
     */
    private static ItemResult buildResult(final String data, final ItemQuery type) {
        final ItemResult.Builder result = ItemResult.newBuilder();
        result.setReturnText(data);
        result.setQuery(type);
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

        DataResult.Builder dataResult = null;
        if (results != null && !results.isEmpty()) {
            dataResult = DataResult.newBuilder();
            dataResult.addAllResults(results);
        }

        final Request.Builder dataReq = Request.newBuilder();
        dataReq.setRequestType(MessageType.DATA_REQUEST);
        dataReq.setSessionInfo(req.getSessionInfo());
        dataReq.setResponseText(message);
        if (dataResult != null) {
            dataReq.setOtherData(dataResult.build().toByteString());
        }
        return dataReq.build();
    }
}
