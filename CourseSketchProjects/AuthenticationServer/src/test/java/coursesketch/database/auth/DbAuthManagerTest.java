package coursesketch.database.auth;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import coursesketch.server.authentication.HashManager;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.utils.Util;
import protobuf.srl.services.authentication.Authentication;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
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
    public FongoRule fongo = new FongoRule();

    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";
    private static final String INVALID_REGISTRATION_KEY = "NOT VALID KEY YO";

    public static final Util.ItemType INVALID_ITEM_TYPE = Util.ItemType.BANK_PROBLEM;
    public static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;
    public static final Util.ItemType VALID_ITEM_CHILD_TYPE = Util.ItemType.ASSIGNMENT;

    public static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_CHILD_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_ID = new ObjectId().toHexString();

    public static final String VALID_GROUP_ID = new ObjectId().toHexString();
    public static final String INVALID_GROUP_ID = new ObjectId().toHexString();

    public static final String TEACHER_ID = new ObjectId().toHexString();
    public static final String STUDENT_ID = new ObjectId().toHexString();
    public static final String MOD_ID = new ObjectId().toHexString();

    // this user id is not in the db
    public static final String NO_ACCESS_ID = new ObjectId().toHexString();

    public DB db;

    public DbAuthManager dbAuthManager;
    DbAuthChecker dbAuthChecker;

    @Before
    public void before() throws UnknownHostException {

        db = fongo.getDB(); // Equivalent to new MongoClient("localhost").getDB("test");
        dbAuthChecker = new DbAuthChecker(db);
        dbAuthManager = new DbAuthManager(db);
    }

    public void insertValidObject(Util.ItemType itemType, String itemId, String... groupId) {
        List<Object> list = new BasicDBList();
        Collections.addAll(list, groupId);
        db.getCollection(getCollectionFromType(itemType)).insert(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(itemId))
                        .append(DatabaseStringConstants.USER_LIST, list));
    }

    public void insertValidGroup(String groupId, String courseId, BasicDBObject... permissions) {
        List<Object> list = new BasicDBList();
        BasicDBObject group = new BasicDBObject(DatabaseStringConstants.SELF_ID,  new ObjectId(groupId))
                .append(DatabaseStringConstants.COURSE_ID, courseId);
        for (BasicDBObject obj: permissions) {
            // grabs the first key and value in the object
            group.append(obj.keySet().iterator().next(), obj.values().iterator().next());
        }
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).insert(group);
    }

    public BasicDBObject createPermission(String authId, Authentication.AuthResponse.PermissionLevel level) {
        return new BasicDBObject(authId, level.getNumber());
    }

    @Test
    public void testGroupCreation() throws AuthenticationException {
        dbAuthManager.createNewGroup(TEACHER_ID, VALID_ITEM_ID);
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(hash));
    }

    @Test
    public void testInsertingCourseItem() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(hash));

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingBankItem() throws AuthenticationException, DatabaseAccessException {
        final String courseId = "COURSE_ID";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, courseId, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(teacherHash));

        String courseHash = null;
        try {
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(courseHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE, dbObject.get(courseHash));

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }


    @Test
    public void testInsertingBankItemWithNoCourseId() throws AuthenticationException, DatabaseAccessException {
        final String courseId = "COURSE_ID";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(teacherHash));

        String courseHash = null;
        try {
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(null, dbObject.get(courseHash));

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingBankItemWithEmptyCourseId() throws AuthenticationException, DatabaseAccessException {
        final String courseId = "COURSE_ID";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, "", VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(teacherHash));

        String courseHash = null;
        try {
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(null, dbObject.get(courseHash));

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingChildItemItemWithAdminPermission() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);

        // looks for item data
        final DBObject dbItemChildObject = db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).findOne(new ObjectId(VALID_ITEM_CHILD_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(hash));

        List<String> userListParent = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        List<String> userListChild = (List<String>) dbItemChildObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userListParent.get(0));
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testInsertingChildItemItemWithMissingParent() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        dbAuthChecker = mock(DbAuthChecker.class);
        when(dbAuthChecker.isAuthenticated(any(Util.ItemType.class), anyString(), anyString(), any(Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder().setPermissionLevel(
                        Authentication.AuthResponse.PermissionLevel.TEACHER).build());

        db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).remove(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(VALID_ITEM_ID)));
        DBObject dbItemObjectNull = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertNull(dbItemObjectNull);

        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);
    }

    @Test
    public void testInsertingChildItemItemWithModeratorPermission() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        // ADDDING MODERATOR AS A USER!
        final DBCursor modifyGroup = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject groupToModify = modifyGroup.next();

        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(MOD_ID, groupToModify.get(DatabaseStringConstants.SALT).toString()).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        BasicDBObject update = new BasicDBObject(hash, Authentication.AuthResponse.PermissionLevel.MODERATOR_VALUE);
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).update(groupToModify,
                new BasicDBObject(DatabaseStringConstants.SET_COMMAND, update));

        dbAuthManager.insertNewItem(MOD_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);

        // looks for item data
        final DBObject dbItemChildObject = db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).findOne(new ObjectId(VALID_ITEM_CHILD_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(teacherHash));
        Assert.assertTrue(dbObject.containsField(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.MODERATOR_VALUE, dbObject.get(hash));

        List<String> userListParent = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        List<String> userListChild = (List<String>) dbItemChildObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userListParent.get(0));
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = AuthenticationException.class)
    public void testInsertingChildItemItemWithInvalidPermission() throws AuthenticationException, DatabaseAccessException {
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());

        dbAuthManager.insertNewItem(STUDENT_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, null, dbAuthChecker);
    }

    @Test
    public void testRegisteringUserInCourse() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY, dbAuthChecker);

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = dbObject.get(DatabaseStringConstants.SALT).toString();


        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(teacherHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(teacherHash));

        String userHash = null;
        try {
            userHash = HashManager.toHex(HashManager.createHash(userId, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(userHash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.STUDENT_VALUE, dbObject.get(userHash));

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseInvalidKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, INVALID_REGISTRATION_KEY, dbAuthChecker);
    }


    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseNullKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
    }


    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidItemId() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, INVALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidGroupId() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        dbAuthManager.insertNewItem(TEACHER_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, VALID_REGISTRATION_KEY, null);

        // Remove group from the coursesketch.util.util
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).remove(dbObject);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(VALID_REGISTRATION_KEY, dbItemObject.get(DatabaseStringConstants.REGISTRATION_KEY).toString());
        Assert.assertEquals(TEACHER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        dbAuthManager.registerSelf(userId, VALID_ITEM_ID, VALID_ITEM_TYPE, VALID_REGISTRATION_KEY, dbAuthChecker);
    }
}
