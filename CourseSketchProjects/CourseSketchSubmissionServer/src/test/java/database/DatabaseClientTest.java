package database;

import com.github.fakemongo.junit.FongoRule;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.DB;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.commands.Commands;
import protobuf.srl.submission.Submission;
import util.SubmissionMergerTest;

import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseClientTest {

    @Rule
    public FongoRule fongoRule = new FongoRule();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUpdateListSubmitsCorrectly() throws DatabaseAccessException {

        // once you have a DB instance, you can interact with it
        // just like you would with a real one.
        DB db = fongoRule.getDB();
        DatabaseClient client = getMockedVersion(db);

        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setUpdateList(createSimpleDatabaseListWithSaveMarker(200));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = DatabaseClient.saveExperiment(expected, 200, client);

        Submission.SrlExperiment result = DatabaseClient.getExperiment(id, client);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testUpdateListAddsSaveMarker() throws DatabaseAccessException, InvalidProtocolBufferException {

        // once you have a DB instance, you can interact with it
        // just like you would with a real one.
        DB db = fongoRule.getDB();
        DatabaseClient client = getMockedVersion(db);

        final long submissionTime = Math.abs(new Random().nextLong());

        Submission.SrlSubmission.Builder usedList = Submission.SrlSubmission.newBuilder();
        usedList.setUpdateList(SubmissionMergerTest.createSimpleDatabaseList(100));
        Submission.SrlExperiment usedUpdate = getFakeExperiment("User1", usedList.build());
        String id = DatabaseClient.saveExperiment(usedUpdate, submissionTime, client);

        Submission.SrlExperiment result = DatabaseClient.getExperiment(id, client);

        Commands.SrlUpdateList resultList = result.getSubmission().getUpdateList();
        Commands.SrlUpdate lastUpdate = resultList.getList(resultList.getListCount() - 1);

        Commands.SrlCommand commands = lastUpdate.getCommands(0);
        // just a check to see if it is a marker
        Assert.assertEquals(Commands.CommandType.MARKER, commands.getCommandType());

        // just a silly check to see if it is Really a marker
        Commands.Marker marker = Commands.Marker.parseFrom(commands.getCommandData());

        Assert.assertEquals(submissionTime, lastUpdate.getTime());
    }

    @Test
    public void testUpdateListReplacesClientUpdateTime() throws DatabaseAccessException, InvalidProtocolBufferException {

        // once you have a DB instance, you can interact with it
        // just like you would with a real one.
        DB db = fongoRule.getDB();
        DatabaseClient client = getMockedVersion(db);

        final long submissionTime = Math.abs(new Random().nextLong()) + 200;

        Submission.SrlSubmission.Builder usedList = Submission.SrlSubmission.newBuilder();
        Commands.SrlUpdateList withSave = createSimpleDatabaseListWithSaveMarker(100);
        usedList.setUpdateList(withSave);
        Submission.SrlExperiment usedUpdate = getFakeExperiment("User1", usedList.build());
        String id = DatabaseClient.saveExperiment(usedUpdate, submissionTime, client);

        Submission.SrlExperiment result = DatabaseClient.getExperiment(id, client);

        Commands.SrlUpdateList resultList = result.getSubmission().getUpdateList();
        Commands.SrlUpdate lastUpdate = resultList.getList(resultList.getListCount() - 1);

        Commands.SrlCommand commands = lastUpdate.getCommands(0);
        // just a check to see if it is a marker
        Assert.assertEquals(Commands.CommandType.MARKER, commands.getCommandType());

        // just a silly check to see if it is Really a marker
        Commands.Marker marker = Commands.Marker.parseFrom(commands.getCommandData());

        Assert.assertEquals(submissionTime, lastUpdate.getTime());

        Commands.SrlUpdate clientUpdate = withSave.getList(resultList.getListCount() - 1);

        Assert.assertNotEquals(clientUpdate.getTime(), lastUpdate.getTime());
    }

    public static Commands.SrlUpdateList createSimpleDatabaseListWithSaveMarker(long submissionTime) {
        Commands.SrlUpdateList fakeList = SubmissionMergerTest.createSimpleDatabaseList(100);
        Commands.SrlUpdateList.Builder newList = Commands.SrlUpdateList.newBuilder(fakeList);

        // create a new marker with the save list item.
        final Commands.SrlUpdate.Builder saveUpdate = Commands.SrlUpdate.newBuilder();
        saveUpdate.setUpdateId(AbstractServerWebSocketHandler.Encoder.nextID().toString());
        saveUpdate.setTime(submissionTime);

        final Commands.Marker saveMarker = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SAVE).build();
        final Commands.SrlCommand saveCommand = Commands.SrlCommand.newBuilder().setCommandType(Commands.CommandType.MARKER).setCommandId(
                AbstractServerWebSocketHandler.Encoder.nextID().toString()).setCommandData(saveMarker.toByteString())
                .setIsUserCreated(false).build();

        saveUpdate.addCommands(saveCommand);

        newList.addList(saveUpdate);
        return newList.build();
    }

    public Submission.SrlExperiment getFakeExperiment(String userId, Submission.SrlSubmission sub) {
        Submission.SrlExperiment.Builder build = Submission.SrlExperiment.newBuilder();
        build.setUserId(userId).setCourseId("Course1").setAssignmentId("Assignment1").setProblemId("Problem1")
                .setSubmission(sub);
        return build.build();
    }

    public DatabaseClient getMockedVersion(DB fakeDb) {
        DatabaseClient cl = mock(DatabaseClient.class);
        when(cl.getDb()).thenReturn(fakeDb);
        return cl;
    }
}
