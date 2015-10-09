package coursesketch.database.auth;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import coursesketch.server.authentication.HashManager;
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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
    public static final String VALID_ITEM_ID = new ObjectId().toHexString();

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
        String salt = null;
        try {
            salt = HashManager.generateSalt();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        insertValidGroup(VALID_GROUP_ID, VALID_ITEM_ID, salt,
                createPermission(salt, TEACHER_ID, Authentication.AuthResponse.PermissionLevel.TEACHER),
                createPermission(salt, STUDENT_ID, Authentication.AuthResponse.PermissionLevel.STUDENT),
                createPermission(salt, MOD_ID, Authentication.AuthResponse.PermissionLevel.MODERATOR));
    }

    public void insertValidObject(School.ItemType itemType, String itemId, String... groupId) {
        List<Object> list = new BasicDBList();
        Collections.addAll(list, groupId);
        db.getCollection(getCollectionFromType(itemType)).insert(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(itemId))
                        .append(DatabaseStringConstants.USER_LIST, list));
    }

    public void insertValidGroup(String groupId, String courseId, String salt, BasicDBObject... permissions) {
        List<Object> list = new BasicDBList();
        BasicDBObject group = new BasicDBObject(DatabaseStringConstants.SELF_ID,  new ObjectId(groupId))
                .append(DatabaseStringConstants.COURSE_ID, courseId)
                .append(DatabaseStringConstants.SALT, salt);
        for (BasicDBObject obj: permissions) {
            // grabs the first key and value in the object
            group.append(obj.keySet().iterator().next(), obj.values().iterator().next());
        }
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).insert(group);
    }

    public BasicDBObject createPermission(String salt, String authId, Authentication.AuthResponse.PermissionLevel level) {
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(authId, salt).getBytes());
            System.out.println("HASH FOR ID: " + authId + "+ SALT: " + salt + " IS [" + hash + "]");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return new BasicDBObject(hash, level.getNumber());
    }

    @Test(expected = AuthenticationException.class)
    public void authExceptionThrownWhenNoAuthDataIsSent() throws DatabaseAccessException, AuthenticationException {
        authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.getDefaultInstance());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenWrongTypeGiven() throws DatabaseAccessException, AuthenticationException {
        authChecker.isAuthenticated(INVALID_ITEM_TYPE, INVALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenGivenWrongIdExists() throws DatabaseAccessException, AuthenticationException {
        authChecker.isAuthenticated(VALID_ITEM_TYPE, INVALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenGroupDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        final String newId = createNonExistentObjectId(VALID_ITEM_ID);
        insertValidObject(VALID_ITEM_TYPE, newId, INVALID_GROUP_ID);
        authChecker.isAuthenticated(VALID_ITEM_TYPE, newId, TEACHER_ID, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());
    }

    @Test
    public void defaultResponseIsReturnedWhenNoGroupsExist() throws DatabaseAccessException, AuthenticationException {
        final String newId = createNonExistentObjectId(VALID_ITEM_ID);
        insertValidObject(VALID_ITEM_TYPE, newId, new String[]{});
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, newId, TEACHER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(Authentication.AuthResponse.getDefaultInstance(), response);
    }

    @Test
    public void noPermissionIsReturnedWhenPersonDoesNotExistWithFilledCheckType() throws DatabaseAccessException, AuthenticationException {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, NO_ACCESS_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.NO_PERMISSION)
                        .setHasAccess(false)
                        .build(),
                response);
    }

    @Test
    public void noPermissionIsReturnedWithDefaultCheckType() throws DatabaseAccessException, AuthenticationException {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.NO_PERMISSION)
                        .setHasAccess(true)
                        .build(),
                response);
    }

    @Test
    public void permissionIsLimitedToWhatIsBeingChecked() throws DatabaseAccessException, AuthenticationException {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .setCheckingUser(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.STUDENT)
                        .setHasAccess(true)
                        .build(),
                response);
    }

    @Test
    public void permissionReturnsMaxLevelWhenBeingChecked() throws DatabaseAccessException, AuthenticationException {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .setCheckingAdmin(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.TEACHER)
                        .setHasAccess(true)
                        .build(),
                response);
    }

    @Test
    public void permissionReturnsStudentLevelEvenWhenAskedIfAdmin() throws DatabaseAccessException, AuthenticationException {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .setCheckingAdmin(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.STUDENT)
                        .setHasAccess(true)
                        .build(),
                response);
    }
}
