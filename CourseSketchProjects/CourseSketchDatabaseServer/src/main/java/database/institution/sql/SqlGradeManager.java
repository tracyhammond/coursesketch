package database.institution.sql;

//import com.mongodb.DB;
//import com.mongodb.DBObject;
//import com.mongodb.DBRef;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.SrlGrade;
import java.sql.*;

import static database.DatabaseStringConstants.*;

/**
 * The state managers handles the students states. States are stored by UserId
 * then type appended by the actual Id
 *
 * @author gigemjt
 *
 */
public final class SqlGradeManager {

    /**
     * Private constructor.
     *
     */
    private SqlGradeManager() {
    }

    /**
     * Returns the state for a given school item.
     *
     * Right now only the completed/started state is applied
     *
     * @param conn the sql connection. Must point to proper database.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param itemId the id of the related state (assignmentId, courseId, ...)
     * @return the sate of the assignment.
     */
    public static SrlGrade getGrade(final Connection conn, final String userId, final String classification, final String itemId) throws DatabaseAccessException {
        final SrlGrade.Builder grade = SrlGrade.newBuilder();
        try {
            grade.setId("");
            grade.setProblemId("");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Grades WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';");
            grade.setGrade((rs.getFloat("Grade")));
            grade.setComment((rs.getString("Comments")));
        }catch(SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
        return grade.build();
    }

    /**
     * Creates the state if it does not exist otherwise it updates the old state.
     * @param conn the database that contains the state. Must point to proper database.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param itemId the id of the related state (assignmentId, courseId, ...)
     * @param grade what the grade is being set to.
     * @return reslut of set: "SET", "INSERT", "ERROR"
     */
    public static String setGrade(final Connection conn, final String userId, final String classification, final String itemId, final SrlGrade grade) throws DatabaseAccessException {
        // FUTURE: finish this!
        // what might be good is to retrieve the old state... compare given
        // values
        // set new updated state. (overriding old state)
        String result = "ERROR";
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT * FROM Grades WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';");
            if(rs.next()) {
                rs.updateFloat("Grade", grade.getGrade());
                rs.updateString("Comments", grade.getComment());
                rs.updateRow();
                result="SET";
            }
            else {
                rs.moveToInsertRow();
                rs.updateString("UserID", userId);
                rs.updateString("SchoolItemType", classification);
                rs.updateString("SchoolItemID", itemId);
                rs.updateFloat("Grade", grade.getGrade());
                rs.updateString("Comments", grade.getComment());
                rs.insertRow();
                rs.moveToCurrentRow();
                result="INSERT";
            }
        }catch(SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
        return result;
    }
}
