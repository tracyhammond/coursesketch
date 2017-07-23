package coursesketch.database.identity;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationChecker;
import coursesketch.database.auth.AuthenticationDataCreator;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationOptionChecker;
import coursesketch.database.auth.Authenticator;
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
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

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

import static coursesketch.database.util.DatabaseStringConstants.USER_LIST;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static coursesketch.database.util.MongoUtilities.getUserGroup;
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
    private static final String INVALID_USERNAME = "NOT VALID USERNAME";

    private static final Util.ItemType VALID_ITEM_TYPE = Util.ItemType.COURSE;
    private static final Util.ItemType VALID_ITEM_CHILD_TYPE = Util.ItemType.ASSIGNMENT;

    private static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_CHILD_ID = new ObjectId().toHexString();
    private static final String VALID_ITEM_ID = new ObjectId().toHexString();

    private static final String TEACHER_AUTH_ID = new ObjectId().toHexString();
    private static final String STUDENT_AUTH_ID = new ObjectId().toHexString();
    private static final String MOD_AUTH_ID = new ObjectId().toHexString();

    private static final String TEACHER_USER_ID = new ObjectId().toHexString();
    private static final String STUDENT_USER_ID = new ObjectId().toHexString();
    private static final String MOD_USER_ID = new ObjectId().toHexString();

    private MongoDatabase db;

    private IdentityManager identityManager;
    private Authenticator dbAuthChecker;
    @Mock
    private AuthenticationChecker authChecker;
    @Mock
    private AuthenticationOptionChecker optionChecker;

    @Before
    public void before() throws UnknownHostException {

        db = fongo.getDatabase(); // new MongoClient("localhost").getDB("test");
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
        } catch (DatabaseAccessException | AuthenticationException e) {
            e.printStackTrace();
        }
        dbAuthChecker = new Authenticator(authChecker, optionChecker);
    }

    public void insertValidObject(Util.ItemType itemType, String itemId, String... groupId) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, groupId);
        db.getCollection(getCollectionFromType(itemType)).insertOne(
                new Document(DatabaseStringConstants.SELF_ID, new ObjectId(itemId))
                        .append(USER_LIST, list));
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
    public void testGroupCreation() throws AuthenticationException, NoSuchAlgorithmException {
        identityManager.createNewGroup(TEACHER_USER_ID, VALID_ITEM_ID);
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, hash);

        Document adminGroup = (Document) document.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);
    }

    @Test
    public void testInsertingCourseItem() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, hash);

        Document adminGroup = (Document) document.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingCourseItemWithEmptyFields() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, "", null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String hash = null;
        try {
            hash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, hash);

        Document adminGroup = (Document) document.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test
    public void testInsertingBankItem() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        final String courseId = "COURSE_ID";
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, courseId, null);

        // looks for item data
        final Document dbItemObject =
                db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        String courseHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
            courseHash = HashManager.toHex(HashManager.createHash(courseId, salt).getBytes());
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        Document adminGroup = ((Document) document.get(DatabaseStringConstants.NON_USER_LIST));
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();
        students.put(courseId, courseHash);

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);
    }


    @Test
    public void testInsertingBankItemWithNoCourse() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        final String courseId = "COURSE_ID";
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, null, null);

        // looks for item data
        final Document dbItemObject =
                db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
            System.out.println(HashManager.toHex(HashManager.createHash(courseId, salt).getBytes()));
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        Document adminGroup = ((Document) document.get(DatabaseStringConstants.NON_USER_LIST));
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);
    }

    @Test
    public void testInsertingBankItemWithEmptyCourse() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, Util.ItemType.BANK_PROBLEM, "", null);

        // looks for item data
        final Document dbItemObject =
                db.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.PROBLEM_BANK_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (AuthenticationException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        Document adminGroup = ((Document) document.get(DatabaseStringConstants.NON_USER_LIST));
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);
    }

    @Test
    public void testInsertingChildItemItemWithAdminPermission() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        AuthenticationHelper.setMockPermissions(authChecker, null, null, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);

        // looks for item data
        final Document dbItemChildObject = db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).find(convertStringToObjectId
                (VALID_ITEM_CHILD_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        Document adminGroup = (Document) document.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);


        List<String> userListParent = getUserGroup(dbItemObject);
        List<String> userListChild = getUserGroup(dbItemChildObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListParent.get(0));
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testInsertingChildItemItemWithMissingParent() throws AuthenticationException, DatabaseAccessException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, null, null, TEACHER_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.TEACHER);

        db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).findOneAndDelete(
                new Document(DatabaseStringConstants.SELF_ID, new ObjectId(VALID_ITEM_ID)));
        Document dbItemObjectNull = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertNull(dbItemObjectNull);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);
    }

    @Test
    public void testInsertingChildItemItemWithModeratorPermission() throws AuthenticationException, DatabaseAccessException,
            NoSuchAlgorithmException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, null, null, MOD_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.MODERATOR);

        identityManager.registerUserInItem(MOD_USER_ID, MOD_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        identityManager.createNewItem(MOD_USER_ID, MOD_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);

        // looks for item data
        final Document dbItemChildObject = db.getCollection(getCollectionFromType(VALID_ITEM_CHILD_TYPE)).find(convertStringToObjectId
                (VALID_ITEM_CHILD_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemChildObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemChildObject.get(DatabaseStringConstants.OWNER_ID).toString());

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash = null;
        String moderatorHash = null;
        try {
            teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
            moderatorHash = HashManager.toHex(HashManager.createHash(MOD_USER_ID, salt).getBytes());
        } catch (NoSuchAlgorithmException | AuthenticationException e) {
            e.printStackTrace();
        }

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, teacherHash);
        nonUsers.put(MOD_USER_ID, moderatorHash);

        Document adminGroup = (Document) document.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);

        // userlists
        List<String> userListParent = getUserGroup(dbItemObject);
        List<String> userListChild = getUserGroup(dbItemChildObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListParent.get(0));
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userListChild.get(0));
        Assert.assertEquals(userListParent, userListChild);
    }

    @Test(expected = AuthenticationException.class)
    public void testInsertingChildItemItemWithInvalidPermission() throws AuthenticationException, DatabaseAccessException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());

        identityManager.createNewItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_CHILD_ID, VALID_ITEM_CHILD_TYPE, VALID_ITEM_ID, dbAuthChecker);
    }

    @Test
    public void testRegisteringUserInCourse() throws AuthenticationException, DatabaseAccessException, NoSuchAlgorithmException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.registerUserInItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);

        // Looks for group data
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        Assert.assertEquals(VALID_ITEM_ID, document.get(DatabaseStringConstants.COURSE_ID).toString());
        String salt = HashManager.generateUnSecureSalt(document.get(DatabaseStringConstants.COURSE_ID).toString());
        String teacherHash;
        String studentHash;

        teacherHash = HashManager.toHex(HashManager.createHash(TEACHER_USER_ID, salt).getBytes());
        studentHash = HashManager.toHex(HashManager.createHash(STUDENT_USER_ID, salt).getBytes());

        // Non users
        Document nonUsers = new Document();
        nonUsers.put(TEACHER_USER_ID, teacherHash);

        Document adminGroup = (Document) document.get(DatabaseStringConstants.NON_USER_LIST);
        Assert.assertEquals(nonUsers, adminGroup);

        // students
        Document students = new Document();
        students.put(STUDENT_USER_ID, studentHash);

        Document studentGroup = (Document) document.get(USER_LIST);
        Assert.assertEquals(students, studentGroup);

        List<String> userList = getUserGroup(dbItemObject);
        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), userList.get(0));
    }

    @Test(expected = AuthenticationException.class)
    public void testRegisteringUserInCourseInvalidKey() throws AuthenticationException, DatabaseAccessException {
        String userId = "New User!";
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        identityManager.registerUserInItem(STUDENT_USER_ID, userId, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidItemId() throws AuthenticationException, DatabaseAccessException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        identityManager.registerUserInItem(STUDENT_USER_ID, STUDENT_AUTH_ID, INVALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testRegisteringUserInCourseInvalidGroupId() throws AuthenticationException, DatabaseAccessException {
        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);

        // Remove group from the database
        final Document document = db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).find().first();

        System.out.println(document);
        db.getCollection(DatabaseStringConstants.USER_GROUP_COLLECTION).findOneAndDelete(document);

        // looks for item data
        final Document dbItemObject = db.getCollection(getCollectionFromType(VALID_ITEM_TYPE)).find(convertStringToObjectId(VALID_ITEM_ID)).first();
        Assert.assertEquals(VALID_ITEM_ID, dbItemObject.get(DatabaseStringConstants.COURSE_ID).toString());
        Assert.assertEquals(TEACHER_USER_ID, dbItemObject.get(DatabaseStringConstants.OWNER_ID).toString());

        AuthenticationHelper.setMockPermissions(authChecker, null, null, STUDENT_AUTH_ID, null, Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.registerUserInItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test
    public void createNewUser() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        Assert.assertEquals(1, results.size());

        final Document document = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find().first();

        Assert.assertEquals(VALID_USERNAME, document.get(DatabaseStringConstants.USER_NAME));

        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), next.getKey());
        Assert.assertTrue(HashManager.validateHash(next.getValue(), document.get(DatabaseStringConstants.PASSWORD).toString()));
    }

    @Test(expected = DatabaseAccessException.class)
    public void createNewUserThrowsExceptionIfUserExists() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        Assert.assertEquals(1, results.size());

        final Document document = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find().first();

        Assert.assertEquals(VALID_USERNAME, document.get(DatabaseStringConstants.USER_NAME));

        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        Assert.assertEquals(document.get(DatabaseStringConstants.SELF_ID).toString(), next.getKey());
        Assert.assertTrue(HashManager.validateHash(next.getValue(), document.get(DatabaseStringConstants.PASSWORD).toString()));

        identityManager.createNewUser(VALID_USERNAME);
    }

    @Test
    public void getUserIdentity() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        // user info is created correctly!
        Assert.assertEquals(1, results.size());

        final Document document = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find().first();

        Assert.assertEquals(VALID_USERNAME, document.get(DatabaseStringConstants.USER_NAME));

        // Gets the user info
        String userIdentity = identityManager.getUserIdentity(VALID_USERNAME, next.getValue());

        // checks that the user identity is grabbed correctly!
        Assert.assertEquals(next.getKey(), userIdentity);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserIdentityThrowsWhenInvalidUsername() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        Map<String, String> results = identityManager.createNewUser(VALID_USERNAME);
        final Map.Entry<String, String> next = results.entrySet().iterator().next();

        final Document document = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find().first();

        Assert.assertEquals(VALID_USERNAME, document.get(DatabaseStringConstants.USER_NAME));

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

        final Document document = db.getCollection(DatabaseStringConstants.USER_COLLECTION).find().first();


        Assert.assertEquals(VALID_USERNAME, document.get(DatabaseStringConstants.USER_NAME));

        // Gets the user info

        String userIdentity = identityManager.getUserIdentity(VALID_USERNAME, "invalid auth");

        // checks that the user identity is grabbed correctly!
        Assert.assertEquals(next.getKey(), userIdentity);
    }

    @Test(expected = AuthenticationException.class)
    public void getUserNameFromIdentityThrowsWhenInvalidPermisson()
            throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserNameFromIdentityThrowsWhenItemDoNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = AuthenticationException.class)
    public void getUserNameFromIdentityThrowsWhenUserDoNotExistInItem()
            throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, null);
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getUserNameFromIdentityThrowsWhenUserDoNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerUserInItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        identityManager.getUserName(STUDENT_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
    }

    @Test
    public void getUserNameFromIdentity() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        final String userId = identityManager.createNewUser(STUDENT_USER_ID).keySet().iterator().next();

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerUserInItem(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
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

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerUserInItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
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

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertFalse(identityManager.isUserInItem(STUDENT_USER_ID, true, VALID_ITEM_ID, VALID_ITEM_TYPE));
    }

    @Test
    public void isAdminInItem() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        identityManager.registerUserInItem(STUDENT_USER_ID, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        Assert.assertTrue(identityManager.isUserInItem(TEACHER_USER_ID, false, VALID_ITEM_ID, VALID_ITEM_TYPE));
    }

    @Test
    public void getCourseRoster() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String userName = "UserName" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash;
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
        for (String userId : userIds) {
            identityManager.registerUserInItem(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertEquals(userIdsToUserNames, userValues);
    }

    @Test
    public void getCourseRosterReturnsSubsetWhenGivenSubset() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();
        Set<String> subset = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            String userName = "UserName" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash;
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
        for (String userId : userIds) {
            identityManager.registerUserInItem(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, subset, dbAuthChecker);

        Assert.assertEquals(userIdsToUserNames, userValues);
    }

    @Test
    public void getCourseRosterDoesNotReturnUserNamesWhenPeer() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.PEER_TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String userName = "UserName" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash;
            try {
                final String unsecuredSalt = HashManager.generateUnSecureSalt(VALID_ITEM_ID);
                hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new AuthenticationException(e);
            }
            userIdsToUserNames.put(hash, null);
        }

        // adds user to the course
        for (String userId : userIds) {
            identityManager.registerUserInItem(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertEquals(userIdsToUserNames, userValues);
    }

    @Test(expected = AuthenticationException.class)
    public void getCourseRosterThrowsWhenNoValidPermission() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            String userName = "UserName1" + i;
            String userId = identityManager.createNewUser(userName).keySet().iterator().next();
            userIds.add(userId);
            String hash;
            try {
                final String unsecuredSalt = HashManager.generateUnSecureSalt(VALID_ITEM_ID);
                hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new AuthenticationException(e);
            }
            userIdsToUserNames.put(hash, null);
        }

        // adds user to the course
        for (String userId : userIds) {
            identityManager.registerUserInItem(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertEquals(userIdsToUserNames, userValues);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getCourseRosterThrowsWhenItemDoesNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {
        AuthenticationHelper.setMockPermissions(authChecker, null, null, null, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        identityManager.getItemRoster(TEACHER_AUTH_ID, INVALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getCourseRosterThrowsWhenUsersDoNotExist() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        Map<String, String> userIdsToUserNames = new HashMap<>();
        List<String> userIds = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            String userId = new ObjectId().toString();
            userIds.add(userId);
            String hash;
            try {
                final String unsecuredSalt = HashManager.generateUnSecureSalt(VALID_ITEM_ID);
                hash = HashManager.toHex(HashManager.createHash(userId, unsecuredSalt)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new AuthenticationException(e);
            }
            userIdsToUserNames.put(hash, null);
        }

        // adds user to the course
        for (String userId : userIds) {
            identityManager.registerUserInItem(userId, STUDENT_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, dbAuthChecker);
        }

        Map<String, String> userValues = identityManager.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
        Assert.assertEquals(userIdsToUserNames, userValues);
    }


    @Test(expected = DatabaseAccessException.class)
    public void getCourseRosterReturnsNothingWhenNoUsersInCourse() throws AuthenticationException, NoSuchAlgorithmException, DatabaseAccessException {

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, VALID_ITEM_TYPE, VALID_ITEM_ID, STUDENT_AUTH_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        identityManager.createNewItem(TEACHER_USER_ID, TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);

        identityManager.getItemRoster(TEACHER_AUTH_ID, VALID_ITEM_ID, VALID_ITEM_TYPE, null, dbAuthChecker);
    }
}
