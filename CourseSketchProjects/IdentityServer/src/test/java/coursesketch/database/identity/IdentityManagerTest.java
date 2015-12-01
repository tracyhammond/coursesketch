package coursesketch.database.identity;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static database.DbSchoolUtility.getCollectionFromType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Created by dtracers on 10/8/2015.
 */
@RunWith(PowerMockRunner.class)
public class IdentityManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    private static final String VALID_USERNAME = "Valid username";
    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";
    private static final String INVALID_REGISTRATION_KEY = "NOT VALID KEY YO";
    private static final String INVALID_USERNAME = "NOT VALID USERNAME";

    public static final School.ItemType INVALID_ITEM_TYPE = School.ItemType.LECTURE;
    public static final School.ItemType VALID_ITEM_TYPE = School.ItemType.COURSE;
    public static final School.ItemType VALID_ITEM_CHILD_TYPE = School.ItemType.ASSIGNMENT;

    public static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_CHILD_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_ID = new ObjectId().toHexString();

    public static final String VALID_GROUP_ID = new ObjectId().toHexString();
    public static final String INVALID_GROUP_ID = new ObjectId().toHexString();

    public static final String TEACHER_AUTH_ID = new ObjectId().toHexString();
    public static final String STUDENT_AUTH_ID = new ObjectId().toHexString();
    public static final String MOD_AUTH_ID = new ObjectId().toHexString();

    public static final String TEACHER_USER_ID = new ObjectId().toHexString();
    public static final String STUDENT_USER_ID = new ObjectId().toHexString();
    public static final String MOD_USER_ID = new ObjectId().toHexString();

    // this user id is not in the db
    public static final String NO_ACCESS_ID = new ObjectId().toHexString();

    public DB db;

    public IdentityManager identityManager;
    Authenticator dbAuthChecker;
    @Mock
    private AuthenticationChecker authChecker;
    @Mock
    private AuthenticationOptionChecker optionChecker;

    @Before
    public void before() throws UnknownHostException {

        db = fongo.getDB(); // new MongoClient("localhost").getDB("test");
        identityManager = new IdentityManager(db);

        try {
            // general rules
            AuthenticationHelper.setMockPermissions(authChecker, null, null, null, null, Authentication.AuthResponse.PermissionLevel.NO_PERMISSION);

            when(optionChecker.authenticateDate(any(AuthenticationDataCreator.class), anyLong()))
                    .thenReturn(false);

            when(optionChecker.isItemPublished(any(AuthenticationDataCreator.class)))
                    .thenReturn(false);

            when(optionChecker.isItemRegistrationRequired(any(AuthenticationDataCreator.class)))
                    .thenReturn(true);
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        dbAuthChecker = new Authenticator(authChecker, optionChecker);
    }

    public void insertValidObject(School.ItemType itemType, String itemId, String... groupId) {
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
    public void testGroupCreation() throws AuthenticationException, NoSuchAlgorithmException {
        identityManager.createNewGroup(TEACHER_USER_ID, VALID_ITEM_ID);
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        DBObject nonUsers = new BasicDBObject();
        nonUsers.put(TEACHER_USER_ID, hash);

        DBObject adminGroup = (DBObject) dbObject.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        DBObject students = new BasicDBObject();

        DBObject studentGroup = (DBObject) dbObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(students, studentGroup);

    }

    @Test
    public void testInsertingCourseItem() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        DBObject nonUsers = new BasicDBObject();
        nonUsers.put(TEACHER_USER_ID, hash);

        DBObject adminGroup = (DBObject) dbObject.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        DBObject students = new BasicDBObject();

        DBObject studentGroup = (DBObject) dbObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(students, studentGroup);

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userList.get(0));
    }

    @Test
    public void testInsertingBankItem() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        final String courseId = "COURSE_ID";
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, School.ItemType.BANK_PROBLEM, courseId, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(School.ItemType.BANK_PROBLEM)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        String courseHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        DBObject nonUsers = new BasicDBObject();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        DBObject adminGroup = ((DBObject) dbObject.get(DatabaseStringConstants.NON_USER_LIST));
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        DBObject students = new BasicDBObject();
        students.put(courseId, courseHash);

        DBObject studentGroup = (DBObject) dbObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(students, studentGroup);
    }

    @Test
    public void testInsertingChildItemItemWithAdminPermission() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        AuthenticationHelper.setMockPermissions(authChecker, null, null, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);

        // looks for item data
        final DBObject dbItemChildObject = db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).findOne(new ObjectId(VALID_ITEM_CHILD_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        DBObject nonUsers = new BasicDBObject();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        DBObject adminGroup = (DBObject) dbObject.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        BasicDBObject students = new BasicDBObject();

        DBObject studentGroup = (BasicDBObject) dbObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(students, studentGroup);


        List<String> userListParent = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        List<String> userListChild = (List<String>) dbItemChildObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userListParent.get(0));
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testInsertingChildItemItemWithMissingParent() throws AuthenticationException, DatabaseAccessException {
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, null, null, TEACHER_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).remove(
                new BasicDBObject(DatabaseStringConstants.SELF_ID, new ObjectId(VALID_ITEM_ID)));
        DBObject dbItemObjectNull = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertNull(dbItemObjectNull);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);
    }

    @Test
    public void testInsertingChildItemItemWithModeratorPermission() throws AuthenticationException, DatabaseAccessException,
            NoSuchAlgorithmException {
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, null, null, MOD_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.MODERATOR);

        identityManager.registerSelf(MOD_USER_ID, MOD_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        identityManager.insertNewItem(MOD_USER_ID, MOD_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);

        // looks for item data
        final DBObject dbItemChildObject = db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).findOne(new ObjectId(VALID_ITEM_CHILD_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        String moderatorHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
            moderatorHash = HashManager.toHex(HashManager.createHash(MOD_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        DBObject nonUsers = new BasicDBObject();
        nonUsers.put(TEACHER_USER_ID, teacherHash);
        nonUsers.put(MOD_USER_ID, moderatorHash);

        DBObject adminGroup = (DBObject) dbObject.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        DBObject students = new BasicDBObject();

        DBObject studentGroup = (DBObject) dbObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(students, studentGroup);

        // userlists
        List<String> userListParent = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        List<String> userListChild = (List<String>) dbItemChildObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userListParent.get(0));
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = AuthenticationException.class)
    public void testInsertingChildItemItemWithInvalidPermission() throws AuthenticationException, DatabaseAccessException {
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        identityManager.insertNewItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);
    }

    @Test
    public void testRegisteringUserInCourse() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.registerSelf(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        // Looks for group data
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        Assert.assertEquals(VALID_ITEM_ID, dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(dbObject.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        String studentHash = null;

        teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        studentHash = HashManager.toHex(HashManager.createHash(STUDENT_USER_ID, salt).getBytes());

        // Non users
        DBObject nonUsers = new BasicDBObject();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        DBObject adminGroup = (DBObject) dbObject.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        DBObject students = new BasicDBObject();
        students.put(STUDENT_USER_ID, studentHash);

        DBObject studentGroup = (DBObject) dbObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(students, studentGroup);

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userList.get(0));
    }

    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseInvalidKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        identityManager.registerSelf(STUDENT_USER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }


    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseNullKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        identityManager.registerSelf(STUDENT_USER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }


    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidItemId() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        identityManager.registerSelf(STUDENT_USER_ID, userId, INVALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidGroupId() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // Remove group from the database
        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find();
        final DBObject dbObject = cursor.next();
        System.out.println(dbObject);
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).remove(dbObject);

        // looks for item data
        final DBObject dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOne(new ObjectId(VALID_ITEM_ID));
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, null, null, STUDENT_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.registerSelf(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test
    public void createNewUser() throws AuthenticationException, NoSuchAlgorithmException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        Assert.assertEquals(1, results.size());

        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find();
        final DBObject dbObject = cursor.next();

        Assert.assertEquals(VALID_USERNAME, dbObject.get(DatabaseStringConstants.USER_NAME));

        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), next.getKey());
        Assert.assertTrue(HashManager.validateHash(next.getValue(), dbObject.get(DatabaseStringConstants.PASSWORD).toString()));
    }

    @Test
    public void getUserIdentity() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        // user info is created correctly!
        Assert.assertEquals(1, results.size());

        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find();
        final DBObject dbObject = cursor.next();

        Assert.assertEquals(VALID_USERNAME, dbObject.get(DatabaseStringConstants.USER_NAME));

        // Gets the user info

        String userIdentity = identityManager.getUserIdentity(VALID_USERNAME, next.getValue());

        // checks that the user identity is grabbed correctly!
        Assert.assertEquals(next.getKey(), userIdentity);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserIdentityThrowsWhenInvalidUsername() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        // user info is created correctly!
        Assert.assertEquals(1, results.size());

        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find();
        final DBObject dbObject = cursor.next();

        Assert.assertEquals(VALID_USERNAME, dbObject.get(DatabaseStringConstants.USER_NAME));

        // Gets the user info

        String userIdentity = identityManager.getUserIdentity(INVALID_USERNAME, next.getValue());

        // checks that the user identity is grabbed correctly!
        Assert.assertEquals(next.getKey(), userIdentity);
    }

    @Test(expected = AuthenticationException.class)
    public void getUserIdentityThrowsWhenInvalidAuth() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        // user info is created correctly!
        Assert.assertEquals(1, results.size());

        final DBCursor cursor = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find();
        final DBObject dbObject = cursor.next();

        Assert.assertEquals(VALID_USERNAME, dbObject.get(DatabaseStringConstants.USER_NAME));

        // Gets the user info

        String userIdentity = identityManager.getUserIdentity(VALID_USERNAME, "invalid auth");

        // checks that the user identity is grabbed correctly!
        Assert.assertEquals(next.getKey(), userIdentity);
    }

    @Test(expected = AuthenticationException.class)
    public void getUserNameFromIdentityThrowsWhenInvalidPermisson() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserNameFromIdentityThrowsWhenItemDoNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = AuthenticationException.class)
    public void getUserNameFromIdentityThrowsWhenUserDoNotExistInItem() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserNameFromIdentityThrowsWhenUserDoNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerSelf(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test
    public void getUserNameFromIdentity() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        final String userId = identityManager.createNewUser(STUDENT_USER_ID).keySet().iterator().next();

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerSelf(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        final Map<String, String> userName = identityManager.getUserName(userId, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        Assert.assertTrue(userName.containsKey(userId));
        Assert.assertEquals(STUDENT_USER_ID, userName.get(userId));
    }

    @Test
    public void isUserInItem() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerSelf(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        Assert.assertTrue(identityManager.isUserInItem(STUDENT_USER_ID, true, VALID_ITEM_ID, VALID_ITEM_TYPE));
    }

    @Test(expected = DatabaseAccessException.class)
    public void isUserInItemThrowsWhenItemDoesNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        Assert.assertTrue(identityManager.isUserInItem(STUDENT_USER_ID, true, VALID_ITEM_ID, VALID_ITEM_TYPE));
    }

    @Test
    public void isUserNotInItem() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertFalse(identityManager.isUserInItem(STUDENT_USER_ID, true, VALID_ITEM_ID, VALID_ITEM_TYPE));
    }

    @Test
    public void isAdminInItem() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerSelf(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        Assert.assertTrue(identityManager.isUserInItem(TEACHER_USER_ID, false, VALID_ITEM_ID, VALID_ITEM_TYPE));
    }

    @Test
    public void getCourseRoster() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String userName = "UserName" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash = null;
            try {
                final String unsecuredSalt = HashManager.generateUnSecureSalt(VALID_ITEM_ID);
                hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new AuthenticationException(e);
            }
            userIdsToUserNames.put(hash, userName);
        }

        // adds user to the course
        for (String userId: userIds) {
            identityManager.registerSelf(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getCourseRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertEquals(userIdsToUserNames, userValues);
    }

    @Test
    public void getCourseRosterReturnsSubsetWhenGivenSubset() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();
        Set<String> subset = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            String userName = "UserName" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash = null;
            try {
                final String unsecuredSalt = HashManager.generateUnSecureSalt(VALID_ITEM_ID);
                hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new AuthenticationException(e);
            }
            if (Math.random() >= 0.5) {
                subset.add(userId);
                userIdsToUserNames.put(hash, userName);
            }
        }

        // adds user to the course
        for (String userId: userIds) {
            identityManager.registerSelf(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getCourseRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, subset, dbAuthChecker);

        Assert.assertEquals(userIdsToUserNames, userValues);
    }

    @Test
    public void getCourseRosterDoesNotReturnUserNamesWhenPeer() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.insertNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String userName = "UserName" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash = null;
            try {
                final String unsecuredSalt = HashManager.generateUnSecureSalt(VALID_ITEM_ID);
                hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new AuthenticationException(e);
            }
            userIdsToUserNames.put(hash, userName);
        }

        // adds user to the course
        for (String userId: userIds) {
            identityManager.registerSelf(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getCourseRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertEquals(userIdsToUserNames, userValues);
    }
}
