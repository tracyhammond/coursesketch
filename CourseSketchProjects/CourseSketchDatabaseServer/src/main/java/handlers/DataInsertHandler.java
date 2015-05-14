package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.SocketSession;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlUser;
import protobuf.srl.submission.Submission;
import utilities.ExceptionUtilities;
import utilities.LoggingConstants;

import static handlers.ResultBuilder.ID_SEPARATOR;

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
            LOG.info("Recieving DATA SEND Request...");

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
                            final SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
                            final String resultId = instance.insertCourse(userId, course);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + course.getId()));
                        }
                        break;
                        case ASSIGNMENT: {
                            final SrlAssignment assignment = SrlAssignment.parseFrom(itemSet.getData());
                            final String resultId = instance.insertAssignment(userId, assignment);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + assignment.getId()));
                        }
                        break;
                        case COURSE_PROBLEM: {
                            final SrlProblem problem = SrlProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertCourseProblem(userId, problem);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + problem.getId()));
                        }
                        break;
                        case BANK_PROBLEM: {
                            final SrlBankProblem problem = SrlBankProblem.parseFrom(itemSet.getData());
                            final String resultId = instance.insertBankProblem(userId, problem);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + problem.getId()));
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
                                results.add(ResultBuilder.buildResult(itemSet.getQuery(), "User was already registered for course!"));
                            }
                        }
                        break;
                        case LECTURE: {
                            final Lecture lecture = Lecture.parseFrom(itemSet.getData());
                            final String resultId = instance.insertLecture(userId, lecture);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + lecture.getId()));
                        }
                        break;
                        case LECTURESLIDE: {
                            final LectureSlide lectureSlide = LectureSlide.parseFrom(itemSet.getData());
                            final String resultId = instance.insertLectureSlide(userId, lectureSlide);
                            results.add(ResultBuilder.buildResult(itemSet.getQuery(), resultId + ID_SEPARATOR + lectureSlide.getId()));
                        }
                        break;
                        case EXPERIMENT: {
                            LOG.info("Inserting experiment!");
                            final Submission.SrlExperiment experiment = Submission.SrlExperiment.parseFrom(itemSet.getData());
                            LOG.info("Experiment: {}", experiment);
                            instance.insertSubmission(userId, experiment.getProblemId(), experiment.getSubmission().getId(), true);
                        }
                        break;
                        case GRADE: {
                            final Grading.ProtoGrade grade = ProtoGrade.parseFrom(itemSet.getData());
                            instance.addGrade(userId, grade);
                        }
                        break;
                        default:
                            throw new Exception("Insert type not supported.");
                    }
                } catch (AuthenticationException e) {
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                    conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder itemResult = ItemResult.newBuilder();
                        itemResult.setQuery(itemSet.getQuery());
                        results.add(ResultBuilder.buildResult(e.getMessage(), itemSet.getQuery(), itemResult.build()));
                    } else {
                        LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
                        throw e;
                    }

                } catch (Exception e) {
                    final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
                    conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
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
        } catch (AuthenticationException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            conn.send(ExceptionUtilities.createExceptionRequest(protoEx, "user was not authenticated to insert data " + protoEx.getMssg(), req));
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            final Message.ProtoException protoEx = ExceptionUtilities.createProtoException(e);
            conn.send(ExceptionUtilities.createExceptionRequest(protoEx, req));
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, e);
            conn.send(ResultBuilder.buildRequest(null, e.getMessage(), req));
        }
    }
}
