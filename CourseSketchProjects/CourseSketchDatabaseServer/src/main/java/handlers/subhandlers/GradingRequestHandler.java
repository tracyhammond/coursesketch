package handlers.subhandlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.institution.Institution;
import handlers.DataInsertHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.grading.Grading.GradingQuery;
import protobuf.srl.query.Data.ItemRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to handle grade data requests to the server.
 *
 * Created by matt on 4/22/15.
 */
public final class GradingRequestHandler {
    /**
     * The index in the request itemId that corresponds to the courseId for the grade being requested..
     */
    private static final int COURSE_INDEX = 0;

    /**
     * The index in the request itemId that corresponds to the assignmentId being requested.
     */
    private static final int ASSIGNMENT_INDEX = 1;

    /**
     * The index in the request itemId that corresponds to the problemId being requested.
     */
    private static final int PROBLEM_INDEX = 2;

    /**
     * The index in the request itemId that corresponds to the userId for the grade being requested.
     */
    private static final int USER_INDEX = 3;

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataInsertHandler.class);

    /**
     * Private constructor.
     */
    private GradingRequestHandler() {
    }

    /**
     * Handles grading requests to the server.
     *
     * @param institution The coursesketch.util.util interface.
     * @param request The request being sent.
     * @param authId The id used to authenticate the user.
     * @param userId The id of the user who sent the request used for identification purposes.
     * @return List of the grades. The list is length 1 if it is only a single grade.
     * @throws AuthenticationException Thrown if user does not have correct permission to retrieve grade.
     * @throws DatabaseAccessException Thrown if there is something not found in the coursesketch.util.util.
     * @throws InvalidProtocolBufferException Thrown if a protobuf object is not correctly formatted.
     */
    public static List<ProtoGrade> gradingRequestHandler(final Institution institution, final ItemRequest request, final String authId,
            final String userId) throws DatabaseAccessException, InvalidProtocolBufferException, AuthenticationException {
        final GradingQuery query = GradingQuery.parseFrom(request.getAdvanceQuery());

        final boolean instructor = query.getPermissionLevel() == GradingQuery.PermissionLevel.INSTRUCTOR;
        final boolean student = query.getPermissionLevel() == GradingQuery.PermissionLevel.STUDENT;
        final boolean allGrades = query.getSearchType() == GradingQuery.SearchType.ALL_GRADES;
        final boolean singleGrade = query.getSearchType() == GradingQuery.SearchType.SINGLE_GRADE;

        LOG.debug("Query State instructor: {}, student: {}, allGrades: {}, singleGrade: {}", instructor, student, allGrades, singleGrade);
        List<ProtoGrade> returnList = new ArrayList<>();
        if (instructor && allGrades) {
            returnList = institution.getAllAssignmentGradesInstructor(authId, request.getItemId(COURSE_INDEX));
        } else if (student && allGrades) {
            returnList = institution.getAllAssignmentGradesStudent(userId, authId, request.getItemId(COURSE_INDEX));
        } else {
            final ProtoGrade gradeData = ProtoGrade.newBuilder()
                    .setUserId(request.getItemId(USER_INDEX))
                    .setCourseId(request.getItemId(COURSE_INDEX))
                    .setAssignmentId(request.getItemId(ASSIGNMENT_INDEX))
                    .setProblemId(request.getItemId(PROBLEM_INDEX))
                    .build();
            returnList.add(institution.getGrade(userId, authId, gradeData));
        }
        return returnList;
    }
}
