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

public final class MongoAuthenticator extends Authenticator {

    final DB dbs;

    public MongoAuthenticator(final DB iDbs) {
        // TODO THIS MAY NOT WORK
        super(null);
        this.dbs = iDbs;
    }

    protected final List<String> getUserList(String id) {
        final DBRef myDbRef = new DBRef(dbs, USER_GROUP_COLLECTION, new ObjectId(id));
        final DBObject corsor = myDbRef.fetch();
        List<String> list = null;
        list = (List) corsor.get(USER_LIST);
        return list;
    }

    protected AuthenticationData getAuthGroups(String collection, String itemId) {
        AuthenticationData data = new AuthenticationData();

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
