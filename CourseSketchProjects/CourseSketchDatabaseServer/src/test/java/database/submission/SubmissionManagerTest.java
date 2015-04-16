package database.submission;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.commands.Commands;
import protobuf.srl.submission.Submission;
import protobuf.srl.tutorial.TutorialOuterClass;

import static database.DatabaseStringConstants.*;

/**
 * Created by kyle on 4/14/15.
 */
public class SubmissionManagerTest {
    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;

    TutorialOuterClass.Tutorial.Builder tutorialObject;
    String id = "randomWhateverWeDontCare";
    String name = "TestTutorial";
    String url = "http://www.reddit.com";
    String description = "This is a test lol";
    Commands.SrlUpdateList.Builder steps;
    BasicDBObject fakeDBObject = new BasicDBObject();

    @Before
    public void before() {
        db = fongo.getDB();
        fauth = new Authenticator(new MongoAuthenticator(fongo.getDB()));

        tutorialObject = TutorialOuterClass.Tutorial.newBuilder();
        tutorialObject.setId(id);
        tutorialObject.setName(name);
        tutorialObject.setUrl(url);
        tutorialObject.setDescription(description);
        steps = Commands.SrlUpdateList.newBuilder();
        tutorialObject.setSteps(steps.build().toByteString());

        fakeDBObject = new BasicDBObject(DESCRIPTION, tutorialObject.getDescription()).append(NAME, tutorialObject.getName())
                .append(URL, tutorialObject.getUrl()).append(URL_HASH, tutorialObject.getUrl().hashCode())
                .append(UPDATELIST, tutorialObject.getSteps().toByteArray());

    }

    @Test
    public void insertTutorial() throws Exception {
        String tutorialObjectId = SubmissionManager.mongoInsertTutorial(fauth, db, "userId", tutorialObject.build());

        fakeDBObject.append(SELF_ID, new ObjectId(tutorialObjectId));
        final DBRef myDbRef = new DBRef(db, TUTORIAL_COLLECTION, new ObjectId(tutorialObjectId));
        final DBObject testDBObject = myDbRef.fetch();

        Assert.assertEquals(fakeDBObject, testDBObject);
    }

    @Test
    public void getTutorial() throws Exception {
        String tutorialObjectId = SubmissionManager.mongoInsertTutorial(fauth, db, "userId", tutorialObject.build());

        TutorialOuterClass.Tutorial tutorial = SubmissionManager.mongoGetTutorial(fauth, db, "userId", tutorialObjectId);
        tutorialObject.setId(tutorialObjectId);
        Assert.assertEquals(tutorialObject.build(), tutorial);
    }

    @Test(expected = DatabaseAccessException.class)
    public void getTutorialDoesNotExist() throws DatabaseAccessException, AuthenticationException {
        SubmissionManager.mongoGetTutorial(fauth, db, "userId", new ObjectId().toString());
    }
}
