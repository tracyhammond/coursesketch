package database;

import static database.DatabaseStringConstants.CLASSIFICATION;
import static database.DatabaseStringConstants.GROUP_PREFIX;
import static database.DatabaseStringConstants.GROUP_PREFIX_LENGTH;
import static database.DatabaseStringConstants.TIME;
import static database.DatabaseStringConstants.UPDATEID;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_LIST;

import java.util.ArrayList;
import java.util.List;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlSchool;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import utilities.TimeManager;
import database.auth.AuthenticationException;
import database.institution.mongo.MongoInstitution;
import database.institution.mongo.UpdateManager;

/**
 * Hanldes updates for the user so that the system can do heavy caching on the client.
 * @author gigemjt
 *
 */
public final class UserUpdateHandler {

    /**
     * The maximum amount of time an update is saved.  (30 days)
     */
    private static final long TIME_LIMIT = 2592000000L;

    /**
     * The classification of an update if it for a course.
     */
    public static final String COURSE_CLASSIFICATION = "COURSE";

    /**
     * The classification of an update if it for an assignment.
     */
    public static final String ASSIGNMENT_CLASSIFICATION = "ASSIGNMENT";

    /**
     * The classification of an update if it for a bank problem.
     */
    public static final String PROBLEM_CLASSIFICATION = "PROBLEM";

    /**
     * The classification of an update if it for a course problem.
     */
    public static final String COURSE_PROBLEM_CLASSIFICATION = "COURSE_PROBLEM";

    /**
     * Private constructor.
     */
    private UserUpdateHandler() {
    }

    /**
     * Removes updates for the user where the userId is older than 30 days.
     *
     * @param database the database where the updates are stored.
     * @param userId the user who is affected by these updates.
     * @throws AuthenticationException Thrown if the user does not have access to the update.
     * @throws DatabaseAccessException Thrown if the database does not contain the specified update.
     */
    public static void removeOldUpdates(final DB database, final String userId) throws AuthenticationException, DatabaseAccessException {
        // ges all of the updates.
        final BasicDBList updateList = UpdateManager.mongoGetUpdate(database, userId, 0);
        final int size = updateList.size();
        for (int i = 0; i < size; i++) {
            final long difference = TimeManager.getSystemTime() - ((Long) ((BasicBSONObject) updateList.get(i)).get(TIME));
            if (TIME_LIMIT < difference) {
                UpdateManager.mongoDeleteUpdate(database, userId, (String) ((BasicBSONObject) updateList.get(i)).get(UPDATEID),
                        (String) ((BasicBSONObject) updateList.get(i)).get(CLASSIFICATION));
            }
        }
    }

    /**
     * Inserts updates for a group of uses.
     *
     * @param database the database where the update is being inserted
     * @param users the list of users affected by this update.
     * @param objectAffectedId the id of the object that was updated.
     * @param classification the type of update (course, assignment, ...)
     */
    public static void insertUpdates(final DB database, final String[] users, final String objectAffectedId, final String classification) {
        if (users == null) {
            System.err.println("There are no users for this school item");
            return;
        }
        for (int i = 0; i < users.length; i++) {
            try {
                UpdateManager.mongoInsertUpdate(database, users[i], objectAffectedId, TimeManager.getSystemTime(), classification);
            } catch (AuthenticationException | DatabaseAccessException e) {
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
     * @param database the database where the updates are being inserted.
     * @param users the list of people who are holding this new update.
     * @param objectAffectedId the id of the object that was updated.
     * @param classification if it is a course, assignment, ...
     */
    public static void insertUpdates(final DB database, final List<String> users, final String objectAffectedId, final String classification) {
        if (users == null) {
            System.err.println("There are no users for this school item");
            return;
        }

        for (String group : users) {
            if (group.startsWith(GROUP_PREFIX)) {
                final DBRef myDbRef = new DBRef(database, USER_GROUP_COLLECTION, new ObjectId(group.substring(GROUP_PREFIX_LENGTH)));
                final DBObject corsor = myDbRef.fetch();
                final ArrayList<String> list = (ArrayList<String>) corsor.get(USER_LIST);
                insertUpdates(database, list, objectAffectedId, classification);
            } else {
                try {
                    UpdateManager.mongoInsertUpdate(database, group, objectAffectedId, TimeManager.getSystemTime(), classification);
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Insert a new update into the database.
     * @param database the database where the update is being inserted into.
     * @param userId who the update is applying to.
     * @param objectAffectedId the id of the object that was updated.
     * @param classification the update classification.
     * @throws AuthenticationException thrown if the user does not have permission to access the update.
     * @throws DatabaseAccessException thrown if the update does not exist or if the user does not exist.
     */
    public static void insertUpdate(final DB database, final String userId, final String objectAffectedId, final String classification)
            throws AuthenticationException, DatabaseAccessException {
        UpdateManager.mongoInsertUpdate(database, userId, objectAffectedId, TimeManager.getSystemTime(), classification);
    }

    /**
     * Retrieves all the updates for a given user and returns them as a
     * {@link SrlSchool} item.
     *
     * FUTURE: handle the case when an update is create for an assignment that
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
     * @throws AuthenticationException thrown if the user does not have access to the update or the contents of the update.
     * @throws DatabaseAccessException thrown if some of the data in the update does not exist.
     */
    public static SrlSchool mongoGetAllRelevantUpdates(final DB dbs, final String userId, final long time)
            throws AuthenticationException, DatabaseAccessException {
        final BasicDBList userUpdates = UpdateManager.mongoGetUpdate(dbs, userId, time);
        final int size = userUpdates.size();
        final List<String> objectAffectedId = new ArrayList<String>();
        final SrlSchool.Builder build = SrlSchool.newBuilder();

        for (int i = 0; i < size; i++) {
            final String classification = (String) ((BasicBSONObject) userUpdates.get(i)).get(CLASSIFICATION);
            objectAffectedId.add((String) ((BasicBSONObject) userUpdates.get(i)).get(UPDATEID));
            if (COURSE_CLASSIFICATION.equals(classification)) {
                build.addCourses(MongoInstitution.getInstance().getCourses(objectAffectedId, userId).get(0));
            } else if (ASSIGNMENT_CLASSIFICATION.equals(classification)) {
                build.addAssignments(MongoInstitution.getInstance().getAssignment(objectAffectedId, userId).get(0));
            } else if (PROBLEM_CLASSIFICATION.equals(classification)) {
                build.addBankProblems(MongoInstitution.getInstance().getProblem(objectAffectedId, userId).get(0));
            } else if (COURSE_PROBLEM_CLASSIFICATION.equals(classification)) {
                build.addProblems(MongoInstitution.getInstance().getCourseProblem(objectAffectedId, userId).get(0));
            }
        }
        return build.build();

    }

}
