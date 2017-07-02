import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.DatabaseClient;
import coursesketch.database.LoginException;
import coursesketch.database.RegistrationException;
import coursesketch.database.identity.IdentityManagerInterface;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.INSTRUCTOR_CLIENT_ID;
import static database.DatabaseStringConstants.INSTRUCTOR_ID;
import static database.DatabaseStringConstants.STUDENT_CLIENT_ID;
import static database.DatabaseStringConstants.STUDENT_ID;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by dtracers on 11/6/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseClientTest {

    private static final String VALID_USERNAME = "ValidUser";
    private static final String VALID_USER_IDENTITY = "ValidUserId";
    private static final String VALID_PASSWORD = "ValidPassword";
    private static final String VALID_IDENTITY_AUTH = "ValidIdentity";
    private static final String VALID_EMAIL = "ValidEmail";
    private static final boolean INSTRUCTOR = true;
    private static final boolean STUDENT = false;
    private static final String INVALID_USERNAME = "InvalidUser";
    private static final String INVALID_PASSWORD = "InvalidPassword";
    @Rule
    public FongoRule fongo = new FongoRule();

    @Mock
    IdentityManagerInterface identityManager;

    Map<String, String> createUserResult;

    public MongoDatabase db;
    DatabaseClient client;

    @Before
    public void before() throws Exception {
        db = fongo.getDatabase();
        client = new DatabaseClient(true, db, identityManager);

        createUserResult = new HashMap<>();
        createUserResult.put(VALID_USER_IDENTITY, VALID_IDENTITY_AUTH);

        when(identityManager.createNewUser(VALID_USERNAME)).thenReturn(createUserResult);

        when(identityManager.getUserIdentity(VALID_USERNAME, VALID_IDENTITY_AUTH)).thenReturn(VALID_USER_IDENTITY);
    }

    @Test
    public void createUserInsertsUserInfo() throws Exception {
        String userId = client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);
        final Document obj = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();
        Assert.assertEquals(VALID_USERNAME, obj.get(DatabaseStringConstants.USER_NAME));
        Assert.assertEquals(VALID_EMAIL, obj.get(DatabaseStringConstants.EMAIL));
        Assert.assertEquals(INSTRUCTOR, obj.get(DatabaseStringConstants.IS_DEFAULT_INSTRUCTOR));

        Assert.assertTrue(HashManager.validateHash(VALID_PASSWORD, (String) obj.get(DatabaseStringConstants.PASSWORD)));
        Assert.assertTrue(obj.containsKey(INSTRUCTOR_ID));
        Assert.assertTrue(obj.containsKey(STUDENT_ID));
        Assert.assertTrue(obj.containsKey(STUDENT_CLIENT_ID));
        Assert.assertTrue(obj.containsKey(INSTRUCTOR_CLIENT_ID));
        Assert.assertEquals(obj.get(DatabaseStringConstants.IDENTITY_AUTH), VALID_IDENTITY_AUTH);
        Assert.assertEquals(VALID_USER_IDENTITY, userId);
    }

    @Test(expected = RegistrationException.class)
    public void createUserThrowsExceptionIfUserNameExists() throws Exception {
        String userId = client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);
        final Document obj = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();
        Assert.assertEquals(VALID_USERNAME, obj.get(DatabaseStringConstants.USER_NAME));
        Assert.assertEquals(VALID_EMAIL, obj.get(DatabaseStringConstants.EMAIL));
        Assert.assertEquals(INSTRUCTOR, obj.get(DatabaseStringConstants.IS_DEFAULT_INSTRUCTOR));

        Assert.assertTrue(HashManager.validateHash(VALID_PASSWORD, (String) obj.get(DatabaseStringConstants.PASSWORD)));
        Assert.assertTrue(obj.containsKey(INSTRUCTOR_ID));
        Assert.assertTrue(obj.containsKey(STUDENT_ID));
        Assert.assertTrue(obj.containsKey(STUDENT_CLIENT_ID));
        Assert.assertTrue(obj.containsKey(INSTRUCTOR_CLIENT_ID));
        Assert.assertEquals(obj.get(DatabaseStringConstants.IDENTITY_AUTH), VALID_IDENTITY_AUTH);
        Assert.assertEquals(VALID_USER_IDENTITY, userId);

        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);
    }

    @Test(expected = RegistrationException.class)
    public void createUserThrowsExceptionIfIdentityServerFailsToSendValue() throws Exception {
        createUserResult.clear();
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);
    }

    @Test
    public void userLoggedInSuccessfullyAddsUserLoginTimeInstructor() throws Exception {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        final Document obj = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String authId = (String) obj.get(INSTRUCTOR_ID);
        long time = System.currentTimeMillis();

        // Actual Test
        client.userLoggedInSuccessfully(VALID_USERNAME, authId, true, Arrays.asList(time));

        final Document obj2 = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        Assert.assertEquals(1, obj2.get(DatabaseStringConstants.LOGIN_AMOUNT_FIELD));
        List<Long> times = (List<Long>) obj2.get(DatabaseStringConstants.LAST_LOGIN_TIMES);
        System.out.println(times);
        Assert.assertEquals(1, times.size());
        Assert.assertEquals(time, (long) times.get(0));
    }

    @Test
    public void userLoggedInSuccessfullyAddsUserLoginTimeStudent() throws Exception {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        final Document obj = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String authId = (String) obj.get(STUDENT_ID);
        long time = System.currentTimeMillis();

        // Actual Test
        client.userLoggedInSuccessfully(VALID_USERNAME, authId, false, Arrays.asList(time));

        final Document obj2 = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        Assert.assertEquals(1, obj2.get(DatabaseStringConstants.LOGIN_AMOUNT_FIELD));
        List<Long> times = (List<Long>) obj2.get(DatabaseStringConstants.LAST_LOGIN_TIMES);
        System.out.println(times);
        Assert.assertEquals(1, times.size());
        Assert.assertEquals(time, (long) times.get(0));
    }

    @Test
    public void userLoggedLimitsLoginsToTen() throws Exception {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        final Document obj = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String authId = (String) obj.get(STUDENT_ID);
        List<Long> times = new ArrayList<>();
        for (int k = 0; k < 11; k++) {
            times.add(System.currentTimeMillis());
        }

        // Actual Test
        client.userLoggedInSuccessfully(VALID_USERNAME, authId, false, times);

        final Document obj2 = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        Assert.assertEquals(1, obj2.get(DatabaseStringConstants.LOGIN_AMOUNT_FIELD));
        List<Long> outpuTimes = (List<Long>) obj2.get(DatabaseStringConstants.LAST_LOGIN_TIMES);
        Assert.assertEquals(10, outpuTimes.size());

        // checks to make sure the sorting works.
        Assert.assertEquals((long) times.get(times.size() - 1), (long) outpuTimes.get(0));
    }

    @Test(expected = LoginException.class)
    public void loggingInThrowsExceptionIfInvalidUsernameIsUsed()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        client.mongoIdentify(INVALID_USERNAME, VALID_PASSWORD, true, true);
    }

    @Test(expected = LoginException.class)
    public void loggingInThrowsExceptionIfIdentityServerThrowsException()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        when(identityManager.getUserIdentity(anyString(), anyString())).thenThrow(DatabaseAccessException.class);

        client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, true, true);
    }

    @Test(expected = LoginException.class)
    public void loggingInThrowsExceptionIfInvalidPasswordIsUsed()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        client.mongoIdentify(VALID_USERNAME, INVALID_PASSWORD, true, true);
    }

    @Test
    public void goodValuesAreReturnedIfLoginIsSuccessfulAsInstructorNoDefault()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        final Document actual = client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, false, INSTRUCTOR);

        final Document expected = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String serverId = (String) expected.get(INSTRUCTOR_ID);
        String clientId = (String) expected.get(INSTRUCTOR_CLIENT_ID);

        Assert.assertEquals(serverId, actual.get(DatabaseClient.SERVER_ID));
        Assert.assertEquals(clientId, actual.get(DatabaseClient.CLIENT_ID));
        Assert.assertEquals(INSTRUCTOR, actual.get(DatabaseClient.IS_INSTRUCTOR));

        Assert.assertEquals(VALID_USER_IDENTITY, actual.get(DatabaseStringConstants.USER_ID));
    }

    @Test
    public void goodValuesAreReturnedIfLoginIsSuccessfulAsDefaultInstructor()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        final Document actual = client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, true, STUDENT);

        final Document expected = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String serverId = (String) expected.get(INSTRUCTOR_ID);
        String clientId = (String) expected.get(INSTRUCTOR_CLIENT_ID);

        Assert.assertEquals(serverId, actual.get(DatabaseClient.SERVER_ID));
        Assert.assertEquals(clientId, actual.get(DatabaseClient.CLIENT_ID));
        Assert.assertEquals(INSTRUCTOR, actual.get(DatabaseClient.IS_INSTRUCTOR));

        Assert.assertEquals(VALID_USER_IDENTITY, actual.get(DatabaseStringConstants.USER_ID));
    }

    @Test
    public void goodValuesAreReturnedIfLoginIsSuccessfulAsDefaultStudentForcedInstructor()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, STUDENT);

        final Document actual = client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, false, INSTRUCTOR);

        final Document expected = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String serverId = (String) expected.get(INSTRUCTOR_ID);
        String clientId = (String) expected.get(INSTRUCTOR_CLIENT_ID);

        Assert.assertEquals(serverId, actual.get(DatabaseClient.SERVER_ID));
        Assert.assertEquals(clientId, actual.get(DatabaseClient.CLIENT_ID));
        Assert.assertEquals(INSTRUCTOR, actual.get(DatabaseClient.IS_INSTRUCTOR));

        Assert.assertEquals(VALID_USER_IDENTITY, actual.get(DatabaseStringConstants.USER_ID));
    }

    @Test
    public void goodValuesAreReturnedIfLoginIsSuccessfulAsNoDefaultStudent()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, INSTRUCTOR);

        final Document actual = client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, false, STUDENT);

        final Document expected = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String serverId = (String) expected.get(STUDENT_ID);
        String clientId = (String) expected.get(STUDENT_CLIENT_ID);

        Assert.assertEquals(serverId, actual.get(DatabaseClient.SERVER_ID));
        Assert.assertEquals(clientId, actual.get(DatabaseClient.CLIENT_ID));
        Assert.assertEquals(STUDENT, actual.get(DatabaseClient.IS_INSTRUCTOR));

        Assert.assertEquals(VALID_USER_IDENTITY, actual.get(DatabaseStringConstants.USER_ID));
    }

    @Test
    public void goodValuesAreReturnedIfLoginIsSuccessfulAsDefaultStudent()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, STUDENT);

        final Document actual = client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, true, STUDENT);

        final Document expected = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String serverId = (String) expected.get(STUDENT_ID);
        String clientId = (String) expected.get(STUDENT_CLIENT_ID);

        Assert.assertEquals(serverId, actual.get(DatabaseClient.SERVER_ID));
        Assert.assertEquals(clientId, actual.get(DatabaseClient.CLIENT_ID));
        Assert.assertEquals(STUDENT, actual.get(DatabaseClient.IS_INSTRUCTOR));

        Assert.assertEquals(VALID_USER_IDENTITY, actual.get(DatabaseStringConstants.USER_ID));
    }


    @Test
    public void goodValuesAreReturnedIfLoginIsSuccessfulAsDefaultStudentWithInstructorIgnored()
            throws Exception, LoginException {
        client.createUser(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL, STUDENT);

        final Document actual = client.mongoIdentify(VALID_USERNAME, VALID_PASSWORD, true, INSTRUCTOR);

        final Document expected = db.getCollection(DatabaseStringConstants.LOGIN_COLLECTION)
                .find(new Document(DatabaseStringConstants.USER_NAME, VALID_USERNAME)).first();

        String serverId = (String) expected.get(STUDENT_ID);
        String clientId = (String) expected.get(STUDENT_CLIENT_ID);

        Assert.assertEquals(serverId, actual.get(DatabaseClient.SERVER_ID));
        Assert.assertEquals(clientId, actual.get(DatabaseClient.CLIENT_ID));
        Assert.assertEquals(STUDENT, actual.get(DatabaseClient.IS_INSTRUCTOR));

        Assert.assertEquals(VALID_USER_IDENTITY, actual.get(DatabaseStringConstants.USER_ID));
    }
}
