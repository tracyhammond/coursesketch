package coursesketch.database.auth;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.server.authentication.HashManager;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static coursesketch.database.util.MongoUtilities.getUserGroup;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dtracers on 10/8/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DbAuthChecker.class)
public class DbAuthManagerTest {

    @Rule
    private FongoRule fongo = new FongoRule();

    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";
    private static final String INVALID_REGISTRATION_KEY = "NOT VALID KEY YO";

    private static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;
    private static final Util.ItemType VALID_ITEM_CHILD_TYPE = Util.ItemType.ASSIGNMENT;

    private static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_CHILD_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_ID = new ObjectId().toHexString();

    private static final String TEACHER_ID = new ObjectId().toHexString();
    private static final String STUDENT_ID = new ObjectId().toHexString();
    private static final String MOD_ID = new ObjectId().toHexString();

    private MongoDatabase db;

    private DbAuthManager dbAuthManager;
    private DbAuthChecker dbAuthChecker;

    @Before
    public void before() throws UnknownHostException {

        db = fongo.getDatabase(); // Equivalent to new MongoClient("localhost").getDB("test");
        dbAuthChecker = new DbAuthChecker(db);
        dbAuthManager = new DbAuthManager(db);
    }

    public void insertValidObject(Util.ItemType itemType, String itemId, String... groupId) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, groupId);
        db.getCollection(getCollectionFromType(itemType)).insertOne(
                new Document(DatabaseStringConstants.SELF_ID, new ObjectId(itemId))
                        .append(DatabaseStringConstants.USER_LIST, list));
    }

    public void insertValidGroup(String groupId, String courseId, Document... permissions) {
        Document group = new Document(DatabaseStringConstants.SELF_ID, new ObjectId(groupId))
                .append(DatabaseStringConstants.COURSE_ID, courseId);
        for (Document obj : permissions) {
            // grabs the first key and value in the object
            group.append(obj.keySet().iterator().next(), obj.values().iterator().next());
        }
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).insertOne(group);
    }

    public Document createPermission(String authId, Authentication.AuthResponse.PermissionLevel level) {
        return new Document(authId, level.getNumber());
    }

    @Test
    public void testGroupCreation() throws AuthenticationException {
        dbAuthManager.createNewGroup(TEACHER_ID, VALID_ITEM_ID);
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(hash));
    }

    @Test
    public void testInsertingCourseItem() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();
        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(hash));

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingBankItem() throws AuthenticationException, DatabaseAccessException {
        final String courseId = "COURSE_ID";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, courseId, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject =
                db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(teacherHash));

        String courseHash = null;
        try {
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(courseHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE, document.get(courseHash));

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }


    @Test
    public void testInsertingBankItemWithNoCourseId() throws AuthenticationException, DatabaseAccessException {
        final String courseId = "COURSE_ID";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject =
                db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(teacherHash));

        String courseHash = null;
        try {
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(null, document.get(courseHash));

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingBankItemWithEmptyCourseId() throws AuthenticationException, DatabaseAccessException {
        final String courseId = "COURSE_ID";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, "", VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject =
                db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(teacherHash));

        String courseHash = null;
        try {
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(null, document.get(courseHash));

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingChildItemItemWithAdminPermission() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);

        // looks for item data
        final Document dbItemChildObject =
                db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).find(convertStringToObjectId(VALID_ITEM_CHILD_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(hash));

        List<String> userListParent = getUserGroup(dbItemObject);
        List<String> userListChild = getUserGroup(dbItemChildObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListParent.get(0));
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testInsertingChildItemItemWithMissingParent() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        dbAuthChecker = mock(DbAuthChecker.class);
        when(dbAuthChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder().setPermissionLevel(
                        Authentication.AuthResponse.PermissionLevel.TEACHER).build());

        db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOneAndDelete(
                new Document(DatabaseStringConstants.SELF_ID, new ObjectId(VALID_ITEM_ID)));
        Document dbItemObjectNull = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertNull(dbItemObjectNull);

        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);
    }

    @Test
    public void testInsertingChildItemItemWithModeratorPermission() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        // ADDDING MODERATOR AS A USER!
        final Document groupToModify = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(MOD_ID, groupToModify.get(DatabaseStringConstants.SALT).toString()).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Document update = new Document(hash, Authentication.AuthResponse.PermissionLevel.MODERATOR_VALUE);
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).updateOne(groupToModify,
                new Document(DatabaseStringConstants.SET_COMMAND, update));

        dbAuthManager.insertNewItem(MOD_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);

        // looks for item data
        final Document dbItemChildObject =
                db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).find(convertStringToObjectId(VALID_ITEM_CHILD_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(teacherHash));
        Assert.assertTrue(document.containsKey(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.MODERATOR_VALUE, document.get(hash));

        List<String> userListParent = getUserGroup(dbItemObject);
        List<String> userListChild = getUserGroup(dbItemChildObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListParent.get(0));
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = AuthenticationException.class)
    public void testInsertingChildItemItemWithInvalidPermission() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        dbAuthManager.insertNewItem(STUDENT_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);
    }

    @Test
    public void testRegisteringUserInCourse() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY);

        checkAddedUser(userId, Authentication.AuthResponse.PermissionLevel.STUDENT, dbItemObject);
    }

    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseInvalidKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, INVALID_REGISTRATION_KEY);
    }


    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseNullKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, null);
    }


    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidItemId() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, INVALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidGroupId() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // Remove group from the database
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).findOneAndDelete(document);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY);
    }

    @Test(expected = AuthenticationException.class)
    public void testAddingUserToDatabaseValidKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.addUser(TEACHER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker, Authentication.AuthType.getDefaultInstance());
    }

    @Test(expected = AuthenticationException.class)
    public void testAddingUserToDatabaseInvalidKey() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.addUser(INVALID_REGISTRATION_KEY, "New User!", VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker,
                Authentication.AuthType.getDefaultInstance());
    }

    @Test
    public void testAddingUserAsStudentToExistingGroup() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.addUser(TEACHER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker, Authentication.AuthType.newBuilder()
                .setCheckingOwner(true).setCheckingUser(true).build());

        checkAddedUser(userId, Authentication.AuthResponse.PermissionLevel.STUDENT, dbItemObject);
    }

    @Test
    public void testAddingUserAsPerrTeacherToExistingGroup() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.addUser(TEACHER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker, Authentication.AuthType.newBuilder()
                .setCheckingOwner(true).setCheckingPeerTeacher(true).build());

        checkAddedUser(userId, Authentication.AuthResponse.PermissionLevel.PEER_TEACHER, dbItemObject);
    }

    @Test
    public void testAddingUserAsModeratorToExistingGroup() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.addUser(TEACHER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker, Authentication.AuthType.newBuilder()
                .setCheckingOwner(true).setCheckingMod(true).build());

        checkAddedUser(userId, Authentication.AuthResponse.PermissionLevel.MODERATOR, dbItemObject);
    }

    @Test
    public void testAddingUserAsTeacherToExistingGroup() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.addUser(TEACHER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker, Authentication.AuthType.newBuilder()
                .setCheckingOwner(true).setCheckingAdmin(true).build());

        checkAddedUser(userId, Authentication.AuthResponse.PermissionLevel.TEACHER, dbItemObject);
    }

    private void checkAddedUser(String userId, Authentication.AuthResponse.PermissionLevel permissionLevel, Document dbItemObject) {

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = document.get(DatabaseStringConstants.SALT).toString();

        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, document.get(teacherHash));

        String userHash = null;
        try {
            userHash = HashManager.toHex(HashManager.createHash(userId, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(document.containsKey(userHash));
        Assert.assertEquals(permissionLevel.getNumber(), document.get(userHash));

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }
}
