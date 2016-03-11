package utilities;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationException;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import local.data.LocalAddAssignments;
import org.bson.types.ObjectId;
import protobuf.srl.school.School;
import protobuf.srl.utils.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import static database.DatabaseStringConstants.*;
import static database.DbSchoolUtility.getCollectionFromType;

/**
 * Lets break DATABASES!!!!!!!!
 * Created by Rauank on 4/8/15.
 */
public final class BreakDatabase {

    private MongoInstitution mongoDatabase;
    private DB database;
    private UserClient userClient;

    public BreakDatabase(final DB db) {
        mongoDatabase = new MongoInstitution(null, null, null, null);
        database = db;
        userClient = new UserClient(true, db);
    }

    /**
     * Creates a random user.
     * @return
     *      A random SrlUser object.
     */
    public School.SrlUser createRandomUser() {
        Random r = new Random();
        School.SrlUser.Builder testUser = School.SrlUser.newBuilder();
        testUser.setUsername(randomString(r) + randomString(r));
        testUser.setEmail(randomString(r) + "@" + randomString(r) + ".com");
        testUser.setSchoolIdentity(randomString(r));
        testUser.setFirstName(randomString(r));
        testUser.setLastName(randomString(r));
        return testUser.build();
    }

    /**
     * Registers a user for a course that doesn't exist.
     * @return
     *      returns a String[] with the userID & courseID (in this order)
     * @throws DatabaseAccessException
     */
    public String[] invalidCourse() throws DatabaseAccessException, AuthenticationException {
        School.SrlUser user = createRandomUser();
        School.SrlCourse course = createRandomCourse();

        userClient.insertUser(user, user.getUsername());
        String courseID = mongoDatabase.insertCourse(null, user.getUsername(), course);
        mongoDatabase.putUserInCourse(null, user.getUsername(), courseID, null);

        DBCollection collection = database.getCollection(getCollectionFromType(School.ItemType.COURSE));
        collection.remove(new BasicDBObject(SELF_ID, new ObjectId(courseID)));
        String[] returnID = {user.getUsername(), courseID};
        return returnID;
    }

    public String[] invalidCourseAuthentication() throws DatabaseAccessException, AuthenticationException {
        School.SrlUser user = createRandomUser();
        School.SrlCourse course = createRandomCourse();

        userClient.insertUser(user, user.getUsername());
        String courseID = mongoDatabase.insertCourse(null, user.getUsername(), course);
        mongoDatabase.putUserInCourse(null, user.getUsername(), courseID, null);

        DBCollection collection = database.getCollection(getCollectionFromType(School.ItemType.COURSE));
        DBObject dbCourse = collection.findOne();
        collection.update(dbCourse, new BasicDBObject(SET_COMMAND, new BasicDBObject(ADMIN, new ArrayList<>())));
        String[] returnID = {user.getUsername(), courseID};
        return returnID;
    }

    public String randomString(Random r){
        return new BigInteger(32, r).toString(32);
    }

    public School.SrlCourse createRandomCourse() {
        Random r = new Random();
        School.SrlCourse.Builder testBuilder = School.SrlCourse.newBuilder();
        testBuilder.setId(randomString(r));
        testBuilder.setAccess(School.SrlCourse.Accessibility.valueOf(r.nextInt(School.SrlCourse.Accessibility.values().length)));
        testBuilder.setSemester(randomString(r));
        testBuilder.setName(randomString(r));
        testBuilder.setDescription(randomString(r) + randomString(r));
        testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds(0));
        testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds(315576000000000L));
        Util.SrlPermission.Builder permissions = Util.SrlPermission.newBuilder();

        testBuilder.setAccessPermission(permissions.build());
        return testBuilder.build();
    }

    public static void testCourses(String instructionID) throws DatabaseAccessException {
        String[] name = new String[]{"Chem 107"};
        String[] description = new String[]{"Howdy! Welcome to Chem 107 where you learn about lewis dot diagrams"};
        for (int k = 0; k < name.length; k ++) {
            School.SrlCourse.Builder testBuilder = School.SrlCourse.newBuilder();
            testBuilder.setAccess(School.SrlCourse.Accessibility.SUPER_PUBLIC);
            testBuilder.setSemester("FALL");
            testBuilder.setName(name[k]);
            testBuilder.setDescription(description[k]);
            testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds(0));
            testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds(315576000000000L));
            Util.SrlPermission.Builder permissions = Util.SrlPermission.newBuilder();

            testBuilder.setAccessPermission(permissions.build());
            System.out.println(testBuilder.toString());

            // testing inserting course
            System.out.println("INSERTING COURSE");
            String courseId = MongoInstitution.getInstance(null).insertCourse(null, instructionID, testBuilder.buildPartial());
            System.out.println("INSERTING COURSE SUCCESSFUL");
            System.out.println(courseId);
            LocalAddAssignments.testAssignments(courseId, instructionID);
        }
    }
}
