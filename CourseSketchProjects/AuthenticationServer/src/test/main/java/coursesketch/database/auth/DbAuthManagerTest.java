package coursesketch.database.auth;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.junit.Assert;
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

import static database.DbSchoolUtility.getCollectionFromType;

/**
 * Created by dtracers on 10/8/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DbAuthManagerTest {

    private static final String VALID_REGISTRATION_KEY = "VALID KEY YO";
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

    public DbAuthManager dbAuthManager;

    @Before
    public void before() throws UnknownHostException {

        db = fongo.getDB(); // new MongoClient("localhost").getDB("test");
        dbAuthManager = new DbAuthManager(db);
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
    public void testGroupCreation() throws AuthenticationException {
        dbAuthManager.createNewGroup(VALID_ITEM_ID, VALID_ITEM_TYPE, TEACHER_ID);
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
        } catch (InvalidKeySpecException e) {
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
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(dbObject.containsField(hash));
        Assert.assertEquals(Authentication.AuthResponse.PermissionLevel.TEACHER_VALUE, dbObject.get(hash));

        List<String> userList = (List<String>) dbItemObject.get(DatabaseStringConstants.USER_LIST);
        Assert.assertEquals(dbObject.get(DatabaseStringConstants.SELF_ID), userList.get(0));
    }
}
