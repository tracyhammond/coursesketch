package database.auth;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

/**
 * Created by dtracers on 9/17/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class DbAuthCheckerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public static final School.ItemType INVALID_ITEM_TYPE = School.ItemType.LECTURE;
    public static final School.ItemType VALID_ITEM_TYPE = School.ItemType.COURSE;

    public static final String INVALID_ITEM_ID = new ObjectId().toHexString();
    public static final String VALID_ITEM_ID = new ObjectId().toHexString();

    public static final String TEACHER_ID = new ObjectId().toHexString();
    public static final String STUDENT_ID = new ObjectId().toHexString();
    public static final String MOD_ID = new ObjectId().toHexString();

    // this user id is not in the db
    public static final String NO_ACCESS_ID = new ObjectId().toHexString();

    public DB db;

    public DbAuthChecker authChecker;

    @Before
    public void before() {
        db = fongo.getDB();
        authChecker = new DbAuthChecker(db);
    }

    @Test(expected = DatabaseAccessException.class)
    public void databaseExceptionThrownWhenNoDataExists() throws DatabaseAccessException, AuthenticationException {
        authChecker.isAuthenticated(INVALID_ITEM_TYPE, VALID_ITEM_ID, TEACHER_ID, Authentication.AuthType.getDefaultInstance());
    }
}
