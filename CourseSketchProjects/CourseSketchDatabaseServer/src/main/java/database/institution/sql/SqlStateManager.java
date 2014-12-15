package database.institution.sql;

//import com.mongodb.DB;
//import com.mongodb.DBObject;
//import com.mongodb.DBRef;

import database.DatabaseAccessException;
import protobuf.srl.school.School.State;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The state managers handles the students states. States are stored by UserId
 * then type appended by the actual Id
 *
 * @author gigemjt
 */
public final class SqlStateManager {

    /**
     * Private constructor.
     */
    private SqlStateManager() {
    }

    /**
     * Returns the state for a given school item.
     *
     * Right now only the completed/started state is applied
     *
     * @param conn
     *         the sql connection. Must point to proper database.
     * @param userId
     *         the id of the user asking for the state.
     * @param classification
     *         if it is a course, assignment, ...
     * @param itemId
     *         the id of the related state (assignmentId, courseId, ...)
     * @return the sate of the assignment.
     * @throws DatabaseAccessException
     *         thrown if connecting to sql database cause and error.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public static State getState(final Connection conn, final String userId, final String classification, final String itemId)
            throws DatabaseAccessException {
        final State.Builder state = State.newBuilder();
        final String query = "SELECT * FROM State WHERE UserID=? AND SchoolItemType=? AND SchoolItemID=?;";
        try (PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setString(1, userId);
            stmt.setString(2, classification);
            stmt.setString(3, itemId);
            try (final ResultSet rst = stmt.executeQuery()) {
                state.setCompleted(rst.getBoolean("Completed"));
                state.setStarted(rst.getBoolean("Started"));
                state.setGraded(rst.getBoolean("Graded"));
            } catch (SQLException e) {
                throw new DatabaseAccessException(e, false);
            }

        } catch (SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
        return state.build();
    }

    /**
     * Creates the state if it does not exist otherwise it updates the old state.
     *
     * @param conn
     *         the database that contains the state. Must point to proper database.
     * @param userId
     *         the id of the user asking for the state.
     * @param classification
     *         if it is a course, assignment, ...
     * @param itemId
     *         the id of the related state (assignmentId, courseId, ...)
     * @param state
     *         what the state is being set to.
     * @return reslut of set: "SET", "INSERT", "ERROR"
     * @throws DatabaseAccessException
     *         thrown if connecting to sql database cause and error.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public static String setState(final Connection conn, final String userId, final String classification, final String itemId, final State state)
            throws DatabaseAccessException {
        String result;
        final String query = "SELECT * FROM State WHERE UserID=? AND SchoolItemType=? AND SchoolItemID=?;";
        try (PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setString(1, userId);
            stmt.setString(2, classification);
            stmt.setString(3, itemId);
            try (final ResultSet rst = stmt.executeQuery()) {
                if (rst.next()) {
                    rst.updateBoolean("Completed", state.getCompleted());
                    rst.updateBoolean("Started", state.getStarted());
                    rst.updateBoolean("Graded", state.getGraded());
                    rst.updateRow();
                    result = "SET";
                } else {
                    rst.moveToInsertRow();
                    rst.updateString("UserID", userId);
                    rst.updateString("SchoolItemType", classification);
                    rst.updateString("SchoolItemID", itemId);
                    rst.updateBoolean("Completed", state.getCompleted());
                    rst.updateBoolean("Started", state.getStarted());
                    rst.updateBoolean("Graded", state.getGraded());
                    rst.insertRow();
                    rst.moveToCurrentRow();
                    result = "INSERT";
                }
            } catch (SQLException e) {
                throw new DatabaseAccessException(e, false);
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
        return result;
    }
}
