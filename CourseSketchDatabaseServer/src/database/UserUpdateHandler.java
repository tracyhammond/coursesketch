package database;

import static database.StringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.BasicBSONObject;

import protobuf.srl.school.School.SrlSchool;

import com.mongodb.BasicDBList;
import com.mongodb.DB;

import connection.TimeManager;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.UpdateManager;

public class UserUpdateHandler {

	final static long TIME_LIMIT = 2592000000L;
	public static final String COURSE_CLASSIFICATION = "COURSE";
	public static final String ASSIGNMENT_CLASSIFICATION = "ASSIGNMENT";
	public static final String PROBLEM_CLASSIFICATION = "PROBLEM";
	public static final String COURSE_PROBLEM_CLASSIFICATION = "COURSE_PROBLEM";

	public static void removeOldUpdates(DB db, String userId) throws AuthenticationException, DatabaseAccessException {
		BasicDBList updateList = UpdateManager.mongoGetUpdate(db, userId, 0); // gets all of the updates
		int size = updateList.size();
		for (int i = 0; i < size; i++) {
			long difference = TimeManager.getSystemTime() - ((Long) ((BasicBSONObject) updateList.get(i)).get(TIME));
			if (TIME_LIMIT < difference) {
				UpdateManager.mongoDeleteUpdate(db, userId, (String) ((BasicBSONObject) updateList.get(i)).get(UPDATEID),
						(String) ((BasicBSONObject) updateList.get(i)).get(CLASSIFICATION));
			}
		}
	}

	public static void InsertUpdates(DB db, String userId, String id, String classification) throws AuthenticationException, DatabaseAccessException {
		UpdateManager.mongoInsertUpdate(db, userId, id, TimeManager.getSystemTime(), classification);
	}

	/**
	 * Retrieves all the updates for a given user and returns them as a
	 * {@link SrlSchool} item.
	 * 
	 * @param dbs
	 *            The database to get the information from.
	 * @param userId
	 *            The userId that we are currently looking at.
	 * @param time
	 *            The last time that the client was updated. (everything past
	 *            that point will be sent)
	 * @return an SrlSchool protobuf object.
	 * @throws AuthenticationException
	 * @throws DatabaseAccessException
	 */
	public static SrlSchool mongoGetAllRelevantUpdates(DB dbs, String userId, long time) throws AuthenticationException, DatabaseAccessException {
		BasicDBList userUpdates = UpdateManager.mongoGetUpdate(dbs, userId, time);
		int size = userUpdates.size();
		List<String> id = new ArrayList<String>();
		SrlSchool.Builder build = SrlSchool.newBuilder();

		for (int i = 0; i < size; i++) {
			String classification = (String) ((BasicBSONObject) userUpdates.get(i)).get(CLASSIFICATION);
			id.add((String) ((BasicBSONObject) userUpdates.get(i)).get(UPDATEID));
			if (COURSE_CLASSIFICATION.equals(classification)) {
				build.addCourses(Institution.mongoGetCourses(id, userId).get(0));
			} else if (ASSIGNMENT_CLASSIFICATION.equals(classification)) {
				build.addAssignments(Institution.mongoGetAssignment(id, userId).get(0));
			} else if (PROBLEM_CLASSIFICATION.equals(classification)) {
				build.addBankProblems(Institution.mongoGetProblem(id, userId).get(0));
			} else if (COURSE_PROBLEM_CLASSIFICATION.equals(classification)) {
				build.addProblems(Institution.mongoGetCourseProblem(id, userId).get(0));
			}
		}
		return build.build();

	}

}
