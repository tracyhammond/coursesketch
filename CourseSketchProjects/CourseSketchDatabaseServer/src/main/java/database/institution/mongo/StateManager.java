package database.institution.mongo;

import static database.DatabaseStringConstants.STATE_COLLECTION;
import static database.DatabaseStringConstants.STATE_COMPLETED;
import static database.DatabaseStringConstants.STATE_STARTED;


import com.mongodb.client.MongoDatabase;
import database.DatabaseAccessException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.utils.Util.State;
import utilities.LoggingConstants;

import static database.utilities.MongoUtilities.convertStringToObjectId;


/**
 * The state managers handles the students states. States are stored by UserId
 * then type appended by the actual Id
 *
 * @author gigemjt
 */
public final class StateManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StateManager.class);

    /**
     * Private constructor.
     */
    private StateManager() {
    }

    /**
     * Returns the state for a given school item.
     *
     * Right now only the completed/started state is applied
     *
     * @param dbs the database that contains the state.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param itemId the id of the related state (assignmentId, courseId, ...)
     * @return the sate of the assignment.
     */
    public static State getState(final MongoDatabase dbs, final String userId, final String classification, final String itemId) {
        final State.Builder state = State.newBuilder();
        Document document = null;
        try {
            document = convertStringToObjectId(userId);
        } catch (DatabaseAccessException exception) {
            LOG.error(LoggingConstants.EXCEPTION_MESSAGE, exception);
        }
        final Document updateState = dbs.getCollection(STATE_COLLECTION).find(document).first();

        final Document stateInfo = (Document) updateState.get(classification + itemId);
        state.setCompleted((Boolean) (stateInfo.get(STATE_COMPLETED)));
        state.setStarted((Boolean) (stateInfo.get(STATE_STARTED)));
        return state.build();
    }

    /**
     * Creates the state if it does not exist otherwise it updates the old state.
     *
     * @param dbs the database that contains the state.
     * @param userId the id of the user asking for the state.
     * @param classification if it is a course, assignment, ...
     * @param itemId the id of the related state (assignmentId, courseId, ...)
     * @param state what the state is being set to.
     */
    public static void setState(final MongoDatabase dbs, final String userId, final String classification,
            final String itemId, final State state) {
        // FUTURE: finish this!
        // what might be good is to retrieve the old state... compare given
        // values
        // set new updated state. (overriding old state)
    }
}
