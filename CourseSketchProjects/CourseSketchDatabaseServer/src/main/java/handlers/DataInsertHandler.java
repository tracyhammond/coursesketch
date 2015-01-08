package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.SocketSession;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlUser;
import protobuf.srl.submission.Submission;

import java.util.ArrayList;

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
     */
    private DataInsertHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     *
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl" })
    public static void handleData(final Request req, final SocketSession conn) {
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
                                results.add(ResultBuilder.buildResult(resultId + ID_SEPARATOR + course.getId(), itemSet.getQuery()));
                            } catch (DatabaseAccessException e) {
                                // unable to register user for course
                                final ItemResult.Builder build = ItemResult.newBuilder();
                                build.setQuery(itemSet.getQuery());
                                results.add(ResultBuilder.buildResult(build.build().toByteString(),
                                        "Unable to register user for course: " + e.getMessage(),
                                        ItemQuery.ERROR));
                            }
                        }
                        break;
                        case ASSIGNMENT: {
                            final SrlAssignment assignment = SrlAssignment.parseFrom(itemSet.getData());
                            final String resultId = instance.insertAssignment(userId, assignment);
                            results.add(ResultBuilder.buildResult(resultId + ID_SEPARATOR + assignment.getId(), itemSet.getQuery()));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final SrlProblem problem = SrlProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertCourseProblem(userId, problem);
                            results.add(ResultBuilder.buildResult(resultId + ID_SEPARATOR + problem.getId(), itemSet.getQuery()));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final SrlBankProblem problem = SrlBankProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertBankProblem(userId, problem);
                            results.add(ResultBuilder.buildResult(resultId + ID_SEPARATOR + problem.getId(), itemSet.getQuery()));
                        }
                        break;
                        /*
                         * case CLASS_GRADE: { SrlGrade grade =
                         * SrlGrade.parseFrom(itemSet.getData()); String
                         * resultId = MongoInstitution.mongoInsertClassGrade(userId,
                         * grade); results.add(ResultBuilder.buildResult(resultId + " : " +
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
                                results.add(ResultBuilder.buildResult("User was already registered for course!", itemSet.getQuery()));
                            }
                        }
                        break;
                        case LECTURE: {
                            final Lecture lecture = Lecture.parseFrom(itemSet.getData());
                            final String resultId = instance.insertLecture(userId, lecture);
                            results.add(ResultBuilder.buildResult(resultId + ID_SEPARATOR + lecture.getId(), itemSet.getQuery()));
                        }
                        break;
                        case LECTURESLIDE: {
                            final LectureSlide lectureSlide = LectureSlide.parseFrom(itemSet.getData());
                            final String resultId = instance.insertLectureSlide(userId, lectureSlide);
                            results.add(ResultBuilder.buildResult(resultId + ID_SEPARATOR + lectureSlide.getId(), itemSet.getQuery()));
                        }
                        break;
                        case EXPERIMENT: {
                            System.out.println("Inserting experiment!");
                            final Submission.SrlExperiment experiment = Submission.SrlExperiment.parseFrom(itemSet.getData());
                            System.out.println(experiment);
                            instance.insertSubmission(userId, experiment.getProblemId(), experiment.getSubmission().getId(), true);
                        }
                        break;
                        default:
                            throw new Exception("Insert type not supported.");
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder build = ItemResult.newBuilder();
                        build.setQuery(itemSet.getQuery());
                        results.add(ResultBuilder.buildResult(build.build().toByteString(), e.getMessage(), itemSet.getQuery()));
                    } else {
                        e.printStackTrace();
                        throw e;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final ItemResult.Builder build = ItemResult.newBuilder();
                    build.setQuery(itemSet.getQuery());
                    build.setData(itemSet.toByteString());
                    results.add(ResultBuilder.buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                }
            }
            if (!results.isEmpty()) {
                conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            conn.send(ResultBuilder.buildRequest(null, "user was not authenticated to insert data " + e.getMessage(), req));
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            e.printStackTrace();
            conn.send(ResultBuilder.buildRequest(null, e.getMessage(), req));
        }
    }
}
