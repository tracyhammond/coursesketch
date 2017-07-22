package utilities;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.RequestConverter;
import coursesketch.database.util.institution.mongo.MongoInstitution;
import coursesketch.database.util.user.UserClient;
import local.data.LocalAddAssignments;
import org.bson.Document;
import org.bson.types.ObjectId;
import protobuf.srl.school.School;
import protobuf.srl.utils.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import static coursesketch.database.util.DatabaseStringConstants.*;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;

/**
 * Lets break DATABASES!!!!!!!!
 * Created by Rauank on 4/8/15.
 */
public final class BreakDatabase {

    private MongoInstitution mongoDatabase;
    private MongoDatabase database;
    private UserClient userClient;

    public BreakDatabase(final MongoDatabase db) {
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

        MongoCollection<Document> courseCollection = database.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        courseCollection.deleteOne(new Document(SELF_ID, new ObjectId(courseID)));
        String[] returnID = {user.getUsername(), courseID};
        return returnID;
    }

    public String[] invalidCourseAuthentication() throws DatabaseAccessException, AuthenticationException {
        School.SrlUser user = createRandomUser();
        School.SrlCourse course = createRandomCourse();

        userClient.insertUser(user, user.getUsername());
        String courseID = mongoDatabase.insertCourse(null, user.getUsername(), course);
        mongoDatabase.putUserInCourse(null, user.getUsername(), courseID, null);

        MongoCollection<Document> courseCollection = database.getCollection(getCollectionFromType(Util.ItemType.COURSE));
        Document dbCourse = courseCollection.find().first();
        courseCollection.updateOne(dbCourse, new Document(SET_COMMAND, new Document(ADMIN, new ArrayList<>())));
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
        testBuilder.setAccess(Util.Accessibility.valueOf(r.nextInt(Util.Accessibility.values().length)));
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
            testBuilder.setAccess(Util.Accessibility.SUPER_PUBLIC);
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
