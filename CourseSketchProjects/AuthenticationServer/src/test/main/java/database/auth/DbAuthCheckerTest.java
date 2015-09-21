package database.auth;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static com.coursesketch.test.utilities.DatabaseHelper.createNonExistentObjectId;
import static database.DbSchoolUtility.getCollectionFromType;

/**
 * Created by dtracers on 9/17/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DbAuthCheckerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public static final School.ItemType INVALID_ITEM_TYPE = School.ItemType.LECTURE;
    public static final School.ItemType VALID_ITEM_TYPE = School.ItemType.COURSE;

    public static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_ID = new ObjectId().toString();

    public static final String VALID_GROUP_ID = new ObjectId().toHexString();
    public static final String INVALID_GROUP_ID = new ObjectId().toHexString();

    public static final String TEACHER_ID = new ObjectId().toHexString();
    public static final String STUDENT_ID = new ObjectId().toHexString();
    public static final String MOD_ID = new ObjectId().toHexString();

    // this user id is not in the db
    public static final String NO_ACCESS_ID = new ObjectId().toHexString();

    public DB db;

    public DbAuthChecker authChecker;

    @Before
    public void before() throws UnknownHostException {

        db = fongo.getDB(); // new MongoClient("localhost").getDB("test");
        authChecker = new DbAuthChecker(db);
        insertValidObject(VALID_ITEM_TYPE, VALID_ITEM_ID, VALID_GROUP_ID);
        insertValidGroup(VALID_GROUP_ID, VALID_ITEM_ID,
                createPermission(TEACHER_ID, Authentication.AuthResponse.PermissionLevel.TEACHER),
                createPermission(STUDENT_ID, Authentication.AuthResponse.PermissionLevel.STUDENT),
                createPermission(MOD_ID, Authentication.AuthResponse.PermissionLevel.MODERATOR));
    }

    public void insertValidObject(School.ItemType itemType, String itemId, String... groupId) {
        List<Object> list = new BasicDBList();
        Collections.addAll(list, groupId);
        db.getCollection(getCollectionFromType(itemType)).insert(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, itemId)
                        .append(DatabaseStringConstants.USER_GROUP_COLLECTION, list));
    }

    public void insertValidGroup(String groupId, String courseId, BasicDBObject... permissions) {
        List<Object> list = new BasicDBList();
        BasicDBObject group = new BasicDBObject(DatabaseStringConstants.SELF_ID, groupId)
                .append(DatabaseStringConstants.COURSE_ID, courseId);
        for (BasicDBObject obj: permissions) {
            // grabs the first key and value in the object
            group.append(obj.keySet().iterator().next(), obj.values().iterator().next());
        }
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION);
    }

    public BasicDBObject createPermission(String authId, Authentication.AuthResponse.PermissionLevel level) {
        return new BasicDBObject(authId, level.getNumber());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenWrongTypeGiven() throws DatabaseAccessException, AuthenticationException {
        authChecker.isAuthenticated(INVALID_ITEM_TYPE, INVALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.getDefaultInstance());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenNoDataExists() throws DatabaseAccessException, AuthenticationException {
        authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.getDefaultInstance());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenGroupDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        final String newId = createNonExistentObjectId(VALID_ITEM_ID);
        insertValidObject(VALID_ITEM_TYPE, newId, VALID_GROUP_ID);
        authChecker.isAuthenticated(VALID_ITEM_TYPE, newId, TEACHER_ID, Authentication.AuthType.getDefaultInstance());
    }

    @Test
    public void defaultResponseIsReturnedWhenNoGroupsExist() throws DatabaseAccessException, AuthenticationException {
        final String newId = createNonExistentObjectId(VALID_ITEM_ID);
        insertValidObject(VALID_ITEM_TYPE, newId, new String[]{});
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, newId, TEACHER_ID,
                Authentication.AuthType.getDefaultInstance());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(Authentication.AuthResponse.getDefaultInstance(), response);
    }

    @Test
    public void defaultResponseIsReturnedWhenPersonDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID,
                Authentication.AuthType.getDefaultInstance());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(Authentication.AuthResponse.getDefaultInstance(), response);
    }
}
