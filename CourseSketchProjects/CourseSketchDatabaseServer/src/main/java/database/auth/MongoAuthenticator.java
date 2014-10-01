package database.auth;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.USERS;
import static database.DatabaseStringConstants.USER_GROUP_COLLECTION;
import static database.DatabaseStringConstants.USER_LIST;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.RequestConverter;
import database.auth.Authenticator.AuthenticationData;

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

    @Override
    public List<String> getUserList(final String id) {
        final DBRef myDbRef = new DBRef(dbs, USER_GROUP_COLLECTION, new ObjectId(id));
        final DBObject corsor = myDbRef.fetch();
        final List<String> list = (List) corsor.get(USER_LIST);
        return list;
    }

    @Override
    public AuthenticationData getAuthGroups(final String collection, final String itemId) {
        final AuthenticationData data = new AuthenticationData();

        final DBRef myDbRef = new DBRef(dbs, collection, new ObjectId(itemId));
        final DBObject corsor = myDbRef.fetch();

        data.setUserList((List) ((List<Object>) corsor.get(USERS)));

        data.setModeratorList((List) ((List<Object>) corsor.get(MOD)));

        data.setAdminList((List) ((List<Object>) corsor.get(ADMIN)));

        data.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(ACCESS_DATE)).longValue()));

        data.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(CLOSE_DATE)).longValue()));
        return data;
    }
}
