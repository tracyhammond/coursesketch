package database.institution.sql;

//import com.mongodb.DB;
//import com.mongodb.DBObject;
//import com.mongodb.DBRef;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.State;
import java.sql.*;

import static database.DatabaseStringConstants.*;

/**
 * The state managers handles the students states. States are stored by UserId
 * then type appended by the actual Id
 *
 * @author gigemjt
 *
 */
public final class SqlStateManager {

    /**
            * Private constructor.
            *
            */
            private SqlStateManager() {
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
        public static State getState(final Connection conn, final String userId, final String classification, final String itemId) throws DatabaseAccessException {
            final State.Builder state = State.newBuilder();
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM STATE WHERE [UserID]=\'" + userId + "\' AND [SchoolItemType]=\'" + classification + "\' AND [SchoolItemID]=\'" + itemId + "\';");
                state.setCompleted((Boolean) (rs.getBoolean("Completed")));
                state.setStarted((Boolean) (rs.getBoolean("Started")));
            state.setGraded((Boolean) (rs.getBoolean("Graded")));
        }catch(SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
        return state.build();
    }

    /**
     * Creates the state if it does not exist otherwise it updates the old state.
     * @param conn the database that contains the state. Must point to proper database.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param itemId the id of the related state (assignmentId, courseId, ...)
     * @param state what the state is being set to.
     */
    public static void setState(final Connection conn, final String userId, final String classification, final String itemId, final State state) throws DatabaseAccessException {
        // FUTURE: finish this!
        // what might be good is to retrieve the old state... compare given
        // values
        // set new updated state. (overriding old state)
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT * FROM STATE WHERE [UserID]=\'" + userId + "\' AND [SchoolItemType]=\'" + classification + "\' AND [SchoolItemID]=\'" + itemId + "\';");
            if(rs.next()) {
                rs.updateBoolean("Completed", state.getCompleted());
                rs.updateBoolean("Started", state.getStarted());
                rs.updateBoolean("Graded", state.getGraded());
                rs.updateRow();
            }
            else {
                rs.moveToInsertRow();
                rs.updateString("UserID", userId);
                rs.updateString("SchoolItemType", classification);
                rs.updateString("SchoolItemID", itemId);
                rs.updateBoolean("Completed", state.getCompleted());
                rs.updateBoolean("Started", state.getStarted());
                rs.updateBoolean("Graded", state.getGraded());
                rs.insertRow();
                rs.moveToCurrentRow();
            }
        }catch(SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
    }
}
