package database.auth;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.Authenticator.AuthenticationData;
import org.bson.types.ObjectId;

import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.USERS;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_LIST;

/**
 * A mongo implementation of the {@link AuthenticationDataCreator}.
 * @author gigemjt
 *
 */
public final class MongoAuthenticator implements AuthenticationDataCreator {

    /**
     * The mongo database that is used for the authentication.
     */
    private final DB dbs;

    /**
     * @param iDbs The database that is used for all authentication purposes.
     */
    public MongoAuthenticator(final DB iDbs) {
        this.dbs = iDbs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserList(final String userGroupId) {
        final DBRef myDbRef = new DBRef(dbs, USER_GROUP_COLLECTION, new ObjectId(userGroupId));
        final DBObject corsor = myDbRef.fetch();
        return (List) corsor.get(USER_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationData getAuthGroups(final String collection, final String itemId) throws DatabaseAccessException {
        final AuthenticationData data = new AuthenticationData();

        final DBRef myDbRef = new DBRef(dbs, collection, new ObjectId(itemId));
        final DBObject cursor = myDbRef.fetch();

        if (cursor == null) {
            throw new DatabaseAccessException("Item: " + itemId + " was not found in collection: " + collection);
        }

        data.setUserList((List) ((List<Object>) cursor.get(USERS)));

        data.setModeratorList((List) ((List<Object>) cursor.get(MOD)));

        data.setAdminList((List) ((List<Object>) cursor.get(ADMIN)));

        data.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(ACCESS_DATE)).longValue()));

        data.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) cursor.get(CLOSE_DATE)).longValue()));
        return data;
    }

    /**
     * @param ids a list of ids for the admin, mod, and user.
     * @return a query created from the list of ids.
     */
    @SuppressWarnings({ "PMD.UselessParentheses" })
    public static BasicDBObject createMongoCopyPermissionQeuery(final List<String>... ids) {
        BasicDBObject updateQuery = null;
        BasicDBObject fieldQuery = null;
        for (int k = 0; k < ids.length; k++) {
            final List<String> list = ids[k];
            // k = 0 ADMIN, k = 1, MOD, k >= 2 USERS
            final String field = k == 0 ? ADMIN : (k == 1 ? MOD : USERS);
            if (k == 0) {
                fieldQuery = new BasicDBObject(field, new BasicDBObject("$each", list));
                updateQuery = new BasicDBObject("$addToSet", fieldQuery);
            } else {
                fieldQuery.append(field, new BasicDBObject("$each", list));
            }
        }
        return updateQuery;
    }
}
