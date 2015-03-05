package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import protobuf.srl.school.School.SrlBankProblem;

import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.SCRIPT;
import static database.institution.mongo.BankProblemManager.mongoGetBankProblem;
import static database.institution.mongo.BankProblemManager.mongoInsertBankProblem;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;

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
    public void testGetScript() throws AuthenticationException {
        DB db = fongoRule.getDB();
        final SrlBankProblem problem = SrlBankProblem.newBuilder()
                .setId("666")
                .setScript("this is a script")
                .build();
        mongoInsertBankProblem(db, problem);
        Authenticator auth = PowerMock.createMock(Authenticator.class);
        EasyMock.expect(auth.checkAuthentication(anyString(), anyListOf(String.class))).andReturn(true);
        final SrlBankProblem getProblem = mongoGetBankProblem(auth ,db, "666", "User1");
        String testString = getProblem.getScript().toString();
        Assert.assertEquals("this is a script", testString);
    }

}
