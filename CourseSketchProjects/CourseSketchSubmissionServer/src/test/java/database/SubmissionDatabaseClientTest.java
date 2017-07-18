package database;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.server.interfaces.AbstractServerWebSocketHandler;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.commands.Commands;
import protobuf.srl.question.QuestionDataOuterClass.MultipleChoice;
import protobuf.srl.question.QuestionDataOuterClass.FreeResponse;
import protobuf.srl.question.QuestionDataOuterClass.QuestionData;
import protobuf.srl.question.QuestionDataOuterClass.SketchArea;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.submission.Submission;
import util.SubmissionMergerTest;

import java.util.Random;

import static database.DatabaseStringConstants.FIRST_STROKE_TIME;
import static database.DatabaseStringConstants.FIRST_SUBMISSION_TIME;
import static database.SubmissionDatabaseClient.createUpdateList;

public class SubmissionDatabaseClientTest {
    @Rule
    public FongoRule fongoRule = new FongoRule();

    private AuthenticationResponder responder;

    private static final String PROBLEM_ID = new ObjectId().toString();
    private static final String ASSIGNMENT_ID = new ObjectId().toString();
    private static final String COURSE_ID = new ObjectId().toString();

    private SubmissionDatabaseClient databaseClient;

    @Before
    public void before() {
        databaseClient = new SubmissionDatabaseClient(true, fongoRule.getDatabase());
        responder = new AuthenticationResponder(Authentication.AuthResponse.getDefaultInstance());
    }

    private static Commands.SrlUpdateList createSimpleDatabaseListWithSaveMarker(long submissionTime) {
        Commands.SrlUpdateList fakeList = SubmissionMergerTest.createSimpleDatabaseList(100);
        return addSaveMarker(fakeList, submissionTime);
    }

    private static Commands.SrlUpdateList createSimpleDatabaseListWithSubmitMarker(long submissionTime) {
        Commands.SrlUpdateList fakeList = SubmissionMergerTest.createSimpleDatabaseList(100);
        return addSubmitMarker(fakeList, submissionTime);
    }

