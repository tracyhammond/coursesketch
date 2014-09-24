package database;

import static database.DatabaseStringConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlSchool;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

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

    public static void removeOldUpdates(final DB db, final String userId) throws AuthenticationException, DatabaseAccessException {
        final BasicDBList updateList = UpdateManager.mongoGetUpdate(db, userId, 0); // gets
                                                                              // all
                                                                              // of
                                                                              // the
                                                                              // updates
        final int size = updateList.size();
        for (int i = 0; i < size; i++) {
            final long difference = TimeManager.getSystemTime() - ((Long) ((BasicBSONObject) updateList.get(i)).get(TIME));
            if (TIME_LIMIT < difference) {
                UpdateManager.mongoDeleteUpdate(db, userId, (String) ((BasicBSONObject) updateList.get(i)).get(UPDATEID),
                        (String) ((BasicBSONObject) updateList.get(i)).get(CLASSIFICATION));
            }
        }
    }

    /**
     * Inserts updates for a group of uses.
     *
     * @param db
     * @param users
     * @param id
     * @param classification
     */
    public static void InsertUpdates(final DB db, final String[] users, final String id, final String classification) {
        if (users == null) {
            System.err.println("There are no users for this school item");
            return;
        }
        for (int i = 0; i < users.length; i++) {
            try {
                UpdateManager.mongoInsertUpdate(db, users[i], id, TimeManager.getSystemTime(), classification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inserts updates for a group of users.
     *
     * This method will recursively search for all users to insert into the
     * update list
     *
     * @param db
     * @param users
     * @param id
     * @param classification
     */
    public static void InsertUpdates(final DB db, final List<String> users, final String id, final String classification) {
        if (users == null) {
            System.err.println("There are no users for this school item");
            return;
        }

        for (String group : users) {
            if (group.startsWith(GROUP_PREFIX)) {
                final DBRef myDbRef = new DBRef(db, USER_GROUP_COLLECTION, new ObjectId(group.substring(GROUP_PREFIX_LENGTH)));
                final DBObject corsor = myDbRef.fetch();
                final ArrayList<String> list = (ArrayList<String>) corsor.get(USER_LIST);
                InsertUpdates(db, list, id, classification);
            } else {
                try {
                    UpdateManager.mongoInsertUpdate(db, group, id, TimeManager.getSystemTime(), classification);
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void InsertUpdate(final DB db, final String userId, final String id, final String classification)
            throws AuthenticationException, DatabaseAccessException {
        UpdateManager.mongoInsertUpdate(db, userId, id, TimeManager.getSystemTime(), classification);
    }

    /**
     * Retrieves all the updates for a given user and returns them as a
     * {@link SrlSchool} item.
     *
     * TODO: handle the case when an update is create for an assignment that
     * does not exist.
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
    public static SrlSchool mongoGetAllRelevantUpdates(final DB dbs, final String userId, final long time)
            throws AuthenticationException, DatabaseAccessException {
        final BasicDBList userUpdates = UpdateManager.mongoGetUpdate(dbs, userId, time);
        final int size = userUpdates.size();
        final List<String> id = new ArrayList<String>();
        final SrlSchool.Builder build = SrlSchool.newBuilder();

        for (int i = 0; i < size; i++) {
            final String classification = (String) ((BasicBSONObject) userUpdates.get(i)).get(CLASSIFICATION);
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
