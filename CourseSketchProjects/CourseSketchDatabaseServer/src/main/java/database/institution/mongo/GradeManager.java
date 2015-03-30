package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import protobuf.srl.grading.Grading.GradeHistory;
import protobuf.srl.grading.Grading.ProtoGrade;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COMMENTS;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static database.DatabaseStringConstants.CURRENT_GRADE;
import static database.DatabaseStringConstants.GRADE_COLLECTION;
import static database.DatabaseStringConstants.GRADE_HISTORY;
import static database.DatabaseStringConstants.USER_ID;

/**
 * Created by Matt on 3/29/2015.
 */
public class GradeManager {

    /**
     * Private constructor.
     */
    private GradeManager() {
    }

    public static List<ProtoGrade> getAllCourseGradesInstructor(final Authenticator authenticator, final DB dbs, final String courseId,
            final String userId) throws AuthenticationException, DatabaseAccessException {
        final DBCollection gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final BasicDBObject query = new BasicDBObject(COURSE_ID, courseId)
                .append(COURSE_PROBLEM_ID, new BasicDBObject("$exists", false));
        final DBCursor cursor = gradeCollection.find(query);
        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Grades were not found for course with ID " + courseId);
        }

        // Check authentication so only teachers of the course can retrieve all grades
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        List<ProtoGrade> grades = new ArrayList<>();
        while (cursor.hasNext()) {
            grades.add(buildProtoGrade(cursor.next()));
        }


    }

    private static ProtoGrade buildProtoGrade(DBObject grade) {
        final ProtoGrade.Builder protoGrade = ProtoGrade.newBuilder();
        protoGrade.setCourseId(grade.get(COURSE_ID).toString());
        protoGrade.setUserId(grade.get(USER_ID).toString());
        protoGrade.setAssignmentId(grade.get(ASSIGNMENT_ID).toString());
        if (grade.containsField(COURSE_PROBLEM_ID)) {
            protoGrade.setProblemId(grade.get(COURSE_PROBLEM_ID).toString());
        }
        protoGrade.setCurrentGrade((float) grade.get(CURRENT_GRADE));
        if (grade.containsField(COMMENTS)) {
            protoGrade.setComment(grade.get(COMMENTS).toString());
        }
        if (grade.containsField(GRADE_HISTORY)) {
            List<DBObject> history = (List<DBObject>) grade.get(GRADE_HISTORY);
            for (int i = 0; i < history.size(); i++) {
                protoGrade.addGradeHistory(buildProtoGradeHistory(history.get(i)));
            }
        }

    }

    private static GradeHistory buildProtoGradeHistory(DBObject history) {

    }
}