    /**
     * Adds a save marker to the end of the list so that it has an update time that is the same as the submission time.
     */
    private static Commands.SrlUpdateList addSaveMarker(Commands.SrlUpdateList fakeList, long submissionTime) {
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

    /**
     * Adds a submit marker to the end of the list so that it has an update time that is the same as the submission time.
     */
    private static Commands.SrlUpdateList addSubmitMarker(Commands.SrlUpdateList fakeList, long submissionTime) {
        Commands.SrlUpdateList.Builder newList = Commands.SrlUpdateList.newBuilder(fakeList);

        // create a new marker with the save list item.
        final Commands.SrlUpdate.Builder submitUpdate = Commands.SrlUpdate.newBuilder();
        submitUpdate.setUpdateId(AbstractServerWebSocketHandler.Encoder.nextID().toString());
        submitUpdate.setTime(submissionTime);

        final Commands.Marker submitMarker = Commands.Marker.newBuilder().setType(Commands.Marker.MarkerType.SUBMISSION).build();
        final Commands.SrlCommand submitCommand = Commands.SrlCommand.newBuilder().setCommandType(Commands.CommandType.MARKER).setCommandId(
                AbstractServerWebSocketHandler.Encoder.nextID().toString()).setCommandData(submitMarker.toByteString())
                .setIsUserCreated(false).build();

        submitUpdate.addCommands(submitCommand);

        newList.addList(submitUpdate);
        return newList.build();
    }

    @Test
    public void testUpdateListSubmitsCorrectly() throws Exception {
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(createSimpleDatabaseListWithSaveMarker(200))));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);
        new ProtobufComparisonBuilder()
                .ignoreField(Submission.SrlSubmission.getDescriptor(), Submission.SrlSubmission.ID_FIELD_NUMBER)
                .ignoreField(Submission.SrlExperiment.getDescriptor(), Submission.SrlExperiment.USERID_FIELD_NUMBER)
                .build()
                .equals(expected, result);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testThrowsExceptionIfNoDataIsSet() throws Exception {
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        databaseClient.saveExperiment(expected, 200);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testGetExperimentThatDoesNotExist() throws Exception {
        databaseClient.getExperiment(ObjectId.createFromLegacyFormat(0, 0, 0).toString(), PROBLEM_ID, null);
    }

    @Test
    public void testUpdateListAddsSaveMarker() throws Exception {

        final long submissionTime = Math.abs(new Random().nextLong());

        Submission.SrlSubmission.Builder usedList = Submission.SrlSubmission.newBuilder();
        usedList.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(SubmissionMergerTest.createSimpleDatabaseList(100))));
        Submission.SrlExperiment usedUpdate = getFakeExperiment("User1", usedList.build());
        String id = databaseClient.saveExperiment(usedUpdate, submissionTime);

        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);

        Commands.SrlUpdateList resultList = result.getSubmission().getSubmissionData().getSketchArea().getRecordedSketch();
        Commands.SrlUpdate lastUpdate = resultList.getList(resultList.getListCount() - 1);

        Commands.SrlCommand commands = lastUpdate.getCommands(0);
        // just a check to see if it is a marker
        Assert.assertEquals(Commands.CommandType.MARKER, commands.getCommandType());

        // just a silly check to see if it is Really a marker
        Commands.Marker.parseFrom(commands.getCommandData());

        Assert.assertEquals(submissionTime, lastUpdate.getTime());
    }

    @Test
    public void testFirstAndLastStroke() throws Exception {
        SketchArea.Builder usedList = SketchArea.newBuilder().setRecordedSketch(createSimpleDatabaseListWithSubmitMarker(200));

        Document basicObject = createUpdateList(usedList.build(), null, true, 300);
        Assert.assertEquals(basicObject.get(FIRST_STROKE_TIME), 110L);
        Assert.assertEquals(basicObject.get(FIRST_SUBMISSION_TIME), 200L);
    }

    @Test
    public void testUpdateListReplacesClientUpdateTime() throws Exception {
        final long submissionTime = Math.abs(new Random().nextLong()) + 200;

        Submission.SrlSubmission.Builder usedList = Submission.SrlSubmission.newBuilder();
        Commands.SrlUpdateList withSave = createSimpleDatabaseListWithSaveMarker(100);
        usedList.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(withSave)));
        Submission.SrlExperiment usedUpdate = getFakeExperiment("User1", usedList.build());
        String id = databaseClient.saveExperiment(usedUpdate, submissionTime);

        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);

        Commands.SrlUpdateList resultList = result.getSubmission().getSubmissionData().getSketchArea().getRecordedSketch();
        Commands.SrlUpdate lastUpdate = resultList.getList(resultList.getListCount() - 1);

        Commands.SrlCommand commands = lastUpdate.getCommands(0);
        // just a check to see if it is a marker
        Assert.assertEquals(Commands.CommandType.MARKER, commands.getCommandType());

        // just a silly check to see if it is Really a marker
        Commands.Marker.parseFrom(commands.getCommandData());

        Assert.assertEquals(submissionTime, lastUpdate.getTime());

        Commands.SrlUpdate clientUpdate = withSave.getList(resultList.getListCount() - 1);

        Assert.assertNotEquals(clientUpdate.getTime(), lastUpdate.getTime());
    }

    @Test
    public void testUpdateListMergesCorrectly() throws Exception {
        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        final Commands.SrlUpdateList original = createSimpleDatabaseListWithSaveMarker(200);
        build.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(original)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // round 2
        Submission.SrlSubmission.Builder secondList = Submission.SrlSubmission.newBuilder();
        secondList.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(
                        SubmissionMergerTest.createSimpleDatabaseListInsertSketchAt(original, 2, 300, true))));
        Submission.SrlExperiment secondSubmission = getFakeExperiment("User1", secondList.build());
        String secondId = databaseClient.saveExperiment(secondSubmission, 200);

        Assert.assertEquals(id, secondId);

        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, new AuthenticationResponder(
                Authentication.AuthResponse.newBuilder().setPermissionLevel(Authentication.AuthResponse.PermissionLevel.MODERATOR).build()));
        new ProtobufComparisonBuilder()
                .ignoreField(Submission.SrlSubmission.getDescriptor(), Submission.SrlSubmission.ID_FIELD_NUMBER)
                .ignoreField(Submission.SrlExperiment.getDescriptor(), Submission.SrlExperiment.USERID_FIELD_NUMBER)
                .build()
                .equals(secondSubmission, result);
    }

    @Test(expected = AuthenticationException.class)
    public void testSubmissionThrowsExceptionIfGivenWrongProblemId() throws Exception {
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        final Commands.SrlUpdateList original = createSimpleDatabaseListWithSaveMarker(200);
        build.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(original)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);
        // get experiment
        databaseClient.getExperiment(id, "NotProblemId", null);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testSwitchSubmissionTypeCausesProblemsTextFirst() throws Exception {
        final String textAnswer = "TEXT ANSWER";

        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setFreeResponse(
                FreeResponse.newBuilder().setStartingText(textAnswer)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // round 2
        Submission.SrlSubmission.Builder secondList = Submission.SrlSubmission.newBuilder();
        secondList.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(createSimpleDatabaseListWithSaveMarker(200))));
        Submission.SrlExperiment secondSubmission = getFakeExperiment("User1", secondList.build());
        String secondId = databaseClient.saveExperiment(secondSubmission, 200);

        Assert.assertEquals(null, secondId);
        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, null);
        Assert.assertEquals(secondSubmission, result);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testSwitchSubmissionTypeCausesProblemsAnswerChoiceSecond() throws Exception {
        final String textAnswer = "TEXT ANSWER";

        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setFreeResponse(FreeResponse.newBuilder().setStartingText(textAnswer)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // round 2
        Submission.SrlSubmission.Builder secondList = Submission.SrlSubmission.newBuilder();
        secondList.setSubmissionData(QuestionData.newBuilder().setMultipleChoice(MultipleChoice.newBuilder().setCorrectId("" + 94)));
        Submission.SrlExperiment secondSubmission = getFakeExperiment("User1", secondList.build());
        String secondId = databaseClient.saveExperiment(secondSubmission, 200);

        Assert.assertEquals(null, secondId);
        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, null);
        Assert.assertEquals(secondSubmission, result);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testSwitchSubmissionTypeCausesProblemsUpdateListFirst() throws Exception {
        final String textAnswer = "TEXT ANSWER";

        // round 1
        Submission.SrlSubmission.Builder secondList = Submission.SrlSubmission.newBuilder();
        secondList.setSubmissionData(QuestionData.newBuilder().setSketchArea(
                SketchArea.newBuilder().setRecordedSketch(createSimpleDatabaseListWithSaveMarker(200))));
        Submission.SrlExperiment secondSubmission = getFakeExperiment("User1", secondList.build());
        String secondId = databaseClient.saveExperiment(secondSubmission, 200);

        // round 2
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setFreeResponse(
                FreeResponse.newBuilder().setStartingText(textAnswer)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        Assert.assertEquals(null, secondId);
        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, null);
        Assert.assertEquals(secondSubmission, result);
    }

    @Test
    public void testTextSubmissionSavesCorrectly() throws Exception {
        final String textAnswer = "TEXT ANSWER";

        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setFreeResponse(
                FreeResponse.newBuilder().setStartingText(textAnswer)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);
        String resultAnswer = result.getSubmission().getSubmissionData().getFreeResponse().getStartingText();

        Assert.assertEquals(textAnswer, resultAnswer);
    }

    @Test
    public void testTextSubmissionUpdatesCorrectly() throws Exception {
        final String textAnswer = "TEXT ANSWER";
        final String textAnswer2 = "TEXT ANSWER2";

        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setFreeResponse(
                FreeResponse.newBuilder().setStartingText(textAnswer)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // round 2
        Submission.SrlSubmission.Builder secondList = Submission.SrlSubmission.newBuilder();
        secondList.setSubmissionData(QuestionData.newBuilder().setFreeResponse(
                FreeResponse.newBuilder().setStartingText(textAnswer2)));
        Submission.SrlExperiment secondSubmission = getFakeExperiment("User1", secondList.build());
        databaseClient.saveExperiment(secondSubmission, 200);

        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);
        String resultAnswer = result.getSubmission().getSubmissionData().getFreeResponse().getStartingText();

        Assert.assertEquals(textAnswer2, resultAnswer);
    }

    @Test
    public void testAnswerChoiceSubmissionSavesCorrectly() throws Exception {
        final String answerChoice = "" + 1;

        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setMultipleChoice(MultipleChoice.newBuilder().setCorrectId(answerChoice)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);
        String resultAnswer = result.getSubmission().getSubmissionData().getMultipleChoice().getCorrectId();

        Assert.assertEquals(answerChoice, resultAnswer);
    }

    @Test
    public void testAnswerChoiceSubmissionUpdatesCorrectly() throws Exception {
        final String textAnswer = "" + 2;
        final String textAnswer2 = "" + 50067;

        // round 1
        Submission.SrlSubmission.Builder build = Submission.SrlSubmission.newBuilder();
        build.setSubmissionData(QuestionData.newBuilder().setMultipleChoice(MultipleChoice.newBuilder().setCorrectId(textAnswer)));
        Submission.SrlExperiment expected = getFakeExperiment("User1", build.build());
        String id = databaseClient.saveExperiment(expected, 200);

        // round 2
        Submission.SrlSubmission.Builder secondList = Submission.SrlSubmission.newBuilder();
        secondList.setSubmissionData(QuestionData.newBuilder().setMultipleChoice(MultipleChoice.newBuilder().setCorrectId(textAnswer2)));
        Submission.SrlExperiment secondSubmission = getFakeExperiment("User1", secondList.build());
        databaseClient.saveExperiment(secondSubmission, 200);

        // get experiment
        Submission.SrlExperiment result = databaseClient.getExperiment(id, PROBLEM_ID, responder);
        String resultAnswer = result.getSubmission().getSubmissionData().getMultipleChoice().getCorrectId();

        Assert.assertEquals(textAnswer2, resultAnswer);
    }

    private Submission.SrlExperiment getFakeExperiment(String userId, Submission.SrlSubmission sub) {
        Submission.SrlExperiment.Builder build = Submission.SrlExperiment.newBuilder();
        build.setUserId(userId).setCourseId(COURSE_ID).setAssignmentId(ASSIGNMENT_ID).setProblemId(PROBLEM_ID)
                .setSubmission(sub);
        return build.build();
    }
}
