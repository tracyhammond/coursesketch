package database.institution.mongo;

import static database.DatabaseStringConstants.STATE_COLLECTION;
import static database.DatabaseStringConstants.STATE_COMPLETED;
import static database.DatabaseStringConstants.STATE_STARTED;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.State;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

/**
 * The state managers handles the students states. States are stored by UserId
 * then type appended by the actual Id
 *
 * @author gigemjt
 *
 */
public class StateManager {

    /**
     * Returns the state for a given school item.
     *
     * Right now only the completed/started state is applied
     *
     * @param dbs the database that contains the state.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param id the id of the related state (assignmentId, courseId, ...)
     * @return the sate of the assignment.
     */
    public static State getState(final DB dbs, final String userId, final String classification, final String id) {
        final State.Builder state = State.newBuilder();
        final DBRef myDbRef = new DBRef(dbs, STATE_COLLECTION, new ObjectId(userId));
        final DBObject obj = myDbRef.fetch();
        final DBObject stateInfo = (DBObject) obj.get(classification + id);
        state.setCompleted((Boolean) (stateInfo.get(STATE_COMPLETED)));
        state.setStarted((Boolean) (stateInfo.get(STATE_STARTED)));
        return state.build();
    }

    /**
     * Creates the state if it does not exist otherwise it updates the old state.
     * @param dbs the database that contains the state.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param id the id of the related state (assignmentId, courseId, ...)
     * @param state what the state is being set to.
     */
    public static void setState(final DB dbs, final String userId, final String classification, final String id, final State state) {
        // FUTURE: finish this!
        // what might be good is to retrieve the old state... compare given
        // values
        // set new updated state. (overriding old state)
    }
}
