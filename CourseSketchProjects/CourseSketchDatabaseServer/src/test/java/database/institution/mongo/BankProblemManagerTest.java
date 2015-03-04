package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.school.School.SrlBankProblem;

import static database.DatabaseStringConstants.PROBLEM_BANK_COLLECTION;
import static database.DatabaseStringConstants.SCRIPT;
import static database.institution.mongo.BankProblemManager.mongoGetBankProblem;
import static database.institution.mongo.BankProblemManager.mongoInsertBankProblem;

public class BankProblemManagerTest {

    @Rule
    public FongoRule fongoRule = new FongoRule();

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

    /*public void testGetScript() throws AuthenticationException {
        DB db = fongoRule.getDB();
        final SrlBankProblem problem = SrlBankProblem.newBuilder()
                .setId("666")
                .setScript("this is a script")
                .build();
        mongoInsertBankProblem(db, problem);
        final SrlBankProblem getProblem = mongoGetBankProblem(, db, "666", );
        String testString = getProblem.Script().toString();
        Assert.assertEquals("this is a script", testString);
    }*/

}
