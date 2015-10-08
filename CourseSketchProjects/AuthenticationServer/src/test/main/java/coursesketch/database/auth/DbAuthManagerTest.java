package coursesketch.database.auth;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import database.DatabaseStringConstants;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static database.DbSchoolUtility.getCollectionFromType;

/**
 * Created by dtracers on 10/8/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DbAuthManagerTest {

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
        insertValidGroup(VALID_GROUP_ID, VALID_ITEM_ID,
                createPermission(TEACHER_ID, Authentication.AuthResponse.PermissionLevel.TEACHER),
                createPermission(STUDENT_ID, Authentication.AuthResponse.PermissionLevel.STUDENT),
                createPermission(MOD_ID, Authentication.AuthResponse.PermissionLevel.MODERATOR));
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

}
