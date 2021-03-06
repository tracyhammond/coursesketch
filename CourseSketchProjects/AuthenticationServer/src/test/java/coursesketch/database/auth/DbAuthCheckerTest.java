package coursesketch.database.auth;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.server.authentication.HashManager;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coursesketch.test.utilities.DatabaseHelper.createNonExistentObjectId;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;

/**
 * Created by dtracers on 9/17/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DbAuthCheckerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    private static final Util.ItemType INVALID_ITEM_TYPE = Util.ItemType.BANK_PROBLEM;
    private static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;

    private static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_ID = new ObjectId().toHexString();

    private static final String VALID_GROUP_ID = new ObjectId().toHexString();
    private static final String INVALID_GROUP_ID = new ObjectId().toHexString();

    private static final String TEACHER_ID = new ObjectId().toHexString();
    private static final String VALID_OWNER_ID = new ObjectId().toHexString();
    private static final String STUDENT_ID = new ObjectId().toHexString();
    private static final String MOD_ID = new ObjectId().toHexString();

    // this user id is not in the db
    private static final String NO_ACCESS_ID = new ObjectId().toHexString();

    private MongoDatabase db;

    private DbAuthChecker authChecker;

    @Before
    public void before() throws Exception {

        db = fongo.getDatabase(); // Equivalent to new MongoClient("localhost").getDB("test");
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

    public void insertValidObject(Util.ItemType itemType, String itemId, String... groupId) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, groupId);
        db.getCollection(getCollectionFromType(itemType)).insertOne(
                new Document(DatabaseStringConstants.SELF_ID, new ObjectId(itemId))
                        .append(DatabaseStringConstants.USER_LIST, list)
                        .append(DatabaseStringConstants.OWNER_ID, VALID_OWNER_ID));
    }

    public void insertValidGroup(String groupId, String courseId, String salt, Document... permissions) {
        List<Object> list = new ArrayList<>();
        Document group = new Document(DatabaseStringConstants.SELF_ID, new ObjectId(groupId))
                .append(DatabaseStringConstants.COURSE_ID, courseId)
                .append(DatabaseStringConstants.SALT, salt);
        for (Document obj : permissions) {
            // grabs the first key and value in the object
            group.append(obj.keySet().iterator().next(), obj.values().iterator().next());
        }
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).insertOne(group);
    }

    public Document createPermission(String salt, String authId, Authentication.AuthResponse.PermissionLevel level) throws Exception {
        String hash = null;
        hash = HashManager.toHex(HashManager.createHash(authId, salt).getBytes());
        System.out.println("HASH FOR ID: " + authId + "+ SALT: " + salt + " IS [" + hash + "]");
        return new Document(hash, level.getNumber());
    }

    @Test(expected = AuthenticationException.class)
    public void authExceptionThrownWhenNoAuthDataIsSent() throws Exception {
        authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.getDefaultInstance());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenWrongTypeGiven() throws Exception {
        authChecker.isAuthenticated(INVALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenGivenWrongIdExists() throws Exception {
        authChecker.isAuthenticated(VALID_ITEM_TYPE, INVALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenGroupDoesNotExist() throws Exception {
        final String newId = createNonExistentObjectId(VALID_ITEM_ID);
        insertValidObject(VALID_ITEM_TYPE, newId, INVALID_GROUP_ID);
        authChecker.isAuthenticated(VALID_ITEM_TYPE, newId, TEACHER_ID, Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build());
    }

    @Test
    public void defaultResponseIsReturnedWhenNoGroupsExist() throws Exception {
        final String newId = createNonExistentObjectId(VALID_ITEM_ID);
        insertValidObject(VALID_ITEM_TYPE, newId, new String[]{});
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, newId, TEACHER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(Authentication.AuthResponse.getDefaultInstance(), response);
    }

    @Test
    public void noPermissionIsReturnedWhenPersonDoesNotExistWithFilledCheckType() throws Exception {
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
    public void noPermissionIsReturnedWithDefaultCheckType() throws Exception {
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
    public void permissionIsLimitedToWhatIsBeingChecked() throws Exception {
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
    public void permissionReturnsMaxLevelWhenBeingChecked() throws Exception {
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
    public void permissionReturnsStudentLevelEvenWhenAskedIfAdmin() throws Exception {
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

    @Test
    public void permissionReturnsMaxLevelWhenBeingCheckedForOwner() throws Exception {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, VALID_OWNER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .setCheckingAdmin(true)
                        .setCheckingOwner(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.TEACHER)
                        .setIsOwner(true)
                        .setHasAccess(true)
                        .build(),
                response);
    }

    @Test
    public void permissionReturnsMaxLevelWhenBeingCheckedForOwnerButNotOwner() throws Exception {
        Authentication.AuthResponse response = authChecker.isAuthenticated(VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID,
                Authentication.AuthType.newBuilder()
                        .setCheckAccess(true)
                        .setCheckingAdmin(true)
                        .setCheckingOwner(true)
                        .build());
        new ProtobufComparisonBuilder().setIgnoreSetDefaultFields(false).build().equals(
                Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(Authentication.AuthResponse.PermissionLevel.TEACHER)
                        .setIsOwner(false)
                        .setHasAccess(true)
                        .build(),
                response);
    }
}
