package database.institution;

import static util.StringConstants.*;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import protobuf.srl.school.School.State;

/**
 * The state managers handles the students states.
 * States are stored by UserId then type appended by the actual Id
 * @author gigemjt
 *
 */
public class StateManager {

	/**
	 * Returns the state for a given school item.
	 *
	 * Right now only the completed/started state is applied
	 * @param dbs
	 * @param userId
	 * @param type
	 * @param id
	 * @return
	 */
	public static State getState(DB dbs, String userId, String type, String id) {
		State.Builder state = State.newBuilder();
		DBRef myDbRef = new DBRef(dbs, STATE_COLLECTION, new ObjectId(userId));
		DBObject obj = myDbRef.fetch();
		DBObject stateInfo = (DBObject) obj.get(type + id);
		state.setCompleted((Boolean)(stateInfo.get(STATE_COMPLETED)));
		state.setStarted((Boolean)(stateInfo.get(STATE_STARTED)));
		return state.build();
	}

	public static void setState(DB dbs, String userId, String type, String id, State state) {
		// TODO: finish this!
	}
}
