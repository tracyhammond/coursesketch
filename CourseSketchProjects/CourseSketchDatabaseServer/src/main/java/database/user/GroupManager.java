package database.user;

import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_LIST;

import java.util.ArrayList;

import protobuf.srl.school.School.SrlGroup;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import database.auth.AuthenticationException;
import database.auth.Authenticator;

/**
 * Manages information about authetication groups.
 * @author gigemjt
 *
 */
public final class GroupManager {

    /**
     * Private constructor.
     *
     */
    private GroupManager() {
    }

    /**
     * Inserts a new group into the database.
     * @param dbs the database that the group is beign added to.
     * @param group the infomration about the group itself.
     * @return A string that represents the database Id of the group.
     */
    public static String mongoInsertGroup(final DB dbs, final SrlGroup group) {
        final DBCollection newUser = dbs.getCollection(USER_GROUP_COLLECTION);
        final BasicDBObject query = new BasicDBObject(USER_LIST, group.getUserIdList()).append(NAME, group.getGroupName()).append(ADMIN,
                group.getAdminList());

        newUser.insert(query);
        final DBObject corsor = newUser.findOne(query);
        return corsor.get(SELF_ID).toString();
    }

    /**
     * Returns a group.
     * @param authenticator The object that is authenticating the user.
     * @param dbs The database where the group is stored.
     * @param groupId The database id of the group.
     * @param userId the user that is asking permission to get the group.
     * @return A group if the user has permission to access the group.
     * @throws AuthenticationException Thrown if the person asking for the group does not have authentication to the group.
     */
    public static SrlGroup mongoGetGroup(final Authenticator authenticator, final DB dbs, final String groupId, final String userId)
            throws AuthenticationException {
        final DBCollection courses = dbs.getCollection(USER_GROUP_COLLECTION);
        final BasicDBObject query = new BasicDBObject("_id", groupId);
        final DBObject corsor = courses.findOne(query);

        final ArrayList<String> adminList = (ArrayList) corsor.get("Admin");
        boolean isAdmin;
        isAdmin = authenticator.checkAuthentication(userId, adminList);

        if (!isAdmin) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final SrlGroup.Builder exactGroup = SrlGroup.newBuilder();
        exactGroup.addAllUserId((ArrayList) corsor.get("UserList"));
        exactGroup.setGroupName((String) corsor.get("Name"));
        if (isAdmin) {
            exactGroup.addAllAdmin((ArrayList) corsor.get("Admin")); // admin
        }
        return exactGroup.build();
    }

    /**
     * Updates the user in the group.
     * @param authenticator The object that is authenticating the user.
     * @param dbs The database where the group is stored
     * @param groupID the database id of the group
     * @param userId the user that is trying to update the group
     * @param group the group that contains the updated information.
     * @return true if succeeds.
     * @throws AuthenticationException Thrown if the user does not have permission to modify the group.
     */
    public static boolean mongoUpdateGroup(final Authenticator authenticator, final DB dbs, final String groupID, final String userId,
            final SrlGroup group) throws AuthenticationException {
        final DBCollection courses = dbs.getCollection(USER_GROUP_COLLECTION);
        final BasicDBObject query = new BasicDBObject(SELF_ID, groupID);
        final DBObject corsor = courses.findOne(query);

        final ArrayList<String> adminList = (ArrayList) corsor.get("Admin");
        boolean isAdmin;
        isAdmin = authenticator.checkAuthentication(userId, adminList);

        if (!isAdmin) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin) {
            if (group.hasGroupName()) {
                updated.append("$set", new BasicDBObject("Name", group.getGroupName()));
            }
            if (group.getUserIdCount() > 0) {
                updated.append("$set", new BasicDBObject("UserList", group.getUserIdList()));
            }
            if (group.getAdminCount() > 0) {
                updated.append("$set", new BasicDBObject("UserList", group.getAdminList()));
            }
        }
        return true;
    }
}
