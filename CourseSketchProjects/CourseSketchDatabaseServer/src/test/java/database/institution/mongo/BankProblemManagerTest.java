package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import protobuf.srl.school.School.SrlBankProblem;

import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.SCRIPT;
import static database.institution.mongo.BankProblemManager.mongoGetBankProblem;
import static database.institution.mongo.BankProblemManager.mongoInsertBankProblem;
import static database.institution.mongo.BankProblemManager.mongoUpdateBankProblem;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNotNull;
import static org.powermock.api.easymock.PowerMock.expectPrivate;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Authenticator.class)

public class BankProblemManagerTest {

    @Rule
    public FongoRule fongoRule = new FongoRule();

    /*
     * testSetScript tests setScript member function by inserting and then retreiving (directly from the datebase) the
     * value that was inserted.
     */
    @Test
    public void testSetScript() throws Exception {

        DB db = fongoRule.getDB();
        final SrlBankProblem problem = SrlBankProblem.newBuilder()
                .setId("4554")
                .setScript("this is a script")
                .build();
        mongoInsertBankProblem(db, problem);
        DBCursor curse = db.getCollection(PROBLEM_BANK_COLLECTION).find();
        System.out.println(curse);
        DBObject obj = curse.next();
        String testString = obj.get(SCRIPT).toString();
        Assert.assertEquals("this is a script", testString);
    }

    /*
     * testGetScript tests getScript member function by inserting and then getting the value that was inserted.
     */
    @Test
    public void testGetScript() throws Exception {
        String id = "507f191e810c19729de860ea";
        DB db = fongoRule.getDB();
        final SrlBankProblem problem = SrlBankProblem.newBuilder()
                .setId(id)
                .setScript("old script")
                .build();
        mongoInsertBankProblem(db, problem);
        Authenticator auth = PowerMock.createMock(Authenticator.class);
        Assert.assertFalse(auth == null);
        Assert.assertFalse(db == null);
        expectPrivate(auth.checkAuthentication(anyString(), anyListOf(String.class))).andReturn(true);
        PowerMock.replayAll();

        SrlBankProblem getProblem = mongoGetBankProblem(auth, db, id, new String("User1"));
        String testString = getProblem.getScript().toString();

        PowerMock.verifyAll();
        Assert.assertEquals("this is a script", testString);
    }

    /*
     * testUpdateScript tests updateScript member function by inserting, updating,
     * and then getting the value that was inserted.
     */
    @Test
    public void testUpdateScript () throws Exception {
        /*DB db = fongoRule.getDB();
        final SrlBankProblem problem = SrlBankProblem.newBuilder()
                .setId("666")
                .setScript("old script")
                .build();
        mongoInsertBankProblem(db, problem);
        final SrlBankProblem updatedProblem = SrlBankProblem.newBuilder()
                .setId("667")
                .setScript("updated script")
                .build();
        final Authenticator auth = PowerMock.createMock(Authenticator.class);
        when(auth.checkAuthentication(anyString(), anyListOf(String.class))).thenReturn(true);
        mongoUpdateBankProblem(auth, db, "666", "User1", updatedProblem);
        final SrlBankProblem getProblem = mongoGetBankProblem(auth ,db, "667", "User1");
        String testString = getProblem.getScript().toString();
        Assert.assertEquals("updated", testString);*/

    }

}
